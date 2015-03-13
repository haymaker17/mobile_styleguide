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
public class HotelViolation implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String enforcementLevel;
    public String message;
    public String violationValueId;

    /**
     * Contains the content Uri.
     */
    private transient Uri contentUri;

    /**
     * Contains the application context.
     */
    protected transient Context context;

    // full column list of hotel violation.
    public static String[] fullColumnList = { Travel.HotelViolationColumns._ID,
            Travel.HotelViolationColumns.ENFORCEMENT_LEVEL, Travel.HotelViolationColumns.MESSAGE,
            Travel.HotelViolationColumns.VIOLATION_VALUE_ID, Travel.HotelViolationColumns.HOTEL_SEARCH_RESULT_ID };

    /**
     * Constructs a new instance of <code>HotelViolation</code>.
     */
    public HotelViolation() {
    }

    /**
     * Will construct an instance of <code>HotelViolation</code> with an application context.
     * 
     * @param context
     *            contains a reference to an application context.
     */
    public HotelViolation(Context context) {
        this.context = context;
    }

    /**
     * Constructs an instance of <code>HotelViolation</code> based on reading values from a <code>Cursor</code> object.
     * 
     * @param cursor
     *            contains the cursor.
     */
    public HotelViolation(Cursor cursor) {
        init(cursor);
    }

    /**
     * Constructs an instance of <code>HotelImagePair</code> based on reading values from a <code>Uri</code> object.
     * 
     * @param context
     *            contains an application context.
     * @param contentUri
     *            contains the content Uri.
     */
    public HotelViolation(Context context, Uri contentUri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(contentUri, fullColumnList, null, null, null);
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
     * Will initialize the hotel violation from a cursor object.
     * 
     * @param cursor
     *            contains the cursor used to initialize fields.
     */
    private void init(Cursor cursor) {
        message = CursorUtil.getStringValue(cursor, Travel.HotelViolationColumns.MESSAGE);
        enforcementLevel = CursorUtil.getStringValue(cursor, Travel.HotelViolationColumns.ENFORCEMENT_LEVEL);
        violationValueId = CursorUtil.getStringValue(cursor, Travel.HotelViolationColumns.VIOLATION_VALUE_ID);
    }
}
