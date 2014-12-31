/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.charge.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.gov.expense.activity.ExpenseListItem;
import com.concur.mobile.gov.expense.charge.data.MobileExpense;
import com.concur.mobile.platform.util.Format;

public class UnAppliedExpenseListItem extends ListItem {

    private static final String CLS_TAG = ExpenseListItem.class.getSimpleName();

    private MobileExpense expense;

    public UnAppliedExpenseListItem(MobileExpense expense) {
        this.expense = expense;
    }

    public MobileExpense getExpense() {
        return expense;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View rowView = null;
        LayoutInflater inflater = null;
        if (convertView == null) {
            // Inflate a new view.
            inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.mobile_expense_row, null);
        } else {
            rowView = convertView;
        }
        // Populate main row container and static elements
        if (expense != null) {
            // Set expense name
            TextView txtView = (TextView) rowView.findViewById(R.id.expense_row_expname);
            if (txtView != null) {
                txtView.setText(FormatUtil.nullCheckForString(expense.tranDescription));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate  expense desc. text view!");
            }
            // set expense date.
            txtView = (TextView) rowView.findViewById(R.id.expense_row_date);
            if (txtView != null) {
                StringBuilder strBuilder = new StringBuilder("");
                strBuilder.append(Format
                    .safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY, expense.tranDate));
                txtView.setText(FormatUtil.nullCheckForString(strBuilder.toString()));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate expense category text view!");
            }
            // set amount
            txtView = (TextView) rowView.findViewById(R.id.expense_row_amount);
            if (txtView != null) {
                // Format amount
                String reportTotal = FormatUtil
                    .formatAmount(expense.postedAmt, com.concur.mobile.gov.util.Const.GOV_LOCALE, com.concur.mobile.gov.util.Const.GOV_CURR_CODE, true, true);
                txtView.setText(FormatUtil.nullCheckForString(reportTotal));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate expense amount text view!");
            }
            // set receipt icon
            ImageView receiptView = (ImageView) rowView.findViewById(R.id.expense_row_receipt);
            if (receiptView != null) {
                String imageId = expense.imageid;
                if (imageId != null && imageId.length() > 0) {
                    receiptView.setVisibility(View.VISIBLE);
                } else {
                    receiptView.setVisibility(View.INVISIBLE);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate receipt image view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: DsDocDetailInfo.expenses is null!");
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
