package com.concur.mobile.platform.ui.travel.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.concur.mobile.platform.ui.common.fragment.RetainerFragmentV1;
import com.concur.mobile.platform.ui.travel.receiver.NetworkActivityReceiver;
import com.concur.mobile.platform.ui.travel.receiver.NetworkActivityReceiver.INetworkActivityListener;
import com.concur.mobile.platform.ui.travel.util.Const;

/**
 * Activity that can be used to keep track of service availability state. This is a copy of <code>BaseActivity</code> from core
 * 
 * @see com.concur.mobile.core.activity.BaseActivity
 * 
 * @author RatanK
 * 
 */
public class BaseActivity extends Activity implements INetworkActivityListener {

    private static final String CLS_TAG = BaseActivity.class.getSimpleName();

    protected static final String RETAINER_TAG = "retainer.fragment";
    private final String CONFIG_CHANGE_RESTART_KEY = "config.changes";

    protected static final String ACTION_STATUS_ERROR_MESSAGE_KEY = "action.status.error.message";
    protected static final String LAST_HTTP_ERROR_MESSAGE = "http.error.message";

    // The one RetainerFragment used to hold objects between activity recreates
    public RetainerFragmentV1 retainer;

    /**
     * Contains a reference to an intent filter used to register for service bound/unbound events.
     */
    private IntentFilter serviceBoundFilter;

    /**
     * Contains a reference to broadcast receiver to handle notifications of service availability.
     */
    private ServiceBoundReceiver serviceBoundReceiver;

    /**
     * Contains a reference to a broadcast receiver to handle notifications when the (server) system is unavailable.
     */
    protected BroadcastReceiver systemUnavailableReceiver;

    /**
     * Contains a reference to a broadcast receiver to handle changes in data connectivity.
     */
    protected BroadcastReceiver offlineConnectivityReceiver;

    /**
     * Contains a reference to a broadcast receiver to handle network activity progress indicator.
     */
    protected NetworkActivityReceiver networkActivityReceiver;

    /**
     * Contains a reference to an intent filter used to register the network activity receiver.
     */
    protected IntentFilter networkActivityFilter;

    /**
     * Flag to determine whether or not offline notification is enabled for this Activity.
     */
    protected boolean offlineNotificationEnabled;

    /**
     * Contains whether or not the service is currently bound.
     */
    private boolean serviceBound;

    /**
     * Contains whether or not the receiver is currently registered.
     */
    private boolean receiverRegistered;

    /**
     * Contains whether during the 'onCreate' call the Concur Service was not available. This is typically an indication that the
     * application was re-started. Sub-classes of this activity that utilize this boolean value to determine the best way to
     * re-build displays.
     */
    public boolean appRestarted;

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
     * Contains whether or not the system unavailable receiver has been registered.
     */
    private boolean systemUnavailableReceiverRegistered;

    /**
     * A reference to most recently created Dialog. Note that this Dialog may be null or not shown (i.e. it is dismissed).
     */
    protected Dialog currProgressDialog;

    @Override
    public boolean isNetworkRequestInteresting(int networkMsgType) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void networkActivityStarted(int networkMsgType) {
        // TODO Auto-generated method stub

    }

    @Override
    public void networkActivityStopped(int networkMsgType) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getNetworkActivityText(int networkMsgType, String defaultText) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the instance of <code>RetainerFragmentV1</code> used to store data.
     * 
     * @return returns the instance of <code>RetainerFragment</code> used to store data.
     */
    public RetainerFragmentV1 getRetainer() {
        return retainer;
    }

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

        // TODO - how to get this offline check from core ????
        // updateOfflineHeaderBar(ConcurCore.isConnected());

    }

    protected void initRetainerFragment() {
        FragmentManager fm = getFragmentManager();

        retainer = (RetainerFragmentV1) fm.findFragmentByTag(RETAINER_TAG);
        if (retainer == null) {
            retainer = new RetainerFragmentV1();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(retainer, RETAINER_TAG);
            ft.commit();
        }
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
                    // TODO - toast is temporary, remove it and show dialog
                    Toast.makeText(getApplicationContext(), "system unavialable", Toast.LENGTH_SHORT);
                    // BaseActivity.this.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
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
     * Called whenever the network data connectivity changes online or offline.
     * 
     * @param available
     *            <code>true</code> if network data connectivity is available, otherwise <code>false</code>
     */
    protected void updateOfflineHeaderBar(boolean available) {
        if (offlineNotificationEnabled) {
            // TODO - toast is temporary, remove it ans show theoff line header
            Toast.makeText(this, "offline", Toast.LENGTH_SHORT);
            // View offlineHeader = findViewById(R.id.offline_header);
            // if (offlineHeader != null) {
            // if (available && offlineHeader.getVisibility() == View.VISIBLE) {
            // offlineHeader.setVisibility(View.GONE);
            // } else if (!available && offlineHeader.getVisibility() == View.GONE) {
            // offlineHeader.setVisibility(View.VISIBLE);
            // }
            // }
        }
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
     * An extension of <code>BroadcastReceiver</code> to handle receiving notifications of when our service is bound/unbound.
     * 
     * @author RatanK
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

}
