package com.concur.mobile.eva.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.concur.mobile.eva.util.Const;

/**
 * Object representing a Hotel Attributes from the Eva JSON response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class HotelAttributes {

    private static final String CLS_TAG = HotelAttributes.class.getSimpleName();

    public String chainName;
    public String chainGdsCode;
    public Boolean smoking = Boolean.FALSE;
    public String rating;

    public HotelAttributes(JSONObject hotelAttributes) {
        try {
            if (hotelAttributes.has("Chain")) {
                JSONArray chainArr = hotelAttributes.getJSONArray("Chain");
                if (chainArr.length() > 0) {
                    JSONObject chain = chainArr.getJSONObject(0);
                    chainName = chain.getString("Name");
                    chainGdsCode = chain.getString("gds_code");
                }
            }

            if (hotelAttributes.has("Rooms")) {
                JSONArray rooms = hotelAttributes.getJSONArray("Rooms");
                if (rooms.length() > 0) {
                    smoking = rooms.getJSONObject(0).getBoolean("Smoking");
                }

            }

            if (hotelAttributes.has("Rating")) {
                rating = hotelAttributes.getString("Rating");
            }

        } catch (JSONException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".HotelAttributes() - Error parsing JSON.", e);
        }
    }
}
