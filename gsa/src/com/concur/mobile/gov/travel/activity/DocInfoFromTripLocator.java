package com.concur.mobile.gov.travel.activity;

import org.apache.http.HttpStatus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.doc.activity.DocumentListActivity;
import com.concur.mobile.gov.expense.doc.authorization.activity.AuthorizationDetailActivity;
import com.concur.mobile.gov.expense.doc.data.GovDocument;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.service.DocInfoFromTripLocatorReply;
import com.concur.mobile.gov.travel.service.DocInfoFromTripLocatorRequest;
import com.concur.mobile.gov.util.TravelBookingCache;

public class DocInfoFromTripLocator extends BaseActivity {

    private static final String CLS_TAG = DocInfoFromTripLocator.class.getSimpleName();

    private static final String GOV_DOC_SEARCH_RECEIVER_KEY = "gov.doc.search.receiver";

    // protected String bookingType;
    protected String tripLocator;
    protected String travId;
    protected String taNumber;

    private DocInfoFromTripLocatorRequest request;
    private DocInfoReceiver receiver;
    protected final IntentFilter filter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_GET_DOCUMENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.docinfo_progress);

        initUI();

        // Restore any receivers.
        restoreReceivers();

        startSearch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (receiver != null) {
            // Clear the activity reference, it will be set in the new HotelSearch instance.
            receiver.setActivity(null);
            retainer.put(GOV_DOC_SEARCH_RECEIVER_KEY, receiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        if (retainer.contains(GOV_DOC_SEARCH_RECEIVER_KEY)) {
            receiver = (DocInfoReceiver) retainer.get(GOV_DOC_SEARCH_RECEIVER_KEY);
            receiver.setActivity(this);
        }
    }

    protected void initUI() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Bundle result = bundle.getBundle(GovSegmentList.BUNDLE);
            taNumber = extras.getString(GovSegmentList.AUTH_NUM);
            travId = extras.getString(GovSegmentList.TRAV_ID);
            tripLocator = extras.getString(GovSegmentList.TRIP_ID);
        } else {
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL);
        }

    }

    protected void startSearch() {

        // Make the call
        GovService svc = (GovService) getConcurService();
        if (svc != null) {
            if (receiver == null) {
                receiver = new DocInfoReceiver(this);
            }
            getApplicationContext().registerReceiver(receiver, filter);
            svc.getDocInfoFromTripLocator(taNumber, travId, tripLocator);
            receiver.setRequest(request);
        }

    }

    /**
     * A broadcast receiver for handling the result of a car search.
     * 
     * @author AndrewK
     */
    protected class DocInfoReceiver extends BroadcastReceiver {

        private DocInfoFromTripLocator activity;

        private DocInfoFromTripLocatorRequest request;

        private Intent intent;

        /**
         * Constructs an instance of <code>CarSearchReceiver</code> with a search
         * request object.
         * 
         * @param hotelSearch
         */
        DocInfoReceiver(DocInfoFromTripLocator activity) {
            this.activity = activity;
        }

        /**
         * Sets the hotel search activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the hotel search activity associated with this broadcast receiver.
         */
        void setActivity(DocInfoFromTripLocator activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.request = request;
                if (this.intent != null) {
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        void setRequest(DocInfoFromTripLocatorRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        public void onReceive(Context context, Intent i) {

            // Does this receiver have a current activity?
            if (activity != null) {
                activity.getApplicationContext().unregisterReceiver(this);
                int serviceRequestStatus = i.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = i.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (i.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                    GovAppMobile app = (GovAppMobile) getApplication();
                                    TravelBookingCache cache = app.trvlBookingCache;
                                    DocInfoFromTripLocatorReply reply = cache.getDocumentListReply();
                                    if (reply != null && reply.document != null) {
                                        GovDocument document = reply.document;
                                        Intent it = new Intent(DocInfoFromTripLocator.this, AuthorizationDetailActivity.class);
                                        it.putExtra(DocumentListActivity.NAME, document.travelerName);
                                        it.putExtra(DocumentListActivity.DOCTYPE, document.docType);
                                        it.putExtra(DocumentListActivity.NEED_STAMPING, document.needsStamping);
                                        it.putExtra(DocumentListActivity.DOC_NAME, document.docName);
                                        it.putExtra(DocumentListActivity.TRAV_ID, document.travelerId);
                                        it.putExtra(DocumentListActivity.DOC_LABEL, document.docTypeLabel);
                                        it.putExtra(DocumentListActivity.GTM_TYPE_FOR_DETAIL, document.gtmDocType);
                                        it.putExtra(DocumentListActivity.AWAITING_STATUS, document.approveLabel);
                                        // as booking is successful remove all the cache data
                                        app.trvlBookingCache = new TravelBookingCache();
                                        activity.setResult(RESULT_OK);
                                        activity.startActivity(it);
                                        activity.finish();
                                    } else {
                                        showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL);
                                    }

                                } else {
                                    activity.actionStatusErrorMessage = i.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                        + activity.actionStatusErrorMessage + ".");
                                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL);
                                }
                            } else {
                                activity.lastHttpErrorMessage = i.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                    + activity.lastHttpErrorMessage + ".");
                                showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                            showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL);
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            Log.e(Const.LOG_TAG,
                                CLS_TAG + ".onReceive: service request error -- "
                                    + i.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                            showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL);
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL);
                }
                activity.request = null;
            } else {
                this.intent = i;
            }
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.gov_retrieve_documents_fail_title);
            dlgBldr.setMessage("");
            dlgBldr.setCancelable(false);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
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
        case com.concur.mobile.gov.util.Const.DIALOG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (actionStatusErrorMessage != null) {
                alertDlg.setMessage(actionStatusErrorMessage);
            } else {
                actionStatusErrorMessage = getString(R.string.gov_retrieve_documents_fail_msg);
                alertDlg.setMessage(actionStatusErrorMessage);
            }
            break;
        }
        }
    }

    @Override
    public void onBackPressed() {
        // dont do anything.
    }

}
