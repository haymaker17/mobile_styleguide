/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.report.data.AttendeeType;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.ControlType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.DataType;
import com.concur.mobile.core.expense.report.service.AttendeeFormRequest;
import com.concur.mobile.core.expense.report.service.AttendeeSaveReply;
import com.concur.mobile.core.expense.report.service.AttendeeSaveRequest;
import com.concur.mobile.core.expense.service.SearchListRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldViewListener;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>AbstractExpenseActivity</code> for attendee editing.
 */
public class ExpenseAttendeeEdit extends AbstractExpenseActivity {

    private static final String COUNT_EDIT_FIELD_ID = "InstanceCount";

    private static final String AMOUNT_EDIT_FIELD_ID = "Amount";

    private static final String CLS_TAG = ExpenseAttendeeEdit.class.getSimpleName();

    private static final String ATTENDEE_TYPE_RECEIVER_KEY = "attendee.type.receiver";

    private static final String ATTENDEE_FORM_RECEIVER_KEY = "attendee.form.receiver";

    private static final String ATTENDEE_SAVE_RECEIVER_KEY = "attendee.save.receiver";

    private static final int REQUEST_DUPLICATE_ATTENDEE = 1;

    /**
     * Contains the attendee type information receiver.
     */
    private AttendeeTypeReceiver attendeeTypeReceiver;

    /**
     * Contains the filter used to register the attendee type receiver.
     */
    private IntentFilter attendeeTypeFilter;

    /**
     * Contains a reference to an outstanding request to retrieve attendee type information.
     */
    private SearchListRequest attendeeTypeRequest;

    /**
     * Contains the attendee form receiver.
     */
    private AttendeeFormReceiver attendeeFormReceiver;

    /**
     * Contains the filter used to register the attendee form receiver.
     */
    private IntentFilter attendeeFormFilter;

    /**
     * Contains an outstanding attendee form request.
     */
    private AttendeeFormRequest attendeeFormRequest;

    /**
     * Contains the attendee save receiver.
     */
    private AttendeeSaveReceiver attendeeSaveReceiver;

    /**
     * Contains the filter used to register the attendee save receiver.
     */
    private IntentFilter attendeeSaveFilter;

    /**
     * Contains an outstanding attendee save request.
     */
    private AttendeeSaveRequest attendeeSaveRequest;

    /**
     * Contains a map from an attendee type key to the the attendee editing form.
     */
    private final HashMap<String, ExpenseReportAttendee> typeAttendeeMap = new HashMap<String, ExpenseReportAttendee>();

    /**
     * Contains the attendee type key currently being used to provide a form for the attendee being edited. Note: This value may
     * initially be <code>null</code> when an attendee that hasn't been added to the system is being edited.
     */
    private String atnTypeKey;

    /**
     * Contains the attendee key for the attendee being edited.
     * 
     * Note: This value may initially be <code>null</code> when an attendee that hasn't been added to the system is being edited.
     */
    private String atnKey;

    private double amount;
    private String crnCode;

    private int instanceCount;

    /**
     * Contains the passed in first name.
     */
    private String firstName;

    /**
     * Contains the passed in last name.
     */
    private String lastName;

    /**
     * Contains the passed in company name.
     */
    private String company;

    /**
     * Contains the passed in title.
     */
    private String title;

    /**
     * Contains the current attendee editing form.
     */
    private ExpenseReportAttendee atnForm;

    /**
     * Contains a reference to the expense report entry detail.
     */
    private ExpenseReportEntryDetail expRepEntDet;

    /**
     * Result data to go back if amount or count has changed
     */
    private Intent resultData;

    boolean dataFieldsChanged;

    /**
     * Contains whether or not the attendee being edited is an external attendee.
     */
    boolean isExternalAttendee;
    private boolean isCancelled = false;
    private static final String EXTRA_IS_CANCELLED = "extras_is_cancelled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Restore any receivers.
        restoreReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            // Save 'AttendeeTypeReceiver'.
            if (attendeeTypeReceiver != null) {
                // Clear the activity reference, it will be set in the
                // 'onCreate' method.
                attendeeTypeReceiver.setActivity(null);
                // Add to the retainer
                retainer.put(ATTENDEE_TYPE_RECEIVER_KEY, attendeeTypeReceiver);
            }
            // Save 'AttendeeFormReceiver'.
            if (attendeeFormReceiver != null) {
                // Clear the activity reference, it will be set in the
                // 'onCreate' method.
                attendeeFormReceiver.setActivity(null);
                // Add to the retainer
                retainer.put(ATTENDEE_FORM_RECEIVER_KEY, attendeeFormReceiver);
            }
            // Save 'AttendeeSaveReceiver'.
            if (attendeeSaveReceiver != null) {
                // Clear the activity reference, it will be set in the
                // 'onCreate' method.
                attendeeSaveReceiver.setActivity(null);
                // Add to the retainer
                retainer.put(ATTENDEE_SAVE_RECEIVER_KEY, attendeeSaveReceiver);
            }
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
        if (retainer != null) {
            // Restore an attendee type receiver.
            if (retainer.contains(ATTENDEE_TYPE_RECEIVER_KEY)) {
                attendeeTypeReceiver = (AttendeeTypeReceiver) retainer.get(ATTENDEE_TYPE_RECEIVER_KEY);
                // Reset the activity reference.
                attendeeTypeReceiver.setActivity(this);
            }
            // Restore an attendee form receiver.
            if (retainer.contains(ATTENDEE_FORM_RECEIVER_KEY)) {
                attendeeFormReceiver = (AttendeeFormReceiver) retainer.get(ATTENDEE_FORM_RECEIVER_KEY);
                // Reset the activity reference.
                attendeeFormReceiver.setActivity(this);
            }
            // Restore an attendee save receiver.
            if (retainer.contains(ATTENDEE_SAVE_RECEIVER_KEY)) {
                attendeeSaveReceiver = (AttendeeSaveReceiver) retainer.get(ATTENDEE_SAVE_RECEIVER_KEY);
                // Reset the activity reference.
                attendeeSaveReceiver.setActivity(this);
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
        case Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(this.getText(R.string.dlg_expense_attendee_retrieve_form_progress_message));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    // NOTE: The same dialog is displayed during the process of
                    // retrieving a set of attendee types
                    // then retrieving an attendee form. Alternatively, once the
                    // attendee types are retrieved, subsequent
                    // calls to retrieve different forms based on attendee type
                    // key utilize the same dialog.
                    // Cancel the request.
                    if (attendeeTypeRequest != null) {
                        attendeeTypeRequest.cancel();
                    } else if (attendeeFormRequest != null) {
                        attendeeFormRequest.cancel();
                    }
                    // If the initial form has not loaded and the end-user
                    // chooses to cancel, then finish the
                    // activity with a cancel result.
                    if (atnForm == null) {
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_ATTENDEE_SAVE_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_expense_attendee_save_progress_message));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            // Register a cancel listener to cancel the request.
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (attendeeSaveRequest != null) {
                        // Cancel the request, this will result in the
                        // 'AttendeeSaveReceiver.onReceiver' being
                        // invoked and handled as a cancellation.
                    	isCancelled=true;
                        attendeeSaveRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: reportApproveRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_ATTENDEE_SAVE_FAILED: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dlg_expense_attendee_save_failed_title);
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
        case Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_FAILED: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dlg_expense_attendee_retrieve_form_failed_title);
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
        case Const.DIALOG_EXPENSE_ATTENDEE_TYPE_NO_EDIT: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warning);
            builder.setCancelable(true);
            builder.setMessage(getText(R.string.dlg_expense_attendee_type_no_edit_message));
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
        case Const.DIALOG_EXPENSE_NO_ATTENDEE_TYPES: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warning);
            builder.setCancelable(false);
            builder.setMessage(getText(R.string.dlg_expense_no_attendee_types_message));
            builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Dismiss this dialog
                    dialog.dismiss();

                    // Exit the activity
                    finish();
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
        case Const.DIALOG_EXPENSE_ATTENDEE_SAVE_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_FAILED: {
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

    /**
     * Will send a request to retrieve attendee type information.
     */
    private void sendAttendeeTypeRequest() {
        ConcurService concurService = getConcurService();
        registerAttendeeTypeReceiver();
        attendeeTypeRequest = concurService.sendAttendeeTypeSearchRequest(getUserId(), null);
        if (attendeeTypeRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".onReceive: unable to create request to retrieve attendee type information!");
            unregisterAttendeeTypeReceiver();
        } else {
            // Set the request object on the receiver.
            attendeeTypeReceiver.setServiceRequest(attendeeTypeRequest);
            // Show the attendee form progress dialog.
            showDialog(Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_PROGRESS);
        }
    }

    /**
     * Will register an instance of <code>AttendeeTypeReceiver</code> with the application context and set the
     * <code>attendeeTypeReceiver</code> attribute.
     */
    protected void registerAttendeeTypeReceiver() {
        if (attendeeTypeReceiver == null) {
            attendeeTypeReceiver = new AttendeeTypeReceiver(this);
            if (attendeeTypeFilter == null) {
                attendeeTypeFilter = new IntentFilter(Const.ACTION_EXPENSE_ATTENDEE_TYPES_DOWNLOADED);
            }
            getApplicationContext().registerReceiver(attendeeTypeReceiver, attendeeTypeFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAttendeeTypeReceiver: attendeeTypeReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>AttendeeTypeReceiver</code> with the application context and set the
     * <code>attendeeTypeReceiver</code> to <code>null</code>.
     */
    protected void unregisterAttendeeTypeReceiver() {
        if (attendeeTypeReceiver != null) {
            getApplicationContext().unregisterReceiver(attendeeTypeReceiver);
            attendeeTypeReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAttendeeTypeReceiver: attendeeTypeReceiver is null!");
        }
    }

    /**
     * Will send a request to retrieve attendee type information.
     */
    private void sendAttendeeFormRequest(String atnTypeKey, String atnKey) {
        ConcurService concurService = getConcurService();
        registerAttendeeFormReceiver();
        attendeeFormRequest = concurService.sendAttendeeFormRequest(getUserId(), atnTypeKey, atnKey);
        if (attendeeFormRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".onReceive: unable to create request to retrieve attendee form information!");
            unregisterAttendeeFormReceiver();
        } else {
            // Set the request object on the receiver.
            attendeeFormReceiver.setServiceRequest(attendeeFormRequest);
            // Show the attendee form progress dialog.
            showDialog(Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_PROGRESS);
        }
    }

    @Override
    protected void commitEditedValues() {

        dataFieldsChanged = false;
        resultData = null;

        // Check to see if amount/count have changed or data fields or both
        // This has to be done now because once committed we can no longer
        // detect changed fields.
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    // Only look at views that actually made it to the screen
                    if (frmFldView.view != null && frmFldView.view.isShown()) {
                        if (frmFldView.hasValueChanged()) {
                            ExpenseReportFormField erff = frmFldView.getFormField();
                            if (AMOUNT_EDIT_FIELD_ID.equals(erff.getId())) {
                                if (resultData == null) {
                                    resultData = new Intent();
                                }
                                resultData.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_AMOUNT,
                                        Parse.safeParseDouble(frmFldView.getCurrentValue()));
                            } else if (COUNT_EDIT_FIELD_ID.equals(erff.getId())) {
                                if (resultData == null) {
                                    resultData = new Intent();
                                }
                                resultData.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_COUNT,
                                        Parse.safeParseInteger(frmFldView.getCurrentValue()));
                            } else {
                                dataFieldsChanged = true;
                            }
                        }
                    }
                }
            }
        }

        super.commitEditedValues();
    }

    /**
     * Will send a request to save attendee information.
     */
    private void sendAttendeeSaveRequest() {
        // Ensure the last saved attendee response is cleared.
        ConcurCore concurCore = getConcurCore();
        concurCore.setAttendeeSaveResults(null);
        if (!dataFieldsChanged && !isCancelled) {
            // No attendee data changed so get out now
            if (resultData != null) {
                // Add the atnKey to these results to find the attendee in the
                // list activity
                resultData.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_KEY, atnKey);
                setResult(Activity.RESULT_OK, resultData);
            } else {
                // Nothing has been changed, so just send back an okay.
                setResult(Activity.RESULT_OK);
            }
            finish();
        } else {
            // Do the full attendee save
            ConcurService concurService = getConcurService();
            // Ensure that any base attributes on 'atnForm' are set from their
            // field values.
            // ((AttendeeFormFieldViewListener)
            // frmFldViewListener).setBaseAttendeeValuesFromFields();
            registerAttendeeSaveReceiver();
            // If editing an external attendee, ensure we copy values from
            // 'atnFormFields' to 'atnForm' prior
            // to the save request.
            if (atnKey != null) {
                atnForm.atnKey = atnKey;
            }
            attendeeSaveRequest = concurService.sendAttendeeSaveRequest(getUserId(), atnForm);
            if (attendeeSaveRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to create request to save attendee information!");
                unregisterAttendeeSaveReceiver();
            } else {
                // Set the request object on the receiver.
                attendeeSaveReceiver.setServiceRequest(attendeeSaveRequest);
                // Show the attendee form progress dialog.
                showDialog(Const.DIALOG_EXPENSE_ATTENDEE_SAVE_PROGRESS);
            }
        }
    }

    /**
     * Will register an instance of <code>AttendeeFormReceiver</code> with the application context and set the
     * <code>attendeeFormReceiver</code> attribute.
     */
    protected void registerAttendeeFormReceiver() {
        if (attendeeFormReceiver == null) {
            attendeeFormReceiver = new AttendeeFormReceiver(this);
            if (attendeeFormFilter == null) {
                attendeeFormFilter = new IntentFilter(Const.ACTION_EXPENSE_ATTENDEE_FORM_DOWNLOADED);
            }
            getApplicationContext().registerReceiver(attendeeFormReceiver, attendeeFormFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAttendeeFormReceiver: attendeeFormReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>AttendeeFormReceiver</code> with the application context and set the
     * <code>attendeeFormReceiver</code> to <code>null</code>.
     */
    protected void unregisterAttendeeFormReceiver() {
        if (attendeeFormReceiver != null) {
            getApplicationContext().unregisterReceiver(attendeeFormReceiver);
            attendeeFormReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAttendeeFormReceiver: attendeeFormReceiver is null!");
        }
    }

    /**
     * Will register an instance of <code>AttendeeSaveReceiver</code> with the application context and set the
     * <code>attendeeSaveReceiver</code> attribute.
     */
    protected void registerAttendeeSaveReceiver() {
        if (attendeeSaveReceiver == null) {
            attendeeSaveReceiver = new AttendeeSaveReceiver(this);
            if (attendeeSaveFilter == null) {
                attendeeSaveFilter = new IntentFilter(Const.ACTION_EXPENSE_ATTENDEE_SAVE);
            }
            getApplicationContext().registerReceiver(attendeeSaveReceiver, attendeeSaveFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAttendeeSaveReceiver: attendeeSaveReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>AttendeeSaveReceiver</code> with the application context and set the
     * <code>attendeeSaveReceiver</code> to <code>null</code>.
     */
    protected void unregisterAttendeeSaveReceiver() {
        if (attendeeSaveReceiver != null) {
            getApplicationContext().unregisterReceiver(attendeeSaveReceiver);
            attendeeSaveReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAttendeeSaveReceiver: attendeeSaveReceiver is null!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isSubmitReportEnabled()
     */
    @Override
    protected boolean isSubmitReportEnabled() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save attendee type key.
        if (atnTypeKey != null) {
            outState.putString(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY, atnTypeKey);
        }
        // Save attendee key.
        if (atnKey != null) {
            outState.putString(Const.EXTRA_EXPENSE_ATTENDEE_KEY, atnKey);
        }
        // Save the first name.
        if (firstName != null) {
            outState.putString(Const.EXTRA_EXPENSE_ATTENDEE_FIRST_NAME, firstName);
        }
        // Save the last name.
        if (lastName != null) {
            outState.putString(Const.EXTRA_EXPENSE_ATTENDEE_LAST_NAME, lastName);
        }
        // Save the company name.
        if (company != null) {
            outState.putString(Const.EXTRA_EXPENSE_ATTENDEE_COMPANY, company);
        }
        // Save the title.
        if (title != null) {
            outState.putString(Const.EXTRA_EXPENSE_ATTENDEE_TITLE, title);
        }

        outState.putDouble(Const.EXTRA_EXPENSE_ATTENDEE_AMOUNT, amount);
        outState.putString(Const.EXTRA_EXPENSE_TRANSACTION_CURRENCY, crnCode);
        outState.putInt(Const.EXTRA_EXPENSE_ATTENDEE_COUNT, instanceCount);
        outState.putBoolean(EXTRA_IS_CANCELLED, isCancelled);
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#buildView()
     */
    @Override
    protected void buildView() {

        // Instruct the window manager to only show the soft keyboard when the
        // end-user clicks on it.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Set the layout.
        setContentView(R.layout.expense_attendee_edit);

        // Configure the screen header.
        configureScreenHeader(expRep);

        // Configure the screen footer.
        configureScreenFooter();

        // Set the expense report entry detail object.
        Intent intent = getIntent();
        if (intent.hasExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY)) {
            String expRepEntryKey = getIntent().getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
            if (expRepEntryKey != null) {
                ExpenseReportEntry expRepEnt = expRepCache.getReportEntry(expRep, expRepEntryKey);
                if (isReportEditable()) {
                    expRepEntDet = getConcurCore().getCurrentEntryDetailForm();
                    if (expRepEntDet == null) {
                        expRepEntDet = (ExpenseReportEntryDetail) expRepEnt;
                        if (expRepEnt instanceof ExpenseReportEntryDetail) {
                            expRepEntDet = (ExpenseReportEntryDetail) expRepEnt;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: invoked with non-detailed report entry object!");
                        }
                    }
                } else {
                    if (expRepEnt instanceof ExpenseReportEntryDetail) {
                        expRepEntDet = (ExpenseReportEntryDetail) expRepEnt;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: invoked with non-detailed report entry object!");
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: report entry key is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: report entry key missing!");
        }

        // Update the listener to have access to the report and entry
        frmFldViewListener.setExpenseReport(expRep);
        frmFldViewListener.setExpenseReportEntry(expRepEntDet);

        // Set the attendee type and attendee keys.
        if (lastSavedInstanceState != null) {
            initState(lastSavedInstanceState);
        } else {
            initState(intent.getExtras());
        }

        // Does the client already have attendee types?
        ConcurCore ConcurCore = getConcurCore();
        IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
        ExpenseType expType = ExpenseType.findExpenseType(expRep.polKey, expRepEntDet.expKey);
        List<ListItem> attendeeTypes = expRepCache.getAttendeeTypes(expType, expRepCache.getAddAttendeeTypes());
        if (attendeeTypes != null) {
            if (atnTypeKey == null || atnTypeKey.length() == 0) {
                // Select a default attendee type with which to fetch a form.
                atnTypeKey = getDefaultAtnTypeKey(attendeeTypes);
            }
            if (atnTypeKey != null) {
                // Fetch the attendee form.
                ExpenseReportAttendee attendeeForm = ConcurCore.getAttendeeForm();
                // check atntype key
                // MOB-10251 : required atnTypeKey check.
                if (attendeeForm != null && atnTypeKey.equals(attendeeForm.atnTypeKey)) {
                    callRegenerateScreen(atnTypeKey, attendeeForm);
                } else {
                    sendAttendeeFormRequest(atnTypeKey, atnKey);
                }
            } else {
                // No attendee types available
                showDialog(Const.DIALOG_EXPENSE_NO_ATTENDEE_TYPES);
            }
        } else {
            // Fetch the set of attendee types.
            sendAttendeeTypeRequest();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# createFormFieldViewListener()
     */
    @Override
    protected FormFieldViewListener createFormFieldViewListener() {
        return new AttendeeFormFieldViewListener(this);
    }

    @Override
    protected List<FormFieldView> checkForInvalidValues() {
        List<FormFieldView> retVal = super.checkForInvalidValues();
        FormFieldView instanceCountFFV = frmFldViewListener.findFormFieldViewById(COUNT_EDIT_FIELD_ID);
        if (instanceCountFFV != null && (retVal == null || !retVal.contains(instanceCountFFV))) {
            // The field is there and not already failed a validation check
            Integer count = Parse.safeParseInteger(instanceCountFFV.getCurrentValue());
            if (count == null || count < 1) {
                if (retVal == null) {
                    retVal = new ArrayList<FormFieldView>();
                }
                retVal.add(instanceCountFFV);
            }
        }

        return retVal;
    }

    /**
     * Uses the currently loaded form and defines any required field as a hard-stop field.
     */
    @Override
    protected String[] getHardStopFieldIds() {
        String[] hardStopFieldIds = null;

        if (frmFldViewListener != null) {
            List<FormFieldView> frmFldViews = frmFldViewListener.getFormFieldViews();
            if (frmFldViews != null) {
                List<String> reqFldIds = new ArrayList<String>();
                for (FormFieldView frmFldView : frmFldViews) {
                    ExpenseReportFormField frmFld = frmFldView.getFormField();
                    if (frmFld != null) {
                        if (frmFld.isRequired()) {
                            reqFldIds.add(frmFld.getId());
                        }
                    }
                }
                if (reqFldIds.size() > 0) {
                    hardStopFieldIds = reqFldIds.toArray(new String[0]);
                }
            }
        }
        return hardStopFieldIds;
    }

    /**
     * Will initialize any passed in value from <code>bundle</code>.
     * 
     * @param bundle
     *            the bundle of values.
     */
    private void initState(Bundle bundle) {
        if (bundle.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY)) {
            atnTypeKey = bundle.getString(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY);
            if (atnTypeKey != null) {
                ConcurCore concurCore = getConcurCore();
                UserConfig userConfig = concurCore.getUserConfig();
                if (userConfig != null) {
                    AttendeeType atdType = userConfig.getAttendeeType(atnTypeKey);
                    if (atdType != null) {
                        isExternalAttendee = atdType.isExternal;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".initState: atdType is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initState: userConfig is null!");
                }
            }
        }
        if (bundle.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_KEY)) {
            atnKey = bundle.getString(Const.EXTRA_EXPENSE_ATTENDEE_KEY);
        }
        if (bundle.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_FIRST_NAME)) {
            firstName = bundle.getString(Const.EXTRA_EXPENSE_ATTENDEE_FIRST_NAME);
        }
        if (bundle.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_LAST_NAME)) {
            lastName = bundle.getString(Const.EXTRA_EXPENSE_ATTENDEE_LAST_NAME);
        }
        if (bundle.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_COMPANY)) {
            company = bundle.getString(Const.EXTRA_EXPENSE_ATTENDEE_COMPANY);
        }
        if (bundle.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_TITLE)) {
            title = bundle.getString(Const.EXTRA_EXPENSE_ATTENDEE_TITLE);
        }
        if (bundle.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_AMOUNT)) {
            amount = bundle.getDouble(Const.EXTRA_EXPENSE_ATTENDEE_AMOUNT);
        }
        if (bundle.containsKey(Const.EXTRA_EXPENSE_TRANSACTION_AMOUNT)) {
            crnCode = bundle.getString(Const.EXTRA_EXPENSE_TRANSACTION_AMOUNT);
        }
        if (bundle.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_COUNT)) {
            instanceCount = bundle.getInt(Const.EXTRA_EXPENSE_ATTENDEE_COUNT);
        }
        if (bundle.containsKey(EXTRA_IS_CANCELLED)) {
        	isCancelled = bundle.getBoolean(EXTRA_IS_CANCELLED);
        }
    }

    /**
     * Gets the default attendee type key from the passed in list of attendee type keys.
     * 
     * @param atnTypeKeys
     *            the list of attendee type keys.
     * @return the default attendee type key.
     */
    protected String getDefaultAtnTypeKey(List<ListItem> atnTypeKeys) {
        String defAtnTypeKey = null;
        if (atnTypeKeys != null) {
            for (ListItem listItem : atnTypeKeys) {
                if (listItem.code != null && listItem.code.equalsIgnoreCase(Const.DEFAULT_ATTENDEE_TYPE_CODE)) {
                    defAtnTypeKey = listItem.key;
                    break;
                }
            }
            // The default attendee type code was not found, just pick the first
            // one and use it.
            if (defAtnTypeKey == null) {
                if (atnTypeKeys.size() > 0) {
                    defAtnTypeKey = atnTypeKeys.get(0).key;
                }
            }
        }
        return defAtnTypeKey;
    }

    /**
     * Will regenerate the form display based on a newly selected attendee type key.
     */
    protected void regenerateFormDisplay() {

        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.attendee_fields);
        if (viewGroup != null) {
            if (viewGroup.getChildCount() > 0) {
                viewGroup.removeAllViews();
            }

            List<FormFieldView> frmFldViews = new ArrayList<FormFieldView>();
            // If an existing attendee is being edited, set the access type of
            // the
            // attendee type field to read-only.
            boolean addClickDialog = false;
            if (atnKey != null && atnKey.length() > 0) {
                if (atnForm.getFormFields() != null) {
                    for (ExpenseReportFormField expRepFrmFld : atnForm.getFormFields()) {
                        if (expRepFrmFld.getId().equalsIgnoreCase(AttendeeFormFieldViewListener.TYPE_KEY_FIELD_ID)) {
                            if (expRepFrmFld.getAccessType() == ExpenseReportFormField.AccessType.RW) {
                                expRepFrmFld.setAccessType(ExpenseReportFormField.AccessType.RO);
                                addClickDialog = true;
                                break;
                            }
                        }
                    }
                }
            }

            List<ExpenseReportFormField> frmFlds = new ArrayList<ExpenseReportFormField>();

            // If not new, if editable and if configured, show fields to edit
            // amount and count
            if (isReportEditable() && (atnKey != null && atnKey.length() > 0)) {
                ExpenseType expType = ExpenseType.findExpenseType(expRep.polKey, expRepEntDet.expKey);
                if (expType.allowEditAtnAmt) {
                    Locale loc = getResources().getConfiguration().locale;
                    String formattedAmount = FormatUtil.formatAmount(amount, loc, crnCode, false);
                    ExpenseReportFormField field = new ExpenseReportFormField(AMOUNT_EDIT_FIELD_ID, getText(
                            R.string.attendee_amount_label).toString(), formattedAmount, AccessType.RW,
                            ControlType.EDIT, DataType.MONEY, true);
                    frmFlds.add(field);
                }

                if (expType.allowEditAtnCount) {
                    // Default to the RW for attendee count editing.
                    ExpenseReportFormField.AccessType accessType = AccessType.RW;
                    // The AttendeeType as defined in UserConfig determines
                    // whether the attendee count is actually
                    // editable.
                    ConcurCore concurCore = getConcurCore();
                    UserConfig userConfig = concurCore.getUserConfig();
                    if (userConfig != null) {
                        AttendeeType atdType = userConfig.getAttendeeType(atnTypeKey);
                        if (atdType != null && atdType.allowEditAtnCount != null) {
                            if (!atdType.allowEditAtnCount) {
                                accessType = ExpenseReportFormField.AccessType.RO;
                            }
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".regenerateFormDisplay: user config is null!");
                    }
                    ExpenseReportFormField field = new ExpenseReportFormField(COUNT_EDIT_FIELD_ID, getText(
                            R.string.attendee_count_label).toString(), Integer.toString(instanceCount), accessType,
                            ControlType.EDIT, DataType.INTEGER, true);
                    frmFlds.add(field);
                }
            }

            frmFlds.addAll(atnForm.getFormFields());

            // If editing an external attendee, only permit editing of 'instance
            // count' and 'amount'.
            // All the original fields should not be edited. This will result in
            // a short-circuit in
            // 'commitEditedValues' since "instance count" and "amount" are not
            // actually in the form
            // for the attendee.
            if (isExternalAttendee) {
                // Set AccessType to 'RO' in the original set (except for
                // "instance count" and "amount").
                for (ExpenseReportFormField expRepFrmFld : frmFlds) {
                    if (expRepFrmFld.getId() != null
                            && !(expRepFrmFld.getId().equalsIgnoreCase(COUNT_EDIT_FIELD_ID) || expRepFrmFld.getId()
                                    .equalsIgnoreCase(AMOUNT_EDIT_FIELD_ID))) {
                        expRepFrmFld.setAccessType(AccessType.RO);
                    }
                }
            }

            frmFldViews.addAll(populateViewWithFormFields(viewGroup, frmFlds, null));
            frmFldViewListener.setFormFieldViews(frmFldViews);
            frmFldViewListener.clearCurrentFormFieldView();
            frmFldViewListener.initFields();
            // If the attendee type field was made read-only from above, then
            // display a dialog upon
            // the end-user selecting it...and also reset the access type to RW!
            // If not reset to RW, an
            // validation error will occur on the server when saving.
            if (addClickDialog) {
                FormFieldView frmFldView = ((AttendeeFormFieldViewListener) frmFldViewListener)
                        .getFieldById(AttendeeFormFieldViewListener.TYPE_KEY_FIELD_ID);
                if (frmFldView != null) {
                    frmFldView.getFormField().setAccessType(ExpenseReportFormField.AccessType.RW);
                    if (frmFldView.getView(this) != null) {
                        frmFldView.getView(this).setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                showDialog(Const.DIALOG_EXPENSE_ATTENDEE_TYPE_NO_EDIT);
                            }
                        });
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".regenerateFormDisplay: unable to locate attendee type form field view!");
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".regenerateFormDisplay: unable to locate linear layout view group!");
        }

        // Ensure the attendee fields group is visible.
        View view = findViewById(R.id.attendee_fields_group);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".regenerateFormDisplay: unable to locate attendee fields group!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getBroadcastReceiverIntentFilter()
     */
    @Override
    protected IntentFilter getBroadcastReceiverIntentFilter() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getHeaderNavBarTitleResourceId()
     */
    @Override
    protected int getHeaderNavBarTitleResourceId() {
        return R.string.attendee_edit;
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
        return isReportEditable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#sendSaveRequest ()
     */
    @Override
    protected void sendSaveRequest() {
        sendAttendeeSaveRequest();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# shouldReceiveDataEvents()
     */
    @Override
    protected boolean shouldReceiveDataEvents() {
        return false;
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
            if (hasFormFieldsChanged()) {
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

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of retrieving the list of
     * attendee types.
     */
    static class AttendeeTypeReceiver extends BaseBroadcastReceiver<ExpenseAttendeeEdit, SearchListRequest> {

        private static final String CLS_TAG = ExpenseAttendeeEdit.CLS_TAG + "."
                + AttendeeTypeReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>AttendeeTypeReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AttendeeTypeReceiver(ExpenseAttendeeEdit activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(ExpenseAttendeeEdit activity) {
            activity.attendeeTypeRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
        }

        @Override
        protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_PROGRESS);
            return super.handleHttpError(context, intent, httpStatus);
        }

        @Override
        protected void handleRequestFailure(Context context, Intent intent, int requestStatus) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_PROGRESS);
            super.handleRequestFailure(context, intent, requestStatus);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_PROGRESS);
            activity.showDialog(Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            ConcurCore ConcurCore = activity.getConcurCore();
            IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
            if (expRepCache != null) {
                ExpenseType expType = ExpenseType.findExpenseType(activity.expRep.polKey, activity.expRepEntDet.expKey);
                List<ListItem> atnTypeKeys = expRepCache.getAttendeeTypes(expType, expRepCache.getAddAttendeeTypes());
                if (atnTypeKeys != null) {
                    activity.atnTypeKey = activity.getDefaultAtnTypeKey(atnTypeKeys);
                    if (activity.atnTypeKey != null) {
                        activity.sendAttendeeFormRequest(activity.atnTypeKey, activity.atnKey);
                    } else {
                        // No attendee types available.
                        activity.showDialog(Const.DIALOG_EXPENSE_NO_ATTENDEE_TYPES);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: attendee type list is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: active report cache is null!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(SearchListRequest request) {
            activity.attendeeTypeRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterAttendeeTypeReceiver();
        }

    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of retrieving a form for
     * attendee editing.
     */
    static class AttendeeFormReceiver extends BaseBroadcastReceiver<ExpenseAttendeeEdit, AttendeeFormRequest> {

        private static final String CLS_TAG = ExpenseAttendeeEdit.CLS_TAG + "."
                + AttendeeFormReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>AttendeeFormReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AttendeeFormReceiver(ExpenseAttendeeEdit activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(ExpenseAttendeeEdit activity) {
            activity.attendeeFormRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // Grab the attendee form from the application object and place it
            // into our map.
            ConcurCore ConcurCore = activity.getConcurCore();
            ExpenseReportAttendee attendeeForm = ConcurCore.getAttendeeForm();
            if (attendeeForm != null) {
                if (intent.hasExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY)) {
                    String atnTypeKey = intent.getStringExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY);
                    activity.callRegenerateScreen(atnTypeKey, attendeeForm);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: intent is missing attendee type key!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: downloaded attendee form is null!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(AttendeeFormRequest request) {
            activity.attendeeFormRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterAttendeeFormReceiver();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_DUPLICATE_ATTENDEE:
            if (resultCode == RESULT_OK) {
                // Some attendee was selected, use it
                if (resultData == null) {
                    resultData = new Intent();
                }

                // Pass along the selected atnKey to find the attendee in the
                // attendee list activity
                resultData.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_KEY,
                        data.getStringExtra(Const.EXTRA_EXPENSE_ATTENDEE_KEY));
                // pass default value to true
                resultData.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_OLD_KEY_FROM_DUP,
                        data.getStringExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_OLD_KEY_FROM_DUP));
                setResult(RESULT_OK, resultData);

                finish();

            } else {
                // Cancelled out
                setResult(RESULT_CANCELED);
                finish();
            }
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    /**
     * call regenerate screen from oncreate and onrecieve method.
     * 
     * @param atnTypeKey
     * @param attendeeForm
     */
    private void callRegenerateScreen(String atnTypeKey, ExpenseReportAttendee attendeeForm) {
        if (atnTypeKey != null) {
            atnTypeKey = atnTypeKey.trim();
            if (atnTypeKey.length() > 0) {
                this.typeAttendeeMap.put(atnTypeKey, attendeeForm);
                this.atnTypeKey = atnTypeKey;
                this.atnForm = attendeeForm;
                this.regenerateFormDisplay();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".callRegenerateScreen: attendee type key is empty!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".callRegenerateScreen: intent has null attendee type key!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of saving an attendee.
     */
    static class AttendeeSaveReceiver extends BaseBroadcastReceiver<ExpenseAttendeeEdit, AttendeeSaveRequest> {

        // private static final String CLS_TAG = ExpenseAttendeeEdit.CLS_TAG +
        // "." + AttendeeSaveReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>AttendeeSaveReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AttendeeSaveReceiver(ExpenseAttendeeEdit activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(ExpenseAttendeeEdit activity) {
            activity.attendeeSaveRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_ATTENDEE_SAVE_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_ATTENDEE_SAVE_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            ConcurCore app = (ConcurCore) activity.getApplication();
            AttendeeSaveReply reply = app.getAttendeeSaveResults();
            if (reply.duplicateAttendees != null) {
                // There were duplicates, handle them.
                Intent i = new Intent(context, ExpenseDuplicateAttendee.class);
                i.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, activity.expRep.reportKey);
                i.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, activity.expRepEntDet.reportEntryKey);
                i.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, activity.reportKeySource);
                i.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_KEY, activity.atnKey);
                activity.startActivityForResult(i, REQUEST_DUPLICATE_ATTENDEE);
            } else {
                if (activity.resultData != null) {
                    // Add the atnKey to these results to find the attendee in
                    // the list activity
                    activity.resultData.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_KEY, reply.attendee.atnKey);
                    activity.setResult(Activity.RESULT_OK, activity.resultData);
                } else {
                    activity.setResult(Activity.RESULT_OK);
                }
                activity.finish();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(AttendeeSaveRequest request) {
            activity.attendeeSaveRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterAttendeeSaveReceiver();
        }

    }

    /**
     * An extension of <code>FormFieldViewListener</code> for the purposes of handling form regeneration for an attendee.
     * 
     * @author AndrewK
     */
    class AttendeeFormFieldViewListener extends FormFieldViewListener {

        // private String CLS_TAG = ExpenseAttendeeEdit.CLS_TAG + '.' +
        // AttendeeFormFieldViewListener.class.getSimpleName();

        static final String TYPE_KEY_FIELD_ID = "AtnTypeKey";
        static final String FIRST_NAME_FIELD_ID = "FirstName";
        static final String LAST_NAME_FIELD_ID = "LastName";
        static final String TITLE_FIELD_ID = "Title";
        static final String COMPANY_FIELD_ID = "Company";

        public AttendeeFormFieldViewListener(BaseActivity activity) {
            super(activity);
        }

        public AttendeeFormFieldViewListener(BaseActivity activity, ExpenseReport expenseReport,
                ExpenseReportEntry expenseReportEntry) {
            super(activity, expenseReport, expenseReportEntry);
        }

        public AttendeeFormFieldViewListener(BaseActivity activity, ExpenseReport expenseReport) {
            super(activity, expenseReport);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.util.FormFieldViewListener#valueChanged(com.concur .mobile.util.FormFieldView)
         */
        @Override
        public void valueChanged(FormFieldView frmFldView) {

            // Check for whether the attendee type field changes.
            if (frmFldView.getFormField().getId().equalsIgnoreCase(TYPE_KEY_FIELD_ID)) {
                if (frmFldView.hasValueChanged()) {
                    if (frmFldView instanceof SearchListFormFieldView) {
                        // Grab the new atnTypeKey from the 'liKey' value for
                        // the newly chosen form
                        // and retrieve a new form.
                        SearchListFormFieldView srchListFrmFldView = (SearchListFormFieldView) frmFldView;
                        atnTypeKey = srchListFrmFldView.getLiKey();
                        sendAttendeeFormRequest(atnTypeKey, atnKey);
                    }
                }
            }
        }

        /**
         * Will ensure that any base values that might be contained in form fields are set on the base object.
         */
        public void setBaseAttendeeValuesFromFields() {
            FormFieldView frmFldView = getFieldById(FIRST_NAME_FIELD_ID);
            if (frmFldView != null) {
                if (frmFldView.getCurrentValue() != null) {
                    atnForm.firstName = frmFldView.getCurrentValue();
                    if (atnForm.firstName != null) {
                        atnForm.firstName = atnForm.firstName.trim();
                    }
                }
            }
            frmFldView = getFieldById(LAST_NAME_FIELD_ID);
            if (frmFldView != null) {
                if (frmFldView.getCurrentValue() != null) {
                    atnForm.lastName = frmFldView.getCurrentValue();
                    if (atnForm.lastName != null) {
                        atnForm.lastName = atnForm.lastName.trim();
                    }
                }
            }
            frmFldView = getFieldById(COMPANY_FIELD_ID);
            if (frmFldView != null) {
                if (frmFldView.getCurrentValue() != null) {
                    atnForm.company = frmFldView.getCurrentValue();
                    if (atnForm.company != null) {
                        atnForm.company = atnForm.company.trim();
                    }
                }
            }
            frmFldView = getFieldById(TITLE_FIELD_ID);
            if (frmFldView != null) {
                if (frmFldView.getCurrentValue() != null) {
                    atnForm.title = frmFldView.getCurrentValue();
                    if (atnForm.title != null) {
                        atnForm.title = atnForm.title.trim();
                    }
                }
            }
        }

        /**
         * Will perform an required initialization over the form fields.
         */
        @Override
        public void initFields() {
            // Initialize fields.

            // Initialize the first name, if set.
            if (firstName != null) {
                FormFieldView ffv = getFieldById(FIRST_NAME_FIELD_ID);
                if (ffv != null) {
                    // It's possible the new form does not have this field
                    ffv.setCurrentValue(firstName, false);
                }
            }
            // Initialize the last name, if set.
            if (lastName != null) {
                FormFieldView ffv = getFieldById(LAST_NAME_FIELD_ID);
                if (ffv != null) {
                    // It's possible the new form does not have this field
                    ffv.setCurrentValue(lastName, false);
                }
            }
            // Initialize the company name, if set.
            if (company != null) {
                FormFieldView ffv = getFieldById(COMPANY_FIELD_ID);
                if (ffv != null) {
                    // It's possible the new form does not have this field
                    ffv.setCurrentValue(company, false);
                }
            }
            // Initialize the title, if set.
            if (title != null) {
                FormFieldView ffv = getFieldById(TITLE_FIELD_ID);
                if (ffv != null) {
                    // It's possible the new form does not have this field
                    ffv.setCurrentValue(title, false);
                }
            }

            // Set the exclude list on the attendee type search field
            ExpenseReport report = getExpenseReport();
            ExpenseReportEntry entry = getExpenseReportEntry();
            if (report != null && entry != null) {
                ExpenseType expType = ExpenseType.findExpenseType(report.polKey, entry.expKey);
                if (expType != null && expType.unallowedAttendeeTypeKeys != null) {
                    FormFieldView ffv = getFieldById(TYPE_KEY_FIELD_ID);
                    if (ffv != null && ffv instanceof SearchListFormFieldView) {
                        ((SearchListFormFieldView) ffv).setExcludeKeys(expType.unallowedAttendeeTypeKeys);
                    }
                }
            }
        }

        /**
         * Gets an instance of <code>FormFieldView</code> based on the underlying field id.
         * 
         * @param id
         *            the form field id.
         * @return an instance of <code>FormFieldView</code> whose form field id matches <code>id</code>; otherwise
         *         <code>null</code> is returned.
         */
        private FormFieldView getFieldById(String id) {
            FormFieldView retVal = null;
            List<FormFieldView> frmFldViews = getFormFieldViews();
            if (frmFldViews != null) {
                for (FormFieldView frmFldView : frmFldViews) {
                    if (frmFldView.getFormField().getId().equalsIgnoreCase(id)) {
                        retVal = frmFldView;
                        break;
                    }
                }
            }
            return retVal;
        }

    }

}
