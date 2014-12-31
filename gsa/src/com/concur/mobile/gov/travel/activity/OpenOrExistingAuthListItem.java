/**
 * @author sunill
 */
package com.concur.mobile.gov.travel.activity;

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
import com.concur.mobile.gov.travel.data.TANumberListRow;
import com.concur.mobile.platform.util.Format;

public class OpenOrExistingAuthListItem extends ListItem {

    private static final String CLS_TAG = OpenOrExistingAuthListItem.class.getSimpleName();
    private TANumberListRow taNumberListItem;

    public OpenOrExistingAuthListItem(TANumberListRow item) {
        this.taNumberListItem = item;
    }

    public TANumberListRow getItem() {
        return taNumberListItem;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View rowView = null;
        LayoutInflater inflater = null;
        if (convertView == null) {
            // Inflate a new view.
            inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.travel_authorization_list_row, null);
        } else {
            rowView = convertView;
        }
        // Populate main row container and static elements
        if (taNumberListItem != null) {
            // Set ta number
            TextView txtView = (TextView) rowView.findViewById(R.id.auth_name);
            if (txtView != null) {
                txtView.setText(taNumberListItem.taNumber);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate tanumber text view!");
            }
            // set date
            txtView = (TextView) rowView.findViewById(R.id.auth_date);
            if (txtView != null) {
                StringBuilder strBuilder = new StringBuilder("");
                strBuilder.append(" (");
                strBuilder
                    .append(Format
                        .safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY, taNumberListItem.tripBeginDate));
                // strBuilder.append(" - ");
                // strBuilder
                // .append(FormatUtil
                // .safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY, taNumberListItem.tripEndDate));
                strBuilder.append(")");
                txtView.setText(strBuilder.toString());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate date text view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: TaNumberListRow is null!");
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
