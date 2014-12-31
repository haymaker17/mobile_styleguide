package com.concur.mobile.core.expense.report.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.ControlType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.DataType;
import com.concur.mobile.core.expense.report.service.ItemizeHotelRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.DatePickerFormFieldView;
import com.concur.mobile.core.view.ExpenseTypeFormFieldView;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldViewListener;
import com.concur.mobile.platform.util.Parse;

public class ExpenseHotelWizard extends AbstractExpenseActivity {

    private static final String CLS_TAG = ExpenseHotelWizard.class.getSimpleName();

    private static final String ITEMIZE_HOTEL_WIZARD_RECEIVER_KEY = "itemize.hotel.wizard.receiver";

    protected ExpenseReportEntryDetail expRepEntDet;

    public static String[] HARD_STOP_FIELD_IDS = { "checkIn", "checkOut", "numOfNights", "roomRate" };

    // Contains a reference to an outstanding request to itemize a hotel.
    protected ItemizeHotelRequest itemizeHotelRequest;

    // Contains a reference to the receiver for handling an itemize hotel
    // response.
    protected ItemizeHotelReceiver itemizeHotelReceiver;

    // Contains the filter for registering the above receiver.
    protected IntentFilter itemizeHotelFilter;

    @Override
    protected String[] getHardStopFieldIds() {
        return HARD_STOP_FIELD_IDS;
    }

    @Override
    protected FormFieldViewListener createFormFieldViewListener() {
        return new HotelWizardFormFieldListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_save, menu);

        if (!((reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE && isReportEditable() && isSaveReportEnabled()) || (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW))) {
            menu.removeItem(R.id.menuSave);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuSave) {
            save();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void buildView() {

        // Create our itemize action filter
        itemizeHotelFilter = new IntentFilter(Const.ACTION_EXPENSE_HOTEL_ITEMIZED);

        // Instruct the window manager to only show the soft keyboard when the
        // end-user clicks on it.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Set the content view.
        setContentView(R.layout.expense_hotel_wizard);

        // Configure the screen header.
        configureScreenHeader(expRep);

        // Configure the screen footer.
        configureScreenFooter();

        Intent intent = getIntent();
        String expRepEntryKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
        ExpenseReportEntry expRepEnt = expRepCache.getReportEntry(expRep, expRepEntryKey);

        if (expRepEnt != null) {
            if (expRepEnt instanceof ExpenseReportEntryDetail) {

                expRepEntDet = (ExpenseReportEntryDetail) expRepEnt;

                // Finally have what we need. Set them on the listener before
                // building things.
                frmFldViewListener.setExpenseReport(expRep);
                frmFldViewListener.setExpenseReportEntry(expRepEntDet);

                // Set the expense entry info.
                populateExpenseEntryTitleHeader();

                // Build the wizard form
                populateHotelWizardForm();

                // Restore any values from an orientation change.
                restoreFormFieldState();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: entry is not detail!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: entry is null!");
        }
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

    protected void populateHotelWizardForm() {

        ExpenseReportFormField field;

        // Build up our nights form fields
        ArrayList<ExpenseReportFormField> nightFields = new ArrayList<ExpenseReportFormField>();

        field = new ExpenseReportFormField("checkIn", getText(R.string.hotel_wizard_checkin).toString(), null,
                AccessType.RW, ControlType.DATE_EDIT, DataType.TIMESTAMP, true);
        nightFields.add(field);

        field = new ExpenseReportFormField("checkOut", getText(R.string.hotel_wizard_checkout).toString(),
                FormatUtil.XML_DF.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime()), AccessType.RW,
                ControlType.DATE_EDIT, DataType.TIMESTAMP, true);
        nightFields.add(field);

        field = new ExpenseReportFormField("numOfNights", getText(R.string.hotel_wizard_nights).toString(), null,
                AccessType.RW, ControlType.EDIT, DataType.INTEGER, true);
        nightFields.add(field);

        // Generate the nights view
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.hotel_wizard_night_details);
        List<FormFieldView> nightFFViews = populateViewWithFormFields(viewGroup, nightFields, null);

        // Build up our room charge form fields
        ArrayList<ExpenseReportFormField> roomChargeFields = new ArrayList<ExpenseReportFormField>();

        field = new ExpenseReportFormField("roomRate", getText(R.string.hotel_wizard_room_rate).toString(), null,
                AccessType.RW, ControlType.EDIT, DataType.MONEY, true);
        roomChargeFields.add(field);

        field = new ExpenseReportFormField("roomTax", getText(R.string.hotel_wizard_room_tax).toString(), null,
                AccessType.RW, ControlType.EDIT, DataType.MONEY, false);
        roomChargeFields.add(field);

        // These fields are hidden until the MWS supports them.
        // StringBuilder sb = new
        // StringBuilder(getText(R.string.hotel_wizard_other_tax)).append(" 1");
        // field = new ExpenseReportFormField("otherTax1", sb.toString(), null,
        // AccessType.RW, ControlType.EDIT,
        // DataType.MONEY, false);
        // roomChargeFields.add(field);
        //
        // sb = new
        // StringBuilder(getText(R.string.hotel_wizard_other_tax)).append(" 2");
        // field = new ExpenseReportFormField("otherTax2", sb.toString(), null,
        // AccessType.RW, ControlType.EDIT,
        // DataType.MONEY, false);
        // roomChargeFields.add(field);

        field = new ExpenseReportFormField("combine", getText(R.string.hotel_wizard_combine).toString(), null,
                AccessType.RW, ControlType.CHECKBOX, DataType.BOOLEAN, false);
        roomChargeFields.add(field);

        // Generate the rooms view
        viewGroup = (ViewGroup) findViewById(R.id.hotel_wizard_room_charges);
        List<FormFieldView> roomFFViews = populateViewWithFormFields(viewGroup, roomChargeFields, null);

        // Build up our other charge form fields
        ArrayList<ExpenseReportFormField> otherChargeFields = new ArrayList<ExpenseReportFormField>();

        field = new ExpenseReportFormField("otherChargeType1", getText(R.string.expense_type).toString(), null,
                AccessType.RW, ControlType.EDIT, DataType.EXPENSE_TYPE, false);
        otherChargeFields.add(field);

        field = new ExpenseReportFormField("otherChargeAmount1", getText(R.string.expense_amount).toString(), null,
                AccessType.RW, ControlType.EDIT, DataType.MONEY, false);
        otherChargeFields.add(field);

        field = new ExpenseReportFormField("otherChargeType2", getText(R.string.expense_type).toString(), null,
                AccessType.RW, ControlType.EDIT, DataType.EXPENSE_TYPE, false);
        otherChargeFields.add(field);

        field = new ExpenseReportFormField("otherChargeAmount2", getText(R.string.expense_amount).toString(), null,
                AccessType.RW, ControlType.EDIT, DataType.MONEY, false);
        otherChargeFields.add(field);

        // Generate the other charges view
        viewGroup = (ViewGroup) findViewById(R.id.hotel_wizard_other_charges);
        List<FormFieldView> otherFFViews = populateViewWithFormFields(viewGroup, otherChargeFields, null);

        // Go get the expense type FFVs and set them to be simple selectors
        int size = otherFFViews.size();
        for (int i = 0; i < size; i++) {
            FormFieldView ffv = otherFFViews.get(i);
            if (ffv instanceof ExpenseTypeFormFieldView) {
                ((ExpenseTypeFormFieldView) ffv).setSimpleSelector();
            }
        }

        if (frmFldViewListener != null) {
            ArrayList<FormFieldView> allFormFieldViews = new ArrayList<FormFieldView>();
            allFormFieldViews.addAll(nightFFViews);
            allFormFieldViews.addAll(roomFFViews);
            allFormFieldViews.addAll(otherFFViews);
            frmFldViewListener.setFormFieldViews(allFormFieldViews);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: frmFldViewListener is null!");
        }

    }

    protected void registerItemizeHotelReceiver() {
        itemizeHotelReceiver = new ItemizeHotelReceiver(this);
        getApplicationContext().registerReceiver(itemizeHotelReceiver, itemizeHotelFilter);
    }

    protected void unregisterItemizeHotelReceiver() {
        getApplicationContext().unregisterReceiver(itemizeHotelReceiver);
        itemizeHotelReceiver = null;
    }

    protected boolean isConditionalFieldMissing(FormFieldView primaryField, FormFieldView conditionalField) {

        boolean isMissing = false;

        // If the primary field has a value
        if (primaryField.getCurrentValue() != null) {
            // Then we need a value in the conditional field as well
            if (!conditionalField.hasValue()) {
                isMissing = true;
            }
        }

        return isMissing;
    }

    @Override
    protected List<FormFieldView> checkForInvalidValues() {
        List<FormFieldView> invalid = super.checkForInvalidValues();

        // Make sure nights aren't over 90
        FormFieldView nightsFFV = frmFldViewListener.findFormFieldViewById("numOfNights");
        Integer nights = Parse.safeParseInteger(nightsFFV.getCurrentValue());
        if (nights != null && nights > 90) {
            if (invalid == null) {
                invalid = new ArrayList<FormFieldView>();
            }
            invalid.add(nightsFFV);
        }

        return invalid;
    }

    @Override
    protected List<FormFieldView> checkForMissingValues() {

        List<FormFieldView> missing = super.checkForMissingValues();

        // We need to check fields that are conditionally required.
        FormFieldView ffv = frmFldViewListener.findFormFieldViewById("otherChargeAmount1");
        if (isConditionalFieldMissing(frmFldViewListener.findFormFieldViewById("otherChargeType1"), ffv)) {
            if (missing == null) {
                missing = new ArrayList<FormFieldView>();
            }
            missing.add(ffv);
        }

        ffv = frmFldViewListener.findFormFieldViewById("otherChargeAmount2");
        if (isConditionalFieldMissing(frmFldViewListener.findFormFieldViewById("otherChargeType2"), ffv)) {
            if (missing == null) {
                missing = new ArrayList<FormFieldView>();
            }
            missing.add(ffv);
        }

        return missing;
    }

    @Override
    protected List<FormFieldView> checkForHardStopMissingFieldValues(List<FormFieldView> frmFldViews) {
        List<FormFieldView> missing = super.checkForHardStopMissingFieldValues(frmFldViews);

        // We need to check fields that are conditionally required.
        FormFieldView ffv = frmFldViewListener.findFormFieldViewById("otherChargeAmount1");
        if (isConditionalFieldMissing(frmFldViewListener.findFormFieldViewById("otherChargeType1"), ffv)) {
            if (missing == null) {
                missing = new ArrayList<FormFieldView>();
            }
            missing.add(ffv);
        }

        ffv = frmFldViewListener.findFormFieldViewById("otherChargeAmount2");
        if (isConditionalFieldMissing(frmFldViewListener.findFormFieldViewById("otherChargeType2"), ffv)) {
            if (missing == null) {
                missing = new ArrayList<FormFieldView>();
            }
            missing.add(ffv);
        }

        return missing;
    }

    @Override
    protected void sendSaveRequest() {
        Locale currentLocale = ConcurCore.getContext().getResources().getConfiguration().locale;

        ConcurCore ConcurCore = (ConcurCore) getApplication();
        ConcurService concurService = ConcurCore.getService();

        registerItemizeHotelReceiver();

        FormFieldView ffv;

        ffv = frmFldViewListener.findFormFieldViewById("checkIn");
        String checkIn = ffv.getCurrentValue();

        ffv = frmFldViewListener.findFormFieldViewById("checkOut");
        String checkOut = ffv.getCurrentValue();

        ffv = frmFldViewListener.findFormFieldViewById("numOfNights");
        String nights = ffv.getCurrentValue();

        // MOB-13256
        // Convert raw string values from form fields to appropriate data types
        // to prevent invalid format types
        // being added as xml elements.
        ffv = frmFldViewListener.findFormFieldViewById("roomRate");
        String rawRoomRate = ffv.getCurrentValue();
        Double roomRate = (rawRoomRate == null ? null : FormatUtil.parseAmount(rawRoomRate, currentLocale));

        ffv = frmFldViewListener.findFormFieldViewById("roomTax");
        String rawRoomTax = ffv.getCurrentValue();
        Double roomTax = (rawRoomTax == null ? null : FormatUtil.parseAmount(rawRoomTax, currentLocale));

        // ffv = frmFldViewListener.findFormFieldViewById("otherTax1");
        // String otherTax1 = ffv.getCurrentValue();
        Double otherTax1 = null;
        //
        // ffv = frmFldViewListener.findFormFieldViewById("otherTax2");
        // String otherTax2 = ffv.getCurrentValue();
        Double otherTax2 = null;

        ffv = frmFldViewListener.findFormFieldViewById("combine");
        boolean combine = ("Y".equals(ffv.getCurrentValue()) ? true : false);

        ffv = frmFldViewListener.findFormFieldViewById("otherChargeType1");
        String additionalExpKey1 = ffv.getCurrentValue();

        ffv = frmFldViewListener.findFormFieldViewById("otherChargeAmount1");
        String rawAdditionalAmount1 = ffv.getCurrentValue();
        Double additionalAmount1 = (rawAdditionalAmount1 == null ? null : FormatUtil.parseAmount(rawAdditionalAmount1,
                currentLocale));

        ffv = frmFldViewListener.findFormFieldViewById("otherChargeType2");
        String additionalExpKey2 = ffv.getCurrentValue();

        ffv = frmFldViewListener.findFormFieldViewById("otherChargeAmount2");
        String rawAdditionalAmount2 = ffv.getCurrentValue();
        Double additionalAmount2 = (rawAdditionalAmount2 == null ? null : FormatUtil.parseAmount(rawAdditionalAmount2,
                currentLocale));

        itemizeHotelRequest = concurService.sendItemizeHotelRequest(expRepEntDet, combine, checkIn, checkOut, nights,
                roomRate, roomTax, otherTax1, otherTax2, additionalExpKey1, additionalAmount1, additionalExpKey2,
                additionalAmount2);

        if (itemizeHotelRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to create 'SaveReportEntry' request!");
            unregisterItemizeHotelReceiver();
        } else {
            // Set the request object on the receiver.
            itemizeHotelReceiver.setRequest(itemizeHotelRequest);
            // Show the itemizing dialog.
            showDialog(Const.DIALOG_EXPENSE_ITEMIZE_HOTEL);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onCreate(android .os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore any receivers.
        restoreReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save 'ItemizeHotelReceiver'.
        if (itemizeHotelReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            itemizeHotelReceiver.setActivity(null);
            // Add to the map.
            retainer.put(ITEMIZE_HOTEL_WIZARD_RECEIVER_KEY, itemizeHotelReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
        if (retainer.contains(ITEMIZE_HOTEL_WIZARD_RECEIVER_KEY)) {
            itemizeHotelReceiver = (ItemizeHotelReceiver) retainer.get(ITEMIZE_HOTEL_WIZARD_RECEIVER_KEY);
            if (itemizeHotelReceiver != null) {
                // Set the activity on the receiver.
                itemizeHotelReceiver.setActivity(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".restoreReceivers: retainer contains null reference for itemize hotel receiver!");
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case Const.DIALOG_EXPENSE_ITEMIZE_HOTEL: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_itemize_hotel));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (itemizeHotelRequest != null) {
                        // Cancel the request.
                        itemizeHotelRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: itemizeHotelRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_ITEMIZE_HOTEL_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.general_error);
            dlgBldr.setMessage(R.string.dlg_itemize_hotel_failed);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    @Override
    protected IntentFilter getBroadcastReceiverIntentFilter() {
        return null;
    }

    @Override
    protected int getHeaderNavBarTitleResourceId() {
        return R.string.hotel_wizard_title;
    }

    @Override
    protected boolean isDetailReportRequired() {
        return true;
    }

    @Override
    protected boolean shouldReceiveDataEvents() {
        return false;
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

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of a hotel itemization action.
     */
    static class ItemizeHotelReceiver extends BroadcastReceiver {

        private final String CLS_TAG = ExpenseHotelWizard.CLS_TAG + "." + ItemizeHotelReceiver.class.getSimpleName();

        // A reference to the activity.
        private ExpenseHotelWizard activity;

        // A reference to the request.
        private ItemizeHotelRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        ItemizeHotelReceiver(ExpenseHotelWizard activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseHotelWizard activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.itemizeHotelRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        void setRequest(ItemizeHotelRequest request) {
            this.request = request;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                // Unregister the receiver.
                activity.unregisterItemizeHotelReceiver();

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
                                        activity.dismissDialog(Const.DIALOG_EXPENSE_ITEMIZE_HOTEL);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }

                                    // Set the result for the calling activity.
                                    // Intent i = new Intent();
                                    // i.putExtra(Const.EXPENSE_REPORT_KEY,
                                    // activity.getIntent().getStringExtra(
                                    // Const.EXPENSE_REPORT_KEY));
                                    // i.putExtra(Const.EXPENSE_REPORT_SOURCE_KEY,
                                    // activity.getIntent().getIntExtra(
                                    // Const.EXPENSE_REPORT_SOURCE_KEY,
                                    // Const.EXPENSE_REPORT_SOURCE_ACTIVE));
                                    // activity.setResult(Activity.RESULT_OK,
                                    // i);

                                    // Set the flag to refresh the active report
                                    // list.
                                    IExpenseReportCache expRepCache = ((ConcurCore) activity.getApplication())
                                            .getExpenseActiveCache();
                                    expRepCache.setShouldFetchReportList();

                                    // Flurry Notification
                                    EventTracker.INSTANCE.track(Flurry.CATEGORY_REPORT_ENTRY,
                                            Flurry.EVENT_NAME_ITEMIZE_HOTEL_ENTRY);

                                    // Move on to the itemization list
                                    Intent i = new Intent(activity, ExpenseEntryItemization.class);
                                    i.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, activity.expRep.reportKey);
                                    i.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY,
                                            activity.expRepEntDet.reportEntryKey);
                                    i.putExtra(Const.EXTRA_EXPENSE_PARENT_REPORT_ENTRY_KEY,
                                            activity.expRepEntDet.reportEntryKey);
                                    i.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, Const.EXPENSE_REPORT_SOURCE_ACTIVE);

                                    activity.startActivity(i);

                                    // Finish this activity off so we don't come
                                    // back.
                                    activity.setResult(RESULT_OK);
                                    activity.finish();
                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);
                                    // Display an error dialog.
                                    activity.showDialog(Const.DIALOG_EXPENSE_ITEMIZE_HOTEL_FAILED);

                                    try {
                                        // Dismiss the dialog.
                                        activity.dismissDialog(Const.DIALOG_EXPENSE_ITEMIZE_HOTEL);
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
                                    activity.dismissDialog(Const.DIALOG_EXPENSE_ITEMIZE_HOTEL);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            try {
                                // Dismiss the dialog.
                                activity.dismissDialog(Const.DIALOG_EXPENSE_ITEMIZE_HOTEL);
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
                                activity.dismissDialog(Const.DIALOG_EXPENSE_ITEMIZE_HOTEL);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    }
                } else {
                    try {
                        // Dismiss the dialog.
                        activity.dismissDialog(Const.DIALOG_EXPENSE_ITEMIZE_HOTEL);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }

                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear the request reference.
                activity.itemizeHotelRequest = null;

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }

    }

    private final static long MS_IN_DAY = 24 * 60 * 60 * 1000;

    protected Calendar clearTime(Calendar cal) {
        if (cal != null) {
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR, 0);
        }
        return cal;
    }

    /**
     * Keep the values for dates and nights in sync.
     * 
     * @param id
     *            the field that changed
     */
    protected void handleDateOrNightChange(String id) {

        FormFieldView nightsFFV = frmFldViewListener.findFormFieldViewById("numOfNights");
        DatePickerFormFieldView checkInFFV = (DatePickerFormFieldView) frmFldViewListener
                .findFormFieldViewById("checkIn");
        DatePickerFormFieldView checkOutFFV = (DatePickerFormFieldView) frmFldViewListener
                .findFormFieldViewById("checkOut");

        if (id.equals("numOfNights")) {
            // Update checkIn to be numOfNights before checkOut
            Integer nights = Parse.safeParseInteger(nightsFFV.getCurrentValue());
            if (nights != null && nights > 0) {
                Calendar newCheckIn = checkOutFFV.getCalendar();
                newCheckIn.add(Calendar.DAY_OF_YEAR, -nights);
                checkInFFV.setCurrentValue(newCheckIn, false);
            }
        } else if (id.equals("checkOut")) {
            Calendar checkOut = clearTime(checkOutFFV.getCalendar());
            Calendar checkIn = clearTime(checkInFFV.getCalendar());
            Integer nights = Parse.safeParseInteger(nightsFFV.getCurrentValue());

            // If before checkIn, move checkIn to be numOfNights (or 1) before
            // checkOut
            if (checkIn != null && checkOut.before(checkIn)) {
                if (nights == null || nights <= 0) {
                    nights = 1;
                }
                checkOut.add(Calendar.DAY_OF_YEAR, -nights);
                checkInFFV.setCurrentValue(checkOut, false);
            }

            // If after checkIn, update numOfNights to be correct
            if (checkIn != null && checkOut.after(checkIn)) {
                long days = ((checkOut.getTimeInMillis() - checkIn.getTimeInMillis()) / MS_IN_DAY);
                nightsFFV.setCurrentValue(Long.toString(days), false);
            }
        } else if (id.equals("checkIn")) {
            Calendar checkOut = clearTime(checkOutFFV.getCalendar());
            Calendar checkIn = clearTime(checkInFFV.getCalendar());
            Integer nights = Parse.safeParseInteger(nightsFFV.getCurrentValue());

            // If after checkOut, move checkOut to be numOfNights (or 1) after
            // checkIn
            if (checkOut != null && checkIn.after(checkOut)) {
                if (nights == null || nights <= 0) {
                    nights = 1;
                }

                checkIn.add(Calendar.DAY_OF_YEAR, nights);
                checkOutFFV.setCurrentValue(checkIn, false);
            }

            // If before checkOut, update numOfNights to be correct
            if (checkOut != null && checkIn.before(checkOut)) {
                long days = ((checkOut.getTimeInMillis() - checkIn.getTimeInMillis()) / MS_IN_DAY);
                nightsFFV.setCurrentValue(Long.toString(days), false);
            }
        }
    }

    protected class HotelWizardFormFieldListener extends FormFieldViewListener {

        public HotelWizardFormFieldListener(BaseActivity activity) {
            super(activity);
        }

        public HotelWizardFormFieldListener(BaseActivity activity, ExpenseReport expenseReport,
                ExpenseReportEntry expenseReportEntry) {
            super(activity, expenseReport, expenseReportEntry);
        }

        public HotelWizardFormFieldListener(BaseActivity activity, ExpenseReport expenseReport) {
            super(activity, expenseReport);
        }

        @Override
        public void valueChanged(FormFieldView frmFldView) {
            String id = frmFldView.getFormField().getId();

            if ("checkIn".equals(id) || "checkOut".equals(id) || "numOfNights".equals(id)) {
                handleDateOrNightChange(id);
            } else {
                super.valueChanged(frmFldView);
            }
        }

    }
}
