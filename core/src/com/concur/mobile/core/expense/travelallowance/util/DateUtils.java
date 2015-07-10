package com.concur.mobile.core.expense.travelallowance.util;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Contains a set of utility methods for performing date related operations.
 *
 * @author Michael Becherer
 */
public final class DateUtils {

    /**
     * The singleton {@code Comparator} instance for comparing {@code Calendar}
     * objects based only on the date component values, ignoring any time
     * component values.
     *
     * @see #getCalendarIgnoringTimeComparator()
     */
    private static Comparator<Calendar> calendarIgnoringTimeComp;

    /**
     * The singleton {@code Comparator} instance for comparing {@code Date}
     * objects based only on the date component values, ignoring millisecond
     * values.
     */
    private static Comparator<Date> dateComp;

    /**
     * The number of milliseconds in one second.
     */
    private static final long MILLISECONDS_IN_ONE_SECOND = 1000L;

    /**
     * The number of seconds in one day.
     */
    private static final long SECONDS_IN_ONE_DAY = 86400L;

    /**
     * Empty string
     */
    private static final String EMPTY_STRING = "";

    /**
     * Single private constructor to prevent direct instantiation of this class.
     */
    private DateUtils() {
    }

    /**
     * Converts the given <code>date</code> to seconds. The time part (hours,
     * minutes, seconds and milliseconds) is considered
     *
     * @param date
     *            the {@code Date} to convert
     *
     * @return the number of seconds in the given <code>date</code>
     *
     * @see #convertDateToSeconds(Date, boolean)
     */
    public static long convertDateToSeconds(final Date date) {
        return convertDateToSeconds(date, false);
    }

    /**
     * Converts the given <code>date</code> to seconds. The time part (hours,
     * minutes, seconds and milliseconds) is ignored if <code>ignoreTime</code>
     * is set to <code>true</code>.
     *
     * @param date
     *            the {@code Date} to convert
     * @param ignoreTime
     *            <code>true</code> to ignore the time part; <code>false</code>
     *            to consider it
     *
     * @return the number of seconds in the given <code>date</code>
     *
     * @see #convertDateToSeconds(Date)
     */
    public static long convertDateToSeconds(final Date date,
                                            final boolean ignoreTime) {
        if (date == null) {
            return 0;
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        final long milliseconds = cal.getTimeInMillis();
        return (milliseconds / MILLISECONDS_IN_ONE_SECOND);
    }

    /**
     * Converts the given number of <code>seconds</code> to a {@code Date}
     * instance.
     *
     * @param seconds
     *            the number of seconds
     *
     * @return the converted {@code Date} instance
     *
     * @see #convertDateToSeconds(Date)
     * @see #convertDateToSeconds(Date, boolean)
     */
    public static Date convertSecondsToDate(final long seconds) {
        final long milliseconds = seconds * MILLISECONDS_IN_ONE_SECOND;
        return new Date(milliseconds);
    }

    /**
     * Gets a {@code Comparator} instance for comparing {@code Calendar} objects
     * based only on the date component values, ignoring any time component
     * values.
     *
     * @return the suitable {@code Comparator}
     */
    public static Comparator<Calendar> getCalendarIgnoringTimeComparator() {
        if (calendarIgnoringTimeComp == null) {
            calendarIgnoringTimeComp = new CalendarIgnoringTimeComparator();
        }
        return calendarIgnoringTimeComp;
    }

    /**
     * Gets a {@code Comparator} instance for comparing {@code Calendar} objects
     * based only on the date component values, ignoring millisecond values.
     *
     * @param ignoreTime
     *            true if time should be ignored
     *
     * @return the suitable {@code Comparator}
     *
     * @since 13.08
     */
    public static Comparator<Date> getDateComparator(final boolean ignoreTime) {
        DateComparator dateComparator;
        if (dateComp == null) {
            dateComparator = new DateComparator();
            dateComparator.setIgnoreTime(ignoreTime);
            dateComp = dateComparator;
        } else {
            dateComparator = (DateComparator) dateComp;
            dateComparator.setIgnoreTime(ignoreTime);
        }

        return dateComp;
    }

    /**
     * This helper method concatenates to dates to one string.
     *
     * @param startDate         the start date
     * @param endDate           the end date
     * @param dateTimeFormatter the {@code DateTimeFormatter}
     * @param includeTime       true = add formatted time information to date
     * @return A string representation of the start and end date. If start and
     * end date is null an empty string will be returned. If start or
     * and date are null the string date which is not null will be
     * returned.
     */
    public static String startEndDateToString(final Date startDate,
                                              final Date endDate, final IDateFormat dateTimeFormatter,
                                              final boolean includeTime,
                                              final boolean includeWeekDay,
                                              final boolean includeYear) {

        String formattedStartEndDate = EMPTY_STRING;

        if (startDate == null && endDate == null) {
            return formattedStartEndDate;
        }

        if (dateTimeFormatter == null) {
            return formattedStartEndDate;
        }

        String formattedStartDate = EMPTY_STRING;
        String formattedEndDate = EMPTY_STRING;

        if (startDate != null && endDate != null) {
            formattedStartDate = dateTimeFormatter
                    .format(startDate, includeTime, includeWeekDay, includeYear);
            formattedEndDate = dateTimeFormatter
                    .format(endDate, includeTime, includeWeekDay, includeYear);
            formattedStartEndDate = formattedStartDate + " - "
                    + formattedEndDate;
        } else if (startDate != null && endDate == null) {
            formattedStartDate = dateTimeFormatter
                    .format(startDate, includeTime, includeWeekDay, includeYear);
            formattedStartEndDate = formattedStartDate;
        } else if (startDate == null && endDate != null) {

            formattedEndDate = dateTimeFormatter
                    .format(endDate, includeTime, includeWeekDay, includeYear);

            formattedStartEndDate = formattedEndDate;
        }
        return formattedStartEndDate;
    }

    /**
     * Returns a formatted String containing full length of month and year
     * separated by space (e.g. August 2013).
     *
     * @param date
     *            The {@code Date} containing month and year info.
     * @return The formatted {@code String}.
     */
    public static String monthAndYearToString(final Date date) {

        if (date == null) {
            return EMPTY_STRING;
        }
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Returns the duration in days between start date and end date.
     * If there is no period because either start or end date is
     * not provided, or if end date is before start date, then the
     * duration will be of 0 days by convention.
     * If start date and and date are of the same day, the duration
     * will be one day by convention.
     * @param startDate The start date.
     * @param endDate The end date.
     * @return The duration in days between start and end date.
     */
    public static int getDurationInDays(final Date startDate,
                                        final Date endDate) {
        int numberOfDays = 0;
        if (startDate == null || endDate == null || endDate.before(startDate)) {
            return numberOfDays;
        }
        final long seconds =
                DateUtils.convertDateToSeconds(endDate, true)
                        - DateUtils.convertDateToSeconds(startDate, true);
        if (seconds > 0L) {
            numberOfDays = (int) (seconds / SECONDS_IN_ONE_DAY + 1);
            //Verification due to winter towards summer time shift
            Calendar cmpCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();
            cmpCal.setTime(DateUtils.getDateIgnoringTime(startDate));
            endCal.setTime(DateUtils.getDateIgnoringTime(endDate));
            cmpCal.add(Calendar.DAY_OF_MONTH, numberOfDays - 1);
            if (cmpCal.before(endCal)) {
                numberOfDays++;
            }
        } else if (seconds == 0L) {
            numberOfDays = 1;
        }
        return numberOfDays;
    }

    /**
     * Returns a copy of the given {@code Date} ignoring time information such
     * as hours, minutes, seconds, milliseconds. All of those are set to 0.
     *
     * @param date
     *            The date.
     * @return The date ignoring time information.
     */
    public static Date getDateIgnoringTime(final Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Returns a {@code Calendar} ignoring time information such as hours,
     * minutes, seconds, milliseconds. All of those are set to 0.
     *
     * @param year
     *            The year (e.g. 2013)
     * @param month
     *            The month (e.g. {@link Calendar#DECEMBER}. Note that the first
     *            month of the year starts with 0.
     * @param day
     *            The day of the month.
     * @return The {@code Calendar} without time information appropriate to the
     *         default Locale.
     */
    public static Calendar getCalendarIgnoringTime(final int year,
                                                   final int month, final int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Creates a Calendar based on the given date. Replaces the time information (hours and
     * minutes) by the given attributes
     *
     * @param date The date where hours and minutes needs to be replaces
     * @param hourOfDay The new hours
     * @param minute The new minutes
     * @return The new Calendar, if date not equals null.
     */
    public static Calendar getCalendarKeepingDate(final Date date, final int hourOfDay, final int minute) {

        if (date == null) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        return cal;
    }

    /**
     * Creates a Calendar based on the given date. Replaces the date information (year, month, day)
     * by the given attributes
     *
     * @param date The date where hours and minutes needs to be replaces
     * @param year The new year
     * @param month The new month
     * @param day The new day
     * @return The new Calendar, if date not equals null.
     */
    public static Calendar getCalendarKeepingTime(final Date date, final int year, final int month, final int day) {

        if (date == null) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal;
    }

    /**
     * Checks for non-empty overlap of periods
     * @param ignoreTime True if only date information to be considered for
     * 					 intersecting period intervals
     * @param firstPeriodStart 1st period start date
     * @param firstPeriodEnd 1st period end date
     * @param secondPeriodStart 2nd period start date
     * @param secondPeriodEnd 2nd period end date
     * @return True if there is an overlap considering the comparison option
     */
    public static boolean isOverlappingPeriods(boolean ignoreTime,
                                               Date firstPeriodStart, Date firstPeriodEnd,
                                               Date secondPeriodStart, Date secondPeriodEnd) {
        Comparator<Date> dateComparator = getDateComparator(ignoreTime);
        return dateComparator.compare(firstPeriodStart, secondPeriodEnd) <= 0
                && dateComparator.compare(firstPeriodEnd, secondPeriodStart) >= 0;
    }

    /**
     * To convert a {@code Date} object in a OData JSON string representation.
     *
     * @param date
     *            the {@code Date} object which should be converted.
     * @return the JSON OData string of the passed date. In case date is null
     *         the method returns null.
     */
    public static String getJsonDate(Date date) {
        if (date == null) {
            return null;
        }

        final int magicNumber = 60000;

        Calendar calDate = Calendar.getInstance();
        calDate.setTime(date);
        int timeZoneOffset =
                (calDate.get(Calendar.ZONE_OFFSET) + calDate
                        .get(Calendar.DST_OFFSET)) / magicNumber;

        return "\\/Date(" + calDate.getTime().getTime() + "-" + timeZoneOffset
                + ")\\/";
    }
}
