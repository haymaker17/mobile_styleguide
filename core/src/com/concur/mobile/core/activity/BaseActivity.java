/**
 * 
 */
package com.concur.mobile.core.activity;

import java.net.URI;
import java.util.HashMap;

import org.apache.http.HttpStatus;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.apptentive.android.sdk.Apptentive;
import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.fragment.RetainerFragment;
import com.concur.mobile.core.receiver.NetworkActivityReceiver;
import com.concur.mobile.core.receiver.NetworkActivityReceiver.INetworkActivityListener;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.ImageCache;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.core.view.ListItemAdapter;

/**
 * An extension of <code>BaseActivity</code> that can be used to keep track of service availability state.
 * 
 * @author AndrewK
 */
public class BaseActivity extends ActionBarActivity implements INetworkActivityListener {

    private static final String CLS_TAG = BaseActivity.class.getSimpleName();

    protected static final String RETAINER_TAG = "retainer.fragment";
    private final String CONFIG_CHANGE_RESTART_KEY = "config.changes";

    protected static final String ACTION_STATUS_ERROR_MESSAGE_KEY = "action.status.error.message";
    protected static final String LAST_HTTP_ERROR_MESSAGE = "http.error.message";

    public static final int REQUEST_UPLOAD_QUEUE = 32768;

    // The one RetainerFragment used to hold objects between activity recreates
    public RetainerFragment retainer;

    /**
     * Contains a reference to an intent filter used to register for service bound/unbound events.
     */
    private IntentFilter serviceBoundFilter;

    /**
     * Contains a reference to broadcast receiver to handle notifications of service availability.
     */
    private ServiceBoundReceiver serviceBoundReceiver;

    /**
     * Contains a reference to a broadcast receiver to handle notifications of images having been downloaded.
     */
    protected BroadcastReceiver imageCacheReceiver;

    /**
     * Contains a reference to a broadcast receiver to handle notifications when the (server) system is unavailable.
     */
    protected BroadcastReceiver systemUnavailableReceiver;

    /**
     * Contains a reference to a broadcast receiver to handle changes in data connectivity.
     */
    protected BroadcastReceiver offlineConnectivityReceiver;

    /**
     * Flag to determine whether or not offline notification is enabled for this Activity.
     */
    protected boolean offlineNotificationEnabled;

    /**
     * Contains whether or not the image cache receiver has been registered.
     */
    private boolean imageCacheReceiverRegistered;

    /**
     * Contains whether or not the system unavailable receiver has been registered.
     */
    private boolean systemUnavailableReceiverRegistered;

    /**
     * Contains whether or not the receiver is currently registered.
     */
    private boolean receiverRegistered;

    private boolean userConfigReceiverRegistered;

    private boolean sysConfigReceiverRegistered;

    /**
     * Contains whether or not the service is currently bound.
     */
    private boolean serviceBound;

    /**
     * Contains whether or not the user config is currently bound.
     */
    private boolean isUserConfigAvail;
    private boolean isSysConfigAvail;

    /**
     * Contains whether or not the building of the view is delayed until the service is available.
     */
    protected boolean buildViewDelay;

    /**
     * Contains whether or not the handling of a call from 'onActivityResult' was delayed due to the view not being present.
     */
    protected boolean activityResultDelay;

    /**
     * Contains the request code from the delayed handling of the 'onActivityResult' call.
     */
    protected int activityResultRequestCode;

    /**
     * Contains the result code from the delayed handling of the 'onActivityResult' call.
     */
    protected int activityResultResultCode;

    /**
     * Contains the intent data from the delayed handling of the 'onActivityResult' call.
     */
    protected Intent activityResultData;

    /**
     * Contains whether or not the handling of a call from 'onRestoreInstanceState' was delayed due to the view not being present.
     */
    protected boolean restoreInstanceStateDelay;

    /**
     * Contains a copy of the bundle that was passed to 'onRestoreInstanceState'.
     */
    protected Bundle restoreInstanceStateData;

    /**
     * Contains whether or not this activity was launched as a result of an orientation change.
     */
    public boolean orientationChange;

    /**
     * Contains last error message returned from a service request.
     */
    public String actionStatusErrorMessage;

    /**
     * Contains the last http error message returned from a service request.
     */
    public String lastHttpErrorMessage;

    /**
     * Contains a reference to a broadcast receiver to handle network activity progress indicator.
     */
    protected NetworkActivityReceiver networkActivityReceiver;

    /**
     * Contains a reference to a broadcast receiver to handle User config progress indicator.
     */
    protected UserConfigServiceReciever userConfigServiceReciever;

    protected SystemConfigServiceReciever sysConfigServiceReciever;

    /**
     * Contains a reference to an intent filter used to register the network activity receiver.
     */
    protected IntentFilter networkActivityFilter;

    /**
     * Contains a reference to an intent filter used to register the user config receiver.
     */
    protected IntentFilter userConfigIntentFilter;

    protected IntentFilter sysConfigIntentFilter;

    /**
     * Contains a reference to the view flipper.
     */
    protected ViewFlipper viewFlipper;

    /**
     * A reference to most recently created Dialog. Note that this Dialog may be null or not shown (i.e. it is dismissed).
     */
    protected Dialog currProgressDialog;

    /**
     * An enum defining a few view states.
     */
    public enum ViewState {
        LOCAL_DATA, // Indicates there is local data being viewed.
        LOCAL_DATA_REFRESH, // Indicates viewing local data with background
                            // fetch on-going.
        RESTORE_APP_STATE, // Indicates the application is restoring state.
        NO_LOCAL_DATA_REFRESH, // No local data present, server refresh
                               // happening.
        NO_DATA
        // No data either locally or from the server.
    };

    /**
     * Contains the current view state.
     */
    protected ViewState viewState;

    /**
     * Contains whether during the 'onCreate' call the Concur Service was not available. This is typically an indication that the
     * application was re-started. Sub-classes of this activity that utilize this boolean value to determine the best way to
     * re-build displays.
     */
    public boolean appRestarted;

    /**
     * Contains a map from the view state to a child index of the view flipper.
     */
    protected HashMap<ViewState, Integer> viewStateFlipChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initRetainerFragment();

        serviceBoundFilter = new IntentFilter(Const.ACTION_CONCUR_SERVICE_BOUND);
        serviceBoundFilter.addAction(Const.ACTION_CONCUR_SERVICE_UNBOUND);
        serviceBoundReceiver = new ServiceBoundReceiver();
        Intent stickyIntent = registerReceiver(serviceBoundReceiver, serviceBoundFilter);
        if (stickyIntent != null) {
            serviceBound = stickyIntent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_SERVICE_BOUND);
            appRestarted = !serviceBound;
        }
        receiverRegistered = true;

        userConfigIntentFilter = new IntentFilter(Const.ACTION_CONCUR_USER_CONFIG_AVAIL);
        userConfigIntentFilter.addAction(Const.ACTION_CONCUR_USER_CONFIG_UNAVAIL);
        userConfigServiceReciever = new UserConfigServiceReciever();
        Intent stickyUserConfigIntent = registerReceiver(userConfigServiceReciever, userConfigIntentFilter);
        if (stickyUserConfigIntent != null) {
            isUserConfigAvail = stickyUserConfigIntent.getAction().equalsIgnoreCase(
                    Const.ACTION_CONCUR_USER_CONFIG_AVAIL);
        }
        userConfigReceiverRegistered = true;

        sysConfigIntentFilter = new IntentFilter(Const.ACTION_CONCUR_SYS_CONFIG_AVAIL);
        sysConfigIntentFilter.addAction(Const.ACTION_CONCUR_SYS_CONFIG_UNAVAIL);
        sysConfigServiceReciever = new SystemConfigServiceReciever();
        Intent stickySysConfigIntent = registerReceiver(sysConfigServiceReciever, sysConfigIntentFilter);
        if (stickySysConfigIntent != null) {
            isSysConfigAvail = stickySysConfigIntent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_SYS_CONFIG_AVAIL);
        }
        sysConfigReceiverRegistered = true;

        if (savedInstanceState != null) {
            // Restore the orientationChange flag.
            int configChanges = savedInstanceState.getInt(CONFIG_CHANGE_RESTART_KEY);
            orientationChange = (configChanges != 0);
            // Restore any action status error message.
            if (savedInstanceState.containsKey(ACTION_STATUS_ERROR_MESSAGE_KEY)) {
                actionStatusErrorMessage = savedInstanceState.getString(ACTION_STATUS_ERROR_MESSAGE_KEY);
            }
            // Restore any last http error message.
            if (savedInstanceState.containsKey(LAST_HTTP_ERROR_MESSAGE)) {
                lastHttpErrorMessage = savedInstanceState.getString(LAST_HTTP_ERROR_MESSAGE);
            }
        }

        // Register the network activity receiver.
        registerNetworkActivityReceiver();

        registerSystemUnavailableReceiver();

        // Check Intent options if we should register offline notification.
        // We're defaulting to show the offline notification on all screens.
        offlineNotificationEnabled = getIntent().getBooleanExtra(Const.EXTRA_ENABLE_OFFLINE_MODE_NOTIFICATION, true);
        registerOfflineConnectivityReceiver();
        updateOfflineHeaderBar(ConcurCore.isConnected());

    }

    protected void initRetainerFragment() {
        FragmentManager fm = getSupportFragmentManager();

        retainer = (RetainerFragment) fm.findFragmentByTag(RETAINER_TAG);
        if (retainer == null) {
            retainer = new RetainerFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(retainer, RETAINER_TAG);
            ft.commit();
        }
    }

    /**
     * Gets the instance of <code>RetainerFragment</code> used to store data.
     * 
     * @return returns the instance of <code>RetainerFragment</code> used to store data.
     */
    public RetainerFragment getRetainer() {
        return retainer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;
        switch (id) {
        case Const.DIALOG_SYSTEM_UNAVAILABLE: {
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            dlg = ConcurCore.createDialog(this, id);
            break;
        }
        default: {
            // Fall through to letting the application create the dialog.
            dlg = getConcurCore().createDialog(this, id);
            currProgressDialog = dlg;
            break;
        }
        }
        return dlg;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // If being destroyed due to a non-configuration change, then
        // unregister the image cache receiver.
        if (getChangingConfigurations() == 0) {
            unregisterImageCacheReceiver();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start the event tracking.
        EventTracker.INSTANCE.activityStart(this);

        // Start Apptentive.
        Apptentive.onStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!receiverRegistered) {
            // Register the receiver.
            Intent stickyIntent = registerReceiver(serviceBoundReceiver, serviceBoundFilter);
            if (stickyIntent != null) {
                serviceBound = stickyIntent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_SERVICE_BOUND);
            }
            receiverRegistered = true;
        }
        if (!userConfigReceiverRegistered) {
            // Register the receiver.
            Intent stickyIntent = registerReceiver(userConfigServiceReciever, userConfigIntentFilter);
            if (stickyIntent != null) {
                isUserConfigAvail = stickyIntent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_USER_CONFIG_AVAIL);
            }
            userConfigReceiverRegistered = true;
        }

        if (!sysConfigReceiverRegistered) {
            // Register the receiver.
            Intent stickyIntent = registerReceiver(sysConfigServiceReciever, sysConfigIntentFilter);
            if (stickyIntent != null) {
                isSysConfigAvail = stickyIntent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_SYS_CONFIG_AVAIL);
            }
            sysConfigReceiverRegistered = true;
        }

        // Registger the network activity receiver.
        registerNetworkActivityReceiver();

        registerSystemUnavailableReceiver();

        registerOfflineConnectivityReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiverRegistered) {
            unregisterReceiver(serviceBoundReceiver);
            receiverRegistered = false;
        }

        if (userConfigReceiverRegistered) {
            unregisterReceiver(userConfigServiceReciever);
            userConfigReceiverRegistered = false;
        }

        if (sysConfigReceiverRegistered) {
            unregisterReceiver(sysConfigServiceReciever);
            sysConfigReceiverRegistered = false;
        }

        // Unregister the network activity receiver.
        unregisterNetworkActivityReceiver();

        unregisterSystemUnavailableReceiver();

        unregisterOfflineConnectivityReceiver();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the configuration reason for the stopping. Non-zero means some
        // kind of config change.
        outState.putInt(CONFIG_CHANGE_RESTART_KEY, getChangingConfigurations());

        // Save any error message information.
        if (actionStatusErrorMessage != null) {
            outState.putString(ACTION_STATUS_ERROR_MESSAGE_KEY, actionStatusErrorMessage);
        }
        // Save last http error message.
        if (lastHttpErrorMessage != null) {
            outState.putString(LAST_HTTP_ERROR_MESSAGE, lastHttpErrorMessage);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        EventTracker.INSTANCE.activityStop(this);

        Apptentive.onStop(this);
    }

    /**
     * Called when the concur service component has become available.
     */
    protected void onServiceAvailable() {
    }

    /**
     * Called when the concur service component has become unavailable.
     */
    protected void onServiceUnavailable() {
    }

    /**
     * Called when the user config component has become available.
     */
    protected void onUserConfigAvailable() {
    }

    /**
     * Called when the user config component has become unavailable.
     */
    protected void onSysConfigUnavailable() {
    }

    /**
     * Called when the user config component has become available.
     */
    protected void onSysConfigAvailable() {
    }

    /**
     * Called when the user config component has become unavailable.
     */
    protected void onUserConfigUnavailable() {
    }

    /**
     * Gets whether or not the service is currently available.
     * 
     * @return whether the service is currently available.
     */
    public boolean isServiceAvailable() {
        return serviceBound;
    }

    /**
     * Gets whether or not the user config is currently available.
     * 
     * @return whether the userconfig is currently available.
     */
    public boolean isUserConfigAvailable() {
        return isUserConfigAvail;
    }

    public boolean isSysConfigAvailable() {
        return isSysConfigAvail;
    }

    /**
     * Gets the current session ID.
     * 
     * @return the current session ID.
     */
    protected String getSessionID() {
        return Preferences.getSessionId();
    }

    /**
     * Gets the cross-session user id based on the current login.
     * 
     * @return the cross-session user id associated with the current login.
     */
    public String getUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        return userId;
    }

    /**
     * Sets the instance of <code>BroadcastReceiver</code> that will handle notifications from the <code>ImageCache</code> of
     * background image load completion.
     * 
     * @param imageCacheReceiver
     *            contains an instance of <code>BroadcastReceiver</code>.
     */
    public void setImageCacheReceiver(BroadcastReceiver imageCacheReceiver) {
        this.imageCacheReceiver = imageCacheReceiver;
    }

    /**
     * Gets the <code>ConcurCore</code> application instance.
     * 
     * @return the <code>ConcurCore</code> application instance.
     */
    public ConcurCore getConcurCore() {
        return (ConcurCore) getApplication();
    }

    /**
     * Gets the <code>ConcurService</code> instance associated with the application.
     * 
     * @return the <code>ConcurService</code> instance associated with the application.
     */
    public ConcurService getConcurService() {
        return getConcurCore().getService();
    }

    /**
     * Will register the network activity receiver.
     */
    protected void registerNetworkActivityReceiver() {
        if (networkActivityReceiver == null) {
            networkActivityReceiver = new NetworkActivityReceiver(this, this);
            if (networkActivityFilter == null) {
                networkActivityFilter = new IntentFilter(Const.ACTION_NETWORK_ACTIVITY_START);
                networkActivityFilter.addAction(Const.ACTION_NETWORK_ACTIVITY_STOP);
            }
            registerReceiver(networkActivityReceiver, networkActivityFilter);
        }
    }

    /**
     * Will unregister the network activity receiver.
     */
    protected void unregisterNetworkActivityReceiver() {
        if (networkActivityReceiver != null) {
            unregisterReceiver(networkActivityReceiver);
            networkActivityReceiver = null;
        }
    }

    /**
     * Will register a receiver to handle data connectivity changes.
     */
    protected void registerOfflineConnectivityReceiver() {
        if (offlineConnectivityReceiver == null && offlineNotificationEnabled) {

            // Inner anonymous class to trigger offline/online mode.
            offlineConnectivityReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction() != null) {
                        updateOfflineHeaderBar(intent.getAction().equalsIgnoreCase(
                                com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_AVAILABLE));
                    }
                }
            };

            IntentFilter connectivityFilter = new IntentFilter(
                    com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_AVAILABLE);
            connectivityFilter
                    .addAction(com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_UNAVAILABLE);

            registerReceiver(offlineConnectivityReceiver, connectivityFilter);
        }
    }

    /**
     * Will unregister a receiver to handle data connectivity changes.
     */
    protected void unregisterOfflineConnectivityReceiver() {
        if (offlineConnectivityReceiver != null && offlineNotificationEnabled) {
            unregisterReceiver(offlineConnectivityReceiver);
            offlineConnectivityReceiver = null;
        }
    }

    /**
     * Will register a receiver to handle image download notifications.
     */
    public void registerImageCacheReceiver() {
        if (imageCacheReceiver != null && !imageCacheReceiverRegistered) {
            getApplicationContext().registerReceiver(imageCacheReceiver,
                    new IntentFilter(ImageCache.IMAGE_DOWNLOAD_ACTION));
            imageCacheReceiverRegistered = true;
        }
    }

    /**
     * Will unregister a receiver to handle image download notifications.
     */
    protected void unregisterImageCacheReceiver() {
        if (imageCacheReceiver != null && imageCacheReceiverRegistered) {
            getApplicationContext().unregisterReceiver(imageCacheReceiver);
            imageCacheReceiverRegistered = false;
        }
    }

    /**
     * Will register a receiver to handle when the (server) system is unavailable.
     */
    protected void registerSystemUnavailableReceiver() {
        if (systemUnavailableReceiver == null || !systemUnavailableReceiverRegistered) {

            systemUnavailableReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (currProgressDialog != null && currProgressDialog.isShowing()) {
                        currProgressDialog.dismiss();
                        currProgressDialog = null;
                    }
                    BaseActivity.this.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                }
            };

            getApplicationContext().registerReceiver(systemUnavailableReceiver,
                    new IntentFilter(Const.ACTION_NETWORK_SYSTEM_UNAVAILABLE));
            systemUnavailableReceiverRegistered = true;
        }
    }

    /**
     * Will unregister a receiver to handle when the (server) system is unavailable.
     */
    protected void unregisterSystemUnavailableReceiver() {
        if (systemUnavailableReceiver != null && systemUnavailableReceiverRegistered) {
            getApplicationContext().unregisterReceiver(systemUnavailableReceiver);
            systemUnavailableReceiverRegistered = false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #isNetworkRequestInteresting(int)
     * 
     * Sub-classes
     */
    @Override
    public boolean isNetworkRequestInteresting(int networkMsgType) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #getNetworkActivityText(int, java.lang.String)
     */
    @Override
    public String getNetworkActivityText(int networkMsgType, String defaultText) {
        return defaultText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #networkActivityStarted(int)
     */
    @Override
    public void networkActivityStarted(int networkMsgType) {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #networkActivityStopped(int)
     */
    @Override
    public void networkActivityStopped(int networkMsgType) {
        // No-op.
    }

    /**
     * Called whenever the network data connectivity changes online or offline.
     * 
     * @param available
     *            <code>true</code> if network data connectivity is available, otherwise <code>false</code>
     */
    protected void updateOfflineHeaderBar(boolean available) {
        if (offlineNotificationEnabled) {
            View offlineHeader = findViewById(R.id.offline_header);
            if (offlineHeader != null) {
                if (available && offlineHeader.getVisibility() == View.VISIBLE) {
                    offlineHeader.setVisibility(View.GONE);
                } else if (!available && offlineHeader.getVisibility() == View.GONE) {
                    offlineHeader.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * Count the number of items waiting for upload. NOTE: This method should not be invoked on the UI thread.
     * 
     * @return A count of the total expenses and standalone receipts waiting to be uploaded
     */
    protected int getOfflineItemCount() {
        int count = -1;

        final ConcurService concurService = getConcurService();
        if (concurService != null && concurService.prefs != null) {
            final String userId = concurService.prefs.getString(Const.PREF_USER_ID, null);
            final MobileDatabase mdb = concurService.getMobileDatabase();
            if (userId != null && mdb != null) {
                count = mdb.getOfflineExpenseCount(userId) + mdb.getOfflineReceiptCount(userId);
            }
        }
        return count;
    }

    /**
     * Update offiline queue bar.
     * */
    public void updateOfflineQueueBar() {

        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                // Get the number of offline items
                int count = getOfflineItemCount();

                if (count == -1) {
                    // Stuff isn't ready. Do a one-time sleep in our thread.
                    try {
                        Thread.sleep(200);
                        count = getOfflineItemCount();
                    } catch (InterruptedException e) {
                    }
                }

                return count;
            }

            @Override
            protected void onPostExecute(Integer count) {
                View offlineHeader = findViewById(R.id.offline_update_queue);
                if (offlineHeader != null) {
                    // set visibility
                    if (count <= 0) {
                        // -1 indicates a problem getting the count
                        offlineHeader.setVisibility(View.GONE);
                    } else {
                        offlineHeader.setVisibility(View.VISIBLE);

                        // enable action(s) required on visibility
                        TextView numberOfItemsText = (TextView) findViewById(R.id.offline_update_numberofItems);
                        numberOfItemsText.setText(getResources().getQuantityString(
                                R.plurals.offline_upload_queue_header, count, count));
                        offlineHeader.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(BaseActivity.this, OffLineUploadList.class);
                                startActivityForResult(intent, REQUEST_UPLOAD_QUEUE);
                            }
                        });
                    }

                }
            }

        }.execute();

    }

    /**
     * Will flip the current view based on the value of <code>viewState</code>.
     */
    protected void flipViewForViewState() {
        if (viewFlipper != null) {
            if (viewStateFlipChild != null) {
                if (viewStateFlipChild.containsKey(viewState)) {
                    int newChildInd = viewStateFlipChild.get(viewState);
                    int curChildInd = viewFlipper.getDisplayedChild();
                    if (newChildInd != curChildInd) {
                        viewFlipper.setDisplayedChild(newChildInd);
                        setFlipViewText(viewFlipper.getCurrentView());
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".flipViewForViewState: current view state '" + viewState
                            + "' not in map!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".flipViewForViewState: null view state flip child map!");
            }
        }
    }

    /**
     * Sets the text string, if any, in <code>view</code> based on the current value of <code>viewState</code>.
     * 
     * @param view
     *            the view on which to set the text.
     */
    protected void setFlipViewText(View view) {

        switch (viewState) {
        case NO_DATA: {
            TextView txtView = (TextView) view.findViewById(R.id.no_data_text);
            if (txtView != null) {
                txtView.setText(getNoDataTextResourceId());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setFlipViewText: unable to locate 'no data' text view!");
            }
            break;
        }
        case NO_LOCAL_DATA_REFRESH: {
            TextView txtView = (TextView) view.findViewById(R.id.no_local_data_server_refresh_text);
            if (txtView != null) {
                txtView.setText(getNoLocalDataRefreshTextResourceId());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".setFlipViewText: unable to locate 'no local data server refresh' text view!");
            }
            break;
        }
        }
    }

    /**
     * Gets the resource id of the text string to be displayed if there exists no data to be presented in this activity.
     * 
     * @return the resource id of the text string to be displayed if there exists no data to be presented in this activity.
     */
    protected int getNoDataTextResourceId() {
        return 0;
    }

    /**
     * Gets the resource id of the text string to be displayed if there exists no local data but with an outstanding request to
     * retrieve data.
     * 
     * @return the resource id of the text string to be displayed if there exists no local data, but with an outstanding request
     *         to retrieve data.
     */
    protected int getNoLocalDataRefreshTextResourceId() {
        return 0;
    }

    /**
     * Whether the currently logged in end-user is a rail user.
     * 
     * @return whether the currently logged in end-user is a rail user.
     */
    protected boolean isRailUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(Const.PREF_CAN_RAIL, false);
    }

    /**
     * Whether the currently logged in end-user is a taxi user.
     * 
     * @return whether the currently logged in end-user is a taxi user.
     */
    protected boolean isTaxiUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(Const.PREF_CAN_TAXI, false);
    }

    /**
     * Whether the currently logged in end-user is a travel user.
     * 
     * @return whether the currently logged in end-user is a rail user.
     */
    protected boolean isTravelUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(Const.PREF_CAN_TRAVEL, false);
    }

    /**
     * Whether the currently logged in end-user is a travel points user.
     * 
     * @return
     */
    protected boolean isTravelPointsUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(Const.PREF_TRAVEL_POINTS_USER, false);
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle receiving notifications of when our service is bound/unbound.
     * 
     * @author AndrewK
     */
    class ServiceBoundReceiver extends BroadcastReceiver {

        private final String CLS_TAG = BaseActivity.CLS_TAG + "." + ServiceBoundReceiver.class.getSimpleName();

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_SERVICE_BOUND)) {
                serviceBound = true;
                onServiceAvailable();
            } else if (intent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_SERVICE_UNBOUND)) {
                serviceBound = false;
                onServiceUnavailable();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unhandled action '" + intent.getAction() + ".");
            }
        }
    }

    /**
     * 
     * An abstract extension of <code>BroadcastReceiver</code>.
     * 
     * @param <A>
     *            an extension Type of <code>BaseActivity</code>
     * @param <S>
     *            an extension Type of <code>ServiceRequest</code>
     */
    protected abstract static class BaseBroadcastReceiver<A extends BaseActivity, S extends ServiceRequest> extends
            BroadcastReceiver {

        private static final String CLS_TAG = BaseBroadcastReceiver.class.getSimpleName();

        /**
         * Contains the activity associated with this receiver.
         */
        protected A activity;
        /**
         * Contains the intent that was passed to the receiver's 'onReceive' method.
         */
        protected Intent intent;

        /**
         * Contains a reference to an outstanding service request for which this receiver is waiting on a reply.
         */
        protected S serviceRequest;

        /**
         * Constructs an instance of <code>BaseBroadcastReceiver</code> associated with <code>activity</code>.
         * 
         * @param activity
         *            the associated activity.
         */
        protected BaseBroadcastReceiver(A activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        public void setActivity(A activity) {
            this.activity = activity;
            if (this.activity != null) {
                setActivityServiceRequest(serviceRequest);
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Gets the activity associated with this broadcast receiver.
         * 
         * @return the activity associated with this broadcast receiver.
         */
        public A getActivity() {
            return this.activity;
        }

        /**
         * Gets the service request associated with this broadcast receiver.
         * 
         * @return the servicer request associated with this broadcast receiver.
         */
        public S getServiceRequest() {
            return this.serviceRequest;
        }

        /**
         * Sets the service request associated with this broadcast receiver.
         * 
         * @param serviceRequest
         *            the service request to be associated with this broadcast receiver.
         */
        public void setServiceRequest(S serviceRequest) {
            this.serviceRequest = serviceRequest;
        }

        /**
         * Sets the service request associated with this receiver on the associated activity.
         * 
         * @param request
         *            the request.
         */
        protected abstract void setActivityServiceRequest(S request);

        /**
         * Clears the request associated with an activity.
         * 
         * @param activity
         *            the activity with which to clear the request.
         */
        protected abstract void clearActivityServiceRequest(A activity);

        /**
         * Unregisters this receiver.
         */
        protected abstract void unregisterReceiver();

        /**
         * Will handle a success scenario.
         * 
         * @param context
         *            the receiver context.
         * @param intent
         *            the intent.
         */
        protected abstract void handleSuccess(Context context, Intent intent);

        /**
         * Will handle a failure scenario.
         * 
         * @param context
         *            the receiver context.
         * @param intent
         *            the intent.
         */
        protected abstract void handleFailure(Context context, Intent intent);

        /**
         * Provides a notification that the status of the request was either not found in <code>intent</code> or was found and did
         * not have a value of <code>Const.SERVICE_REQUEST_STATUS_OKAY</code>.
         * 
         * <br>
         * <b>NOTE:</b><br>
         * Default implementation is a no-op.
         * 
         * @param context
         *            the context.
         * @param intent
         *            the intent.
         * @param requestStatus
         *            the request status within <code>intent</code>. Should be one of
         *            <code>Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST, Const.SERVICE_REQUEST_STATUS_IO_ERROR</code> or
         *            <code>-1</code> if in the case of no request status found in <code>intent</code>.
         */
        protected void handleRequestFailure(Context context, Intent intent, int requestStatus) {
            // No-op.
        }

        /**
         * Provides a notification that either the http status is missing from <code>intent</code> or has a value that is not
         * equal to <code>HttpStatus.SC_OK</code>.
         * 
         * <br>
         * <b>NOTE:</b><br>
         * Default implementation is a no-op.
         * 
         * @param context
         *            the context
         * @param intent
         *            the intent
         * @param httpStatus
         *            the http status. Will have a value of <code>-1</code> if no http status code found in <code>intent</code>.
         * @return returns whether or not the HttpError was handled.
         */
        protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
            // No-op.
            boolean retVal = false;

            return retVal;
        }

        /**
         * Will handle dismissing any dialog displayed while the receiver is waiting on a result. <br>
         * <br>
         * <b>NOTE:</b><br>
         * Default implementation is a no-op.
         * 
         * @param context
         *            the context.
         * @param intent
         *            the intent.
         */
        protected void dismissRequestDialog(Context context, Intent intent) {
            // No-op.
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Does this receiver have a current activity?
            if (activity != null) {

                // Check that if there is a service request message ID in the
                // intent data
                // that is matches with the message ID on the request associated
                // with this
                // handler.
                String requestMessageId = intent.getStringExtra(Const.SERVICE_REQUEST_MESSAGE_ID);
                if (serviceRequest != null && serviceRequest.messageId != null && requestMessageId != null
                        && !serviceRequest.messageId.equalsIgnoreCase(requestMessageId)) {
                    return;
                }

                // Unregister the receiver.
                unregisterReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)
                                        || intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                                Const.REPLY_STATUS_OK)) {
                                    // Handle the success.
                                    handleSuccess(context, intent);
                                    try {
                                        // Dismiss the dialog.
                                        dismissRequestDialog(context, intent);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                } else {
                                    // Set the error message.
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);

                                    handleFailure(context, intent);

                                    try {
                                        // Dismiss the dialog.
                                        dismissRequestDialog(context, intent);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                }
                            } else {
                                // Set the error message.
                                activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage);

                                if (!handleHttpError(context, intent, httpStatusCode)) {
                                    activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                }

                                try {
                                    // Dismiss the dialog.
                                    dismissRequestDialog(context, intent);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");

                            handleHttpError(context, intent, -1);

                            try {
                                // Dismiss the dialog.
                                dismissRequestDialog(context, intent);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    } else {
                        if (serviceRequest != null && !serviceRequest.isCanceled()) {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + activity.lastHttpErrorMessage);

                            handleRequestFailure(context, intent, serviceRequestStatus);

                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                            try {
                                // Dismiss the dialog.
                                dismissRequestDialog(context, intent);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    }
                } else {

                    handleRequestFailure(context, intent, -1);

                    try {
                        // Dismiss the dialog.
                        dismissRequestDialog(context, intent);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear out the request reference.
                clearActivityServiceRequest(activity);
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    class UserConfigServiceReciever extends BroadcastReceiver {

        private final String CLS_TAG = BaseActivity.CLS_TAG + "." + UserConfigServiceReciever.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_USER_CONFIG_AVAIL)) {
                isUserConfigAvail = true;
                onUserConfigAvailable();
            } else if (intent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_USER_CONFIG_UNAVAIL)) {
                isUserConfigAvail = false;
                onUserConfigUnavailable();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unhandled action '" + intent.getAction() + ".");
            }
        }
    }

    class SystemConfigServiceReciever extends BroadcastReceiver {

        private final String CLS_TAG = BaseActivity.CLS_TAG + "." + SystemConfigServiceReciever.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_SYS_CONFIG_AVAIL)) {
                isSysConfigAvail = true;
                onSysConfigAvailable();
            } else if (intent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_SYS_CONFIG_UNAVAIL)) {
                isSysConfigAvail = false;
                onSysConfigUnavailable();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unhandled action '" + intent.getAction() + ".");
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for the purposes of handling a notification that an image has been
     * downloaded.
     * 
     * @deprecated - use {@link com.concur.platform.ui.common.util.ImageCacheReceiver} instead.
     */
    public static class ImageCacheReceiver<T extends ListItem> extends BroadcastReceiver {

        private static final String CLS_TAG = BaseActivity.CLS_TAG + "." + ImageCacheReceiver.class.getSimpleName();

        private ListItemAdapter<T> listItemAdapter;

        private ListView listView;

        /**
         * Constructs an instance of <code>ImageCacheReceiver</code> with an list adapter and view.
         * 
         * @param listItemAdapter
         *            contains the list item adapter.
         * @param listView
         *            contains the list view.
         */
        public ImageCacheReceiver(ListItemAdapter<T> listItemAdapter, ListView listView) {
            this.listItemAdapter = listItemAdapter;
            this.listView = listView;
        }

        /**
         * Will reset the list adapter and view.
         * 
         * @param listItemAdapter
         *            contains the list item adapter.
         * @param listView
         *            contains the list view.
         */
        protected void setListAdapter(ListItemAdapter<T> listItemAdapter, ListView listView) {
            this.listItemAdapter = listItemAdapter;
            this.listView = listView;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(ImageCache.IMAGE_DOWNLOAD_ACTION)) {
                boolean result = intent.getBooleanExtra(ImageCache.EXTRA_IMAGE_DOWNLOAD_RESULT, false);
                if (result) {
                    URI uri = (URI) intent.getSerializableExtra(ImageCache.EXTRA_IMAGE_DOWNLOAD_URI);
                    if (listItemAdapter != null) {
                        listItemAdapter.refreshView(listView, uri);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: listItemAdapter is null!");
                    }
                }
            }
        }

    }

    /**
     * Show the dialog - reserve not allowed
     * 
     * @param maxEnforcementViolationMsg
     *            - violation message
     */
    // MOB-14778
    protected void showReserveNotAllowed(String maxEnforcementViolationMsg) {
        final AlertDialogFragment frag = new AlertDialogFragment();
        frag.setTitle(R.string.general_booking_not_allowed_title);
        // set the message from violation else the generic message
        if (maxEnforcementViolationMsg != null && maxEnforcementViolationMsg.trim().length() > 0) {
            frag.setMessage(maxEnforcementViolationMsg);
        } else {
            frag.setMessage(R.string.general_booking_not_allowed_message);
        }

        frag.setPositiveButtonText(R.string.okay);

        frag.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                frag.dismiss();
            }

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }
        });
        frag.show(getSupportFragmentManager(), null);
    }

}
