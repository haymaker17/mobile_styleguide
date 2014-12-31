/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.stamp.activity;

import java.util.HashMap;
import java.util.List;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.doc.activity.DocumentDetail;
import com.concur.mobile.gov.expense.doc.activity.DocumentListActivity;
import com.concur.mobile.gov.expense.doc.data.DsDocDetailInfo;
import com.concur.mobile.gov.expense.doc.data.ReasonCodes;
import com.concur.mobile.gov.expense.doc.stamp.data.MttDocument;
import com.concur.mobile.gov.expense.doc.stamp.data.MttReturnTo;
import com.concur.mobile.gov.expense.doc.stamp.data.MttStamps;
import com.concur.mobile.gov.expense.doc.stamp.data.ReasonCodeReqdResponse;
import com.concur.mobile.gov.expense.doc.stamp.service.AvailableStampsRequest;
import com.concur.mobile.gov.expense.doc.stamp.service.DsStampReply;
import com.concur.mobile.gov.expense.doc.stamp.service.StampRequirementInfoRequest;
import com.concur.mobile.gov.expense.doc.stamp.service.StampTMDocumentRequest;
import com.concur.mobile.gov.expense.doc.stamp.service.StampTMDocumentResponse;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GovDBAsyncTask;
import com.concur.mobile.gov.util.GovFlurry;
import com.concur.mobile.gov.util.IGovDBListener;

public class StampDocumentActivity extends BaseActivity implements View.OnClickListener,
    IGovDBListener
{

    private static final String CLS_TAG = StampDocumentActivity.class.getSimpleName();
    private static final String REASON_CODE_RESPONSE = "REASON_CODE_RESPONSE";
    private static final String STAMP_REPLY = "STAMP_REPLY";
    private static final String DOC_DETAIL_INFO = "DOC_DETAIL_INFO";
    private static final String MTT_STAMP = "MTT_STAMP";
    private static final String REASON_CODE = "REASON_CODE";
    private static final String RETURN_TO = "RETURN_TO";
    private static final String STAMP_NAME = "STAMP_NAME";
    private static final String COMMENTS = "COMMENTS";
    private static final String SIGNING_PIN = "SIGNING_PIN";

    private Bundle bundle;

    private AvailableStampsRequest request;
    private StampRequirementInfoRequest stampReqRequest;
    private StampTMDocumentRequest documentRequest;
    private StampListReceiver receiver;
    private StampRequiredInfoReceiver stampReqReceiver;
    private StampDocumentReceiver documentReceiver;

    private IntentFilter stamplistFilter;
    private IntentFilter stampReqInfoFilter;
    private IntentFilter documentFilter;

    // TODO on-rotation store this..
    private ReasonCodeReqdResponse reqdResponse;
    private DsStampReply stampReply;
    private DsDocDetailInfo docDetailInfo;

    private String selectedStampName;
    private String commentsText = "", signingPinText = "";

    private MttStamps selectedMttStamp;
    private ReasonCodes selectedReason;
    private MttReturnTo selectedReturnTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore any receivers.
        setContentView(R.layout.stamp_document);
        initScreenHeader();
        initAwaitingStatus(savedInstanceState);
    }

    /** set screen title */
    private void initScreenHeader() {
        getSupportActionBar().setTitle(getString(R.string.gov_stamp_doc_title).toString());
    }

    /**
     * put awaiting status value
     * 
     * @param savedInstanceState
     */
    private void initAwaitingStatus(Bundle savedInstanceState) {
        bundle = getIntent().getExtras().getBundle(DocumentDetail.BUNDLE);
        if (bundle != null) {
            String status = bundle.getString(DocumentListActivity.AWAITING_STATUS);
            TextView txtView = (TextView) findViewById(R.id.gov_stampdoc_status_value);
            if (txtView != null) {
                txtView.setText(status);
            }
            initValue(savedInstanceState);
            // setViewConfiguration();
        }
    }

    /**
     * initialize value for the view.
     * */
    private void initValue(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (retainer != null) {
                docDetailInfo = (DsDocDetailInfo) retainer.get(DOC_DETAIL_INFO);
                reqdResponse = (ReasonCodeReqdResponse) retainer.get(REASON_CODE_RESPONSE);
                stampReply = (DsStampReply) retainer.get(STAMP_REPLY);
                selectedMttStamp = (MttStamps) retainer.get(MTT_STAMP);
                selectedReason = (ReasonCodes) retainer.get(REASON_CODE);
                selectedReturnTo = (MttReturnTo) retainer.get(RETURN_TO);
                selectedStampName = (String) retainer.get(STAMP_NAME);
                commentsText = (String) retainer.get(COMMENTS);
                signingPinText = (String) retainer.get(SIGNING_PIN);
                onHandleStampReqInfoSuccess();
            } else {
                setViewConfiguration();
            }
        } else {
            setViewConfiguration();
        }
    }

    /** set view configuration. */
    private void setViewConfiguration() {
        String docName = bundle.getString(DocumentListActivity.DOC_NAME);
        String docType = bundle.getString(DocumentListActivity.DOCTYPE);
        String travId = bundle.getString(DocumentListActivity.TRAV_ID);
        GovService service = (GovService) getConcurService();
        GovDBAsyncTask task = new GovDBAsyncTask(docName, docType, travId, service);
        task.setGovDBListener(StampDocumentActivity.this);
        task.execute();
    }

    @Override
    public void onDocDetailListenerSucceeded(Cursor cur) {
        // get docDetailInfo
        if (cur.getCount() > 0) {
            if (cur.moveToFirst()) {
                DsDocDetailInfo info = new DsDocDetailInfo(cur);
                docDetailInfo = info;
                selectedStampName = bundle.getString(DocumentListActivity.AWAITING_STATUS);
                configStampListRequestAndReasonReqInfo();
            } else {
                Log.e(CLS_TAG, " .onDocDetailListenerSucceeded : cursor is not empty but cursor.movetofirst is false");
            }
        } else {
            Log.e(CLS_TAG, " .onDocDetailListenerSucceeded : cursor is null. Something is  in DB table/query wrong.");
        }
    }

    /**
     * Config services.
     * */
    private void configStampListRequestAndReasonReqInfo() {
        String docName = bundle.getString(DocumentListActivity.DOC_NAME);
        String docType = bundle.getString(DocumentListActivity.DOCTYPE);
        String travId = bundle.getString(DocumentListActivity.TRAV_ID);
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerListOfStampReceiver();
                request = govService.sendStampListRequest(docName, docType, travId);
                if (request == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendStampListRequest: unable to create request to get list of available stamp for document!");
                    unregisterListOfStampReceiver();
                } else {
                    // set service request.
                    receiver.setServiceRequest(request);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_LIST);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * register document detail receiver
     * */
    protected void registerListOfStampReceiver() {
        if (receiver == null) {
            receiver = new StampListReceiver(this);
            if (stamplistFilter == null) {
                stamplistFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_GET_LIST_OF_STAMP);
            }
            registerReceiver(receiver, stamplistFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".registerListOfStampReceiver and stamplistFilter filter not null");
        }
    }

    /**
     * unregister document detail receiver
     * */
    protected void unregisterListOfStampReceiver() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterListOfStampReceiver is null!");
        }
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling
     * the response for list of stamp for respective document.
     */
    class StampListReceiver extends
        BaseBroadcastReceiver<StampDocumentActivity, AvailableStampsRequest>
    {

        private final String CLS_TAG = StampDocumentActivity.CLS_TAG + "."
            + StampListReceiver.class.getSimpleName();

        protected StampListReceiver(StampDocumentActivity activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(StampDocumentActivity activity) {
            activity.request = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_LIST);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_LIST_FAIL);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();
            activity.stampReply = app.stampCache.getStampReply();
            onHandleStampListSuccess();
        }

        @Override
        protected void setActivityServiceRequest(AvailableStampsRequest request) {
            activity.request = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterListOfStampReceiver();
        }
    }

    /**
     * After getting stamp list service reply successful check the database to get
     * stamp required information.
     * */
    private void onHandleStampListSuccess() {
        final MttDocument doc = stampReply.document;
        if (doc != null && selectedStampName != null) {
            final GovService service = (GovService) getConcurService();
            new AsyncTask<Void, Void, Cursor>() {

                @Override
                protected Cursor doInBackground(Void... params) {
                    MobileDatabase db = service.getMobileDatabase();
                    Cursor cur = db.loadStampDocumentRequirementInfo(doc.userId, doc.travId, selectedStampName);
                    return cur;
                }

                protected void onPostExecute(Cursor cur) {
                    if (cur.getCount() > 0) {
                        if (cur.moveToFirst()) {
                            ReasonCodeReqdResponse response = new ReasonCodeReqdResponse(cur);
                            reqdResponse = response;
                            onHandleStampReqInfoSuccess();
                        } else {
                            Log.e(CLS_TAG, " .DBASyncTask : cursor is not empty but cursor.movetofirst is false");
                        }
                    } else {
                        MttDocument doc = stampReply.document;
                        if (doc != null && selectedStampName != null) {
                            sendStampRequiredInfoRequest(doc, selectedStampName);
                        }
                    }
                }
            }.execute();
        }
    }

    private void sendStampRequiredInfoRequest(MttDocument doc, String stampname) {
        String docName = FormatUtil.nullCheckForString(doc.docName);
        String docType = FormatUtil.nullCheckForString(doc.docType);
        String travId = FormatUtil.nullCheckForString(doc.travId);
        String stampReqUserId = FormatUtil.nullCheckForString(doc.userId);
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerStampReqReceiver();
                stampReqRequest = govService
                    .sendStampReasonRequiredInfo(docName, docType, travId, stampname, stampReqUserId);
                if (stampReqRequest == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendStampRequiredInfoRequest: unable to create request to get required info for selected stamp name!");
                    unregisterStampReqReceiver();
                } else {
                    // set service request.
                    stampReqReceiver.setServiceRequest(stampReqRequest);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_LIST);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * register document detail receiver
     * */
    protected void registerStampReqReceiver() {
        if (stampReqReceiver == null) {
            stampReqReceiver = new StampRequiredInfoReceiver(this);
            if (stampReqInfoFilter == null) {
                stampReqInfoFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_GET_STAMP_REQ_INFO);
            }
            registerReceiver(stampReqReceiver, stampReqInfoFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".registerStampReqReceiver and stampReqInfoFilter filter not null");
        }
    }

    /**
     * unregister document detail receiver
     * */
    protected void unregisterStampReqReceiver() {
        if (stampReqReceiver != null) {
            unregisterReceiver(stampReqReceiver);
            stampReqReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterStampReqReceiver is null!");
        }
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling
     * the response for list of stamp for respective document.
     */
    class StampRequiredInfoReceiver extends
        BaseBroadcastReceiver<StampDocumentActivity, StampRequirementInfoRequest>
    {

        private final String CLS_TAG = StampDocumentActivity.CLS_TAG + "."
            + StampRequiredInfoReceiver.class.getSimpleName();

        protected StampRequiredInfoReceiver(StampDocumentActivity activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(StampDocumentActivity activity) {
            activity.stampReqRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_LIST);
        }

        @Override
        protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_LIST_FAIL);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleHttpError");
            return true;
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_LIST_FAIL);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();
            activity.reqdResponse = app.stampCache.getStampReqRes();
            onHandleStampReqInfoSuccess();
        }

        @Override
        protected void setActivityServiceRequest(StampRequirementInfoRequest stampReqRequest) {
            activity.stampReqRequest = stampReqRequest;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterStampReqReceiver();
        }
    }

    private void onHandleStampReqInfoSuccess() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.gov_stampdoc_comment_pin_layout);
        MttDocument doc = stampReply.document;
        List<MttStamps> stampList = stampReply.stamps;
        if (doc.sigRequired != null && doc.sigRequired == Boolean.TRUE) {
            layout.setVisibility(View.VISIBLE);
            // set label.
            ViewUtil
                .setTextViewText(this, R.id.gov_stampdoc_comment, R.id.field_name, getString(R.string.gov_stamp_doc_comment), true);
            ViewUtil
                .setTextViewText(this, R.id.gov_stampdoc_signingpin, R.id.field_name, getString(R.string.gov_stamp_doc_signing_pin), true);
            // set on click for layout.
            View v = findViewById(R.id.gov_stampdoc_comment);
            v.setOnClickListener(this);
            v = findViewById(R.id.gov_stampdoc_signingpin);
            v.setOnClickListener(this);
            // set pre-value
            if (commentsText != null) {
                ViewUtil
                    .setTextViewText(this, R.id.gov_stampdoc_comment, R.id.field_value, commentsText, true);
            }
            if (signingPinText != null) {
                setPasswordInputType();
            }
        } else {
            layout.setVisibility(View.GONE);
        }
        setStampFieldsName(stampList);
        // set footer..
        View view = findViewById(R.id.footer);
        view.setVisibility(View.VISIBLE);
        view = ViewUtil.findSubView(this, R.id.footer, R.id.gov_footer_stamp);
        view.setOnClickListener(this);
    }

    private void setStampFieldsName(List<MttStamps> stampList) {
        String value = "";
        // MttStamps mttStamps=null;
        if (stampList != null) {
            final int size = stampList.size();
            for (int i = 0; i < size; i++) {
                MttStamps obj = stampList.get(i);
                if (obj != null) {
                    if (selectedStampName.equalsIgnoreCase(obj.stamp)) {
                        value = obj.stamp;
                        selectedMttStamp = obj;
                        break;
                    }
                }
            }
            // set status to apply label.
            ViewUtil
                .setTextViewText(this, R.id.gov_stampdoc_statustoapply, R.id.field_name, getString(R.string.gov_stamp_doc_status_to_apply), true);
            // set status to apply on click event
            View v = findViewById(R.id.gov_stampdoc_statustoapply);
            v.setOnClickListener(this);
            // set status to apply value
            if (value != null) {
                ViewUtil
                    .setTextViewText(this, R.id.gov_stampdoc_statustoapply, R.id.field_value, value, true);
            } else {
                ViewUtil.setTextViewText(this, R.id.gov_stampdoc_statustoapply, R.id.field_value, "", true);
            }
            // set reason layout
            setReasonCodeLayout();
            // set return to layout
            setReturnToLayout(selectedMttStamp);
        }
    }

    /**
     * set reason code layout
     * 
     * */
    private void setReasonCodeLayout() {
        Boolean isReasonReq = reqdResponse.reasonReqd;
        LinearLayout layout = (LinearLayout) findViewById(R.id.gov_stampdoc_reason_layout);
        if (isReasonReq != null && isReasonReq == Boolean.TRUE) {
            // set return to layout visibility
            layout.setVisibility(View.VISIBLE);
            // set reason label.
            ViewUtil
                .setTextViewText(this, R.id.gov_stampdoc_reason, R.id.field_name, getString(R.string.gov_stamp_doc_reason), true);
            // set reason on click event
            View v = findViewById(R.id.gov_stampdoc_reason);
            v.setOnClickListener(this);
            // set reason value
            if (selectedReason != null && selectedReason.comments.length() > 0) {
                ViewUtil
                    .setTextViewText(StampDocumentActivity.this, R.id.gov_stampdoc_reason, R.id.field_value, selectedReason.comments, true);
            } else {
                ViewUtil
                    .setTextViewText(StampDocumentActivity.this, R.id.gov_stampdoc_reason, R.id.field_value, "", true);
            }
            // TODO
        } else {
            layout.setVisibility(View.GONE);
        }
    }

    /**
     * Set return to layout for the selected stamp.
     * 
     * @param mttStamps
     * */
    private void setReturnToLayout(MttStamps mttStamps) {
        if (mttStamps != null) {
            Boolean returnReq = mttStamps.returntoRequired;
            LinearLayout layout = (LinearLayout) findViewById(R.id.gov_stampdoc_returnTo_layout);
            if (returnReq != null && returnReq == Boolean.TRUE) {
                // set return to layout visibility
                layout.setVisibility(View.VISIBLE);
                // set return to label.
                ViewUtil
                    .setTextViewText(this, R.id.gov_stampdoc_returnTo, R.id.field_name, getString(R.string.gov_stamp_doc_returnto), true);
                // set return to on click event
                View v = findViewById(R.id.gov_stampdoc_returnTo);
                v.setOnClickListener(this);
                // set return to value
                if (selectedReturnTo != null && selectedReturnTo.returntoName.length() > 0) {
                    ViewUtil
                        .setTextViewText(StampDocumentActivity.this, R.id.gov_stampdoc_returnTo, R.id.field_value, selectedReturnTo.returntoName, true);
                } else {
                    ViewUtil.setTextViewText(this, R.id.gov_stampdoc_returnTo, R.id.field_value, "", true);
                }
            } else {
                layout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.gov_stampdoc_statustoapply:
            setColorForLabelField(R.id.gov_stampdoc_statustoapply, R.id.field_name, R.color.FormLabel);
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_STAMPLIST);
            break;
        case R.id.gov_stampdoc_reason:
            setColorForLabelField(R.id.gov_stampdoc_reason, R.id.field_name, R.color.FormLabel);
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_REASONLIST);
            break;
        case R.id.gov_stampdoc_signingpin:
            setColorForLabelField(R.id.gov_stampdoc_signingpin, R.id.field_name, R.color.FormLabel);
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_GOV_SIGNING_PIN);
            break;
        case R.id.gov_stampdoc_returnTo:
            setColorForLabelField(R.id.gov_stampdoc_returnTo, R.id.field_name, R.color.FormLabel);
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_RETURN_TO_LIST);
            break;
        case R.id.gov_stampdoc_comment:
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_GOV_COMMENTS);
            break;
        case R.id.gov_footer_stamp:
            onclickStamp();
        default:
            break;
        }
    }

    /**
     * set color for the field name.
     * 
     * @param parentId
     *            : parent layout
     * @param childId
     *            : child layout
     * @param colorId
     *            : which color you need to set.
     */
    private void setColorForLabelField(int parentId, int childId, int colorId) {
        TextView txtView = (TextView) ViewUtil.findSubView(StampDocumentActivity.this, parentId, childId);
        txtView.setTextColor(getResources().getColor(colorId));
    }

    /**
     * on click stamp button.
     * */
    private void onclickStamp() {
        String reason = ViewUtil.getTextViewText(this, R.id.gov_stampdoc_reason, R.id.field_value).trim();
        String status = ViewUtil.getTextViewText(this, R.id.gov_stampdoc_statustoapply, R.id.field_value)
            .trim();
        String returnTo = ViewUtil.getTextViewText(this, R.id.gov_stampdoc_returnTo, R.id.field_value)
            .trim();
        signingPinText = FormatUtil.escapeForXML(signingPinText);
        commentsText = FormatUtil.escapeForXML(commentsText);
        MttDocument document = stampReply.document;
        Boolean signingReq = document.sigRequired;
        boolean isValidate = checkValidation(reason, status, returnTo, signingPinText, commentsText, signingReq);
        if (isValidate) {
            if (GovAppMobile.isConnected()) {
                GovService govService = (GovService) getConcurService();
                if (govService != null) {
                    registerStampDocumentReceiver();
                    documentRequest = govService
                        .sendStampGovDocumentReq(document, signingPinText, commentsText, selectedMttStamp, selectedReason, selectedReturnTo);
                    if (documentRequest == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG
                            + ".onClickStamp: unable to create request to stamp selected document!");
                        unregisterStampDocumentReceiver();
                    } else {
                        // set service request.
                        documentReceiver.setServiceRequest(documentRequest);
                        // Show the progress dialog.
                        showDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC);
                    }
                } else {
                    Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
                }
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        }
    }

    /**
     * check field validation
     * 
     * @param signingReq
     * @param commentsText
     * @param signingPinText
     * @param returnTo
     * @param status
     * @param reason
     * @return false: if error occurred.
     */
    private boolean checkValidation(String reason, String status, String returnTo,
        String signingPinText, String commentsText, Boolean signingReq)
    {
        boolean returnValue = true;
        if (status == null || status.length() <= 0) {
            TextView txtView = (TextView) ViewUtil
                .findSubView(StampDocumentActivity.this, R.id.gov_stampdoc_statustoapply, R.id.field_name);
            txtView.setError("");
            setColorForLabelField(R.id.gov_stampdoc_statustoapply, R.id.field_name, R.color.ErrorTextColor);
            returnValue = false;
        }
        View v = findViewById(R.id.gov_stampdoc_comment_pin_layout);
        if (v.getVisibility() == View.VISIBLE && signingReq) {
            if (signingPinText == null || signingPinText.length() <= 0) {
                setColorForLabelField(R.id.gov_stampdoc_signingpin, R.id.field_name, R.color.ErrorTextColor);
                returnValue = false;
            }
        } else {
            this.signingPinText = "";
        }
        v = findViewById(R.id.gov_stampdoc_reason_layout);
        if (v.getVisibility() == View.VISIBLE) {
            if (reason == null || reason.length() <= 0) {
                setColorForLabelField(R.id.gov_stampdoc_reason, R.id.field_name, R.color.ErrorTextColor);
                showDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_REASONLIST);
                returnValue = false;
            }
        } else {
            selectedReason = null;
        }
        v = findViewById(R.id.gov_stampdoc_returnTo_layout);
        if (v.getVisibility() == View.VISIBLE) {
            if (returnTo == null || returnTo.length() <= 0) {
                setColorForLabelField(R.id.gov_stampdoc_returnTo, R.id.field_name, R.color.ErrorTextColor);
                returnValue = false;
            }
        } else {
            selectedReturnTo = null;
        }
        return returnValue;
    }

    /**
     * register document detail receiver
     * */
    protected void registerStampDocumentReceiver() {
        if (documentReceiver == null) {
            documentReceiver = new StampDocumentReceiver(this);
            if (documentFilter == null) {
                documentFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_STAMP_DOC);
            }
            ((GovAppMobile) getApplication()).registerReceiver(documentReceiver, documentFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".documentReceiver and documentFilter filter is null");
        }
    }

    /**
     * unregister document detail receiver
     * */
    protected void unregisterStampDocumentReceiver() {
        if (documentReceiver != null) {
            ((GovAppMobile) getApplication()).unregisterReceiver(documentReceiver);
            documentReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterStampDocumentReceiver is null!");
        }
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling
     * the response for stamp document.
     */
    class StampDocumentReceiver extends
        BaseBroadcastReceiver<StampDocumentActivity, StampTMDocumentRequest>
    {

        private final String CLS_TAG = StampDocumentActivity.CLS_TAG + "."
            + StampDocumentReceiver.class.getSimpleName();

        protected StampDocumentReceiver(StampDocumentActivity activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(StampDocumentActivity activity) {
            activity.documentRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_FAILURE);
            logFlurryEvents(false);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getApplication();
            StampTMDocumentResponse reply = app.stampCache.getStampTMDocumentResponse();
            Boolean errorFlag = reply.response.errorFlag;
            /*
             * if(errorFlag!=null && errorFlag==Boolean.TRUE){
             * onHandleStampDocumentSuccess();
             * }else{
             * String msg = reply.response.errorDesc;
             * if(msg!=null && msg.length()>0){
             * intent.putExtra(Const.REPLY_ERROR_MESSAGE, msg);
             * }
             * handleFailure(context, intent);
             * }
             */
            onHandleStampDocumentSuccess();
        }

        @Override
        protected void setActivityServiceRequest(StampTMDocumentRequest documentRequest) {
            activity.documentRequest = documentRequest;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterStampDocumentReceiver();
        }
    }

    /**
     * Handle successful result after stamp the document successfully. Refresh
     * list.
     * */
    private void onHandleStampDocumentSuccess() {
        // refresh list
        setResult(RESULT_OK);
        logFlurryEvents(true);
        finish();
    }

    private void setPasswordInputType() {
        TextView txtView = (TextView) ViewUtil
            .findSubView(StampDocumentActivity.this, R.id.gov_stampdoc_signingpin, R.id.field_value);
        if (txtView != null) {
            txtView.setText(signingPinText);
            txtView.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    /**
     * Log flurry events that do you successfully created new auth or not
     * */
    private void logFlurryEvents(boolean isSuccessful) {
        Map<String, String> params = new HashMap<String, String>();
        if (isSuccessful) {
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_YES);
        } else {
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_NO);
        }
        if (signingPinText != null) {
            params.put(GovFlurry.PARAM_NAME_STAMP_DOCUMENT_WITH_PIN, Flurry.PARAM_VALUE_YES);
        } else {
            params.put(GovFlurry.PARAM_NAME_STAMP_DOCUMENT_WITH_PIN, Flurry.PARAM_VALUE_NO);
        }
        String docType = bundle.getString(DocumentListActivity.DOCTYPE);
        if (docType != null) {
            params.put(GovFlurry.PARAM_NAME_DOCUMENT_TYPE, docType);
        }
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_STAMP_DOCUMENT,
            GovFlurry.EVENT_STAMP_DOCUMENT, params);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_LIST: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.gov_retrieve_document_stamp_list));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    // Dismiss the dialog.
                    dialog.dismiss();
                    // Attempt to cancel the request.
                    if (request != null) {
                        request.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: perDiemRateRequest is null!");
                    }
                }
            });
            dialog = pDialog;
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_LIST_FAIL: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.gov_stamp_doc_fail_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            dialog = dlgBldr.create();
            break;
        }

        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.gov_stamp_document_progress));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    // Dismiss the dialog.
                    dialog.dismiss();
                    // Attempt to cancel the request.
                    if (documentRequest != null) {
                        documentRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: perDiemRateRequest is null!");
                    }
                }
            });
            dialog = pDialog;
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_STAMPLIST: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.gov_stamp_doc_stamplist_prompt);
            // TODO Override toString() in MttStamps to show listitem.
            final ArrayAdapter<MttStamps> adapter = new ArrayAdapter<MttStamps>(this,
                R.layout.stamp_document_list_row, R.id.stampdoc_stamp_name, stampReply.stamps);
            builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_STAMPLIST);
                    if (which != -1) {
                        selectedMttStamp = adapter.getItem(which);
                        selectedStampName = selectedMttStamp.stamp;
                        configStampListRequestAndReasonReqInfo();
                    }
                }
            });
            AlertDialog alertDlg = builder.create();
            dialog = alertDlg;
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_REASONLIST: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.gov_stamp_doc_reasonlist_prompt);
            // TODO Override toString() in ReasonCodes to show listitem.
            List<ReasonCodes> list = docDetailInfo.reasonCodeList;
            final ArrayAdapter<ReasonCodes> adapter = new ArrayAdapter<ReasonCodes>(this,
                R.layout.stamp_document_list_row, R.id.stampdoc_stamp_name, list);
            builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_REASONLIST);
                    if (which != -1) {
                        selectedReason = adapter.getItem(which);
                        ViewUtil
                            .setTextViewText(StampDocumentActivity.this, R.id.gov_stampdoc_reason, R.id.field_value, selectedReason.comments, true);
                        setColorForLabelField(R.id.gov_stampdoc_reason, R.id.field_name, R.color.FormLabel);
                    }
                }
            });
            AlertDialog alertDlg = builder.create();
            dialog = alertDlg;
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_GOV_COMMENTS: {
            final View layout = View.inflate(this, R.layout.gov_comment, null);
            final EditText comments = ((EditText) layout.findViewById(R.id.edittext_value));
            comments.setMinLines(3);
            comments.setGravity(Gravity.TOP | Gravity.LEFT);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.gov_stamp_doc_comment_promptmsg));
            builder.setPositiveButton(getString(R.string.okay), new Dialog.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    String myTextString = comments.getText().toString().trim();
                    removeDialog(com.concur.mobile.gov.util.Const.DIALOG_GOV_COMMENTS);
                    commentsText = myTextString;
                    ViewUtil
                        .setTextViewText(StampDocumentActivity.this, R.id.gov_stampdoc_comment, R.id.field_value, commentsText, true);
                }
            });
            builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    removeDialog(com.concur.mobile.gov.util.Const.DIALOG_GOV_COMMENTS);
                }
            });
            builder.setView(layout);
            dialog = builder.create();
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_GOV_SIGNING_PIN: {
            final View layout = View.inflate(this, R.layout.gov_comment, null);
            final EditText signingPin = ((EditText) layout.findViewById(R.id.edittext_value));
            signingPin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.gov_stamp_doc_signing_pin_promptmsg));
            builder.setPositiveButton(getString(R.string.okay), new Dialog.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    String myTextString = signingPin.getText().toString().trim();
                    removeDialog(com.concur.mobile.gov.util.Const.DIALOG_GOV_SIGNING_PIN);
                    signingPinText = myTextString;
                    setPasswordInputType();
                }
            });
            builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    removeDialog(com.concur.mobile.gov.util.Const.DIALOG_GOV_SIGNING_PIN);
                    setPasswordInputType();
                }
            });
            builder.setView(layout);
            dialog = builder.create();
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_RETURN_TO_LIST: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.gov_stamp_doc_returntot_prompt);
            // TODO Override toString() in MttReturnTo to show listitem.
            List<MttReturnTo> list = stampReply.returnTos;
            final ArrayAdapter<MttReturnTo> adapter = new ArrayAdapter<MttReturnTo>(this,
                R.layout.stamp_document_list_row, R.id.stampdoc_stamp_name, list);
            builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_RETURN_TO_LIST);
                    if (which != -1) {
                        selectedReturnTo = adapter.getItem(which);
                        ViewUtil
                            .setTextViewText(StampDocumentActivity.this, R.id.gov_stampdoc_returnTo, R.id.field_value, selectedReturnTo.returntoName, true);
                    }
                }
            });
            AlertDialog alertDlg = builder.create();
            dialog = alertDlg;
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_FAILURE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.gov_stamp_doc_fail_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        case Const.DIALOG_SYSTEM_UNAVAILABLE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_system_unavailable_title);
            dlgBldr.setMessage(getText(R.string.dlg_system_unavailable_message));
            dlgBldr.setPositiveButton(this.getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    setResult(RESULT_CANCELED);
                    finish();
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
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_LIST: {
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC: {
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_STAMPLIST: {
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_REASONLIST: {
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_LIST_FAIL: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (actionStatusErrorMessage != null) {
                alertDlg.setMessage(actionStatusErrorMessage);
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_STAMP_DOC_FAILURE: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (actionStatusErrorMessage != null) {
                alertDlg.setMessage(actionStatusErrorMessage);
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_GOV_COMMENTS: {
            TextView txtView = (TextView) ViewUtil
                .findSubView(this, R.id.gov_stampdoc_comment, R.id.field_value);
            if (txtView != null) {
                String comment = ViewUtil
                    .getTextViewText(this, R.id.gov_stampdoc_comment, R.id.field_value);
                String txtVal = (comment != null) ? comment : "";
                if (commentsText != null) {
                    txtVal = commentsText;
                }
                txtView.setText(txtVal);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: field_value is null!");
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_GOV_SIGNING_PIN: {
            TextView txtView = (TextView) ViewUtil
                .findSubView(this, R.id.gov_stampdoc_signingpin, R.id.field_value);
            if (txtView != null) {
                String pin = ViewUtil.getTextViewText(this, R.id.gov_stampdoc_signingpin, R.id.field_value);
                String txtVal = (pin != null) ? pin : "";
                if (signingPinText != null) {
                    txtVal = signingPinText;
                }
                txtView.setText(txtVal);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: field_value is null!");
            }
            break;
        }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            if (receiver != null) {
                // Clear activity and we will reassigned.
                receiver.setActivity(null);
                retainer.put(com.concur.mobile.gov.util.Const.RETAINER_STAMP_LIST_RECEIVER_KEY, receiver);
            }
            if (stampReqReceiver != null) {
                // Clear activity and we will reassigned.
                stampReqReceiver.setActivity(null);
                retainer
                    .put(com.concur.mobile.gov.util.Const.RETAINER_STAMP_REQ_RESPONSE_RECEIVER_KEY, stampReqReceiver);
            }
            if (documentReceiver != null) {
                // Clear activity and we will reassigned.
                documentReceiver.setActivity(null);
                retainer
                    .put(com.concur.mobile.gov.util.Const.RETAINER_STAMP_DOCUMENT_RECEIVER_KEY, documentReceiver);
            }
        }
    }

    /**
     * restore any receiver store for this activity into Retainer.
     * */
    protected void restoreReceivers() {
        if (retainer != null) {
            if (retainer.contains(com.concur.mobile.gov.util.Const.RETAINER_STAMP_LIST_RECEIVER_KEY)) {
                receiver = (StampListReceiver) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_STAMP_LIST_RECEIVER_KEY);
                // Reset the activity reference.
                receiver.setActivity(this);
            }
            if (retainer.contains(com.concur.mobile.gov.util.Const.RETAINER_STAMP_REQ_RESPONSE_RECEIVER_KEY)) {
                stampReqReceiver = (StampRequiredInfoReceiver) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_STAMP_REQ_RESPONSE_RECEIVER_KEY);
                // Reset the activity reference.
                stampReqReceiver.setActivity(this);
            }
            if (retainer.contains(com.concur.mobile.gov.util.Const.RETAINER_STAMP_DOCUMENT_RECEIVER_KEY)) {
                documentReceiver = (StampDocumentReceiver) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_STAMP_DOCUMENT_RECEIVER_KEY);
                // Reset the activity reference.
                documentReceiver.setActivity(this);
            }
        }
    }

    protected void onSaveInstanceState(Bundle savedInstance) {
        super.onSaveInstanceState(savedInstance);
        if (retainer != null) {
            if (docDetailInfo != null) {
                retainer.put(DOC_DETAIL_INFO, docDetailInfo);
            }
            if (reqdResponse != null) {
                retainer.put(REASON_CODE_RESPONSE, reqdResponse);
            }
            if (stampReply != null) {
                retainer.put(STAMP_REPLY, stampReply);
            }
            if (selectedMttStamp != null) {
                retainer.put(MTT_STAMP, selectedMttStamp);
            }
            if (selectedReason != null) {
                retainer.put(REASON_CODE, selectedReason);
            }
            if (selectedReturnTo != null) {
                retainer.put(RETURN_TO, selectedReturnTo);
            }
            if (selectedStampName != null && selectedStampName.length() > 0) {
                retainer.put(STAMP_NAME, selectedStampName);
            }
            if (commentsText != null && commentsText.length() > 0) {
                retainer.put(COMMENTS, commentsText);
            }
            if (signingPinText != null && signingPinText.length() > 0) {
                retainer.put(SIGNING_PIN, signingPinText);
            }
        }
    }
}
