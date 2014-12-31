package com.concur.mobile.platform.travel.trip;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Models base segment data.
 */
public class SegmentData extends BaseParser {

    private static final String CLS_TAG = "SegmentData";

    private static final String TAG_TRAVEL_POINTS = "TravelPoints";

    // Tags.
    private static final String TAG_START_DATE_UTC = "StartDateUtc";
    private static final String TAG_END_DATE_UTC = "EndDateUtc";
    private static final String TAG_START_DATE_LOCAL = "StartDateLocal";
    private static final String TAG_END_DATE_LOCAL = "EndDateLocal";
    private static final String TAG_CONFIRMATION_NUMBER = "ConfirmationNumber";
    private static final String TAG_CREDIT_CARD_ID = "CreditCardId";
    private static final String TAG_CREDIT_CARD_LAST_FOUR = "CreditCardLastFour";
    private static final String TAG_CREDIT_CARD_TYPE = "CreditCardType";
    private static final String TAG_CREDIT_CARD_TYPE_LOCALIZED = "CreditCardTypeLocalized";
    private static final String TAG_CURRENCY = "Currency";
    private static final String TAG_ERECEIPT_STATUS = "EReceiptStatus";
    private static final String TAG_END_ADDRESS = "EndAddress";
    private static final String TAG_END_ADDRESS_2 = "EndAddress2";
    private static final String TAG_END_CITY = "EndCity";
    private static final String TAG_END_CITY_CODE = "EndCityCode";
    private static final String TAG_END_CITY_CODE_LOCALIZED = "EndCityCodeLocalized";
    private static final String TAG_END_COUNTRY = "EndCountry";
    private static final String TAG_END_COUNTRY_CODE = "EndCountryCode";
    private static final String TAG_END_LATITUDE = "EndLatitude";
    private static final String TAG_END_LONGITUDE = "EndLongitude";
    private static final String TAG_END_POSTAL_CODE = "EndPostalCode";
    private static final String TAG_END_STATE = "EndState";
    private static final String TAG_FREQUENT_TRAVELER_ID = "FrequentTravelerId";
    private static final String TAG_IMAGE_VENDOR_URI = "ImageVendorURI";
    private static final String TAG_NUM_PERSONS = "NumPersons";
    private static final String TAG_OPERATED_BY_VENDOR = "OperatedByVendor";
    private static final String TAG_OPERATED_BY_VENDOR_NAME = "OperatedByVendorName";
    private static final String TAG_PHONE_NUMBER = "PhoneNumber";
    private static final String TAG_RATE_CODE = "RateCode";
    private static final String TAG_SEGMENT_KEY = "SegmentKey";
    private static final String TAG_SEGMENT_LOCATOR = "SegmentLocator";
    private static final String TAG_SEGMENT_NAME = "SegmentName";
    private static final String TAG_START_ADDRESS = "StartAddress";
    private static final String TAG_START_ADDRESS_2 = "StartAddress2";
    private static final String TAG_START_CITY = "StartCity";
    private static final String TAG_START_CITY_CODE = "StartCityCode";
    private static final String TAG_START_COUNTRY = "StartCountry";
    private static final String TAG_START_COUNTRY_CODE = "StartCountryCode";
    private static final String TAG_START_LATITUDE = "StartLatitude";
    private static final String TAG_START_LONGITUDE = "StartLongitude";
    private static final String TAG_START_POSTAL_CODE = "StartPostalCode";
    private static final String TAG_START_STATE = "StartState";
    private static final String TAG_STATUS = "Status";
    private static final String TAG_STATUS_LOCALIZED = "StatusLocalized";
    private static final String TAG_TIMEZONE_ID = "TimeZoneId";
    private static final String TAG_TOTAL_RATE = "TotalRate";
    private static final String TAG_TYPE_LOCALIZED = "TypeLocalized";
    private static final String TAG_VENDOR = "Vendor";
    private static final String TAG_VENDOR_NAME = "VendorName";
    private static final String TAG_VENDOR_URL = "VendorURL";
    private static final String TAG_ETICKET = "ETicket";
    private static final String TAG_TYPE = "Type";
    private static final String TAG_ID_KEY = "IdKey";

    // Tag codes.
    private static final int CODE_START_DATE_UTC = 0;
    private static final int CODE_END_DATE_UTC = 1;
    private static final int CODE_START_DATE_LOCAL = 2;
    private static final int CODE_END_DATE_LOCAL = 3;
    private static final int CODE_CONFIRMATION_NUMBER = 4;
    private static final int CODE_CREDIT_CARD_ID = 5;
    private static final int CODE_CREDIT_CARD_LAST_FOUR = 6;
    private static final int CODE_CREDIT_CARD_TYPE = 7;
    private static final int CODE_CREDIT_CARD_TYPE_LOCALIZED = 8;
    private static final int CODE_CURRENCY = 9;
    private static final int CODE_ERECEIPT_STATUS = 10;
    private static final int CODE_END_ADDRESS = 11;
    private static final int CODE_END_ADDRESS_2 = 12;
    private static final int CODE_END_CITY = 13;
    private static final int CODE_END_CITY_CODE = 14;
    private static final int CODE_END_CITY_CODE_LOCALIZED = 15;
    private static final int CODE_END_COUNTRY = 16;
    private static final int CODE_END_COUNTRY_CODE = 17;
    private static final int CODE_END_LATITUDE = 18;
    private static final int CODE_END_LONGITUDE = 19;
    private static final int CODE_END_POSTAL_CODE = 20;
    private static final int CODE_END_STATE = 21;
    private static final int CODE_FREQUENT_TRAVELER_ID = 22;
    private static final int CODE_IMAGE_VENDOR_URI = 23;
    private static final int CODE_NUM_PERSONS = 24;
    private static final int CODE_OPERATED_BY_VENDOR = 25;
    private static final int CODE_OPERATED_BY_VENDOR_NAME = 26;
    private static final int CODE_PHONE_NUMBER = 27;
    private static final int CODE_RATE_CODE = 28;
    private static final int CODE_SEGMENT_KEY = 29;
    private static final int CODE_SEGMENT_NAME = 30;
    private static final int CODE_START_ADDRESS = 31;
    private static final int CODE_START_ADDRESS_2 = 32;
    private static final int CODE_START_CITY = 33;
    private static final int CODE_START_CITY_CODE = 34;
    private static final int CODE_START_COUNTRY = 35;
    private static final int CODE_START_COUNTRY_CODE = 36;
    private static final int CODE_START_LATITUDE = 37;
    private static final int CODE_START_LONGITUDE = 38;
    private static final int CODE_START_POSTAL_CODE = 39;
    private static final int CODE_START_STATE = 40;
    private static final int CODE_STATUS = 41;
    private static final int CODE_STATUS_LOCALIZED = 42;
    private static final int CODE_TIMEZONE_ID = 43;
    private static final int CODE_TOTAL_RATE = 44;
    private static final int CODE_TYPE_LOCALIZED = 45;
    private static final int CODE_VENDOR = 46;
    private static final int CODE_VENDOR_NAME = 47;
    private static final int CODE_VENDOR_URL = 48;
    private static final int CODE_ETICKET = 49;
    private static final int CODE_SEGMENT_LOCATOR = 50;
    private static final int CODE_TYPE = 51;
    private static final int CODE_ID_KEY = 52;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> sdTagMap;

    static {
        // Initialize the map from tags to integer codes.
        sdTagMap = new HashMap<String, Integer>();
        sdTagMap.put(TAG_START_DATE_UTC, CODE_START_DATE_UTC);
        sdTagMap.put(TAG_END_DATE_UTC, CODE_END_DATE_UTC);
        sdTagMap.put(TAG_START_DATE_LOCAL, CODE_START_DATE_LOCAL);
        sdTagMap.put(TAG_END_DATE_LOCAL, CODE_END_DATE_LOCAL);
        sdTagMap.put(TAG_CONFIRMATION_NUMBER, CODE_CONFIRMATION_NUMBER);
        sdTagMap.put(TAG_CREDIT_CARD_ID, CODE_CREDIT_CARD_ID);
        sdTagMap.put(TAG_CREDIT_CARD_LAST_FOUR, CODE_CREDIT_CARD_LAST_FOUR);
        sdTagMap.put(TAG_CREDIT_CARD_TYPE, CODE_CREDIT_CARD_TYPE);
        sdTagMap.put(TAG_CREDIT_CARD_TYPE_LOCALIZED, CODE_CREDIT_CARD_TYPE_LOCALIZED);
        sdTagMap.put(TAG_CURRENCY, CODE_CURRENCY);
        sdTagMap.put(TAG_ERECEIPT_STATUS, CODE_ERECEIPT_STATUS);
        sdTagMap.put(TAG_END_ADDRESS, CODE_END_ADDRESS);
        sdTagMap.put(TAG_END_ADDRESS_2, CODE_END_ADDRESS_2);
        sdTagMap.put(TAG_END_CITY, CODE_END_CITY);
        sdTagMap.put(TAG_END_CITY_CODE, CODE_END_CITY_CODE);
        sdTagMap.put(TAG_END_CITY_CODE_LOCALIZED, CODE_END_CITY_CODE_LOCALIZED);
        sdTagMap.put(TAG_END_COUNTRY, CODE_END_COUNTRY);
        sdTagMap.put(TAG_END_COUNTRY_CODE, CODE_END_COUNTRY_CODE);
        sdTagMap.put(TAG_END_LATITUDE, CODE_END_LATITUDE);
        sdTagMap.put(TAG_END_LONGITUDE, CODE_END_LONGITUDE);
        sdTagMap.put(TAG_END_POSTAL_CODE, CODE_END_POSTAL_CODE);
        sdTagMap.put(TAG_END_STATE, CODE_END_STATE);
        sdTagMap.put(TAG_FREQUENT_TRAVELER_ID, CODE_FREQUENT_TRAVELER_ID);
        sdTagMap.put(TAG_IMAGE_VENDOR_URI, CODE_IMAGE_VENDOR_URI);
        sdTagMap.put(TAG_NUM_PERSONS, CODE_NUM_PERSONS);
        sdTagMap.put(TAG_OPERATED_BY_VENDOR, CODE_OPERATED_BY_VENDOR);
        sdTagMap.put(TAG_OPERATED_BY_VENDOR_NAME, CODE_OPERATED_BY_VENDOR_NAME);
        sdTagMap.put(TAG_PHONE_NUMBER, CODE_PHONE_NUMBER);
        sdTagMap.put(TAG_RATE_CODE, CODE_RATE_CODE);
        sdTagMap.put(TAG_SEGMENT_KEY, CODE_SEGMENT_KEY);
        sdTagMap.put(TAG_SEGMENT_NAME, CODE_SEGMENT_NAME);
        sdTagMap.put(TAG_START_ADDRESS, CODE_START_ADDRESS);
        sdTagMap.put(TAG_START_ADDRESS_2, CODE_START_ADDRESS_2);
        sdTagMap.put(TAG_START_CITY, CODE_START_CITY);
        sdTagMap.put(TAG_START_CITY_CODE, CODE_START_CITY_CODE);
        sdTagMap.put(TAG_START_COUNTRY, CODE_START_COUNTRY);
        sdTagMap.put(TAG_START_COUNTRY_CODE, CODE_START_COUNTRY_CODE);
        sdTagMap.put(TAG_START_LATITUDE, CODE_START_LATITUDE);
        sdTagMap.put(TAG_START_LONGITUDE, CODE_START_LONGITUDE);
        sdTagMap.put(TAG_START_POSTAL_CODE, CODE_START_POSTAL_CODE);
        sdTagMap.put(TAG_START_STATE, CODE_START_STATE);
        sdTagMap.put(TAG_STATUS, CODE_STATUS);
        sdTagMap.put(TAG_STATUS_LOCALIZED, CODE_STATUS_LOCALIZED);
        sdTagMap.put(TAG_TIMEZONE_ID, CODE_TIMEZONE_ID);
        sdTagMap.put(TAG_TOTAL_RATE, CODE_TOTAL_RATE);
        sdTagMap.put(TAG_TYPE_LOCALIZED, CODE_TYPE_LOCALIZED);
        sdTagMap.put(TAG_VENDOR, CODE_VENDOR);
        sdTagMap.put(TAG_VENDOR_NAME, CODE_VENDOR_NAME);
        sdTagMap.put(TAG_VENDOR_URL, CODE_VENDOR_URL);
        sdTagMap.put(TAG_ETICKET, CODE_ETICKET);
        sdTagMap.put(TAG_SEGMENT_LOCATOR, CODE_SEGMENT_LOCATOR);
        sdTagMap.put(TAG_TYPE, CODE_TYPE);
        sdTagMap.put(TAG_ID_KEY, CODE_ID_KEY);
    }

    public Calendar startDateUtc;
    public Calendar endDateUtc;
    public Calendar startDateLocal;
    public Calendar endDateLocal;

    // Hold onto the day values (no time) as well for quick lookups
    protected Calendar startDayUtc;
    protected Calendar endDayUtc;
    protected Calendar startDayLocal;
    protected Calendar endDayLocal;

    // Segment type name from server
    public String segmentTypeName;

    public int gdsId;
    public String bookingSource;

    // The record locator.
    public String locator;

    public String confirmNumber;
    public String creditCardId;
    public String creditCardLastFour;
    public String creditCardType;
    public String creditCardTypeLocalized;
    public String currency;
    public String eReceiptStatus;
    public String eTicket;
    public String endAddress;
    public String endAddress2;
    public String endCity;
    public String endCityCode;
    public String endCityCodeLocalized;
    public String endCountry;
    public String endCountryCode;
    public Double endLat;
    public Double endLong;
    public String endPostCode;
    public String endState;
    public String frequentTravelerId;
    public String imageVendorUri;
    public Integer numPersons;
    public String operatedByVendor;
    public String operatedByVendorName;
    public String phoneNumber;
    public String rateCode;
    public String segmentKey;
    public String segmentLocator;
    public String segmentName;
    public String startAddress;
    public String startAddress2;
    public String startCity;
    public String startCityCode;
    public String startCountry;
    public String startCountryCode;
    public Double startLat;
    public Double startLong;
    public String startPostCode;
    public String startState;
    public String status;
    public String statusLocalized;
    public TimeZone timeZone;
    public Double totalRate;
    public String vendor;
    public String vendorName;
    public String vendorURL;
    public String type;
    public String idKey;

    public TravelPoint travelPoint;
    public boolean inTravelPoint;

    public static enum SegmentType {
        AIR, CAR, DINING, EVENT, HOTEL, PARKING, RAIL, RIDE, UNDEFINED
    }

    protected SegmentType segmentType;

    public SegmentData(SegmentType type) {
        this.segmentType = type;
    }

    public SegmentType getSegmentType() {
        return segmentType;
    }

    @Override
    public void startTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_TRAVEL_POINTS)) {
                inTravelPoint = true;
                travelPoint = new TravelPoint();
            }
        }
    }

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_TRAVEL_POINTS)) {
                inTravelPoint = false;
            }
        }
    }

    /**
     * Will handle <code>tag</code> with <code>text</code>.
     * 
     * @param tag
     *            contains the tag.
     * @param text
     *            contains the text.
     * @return returns whether or not this method identified the tag.
     */
    public boolean handleSegmentText(String tag, String text) {
        boolean retVal = false;
        if (inTravelPoint) {
            travelPoint.handleText(tag, text);
            retVal = true;
        } else {
            Integer tagCode = sdTagMap.get(tag);
            if (tagCode != null) {
                if (!TextUtils.isEmpty(text)) {
                    switch (tagCode) {
                    case CODE_START_DATE_UTC: {
                        startDateUtc = Parse.parseXMLTimestamp(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_END_DATE_UTC: {
                        endDateUtc = Parse.parseXMLTimestamp(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_START_DATE_LOCAL: {
                        startDateLocal = Parse.parseTimestamp(text.trim(), Parse.XML_DF_LOCAL);
                        retVal = true;
                        break;
                    }
                    case CODE_END_DATE_LOCAL: {
                        endDateLocal = Parse.parseTimestamp(text.trim(), Parse.XML_DF_LOCAL);
                        retVal = true;
                        break;
                    }
                    case CODE_CONFIRMATION_NUMBER: {
                        confirmNumber = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_CREDIT_CARD_ID: {
                        creditCardId = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_CREDIT_CARD_LAST_FOUR: {
                        creditCardLastFour = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_CREDIT_CARD_TYPE: {
                        creditCardType = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_CREDIT_CARD_TYPE_LOCALIZED: {
                        creditCardTypeLocalized = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_CURRENCY: {
                        currency = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_ERECEIPT_STATUS: {
                        eReceiptStatus = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_ETICKET: {
                        eTicket = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_ADDRESS: {
                        endAddress = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_ADDRESS_2: {
                        endAddress2 = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_CITY: {
                        endCity = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_CITY_CODE: {
                        endCityCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_CITY_CODE_LOCALIZED: {
                        endCityCodeLocalized = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_COUNTRY: {
                        endCountry = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_COUNTRY_CODE: {
                        endCountryCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_LATITUDE: {
                        endLat = Parse.safeParseDouble(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_END_LONGITUDE: {
                        endLong = Parse.safeParseDouble(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_END_POSTAL_CODE: {
                        endPostCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_STATE: {
                        endState = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_FREQUENT_TRAVELER_ID: {
                        frequentTravelerId = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_IMAGE_VENDOR_URI: {
                        imageVendorUri = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_NUM_PERSONS: {
                        numPersons = Parse.safeParseInteger(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_OPERATED_BY_VENDOR: {
                        operatedByVendor = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_OPERATED_BY_VENDOR_NAME: {
                        operatedByVendorName = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_PHONE_NUMBER: {
                        phoneNumber = text.trim().replace('/', '-');
                        retVal = true;
                        break;
                    }
                    case CODE_RATE_CODE: {
                        rateCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_SEGMENT_KEY: {
                        segmentKey = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_SEGMENT_LOCATOR: {
                        segmentLocator = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_SEGMENT_NAME: {
                        segmentName = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_ADDRESS: {
                        startAddress = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_ADDRESS_2: {
                        startAddress2 = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_CITY: {
                        startCity = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_CITY_CODE: {
                        startCityCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_COUNTRY: {
                        startCountry = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_COUNTRY_CODE: {
                        startCountryCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_LATITUDE: {
                        startLat = Parse.safeParseDouble(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_START_LONGITUDE: {
                        startLong = Parse.safeParseDouble(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_START_POSTAL_CODE: {
                        startPostCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_STATE: {
                        startState = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_STATUS: {
                        status = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_STATUS_LOCALIZED: {
                        statusLocalized = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_TIMEZONE_ID: {
                        timeZone = TimeZone.getTimeZone(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_TOTAL_RATE: {
                        totalRate = Parse.safeParseDouble(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_TYPE_LOCALIZED: {
                        segmentTypeName = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_VENDOR: {
                        vendor = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_VENDOR_NAME: {
                        vendorName = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_VENDOR_URL: {
                        vendorURL = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_TYPE: {
                        type = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_ID_KEY: {
                        idKey = text.trim();
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

    @Override
    public void handleText(String tag, String text) {
        if (!handleSegmentText(tag, text)) {
            if (Const.DEBUG_PARSING) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
            }
        }
    }

}
