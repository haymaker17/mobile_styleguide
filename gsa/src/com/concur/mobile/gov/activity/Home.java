package com.concur.mobile.gov.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.fragment.navigation.DefaultNavigationItem;
import com.concur.mobile.core.fragment.navigation.DefaultSimpleNavigationItem;
import com.concur.mobile.core.fragment.navigation.DefaultTextNavigationItem;
import com.concur.mobile.core.fragment.navigation.Navigation.CustomNavigationItem;
import com.concur.mobile.core.fragment.navigation.Navigation.NavigationItem;
import com.concur.mobile.core.fragment.navigation.Navigation.NavigationListener;
import com.concur.mobile.core.fragment.navigation.Navigation.SimpleNavigationItem;
import com.concur.mobile.core.fragment.navigation.Navigation.TextNavigationItem;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.CorpSsoQueryReply;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.service.ItineraryRequest;
import com.concur.mobile.core.travel.service.ItinerarySummaryListRequest;
import com.concur.mobile.core.util.ConcurException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.charge.activity.UnAppliedExpenseListActivty;
import com.concur.mobile.gov.expense.doc.activity.Expense;
import com.concur.mobile.gov.expense.doc.authorization.activity.AuthorizationListActivity;
import com.concur.mobile.gov.expense.doc.stamp.activity.StampDocumentListActivity;
import com.concur.mobile.gov.expense.doc.voucher.activity.VouchersListActivity;
import com.concur.mobile.gov.service.GovMessagesReply;
import com.concur.mobile.gov.travel.activity.GovSegmentList;
import com.concur.mobile.gov.travel.activity.GovTripList;
import com.concur.mobile.gov.travel.activity.TravelAuthType;
import com.concur.mobile.gov.util.GovFlurry;
import com.concur.mobile.gov.util.TravelBookingCache;
import com.concur.mobile.gov.util.TravelBookingCache.BookingSelection;

public class Home extends BaseActivity implements View.OnClickListener, NavigationListener {

    private static final String CLS_TAG = Home.class.getSimpleName();

    private final SparseArray<Dialog> dialogs = new SparseArray<Dialog>(3);

    private static final String ITINERARY_RECEIVER_KEY = "itinerary.receiver";
    private static final int LOG_OUT_REQ_CODE = 100;

    protected int inProgressRef = 0;

    /**
     * Flag to gate data requests on service availability
     */
    boolean needService = false;
    // Contains the action status error message returned from the server.
    private String actionStatusErrorMessage;
    // Contains the last http error message.
    private String lastHttpErrorMessage;
    // Contains the current itinerary summary list request object.
    private ItinerarySummaryListRequest itinerarySummaryListRequest;
    // Contains the receiver used to handle the results of an itinerary request.
    private ItineraryReceiver itineraryReceiver;
    // Contains the filter used to register the itinerary receiver.
    private IntentFilter itineraryFilter;
    // Contains a reference to the currently outstanding itinerary request.
    private ItineraryRequest itineraryRequest;
    protected static final String EXPIRE_LOGIN = "expire_login";
    public static ExpireLoginHandler sHandler = null;
    protected static Home liveHome;
    protected boolean loginHasExpired = false;

    private static final int NAVIGATION_BOOK_TRAVEL = 0;
    private static final int NAVIGATION_SETTINGS_HEADER = 1;
    private static final int NAVIGATION_SETTINGS = 2;

    // Boolean value for flurry events
    private boolean isFromMoreMenu = false;
    //
    protected MenuItem refreshActionItem;
    protected View refreshProgressView;

    // Navigation Drawer items
    protected DrawerLayout mDrawerLayout;
    protected ScrollView mDrawerView;
    protected ActionBarDrawerToggle mDrawerToggle;

    /**
     * A custom handler to catch a message about the login session expiring.
     */
    protected static class ExpireLoginHandler extends Handler {

        ExpireLoginHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (liveHome != null && !liveHome.loginHasExpired) {
                switch (msg.what) {
                case 1:
                    Log.d(Const.LOG_TAG, "login expired, resetting");
                    liveHome.loginHasExpired = true;
                    Intent home = new Intent(liveHome, Home.class);
                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    home.putExtra(EXPIRE_LOGIN, true);
                    liveHome.startActivity(home);
                    break;
                }
            }
        }
    }

    /**
     * An access method to be used from another thread (presumably the
     * ConcurServiceHandler) to indicate that the session has expired and the user
     * needs to login again. The end result will be a cleared task stack with the
     * Login activity sitting at the root. See
     * ConcurServiceHandler.handleMessage() for the invocation.
     */
    public static void expireLogin() {
        if (sHandler == null) {
            Log.d(Const.LOG_TAG, "creating expire handler");
            // Create a handler for the message making sure to specify the main
            // looper because
            // this should be coming in from another thread.
            sHandler = new ExpireLoginHandler(Looper.getMainLooper());
        }
        sHandler.dispatchMessage(sHandler.obtainMessage(1));
    }

    // Contains the receiver to handle responses for home screen data.
    private final BroadcastReceiver dataReceiver = new BroadcastReceiver() {

        /**
         * Receive notification that some piece of data has been retrieved.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Const.ACTION_SUMMARY_TRIPS_UPDATED.equals(action)) {
                updateTripInfo(intent);
            } else if (Const.ACTION_DATABASE_RESET.equals(action)) {

                updateTripUI();

                // Go get the data
                loadData();

                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_REFRESH_DATA);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_SETTINGS, Flurry.EVENT_NAME_ACTION,
                    params);
            }
        }
    };
    // Contains whether our data receiver has been registered.
    private boolean dataReceiverRegistered = false;
    // Contains the intent filter used to register the data receiver.
    private final IntentFilter dataReceiverFilter = new IntentFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Track this activity in a static for use by the expired login handler.
        // This is nulled in onDestroy to hopefully prevent a leak.
        liveHome = this;
        // Indicate to flurry if this is a breeze user
        String isBreezeUser = "No";
        if (isBreezeUser()) {
            isBreezeUser = "Yes";
        }
        HashMap<String, String> flurryParam = new HashMap<String, String>(1);
        flurryParam.put("Is Breeze User", isBreezeUser);
        EventTracker.INSTANCE.track(getClass().getSimpleName(), "Is Breeze User", flurryParam);
        setContentView(R.layout.home);

        // Initialize the Navigation DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_frame);

        // Tweak the action bar
        final ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
            | ActionBar.DISPLAY_USE_LOGO, ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
            | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);

        actionBar.setLogo(R.drawable.concur_logo);

        // The button that toggles the drawer between open and close
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.concur_logo, R.string.empty_string,
            R.string.empty_string);

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        Intent launchIntent = getIntent();

        // init navigation

        initNavigationMenu();

        if (launchIntent.hasExtra(EXPIRE_LOGIN)
            || (savedInstanceState != null && savedInstanceState.containsKey(EXPIRE_LOGIN))) {
            // We're here because the session has expired or is about to and we
            // can't get a new one automagically. Bail.
            // Immediately clear the session.
            Preferences.clearSession(PreferenceManager.getDefaultSharedPreferences(this));
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setMessage(R.string.login_expired);
            b.setCancelable(false);
            b.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // In this case, the end-user didn't explicitly log out,
                    // i.e., their session
                    // expired on them. In that case, for company SSO, we'll
                    // take them to company SSO
                    // page if they have one. If not, then go to the PIN-based
                    // login screen.
                    GovAppMobile govApp = (GovAppMobile) getApplication();
                    CorpSsoQueryReply ssoQueryReply = govApp.getCorpSsoQueryReply();
                    Intent login = null;
                    if (ssoQueryReply != null && ssoQueryReply.ssoEnabled && ssoQueryReply.ssoUrl != null) {
                        // Company sign-on.
                        login = new Intent(Home.this, Login.class);
                        login.putExtra(CompanyLogin.EXTRA_ADVANCE_TO_COMPANY_SIGN_ON, true);
                    } else {
                        // PIN-based login.
                        login = new Intent(Home.this, Login.class);
                        login.putExtra(com.concur.mobile.platform.ui.common.util.Const.EXTRA_LOGOUT, true);
                    }
                    startActivity(login);
                    dialog.dismiss();
                    finish();
                }
            });
            final AlertDialog dlg = b.create();
            dlg.show();
        }
        // Hide the travel UI elements, if need be.
        boolean isTraveler = isTraveler();
        boolean isItinViewer = isItinViewer();
        // ItinViewer is the new role indicating a non-cliqbook (TMC) company
        // Due to server issues, both flags may be set. If so, make TU trump IV.
        if (isTraveler) {
            isItinViewer = false;
        }
        if (isItinViewer) {
            hideBookingUI();
        } else if (!isTraveler) {
            hideTravelUI();
        }
        // Add expense related broadcast receivers.
        dataReceiverFilter.addAction(Const.ACTION_SUMMARY_UPDATED);
        // We will receive this once a database reset has occurred.
        dataReceiverFilter.addAction(Const.ACTION_DATABASE_RESET);

        // Add traveler broadcast receiver.
        if (isTraveler || isItinViewer) {
            dataReceiverFilter.addAction(Const.ACTION_SUMMARY_TRIPS_UPDATED);
        }
        // Register the receiver.
        registerReceiver(dataReceiver, dataReceiverFilter);
        dataReceiverRegistered = true;

        GovAppMobile govApp = (GovAppMobile) getApplication();
        if (govApp.isPrivacyActNoticeShow()) {
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_PRIVACY_ACT_NOTICE);
        }
        // Restore any receivers.
        restoreReceivers();
        // If the home screen was re-started due to a non-orientation change,
        // then set the flag
        // to refetch the itinerary summary list.
        if (!orientationChange) {
            IItineraryCache itinCache = getConcurCore().getItinCache();
            if (itinCache != null) {
                itinCache.setShouldRefetchSummaryList(true);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: itinerary cache is null!");
            }
        }
    }

    /**
     * Will initialize the navigation menu fragment.
     */
    private void initNavigationMenu() {

        List<NavigationItem> navItems = new ArrayList<NavigationItem>();

        // Should allow and travel user check.
        if (Preferences.shouldAllowTravelBooking() && isTraveler()) {
            HomeScreenSimpleNavigationItem navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_BOOK_TRAVEL, -1,
                R.string.home_navigation_book_travel, R.drawable.icon_menu_book, View.VISIBLE, View.VISIBLE, new Runnable() {

                    @Override
                    public void run() {
                        View homeBook = findViewById(R.id.homeBook);
                        if (homeBook != null) {
                            isFromMoreMenu = true;
                            onClick(homeBook);
                            // Flurry Notifications.
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_BOOK_TRAVEL);
                           EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME_MORE,
                                Flurry.EVENT_NAME_ACTION, params);
                        }
                    }
                });
            navItems.add(navItem);
        }

        // Add the settings navigation segment bar.
        DefaultTextNavigationItem setSegNavItem = new DefaultTextNavigationItem(NAVIGATION_SETTINGS_HEADER,
            R.layout.navigation_segment, R.string.home_navigation_settings, false);
        navItems.add(setSegNavItem);

        // Add the Settings navigation item.
        HomeScreenSimpleNavigationItem setNavItem = new HomeScreenSimpleNavigationItem(NAVIGATION_SETTINGS, -1,
            R.string.home_navigation_settings, R.drawable.icon_menu_settings, View.VISIBLE, View.VISIBLE, new Runnable() {

                @Override
                public void run() {

                    // Launch the activity.
                    Intent i = new Intent(Home.this, Preferences.class);
                    startActivity(i);
                }
            });
        navItems.add(setNavItem);
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout itemContainer = (LinearLayout) findViewById(R.id.drawer_item_container);

        addNavigationItems(itemContainer, inflater, navItems);
    }

    /**
     * Goes through all of the items to be added to the Navigation Drawer and adds them one at a time.
     * 
     * @param itemContainer
     *            The LinearLayout inside of the Navigation Drawer that holds all of the NavigationItems
     * 
     * @param inflater
     *            The LayoutInflater
     * 
     * @param navItems
     *            The actual list of Navigation Items that we're to add to the Drawer
     * 
     */
    protected void addNavigationItems(ViewGroup itemContainer, LayoutInflater inflater, List<NavigationItem> navItems) {
        if (navItems != null) {
            for (NavigationItem navItem : navItems) {
                if ((navItem instanceof TextNavigationItem) || (navItem instanceof SimpleNavigationItem)) {
                    int layResId = R.layout.navigation_item;
                    if (navItem.getLayoutResId() != -1) {
                        layResId = navItem.getLayoutResId();
                    }
                    View navView = inflater.inflate(layResId, null, false);
                    navView.setId(navItem.getId());
                    int txtResId = -1;
                    if (navItem instanceof TextNavigationItem) {
                        txtResId = ((TextNavigationItem) navItem).getTextResId();
                    }
                    if (txtResId != -1) {
                        TextView txtView = (TextView) navView.findViewById(R.id.text);
                        if (txtView != null) {
                            txtView.setText(txtResId);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                + ".addNavigationItems: unable to locate 'text' view in 'navigation_item' layout!");
                        }
                    }
                    int iconResId = -1;
                    int iconVisibility = View.VISIBLE;
                    if (navItem instanceof SimpleNavigationItem) {
                        iconVisibility = ((SimpleNavigationItem) navItem).getIconVisibility();
                        if (iconVisibility == View.VISIBLE) {
                            iconResId = ((SimpleNavigationItem) navItem).getIconResourceId();
                            ImageView imgView = (ImageView) navView.findViewById(R.id.icon);
                            if (imgView != null) {
                                imgView.setImageResource(iconResId);
                            } else {
                                Log.e(Const.LOG_TAG,
                                    CLS_TAG
                                        + ".addNavigationItems: unable to locate 'icon' view in 'navigation_item' layout!");
                            }
                        } else {
                            ViewUtil.setVisibility(navView, R.id.icon, View.GONE);
                        }
                    } else {
                        ViewUtil.setVisibility(navView, R.id.icon, View.GONE);
                    }
                    navView.setTag(navItem);
                    itemContainer.addView(navView); // PARENT IS JUST ITEMCONTAINER
                    // Is the item selectable?
                    if (navItem.isSelectable()) {
                        navView.setOnClickListener(new NavigationItemOnClickListener());
                    }
                } else if (navItem instanceof CustomNavigationItem) {
                    View customView = ((CustomNavigationItem) navItem).getView();
                    if (customView != null) {
                        customView.setTag(navItem);
                        // Is the item selectable?
                        if (navItem.isSelectable()) {
                            customView.setOnClickListener(new NavigationItemOnClickListener());
                        }
                        itemContainer.addView(customView);
                    }
                }
            }
        }
    }

    /**
     * The OnClickListener for items inside of the Navigation Drawer. Basically just check which item was clicked and hand off to
     * the {@link #onItemSelected(navItem) onItemSelected} method to handle which item was pressed.
     * 
     */
    class NavigationItemOnClickListener implements OnClickListener {

        public void onClick(View view) {
            if (view.getTag() != null) {
                try {
                    NavigationItem navItem = (NavigationItem) view.getTag();
                    onItemSelected(navItem);
                } catch (ClassCastException ccExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: view tag object not instance of 'NavigationItem'!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: view has null tag!");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Restore any receivers.
        restoreReceivers();
        // Re-register the data receiver, if need be.
        if (!dataReceiverRegistered) {
            registerReceiver(dataReceiver, dataReceiverFilter);
            dataReceiverRegistered = true;
        }
        // Go get the data
        if (isServiceAvailable()) {
            if (!orientationChange) {
                loadData();
            } else {
                // Clear the orientation change flag.
                orientationChange = false;
                // Immediately update the trip information.
                updateTripUI();
            }
        } else {
            needService = true;
        }
        updateOfflineQueueBar();
    }

    protected void restoreReceivers() {
        // Restore any retained data
        if (retainer.contains(ITINERARY_RECEIVER_KEY)) {
            itineraryReceiver = (ItineraryReceiver) retainer.get(ITINERARY_RECEIVER_KEY);
            itineraryReceiver.setActivity(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (dataReceiverRegistered) {
            unregisterReceiver(dataReceiver);
            dataReceiverRegistered = false;
            // Clear out the reference count as unregistering the
            // data receiver from above can result in missed replies
            // which means 'inProgressRef' not getting decrementing.
            // When the activity resumes, 'inProgressRef' will be incremented
            // further and as a result will only go to '0' if the activity
            // is re-created.
            clearInProgressRef();
        }
        // Save the itinerary receiver.
        if (itineraryReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            itineraryReceiver.setActivity(null);
            // Store it in the retainer
            retainer.put(ITINERARY_RECEIVER_KEY, itineraryReceiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        liveHome = null;
        // Unregister the receiver.
        if (dataReceiverRegistered) {
            unregisterReceiver(dataReceiver);
            dataReceiverRegistered = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Indicate if we are in an expired state. This will get us to
        // exit if someone rotates while the expire dialog is up.
        if (loginHasExpired) {
            outState.putBoolean(EXPIRE_LOGIN, true);
        }
    }

    @Override
    protected void onServiceAvailable() {
        if (ConcurCore.isConnected()) {
            // Upload any saved exceptions. Occurs in a background task and we
            // do not care about success/fail.
            ConcurException.processSavedExceptions((ConcurCore) getApplication());
        }
        // Only do this if we previously knew the service was down. We don't
        // want to spin
        // useless requests if we already have our data.
        if (needService) {
            if (!orientationChange) {
                loadData();
            } else {
                // Clear the orientation change flag.
                orientationChange = false;
                // Immediately update the trip information.
                updateTripUI();
            }
            needService = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        // Grab our action view for showing/hiding
        refreshActionItem = menu.findItem(R.id.menuRefresh);
        refreshProgressView = MenuItemCompat.getActionView(refreshActionItem);

        if (inProgressRef == 0) {
            // Toggle the progress indicator
            MenuItemCompat.setActionView(refreshActionItem, null);
        }
        menu.removeItem(R.id.menuMessageCenter);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!ConcurCore.isConnected()) {
            menu.findItem(R.id.menuReset).setVisible(false);
        } else {
            menu.findItem(R.id.menuReset).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
        case android.R.id.home: {
            mDrawerToggle.onOptionsItemSelected(item);
        }
        case R.id.menuRefresh:
            loadData();
            break;
        case R.id.menuSettings: {
            i = new Intent(this, Preferences.class);
            startActivity(i);
            break;
        }
        case R.id.menuReset: {
            if (ConcurCore.isConnected()) {
                GovAppMobile app = (GovAppMobile) getApplication();
                // Only perform a clear if there are no outstanding requests
                // for data.
                if (inProgressRef == 0) {
                    // Clear everything
                    app.clearLocalData();
                }
            }
            break;
        }
        case R.id.menuLogout: {
            logout();
            break;
        }

        }
        return super.onOptionsItemSelected(item);
    }

    /** logout from app */
    private void logout() {
        if (GovAppMobile.isConnected()) {
            // Connected, so send a logout request.
            GovAppMobile govConcurMobile = (GovAppMobile) getApplication();
            govConcurMobile.getService().sendLogoutRequest();
            // TODO cdiaz: call new platform logout
        } else {
            // Not connected, immediately clear the session.
            Preferences.clearSession(PreferenceManager.getDefaultSharedPreferences(this));
        }
        // Clear from in-memory or persistence any company sign-on
        // information.
        GovAppMobile govConcurMobile = (GovAppMobile) getApplication();
        govConcurMobile.setCorpSsoQueryReply(null);
        govConcurMobile.getService().clearCorpSSoQueryReply();
        ViewUtil.clearWebViewCache(this);
        Intent i = new Intent(this, Login.class);
        i.putExtra(com.concur.mobile.platform.ui.common.util.Const.EXTRA_LOGOUT, true);
        startActivity(i);
        // Flurry Notification
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_LOGOUT);
       EventTracker.INSTANCE.track(Flurry.CATEGORY_SETTINGS, Flurry.EVENT_NAME_ACTION, params);

        finish();
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        super.onContextItemSelected(item);
        Intent i;
        boolean handled = true;
        GovAppMobile app = ((GovAppMobile) getApplication());
        // clear cache..
        app.trvlBookingCache = new TravelBookingCache();
        switch (item.getItemId()) {
        case R.id.menuHomeBookAir:
            // Check whether user has permission to book air via mobile.
            if (ViewUtil.isAirUser(this)) {
                // Check for a complete travel profile.
                if (ViewUtil.isTravelProfileComplete(this)
                    || ViewUtil.isTravelProfileCompleteMissingTSA(this)) {
                    // i = new Intent(this, AirSearch.class);
                    i = new Intent(this, TravelAuthType.class);
                    app.trvlBookingCache.setSelectedBookingType(BookingSelection.AIR);
                    if (isFromMoreMenu) {
                        i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                        isFromMoreMenu = false;
                    } else {
                        i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
                    }
                    startActivity(i);
                } else {
                    showDialog(Const.DIALOG_TRAVEL_PROFILE_INCOMPLETE);
                }
            } else {
                showDialog(Const.DIALOG_TRAVEL_NO_AIR_PERMISSION);
            }
            break;
        case R.id.menuHomeBookCar:
            // i = new Intent(this, CarSearch.class);
            i = new Intent(this, TravelAuthType.class);
            app.trvlBookingCache.setSelectedBookingType(BookingSelection.CAR);
            if (isFromMoreMenu) {
                i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                isFromMoreMenu = false;
            } else {
                i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
            }
            startActivity(i);
            break;
        case R.id.menuHomeBookHotel:
            // i = new Intent(this, HotelSearch.class);
            i = new Intent(this, TravelAuthType.class);
            app.trvlBookingCache.setSelectedBookingType(BookingSelection.HOTEL);
            if (isFromMoreMenu) {
                i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                isFromMoreMenu = false;
            } else {
                i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
            }
            startActivityForResult(i, Const.REQUEST_CODE_BOOK_HOTEL);
            break;
        case R.id.menuHomeBookRail:
            // i = new Intent(this, RailSearch.class);
            app.trvlBookingCache.setSelectedBookingType(BookingSelection.RAIL);
            i = new Intent(this, TravelAuthType.class);
            if (isFromMoreMenu) {
                i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                isFromMoreMenu = false;
            } else {
                i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
            }
            startActivity(i);
            break;
        default:
            handled = false;
        }
        return handled;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        android.view.MenuInflater infl = getMenuInflater();
        switch (v.getId()) {
        case R.id.homeBook:
            infl.inflate(R.menu.home_book, menu);
            menu.setHeaderTitle(R.string.home_action_title);
            if (!isRailUser()) {
                // You've been thrown from the train
                menu.removeItem(R.id.menuHomeBookRail);
            }
            break;
        }
    }

    @Override
    public void onClick(View v) {
        Intent i = null;
        Integer requestCode = null;
        switch (v.getId()) {
        case R.id.homeBook: {
            // MOB-11313
            if (!ConcurCore.isConnected()) {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            } else {
                registerForContextMenu(v);
                openContextMenu(v);
            }
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_BOOK_TRAVEL);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME_MORE, Flurry.EVENT_NAME_ACTION,
                params);
            break;
        }
        case R.id.homeTrips: {
            cancelAllDataRequests();
            i = new Intent(this, GovTripList.class);
            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_VIEW_TRIPS);
           EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
            break;
        }
        case R.id.homeQuickExpense: {
            i = new Intent(this, Expense.class);
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_QUICK_EXPENSE);
           EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
            break;
        }

        case R.id.homeAuthorizations: {
            cancelAllDataRequests();
            i = new Intent(this, AuthorizationListActivity.class);
            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_VIEW_AUTH_LIST);
           EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
            break;
        }

        case R.id.homeVouchers: {
            cancelAllDataRequests();
            i = new Intent(this, VouchersListActivity.class);
            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_VIEW_VCH_LIST);
           EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
            break;
        }
        case R.id.homeStampDocuments: {
            cancelAllDataRequests();
            i = new Intent(this, StampDocumentListActivity.class);
            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_VIEW_STAMP_LIST);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
            break;
        }
        case R.id.homeExpense: {
            cancelAllDataRequests();
            i = new Intent(this, UnAppliedExpenseListActivty.class);
            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_VIEW_EXPENSE_LIST);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
            break;
        }
        }
        if (i != null) {
            if (requestCode == null) {
                startActivity(i);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = dialogs.get(id);
        if (dlg == null) {
            switch (id) {
            case Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY: {
                dlg = super.onCreateDialog(id);
                dlg.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Cancel any outstanding request.
                        if (itineraryRequest != null) {
                            itineraryRequest.cancel();
                        }
                    }
                });
                break;
            }
            case com.concur.mobile.gov.util.Const.DIALOG_PRIVACY_ACT_NOTICE: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                GovAppMobile concurMobile = (GovAppMobile) getApplication();
                GovMessagesReply reply = concurMobile.getMsgs();
                if (reply != null) {
                    dlgBldr.setTitle(reply.privacyTitle);
                    dlgBldr.setMessage(reply.privacyText);
                } else {
                    dlgBldr.setTitle(R.string.gov_privacy_act_notice_title);
                    dlgBldr.setMessage("");
                }
                dlgBldr.setCancelable(false);
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if ((isShowSafeHarborAgreement())) {
                            showRules();
                        } else {
                            ((GovAppMobile) getApplication()).setShowPrivacyActNotice(false);
                        }
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            default:
                dlg = ((GovAppMobile) getApplication()).createDialog(this, id);
                dialogs.put(id, dlg);
                break;
            }
        }
        return dlg;
    }

    /***
     * Show Behavior Text and Behavior Rules for Gov User.
     * */
    private void showRules() {
        // May be onactivityresult required..
        Intent it = new Intent(Home.this, GovRulesActivity.class);
        startActivityForResult(it, LOG_OUT_REQ_CODE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        default: {
            super.onPrepareDialog(id, dialog);
            break;
        }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case Const.REQUEST_CODE_BOOK_HOTEL: {
            if (resultCode == Activity.RESULT_OK) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GovAppMobile
                    .getContext());
                if (Preferences.shouldPromptToRate(prefs, false)) {
                    showDialog(Const.DIALOG_PROMPT_TO_RATE);
                }
            }
            break;
        }
        case LOG_OUT_REQ_CODE: {
            if (resultCode == RESULT_CANCELED) {
                logout();
            }
            break;
        }
        }
    }

    protected void incrInProgressRef() {
        if (++inProgressRef > 0) {
            if (refreshActionItem != null && refreshProgressView != null) {
                // Toggle the progress indicator
                MenuItemCompat.setActionView(refreshActionItem, refreshProgressView);
            }
        }
    }

    protected void decrInProgressRef() {
        if (--inProgressRef < 1) {
            if (refreshActionItem != null && refreshProgressView != null) {
                // Toggle the progress indicator
                MenuItemCompat.setActionView(refreshActionItem, null);
            }
        }
    }

    protected void clearInProgressRef() {
        inProgressRef = 0;

        if (refreshActionItem != null) {
            // Clear the progress indicator
            MenuItemCompat.setActionView(refreshActionItem, null);
        }
    }

    protected void loadData() {
        GovAppMobile govConcurMobile = (GovAppMobile) getApplication();
        // get car config data immediately show cached data.
        govConcurMobile.getSystemConfig();
        // get car config data immediately show cached data.
        govConcurMobile.getUserConfig();
        // Request trips if traveler or itinerary viewer.
        if (isTraveler() || isItinViewer()) {
            // Immediately show the cached data
            govConcurMobile.getService().getItinerarySummaryList();
            updateTripUI();
            // If connected, then send a request for an updated itinerary list.
            if (GovAppMobile.isConnected()) {
                itinerarySummaryListRequest = govConcurMobile.getService().sendItinerarySummaryListRequest(true);
                // Don't increment the progress reference count if the
                // request wasn't created.
                if (itinerarySummaryListRequest != null) {
                    incrInProgressRef();
                }
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        }
    }

    private void cancelAllDataRequests() {
        ServiceRequest request = itinerarySummaryListRequest;
        if (request != null) {
            request.cancel();
        }
    }

    /**
     * Will update the trip-related information displayed on the screen.
     */
    protected void updateTripInfo(Intent intent) {
        int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
        if (serviceRequestStatus != -1) {
            if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                if (httpStatusCode != -1) {
                    if (httpStatusCode == HttpStatus.SC_OK) {
                        if (!(intent.getStringExtra(Const.REPLY_STATUS)
                            .equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS))) {
                            actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                + actionStatusErrorMessage + ".");
                        }
                    } else {
                        lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- " + lastHttpErrorMessage
                            + ".");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                }
            } else {
                if (itinerarySummaryListRequest != null && !itinerarySummaryListRequest.isCanceled()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                        + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
        }
        updateTripUI();
        decrInProgressRef();
        // Reset the request.
        itinerarySummaryListRequest = null;
    }

    /**
     * Will update the trips UI indicating there is no trip data available.
     */
    private void updateTripNoDataAvailable() {
    }

    /**
     * Will examine the current set of trips and update the UI accordingly.
     */
    private void updateTripUI() {
        if (isTraveler() || isItinViewer()) {
            GovAppMobile govConcurMobile = (GovAppMobile) getApplication();
            IItineraryCache itinCache = govConcurMobile.getItinCache();
            if (itinCache != null && itinCache.getItinerarySummaryListUpdateTime() != null) {
                // Clear current trip
                List<Trip> trips = itinCache.getItinerarySummaryList();
                if (trips != null && trips.size() > 0) {
                    // Sort the trips into active and upcoming
                    Calendar curTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    Iterator<Trip> tripIter = trips.iterator();
                    List<Trip> activeTrips = new ArrayList<Trip>(3);
                    List<Trip> upcomingTrips = new ArrayList<Trip>(5);
                    while (tripIter.hasNext()) {
                        Trip trip = tripIter.next();
                        // See if the trip ends in the future
                        if (trip.endUtc != null && trip.endUtc.after(curTime)) {
                            // See if the start was in the past. That makes it
                            // active.
                            if (trip.startUtc != null && trip.startUtc.before(curTime)) {
                                // Active trip
                                activeTrips.add(trip);
                            } else {
                                // Must be in the future. Add it to upcoming.
                                upcomingTrips.add(trip);
                            }
                        }
                    }
                    final int activeTripCount = activeTrips.size();
                    final int upcomingTripCount = upcomingTrips.size();
                    if (activeTripCount > 0 || upcomingTripCount > 0) {
                        TextView sub = (TextView) findViewById(R.id.tripSubheader);

                        String subHeader = getResources().getQuantityString(R.plurals.home_row_travel_subheader,
                            upcomingTripCount);
                        sub.setText(Format.localizeText(this, subHeader, Integer.toString(activeTripCount),
                            Integer.toString(upcomingTripCount)));
                    } else {
                        // No upcoming trips.
                        updateTripNoDataAvailable();
                    }
                } else {
                    // No trips.
                    updateTripNoDataAvailable();
                }
            } else {
                // Indicate to the end-user there is no trip data available.
                updateTripNoDataAvailable();
            }
        }
    }

    /**
     * Sets the visibility property on a view to <code>View.GONE</code>.
     * 
     * @param resId
     *            the resource id of the view.
     */
    private void setViewGone(int resId) {
        View view = findViewById(resId);
        if (view != null) {
            if (view.getVisibility() != View.GONE) {
                view.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Will set the visibility on a view to <code>View.VISIBLE</code>.
     * 
     * @param resId
     *            the resource id of the view.
     */
    private void setViewVisible(int resId) {
        View view = findViewById(resId);
        if (view != null) {
            if (view.getVisibility() != View.VISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Will set the visibility on a view to <code>View.INVISIBLE</code>.
     * 
     * @param resId
     *            the resource id of the view.
     */
    private void setViewInvisible(int resId) {
        View view = findViewById(resId);
        if (view != null) {
            if (view.getVisibility() != View.INVISIBLE) {
                view.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Will hide all travel UI elements.
     */
    private void hideTravelUI() {
        hideTripsUI();
        hideBookingUI();
    }

    /**
     * Hides any trip-related elements
     */
    private void hideTripsUI() {
        // Hide the section.
        setViewGone(R.id.homeTrips);
    }

    /**
     * Hides any elements that book through cliqbook
     */
    private void hideBookingUI() {
        // Hide the row
        setViewGone(R.id.homeBook);
    }

    /**
     * An implementation of <code>DialogInterface.OnCancelListener</code> to
     * handle canceling the mileage expense action.
     */
    class DialogCancelListener implements DialogInterface.OnCancelListener {

        /*
         * (non-Javadoc)
         * 
         * @see android.content.DialogInterface.OnCancelListener#onCancel(android
         * .content.DialogInterface)
         */
        @Override
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
        }
    }

    /**
     * Whether the currently logged in end-user is a traveler.
     * 
     * @return whether the currently logged in end-user is a traveler.
     */
    private boolean isTraveler() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(Const.PREF_CAN_TRAVEL, false);
    }

    /**
     * Whether the currently logged in end-user is an itinerary viewer.
     * 
     * @return whether the currently logged in end-user is an itinerary viewer.
     */
    private boolean isItinViewer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(Const.PREF_IS_ITIN_VIEWER, false);
    }

    /**
     * Whether the currently logged in end-user is a Breeze user.
     * 
     * @return Whether the currently logged in end-user is a Breeze user.
     */
    private boolean isBreezeUser() {
        boolean isBreezeUser = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String entityType = prefs.getString(Const.PREF_ENTITY_TYPE, null);
        if (entityType != null) {
            isBreezeUser = entityType.equalsIgnoreCase(Const.ENTITY_TYPE_BREEZE);
        }
        return isBreezeUser;
    }

    /**
     * Will send a request to obtain an itinerary.
     */
    protected void sendItineraryRequest(String itinLocator) {
        ConcurService concurService = getConcurService();
        registerItineraryReceiver();
        itineraryRequest = concurService.sendItineraryRequest(itinLocator);
        if (itineraryRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendItineraryRequest: unable to create itinerary request.");
            unregisterItineraryReceiver();
        } else {
            // Set the request object on the receiver.
            itineraryReceiver.setServiceRequest(itineraryRequest);
            // Show the dialog.
            showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
        }
    }

    /**
     * Will register an itinerary receiver.
     */
    private void registerItineraryReceiver() {
        if (itineraryReceiver == null) {
            itineraryReceiver = new ItineraryReceiver(this);
            if (itineraryFilter == null) {
                itineraryFilter = new IntentFilter(Const.ACTION_TRIP_UPDATED);
            }
            getApplicationContext().registerReceiver(itineraryReceiver, itineraryFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerItineraryReceiver: itineraryReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an itinerary receiver.
     */
    private void unregisterItineraryReceiver() {
        if (itineraryReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(itineraryReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterItinerarySReceiver: illegal argument", ilaExc);
            }
            itineraryReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterItineraryReceiver: itineraryReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of
     * handling the results of an itinerary list request.
     */
    static class ItineraryReceiver extends BaseBroadcastReceiver<Home, ItineraryRequest> {

        /**
         * Constructs an instance of <code>ItineraryReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ItineraryReceiver(Home activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(Home activity) {
            activity.itineraryRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * dismissRequestDialog(android.content.Context, android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * handleFailure(android.content.Context, android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * handleSuccess(android.content.Context, android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            if (intent.hasExtra(Const.EXTRA_ITIN_LOCATOR)) {
                String itinLocator = intent.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
                if (itinLocator != null) {
                    Intent i = new Intent(activity, GovSegmentList.class);
                    i.putExtra(Const.EXTRA_ITIN_LOCATOR, itinLocator);
                    activity.startActivity(i);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: itin locator has invalid value!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: itin locator missing!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity,
         * com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ItineraryRequest request) {
            activity.itineraryRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterItineraryReceiver();
        }
    }

    /**
     * Whether the currently logged in end-user did agree with gov Rules.
     * 
     * @return whether the currently logged in end-user did agree with rules.
     */
    private boolean isShowSafeHarborAgreement() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(Const.PREF_NSH_AGREE, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.fragment.navigation.Navigation.NavigationListener#
     * onItemSelected(com.concur.core.fragment.navigation.Navigation .NavigationItem)
     */
    @Override
    public void onItemSelected(NavigationItem item) {
        if (item instanceof HomeScreenSimpleNavigationItem) {
            HomeScreenSimpleNavigationItem homeItem = (HomeScreenSimpleNavigationItem) item;
            Handler handler = new Handler();
            handler.post(homeItem.run);
        }
        mDrawerView = (ScrollView) findViewById(R.id.left_drawer);
        mDrawerLayout.closeDrawer(mDrawerView);
    }
}

/**
 * An extension of <code>DefaultSimpleNavigation</code> with an ID.
 */
class HomeScreenSimpleNavigationItem extends DefaultSimpleNavigationItem {

    Runnable run;

    HomeScreenSimpleNavigationItem(int id, int layoutResId, int textResId, int iconResId, int iconVisibility,
        int viewVisibility,
        Runnable run) {
        super(id, layoutResId, textResId, iconResId, iconVisibility, viewVisibility);
        this.run = run;
    }

}

class HomeScreenNavigationItem extends DefaultNavigationItem {

    Runnable run;

    HomeScreenNavigationItem(int id, int layoutResId, Runnable run) {
        super(id, layoutResId);
        this.run = run;
    }
}