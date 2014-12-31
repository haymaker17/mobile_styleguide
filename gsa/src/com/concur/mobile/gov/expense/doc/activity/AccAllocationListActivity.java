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
import com.concur.mobile.gov.expense.doc.data.AccountCode;

public class AccAllocationListActivity extends BasicListActivity {

    private ListItemAdapter<AccAllocationListItem> accAlocationItemAdapter;
    private ListView list;

    // private AccAllocationListItem selectedDrillInOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getTitleText() {
        return getString(R.string.gov_docdetail_accounting_allocation);
    }

    @Override
    protected void configureListItems() {
        list = (ListView) findViewById(R.id.gov_drillin_opt_list_view);
        if (list != null) {
            List<AccAllocationListItem> accListOfItems = new ArrayList<AccAllocationListItem>();
            List<AccountCode> accountCodes = docDetailInfo.accountCodeList;
            if (accountCodes != null) {
                for (AccountCode acc : accountCodes) {
                    AccAllocationListItem accListItem = new AccAllocationListItem(acc);
                    accListOfItems.add(accListItem);
                }
                accAlocationItemAdapter = new ListItemAdapter<AccAllocationListItem>(this,
                    accListOfItems);
                list.setAdapter(accAlocationItemAdapter);
                list.setOnItemClickListener(new OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // selectedDrillInOption=(AccAllocationListItem) list.getItemAtPosition(position);
                    }
                });
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems: docDetailInfo.accountcode is null..finish activity!");
                // TODO no data view
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureListItems: unable to find list view!");
        }
    }
}
