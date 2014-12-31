package com.concur.mobile.platform.travel.trip;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ItemParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

public class Itinerary extends BaseParser {

    public static final String CLS_TAG = "Itinerary";

    private static final String TAG_TRAVEL_POINTS = "TravelPoints";

    // Tags
    private static final String TAG_CLIENT_LOCATOR = "ClientLocator";
    private static final String TAG_CLIQBOOK_TRIP_ID = "CliqbookTripId";
    private static final String TAG_DESCRIPTION = "Description";
    private static final String TAG_END_DATE_LOCAL = "EndDateLocal";
    private static final String TAG_END_DATE_UTC = "EndDateUtc";
    private static final String TAG_ITIN_LOCATOR = "ItinLocator";
    private static final String TAG_RECORD_LOCATOR = "RecordLocator";
    private static final String TAG_START_DATE_LOCAL = "StartDateLocal";
    private static final String TAG_START_DATE_UTC = "StartDateUtc";
    private static final String TAG_STATE = "State";
    private static final String TAG_TRIP_NAME = "TripName";

    // Tag codes.
    private static final int TAG_CLIENT_LOCATOR_CODE = 0;
    private static final int TAG_CLIQBOOK_TRIP_ID_CODE = 1;
    private static final int TAG_DESCRIPTION_CODE = 2;
    private static final int TAG_END_DATE_LOCAL_CODE = 3;
    private static final int TAG_END_DATE_UTC_CODE = 4;
    private static final int TAG_ITIN_LOCATOR_CODE = 5;
    private static final int TAG_RECORD_LOCATOR_CODE = 6;
    private static final int TAG_START_DATE_LOCAL_CODE = 7;
    private static final int TAG_START_DATE_UTC_CODE = 8;
    private static final int TAG_STATE_CODE = 9;
    private static final int TAG_TRIP_NAME_CODE = 10;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_CLIENT_LOCATOR, TAG_CLIENT_LOCATOR_CODE);
        tagMap.put(TAG_CLIQBOOK_TRIP_ID, TAG_CLIQBOOK_TRIP_ID_CODE);
        tagMap.put(TAG_DESCRIPTION, TAG_DESCRIPTION_CODE);
        tagMap.put(TAG_END_DATE_LOCAL, TAG_END_DATE_LOCAL_CODE);
        tagMap.put(TAG_END_DATE_UTC, TAG_END_DATE_UTC_CODE);
        tagMap.put(TAG_ITIN_LOCATOR, TAG_ITIN_LOCATOR_CODE);
        tagMap.put(TAG_RECORD_LOCATOR, TAG_RECORD_LOCATOR_CODE);
        tagMap.put(TAG_START_DATE_LOCAL, TAG_START_DATE_LOCAL_CODE);
        tagMap.put(TAG_START_DATE_UTC, TAG_START_DATE_UTC_CODE);
        tagMap.put(TAG_STATE, TAG_STATE_CODE);
        tagMap.put(TAG_TRIP_NAME, TAG_TRIP_NAME_CODE);
    }

    // Public Attributes.

    /**
     * Contains the client locator.
     */
    public String clientLocator;

    /**
     * Contains the cliqbook trip id.
     */
    public Integer cliqBookTripId;

    /**
     * Contains the description.
     */
    public String description;

    /**
     * Contains the end-date in local time.
     */
    public Calendar endDateLocal;

    /**
     * Contains the end-date in UTC.
     */
    public Calendar endDateUtc;

    /**
     * Contains the itin locator.
     */
    public String itinLocator;

    /**
     * Contains the record locator.
     */
    public String recordLocator;

    /**
     * Contains the start date in local time.
     */
    public Calendar startDateLocal;

    /**
     * Contains the start date in UTC.
     */
    public Calendar startDateUtc;

    /**
     * Contains the state.
     */
    public Integer state;

    /**
     * Contains the trip name.
     */
    public String tripName;

    /**
     * Contains the trip enhancement object.
     */
    public Enhancements enhancements;

    /**
     * Contains the rule violations.
     */
    public RuleViolations ruleViolations;

    /**
     * Contains the allowable actions.
     */
    public Actions actions;

    /**
     * Contains the list of bookings.
     */
    public List<Booking> bookings;

    /**
     * Contains the travel points.
     */
    public TravelPoint travelPoint;

    /**
     * Contains whether or travel points information is being parsed.
     */
    private boolean inTravelPoint;

    /**
     * Contains the enhancements item parser.
     */
    private ItemParser<Enhancements> enhancementsParser;

    /**
     * Contains the rule violations item parser.
     */
    private ItemParser<RuleViolations> ruleViolationsParser;

    /**
     * Contains the actions item parser.
     */
    private ItemParser<Actions> actionsParser;

    /**
     * Contains the list of bookings.
     */
    private ListParser<Booking> bookingListParser;

    /**
     * Contains a reference to the start tag.
     */
    private String startTag;

    /**
     * Constructs an instance of <code>Itinerary</code> with a parser and start tag.
     * 
     * @param parser
     *            contains the parser.
     * @param startTag
     *            contains the start tag.
     */
    public Itinerary(CommonParser parser, String startTag) {

        // Set the start tag.
        this.startTag = startTag;

        // Create and register the actions parser.
        String itemTag = "Actions";
        actionsParser = new ItemParser<Actions>(itemTag, Actions.class);
        parser.registerParser(actionsParser, itemTag);

        // Create and register the enhancements parser.
        itemTag = "Enhancements";
        enhancementsParser = new ItemParser<Enhancements>(parser, itemTag, Enhancements.class);
        parser.registerParser(enhancementsParser, itemTag);

        // Create and register the rule violations parser.
        itemTag = "RuleViolations";
        ruleViolationsParser = new ItemParser<RuleViolations>(parser, itemTag, RuleViolations.class);
        parser.registerParser(ruleViolationsParser, itemTag);

        // Create and register the booking list parser.
        String listTag = "Bookings";
        itemTag = "Booking";
        bookingListParser = new ListParser<Booking>(parser, listTag, itemTag, Booking.class);
        parser.registerParser(bookingListParser, listTag);
    }

    @Override
    public void startTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_TRAVEL_POINTS)) {
                travelPoint = new TravelPoint();
            }
        }
    }

    @Override
    public void handleText(String tag, String text) {
        if (inTravelPoint) {
            travelPoint.handleText(tag, text);
        } else {
            Integer tagCode = tagMap.get(tag);
            if (tagCode != null) {
                switch (tagCode) {
                case TAG_CLIENT_LOCATOR_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        clientLocator = text.trim();
                    }
                    break;
                }
                case TAG_CLIQBOOK_TRIP_ID_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        cliqBookTripId = Parse.safeParseIntegerDefaultToZero(text.trim());
                    }
                    break;
                }
                case TAG_DESCRIPTION_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        description = text.trim();
                    }
                    break;
                }
                case TAG_END_DATE_LOCAL_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        endDateLocal = Parse.parseTimestamp(text.trim(), Parse.XML_DF_LOCAL);
                    }
                    break;
                }
                case TAG_END_DATE_UTC_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        endDateUtc = Parse.parseXMLTimestamp(text.trim());
                    }
                    break;
                }
                case TAG_ITIN_LOCATOR_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        itinLocator = text.trim();
                    }
                    break;
                }
                case TAG_RECORD_LOCATOR_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        recordLocator = text.trim();
                    }
                    break;
                }
                case TAG_START_DATE_LOCAL_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        startDateLocal = Parse.parseTimestamp(text.trim(), Parse.XML_DF_LOCAL);
                    }
                    break;
                }
                case TAG_START_DATE_UTC_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        startDateUtc = Parse.parseXMLTimestamp(text.trim());
                    }
                    break;
                }
                case TAG_STATE_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        state = Parse.safeParseIntegerDefaultToZero(text.trim());
                    }
                    break;
                }
                case TAG_TRIP_NAME_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        tripName = text.trim();
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

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {

                // Set the actions object.
                actions = actionsParser.getItem();

                // Set the rule violations.
                ruleViolations = ruleViolationsParser.getItem();

                // Set the enhancements object.
                enhancements = enhancementsParser.getItem();

                // Set the booking list.
                bookings = bookingListParser.getList();
            } else if (tag.equalsIgnoreCase(TAG_TRAVEL_POINTS)) {
                inTravelPoint = false;
            }
        }
    }

}
