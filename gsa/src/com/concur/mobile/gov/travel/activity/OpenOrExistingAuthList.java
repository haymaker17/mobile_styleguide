package com.concur.mobile.gov.travel.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.travel.data.TANumberListRow;

public class OpenOrExistingAuthList extends BaseActivity {

    private static final String CLS_TAG = OpenOrExistingAuthList.class.getSimpleName();
    private static final String RETAINER_LIST_KEY = "retainer.list.key";

    private List<TANumberListRow> authorizationList;
    private ListView listView;
    private ListItemAdapter<OpenOrExistingAuthListItem> authorizationAdapter;
    private OpenOrExistingAuthListItem selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.travel_authorization_list);
        setViewConfiguration();
    }

    /**
     * set screen title
     * 
     * @param whichList
     *            : is it open & group list OR existing auth list.
     */
    private void initScreenHeader(String whichList) {
        if (whichList.equalsIgnoreCase(TravelAuthType.ISOPEN)) {
            getSupportActionBar().setTitle(getString(R.string.gov_travel_authorization_open_group_auth_title).toString());
        } else {
            getSupportActionBar().setTitle(getString(R.string.gov_travel_authorization_add_to_existing_auth_title).toString());
        }
    }

    /** set view configuration. filter the list and set build the view */
    // TODO database query...
    private void setViewConfiguration() {
        GovAppMobile app = (GovAppMobile) getApplication();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String whichList = bundle.getString(TravelAuthType.WHICH_LIST);
            if (whichList.equalsIgnoreCase(TravelAuthType.ISOPEN)) {
                authorizationList = app.trvlBookingCache.getOpenGroupAutList();
                initScreenHeader(TravelAuthType.ISOPEN);
                buildView(authorizationList);
            } else if (whichList.equalsIgnoreCase(TravelAuthType.ISEXISTING)) {
                authorizationList = app.trvlBookingCache.getExistingAuthList();
                initScreenHeader(TravelAuthType.ISEXISTING);
                buildView(authorizationList);
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }

    }

    /** build view */
    private void buildView(List<TANumberListRow> authorizationList) {
        if (authorizationList != null) {
            // set listview
            initAuthList(authorizationList);
        }
    }

    /** initialize list and set adapter */
    protected void initAuthList(List<TANumberListRow> authorizationList) {
        listView = (ListView) findViewById(R.id.gov_ta_list_view);
        if (listView != null) {
            if (authorizationList != null) {
                List<OpenOrExistingAuthListItem> authListItem = getListItem(authorizationList);
                authorizationAdapter = new ListItemAdapter<OpenOrExistingAuthListItem>(this, authListItem);
                listView.setAdapter(authorizationAdapter);
                listView.setOnItemClickListener(new OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectedItem = (OpenOrExistingAuthListItem) listView.getItemAtPosition(position);
                        GovAppMobile app = (GovAppMobile) getApplication();
                        app.trvlBookingCache.setSelectedAuthItem(selectedItem);
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }
        } else {
            Log.e(CLS_TAG, ".initAuthList: unable to find list view!");
        }

    }

    /**
     * get List Item
     * 
     * @param authorizationList
     * @return
     */
    private List<OpenOrExistingAuthListItem> getListItem(List<TANumberListRow> authList) {
        List<OpenOrExistingAuthListItem> authlistItem = null;
        if (authList != null) {
            authlistItem = new ArrayList<OpenOrExistingAuthListItem>(authList.size());
            for (TANumberListRow item : authList) {
                authlistItem.add(new OpenOrExistingAuthListItem(item));
            }
        }
        return authlistItem;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (retainer != null) {
            if (authorizationList != null) {
                retainer.put(RETAINER_LIST_KEY, authorizationList);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
