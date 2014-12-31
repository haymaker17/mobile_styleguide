package com.concur.mobile.core.travel.rail.data;

import android.os.Bundle;

import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.platform.util.Parse;

public class RailStation extends LocationChoice {

    protected static final String KEY_COUNTRY_CODE = "CountryCode";
    protected static final String KEY_COUNTRY_NAME = "CountryName";
    protected static final String KEY_STATION_CODE = "StationCode";
    protected static final String KEY_STATION_NAME = "StationName";
    protected static final String KEY_IATA_CODE = "IataCode";
    protected static final String KEY_TIMEZONE_ID = "TimeZoneId";

    public String countryCode;
    public String countryName;
    public String stationCode;
    public String stationName;
    public String iataCode;
    public int timeZoneId;

    public RailStation() {
    }

    public RailStation(Bundle b) {
        super(b);
        if (b != null) {
            city = b.getString(KEY_CITY);
            countryCode = b.getString(KEY_COUNTRY_CODE);
            countryName = b.getString(KEY_COUNTRY_NAME);
            state = b.getString(KEY_STATE);
            stationCode = b.getString(KEY_STATION_CODE);
            stationName = b.getString(KEY_STATION_NAME);
            iataCode = b.getString(KEY_IATA_CODE);
            timeZoneId = b.getInt(KEY_TIMEZONE_ID);
        }
    }

    public Bundle getBundle() {
        Bundle b = super.getBundle();
        b.putString(KEY_CITY, city);
        b.putString(KEY_COUNTRY_CODE, countryCode);
        b.putString(KEY_COUNTRY_NAME, countryName);
        b.putString(KEY_STATE, state);
        b.putString(KEY_STATION_CODE, stationCode);
        b.putString(KEY_STATION_NAME, stationName);
        b.putString(KEY_IATA_CODE, iataCode);
        b.putInt(KEY_TIMEZONE_ID, timeZoneId);

        return b;
    }

    @Override
    public String getName() {
        if (name == null || name.trim().length() == 0) {
            StringBuilder sb = new StringBuilder("(");
            sb.append(stationCode).append(") ").append(stationName);
            name = sb.toString();
        }

        return name;
    }

    /**
     * Will examine the attribute <code>localName</code> and assign the value in <code>cleanChars</code>.
     * 
     * @param localName
     *            the attribute name.
     * @param value
     *            the attribute value trimmed of whitespace.
     */
    public boolean handleElement(String localName, String value) {
        boolean attrSet = super.handleElement(localName, value);

        if (!attrSet) {
            if (localName.equalsIgnoreCase(KEY_CITY)) {
                city = value;
            } else if (localName.equalsIgnoreCase(KEY_COUNTRY_CODE)) {
                countryCode = value;
            } else if (localName.equalsIgnoreCase(KEY_COUNTRY_NAME)) {
                countryName = value;
            } else if (localName.equalsIgnoreCase(KEY_STATE)) {
                state = value;
            } else if (localName.equalsIgnoreCase(KEY_STATION_CODE)) {
                stationCode = value;
            } else if (localName.equalsIgnoreCase(KEY_STATION_NAME)) {
                stationName = value;
            } else if (localName.equalsIgnoreCase(KEY_IATA_CODE)) {
                iataCode = value;
            } else if (localName.equalsIgnoreCase(KEY_TIMEZONE_ID)) {
                timeZoneId = Parse.safeParseInteger(value);
            } else {
                attrSet = false;
            }
        }

        return attrSet;
    }

}
