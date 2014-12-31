/**
 * 
 */
package com.concur.mobile.core.travel.air.activity;

import java.util.Calendar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.air.data.AlternativeCOS;
import com.concur.mobile.core.travel.air.data.Flight;
import com.concur.mobile.core.travel.data.SegmentOption;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.StyleableSpannableStringBuilder;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> to render air choice entries in a list.
 */
public class AlternativeFlightScheduleListItem extends ListItem {

    private static final String CLS_TAG = AlternativeFlightScheduleListItem.class.getSimpleName();

    private SegmentOption segmentOption;

    /**
     * Constructs an instance of <code>SegmentOption</code>.
     * 
     * @param segmentOption
     *            the segment option having all data.
     */
    public AlternativeFlightScheduleListItem(SegmentOption segmentOption) {
        this.segmentOption = segmentOption;
    }

    /**
     * Gets the instance of <code>SegmentOption</code>
     * 
     * @return the instance of <code>SegmentOption</code>.
     */
    public SegmentOption getAirChoice() {
        return segmentOption;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.ListItem#buildView(android.content.Context, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View rowView = null;
        StringBuilder strBuilder = new StringBuilder("");
        LayoutInflater inflater = null;
        if (convertView == null) {
            // Inflate a new view.
            inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.air_result_alternative_list_row, null);
        } else {
            rowView = convertView;
        }
        // Populate main row container and static elements
        if (segmentOption.flights != null && segmentOption.flights.size() > 0) {
            Flight firstFlightForRow = segmentOption.flights.get(0);
            if (firstFlightForRow != null) {
                Flight firstFlight = firstFlightForRow;
                // Set the carrier name.
                TextView txtView = (TextView) rowView.findViewById(R.id.alternative_list_item_airlineName);
                if (txtView != null) {
                    strBuilder.append(context.getString(R.string.segment_air_label_flight).toString());
                    strBuilder.append(" ");
                    strBuilder.append(firstFlight.flightNum);
                    txtView.setText(strBuilder.toString());
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate airline name text view!");
                }
                // set number of available seats.
                txtView = (TextView) rowView.findViewById(R.id.alternative_list_item_seats_value);
                if (txtView != null) {
                    AlternativeCOS cos = firstFlight.getCOS();
                    if (cos != null) {
                        txtView.setText(cos.getSeats() + "+ ");
                    } else {
                        txtView.setText("0 ");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate airline name text view!");
                }
                // Dynamically create segment row objects and add them to the segment view group.
                ViewGroup segmentList = (ViewGroup) rowView.findViewById(R.id.alternative_list_item_segment_list);

                if (segmentList != null) {
                    // Retrieve the last flights.
                    Flight lastFlight = segmentOption.flights.get(segmentOption.flights.size() - 1);
                    View segView = null;

                    if (convertView == null) {
                        if (inflater == null) {
                            inflater = LayoutInflater.from(context);
                        }
                        segView = inflater.inflate(R.layout.air_result_segment_row, null);
                        segmentList.addView(segView);
                    } else {
                        segView = segmentList.getChildAt(segmentOption.flights.indexOf(firstFlight));
                    }
                    if (segView != null) {
                        // set segment view .
                        txtView = (TextView) segView.findViewById(R.id.segment_location_time);
                        if (txtView != null) {
                            StyleableSpannableStringBuilder spanStrBldr = new StyleableSpannableStringBuilder();
                            spanStrBldr.appendBold(firstFlight.departureAirport);
                            spanStrBldr.append(' ');
                            spanStrBldr.append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_OF_WEEK_TIME_DISPLAY,
                                    firstFlight.departureDateTime));
                            spanStrBldr.append(" - ");
                            spanStrBldr.appendBold(lastFlight.arrivalAirport);
                            spanStrBldr.append(' ');
                            spanStrBldr.append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_OF_WEEK_TIME_DISPLAY,
                                    lastFlight.arrivalDateTime));
                            txtView.setText(spanStrBldr);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".buildView: unable to locate 'segment location time' text view!");
                        }
                        // Set the duration, number of stops and fare type.
                        txtView = (TextView) segView.findViewById(R.id.segment_duration_stops);
                        if (txtView != null) {
                            strBuilder.setLength(0);
                            // Elapsed time.
                            String time = FormatUtil.formatElapsedTime(context, segmentOption.totalElapsedTime);
                            strBuilder.append(time);
                            if (strBuilder.length() > 0) {
                                strBuilder.append(" / ");
                            }
                            // Number of stops.
                            int numStops = segmentOption.flights.size() - 1;
                            if (numStops == 1) {
                                strBuilder.append(com.concur.mobile.base.util.Format.localizeText(context,
                                        R.string.air_results_title_one_non_stop));
                            } else {
                                strBuilder.append(com.concur.mobile.base.util.Format.localizeText(context,
                                        R.string.air_results_title_multi_stop, numStops));
                            }
                            txtView.setText(strBuilder.toString());
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".buildView: unable to locate 'segment duration stops' text view!");
                        }
                        // Show/hide overnight icon.
                        ImageView imgView = (ImageView) segView.findViewById(R.id.overnight);
                        if (imgView != null) {
                            if (firstFlight.departureDateTime != null
                                    && lastFlight.arrivalDateTime != null
                                    && firstFlight.departureDateTime.get(Calendar.DAY_OF_YEAR) != lastFlight.arrivalDateTime
                                            .get(Calendar.DAY_OF_YEAR)) {
                                imgView.setVisibility(View.VISIBLE);
                            } else {
                                imgView.setVisibility(View.GONE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate 'overnight' image view!");
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate segment list view group!");
                }

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: first airbooking segment is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: airchoice segments is null or empty!");
        }

        return rowView;
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
