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
import com.concur.mobile.core.travel.data.ParkingSegment;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> for rendering a parking segment within an itinerary display.
 */
public class ParkingSegmentListItem extends ListItem {

    private ParkingSegment prkSeg;

    private boolean showLongPressText;

    public ParkingSegmentListItem(ParkingSegment prkSeg, boolean showLongPressText, int listItemViewType) {
        this.prkSeg = prkSeg;
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
        View v = inflater.inflate(R.layout.segmentlist_parking, null);

        final Calendar startDateLocal = prkSeg.getStartDateLocal();
        final String timeText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_ONLY_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListParkingTime)).setText(timeText);
        final String ampmText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_AMPM_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListParkingTimeAMPM)).setText(ampmText);

        ((TextView) v.findViewById(R.id.segmentListParkingVendor)).setText(prkSeg.getVendorName());

        ((TextView) v.findViewById(R.id.segmentListParkingDropoff)).setText(prkSeg.startLocation);

        String confirm = com.concur.mobile.base.util.Format.localizeText(context, R.string.general_confirmnum,
                prkSeg.confirmNumber);
        ((TextView) v.findViewById(R.id.segmentListParkingConfirm)).setText(confirm);

        // Set the segment object as the view tag.
        v.setTag(prkSeg);
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
