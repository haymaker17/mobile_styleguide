package com.concur.mobile.core.travel.data;

import com.concur.mobile.platform.util.Parse;

public class RideSegment extends Segment {

    /**
     * Default constructor does nothing but set the type
     */
    public RideSegment() {
        type = SegmentType.RIDE;
    }

    public String cancellationPolicy;
    public String dropoffInstructions;
    public Integer duration;
    public String meetingInstructions;
    public Integer miles;
    public Float numberOfHours;
    public String pickupInstructions;
    public Double rate;
    public String rateDescription;
    public String rateType;
    public String startLocation;
    public String startLocationCode;
    public String startLocationName;

    @Override
    protected boolean handleSegmentElement(String localName, String chars) {

        if (!super.handleSegmentElement(localName, chars)) {
            if (localName.equalsIgnoreCase("CancellationPolicy")) {
                cancellationPolicy = chars;
            } else if (localName.equalsIgnoreCase("DropoffInstructions")) {
                dropoffInstructions = chars;
            } else if (localName.equalsIgnoreCase("Duration")) {
                duration = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("MeetingInstructions")) {
                meetingInstructions = chars;
            } else if (localName.equalsIgnoreCase("Miles")) {
                miles = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("NumberOfHours")) {
                numberOfHours = Parse.safeParseFloat(chars);
            } else if (localName.equalsIgnoreCase("PickupInstructions")) {
                pickupInstructions = chars;
            } else if (localName.equalsIgnoreCase("Rate")) {
                rate = Parse.safeParseDouble(chars);
            } else if (localName.equalsIgnoreCase("RateDescription")) {
                rateDescription = chars;
            } else if (localName.equalsIgnoreCase("RateType")) {
                rateType = chars;
            } else if (localName.equalsIgnoreCase("StartLocation")) {
                startLocation = chars;
            } else if (localName.equalsIgnoreCase("StartLocationCode")) {
                startLocationCode = chars;
            } else if (localName.equalsIgnoreCase("StartLocationName")) {
                startLocationName = chars;
            }
        }

        return true;
    }
}
