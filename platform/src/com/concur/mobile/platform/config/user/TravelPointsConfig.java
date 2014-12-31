package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a model of travel points config information.
 */
public class TravelPointsConfig extends BaseParser {

    private static final String CLS_TAG = "TravelPointsConfig";

    private static final String TAG_AIR_TRAVEL_POINTS_ENABLED = "AirTravelPointsEnabled";
    private static final String TAG_HOTEL_TRAVEL_POINTS_ENABLED = "HotelTravelPointsEnabled";

    private static final int TAG_AIR_TRAVEL_POINTS_ENABLED_CODE = 0;
    private static final int TAG_HOTEL_TRAVEL_POINTS_ENABLED_CODE = 1;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_AIR_TRAVEL_POINTS_ENABLED, TAG_AIR_TRAVEL_POINTS_ENABLED_CODE);
        tagMap.put(TAG_HOTEL_TRAVEL_POINTS_ENABLED, TAG_HOTEL_TRAVEL_POINTS_ENABLED_CODE);
    }

    /**
     * Contains whether air travel points is enabled.
     */
    public Boolean airTravelPointsEnabled;

    /**
     * Contains whether hotel travel points is enabled.
     */
    public Boolean hotelTravelPointsEnabled;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_AIR_TRAVEL_POINTS_ENABLED_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    airTravelPointsEnabled = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_HOTEL_TRAVEL_POINTS_ENABLED_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    hotelTravelPointsEnabled = Parse.safeParseBoolean(text.trim());
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
