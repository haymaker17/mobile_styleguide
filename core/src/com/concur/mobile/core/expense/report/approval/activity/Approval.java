/**
 * 
 */
package com.concur.mobile.core.expense.report.approval.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.ui.UIUtils;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.data.CountSummary;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.report.activity.ActiveReportListItem;
import com.concur.mobile.core.expense.report.activity.ExpenseEntries;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ReportComparator;
import com.concur.mobile.core.expense.report.service.ReportDetailRequest;
import com.concur.mobile.core.expense.service.CountSummaryRequest;
import com.concur.mobile.core.fragment.RetainerFragment;
import com.concur.mobile.core.invoice.activity.InvoicesWebView;
import com.concur.mobile.core.invoice.activity.PurchaseRequestsWebView;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.activity.SegmentList;
import com.concur.mobile.core.travel.approval.activity.ApprovalTripListItem;
import com.concur.mobile.core.travel.data.TripToApprove;
import com.concur.mobile.core.travel.request.activity.TravelRequestApprovalsWebView;
import com.concur.mobile.core.travel.service.GetTripsToApprove;
import com.concur.mobile.core.travel.service.ItineraryRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.SortOrder;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.HeaderListItem;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;

/**
 * Provides an activity to display expense reports for approval.
 * 
 * @author AndrewK
 */
@SuppressLint("NewApi")
public class Approval extends BaseActivity {

    static final String CLS_TAG = Approval.class.getSimpleName();

    private static final String REPORT_DETAIL_RECEIVER_KEY = "report.detail.receiver";
    /** list item type for report approval */
    private static final int APPROVAL_REPORT_LIST_ITEM_VIEW_TYPE = 0;
    /** list item type for header */
    private static final int HEADER_VIEW_TYPE = 1;
    /** list item type for approval count i.e. invoice and travel request */
    private static final int APPROVAL_COUNT_LIST_ITEM = 2;
    /** list item type for trip list approval */
    private static final int APPROVAL_TRIP_LIST_ITEM_VIEW_TYPE = 3;

    // This must be kept in sync with total possible view types
    private static final int MAXIMUM_LIST_ITEM_VIEW_TYPE_COUNT = 4;

    /**
     * Indicates if the tips overlay is currently showing.
     */
    protected boolean isTipsOverlayVisible = false;

    /**
     * The time, in nanoseconds, this activity has been started.
     */
    protected long startTime = 0L;

    /**
     * The time, in seconds, this activity has been active (showing).
     */
    protected long upTime = 0L;

    private CountSummaryRequest countSummaryRequest;

    protected int inProgressRef = 0;
    /**
     * Contains a reference to an <code>IntentFilter</code> used to register a receiver to handle data update events.
     */
    protected IntentFilter filter;

    // Contains the intent filter used to register the data receiver.
    private final IntentFilter dataReceiverFilter = new IntentFilter();

    // Contains whether our data receiver has been registered.
    private boolean dataReceiverRegistered = false;

    /**
     * Contains a reference to a broadcast receiver to handle data update notifications.
     */
    protected final BroadcastReceiver broadcastReceiver = new DataUpdateReceiver();

    /**
     * Contains a reference to an outstanding request to refresh report data.
     */
    protected ServiceRequest dataUpdateRequest;

    /**
     * Contains whether a broadcast receiver is currently registered.
     */
    protected boolean broadcastReceiverRegistered;

    /**
     * Contains a reference to the dialog displayed when no local reports exist within the local cache and a request has been
     * passed to the service.
     */
    protected Dialog dataRetrieveDlg;

    /**
     * Contains a reference to the expense report list adapter.
     */
    protected ListItemAdapter<ListItem> listItemAdapter;

    // A reference to the get report detail request.
    private ReportDetailRequest reportDetailRequest;

    /**
     * Contains a reference to a report detail receiver.
     */
    private ReportDetailReceiver reportDetailReceiver;

    /**
     * Contains a reference to a filter used to register a report detail receiver.
     */
    private IntentFilter reportDetailFilter;

    // protected Bundle lastSavedInstanceState;

    protected BaseAsyncResultReceiver tripsToApproveReceiver;

    // Contains the receiver used to handle the results of an itinerary request.
    private ItineraryReceiver itineraryReceiver;
    // Contains the filter used to register the itinerary receiver.
    private IntentFilter itineraryFilter;
    // Contains a reference to the currently outstanding itinerary request.
    private ItineraryRequest itineraryRequest;

    private boolean isTravelApprover;

    // The one RetainerFragment used to hold objects between activity recreates
    protected RetainerFragment retainer;

    protected boolean fromNotification;
    private static final String GET_TRIPS_TO_APPROVE_RECEIVER = "trips.to.approve.receiver.token";

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            upTime = savedInstanceState.getLong(Const.ACTIVITY_STATE_UPTIME, 0L);
        }

        if (isTravelApprover()) {

            if (retainer != null) {
                tripsToApproveReceiver = (BaseAsyncResultReceiver) retainer.get(GET_TRIPS_TO_APPROVE_RECEIVER);
                if (tripsToApproveReceiver != null) {
                    tripsToApproveReceiver.setListener(new TripsToApproveListener());
                }
            }
        }

        // Construct and populate map from view state to child index.
        viewStateFlipChild = new HashMap<ViewState, Integer>();
        viewStateFlipChild.put(ViewState.LOCAL_DATA, 0);
        viewStateFlipChild.put(ViewState.NO_DATA, 2);
        viewStateFlipChild.put(ViewState.RESTORE_APP_STATE, 1);
        // The last two states here map to the same view.
        viewStateFlipChild.put(ViewState.NO_LOCAL_DATA_REFRESH, 3);
        viewStateFlipChild.put(ViewState.LOCAL_DATA_REFRESH, 3);

        // Init to local data.
        viewState = ViewState.LOCAL_DATA;

        filter = getDataUpdateIntentFilter();
        registerReceiver(broadcastReceiver, filter);
        broadcastReceiverRegistered = true;

        // Restore any receivers.
        restoreReceivers();

        // Init report detail filter.
        reportDetailFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_DETAIL_UPDATED);

        setContentView(getContentViewLayoutId());

        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        if (viewFlipper != null) {
            // Animation anim = AnimationUtils.loadAnimation(this,
            // R.anim.fade_out);
            // anim.setDuration(400L);
            // viewFlipper.setOutAnimation(anim);
        }

        // Set the title header.
        TextView txtView = (TextView) findViewById(R.id.header_navigation_bar_title);
        if (txtView != null) {
            txtView.setText(getTitleHeaderTextResId());
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate title header text view!");
        }
        // Set the title header.
        getSupportActionBar().setTitle(getTitleHeaderTextResId());
        Bundle extras = getIntent().getExtras();
        final ConcurCore app = (ConcurCore) getApplication();
        if (extras != null && extras.containsKey(ConcurCore.FROM_NOTIFICATION)) {
            fromNotification = extras.getBoolean(ConcurCore.FROM_NOTIFICATION, false);
            if (fromNotification) {
                // app.getStartUpAct(this);
                new AsyncTask<Void, Void, Boolean>() {

                    @Override
                    protected void onPreExecute() {
                        viewState = ViewState.NO_LOCAL_DATA_REFRESH;
                        flipViewForViewState();
                    };

                    @Override
                    protected Boolean doInBackground(Void... params) {
                        boolean returnValue = app.isSessionAvailable();
                        return returnValue;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        onHandleSuccess(result, app, savedInstanceState);
                    };

                }.execute();

                // Flurry Notification
                if (extras.containsKey(Flurry.EXTRA_FLURRY_CATEGORY)) {
                    Map<String, String> params = new HashMap<String, String>();
                    if (extras.containsKey(Flurry.EXTRA_FLURRY_ACTION_PARAM_VALUE)) {
                        params.put(Flurry.PARAM_NAME_ACTION, extras.getString(Flurry.EXTRA_FLURRY_ACTION_PARAM_VALUE));
                    }
                    EventTracker.INSTANCE.track(extras.getString(Flurry.EXTRA_FLURRY_CATEGORY),
                            Flurry.EVENT_NAME_ACTION, params);
                }
            } else {
                configureView(savedInstanceState);
            }
        } else {
            configureView(savedInstanceState);
        }

        // Show Test Drive Overlay Tips
        boolean isTestDriveUser = Preferences.isTestDriveUser();
        if (isTestDriveUser && Preferences.shouldShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_APPROVAL)) {
            showTestDriveTips();
        }

    }

    protected void onHandleSuccess(Boolean result, ConcurCore app, Bundle savedInstanceState) {
        if (result != null && result == Boolean.FALSE) {
            app.launchStartUpActivity(Approval.this);
        } else {
            configureView(savedInstanceState);
        }
    }

    private void configureView(Bundle savedInstanceState) {
        // Is the service immediately available for us to retrieve data?
        if (isServiceAvailable()) {
            buildView();
        } else {
            buildViewDelay = true;
            viewState = ViewState.RESTORE_APP_STATE;
            flipViewForViewState();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ConcurCore.isConnected()) {
            // For Trip Approvals..
            if (isTravelApprover()) {
                tripsToApproveReceiver = new BaseAsyncResultReceiver(new Handler());
                tripsToApproveReceiver.setListener(new TripsToApproveListener());
                Log.d(Const.LOG_TAG, CLS_TAG + " increment from onStrat");
                incrInProgressRef();
                new GetTripsToApprove(getApplicationContext(), 1, tripsToApproveReceiver).execute();
            }
            // end
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        ConcurCore app = (ConcurCore) getApplication();
        // MOB-21304 Do not call app.isSessionAvailable(), which can block UI threads
        restoreReceivers();

        // Re-register the data receiver, if need be.
        if (!dataReceiverRegistered) {
            registerReceiver(countDataReceiver, dataReceiverFilter);
            dataReceiverRegistered = true;
        }

        // Set up the broadcast receiver if needbe.
        if (!broadcastReceiverRegistered) {
            registerReceiver(broadcastReceiver, filter);
            broadcastReceiverRegistered = true;
        }
        // If the 'onResume' is for approvals, then check for data
        // freshness.
        // TODO: Examine whether we can generalize the update logic in
        // ExpenseActiveReports.onResume with the code in
        // 'checkForRefreshData'.
        if (this.getClass().equals(Approval.class)) {
            if (isServiceAvailable() && dataUpdateRequest == null) {
                // Check and request new data, if needbe.
                checkForRefreshData(false);
            }
        }
        if (isTravelApprover && (retainer != null) && retainer.contains(GET_TRIPS_TO_APPROVE_RECEIVER)) {
            tripsToApproveReceiver = (BaseAsyncResultReceiver) retainer.get(GET_TRIPS_TO_APPROVE_RECEIVER);
            tripsToApproveReceiver.setListener(new TripsToApproveListener());
        }

        if (isTipsOverlayVisible) {
            startTime = System.nanoTime();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(getMenuLayout(), menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean retVal = false;
        final int itemId = menuItem.getItemId();
        if (itemId == R.id.refresh) {
            refreshApprovalList();
            refreshTripsToApproveList();
            retVal = true;
        }
        return retVal;
    }

    /**
     * Refresh approval List.
     * */
    private void refreshApprovalList() {
        if (isServiceAvailable()) {
            if (ConcurCore.isConnected()) {
                if (dataUpdateRequest == null) {
                    // Send the data update request.
                    ConcurCore ConcurCore = (ConcurCore) getApplication();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore
                            .getApplicationContext());
                    sendDataUpdateRequest(prefs.getString(Const.PREF_USER_ID, null));
                }
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        }
    }

    /**
     * Refresh trips for approval list
     */
    private void refreshTripsToApproveList() {
        if (isTravelApprover()) {
            if (isServiceAvailable() && ConcurCore.isConnected()) {
                tripsToApproveReceiver = new BaseAsyncResultReceiver(new Handler());
                tripsToApproveReceiver.setListener(new TripsToApproveListener());
                Log.d(Const.LOG_TAG, CLS_TAG + " calling increment from refresgTripsToApproveList");
                incrInProgressRef();
                new GetTripsToApprove(getApplicationContext(), 1, tripsToApproveReceiver).execute();
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        }
    }

    protected void incrInProgressRef() {
        if (++inProgressRef > 0) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".incrInProgressRef=" + inProgressRef);
        }
    }

    protected void decrInProgressRef() {
        if (--inProgressRef < 1) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".decrInProgressRef=" + inProgressRef);
        }
    }

    protected void clearInProgressRef() {
        inProgressRef = 0;
        ViewUtil.setNetworkActivityIndicatorVisibility(this, View.INVISIBLE, null);
        setViewVisible(R.id.action_refresh);
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
     * Returns the menu layout for this View.
     */
    protected int getMenuLayout() {
        return R.menu.reports;
    }

    protected void buildView() {

        Intent launchIntent = getIntent();
        boolean tripListUpdated = false;// TODO

        boolean reportListUpdated = false;
        if (launchIntent.getExtras() != null) {
            reportListUpdated = launchIntent.getExtras().getBoolean(getPendingDataRetrievalExtraKey(), false);
        }

        // Initialize the view.
        initView();

        // Only send a request to retrieve data if there is no pending request.
        if (!reportListUpdated || (!tripListUpdated && isTravelApprover)) {
            // Check for an orientation change.
            if (!orientationChange) {
                if (dataUpdateRequest == null) {
                    checkForRefreshData(true);
                }
            } else {
                // Clear the orientation change flag.
                orientationChange = false;
            }
        } else {
            // Clear the "request pending" flag.
            launchIntent.putExtra(getPendingDataRetrievalExtraKey(), false);
        }

        // Check if we need to prompt to rate
        boolean shouldCheck = launchIntent.getBooleanExtra(Const.EXTRA_CHECK_PROMPT_TO_RATE, false);
        if (shouldCheck) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
            if (Preferences.shouldPromptToRate(prefs, false)) {
                showDialog(Const.DIALOG_PROMPT_TO_RATE);
            }
        }
    }

    /**
     * If this user is a test drive user, this will get called the first time the user loads the Approvals screen to show the test
     * drive tips overlay
     */
    protected void showTestDriveTips() {
        // OnClickListener whose onClick() is called from setupOverlay()
        OnClickListener dismissListener = new OnClickListener() {

            public void onClick(View v) {
                Preferences.setShouldNotShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_APPROVAL);
                isTipsOverlayVisible = false;

                // Analytics stuff.
                Map<String, String> flurryParams = new HashMap<String, String>();
                upTime = ((System.nanoTime() - startTime) / 1000000000L) + upTime; // Convert nanoseconds to seconds.
                flurryParams.put(Flurry.PARAM_NAME_SECONDS_ON_OVERLAY, Flurry.formatDurationEventParam(upTime));
                EventTracker.INSTANCE.track(Flurry.CATEGORY_OVERLAYS, "Approvals", flurryParams);
            }
        };

        UIUtils.setupOverlay((ViewGroup) getWindow().getDecorView(), R.layout.td_overlay_approval, dismissListener,
                R.id.td_icon_cancel_button, this, R.anim.fade_out, 300L);

        isTipsOverlayVisible = true;
    }

    /**
     * Will initiate refreshing the data if need be.
     * 
     * @param showNoConnectivityDialog
     *            whether to show the "no connectivity" dialog if the client is not connected.
     */
    private void checkForRefreshData(boolean showNoConnectivityDialog) {
        IExpenseReportCache expRepCache = getExpenseReportCache();
        if (expRepCache != null) {
            // Check for no list, an expired one or the flag being set
            // indicating the list should be explicitly
            // re-fetched (due to end-user interaction).
            boolean hasReportInfo = expRepCache.hasLastReportList();
            boolean shouldUpdateReports = (!hasReportInfo
                    || expRepCache.isLastReportListUpdateExpired(Const.CACHE_DATA_EXPIRATION_DURATION_MILLISECONDS) || expRepCache
                    .shouldRefetchReportList());
            // boolean shouldUpdateTrips = true; // TODO check the update from
            // the mem cache
            // boolean hasTrips = false; // TODO check the update from the mem
            // cache
            // if (shouldUpdateReports || shouldUpdateTrips) {
            if (shouldUpdateReports) {
                if (ConcurCore.isConnected()) {
                    // Send a request for an initial/updated list of reports or
                    // trips.
                    ConcurCore ConcurCore = (ConcurCore) getApplication();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore
                            .getApplicationContext());
                    sendDataUpdateRequest(prefs.getString(Const.PREF_USER_ID, null));

                    if (shouldUpdateReports) {// Clear refetch report list.
                        expRepCache.clearShouldRefetchReportList();
                    }
                } else {
                    // If there is no cached data, then present a dialog
                    // indicating the client
                    // is offline.
                    // if ((!hasReportInfo || !hasTrips) &&
                    // showNoConnectivityDialog) {
                    if ((!hasReportInfo) && showNoConnectivityDialog) {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                }
            } else {
                List<ExpenseReport> expRepList = expRepCache.getReportList();
                boolean hasReportData = (isExpenseApprover() && hasReportInfo && (expRepList != null && expRepList
                        .size() > 0));

                // trip approvals
                boolean hasTripsData = hasTripsToApprove();

                if (!hasReportData && !hasTripsData && (!isInvoiceApprover() && !isInvoiceUser() && !isTRApprover())) {
                    // Flip to view indicating no data currently exists.
                    // TODO MOB-13000
                    viewState = ViewState.NO_DATA;
                    flipViewForViewState();
                } else {
                    updateListUI();
                }
            }
        } else {
            viewState = ViewState.NO_DATA;
            flipViewForViewState();
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: expRepCache is null!");
        }
    }

    /**
     * if travel approver then retrieves trips for approval
     * 
     * @return
     */
    private boolean hasTripsToApprove() {
        if (isTravelApprover) {
            List<TripToApprove> tripsToApprove = getTripsToApprove();
            return ((tripsToApprove != null && tripsToApprove.size() > 0) ? true : false);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onServiceAvailable()
     */
    @Override
    protected void onServiceAvailable() {
        if (buildViewDelay) {
            buildView();
            buildViewDelay = false;
        }
    }

    private void initView() {
        // TODO MOB-13000
        updateListUI();

        // Show/Hide the screen title action button.
        ImageView imgView = (ImageView) findViewById(R.id.action_refresh);
        if (imgView != null) {

            if (showTitleBarActionButton()) {
                imgView.setVisibility(View.VISIBLE);
                imgView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        refreshApprovalList();
                    }
                });
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".populateExpenseHeaderNavBarInfo: unable to locate action button image view!");
        }

    }

    /**
     * Gets whether or not the title bar should show the action button.
     * 
     * @return whether the title bar should show the action button.
     */
    protected boolean showTitleBarActionButton() {
        return true;
    }

    /**
     * Handles when the end-user has selected the add action button.
     */
    protected void onAddActionButton() {
        // No-op.
    }

    /**
     * Will launch viewing an expense report.
     * 
     * @param expenseReport
     *            the expense report to view.
     */
    protected void onViewExpenseReport(ExpenseReport expenseReport) {
        Intent intent = new Intent(Approval.this, ExpenseEntries.class);
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expenseReport.reportKey);
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, getExpenseReportSource());
        // Is a detail report required for this selection?
        if (isDetailedReportRequired()) {
            IExpenseReportCache expRepCache = getExpenseReportCache();
            boolean reportDetailExpired = expRepCache.isLastDetailReportUpdateExpired(expenseReport.reportKey,
                    Const.CACHE_DATA_EXPIRATION_DURATION_MILLISECONDS);
            boolean hasReportDetail = expRepCache.hasReportDetail(expenseReport.reportKey);
            if (hasReportDetail && !reportDetailExpired) {
                addExtraIntentData(intent, expenseReport);
                startActivity(intent);
            } else {
                if (ConcurCore.isConnected()) {
                    // Flurry Notification
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_REPORT_TO_APPROVE);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_APPROVALS, Flurry.EVENT_NAME_ACTION, params);

                    // Launch a request to obtain a detailed report.
                    ConcurCore ConcurCore = (ConcurCore) getApplication();
                    ConcurService concurService = ConcurCore.getService();
                    registerReportDetailReceiver();
                    reportDetailReceiver.setLaunchIntent(intent);
                    if (ViewUtil.shouldFetchDetailSummaryReport(expenseReport)) {
                        reportDetailRequest = concurService.sendReportDetailSummaryRequest(expenseReport.reportKey,
                                getExpenseReportSource());
                    } else {
                        reportDetailRequest = concurService.sendReportDetailRequest(expenseReport.reportKey,
                                getExpenseReportSource());
                    }
                    if (reportDetailRequest == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".configureReportEntries.onItemClick: unable to create report detail request!");
                        unregisterReportDetailReceiver();
                    } else {
                        showDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL);
                        // Set the request on the receiver.
                        reportDetailReceiver.setRequest(reportDetailRequest);
                    }
                } else {
                    if (!hasReportDetail) {
                        // Show the dialog indicating there is no connectivity
                        // at this time.
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    } else {
                        // No connectivity, but the client has a cached and
                        // expired detail report.
                        // Still permit the end-user to view the detailed
                        // report.
                        startActivity(intent);
                    }
                }
            }
        } else {
            addExtraIntentData(intent, expenseReport);
            startActivity(intent);
        }
    }

    /**
     * Will launch viewing a trip needing approval.
     * 
     * @param tripToApprove
     *            trip to view.
     */
    protected void onViewTripToApprove(TripToApprove tripToApprove) {
        if (ConcurCore.isConnected()) {
            // Launch a request to obtain the itinerary of the trip to approve
            sendItineraryRequest(tripToApprove.getItinLocator(), tripToApprove.getTravelerCompanyId(),
                    tripToApprove.getTravelerUserId());
        }
    }

    /**
     * Will send a request to obtain an itinerary.
     */
    protected void sendItineraryRequest(String itinLocator, String travelerCompanyId, String travelerUserId) {
        // @REF: MOB-11304
        if (ConcurCore.isConnected()) {
            ConcurService concurService = getConcurService();
            registerItineraryReceiver();
            itineraryRequest = concurService.sendItineraryRequest(itinLocator, travelerCompanyId, travelerUserId, true);
            if (itineraryRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendItineraryRequest: unable to create itinerary request.");
                unregisterItineraryReceiver();
            } else {
                // Set the request object on the receiver.
                itineraryReceiver.setServiceRequest(itineraryRequest);
                // Show the dialog.
                showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
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
     * onHandleSuccessItinerary
     * 
     * @param activity
     * @param intent
     * @param activity
     * */
    protected void onHandleSuccessItinerary(Intent intent) {
        String itinLocator = intent.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
        if (itinLocator != null) {
            Intent i = new Intent(this, SegmentList.class);
            i.putExtra(Const.EXTRA_ITIN_LOCATOR, itinLocator);
            List<TripToApprove> tripsToApprove = getTripsToApprove();
            for (TripToApprove tripToApprove : tripsToApprove) {
                if (tripToApprove.getItinLocator().equals(itinLocator)) {
                    i.putExtra(Const.EXTRA_IS_FOR_TRIP_APPROVAL, true);
                    i.putExtra(Const.EXTRA_TRAVELLER_NAME, tripToApprove.getTravelerName());
                    i.putExtra(Const.EXTRA_TRIP_ID, tripToApprove.getItinLocator());
                    i.putExtra(Const.EXTRA_TRIP_NAME, tripToApprove.getTripName());

                    // format the amount
                    String amount = FormatUtil.formatAmount(tripToApprove.getTotalTripCost(), getResources()
                            .getConfiguration().locale, tripToApprove.getTotalTripCostCrnCode(), true, true);
                    i.putExtra(Const.EXTRA_TOTAL_TRIP_COST, amount);

                    // format the approve by date message
                    String approveDateDisplayStr = "";
                    if (DateFormat.is24HourFormat(this)) {
                        approveDateDisplayStr = FormatUtil.SHORT_WEEKDAY_SHORT_MONTH_DAY_FULL_YEAR_24HOUR_TIMEZONE_DISPLAY_LOCAL
                                .format(tripToApprove.getApproveByDate().getTime());
                    } else {
                        approveDateDisplayStr = FormatUtil.SHORT_WEEKDAY_SHORT_MONTH_DAY_FULL_YEAR_12HOUR_TIMEZONE_DISPLAY_LOCAL
                                .format(tripToApprove.getApproveByDate().getTime());
                    }
                    String approvalMsg = Format.localizeText(this, R.string.trip_approve_by,
                            new Object[] { approveDateDisplayStr });
                    i.putExtra(Const.EXTRA_TRIP_APPROVAL_MESSAGE, approvalMsg);

                    i.putExtra(Const.EXTRA_TRAVELLER_USER_ID, tripToApprove.getTravelerUserId());
                    i.putExtra(Const.EXTRA_TRAVELLER_COMPANY_ID, tripToApprove.getTravelerCompanyId());
                    break;
                }
            }
            startActivity(i);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: itin locator has invalid value!");
        }

    }

    /**
     * Configures a list adapter to populate the list with expense reports, invoices, travel requests and trips for approval.
     */
    private void configureListEntries() {
        // Use the cached data to immediately display a list
        listItemAdapter = new ListItemAdapter<ListItem>(this, getListItems(), MAXIMUM_LIST_ITEM_VIEW_TYPE_COUNT);
        ListView listView = (ListView) viewFlipper.getCurrentView().findViewById(android.R.id.list);
        if (listView != null) {
            registerForContextMenu(listView);
            listView.setAdapter(listItemAdapter);
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Construct an intent object to launch the ExpenseEntries
                    // activity with
                    // the report key.
                    ListItem listItem = listItemAdapter.getItem(position);
                    if ((listItem instanceof ApprovalReportListItem) || (listItem instanceof ActiveReportListItem)) {
                        ExpenseReport expenseReport = null;
                        if (listItem instanceof ApprovalReportListItem) {
                            expenseReport = ((ApprovalReportListItem) listItem).report;
                        }
                        if (listItem instanceof ActiveReportListItem) {
                            expenseReport = ((ActiveReportListItem) listItem).report;
                        }

                        onViewExpenseReport(expenseReport);
                    } else if ((listItem instanceof ApprovalTripListItem)) {
                        TripToApprove tripToApprove = ((ApprovalTripListItem) listItem).getTripToApprove();
                        // check if there is a trip to approve available as the
                        // same list item is used to display 'no trips
                        // available message'
                        if (tripToApprove != null) {
                            onViewTripToApprove(tripToApprove);
                        }
                    } else if (listItem instanceof ApprovalCountListItem) {
                        Intent it = ((ApprovalCountListItem) listItem).getIntent();
                        String approvalType = ((ApprovalCountListItem) listItem).getApprovalType();
                        if (approvalType.equalsIgnoreCase(Flurry.PARAM_VALUE_VIEW_TRAVEL_REQUESTS)) {
                            // Flurry Notification
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_VIEW_TRAVEL_REQUESTS);
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_APPROVALS, Flurry.EVENT_NAME_ACTION, params);
                        } else if (approvalType.equalsIgnoreCase(Flurry.PARAM_VALUE_VIEW_INVOICES)) {
                            // Flurry Notification
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_VIEW_INVOICES);
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_APPROVALS, Flurry.EVENT_NAME_ACTION, params);
                        }
                        startActivity(it);
                    } else {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG
                                        + ".configureReportEntries.onItemClick: select list item is not of type Approval Count report list item!");
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureReportEntries: no list view found!");
        }
    }

    /**
     * Gets the appropriate instance of <code>IExpenseReportCache</code> for this activity.
     * 
     * @return the appropriate instance of <code>IExpenseReportCache</code> for this activity.
     */
    protected IExpenseReportCache getExpenseReportCache() {
        ConcurCore ConcurCore = (ConcurCore) getApplication();
        return ConcurCore.getExpenseApprovalCache();
    }

    /**
     * Gets whether a detail report is required upon selection.
     * 
     * @return whether a detail report is required upon selection.
     */
    protected boolean isDetailedReportRequired() {
        return true;
    }

    /**
     * Gets whether this report list is for approvals.
     * 
     * @return
     */
    protected boolean isApprovalList() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = super.onCreateDialog(id);
        switch (id) {
        case Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_retrieving_exp_detail));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (reportDetailRequest != null) {
                        // Cancel the request.
                        reportDetailRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: reportDetailRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY: {
            dialog.setOnCancelListener(new OnCancelListener() {

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
        }
        return dialog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case Const.DIALOG_EXPENSE_ACTIVE_REPORT_RETRIEVE_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case Const.DIALOG_EXPENSE_APPROVAL_RETRIEVE_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case Const.DIALOG_EXPENSE_REPORT_DETAIL_RETRIEVE_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and stop any outstanding
        // request
        // for a data update request.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (dataUpdateRequest != null) {
                dataUpdateRequest.cancel();
            }
            if (reportDetailRequest != null) {
                reportDetailRequest.cancel();
            }
            if (itineraryRequest != null) {
                itineraryRequest.cancel();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {

        super.onPause();

        if (broadcastReceiverRegistered) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiverRegistered = false;
        }

        if (reportDetailReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            reportDetailReceiver.setActivity(null);
            retainer.put(REPORT_DETAIL_RECEIVER_KEY, reportDetailReceiver);
        }

        if (dataReceiverRegistered) {
            unregisterReceiver(countDataReceiver);
            dataReceiverRegistered = false;
        }

        if (retainer != null && tripsToApproveReceiver != null) {
            tripsToApproveReceiver.setListener(null);
            retainer.put(GET_TRIPS_TO_APPROVE_RECEIVER, tripsToApproveReceiver);
        }

        if (isTipsOverlayVisible) {
            // Save the time the user spent on this screen, but
            // perhaps put the app in the background.
            upTime += (System.nanoTime() - startTime) / 1000000000L; // Convert to seconds.
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (isTipsOverlayVisible) {
            // Save the uptime so we know how long the user has been on this screen,
            // even if it has been destroyed.
            outState.putLong(Const.ACTIVITY_STATE_UPTIME, upTime);
        }
    }

    protected void restoreReceivers() {
        // Check for outstanding 'ReportDetailReceiver'.
        if (retainer != null && retainer.contains(REPORT_DETAIL_RECEIVER_KEY)) {
            reportDetailReceiver = (ReportDetailReceiver) retainer.get(REPORT_DETAIL_RECEIVER_KEY);
            // Reset the activity reference.
            reportDetailReceiver.setActivity(this);
        }
    }

    /**
     * Gets the resource id of the text string to be displayed if there exists no data to be presented in this activity.
     * 
     * @return the resource id of the text string to be displayed if there exists no data to be presented in this activity.
     */
    @Override
    protected int getNoDataTextResourceId() {
        return R.string.no_approvals;
    }

    /**
     * Gets the resource id of the text string to be displayed if there exists no local data but with an outstanding request to
     * retrieve data.
     * 
     * @return the resource id of the text string to be displayed if there exists no local data, but with an outstanding request
     *         to retrieve data.
     */
    @Override
    protected int getNoLocalDataRefreshTextResourceId() {
        return R.string.no_local_data_server_refresh;
    }

    /**
     * Will add any extra data to an intent appropriate to launching the 'ExpenseEntries' activity.
     * 
     * @param intent
     *            the intent on which extra data will be provided.
     * @param expRep
     *            a selected expense report.
     */
    protected void addExtraIntentData(Intent intent, ExpenseReport expRep) {
    }

    /**
     * Gets the list of approval reports.
     * 
     * @return the list of approval reports.
     */
    protected List<ExpenseReport> getReports() {
        IExpenseReportCache expAppCache = ((ConcurCore) getApplication()).getExpenseApprovalCache();
        List<ExpenseReport> reports = expAppCache.getReportList();
        return reports;
    }

    /**
     * 
     * @return list of TripApprove objects or an empty list
     */
    protected List<TripToApprove> getTripsToApprove() {
        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        // TODO set this into a class member and then return it rather that
        // getting from context every time
        return core.getTripsToApprove();
    }

    /**
     * Gets the list of expense reports, invoices, travel requests and trips for approval, to be displayed.
     * 
     * @return the list of expense reports, invoices, travel requests and trips for approval.
     */
    protected List<ListItem> getListItems() {
        List<ListItem> listItems = new ArrayList<ListItem>();

        // TODO MOB-13000/MOB-13286
        if (isTRApprover()) {
            // Approval Counts.
            int count = getTravelRequestApprovalCount();
            // TR Request
            String header = getText(R.string.approval_row_tr_header).toString();
            listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
            String trMsg = getResources().getQuantityString(R.plurals.approval_row_tr_msg, count, count);

            Intent it = new Intent(Approval.this, TravelRequestApprovalsWebView.class);
            listItems.add(new ApprovalCountListItem(trMsg, APPROVAL_COUNT_LIST_ITEM, it,
                    Flurry.PARAM_VALUE_VIEW_TRAVEL_REQUESTS));
        }

        // TODO MOB-13000/MOB-13286
        if (isInvoiceApprover() || isInvoiceUser()) {

            // Add Invoice Header
            String header = getText(R.string.approval_row_invoice_header).toString();
            listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));

            // Row with count
            int count = getInvoicesCount();
            String invoiceCountMsg = getResources().getQuantityString(R.plurals.approval_row_invoice_msg, count, count);

            Intent it = new Intent(Approval.this, InvoicesWebView.class);
            listItems.add(new ApprovalCountListItem(invoiceCountMsg, APPROVAL_COUNT_LIST_ITEM, it,
                    Flurry.PARAM_VALUE_VIEW_INVOICES));
        }

        // MOB-16925
        if (ViewUtil.isPurchaseRequestApprover(this)) {

            // Header
            String header = getText(R.string.approval_row_purchase_requests_header).toString();
            listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
            Resources res = getResources();

            // Row with count
            int count = getPurchaseRequests();
            String prCountMsg = res.getQuantityString(R.plurals.approval_row_purchase_request_count_msg, count, count);
            Intent it = new Intent(Approval.this, PurchaseRequestsWebView.class);
            listItems.add(new ApprovalCountListItem(prCountMsg, APPROVAL_COUNT_LIST_ITEM, it,
                    Flurry.PARAM_VALUE_VIEW_PURCHASE_REQUESTS));
        }

        if (isTravelApprover) {
            List<TripToApprove> tripsToApprove = getTripsToApprove();
            listItems.add(new HeaderListItem(getText(R.string.approval_row_trip_header).toString(), HEADER_VIEW_TYPE));

            if (tripsToApprove == null || tripsToApprove.size() == 0) {
                // check to see if error occurred while retrieving the trips for
                // approval list from server
                MWSResponseStatus reqStatus = ((ConcurCore) ConcurCore.getContext()).getRequestTaskStatus();
                String listItemMsg = "";
                if (reqStatus != null) {
                    if (reqStatus.isSuccess()) {
                        listItemMsg = getText(R.string.trip_approval_list_msg_zero).toString();
                        Log.e(Const.LOG_TAG, "No trips for approval in Approval.getListItems");
                    } else {
                        listItemMsg = getText(R.string.trip_approval_list_failed_msg).toString();
                        Log.e(Const.LOG_TAG,
                                "Error occured in retrieving trips for approval in Approval.getListItems -"
                                        + (reqStatus.getResponseMessage()));
                    }
                } else {
                    listItemMsg = getText(R.string.trip_approval_list_failed_msg).toString();
                    Log.e(Const.LOG_TAG,
                            "Error occured in retrieving trips for approval in Approval.getListItems -MWSResponseStatus is null ");
                }
                listItems.add(new ApprovalTripListItem(listItemMsg, APPROVAL_TRIP_LIST_ITEM_VIEW_TYPE));
            } else if (tripsToApprove.size() > 0) {
                for (TripToApprove tripToApprove : tripsToApprove) {
                    listItems.add(new ApprovalTripListItem(tripToApprove, APPROVAL_TRIP_LIST_ITEM_VIEW_TYPE));
                }
            }
        }

        // TODO MOB-13000
        if (isExpenseApprover()) {
            List<ExpenseReport> reports = getReports();

            listItems
                    .add(new HeaderListItem(getText(R.string.approval_row_report_header).toString(), HEADER_VIEW_TYPE));
            if (reports != null && !reports.isEmpty()) {
                Collections.sort(reports, new ReportComparator(SortOrder.DESCENDING));
                for (ExpenseReport report : reports) {
                    listItems.add(new ApprovalReportListItem(report, APPROVAL_REPORT_LIST_ITEM_VIEW_TYPE));
                }
            } else {
                // Row with count
                String listItemMsg = getResources().getQuantityString(R.plurals.approval_row_report_msg, 0, 0);
                listItems.add(new ApprovalTripListItem(listItemMsg, APPROVAL_REPORT_LIST_ITEM_VIEW_TYPE));
            }
        }

        return listItems;
    }

    /**
     * Gets the last data update time.
     * 
     * @return the last data update time.
     */
    protected Calendar getLastDataUpdateTime() {
        Calendar lastDataUpdateTime;
        IExpenseReportCache expAppCache = ((ConcurCore) getApplication()).getExpenseApprovalCache();
        lastDataUpdateTime = expAppCache.getLastReportListUpdateTime();
        return lastDataUpdateTime;
    }

    /**
     * Returns the number of Travel Requests to approve, or 0 if there are none.
     * 
     * @return the number of Travel Requests to approve, or 0 if there are none.
     */
    protected int getTravelRequestApprovalCount() {

        ConcurCore concurMobile = (ConcurCore) getApplication();
        CountSummary summary = concurMobile.getSummary();

        if (summary != null) {
            return summary.travelRequestsToApprove;
        }

        return 0;
    }

    /**
     * Returns the number of Invoices to approve and submit, or 0 if there are none.
     * 
     * @return the number of Invoices to approve and submit, or 0 if there are none.
     */
    protected int getInvoicesCount() {

        ConcurCore concurMobile = (ConcurCore) getApplication();
        CountSummary summary = concurMobile.getSummary();

        if (summary != null) {
            return summary.invoicesToApprove + summary.invoicesToSubmit;
        }

        return 0;
    }

    /**
     * Returns the number of Purchase Requests to approve and submit, or 0 if there are none.
     * 
     * @return the number of Purchase Requests to approve and submit, or 0 if there are none.
     */
    protected int getPurchaseRequests() {

        ConcurCore concurMobile = (ConcurCore) getApplication();
        CountSummary summary = concurMobile.getSummary();

        if (summary != null) {
            return summary.purchaseRequestsToApprove;
        }

        return 0;
    }

    /**
     * Gets the intent filter that is used to receive broadcasts for data update.
     * 
     * @return the intent filter used to receive broadcasts for data update.
     */
    protected IntentFilter getDataUpdateIntentFilter() {
        return new IntentFilter(Const.ACTION_EXPENSE_APPROVAL_REPORTS_UPDATED);
    }

    /**
     * Gets the resource id of the layout used as the main content view.
     * 
     * @return the resource id of the layout used as the main content view.
     */
    protected int getContentViewLayoutId() {
        return R.layout.expense_approval;
    }

    protected int getDataLoadingTextResourceId() {
        int retVal = getRetrievingDataTextResourceId();
        List<ExpenseReport> reports = getReports();
        if ((reports != null && reports.size() > 0) || getTravelRequestApprovalCount() > 0 || getInvoicesCount() > 0) {
            retVal = getUpdatingDataTextResourceId();
        }
        return retVal;
    }

    protected int getRetrievingDataTextResourceId() {
        return R.string.retrieving_approvals;
    }

    protected int getUpdatingDataTextResourceId() {
        return R.string.updating_approvals;
    }

    /**
     * Gets one of the constants in <code>Const</code> indicating the type of data retrieving dialog that should be displayed.
     * 
     * @return the data retrieving dialog constant type, i.e., <code>Const.DIALOG_EXPENSE_RETRIEVE_EXPENSE_APPROVALS</code>.
     */
    protected int getRetrievingDataDialogType() {
        return Const.DIALOG_EXPENSE_RETRIEVE_EXPENSE_APPROVALS;
    }

    /**
     * Gets the key used in an intent to indicate a data retrieval request is pending.
     * 
     * @return the key used to indicate a data retrieval request is pending.
     */
    protected String getPendingDataRetrievalExtraKey() {
        return Const.EXTRA_EXPENSE_REPORT_TO_APPROVE_LIST_PENDING;
    }

    /**
     * Sends a request to update the data backing this display.
     * 
     * @param userId
     *            the user id associated with the request.
     */
    protected void sendDataUpdateRequest(String userId) {
        ConcurService service = getConcurService();
        if ((service != null) && (ViewUtil.isExpenseApprover(this))) {
            dataUpdateRequest = service.sendReportsToApproveRequest(userId);
            if (dataUpdateRequest != null) {
                ViewUtil.setTextViewText(this, R.id.loading_data, R.id.data_loading_text,
                        getText(getDataLoadingTextResourceId()).toString(), true);
                // Set the view state to indicate data being loaded.
                viewState = ViewState.LOCAL_DATA_REFRESH;
                flipViewForViewState();
                Log.d(Const.LOG_TAG, CLS_TAG + " calling increment from send data update request");
                incrInProgressRef();
            }
        }
    }

    /**
     * Gets the text resource id for the title header.
     * 
     * @return the title header text resource id.
     */
    protected int getTitleHeaderTextResId() {
        return R.string.expense_approval_list_title;
    }

    /**
     * Gets the layout resource id of the expense report row.
     * 
     * @param enabled
     *            whether the expense report is enabled for selection.
     * 
     * @return the layout resource id of the expense report row.
     */
    protected int getExpenseReportRowLayoutResId(boolean enabled) {
        int layoutResId = ((enabled) ? R.layout.expense_approval_row : R.layout.expense_approval_row_disabled);
        return layoutResId;
    }

    /**
     * Gets the "source" of the expense report, i.e., either from approvals or active. This method should return one of the two
     * constants, <code>Const.EXPENSE_REPORT_SOURCE_APPROVAL</code> or <code>Const.EXPENSE_REPORT_SOURCE_ACTIVE</code>.
     * 
     * @return the source id of the expense report.
     */
    protected int getExpenseReportSource() {
        return Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL;
    }

    /**
     * Gets the type of dialog to be displayed when data retrieval fails.
     * 
     * @return an integer constant, i.e. one of the constants in <code>Const.DIALOG_EXPENSE_*</code>.
     */
    protected int getDataUpdateErrorDialogType() {
        return Const.DIALOG_EXPENSE_APPROVAL_RETRIEVE_FAILED;
    }

    /**
     * Will update the list UI.
     */
    protected void updateListUI() {
        if (inProgressRef > 0) {
            return;
        }
        // If there is no local cached data, then flip to view indicating this.
        // If there are reports cached locally, then set the list adapter.
        List<ExpenseReport> expenseReports = null;
        if (isExpenseApprover()) {
            Log.d(Const.LOG_TAG, " in updateListUI for expense approver...");
            expenseReports = getReports();
        }

        // TODO MOB-13000
        if ((expenseReports != null && expenseReports.size() > 0) || (hasTripsToApprove())
                || (isInvoiceApprover() || isInvoiceUser()) || (isTRApprover())) {
            // Ensure the view containing the expense list is displayed.
            if (viewState != ViewState.LOCAL_DATA) {
                viewState = ViewState.LOCAL_DATA;
                flipViewForViewState();
            }
            // Populate the report list.
            Log.d(Const.LOG_TAG, " in updateListUI before configListentries...");
            configureListEntries();
        } else {
            // Flip to view indicating no data currently exists.
            Log.d(Const.LOG_TAG, " in updateListUI NODATA ...");
            viewState = ViewState.NO_DATA;
            flipViewForViewState();
        }
    }

    /**
     * Will create and register with the application context an instance of 'ReportDetailReceiver' and update the
     * 'reportDetailReceiver' attribute.
     */
    protected void registerReportDetailReceiver() {
        reportDetailReceiver = new ReportDetailReceiver(this);
        getApplicationContext().registerReceiver(reportDetailReceiver, reportDetailFilter);
    }

    /**
     * Will unregister with the application context the current instance of 'reportDetailReceiver' and set the
     * 'reportDetailReceiver' attribute to 'null'.
     */
    protected void unregisterReportDetailReceiver() {
        getApplicationContext().unregisterReceiver(reportDetailReceiver);
        reportDetailReceiver = null;
    }

    /**
     * Provides a broadcast receiver to handle asynchronous data updates.
     * 
     * @author AndrewK
     */
    class DataUpdateReceiver extends BroadcastReceiver {

        final String CLS_TAG = Approval.CLS_TAG + "." + DataUpdateReceiver.class.getSimpleName();

        /**
         * Receive notification that the list of trips has been updated. This method may be called any number of times while the
         * Activity is running.
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            ServiceRequest serviceRequest = dataUpdateRequest;

            dataUpdateRequest = null;

            decrInProgressRef();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from data update receiver");
            ConcurCore concurCore = (ConcurCore) getApplication();
            int requestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
            if (requestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                int httpStatus = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, 1);
                if (httpStatus == HttpStatus.SC_OK) {

                    String mwsStatus = intent.getStringExtra(Const.REPLY_STATUS);
                    if (!mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                        actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                        Log.d(Const.LOG_TAG, CLS_TAG + ".onReceive: MWS error '" + actionStatusErrorMessage + "'.");
                        // Update the list UI regardless of the outcome below.
                        updateListUI();
                        showDialog(getDataUpdateErrorDialogType());
                    } else if (mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                        // Count summary
                        countSummaryRequest = concurCore.getService().sendCountSummaryRequest(true);
                        // Add expense related broadcast receivers.
                        dataReceiverFilter.addAction(Const.ACTION_SUMMARY_UPDATED);
                        // Register the receiver.
                        registerReceiver(countDataReceiver, dataReceiverFilter);
                        dataReceiverRegistered = true;
                        Log.d(Const.LOG_TAG, CLS_TAG + " calling increment from receiver");
                        incrInProgressRef();
                    }
                } else {
                    if (serviceRequest != null && !serviceRequest.isCanceled()) {
                        lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                        Log.d(Const.LOG_TAG, CLS_TAG + ".onReceive: non HTTP status: '" + lastHttpErrorMessage + "'.");
                        // Update the list UI regardless of the outcome below.
                        updateListUI();
                        showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                    }
                }
            } else {
                if (serviceRequest != null && !serviceRequest.isCanceled()) {
                    Log.d(Const.LOG_TAG,
                            CLS_TAG + ".onReceive: request could not be completed due to: "
                                    + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                    // Update the list UI regardless of the outcome below.
                    updateListUI();
                    showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                }
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for retrieving a detail report object.
     * 
     * @author AndrewK
     */
    static class ReportDetailReceiver extends BroadcastReceiver {

        private final String CLS_TAG = Approval.CLS_TAG + "." + ReportDetailReceiver.class.getSimpleName();

        // A reference to the activity.
        private Approval activity;

        // A reference to the get report detail request.
        private ReportDetailRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        // Contains the intent to launch once a detailed report has been
        // successfully retrieved.
        private Intent launchIntent;

        /**
         * Constructs an instance of <code>ReportDetailReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReportDetailReceiver(Approval activity) {
            this.activity = activity;
        }

        /**
         * Sets the intent to be launched once a detailed report has been received.
         * 
         * @param launchIntent
         *            the intent to be launched once a detailed report has been received.
         */
        void setLaunchIntent(Intent launchIntent) {
            this.launchIntent = launchIntent;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(Approval activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.reportDetailRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the report detail request object associated with this broadcast receiver.
         * 
         * @param request
         *            the add report receipt request object associated with this broadcast receiver.
         */
        void setRequest(ReportDetailRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                // Unregister the receiver.
                activity.unregisterReportDetailReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    try {
                                        // Dismiss the dialog.
                                        activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                    if (launchIntent != null) {
                                        // Launch the activity.
                                        activity.startActivity(launchIntent);
                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: launchIntent is null!");
                                    }
                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);
                                    // Display an error dialog.
                                    activity.showDialog(Const.DIALOG_EXPENSE_REPORT_DETAIL_RETRIEVE_FAILED);

                                    try {
                                        // Dismiss the dialog.
                                        activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                }
                            } else {
                                activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage);
                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                                try {
                                    // Dismiss the dialog.
                                    activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                            try {
                                // Dismiss the dialog.
                                activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + activity.lastHttpErrorMessage);
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                            try {
                                // Dismiss the dialog.
                                activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    }
                } else {
                    try {
                        // Dismiss the dialog.
                        activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }

                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear out the request.
                activity.reportDetailRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    // Contains the receiver to handle responses for home screen data.
    private final BroadcastReceiver countDataReceiver = new BroadcastReceiver() {

        /**
         * Receive notification that some piece of data has been retrieved.
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (Const.ACTION_SUMMARY_UPDATED.equals(action)) {
                updateExpenseSummaryInfo(intent);
            }
        }
    };

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of an itinerary list request.
     */
    protected class ItineraryReceiver extends BaseBroadcastReceiver<Approval, ItineraryRequest> {

        /**
         * Constructs an instance of <code>ItineraryReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ItineraryReceiver(Approval activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(Approval activity) {
            activity.itineraryRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure (android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess (android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            onHandleSuccessItinerary(intent);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ItineraryRequest request) {
            activity.itineraryRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterItineraryReceiver();
        }
    }

    /**
     * Will examine the result of an attempt to retrieve summary information and update the UI.
     * 
     * @param intent
     *            the intent object containing the result information.
     */
    protected void updateExpenseSummaryInfo(Intent intent) {
        decrInProgressRef();
        Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from updateexpenseSummary");
        // Update the list UI regardless of the outcome below.
        updateListUI();

        int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
        if (serviceRequestStatus != -1) {
            if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                if (httpStatusCode != -1) {
                    if (httpStatusCode == HttpStatus.SC_OK) {
                        if (!(intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS))) {
                            actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                    + actionStatusErrorMessage + ".");
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
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".onReceive: service request error -- "
                                    + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
        }
        // Reset the request.
        countSummaryRequest = null;
    }

    /**
     * Whether the currently logged in end-user can approve Invoices.
     * 
     * @return whether the currently logged in end-user can approve Invoices.
     */
    private boolean isInvoiceApprover() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean result = prefs.getBoolean(Const.PREF_IS_INVOICE_APPROVER, false);
        return result;
    }

    /**
     * Whether the currently logged in end-user can view and submit Invoices.
     * 
     * @return whether the currently logged in end-user can view and submit Invoices.
     */
    private boolean isInvoiceUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean result = prefs.getBoolean(Const.PREF_IS_INVOICE_USER, false);
        return result;
    }

    /**
     * Whether the currently logged in user is an expense approver.
     * 
     * @return whether the currently logged in end-user is an expense approver.
     */
    private boolean isExpenseApprover() {
        boolean retVal = false;
        if (Preferences.shouldAllowReportApprovals()) {
            retVal = ViewUtil.isExpenseApprover(this);
        }
        return retVal;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ConcurCore.START_UP_REQ_CODE) {
                refreshApprovalList();
                refreshTripsToApproveList();
            }
        } else {
            // Intent it = new Intent();
            // it.putExtra(ConcurCore.FROM_NOTIFICATION, true);
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    /**
     * Whether the currently logged in user is a Travel Request approver.
     * 
     * @return whether the currently logged in user is a Travel Request approver.
     */
    private boolean isTRApprover() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean result = prefs.getBoolean(Const.PREF_CAN_TR_APPROVE, false);
        return result;
    }

    /**
     * Whether the currently logged in user is a Travel approver.
     * 
     * @return whether the currently logged in user is a Travel approver.
     */
    private boolean isTravelApprover() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // set into class member to be used in the rest of the code instead of
        // calling this method
        isTravelApprover = prefs.getBoolean(Const.PREF_CAN_TRAVEL_APPROVE, false);

        return isTravelApprover;
    }

    /**
     * Listener used for updating the trip approval list
     */
    private class TripsToApproveListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {

            decrInProgressRef();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestsuccess");
            // update the approvals list with the trips needing approval
            updateListUI();
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            decrInProgressRef();
            updateListUI();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in TripsToApproveListener...");
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            decrInProgressRef();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TripsToApproveListener...");

        }

        @Override
        public void cleanup() {
            tripsToApproveReceiver = null;

        }
    }
}
