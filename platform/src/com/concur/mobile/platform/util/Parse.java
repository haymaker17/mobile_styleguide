package com.concur.mobile.platform.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.util.Log;

import com.concur.mobile.platform.common.formfield.IFormField.EnumField;
import com.concur.mobile.platform.util.EnumDeserializer.EnumParsingType;

/**
 * Contains a set of utility methods for parsing data for platform responses.
 */
public class Parse {

    // Contains the class tag used in log statements.
    private static final String CLS_TAG = "Parse";

    public static final TimeZone UTC;
    public static final DateFormat XML_DF;
    public static final DateFormat XML_DF_NO_T;
    public static final DateFormat XML_DF_LOCAL;
    public static final DateFormat SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY;
    public static final DateFormat LONG_YEAR_MONTH_DAY;

    static {

        UTC = TimeZone.getTimeZone("UTC");
        XML_DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        XML_DF.setTimeZone(UTC);
        XML_DF_NO_T = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        XML_DF_NO_T.setTimeZone(UTC);
        XML_DF_LOCAL = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY = new SimpleDateFormat("MM/dd/yy");
        SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY.setTimeZone(UTC);
        LONG_YEAR_MONTH_DAY = new SimpleDateFormat("yyyy-MM-dd");
        LONG_YEAR_MONTH_DAY.setTimeZone(UTC);
    }

    /**
     * Helper to parse timestamps from the XML
     * 
     * @param dateText
     *            The timestamp string from the XML. Should be trimmed.
     * @return A {@link Calendar} representing the timestamp. If the timestamp was not parseable then this will have the value 20
     *         Dec 1970 16:00:00 .
     */
    public static Calendar parseXMLTimestamp(String dateText) {
        return parseTimestamp(dateText, XML_DF);
    }

    /**
     * Helper to parse timestamps from XML
     * 
     * @param dateText
     *            The timestamp string from the XML. Should be trimmed.
     * @param df
     *            An instance of <code>DateFormat</code>.
     * 
     * @return A {@link Calendar} representing the timestamp. If the timestamp was not parseable then this will have the value 20
     *         Dec 1970 16:00:00 .
     */
    public static Calendar parseTimestamp(String dateText, DateFormat df) {
        Calendar cal = null;
        try {
            if (dateText != null && dateText.length() > 0) {
                cal = Calendar.getInstance(UTC);
                cal.setTime(df.parse(dateText));
                cal.set(Calendar.MILLISECOND, 0);
            }
        } catch (ParseException e) {
            // Something whack with the XML. Log it and stick in a flag date.
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseTimestamp: Unable to parse XML timestamp: " + dateText, e);
            cal.set(1970, 11, 19, 16, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal;
    }

    /**
     * Will parse a boolean value indicated within <code>text</code> as either the case insensitive value
     * <code>y, n, t, f, true, false, yes, no</code>.
     * 
     * @param text
     *            the boolean value to be parsed.
     * @return an instance of <code>Boolean</code> indicating the parsed value; <code>null</code> otherwise.
     */
    public static Boolean safeParseBoolean(String text) {

        Boolean result = Boolean.FALSE;
        if (text != null) {
            if (text.length() == 1) {
                if (text.charAt(0) == 'Y' || text.charAt(0) == 'y' || text.charAt(0) == 'T' || text.charAt(0) == 't') {
                    result = Boolean.TRUE;
                } else if (text.charAt(0) == 'N' || text.charAt(0) == 'n' || text.charAt(0) == 'F'
                        || text.charAt(0) == 'f') {
                    result = Boolean.FALSE;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".safeParseBoolean: Unable to parse boolean value: " + text);
                }
            } else if (text.length() > 1) {
                if (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("yes")) {
                    result = Boolean.TRUE;
                } else if (text.equalsIgnoreCase("false") || text.equalsIgnoreCase("no")) {
                    result = Boolean.FALSE;
                }
            }
        }

        return result;
    }

    /**
     * Parse a String value into a Long. Checks for null and length before attempting the parse and will catch and log parse
     * exceptions.
     * 
     * @param text
     *            A {@link String} containing the value to parse
     * @return A {@link Long} value if the text parses okay; null otherwise.
     */
    public static Long safeParseLong(String text) {
        Long result = null;

        if (text != null && text.length() > 0) {
            try {
                result = Long.valueOf(text);
            } catch (NumberFormatException nfe) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".safeParseLong: Unable to parse long: " + text, nfe);
            }
        }

        return result;
    }

    /**
     * Parse a String value into an Integer. Checks for null and length before attempting the parse and will catch and log parse
     * exceptions.
     * 
     * @param text
     *            A {@link String} containing the value to parse
     * @return An {@link Integer} value if the text parses okay; null otherwise.
     */
    public static Integer safeParseInteger(String text) {
        Integer result = null;

        if (text != null && text.length() > 0) {
            try {
                result = Integer.valueOf(text);
            } catch (NumberFormatException nfe) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".safeParseInteger: Unable to parse integer: " + text, nfe);
            }
        }

        return result;
    }

    /**
     * Parse a String value into an Integer. Checks for null and length before attempting the parse and will catch and log parse
     * exceptions. If <code>text</code> could not be parsed into an <code>Integer</code>, then <code>0</code> will be returned.
     * 
     * @param text
     *            A {@link String} containing the value to parse
     * @return An {@link Integer} value if the text parses okay; otherwise 0 will be returned.
     */
    public static Integer safeParseIntegerDefaultToZero(String text) {
        Integer val = Parse.safeParseInteger(text);
        if (val == null)
            val = 0;
        return val;
    }

    /**
     * Parse a String value into a Float. Checks for null and length before attempting the parse and will catch and log parse
     * exceptions.
     * 
     * @param text
     *            A {@link String} containing the value to parse
     * @return A {@link Float} value if the text parses okay; null otherwise.
     */
    public static Float safeParseFloat(String text) {
        Float result = null;

        if (text != null && text.length() > 0) {
            try {
                result = Float.valueOf(text);
            } catch (NumberFormatException nfe) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".safeParseFloat: Unable to parse float: " + text, nfe);
            }
        }

        return result;
    }

    /**
     * Parse a String value into a Double. Checks for null and length before attempting the parse and will catch and log parse
     * exceptions.
     * 
     * @param text
     *            A {@link String} containing the value to parse
     * @return An {@link Double} value if the text parses okay; null otherwise.
     */
    public static Double safeParseDouble(String text) {
        Double result = null;

        if (text != null && text.length() > 0) {
            try {
                result = Double.valueOf(text);
            } catch (NumberFormatException nfe) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".safeParseDouble: Unable to parse double: " + text, nfe);
            }
        }

        return result;
    }

    /**
     * Parse a String value into a Date. Checks for null and length before attempting the parse and will catch and log parse
     * exceptions.
     *
     * @param text
     *            A {@link String} containing the value to parse
     * @return An {@link Date} value if the text parses okay; null otherwise.
     */
    public static Date safeParseDate(String text, DateFormat df) {
        Date result = null;

        if (text != null && text.length() > 0) {
            try {
                result = df.parse(text);
            } catch (ParseException pe) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".safeParseDate: Unable to parse date: " + text, pe);
            }
        }

        return result;
    }

    /**
     * Parse a String value into an enum. Checks for null and length before attempting the parse and will catch and log parse
     * exceptions. Enum's class is required as Enum.valueOf() can't be called without it.
     * 
     * @param enumClass
     *            the enum's class
     * @param text
     *            A {@link String} containing the value to parse
     * @param parsingType
     *            the parsingType to use with this kind of enum
     * @return An {@link Enum} value if the text parses okay; null otherwise.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T safeParseEnum(Class<T> enumClass, String text, EnumParsingType parsingType) {
        if (enumClass != null && text != null) {
            try {
                switch (parsingType) {
                case STRING_VALUE:
                    // --- ENUM_VALUE('stringvalue') => @See EnumField<T>
                    for (T t : enumClass.getEnumConstants()) {
                        // --- <?> => generic information erased at runtime
                        if (t instanceof EnumField<?>) {
                            if (((EnumField<T>) t).getName().equalsIgnoreCase(text))
                                return t;
                        } else
                            throw new IllegalArgumentException(
                                    "enum needs to implements EnumField to use EnumParsingType.STRING_VALUE .");
                    }
                    throw new IllegalArgumentException("can't locate enum value for parsed text '" + text + "'.");

                default:
                    Log.d(CLS_TAG, "default - text :: " + text + " | class => " + enumClass.getName());
                    return Enum.valueOf(enumClass, text);
                }
            } catch (IllegalArgumentException iae) {
                Log.w(Const.LOG_TAG,
                        CLS_TAG + ".safeParseEnum: Unable to parse enum value (class: " + enumClass.getName() + "): "
                                + text, iae);
            }
        }
        return null;
    }
}
