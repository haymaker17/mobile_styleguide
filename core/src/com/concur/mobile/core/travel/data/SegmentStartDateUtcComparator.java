/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.Calendar;
import java.util.Comparator;

/**
 * An implementation of <code>Comparator</code> for comparing itinerary segments based on <code>Segment.getStartDateUtc</code>.
 * 
 * @author AndrewK
 */
public class SegmentStartDateUtcComparator implements Comparator<Segment> {

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Segment seg1, Segment seg2) {
        int retVal = 0;

        if (seg1 != seg2) {
            Calendar s1Start = seg1.getStartDateUtc();
            Calendar s2Start = seg2.getStartDateUtc();
            if (s2Start == null) {
                retVal = 1;
            } else if (s1Start == null) {
                retVal = -1;
            } else {
                retVal = s1Start.compareTo(s2Start);
            }
        }
        return retVal;
    }

}
