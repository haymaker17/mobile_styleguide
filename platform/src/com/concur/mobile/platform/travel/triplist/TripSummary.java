package com.concur.mobile.platform.travel.triplist;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ItemParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purposes of parsing an <code>TripListItinerary</code> object.
 */
public class TripSummary extends BaseParser {

    private static final String CLS_TAG = "TripListItinerary";

    // Tags.
    private static final String TAG_APPROVAL_STATUS = "ApprovalStatus";
    private static final String TAG_APPROVER_ID = "ApproverId";
    private static final String TAG_APPROVER_NAME = "ApproverName";
    private static final String TAG_AUTHORIZATION_NUMBER = "AuthorizationNumber";
    private static final String TAG_BOOKED_VIA = "BookedVia";
    private static final String TAG_BOOKING_SOURCE = "BookingSource";
    private static final String TAG_CAN_BE_EXPENSED = "CanBeExpensed";
    private static final String TAG_CLIQBOOK_STATE = "CliqbookState";
    private static final String TAG_END_DATE_LOCAL = "EndDateLocal";
    private static final String TAG_END_DATE_UTC = "EndDateUtc";
    private static final String TAG_HAS_OTHERS = "HasOthers";
    private static final String TAG_HAS_TICKETS = "HasTickets";
    private static final String TAG_IS_EXPENSED = "IsExpensed";
    private static final String TAG_IS_GDS_BOOKING = "IsGdsBooking";
    private static final String TAG_IS_PERSONAL = "IsPersonal";
    private static final String TAG_IS_WITHDRAWN = "IsWithdrawn";
    private static final String TAG_IS_PUBLIC = "IsPublic";
    private static final String TAG_ITIN_ID = "ItinId";
    private static final String TAG_ITIN_LOCATOR = "ItinLocator";
    private static final String TAG_ITIN_SOURCE_LIST = "ItinSourceList";
    private static final String TAG_RECORD_LOCATOR = "RecordLocator";
    private static final String TAG_SEGMENT_TYPES = "SegmentTypes";
    private static final String TAG_START_DATE_LOCAL = "StartDateLocal";
    private static final String TAG_START_DATE_UTC = "StartDateUtc";
    private static final String TAG_TRIP_ID = "TripId";
    private static final String TAG_TRIP_KEY = "TripKey";
    private static final String TAG_TRIP_NAME = "TripName";
    private static final String TAG_TRIP_STATUS = "TripStatus";

    // Tag codes.
    private static final int TAG_APPROVAL_STATUS_CODE = 0;
    private static final int TAG_APPROVER_ID_CODE = 1;
    private static final int TAG_APPROVER_NAME_CODE = 2;
    private static final int TAG_AUTHORIZATION_NUMBER_CODE = 3;
    private static final int TAG_BOOKED_VIA_CODE = 4;
    private static final int TAG_BOOKING_SOURCE_CODE = 5;
    private static final int TAG_CAN_BE_EXPENSED_CODE = 6;
    private static final int TAG_CLIQBOOK_STATE_CODE = 7;
    private static final int TAG_END_DATE_LOCAL_CODE = 8;
    private static final int TAG_END_DATE_UTC_CODE = 9;
    private static final int TAG_HAS_OTHERS_CODE = 10;
    private static final int TAG_HAS_TICKETS_CODE = 11;
    private static final int TAG_IS_EXPENSED_CODE = 12;
    private static final int TAG_IS_GDS_BOOKING_CODE = 13;
    private static final int TAG_IS_PERSONAL_CODE = 14;
    private static final int TAG_IS_WITHDRAWN_CODE = 15;
    private static final int TAG_IS_PUBLIC_CODE = 16;
    private static final int TAG_ITIN_ID_CODE = 17;
    private static final int TAG_ITIN_LOCATOR_CODE = 18;
    private static final int TAG_ITIN_SOURCE_LIST_CODE = 19;
    private static final int TAG_RECORD_LOCATOR_CODE = 20;
    private static final int TAG_SEGMENT_TYPES_CODE = 21;
    private static final int TAG_START_DATE_LOCAL_CODE = 22;
    private static final int TAG_START_DATE_UTC_CODE = 23;
    private static final int TAG_TRIP_ID_CODE = 24;
    private static final int TAG_TRIP_KEY_CODE = 25;
    private static final int TAG_TRIP_NAME_CODE = 26;
    private static final int TAG_TRIP_STATUS_CODE = 27;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_APPROVAL_STATUS, TAG_APPROVAL_STATUS_CODE);
        tagMap.put(TAG_APPROVER_ID, TAG_APPROVER_ID_CODE);
        tagMap.put(TAG_APPROVER_NAME, TAG_APPROVER_NAME_CODE);
        tagMap.put(TAG_AUTHORIZATION_NUMBER, TAG_AUTHORIZATION_NUMBER_CODE);
        tagMap.put(TAG_BOOKED_VIA, TAG_BOOKED_VIA_CODE);
        tagMap.put(TAG_BOOKING_SOURCE, TAG_BOOKING_SOURCE_CODE);
        tagMap.put(TAG_CAN_BE_EXPENSED, TAG_CAN_BE_EXPENSED_CODE);
        tagMap.put(TAG_CLIQBOOK_STATE, TAG_CLIQBOOK_STATE_CODE);
        tagMap.put(TAG_END_DATE_LOCAL, TAG_END_DATE_LOCAL_CODE);
        tagMap.put(TAG_END_DATE_UTC, TAG_END_DATE_UTC_CODE);
        tagMap.put(TAG_HAS_OTHERS, TAG_HAS_OTHERS_CODE);
        tagMap.put(TAG_HAS_TICKETS, TAG_HAS_TICKETS_CODE);
        tagMap.put(TAG_IS_EXPENSED, TAG_IS_EXPENSED_CODE);
        tagMap.put(TAG_IS_GDS_BOOKING, TAG_IS_GDS_BOOKING_CODE);
        tagMap.put(TAG_IS_PERSONAL, TAG_IS_PERSONAL_CODE);
        tagMap.put(TAG_IS_WITHDRAWN, TAG_IS_WITHDRAWN_CODE);
        tagMap.put(TAG_IS_PUBLIC, TAG_IS_PUBLIC_CODE);
        tagMap.put(TAG_ITIN_ID, TAG_ITIN_ID_CODE);
        tagMap.put(TAG_ITIN_LOCATOR, TAG_ITIN_LOCATOR_CODE);
        tagMap.put(TAG_ITIN_SOURCE_LIST, TAG_ITIN_SOURCE_LIST_CODE);
        tagMap.put(TAG_RECORD_LOCATOR, TAG_RECORD_LOCATOR_CODE);
        tagMap.put(TAG_SEGMENT_TYPES, TAG_SEGMENT_TYPES_CODE);
        tagMap.put(TAG_START_DATE_LOCAL, TAG_START_DATE_LOCAL_CODE);
        tagMap.put(TAG_START_DATE_UTC, TAG_START_DATE_UTC_CODE);
        tagMap.put(TAG_TRIP_ID, TAG_TRIP_ID_CODE);
        tagMap.put(TAG_TRIP_KEY, TAG_TRIP_KEY_CODE);
        tagMap.put(TAG_TRIP_NAME, TAG_TRIP_NAME_CODE);
        tagMap.put(TAG_TRIP_STATUS, TAG_TRIP_STATUS_CODE);
    }

    public String approvalStatus;

    public String approverId;

    public String approverName;

    public String authorizationNumber;

    public String bookedVia;

    public String bookingSource;

    public Boolean canBeExpensed;

    public Integer cliqBookState;

    public Calendar endDateLocal;

    public Calendar endDateUtc;

    public Boolean hasOthers;

    public Boolean hasTickets;

    public Boolean isExpensed;

    public Boolean isGdsBooking;

    public Boolean isPersonal;

    public Boolean isWithdrawn;

    public Boolean isPublic;

    public Integer itinId;

    public String itinLocator;

    public String itinSourceList;

    public String recordLocator;

    public String segmentTypes;

    public Calendar startDateLocal;

    public Calendar startDateUtc;

    public Integer tripId;

    public String tripKey;

    public String tripName;

    public TripStateMessages tripStateMessages;

    public Integer tripStatus;

    private ItemParser<TripStateMessages> tripStateMessagesItemParser;

    private String startTag;

    /**
     * Constructs an instance of <code>TripListItinerary</code> with a parser and start tag.
     * 
     * @param parser
     *            contains a reference to the parser.
     * @param startTag
     *            contains a reference to the start tag.
     */
    public TripSummary(CommonParser parser, String startTag) {

        this.startTag = startTag;

        String itemTag = "TripStateMessages";
        tripStateMessagesItemParser = new ItemParser<TripStateMessages>(itemTag, TripStateMessages.class);
        parser.registerParser(tripStateMessagesItemParser, itemTag);
    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_APPROVAL_STATUS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    approvalStatus = text.trim();
                }
                break;
            }
            case TAG_APPROVER_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    approverId = text.trim();
                }
                break;
            }
            case TAG_APPROVER_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    approverName = text.trim();
                }
                break;
            }
            case TAG_AUTHORIZATION_NUMBER_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    authorizationNumber = text.trim();
                }
                break;
            }
            case TAG_BOOKED_VIA_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    bookedVia = text.trim();
                }
                break;
            }
            case TAG_BOOKING_SOURCE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    bookingSource = text.trim();
                }
                break;
            }
            case TAG_CAN_BE_EXPENSED_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    canBeExpensed = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_CLIQBOOK_STATE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    cliqBookState = Parse.safeParseInteger(text.trim());
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
            case TAG_HAS_OTHERS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    hasOthers = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_HAS_TICKETS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    hasTickets = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_IS_EXPENSED_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    isExpensed = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_IS_GDS_BOOKING_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    isGdsBooking = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_IS_PERSONAL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    isPersonal = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_IS_WITHDRAWN_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    isWithdrawn = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_IS_PUBLIC_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    isPublic = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_ITIN_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    itinId = Parse.safeParseIntegerDefaultToZero(text.trim());
                } else {
                    itinId = 0;
                }
                break;
            }
            case TAG_ITIN_LOCATOR_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    itinLocator = text.trim();
                }
                break;
            }
            case TAG_ITIN_SOURCE_LIST_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    itinSourceList = text.trim();
                }
                break;
            }
            case TAG_RECORD_LOCATOR_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    recordLocator = text.trim();
                }
                break;
            }
            case TAG_SEGMENT_TYPES_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    segmentTypes = text.trim();
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
            case TAG_TRIP_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    tripId = Parse.safeParseIntegerDefaultToZero(text.trim());
                } else {
                    tripId = 0;
                }
                break;
            }
            case TAG_TRIP_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    tripKey = text.trim();
                }
                break;
            }
            case TAG_TRIP_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    tripName = text.trim();
                }
                break;
            }
            case TAG_TRIP_STATUS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    tripStatus = Parse.safeParseIntegerDefaultToZero(text.trim());
                } else {
                    tripStatus = 0;
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
                tripStateMessages = tripStateMessagesItemParser.getItem();
            }
        }
    }

}
