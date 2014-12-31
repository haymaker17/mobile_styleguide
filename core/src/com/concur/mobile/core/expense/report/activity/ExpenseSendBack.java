/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.report.approval.activity.Approval;
import com.concur.mobile.core.expense.report.approval.service.RejectReportRequest;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;

/**
 * Provides an activity to send back an expense report with optional comment.
 * 
 * @author AndrewK
 */
public class ExpenseSendBack extends AbstractExpenseActivity {

    private static final String CLS_TAG = ExpenseSendBack.class.getSimpleName();

    /**
     * Contains the key used to look up the report send back receiver in a non-configuration data hashmap.
     */
    private static final String REPORT_SEND_BACK_RECEIVER_KEY = "report.send.back.receiver";

    /**
     * Contains the receiver to handle the report send back result.
     */
    private ReportSendBackReceiver reportSendBackReceiver;

    /**
     * Contains the filter used to register the report send back receiver.
     */
    private IntentFilter reportSendBackFilter;

    /**
     * Contains an outstanding request to send a report back.
     */
    private RejectReportRequest reportSendBackRequest;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reportSendBackFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_SEND_BACK);

        // Restore any receivers.
        restoreReceivers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case Const.DIALOG_EXPENSE_REPORT_SEND_BACK: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_expense_report_send_back));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (reportSendBackRequest != null) {
                        // Cancel the request.
                        reportSendBackRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: reportSendBackRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_REPORT_SEND_BACK_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getText(R.string.dlg_report_send_back_failed_title));
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        default: {
            dialog = super.onCreateDialog(id);
        }
        }
        return dialog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case Const.DIALOG_EXPENSE_REPORT_SEND_BACK_FAILED: {
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
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();

        // Save 'ReportSendBackReceiver'.
        if (reportSendBackReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate' method.
            reportSendBackReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(REPORT_SEND_BACK_RECEIVER_KEY, reportSendBackReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        super.restoreReceivers();
        // Restore 'ReportSendBackReceiver'.
        if (retainer.contains(REPORT_SEND_BACK_RECEIVER_KEY)) {
            reportSendBackReceiver = (ReportSendBackReceiver) retainer.get(REPORT_SEND_BACK_RECEIVER_KEY);
            if (reportSendBackReceiver != null) {
                // Set the activity on the receiver.
                reportSendBackReceiver.setActivity(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".restoreReceivers: retainer contains null reference for report send back receiver!");
            }
        }
    }

    /**
     * Will create and register with the application context an instance of 'ReportSendBackReceiver' and update the
     * 'reportSendBAckReceiver' attribute.
     */
    protected void registerReportSendBackReceiver() {
        reportSendBackReceiver = new ReportSendBackReceiver(this);
        getApplicationContext().registerReceiver(reportSendBackReceiver, reportSendBackFilter);
    }

    /**
     * Will unregister with the application context the current instance of 'ReportSendBackReceiver' and set the
     * 'reportSendBackReceiver' attribute to 'null'.
     */
    protected void unregisterReportSendBackReceiver() {
        getApplicationContext().unregisterReceiver(reportSendBackReceiver);
        reportSendBackReceiver = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#buildView()
     */
    @Override
    protected void buildView() {

        // Instruct the window manager to only show the soft keyboard when the end-user clicks on it.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.expense_send_back);

        // Configure the screen header.
        configureScreenHeader(expRep);

        // Configure the screen footer.
        configureScreenFooter();

        // Set the expense title header information.
        populateReportHeaderInfo(expRep);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.activity.expense.AbstractExpenseActivity#populateExpenseHeaderNavBarInfo(com.concur.mobile.data.expense
     * .ExpenseReport)
     */
    @Override
    protected void configureScreenHeader(ExpenseReport expRep) {
        super.configureScreenHeader(expRep);

        // Hide the 'Approve' button.
        View view = findViewById(R.id.reject_button);
        if (view != null) {
            view.setVisibility(View.INVISIBLE);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseHeaderNavBarInfo: can't find 'send back' button!");
        }

    }

    @Override
    protected void configureScreenFooter() {
        Button rejectButton = (Button) findViewById(R.id.reject_button);
        if (rejectButton != null) {
            rejectButton.setVisibility(View.INVISIBLE);
        }

        // Change text label on 'Approve' button to 'Commit' and set up a new
        // OnClick listener.
        Button button = (Button) findViewById(R.id.approve_button);
        if (button != null) {
            button.setVisibility(View.VISIBLE);
            button.setText(getText(R.string.send_back));
            final ExpenseReport finExpRep = expRep;
            button.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    // Check for connectivity, if none, then display dialog and return.
                    if (!ConcurCore.isConnected()) {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                        return;
                    }
                    // Send the request off to reject the expense report.
                    ConcurCore app = (ConcurCore) getApplication();
                    TextView txtView = (TextView) findViewById(R.id.send_back_comment);
                    if (txtView != null) {

                        String commentStr = null;
                        CharSequence cs = txtView.getText();
                        if (cs != null) {
                            commentStr = cs.toString().trim();
                        }
                        if (commentStr != null && commentStr.length() > 0) {
                            // Register the receiver to handle the result.
                            registerReportSendBackReceiver();
                            // Construct the request.
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app
                                    .getApplicationContext());
                            reportSendBackRequest = app.getService().sendReportReject(finExpRep, commentStr,
                                    prefs.getString(Const.PREF_USER_ID, null));
                            if (reportSendBackRequest != null) {
                                reportSendBackReceiver.setRequest(reportSendBackRequest);
                                // Display the progress dialog.
                                showDialog(Const.DIALOG_EXPENSE_REPORT_SEND_BACK);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to create report send back request!");
                                // Unregister the receiver.
                                unregisterReportSendBackReceiver();
                            }
                        } else {
                            // Prompt to provide a comment.
                            showDialog(Const.DIALOG_EXPENSE_REJECT_COMMENT_PROMPT);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".populateExpenseHeaderNavBarInfo.run: can't find comment text view!");
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseHeaderNavBarInfo: can't find 'approve' button!");
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#isApproveReportEnabled()
     */
    @Override
    protected boolean isApproveReportEnabled() {
        // The report send back activity actually uses the approve button; hence this method must return true!
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#getBroadcastReceiverIntentFilter()
     */
    @Override
    protected IntentFilter getBroadcastReceiverIntentFilter() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#getHeaderNavBarTitleResourceId()
     */
    @Override
    protected int getHeaderNavBarTitleResourceId() {
        return R.string.send_back;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#isDetailReportRequired()
     */
    @Override
    protected boolean isDetailReportRequired() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#shouldReceiveDataEvents()
     */
    @Override
    protected boolean shouldReceiveDataEvents() {
        return false;
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle the results of sending a report back.
     * 
     * @author AndrewK
     */
    static class ReportSendBackReceiver extends BroadcastReceiver {

        final String CLS_TAG = ExpenseSendBack.CLS_TAG + "." + ReportSendBackReceiver.class.getSimpleName();

        // A reference to the activity.
        private ExpenseSendBack activity;

        // A reference to the report send back request.
        private RejectReportRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ReportSendBackReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReportSendBackReceiver(ExpenseSendBack activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseSendBack activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.reportSendBackRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the report send back request object associated with this broadcast receiver.
         * 
         * @param request
         *            the report send back object associated with this broadcast receiver.
         */
        void setRequest(RejectReportRequest request) {
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
                activity.unregisterReportSendBackReceiver();

                // Handle the reponse.
                int requestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (requestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                    int httpStatus = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, 1);
                    if (httpStatus == HttpStatus.SC_OK) {
                        String mwsStatus = intent.getStringExtra(Const.REPLY_STATUS);
                        if (mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                            try {
                                // Dismiss the expense send back progress dialog.
                                activity.dismissDialog(Const.DIALOG_EXPENSE_REPORT_SEND_BACK);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }

                            // Flurry Notification
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_REPORTS,
                                    Flurry.EVENT_NAME_REJECT_REPORT);

                            // Punt any client-side detailed report information.
                            ConcurCore app = (ConcurCore) activity.getApplication();
                            app.getExpenseApprovalCache().deleteDetailReport(activity.expRep.reportKey);

                            // Launch the 'ExpenseApproval' activity with the flag
                            // 'Intent.FLAG_ACTIVITY_CLEAR_TOP' which will unwind the activity
                            // stack and ensure 'ExpenseApproval' is on top of the stack.
                            Intent approvalsIntent = new Intent(activity, Approval.class);
                            ConcurCore ConcurCore = (ConcurCore) activity.getApplication();
                            IExpenseReportCache expRepCache = ConcurCore.getExpenseApprovalCache();
                            expRepCache.setShouldFetchReportList();
                            approvalsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            approvalsIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, activity.reportKeySource);
                            boolean approvalListUpdated = intent.getBooleanExtra(
                                    Const.EXTRA_EXPENSE_REPORT_TO_APPROVE_LIST_PENDING, false);
                            approvalsIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_TO_APPROVE_LIST_PENDING,
                                    approvalListUpdated);
                            activity.startActivity(approvalsIntent);
                        } else {
                            activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                            Log.d(Const.LOG_TAG, CLS_TAG + ".onReceive: MWS error '"
                                    + activity.actionStatusErrorMessage + "'.");
                            activity.showDialog(Const.DIALOG_EXPENSE_REPORT_SEND_BACK_FAILED);
                            try {
                                // Dismiss the expense send back progress dialog.
                                activity.dismissDialog(Const.DIALOG_EXPENSE_REPORT_SEND_BACK);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    } else {
                        activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                        Log.d(Const.LOG_TAG, CLS_TAG + ".onReceive: non HTTP status: '" + activity.lastHttpErrorMessage
                                + "'.");
                        activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                        try {
                            // Dismiss the expense send back progress dialog.
                            activity.dismissDialog(Const.DIALOG_EXPENSE_REPORT_SEND_BACK);
                        } catch (IllegalArgumentException ilaExc) {
                            Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                        }
                    }
                } else {
                    if (request != null && !request.isCanceled()) {
                        Log.d(Const.LOG_TAG,
                                CLS_TAG + ".onReceive: request could not be completed due to: "
                                        + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                        try {
                            // Dismiss the expense send back progress dialog.
                            activity.dismissDialog(Const.DIALOG_EXPENSE_REPORT_SEND_BACK);
                        } catch (IllegalArgumentException ilaExc) {
                            Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                        }
                    }
                }

                // Clear the request reference.
                activity.reportSendBackRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }
}
