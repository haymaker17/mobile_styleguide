/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.eva.data;

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
public class CarAttributes {

    private static final String CLS_TAG = CarAttributes.class.getSimpleName();

    /**
     * The car type, e.g. "Compact", "Economy", "Luxury", etc.
     */
    public String carType = null;

    /**
     * Flag for smoking or non-smoking cars.
     */
    public boolean smoking = false;

    /**
     * Constructor - parses the given JSON object representing an Eva Car
     * 
     * @param carAttributes
     *            JSON object representing an Eva Flight
     */
    public CarAttributes(JSONObject carAttributes) {
        try {
            if (carAttributes.has("Smoking"))
                smoking = carAttributes.getBoolean("Smoking");
            if (carAttributes.has("Car Type"))
                carType = carAttributes.getString("Car Type");
        } catch (JSONException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".CarAttributes() - Error Parsing JSON", e);
        }
    }

} // end CarAttributes
