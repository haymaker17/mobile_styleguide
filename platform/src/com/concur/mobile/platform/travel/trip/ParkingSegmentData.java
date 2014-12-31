package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>SegmentData</code> for identifying parking specific segment information.
 */
public class ParkingSegmentData extends SegmentData {

    private static final String CLS_TAG = "ParkingSegmentData";

    private static final String TAG_PARKING_LOCATION_ID = "ParkingLocationId";
    private static final String TAG_PIN = "Pin";
    private static final String TAG_START_LOCATION = "StartLocation";

    private static final int CODE_PARKING_LOCATION_ID = 0;
    private static final int CODE_PIN = 1;
    private static final int CODE_START_LOCATION = 2;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> psdTagMap;

    static {
        // Initialize the map from tags to integer codes.
        psdTagMap = new HashMap<String, Integer>();
        psdTagMap.put(TAG_PARKING_LOCATION_ID, CODE_PARKING_LOCATION_ID);
        psdTagMap.put(TAG_PIN, CODE_PIN);
        psdTagMap.put(TAG_START_LOCATION, CODE_START_LOCATION);
    }

    public String parkingLocationId;
    public String pin;
    public String startLocation;

    public ParkingSegmentData() {
        super(SegmentType.PARKING);
    }

    @Override
    public boolean handleSegmentText(String tag, String text) {
        boolean retVal = false;
        retVal = super.handleSegmentText(tag, text);
        if (!retVal) {
            Integer tagCode = psdTagMap.get(tag);
            if (tagCode != null) {
                if (!TextUtils.isEmpty(text)) {
                    switch (tagCode) {
                    case CODE_PARKING_LOCATION_ID: {
                        parkingLocationId = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_PIN: {
                        pin = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_LOCATION: {
                        startLocation = text.trim();
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
