package com.concur.mobile.core.travel.hotel.jarvis.activity;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.eva.data.EvaTime;
import com.concur.mobile.eva.service.EvaApiRequest.BookingSelection;
import com.concur.mobile.eva.service.EvaHotelReply;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.util.Format;

import java.util.Calendar;

/**
 * Implementation of the Voice Searching for Hotels.
 *
 * @author ratank
 */
@EventTracker.EventTrackerClassName(getClassName = Flurry.SCREEN_NAME_TRAVEL_HOTEL_VOICE_SEARCH)
public class HotelVoiceSearchActivity extends VoiceSearchActivity {

    public final static String CLS_TAG = HotelVoiceSearchActivity.class.getSimpleName();

    public final static String RESET_UI_ON_RESUME = "RESET_UI_ON_RESUME";


    @Override
    protected void onStart() {
        super.onStart();
        EventTracker.INSTANCE.activityStart(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        EventTracker.INSTANCE.activityStop(this);
    }

    /**
     * @param hotelSearch
     */
    @Override
    public void doHotelSearch(EvaHotelReply hotelSearch) {

        String location = hotelSearch.locationName;

        // Setup Hotel intents.
        if (resultsIntent == null) {
            resultsIntent = (Intent) getIntent().clone();
            resultsIntent.setClass(this, HotelSearchAndResultActivity.class);
        }

        resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, location);
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_LATITUDE, hotelSearch.latitude);
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_LONGITUDE, hotelSearch.longitude);

        // MOB-11600
        Calendar checkInDateCal = EvaTime.convertToConcurServerCalendar(hotelSearch.checkInDate, "UTC");
        Calendar checkOutDateCal = EvaTime.convertToConcurServerCalendar(hotelSearch.checkOutDate, "UTC");

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
            String s = Format
                    .localizeText(this, R.string.hotel_voice_search_criteria, locationNameShort, shortCheckInDate,
                            shortCheckOutDate);

            // Set the chat text.
            showResponseText(s);

            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".doHotelSearch: TTS is null!");

            showErrorMessage();
            cancelSearch(false);

            return;
        }

        startActivityForResult(resultsIntent, Const.REQUEST_CODE_BOOK_HOTEL);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Const.REQUEST_CODE_BACK_BUTTON_PRESSED: {
                // back button press on started activity
                getIntent().putExtra(RESET_UI_ON_RESUME, true);
                break;
            }
            case Const.REQUEST_CODE_BOOK_HOTEL: {
                Log.d(com.concur.mobile.platform.util.Const.LOG_TAG,
                        "\n\n\n ****** HotelVoiceSearchActivity onActivityResult with REQUEST_CODE_BOOK_HOTEL result code : "
                                + resultCode);
                if (resultCode == RESULT_OK) {
                    // Hotel was booked, set the result code to okay and show Itineary.
                    String itinLocator = data.getStringExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR);
                    String bookingRecordLocator = data.getStringExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR);
                    Intent i = new Intent(HotelVoiceSearchActivity.this, ShowHotelItinerary.class);
                    i.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, itinLocator);
                    i.putExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR, bookingRecordLocator);
                    Log.d(Const.LOG_TAG, CLS_TAG + ".HotelVoiceSearchActivity start activity to retrieve itinerary");
                    HotelVoiceSearchActivity.this.startActivity(i);
                    finish();
                } else {
                    getIntent().putExtra(RESET_UI_ON_RESUME, true);
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
        return Html.fromHtml(getString(R.string.voice_hotel_book_hint_primary) + "<br /><small><i>" + getString(
                R.string.voice_hotel_book_hint_secondary) + "</i></small>");

    } // getGreetingText()

    @Override
    protected void cancelMwsRequests() {
        getIntent().putExtra(RESET_UI_ON_RESUME, false);
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
        return null;
    }

    @Override
    public Intent getNoResultsIntent() {
        return null;
    }

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

            // Stop the TTS if it's saying something.
            if (tts != null && tts.isSpeaking()) {
                tts.stop();
            }
        }
    }
}
