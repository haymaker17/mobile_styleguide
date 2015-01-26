package com.concur.mobile.core.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.platform.util.Format;

public class FormatUtil {

    private static final String CLS_TAG = FormatUtil.class.getSimpleName();

    public static final TimeZone UTC;
    public static final DateFormat XML_DF_LOCAL;
    public static final DateFormat XML_DF;
    public static final DateFormat SHORT_DAY_DISPLAY;
    public static final DateFormat SHORT_DAY_DISPLAY_NO_COMMA;
    public static final DateFormat SHORT_DAY_YEAR_DISPLAY_NO_COMMA;
    public static final DateFormat SHORT_MONTH_FULL_YEAR_DISPLAY;
    public static final DateFormat SHORT_MONTH_DAY_YEAR_DISPLAY;
    public static final DateFormat SHORT_MONTH_DAY_FULL_YEAR_DISPLAY;
    public static final DateFormat SHORT_MONTH_DAY_FULL_YEAR_DISPLAY_LOCAL;
    public static final DateFormat SHORT_MONTH_DAY_SHORT_YEAR_SHORT_TIME_DISPLAY;
    public static final DateFormat SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY;
    public static final DateFormat SHORT_TIME_ONLY_DISPLAY;
    public static final DateFormat SHORT_24HR_TIME_ONLY_DISPLAY;
    public static final DateFormat SHORT_TIME_AMPM_DISPLAY;
    public static final DateFormat SHORT_TIME_DISPLAY;
    public static final DateFormat SHORT_DAY_TIME_DISPLAY;
    public static final DateFormat SHORT_DAY_OF_WEEK_TIME_DISPLAY;
    public static final DateFormat LONG_DAY_TIME_DISPLAY;
    public static final DateFormat MONTH_DAY_FULL_YEAR_DISPLAY_LOCAL;
    public static final DateFormat MONTH_DAY_FULL_YEAR_DISPLAY;
    public static final DateFormat LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND;
    public static final DateFormat LONG_YEAR_MONTH_DAY;
    public static final DateFormat SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY;
    public static final DateFormat SHORT_MONTH_DAY_SHORT_DISPLAY;
    public static final DateFormat SHORT_MONTH_DAY_DISPLAY;
    public static final DateFormat DATA_UPDATE_DISPLAY;
    public static final DateFormat RECEIPT_STORE_LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND;
    public static final DateFormat REPORT_NAME_DATE_LOCAL;
    public static final DateFormat GOV_EXPENSE_DATE_LOCAL;
    public static final DateFormat SHORT_WEEKDAY_SHORT_MONTH_DAY_FULL_YEAR_12HOUR_TIMEZONE_DISPLAY_LOCAL;
    public static final DateFormat SHORT_WEEKDAY_SHORT_MONTH_DAY_FULL_YEAR_24HOUR_TIMEZONE_DISPLAY_LOCAL;
    public static final DateFormat SHORT_WEEKDAY_SHORT_MONTH_DAY_FULL_YEAR_V1_DISPLAY;
    public static final DateFormat LONG_YEAR_MONTH_DAY_LOCAL;
    public static final DateFormat SHORT_DAY_OF_WEEK_TIME_DISPLAY_LOCAL;

    static {
        // We don't do date conversion, we just display. Force everything to UTC so we don't
        // accidentally run afoul of implicit time/date conversions in the underlying libraries
        // that default to device timezone.
        UTC = TimeZone.getTimeZone("UTC");
        XML_DF_LOCAL = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        XML_DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        XML_DF.setTimeZone(UTC);
        SHORT_DAY_DISPLAY = new SimpleDateFormat("EEE, MMM d");
        SHORT_DAY_DISPLAY.setTimeZone(UTC);
        SHORT_DAY_DISPLAY_NO_COMMA = new SimpleDateFormat("EEE MMM d");
        SHORT_DAY_DISPLAY_NO_COMMA.setTimeZone(UTC);
        SHORT_DAY_YEAR_DISPLAY_NO_COMMA = new SimpleDateFormat("EEE MMM d, yyyy");
        SHORT_DAY_YEAR_DISPLAY_NO_COMMA.setTimeZone(UTC);
        SHORT_MONTH_FULL_YEAR_DISPLAY = new SimpleDateFormat("MMM yyyy");
        SHORT_MONTH_FULL_YEAR_DISPLAY.setTimeZone(UTC);
        SHORT_MONTH_DAY_DISPLAY = new SimpleDateFormat("MMM d");
        SHORT_MONTH_DAY_DISPLAY.setTimeZone(UTC);
        SHORT_MONTH_DAY_YEAR_DISPLAY = new SimpleDateFormat("MMM d, yy");
        SHORT_MONTH_DAY_YEAR_DISPLAY.setTimeZone(UTC);
        SHORT_MONTH_DAY_FULL_YEAR_DISPLAY_LOCAL = new SimpleDateFormat("MMM d, yyyy");
        SHORT_MONTH_DAY_FULL_YEAR_DISPLAY = new SimpleDateFormat("MMM d, yyyy");
        SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.setTimeZone(UTC);
        SHORT_MONTH_DAY_SHORT_YEAR_SHORT_TIME_DISPLAY = new SimpleDateFormat("MMM d, yy h:mm a");
        SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY = new SimpleDateFormat("EEE, MMM d, yyyy");
        SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY.setTimeZone(UTC);
        SHORT_TIME_DISPLAY = new SimpleDateFormat("h:mm a");
        SHORT_TIME_DISPLAY.setTimeZone(UTC);
        SHORT_TIME_ONLY_DISPLAY = new SimpleDateFormat("h:mm");
        SHORT_TIME_ONLY_DISPLAY.setTimeZone(UTC);
        SHORT_24HR_TIME_ONLY_DISPLAY = new SimpleDateFormat("H:mm");
        SHORT_24HR_TIME_ONLY_DISPLAY.setTimeZone(UTC);
        SHORT_TIME_AMPM_DISPLAY = new SimpleDateFormat("a");
        SHORT_TIME_AMPM_DISPLAY.setTimeZone(UTC);
        SHORT_DAY_TIME_DISPLAY = new SimpleDateFormat("EEE MMM d h:mm a");
        SHORT_DAY_TIME_DISPLAY.setTimeZone(UTC);
        SHORT_DAY_OF_WEEK_TIME_DISPLAY = new SimpleDateFormat("EEE h:mma");
        SHORT_DAY_OF_WEEK_TIME_DISPLAY.setTimeZone(UTC);
        LONG_DAY_TIME_DISPLAY = new SimpleDateFormat("MMMM d, yyyy h:mm a");
        LONG_DAY_TIME_DISPLAY.setTimeZone(UTC);
        MONTH_DAY_FULL_YEAR_DISPLAY_LOCAL = new SimpleDateFormat("MMMM dd, yyyy");
        MONTH_DAY_FULL_YEAR_DISPLAY = new SimpleDateFormat("MMMM dd, yyyy");
        MONTH_DAY_FULL_YEAR_DISPLAY.setTimeZone(UTC);
        LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND.setTimeZone(UTC);
        LONG_YEAR_MONTH_DAY = new SimpleDateFormat("yyyy-MM-dd");
        LONG_YEAR_MONTH_DAY.setTimeZone(UTC);
        SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY = new SimpleDateFormat("MM/dd/yy");
        SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY.setTimeZone(UTC);
        SHORT_MONTH_DAY_SHORT_DISPLAY = new SimpleDateFormat("MM/dd");
        SHORT_MONTH_DAY_SHORT_DISPLAY.setTimeZone(UTC);
        DATA_UPDATE_DISPLAY = new SimpleDateFormat("MMMM d, yyyy h:mm a");
        RECEIPT_STORE_LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        RECEIPT_STORE_LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND.setTimeZone(UTC);
        REPORT_NAME_DATE_LOCAL = new SimpleDateFormat("yyyy.MM.dd");
        GOV_EXPENSE_DATE_LOCAL = new SimpleDateFormat("yyyy-MM-dd");
        SHORT_WEEKDAY_SHORT_MONTH_DAY_FULL_YEAR_12HOUR_TIMEZONE_DISPLAY_LOCAL = new SimpleDateFormat(
                "EEE, MMM dd, yyyy hh:mm a z");
        SHORT_WEEKDAY_SHORT_MONTH_DAY_FULL_YEAR_24HOUR_TIMEZONE_DISPLAY_LOCAL = new SimpleDateFormat(
                "EEE, MMM dd, yyyy HH:mm z");
        SHORT_WEEKDAY_SHORT_MONTH_DAY_FULL_YEAR_V1_DISPLAY = new SimpleDateFormat("MM/dd/yyyy");
        LONG_YEAR_MONTH_DAY_LOCAL = new SimpleDateFormat("yyyy-MM-dd");
        SHORT_DAY_OF_WEEK_TIME_DISPLAY_LOCAL = new SimpleDateFormat("EEE h:mma");
    }

    private static final int MINUTES_IN_HOUR = 60;
    private static final int MINUTES_IN_DAY = 24 * MINUTES_IN_HOUR;
    public final static long ONE_SECOND_MILLIS = 1000;
    public final static long ONE_MINUTE_MILLIS = ONE_SECOND_MILLIS * 60;

    private static HashMap<String, String> decimalSymbols;

    static {
        decimalSymbols = new HashMap<String, String>(2);
        decimalSymbols.put("", "0123456789- ,.");
    }

    private FormatUtil() {
        // And never shall this class be constructed...
    }

    public static String localizeDouble(Double value) {
        // TODO: Make this all locale and currency aware and all that good stuff
        String text = "";

        if (value != null) {
            text = Double.toString(value);
        }

        return text;
    }

    /**
     * Contact String with pass delimiter. charactor
     * 
     * @param strBlder
     *            : Reference of string builder.
     * @param delim
     *            : delimeter
     * @param isSpaceRequired
     *            : do we required space after delimeter
     * @param values
     *            : values to append
     * @return : reference of string builder.
     */
    public static StringBuilder concateStringWithDelim(StringBuilder strBlder, char delim, boolean isSpaceRequired,
            Object values) {
        if (strBlder == null) {
            strBlder = new StringBuilder("");
        }

        Object obj = values;
        String str = "";
        if (obj != null) {
            str = obj.toString();
            if (str != null && str.length() > 0) {
                int len = strBlder.length();
                if (len > 0) {
                    strBlder.append(delim);
                    strBlder.append(' ');
                }
                strBlder.append(str);
            }
        }
        return strBlder;
    }

    private static boolean isSymbolASuffix(Locale loc) {
        boolean isSuffix = false;

        NumberFormat nf = NumberFormat.getCurrencyInstance(loc);
        if (nf instanceof DecimalFormat) {
            String patt = ((DecimalFormat) nf).toLocalizedPattern();
            if (patt.indexOf('\u00a4') > 0) {
                isSuffix = true;
            }
        }

        return isSuffix;
    }

    public static String formatAmount(double amount, Locale loc, String crnCode, boolean useSymbol) {
        return formatAmount(amount, loc, crnCode, useSymbol, false);
    }

    public static String formatAmount(double amount, Locale loc, String crnCode, boolean useSymbol, boolean useGrouping) {

        Currency curr = null;
        if (crnCode != null) {
            try {
                curr = Currency.getInstance(crnCode);
            } catch (IllegalArgumentException iae) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".formatAmount: invalid currency code: " + crnCode, iae);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".formatAmount: currency code is null!");
        }

        int decDigits = 2; // A sane default
        String symbol = "";
        if (curr != null) {
            decDigits = curr.getDefaultFractionDigits();
            symbol = curr.getSymbol(loc);
        }

        NumberFormat nf = DecimalFormat.getInstance(loc);
        nf.setMaximumFractionDigits(decDigits);
        nf.setMinimumFractionDigits(decDigits);
        nf.setGroupingUsed(useGrouping);

        StringBuilder amountSB = new StringBuilder(nf.format(amount));

        if (useSymbol) {
            if (isSymbolASuffix(loc)) {
                amountSB.append(' ').append(symbol);
            } else {
                if (symbol.length() > 1) {
                    // For currency abbreviations, we want a space...
                    amountSB.insert(0, ' ').insert(0, symbol);
                } else {
                    // For one character symbols, we do not.
                    amountSB.insert(0, symbol);
                }
            }
        }

        return amountSB.toString();
    }

    /**
     * @deprecated Use the four or five parameter version instead.
     * @param amt
     * @param currencyCode
     * @return
     */
    @Deprecated
    public static String formatAmount(Double amt, String currencyCode) {

        if (amt == null) {
            return "";
        }

        Currency crn = Currency.getInstance(currencyCode);
        int digits = crn.getDefaultFractionDigits();

        DecimalFormat fmt = new DecimalFormat();
        fmt.setMaximumFractionDigits(digits);
        fmt.setMinimumFractionDigits(digits);

        String symbol = crn.getSymbol();
        String amtStr = fmt.format(amt);

        StringBuilder display = new StringBuilder(symbol).append(amtStr);

        return display.toString();
    }

    /**
     * Convert a decimal string for the given locale into a double
     * 
     * @param amountStr
     *            String representation of the decimal using the formatting of the given locale
     * @param loc
     *            The given locale
     * @return The decimal value as a Double; null if it is unparseable
     */
    public static Double parseAmount(String amountStr, Locale loc) {
        Double amount = null;

        NumberFormat df = DecimalFormat.getInstance(loc);
        if (df instanceof DecimalFormat) {
            try {
                if (amountStr != null && amountStr.trim().length() > 0) {
                    Number num = ((DecimalFormat) df).parse(amountStr);
                    amount = num.doubleValue();
                }
            } catch (ParseException pe) {
                // That sucks.
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".parseAmount: unable to parse '" + amountStr + "' for locale " + loc.toString(), pe);
            }
        } else {
            // Weird. Log it.
            Log.e(Const.LOG_TAG, "Unable to find locale-specific decimal format.");
        }
        return amount;
    }

    public static URI buildURI(String path, boolean ssl) {

        // Build our URI
        StringBuilder serverURI = new StringBuilder(Format.formatServerAddress(ssl, Preferences.getServerAddress()))
                .append(path);

        URI uri = null;

        try {
            uri = new URI(serverURI.toString());
        } catch (URISyntaxException e) {
            Log.w(Const.LOG_TAG, "Failed to build URI for: " + path, e);
        }

        return uri;
    }

    public static StringBuilder addXMLElement(StringBuilder sb, String elementName, String value) {
        sb.append('<').append(elementName).append('>');
        sb.append(value);
        sb.append("</").append(elementName).append('>');

        return sb;
    }

    public static StringBuilder addXMLElementEscaped(StringBuilder sb, String elementName, String value) {
        if (value == null) {
            sb.append('<').append(elementName).append("/>");
        } else {
            sb.append('<').append(elementName).append('>');
            sb.append(escapeForXML(value));
            sb.append("</").append(elementName).append('>');
        }
        return sb;
    }

/**
     * Will replace the characters '<', '>', '&', '"' and ''' with the XML escape sequences
     * "&lt;", "&gt;", "&amp;", "&quot;", "&apos;", respectfully.
     * 
     * @param string        the string of characters to escape.
     * 
     * @return              the escaped string.
     */
    public static String escapeForXML(String string) {

        String retVal = string;

        if (retVal != null && retVal.length() > 0) {
            retVal = retVal.replaceAll("&", "&amp;");
            retVal = retVal.replaceAll("<", "&lt;");
            retVal = retVal.replaceAll(">", "&gt;");
            retVal = retVal.replaceAll("\"", "&quot;");
            retVal = retVal.replaceAll("'", "&apos;");
        }
        return retVal;
    }

/**
     * Will replace the character '<', '>', '&', '"' and ''' with the XML escape sequences
     * "&lt;", "&gt;", "&amp;", "&quot;", "&apos;", respectfully.
     * 
     * @param strBldr
     * 		a string builder into which to place the escaped string.
     * @param ch
     * 		the character to be replaced.
     */
    public static void escapeForXML(StringBuilder strBldr, char ch) {
        if (strBldr != null) {
            if (ch == '&') {
                strBldr.append("&amp;");
            } else if (ch == '<') {
                strBldr.append("&lt;");
            } else if (ch == '>') {
                strBldr.append("&gt;");
            } else if (ch == '"') {
                strBldr.append("&quot;");
            } else if (ch == '\'') {
                strBldr.append("&apos;");
            } else {
                // Just add the character.
                strBldr.append(ch);
            }
        }
    }

    public static String formatElapsedTime(Context context, int elapsed) {
        return formatElapsedTime(context, elapsed, false);
    }

    public static String formatElapsedTime(Context context, int elapsedMinutes, boolean useLongFormat) {

        int days = elapsedMinutes / MINUTES_IN_DAY;
        elapsedMinutes %= MINUTES_IN_DAY;

        int hours = elapsedMinutes / MINUTES_IN_HOUR;
        elapsedMinutes %= MINUTES_IN_HOUR;

        int minutes = elapsedMinutes;

        String elapsedText;

        int formatId;
        if (days > 0) {
            if (useLongFormat) {
                formatId = R.string.general_elapsed_day_hour_min_long;
            } else {
                formatId = R.string.general_elapsed_day_hour_min;
            }
            elapsedText = com.concur.mobile.base.util.Format.localizeText(context, formatId, new Object[] { days,
                    hours, minutes });
        } else if (hours > 0) {
            if (useLongFormat) {
                formatId = R.string.general_elapsed_hour_min_long;
            } else {
                formatId = R.string.general_elapsed_hour_min;
            }
            elapsedText = com.concur.mobile.base.util.Format.localizeText(context, formatId, new Object[] { hours,
                    minutes });
        } else {
            if (useLongFormat) {
                formatId = R.string.general_elapsed_min_long;
            } else {
                formatId = R.string.general_elapsed_min;
            }
            elapsedText = com.concur.mobile.base.util.Format.localizeText(context, formatId, new Object[] { minutes });
        }

        return elapsedText;

    }

    /**
     * Get the minutes difference
     * 
     * @param beginTime
     * @param endTime
     *            return difference;
     */
    public static int getMinutesDifference(Calendar beginTime, Calendar endTime) {
        int difference = -1;
        if (beginTime == null || endTime == null)
            return difference;
        difference = (int) ((endTime.getTimeInMillis() / ONE_MINUTE_MILLIS) - (beginTime.getTimeInMillis() / ONE_MINUTE_MILLIS));
        return difference;
    }

    /**
     * Get the month difference
     * 
     * @param beginTime
     * @param endTime
     * @return difference
     */
    public static final int getMonthsDifference(Calendar beginTime, Calendar endTime) {
        int difference = -1;
        if (beginTime == null || endTime == null)
            return difference;
        int month1 = beginTime.get(Calendar.MONTH) + 1;
        int month2 = endTime.get(Calendar.MONTH) + 1;
        return month2 - month1;
    }

    /**
     * set color for the field name.
     * 
     * @param parentId
     *            : parent layout
     * @param childId
     *            : child layout
     * @param colorId
     *            : which color you need to set.
     */
    public static void setColorForLabelField(int parentId, int childId, int colorId, Context ctx) {
        TextView txtView = (TextView) ViewUtil.findSubView((Activity) ctx, parentId, childId);
        txtView.setTextColor(ctx.getResources().getColor(colorId));
    }

    public static NumberKeyListener getLocaleDecimalListener(Context ctx) {

        // Get the digits for the locale
        Locale loc = ctx.getResources().getConfiguration().locale;
        String locCode = loc.toString();

        // Set these values conditionally below
        char minusSymbol, decimalSymbol;

        String symbols = decimalSymbols.get(locCode);
        if (symbols == null) {
            // First time. Get our decimal format
            NumberFormat df = DecimalFormat.getInstance(loc);
            if (df instanceof DecimalFormat) {
                // Avoiding insanity, get the allowed symbols
                DecimalFormatSymbols dfs = ((DecimalFormat) df).getDecimalFormatSymbols();
                StringBuilder sb = new StringBuilder("0123456789");
                decimalSymbol = dfs.getMonetaryDecimalSeparator();
                sb.append(decimalSymbol);
                sb.append(dfs.getGroupingSeparator());
                minusSymbol = dfs.getMinusSign();
                sb.append(minusSymbol);
                symbols = sb.toString();

                // Save them for next time
                decimalSymbols.put(locCode, symbols);
            } else {
                // Very odd...
                // Get our default set
                Log.e(Const.LOG_TAG, "Unable to find locale-specific decimal digits.  Using defaults.");
                symbols = decimalSymbols.get("");
                minusSymbol = '-';
                decimalSymbol = '.';
            }
        } else {
            // If getLocaleDecimalListener called more than once, set minus and decimal symbols
            // to those already set in symbols string.
            minusSymbol = symbols.charAt(12);
            decimalSymbol = symbols.charAt(10);
        }

        // Return a new listener using the proper digits, minus and decimal symbol
        return new LocaleDecimalKeyListener(symbols, minusSymbol, decimalSymbol);
    }

    /**
     * Convert Boolean value into a single digit.
     * 
     * @param givenValue
     *            : value which needs to convert into a single string.
     * @return null : if passed value is null. 1: if value is true;
     */
    public static Integer convertBooleanIntoInt(Boolean givenValue) {
        if (givenValue == null) {
            return null;
        } else {
            int result = 0;
            if (givenValue) {
                result = 1;
            }
            return result;
        }
    }

    /**
     * Convert String array into a single String. All the elements are separated by single comma.
     * 
     * @param givenArray
     *            : arrya which needs to convert into a single string.
     * @return Single String.
     */
    public static String convertArrayIntoString(String[] givenArray) {
        StringBuilder builder = new StringBuilder("");
        if (givenArray != null) {
            if (givenArray.length > 0) {
                builder.append(givenArray[0]);
                for (int i = 1; i < givenArray.length; i++) {
                    builder.append(',');
                    builder.append(givenArray[i]);
                }
            }
        }
        return builder.toString();
    }

    /**
     * Convert int value into Boolean.
     * 
     * @param givenValue
     *            : value which needs to convert into boolean.
     * @return null : if passed value is null. True: if value is >=1;
     */
    public static Boolean getValueFromInt(Integer givenValue) {
        if (givenValue == null) {
            return null;
        }
        if (givenValue >= 1) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public static String[] convertStringToArray(String value) {
        if (value == null)
            return null;
        String[] result = value.split(",");
        return result;
    }

    /**
     * Check null value. If value is null return empty string.
     * 
     * @param value
     *            : String value which requires null check.
     * **/
    public static String nullCheckForString(String value) {
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    /**
     * Test Drive users that register on a mobile device have their FirstName and LastName set as their email address on the
     * server, so EmployeeName comes back "email, email". Here we check if the user is a test drive user, and return only one
     * email address if they are.
     * 
     * @param employeeName
     *            EmployeeName returned from the server
     * 
     * @return Updated employeeName as single email address if Test Drive user, else EmployeeName
     */
    public static String formatEmployeeName(String employeeName) {
        if (Preferences.isTestDriveUser()) {
            if (employeeName.indexOf('@') != -1) {
                int commaIndex = employeeName.indexOf(',');
                if (commaIndex != -1) {
                    return employeeName.substring(0, commaIndex);
                }
            }
        }
        return employeeName;
    }

}

/**
 * An extension of <code>NumberKeyListener</code> that returns the accepted symbols and an input type. The keyboard manager code
 * deployed on some models will query the input type for an installed keylistener in order to know what keys to enable.
 */
class LocaleDecimalKeyListener extends NumberKeyListener {

    private char[] acceptedSymbols;
    private int inputType;
    private char decimalSym;
    private char minusSym;

    // MOB-12781 Base 10 values for direct comparison to char
    private static final char shortMinus = '\u002d';
    private static final char longMinus = '\u2212';

    /*
     * MOB-12781 Constructor takes all accepted symbols and explicit values of minus and decimal symbol to prevent an error if
     * order of accepted symbols were to change.
     */
    public LocaleDecimalKeyListener(String accepted, char minusSymbol, char decimalSymbol) {
        acceptedSymbols = new char[accepted.length()];
        accepted.getChars(0, accepted.length(), this.acceptedSymbols, 0);

        minusSym = minusSymbol;
        decimalSym = decimalSymbol;

        // MOB-11669 - If this is a 'flagged' phone and we're using a comma as the decimal
        // separate (e.g. France or Germany), then we should set the softkeyboard to use
        // the Phone layout. This will allow the user to input a comma as the decimal separator.
        String device = Build.MANUFACTURER.toUpperCase();
        if (Const.FLAGGED_LOCALE_KEYBOARD_LAYOUT.contains(device) && this.decimalSym == ',') {
            this.inputType = InputType.TYPE_CLASS_PHONE;
        } else {
            this.inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED
                    | InputType.TYPE_NUMBER_FLAG_DECIMAL;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.text.method.NumberKeyListener#getAcceptedChars()
     */
    @Override
    protected char[] getAcceptedChars() {
        return acceptedSymbols;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.text.method.KeyListener#getInputType()
     */
    @Override
    public int getInputType() {
        return inputType;
    }

    /*
     * Copied from <code>DigitsKeyListener.filter()</code>
     * 
     * (non-Javadoc)
     * 
     * @see android.text.method.NumberKeyListener#filter(java.lang.CharSequence, int, int, android.text.Spanned, int, int)
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        /*
         * MOB-12781 Swedish locale uses the longMinus char but the keyboard has the short minus char, so we change any minus
         * symbols in the source (keyboard or paste option) to locale. Then we use the new string (as CharSequence) as the source
         * for the rest of the method.
         */
        StringBuilder adjustMinus = new StringBuilder(source);

        for (int i = 0; i < adjustMinus.length(); i++) {
            if (adjustMinus.charAt(i) == shortMinus || adjustMinus.charAt(i) == longMinus)
                adjustMinus.setCharAt(i, minusSym);
        }

        CharSequence stringReceived = adjustMinus.subSequence(0, adjustMinus.length());

        CharSequence out = super.filter(stringReceived, start, end, dest, dstart, dend);

        if (out != null) {
            stringReceived = out;
            start = 0;
            end = out.length();
        }

        int sign = -1;
        int decimal = -1;
        int dlen = dest.length();

        /*
         * Find out if the existing text has '-' or '.' characters.
         */

        for (int i = 0; i < dstart; i++) {
            char c = dest.charAt(i);

            if (c == minusSym) {
                sign = i;
            } else if (c == decimalSym) {
                decimal = i;
            }
        }
        for (int i = dend; i < dlen; i++) {
            char c = dest.charAt(i);

            if (c == minusSym) {
                return ""; // Nothing can be inserted in front of a '-'.
            } else if (c == decimalSym) {
                decimal = i;
            }
        }

        /*
         * If it does, we must strip them out from the source. In addition, '-' must be the very first character, and nothing can
         * be inserted before an existing '-'. Go in reverse order so the offsets are stable.
         */

        SpannableStringBuilder stripped = null;

        for (int i = end - 1; i >= start; i--) {
            char c = stringReceived.charAt(i);
            boolean strip = false;

            if (c == minusSym) {
                if (i != start || dstart != 0) {
                    strip = true;
                } else if (sign >= 0) {
                    strip = true;
                } else {
                    sign = i;
                }
            } else if (c == decimalSym) {
                if (decimal >= 0) {
                    strip = true;
                } else {
                    decimal = i;
                }
            }

            if (strip) {
                if (end == start + 1) {
                    return ""; // Only one character, and it was stripped.
                }

                if (stripped == null) {
                    stripped = new SpannableStringBuilder(stringReceived, start, end);
                }

                stripped.delete(i - start, i + 1 - start);
            }
        }

        if (stripped != null) {
            return stripped;
        } else if (out != null) {
            return out;
        } else {
            return null;
        }
    }
}