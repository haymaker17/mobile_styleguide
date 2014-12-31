/**
 * Helper class to parse AlternativeAirScheduleReply.
 * 
 * @author sunill
 * */
package com.concur.mobile.core.travel.data;

import java.util.ArrayList;

import com.concur.mobile.core.travel.air.data.Flight;
import com.concur.mobile.platform.util.Parse;

public class SegmentOption {

    public String id;
    public String travelConfigId;
    public int totalElapsedTime;
    public ArrayList<Flight> flights;

    public SegmentOption() {
        flights = new ArrayList<Flight>();
    }

    /**
     * handle all the tags in Segment Option Elemenet/Node.
     * */
    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("ID")) {
            id = cleanChars;
        } else if (localName.equalsIgnoreCase("TravelConfigID")) {
            travelConfigId = cleanChars;
        } else if (localName.equalsIgnoreCase("TotalElapsedTime")) {
            totalElapsedTime = Parse.safeParseInteger(cleanChars);
        }

    }
}
