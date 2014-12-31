package com.concur.mobile.core.travel.air.activity;

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
import com.concur.mobile.core.util.LayoutUtil;
import com.concur.mobile.core.view.AsyncImageView;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

public class AirResultSummaryListItem extends ListItem {

    public int stopGroup;
    public AirlineEntry airline;

    public AirResultSummaryListItem(int stopGroup, AirlineEntry airline, int listItemViewType) {
        this.stopGroup = stopGroup;
        this.listItemViewType = listItemViewType;
        this.airline = airline;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

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

        // set the Travel Points TODO - need to revisit this if condition to display
        LayoutUtil.initTravelPointsAtItemLevel(rowView, R.id.travel_points, airline.travelPoints);

        ImageView iv = (ImageView) rowView.findViewById(R.id.diamonds);
        int rank = airline.getPreferenceRank();
        if (rank >= 3) {
            iv.setImageResource(R.drawable.diamonds_3);
        } else if (rank == 2) {
            iv.setImageResource(R.drawable.diamonds_2);
        } else if (rank == 1) {
            iv.setImageResource(R.drawable.diamonds_1);
        } else {
            iv.setVisibility(View.INVISIBLE);
        }

        return rowView;
    }

}
