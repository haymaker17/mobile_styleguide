package com.concur.mobile.core.data;

import java.util.ArrayList;
import java.util.Collections;

import com.concur.mobile.platform.util.Parse;

public class CreditCard {

    public int ccId;
    public String maskedNumber;
    public String name;
    public int type;

    // added for the travel pre-sell options
    public String ccLastFour;
    public boolean defaultCard;

    public ArrayList<String> allowedFor = new ArrayList<String>();
    public ArrayList<String> defaultFor = new ArrayList<String>();

    /**
     * Populates a usage list (allowed or default usage) with the values from a comma-delimited string.
     * 
     * @param usageList
     * @param usageString
     */
    protected void populateUsageList(ArrayList<String> usageList, String usageString) {
        if (usageString != null && usageString.trim().length() > 0) {
            Collections.addAll(usageList, usageString.split(","));
        }
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("AllowFor")) {
            populateUsageList(allowedFor, cleanChars);
        } else if (localName.equalsIgnoreCase("CcId")) {
            ccId = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("DefaultFor")) {
            populateUsageList(defaultFor, cleanChars);
        } else if (localName.equalsIgnoreCase("MaskedNumber")) {
            maskedNumber = cleanChars;
        } else if (localName.equalsIgnoreCase("Name")) {
            name = cleanChars;
        } else if (localName.equalsIgnoreCase("Type")) {
            type = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("CreditCardId")) {
            ccId = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("CreditCardLastFour")) {
            ccLastFour = cleanChars;
        } else if (localName.equalsIgnoreCase("IsDefault")) {
            defaultCard = Parse.safeParseBoolean(cleanChars);
        }
    }

}
