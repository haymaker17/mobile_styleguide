package com.concur.mobile.platform.ui.travel.hotel.activity;

import java.util.Calendar;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.concur.mobile.eva.service.EvaApiRequest.BookingSelection;
import com.concur.mobile.eva.service.EvaHotelReply;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.activity.VoiceSearchActivity;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.util.Format;

public class HotelVoiceSearchActivity extends VoiceSearchActivity {

    public final static String CLS_TAG = HotelVoiceSearchActivity.class.getSimpleName();

    private final static String RESET_UI_ON_RESUME = "RESET_UI_ON_RESUME";

    /**
     * 
     * @param hotelSearch
     */
    @Override
    public void doHotelSearch(EvaHotelReply hotelSearch) {

        Log.d(Const.LOG_TAG, CLS_TAG + ".doHotelSearch: invoking Hotel Search MWS...");

        String location = "LONDON";// hotelSearch.locationName;

        // Setup Hotel intents.
        if (resultsIntent == null) {
            resultsIntent = (Intent) getIntent().clone();
            resultsIntent.setClass(this, HotelSearchAndResultActivity.class);
        }

        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, location);
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_LATITUDE, "51.50722");// hotelSearch.latitude);
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_LONGITUDE, "-0.12750");// hotelSearch.longitude);

        Calendar checkInDateCal = Calendar.getInstance();// EvaTime.convertToConcurServerCalendar(hotelSearch.checkInDate, "UTC");
                                                         // // MOB-11600
        Calendar checkOutDateCal = Calendar.getInstance();// EvaTime.convertToConcurServerCalendar(hotelSearch.checkOutDate,
                                                          // "UTC"); // MOB-11600
        checkOutDateCal.add(Calendar.DAY_OF_MONTH, 1);

        // resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN,
        // Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, checkInDateCal));
        // resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR, checkInDateCal);
        // resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT,
        // Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, checkOutDateCal));
        // resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR, checkOutDateCal);

        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN,
                Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, checkInDateCal));
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR, checkInDateCal);
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT,
                Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, checkOutDateCal));
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR, checkOutDateCal);

        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID, distanceUnit);

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
            String s = Format.localizeText(this, R.string.hotel_voice_search_criteria, locationNameShort,
                    shortCheckInDate, shortCheckOutDate);

            // Set the chat text.
            showResponseText(s);

            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".doHotelSearch: TTS is null!");

            showErrorMessage();
            cancelSearch(false);

            return;
        }

        startActivityForResult(resultsIntent, 1);

    }

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

    @Override
    public void logErrorFlurryEvent(String arg0) {
        // TODO Auto-generated method stub

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

    @Override
    protected void cancelMwsRequests() {
        // TODO Auto-generated method stub

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

    @Override
    public Intent getResultsIntent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Intent getNoResultsIntent() {
        // TODO Auto-generated method stub
        return null;
    }

}
