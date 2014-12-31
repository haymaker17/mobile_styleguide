/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.concur.core.R;
import com.concur.mobile.base.ui.UIUtils;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.report.approval.activity.ExpenseApproval;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ReportComparator;
import com.concur.mobile.core.expense.report.service.ReportDeleteRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.SortOrder;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.HeaderListItem;
import com.concur.mobile.core.view.ListItem;

/**
 * An extension of <code>Activity</code> for displaying a list of expense active reports.
 * 
 * @author AndrewK
 */
public class ExpenseActiveReports extends ExpenseApproval {

    static final String CLS_TAG = ExpenseActiveReports.class.getSimpleName();

    private static final int HEADER_VIEW_TYPE = 0;
    private static final int ACTIVE_REPORT_VIEW_TYPE = 1;

    private static final String REPORT_DELETE_RECEIVER_KEY = "report.delete.receiver";

    private static final String LONG_PRESS_REPORT_KEY = "long.press.report.key";

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

    /**
     * Contains a broadcast receiver to handle the result of deleting a report.
     */
    protected ReportDeleteReceiver reportDeleteReceiver;

    /**
     * Contains the filter used to register the report delete receiver.
     */
    protected IntentFilter reportDeleteFilter;

    /**
     * Contains an outstanding request to delete a report.
     */
    protected ReportDeleteRequest reportDeleteRequest;

    /**
     * Contains a reference to the expense report key upon which a long-press has occurred.
     */
    protected String longPressReportKey;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#onCreate(android.os .Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LONG_PRESS_REPORT_KEY)) {
                longPressReportKey = savedInstanceState.getString(LONG_PRESS_REPORT_KEY);
            }

            upTime = savedInstanceState.getLong(Const.ACTIVITY_STATE_UPTIME, 0L);
        }

        // Determine if user is test drive user
        boolean isTestDriveUser = Preferences.isTestDriveUser();
        if (isTestDriveUser && Preferences.shouldShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_EXPENSE_ACTIVE_REPORTS)) {
            showTestDriveTips();
        }
    }

    /**
     * If this user is a test drive user, this will get called the first time the user loads the Expense Reports screen to show
     * the test drive tips overlay
     */
    protected void showTestDriveTips() {
        // OnClickListener whose onClick() is called from setupOverlay()
        OnClickListener dismissListener = new OnClickListener() {

            public void onClick(View v) {
                Preferences.setShouldNotShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_EXPENSE_ACTIVE_REPORTS);
                isTipsOverlayVisible = false;

                // Analytics stuff.
                Map<String, String> flurryParams = new HashMap<String, String>();
                upTime = ((System.nanoTime() - startTime) / 1000000000L) + upTime; // Convert nanoseconds to seconds.
                flurryParams.put(Flurry.PARAM_NAME_SECONDS_ON_OVERLAY, Flurry.formatDurationEventParam(upTime));
                EventTracker.INSTANCE.track(Flurry.CATEGORY_OVERLAYS, "Reports", flurryParams);
            }
        };

        UIUtils.setupOverlay((ViewGroup) getWindow().getDecorView(), R.layout.td_overlay_expense_active_reports,
                dismissListener, R.id.td_icon_cancel_button, this, R.anim.fade_out, 300L);

        isTipsOverlayVisible = true;
    }

    /**
     * Gets the list of active reports.
     * 
     * @return the list of active reports.
     */
    @Override
    protected List<ExpenseReport> getReports() {
        ConcurCore ConcurCore = (ConcurCore) getApplication();
        List<ExpenseReport> reports = ConcurCore.getExpenseActiveCache().getReportListDetail();
        return reports;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#getExpenseReports()
     */
    @Override
    protected List<ListItem> getReportListItems() {
        List<ListItem> listItems = null;
        List<ExpenseReport> reports = getReports();
        if (reports != null) {
            listItems = new ArrayList<ListItem>(reports.size());
            Collections.sort(reports, new ReportComparator(SortOrder.DESCENDING));
            // First, place the reports into two buckets, submitted and
            // unsubmitted reports.
            List<ExpenseReport> unsubmittedReports = new ArrayList<ExpenseReport>();
            List<ExpenseReport> submittedReports = new ArrayList<ExpenseReport>();
            for (ExpenseReport report : reports) {
                if (report.isUnsubmitted() || report.isSentBack()) {
                    unsubmittedReports.add(report);
                } else {
                    submittedReports.add(report);
                }
            }
            // Second, if the unsubmitted reports list is non-empty, then add a
            // header
            // and the list items.
            if (unsubmittedReports.size() > 0) {
                listItems.add(new HeaderListItem(getText(R.string.unsubmitted).toString(), HEADER_VIEW_TYPE));
                for (ExpenseReport report : unsubmittedReports) {
                    listItems.add(new ActiveReportListItem(report, ACTIVE_REPORT_VIEW_TYPE));
                }
            }
            // Third, if the submitted reports list is non-empty, then add a
            // header
            // and the list items.
            // Fourth, add a sorted list of submitted reports.
            if (submittedReports.size() > 0) {
                listItems.add(new HeaderListItem(getText(R.string.submitted).toString(), HEADER_VIEW_TYPE));
                for (ExpenseReport report : submittedReports) {
                    listItems.add(new ActiveReportListItem(report, ACTIVE_REPORT_VIEW_TYPE));
                }
            }

            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_UNSUBMITTED_COUNT, Integer.toString(unsubmittedReports.size()));
            params.put(Flurry.PARAM_NAME_SUBMITTED_PENDING_COUNT, Integer.toString(submittedReports.size()));
            EventTracker.INSTANCE.track(Flurry.CATEGORY_REPORTS, Flurry.EVENT_NAME_LIST, params);
        }
        return listItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#getExpenseReportCache ()
     */
    @Override
    protected IExpenseReportCache getExpenseReportCache() {
        ConcurCore ConcurCore = (ConcurCore) getApplication();
        return ConcurCore.getExpenseActiveCache();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#isDetailedReportRequired ()
     */
    @Override
    protected boolean isDetailedReportRequired() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.ExpenseApproval#isApprovalList()
     */
    @Override
    protected boolean isApprovalList() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#getLastDataUpdateTime ()
     */
    @Override
    protected Calendar getLastDataUpdateTime() {
        ConcurCore ConcurCore = (ConcurCore) getApplication();
        return ConcurCore.getExpenseActiveCache().getLastReportListUpdateTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#getDataUpdateIntentFilter ()
     */
    @Override
    protected IntentFilter getDataUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter(Const.ACTION_EXPENSE_ACTIVE_REPORTS_UPDATED);
        intentFilter.addAction(Const.ACTION_EXPENSE_REPORT_SUBMIT_UPDATE);
        return intentFilter;
    }

    @Override
    protected int getRetrievingDataTextResourceId() {
        return R.string.retrieving_reports;
    }

    @Override
    protected int getUpdatingDataTextResourceId() {
        return R.string.updating_reports;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval# getRetrievingDataDialogType()
     */
    @Override
    protected int getRetrievingDataDialogType() {
        return Const.DIALOG_EXPENSE_RETRIEVE_ACTIVE_REPORTS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval# getDataUpdateErrorDialogType()
     */
    @Override
    protected int getDataUpdateErrorDialogType() {
        return Const.DIALOG_EXPENSE_ACTIVE_REPORT_RETRIEVE_FAILED;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval# getPendingDataRetrievalExtraKey()
     */
    @Override
    protected String getPendingDataRetrievalExtraKey() {
        return Const.EXTRA_EXPENSE_ACTIVE_REPORT_LIST_PENDING;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#getNoDataTextResourceId ()
     */
    @Override
    protected int getNoDataTextResourceId() {
        return R.string.no_reports;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#sendDataUpdateRequest (java.lang.String)
     */
    @Override
    protected void sendDataUpdateRequest(String userId) {
        ConcurService service = getConcurService();
        if (service != null) {
            // Send the request to the server.
            dataUpdateRequest = service.sendActiveReportsRequest();
            if (dataUpdateRequest != null) {
                ViewUtil.setTextViewText(this, R.id.loading_data, R.id.data_loading_text,
                        getText(getDataLoadingTextResourceId()).toString(), true);
                // Set the view state to indicate data being loaded.
                viewState = ViewState.LOCAL_DATA_REFRESH;
                flipViewForViewState();
                // Ensure we clear any fetch flag set on the cache.
                IExpenseReportCache expRepCache = ((ConcurCore) getApplication()).getExpenseActiveCache();
                expRepCache.clearShouldRefetchReportList();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#getTitleHeaderTextResId ()
     */
    @Override
    protected int getTitleHeaderTextResId() {
        return R.string.reports;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval# getExpenseReportRowLayoutResId(boolean)
     */
    @Override
    protected int getExpenseReportRowLayoutResId(boolean enabled) {
        int layoutResId = ((enabled) ? R.layout.active_report_row : R.layout.active_report_row_disabled);
        return layoutResId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#getExpenseReportSource ()
     */
    @Override
    protected int getExpenseReportSource() {
        return Const.EXPENSE_REPORT_SOURCE_ACTIVE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#addExtraIntentData (android.content.Intent,
     * com.concur.mobile.data.expense.ExpenseReport)
     */
    @Override
    protected void addExtraIntentData(Intent intent, ExpenseReport expRep) {
        super.addExtraIntentData(intent, expRep);
        // Determine whether 'expRep' is a detailed report and if it's cache
        // time is older
        // than 'Const.CACHE_DATA_EXPIRATION_DURATION_MILLISECONDS'
        // milliseconds, then set the flag
        // to retrieve a new detailed report.
        boolean updateReportDetailKey = false;
        IExpenseReportCache expRepCache = getExpenseReportCache();
        if (expRepCache.hasReportDetail(expRep.reportKey)) {
            updateReportDetailKey = expRepCache.isLastDetailReportUpdateExpired(expRep.reportKey,
                    Const.CACHE_DATA_EXPIRATION_DURATION_MILLISECONDS);
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".addExtraIntentData: expense report cache has no report detail object!");
            updateReportDetailKey = true;
        }
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, updateReportDetailKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Save 'ReportDeleteReceiver'.
        if (reportDeleteReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            reportDeleteReceiver.setActivity(null);
            // Add to the retainer.
            retainer.put(REPORT_DELETE_RECEIVER_KEY, reportDeleteReceiver);
        }

        if (isTipsOverlayVisible) {
            // Save the time the user spent on this screen, but
            // perhaps put the app in the background.
            upTime += (System.nanoTime() - startTime) / 1000000000L; // Convert to seconds.
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#restoreReceivers()
     */
    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();

        // Restore 'ReportDeleteReceiver'.
        if (retainer.contains(REPORT_DELETE_RECEIVER_KEY)) {
            reportDeleteReceiver = (ReportDeleteReceiver) retainer.get(REPORT_DELETE_RECEIVER_KEY);
            if (reportDeleteReceiver != null) {
                // Set the activity on the receiver.
                reportDeleteReceiver.setActivity(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".restoreReceivers: retainer contains null reference for report delete receiver!");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case Const.DIALOG_EXPENSE_DELETE_REPORT_CONFIRM: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_expense_confirm_report_delete_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Dismiss the dialog.
                    dialog.dismiss();
                    // Send the request to delete.
                    sendReportDeleteRequest();
                }
            });
            dlgBldr.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        case Const.DIALOG_EXPENSE_DELETE_REPORT_PROGRESS: {
            dialog = super.onCreateDialog(id);
            dialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    // Dismiss the dialog.
                    dialog.dismiss();
                    // Attempt to cancel the request.
                    if (reportDeleteRequest != null) {
                        reportDeleteRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: reportDeleteRequest is null!");
                    }
                }
            });
            break;
        }
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    /**
     * Will refresh the report list.
     */
    protected void refreshReportList() {
        IExpenseReportCache expRepCache = getConcurCore().getExpenseActiveCache();
        if (expRepCache.shouldRefreshReportList()) {
            // Refresh the display.
            List<ListItem> listItems = getReportListItems();
            listItemAdapter.setItems(listItems);
            listItemAdapter.notifyDataSetChanged();
            // Clear the refresh flag.
            expRepCache.clearShouldRefreshReportList();
            if (listItems == null || listItems.size() == 0) {
                // Flip to view indicating no data currently exists.
                viewState = ViewState.NO_DATA;
                flipViewForViewState();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case Const.DIALOG_EXPENSE_DELETE_REPORT_CONFIRM: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (longPressReportKey != null) {
                ExpenseReport expRep = null;
                IExpenseReportCache expRepCache = getConcurCore().getExpenseActiveCache();
                // First check for a detailed report, second a non-detailed one.
                // NOTE: A newly created report, i.e., not one that came back in
                // the active reports
                // list will be in the cache as a detailed report.
                if (expRepCache.hasReportDetail(longPressReportKey)) {
                    expRep = expRepCache.getReportDetail(longPressReportKey);
                } else {
                    expRep = expRepCache.getReport(longPressReportKey);
                }
                if (expRep != null) {
                    alertDlg.setMessage(Format.localizeText(this, R.string.dlg_expense_confirm_report_delete_message,
                            expRep.reportName));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: expRep is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: longPressReportKey is null!");
            }
            break;
        }
        case Const.DIALOG_EXPENSE_DELETE_REPORT_FAILED: {
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onCreateContextMenu(android .view.ContextMenu, android.view.View,
     * android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        if (menuInfo != null && menuInfo instanceof AdapterContextMenuInfo) {
            if (listItemAdapter.getItem(((AdapterContextMenuInfo) menuInfo).position) instanceof ActiveReportListItem) {
                final ExpenseReport expRep = ((ActiveReportListItem) listItemAdapter
                        .getItem(((AdapterContextMenuInfo) menuInfo).position)).report;
                menu.setHeaderTitle(R.string.expense_report_action_long_press_title);
                android.view.MenuItem menuItem = menu.add(0, Menu.NONE, 0, R.string.general_view);
                menuItem.setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {

                    /*
                     * (non-Javadoc)
                     * 
                     * @see android.view.MenuItem.OnMenuItemClickListener#onMenuItemClick (android.view.MenuItem)
                     */
                    @Override
                    public boolean onMenuItemClick(android.view.MenuItem item) {
                        onViewExpenseReport(expRep);
                        return true;
                    }
                });
                if (expRep.isUnsubmitted() || expRep.isSentBack()) {
                    menuItem = menu.add(0, Menu.NONE, 0, R.string.delete);
                    menuItem.setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {

                        /*
                         * (non-Javadoc)
                         * 
                         * @see android.view.MenuItem.OnMenuItemClickListener# onMenuItemClick(android.view.MenuItem)
                         */
                        @Override
                        public boolean onMenuItemClick(android.view.MenuItem item) {
                            // MOB-11312
                            if (!ConcurCore.isConnected()) {
                                showDialog(Const.DIALOG_NO_CONNECTIVITY);
                            } else {
                                // Set the long-press key.
                                longPressReportKey = expRep.reportKey;
                                // Display the report deletion confirmation
                                // dialog.
                                showDialog(Const.DIALOG_EXPENSE_DELETE_REPORT_CONFIRM);
                            }
                            return true;
                        }
                    });
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateContextMenu: null/incorrect ContextMenuInfo object!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!buildViewDelay) {
            // Check if the expense cache indicates the set of active reports
            // should be updated from the server.
            IExpenseReportCache expRepCache = ((ConcurCore) getApplication()).getExpenseActiveCache();
            if ((expRepCache.shouldRefetchReportList() || expRepCache
                    .isLastReportListUpdateExpired(Const.CACHE_DATA_EXPIRATION_DURATION_MILLISECONDS))
                    && dataUpdateRequest == null) {
                if (ConcurCore.isConnected()) {
                    // Send the request.
                    ConcurCore ConcurCore = (ConcurCore) getApplication();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore
                            .getApplicationContext());
                    sendDataUpdateRequest(prefs.getString(Const.PREF_USER_ID, null));
                    // Clear the flag.
                    expRepCache.clearShouldRefetchReportList();
                    // Clear any refresh flag.
                    expRepCache.clearShouldRefreshReportList();
                }
            } else if (expRepCache.shouldRefreshReportList()) {
                // Refresh the display.
                List<ListItem> listItems = getReportListItems();
                listItemAdapter.setItems(listItems);
                listItemAdapter.notifyDataSetChanged();
                // Clear the refresh flag.
                expRepCache.clearShouldRefreshReportList();
                if (listItems == null || listItems.size() == 0) {
                    // Flip to view indicating no data currently exists.
                    viewState = ViewState.NO_DATA;
                    flipViewForViewState();
                }
            }
        }

        if (isTipsOverlayVisible) {
            startTime = System.nanoTime();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#showTitleBarActionButton ()
     */
    @Override
    protected boolean showTitleBarActionButton() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#onAddActionButton()
     */
    @Override
    protected void onAddActionButton() {
        // MOB-11304
        if (!ConcurCore.isConnected()) {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        } else {
            onCreateReport();
        }
    }

    private void onCreateReport() {
        // Invoke the ExpenseHeader class and specify that source as "New".
        Intent intent = new Intent(this, ExpenseReportHeader.class);
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, Const.EXPENSE_REPORT_SOURCE_NEW);
        this.startActivityForResult(intent, Const.CREATE_NEW_REPORT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Get the reportKey and launch the Report Details
        // of the newly created report.
        if (requestCode == Const.CREATE_NEW_REPORT && data != null && data.hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {

            // Set the flag indicating the report list should be refreshed.
            IExpenseReportCache expRepCache = getConcurCore().getExpenseActiveCache();
            if (expRepCache != null) {
                expRepCache.setShouldRefreshReportList();
            }

            String reportKey = data.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
            Intent i = new Intent(this, ExpenseEntries.class);
            i.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, reportKey);
            i.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, Const.EXPENSE_REPORT_SOURCE_ACTIVE);
            startActivity(i);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#getMenuLayout()
     */
    @Override
    protected int getMenuLayout() {
        return R.menu.active_reports;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseApproval#onOptionsItemSelected (android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        final int itemId = menuItem.getItemId();
        if (itemId == R.id.new_report) {
            if (isServiceAvailable()) {
                if (ConcurCore.isConnected()) {
                    onCreateReport();
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
            }
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onSaveInstanceState(android.os .Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save any long-press report key.
        if (longPressReportKey != null) {
            outState.putString(LONG_PRESS_REPORT_KEY, longPressReportKey);
        }

        if (isTipsOverlayVisible) {
            // Save the uptime so we know how long the user has been on this screen,
            // even if it has been destroyed.
            outState.putLong(Const.ACTIVITY_STATE_UPTIME, upTime);
        }
    }

    /**
     * Will send a request to the server to delete the report currently being viewed.
     */
    protected void sendReportDeleteRequest() {
        ConcurService concurService = getConcurService();
        if (longPressReportKey != null) {
            IExpenseReportCache expRepCache = getConcurCore().getExpenseActiveCache();
            ExpenseReport expRep = null;
            // First check for a detailed report, second a non-detailed one.
            // NOTE: A newly created report, i.e., not one that came back in the
            // active reports
            // list will be in the cache as a detailed report.
            if (expRepCache.hasReportDetail(longPressReportKey)) {
                expRep = expRepCache.getReportDetail(longPressReportKey);
            } else {
                expRep = expRepCache.getReport(longPressReportKey);
            }
            if (expRep != null) {
                registerReportDeleteReceiver();
                reportDeleteRequest = concurService.sendDeleteReportRequest(expRep.reportKey);
                if (reportDeleteRequest == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportDeleteRequest: unable to create report delete request.");
                    unregisterReportDeleteReceiver();
                } else {
                    // Set the request object on the receiver.
                    reportDeleteReceiver.setServiceRequest(reportDeleteRequest);
                    // Show the dialog.
                    showDialog(Const.DIALOG_EXPENSE_DELETE_REPORT_PROGRESS);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportDeleteRequest: unable to locate long press report!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportDeleteRequest: long press report key is null!");
        }
    }

    /**
     * Will register a report deletion broadcast receiver.
     */
    protected void registerReportDeleteReceiver() {
        if (reportDeleteReceiver == null) {
            reportDeleteReceiver = new ReportDeleteReceiver(this);
            if (reportDeleteFilter == null) {
                reportDeleteFilter = new IntentFilter(Const.ACTION_DELETE_REPORT);
            }
            getApplicationContext().registerReceiver(reportDeleteReceiver, reportDeleteFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerReportDeleteReceiver: reportDeleteReceiver is *not* null!");
        }
    }

    /**
     * Will unregister a report deletion broadcast receiver.
     */
    protected void unregisterReportDeleteReceiver() {
        if (reportDeleteReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(reportDeleteReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterReportDeleteReceiver: illegal argument", ilaExc);
            }
            reportDeleteReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterReportDeleteReceiver: reportDeleteReceiver is null!");
        }

    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> to handle the result of report deletions.
     */
    static class ReportDeleteReceiver extends BaseBroadcastReceiver<ExpenseActiveReports, ReportDeleteRequest> {

        /**
         * Constructs an instance of <code>ReportDeleteReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReportDeleteReceiver(ExpenseActiveReports activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(ExpenseActiveReports activity) {
            activity.reportDeleteRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_DELETE_REPORT_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_DELETE_REPORT_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_REPORT);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_DELETE, Flurry.EVENT_NAME_ACTION, params);

            // Refresh the report list.
            getActivity().refreshReportList();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ReportDeleteRequest request) {
            activity.reportDeleteRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterReportDeleteReceiver();
        }

    }

}
