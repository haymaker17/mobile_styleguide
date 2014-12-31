package com.concur.mobile.core.expense.report.activity;

import java.util.ArrayList;
import java.util.List;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.service.AttendeeSaveReply;
import com.concur.mobile.core.expense.report.service.AttendeeSaveRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;

public class ExpenseDuplicateAttendee extends AbstractExpenseActivity implements OnCheckedChangeListener {

    private static final String CLS_TAG = ExpenseDuplicateAttendee.class.getSimpleName();

    private static final String NEW_ATTENDEE_TAG = "new";

    private static final String ATTENDEE_SAVE_RECEIVER_KEY = "attendee.save.receiver";

    private List<RadioButton> radioButtons = new ArrayList<RadioButton>();

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

    private String oldAtnKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            oldAtnKey = extras.getString(Const.EXTRA_EXPENSE_ATTENDEE_KEY);
        } else {
            oldAtnKey = null;
        }

        // Restore any receivers.
        restoreReceivers();

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save 'AttendeeSaveReceiver'.
        if (attendeeSaveReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate' method.
            attendeeSaveReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(ATTENDEE_SAVE_RECEIVER_KEY, attendeeSaveReceiver);
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
            // Restore an attendee save receiver.
            if (retainer.contains(ATTENDEE_SAVE_RECEIVER_KEY)) {
                attendeeSaveReceiver = (AttendeeSaveReceiver) retainer.get(ATTENDEE_SAVE_RECEIVER_KEY);
                // Reset the activity reference.
                attendeeSaveReceiver.setActivity(this);
            }
        }
    }

    @Override
    protected void buildView() {
        setContentView(R.layout.expense_duplicate_attendee);

        // Configure the screen header.
        configureScreenHeader(expRep);

        // Configure the screen footer.
        configureScreenFooter();

        // Intent intent = getIntent();
        // String expRepEntryKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
        // ExpenseReportEntry expRepEntry = expRepCache.getReportEntry(expRep, expRepEntryKey);

        // Get the service reply and create the attendee views
        ConcurCore app = (ConcurCore) getApplication();
        AttendeeSaveReply reply = app.getAttendeeSaveResults();
        RadioGroup attendeeGroup = (RadioGroup) findViewById(R.id.attendees);

        // First, the new attendee
        // Make it selected
        // Set it's tag to 'new'. The server returns the atnKey of the first dupe as the atnKey
        // of the new attendee. We don't want that.
        View newAtt = populateAttendeeView(reply.attendee);
        final RadioButton rb = (RadioButton) newAtt.findViewById(R.id.attendee_select);
        rb.setChecked(true);
        rb.setTag(NEW_ATTENDEE_TAG);
        attendeeGroup.addView(newAtt, 1);

        // Then the duplicates
        for (ExpenseReportAttendee dupe : reply.duplicateAttendees) {
            attendeeGroup.addView(populateAttendeeView(dupe));
        }

    }

    protected View populateAttendeeView(ExpenseReportAttendee attendee) {

        LayoutInflater inflater = LayoutInflater.from(this);
        StringBuilder sb = new StringBuilder();
        View view = inflater.inflate(R.layout.expense_duplicate_attendee_row, null);

        // Set the attendee name information.

        // Last name.
        String fieldValue = attendee.getLastName();
        if (fieldValue != null) {
            fieldValue = fieldValue.trim();
        }
        if (fieldValue != null && fieldValue.length() > 0) {
            sb.append(fieldValue);
        }
        // First name.
        fieldValue = attendee.getFirstName();
        if (fieldValue != null) {
            fieldValue = fieldValue.trim();
        }
        if (fieldValue != null && fieldValue.length() > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(fieldValue);
        }
        TextView txtView = (TextView) view.findViewById(R.id.exp_entries_attendee_row_name);
        if (txtView != null) {
            txtView.setText(sb.toString());
        }

        // Set the attendee title, company and type information.
        sb.setLength(0);
        // Company
        fieldValue = attendee.getCompany();
        if (fieldValue != null) {
            fieldValue = fieldValue.trim();
        }
        if (fieldValue != null && fieldValue.length() > 0) {
            sb.append(fieldValue);
        }

        fieldValue = attendee.getTypeName();
        if (fieldValue != null) {
            fieldValue = fieldValue.trim();
        }
        if (fieldValue != null && fieldValue.length() > 0) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(fieldValue);
        }
        txtView = (TextView) view.findViewById(R.id.exp_entries_attendee_row_company_type);
        if (txtView != null) {
            txtView.setText(sb.toString());
        }

        // Hook up the listener since a RadioGroup will only look at immediate children (stupid design)
        RadioButton rb = (RadioButton) view.findViewById(R.id.attendee_select);
        rb.setOnCheckedChangeListener(this);

        // Set the tag
        rb.setTag(attendee.atnKey);

        radioButtons.add(rb);

        return view;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Iterate all the radio buttons and turn them off if not this one
        if (isChecked) {
            for (RadioButton rb : radioButtons) {
                if (rb != buttonView) {
                    rb.setChecked(false);
                }
            }
        }
    }

    @Override
    protected void configureScreenFooter() {
        // Hide the reject button
        ((Button) findViewById(R.id.reject_button)).setVisibility(View.INVISIBLE);

        // Use the approve button as the add attendee button
        Button addAttendee = (Button) findViewById(R.id.approve_button);
        if (addAttendee != null) {
            addAttendee.setText(R.string.duplicate_attendee_add_attendee);
            addAttendee.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // Check for connectivity, if none, then display dialog and return.
                    if (!ConcurCore.isConnected()) {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                        return;
                    }

                    // Get the selected atnKey
                    String atnKey = null;
                    for (RadioButton rb : radioButtons) {
                        if (rb.isChecked()) {
                            atnKey = (String) rb.getTag();
                        }
                    }

                    if (atnKey == null) {
                        // Something bad. Should not happen. Get out.
                        return;
                    }

                    if (atnKey != NEW_ATTENDEE_TAG) {
                        // Pass back the selected attendee
                        Intent result = new Intent();
                        result.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_KEY, atnKey);
                        result.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_OLD_KEY_FROM_DUP, oldAtnKey);
                        setResult(RESULT_OK, result);
                        finish();
                    } else {
                        // Force the save of the new attendee and eventually pass back its atnKey
                        sendAttendeeSaveRequest();
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populdateExpenseHeaderNavBarInfo: can't find approve report button view!");
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case Const.DIALOG_EXPENSE_ATTENDEE_SAVE_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_expense_attendee_save_progress_message));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            // Register a cancel listener to cancel the request.
            progDlg.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (attendeeSaveRequest != null) {
                        // Cancel the request, this will result in the 'AttendeeSaveReceiver.onReceiver' being
                        // invoked and handled as a cancellation.
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

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case Const.DIALOG_EXPENSE_ATTENDEE_SAVE_FAILED: {
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

    @Override
    protected boolean isDetailReportRequired() {
        return true;
    }

    @Override
    protected int getHeaderNavBarTitleResourceId() {
        return R.string.duplicate_attendee_title;
    }

    @Override
    protected IntentFilter getBroadcastReceiverIntentFilter() {
        return null;
    }

    @Override
    protected boolean shouldReceiveDataEvents() {
        return false;
    }

    private void sendAttendeeSaveRequest() {
        ConcurCore app = (ConcurCore) getApplication();
        AttendeeSaveReply reply = app.getAttendeeSaveResults();

        // Do the full attendee save
        ConcurService concurService = getConcurService();
        registerAttendeeSaveReceiver();
        attendeeSaveRequest = concurService.sendAttendeeSaveRequest(getUserId(), reply.attendee, true);
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

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of saving an attendee.
     */
    static class AttendeeSaveReceiver extends BaseBroadcastReceiver<ExpenseDuplicateAttendee, AttendeeSaveRequest> {

        // private static final String CLS_TAG = ExpenseAttendeeEdit.CLS_TAG + "." + AttendeeSaveReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>AttendeeSaveReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AttendeeSaveReceiver(ExpenseDuplicateAttendee activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#clearActivityServiceRequest(com.concur.mobile.activity
         * .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(ExpenseDuplicateAttendee activity) {
            activity.attendeeSaveRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_ATTENDEE_SAVE_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_ATTENDEE_SAVE_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            ConcurCore app = (ConcurCore) activity.getApplication();
            AttendeeSaveReply reply = app.getAttendeeSaveResults();
            if (reply != null) { // paranoia
                Intent result = new Intent();
                result.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_KEY, reply.attendee.atnKey);
                result.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_OLD_KEY_FROM_DUP, activity.oldAtnKey);
                activity.setResult(RESULT_OK, result);
                activity.finish();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#setActivityServiceRequest(com.concur.mobile.activity.
         * BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(AttendeeSaveRequest request) {
            activity.attendeeSaveRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterAttendeeSaveReceiver();
        }

    }
}
