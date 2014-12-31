package com.concur.mobile.eva.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.concur.mobile.eva.util.Const;

/**
 * Object representing a Location from the Eva JSON response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class EvaLocation {

    private final String CLS_TAG = EvaLocation.class.getSimpleName();

    /**
     * A number representing the location index in the trip. Index numbers usually progress with the duration of the trip (so a
     * location with index 11 is visited before a location with index 21). An index number is unique for a locations in Locations
     * (unless the same location visited multiple times, for example home location at start and end of trip will have the same
     * index) but <code>Alt Locations</code> may have multiple locations with the same index, indicating alternatives for the same
     * part of a trip. Index numbers are not serial, so indexes can be (0,1,11,21,22, etc.). Index number <code>0</code> is unique
     * and always represents the home location.
     */
    public int index;

    /**
     * The index number of the location in a trip, if known. Default to -1.
     */
    public int next = -1;

    /**
     * Will be present in cities that have an <code>all airports</code> IATA code e.g. San Francisco, New York, etc.
     */
    public String allAirportCode;

    /**
     * If a location is not an airport, this key provides 5 recommended airports for this location. Airports are named by their
     * IATA code.
     */
    public List<String> airports = null;

    /**
     * The location type. Example values: Continent, Country, City, Island, Airport.
     */
    public String type;

    /**
     * A global identifier for the location. IATA code for airports and Geoname ID for other locations. Note: if Geoname ID is not
     * defined for a location, a string representing the name of the location will be given in as value instead. The format of
     * this name is currently not set and MAY CHANGE. If you plan to use this field, please contact us.
     */
    public String geoId;

    /**
     * Provides a list of actions requested for this location. Actions can include the following values: <code>Get There</code>
     * (request any way to be transported there, mostly flights but can be train, bus etc.), <code>Get Accommodation</code>,
     * <code>Get Car</code>.
     */
    public String[] actions;

    /**
     * There are many general request attributes that apply to the entire request and not just some portion of it. Examples:
     * <code>last minute deals</code> and <code>Low deposits</code>.
     */
    public RequestAttributes requestAttributes = null;

    /**
     * Represents an array of GeoAttributes of the Location. e.g. <code>"Airport:true"</code>
     */
    public GeoAttribute geoAttributes = null;

    /**
     * Complex Eva Time object
     */
    public EvaTime departureTime = null;

    /**
     * Complex Eva Time object
     */
    public EvaTime arrivalTime = null;

    /**
     * Complex Eva Time object for picking up Car.
     */
    public EvaTime pickupTime = null;

    /**
     * Complex Eva Time object for return Car.
     */
    public EvaTime returnTime = null;

    // public GeoAttributes

    /**
     * The number of days a car is rented out.
     * 
     */
    public int rentalCarDuration = -1;

    public JSONObject[] stay;

    public String longitude;
    public String latitude;
    public String name;
    public String country;

    /**
     * Complex Hotel Attributes object.
     */
    public HotelAttributes hotelAttributes = null;

    /**
     * Constructor that parses the given JSON object representing an Eva Location.
     * 
     * @param location
     *            JSON object representing an Eva Location.
     */
    public EvaLocation(JSONObject location) {
        try {

            index = location.getInt("Index");

            if (location.has("Next")) {
                next = location.getInt("Next");
            }

            if (location.has("All Airports Code")) {
                allAirportCode = location.getString("All Airports Code");
            }

            if (location.has("Geoid")) {
                try {
                    geoId = location.getString("Geoid");
                } catch (JSONException e) {
                    geoId = String.valueOf(location.getInt("Geoid"));
                }
            }

            if (location.has("Name")) {
                name = location.getString("Name");
                if (name != null && name.indexOf("(GID") != -1) {
                    name = name.substring(0, name.indexOf("(GID"));
                } else if (name != null && name.indexOf(" = ") != -1) {
                    name = name.substring(name.indexOf(" = ") + 3);
                }
            }

            if (location.has("Country")) {
                country = location.getString("Country");
            }

            if (location.has("Type")) {
                type = location.getString("Type");
            }

            if (location.has("Longitude")) {
                longitude = location.getString("Longitude");
            }

            if (location.has("Latitude")) {
                latitude = location.getString("Latitude");
            }

            if (location.has("Arrival")) {
                arrivalTime = new EvaTime(location.getJSONObject("Arrival"));
            }

            if (location.has("Departure")) {
                departureTime = new EvaTime(location.getJSONObject("Departure"));
            }

            if (location.has("Pickup Car")) {
                pickupTime = new EvaTime(location.getJSONObject("Pickup Car"));
            }

            if (location.has("Return Car")) {
                returnTime = new EvaTime(location.getJSONObject("Return Car"));
            }

            if (location.has("Stay")) {

                Object stayObj = location.get("Stay");
                if (stayObj instanceof JSONArray) {
                    JSONArray jStay = (JSONArray) stayObj;
                    stay = new JSONObject[jStay.length()];
                    for (int index = 0; index < jStay.length(); index++) {
                        stay[index] = jStay.getJSONObject(index);
                    }
                } else if (stayObj instanceof JSONObject) {
                    // Only one value in the Stay element.
                    stay = new JSONObject[1];
                    stay[0] = (JSONObject) stayObj;
                }
            }

            if (location.has("Actions")) {
                JSONArray jActions = location.getJSONArray("Actions");
                actions = new String[jActions.length()];
                for (int index = 0; index < jActions.length(); index++) {
                    actions[index] = new String(jActions.getString(index));
                }
            }

            if (location.has("Airports")) {
                airports = new ArrayList<String>();
                String[] temp = location.getString("Airports").split(",");
                for (int i = 0; i < temp.length; i++) {
                    airports.add(temp[i]);
                }
            }

            if (location.has("Request Attributes")) {
                requestAttributes = new RequestAttributes(location.getJSONObject("Request Attributes"));
            }

            if (location.has("Geo Attributes")) {
                geoAttributes = new GeoAttribute(location.getJSONObject("Geo Attributes"));
            }

            if (location.has("Hotel Attributes")) {
                hotelAttributes = new HotelAttributes(location.getJSONObject("Hotel Attributes"));
            }

            if (location.has("Rental Car Duration")) {
                String delta = location.getJSONObject("Rental Car Duration").getString("Delta");
                if (delta != null) {
                    int index = delta.indexOf("+");
                    if (index != -1) {
                        try {
                            rentalCarDuration = Integer.parseInt(delta.substring(index + 1));
                        } catch (NumberFormatException e) {
                            Log.w(Const.LOG_TAG, CLS_TAG + ".EvaLocation()  - couldn't parse rental car duration: "
                                    + delta);
                        }
                    }
                }
            }

        } catch (JSONException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".EvaLocation() - Bad EVA reply!", e);
        }

    }

} // end EvaLocation
