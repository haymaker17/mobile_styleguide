/**
 * This class is the base class for voucher list, authorization list and stamp-document list pages.
 * 
 * @author sunill
 * */
package com.concur.mobile.gov.expense.doc.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.doc.data.GovDocument;
import com.concur.mobile.gov.expense.doc.service.DocumentListReply;
import com.concur.mobile.gov.expense.doc.service.DocumentListRequest;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.car.activity.GovCarSearchDetail;

public class DocumentListActivity extends BaseActivity {

    protected final static String CLS_TAG = DocumentListActivity.class.getSimpleName();
    protected static final int REFRESH_STAMP_DOCUMENT_LIST = 100;

    public static final String NAME = "name";
    public static final String DOCTYPE = "docType";
    public static final String NEED_STAMPING = "need stamping";
    public static final String DOC_NAME = "docname";
    public static final String EXP_ID = "expId";
    public static final String TRAV_ID = "travId";
    public static final String DOC_LABEL = "documentLabel";
    public static final String GTM_TYPE_FOR_DETAIL = "documentGTMType";
    public static final String AWAITING_STATUS = "awaiting status";

    private DocumentListRequest request = null;
    private DocumentListReceiver receiver;
    private IntentFilter documentIntentFilter = null;

    private String dlgMessage = "";
    private String dlgFailureMsg = "";

    protected boolean isRefreshRequiredFromBooking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore any receivers.
        restoreReceivers();
        String msg = getText(R.string.gov_retrieve_documents).toString();
        setDlgMsg(msg);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isRefreshRequiredFromBooking = extras.getBoolean(GovCarSearchDetail.ISREFRESH);
        }
        setContentView(R.layout.document_list);
    }

    /** set screen title */
    protected void initScreenHeader() {
        getSupportActionBar().setTitle(getHeaderTitle());
    }

    protected String getHeaderTitle() {
        return "Document";
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_SEARCHING_DOCUMENT: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(getMessage());
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
        case com.concur.mobile.gov.util.Const.DIALOG_CREATE_VOUCHER_FROM_AUTH: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.gov_create_voucher));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            dialog = pDialog;
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_CREATE_VOUCHER_FAIL: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.gov_dlg_create_voucher_fail_title);
            // TODO Need to find out what to do here. They can't fix the problem so what detail error do we show?
            dlgBldr.setMessage(getFailureMessage());
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
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
        case com.concur.mobile.gov.util.Const.DIALOG_SEARCHING_DOCUMENT: {
            ProgressDialog pDlg = (ProgressDialog) dialog;
            if (dlgMessage != null) {
                pDlg.setMessage(dlgMessage);
            }
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_CREATE_VOUCHER_FROM_AUTH: {
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_CREATE_VOUCHER_FAIL: {
            break;
        }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Log flurry event
        // restoreReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            if (receiver != null) {
                // Clear activity and we will reassigned.
                receiver.setActivity(null);
                retainer.put(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_RECIEVER_KEY, receiver);
            }
        }
    }

    /**
     * restore any receiver store for this activity into Retainer.
     * */
    protected void restoreReceivers() {
        if (retainer != null) {
            // Restore any segment cancel receiver.
            if (retainer.contains(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_RECIEVER_KEY)) {
                receiver = (DocumentListReceiver) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_RECIEVER_KEY);
                // Reset the activity reference.
                receiver.setActivity(this);
            }
        }
    }

    /**
     * send document list request to get document list from the server.
     * */
    protected void sendDocumentListRequest() {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerDocumentListReceiver();
                request = govService.sendDocumentListRequest();
                if (request == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".getGovDocumentListRequest: unable to create request to get Govt. Documents!");
                    unregisterDocumentListReceiver();
                } else {
                    // set service request.
                    receiver.setServiceRequest(request);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_SEARCHING_DOCUMENT);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * register document receiver
     * */
    protected void registerDocumentListReceiver() {
        if (receiver == null) {
            receiver = new DocumentListReceiver(this);
            if (documentIntentFilter == null) {
                documentIntentFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_GET_DOCUMENT);
            }
            getApplicationContext().registerReceiver(receiver, documentIntentFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerDocumentListReceiver and document filter not null");
        }
    }

    /**
     * un-register document list receiver
     * */
    protected void unregisterDocumentListReceiver() {
        if (receiver != null) {
            getApplicationContext().unregisterReceiver(receiver);
            receiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterDocumentListReceiver is null!");
        }
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling
     * the response for gov. document
     */
    class DocumentListReceiver extends
        BaseBroadcastReceiver<DocumentListActivity, DocumentListRequest>
    {

        private final String CLS_TAG = DocumentListActivity.CLS_TAG + "."
            + DocumentListReceiver.class.getSimpleName();

        protected DocumentListReceiver(DocumentListActivity activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(DocumentListActivity activity) {
            activity.request = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_SEARCHING_DOCUMENT);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            // TODO handle fail case..show dialog...
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();
            final DocumentListReply reply = app.getDocumentListReply();
            if (reply != null) {
                onHandleSuccessForActivity(reply);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
            }
        }

        @Override
        protected void setActivityServiceRequest(DocumentListRequest request) {
            activity.request = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterDocumentListReceiver();
        }
    }

    /**
     * Helper Method : Implement this method in respective activity to build
     * respective page.
     * 
     * @param {@link DocumentListReply} reply
     * 
     * */
    protected void onHandleSuccessForActivity(DocumentListReply reply) {
        // TODO Auto-generated method stub
    }

    /**
     * Check whether we need to refresh our list or not.
     * 
     * @param reply
     *            : Document list reply contains lastRefreshtime.
     * */
    protected boolean isUpdateRequiredForList(DocumentListReply reply) {
        GovAppMobile app = (GovAppMobile) this.getConcurCore();
        if (GovAppMobile.isConnected()) {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            Calendar lastRefresh = reply.lastRefreshTime;
            int minuteDifference = FormatUtil.getMinutesDifference(lastRefresh, now);
            if (app.isDocumentListRefReq()) {
                return true;
            } else if (minuteDifference == -1 || minuteDifference > 2) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected Intent createBundleForDetail(Intent it, GovDocument document) {
        it.putExtra(NAME, document.travelerName);
        it.putExtra(DOCTYPE, document.docType);
        it.putExtra(NEED_STAMPING, document.needsStamping);
        it.putExtra(DOC_NAME, document.docName);
        it.putExtra(TRAV_ID, document.travelerId);
        it.putExtra(DOC_LABEL, document.docTypeLabel);
        it.putExtra(GTM_TYPE_FOR_DETAIL, document.gtmDocType);
        it.putExtra(AWAITING_STATUS, document.approveLabel);
        return it;
    }

    /**
     * generate list item for stamplist adapter using stamp document list
     * 
     * @param stampDocList
     *            : stamp document list
     * @return : list of stamp document list item.
     */
    protected List<DocumentListItem> getStampListItem(List<GovDocument> documentList) {
        List<DocumentListItem> documentListItem = null;
        if (documentList != null) {
            documentListItem = new ArrayList<DocumentListItem>(documentList.size());
            for (GovDocument govDocument : documentList) {
                documentListItem.add(new DocumentListItem(govDocument));
            }
        }
        return documentListItem;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REFRESH_STAMP_DOCUMENT_LIST) {
            if (resultCode == RESULT_OK) {
                updateDocumentList();
            } else if (resultCode == RESULT_CANCELED) {
                boolean isRefresh = data.getExtras().getBoolean(DocumentDetail.REFRESH);
                GovAppMobile app = (GovAppMobile) this.getConcurCore();
                if (isRefresh || app.isDocumentListRefReq()) {
                    updateDocumentList();
                    // after refreshing the list, mark isDocumentListRefReq to FALSE, as document list will have last refresh
                    // time.
                    app.setDocumentListRefReq(false);
                }
            }
        }
    }

    /** update ui.. */
    private void updateDocumentList() {
        String msg = getText(R.string.gov_refresh_documents).toString();
        setDlgMsg(msg);
        sendDocumentListRequest();
    }

    /**
     * Must be override in respective activity
     * 
     * @param reply
     *            : Document List reply reference from cache.
     * */
    protected void updateUIList(DocumentListReply reply) {
        // do nothing.
    }

    /**
     * Set progress dialog message.
     * 
     * @param msg
     *            : message need to be shown when progress dialog shows.
     * */
    protected void setDlgMsg(String msg) {
        dlgMessage = msg;
    }

    /**
     * get progress dialog message.
     * 
     * @return : return progress dialog message.
     */
    protected String getMessage() {
        return dlgMessage;
    }

    /**
     * Set progress dialog message.
     * 
     * @param msg
     *            : message need to be shown when progress dialog shows.
     * */
    protected void setFaliureMsg(String msg) {
        dlgFailureMsg = msg;
    }

    /**
     * get progress dialog message.
     * 
     * @return : return progress dialog message.
     */
    protected String getFailureMessage() {
        return dlgFailureMsg;
    }

    protected List<GovDocument> filterAuthListToCreateVch(List<GovDocument> listOfDoc) {
        List<GovDocument> result = new ArrayList<GovDocument>();
        if (listOfDoc == null || listOfDoc.isEmpty()) {
            return result;
        } else {
            for (GovDocument govDocument : listOfDoc) {
                if (govDocument.authForVch != null && govDocument.authForVch == Boolean.TRUE) {
                    result.add(govDocument);
                }
            }
            return result;
        }
    }
}
