/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.util.Const;

/**
 * Models a search option specific to a city with no dates.
 * 
 * @author AndrewK
 */
public class CitySearchSuggestion extends SearchSuggestion {

    private static final String CLS_TAG = CitySearchSuggestion.class.getSimpleName();

    public String city;

    public String state;

    public String country;

    public String address;

    protected boolean requireRailStationInformation;

    public String railStationCode;

    public String displayText;

    public String iataCode;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#requiresRailStations()
     */
    @Override
    public boolean requiresRailStations() {
        return requireRailStationInformation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#getDisplayText()
     */
    @Override
    public String getDisplayText(ConcurCore concurMobile) {
        if (displayText == null) {
            StringBuilder strBldr = new StringBuilder();
            if (!requireRailStationInformation) {
                if (city != null) {
                    strBldr.append(city);
                }
            } else {
                // Obtain city, state and country information from a rail station map.
                Map<String, RailStation> codeRailStationMap = concurMobile.getCodeRailStationMap();
                if (codeRailStationMap != null) {
                    RailStation railStation = codeRailStationMap.get(railStationCode);
                    if (railStation != null) {
                        city = railStation.city;
                        state = railStation.state;
                        country = railStation.countryName;
                        strBldr.append(city);
                    } else {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".getDisplayText: no mapping for rail code '" + railStationCode
                                + "'.");
                    }
                } else {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".getDisplayText: railcode to rail station map is null!");
                }
            }
            displayText = strBldr.toString();
        }
        return displayText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#getStartLocationChoice(android.content.Context)
     */
    public LocationChoice getStartLocationChoice(Context context) {
        LocationChoice locChoice = null;
        // Use the built-in Geocoder to convert a city, state & country tuple into
        // an Address object.
        Geocoder geoCoder = new Geocoder(context);
        StringBuilder strBldr = new StringBuilder();
        if (city != null) {
            strBldr.append(city);
        }
        if (state != null) {
            if (strBldr.length() > 0) {
                strBldr.append(',');
            }
            strBldr.append(state);
        }
        if (country != null) {
            if (strBldr.length() > 0) {
                strBldr.append(',');
            }
            strBldr.append(country);
        }
        String locationName;
        if (iataCode == null) {
            locationName = strBldr.toString().trim();
        } else {
            // in case of Car search, we need the latitude and longitude of the airport not the city
            locationName = iataCode;
        }

        if (locationName != null && locationName.length() > 0) {
            try {
                List<Address> locations = geoCoder.getFromLocationName(locationName, 10);
                for (Address address : locations) {
                    if (address.hasLatitude() && address.hasLongitude()) {
                        locChoice = new LocationChoice();
                        locChoice.latitude = Double.toString(address.getLatitude());
                        locChoice.longitude = Double.toString(address.getLongitude());
                        // MOB-14727 - in case of Car search, send the iata though it is optional
                        locChoice.iata = iataCode;
                        String stateProvince = address.getAdminArea();
                        locChoice.name = city
                                + ((stateProvince != null && stateProvince.trim().length() > 0) ? ", " + stateProvince
                                        : "") + ", " + country;
                        break;
                    }
                }
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getStartLocationChoice: ", ilaExc);
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getStartLocationChoice: I/O exception retrieving location: ", ioExc);
            }
        }
        return locChoice;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchOption#getStartDate()
     */
    @Override
    public Calendar getStartDate() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#getStopLocationChoice(android.content.Context)
     */
    @Override
    public LocationChoice getStopLocationChoice(Context context) {
        return getStartLocationChoice(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchOption#getStopDate()
     */
    @Override
    public Calendar getStopDate() {
        return null;
    }
}
