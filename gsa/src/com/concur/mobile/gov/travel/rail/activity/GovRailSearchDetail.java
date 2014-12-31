package com.concur.mobile.gov.travel.rail.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.rail.activity.RailSearchDetail;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.activity.GovSegmentList;
import com.concur.mobile.gov.travel.activity.GovTripList;
import com.concur.mobile.gov.travel.activity.TDYPerDiemLocationItem;
import com.concur.mobile.gov.travel.service.PerDiemRateReply;
import com.concur.mobile.gov.util.TravelBookingCache;

public class GovRailSearchDetail extends RailSearchDetail {

    private String existingTANumber;
    private String perdiemLocationID;

    private static final int BOOK_RAIL_DOC = 1;

    private Bundle bundle;

    private Trip selectedTrip;

    private boolean isCreatedAuthAvial = false;

    private TDYPerDiemLocationItem selectedPerDiemItem;

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
            existingTANumber = cache.getSelectedAuthItem().getItem().taNumber;
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

    @Override
    protected void sendReserveRailRequest() {
        doReservation();
    }

    private void doReservation() {
        GovService concurService = (GovService) getConcurService();
        if (concurService != null) {
            registerReserveRailReceiver();

            String creditCardId = null;
            if (curCardChoice != null) {
                creditCardId = curCardChoice.id;
            }

            String reasonCodeId = "";
            if (reasonCode != null) {
                reasonCodeId = reasonCode.id;
            }
            String violationText = (justificationText != null) ? justificationText : "";

            String tdoCode = null;
            if (currentDeliveryOption != null) {
                tdoCode = currentDeliveryOption.id;
            }
            List<TravelCustomField> tcfs = getTravelCustomFields();
            reserveRailRequest = concurService.reserveTrainReq(groupId, bucket, creditCardId, tdoCode, reasonCodeId, violationText, tcfs, existingTANumber, perdiemLocationID, selectedPerDiemItem);
            if (reserveRailRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendRailRequest: unable to create 'RailSellRequest' request!");
                unregisterReserveRailReceiver();
            } else {
                // Set the request object on the receiver.
                reserveRailReceiver.setServiceRequest(reserveRailRequest);
                // Show the delete receipt progress dialog.
                showDialog(BOOKING_PROGRESS_DIALOG);
            }
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".sendRailRequest: service is unavailable.");
        }
    }

    @Override
    protected void onHandleSuccessReservation(RailSearchDetail activity, Intent intent) {
        bundle = intent.getExtras();
        locateTripId(activity, intent);
        flurryEvents(activity);
        activity.showDialog(BOOKING_SUCCEEDED_DIALOG);
    }

    protected void flurryEvents(RailSearchDetail activity) {
        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_TRAIN);
        Intent launchIntent = activity.getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            params.put(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        EventTracker.INSTANCE.track(Flurry.CATEGORY_BOOK, Flurry.EVENT_NAME_RESERVE, params);
    }

    @Override
    protected Intent getTripListIntent() {
        return new Intent(GovRailSearchDetail.this, GovTripList.class);
    }

    @Override
    protected void locateTripId(RailSearchDetail activity, Intent intent) {
        Intent result = new Intent();
        itinLocator = intent.getStringExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR);
        result.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, itinLocator);
        if (cliqbookTripId != null) {
            IItineraryCache itinCache = activity.getConcurCore().getItinCache();
            Trip trip = itinCache.getItinerarySummaryByCliqbookTripId(cliqbookTripId);
            if (trip != null) {
                result.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, trip.itinLocator);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".onReceive: unable to locate trip based on cliqbook trip id!");
            }
        }
        activity.setResult(Activity.RESULT_OK, result);
    }

    @Override
    protected CharSequence getBookingSucceededDialogMessage() {
        GovAppMobile app = (GovAppMobile) getApplication();
        if (app.trvlBookingCache.isGroupAuthUsed()) {
            return getText(com.concur.gov.R.string.gov_travel_booking_success_groupauth_msg);
        } else {
            return super.getBookingSucceededDialogMessage();
        }

    }

    @Override
    protected void onBookingSucceeded() {
        /*
         * GovAppMobile app = (GovAppMobile) getApplication();
         * if (app.trvlBookingCache.isGroupAuthUsed()) {
         * Intent it = new Intent(this, AuthorizationListActivity.class);
         * it.putExtra(GovCarSearchDetail.ISREFRESH, true);
         * setResult(RESULT_OK);
         * startActivity(it);
         * // After successfull booking remove all the cache data
         * app.trvlBookingCache = new TravelBookingCache();
         * finish();
         * } else {
         */
        if (!launchedWithCliqbookTripId) {
            // Set the flag that the trip list should be refetched.
            IItineraryCache itinCache = getConcurCore().getItinCache();
            if (itinCache != null) {
                itinCache.setShouldRefetchSummaryList(true);
            }
            // Retrieve an updated trip summary list, then retrieve the detailed itinerary.
            sendItinerarySummaryListRequest();
        } else {
            // Just finish the activity.
            finish();
        }
        // }

    }

    @Override
    protected void onHandleSuccessItineraryList(Trip trip, TravelBaseActivity activity) {
        selectedTrip = trip;
        /*
         * Intent it = new Intent(GovRailSearchDetail.this, DocInfoFromTripLocator.class);
         * String bookingType = ((TextView) findViewById(R.id.header_navigation_bar_title)).getText().toString();
         * if (bundle != null) {
         * bundle.putString(GovCarSearchDetail.BOOKING_TYPE, bookingType);
         * String tripId = trip.cliqbookTripId;
         * bundle.putString(GovCarSearchDetail.TRIP_ID, tripId);
         * it.putExtra(GovCarSearchDetail.BUNDLE, bundle);
         * startActivityForResult(it, BOOK_RAIL_DOC);
         * }
         */
        if (selectedTrip != null) {
            sendItineraryRequest(selectedTrip.itinLocator);
        } else {
            Intent intent = getTripListIntent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected Intent getSegmentIntent() {
        return new Intent(GovRailSearchDetail.this, GovSegmentList.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case BOOK_RAIL_DOC: {
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
