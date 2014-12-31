package com.concur.mobile.core.expense.report.data;

import java.util.Calendar;

import com.concur.mobile.platform.util.Parse;

public class CarRate implements Comparable<CarRate> {

    public double rate;
    public Calendar startDate;

    public int compareTo(CarRate otherRate) {
        int result = 0;

        if (startDate != null && otherRate.startDate != null) {
            result = startDate.compareTo(otherRate.startDate);
        } else {
            if (startDate == null && otherRate.startDate == null) {
                result = 0;
            } else if (otherRate.startDate == null) {
                result = 1;
            } else if (startDate == null) {
                result = -1;
            }
        }

        return result;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////
    //
    // BELOW HERE BE SAX DRAGONS
    //
    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle the rate level elements
     * 
     * @param localName
     */
    protected void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("Rate")) {
            rate = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("StartDate")) {
            startDate = Parse.parseXMLTimestamp(cleanChars);
        }
    }
}
