/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.Calendar;

/**
 * Models a time range.
 */
public class TimeRange {

    public Calendar startDateTimeUTC;

    public Calendar endDateTimeUTC;

    public boolean inRange(Calendar dateTimeUTC) {
        boolean in = dateTimeUTC != null;
        if (in) {
            in = dateTimeUTC.after(startDateTimeUTC) && dateTimeUTC.before(endDateTimeUTC);
        }

        return in;
    }
}
