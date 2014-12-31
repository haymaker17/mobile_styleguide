package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>SegmentData</code> for identifying ride specific segment information.
 */
public class RideSegmentData extends SegmentData {

    private static final String CLS_TAG = "RideSegmentData";

    private static final String TAG_CANCELLATION_POLICY = "CancellationPolicy";
    private static final String TAG_DROP_OFF_INSTRUCTIONS = "DropoffInstructions";
    private static final String TAG_DURATION = "Duration";
    private static final String TAG_MEETING_INSTRUCTIONS = "MeetingInstructions";
    private static final String TAG_MILES = "Miles";
    private static final String TAG_NUMBER_OF_HOURS = "NumberOfHours";
    private static final String TAG_PICK_UP_INSTRUCTIONS = "PickupInstructions";
    private static final String TAG_RATE = "Rate";
    private static final String TAG_RATE_DESCRIPTION = "RateDescription";
    private static final String TAG_RATE_TYPE = "RateType";
    private static final String TAG_START_LOCATION = "StartLocation";
    private static final String TAG_START_LOCATION_CODE = "StartLocationCode";
    private static final String TAG_START_LOCATION_NAME = "StartLocationName";

    private static final int CODE_CANCELLATION_POLICY = 0;
    private static final int CODE_DROP_OFF_INSTRUCTIONS = 1;
    private static final int CODE_DURATION = 2;
    private static final int CODE_MEETING_INSTRUCTIONS = 3;
    private static final int CODE_MILES = 4;
    private static final int CODE_NUMBER_OF_HOURS = 5;
    private static final int CODE_PICK_UP_INSTRUCTIONS = 6;
    private static final int CODE_RATE = 7;
    private static final int CODE_RATE_DESCRIPTION = 8;
    private static final int CODE_RATE_TYPE = 9;
    private static final int CODE_START_LOCATION = 10;
    private static final int CODE_START_LOCATION_CODE = 11;
    private static final int CODE_START_LOCATION_NAME = 12;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> rsdTagMap;

    static {
        // Initialize the map from tags to integer codes.
        rsdTagMap = new HashMap<String, Integer>();
        rsdTagMap.put(TAG_CANCELLATION_POLICY, CODE_CANCELLATION_POLICY);
        rsdTagMap.put(TAG_DROP_OFF_INSTRUCTIONS, CODE_DROP_OFF_INSTRUCTIONS);
        rsdTagMap.put(TAG_DURATION, CODE_DURATION);
        rsdTagMap.put(TAG_MEETING_INSTRUCTIONS, CODE_MEETING_INSTRUCTIONS);
        rsdTagMap.put(TAG_MILES, CODE_MILES);
        rsdTagMap.put(TAG_NUMBER_OF_HOURS, CODE_NUMBER_OF_HOURS);
        rsdTagMap.put(TAG_PICK_UP_INSTRUCTIONS, CODE_PICK_UP_INSTRUCTIONS);
        rsdTagMap.put(TAG_RATE, CODE_RATE);
        rsdTagMap.put(TAG_RATE_DESCRIPTION, CODE_RATE_DESCRIPTION);
        rsdTagMap.put(TAG_RATE_TYPE, CODE_RATE_TYPE);
        rsdTagMap.put(TAG_START_LOCATION, CODE_START_LOCATION);
        rsdTagMap.put(TAG_START_LOCATION_CODE, CODE_START_LOCATION_CODE);
        rsdTagMap.put(TAG_START_LOCATION_NAME, CODE_START_LOCATION_NAME);
    }

    public String cancellationPolicy;
    public String dropoffInstructions;
    public Integer duration;
    public String meetingInstructions;
    public Integer miles;
    public Float numberOfHours;
    public String pickupInstructions;
    public Double rate;
    public String rateDescription;
    public String rateType;
    public String startLocation;
    public String startLocationCode;
    public String startLocationName;

    public RideSegmentData() {
        super(SegmentType.RIDE);
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
                    case CODE_CANCELLATION_POLICY: {
                        cancellationPolicy = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_DROP_OFF_INSTRUCTIONS: {
                        dropoffInstructions = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_DURATION: {
                        duration = Parse.safeParseInteger(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_MEETING_INSTRUCTIONS: {
                        meetingInstructions = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_MILES: {
                        miles = Parse.safeParseInteger(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_NUMBER_OF_HOURS: {
                        numberOfHours = Parse.safeParseFloat(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_PICK_UP_INSTRUCTIONS: {
                        pickupInstructions = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_RATE: {
                        rate = Parse.safeParseDouble(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_RATE_DESCRIPTION: {
                        rateDescription = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_RATE_TYPE: {
                        rateType = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_LOCATION: {
                        startLocation = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_LOCATION_CODE: {
                        startLocationCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_LOCATION_NAME: {
                        startLocationName = text.trim();
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
