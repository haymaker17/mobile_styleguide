package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>SegmentData</code> for identifying hotel specific segment information.
 */
public class HotelSegmentData extends SegmentData {

    private static final String CLS_TAG = "HotelSegmentData";

    // Tags.
    private static final String TAG_CHECK_IN_TIME = "CheckinTime";
    private static final String TAG_CHECK_OUT_TIME = "CheckoutTime";
    private static final String TAG_DISCOUNT_CODE = "DiscountCode";
    private static final String TAG_NUM_ROOMS = "NumRooms";
    private static final String TAG_RATE_CODE = "RateCode";
    private static final String TAG_ROOM_TYPE = "RoomType";
    private static final String TAG_ROOM_TYPE_LOCALIZED = "RoomTypeLocalized";
    private static final String TAG_DAILY_RATE = "DailyRate";
    private static final String TAG_TOTAL_RATE = "TotalRate";
    private static final String TAG_CANCELLATION_POLICY = "CancellationPolicy";
    private static final String TAG_SPECIAL_INSTRUCTIONS = "SpecialInstructions";
    private static final String TAG_ROOM_DESCRIPTION = "RoomDescription";
    private static final String TAG_RATE_TYPE = "RateType";
    private static final String TAG_PROPERTY_ID = "HotelPropertyId";
    private static final String TAG_HOTEL_IMAGE = "HotelImage";

    // Tag codes.
    private static final int CODE_CHECK_IN_TIME = 1;
    private static final int CODE_CHECK_OUT_TIME = 2;
    private static final int CODE_DISCOUNT_CODE = 3;
    private static final int CODE_NUM_ROOMS = 4;
    private static final int CODE_RATE_CODE = 5;
    private static final int CODE_ROOM_TYPE = 6;
    private static final int CODE_ROOM_TYPE_LOCALIZED = 7;
    private static final int CODE_DAILY_RATE = 8;
    private static final int CODE_TOTAL_RATE = 9;
    private static final int CODE_CANCELLATION_POLICY = 10;
    private static final int CODE_SPECIAL_INSTRUCTIONS = 11;
    private static final int CODE_ROOM_DESCRIPTION = 12;
    private static final int CODE_RATE_TYPE = 13;
    private static final int CODE_PROPERTY_ID = 14;
    private static final int CODE_HOTEL_IMAGE = 15;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> hsdTagMap;

    static {
        // Initialize the map from tags to integer codes.
        hsdTagMap = new HashMap<String, Integer>();
        hsdTagMap.put(TAG_CHECK_IN_TIME, CODE_CHECK_IN_TIME);
        hsdTagMap.put(TAG_CHECK_OUT_TIME, CODE_CHECK_OUT_TIME);
        hsdTagMap.put(TAG_DISCOUNT_CODE, CODE_DISCOUNT_CODE);
        hsdTagMap.put(TAG_NUM_ROOMS, CODE_NUM_ROOMS);
        hsdTagMap.put(TAG_RATE_CODE, CODE_RATE_CODE);
        hsdTagMap.put(TAG_ROOM_TYPE, CODE_ROOM_TYPE);
        hsdTagMap.put(TAG_ROOM_TYPE_LOCALIZED, CODE_ROOM_TYPE_LOCALIZED);
        hsdTagMap.put(TAG_DAILY_RATE, CODE_DAILY_RATE);
        hsdTagMap.put(TAG_TOTAL_RATE, CODE_TOTAL_RATE);
        hsdTagMap.put(TAG_CANCELLATION_POLICY, CODE_CANCELLATION_POLICY);
        hsdTagMap.put(TAG_SPECIAL_INSTRUCTIONS, CODE_SPECIAL_INSTRUCTIONS);
        hsdTagMap.put(TAG_ROOM_DESCRIPTION, CODE_ROOM_DESCRIPTION);
        hsdTagMap.put(TAG_RATE_TYPE, CODE_RATE_TYPE);
        hsdTagMap.put(TAG_PROPERTY_ID, CODE_PROPERTY_ID);
        hsdTagMap.put(TAG_HOTEL_IMAGE, CODE_HOTEL_IMAGE);
    }

    public String checkinTime; // I don't believe we will ever need to do calcs with these
    public String checkoutTime; // so we're just leaving them as strings for now
    public String discountCode;
    public Integer numRooms;
    public String rateCode;
    public String roomType;
    public String roomTypeLocalized;
    public Double dailyRate;
    public Double totalRate;
    public String cancellationPolicy;
    public String specialInstructions;
    public String roomDescription;
    public String rateType;
    public String propertyId;

    public int propertyImageCount;

    public HotelSegmentData() {
        super(SegmentType.HOTEL);
    }

    @Override
    public boolean handleSegmentText(String tag, String text) {
        boolean retVal = false;
        retVal = super.handleSegmentText(tag, text);
        if (!retVal) {
            Integer tagCode = hsdTagMap.get(tag);
            if (tagCode != null) {
                if (!TextUtils.isEmpty(text)) {
                    switch (tagCode) {
                    case CODE_CHECK_IN_TIME: {
                        checkinTime = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_CHECK_OUT_TIME: {
                        checkoutTime = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_DISCOUNT_CODE: {
                        discountCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_NUM_ROOMS: {
                        numRooms = Parse.safeParseInteger(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_RATE_CODE: {
                        rateCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_ROOM_TYPE: {
                        roomType = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_ROOM_TYPE_LOCALIZED: {
                        roomTypeLocalized = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_DAILY_RATE: {
                        dailyRate = Parse.safeParseDouble(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_TOTAL_RATE: {
                        totalRate = Parse.safeParseDouble(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_CANCELLATION_POLICY: {
                        cancellationPolicy = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_SPECIAL_INSTRUCTIONS: {
                        specialInstructions = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_ROOM_DESCRIPTION: {
                        roomDescription = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_RATE_TYPE: {
                        rateType = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_PROPERTY_ID: {
                        propertyId = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_HOTEL_IMAGE: {
                        propertyImageCount++;
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

}
