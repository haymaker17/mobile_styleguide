package com.concur.mobile.gov.travel.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.concur.mobile.gov.travel.data.PerDiemListRow;

public class TDYPerDiemLocations extends BaseActivity {

    private static final String CLS_TAG = TDYPerDiemLocations.class.getSimpleName();
    private static final String RETAINER_LIST_KEY = "retainer.list.key";

    private List<PerDiemListRow> perDiemList;
    private ListView listView;
    private ListItemAdapter<TDYPerDiemLocationItem> perDiemAdapter;
    private TDYPerDiemLocationItem selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.travel_authorization_perdiem_list);
        initScreenHeader();
        setViewConfiguration();
    }

    /** set screen title */
    private void initScreenHeader() {
        getSupportActionBar().setTitle(getString(R.string.gov_travel_authorization_perdiem_location).toString());
    }

    /** set view configuration. filter the list and set build the view */
    private void setViewConfiguration() {
        GovAppMobile app = (GovAppMobile) getApplication();
        perDiemList = app.trvlBookingCache.getPerDiemList();
        buildView(perDiemList);
    }

    /** build view */
    private void buildView(List<PerDiemListRow> perDiemList) {
        if (perDiemList != null) {
            // set listview
            initPerdiemList(perDiemList);
        }
    }

    /** initialize list and set adapter */
    protected void initPerdiemList(List<PerDiemListRow> perDiemList) {
        listView = (ListView) findViewById(R.id.gov_perdiem_list_view);
        if (listView != null) {
            if (perDiemList != null) {
                List<TDYPerDiemLocationItem> listItems = getListItem(perDiemList);
                perDiemAdapter = new ListItemAdapter<TDYPerDiemLocationItem>(this, listItems);
                listView.setAdapter(perDiemAdapter);
                listView.setOnItemClickListener(new OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectedItem = (TDYPerDiemLocationItem) listView.getItemAtPosition(position);
                        GovAppMobile app = (GovAppMobile) getApplication();
                        app.trvlBookingCache.setSelectedPerDiemItem(selectedItem);
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }
        } else {
            Log.e(CLS_TAG, ".initPerdiemList: unable to find list view!");
        }

    }

    /**
     * get List Item
     * 
     * @param authorizationList
     * @return
     */
    private List<TDYPerDiemLocationItem> getListItem(List<PerDiemListRow> perDiemList) {
        List<TDYPerDiemLocationItem> list = null;
        if (perDiemList != null) {
            // 0th element is always default location
            TDYPerDiemLocationItem defaultPerdiemLocation = new TDYPerDiemLocationItem(perDiemList.get(0));
            // remove 0th element
            perDiemList.remove(0);

            // add to local list to sort the list
            list = new ArrayList<TDYPerDiemLocationItem>(perDiemList.size() - 1);
            for (int i = 1; i < perDiemList.size(); i++) {
                list.add(new TDYPerDiemLocationItem(perDiemList.get(i)));
            }

            // sort
            PerDiemComparator perDiemComparator = new PerDiemComparator();
            // alphabetically order list of location name.
            if (list != null && list.size() > 0) {
                Collections.sort(list, perDiemComparator);
            }

            // after sorting add the default location on the top
            list.add(0, defaultPerdiemLocation);
        }
        return list;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (retainer != null) {
            if (perDiemList != null) {
                retainer.put(RETAINER_LIST_KEY, perDiemList);
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

    private class PerDiemComparator implements Comparator<TDYPerDiemLocationItem> {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(TDYPerDiemLocationItem item1, TDYPerDiemLocationItem item2) {
            int retVal = 0;

            if ((item1 != null && item2 != null) && (item1 != item2)) {
                retVal = item1.getPerDiemItem().locate.compareTo(item2.getPerDiemItem().locate);
            } else {
                retVal = 0;
            }
            return retVal;
        }

    }

}
