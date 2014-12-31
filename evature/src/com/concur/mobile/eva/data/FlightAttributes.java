package com.concur.mobile.eva.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.concur.mobile.eva.util.Const;

/**
 * Object representing a Flight Attributes from the Eva JSON response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class FlightAttributes {

    private static final String CLS_TAG = FlightAttributes.class.getSimpleName();

    /**
     * A Non stop flight - Boolean attribute.
     */
    public Boolean nonstop = null;

    /**
     * A Red eye flight - Boolean attribute.
     */
    public Boolean redeye = null;

    /**
     * The request is specifically asking for just-a-flight (and no hotel, car etc.) - Boolean
     */
    public Boolean only = null;

    /**
     * Specific request for one way trip. Example: <code>united airlines one way flights to ny</code>
     */
    public Boolean oneWay = null;

    /**
     * Specific request for round trip. Example: <code>3 ticket roundtrip from tagbilaran to manila 1/26/2011-1/30/2011</code>
     */
    public Boolean twoWay = null;

    /**
     * Specifies the seat class. Example: "business or first class" results in key value [Business,First]
     */
    public JSONArray seatClass = null;

    /**
     * Constructor - parses the given JSON object representing an Eva Flight
     * 
     * @param flightAttributes
     *            JSON object representing an Eva Flight
     */
    public FlightAttributes(JSONObject flightAttributes) {
        try {
            if (flightAttributes.has("Nonstop"))
                nonstop = flightAttributes.getBoolean("Nonstop");
            if (flightAttributes.has("Redeye"))
                redeye = flightAttributes.getBoolean("Redeye");
            if (flightAttributes.has("Only"))
                only = flightAttributes.getBoolean("Only");
            if (flightAttributes.has("Two-Way"))
                twoWay = flightAttributes.getBoolean("Two-Way");
            if (flightAttributes.has("Seat Class")) {
                seatClass = flightAttributes.getJSONArray("Seat Class");
            }

        } catch (JSONException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".FlightAttributes() - Error Parsing JSON", e);
        }
    }

} // end FlightAttributes
