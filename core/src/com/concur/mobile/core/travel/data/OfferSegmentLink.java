/**
 * 
 */
package com.concur.mobile.core.travel.data;

/**
 * Models a link from an offer to an itinerary segment.
 */
public class OfferSegmentLink {

    protected static final String SIDE_START_TEXT = "Start";
    protected static final String SIDE_END_TEXT = "End";
    protected static final String SIDE_DURATION_TEXT = "Duration";

    public static final byte SIDE_NONE = 0x0;
    public static final byte SIDE_START = 0x1;
    public static final byte SIDE_END = 0x2;
    public static final byte SIDE_DURATION = 0x4;

    public String bookingSource;

    public String recordLocator;

    public String segmentKey;

    /**
     * Contains what "side" of the segment this offer is related to. Possible values are "Start", "End", or "Duration"
     */
    public byte segmentSide;

    public Segment segment;

    public void setSegmentSide(String side) {
        segmentSide = SIDE_NONE;
        if (side != null) {
            if (SIDE_START_TEXT.equals(side)) {
                segmentSide = SIDE_START;
            } else if (SIDE_END_TEXT.equals(side)) {
                segmentSide = SIDE_END;
            } else if (SIDE_DURATION_TEXT.equals(side)) {
                segmentSide = SIDE_DURATION;
            }
        }
    }
}
