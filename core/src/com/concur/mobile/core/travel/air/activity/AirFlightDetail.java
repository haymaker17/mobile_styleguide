/**
 * 
 */
package com.concur.mobile.core.travel.air.activity;

import java.io.Serializable;
import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.data.SystemConfig;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.activity.SellOptionFieldsView;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.air.activity.AirSearch.SearchMode;
import com.concur.mobile.core.travel.air.data.AirBookingSegment;
import com.concur.mobile.core.travel.air.data.AirChoice;
import com.concur.mobile.core.travel.air.data.AirDictionaries;
import com.concur.mobile.core.travel.air.data.Flight;
import com.concur.mobile.core.travel.air.service.AirFilterReply;
import com.concur.mobile.core.travel.air.service.AirSearchReply;
import com.concur.mobile.core.travel.air.service.AirSellRequest;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.data.RefundableInfo;
import com.concur.mobile.core.travel.data.SellOptionField;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.LayoutUtil;
import com.concur.mobile.core.util.StyleableSpannableStringBuilder;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.AsyncImageView;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>BaseActivity</code> for viewing air booking flight details.
 */
public class AirFlightDetail extends TravelBaseActivity {

    private static final String CLS_TAG = AirFlightDetail.class.getSimpleName();
    private static final String EXTRA_AIR_SELL_RECEIVER_KEY = "air.sell.receiver";
    private static final String EXTRA_CREDIT_CARD_CVV_NUMBER = "credit.card.cvv.number";

    protected SearchMode searchMode;
    protected LocationChoice departLocation;
    protected LocationChoice arriveLocation;
    protected Calendar departDateTime;
    protected Calendar returnDateTime;
    protected AirChoice airChoice;
    protected AirSellRequest airSellRequest;
    protected AirSellReceiver airSellReceiver;
    protected IntentFilter airSellFilter;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore any receivers.
        restoreReceivers();

        initValues(savedInstanceState);
        initUI();

        // If not an orientation change, then send a booking fields information
        // request.
        if (!orientationChange) {
            // Check whether a travel custom fields view fragment already exists. This
            // can
            // can be the case if a device gets rotated while this activity is on the
            // stack
            // and not directly visible. Example, is on HotelReserveRoom screen, go to
            // hotel room details
            // and rotate the device, then press HW back button.
            if (!hasTravelCustomFieldsView()) {
                if (shouldRequestTravelCustomFields()) {
                    sendTravelCustomFieldsRequest();
                } else {
                    initTravelCustomFieldsView();
                }
            }
        }

    }

    @Override
    protected CharSequence getBookingProgressDialogMessage() {
        return getText(R.string.air_reserving_flights);
    }

    @Override
    protected CharSequence getBookingConfirmDialogTitle() {
        return getText(R.string.air_confirm_flight);
    }

    @Override
    protected CharSequence getBookingConfirmDialogMessage() {
        // MOB-15102
        if (isBookingInstantPurchaseFare()) {
            return getInstantPurchaseWarningDialogMessage();
        }
        return getText(R.string.air_confirm_flight_message);
    }

    @Override
    protected CharSequence getBookingSucceededDialogMessage() {
        return getText(R.string.air_reservation_confirmed_message);
    }

    @Override
    protected CharSequence getAffinityChoiceDialogTitle() {
        return getText(R.string.dlg_travel_select_frequent_flyer_title);
    }

    @Override
    protected CharSequence getAffinityChoiceReminderDialogTitle() {
        return getText(R.string.dlg_air_book_frequent_flyer_warning_title);
    }

    @Override
    protected CharSequence getAffinityChoiceReminderDialogMessage() {
        return getText(R.string.dlg_air_book_frequent_flyer_warning_message);
    }

    @Override
    protected CharSequence getBookingType() {
        return getText(R.string.general_air);
    }

    @Override
    protected CharSequence getNonRefundableWarningDialogTitle() {
        return getText(R.string.dlg_air_book_non_refundable_warning_title);
    }

    @Override
    protected CharSequence getNonRefundableWarningDialogMessage() {
        CharSequence nonRefMsg = null;
        ConcurCore concurCore = getConcurCore();
        SystemConfig sysConfig = concurCore.getSystemConfig();
        if (sysConfig != null) {
            RefundableInfo refInfo = sysConfig.getRefundableInfo();
            if (refInfo != null) {
                nonRefMsg = (refInfo.message != null) ? refInfo.message : "";
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getNonRefunableWarningDialogMessage: sysConfig is null!");
        }
        return nonRefMsg;
    }

    @Override
    protected boolean getDisplayAtStart() {
        return false;
    }

    @Override
    protected CharSequence getAffinityFieldLabel() {
        return getText(R.string.air_book_frequent_flyer_title);
    }

    @Override
    protected List<Violation> getViolations() {
        List<Violation> violations = null;
        if (airChoice != null) {
            violations = airChoice.violations;
        }
        return violations;
    }

    @Override
    protected boolean getItineraryViewPromptForAdd() {
        return true;
    }

    @Override
    protected void onBookingSucceeded() {
        if (!launchedWithCliqbookTripId) {
            // Set the flag that the trip list should be refetched.
            IItineraryCache itinCache = getConcurCore().getItinCache();
            if (itinCache != null) {
                itinCache.setShouldRefetchSummaryList(true);
            }
            // Retrieve an updated trip summary list, then retrieve the detailed
            // itinerary.
            sendItinerarySummaryListRequest();
        } else {
            // Just finish the activity.
            finish();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            // Save a reference to the air sell receiver.
            if (airSellReceiver != null) {
                // Clear the activity reference, it will be reset in the 'initValues'
                // method.
                airSellReceiver.setActivity(null);
                // Store the reference in the retainer.
                retainer.put(EXTRA_AIR_SELL_RECEIVER_KEY, airSellReceiver);
            }

            // Restore the credit card cvv number
            if (cvvNumber > 0) {
                retainer.put(EXTRA_CREDIT_CARD_CVV_NUMBER, cvvNumber);
            }
        }
    }

    /**
     * Will restore any receivers that are referenced in the retainer object.
     */
    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
        if (retainer != null) {
            // Restore any receiver waiting on an air sell response.
            if (retainer.contains(EXTRA_AIR_SELL_RECEIVER_KEY)) {
                airSellReceiver = (AirSellReceiver) retainer.get(EXTRA_AIR_SELL_RECEIVER_KEY);
                if (airSellReceiver != null) {
                    airSellReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".restoreReceivers: retainer has null value for air sell receiver!");
                }
            }

            // Retrieve the credit card cvv number
            if (retainer.contains(EXTRA_CREDIT_CARD_CVV_NUMBER)) {
                cvvNumber = (Integer) retainer.get(EXTRA_CREDIT_CARD_CVV_NUMBER);
            }
        }
    }

    protected void initValues(Bundle inState) {
        Intent i = getIntent();
        searchMode = SearchMode.None;
        String mode = i.getStringExtra(Const.EXTRA_SEARCH_MODE);
        if (mode != null) {
            searchMode = SearchMode.valueOf(mode);
        }
        final Bundle departLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_FROM);
        final Bundle arriveLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_TO);
        departLocation = new LocationChoice(departLocBundle);
        arriveLocation = new LocationChoice(arriveLocBundle);
        departDateTime = (Calendar) i.getSerializableExtra(Const.EXTRA_SEARCH_DT_DEPART);
        if (searchMode != SearchMode.OneWay) {
            returnDateTime = (Calendar) i.getSerializableExtra(Const.EXTRA_SEARCH_DT_RETURN);
        }
        if (i.hasExtra(Const.EXTRA_TRAVEL_AIR_CHOICE_FARE_ID)) {
            String fareId = i.getStringExtra(Const.EXTRA_TRAVEL_AIR_CHOICE_FARE_ID);
            if (fareId != null) {
                ConcurCore ConcurCore = getConcurCore();
                AirFilterReply airFilterReply = ConcurCore.getAirFilterResults();
                if (airFilterReply != null) {
                    if (airFilterReply.choices != null) {
                        for (AirChoice airChoice : airFilterReply.choices) {
                            if (airChoice.fareId != null && airChoice.fareId.equalsIgnoreCase(fareId)) {
                                this.airChoice = airChoice;
                                break;
                            }
                        }
                        if (airChoice == null) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".initValues: unable to locate fare id!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".initValues: air filter result choices is null!");
                    }
                } else {
                    Log.i(Const.LOG_TAG, CLS_TAG + ".initValues: air filter result is not available!");
                    // TODO: Display dialog and finish the activity.
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initValues: fareId is null!");
            }
        }

        // Added for MOB-14317
        if (!orientationChange) {
            showDialog(RETRIEVE_PRE_SELL_OPTIONS_DIALOG);
            getPreSellOptions(airChoice.choiceId);
        }

        super.initValues(inState);
    }

    protected void initUI() {
        setContentView(R.layout.air_flight_detail);
        // The header
        getSupportActionBar().setTitle(R.string.air_search_flight_detail_title);

        final String departIATACode = departLocation.getIATACode();
        final String arriveIATACode = arriveLocation.getIATACode();
        // The travel header
        TextView txtView = (TextView) findViewById(R.id.travel_name);
        txtView.setText(com.concur.mobile.base.util.Format.localizeText(this, R.string.segmentlist_air_fromto,
                new Object[] { departIATACode, arriveIATACode }));
        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(departDateTime.getTime()));
        if (searchMode != SearchMode.OneWay) {
            sb.append(" - ").append(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(returnDateTime.getTime()));
        }
        txtView = (TextView) findViewById(R.id.date_span);
        txtView.setText(sb.toString());
        // Set the total price field title and value.
        initTotalPrice();
        // Populate with flight information.
        initFlightInfo();

        if (airChoice != null && !orientationChange) {
            initAirPreSellOptions();
        }

        if (orientationChange) {
            initSellOptionFieldsView();
        }

        // set Travel Points header
        // initAirTravelPointsHeader(formattedBenchmarkPrice, R.string.travel_points_air_booking_workflow_p2b_header,
        // travelPointsInBank, R.string.travel_points_air_booking_workflow_points_header);

        super.initUI();

    }

    protected View buildFlightView(LayoutInflater inflater, Flight flight) {
        View flightView = null;
        flightView = inflater.inflate(R.layout.flight_detail_flight, null);
        // Set carrier logo.
        // Set the airline logo.
        AsyncImageView aiv = (AsyncImageView) flightView.findViewById(R.id.airlineLogo);
        if (aiv != null) {
            String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
            StringBuilder sb = new StringBuilder();
            sb.append(serverAdd);
            sb.append("/images/trav/a_small/").append(flight.carrier).append(".gif");
            aiv.setAsyncUri(URI.create(sb.toString()));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildFlightView: unable to locate airline logo view!");
        }
        // Set the carrier name.
        TextView txtView = (TextView) flightView.findViewById(R.id.airlineName);
        if (txtView != null) {
            txtView.setText(flight.getCarrierName());
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildFlightView: unable to locate 'airline name' text view!");
        }
        // Set the flight number.
        txtView = (TextView) flightView.findViewById(R.id.flightNumber);
        if (txtView != null) {
            txtView.setText(Integer.toString(flight.flightNum));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildFlightView: unable to locate 'flight number' text view!");
        }
        // Show/hide the "operated by".
        boolean hasDifferentOperatingCarrier = false;
        txtView = (TextView) flightView.findViewById(R.id.operatedBy);
        if (txtView != null) {
            if (flight.operatingCarrier != null && flight.operatingCarrier.length() > 0 && flight.carrier != null
                    && !flight.operatingCarrier.equalsIgnoreCase(flight.carrier)) {
                String operatingVendorName = AirDictionaries.vendorCodeMap.get(flight.operatingCarrier);
                if (operatingVendorName == null) {
                    operatingVendorName = flight.operatingCarrier;
                }
                txtView.setText(com.concur.mobile.base.util.Format.localizeText(this, R.string.flight_operated_by,
                        operatingVendorName));
                txtView.setVisibility(View.VISIBLE);
                hasDifferentOperatingCarrier = true;
            } else {
                txtView.setVisibility(View.GONE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildFlightView: unable to locate 'operated by' text view!");
        }
        // Set the departure/destination IATA codes and times.
        txtView = (TextView) flightView.findViewById(R.id.segment_location_time);
        if (txtView != null) {
            StyleableSpannableStringBuilder spanStrBldr = new StyleableSpannableStringBuilder();
            spanStrBldr.appendBold(flight.startIATA);
            spanStrBldr.append(' ');
            spanStrBldr.append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_OF_WEEK_TIME_DISPLAY,
                    flight.departureDateTime));
            spanStrBldr.append(" - ");
            spanStrBldr.appendBold(flight.endIATA);
            spanStrBldr.append(' ');
            spanStrBldr.append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_OF_WEEK_TIME_DISPLAY,
                    flight.arrivalDateTime));
            txtView.setText(spanStrBldr);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildFlightView: unable to locate 'segment location time' text view!");
        }
        // Set the duration and number of stops.
        txtView = (TextView) flightView.findViewById(R.id.segment_duration_stops_class);
        if (txtView != null) {
            // Set duration.
            StringBuilder strBldr = new StringBuilder();
            int hours = (flight.flightTime / 60);
            int minutes = (flight.flightTime % 60);
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
            // Set number of stops.
            if (strBldr.length() > 0) {
                strBldr.append(" / ");
            }
            if (flight.numStops == 1) {
                strBldr.append(com.concur.mobile.base.util.Format.localizeText(this,
                        R.string.air_results_title_one_non_stop));
            } else {
                strBldr.append(com.concur.mobile.base.util.Format.localizeText(this,
                        R.string.air_results_title_multi_stop, flight.numStops));
            }
            // Set cabin class.
            if (strBldr.length() > 0) {
                strBldr.append(" / ");
            }
            // Southwest airlines fare types are actually mentioned in the fare title,
            // i.e., the seat classes are all
            // 'economy'.
            if (flight.carrier != null && !hasDifferentOperatingCarrier
                    && flight.carrier.equalsIgnoreCase(Const.VENDOR_SOUTHWEST) && flight.title != null) {
                strBldr.append(flight.title);
            } else if (flight.seatClass != null) {
                if (flight.seatClass.equalsIgnoreCase(Const.AIR_SEAT_CLASS_ECONOMY)) {
                    strBldr.append(getText(R.string.air_search_class_value_economy));
                } else if (flight.seatClass.equalsIgnoreCase(Const.AIR_SEAT_CLASS_PREMIUM_ECONOMY)) {
                    strBldr.append(getText(R.string.air_search_class_value_premium_economy));
                } else if (flight.seatClass.equalsIgnoreCase(Const.AIR_SEAT_CLASS_BUSINESS)) {
                    strBldr.append(getText(R.string.air_search_class_value_business));
                } else if (flight.seatClass.equalsIgnoreCase(Const.AIR_SEAT_CLASS_FIRST)) {
                    strBldr.append(getText(R.string.air_search_class_value_first));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildFlightView: unknown seat class '" + flight.seatClass + "'!");
                }
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG + ".buildFlightView: flight seat class is null!");
            }
            // Set the BIC code.
            if (strBldr.length() > 0) {
                strBldr.append(" / ");
            }
            if (flight.bic != null) {
                strBldr.append('(');
                strBldr.append(flight.bic);
                strBldr.append(')');
            }
            txtView.setText(strBldr.toString());
            // Show/hide overnight icon.
            ImageView imgView = (ImageView) flightView.findViewById(R.id.overnight);
            if (imgView != null) {
                if (flight.departureDateTime.get(Calendar.DAY_OF_YEAR) != flight.arrivalDateTime
                        .get(Calendar.DAY_OF_YEAR)) {
                    imgView.setVisibility(View.VISIBLE);
                } else {
                    imgView.setVisibility(View.GONE);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildFlightView: unable to locate 'overnight' image view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildFlightView: unable to locate 'segment duration stops' text view!");
        }
        return flightView;
    }

    protected void initFlightInfo() {
        ViewGroup airSegmentList = (ViewGroup) findViewById(R.id.flight_segment_list);
        if (airSegmentList != null) {
            if (airChoice != null) {
                if (airChoice.segments != null) {
                    LayoutInflater inflater = LayoutInflater.from(this);
                    for (AirBookingSegment airBkSeg : airChoice.segments) {
                        View airBkSegView = inflater.inflate(R.layout.flight_detail_segment, null);
                        // Set the segment title.
                        TextView txtView = (TextView) airBkSegView.findViewById(R.id.segment_title);
                        if (txtView != null) {
                            int airBkSegInd = airChoice.segments.indexOf(airBkSeg);
                            if (airBkSegInd == 0) {
                                txtView.setText(R.string.general_departure);
                            } else if (airBkSegInd == (airChoice.segments.size() - 1)) {
                                txtView.setText(R.string.general_return);
                            } else {
                                txtView.setText(R.string.general_destination);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".initFlightInfo: unable to locate 'segment_title' view!");
                        }
                        // Grab the segment flight list.
                        ViewGroup segmentFlightList = (ViewGroup) airBkSegView.findViewById(R.id.segment_flight_list);
                        if (segmentFlightList != null) {
                            if (airBkSeg.flights != null) {
                                for (Flight airBkSegFlt : airBkSeg.flights) {
                                    int flightIndex = airBkSeg.flights.indexOf(airBkSegFlt);
                                    if (flightIndex > 0) {
                                        ViewUtil.addSeparatorView(this, segmentFlightList);
                                        // Check for a layover.
                                        if (flightIndex < airBkSeg.flights.size()) {
                                            Flight prevFlight = airBkSeg.flights.get(flightIndex - 1);
                                            long curFlightTimeMillis = airBkSegFlt.departureDateTime.getTimeInMillis();
                                            long prevFlightTimeMillis = prevFlight.arrivalDateTime.getTimeInMillis();
                                            if (curFlightTimeMillis != prevFlightTimeMillis) {
                                                // Add a layover entry.
                                                TextView layOver = (TextView) inflater.inflate(
                                                        R.layout.flight_detail_layover, null);
                                                int totalLayoverMinutes = (int) ((curFlightTimeMillis - prevFlightTimeMillis) / 60000);
                                                int layoverHours = (totalLayoverMinutes / 60);
                                                int layoverMinutes = (totalLayoverMinutes % 60);
                                                if (layoverHours > 0 && layoverMinutes > 0) {
                                                    layOver.setText(com.concur.mobile.base.util.Format.localizeText(
                                                            this, R.string.air_search_flight_layover_hour_minute,
                                                            Integer.toString(layoverHours),
                                                            Integer.toString(layoverMinutes), prevFlight.endIATA));
                                                } else if (layoverHours > 0) {
                                                    layOver.setText(com.concur.mobile.base.util.Format.localizeText(
                                                            this, R.string.air_search_flight_layover_hour,
                                                            Integer.toString(layoverHours), prevFlight.endIATA));
                                                } else if (layoverMinutes > 0) {
                                                    layOver.setText(com.concur.mobile.base.util.Format.localizeText(
                                                            this, R.string.air_search_flight_layover_minute,
                                                            Integer.toString(layoverMinutes), prevFlight.endIATA));
                                                }
                                                segmentFlightList.addView(layOver);
                                                ViewUtil.addSeparatorView(this, segmentFlightList);
                                            }
                                        }
                                    }
                                    segmentFlightList.addView(buildFlightView(inflater, airBkSegFlt));
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".initFlightInfo: segment has no flights!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".initFlightInfo: unable to locate 'segment_flight_list' view!");
                        }
                        // Add the segment to the segment list.
                        airSegmentList.addView(airBkSegView);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initFlightInfo: selected airchoice has no segments!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initFlightInfo: no current air choice available!");
                setResult(Activity.RESULT_OK);
                finish();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initFlightInfo: unable to locate 'flight_segment_list' view group!");
        }
    }

    protected void initTotalPrice() {
        View totalPriceView = findViewById(R.id.total_price);
        if (totalPriceView != null) {
            // Set the field title.
            TextView txtView = (TextView) totalPriceView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.air_search_flight_detail_total_price);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initTotalPrice: unable to locate 'field_name' text view!");
            }
            // Set the field value.
            txtView = (TextView) totalPriceView.findViewById(R.id.field_value1);
            if (txtView != null) {
                StyleableSpannableStringBuilder spanStrBldr = new StyleableSpannableStringBuilder();
                String formattedAmtStr = FormatUtil.formatAmount(airChoice.fare,
                        getResources().getConfiguration().locale, airChoice.crnCode, true, true);
                spanStrBldr.appendWithStyle(new TextAppearanceSpan(this, R.style.AirFareNormal), formattedAmtStr);
                if (airChoice.refundable != null && airChoice.refundable) {
                    String refundStr = " (" + getText(R.string.general_refundable) + ")";
                    spanStrBldr.appendWithStyle(new TextAppearanceSpan(this, R.style.ListCellSmallText), refundStr);
                }
                txtView.setText(spanStrBldr);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initTotalPrice: unable to locate 'field_value' text view!");
            }

            // init Travel Points
            initTravelPointsInPrice();

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initTotalPrice: unable to locate total price group!");
        }
    }

    @Override
    protected boolean isBookingRefundable() {
        boolean refundable = true;
        if (airChoice != null) {
            refundable = airChoice.refundable;
        }
        return refundable;
    }

    @Override
    protected boolean isBookingInstantPurchaseFare() {
        return (airChoice == null ? false : airChoice.instantPurchase);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case AirSearch.RESULTS_ACTIVITY_CODE:
            setResult(resultCode, data);
            // If the result was 'OK', then finish the activity.
            if (resultCode == Activity.RESULT_OK) {
                finish();
            }
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void cancelBookingRequest() {
        if (airSellRequest != null) {
            airSellRequest.cancel();
        }
    }

    @Override
    protected void sendBookingRequest() {
        // Log the flurry event if the user completed this hotel booking using Voice.
        if (getIntent().getBooleanExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, false)) {
            EventTracker.INSTANCE.track(Flurry.CATEGORY_VOICE_BOOK, Flurry.EVENT_NAME_COMPLETED_AIR);
        }

        sendAirSellRequest();
    }

    protected void sendAirSellRequest() {
        if (ConcurCore.isConnected()) {
            ConcurService concurService = getConcurService();
            registerAirSellReceiver();
            String tripName = "";
            Flight fstSegFstFlt = airChoice.segments.get(0).flights.get(0);
            Flight fstSegLstFlg = airChoice.segments.get(0).flights.get(airChoice.segments.get(0).flights.size() - 1);
            tripName = com.concur.mobile.base.util.Format.localizeText(this,
                    R.string.air_reservation_default_trip_name, fstSegFstFlt.startIATA, fstSegLstFlg.endIATA);
            String reasonCodeId = (reasonCode != null) ? reasonCode.id : "";
            String violationText = (justificationText != null) ? justificationText : "";
            String ffProgramId = (curAffinityChoice != null) ? curAffinityChoice.id : null;
            boolean refundableOnly = getIntent().getBooleanExtra(Const.EXTRA_SEARCH_REFUNDABLE_ONLY, false);
            List<TravelCustomField> tcfs = getTravelCustomFields();
            List<SellOptionField> preSellOptionFields = getSellOptionFields();
            String cvvNumberStr = String.valueOf(cvvNumber);// com.concur.mobile.core.activity.Preferences.PREF_CRYPT.encrypt(String.valueOf(cvvNumber));

            // MOB-15483 - add the hasSellOptionFieldsView() boolean to let the AirSellRequest determine to set the socket time
            // out
            // TODO - when implemented for other DCs, we need to revisit this boolean flag as some DCs do not have flight options
            // and have only fare review
            airSellRequest = concurService.sendAirSellRequest(getUserId(), Integer.parseInt(curCardChoice.id),
                    airChoice.fareId, ffProgramId, refundableOnly, tripName, reasonCodeId, violationText, tcfs,
                    preSellOptionFields, cvvNumberStr, hasSellOptionFieldsView(), useTravelPoints);
            if (airSellRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendAirSellRequest: unable to create request to book air!");
                unregisterAirSellReceiver();
            } else {
                // Set the request object on the receiver.
                airSellReceiver.setServiceRequest(airSellRequest);
                showDialog(BOOKING_PROGRESS_DIALOG);
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * Will register an instance of <code>AirSellReceiver</code> with the application context and set the
     * <code>airSellReceiver</code> attribute.
     */
    protected void registerAirSellReceiver() {
        if (airSellReceiver == null) {
            airSellReceiver = new AirSellReceiver(this);
            if (airSellFilter == null) {
                airSellFilter = new IntentFilter(Const.ACTION_AIR_BOOK_RESULTS);
            }
            getApplicationContext().registerReceiver(airSellReceiver, airSellFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAirSellReceiver: airSellReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>AirSellReceiver</code> with the application context and set the
     * <code>airSelllReceiver</code> to <code>null</code>.
     */
    protected void unregisterAirSellReceiver() {
        if (airSellReceiver != null) {
            getApplicationContext().unregisterReceiver(airSellReceiver);
            airSellReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAirSellReceiver: airSellReceiver is null!");
        }
    }

    /**
     * 
     * @param activity
     * @param intent
     */
    protected void onHandleSuccessReservation(AirFlightDetail activity, Intent intent) {
        locateTripId(activity, intent);
        flurryEvents(activity);
        activity.showDialog(BOOKING_SUCCEEDED_DIALOG);
    } // onHandleSuccessReservation

    protected void locateTripId(AirFlightDetail activity, Intent intent) {
        Intent result = new Intent();
        activity.itinLocator = intent.getStringExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR);
        result.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, activity.itinLocator);
        if (activity.cliqbookTripId != null) {
            IItineraryCache itinCache = activity.getConcurCore().getItinCache();
            Trip trip = itinCache.getItinerarySummaryByCliqbookTripId(activity.cliqbookTripId);
            if (trip != null) {
                result.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, trip.itinLocator);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to locate trip based on cliqbook trip id!");
            }
        }
        activity.setResult(Activity.RESULT_OK, result);
    }

    protected void flurryEvents(AirFlightDetail activity) {
        // Flurry Notification
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_AIR);
        Intent launchIntent = activity.getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            params.put(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        EventTracker.INSTANCE.track(Flurry.CATEGORY_BOOK, Flurry.EVENT_NAME_RESERVE, params);

        if (getTravelPointsInBank() != null) {

            int travelPoints = (airChoice.travelPoints == null ? 0 : airChoice.travelPoints);
            logEvents(travelPoints, Flurry.PARAM_VALUE_AIR);
        }

    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the response to an air sell.
     */
    protected static class AirSellReceiver extends BaseBroadcastReceiver<AirFlightDetail, AirSellRequest> {

        /**
         * Constructs an instance of <code>AirSellReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        protected AirSellReceiver(AirFlightDetail activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(AirFlightDetail activity) {
            activity.airSellRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(BOOKING_PROGRESS_DIALOG);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(BOOKING_FAILED_DIALOG);
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            activity.onHandleSuccessReservation(activity, intent);
        }

        @Override
        protected void setActivityServiceRequest(AirSellRequest request) {
            activity.airSellRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterAirSellReceiver();
        }
    } // end AirSellReceiver

    @Override
    protected void updatePreSellOptions() {
        initAirPreSellOptions();
    }

    // Pre-sell options
    private void initAirPreSellOptions() {
        // update credit cards
        initCreditCards();

        // update travel programs i.e. Affinity Programs
        initTravelPrograms();

        // update sell options
        initSellOptionFieldsView();
    }

    private void initCreditCards() {
        initCardChoices();
        initCardChoiceView();
        initCardCVVNumberView();
    }

    private void initTravelPrograms() {
        initAffinityChoices();
        initAffinityChoiceView();
    }

    // sell options are flight options
    private void initSellOptionFieldsView() {
        if (preSellOption != null) {
            // add the sell options to the UI
            if (preSellOption.getSellOptionFields() != null && preSellOption.getSellOptionFields().size() > 0) {
                View sellOptionsView = findViewById(R.id.flight_options_fields);
                if (sellOptionsView != null) {
                    sellOptionsView.setVisibility(View.VISIBLE);
                    addSellOptionFieldsView(preSellOption.getSellOptionFields(), R.id.flight_options_fields);
                }
            }
        }
    }

    private void addSellOptionFieldsView(List<SellOptionField> sellOptionFields, int sellOptionsViewId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        sellOptionFieldsView = new SellOptionFieldsView();
        Bundle args = new Bundle();
        args.putSerializable("sellOptionFields", (Serializable) sellOptionFields);
        sellOptionFieldsView.setArguments(args);
        fragmentTransaction.add(sellOptionsViewId, sellOptionFieldsView, TRAVEL_SELL_OPTION_VIEW_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    /**
     * Will retrieve the list of <code>SellOptionField</code> objects containing edited values.
     * 
     * @return returns the list of <SellOptionField> objects containing edited values.
     */
    public List<SellOptionField> getSellOptionFields() {
        List<SellOptionField> retVal = null;
        if (sellOptionFieldsView != null) {
            retVal = sellOptionFieldsView.getSellOptionFields();
        }
        return retVal;
    }

    @Override
    protected boolean canRedeemTravelPointsAgainstViolations() {
        return airChoice.canRedeemTravelPointsAgainstViolations;
    }

    @Override
    protected int getTravelPointsToUse() {
        int tp = (airChoice.travelPoints == null ? 0 : airChoice.travelPoints);
        if (tp != 0) {
            tp = Math.abs(tp);
        }
        return tp;
    }

    @Override
    protected String getTravelPointsInBank() {
        AirSearchReply results = ((ConcurCore) getApplication()).getAirSearchResults();
        String travelPointsInBank = null;

        if (results.travelPointsBank != null && results.travelPointsBank.getPointsAvailableToSpend() != null) {
            travelPointsInBank = Integer.toString(results.travelPointsBank.getPointsAvailableToSpend());
        }
        return travelPointsInBank;
    }

    @Override
    protected void showTravelPointsInPrice() {
        View priceView = findViewById(R.id.total_price);
        if (priceView != null) {
            LayoutUtil.initTravelPointsAtItemLevel(priceView, R.id.field_value2, airChoice.travelPoints);
        }
    }

    @Override
    protected void hideTravelPointsInPrice() {
        View priceView = findViewById(R.id.total_price);
        if (priceView != null) {
            priceView.findViewById(R.id.field_value2).setVisibility(View.GONE);
        }
    }

}
