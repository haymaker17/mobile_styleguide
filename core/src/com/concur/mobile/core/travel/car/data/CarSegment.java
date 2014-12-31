package com.concur.mobile.core.travel.car.data;

import java.net.URI;

import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.travel.data.Segment;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

public class CarSegment extends Segment {

    /**
     * Default constructor does nothing but set the type
     */
    public CarSegment() {
        type = SegmentType.CAR;
    }

    public String airCondition;
    public String airConditionLocalized;
    public String body;
    public String bodyLocalized;
    public String classOfCar;
    public String classOfCarLocalized;
    public Double dailyRate;
    public String discountCode;
    public URI imageCarUri;
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

    @Override
    protected boolean handleSegmentElement(String localName, String chars) {

        if (!super.handleSegmentElement(localName, chars)) {
            if (localName.equalsIgnoreCase("AirCondition")) {
                airCondition = chars;
            } else if (localName.equalsIgnoreCase("AirConditionLocalized")) {
                airConditionLocalized = chars;
            } else if (localName.equalsIgnoreCase("Body")) {
                body = chars;
            } else if (localName.equalsIgnoreCase("BodyLocalized")) {
                bodyLocalized = chars;
            } else if (localName.equalsIgnoreCase("ClassOfCar")) {
                classOfCar = chars;
            } else if (localName.equalsIgnoreCase("ClassOfCarLocalized")) {
                classOfCarLocalized = chars;
            } else if (localName.equalsIgnoreCase("DailyRate")) {
                dailyRate = Parse.safeParseDouble(chars);
            } else if (localName.equalsIgnoreCase("DiscountCode")) {
                discountCode = chars;
            } else if (localName.equalsIgnoreCase("EndAirportCity")) {
                endAirportCity = chars;
            } else if (localName.equalsIgnoreCase("EndAirportCountry")) {
                endAirportCountry = chars;
            } else if (localName.equalsIgnoreCase("EndAirportCountryCode")) {
                endAirportCountryCode = chars;
            } else if (localName.equalsIgnoreCase("EndAirportName")) {
                endAirportName = chars;
            } else if (localName.equalsIgnoreCase("EndAirportState")) {
                endAirportState = chars;
            } else if (localName.equalsIgnoreCase("EndLocation")) {
                endLocation = chars;
            } else if (localName.equalsIgnoreCase("ImageCarUri")) {
                imageCarUri = Format.formatServerURI(false, Preferences.getServerAddress(), chars);
            } else if (localName.equalsIgnoreCase("NumCars")) {
                numCars = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("RateType")) {
                rateType = chars;
            } else if (localName.equalsIgnoreCase("SpecialEquipment")) {
                specialEquipment = chars;
            } else if (localName.equalsIgnoreCase("StartAirportCity")) {
                startAirportCity = chars;
            } else if (localName.equalsIgnoreCase("StartAirportCountry")) {
                startAirportCountry = chars;
            } else if (localName.equalsIgnoreCase("StartAirportCountryCode")) {
                startAirportCountryCode = chars;
            } else if (localName.equalsIgnoreCase("StartAirportName")) {
                startAirportName = chars;
            } else if (localName.equalsIgnoreCase("StartAirportState")) {
                startAirportState = chars;
            } else if (localName.equalsIgnoreCase("StartLocation")) {
                startLocation = chars;
            } else if (localName.equalsIgnoreCase("Transmission")) {
                transmission = chars;
            } else if (localName.equalsIgnoreCase("TransmissionLocalized")) {
                transmissionLocalized = chars;
            }
        }

        return true;
    }
}
