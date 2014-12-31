package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

import com.concur.mobile.platform.util.Const;

/**
 * Provides a model for an offer link segment tag with values defined in attributes.
 */
public class LinkSegment {

    private final static String CLS_TAG = "LinkSegment";

    // ATTR tags.
    private static String ATTR_BOOKING_SOURCE = "BookingSource";
    private static String ATTR_RECORD_LOCATOR = "RecordLocator";
    private static String ATTR_SEGMENT_KEY = "SegmentKey";
    private static String ATTR_SEGMENT_SIDE = "SegmentSide";

    // ATTR codes.
    private static final int ATTR_BOOKING_SOURCE_CODE = 0;
    private static final int ATTR_RECORD_LOCATOR_CODE = 1;
    private static final int ATTR_SEGMENT_KEY_CODE = 2;
    private static final int ATTR_SEGMENT_SIDE_CODE = 3;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> atrMap;

    static {
        // Initialize the map from tags to integer codes.
        atrMap = new HashMap<String, Integer>();
        atrMap.put(ATTR_BOOKING_SOURCE, ATTR_BOOKING_SOURCE_CODE);
        atrMap.put(ATTR_RECORD_LOCATOR, ATTR_RECORD_LOCATOR_CODE);
        atrMap.put(ATTR_SEGMENT_KEY, ATTR_SEGMENT_KEY_CODE);
        atrMap.put(ATTR_SEGMENT_SIDE, ATTR_SEGMENT_SIDE_CODE);
    }

    /**
     * Contains the booking source.
     */
    public String bookingSource;

    /**
     * Contains the record locator.
     */
    public String recordLocator;

    /**
     * Contains the segment key.
     */
    public String segmentKey;

    /**
     * Contains the segment side.
     */
    public String segmentSide;

    /**
     * Will examine the current set of tag attribute values and set values.
     * 
     * @param xpp
     *            contains a reference to the pull parser.
     */
    public void setAttributeValues(XmlPullParser xpp) {

        if (xpp != null) {
            int attrCount = xpp.getAttributeCount();
            for (int attrInd = 0; attrInd < attrCount; ++attrInd) {
                String attrName = xpp.getAttributeName(attrInd);
                Integer atrCode = atrMap.get(attrName);
                if (atrCode != null) {
                    String atrValue = xpp.getAttributeValue(attrInd);
                    if (atrValue != null) {
                        switch (atrCode) {
                        case ATTR_BOOKING_SOURCE_CODE: {
                            bookingSource = atrValue.trim();
                            break;
                        }
                        case ATTR_RECORD_LOCATOR_CODE: {
                            recordLocator = atrValue.trim();
                            break;
                        }
                        case ATTR_SEGMENT_KEY_CODE: {
                            segmentKey = atrValue.trim();
                            break;
                        }
                        case ATTR_SEGMENT_SIDE_CODE: {
                            segmentSide = atrValue.trim();
                            break;
                        }
                        }
                    }
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setAttributeValues: xpp is null.");
            }
        }
    }

}
