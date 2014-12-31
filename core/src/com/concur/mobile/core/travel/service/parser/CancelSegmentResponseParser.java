package com.concur.mobile.core.travel.service.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.core.travel.hotel.service.parser.HotelCancelAry;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Common parser for travel segment cancel requests (Hotel/Car/Rail/Air)
 * 
 * @author TejoA
 * 
 */
public class CancelSegmentResponseParser extends BaseParser {

    private static final String CLS_TAG = "CancelSegmentResponseParser";

    public static final String TAG_CANCEL_RESPONSE = "TravelCancelResponse";

    private static final String TAG_ENTIRE_TRIP_CANCELLED = "EntireTripCancelled";
    private static final int TAG_ENTIRE_TRIP_CANCELLED_CODE = 0;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;
    static {
        // Initialise the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_ENTIRE_TRIP_CANCELLED, TAG_ENTIRE_TRIP_CANCELLED_CODE);
    }
    /**
     * Contains the start tag used to register this parser.
     */
    private String startTag;

    /**
     * boolean flag to represent entire trip cancellation
     */
    public boolean isEntireTripCancelled;

    /**
     * Contains the hotel Cancel Array.
     */
    public List<HotelCancelAry> hotelCancelAry;

    /**
     * Contains the parser that parses a list of <code>HotelCancelAry</code> objects.
     */
    private ListParser<HotelCancelAry> hotelCancelAryListParser;

    /**
     * Constructs an instance of <code>CancelSegmentResponseParser</code> for parsing a UserConfig object.
     * 
     * @param parser
     *            contains a reference to a <code>CommonParser</code> object.
     * @param startTag
     *            contains the start tag used to register this parser.
     */
    public CancelSegmentResponseParser(CommonParser parser, String startTag) {

        // Set the start tag and register this parser.
        this.startTag = startTag;
        parser.registerParser(this, startTag);

        // Register the HotelCancelAry parser.
        hotelCancelAryListParser = new ListParser<HotelCancelAry>("HotelCancelAry", "Hotel", HotelCancelAry.class);
        parser.registerParser(hotelCancelAryListParser, "HotelCancelAry");

    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null && tagCode == TAG_ENTIRE_TRIP_CANCELLED_CODE) {
            if (!TextUtils.isEmpty(text)) {
                isEntireTripCancelled = Parse.safeParseBoolean(text);
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
                // Assemble data from the sub-parsers.
                hotelCancelAry = hotelCancelAryListParser.getList();
            }
        }
    }
}
