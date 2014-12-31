package com.concur.mobile.gov.expense.doc.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.concur.gov.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.activity.ExpenseTypeSpinnerAdapter;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.ControlType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.DataType;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormUtil;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldView.ValidityCheck;
import com.concur.mobile.core.view.FormFieldViewListener;
import com.concur.mobile.core.view.StaticPickListFormFieldView;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.charge.activity.UnAppliedList;
import com.concur.mobile.gov.expense.charge.data.MobileExpense;
import com.concur.mobile.gov.expense.doc.data.DsDocDetailInfo;
import com.concur.mobile.gov.expense.doc.data.GovExpense;
import com.concur.mobile.gov.expense.doc.data.GovExpenseForm;
import com.concur.mobile.gov.expense.doc.data.GovExpenseFormField;
import com.concur.mobile.gov.expense.doc.service.AttachTMReceiptRequest;
import com.concur.mobile.gov.expense.doc.service.GetTMExpenseFormRequest;
import com.concur.mobile.gov.expense.doc.service.GetTMExpenseTypesRequest;
import com.concur.mobile.gov.expense.doc.service.SaveTMExpenseFormRequest;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GovDBAsyncTask;
import com.concur.mobile.gov.util.GovFlurry;
import com.concur.mobile.gov.util.IGovDBListener;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

public class Expense extends BaseActivity implements IGovDBListener, OnClickListener {

    protected final static String CLS_TAG = Expense.class.getSimpleName();

    protected static final String CREATING_EXPENSE = "creating";
    protected static final String FROM_DOC_DETAIL_EXPENSE_DRILL_IN = "from document detail expense drill in option";
    protected static final String CREATING_EXPENSE_IMG_ID = "creatingQE.imgId";
    protected static final String EXPENSE_TYPE = "expense_type";

    public static final int DIALOG_EXPENSE_TYPE = 1;
    protected static final int DIALOG_BAD_FIELDS = 2;
    protected static final int DIALOG_SAVE_FAIL = 3;

    private static final int DIALOG_SAVE_RECEIPT = 4;
    private static final int DIALOG_ATTACH_RECEIPT_FAILED = 5;

    protected static final int RECEIPT_REQ_EXP_CODE = 100;
    protected static final int RECEIPT_REQ_QE_CODE = 200;

    protected boolean creatingExpense, isFinished;

    protected Bundle bundle;

    protected String docName;
    protected String docType;
    protected String travId;
    protected String expId;
    protected String ccExpId;

    protected DsDocDetailInfo docDetailInfo;
    protected GovExpense currentExpense;

    protected ExpenseTypeSpinnerAdapter expTypeAdapter;
    protected ExpenseType selExpType;

    protected GovExpenseForm expenseForm;

    protected FormFieldViewListener frmFldViewListener;

    private GetTMExpenseTypesRequest expTypesRequest = null;
    private GetTMExpenseTypesReceiver expTypesReceiver;
    private IntentFilter expTypesFilter = null;

    private GetTMExpenseFormRequest expFormRequest = null;
    private GetTMExpenseFormReceiver expFormReceiver;
    private IntentFilter expFormFilter = null;

    private SaveTMExpenseFormRequest saveExpFormRequest = null;
    private SaveTMExpenseFormReceiver saveExpFormReceiver;
    private IntentFilter saveExpFormFilter = null;

    protected AttachReceiptReceiver attachReceiptReceiver;
    protected IntentFilter attachReceiptFilter;
    protected AttachTMReceiptRequest attachReceiptRequest;

    // Response from a service call failure for the failure dialog
    protected String errorMessage;
    protected String imgReceiptId;

    // holder for expense_desc data type;
    private DataType oldDataType;

    private ControlType oldCtrlType;

    /**
     * Contains the list of <code>FormFieldView</code> objects that are required
     * but with missing values, invalid values or copy-down field values.
     */
    protected List<FormFieldView> missReqInvalidFormFieldValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.expense);

        frmFldViewListener = new FormFieldViewListener(this);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            bundle = extras.getBundle(DocumentDetail.BUNDLE);
        }

        if (bundle != null) {
            docName = bundle.getString(DocumentListActivity.DOC_NAME);
            docType = bundle.getString(DocumentListActivity.DOCTYPE);
            travId = bundle.getString(DocumentListActivity.TRAV_ID);
            expId = bundle.getString(DocumentListActivity.EXP_ID);
            isFinished = bundle.getBoolean(UnAppliedList.ISFINISH);
            ccExpId = bundle.getString(UnAppliedList.CCEXPID);

            boolean isDocNameAvail = (docName == null || docName.length() == 0) ? false : true;
            boolean isDocTypeAvail = (docType == null || docType.length() == 0) ? false : true;
            boolean isTravIdAvail = (travId == null || travId.length() == 0) ? false : true;

            if (isDocNameAvail && isDocTypeAvail && isTravIdAvail) {
                GovService service = (GovService) getConcurService();
                GovDBAsyncTask task = new GovDBAsyncTask(docName, docType, travId, service);
                task.setGovDBListener(this);
                task.execute();
            } else {
                if (ccExpId != null) {
                    MobileExpense mobileExp = (MobileExpense) bundle.getSerializable(UnAppliedList.QEOBJ);
                    currentExpense = new GovExpense();
                    currentExpense.amount = mobileExp.postedAmt;
                    currentExpense.ccexpid = mobileExp.ccexpid;
                    currentExpense.expDate = mobileExp.tranDate;
                    currentExpense.expenseDesc = mobileExp.tranDescription;
                    currentExpense.imageid = mobileExp.imageid;
                    currentExpense.paymentMethod = "";
                    currentExpense.expenseCategory = "";
                    expenseForm = createExpenseForm();
                    buildView();
                }
            }
        } else {
            // Creating a new unapplied expense
            creatingExpense = true;

            // Make sure we have expense types
            GovAppMobile app = (GovAppMobile) getApplication();
            ArrayList<ExpenseType> expTypes = app.getExpenseTypes();
            if (expTypes == null || expTypes.size() == 0) {
                // Go get expense types
                sendExpenseTypesRequest();
            } else {
                // See if we have previously picked the type
                String selExpKey = null;
                if (savedInstanceState != null) {
                    selExpKey = savedInstanceState.getString(EXPENSE_TYPE);
                }

                if (selExpKey != null) {
                    for (ExpenseType et : expTypes) {
                        if (selExpKey.equals(et.key)) {
                            selExpType = et;
                            break;
                        }
                    }
                }

                if (selExpType == null) {
                    selectExpenseType();
                } else {
                    setSelectedExpenseType(selExpType);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Rotating while this dialog is up will leave an orphaned dialog. Just remove it now.
        removeDialog(DIALOG_EXPENSE_TYPE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(CREATING_EXPENSE, creatingExpense);
        if (creatingExpense) {
            if (selExpType != null) {
                outState.putString(EXPENSE_TYPE, selExpType.key);
            }
        } else {
            outState.putString(DocumentListActivity.DOC_NAME, docName);
            outState.putString(DocumentListActivity.DOCTYPE, docType);
            outState.putString(DocumentListActivity.TRAV_ID, travId);
            outState.putString(DocumentListActivity.EXP_ID, expId);
        }
    }

    protected void selectExpenseType() {
        showDialog(DIALOG_EXPENSE_TYPE);
    }

    @Override
    public void onDocDetailListenerSucceeded(Cursor cur) {
        if (cur.getCount() > 0) {
            if (cur.moveToFirst()) {
                docDetailInfo = new DsDocDetailInfo(cur);
                if (docDetailInfo != null && expId != null) {
                    currentExpense = docDetailInfo.findExpense(expId);
                    if (currentExpense != null) {
                        expenseForm = createExpenseForm();
                    }
                } else {
                    Log.e(CLS_TAG, ".onDocDetailListenerSucceeded : info from cursor is null. Something is  in DB table/query wrong.");
                }
            } else {
                Log.e(CLS_TAG, ".onDocDetailListenerSucceeded : cursor is not empty but cursor.movetofirst is false");
            }
        } else {
            Log.e(CLS_TAG, ".onDocDetailListenerSucceeded : cursor is null. Something is  in DB table/query wrong.");
        }

        buildView();
    }

    private GovExpenseForm createExpenseForm() {
        // Build the form of the fixed few fields for existing expenses
        GovExpenseForm expenseForm = new GovExpenseForm();
        expenseForm.fields = new ArrayList<GovExpenseFormField>(5);
        GovExpenseFormField field;

        field = new GovExpenseFormField("description", getText(R.string.gov_field_label_description).toString(), currentExpense.expenseDesc,
            AccessType.RO, ControlType.EDIT, DataType.VARCHAR, false);
        expenseForm.fields.add(field);

        field = new GovExpenseFormField("date", getText(R.string.gov_field_label_date).toString(),
            Format.safeFormatCalendar(FormatUtil.XML_DF_LOCAL, currentExpense.expDate),
            AccessType.RO, ControlType.DATE_EDIT, DataType.TIMESTAMP, false);
        expenseForm.fields.add(field);

        field = new GovExpenseFormField("amount", getText(R.string.gov_field_label_amount).toString(),
            FormatUtil.formatAmount(currentExpense.amount, Locale.US, "USD", true),
            AccessType.RO, ControlType.EDIT, DataType.VARCHAR, false);
        expenseForm.fields.add(field);

        return expenseForm;

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()
            && id >= FormFieldView.DIALOG_ID_BASE) {
            dialog = frmFldViewListener.getCurrentFormFieldView().onCreateDialog(id);
        } else {
            switch (id) {
            case com.concur.mobile.gov.util.Const.DIALOG_SAVE_TM_EXPENSE_FORM: {
                ProgressDialog pDialog = new ProgressDialog(this);
                pDialog.setMessage(this.getText(R.string.gov_saving_expense));
                pDialog.setIndeterminate(true);
                pDialog.setCancelable(true);
                pDialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Nothing to do. We can't cancel so it will run to completion.
                    }
                });

                dialog = pDialog;
                break;
            }
            case com.concur.mobile.gov.util.Const.DIALOG_GET_TM_EXPENSE_FORM: {
                ProgressDialog pDialog = new ProgressDialog(this);
                pDialog.setMessage(this.getText(R.string.gov_retrieve_expense_form));
                pDialog.setIndeterminate(true);
                pDialog.setCancelable(true);
                pDialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Gotta have a form
                        finish();
                    }
                });

                dialog = pDialog;
                break;
            }
            case com.concur.mobile.gov.util.Const.DIALOG_GET_TM_EXPENSE_TYPES: {
                ProgressDialog pDialog = new ProgressDialog(this);
                pDialog.setMessage(this.getText(R.string.gov_retrieve_expense_types));
                pDialog.setIndeterminate(true);
                pDialog.setCancelable(true);
                pDialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Gotta have a form
                        finish();
                    }
                });

                dialog = pDialog;
                break;
            }
            case DIALOG_EXPENSE_TYPE: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.gov_expense_type_prompt);
                expTypeAdapter = new ExpenseTypeSpinnerAdapter(this, null);
                GovAppMobile app = (GovAppMobile) getApplication();
                ArrayList<ExpenseType> expTypes = app.getExpenseTypes();

                expTypeAdapter.setExpenseTypes(expTypes);
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
                    }
                });

                customListView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                        removeDialog(DIALOG_EXPENSE_TYPE);
                        if (which != -1) {
                            Object selExpObj = expTypeAdapter.getItem(which);
                            if (selExpObj instanceof ExpenseType) {
                                setSelectedExpenseType((ExpenseType) selExpObj);
                            }
                        }
                    }
                });

                AlertDialog alertDlg = builder.create();
                dialog = alertDlg;
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Can't proceed, get out
                        removeDialog(DIALOG_EXPENSE_TYPE);
                        if (selExpType == null) {
                            finish();
                        }
                    }
                });

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        removeDialog(DIALOG_EXPENSE_TYPE);
                    }
                });
                break;
            }
            case DIALOG_BAD_FIELDS: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setMessage(R.string.gov_dlg_expense_fields_message);
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = dlgBldr.create();
                break;
            }
            case DIALOG_SAVE_RECEIPT: {
                ProgressDialog progDlg = new ProgressDialog(this);
                progDlg.setMessage(getText(R.string.saving_receipt));
                progDlg.setIndeterminate(true);
                progDlg.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (attachReceiptRequest != null) {
                            attachReceiptRequest.cancel();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onCancel(SaveReceiptDialog): saveReceiptRequest is null!");
                        }
                    }
                });
                dialog = progDlg;
                break;
            }
            case DIALOG_ATTACH_RECEIPT_FAILED: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setMessage(getText(R.string.gov_dlg_receipt_attach_failed));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                return dlgBldr.create();
            }
            case DIALOG_SAVE_FAIL: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.gov_dlg_save_fail_title);
                dlgBldr.setMessage(errorMessage);
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
        }
        return dialog;
    }

    protected void setSelectedExpenseType(ExpenseType selExpType) {
        // Set the reference.
        this.selExpType = selExpType;

        // Update the display.
        sendFormRequest("", selExpType.name);
    }

    protected void buildView() {
        initScreenHeader();
        populateFormFields();
        initOtherInformation();

    }

    protected void populateFormFields() {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.entry_field_list);
        if (viewGroup != null) {
            if ((viewGroup).getChildCount() > 0) {
                ((viewGroup)).removeAllViews();
            }
            List<FormFieldView> frmFldViews = populateExpenseDetailViewGroup(viewGroup);
            if (frmFldViews != null && frmFldViews.size() > 0) {
                if (frmFldViewListener != null) {
                    frmFldViewListener.setFormFieldViews(frmFldViews);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseDetails: frmFldViewListener is null!");
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseDetails: expense form field group not found!");
        }
    }

    protected List<FormFieldView> populateExpenseDetailViewGroup(ViewGroup viewGroup) {

        List<FormFieldView> frmFldViews = new ArrayList<FormFieldView>();
        if (viewGroup != null) {
            if (expenseForm != null && expenseForm.fields != null && expenseForm.fields.size() > 0) {
                // MOB-12835 is required to change description form field. so user can rechange it after selecting an exp_type;
                GovExpenseFormField formField = findFormFieldByFieldId("expense.tran_description");
                if (formField != null) {
                    formField.setAccessType(AccessType.RW);
                    oldCtrlType = formField.getControlType();
                    formField.setControlType(ControlType.UNSPECIFED);
                    oldDataType = formField.getDataType();
                    formField.setDataType(DataType.EXPENSE_TYPE);
                }
                frmFldViews.addAll(FormUtil.populateViewWithFormFields(this, viewGroup, expenseForm.fields, null, frmFldViewListener));
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseDetailViewGroup: null view group!");
        }

        return frmFldViews;
    }

    protected void initScreenHeader() {
        getSupportActionBar().setTitle(R.string.expense);
    }

    protected void initOtherInformation() {
        // set Reimbursable default value
        GovExpenseFormField formField = findFormFieldByFieldId("expense.Reimbursable");
        FormFieldView reimbursable = frmFldViewListener.findFormFieldViewById("expense.Reimbursable");
        if (reimbursable instanceof StaticPickListFormFieldView) {
            reimbursable.setCurrentValue(formField.getValue(), false);
        }
        // set taxable default value
        formField = findFormFieldByFieldId("expense.Taxable");
        reimbursable = frmFldViewListener.findFormFieldViewById("expense.Taxable");
        if (reimbursable instanceof StaticPickListFormFieldView) {
            reimbursable.setCurrentValue(formField.getValue(), false);
        }
        // set Sponsor default value
        formField = findFormFieldByFieldId("expense.Sponsor");
        reimbursable = frmFldViewListener.findFormFieldViewById("expense.Sponsor");
        if (reimbursable instanceof StaticPickListFormFieldView) {
            reimbursable.setCurrentValue(formField.getValue(), false);
        }

        View footerView = findViewById(R.id.footer);
        footerView.setVisibility(View.GONE);
    }

    /**
     * Find the form field by field id.
     * 
     * @param fieldId
     *            : field id
     * @return : reference of {@link GovExpenseFormField} which matches given fieldId
     */
    public GovExpenseFormField findFormFieldByFieldId(String fieldId) {
        GovExpenseFormField expRepFrmFld = null;
        ArrayList<GovExpenseFormField> formFields = expenseForm.fields;
        if (formFields != null) {
            Iterator<GovExpenseFormField> frmFldIter = formFields.iterator();
            while (frmFldIter.hasNext()) {
                GovExpenseFormField frmFld = frmFldIter.next();
                if (frmFld.getId().equalsIgnoreCase(fieldId)) {
                    expRepFrmFld = frmFld;
                    break;
                }
            }
        }
        return expRepFrmFld;
    }

    protected void save() {

        List<FormFieldView> invalidFieldValues = checkForInvalidValues();
        if (invalidFieldValues != null) {
            // Set the reference used in the 'onPrepareDialog' to dynamically populate
            // the main dialog view.
            missReqInvalidFormFieldValues = invalidFieldValues;
            // Display a dialog about the invalid field values.
            showDialog(DIALOG_BAD_FIELDS);
        } else {
            // First, obtain a list of form field views that are required, but have missing
            // values.
            List<FormFieldView> reqMissingValues = checkForMissingValues();
            if (reqMissingValues != null) {
                // Set the reference used in 'onPrepareDialog' to dynamically populate
                // the main dialog view.
                missReqInvalidFormFieldValues = reqMissingValues;
                // Display the missing values hard-stop dialog.
                showDialog(DIALOG_BAD_FIELDS);
            } else {
                // Check for connectivity, if none, then display dialog and return.
                if (ConcurCore.isConnected()) {
                    // Commit form field values to their backed domain objects.
                    commitEditedValues();

                    // Instruct the actual save request to be sent to the server.
                    sendSaveRequest();
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
            }
        }
    }

    /**
     * Will save the new expense
     */
    protected void sendSaveRequest() {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerSaveExpenseReceiver();
                // Here you need to revert all the changes back for expense description form field.
                GovExpenseFormField formField = findFormFieldByFieldId("expense.tran_description");
                if (formField != null) {
                    formField.setAccessType(AccessType.RO);
                    if (oldDataType != null) {
                        formField.setDataType(oldDataType);
                    } else {
                        // Accoring to wiki expense desc has data type varchar.
                        formField.setDataType(DataType.VARCHAR);
                    }
                    if (oldCtrlType != null) {
                        formField.setControlType(oldCtrlType);
                    } else {
                        // According to wiki expense desc has control type "edit"
                        formField.setControlType(ControlType.EDIT);
                    }
                }
                saveExpFormRequest = govService.sendSaveTMExpenseFormRequest(expenseForm);
                if (saveExpFormRequest == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendSaveRequest: unable to create request to save expense!");
                    unregisterSaveExpenseReceiver();
                } else {
                    // set service request.
                    saveExpFormReceiver.setServiceRequest(saveExpFormRequest);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_SAVE_TM_EXPENSE_FORM);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * Will commit edited values in form field view objects to their underlying
     * form field objects.
     */
    protected void commitEditedValues() {
        // Commit form field values to their backed domain objects.
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    frmFldView.commit();

                    GovExpenseFormField ff = (GovExpenseFormField) frmFldView.getFormField();

                    // Manually update two form values with their field values
                    if ("expense.tran_description".equalsIgnoreCase(ff.getId())) {
                        expenseForm.description = ff.getValue();
                    } else if ("expense.sublabel".equalsIgnoreCase(ff.getId())) {
                        expenseForm.sublabel = ff.getValue();
                    }

                    // Change the date format
                    // MOB-16870 required MM/dd/yyyy date format.
                    if (DataType.TIMESTAMP.equals(ff.getDataType())) {
                        Calendar cal = Parse.parseXMLTimestamp(ff.getValue());
                        ff.setValue(Format.safeFormatCalendar(new SimpleDateFormat("MM/dd/yyyy"), cal));
                    }
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".commitEditedValues: frmFldViewListener is null!");
        }
    }

    /**
     * Will examine all filled in form field values for valid values.
     * 
     * @return
     *         the list of form field view objects containing invalid data.
     */
    protected List<FormFieldView> checkForInvalidValues() {
        List<FormFieldView> retVal = null;
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    ValidityCheck check = frmFldView.isValueValid();
                    if (!check.result) {
                        if (retVal == null) {
                            retVal = new ArrayList<FormFieldView>();
                        }
                        retVal.add(frmFldView);
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Will return a list of form field objects with required but missing values.
     * 
     * @return
     *         the list of form field objects with required but missing values.
     */
    protected List<FormFieldView> checkForMissingValues() {
        List<FormFieldView> retVal = null;
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    // Check for required, no-value, with visibile views.
                    if (frmFldView.getFormField().isRequired() && !frmFldView.hasValue() && frmFldView.view != null
                        && frmFldView.view.getVisibility() == View.VISIBLE) {
                        if (retVal == null) {
                            retVal = new ArrayList<FormFieldView>();
                        }
                        retVal.add(frmFldView);
                    }
                }
            }
        }
        return retVal;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.header_view_attach_receipts) {
            Intent i = new Intent(this, DocumentReceipt.class);
            if (bundle != null) {
                i.putExtra(CREATING_EXPENSE, false);
                i.putExtra(FROM_DOC_DETAIL_EXPENSE_DRILL_IN, true);
                i.putExtra(CREATING_EXPENSE_IMG_ID, currentExpense.imageid);
                i.putExtra(DocumentDetail.BUNDLE, bundle);
                startActivityForResult(i, RECEIPT_REQ_EXP_CODE);
            } else {
                i.putExtra(CREATING_EXPENSE, true);
                i.putExtra(FROM_DOC_DETAIL_EXPENSE_DRILL_IN, false);
                i.putExtra(CREATING_EXPENSE_IMG_ID, imgReceiptId);
                startActivityForResult(i, RECEIPT_REQ_QE_CODE);
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".onClick : bundle is null, so can not pass anything to new screen!");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()) {
            frmFldViewListener.getCurrentFormFieldView().onActivityResult(requestCode, resultCode, data);
        } else {
            if (requestCode == RECEIPT_REQ_EXP_CODE && resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                if (isFinished) {
                    finish();
                }

            } else if (requestCode == RECEIPT_REQ_QE_CODE && resultCode == RESULT_OK) {
                imgReceiptId = data.getExtras().getString(CREATING_EXPENSE_IMG_ID);
                setResult(RESULT_OK);
            }
        }
    }

    private void sendExpenseTypesRequest() {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerExpTypesReceiver();
                expTypesRequest = govService.sendGetTMExpenseTypesRequest();
                if (expTypesRequest == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendExpenseTypesRequest: unable to create request to get expense types!");
                    unregisterExpTypesReceiver();
                } else {
                    // set service request.
                    expTypesReceiver.setServiceRequest(expTypesRequest);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_TM_EXPENSE_TYPES);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    protected void registerExpTypesReceiver() {
        if (expTypesReceiver == null) {
            expTypesReceiver = new GetTMExpenseTypesReceiver(this);
            if (expTypesFilter == null) {
                expTypesFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_GET_TM_EXPENSE_TYPES);
            }
            getApplicationContext().registerReceiver(expTypesReceiver, expTypesFilter);
        }
    }

    /**
     * unregister document detail receiver
     * */
    protected void unregisterExpTypesReceiver() {
        if (expTypesReceiver != null) {
            getApplicationContext().unregisterReceiver(expTypesReceiver);
            expTypesReceiver = null;
        }
    }

    class GetTMExpenseTypesReceiver extends BaseBroadcastReceiver<Expense, GetTMExpenseTypesRequest>
    {

        protected GetTMExpenseTypesReceiver(Expense activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(Expense activity) {
            activity.expTypesRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.removeDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_TM_EXPENSE_TYPES);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            // View v = findViewById(R.id.gov_docdetail_msg_layout);
            // // RelativeLayout
            // // msgLayout=(RelativeLayout)v.findViewById(R.id.gov_docdetail_main_layout);
            // RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.gov_docdetail_layout);
            // TextView errorMsg = (TextView) v.findViewById(R.id.gov_docdetail_msg);
            // v.setVisibility(View.VISIBLE);
            // mainLayout.setVisibility(View.GONE);
            // activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            // errorMsg.setText(actionStatusErrorMessage);
            // Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();
            ArrayList<ExpenseType> expTypes = app.getExpenseTypes();
            if (expTypes != null && expTypes.size() > 0) {
                selectExpenseType();
            } else {
                handleFailure(context, intent);
            }
        }

        @Override
        protected void setActivityServiceRequest(GetTMExpenseTypesRequest request) {
            activity.expTypesRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterExpTypesReceiver();
        }
    }

    private void sendFormRequest(String doctype, String expDesc) {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerExpFormReceiver();
                expFormRequest = govService.sendGetTMExpenseFormRequest(doctype, expDesc);
                if (expFormRequest == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendFormRequest: unable to create request to get expense form!");
                    unregisterExpFormReceiver();
                } else {
                    // set service request.
                    expFormReceiver.setServiceRequest(expFormRequest);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_TM_EXPENSE_FORM);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    protected void registerExpFormReceiver() {
        if (expFormReceiver == null) {
            expFormReceiver = new GetTMExpenseFormReceiver(this);
            if (expFormFilter == null) {
                expFormFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_GET_TM_EXPENSE_FORM);
            }
            getApplicationContext().registerReceiver(expFormReceiver, expFormFilter);
        }
    }

    /**
     * unregister document detail receiver
     * */
    protected void unregisterExpFormReceiver() {
        if (expFormReceiver != null) {
            getApplicationContext().unregisterReceiver(expFormReceiver);
            expFormReceiver = null;
        }
    }

    /**
     * Iterate the form fields and do any internal setup such as static lists
     */
    protected void processFormFields() {
        for (GovExpenseFormField ff : expenseForm.fields) {
            // A non-searchable pick list needs a static list built to trigger the proper code downstream
            if (ControlType.PICK_LIST.equals(ff.getControlType()) && !ff.isSearchable()) {
                ff.populateStaticList();
            }

            // The expense description is read-only. User must go back and select a new quick expense.
            if ("expense.tran_description".equalsIgnoreCase(ff.getId())) {
                ff.setAccessType(AccessType.RO);
            }
        }
    }

    class GetTMExpenseFormReceiver extends BaseBroadcastReceiver<Expense, GetTMExpenseFormRequest>
    {

        protected GetTMExpenseFormReceiver(Expense activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(Expense activity) {
            activity.expFormRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.removeDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_TM_EXPENSE_FORM);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            // View v = findViewById(R.id.gov_docdetail_msg_layout);
            // // RelativeLayout
            // // msgLayout=(RelativeLayout)v.findViewById(R.id.gov_docdetail_main_layout);
            // RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.gov_docdetail_layout);
            // TextView errorMsg = (TextView) v.findViewById(R.id.gov_docdetail_msg);
            // v.setVisibility(View.VISIBLE);
            // mainLayout.setVisibility(View.GONE);
            // activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            // errorMsg.setText(actionStatusErrorMessage);
            // Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();
            activity.expenseForm = app.getCurrentExpenseForm();
            if (activity.expenseForm != null && activity.expenseForm.fields != null
                && activity.expenseForm.fields.size() > 0) {
                activity.processFormFields();
                activity.buildView();
            } else {
                handleFailure(context, intent);
            }
        }

        @Override
        protected void setActivityServiceRequest(GetTMExpenseFormRequest request) {
            activity.expFormRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterExpFormReceiver();
        }
    }

    protected void registerSaveExpenseReceiver() {
        if (saveExpFormReceiver == null) {
            saveExpFormReceiver = new SaveTMExpenseFormReceiver(this);
            if (saveExpFormFilter == null) {
                saveExpFormFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_SAVE_TM_EXPENSE_FORM);
            }
            getApplicationContext().registerReceiver(saveExpFormReceiver, saveExpFormFilter);
        }
    }

    protected void unregisterSaveExpenseReceiver() {
        if (saveExpFormReceiver != null) {
            getApplicationContext().unregisterReceiver(saveExpFormReceiver);
            saveExpFormReceiver = null;
        }
    }

    class SaveTMExpenseFormReceiver extends BaseBroadcastReceiver<Expense, SaveTMExpenseFormRequest>
    {

        protected SaveTMExpenseFormReceiver(Expense activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(Expense activity) {
            activity.expFormRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {

        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.errorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            activity.showDialog(DIALOG_SAVE_FAIL);
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // Just return to the previous activity
            if (imgReceiptId != null && creatingExpense) {
                ccExpId = intent.getExtras().getString(com.concur.mobile.gov.util.Const.EXTRA_GOV_QE_ID);
                activity.sendAttachReceiptRequest();
            } else {
                GovAppMobile app = (GovAppMobile) getApplication();
                app.setExpListRefreshReq(true);
                // flurry notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_YES);
                if (creatingExpense) {
                    params.put(GovFlurry.PARAM_NAME_VIEWED_FROM, Flurry.PARAM_VALUE_QUICK_EXPENSE);
                }
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_EXPENSE, GovFlurry.EVENT_EXPENSE,
                    params);
                finish();
            }
        }

        @Override
        protected void setActivityServiceRequest(SaveTMExpenseFormRequest request) {
            activity.saveExpFormRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterSaveExpenseReceiver();
        }
    }

    protected void sendAttachReceiptRequest() {
        if (imgReceiptId != null) {
            GovService govService = (GovService) getConcurService();
            registerAttachReceiptReceiver();
            attachReceiptRequest = govService.sendAttachTMReceiptRequest(imgReceiptId, ccExpId, docName, docType, expId);
            if (attachReceiptRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".attachReceiptRequest: unable to create attach receipt request.");
                unregisterAttachReceiptReceiver();
            } else {
                // Set the request object on the receiver.
                attachReceiptReceiver.setServiceRequest(attachReceiptRequest);
            }
        }
    }

    protected void registerAttachReceiptReceiver() {
        if (attachReceiptReceiver == null) {
            attachReceiptReceiver = new AttachReceiptReceiver(this);
            if (attachReceiptFilter == null) {
                attachReceiptFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_ATTACH_TM_RECEIPT);
            }
            getApplicationContext().registerReceiver(attachReceiptReceiver, attachReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".registerAttachReceiptReceiver: attachReceiptReceiver is *not* null!");
        }
    }

    protected void unregisterAttachReceiptReceiver() {
        if (attachReceiptReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(attachReceiptReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAttachReceiptReceiver: illegal argument", ilaExc);
            }
            attachReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAttachReceiptReceiver: attachReceiptReceiver is null!");
        }
    }

    static class AttachReceiptReceiver extends BaseBroadcastReceiver<Expense, AttachTMReceiptRequest> {

        AttachReceiptReceiver(Expense activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(Expense activity) {
            activity.attachReceiptRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(DIALOG_ATTACH_RECEIPT_FAILED);
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            activity.removeDialog(com.concur.mobile.gov.util.Const.DIALOG_SAVE_TM_EXPENSE_FORM);
            // activity.setResult(Activity.RESULT_OK);
            GovAppMobile app = (GovAppMobile) activity.getApplication();
            app.setExpListRefreshReq(true);
            // flurry notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_YES);
            if (activity.imgReceiptId != null) {
                params.put(GovFlurry.PARAM_NAME_HAS_RECEIPT, Flurry.PARAM_VALUE_YES);
            }
            if (activity.creatingExpense) {
                params.put(GovFlurry.PARAM_NAME_VIEWED_FROM, Flurry.PARAM_VALUE_QUICK_EXPENSE);
            }
            EventTracker.INSTANCE.track(GovFlurry.CATEGORY_EXPENSE, GovFlurry.EVENT_EXPENSE,
                params);
            activity.finish();
        }

        @Override
        protected void setActivityServiceRequest(AttachTMReceiptRequest request) {
            activity.attachReceiptRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterAttachReceiptReceiver();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_save, menu);
        if (!creatingExpense) {
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
}
