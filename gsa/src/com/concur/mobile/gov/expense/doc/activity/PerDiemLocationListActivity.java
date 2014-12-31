/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.concur.gov.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.gov.expense.activity.BasicListActivity;
import com.concur.mobile.gov.expense.doc.data.PerdiemTDY;

public class PerDiemLocationListActivity extends BasicListActivity {

    private ListItemAdapter<PerDiemLocationListItem> tdyItemAdapter;
    private ListView list;

    // private PerDiemLocationListItem selectedDrillInOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getTitleText() {
        return getString(R.string.gov_docdetail_perdiem_location);
    }

    @Override
    protected void configureListItems() {
        list = (ListView) findViewById(R.id.gov_drillin_opt_list_view);
        if (list != null) {
            List<PerDiemLocationListItem> tdyListOfItems = new ArrayList<PerDiemLocationListItem>();
            List<PerdiemTDY> tdy = docDetailInfo.perdiemList;
            if (tdy != null) {
                for (PerdiemTDY tdyObj : tdy) {
                    PerDiemLocationListItem accListItem = new PerDiemLocationListItem(tdyObj);
                    tdyListOfItems.add(accListItem);
                }
                tdyItemAdapter = new ListItemAdapter<PerDiemLocationListItem>(this, tdyListOfItems);
                list.setAdapter(tdyItemAdapter);
                list.setOnItemClickListener(new OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // selectedDrillInOption=(PerDiemLocationListItem) list.getItemAtPosition(position);
                    }
                });
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems: docDetailInfo.perdiemlist is null..finish activity!");
                // TODO no data view
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureListItems: unable to find list view!");
        }
    }
}
