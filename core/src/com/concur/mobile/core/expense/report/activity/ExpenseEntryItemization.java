/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.activity.ExpenseTypeSpinnerAdapter;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.service.ReportItemizationEntryFormRequest;
import com.concur.mobile.core.expense.service.GetExpenseTypesRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.StyleableSpannableStringBuilder;

/**
 * Provides an activity to display expense report entry itemizations.
 * 
 * @author AndrewK
 */
public class ExpenseEntryItemization extends ExpenseEntries {

    private static final String CLS_TAG = ExpenseEntryItemization.class.getSimpleName();

    private static final String ENTRY_FORM_RECEIVER_KEY = "itemization.entry.form.receiver";

    private static final String EXPENSE_TYPES_RECEIVER_KEY = "itemization.expense.types.receiver";

    private static final String SELECTED_EXPENSE_TYPE_KEY = "itemization.selected.expense.type";

    private static final String EXPENSE_TYPES_KEY = "itemization.expense.types";
    private static final String EXPENSE_TYPES_FILTERED_OBJ_KEY = "expense.types.filtered.object.key";
    /**
     * Contains a reference to the instance of <code>ExpenseReportEntry</code> that was passed to this activity.
     */
    protected ExpenseReportEntryDetail expRepEntryDetail;

    // A broadcast receiver to handle the result of an expense types request.
    private ExpenseTypesReceiver expenseTypesReceiver;

    // The filter used to register the above receiver.
    private IntentFilter expenseTypesFilter;

    // A reference to an outstanding request.
    private GetExpenseTypesRequest expenseTypesRequest;

    // Contains a reference to the expense type adapter.
    private ExpenseTypeSpinnerAdapter expTypeAdapter;

    // Contains a reference to a selected expense type.
    // filter object contains reference to a filter expense type
    private ExpenseType selectedExpenseType, filteredObject;

    // A broadcast receiver to handle the result of an entry form request.
    private EntryFormReceiver entryFormReceiver;

    // The filter used to register the above receiver.
    private IntentFilter entryFormFilter;

    // A reference to an outstanding request.
    private ReportItemizationEntryFormRequest entryFormRequest;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        expenseTypesFilter = new IntentFilter(Const.ACTION_EXPENSE_EXPENSE_TYPES_DOWNLOADED);
        entryFormFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_ITEMIZATION_ENTRY_FORM_UPDATED);

        // Restore any receivers.
        restoreReceivers();

        // Restore 'selectedExpenseType'.
        if (retainer.contains(SELECTED_EXPENSE_TYPE_KEY)) {
            selectedExpenseType = (ExpenseType) retainer.get(SELECTED_EXPENSE_TYPE_KEY);
        }

        if (savedInstanceState != null) {
            // Restore the list of expense types.
            if (savedInstanceState.containsKey(EXPENSE_TYPES_KEY)) {
                String expTypesStr = savedInstanceState.getString(EXPENSE_TYPES_KEY);
                if (expTypesStr != null) {
                    expTypes = ExpenseType.parseExpenseTypeXml(expTypesStr);
                }
            }

            if (savedInstanceState.containsKey(EXPENSE_TYPES_FILTERED_OBJ_KEY)) {
                String key = savedInstanceState.getString(EXPENSE_TYPES_FILTERED_OBJ_KEY);
                if (key != null) {
                    ConcurCore ConcurCore = (ConcurCore) getApplication();
                    IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
                    filteredObject = expEntCache.getFilteredExpenseType(expTypes, key);
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseEntries#isSubmitReportEnabled()
     */
    @Override
    protected boolean isSubmitReportEnabled() {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save 'EntryFormReceiver'.
        if (entryFormReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            entryFormReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(ENTRY_FORM_RECEIVER_KEY, entryFormReceiver);
        }
        // Save 'ExpenseTypesReceiver'.
        if (expenseTypesReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            expenseTypesReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(EXPENSE_TYPES_RECEIVER_KEY, expenseTypesReceiver);
        }
        // Save the 'selectedExpenseType'.
        if (selectedExpenseType != null) {
            retainer.put(SELECTED_EXPENSE_TYPE_KEY, selectedExpenseType);
        }

        // Save the list of expense types
        // We need to do this because the managed dialog stuff will happen
        // before our other
        // init code and the expense type dialog may be up.
        // if (expTypes != null) {
        // retainer.put(EXPENSE_TYPES_KEY, expTypes);
        // }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
        if (retainer != null) {
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
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseEntries#onSaveInstanceState (android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the expense types list.
        if (this.expTypes != null) {
            StringBuilder strBldr = new StringBuilder();
            ExpenseType.ExpenseTypeSAXHandler.serializeToXML(strBldr, expTypes);
            outState.putString(EXPENSE_TYPES_KEY, strBldr.toString());
        }
        if (this.expRepEntryDetail != null) {
            outState.putString(EXPENSE_TYPES_FILTERED_OBJ_KEY, expRepEntryDetail.expKey);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseEntries#getExpenseEntries()
     */
    @Override
    protected ArrayList<ExpenseReportEntry> getExpenseEntries() {
        // Return the list of itemization entries.
        ArrayList<ExpenseReportEntry> itemizations = null;
        if (expRepEntryDetail != null) {
            if (expRepEntryDetail.isItemized()) {
                itemizations = expRepEntryDetail.getItemizations();
            }
        }
        return itemizations;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean doMenu = super.onCreateOptionsMenu(menu);

        if (doMenu) {
            MenuItem add = menu.findItem(R.id.menuAdd);
            if (add != null) {
                add.setTitle(R.string.entry_add_itemization);
                menu.removeItem(R.id.report_delete);
            }
        }
        return doMenu;
    }

    @Override
    protected void onAddExpense() {
        if (ConcurCore.isConnected()) {
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
            expTypes = expEntCache.getExpenseTypes(expRep.polKey);
            if (expTypes != null) {
                // Show the expense types list selection dialog.
                filteredObject = expEntCache.getFilteredExpenseType(expTypes, expRepEntryDetail.expKey);
                showDialog(Const.DIALOG_SELECT_EXPENSE_TYPE);

            } else {
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
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseEntries# buildExpenseEntryClickIntent(java.lang.String, java.lang.String)
     */
    @Override
    protected Intent buildExpenseEntryClickIntent(String polKey, String reportKey, String reportEntryKey, String expKey) {

        // Let the parent build the intent.
        Intent clickIntent = super.buildExpenseEntryClickIntent(polKey, reportKey, reportEntryKey, expKey);

        // Add as extra data the 'expRepEntryDetail' report entry key as the
        // parent report entry key.
        clickIntent.putExtra(Const.EXTRA_EXPENSE_PARENT_REPORT_ENTRY_KEY, expRepEntryDetail.reportEntryKey);
        clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);

        return clickIntent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseEntries# buildExpenseEntryReceiptClickIntent(java.lang.String,
     * java.lang.String)
     */
    @Override
    protected Intent buildExpenseEntryReceiptClickIntent(String reportKey, String reportEntryKey) {
        Intent clickIntent = super.buildExpenseEntryReceiptClickIntent(reportKey, reportEntryKey);
        // Add as extra data the 'expRepEntryDetail' report entry key as the
        // parent report entry key.
        clickIntent.putExtra(Const.EXTRA_EXPENSE_PARENT_REPORT_ENTRY_KEY, expRepEntryDetail.reportEntryKey);
        return clickIntent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getHeaderNavBarTitleResourceId()
     */
    @Override
    protected int getHeaderNavBarTitleResourceId() {
        return R.string.itemization_list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isDetailReportRequired()
     */
    @Override
    protected boolean isDetailReportRequired() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#buildView()
     */
    @Override
    protected void buildView() {

        // Set the content view.
        setContentView(R.layout.expense_entry_itemization);

        Intent intent = getIntent();
        String expRepEntryKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
        if (expRepEntryKey != null) {
            ExpenseReportEntry expRepEntry = expRepCache.getReportEntry(expRep, expRepEntryKey);
            if (expRepEntry != null) {
                try {
                    expRepEntryDetail = (ExpenseReportEntryDetail) expRepEntry;

                    // Configure the screen header.
                    configureScreenHeader(expRep);

                    // Configure the screen footer.
                    configureScreenFooter();

                    // Populate the expense list entry rows.
                    LinearLayout expEntriesView = (LinearLayout) findViewById(R.id.entries_list);
                    populateExpenseEntriesView(expRep, expEntriesView);
                    ArrayList<ExpenseReportEntry> entries = getExpenseEntries();
                    if (entries == null || entries.size() == 0) {
                        View view = findViewById(R.id.scroll_view);
                        if (view != null) {
                            view.setVisibility(View.GONE);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate 'scroll_view' view!");
                        }
                    }

                    // Flurry Notification.
                    Map<String, String> params = new HashMap<String, String>();
                    int count = 0;
                    if (entryListAdapter != null) {
                        count = entryListAdapter.getCount();
                    }
                    params.put(Flurry.PARAM_NAME_COUNT, Integer.toString(count));
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_REPORT_ENTRY,
                            Flurry.EVENT_NAME_ITEMIZED_ENTRY_LIST, params);

                } catch (ClassCastException ccExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: non detail expense entry - " + ccExc.getMessage(),
                            ccExc);
                }
            }
        }
    }

    protected void populateExpenseEntryTitleHeader() {
        View view = findViewById(R.id.expense_entry_title_header);
        if (view != null) {
            if (expRepEntryDetail != null) {
                updateExpenseEntryRowView(view, expRepEntryDetail);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseEntryTitleHeader: expense report entry detail is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".populateExpenseEntryTitleHeader: unable to locate expense entry title header view!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# configureScreenFooter()
     */
    @Override
    protected void configureScreenFooter() {
        // Fill in the amount fields
        final Locale locale = getResources().getConfiguration().locale;
        double itemized = expRepEntryDetail.getItemizationTotal();
        TextView tv = (TextView) findViewById(R.id.itemizedAmount);
        if (tv != null) {
            tv.setText(FormatUtil.formatAmount(itemized, locale, expRepEntryDetail.transactionCrnCode, true, true));
        }

        // Configure the remaining amount fields
        double remaining = expRepEntryDetail.transactionAmount - itemized;
        tv = (TextView) findViewById(R.id.itemizedRemainingAmount);
        if (tv != null) {
            StyleableSpannableStringBuilder strBldr = new StyleableSpannableStringBuilder();
            String formattedRemainingAmount = FormatUtil.formatAmount(remaining, locale,
                    expRepEntryDetail.transactionCrnCode, true, true);
            if (remaining != 0.0) {
                strBldr.appendBold(formattedRemainingAmount);
            } else {
                strBldr.append(formattedRemainingAmount);
            }
            tv.setText(strBldr);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseEntries#showTitleBarActionButton ()
     */
    @Override
    protected boolean showTitleBarActionButton() {
        return isReportEditable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseEntries# createRemoveReportExpenseReceiver()
     */
    @Override
    protected RemoveReportExpenseReceiver createRemoveReportExpenseReceiver() {
        return new RemoveItemizedExpenseReceiver(this);
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onCreateDialog(int)
     */
    @Override
    public Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case Const.DIALOG_SELECT_EXPENSE_TYPE: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (filteredObject != null) {
                List<ExpenseType> result = filteredObject.filterExpensetype(expTypes, filteredObject);
                if (result != null && result.size() > 0) {

                    builder.setTitle(R.string.expense_type_prompt);
                    expTypeAdapter = new ExpenseTypeSpinnerAdapter(this, null, expRepEntryDetail.expKey);
                    expTypeAdapter.setExpenseTypes(result, null, false, true, Const.EXPENSE_CODE_PERSONAL_MILEAGE,
                            Const.EXPENSE_CODE_COMPANY_MILEAGE);
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
                                // Register the receiver to handle the
                                // result.
                                registerEntryFormReceiver();
                                ConcurService concurService = ConcurCore.getService();
                                entryFormRequest = concurService.sendReportItemizationEntryFormRequest(true,
                                        selectedExpenseType.key, expRep.reportKey, expRepEntryDetail.reportEntryKey,
                                        null);
                                if (entryFormRequest == null) {
                                    // Unregister the receiver.
                                    unregisterEntryFormReceiver();
                                    // TODO: Need a dialog
                                } else {
                                    // Set the request object on the
                                    // receiver.
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
                } else {
                    showDialog(Const.MISSING_ITEMIZATION_ALERT_DIALOG);
                }
            } else {
                showDialog(Const.MISSING_ITEMIZATION_ALERT_DIALOG);
            }
            break;
        }
        case Const.DIALOG_RETRIEVE_EXPENSE_TYPES: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.retrieve_expense_types));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (expenseTypesRequest != null) {
                        expenseTypesRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog.onCancel: null expense types request!");
                    }
                }
            });
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
        case Const.MISSING_ITEMIZATION_ALERT_DIALOG: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(this.getText(R.string.expense_itemization_error_dialog_title));
            builder.setCancelable(true);
            builder.setMessage(R.string.expense_itemization_error_dialog_text);
            builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Dismiss this dialog
                    dialog.dismiss();
                }
            });
            AlertDialog alertDlg = builder.create();
            alertDlg.show();
            break;
        }
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case REQUEST_VIEW_ENTRY_DETAILS:
            if (resultCode == RESULT_OK) {
                // Set our result to be OK as well to force a view rebuild at
                // the previous activity
                setResult(RESULT_OK);

                // Flurry Notification
                EventTracker.INSTANCE.track(Flurry.CATEGORY_REPORT_ENTRY,
                        Flurry.EVENT_NAME_ITEMIZE_ENTRY);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle the result of an attempt to retrieve a list of expense types for
     * child entries.
     */
    static class ExpenseTypesReceiver extends BroadcastReceiver {

        private final String CLS_TAG = ExpenseEntryItemization.CLS_TAG + '.'
                + ExpenseTypesReceiver.class.getSimpleName();

        // A reference to the activity.
        private ExpenseEntryItemization activity;

        // A reference to the expense types request.
        private GetExpenseTypesRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ExpenseTypesReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ExpenseTypesReceiver(ExpenseEntryItemization activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseEntryItemization activity) {
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
         * Sets the expense types request object associated with this broadcast receiver.
         * 
         * @param request
         *            the expense types request object associated with this broadcast receiver.
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

                // Unregister the receiver.
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
                                    if (activity.expTypes != null) {
                                        // Display the expense type selection
                                        // dialog.
                                        activity.filteredObject = expEntCache.getFilteredExpenseType(activity.expTypes,
                                                activity.expRepEntryDetail.expKey);
                                        activity.showDialog(Const.DIALOG_SELECT_EXPENSE_TYPE);

                                        // activity.showDialog(Const.DIALOG_SELECT_EXPENSE_TYPE);
                                    } else {
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
                // Clear the request reference.
                activity.expenseTypesRequest = null;

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle the result of an attempt to retrieve an expense entry form.
     */
    static class EntryFormReceiver extends BroadcastReceiver {

        private final String CLS_TAG = ExpenseEntryItemization.CLS_TAG + '.' + EntryFormReceiver.class.getSimpleName();

        // A reference to the activity.
        private ExpenseEntryItemization activity;

        // A reference to the report itemization entry form request.
        private ReportItemizationEntryFormRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>EntryFormReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        EntryFormReceiver(ExpenseEntryItemization activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseEntryItemization activity) {
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
        void setRequest(ReportItemizationEntryFormRequest request) {
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
     * An extension of <code>RemoveReportExpenseReceiver</code> for handling the result of removing a report itemization expense.
     */
    static class RemoveItemizedExpenseReceiver extends RemoveReportExpenseReceiver {

        final String CLS_TAG = ExpenseEntryItemization.CLS_TAG + "."
                + RemoveItemizedExpenseReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>RemoveItemizedExpenseReceiver</code> with an associated activity.
         * 
         * @param activity
         *            the associated activity.
         */
        RemoveItemizedExpenseReceiver(ExpenseEntryItemization activity) {
            super(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {
                // Unregister the receiver.
                activity.unregisterRemoveReportExpenseReceiver();
                // Dismiss the dialog.
                activity.dismissDialog(Const.DIALOG_EXPENSE_REMOVE_REPORT_EXPENSE_PROGRESS);

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    // The result is a report key for a detailed
                                    // report.
                                    String reportKey = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
                                    if (reportKey != null && reportKey.length() > 0) {
                                        activity.expRep = activity.expRepCache.getReportDetail(reportKey);
                                        if (activity.expRep != null) {
                                            // Set the flag on the active report
                                            // cache that a re-fetch needs
                                            // to be completed.
                                            activity.expRepCache.setShouldFetchReportList();

                                            // Rebuild the view because it makes
                                            // sure we are looking at the
                                            // most up-to-date entry information
                                            activity.buildView();

                                            // If we deleted the last child then
                                            // get out.
                                            ArrayList<ExpenseReportEntry> children = activity.getExpenseEntries();
                                            if (children == null || children.size() < 1) {
                                                // Get out
                                                activity.setResult(RESULT_OK);
                                                activity.finish();
                                            }
                                        } else {
                                            Log.e(Const.LOG_TAG, CLS_TAG
                                                    + ".onReceive: unable to locate detailed report for report key '"
                                                    + reportKey + "'.");
                                        }
                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: null report key!");
                                    }

                                    // Flurry Notification
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_ITEMIZATION);
                                    EventTracker.INSTANCE.track(Flurry.CATEGORY_DELETE, Flurry.EVENT_NAME_ACTION,
                                            params);

                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);
                                    // Display an error dialog.
                                    activity.showDialog(Const.DIALOG_EXPENSE_REMOVE_REPORT_EXPENSE_FAILED);
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

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }
}
