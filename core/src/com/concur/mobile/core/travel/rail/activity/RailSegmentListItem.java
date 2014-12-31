/**
 * 
 */
package com.concur.mobile.core.travel.rail.activity;

import java.util.Calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.rail.data.RailSegment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> for rendering a rail segment within an itinerary display.
 */
public class RailSegmentListItem extends ListItem {

    private RailSegment railSeg;

    private boolean showLongPressText;

    public RailSegmentListItem(RailSegment railSeg, boolean showLongPressText, int listItemViewType) {
        this.railSeg = railSeg;
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
        View v = inflater.inflate(R.layout.segmentlist_rail, null);

        final Calendar startDateLocal = railSeg.getStartDateLocal();
        final String timeText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_ONLY_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListRailTime)).setText(timeText);
        final String ampmText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_AMPM_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListRailTimeAMPM)).setText(ampmText);

        ((TextView) v.findViewById(R.id.segmentListRailFrom)).setText(railSeg.startRailStationLocalized);

        StringBuilder train = new StringBuilder(railSeg.vendorName).append(' ').append(railSeg.trainNumber);
        ((TextView) v.findViewById(R.id.segmentListRailTrain)).setText(train);

        String platform;
        if (railSeg.startPlatform != null && railSeg.startPlatform.trim().length() > 0) {
            platform = com.concur.mobile.base.util.Format.localizeText(context, R.string.segmentlist_rail_platform,
                    railSeg.startPlatform);
        } else {
            platform = com.concur.mobile.base.util.Format.localizeText(context, R.string.segmentlist_rail_platform,
                    Const.NA);
        }
        ((TextView) v.findViewById(R.id.segmentListRailPlatform)).setText(platform);

        String wagon;
        if (railSeg.wagonNumber != null && railSeg.wagonNumber.trim().length() > 0) {
            wagon = com.concur.mobile.base.util.Format.localizeText(context, R.string.segmentlist_rail_wagon,
                    railSeg.wagonNumber);
        } else {
            wagon = com.concur.mobile.base.util.Format.localizeText(context, R.string.segmentlist_rail_wagon, Const.NA);
        }
        ((TextView) v.findViewById(R.id.segmentListRailWagon)).setText(wagon);

        // Set the segment object as the view tag.
        v.setTag(railSeg);
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
