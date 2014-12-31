package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for parsing seat information.
 */
public class SeatData extends BaseParser {

    private static final String CLS_TAG = "SeatData";

    private static final String TAG_PASSENGER_RPH = "PassengerRph";
    private static final String TAG_SEAT_NUMBER = "SeatNumber";
    private static final String TAG_STATUS_CODE = "StatusCode";

    private static final int CODE_PASSENGER_RPH = 0;
    private static final int CODE_SEAT_NUMBER = 1;
    private static final int CODE_STATUS_CODE = 2;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_PASSENGER_RPH, CODE_PASSENGER_RPH);
        tagMap.put(TAG_SEAT_NUMBER, CODE_SEAT_NUMBER);
        tagMap.put(TAG_STATUS_CODE, CODE_STATUS_CODE);
    }

    public String passengerRPH;
    public String seatNumber;
    public String statusCode;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            if (!TextUtils.isEmpty(text)) {
                switch (tagCode) {
                case CODE_PASSENGER_RPH: {
                    passengerRPH = text.trim();
                    break;
                }
                case CODE_SEAT_NUMBER: {
                    seatNumber = text.trim();
                    break;
                }
                case CODE_STATUS_CODE: {
                    statusCode = text.trim();
                    break;
                }
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }

}
