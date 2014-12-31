package com.concur.mobile.gov.travel.hotel.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.hotel.activity.HotelReserveRoom;
import com.concur.mobile.core.travel.hotel.service.HotelConfirmRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.activity.GovSegmentList;
import com.concur.mobile.gov.travel.activity.OpenOrExistingAuthListItem;
import com.concur.mobile.gov.travel.activity.TDYPerDiemLocationItem;
import com.concur.mobile.gov.travel.service.PerDiemRateReply;
import com.concur.mobile.gov.util.TravelBookingCache;

public class GovHotelReserveRoom extends HotelReserveRoom {

    private static final String CLS_TAG = GovHotelReserveRoom.class.getSimpleName();

    private static final int BOOK_HOTEL_DOC = 1;

    private Bundle bundle;

    protected HotelConfirmRequest reserveRoomRequest;

    private String existingTANumber;
    private String perdiemLocationID;

    private Trip selectedTrip;

    private boolean isCreatedAuthAvial;
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

    @Override
    public void sendReserveRoomRequest() {
        doReservation();
    }

    private void doReservation() {
        registerReserveRoomReceiver();
        GovAppMobile app = (GovAppMobile) getApplication();
        // Kick-off the request.
        app = (GovAppMobile) getApplication();
        GovService concurService = app.getService();
        String reasonCodeId = "";
        if (reasonCode != null) {
            reasonCodeId = reasonCode.id;
        }
        String violationText = (justificationText != null) ? justificationText : "";
        String creditCardId = null;
        if (curCardChoice != null) {
            creditCardId = curCardChoice.id;
        }
        List<TravelCustomField> tcfs = null;
        // If this booking is part of an existing trip, then custom fields are not presented.
        if (cliqbookTripId == null) {
            tcfs = getTravelCustomFields();
        }
        reserveRoomRequest = concurService.sendConfirmGovHotelRoomRequest(hotelRoom.bicCode, creditCardId, hotel.chainCode,
            propertyId, hotel.hotel, hotelRoom.sellSource, cliqbookTripId, violationText,
            reasonCodeId, tcfs, existingTANumber, perdiemLocationID, selectedPerDiemItem);
        if (reserveRoomRequest != null) {
            showDialog(BOOKING_PROGRESS_DIALOG);
        } else {
            getApplicationContext().unregisterReceiver(reserveRoomReceiver);
            unregisterReserveRoomReceiver();
        }

        // Log the flurry event if the user completed this hotel booking using Voice.
        if (getIntent().getBooleanExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, false)) {
            EventTracker.INSTANCE.track(Flurry.CATEGORY_VOICE_BOOK, Flurry.EVENT_NAME_COMPLETED_HOTEL);
        }
    }

    @Override
    protected void onHandleSuccessReservation(HotelReserveRoom activity, Intent intent) {
        bundle = intent.getExtras();
        locateTripId(activity, intent);
        flurryEvents(activity);
        activity.showDialog(BOOKING_SUCCEEDED_DIALOG);
    }

    protected void flurryEvents(HotelReserveRoom activity) {
        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_HOTEL);
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
            return getText(com.concur.gov.R.string.gov_travel_booking_success_groupauth_msg);
        } else {
            return super.getBookingSucceededDialogMessage();
        }

    }

    @Override
    protected void onBookingSucceeded() {
        super.onBookingSucceeded();
    }

    @Override
    protected void onHandleSuccessItineraryList(Trip trip, TravelBaseActivity activity) {
        selectedTrip = trip;
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
        return new Intent(GovHotelReserveRoom.this, GovSegmentList.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case BOOK_HOTEL_DOC: {
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
