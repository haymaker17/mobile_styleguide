package com.concur.mobile.core.travel.data;

import com.concur.mobile.platform.util.Parse;

/**
 * Object for TravelPointsConfig context that comes in UserConfigV2 MWS response. Used in Price To Beat feature for Air and Hotel
 * 
 * @author RatanK
 * 
 */
public class TravelPointsConfig {

    private boolean airTravelPointsEnabled;
    private boolean hotelTravelPointsEnabled;

    public void setAirTravelPointsEnabled(boolean airTravelPointsEnabled) {
        this.airTravelPointsEnabled = airTravelPointsEnabled;
    }

    public void setHotelTravelPointsEnabled(boolean hotelTravelPointsEnabled) {
        this.hotelTravelPointsEnabled = hotelTravelPointsEnabled;
    }

    public boolean isAirTravelPointsEnabled() {
        return airTravelPointsEnabled;
    }

    public boolean isHotelTravelPointsEnabled() {
        return hotelTravelPointsEnabled;
    }

    public void handleElement(String name, String value) {
        if (name.equalsIgnoreCase("AirTravelPointsEnabled")) {
            airTravelPointsEnabled = Parse.safeParseBoolean(value);
        } else if (name.equalsIgnoreCase("HotelTravelPointsEnabled")) {
            hotelTravelPointsEnabled = Parse.safeParseBoolean(value);
        }
    }
}
