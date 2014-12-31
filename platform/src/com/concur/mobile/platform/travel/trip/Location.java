package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purposes of parsing offer validity location information.
 */
public class Location extends BaseParser {

    public static final String CLS_TAG = "Location";

    private static final String TAG_LATITUDE = "Latitude";
    private static final String TAG_LONGITUDE = "Longitude";
    private static final String TAG_PROXIMITY = "Proximity";

    private static final int TAG_LATITUDE_CODE = 0;
    private static final int TAG_LONGITUDE_CODE = 1;
    private static final int TAG_PROXIMITY_CODE = 2;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_LATITUDE, TAG_LATITUDE_CODE);
        tagMap.put(TAG_LONGITUDE, TAG_LONGITUDE_CODE);
        tagMap.put(TAG_PROXIMITY, TAG_PROXIMITY_CODE);
    }

    /**
     * Contains the latitude.
     */
    public Double latitude;

    /**
     * Contains the longitude.
     */
    public Double longitude;

    /**
     * Contains the proximity.
     */
    public Double proximity;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_LATITUDE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    latitude = Parse.safeParseDouble(text.trim());
                    if (latitude == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse latitude.");
                    }
                }
                break;
            }
            case TAG_LONGITUDE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    longitude = Parse.safeParseDouble(text.trim());
                    if (longitude == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse longitude.");
                    }
                }
                break;
            }
            case TAG_PROXIMITY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    proximity = Parse.safeParseDouble(text.trim());
                    if (proximity == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse proximity.");
                    }
                }
                break;
            }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }

}
