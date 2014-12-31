package com.concur.mobile.core.travel.air.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.view.ListItem;

public class AirResultAllListItem extends ListItem {

    public static final int ALL_STOP_GROUPS = -1;

    public int stops;
    public int count;

    public AirResultAllListItem(int stops, int count, int listItemViewType) {
        this.listItemViewType = listItemViewType;
        this.stops = stops;
        this.count = count;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        // Create the main row container and static elements
        View rowView = inflater.inflate(R.layout.air_search_results_all_row, null);
        TextView tv = (TextView) rowView.findViewById(R.id.allCount);

        String all = context.getText(R.string.air_search_stopgroup_all).toString();
        String stopGroupName;
        if (stops == ALL_STOP_GROUPS) {
            stopGroupName = "";
        } else if (stops == 0) {
            stopGroupName = context.getText(R.string.air_search_stopgroup_nonstop).toString();
        } else if (stops == 1) {
            stopGroupName = context.getText(R.string.air_search_stopgroup_singular).toString();
        } else {
            stopGroupName = Format.localizeText(context, R.string.air_search_stopgroup_plural, stops);
        }

        StringBuilder sb = new StringBuilder(all).append(' ').append(stopGroupName);
        stopGroupName = sb.toString();

        String resultWord;
        if (count == 1) {
            resultWord = context.getText(R.string.result_singular).toString();
        } else {
            resultWord = context.getText(R.string.result_plural).toString();
        }

        tv.setText(Format.localizeText(context, R.string.air_search_see_all, new Object[] { stopGroupName, count,
                resultWord }));

        return rowView;
    }

}
