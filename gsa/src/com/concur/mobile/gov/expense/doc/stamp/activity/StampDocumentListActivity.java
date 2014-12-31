/**
 * Show Stamp Document list using this class. This extends {@link DocumentListActivity} class.
 * Set Stamp Document view, list, list item click events.
 * 
 * @author sunill
 * */
package com.concur.mobile.gov.expense.doc.stamp.activity;

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
import android.widget.ImageView;
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

public class StampDocumentListActivity extends DocumentListActivity {

    private static final String LOG_TAG = StampDocumentListActivity.class.getSimpleName();
    private List<GovDocument> stampDocumentsList;
    private ListView listView;
    private ListItemAdapter<DocumentListItem> stampDocumentAdapter;
    private RelativeLayout noDataView;
    private DocumentListItem selectedDocumentListItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.document_list);
        initScreenHeader();
        initValue(savedInstanceState);
    }

    @Override
    protected String getHeaderTitle() {
        return getString(R.string.gov_stamp_doc_title);
    }

    /**
     * initialize value for the view.
     * */
    @SuppressWarnings("unchecked")
    private void initValue(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (retainer != null) {
                stampDocumentsList = (List<GovDocument>) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_STAMP_LIST_KEY);
                buildView(stampDocumentsList);
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
                stampDocumentsList = filterList(reply.documentList);
                buildView(stampDocumentsList);
            }
        } else {
            sendDocumentListRequest();
        }
    }

    /**
     * filter list for stampDocument
     * 
     * @param documentList
     */
    private List<GovDocument> filterList(List<GovDocument> documentList) {
        List<GovDocument> result = null;
        if (documentList != null) {
            result = new ArrayList<GovDocument>();
            for (GovDocument govDocument : documentList) {
                if (govDocument.needsStamping == Boolean.TRUE) {
                    result.add(govDocument);
                }
            }
        }
        return result;
    }

    /** build view */
    private void buildView(List<GovDocument> stampDocList) {
        int count = 0;
        // stampDocList=null;
        if (stampDocList != null) {
            // set listview
            count = stampDocList.size();
            initDocumentList(stampDocList);
        } else {
            showNoDataView();
        }
        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(GovFlurry.PARAM_NAME_STAMP_COUNT, Integer.toString(count));
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_LIST, GovFlurry.EVENT_VIEW_STAMP, params);
    }

    /** initialize list and set adapter */
    protected void initDocumentList(List<GovDocument> stampDocList) {
        noDataView = (RelativeLayout) findViewById(R.id.document_no_list_view);
        listView = (ListView) findViewById(R.id.document_list_view);
        if (stampDocList.size() <= 0) {
            showNoDataView();
        } else {
            noDataView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            if (listView != null) {
                // List<DocumentListItem> stampDocListItem=null;
                if (stampDocList != null) {
                    List<DocumentListItem> stampDocListItem = getStampListItem(stampDocList);
                    stampDocumentAdapter = new ListItemAdapter<DocumentListItem>(this, stampDocListItem);
                    listView.setAdapter(stampDocumentAdapter);
                    listView.setOnItemClickListener(new OnItemClickListener() {

                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedDocumentListItem = (DocumentListItem) listView.getItemAtPosition(position);
                            gotoDetailActivity(selectedDocumentListItem);
                        }
                    });
                }
            } else {
                Log.e(CLS_TAG, LOG_TAG + ".initDocumentList: unable to find list view!");
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
        textView.setText(getString(R.string.gov_stamp_doc_noValue).toString());
        Button btn = (Button) findViewById(R.id.document_create);
        ImageView imgView = (ImageView) findViewById(R.id.document_addIcon);
        btn.setVisibility(View.GONE);
        imgView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onHandleSuccessForActivity(DocumentListReply reply) {
        stampDocumentsList = filterList(reply.documentList);
        buildView(stampDocumentsList);
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
                Intent it = new Intent(StampDocumentListActivity.this, StampDocumentDetailActivity.class);
                it = createBundleForDetail(it, document);
                startActivityForResult(it, REFRESH_STAMP_DOCUMENT_LIST);
            } else {
                Log.e(CLS_TAG, LOG_TAG
                    + ".gotoDetailActivity: unable to find gov document from list item. it is null!");
            }
        } else {
            Log.e(LOG_TAG, CLS_TAG
                + ".gotoDetailActivity: unable to find selected list item. its null!");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (retainer != null) {
            if (stampDocumentsList != null) {
                retainer
                    .put(com.concur.mobile.gov.util.Const.RETAINER_DOCUMENT_STAMP_LIST_KEY, stampDocumentsList);
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
            stampDocumentsList = filterList(reply.documentList);
            if (stampDocumentsList != null && stampDocumentsList.size() > 0) {
                stampDocumentAdapter.setItems(getStampListItem(stampDocumentsList));
                stampDocumentAdapter.notifyDataSetChanged();
            }
        } else {
            sendDocumentListRequest();
        }
    }
}
