package com.concur.mobile.platform.travel.provider;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.concur.mobile.platform.config.system.dao.ReasonCodeDAO;
import com.concur.mobile.platform.config.system.dao.SystemConfigDAO;
import com.concur.mobile.platform.travel.search.hotel.*;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides a set of travel provider utility methods specific to Hotel.
 *
 * @author RatanK
 */
public class TravelUtilHotel {

    private static final String CLS_TAG = "TravelUtilHotel";

    private static final Boolean DEBUG = Boolean.TRUE;

    /**
     * Will insert hotel detail information into the <code>Travel.HotelDetailColumns.TABLE_NAME</code> table.
     *
     * @param resolver          contains a reference to a content resolver.
     * @param hotelSearchResult contains a list of <code>HotelSearchRESTResult</code> objects.
     */
    @SuppressLint("SimpleDateFormat")
    public static void insertHotelDetails(ContentResolver resolver, HotelSearchRESTResult hotelSearchResult) {

        // Punt all Hotel Search Result information.
        // int rowsAffected = resolver.delete(Travel.HotelSearchResultColumns.CONTENT_URI, null, null);
        // if (DEBUG) {
        // Log.d(Const.LOG_TAG, CLS_TAG + ".insertHotelDetails: deleted " + Integer.toString(rowsAffected)
        // + " Hotel Search Result rows.");
        // }
        boolean hasImagePairs = false;

        // Set up the content values object.
        ContentValues values = new ContentValues();

        List<Hotel> hotels = hotelSearchResult != null ? hotelSearchResult.hotels : null;
        if (hotels != null && hotels.size() > 0) {

            ContentUtils
                    .putValue(values, Travel.HotelSearchResultColumns.DISTANCE_UNIT, hotelSearchResult.distanceUnit);
            ContentUtils.putValue(values, Travel.HotelSearchResultColumns.CURRENCY, hotelSearchResult.currency);
            ContentUtils
                    .putValue(values, Travel.HotelSearchResultColumns.SEARCH_CRITERIA_URL, hotelSearchResult.searchUrl);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ContentUtils.putValue(values, Travel.HotelSearchResultColumns.EXPIRY_DATETIME,
                    df.format(new Date().getTime() + 5 * 60 * 1000)); // 5 mins expiry time.

            // insert hotel search result
            Uri hotelSearchResultInsertUri = resolver.insert(Travel.HotelSearchResultColumns.CONTENT_URI, values);

            values.clear();

            if (hotelSearchResultInsertUri != null) {

                // get the hotel search result unique key
                int hotelSearchResultId = Integer.parseInt(hotelSearchResultInsertUri.getPathSegments()
                        .get(Travel.HotelSearchResultColumns.HOTEL_SEARCH_RESULT_ID_PATH_POSITION));

                // insert violations - TODO - can we span this in a new thread
                bulkInsertHotelViolations(resolver, hotelSearchResultId, hotelSearchResult.violations);

                for (Hotel hotel : hotels) {

                    // Set the foreign key
                    ContentUtils
                            .putValue(values, Travel.HotelDetailColumns.HOTEL_SEARCH_RESULT_ID, hotelSearchResultId);
                    // Set the name.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.NAME, hotel.name);
                    // Set the chain name.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.CHAIN_NAME, hotel.chainName);
                    // Set the chain code.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.CHAIN_CODE, hotel.chainCode);

                    if (hotel.contact != null) {
                        // Set the street.
                        ContentUtils.putValue(values, Travel.HotelDetailColumns.STREET, hotel.contact.street);
                        // Set the address line 1.
                        ContentUtils
                                .putValue(values, Travel.HotelDetailColumns.ADDRESS_LINE_1, hotel.contact.addressLine1);
                        // Set the city.
                        ContentUtils.putValue(values, Travel.HotelDetailColumns.CITY, hotel.contact.city);
                        // Set the state.
                        ContentUtils.putValue(values, Travel.HotelDetailColumns.STATE, hotel.contact.state);
                        // Set the country.
                        ContentUtils.putValue(values, Travel.HotelDetailColumns.COUNTRY, hotel.contact.country);
                        // Set the country code.
                        ContentUtils
                                .putValue(values, Travel.HotelDetailColumns.COUNTRY_CODE, hotel.contact.countryCode);
                        // Set the phone.
                        ContentUtils.putValue(values, Travel.HotelDetailColumns.PHONE, hotel.contact.phone);
                        // Set the toll free phone.
                        ContentUtils
                                .putValue(values, Travel.HotelDetailColumns.TOLL_FREE_PHONE, hotel.contact.tollFree);
                        // Set the zip.
                        ContentUtils.putValue(values, Travel.HotelDetailColumns.ZIP, hotel.contact.zip);
                    }
                    // Set the latitude.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.LAT, hotel.latitude);
                    // Set the longitude.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.LON, hotel.longitude);

                    // Set the distance.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.DISTANCE, hotel.distance);
                    // Set the distance.
                    ContentUtils
                            .putValue(values, Travel.HotelDetailColumns.DISTANCE_UNIT, hotelSearchResult.distanceUnit);
                    // Set the lowest rate.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.LOWEST_RATE, hotel.lowestRate);
                    // Set the currency code.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.CURRENCY_CODE, hotelSearchResult.currency);
                    // Set the price to beat.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.PRICE_TO_BEAT, hotel.priceToBeat);
                    // Set the travel points for the lowest rate.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.TRAVEL_POINTS_FOR_LOWEST_RATE,
                            hotel.travelPointsForLowestRate);

                    // Set the suggestion.
                    if (hotel.recommended != null) {
                        ContentUtils.putValue(values, Travel.HotelDetailColumns.SUGESTED_CATEGORY,
                                hotel.recommended.getSuggestedCategory());
                        ContentUtils.putValue(values, Travel.HotelDetailColumns.SUGESTED_SCORE,
                                hotel.recommended.totalScore);
                    }
                    // Set the company preference.
                    if (hotel.preferences != null) {
                        ContentUtils
                                .putValue(values, Travel.HotelDetailColumns.STAR_RATING, hotel.preferences.starRating);
                        ContentUtils.putValue(values, Travel.HotelDetailColumns.COMPANY_PREFERENCE,
                                hotel.preferences.companyPreference);
                    }

                    // Set the thumbnail image URL.
                    if (hotel.imagePairs != null && hotel.imagePairs.size() > 0) {
                        hasImagePairs = true;
                        ContentUtils.putValue(values, Travel.HotelDetailColumns.THUMBNAIL_URL,
                                hotel.imagePairs.get(0).thumbnail);
                    } else {
                        hasImagePairs = false;
                    }

                    // Set the availability error code.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.AVAILABILITY_ERROR_CODE,
                            hotel.availabilityErrorCode);

                    // Set the Rates URL.
                    if (hotel.ratesURL != null) {
                        ContentUtils.putValue(values, Travel.HotelDetailColumns.RATES_URL, hotel.ratesURL.href);
                    }

                    // now, insert the values
                    // TODO - use ContentProviderOperation for batch insert
                    Uri hotelDetailInsertUri = resolver.insert(Travel.HotelDetailColumns.CONTENT_URI, values);

                    if (DEBUG) {
                        Log.d(Const.LOG_TAG,
                                CLS_TAG + ".insertHotelDetails: new hotel detail uri '" + ((hotelDetailInsertUri
                                        != null) ? hotelDetailInsertUri.toString() : "null"));
                    }

                    if (hotelDetailInsertUri != null) {

                        try {
                            int hotelDetailId = Integer.parseInt(hotelDetailInsertUri.getPathSegments()
                                    .get(Travel.HotelDetailColumns.HOTEL_DETAIL_ID_PATH_POSITION));

                            // Insert the HotelRate - TODO - can we span this in a new thread
                            bulkInsertHotelRateDetail(resolver, hotelDetailId, hotel.rates);

                            // Insert HotelImagePairs
                            if (hasImagePairs) { // TODO - can we span this in a new thread
                                bulkInsertHotelImagePairs(resolver, hotelDetailId, hotel.imagePairs);
                            }
                        } catch (NumberFormatException nfe) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".insertHotelDetails: content id is not an integer!", nfe);
                        } catch (IndexOutOfBoundsException iobe) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".insertHotelDetails: id is not in content uri!", iobe);
                        }
                    } else {
                        if (DEBUG) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".insertHotelDetails: insertion uri is null.");
                        }
                    }

                    values.clear();
                }
            }
        }

    }

     /**
     * Bulk insert image pairs for the given hotelDetailId
     *
     * @param resolver
     * @param hotelDetailId
     * @param imagePairs
     */
    public static void bulkInsertHotelImagePairs(ContentResolver resolver, int hotelDetailId,
            List<HotelImagePair> imagePairs) {

        if (imagePairs != null && imagePairs.size() > 0) {
            ContentValues[] valuesInfo = new ContentValues[imagePairs.size()];
            int valInd = 0;

            for (HotelImagePair imagePair : imagePairs) {
                valuesInfo[valInd] = new ContentValues();

                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelImagePairColumns.HOTEL_DETAIL_ID, hotelDetailId);
                ContentUtils
                        .putValue(valuesInfo[valInd], Travel.HotelImagePairColumns.THUMBNAIL_URL, imagePair.thumbnail);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelImagePairColumns.IMAGE_URL, imagePair.image);

                ++valInd;
            }

            int numInserted = resolver.bulkInsert(Travel.HotelImagePairColumns.CONTENT_URI, valuesInfo);
            if (DEBUG) {
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".bulkInsertHotelImagePairs : number of image items inserted :  " + numInserted);
            }
        }
    }



    /**
     * Bulk insert hotel rates for the given hotelDetailId
     *
     * @param resolver
     * @param hotelDetailId
     * @param rates
     */
    public static void bulkInsertHotelRateDetail(ContentResolver resolver, int hotelDetailId, List<HotelRate> rates) {
        if (rates != null && rates.size() > 0) {
            ContentValues[] valuesInfo = new ContentValues[rates.size()];
            int valInd = 0;

            for (HotelRate rateDetail : rates) {

                valuesInfo[valInd] = new ContentValues();

                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.HOTEL_DETAIL_ID, hotelDetailId);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.RATE_ID, rateDetail.rateId);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.AMOUNT, rateDetail.amount);
                ContentUtils
                        .putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.CURRENCY_CODE, rateDetail.currency);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.SOURCE, rateDetail.source);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.ROOM_TYPE, rateDetail.roomType);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.DESCRIPTION,
                        rateDetail.description);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.ESTIMATED_BED_TYPE,
                        rateDetail.estimatedBedType);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.GUARANTEE_SURCHARGE,
                        rateDetail.guaranteeSurcharge);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.RATE_CHANGES_OVERSTAY,
                        rateDetail.rateChangesOverStay);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.MAX_ENF_LEVEL,
                        rateDetail.maxEnforcementLevel);

                if (rateDetail.sellOptions != null) {
                    ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.SELL_OPTIONS_URL,
                            rateDetail.sellOptions.href);
                }

                // Set the rule violation ids - convert the int[] into a comma separated string
                if (rateDetail.violationValueIds != null && rateDetail.violationValueIds.length > 0) {
                    StringBuffer violationIds = new StringBuffer(rateDetail.violationValueIds.length);
                    violationIds.append(rateDetail.violationValueIds[0]);
                    for (int i = 1; i < rateDetail.violationValueIds.length; i++) {
                        violationIds.append("," + rateDetail.violationValueIds[i]);
                    }
                    ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.VIOLATION_VALUE_IDS,
                            violationIds.toString());

                }
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.TRAVEL_POINTS,
                        rateDetail.travelPoints);
                ContentUtils
                        .putValue(valuesInfo[valInd], Travel.HotelRateDetailColumns.CAN_REDEEM_TP_AGAINST_VIOLATIONS,
                                rateDetail.canRedeemTravelPointsAgainstViolations);

                ++valInd;
            }
            // now, insert the values

            int numInserted = resolver.bulkInsert(Travel.HotelRateDetailColumns.CONTENT_URI, valuesInfo);

            if (DEBUG) {

                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".bulkInsertHotelRateDetail : number of rate items inserted :  " + numInserted);
            }
        }
    }

    public static void insertHotelViolations(ContentResolver resolver, int hotelSearchResultId,
            List<HotelViolation> violations, boolean queryFlag) {

        // insert hotel violations with hotel search result id as foreign key
        if (violations != null && violations.size() > 0) {
            ContentValues values = new ContentValues();

            for (HotelViolation hotelViolation : violations) {
                // Set the foreign key
                ContentUtils.putValue(values, Travel.HotelViolationColumns.HOTEL_SEARCH_RESULT_ID, hotelSearchResultId);

                // Set the columns
                ContentUtils.putValue(values, Travel.HotelViolationColumns.ENFORCEMENT_LEVEL,
                        hotelViolation.enforcementLevel);
                ContentUtils.putValue(values, Travel.HotelViolationColumns.MESSAGE, hotelViolation.message);
                ContentUtils.putValue(values, Travel.HotelViolationColumns.VIOLATION_VALUE_ID,
                        hotelViolation.violationValueId);

                if (queryFlag) {
                    // only insert if not exists
                    Cursor cursor = null;
                    try {
                        StringBuilder strBldr = new StringBuilder();
                        strBldr.append(Travel.HotelViolationColumns.VIOLATION_VALUE_ID);
                        strBldr.append(" = " + hotelViolation.violationValueId + " and ");
                        strBldr.append(Travel.HotelViolationColumns.HOTEL_SEARCH_RESULT_ID);
                        strBldr.append(" = " + hotelSearchResultId);
                        String where = strBldr.toString();

                        cursor = resolver
                                .query(Travel.HotelViolationColumns.CONTENT_URI, HotelViolation.fullColumnList, where,
                                        null, null);
                        if (cursor == null || !cursor.moveToFirst()) {
                            {
                                resolver.insert(Travel.HotelViolationColumns.CONTENT_URI, values);
                            }
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }

                } else {

                    // insert
                    resolver.insert(Travel.HotelViolationColumns.CONTENT_URI, values);
                }
            }
        }
    }

    /**
     * Bulk insert violations for the given hotelSearchResultId
     *
     * @param resolver
     * @param hotelSearchResultId
     * @param violations
     */
    public static void bulkInsertHotelViolations(ContentResolver resolver, int hotelSearchResultId,
            List<HotelViolation> violations) {
        // insert hotel violations with hotel search result id as foreign key
        if (violations != null && violations.size() > 0) {
            ContentValues[] valuesInfo = new ContentValues[violations.size()];
            int valInd = 0;
            for (HotelViolation hotelViolation : violations) {

                valuesInfo[valInd] = new ContentValues();

                // Set the foreign key
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelViolationColumns.HOTEL_SEARCH_RESULT_ID,
                        hotelSearchResultId);

                // Set the columns
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelViolationColumns.ENFORCEMENT_LEVEL,
                        hotelViolation.enforcementLevel);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelViolationColumns.MESSAGE, hotelViolation.message);
                ContentUtils.putValue(valuesInfo[valInd], Travel.HotelViolationColumns.VIOLATION_VALUE_ID,
                        hotelViolation.violationValueId);
            }
            // insert
            int numInserted = resolver.bulkInsert(Travel.HotelViolationColumns.CONTENT_URI, valuesInfo);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".bulkInsertHotelViolations : number of violation items inserted :  "
                        + numInserted);
            }
        }
    }


    /**
     * Delete the hotels search result
     *
     * @param context
     */
    public static void deleteAllHotelDetails(Context context) {
        ContentResolver resolver = context.getContentResolver();

        // delete all records
        int numOfRecordsDeleted = resolver.delete(Travel.HotelSearchResultColumns.CONTENT_URI, null, null);

        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".deleteAllHotelDetails: number of hotels deleted '" + numOfRecordsDeleted);
        }

    }

    /**
     * Delete the hotels search result
     *
     * @param context
     */
    public static void deleteHotelDetails(Context context) {
        ContentResolver resolver = context.getContentResolver();

        StringBuilder strBldr = new StringBuilder();
        strBldr.append(Travel.HotelSearchResultColumns.EXPIRY_DATETIME + "<  datetime('now') OR ");

        strBldr.append(Travel.HotelSearchResultColumns.INSERT_DATETIME + ">  datetime('now') ;");

        String whereClause = strBldr.toString();

        // delete all records
        int numOfRecordsDeleted = resolver.delete(Travel.HotelSearchResultColumns.CONTENT_URI, whereClause, null);

        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".deleteHotelDetails: number of hotels deleted '" + numOfRecordsDeleted);
        }

    }



    /**
     * Get persisted hotels search result list by cache key
     *
     * @param context
     * @return
     */
    public static List<Hotel> getHotels(Context context, String searchUrl) {

        List<Hotel> hotels = new ArrayList<Hotel>();

        deleteHotelDetails(context);
        String id = getHotelSearchResultId(context, searchUrl);

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;

        if (id != null) {
            try {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Travel.HotelDetailColumns.HOTEL_SEARCH_RESULT_ID + " =? ");
                String where = strBldr.toString();
                String[] whereArgs = { id };
                cursor = resolver.query(Travel.HotelDetailColumns.CONTENT_URI, Hotel.fullColumnList, where, whereArgs,
                        Travel.HotelDetailColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            hotels.add(new Hotel(cursor));
                        } while (cursor.moveToNext());
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return hotels;
    }

    /**
     * @param context
     * @param searchUrl
     * @return
     */
    public static String getHotelSearchResultId(Context context, String searchUrl) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        String id = null;
        try {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Travel.HotelSearchResultColumns.SEARCH_CRITERIA_URL);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { searchUrl };
            cursor = resolver
                    .query(Travel.HotelSearchResultColumns.CONTENT_URI, HotelSearchRESTResult.fullColumnList, where,
                            whereArgs, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    id = cursor.getString(0);
                }
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return id;
    }

    /**
     * Get persisted hotels search result list
     *
     * @param context
     * @return
     */
    public static Hotel getHotelByRateUrl(Context context, String rateUrl, String searchUrl) {

        Hotel hotel = null;
        String id = getHotelSearchResultId(context, searchUrl);
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        if (id != null) {
            try {
                StringBuilder strBldr = new StringBuilder();

                strBldr.append(Travel.HotelDetailColumns.HOTEL_SEARCH_RESULT_ID + " = " + id + " AND ");
                strBldr.append(Travel.HotelDetailColumns.RATES_URL);
                strBldr.append(" = ? ");
                String where = strBldr.toString();
                String[] whereArgs = { rateUrl };
                cursor = resolver.query(Travel.HotelDetailColumns.CONTENT_URI, Hotel.fullColumnList, where, whereArgs,
                        Travel.HotelDetailColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        hotel = (new Hotel(cursor));
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        }
        return hotel;
    }

    /**
     * Get persisted images of the specific hotel
     *
     * @param context
     * @param id
     * @return
     */
    public static List<HotelImagePair> getHotelImagePairs(Context context, long id) {

        List<HotelImagePair> hotelImagePairs = new ArrayList<HotelImagePair>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Travel.HotelImagePairColumns.HOTEL_DETAIL_ID);
            strBldr.append(" = " + id);
            String where = strBldr.toString();
            cursor = resolver
                    .query(Travel.HotelImagePairColumns.CONTENT_URI, HotelImagePair.fullColumnList, where, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        hotelImagePairs.add(new HotelImagePair(cursor));
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return hotelImagePairs;
    }

    /**
     * Get hotel violations for the passed in violation ids and hotel search id
     *
     * @param context
     * @param violationValueIds
     * @return
     */
    public static List<HotelViolation> getHotelViolations(Context context, String[] violationValueIds, int search_id) {
        List<HotelViolation> hotelViolations = new ArrayList<HotelViolation>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Travel.HotelViolationColumns.HOTEL_SEARCH_RESULT_ID);
            strBldr.append(" = " + search_id);
            if (violationValueIds != null) {
                strBldr.append(" and " + Travel.HotelViolationColumns.VIOLATION_VALUE_ID);
                strBldr.append(" = ?");
            }
            String where = strBldr.toString();

            String[] whereArgs = violationValueIds;

            cursor = resolver
                    .query(Travel.HotelViolationColumns.CONTENT_URI, HotelViolation.fullColumnList, where, whereArgs,
                            null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        hotelViolations.add(new HotelViolation(cursor));
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return hotelViolations;
    }



    /**
     * Gets the list of <code>HotelRate</code> objects specific to the Hotel.
     *
     * @param context
     * @param hotelDetailId - unique database id of the parent table Hotel
     * @return
     */
    public static List<HotelRate> getHotelRateDetails(Context context, long hotelDetailId) {
        List<HotelRate> hotelRates = new ArrayList<HotelRate>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Travel.HotelRateDetailColumns.HOTEL_DETAIL_ID);
            strBldr.append(" = " + hotelDetailId);
            String where = strBldr.toString();

            cursor = resolver
                    .query(Travel.HotelRateDetailColumns.CONTENT_URI, HotelRate.fullColumnList, where, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        hotelRates.add(new HotelRate(cursor));
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return hotelRates;
    }
}
