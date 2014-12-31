package com.concur.mobile.eva.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.concur.mobile.eva.util.Const;

/**
 * Object representing a EvaMoney attribute the Eva JSON response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class EvaMoney {

    private static final String CLS_TAG = EvaMoney.class.getSimpleName();

    public String amount;
    public String currency;
    public String restriction;
    public boolean perPerson;

    public EvaMoney(JSONObject money) {
        try {
            if (money.has("Amount")) {
                amount = money.getString("Amount");
            }

            if (money.has("Currency")) {
                currency = money.getString("Currency");
            }

            if (money.has("Restriction")) {
                restriction = money.getString("Restriction");
            }

            if (money.has("Per Person")) {
                perPerson = money.getBoolean("Per Person");
            }

        } catch (JSONException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".Money() - Error parsing JSON.", e);
        }
    }

} // EvaMoney
