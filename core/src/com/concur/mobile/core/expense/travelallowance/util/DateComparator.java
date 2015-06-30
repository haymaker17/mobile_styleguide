package com.concur.mobile.core.expense.travelallowance.util;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * {@code Comparator} implementation capable of comparing {@code Date} objects
 * whereas all {@code Date} components except milliseconds are considered.
 *
 * @author Michael Becherer
 */
public class DateComparator implements Comparator<Date>, Serializable {

    /**
     * Version ID for serialization
     */
    private static final long serialVersionUID = 1L;
    /**
     * Indicates whether to ignore the time component.
     */
    private boolean ignoreTime = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final Date lhs, final Date rhs) {
        if (lhs == null && rhs == null) {
            return 0;
        }

        if (lhs == null) {
            return -1;
        }

        if (rhs == null) {
            return 1;
        }

        final Calendar lhsCal = Calendar.getInstance();
        lhsCal.setTime(lhs);
        lhsCal.set(Calendar.MILLISECOND, 0);

        final Calendar rhsCal = Calendar.getInstance();
        rhsCal.setTime(rhs);
        rhsCal.set(Calendar.MILLISECOND, 0);

        if (ignoreTime) {
            lhsCal.set(Calendar.HOUR_OF_DAY, 0);
            lhsCal.set(Calendar.MINUTE, 0);
            lhsCal.set(Calendar.SECOND, 0);
            lhsCal.set(Calendar.MILLISECOND, 0);

            rhsCal.set(Calendar.HOUR_OF_DAY, 0);
            rhsCal.set(Calendar.MINUTE, 0);
            rhsCal.set(Calendar.SECOND, 0);
            rhsCal.set(Calendar.MILLISECOND, 0);
        }

        return lhsCal.compareTo(rhsCal);
    }

    /**
     * Set to true if the time component should be ignored.
     *
     * @param ignoreTime
     *            true if time should be ignored
     */
    public void setIgnoreTime(final boolean ignoreTime) {
        this.ignoreTime = ignoreTime;
    }

}
