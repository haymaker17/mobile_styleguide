/**
 * 
 */
package com.concur.mobile.core.travel.air.activity;

import java.net.URI;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.travel.air.data.AirBookingSegment;
import com.concur.mobile.core.travel.air.data.AirChoice;
import com.concur.mobile.core.travel.air.data.Flight;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ImageCache;
import com.concur.mobile.core.util.LayoutUtil;
import com.concur.mobile.core.util.StyleableSpannableStringBuilder;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> to render air choice entries in a list.
 */
public class AirChoiceListItem extends ListItem {

    private static final String CLS_TAG = AirChoiceListItem.class.getSimpleName();

    protected AirChoice airChoice;

    /**
     * Constructs an instance of <code>AirChoiceListItem</code> backed by <code>airChoice</code>.
     * 
     * @param airChoice
     *            the air choice.
     */
    public AirChoiceListItem(AirChoice airChoice) {
        this.airChoice = airChoice;
    }

    /**
     * Gets the instance of <code>AirChoice</code> backing this <code>AirChoiceListItem</code> instance.
     * 
     * @return the instance of <code>AirChoice</code>.
     */
    public AirChoice getAirChoice() {
        return airChoice;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.ListItem#buildView(android.content.Context, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View rowView = null;

        LayoutInflater inflater = null;
        if (convertView == null) {
            // Inflate a new view.
            inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.air_result_list_row, null);
        } else {
            rowView = convertView;
        }
        // Populate main row container and static elements
        if (airChoice.segments != null && airChoice.segments.size() > 0) {
            AirBookingSegment airBookSeg = airChoice.segments.get(0);
            if (airBookSeg != null) {
                if (airBookSeg.flights != null && airBookSeg.flights.size() > 0) {
                    Flight frstSegFrstFlt = airBookSeg.flights.get(0);
                    if (frstSegFrstFlt != null) {
                        // Set the airline logo.
                        ImageView logoImg = (ImageView) rowView.findViewById(R.id.airlineLogo);
                        String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
                        StringBuilder sb = new StringBuilder();
                        sb.append(serverAdd);
                        sb.append("/images/trav/a_small/").append(frstSegFrstFlt.carrier).append(".gif");
                        URI uri = URI.create(sb.toString());
                        // Set the list item tag to the uri, this tag value is used in 'ListItemAdapter.refreshView'
                        // to refresh the appropriate view items once images have been loaded.
                        listItemTag = uri;
                        // Attempt to load the image from the image cache, if not there, then the
                        // ImageCache will load it asynchronously and this view will be updated via
                        // the ImageCache broadcast receiver available in BaseActivity.
                        ImageCache imgCache = ImageCache.getInstance(context);
                        Bitmap bitmap = imgCache.getBitmap(uri, null);
                        if (bitmap != null) {
                            logoImg.setImageBitmap(bitmap);
                            logoImg.setVisibility(View.VISIBLE);
                        } else {
                            // Since the bitmap isn't available at the moment, set the visibility to 'INVISIBLE' so that
                            // the client is not showing a previously loaded image for a different carrier!
                            logoImg.setVisibility(View.INVISIBLE);
                        }

                        // Set the carrier name.
                        TextView txtView = (TextView) rowView.findViewById(R.id.airlineName);
                        if (txtView != null) {
                            txtView.setText(frstSegFrstFlt.getCarrierName());
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate airline name text view!");
                        }

                        // Set the cost.
                        txtView = (TextView) rowView.findViewById(R.id.fare_cost);
                        if (txtView != null) {
                            txtView.setText(FormatUtil.formatAmount(airChoice.fare, context.getResources()
                                    .getConfiguration().locale, airChoice.crnCode, true, true));
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate cost text view!");
                        }

                        // set the Travel Points
                        LayoutUtil.initTravelPointsAtItemLevel(rowView, R.id.travel_points, airChoice.travelPoints);

                        setPreferenceRanking(rowView);

                        // Set any rule violation.
                        int enforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(airChoice.violations);
                        ImageView violationIconView = (ImageView) rowView.findViewById(R.id.violation_icon);
                        switch (ViewUtil.getRuleEnforcementLevel(enforcementLevel)) {
                        case NONE: {
                            if (violationIconView != null) {
                                violationIconView.setVisibility(View.GONE);
                            }
                            break;
                        }
                        case WARNING: {
                            // Show yellow exception.
                            if (violationIconView != null) {
                                violationIconView.setImageResource(R.drawable.icon_yellowex);
                                violationIconView.setVisibility(View.VISIBLE);
                            }
                            break;
                        }
                        case ERROR: {
                            // Show red exception.
                            if (violationIconView != null) {
                                violationIconView.setImageResource(R.drawable.icon_redex);
                                violationIconView.setVisibility(View.VISIBLE);
                            }
                            break;
                        }
                        case INACTIVE: {
                            // Hide the violation icon.
                            if (violationIconView != null) {
                                violationIconView.setVisibility(View.GONE);
                            }
                            break;
                        }
                        case HIDE: {
                            // No-op.
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".buildView: airchoice has rule enforcement level of 'hidden'!");
                            break;
                        }
                        }

                        // Show the refundable text.
                        View refundableView = rowView.findViewById(R.id.refundable);
                        if (refundableView != null) {
                            if (airChoice.refundable != null) {
                                if (airChoice.refundable) {
                                    refundableView.setVisibility(View.VISIBLE);
                                } else {
                                    refundableView.setVisibility(View.GONE);
                                }
                            } else {
                                refundableView.setVisibility(View.GONE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate 'refundable' text view!");
                        }

                        // Dynamically create segment row objects and add them to the segment view group.
                        ViewGroup segmentList = (ViewGroup) rowView.findViewById(R.id.segment_list);
                        StringBuilder strBldr = new StringBuilder();
                        if (segmentList != null) {
                            for (AirBookingSegment airBkSeg : airChoice.segments) {
                                // Grab the first/last flights in the current air booking segment.
                                Flight airBkSegFstFlt = airBkSeg.flights.get(0);
                                Flight airBkSegLstFlt = airBkSeg.flights.get(airBkSeg.flights.size() - 1);
                                View segView = null;
                                if (convertView == null) {
                                    if (inflater == null) {
                                        inflater = LayoutInflater.from(context);
                                    }
                                    segView = inflater.inflate(R.layout.air_result_segment_row, null);
                                    segmentList.addView(segView);
                                } else {
                                    segView = segmentList.getChildAt(airChoice.segments.indexOf(airBkSeg));
                                }
                                // Set the departure/destination IATA codes and times.
                                txtView = (TextView) segView.findViewById(R.id.segment_location_time);
                                if (txtView != null) {
                                    StyleableSpannableStringBuilder spanStrBldr = new StyleableSpannableStringBuilder();
                                    spanStrBldr.appendBold(airBkSegFstFlt.startIATA);
                                    spanStrBldr.append(' ');
                                    spanStrBldr.append(Format
                                            .safeFormatCalendar(FormatUtil.SHORT_DAY_OF_WEEK_TIME_DISPLAY,
                                                    airBkSegFstFlt.departureDateTime));
                                    spanStrBldr.append(" - ");
                                    spanStrBldr.appendBold(airBkSegLstFlt.endIATA);
                                    spanStrBldr.append(' ');
                                    spanStrBldr.append(Format.safeFormatCalendar(
                                            FormatUtil.SHORT_DAY_OF_WEEK_TIME_DISPLAY, airBkSegLstFlt.arrivalDateTime));
                                    txtView.setText(spanStrBldr);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".buildView: unable to locate 'segment location time' text view!");
                                }
                                // Set the duration, number of stops and fare type.
                                txtView = (TextView) segView.findViewById(R.id.segment_duration_stops);
                                if (txtView != null) {
                                    strBldr.setLength(0);
                                    // Elapsed time.
                                    int hours = (airBkSeg.elapsedTime / 60);
                                    int minutes = (airBkSeg.elapsedTime % 60);
                                    if (hours > 0) {
                                        strBldr.append(Integer.toString(hours));
                                        strBldr.append('h');
                                    }
                                    if (minutes > 0) {
                                        if (strBldr.length() > 0) {
                                            strBldr.append(' ');
                                        }
                                        strBldr.append(Integer.toString(minutes));
                                        strBldr.append('m');
                                    }
                                    if (strBldr.length() > 0) {
                                        strBldr.append(" / ");
                                    }
                                    // Number of stops.
                                    int numStops = airBkSeg.getNumberOfStops();
                                    if (numStops == 1) {
                                        strBldr.append(com.concur.mobile.base.util.Format.localizeText(context,
                                                R.string.air_results_title_one_non_stop));
                                    } else {
                                        strBldr.append(com.concur.mobile.base.util.Format.localizeText(context,
                                                R.string.air_results_title_multi_stop, numStops));
                                    }
                                    // Fare type.
                                    // MOB-10263. According to LOC there is no need to show Fare Type like iOS.
                                    // String multiFareTypeTitle = context.getString(R.string.air_search_multi_fare_type);
                                    // String fareTypeTitle = airBkSeg.getFareTypeTitle(multiFareTypeTitle);
                                    // if( fareTypeTitle != null ) {
                                    // if( strBldr.length() > 0 ) {
                                    // strBldr.append( " / ");
                                    // }
                                    // strBldr.append(fareTypeTitle);
                                    // }
                                    txtView.setText(strBldr.toString());
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".buildView: unable to locate 'segment duration stops' text view!");
                                }
                                // Show/hide overnight icon.
                                ImageView imgView = (ImageView) segView.findViewById(R.id.overnight);
                                if (imgView != null) {
                                    if (airBkSegFstFlt.departureDateTime.get(Calendar.DAY_OF_YEAR) != airBkSegLstFlt.arrivalDateTime
                                            .get(Calendar.DAY_OF_YEAR)) {
                                        imgView.setVisibility(View.VISIBLE);
                                    } else {
                                        imgView.setVisibility(View.GONE);
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".buildView: unable to locate 'overnight' image view!");
                                }
                                // Show the gds name text.
                                txtView = (TextView) rowView.findViewById(R.id.air_gds_name);
                                if (txtView != null && airChoice.gdsName != null) {
                                    ViewUtil.showGDSName(context, txtView, airChoice.gdsName);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate segment list view group!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: first segment, first flight is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: first airbooking segment flights is null or empty!");
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

    protected void setPreferenceRanking(View rowView) {
        // Set the preference ranking.
        ImageView imgView = (ImageView) rowView.findViewById(R.id.diamonds);
        if (imgView != null) {
            int rank = AirResultsList.getAirChoicePreference(airChoice);
            if (rank >= 3) {
                imgView.setImageResource(R.drawable.diamonds_3);
                imgView.setVisibility(View.VISIBLE);
            } else if (rank == 2) {
                imgView.setImageResource(R.drawable.diamonds_2);
                imgView.setVisibility(View.VISIBLE);
            } else if (rank == 1) {
                imgView.setImageResource(R.drawable.diamonds_1);
                imgView.setVisibility(View.VISIBLE);
            } else {
                imgView.setVisibility(View.GONE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate preference image view!");
        }
    }

}
