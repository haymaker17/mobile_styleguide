package com.concur.mobile.platform.travel.search.hotel;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.concur.mobile.platform.travel.provider.Travel;
import com.concur.mobile.platform.travel.search.hotel.dao.HotelDAO;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author RatanK
 */
public class Hotel implements Serializable, HotelDAO {

    private static final String CLS_TAG = "Hotel";

    /**
     * generated
     */
    private static final long serialVersionUID = 2153015407138524370L;
    public List<HotelPropertyId> propertyIds;
    public String name;
    public Double longitude;
    public Double latitude;
    public Double distance;
    public String distanceUnit;
    public Double priceToBeat;
    public Double lowestRate;
    public Integer travelPointsForLowestRate;
    public int lowestEnforcementLevel;
    public String chainCode;
    public String chainName;
    public URLInfo ratesURL;
    public Contact contact;
    public HotelPreference preferences;
    public HotelRecommended recommended;
    public List<HotelImagePair> imagePairs;
    public List<HotelRate> rates;
    public String availabilityErrorCode;
    public String currencyCode;
    public boolean showNearMe;

    /**
     * Contains search url
     */
    public URLInfo searchURL;
    /**
     * Contains the content Uri.
     */
    private Uri contentUri;

    /**
     * Contains the application context.
     */
    protected transient Context context;

    /**
     * Contains db id
     */
    public transient long _id;

    /**
     * Contains search db id
     */
    public transient long search_id;

    // full column list of hotel detail.
    public static String[] fullColumnList = { Travel.HotelDetailColumns._ID, Travel.HotelDetailColumns.NAME,
            Travel.HotelDetailColumns.CHAIN_NAME, Travel.HotelDetailColumns.CHAIN_CODE,
            Travel.HotelDetailColumns.STREET, Travel.HotelDetailColumns.ADDRESS_LINE_1, Travel.HotelDetailColumns.STATE,
            Travel.HotelDetailColumns.COUNTRY, Travel.HotelDetailColumns.CITY, Travel.HotelDetailColumns.DISTANCE,
            Travel.HotelDetailColumns.PHONE, Travel.HotelDetailColumns.TOLL_FREE_PHONE,
            Travel.HotelDetailColumns.DISTANCE_UNIT, Travel.HotelDetailColumns.ZIP,
            Travel.HotelDetailColumns.LOWEST_RATE, Travel.HotelDetailColumns.CURRENCY_CODE,
            Travel.HotelDetailColumns.COMPANY_PREFERENCE, Travel.HotelDetailColumns.SUGESTED_CATEGORY,
            Travel.HotelDetailColumns.SUGESTED_SCORE, Travel.HotelDetailColumns.STAR_RATING,
            Travel.HotelDetailColumns.THUMBNAIL_URL, Travel.HotelDetailColumns.AVAILABILITY_ERROR_CODE,
            Travel.HotelDetailColumns.LAT, Travel.HotelDetailColumns.LON, Travel.HotelDetailColumns.RATES_URL,
            Travel.HotelDetailColumns.HOTEL_SEARCH_RESULT_ID, Travel.HotelDetailColumns.TRAVEL_POINTS_FOR_LOWEST_RATE, Travel.HotelDetailColumns.PRICE_TO_BEAT };

    // ,
    // Travel.HotelImagePairColumns.THUMBNAIL_URL, Travel.HotelImagePairColumns.IMAGE_URL,
    // Travel.HotelRateDetailColumns.RATE_ID, Travel.HotelRateDetailColumns.AMOUNT };Travel.HotelDetailColumns.LOWEST_ENF_LEVEL,

    /**
     * Constructs a new instance of <code>Hotel</code>.
     */
    public Hotel() {
    }

    /**
     * Will construct an instance of <code>Hotel</code> with an application context.
     *
     * @param context contains a reference to an application context.
     */
    public Hotel(Context context) {
        this.context = context;
    }

    /**
     * Constructs an instance of <code>Hotel</code> based on reading values from a <code>Cursor</code> object.
     *
     * @param cursor contains the cursor.
     */
    public Hotel(Cursor cursor) {
        init(cursor);
    }

    /**
     * Constructs an instance of <code>Hotel</code> based on reading values from a <code>Uri</code> object.
     *
     * @param context    contains an application context.
     * @param contentUri contains the content Uri.
     */
    public Hotel(Context context, Uri contentUri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver
                    .query(contentUri, fullColumnList, null, null, Travel.HotelDetailColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    init(cursor);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Will initialize the hotel from a cursor object.
     *
     * @param cursor contains the cursor used to initialize fields.
     */
    private void init(Cursor cursor) {

        search_id = CursorUtil.getLongValue(cursor, Travel.HotelDetailColumns.HOTEL_SEARCH_RESULT_ID);

        _id = CursorUtil.getLongValue(cursor, Travel.HotelDetailColumns._ID);

        name = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.NAME);

        chainName = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.CHAIN_NAME);
        chainCode = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.CHAIN_CODE);

        contact = new Contact();
        contact.city = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.CITY);
        contact.addressLine1 = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.ADDRESS_LINE_1);
        contact.street = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.STREET);
        contact.state = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.STATE);
        contact.country = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.COUNTRY);

        contact.phone = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.PHONE);
        contact.tollFree = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.TOLL_FREE_PHONE);
        contact.zip = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.ZIP);

        distance = CursorUtil.getDoubleValue(cursor, Travel.HotelDetailColumns.DISTANCE);
        distanceUnit = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.DISTANCE_UNIT);

        lowestRate = CursorUtil.getDoubleValue(cursor, Travel.HotelDetailColumns.LOWEST_RATE);
        priceToBeat = CursorUtil.getDoubleValue(cursor, Travel.HotelDetailColumns.PRICE_TO_BEAT);
        travelPointsForLowestRate = CursorUtil
                .getIntValue(cursor, Travel.HotelDetailColumns.TRAVEL_POINTS_FOR_LOWEST_RATE);
        currencyCode = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.CURRENCY_CODE);

        latitude = CursorUtil.getDoubleValue(cursor, Travel.HotelDetailColumns.LAT);
        longitude = CursorUtil.getDoubleValue(cursor, Travel.HotelDetailColumns.LON);

        // TODO - can we check if suggestion exists then create the object
        recommended = new HotelRecommended();
        recommended.category = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.SUGESTED_CATEGORY);
        if (recommended.category == null) {
            // we can nullify the object
            recommended = null;
        } else {
            recommended.totalScore = CursorUtil.getDoubleValue(cursor, Travel.HotelDetailColumns.SUGESTED_SCORE);
        }

        availabilityErrorCode = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.AVAILABILITY_ERROR_CODE);

        // TODO - can we check if preference exists then create the object
        preferences = new HotelPreference();
        preferences.starRating = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.STAR_RATING);
        preferences.companyPreference = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.COMPANY_PREFERENCE);

        // TODO - add other image pairs
        imagePairs = new ArrayList<HotelImagePair>();
        HotelImagePair imagePair = new HotelImagePair();
        imagePair.thumbnail = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.THUMBNAIL_URL);
        imagePairs.add(imagePair);

        // lowestEnforcementLevel = CursorUtil.getIntValue(cursor, Travel.HotelDetailColumns.LOWEST_ENF_LEVEL);
        ratesURL = new URLInfo();
        ratesURL.href = CursorUtil.getStringValue(cursor, Travel.HotelDetailColumns.RATES_URL);

    }

    public ArrayList<String> getPropertyIds() {
        if (propertyIds != null && propertyIds.size() > 0) {
            ArrayList<String> propertyIdsList = new ArrayList<String>(this.propertyIds.size());
            for (HotelPropertyId propertyId : this.propertyIds) {
                propertyIdsList.add(propertyId.propertyId);
            }
            return propertyIdsList;
        }
        return null;
    }

    @Override
    public Uri getContentURI(Context context) {
        // get the associated hotel based on the rates url which will be unique
        if (ratesURL != null && (!TextUtils.isEmpty(ratesURL.href))) {
            String[] columnNames = { Travel.HotelDetailColumns.RATES_URL };
            String[] columnValues = { ratesURL.href };
            contentUri = ContentUtils
                    .getContentUri(context, Travel.HotelDetailColumns.CONTENT_URI, columnNames, columnValues);

        }
        return contentUri;
    }

    @Override
    public boolean updateHotel(Context context) {
        boolean retVal = true;

        ContentResolver resolver = context.getContentResolver();

        // Build the content values object for insert/update.
        ContentValues values = new ContentValues();

        // TODO - perform an update on the provided Hotel that has the rates URL
        // ContentUtils.putValue(values, Expense.SmartExpenseColumns.SMART_EXPENSE_ID, smartExpenseId);

        contentUri = getContentURI(context);
        if (contentUri != null) {
            // Perform an update... chnage the below code.... to accept where clause for rates url
            int rowsUpdated = resolver.update(contentUri, values, null, null);
            if (rowsUpdated == 0) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".update: 0 rows updated for Uri '" + contentUri.toString() + "'.");
                // Perform an insertion.
                contentUri = resolver.insert(Travel.HotelDetailColumns.CONTENT_URI, values);
                retVal = (contentUri != null);
            } else {
                retVal = true;
                if (rowsUpdated > 1) {
                    Log.w(Const.LOG_TAG,
                            CLS_TAG + ".update: more than 1 row updated for Uri '" + contentUri.toString() + "'.");
                }
            }
            retVal = (rowsUpdated == 1);
        } else {
            // Perform an insertion.
            contentUri = resolver.insert(Travel.HotelDetailColumns.CONTENT_URI, values);
            retVal = (contentUri != null);
        }
        return retVal;
    }
}
