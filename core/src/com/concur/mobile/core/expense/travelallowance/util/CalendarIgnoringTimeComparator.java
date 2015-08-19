package com.concur.mobile.core.expense.travelallowance.util;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;

/**
 * <code>Comparator</code> capable of comparing <code>Calendar</code> objects
 * ignoring the time portion. That is only the date portion in shape of year,
 * month and day is compared.
 *
 * @author Michael Becherer
 *
 */
public final class CalendarIgnoringTimeComparator implements
        Comparator<Calendar>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Compares the <code>Calendar</code> objects denoted by <code>cal1</code>
     * and <code>cal2</code> ignoring the time. That is only the date portion in
     * shape of year, month and day is compared.
     *
     * @param cal1
     *            the first <code>Calendar</code> object to compare
     * @param cal2
     *            the second <code>Calendar</code> object to compare
     *
     * @return a negative integer if <code>cal1</code> is less than
     *         <code>cal2</code> a positive integer if <code>cal1</code> is
     *         greater than <code>cal2</code>; 0 if <code>cal1</code> has the
     *         same order as <code>cal2</code>
     */
    public int compare(final Calendar cal1, final Calendar cal2) {
        if (cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)) {
            return cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        }

        if (cal1.get(Calendar.MONTH) != cal2.get(Calendar.MONTH)) {
            return cal1.get(Calendar.MONTH) - cal2.get(Calendar.MONTH);
        }

        return cal1.get(Calendar.DAY_OF_MONTH)
                - cal2.get(Calendar.DAY_OF_MONTH);
    }

}