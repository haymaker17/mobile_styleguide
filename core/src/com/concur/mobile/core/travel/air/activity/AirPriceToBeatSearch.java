package com.concur.mobile.core.travel.air.activity;

import java.util.Calendar;
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

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.activity.LocationSearch;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.air.fragment.AirPriceToBeatSearchFragment;
import com.concur.mobile.core.travel.air.service.GetAirBenchmarks;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.data.TravelPointsConfig;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

/**
 * Activity to launch the Air Price to Beat Search
 * 
 * @author RatanK
 * 
 */
public class AirPriceToBeatSearch extends TravelBaseActivity implements
        AirPriceToBeatSearchFragment.PriceToBeatSearchFragmentOnClickListener {

    private static final String CLS_TAG = AirPriceToBeatSearch.class.getSimpleName();

    private static final int DEPART_LOCATION_ACTIVITY_CODE = 0;
    private static final int ARRIVE_LOCATION_ACTIVITY_CODE = 1;
    private static final int RESULTS_ACTIVITY_CODE = 2;

    private static final String AIR_BENCH_MARK_RECEIVER_KEY = "air.bench.mark.receiver";
    private static final String SEARCH_FRAGMENT = "search.fragment";

    private static final String STATE_DEPART_LOC_KEY = "depart_loc";
    private static final String STATE_ARRIVE_LOC_KEY = "arrive_loc";
    private static final String STATE_DEPART_DT_KEY = "depart_datetime";
    private static final String STATE_ROUND_TRIP_KEY = "round_trip";
    private static final String STATE_TP_ENABLED_KEY = "tp_enabled";
    private static final String STATE_FAILED_DIALOG_SHOWING_KEY = "failed_dialog_showing";
    private static final String STATE_FAILED_MESSAGE_KEY = "failed_message";

    private GetAirBenchmarks getAirBenchmarks;
    private BaseAsyncResultReceiver benchmarkReciever;

    private AlertDialog searchFailedFrag;

    private AirPriceToBeatSearchFragment searchFragment;
    private boolean roundTrip;
    private LocationChoice departLocation;
    private LocationChoice arriveLocation;
    private Calendar departDate;
    private boolean travelPointsEnabled;

    private String failedMessage;
    private boolean showingFailedDialog;
    private boolean showingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.air_price_to_beat_main);

        // add the search fragment
        FragmentManager fm = getSupportFragmentManager();
        searchFragment = (AirPriceToBeatSearchFragment) fm.findFragmentByTag(SEARCH_FRAGMENT);
        if (searchFragment == null) {
            searchFragment = new AirPriceToBeatSearchFragment();
            searchFragment.setPriceToBeatSearchFragmentOnClickListener(this);
            TravelPointsConfig travelPointsConfig = ((ConcurCore) getApplication()).getUserConfig().travelPointsConfig;
            if (travelPointsConfig != null) {
                travelPointsEnabled = travelPointsConfig.isAirTravelPointsEnabled();
            }

            // send the header text
            String headerTxt = null;
            if (travelPointsEnabled) {
                headerTxt = (String) getText(R.string.air_price_to_beat_search_header_tp_enabled);
            } else {
                headerTxt = (String) getText(R.string.air_price_to_beat_search_header);
            }
            searchFragment.setHeaderTxt(headerTxt);

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.airPriceToBeatContainer, searchFragment, SEARCH_FRAGMENT);
            ft.commit();
        }

        if (savedInstanceState == null) {
            // Flurry Notification.
            EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT,
                    Flurry.EVENT_NAME_VIEWED_AIR_PRICE_TO_BEAT_SEARCH);
        } else {

            Bundle locBundle = savedInstanceState.getBundle(STATE_DEPART_LOC_KEY);
            if (locBundle != null) {
                departLocation = new LocationChoice(locBundle);
            }

            locBundle = savedInstanceState.getBundle(STATE_ARRIVE_LOC_KEY);
            if (locBundle != null) {
                arriveLocation = new LocationChoice(locBundle);
            }

            departDate = (Calendar) savedInstanceState.getSerializable(STATE_DEPART_DT_KEY);

            roundTrip = savedInstanceState.getBoolean(STATE_ROUND_TRIP_KEY);

            travelPointsEnabled = savedInstanceState.getBoolean(STATE_TP_ENABLED_KEY);

            if (savedInstanceState.containsKey(STATE_FAILED_DIALOG_SHOWING_KEY)) {
                // Log.d(Const.LOG_TAG, " ******************* onCreate invoking showSearchFailedDialog ****");
                showSearchFailedDialog(savedInstanceState.getString(STATE_FAILED_MESSAGE_KEY));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle locBundle;
            switch (requestCode) {
            case DEPART_LOCATION_ACTIVITY_CODE:
                locBundle = data.getBundleExtra(LocationChoice.LOCATION_BUNDLE);
                searchFragment.setDepartLocation(new LocationChoice(locBundle));
                searchFragment.updateLocationViews();
                break;
            case ARRIVE_LOCATION_ACTIVITY_CODE:
                locBundle = data.getBundleExtra(LocationChoice.LOCATION_BUNDLE);
                searchFragment.setArriveLocation(new LocationChoice(locBundle));
                searchFragment.updateLocationViews();
                break;
            case RESULTS_ACTIVITY_CODE:
                setResult(resultCode, data);
                finish();
                break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (departLocation != null) {
            outState.putBundle(STATE_DEPART_LOC_KEY, departLocation.getBundle());
        }
        if (arriveLocation != null) {
            outState.putBundle(STATE_ARRIVE_LOC_KEY, arriveLocation.getBundle());
        }

        outState.putSerializable(STATE_DEPART_DT_KEY, departDate);

        outState.putBoolean(STATE_ROUND_TRIP_KEY, roundTrip);

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
            retainer.put(AIR_BENCH_MARK_RECEIVER_KEY, benchmarkReciever);
        }

        // Log.d(Const.LOG_TAG, " ******************* onPause showingFailedDialog is " + showingFailedDialog + " ***** ");
        retainer.put(STATE_FAILED_DIALOG_SHOWING_KEY, showingFailedDialog);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // called from super restoreReceivers()
    protected void restoreReceivers() {
        if (retainer.contains(AIR_BENCH_MARK_RECEIVER_KEY)) {
            benchmarkReciever = (BaseAsyncResultReceiver) retainer.get(AIR_BENCH_MARK_RECEIVER_KEY);
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

    // launch the search results activity
    private void showPriceToBeatSearchResults(Bundle resultData) {

        Intent i = new Intent(this, AirPriceToBeatSearchResults.class);

        i.putExtras(resultData);

        i.putExtra(Const.EXTRA_SEARCH_LOC_FROM, departLocation.getBundle());
        i.putExtra(Const.EXTRA_SEARCH_LOC_TO, arriveLocation.getBundle());
        i.putExtra(Const.EXTRA_SEARCH_DT_DEPART, departDate);

        // send the header text
        String headerTxt = (String) getText(R.string.air_price_to_beat_search_results_header);
        if (travelPointsEnabled) {
            headerTxt = (String) getText(R.string.air_price_to_beat_search_results_header_tp_enabled);
        }
        i.putExtra(Const.EXTRA_PRICE_TO_BEAT_HEADER_TXT, headerTxt);

        // send the search mode text
        String searchModeTxt = (String) getText(R.string.home_action_air_price_to_beat);
        if (roundTrip) {
            searchModeTxt = (String) getText(R.string.air_price_to_beat_round_trip);
        }
        i.putExtra(Const.EXTRA_AIR_PRICE_TO_BEAT_ROUND_TRIP, searchModeTxt);

        showingFailedDialog = false;
        showingProgressDialog = false;

        // start the results activity
        startActivity(i);
    }

    // Call the Async Request to get Price to Beat
    public void getPriceToBeat() {

        benchmarkReciever = new BaseAsyncResultReceiver(new Handler());
        benchmarkReciever.setListener(new BenchmarkListener());

        getAirBenchmarks = new GetAirBenchmarks(AirPriceToBeatSearch.this, this, 1, benchmarkReciever,
                departLocation.getIATACode(), arriveLocation.getIATACode(), departDate, roundTrip);

        getAirBenchmarks.execute();
    }

    public void showSearchFailedDialog(String message) {
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

        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);

        dlgBldr.setTitle(R.string.general_search_for_price_to_beat_failed_title);
        dlgBldr.setMessage(message);
        dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                showingFailedDialog = false;
                dialog.dismiss();
            }
        });
        dlgBldr.setCancelable(true);
        dlgBldr.setOnCancelListener(new OnCancelListener() {

            public void onCancel(DialogInterface dialog) {
                showingFailedDialog = false;
                dialog.dismiss();
            }
        });

        searchFailedFrag = dlgBldr.create();

        searchFailedFrag.show();
    }

    // Listener for the GetPriceToBeat Async Request
    private class BenchmarkListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestSuccess. in BenchmarkListener");
            if (resultData.containsKey(GetAirBenchmarks.AIR_BENCHMARKS)) {
                showPriceToBeatSearchResults(resultData);
            } else {
                // Flurry Notification.
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_TRAVEL_DEPARTURE_LOC, departLocation.getName());
                params.put(Flurry.PARAM_NAME_TRAVEL_ARRIVAL_LOC, arriveLocation.getName());
                String dateStr = Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY,
                        departDate);
                params.put(Flurry.PARAM_NAME_TRAVEL_DEPARTURE_DATE, dateStr);

                EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT,
                        Flurry.EVENT_NAME_VIEWED_AIR_PRICE_TO_BEAT_RESULTS_NOT_FOUND, params);

                String message = resultData.getString(Const.MWS_ERROR_MESSAGE);
                // Log.d(Const.LOG_TAG, " ******************* onRequestSuccess invoking showingFailedDialog ***** ");
                showSearchFailedDialog(message);
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestFail in BenchmarkListener");
            // handle general error like response parse error
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

    // start of search fragment call backs
    @Override
    public void onDepartLocClicked() {
        if (ConcurCore.isConnected()) {
            Intent i = new Intent(this, LocationSearch.class);
            i.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, LocationSearch.SEARCH_AIRPORTS);
            startActivityForResult(i, DEPART_LOCATION_ACTIVITY_CODE);
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    @Override
    public void onArriveLocClicked() {
        if (ConcurCore.isConnected()) {
            Intent i = new Intent(this, LocationSearch.class);
            i.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, LocationSearch.SEARCH_AIRPORTS);
            startActivityForResult(i, ARRIVE_LOCATION_ACTIVITY_CODE);
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    @Override
    public void onSearchClicked(LocationChoice departLocation, LocationChoice arriveLocation, Calendar departDate,
            boolean roundTrip) {
        if (ConcurCore.isConnected()) {
            this.departLocation = departLocation;
            this.arriveLocation = arriveLocation;
            this.departDate = departDate;
            this.roundTrip = roundTrip;
            getPriceToBeat();
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    // end of search fragment call backs
}
