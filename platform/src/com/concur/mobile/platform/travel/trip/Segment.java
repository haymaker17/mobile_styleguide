package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing a segment object.
 */
public class Segment extends BaseParser {

    private static final String CLS_TAG = "Segment";

    private static final String TAG_SEGMENT = "Segment";

    private static final String ATR_TYPE = "type";

    // ATR values.
    private static final String ATR_VAL_AIR = "Air";
    private static final String ATR_VAL_CAR = "Car";
    private static final String ATR_VAL_DINING = "Dining";
    private static final String ATR_VAL_EVENT = "Event";
    private static final String ATR_VAL_HOTEL = "Hotel";
    private static final String ATR_VAL_PARKING = "Parking";
    private static final String ATR_VAL_RAIL = "Rail";
    private static final String ATR_VAL_RIDE = "Ride";

    // ATR codes.
    private static final int ATR_VAL_AIR_CODE = 0;
    private static final int ATR_VAL_CAR_CODE = 1;
    private static final int ATR_VAL_DINING_CODE = 2;
    private static final int ATR_VAL_EVENT_CODE = 3;
    private static final int ATR_VAL_HOTEL_CODE = 4;
    private static final int ATR_VAL_PARKING_CODE = 5;
    private static final int ATR_VAL_RAIL_CODE = 6;
    private static final int ATR_VAL_RIDE_CODE = 7;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> atrMap;

    static {
        // Initialize the map from tags to integer codes.
        atrMap = new HashMap<String, Integer>();
        atrMap.put(ATR_VAL_AIR, ATR_VAL_AIR_CODE);
        atrMap.put(ATR_VAL_CAR, ATR_VAL_CAR_CODE);
        atrMap.put(ATR_VAL_DINING, ATR_VAL_DINING_CODE);
        atrMap.put(ATR_VAL_EVENT, ATR_VAL_EVENT_CODE);
        atrMap.put(ATR_VAL_HOTEL, ATR_VAL_HOTEL_CODE);
        atrMap.put(ATR_VAL_PARKING, ATR_VAL_PARKING_CODE);
        atrMap.put(ATR_VAL_RAIL, ATR_VAL_RAIL_CODE);
        atrMap.put(ATR_VAL_RIDE, ATR_VAL_RIDE_CODE);
    }

    /**
     * Contains the segment data.
     */
    public SegmentData segmentData;

    /**
     * Contains a reference to the common parser.
     */
    private CommonParser parser;

    /**
     * Constructs an instance of <code>Segment</code> for the purpose of parsing a segment.
     * 
     * @param parser
     *            contains a reference to a common parser object.
     * @param startTag
     *            contains the start tag for this parser.
     */
    public Segment(CommonParser parser, String startTag) {
        this.parser = parser;
    }

    @Override
    public void startTag(String tag) {
        if (segmentData == null) {
            if (!TextUtils.isEmpty(tag)) {
                if (tag.equalsIgnoreCase(TAG_SEGMENT)) {
                    segmentData = createSegmentData(tag);
                }
            }
        }
        if (segmentData != null) {
            segmentData.startTag(tag);
        }
    }

    private SegmentData createSegmentData(String tag) {
        SegmentData segmentData = null;
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_SEGMENT)) {
                XmlPullParser xpp = parser.getXmlPullParser();
                if (xpp != null) {
                    int attrCount = xpp.getAttributeCount();
                    for (int attrInd = 0; attrInd < attrCount; ++attrInd) {
                        String attrName = xpp.getAttributeName(attrInd);
                        if (attrName.equalsIgnoreCase(ATR_TYPE)) {
                            String atrValue = xpp.getAttributeValue(attrInd);
                            Integer atrValCode = atrMap.get(atrValue.trim());
                            if (atrValCode != null) {
                                switch (atrValCode) {
                                case ATR_VAL_AIR_CODE: {
                                    segmentData = new AirSegmentData();
                                    break;
                                }
                                case ATR_VAL_CAR_CODE: {
                                    segmentData = new CarSegmentData();
                                    break;
                                }
                                case ATR_VAL_DINING_CODE: {
                                    segmentData = new DiningSegmentData();
                                    break;
                                }
                                case ATR_VAL_EVENT_CODE: {
                                    segmentData = new EventSegmentData();
                                    break;
                                }
                                case ATR_VAL_HOTEL_CODE: {
                                    segmentData = new HotelSegmentData();
                                    break;
                                }
                                case ATR_VAL_PARKING_CODE: {
                                    segmentData = new ParkingSegmentData();
                                    break;
                                }
                                case ATR_VAL_RAIL_CODE: {
                                    segmentData = new RailSegmentData();
                                    break;
                                }
                                case ATR_VAL_RIDE_CODE: {
                                    segmentData = new RideSegmentData();
                                    break;
                                }
                                }
                            } else {
                                if (Const.DEBUG_PARSING) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".startTag: unknown value of '" + atrValue
                                            + "' for 'type' attribute.");
                                }
                            }
                        }
                    }
                } else {
                    if (Const.DEBUG_PARSING) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".startTag: xpp is null.");
                    }
                }
                if (segmentData == null) {
                    if (Const.DEBUG_PARSING) {
                        Log.w(Const.LOG_TAG, CLS_TAG
                                + ".startTag: unable to determine segment type, using 'undefined' type.");
                    }
                }
            }
        }
        return segmentData;
    }

    @Override
    public void handleText(String tag, String text) {
        if (segmentData != null) {
            if (!segmentData.handleSegmentText(tag, text)) {
                if (Const.DEBUG_PARSING) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'");
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".unexpected tag '" + tag + "'");
            }
        }
    }

    @Override
    public void endTag(String tag) {
        if (segmentData != null) {
            segmentData.endTag(tag);
        } else {
            if (Const.DEBUG_PARSING) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".unexpected tag '" + tag + "'");
            }
        }
    }

}
