package com.concur.mobile.core.expense.report.activity;

import java.util.List;

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
import android.view.ViewGroup;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.service.AttendeeFormRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.FormFieldView;

/**
 * Displays available attendee information when an attendee is clicked form Advanced Attendee Search.
 * 
 * @see com.concur.mobile.core.expense.report.activity.AttendeeSearch.AdvancedSearchResultClickListener#onItemClick(android.widget.AdapterView,
 *      android.view.View, int, long)
 * 
 * @author sunill
 * 
 */
public class ExpenseAttendeePreview extends AbstractExpenseActivity {

    private static final String CLS_TAG = ExpenseAttendeePreview.class.getSimpleName();

    /**
     * Contains the attendee form receiver.
     */
    private AttendeeFormReceiver attendeeFormReceiver;
    /**
     * Contains an outstanding attendee form request.
     */
    private AttendeeFormRequest attendeeFormRequest;

    /**
     * Contains the filter used to register the attendee form receiver.
     */
    private IntentFilter attendeeFormFilter;

    private ExpenseReportAttendee curAttendeeTypeItem;

    private String atnTypeKey;

    private ExpenseReportAttendee atnForm;
    private static final String ATTENDEE_FORM_RECEIVER_KEY = "attendee.form.receiver";

    @Override
    protected boolean isDetailReportRequired() {
        return true;
    }

    @Override
    protected int getHeaderNavBarTitleResourceId() {
        return R.string.attendee;
    }

    @Override
    protected void buildView() {
        // Set the layout.
        setContentView(R.layout.expense_attendee_preview);
        // Set the title.
        getSupportActionBar().setTitle(R.string.attendee);
        // Initialize all fields/variables
        // Set the attendee type and attendee keys.
        if (lastSavedInstanceState != null) {
            initState(lastSavedInstanceState);
        } else {
            initState(null);
        }
    }

    @Override
    protected IntentFilter getBroadcastReceiverIntentFilter() {
        return null;
    }

    @Override
    protected boolean shouldReceiveDataEvents() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildView();
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

                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();

                    if (attendeeFormRequest != null) {
                        attendeeFormRequest.cancel();
                    }
                    if (atnForm == null) {
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_FAILED: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dlg_expense_attendee_retrieve_form_failed_title);
            builder.setCancelable(true);
            builder.setMessage("");
            builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    // Dismiss this dialog
                    dialog.dismiss();
                    setResult(RESULT_CANCELED);
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

    @Override
    protected void onPause() {
        super.onPause();
        // Save 'AttendeeFormReceiver'.
        if (attendeeFormReceiver != null) {
            // Clear the activity reference, it will be set in the
            // 'onCreate' method.
            attendeeFormReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(ATTENDEE_FORM_RECEIVER_KEY, attendeeFormReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        super.restoreReceivers();
        if (retainer != null) {
            // Restore an attendee form receiver.
            if (retainer.contains(ATTENDEE_FORM_RECEIVER_KEY)) {
                attendeeFormReceiver = (AttendeeFormReceiver) retainer.get(ATTENDEE_FORM_RECEIVER_KEY);
                // Reset the activity reference.
                attendeeFormReceiver.setActivity(this);
            }
        }
    }

    public void initState(Bundle bundle) {
        if (bundle == null) {
            ConcurCore concurCore = getConcurCore();
            List<ExpenseReportAttendee> selectedAttendees = concurCore.getSelectedAttendees();
            if (selectedAttendees != null) {
                curAttendeeTypeItem = selectedAttendees.get(0);
                sendAttendeeFormRequest(curAttendeeTypeItem.atnTypeKey, curAttendeeTypeItem.atnKey);
            } else {
                showDialog(Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_FAILED);
            }
        } else {
            if (bundle.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY)) {
                atnTypeKey = bundle.getString(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY);
                ConcurCore ConcurCore = getConcurCore();
                ExpenseReportAttendee attendeeForm = ConcurCore.getAttendeeForm();
                if (attendeeForm != null) {
                    if (atnTypeKey != null && atnTypeKey.length() > 0) {
                        generateScreen(atnTypeKey, attendeeForm);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".initState: bundle is missing attendee type key!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initState: downloaded attendee form is null!");
                }
            }
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
                    + ".sendAttendeeFormRequest: unable to create request to retrieve attendee form information!");
            unregisterAttendeeFormReceiver();
        } else {
            // Set the request object on the receiver.
            attendeeFormReceiver.setServiceRequest(attendeeFormRequest);
            // Show the attendee form progress dialog.
            showDialog(Const.DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_PROGRESS);
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
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of retrieving a form for
     * attendee editing.
     */
    static class AttendeeFormReceiver extends BaseBroadcastReceiver<ExpenseAttendeePreview, AttendeeFormRequest> {

        private static final String CLS_TAG = ExpenseAttendeePreview.CLS_TAG + "."
                + AttendeeFormReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>AttendeeFormReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AttendeeFormReceiver(ExpenseAttendeePreview activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(ExpenseAttendeePreview activity) {
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
                    activity.generateScreen(atnTypeKey, attendeeForm);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: intent is missing attendee type key!");
                }
            } else {
                handleFailure(context, intent);
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

    /**
     * call generate screen from onrecieve method.
     * 
     * @param atnTypeKey
     * @param attendeeForm
     */
    private void generateScreen(String atnTypeKey, ExpenseReportAttendee attendeeForm) {
        if (atnTypeKey != null) {
            atnTypeKey = atnTypeKey.trim();
            if (atnTypeKey.length() > 0) {
                this.atnTypeKey = atnTypeKey;
                Log.d(Const.LOG_TAG, CLS_TAG + ".generateScreen: attendee Type Key after form generate is : "
                        + this.atnTypeKey);
                this.atnForm = attendeeForm;
                loadAttendeeFieldViews();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".callRegenerateScreen: attendee type key is empty!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".callRegenerateScreen: intent has null attendee type key!");
        }
    }

    /**
     * Will layout the current set of attendee views.
     */
    protected void loadAttendeeFieldViews() {

        if (atnForm != null) {
            ViewGroup fldGroup = (ViewGroup) findViewById(R.id.attendee_search_field_list);
            if (fldGroup != null) {
                // First, punt all the children within the group.
                if (fldGroup.getChildCount() > 0) {
                    fldGroup.removeAllViews();
                }
                // Determine whether we have a cached list of fields.
                List<ExpenseReportFormField> frmFields = atnForm.getFormFields();
                for (ExpenseReportFormField expenseReportFormField : frmFields) {
                    expenseReportFormField.setAccessType(AccessType.RO);
                }
                List<FormFieldView> frmFldViews;
                frmFldViews = FormUtil.populateViewWithFormFields(this, fldGroup, frmFields, null, frmFldViewListener);
                if (frmFldViews != null && !frmFldViews.isEmpty()) {
                    ViewUtil.addSeparatorView(this, fldGroup);
                }
                // Set the current list of form field view on the listener.
                frmFldViewListener.setFormFieldViews(frmFldViews);
                // Clear any current form field view.
                frmFldViewListener.clearCurrentFormFieldView();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".loadAttendeeFieldViews: unable to locate attendee field list group!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".loadAttendeeFieldViews: atnForm is null!");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save attendee type key.
        if (atnTypeKey != null) {
            outState.putString(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY, atnTypeKey);
        }
    }
}
