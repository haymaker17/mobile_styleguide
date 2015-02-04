package com.concur.mobile.core.request.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author olivierb
 */
public class DateUtil {

    private static final String CLS_TAG = DateUtil.class.getSimpleName();

    // Cache for SimpleDateFormat objects
    private static final Map<String, SimpleDateFormat> formatByPattern = new HashMap<String, SimpleDateFormat>();

    /**
     * See http://fr.wikipedia.org/wiki/Date#Pays_utilisant_le_format_jj.2Fmm.2F
     * aaaa => To put things simply, US uses mm/dd/yyyy while every other
     * countries uses dd/mm/yyyy so we consider every pattern to be
     * reverted on this basis depending on the locale used (english
     * translation won't give you usage list per country, sry. Just
     * consider the lists below the different patterns)
     */
    public enum DatePattern {
        SHORT("EEE, dd/MM", "EEE, MM/dd"),
        TIME("hh:mm aa", "hh:mm aa"),
        DB_INPUT("yyyy-MM-dd", "yyyy-MM-dd");

        private String pattern = null;
        private String patternUS = null;

        DatePattern(String pattern, String patternUS) {
            this.pattern = pattern;
            this.patternUS = patternUS;
        }

        public String getPattern(Locale locale) {
            if (locale.equals(Locale.US)) {
                return patternUS;
            } else {
                return pattern;
            }
        }
    }

    // --- convert to an enum if there is more than one
    public static final String TIME_PATTERN = "HH:mm";

    /**
     * Converts a date to a string with the given pattern for the given locale !
     * Use this method only for single uses - no multiple calls ! (store an SDF
     * & call it multiple times with your different dates instead)
     *
     * @param pattern DatePattern.SHORT for example (enum is declared in
     *                DateUtil.java)
     * @param locale  in-use locale
     * @param date    the date to convert
     * @return date formatted to given locale
     */
    public static String getFormattedDateForLocale(DatePattern pattern, Locale locale, Date date) {
        addPattern(pattern, locale);
        return date != null ? formatByPattern.get(pattern.getPattern(locale)).format(date) : "";
    }

    public static Date parseFormattedDateForLocale(DatePattern inputPattern, Locale locale, String dateStr) {
        addPattern(inputPattern, locale);
        if (dateStr != null) {
            try {
                return formatByPattern.get(inputPattern.getPattern(locale)).parse(dateStr);
            } catch (ParseException pe) {
                Log.e(CLS_TAG, pe.getMessage());
            }
        }
        return null;
    }

    private static void addPattern(DatePattern pattern, Locale locale) {
        final String locPattern = pattern.getPattern(locale);
        if (!formatByPattern.containsKey(locPattern)) {
            formatByPattern.put(locPattern, new SimpleDateFormat(locPattern));
        }
    }
}
