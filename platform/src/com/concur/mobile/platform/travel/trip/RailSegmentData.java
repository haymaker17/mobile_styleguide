package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>SegmentData</code> for identifying rail specific segment information.
 */
public class RailSegmentData extends SegmentData {

    private static final String CLS_TAG = "RailSegmentData";

    private static final String TAG_AMENITIES = "Amenities";
    private static final String TAG_CABIN = "Cabin";
    private static final String TAG_CLASS_OF_SERVICE = "ClassOfService";
    private static final String TAG_DISCOUNT_CODE = "DiscountCode";
    private static final String TAG_DURATION = "Duration";
    private static final String TAG_END_PLATFORM = "EndPlatform";
    private static final String TAG_END_RAIL_STATION = "EndRailStation";
    private static final String TAG_END_RAIL_STATION_LOCALIZED = "EndRailStationLocalized";
    private static final String TAG_LEG_ID = "LegId";
    private static final String TAG_MEALS = "Meals";
    private static final String TAG_MILES = "Miles";
    private static final String TAG_NUM_STOPS = "NumStops";
    private static final String TAG_OPERATED_BY_TRAIN_NUMBER = "OperatedByTrainNumber";
    private static final String TAG_PIN = "Pin";
    private static final String TAG_START_PLATFORM = "StartPlatform";
    private static final String TAG_START_RAIL_STATION = "StartRailStation";
    private static final String TAG_START_RAIL_STATION_LOCALIZED = "StartRailStationLocalized";
    private static final String TAG_TRAIN_NUMBER = "TrainNumber";
    private static final String TAG_TRAIN_TYPE_CODE = "TrainTypeCode";
    private static final String TAG_WAGON_NUMBER = "WagonNumber";

    private static final int CODE_AMENITIES = 0;
    private static final int CODE_CABIN = 1;
    private static final int CODE_CLASS_OF_SERVICE = 2;
    private static final int CODE_DISCOUNT_CODE = 3;
    private static final int CODE_DURATION = 4;
    private static final int CODE_END_PLATFORM = 5;
    private static final int CODE_END_RAIL_STATION = 6;
    private static final int CODE_END_RAIL_STATION_LOCALIZED = 7;
    private static final int CODE_LEG_ID = 8;
    private static final int CODE_MEALS = 9;
    private static final int CODE_MILES = 10;
    private static final int CODE_NUM_STOPS = 11;
    private static final int CODE_OPERATED_BY_TRAIN_NUMBER = 12;
    private static final int CODE_PIN = 13;
    private static final int CODE_START_PLATFORM = 14;
    private static final int CODE_START_RAIL_STATION = 15;
    private static final int CODE_START_RAIL_STATION_LOCALIZED = 16;
    private static final int CODE_TRAIN_NUMBER = 17;
    private static final int CODE_TRAIN_TYPE_CODE = 18;
    private static final int CODE_WAGON_NUMBER = 19;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> rsdTagMap;

    static {
        // Initialize the map from tags to integer codes.
        rsdTagMap = new HashMap<String, Integer>();
        rsdTagMap.put(TAG_AMENITIES, CODE_AMENITIES);
        rsdTagMap.put(TAG_CABIN, CODE_CABIN);
        rsdTagMap.put(TAG_CLASS_OF_SERVICE, CODE_CLASS_OF_SERVICE);
        rsdTagMap.put(TAG_DISCOUNT_CODE, CODE_DISCOUNT_CODE);
        rsdTagMap.put(TAG_DURATION, CODE_DURATION);
        rsdTagMap.put(TAG_END_PLATFORM, CODE_END_PLATFORM);
        rsdTagMap.put(TAG_END_RAIL_STATION, CODE_END_RAIL_STATION);
        rsdTagMap.put(TAG_END_RAIL_STATION_LOCALIZED, CODE_END_RAIL_STATION_LOCALIZED);
        rsdTagMap.put(TAG_LEG_ID, CODE_LEG_ID);
        rsdTagMap.put(TAG_MEALS, CODE_MEALS);
        rsdTagMap.put(TAG_MILES, CODE_MILES);
        rsdTagMap.put(TAG_NUM_STOPS, CODE_NUM_STOPS);
        rsdTagMap.put(TAG_OPERATED_BY_TRAIN_NUMBER, CODE_OPERATED_BY_TRAIN_NUMBER);
        rsdTagMap.put(TAG_PIN, CODE_PIN);
        rsdTagMap.put(TAG_START_PLATFORM, CODE_START_PLATFORM);
        rsdTagMap.put(TAG_START_RAIL_STATION, CODE_START_RAIL_STATION);
        rsdTagMap.put(TAG_START_RAIL_STATION_LOCALIZED, CODE_START_RAIL_STATION_LOCALIZED);
        rsdTagMap.put(TAG_TRAIN_NUMBER, CODE_TRAIN_NUMBER);
        rsdTagMap.put(TAG_TRAIN_TYPE_CODE, CODE_TRAIN_TYPE_CODE);
        rsdTagMap.put(TAG_WAGON_NUMBER, CODE_WAGON_NUMBER);
    }

    public String amenities;
    public String cabin;
    public String classOfService;
    public String discountCode;
    public Integer duration;
    public String endPlatform;
    public String endRailStation;
    public String endRailStationLocalized;
    public Integer legId;
    public String meals;
    public Integer miles;
    public Integer numStops;
    public String operatedByTrainNumber;
    public String pin;
    public String startPlatform;
    public String startRailStation;
    public String startRailStationLocalized;
    public String trainNumber;
    public String trainTypeCode;
    public String wagonNumber;

    public RailSegmentData() {
        super(SegmentType.RAIL);
    }

    @Override
    public boolean handleSegmentText(String tag, String text) {
        boolean retVal = false;
        retVal = super.handleSegmentText(tag, text);
        if (!retVal) {
            Integer tagCode = rsdTagMap.get(tag);
            if (tagCode != null) {
                if (!TextUtils.isEmpty(text)) {
                    switch (tagCode) {
                    case CODE_AMENITIES: {
                        amenities = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_CABIN: {
                        cabin = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_CLASS_OF_SERVICE: {
                        classOfService = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_DISCOUNT_CODE: {
                        discountCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_DURATION: {
                        duration = Parse.safeParseInteger(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_END_PLATFORM: {
                        endPlatform = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_RAIL_STATION: {
                        endRailStation = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_RAIL_STATION_LOCALIZED: {
                        endRailStationLocalized = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_LEG_ID: {
                        legId = Parse.safeParseInteger(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_MEALS: {
                        meals = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_MILES: {
                        miles = Parse.safeParseInteger(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_NUM_STOPS: {
                        numStops = Parse.safeParseInteger(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_OPERATED_BY_TRAIN_NUMBER: {
                        operatedByTrainNumber = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_PIN: {
                        pin = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_PLATFORM: {
                        startPlatform = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_RAIL_STATION: {
                        startRailStation = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_RAIL_STATION_LOCALIZED: {
                        startRailStationLocalized = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_TRAIN_NUMBER: {
                        trainNumber = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_TRAIN_TYPE_CODE: {
                        trainTypeCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_WAGON_NUMBER: {
                        wagonNumber = text.trim();
                        retVal = true;
                        break;
                    }
                    default: {
                        if (Const.DEBUG_PARSING) {
                            Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: missing case statement for tag '" + tag + "'.");
                        }
                        break;
                    }
                    }
                }
            }
        }
        return retVal;
    }

}
