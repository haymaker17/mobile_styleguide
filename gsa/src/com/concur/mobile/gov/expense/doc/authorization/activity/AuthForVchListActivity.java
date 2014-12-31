/**
 * Show Authorization list using this class.
 * Set Authorization view, list, list item click events.
 * 
 * @author sunill
 * */
package com.concur.mobile.gov.expense.doc.authorization.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.doc.activity.DocumentListItem;
import com.concur.mobile.gov.expense.doc.data.GovDocument;
import com.concur.mobile.gov.expense.doc.service.DocumentListReply;
import com.concur.mobile.gov.expense.doc.service.GetAuthForVchDocListRequest;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GovFlurry;

public class AuthForVchListActivity extends BaseActivity {

    private static final String CLS_TAG = AuthForVchListActivity.class.getSimpleName();

    // Indicate whether the list is being accessed in select mode (as for creating a voucher from an authorization)
    protected boolean isSingleSelect;

    private List<GovDocument> authorizationList;
    private ListView listView;
    private ListItemAdapter<DocumentListItem> authorizationAdapter;
    private RelativeLayout noDataView;
    private DocumentListItem selectedDocumentListItem;

    private GetAuthForVchDocListRequest request = null;
    private DocumentListReceiver receiver;
    private IntentFilter documentIntentFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Flag this as just a selectable list if the extra is set
        isSingleSelect = getIntent().getBooleanExtra(com.concur.mobile.gov.util.Const.EXTRA_SINGLE_SELECT, false);

        restoreReceivers();

        setContentView(R.layout.document_list);

        getSupportActionBar().setTitle(getHeaderTitle());
        initValue(savedInstanceState);
    }

    protected String getHeaderTitle() {
        return getString(R.string.gov_authorization_select_title);
    }

    /**
     * initialize value for the view.
     * */
    @SuppressWarnings("unchecked")
    private void initValue(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (retainer != null) {
                authorizationList = (List<GovDocument>) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_AUTH_FOR_VCH_LIST_KEY);
                buildView(authorizationList);
            } else {
                setViewConfiguration();
            }
        } else {
            setViewConfiguration();
        }
    }

    /** set view configuration. filter the list and set build the view */
    private void setViewConfiguration() {
        GovAppMobile app = (GovAppMobile) getApplication();
        DocumentListReply reply = app.getAuthForVchDocumentList();
        if (reply != null) {
            if (isUpdateRequiredForList(reply)) {
                getAuthListToCreateVch();
            } else {
                authorizationList = reply.documentList;
                buildView(authorizationList);
            }
        } else {
            getAuthListToCreateVch();
        }
    }

    /** build view */
    private void buildView(List<GovDocument> authorizationList) {
        int count = 0;
        if (authorizationList != null) {
            initDocumentList(authorizationList);
            count = authorizationList.size();
        } else {
            showNoDataView();
        }
        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(GovFlurry.PARAM_NAME_AUTH_COUNT, Integer.toString(count));
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_LIST, GovFlurry.EVENT_VIEW_AUTH, params);
    }

    /** initialize list and set adapter */
    protected void initDocumentList(List<GovDocument> authorizationList) {
        noDataView = (RelativeLayout) findViewById(R.id.document_no_list_view);
        listView = (ListView) findViewById(R.id.document_list_view);
        if (authorizationList == null || authorizationList.size() <= 0) {
            showNoDataView();
        } else {
            noDataView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            if (listView != null) {
                if (authorizationList != null) {
                    List<DocumentListItem> authorizationListItem = getDocListItem(authorizationList);
                    authorizationAdapter = new ListItemAdapter<DocumentListItem>(this, authorizationListItem);
                    listView.setAdapter(authorizationAdapter);
                    listView.setOnItemClickListener(new OnItemClickListener() {

                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedDocumentListItem = (DocumentListItem) listView.getItemAtPosition(position);
                            if (isSingleSelect) {
                                // Grab the details and finish
                                GovDocument doc = selectedDocumentListItem.getDocument();
                                Intent data = new Intent();
                                data.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_TRAVELER_ID, doc.travelerId);
                                data.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_AUTH_NAME, doc.docName);
                                data.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_AUTH_TYPE, doc.docType);
                                setResult(Activity.RESULT_OK, data);

                                finish();

                            }
                        }
                    });
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initDocumentList: unable to find list view!");
            }
        }
    }

    /** set the view where no data is available */
    private void showNoDataView() {
        noDataView = (RelativeLayout) findViewById(R.id.document_no_list_view);
        listView = (ListView) findViewById(R.id.document_list_view);
        noDataView.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        TextView textView = (TextView) findViewById(R.id.document_no_item_message);
        Button btn = (Button) findViewById(R.id.document_create);
        btn.setVisibility(View.INVISIBLE);
        textView.setText(getString(R.string.gov_authorization_noValue).toString());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (retainer != null) {
            if (authorizationList != null) {
                retainer.put(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_AUTH_FOR_VCH_LIST_KEY, authorizationList);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected List<GovDocument> getAuthListToCreateVch() {
        List<GovDocument> list = null;
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerAuthForVchDocListReceiver();
                request = govService.sendAuthForVchDocRequest();
                if (request == null) {
                    Log.e(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG
                        + ".getAuthListToCreateVch: unable to create request to get Govt. Documents for vouchers!");
                    unregisterAuthForVchListReceiver();
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
        return list;
    }

    /**
     * register document receiver
     * */
    protected void registerAuthForVchDocListReceiver() {
        if (receiver == null) {
            receiver = new DocumentListReceiver(this);
            if (documentIntentFilter == null) {
                documentIntentFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_GET_AUTH_FOR_VCH_DOCUMENT);
            }
            getApplicationContext().registerReceiver(receiver, documentIntentFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAuthForVchDocListReceiver and document filter not null");
        }
    }

    /**
     * un-register document list receiver
     * */
    protected void unregisterAuthForVchListReceiver() {
        if (receiver != null) {
            getApplicationContext().unregisterReceiver(receiver);
            receiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAuthForVchListReceiver is null!");
        }
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling
     * the response for gov. document
     */
    class DocumentListReceiver extends
        BaseBroadcastReceiver<AuthForVchListActivity, GetAuthForVchDocListRequest>
    {

        private final String CLS_TAG = AuthForVchListActivity.CLS_TAG + "."
            + DocumentListReceiver.class.getSimpleName();

        protected DocumentListReceiver(AuthForVchListActivity authForVchListActivity) {
            super(authForVchListActivity);
        }

        @Override
        protected void clearActivityServiceRequest(AuthForVchListActivity activity) {
            activity.request = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_SEARCHING_DOCUMENT);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();
            final DocumentListReply reply = app.getAuthForVchDocumentList();
            if (reply != null) {
                onHandleSuccess(reply);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
            }
        }

        @Override
        protected void setActivityServiceRequest(GetAuthForVchDocListRequest request) {
            activity.request = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterAuthForVchListReceiver();
        }
    }

    /**
     * Helper Method to show listview or configure activity view
     * 
     * @param {@link DocumentListReply} reply
     * 
     * */
    protected void onHandleSuccess(DocumentListReply reply) {
        authorizationList = reply.documentList;
        buildView(authorizationList);
    }

    /**
     * Check whether we need to refresh our list or not.
     * 
     * @param reply
     *            : Document list reply contains lastRefreshtime.
     * */
    protected boolean isUpdateRequiredForList(DocumentListReply reply) {
        if (GovAppMobile.isConnected()) {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            Calendar lastRefresh = reply.lastRefreshTime;
            int minuteDifference = FormatUtil.getMinutesDifference(lastRefresh, now);
            if (minuteDifference == -1 || minuteDifference > 2) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_SEARCHING_DOCUMENT: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(getString(R.string.gov_retrieve_auth_for_vch_documents).toString());
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
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    /**
     * generate list item for document list adapter.
     * 
     * @param documentList
     *            : document list
     * @return : list of document list item for create vch.
     */
    protected List<DocumentListItem> getDocListItem(List<GovDocument> documentList) {
        List<DocumentListItem> documentListItem = null;
        if (documentList != null) {
            documentListItem = new ArrayList<DocumentListItem>(documentList.size());
            for (GovDocument govDocument : documentList) {
                documentListItem.add(new DocumentListItem(govDocument));
            }
        }
        return documentListItem;
    }
}
