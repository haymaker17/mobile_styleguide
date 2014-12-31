package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>SegmentData</code> for identifying car specific segment information.
 */
public class CarSegmentData extends SegmentData {

    private static final String CLS_TAG = "CarSegmentData";

    private static final String TAG_AIR_CONDITION = "AirCondition";
    private static final String TAG_AIR_CONDITION_LOCALIZED = "AirConditionLocalized";
    private static final String TAG_BODY = "Body";
    private static final String TAG_BODY_LOCALIZED = "BodyLocalized";
    private static final String TAG_CLASS_OF_CAR = "ClassOfCar";
    private static final String TAG_CLASS_OF_CAR_LOCALIZED = "ClassOfCarLocalized";
    private static final String TAG_DAILY_RATE = "DailyRate";
    private static final String TAG_DISCOUNT_CODE = "DiscountCode";
    private static final String TAG_END_AIRPORT_CITY = "EndAirportCity";
    private static final String TAG_END_AIRPORT_COUNTRY = "EndAirportCountry";
    private static final String TAG_END_AIRPORT_COUNTRY_CODE = "EndAirportCountryCode";
    private static final String TAG_END_AIRPORT_NAME = "EndAirportName";
    private static final String TAG_END_AIRPORT_STATE = "EndAirportState";
    private static final String TAG_END_LOCATION = "EndLocation";
    private static final String TAG_IMAGE_CAR_URI = "ImageCarUri";
    private static final String TAG_NUM_CARS = "NumCars";
    private static final String TAG_RATE_TYPE = "RateType";
    private static final String TAG_SPECIAL_EQUIPMENT = "SpecialEquipment";
    private static final String TAG_START_AIRPORT_CITY = "StartAirportCity";
    private static final String TAG_START_AIRPORT_COUNTRY = "StartAirportCountry";
    private static final String TAG_START_AIRPORT_COUNTRY_CODE = "StartAirportCountryCode";
    private static final String TAG_START_AIRPORT_NAME = "StartAirportName";
    private static final String TAG_START_AIRPORT_STATE = "StartAirportState";
    private static final String TAG_START_LOCATION = "StartLocation";
    private static final String TAG_TRANSMISSION = "Transmission";
    private static final String TAG_TRANSMISSION_LOCALIZED = "TransmissionLocalized";

    private static final int CODE_AIR_CONDITION = 0;
    private static final int CODE_AIR_CONDITION_LOCALIZED = 1;
    private static final int CODE_BODY = 2;
    private static final int CODE_BODY_LOCALIZED = 3;
    private static final int CODE_CLASS_OF_CAR = 4;
    private static final int CODE_CLASS_OF_CAR_LOCALIZED = 5;
    private static final int CODE_DAILY_RATE = 6;
    private static final int CODE_DISCOUNT_CODE = 7;
    private static final int CODE_END_AIRPORT_CITY = 8;
    private static final int CODE_END_AIRPORT_COUNTRY = 9;
    private static final int CODE_END_AIRPORT_COUNTRY_CODE = 10;
    private static final int CODE_END_AIRPORT_NAME = 11;
    private static final int CODE_END_AIRPORT_STATE = 12;
    private static final int CODE_END_LOCATION = 13;
    private static final int CODE_IMAGE_CAR_URI = 14;
    private static final int CODE_NUM_CARS = 15;
    private static final int CODE_RATE_TYPE = 16;
    private static final int CODE_SPECIAL_EQUIPMENT = 17;
    private static final int CODE_START_AIRPORT_CITY = 18;
    private static final int CODE_START_AIRPORT_COUNTRY = 19;
    private static final int CODE_START_AIRPORT_COUNTRY_CODE = 20;
    private static final int CODE_START_AIRPORT_NAME = 21;
    private static final int CODE_START_AIRPORT_STATE = 22;
    private static final int CODE_START_LOCATION = 23;
    private static final int CODE_TRANSMISSION = 24;
    private static final int CODE_TRANSMISSION_LOCALIZED = 25;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> csdTagMap;

    static {
        // Initialize the map from tags to integer codes.
        csdTagMap = new HashMap<String, Integer>();
        csdTagMap.put(TAG_AIR_CONDITION, CODE_AIR_CONDITION);
        csdTagMap.put(TAG_AIR_CONDITION_LOCALIZED, CODE_AIR_CONDITION_LOCALIZED);
        csdTagMap.put(TAG_BODY, CODE_BODY);
        csdTagMap.put(TAG_BODY_LOCALIZED, CODE_BODY_LOCALIZED);
        csdTagMap.put(TAG_CLASS_OF_CAR, CODE_CLASS_OF_CAR);
        csdTagMap.put(TAG_CLASS_OF_CAR_LOCALIZED, CODE_CLASS_OF_CAR_LOCALIZED);
        csdTagMap.put(TAG_DAILY_RATE, CODE_DAILY_RATE);
        csdTagMap.put(TAG_DISCOUNT_CODE, CODE_DISCOUNT_CODE);
        csdTagMap.put(TAG_END_AIRPORT_CITY, CODE_END_AIRPORT_CITY);
        csdTagMap.put(TAG_END_AIRPORT_COUNTRY, CODE_END_AIRPORT_COUNTRY);
        csdTagMap.put(TAG_END_AIRPORT_COUNTRY_CODE, CODE_END_AIRPORT_COUNTRY_CODE);
        csdTagMap.put(TAG_END_AIRPORT_NAME, CODE_END_AIRPORT_NAME);
        csdTagMap.put(TAG_END_AIRPORT_STATE, CODE_END_AIRPORT_STATE);
        csdTagMap.put(TAG_END_LOCATION, CODE_END_LOCATION);
        csdTagMap.put(TAG_IMAGE_CAR_URI, CODE_IMAGE_CAR_URI);
        csdTagMap.put(TAG_NUM_CARS, CODE_NUM_CARS);
        csdTagMap.put(TAG_RATE_TYPE, CODE_RATE_TYPE);
        csdTagMap.put(TAG_SPECIAL_EQUIPMENT, CODE_SPECIAL_EQUIPMENT);
        csdTagMap.put(TAG_START_AIRPORT_CITY, CODE_START_AIRPORT_CITY);
        csdTagMap.put(TAG_START_AIRPORT_COUNTRY, CODE_START_AIRPORT_COUNTRY);
        csdTagMap.put(TAG_START_AIRPORT_COUNTRY_CODE, CODE_START_AIRPORT_COUNTRY_CODE);
        csdTagMap.put(TAG_START_AIRPORT_NAME, CODE_START_AIRPORT_NAME);
        csdTagMap.put(TAG_START_AIRPORT_STATE, CODE_START_AIRPORT_STATE);
        csdTagMap.put(TAG_START_LOCATION, CODE_START_LOCATION);
        csdTagMap.put(TAG_TRANSMISSION, CODE_TRANSMISSION);
        csdTagMap.put(TAG_TRANSMISSION_LOCALIZED, CODE_TRANSMISSION_LOCALIZED);
    }

    public String airCondition;
    public String airConditionLocalized;
    public String body;
    public String bodyLocalized;
    public String classOfCar;
    public String classOfCarLocalized;
    public Double dailyRate;
    public String discountCode;
    public String imageCarUri;
    public Integer numCars;
    public String rateType;
    public String specialEquipment;
    public String startLocation;
    public String endLocation;
    public String transmission;
    public String transmissionLocalized;

    // The location info for car is stored in these airport fields.
    public String endAirportCity;
    public String endAirportCountry;
    public String endAirportCountryCode;
    public String endAirportName;
    public String endAirportState;
    public String startAirportCity;
    public String startAirportCountry;
    public String startAirportCountryCode;
    public String startAirportName;
    public String startAirportState;

    public CarSegmentData() {
        super(SegmentType.CAR);
    }

    @Override
    public boolean handleSegmentText(String tag, String text) {
        boolean retVal = false;
        retVal = super.handleSegmentText(tag, text);
        if (!retVal) {
            Integer tagCode = csdTagMap.get(tag);
            if (tagCode != null) {
                if (!TextUtils.isEmpty(text)) {
                    switch (tagCode) {
                    case CODE_AIR_CONDITION: {
                        airCondition = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_AIR_CONDITION_LOCALIZED: {
                        airConditionLocalized = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_BODY: {
                        body = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_BODY_LOCALIZED: {
                        bodyLocalized = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_CLASS_OF_CAR: {
                        classOfCar = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_CLASS_OF_CAR_LOCALIZED: {
                        classOfCarLocalized = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_DAILY_RATE: {
                        dailyRate = Parse.safeParseDouble(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_DISCOUNT_CODE: {
                        discountCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_AIRPORT_CITY: {
                        endAirportCity = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_AIRPORT_COUNTRY: {
                        endAirportCountry = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_AIRPORT_COUNTRY_CODE: {
                        endAirportCountryCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_AIRPORT_NAME: {
                        endAirportName = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_AIRPORT_STATE: {
                        endAirportState = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_END_LOCATION: {
                        endLocation = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_IMAGE_CAR_URI: {
                        imageCarUri = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_NUM_CARS: {
                        numCars = Parse.safeParseInteger(text.trim());
                        retVal = true;
                        break;
                    }
                    case CODE_RATE_TYPE: {
                        rateType = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_SPECIAL_EQUIPMENT: {
                        specialEquipment = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_AIRPORT_CITY: {
                        startAirportCity = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_AIRPORT_COUNTRY: {
                        startAirportCountry = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_AIRPORT_COUNTRY_CODE: {
                        startAirportCountryCode = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_AIRPORT_NAME: {
                        startAirportName = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_AIRPORT_STATE: {
                        startAirportState = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_START_LOCATION: {
                        startLocation = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_TRANSMISSION: {
                        transmission = text.trim();
                        retVal = true;
                        break;
                    }
                    case CODE_TRANSMISSION_LOCALIZED: {
                        transmissionLocalized = text.trim();
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
