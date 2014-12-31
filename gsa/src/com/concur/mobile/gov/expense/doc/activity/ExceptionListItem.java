/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.activity;

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
import com.concur.mobile.gov.expense.doc.data.Exceptions;

public class ExceptionListItem extends ListItem {

    private static final String CLS_TAG = ExceptionListItem.class.getSimpleName();
    private Exceptions exceptions;
    private static final String FAIL = "FAIL";
    private static final String PASS = "PASS";

    public ExceptionListItem(Exceptions exceptions) {
        this.exceptions = exceptions;
    }

    public Exceptions getDocument() {
        return exceptions;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View rowView = null;
        LayoutInflater inflater = null;
        if (convertView == null) {
            // Inflate a new view.
            inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.drill_in_exception_row, null);
        } else {
            rowView = convertView;
        }
        // Populate main row container and static elements
        if (exceptions != null) {
            // Set expense name
            TextView txtView = (TextView) rowView.findViewById(R.id.exception_row_process);
            if (txtView != null) {
                txtView.setText(FormatUtil.nullCheckForString(exceptions.name));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate  exception name text view!");
            }
            // set expense category
            txtView = (TextView) rowView.findViewById(R.id.exception_row_status);
            if (txtView != null) {
                txtView.setText(FormatUtil.nullCheckForString(exceptions.errorStatus));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".buildView: unable to locate exception error status text view!");
            }
            // set alert icon
            ImageView alertView = (ImageView) rowView.findViewById(R.id.exception_row_alert);
            txtView = (TextView) rowView.findViewById(R.id.exception_row_alert_msg);
            if (alertView != null && txtView != null) {
                if (exceptions.errorStatus.equalsIgnoreCase(PASS)) {
                    alertView.setVisibility(View.GONE);
                    txtView.setVisibility(View.GONE);
                } else {
                    alertView.setVisibility(View.VISIBLE);
                    txtView.setVisibility(View.VISIBLE);
                    txtView.setText(FormatUtil.nullCheckForString(exceptions.comments));
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".buildView: unable to locate alert image view and/or alert msg view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: DsDocDetailInfo.exceptions is null!");
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
