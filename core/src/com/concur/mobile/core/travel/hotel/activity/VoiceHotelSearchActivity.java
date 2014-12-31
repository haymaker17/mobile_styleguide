/**
 * Copyright (c) 2012 Concur Technologies, Inc.
 */
package com.concur.mobile.core.travel.hotel.activity;

import java.util.Calendar;

import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.eva.activity.VoiceSearchActivity;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.hotel.receiver.HotelSearchReceiver;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.eva.data.EvaTime;
import com.concur.mobile.eva.service.EvaApiRequest.BookingSelection;
import com.concur.mobile.eva.service.EvaHotelReply;
import com.concur.mobile.platform.util.Format;

/**
 * Implementation of the Voice Searching for Hotels.
 * 
 * @author Chris N. Diaz
 * 
 */
public class VoiceHotelSearchActivity extends VoiceSearchActivity {

    public final static String CLS_TAG = VoiceHotelSearchActivity.class.getSimpleName();

    private final static String RESET_UI_ON_RESUME = "RESET_UI_ON_RESUME";

    // Contains a reference to the current outstanding hotel search request.
    private ServiceRequest hotelSearchRequest;

    // Contains a reference to a receiver for handling a search response.
    private HotelSearchReceiver hotelSearchReceiver;

    // Contains the filter used to register the above receiver.
    protected final IntentFilter hotelResultsFilter = new IntentFilter(Const.ACTION_HOTEL_SEARCH_RESULTS);

    /**
     * 
     * @param hotelSearch
     */
    @Override
    public void doHotelSearch(EvaHotelReply hotelSearch) {

        Log.d(Const.LOG_TAG, CLS_TAG + ".doHotelSearch: invoking Hotel Search MWS...");

        String location = hotelSearch.locationName;
        Calendar checkInDateCal = EvaTime.convertToConcurServerCalendar(hotelSearch.checkInDate, "UTC"); // MOB-11600
        Calendar checkOutDateCal = EvaTime.convertToConcurServerCalendar(hotelSearch.checkOutDate, "UTC"); // MOB-11600

        // Setup Hotel intents.
        if (resultsIntent == null) {
            resultsIntent = (Intent) getIntent().clone();
            resultsIntent.setClass(this, HotelSearchResults.class);
        }

        if (noResultsIntent == null) {
            noResultsIntent = (Intent) getIntent().clone();
            noResultsIntent.setClass(this, HotelSearchNoResults.class);
        }

        // Extra info for the search results Intent.
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, true);
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, location);
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, checkInDateCal));
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR, checkInDateCal);
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, checkOutDateCal));
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR, checkOutDateCal);
        // Set amount restriction, if specified.
        if (hotelSearch.amountRestriction != null) {
            String amount = hotelSearch.amountRestriction.amount;
            String restriction = hotelSearch.amountRestriction.restriction;
            if (amount != null && restriction != null && restriction.equalsIgnoreCase("Less")) {
                resultsIntent.putExtra(Const.EXTRA_HOTEL_SEARCH_FILTER_LESS_THAN_AMOUNT, amount);
                resultsIntent.putExtra(Const.EXTRA_HOTEL_SEARCH_FILTER_LESS_THAN_AMOUNT_CURRENCY,
                        hotelSearch.amountRestriction.currency);
            }

        }
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            resultsIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM,
                    launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }

        // Extra info for the Intent when there are no results.
        noResultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, location);
        noResultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR, checkInDateCal);
        noResultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR, checkOutDateCal);

        if (tts != null) {

            // Try to get a shorter name to say...
            String locationNameShort = "";
            if (location != null) {
                int index = location.indexOf(",");
                if (index > 0) {
                    locationNameShort = location.substring(0, index);
                } else {
                    locationNameShort = location;
                }
            }

            // Text to Speech...
            String shortCheckInDate = toVoiceDateFormat(hotelSearch.checkInDate);
            String shortCheckOutDate = toVoiceDateFormat(hotelSearch.checkOutDate);
            String s = com.concur.mobile.base.util.Format.localizeText(this, R.string.hotel_voice_search_criteria,
                    locationNameShort, shortCheckInDate, shortCheckOutDate);

            // Set the chat text.
            showResponseText(s);

            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".doHotelSearch: TTS is null!");

            logErrorFlurryEvent(Flurry.PARAM_VALUE_SPEECH_RECOGNIZER);

            showErrorMessage();
            cancelSearch(false);

            return;
        }

        // Clear out any current results.
        getConcurCore().setHotelSearchResults(null);

        // Make the call
        ConcurService svc = getConcurService();
        if (svc != null) {
            if (hotelSearchReceiver == null) {
                hotelSearchReceiver = new HotelSearchReceiver(this);
            }
            getApplicationContext().registerReceiver(hotelSearchReceiver, hotelResultsFilter);
            hotelSearchRequest = svc.searchForHotels(checkOutDateCal, checkInDateCal, hotelSearch.namesContaining,
                    hotelSearch.latitude, hotelSearch.longitude, hotelSearch.distanceValue, hotelSearch.distanceUnit,
                    0, Const.HOTEL_RETRIEVE_COUNT);
            hotelSearchReceiver.setRequest(hotelSearchRequest);
        }

    } // doHotelSearch()

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.VoiceSearchActivity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case Const.REQUEST_CODE_BOOK_HOTEL: {
            if (resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            }
            break;
        }
        }

        super.onActivityResult(requestCode, resultCode, data);

    } // onActivityResult()

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.activity.BaseActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        boolean resetUI = getIntent().getBooleanExtra(RESET_UI_ON_RESUME, false);
        if (resetUI) {
            super.resetUI(true);
            // Be sure to set the property back to false.
            getIntent().putExtra(RESET_UI_ON_RESUME, false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.VoiceSearchActivity#getGreetingText()
     */
    @Override
    protected Spanned getGreetingText() {
        return Html.fromHtml(getString(R.string.voice_hotel_book_hint_primary) + "<br /><small><i>"
                + getString(R.string.voice_hotel_book_hint_secondary) + "</i></small>");

    } // getGreetingText()

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.VoiceSearchActivity#cancelMwsRequests()
     */
    @Override
    protected void cancelMwsRequests() {

        getIntent().putExtra(RESET_UI_ON_RESUME, false);

        // Cancel the MWS request.
        if (hotelSearchRequest != null) {
            hotelSearchRequest.cancel();
        }
    } // cancelMwsRequests()

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.AbstractTravelSearchProgress#getSearchRequest()
     */
    @Override
    public ServiceRequest getSearchRequest() {
        return hotelSearchRequest;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.core.travel.air.activity.AbstractTravelSearchProgress#setSearchRequest(com.concur.mobile.core.service
     * .ServiceRequest)
     */
    @Override
    public void setSearchRequest(ServiceRequest searchRequest) {
        this.hotelSearchRequest = searchRequest;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.AbstractTravelSearchProgress#resetHotelSearchRequest()
     */
    @Override
    public void onReceiveComplete() {

        // Flurry event indicating everything worked.
        logSuccessUsageFlurryEvent();

        if (hotelSearchRequest != null && hotelSearchRequest.isCanceled()) {
            getIntent().putExtra(RESET_UI_ON_RESUME, false);
        } else {
            getIntent().putExtra(RESET_UI_ON_RESUME, true);
        }

        this.hotelSearchRequest = null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.AbstractTravelSearchProgress#finishActivityOnNoResults()
     */
    @Override
    public boolean finishActivityOnNoResults() {
        return false;
    }

    @Override
    protected BookingSelection getBookType() {
        return BookingSelection.HOTEL;
    }

} // end VoiceHotelSearchActivity
