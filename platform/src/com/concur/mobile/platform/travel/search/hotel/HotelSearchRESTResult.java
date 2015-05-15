package com.concur.mobile.platform.travel.search.hotel;

import java.util.List;

import com.concur.mobile.platform.travel.provider.Travel;

/**
 * Object to hold the search/poll response
 * 
 * @author RatanK
 * 
 */
public class HotelSearchRESTResult {

    public static String[] fullColumnList = { Travel.HotelSearchResultColumns._ID };

    public String inventoryId;
    public int totalCount;
    public URLInfo polling;
    public String currency;
    public boolean searchDone;
    public String distanceUnit;
    public List<Hotel> hotels;
    public List<HotelViolation> violations;
    public String searchUrl;
    public BenchmarksCollection benchmarksCollection;
}
