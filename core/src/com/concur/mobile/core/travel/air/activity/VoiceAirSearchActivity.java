/**
 * Copyright (c) 2012 Concur Technologies, Inc.
 */
package com.concur.mobile.core.travel.air.activity;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.eva.activity.VoiceSearchActivity;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.air.service.AirSearchReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.eva.service.EvaAirReply;
import com.concur.mobile.eva.service.EvaApiRequest.BookingSelection;

/**
 * Implementation of the Voice Searching for Air.
 * 
 * @author Chris N. Diaz
 * 
 */
public class VoiceAirSearchActivity extends VoiceSearchActivity {

    public final static String CLS_TAG = VoiceAirSearchActivity.class.getSimpleName();

    private final static String RESET_UI_ON_RESUME = "RESET_UI_ON_RESUME";

    // Contains a reference to the current outstanding air search request.
    private ServiceRequest airSearchRequest;

    // Contains the filter used to register the above receiver.
    protected final IntentFilter airResultsFilter = new IntentFilter(Const.ACTION_AIR_SEARCH_RESULTS);

    // Contains a reference to a receiver for handling a search response.
    private BroadcastReceiver airSearchReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent i) {
            getApplicationContext().unregisterReceiver(this);

            String status = i.getStringExtra(Const.REPLY_STATUS);

            if (status != null && status.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                final ConcurCore app = (ConcurCore) getApplication();
                final AirSearchReply reply = app.getAirSearchResults();
                int resultCount = reply.getResultCount();
                if (resultCount > 0) {
                    startActivityForResult(resultsIntent, AirSearch.RESULTS_ACTIVITY_CODE);
                } else {
                    startActivity(noResultsIntent);
                }
            } else if (airSearchRequest != null && airSearchRequest.canceled) {
                Log.i(Const.LOG_TAG, CLS_TAG + ": - Air search was canceled.");
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ": - AirSearchReceiver returned null status!");
                startActivity(noResultsIntent);
            }

            onReceiveComplete();
        }
    };

    /**
     * Performs an Air search based on the Eva API rely.
     * 
     * @param airSearch
     *            the Eva API reply for Air search.
     */
    @Override
    public void doAirSearch(EvaAirReply airReply) {

        // MOB-13811 - Check user's travel profile and Flex Fare.
        if (showTravelProfileInCompleteWarningDialog(airReply.departCountry, airReply.arriveCountry)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int travelProfile = prefs.getInt(Const.PREF_TRAVEL_PROFILE_STATUS,
                    Const.TRAVEL_PROFILE_ALL_REQUIRED_DATA_PLUS_TSA);
            String msg = getString(ViewUtil.getTextResourceIdForProfileCheck(travelProfile));
            showResponseText(msg);
            resetUI(false);

            Log.d(Const.LOG_TAG, CLS_TAG + ".doAirSearch: user travel profile incomplete.");
            return;
        }
        if (showFlexFareWarningDialog(airReply.departCountry, airReply.arriveCountry)) {
            showResponseText(getString(R.string.dlg_no_air_flex_message));
            resetUI(false);

            Log.d(Const.LOG_TAG, CLS_TAG + ".doAirSearch: Flex Fare user should not book on mobile.");
            return;
        }

        Log.d(Const.LOG_TAG, CLS_TAG + ".doAirSearch: invoking Air Search MWS...");

        Calendar departDateCal = airReply.departDate;
        Calendar returnDateCal = airReply.returnDate;

        // Setup Air intents.
        if (resultsIntent == null) {
            resultsIntent = (Intent) getIntent().clone();
            resultsIntent.setClass(this, AirSearchResultsSummary.class);
        }

        if (noResultsIntent == null) {
            noResultsIntent = (Intent) getIntent().clone();
            noResultsIntent.setClass(this, AirSearchNoResults.class);
        }

        // Map the search mode name from the 'EvaAirReply.SearchMode' enum value
        // onto the corresponding one 'AirSearch.SearchMode'.
        String searchModeNameExtra = null;
        switch (airReply.searchMode) {
        case MultiSegment: {
            searchModeNameExtra = AirSearch.SearchMode.MultiSegment.name();
            break;
        }
        case None: {
            searchModeNameExtra = AirSearch.SearchMode.None.name();
            break;
        }
        case OneWay: {
            searchModeNameExtra = AirSearch.SearchMode.OneWay.name();
            break;
        }
        case RoundTrip: {
            searchModeNameExtra = AirSearch.SearchMode.RoundTrip.name();
            break;
        }
        }

        // Extra info for the search results Intent.
        resultsIntent.putExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, true);
        resultsIntent.putExtra(Const.EXTRA_SEARCH_MODE, searchModeNameExtra);
        resultsIntent.putExtra(Const.EXTRA_SEARCH_LOC_FROM, airReply.departLocationBundle);
        resultsIntent.putExtra(Const.EXTRA_SEARCH_LOC_TO, airReply.arriveLocationBundle);
        resultsIntent.putExtra(Const.EXTRA_SEARCH_DT_DEPART, departDateCal);
        resultsIntent.putExtra(Const.EXTRA_SEARCH_CABIN_CLASS, airReply.cabinClass);
        resultsIntent.putExtra(Const.EXTRA_SEARCH_REFUNDABLE_ONLY, airReply.refundableOnly);

        if (airReply.searchMode != EvaAirReply.SearchMode.OneWay) {
            resultsIntent.putExtra(Const.EXTRA_SEARCH_DT_RETURN, returnDateCal);
            noResultsIntent.putExtra(Const.EXTRA_SEARCH_DT_RETURN, returnDateCal);
        }
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            resultsIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM,
                    launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }

        // Extra info for the Intent when there are no results.
        noResultsIntent.putExtra(Const.EXTRA_SEARCH_MODE, searchModeNameExtra);
        noResultsIntent.putExtra(Const.EXTRA_SEARCH_LOC_FROM, airReply.departLocationBundle);
        noResultsIntent.putExtra(Const.EXTRA_SEARCH_LOC_TO, airReply.arriveLocationBundle);
        noResultsIntent.putExtra(Const.EXTRA_SEARCH_DT_DEPART, departDateCal);

        if (tts != null) {

            // Try to get a shorter name to say...
            String departLocShort = getShortLocationName(airReply.departLocation);
            String arriveLocShort = getShortLocationName(airReply.arriveLocation);

            // Text to Speech...
            String s = null;
            String shortDepartDate = voiceDateFormat.format(departDateCal.getTime());
            // Different text for one-way and round-trip.
            if (airReply.searchMode != EvaAirReply.SearchMode.OneWay) {
                String shortReturnDate = voiceDateFormat.format(returnDateCal.getTime());
                s = Format.localizeText(this, R.string.air_voice_search_criteria_roundtrip, departLocShort,
                        arriveLocShort, shortDepartDate, shortReturnDate);
            } else {
                s = Format.localizeText(this, R.string.air_voice_search_criteria_oneway, departLocShort,
                        arriveLocShort, shortDepartDate);
            }

            // Set the chat text.
            showResponseText(s);

            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".doAirSearch: TTS is null!");

            logErrorFlurryEvent(Flurry.PARAM_VALUE_SPEECH_RECOGNIZER);

            showErrorMessage();
            cancelSearch(false);

            return;
        }

        // Clear out any current results.
        getConcurCore().setAirSearchResults(null);

        // Make the call
        ConcurService svc = getConcurService();
        if (svc != null) {

            getApplicationContext().registerReceiver(airSearchReceiver, airResultsFilter);
            airSearchRequest = svc.searchForFlights(airReply.departLocIATA, airReply.arriveLocIATA, departDateCal,
                    returnDateCal, airReply.cabinClass, airReply.refundableOnly);
        }

    } // doAirSearch()

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.VoiceSearchActivity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case AirSearch.RESULTS_ACTIVITY_CODE: {
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
        return Html.fromHtml(getString(R.string.voice_air_book_hint_primary) + "<br /><small><i>"
                + getString(R.string.voice_air_book_hint_secondary) + "</i></small>");

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
        if (airSearchRequest != null) {
            airSearchRequest.cancel();
        }
    } // cancelMwsRequests()

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.AbstractTravelSearchProgress#getSearchRequest()
     */
    @Override
    public ServiceRequest getSearchRequest() {
        return airSearchRequest;
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
        this.airSearchRequest = searchRequest;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.AbstractTravelSearchProgress#resetAirSearchRequest()
     */
    @Override
    public void onReceiveComplete() {

        // Flurry event indicating everything worked.
        logSuccessUsageFlurryEvent();

        if (airSearchRequest != null && airSearchRequest.isCanceled()) {
            getIntent().putExtra(RESET_UI_ON_RESUME, false);
        } else {
            getIntent().putExtra(RESET_UI_ON_RESUME, true);
        }

        this.airSearchRequest = null;

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
        return BookingSelection.AIR;
    }

    /**
     * Will determine whether or not an end-user that is searching for flights where the to/from location is within the US has
     * missing TSA information.
     * 
     * @param departCountry
     * @param arriveCountry
     * 
     * @return returns whether or not an end-user that is searching for flights where the to/from location is within the US has
     *         missing TSA information.
     */
    protected boolean showTravelProfileInCompleteWarningDialog(String departCountry, String arriveCountry) {
        boolean retVal = false;
        if ((departCountry != null && departCountry.equalsIgnoreCase("US"))
                || (arriveCountry != null && arriveCountry.equalsIgnoreCase("US"))) {
            retVal = ViewUtil.isTravelProfileCompleteMissingTSA(this);
        }
        return retVal;
    }

    /**
     * Will determine whether the current user is a Flex Fare user and if so whether the current depart/arrive locations require
     * the end-user to not book via mobile.
     * 
     * @return returns <code>true</code> if the flex fare warning dialog should be displayed; <code>false</code> otherwise.
     */
    protected boolean showFlexFareWarningDialog(String departCountry, String arriveCountry) {
        boolean retVal = false;
        if (ViewUtil.isFlexFareUser(this)) {
            if (departCountry != null && arriveCountry != null) {
                // First check for intra-country travel.
                for (int ffInd = 0; ffInd < AirSearch.FLEX_FARING_INTRA_COUNTRY_TRAVEL.length; ++ffInd) {
                    if (departCountry.equalsIgnoreCase(AirSearch.FLEX_FARING_INTRA_COUNTRY_TRAVEL[ffInd])
                            && arriveCountry.equalsIgnoreCase(AirSearch.FLEX_FARING_INTRA_COUNTRY_TRAVEL[ffInd])) {
                        retVal = true;
                        break;
                    }
                }
                if (!retVal) {
                    // Second check for inter-country travel between specific countries.
                    if ((departCountry.equalsIgnoreCase("AU") && arriveCountry.equalsIgnoreCase("NZ"))
                            || (departCountry.equalsIgnoreCase("NZ") && arriveCountry.equalsIgnoreCase("AU"))) {
                        retVal = true;
                    }
                }
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG
                        + ".showFlexFareWarningDialog: depart/arrive country abbreviation missing!");
            }
        }
        return retVal;
    }
} // end VoiceCarSearchActivity

