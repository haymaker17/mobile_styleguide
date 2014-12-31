package com.concur.mobile.core.travel.data;

public class EventSegment extends Segment {

    /**
     * Default constructor does nothing but set the type
     */
    public EventSegment() {
        type = SegmentType.EVENT;
    }

    @Override
    protected boolean handleSegmentElement(String localName, String chars) {

        if (!super.handleSegmentElement(localName, chars)) {
        }

        return true;
    }
}
