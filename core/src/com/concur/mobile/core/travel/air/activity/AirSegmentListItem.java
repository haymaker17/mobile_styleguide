/**
 * 
 */
package com.concur.mobile.core.travel.air.activity;

import java.util.Calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.air.data.AirSegment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> for rendering an air segment within an itinerary display.
 */
public class AirSegmentListItem extends ListItem {

    private AirSegment airSeg;

    boolean showLongPressText;

    public AirSegmentListItem(AirSegment airSeg, boolean showLongPressText, int listItemViewType) {
        this.airSeg = airSeg;
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
        View v = inflater.inflate(R.layout.segmentlist_air, null);

        final Calendar startDateLocal = airSeg.getStartDateLocal();
        final String timeText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_ONLY_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListAirTime)).setText(timeText);

        final String ampmText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_AMPM_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListAirTimeAMPM)).setText(ampmText);

        String depart = com.concur.mobile.base.util.Format.localizeText(context, R.string.segmentlist_air_fromto,
                airSeg.startAirportCity, airSeg.endAirportCity);
        ((TextView) v.findViewById(R.id.segmentListAirFromTo)).setText(depart);

        StringBuilder flight = new StringBuilder(airSeg.vendorName).append(' ').append(airSeg.flightNumber);
        ((TextView) v.findViewById(R.id.segmentListAirFlight)).setText(flight);

        // Set the terminal.
        String terminal = Const.NA;
        if (airSeg.startTerminal != null && airSeg.startTerminal.trim().length() > 0) {
            terminal = airSeg.startTerminal;
        }
        ((TextView) v.findViewById(R.id.segmentListAirTerminal)).setText(com.concur.mobile.base.util.Format
                .localizeText(context, R.string.segmentlist_air_terminal, terminal));

        // Set the gate.
        String gate = Const.NA;
        if (airSeg.startGate != null && airSeg.startGate.trim().length() > 0) {
            gate = airSeg.startGate;
        }
        ((TextView) v.findViewById(R.id.segmentListAirGate)).setText(com.concur.mobile.base.util.Format.localizeText(
                context, R.string.segmentlist_air_gate, gate));

        // Set the segment object as the view tag.
        v.setTag(airSeg);
        if (showLongPressText) {
            // TextView txtView = (TextView) v.findViewById(R.id.segmentListLongPress);
            // if (txtView != null) {
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
