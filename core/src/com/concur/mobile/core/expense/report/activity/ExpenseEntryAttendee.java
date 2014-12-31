/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.ControlType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.DataType;
import com.concur.mobile.core.expense.report.service.AttendeeSaveReply;
import com.concur.mobile.core.expense.report.service.SaveReportEntryRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldViewListener;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides an activity to display expense report entry attendees.
 * 
 * @author AndrewK
 */
public class ExpenseEntryAttendee extends AbstractExpenseActivity {

    private static final String NOSHOW_FIELD_ID = "noshow";

    private static final String CLS_TAG = ExpenseEntryAttendee.class.getSimpleName();

    private static final String SAVE_REPORT_ENTRY_RECEIVER_KEY = "save.report.entry.receiver";

    private static final String SELECTED_ATTENDEE_KEY = "selected.attendee";

    private static final String SELECTED_CONTACT_INFO_KEY = "selected.contact.info";

    private static final int REQUEST_SELECT_CONTACT = 1;

    private static final int REQUEST_ADD_FROM_SEARCH = 2;

    private static final int REQUEST_ADD_FROM_CREATE = 3;

    private static final int REQUEST_ADD_FROM_CONTACT = 4;

    private static final int REQUEST_EDIT_ATTENDEE = 5;

    private static enum AttendeeAction {
        ADD, // An attendee is added.
        REMOVE, // An attendee is removed.
        UPDATE
        // An attendee is updated.
    }

    /**
     * Contains the current attendee action. This is used to determine what progress dialogs to show and dismiss.
     */
    protected AttendeeAction currentAttendeeAction;

    /**
     * Contains a reference to the instance of <code>ExpenseReportEntry</code> that was passed to this activity.
     */
    protected ExpenseReportEntryDetail expRepEntDet;

    /**
     * Contains the list adapter used to populate the "add attendee" options.
     */
    private AddAttendeeOptionListAdapter addAttendeeActionAdapter;

    /**
     * Contains the currently selected contact information from which an attendee may be created.
     */
    private AttendeeContactInfo selectedContactInfo;

    /**
     * Contains the receiver used to handle saving a report entry.
     */
    private SaveReportEntryReceiver saveReportEntryReceiver;

    /**
     * Contains the intent filter used to register the above report entry receiver.
     */
    private IntentFilter saveReportEntryFilter;

    /**
     * Contains an outstanding request to save the report entry.
     */
    private SaveReportEntryRequest saveReportEntryRequest;

    /**
     * Contains the currently selected attendee.
     */
    protected ExpenseReportAttendee selectedAttendee;

    /**
     * Contains the list of attendees currently being edited.
     */
    protected List<ExpenseReportAttendee> editedAttendees;

    /**
     * Contains the transaction currency code.
     */
    protected String transCurCode;

    /**
     * Contains the transaction amount.
     */
    protected Double transAmt;

    /**
     * Data object indicating no show count has changed
     */
    protected Intent resultData;

    /**
     * Contains the list of "add attendee" options.
     */
    private enum AddAttendeeAction {
        ADD_FROM_SEARCH, // Add from the results of an attendee search.
        ADD_FROM_CONTACTS, // Add from selecting a device contact.
        ADD_FROM_CREATE
        // Add from creating a new contact.
    };

    /**
     * Contains a map from a view object to an expense report attendee object. This map is used to get the attendee domain object
     * associated with a long-press on a attendee view.
     */
    protected Map<View, ExpenseReportAttendee> viewAttendeeMap = new HashMap<View, ExpenseReportAttendee>();

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onCreate: ");

        initFromIntent(getIntent());
        initFromSavedInstanceState(savedInstanceState);

        super.onCreate(savedInstanceState);

        // Restore any receivers.
        restoreReceivers();
    }

    /**
     * Will initialize from passed intent data.
     * 
     * @param intent
     *            the initialization data.
     */
    protected void initFromIntent(Intent intent) {
        // Init transaction amount.
        transAmt = intent.getDoubleExtra(Const.EXTRA_EXPENSE_TRANSACTION_AMOUNT, 0.0D);
        // Init transaction currency code.
        if (intent.hasExtra(Const.EXTRA_EXPENSE_TRANSACTION_CURRENCY)) {
            transCurCode = intent.getStringExtra(Const.EXTRA_EXPENSE_TRANSACTION_CURRENCY);
        } else {
            if (isReportEditable()) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".initFromIntent: editable report but no transaction currency code passed!");
            }
        }
    }

    /**
     * Will initialize from any saved instance data.
     * 
     * @param savedState
     */
    protected void initFromSavedInstanceState(Bundle savedState) {
        if (savedState != null) {
            // Restore 'selectedAttendee'
            if (savedState.containsKey(SELECTED_ATTENDEE_KEY)) {
                boolean foundSelectedAttendee = false;
                String selAtnKey = savedState.getString(SELECTED_ATTENDEE_KEY);
                ConcurCore ConcurCore = getConcurCore();
                List<ExpenseReportAttendee> editingAttendees = ConcurCore.getEditedAttendees();
                if (editingAttendees != null) {
                    for (ExpenseReportAttendee atn : editingAttendees) {
                        if (atn.atnKey != null && atn.atnKey.equalsIgnoreCase(selAtnKey)) {
                            selectedAttendee = atn;
                            foundSelectedAttendee = true;
                            break;
                        }
                    }
                }
                if (!foundSelectedAttendee) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initFromSavedInstanceState: unable to restore selected attendee!");
                }
            }
            // Restore 'selectedContactInfo'.
            if (savedState.containsKey(SELECTED_CONTACT_INFO_KEY)) {
                selectedContactInfo = (AttendeeContactInfo) savedState.getSerializable(SELECTED_CONTACT_INFO_KEY);
                if (selectedContactInfo == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".initFromSavedInstanceState: unable to restore 'selectedContactInfo'!");
                }
            }
        }
    }

    @Override
    protected FormFieldViewListener createFormFieldViewListener() {
        return new NoShowFormFieldListener(this);
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
        case Const.DIALOG_EXPENSE_ADD_ATTENDEE: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.add_attendee_options));
            addAttendeeActionAdapter = new AddAttendeeOptionListAdapter();
            addAttendeeActionAdapter.options.add(AddAttendeeAction.ADD_FROM_SEARCH);
            addAttendeeActionAdapter.options.add(AddAttendeeAction.ADD_FROM_CONTACTS);
            addAttendeeActionAdapter.options.add(AddAttendeeAction.ADD_FROM_CREATE);
            builder.setSingleChoiceItems(addAttendeeActionAdapter, -1, new AddAttendeeDialogListener());
            dialog = builder.create();
            break;
        }
        case Const.DIALOG_EXPENSE_CONTACT_IMPORT_FAILED: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dlg_expense_attendee_contact_import_failed_title);
            builder.setMessage(getText(R.string.dlg_expense_attendee_contact_import_failed_message));
            builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            dialog = builder.create();
            break;
        }
        case Const.DIALOG_EXPENSE_ATTENDEE_CONFIRM_CONTACT_CHOICE: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dlg_expense_confirm_contact_selection_title);
            builder.setMessage("");
            builder.setCancelable(true);
            builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    if (selectedContactInfo != null) {
                        // Launch the attendee editing activity.
                        Intent intent = new Intent(ExpenseEntryAttendee.this, ExpenseAttendeeEdit.class);
                        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
                        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, expRepEntDet.reportEntryKey);
                        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
                        if (selectedContactInfo.firstName != null) {
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_FIRST_NAME, selectedContactInfo.firstName);
                        }
                        if (selectedContactInfo.lastName != null) {
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_LAST_NAME, selectedContactInfo.lastName);
                        }
                        if (selectedContactInfo.displayName != null) {
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_DISPLAY_NAME, selectedContactInfo.displayName);
                        }
                        if (selectedContactInfo.title != null) {
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_TITLE, selectedContactInfo.title);
                        }
                        if (selectedContactInfo.company != null) {
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_COMPANY, selectedContactInfo.company);
                        }
                        startActivityForResult(intent, REQUEST_ADD_FROM_CONTACT);
                        selectedContactInfo = null;
                        // Set the attendee action.
                        currentAttendeeAction = AttendeeAction.ADD;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: selectedContactInfo is null!");
                    }
                }
            });
            builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    selectedContactInfo = null;
                }
            });
            builder.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    selectedContactInfo = null;
                }
            });
            dialog = builder.create();
            break;
        }
        case Const.DIALOG_EXPENSE_ATTENDEE_ADD_PROGRESS:
        case Const.DIALOG_EXPENSE_ATTENDEE_REMOVE_PROGRESS:
        case Const.DIALOG_EXPENSE_ATTENDEE_UPDATE_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(this.getText(getAttendeeActionProgressTextResourceId()));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (saveReportEntryRequest != null) {
                        saveReportEntryRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog.onCancel: saveReportEntryRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_ATTENDEE_ADD_FAILED:
        case Const.DIALOG_EXPENSE_ATTENDEE_REMOVE_FAILED:
        case Const.DIALOG_EXPENSE_ATTENDEE_UPDATE_FAILED: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getAttendeeActionFailTitleTextResourceId());
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
        case Const.DIALOG_EXPENSE_CONFIRM_ATTENDEE_REMOVE: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dlg_expense_confirm_attendee_remove_title);
            builder.setMessage("");
            builder.setCancelable(true);
            builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    if (selectedAttendee != null) {
                        handleRemoveAttendee(selectedAttendee);
                        selectedAttendee = null;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog.onClick(OK): selectedAttendee is null!");
                    }
                }
            });
            builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    selectedAttendee = null;
                }
            });
            builder.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    selectedAttendee = null;
                }
            });
            dialog = builder.create();
            break;

        }
        case Const.DIALOG_EXPENSE_ATTENDEE_NO_EDIT: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.attendee);
            builder.setMessage(getText(R.string.dlg_expense_attendee_no_edit_message));
            builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            dialog = builder.create();
            break;
        }
        case Const.DIALOG_EXPENSE_ATTENDEE_VERSION_MISMATCH: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.attendee);
            builder.setMessage(getText(R.string.dlg_expense_attendee_version_mismatch_message));
            builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
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
        case Const.DIALOG_EXPENSE_ATTENDEE_CONFIRM_CONTACT_CHOICE: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            // Dev Note: Found it interesting that with managed dialogs, when
            // the Android runtime will re-create a dialog
            // with a call to 'onPrepare', but won't actually show the dialog if
            // it's been dismissed. Therefore, the code
            // below checks for whether domain data is actually set prior to
            // accessing it. The runtime will still call
            // 'onPrepareDialog' for a dialog it won't actually show! This can
            // be easily created by changing orientation
            // a number of times, then clicking on a dialog button that will set
            // to 'null' a piece of domain data. When switching
            // back to a configuration that had the managed dialog displayed,
            // the runtime will attempt to call 'onPrepare' on
            // the dialog, but won't actually display it!
            if (selectedContactInfo != null) {
                String prompt = Format.localizeText(this, R.string.dlg_expense_confirm_contact_selection_message,
                        selectedContactInfo.displayName);
                alertDlg.setMessage(prompt);
            }
            break;
        }
        case Const.DIALOG_EXPENSE_CONFIRM_ATTENDEE_REMOVE: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (selectedAttendee != null) {
                StringBuilder strBldr = new StringBuilder();
                String firstName = selectedAttendee.getFirstName();
                if (firstName != null) {
                    strBldr.append(firstName);
                    strBldr.append(' ');
                }
                strBldr.append(selectedAttendee.getLastName());
                String prompt = Format.localizeText(this, R.string.dlg_expense_confirm_attendee_remove_message,
                        strBldr.toString());
                alertDlg.setMessage(prompt);
            }
            break;
        }
        case Const.DIALOG_EXPENSE_ATTENDEE_ADD_FAILED:
        case Const.DIALOG_EXPENSE_ATTENDEE_REMOVE_FAILED:
        case Const.DIALOG_EXPENSE_ATTENDEE_UPDATE_FAILED: {
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
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handled = super.onContextItemSelected(item);
        if (!handled) {
            if (item.getItemId() == AttendeeAction.UPDATE.ordinal()) {
                if (selectedAttendee != null) {
                    if (ConcurCore.isConnected()) {
                        ExpenseReportAttendee defAtt = getConcurCore().getExpenseActiveCache().getDefaultAttendee();
                        if (defAtt != null && !selectedAttendee.isEditable(defAtt)) {
                            showDialog(Const.DIALOG_EXPENSE_ATTENDEE_NO_EDIT);
                        } else if (selectedAttendee.isVersionMismatch()) {
                            showDialog(Const.DIALOG_EXPENSE_ATTENDEE_VERSION_MISMATCH);
                        } else {
                            Intent intent = new Intent(this, ExpenseAttendeeEdit.class);
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, expRepEntDet.reportEntryKey);
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY, selectedAttendee.atnTypeKey);
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_KEY, selectedAttendee.atnKey);
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY, selectedAttendee.getTypeKey());
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_AMOUNT, selectedAttendee.amount);
                            intent.putExtra(Const.EXTRA_EXPENSE_TRANSACTION_CURRENCY,
                                    ((transCurCode != null) ? transCurCode : expRepEntDet.transactionCrnCode));
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_COUNT, selectedAttendee.instanceCount);
                            // MOB-11721
                            if (selectedAttendee.getFirstName() != null) {
                                intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_FIRST_NAME,
                                        selectedAttendee.getFirstName());
                            }
                            if (selectedAttendee.getLastName() != null) {
                                intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_LAST_NAME, selectedAttendee.getLastName());
                            }
                            if (selectedAttendee.getTitle() != null) {
                                intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_TITLE, selectedAttendee.getTitle());
                            }
                            if (selectedAttendee.getCompany() != null) {
                                intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_COMPANY, selectedAttendee.getCompany());
                            }
                            startActivityForResult(intent, REQUEST_EDIT_ATTENDEE);
                        }
                    } else {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onContextItemSelected(UPDATE): selectedAttendee is null!");
                }
                handled = true;
            } else if (item.getItemId() == AttendeeAction.REMOVE.ordinal()) {
                if (selectedAttendee != null) {
                    showDialog(Const.DIALOG_EXPENSE_CONFIRM_ATTENDEE_REMOVE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onContextItemSelected(REMOVE): selectedAttendee is null!");
                }
                handled = true;
            }
        }
        return handled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View,
     * android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        selectedAttendee = viewAttendeeMap.get(v);
        if (selectedAttendee != null) {
            menu.setHeaderTitle(R.string.expense_attendee_action);
            menu.add(0, AttendeeAction.UPDATE.ordinal(), 0, R.string.edit);
            menu.add(0, AttendeeAction.REMOVE.ordinal(), 0, R.string.remove_from_expense);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".onCreateContextMenu: unable to locate selected attendee in view-attendee map!");
        }
    }

    /**
     * Gets the id of the dialog to show progress based on the current value of <code>currentAttendeeAction</code>.
     * 
     * @return the id of the dialog to show progress based on the current value of <code>currentAttendeeAction</code>.
     */
    protected int getAttendeeActionProgressDialog() {
        int dialogId = -1;

        if (currentAttendeeAction != null) {
            switch (currentAttendeeAction) {
            case ADD: {
                dialogId = Const.DIALOG_EXPENSE_ATTENDEE_ADD_PROGRESS;
                break;
            }
            case REMOVE: {
                dialogId = Const.DIALOG_EXPENSE_ATTENDEE_REMOVE_PROGRESS;
                break;
            }
            case UPDATE: {
                dialogId = Const.DIALOG_EXPENSE_ATTENDEE_UPDATE_PROGRESS;
                break;
            }
            }
        }
        return dialogId;
    }

    /**
     * Gets the id of the dialog to show failure based on the current value of <code>currentAttendeeAction</code>.
     * 
     * @return the id of the dialog to show failure based on the current value of <code>currentAttendeeAction</code>.
     */
    protected int getAttendeeActionFailureDialog() {
        int dialogId = -1;
        if (currentAttendeeAction != null) {
            switch (currentAttendeeAction) {
            case ADD: {
                dialogId = Const.DIALOG_EXPENSE_ATTENDEE_ADD_FAILED;
                break;
            }
            case REMOVE: {
                dialogId = Const.DIALOG_EXPENSE_ATTENDEE_REMOVE_FAILED;
                break;
            }
            case UPDATE: {
                dialogId = Const.DIALOG_EXPENSE_ATTENDEE_UPDATE_FAILED;
                break;
            }
            }
        }
        return dialogId;
    }

    /**
     * Gets the text resource id to show a progress message based on the value of <code>currentAttendeeAction</code>.
     * 
     * @return the text resource id to show a progress message based on the value of <code>currentAttendeeAction</code>.
     */
    protected int getAttendeeActionProgressTextResourceId() {
        int txtResId = -1;
        if (currentAttendeeAction != null) {
            switch (currentAttendeeAction) {
            case ADD: {
                txtResId = R.string.dlg_expense_attendee_add_progress_message;
                break;
            }
            case REMOVE: {
                txtResId = R.string.dlg_expense_attendee_remove_progress_message;
                break;
            }
            case UPDATE: {
                txtResId = R.string.dlg_expense_attendee_update_progress_message;
                break;
            }
            }
        }
        return txtResId;
    }

    /**
     * Gets the text resource id to show a failure title based on the value of <code>currentAttendeeAction</code>.
     * 
     * @return the text resource id to show a failure title based on the value of <code>currentAttendeeAction</code>.
     */
    protected int getAttendeeActionFailTitleTextResourceId() {
        int txtResId = -1;
        if (currentAttendeeAction != null) {
            switch (currentAttendeeAction) {
            case ADD: {
                txtResId = R.string.dlg_expense_attendee_add_failed_title;
                break;
            }
            case REMOVE: {
                txtResId = R.string.dlg_expense_attendee_remove_failed_title;
                break;
            }
            case UPDATE: {
                txtResId = R.string.dlg_expense_attendee_update_failed_title;
                break;
            }
            }
        }
        return txtResId;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    @Override
    protected void restoreReceivers() {
        // Restore any save report entry receiver.
        if (retainer.contains(SAVE_REPORT_ENTRY_RECEIVER_KEY)) {
            saveReportEntryReceiver = (SaveReportEntryReceiver) retainer.get(SAVE_REPORT_ENTRY_RECEIVER_KEY);
            // Reset the activity reference.
            saveReportEntryReceiver.setActivity(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        ConcurCore ConcurCore = getConcurCore();
        if (!buildViewDelay) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult: build view present, handling result.");
            // Check whether a form field view should handle the activity
            // result.
            if (requestCode == REQUEST_SELECT_CONTACT) {
                if (resultCode == Activity.RESULT_OK) {
                    handlePickedContact(data);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Treat canceling taking a photo as canceling the action.
                    Toast toast = Toast.makeText(this, getText(R.string.attendee_contact_selection_cancelled), 1000);
                    toast.show();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + "onActivityResult(AddFromContacts): unhandled result code '"
                            + resultCode + "'.");
                }
            } else if (requestCode == REQUEST_ADD_FROM_CONTACT) {
                if (resultCode == Activity.RESULT_OK) {
                    AttendeeSaveReply attendeeReply = ConcurCore.getAttendeeSaveResults();
                    ExpenseReportAttendee createdAttendee = attendeeReply.attendee;
                    List<ExpenseReportAttendee> attendees = new ArrayList<ExpenseReportAttendee>(1);
                    attendees.add(createdAttendee);
                    handleAddAttendee(attendees);
                    // Flurry Notification
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_VIA, Flurry.PARAM_VALUE_CONTACTS);
                    params.put(Flurry.PARAM_NAME_ATTENDEE_COUNT, "1");
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_ATTENDEE, Flurry.EVENT_NAME_ADD,
                            params);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Treat canceling choosing a photo as canceling the action.
                    Toast toast = Toast.makeText(this, getText(R.string.attendee_create_from_contact_cancelled), 1000);
                    toast.show();
                }
            } else if (requestCode == REQUEST_ADD_FROM_SEARCH) {
                if (resultCode == Activity.RESULT_OK) {
                    // Handle success.
                    List<ExpenseReportAttendee> selectedAttendees = ConcurCore.getSelectedAttendees();
                    ConcurCore.setSelectedAttendees(null);
                    handleAddAttendee(selectedAttendees);
                    // Flurry Notification
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_VIA, data.getStringExtra(Flurry.PARAM_NAME_VIA));
                    params.put(Flurry.PARAM_NAME_ATTENDEE_COUNT,
                            (selectedAttendees != null) ? Integer.toString(selectedAttendees.size()) : "0");
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_ATTENDEE, Flurry.EVENT_NAME_ADD,
                            params);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Treat canceling choosing a photo as canceling the action.
                    Toast toast = Toast.makeText(this, getText(R.string.attendee_selection_cancelled), 1000);
                    toast.show();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + "onActivityResult(AddFromSearch): unhandled result code '"
                            + resultCode + "'.");
                }
            } else if (requestCode == REQUEST_ADD_FROM_CREATE) {
                if (resultCode == Activity.RESULT_OK) {
                    AttendeeSaveReply attendeeReply = ConcurCore.getAttendeeSaveResults();
                    if (attendeeReply != null) {
                        ExpenseReportAttendee createdAttendee = attendeeReply.attendee;
                        List<ExpenseReportAttendee> attendees = new ArrayList<ExpenseReportAttendee>(1);
                        attendees.add(createdAttendee);
                        handleAddAttendee(attendees);

                        // Flurry Notification
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Flurry.PARAM_NAME_VIA, Flurry.PARAM_VALUE_MANUAL);
                        params.put(Flurry.PARAM_NAME_ATTENDEE_COUNT, "1");
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_ATTENDEE, Flurry.EVENT_NAME_ADD,
                                params);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Treat canceling choosing a photo as canceling the action.
                    Toast toast = Toast.makeText(this, getText(R.string.attendee_create_cancelled), 1000);
                    toast.show();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + "onActivityResult(AddFromCreate): unhandled result code '"
                            + resultCode + "'.");
                }
            } else if (requestCode == REQUEST_EDIT_ATTENDEE) {
                if (resultCode == Activity.RESULT_OK) {
                    AttendeeSaveReply attendeeReply = ConcurCore.getAttendeeSaveResults();
                    ExpenseReportAttendee editedAttendee = null;
                    if (attendeeReply != null) {
                        // Default to the first attendee returned in the reply.
                        editedAttendee = attendeeReply.attendee;
                        // If there were any duplicates, set 'editedAttendee' to
                        // one of the duplicates
                        // if it matches on 'editedAtnKey' in the result data.
                        if (attendeeReply.duplicateAttendees != null && attendeeReply.duplicateAttendees.size() > 0) {
                            if (data != null) {
                                String editedAtnKey = data.getStringExtra(Const.EXTRA_EXPENSE_ATTENDEE_KEY);
                                if (editedAtnKey != null && editedAtnKey.length() > 0) {
                                    editedAtnKey = editedAtnKey.trim();
                                    for (ExpenseReportAttendee expRepAtd : attendeeReply.duplicateAttendees) {
                                        if (expRepAtd.atnKey != null && expRepAtd.atnKey.equalsIgnoreCase(editedAtnKey)) {
                                            editedAttendee = expRepAtd;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (attendeeReply == null && data == null) {
                        Toast toast = Toast.makeText(this, getText(R.string.attendee_edit_no_changes), 1000);
                        toast.show();
                    } else {
                        handleEditedAttendee(editedAttendee, ((data != null) ? (data.getExtras()) : null));
                    }
                } else {
                    Toast toast = Toast.makeText(this, getText(R.string.attendee_edit_cancelled), 1000);
                    toast.show();
                }
            }

            // Clear out the last saved attendee results
            // This needs to happen to handle a situation where an attendee is
            // added/edited and then
            // re-entered to change amount/count. We do not want the old
            // attendee record to be found
            // unless it truly was saved.
            ConcurCore.setAttendeeSaveResults(null);

            // Ensure any fetched attendee form is cleared out.
            ConcurCore.setAttendeeForm(null);

        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult: build view delayed, delaying handling of result.");
            activityResultDelay = true;
            activityResultRequestCode = requestCode;
            activityResultResultCode = resultCode;
            activityResultData = data;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save 'selectedAttendee'.
        if (selectedAttendee != null) {
            outState.putString(SELECTED_ATTENDEE_KEY, selectedAttendee.atnKey);
        }
        // Save 'selectedContactInfo'.
        if (selectedContactInfo != null) {
            outState.putSerializable(SELECTED_CONTACT_INFO_KEY, selectedContactInfo);
        }
    }

    /**
     * Will handle the adding of an attendee to this expense.
     * 
     * @param attendee
     *            the attendee to add to this expense.
     */
    private void handleAddAttendee(List<ExpenseReportAttendee> attendees) {

        if (editedAttendees != null) {
            for (ExpenseReportAttendee attendee : attendees) {
                if (!attendeeAlreadyInList(editedAttendees, attendee)) {
                    // Set the attendee action.
                    currentAttendeeAction = AttendeeAction.ADD;

                    // Add the selected attendee to the list of attendees.
                    if (editedAttendees == null) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".handleAddAttendee: editedAttendees is null!");
                        editedAttendees = new ArrayList<ExpenseReportAttendee>();
                        getConcurCore().setEditedAttendees(editedAttendees);
                    }
                    editedAttendees.add(attendee);
                    // Initialize the 'instanceCount' and 'isAmountEdited'
                    // fields.
                    attendee.instanceCount = 1;
                    attendee.isAmountEdited = false;
                    // Update the allotments among attendees.
                    expRepEntDet.divideAmountAmongAttendees(transAmt, transCurCode, editedAttendees);
                    // regenerate the attendee list.
                    regenerateAttendeeListView();
                    // Pass the result back to the calling activity.
                    setResult(Activity.RESULT_OK);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleAddAttendee: editedAttendees is null!");
        }
    }

    /**
     * Determines whether an attendee to be added to a list of attendees is not in that list already based on matching on attendee
     * key and version number.
     * 
     * @param attendees
     *            the list of attendees to check.
     * @param attendeeToAdd
     *            the candidate attendee to add.
     * @return whether <code>attendeeToAdd</code> is already in <code>attendees</code>.
     */
    private boolean attendeeAlreadyInList(List<ExpenseReportAttendee> attendees, ExpenseReportAttendee attendeeToAdd) {
        boolean retVal = false;
        if (attendees != null) {
            for (ExpenseReportAttendee attendee : attendees) {
                if (attendee.atnKey != null && attendeeToAdd.atnKey != null
                        && attendee.atnKey.equalsIgnoreCase(attendeeToAdd.atnKey) && attendee.versionNumber != null
                        && attendeeToAdd.versionNumber != null
                        && attendee.versionNumber.equalsIgnoreCase(attendeeToAdd.versionNumber)) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Will handle the removing of an attendee from this expense.
     * 
     * @param attendee
     *            the attendee to remove from this expense.
     */
    private void handleRemoveAttendee(ExpenseReportAttendee attendee) {
        // Set the attendee action.
        currentAttendeeAction = AttendeeAction.REMOVE;
        if (editedAttendees != null) {
            boolean foundAttendee = false;
            for (ExpenseReportAttendee att : editedAttendees) {
                if (att.atnKey != null && att.atnKey.equalsIgnoreCase(attendee.atnKey)) {
                    editedAttendees.remove(att);
                    foundAttendee = true;
                    break;
                }
            }
            if (foundAttendee) {
                // Update the allotments among attendees.
                expRepEntDet.divideAmountAmongAttendees(transAmt, transCurCode, editedAttendees);
                // // Kick-off a request save the report entry.
                // sendSaveReportEntryRequest();
                // regenerate the attendee list.
                regenerateAttendeeListView();
                // Pass the result back to the calling activity.
                setResult(Activity.RESULT_OK);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".handleRemoveAttendee: attendee not found in list of existing attendees!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleRemoveAttendee: edited attendee list is null!");
        }
    }

    /**
     * Will handle an edited attendee to be updated within this expense.
     * 
     * @param attendee
     *            the updated attendee within this expense.
     */
    private void handleEditedAttendee(ExpenseReportAttendee attendee, Bundle data) {
        currentAttendeeAction = AttendeeAction.UPDATE;
        boolean needsRecalc = false;

        // // Kick-off a request save the report entry.
        // sendSaveReportEntryRequest();
        // Replace the attendee in the list.
        if (editedAttendees != null) {
            boolean foundAttendee = false;
            String editedAtnKey = null;
            String oldAtnKey = null;
            if (data != null) {
                // The attendee may or may not have changed but amount/count was
                // changed
                // Just grab the sent atnKey and we'll always be correct
                editedAtnKey = data.getString(Const.EXTRA_EXPENSE_ATTENDEE_KEY);
                oldAtnKey = data.getString(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_OLD_KEY_FROM_DUP);
            } else {
                editedAtnKey = attendee.atnKey;
            }

            ListIterator<ExpenseReportAttendee> listIter = editedAttendees.listIterator();
            while (listIter.hasNext()) {
                ExpenseReportAttendee listAtt = listIter.next();
                if (listAtt.atnKey != null && editedAtnKey != null && listAtt.atnKey.equalsIgnoreCase(editedAtnKey)) {
                    if (data != null) {

                        if (attendee == null) {
                            // Use the attendee from the list and just update
                            // fields
                            attendee = listAtt;
                        }

                        if (data.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_AMOUNT)) {
                            attendee.amount = data.getDouble(Const.EXTRA_EXPENSE_ATTENDEE_AMOUNT);
                            attendee.isAmountEdited = true;
                            needsRecalc = true;
                        }

                        if (data.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_COUNT)) {
                            attendee.instanceCount = data.getInt(Const.EXTRA_EXPENSE_ATTENDEE_COUNT);
                            needsRecalc = true;
                        }

                    } else {

                        // Update instance count, or init to 1.
                        if (attendee.instanceCount == null) {
                            if (listAtt.instanceCount != null) {
                                attendee.instanceCount = listAtt.instanceCount;
                            } else {
                                attendee.instanceCount = 1;
                            }
                        }

                        // Copy over 'isAmountEdited' if set, or init to
                        // 'false'.
                        if (attendee.isAmountEdited == null) {
                            if (listAtt.isAmountEdited != null) {
                                attendee.isAmountEdited = listAtt.isAmountEdited;
                            } else {
                                attendee.isAmountEdited = false;
                            }
                        }
                        if (attendee.amount == null) {
                            if (listAtt.amount != null) {
                                attendee.amount = listAtt.amount;
                            }
                        }
                    }

                    listIter.set(attendee);
                    foundAttendee = true;
                    break;
                } else if (listAtt.atnKey != null && oldAtnKey != null && listAtt.atnKey.equalsIgnoreCase(oldAtnKey)) {
                    // This implies that the attendee being edited resulted in a
                    // selected duplicate.
                    if (data != null) {

                        if (attendee == null) {
                            // Use the attendee from the list and just update
                            // fields
                            attendee = listAtt;
                        }

                        if (data.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_AMOUNT)) {
                            attendee.amount = data.getDouble(Const.EXTRA_EXPENSE_ATTENDEE_AMOUNT);
                            attendee.isAmountEdited = true;
                            needsRecalc = true;
                        } else {
                            if (attendee.isAmountEdited == null) {
                                if (listAtt.isAmountEdited != null) {
                                    attendee.isAmountEdited = listAtt.isAmountEdited;
                                } else {
                                    attendee.isAmountEdited = false;
                                }
                            }
                            if (attendee.amount == null) {
                                if (listAtt.amount != null) {
                                    attendee.amount = listAtt.amount;
                                }
                            }
                        }

                        if (data.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_COUNT)) {
                            attendee.instanceCount = data.getInt(Const.EXTRA_EXPENSE_ATTENDEE_COUNT);
                            needsRecalc = true;
                        } else {
                            // Update instance count, or init to 1.
                            if (attendee.instanceCount == null) {
                                if (listAtt.instanceCount != null) {
                                    attendee.instanceCount = listAtt.instanceCount;
                                } else {
                                    attendee.instanceCount = 1;
                                }
                                needsRecalc = true;
                            }
                        }
                    }
                    listIter.set(attendee);
                    foundAttendee = true;
                    break;
                }
            }
            if (!foundAttendee) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".handleEditedAttendee: unable to locate edited attendee in attendee list!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleEditedAttendee: editedAttendees is null!");
        }

        if (needsRecalc) {
            // Update the allotments among attendees.
            expRepEntDet.divideAmountAmongAttendees(transAmt, transCurCode, editedAttendees);
        }

        // regenerate the attendee list.
        regenerateAttendeeListView();
        // Pass the result back to the calling activity.
        setResult(Activity.RESULT_OK);
    }

    /**
     * Will handle the action of picking a contact as the basis for an attendee.
     * 
     * @param data
     *            the intent data for the picked contact.
     */
    private void handlePickedContact(Intent data) {
        final String MTAG = CLS_TAG + ".handlePickedContact: ";
        Uri result = data.getData();
        try {
            Assert.assertNotNull(MTAG + "result is null!", result);
            // get the contact id from the Uri
            String id = result.getLastPathSegment();
            // Check for null id.
            Assert.assertNotNull(MTAG + "contact id is null!", id);
            id = id.trim();
            // Check for non-empty id.
            Assert.assertTrue(MTAG + "contact is is empty!", (id.length() > 0));
            AttendeeContactInfo attContInfo = getAttendeeInfoFromPickedContact(id);
            if (attContInfo != null) {
                if (attContInfo.firstName == null) {
                    attContInfo.firstName = "";
                }
                if (attContInfo.lastName == null) {
                    attContInfo.lastName = "";
                }
                if (attContInfo.company == null || attContInfo.company.length() <= 0) {
                    attContInfo.company = "";
                }
                if (attContInfo.title == null || attContInfo.title.length() <= 0) {
                    attContInfo.title = "";
                }
                selectedContactInfo = attContInfo;
                showDialog(Const.DIALOG_EXPENSE_ATTENDEE_CONFIRM_CONTACT_CHOICE);
            } else {
                showDialog(Const.DIALOG_EXPENSE_CONTACT_IMPORT_FAILED);
            }
        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, afe.getMessage());
            showDialog(Const.DIALOG_EXPENSE_CONTACT_IMPORT_FAILED);
        }
    }

    /**
     * Will look over all contact information associated with <code>contactId</code> and return an
     * <code>AttendeeContactInfo</code> object.
     * 
     * @param contactId
     *            the id of the picked contact.
     * @return an instance of <code>AttendeeContactInfo</code> with all the information filled in that could be found. If not one
     *         field can be found from <code>AttendeeContactInfo</code>, then <code>null</code> will be returned.
     */
    private AttendeeContactInfo getAttendeeInfoFromPickedContact(String contactId) {
        AttendeeContactInfo retVal = null;

        // Query for the first name, last name and company.
        String[] projection = { Data.MIMETYPE, StructuredName.GIVEN_NAME, StructuredName.FAMILY_NAME,
                StructuredName.DISPLAY_NAME, Organization.COMPANY, Organization.TITLE };
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(Data.CONTENT_URI, projection, StructuredName.CONTACT_ID + " = ?",
                    new String[] { contactId }, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        // Obtain the mime-type of the row.
                        String rowMimeType = ViewUtil.getCursorStringValue(cursor, Data.MIMETYPE);
                        if (rowMimeType != null) {
                            rowMimeType = rowMimeType.trim();
                        }
                        if (rowMimeType != null && rowMimeType.length() > 0) {
                            if (rowMimeType.equalsIgnoreCase(StructuredName.CONTENT_ITEM_TYPE)) {
                                // StructuredName information provides first,
                                // last and display names.
                                // Grab the given (first) name.
                                String firstName = ViewUtil.getCursorStringValue(cursor, StructuredName.GIVEN_NAME);
                                if (firstName != null) {
                                    firstName = firstName.trim();
                                    if (firstName.length() > 0) {
                                        if (retVal == null) {
                                            retVal = new AttendeeContactInfo();
                                        }
                                        // Check that we haven't already
                                        // collected first name.
                                        if (retVal.firstName == null || retVal.firstName.length() <= 0) {
                                            retVal.firstName = firstName;
                                        }
                                    }
                                }
                                // Grab the family (last) name.
                                String lastName = ViewUtil.getCursorStringValue(cursor, StructuredName.FAMILY_NAME);
                                if (lastName != null) {
                                    lastName = lastName.trim();
                                    if (lastName.length() > 0) {
                                        if (retVal == null) {
                                            retVal = new AttendeeContactInfo();
                                        }
                                        // Check that we haven't already
                                        // collected last name.
                                        if (retVal.lastName == null || retVal.lastName.length() <= 0) {
                                            retVal.lastName = lastName;
                                        }
                                    }
                                }
                                // Grab the display name.
                                String displayName = ViewUtil.getCursorStringValue(cursor, StructuredName.DISPLAY_NAME);
                                if (displayName != null) {
                                    displayName = displayName.trim();
                                    if (displayName.length() > 0) {
                                        if (retVal == null) {
                                            retVal = new AttendeeContactInfo();
                                        }
                                        // Check that we haven't already
                                        // collected display name.
                                        if (retVal.displayName == null || retVal.displayName.length() <= 0) {
                                            retVal.displayName = displayName;
                                        }
                                    }
                                }
                            } else if (rowMimeType.equalsIgnoreCase(Organization.CONTENT_ITEM_TYPE)) {
                                // Organization information provides company and
                                // title.
                                // Grab the company.
                                String company = ViewUtil.getCursorStringValue(cursor, Organization.COMPANY);
                                if (company != null) {
                                    company = company.trim();
                                    if (company.length() > 0) {
                                        if (retVal == null) {
                                            retVal = new AttendeeContactInfo();
                                        }
                                        // Check that we haven't already
                                        // collected company name.
                                        if (retVal.company == null || retVal.company.length() <= 0) {
                                            retVal.company = company;
                                        }
                                    }
                                }

                                // Grab the title.
                                String title = ViewUtil.getCursorStringValue(cursor, Organization.TITLE);
                                if (title != null) {
                                    title = title.trim();
                                    if (title.length() > 0) {
                                        if (retVal == null) {
                                            retVal = new AttendeeContactInfo();
                                        }
                                        // Check that we haven't already
                                        // collected company name.
                                        if (retVal.title == null || retVal.title.length() <= 0) {
                                            retVal.title = title;
                                        }
                                    }
                                }
                            }
                        }
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            // Ensure 'cursor' is closed.
            if (cursor != null) {
                cursor.close();
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
        return R.string.attendees;
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
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# isSubmitReportEnabled()
     */
    @Override
    protected boolean isSubmitReportEnabled() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_add, menu);

        if (!showTitleBarActionButton()) {
            menu.removeItem(R.id.menuAdd);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuAdd) {
            onAddActionButton();
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

        // Set the content view.
        setContentView(R.layout.expense_entry_attendee);

        // Configure the screen header.
        configureScreenHeader(expRep);

        // Configure the screen footer.
        configureScreenFooter();

        // Set the expense entry.
        Intent intent = getIntent();
        String expRepEntryKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
        if (expRepEntryKey != null) {
            ExpenseReportEntry expRepEntry = expRepCache.getReportEntry(expRep, expRepEntryKey);
            try {
                if (isReportEditable()) {
                    expRepEntDet = getConcurCore().getCurrentEntryDetailForm();
                    if (expRepEntDet == null) {
                        // Cast to the detailed version.
                        expRepEntDet = (ExpenseReportEntryDetail) expRepEntry;
                    }
                } else {
                    // Cast to the detailed version.
                    expRepEntDet = (ExpenseReportEntryDetail) expRepEntry;
                }
                // Initialize any list of attendees currently being edited.
                if (isReportEditable()) {
                    editedAttendees = getConcurCore().getEditedAttendees();
                    if (!editedAttendees.isEmpty()) {
                        // Apply the "pennies" algorithm over the attendees
                        // list.
                        expRepEntDet.divideAmountAmongAttendees(transAmt, transCurCode, editedAttendees);
                    }
                }
                // Set the expense entry title information.
                populateExpenseEntryTitleHeader();

                // Handle the no show field
                ExpenseType expType = ExpenseType.findExpenseType(expRep.polKey, expRepEntDet.expKey);
                Boolean isAllowNoShows = Boolean.TRUE;
                if (expType != null) {
                    isAllowNoShows = expType.allowNoShows;
                }
                if (isAllowNoShows) {
                    // Get our no show amount
                    List<ExpenseReportAttendee> expRepAtts = ((editedAttendees != null) ? editedAttendees
                            : (expRepEntDet.getAttendees()));
                    double noShowAmount = expRepEntDet.getNoShowAmount(transAmt, expRepAtts);
                    Locale loc = getResources().getConfiguration().locale;
                    String noShowAmountString = FormatUtil.formatAmount(noShowAmount, loc, transCurCode, true);

                    // Ensure the group is visible
                    View layout = findViewById(R.id.attendee_noshow_group);
                    layout.setVisibility(View.VISIBLE);

                    // Add the no show field
                    AccessType access = isReportEditable() ? AccessType.RW : AccessType.RO;
                    List<ExpenseReportFormField> frmFlds = new ArrayList<ExpenseReportFormField>();
                    ExpenseReportFormField field = new ExpenseReportFormField(NOSHOW_FIELD_ID, Format.localizeText(
                            this, R.string.attendee_no_show_label, new Object[] { noShowAmountString }),
                            Integer.toString(expRepEntDet.noShowCount), access, ControlType.EDIT, DataType.INTEGER,
                            false);
                    frmFlds.add(field);

                    ViewGroup fieldGroup = (ViewGroup) findViewById(R.id.attendee_noshow_field);
                    List<FormFieldView> frmFldViews = populateViewWithFormFields(fieldGroup, frmFlds, null);

                    // Hook up the listener
                    frmFldViewListener.setExpenseReport(expRep);
                    frmFldViewListener.setExpenseReportEntry(expRepEntDet);
                    frmFldViewListener.setFormFieldViews(frmFldViews);
                    frmFldViewListener.clearCurrentFormFieldView();
                    frmFldViewListener.initFields();
                }

                // Populate the view with the list of attendees.
                populateExpenseAttendees();
                // set up the add attendee action.
                populateAddAttendeeButton();
            } catch (ClassCastException ccExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: non detail expense entry - " + ccExc.getMessage(), ccExc);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: intent does not contain expense entry report key!");
        }
    }

    private void populateAddAttendeeButton() {
        View addAttendeeView = findViewById(R.id.add_attendee);
        if (addAttendeeView != null) {
            if (!isReportEditable()) {
                addAttendeeView.setVisibility(View.GONE);
            } else {
                addAttendeeView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showDialog(Const.DIALOG_EXPENSE_ADD_ATTENDEE);
                    }
                });
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateAddAttendeeButton: unable to locate add attendee button!");
        }
    }

    /**
     * Will set expense entry information in the title header.
     */
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
     * Will reset the reference to the expense entry and then regenerate the list of attendees.
     */
    private void regenerateAttendeeListView() {

        Intent intent = getIntent();
        String expRepEntryKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
        if (expRepEntryKey != null) {
            ExpenseReportEntry expRepEntry = expRepCache.getReportEntry(expRep, expRepEntryKey);
            if (isReportEditable()) {
                expRepEntDet = getConcurCore().getCurrentEntryDetailForm();
                if (expRepEntDet == null) {
                    // Cast to the detailed version.
                    expRepEntDet = (ExpenseReportEntryDetail) expRepEntry;
                }
            } else {
                // Cast to the detailed version.
                expRepEntDet = (ExpenseReportEntryDetail) expRepEntry;
            }

            // Locate the attendee view container and remove all child views.
            LinearLayout ll = (LinearLayout) findViewById(R.id.attendees_list);
            if (ll != null) {
                // Clear the view to attendee map.
                viewAttendeeMap.clear();
                // Punt the views.
                ll.removeAllViews();
                // Add the attendees.
                populateExpenseAttendees();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".regenerateAttendeeListView: can't locate attendee list linear layout!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".regenerateAttendeeListView: intent does not contain expense entry report key!");
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
        showDialog(Const.DIALOG_EXPENSE_ADD_ATTENDEE);
    }

    /**
     * Will populate the content view with expense entry attendee information.
     */
    protected void populateExpenseAttendees() {

        final String MTAG = "populateExpenseAttendees";
        ExpenseType expType = ExpenseType.findExpenseType(expRep.polKey, expRepEntDet.expKey);
        if (expRepEntDet != null) {
            // Locate the view to populate.
            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.attendees_list);
            if (viewGroup != null) {
                List<ExpenseReportAttendee> expRepAtts = ((editedAttendees != null) ? editedAttendees : (expRepEntDet
                        .getAttendees()));
                // Set the long-press message visibility.
                if (isReportEditable()) {
                    setLongPressMessageVisibility((expRepAtts != null && expRepAtts.size() > 0) ? View.VISIBLE
                            : View.GONE);
                } else {
                    setLongPressMessageVisibility(View.GONE);
                }
                if (expRepAtts != null && expRepAtts.size() > 0) {
                    setNoAttendeesVisibility(View.GONE);
                    setAttendeeListVisibility(View.VISIBLE);
                    LayoutInflater inflater = LayoutInflater.from(this);
                    StringBuilder strBldr = new StringBuilder();
                    for (int attInd = 0; attInd < expRepAtts.size(); ++attInd) {
                        ExpenseReportAttendee expRepAtt = expRepAtts.get(attInd);
                        View view = inflater.inflate(R.layout.expense_entry_attendee_row, null);

                        // Set the attendee name information.
                        strBldr.setLength(0);
                        // Last name.
                        String fieldValue = expRepAtt.getLastName();
                        if (fieldValue != null) {
                            fieldValue = fieldValue.trim();
                        }
                        if (fieldValue != null && fieldValue.length() > 0) {
                            strBldr.append(fieldValue);
                        }
                        // First name.
                        fieldValue = expRepAtt.getFirstName();
                        if (fieldValue != null) {
                            fieldValue = fieldValue.trim();
                        }
                        if (fieldValue != null && fieldValue.length() > 0) {
                            if (strBldr.length() > 0) {
                                strBldr.append(", ");
                            }
                            strBldr.append(fieldValue);
                        }
                        TextView txtView = (TextView) view.findViewById(R.id.exp_entries_attendee_row_name);
                        if (txtView != null) {
                            txtView.setText(strBldr.toString());
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + "." + MTAG + ": unable to find attendee name text view!");
                        }

                        // Set the attendee amount information.
                        Locale loc = getResources().getConfiguration().locale;
                        String formattedAmount = FormatUtil.formatAmount(expRepAtt.amount, loc,
                                ((transCurCode != null) ? transCurCode : expRepEntDet.transactionCrnCode), true);
                        if (formattedAmount != null) {
                            txtView = (TextView) view.findViewById(R.id.exp_entries_attendee_row_amount);
                            Boolean isDisplayAtnAmt = Boolean.TRUE;
                            if (expType != null) {
                                isDisplayAtnAmt = expType.displayAtnAmts;
                            }
                            if (txtView != null) {
                                if (isDisplayAtnAmt == Boolean.TRUE) {
                                    txtView.setVisibility(View.VISIBLE);
                                    txtView.setText(formattedAmount);
                                } else {
                                    txtView.setVisibility(View.GONE);
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + "." + MTAG + ": couldn't find amount textview.");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + "." + MTAG + ": unable to format transaction amount of '"
                                    + expRepEntDet.transactionAmount + "'.");
                        }

                        // Set the attendee title, company and type information.
                        strBldr.setLength(0);
                        // Company
                        fieldValue = expRepAtt.getCompany();
                        if (fieldValue != null) {
                            fieldValue = fieldValue.trim();
                        }
                        if (fieldValue != null && fieldValue.length() > 0) {
                            strBldr.append(expRepAtt.getCompany());
                        }
                        // NOTE: Due to a bug in the MWS, the 'value' of the
                        // 'AtnTypeKey' field is not being properly set.
                        // For now (3/16/2011), we will just pull this
                        // information directly from the 'expRepAtt' object
                        // itself.
                        // if (!appendFormFieldValue(strBldr, expRepAtt,
                        // IExpenseReportFormField.ATTENDEE_TYPE_FIELD_ID)) {
                        // Log.w(Const.LOG_TAG, CLS_TAG +
                        // ".populateExpenseAttendees: attendee type not set!");
                        // }
                        // Attendee type.
                        fieldValue = expRepAtt.getTypeName();
                        if (fieldValue != null) {
                            fieldValue = fieldValue.trim();
                        }
                        if (fieldValue != null && fieldValue.length() > 0) {
                            if (strBldr.length() > 0) {
                                strBldr.append(" - ");
                            }
                            strBldr.append(fieldValue);
                        }
                        txtView = (TextView) view.findViewById(R.id.exp_entries_attendee_row_company_type);
                        if (txtView != null) {
                            txtView.setText(strBldr.toString());
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".populateExpenseAttendees: unable to find attendee company/type text view!");
                        }

                        // Set the attendee count
                        txtView = (TextView) view.findViewById(R.id.exp_entries_attendee_row_count);
                        Boolean isAllowEditAtnCount = Boolean.TRUE;
                        if (expType != null) {
                            isAllowEditAtnCount = expType.allowEditAtnCount;
                        }
                        if (txtView != null) {
                            if (isAllowEditAtnCount == Boolean.TRUE) {
                                txtView.setVisibility(View.VISIBLE);
                                String count = String.format("(%d)", (expRepAtt.instanceCount == null ? 1
                                        : expRepAtt.instanceCount));
                                txtView.setText(count);
                            } else {
                                txtView.setVisibility(View.GONE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + "." + MTAG
                                    + ": expType.allowEditAtnCount is null || couldn't find attCount textview.");
                        }
                        // Add a separate if needed.
                        if (attInd != 0) {
                            ViewUtil.addSeparatorView(this, viewGroup);
                        }

                        // If the report is editable, then add a click handler
                        // which will launch
                        // the editor.
                        if (isReportEditable()) {
                            // set up a short-press handler.
                            view.setFocusable(true);
                            view.setClickable(true);
                            final ExpenseReportAttendee selAtt = expRepAtt;
                            view.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (ConcurCore.isConnected()) {
                                        ExpenseReportAttendee defAtt = getConcurCore().getExpenseActiveCache()
                                                .getDefaultAttendee();
                                        if (defAtt != null && !selAtt.isEditable(defAtt)) {
                                            showDialog(Const.DIALOG_EXPENSE_ATTENDEE_NO_EDIT);
                                        } else if (selAtt.isVersionMismatch()) {
                                            showDialog(Const.DIALOG_EXPENSE_ATTENDEE_VERSION_MISMATCH);
                                        } else {
                                            Log.d(CLS_TAG, selAtt.toString());
                                            Intent intent = new Intent(ExpenseEntryAttendee.this,
                                                    ExpenseAttendeeEdit.class);
                                            // selectedAttendee.getFirstName();
                                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
                                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY,
                                                    expRepEntDet.reportEntryKey);
                                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
                                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_KEY, selAtt.atnKey);
                                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY, selAtt.getTypeKey());
                                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_AMOUNT, selAtt.amount);
                                            intent.putExtra(Const.EXTRA_EXPENSE_TRANSACTION_CURRENCY,
                                                    ((transCurCode != null) ? transCurCode
                                                            : expRepEntDet.transactionCrnCode));
                                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_COUNT, selAtt.instanceCount);
                                            // MOB-11721
                                            if (selAtt.getFirstName() != null) {
                                                intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_FIRST_NAME,
                                                        selAtt.getFirstName());
                                            }
                                            if (selAtt.getLastName() != null) {
                                                intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_LAST_NAME,
                                                        selAtt.getLastName());
                                            }
                                            if (selAtt.getTitle() != null) {
                                                intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_TITLE, selAtt.getTitle());
                                            }
                                            if (selAtt.getCompany() != null) {
                                                intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_COMPANY,
                                                        selAtt.getCompany());
                                            }
                                            startActivityForResult(intent, REQUEST_EDIT_ATTENDEE);
                                        }
                                    } else {
                                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                                    }
                                }
                            });

                            // Add the view to the map.
                            viewAttendeeMap.put(view, expRepAtt);
                            // Enable long-press.
                            registerForContextMenu(view);
                        }

                        // Add the newly built view to the group.
                        viewGroup.addView(view);

                    }
                } else {
                    // Hide the long-press message and attendees list.
                    setLongPressMessageVisibility(View.GONE);
                    setAttendeeListVisibility(View.GONE);
                    setNoAttendeesVisibility(View.VISIBLE);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseAttendees: unable to find view group to populate!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseAttendees: expense report entry detail is null!");
        }
    }

    private void setNoAttendeesVisibility(int visibility) {
        View view = findViewById(R.id.no_attendees);
        if (view != null) {
            view.setVisibility(visibility);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setNoAttendeesVisibility: unable to locate 'no attendees' view!");
        }
    }

    private void setAttendeeListVisibility(int visibility) {
        View view = findViewById(R.id.attendees);
        if (view != null) {
            view.setVisibility(visibility);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setAttendeeListVisibility: unable to locate attendee list group!");
        }
    }

    private void setLongPressMessageVisibility(int visibility) {
        TextView txtView = (TextView) findViewById(R.id.long_press_msg_view);
        if (txtView != null) {
            txtView.setVisibility(visibility);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".visibility: unable to locate long press text view!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getTransactionAmount(com.concur.mobile.data.expense.
     * ExpenseReportEntry)
     */
    @Override
    protected double getTransactionAmount(ExpenseReportEntry expRepEntry) {
        if (transAmt != null) {
            return transAmt;
        } else {
            return super.getTransactionAmount(expRepEntry);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getTransactionCurrencyCode(com.concur.mobile.data.expense.
     * ExpenseReportEntry)
     */
    @Override
    protected String getTransactionCurrencyCode(ExpenseReportEntry expRepEntry) {
        if (transCurCode != null) {
            return transCurCode;
        } else {
            return super.getTransactionCurrencyCode(expRepEntry);
        }
    }

    /**
     * Will initiate the process of adding a contact via selecting one from the device contact list.
     */
    private void addFromContacts() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, REQUEST_SELECT_CONTACT);
    }

    /**
     * Will initiate the process of adding a contact via creating one from scratch.
     */
    private void addFromCreate() {
        Intent intent = new Intent(ExpenseEntryAttendee.this, ExpenseAttendeeEdit.class);
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, expRepEntDet.reportEntryKey);
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
        ConcurCore ConcurCore = getConcurCore();
        ConcurCore.setAttendeeForm(null);
        startActivityForResult(intent, REQUEST_ADD_FROM_CREATE);
    }

    /**
     * Will register an instance of <code>SaveReportEntryReceiver</code> with the application context and set the
     * <code>saveReportEntryReceiver</code> attribute.
     */
    protected void registerSaveReportEntryReceiver() {
        if (saveReportEntryReceiver == null) {
            saveReportEntryReceiver = new SaveReportEntryReceiver(this);
            if (saveReportEntryFilter == null) {
                saveReportEntryFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_ENTRY_SAVE);
            }
            getApplicationContext().registerReceiver(saveReportEntryReceiver, saveReportEntryFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveReportEntryReceiver: saveReportEntryReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>SaveReportEntryReceiver</code> with the application context and set the
     * <code>saveReportEntryReceiver</code> to <code>null</code>.
     */
    protected void unregisterSaveReportEntryReceiver() {
        if (saveReportEntryReceiver != null) {
            getApplicationContext().unregisterReceiver(saveReportEntryReceiver);
            saveReportEntryReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveReportEntryReceiver: saveReportEntryReceiver is null!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#save()
     */
    @Override
    protected void save() {
        // TODO: Save the list of attendees on the expense item.
    }

    /**
     * Gets a list of attendee keys already associated with this expense entry.
     * 
     * @return the list of attendee keys associated with this expense.
     */
    private List<String> getExclusionAttendeeKeys() {
        List<String> excAtnKeys = new ArrayList<String>();
        // Add the default attendee key.
        // ConcurCore ConcurCore = getConcurCore();
        // IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
        // if( expRepCache != null ) {
        // ExpenseReportAttendee defaultAttendee =
        // expRepCache.getDefaultAttendee();
        // if( defaultAttendee != null ) {
        // if( defaultAttendee.atnKey != null && defaultAttendee.atnKey.length()
        // > 0 ) {
        // excAtnKeys.add(defaultAttendee.atnKey);
        // }
        // }
        // }
        // Add the current list of attendees.
        if (editedAttendees != null) {
            for (ExpenseReportAttendee attendee : editedAttendees) {
                excAtnKeys.add(attendee.atnKey);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExclusionAttendeeKeys: editedAttendees is null!");
        }
        return excAtnKeys;
    }

    /**
     * Gets a list of attendee external IDS.
     * 
     * @return returns a list of attendee external IDs.
     */
    private List<String> getExclusionExternalIDs() {
        List<String> excExtIds = new ArrayList<String>();
        if (editedAttendees != null) {
            for (ExpenseReportAttendee attendee : editedAttendees) {
                if (attendee.externalId != null && attendee.externalId.length() > 0) {
                    excExtIds.add(attendee.externalId);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExclusionExternalIDs: editedAttendees is null!");
        }
        return excExtIds;
    }

    /**
     * Will initiate the process of adding a contact via search.
     */
    private void addFromSearch() {
        Intent intent = new Intent(this, AttendeeSearch.class);
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, expRepEntDet.reportEntryKey);
        intent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
        intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_KEYS,
                getExclusionAttendeeKeys().toArray(new String[0]));
        intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_EXTERNAL_IDS,
                getExclusionExternalIDs().toArray(new String[0]));
        startActivityForResult(intent, REQUEST_ADD_FROM_SEARCH);
    }

    /**
     * An implementation of <code>DialogInterface.OnClickListener</code> for handling user selection of an "add attendee" option.
     * 
     * @author AndrewK
     */
    class AddAttendeeDialogListener implements DialogInterface.OnClickListener {

        /*
         * (non-Javadoc)
         * 
         * @see android.content.DialogInterface.OnClickListener#onClick(android.content .DialogInterface, int)
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            AddAttendeeAction addAttendeeAction = (AddAttendeeAction) addAttendeeActionAdapter.getItem(which);
            switch (addAttendeeAction) {
            case ADD_FROM_CONTACTS: {
                if (ConcurCore.isConnected()) {
                    addFromContacts();
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
                break;
            }
            case ADD_FROM_CREATE: {
                if (ConcurCore.isConnected()) {
                    addFromCreate();
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
                break;
            }
            case ADD_FROM_SEARCH: {
                if (ConcurCore.isConnected()) {
                    addFromSearch();
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
                break;
            }
            }
            removeDialog(Const.DIALOG_EXPENSE_ADD_ATTENDEE);
        }

    }

    /**
     * An extension of <code>BaseAdapter</code> for selecting a way to add an attendee.
     * 
     * @author AndrewK
     */
    class AddAttendeeOptionListAdapter extends BaseAdapter {

        /**
         * Contains a list of available options.
         */
        ArrayList<AddAttendeeAction> options = new ArrayList<AddAttendeeAction>();

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            return options.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            return options.get(position);
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;

            LayoutInflater inflater = LayoutInflater.from(ExpenseEntryAttendee.this);

            int textResId = 0;
            switch (options.get(position)) {
            case ADD_FROM_CONTACTS:
                textResId = R.string.add_attendee_select_contact;
                break;
            case ADD_FROM_CREATE:
                textResId = R.string.add_attendee_create_contact;
                break;
            case ADD_FROM_SEARCH:
                textResId = R.string.add_attendee_from_search;
                break;
            }
            view = inflater.inflate(R.layout.expense_receipt_option, null);
            // Set the text.
            if (textResId != 0) {
                TextView txtView = (TextView) view.findViewById(R.id.text);
                if (txtView != null) {
                    txtView.setText(ExpenseEntryAttendee.this.getText(textResId));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: can't locate text view!");
                }
            }
            return view;
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of saving report entry.
     */
    static class SaveReportEntryReceiver extends BaseBroadcastReceiver<ExpenseEntryAttendee, SaveReportEntryRequest> {

        /**
         * Constructs an instance of <code>SaveReportEntryReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        SaveReportEntryReceiver(ExpenseEntryAttendee activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(ExpenseEntryAttendee activity) {
            activity.saveReportEntryRequest = null;

        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(activity.getAttendeeActionProgressDialog());
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(activity.getAttendeeActionFailureDialog());
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // regenerate the attendee list.
            activity.regenerateAttendeeListView();
            // Pass the result back to the calling activity.
            activity.setResult(Activity.RESULT_OK);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(SaveReportEntryRequest request) {
            activity.saveReportEntryRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterSaveReportEntryReceiver();
        }

    }

    class NoShowFormFieldListener extends FormFieldViewListener {

        private final Activity activity;

        public NoShowFormFieldListener(BaseActivity activity) {
            super(activity);
            this.activity = activity;
        }

        @Override
        public void valueChanged(FormFieldView frmFldView) {
            ExpenseReportFormField frmFld = frmFldView.getFormField();
            if (NOSHOW_FIELD_ID.equals(frmFld.getId())) {
                // Commit the change (not truly necessary, but good for
                // completeness)
                frmFldView.commit();

                // Update the entry
                Integer count = Parse.safeParseInteger(frmFld.getValue());
                expRepEntDet.noShowCount = (count == null) ? 0 : count;
                expRepEntDet.noShowCountChanged = true;
                expRepEntDet.divideAmountAmongAttendees(transAmt, transCurCode, editedAttendees);

                // Update the display
                regenerateAttendeeListView();
                double noShowAmount = expRepEntDet.getNoShowAmount(transAmt, editedAttendees);
                Locale loc = getResources().getConfiguration().locale;
                String noShowAmountString = FormatUtil.formatAmount(noShowAmount, loc, transCurCode, true);

                FormFieldView ffv = findFormFieldViewById(NOSHOW_FIELD_ID);
                if (ffv != null) {
                    ffv.setTextViewText(ffv.view, R.id.field_name, ffv.buildLabel(Format.localizeText(activity,
                            R.string.attendee_no_show_label, new Object[] { noShowAmountString })));
                }

            }
        }

    }
}

/**
 * A non-public class used to collect attendee information from a picked contact.
 */
class AttendeeContactInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    String firstName;

    String lastName;

    String company;

    String title;

    String displayName;

}
