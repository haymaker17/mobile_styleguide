package com.concur.mobile.core.travel.data;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;

public class LocationChoice implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -760396805621134093L;

    public final static String LOCATION_BUNDLE = "location_bundle";

    public final static String KEY_NAME = "Location";
    public final static String KEY_LATITUDE = "Lat";
    public final static String KEY_LONGITUDE = "Lon";
    public final static String KEY_COUNTRY_ABBREV = "CountryAbbrev";
    public final static String KEY_IATA = "Iata";
    protected final static String KEY_CITY = "City";
    protected final static String KEY_STATE = "State";

    protected String name;
    public String latitude;
    public String longitude;
    public String countryAbbrev;
    public String iata;
    public String city;
    public String state;

    public LocationChoice() {
    }

    public LocationChoice(Bundle b) {
        if (b != null) {
            name = b.getString(KEY_NAME);
            latitude = b.getString(KEY_LATITUDE);
            longitude = b.getString(KEY_LONGITUDE);
            countryAbbrev = b.getString(KEY_COUNTRY_ABBREV);
            iata = b.getString(KEY_IATA);
            city = b.getString(KEY_CITY);
            state = b.getString(KEY_STATE);
        }
    }

    public Bundle getBundle() {
        Bundle b = new Bundle();
        b.putString(KEY_NAME, name);
        b.putString(KEY_LATITUDE, latitude);
        b.putString(KEY_LONGITUDE, longitude);
        b.putString(KEY_COUNTRY_ABBREV, countryAbbrev);
        b.putString(KEY_IATA, iata);
        b.putString(KEY_CITY, city);
        b.putString(KEY_STATE, state);
        return b;
    }

    /**
     * Attempt to return the IATA code from the location name. If unable, return the whole name.
     */
    public String getIATACode() {
        if (iata != null && iata.trim().length() > 0) {
            return iata;
        }
        // HACK HACK HACK HACK HACK HACK
        String code = name;

        Pattern pat = Pattern.compile(".*\\(([A-Z]{3})\\)$");
        Matcher match = pat.matcher(code);
        if (match.lookingAt()) {
            code = match.group(1);
        }

        return code;
        // HACK HACK HACK HACK HACK HACK
    }

    /**
     * Return the name for this location. Allows sub-classes without names to override and create a name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean handleElement(String localName, String cleanChars) {

        boolean handled = true;

        if (localName.equalsIgnoreCase(KEY_NAME)) {
            name = cleanChars;
        } else if (localName.equalsIgnoreCase(KEY_LATITUDE)) {
            latitude = cleanChars;
        } else if (localName.equalsIgnoreCase(KEY_LONGITUDE)) {
            longitude = cleanChars;
        } else if (localName.equalsIgnoreCase(KEY_COUNTRY_ABBREV)) {
            countryAbbrev = cleanChars;
        } else if (localName.equalsIgnoreCase(KEY_IATA)) {
            iata = cleanChars;
        } else if (localName.equalsIgnoreCase(KEY_CITY)) {
            handled = false;
            city = cleanChars;
        } else if (localName.equalsIgnoreCase(KEY_STATE)) {
            handled = false;
            state = cleanChars;
        } else {
            handled = false;
        }

        return handled;
    }

}
