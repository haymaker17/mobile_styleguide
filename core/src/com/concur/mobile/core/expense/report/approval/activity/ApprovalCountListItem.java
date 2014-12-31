/**
 * 
 */
package com.concur.mobile.core.expense.report.approval.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.ListItem;

/**
 * An extension of <code>ListItem</code> for rendering approval list item within a list.
 */
public class ApprovalCountListItem extends ListItem {

    private static final String CLS_TAG = ApprovalCountListItem.class.getSimpleName();
    private String count;
    private Intent intent;
    private String approvalType;

    /**
     * Constructs an instance of <code>ApprovalCountListItem</code> with approval request name and number of items needs to be
     * approve.
     * 
     * 
     * @param count
     *            : message + number of items needs to be approved.
     */
    public ApprovalCountListItem(String count, int listItemViewType, Intent it, String approvalType) {
        this.count = count;
        this.listItemViewType = listItemViewType;
        this.intent = it;
        this.approvalType = approvalType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ListItem#buildView(android.content.Context , android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View headerView = null;
        // Create the main row container and static elements
        LayoutInflater inflater = LayoutInflater.from(context);
        headerView = inflater.inflate(R.layout.count_approval_row, null);
        TextView txtView = (TextView) headerView.findViewById(R.id.request_app_number);
        if (txtView != null) {
            txtView.setText(count);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate list section count text view!");
        }
        return headerView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    public Intent getIntent() {
        return intent;
    }

    public String getApprovalType() {
        return approvalType;
    }

}
