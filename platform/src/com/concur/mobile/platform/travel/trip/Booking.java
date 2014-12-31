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
 * An extension of <code>BaseParser</code> for the purpose of parsing a booking object.
 */
public class Booking extends BaseParser {

    private static final String CLS_TAG = "Booking";

    private static final String TAG_AGENCY_PCC = "AgencyPCC";
    private static final String TAG_BOOKING_SOURCE = "BookingSource";
    private static final String TAG_COMPANY_ACCOUNTING_CODE = "CompanyAccountingCode";
    private static final String TAG_DATE_BOOKED_LOCAL = "DateBookedLocal";
    private static final String TAG_IS_CLIQBOOK_SYSTEM_OF_RECORD = "IsCliqbookSystemOfRecord";
    private static final String TAG_RECORD_LOCATOR = "RecordLocator";
    private static final String TAG_TRAVEL_CONFIG_ID = "TravelConfigId";
    private static final String TAG_TYPE = "Type";
    private static final String TAG_IS_GDS_BOOKING = "_x0020_IsGdsBooking";

    private static final int TAG_AGENCY_PCC_CODE = 0;
    private static final int TAG_BOOKING_SOURCE_CODE = 1;
    private static final int TAG_COMPANY_ACCOUNTING_CODE_CODE = 2;
    private static final int TAG_TYPE_CODE = 3;
    private static final int TAG_DATE_BOOKED_LOCAL_CODE = 4;
    private static final int TAG_IS_CLIQBOOK_SYSTEM_OF_RECORD_CODE = 5;
    private static final int TAG_RECORD_LOCATOR_CODE = 6;
    private static final int TAG_TRAVEL_CONFIG_ID_CODE = 7;
    private static final int TAG_IS_GDS_BOOKING_CODE = 8;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_AGENCY_PCC, TAG_AGENCY_PCC_CODE);
        tagMap.put(TAG_BOOKING_SOURCE, TAG_BOOKING_SOURCE_CODE);
        tagMap.put(TAG_COMPANY_ACCOUNTING_CODE, TAG_COMPANY_ACCOUNTING_CODE_CODE);
        tagMap.put(TAG_DATE_BOOKED_LOCAL, TAG_DATE_BOOKED_LOCAL_CODE);
        tagMap.put(TAG_IS_CLIQBOOK_SYSTEM_OF_RECORD, TAG_IS_CLIQBOOK_SYSTEM_OF_RECORD_CODE);
        tagMap.put(TAG_RECORD_LOCATOR, TAG_RECORD_LOCATOR_CODE);
        tagMap.put(TAG_TRAVEL_CONFIG_ID, TAG_TRAVEL_CONFIG_ID_CODE);
        tagMap.put(TAG_TYPE, TAG_TYPE_CODE);
        tagMap.put(TAG_IS_GDS_BOOKING, TAG_IS_GDS_BOOKING_CODE);
    }

    /**
     * Contains the agency PCC.
     */
    public String agencyPCC;

    /**
     * Contains the airline tickets.
     */
    public List<AirlineTicket> airlineTickets;

    /**
     * Contains the booking source.
     */
    public String bookingSource;

    /**
     * Contains the company accounting code.
     */
    public String companyAccountingCode;

    /**
     * Contains the date booked local.
     */
    public Calendar dateBookedLocal;

    /**
     * Contains whether Cliqbook is the system of record.
     */
    public Boolean isCliqbookSystemOfRecord;

    /**
     * Contains the passenger list.
     */
    public List<Passenger> passengers;

    /**
     * Contains the record locator.
     */
    public String recordLocator;

    /**
     * Contains the segment list.
     */
    public List<Segment> segments;

    /**
     * Contains the travel config ID.
     */
    public String travelConfigID;

    /**
     * Contains the type.
     */
    public String type;

    /**
     * Contains whether this is a GDS booking.
     */
    public Boolean isGdsBooking;

    /**
     * Contains the start tag for this parser.
     */
    private String startTag;

    /**
     * Contains the airline ticket list parser.
     */
    private ListParser<AirlineTicket> airlineTicketListParser;

    /**
     * Contains the passenger list parser.
     */
    private ListParser<Passenger> passengerListParser;

    /**
     * Contains the segment list parser.
     */
    private ListParser<Segment> segmentListParser;

    public Booking(CommonParser parser, String startTag) {

        this.startTag = startTag;

        // Create and register the airline ticket list parser.
        String listTag = "AirlineTickets";
        airlineTicketListParser = new ListParser<AirlineTicket>(parser, listTag, "AirlineTicket", AirlineTicket.class);
        parser.registerParser(airlineTicketListParser, listTag);

        // Create and register the passenger list parser.
        listTag = "Passengers";
        passengerListParser = new ListParser<Passenger>(parser, listTag, "Passenger", Passenger.class);
        parser.registerParser(passengerListParser, listTag);

        // Create and register the segment list parser.
        listTag = "Segments";
        segmentListParser = new ListParser<Segment>(parser, listTag, "Segment", Segment.class);
        parser.registerParser(segmentListParser, listTag);

    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);

        if (tagCode != null) {
            switch (tagCode) {
            case TAG_AGENCY_PCC_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    agencyPCC = text.trim();
                }
                break;
            }
            case TAG_BOOKING_SOURCE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    bookingSource = text.trim();
                }
                break;
            }
            case TAG_COMPANY_ACCOUNTING_CODE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    companyAccountingCode = text.trim();
                }
                break;
            }
            case TAG_TYPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    type = text.trim();
                }
                break;
            }
            case TAG_DATE_BOOKED_LOCAL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    dateBookedLocal = Parse.parseTimestamp(text.trim(), Parse.XML_DF_LOCAL);
                }
                break;
            }
            case TAG_IS_CLIQBOOK_SYSTEM_OF_RECORD_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    isCliqbookSystemOfRecord = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_RECORD_LOCATOR_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    recordLocator = text.trim();
                }
                break;
            }
            case TAG_TRAVEL_CONFIG_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    travelConfigID = text.trim();
                }
                break;
            }
            case TAG_IS_GDS_BOOKING_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    isGdsBooking = Parse.safeParseBoolean(text.trim());
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
                // Set the airline ticket list.
                airlineTickets = airlineTicketListParser.getList();
                // Set the passenger list.
                passengers = passengerListParser.getList();
                // Set the segments.
                segments = segmentListParser.getList();
            }
        }
    }

}
