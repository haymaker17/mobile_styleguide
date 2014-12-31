package com.concur.mobile.platform.travel.location;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

public class LocationChoice extends BaseParser {

    private static final String CLS_TAG = "Location";

    private static final String TAG_CITY = "City";
    private static final String TAG_COUNTRY = "Country";
    private static final String TAG_COUNTRY_ABBREV = "CountryAbbrev";
    private static final String TAG_IATA = "Iata";
    private static final String TAG_LAT = "Lat";
    private static final String TAG_LOCATION = "Location";
    private static final String TAG_LON = "Lon";
    private static final String TAG_STATE = "State";

    private static final int CODE_CITY = 0;
    private static final int CODE_COUNTRY = 1;
    private static final int CODE_COUNTRY_ABBREV = 2;
    private static final int CODE_IATA = 3;
    private static final int CODE_LAT = 4;
    private static final int CODE_LOCATION = 5;
    private static final int CODE_LON = 6;
    private static final int CODE_STATE = 7;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_CITY, CODE_CITY);
        tagMap.put(TAG_COUNTRY, CODE_COUNTRY);
        tagMap.put(TAG_COUNTRY_ABBREV, CODE_COUNTRY_ABBREV);
        tagMap.put(TAG_IATA, CODE_IATA);
        tagMap.put(TAG_LAT, CODE_LAT);
        tagMap.put(TAG_LOCATION, CODE_LOCATION);
        tagMap.put(TAG_LON, CODE_LON);
        tagMap.put(TAG_STATE, CODE_STATE);
    }

    public String city;

    public String country;

    public String countryAbbrev;

    public String iata;

    public Double lat;

    public String location;

    public Double lon;

    public String state;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            if (text != null) {
                switch (tagCode) {
                case CODE_CITY: {
                    if (!TextUtils.isEmpty(text)) {
                        text = text.trim();
                    }
                    city = text;
                    break;
                }
                case CODE_COUNTRY: {
                    if (!TextUtils.isEmpty(text)) {
                        text = text.trim();
                    }
                    country = text;
                    break;
                }
                case CODE_COUNTRY_ABBREV: {
                    if (!TextUtils.isEmpty(text)) {
                        text = text.trim();
                    }
                    countryAbbrev = text;
                    break;
                }
                case CODE_IATA: {
                    if (!TextUtils.isEmpty(text)) {
                        text = text.trim();
                    }
                    iata = text;
                    break;
                }
                case CODE_LAT: {
                    if (!TextUtils.isEmpty(text)) {
                        text = text.trim();
                    }
                    lat = Parse.safeParseDouble(text);
                    break;
                }
                case CODE_LOCATION: {
                    if (!TextUtils.isEmpty(text)) {
                        text = text.trim();
                    }
                    location = text;
                    break;
                }
                case CODE_LON: {
                    if (!TextUtils.isEmpty(text)) {
                        text = text.trim();
                    }
                    lon = Parse.safeParseDouble(text);
                    break;
                }
                case CODE_STATE: {
                    if (!TextUtils.isEmpty(text)) {
                        text = text.trim();
                    }
                    state = text;
                    break;
                }
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }
}
