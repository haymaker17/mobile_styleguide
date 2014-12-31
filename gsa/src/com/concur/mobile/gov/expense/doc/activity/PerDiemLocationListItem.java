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
import com.concur.mobile.gov.expense.doc.data.PerdiemTDY;
import com.concur.mobile.platform.util.Format;

public class PerDiemLocationListItem extends ListItem {

    private static final String CLS_TAG = PerDiemLocationListItem.class.getSimpleName();
    private PerdiemTDY perdiemTDY;

    public PerDiemLocationListItem(PerdiemTDY perdiemTDY) {
        this.perdiemTDY = perdiemTDY;
    }

    public PerdiemTDY getDocument() {
        return perdiemTDY;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View rowView = null;
        LayoutInflater inflater = null;
        if (convertView == null) {
            // Inflate a new view.
            inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.drill_in_perdiem_row, null);
        } else {
            rowView = convertView;
        }
        // Populate main row container and static elements
        if (perdiemTDY != null) {
            // Set expense name
            TextView txtView = (TextView) rowView.findViewById(R.id.perdiem_row_name);
            if (txtView != null) {
                txtView.setText(FormatUtil.nullCheckForString(perdiemTDY.perdiemLocation));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate  location text view!");
            }
            // set date
            txtView = (TextView) rowView.findViewById(R.id.perdiem_trip_date);
            if (txtView != null) {
                StringBuilder strBuilder = new StringBuilder("");
                strBuilder
                    .append(Format
                        .safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY, perdiemTDY.beginTripday));
                strBuilder.append(" - ");
                strBuilder
                    .append(Format
                        .safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY, perdiemTDY.endTripday));
                txtView.setText(FormatUtil.nullCheckForString(strBuilder.toString()));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate trip date text view!");
            }
            // set rate
            txtView = (TextView) rowView.findViewById(R.id.perdiem_trip_rate);
            if (txtView != null) {
                txtView.setText(FormatUtil.nullCheckForString(perdiemTDY.rate));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate rate text view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: DsDocDetailInfo.PerdiemTDY list is null!");
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
