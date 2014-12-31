/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.air.activity;

import java.net.URI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.travel.air.data.AirlineEntry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.AsyncImageView;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * 
 * @author Chris N. Diaz
 * 
 */
public class GovAirResultSummaryListItem extends ListItem {

    public AirlineEntry airline;
    public GovRateType rateType;

    /**
     * 
     * @param rateType
     * @param airline
     * @param listItemViewType
     */
    public GovAirResultSummaryListItem(GovRateType rateType, AirlineEntry airline, int listItemViewType) {
        this.rateType = rateType;
        this.airline = airline;
        this.listItemViewType = listItemViewType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.ListItem#buildView(android.content.Context, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        // Create the main row container and static elements
        View rowView = inflater.inflate(R.layout.air_search_results_summary_row, null);

        // The logo
        AsyncImageView aiv = (AsyncImageView) rowView.findViewById(R.id.airlineLogo);
        String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
        StringBuilder sb = new StringBuilder();
        sb.append(serverAdd);
        sb.append("/images/trav/a_small/").append(airline.airlineCode).append(".gif");
        aiv.setAsyncUri(URI.create(sb.toString()));

        TextView tv = (TextView) rowView.findViewById(R.id.airlineName);
        tv.setText(airline.getAirlineName());

        String resultWord;
        if (airline.numChoices == 1) {
            resultWord = context.getText(R.string.result_singular).toString();
        } else {
            resultWord = context.getText(R.string.result_plural).toString();
        }

        StringBuilder countText = new StringBuilder().append(airline.numChoices).append(' ').append(resultWord);
        final String s = countText.toString();
        tv = (TextView) rowView.findViewById(R.id.resultCount);
        tv.setText(s);

        String startingCost = FormatUtil.formatAmount(airline.lowestCost,
            context.getResources().getConfiguration().locale, airline.crnCode, true, true);
        tv = (TextView) rowView.findViewById(R.id.startingCost);
        tv.setText(startingCost);

        ImageView iv = (ImageView) rowView.findViewById(com.concur.gov.R.id.rateType);
        if (rateType == GovRateType.LIMITED_CAPACITY
            || rateType == GovRateType.CONTRACT
            || rateType == GovRateType.CONTRACT_BUSINESS) {
            iv.setImageResource(com.concur.gov.R.drawable.rate_three_star);
        } else if (rateType == GovRateType.ME_TOO) {
            iv.setImageResource(com.concur.gov.R.drawable.rate_two_star);
        } else {
            iv.setVisibility(View.INVISIBLE);
        }

        return rowView;
    }
}
