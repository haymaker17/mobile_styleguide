package com.concur.mobile.gov.travel.car.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.concur.gov.R;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.car.activity.CarSearchDetail;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.activity.GovSegmentList;
import com.concur.mobile.gov.travel.activity.OpenOrExistingAuthListItem;
import com.concur.mobile.gov.travel.activity.TDYPerDiemLocationItem;
import com.concur.mobile.gov.travel.service.GovCarSellRequest;
import com.concur.mobile.gov.travel.service.PerDiemRateReply;
import com.concur.mobile.gov.util.TravelBookingCache;

public class GovCarSearchDetail extends CarSearchDetail {

    public static final String CLS_TAG = GovCarSearchDetail.class.getSimpleName();

    /*
     * public static final String BOOKING_TYPE = "bookingType";
     * public static final String TRAV_ID = "travID";
     * public static final String BUNDLE = "bundle";
     * public static final String TRIP_ID = "tripId";
     */
    public static final String ISREFRESH = "isRefreshRequiredForAuthList";

    private static final int BOOK_CAR_DOC = 1;

    private String existingTANumber;
    private String perdiemLocationID;
    private TDYPerDiemLocationItem selectedPerDiemItem;

    protected GovCarSellRequest reserveCarRequest;

    private Trip selectedTrip;

    private boolean isCreatedAuthAvial;

    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);
        GovAppMobile app = (GovAppMobile) getApplication();
        TravelBookingCache cache = app.trvlBookingCache;
        if (cache.isGenerateAuthUsed()) {
            if (!isCreatedAuthAvial) {
                existingTANumber = null;
            }

        } else {
            OpenOrExistingAuthListItem item = cache.getSelectedAuthItem();
            if (item == null) {
                existingTANumber = null;
            } else {
                existingTANumber = item.getItem().taNumber;
            }
        }
        PerDiemRateReply reply = cache.getPerDiemRateReply();
        if (reply == null || reply.rateList == null || reply.rateList.size() <= 0) {
            perdiemLocationID = null;
        } else {
            perdiemLocationID = reply.rateList.get(0).tabRow;
        }
        // get perdiem item.
        selectedPerDiemItem = cache.getSelectedPerDiemItem();
    }

    /**
     * Will send a request off to reserve the car.
     */
    @Override
    protected void sendReserveCarRequest() {
        doReservation();
    }

    private void doReservation() {
        GovService service = (GovService) getConcurService();
        if (service != null) {
            registerReserveCarReceiver();

            String creditCardId = null;
            if ((isSendCreditCard() == null || isSendCreditCard())
                && curCardChoice != null) {
                creditCardId = curCardChoice.id;
            }
            String reasonCodeId = "";
            if (reasonCode != null) {
                reasonCodeId = reasonCode.id;
            }
            String violationText = (justificationText != null) ? justificationText : "";
            List<TravelCustomField> tcfs = null;
            // If this booking is part of an existing trip, then custom fields are not presented.
            if (cliqbookTripId == null) {
                tcfs = getTravelCustomFields();
            }
            reserveCarRequest = service.reserveCar(carId, creditCardId, ((recordLocator != null) ? recordLocator
                : ""), ((cliqbookTripId != null) ? cliqbookTripId : ""), ((clientLocator != null) ? clientLocator
                : ""), reasonCodeId, violationText, tcfs, existingTANumber, perdiemLocationID, selectedPerDiemItem);
            if (reserveCarRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendCarRequest: unable to create 'GovCarSearchDetail' request!");
                unregisterReserveCarReceiver();
            } else {
                // Set the request object on the receiver.
                reserveCarReceiver.setServiceRequest(reserveCarRequest);
                // Show the progress dialog.
                showDialog(BOOKING_PROGRESS_DIALOG);
            }
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".sendCarRequest: service is unavailable.");
        }
    }

    @Override
    protected void onHandleSuccessReservation(CarSearchDetail activity, Intent intent) {
        Bundle bundle = intent.getExtras();
        locateTripId(activity, intent);
        flurryEvents(activity);
        activity.showDialog(BOOKING_SUCCEEDED_DIALOG);
    }

    protected void flurryEvents(CarSearchDetail activity) {
        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_CAR);
        Intent launchIntent = activity.getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            params.put(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        EventTracker.INSTANCE.track(Flurry.CATEGORY_BOOK, Flurry.EVENT_NAME_RESERVE, params);
    }

    @Override
    protected CharSequence getBookingSucceededDialogMessage() {
        GovAppMobile app = (GovAppMobile) getApplication();
        if (app.trvlBookingCache.isGroupAuthUsed()) {
            return getText(R.string.gov_travel_booking_success_groupauth_msg);
        } else {
            return super.getBookingSucceededDialogMessage();
        }

    }

    @Override
    protected void onBookingSucceeded() {
        super.onBookingSucceeded();
        /*
         * GovAppMobile app = (GovAppMobile) getApplication();
         * if (app.trvlBookingCache.isGroupAuthUsed()) {
         * Intent it = new Intent(this, AuthorizationListActivity.class);
         * it.putExtra(ISREFRESH, true);
         * setResult(RESULT_OK);
         * startActivity(it);
         * // After successfull booking remove all the cache data
         * app.trvlBookingCache = new TravelBookingCache();
         * finish();
         * } else {
         * super.onBookingSucceeded();
         * }
         */
    }

    @Override
    protected void onHandleSuccessItineraryList(Trip trip, TravelBaseActivity activity) {
        selectedTrip = trip;
        /*
         * Intent it = new Intent(GovCarSearchDetail.this, DocInfoFromTripLocator.class);
         * String bookingType = ((TextView) findViewById(R.id.header_navigation_bar_title)).getText().toString();
         * if (bundle != null) {
         * bundle.putString(BOOKING_TYPE, bookingType);
         * String tripId = trip.cliqbookTripId;
         * bundle.putString(TRIP_ID, tripId);
         * it.putExtra(BUNDLE, bundle);
         * startActivityForResult(it, BOOK_CAR_DOC);
         * }
         */
        if (selectedTrip != null) {
            // set Auth number for future.
            sendItineraryRequest(selectedTrip.itinLocator);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onHandleSuccessItineraryList.selectedTrip==null");
            finish();
        }
    }

    @Override
    protected Intent getSegmentIntent() {
        return new Intent(GovCarSearchDetail.this, GovSegmentList.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case BOOK_CAR_DOC: {
            // After successfull booking remove all the cache data
            GovAppMobile app = (GovAppMobile) getApplication();
            app.trvlBookingCache = new TravelBookingCache();
            if (resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            } else {
                if (selectedTrip != null) {
                    sendItineraryRequest(selectedTrip.itinLocator);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult.selectedTrip==null");
                    finish();
                }
            }
            break;
        }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * restore any receiver store for this activity into Retainer.
     * */
    protected void restoreReceivers() {
        super.restoreReceivers();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }
}
