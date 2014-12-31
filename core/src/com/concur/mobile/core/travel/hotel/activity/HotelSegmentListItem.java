/**
 * 
 */
package com.concur.mobile.core.travel.hotel.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.travel.hotel.data.HotelSegment;
import com.concur.mobile.core.view.ListItem;

/**
 * An extension of <code>ListItem</code> for rendering a hotel segment within an itinerary display.
 */
public class HotelSegmentListItem extends ListItem {

    private HotelSegment hotSeg;

    private boolean showLongPressText;

    public HotelSegmentListItem(HotelSegment hotSeg, boolean showLongPressText, int listItemViewType) {
        this.hotSeg = hotSeg;
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
        View v = inflater.inflate(R.layout.segmentlist_hotel, null);

        // No time for hotel in 8.0 (MOB-6277). The time is in the feed but needs conversion
        // work. Do that and display time again in 8.1 (MOB-6275)
        // final Calendar startDateLocal = hotSeg.getStartDateLocal();
        // final String timeText = FormatUtil.safeFormatCalendar(FormatUtil.SHORT_TIME_ONLY_DISPLAY, startDateLocal);
        // ((TextView) v.findViewById(R.id.segmentListHotelTime)).setText(timeText);
        //
        // final String ampmText = FormatUtil.safeFormatCalendar(FormatUtil.SHORT_TIME_AMPM_DISPLAY, startDateLocal);
        // ((TextView) v.findViewById(R.id.segmentListHotelTimeAMPM)).setText(ampmText);

        ((TextView) v.findViewById(R.id.segmentListHotelVendor)).setText(hotSeg.segmentName);

        ((TextView) v.findViewById(R.id.segmentListHotelAddress1)).setText(hotSeg.startAddress);

        String addr2 = Format.localizeText(context, R.string.general_address2, hotSeg.startCity, hotSeg.startState,
                hotSeg.startPostCode);
        ((TextView) v.findViewById(R.id.segmentListHotelAddress2)).setText(addr2);

        // Set the segment object as the view tag.
        v.setTag(hotSeg);
        if (showLongPressText) {
            TextView txtView = (TextView) v.findViewById(R.id.segmentListLongPress);
            if (txtView != null) {
                txtView.setVisibility(View.VISIBLE);
            }
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
