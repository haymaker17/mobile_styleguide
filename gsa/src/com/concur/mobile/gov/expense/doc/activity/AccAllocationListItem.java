/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.gov.expense.doc.data.AccountCode;

public class AccAllocationListItem extends ListItem {

    private static final String CLS_TAG = AccAllocationListItem.class.getSimpleName();
    private AccountCode accAlocation;

    public AccAllocationListItem(AccountCode accAlocation) {
        this.accAlocation = accAlocation;
    }

    public AccountCode getDocument() {
        return accAlocation;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View rowView = null;
        LayoutInflater inflater = null;
        if (convertView == null) {
            // Inflate a new view.
            inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.drill_in_accalocation_row, null);
        } else {
            rowView = convertView;
        }
        // Populate main row container and static elements
        if (accAlocation != null) {
            // Set expense name
            TextView txtView = (TextView) rowView.findViewById(R.id.accalocation_row_account);
            if (txtView != null) {
                txtView.setText(FormatUtil.nullCheckForString(accAlocation.account));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate account text view!");
            }
            // set amount
            txtView = (TextView) rowView.findViewById(R.id.accalocation_row_amount);
            if (txtView != null) {
                // Format amount
                String reportTotal = FormatUtil
                    .formatAmount(accAlocation.amount, com.concur.mobile.gov.util.Const.GOV_LOCALE, com.concur.mobile.gov.util.Const.GOV_CURR_CODE, true, true);
                txtView.setText(FormatUtil.nullCheckForString(reportTotal));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate account amount text view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: DsDocDetailInfo.accountcodes is null!");
        }
        return rowView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
