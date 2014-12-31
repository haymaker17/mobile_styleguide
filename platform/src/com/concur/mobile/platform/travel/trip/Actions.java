package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for parsing the set of allowable actions associated with an itinerary.
 */
public class Actions extends BaseParser {

    public static final String CLS_TAG = "Actions";

    // Tags.
    private static final String TAG_ALLOW_AIR = "AllowAddAir";
    private static final String TAG_ALLOW_CAR = "AllowAddCar";
    private static final String TAG_ALLOW_HOTEL = "AllowAddHotel";
    private static final String TAG_ALLOW_RAIL = "AllowAddRail";
    private static final String TAG_ALLOW_CANCEL = "AllowCancel";

    // Tag codes.
    private static final int TAG_ALLOW_AIR_CODE = 0;
    private static final int TAG_ALLOW_CAR_CODE = 1;
    private static final int TAG_ALLOW_HOTEL_CODE = 2;
    private static final int TAG_ALLOW_RAIL_CODE = 3;
    private static final int TAG_ALLOW_CANCEL_CODE = 4;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_ALLOW_AIR, TAG_ALLOW_AIR_CODE);
        tagMap.put(TAG_ALLOW_CAR, TAG_ALLOW_CAR_CODE);
        tagMap.put(TAG_ALLOW_HOTEL, TAG_ALLOW_HOTEL_CODE);
        tagMap.put(TAG_ALLOW_RAIL, TAG_ALLOW_RAIL_CODE);
        tagMap.put(TAG_ALLOW_CANCEL, TAG_ALLOW_CANCEL_CODE);
    }

    /**
     * Contains whether an air booking can be added.
     */
    public Boolean allowAddAir;

    /**
     * Contains whether a car booking can be added.
     */
    public Boolean allowAddCar;

    /**
     * Contains whether a hotel booking can be added.
     */
    public Boolean allowAddHotel;

    /**
     * Contains whether a rail booking can be added.
     */
    public Boolean allowAddRail;

    /**
     * Contains whether a segment can be canceled.
     */
    public Boolean allowCancel;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_ALLOW_AIR_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    allowAddAir = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_ALLOW_CAR_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    allowAddCar = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_ALLOW_HOTEL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    allowAddHotel = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_ALLOW_RAIL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    allowAddRail = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_ALLOW_CANCEL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    allowCancel = Parse.safeParseBoolean(text.trim());
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
