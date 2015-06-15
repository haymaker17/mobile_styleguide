package com.concur.mobile.platform.travel.search.hotel;

import java.util.Comparator;

/**
 * Hotel Violation comparator - sorted by descending enforcement level
 *
 * @author RatanK
 */
public class HotelViolationComparator implements Comparator<HotelViolation> {

    @Override
    public int compare(HotelViolation hotelViolation1, HotelViolation hotelViolation2) {
        int retVal = 0;
        if (hotelViolation1.displayOrder != 0 && hotelViolation2.displayOrder != 0) {
            if (hotelViolation1.displayOrder > hotelViolation2.displayOrder) {
                retVal = -1;
            } else if (hotelViolation1.displayOrder == hotelViolation2.displayOrder) {
                retVal = 0;
            } else {
                retVal = 1;
            }
        } else if (hotelViolation1.displayOrder != 0) {
            retVal = -1;
        } else if (hotelViolation2.displayOrder != 0) {
            retVal = 1;
        }
        return retVal;
    }

}
