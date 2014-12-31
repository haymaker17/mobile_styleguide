/**
 * 
 */
package com.concur.mobile.platform.travel.provider;

import java.util.HashMap;

import android.content.Context;
import android.content.UriMatcher;
import android.util.SparseArray;

import com.concur.mobile.platform.provider.EncryptedSQLiteOpenHelper;
import com.concur.mobile.platform.provider.PlatformContentProvider;
import com.concur.mobile.platform.provider.PlatformSQLiteOpenHelper;
import com.concur.mobile.platform.provider.UriMatcherInfo;

/**
 * An extension of <code>PlatformContentProvider</code> providing travel content.
 * 
 * @author andrewk
 */
public class TravelProvider extends PlatformContentProvider {

    // Contains a static reference to the last instantiated <code>TravelProvider</code>.
    private static TravelProvider provider;

    // Trip Summary List.
    private static final int TRIP_SUMMARIES = 0;
    private static final int TRIP_SUMMARY_ID = 1;
    private static final int TRIP_SUMMARY_MESSAGES = 3;
    private static final int TRIP_SUMMARY_MESSAGE_ID = 4;

    // Trip Detail.
    private static final int TRIPS = 5;
    private static final int TRIP_ID = 6;
    private static final int ENHANCEMENT_DAYS = 7;
    private static final int ENHANCEMENT_DAY_ID = 8;
    private static final int SORTABLE_SEGMENTS = 9;
    private static final int SORTABLE_SEGMENT_ID = 10;
    private static final int ENHANCEMENT_OFFERS = 11;
    private static final int ENHANCEMENT_OFFER_ID = 12;
    private static final int OFFER_LINKS = 13;
    private static final int OFFER_LINK_ID = 14;
    private static final int OFFER_CONTENTS = 15;
    private static final int OFFER_CONTENT_ID = 16;
    private static final int CONTENT_LINKS = 17;
    private static final int CONTENT_LINK_ID = 18;
    private static final int MAP_DISPLAYS = 19;
    private static final int MAP_DISPLAY_ID = 20;
    private static final int DISPLAY_OVERLAYS = 21;
    private static final int DISPLAY_OVERLAY_ID = 22;
    private static final int VALIDITY_LOCATIONS = 23;
    private static final int VALIDITY_LOCATION_ID = 24;
    private static final int VALIDITY_TIME_RANGES = 25;
    private static final int VALIDITY_TIME_RANGE_ID = 26;
    private static final int TRIP_RULE_VIOLATIONS = 27;
    private static final int TRIP_RULE_VIOLATION_ID = 28;
    private static final int CAR_RULE_VIOLATIONS = 29;
    private static final int CAR_RULE_VIOLATION_ID = 30;
    private static final int HOTEL_RULE_VIOLATIONS = 31;
    private static final int HOTEL_RULE_VIOLATION_ID = 32;
    private static final int FLIGHT_RULE_VIOLATIONS = 33;
    private static final int FLIGHT_RULE_VIOLATION_ID = 34;
    private static final int RAIL_RULE_VIOLATIONS = 35;
    private static final int RAIL_RULE_VIOLATION_ID = 36;
    private static final int RULES = 37;
    private static final int RULE_ID = 38;
    private static final int RULE_VIOLATION_REASONS = 39;
    private static final int RULE_VIOLATION_REASON_ID = 40;
    private static final int TRAVEL_POINTS = 41;
    private static final int TRAVEL_POINT_ID = 42;
    private static final int BOOKINGS = 43;
    private static final int BOOKING_ID = 44;
    private static final int AIRLINE_TICKETS = 45;
    private static final int AIRLINE_TICKET_ID = 46;
    private static final int PASSENGERS = 47;
    private static final int PASSENGER_ID = 48;
    private static final int FREQUENT_TRAVELER_PROGRAMS = 49;
    private static final int FREQUENT_TRAVELER_PROGRAM_ID = 50;
    private static final int SEGMENTS = 51;
    private static final int SEGMENT_ID = 52;
    private static final int AIR_SEGMENTS = 53;
    private static final int AIR_SEGMENT_ID = 54;
    private static final int FLIGHT_STATUSES = 55;
    private static final int FLIGHT_STATUS_ID = 56;
    private static final int SEATS = 57;
    private static final int SEAT_ID = 58;
    private static final int HOTEL_SEGMENTS = 59;
    private static final int HOTEL_SEGMENT_ID = 60;
    private static final int CAR_SEGMENTS = 61;
    private static final int CAR_SEGMENT_ID = 62;
    private static final int RAIL_SEGMENTS = 63;
    private static final int RAIL_SEGMENT_ID = 64;
    private static final int DINING_SEGMENTS = 65;
    private static final int DINING_SEGMENT_ID = 66;
    private static final int EVENT_SEGMENTS = 67;
    private static final int EVENT_SEGMENT_ID = 68;
    private static final int PARKING_SEGMENTS = 69;
    private static final int PARKING_SEGMENT_ID = 70;
    private static final int RIDE_SEGMENTS = 71;
    private static final int RIDE_SEGMENT_ID = 72;

    // Location Choice
    private static final int LOCATION_CHOICES = 73;
    private static final int LOCATION_CHOICE_ID = 74;

    // Hotel Detail
    private static final int HOTEL_DETAILS = 75;
    private static final int HOTEL_DETAIL_ID = 76;

    // Hotel Image Pair
    private static final int HOTEL_IMAGE_PAIRS = 77;
    private static final int HOTEL_IMAGE_PAIR_ID = 78;

    // Hotel Rate Detail
    private static final int HOTEL_RATE_DETAILS = 79;
    private static final int HOTEL_RATE_DETAIL_ID = 80;

    // Contains the trip summary projection map.
    private static HashMap<String, String> tripSummaryProjectionMap;

    // Contains the trip summary message projection map.
    private static HashMap<String, String> tripSummaryMessageProjectionMap;

    // Contains the Trip projection map.
    static HashMap<String, String> tripProjectionMap;

    // Contains the Enhancement Day projection map.
    static HashMap<String, String> enhancementDayProjectionMap;

    // Contains the Sortable Segment projection map.
    static HashMap<String, String> sortableSegmentProjectionMap;

    // Contains the Enhancement Offer projection map.
    static HashMap<String, String> enhancementOfferProjectionMap;

    // Contains the Offer Link projection map.
    static HashMap<String, String> offerLinkProjectionMap;

    // Contains the Offer Content projection map.
    static HashMap<String, String> offerContentProjectionMap;

    // Contains the Content Link projection map.
    static HashMap<String, String> contentLinkProjectionMap;

    // Contains the Map Display projection map.
    static HashMap<String, String> mapDisplayProjectionMap;

    // Contains the Display Overlay projection map.
    static HashMap<String, String> displayOverlayProjectionMap;

    // Contains the Validity Location projection map.
    static HashMap<String, String> validityLocationProjectionMap;

    // Contains the Validity Time Range projection map.
    static HashMap<String, String> validityTimeRangeProjectionMap;

    // Contains the Trip Rule Violation projection map.
    static HashMap<String, String> tripRuleViolationProjectionMap;

    // Contains the Car Rule Violation projection map.
    static HashMap<String, String> carRuleViolationProjectionMap;

    // Contains the Hotel Rule Violation projection map.
    static HashMap<String, String> hotelRuleViolationProjectionMap;

    // Contains the Flight Rule Violation projection map.
    static HashMap<String, String> flightRuleViolationProjectionMap;

    // Contains the Rail Rule Violation projection map.
    static HashMap<String, String> railRuleViolationProjectionMap;

    // Contains the Rule projection map.
    static HashMap<String, String> ruleProjectionMap;

    // Contains the Rule Violation Reason projection map.
    static HashMap<String, String> ruleViolationReasonProjectionMap;

    // Contains the Travel Point projection map.
    static HashMap<String, String> travelPointProjectionMap;

    // Contains the Booking projection map.
    static HashMap<String, String> bookingProjectionMap;

    // Contains the Airline Ticket projection map.
    static HashMap<String, String> airlineTicketProjectionMap;

    // Contains the Passenger projection map.
    static HashMap<String, String> passengerProjectionMap;

    // Contains the Frequent Traveler Program projection map.
    static HashMap<String, String> frequentTravelerProgramProjectionMap;

    // Contains the Segment projection map.
    static HashMap<String, String> segmentProjectionMap;

    // Contains the Air Segment projection map.
    static HashMap<String, String> airSegmentProjectionMap;

    // Contains the Flight Status projection map.
    static HashMap<String, String> flightStatusProjectionMap;

    // Contains the Seat projection map.
    static HashMap<String, String> seatProjectionMap;

    // Contains the Hotel Segment projection map.
    static HashMap<String, String> hotelSegmentProjectionMap;

    // Contains the Car Segment projection map.
    static HashMap<String, String> carSegmentProjectionMap;

    // Contains the Rail Segment projection map.
    static HashMap<String, String> railSegmentProjectionMap;

    // Contains the Dining Segment projection map.
    static HashMap<String, String> diningSegmentProjectionMap;

    // Contains the Event Segment projection map.
    static HashMap<String, String> eventSegmentProjectionMap;

    // Contains the Parking Segment projection map.
    static HashMap<String, String> parkingSegmentProjectionMap;

    // Contains the Ride Segment projection map.
    static HashMap<String, String> rideSegmentProjectionMap;

    // Contains the Location Choice projection map.
    static HashMap<String, String> locationChoiceProjectionMap;

    // Contains the Hotel Detail projection map.
    static HashMap<String, String> hotelDetailProjectionMap;

    // Contains the Hotel Image Pair projection map.
    static HashMap<String, String> hotelImagePairProjectionMap;

    // Contains the Hotel Rate Detail projection map.
    static HashMap<String, String> hotelRateDetailProjectionMap;

    @Override
    public boolean onCreate() {
        boolean retVal = super.onCreate();

        // Set the static reference.
        provider = this;

        return retVal;
    }

    /**
     * Gets the current instance of <code>TravelProvider</code>.
     * 
     * @return returns the current instance of
     */
    public static TravelProvider getTravelProvider() {
        return provider;
    }

    @Override
    protected UriMatcher initUriMatcher() {

        // Trip summary list support.
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Travel.AUTHORITY, "trip_summaries", TRIP_SUMMARIES);
        matcher.addURI(Travel.AUTHORITY, "trip_summaries/#", TRIP_SUMMARY_ID);
        matcher.addURI(Travel.AUTHORITY, "trip_summary_messages", TRIP_SUMMARY_MESSAGES);
        matcher.addURI(Travel.AUTHORITY, "trip_summary_messages/#", TRIP_SUMMARY_MESSAGE_ID);

        // Trip detail list support.
        matcher.addURI(Travel.AUTHORITY, "trips", TRIPS);
        matcher.addURI(Travel.AUTHORITY, "trips/#", TRIP_ID);
        matcher.addURI(Travel.AUTHORITY, "enhancement_days", ENHANCEMENT_DAYS);
        matcher.addURI(Travel.AUTHORITY, "enhancement_days/#", ENHANCEMENT_DAY_ID);
        matcher.addURI(Travel.AUTHORITY, "sortable_segments", SORTABLE_SEGMENTS);
        matcher.addURI(Travel.AUTHORITY, "sortable_segments/#", SORTABLE_SEGMENT_ID);
        matcher.addURI(Travel.AUTHORITY, "enhancement_offers", ENHANCEMENT_OFFERS);
        matcher.addURI(Travel.AUTHORITY, "enhancement_offers/#", ENHANCEMENT_OFFER_ID);
        matcher.addURI(Travel.AUTHORITY, "offer_links", OFFER_LINKS);
        matcher.addURI(Travel.AUTHORITY, "offer_links/#", OFFER_LINK_ID);
        matcher.addURI(Travel.AUTHORITY, "offer_contents", OFFER_CONTENTS);
        matcher.addURI(Travel.AUTHORITY, "offer_contents/#", OFFER_CONTENT_ID);
        matcher.addURI(Travel.AUTHORITY, "content_links", CONTENT_LINKS);
        matcher.addURI(Travel.AUTHORITY, "content_links/#", CONTENT_LINK_ID);
        matcher.addURI(Travel.AUTHORITY, "map_displays", MAP_DISPLAYS);
        matcher.addURI(Travel.AUTHORITY, "map_displays/#", MAP_DISPLAY_ID);
        matcher.addURI(Travel.AUTHORITY, "display_overlays", DISPLAY_OVERLAYS);
        matcher.addURI(Travel.AUTHORITY, "display_overlays/#", DISPLAY_OVERLAY_ID);
        matcher.addURI(Travel.AUTHORITY, "validity_locations", VALIDITY_LOCATIONS);
        matcher.addURI(Travel.AUTHORITY, "validity_locations/#", VALIDITY_LOCATION_ID);
        matcher.addURI(Travel.AUTHORITY, "validity_time_ranges", VALIDITY_TIME_RANGES);
        matcher.addURI(Travel.AUTHORITY, "validity_time_ranges/#", VALIDITY_TIME_RANGE_ID);
        matcher.addURI(Travel.AUTHORITY, "trip_rule_violations", TRIP_RULE_VIOLATIONS);
        matcher.addURI(Travel.AUTHORITY, "trip_rule_violations/#", TRIP_RULE_VIOLATION_ID);
        matcher.addURI(Travel.AUTHORITY, "car_rule_violations", CAR_RULE_VIOLATIONS);
        matcher.addURI(Travel.AUTHORITY, "car_rule_violations/#", CAR_RULE_VIOLATION_ID);
        matcher.addURI(Travel.AUTHORITY, "hotel_rule_violations", HOTEL_RULE_VIOLATIONS);
        matcher.addURI(Travel.AUTHORITY, "hotel_rule_violations/#", HOTEL_RULE_VIOLATION_ID);
        matcher.addURI(Travel.AUTHORITY, "flight_rule_violations", FLIGHT_RULE_VIOLATIONS);
        matcher.addURI(Travel.AUTHORITY, "flight_rule_violations/#", FLIGHT_RULE_VIOLATION_ID);
        matcher.addURI(Travel.AUTHORITY, "rail_rule_violations", RAIL_RULE_VIOLATIONS);
        matcher.addURI(Travel.AUTHORITY, "rail_rule_violations/#", RAIL_RULE_VIOLATION_ID);
        matcher.addURI(Travel.AUTHORITY, "rules", RULES);
        matcher.addURI(Travel.AUTHORITY, "rules/#", RULE_ID);
        matcher.addURI(Travel.AUTHORITY, "rule_violation_reasons", RULE_VIOLATION_REASONS);
        matcher.addURI(Travel.AUTHORITY, "rule_violation_reasons/#", RULE_VIOLATION_REASON_ID);
        matcher.addURI(Travel.AUTHORITY, "travel_points", TRAVEL_POINTS);
        matcher.addURI(Travel.AUTHORITY, "travel_points/#", TRAVEL_POINT_ID);
        matcher.addURI(Travel.AUTHORITY, "bookings", BOOKINGS);
        matcher.addURI(Travel.AUTHORITY, "bookings/#", BOOKING_ID);
        matcher.addURI(Travel.AUTHORITY, "airline_tickets", AIRLINE_TICKETS);
        matcher.addURI(Travel.AUTHORITY, "airline_tickets/#", AIRLINE_TICKET_ID);
        matcher.addURI(Travel.AUTHORITY, "passengers", PASSENGERS);
        matcher.addURI(Travel.AUTHORITY, "passengers/#", PASSENGER_ID);
        matcher.addURI(Travel.AUTHORITY, "frequent_traveler_programs", FREQUENT_TRAVELER_PROGRAMS);
        matcher.addURI(Travel.AUTHORITY, "frequent_traveler_programs/#", FREQUENT_TRAVELER_PROGRAM_ID);
        matcher.addURI(Travel.AUTHORITY, "segments", SEGMENTS);
        matcher.addURI(Travel.AUTHORITY, "segments/#", SEGMENT_ID);
        matcher.addURI(Travel.AUTHORITY, "air_segments", AIR_SEGMENTS);
        matcher.addURI(Travel.AUTHORITY, "air_segments/#", AIR_SEGMENT_ID);
        matcher.addURI(Travel.AUTHORITY, "flight_statuses", FLIGHT_STATUSES);
        matcher.addURI(Travel.AUTHORITY, "flight_statuses/#", FLIGHT_STATUS_ID);
        matcher.addURI(Travel.AUTHORITY, "seats", SEATS);
        matcher.addURI(Travel.AUTHORITY, "seats/#", SEAT_ID);
        matcher.addURI(Travel.AUTHORITY, "hotel_segments", HOTEL_SEGMENTS);
        matcher.addURI(Travel.AUTHORITY, "hotel_segments/#", HOTEL_SEGMENT_ID);
        matcher.addURI(Travel.AUTHORITY, "car_segments", CAR_SEGMENTS);
        matcher.addURI(Travel.AUTHORITY, "car_segments/#", CAR_SEGMENT_ID);
        matcher.addURI(Travel.AUTHORITY, "rail_segments", RAIL_SEGMENTS);
        matcher.addURI(Travel.AUTHORITY, "rail_segments/#", RAIL_SEGMENT_ID);
        matcher.addURI(Travel.AUTHORITY, "dining_segments", DINING_SEGMENTS);
        matcher.addURI(Travel.AUTHORITY, "dining_segments/#", DINING_SEGMENT_ID);
        matcher.addURI(Travel.AUTHORITY, "event_segments", EVENT_SEGMENTS);
        matcher.addURI(Travel.AUTHORITY, "event_segments/#", EVENT_SEGMENT_ID);
        matcher.addURI(Travel.AUTHORITY, "parking_segments", PARKING_SEGMENTS);
        matcher.addURI(Travel.AUTHORITY, "parking_segments/#", PARKING_SEGMENT_ID);
        matcher.addURI(Travel.AUTHORITY, "ride_segments", RIDE_SEGMENTS);
        matcher.addURI(Travel.AUTHORITY, "ride_segments/#", RIDE_SEGMENT_ID);

        // Location Choice support
        matcher.addURI(Travel.AUTHORITY, "location_choices", LOCATION_CHOICES);
        matcher.addURI(Travel.AUTHORITY, "location_choices/#", LOCATION_CHOICE_ID);

        // Hotel Detail support
        matcher.addURI(Travel.AUTHORITY, "hotel_details", HOTEL_DETAILS);
        matcher.addURI(Travel.AUTHORITY, "hotel_details/#", HOTEL_DETAIL_ID);

        // Hotel Image Pair support
        matcher.addURI(Travel.AUTHORITY, "hotel_image_pairs", HOTEL_IMAGE_PAIRS);
        matcher.addURI(Travel.AUTHORITY, "hotel_image_pairs/#", HOTEL_IMAGE_PAIR_ID);

        // Hotel Rate Detail support
        matcher.addURI(Travel.AUTHORITY, "hotel_rate_details", HOTEL_RATE_DETAILS);
        matcher.addURI(Travel.AUTHORITY, "hotel_rate_details/#", HOTEL_RATE_DETAIL_ID);

        return matcher;
    }

    @Override
    protected void initProjectionMaps() {

        // Creates and initializes the trip summary projection map.
        tripSummaryProjectionMap = new HashMap<String, String>();
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns._ID, Travel.TripSummaryColumns._ID);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns._COUNT, Travel.TripSummaryColumns._COUNT);

        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.APPROVAL_STATUS,
                Travel.TripSummaryColumns.APPROVAL_STATUS);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.APPROVER_ID, Travel.TripSummaryColumns.APPROVER_ID);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.APPROVER_NAME, Travel.TripSummaryColumns.APPROVER_NAME);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.AUTHORIZATION_NUMBER,
                Travel.TripSummaryColumns.AUTHORIZATION_NUMBER);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.BOOKED_VIA, Travel.TripSummaryColumns.BOOKED_VIA);
        tripSummaryProjectionMap
                .put(Travel.TripSummaryColumns.BOOKING_SOURCE, Travel.TripSummaryColumns.BOOKING_SOURCE);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.CAN_BE_EXPENSED,
                Travel.TripSummaryColumns.CAN_BE_EXPENSED);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.CLIQ_BOOK_STATE,
                Travel.TripSummaryColumns.CLIQ_BOOK_STATE);
        tripSummaryProjectionMap
                .put(Travel.TripSummaryColumns.END_DATE_LOCAL, Travel.TripSummaryColumns.END_DATE_LOCAL);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.END_DATE_UTC, Travel.TripSummaryColumns.END_DATE_UTC);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.HAS_OTHERS, Travel.TripSummaryColumns.HAS_OTHERS);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.HAS_TICKETS, Travel.TripSummaryColumns.HAS_TICKETS);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.IS_EXPENSED, Travel.TripSummaryColumns.IS_EXPENSED);
        tripSummaryProjectionMap
                .put(Travel.TripSummaryColumns.IS_GDS_BOOKING, Travel.TripSummaryColumns.IS_GDS_BOOKING);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.IS_PERSONAL, Travel.TripSummaryColumns.IS_PERSONAL);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.IS_WITHDRAWN, Travel.TripSummaryColumns.IS_WITHDRAWN);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.IS_PUBLIC, Travel.TripSummaryColumns.IS_PUBLIC);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.ITIN_ID, Travel.TripSummaryColumns.ITIN_ID);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.ITIN_LOCATOR, Travel.TripSummaryColumns.ITIN_LOCATOR);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.ITIN_SOURCE_LIST,
                Travel.TripSummaryColumns.ITIN_SOURCE_LIST);
        tripSummaryProjectionMap
                .put(Travel.TripSummaryColumns.RECORD_LOCATOR, Travel.TripSummaryColumns.RECORD_LOCATOR);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.SEGMENT_TYPES, Travel.TripSummaryColumns.SEGMENT_TYPES);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.START_DATE_LOCAL,
                Travel.TripSummaryColumns.START_DATE_LOCAL);
        tripSummaryProjectionMap
                .put(Travel.TripSummaryColumns.START_DATE_UTC, Travel.TripSummaryColumns.START_DATE_UTC);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.TRIP_ID, Travel.TripSummaryColumns.TRIP_ID);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.TRIP_KEY, Travel.TripSummaryColumns.TRIP_KEY);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.TRIP_NAME, Travel.TripSummaryColumns.TRIP_NAME);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.TRIP_STATUS, Travel.TripSummaryColumns.TRIP_STATUS);
        tripSummaryProjectionMap.put(Travel.TripSummaryColumns.USER_ID, Travel.TripSummaryColumns.USER_ID);

        // Creates and initializes the trip summary message map.
        tripSummaryMessageProjectionMap = new HashMap<String, String>();
        tripSummaryMessageProjectionMap.put(Travel.TripSummaryMessageColumns._ID, Travel.TripSummaryMessageColumns._ID);
        tripSummaryMessageProjectionMap.put(Travel.TripSummaryMessageColumns._COUNT,
                Travel.TripSummaryMessageColumns._COUNT);

        tripSummaryMessageProjectionMap.put(Travel.TripSummaryMessageColumns.MESSAGE,
                Travel.TripSummaryMessageColumns.MESSAGE);
        tripSummaryMessageProjectionMap.put(Travel.TripSummaryMessageColumns.TRIP_ID,
                Travel.TripSummaryMessageColumns.TRIP_ID);
        tripSummaryMessageProjectionMap.put(Travel.TripSummaryMessageColumns.USER_ID,
                Travel.TripSummaryMessageColumns.USER_ID);

        // Create and intialize the various trip projection maps.
        initTripDetailProjectionMaps();

        // Creates and initializes the location choice projection map.
        locationChoiceProjectionMap = new HashMap<String, String>();
        locationChoiceProjectionMap.put(Travel.LocationChoiceColumns._ID, Travel.LocationChoiceColumns._ID);
        locationChoiceProjectionMap.put(Travel.LocationChoiceColumns._COUNT, Travel.LocationChoiceColumns._COUNT);
        locationChoiceProjectionMap.put(Travel.LocationChoiceColumns.CITY, Travel.LocationChoiceColumns.CITY);
        locationChoiceProjectionMap.put(Travel.LocationChoiceColumns.COUNTRY, Travel.LocationChoiceColumns.COUNTRY);
        locationChoiceProjectionMap.put(Travel.LocationChoiceColumns.COUNTRY_ABBREVIATION,
                Travel.LocationChoiceColumns.COUNTRY_ABBREVIATION);
        locationChoiceProjectionMap.put(Travel.LocationChoiceColumns.IATA, Travel.LocationChoiceColumns.IATA);
        locationChoiceProjectionMap.put(Travel.LocationChoiceColumns.LOCATION, Travel.LocationChoiceColumns.LOCATION);
        locationChoiceProjectionMap.put(Travel.LocationChoiceColumns.STATE, Travel.LocationChoiceColumns.STATE);
        locationChoiceProjectionMap.put(Travel.LocationChoiceColumns.LAT, Travel.LocationChoiceColumns.LAT);
        locationChoiceProjectionMap.put(Travel.LocationChoiceColumns.LON, Travel.LocationChoiceColumns.LON);

        // Create and intialize the various hotel detail projection maps.
        initHotelDetailProjectionMaps();

    }

    @Override
    protected SparseArray<UriMatcherInfo> initCodeUriMatcherInfoMap() {

        SparseArray<UriMatcherInfo> map = new SparseArray<UriMatcherInfo>();
        // Init the TRIP_SUMMARIES info.
        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.TripSummaryColumns.CONTENT_TYPE;
        info.tableName = Travel.TripSummaryColumns.TABLE_NAME;
        info.nullColumnName = Travel.TripSummaryColumns.USER_ID;
        info.contentIdUriBase = Travel.TripSummaryColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.TripSummaryColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = tripSummaryProjectionMap;
        map.put(TRIP_SUMMARIES, info);

        // Init the TRIP_SUMMARY_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.TripSummaryColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.TripSummaryColumns.TABLE_NAME;
        info.nullColumnName = Travel.TripSummaryColumns.USER_ID;
        info.contentIdUriBase = Travel.TripSummaryColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.TripSummaryColumns._ID;
        info.projectionMap = tripSummaryProjectionMap;
        info.defaultSortOrder = Travel.TripSummaryColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.TripSummaryColumns.TRIP_SUMMARY_ID_PATH_POSITION;
        map.put(TRIP_SUMMARY_ID, info);

        // Init the TRIP_SUMMARY_MESSAGES info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.TripSummaryMessageColumns.CONTENT_TYPE;
        info.tableName = Travel.TripSummaryMessageColumns.TABLE_NAME;
        info.nullColumnName = Travel.TripSummaryMessageColumns.USER_ID;
        info.contentIdUriBase = Travel.TripSummaryMessageColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.TripSummaryMessageColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = tripSummaryMessageProjectionMap;
        map.put(TRIP_SUMMARY_MESSAGES, info);

        // Init the TRIP_SUMMARY_MESSAGE_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.TripSummaryMessageColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.TripSummaryMessageColumns.TABLE_NAME;
        info.nullColumnName = Travel.TripSummaryMessageColumns.USER_ID;
        info.contentIdUriBase = Travel.TripSummaryMessageColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.TripSummaryMessageColumns._ID;
        info.projectionMap = tripSummaryMessageProjectionMap;
        info.defaultSortOrder = Travel.TripSummaryMessageColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.TripSummaryMessageColumns.TRIP_SUMMARY_MESSAGE_ID_PATH_POSITION;
        map.put(TRIP_SUMMARY_MESSAGE_ID, info);

        initTripDetailCodeUriMatcherInfoMap(map);

        // Init the LOCATION_CHOICES info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.LocationChoiceColumns.CONTENT_TYPE;
        info.tableName = Travel.LocationChoiceColumns.TABLE_NAME;
        info.nullColumnName = Travel.LocationChoiceColumns.IATA;
        info.contentIdUriBase = Travel.LocationChoiceColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.LocationChoiceColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = locationChoiceProjectionMap;
        map.put(LOCATION_CHOICES, info);

        // Init the LOCATION_CHOICE_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.LocationChoiceColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.LocationChoiceColumns.TABLE_NAME;
        info.nullColumnName = Travel.LocationChoiceColumns.IATA;
        info.contentIdUriBase = Travel.LocationChoiceColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.LocationChoiceColumns._ID;
        info.projectionMap = locationChoiceProjectionMap;
        info.defaultSortOrder = Travel.LocationChoiceColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.LocationChoiceColumns.LOCATION_CHOICE_ID_PATH_POSITION;
        map.put(LOCATION_CHOICE_ID, info);

        // Init the hotel Detail related info
        initHotelDetailCodeUriMatcherInfoMap(map);

        return map;
    }

    @Override
    public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
        // This implementation will use an encrypted database.
        PlatformSQLiteOpenHelper helper = new EncryptedSQLiteOpenHelper(new EncryptedTravelDBHelper(context));
        return helper;
    }

    @Override
    protected String getDatabaseName() {
        return TravelDBSchema.DATABASE_NAME;
    }

    /**
     * This method creates and initializes the various projection maps for the tables supporting a trip detail object.
     */
    private void initTripDetailProjectionMaps() {

        // Init the Trip projection map.
        tripProjectionMap = TravelProviderUtil.initTripProjectionMap();

        // Init the Enhancement Day projection map.
        enhancementDayProjectionMap = TravelProviderUtil.initEnhancementDayProjectionMap();

        // Init the Sortable Segment projection map.
        sortableSegmentProjectionMap = TravelProviderUtil.initSortableSegmentProjectionMap();

        // Init the Enhancement Offer projection map.
        enhancementOfferProjectionMap = TravelProviderUtil.initEnhancementOfferProjectionMap();

        // Init the Offer Link projection map.
        offerLinkProjectionMap = TravelProviderUtil.initOfferLinkProjectionMap();

        // Init the Offer Content projection map.
        offerContentProjectionMap = TravelProviderUtil.initOfferContentProjectionMap();

        // Init the Content Link projection map.
        contentLinkProjectionMap = TravelProviderUtil.initContentLinkProjectionMap();

        // Init the Map Display projection map.
        mapDisplayProjectionMap = TravelProviderUtil.initMapDisplayProjectionMap();

        // Init the Display Overlay projection map.
        displayOverlayProjectionMap = TravelProviderUtil.initDisplayOverlayProjectionMap();

        // Init the Validity Location projection map.
        validityLocationProjectionMap = TravelProviderUtil.initValidityLocationProjectionMap();

        // Init the Validity Time Range projection map.
        validityTimeRangeProjectionMap = TravelProviderUtil.initValidityTimeRangeProjectionMap();

        // Init the Trip Rule Violation projection map.
        tripRuleViolationProjectionMap = TravelProviderUtil.initTripRuleViolationProjectionMap();

        // Init the Car Rule Violation projection map.
        carRuleViolationProjectionMap = TravelProviderUtil.initCarRuleViolationProjectionMap();

        // Init the Hotel Rule Violation projection map.
        hotelRuleViolationProjectionMap = TravelProviderUtil.initHotelRuleViolationProjectionMap();

        // Init the Flight Rule Violation projection map.
        flightRuleViolationProjectionMap = TravelProviderUtil.initFlightRuleViolationProjectionMap();

        // Init the Rail Rule Violation projection map.
        railRuleViolationProjectionMap = TravelProviderUtil.initRailRuleViolationProjectionMap();

        // Init the Rule projection map.
        ruleProjectionMap = TravelProviderUtil.initRuleProjectionMap();

        // Init the Rule Violation Reason projection map.
        ruleViolationReasonProjectionMap = TravelProviderUtil.initRuleViolationReasonProjectionMap();

        // Init the Travel Point projection map.
        travelPointProjectionMap = TravelProviderUtil.initTravelPointProjectionMap();

        // Init the Booking projection map.
        bookingProjectionMap = TravelProviderUtil.initBookingProjectionMap();

        // Init the Airline Ticket projection map.
        airlineTicketProjectionMap = TravelProviderUtil.initAirlineTicketProjectionMap();

        // Init the Passenger projection map.
        passengerProjectionMap = TravelProviderUtil.initPassengerProjectionMap();

        // Init the Frequent Traveler Program projection map.
        frequentTravelerProgramProjectionMap = TravelProviderUtil.initFrequentTravelerProgramProjectionMap();

        // Init the Segment projection map.
        segmentProjectionMap = TravelProviderUtil.initSegmentProjectionMap();

        // Init the Air Segment projection map.
        airSegmentProjectionMap = TravelProviderUtil.initAirSegmentProjectionMap();

        // Init the Flight Status projection map.
        flightStatusProjectionMap = TravelProviderUtil.initFlightStatusProjectionMap();

        // Init the Seat projection map.
        seatProjectionMap = TravelProviderUtil.initSeatProjectionMap();

        // Init the Hotel Segment projection map.
        hotelSegmentProjectionMap = TravelProviderUtil.initHotelSegmentProjectionMap();

        // Init the Car Segment projection map.
        carSegmentProjectionMap = TravelProviderUtil.initCarSegmentProjectionMap();

        // Init the Rail Segment projection map.
        railSegmentProjectionMap = TravelProviderUtil.initRailSegmentProjectionMap();

        // Init the Dining Segment projection map.
        diningSegmentProjectionMap = TravelProviderUtil.initDiningSegmentProjectionMap();

        // Init the Event Segment projection map.
        eventSegmentProjectionMap = TravelProviderUtil.initEventSegmentProjectionMap();

        // Init the Parking Segment projection map.
        parkingSegmentProjectionMap = TravelProviderUtil.initParkingSegmentProjectionMap();

        // Init the Ride Segment projection map.
        rideSegmentProjectionMap = TravelProviderUtil.initRideSegmentProjectionMap();
    }

    private void initTripDetailCodeUriMatcherInfoMap(SparseArray<UriMatcherInfo> map) {

        map.put(TRIPS, TravelProviderUtil.initTripsUriMatcherInfo());
        map.put(TRIP_ID, TravelProviderUtil.initTripUriMatcherInfo());

        map.put(ENHANCEMENT_DAYS, TravelProviderUtil.initEnhancementDaysUriMatcherInfo());
        map.put(ENHANCEMENT_DAY_ID, TravelProviderUtil.initEnhancementDayUriMatcherInfo());

        map.put(SORTABLE_SEGMENTS, TravelProviderUtil.initSortableSegmentsUriMatcherInfo());
        map.put(SORTABLE_SEGMENT_ID, TravelProviderUtil.initSortableSegmentUriMatcherInfo());

        map.put(ENHANCEMENT_OFFERS, TravelProviderUtil.initEnhancementOffersUriMatcherInfo());
        map.put(ENHANCEMENT_OFFER_ID, TravelProviderUtil.initEnhancementOfferUriMatcherInfo());

        map.put(OFFER_LINKS, TravelProviderUtil.initOfferLinksUriMatcherInfo());
        map.put(OFFER_LINK_ID, TravelProviderUtil.initOfferLinkUriMatcherInfo());

        map.put(OFFER_CONTENTS, TravelProviderUtil.initOfferContentsUriMatcherInfo());
        map.put(OFFER_CONTENT_ID, TravelProviderUtil.initOfferContentUriMatcherInfo());

        map.put(CONTENT_LINKS, TravelProviderUtil.initContentLinksUriMatcherInfo());
        map.put(CONTENT_LINK_ID, TravelProviderUtil.initContentLinkUriMatcherInfo());

        map.put(MAP_DISPLAYS, TravelProviderUtil.initMapDisplaysUriMatcherInfo());
        map.put(MAP_DISPLAY_ID, TravelProviderUtil.initMapDisplayUriMatcherInfo());

        map.put(DISPLAY_OVERLAYS, TravelProviderUtil.initDisplayOverlaysUriMatcherInfo());
        map.put(DISPLAY_OVERLAY_ID, TravelProviderUtil.initDisplayOverlayUriMatcherInfo());

        map.put(VALIDITY_LOCATIONS, TravelProviderUtil.initValidityLocationsUriMatcherInfo());
        map.put(VALIDITY_LOCATION_ID, TravelProviderUtil.initValidityLocationUriMatcherInfo());

        map.put(VALIDITY_TIME_RANGES, TravelProviderUtil.initValidityTimeRangesUriMatcherInfo());
        map.put(VALIDITY_TIME_RANGE_ID, TravelProviderUtil.initValidityTimeRangeUriMatcherInfo());

        map.put(TRIP_RULE_VIOLATIONS, TravelProviderUtil.initTripRuleViolationsUriMatcherInfo());
        map.put(TRIP_RULE_VIOLATION_ID, TravelProviderUtil.initTripRuleViolationUriMatcherInfo());

        map.put(CAR_RULE_VIOLATIONS, TravelProviderUtil.initCarRuleViolationsUriMatcherInfo());
        map.put(CAR_RULE_VIOLATION_ID, TravelProviderUtil.initCarRuleViolationUriMatcherInfo());

        map.put(HOTEL_RULE_VIOLATIONS, TravelProviderUtil.initHotelRuleViolationsUriMatcherInfo());
        map.put(HOTEL_RULE_VIOLATION_ID, TravelProviderUtil.initHotelRuleViolationUriMatcherInfo());

        map.put(FLIGHT_RULE_VIOLATIONS, TravelProviderUtil.initFlightRuleViolationsUriMatcherInfo());
        map.put(FLIGHT_RULE_VIOLATION_ID, TravelProviderUtil.initFlightRuleViolationUriMatcherInfo());

        map.put(RAIL_RULE_VIOLATIONS, TravelProviderUtil.initRailRuleViolationsUriMatcherInfo());
        map.put(RAIL_RULE_VIOLATION_ID, TravelProviderUtil.initRailRuleViolationUriMatcherInfo());

        map.put(RULES, TravelProviderUtil.initRulesUriMatcherInfo());
        map.put(RULE_ID, TravelProviderUtil.initRuleUriMatcherInfo());

        map.put(RULE_VIOLATION_REASONS, TravelProviderUtil.initRuleViolationReasonsUriMatcherInfo());
        map.put(RULE_VIOLATION_REASON_ID, TravelProviderUtil.initRuleViolationReasonUriMatcherInfo());

        map.put(TRAVEL_POINTS, TravelProviderUtil.initTravelPointsUriMatcherInfo());
        map.put(TRAVEL_POINT_ID, TravelProviderUtil.initTravelPointUriMatcherInfo());

        map.put(BOOKINGS, TravelProviderUtil.initBookingsUriMatcherInfo());
        map.put(BOOKING_ID, TravelProviderUtil.initBookingUriMatcherInfo());

        map.put(AIRLINE_TICKETS, TravelProviderUtil.initAirlineTicketsUriMatcherInfo());
        map.put(AIRLINE_TICKET_ID, TravelProviderUtil.initAirlineTicketUriMatcherInfo());

        map.put(PASSENGERS, TravelProviderUtil.initPassengersUriMatcherInfo());
        map.put(PASSENGER_ID, TravelProviderUtil.initPassengerUriMatcherInfo());

        map.put(FREQUENT_TRAVELER_PROGRAMS, TravelProviderUtil.initFrequentTravelerProgramsUriMatcherInfo());
        map.put(FREQUENT_TRAVELER_PROGRAM_ID, TravelProviderUtil.initFrequentTravelerProgramUriMatcherInfo());

        map.put(SEGMENTS, TravelProviderUtil.initSegmentsUriMatcherInfo());
        map.put(SEGMENT_ID, TravelProviderUtil.initSegmentUriMatcherInfo());

        map.put(AIR_SEGMENTS, TravelProviderUtil.initAirSegmentsUriMatcherInfo());
        map.put(AIR_SEGMENT_ID, TravelProviderUtil.initAirSegmentUriMatcherInfo());

        map.put(FLIGHT_STATUSES, TravelProviderUtil.initFlightStatusesUriMatcherInfo());
        map.put(FLIGHT_STATUS_ID, TravelProviderUtil.initFlightStatusUriMatcherInfo());

        map.put(SEATS, TravelProviderUtil.initSeatsUriMatcherInfo());
        map.put(SEAT_ID, TravelProviderUtil.initSeatUriMatcherInfo());

        map.put(HOTEL_SEGMENTS, TravelProviderUtil.initHotelSegmentsUriMatcherInfo());
        map.put(HOTEL_SEGMENT_ID, TravelProviderUtil.initHotelSegmentUriMatcherInfo());

        map.put(CAR_SEGMENTS, TravelProviderUtil.initCarSegmentsUriMatcherInfo());
        map.put(CAR_SEGMENT_ID, TravelProviderUtil.initCarSegmentUriMatcherInfo());

        map.put(RAIL_SEGMENTS, TravelProviderUtil.initRailSegmentsUriMatcherInfo());
        map.put(RAIL_SEGMENT_ID, TravelProviderUtil.initRailSegmentUriMatcherInfo());

        map.put(DINING_SEGMENTS, TravelProviderUtil.initDiningSegmentsUriMatcherInfo());
        map.put(DINING_SEGMENT_ID, TravelProviderUtil.initDiningSegmentUriMatcherInfo());

        map.put(EVENT_SEGMENTS, TravelProviderUtil.initEventSegmentsUriMatcherInfo());
        map.put(EVENT_SEGMENT_ID, TravelProviderUtil.initEventSegmentUriMatcherInfo());

        map.put(PARKING_SEGMENTS, TravelProviderUtil.initParkingSegmentsUriMatcherInfo());
        map.put(PARKING_SEGMENT_ID, TravelProviderUtil.initParkingSegmentUriMatcherInfo());

        map.put(RIDE_SEGMENTS, TravelProviderUtil.initRideSegmentsUriMatcherInfo());
        map.put(RIDE_SEGMENT_ID, TravelProviderUtil.initRideSegmentUriMatcherInfo());

    }

    /**
     * This method creates and initializes the various projection maps for the tables supporting a hotel detail object.
     */
    private void initHotelDetailProjectionMaps() {

        // Creates and initializes the hotel detail projection map.
        hotelDetailProjectionMap = TravelProviderUtilHotel.initHotelDetailProjectionMap();

        // Creates and initializes the hotel image pair projection map.
        hotelImagePairProjectionMap = TravelProviderUtilHotel.initHotelImagePairProjectionMap();

        // Creates and initializes the hotel rate detail projection map.
        hotelRateDetailProjectionMap = TravelProviderUtilHotel.initHotelRateDetailProjectionMap();

    }

    /**
     * This method initializes the various uri matcher info objects supporting the hotel detail object.
     */
    private void initHotelDetailCodeUriMatcherInfoMap(SparseArray<UriMatcherInfo> map) {

        // TODO - bulkInserter ?
        map.put(HOTEL_DETAILS, TravelProviderUtilHotel.initHotelDetailsUriMatcherInfo());
        map.put(HOTEL_DETAIL_ID, TravelProviderUtilHotel.initHotelDetailUriMatcherInfo());

        map.put(HOTEL_IMAGE_PAIRS, TravelProviderUtilHotel.initHotelImagePairsUriMatcherInfo());
        map.put(HOTEL_IMAGE_PAIR_ID, TravelProviderUtilHotel.initHotelImagePairUriMatcherInfo());

        map.put(HOTEL_RATE_DETAILS, TravelProviderUtilHotel.initHotelRateDetailsUriMatcherInfo());
        map.put(HOTEL_RATE_DETAIL_ID, TravelProviderUtilHotel.initHotelRateDetailUriMatcherInfo());

    }

}
