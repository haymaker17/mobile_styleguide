package com.concur.mobile.core.travel.hotel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.platform.ui.travel.util.Const;

/**
 * activity to show Itinerary
 *
 * @author tejoa
 */
public class ShowHotelItinerary extends TravelBaseActivity {

    private static final String CLS_TAG = ShowHotelItinerary.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hotel was booked, set the result code to okay.
        Log.d(Const.LOG_TAG, CLS_TAG + ".Hotel was booked, set the result code to okay!");
        Intent data = getIntent();
        itinLocator = data.getStringExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR);
        bookingRecordLocator = data.getStringExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR);
        if (cliqbookTripId != null) {
            IItineraryCache itinCache = this.getConcurCore().getItinCache();
            Trip trip = itinCache.getItinerarySummaryByCliqbookTripId(cliqbookTripId);
            if (trip != null) {
                data.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, trip.itinLocator);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to locate trip based on cliqbook trip id!");
            }
        }

        onBookingSucceeded();

    }

    @Override
    protected void onBookingSucceeded() {
        if (!launchedWithCliqbookTripId) {
            // Set the flag that the trip list should be refetched.
            IItineraryCache itinCache = getConcurCore().getItinCache();
            if (itinCache != null) {
                itinCache.setShouldRefetchSummaryList(true);
            }
            // Retrieve an updated trip summary list, then retrieve the detailed itinerary.
            isShowRatingPrompt = true;
            sendItinerarySummaryListRequest();
        } else {
            // Just finish the activity.
            finish();
        }
    }
}
