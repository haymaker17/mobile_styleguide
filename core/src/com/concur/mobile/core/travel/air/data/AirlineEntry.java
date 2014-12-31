package com.concur.mobile.core.travel.air.data;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

public class AirlineEntry {

    public String airlineCode;
    public String crnCode;
    public Double lowestCost;
    public int numChoices;
    public int travelPoints;

    public AirlineEntry() {
    }

    public String getAirlineName() {
        String name = AirDictionaries.vendorCodeMap.get(airlineCode);
        if (name == null) {
            name = "";
        }
        return name;
    }

    public int getPreferenceRank() {
        int rank = 0;
        String rankStr = AirDictionaries.preferenceRankMap.get(airlineCode);
        if (rankStr != null && rankStr.trim().length() > 0) {
            try {
                rank = Integer.decode(rankStr);
            } catch (NumberFormatException nfe) {
                Log.d(Const.LOG_TAG, "Failed to convert preference rank to integer // " + rankStr);
            }
        }
        return rank;
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("Airline")) {
            airlineCode = cleanChars;
        } else if (localName.equalsIgnoreCase("Crn")) {
            crnCode = cleanChars;
        } else if (localName.equalsIgnoreCase("LowestCost")) {
            lowestCost = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("NumChoices")) {
            numChoices = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("TravelPoints")) {
            travelPoints = Parse.safeParseInteger(cleanChars);
        }

    }

}
