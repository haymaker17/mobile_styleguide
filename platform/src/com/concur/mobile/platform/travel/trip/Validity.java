package com.concur.mobile.platform.travel.trip;

import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for parsing offer validity information.
 */
public class Validity extends BaseParser {

    public static final String CLS_TAG = "Validity";

    /**
     * Contains the list of locations.
     */
    public List<Location> locations;

    /**
     * Contains the list of time ranges.
     */
    public List<TimeRange> timeRanges;

    /**
     * Contains the location list parser.
     */
    private ListParser<Location> locationListParser;

    /**
     * Contains the timerange list parser.
     */
    private ListParser<TimeRange> timeRangeListParser;

    /**
     * Contains the start tag.
     */
    private String startTag;

    public Validity(CommonParser parser, String startTag) {
        this.startTag = startTag;

        // Create and register the locations parser.
        String listTag = "Locations";
        locationListParser = new ListParser<Location>(parser, listTag, "LocalProximity", Location.class);
        parser.registerParser(locationListParser, listTag);

        // Create and register the timerange list parser.
        listTag = "TimeRanges";
        timeRangeListParser = new ListParser<TimeRange>(parser, listTag, "TimeRange", TimeRange.class);
        parser.registerParser(timeRangeListParser, listTag);
    }

    @Override
    public void handleText(String tag, String text) {
        if (Const.DEBUG_PARSING) {
            Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
        }
    }

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {
                // Set the parsed locations list.
                locations = locationListParser.getList();

                // Set the parsed time range list.
                timeRanges = timeRangeListParser.getList();
            }
        }
    }

}
