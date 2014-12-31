package com.concur.mobile.core.travel.data;

public class ParkingSegment extends Segment {

    /**
     * Default constructor does nothing but set the type
     */
    public ParkingSegment() {
        type = SegmentType.PARKING;
    }

    public String parkingLocationId;
    public String pin;
    public String startLocation;

    @Override
    protected boolean handleSegmentElement(String localName, String chars) {

        if (!super.handleSegmentElement(localName, chars)) {
            if (localName.equalsIgnoreCase("ParkingLocationId")) {
                parkingLocationId = chars;
            } else if (localName.equalsIgnoreCase("Pin")) {
                pin = chars;
            } else if (localName.equalsIgnoreCase("StartLocation")) {
                startLocation = chars;
            }
        }

        return true;
    }

    // HACK HACK HACK HACK
    // HACK HACK HACK HACK
    //
    // Return a usable vendor name. Only use until the service gives us one.
    //
    // HACK HACK HACK HACK
    // HACK HACK HACK HACK
    public String getVendorName() {
        if ("PF".equals(vendor)) {
            return "Park 'N Fly";
        } else {
            return vendorName;
        }
    }
}
