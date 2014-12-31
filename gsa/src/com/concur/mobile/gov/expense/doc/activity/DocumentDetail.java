/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.doc.data.AccountCode;
import com.concur.mobile.gov.expense.doc.data.Audit;
import com.concur.mobile.gov.expense.doc.data.DsDocDetailInfo;
import com.concur.mobile.gov.expense.doc.data.Exceptions;
import com.concur.mobile.gov.expense.doc.data.GovExpense;
import com.concur.mobile.gov.expense.doc.data.PerdiemTDY;
import com.concur.mobile.gov.expense.doc.service.DocumentDetailRequest;
import com.concur.mobile.gov.expense.doc.stamp.activity.StampDocumentActivity;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GovDBAsyncTask;
import com.concur.mobile.gov.util.GovFlurry;
import com.concur.mobile.gov.util.IGovDBListener;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

public class DocumentDetail extends BaseActivity implements View.OnClickListener,
    IGovDBListener
{

    protected final static String CLS_TAG = DocumentDetail.class.getSimpleName();

    protected static final int REFRESH_EXPENSE_LIST_REQ_CODE = 101;
    protected static final int RECEIPT_REQ_CODE = 102;

    protected static final int STAMP_SELECTED_DOCUMENT = 200;

    public static final String BUNDLE = "bundle";
    public static final String COMMENTS = "comments";
    public static final String REFRESH = "isRefreshReq";

    protected Bundle bundle;
    private String travId, docname, doctype;

    protected DsDocDetailInfo docDetailInfo = null;

    private DocumentDetailRequest docDetailRequest = null;
    private DocumentDetailReceiver docDetailReceiver;
    private IntentFilter documentDetailIntentFilter = null;

    private Button stampButton;

    private Boolean isRefreshRequired = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore any receivers.
        restoreReceivers();
        setContentView(R.layout.document_detail);
        bundle = getIntent().getExtras();
        if (bundle != null) {
            initValue(savedInstanceState);
        } else {
            finish();
            Log.e(Const.LOG_TAG, CLS_TAG
                + " .onCreate : bundle is null, so can not show anything to new screen!"
                + "finishing activity go back and try again...");
        }
    }

    /**
     * initialize value for the view.
     * */
    private void initValue(Bundle savedInstanceState) {
        View v = findViewById(R.id.gov_docdetail_msg_layout);
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.gov_docdetail_layout);
        if (savedInstanceState != null) {
            if (retainer != null) {
                docDetailInfo = (DsDocDetailInfo) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_DETAIL_KEY);
                if (docDetailInfo != null) {
                    v.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                    buildView();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".initValue no docDetailinfo saved in savedInstance calling DBASyncTask!");
                    setValueUsingDB();
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initValue retainer is null calling DBASyncTask!");
                setValueUsingDB();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initValue savedInstance is null calling DBASyncTask!");
            setValueUsingDB();
        }
    }

    /**
     * set value using activity
     * */
    private void setValueUsingDB() {
        initTravellerInfoHeader();
        // call asynctask
        docname = bundle.getString(DocumentListActivity.DOC_NAME);
        doctype = bundle.getString(DocumentListActivity.DOCTYPE);
        travId = bundle.getString(DocumentListActivity.TRAV_ID);
        GovService service = (GovService) getConcurService();
        if (travId == null || travId.length() <= 0) {
            sendDocumentDetailRequest(docname, doctype, null);
        } else {
            GovDBAsyncTask task = new GovDBAsyncTask(docname, doctype, travId, service);
            task.setGovDBListener(DocumentDetail.this);
            task.execute();
        }
    }

    @Override
    public void onDocDetailListenerSucceeded(Cursor cur) {
        if (cur.getCount() > 0) {
            if (cur.moveToFirst()) {
                DsDocDetailInfo info = new DsDocDetailInfo(cur);
                if (GovAppMobile.isConnected()) {
                    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    Calendar lastRefresh = info.lastUsed;
                    int minuteDifference = FormatUtil.getMinutesDifference(lastRefresh, now);
                    if (minuteDifference == -1 || minuteDifference > 2) {
                        sendDocumentDetailRequest(docname, doctype, travId);
                    } else {
                        onHandleDocDetailSuccess(info);
                    }
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    onHandleDocDetailSuccess(info);
                }
            } else {
                Log.e(CLS_TAG, " .onDocDetailListenerSucceeded : cursor is not empty but cursor.movetofirst is false");
            }
        } else {
            sendDocumentDetailRequest(docname, doctype, travId);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        restoreReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            if (docDetailReceiver != null) {
                // Clear activity and we will reassigned.
                docDetailReceiver.setActivity(null);
                retainer
                    .put(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_DETAIL_RECIEVER_KEY, docDetailReceiver);
            }
            if (docDetailInfo != null) {
                retainer.put(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_DETAIL_KEY, docDetailInfo);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_DETAIL_DOCUMENT: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.gov_retrieve_documents_detail));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    // Dismiss the dialog.
                    dialog.dismiss();
                    // Attempt to cancel the request.
                    if (docDetailRequest != null) {
                        docDetailRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: perDiemRateRequest is null!");
                    }
                }
            });
            dialog = pDialog;
            break;
        }
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    /**
     * restore any receiver store for this activity into Retainer.
     * */
    protected void restoreReceivers() {
        if (retainer != null) {
            if (retainer.contains(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_DETAIL_RECIEVER_KEY)) {
                docDetailReceiver = (DocumentDetailReceiver) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_DETAIL_RECIEVER_KEY);
                // Reset the activity reference.
                docDetailReceiver.setActivity(this);
            }
        }
    }

    /**
     * register document detail receiver
     * */
    protected void registerDocumentDetailReceiver() {
        if (docDetailReceiver == null) {
            docDetailReceiver = new DocumentDetailReceiver(this);
            if (documentDetailIntentFilter == null) {
                documentDetailIntentFilter = new IntentFilter(
                    com.concur.mobile.gov.util.Const.ACTION_GET_DOCUMENT_DETAIL);
            }
            getApplicationContext().registerReceiver(docDetailReceiver, documentDetailIntentFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".registerDocumentDetailReceiver and documentdetail filter not null");
        }
    }

    /**
     * unregister document detail receiver
     * */
    protected void unregisterDocumentDetailReceiver() {
        if (docDetailReceiver != null) {
            getApplicationContext().unregisterReceiver(docDetailReceiver);
            docDetailReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterDocumentDetailReceiver is null!");
        }
    }

    /**
     * send document detail request to get document detail from the server.
     * 
     * @param documentListItem
     * */
    private void sendDocumentDetailRequest(String docname, String doctype, String travID) {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerDocumentDetailReceiver();
                docDetailRequest = govService.sendDocumentDetailRequest(docname, doctype, travID);
                if (docDetailRequest == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendDocumentDetailRequest: unable to create request to get Govt. Documents' Detail!");
                    unregisterDocumentDetailReceiver();
                } else {
                    // set service request.
                    docDetailReceiver.setServiceRequest(docDetailRequest);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_DETAIL_DOCUMENT);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling
     * the response for document detail.
     */
    class DocumentDetailReceiver extends
        BaseBroadcastReceiver<DocumentDetail, DocumentDetailRequest>
    {

        private final String CLS_TAG = DocumentDetail.CLS_TAG + "."
            + DocumentDetailReceiver.class.getSimpleName();

        protected DocumentDetailReceiver(DocumentDetail activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(DocumentDetail activity) {
            activity.docDetailRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_DETAIL_DOCUMENT);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            View v = findViewById(R.id.gov_docdetail_msg_layout);
            // RelativeLayout
            // msgLayout=(RelativeLayout)v.findViewById(R.id.gov_docdetail_main_layout);
            RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.gov_docdetail_layout);
            TextView errorMsg = (TextView) v.findViewById(R.id.gov_docdetail_msg);
            v.setVisibility(View.VISIBLE);
            mainLayout.setVisibility(View.GONE);
            activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            errorMsg.setText(actionStatusErrorMessage);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
            logFlurryEvent(false);
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();
            final DocumentDetailRequest detailRequest = activity.docDetailRequest;
            if (detailRequest != null) {
                DsDocDetailInfo reply = app.getDocDetailInfo();
                if (reply != null) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".handleSuccess ");
                    onHandleDocDetailSuccess(reply);
                } else {
                    logFlurryEvent(false);
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
                }
            } else {
                handleFailure(context, intent);
            }
        }

        @Override
        protected void setActivityServiceRequest(DocumentDetailRequest request) {
            activity.docDetailRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterDocumentDetailReceiver();
        }
    }

    /**
     * Helper Method : Implement this method in respective activity to build
     * respective page.
     * 
     * @param {@link DsDocDetailInfo} reply
     * 
     * */
    protected void onHandleDocDetailSuccess(DsDocDetailInfo reply) {
        docDetailInfo = reply;
        if (docDetailInfo != null) {
            View v = findViewById(R.id.gov_docdetail_msg_layout);
            RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.gov_docdetail_layout);
            v.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
            buildView();
            logFlurryEvent(true);
        } else {
            logFlurryEvent(false);
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to find document info from cache!");
        }
    }

    /**
     * Log flurry events that do you have doc info or not
     * */
    private void logFlurryEvent(boolean isSuccessful) {
        Map<String, String> params = new HashMap<String, String>();
        if (isSuccessful) {
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_YES);
            if (doctype != null) {
                params.put(GovFlurry.PARAM_NAME_DOCUMENT_TYPE, doctype);
            }
            Audit audit = docDetailInfo.audit;
            if (audit != null) {
                int passed = audit.passed;
                params.put(GovFlurry.PARAM_NAME_AUDIT_PASS_COUNT, Integer.toString(passed));
                int failed = audit.failed;
                params.put(GovFlurry.PARAM_NAME_AUDIT_FAIL_COUNT, Integer.toString(failed));
            }
        } else {
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_NO);
        }
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_DETAIL,
            GovFlurry.EVENT_VIEW_DOCUMENT_DETAIL, params);
    }

    /**
     * set up your activity screen here.
     * */
    private void buildView() {
        initScreenHeader();
        initTravellerInfoHeader();
        initOtherInformation();
    }

    /**
     * This method set screen header title.
     * 
     * */
    protected void initScreenHeader() {
        getSupportActionBar().setTitle(getTitleText());
    }

    /**
     * initialized Traveler info such as name, tripdate and amount
     * */
    private void initTravellerInfoHeader() {
        TextView txtView = (TextView) findViewById(R.id.gov_traveller_name);
        if (txtView != null) {
            txtView.setText(getTravellerName());
        }
        txtView = (TextView) findViewById(R.id.gov_doc_type);
        if (txtView != null) {
            txtView.setText(getGovDocDetailType());
        }
        txtView = (TextView) findViewById(R.id.gov_trip_doclabelType);
        if (txtView != null) {
            txtView.setText(getDocLabel());
        }
    }

    // TODO generic method..REF SegmentDetail.
    protected void initOtherInformation() {
        TextView txtView = (TextView) findViewById(R.id.gov_traveller_totalAmt);
        if (txtView != null) {
            txtView.setText(getTravellerAmount());
        }
        txtView = (TextView) findViewById(R.id.gov_trip_date);
        if (txtView != null) {
            txtView.setText(getGovDocDetailTripDate());
        }
        txtView = (TextView) findViewById(R.id.gov_doc_status_value);
        if (txtView != null) {
            txtView.setText(getGovDocStatus());
        }
        txtView = (TextView) findViewById(R.id.gov_doc_tanumber_value);
        if (txtView != null) {
            txtView.setText(getGovTANumber());
        }
        txtView = (TextView) findViewById(R.id.gov_doc_trip_purpose_value);
        if (txtView != null) {
            txtView.setText(getGovTripPurpose());
        }
        txtView = (TextView) findViewById(R.id.gov_doc_emissions_value);
        if (txtView != null) {
            txtView.setText(getGovDocEmissions());
        }
        ImageView img = (ImageView) findViewById(R.id.gov_trip_receipt);
        if (img != null) {
            setIconReceiptVisibility(img);
        }
        img = (ImageView) findViewById(R.id.gov_trip_violation);
        if (img != null) {
            setIconViolation(img);
        }
        View footerView = findViewById(R.id.footer);
        showHideBottomBar(footerView);
        configureListItems();
    }

    protected void configureListItems() {
        // receipt
        View view = findViewById(R.id.doc_detail_receipt);
        if (view != null) {
            view.setOnClickListener(this);
        }
        ImageView receiptIcon = (ImageView) ViewUtil.findSubView(this, R.id.doc_detail_receipt, R.id.receipt_icon);
        if (docDetailInfo.imageId != null && docDetailInfo.imageId.length() > 0) {
            receiptIcon.setVisibility(View.VISIBLE);
        } else {
            receiptIcon.setVisibility(View.GONE);
        }
        TextView txtView = (TextView) ViewUtil.findSubView(this, R.id.doc_detail_receipt, R.id.doc_detail_row_title);
        if (txtView != null) {
            txtView.setText(getString(R.string.gov_docdetail_receipt));
        }

        // per diem location
        view = findViewById(R.id.doc_detail_perdiem_location);
        if (view != null) {
            view.setOnClickListener(this);
        }
        txtView = (TextView) ViewUtil
            .findSubView(this, R.id.doc_detail_perdiem_location, R.id.doc_detail_row_title);
        if (txtView != null) {
            txtView.setText(getString(R.string.gov_docdetail_perdiem_location));
        }
        txtView = (TextView) ViewUtil
            .findSubView(this, R.id.doc_detail_perdiem_location, R.id.doc_detail_row_subtitle);
        if (txtView != null) {
            List<PerdiemTDY> list = docDetailInfo.perdiemList;
            if (list != null) {
                final int size = list.size();
                setSingleDigitSubtitle(size, txtView);
            } else {
                setSingleDigitSubtitle(-1, txtView);
            }
        }
        // expenses
        view = findViewById(R.id.doc_detail_expenses);
        if (view != null) {
            view.setOnClickListener(this);
        }
        txtView = (TextView) ViewUtil
            .findSubView(this, R.id.doc_detail_expenses, R.id.doc_detail_row_title);
        if (txtView != null) {
            txtView.setText(getString(R.string.gov_docdetail_expenses));
        }
        txtView = (TextView) ViewUtil
            .findSubView(this, R.id.doc_detail_expenses, R.id.doc_detail_row_subtitle);
        if (txtView != null) {
            List<GovExpense> list = docDetailInfo.expensesList;
            if (list != null) {
                final int size = list.size();
                setSingleDigitSubtitle(size, txtView);
            } else {
                setSingleDigitSubtitle(-1, txtView);
            }
        }
        // accounting allocation
        view = findViewById(R.id.doc_detail_accounting_allocation);
        if (view != null) {
            view.setOnClickListener(this);
        }
        txtView = (TextView) ViewUtil
            .findSubView(this, R.id.doc_detail_accounting_allocation, R.id.doc_detail_row_title);
        if (txtView != null) {
            txtView.setText(getString(R.string.gov_docdetail_accounting_allocation));
        }
        txtView = (TextView) ViewUtil
            .findSubView(this, R.id.doc_detail_accounting_allocation, R.id.doc_detail_row_subtitle);
        if (txtView != null) {
            List<AccountCode> list = docDetailInfo.accountCodeList;
            if (list != null) {
                final int size = list.size();
                setSingleDigitSubtitle(size, txtView);
            } else {
                setSingleDigitSubtitle(-1, txtView);
            }
        }
        // total and travel advances
        view = findViewById(R.id.doc_detail_TnTadvances);
        if (view != null) {
            view.setOnClickListener(this);
        }
        txtView = (TextView) ViewUtil
            .findSubView(this, R.id.doc_detail_TnTadvances, R.id.doc_detail_row_title);
        if (txtView != null) {
            txtView.setText(getString(R.string.gov_docdetail_totals_travel));
        }
        txtView = (TextView) ViewUtil
            .findSubView(this, R.id.doc_detail_TnTadvances, R.id.doc_detail_row_subtitle);
        if (txtView != null) {
            setSingleDigitSubtitle(-1, txtView);
        }
        // audit
        view = findViewById(R.id.doc_detail_audit);
        if (view != null) {
            view.setOnClickListener(this);
        }
        txtView = (TextView) ViewUtil.findSubView(this, R.id.doc_detail_audit, R.id.doc_detail_row_title);
        if (txtView != null) {
            txtView.setText(getString(R.string.gov_docdetail_audits));
        }
        txtView = (TextView) ViewUtil
            .findSubView(this, R.id.doc_detail_audit, R.id.doc_detail_row_subtitle);
        if (txtView != null) {
            setAuditDigitSubtitle(txtView);
        }
        // comments
        view = findViewById(R.id.doc_detail_comments);
        if (view != null) {
            view.setOnClickListener(this);
        }
        txtView = (TextView) ViewUtil
            .findSubView(this, R.id.doc_detail_comments, R.id.doc_detail_row_title);
        if (txtView != null) {
            txtView.setText(getString(R.string.gov_docdetail_comment));
        }
        txtView = (TextView) ViewUtil
            .findSubView(this, R.id.doc_detail_comments, R.id.doc_detail_row_subtitle);
        if (txtView != null) {
            setSingleDigitSubtitle(-1, txtView);
        }
    }

    /**
     * May be override in respective class for getting digit subtitle in "()";
     * 
     * @param txtView
     * */
    protected void setSingleDigitSubtitle(int size, TextView txtView) {
        if (size < 0) {
            txtView.setVisibility(View.INVISIBLE);
        } else {
            txtView.setVisibility(View.VISIBLE);
            txtView.setText(getString(R.string.gov_docdetail_digit, size));
        }
    }

    /**
     * May be override in respective class for getting digit subtitle in "()" for
     * Audit;
     * 
     * @param txtView
     * */
    protected void setAuditDigitSubtitle(TextView txtView) {
        Audit audit = docDetailInfo.audit;
        int passed = audit.passed;
        int failed = audit.failed;
        txtView.setText(getString(R.string.gov_docdetail_pass_fail_digit, passed, failed));
        txtView.setVisibility(View.VISIBLE);
    }

    /**
     * Must be override in respective class for getting Different Title
     * */
    protected String getTitleText() {
        return "";
    }

    /**
     * May be override in respective class to get traveller name.
     * */
    protected String getTravellerName() {
        String name = bundle.getString(DocumentListActivity.NAME);
        return FormatUtil.nullCheckForString(name);
    }

    /**
     * May be override in respective class to get document label.
     * */
    protected String getDocLabel() {
        String lbl = bundle.getString(DocumentListActivity.DOC_LABEL);
        return FormatUtil.nullCheckForString(lbl);
    }

    /**
     * May be override in respective class to get traveller amount.
     * */
    protected String getTravellerAmount() {
        Double amt = docDetailInfo.totalEstCost;
        String retVal = FormatUtil
            .formatAmount(amt, com.concur.mobile.gov.util.Const.GOV_LOCALE, com.concur.mobile.gov.util.Const.GOV_CURR_CODE, true, true);
        return FormatUtil.nullCheckForString(retVal);
    }

    /**
     * May be override in respective class to get selected document type.
     * */
    protected String getGovDocDetailType() {
        String docType = bundle.getString(DocumentListActivity.DOCTYPE);
        return FormatUtil.nullCheckForString(docType);
    }

    /**
     * May be override in respective class to get trip date.
     * */
    protected String getGovDocDetailTripDate() {
        StringBuilder strBuilder = new StringBuilder("");
        strBuilder
            .append(Format
                .safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY, docDetailInfo.tripBeginDate));
        strBuilder.append(" - ");
        strBuilder
            .append(Format
                .safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY, docDetailInfo.tripEndDate));
        return FormatUtil.nullCheckForString(strBuilder.toString());
    }

    /**
     * May be override in respective class to get selected document status
     * */
    protected String getGovDocStatus() {
        String retVal = docDetailInfo.currentStatus;
        return FormatUtil.nullCheckForString(retVal);
    }

    /**
     * May be override in respective class to get selected document's TA Number
     * */
    protected String getGovTANumber() {
        String retVal = docDetailInfo.TANumber;
        return FormatUtil.nullCheckForString(retVal);
    }

    /**
     * May be override in respective class to get selected Travller's trip
     * purpose.
     * */
    protected String getGovTripPurpose() {
        String retVal = docDetailInfo.purposeCode;
        return FormatUtil.nullCheckForString(retVal);
    }

    /**
     * May be override in respective class to get emissions.
     * */
    protected String getGovDocEmissions() {
        String retVal = Double.toString(docDetailInfo.emissionsLbs);
        return FormatUtil.nullCheckForString(Parse.safeParseDouble(retVal) + "");
    }

    /**
     * May be override in respective class to visible or invisible imgview.
     * 
     * @param img
     * */
    protected void setIconReceiptVisibility(ImageView img) {
        img.setVisibility(View.INVISIBLE);
        return;
    }

    /**
     * May be override in respective class to visible or invisible imgview.
     * 
     * @param img
     * */
    protected void setIconViolation(ImageView img) {
        Audit audit = docDetailInfo.audit;
        if (audit.failed > 0) {
            img.setVisibility(View.VISIBLE);
        } else {
            img.setVisibility(View.INVISIBLE);
        }
        return;
    }

    /**
     * show or hide footer bar. This method May be override in respective
     * activity.
     * 
     * @param footerView
     * */
    protected void showHideBottomBar(View footerView) {
        boolean needStamping = bundle.getBoolean(DocumentListActivity.NEED_STAMPING);
        if (needStamping) {
            footerView.setVisibility(View.VISIBLE);
            stampButton = (Button) footerView.findViewById(R.id.gov_footer_stamp);
            stampButton.setOnClickListener(this);
        } else {
            footerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        bundle = reCreateBundle(bundle);
        switch (id) {
        case R.id.doc_detail_receipt: {
            onClickReceipt();
            break;
        }
        case R.id.doc_detail_perdiem_location: {
            onClickPerdiemLocation();
            break;
        }
        case R.id.doc_detail_expenses: {
            onClickExpenses();
            break;
        }
        case R.id.doc_detail_accounting_allocation: {
            onClickAccountingAllocation();
            break;
        }
        case R.id.doc_detail_TnTadvances: {
            onClickTnTadvance();
            break;
        }
        case R.id.doc_detail_audit: {
            onClickAudit();
            break;
        }
        case R.id.doc_detail_comments: {
            onClickComment();
            break;
        }
        case R.id.gov_footer_stamp: {
            onClickStampButton();
            break;
        }
        }
    }

    /**
     * Recreate bundle to get travel id.
     * 
     * @param bundle
     * */
    private Bundle reCreateBundle(Bundle bundle) {
        Bundle result = null;
        if (bundle != null) {
            result = bundle;
            String travId = bundle.getString(DocumentListActivity.TRAV_ID);
            if (travId == null) {
                result.putString(DocumentListActivity.TRAV_ID, docDetailInfo.travelerId);
            }
        }
        return result;
    }

    /** go to document receipt */
    protected void onClickReceipt() {
        Intent i = new Intent(this, DocumentReceipt.class);
        if (bundle != null) {
            i.putExtra(BUNDLE, bundle);
            startActivityForResult(i, RECEIPT_REQ_CODE);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + " .onClickReceipt : bundle is null, so can not pass anything to new screen!");
        }
    }

    /** go to perdiem list location screen */
    protected void onClickPerdiemLocation() {
        Log.d(CLS_TAG, "onClickPerdiemLocation");
        List<PerdiemTDY> list = docDetailInfo.perdiemList;
        if (list != null && list.size() > 0) {
            Intent it = new Intent(DocumentDetail.this, PerDiemLocationListActivity.class);
            if (bundle != null) {
                it.putExtra(BUNDLE, bundle);
                startActivity(it);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + " .onClickPerdiemLocation : bundle is null, so can not pass anything to new screen!");
            }
            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_VIEW_PER_DIEM_LOCATION);
            if (doctype != null) {
                params.put(GovFlurry.PARAM_NAME_DOCUMENT_TYPE, doctype);
            }
            EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_DETAIL, GovFlurry.EVENT_VIEW_DOCUMENT_DETAIL,
                params);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + " .onClickPerdiemLocation : list is empty");
        }
    }

    /** go to expense list */
    protected void onClickExpenses() {
        Log.d(CLS_TAG, "onClickExpenses");
        Intent it = new Intent(DocumentDetail.this, ExpenseListActivity.class);
        if (bundle != null) {
            bundle.putBoolean(REFRESH, isRefreshRequired);
            it.putExtra(BUNDLE, bundle);
            startActivityForResult(it, REFRESH_EXPENSE_LIST_REQ_CODE);
            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_VIEW_EXPENSES);
            if (doctype != null) {
                params.put(GovFlurry.PARAM_NAME_DOCUMENT_TYPE, doctype);
            }
            EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_DETAIL, GovFlurry.EVENT_VIEW_DOCUMENT_DETAIL,
                params);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + " .onClickExpenses : bundle is null, so can not pass anything to new screen!");
        }

    }

    /** go to accounting allocation screen */
    protected void onClickAccountingAllocation() {
        Log.d(CLS_TAG, "onClickAccountingAllocation");
        List<AccountCode> list = docDetailInfo.accountCodeList;
        if (list != null && list.size() > 0) {
            Intent it = new Intent(DocumentDetail.this, AccAllocationListActivity.class);
            if (bundle != null) {
                it.putExtra(BUNDLE, bundle);
                startActivity(it);
                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_VIEW_ACC_ALLOCATION);
                if (doctype != null) {
                    params.put(GovFlurry.PARAM_NAME_DOCUMENT_TYPE, doctype);
                }
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_DETAIL, GovFlurry.EVENT_VIEW_DOCUMENT_DETAIL,
                    params);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + " .onClickAccountingAllocation : bundle is null, so can not pass anything to new screen!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + " .onClickAccountingAllocation : list is empty");
        }
    }

    /** go to trips and travel advances screen */
    protected void onClickTnTadvance() {
        Log.d(CLS_TAG, "onClickTnTadances");
        Intent it = new Intent(DocumentDetail.this, TotalsAndTravelsActivity.class);
        if (bundle != null) {
            it.putExtra(BUNDLE, bundle);
            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_VIEW_TOTAL_AND_TRAVELS);
            if (doctype != null) {
                params.put(GovFlurry.PARAM_NAME_DOCUMENT_TYPE, doctype);
            }
            EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_DETAIL, GovFlurry.EVENT_VIEW_DOCUMENT_DETAIL,
                params);
            startActivity(it);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + " .onClickTnTadances : bundle is null, so can not pass anything to new screen!");
        }
    }

    /** go to Audit screen and show list of exception */
    protected void onClickAudit() {
        Log.d(CLS_TAG, "onClickAudit");
        List<Exceptions> list = docDetailInfo.exceptionsList;
        if (list != null && list.size() > 0) {
            Intent it = new Intent(DocumentDetail.this, ExceptionsListActivity.class);
            if (bundle != null) {
                it.putExtra(BUNDLE, bundle);
                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_VIEW_AUDITS);
                if (doctype != null) {
                    params.put(GovFlurry.PARAM_NAME_DOCUMENT_TYPE, doctype);
                }
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_DETAIL, GovFlurry.EVENT_VIEW_DOCUMENT_DETAIL,
                    params);
                startActivity(it);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + " .onClickAudit : bundle is null, so can not pass anything to new screen!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + " .onClickAudit : list is empty");
        }
    }

    /** go to comment screen */
    protected void onClickComment() {
        Log.d(CLS_TAG, "onClickComment");
        String comments = docDetailInfo.comments;
        Intent it = new Intent(DocumentDetail.this, CommentsActivity.class);
        it.putExtra(COMMENTS, comments);
        // Flurry Notification
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_VIEW_COMMENTS);
        if (doctype != null) {
            params.put(GovFlurry.PARAM_NAME_DOCUMENT_TYPE, doctype);
        }
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_DETAIL, GovFlurry.EVENT_VIEW_DOCUMENT_DETAIL,
            params);
        startActivity(it);
    }

    /**
     * Start Stamp process. Go to StampDocumentActivity
     * */
    protected void onClickStampButton() {
        String status = bundle.getString(DocumentListActivity.AWAITING_STATUS);
        if (status != null && status.length() > 0) {
            Intent it = new Intent(DocumentDetail.this, StampDocumentActivity.class);
            it.putExtra(BUNDLE, bundle);
            startActivityForResult(it, STAMP_SELECTED_DOCUMENT);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + " .onClickStampButton :AWAITING_STATUS is null");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REFRESH_EXPENSE_LIST_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                isRefreshRequired = true;
                sendDocumentDetailRequest(docname, doctype, travId);
            }
        } else if (requestCode == RECEIPT_REQ_CODE && resultCode == RESULT_OK) {
            isRefreshRequired = true;
            sendDocumentDetailRequest(docname, doctype, travId);
        } else if (requestCode == STAMP_SELECTED_DOCUMENT) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent it = new Intent();
        it.putExtra(REFRESH, isRefreshRequired);
        setResult(RESULT_CANCELED, it);
        finish();
    }

}
