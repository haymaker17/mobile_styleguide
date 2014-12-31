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
import com.concur.mobile.core.travel.data.EventSegment;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> for rendering an event segment within an itinerary display.
 */
public class EventSegmentListItem extends ListItem {

    private EventSegment evtSeg;

    private boolean showLongPressText;

    public EventSegmentListItem(EventSegment evtSeg, boolean showLongPressText, int listItemViewType) {
        this.evtSeg = evtSeg;
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
        View v = inflater.inflate(R.layout.segmentlist_event, null);

        final Calendar startDateLocal = evtSeg.getStartDateLocal();
        final String timeText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_ONLY_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListEventTime)).setText(timeText);
        final String ampmText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_AMPM_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListEventTimeAMPM)).setText(ampmText);

        ((TextView) v.findViewById(R.id.segmentListEventName)).setText(evtSeg.segmentName);

        ((TextView) v.findViewById(R.id.segmentListEventAddress1)).setText(evtSeg.startAddress);

        String addr2 = com.concur.mobile.base.util.Format.localizeText(context, R.string.general_address2,
                evtSeg.startCity, evtSeg.startState, evtSeg.startPostCode);
        ((TextView) v.findViewById(R.id.segmentListEventAddress2)).setText(addr2);

        // Set the segment object as the view tag.
        v.setTag(evtSeg);
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
