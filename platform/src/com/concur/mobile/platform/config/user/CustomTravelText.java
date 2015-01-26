package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * Provides a model of custom travel text information.
 * 
 * @author RatanK
 * 
 */
public class CustomTravelText extends BaseParser {

    private static final String CLS_TAG = "CustomTravelText";

    private static final String TAG_AIR_RULES_VIOLATION_TEXT = "AirRulesViolationText";
    private static final String TAG_HOTEL_RULES_VIOLATION_TEXT = "HotelRulesViolationText";
    private static final String TAG_CAR_RULES_VIOLATION_TEXT = "CarRulesViolationText";

    private static final int TAG_AIR_RULES_VIOLATION_TEXT_CODE = 0;
    private static final int TAG_HOTEL_RULES_VIOLATION_TEXT_CODE = 1;
    private static final int TAG_CAR_RULES_VIOLATION_TEXT_CODE = 2;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_AIR_RULES_VIOLATION_TEXT, TAG_AIR_RULES_VIOLATION_TEXT_CODE);
        tagMap.put(TAG_HOTEL_RULES_VIOLATION_TEXT, TAG_HOTEL_RULES_VIOLATION_TEXT_CODE);
        tagMap.put(TAG_CAR_RULES_VIOLATION_TEXT, TAG_CAR_RULES_VIOLATION_TEXT_CODE);
    }

    public String airRulesViolationText;
    public String hotelRulesViolationText;
    public String carRulesViolationText;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_AIR_RULES_VIOLATION_TEXT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    airRulesViolationText = text.trim();
                }
                break;
            }
            case TAG_HOTEL_RULES_VIOLATION_TEXT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    hotelRulesViolationText = text.trim();
                }
                break;
            }
            case TAG_CAR_RULES_VIOLATION_TEXT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    carRulesViolationText = text.trim();
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
