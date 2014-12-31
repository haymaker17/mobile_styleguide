package com.concur.mobile.platform.config.system;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing an <code>OfficeLocation</code> object.
 */
public class OfficeLocation extends BaseParser {

    private static final String CLS_TAG = "OfficeLocation";

    // tags.
    private static final String TAG_ADDRESS = "Address";
    private static final String TAG_CITY = "City";
    private static final String TAG_COUNTRY = "Country";
    private static final String TAG_LAT = "Lat";
    private static final String TAG_LON = "Lon";
    private static final String TAG_STATE = "State";

    // tag codes.
    private static final int TAG_ADDRESS_CODE = 0;
    private static final int TAG_CITY_CODE = 2;
    private static final int TAG_COUNTRY_CODE = 3;
    private static final int TAG_LAT_CODE = 4;
    private static final int TAG_LON_CODE = 5;
    private static final int TAG_STATE_CODE = 6;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_ADDRESS, TAG_ADDRESS_CODE);
        tagMap.put(TAG_CITY, TAG_CITY_CODE);
        tagMap.put(TAG_COUNTRY, TAG_COUNTRY_CODE);
        tagMap.put(TAG_LAT, TAG_LAT_CODE);
        tagMap.put(TAG_LON, TAG_LON_CODE);
        tagMap.put(TAG_STATE, TAG_STATE_CODE);
    }

    /**
     * Contains the address.
     */
    public String address;

    /**
     * Contains the city.
     */
    public String city;

    /**
     * Contains the country.
     */
    public String country;

    /**
     * Contains the latitude.
     */
    public Double lat;

    /**
     * Contains the longitude.
     */
    public Double lon;

    /**
     * Contains the state.
     */
    public String state;

    @Override
    public void handleText(String tag, String text) {

        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_ADDRESS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    address = text.trim();
                }
                break;
            }
            case TAG_CITY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    city = text.trim();
                }
                break;
            }
            case TAG_COUNTRY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    country = text.trim();
                }
                break;
            }
            case TAG_LAT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    lat = Parse.safeParseDouble(text.trim());
                    if (lat == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse office location latitude");
                    }
                }
                break;
            }
            case TAG_LON_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    lon = Parse.safeParseDouble(text.trim());
                    if (lon == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse office location longitude");
                    }
                }
                break;
            }
            case TAG_STATE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    state = text.trim();
                }
                break;
            }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
            }
        }
    }

}
