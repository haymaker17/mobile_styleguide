package com.concur.mobile.core.travel.rail.data;

import com.concur.mobile.core.travel.data.Segment;
import com.concur.mobile.platform.util.Parse;

public class RailSegment extends Segment {

    /**
     * Default constructor does nothing but set the type
     */
    public RailSegment() {
        type = SegmentType.RAIL;
    }

    public String amenities;
    public String cabin;
    public String classOfService;
    public String discountCode;
    public Integer duration;
    public String endPlatform;
    public String endRailStation;
    public String endRailStationLocalized;
    public Integer legId;
    public String meals;
    public Integer miles;
    public Integer numStops;
    public String operatedByTrainNumber;
    public String pin;
    public String startPlatform;
    public String startRailStation;
    public String startRailStationLocalized;
    public String trainNumber;
    public String trainTypeCode;
    public String wagonNumber;

    @Override
    protected boolean handleSegmentElement(String localName, String chars) {

        if (!super.handleSegmentElement(localName, chars)) {
            if (localName.equalsIgnoreCase("Amenities")) {
                amenities = chars;
            } else if (localName.equalsIgnoreCase("Cabin")) {
                cabin = chars;
            } else if (localName.equalsIgnoreCase("ClassOfService")) {
                classOfService = chars;
            } else if (localName.equalsIgnoreCase("DiscountCode")) {
                discountCode = chars;
            } else if (localName.equalsIgnoreCase("Duration")) {
                duration = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("EndPlatform")) {
                endPlatform = chars;
            } else if (localName.equalsIgnoreCase("EndRailStation")) {
                endRailStation = chars;
            } else if (localName.equalsIgnoreCase("EndRailStationLocalized")) {
                endRailStationLocalized = chars;
            } else if (localName.equalsIgnoreCase("LegId")) {
                legId = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("Meals")) {
                meals = chars;
            } else if (localName.equalsIgnoreCase("Miles")) {
                miles = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("NumStops")) {
                numStops = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("OperatedByTrainNumber")) {
                operatedByTrainNumber = chars;
            } else if (localName.equalsIgnoreCase("Pin")) {
                pin = chars;
            } else if (localName.equalsIgnoreCase("StartPlatform")) {
                startPlatform = chars;
            } else if (localName.equalsIgnoreCase("StartRailStation")) {
                startRailStation = chars;
            } else if (localName.equalsIgnoreCase("StartRailStationLocalized")) {
                startRailStationLocalized = chars;
            } else if (localName.equalsIgnoreCase("TrainNumber")) {
                trainNumber = chars;
            } else if (localName.equalsIgnoreCase("TrainTypeCode")) {
                trainTypeCode = chars;
            } else if (localName.equalsIgnoreCase("WagonNumber")) {
                wagonNumber = chars;
            }
        }

        return true;
    }
}
