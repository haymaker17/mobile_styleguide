package com.concur.mobile.core.travel.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.Offer;
import com.concur.mobile.core.view.AsyncImageView;
import com.concur.mobile.core.view.ListItem;

public class OfferListItem extends ListItem {

    private Offer offer;

    public OfferListItem(Offer o, int listItemViewType) {
        this.listItemViewType = listItemViewType;
        offer = o;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.segmentlist_offer, null);

        // Set the icon
        AsyncImageView icon = (AsyncImageView) v.findViewById(R.id.segmentListSegmentIconView);
        icon.setAsyncUri(offer.content.getVendorImageURI(context));

        // The offer text
        TextView tv = (TextView) v.findViewById(R.id.segmentListOfferTitle);
        tv.setText(offer.content.title);

        // Set the offer content as the tag
        v.setTag(offer);

        return v;
    }

}
