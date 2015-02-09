package com.concur.mobile.platform.travel.search.hotel;

import java.util.Comparator;

/**
 * Hotel Violation comparator - sorted by descending enforcement level
 * 
 * @author RatanK
 * 
 */
public class HotelViolationComparator implements Comparator<HotelViolation> {

    @Override
    public int compare(HotelViolation hotelViolation1, HotelViolation hotelViolation2) {
        int retVal = 0;
        if (hotelViolation1.enforcementLevel != null && hotelViolation2.enforcementLevel != null) {
            if (hotelViolation1.enforcementLevel.equals("RequiresApproval")) {
                retVal = -1;
            } else if (hotelViolation1.enforcementLevel.equals(hotelViolation2.enforcementLevel)) {
                retVal = 0;
            } else if (hotelViolation2.enforcementLevel.equals("RequiresApproval")) {
                retVal = 1;
            }
        } else if (hotelViolation1.enforcementLevel != null) {
            // If no hotelViolation2 price, then put hotelViolation1 before hotelViolation2.
            retVal = -1;
        } else if (hotelViolation2.enforcementLevel != null) {
            // If no hotelViolation1 price, then put hotelViolation1 after hotelViolation2.
            retVal = 1;
        }
        return retVal;
    }

}
