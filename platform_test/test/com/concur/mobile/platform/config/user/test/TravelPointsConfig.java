package com.concur.mobile.platform.config.user.test;

import org.simpleframework.xml.Element;

public class TravelPointsConfig {

    /**
     * Contains whether air travel points is enabled.
     */
    @Element(name = "AirTravelPointsEnabled")
    public Boolean airTravelPointsEnabled;

    /**
     * Contains whether hotel travel points is enabled.
     */
    @Element(name = "HotelTravelPointsEnabled")
    public Boolean hotelTravelPointsEnabled;

}
