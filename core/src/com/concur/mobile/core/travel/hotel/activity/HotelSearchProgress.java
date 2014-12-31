/**
 * 
 */
package com.concur.mobile.core.travel.hotel.activity;

import java.util.Calendar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.activity.AbstractTravelSearchProgress;
import com.concur.mobile.core.travel.hotel.receiver.HotelSearchReceiver;
import com.concur.mobile.core.travel.hotel.service.GetHotels;
import com.concur.mobile.core.travel.hotel.service.HotelSearchReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>BaseActivity</code> for displaying a hotel search progress screen.
 */
public class HotelSearchProgress extends AbstractTravelSearchProgress {

    static final String CLS_TAG = HotelSearchProgress.class.getSimpleName();

    private static final String HOTEL_SEARCH_RECEIVER_KEY = "hotel.search.receiver";

    protected Intent resultsIntent;
    protected Intent noResultsIntent;

    protected String location;

    protected String checkInDate;
    protected Calendar checkInDateCal;
    protected String checkOutDate;
    protected Calendar checkOutDateCal;

    protected String longitude;
    protected String latitude;
    protected String distanceId;
    protected String distanceUnitId;
    protected String namesContaining;

    // Contains a reference to the current outstanding hotel search request.
    protected ServiceRequest hotelSearchRequest;

    // Contains a reference to a receiver for handling a search response.
    protected HotelSearchReceiver hotelSearchReceiver;

    // Contains the filter used to register the above receiver.
    protected final IntentFilter hotelResultsFilter = new IntentFilter(Const.ACTION_HOTEL_SEARCH_RESULTS);

    // start of hotel streaming
    private BaseAsyncResultReceiver hotelResultsReceiver;
    private GetHotels getHotelsAsycTask;
    // end of hotel streaming

    private AlertDialogFragment searchFailedDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hotel_search_progress);

        initValues();
        initUI();

        // Restore any receivers.
        restoreReceivers();

        startSearch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (hotelSearchReceiver != null) {
            // Clear the activity reference, it will be set in the new HotelSearch instance.
            hotelSearchReceiver.setActivity(null);
            retainer.put(HOTEL_SEARCH_RECEIVER_KEY, hotelSearchReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        // Check if due an orientation change, there's a saved search receiver.
        if (retainer.contains(HOTEL_SEARCH_RECEIVER_KEY)) {
            hotelSearchReceiver = (HotelSearchReceiver) retainer.get(HOTEL_SEARCH_RECEIVER_KEY);
            // Reset the activity reference.
            hotelSearchReceiver.setActivity(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case Const.REQUEST_CODE_BOOK_HOTEL: {
            setResult(resultCode, data);
            break;
        }
        }
        finish();
    }

    protected void initValues() {
        Intent intent = getIntent();

        // Setup our future intents while we are here
        noResultsIntent = (Intent) getIntent().clone();
        noResultsIntent.setClass(this, HotelSearchNoResults.class);
        resultsIntent = (Intent) getIntent().clone();
        resultsIntent.setClass(this, HotelSearchResults.class);

        checkInDate = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN);
        checkInDateCal = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
        checkOutDate = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT);
        checkOutDateCal = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR);

        location = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);

        latitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LATITUDE);
        longitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LONGITUDE);

        distanceId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_ID);
        distanceUnitId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID);
        namesContaining = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING);

    }

    protected void initUI() {

        // Set the screen header.
        getSupportActionBar().setTitle(R.string.hotel_search_title);

        // Set the location.
        TextView tv = (TextView) findViewById(R.id.searchNearValue);
        if (tv != null) {
            tv.setText(location);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate searchNearValue text view!");
        }

        // Set the check-in date.
        tv = (TextView) findViewById(R.id.searchCheckInDateTime);
        if (tv != null) {
            tv.setText(checkInDate);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate searchCheckInDateTime text view!");
        }
        // Set the check-out date.
        tv = (TextView) findViewById(R.id.searchCheckOutDateTime);
        if (tv != null) {
            tv.setText(checkOutDate);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate searchCheckOutDateTime text view!");
        }
    }

    protected void startSearch() {
        if (!ConcurCore.isConnected()) {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        } else {
            // Clear out any current results.
            getConcurCore().setHotelSearchResults(null);

            // Make the Async call, pass the current time to SearchResults
            resultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_WORKFLOW_START_TIME, System.currentTimeMillis());
            hotelResultsReceiver = new BaseAsyncResultReceiver(new Handler());
            hotelResultsReceiver.setListener(new HotelResultsListener());
            getHotelsAsycTask = new GetHotels(getApplicationContext(), 1, hotelResultsReceiver, checkOutDateCal,
                    checkInDateCal, namesContaining, latitude, longitude, distanceId, distanceUnitId, 0,
                    Const.HOTEL_RETRIEVE_COUNT);
            getHotelsAsycTask.execute();

        }
    }

    @Override
    protected void updateOfflineHeaderBar(boolean available) {
        super.updateOfflineHeaderBar(available);
        if (!available) {
            // cancel task;
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateOfflineHeaderBar: offline mode detect!");
            if (noResultsIntent == null) {
                noResultsIntent = (Intent) getIntent().clone();
                noResultsIntent.setClass(this, HotelSearchNoResults.class);
            }
            // Launch the no results activity.
            startActivity(noResultsIntent);
            // finish.
            finish();
        }
    }

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
        this.hotelSearchRequest = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.AbstractTravelSearchProgress#getResultsIntent()
     */
    @Override
    public Intent getResultsIntent() {
        return resultsIntent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.AbstractTravelSearchProgress#getNoResultsIntent()
     */
    @Override
    public Intent getNoResultsIntent() {
        return noResultsIntent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.AbstractTravelSearchProgress#finishActivityOnNoResults()
     */
    @Override
    public boolean finishActivityOnNoResults() {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and stop any outstanding
        // request of async task
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getHotelsAsycTask != null) {
                getHotelsAsycTask.cancel(true);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // MOB-15804
    public void showSearchFailedDialog(String message) {
        searchFailedDialog = new AlertDialogFragment();
        searchFailedDialog.setTitle(R.string.dlg_travel_hotel_hotel_search_failed_title);
        searchFailedDialog.setMessage(message);
        searchFailedDialog.setPositiveButtonText(R.string.okay);
        searchFailedDialog.setCancelable(false);
        searchFailedDialog.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                hotelResultsReceiver = null;
                finish();
            }

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }
        });
        searchFailedDialog.show(getSupportFragmentManager(), null);
    }

    /**
     * Listener used for displaying the hotel results
     */
    private class HotelResultsListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestSuccess in HotelResultsListener...");
            HotelSearchReply hotelSearchReply = getConcurCore().getHotelSearchResults();

            if (hotelSearchReply.hotelChoices != null && hotelSearchReply.hotelChoices.size() > 0) {

                // Launch the results intent.
                Intent showResultsIntent = getResultsIntent();

                // set the flag to make the search list UI read only if FINAL is false;
                showResultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SHOW_READ_ONLY_LIST, !hotelSearchReply.isFinal);

                showResultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_POLLING_ID, hotelSearchReply.pollingId);

                // request parameters for the pricing end point
                showResultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_ID, distanceId);
                showResultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID, distanceUnitId);
                showResultsIntent.putExtra(Const.EXTRA_TRAVEL_LATITUDE, latitude);
                showResultsIntent.putExtra(Const.EXTRA_TRAVEL_LONGITUDE, longitude);
                showResultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING, namesContaining);
                showResultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR, checkInDateCal);
                showResultsIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR, checkOutDateCal);

                startActivityForResult(showResultsIntent, Const.REQUEST_CODE_BOOK_HOTEL);

            } else {

                // Launch the no results activity.
                startActivity(getNoResultsIntent());

                // finish.
                if (finishActivityOnNoResults()) {
                    finish();
                }
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestFail in HotelResultsListener...");
            // MOB-15804 - if MWS Response has error message then display it
            if (resultData.containsKey("mwsErrorMessage")) {
                String mwsErrorMessage = resultData.getString("mwsErrorMessage");
                if (mwsErrorMessage == null) {
                    mwsErrorMessage = (String) getText(R.string.dlg_travel_hotel_hotel_search_failed_general_msg);
                }
                showSearchFailedDialog(mwsErrorMessage);
            } else {
                // Launch the no results activity.
                startActivity(getNoResultsIntent());

                // finish.
                if (finishActivityOnNoResults()) {
                    finish();
                }
            }
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestCancel in HotelResultsListener...");
        }

        @Override
        public void cleanup() {
            hotelResultsReceiver = null;
        }
    }

}
