package com.concur.mobile.core.travel.data;

public class DiningSegment extends Segment {

    /**
     * Default constructor does nothing but set the type
     */
    public DiningSegment() {
        type = SegmentType.DINING;
    }

    public String reservationId;

    @Override
    protected boolean handleSegmentElement(String localName, String chars) {

        if (!super.handleSegmentElement(localName, chars)) {
            if (localName.equalsIgnoreCase("ReservationId")) {
                reservationId = chars;
            }
        }

        return true;
    }
}
