package com.concur.mobile.platform.travel.search.hotel;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.concur.mobile.platform.travel.provider.Travel;
import com.concur.mobile.platform.util.CursorUtil;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Hotel Price-to-Beat
 * <p/>
 * Created by RatanK
 */
public class HotelBenchmark implements Serializable {

    // full column list of hotel benchmark.
    public static String[] fullColumnList = { Travel.HotelBenchmarkColumns._ID,
            Travel.HotelBenchmarkColumns.LOCATION_NAME, Travel.HotelBenchmarkColumns.CRN_CODE,
            Travel.HotelBenchmarkColumns.PRICE, Travel.HotelBenchmarkColumns.SUB_DIV_CODE,
            Travel.HotelBenchmarkColumns.HOTEL_SEARCH_RESULT_ID };
    @SerializedName("currency")
    public String crnCode;
    @SerializedName("name")
    public String locationName;
    @SerializedName("price")
    public Double price;
    public String subDivCode;
    /**
     * Contains the application context.
     */
    protected transient Context context;
    /**
     * Contains the content Uri.
     */
    private transient Uri contentUri;

    /**
     * Constructs a new instance of <code>HotelBenchmark</code>.
     */
    public HotelBenchmark() {
    }

    /**
     * Will construct an instance of <code>HotelBenchmark</code> with an application context.
     *
     * @param context contains a reference to an application context.
     */
    public HotelBenchmark(Context context) {
        this.context = context;
    }

    /**
     * Constructs an instance of <code>HotelBenchmark</code> based on reading values from a <code>Cursor</code> object.
     *
     * @param cursor contains the cursor.
     */
    public HotelBenchmark(Cursor cursor) {
        init(cursor);
    }

    /**
     * Constructs an instance of <code>HotelBenchmark</code> based on reading values from a <code>Uri</code> object.
     *
     * @param context    contains an application context.
     * @param contentUri contains the content Uri.
     */
    public HotelBenchmark(Context context, Uri contentUri) {
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
     * Will initialize the hotel benchmark from a cursor object.
     *
     * @param cursor contains the cursor used to initialize fields.
     */
    private void init(Cursor cursor) {
        locationName = CursorUtil.getStringValue(cursor, Travel.HotelBenchmarkColumns.LOCATION_NAME);
        crnCode = CursorUtil.getStringValue(cursor, Travel.HotelBenchmarkColumns.CRN_CODE);
        price = CursorUtil.getDoubleValue(cursor, Travel.HotelBenchmarkColumns.PRICE);
        subDivCode = CursorUtil.getStringValue(cursor, Travel.HotelBenchmarkColumns.SUB_DIV_CODE);
    }

}
