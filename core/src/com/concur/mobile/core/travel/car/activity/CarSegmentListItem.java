/**
 * 
 */
package com.concur.mobile.core.travel.car.activity;

import java.util.Calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.car.data.CarSegment;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> for rendering a car segment within an itinerary display.
 */
public class CarSegmentListItem extends ListItem {

    private CarSegment carSeg;

    private boolean showLongPressText;

    public CarSegmentListItem(CarSegment carSeg, boolean showLongPressText, int listItemViewType) {
        this.carSeg = carSeg;
        this.showLongPressText = showLongPressText;
        this.listItemViewType = listItemViewType;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.segmentlist_car, null);

        final Calendar startDateLocal = carSeg.getStartDateLocal();
        final String timeText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_ONLY_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListCarPickup)).setText(timeText);
        final String ampmText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_AMPM_DISPLAY, startDateLocal);
        ((TextView) v.findViewById(R.id.segmentListCarPickupAMPM)).setText(ampmText);

        ((TextView) v.findViewById(R.id.segmentListCarVendor)).setText(carSeg.vendorName);

        String from;
        if (!isBlank(carSeg.startAddress)) {
            from = carSeg.startAddress;
        } else {
            from = com.concur.mobile.base.util.Format.localizeText(context, R.string.segmentlist_car_from,
                    carSeg.startCityCode, carSeg.startAirportName);
        }
        ((TextView) v.findViewById(R.id.segmentListCarFrom)).setText(from);
        String city = isBlank(carSeg.startCity) ? carSeg.startAirportCity : carSeg.startCity;

        String fromCity = com.concur.mobile.base.util.Format.localizeText(context, R.string.general_citycountry, city,
                carSeg.startAirportState);
        ((TextView) v.findViewById(R.id.segmentListCarFromCity)).setText(fromCity);

        // Set the segment object as the view tag.
        v.setTag(carSeg);
        if (showLongPressText) {
            TextView txtView = (TextView) v.findViewById(R.id.segmentListLongPress);
            if (txtView != null) {
                txtView.setVisibility(View.VISIBLE);
            }
        }
        return v;

    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Ye old blank string helper
     */
    protected boolean isBlank(String s) {
        return (s == null || s.length() == 0);
    }

}
