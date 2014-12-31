package com.concur.mobile.eva.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.concur.mobile.eva.util.Const;

/**
 * Object representing a Location Geo Attribute from the Eva JSON response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class GeoAttribute {

    public static final String CLS_TAG = GeoAttribute.class.getSimpleName();

    public boolean airport;
    public String distanceUnit;
    public String distanceValue;

    public GeoAttribute(JSONObject geoAttribute) {

        if (geoAttribute.has("Airport")) {
            try {
                airport = geoAttribute.getBoolean("Airport");
            } catch (JSONException e) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".GeoAttribute() - Problem parsing 'Airport'", e);
            }
        }

        if (geoAttribute.has("Distance")) {
            try {
                JSONObject distance = geoAttribute.getJSONObject("Distance");
                if (distance != null) {
                    distanceUnit = distance.getString("Units");
                    distanceValue = distance.getString("Quantity");
                }
            } catch (JSONException e) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".GeoAttribute() - Problem parsing 'Distance'", e);
            }
        }
    }

} // end RequestAttributes
