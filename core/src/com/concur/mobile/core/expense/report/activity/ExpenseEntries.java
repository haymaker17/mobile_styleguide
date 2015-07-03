/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.ui.UIUtils;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.activity.ExpenseTypeSpinnerAdapter;
import com.concur.mobile.core.expense.activity.ExpensesAndReceipts;
import com.concur.mobile.core.expense.activity.TAItineraryActivity;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ReportEntryComparator;
import com.concur.mobile.core.expense.report.service.RemoveReportExpenseRequest;
import com.concur.mobile.core.expense.report.service.ReportDeleteRequest;
import com.concur.mobile.core.expense.report.service.ReportEntryDetailRequest;
import com.concur.mobile.core.expense.report.service.ReportEntryFormRequest;
import com.concur.mobile.core.expense.service.GetExpenseTypesRequest;
import com.concur.mobile.core.expense.travelallowance.activity.CreateItineraryActivity;
import com.concur.mobile.core.expense.travelallowance.activity.TravelAllowanceActivity;
import com.concur.mobile.core.expense.travelallowance.controller.IServiceRequestListener;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.SortOrder;
import com.concur.mobile.core.util.ViewUtil;

/**
 * Provides an activity to display expense report entries.
 * 
 * @author AndrewK
 */
public class ExpenseEntries extends AbstractExpenseActivity
                            implements IServiceRequestListener {

    private static final String CLS_TAG = ExpenseEntries.class.getSimpleName();

    private static final String REMOVE_REPORT_EXPENSE_RECEIVER_KEY = "remove.report.expense.receiver";

    private static final String REPORT_ENTRY_DETAIL_RECEIVER_KEY = "report.entry.detail.receiver";

    private static final String EXPENSE_TYPES_RECEIVER_KEY = "expense.types.receiver";

    private static final String ENTRY_FORM_RECEIVER_KEY = "entry.form.receiver";

    private static final String REPORT_DELETE_RECEIVER_KEY = "report.delete.receiver";

    private static final String SELECTED_REPORT_ENTRY_KEY = "selected.report.entry";

    private static final String EXPENSE_TYPES = "expense.types";

    private static final String SELECTED_EXPENSE_TYPE = "selected.expense.type";

    private TravelAllowanceItineraryController itineraryController;

    @Override
    public void onRequestSuccess(String controllerTag) {
        ConcurCore app = (ConcurCore) getApplication();
        if (app.getTaItineraryController() != null && app.getTaItineraryController().getItineraryList().size() > 0){
            showTravelAllowanceButton();
        }
    }

    @Override
    public void onRequestFail(String controllerTag) {

    }

    private enum ExpenseEntryOption {
        ViewDetails, ViewReceipt, RemoveFromReport, AttachSelectedGalleryReceipt, AttachSelectedCloudReceipt, AttachCapturedReceipt
    };

    /**
     * Contains the selected report entry.
     */
    private ExpenseReportEntry selectedReportEntry;

    /**
     * Contains a reference to a broadcast receiver for handling result of removing an expense entry.
     */
    protected RemoveReportExpenseReceiver removeReportExpenseReceiver;

    /**
     * Contains a reference to an outstanding request to remove a report expense.
     */
    protected RemoveReportExpenseRequest removeReportExpenseRequest;

    /**
     * Contains the intent filter used to register the remove report expense receiver.
     */
    protected IntentFilter removeReportExpenseFilter;

    protected ReportEntryDetailReceiver reportEntryDetailReceiver;

    /**
     * Contains a reference to an outstanding request to retrieve a report entry detail.
     */
    protected ReportEntryDetailRequest reportEntryDetailRequest;

    /**
     * Contains the intent filter used to register the report entry detail receiver.
     */
    protected IntentFilter reportEntryDetailFilter;

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
     * Contains a reference to the entry list adapter.
     */
    protected ExpenseReportEntryListAdapter entryListAdapter;

    // A broadcast receiver to handle the result of an expense types request.
    private ExpenseTypesReceiver expenseTypesReceiver;

    // The filter used to register the above receiver.
    private IntentFilter expenseTypesFilter;

    // A reference to an outstanding request.
    private GetExpenseTypesRequest expenseTypesRequest;

    // MOB-13572
    // Removed expTypes from ExpenseEntryItemization and made this protected to
    // prevent having two Lists
    // of ExpenseType in ExpenseEntryItemization (one of which sat null and
    // caused a crash.)
    // Contains a list of expense types for new entries.
    protected List<ExpenseType> expTypes;

    // Contains a reference to the expense type adapter.
    private ExpenseTypeSpinnerAdapter expTypeAdapter;

    // Contains a reference to a selected expense type.
    private ExpenseType selectedExpenseType;

    // A broadcast receiver to handle the result of an entry form request.
    private EntryFormReceiver entryFormReceiver;

    // The filter used to register the above receiver.
    private IntentFilter entryFormFilter;

    // A reference to an outstanding request.
    private ReportEntryFormRequest entryFormRequest;

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

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        removeReportExpenseFilter = new IntentFilter(Const.ACTION_EXPENSE_REMOVE_REPORT_EXPENSE);
        reportEntryDetailFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_ENTRY_DETAIL_UPDATED);
        expenseTypesFilter = new IntentFilter(Const.ACTION_EXPENSE_EXPENSE_TYPES_DOWNLOADED);
        entryFormFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_ENTRY_FORM_UPDATED);

        super.onCreate(savedInstanceState);

        // Restore 'selectedReportEntry'.
        if (retainer.contains(SELECTED_REPORT_ENTRY_KEY)) {
            selectedReportEntry = (ExpenseReportEntry) retainer.get(SELECTED_REPORT_ENTRY_KEY);
            if (selectedReportEntry == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: retainer contains null reference for selectedReportEntry!");
            }
        }
        // Restore any receivers.
        restoreReceivers();

        if (savedInstanceState != null) {
            // Restore the list of expense types.
            if (savedInstanceState.containsKey(EXPENSE_TYPES)) {
                String expTypesStr = savedInstanceState.getString(EXPENSE_TYPES);
                if (expTypesStr != null) {
                    expTypes = ExpenseType.parseExpenseTypeXml(expTypesStr);
                }
            }

            upTime = savedInstanceState.getLong(Const.ACTIVITY_STATE_UPTIME, 0L);
        }

        if (retainer.contains(SELECTED_EXPENSE_TYPE)) {
            selectedExpenseType = (ExpenseType) retainer.get(SELECTED_EXPENSE_TYPE);
        }

        if (expRep != null) {
            ConcurCore app = (ConcurCore) getApplication();
            app.getTaItineraryController().refreshItineraries(expRep.reportKey, true);
            app.getFixedTravelAllowanceController().refreshFixedTravelAllowances(expRep.reportKey);

//          Register Listener for Itinerary data and make button visible
            this.itineraryController = app.getTaItineraryController();
            this.itineraryController.registerListener(this);

//          Make button visible if itinerary table isn't empty.
            if (app.getTaItineraryController() != null  && app.getTaItineraryController().getItineraryList().size() > 0){
                showTravelAllowanceButton();
            }
            if (reportKeySource != Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
                showTravelAllowanceButton();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.itineraryController.unregisterListener(this);
    }

    private void showTravelAllowanceButton(){
        View vHeaderItinerary = this.findViewById(R.id.header_itinerary);
        if (vHeaderItinerary != null){
            vHeaderItinerary.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save 'RemoveReportExpenseReceiver'.
        if (removeReportExpenseReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            removeReportExpenseReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(REMOVE_REPORT_EXPENSE_RECEIVER_KEY, removeReportExpenseReceiver);
        }
        // Save 'selectedReportEntry'.
        if (selectedReportEntry != null) {
            retainer.put(SELECTED_REPORT_ENTRY_KEY, selectedReportEntry);
        }
        // Save 'ReportEntryDetailReceiver'.
        if (reportEntryDetailReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            reportEntryDetailReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(REPORT_ENTRY_DETAIL_RECEIVER_KEY, reportEntryDetailReceiver);
        }
        // Save 'ExpenseTypesReceiver'.
        if (expenseTypesReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            expenseTypesReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(EXPENSE_TYPES_RECEIVER_KEY, expenseTypesReceiver);
        }
        // Save 'EntryFormReceiver'.
        if (entryFormReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            entryFormReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(ENTRY_FORM_RECEIVER_KEY, entryFormReceiver);
        }
        // Save 'ReportDeleteReceiver'.
        if (reportDeleteReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            reportDeleteReceiver.setActivity(null);
            // Add to the retainer.
            retainer.put(REPORT_DELETE_RECEIVER_KEY, reportDeleteReceiver);
        }

        if (selectedExpenseType != null) {
            retainer.put(SELECTED_EXPENSE_TYPE, selectedExpenseType);
        }

        if (isTipsOverlayVisible) {
            // Save the time the user spent on this screen, but
            // perhaps put the app in the background.
            upTime += (System.nanoTime() - startTime) / 1000000000L; // Convert to seconds.
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();

        if (isTipsOverlayVisible) {
            startTime = System.nanoTime();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_OK);
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPostCreate(android.os.Bundle)
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        // Ensure a call through to super.
        super.onPostCreate(savedInstanceState);

        // NOTE: The expense types request and dialog showing has been moved to
        // the 'onPostCreate' method.
        // Previously it was being called from within the overridden
        // 'setExpenseReportWithoutBuildView'
        // method which was indirectly being called from the 'onCreate' call.
        // This was causing the issue
        // where the 'removeDialog' in the broadcast receiver was successfully
        // hiding the dialog when
        // the end-user performed an action from within the 'ExpenseEntry'
        // activity that resulted in the application
        // being shutdown (i.e., user with Droid X device taking a picture of a
        // receipt).
        // AVK: 9/5/2012.

        // We must have the expense types for the policy. Fire off a request now
        // if needed.
        if (expTypes == null && ConcurCore.isConnected()) {
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
            expTypes = expEntCache.getExpenseTypes(expRep.polKey);
            if (expTypes == null) {
                // Register an expense type receiver.
                registerExpenseTypesReceiver();
                // Make the request.
                ConcurService concurService = ConcurCore.getService();
                expenseTypesRequest = concurService.sendGetExpenseTypesRequest(getUserId(), expRep.polKey);
                // If the request couldn't be made, then unregister the
                // receiver.
                if (expenseTypesRequest == null) {
                    // Unregister the expense type receiver.
                    unregisterExpenseTypesReceiver();
                    expTypes = null;
                } else {
                    // Set the request on the expense types receiver.
                    expenseTypesReceiver.setRequest(expenseTypesRequest);
                    // Display an expense type retrieval dialog.
                    showDialog(Const.DIALOG_RETRIEVE_EXPENSE_TYPES);
                }
            }
        }
    }

    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
        // Restore 'RemoveReportExpenseReceiver'.
        if (retainer.contains(REMOVE_REPORT_EXPENSE_RECEIVER_KEY)) {
            removeReportExpenseReceiver = (RemoveReportExpenseReceiver) retainer
                    .get(REMOVE_REPORT_EXPENSE_RECEIVER_KEY);
            if (removeReportExpenseReceiver != null) {
                // Set the activity on the receiver.
                removeReportExpenseReceiver.setActivity(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".restoreReceivers: retainer contains null reference for remove report entry receiver!");
            }
        }
        // Restore 'ReportEntryDetailReceiver'.
        if (retainer.contains(REPORT_ENTRY_DETAIL_RECEIVER_KEY)) {
            reportEntryDetailReceiver = (ReportEntryDetailReceiver) retainer.get(REPORT_ENTRY_DETAIL_RECEIVER_KEY);
            if (reportEntryDetailReceiver != null) {
                // Set the activity on the receiver.
                reportEntryDetailReceiver.setActivity(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".restoreReceivers: retainer contains null reference for report entry detail receiver!");
            }
        }
        // Restore 'ExpenseTypesReceiver'.
        if (retainer.contains(EXPENSE_TYPES_RECEIVER_KEY)) {
            expenseTypesReceiver = (ExpenseTypesReceiver) retainer.get(EXPENSE_TYPES_RECEIVER_KEY);
            if (expenseTypesReceiver != null) {
                // Set the activity on the receiver.
                expenseTypesReceiver.setActivity(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".restoreReceivers: retainer contains null reference for expense types receiver!");
            }
        }
        // Restore 'EntryFormReceiver'.
        if (retainer.contains(ENTRY_FORM_RECEIVER_KEY)) {
            entryFormReceiver = (EntryFormReceiver) retainer.get(ENTRY_FORM_RECEIVER_KEY);
            if (entryFormReceiver != null) {
                // Set the activity on the receiver.
                entryFormReceiver.setActivity(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".restoreReceivers: retainer contains null reference for entry form receiver!");
            }
        }
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
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the list of expense types.
        if (expTypes != null) {
            StringBuilder strBldr = new StringBuilder();
            ExpenseType.ExpenseTypeSAXHandler.serializeToXML(strBldr, expTypes);
            outState.putString(EXPENSE_TYPES, strBldr.toString());
        }

        if (isTipsOverlayVisible) {
            // Save the uptime so we know how long the user has been on this screen,
            // even if it has been destroyed.
            outState.putLong(Const.ACTIVITY_STATE_UPTIME, upTime);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#buildView()
     */
    @Override
    protected void buildView() {

        // Set the content view.
        setContentView(R.layout.expense_entries);

        // Configure the screen header.
        configureScreenHeader(expRep);

        // Configure the screen footer.
        configureScreenFooter();

        // Set the expense title header information.
        populateReportHeaderInfo(expRep);

        // Populate the expense list header row.
        populateExpenseHeaderView(expRep);

        // Populate the expense list entry rows.
        LinearLayout expEntriesView = (LinearLayout) findViewById(R.id.entries_list);
        populateExpenseEntriesView(expRep, expEntriesView);
        View linearLayoutView = findViewById(R.id.linear_layout);
        linearLayoutView.requestLayout();

        boolean isTestDriveUser = Preferences.isTestDriveUser();
        boolean expRepIsSubmitted = expRep.isSubmitted();

        // Send whether or not report has been submitted to determine which overlay to use
        if (isTestDriveUser) {

            View navBarFooter = findViewById(R.id.expense_nav_bar_footer);

            if (expRepIsSubmitted
                    && Preferences.shouldShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_EXPENSE_ENTRIES_SUBMITTED)) {

                TextView approveButton = (TextView) findViewById(R.id.approve_button);
                TextView rejectButton = (TextView) findViewById(R.id.reject_button);

                if ((approveButton != null && (approveButton.getVisibility() != View.INVISIBLE))
                        && (rejectButton != null && (rejectButton.getVisibility() != View.INVISIBLE))) {
                    showTestDriveTips(expRepIsSubmitted);
                }
            } else if (!expRepIsSubmitted
                    && Preferences.shouldShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_EXPENSE_ENTRIES)
                    && (navBarFooter != null && (navBarFooter.getVisibility() != View.GONE))) {
                showTestDriveTips(expRepIsSubmitted);
            }
        }

        // MOB-17302 Add click listener to header to view the Report Summary.
        View titleHeader = findViewById(R.id.title_header);
        if (titleHeader != null) {
            Intent clickIntent = new Intent(this, ExpenseReportHeader.class);
            clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
            clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
            viewClickHandler.addViewLauncherForResult(titleHeader, clickIntent, REQUEST_VIEW_SUMMARY);
            titleHeader.setOnClickListener(viewClickHandler);
        }
    }

    /**
     * Because ExpenseEntries is responsible for both submitted and unsubmitted reports, we have an {@code expRepIsSubmitted} flag
     * throughout and we show the correct overlay based on its value. We keep separate flags for submitted and unsubmitted entries
     * test drive overlays so the user can see both tips overlays on their respective screens.
     * 
     * @param expRepIsSubmitted
     *            A flag to indicate whether we're dealing with an Expense Report or an Approval.
     */
    protected void showTestDriveTips(final boolean expRepIsSubmitted) {
        // Which overlay we use depends on if we're in a Report or an Approval
        int overlayResId = expRepIsSubmitted ? R.layout.td_overlay_expense_entries_approval
                : R.layout.td_overlay_expense_entries;

        // OnClickListener whose onClick() is called from setupOverlay()
        OnClickListener dismissListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (expRepIsSubmitted) {
                    Preferences.setShouldNotShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_EXPENSE_ENTRIES_SUBMITTED);
                } else {
                    Preferences.setShouldNotShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_EXPENSE_ENTRIES);
                }

                isTipsOverlayVisible = false;

                // Analytics stuff.
                Map<String, String> flurryParams = new HashMap<String, String>();
                upTime = ((System.nanoTime() - startTime) / 1000000000L) + upTime; // Convert nanoseconds to seconds.
                flurryParams.put(Flurry.PARAM_NAME_SECONDS_ON_OVERLAY, Flurry.formatDurationEventParam(upTime));
                EventTracker.INSTANCE.track(Flurry.CATEGORY_OVERLAYS,
                        (expRepIsSubmitted ? "Approve Report" : "Report Details"), flurryParams);
            }
        };

        UIUtils.setupOverlay((ViewGroup) getWindow().getDecorView(), overlayResId, dismissListener,
                R.id.td_icon_cancel_button, this, R.anim.fade_out, 300L);

        isTipsOverlayVisible = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onCreateDialog (int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case Const.DIALOG_EXPENSE_REPORT_REMOVE_EXPENSE_ENTRY_CONFIRM: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getText(R.string.dlg_expense_remove_confirmation_title));
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    handleRemoveExpenseEntry(selectedReportEntry);
                }
            });
            dlgBldr.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    selectedReportEntry = null;
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        case Const.DIALOG_EXPENSE_REMOVE_REPORT_EXPENSE_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_remove_report_expense_progress));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (removeReportExpenseRequest != null) {
                        // Cancel the request.
                        removeReportExpenseRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: removeReportExpenseRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_REPORT_ENTRY_DETAIL_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_expense_retrieve_report_entry_detail_progress));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (reportEntryDetailRequest != null) {
                        // Cancel the request.
                        reportEntryDetailRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: reportEntryDetailRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_REPORT_ENTRY_DETAIL_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_expense_retrieve_report_entry_detail_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }

        case Const.DIALOG_SELECT_EXPENSE_TYPE: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.expense_type_prompt);

            expTypeAdapter = new ExpenseTypeSpinnerAdapter(this, null);
            expTypeAdapter.setExpenseTypes(expTypes, null, true, false);
            expTypeAdapter.setUseDropDownOnly(true);

            LayoutInflater inflater = LayoutInflater.from(this);
            View customView = inflater.inflate(R.layout.expense_mru, null);

            ListView customListView = (ListView) customView.findViewById(R.id.list_expense_mru);
            EditText customEditText = (EditText) customView.findViewById(R.id.list_search_mru);
            customListView.setAdapter(expTypeAdapter);

            customListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            builder.setView(customView);

            customEditText.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    expTypeAdapter.clearSearchFilter();
                    expTypeAdapter.getFilter().filter(s);

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // TODO Auto-generated method stub

                }
            });

            customListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                    dismissDialog(Const.DIALOG_SELECT_EXPENSE_TYPE);
                    selectedExpenseType = (ExpenseType) expTypeAdapter.getItem(which);
                    if (selectedExpenseType != null) {
                        // Retrieve the form
                        ConcurCore ConcurCore = (ConcurCore) getApplication();
                        // Register the receiver to handle the result.
                        registerEntryFormReceiver();
                        ConcurService concurService = ConcurCore.getService();
                        entryFormRequest = concurService.sendReportEntryFormRequest(selectedExpenseType.key,
                                expRep.reportKey, null);
                        if (entryFormRequest == null) {
                            // Unregister the receiver.
                            unregisterEntryFormReceiver();
                            // TODO: Need a dialog
                        } else {
                            // Set the request object on the receiver.
                            entryFormReceiver.setRequest(entryFormRequest);
                            showDialog(Const.DIALOG_EXPENSE_ENTRY_FORM);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: selectedExpenseType is null!");
                    }
                }
            });

            AlertDialog alertDlg = builder.create();
            dialog = alertDlg;
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    removeDialog(Const.DIALOG_SELECT_EXPENSE_TYPE);
                }
            });
            break;
        }
        case Const.DIALOG_RETRIEVE_EXPENSE_TYPES: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.retrieve_expense_types));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(false);
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_ENTRY_FORM: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(this.getText(R.string.dlg_retrieving_expense_form));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (entryFormRequest != null) {
                        entryFormRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog.onCancel: null entry form request!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
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
            return dlgBldr.create();
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
        default:
            dialog = super.onCreateDialog(id);
            break;
        }
        return dialog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onPrepareDialog (int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case Const.DIALOG_EXPENSE_REPORT_REMOVE_EXPENSE_ENTRY_CONFIRM: {
            if (selectedReportEntry != null) {
                Object[] values = {
                        selectedReportEntry.expenseName,
                        selectedReportEntry.getFormattedTransactionDate(),
                        FormatUtil.formatAmount(selectedReportEntry.transactionAmount, getResources()
                                .getConfiguration().locale, selectedReportEntry.transactionCrnCode, true, true) };
                String msg = Format.localizeText(this, R.string.dlg_expense_remove_confirmation_message, values);
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(msg);
            }
            break;
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_REPORT_ENTRY_DETAIL_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case Const.DIALOG_EXPENSE_DELETE_REPORT_CONFIRM: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (expRep != null) {
                alertDlg.setMessage(Format.localizeText(this, R.string.dlg_expense_confirm_report_delete_message,
                        expRep.reportName));
            }
            break;
        }
        case Const.DIALOG_EXPENSE_DELETE_REPORT_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        default:
            super.onPrepareDialog(id, dialog);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        boolean handled = super.onContextItemSelected(item);
        if (!handled) {
            if (item.getItemId() == ExpenseEntryOption.RemoveFromReport.ordinal()) {
                // Check for connectivity, if none, then display dialog and
                // return.
                if (!ConcurCore.isConnected()) {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                } else {
                    showDialog(Const.DIALOG_EXPENSE_REPORT_REMOVE_EXPENSE_ENTRY_CONFIRM);
                }
                handled = true;
            } else if (item.getItemId() == ExpenseEntryOption.AttachCapturedReceipt.ordinal()) {
                if (ConcurCore.isConnected()) {
                    captureReportEntryReceipt();
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
                handled = true;
            } else if (item.getItemId() == ExpenseEntryOption.AttachSelectedGalleryReceipt.ordinal()) {
                if (ConcurCore.isConnected()) {
                    selectReportEntryReceipt();
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
                handled = true;
            } else if (item.getItemId() == ExpenseEntryOption.AttachSelectedCloudReceipt.ordinal()) {
                if (ConcurCore.isConnected()) {
                    selectCloudReportEntryReceipt();
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
                handled = true;
            } else if (item.getItemId() == ExpenseEntryOption.ViewReceipt.ordinal()) {
                if (selectedReportEntry != null) {
                    if (ConcurCore.isConnected()) {
                        Intent viewExpenseEntryReceiptIntent = buildExpenseEntryReceiptClickIntent(expRep.reportKey,
                                selectedReportEntry.reportEntryKey);
                        if (selectedReportEntry.receiptImageId != null) {
                            viewExpenseEntryReceiptIntent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY,
                                    selectedReportEntry.receiptImageId);
                        }
                        startActivity(viewExpenseEntryReceiptIntent);
                    } else {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                    // Clear the reference to the selected report entry.
                    clearSelectedExpenseReportEntry();
                } else {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onContextItemSelected: selectedReportEntry is null!");
                }
                handled = true;
            } else if (item.getItemId() == R.id.new_expense) {
                if (ConcurCore.isConnected()) {
                    onAddExpense();
                    trackAddExpenseEvent("Button");
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
            } else if (item.getItemId() == R.id.import_expenses) {
                if (ConcurCore.isConnected()) {
                    onImportExpenses();
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
            }
        }
        return handled;
    }

    /**
     * Will handle starting an activity "for result" via <code>intent</code> and with request code <code>requestCode</code> after
     * successfully retrieving a report entry detail object.
     * 
     * @param intent
     *            the intent to launch.
     * @param requestCode
     *            the request code.
     * @param reportKey
     *            the report key.
     * @param reportEntryKey
     *            the report entry key.
     * @param reportSourceKey
     *            the source key.
     */
    protected void handleStartActivityForResultAfterEntryDetailFetch(Intent intent, int requestCode, String reportKey,
            String reportEntryKey, int reportSourceKey) {

        if (ConcurCore.isConnected()) {
            // Register the receiver.
            registerReportEntryDetailReceiver();
            reportEntryDetailReceiver.setIntent(intent);
            reportEntryDetailReceiver.setRequestCode(requestCode);
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            ConcurService concurService = ConcurCore.getService();
            reportEntryDetailRequest = concurService.sendReportEntryDetailRequest(reportKey, reportEntryKey,
                    reportSourceKey);
            if (reportEntryDetailRequest != null) {
                // Set the request on the receiver.
                reportEntryDetailReceiver.setRequest(reportEntryDetailRequest);
                // Display a progress dialog.
                showDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_ENTRY_DETAIL_PROGRESS);
            } else {
                unregisterReportEntryDetailReceiver();
                Log.e(Const.LOG_TAG,
                        CLS_TAG
                                + ".handleStartActivityForResultAfterEntryDetailFetch: unable to send report entry detail request!");
            }
        } else {
            // Display the "no connectivity" dialog.
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View,
     * android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        if (view.getId() == R.id.menuAdd) {
            android.view.MenuInflater infl = getMenuInflater();
            menu.setHeaderTitle(R.string.add_expense);
            infl.inflate(R.menu.report_entries_add_expense, menu);
        } else {
            String rptEntKey = (String) view.getTag();
            final ExpenseReportEntry expRepEntry = expRep.findEntryByReportKey(rptEntKey);
            if (expRepEntry != null) {
                menu.setHeaderTitle(R.string.expense_entry_action);
                android.view.MenuItem menuItem = menu.add(0, ExpenseEntryOption.ViewDetails.ordinal(), 0,
                        R.string.view_details);
                // Set up a custom menuitem click listener in order to launch
                // the view entry details activity
                // for a result.
                final ExpenseReportEntry selExpRepEntry = expRepEntry;
                menuItem.setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(android.view.MenuItem item) {
                        Intent intent = buildExpenseEntryClickIntent(expRep.polKey, expRep.reportKey,
                                expRepEntry.reportEntryKey, expRepEntry.expKey);
                        if (!expRepEntry.isItemization()) {
                            if (expRepEntry.isDetail()) {
                                startActivityForResult(intent, REQUEST_VIEW_ENTRY_DETAILS);
                            } else {
                                selectedReportEntry = selExpRepEntry;
                                handleStartActivityForResultAfterEntryDetailFetch(intent, REQUEST_VIEW_ENTRY_DETAILS,
                                        expRepEntry.rptKey, expRepEntry.reportEntryKey, reportKeySource);
                            }
                        } else {
                            startActivityForResult(intent, REQUEST_VIEW_ENTRY_DETAILS);
                        }
                        return true;
                    }
                });
                selectedReportEntry = null;
                if ((expRepEntry.meKey != null && expRepEntry.hasMobileReceipt()) || expRepEntry.receiptImageId != null) {
                    menuItem = menu.add(0, ExpenseEntryOption.ViewReceipt.ordinal(), 0, R.string.view_receipt);
                    selectedReportEntry = expRepEntry;
                }

                // If the report is editable, then permit the remove operation.
                if (isReportEditable()) {
                    selectedReportEntry = expRepEntry;
                    int removeStringId = R.string.remove_from_report;
                    if (isItemizationExpense()) {
                        removeStringId = R.string.remove_from_itemization;
                    }
                    menuItem = menu.add(0, ExpenseEntryOption.RemoveFromReport.ordinal(), 0, removeStringId);
                }

                // If receipts are editable, then permit receipt operations.
                if (canEditReceipt()) {
                    if ((expRepEntry.parentReportEntryKey == null) && !isItemizationExpense()) {

                        // Add menu options for attach selected/captured
                        // receipt.
                        menuItem = menu.add(0, ExpenseEntryOption.AttachCapturedReceipt.ordinal(), 0,
                                R.string.capture_receipt_picture);
                        menuItem = menu.add(0, ExpenseEntryOption.AttachSelectedGalleryReceipt.ordinal(), 0,
                                R.string.select_gallery_receipt_picture);
                        // Check for whether Receipt Store access is enabled.
                        if (!ViewUtil.isReceiptStoreHidden(this)) {
                            menuItem = menu.add(0, ExpenseEntryOption.AttachSelectedCloudReceipt.ordinal(), 0,
                                    R.string.select_cloud_receipt_picture);
                        }
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateContextMenu: unable to locate expense report entry in map!");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean retVal = false;

        boolean editable = isReportEditable();
        boolean receiptEditable = canEditReceipt();
        boolean isChild = isItemizationExpense();

        if (editable || (receiptEditable && !isChild)) {
            // Blow up the menu
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.expense_report, menu);

            // Filter it
            if (!editable) {
                // No more entries allowed
                menu.removeItem(R.id.menuAdd);
                // Deletion not supported.
                menu.removeItem(R.id.report_delete);
            }
            if (!receiptEditable || isChild) {
                // hot fix for MOB-10897
                menu.removeItem(R.id.capture_receipt_picture);
                menu.removeItem(R.id.select_picture);// }

                // Check for whether Receipt Store access is enabled.
                // hot fix for MOB-10897
                if (ViewUtil.isReceiptStoreHidden(this)) {
                    MenuItem menuItem = menu.findItem(R.id.select_picture);
                    if (menuItem != null) {
                        SubMenu subMenu = menuItem.getSubMenu();
                        if (subMenu != null) {
                            subMenu.removeItem(R.id.select_receipt_cloud_picture);
                        }
                    }
                }
            }
            retVal = true;
        }
        return retVal;
    }

    /**
     * Starts the process of adding a new expense to the list of expenses.
     */
    protected void onAddExpense() {
        if (expTypes == null) {
            if (ConcurCore.isConnected()) {
                ConcurCore ConcurCore = (ConcurCore) getApplication();
                IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
                expTypes = expEntCache.getExpenseTypes(expRep.polKey);

                if (expTypes != null) {
                    // if(getListFromDB()!=null){
                    showDialog(Const.DIALOG_SELECT_EXPENSE_TYPE);
                    // }else{
                    // Something is really wrong. They should be there.
                    // Log.wtf(Const.LOG_TAG, CLS_TAG +
                    // ".onAddExpense: expense types from database are not available");
                    // }
                } else {
                    // Something is really wrong. They should be there.
                    Log.wtf(Const.LOG_TAG, CLS_TAG + ".onAddExpense: expense types are not available");
                }
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        } else {
            // Show the expense types list selection dialog.
            showDialog(Const.DIALOG_SELECT_EXPENSE_TYPE);
        }
    }

    protected void deleteReport() {
        showDialog(Const.DIALOG_EXPENSE_DELETE_REPORT_CONFIRM);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retVal = false;
        final int itemId = item.getItemId();
        if (itemId == R.id.menuAdd && isItemizationExpense()) {
            if (ConcurCore.isConnected()) {
                // NOTE: The sub-class 'ExpenseEntryItemization' extends this
                // class, re-uses the option menu
                // and re-names the 'menuAdd' menu to add an itemization.
                // However, since it re-uses
                // this method, the code below will call 'onAddExpense' upon
                // selection of the 'add itemization expense'
                // option menu item. Additionally, this code clears out the
                // contents of the sub-menu which would normally
                // present the option to either define a new expense or pick an
                // existing one. In the case of an itemization,
                // we want to treat it like the end-user has selected the option
                // to define a new expense; hence the call below
                // to the 'onAddExpense' method.
                onAddExpense();
                // Wipe out the sub-menu.
                if (item.hasSubMenu()) {
                    item.getSubMenu().clear();
                    item.getSubMenu().clearHeader();
                }
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
            retVal = true;
        } else if (itemId == R.id.new_expense) {
            if (ConcurCore.isConnected()) {
                onAddExpense();
                trackAddExpenseEvent("Plus");
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        } else if (itemId == R.id.import_expenses) {
            if (ConcurCore.isConnected()) {
                onImportExpenses();
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        } else if (itemId == R.id.report_delete) {
            if (ConcurCore.isConnected()) {
                showDialog(Const.DIALOG_EXPENSE_DELETE_REPORT_CONFIRM);
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
            retVal = true;
        } else if (itemId == R.id.capture_receipt_picture) {
            if (ConcurCore.isConnected()) {
                captureReportReceipt();
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
            retVal = true;
        } else if (itemId == R.id.select_receipt_picture) {
            if (ConcurCore.isConnected()) {
                selectReportReceipt();
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
            retVal = true;
        } else if (itemId == R.id.select_receipt_cloud_picture) {
            if (ConcurCore.isConnected()) {
                selectCloudReportReceipt();
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(R.id.capture_receipt_picture);
        menu.removeItem(R.id.select_picture);
        return super.onPrepareOptionsMenu(menu);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# showExceptionIconInHeader()
     */
    @Override
    protected boolean showExceptionIconInHeader() {
        return true;
    }

    public static void trackAddExpenseEvent(String addExpAction) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("From", addExpAction);

        EventTracker.INSTANCE.track(Flurry.CATEGORY_REPORTS, Flurry.EVENT_ADD_EXPENSE, params);

    }

    /**
     * Will handle starting the appropriate activity to permit expenses selection to be added to this report.
     */
    protected void onImportExpenses() {
        Intent intent = new Intent(this, ExpensesAndReceipts.class);
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_NAME, expRep.reportName);
        intent.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_REPORT_HEADER);
        startActivityForResult(intent, Const.REQUEST_CODE_ADD_EXPENSES);
    }

    /**
     * Will make the necessary calls to remove an expense entry from a report.
     * 
     * @param expRepEntry
     *            the expense report entry to be removed.
     */
    private void handleRemoveExpenseEntry(ExpenseReportEntry expRepEntry) {

        if (ConcurCore.isConnected()) {
            // Make a request to the service.
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            ConcurService concurService = ConcurCore.getService();
            ArrayList<String> expRepEntKeys = new ArrayList<String>();
            expRepEntKeys.add(expRepEntry.reportEntryKey);
            // Register a receiver.
            registerRemoveReportExpenseReceiver();
            removeReportExpenseRequest = concurService.sendRemoveReportExpenseRequest(getUserId(), expRep.reportKey,
                    expRepEntKeys);
            if (removeReportExpenseRequest != null) {
                // Set the request on the receiver.
                removeReportExpenseReceiver.setRequest(removeReportExpenseRequest);
                // Display a progress dialog.
                showDialog(Const.DIALOG_EXPENSE_REMOVE_REPORT_EXPENSE_PROGRESS);
            } else {
                // Unregister the receiver.
                unregisterRemoveReportExpenseReceiver();
                // TODO: Display a dialog indicating the request couldn't be
                // sent.
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getHeaderNavBarTitleResourceId()
     */
    @Override
    protected int getHeaderNavBarTitleResourceId() {
        return R.string.expense_entries_list_title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# showTitleBarActionButton()
     */
    @Override
    protected boolean showTitleBarActionButton() {
        return isReportEditable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onAddActionButton ()
     */
    @Override
    protected void onAddActionButton() {
        View actionButton = findViewById(R.id.menuAdd);
        if (actionButton != null) {
            if (!isItemizationExpense()) {
                registerForContextMenu(actionButton);
                openContextMenu(actionButton);
            } else {
                onAddExpense();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onAddActionButton: unable ot locate 'action_button' view!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isSubmitReportEnabled()
     */
    @Override
    protected boolean isSubmitReportEnabled() {
        return (isReportEditable());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isApproveReportEnabled()
     */
    @Override
    protected boolean isApproveReportEnabled() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isRejectReportEnabled()
     */
    @Override
    protected boolean isRejectReportEnabled() {
        return true;
    }

    @Override
    protected boolean isExportReportEnabled() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isDetailReportRequired()
     */
    @Override
    protected boolean isDetailReportRequired() {
        boolean retVal = false;
        // Active reports require a detailed report in order to make the proper
        // decision regarding whether a submit is permitted.
        retVal = (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE);
        return retVal;
    }

    /**
     * Gets the list of <code>ExpenseReportEntry</code> objects that should be displayed within this view.
     * 
     * @return the list of <code>ExpenseReportEntry</code> objects.
     */
    protected ArrayList<ExpenseReportEntry> getExpenseEntries() {
        ArrayList<ExpenseReportEntry> entries = null;
        if (expRep != null) {
            entries = expRep.getExpenseEntries();
        }
        return entries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#getReportEntryKey ()
     */
    @Override
    protected String getReportEntryKey() {
        String reportEntryKey = "";
        if (selectedReportEntry != null) {
            reportEntryKey = selectedReportEntry.reportEntryKey;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getReportEntryKey: selectedReportEntry is null!");
        }
        return reportEntryKey;
    }

    @Override
    protected String getFormattedReportEntryAmount() {
        String formattedAmountStr = null;
        if (selectedReportEntry != null) {
            formattedAmountStr = FormatUtil.formatAmount(selectedReportEntry.transactionAmount, getResources()
                    .getConfiguration().locale, selectedReportEntry.transactionCrnCode, true, true);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getFormattedReportEntryAmount: selectedReportEntry is null!");
        }
        return formattedAmountStr;
    }

    @Override
    protected String getReportEntryName() {
        String entryName = null;
        if (selectedReportEntry != null) {
            entryName = selectedReportEntry.expenseName;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getReportEntryName: selectedReportEntry is null!");
        }
        return entryName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseActivity# getReportEntryReceiptImageId()
     */
    @Override
    protected String getReportEntryReceiptImageId() {
        String receiptImageId = null;
        if (selectedReportEntry != null) {
            receiptImageId = selectedReportEntry.receiptImageId;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getReportEntryReceiptImageId: selectedReportEntry is null!");
        }
        return receiptImageId;
    }

    /**
     * Will set information on the expense header view.
     * 
     * @param expRep
     *            the expense report.
     */
    protected void populateExpenseHeaderView(final ExpenseReport expRep) {

        // Set click-handler on report summary view.
        View view = findViewById(R.id.header_report_summary);
        if (view != null) {
            view.setFocusable(true);
            view.setClickable(true);
            // Add a view click listener to view expense header information.
            Intent clickIntent = new Intent(this, ExpenseReportHeader.class);
            clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
            clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
            viewClickHandler.addViewLauncherForResult(view, clickIntent, REQUEST_VIEW_SUMMARY);
            view.setOnClickListener(viewClickHandler);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseHeaderView: unable to locate header report summary view!");
        }

        // Set the click-handler on the view/attach receipt view.
        view = findViewById(R.id.header_view_attach_receipts);
        if (view != null) {
            // Check for an active report or an approval with receipt.
            if ((reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE)
                    || (reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL && expRep
                            .isReceiptImageAvailable())) {
                boolean showReceipts = true;
                view.setFocusable(true);
                view.setClickable(true);
                // Set the text resource id.
                // REF: MOB-10897
                int viewReceiptsStrResId = R.string.receipts;
                if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE) {
                    if (canEditReceipt()) {
                        if (!expRep.isReceiptImageAvailable()) {
                            viewReceiptsStrResId = R.string.receipts;
                        }
                    } else if (!expRep.isReceiptImageAvailable()) {
                        // Report is active, receipt editing is disabled and
                        // there is no receipt.
                        showReceipts = false;
                    }
                }
                if (showReceipts) {
                    TextView txtView = (TextView) view.findViewById(R.id.view_receipts);
                    if (txtView != null) {
                        txtView.setText(viewReceiptsStrResId);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".populateExpenseHeaderView: unable to locate view/attach receipt text view!");
                    }
                    // Set the click intent.
                    if (reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
                        // Add a view click listener to view expense receipt
                        // information.
                        Intent clickIntent = new Intent(this, ExpenseReceipt.class);
                        clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
                        clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
                        viewClickHandler.addViewLauncher(view, clickIntent);
                        view.setOnClickListener(viewClickHandler);
                    } else {
                        view.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                if (!ViewUtil.isExternalMediaMounted()) {
                                    showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
                                } else {
                                    showDialog(DIALOG_RECEIPT_IMAGE);
                                }
                            }
                        });
                    }
                } else {
                    // Hide the view/attach receipt button.
                    view.setVisibility(View.GONE);
                }
            } else {
                // Hide the view/attach receipt button.
                view.setVisibility(View.GONE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".populateExpenseHeaderView: unable to locate header view/attach receipt view!");
        }

        // Set click-handler on itinerary view
        view = findViewById(R.id.header_itinerary);
        if (view != null) {
            // hide if we don't have the setting
            if (!ViewUtil.hasFixedTA(this)) {
                view.setVisibility(View.GONE);
            }
            view.setFocusable(true);
            view.setClickable(true);
            // Add a view click listener to start TA flow
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent;
                    if (reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
                        intent = new Intent(ExpenseEntries.this, TravelAllowanceActivity.class);
                        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
                        startActivityForResult(intent, REQUEST_VIEW_TA_ITINERARY);
                    } else {
                        ConcurCore app = (ConcurCore) getApplication();
                        if (app.getTaItineraryController() != null) {
                            if (app.getTaItineraryController().getItineraryList() == null
                                    || app.getTaItineraryController().getItineraryList().size() == 0) {
                                intent = new Intent(ExpenseEntries.this, CreateItineraryActivity.class);
                                intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
                                startActivityForResult(intent, REQUEST_VIEW_TA_ITIN_CREATE);
                            }
                        }
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseHeaderView: unable to locate header report summary view!");
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIEW_TA_ITINERARY) {
            if (resultCode == Activity.RESULT_OK) {

                // Register the receiver.
                registerReportDetailReceiver();

                showDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL);

                reportDetailRequest = ((ConcurCore) getApplication()).getService().sendReportDetailSummaryRequest(
                        expRep.reportKey, reportKeySource);

                if (reportDetailRequest != null) {
                    // Set the request on the receiver.
                    reportDetailReceiver.setRequest(reportDetailRequest);
                } else {
                    // Unregister the receiver.
                    unregisterReportDetailReceiver();
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult: unable to create report detail request!");
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Will populate a list of expense report entry views into a <code>LinearLayout</code> object.
     * 
     * @param expRep
     *            the <code>ExpenseReport</code> containing the expense entries.
     * @param expEntriesView
     *            the <code>LinearLayout</code> to contain the inflated views.
     */
    protected void populateExpenseEntriesView(ExpenseReport expRep, LinearLayout expEntriesView) {

        // Ensure the view to expense report entry map is cleared.
        entryListAdapter = new ExpenseReportEntryListAdapter(expRep, getExpenseEntries(), this);
        for (int entryInd = 0; entryInd < entryListAdapter.getCount(); ++entryInd) {
            View view = entryListAdapter.getView(entryInd, null, expEntriesView);
            if (view != null) {
                // Add a small separator view.
                if (entryInd != 0) {
                    ViewUtil.addSeparatorView(this, expEntriesView);
                }
                view.setFocusable(true);
                view.setClickable(true);
                expEntriesView.addView(view);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseEntriesView: can't create view!");
            }
        }

        // Hide the long press message if there are no expense entries and the
        // entries container.
        if (entryListAdapter.getCount() == 0) {
            // Hide the long-press section.
            TextView txtView = (TextView) findViewById(R.id.long_press_msg_view);
            if (txtView != null) {
                txtView.setVisibility(View.GONE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".populateExpenseEntriesView: unable to locate long press message text view!");
            }
            // Hide the entries section.
            View view = findViewById(R.id.entries);
            if (view != null) {
                view.setVisibility(View.GONE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseEntriesView: unable to locate entries container!");
            }

            Button button = (Button) findViewById(R.id.add_expense);
            // Show the no data section.
            view = findViewById(R.id.no_entries);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
                // Add an on-click handler for the "add expenses" button.
                if (button != null) {
                    if (!expRep.isSubmitted()) {
                        button.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                onAddActionButton();
                            }
                        });
                    } else {
                        button.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateExpenseEntriesView: unable to locate 'add expense' button!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseEntriesView: unable to locate 'no entries' container!");
            }
            // Also, if this report is an active report, then no need to show
            // footer as end-user won't be able to submit.
            if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE) {
                view = findViewById(R.id.expense_nav_bar_footer);
                if (view != null) {
                    view.setVisibility(View.GONE);

                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getBroadcastReceiverIntentFilter()
     */
    @Override
    protected IntentFilter getBroadcastReceiverIntentFilter() {
        return new IntentFilter(Const.ACTION_EXPENSE_REPORT_DETAIL_UPDATED);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# shouldReceiveDataEvents()
     */
    @Override
    protected boolean shouldReceiveDataEvents() {
        return true;
    }

    @Override
    protected boolean shouldListenForNetworkActivity() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# shouldRetrieveDetailedReport()
     */
    @Override
    protected boolean shouldPrefetchDetailedReport() {
        boolean retVal = false;
        // Only approvals pre-fetch for detailed reports, "active" reports
        // require the
        // detailed report.
        retVal = (reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL);
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isReportLevelReceiptDialog()
     */
    @Override
    protected boolean isReportLevelReceiptDialog() {
        return true;
    }

    /**
     * Will construct an instance of <code>Intent</code> used to start the <code>ExpenseEntry</code> activity.
     * 
     * @param reportKey
     *            the report key.
     * @param reportEntryKey
     *            the report extra key.
     * @param expKey
     * 
     * @return an instance of <code>Intent</code> used to launch the <code>ExpenseEntry</code>
     */
    protected Intent buildExpenseEntryClickIntent(String polKey, String reportKey, String reportEntryKey, String expKey) {
        Intent clickIntent = null;

        ExpenseType et = ExpenseType.findExpenseType(polKey, expKey);
        String expCode = null;
        if (et != null) {
            expCode = et.expCode;
        }

        // Check the code if we have it, otherwise fall back to the type
        if ((expCode != null && Const.EXPENSE_CODE_PERSONAL_MILEAGE.equals(expCode))
                || Const.EXPENSE_TYPE_MILEAGE.equals(expKey)) {

            clickIntent = new Intent(this, ExpenseEntryMileage.class);

        } else if ((expCode != null && Const.EXPENSE_CODE_COMPANY_MILEAGE.equals(expCode))
                || Const.EXPENSE_TYPE_COMPANY_MILEAGE.equals(expKey)) {

            clickIntent = new Intent(this, ExpenseEntryCompanyMileage.class);

        } else {
            clickIntent = new Intent(this, ExpenseEntry.class);
        }

        clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, reportKey);
        clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, reportEntryKey);
        clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
        if (selectedExpenseType != null) {
            clickIntent.putExtra(Const.EXTRA_EXPENSE_TYPE_HAS_TAX_FORM, selectedExpenseType.hasTaxForm);
        } else if (et != null) {
            clickIntent.putExtra(Const.EXTRA_EXPENSE_TYPE_HAS_TAX_FORM, et.hasTaxForm);
        }

        return clickIntent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getSelectedExpenseReportEntry()
     */
    @Override
    protected ExpenseReportEntry getSelectedExpenseReportEntry() {
        return selectedReportEntry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#
     * setSelectedExpenseReportEntry(com.concur.mobile.data.expense .ExpenseReportEntry)
     */
    @Override
    protected void setSelectedExpenseReportEntry(ExpenseReportEntry expRepEnt) {
        selectedReportEntry = expRepEnt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# clearSelectedExpenseReportEntry()
     */
    @Override
    protected void clearSelectedExpenseReportEntry() {
        selectedReportEntry = null;
    }

    /**
     * Provides a list adapter to populate expense entry information.
     * 
     * @author AndrewK
     */
    class ExpenseReportEntryListAdapter extends BaseAdapter {

        private final ExpenseReport expRep;

        private final ArrayList<ExpenseReportEntry> expRepEntries;

        /**
         * Constructs an instance of <code>ExpenseReportEntryListAdapter</code> with an expense report.
         * 
         * @param expRep
         *            the expense report backing this adapter.
         */
        ExpenseReportEntryListAdapter(ExpenseReport expRep, ArrayList<ExpenseReportEntry> expRepEntries, Context context) {
            super();
            this.expRep = expRep;
            this.expRepEntries = expRepEntries;
            if (this.expRepEntries != null) {
                Collections.sort(this.expRepEntries, new ReportEntryComparator(SortOrder.DESCENDING));
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            int count = 0;
            if (expRep != null) {
                if (expRepEntries != null) {
                    count = expRepEntries.size();
                }
            }
            return count;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            ExpenseReportEntry expRepEntry = null;
            if (expRepEntries != null) {
                expRepEntry = expRepEntries.get(position);
            }
            return expRepEntry;
        }

        /**
         * Will set the item at the given position.
         * 
         * @param object
         *            the item.
         * @param position
         *            the position.
         */
        public void setItem(ExpenseReportEntry entry, int position) {
            if (expRepEntries != null && expRepEntries.size() > position) {
                expRepEntries.set(position, entry);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;

            ExpenseReportEntry expRepEntry = (ExpenseReportEntry) getItem(position);
            if (expRepEntry != null) {

                view = buildExpenseEntryRowView(expRepEntry);
                view.setId(position);
                view.setTag(expRepEntry.reportEntryKey);

                // Add a view click listener.
                if (!expRepEntry.isItemization()) {
                    // Regular entry
                    final String polKey = expRep.polKey;
                    final String reportKey = expRepEntry.rptKey;
                    final String reportEntryKey = expRepEntry.reportEntryKey;
                    view.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // Set the selected report entry by looking up the
                            // current entry within the
                            // caches.
                            String rptEntKey = (String) v.getTag();
                            ExpenseReport cacheRep = expRepCache.getReportDetail(reportKey);
                            selectedReportEntry = cacheRep.findEntryByReportKey(rptEntKey);

                            final Intent clickIntent = buildExpenseEntryClickIntent(polKey, reportKey, reportEntryKey,
                                    selectedReportEntry.expKey);
                            // Outstanding request to refresh the report?
                            if (!isReportDetailRequestOutstanding()) {
                                if (!selectedReportEntry.isDetail()) {
                                    // Fetch the detailed report entry first,
                                    // then permit editing.
                                    handleStartActivityForResultAfterEntryDetailFetch(clickIntent,
                                            REQUEST_VIEW_ENTRY_DETAILS, reportKey, reportEntryKey, reportKeySource);
                                } else {
                                    // Permit immediate editing.
                                    startActivityForResult(clickIntent, REQUEST_VIEW_ENTRY_DETAILS);
                                }
                            } else {
                                // Outstanding request to refresh a report, so
                                // let that complete, then queue up
                                // a request to fetch a the detailed report
                                // entry.
                                handleStartActivityForResultAfterEntryDetailFetch(clickIntent,
                                        REQUEST_VIEW_ENTRY_DETAILS, reportKey, reportEntryKey, reportKeySource);
                            }
                        }
                    });

                } else {
                    // Child entry
                    // We should be fine to build the intent now since we
                    // shouldn't be able to use mileage types as children.
                    final Intent clickIntent = buildExpenseEntryClickIntent(expRep.polKey, expRep.reportKey,
                            expRepEntry.reportEntryKey, expRepEntry.expKey);

                    viewClickHandler.addViewLauncherForResult(view, clickIntent, REQUEST_VIEW_ENTRY_DETAILS);
                    view.setOnClickListener(viewClickHandler);
                }

                // Add a long-press listener.
                registerForContextMenu(view);
            }
            return view;
        }

    }

    /**
     * Will register with the application context an instance of <code>ReportEntryDetailReceiver</code> and set
     * <code>reportEntryDetailReceiver</code>.
     */
    protected void registerReportEntryDetailReceiver() {
        if (reportEntryDetailReceiver == null) {
            reportEntryDetailReceiver = new ReportEntryDetailReceiver(this);
            getApplicationContext().registerReceiver(reportEntryDetailReceiver, reportEntryDetailFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerReportEntryDetailReceiver: reportEntryDetailReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context an instance of <code>ReportEntryDetailReceiver</code> referenced by
     * <code>reportEntryDetailReceiver</code> and setting it to <code>null</code>.
     */
    protected void unregisterReportEntryDetailReceiver() {
        if (reportEntryDetailReceiver != null) {
            getApplicationContext().unregisterReceiver(reportEntryDetailReceiver);
            reportEntryDetailReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterReportEntryDetailReceiver: reportEntryDetailReceiver is null!");
        }
    }

    /**
     * Will register with the application context an instance of <code>RemoveItemizedExpenseReceiver</code> and set
     * <code>removeReportExpenseReceiver</code>.
     */
    protected void registerRemoveReportExpenseReceiver() {
        if (removeReportExpenseReceiver == null) {
            removeReportExpenseReceiver = createRemoveReportExpenseReceiver();
            getApplicationContext().registerReceiver(removeReportExpenseReceiver, removeReportExpenseFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerRemoveExpenseReceiver: removeReportExpenseReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context an instance of <code>ExpenseTypesReceiver</code> referenced by
     * <code>removeReportExpenseReceiver</code> and setting it to <code>null</code>.
     */
    protected void unregisterRemoveReportExpenseReceiver() {
        if (removeReportExpenseReceiver != null) {
            getApplicationContext().unregisterReceiver(removeReportExpenseReceiver);
            removeReportExpenseReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterRemoveExpenseReceiver: removeReportExpenseReceiver is null!");
        }
    }

    /**
     * Will register with the application context an instance of <code>ExpenseTypesReceiver</code> and set
     * <code>expenseTypesReceiver</code>.
     */
    private void registerExpenseTypesReceiver() {
        if (expenseTypesReceiver == null) {
            expenseTypesReceiver = new ExpenseTypesReceiver(this);
            getApplicationContext().registerReceiver(expenseTypesReceiver, expenseTypesFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerExpenseTypesReceiver: expenseTypesReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context an instance of <code>ExpenseTypesReceiver</code> referenced by
     * <code>expenseTypesReceiver</code> and setting it to <code>null</code>.
     */
    private void unregisterExpenseTypesReceiver() {
        if (expenseTypesReceiver != null) {
            getApplicationContext().unregisterReceiver(expenseTypesReceiver);
            expenseTypesReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterExpenseTypesReceiver: expenseTypesReceiver is null!");
        }
    }

    /**
     * Will register with the application context an instance of <code>EntryFormReceiver</code> and set
     * <code>entryFormReceiver</code>.
     */
    private void registerEntryFormReceiver() {
        if (entryFormReceiver == null) {
            entryFormReceiver = new EntryFormReceiver(this);
            getApplicationContext().registerReceiver(entryFormReceiver, entryFormFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerEntryFormReceiver: entryFormReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context an instance of <code>EntryFormReceiver</code> referenced by
     * <code>entryFormReceiver</code> and setting it to <code>null</code>.
     */
    private void unregisterEntryFormReceiver() {
        if (entryFormReceiver != null) {
            getApplicationContext().unregisterReceiver(entryFormReceiver);
            entryFormReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterEntryFormReceiver: entryFormReceiver is null!");
        }
    }

    /**
     * Constructs an instance of <code>RemoveReportExpenseReceiver</code> to handle the result of removing an expense from a
     * report.
     * 
     * @return an instance of <code>RemoveReportExpenseReceiver</code> to handle the result of removing an expense from a report.
     */
    protected RemoveReportExpenseReceiver createRemoveReportExpenseReceiver() {
        return new RemoveReportExpenseReceiver(this);
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle the result of an attempt to retrieve a report entry detail object.
     * 
     * @author andy
     */
    protected static class ReportEntryDetailReceiver extends BroadcastReceiver {

        final String CLS_TAG = ExpenseEntries.CLS_TAG + "." + ReportEntryDetailReceiver.class.getSimpleName();

        // A reference to the activity.
        protected ExpenseEntries activity;

        // A reference to the exchange rate request.
        protected ReportEntryDetailRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        protected Intent intent;

        // Contains the intent to launch "for result" the activity to view
        // report entry details.
        protected Intent launchIntent;

        // Contains the request code used to launch "for result" the activity to
        // view
        // report entry details.
        protected int requestCode;

        /**
         * Constructs an instance of <code>ReportEntryDetailReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReportEntryDetailReceiver(ExpenseEntries activity) {
            this.activity = activity;
        }

        /**
         * Sets the request code used to launch "for result" the activity to view report entry details.
         * 
         * @param requestCode
         *            the request code.
         */
        public void setRequestCode(int requestCode) {
            this.requestCode = requestCode;
        }

        /**
         * Sets the intent used to launch "for result" the activity to view report entry details.
         * 
         * @param intent
         *            the launch intent.
         */
        public void setIntent(Intent intent) {
            launchIntent = intent;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseEntries activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.reportEntryDetailRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the report entry detail request object associated with this broadcast receiver.
         * 
         * @param request
         *            the report entry detail request object associated with this broadcast receiver.
         */
        void setRequest(ReportEntryDetailRequest request) {
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
                activity.unregisterReportEntryDetailReceiver();

                try {
                    // Dismiss the dialog.
                    activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_ENTRY_DETAIL_PROGRESS);
                } catch (IllegalArgumentException ilaExc) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                }

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                // Display the error dialog if the removal
                                // failed.
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    if (launchIntent != null) {

                                        String rptEntKey = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                                        if (rptEntKey != null) {
                                            View entriesList = activity.findViewById(R.id.entries_list);
                                            if (entriesList != null) {
                                                View rptEntView = entriesList.findViewWithTag(rptEntKey);
                                                if (rptEntView != null) {
                                                    ExpenseReportEntry expRepEnt = activity.expRep
                                                            .findEntryByReportKey(rptEntKey);
                                                    if (expRepEnt != null) {
                                                        // Update the view.
                                                        activity.updateExpenseEntryRowView(rptEntView, expRepEnt);
                                                        // Update the adapter.
                                                        if (activity.entryListAdapter != null) {
                                                            activity.entryListAdapter.setItem(expRepEnt,
                                                                    rptEntView.getId());
                                                        } else {
                                                            Log.e(Const.LOG_TAG, CLS_TAG
                                                                    + ".onReceive: entry list adapter is null!");
                                                        }
                                                    } else {
                                                        Log.e(Const.LOG_TAG,
                                                                CLS_TAG
                                                                        + ".onReceive: unable to locate report entry in report!");
                                                    }
                                                } else {
                                                    Log.e(Const.LOG_TAG, CLS_TAG
                                                            + ".onReceive: unable to locate report entry view!");
                                                }
                                            } else {
                                                Log.e(Const.LOG_TAG, CLS_TAG
                                                        + ".onReceive: unable to locate entries list!");
                                            }
                                        } else {
                                            Log.e(Const.LOG_TAG, CLS_TAG
                                                    + ".onReceive: intent missing report entry key!");
                                        }
                                        // Launch the activity.
                                        activity.startActivityForResult(launchIntent, requestCode);
                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: launchIntent is null!");
                                    }
                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);
                                    // Display an error dialog.
                                    activity.showDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_ENTRY_DETAIL_FAILED);
                                }
                            } else {
                                activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage);
                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        // MOB-14269
                        // If loading an ExpenseEntry was cancelled, don't show System Unavailable message.
                        if (activity.reportEntryDetailRequest != null
                                && !activity.reportEntryDetailRequest.isCanceled()) {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + activity.lastHttpErrorMessage);
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }
                // Clear the request reference.
                activity.reportEntryDetailRequest = null;

                // Clear the currently selected expense entry.
                activity.selectedReportEntry = null;

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling the result of removing an expense entry from a report.
     * 
     * @author AndrewK
     */
    protected static class RemoveReportExpenseReceiver extends BroadcastReceiver {

        final String CLS_TAG = ExpenseEntries.CLS_TAG + "." + RemoveReportExpenseReceiver.class.getSimpleName();

        // A reference to the activity.
        protected ExpenseEntries activity;

        // A reference to the exchange rate request.
        protected RemoveReportExpenseRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        protected Intent intent;

        /**
         * Constructs an instance of <code>RemoveReportExpenseReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        RemoveReportExpenseReceiver(ExpenseEntries activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseEntries activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.removeReportExpenseRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the remove report expense request object associated with this broadcast receiver.
         * 
         * @param request
         *            the remove report expense request object associated with this broadcast receiver.
         */
        void setRequest(RemoveReportExpenseRequest request) {
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
                activity.unregisterRemoveReportExpenseReceiver();

                try {
                    // Dismiss the dialog.
                    activity.dismissDialog(Const.DIALOG_EXPENSE_REMOVE_REPORT_EXPENSE_PROGRESS);
                } catch (IllegalArgumentException ilaExc) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                }

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                // Display the error dialog if the removal
                                // failed.
                                if (!intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);
                                    // Display an error dialog.
                                    activity.showDialog(Const.DIALOG_EXPENSE_REMOVE_REPORT_EXPENSE_FAILED);
                                } else {
                                    // Flurry Notification
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_REPORT_ENTRY);
                                    EventTracker.INSTANCE.track(Flurry.CATEGORY_DELETE, Flurry.EVENT_NAME_ACTION,
                                            params);
                                }
                                // If a new detailed report was fetched, then
                                // update the display.
                                if (intent.getBooleanExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, false)) {
                                    // The result is a report key for a detailed
                                    // report.
                                    String reportKey = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
                                    if (reportKey != null && reportKey.length() > 0) {
                                        activity.expRep = activity.expRepCache.getReportDetail(reportKey);
                                        if (activity.expRep != null) {
                                            // Re-build the view.
                                            activity.buildView();
                                            // Set the report list refresh flag.
                                            activity.expRepCache.setShouldRefreshReportList();

                                            // Set the expense list refetch
                                            // flag.
                                            ConcurCore ConcurCore = (ConcurCore) activity.getApplication();
                                            IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
                                            expEntCache.setShouldFetchExpenseList();
                                        } else {
                                            Log.e(Const.LOG_TAG, CLS_TAG
                                                    + ".onReceive: unable to locate detailed report for report key '"
                                                    + reportKey + "'.");
                                        }
                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: null/empty report key!");
                                    }
                                }
                            } else {
                                activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage);
                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                + activity.lastHttpErrorMessage);
                        activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }
                // Clear the request reference.
                activity.removeReportExpenseRequest = null;

                // Clear the currently selected expense entry.
                activity.selectedReportEntry = null;

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle the result of an attempt to retrieve a list of expense types
     */
    static class ExpenseTypesReceiver extends BroadcastReceiver {

        private final String CLS_TAG = ExpenseEntries.CLS_TAG + '.' + ExpenseTypesReceiver.class.getSimpleName();

        // A reference to the activity.
        protected ExpenseEntries activity;

        // A reference to the get expense types request.
        private GetExpenseTypesRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ExpenseTypesReceiver</code>.
         * 
         * @param frmFldView
         *            the form field view associated with this receiver.
         */
        ExpenseTypesReceiver(ExpenseEntries activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseEntries activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.expenseTypesRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the get expense types request object associated with this broadcast receiver.
         * 
         * @param request
         *            the get expense types request object associated with this broadcast receiver.
         */
        void setRequest(GetExpenseTypesRequest request) {
            this.request = request;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                try {
                    // Remove the dialog.
                    activity.removeDialog(Const.DIALOG_RETRIEVE_EXPENSE_TYPES);
                } catch (IllegalArgumentException ilaExc) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                }

                // Unregister this receiver.
                activity.unregisterExpenseTypesReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    // Attempt to pull the set of expense types
                                    // now from in-memory cache and present the
                                    // dialog
                                    // for selection.
                                    ConcurCore ConcurCore = (ConcurCore) activity.getApplication();
                                    IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
                                    activity.expTypes = expEntCache.getExpenseTypes(activity.expRep.polKey);
                                    if (activity.expTypes == null) {
                                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: no expense types fetched!");
                                        activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG + ".onReceive: mobile web service error -- "
                                                    + intent.getStringExtra(Const.REPLY_ERROR_MESSAGE));
                                    activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG + ".onReceive: http error -- "
                                                + intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT));
                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear out the expense types request.
                activity.expenseTypesRequest = null;
            } else {
                // The activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle the result of an attempt to retrieve an expense entry form.
     */
    static class EntryFormReceiver extends BroadcastReceiver {

        private final String CLS_TAG = ExpenseEntries.CLS_TAG + '.' + EntryFormReceiver.class.getSimpleName();

        // A reference to the activity.
        private ExpenseEntries activity;

        // A reference to the report entry form request.
        private ReportEntryFormRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>EntryFormReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        EntryFormReceiver(ExpenseEntries activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseEntries activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.entryFormRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the report itemization entry form request object associated with this broadcast receiver.
         * 
         * @param request
         *            the report itemization entry form request object associated with this broadcast receiver.
         */
        void setRequest(ReportEntryFormRequest request) {
            this.request = request;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                try {
                    // Remove the dialog.
                    activity.removeDialog(Const.DIALOG_EXPENSE_ENTRY_FORM);
                } catch (IllegalArgumentException ilaExc) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                }

                // Unregister the receiver.
                activity.unregisterEntryFormReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {

                                    // Verify that we have a new form
                                    ConcurCore ConcurCore = (ConcurCore) activity.getApplication();
                                    if (ConcurCore.getCurrentEntryDetailForm() != null) {
                                        // Flurry Notification
                                        Map<String, String> params = new HashMap<String, String>();
                                        params.put(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_REPORT);
                                        EventTracker.INSTANCE.track(Flurry.CATEGORY_REPORT_ENTRY,
                                                Flurry.EVENT_NAME_CREATE, params);

                                        // Hand off to the expense entry
                                        // activity
                                        Intent i = activity.buildExpenseEntryClickIntent(activity.expRep.polKey,
                                                activity.expRep.reportKey, null, activity.selectedExpenseType.key);
                                        activity.startActivityForResult(i, REQUEST_VIEW_ENTRY_DETAILS);
                                    } else {
                                        // Everything worked but form didn't
                                        // show. Hrm.
                                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: itemized entry form not received");
                                        activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG + ".onReceive: mobile web service error -- "
                                                    + intent.getStringExtra(Const.REPLY_ERROR_MESSAGE));
                                    activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG + ".onReceive: http error -- "
                                                + intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT));
                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }
                // Clear the request reference.
                activity.entryFormRequest = null;

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    /**
     * Will send a request to the server to delete the report currently being viewed.
     */
    protected void sendReportDeleteRequest() {
        ConcurService concurService = getConcurService();
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
    static class ReportDeleteReceiver extends BaseBroadcastReceiver<ExpenseEntries, ReportDeleteRequest> {

        /**
         * Constructs an instance of <code>ReportDeleteReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReportDeleteReceiver(ExpenseEntries activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(ExpenseEntries activity) {
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

            // Finish the activity.
            getActivity().finish();
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
