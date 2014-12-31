/**
 * Show Authorization list using this class. This extends {@link DocumentListActivity} class.
 * Set Authorization view, list, list item click events.
 * 
 * @author sunill
 * */
package com.concur.mobile.gov.expense.doc.authorization.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
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
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.doc.activity.DocumentListActivity;
import com.concur.mobile.gov.expense.doc.activity.DocumentListItem;
import com.concur.mobile.gov.expense.doc.data.GovDocument;
import com.concur.mobile.gov.expense.doc.service.DocumentListReply;
import com.concur.mobile.gov.util.GovFlurry;

public class AuthorizationListActivity extends DocumentListActivity {

    private static final String LOG_TAG = AuthorizationListActivity.class.getSimpleName();
    private static final String GTM_TYP = "AUTH";

    private List<GovDocument> authorizationList;
    private ListView listView;
    private ListItemAdapter<DocumentListItem> authorizationAdapter;
    private RelativeLayout noDataView;
    private DocumentListItem selectedDocumentListItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initScreenHeader();
        initValue(savedInstanceState);
    }

    @Override
    protected String getHeaderTitle() {
        return getString(R.string.gov_authorization_title);
    }

    /**
     * initialize value for the view.
     * */
    @SuppressWarnings("unchecked")
    private void initValue(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (retainer != null) {
                authorizationList = (List<GovDocument>) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_AUTH_LIST_KEY);
                buildView(authorizationList);
            } else {
                setViewConfiguration();
            }
        } else {
            setViewConfiguration();
        }
    }

    /** set view configuration. filter the list and set build the view */
    // TODO database query...
    private void setViewConfiguration() {
        GovAppMobile app = (GovAppMobile) getApplication();
        DocumentListReply reply = app.getDocumentListReply();
        if (reply != null) {
            if (isRefreshRequiredFromBooking || isUpdateRequiredForList(reply)) {
                sendDocumentListRequest();
            } else {
                authorizationList = filterList(reply.documentList);
                buildView(authorizationList);
            }
        } else {
            sendDocumentListRequest();
        }
    }

    /**
     * filter list for authorization
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
    private void buildView(List<GovDocument> authorizationList) {
        int count = 0;
        // authorizationList=null;
        if (authorizationList != null) {
            // set listview
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
        if (authorizationList.size() <= 0) {
            showNoDataView();
        } else {
            noDataView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            if (listView != null) {
                if (authorizationList != null) {
                    List<DocumentListItem> authorizationListItem = getStampListItem(authorizationList);
                    authorizationAdapter = new ListItemAdapter<DocumentListItem>(this, authorizationListItem);
                    listView.setAdapter(authorizationAdapter);
                    listView.setOnItemClickListener(new OnItemClickListener() {

                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedDocumentListItem = (DocumentListItem) listView.getItemAtPosition(position);
                            gotoDetailActivity(selectedDocumentListItem);
                        }
                    });
                }
            } else {
                Log.e(LOG_TAG, CLS_TAG + ".initDocumentList: unable to find list view!");
            }
        }
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
                Intent it = new Intent(AuthorizationListActivity.this, AuthorizationDetailActivity.class);
                it = createBundleForDetail(it, document);
                startActivityForResult(it, REFRESH_STAMP_DOCUMENT_LIST);
            } else {
                Log.e(LOG_TAG, CLS_TAG
                    + ".gotoDetailActivity: unable to find gov document from list item. it is null!");
            }
        } else {
            Log.e(LOG_TAG, CLS_TAG
                + ".gotoDetailActivity: unable to find selected list item. its null!");
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
        textView.setText(getString(R.string.gov_authorization_noValue).toString());
        btn.setVisibility(View.INVISIBLE);
        btn.setText(getString(R.string.gov_authorization_create).toString());
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    protected void onHandleSuccessForActivity(DocumentListReply reply) {
        authorizationList = filterList(reply.documentList);
        buildView(authorizationList);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (retainer != null) {
            if (authorizationList != null) {
                retainer.put(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_AUTH_LIST_KEY, authorizationList);
            }
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void updateUIList(DocumentListReply reply) {
        if (reply != null) {
            if (listView == null) {
                listView = (ListView) findViewById(R.id.document_list_view);
            }
            authorizationList = filterList(reply.documentList);
            // buildView(stampDocumentsList);
            if (authorizationList != null && authorizationList.size() > 0) {
                authorizationAdapter.setItems(getStampListItem(authorizationList));
                authorizationAdapter.notifyDataSetChanged();
            }
        } else {
            sendDocumentListRequest();
        }
    }
}
