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
public class HotelImagePair implements Serializable {

    /**
     * generated
     */
    private static final long serialVersionUID = -5712574792648183706L;

    /**
     * Image URL
     */
    public String image;

    /**
     * Thumbnail URL
     */
    public String thumbnail;

    /**
     * Contains the content Uri.
     */
    private Uri contentUri;

    /**
     * Contains the application context.
     */
    protected transient Context context;

    // full column list of hotel image pair.
    public static String[] fullColumnList = { Travel.HotelImagePairColumns._ID, Travel.HotelImagePairColumns.IMAGE_URL,
            Travel.HotelImagePairColumns.THUMBNAIL_URL };

    /**
     * Constructs a new instance of <code>HotelImagePair</code>.
     */
    public HotelImagePair() {
    }

    /**
     * Will construct an instance of <code>HotelImagePair</code> with an application context.
     * 
     * @param context
     *            contains a reference to an application context.
     */
    public HotelImagePair(Context context) {
        this.context = context;
    }

    /**
     * Constructs an instance of <code>HotelImagePair</code> based on reading values from a <code>Cursor</code> object.
     * 
     * @param cursor
     *            contains the cursor.
     */
    public HotelImagePair(Cursor cursor) {
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
    public HotelImagePair(Context context, Uri contentUri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(contentUri, fullColumnList, null, null,
                    Travel.HotelImagePairColumns.DEFAULT_SORT_ORDER);
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
     * Will initialize the hotel image pair from a cursor object.
     * 
     * @param cursor
     *            contains the cursor used to initialize fields.
     */
    private void init(Cursor cursor) {
        image = CursorUtil.getStringValue(cursor, Travel.HotelImagePairColumns.IMAGE_URL);
        thumbnail = CursorUtil.getStringValue(cursor, Travel.HotelImagePairColumns.THUMBNAIL_URL);
    }
}
