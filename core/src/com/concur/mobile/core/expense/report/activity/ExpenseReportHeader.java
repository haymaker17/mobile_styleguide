/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

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
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportComment;
import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportDisbursement;
import com.concur.mobile.core.expense.report.data.ExpenseReportException;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.service.ConditionalFieldAction;
import com.concur.mobile.core.expense.report.service.ReportFormRequest;
import com.concur.mobile.core.expense.report.service.SaveReportRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormUtil;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldViewListener;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.platform.util.Format;

/**
 * Provides an activity to display expense report header.
 * 
 * @author AndrewK
 */
public class ExpenseReportHeader extends AbstractExpenseActivity {

    public static final String CLS_TAG = ExpenseReportHeader.class.getSimpleName();

    // Contains a list of "hard-stop" form field ID's that must have values.
    public static String[] HARD_STOP_FIELD_IDS = { "Name", "Purpose" };

    private static final String SAVE_REPORT_RECEIVER_KEY = "save.report.receiver";

    private static final String REPORT_FORM_RECEIVER_KEY = "report.form.receiver";

    private static final String EDITED_REPORT_DETAIL_BUNDLE_KEY = "edited.report.detail.key";

    private static final int DIALOG_ERROR_PARSING_REPORT_FROM_FIELDS = 0;

    protected static final String POLICY_ID = "PolKey";

    // Contains a reference to an outstanding request to save a report.
    private SaveReportRequest saveReportRequest;

    // Contains a reference to the receiver for handling a save report response.
    private SaveReportReceiver saveReportReceiver;

    // Contains the filter for registering the SaveReportReceiver.
    private IntentFilter saveReportFilter;

    // Contains a reference to an outstanding request to get a report's form
    // fields.
    private ReportFormRequest reportFormRequest;

    // Contains a reference to the receiver for handling a report form response.
    private ReportFormReceiver reportFormReceiver;

    // Contains the filter for registering the ReportFormReceiver.
    private IntentFilter reportFormFilter;

    /**
     * Contains a reference to an instance of <code>ExpenseReportDetail</code> that was loaded as a result of changing the policy
     * type for an entry prior to a save occurring.
     */
    protected ExpenseReportDetail editedExpRep;

    // A flag indicating whether the activity should finish after a successful
    // save
    protected boolean finishOnSave;

    // A flag indicating whether we should re-fetch the Report form fields.
    // Should only be used 'reportKeySource' is Const.EXPENSE_REPORT_SOURCE_NEW.
    protected boolean fetchFormFields;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getHeaderNavBarTitleResourceId()
     */
    @Override
    protected int getHeaderNavBarTitleResourceId() {
        if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW) {
            return R.string.new_report;
        } else {
            return R.string.summary;
        }
    }

    /**
     * Will create and register with the application context an instance of 'SaveReportReceiver' and update the
     * 'saveReportReceiver' attribute.
     */
    protected void registerSaveReportReceiver() {
        if (saveReportReceiver == null) {
            saveReportReceiver = new SaveReportReceiver(this);
            getApplicationContext().registerReceiver(saveReportReceiver, saveReportFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveReportReceiver: saveReportReceiver is not null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of 'SaveReportReceiver' and set the 'saveReportReceiver'
     * attribute to 'null'.
     */
    protected void unregisterSaveReportReceiver() {
        if (saveReportReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(saveReportReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveReportReceiver: receiver not registered!", ilaExc);
            }
            saveReportReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveReportReceiver: saveReportReceiver is null!");
        }
    }

    /**
     * Will create and register with the application context an instance of 'ReportFormReceiver' attribute.
     */
    protected void registerGetReportFormReceiver() {
        if (reportFormReceiver == null) {
            reportFormReceiver = new ReportFormReceiver(this);
            getApplicationContext().registerReceiver(reportFormReceiver, reportFormFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerLocationCheckInReceiver: locationCheckInFilter is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>ReportFormReceiver</code> with the application context and set the
     * <code>reportFormReceiver</code> to <code>null</code>.
     */
    protected void unregisterGetReportFormReceiver() {
        if (reportFormReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(reportFormReceiver);
            } catch (IllegalArgumentException e) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterGetReportFormReceiver: receiver not registered!", e);
            }
            reportFormReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterGetReportFormReceiver: reportFormReceiver is null!");
        }
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

    /**
     * Populate any report-level exception information.
     * 
     * @param expRep
     *            the expense report.
     */
    private void populateReportExceptionInfo(ExpenseReport expRep) {
        try {
            ExpenseReportDetail expRepDet = (ExpenseReportDetail) expRep;
            ViewGroup excGroup = (ViewGroup) findViewById(R.id.report_summary_exception_list);
            if (expRepDet != null && expRepDet.getExceptions() != null && expRepDet.getExceptions().size() > 0) {
                ArrayList<ExpenseReportException> excList = expRepDet.getExceptions();
                populateExceptionViewGroup(excList, excGroup);
            } else {
                // Hide the exceptions view.
                excGroup = (ViewGroup) findViewById(R.id.header_exception_group);
                if (excGroup != null) {
                    excGroup.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportExceptionInfo: unable to locate exception group!");
                }
            }
        } catch (ClassCastException ccExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportExceptionInfo: " + ccExc.getMessage(), ccExc);
        }
    }

    /**
     * Will return the instance of <code>ExpenseReportDetail</code> that reflects either the current one being edited (based on
     * policy type change) or the current one.
     * 
     * @return the instance of ExpenseReportEntryDetail in use.
     */
    protected ExpenseReportDetail getExpenseReportDetail() {
        return ((editedExpRep != null) ? editedExpRep : (ExpenseReportDetail) expRep);
    }

    // Populate report summary header information.
    protected void populateReportSummaryHeaderInfo(ExpenseReport expRep) {

        ExpenseReportDetail expRepDet = (ExpenseReportDetail) expRep;

        // If this is a new Report and the ExpenseReport is null,
        // then we need to get the Report Form fields.
        if ((reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW && expRepDet == null)
                || (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW && fetchFormFields)) {

            fetchFormFields = false; // reset back to false.

            // Initially hide the group.
            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.linear_layout);
            viewGroup.setVisibility(View.INVISIBLE);

            // Get the polKey used to fetch the Report form fields.
            String polKey = null;
            if (expRepDet != null) {
                ExpenseReportFormField formField = expRepDet.getFormField(POLICY_ID);
                if (formField != null) {
                    polKey = formField.getLiKey();
                }
            }

            // Invoke MWS to get list of Report form fields.
            sendReportFormRequest(polKey);

        } else {
            // Make sure the Summary Header is visible in case it was
            // set to invisible if this is a new report.
            ViewGroup scrollView = (ViewGroup) findViewById(R.id.linear_layout);
            if (scrollView != null && scrollView.getVisibility() == View.INVISIBLE) {
                scrollView.setVisibility(View.VISIBLE);
            }

            // Populate field with some of the custom field information.
            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.report_summary_header_list);
            if (viewGroup != null) {
                viewGroup.setVisibility(View.VISIBLE);
                if (isReportEditable()) {
                    List<FormFieldView> frmFldViews = populateViewWithFormFields(viewGroup, getExpenseReportDetail()
                            .getFormFields(), null);
                    if (frmFldViews != null && frmFldViews.size() > 0) {
                        if (frmFldViewListener != null) {
                            frmFldViewListener.setFormFieldViews(frmFldViews);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".populateReportSummaryHeaderInfo: frmFldViewListener is null!");
                        }
                    }
                } else {
                    populateViewWithFields(viewGroup, expRepDet.getFormFields(), null);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportSummaryHeaderInfo: can't find view group!");
            }

        }
    }

    // Populate report summary company disbursement information.
    private void populateReportSummaryCompanyDisbursementInfo(ExpenseReport expRep) {

        // If this is a new Report, hide the whole company disbursement section.
        if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW) {
            View group = findViewById(R.id.company_disbursement_group);
            if (group != null) {
                group.setVisibility(View.GONE);
            }

        } else {

            ExpenseReportDetail expRepDet = (ExpenseReportDetail) expRep;

            Resources res = getResources();
            Locale loc = res.getConfiguration().locale;

            // Set amount due employee.
            setFieldValue(R.id.report_amount_due_employee, R.id.field_name, res.getString(R.string.amount_due_emp));
            String fldVal = "";
            if (expRepDet.totalDueEmployee != null) {
                fldVal = FormatUtil.formatAmount(expRepDet.totalDueEmployee, loc, expRepDet.crnCode, true);
            }
            setFieldValue(R.id.report_amount_due_employee, R.id.field_value, fldVal);

            // Set the "due company card" field.
            if (expRepDet.dueCompanyCard != null) {
                // Set company paid to credit card.
                setFieldValue(R.id.report_company_paid_to_credit_card, R.id.field_name,
                        res.getString(R.string.company_paid_to_credit_card));
                fldVal = "";
                if (expRepDet.dueCompanyCard != null) {
                    fldVal = FormatUtil.formatAmount(expRepDet.dueCompanyCard, loc, expRepDet.crnCode, true);
                }
                setFieldValue(R.id.report_company_paid_to_credit_card, R.id.field_value, fldVal);
            } else {
                // Hide the "due company card" field and its separator.
                hideView(R.id.report_company_paid_to_credit_card);
                hideView(R.id.report_company_paid_to_credit_card_separator);
            }

            // Set total paid by company.
            setFieldValue(R.id.report_total_paid_by_company, R.id.field_name,
                    res.getString(R.string.total_paid_by_company));
            fldVal = "";
            if (expRepDet.totalPaidByCompany != null) {
                fldVal = FormatUtil.formatAmount(expRepDet.totalPaidByCompany, loc, expRep.crnCode, true);
            }
            setFieldValue(R.id.report_total_paid_by_company, R.id.field_value, fldVal);

        }
    }

    // Populate report summary employee disbursement information.
    private void populateReportSummaryEmployeeDisbursementInfo(ExpenseReport expRep) {

        // If this is a new Report, hide the whole company disbursement section.
        if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW) {
            View group = findViewById(R.id.employee_disbursement_group);
            if (group != null) {
                group.setVisibility(View.GONE);
            }

        } else {

            Resources res = getResources();
            Locale loc = res.getConfiguration().locale;
            LayoutInflater inflater = LayoutInflater.from(this);
            LinearLayout empDisbLayout = (LinearLayout) findViewById(R.id.report_summary_employee_disbursement_list);

            if (expRep instanceof ExpenseReportDetail) {
                ExpenseReportDetail expRepDet = (ExpenseReportDetail) expRep;
                ArrayList<ExpenseReportDisbursement> disbs = expRepDet.getEmployeeDisbursements();

                if (disbs != null) {

                    for (ExpenseReportDisbursement disb : disbs) {
                        View field = inflater.inflate(R.layout.static_text_form_field, null);
                        setFieldValue(field, R.id.field_name, disb.label);
                        setFieldValue(field, R.id.field_value,
                                FormatUtil.formatAmount(disb.amount, loc, expRepDet.crnCode, true));
                        View sep = inflater.inflate(R.layout.group_separator, null);

                        empDisbLayout.addView(field);
                        empDisbLayout.addView(sep);
                    }
                }
            }

            // Show/Hide Total Personal Amount.
            if (expRep.totalPersonalAmount != null) {
                // Set the Total Personal Amount field.

                View field = inflater.inflate(R.layout.static_text_form_field, null);
                setFieldValue(field, R.id.field_name, res.getString(R.string.total_personal_amount));
                setFieldValue(field, R.id.field_value,
                        FormatUtil.formatAmount(expRep.totalPersonalAmount, loc, expRep.crnCode, true));

                empDisbLayout.addView(field);
            }
        }
    }

    /**
     * Populate any report-level comment information.
     * 
     * @param expRep
     *            the expense report.
     */
    private void populateReportCommentInfo(ExpenseReport expRep) {
        try {
            ExpenseReportDetail expRepDet = (ExpenseReportDetail) expRep;
            ViewGroup comGroup = (ViewGroup) findViewById(R.id.report_summary_comment_list);
            if (expRepDet != null && expRepDet.getComments() != null && expRepDet.getComments().size() > 0) {
                ArrayList<ExpenseReportComment> comList = expRepDet.getComments();
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
                comGroup = (ViewGroup) findViewById(R.id.header_comment_group);
                if (comGroup != null) {
                    comGroup.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportCommentInfo: unable to locate comment group!");
                }
            }
        } catch (ClassCastException ccExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportCommentInfo: " + ccExc.getMessage(), ccExc);
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
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# hasCopyDownChildren()
     */
    @Override
    protected boolean hasCopyDownChildren() {
        boolean retVal = false;
        if (expRep != null) {
            retVal = (expRep.getExpenseEntries() != null && expRep.getExpenseEntries().size() > 0);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".hasCopyDownChildren: expRep is null!");
        }
        return retVal;
    }

    /*
     * 
     */
    @Override
    protected FormFieldViewListener createFormFieldViewListener() {

        // If this is a new report, add listener to handle policy change.
        if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW) {
            return new ReportFormFieldViewLisetener(this);
        } else {
            return super.createFormFieldViewListener();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#sendSaveRequest ()
     */
    @Override
    protected void sendSaveRequest() {
        ConcurCore ConcurCore = (ConcurCore) getApplication();
        ConcurService concurService = ConcurCore.getService();

        final ExpenseReportDetail rpt = getExpenseReportDetail();
        if (rpt == null) {
            // Something bad has happened. Just get out.
            finish();
        } else {
            registerSaveReportReceiver();
            saveReportRequest = concurService.sendSaveReportRequest(getUserId(), getExpenseReportDetail(),
                    overWriteCopyDownValues);
            if (saveReportRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to create 'SaveReport' request!");
                unregisterSaveReportReceiver();
            } else {
                saveReportReceiver.setRequest(saveReportRequest);
                // Show the saving report dialog.
                showDialog(Const.DIALOG_EXPENSE_SAVE_REPORT);
            }
        }
    }

    /**
     * Invokes ConcurService to retrieve the report from fields with the given <code>polKey</code>. If <code>null</code>, then the
     * 'default' report form fields will be returned by the web service.
     * 
     * @param polKey
     *            the Policy Key to get the list of report form fields. If <code>null</code>, then the 'default' report form
     *            fields will be returned by the web service.
     */
    protected void sendReportFormRequest(String polKey) {

        ConcurCore ConcurCore = (ConcurCore) getApplication();
        ConcurService concurService = ConcurCore.getService();

        if (concurService != null) {
            registerGetReportFormReceiver();

            // Invoke MWS to get the Report Form Fields.
            reportFormRequest = concurService.sendReportFormRequest(polKey);

            if (reportFormRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to create 'ReportForm' request!");
                unregisterGetReportFormReceiver();
            } else {
                // Set the request object on the receiver.
                reportFormReceiver.setServiceRequest(reportFormRequest);
                // Show the "Loading Report form..."
                showDialog(Const.DIALOG_EXPENSE_GET_REPORT_FORM);
            }
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".sendReportFormRequest: service is unavailable.");
        }
    }

    @Override
    protected String[] getHardStopFieldIds() {
        return HARD_STOP_FIELD_IDS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getCopyDownPromptCharSequence()
     */
    @Override
    protected CharSequence getCopyDownPromptCharSequence() {
        return getResources().getQuantityString(R.plurals.dlg_expense_copy_down_fields_message_report_header,
                missReqInvalidCopyDownFormFieldValues.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onCreate(android .os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        finishOnSave = true;

        // Restore any receivers.
        restoreReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save 'SaveReportReceiver'.
        if (saveReportReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            saveReportReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(SAVE_REPORT_RECEIVER_KEY, saveReportReceiver);
        }

        // Save 'ReportFormReciever'
        if (reportFormReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            reportFormReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(REPORT_FORM_RECEIVER_KEY, reportFormReceiver);
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
            // Restore 'SaveReportEntryReceiver'.
            if (retainer.contains(SAVE_REPORT_RECEIVER_KEY)) {
                saveReportReceiver = (SaveReportReceiver) retainer.get(SAVE_REPORT_RECEIVER_KEY);
                if (saveReportReceiver != null) {
                    // Set the activity on the receiver.
                    saveReportReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer contains null reference for save report entry receiver!");
                }
            }

            // Restore 'ReportFormReceiver'
            if (retainer.contains(REPORT_FORM_RECEIVER_KEY)) {
                reportFormReceiver = (ReportFormReceiver) retainer.get(REPORT_FORM_RECEIVER_KEY);
                if (reportFormReceiver != null) {
                    // Set the activity on the receiver.
                    reportFormReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer contains null reference for report form receiver!");
                }
            }
        }
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onCreateDialog (int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case Const.DIALOG_EXPENSE_SAVE_REPORT: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.saving_report));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (saveReportRequest != null) {
                        // Cancel the request.
                        saveReportRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: saveReportRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_SAVE_REPORT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_expense_save_report_failed_title);
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
        case Const.DIALOG_EXPENSE_GET_REPORT_FORM: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_expense_report_header_loading_form));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (reportFormRequest != null) {
                        // Cancel the request.
                        reportFormRequest.cancel();

                        // And get out. We do not want to be here without a
                        // report form.
                        finish();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: reportFormRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_GET_REPORT_FORM_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_expense_report_header_loading_form_failed);
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
        case DIALOG_ERROR_PARSING_REPORT_FROM_FIELDS: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_expense_report_header_form_field_parse_error);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
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
        case Const.DIALOG_EXPENSE_SAVE_REPORT_FAILED: {
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
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#buildView()
     */
    @Override
    protected void buildView() {

        // Construct the filter and receiver.
        saveReportFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_SAVE);

        // Construct the filter for downloading the report from.
        reportFormFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_FORM_DOWNLOADED);

        // Instruct the window manager to only show the soft keyboard when the
        // end-user clicks on it.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Set the content view.
        setContentView(R.layout.expense_report_header);

        // Configure the screen header.
        configureScreenHeader(expRep);

        // Configure the screen footer.
        configureScreenFooter();

        // Set the report-level exceptions.
        populateReportExceptionInfo(expRep);

        // Populate report-level comments.
        populateReportCommentInfo(expRep);

        // Populate report summary company disbursement information.
        populateReportSummaryCompanyDisbursementInfo(expRep);

        // Populate report summary employee disbursement information.
        populateReportSummaryEmployeeDisbursementInfo(expRep);

        // Hide certain fields and widgets if this is a new Report entry.
        if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW) {

            // Hide the summary header.
            View titleHeader = findViewById(R.id.title_header);
            titleHeader.setVisibility(View.GONE);

            // Hide the summary header separator.
            View separator = findViewById(R.id.header_separator);
            separator.setVisibility(View.GONE);

            // Attempt to restore 'editedExpRep' if necessary.
            if (lastSavedInstanceState != null && lastSavedInstanceState.containsKey(EDITED_REPORT_DETAIL_BUNDLE_KEY)
                    && editedExpRep == null) {

                this.editedExpRep = (ExpenseReportDetail) lastSavedInstanceState
                        .getSerializable(EDITED_REPORT_DETAIL_BUNDLE_KEY);
                if (frmFldViewListener != null) {
                    frmFldViewListener.setExpenseReport(editedExpRep);
                }
            } else if (frmFldViewListener != null) {
                // Set the expense report on the save form view listener.
                frmFldViewListener.setExpenseReport(getExpenseReportDetail());
            }

            // Grab a reference to any previously build form field views.
            List<FormFieldView> srcFrmFlds = null;
            if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null
                    && frmFldViewListener.getFormFieldViews().size() > 0) {
                srcFrmFlds = frmFldViewListener.getFormFieldViews();
            }

            // Populate report summary header information.
            populateReportSummaryHeaderInfo(getExpenseReportDetail());

            // Grab a reference to any newly build form fields.
            List<FormFieldView> dstFrmFlds = null;
            if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null
                    && frmFldViewListener.getFormFieldViews().size() > 0) {
                dstFrmFlds = frmFldViewListener.getFormFieldViews();
            }
            // Transfer any edited values from 'srcFrmFlds' to 'dstFrmFlds'
            // where they match on
            // field id and field type.
            if (srcFrmFlds != null && srcFrmFlds.size() > 0 && dstFrmFlds != null && dstFrmFlds.size() > 0) {
                transferEditedValues(srcFrmFlds, dstFrmFlds);
                frmFldViewListener.setFormFieldViews(dstFrmFlds);
            }

        } else {
            // Set the report header information
            populateReportHeaderInfo(expRep);

            // Populate report summary header information.
            populateReportSummaryHeaderInfo(expRep);
        }

        // Request a re-layout after populating views.
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linear_layout);
        linearLayout.requestLayout();

        // Restore any saved values.
        if (lastSavedInstanceState != null) {
            restoreFormFieldState();
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
                // Iterate over 'srcFldViews' and if their values have changed,
                // then update the corresponding fields in 'dstFldViews'.
                for (FormFieldView srcFldView : srcFldViews) {
                    // Does the set of 'dstFldViews' contain a field id as
                    // 'srcFldView'?
                    if (dstFrmFldMap.containsKey(srcFldView.getFormField().getId())) {
                        FormFieldView dstFldView = dstFrmFldMap.get(srcFldView.getFormField().getId());
                        // Update the currently displayed value in
                        // 'newFrmFldView' with the currently edited
                        // value from 'frmFldView'.
                        dstFldView.updateEditedValue(srcFldView);
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Serialize to XML and store the string.
        if (editedExpRep != null) {
            outState.putSerializable(EDITED_REPORT_DETAIL_BUNDLE_KEY, editedExpRep);
        }

    }

    // ############################################################# //
    // ####################### INNER CLASS ######################### //
    // ############################################################# //

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of a save receipt action.
     * 
     * @author AndrewK
     */
    static class SaveReportReceiver extends BroadcastReceiver {

        private final String CLS_TAG = ExpenseReportHeader.CLS_TAG + "." + SaveReportReceiver.class.getSimpleName();

        // A reference to the activity.
        private ExpenseReportHeader activity;

        // A reference to the save report request.
        private SaveReportRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>SaveReportReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        SaveReportReceiver(ExpenseReportHeader activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ExpenseReportHeader activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.saveReportRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the save receipt request object associated with this broadcast receiver.
         * 
         * @param request
         *            the save receipt request object associated with this broadcast receiver.
         */
        void setRequest(SaveReportRequest request) {
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
                activity.unregisterSaveReportReceiver();

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
                                        activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }

                                    if (activity.reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW) {
                                        // Clear out any currently edited
                                        // expense report edit.
                                        activity.editedExpRep = null;

                                        // Get the new report key and pass down
                                        // it down
                                        // so the caller can reference it.
                                        Intent data = new Intent();
                                        if (intent != null && intent.hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {

                                            data.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY,
                                                    intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY));
                                        }
                                        activity.setResult(Activity.RESULT_OK, data);

                                    } else {
                                        // Set the result for the calling activity.
                                        activity.setResult(Activity.RESULT_OK);
                                    }

                                    // Set the flag to refresh the active report
                                    // list.
                                    IExpenseReportCache expRepCache = ((ConcurCore) activity.getApplication())
                                            .getExpenseActiveCache();
                                    expRepCache.setShouldRefreshReportList();
                                    // If the save was not due to a confirmed
                                    // 'back' button press, then rebuild the
                                    // display;
                                    // otherwise, finish the activity.
                                    if (!activity.processingBackPressed && !activity.finishOnSave) {
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
                                    activity.showDialog(Const.DIALOG_EXPENSE_SAVE_REPORT_FAILED);

                                    try {
                                        // Dismiss the dialog.
                                        activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT);
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
                                    activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            try {
                                // Dismiss the dialog.
                                activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT);
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
                                activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    }
                } else {
                    try {
                        // Dismiss the dialog.
                        activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }

                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear the 'processBackPressed' flag.
                activity.processingBackPressed = false;

                // Clear the request reference.
                activity.saveReportRequest = null;

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }

    }

    /**
     * Extension of <code>BaseBroadcastReceiver</code> for handling notification from requesting Report Form Fields.
     * 
     * @author Chris N. Diaz
     */
    static class ReportFormReceiver extends BaseBroadcastReceiver<ExpenseReportHeader, ReportFormRequest> {

        /**
         * Default constructor.
         * 
         * @param activity
         */
        protected ReportFormReceiver(ExpenseReportHeader activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setServiceRequest(com.concur.mobile.service.ServiceRequest )
         */
        @Override
        protected void setActivityServiceRequest(ReportFormRequest request) {
            activity.reportFormRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(ExpenseReportHeader activity) {
            activity.reportFormRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterGetReportFormReceiver();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {

            String xmlReply = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_FORM_FIELDS);
            ExpenseReportDetail expRepDetail = null;
            try {
                expRepDetail = ExpenseReportDetail.parseReportDetailXml(xmlReply, true);
            } catch (Exception e) {
                // MOB-18684 Eat the exception in case xml response is empty or gets cut off, and show error dialog to user.
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: runtime exception during parse report form", e);
            }

            if (expRepDetail != null) {

                // Set the default name if none is set.
                String dfltRptName = ViewUtil.getUniqueDefaultNewReportName(activity);
                if (expRepDetail.reportName == null || expRepDetail.reportName.trim().length() == 0) {
                    expRepDetail.reportName = dfltRptName;
                }
                if (expRepDetail.getFormField("Name").getValue() == null
                        || expRepDetail.getFormField("Name").getValue().trim().length() == 0) {
                    expRepDetail.getFormField("Name").setValue(dfltRptName);
                }

                if (expRepDetail.getFormField("UserDefinedDate") != null) {
                    // MOB-21227 Update the report date
                    Calendar calendar = Calendar.getInstance();
                    // Use 00:00:00 for time instead of current phone time.
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

                    String dateVal = Format.safeFormatCalendar(FormatUtil.XML_DF_LOCAL, calendar);
                    expRepDetail.getFormField("UserDefinedDate").setValue(dateVal);
                }
                // Set the PolKey (Policy) field to RW - (it's set to RO in ExpenseReportFormField).
                // MOB-15965: Some users/companies are configured for RO or RW policies, so we need to honor it.
                // Related to MOB-17496: The fix above caused issues for other companies that DO require
                // read/write access to the Policy.
                /*
                 * ExpenseReportFormField polKeyField = expRepDetail.getFormField(POLICY_ID); if (polKeyField != null &&
                 * activity.reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW) { polKeyField.setAccessType(AccessType.RW); }
                 */

                // Update the listener with the newly loaded expense report
                // entry.
                activity.frmFldViewListener.setExpenseReport(expRepDetail);
                activity.editedExpRep = expRepDetail;

                // Populate report summary header information.
                activity.buildView();
            } else {
                activity.showDialog(DIALOG_ERROR_PARSING_REPORT_FROM_FIELDS);
                activity.finish();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_GET_REPORT_FORM_FAILED);
            activity.finish();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_GET_REPORT_FORM);
        }

    }

    /**
     * An extension of <code>FormFieldViewListener</code> for the purposes of handling form regeneration for a report header.
     * 
     * @author AndrewK
     */
    class ReportFormFieldViewLisetener extends FormFieldViewListener {

        /**
         * Default constructor.
         * 
         * @param activity
         */
        public ReportFormFieldViewLisetener(BaseActivity activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.util.FormFieldViewListener#valueChanged(com.concur .mobile.util.FormFieldView)
         */
        @Override
        public void valueChanged(FormFieldView frmFldView) {

            // If the Policy changed and this is a New Report,
            // then we need to fetch the Report Form fields.
            ExpenseReportHeader activity = (ExpenseReportHeader) getActivity();
            if (activity.reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW
                    && frmFldView.getFormField().getId().equalsIgnoreCase(POLICY_ID) && frmFldView.hasValueChanged()) {

                if (frmFldView instanceof SearchListFormFieldView) {

                    // Get the PolKey of the ExpenseReportDetail
                    // to the newly selected Policy.
                    SearchListFormFieldView srchLstFrmFldView = (SearchListFormFieldView) frmFldView;
                    String polKey = srchLstFrmFldView.getLiKey();
                    ExpenseReportDetail expRepDetail = (ExpenseReportDetail) getExpenseReport();
                    expRepDetail.getFormField(POLICY_ID).setLiKey(polKey);
                    setExpenseReport(expRepDetail);
                    activity.editedExpRep = expRepDetail;

                    // Need to set to true in order to get the Report Form
                    // fields
                    // based on the newly selected Policy.
                    activity.fetchFormFields = true;

                    // Re-build the view based on the new Policy.
                    activity.buildView();

                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".valueChanged: frmFldView is not of type SearchListFormFieldView!");
                }
            }

            // Check if this field is dynamic (Dynamic means it drives visibility of other fields)
            // Currently dynamic field doesn't support (ConnectedListFormFieldView)
            // ExpenseTypeFormFieldView, SearchListFormFieldView cannot be a custom field? (to confirm?)
            checkConditionalFieldActions(frmFldView);
        }

    }

    @Override
    protected void onHandleSuccessConditionalFieldActions(List<ConditionalFieldAction> actions) {

        Log.d(Const.LOG_TAG, CLS_TAG + ".onHandleSuccessConditionalFieldActions: receiving dynamicField Actions!");

        List<ExpenseReportFormField> expRepFrmFields = getExpenseReportDetail().getFormFields();
        if (expRepFrmFields == null || expRepFrmFields.isEmpty() || actions.isEmpty()) {
            return;
        }

        Hashtable<String, ConditionalFieldAction> changedFields = new Hashtable<String, ConditionalFieldAction>();
        for (ConditionalFieldAction action : actions) {
            changedFields.put(action.getFormField(), action);
        }

        for (ExpenseReportFormField expRepFrmFld : expRepFrmFields) {
            if (changedFields.keySet().contains(expRepFrmFld.getFormFieldKey())) {
                ConditionalFieldAction action = changedFields.get(expRepFrmFld.getFormFieldKey());
                if (action.getVisibility() == ConditionalFieldAction.AccessVisibility.HIDE) {
                    expRepFrmFld.setAccessType(ExpenseReportFormField.AccessType.HD);
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
