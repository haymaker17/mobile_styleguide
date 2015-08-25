/**
 * 
 */
package com.concur.mobile.core.expense.report.approval.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;

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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.report.activity.ActiveReportListItem;
import com.concur.mobile.core.expense.report.activity.ExpenseEntries;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ReportComparator;
import com.concur.mobile.core.expense.report.service.ReportDetailRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FeedbackManager;
import com.concur.mobile.core.util.SortOrder;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.core.view.ListItemAdapter;

/**
 * Provides an activity to display expense reports for approval.
 * 
 * @author AndrewK
 */
public class ExpenseApproval extends BaseActivity {

    static final String CLS_TAG = ExpenseApproval.class.getSimpleName();

    private static final String REPORT_DETAIL_RECEIVER_KEY = "report.detail.receiver";

    private static final int APPROVAL_REPORT_LIST_ITEM_VIEW_TYPE = 0;

    private static final int VIEW_EXP_DETAIL = 1;

    /**
     * Contains a reference to an <code>IntentFilter</code> used to register a receiver to handle data update events.
     */
    protected IntentFilter filter;

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

    protected Bundle lastSavedInstanceState;

    /**
     * Flag to disable multiple consecutive clicks on report items
     */
    protected boolean itemClickEnabled;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

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

        // Set the actionbar title
        getSupportActionBar().setTitle(getTitleHeaderTextResId());

        // Is the service immediately available for us to retrieve data?
        if (isServiceAvailable()) {
            buildView();
        } else {
            lastSavedInstanceState = savedInstanceState;
            buildViewDelay = true;
            viewState = ViewState.RESTORE_APP_STATE;
            flipViewForViewState();
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
            retVal = true;
        }
        return retVal;
    }

    /**
     * Returns the menu layout for this View.
     */
    protected int getMenuLayout() {
        return R.menu.reports;
    }

    protected void buildView() {

        Intent launchIntent = getIntent();
        boolean reportListUpdated = false;
        if (launchIntent.getExtras() != null) {
            reportListUpdated = launchIntent.getExtras().getBoolean(getPendingDataRetrievalExtraKey(), false);
        }

        // Initialize the view.
        initView();

        // Only send a request to retrieve data if there is no pending request.
        if (!reportListUpdated) {
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
     * Will initiate refreshing the data if needbe.
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
            if (!expRepCache.hasLastReportList()
                    || expRepCache.isLastReportListUpdateExpired(Const.CACHE_DATA_EXPIRATION_DURATION_MILLISECONDS)
                    || expRepCache.shouldRefetchReportList()) {
                if (ConcurCore.isConnected()) {
                    // Send a request for an initial/updated list of reports.
                    ConcurCore ConcurCore = (ConcurCore) getApplication();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore
                            .getApplicationContext());
                    sendDataUpdateRequest(prefs.getString(Const.PREF_USER_ID, null));

                    // Clear refetch report list.
                    expRepCache.clearShouldRefetchReportList();
                } else {
                    // If there is no cached data, then present a dialog
                    // indicating the client
                    // is offline.
                    if (!expRepCache.hasLastReportList() && showNoConnectivityDialog) {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                }
            } else {
                List<ExpenseReport> expRepList = expRepCache.getReportList();
                if (expRepCache.hasLastReportList() && (expRepList == null || expRepList.size() == 0)) {
                    // Flip to view indicating no data currently exists.
                    viewState = ViewState.NO_DATA;
                    flipViewForViewState();
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: expRepCache is null!");
        }
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
        List<ExpenseReport> expenseReports = getReports();
        if (expenseReports != null && expenseReports.size() > 0) {
            // Flip to the view containing the list and button bar.
            viewState = ViewState.LOCAL_DATA;
            flipViewForViewState();
            // Populate the report list.
            configureReportEntries();
        } else {
            // Report list was updated, and empty.
            viewState = ViewState.NO_DATA;
            flipViewForViewState();
            // Populate the report list.
            configureReportEntries();
        }

        // Show/Hide the screen title action button.
        ImageView imgView = (ImageView) findViewById(R.id.action_button);
        if (imgView != null) {

            if (showTitleBarActionButton()) {
                imgView.setVisibility(View.VISIBLE);
                imgView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onAddActionButton();
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
        return false;
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
        // MOB-20035 Wait for the existing item click to finish.
        if (itemClickEnabled == false)
            return;

        Intent intent = new Intent(ExpenseApproval.this, ExpenseEntries.class);
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
                startActivityForResult(intent, VIEW_EXP_DETAIL);
            } else {
                if (ConcurCore.isConnected()) {
                    // MOB-20035 Need to fetch report detail, disable clicking on report items
                    itemClickEnabled = false;

                    ConcurCore ConcurCore = (ConcurCore) getApplication();

                    // Launch a request to obtain a detailed report.
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
                        itemClickEnabled = true;
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
                        startActivityForResult(intent, VIEW_EXP_DETAIL);
                    }
                }
            }
        } else {
            addExtraIntentData(intent, expenseReport);
            startActivityForResult(intent, VIEW_EXP_DETAIL);
        }
    }

    /**
     * Configures a list adapter to populate the list with expense reports.
     */
    private void configureReportEntries() {
        itemClickEnabled = true;

        // Use the cached data to immediately display a list.
        listItemAdapter = new ListItemAdapter<ListItem>(this, getReportListItems());
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
                    } else {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG
                                        + ".configureReportEntries.onItemClick: select list item is not of type Active/Approval report list item!");
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
        Dialog dialog = null;
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
        default: {
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            dialog = ConcurCore.createDialog(this, id);
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

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {

        super.onResume();
        restoreReceivers();

        // Set up the broadcast receiver if needbe.
        if (!broadcastReceiverRegistered) {
            registerReceiver(broadcastReceiver, filter);
            broadcastReceiverRegistered = true;
        }
        // If the 'onResume' is for approvals, then check for data freshness.
        // TODO: Examine whether we can generalize the update logic in
        // ExpenseActiveReports.onResume with the code in 'checkForRefreshData'.
        if (this.getClass().equals(ExpenseApproval.class)) {
            if (isServiceAvailable() && dataUpdateRequest == null) {
                // Check and request new data, if needbe.
                checkForRefreshData(false);
            }
        }
    }

    protected void restoreReceivers() {
        // Check for outstanding 'ReportDetailReceiver'.
        if (retainer.contains(REPORT_DETAIL_RECEIVER_KEY)) {
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
     * Gets the list of expense reports to be displayed.
     * 
     * @return the list of expense reports.
     */
    protected List<ListItem> getReportListItems() {
        List<ListItem> listItems = null;
        List<ExpenseReport> reports = getReports();
        if (reports != null) {
            listItems = new ArrayList<ListItem>(reports.size());
            Collections.sort(reports, new ReportComparator(SortOrder.DESCENDING));
            for (ExpenseReport report : reports) {
                listItems.add(new ApprovalReportListItem(report, APPROVAL_REPORT_LIST_ITEM_VIEW_TYPE));
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
        if (reports != null && reports.size() > 0) {
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
        if (service != null) {
            dataUpdateRequest = service.sendReportsToApproveRequest(userId);
            if (dataUpdateRequest != null) {
                ViewUtil.setTextViewText(this, R.id.loading_data, R.id.data_loading_text,
                        getText(getDataLoadingTextResourceId()).toString(), true);
                // Set the view state to indicate data being loaded.
                viewState = ViewState.LOCAL_DATA_REFRESH;
                flipViewForViewState();
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
     * Will update the report list UI.
     */
    protected void updateReportListUI() {
        // If there is no local cached data, then flip to view indicating this.
        // If there are reports cached locally, then set the list adapter.
        List<ExpenseReport> expenseReports = getReports();
        if (expenseReports != null && expenseReports.size() > 0) {
            // Ensure the view containing the expense list is displayed.
            if (viewState != ViewState.LOCAL_DATA) {
                viewState = ViewState.LOCAL_DATA;
                flipViewForViewState();
            }
            // Populate the report list.
            configureReportEntries();
        } else {
            // Flip to view indicating no data currently exists.
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
        // MOB-20035 prevent crash when quick double taps on report item(s) cause this to be called twice.
        if (reportDetailReceiver != null) {
            getApplicationContext().unregisterReceiver(reportDetailReceiver);
            reportDetailReceiver = null;
        }
    }

    /**
     * Provides a broadcast receiver to handle asynchronous data updates.
     * 
     * @author AndrewK
     */
    class DataUpdateReceiver extends BroadcastReceiver {

        final String CLS_TAG = ExpenseApproval.CLS_TAG + "." + DataUpdateReceiver.class.getSimpleName();

        /**
         * Receive notification that the list of trips has been updated. This method may be called any number of times while the
         * Activity is running.
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            ServiceRequest serviceRequest = dataUpdateRequest;

            dataUpdateRequest = null;

            // Update the list UI regardless of the outcome below.
            updateReportListUI();

            int requestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
            if (requestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                int httpStatus = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, 1);
                if (httpStatus == HttpStatus.SC_OK) {
                    String mwsStatus = intent.getStringExtra(Const.REPLY_STATUS);
                    if (!mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                        actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                        Log.d(Const.LOG_TAG, CLS_TAG + ".onReceive: MWS error '" + actionStatusErrorMessage + "'.");
                        showDialog(getDataUpdateErrorDialogType());
                    }
                } else {
                    if (serviceRequest != null && !serviceRequest.isCanceled()) {
                        lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                        Log.d(Const.LOG_TAG, CLS_TAG + ".onReceive: non HTTP status: '" + lastHttpErrorMessage + "'.");
                        showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                    }
                }
            } else {
                if (serviceRequest != null && !serviceRequest.isCanceled()) {
                    Log.d(Const.LOG_TAG,
                            CLS_TAG + ".onReceive: request could not be completed due to: "
                                    + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                    showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == VIEW_EXP_DETAIL) {
                FeedbackManager.with(this).showRatingsPrompt();
            }
        }
        super.onActivityResult(requestCode, requestCode, data);
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for retrieving a detail report object.
     * 
     * @author AndrewK
     */
    static class ReportDetailReceiver extends BroadcastReceiver {

        private final String CLS_TAG = ExpenseApproval.CLS_TAG + "." + ReportDetailReceiver.class.getSimpleName();

        // A reference to the activity.
        private ExpenseApproval activity;

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
        ReportDetailReceiver(ExpenseApproval activity) {
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
        void setActivity(ExpenseApproval activity) {
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
                                        activity.startActivityForResult(launchIntent, ExpenseApproval.VIEW_EXP_DETAIL);
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
                // MOB-20035 re-enable clicking on report items
                activity.itemClickEnabled = true;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

}
