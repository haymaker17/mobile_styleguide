package com.concur.mobile.core.travel.air.data;

import java.util.ArrayList;

import com.concur.mobile.platform.util.Parse;

public class AirBookingSegment {

    public Double distance;
    public int elapsedTime;

    public ArrayList<Flight> flights;

    public AirBookingSegment() {
        flights = new ArrayList<Flight>();
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("Distance")) {
            distance = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("ElapsedTimeMin")) {
            elapsedTime = Parse.safeParseInteger(cleanChars);
        }

    }

    /**
     * Will determine a fare type title to be applied for this air booking segment based on the following algorithm:<br>
     * <li>If all flights with fare type titles are the same, then return the one fare type title</li> <li>If all flights with
     * fare type titles are different, then return text like "multiple fare types"</li> <li>If all flights do not specify fare
     * type titles, then return <code>null</code></li>
     * 
     * @param multiFareTypeTitle
     *            contains the text to be used if the fare type is different among the various flights.
     * 
     * @return See comments above.
     */
    public String getFareTypeTitle(String multiFareTypeTitle) {
        String currentFareTitle = null;
        if (flights != null) {
            for (Flight flt : flights) {
                if (currentFareTitle != null) {
                    if (flt.title != null) {
                        if (!flt.title.equalsIgnoreCase(currentFareTitle)) {
                            // Detected different fare type titles. Go with multi-fare type title.
                            currentFareTitle = multiFareTypeTitle;
                            break;
                        }
                    } else {
                        // At least one flight has a fare title, but at least one doesn't, so
                        // this case is treated as a mult fare-type title.
                        currentFareTitle = multiFareTypeTitle;
                        break;
                    }
                } else {
                    currentFareTitle = flt.title;
                }
            }
        }
        return currentFareTitle;
    }

    /**
     * Gets the number of stops across this air segment.
     * 
     * @return the number of stops across this air booking segment.
     */
    public int getNumberOfStops() {

        int retVal = 0;
        if (flights != null) {
            // First, sum up all the stops across the flights.
            for (Flight flt : flights) {
                retVal += flt.numStops;
            }
            // Second, add (# of flights) - 1 for lay-overs.
            if (flights.size() > 0) {
                retVal += (flights.size() - 1);
            }
        }
        return retVal;
    }

}
