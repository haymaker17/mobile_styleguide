package com.concur.mobile.platform.travel.trip;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for parsing trip enhancement day objects.
 */
public class Day extends BaseParser {

    private static final String CLS_TAG = "Day";

    // Tags.
    private static final String TAG_DAY_TYPE = "DayType";
    private static final String TAG_TRIP_LOCAL_DATE = "TripLocalDate";

    // Tag codes.
    private static final int TAG_DAY_TYPE_CODE = 0;
    private static final int TAG_TRIP_LOCAL_DATE_CODE = 1;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_DAY_TYPE, TAG_DAY_TYPE_CODE);
        tagMap.put(TAG_TRIP_LOCAL_DATE, TAG_TRIP_LOCAL_DATE_CODE);
    }

    /**
     * Contains the day type.
     */
    public String dayType;

    /**
     * Contains the trip local date.
     */
    public Calendar tripLocalDate;

    /**
     * Contains the list of sortable segments for this day.
     */
    public List<SortableSegment> sortableSegments;

    /**
     * Contains the parser for a list of sortable segments.
     */
    private ListParser<SortableSegment> sortableSegmentListParser;

    /**
     * Contains the start tag for this parser.
     */
    private String startTag;

    /**
     * Constructs an instance of a day parser.
     * 
     * @param parser
     *            contains a reference to a common parser.
     * @param startTag
     *            contains the start tag.
     */
    public Day(CommonParser parser, String startTag) {
        this.startTag = startTag;

        String listTag = "SegmentLinks";
        sortableSegmentListParser = new ListParser<SortableSegment>(listTag, "SortableSegment", SortableSegment.class);
        parser.registerParser(sortableSegmentListParser, listTag);
    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_DAY_TYPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    dayType = text.trim();
                }
                break;
            }
            case TAG_TRIP_LOCAL_DATE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    tripLocalDate = Parse.parseTimestamp(text.trim(), Parse.XML_DF_LOCAL);
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

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {
                // Set sortable segments.
                sortableSegments = sortableSegmentListParser.getList();
            }
        }
    }

}
