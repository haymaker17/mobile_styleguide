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
import com.concur.mobile.core.travel.data.DiningSegment;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> for rendering a dining segment within an itinerary display.
 */
public class DiningSegmentListItem extends ListItem {

    private DiningSegment dinSeg;

    private boolean showLongPressText;

    public DiningSegmentListItem(DiningSegment dinSeg, boolean showLongPressText, int listItemViewType) {
        this.dinSeg = dinSeg;
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
        View v = inflater.inflate(R.layout.segmentlist_dining, null);

        final Calendar startDateLocal = dinSeg.getStartDateLocal();
        final String timeText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_ONLY_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListDiningTime)).setText(timeText);
        final String ampmText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_AMPM_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListDiningTimeAMPM)).setText(ampmText);

        ((TextView) v.findViewById(R.id.segmentListDiningVendor)).setText(dinSeg.segmentName);

        ((TextView) v.findViewById(R.id.segmentListDiningAddress1)).setText(dinSeg.startAddress);

        String addr2 = com.concur.mobile.base.util.Format.localizeText(context, R.string.general_address2,
                dinSeg.startCity, dinSeg.startState, dinSeg.startPostCode);
        ((TextView) v.findViewById(R.id.segmentListDiningAddress2)).setText(addr2);

        // Set the segment object as the view tag.
        v.setTag(dinSeg);
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
