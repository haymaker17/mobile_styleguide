/**
 * 
 */
package com.concur.mobile.core.travel.car.activity;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.car.data.CarChoice;
import com.concur.mobile.core.travel.car.service.CarSearchRequest;
import com.concur.mobile.core.util.Const;

public class CarSearchProgress extends BaseActivity {

    private static final String CLS_TAG = CarSearchProgress.class.getSimpleName();

    private static final String CAR_SEARCH_RECEIVER_KEY = "car.search.receiver";

    protected Intent resultsIntent;
    protected Intent noResultsIntent;

    protected String location;
    protected String latitude;
    protected String longitude;
    protected String pickUpDate;
    protected Calendar pickupDateTime;
    protected String dropOffDate;
    protected Calendar dropoffDateTime;
    protected String carType;
    protected String locationIata;

    // Contains a reference to the current outstanding car search request.
    private CarSearchRequest carSearchRequest;

    // Contains a reference to a receiver for handling a search response.
    private CarSearchReceiver carSearchReceiver;

    // Contains the filter used to register the above receiver.
    protected final IntentFilter carResultsFilter = new IntentFilter(Const.ACTION_CAR_SEARCH_RESULTS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.car_search_progress);

        initValues();
        initUI();

        // Restore any receivers.
        restoreReceivers();

        startSearch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (carSearchReceiver != null) {
            // Clear the activity reference, it will be set in the new HotelSearch instance.
            carSearchReceiver.setActivity(null);
            retainer.put(CAR_SEARCH_RECEIVER_KEY, carSearchReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        // Check if due an orientation change, there's a saved search receiver.
        if (retainer.contains(CAR_SEARCH_RECEIVER_KEY)) {
            carSearchReceiver = (CarSearchReceiver) retainer.get(CAR_SEARCH_RECEIVER_KEY);
            // Reset the activity reference.
            carSearchReceiver.setActivity(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case Const.REQUEST_CODE_BOOK_CAR:
            setResult(resultCode, data);
            break;
        }
        finish();
    }

    protected void initValues() {
        Intent intent = getIntent();

        // Setup our future intents while we are here
        noResultsIntent = (Intent) getIntent().clone();
        noResultsIntent.setClass(this, CarSearchNoResults.class);
        resultsIntent = (Intent) getIntent().clone();
        resultsIntent.setClass(this, CarSearchResults.class);

        location = intent.getStringExtra(Const.EXTRA_TRAVEL_LOCATION);
        latitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LATITUDE);
        longitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LONGITUDE);
        pickupDateTime = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP_CALENDAR);
        pickUpDate = intent.getStringExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP);
        dropoffDateTime = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF_CALENDAR);
        dropOffDate = intent.getStringExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF);
        carType = intent.getStringExtra(Const.EXTRA_TRAVEL_CAR_TYPE);
        locationIata = intent.getStringExtra(Const.EXTRA_LOCATION_IATA);
    }

    protected void initUI() {

        // Set the screen header.
        getSupportActionBar().setTitle(R.string.car_search_title);

        // Set the location.
        TextView tv = (TextView) findViewById(R.id.searchNearValue);
        if (tv != null) {
            tv.setText(location);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate searchNearValue text view!");
        }

        // MOB-6918 Set the pick-up time.
        tv = (TextView) findViewById(R.id.searchPickUpDateTime);
        if (tv != null) {
            tv.setText(pickUpDate);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate searchPickUpDateTime text view!");
        }
        // MOB-6918 Set the drop-off time.
        tv = (TextView) findViewById(R.id.searchDropOffDateTime);
        if (tv != null) {
            tv.setText(dropOffDate);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate searchDropOffDateTime text view!");
        }
    }

    protected void startSearch() {
        if (!ConcurCore.isConnected()) {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        } else {
            // Make the call
            ConcurService svc = getConcurService();
            if (svc != null) {
                if (carSearchReceiver == null) {
                    carSearchReceiver = new CarSearchReceiver(this);
                }
                getApplicationContext().registerReceiver(carSearchReceiver, carResultsFilter);
                svc.searchForCars(latitude, longitude, pickupDateTime, latitude, longitude, dropoffDateTime, carType,
                        locationIata, locationIata);
                carSearchReceiver.setRequest(carSearchRequest);
            }
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
                noResultsIntent.setClass(this, CarSearchNoResults.class);
            }
            // Launch the no results activity.
            startActivity(noResultsIntent);
            // finish.
            finish();
        }
    }

    /**
     * A broadcast receiver for handling the result of a car search.
     * 
     * @author AndrewK
     */
    static class CarSearchReceiver extends BroadcastReceiver {

        // A reference to the car search activity.
        private CarSearchProgress activity;

        // A reference to the car search request.
        private CarSearchRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>CarSearchReceiver</code> with a search request object.
         * 
         * @param hotelSearch
         */
        CarSearchReceiver(CarSearchProgress activity) {
            this.activity = activity;
        }

        /**
         * Sets the hotel search activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the hotel search activity associated with this broadcast receiver.
         */
        void setActivity(CarSearchProgress activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.carSearchRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the car search request object associated with this broadcast receiver.
         * 
         * @param request
         *            the car search request object associated with this broadcast receiver.
         */
        void setRequest(CarSearchRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        public void onReceive(Context context, Intent i) {

            // Does this receiver have a current activity?
            if (activity != null) {
                activity.getApplicationContext().unregisterReceiver(this);
                int serviceRequestStatus = i.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = i.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (i.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                                    final ArrayList<CarChoice> carChoices = activity.getConcurCore()
                                            .getCarSearchResults().carChoices;
                                    if (carChoices.size() > 0) {

                                        // Launch the results intent.
                                        activity.startActivityForResult(activity.resultsIntent,
                                                Const.REQUEST_CODE_BOOK_CAR);

                                    } else {

                                        // Launch the no results activity.
                                        activity.startActivity(activity.noResultsIntent);

                                        // finish.
                                        activity.finish();
                                    }
                                } else {
                                    activity.actionStatusErrorMessage = i.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage + ".");

                                    // Launch the no results activity.
                                    activity.startActivity(activity.noResultsIntent);

                                    // finish.
                                    activity.finish();
                                }
                            } else {
                                activity.lastHttpErrorMessage = i.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage + ".");

                                // Launch the no results activity.
                                activity.startActivity(activity.noResultsIntent);

                                // finish.
                                activity.finish();
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");

                            // Launch the no results activity.
                            activity.startActivity(activity.noResultsIntent);

                            // finish.
                            activity.finish();
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + i.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));

                            // Launch the no results activity.
                            activity.startActivity(activity.noResultsIntent);

                            // finish.
                            activity.finish();
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");

                    // Launch the no results activity.
                    activity.startActivity(activity.noResultsIntent);

                    // finish.
                    activity.finish();
                }

                // Reset the search request object.
                activity.carSearchRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = i;
            }
        }

    }

}
