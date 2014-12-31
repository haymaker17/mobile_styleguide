/**
 * 
 */
package com.concur.mobile.core.travel.activity;

import java.util.Calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.RideSegment;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> for rendering a ride segment within an itinerary display.
 */
public class RideSegmentListItem extends ListItem {

    private RideSegment rideSeg;

    private boolean showLongPressText;

    public RideSegmentListItem(RideSegment rideSeg, boolean showLongPressText, int listItemViewType) {
        this.rideSeg = rideSeg;
        this.showLongPressText = showLongPressText;
        this.listItemViewType = listItemViewType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.ListItem#buildView(android.content.Context, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.segmentlist_ride, null);

        final Calendar startDateLocal = rideSeg.getStartDateLocal();
        final String timeText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_ONLY_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListRideTime)).setText(timeText);
        final String ampmText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_AMPM_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListRideTimeAMPM)).setText(ampmText);

        ((TextView) v.findViewById(R.id.segmentListRideVendor)).setText(rideSeg.segmentName);

        if (rideSeg.startCityCode != null && rideSeg.startCityCode.length() > 0) {
            // Airport
            ((TextView) v.findViewById(R.id.segmentListRideAddress)).setText(rideSeg.startCityCode);
            ((TextView) v.findViewById(R.id.segmentListRideAddress2)).setText("");
        } else {
            // Just some address
            ((TextView) v.findViewById(R.id.segmentListRideAddress)).setText(rideSeg.startAddress);
            String addr2 = com.concur.mobile.base.util.Format.localizeText(context, R.string.general_address2,
                    rideSeg.startCity, rideSeg.startState, rideSeg.startPostCode);
            ((TextView) v.findViewById(R.id.segmentListRideAddress2)).setText(addr2);

        }

        // Set the segment object as the view tag.
        v.setTag(rideSeg);
        if (showLongPressText) {
            // TextView txtView = (TextView) v.findViewById(R.id.segmentListLongPress);
            // if( txtView != null ) {
            // txtView.setVisibility(View.VISIBLE);
            // }
        }
        return v;
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
