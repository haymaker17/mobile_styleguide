package com.concur.mobile.platform.travel.search.hotel;

import java.io.Serializable;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.concur.mobile.platform.travel.provider.Travel;
import com.concur.mobile.platform.util.CursorUtil;

/**
 * 
 * @author RatanK
 * 
 */
public class HotelRate implements Serializable {

    /**
     * generated
     */
    private static final long serialVersionUID = 8136223454047648920L;
    public String rateId;
    public Double amount;
    public String currency;
    public String source;
    public String roomType;
    public String description;
    public String estimatedBedType;
    public String guaranteeSurcharge;
    public boolean rateChangesOverStay;
    public int maxEnforcementLevel;
    public URLInfo sellOptions;
    public int[] violationValueIds;
    public Double travelPoints;
    public boolean canRedeemTravelPointsAgainstViolations;
    /**
     * Contains the content Uri.
     */
    private Uri contentUri;

    /**
     * Contains the application context.
     */
    protected transient Context context;

    // full column list of hotel rate detail.
    public static String[] fullColumnList = { Travel.HotelRateDetailColumns._ID, Travel.HotelRateDetailColumns.RATE_ID,
            Travel.HotelRateDetailColumns.AMOUNT, Travel.HotelRateDetailColumns.CURRENCY_CODE,
            Travel.HotelRateDetailColumns.SOURCE, Travel.HotelRateDetailColumns.ROOM_TYPE,
            Travel.HotelRateDetailColumns.DESCRIPTION, Travel.HotelRateDetailColumns.ESTIMATED_BED_TYPE,
            Travel.HotelRateDetailColumns.GUARANTEE_SURCHARGE, Travel.HotelRateDetailColumns.RATE_CHANGES_OVERSTAY,
            Travel.HotelRateDetailColumns.MAX_ENF_LEVEL, Travel.HotelRateDetailColumns.SELL_OPTIONS_URL,
            Travel.HotelRateDetailColumns.VIOLATION_VALUE_IDS, Travel.HotelRateDetailColumns.TRAVEL_POINTS,
            Travel.HotelRateDetailColumns.CAN_REDEEM_TP_AGAINST_VIOLATIONS };

    /**
     * Constructs a new instance of <code>HotelRate</code>.
     */
    public HotelRate() {
    }

    /**
     * Will construct an instance of <code>HotelRate</code> with an application context.
     * 
     * @param context
     *            contains a reference to an application context.
     */
    public HotelRate(Context context) {
        this.context = context;
    }

    /**
     * Constructs an instance of <code>HotelRate</code> based on reading values from a <code>Cursor</code> object.
     * 
     * @param cursor
     *            contains the cursor.
     */
    public HotelRate(Cursor cursor) {
        init(cursor);
    }

    /**
     * Constructs an instance of <code>HotelRate</code> based on reading values from a <code>Uri</code> object.
     * 
     * @param context
     *            contains an application context.
     * @param contentUri
     *            contains the content Uri.
     */
    public HotelRate(Context context, Uri contentUri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(contentUri, fullColumnList, null, null,
                    Travel.HotelRateDetailColumns.DEFAULT_SORT_ORDER);
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
     * Will initialize the hotel rate detail from a cursor object.
     * 
     * @param cursor
     *            contains the cursor used to initialize fields.
     */
    private void init(Cursor cursor) {
        rateId = CursorUtil.getStringValue(cursor, Travel.HotelRateDetailColumns.RATE_ID);
        amount = CursorUtil.getDoubleValue(cursor, Travel.HotelRateDetailColumns.AMOUNT);
        currency = CursorUtil.getStringValue(cursor, Travel.HotelRateDetailColumns.CURRENCY_CODE);
        source = CursorUtil.getStringValue(cursor, Travel.HotelRateDetailColumns.SOURCE);
        roomType = CursorUtil.getStringValue(cursor, Travel.HotelRateDetailColumns.ROOM_TYPE);
        description = CursorUtil.getStringValue(cursor, Travel.HotelRateDetailColumns.DESCRIPTION);
        estimatedBedType = CursorUtil.getStringValue(cursor, Travel.HotelRateDetailColumns.ESTIMATED_BED_TYPE);
        guaranteeSurcharge = CursorUtil.getStringValue(cursor, Travel.HotelRateDetailColumns.GUARANTEE_SURCHARGE);
        rateChangesOverStay = CursorUtil.getBooleanValue(cursor, Travel.HotelRateDetailColumns.RATE_CHANGES_OVERSTAY);
        maxEnforcementLevel = CursorUtil.getIntValue(cursor, Travel.HotelRateDetailColumns.MAX_ENF_LEVEL);
        travelPoints = CursorUtil.getDoubleValue(cursor, Travel.HotelRateDetailColumns.TRAVEL_POINTS);
        canRedeemTravelPointsAgainstViolations = CursorUtil.getBooleanValue(cursor,
                Travel.HotelRateDetailColumns.CAN_REDEEM_TP_AGAINST_VIOLATIONS);

        sellOptions = new URLInfo();
        sellOptions.href = CursorUtil.getStringValue(cursor, Travel.HotelRateDetailColumns.SELL_OPTIONS_URL);

        String violationValueIdString = CursorUtil.getStringValue(cursor,
                Travel.HotelRateDetailColumns.VIOLATION_VALUE_IDS);
        if (violationValueIdString != null && violationValueIdString.length() > 0) {
            String str[] = violationValueIdString.split(",");
            violationValueIds = new int[str.length];
            for (int i = 0; i < str.length; i++) {
                String value = str[i];
                if (value != null && value.length() > 0) {
                    violationValueIds[i] = Integer.parseInt(value);
                }
            }
        }
    }
}
