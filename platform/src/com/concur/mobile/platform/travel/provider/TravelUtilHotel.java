package com.concur.mobile.platform.travel.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.concur.mobile.platform.travel.search.hotel.Hotel;
import com.concur.mobile.platform.travel.search.hotel.HotelImagePair;
import com.concur.mobile.platform.travel.search.hotel.HotelRate;
import com.concur.mobile.platform.travel.search.hotel.HotelSearchRESTResult;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;

/**
 * Provides a set of travel provider utility methods specific to Hotel.
 * 
 * @author RatanK
 * 
 */
public class TravelUtilHotel {

    private static final String CLS_TAG = "TravelUtilHotel";

    private static final Boolean DEBUG = Boolean.TRUE;

    /**
     * Will insert hotel detail information into the <code>Travel.HotelDetailColumns.TABLE_NAME</code> table.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param hotels
     *            contains a list of <code>Hotel</code> objects.
     */
    public static void insertHotelDetails(ContentResolver resolver, HotelSearchRESTResult hotelSearchResult) {

        boolean hasImagePairs = false;

        // Set up the content values object.
        ContentValues values = new ContentValues();

        List<Hotel> hotels = hotelSearchResult != null ? hotelSearchResult.hotels : null;
        if (hotels != null && hotels.size() > 0) {
            for (Hotel hotel : hotels) {
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
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.ADDRESS_LINE_1, hotel.contact.addressLine1);
                    // Set the city.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.CITY, hotel.contact.city);
                    // Set the state.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.STATE, hotel.contact.state);
                    // Set the country.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.COUNTRY, hotel.contact.country);
                    // Set the country code.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.COUNTRY_CODE, hotel.contact.countryCode);
                    // Set the phone.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.PHONE, hotel.contact.phone);
                    // Set the toll free phone.
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.TOLL_FREE_PHONE, hotel.contact.tollFree);
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
                ContentUtils.putValue(values, Travel.HotelDetailColumns.DISTANCE_UNIT, hotelSearchResult.distanceUnit);
                // Set the lowest rate.
                ContentUtils.putValue(values, Travel.HotelDetailColumns.LOWEST_RATE, hotel.lowestRate);
                // Set the currency code.
                ContentUtils.putValue(values, Travel.HotelDetailColumns.CURRENCY_CODE, hotelSearchResult.currency);
                // Set the price to beat.
                ContentUtils.putValue(values, Travel.HotelDetailColumns.PRICE_TO_BEAT, hotel.priceToBeat);

                // Set the suggestion.
                if (hotel.recommended != null) {
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.SUGESTED_CATEGORY,
                            hotel.recommended.getSuggestedCategory());
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.SUGESTED_SCORE,
                            hotel.recommended.totalScore);
                }
                // Set the company preference.
                if (hotel.preferences != null) {
                    ContentUtils.putValue(values, Travel.HotelDetailColumns.STAR_RATING, hotel.preferences.starRating);
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
                    Log.d(Const.LOG_TAG, CLS_TAG + ".insertHotelDetails: new hotel detail uri '"
                            + ((hotelDetailInsertUri != null) ? hotelDetailInsertUri.toString() : "null"));
                }

                if (hotelDetailInsertUri != null) {

                    try {
                        int hotelDetailId = Integer.parseInt(hotelDetailInsertUri.getPathSegments().get(
                                Travel.HotelDetailColumns.HOTEL_DETAIL_ID_PATH_POSITION));

                        // retVal = insertId;

                        // Insert HotelImagePair
                        if (hasImagePairs) {
                            // TODO - use batch insert?
                            for (HotelImagePair imagePair : hotel.imagePairs) {
                                insertHotelImagePair(resolver, hotelDetailId, imagePair);
                            }
                        }

                        // Insert the HotelRate
                        if (hotel.rates != null && hotel.rates.size() > 0) {
                            // TODO - use batch insert?
                            for (HotelRate rateDetail : hotel.rates) {
                                insertHotelRateDetail(resolver, hotelDetailId, rateDetail);
                            }
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

    public static void insertHotelImagePair(ContentResolver resolver, int hotelDetailId, HotelImagePair imagePair) {
        if (imagePair != null) {
            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.HotelImagePairColumns.THUMBNAIL_URL, imagePair.thumbnail);
            ContentUtils.putValue(values, Travel.HotelImagePairColumns.IMAGE_URL, imagePair.image);

            // now, insert the values

            Uri hotelImagePairInsertUri = resolver.insert(Travel.HotelImagePairColumns.CONTENT_URI, values);

            // if (DEBUG) {
            // Log.d(Const.LOG_TAG, CLS_TAG + ".insertHotelImagePair: new hotel image pair uri '"
            // + ((hotelImagePairInsertUri != null) ? hotelImagePairInsertUri.toString() : "null"));
            // }

        }
    }

    public static void insertHotelRateDetail(ContentResolver resolver, int hotelDetailId, HotelRate rateDetail) {
        if (rateDetail != null) {
            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.HotelRateDetailColumns.RATE_ID, rateDetail.rateId);
            ContentUtils.putValue(values, Travel.HotelRateDetailColumns.AMOUNT, rateDetail.amount);
            ContentUtils.putValue(values, Travel.HotelRateDetailColumns.CURRENCY_CODE, rateDetail.currency);
            ContentUtils.putValue(values, Travel.HotelRateDetailColumns.SOURCE, rateDetail.source);
            ContentUtils.putValue(values, Travel.HotelRateDetailColumns.ROOM_TYPE, rateDetail.roomType);
            ContentUtils.putValue(values, Travel.HotelRateDetailColumns.DESCRIPTION, rateDetail.description);
            ContentUtils
                    .putValue(values, Travel.HotelRateDetailColumns.ESTIMATED_BED_TYPE, rateDetail.estimatedBedType);
            ContentUtils.putValue(values, Travel.HotelRateDetailColumns.GUARANTEE_SURCHARGE,
                    rateDetail.guaranteeSurcharge);
            ContentUtils.putValue(values, Travel.HotelRateDetailColumns.RATE_CHANGES_OVERSTAY,
                    rateDetail.rateChangesOverStay);
            ContentUtils.putValue(values, Travel.HotelRateDetailColumns.MAX_ENF_LEVEL, rateDetail.maxEnforcementLevel);

            if (rateDetail.sellOptions != null) {
                ContentUtils.putValue(values, Travel.HotelRateDetailColumns.SELL_OPTIONS_URL,
                        rateDetail.sellOptions.href);
            }

            // Set the rule violation ids - convert the int[] into a comma separated string
            if (rateDetail.violationValueIds != null && rateDetail.violationValueIds.length > 0) {
                StringBuffer violationIds = new StringBuffer(rateDetail.violationValueIds[0]);
                for (int i = 1; i < rateDetail.violationValueIds.length; i++) {
                    violationIds.append("," + rateDetail.violationValueIds[i]);
                }
                ContentUtils.putValue(values, Travel.HotelRateDetailColumns.VIOLATION_VALUE_IDS,
                        violationIds.toString());
            }

            ContentUtils.putValue(values, Travel.HotelRateDetailColumns.TRAVEL_POINTS, rateDetail.travelPoints);
            ContentUtils.putValue(values, Travel.HotelRateDetailColumns.CAN_REDEEM_TP_AGAINST_VIOLATIONS,
                    rateDetail.canRedeemTravelPointsAgainstViolations);

            // now, insert the values
            Uri hotelRateDetailInsertUri = resolver.insert(Travel.HotelRateDetailColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertHotelImagePair: new hotel image pair uri '"
                        + ((hotelRateDetailInsertUri != null) ? hotelRateDetailInsertUri.toString() : "null"));
            }

        }
    }

    /**
     * Will update a hotel detail within the travel provider.
     * 
     * @param context
     *            contains an application context.
     * @param hotel
     *            hotel detail to be updated
     */
    public static void updateHotelDetail(Context context, Hotel hotel) {
        // Punt any existing hotel detail object based on 'property id'.
        ContentResolver resolver = context.getContentResolver();

        // delete the existing hotel detail row where ratesURL == hotel.ratesURL.href

        // insert the hotel detail row by calling the existing insert method
    }

    public static void deleteAllHotelDetails(Context context) {
        ContentResolver resolver = context.getContentResolver();

        // delete all records
        int numOfRecordsDeleted = resolver.delete(Travel.HotelDetailColumns.CONTENT_URI, null, null);

        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".deleteAllHotelDetails: number of hotels deleted '" + numOfRecordsDeleted);
        }

    }

    public static List<Hotel> getHotels(Context context) {

        List<Hotel> hotels = new ArrayList<Hotel>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(Travel.HotelDetailColumns.CONTENT_URI, Hotel.fullColumnList, null, null,
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

        return hotels;
    }
}
