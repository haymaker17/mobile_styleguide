package com.concur.mobile.platform.travel.provider;

import com.concur.mobile.platform.provider.UriMatcherInfo;

import java.util.HashMap;

/**
 * This class provides utility methods used by the Travel Provider class specific to Hotel.
 * 
 * @author RatanK
 * 
 */
public class TravelProviderUtilHotel {

    /**
     * Will initialize the Hotel Search Result projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initHotelSearchResultProjectionMap() {
        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.HotelSearchResultColumns._ID, Travel.HotelSearchResultColumns._ID);

        return retVal;
    }

    /**
     * Will initialize the Hotel Detail projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initHotelDetailProjectionMap() {
        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.HotelDetailColumns._ID, Travel.HotelDetailColumns._ID);
        retVal.put(Travel.HotelDetailColumns._COUNT, Travel.HotelDetailColumns._COUNT);

        // hotel name
        retVal.put(Travel.HotelDetailColumns.NAME, Travel.HotelDetailColumns.NAME);
        retVal.put(Travel.HotelDetailColumns.CHAIN_NAME, Travel.HotelDetailColumns.CHAIN_NAME);
        retVal.put(Travel.HotelDetailColumns.CHAIN_CODE, Travel.HotelDetailColumns.CHAIN_CODE);

        // hotel thumb nail image
        retVal.put(Travel.HotelDetailColumns.THUMBNAIL_URL, Travel.HotelDetailColumns.THUMBNAIL_URL);

        // hotel contact address
        retVal.put(Travel.HotelDetailColumns.ADDRESS_LINE_1, Travel.HotelDetailColumns.ADDRESS_LINE_1);
        retVal.put(Travel.HotelDetailColumns.STREET, Travel.HotelDetailColumns.STREET);
        retVal.put(Travel.HotelDetailColumns.CITY, Travel.HotelDetailColumns.CITY);
        retVal.put(Travel.HotelDetailColumns.STATE, Travel.HotelDetailColumns.STATE);
        retVal.put(Travel.HotelDetailColumns.COUNTRY, Travel.HotelDetailColumns.COUNTRY);
        retVal.put(Travel.HotelDetailColumns.COUNTRY_CODE, Travel.HotelDetailColumns.COUNTRY_CODE);
        retVal.put(Travel.HotelDetailColumns.PHONE, Travel.HotelDetailColumns.PHONE);
        retVal.put(Travel.HotelDetailColumns.TOLL_FREE_PHONE, Travel.HotelDetailColumns.TOLL_FREE_PHONE);
        retVal.put(Travel.HotelDetailColumns.ZIP, Travel.HotelDetailColumns.ZIP);
        retVal.put(Travel.HotelDetailColumns.LAT, Travel.HotelDetailColumns.LAT);
        retVal.put(Travel.HotelDetailColumns.LON, Travel.HotelDetailColumns.LON);

        // hotel distance
        retVal.put(Travel.HotelDetailColumns.DISTANCE, Travel.HotelDetailColumns.DISTANCE);
        retVal.put(Travel.HotelDetailColumns.DISTANCE_UNIT, Travel.HotelDetailColumns.DISTANCE_UNIT);

        // hotel rate
        retVal.put(Travel.HotelDetailColumns.RATES_URL, Travel.HotelDetailColumns.RATES_URL);
        retVal.put(Travel.HotelDetailColumns.LOWEST_RATE, Travel.HotelDetailColumns.LOWEST_RATE);
        retVal.put(Travel.HotelDetailColumns.CURRENCY_CODE, Travel.HotelDetailColumns.CURRENCY_CODE);
        retVal.put(Travel.HotelDetailColumns.PRICE_TO_BEAT, Travel.HotelDetailColumns.PRICE_TO_BEAT);
        retVal.put(Travel.HotelDetailColumns.TRAVEL_POINTS_FOR_LOWEST_RATE, Travel.HotelDetailColumns.TRAVEL_POINTS_FOR_LOWEST_RATE);

        // hotel preference and suggestion
        retVal.put(Travel.HotelDetailColumns.SUGESTED_CATEGORY, Travel.HotelDetailColumns.SUGESTED_CATEGORY);
        retVal.put(Travel.HotelDetailColumns.SUGESTED_SCORE, Travel.HotelDetailColumns.SUGESTED_SCORE);
        retVal.put(Travel.HotelDetailColumns.STAR_RATING, Travel.HotelDetailColumns.STAR_RATING);
        retVal.put(Travel.HotelDetailColumns.COMPANY_PREFERENCE, Travel.HotelDetailColumns.COMPANY_PREFERENCE);

        // hotel availability
        retVal.put(Travel.HotelDetailColumns.AVAILABILITY_ERROR_CODE, Travel.HotelDetailColumns.AVAILABILITY_ERROR_CODE);
        retVal.put(Travel.HotelDetailColumns.HOTEL_SEARCH_RESULT_ID, Travel.HotelDetailColumns.HOTEL_SEARCH_RESULT_ID);

        return retVal;
    }

    /**
     * Will initialize the Hotel Image Pair projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initHotelImagePairProjectionMap() {
        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.HotelImagePairColumns._ID, Travel.HotelImagePairColumns._ID);
        retVal.put(Travel.HotelImagePairColumns._COUNT, Travel.HotelImagePairColumns._COUNT);

        retVal.put(Travel.HotelImagePairColumns.THUMBNAIL_URL, Travel.HotelImagePairColumns.THUMBNAIL_URL);
        retVal.put(Travel.HotelImagePairColumns.IMAGE_URL, Travel.HotelImagePairColumns.IMAGE_URL);

        return retVal;
    }

    /**
     * Will initialize the Hotel Rate Detail projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initHotelRateDetailProjectionMap() {
        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.HotelRateDetailColumns._ID, Travel.HotelImagePairColumns._ID);
        retVal.put(Travel.HotelRateDetailColumns._COUNT, Travel.HotelImagePairColumns._COUNT);

        retVal.put(Travel.HotelRateDetailColumns.RATE_ID, Travel.HotelRateDetailColumns.RATE_ID);
        retVal.put(Travel.HotelRateDetailColumns.AMOUNT, Travel.HotelRateDetailColumns.AMOUNT);
        retVal.put(Travel.HotelRateDetailColumns.CURRENCY_CODE, Travel.HotelRateDetailColumns.CURRENCY_CODE);
        retVal.put(Travel.HotelRateDetailColumns.SOURCE, Travel.HotelRateDetailColumns.SOURCE);
        retVal.put(Travel.HotelRateDetailColumns.ROOM_TYPE, Travel.HotelRateDetailColumns.ROOM_TYPE);
        retVal.put(Travel.HotelRateDetailColumns.DESCRIPTION, Travel.HotelRateDetailColumns.DESCRIPTION);
        retVal.put(Travel.HotelRateDetailColumns.ESTIMATED_BED_TYPE, Travel.HotelRateDetailColumns.ESTIMATED_BED_TYPE);
        retVal.put(Travel.HotelRateDetailColumns.GUARANTEE_SURCHARGE, Travel.HotelRateDetailColumns.GUARANTEE_SURCHARGE);
        retVal.put(Travel.HotelRateDetailColumns.RATE_CHANGES_OVERSTAY,
                Travel.HotelRateDetailColumns.RATE_CHANGES_OVERSTAY);
        retVal.put(Travel.HotelRateDetailColumns.MAX_ENF_LEVEL, Travel.HotelRateDetailColumns.MAX_ENF_LEVEL);
        retVal.put(Travel.HotelRateDetailColumns.SELL_OPTIONS_URL, Travel.HotelRateDetailColumns.SELL_OPTIONS_URL);
        retVal.put(Travel.HotelRateDetailColumns.VIOLATION_VALUE_IDS, Travel.HotelRateDetailColumns.VIOLATION_VALUE_IDS);
        retVal.put(Travel.HotelRateDetailColumns.TRAVEL_POINTS, Travel.HotelRateDetailColumns.TRAVEL_POINTS);
        retVal.put(Travel.HotelRateDetailColumns.CAN_REDEEM_TP_AGAINST_VIOLATIONS,
                Travel.HotelRateDetailColumns.CAN_REDEEM_TP_AGAINST_VIOLATIONS);
        retVal.put(Travel.HotelRateDetailColumns.HOTEL_DETAIL_ID, Travel.HotelRateDetailColumns.HOTEL_DETAIL_ID);

        return retVal;
    }

    /**
     * Will initialize the Hotel Violation projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initHotelViolationProjectionMap() {
        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.HotelViolationColumns._ID, Travel.HotelViolationColumns._ID);
        retVal.put(Travel.HotelViolationColumns.MESSAGE, Travel.HotelViolationColumns.MESSAGE);

        retVal.put(Travel.HotelViolationColumns.ENFORCEMENT_LEVEL, Travel.HotelViolationColumns.ENFORCEMENT_LEVEL);
        retVal.put(Travel.HotelViolationColumns.VIOLATION_VALUE_ID, Travel.HotelViolationColumns.VIOLATION_VALUE_ID);
        retVal.put(Travel.HotelViolationColumns.HOTEL_SEARCH_RESULT_ID,
                Travel.HotelViolationColumns.HOTEL_SEARCH_RESULT_ID);

        return retVal;
    }

    /**
     * Will initialize the Hotel Benchmark projection map.
     *
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initHotelBenchmarkProjectionMap() {
        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.HotelBenchmarkColumns._ID, Travel.HotelBenchmarkColumns._ID);
        retVal.put(Travel.HotelBenchmarkColumns.LOCATION_NAME, Travel.HotelBenchmarkColumns.LOCATION_NAME);

        retVal.put(Travel.HotelBenchmarkColumns.CRN_CODE, Travel.HotelBenchmarkColumns.CRN_CODE);
        retVal.put(Travel.HotelBenchmarkColumns.PRICE, Travel.HotelBenchmarkColumns.PRICE);
        retVal.put(Travel.HotelBenchmarkColumns.SUB_DIV_CODE, Travel.HotelBenchmarkColumns.SUB_DIV_CODE);
        retVal.put(Travel.HotelBenchmarkColumns.HOTEL_SEARCH_RESULT_ID,
                Travel.HotelBenchmarkColumns.HOTEL_SEARCH_RESULT_ID);

        return retVal;
    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>HotelSearchResult</code>.
     */
    public static UriMatcherInfo initHotelSearchResultUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.HotelSearchResultColumns.CONTENT_TYPE;
        info.tableName = Travel.HotelSearchResultColumns.TABLE_NAME;
        info.contentIdUriBase = Travel.HotelSearchResultColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.HotelSearchResultColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.hotelSearchResultProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Hotel</code>.
     */
    public static UriMatcherInfo initHotelDetailsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.HotelDetailColumns.CONTENT_TYPE;
        info.tableName = Travel.HotelDetailColumns.TABLE_NAME;
        info.contentIdUriBase = Travel.HotelDetailColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.HotelDetailColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.hotelDetailProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Hotel</code>.
     */
    public static UriMatcherInfo initHotelDetailUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.HotelDetailColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.HotelDetailColumns.TABLE_NAME;
        info.contentIdUriBase = Travel.HotelDetailColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.HotelDetailColumns._ID;
        info.projectionMap = TravelProvider.hotelDetailProjectionMap;
        info.defaultSortOrder = Travel.HotelDetailColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.HotelDetailColumns.HOTEL_DETAIL_ID_PATH_POSITION;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>HotelImagePair</code>.
     */
    public static UriMatcherInfo initHotelImagePairsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.HotelImagePairColumns.CONTENT_TYPE;
        info.tableName = Travel.HotelImagePairColumns.TABLE_NAME;
        info.contentIdUriBase = Travel.HotelImagePairColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.HotelImagePairColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.hotelImagePairProjectionMap;
        info.bulkInserter = new HotelImagePairBulkInserter();

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>HotelImagePair</code>.
     */
    public static UriMatcherInfo initHotelImagePairUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.HotelImagePairColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.HotelImagePairColumns.TABLE_NAME;
        info.contentIdUriBase = Travel.HotelImagePairColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.HotelImagePairColumns._ID;
        info.projectionMap = TravelProvider.hotelImagePairProjectionMap;
        info.defaultSortOrder = Travel.HotelImagePairColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.HotelImagePairColumns.HOTEL_IMAGE_PAIR_ID_PATH_POSITION;
        info.bulkInserter = new HotelImagePairBulkInserter();

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>HotelRate</code>.
     */
    public static UriMatcherInfo initHotelRateDetailsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.HotelRateDetailColumns.CONTENT_TYPE;
        info.tableName = Travel.HotelRateDetailColumns.TABLE_NAME;
        info.contentIdUriBase = Travel.HotelRateDetailColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.HotelRateDetailColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.hotelRateDetailProjectionMap;
        info.bulkInserter = new HotelRateBulkInserter();

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>HotelRate</code>.
     */
    public static UriMatcherInfo initHotelRateDetailUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.HotelRateDetailColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.HotelRateDetailColumns.TABLE_NAME;
        info.contentIdUriBase = Travel.HotelRateDetailColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.HotelRateDetailColumns._ID;
        info.projectionMap = TravelProvider.hotelRateDetailProjectionMap;
        info.defaultSortOrder = Travel.HotelRateDetailColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.HotelRateDetailColumns.HOTEL_RATE_DETAIL_ID_PATH_POSITION;
        info.bulkInserter = new HotelRateBulkInserter();

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>HotelViolation</code>.
     */
    public static UriMatcherInfo initHotelViolationsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.HotelViolationColumns.CONTENT_TYPE;
        info.tableName = Travel.HotelViolationColumns.TABLE_NAME;
        info.contentIdUriBase = Travel.HotelViolationColumns.CONTENT_ID_URI_BASE;
        info.projectionMap = TravelProvider.hotelViolationProjectionMap;
        info.bulkInserter = new HotelViolationBulkInserter();

        return info;
    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>HotelViolation</code>.
     */
    public static UriMatcherInfo initHotelViolationUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.HotelViolationColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.HotelViolationColumns.TABLE_NAME;
        info.contentIdUriBase = Travel.HotelViolationColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.HotelViolationColumns._ID;
        info.projectionMap = TravelProvider.hotelViolationProjectionMap;
        info.idPathPosition = Travel.HotelViolationColumns.HOTEL_VIOLATION_ID_PATH_POSITION;
        info.bulkInserter = new HotelViolationBulkInserter();

        return info;
    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>HotelBenchmark</code>.
     */
    public static UriMatcherInfo initHotelBenchmarksUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.HotelBenchmarkColumns.CONTENT_TYPE;
        info.tableName = Travel.HotelBenchmarkColumns.TABLE_NAME;
        info.contentIdUriBase = Travel.HotelBenchmarkColumns.CONTENT_ID_URI_BASE;
        info.projectionMap = TravelProvider.hotelBenchmarkProjectionMap;
        info.bulkInserter = new HotelBenchmarkBulkInserter();

        return info;
    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>HotelBenchmark</code>.
     */
    public static UriMatcherInfo initHotelBenchmarkUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.HotelBenchmarkColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.HotelBenchmarkColumns.TABLE_NAME;
        info.contentIdUriBase = Travel.HotelBenchmarkColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.HotelBenchmarkColumns._ID;
        info.projectionMap = TravelProvider.hotelBenchmarkProjectionMap;
        info.idPathPosition = Travel.HotelBenchmarkColumns.HOTEL_BENCHMARK_ID_PATH_POSITION;
        info.bulkInserter = new HotelBenchmarkBulkInserter();

        return info;
    }
}
