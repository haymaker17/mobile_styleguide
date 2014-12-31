package com.concur.mobile.core.util;

/**
 * this class handles all the utility function for date and time.
 * 
 * @author sunill
 * */
import java.util.Calendar;
import java.util.TimeZone;

public class BookingDateUtil {

    public BookingDateUtil() {
    }

    /**
     * Check out the given date is Valid or not. This method is helpful in All Search Criteria.
     * 
     * @param departDate
     *            : Depart date (AirSearch, RailSearch) || Check in date (HotelSearch) || Pick up date (CarSearch)
     * @param returnDate
     *            : Return date (AirSearch, RailSearch) || Check out date (HotelSearch) || dropped date (CarSearch)
     * @param isCompareWithCurrentDate
     *            : Compare with current date and time. if True send returnDate always NULL as an argument.
     * @return : returns given date is valid or not.
     */
    public boolean isDateInValid(Calendar departDate, Calendar returnDate, boolean isCompareWithCurrentDate) {
        boolean isValid;
        if (isCompareWithCurrentDate && returnDate == null) {
            isValid = departDate.before(getCurrentTime());
        } else {
            isValid = returnDate.before(departDate);
        }
        return isValid;
    }

    /**
     * Check out the given date is Valid or not with default time zone. This method is used in Air search.
     * 
     * @param departDate
     *            : Depart date (AirSearch)
     * @param returnDate
     *            : Return date (AirSearch)
     * @param isCompareWithCurrentDate
     *            : Compare with current date and time (for default time zone). if True send returnDate always NULL as an
     *            argument.
     * @return : returns given date is valid or not.
     */
    // MOB-21681 - set user's preferred time zone
    public boolean isDateInValidForDefaultTimeZone(Calendar departDate, Calendar returnDate,
            boolean isCompareWithCurrentDate) {
        boolean inValid;
        if (isCompareWithCurrentDate && returnDate == null) {
            inValid = departDate.before(Calendar.getInstance());
        } else {
            inValid = returnDate.before(departDate);
        }
        return inValid;
    }

    /**
     * Get Current Date and Time.
     * 
     * @return Calendar reference with UTC TimeZone
     */
    public Calendar getCurrentTime() {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return now;
    }

    /**
     * set depart date to current date
     * 
     * @param departDateTime
     * @param returnDateTime
     * @param now
     */
    public Calendar setDepartToCurrent(Calendar departDateTime, Calendar returnDateTime, Calendar now) {
        departDateTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        return departDateTime;
    }

    /**
     * set return date to current date and add 3 as MOB-10156
     * 
     * @param returnDateTime
     * @param departDateTime
     */
    public Calendar setReturnToCurrent(Calendar returnDateTime, Calendar departDateTime) {
        returnDateTime.set(departDateTime.get(Calendar.YEAR), departDateTime.get(Calendar.MONTH),
                departDateTime.get(Calendar.DAY_OF_MONTH));
        returnDateTime.add(Calendar.DAY_OF_MONTH, 3);
        return returnDateTime;
    }

}
