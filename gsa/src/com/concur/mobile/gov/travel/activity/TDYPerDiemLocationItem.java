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
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.gov.travel.data.PerDiemListRow;

public class TDYPerDiemLocationItem extends ListItem {

    private static final String CLS_TAG = TDYPerDiemLocationItem.class.getSimpleName();
    private PerDiemListRow perDiemListItem;

    public TDYPerDiemLocationItem(PerDiemListRow item) {
        this.perDiemListItem = item;
    }

    public PerDiemListRow getPerDiemItem() {
        return perDiemListItem;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View rowView = null;
        LayoutInflater inflater = null;
        if (convertView == null) {
            // Inflate a new view.
            inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.general_one_txtview_row, null);
        } else {
            rowView = convertView;
        }
        // Populate main row container and static elements
        if (perDiemListItem != null) {
            // Set ta number
            TextView txtView = (TextView) rowView.findViewById(R.id.row_name);
            if (txtView != null) {
                String value = perDiemListItem.locate;
                String county = perDiemListItem.county;
                if (value != null && value.length() > 0) {
                    StringBuilder strbBuilder = new StringBuilder(value);
                    if (county != null && county.length() > 0) {
                        strbBuilder.append(", ");
                        strbBuilder.append(county);
                        txtView.setText(strbBuilder.toString());
                    } else {
                        txtView.setText(value);
                    }
                } else {
                    txtView.setText("");
                }

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate tanumber text view!");
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
