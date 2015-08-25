/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.expense.activity.ListSearch;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.data.ListItemField;
import com.concur.mobile.core.expense.data.ReceiptPictureSaveAction;
import com.concur.mobile.core.expense.data.SearchListResponse;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportComment;
import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportException;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.ControlType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.DataType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.InputType;
import com.concur.mobile.core.expense.report.service.ConditionalFieldAction;
import com.concur.mobile.core.expense.report.service.DefaultAttendeeRequest;
import com.concur.mobile.core.expense.report.service.ExchangeRateRequest;
import com.concur.mobile.core.expense.report.service.GetTaxFormReply;
import com.concur.mobile.core.expense.report.service.SaveReportEntryRequest;
import com.concur.mobile.core.expense.report.service.TaxForm;
import com.concur.mobile.core.expense.service.GetExpenseTypesRequest;
import com.concur.mobile.core.expense.service.SearchListRequest;
import com.concur.mobile.core.expense.travelallowance.TravelAllowanceFacade;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ExpTypeMruAsyncTask;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormUtil;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ListItemMruAsyncTask;
import com.concur.mobile.core.util.MrudataCollector;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.ViewUtil.LocationSelection;
import com.concur.mobile.core.view.ComboListFormFieldView;
import com.concur.mobile.core.view.DatePickerFormFieldView;
import com.concur.mobile.core.view.ExpenseTypeFormFieldView;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldViewListener;
import com.concur.mobile.core.view.InlineTextFormFieldView;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.platform.util.Format;

/**
 * Provides an activity to display an expense report entry.
 * 
 * The following information will be directly displayed within this
 * 
 * The intent that invokes this activity must contain the following: 1. An expense report key. 2. An expense report entry key. 3.
 * (Optionally), an expense report parent entry key (parent of report entry key).
 * 
 * @author AndrewK
 */
/**
 * @author sunill
 * 
 */
public class ExpenseEntry extends AbstractExpenseActivity {

    private static final String CLS_TAG = ExpenseEntry.class.getSimpleName();

    private static final String EDITED_REPORT_ENTRY_DETAIL_BUNDLE_KEY = "edited.report.entry.detail.key";
    // private static final String EDITED_REPORT_ENTRY_DETAIL_TAX_FORM_BUNDLE_KEY = "edited.report.entry.detail.tax.form.key";

    private static final String SAVING_EXPENSE_BUNDLE_KEY = "saving.expense";

    private static final String RECEIPT_MINI_VIEW_FETCH_SUCCEEDED_BUNDLE_KEY = "receipt.mini.view.fetch.succeeded";

    private static final String SAVE_REPORT_ENTRY_RECEIVER_KEY = "save.report.entry.receiver";

    private static final String EXCHANGE_RATE_RECEIVER_KEY = "exchange.rate.receiver";

    private static final String CURRENCY_SEARCH_RECEIVER_KEY = "currency.search.receiver";

    private static final String EXPENSE_TYPES_RECEIVER_KEY = "expense.types.receiver";

    private static final String DEFAULT_ATTENDEE_RECEIVER_KEY = "default.attendee.receiver";

    // Contains a list of "hard-stop" form field ID's that must have values for
    // foreign currency transactions.
    public static String[] FOREIGN_CURRENCY_HARD_STOP_FIELD_IDS = { "ExpKey", "TransactionDate", "PostedAmount",
            "ExchangeRate", "TransactionAmount", "TransactionCurrencyName", "PatKey", "ReceiptType", "AdjustedAmount" };

    // Contains a list of "hard-stop" form field ID's that must have values for
    // local currency transactions.
    public static String[] LOCAL_CURRENCY_HARD_STOP_FIELD_IDS = { "ExpKey", "TransactionDate", "TransactionAmount",
            "TransactionCurrencyName", "PatKey", "ReceiptType", "AdjustedAmount" };

    // Contains a list of read-only field ids for itemization entries.
    public static String[] ITEMIZATION_READ_ONLY_FIELD_IDS = { "VenLiKey", "VendorDescription", "LocName", "PatKey",
            "TransactionCurrencyName" };

    // Contains a list of read-only field ids for corporate credit card entries.
    public static String[] CCT_READ_ONLY_FIELD_IDS = { "VenLiKey", "VendorDescription", "TransactionAmount",
            "TransactionDate", "PatKey", "TransactionCurrencyName", "ExchangeRate" };

    /**
     * Contains a reference to the instance of <code>ExpenseReportEntry</code> this activity was invoked with.
     */
    protected ExpenseReportEntryDetail expRepEntDet;

    /**
     * Contains a reference to an instance of <code>ExpenseReportEntryDetail</code> that was loaded as a result of changing the
     * expense type for an entry prior to a save occurring.
     */
    protected ExpenseReportEntryDetail editedRepEntDet;

    // Contains a reference to an outstanding request to save a report.
    protected SaveReportEntryRequest saveReportEntryRequest;

    // Contains a reference to the receiver for handling a save report response.
    protected SaveReportEntryReceiver saveReportEntryReceiver;

    // Contains the filter for registering the above receiver.
    protected IntentFilter saveReportEntryFilter;

    // Contains a reference to an outstanding request to retrieve an exchange
    // rate.
    protected ExchangeRateRequest exchangeRateRequest;

    // Contains the broadcast receiver to handle a list search for a currency.
    protected CurrencySearchReceiver currencySearchReceiver;

    // A reference to an outstanding Currency search request.
    protected SearchListRequest currencySearchRequest;

    // Contains a reference to a filter used to register the Currency search
    // receiver.
    protected IntentFilter currencySearchFilter;

    // Contains a reference to the receiver for handling the exchange rate
    // response.
    protected ExchangeRateReceiver exchangeRateReceiver;

    // Contains the filter used to register the exchange rate receiver.
    protected IntentFilter exchangeRateFilter;

    // Contains the receiver used to handle the result of retrieving expense
    // type information.
    protected ExpenseTypesReceiver expenseTypesReceiver;

    // Contains the filter used to register the expense types receiver.
    protected IntentFilter expenseTypesFilter;

    protected DefaultAttendeeReceiver defaultAttendeeReceiver;

    // Contains the filter used to register the above default attendee receiver.
    protected IntentFilter defaultAttendeeFilter;

    // Contains an outstanding request to retrieve default attendee information.
    private DefaultAttendeeRequest defaultAttendeeRequest;

    // Contains an outstanding request to retrieve expense type information.
    protected GetExpenseTypesRequest expenseTypesRequest;

    // Contains the list of attendees that are being edited.
    protected List<ExpenseReportAttendee> editedAttendees;

    // A flag indicating whether the activity should finish after a successful
    // save
    protected boolean finishOnSave;

    // A flag indicating the expense is being saved.
    protected boolean savingExpense;

    // A flag indicating the previous fetch of the receipt mini view succeeded.
    protected boolean receiptMiniViewFetchSucceeded;

    // A flag indicating whether or not the 'isPersonal' field was changed.
    protected boolean isPersonalFieldChanged;

    // A flag indicating whether or not the attendees have changed.
    protected boolean attendeesChanged;

    // A flag indicating the transaction date should be stored.
    protected boolean storeLastTransDate;

    // A flag indicating the location selection should be stored.
    protected boolean storeLastLocationSelection;

    // Progress Dialog when searching for Currency.
    protected ProgressDialog currencySearchProgressDialog;

    // MRU data collector
    protected MrudataCollector mrudataCollector;

    /** A flag indicating the key elements(location,exptype,date,country, country subcode) has been changed */
    protected boolean isKeyElementChangedForVAT = false;

    /**
     * The facade handles the entire travel allowance logic in this activity.
     */
    private TravelAllowanceFacade taFacade;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // After MOB-4802, just always go back.
        finishOnSave = true;

        super.onCreate(savedInstanceState);

        // Restore any receivers.
        restoreReceivers();

        if (ViewUtil.hasTravelAllowanceFixed(this)) {
            this.taFacade = new TravelAllowanceFacade(new TravelAllowanceFacade.ExpenseEntryTACallback() {

                @Override
                public void populateTravelAllowanceFields(
                        List<ExpenseReportFormField> expenseReportFormFields) {
                    View allowanceFields = findViewById(R.id.allowance_fields);
                    ViewGroup viewGroup = (ViewGroup) findViewById(R.id.travel_allowance_field_list);

                    if (allowanceFields == null || viewGroup == null) {
                        return;
                    }

                    if (expenseReportFormFields == null || expenseReportFormFields.size() == 0) {
                        // Nothing to do. There seems to be no fixed allowances available so keep the view GONE.
                        allowanceFields.setVisibility(View.GONE);
                        return;
                    } else {
                        // Make the view group visible for fixed allowances.
                        allowanceFields.setVisibility(View.VISIBLE);
                    }

                    List<FormFieldView> frmFldViews = populateViewWithFormFields(viewGroup,
                            expenseReportFormFields, null);

                    if (frmFldViews != null && frmFldViews.size() > 0 && frmFldViewListener != null) {
                    if (frmFldViewListener.getFormFieldViews() != null) {
                        frmFldViewListener.getFormFieldViews().addAll(frmFldViews);
                    } else {
                        frmFldViewListener.setFormFieldViews(frmFldViews);
                    }
                }
                }
            });
            taFacade.setupExpenseEntryTAFields(this.getApplicationContext(), expRepEntDet);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save 'SaveReportEntryReceiver'.
        if (saveReportEntryReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            saveReportEntryReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(SAVE_REPORT_ENTRY_RECEIVER_KEY, saveReportEntryReceiver);
        }
        // Save 'ExchangeRateReceiver'.
        if (exchangeRateReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            exchangeRateReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(EXCHANGE_RATE_RECEIVER_KEY, exchangeRateReceiver);
        }
        // Save 'CurrencySearchReceiver'
        if (currencySearchReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            currencySearchReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(CURRENCY_SEARCH_RECEIVER_KEY, currencySearchReceiver);
        }
        // Save 'ExpenseTypesReceiver'
        if (expenseTypesReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            expenseTypesReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(EXPENSE_TYPES_RECEIVER_KEY, expenseTypesReceiver);
        }
        // Save 'DefaultAttendeeReceiver'.
        if (defaultAttendeeReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            defaultAttendeeReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(DEFAULT_ATTENDEE_RECEIVER_KEY, defaultAttendeeReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();

        // If no show changed then force a repaint of the view
        if (expRepEntDet != null && expRepEntDet.noShowCountChanged) {
            buildView();
        }
    }

    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
        if (retainer != null) {
            // Restore 'SaveReportEntryReceiver'.
            if (retainer.contains(SAVE_REPORT_ENTRY_RECEIVER_KEY)) {
                saveReportEntryReceiver = (SaveReportEntryReceiver) retainer.get(SAVE_REPORT_ENTRY_RECEIVER_KEY);
                if (saveReportEntryReceiver != null) {
                    // Set the activity on the receiver.
                    saveReportEntryReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer contains null reference for save report entry receiver!");
                }
            }
            // Restore 'ExchangeRateReceiver'.
            if (retainer.contains(EXCHANGE_RATE_RECEIVER_KEY)) {
                exchangeRateReceiver = (ExchangeRateReceiver) retainer.get(EXCHANGE_RATE_RECEIVER_KEY);
                if (exchangeRateReceiver != null) {
                    // Set the activity on the receiver.
                    exchangeRateReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer contains null reference for exchange rate receiver!");
                }
            }
            // Restore 'CurrencySearchReceiver'.
            if (retainer.contains(CURRENCY_SEARCH_RECEIVER_KEY)) {
                currencySearchReceiver = (CurrencySearchReceiver) retainer.get(CURRENCY_SEARCH_RECEIVER_KEY);
                if (currencySearchReceiver != null) {
                    // Set the activity on the receiver.
                    currencySearchReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer contains null reference for currency search receiver!");
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
            // Restore any default attendee receiver.
            if (retainer.contains(DEFAULT_ATTENDEE_RECEIVER_KEY)) {
                defaultAttendeeReceiver = (DefaultAttendeeReceiver) retainer.get(DEFAULT_ATTENDEE_RECEIVER_KEY);
                // Reset the activity reference.
                defaultAttendeeReceiver.setActivity(this);
            }
        }
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
        case Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_expense_save_report_entry));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(false);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (saveReportEntryRequest != null) {
                        // Cancel the request.
                        saveReportEntryRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: saveReportEntryRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_expense_save_report_entry_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_expense_fetch_exchange_rate_message));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (exchangeRateRequest != null) {
                        // Cancel the request.
                        exchangeRateRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: exchangeRateRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_expense_fetch_exchange_rate_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_EXPENSE_TYPE_PROGRESS: {
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
        case Const.DIALOG_EXPENSE_RETRIEVE_EXPENSE_TYPE_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_expense_retrieve_expense_types_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        case Const.DIALOG_EXPENSE_DEFAULT_ATTENDEE_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(this.getText(R.string.dlg_expense_attendee_retrieve_default_attendee_progress_message));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (defaultAttendeeRequest != null) {
                        defaultAttendeeRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog.onCancel: defaultAttendeeRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_DEFAULT_ATTENDEE_FAILED: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dlg_expense_attendee_retrieve_default_attendee_title);
            builder.setCancelable(true);
            builder.setMessage("");
            builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Dismiss this dialog
                    dialog.dismiss();
                }
            });
            dialog = builder.create();
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
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onPrepareDialog (int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_EXPENSE_TYPE_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case Const.DIALOG_EXPENSE_DEFAULT_ATTENDEE_FAILED: {
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
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Serialize to XML and store the string.
        if (editedRepEntDet != null) {
            StringBuilder strBldr = new StringBuilder();
            ExpenseReportEntryDetail.ExpenseReportEntryDetailSAXHandler.serializeAllToXML(strBldr, editedRepEntDet);
            outState.putString(EDITED_REPORT_ENTRY_DETAIL_BUNDLE_KEY, strBldr.toString());
        }
        // Store 'savingExpense'.
        outState.putBoolean(SAVING_EXPENSE_BUNDLE_KEY, savingExpense);
        // Store 'receiptMiniViewFetchSucceeded'.
        outState.putBoolean(RECEIPT_MINI_VIEW_FETCH_SUCCEEDED_BUNDLE_KEY, receiptMiniViewFetchSucceeded);
    }

    /**
     * Gets whether or not the list of attendees has changed.
     * 
     * @return whether or not the list of attendees has been changed.
     */
    protected boolean hasAttendeesChanged() {
        return attendeesChanged;
    }

    protected boolean hasNoShowCountChanged() {
        boolean changed = false;
        if (getExpRepEntDet() != null) {
            changed = getExpRepEntDet().noShowCountChanged;
        }
        return changed;
    }

    /**
     * Gets whether or not the expense type has changed.
     * 
     * @return whether the expense type has changed.
     */
    protected boolean hasExpenseTypeChanged() {
        boolean expenseTypeChanged = false;
        FormFieldView frmFldView = FormUtil.getFieldById(frmFldViewListener,
                ExpenseFormFieldViewListener.EXPENSE_TYPE_FIELD_ID);
        if (frmFldView instanceof ExpenseTypeFormFieldView) {
            ExpenseTypeFormFieldView expTypeFrmFldView = (ExpenseTypeFormFieldView) frmFldView;
            String origExpTypeKey = expRepEntDet.expKey;
            String curExpTypeKey = expTypeFrmFldView.getCurrentValue();
            if (origExpTypeKey != null && curExpTypeKey != null) {
                expenseTypeChanged = !origExpTypeKey.equalsIgnoreCase(curExpTypeKey);
            }
        }
        return expenseTypeChanged;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#changesPending ()
     */
    @Override
    protected boolean changesPending() {
        return (hasAttendeesChanged() || hasExpenseTypeChanged() || hasNoShowCountChanged());
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean retVal = false;
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
            if (hasFormFieldsChanged() || changesPending()) {
                processingBackPressed = true;
                showDialog(Const.DIALOG_EXPENSE_CONFIRM_SAVE_REPORT);
                retVal = true;
            } else {
                retVal = super.onKeyDown(keyCode, event);
            }
            break;
        }
        default: {
            retVal = super.onKeyDown(keyCode, event);
            break;
        }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getHeaderNavBarTitleResourceId()
     */
    @Override
    protected int getHeaderNavBarTitleResourceId() {
        int strId = R.string.expense;
        if (isItemizationExpense()) {
            strId = R.string.itemization;
        }
        return strId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# createFormFieldViewListener()
     */
    @Override
    protected FormFieldViewListener createFormFieldViewListener() {
        return new ExpenseFormFieldViewListener(this);
    }

    @Override
    protected boolean refreshReportHeaderAfterEntryReceiptUpdate() {
        return false;
    }

    /**
     * Will create and register with the application context an instance of 'SaveReportEntryReceiver' and update the
     * 'saveReportEntryReceiver' attribute.
     */
    protected void registerSaveReportEntryReceiver() {
        if (saveReportEntryReceiver == null) {
            saveReportEntryReceiver = new SaveReportEntryReceiver(this);
            getApplicationContext().registerReceiver(saveReportEntryReceiver, saveReportEntryFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveReportEntryReceiver: saveReportEntryReceiver is not null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of 'SaveReportEntryReceiver' and set the
     * 'saveReportEntryReceiver' attribute to 'null'.
     */
    protected void unregisterSaveReportEntryReceiver() {
        if (saveReportEntryReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(saveReportEntryReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveReportEntryReceiver: invalid receiver!", ilaExc);
            }
            saveReportEntryReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveReportEntryReceiver: saveReportEntryReceiver is null!");
        }
    }

    /**
     * Will register with the application context the current instance of 'ExchangeRateReceiver' and update the
     * 'exchangeRateReceiver' attribute.
     */
    protected void registerExchangeRateReceiver() {
        if (exchangeRateReceiver == null) {
            exchangeRateReceiver = new ExchangeRateReceiver(this);
            getApplicationContext().registerReceiver(exchangeRateReceiver, exchangeRateFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerExchangeRateReceiver: exchangeRateReceiver is not null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of 'ExchangeRateReceiver' and set the
     * 'exchangeRateReceiver' attribute to 'null'.
     */
    protected void unregisterExchangeRateReceiver() {
        if (exchangeRateReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(exchangeRateReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterExchangeRateReceiver: invalid receiver!", ilaExc);
            }
            exchangeRateReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterExchangeRateReceiver: exchangeRateReceiver is null!");
        }
    }

    /**
     * Will register with the application context the current instance of 'CurrencySearchReceiver' and update the
     * 'currencySearchReceiver' attribute.
     */
    protected void registerCurrencySearchReceiver(boolean isLocationChangedForVat) {
        if (currencySearchReceiver == null) {
            currencySearchReceiver = new CurrencySearchReceiver(this, isLocationChangedForVat);
            getApplicationContext().registerReceiver(currencySearchReceiver, currencySearchFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerCurrencySearchReceiver: currencySearchReceiver is not null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of 'CurrencySearchReceiver' and set the
     * 'currencySearchReceiver' attribute to 'null'.
     */
    protected void unregisterCurrencySearchReceiver() {
        if (currencySearchReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(currencySearchReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterCurrencySearchReceiver: invalid receiver!", ilaExc);
            }
            currencySearchReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterCurrencySearchReceiver: currencySearchReceiver is null!");
        }
    }

    /**
     * Will handle sending a request to obtain expense types for the report policy.
     */
    protected void sendExpenseTypesRequest() {
        if (ConcurCore.isConnected()) {
            ConcurService concurService = getConcurService();
            registerExpenseTypesReceiver();
            expenseTypesRequest = concurService.sendGetExpenseTypesRequest(getUserId(), expRep.polKey);
            if (expenseTypesRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to create 'GetExpenseTypesRequest' request!");
                unregisterExpenseTypesReceiver();
            } else {
                // Set the request object on the receiver.
                expenseTypesReceiver.setServiceRequest(expenseTypesRequest);
                // Show the dialog.
                showDialog(Const.DIALOG_EXPENSE_RETRIEVE_EXPENSE_TYPE_PROGRESS);
            }
        } else {
            // TODO: Show some sort of dialog with a message indicating that
            // policy-specific expense types
            // could not be downloaded from the server at this time.
            Log.i(Const.LOG_TAG, CLS_TAG
                    + ".sendExpenseTypesRequest: client off-line, unable to request policy-specific expense types!");
        }
    }

    /**
     * Will register an instance of <code>ExpenseTypesReceiver</code> with the application context and set the
     * <code>expenseTypesReceiver</code> attribute.
     */
    private void registerExpenseTypesReceiver() {
        if (expenseTypesReceiver == null) {
            expenseTypesReceiver = new ExpenseTypesReceiver(this);
            if (expenseTypesFilter == null) {
                expenseTypesFilter = new IntentFilter(Const.ACTION_EXPENSE_EXPENSE_TYPES_DOWNLOADED);
            }
            getApplicationContext().registerReceiver(expenseTypesReceiver, expenseTypesFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerExpenseTypesReceiver: expenseTypesReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>ExpenseTypesReceiver</code> with the application context and set the
     * <code>expenseTypesReceiver</code> to <code>null</code>.
     */
    private void unregisterExpenseTypesReceiver() {
        if (expenseTypesReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(expenseTypesReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterExpenseTypesReceiver: invalid receiver!", ilaExc);
            }
            expenseTypesReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterExpenseTypesReceiver: expenseTypesReceiver is null!");
        }
    }

    /**
     * Will send off a request to retrieve default attendee information.
     */
    protected void sendDefaultAttendeeRequest() {
        if (ConcurCore.isConnected()) {
            ConcurService concurService = getConcurService();
            registerDefaultAttendeeReceiver();
            defaultAttendeeRequest = concurService.sendDefaultAttendeeRequest(getUserId());
            if (defaultAttendeeRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".onReceive: unable to create request to retrieve default attendee information!");
                unregisterDefaultAttendeeReceiver();
            } else {
                // Set the request object on the receiver.
                defaultAttendeeReceiver.setServiceRequest(defaultAttendeeRequest);
                // Show the attendee form progress dialog.
                showDialog(Const.DIALOG_EXPENSE_DEFAULT_ATTENDEE_PROGRESS);
            }
        } else {
            // TODO: Show some sort of dialog with a message indicating that
            // default attendee information
            // could not be downloaded from the server at this time.
            Log.i(Const.LOG_TAG, CLS_TAG
                    + ".sendDefaultAttendeeRequest: client off-line, unable to request default attendee information!");
        }

    }

    /**
     * Will register an instance of <code>DefaultAttendeeReceiver</code> with the application context and set the
     * <code>defaultAttendeeReceiver</code> attribute.
     */
    protected void registerDefaultAttendeeReceiver() {
        if (defaultAttendeeReceiver == null) {
            defaultAttendeeReceiver = new DefaultAttendeeReceiver(this);
            if (defaultAttendeeFilter == null) {
                defaultAttendeeFilter = new IntentFilter(Const.ACTION_EXPENSE_DEFAULT_ATTENDEE_DOWNLOADED);
            }
            getApplicationContext().registerReceiver(defaultAttendeeReceiver, defaultAttendeeFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerDefaultAttendeeReceiver: defaultAttendeeReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>DefaultAttendeeReceiver</code> with the application context and set the
     * <code>defaultAttendeeReceiver</code> to <code>null</code>.
     */
    protected void unregisterDefaultAttendeeReceiver() {
        if (defaultAttendeeReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(defaultAttendeeReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterDefaultAttendeeReceiver: invalid receiver!", ilaExc);
            }
            defaultAttendeeReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterDefaultAttendeeReceiver: defaultAttendeeReceiver is null!");
        }
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
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isSaveReportEnabled()
     */
    @Override
    protected boolean isSaveReportEnabled() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isReportEntryWithReceipt()
     */
    @Override
    protected boolean isReportEntryWithReceipt() {
        boolean retVal = false;
        if (expRepEntDet != null) {
            if ((expRepEntDet.hasEReceiptImageId()) || (!TextUtils.isEmpty(expRepEntDet.receiptImageId))
                    || (expRepEntDet.hasMobileReceipt() && (!TextUtils.isEmpty(expRepEntDet.receiptImageId)))) {

                retVal = true;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".isReportEntryWithReceipt: expRepEntDet is null!");
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# hasCopyDownChildren()
     */
    @Override
    protected boolean hasCopyDownChildren() {
        boolean retVal = false;
        if (expRepEntDet != null) {
            retVal = (expRepEntDet.getItemizations() != null && expRepEntDet.getItemizations().size() > 0);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".hasCopyDownChildren: expRepEntDet is null!");
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#sendSaveRequest ()
     */
    @Override
    protected void sendSaveRequest() {
        ConcurCore ConcurCore = getConcurCore();
        ConcurService concurService = getConcurService();
        registerSaveReportEntryReceiver();
        boolean canHaveAttendees = false;
        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
        List<ExpenseType> list = expEntCache.getExpenseTypes(expRep.polKey);
        boolean editable = isReportEditable();
        canHaveAttendees = (getExpRepEntDet().canHaveAttendees(list) && editable);
        if (canHaveAttendees) {
            getExpRepEntDet().setAttendees(editedAttendees);
        } else {
            // Set an empty attendee list.
            getExpRepEntDet().setAttendees(new ArrayList<ExpenseReportAttendee>());
        }

        // MOB-14876 - Ensure that LiCode is not set and that LiKey is set.
        // The current MRU design for location stores the original 'LiKey'
        // value in the 'LiCode' field.
        ExpenseReportFormField frmFld = getExpRepEntDet().findFormFieldByFieldId(ExpenseReportFormField.LOCATION_NAME);
        if (frmFld != null) {
            if ((frmFld.getLiKey() == null || frmFld.getLiKey().trim().length() == 0)
                    && (frmFld.getLiCode() != null && frmFld.getLiCode().trim().length() > 0)) {
                frmFld.setLiKey(frmFld.getLiCode());
            }
            frmFld.setLiCode(null);
        }

        // TODO MOB-8452
        saveReportEntryRequest = concurService.sendSaveReportEntryRequest(getUserId(), getExpRepEntDet(),
                overWriteCopyDownValues, expRep.polKey, getExpRepEntDet().expKey);
        if (saveReportEntryRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to create 'SaveReportEntry' request!");
            unregisterSaveReportEntryReceiver();
        } else {
            // Set the request object on the receiver.
            saveReportEntryReceiver.setRequest(saveReportEntryRequest);
            // Show the saving report dialog.
            showDialog(Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY);
            // Set the flag indicating a save operation is underway.
            savingExpense = true;

            // Save is in progress. Clear the change flag for no show count.
            getExpRepEntDet().noShowCountChanged = false;
        }

        // Store the last transaction date, if needbe.
        if (storeLastTransDate) {
            saveLastTransactionDate();
        }
    }

    @Override
    protected String[] getHardStopFieldIds() {
        String[] retVal = LOCAL_CURRENCY_HARD_STOP_FIELD_IDS;
        // Obtain the form field view object that contains the currently
        // selected transaction currency.
        FormFieldView frmFldView = frmFldViewListener
                .findFormFieldViewById(ExpenseFormFieldViewListener.TRANSACTION_CURRENCY_FIELD_ID);
        if (frmFldView != null) {
            if (frmFldView instanceof SearchListFormFieldView) {
                SearchListFormFieldView srchLstFrmFldView = (SearchListFormFieldView) frmFldView;
                String transCrnCode = srchLstFrmFldView.getLiCode();
                if (transCrnCode != null) {
                    if (expRep != null) {
                        String reportCrnCode = expRep.crnCode;
                        if (reportCrnCode != null) {
                            transCrnCode = transCrnCode.trim();
                            reportCrnCode = reportCrnCode.trim();
                            if (!transCrnCode.equalsIgnoreCase(reportCrnCode)) {
                                retVal = FOREIGN_CURRENCY_HARD_STOP_FIELD_IDS;
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".getHardStopFieldIds: the report currency code value is null!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getHardStopFieldIds: expense report reference is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".getHardStopFieldIds: the selected currency list item code is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getHardStopFieldIds: expected SearchListFormFieldView for field '"
                        + ExpenseFormFieldViewListener.TRANSACTION_CURRENCY_FIELD_ID + "'.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getHardStopFieldIds: unable to locate '"
                    + ExpenseFormFieldViewListener.TRANSACTION_CURRENCY_FIELD_ID + "' in form field views!");
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# shouldListenForNetworkActivity()
     */
    @Override
    protected boolean shouldListenForNetworkActivity() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getNetworkActivityText(java.lang.String)
     */
    @Override
    public String getNetworkActivityText(int networkMsgType, String defaultText) {
        String retVal;
        switch (networkMsgType) {
        case Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST:
            retVal = getText(R.string.retrieve_mini_receipt).toString();
            break;
        default:
            retVal = defaultText;
            break;
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isNetworkRequestInteresting(int)
     */
    @Override
    public boolean isNetworkRequestInteresting(int networkMsgType) {
        return (networkMsgType == Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isReportLevelReceiptDialog()
     */
    @Override
    protected boolean isReportLevelReceiptDialog() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isEntryWithEReceipt()
     */
    @Override
    protected boolean isReportEntryWithEReceipt() {
        return !TextUtils.isEmpty(expRepEntDet.eReceiptId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#isSaveRequired ()
     */
    @Override
    protected boolean isNewExpense() {
        boolean retVal = false;
        String rpeKey = getReportEntryKey();
        retVal = (rpeKey == null || rpeKey.length() == 0);
        return retVal;
    }

    /**
     * Will determine whether an expense type (by key) supports having the default attendee included.
     * 
     * @param expTypes
     *            the list of expense types.
     * @param expKey
     *            the expense type key to check.
     * @return whether <code>expKey</code> supports having the default attendee based on <code>expTypes</code>.
     */
    private boolean expenseTypeHasDefaultAttendee(List<ExpenseType> expTypes, String expKey) {
        boolean defaultAttendee = false;
        if (expTypes != null) {
            for (ExpenseType expType : expTypes) {
                if (expType.key != null && expKey != null && expType.key.equalsIgnoreCase(expKey)) {
                    if (expType.userAsAtnDefault != null) {
                        defaultAttendee = expType.userAsAtnDefault;
                        break;
                    }
                }
            }
        }
        return defaultAttendee;
    }

    /**
     * Will add the default attendee to the 'editedAttendees' list if needbe.
     * 
     * @return whether the default attendee was added to the 'editedAttendees' list.
     */
    private boolean addDefaultAttendee() {
        boolean addedDefaultAttendee = false;
        // Obtain a reference to the default attendee.
        IExpenseReportCache expRepCache = getConcurCore().getExpenseActiveCache();
        if (expRepCache != null) {
            ExpenseReportAttendee defaultAttendee = expRepCache.getDefaultAttendee();
            if (defaultAttendee != null) {
                // Set up the attendee list that may be edited.
                if (editedAttendees == null) {
                    editedAttendees = new ArrayList<ExpenseReportAttendee>();
                    if (getExpRepEntDet().getAttendees() != null) {
                        editedAttendees.addAll(getExpRepEntDet().getAttendees());
                    }
                }
                // Determine whether the default attendee is already in the
                // list.
                boolean foundDefaultAttendee = false;
                for (ExpenseReportAttendee expRepAtt : editedAttendees) {
                    if (expRepAtt.atnKey != null && defaultAttendee.atnKey != null
                            && expRepAtt.atnKey.equalsIgnoreCase(defaultAttendee.atnKey)) {
                        foundDefaultAttendee = true;
                        break;
                    }
                }
                // Add the default attendee and then re-calculate the amounts.
                if (!foundDefaultAttendee) {
                    if (defaultAttendee.instanceCount == null) {
                        defaultAttendee.instanceCount = 1;
                    }
                    if (defaultAttendee.isAmountEdited == null) {
                        defaultAttendee.isAmountEdited = false;
                    }
                    // Add the attendee.
                    editedAttendees.add(defaultAttendee);
                    addedDefaultAttendee = true;
                }
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG + ".addDefaultAttendee: default attendee information is unavailable!");
            }
        }
        return addedDefaultAttendee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#buildView()
     */
    @Override
    protected void buildView() {

        // Restore some state.
        if (lastSavedInstanceState != null) {
            // Restore 'savingExpense'.
            if (lastSavedInstanceState.containsKey(SAVING_EXPENSE_BUNDLE_KEY)) {
                savingExpense = lastSavedInstanceState.getBoolean(SAVING_EXPENSE_BUNDLE_KEY);
            }
            // Restore 'receiptMiniViewFetchSucceeded'.
            if (lastSavedInstanceState.containsKey(RECEIPT_MINI_VIEW_FETCH_SUCCEEDED_BUNDLE_KEY)) {
                receiptMiniViewFetchSucceeded = lastSavedInstanceState
                        .getBoolean(RECEIPT_MINI_VIEW_FETCH_SUCCEEDED_BUNDLE_KEY);
            }
        }

        // Construct the save report entry filter.
        saveReportEntryFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_ENTRY_SAVE);

        // Construct the exchange rate filter.
        exchangeRateFilter = new IntentFilter(Const.ACTION_EXPENSE_EXCHANGE_RATE_UPDATED);

        // Construct the Currency search filter.
        currencySearchFilter = new IntentFilter(Const.ACTION_EXPENSE_CURRENCY_SEARCH_UPDATED);

        // Instruct the window manager to only show the soft keyboard when the
        // end-user clicks on it.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Set the content view.
        setContentView(R.layout.expense_entry);

        // Configure the screen header.
        configureScreenHeader(expRep);

        // Configure the screen footer.
        configureScreenFooter();

        ConcurCore app = getConcurCore();
        IExpenseEntryCache expEntCache = app.getExpenseEntryCache();

        ExpenseReportEntry expRepEnt = null;
        Intent intent = getIntent();
        String expRepEntryKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
        if (expRepEntryKey == null || expRepEntryKey.trim().length() == 0) {

            // Set the flags to save the last transaction date and location.
            storeLastLocationSelection = true;
            storeLastTransDate = true;

            // A blank entry key indicates a new entry is being created.
            // Get the entry form that was previously retrieved.
            expRepEnt = app.getCurrentEntryDetailForm();

            if (expRepEnt != null) {
                // The server sends back 1/1/1 as the date for a blank form.
                // Update to now.
                Calendar now = Calendar.getInstance();
                now.set(Calendar.HOUR_OF_DAY, 0);
                now.set(Calendar.MINUTE, 0);
                now.set(Calendar.SECOND, 0);

                expRepEnt.transactionDateCalendar = now;
                expRepEnt.transactionDate = FormatUtil.XML_DF_LOCAL.format(now.getTime());
                // Initialize the transaction amount to 0.
                if (expRepEnt.transactionAmount == null) {
                    expRepEnt.transactionAmount = 0.0;
                }

                // Set up the attendee edited list.
                if (expRepEnt instanceof ExpenseReportEntryDetail) {
                    expRepEntDet = (ExpenseReportEntryDetail) expRepEnt;
                    List<ExpenseType> expTypes = expEntCache.getExpenseTypes(expRep.polKey);
                    boolean canHaveAttendees = (getExpRepEntDet().canHaveAttendees(expTypes) && isReportEditable());
                    if (canHaveAttendees && expenseTypeHasDefaultAttendee(expTypes, expRepEntDet.expKey)) {
                        if (addDefaultAttendee()) {
                            // If the transaction currency code/amount is set
                            // and the attendees is a non-empty list, then apply
                            // the current transaction amount over the
                            // attendees.
                            if (expRepEntDet.transactionCrnCode != null && expRepEntDet.transactionAmount != null
                                    && editedAttendees != null && editedAttendees.size() > 0) {
                                getExpRepEntDet().divideAmountAmongAttendees(expRepEntDet.transactionAmount,
                                        expRepEntDet.transactionCrnCode, editedAttendees);
                            }
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: new entry detail form is not detailed type!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: new entry detail form is null!");
            }
        } else {
            // A non-blank entry means we're editing. Get the entry.
            expRepEnt = expRepCache.getReportEntry(expRep, expRepEntryKey);
        }

        if (expRepEnt != null) {
            if (expRepEnt instanceof ExpenseReportEntryDetail) {
                // initiate MRU obj
                if (mrudataCollector == null) {
                    mrudataCollector = new MrudataCollector(expRepEntryKey);
                }

                expRepEntDet = (ExpenseReportEntryDetail) expRepEnt;

                // Attempt to restore 'editedRepEntDet' if necessary.
                if (lastSavedInstanceState != null
                        && lastSavedInstanceState.containsKey(EDITED_REPORT_ENTRY_DETAIL_BUNDLE_KEY)
                        && editedRepEntDet == null) {
                    String editedRepEntDetXmlStr = lastSavedInstanceState
                            .getString(EDITED_REPORT_ENTRY_DETAIL_BUNDLE_KEY);
                    if (editedRepEntDetXmlStr != null) {
                        editedRepEntDet = ExpenseReportEntryDetail.parseReportEntryDetailXml(editedRepEntDetXmlStr);
                        if (frmFldViewListener != null) {
                            frmFldViewListener.setExpenseReportEntry(editedRepEntDet);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: lastSavedInstanceState contains key '"
                                + EDITED_REPORT_ENTRY_DETAIL_BUNDLE_KEY + "' but with null value!");
                    }
                }

                // If the date is blank at this point, default it to today.
                ExpenseReportFormField transDate = expRepEntDet.findFormFieldByFieldId("TransactionDate");

                if (transDate != null) {
                    String val = transDate.getValue();
                    if (val == null || val.trim().length() == 0) {
                        // We're specifically getting 'now' in the device local
                        // timezone because
                        // we want to init the value to a local date.
                        Calendar now = Calendar.getInstance();
                        transDate.setValue(FormatUtil.XML_DF_LOCAL.format(now.getTime()));
                    }
                }

                // Set the expense report entry on any form view listener.
                if (frmFldViewListener != null && editedRepEntDet == null) {
                    frmFldViewListener.setExpenseReportEntry(expRepEntDet);
                }

                // Grab a reference to any previously build form field views.
                List<FormFieldView> srcFrmFlds = null;
                if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null
                        && frmFldViewListener.getFormFieldViews().size() > 0) {
                    srcFrmFlds = frmFldViewListener.getFormFieldViews();
                }

                // Grab a reference to any previously built tax form field
                // views.
                List<FormFieldView> srcTaxFrmFlds = null;
                if (frmFldViewListener != null && frmFldViewListener.getTaxFormFieldViews() != null
                        && frmFldViewListener.getTaxFormFieldViews().size() > 0) {
                    srcTaxFrmFlds = frmFldViewListener.getTaxFormFieldViews();
                }

                // Set the expense entry title information.
                populateExpenseEntryTitleHeader();

                // Set the expense entry exceptions.
                populateExpenseEntryExceptions();

                // Set the expense comments.
                populateExpenseComments();

                // Set itemizations/attendees.
                populateItemizationAttendeeReceipt();

                // Set calculated fields.
                setCalculatedFields();

                // Set Payment Type for Breeze report or personal card charge.
                setPaymentTypeFieldValues();

                // Lockdown read-only fields.
                lockDownReadOnlyFields();

                // Set the expense details.
                populateFormFields();

                // Set the tax form details.
                populateTaxFormFields();

                // Combine VendorName fields into one field if both present and
                // editable.
                combineVendorNameFields();

                // Set any "last" values for transaction date/location if
                // needbe.
                if (storeLastTransDate) {
                    restoreLastTransactionDate();
                }
                if (storeLastLocationSelection) {
                    restoreLastLocationSelection();
                }

                // Grab a reference to any newly build form fields.
                List<FormFieldView> dstFrmFlds = null;
                if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null
                        && frmFldViewListener.getFormFieldViews().size() > 0) {
                    dstFrmFlds = frmFldViewListener.getFormFieldViews();
                }

                // Grab a reference to any newly built tax form fields.
                List<FormFieldView> dstTaxFrmFlds = null;
                if (frmFldViewListener != null && frmFldViewListener.getTaxFormFieldViews() != null
                        && frmFldViewListener.getTaxFormFieldViews().size() > 0) {
                    dstTaxFrmFlds = frmFldViewListener.getTaxFormFieldViews();
                }

                // Transfer any edited values from 'srcFrmFlds' to 'dstFrmFlds'
                // where they match on
                // field id and field type.
                if (srcFrmFlds != null && srcFrmFlds.size() > 0 && dstFrmFlds != null && dstFrmFlds.size() > 0) {
                    transferEditedValues(srcFrmFlds, dstFrmFlds);
                }

                // Transfer any edited values from 'srcTaxFrmFlds' to
                // 'dstTaxFrmFlds' where they match on
                // field id and field type.
                if (srcTaxFrmFlds != null && srcTaxFrmFlds.size() > 0 && dstTaxFrmFlds != null
                        && dstTaxFrmFlds.size() > 0) {
                    transferEditedValues(srcTaxFrmFlds, dstTaxFrmFlds);
                }

                // Restore any saved values.
                if (lastSavedInstanceState != null) {
                    restoreFormFieldState();
                }

                // Perform any post initialization on the any form field view.
                if (frmFldViewListener != null) {
                    frmFldViewListener.initFields();
                }

                // If this entry is an itemization entry, then check whether the
                // expense entry cache has a set of expense types for the report
                // policy. If not, then send a request to obtain the report
                // policy
                // specific expense types.
                if (expRepEntDet != null && expRepEntDet.isItemization()) {
                    if (expEntCache.getExpenseTypes(expRep.polKey) == null) {
                        sendExpenseTypesRequest();
                    }
                }

                // If the report is editable, check whether default attendee
                // information is present, if not,
                // then kick-off a request to retrieve it.
                if (isReportEditable() && expRepEntDet.canHaveAttendees(expEntCache.getExpenseTypes())) {
                    if (expRepCache.getDefaultAttendee() == null) {
                        sendDefaultAttendeeRequest();
                    }
                }

                // call get tax form
                String rpeKey = getIntent().getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                ExpenseType expType = ExpenseType.findExpenseType(expRep.polKey, getExpRepEntDet().expKey);
                sendTaxFormRequest(rpeKey, expType);

                // set old data for mruDataCollector
                if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null
                        && frmFldViewListener.getFormFieldViews().size() > 0) {
                    List<FormFieldView> formFields = frmFldViewListener.getFormFieldViews();
                    FormFieldView expFrmField = FormUtil.findFormFieldViewById(formFields,
                            ExpenseFormFieldViewListener.EXPENSE_TYPE_FIELD_ID);
                    if (expFrmField != null && expFrmField.frmFld.getLiKey() != null) {
                        if (expRepEntryKey != null && expRepEntryKey.length() > 0) {
                            mrudataCollector.setOldExpType(expFrmField.frmFld.getLiKey());
                        }
                    }
                    expFrmField = FormUtil.findFormFieldViewById(formFields,
                            ExpenseFormFieldViewListener.TRANSACTION_CURRENCY_FIELD_ID);
                    if (expFrmField != null && expFrmField.frmFld.getLiCode() != null) {
                        if (expRepEntryKey != null && expRepEntryKey.length() > 0) {
                            mrudataCollector.setOldCurType(expFrmField.frmFld.getLiCode());
                        }
                    }
                    expFrmField = FormUtil.findFormFieldViewById(formFields,
                            ExpenseFormFieldViewListener.LOCATION_FIELD_ID);
                    if (expFrmField != null && expFrmField.frmFld.getValue() != null) {
                        if (expRepEntryKey != null && expRepEntryKey.length() > 0) {
                            mrudataCollector.setOldLoc(expFrmField.frmFld.getValue());
                        }
                    }
                }

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: expense report entry is not of detailed type!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't find expense report entry form!");
        }

    }

    @Override
    protected void ensureTwoVendorNameFields(List<ExpenseReportFormField> expRepFrmFlds) {

        ExpenseReportFormField vendorDescFrmFld = null;
        ExpenseReportFormField vendorListFrmFld = null;
        for (ExpenseReportFormField frmFld : expRepFrmFlds) {
            if (frmFld.getId().equalsIgnoreCase(ExpenseFormFieldViewListener.VENDOR_DESCRIPTION_FIELD_ID)) {
                vendorDescFrmFld = frmFld;
            } else if (frmFld.getId().equalsIgnoreCase(ExpenseFormFieldViewListener.VENDOR_LIST_KEY_FIELD_ID)) {
                vendorListFrmFld = frmFld;
            }
        }
        if (vendorDescFrmFld == null) {
            vendorDescFrmFld = new ExpenseReportFormField();
            vendorDescFrmFld.setLabel("Vendor Name");
            vendorDescFrmFld.setAccessType(AccessType.RW);
            vendorDescFrmFld.setId(ExpenseFormFieldViewListener.VENDOR_DESCRIPTION_FIELD_ID);
            vendorDescFrmFld.setControlType(ControlType.EDIT);
            vendorDescFrmFld.setDataType(DataType.CHAR);
            expRepFrmFlds.add(vendorDescFrmFld);
            Log.d(Const.LOG_TAG, CLS_TAG + ".ensureTwoVendorNameFields: added vendor desc field!");
        }
        if (vendorListFrmFld == null) {
            vendorListFrmFld = new ExpenseReportFormField();
            vendorListFrmFld.setLabel("Vendor Name");
            vendorListFrmFld.setAccessType(AccessType.RW);
            vendorListFrmFld.setId(ExpenseFormFieldViewListener.VENDOR_LIST_KEY_FIELD_ID);
            vendorListFrmFld.setControlType(ControlType.LIST_EDIT);
            vendorListFrmFld.setDataType(DataType.LIST);
            vendorListFrmFld.setListKey("VenLiKey");
            expRepFrmFlds.add(vendorListFrmFld);
            Log.d(Const.LOG_TAG, CLS_TAG + ".ensureTwoVendorNameFields: added vendor list field!");
        }
    }

    /**
     * Combine both vendor name fields, if both are found and editable into one combobox like field.
     */
    protected void combineVendorNameFields() {
        if (frmFldViewListener != null) {
            FormFieldView vendorDescFFV = frmFldViewListener
                    .findFormFieldViewById(ExpenseFormFieldViewListener.VENDOR_DESCRIPTION_FIELD_ID);
            FormFieldView vendorListFFV = frmFldViewListener
                    .findFormFieldViewById(ExpenseFormFieldViewListener.VENDOR_LIST_KEY_FIELD_ID);
            if (vendorDescFFV != null && vendorDescFFV.view != null
                    && vendorDescFFV.view.getVisibility() == View.VISIBLE && vendorListFFV != null
                    && vendorListFFV.getFormField().getAccessType() == AccessType.RW && vendorListFFV.view != null
                    && vendorListFFV.view.getVisibility() == View.VISIBLE) {
                if (vendorDescFFV instanceof InlineTextFormFieldView
                        && vendorListFFV instanceof SearchListFormFieldView) {
                    // Both fields are visible and editable. Create one
                    // combo-box like view from the two existing
                    // fields.
                    ViewGroup viewGroup = (ViewGroup) findViewById(R.id.entry_field_list);
                    int vendorDescChildIndex = -1;
                    if (vendorDescFFV.view != null) {
                        vendorDescChildIndex = viewGroup.indexOfChild(vendorDescFFV.view);
                    }
                    int vendorListChildIndex = -1;
                    if (vendorListFFV.view != null) {
                        vendorListChildIndex = viewGroup.indexOfChild(vendorListFFV.view);
                    }
                    int minIndex = Math.min(vendorDescChildIndex, vendorListChildIndex);
                    // Hide the two vendor fields.
                    FormUtil.hideFieldById(this, frmFldViewListener,
                            ExpenseFormFieldViewListener.VENDOR_LIST_KEY_FIELD_ID);
                    FormUtil.hideFieldById(this, frmFldViewListener,
                            ExpenseFormFieldViewListener.VENDOR_DESCRIPTION_FIELD_ID);
                    ExpenseReportFormField expRepFrmFld = new ExpenseReportFormField();
                    expRepFrmFld.setAccessType(AccessType.RW);
                    expRepFrmFld.setId("vendor_list_desc");
                    expRepFrmFld.setControlType(ControlType.LIST_EDIT);
                    expRepFrmFld.setDataType(DataType.LIST);
                    expRepFrmFld.setInputType(InputType.USER);
                    String vendorLabel = vendorDescFFV.getFormField().getLabel();
                    if (vendorDescFFV.getFormField().getAccessType() == AccessType.RO) {
                        vendorLabel = vendorListFFV.getFormField().getLabel();
                    }
                    expRepFrmFld.setLabel(vendorLabel);
                    expRepFrmFld.setRequired(vendorDescFFV.getFormField().isRequired()
                            || vendorListFFV.getFormField().isRequired());
                    ComboListFormFieldView comboFFV = new ComboListFormFieldView(expRepFrmFld,
                            (SearchListFormFieldView) vendorListFFV, (InlineTextFormFieldView) vendorDescFFV,
                            frmFldViewListener);
                    frmFldViewListener.getFormFieldViews().add(comboFFV);
                    if (minIndex > 0) {
                        ViewUtil.addSeparatorView(this, viewGroup, minIndex);
                        ++minIndex;
                    }
                    viewGroup.addView(comboFFV.getView(this), minIndex);
                } else {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG
                                    + ".combineVendorNameFields: either vendor desc or vendor list are not text or list fields!");
                }
            }
        }
    }

    /**
     * Will retrieve the last transaction date if exists and is non-expired and set it's value on a transaction date field
     * contained within an expense entry form.
     * 
     * @return will return <code>true</code> if the form field value was set; <code>false</code> otherwise.
     */
    protected boolean restoreLastTransactionDate() {
        boolean retVal = false;
        if (!ViewUtil.isLastDateExpired(this, Const.LAST_TRANS_DATE_LOCATION_EXPIRATION_MILLISECONDS)) {
            Calendar lastTransDate = ViewUtil.getLastTransDate(this);
            if (lastTransDate != null && frmFldViewListener != null) {
                FormFieldView ffView = frmFldViewListener
                        .findFormFieldViewById(ExpenseReportFormField.TRANSACTION_DATE_FIELD_ID);
                if (ffView instanceof DatePickerFormFieldView
                        && ffView.getFormField().getAccessType() == ExpenseReportFormField.AccessType.RW) {
                    // Set the last transaction date on the expense report
                    // entry.
                    expRepEntDet.transactionDateCalendar = lastTransDate;
                    expRepEntDet.transactionDate = FormatUtil.XML_DF_LOCAL.format(lastTransDate.getTime());
                    // Set the last transaction date on the form field view.
                    DatePickerFormFieldView datePickerView = (DatePickerFormFieldView) ffView;
                    datePickerView.setCurrentValue(lastTransDate, false);

                    retVal = true;
                }
            }
        }
        return retVal;
    }

    /**
     * Will look for a transaction date field within the current form and store it's value as the last saved transaction date.
     */
    protected void saveLastTransactionDate() {
        if (frmFldViewListener != null) {
            FormFieldView ffView = frmFldViewListener
                    .findFormFieldViewById(ExpenseReportFormField.TRANSACTION_DATE_FIELD_ID);
            if (ffView instanceof DatePickerFormFieldView) {
                DatePickerFormFieldView datePickerView = (DatePickerFormFieldView) ffView;
                if (datePickerView.hasValue()) {
                    Calendar datePickerDate = datePickerView.getCalendar();
                    String transDateStr = Format.safeFormatCalendar(FormatUtil.XML_DF, datePickerDate);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    long curTimeMillis = System.currentTimeMillis();
                    // Save the last quick expense transaction and current time.
                    getConcurCore().savePreference(prefs, Const.PREF_LAST_SAVED_DATE_TIME, curTimeMillis);
                    getConcurCore().savePreference(this, Const.PREF_LAST_SAVED_DATE, transDateStr);
                }
            }
        }
    }

    /**
     * Will retrieve the last location selection if it exists and is not expired and set it's value on a location field (city)
     * contained within an expense entry form.
     * 
     * @return will return <code>true</code>if the form field value was set; <code>false</code> otherwise.
     */
    protected boolean restoreLastLocationSelection() {
        boolean retVal = false;

        LocationSelection locSel = ViewUtil.getLocationSelection(this);
        String crnCode = ViewUtil.getLastUsedCrnCode(this);
        if (locSel != null && frmFldViewListener != null) {
            FormFieldView ffView = frmFldViewListener.findFormFieldViewById(ExpenseReportFormField.LOCATION_NAME);
            if (ffView instanceof SearchListFormFieldView
                    && ffView.getFormField().getAccessType() == ExpenseReportFormField.AccessType.RW) {
                SearchListFormFieldView srchLstFFView = (SearchListFormFieldView) ffView;
                if (!TextUtils.isEmpty(locSel.liKey)) {
                    srchLstFFView.listItemSelected(locSel.liCode, locSel.liKey, locSel.value);
                    srchLstFFView.setLiCrnCode(crnCode);
                    srchLstFFView.updateView();
                    frmFldViewListener.valueChanged(srchLstFFView);
                    retVal = true;
                }
            }
        }
        return retVal;
    }

    /**
     * Will look for a location (city) field within the current form and store it's value as the last location selection.
     */
    protected void saveLastLocationSelection() {
        if (frmFldViewListener != null) {
            FormFieldView ffView = frmFldViewListener.findFormFieldViewById(ExpenseReportFormField.LOCATION_NAME);
            if (ffView instanceof SearchListFormFieldView) {
                SearchListFormFieldView srchLstFFView = (SearchListFormFieldView) ffView;
                if (srchLstFFView.hasValue()) {
                    ViewUtil.saveLocationSelection(getConcurCore(), this, srchLstFFView.getLiKey(),
                            srchLstFFView.getLiCode(), srchLstFFView.getValue());
                }
            }
        }
    }

    /**
     * Will look for a currency field within the current form and store it's value as the last location selection.
     */
    protected void saveLastCurrencySelection() {
        if (frmFldViewListener != null) {
            FormFieldView ffView = frmFldViewListener
                    .findFormFieldViewById(ExpenseFormFieldViewListener.TRANSACTION_CURRENCY_FIELD_ID);
            if (ffView instanceof SearchListFormFieldView) {
                SearchListFormFieldView srchLstFFView = (SearchListFormFieldView) ffView;
                if (srchLstFFView.hasValue()) {
                    ViewUtil.saveLastUsedCrnCode(getConcurCore(), this, srchLstFFView.getLiCode());
                }
            }
        }
    }

    /**
     * Sets up any fields to be calculated.
     */
    protected void setCalculatedFields() {
        ExpenseReportFormField frmFld = getExpRepEntDet().findFormFieldByFieldId(
                ExpenseFormFieldViewListener.POSTED_AMOUNT_FIELD_ID);
        if (frmFld != null) {
            frmFld.setInputType(ExpenseReportFormField.InputType.CALC);
        }
    }

    /**
     * Will set the list of picklist items (for either a Breeze report or a personal card charge) based on values passed down in
     * the UserConfig response.
     */
    protected void setPaymentTypeFieldValues() {
        ExpenseReportEntryDetail expRepEntDet = getExpRepEntDet();
        if (expRepEntDet != null) {
            ExpenseReportFormField frmFld = expRepEntDet
                    .findFormFieldByFieldId(ExpenseFormFieldViewListener.PAYMENT_TYPE_FIELD_ID);
            if (frmFld != null) {
                ConcurCore app = getConcurCore();
                if (ViewUtil.isBreezeUser(app) || expRepEntDet.isPersonalCardCharge()) {
                    UserConfig userConfig = app.getUserConfig();
                    if (userConfig != null) {
                        frmFld.setStaticList(userConfig.getYodleePaymentTypeItems());
                    }
                }
            }
        }
    }

    /**
     * Will lock-down any fields that should be made read-only.
     */
    protected void lockDownReadOnlyFields() {
        // First, if itemization, check for fields that should be read-only.
        if (getExpRepEntDet().parentReportEntryKey != null) {
            String[] readOnlyFldIds = getItemizationEntryReadOnlyFieldsIds();
            if (readOnlyFldIds != null) {
                for (String fldId : readOnlyFldIds) {
                    ExpenseReportFormField frmFld = getExpRepEntDet().findFormFieldByFieldId(fldId);
                    if (frmFld != null) {
                        frmFld.setAccessType(ExpenseReportFormField.AccessType.RO);
                    }
                }
            }
        }
        // Second, handle corporate credit entries.
        if (getExpRepEntDet().isCreditCardCharge() && !getExpRepEntDet().isPersonalCardCharge()) {
            // First, ensure certain fields are marked read-only.
            String[] readOnlyFldIds = getCCTReadOnlyFieldIds();
            if (readOnlyFldIds != null) {
                for (String fldId : readOnlyFldIds) {
                    ExpenseReportFormField frmFld = getExpRepEntDet().findFormFieldByFieldId(fldId);
                    if (frmFld != null) {
                        frmFld.setAccessType(ExpenseReportFormField.AccessType.RO);
                    }
                }
            }
            // Second, check whether transaction date should be editable.
            ExpenseReportFormField frmFld = getExpRepEntDet().findFormFieldByFieldId(
                    ExpenseReportFormField.TRANSACTION_DATE_FIELD_ID);
            if (frmFld != null && ViewUtil.isCardTransDateEditable(this)) {
                frmFld.setAccessType(ExpenseReportFormField.AccessType.RW);
            }
        }
        // MOB-11451 If expense report entry is not a card charge, personal card
        // charge or itemization
        // then check for HasPostAmtCalc flag and if set, lock down Transaction
        // Amount field.
        if (!(expRepEntDet.isCreditCardCharge() || expRepEntDet.isPersonalCardCharge() || expRepEntDet.isItemization())) {
            IExpenseEntryCache expEntCache = getConcurCore().getExpenseEntryCache();
            List<ExpenseType> expenseTypes = expEntCache.getExpenseTypes();
            ExpenseType expTyp = expEntCache.getFilteredExpenseType(expenseTypes, expRepEntDet.expKey);
            if (expTyp != null && expTyp.hasPostAmtCalc != null && expTyp.hasPostAmtCalc == Boolean.TRUE) {
                ExpenseReportFormField frmFld = getExpRepEntDet().findFormFieldByFieldId(
                        ExpenseReportFormField.TRANSACTION_AMOUNT_FIELD_ID);
                if (frmFld != null) {
                    frmFld.setAccessType(ExpenseReportFormField.AccessType.RO);
                }
            }
        }
    }

    /**
     * Gets the list of form field ids that should be made read-only if this entry represents an itemization entry.
     * 
     * @return a list of form field ids that should be made read-only if this entry represents an itemization entry.
     */
    protected String[] getItemizationEntryReadOnlyFieldsIds() {
        return ITEMIZATION_READ_ONLY_FIELD_IDS;
    }

    /**
     * Gets the list of form field ids that should be made read-only if this entry is a corporate credit card transaction.
     * 
     * @return
     */
    protected String[] getCCTReadOnlyFieldIds() {
        return CCT_READ_ONLY_FIELD_IDS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getCopyDownPromptCharSequence()
     */
    @Override
    protected CharSequence getCopyDownPromptCharSequence() {
        return getResources().getQuantityString(R.plurals.dlg_expense_copy_down_fields_message_report_entry,
                missReqInvalidCopyDownFormFieldValues.size());
    }

    protected void populateExpenseEntryTitleHeader() {
        View view = findViewById(R.id.expense_entry_title_header);
        if (view != null) {
            if (expRepEntDet != null) {
                updateExpenseEntryRowView(view, expRepEntDet);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseEntryTitleHeader: expense report entry detail is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".populateExpenseEntryTitleHeader: unable to locate expense entry title header view!");
        }
    }

    /**
     * Will populate the view with expense details.
     */
    protected void populateFormFields() {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.entry_field_list);
        if (viewGroup != null) {
            List<FormFieldView> frmFldViews = populateExpenseDetailViewGroup(viewGroup, getExpRepEntDet());
            if (frmFldViews != null && frmFldViews.size() > 0) {
                if (frmFldViewListener != null) {
                    frmFldViewListener.setFormFieldViews(frmFldViews);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseDetails: frmFldViewListener is null!");
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseDetails: expense entry form field group not found!");
        }
    }

    /**
     * Will populate the tax form fields.
     */
    protected void populateTaxFormFields() {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.entry_tax_field_list);
        if (viewGroup != null) {
            List<FormFieldView> frmFldViews = populateTaxFormFieldsViewGroup(viewGroup, getExpRepEntDet());
            if (frmFldViews != null && frmFldViews.size() > 0) {
                if (frmFldViewListener != null) {
                    viewGroup.setVisibility(View.VISIBLE);
                    frmFldViewListener.setTaxFormFieldViews(frmFldViews);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseDetails: frmFldViewListener is null!");
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseDetails: expense entry form field group not found!");
        }
    }

    /**
     * Will populate the view with comments.
     */
    protected void populateExpenseComments() {
        try {
            ViewGroup comGroup = (ViewGroup) findViewById(R.id.entry_comment_list);
            if (expRepEntDet.getComments() != null && expRepEntDet.getComments().size() > 0) {
                ArrayList<ExpenseReportComment> comList = expRepEntDet.getComments();
                ListIterator<ExpenseReportComment> comIter = comList.listIterator();
                while (comIter.hasNext()) {
                    int curComIndex = comIter.nextIndex();
                    ExpenseReportComment expCom = comIter.next();
                    if (curComIndex > 0) {
                        ViewUtil.addSeparatorView(this, comGroup);
                    }
                    View comView = buildCommentView(expCom);
                    comView.setFocusable(true);
                    comView.setClickable(true);
                    final ExpenseReportComment selExpRepCom = expCom;
                    comView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            selComAuthor = FormatUtil.formatEmployeeName(selExpRepCom.getCommentBy());
                            selComDate = selExpRepCom.getFormattedCreationDate();
                            selComBody = selExpRepCom.getComment();
                            showDialog(Const.DIALOG_EXPENSE_VIEW_COMMENT);
                        }
                    });
                    comGroup.addView(comView);
                }
            } else {
                // Hide the comments view.
                comGroup = (ViewGroup) findViewById(R.id.entry_comments);
                if (comGroup != null) {
                    comGroup.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseComments: unable to locate comment group!");
                }
            }
        } catch (ClassCastException ccExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseComments: " + ccExc.getMessage(), ccExc);
        }
    }

    /**
     * Will populate the view with itemization/attendees view objects.
     */
    protected void populateItemizationAttendeeReceipt() {

        // Set up the attendee list that may be edited.
        if (editedAttendees == null) {
            editedAttendees = new ArrayList<ExpenseReportAttendee>();
            if (getExpRepEntDet().getAttendees() != null) {
                editedAttendees.addAll(getExpRepEntDet().getAttendees());
            }
        }
        boolean showingItemization = false;
        boolean showingAttendee = false;
        boolean showingReceipt = false;

        IExpenseEntryCache expEntCache = getConcurCore().getExpenseEntryCache();
        final List<ExpenseType> expenseTypes = expEntCache.getExpenseTypes(expRep.polKey);

        final boolean isItemized = getExpRepEntDet().isItemized();
        final boolean canBeItemized = (getExpRepEntDet().canBeItemized(expenseTypes) && (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE));

        boolean hasAttendees = getExpRepEntDet().hasAttendees();

        // Determine from the report policy key whether attendees are permitted.
        boolean canHaveAttendees = false;
        canHaveAttendees = (getExpRepEntDet().canHaveAttendees(expenseTypes) && isReportEditable());

        // Determine from the report policy key whether to use the hotel wizard.
        boolean useHotelWizard = false;
        useHotelWizard = (getExpRepEntDet().usesHotelWizard(expenseTypes) && isReportEditable());

        showingItemization = (canBeItemized || isItemized);
        showingReceipt = !isItemizationExpense() || isReportEntryWithEReceipt();
        if (isItemized || canBeItemized || canHaveAttendees || hasAttendees) {
            // Check for both "is itemized" and that this view isn't already
            // showing an itemized entry.
            if (isItemized || canBeItemized) {

                // Ensure the itemizations view is displayed.
                View itemizeView = findViewById(R.id.entry_itemization);
                if (itemizeView != null) {
                    itemizeView.setVisibility(View.VISIBLE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateItemizationAttendeeReceipt: unable to locate itemization view group!");
                }

                // Set the itemization label.
                int itemizationCount = (expRepEntDet.getItemizations() == null) ? 0 : expRepEntDet.getItemizations()
                        .size();
                TextView itemizationLabel = (TextView) findViewById(R.id.entry_itemization_label);
                if (itemizationLabel != null) {
                    itemizationLabel.setText(com.concur.mobile.base.util.Format.localizeText(this,
                            R.string.itemization_count, itemizationCount));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateItemizationAttendeeReceipt: unable to locate itemization label!");
                }

                Intent clickIntent = null;
                String flurryEvent = null;
                Map<String, String> flurryParams = null;
                if (!isItemized && useHotelWizard) {
                    clickIntent = new Intent(this, ExpenseHotelWizard.class);
                } else {
                    clickIntent = new Intent(this, ExpenseEntryItemization.class);
                    flurryEvent = Flurry.formatFlurryEvent(Flurry.CATEGORY_REPORT_ENTRY,
                            Flurry.EVENT_NAME_ITEMIZED_ENTRY_LIST);
                }
                clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
                clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, expRepEntDet.reportEntryKey);
                clickIntent.putExtra(Const.EXTRA_EXPENSE_PARENT_REPORT_ENTRY_KEY, expRepEntDet.reportEntryKey);
                clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
                viewClickHandler.addViewLauncherForResult(itemizeView, clickIntent, REQUEST_VIEW_ITEMIZATIONS,
                        flurryEvent, flurryParams);
                // Interject our own click listener to check for changed values.
                itemizeView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String rpeKey = getReportEntryKey();
                        if (rpeKey == null || rpeKey.length() == 0) {
                            processingItemizePressed = v;
                            showDialog(Const.DIALOG_EXPENSE_REQUIRE_ENTRY_SAVE_CONFIRM);
                        } else if (hasFormFieldsChanged() || changesPending()) {
                            processingItemizePressed = v;
                            showDialog(Const.DIALOG_EXPENSE_CONFIRM_SAVE_REPORT);
                        } else {
                            viewClickHandler.onClick(v);
                        }
                    }
                });
            } else {
                // Ensure the itemizations view is hidden.
                View itemizeView = findViewById(R.id.entry_itemization);
                if (itemizeView != null) {
                    itemizeView.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateItemizationAttendeeReceipt: unable to locate itemization view group!");
                }
            }

            if ((canHaveAttendees || (hasAttendees && !isReportEditable()) && !isItemized)) {
                showingAttendee = true;
                int attendeeResId = R.string.view_attendees;
                if (isReportEditable()) {
                    attendeeResId = R.string.view_add_attendees;
                }
                buildClickableFocusableAttendeeView(attendeeResId);
            } else {
                // Ensure the attendees view is hidden.
                View attendeeView = findViewById(R.id.entry_attendee);
                if (attendeeView != null) {
                    attendeeView.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateItemizationAttendeeReceipt: unable to locate attendee view group!");
                }
            }
        } else {
            // Hide itemization and attendees
            View itemizeView = findViewById(R.id.entry_itemization);
            if (itemizeView != null) {
                itemizeView.setVisibility(View.GONE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".populateItemizationAttendeeReceipt: unable to locate itemization view group!");
            }
            View attendeeView = findViewById(R.id.entry_attendee);
            if (attendeeView != null) {
                attendeeView.setVisibility(View.GONE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".populateItemizationAttendeeReceipt: unable to locate attendee view group!");
            }

        }

        // Show/Hide receipt.
        View receiptView = findViewById(R.id.entry_receipt);
        if (receiptView != null) {
            if (showingReceipt) {
                // Check for an active report or an approval with an entry
                // receipt.
                if ((reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE)
                        || (reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL && ((expRepEntDet.meKey != null && expRepEntDet
                                .hasMobileReceipt()) || expRepEntDet.receiptImageId != null))) {

                    // Set the correct label.
                    TextView txtView = (TextView) receiptView.findViewById(R.id.view_attach_receipts);
                    if (txtView != null) {
                        // Set the text resource id.
                        int viewReceiptsStrResId = R.string.view_receipt;
                        if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE) {
                            // If the entry already has a receipt, then set the
                            // label to 'view'.
                            if ((expRepEntDet.meKey != null && expRepEntDet.hasMobileReceipt())
                                    || expRepEntDet.receiptImageId != null || isReportEntryWithEReceipt()) {
                                viewReceiptsStrResId = R.string.view_receipt;
                            } else if (canEditReceipt()) {
                                // No receipt, so set the label to 'attach'.
                                viewReceiptsStrResId = R.string.attach_receipt;
                            } else {
                                // No receipt and not editable, so remove the
                                // receipt view.
                                showingReceipt = false;
                                receiptView.setVisibility(View.GONE);
                            }
                        }
                        txtView.setText(viewReceiptsStrResId);
                    } else {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG
                                        + ".populateItemizationAttendeeReceipt: unable to locate 'view_attach_receipts' text view!");
                    }
                    if (showingReceipt) {
                        // Attach the handler.
                        receiptView.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                if (!ViewUtil.isExternalMediaMounted()) {
                                    showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
                                } else {
                                    showDialog(DIALOG_RECEIPT_IMAGE);
                                }
                            }
                        });
                        receiptView.setVisibility(View.VISIBLE);
                    }
                } else {
                    showingReceipt = false;
                    receiptView.setVisibility(View.GONE);
                    // Hide the group shadown.
                    if (!showingAttendee && !showingItemization) {
                        View shadowView = findViewById(R.id.itemization_attendee_receipt_group_shadow);
                        if (shadowView != null) {
                            shadowView.setVisibility(View.GONE);
                        }
                    }
                }
            } else {
                showingReceipt = false;
                receiptView.setVisibility(View.GONE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateItemizationAttendeeReceipt: unable to locate receipt view!");
        }

        // TODO This needs to get cleaned up. The separators need to be in a
        // layout with the main group
        // they are all get hidden or shown at once.
        // Show/Hide the separators.
        // Itemization separator.
        View sepView = findViewById(R.id.entry_itemization_separator);
        if (sepView != null) {
            if (showingItemization && (showingAttendee || showingReceipt)) {
                sepView.setVisibility(View.VISIBLE);
            } else {
                sepView.setVisibility(View.GONE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".populateItemizationAttendeeReceipt: unable to locate itemization separator!");
        }
        // Attendee separator
        sepView = findViewById(R.id.entry_attendee_separator);
        if (sepView != null) {
            if (showingAttendee && showingReceipt) {
                sepView.setVisibility(View.VISIBLE);
            } else {
                sepView.setVisibility(View.GONE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateItemizationAttendeeReceipt: unable to locate attendee separator!");
        }

        // Check for whether entire section should be hidden.
        View itemizeAttendeeReceipt = findViewById(R.id.itemization_attendee_receipt_group);
        if (itemizeAttendeeReceipt != null) {
            if (!showingItemization && !showingAttendee && !showingReceipt) {
                itemizeAttendeeReceipt.setVisibility(View.GONE);
            } else {
                itemizeAttendeeReceipt.setVisibility(View.VISIBLE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".populateItemizationAttendeeReceipt: unable to locate itemize/attendee/receipt group!");
        }
    }

    /**
     * Will build a clickable and focusable attendee view object.
     * 
     * @return an instance of <code>View</code> that contains a clickable and focusable attendee view object.
     */
    protected View buildClickableFocusableAttendeeView(int textResId) {
        View view = findViewById(R.id.entry_attendee);
        // Set the attendee label.
        TextView txtView = (TextView) findViewById(R.id.entry_attendee_label);
        if (txtView != null) {
            int attendeeCount = expRepEntDet.countAttendeeInstances(editedAttendees) + expRepEntDet.noShowCount;
            txtView.setText(com.concur.mobile.base.util.Format.localizeText(this, R.string.attendee_count,
                    attendeeCount));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildClickableFocusableAttendeeView: unable to locate attendee label!");
        }
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent clickIntent = new Intent(ExpenseEntry.this, ExpenseEntryAttendee.class);
                clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
                clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, expRepEntDet.reportEntryKey);
                clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
                if (isReportEditable()) {
                    // Set the references to the attendees being edited.
                    getConcurCore().setEditedAttendees(editedAttendees);
                    // Set the current transaction currency code.
                    FormFieldView frmFldView = FormUtil.getFieldById(frmFldViewListener,
                            ExpenseFormFieldViewListener.TRANSACTION_CURRENCY_FIELD_ID);
                    if (frmFldView != null) {
                        if (frmFldView instanceof SearchListFormFieldView) {
                            SearchListFormFieldView srchLstFrmFldView = (SearchListFormFieldView) frmFldView;
                            clickIntent.putExtra(Const.EXTRA_EXPENSE_TRANSACTION_CURRENCY,
                                    srchLstFrmFldView.getLiCode());
                        } else {
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG
                                            + ".buildClickableFocusableAttendeeView.OnClickListener.onClick: search list form field view expected for transaction currency!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG
                                        + ".buildClickableFocusableAttendeeView.OnClickListener.onClick: form missing transaction currency field!");
                    }
                    // Set the current transaction amount.
                    frmFldView = FormUtil.getFieldById(frmFldViewListener,
                            ExpenseFormFieldViewListener.TRANSACTION_AMOUNT_FIELD_ID);
                    if (frmFldView != null) {
                        String transAmtStr = frmFldView.getCurrentValue();
                        if (transAmtStr != null && transAmtStr.length() > 0) {
                            try {
                                Double transAmt = FormatUtil.parseAmount(transAmtStr, Locale.getDefault());
                                if (transAmt != null) {
                                    clickIntent.putExtra(Const.EXTRA_EXPENSE_TRANSACTION_AMOUNT, transAmt);
                                } else {
                                    // No-op, invalid transaction amount.
                                }
                            } catch (NumberFormatException numFormExc) {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".applyCurrentExchangeRate: invalid transaction amount value of '"
                                        + transAmtStr + "'", numFormExc);
                            }
                        }
                    } else {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG
                                        + ".buildClickableFocusableAttendeeView.OnClickListener.onClick: form missing transaction amount field!");
                    }
                } else {
                    // Not editable. Set the amount for the attendee screen.
                    clickIntent.putExtra(Const.EXTRA_EXPENSE_TRANSACTION_AMOUNT, expRepEntDet.transactionAmount);
                }

                // Set the instance of ExpenseReportEntryDetail that is
                // currently being edited on the ConcurCore application
                // object.
                getConcurCore().setCurrentEntryDetailForm(getExpRepEntDet());
                startActivityForResult(clickIntent, REQUEST_VIEW_ATTENDEES);
            }
        });
        return view;
    }

    /**
     * Will return the instance of <code>ExpenseReportEntryDetail</code> that reflects either the current one being edited (based
     * on expense type change) or the current one.
     * 
     * @return the instance of ExpenseReportEntryDetail in use.
     */
    protected ExpenseReportEntryDetail getExpRepEntDet() {
        return ((editedRepEntDet != null) ? editedRepEntDet : expRepEntDet);
    }

    /**
     * Will populate the view with a box of report entry exceptions.
     * 
     * @param expRep
     *            the expense report.
     */
    protected void populateExpenseEntryExceptions() {

        try {
            ViewGroup excGroup = (ViewGroup) findViewById(R.id.entry_exception_list);
            if (expRepEntDet.getExceptions() != null && expRepEntDet.getExceptions().size() > 0) {
                ArrayList<ExpenseReportException> excList = expRepEntDet.getExceptions();
                populateExceptionViewGroup(excList, excGroup);
            } else {
                // Hide the exceptions view.
                excGroup = (ViewGroup) findViewById(R.id.entry_exceptions);
                if (excGroup != null) {
                    excGroup.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseEntryExceptions: unable to locate exception group!");
                }
            }
        } catch (ClassCastException ccExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseEntryExceptions: " + ccExc.getMessage(), ccExc);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.concur.mobile.activity.expense.AbstractExpenseActivity# getReportEntryExpenseName(com.concur.mobile.data.expense.
     * ExpenseReportEntry)
     */
    @Override
    protected String getReportEntryExpenseName(ExpenseReportEntry expRepEntry) {
        if (editedRepEntDet != null) {
            return editedRepEntDet.expenseName;
        } else {
            return super.getReportEntryExpenseName(expRepEntry);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#getReportEntryKey ()
     */
    @Override
    protected String getReportEntryKey() {
        String reportEntryKey = null;
        if (expRepEntDet != null) {
            reportEntryKey = expRepEntDet.reportEntryKey;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getReportEntryKey: expRepEntDet is null!");
        }
        return reportEntryKey;
    }

    @Override
    protected String getFormattedReportEntryAmount() {
        ExpenseReportEntryDetail expRepEnt = getExpRepEntDet();
        Locale loc = this.getResources().getConfiguration().locale;
        return FormatUtil.formatAmount((expRepEnt.transactionAmount != null) ? expRepEnt.transactionAmount : (0D), loc,
                expRepEnt.transactionCrnCode, true);
    }

    @Override
    protected String getReportEntryName() {
        ExpenseReportEntryDetail expRepEnt = getExpRepEntDet();
        return expRepEnt.expenseName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getReportEntryReceiptImageId()
     */
    @Override
    protected String getReportEntryReceiptImageId() {
        String receiptImageId = null;
        if (expRepEntDet != null) {
            if (!TextUtils.isEmpty(expRepEntDet.eReceiptImageId)) {
                receiptImageId = expRepEntDet.eReceiptImageId;
            } else if (!TextUtils.isEmpty(expRepEntDet.receiptImageId)) {
                receiptImageId = expRepEntDet.receiptImageId;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getReportEntryKey: expRepEntDet is null!");
            }
        }
        return receiptImageId;
    }

    @Override
    public boolean isEreceiptExpense() {
        boolean retVal = false;
        if (expRepEntDet != null) {
            if (!TextUtils.isEmpty(expRepEntDet.eReceiptId)) {
                retVal = true;
            }
        }
        return retVal;

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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# clearSelectedExpenseReportEntry()
     */
    @Override
    protected void clearSelectedExpenseReportEntry() {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getSelectedExpenseReportEntry()
     */
    @Override
    protected ExpenseReportEntry getSelectedExpenseReportEntry() {
        return expRepEntDet;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case REQUEST_VIEW_ITEMIZATIONS:
            if (resultCode == RESULT_OK) {
                // Set our result to be OK as well to force a view rebuild at
                // the previous activity
                Intent resultData = new Intent();
                resultData.putExtra(Const.EXTRA_APP_RESTART, appRestarted);
                setResult(RESULT_OK, resultData);
            }
            break;
        case REQUEST_VIEW_ATTENDEES: {
            if (resultCode == RESULT_OK) {
                Intent resultData = new Intent();
                resultData.putExtra(Const.EXTRA_APP_RESTART, appRestarted);
                // Set our result to be OK as well to force a view rebuild at
                // the previous activity
                setResult(RESULT_OK, resultData);
                // Set the flag that the attendees have changed.
                attendeesChanged = true;
                // Update the attendee view count.
                populateItemizationAttendeeReceipt();
            }
            break;
        }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.net.ContentFetcher.ContentFetcherListener# fetchCancelled(java.net.URL)
     */
    public void fetchCancelled(URL url) {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.expense_entry, menu);

        if (!((reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE && isReportEditable() && isSaveReportEnabled()) || (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW))) {
            menu.removeItem(R.id.menuSave);
        }

        if (isItemizationExpense() || !canEditReceipt()) {
            // Child entries don't get receipt access
            menu.removeItem(R.id.capture_receipt_picture);
            menu.removeItem(R.id.select_picture);
        } else {
            // Check for whether Receipt Store access is enabled.
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

        return true;
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
        if (itemId == R.id.menuSave) {
            save();
        } else if (itemId == R.id.capture_receipt_picture) {
            if (ConcurCore.isConnected()) {
                if (!isNewExpense()) {
                    if (hasFormFieldsChanged() || changesPending()) {
                        processingReceiptAction = ReceiptPictureSaveAction.TAKE_PICTURE;
                        ;
                        showDialog(Const.DIALOG_EXPENSE_CONFIRM_SAVE_REPORT);
                    } else {
                        captureReportEntryReceipt();
                    }
                } else {
                    processingReceiptAction = ReceiptPictureSaveAction.TAKE_PICTURE;
                    showDialog(Const.DIALOG_EXPENSE_REQUIRE_ENTRY_SAVE_CONFIRM);
                }
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
            retVal = true;
        } else if (itemId == R.id.select_receipt_picture) {
            if (ConcurCore.isConnected()) {
                if (!isNewExpense()) {
                    if (hasFormFieldsChanged() || changesPending()) {
                        processingReceiptAction = ReceiptPictureSaveAction.CHOOSE_PICTURE;
                        ;
                        showDialog(Const.DIALOG_EXPENSE_CONFIRM_SAVE_REPORT);
                    } else {
                        selectReportEntryReceipt();
                    }
                } else {
                    processingReceiptAction = ReceiptPictureSaveAction.CHOOSE_PICTURE;
                    showDialog(Const.DIALOG_EXPENSE_REQUIRE_ENTRY_SAVE_CONFIRM);
                }
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
            retVal = true;
        } else if (itemId == R.id.select_receipt_cloud_picture) {
            if (ConcurCore.isConnected()) {
                if (!isNewExpense()) {
                    if (hasFormFieldsChanged() || changesPending()) {
                        processingReceiptAction = ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD;
                        ;
                        showDialog(Const.DIALOG_EXPENSE_CONFIRM_SAVE_REPORT);
                    } else {
                        selectCloudReportEntryReceipt();
                    }
                } else {
                    processingReceiptAction = ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD;
                    showDialog(Const.DIALOG_EXPENSE_REQUIRE_ENTRY_SAVE_CONFIRM);
                }
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
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Indicates the end-user has selected the receipt image button.
     * 
     * @param view
     *            the view.
     */
    public void onReceiptViewClicked(View view) {

        if (ConcurCore.isConnected()) {
            // Create the intent.
            Intent clickIntent = new Intent(this, ExpenseReceipt.class);
            clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
            clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, expRepEntDet.reportEntryKey);
            clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
            // If there's a parent report entry key, then include that as well.
            if (expRepEntDet.parentReportEntryKey != null && expRepEntDet.parentReportEntryKey.length() > 0) {
                clickIntent.putExtra(Const.EXTRA_EXPENSE_PARENT_REPORT_ENTRY_KEY, expRepEntDet.parentReportEntryKey);
            }
            // If there's a receipt image ID, then add that to the intent.
            if (expRepEntDet.receiptImageId != null) {
                clickIntent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, expRepEntDet.receiptImageId);
            }
            // Kick-off the activity.
            startActivity(clickIntent);
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * Will transfer any edited values from <code>srcFldViews</code> to <code>dstFldViews</code>.
     * 
     * @param srcFldViews
     *            a list of source form field views.
     * @param dstFldViews
     *            a list of destination form field views.
     */
    protected void transferEditedValues(List<FormFieldView> srcFldViews, List<FormFieldView> dstFldViews) {
        if (dstFldViews != null) {
            // Create a map from form field id's to their new view objects.
            HashMap<String, FormFieldView> dstFrmFldMap = new HashMap<String, FormFieldView>();
            for (FormFieldView dstFldView : dstFldViews) {
                dstFrmFldMap.put(dstFldView.getFormField().getId(), dstFldView);
            }
            if (srcFldViews != null) {

                // MOB-11863 - Determine whether we have to preserve field
                // values from server that are in same hierarchy
                // (connected list) as the expense type.
                FormFieldView dstExpTypeFrmFldView = getHierarchyFormFieldViewToPreserve(srcFldViews, dstFldViews,
                        ExpenseReportFormField.EXPENSE_TYPE_LI_KEY_ID);

                // Iterate over 'srcFldViews' and if their values have changed,
                // then update the corresponding fields in 'dstFldViews'.
                for (FormFieldView srcFldView : srcFldViews) {
                    // Does the set of 'dstFldViews' contain a field id as
                    // 'srcFldView'?
                    if (dstFrmFldMap.containsKey(srcFldView.getFormField().getId()) && srcFldView.getView(this) != null) {
                        FormFieldView dstFldView = dstFrmFldMap.get(srcFldView.getFormField().getId());

                        // Determine whether we have to preserve the value in
                        // 'dstFldView' by not merging.
                        if (dstExpTypeFrmFldView != null) {
                            boolean descendantOfExpTypeLiKey = false;
                            if (dstFldView.getFormField().getHierKey() > 0
                                    && dstFldView != dstExpTypeFrmFldView
                                    && dstFldView.getFormField().getHierKey() == dstExpTypeFrmFldView.getFormField()
                                            .getHierKey()) {
                                descendantOfExpTypeLiKey = true;
                            }
                            if (!descendantOfExpTypeLiKey) {
                                // Update the currently displayed value in
                                // 'dstFldView' with the currently edited
                                // value from 'srcFldView'.
                                dstFldView.updateEditedValue(srcFldView);
                            }
                        } else {
                            // Update the currently displayed value in
                            // 'dstFldView' with the currently edited
                            // value from 'srcFldView'.
                            dstFldView.updateEditedValue(srcFldView);
                        }
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".regenerateFormFieldViews: existing report entry detail object has no form fields!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".regenerateFormFieldViews: new report entry detail has no form fields!");
        }
    }

    /**
     * Will determine whether there is a <code>FormFieldView</object> that is part of a connected list for which
     * the hierarchy values coming from the server should be preserved, i.e., not merged with a locally edited value.
     * 
     * @param srcFldViews
     *            contains the list of source form field view objects.
     * @param dstFldViews
     *            contain the list of destination form field view objects.
     * @param frmFldId
     *            contains the form field ID.
     * @return returns an instance of <code>FormFieldView</code> whose connected fields within the same hiearchy should have their
     *         values preserved from the server, i.e., not merged locally.
     */
    protected FormFieldView getHierarchyFormFieldViewToPreserve(List<FormFieldView> srcFldViews,
            List<FormFieldView> dstFldViews, String frmFldId) {
        FormFieldView retVal = null;
        if (srcFldViews != null && dstFldViews != null) {
            FormFieldView srcExpTypeLiKeyField = FormUtil.findFormFieldViewById(srcFldViews, frmFldId);
            FormFieldView dstExpTypeLiKeyField = FormUtil.findFormFieldViewById(dstFldViews, frmFldId);
            if (srcExpTypeLiKeyField != null
                    && dstExpTypeLiKeyField != null
                    && srcExpTypeLiKeyField.getFormField().getLiKey() != null
                    && dstExpTypeLiKeyField.getFormField().getLiKey() != null
                    && !srcExpTypeLiKeyField.getFormField().getLiKey()
                            .equalsIgnoreCase(dstExpTypeLiKeyField.getFormField().getLiKey())) {

                retVal = dstExpTypeLiKeyField;
            }
        }
        return retVal;
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of retrieving an exchange rate.
     * 
     * @author AndrewK
     */
    static class ExchangeRateReceiver extends BroadcastReceiver {

        private final String CLS_TAG = ExpenseEntry.CLS_TAG + "." + ExchangeRateReceiver.class.getSimpleName();

        // A reference to the activity.
        private ExpenseEntry activity;

        // A reference to the exchange rate request.
        private ExchangeRateRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ExchangeRateReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ExchangeRateReceiver(ExpenseEntry activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseEntry activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.exchangeRateRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the exchange rate request object associated with this broadcast receiver.
         * 
         * @param request
         *            the exchange rate request object associated with this broadcast receiver.
         */
        void setRequest(ExchangeRateRequest request) {
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
                activity.unregisterExchangeRateReceiver();

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
                                        activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }

                                    if (intent.hasExtra(Const.EXTRA_EXPENSE_EXCHANGE_RATE_KEY)) {
                                        String exchRateStr = intent
                                                .getStringExtra(Const.EXTRA_EXPENSE_EXCHANGE_RATE_KEY);
                                        if (exchRateStr != null && exchRateStr.length() > 0) {
                                            try {
                                                // Update the exchange rate
                                                // field.
                                                FormFieldView frmFldView = activity.frmFldViewListener
                                                        .findFormFieldViewById(ExpenseFormFieldViewListener.EXCHANGE_RATE_FIELD_ID);
                                                if (frmFldView != null) {
                                                    if (frmFldView instanceof InlineTextFormFieldView) {
                                                        InlineTextFormFieldView inTxtFrmFldView = (InlineTextFormFieldView) frmFldView;
                                                        // Format 'exchRate'
                                                        // according to the
                                                        // current locale.
                                                        Double amtDbl = FormatUtil.parseAmount(exchRateStr.trim(),
                                                                Locale.US);
                                                        if (amtDbl != null) {
                                                            exchRateStr = FormFieldView.deviceLocaleNumericFormatter
                                                                    .format(amtDbl);
                                                            inTxtFrmFldView.setCurrentValue(exchRateStr, true);
                                                        } else {
                                                            Log.e(Const.LOG_TAG, CLS_TAG
                                                                    + ".onReceive: invalid exchange rate '"
                                                                    + exchRateStr + "' retrieved from server!");
                                                        }
                                                    } else {
                                                        Log.e(Const.LOG_TAG,
                                                                CLS_TAG
                                                                        + ".onReceive: expected inline text form field view by id '"
                                                                        + ExpenseFormFieldViewListener.EXCHANGE_RATE_FIELD_ID
                                                                        + "' in form field view list!");
                                                    }
                                                } else {
                                                    Log.e(Const.LOG_TAG, CLS_TAG
                                                            + ".onReceive: unable to locate form field view by id '"
                                                            + ExpenseFormFieldViewListener.EXCHANGE_RATE_FIELD_ID
                                                            + "' in form field view list!");
                                                }
                                            } catch (NumberFormatException numFormExc) {
                                                Log.e(Const.LOG_TAG, CLS_TAG
                                                        + ".onReceive: invalid exchange rate value of '" + exchRateStr
                                                        + "'", numFormExc);
                                            }
                                        } else {
                                            Log.e(Const.LOG_TAG, CLS_TAG
                                                    + ".onReceive: success but null or empty exchange rate extra!");
                                        }
                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG
                                                + ".onReceive: success but missing exchange rate extra!");
                                    }
                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);
                                    // Display an error dialog.
                                    activity.showDialog(Const.DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE_FAILED);

                                    try {
                                        // Dismiss the dialog.
                                        activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE);
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
                                    activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            try {
                                // Dismiss the dialog.
                                activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }

                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + activity.lastHttpErrorMessage);
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                            try {
                                // Dismiss the dialog.
                                activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    }
                } else {
                    try {
                        // Dismiss the dialog.
                        activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }

                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear the request reference.
                activity.exchangeRateRequest = null;

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of retrieving a list of currencies.
     * 
     * @author Chris N. Diaz
     */
    static class CurrencySearchReceiver extends BroadcastReceiver {

        private final String CLS_TAG = ExpenseEntry.CLS_TAG + "." + CurrencySearchReceiver.class.getSimpleName();

        // A reference to the activity.
        private ExpenseEntry activity;

        // A reference to the currency search request.
        private SearchListRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        // Reference to the searched CRN code.
        private String crn;

        // Reference to the search query.
        private String crnName;

        // Reference to the search query.
        private boolean isLocationChangedForVat = false;

        /**
         * Constructs an instance of <code>CurrencySearchReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        CurrencySearchReceiver(ExpenseEntry activity, boolean isLocationChangedForVat) {
            this.activity = activity;
            this.isLocationChangedForVat = isLocationChangedForVat;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseEntry activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.currencySearchRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the exchange rate request object associated with this broadcast receiver.
         * 
         * @param request
         *            the exchange rate request object associated with this broadcast receiver.
         */
        void setRequest(SearchListRequest request, String crn, String crnName) {
            this.request = request;
            this.crn = crn;
            this.crnName = crnName;
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
                activity.unregisterCurrencySearchReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {

                                    // Dismiss the dialog.
                                    if (activity.currencySearchProgressDialog != null) {
                                        activity.currencySearchProgressDialog.dismiss();
                                    }

                                    if (intent.hasExtra(Const.EXTRA_EXPENSE_CURRENCY_SEARCH_RESULTS)) {
                                        SearchListResponse response = (SearchListResponse) intent
                                                .getSerializableExtra(Const.EXTRA_EXPENSE_CURRENCY_SEARCH_RESULTS);
                                        List<ListItem> listItems = response.listItems;
                                        String key = null;
                                        for (ListItem item : listItems) {
                                            String code = item.code;
                                            if (crn != null && code.equals(crn)) {
                                                key = item.key;
                                                // activity.selCurType=item;
                                                break;
                                            }
                                        }

                                        if (key != null) {
                                            FormFieldView frmFldView = activity.frmFldViewListener
                                                    .findFormFieldViewById(ExpenseFormFieldViewListener.TRANSACTION_CURRENCY_FIELD_ID);
                                            if (frmFldView != null) {
                                                if (frmFldView instanceof SearchListFormFieldView) {
                                                    SearchListFormFieldView currSearchView = (SearchListFormFieldView) frmFldView;

                                                    currSearchView.listItemSelected(crn, key, crnName);
                                                    currSearchView.commit();
                                                    currSearchView.updateView();

                                                    if (activity.frmFldViewListener != null) {
                                                        activity.frmFldViewListener.valueChanged(currSearchView);
                                                        if (isLocationChangedForVat) {
                                                            // call get tax form
                                                            String rpeKey = activity.getIntent().getExtras()
                                                                    .getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                                                            ExpenseType expType = ExpenseType.findExpenseType(
                                                                    activity.expRep.polKey,
                                                                    activity.getExpRepEntDet().expKey);
                                                            activity.isKeyElementChangedForVAT = isLocationChangedForVat;
                                                            activity.sendTaxFormRequest(rpeKey, expType);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);

                                    // Dismiss the dialog.
                                    if (activity.currencySearchProgressDialog != null) {
                                        activity.currencySearchProgressDialog.dismiss();
                                    }

                                }
                            } else {
                                activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage);

                                // Dismiss the dialog.
                                if (activity.currencySearchProgressDialog != null) {
                                    activity.currencySearchProgressDialog.dismiss();
                                }

                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                            }
                        } else {
                            // Dismiss the dialog.
                            if (activity.currencySearchProgressDialog != null) {
                                activity.currencySearchProgressDialog.dismiss();
                            }

                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + activity.lastHttpErrorMessage);

                            // Dismiss the dialog.
                            if (activity.currencySearchProgressDialog != null) {
                                activity.currencySearchProgressDialog.dismiss();
                            }
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                        }
                    }
                } else {
                    // Dismiss the dialog.
                    if (activity.currencySearchProgressDialog != null) {
                        activity.currencySearchProgressDialog.dismiss();
                    }

                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear the request reference.
                activity.currencySearchRequest = null;

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    } // end CurrencySearchReceiver

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of a save report entry action.
     * 
     * @author AndrewK
     */
    static class SaveReportEntryReceiver extends BroadcastReceiver {

        private final String CLS_TAG = ExpenseEntry.CLS_TAG + "." + SaveReportEntryReceiver.class.getSimpleName();

        // A reference to the activity.
        private ExpenseEntry activity;

        // A reference to the save report request.
        private SaveReportEntryRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>SaveReportEntryReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        SaveReportEntryReceiver(ExpenseEntry activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseEntry activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.saveReportEntryRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the save report entry request object associated with this broadcast receiver.
         * 
         * @param request
         *            the save report entry request object associated with this broadcast receiver.
         */
        void setRequest(SaveReportEntryRequest request) {
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
                activity.unregisterSaveReportEntryReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {

                                    activity.updateMRUs(intent, request);
                                    // Dismiss the dialog.
                                    activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY);

                                    // clear car configs from application, so
                                    // when user clicks to CCM, they will get
                                    // updated car
                                    // configs from server
                                    ConcurCore app = (ConcurCore) activity.getApplication();
                                    app.setCarConfigs(null);
                                    // Set the result for the calling activity.
                                    Intent i = new Intent();
                                    i.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY,
                                            activity.getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY));
                                    i.putExtra(
                                            Const.EXTRA_EXPENSE_REPORT_SOURCE,
                                            activity.getIntent().getIntExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE,
                                                    Const.EXPENSE_REPORT_SOURCE_ACTIVE));
                                    // Always request an updated report upon
                                    // save. This will be revisited in 7.3 to
                                    // prevent needing to grab an updated
                                    // detailed report.
                                    // i.putExtra(Const.EXPENSE_REPORT_UPDATE_DETAIL_KEY,
                                    // activity.isPersonalFieldChanged);
                                    i.putExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE,
                                            !activity.expRepEntDet.isItemization());
                                    i.putExtra(Const.EXTRA_APP_RESTART, activity.appRestarted);
                                    i.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
                                    activity.setResult(Activity.RESULT_OK, i);

                                    // Clear out any currently edited expense
                                    // report edit.
                                    activity.editedRepEntDet = null;

                                    // Set the flag to refresh the active report
                                    // list.
                                    IExpenseReportCache expRepCache = ((ConcurCore) activity.getApplication())
                                            .getExpenseActiveCache();
                                    expRepCache.setShouldRefreshReportList();

                                    if (activity.processingReceiptAction != null) {
                                        // We saved due to a receipt action on a
                                        // new expense entry.
                                        // Update the local detailed entry
                                        // object.
                                        String rpeKey = intent.getExtras().getString(
                                                Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                                        if (rpeKey != null && rpeKey.length() > 0) {
                                            // Update our activity entry detail
                                            // object. This should be sufficient
                                            // because it's
                                            // already in the cache
                                            // and the screen already represents
                                            // what was saved.
                                            activity.expRepEntDet = (ExpenseReportEntryDetail) expRepCache
                                                    .getReportEntry(activity.expRep, rpeKey);
                                            // Make sure the passed in intent to
                                            // this activity contains 'rpeKey'
                                            // as the
                                            // code that regenerates the display
                                            // will use this value to initialize
                                            // the 'expRepEntDet' object.
                                            Intent origIntent = activity.getIntent();
                                            origIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, rpeKey);
                                            // Ensure a new request is not made
                                            // to update the display if the
                                            // original
                                            // intent had the flag passed in.
                                            origIntent
                                                    .putExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, Boolean.FALSE);
                                            activity.setExpenseReport(origIntent);
                                        }
                                        // Kick-off the appropriate action.
                                        if (activity.processingReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE) {
                                            // Kick-off the choose picture
                                            // action.
                                            activity.selectReportEntryReceipt();
                                        } else if (activity.processingReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD) {
                                            // Kick-off the choose picture cloud
                                            // action.
                                            activity.selectCloudReportEntryReceipt();
                                        } else if (activity.processingReceiptAction == ReceiptPictureSaveAction.TAKE_PICTURE) {
                                            // Kick-off the capture picture
                                            // action.
                                            activity.captureReportEntryReceipt();
                                        }
                                        // Clear the flag.
                                        activity.processingReceiptAction = null;

                                    } else if (activity.processingItemizePressed != null) {
                                        // We saved due to an itemization. Click
                                        // on through. Update the intent if
                                        // needed. Update
                                        // the local entry detail object.
                                        String rpeKey = intent.getExtras().getString(
                                                Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                                        if (rpeKey != null && rpeKey.length() > 0) {

                                            // Update our activity entry detail
                                            // object. This should be sufficient
                                            // because it's
                                            // already in the cache
                                            // and the screen already represents
                                            // what was saved.
                                            activity.expRepEntDet = (ExpenseReportEntryDetail) expRepCache
                                                    .getReportEntry(activity.expRep, rpeKey);

                                            // Update the launch intent to have
                                            // the new keys (if needed)
                                            Intent launchIntent = activity.viewClickHandler
                                                    .getIntentForView(activity.processingItemizePressed);
                                            if (launchIntent != null) {
                                                String intentRpeKey = launchIntent.getExtras().getString(
                                                        Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                                                if (intentRpeKey != null && intentRpeKey.length() == 0) {
                                                    // Replace it with a good
                                                    // value
                                                    launchIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, rpeKey);
                                                    launchIntent.putExtra(Const.EXTRA_EXPENSE_PARENT_REPORT_ENTRY_KEY,
                                                            rpeKey);
                                                }
                                            }
                                            // NOTE: This is kind of
                                            // sledgehammer approach to updating
                                            // the
                                            // display. It's going to completely
                                            // re-create the
                                            // display.

                                            // Update the original intent to
                                            // have the new key
                                            Intent origIntent = activity.getIntent();
                                            if (origIntent != null) {
                                                String intentRpeKey = origIntent.getExtras().getString(
                                                        Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                                                if (intentRpeKey == null || intentRpeKey.length() == 0) {
                                                    // Replace it with a good
                                                    // value
                                                    origIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, rpeKey);
                                                }
                                            }
                                            // Ensure a new request is not made
                                            // to update the display if the
                                            // original
                                            // intent had the flag passed in.
                                            origIntent
                                                    .putExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, Boolean.FALSE);
                                            activity.setExpenseReport(origIntent);

                                        }
                                        activity.viewClickHandler.onClick(activity.processingItemizePressed);
                                        activity.processingItemizePressed = null;
                                    } else if (!activity.processingBackPressed && !activity.finishOnSave) {
                                        // If the save was not due to a
                                        // confirmed 'back' button press, then
                                        // rebuild the display;
                                        // otherwise, finish the activity.

                                        // Clear out any existing form fields
                                        // being edited.
                                        if (activity.frmFldViewListener != null) {
                                            activity.frmFldViewListener.clearCurrentFormFieldView();
                                            activity.frmFldViewListener.setFormFieldViews(null);
                                        }

                                        // NOTE: This is kind of sledgehammer
                                        // approach to updating the
                                        // display. It's going to completely
                                        // re-create the
                                        // display.
                                        Intent origIntent = activity.getIntent();
                                        // Ensure a new request is not made to
                                        // update the display if the original
                                        // intent had the flag passed in.
                                        origIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, Boolean.FALSE);
                                        activity.setExpenseReport(origIntent);
                                        // If the save operation was kicked-off
                                        // due to the end-user clicking the
                                        // 'Submit' button, then start the
                                        // submit process.
                                        if (activity.processingSubmitPressed) {
                                            activity.processingSubmitPressed = false;
                                            activity.startSubmitReportConfirmation();
                                        }
                                    } else {
                                        activity.finish();
                                    }
                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);
                                    // Display an error dialog.
                                    activity.showDialog(Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY_FAILED);

                                    // Dismiss the dialog.
                                    activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY);
                                }
                            } else {
                                activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage);
                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                                // Dismiss the dialog.
                                activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY);
                            }
                        } else {
                            // Dismiss the dialog.
                            activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY);

                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + activity.lastHttpErrorMessage);
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                            // Dismiss the dialog.
                            activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY);
                        }
                    }
                } else {
                    // Dismiss the dialog.
                    activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY);

                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear the 'processBackPressed' flag.
                activity.processingBackPressed = false;

                // Clear the request reference.
                activity.saveReportEntryRequest = null;

                // Clear the flag indicating a save operation is underway.
                activity.savingExpense = false;

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    private void updateMRUs(Intent intent, SaveReportEntryRequest request) {
        if (mrudataCollector != null) {
            // After getting report entry; save it to db
            // MRU
            MobileDatabase mdb = getConcurService().getMobileDatabase();
            String userId = getUserId();
            IExpenseReportCache cache = ((ConcurCore) getApplication()).getExpenseActiveCache();
            String rptKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
            // set old data for mruDataCollector
            ExpenseReportEntryDetail expRptEntDetail = (ExpenseReportEntryDetail) cache.getReportEntry(expRep, rptKey);
            List<ExpenseReportFormField> list = expRptEntDetail.getFormFields();
            if (list != null && list.size() > 0) {
                ExpenseReportFormField expFrmField = FormUtil.findFieldById(list,
                        ExpenseFormFieldViewListener.EXPENSE_TYPE_FIELD_ID);
                if (expFrmField.getLiKey() != null) {
                    mrudataCollector.setNewExpType(expFrmField.getLiKey());
                }
                expFrmField = FormUtil.findFieldById(list, ExpenseFormFieldViewListener.TRANSACTION_CURRENCY_FIELD_ID);
                if (expFrmField.getLiCode() != null) {
                    mrudataCollector.setNewCurType(expFrmField.getLiCode());
                }

                expFrmField = FormUtil.findFieldById(list, ExpenseFormFieldViewListener.LOCATION_FIELD_ID);
                if (expFrmField != null && expFrmField.getValue() != null) {
                    mrudataCollector.setNewLoc(expFrmField.getValue());
                }

            }
            if (userId != null && userId.length() > 0) {
                if (mrudataCollector.isNewExpType()) {
                    String polKey = request.getPolKey();
                    String expKey = request.getExpKey();
                    String replyExpKey = expRptEntDetail.expKey;
                    Boolean isItemization = expRptEntDetail.isItemized();

                    if (expKey.equalsIgnoreCase(replyExpKey)) {
                        new ExpTypeMruAsyncTask(mdb, userId, expKey, polKey, getConcurService()).execute();
                    } else {
                        // Check whether it is itemized entry.
                        if (isItemization != null && isItemization == Boolean.TRUE) {
                            ArrayList<ExpenseReportEntry> listOfItemization = expRptEntDetail.getItemizations();
                            if (listOfItemization != null) {
                                final int size = listOfItemization.size();
                                if (size > 0) {
                                    for (ExpenseReportEntry expenseReportEntry : listOfItemization) {
                                        if (expKey.equalsIgnoreCase(expenseReportEntry.expKey)) {
                                            new ExpTypeMruAsyncTask(mdb, userId, expKey, polKey, getConcurService())
                                                    .execute();
                                            break;
                                        }
                                    }// end of for.
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".updateMRu: listOfItemization is null");
                            }
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".updateMRu: user didnt select new expeType so no need to update MRU");
                }
                if (mrudataCollector.isNewCurType()) {
                    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    List<ExpenseReportFormField> formFields = expRptEntDetail.getFormFields();
                    ExpenseReportFormField formField = FormUtil.findFieldById(formFields,
                            ExpenseFormFieldViewListener.TRANSACTION_CURRENCY_FIELD_ID);
                    String code = formField.getLiCode();
                    String value = formField.getValue();
                    String fieldId = formField.getId();
                    now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    ListItem selCurType = new ListItem();
                    if (selCurType != null) {
                        selCurType.setUserID(userId);
                        selCurType.setLastUseCount(1);
                        selCurType.setLastUsed(now);
                        selCurType.code = code;
                        selCurType.text = value;
                        selCurType.fieldId = fieldId;
                    }
                    new ListItemMruAsyncTask(selCurType, mdb, userId, getConcurService()).execute();
                    // Save last used currency
                    saveLastCurrencySelection();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".updateMRu: user didnt select new currency Type so no need to update MRU");
                }

                if (mrudataCollector.isNewLocation()) {
                    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    List<ExpenseReportFormField> formFields = expRptEntDetail.getFormFields();
                    ExpenseReportFormField formField = FormUtil.findFieldById(formFields,
                            ExpenseFormFieldViewListener.LOCATION_FIELD_ID);
                    SearchListFormFieldView srchListFrmFldView = (SearchListFormFieldView) frmFldViewListener
                            .findFormFieldViewById(ExpenseFormFieldViewListener.LOCATION_FIELD_ID);
                    String key = formField.getLiKey();
                    String value = formField.getValue();
                    String fieldId = formField.getId();
                    String crnCode = srchListFrmFldView.getLiCrnCode();
                    String crnKey = srchListFrmFldView.getLiCrnKey();
                    now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    ListItem selLoc = new ListItem();
                    if (selLoc != null) {
                        selLoc.setUserID(userId);
                        selLoc.setLastUseCount(1);
                        selLoc.setLastUsed(now);
                        selLoc.code = key;
                        // MOB-14876 - Ensure that the 'key' value is set as
                        // location MRU items
                        // used in Quick Expenses (QE) have LiCode values, but
                        // not LiKey
                        // values. Whereas, for location report entry form
                        // fields, the opposite
                        // is true, form fields have values for 'LiKey' and not
                        // 'LiCode'.
                        selLoc.key = key;
                        selLoc.text = value;
                        selLoc.fieldId = fieldId;
                        List<ListItemField> fieldItems = new ArrayList<ListItemField>();
                        if (crnCode != null) {
                            fieldItems.add(new ListItemField(ListSearch.CODE_ID, crnCode));
                        }
                        if (crnKey != null) {
                            fieldItems.add(new ListItemField(ListSearch.KEY_ID, crnKey));
                        }
                        if (fieldItems != null) {
                            selLoc.fields = fieldItems;
                        }
                    }
                    new ListItemMruAsyncTask(selLoc, mdb, userId, getConcurService()).execute();
                    // save last used location
                    saveLastLocationSelection();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".updateMRu: user didnt select new location so no need to update MRU");
                }

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateMRu: userID is null");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateMRu: mrudataCollector is null");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of receiving notification of the outcomes of fetching
     * expense type information specific to the report policy.
     */
    protected static class ExpenseTypesReceiver extends BaseBroadcastReceiver<ExpenseEntry, GetExpenseTypesRequest> {

        /**
         * Constructs an instance of <code>ExpenseTypesReceiver</code>.
         * 
         * @param activity
         *            the base activity.
         */
        protected ExpenseTypesReceiver(ExpenseEntry activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(ExpenseEntry activity) {
            activity.expenseTypesRequest = null;
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_RETRIEVE_EXPENSE_TYPE_FAILED);
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            activity.populateItemizationAttendeeReceipt();
        }

        @Override
        protected void setActivityServiceRequest(GetExpenseTypesRequest request) {
            activity.expenseTypesRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterExpenseTypesReceiver();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_EXPENSE_TYPE_PROGRESS);
        }

    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of retrieving default attendee
     * information.
     */
    static class DefaultAttendeeReceiver extends BaseBroadcastReceiver<ExpenseEntry, DefaultAttendeeRequest> {

        /**
         * Constructs an instance of <code>DefaultAttendeeReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        DefaultAttendeeReceiver(ExpenseEntry activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(ExpenseEntry activity) {
            activity.defaultAttendeeRequest = null;

        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_DEFAULT_ATTENDEE_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_DEFAULT_ATTENDEE_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // No-op.
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(DefaultAttendeeRequest request) {
            activity.defaultAttendeeRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterDefaultAttendeeReceiver();
        }

    }

    /**
     * An extension of <code>FormFieldViewListener</code> for the purposes of handling form regeneration for an expense entry.
     * 
     * @author AndrewK
     */
    class ExpenseFormFieldViewListener extends FormFieldViewListener {

        private final String CLS_TAG = ExpenseEntry.CLS_TAG + '.' + ExpenseFormFieldViewListener.class.getSimpleName();

        static final String REPORT_CURRENCY_NAME_FIELD_ID = "CurrencyName";

        static final String EXCHANGE_RATE_FIELD_ID = "ExchangeRate";
        static final String POSTED_AMOUNT_FIELD_ID = "PostedAmount";
        static final String LOCATION_FIELD_ID = "LocName";
        static final String TRANSACTION_CURRENCY_FIELD_ID = "TransactionCurrencyName";
        static final String TRANSACTION_DATE_FIELD_ID = "TransactionDate";
        static final String TRANSACTION_AMOUNT_FIELD_ID = "TransactionAmount";
        static final String VENDOR_LIST_KEY_FIELD_ID = "VenLiKey";
        static final String VENDOR_DESCRIPTION_FIELD_ID = "VendorDescription";
        static final String IS_PERSONAL_FIELD_ID = "IsPersonal";
        static final String ATTENDEES_FIELD_ID = "Attendees";
        static final String EXPENSE_TYPE_FIELD_ID = "ExpKey";
        static final String PAYMENT_TYPE_FIELD_ID = "PatKey";
        static final String COUNTRY_FIELD_ID = "CtryCode";
        static final String COUNTRY_SUBCODE_FIELD_ID = "CtrySubCode";

        public ExpenseFormFieldViewListener(BaseActivity activity) {
            super(activity);
        }

        public ExpenseFormFieldViewListener(BaseActivity activity, ExpenseReport expenseReport,
                ExpenseReportEntry expenseReportEntry) {
            super(activity, expenseReport, expenseReportEntry);
        }

        public ExpenseFormFieldViewListener(BaseActivity activity, ExpenseReport expenseReport) {
            super(activity, expenseReport);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.util.FormFieldViewListener#regenerateFormFieldViews ()
         */
        @Override
        public void regenerateFormFieldViews() {

            // Second, set the instance of ExpenseReportEntry that this activity
            // is working with from the FormFieldViewListener as that will
            // contain
            // the ExpenseReportEntryDetail object that was fetched from the
            // server.
            ExpenseReportEntry expRepEnt = getExpenseReportEntry();
            if (expRepEnt instanceof ExpenseReportEntryDetail) {
                // Set the reference to the instance of ExpenseReportEntryDetail
                // that contains the
                // new set of form fields.
                editedRepEntDet = (ExpenseReportEntryDetail) expRepEnt;
                // Re-build the view.
                isKeyElementChangedForVAT = true;
                buildView();

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".regenerateFormFieldViews: expense report entry is not of type 'ExpenseReportEntryDetail'!");
            }
            // Clear any current selection left-over from the previous list of
            // fields.
            clearCurrentFormFieldView();

        }

        /**
         * Will handle changes to attendee information based on the current values of the transaction currency and transaction
         * amount field values.
         * 
         * @param curFrmFldView
         *            the transaction currency form field view.
         * @param amtFrmFldView
         *            the transaction amount form field view.
         */
        private void handleAttendeeForCurrencyAmountChanged(FormFieldView curFrmFldView, FormFieldView amtFrmFldView) {

            // Obtain the transaction currency code.
            String crnCode = null;
            if (curFrmFldView != null) {
                if (curFrmFldView instanceof SearchListFormFieldView) {
                    SearchListFormFieldView srchLstFrmFldView = (SearchListFormFieldView) curFrmFldView;
                    crnCode = srchLstFrmFldView.getLiCode();
                } else {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG
                                    + ".handleAttendeeForAmountCurrencyChanged: curFrmFldView is not of type SearchListFormFieldView!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleAttendeeForAmountCurrencyChanged: curFrmFldView is null!");
            }

            // Obtain the transaction amount.
            Double transAmt = null;
            if (amtFrmFldView != null) {
                String transAmtStr = amtFrmFldView.getCurrentValue();
                if (transAmtStr != null && transAmtStr.length() > 0) {
                    try {
                        transAmt = FormatUtil.parseAmount(transAmtStr, Locale.getDefault());
                    } catch (NumberFormatException numFormExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".handleAttendeeForAmountCurrencyChanged: invalid transaction amount value of '"
                                + transAmtStr + "'", numFormExc);
                    }
                }
            }

            // If the transaction currency code/amount is set and the attendees
            // is a non-empty list, then apply
            // the current transaction amount over the attendees.
            if (crnCode != null && transAmt != null && editedAttendees != null && editedAttendees.size() > 0) {
                getExpRepEntDet().divideAmountAmongAttendees(transAmt, crnCode, editedAttendees);
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.util.FormFieldViewListener#valueChanged(com.concur .mobile.util.FormFieldView)
         */
        @Override
        public void valueChanged(FormFieldView frmFldView) {

            // Log.d(Const.LOG_TAG, CLS_TAG + ".valueChanged: field '" +
            // frmFldView.getFormField().getLabel() + "("
            // + frmFldView.getFormField().getId() + ")' changed value!");

            // If the 'IsPersonal' field id has been changed, then call the
            // 'hasValueChanged' to determine whether
            // in fact the value is different then the original value.
            if (frmFldView.getFormField().getId().equalsIgnoreCase(IS_PERSONAL_FIELD_ID)) {
                isPersonalFieldChanged = frmFldView.hasValueChanged();
            }

            // Check for transaction currency field change.
            if (frmFldView.getFormField().getId().equalsIgnoreCase(TRANSACTION_CURRENCY_FIELD_ID)) {
                // Is the transaction currency the same as the report currency?
                if (frmFldView instanceof SearchListFormFieldView) {
                    SearchListFormFieldView srchLstFrmFldView = (SearchListFormFieldView) frmFldView;
                    // Apply to attendees.
                    handleAttendeeForCurrencyAmountChanged(frmFldView,
                            FormUtil.getFieldById(this, TRANSACTION_AMOUNT_FIELD_ID));

                    if (srchLstFrmFldView.getLiCode().equalsIgnoreCase(getExpenseReport().crnCode)) {
                        FormUtil.hideFieldById(ExpenseEntry.this, this, EXCHANGE_RATE_FIELD_ID);
                        FormUtil.hideFieldById(ExpenseEntry.this, this, POSTED_AMOUNT_FIELD_ID);
                    } else {
                        FormUtil.showFieldById(ExpenseEntry.this, this, EXCHANGE_RATE_FIELD_ID);
                        FormUtil.showFieldById(ExpenseEntry.this, this, POSTED_AMOUNT_FIELD_ID);
                        if (ConcurCore.isConnected()) {
                            FormFieldView transDateFrmFldView = FormUtil.getFieldById(this, TRANSACTION_DATE_FIELD_ID);
                            if (transDateFrmFldView instanceof DatePickerFormFieldView) {
                                DatePickerFormFieldView datePickerFrmFldView = (DatePickerFormFieldView) transDateFrmFldView;
                                Calendar date = datePickerFrmFldView.getCurrentValueAsCalendar();
                                sendExchangeRateRequest(srchLstFrmFldView.getLiCode(), getExpenseReport().crnCode, date);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".valueChanged: transaction date expected to be a date picker!");
                            }
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".valueChanged: search list form field view expected for transaction currency!");
                }
            }
            // Handle any date change that would impact re-computing the posted
            // amount, etc.
            if (frmFldView.getFormField().getId().equalsIgnoreCase(TRANSACTION_DATE_FIELD_ID)) {
                if (frmFldView instanceof DatePickerFormFieldView) {
                    DatePickerFormFieldView dtPckrFormFieldView = (DatePickerFormFieldView) frmFldView;
                    Calendar date = dtPckrFormFieldView.getCalendar();
                    FormFieldView transCurFrmFldView = findFormFieldViewById(TRANSACTION_CURRENCY_FIELD_ID);
                    if (transCurFrmFldView != null) {
                        if (transCurFrmFldView instanceof SearchListFormFieldView) {
                            SearchListFormFieldView srchLstFrmFldView = (SearchListFormFieldView) transCurFrmFldView;
                            if (!srchLstFrmFldView.getLiCode().equalsIgnoreCase(getExpenseReport().crnCode)) {
                                sendExchangeRateRequest(srchLstFrmFldView.getLiCode(), getExpenseReport().crnCode, date);
                            } else {
                                // call get tax form
                                String rpeKey = getIntent().getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                                ExpenseType expType = ExpenseType.findExpenseType(expRep.polKey,
                                        getExpRepEntDet().expKey);
                                isKeyElementChangedForVAT = true;
                                sendTaxFormRequest(rpeKey, expType);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".valueChanged: search list form field view expected for transaction currency!");
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".valueChanged: date picker form field expected for field id '"
                            + TRANSACTION_DATE_FIELD_ID + "'.");
                }
            }
            // Handle 'ExchangeRate' change.
            if (frmFldView.getFormField().getId().equalsIgnoreCase(EXCHANGE_RATE_FIELD_ID)) {
                applyCurrentExchangeRate();
                Log.d(Const.LOG_TAG, CLS_TAG + ".valueChanged: currency didnt change");
            }
            // Handle 'TransactionAmount' change.
            if (frmFldView.getFormField().getId().equalsIgnoreCase(TRANSACTION_AMOUNT_FIELD_ID)) {
                // Check for transaction currency being different then report
                // currency.
                FormFieldView transCurFrmFldView = findFormFieldViewById(TRANSACTION_CURRENCY_FIELD_ID);
                // Apply to attendees.
                handleAttendeeForCurrencyAmountChanged(transCurFrmFldView, frmFldView);
                if (transCurFrmFldView != null) {
                    if (transCurFrmFldView instanceof SearchListFormFieldView) {
                        SearchListFormFieldView srchLstFrmFldView = (SearchListFormFieldView) transCurFrmFldView;
                        if (!srchLstFrmFldView.getLiCode().equalsIgnoreCase(getExpenseReport().crnCode)) {
                            applyCurrentExchangeRate();
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".valueChanged: search list form field view expected for transaction currency!");
                    }
                }
            }

            // If the vendor has been selected from the search, update any
            // editable vendor description field.
            if (frmFldView.getFormField().getId().equalsIgnoreCase(VENDOR_LIST_KEY_FIELD_ID)) {
                // Clear the 'value' of a vendor description field.
                FormFieldView venDescFFV = findFormFieldViewById(VENDOR_DESCRIPTION_FIELD_ID);
                if (venDescFFV != null && venDescFFV.getFormField().getAccessType() == AccessType.RW) {
                    if (frmFldView instanceof SearchListFormFieldView) {
                        venDescFFV.setCurrentValue("", false);
                    }
                }
                // If this field is part of a ComboListFormFieldView, then set
                // the ValueSource of the combolist field
                // to the value of SEARCH.
                ComboListFormFieldView clFFV = FormUtil.findReferencedSearchListFormFieldView(this, frmFldView
                        .getFormField().getId());
                if (clFFV != null) {
                    clFFV.setValueSource(ComboListFormFieldView.ValueSource.SEARCH);
                }
            }

            // If the end-user has typed in a vendor description field, then
            // update any editable vendor list key field.
            if (frmFldView.getFormField().getId().equalsIgnoreCase(VENDOR_DESCRIPTION_FIELD_ID)) {
                // Clear any values out of a vendor search list field.
                FormFieldView venLiKeyFFV = findFormFieldViewById(VENDOR_LIST_KEY_FIELD_ID);
                if (venLiKeyFFV != null && venLiKeyFFV.getFormField().getAccessType() == AccessType.RW) {
                    if (venLiKeyFFV instanceof SearchListFormFieldView) {
                        SearchListFormFieldView srchLstFrmFldView = (SearchListFormFieldView) venLiKeyFFV;
                        srchLstFrmFldView.clear();
                    }
                }
                // If this field is part of a ComboListFormFieldView, then set
                // the ValueSource of the combolist field
                // to the value of INLINE.
                ComboListFormFieldView clFFV = FormUtil.findReferencedInlineTextFormFieldView(this, frmFldView
                        .getFormField().getId());
                if (clFFV != null) {
                    clFFV.setValueSource(ComboListFormFieldView.ValueSource.INLINE);
                }
            }

            // If the expense type has been changed to one requiring attendees,
            // then ensure that
            // if the end-user is supposed to be the default attendee, that it's
            // in the current attendee list.
            if (frmFldView.getFormField().getId().equalsIgnoreCase(EXPENSE_TYPE_FIELD_ID)) {
                if (frmFldView instanceof ExpenseTypeFormFieldView) {

                    ExpenseTypeFormFieldView expTypFrmFldView = (ExpenseTypeFormFieldView) frmFldView;
                    String selExpKey = expTypFrmFldView.getCurrentValue();
                    // Expense doesn't have attendees, if this expense is not an
                    // itemization, then use the parent expense types
                    // loaded at app start-up time.
                    boolean canHaveAttendees = false;
                    IExpenseEntryCache expEntCache = getConcurCore().getExpenseEntryCache();
                    List<ExpenseType> expTypes = null;
                    if (!getExpRepEntDet().isItemization()) {
                        expTypes = expEntCache.getExpenseTypes();
                        canHaveAttendees = getExpRepEntDet().canHaveAttendees(expTypes);
                    } else {
                        // Expense is an itemization, examine whether the
                        // expense entry cache already has policy-specific
                        // expense types loaded, if so, then use them.
                        expTypes = expEntCache.getExpenseTypes(expRep.polKey);
                        canHaveAttendees = getExpRepEntDet().canHaveAttendees(expTypes);
                    }
                    if (canHaveAttendees) {
                        // Determine whether default attendee should be added to
                        // the attendee list.
                        if (expenseTypeHasDefaultAttendee(expTypes, selExpKey)) {
                            if (addDefaultAttendee()) {
                                // Apply to attendees.
                                handleAttendeeForCurrencyAmountChanged(
                                        FormUtil.getFieldById(this, TRANSACTION_CURRENCY_FIELD_ID),
                                        FormUtil.getFieldById(this, TRANSACTION_AMOUNT_FIELD_ID));
                                // Set the flag that attendees have changed.
                                attendeesChanged = true;
                                // Set the attendee label.
                                TextView txtView = (TextView) findViewById(R.id.entry_attendee_label);
                                if (txtView != null) {
                                    int attendeeCount = expRepEntDet.countAttendeeInstances(editedAttendees)
                                            + expRepEntDet.noShowCount;
                                    txtView.setText(com.concur.mobile.base.util.Format.localizeText(ExpenseEntry.this,
                                            R.string.attendee_count, attendeeCount));
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".buildClickableFocusableAttendeeView: unable to locate attendee label!");
                                }
                            }
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".valueChanged: expense type form field not of type ExpenseTypeFormFieldView!");
                }
            }

            // MOB-11190 - If the Location expense type has been changed,
            // then update the currency field to the corresponding CRN.
            if (frmFldView.getFormField().getId().equalsIgnoreCase(LOCATION_FIELD_ID)) {

                if (frmFldView instanceof SearchListFormFieldView) {

                    SearchListFormFieldView srchLstFrmFldView = (SearchListFormFieldView) frmFldView;
                    String selectedListItemKey = srchLstFrmFldView.getLiKey();
                    FormFieldView transCurFrmFldView = findFormFieldViewById(TRANSACTION_CURRENCY_FIELD_ID);

                    if (selectedListItemKey != null
                            && transCurFrmFldView != null
                            && transCurFrmFldView.getFormField().getAccessType() == ExpenseReportFormField.AccessType.RW
                            && transCurFrmFldView instanceof SearchListFormFieldView) {

                        String crn = srchLstFrmFldView.getLiCrnCode();
                        if (crn != null) {

                            String crnName = null;
                            // Get the list of currency types and
                            // iterate through it
                            // until we find the corresponding CRN
                            // name - it will be used
                            // as the search query.
                            ConcurCore ConcurCore = (ConcurCore) ExpenseEntry.this.getApplication();
                            IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
                            ArrayList<ListItem> curTypes = expEntCache.getCurrencyTypes();
                            if (curTypes != null && curTypes.size() > 0) {
                                for (ListItem currency : curTypes) {
                                    if (currency.code.equals(crn)) {
                                        crnName = currency.text;
                                        break;
                                    }
                                }
                            }

                            FormFieldView transCurrencyFF = FormUtil.getFieldById(this, TRANSACTION_CURRENCY_FIELD_ID);
                            String curCurrency = null;
                            if (transCurrencyFF != null) {
                                curCurrency = transCurrencyFF.getCurrentValue();
                            }
                            if ((curCurrency != null && curCurrency.length() > 0)) {
                                if (crnName != null) {
                                    if (!(crnName.equalsIgnoreCase(curCurrency))) {
                                        // We got the CRN name, so
                                        // update the values!
                                        if (crnName != null) {
                                            SearchListFormFieldView currSearchView = (SearchListFormFieldView) transCurFrmFldView;
                                            boolean isLocationChangedForVAT = true;
                                            sendCurrencySearchRequest(crn, crnName, currSearchView.getFormField()
                                                    .getId(), isLocationChangedForVAT);
                                        }
                                    } else {
                                        // call get tax form
                                        String rpeKey = getIntent().getExtras().getString(
                                                Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                                        ExpenseType expType = ExpenseType.findExpenseType(expRep.polKey,
                                                getExpRepEntDet().expKey);
                                        isKeyElementChangedForVAT = true;
                                        sendTaxFormRequest(rpeKey, expType);
                                        Log.d(Const.LOG_TAG, CLS_TAG + ".valueChanged: currency didnt change");
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".valueChanged: crnName is null");
                                }
                            }
                        }

                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".valueChanged: search list form field view expected for location name!");
                }
            }

            // Handle any cntry code change
            if (frmFldView.getFormField().getId().equalsIgnoreCase(COUNTRY_FIELD_ID)) {
                if (frmFldView instanceof SearchListFormFieldView) {
                    // HAK HAK MOB15087:Handle any country sub code change as this frmField is not connected to country frmField.
                    FormFieldView subFrmFldView = findFormFieldViewById(COUNTRY_SUBCODE_FIELD_ID);
                    if (subFrmFldView != null) {
                        if (subFrmFldView instanceof SearchListFormFieldView) {
                            ((SearchListFormFieldView) subFrmFldView).setLiKey(null);
                            ((SearchListFormFieldView) subFrmFldView).setValue("");
                            ((SearchListFormFieldView) subFrmFldView).setLiCode(null);
                            ((SearchListFormFieldView) subFrmFldView).updateView();
                        }
                    }
                    // call get tax form
                    String rpeKey = getIntent().getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                    ExpenseType expType = ExpenseType.findExpenseType(expRep.polKey, getExpRepEntDet().expKey);
                    isKeyElementChangedForVAT = true;
                    sendTaxFormRequest(rpeKey, expType);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".valueChanged: date picker form field expected for field id '"
                            + TRANSACTION_DATE_FIELD_ID + "'.");
                }
            }

            // Handle any cntry code change
            if (frmFldView.getFormField().getId().equalsIgnoreCase(COUNTRY_SUBCODE_FIELD_ID)) {
                if (frmFldView instanceof SearchListFormFieldView) {
                    // call get tax form
                    String rpeKey = getIntent().getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                    ExpenseType expType = ExpenseType.findExpenseType(expRep.polKey, getExpRepEntDet().expKey);
                    isKeyElementChangedForVAT = true;
                    sendTaxFormRequest(rpeKey, expType);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".valueChanged: date picker form field expected for field id '"
                            + TRANSACTION_DATE_FIELD_ID + "'.");
                }
            }

            // Check if this field is dynamic (Dynamic means it drives visibility of other fields)
            // Currently dynamic field doesn't support (ConnectedListFormFieldView)
            // ExpenseTypeFormFieldView, SearchListFormFieldView cannot be a custom field? (to confirm?)
            checkConditionalFieldActions(frmFldView);
        }

        /**
         * Will send off a request to retrieve the exchange rate for converting amounts from <code>fromCrnCode</code> to
         * <code>toCrnCode</code> for the date <code>date</code>.
         * 
         * @param fromCrnCode
         *            the currency code of the currency to convert from.
         * @param toCrnCode
         *            the currency code of the currency to convert to.
         * @param date
         *            the exchange rate date.
         */
        private void sendExchangeRateRequest(String fromCrnCode, String toCrnCode, Calendar date) {
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            ConcurService concurService = ConcurCore.getService();
            registerExchangeRateReceiver();
            exchangeRateRequest = concurService.sendExchangeRateRequest(getUserId(), fromCrnCode, toCrnCode, date);
            if (exchangeRateRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendExchangeRateRequest: unable to create 'ExchangeRate' request!");
                unregisterExchangeRateReceiver();
            } else {
                // Set the request object on the receiver.
                exchangeRateReceiver.setRequest(exchangeRateRequest);
                // Show the fetching exchange rate dialog.
                ExpenseEntry.this.showDialog(Const.DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE);
            }
        }

        private void sendCurrencySearchRequest(String crn, String crnName, String fieldId,
                boolean isLocationChangedForVat) {

            if (ConcurCore.isConnected() && crn != null && crnName != null) {

                ConcurCore app = (ConcurCore) getApplication();

                // Register a receiver to handle the request.
                registerCurrencySearchReceiver(isLocationChangedForVat);

                currencySearchRequest = app.getService().sendCurrencySearchRequest(getUserId(), crnName, fieldId,
                        expRep.reportKey);

                if (currencySearchRequest == null) {
                    // Unregister the receiver.
                    unregisterCurrencySearchReceiver();
                } else {
                    // Set the request on the receiver.
                    currencySearchReceiver.setRequest(currencySearchRequest, crn, crnName);
                    // Show the fetching currency rate dialog.
                    currencySearchProgressDialog = ProgressDialog.show(ExpenseEntry.this, "",
                            getText(R.string.title_header_progress_text), true, true);

                }
            }
        } // sendCurrencySearchRequest()

        /**
         * Will perform an required initialization over the form fields.
         */
        @Override
        public void initFields() {
            // Handle whether to show foreign amounts and posted amounts based
            // on transaction
            // currency and report currency.
            FormFieldView frmFldView = FormUtil.getFieldById(this, TRANSACTION_CURRENCY_FIELD_ID);
            if (frmFldView != null) {
                if (frmFldView instanceof SearchListFormFieldView) {
                    SearchListFormFieldView srchLstFrmFldView = (SearchListFormFieldView) frmFldView;
                    if (srchLstFrmFldView.getLiCode().equalsIgnoreCase(getExpenseReport().crnCode)) {
                        FormUtil.hideFieldById(ExpenseEntry.this, this, EXCHANGE_RATE_FIELD_ID);
                        FormUtil.hideFieldById(ExpenseEntry.this, this, POSTED_AMOUNT_FIELD_ID);
                    } else {
                        FormUtil.showFieldById(ExpenseEntry.this, this, EXCHANGE_RATE_FIELD_ID);
                        FormUtil.showFieldById(ExpenseEntry.this, this, POSTED_AMOUNT_FIELD_ID);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".initFields: search list form field view expected for transaction currency!");
                }
            }

            // Override label on 'PostedAmount' field to say 'Amount in <Report
            // Currency>'.
            frmFldView = FormUtil.getFieldById(this, POSTED_AMOUNT_FIELD_ID);
            if (frmFldView != null) {
                String reportCurrencyName = expRep.crnCode;
                ExpenseReportFormField frmFld = ((ExpenseReportDetail) expRep)
                        .getFormField(REPORT_CURRENCY_NAME_FIELD_ID);
                if (frmFld != null) {
                    reportCurrencyName = frmFld.getValue();
                }
                String postedAmountLabel = com.concur.mobile.base.util.Format.localizeText(ExpenseEntry.this,
                        R.string.amount_in_currency, reportCurrencyName);
                CharSequence label = frmFldView.buildLabel(postedAmountLabel);
                frmFldView.setTextViewText(frmFldView.getView(ExpenseEntry.this), R.id.field_name, label);
            }
            // Override label on 'TransactionAmount' to say 'Amount'.
            frmFldView = FormUtil.getFieldById(this, TRANSACTION_AMOUNT_FIELD_ID);
            if (frmFldView != null) {
                frmFldView.setFieldLabel(ExpenseEntry.this.getText(R.string.amount).toString());
                // Clearing transaction amount field on new expense form.
                if (expRepEntDet.reportEntryKey == null || expRepEntDet.reportEntryKey.length() == 0) {
                    String curValue = frmFldView.getCurrentValue();
                    // MOB-12782
                    Double value = FormatUtil.parseAmount(curValue, getResources().getConfiguration().locale);
                    if (value != null && value == 0) {
                        frmFldView.setCurrentValue("", false);
                    }
                }

                // MOB-10638 - Company Car Mileage can have 0 amount value.
                ExpenseReportFormField frmFld = frmFldView.getFormField();
                if (frmFld != null) {

                    // Need to get the currently selected EpenseType instead
                    // of using the expCode in chase the user changed the
                    // ExpenseType.
                    FormFieldView expTypeView = FormUtil.getFieldById(this, EXPENSE_TYPE_FIELD_ID);
                    if (expTypeView != null) {

                        String expType = expTypeView.getCurrentValue();
                        if (Const.EXPENSE_TYPE_COMPANY_MILEAGE.equals(expType)
                                || Const.EXPENSE_CODE_COMPANY_MILEAGE.equals(expType)) {
                            frmFld.setVerifyValue(false);
                        } else {
                            frmFld.setVerifyValue(true);
                        }
                    }
                }

            }

            // Hide the 'IsPersonal' field if this entry has itemizations.
            if (expRepEntDet != null) {
                // Does this entry have itemizations?
                if (expRepEntDet.getItemizations() != null && expRepEntDet.getItemizations().size() > 0) {
                    FormUtil.hideFieldById(ExpenseEntry.this, this, IS_PERSONAL_FIELD_ID);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initFields: expRepEntDet is null!");
            }

            // If the form contains the 'VendorLiKey' field and there is no
            // 'LiKey' set on the field, then
            // hide the field.
            frmFldView = FormUtil.getFieldById(this, VENDOR_LIST_KEY_FIELD_ID);
            if (frmFldView != null
                    && (frmFldView.getFormField().getListKey() == null || frmFldView.getFormField().getListKey()
                            .length() == 0)) {
                FormUtil.hideFieldById(ExpenseEntry.this, this, VENDOR_LIST_KEY_FIELD_ID);
            }

            // If the form contains the 'Attendees' field, then hide it.
            FormUtil.hideFieldById(ExpenseEntry.this, this, ATTENDEES_FIELD_ID);
        }

        /**
         * Will apply an exchange rate to the transaction amount to set the posted amount.
         * 
         * @param exchangeRate
         */
        public void applyCurrentExchangeRate() {

            FormFieldView frmFldView = findFormFieldViewById(EXCHANGE_RATE_FIELD_ID);
            if (frmFldView != null) {
                String exchRateStr = frmFldView.getCurrentValue();
                if (exchRateStr != null && exchRateStr.length() > 0) {
                    try {
                        Double exchRate = FormatUtil.parseAmount(exchRateStr.trim(), Locale.getDefault());
                        if (exchRate != null) {
                            // Obtain the current transaction amount.
                            frmFldView = findFormFieldViewById(TRANSACTION_AMOUNT_FIELD_ID);
                            if (frmFldView != null) {
                                String transAmtStr = frmFldView.getCurrentValue();
                                if (transAmtStr != null && transAmtStr.length() > 0) {
                                    try {
                                        Double transAmt = FormatUtil.parseAmount(transAmtStr, Locale.getDefault());
                                        if (transAmt != null) {
                                            Double postedAmt = transAmt * exchRate;
                                            // Update the postedAmt field.
                                            frmFldView = findFormFieldViewById(POSTED_AMOUNT_FIELD_ID);
                                            if (frmFldView != null) {
                                                if (frmFldView instanceof InlineTextFormFieldView) {
                                                    InlineTextFormFieldView inTxtFrmFldView = (InlineTextFormFieldView) frmFldView;
                                                    inTxtFrmFldView.setCurrentValue(FormatUtil
                                                            .formatAmount(postedAmt, ConcurCore.getContext()
                                                                    .getResources().getConfiguration().locale,
                                                                    expRep.crnCode, false), false);
                                                }
                                            }
                                        } else {
                                            // No-op, invalid transaction
                                            // amount.
                                        }
                                    } catch (NumberFormatException numFormExc) {
                                        Log.e(Const.LOG_TAG, CLS_TAG
                                                + ".applyCurrentExchangeRate: invalid transaction amount value of '"
                                                + transAmtStr + "'", numFormExc);
                                    }
                                }
                            }
                        } else {
                            // No-op, invalid exchange rate.
                        }
                    } catch (NumberFormatException numFormExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".applyCurrentExchangeRate: invalid exchange rate value of '"
                                + exchRateStr + "'", numFormExc);
                    }
                }
            }
        }

    }

    protected void sendTaxFormRequest(String expRepEntryKey, ExpenseType selExpType) {
        if (selExpType != null && (selExpType.hasTaxForm != null && selExpType.hasTaxForm == Boolean.TRUE)) {
            if (isKeyElementChangedForVAT) {
                callGetTaxForm(expRepEntryKey);
            } else {
                List<TaxForm> taxForms = getExpRepEntDet().getTaxForm();
                if (taxForms == null || taxForms.size() == 0) {
                    callGetTaxForm(expRepEntryKey);
                }
            }
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".sendTaxFormRequest: exp.hasTaxform is false");
        }
    }

    private void callGetTaxForm(String expRepEntryKey) {
        isKeyElementChangedForVAT = false;
        if (ConcurCore.isConnected()) {
            String expKey = getExpRepEntDet().expKey;
            List<FormFieldView> frmfieldViews = frmFldViewListener.getFormFieldViews();
            String lnkey = null;
            String ctrySubCode = null, ctryCode = null;
            if (frmfieldViews != null && frmfieldViews.size() > 0) {
                FormFieldView locationView = FormUtil.findFormFieldViewById(frmfieldViews,
                        ExpenseFormFieldViewListener.LOCATION_FIELD_ID);
                if (locationView != null && locationView instanceof SearchListFormFieldView) {
                    lnkey = ((SearchListFormFieldView) locationView).getLiKey();
                }

                FormFieldView ctryView = FormUtil.findFormFieldViewById(frmfieldViews,
                        ExpenseFormFieldViewListener.COUNTRY_FIELD_ID);
                if (ctryView != null && ctryView instanceof SearchListFormFieldView) {
                    ctryCode = ((SearchListFormFieldView) ctryView).getLiKey();
                }

                FormFieldView ctrySubView = FormUtil.findFormFieldViewById(frmfieldViews,
                        ExpenseFormFieldViewListener.COUNTRY_SUBCODE_FIELD_ID);
                if (ctrySubView != null && ctrySubView instanceof SearchListFormFieldView) {
                    ctrySubCode = ((SearchListFormFieldView) ctrySubView).getLiKey();
                }
            }
            String date = getExpRepEntDet().transactionDate;
            if ((expKey != null && expKey.length() > 0)
                    && (date != null && date.length() > 0)
                    && ((lnkey != null && lnkey.length() > 0) || (ctryCode != null && ctryCode.length() > 0) || (ctrySubCode != null && ctrySubCode
                            .length() > 0))) {
                ConcurService concurService = getConcurService();
                registerTaxFormReceiver();
                getTaxFormRequest = concurService
                        .getTaxForm(expKey, date, lnkey, expRepEntryKey, ctryCode, ctrySubCode);
                if (getTaxFormRequest == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".callGetTaxForm: unable to create 'GetTaxFormRequest' request!");
                    unregisterTaxFormReceiver();
                } else {
                    showTaxFormDialog(Const.DIALOG_EXPENSE_TAX_FORM_PROGRESS);
                    taxFormReceiver.setServiceRequest(getTaxFormRequest);
                }
            }
        }
    }

    @Override
    protected void onHandleSuccessTaxForm(GetTaxFormReply taxForm) {
        List<TaxForm> taxForms = taxForm.listOfTaxForm;
        if (taxForms != null && taxForms.size() > 0) {
            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.entry_tax_field_list);
            if (viewGroup != null) {
                viewGroup.removeAllViews();
                viewGroup.invalidate();
                getExpRepEntDet().setTaxForm(taxForms);

                // Grab a reference to any previously built tax form field
                // views.
                List<FormFieldView> srcTaxFrmFlds = null;
                if (frmFldViewListener != null && frmFldViewListener.getTaxFormFieldViews() != null
                        && frmFldViewListener.getTaxFormFieldViews().size() > 0) {
                    srcTaxFrmFlds = frmFldViewListener.getTaxFormFieldViews();
                }

                populateTaxFormFields();

                // Grab a reference to any newly built tax form fields.
                List<FormFieldView> dstTaxFrmFlds = null;
                if (frmFldViewListener != null && frmFldViewListener.getTaxFormFieldViews() != null
                        && frmFldViewListener.getTaxFormFieldViews().size() > 0) {
                    dstTaxFrmFlds = frmFldViewListener.getTaxFormFieldViews();
                }

                // Transfer any edited values from 'srcTaxFrmFlds' to
                // 'dstTaxFrmFlds' where they match on
                // field id and field type.
                if (srcTaxFrmFlds != null && srcTaxFrmFlds.size() > 0 && dstTaxFrmFlds != null
                        && dstTaxFrmFlds.size() > 0) {
                    transferEditedValues(srcTaxFrmFlds, dstTaxFrmFlds);
                }

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onHandleSuccessTaxForm: can not find view group");
            }
        } else {
            // MOB-19100 Hide the Tax Form
            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.entry_tax_field_list);
            if (viewGroup != null) {
                viewGroup.removeAllViews();
                viewGroup.invalidate();
                hideView(R.id.entry_tax_field_list);
            }

            if (frmFldViewListener != null) {
                frmFldViewListener.setTaxFormFieldViews(null);
            }
        }
    }

    @Override
    protected void onHandleSuccessConditionalFieldActions(List<ConditionalFieldAction> actions) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onHandleSuccessConditionalFieldActions: receiving dynamicField Actions!");

        List<ExpenseReportFormField> expRepFrmFields = expRepEntDet.getFormFields();

        if (expRepFrmFields == null || expRepFrmFields.isEmpty() || actions.isEmpty()) {
            return;
        }

        // Find which fields needs to be updated.
        Hashtable<String, ConditionalFieldAction> changedFields = new Hashtable<String, ConditionalFieldAction>();
        for (ConditionalFieldAction action : actions) {
            changedFields.put(action.getFormField(), action);
        }

        for (ExpenseReportFormField expRepFrmFld : expRepFrmFields) {
            if (changedFields.keySet().contains(expRepFrmFld.getFormFieldKey())) {
                ConditionalFieldAction action = changedFields.get(expRepFrmFld.getFormFieldKey());
                if (action.getVisibility() == ConditionalFieldAction.AccessVisibility.HIDE) {
                    expRepFrmFld.setControlType(ControlType.HIDDEN);
                    FormUtil.hideFieldById(this, frmFldViewListener, expRepFrmFld.getId());
                } else {
                    expRepFrmFld.setAccessType(action.getAccessType());
                    expRepFrmFld.setControlType(expRepFrmFld.getOriginalCtrlType());
                    FormUtil.showFieldById(this, frmFldViewListener, expRepFrmFld.getId());
                }
            }
        }

    }
}
