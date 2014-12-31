package com.concur.mobile.eva.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.concur.mobile.eva.util.Const;

/**
 * Object representing a Request Attributes from the Eva JSON response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class RequestAttributes {

    public static final String CLS_TAG = RequestAttributes.class.getSimpleName();

    public List<String> transportType = new ArrayList<String>();

    public RequestAttributes(JSONObject requestAttributes) {

        if (requestAttributes.has("Transport Type")) {
            JSONArray jTransportType;
            try {
                jTransportType = requestAttributes.getJSONArray("Transport Type");
                for (int i = 0; i < jTransportType.length(); i++) {
                    transportType.add(jTransportType.getString(i));
                }
            } catch (JSONException e) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".RequestAttributes() - Problem parsing JSON", e);
            }
        }
    }

} // end RequestAttributes
