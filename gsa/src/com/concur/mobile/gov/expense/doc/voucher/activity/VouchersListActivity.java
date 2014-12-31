/**
 * Show Voucher list using this class. This extends {@link DocumentListActivity} class.
 * Set Voucher list view, list, list item click events.
 * 
 * @author sunill
 * */
package com.concur.mobile.gov.expense.doc.voucher.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.doc.activity.DocumentListActivity;
import com.concur.mobile.gov.expense.doc.activity.DocumentListItem;
import com.concur.mobile.gov.expense.doc.authorization.activity.AuthForVchListActivity;
import com.concur.mobile.gov.expense.doc.data.GovDocument;
import com.concur.mobile.gov.expense.doc.service.DocumentListReply;
import com.concur.mobile.gov.expense.doc.voucher.service.CreateVoucherFromAuthRequest;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.Const;
import com.concur.mobile.gov.util.GovFlurry;

public class VouchersListActivity extends DocumentListActivity implements View.OnClickListener {

    private static final String CLS_TAG = VouchersListActivity.class.getSimpleName();
    private static final String GTM_TYP = "VCH";

    private static final int REQUEST_AUTH_SELECT = 1;

    protected CreateVoucherFromAuthRequest createVoucherRequest;
    protected CreateVoucherReceiver cvReceiver;
    protected IntentFilter createVoucherIntentFilter;

    protected List<GovDocument> voucherList;
    protected ListView listView;
    protected ListItemAdapter<DocumentListItem> voucherAdapter;
    protected RelativeLayout noDataView;
    protected DocumentListItem selectedDocumentListItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initScreenHeader();
        initValue(savedInstanceState);
    }

    @Override
    protected String getHeaderTitle() {
        return getString(R.string.gov_vouchers_title);
    }

    /**
     * initialize value for the view.
     * */
    @SuppressWarnings("unchecked")
    private void initValue(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (retainer != null) {
                voucherList = (List<GovDocument>) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_VCH_LIST_KEY);
                buildView(voucherList);
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
        DocumentListReply reply = app.getDocumentListReply();
        if (reply != null) {
            if (isUpdateRequiredForList(reply)) {
                sendDocumentListRequest();
            } else {
                voucherList = filterList(reply.documentList);
                buildView(voucherList);
            }
        } else {
            sendDocumentListRequest();
        }
    }

    /**
     * filter list for voucher list
     * 
     * @param documentList
     */
    private List<GovDocument> filterList(List<GovDocument> documentList) {
        List<GovDocument> result = null;
        if (documentList != null) {
            result = new ArrayList<GovDocument>();
            for (GovDocument govDocument : documentList) {
                if (govDocument.gtmDocType.equalsIgnoreCase(GTM_TYP)) {
                    result.add(govDocument);
                }
            }
        }
        return result;
    }

    /** build view */
    private void buildView(List<GovDocument> vchList) {
        int count = 0;
        if (vchList != null) {
            // set listview
            count = vchList.size();
            initDocumentList(vchList);
        } else {
            showNoDataView();
        }
        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(GovFlurry.PARAM_NAME_VCH_COUNT, Integer.toString(count));
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_LIST, GovFlurry.EVENT_VIEW_VCH, params);
    }

    /** initialize list and set adapter */
    protected void initDocumentList(List<GovDocument> vchList) {
        noDataView = (RelativeLayout) findViewById(R.id.document_no_list_view);
        listView = (ListView) findViewById(R.id.document_list_view);
        if (vchList.size() <= 0) {
            showNoDataView();
        } else {
            noDataView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            if (listView != null) {
                if (vchList != null) {
                    List<DocumentListItem> vchListItem = getStampListItem(vchList);
                    voucherAdapter = new ListItemAdapter<DocumentListItem>(this, vchListItem);
                    listView.setAdapter(voucherAdapter);
                    listView.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedDocumentListItem = (DocumentListItem) listView.getItemAtPosition(position);
                            gotoDetailActivity(selectedDocumentListItem);
                        }
                    });
                }
            } else {
                Log.e(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG
                    + ".initDocumentList: unable to find list view!");
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
        textView.setText(getString(R.string.gov_vouchers_noValue).toString());
        Button btn = (Button) findViewById(R.id.document_create);
        btn.setText(getString(R.string.gov_vouchers_create_title).toString());
        btn.setOnClickListener(this);
    }

    /***
     * Go to Documnet Detail Activity
     * 
     * @param selectedDocumentListItem
     *            : selected list item.
     * */
    private void gotoDetailActivity(DocumentListItem selectedDocumentListItem) {
        if (selectedDocumentListItem != null) {
            GovDocument document = selectedDocumentListItem.getDocument();
            if (document != null) {
                gotoDetailActivity(document);
            } else {
                Log.e(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG
                    + ".gotoDetailActivity: unable to find gov document from list item. it is null!");
            }
        } else {
            Log.e(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG
                + ".gotoDetailActivity: unable to find selected list item. its null!");
        }
    }

    private void gotoDetailActivity(GovDocument document) {
        Intent it = new Intent(VouchersListActivity.this, VoucherDetailActivity.class);
        it = createBundleForDetail(it, document);
        startActivityForResult(it, REFRESH_STAMP_DOCUMENT_LIST);
    }

    @Override
    protected void onHandleSuccessForActivity(DocumentListReply reply) {
        voucherList = filterList(reply.documentList);
        buildView(voucherList);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (retainer != null) {
            if (voucherList != null) {
                retainer.put(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_VCH_LIST_KEY, voucherList);
            }
        }
    }

    @Override
    public void onClick(View v) {
        View actionButton = findViewById(R.id.document_create);
        if (actionButton != null) {
            registerForContextMenu(actionButton);
            openContextMenu(actionButton);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (view.getId() == R.id.document_create) {
            android.view.MenuInflater infl = getMenuInflater();
            menu.setHeaderTitle(R.string.gov_vouchers_create_title);
            infl.inflate(R.menu.gov_voucher_create_menu_context, menu);
            // TODO removed for gov3.0 release.
            menu.removeItem(R.id.vch_create);
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        boolean handled = super.onContextItemSelected(item);
        final int itemId = item.getItemId();
        switch (itemId) {
        case R.id.vch_create:
            Log.d(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG + " do nothing from create");
            break;
        case R.id.vch_create_auth:
            // Launch the authorization list in select mode and get a return
            callAuthList();
            break;
        default:
            break;
        }
        return handled;
    }

    private void callAuthList() {
        Intent i = new Intent(this, AuthForVchListActivity.class);
        i.putExtra(Const.EXTRA_SINGLE_SELECT, true);
        startActivityForResult(i, REQUEST_AUTH_SELECT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gov_voucher_create_menu_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
        /*
         * case R.id.vch_create:
         * Log.d(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG + " do nothing from create");
         * break;
         */
        case R.id.vch_create_auth:
            callAuthList();
            break;
        default:
            break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void updateUIList(DocumentListReply reply) {
        if (reply != null) {
            if (listView == null) {
                listView = (ListView) findViewById(R.id.document_list_view);
            }
            voucherList = filterList(reply.documentList);
            // buildView(stampDocumentsList);
            if (voucherList != null && voucherList.size() > 0) {
                voucherAdapter.setItems(getStampListItem(voucherList));
                voucherAdapter.notifyDataSetChanged();
            }
        } else {
            sendDocumentListRequest();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_AUTH_SELECT == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                String travelerId = data.getStringExtra(Const.EXTRA_GOV_TRAVELER_ID);
                String authName = data.getStringExtra(Const.EXTRA_GOV_AUTH_NAME);
                String authType = data.getStringExtra(Const.EXTRA_GOV_AUTH_TYPE);

                sendCreateVoucherFromAuthRequest(travelerId, authName, authType);
            }
        }
    }

    protected void sendCreateVoucherFromAuthRequest(String travelerId, String authName, String authType) {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerCreateVoucherReceiver();
                createVoucherRequest = govService.sendCreateVoucherFromAuthReq(travelerId, authName, authType);
                if (createVoucherRequest == null) {
                    Log.e(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG
                        + ".sendCreateVoucherFromAuthRequest: unable to send request");
                    unregisterCreateVoucherReceiver();
                } else {
                    // set service request.
                    cvReceiver.setServiceRequest(createVoucherRequest);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_CREATE_VOUCHER_FROM_AUTH);
                }
            } else {
                Log.wtf(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG
                    + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(com.concur.mobile.core.util.Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * register document receiver
     * */
    protected void registerCreateVoucherReceiver() {
        if (cvReceiver == null) {
            cvReceiver = new CreateVoucherReceiver(this);
            if (createVoucherIntentFilter == null) {
                createVoucherIntentFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_VOUCHER_CREATE_FROM_AUTH);
            }
            registerReceiver(cvReceiver, createVoucherIntentFilter);
        } else {
            Log.e(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG
                + ".registerDocumentListReceiver and document filter not null");
        }
    }

    /**
     * un-register document list receiver
     * */
    protected void unregisterCreateVoucherReceiver() {
        if (cvReceiver != null) {
            unregisterReceiver(cvReceiver);
            cvReceiver = null;
        } else {
            Log.e(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG + ".unregisterDocumentListReceiver is null!");
        }
    }

    class CreateVoucherReceiver extends BaseBroadcastReceiver<VouchersListActivity, CreateVoucherFromAuthRequest>
    {

        private final String CLS_TAG = VouchersListActivity.CLS_TAG + "."
            + CreateVoucherReceiver.class.getSimpleName();

        protected CreateVoucherReceiver(VouchersListActivity activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(VouchersListActivity activity) {
            activity.createVoucherRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_CREATE_VOUCHER_FROM_AUTH);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            logFlurryEvents(false);
            showDialog(Const.DIALOG_CREATE_VOUCHER_FAIL);
            Log.e(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // Voucher was created. Refresh our voucher list and jump to the detail.

            logFlurryEvents(true);
            // Grab the doc from the app
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();
            final DocumentListReply reply = app.getDocumentListReply();
            if (reply != null && reply.documentList != null && reply.documentList.size() > 0) {

                // Kick off the detail activity
                GovDocument doc = reply.documentList.get(0);
                activity.gotoDetailActivity(doc);
            } else {
                // TODO
                // We are here because the DocumentListReply is not properly handling errors because
                // they come back as Action Status replies. That needs to get updated to handle those
                // error responses which will properly direct us into the handleFailure() method.
                if (reply.mwsErrorMessage != null) {
                    setFaliureMsg(reply.mwsErrorMessage);
                }
                showDialog(Const.DIALOG_CREATE_VOUCHER_FAIL);
            }

        }

        @Override
        protected void setActivityServiceRequest(CreateVoucherFromAuthRequest request) {
            activity.createVoucherRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterCreateVoucherReceiver();
        }
    }

    private void logFlurryEvents(boolean isSuccess) {
        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        if (isSuccess) {
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_RECEIPT);
        } else {
            params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_RECEIPT);
        }
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_LIST, GovFlurry.EVENT_CREATE_VCH_FROM_AUTH, params);

    }
}
