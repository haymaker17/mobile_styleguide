package com.concur.mobile.platform.ui.travel.util;

public class Const {

    // Instantiation is a crime.
    private Const() {
    }

    // -------------------------------------------------
    // General
    // -------------------------------------------------
    public static final String LOG_TAG = "CNQR.PLATFORM.UI.TRAVEL";

    // Travel related extra intent key values.
    public static final String EXTRA_TRAVEL_LATITUDE = "travel.latitude";
    public static final String EXTRA_TRAVEL_LONGITUDE = "travel.longitude";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN = "travel.hotel.search.check.in";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR = "travel.hotel.search.check.in.calendar";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT = "travel.hotel.search.check.out";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR = "travel.hotel.search.check.out.calendar";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION = "travel.hotel.search.location";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_OF_STAY = "travel.hotel.search.duration.of.stay";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS = "travel.hotel.search.duration.number.of.nights";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING = "travel.hotel.search.names.containing";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID = "travel.hotel.search.distance.unit";
    public static final String EXTRA_LOCATION_SEARCH_ALLOWED_MODES = "loc_search_modes_allowed";
    public static final String EXTRA_LOCATION_SEARCH_MODE_USED = "loc_search_mode_used";
    public static final String EXTRA_TRAVEL_LOCATION = "travel.location";
    public static final String EXTRA_TRAVEL_CLIQBOOK_TRIP_ID = "travel.cliqbook.trip.id";

    public static final String ACTION_HOTEL_SEARCH_RESULTS = "com.concur.mobile.action.HOTELS_FOUND";
    public static final String EXTRA_HOTELS_LIST = "hotel.list";
    public static final String EXTRA_HOTELS_DETAILS = "hotel.details";
    public static final String EXTRA_HOTEL_IMAGE_ITEM = "hotel.image";
    public static final String EXTRA_HOTEL_LOCATION = "hotel.location";

    // Activity result codes
    public static final int REQUEST_CODE_LOCATION = 1;
    public static final int REQUEST_CODE_BOOK_HOTEL = 2;

    public static final String EXTRA_IMAGE_URL = "image.url";
    public static final String EXTRA_IMAGE_TITLE = "image.title";

}
