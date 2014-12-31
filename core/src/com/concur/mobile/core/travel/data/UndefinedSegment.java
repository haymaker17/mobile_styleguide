package com.concur.mobile.core.travel.data;

public class UndefinedSegment extends Segment {

    /**
     * Default constructor does nothing but set the type
     */
    public UndefinedSegment() {
        type = SegmentType.UNDEFINED;
    }

    @Override
    protected boolean handleSegmentElement(String localName, String chars) {

        if (!super.handleSegmentElement(localName, chars)) {
        }

        return true;
    }
}
