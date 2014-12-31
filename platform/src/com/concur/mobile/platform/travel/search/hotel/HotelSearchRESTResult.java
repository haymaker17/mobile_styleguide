package com.concur.mobile.platform.travel.search.hotel;

import java.util.List;

/**
 * Object to hold the search/poll response
 * 
 * @author RatanK
 * 
 */
public class HotelSearchRESTResult {

    public String inventoryId;
    public int totalCount;
    public URLInfo polling;
    public String currency;
    public boolean searchDone;
    public String distanceUnit;
    public List<Hotel> hotels;
    public List<HotelViolation> violations;
}
