package com.concur.mobile.corp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.concur.breeze.R;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.ui.UIUtils;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.MessageCenter;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.activity.Tour;
import com.concur.mobile.core.activity.ViewImage;
import com.concur.mobile.core.config.RuntimeConfig;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.dialog.ReceiptChoiceDialogFragment;
import com.concur.mobile.core.dialog.ReceiptChoiceDialogFragment.ReceiptChoiceListener;
import com.concur.mobile.core.dialog.SystemUnavailableDialogFragment;
import com.concur.mobile.core.expense.activity.ExpensesAndReceipts;
import com.concur.mobile.core.expense.activity.ExpensesAndReceiptsActivity;
import com.concur.mobile.core.expense.charge.activity.QuickExpense;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.data.CountSummary;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.receiptstore.activity.ReceiptStoreFragment;
import com.concur.mobile.core.expense.receiptstore.service.ReceiptShareService;
import com.concur.mobile.core.expense.report.activity.ActiveReportsListAdapter;
import com.concur.mobile.core.expense.report.activity.ExpenseActiveReports;
import com.concur.mobile.core.expense.report.activity.ExpenseEntries;
import com.concur.mobile.core.expense.report.activity.ExpenseEntryMileage;
import com.concur.mobile.core.expense.report.activity.ExpenseReportHeader;
import com.concur.mobile.core.expense.report.approval.activity.Approval;
import com.concur.mobile.core.expense.report.data.CarConfig;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.fragment.ActiveReportsListDialogFragment;
import com.concur.mobile.core.expense.report.service.CarConfigsRequest;
import com.concur.mobile.core.expense.service.CountSummaryRequest;
import com.concur.mobile.core.fragment.navigation.DefaultNavigationItem;
import com.concur.mobile.core.fragment.navigation.DefaultSimpleNavigationItem;
import com.concur.mobile.core.fragment.navigation.DefaultTextNavigationItem;
import com.concur.mobile.core.fragment.navigation.Navigation.CustomNavigationItem;
import com.concur.mobile.core.fragment.navigation.Navigation.NavigationItem;
import com.concur.mobile.core.fragment.navigation.Navigation.NavigationListener;
import com.concur.mobile.core.fragment.navigation.Navigation.SimpleNavigationItem;
import com.concur.mobile.core.fragment.navigation.Navigation.TextNavigationItem;
import com.concur.mobile.core.request.activity.RequestListActivity;
import com.concur.mobile.core.request.util.RequestStatus;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.CorpSsoQueryReply;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.activity.BookTravelDialogFragment;
import com.concur.mobile.core.travel.activity.PriceToBeatDialogFragment;
import com.concur.mobile.core.travel.activity.SegmentList;
import com.concur.mobile.core.travel.activity.TripList;
import com.concur.mobile.core.travel.air.activity.AirSearch;
import com.concur.mobile.core.travel.car.activity.CarSearch;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.hotel.activity.HotelSearch;
import com.concur.mobile.core.travel.hotel.activity.RestHotelSearch;
import com.concur.mobile.core.travel.rail.activity.RailSearch;
import com.concur.mobile.core.travel.service.ItineraryRequest;
import com.concur.mobile.core.travel.service.ItinerarySummaryListRequest;
import com.concur.mobile.core.util.ConcurException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.RolesUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.platform.authentication.LogoutRequestTask;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.location.LastLocationTracker;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;
import com.concur.mobile.platform.ui.common.util.ImageUtil;
import com.concur.platform.ExpenseItProperties;
import com.concur.platform.PlatformProperties;

import org.apache.http.HttpStatus;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@EventTracker.EventTrackerClassName(getClassName = Flurry.SCREEN_NAME_HOME)
public class Home extends BaseActivity implements View.OnClickListener, NavigationListener, ReceiptChoiceListener {

    public static boolean forceExpirationHome;

    public AlertDialog expireDialog;

    public static final String LAUNCH_EXPENSE_LIST = "LAUNCH_EXPENSE_LIST";

    private static final String CLS_TAG = Home.class.getSimpleName();

    private final SparseArray<Dialog> dialogs = new SparseArray<Dialog>(3);

    private static final String ITINERARY_RECEIVER_KEY = "itinerary.receiver";
    private static final String TRAVEL_TEXT_URI = "http://traveltext.net/CMobile";
    private static final String ACTIVE_REPORTS_LIST_TAG = "active.reports.list.tag";

    private static final String OPEN_SOURCE_LIBRARY_CLASS = "open_source_library_class";

    protected int inProgressRef = 0;

    private String receiptCameraImageDataLocalFilePath;
    private static final int REQUEST_TAKE_PICTURE = 100;
    private static final int REQUEST_CODE_MSG_CENTER = 101;

    private static final int NAVIGATION_BOOK_TRAVEL = 0;
    private static final int NAVIGATION_RECEIPTS = 1;
    private static final int NAVIGATION_PERSONAL_CAR_MILEAGE = 2;
    private static final int NAVIGATION_CHECK_IN = 3;
    private static final int NAVIGATION_APP_HEADER = 4;
    private static final int NAVIGATION_APP_TRIP_IT = 5;
    private static final int NAVIGATION_APP_TAXI_MAGIC = 7;
    private static final int NAVIGATION_HEADER = 9;
    private static final int NAVIGATION_SETTINGS = 10;
    private static final int NAVIGATION_AD = 11;
    private static final int NAVIGATION_LEARN_MORE = 12;
    private static final int NAVIGATION_APP_EXPENSE_IT = 13;
    private static final int NAVIGATION_HOME = 14;
    private static final int NAVIGATION_SIGN_OUT = 15;
    private static final int NAVIGATION_APP_TRAVEL_TEXT = 16;
    private static final int NAVIGATION_TRAVEL_POINTS = 17;
    private static final int NAVIGATION_APP_UBER = 18;
    private static final int NAVIGATION_PROFILE = 19;
    private static final int NAVIGATION_APP_CENTER = 20;

    private static final int REQUEST_ID_LOGOUT = 1;

    /**
     * Flag to gate data requests on service availability
     */
    boolean needService = false;

    // Boolean value for flurry events
    private boolean isFromMoreMenu = false;
    protected boolean isTipsOverlayVisible = false;
    protected boolean isProfileDisable = true;

    // Time values for flurry uptime events.
    protected long startTime = 0L;
    protected long upTime = 0L;

    // Contains the action status error message returned from the server.
    private String actionStatusErrorMessage;

    // Contains the last http error message.
    private String lastHttpErrorMessage;

    // Contains a reference to an active report list adapter.
    protected ActiveReportsListAdapter activeReportListAdapter;

    // Contains the current itinerary summary list request object.
    private ItinerarySummaryListRequest itinerarySummaryListRequest;

    // Contains the receiver used to handle the results of an itinerary request.
    private ItineraryReceiver itineraryReceiver;

    // Contains the filter used to register the itinerary receiver.
    private IntentFilter itineraryFilter;

    // Contains a reference to the currently outstanding itinerary request.
    private ItineraryRequest itineraryRequest;

    // Contains the current count summary request object.
    private CountSummaryRequest countSummaryRequest;
    private CarConfigsRequest carConfigsRequest;

    protected static final String EXPIRE_LOGIN = "expire_login";
    public static ExpireLoginHandler sHandler = null;

    protected static final String REMOTE_WIPE = "remote_wipe";
    public static RemoteWipeHandler remoteWipeHandler = null;

    protected static final String RECEIPT_CHOICE_SHOWN = "receipt_choice_shown";

    protected static Home liveHome;
    protected boolean loginHasExpired = false;
    protected boolean remoteWipe = false;

    // Fields for location
    protected LocationManager locationManager;
    protected Criteria locCriteria;
    protected PendingIntent locationListenerPI;
    protected BroadcastReceiver lpDisabledReceiver;
    protected LocationListener lpBestInactiveListener;
    protected LocationListener lastKnownLocUpdateListener;

    protected MenuItem refreshActionItem;
    protected View refreshProgressView;

    private boolean showTravelAgencyBtn = true;

    // Navigation Drawer items
    protected DrawerLayout mDrawerLayout;
    protected ScrollView mDrawerView;
    protected ActionBarDrawerToggle mDrawerToggle;

    private AlertDialogFragment confirmationDialog;

    private List<NavigationItem> navItems = new ArrayList<NavigationItem>();

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
     * A custom handler to catch a message about the remote wipe.
     */
    protected static class RemoteWipeHandler extends Handler {

        RemoteWipeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (liveHome != null && !liveHome.remoteWipe) {
                switch (msg.what) {
                    case 1:
                        Log.d(Const.LOG_TAG, "RemoteWipeHandler: remote wipe, resetting");

                        liveHome.remoteWipe = true;
                        Intent home = new Intent(liveHome, Home.class);
                        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        home.putExtra(REMOTE_WIPE, true);
                        liveHome.startActivity(home);

                        break;
                }
            }
        }

    }

    /**
     * An access method to be used from another thread (presumably the ConcurServiceHandler) to indicate that the session has
     * expired and the user needs to login again. The end result will be a cleared task stack with the Login activity sitting at
     * the root. See ConcurServiceHandler.handleMessage() for the invocation.
     */
    public static void expireLogin() {

        // Clear out any pending messages in the queue.
        ((ConcurCore) ConcurCore.getContext()).getService().clearHandlerMessages();

        if (sHandler == null) {
            Log.d(Const.LOG_TAG, "creating expire handler");
            // Create a handler for the message making sure to specify the main
            // looper because
            // this should be coming in from another thread.
            sHandler = new ExpireLoginHandler(Looper.getMainLooper());
        }
        sHandler.dispatchMessage(sHandler.obtainMessage(1));
    }

    public static void expireLogin(boolean forceExpiration) {
        forceExpirationHome = forceExpiration;
    }

    /**
     * An access method to be used from another thread (presumably the ConcurServiceHandler) to indicate that the a "remote-wipe"
     * has been sent down from a login/auto-login response. The end result will be a cleared task stack with the Login activity
     * sitting at the root. See ConcurServiceHandler.handleMessage() for the invocation.
     */
    public static void remoteWipe() {

        // Clear out any pending messages in the queue.
        ((ConcurCore) ConcurCore.getContext()).getService().clearHandlerMessages();

        if (remoteWipeHandler == null) {
            Log.d(Const.LOG_TAG, "creating remote wipe handler");
            // Create a handler for the message making sure to specify the main
            // looper because this should be coming in from another thread.
            remoteWipeHandler = new RemoteWipeHandler(Looper.getMainLooper());
        }
        remoteWipeHandler.dispatchMessage(remoteWipeHandler.obtainMessage(1));
    }

    // Contains the receiver to handle responses for home screen data.
    private final BroadcastReceiver dataReceiver = new BroadcastReceiver() {

        /**
         * Receive notification that some piece of data has been retrieved.
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (Const.ACTION_SUMMARY_UPDATED.equals(action)) {
                updateExpenseSummaryInfo(intent);
            } else if (Const.ACTION_SUMMARY_TRIPS_UPDATED.equals(action)) {
                updateTripInfo(intent);
            } else if (Const.ACTION_EXPENSE_REPORT_ENTRY_FORM_UPDATED.equals(action)) {
                // Check with the app to see if a form was really retrieved...
                ConcurMobile app = (ConcurMobile) getApplication();
                if (app.getCurrentEntryDetailForm() != null) {
                    // If so, go ahead with the mileage
                    addMileageExpense(intent);
                } else {
                    // If not, alert the user and stop
                    showDialog(Const.DIALOG_EXPENSE_NO_MILEAGE_FORM);
                    try {
                        dismissDialog(Const.DIALOG_EXPENSE_ENTRY_FORM);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }
                }
            } else if (Const.ACTION_EXPENSE_CAR_CONFIGS_UPDATED.equals(action)) {
                decrInProgressRef();
                if (ViewUtil.isShowMileageExpenseOnHomeScreenEnabled(Home.this) && showPersonalCarMileage()) {
                    showMileageFooterButton();
                    showMileageDrawerButton(View.VISIBLE);
                }
            } else if (Const.ACTION_DATABASE_RESET.equals(action)) {

                // Refresh the screen
                updateExpenseSummaryUI();
                updateTripUI();

                // Go get the data
                loadData();

                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_REFRESH_DATA);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_SETTINGS, Flurry.EVENT_NAME_ACTION, params);
            }
        }
    };

    // Contains whether our data receiver has been registered.
    private boolean dataReceiverRegistered = false;

    // Contains the intent filter used to register the data receiver.
    private final IntentFilter dataReceiverFilter = new IntentFilter();

    // An AsyncTask to manipulate the travel image however we want
    AsyncTask<Void, Void, Integer> imageManipTask;

    private String receiptImageDataLocalFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Track this activity in a static for use by the expired login handler.
        // This is nulled in onDestroy to hopefully prevent a leak.
        liveHome = this;

        setContentView(R.layout.home);

        if (forceExpirationHome) {
            //forceExpirationHome = false;
            cancelAllDataRequests();
            clearSessionData();
            showExpiredDialog();

        } else {

            // Initialize the Navigation DrawerLayout
            mDrawerLayout = (DrawerLayout) findViewById(R.id.home_frame);

            // Tweak the action bar
            final ActionBar actionBar = getSupportActionBar();

            actionBar.setDisplayOptions(
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO,
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO
                            | ActionBar.DISPLAY_SHOW_TITLE);

            actionBar.setLogo(R.drawable.concur_logo);

            // The button that toggles the drawer between open and close
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.concur_logo,
                    R.string.empty_string, R.string.empty_string);

            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            // If this is a test drive user, show them the tips overlay and skip the
            // usual home tips overlay
            // Note: Do NOT Prompt for notifications for Test Drive users.
            if (RolesUtil.isTestDriveUser() && Preferences.shouldShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_HOME)) {
                showTestDriveTips();
            } else {
                // Try to prompt for notifications just to be sure
                if (!RolesUtil.isTestDriveUser()) {
                    promptForNotifications();
                }
            }

            // If it's the first time running, and it's not a test drive user, show
            // tour. Set not first time running either way.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (Preferences.isFirstTimeRunning(prefs)) {
                if (!RolesUtil.isTestDriveUser() && (RolesUtil.isExpenser(Home.this) || RolesUtil
                        .isTraveler(Home.this))) {
                    showTour();
                } else {
                    Preferences.setNotFirstTimeRunning(prefs);
                }
            }

            // If the user should see the minSdkUpgradeMessage, show it.
            if ((android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) && !Preferences
                    .hasShownMinSDKIncreaseMessage()) {
                showMinSDKIncreaseMessage();
            }

            // Initialize the home screen navigation menu.
            initNavigationMenu();

            Intent launchIntent = getIntent();

            if (launchIntent.hasExtra(REMOTE_WIPE) || (savedInstanceState != null && savedInstanceState
                    .containsKey(REMOTE_WIPE))) {

                clearSessionData();
                showRemoteWipeDialog();

            } else if (launchIntent.hasExtra(EXPIRE_LOGIN) || (savedInstanceState != null && savedInstanceState
                    .containsKey(EXPIRE_LOGIN))) {

                clearSessionData();
                showExpiredDialog();
            }

            // MOB-17239
            // If the current device date is greater than the Test Drive account
            // expiration date, log them out.
            // Note that this is temporary pending a more permanent design from
            // UX/PM. At that point this will need to be removed.
            if (Preferences.isTestDriveAccountExpired()) {
                showTestDriveAccountExpiredDialog();
            }

            if (savedInstanceState != null && savedInstanceState.containsKey(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH)) {
                receiptCameraImageDataLocalFilePath = savedInstanceState.getString(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH);
            }
            if (savedInstanceState != null) {
                upTime = savedInstanceState.getLong(Const.ACTIVITY_STATE_UPTIME, 0L);
            }

        hideFooter();
        hideBookFooterButton();
        hideMileageFooterButton();
        // OCR: Disable backdoor Easter egg.
        if (!Preferences.isExpenseItUser()/* || !Preferences.shouldUseNewOcrFeatures() */) {
            hideQuickExpenseFooterButton();
        }
        hideReceiptFooterButton();
        hideTravelRequestRow();

            // Hide the travel UI elements, if need be.
            boolean isTraveler = RolesUtil.isTraveler(Home.this);
            boolean isItinViewer = RolesUtil.isItinViewer(Home.this);

            // ItinViewer is the new role indicating a non-cliqbook (TMC) company
            // Due to server issues, both flags may be set. If so, make TU trump IV.
            if (isTraveler) {
                isItinViewer = false;
            }

            boolean isTravelOnly =
                    isTraveler && !(RolesUtil.isExpenser(Home.this) || RolesUtil.isExpenseApprover(Home.this)
                            || RolesUtil.isTRApprover(Home.this) || RolesUtil.isInvoiceApprover(Home.this) || RolesUtil
                            .isInvoiceUser(Home.this) || RolesUtil.isTravelApprover(Home.this));

            if (isItinViewer) {
                hideBookingUI();
            } else if (!isTraveler) {
                hideTravelUI();
            } else if (isTravelOnly) {
                hideExpenseUI();

                // If travel booking is disabled, then don't show the booking UI.
                if (Preferences.shouldAllowTravelBooking() && RolesUtil.isTraveler(Home.this)) {
                    showBookingRows();
                } else {
                    hideBookingUI();
                }
            } else if (Preferences.shouldAllowTravelBooking() && RolesUtil.isTraveler(Home.this)) {
                showBookFooterButton();
            }

            if (isItinViewer || isTraveler) {
                // Travel UI initialization.

                // Show no trips
                TextView view = getRowSubheader(R.id.homeRowTravel);
                view.setText(R.string.home_row_travel_subheader_negative);

            }

            if (ViewUtil.isTravelRequestUser(this)) {
                showTravelRequestRow();
            }

            // Hide the expense section, if need be.
            if (RolesUtil.isExpenser(Home.this)) {
                // OCR: Disable backdoor Easter egg.
                if (!Preferences.isExpenseItUser()) {
                    showQuickExpenseFooterButton();
                    showReceiptFooterButton();
                } else {
                    /* MOB-24972 - Disable to show QE at bottom of screen again.
                    hideQuickExpenseFooterButton();
                    */
                    hideReceiptFooterButton();
                    showExpenseItFooterButton();
                }
                if (ViewUtil.isShowMileageExpenseOnHomeScreenEnabled(Home.this) && showPersonalCarMileage()) {
                    showMileageFooterButton();
                    showMileageDrawerButton(View.VISIBLE);
                }
            } else {
                // Hide the expense section.
                hideExpenseUI();
            }

            // Hide the approval section, if need be.
            if (!RolesUtil.isExpenseApprover(Home.this) && !RolesUtil.isTRApprover(Home.this) && !RolesUtil
                    .isInvoiceApprover(Home.this) && !RolesUtil.isInvoiceUser(Home.this) && !RolesUtil
                    .isTravelApprover(Home.this)) {
                hideApproverUI();
            }

            // If access to report is now allowed, then hide the section.
            if (!Preferences.shouldAllowReports()) {
                // Hide the reports UI elements.
                hideReportsUI();
            }

            // Hide the travel request approver elements.
            if (!RolesUtil.isTRApprover(Home.this)) {
                // TODO9
                // Hide Travel Request Approver.
                // setViewGone(R.id.homeTravelRequestApprovals);
                // Hide the travel header.
                // setViewGone(R.id.homeSectionTravelRequestHeader);
            } else {
                // If the user is a TR Approver and also has the Travel
                // role, then hide the "Travel" header text.
                if (isTraveler) {
                    // TODO9
                    // setViewGone(R.id.homeSectionTravelRequestHeader);
                }
            }

            // Hide the whole Invoice section
            // if neither approver or submitter is enabled.
            if (!RolesUtil.isInvoiceApprover(Home.this) && !RolesUtil.isInvoiceUser(Home.this)) {
                hideInvoiceUI();
            }

            // check to show trip list for a open booking user
            boolean showTrips = RolesUtil.showTripsForOpenBookingUser(Home.this);
            if (showTrips) {
                showTripsUI();
                // Show no trips
                getRowSubheader(R.id.homeRowTravel).setText(R.string.home_row_travel_open_booking_subheader_negative);
                if (isItinViewer || isTraveler) {
                    showTravelAgencyBtn = true;
                } else {
                    showTravelAgencyBtn = false;
                }
            }

            // Add expense-user broadcast receivers.
            if (RolesUtil.isExpenser(Home.this) || RolesUtil.isExpenseApprover(Home.this) || RolesUtil
                    .isInvoiceApprover(Home.this) || RolesUtil.isInvoiceUser(Home.this) || RolesUtil
                    .isTRApprover(Home.this)) {
                // Add expense related broadcast receivers.
                dataReceiverFilter.addAction(Const.ACTION_SUMMARY_UPDATED);

                // We will receive this when getting the mileage form.
                dataReceiverFilter.addAction(Const.ACTION_EXPENSE_REPORT_ENTRY_FORM_UPDATED);

                // We will receive this when getting the carconfig.
                dataReceiverFilter.addAction(Const.ACTION_EXPENSE_CAR_CONFIGS_UPDATED);

                // We will receive this once a database reset has occurred.
                dataReceiverFilter.addAction(Const.ACTION_DATABASE_RESET);
            }

            // Add traveler broadcast receiver.
            if (isTraveler || isItinViewer || showTrips) {
                dataReceiverFilter.addAction(Const.ACTION_SUMMARY_TRIPS_UPDATED);
            }

            // Register the receiver.
            registerReceiver(dataReceiver, dataReceiverFilter);
            dataReceiverRegistered = true;

            // Try to get initial location
            requestLastKnownLocation();

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

                // Start the Receipt Share service. This service will only run for a
                // few minutes at start-up
                // if there are no pending receipts to be uploaded to the Receipt
                // Store.
                Intent serviceIntent = new Intent(this, ReceiptShareService.class);
                startService(serviceIntent);
            }

            // In some locales, having 4 buttons causes long text to wrap in footer
            // buttons.
            if (numberOfVisibleFooterButtons() >= 4) {
                setSmallFooterButtonText();
            }

            hideBadges();

            boolean launch = getIntent().getBooleanExtra(LAUNCH_EXPENSE_LIST, false);
            if (launch) {
                Intent intent;
                if (Preferences.shouldUseNewOcrFeatures()) {
                    intent = new Intent(this, ExpensesAndReceiptsActivity.class);
                } else {
                    intent = new Intent(this, ExpensesAndReceipts.class);
                }
                intent.putExtra("FORCE_REFRESH", true);
                startActivity(intent);
            }
        }

    }

    private void clearSessionData() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // We're here because the session has expired or is about to and we
        // can't get a new one automagically. Bail.
        // Immediately clear the session.
        // Clear out relative platform properties values.
        // ConfigUtil.removeLoginInfo(this);
        PlatformProperties.setAccessToken(null);
        PlatformProperties.setSessionId(null);

        // Also, clear any A/B Test information.
        Preferences.clearABTestInfo(prefs);
        // NOTE: Still need to call old Preference.clearSession() because it
        // removes
        // some of the expiration flags used at Startup.java.
        Preferences.clearSession(prefs);

    }

    private void showRemoteWipeDialog() {
        // Notify the user about the "Remote Wipe".
        AlertDialogFragment adf = DialogFragmentFactory
                .getAlertOkayInstance(R.string.account_locked, R.string.account_locked_message);
        adf.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }

            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                launchLoginScreen();
                dialog.dismiss();
                finish();
            }
        });
        adf.setCancelable(false);
        adf.show(Home.this.getSupportFragmentManager(), "tag.login.remote.wipe.dialog");
    }

    private void showExpiredDialog() {

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage(R.string.login_expired);
        b.setCancelable(false);
        b.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                launchLoginScreen();
                dialog.dismiss();
                finish();
            }
        });

        if (expireDialog != null && expireDialog.isShowing()) {
            // do nothing
        } else {
            expireDialog = b.create();
            expireDialog.show();
        }
    }

    private void launchLoginScreen() {
        // In this case, the end-user didn't explicitly log out,
        // i.e., their session
        // expired on them. In that case, for company SSO, we'll
        // take them to company SSO
        // page if they have one. If not, then go to the
        // PIN-based
        // login screen.
        ConcurMobile concurMobile = (ConcurMobile) getApplication();
        CorpSsoQueryReply ssoReply = concurMobile.getCorpSsoQueryReply();
        SessionInfo sessionInfo = ConfigUtil.getSessionInfo(concurMobile.getApplicationContext());
        Intent login = null;
        if (((ssoReply != null && ssoReply.ssoEnabled && ssoReply.ssoUrl != null)) || (sessionInfo != null
                && (!TextUtils.isEmpty(sessionInfo.getSSOUrl())))) {
            // Company sign-on.
            login = new Intent(Home.this, EmailLookupActivity.class);
            login.putExtra(EmailLookupActivity.EXTRA_ADVANCE_TO_COMPANY_SIGN_ON, true);
        } else {
            // PIN-based login.
            login = new Intent(Home.this, EmailLookupActivity.class);
            login.putExtra(com.concur.mobile.platform.ui.common.util.Const.EXTRA_LOGOUT, true);
        }
        startActivity(login);
    }

    /**
     * If the user is a test drive user and their account is expired, show an AlertDialog with our business number attached to it,
     * and upon clicking "close" on the dialog, log the user out and send them to the login screen.
     */
    private void showTestDriveAccountExpiredDialog() {

        // Note that Linkify, for whatever reason, will not read special
        // characters (IE '&#8211;', the '-' symbol) so the phone
        // number had to be hard written here and not in strings.xml. This is
        // the only place we use it anyways.
        final SpannableString dialogBodyText = new SpannableString(
                getString(R.string.test_drive_expiration_message) + Const.TEST_DRIVE_CONTACT_CONCUR_NUMBER);

        final AlertDialog expirationAlertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.test_drive_expiration_title).setCancelable(false).setMessage(dialogBodyText)
                .setPositiveButton(R.string.test_drive_expiration_close, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        // Clear the session upon clicking because the
                        // login expiration checks get called in
                        // onCreate() and
                        // onResume() so if we clear the session outside
                        // of the dialog, the onResume() call will crash
                        // us.
                        // ConfigUtil.removeLoginInfo(Home.this);
                        // NOTE: Still need to call old
                        // Preference.clearSession() because it removes
                        // some of the expiration flags used at
                        // Startup.java.
                        Preferences.clearSession(PreferenceManager.getDefaultSharedPreferences(Home.this));

                        PlatformProperties.setAccessToken(null);
                        PlatformProperties.setSessionId(null);

                        Intent login = new Intent(Home.this, EmailLookupActivity.class);
                        login.putExtra(com.concur.mobile.platform.ui.common.util.Const.EXTRA_LOGOUT, true);

                        startActivity(login);
                        dialog.dismiss();
                        finish();
                    }
                }).show();

        Linkify.addLinks(dialogBodyText, Linkify.PHONE_NUMBERS);

        // Grab the AlertDialog message as a TextView to actually set the
        // Linkify link.
        TextView bodyTextView = ((TextView) expirationAlertDialog.findViewById(android.R.id.message));
        bodyTextView.setAutoLinkMask(RESULT_OK);
        bodyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        bodyTextView.setGravity(Gravity.CENTER);

    }

    /**
     * The first time a non Test Drive user logs in, they'll see a tour screen where they can fling to the next image. These
     * images are selected dynamically depending on user type (Travel, Expense, Both, or Neither).
     */
    protected void showTour() {

        /**
         * A child of SimpleOnGestureListener that overrides fling to allow the user to flip through tour screens in the
         * ViewFlipper and shows animation when they do so.
         */
        class HomeTourGestureDetector extends SimpleOnGestureListener {

            private static final int SWIPE_MIN_DISTANCE = 120;
            private static final int SWIPE_MAX_OFF_PATH = 250;
            private static final int SWIPE_THRESHOLD_VELOCITY = 200;

            ViewFlipper flipper;
            ArrayList<View> homeTourDotsList;
            String[] homeTourTitleStrings;
            String[] homeTourMessageStrings;
            TextView homeTourTextTitle;
            TextView homeTourTextMessage;

            public HomeTourGestureDetector(ViewFlipper flipper, ArrayList<View> homeTourDotsList) {
                this.flipper = flipper;
                this.homeTourDotsList = homeTourDotsList;

                Resources res = getResources();
                homeTourTitleStrings = new String[]{res.getString(R.string.home_tour_expense1_title),
                        res.getString(R.string.home_tour_expense2_title),
                        res.getString(R.string.home_tour_travel1_title)};

                homeTourMessageStrings = new String[]{res.getString(R.string.home_tour_expense1_message),
                        res.getString(R.string.home_tour_expense2_message),
                        res.getString(R.string.home_tour_travel1_message)};

                homeTourTextTitle = (TextView) findViewById(R.id.home_tour_text_title);
                homeTourTextMessage = (TextView) findViewById(R.id.home_tour_text_message);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                        return false;
                    // Swipe right to left (next)
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        flipper.setInAnimation(Home.this, R.anim.slide_in_right_fast);
                        flipper.setOutAnimation(Home.this, R.anim.slide_out_left_fast);

                        homeTourDotsList.get(flipper.getDisplayedChild())
                                .setBackgroundResource(R.drawable.home_tour_white_dot);

                        flipper.showNext();

                        int currentViewIndex = flipper.getDisplayedChild();

                        homeTourDotsList.get(currentViewIndex).setBackgroundResource(R.drawable.home_tour_blue_dot);
                        homeTourTextTitle.setText(homeTourTitleStrings[currentViewIndex]);
                        homeTourTextMessage.setText(homeTourMessageStrings[currentViewIndex]);

                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                            && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        flipper.setInAnimation(Home.this, R.anim.slide_in_left_fast);
                        flipper.setOutAnimation(Home.this, R.anim.slide_out_right_fast);

                        homeTourDotsList.get(flipper.getDisplayedChild())
                                .setBackgroundResource(R.drawable.home_tour_white_dot);

                        flipper.showPrevious();

                        int currentViewIndex = flipper.getDisplayedChild();

                        homeTourDotsList.get(currentViewIndex).setBackgroundResource(R.drawable.home_tour_blue_dot);
                        homeTourTextTitle.setText(homeTourTitleStrings[currentViewIndex]);
                        homeTourTextMessage.setText(homeTourMessageStrings[currentViewIndex]);
                    }
                } catch (Exception e) {
                    // no-op
                }
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        }

        View.OnClickListener dismissListener = new OnClickListener() {

            public void onClick(View v) {
                Preferences.setNotFirstTimeRunning(PreferenceManager.getDefaultSharedPreferences(Home.this));
            }
        };

        View homeTourView = UIUtils
                .setupOverlay((ViewGroup) getWindow().getDecorView(), R.layout.home_tour, dismissListener,
                        R.id.home_tour_icon_cancel, this, R.anim.fade_out, 500L);

        ViewFlipper homeTourFlipper = (ViewFlipper) findViewById(R.id.home_tour_view_flipper);
        View homeTourDots = homeTourView.findViewById(R.id.home_tour_dots);

        // This is the bulk of the logic to determine what to show and how to
        // show it based on user
        if (RolesUtil.isExpenser(Home.this)) {
            // If Expenser, we will have multiple tour images, so we need the
            // dots list.
            ArrayList<View> homeTourDotsList = new ArrayList<View>();
            homeTourDotsList.add(homeTourDots.findViewById(R.id.homeTourDot1));
            homeTourDotsList.add(homeTourDots.findViewById(R.id.homeTourDot2));

            if (RolesUtil.isTraveler(Home.this)) {
                // Is Travel and Expense
                homeTourDotsList.add(homeTourDots.findViewById(R.id.homeTourDot3));

            } else {
                // Is Expense only

                // If we're not not a traveler, remove traveler image and dot
                // for it.
                homeTourDots.findViewById(R.id.homeTourDot3).setVisibility(View.GONE);
                homeTourFlipper.removeView(homeTourFlipper.findViewById(R.id.homeTourTravel1));

            }

            // Note: Only make this for expense users because Travel Only has
            // only one view.
            final GestureDetector gestureDetector = new GestureDetector(this,
                    new HomeTourGestureDetector(homeTourFlipper, homeTourDotsList));

            homeTourFlipper.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        } else {
            // Is travel only
            // Since travel only has one screen, remove all the dots.
            homeTourDots.setVisibility(View.GONE);

            // Set text to travel text
            Resources res = getResources();
            TextView homeTourTextTitle = (TextView) findViewById(R.id.home_tour_text_title);
            homeTourTextTitle.setText(res.getString(R.string.home_tour_travel1_title));
            TextView homeTourTextMessage = (TextView) findViewById(R.id.home_tour_text_message);
            homeTourTextMessage.setText(res.getString(R.string.home_tour_travel1_message));

            // Remove all except for the travel tour image
            homeTourFlipper.removeView(findViewById(R.id.homeTourExpense1));
            homeTourFlipper.removeView(findViewById(R.id.homeTourExpense2));

        }

    }

    /**
     * If this user is a test drive user, this will get called the first time the user loads the home screen to show the test
     * drive tips overlay
     */
    protected void showTestDriveTips() {
        // OnClickListener whose onClick() is called from setupOverlay()
        OnClickListener dismissListener = new OnClickListener() {

            public void onClick(View v) {
                Preferences.setShouldNotShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_HOME);
                isTipsOverlayVisible = false;

                // Analytics stuff.
                Map<String, String> flurryParams = new HashMap<String, String>();
                upTime = ((System.nanoTime() - startTime) / 1000000000L) + upTime; // Convert nanoseconds to seconds.
                flurryParams.put(Flurry.PARAM_NAME_SECONDS_ON_OVERLAY, Flurry.formatDurationEventParam(upTime));
                EventTracker.INSTANCE.track(Flurry.CATEGORY_OVERLAYS, "Home Screen", flurryParams);
            }
        };

        UIUtils.setupOverlay((ViewGroup) getWindow().getDecorView(), R.layout.td_overlay_home, dismissListener,
                R.id.td_icon_cancel_button, this, R.anim.fade_out, 300L);

        isTipsOverlayVisible = true;
    }

    protected void hideTipsExpenseUI() {
        // Turn off receipt capture.
        setViewInvisible(R.id.tip_icon_camera);
        setViewInvisible(R.id.tip_camera_arrowdown);
        setViewInvisible(R.id.tip_camera_text);
        // Turn off quick expense.
        setViewInvisible(R.id.tip_icon_quickexpense);
        setViewInvisible(R.id.tip_quick_expense_arrowdown);
        setViewInvisible(R.id.tip_quick_text);
    }

    protected void hideTipsTravelUI() {
        // Turn off book trip
        setViewInvisible(R.id.tip_icon_trip);
        setViewInvisible(R.id.tip_trip_arrowdown);
        setViewInvisible(R.id.tip_trip_text);
    }

    @Override
    protected void onStart() {
        super.onStart();

        startLocationUpdates();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.corp.activity.BaseActivity#onResume()
     */
    @Override
    protected void onResume() {

        super.onResume();

        if (forceExpirationHome) {
            forceExpirationHome = false;
            cancelAllDataRequests();
            clearSessionData();
            showExpiredDialog();
        } else {

            String sessionId = PlatformProperties.getSessionId();
            if (sessionId == null || sessionId.length() == 0) {

                if (getIntent().hasExtra(REMOTE_WIPE)) {
                    showRemoteWipeDialog();
                } else if (isServiceAvailable()) {
                /*
                 * MOB-15538 requires this check. In this case application has been forced stopped or killed by system. In that
                 * case when application recreated or restarted this activity also get created simultaneously. It is quite
                 * possible you don't have session id as db creation has delayed. please see newly created
                 * updateDataBasedOnServiceAvail in onResume and onServiceAvailable.
                 */
                    showExpiredDialog();
                } else {
                    needService = true;
                }
            } else {
                // Restore any receivers.
                restoreReceivers();

                // Re-register the data receiver, if need be.
                if (!dataReceiverRegistered) {
                    registerReceiver(dataReceiver, dataReceiverFilter);
                    dataReceiverRegistered = true;
                }

                // Go get the data
                if (isServiceAvailable()) {
                    updateDataBasedOnServiceAvail();
                } else {
                    needService = true;
                }
                updateOfflineQueueBar();

            }

            showHideHomeImage();

            // If we're showing the ActiveReportsListDialogFragment, we want to
            // update its click and cancel listener because
            // the fragment does not retain instance state.
            Fragment reportListDialogFrag = getSupportFragmentManager().findFragmentByTag(ACTIVE_REPORTS_LIST_TAG);
            if (reportListDialogFrag instanceof ActiveReportsListDialogFragment) {
                ActiveReportsListDialogFragment dlgFrag = (ActiveReportsListDialogFragment) reportListDialogFrag;

                dlgFrag.setClickListener(new SelectReportDialogClickListener());
                dlgFrag.setCancelListener(new DialogCancelListener());
            } else {
                Log.w(CLS_TAG, "Failed to find fragment for ACTIVE_REPORTS_LIST_TAG");
            }

            if (isTipsOverlayVisible) {
                startTime = System.nanoTime();
            }
        }
    }

    /**
     * if your application targets API level 12 or lower, then your activity always handles this configuration change via this
     * method. API level 12 or lower doesnt restart the app if in AndroidManifest we mentioned android:confiChange="orientation".
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        showHideHomeImage();
    }

    /**
     * Show or Hide Home Image based on orientation change.
     */
    private void showHideHomeImage() {
        /*
         * MOB-16309 : Talked to Loc about this JIRA and we have decided to remove image if the phone is in landscape mode. In
         * future it may change.
         */
        // Setup the cityscape image switcher and put the placeholder image in
        // place
        ImageView cityscape = (ImageView) findViewById(R.id.travelCityscape);
        int orientation = getResources().getConfiguration().orientation;
        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                cityscape.setVisibility(View.VISIBLE);
                cityscape.setImageResource(
                        getResources().getIdentifier(Preferences.getCurrentCityscape(), "drawable", this.getPackageName()));
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                cityscape.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Update your data after you have concur service available.
     */
    private void updateDataBasedOnServiceAvail() {
        if (!orientationChange) {
            loadData();
        } else {
            // Clear the orientation change flag.
            orientationChange = false;
            // Immediately update the summary information.
            updateExpenseSummaryUI();
            // Immediately update the trip information.
            updateTripUI();
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
        // want to spin useless requests if we already have our data.
        if (needService) {
            /*
             * MOB-15538 : we required restoreReceivers() and updateOfflineQueueBar() as we are calling from onResume
             */
            // Restore any receivers.
            restoreReceivers();

            // Re-register the data receiver, if need be.
            if (!dataReceiverRegistered) {
                registerReceiver(dataReceiver, dataReceiverFilter);
                dataReceiverRegistered = true;
            }
            updateDataBasedOnServiceAvail();
            updateOfflineQueueBar();
            needService = false;
        }
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
     * @see com.concur.mobile.corp.activity.BaseActivity#onPause()
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

        // Turn off the image manipulator
        // imageManipTask.cancel(true);
        imageManipTask = null;

        if (isTipsOverlayVisible) {
            // Save the time the user spent on this screen, but
            // perhaps put the app in the background.
            upTime += (System.nanoTime() - startTime) / 1000000000L; // Convert
            // to
            // seconds.
        }

        if (confirmationDialog != null) {
            confirmationDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopLocationUpdates();
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

        if (remoteWipe) {
            outState.putBoolean(REMOTE_WIPE, true);
        }

        if (receiptCameraImageDataLocalFilePath != null) {
            outState.putString(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH, receiptCameraImageDataLocalFilePath);
        }

        if (isTipsOverlayVisible) {
            // Save the uptime so we know how long the user has been on this
            // screen,
            // even if it has been destroyed.
            outState.putLong(Const.ACTIVITY_STATE_UPTIME, upTime);
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
        showHideOptionMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!ConcurCore.isConnected()) {
            menu.findItem(R.id.menuReset).setVisible(false);
            menu.findItem(R.id.menuRefresh).setVisible(false);
        } else {
            menu.findItem(R.id.menuReset).setVisible(true);
            menu.findItem(R.id.menuRefresh).setVisible(true);
        }
        showHideOptionMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * refresh option menu using preferences.
     *
     * @param menu
     */
    private void showHideOptionMenu(Menu menu) {
        boolean showNotificationbadge = Preferences.shouldShowNotificationBadge();
        MenuItem msgcntrMenuItem = menu.findItem(R.id.menuMessageCenter);
        if (!showNotificationbadge) {
            msgcntrMenuItem.setIcon(R.drawable.icon_messagecenter);
        } else {
            msgcntrMenuItem.setIcon(R.drawable.message_center_with_badge);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent i;

        switch (item.getItemId()) {
            case android.R.id.home: {
                // If navigation button is pressed, slide nav drawer open
                mDrawerToggle.onOptionsItemSelected(item);
            }
            case R.id.menuRefresh:
                loadData();
                break;
            case R.id.menuMessageCenter: {
                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_MESSAGE_CENTER);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);

                Intent intent = new Intent(Home.this, MessageCenter.class);
                startActivityForResult(intent, REQUEST_CODE_MSG_CENTER);
                break;
            }
            case R.id.menuSettings: {
                i = new Intent(this, Preferences.class);
                i.putExtra(OPEN_SOURCE_LIBRARY_CLASS, OpenSourceLicenseInfo.class);
                startActivity(i);
                break;
            }
            case R.id.menuReset: {
                if (ConcurCore.isConnected()) {
                    ConcurMobile app = (ConcurMobile) getApplication();
                    // Only perform a clear if there are no outstanding requests
                    // for data.
                    if (inProgressRef == 0) {
                        // Clear everything
                        app.clearLocalData();
                        ((ConcurCore) getApplication()).getUserConfig();
                    }
                }
                break;
            }
            case R.id.menuLogout: {
                logout();

                break;
            }
            // case R.id.menuViewLog: {
            // i = new Intent(this, LogView.class);
            // startActivity(i);
            // break;
            // }
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {

        if (ConcurMobile.isConnected()) {

            // When logging out, we clear the Session Id, but if we're updating
            // home (IE clearing offline data, refreshing, etc.)
            // then other Messages will still be in the queue and will try to
            // verify the session we just killed. If we're logging
            // out, we want to clear that message queue.
            getConcurService().clearHandlerMessages();

            // Connected, so send a logout request.
            // Note: No need to make a custom Receiver and Listener when this
            // Request Task
            // completes since we always punch out to the Login screen
            // regardless.
            LogoutRequestTask logoutRequestTask = new LogoutRequestTask(getApplicationContext(), REQUEST_ID_LOGOUT,
                    new BaseAsyncResultReceiver(new Handler()));
            logoutRequestTask.execute();

        }

        // Note 2: Clearing out old session and access token in the event
        // something happens
        // in LogoutRequestTask and it doesn't clear out the cache.
        ConfigUtil.removeLoginInfo(this);

        // Clear out relative platform properties values.
        PlatformProperties.setAccessToken(null);
        PlatformProperties.setSessionId(null);

        // Clear any A/B Test information.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Preferences.clearABTestInfo(prefs);
        // NOTE: Still need to call old Preference.clearSession() because it
        // removes
        // some of the expiration flags used at Startup.java.
        Preferences.clearSession(prefs);

        //Clear ExpenseIt Login Info
        ExpenseItProperties.setAccessToken(null);
        Preferences.setUserLoggedOnToExpenseIt(false);

        // Update the config content provider.
        ConfigUtil.removeExpenseItLoginInfo(this);

        // Go back to the EmailLookup screen.
        Intent i = new Intent(this, EmailLookupActivity.class);
        i.putExtra(com.concur.mobile.platform.ui.common.util.Const.EXTRA_LOGOUT, true);
        ConcurCore ConcurCore = (ConcurCore) getApplication();
        ConcurService ConcurService = ConcurCore.getService();
        CorpSsoQueryReply ssoQueryReply = ConcurService.getCorpSsoQueryReply();
        if (ssoQueryReply != null && ssoQueryReply.ssoEnabled && ssoQueryReply.ssoUrl != null) {
            i.putExtra(EmailLookupActivity.EXTRA_ADVANCE_TO_COMPANY_SIGN_ON, true);

            // MOB-18839 Clear cookies so SSO user isn't auto-logged back in
            // when
            // redirected to the Company Sign On screen.
            // NOTE: Commenting out the runnable thread added by fix for
            // MOB-18541
            // because this caused a syncing issue. The cookies might not be
            // cleared
            // by the time the Company Sign On Screen launches. This will cause
            // within
            // the screen to auto-login the user since the session isn't
            // cleared.

            // MOB-18541 Clear SSO web cookies. The new Runnable in the new
            // Thread is necessary because the method call sleeps the
            // thread for 1000ms, and if that isn't done in a new Runnable, it
            // sleeps the main thread so we can't finish().
            // new Thread(new Runnable() {
            //
            // public void run() {
            ViewUtil.clearWebViewCookies(Home.this);
            // }
            // }).start();

        }
        startActivity(i);

        // Flurry Notification
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_LOGOUT);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_SETTINGS, Flurry.EVENT_NAME_ACTION, params);

        finish();

    }

    protected void handleBookingClick(int id) {
        Intent i;

        switch (id) {
            case R.id.menuHomeBookAir:
            case R.id.homeRowBookFlight:
                // Check whether user has permission to book air via mobile.
                if (ViewUtil.isAirUser(this)) {
                    // Check for a complete travel profile.
                    if (ViewUtil.isTravelProfileComplete(this) || ViewUtil.isTravelProfileCompleteMissingTSA(this)) {
                        i = new Intent(this, AirSearch.class);
                        if (isFromMoreMenu) {
                            i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                            isFromMoreMenu = false;
                        } else {
                            i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
                        }
                        ConcurMobile.userClickTime = System.currentTimeMillis();
                        startActivity(i);
                    } else {
                        showDialog(Const.DIALOG_TRAVEL_PROFILE_INCOMPLETE);
                    }
                } else {
                    showDialog(Const.DIALOG_TRAVEL_NO_AIR_PERMISSION);
                }
                break;
            case R.id.menuHomeBookCar:
            case R.id.homeRowBookCar:
                i = new Intent(this, CarSearch.class);
                if (isFromMoreMenu) {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                    isFromMoreMenu = false;
                } else {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
                }
                ConcurMobile.userClickTime = System.currentTimeMillis();
                startActivity(i);
                break;
            case R.id.menuHomeBookHotel:
            case R.id.homeRowBookHotel:
                if (Preferences.shouldShowHotelJarvisUI()) {
                    i = new Intent(this, RestHotelSearch.class);
                } else {
                    i = new Intent(this, HotelSearch.class);
                }
                if (isFromMoreMenu) {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                    isFromMoreMenu = false;
                } else {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
                }
                ConcurMobile.userClickTime = System.currentTimeMillis();
                startActivityForResult(i, Const.REQUEST_CODE_BOOK_HOTEL);
                break;
            case R.id.menuHomeBookRail:
            case R.id.homeRowBookTrain:
                i = new Intent(this, RailSearch.class);
                if (isFromMoreMenu) {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                    isFromMoreMenu = false;
                } else {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
                }
                ConcurMobile.userClickTime = System.currentTimeMillis();
                startActivity(i);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        switch (v.getId()) {
            case R.id.homeBook:

                BookTravelDialogFragment dialogFragment = new BookTravelDialogFragment();

                // Add arguments
                Bundle args = new Bundle();
                if (isFromMoreMenu) {
                    args.putBoolean(BookTravelDialogFragment.IS_FROM_MORE_MENU_ARG, isFromMoreMenu);
                    isFromMoreMenu = false;
                }

                dialogFragment.setArguments(args);
                (dialogFragment).show(getSupportFragmentManager(), null);

                break;
        }
    }

    public void onClick(View v) {

        Intent i = null;
        Integer requestCode = null;

        final int id = v.getId();
        switch (id) {
            // TODO - Remove refresh and MC from here
            // case R.id.homeRefresh:
            // loadData();
            // break;
            // case R.id.homeMsgCntr: {
            // // Flurry Notification
            // Map<String, String> params = new HashMap<String, String>();
            // params.put(Flurry.PARAM_NAME_ACTION,
            // Flurry.PARAM_VALUE_MESSAGE_CENTER);
            // EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME,
            // Flurry.EVENT_NAME_ACTION, params);
            //
            // Intent intent = new Intent(Home.this, MessageCenter.class);
            // startActivity(intent);
            // break;
            // }
            case R.id.homeBook:
                // MOB-11313
                if (!ConcurCore.isConnected()) {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                } else {
                    registerForContextMenu(v);
                    openContextMenu(v);

                    // Flurry Notification
                /*
                 * Map<String, String> params = new HashMap<String, String>(); params.put(Flurry.PARAM_NAME_ACTION,
                 * Flurry.PARAM_VALUE_BOOK_TRAVEL); EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION,
                 * params);
                 */

                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_BOOK_TRAVEL);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME_MORE, Flurry.EVENT_NAME_ACTION, params);
                }
                break;
            case R.id.homeRowTravel: {
                cancelAllDataRequests();
                i = new Intent(this, TripList.class);

                // do not show Travel Agency button for expense only && open booking
                i.putExtra(Const.EXTRA_SHOW_TRAVEL_AGENCY_BUTTON, showTravelAgencyBtn);

                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_VIEW_TRIPS);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);

                break;
            }
            case R.id.homeRowApprovals: {
                cancelAllDataRequests();
                i = new Intent(this, Approval.class);
                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_VIEW_APPROVALS);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
                break;
            }
            case R.id.homeRowExpenses: {
                cancelAllDataRequests();
                // i = new Intent(this, AllExpense.class);
                if (Preferences.shouldUseNewOcrFeatures()) {
                    i = new Intent(this, ExpensesAndReceiptsActivity.class);
                } else {
                    i = new Intent(this, ExpensesAndReceipts.class);
                }
                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_VIEW_EXPENSE_LIST);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
                break;
            }

            case R.id.homeQuickExpense:
                // Verify that we have expense types
                ConcurCore ConcurCore = (ConcurCore) getApplication();
                IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
                ArrayList<ExpenseType> expenseTypes = expEntCache.getExpenseTypes();
                if (expenseTypes != null) {
                    i = new Intent(Home.this, QuickExpense.class);
                    // Set the type of expense entry we are creating, defaults to
                    // cash.
                    i.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, Expense.ExpenseEntryType.CASH.name());
                    i.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_HOME);
                    i.putExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_ACTION, Const.CREATE_MOBILE_ENTRY);
                    requestCode = Const.CREATE_MOBILE_ENTRY;

                    // Flurry Notification
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_QUICK_EXPENSE);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
                } else {
                    // Can't go there
                    showDialog(Const.DIALOG_EXPENSE_NO_EXPENSE_TYPES);
                }
                break;
            case R.id.homeMileage:
                showMileageDialog();
                break;

            case R.id.homeRowTR:
                i = showTravelRequest(i);
                break;

            case R.id.homeRowExpenseReports: {
                cancelAllDataRequests();
                i = new Intent(this, ExpenseActiveReports.class);
                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_VIEW_REPORTS);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
                break;
            }

            case R.id.homeExpenseIt: {
                DialogFragment newFragment = new ReceiptChoiceDialogFragment();
                newFragment.show(this.getSupportFragmentManager(), ReceiptChoiceDialogFragment.DIALOG_FRAGMENT_ID);
                break;
            }

            case R.id.homeCamera: {
                cancelAllDataRequests();
                captureReceipt();
                break;
            }
            case R.id.homeRowBookCar:
            case R.id.homeRowBookFlight:
            case R.id.homeRowBookHotel:
            case R.id.homeRowBookTrain:
                handleBookingClick(id);
                // We don't worry about setting i here because the handle() method
                // will launch the activity
                break;
        }

        if (i != null) {
            ConcurMobile.userClickTime = System.currentTimeMillis();
            if (requestCode == null) {
                startActivity(i);
            } else {
                startActivityForResult(i, requestCode);
            }
        }
    }

    /**
     * - ongoing -
     *
     * @param
     * @return
     */
    private Intent showTravelRequest(Intent i) {
        if (!ConcurCore.isConnected()) {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        } else if (ViewUtil.isTravelRequestUser(this)) {
            i = new Intent(Home.this, RequestListActivity.class);
            i.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_HOME);
            i.putExtra(RequestListActivity.KEY_SEARCHED_STATUS, RequestStatus.ACTIVE.name());

            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_TRAVEL_REQUEST);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
        }
        return i;
    }

    /**
     * Captures a receipt image to be imported into the receipt store.
     */
    protected void captureReceipt() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Create a place for the camera to write its output.
            String receiptFilePath = ViewUtil.createExternalMediaImageFilePath();
            File receiptFile = new File(receiptFilePath);
            Uri outputFileUri = Uri.fromFile(receiptFile);
            receiptCameraImageDataLocalFilePath = receiptFile.getAbsolutePath();
            Log.d(Const.LOG_TAG,
                    CLS_TAG + ".captureReceipt: receipt image path -> '" + receiptCameraImageDataLocalFilePath + "'.");
            // Launch the camera application.
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            try {
                startActivityForResult(intent, REQUEST_TAKE_PICTURE);
            } catch (Exception e) {
                // Device has no camera, see MOB-16872
            }

        } else {
            showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = dialogs.get(id);

        if (dlg == null) {
            switch (id) {
                case Const.DIALOG_EXPENSE_NO_MILEAGE_FORM: {
                    AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                    dlgBldr.setTitle(R.string.dlg_expense_no_mileage_form_title);
                    dlgBldr.setMessage(R.string.dlg_expense_no_mileage_form);
                    dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    return dlgBldr.create();

                }
                case Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY: {
                    dlg = super.onCreateDialog(id);
                    dlg.setOnCancelListener(new OnCancelListener() {

                        public void onCancel(DialogInterface dialog) {
                            // Cancel any outstanding request.
                            if (itineraryRequest != null) {
                                itineraryRequest.cancel();
                            }
                        }
                    });
                    break;
                }
                case Const.DIALOG_ALLOW_REPORTS: {
                    AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                    dlgBldr.setTitle(R.string.home_navigation_car_mileage_not_allow_title);
                    dlgBldr.setMessage(R.string.home_navigation_car_mileage_not_allow_msg);
                    dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    return dlgBldr.create();
                }
                default:
                    dlg = ((ConcurMobile) getApplication()).createDialog(this, id);
                    dialogs.put(id, dlg);
                    break;
            }
        }

        return dlg;
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
            case Const.CREATE_MOBILE_ENTRY:
                if (resultCode == Activity.RESULT_OK) {
                    // Go straight to the entries list
                    Intent intent;
                    if (Preferences.shouldUseNewOcrFeatures()) {
                        intent = new Intent(this, ExpensesAndReceiptsActivity.class);
                    } else {
                        intent = new Intent(this, ExpensesAndReceipts.class);
                    }
                    intent.putExtra(Const.EXTRA_CHECK_PROMPT_TO_RATE, true);
                    startActivity(intent);
                }
                break;
            case Const.CREATE_NEW_REPORT: {

                if (data != null && data.hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
                    String reportKey = data.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
                    if (reportKey != null) {
                        getMileageEntryForm(reportKey);
                    }
                }
                break;
            }
            case Const.CREATE_MILEAGE_EXPENSE:
                if (resultCode == Activity.RESULT_OK) {
                    // Go to the report
                    Intent i = new Intent(this, ExpenseEntries.class);
                    i.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, data.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY));
                    i.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE,
                            data.getIntExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, Const.EXPENSE_REPORT_SOURCE_ACTIVE));
                    startActivity(i);
                }
                break;
            case Const.REQUEST_CODE_BOOK_HOTEL:
                if (resultCode == Activity.RESULT_OK) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurMobile.getContext());
                    if (Preferences.shouldPromptToRate(prefs, false)) {
                        showDialog(Const.DIALOG_PROMPT_TO_RATE);
                    }
                }
                break;

            case REQUEST_TAKE_PICTURE: {
                if (resultCode == Activity.RESULT_OK) {
                    if (copyCapturedImage()) {

                        // Flurry Notification
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_CAMERA);
                        params.put(Flurry.PARAM_NAME_RESULT, Flurry.PARAM_VALUE_OKAY);

                        Intent it = new Intent(this, ViewImage.class);
                        StringBuilder strBldr = new StringBuilder("file://");
                        strBldr.append(receiptImageDataLocalFilePath);
                        it.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, strBldr.toString());
                        it.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, true);
                        it.putExtra(Const.EXTRA_SHOW_MENU, true);
                        it.putExtra(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH, receiptImageDataLocalFilePath);
                        it.putExtra(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_CAMERA);
                        it.putExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY, getText(R.string.receipt_image));
                        startActivity(it);

                    } else {
                        // Flurry Notification.
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Flurry.PARAM_NAME_FAILURE,
                                Flurry.PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE);
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                        showDialog(Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED);
                    }
                } else {
                    // Flurry Notification
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_CAMERA);
                    params.put(Flurry.PARAM_NAME_RESULT, Flurry.PARAM_VALUE_CANCEL);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
                }
                break;
            }
            case REQUEST_CODE_MSG_CENTER:
                if (resultCode == Activity.RESULT_OK) {
                    // refresh option menu
                    supportInvalidateOptionsMenu();
                }
                break;
        }
    }

    /**
     * Will copy the image data captured by the camera.
     *
     */
    private boolean copyCapturedImage() {
        boolean retVal = true;
        // Assign the path written by the camera application.
        receiptImageDataLocalFilePath = receiptCameraImageDataLocalFilePath;
        retVal = ImageUtil.compressAndRotateImage(receiptImageDataLocalFilePath);
        if (!retVal) {
            receiptImageDataLocalFilePath = null;
        }
        return retVal;
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

        ConcurMobile concurMobile = (ConcurMobile) getApplication();

        // Request card list + summary count if expense user.
        if (RolesUtil.isExpenser(Home.this) || RolesUtil.isExpenseApprover(Home.this) || RolesUtil
                .isTRApprover(Home.this) || RolesUtil.isInvoiceApprover(Home.this) || RolesUtil.isInvoiceUser(Home.this)
                || RolesUtil.isTravelApprover(Home.this)) {

            // Grab the summary count.
            // Immediately show cached data.
            concurMobile.getService().getCountSummary();
            updateExpenseSummaryUI();

            // MOB-15537
            if (RolesUtil.isExpenser(Home.this)) {
                // get car config data immediately show cached data.
                concurMobile.getService().getCarConfigs();
            }
            // get car config data immediately show cached data.
            concurMobile.getSystemConfig();
            // get car config data immediately show cached data.
            concurMobile.getUserConfig();
            // If the client is in a connected state, then make the request.
            if (ConcurMobile.isConnected()) {
                countSummaryRequest = concurMobile.getService().sendCountSummaryRequest(true);
                // Don't increment the progress reference count if the request
                // wasn't created.
                if (countSummaryRequest != null) {
                    incrInProgressRef();
                }
                if (RolesUtil.isExpenser(Home.this)) {
                    carConfigsRequest = concurMobile.getService().sendCarConfigsRequest();
                }
                if (carConfigsRequest != null) {
                    incrInProgressRef();
                }
            }
        }

        // Request trips if traveler or itinerary viewer.
        if (RolesUtil.isTraveler(Home.this) || RolesUtil.isItinViewer(Home.this) || RolesUtil.isTRApprover(Home.this)
                || RolesUtil.isInvoiceApprover(Home.this) || RolesUtil.isInvoiceUser(Home.this) || RolesUtil
                .showTripsForOpenBookingUser(Home.this)) {

            // Immediately show the cached data
            concurMobile.getService().getItinerarySummaryList();
            updateTripUI();

            // If connected, then send a request for an updated itinerary list.
            if (ConcurMobile.isConnected()) {
                itinerarySummaryListRequest = concurMobile.getService().sendItinerarySummaryListRequest(true);
                // Don't increment the progress reference count if the
                // request wasn't created.
                if (itinerarySummaryListRequest != null) {
                    incrInProgressRef();
                }
            }
        }

    }

    private void cancelAllDataRequests() {

        ServiceRequest request = countSummaryRequest;
        if (request != null) {
            request.cancel();
        }

        request = itinerarySummaryListRequest;
        if (request != null) {
            request.cancel();
        }

        // cancel car config request
        request = carConfigsRequest;
        if (request != null) {
            request.cancel();
        }
    }

    /**
     * Will examine the result of an attempt to retrieve summary information and update the UI.
     *
     * @param intent the intent object containing the result information.
     */
    protected void updateExpenseSummaryInfo(Intent intent) {

        int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
        if (serviceRequestStatus != -1) {
            if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                if (httpStatusCode != -1) {
                    if (httpStatusCode == HttpStatus.SC_OK) {
                        if (!(intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS))) {
                            actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: mobile web service error -- " + actionStatusErrorMessage
                                            + ".");
                        }
                    } else {
                        lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- " + lastHttpErrorMessage + ".");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                }
            } else {
                if (countSummaryRequest != null && !countSummaryRequest.isCanceled()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- " + intent
                            .getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
        }

        updateExpenseSummaryUI();

        decrInProgressRef();

        // Reset the request.
        countSummaryRequest = null;
    }

    /**
     * Will update the UI to reflect no summary data available.
     */
    private void updateExpenseSummaryNoDataAvailable() {

        if (RolesUtil.isExpenser(Home.this)) {
            TextView subView = getRowSubheader(R.id.homeRowExpenseReports);
            subView.setText(R.string.home_row_expensereports_subheader_negative);
        }

        if (RolesUtil.isExpenseApprover(Home.this) || RolesUtil.isTRApprover(Home.this) || RolesUtil
                .isInvoiceApprover(Home.this) || RolesUtil.isInvoiceUser(Home.this)) {
            TextView subView = getRowSubheader(R.id.homeRowApprovals);
            subView.setText(R.string.home_row_approvals_subheader_negative);
        }

        // Set the expenses message.
        TextView subView = getRowSubheader(R.id.homeRowExpenses);
        subView.setText(R.string.home_row_expenses_subheader_negative);
    }

    /**
     * Update the count (approvals, entries and reports) sections
     */
    protected void updateExpenseSummaryUI() {

        ConcurMobile concurMobile = (ConcurMobile) getApplication();
        CountSummary summary = concurMobile.getSummary();

        if (summary != null) {

            if (RolesUtil.isExpenser(Home.this)) {
                TextView subView = getRowSubheader(R.id.homeRowExpenseReports);
                int unsubCount = summary.unsubmittedReportsCount;
                final TextView txtvExpensesReportBadge = (TextView) findViewById(R.id.txtvExpensesReportBadge);

                if (unsubCount > 0) {
                    txtvExpensesReportBadge.setVisibility(View.VISIBLE);
                    txtvExpensesReportBadge.setText(Integer.toString(unsubCount));
                } else {
                    txtvExpensesReportBadge.setVisibility(View.INVISIBLE);
                }
                subView.setText(R.string.home_row_expensereports_subheader_negative);
            }

            if (RolesUtil.isTravelApprover(Home.this) || RolesUtil.isExpenseApprover(Home.this) || RolesUtil
                    .isTRApprover(Home.this) || RolesUtil.isInvoiceApprover(Home.this) || RolesUtil
                    .isInvoiceUser(Home.this)) {
                TextView subView = getRowSubheader(R.id.homeRowApprovals);
                final TextView txtvApprovalBadge = (TextView) findViewById(R.id.txtvApprovalBadge);

                int approvalCount = 0;
                if (RolesUtil.isTravelApprover(Home.this)) {
                    approvalCount += summary.tripsToApproveCount;
                }
                if (RolesUtil.isExpenseApprover(Home.this)) {
                    approvalCount += summary.reportsToApprove;
                }
                if (RolesUtil.isTRApprover(Home.this)) {
                    approvalCount += summary.travelRequestsToApprove;
                }
                if (RolesUtil.isInvoiceApprover(Home.this) || RolesUtil.isInvoiceUser(Home.this)) {
                    approvalCount += summary.invoicesToApprove + summary.invoicesToSubmit;
                }
                if (ViewUtil.isPurchaseRequestApprover(this)) {
                    approvalCount += summary.purchaseRequestsToApprove;
                }

                if (approvalCount > 0) {
                    txtvApprovalBadge.setVisibility(View.VISIBLE);
                    txtvApprovalBadge.setText(Integer.toString(approvalCount));
                } else {
                    txtvApprovalBadge.setVisibility(View.INVISIBLE);
                }
                subView.setText(R.string.home_row_approvals_subheader_negative);
            }

            // Set the expenses message.
            TextView subView = getRowSubheader(R.id.homeRowExpenses);
            final TextView txtvExpensesBadge = (TextView) findViewById(R.id.txtvExpensesBadge);

            int expenseCount = summary.corpCardTransCount + summary.mobileEntryCount + summary.persCardTransCount
                    + summary.receiptCaptureCount;
            if (expenseCount > 0) {
                txtvExpensesBadge.setVisibility(View.VISIBLE);
                txtvExpensesBadge.setText(Integer.toString(expenseCount));
            } else {
                txtvExpensesBadge.setVisibility(View.INVISIBLE);
            }
            subView.setText(R.string.home_row_expenses_subheader_negative);

        } else {
            // Indicate there is no summary data available
            updateExpenseSummaryNoDataAvailable();
        }
    }

    /**
     * Determines whether personal car mileage should be displayed on action menu. Requires car configs to have been loaded.
     */
    private boolean showPersonalCarMileage() {

        // Test Drive users never show Personal Car Mileage in the footer.
        if (Preferences.isTestDriveUser()) {
            return false;
        }
        // MOB-20183 requires that if offline and showCarMileage preference is true then show the car mileage to bottom bar.
        if ((!ConcurMobile.isConnected()) && (ViewUtil.isShowMileageExpenseOnHomeScreenEnabled(Home.this))) {
            return true;
        }

        ConcurMobile concurMobile = (ConcurMobile) getApplication();
        ArrayList<CarConfig> carConfigList = concurMobile.getCarConfigs();
        if (carConfigList == null && concurMobile.getService() != null) {
            carConfigList = concurMobile.getService().getCarConfigs();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userCurrencyCode = prefs.getString(Const.PREF_USER_CRN_CODE, null);

        if (carConfigList != null && carConfigList.size() > 0) {
            if (RolesUtil.isExpenser(Home.this)) {
                for (int i = 0; i < carConfigList.size(); i++) {
                    // Check rates availability under PER_ONE (fixed) or cars
                    // availability under PER_VARIABLE
                    CarConfig carConfig = carConfigList.get(i);
                    if (carConfig != null) {
                        if (carConfig.crnCode.equalsIgnoreCase(userCurrencyCode) && ((
                                carConfig.configType.equalsIgnoreCase(CarConfig.TYPE_PER_ONE)
                                        && carConfig.rates.size() > 0))
                                // TODO this line of code is not required.
                                // Double check with Walt
                                // ||
                                // ((carConfig.configType.equalsIgnoreCase(CarConfig.TYPE_COM_FIX)
                                // && carConfig.rates.size()>0))
                                || ((carConfig.configType.equalsIgnoreCase(CarConfig.TYPE_PER_VAR)
                                && carConfig.details.size() > 0))) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
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
                        if (!(intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS))) {
                            actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: mobile web service error -- " + actionStatusErrorMessage
                                            + ".");
                        }
                    } else {
                        lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- " + lastHttpErrorMessage + ".");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                }
            } else {
                if (itinerarySummaryListRequest != null && !itinerarySummaryListRequest.isCanceled()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- " + intent
                            .getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
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
        TextView sub = getRowSubheader(R.id.homeRowTravel);
        sub.setText(R.string.home_row_travel_subheader_negative);
    }

    /**
     * Will examine the current set of trips and update the UI accordingly.
     */
    private void updateTripUI() {

        if (RolesUtil.isTraveler(Home.this) || RolesUtil.isItinViewer(Home.this) || RolesUtil
                .showTripsForOpenBookingUser(Home.this)) {

            ConcurMobile concurMobile = (ConcurMobile) getApplication();
            IItineraryCache itinCache = concurMobile.getItinCache();
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
                        } else if (trip.endLocal != null && trip.endLocal.after(curTime)) {
                            // MOB-11072 Try using the local time.
                            if (trip.startLocal != null && trip.startLocal.before(curTime)) {
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
                    final TextView txtvTripBadge = (TextView) findViewById(R.id.txtvTripBadge);

                    if (activeTripCount > 0 || upcomingTripCount > 0) {
                        txtvTripBadge.setVisibility(View.VISIBLE);
                        txtvTripBadge.setText(Integer.toString(upcomingTripCount + activeTripCount));
                    } else {
                        txtvTripBadge.setVisibility(View.INVISIBLE);
                    }

                    updateTripNoDataAvailable();

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

    protected TextView getRowSubheader(int rowId) {
        View v = findViewById(rowId);
        if (v != null) {
            return (TextView) v.findViewById(R.id.rowSubheader);
        }
        return null;
    }

    /**
     * Sets the visibility property on a view to <code>View.GONE</code>.
     *
     * @param resId the resource id of the view.
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
     * @param resId the resource id of the view.
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
     * @param resId the resource id of the view.
     */
    private void setViewInvisible(int resId) {
        View view = findViewById(resId);
        if (view != null) {
            if (view.getVisibility() != View.INVISIBLE) {
                view.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void showBookFooterButton() {
        showFooter();
        setViewVisible(R.id.homeBook);
    }

    private void hideBookFooterButton() {
        setViewGone(R.id.homeBook);
    }

    private void showReceiptFooterButton() {
        showFooter();
        setViewVisible(R.id.homeCamera);
    }

    private void hideReceiptFooterButton() {
        setViewGone(R.id.homeCamera);
    }

    private void showQuickExpenseFooterButton() {
        showFooter();
        setViewVisible(R.id.homeQuickExpense);
    }

    private void hideQuickExpenseFooterButton() {
        setViewGone(R.id.homeQuickExpense);
    }

    private void showExpenseItFooterButton() {
        showFooter();
        setViewVisible(R.id.homeExpenseIt);
    }

    private void hideExpenseItFooterButton() {
        setViewGone(R.id.homeExpenseIt);
    }

    private void showTravelRequestRow() {
        showFooter();
        setViewVisible(R.id.homeRowTR);
    }

    private void hideTravelRequestRow() {
        setViewGone(R.id.homeRowTR);
    }

    private void showMileageFooterButton() {
        showFooter();
        setViewVisible(R.id.homeMileage);
    }

    private void showMileageDrawerButton(int visibility) {
        if (!navItems.isEmpty()) {
            for (NavigationItem i : navItems) {
                if (i.getId() == NAVIGATION_PERSONAL_CAR_MILEAGE) {
                    // layout visibility
                    LinearLayout ll = (LinearLayout) findViewById(R.id.drawer_item_container);
                    View v = ll.findViewById(i.getId());
                    v.setVisibility(visibility);
                }
            }
        }
    }

    private void hideMileageFooterButton() {
        setViewGone(R.id.homeMileage);
    }

    private void hideFooter() {
        setViewGone(R.id.homeFooter);
    }

    private void showFooter() {
        setViewVisible(R.id.homeFooter);
    }

    private void hideBadges() {
        final TextView txtvExpensesReportBadge = (TextView) findViewById(R.id.txtvExpensesReportBadge);
        txtvExpensesReportBadge.setVisibility(View.INVISIBLE);

        final TextView txtvApprovalBadge = (TextView) findViewById(R.id.txtvApprovalBadge);
        txtvApprovalBadge.setVisibility(View.INVISIBLE);

        final TextView txtvTripBadge = (TextView) findViewById(R.id.txtvTripBadge);
        txtvTripBadge.setVisibility(View.INVISIBLE);

        final TextView txtvExpensesBadge = (TextView) findViewById(R.id.txtvExpensesBadge);
        txtvExpensesBadge.setVisibility(View.INVISIBLE);

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
        setViewGone(R.id.homeRowTravel);
    }

    /**
     * Shows any trip-related elements
     */
    private void showTripsUI() {
        setViewVisible(R.id.homeRowTravel);
    }

    /**
     * Hides any elements that book through cliqbook
     */
    private void hideBookingUI() {
        // Hide the booking rows, just in case
        setViewGone(R.id.homeAllBooking);

        // Hide the booking icon
        setViewGone(R.id.homeBook);

        // Disable the click
        findViewById(R.id.homeBook).setClickable(false);

        // Disable any tip UI.
        hideTipsTravelUI();
    }

    private void showBookingRows() {
        setViewVisible(R.id.homeAllBooking);

        if (!isRailUser()) {
            // You've been thrown from the train
            setViewGone(R.id.homeRowBookTrain);
        }

    }

    /**
     * Will hide the entire expense section.
     */
    private void hideAppExpUI() {
        hideApproverUI();
        hideExpenseUI();
    }

    /**
     * Will hide all approver UI elements.
     */
    private void hideApproverUI() {
        setViewGone(R.id.homeRowApprovals);
    }

    /**
     * Will hide the whole Invoice section.
     */
    private void hideInvoiceUI() {
        // TODO9
        // // Hide the row
        // setViewGone(R.id.homeSectionInvoice);
    }

    /**
     * Will hide the reports section.
     */
    private void hideReportsUI() {
        setViewGone(R.id.homeRowExpenseReports);
    }

    /**
     * Will hide all expense UI elements.
     */
    private void hideExpenseUI() {
        // Hide the reports section.
        setViewGone(R.id.homeRowExpenseReports);

        // Hide the expenses section.
        setViewGone(R.id.homeRowExpenses);

        // Hide the Camera.
        hideReceiptFooterButton();

        // Disable the camera click.
        findViewById(R.id.homeCamera).setClickable(false);

        // Disable any tip UI text.
        hideTipsExpenseUI();
    }

    /**
     * Checks how many footer buttons are visible on the Home Screen
     *
     * @return number of visible buttons
     */
    private int numberOfVisibleFooterButtons() {
        int numVisible = 0;

        if (findViewById(R.id.homeBook).getVisibility() == View.VISIBLE) {
            numVisible++;
        }

        if (findViewById(R.id.homeCamera).getVisibility() == View.VISIBLE) {
            numVisible++;
        }

        if (findViewById(R.id.homeExpenseIt).getVisibility() == View.VISIBLE) {
            numVisible++;
        }

        if (findViewById(R.id.homeMileage).getVisibility() == View.VISIBLE) {
            numVisible++;
        }

        return numVisible;
    }

    /**
     * Sets the text size of the footer buttons to a smaller size. This is called if all four footer buttons are showing because
     * some locales have long translations and cause text to wrap in the default size.
     */
    private void setSmallFooterButtonText() {
        float textSize = getResources().getDimension(R.dimen.homeFooterSmallText);

        TextView buttonText = (TextView) findViewById(R.id.homeBookText);
        buttonText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        buttonText = (TextView) findViewById(R.id.homeCameraText);
        buttonText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        buttonText = (TextView) findViewById(R.id.homeExpenseItText);
        buttonText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        buttonText = (TextView) findViewById(R.id.homeMileageText);
        buttonText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    /**
     * An implementation of <code>DialogInterface.OnCancelListener</code> to handle canceling the mileage expense action.
     */
    class DialogCancelListener implements DialogInterface.OnCancelListener {

        /*
         * (non-Javadoc)
         * 
         * @see android.content.DialogInterface.OnCancelListener#onCancel(android .content.DialogInterface)
         */
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
        }

    }

    /**
     * An implementation of <code>DialogInterface.OnClickListener</code> to handle selecting a report.
     */
    class SelectReportDialogClickListener implements DialogInterface.OnClickListener {

        public void onClick(DialogInterface dialog, int which) {

            // Dismiss the dialog.
            dialog.dismiss();

            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_PERSONAL_CAR_MILEAGE);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);

            Fragment reportListDialogFrag = getSupportFragmentManager().findFragmentByTag(ACTIVE_REPORTS_LIST_TAG);
            if (reportListDialogFrag instanceof ActiveReportsListDialogFragment) {
                activeReportListAdapter = ((ActiveReportsListDialogFragment) reportListDialogFrag)
                        .getActiveReportsListAdapter();
                if (!activeReportListAdapter.isNewOptionSelected(which)) {
                    // An existing report was selected, just start the creation
                    // process.
                    ExpenseReport report = (ExpenseReport) activeReportListAdapter.getItem(which);

                    ConcurCore ConcurCore = (ConcurCore) getApplication();
                    IExpenseReportCache expRepCache = ConcurCore.getExpenseApprovalCache();
                    if (!expRepCache.hasReportDetail(report.reportKey)) {
                        // Fire off a request to get the detailed report. This
                        // is
                        // necessary because after
                        // saving the mileage entry we go directly to the entry
                        // list
                        // and that requires a detail.
                        // Just get it now.
                        ConcurService concurService = ConcurCore.getService();
                        concurService.sendReportDetailRequest(report.reportKey, Const.EXPENSE_REPORT_SOURCE_ACTIVE);
                    }

                    // Call out to get the entry form
                    getMileageEntryForm(report.reportKey);

                } else {
                    // Invoke the ExpenseReportHeader class and specify that
                    // source
                    // as
                    // "New".
                    Intent intent = new Intent(Home.this, ExpenseReportHeader.class);
                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, Const.EXPENSE_REPORT_SOURCE_NEW);
                    Home.this.startActivityForResult(intent, Const.CREATE_NEW_REPORT);
                }
            } else {
                Log.w(CLS_TAG, "Failed to find fragment for ACTIVE_REPORTS_LIST_TAG");
            }

        }

    }

    /**
     * Retrieve the mileage entry form fields.
     *
     * @param reportKey a protected report key
     */
    protected void getMileageEntryForm(String reportKey) {
        // Call out to get the entry form
        // Receiver will call addMileageExpense
        ConcurMobile app = (ConcurMobile) getApplication();
        ConcurService service = app.getService();
        service.sendReportEntryFormRequest(Const.EXPENSE_TYPE_MILEAGE, reportKey, null);

        showDialog(Const.DIALOG_EXPENSE_ENTRY_FORM);
    }

    /**
     * Hand off to the mileage expense entry form.
     *
     * @param intent
     */
    protected void addMileageExpense(Intent intent) {

        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_HOME);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_REPORT_ENTRY, Flurry.EVENT_NAME_CREATE, params);

        Intent i = new Intent(this, ExpenseEntryMileage.class);
        i.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY));
        i.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, Const.EXPENSE_REPORT_SOURCE_ACTIVE);
        startActivityForResult(i, Const.CREATE_MILEAGE_EXPENSE);

        dismissDialog(Const.DIALOG_EXPENSE_ENTRY_FORM);
    }

    /**
     * Return an intent to launch Tripit if installed or the market otherwise
     *
     * @return
     */
    // MOB-11146 required URI change
    private Intent getTripitIntent() {
        Intent intent = null;
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(TRAVEL_TEXT_URI));
        return intent;
    }

    // /////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////
    // Location code
    // /////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////

    /**
     * Tries to get the user's last known location.
     */
    private void requestLastKnownLocation() {
        LastLocationTracker locTracker = ((ConcurCore) getApplication()).getLocationTracker();
        locTracker.startLocationTrace(CLS_TAG, null, true, Const.LOC_UPDATE_MIN_TIME, Const.LOC_UPDATE_MIN_DISTANCE);
    }

    /**
     * Starts the updates to get the user's last known location.
     */
    private void startLocationUpdates() {
        requestLastKnownLocation();
    }

    /**
     * Stops and unregisters the Location Provider receiver.
     */
    private void stopLocationUpdates() {
        // Unregister location requestor, if this activity is to be destroyed
        // if the one time location request has been fulfilled, the requestor
        // will be automatically unregistered.
        LastLocationTracker locTracker = ((ConcurCore) getApplication()).getLocationTracker();
        locTracker.stopLocationTrace(CLS_TAG);
    }

    /**
     * Will send a request to obtain an itinerary.
     */
    private void sendItineraryRequest(String itinLocator) {
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
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of an itinerary list request.
     */
    static class ItineraryReceiver extends BaseBroadcastReceiver<Home, ItineraryRequest> {

        /**
         * Constructs an instance of <code>ItineraryReceiver</code>.
         *
         * @param activity the activity.
         */
        ItineraryReceiver(Home activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.corp.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(Home activity) {
            activity.itineraryRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            if (intent.hasExtra(Const.EXTRA_ITIN_LOCATOR)) {
                String itinLocator = intent.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
                if (itinLocator != null) {
                    Intent i = new Intent(activity, SegmentList.class);
                    i.putExtra(Const.EXTRA_ITIN_LOCATOR, itinLocator);
                    activity.startActivity(i);

                    // Flurry Notification
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_VIEW_CURRENT_TRIP);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
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
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.corp.activity. BaseActivity, com.concur.mobile.corp.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ItineraryRequest request) {
            activity.itineraryRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterItineraryReceiver();
        }

    }

    /**
     * Create a new AsyncTask to handle travel cityscape image manipulation
     *
     * @return
     */
    protected AsyncTask<Void, Void, Integer> initImageManipulator(final ImageSwitcher imgSwitcher) {
        Log.d(Const.LOG_TAG, "imgMan // init");
        return new AsyncTask<Void, Void, Integer>() {

            int[] cityscapes = {R.drawable.city_01, R.drawable.city_02, R.drawable.city_03, R.drawable.city_04,
                    R.drawable.city_05, R.drawable.city_06, R.drawable.city_07, R.drawable.city_08,
                    R.drawable.city_09};
            ImageSwitcher cityscape = imgSwitcher;

            @Override
            protected Integer doInBackground(Void... params) {

                Log.d(Const.LOG_TAG, "imgMan // doIB // cityscape = " + cityscape);
                Integer cityscapeResourceId = null;

                if (cityscape != null) {
                    Log.d(Const.LOG_TAG, "imgMan // checking to update");
                    // Check if we should update the image
                    if (Preferences.shouldUpdateCityscape()) {
                        Log.d(Const.LOG_TAG, "imgMan // updating");
                        // int currentCity = Preferences.getCurrentCityscape();
                        int currentCity = 0;
                        // Get a new city
                        int city;
                        do {
                            city = (int) (Math.random() * cityscapes.length);
                        } while (city == currentCity);

                        cityscapeResourceId = cityscapes[city];

                        Log.d(Const.LOG_TAG, "imgMan // resId = " + cityscapeResourceId);
                    }
                }

                return cityscapeResourceId;
            }

            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                Log.d(Const.LOG_TAG, "imgMan // onPE //  res = " + result);
                if (result != null) {
                    cityscape.setImageResource(result);
                    Preferences.setCityscapeUpdated(result);
                }

            }

        };
    }

    //
    // Navigation Stuff
    //

    /**
     * Initialize all of the Navigation buttons to be runnable inside of the Navigation DrawerLayout
     */
    private void initNavigationMenu() {

        HomeScreenSimpleNavigationItem navItem = null;
        DefaultTextNavigationItem setSegNavItem = null;
        if (!isProfileDisable) {
            // Add the navigation segment bar.
            setSegNavItem = new DefaultTextNavigationItem(NAVIGATION_HEADER, R.layout.navigation_segment,
                    R.string.home_navigation_settings, false);
            navItems.add(setSegNavItem);

            // Profile access.
            navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_PROFILE, -1, R.string.home_navigation_profile,
                    R.drawable.profile_icon_name, View.VISIBLE, View.VISIBLE, new Runnable() {

                public void run() {
                    // Launch the receipt store.
                    Intent intent;
                    intent = new Intent(Home.this, ProfileInfo.class);
                    startActivity(intent);

                    // Flurry Notification
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_PROFILE);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
                }
            });
            navItems.add(navItem);

        }

        // Add the navigation segment bar.
        DefaultTextNavigationItem setSegNavItemProfile = new DefaultTextNavigationItem(NAVIGATION_HEADER,
                R.layout.navigation_segment, R.string.home_navigation_settings, false);
        navItems.add(setSegNavItemProfile);

        // Should allow and travel user check.
        if (Preferences.shouldAllowTravelBooking() && RolesUtil.isTraveler(Home.this)) {
            navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_BOOK_TRAVEL, -1,
                    R.string.home_navigation_book_travel, R.drawable.icon_menu_book, View.VISIBLE, View.VISIBLE,
                    new Runnable() {

                        public void run() {
                            View homeBookTravel = findViewById(R.id.homeBook);
                            if (homeBookTravel != null) {
                                isFromMoreMenu = true;
                                onClick(homeBookTravel);

                                // Flurry Notifications.
                                Map<String, String> params = new HashMap<String, String>();
                                params.put(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_BOOK_TRAVEL);
                                EventTracker.INSTANCE
                                        .track(Flurry.CATEGORY_HOME_MORE, Flurry.EVENT_NAME_ACTION, params);
                            }
                        }
                    });
            navItems.add(navItem);
        }

        // show Price To Beat menu if allowed
        if (RolesUtil.showPriceToBeatGenerator(Home.this)) {
            navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_TRAVEL_POINTS, -1,
                    R.string.home_navigation_price_to_beat, R.drawable.icon_menu_book, View.VISIBLE, View.VISIBLE,
                    new Runnable() {

                        public void run() {
                            // Launch the travel points sub menu.
                            showTravelPointsDialog();

                            // TODO - Flurry Notification
                        }
                    });
            navItems.add(navItem);
        }

        // Receipt Store access.
        if (!ViewUtil.isReceiptStoreHidden(this) && RolesUtil.isExpenser(Home.this)) {
            navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_RECEIPTS, -1, R.string.receipts,
                    R.drawable.icon_menu_receipt, View.VISIBLE, View.VISIBLE, new Runnable() {

                public void run() {
                    // Launch the receipt store.
                    Intent intent;
                    if (Preferences.shouldUseNewOcrFeatures()) {
                        intent = new Intent(Home.this, ExpensesAndReceiptsActivity.class);
                    } else {
                        intent = new Intent(Home.this, ExpensesAndReceipts.class);
                    }

                    intent.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, true);
                    startActivity(intent);

                    // Flurry Notification
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_RECEIPT_STORE);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
                }
            });
            navItems.add(navItem);
        }

        // Access to Personal Car Mileage
        if (RolesUtil.isExpenser(Home.this) && ViewUtil.isShowMileageExpenseOnHomeScreenEnabled(Home.this)) {
            // Check whether or not we need to show the mileage icon in the
            // navigation drawer.
            int navMileageVisibility = (showPersonalCarMileage()) ? View.VISIBLE : View.GONE;
            navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_PERSONAL_CAR_MILEAGE, -1,
                    R.string.home_navigation_car_mileage, R.drawable.icon_menu_mileage, View.VISIBLE,
                    navMileageVisibility, new Runnable() {

                public void run() {
                    showMileageDialog();
                }
            });
            navItems.add(navItem);
        }

        // Location Check-in.
        if (ViewUtil.isLNAUser(getBaseContext()) && RolesUtil.canCheckInLocation(Home.this)) {
            navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_CHECK_IN, -1,
                    R.string.home_action_location_check_in, R.drawable.icon_menu_location, View.VISIBLE, View.VISIBLE,
                    new Runnable() {

                        public void run() {
                            if (!ConcurMobile.isConnected()) {
                                showDialog(Const.DIALOG_NO_CONNECTIVITY);
                            } else {
                                // Launch the activity.
                                Intent i = new Intent(Home.this, LocationCheckIn.class);
                                startActivity(i);

                                // Flurry Notification
                                Map<String, String> params = new HashMap<String, String>();
                                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_LOCATION_CHECK_IN);
                                EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
                            }
                        }
                    });
            navItems.add(navItem);
        }

        // Add the "Learn More" tour here.
        if (RolesUtil.isTraveler(Home.this) && RolesUtil.isExpenser(Home.this)) {
            navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_LEARN_MORE, -1, R.string.home_navigation_learn_more,
                    R.drawable.icon_menu_help, View.VISIBLE, View.VISIBLE, new Runnable() {

                public void run() {
                    if (!ConcurMobile.isConnected()) {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    } else {
                        // Launch the activity.
                        Intent i = new Intent(Home.this, Tour.class);
                        startActivity(i);

                        // Flurry Notification.
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_TOUR);
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
                    }
                }
            });
            navItems.add(navItem);
        }

        if (navItems.size() > 1) {
            // Add the navigation segment bar.
            setSegNavItem = new DefaultTextNavigationItem(NAVIGATION_HEADER, R.layout.navigation_segment,
                    R.string.home_navigation_settings, false);
            navItems.add(setSegNavItem);
        }

        // Add TripIt item.
        if (Preferences.shouldShowTripItAd()) {
            navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_APP_TRIP_IT, -1, R.string.home_navigation_tripit,
                    R.drawable.icon_menu_tripit, View.VISIBLE, View.VISIBLE, new Runnable() {

                public void run() {
                    // First check for the paid version (package
                    // name: "com.tripit.paid").
                    Intent i = ViewUtil.getPackageLaunchIntent(Home.this, "com.tripit.paid");
                    if (i == null) {
                        // Second, try for the free version (package
                        // name: "com.tripit").
                        i = ViewUtil.getPackageLaunchIntent(Home.this, "com.tripit");
                    }
                    if (i == null) {
                        String url = "market://details?id=com.tripit";
                        i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                    }
                    if (i != null) {
                        startActivity(i);
                        // Flurry Notification
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_TRIP_IT);
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_EXTERNAL_APP, Flurry.EVENT_NAME_LAUNCH, params);
                    }
                }
            });
            navItems.add(navItem);
        }

        // Add ExpenseIt item.
        if (Preferences.isExpenseItUser()) {
            navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_APP_EXPENSE_IT, -1,
                    R.string.home_navigation_expenseit, R.drawable.icon_menu_expenseit, View.VISIBLE, View.VISIBLE,
                    new Runnable() {

                        public void run() {
                            Intent i = ViewUtil.getPackageLaunchIntent(Home.this, "com.expenseit");
                            if (i == null) {
                                String url = "market://details?id=com.expenseit";
                                i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                            }
                            if (i != null) {
                                startActivity(i);
                                // Flurry Notification
                                Map<String, String> params = new HashMap<String, String>();
                                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_EXPENSE_IT);
                                EventTracker.INSTANCE
                                        .track(Flurry.CATEGORY_EXTERNAL_APP, Flurry.EVENT_NAME_LAUNCH, params);
                            }
                        }
                    });
            navItems.add(navItem);
        }

        // Add TravelText
        /*
         * Removed per https://concur.aha.io/features/CM-252
         * 
         * navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_APP_TRAVEL_TEXT, -1, R.string.home_navigation_traveltext,
         * R.drawable.icon_travel_text, View.VISIBLE, View.VISIBLE, new Runnable() {
         * 
         * public void run() { Intent i = null; String url = "http://www.traveltext.net/CMobile"; i = new
         * Intent(Intent.ACTION_VIEW); i.setData(Uri.parse(url));
         * 
         * if (i != null) { startActivity(i); // Flurry Notification
         * 
         * Map<String, String> params = new HashMap<String, String>(); params.put(Flurry.PARAM_NAME_ACTION,
         * Flurry.PARAM_VALUE_TRAVEL_TEXT); EventTracker.INSTANCE.track(Flurry.CATEGORY_EXTERNAL_APP, Flurry.EVENT_NAME_LAUNCH,
         * params);
         * 
         * } } }); navItems.add(navItem);
         */

        /*
         * Separator no longer wanted, per https://concur.aha.io/features/CM-252.
         * 
         * if (navItems.size() > 1) { // Add the apps navigation segment bar. DefaultTextNavigationItem segNavItem = new
         * DefaultTextNavigationItem(NAVIGATION_HEADER, R.layout.navigation_segment, R.string.home_navigation_apps, false);
         * navItems.add(segNavItem); }
         */

        // Add Curb (Taxi Magic) item.
        /*
         * Removed per https://concur.aha.io/features/CM-252
         * 
         * if (isTaxiUser()) { navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_APP_TAXI_MAGIC, -1,
         * R.string.home_navigation_curb, R.drawable.icon_menu_curb, View.VISIBLE, View.VISIBLE, new Runnable() {
         * 
         * public void run() { Intent i = ViewUtil.getTaxiMagicIntent(Home.this); if (i == null) { String url =
         * "http://taximagic.com"; i = new Intent(Intent.ACTION_VIEW); i.setData(Uri.parse(url)); } if (i != null) {
         * startActivity(i); // Flurry Notification Map<String, String> params = new HashMap<String, String>();
         * params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_CURB);
         * EventTracker.INSTANCE.track(Flurry.CATEGORY_EXTERNAL_APP, Flurry.EVENT_NAME_LAUNCH, params); } } });
         * navItems.add(navItem); }
         */

        // Add Uber item - for now, we will show Uber for every user
        /*
         * Removed per https://concur.aha.io/features/CM-252
         * 
         * navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_APP_UBER, -1, R.string.home_navigation_uber,
         * R.drawable.icon_menu_uber, View.VISIBLE, View.VISIBLE, new Runnable() {
         * 
         * public void run() { Intent i = ViewUtil.getPackageLaunchIntent(Home.this, "com.ubercab"); if (i == null) { // app not
         * available hence launch the mobile web site i = new Intent(Intent.ACTION_VIEW);
         * i.setData(Uri.parse("https://m.uber.com")); } if (i != null) { startActivity(i); // Flurry Notification Map<String,
         * String> params = new HashMap<String, String>(); params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_UBER);
         * EventTracker.INSTANCE.track(Flurry.CATEGORY_EXTERNAL_APP, Flurry.EVENT_NAME_LAUNCH, params); } } });
         * navItems.add(navItem);
         */

        if (RuntimeConfig.with(this).canUseAppCenter()) {
            navItem = new HomeScreenSimpleNavigationItem(NAVIGATION_APP_CENTER, -1, R.string.home_navigation_app_center,
                    R.drawable.icon_menu_connect_to_apps, View.VISIBLE, View.VISIBLE, new Runnable() {

                public void run() {
                    String bareToken = Preferences.getAccessToken();

                    if (bareToken == null) {
                        Toast.makeText(Home.this, "Unable to retrieve access token. Please log out and back in.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    Locale locale = Locale.getDefault();

                    String encodedToken = "";

                    try {
                        String appCenterUrl;
                        String serverAddress = Preferences.getServerAddress();

                        if (serverAddress.toLowerCase(locale).contains("rqa3-cb.concurtech.net")) {
                            appCenterUrl = "http://appcenterdev.concursolutions.com";
                        } else {
                            appCenterUrl = "https://appcenter.concursolutions.com";
                        }

                        encodedToken = URLEncoder.encode(bareToken, "UTF-8");

                        String urlString = appCenterUrl + "/#/?accessToken=" + encodedToken + "&lang=" + locale;

                        Intent i = new Intent(Home.this, SimpleWebViewActivity.class);
                        i.putExtra("url", urlString);

                        if (i != null) {
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_MAIN_MENU, Flurry.ACTION_APP_CENTER);

                            startActivity(i);
                        }
                    } catch (Exception e) {
                        Log.i(CLS_TAG, "Unable to URL-encode token: '" + bareToken + "'");
                    }
                }
            });
            navItems.add(navItem);
        }

        // MOB-15458 : it required to remove ad from more menu. so adnav item is
        // null.
        NavigationItem adNavItem = null;

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout itemContainer = (LinearLayout) findViewById(R.id.drawer_item_container);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.drawer_ad_container);
        addNavigationItems(itemContainer, adContainer, inflater, navItems, adNavItem);
    }

    protected void showMileageDialog() {
        // Check for connectivity, if none, then display
        // dialog and return.
        if (!ConcurMobile.isConnected()) {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        } else {
            // MOB-13027 When user clicks it, we need to
            // check site settings.
            // If access to report is not allowed, then
            // show a proper dialog.
            if (!Preferences.shouldAllowReports()) {
                showDialog(Const.DIALOG_ALLOW_REPORTS);
            } else {
                // Otherwise, pick a report, any report.
                activeReportListAdapter = new ActiveReportsListAdapter(Home.this, true, false);

                // Build the report list dialog fragment.
                ActiveReportsListDialogFragment frag = new ActiveReportsListDialogFragment();

                frag.setActiveReportsListAdapter(activeReportListAdapter);
                frag.setClickListener(new SelectReportDialogClickListener());
                frag.setCancelListener(new DialogCancelListener());

                frag.show(getSupportFragmentManager(), ACTIVE_REPORTS_LIST_TAG);
            }
        }
    }

    protected void onHomeClicked() {
        // TODO Auto-generated method stub

    }

    /**
     * Goes through all of the items to be added to the Navigation Drawer and adds them one at a time.
     *
     * @param itemContainer The LinearLayout inside of the Navigation Drawer that holds all of the NavigationItems
     * @param adContainer   The LinearLayout below itemContainer in the NavigationDrawer that holds the Ad Item
     * @param inflater      The LayoutInflater
     * @param navItems      The actual list of Navigation Items that we're to add to the Drawer
     * @param adNavItem     The advert item at the bottom of the drawer
     */
    protected void addNavigationItems(ViewGroup itemContainer, ViewGroup adContainer, LayoutInflater inflater,
                                      List<NavigationItem> navItems, NavigationItem adNavItem) {
        NavigationItem navItem = null;

        if (navItems != null) {
            int NavItemsSize = navItems.size();
            for (int i = 0; i < NavItemsSize; i++) {
                navItem = navItems.get(i);
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

                        // Remove line from last item in menu
                        if (i == navItems.size() - 1) {
                            try {
                                if (navItem.getId() != NAVIGATION_HEADER) {
                                    navView.findViewById(R.id.navigation_item_separator).setVisibility(View.INVISIBLE);
                                }
                            } catch (Exception ex) {
                                Log.e(Const.LOG_TAG, ex.getMessage());
                            }
                        }

                        // Remove line from items prior to separators in menu
                        if (i < navItems.size() - 2 && navItems.get(i + 1).getClass().getSimpleName()
                                .equalsIgnoreCase("DefaultTextNavigationItem")) {
                            try {
                                navView.findViewById(R.id.navigation_item_separator).setVisibility(View.INVISIBLE);
                            } catch (Exception ex) {
                                // TODO Figure out why this crashes when user is
                                // Approver Only
                                // TODO Update... I know why it crashes... Nav
                                // menu is empty.. gotta think about this one...
                            }
                        }

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
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".addNavigationItems: unable to locate 'icon' view in 'navigation_item' layout!");
                            }
                        } else {
                            ViewUtil.setVisibility(navView, R.id.icon, View.GONE);
                        }

                        // layout visibility
                        int layoutVisibility = ((SimpleNavigationItem) navItem).getNavItemVisibility();
                        View view = navView.findViewById(navItem.getId());
                        if (view != null) {
                            view.setVisibility(layoutVisibility);
                        }

                    } else {
                        ViewUtil.setVisibility(navView, R.id.icon, View.GONE);
                    }
                    navView.setTag(navItem);
                    itemContainer.addView(navView); // PARENT IS JUST
                    // ITEMCONTAINER
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
        // Add the navigation ad item.
        if (adNavItem != null) {
            int layResId = R.layout.navigation_ad;
            if (adNavItem.getLayoutResId() != -1) {
                layResId = adNavItem.getLayoutResId();
            }
            View adNavView = inflater.inflate(layResId, null, false);
            adNavView.setTag(adNavItem);
            adContainer.addView(adNavView);
            // Is the item selectable?
            if (adNavItem.isSelectable()) {
                adNavView.setOnClickListener(new NavigationItemOnClickListener());
            }
        }
    }

    /**
     * The OnClickListener for items inside of the Navigation Drawer. Basically just check which item was clicked and hand off to
     * the method to handle which item was pressed.
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.fragment.navigation.Navigation.NavigationListener#
     * onItemSelected(com.concur.core.fragment.navigation.Navigation .NavigationItem)
     */
    public void onItemSelected(NavigationItem item) {
        if (item instanceof HomeScreenSimpleNavigationItem) {
            HomeScreenSimpleNavigationItem homeItem = (HomeScreenSimpleNavigationItem) item;
            Handler handler = new Handler();
            handler.post(homeItem.run);
        } else if (item instanceof HomeScreenNavigationItem) {
            HomeScreenNavigationItem homeItem = (HomeScreenNavigationItem) item;
            Handler handler = new Handler();
            handler.post(homeItem.run);
        }
        // If an item is selected, close the drawer
        mDrawerView = (ScrollView) findViewById(R.id.left_drawer);
        mDrawerLayout.closeDrawer(mDrawerView);
    }

    /**
     * If this is a first-time run with a notification-enabled build, prompt the user to see if they wish to allow notifications.
     */
    protected void promptForNotifications() {

        if (Preferences.shouldPromptForNotifications()) {
            showPushNotificationConfirmationDialog();
        }
    }

    public void showPushNotificationConfirmationDialog() {
        confirmationDialog = new AlertDialogFragment();
        confirmationDialog.setTitle(R.string.prompt_notifications_allow_title);
        confirmationDialog.setMessage(R.string.prompt_notifications_allow_text);
        confirmationDialog.setPositiveButtonText(R.string.general_yes);
        confirmationDialog.setNegativeButtonText(R.string.general_no);
        confirmationDialog.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                // TODO Auto-generated method stub

            }

            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                Log.v(Const.LOG_TAG, "AWSPush // Notifications enabled");
                Preferences.setAllowNotifications(true);
            }

        });
        confirmationDialog.setNegativeButtonListener(new AlertDialogFragment.OnClickListener() {

            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                // TODO Auto-generated method stub

            }

            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                Log.v(Const.LOG_TAG, "AWSPush // Notifications enabled");
                Preferences.setAllowNotifications(false);
            }

        });
        confirmationDialog.show(getSupportFragmentManager(), null);
    }

    // show travel points menu
    protected void showTravelPointsDialog() {
        if (!ConcurMobile.isConnected()) {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), null);
        } else if (((ConcurCore) getApplication()).getUserConfig() == null) {
            // MOB-17426 - inform the user to try later
            new SystemUnavailableDialogFragment().show(getSupportFragmentManager(), null);
        } else {
            PriceToBeatDialogFragment dialogFragment = new PriceToBeatDialogFragment();
            (dialogFragment).show(getSupportFragmentManager(), null);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.dialog.ReceiptChoiceDialogFragment. ReceiptChoiceListener#onCameraSuccess(java.lang.String)
     */
    public void onCameraSuccess(String filePath) {

        // OCR - After capturing the image, launching the ExpenseAndReceipts
        // class
        // to upload/save the image to the R.S. and refresh the Receipts List
        // UI.
        Intent newIt = new Intent(Home.this, ExpensesAndReceipts.class);
        newIt.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, false);
        newIt.putExtra(ReceiptStoreFragment.EXTRA_START_OCR_ON_UPLOAD, true);
        //We may need to check for more conditions here such as if we're connected successfully to expenseit.
        newIt.putExtra(ReceiptStoreFragment.EXTRA_USE_EXPENSEIT, Preferences.isExpenseItUser());
        newIt.putExtra(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH, filePath);
        newIt.putExtra(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_CAMERA);
        newIt.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(newIt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.dialog.ReceiptChoiceDialogFragment. ReceiptChoiceListener#onCameraFailure(java.lang.String)
     */
    public void onCameraFailure(String filePath) {
        DialogFragmentFactory
                .getAlertOkayInstance(this.getText(R.string.dlg_expense_camera_image_import_failed_title).toString(),
                        R.string.dlg_expense_camera_image_import_failed_message)
                .show(getSupportFragmentManager(), null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.dialog.ReceiptChoiceDialogFragment. ReceiptChoiceListener#onGallerySuccess(java.lang.String)
     */
    public void onGallerySuccess(String filePath) {

        // OCR - Launch the ViewImage activity so the user can preview the image
        // and choose to save or cancel. If they select save, the
        // ExpenseAndReceipts
        // class will be launched to upload/save the image to the R.S. and
        // refresh
        // the Receipts List UI.
        Intent it = new Intent(this, ViewImage.class);
        StringBuilder strBldr = new StringBuilder("file://");
        strBldr.append(filePath);
        it.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, strBldr.toString());
        it.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, false);
        it.putExtra(ReceiptStoreFragment.EXTRA_START_OCR_ON_UPLOAD, true);
        //We may need to check for more conditions here such as if we're connected successfully to expenseit.
        it.putExtra(ReceiptStoreFragment.EXTRA_USE_EXPENSEIT, Preferences.isExpenseItUser());
        it.putExtra(Const.EXTRA_SHOW_MENU, true);
        it.putExtra(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH, filePath);
        it.putExtra(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_CAMERA);
        it.putExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY, getText(R.string.receipt_image));

        // Add Hide Create Expense flag to View Image page
        it.putExtra(ViewImage.EXTRA_HIDE_CREATE_EXPENSE_ACTION_MENU, true);

        startActivity(it);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.dialog.ReceiptChoiceDialogFragment. ReceiptChoiceListener#onGalleryFailure(java.lang.String)
     */
    public void onGalleryFailure(String filePath) {
        DialogFragmentFactory
                .getAlertOkayInstance(this.getText(R.string.dlg_expense_camera_image_import_failed_title).toString(),
                        R.string.dlg_expense_camera_image_import_failed_message)
                .show(getSupportFragmentManager(), null);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.dialog.ReceiptChoiceDialogFragment.
     * ReceiptChoiceListener#onStorageMountFailure(java.lang.String)
     */
    public void onStorageMountFailure(String filePath) {
        DialogFragmentFactory
                .getAlertOkayInstance(this.getText(R.string.dlg_expense_no_external_storage_available_title).toString(),
                        R.string.dlg_expense_no_external_storage_available_message)
                .show(getSupportFragmentManager(), null);

    }

    /**
     * If the user is using a device running an Android version below 4.1, pop up a notification and vibrate to inform them that
     * their device will no longer be receiving Concur app updates until they get on Android 4.1 or higher.
     */
    private void showMinSDKIncreaseMessage() {
        View minSdkUpgradeMessage = findViewById(R.id.minSdkUpgradeMessage);

        // Show the message
        minSdkUpgradeMessage.setVisibility(View.VISIBLE);

        // Set up listener to go to message center via text link click
        TextView linkToMessageCenter = (TextView) minSdkUpgradeMessage.findViewById(R.id.minSdkTextLinkToMessageCenter);
        linkToMessageCenter.setPaintFlags(linkToMessageCenter.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        linkToMessageCenter.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(Home.this, MessageCenter.class);
                startActivity(intent);

                findViewById(R.id.minSdkUpgradeMessage).setVisibility(View.GONE);
                Preferences.setShownMinSDKIncreaseMessage(true);
            }
        });

        // Close the view via the "x"/close button.
        View cancelButton = minSdkUpgradeMessage.findViewById(R.id.minSdkMessageClose);
        cancelButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                findViewById(R.id.minSdkUpgradeMessage).setVisibility(View.GONE);
                Preferences.setShownMinSDKIncreaseMessage(true);
            }
        });
    }

}

/**
 * An extension of <code>DefaultSimpleNavigation</code> with an ID.
 */
class HomeScreenSimpleNavigationItem extends DefaultSimpleNavigationItem {

    Runnable run;

    HomeScreenSimpleNavigationItem(int id, int layoutResId, int textResId, int iconResId, int iconVisibility,
                                   int navItemVisibility, Runnable run) {
        super(id, layoutResId, textResId, iconResId, iconVisibility, navItemVisibility);
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