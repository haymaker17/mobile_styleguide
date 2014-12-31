package com.concur.mobile.core.travel.rail.data;

import android.os.Bundle;

import com.concur.mobile.platform.util.Parse;

public class RailTicketDeliveryOption {

    public static final String KEY_FEE = "fee";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";

    public Double fee;
    public String name;
    public String type;

    public RailTicketDeliveryOption() {
    }

    public RailTicketDeliveryOption(Bundle b) {
        if (b != null) {
            fee = b.getDouble(KEY_FEE);
            name = b.getString(KEY_NAME);
            type = b.getString(KEY_TYPE);
        }
    }

    public Bundle getBundle() {
        Bundle b = new Bundle();
        b.putDouble(KEY_FEE, fee);
        b.putString(KEY_NAME, name);
        b.putString(KEY_TYPE, type);
        return b;
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("Fee")) {
            fee = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("Name")) {
            name = cleanChars;
        } else if (localName.equalsIgnoreCase("Type")) {
            type = cleanChars;
        }

    }

}
