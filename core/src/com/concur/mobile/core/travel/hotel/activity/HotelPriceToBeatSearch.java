package com.concur.mobile.core.travel.hotel.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.SystemConfig;
import com.concur.mobile.core.travel.activity.LocationSearch;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.data.CompanyLocation;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.data.TravelPointsConfig;
import com.concur.mobile.core.travel.hotel.fragment.HotelPriceToBeatSearchFragment;
import com.concur.mobile.core.travel.hotel.service.GetHotelBenchmarks;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;

/**
 * Activity to launch the Hotel Price to Beat Search
 * 
 * @author RatanK
 * 
 */
public class HotelPriceToBeatSearch extends TravelBaseActivity implements
        HotelPriceToBeatSearchFragment.PriceToBeatSearchFragmentOnClickListener {

    private static final String CLS_TAG = HotelPriceToBeatSearch.class.getSimpleName();

    private static final String HOTEL_BENCH_MARK_RECEIVER_KEY = "hotel.bench.mark.receiver";
    private static final String SEARCH_FRAGMENT = "search.fragment";
    private static final String STATE_TP_ENABLED_KEY = "tp_enabled";
    private static final String STATE_FAILED_DIALOG_SHOWING_KEY = "failed_dialog_showing";
    private static final String STATE_FAILED_MESSAGE_KEY = "failed_message";

    private static final String STATE_LOCATION = "location";
    private static final String STATE_RADIUS = "radius";
    private static final String STATE_SCALE = "scale";
    private static final String STATE_MONTH_NUMBER = "month.number";

    protected static final int MONTH_OF_STAY_DIALOG = DIALOG_ID_BASE + 0;
    protected static final int DISTANCE_DIALOG = DIALOG_ID_BASE + 1;
    protected static final int DISTANCE_UNIT_DIALOG = DIALOG_ID_BASE + 2;

    private GetHotelBenchmarks getHotelBenchmarks;
    private BaseAsyncResultReceiver benchmarkReciever;

    private AlertDialog searchFailedFrag;
    private HotelPriceToBeatSearchFragment searchFragment;

    private LocationChoice location;
    private String radius;
    private String scale;
    private String monthNumber;
    private boolean travelPointsEnabled;

    private String failedMessage;
    private boolean showingFailedDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hotel_price_to_beat_main);

        // add the search fragment
        FragmentManager fm = getSupportFragmentManager();
        searchFragment = (HotelPriceToBeatSearchFragment) fm.findFragmentByTag(SEARCH_FRAGMENT);
        if (searchFragment == null) {
            searchFragment = new HotelPriceToBeatSearchFragment();
            searchFragment.setPriceToBeatSearchFragmentOnClickListener(this);
            TravelPointsConfig travelPointsConfig = ((ConcurCore) getApplication()).getUserConfig().travelPointsConfig;
            if (travelPointsConfig != null) {
                travelPointsEnabled = travelPointsConfig.isHotelTravelPointsEnabled();
            }

            // send the header text
            String headerTxt = null;
            if (travelPointsEnabled) {
                headerTxt = (String) getText(R.string.hotel_price_to_beat_search_header_tp_enabled);
            } else {
                headerTxt = (String) getText(R.string.hotel_price_to_beat_search_header);
            }
            searchFragment.setHeaderTxt(headerTxt);

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.hotelPriceToBeatContainer, searchFragment, SEARCH_FRAGMENT);
            ft.commit();
        }

        if (savedInstanceState == null) {
            // Flurry Notification.
            EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT,
                    Flurry.EVENT_NAME_VIEWED_HOTEL_PRICE_TO_BEAT_SEARCH);
        } else {

            Bundle locBundle = savedInstanceState.getBundle(STATE_LOCATION);
            if (locBundle != null) {
                location = new LocationChoice(locBundle);
            }

            if (savedInstanceState.containsKey(STATE_RADIUS)) {
                radius = savedInstanceState.getString(STATE_RADIUS);
            }

            if (savedInstanceState.containsKey(STATE_SCALE)) {
                scale = savedInstanceState.getString(STATE_SCALE);
            }

            if (savedInstanceState.containsKey(STATE_MONTH_NUMBER)) {
                monthNumber = savedInstanceState.getString(STATE_MONTH_NUMBER);
            }

            travelPointsEnabled = savedInstanceState.getBoolean(STATE_TP_ENABLED_KEY);

            if (savedInstanceState.containsKey(STATE_FAILED_DIALOG_SHOWING_KEY)) {
                // Log.d(Const.LOG_TAG, " ******************* onCreate invoking showSearchFailedDialog ****");
                showSearchFailedDialog(savedInstanceState.getString(STATE_FAILED_MESSAGE_KEY));
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case Const.REQUEST_CODE_LOCATION: {
            if (resultCode == RESULT_OK) {
                Bundle locBundle = data.getBundleExtra(LocationChoice.LOCATION_BUNDLE);
                // obtain the search mode that provided the choice.
                int searchMode = data.getIntExtra(Const.EXTRA_LOCATION_SEARCH_MODE_USED, -1);
                if (searchMode != -1) {
                    switch (searchMode) {
                    case LocationSearch.SEARCH_COMPANY_LOCATIONS: {
                        searchFragment.setLocation(new CompanyLocation(locBundle));
                        break;
                    }
                    case LocationSearch.SEARCH_CUSTOM: {
                        searchFragment.setLocation(new LocationChoice(locBundle));
                        break;
                    }
                    default: {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult: invalid search mode of '" + searchMode
                                + "' on result intent.");
                    }
                    }
                    searchFragment.updateLocationButton();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult: no search mode used flag set on result intent.");
                }
            }
            break;
        }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (location != null) {
            outState.putBundle(STATE_LOCATION, location.getBundle());
        }

        if (radius != null) {
            outState.putString(STATE_RADIUS, radius);
        }
        if (scale != null) {
            outState.putString(STATE_SCALE, scale);
        }
        if (monthNumber != null) {
            outState.putString(STATE_MONTH_NUMBER, monthNumber);
        }

        outState.putBoolean(STATE_TP_ENABLED_KEY, travelPointsEnabled);

        if (showingFailedDialog) {
            // Log.d(Const.LOG_TAG, " ******************* onSaveInstanceState showingFailedDialog is TRUE ***** ");
            outState.putBoolean(STATE_FAILED_DIALOG_SHOWING_KEY, true);
            outState.putString(STATE_FAILED_MESSAGE_KEY, failedMessage);
        }

        // Notify the Application that this activity is going to be destroyed so detach this activity reference from all Async
        // Tasks that are started by this activity
        ConcurCore concurCoreApp = (ConcurCore) getApplication();
        concurCoreApp.detach(this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Notify the Application that this activity is recreated so attach this new activity reference to all Async Tasks that
        // are running (started) by this activity
        ConcurCore concurCoreApp = (ConcurCore) getApplication();
        concurCoreApp.attach(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (benchmarkReciever != null) {
            benchmarkReciever.setListener(null);
            retainer.put(HOTEL_BENCH_MARK_RECEIVER_KEY, benchmarkReciever);
        }

        // Log.d(Const.LOG_TAG, " ******************* onPause showingFailedDialog is ***** " + showingFailedDialog);
        retainer.put(STATE_FAILED_DIALOG_SHOWING_KEY, showingFailedDialog);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void restoreReceivers() {
        if (retainer.contains(HOTEL_BENCH_MARK_RECEIVER_KEY)) {
            benchmarkReciever = (BaseAsyncResultReceiver) retainer.get(HOTEL_BENCH_MARK_RECEIVER_KEY);
            benchmarkReciever.setListener(new BenchmarkListener());
        }

        if (retainer.contains(STATE_FAILED_DIALOG_SHOWING_KEY)) {
            showingFailedDialog = (Boolean) retainer.get(STATE_FAILED_DIALOG_SHOWING_KEY);
            // Log.d(Const.LOG_TAG, " ******************* restoreReceivers showingFailedDialog is " + showingFailedDialog +
            // " ***** ");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (searchFailedFrag != null) {
            // Log.d(Const.LOG_TAG, " ******************* onDestroy destroying searchFailedFrag ***** ");
            searchFailedFrag.dismiss();
            searchFailedFrag = null;
        }
    }

    // Call Async Request to get Price to Beat
    public void getPriceToBeat() {

        benchmarkReciever = new BaseAsyncResultReceiver(new Handler());
        benchmarkReciever.setListener(new BenchmarkListener());

        getHotelBenchmarks = new GetHotelBenchmarks(HotelPriceToBeatSearch.this, this, 1, benchmarkReciever,
                location.latitude, location.longitude, radius, scale, monthNumber);

        getHotelBenchmarks.execute();
    }

    private void showSearchFailedDialog(String message) {
        showingFailedDialog = true;

        if (searchFailedFrag != null) {
            // Log.d(Const.LOG_TAG, " ******************* showSearchFailedDialog dismissing searchFailedFrag ***** ");
            searchFailedFrag.dismiss();
            searchFailedFrag = null;
        }

        // if no message available then show general message
        if (message == null || message.trim().length() == 0) {
            message = (String) getText(R.string.general_search_for_price_to_beat_failed);
        }
        failedMessage = message;

        AlertDialog.Builder builder = new AlertDialog.Builder(HotelPriceToBeatSearch.this);
        builder.setTitle(R.string.general_search_for_price_to_beat_failed_title);
        builder.setMessage(message);
        builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                showingFailedDialog = false;
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                showingFailedDialog = false;
                dialog.dismiss();
            }
        });

        searchFailedFrag = builder.create();

        searchFailedFrag.show();
    }

    // launch the search results activity
    private void showPriceToBeatSearchResults(Bundle resultData) {

        Intent i = new Intent(this, HotelPriceToBeatSearchResults.class);

        i.putExtras(resultData);

        i.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, location.getName());

        int monthNumberInt = Integer.parseInt(monthNumber);
        i.putExtra(Const.EXTRA_HOTEL_PRICE_TO_BEAT_MONTH_OF_STAY,
                HotelPriceToBeatSearchFragment.monthItems[monthNumberInt - 1].name);

        // distance with units localized
        String distanceWithUnits = null;
        int distResourceId = R.string.hotel_price_to_beat_distance_mile_plural;
        boolean singularStrings = (Integer.parseInt(radius) == 1);
        if (singularStrings) {
            if (scale.equals("M")) {
                distResourceId = R.string.hotel_price_to_beat_distance_mile_singular;
            } else {
                distResourceId = R.string.hotel_price_to_beat_distance_km_singular;
            }
        } else {
            if (scale.equals("M")) {
                distResourceId = R.string.hotel_price_to_beat_distance_mile_plural;
            } else {
                distResourceId = R.string.hotel_price_to_beat_distance_km_plural;
            }
        }

        distanceWithUnits = Format.localizeText(this, distResourceId, radius);

        i.putExtra(Const.EXTRA_HOTEL_PRICE_TO_BEAT_DIST_WITH_UNITS, distanceWithUnits);

        // send the header text
        String headerTxt = (String) getText(R.string.hotel_price_to_beat_search_results_header);
        if (travelPointsEnabled) {
            headerTxt = (String) getText(R.string.hotel_price_to_beat_search_results_header_tp_enabled);
        }
        i.putExtra(Const.EXTRA_PRICE_TO_BEAT_HEADER_TXT, headerTxt);

        // showingFailedDialog = false;

        startActivity(i);
    }

    // Listener for the GetPriceToBeat Async Request
    private class BenchmarkListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            Log.d(Const.LOG_TAG, " onRequestSuccess. in BenchmarkListener");
            if (resultData.containsKey(GetHotelBenchmarks.HOTEL_BENCHMARKS)) {
                showPriceToBeatSearchResults(resultData);
            } else {
                String message = resultData.getString(Const.MWS_ERROR_MESSAGE);
                showSearchFailedDialog(message);
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            Log.d(Const.LOG_TAG, " onRequestFail in BenchmarkListener");
            // handle general error like response parse error
            // Flurry Notification.
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_LOCATION, location.getName());
            params.put(Flurry.PARAM_NAME_RADIUS, radius);
            params.put(Flurry.PARAM_NAME_SCALE, scale);
            int monthNumberInt = Integer.parseInt(monthNumber);
            params.put(Flurry.PARAM_NAME_MONTH, HotelPriceToBeatSearchFragment.monthItems[monthNumberInt - 1].name);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT,
                    Flurry.EVENT_NAME_VIEWED_HOTEL_PRICE_TO_BEAT_RESULTS_NOT_FOUND, params);

            showSearchFailedDialog(null);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestCancel in BenchmarkListener");
        }

        @Override
        public void cleanup() {
            benchmarkReciever.setListener(null);
            benchmarkReciever = null;
        }
    }

    @Override
    public void onLocClicked() {
        if (!ConcurCore.isConnected()) {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        } else {
            // Determine if there are any company locations, if so, then
            // pass that flag into
            // the location search activity.
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            SystemConfig sysConfig = ConcurCore.getSystemConfig();
            int locationSearchMode = LocationSearch.SEARCH_CUSTOM;
            if (sysConfig != null && sysConfig.getCompanyLocations() != null
                    && sysConfig.getCompanyLocations().size() > 0) {
                locationSearchMode |= LocationSearch.SEARCH_COMPANY_LOCATIONS;
            }

            Intent intent = new Intent(this, LocationSearch.class);
            intent.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, locationSearchMode);
            startActivityForResult(intent, Const.REQUEST_CODE_LOCATION);
        }
    }

    @Override
    public void onSearchClicked(LocationChoice location, String distance, String distanceUnit, String monthNumber) {
        if (ConcurCore.isConnected()) {
            this.location = location;
            this.radius = distance;
            this.scale = distanceUnit;
            this.monthNumber = monthNumber;

            getPriceToBeat();
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }
}
