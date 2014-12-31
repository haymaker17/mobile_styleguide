/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.core.travel.car.activity;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.eva.activity.VoiceSearchActivity;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.car.data.CarType;
import com.concur.mobile.core.travel.car.service.CarSearchReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.eva.service.EvaApiRequest.BookingSelection;
import com.concur.mobile.eva.service.EvaCarReply;

/**
 * Implementation of the Voice Searching for Car.
 * 
 * @author Chris N. Diaz
 * 
 */
public class VoiceCarSearchActivity extends VoiceSearchActivity {

    public final static String CLS_TAG = VoiceCarSearchActivity.class.getSimpleName();

    private final static String RESET_UI_ON_RESUME = "RESET_UI_ON_RESUME";

    // Contains a reference to the current outstanding car search request.
    private ServiceRequest carSearchRequest;

    // Contains the filter used to register the above receiver.
    protected final IntentFilter carResultsFilter = new IntentFilter(Const.ACTION_CAR_SEARCH_RESULTS);

    // Contains a reference to a receiver for handling a search response.
    private BroadcastReceiver carSearchReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent i) {
            getApplicationContext().unregisterReceiver(this);

            String status = i.getStringExtra(Const.REPLY_STATUS);

            if (status != null && status.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                final ConcurCore app = (ConcurCore) getApplication();
                final CarSearchReply reply = app.getCarSearchResults();
                int resultCount = reply.carChoices.size();
                if (resultCount > 0) {
                    startActivityForResult(resultsIntent, Const.REQUEST_CODE_BOOK_CAR);
                } else {
                    startActivity(noResultsIntent);
                }
            } else if (carSearchRequest != null && carSearchRequest.canceled) {
                Log.i(Const.LOG_TAG, CLS_TAG + ": - Car search was canceled.");
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ": - CarSearchReceiver returned null status!");
                startActivity(noResultsIntent);
            }

            onReceiveComplete();
        }
    };

    /**
     * Performs an Car search based on the Eva API rely.
     * 
     * @param carReply
     *            the Eva API reply for Car search.
     */
    @Override
    public void doCarSearch(EvaCarReply carReply) {

        Log.d(Const.LOG_TAG, CLS_TAG + ".doCarSearch");

        // Setup Car intents.
        if (resultsIntent == null) {
            resultsIntent = (Intent) getIntent().clone();
            resultsIntent.setClass(this, CarSearchResults.class);
        }

        if (noResultsIntent == null) {
            noResultsIntent = (Intent) getIntent().clone();
            noResultsIntent.setClass(this, CarSearchNoResults.class);
        }

        // Extra info for the search results Intent.
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, true);
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_LOCATION, carReply.locationName);
        resultsIntent.putExtra(CarSearch.PICKUP_DATETIME, carReply.pickupDateTime);
        resultsIntent.putExtra(CarSearch.DROPOFF_DATETIME, carReply.dropoffDateTime);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            resultsIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM,
                    launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }

        // Extra info for the Intent when there are no results.
        noResultsIntent.putExtra(Const.EXTRA_TRAVEL_LOCATION, carReply.locationName);
        noResultsIntent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP_CALENDAR, carReply.pickupDateTime);
        noResultsIntent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF_CALENDAR, carReply.dropoffDateTime);

        if (tts != null) {

            String shortLoc = getShortLocationName(carReply.locationName);

            // Text to Speech...
            String shortPickupDate = voiceDateFormat.format(carReply.pickupDateTime.getTime());
            String shortReturnDate = voiceDateFormat.format(carReply.dropoffDateTime.getTime());

            String s = Format.localizeText(this, R.string.car_voice_search_criteria, shortLoc, shortPickupDate,
                    shortReturnDate);

            // Set the chat text.
            showResponseText(s);

            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".doCarSearch: TTS is null!");

            logErrorFlurryEvent(Flurry.PARAM_VALUE_SPEECH_RECOGNIZER);

            showErrorMessage();
            cancelSearch(false);

            return;
        }

        // Get the available car types and see if what the user searched for is a part of it.
        // If not, then use the user's default car type.
        String carType = carReply.carType;
        String carTypeCode = null;
        UserConfig uc = ((ConcurCore) getApplication()).getUserConfig();
        if (uc != null) {
            ArrayList<CarType> allowedCars = uc.allowedCarTypes;
            for (CarType ct : allowedCars) {

                if (carType != null && ct.description.equalsIgnoreCase(carType)) {
                    carTypeCode = ct.code;
                    break;
                }
            }
        }

        // Default to any.
        if (carTypeCode == null) {
            carTypeCode = "";
        }

        // Clear out any current results.
        getConcurCore().setCarSearchResults(null);

        // Make the call
        ConcurService svc = getConcurService();
        if (svc != null) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".doCarSearch: invoking Car Search MWS...");

            getApplicationContext().registerReceiver(carSearchReceiver, carResultsFilter);
            carSearchRequest = svc.searchForCars(carReply.pickupLat, carReply.pickupLong, carReply.pickupDateTime,
                    carReply.dropoffLat, carReply.dropoffLong, carReply.dropoffDateTime, carTypeCode);
        }

    } // doCarSearch()

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.car.activity.VoiceSearchActivity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case Const.REQUEST_CODE_BOOK_CAR:
            if (resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            }
            break;
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
     * @see com.concur.mobile.core.travel.car.activity.VoiceSearchActivity#getGreetingText()
     */
    @Override
    protected Spanned getGreetingText() {
        return Html.fromHtml(getString(R.string.voice_car_book_hint_primary) + "<br /><small><i>"
                + getString(R.string.voice_car_book_hint_secondary) + "</i></small>");

    } // getGreetingText()

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.car.activity.VoiceSearchActivity#cancelMwsRequests()
     */
    @Override
    protected void cancelMwsRequests() {

        getIntent().putExtra(RESET_UI_ON_RESUME, false);

        // Cancel the MWS request.
        if (carSearchRequest != null) {
            carSearchRequest.cancel();
        }
    } // cancelMwsRequests()

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.car.activity.AbstractTravelSearchProgress#getSearchRequest()
     */
    @Override
    public ServiceRequest getSearchRequest() {
        return carSearchRequest;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.core.travel.car.activity.AbstractTravelSearchProgress#setSearchRequest(com.concur.mobile.core.service
     * .ServiceRequest)
     */
    @Override
    public void setSearchRequest(ServiceRequest searchRequest) {
        this.carSearchRequest = searchRequest;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.activity.AbstractTravelSearchProgress#onReceiveComplete()
     */
    public void onReceiveComplete() {

        // Flurry event indicating everything worked.
        logSuccessUsageFlurryEvent();

        if (carSearchRequest != null && carSearchRequest.isCanceled()) {
            getIntent().putExtra(RESET_UI_ON_RESUME, false);
        } else {
            getIntent().putExtra(RESET_UI_ON_RESUME, true);
        }

        this.carSearchRequest = null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.car.activity.AbstractTravelSearchProgress#finishActivityOnNoResults()
     */
    @Override
    public boolean finishActivityOnNoResults() {
        return false;
    }

    @Override
    protected BookingSelection getBookType() {
        return BookingSelection.CAR;
    }

} // end VoiceCarSearchActivity

