package com.concur.mobile.eva.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.concur.mobile.eva.util.Const;

/**
 * Object representing a Time from the Eva JSON response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class EvaTime {

    public final static String CLS_TAG = EvaTime.class.getSimpleName();

    /**
     * Represent a specific date and time if given.
     */
    public String date;

    /**
     * Example: <code>fly to ny 3/4/2010 at 10am</code> results: <code>date</code>: <code>2010-04-03</code>, <code>time</code>:
     * <code>10:00:00</code>.
     */
    public String time;

    /**
     * May represent: A range starting from Date/Time. Example: <code>next week</code> results: <code>date</code>:
     * <code>2010-10-25</code>, <code>delta</code>: <code>days=+6</code> A duration without an anchor date. Example:
     * <code>hotel for a week</code> results: <code>delta</code>: <code>days=+7</code>
     */
    public String delta;

    /**
     * A restriction on the date/time requirement. Values can be: <code>no_earlier</code>, <code>no_later</code>,
     * <code>no_more</code>, <code>no_less</code>, <code>latest</code>, <code>earliest</code>
     * 
     * Example: <code>depart NY no later than 10am</code> results: <code>restriction</code>: <code>no_later</code>,
     * <code>time</code>: <code>10:00:00</code>
     */
    public String restriction;

    /**
     * A boolean flag representing that a particular time has been calculated from other times, and not directly derived from the
     * input text. In most cases if an arrival time to a location is specified, the departure time from the previous location is
     * calculated.
     */
    public Boolean calculated;

    /**
     * Constructor - parses the given JSON object representing an Eva Time.
     * 
     * @param evaTime
     *            JSON object representing an Eva Time.
     */
    public EvaTime(JSONObject evaTime) {
        try {
            if (evaTime.has("Date")) {
                date = evaTime.getString("Date");
            }
            if (evaTime.has("Time")) {
                time = evaTime.getString("Time");
            }
            if (evaTime.has("Delta")) {
                delta = evaTime.getString("Delta");
            }
            if (evaTime.has("Restriction")) {
                restriction = evaTime.getString("Restriction");
            }
            if (evaTime.has("Calculated")) {
                calculated = evaTime.getBoolean("Calculated");
            }

        } catch (JSONException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".EvaTime() - error parsing the JSON Time object.", e);
        }
    }

    /**
     * Convenience method that formats the given <code>time</code> to the String format <code>yyyy-MM-dd</code>.
     * 
     * @param time
     *            the time in milliseconds to format.
     * 
     * @return a String formatted in <code>yyyy-MM-dd</code>
     */
    public static String formatDate(long time) {

        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    /**
     * Convenience method that formats the given <code>time</code> to the String format <code>HH:MM:SS</code>.
     * 
     * @param time
     *            the time in milliseconds to format.
     * 
     * @return a String formatted in <code>HH:MM:SS</code>
     */
    public static String formatTime(long time) {

        Date t = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("k:m:s");
        return format.format(t);
    }

    /**
     * Parses the given String <code>date</code> into a <code>Date</code> Object.
     * 
     * @param date
     *            the String date, must be in the format <code>yyyy-MM-dd</code>
     * @return a <code>Date</code> Object representing the given String <code>date</code>.
     */
    public static Date parseDate(String date) {

        if (date != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return format.parse(date);
            } catch (ParseException e) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".parseDate() - error formatting the date: " + date, e);
            }
        }

        return null;
    }

    /**
     * Convenience method to convert the Eva Date into the Concur server date.
     * 
     * @param evaDate
     *            the EvaApiReply date in <code>YYYY-MM-DD</code>
     * 
     * @return returns the date in Concur server format <code>DD/MM/YYYY</code>
     */
    public static String convertToConcurServerDate(String evaDate) {
        if (evaDate != null) {
            String[] split = evaDate.split("-");
            if (split.length == 3) {
                StringBuilder sb = new StringBuilder();
                return sb.append(split[1]).append("/").append(split[2]).append("/").append(split[0]).toString();
            } else {
                return evaDate;
            }
        }

        return null;
    }

    /**
     * Formats the given <code>timeInMillis</code> to <code>MM/dd/yyyy</code> format.
     * 
     * @param timeInMillis
     *            the time to format.
     * @return the formatted date in <code>MM/dd/yyyy</code>
     */
    public static String convertToConcurServerDate(long timeInMillis) {
        Date date = new Date(timeInMillis);
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        return format.format(date);
    }

    /**
     * Convenience method to convert the given Eva date into a Concur <code>Calendar</code> Object with the TimeZone of the given
     * timezonId, or GMT for unknown ids.
     * 
     * @param evaDate
     *            the EvaApiReply date in <code>YYYY-MM-DD</code>
     * @param timezoneId
     *            An ID can be an Olson name of the form Area/Location, such as America/Los_Angeles.
     *            <p>
     *            The getAvailableIDs() method returns the supported names.
     *            </p>
     *            <p>
     *            This method can also create a custom TimeZone given an ID with the following syntax: GMT[+|-]hh[[:]mm]. For
     *            example, "GMT+05:00", "GMT+0500", "GMT+5:00", "GMT+500", "GMT+05", and "GMT+5" all return an object with a raw
     *            offset of +5 hours from UTC, and which does not use daylight savings. These are rarely useful, because they
     *            don't correspond to time zones actually in use by humans.
     *            </p>
     *            <p>
     *            Other than the special cases "UTC" and "GMT" (which are synonymous in this context, both corresponding to UTC),
     *            Android does not support the deprecated three-letter time zone IDs used in Java 1.1.
     *            </p>
     * 
     * @return a <code>Calendar</code> Object representation of <code>evaDate</code>
     */
    public static Calendar convertToConcurServerCalendar(String evaDate, String timezoneId) {
        return convertToConcurServerCalendar(evaDate, null, timezoneId);
    }

    /**
     * Convenience method to convert the given Eva date and time (if specified) into a Concur <code>Calendar</code> Object with
     * the TimeZone of the given timezonId, or GMT for unknown ids.
     * 
     * @param evaDate
     *            the EvaApiReply date in <code>YYYY-MM-DD</code>
     * @param evaTime
     *            (optional parameter) the EvaApiReply time in <code>HH:MM:SS</code> format.
     * @param timezoneId
     *            An ID can be an Olson name of the form Area/Location, such as America/Los_Angeles.
     *            <p>
     *            The getAvailableIDs() method returns the supported names.
     *            </p>
     *            <p>
     *            This method can also create a custom TimeZone given an ID with the following syntax: GMT[+|-]hh[[:]mm]. For
     *            example, "GMT+05:00", "GMT+0500", "GMT+5:00", "GMT+500", "GMT+05", and "GMT+5" all return an object with a raw
     *            offset of +5 hours from UTC, and which does not use daylight savings. These are rarely useful, because they
     *            don't correspond to time zones actually in use by humans.
     *            </p>
     *            <p>
     *            Other than the special cases "UTC" and "GMT" (which are synonymous in this context, both corresponding to UTC),
     *            Android does not support the deprecated three-letter time zone IDs used in Java 1.1.
     *            </p>
     * 
     * @return a <code>Calendar</code> Object representation of <code>evaDate</code>
     */
    public static Calendar convertToConcurServerCalendar(String evaDate, String evaTime, String timezoneId) {

        GregorianCalendar cal = null;

        if (evaDate != null) {
            String[] split = evaDate.split("-");
            if (split.length == 3) {
                cal = new GregorianCalendar(Integer.parseInt(split[0]), (Integer.parseInt(split[1]) - 1),
                        Integer.parseInt(split[2]));
                if (timezoneId != null) {
                    cal.setTimeZone(TimeZone.getTimeZone(timezoneId)); // MOB-11600
                }
            }
        }

        if (evaTime != null && cal != null) {
            String[] split = evaTime.split(":");
            if (split.length == 3) {
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(split[1]));
                cal.set(Calendar.SECOND, Integer.parseInt(split[2]));
            }
        }

        return cal;
    }

    /**
     * Returns the offset of the current device's timezone. e.g. +10:00 for Guam or -07:00 for PST
     * 
     * @return the offset of the current device's timezone.
     */
    public static String getCurrentTimezoneOffset() {

        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

        String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000),
                Math.abs((offsetInMillis / 60000) % 60));
        offset = (offsetInMillis >= 0 ? "+" : "-") + offset;

        return offset;
    }

} // end EvaTime
