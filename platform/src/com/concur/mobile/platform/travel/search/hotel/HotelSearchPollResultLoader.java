package com.concur.mobile.platform.travel.search.hotel;

import java.util.Calendar;

import android.content.Context;

/**
 * 
 * Async task loader class for the hotel poll
 * 
 * @author RatanK
 * 
 */

public class HotelSearchPollResultLoader extends HotelSearchResultLoader {

    private static final String CLS_TAG = "HotelSearchPollRESTResultLoader";

    public HotelSearchPollResultLoader(Context context, Calendar checkInDate, Calendar checkOutDate, Double lat,
            Double lon, Integer radius, String distanceUnit, String pollingURL) {

        super(context, checkInDate, checkOutDate, lat, lon, radius, distanceUnit);

        this.pollingURL = pollingURL;
    }

    @Override
    protected String getServiceEndPoint() {
        return pollingURL;
    }

}
