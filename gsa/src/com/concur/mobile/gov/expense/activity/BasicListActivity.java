/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.activity;

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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.doc.activity.DocumentDetail;
import com.concur.mobile.gov.expense.doc.activity.DocumentListActivity;
import com.concur.mobile.gov.expense.doc.data.DsDocDetailInfo;
import com.concur.mobile.gov.expense.doc.service.DocumentDetailRequest;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GovDBAsyncTask;
import com.concur.mobile.gov.util.IGovDBListener;
import com.concur.mobile.platform.util.Format;

public class BasicListActivity extends BaseActivity implements IGovDBListener {

    protected final static String CLS_TAG = BasicListActivity.class.getSimpleName();
    protected static final String GTM_AUTH = "AUTH";
    protected static final String GTM_VCH = "VCH";

    public static final String RETAINER_DOC_DETAIL = "retainer.documentlist.document.detailreciever.key";

    private DocumentDetailRequest docDetailRequest = null;
    private DocumentDetailReceiver docDetailReceiver;
    private IntentFilter documentDetailIntentFilter = null;

    public static final String BUNDLE = "bundle";

    protected Bundle bundle;

    protected DsDocDetailInfo docDetailInfo;

    protected String docname, doctype, travId;

    protected String gtmType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore any receivers.
        // restoreReceivers();
        setContentView(getLayout());
        bundle = getIntent().getExtras().getBundle(DocumentDetail.BUNDLE);
        if (bundle != null) {
            docname = bundle.getString(DocumentListActivity.DOC_NAME);
            doctype = bundle.getString(DocumentListActivity.DOCTYPE);
            travId = bundle.getString(DocumentListActivity.TRAV_ID);
            gtmType = bundle.getString(DocumentListActivity.GTM_TYPE_FOR_DETAIL);
            GovService service = (GovService) getConcurService();
            GovDBAsyncTask task = new GovDBAsyncTask(docname, doctype, travId, service);
            task.setGovDBListener(BasicListActivity.this);
            task.execute();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + " .onCreate : bundle is null, so can not show anything to new screen!"
                + "finishing activity go back and try again...");
            finish();
        }
    }

    @Override
    public void onDocDetailListenerSucceeded(Cursor cur) {
        if (cur.getCount() > 0) {
            if (cur.moveToFirst()) {
                docDetailInfo = new DsDocDetailInfo(cur);
                if (docDetailInfo != null) {
                    buildView();
                } else {
                    Log.e(CLS_TAG, " .BasicListActivity : info from cursor is null. Something is  in DB table/query wrong.");
                }
            } else {
                Log.e(CLS_TAG, " .BasicListActivity : cursor is not empty but cursor.movetofirst is false");
            }
        } else {
            Log.e(CLS_TAG, " .BasicListActivity : cursor is null. Something is  in DB table/query wrong.");
        }
    }

    /**
     * get layout resource id for the screen.
     * 
     * @return resource id
     * */
    protected int getLayout() {
        return R.layout.drill_in_options;
    }

    /**
     * set up your activity screen here.
     * */
    private void buildView() {
        initScreenHeader();
        initOtherInformation();
    }

    /**
     * This method set screen header title.
     * 
     * */
    protected void initScreenHeader() {
        getSupportActionBar().setTitle(getTitleText());
    }

    protected void initOtherInformation() {
        TextView txtView = (TextView) findViewById(R.id.gov_drillin_traveller_name);
        if (txtView != null) {
            txtView.setText(getTravellerName());
        }
        txtView = (TextView) findViewById(R.id.gov_drillin_traveller_totalAmt);
        if (txtView != null) {
            txtView.setText(getTravellerAmount());
        }
        txtView = (TextView) findViewById(R.id.gov_drillin_doc_type);
        if (txtView != null) {
            txtView.setText(getGovDocDetailType());
        }
        txtView = (TextView) findViewById(R.id.gov_drillin_trip_date);
        if (txtView != null) {
            txtView.setText(getGovDocDetailTripDate());
        }
        txtView = (TextView) findViewById(R.id.gov_trip_drillin_doclabelType);
        if (txtView != null) {
            txtView.setText(getDocLabel());
        }
        ImageView img = (ImageView) findViewById(R.id.gov_drillin_trip_receipt);
        if (img != null) {
            setIconReceiptVisibility(img);
        }
        img = (ImageView) findViewById(R.id.gov_drillin_trip_violation);
        if (img != null) {
            setIconViolation(img);
        }
        View footerView = findViewById(R.id.footer);
        showHideBottomBar(footerView);
        configureListItems();
    }

    /**
     * May be override in respective class to get document label.
     * */
    protected String getDocLabel() {
        String lbl = bundle.getString(DocumentListActivity.DOC_LABEL);
        return FormatUtil.nullCheckForString(lbl);
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
        img.setVisibility(View.INVISIBLE);
        return;
    }

    /**
     * show or hide footer bar. This method May be override in respective
     * activity.
     * 
     * @param footerView
     * */
    // we dont know we need to show footer bar in this page or not.
    // so set it as invisible for now.
    protected void showHideBottomBar(View footerView) {
        boolean needStamping = bundle.getBoolean(DocumentListActivity.NEED_STAMPING);
        if (needStamping) {
            footerView.setVisibility(View.GONE);
        } else {
            footerView.setVisibility(View.GONE);
        }
    }

    /**
     * Configure List items.
     * */
    protected void configureListItems() {
    }

    /**
     * send document detail request to get document detail from the server.
     * 
     * @param documentListItem
     * */
    protected void sendDocumentDetailRequest(String docname, String doctype, String travID) {
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
        BaseBroadcastReceiver<BasicListActivity, DocumentDetailRequest>
    {

        private final String CLS_TAG = BasicListActivity.CLS_TAG + "."
            + DocumentDetailReceiver.class.getSimpleName();

        protected DocumentDetailReceiver(BasicListActivity activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(BasicListActivity activity) {
            activity.docDetailRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_DETAIL_DOCUMENT);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_DETAIL_DOCUMENT_FAIL);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
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
        case com.concur.mobile.gov.util.Const.DIALOG_DETAIL_DOCUMENT_FAIL: {
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
        case com.concur.mobile.gov.util.Const.DIALOG_DETAIL_DOCUMENT_FAIL: {
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
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            if (docDetailReceiver != null) {
                // Clear activity and we will reassign.
                docDetailReceiver.setActivity(null);
                retainer.put(RETAINER_DOC_DETAIL, docDetailReceiver);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    /**
     * Restore any reciever store for this activity into Retainer.
     */
    protected void restoreReceivers() {
        if (retainer != null) {
            if (retainer.contains(RETAINER_DOC_DETAIL)) {
                docDetailReceiver = (DocumentDetailReceiver) retainer
                    .get(RETAINER_DOC_DETAIL);

                // Reset the activity reference
                docDetailReceiver.setActivity(this);
            }
        }
    }
}
