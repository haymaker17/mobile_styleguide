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
import com.concur.mobile.gov.expense.doc.data.Exceptions;

public class ExceptionsListActivity extends BasicListActivity {

    private ListItemAdapter<ExceptionListItem> exceptionItemAdapter;
    private ListView list;

    // private ExceptionListItem selectedDrillInOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getTitleText() {
        return getString(R.string.gov_docdetail_doc_audit);
    }

    @Override
    protected void configureListItems() {
        list = (ListView) findViewById(R.id.gov_drillin_opt_list_view);
        if (list != null) {
            List<ExceptionListItem> excptnList = new ArrayList<ExceptionListItem>();
            List<Exceptions> exceptionsList = docDetailInfo.exceptionsList;
            if (exceptionsList != null) {
                for (Exceptions exceptions : exceptionsList) {
                    ExceptionListItem exceptionInListItem = new ExceptionListItem(exceptions);
                    excptnList.add(exceptionInListItem);
                }
                exceptionItemAdapter = new ListItemAdapter<ExceptionListItem>(this, excptnList);
                list.setAdapter(exceptionItemAdapter);
                list.setOnItemClickListener(new OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // selectedDrillInOption=(ExceptionListItem) list.getItemAtPosition(position);
                    }
                });
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems: docDetailInfo.exception is null..finish activity!");
                // TODO no data view
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureListItems: unable to find list view!");
        }
    }
}
