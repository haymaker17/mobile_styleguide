/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.Comparator;

/**
 * An implementation of <code>Comparator</code> for sorting <code>Trip</code> objects by ascending <code>endLocal</code> dates.
 * 
 * @author AndrewK
 */
public class TripEndDateLocalComparator implements Comparator<Trip> {

    public int compare(Trip trip1, Trip trip2) {
        int retVal = 0;
        if (trip1 != trip2) {
            retVal = trip1.endLocal.compareTo(trip2.endLocal);
        }
        return retVal;
    }

}
