package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for parsing a MapDisplay object.
 */
public class MapDisplay extends BaseParser {

    private static final String CLS_TAG = "MapDisplay";

    private static final String TAG_LATITUDE = "Latitude";
    private static final String TAG_LONGITUDE = "Longitude";
    private static final String TAG_DIMENSION_KM = "DimensionKm";

    private static final int TAG_LATITUDE_CODE = 0;
    private static final int TAG_LONGITUDE_CODE = 1;
    private static final int TAG_DIMENSION_KM_CODE = 2;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_LATITUDE, TAG_LATITUDE_CODE);
        tagMap.put(TAG_LONGITUDE, TAG_LONGITUDE_CODE);
        tagMap.put(TAG_DIMENSION_KM, TAG_DIMENSION_KM_CODE);
    }

    /**
     * Contains the latitude.
     */
    public Double latitude;

    /**
     * Contains the longitude.
     */
    public Double longitude;

    /**
     * Contains the dimension in km.
     */
    public Double dimensionKm;

    /**
     * Contains the overlay list.
     */
    public List<Overlay> overlays;

    /**
     * Contains the overlay list parser.
     */
    private ListParser<Overlay> overlayListParser;

    /**
     * Contains this parsers start tag.
     */
    private String startTag;

    public MapDisplay(CommonParser parser, String startTag) {

        this.startTag = startTag;
        // Create and register the overlay list parser.
        String listTag = "OverlayList";
        overlayListParser = new ListParser<Overlay>(listTag, "Overlay", Overlay.class);
        parser.registerParser(overlayListParser, listTag);
    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_LATITUDE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    latitude = Parse.safeParseDouble(text.trim());
                    if (latitude == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse latitude.");
                    }
                }
                break;
            }
            case TAG_LONGITUDE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    longitude = Parse.safeParseDouble(text.trim());
                    if (longitude == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse longitude.");
                    }
                }
                break;
            }
            case TAG_DIMENSION_KM_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    dimensionKm = Parse.safeParseDouble(text.trim());
                    if (dimensionKm == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse dimensionKm.");
                    }
                }
                break;
            }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {
                // Set the overlay list.
                overlays = overlayListParser.getList();
            }
        }
    }

}
