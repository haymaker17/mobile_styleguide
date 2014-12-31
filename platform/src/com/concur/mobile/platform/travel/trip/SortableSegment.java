package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing a sortable segment object.
 */
public class SortableSegment extends BaseParser {

    private static final String CLS_TAG = "SortableSegment";

    // Tags.
    private static final String TAG_BOOKING_SOURCE = "BookingSource";
    private static final String TAG_RECORD_LOCATOR = "RecordLocator";
    private static final String TAG_SEGMENT_KEY = "SegmentKey";
    private static final String TAG_SEGMENT_SIDE = "SegmentSide";
    private static final String TAG_SORT_VALUE = "SortValue";

    // Tag codes.
    private static final int TAG_BOOKING_SOURCE_CODE = 0;
    private static final int TAG_RECORD_LOCATOR_CODE = 1;
    private static final int TAG_SEGMENT_KEY_CODE = 2;
    private static final int TAG_SEGMENT_SIDE_CODE = 3;
    private static final int TAG_SORT_VALUE_CODE = 4;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_BOOKING_SOURCE, TAG_BOOKING_SOURCE_CODE);
        tagMap.put(TAG_RECORD_LOCATOR, TAG_RECORD_LOCATOR_CODE);
        tagMap.put(TAG_SEGMENT_KEY, TAG_SEGMENT_KEY_CODE);
        tagMap.put(TAG_SEGMENT_SIDE, TAG_SEGMENT_SIDE_CODE);
        tagMap.put(TAG_SORT_VALUE, TAG_SORT_VALUE_CODE);
    }

    /**
     * Contains the booking source.
     */
    public String bookingSource;

    /**
     * Contains the record locator.
     */
    public String recordLocator;

    /**
     * Contains the segment key.
     */
    public String segmentKey;

    /**
     * Contains the segment side.
     */
    public String segmentSide;

    /**
     * Contains the sort value.
     */
    public String sortValue;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_BOOKING_SOURCE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    bookingSource = text.trim();
                }
                break;
            }
            case TAG_RECORD_LOCATOR_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    recordLocator = text.trim();
                }
                break;
            }
            case TAG_SEGMENT_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    segmentKey = text.trim();
                }
                break;
            }
            case TAG_SEGMENT_SIDE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    segmentSide = text.trim();
                }
                break;
            }
            case TAG_SORT_VALUE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    sortValue = text.trim();
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
