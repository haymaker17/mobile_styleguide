package com.concur.mobile.core.travel.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.ListItem;

public class TripListItem extends ListItem {

    public Trip trip;

    public TripListItem(Trip trip, int listItemViewType) {
        this.listItemViewType = listItemViewType;
        this.trip = trip;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        // Create the main row container and static elements
        View tripView = inflater.inflate(R.layout.triplist_row, null);
        ((TextView) tripView.findViewById(R.id.tripListTripName)).setText(trip.name);
        ((TextView) tripView.findViewById(R.id.tripListTripSpan)).setText(trip.getDateSpan());

        // show the trip state message for every trip
        if (trip.getFormattedTripStateMessages().length() > 0) {
            View tripItemView = tripView.findViewById(R.id.tripListTripStateMessage);
            ((TextView) tripItemView).setText(trip.getFormattedTripStateMessages());
            tripItemView.setVisibility(View.VISIBLE);
        }

        return tripView;
    }

}
