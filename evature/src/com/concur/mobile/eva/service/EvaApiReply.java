package com.concur.mobile.eva.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.concur.mobile.eva.data.CarAttributes;
import com.concur.mobile.eva.data.EvaLocation;
import com.concur.mobile.eva.data.EvaMoney;
import com.concur.mobile.eva.data.FlightAttributes;
import com.concur.mobile.eva.util.Const;

/**
 * Object representing an Eva response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class EvaApiReply {

    private final static String CLS_TAG = EvaApiReply.class.getSimpleName();
    protected final static String SUCCESSFUL_PARSE = "Successful Parse";
    protected final static String PARTIAL_PARSE = "Partial Parse";

    public String sayIt = null;
    public EvaLocation[] locations;
    public EvaLocation[] alt_locations;
    public Map<String, String> ean = null;
    public FlightAttributes flightAttributes = null;
    public CarAttributes carAttributes = null;
    public EvaMoney money;

    protected String message;
    protected boolean status;

    protected JSONObject jFullReply;

    /**
     * Constructor that parses the XML reply.
     * 
     * @param fullReply
     *            the XML reply from the Eva web service.
     */
    public EvaApiReply(String fullReply) {

        Log.d(Const.LOG_TAG, CLS_TAG + " - Constructor");

        try {
            jFullReply = new JSONObject(fullReply);
            status = jFullReply.getBoolean("status");
            message = jFullReply.getString("message");
            if (status) {
                JSONObject jApiReply = jFullReply.getJSONObject("api_reply");
                Log.d(CLS_TAG, "api_reply: " + jApiReply.toString(2));
                if (jApiReply.has("Say It"))
                    sayIt = jApiReply.getString("Say It");
                if (jApiReply.has("Locations")) {
                    JSONArray jLocations = jApiReply.getJSONArray("Locations");
                    locations = new EvaLocation[jLocations.length()];

                    for (int index = 0; index < jLocations.length(); index++) {
                        locations[index] = new EvaLocation(jLocations.getJSONObject(index));
                    }
                }
                if (jApiReply.has("Alt Locations")) {
                    JSONArray jLocations = jApiReply.getJSONArray("Alt Locations");
                    alt_locations = new EvaLocation[jLocations.length()];
                    for (int index = 0; index < jLocations.length(); index++) {
                        alt_locations[index] = new EvaLocation(jLocations.getJSONObject(index));
                    }
                }
                if (jApiReply.has("ean")) {
                    JSONObject jEan = jApiReply.getJSONObject("ean");
                    @SuppressWarnings("unchecked")
                    Iterator<String> nameItr = jEan.keys();
                    ean = new HashMap<String, String>();
                    while (nameItr.hasNext()) {
                        String key = nameItr.next().toString();
                        String value = jEan.getString(key);
                        ean.put(key, value);
                    }
                }

                if (jApiReply.has("Flight Attributes")) {
                    flightAttributes = new FlightAttributes(jApiReply.getJSONObject("Flight Attributes"));
                }

                if (jApiReply.has("Car Attributes")) {
                    carAttributes = new CarAttributes(jApiReply.getJSONObject("Car Attributes"));
                }

                if (jApiReply.has("EvaMoney")) {
                    money = new EvaMoney(jApiReply.getJSONObject("EvaMoney"));
                }

            } // end if-status

        } catch (Exception e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".EvaApiReply() - Bad EVA reply!", e);

            // Override the status to be unsuccessful.
            status = false;
        }
    }

    /**
     * 
     * @return <code>true</code> if this is a hotel/room search.
     */
    public boolean isHotelSearch() {
        if (locations != null && locations.length > 1) {
            if ((locations[0].requestAttributes != null)
                    && ((locations[0].requestAttributes.transportType.contains("Train") || locations[0].requestAttributes.transportType
                            .contains("Airplane")))) {
                return false; // It is a train search!
            }
            if (locations[1].actions == null) {
                return true;
            } else {
                List<String> actions = Arrays.asList(locations[1].actions);
                if (actions.contains("Get Accommodation"))
                    return true;
            }
        }
        return false;
    }

    /**
     * 
     * @return <code>true</code> if this is an air/flight search.
     */
    public boolean isFlightSearch() {
        if (locations != null && locations.length > 1) {
            if (locations[0].requestAttributes != null) {
                if (locations[0].requestAttributes.transportType.contains("Airplane")) {
                    return true; // It is an Air search!
                }
                if (locations[0].requestAttributes.transportType.contains("Train")
                        || locations[0].requestAttributes.transportType.contains("Car")) {
                    return false;
                }
            }
            if (locations[1].actions == null) {
                return true;
            } else {
                List<String> actions = Arrays.asList(locations[1].actions);
                if (actions.contains("Get There"))
                    return true;
            }
        }
        return false;
    }

    /**
     * 
     * @return <code>true</code> if this is a train/rail search.
     */
    public boolean isTrainSearch() {
        if (locations != null && locations.length > 1) {
            if ((locations[0].requestAttributes != null)
                    && (locations[0].requestAttributes.transportType.contains("Train"))) {
                return true; // It is a train search!
            }
        }
        return false;
    }

    /**
     * 
     * @return <code>true</code> if this is a car rental search.
     */
    public boolean isCarSearch() {

        if (locations != null && locations.length > 0) {

            EvaLocation loc = locations[0];

            if (loc.requestAttributes != null && loc.requestAttributes.transportType.contains("Car")) {
                return true; // It is a car search!
            }

            if (loc.actions != null) {
                for (String action : loc.actions) {
                    if (action.equalsIgnoreCase("Pickup Car") || action.equalsIgnoreCase("Return Car")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 
     * @return <code>true</code> if the Eva call successfully parsed the text.
     */
    public boolean isSuccessfulParse() {
        boolean result = (status == true && message != null && (message.equalsIgnoreCase(SUCCESSFUL_PARSE) || (message
                .equalsIgnoreCase(PARTIAL_PARSE) && (isHotelSearch() || isFlightSearch() || isTrainSearch() || isCarSearch()))));

        if (!result) {
            Log.w(Const.LOG_TAG, CLS_TAG + ".isSuccessfulParse() - false: " + message);
        }

        return result;
    }

} // end EvaApiReply

