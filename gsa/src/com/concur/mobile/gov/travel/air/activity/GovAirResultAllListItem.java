/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.air.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.travel.air.activity.AirResultAllListItem;
import com.concur.mobile.core.util.FormatUtil;

/**
 * 
 * @author Chris N. Diaz
 * 
 */
public class GovAirResultAllListItem extends AirResultAllListItem {

    private Double values = 0.0;

    /**
     * 
     * @param type
     * @param count
     * @param listItemViewType
     */
    public GovAirResultAllListItem(int stops, int count, int listItemViewType) {
        super(stops, count, listItemViewType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirResultAllListItem#buildView(android.content.Context, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        // Create the main row container and static elements
        View rowView = inflater.inflate(R.layout.air_search_results_all_row, null);
        TextView tv = (TextView) rowView.findViewById(R.id.allCount);

        String result = context.getResources().getString(R.string.gov_travel_see_all_result_negative);
        if (count > 0) {
            String startingCost = FormatUtil.formatAmount(values,
                context.getResources().getConfiguration().locale, "USD", true, true);
            result = String.format(context.getResources().getString(R.string.gov_travel_see_results_all_flights, count, startingCost));
        }
        tv.setText(result);
        return rowView;
    }

    public void setLowest(Double values) {
        this.values = values;
    }
}
