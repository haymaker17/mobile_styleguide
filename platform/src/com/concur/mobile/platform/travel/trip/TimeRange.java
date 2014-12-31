package com.concur.mobile.platform.travel.trip;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purposes of parsing offer validity time ranges.
 */
public class TimeRange extends BaseParser {

    public static final String CLS_TAG = "TimeRange";

    // Tags.
    private static final String TAG_START_DATE_UTC = "startDateTimeUTC";
    private static final String TAG_END_DATE_UTC = "endDateTimeUTC";

    // Tag codes.
    private static final int TAG_START_DATE_UTC_CODE = 0;
    private static final int TAG_END_DATE_UTC_CODE = 1;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_START_DATE_UTC, TAG_START_DATE_UTC_CODE);
        tagMap.put(TAG_END_DATE_UTC, TAG_END_DATE_UTC_CODE);
    }

    /**
     * Contains the start date in UTC.
     */
    public Calendar startDateUtc;

    /**
     * Contains the end date in UTC.
     */
    public Calendar endDateUtc;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_START_DATE_UTC_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    startDateUtc = Parse.parseXMLTimestamp(text.trim());
                }
                break;
            }
            case TAG_END_DATE_UTC_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    endDateUtc = Parse.parseXMLTimestamp(text.trim());
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
