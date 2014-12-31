package com.concur.mobile.platform.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.util.Log;

/**
 * Contains a set of utility methods for formatting data for platform requests.
 */
public class Format {

    // Contains the class tag used in log statements.
    private static final String CLS_TAG = "FormatUtil";

    private final static String SCHEME_DEV = "dev://";
    private final static int SCHEME_DEV_LENGTH = SCHEME_DEV.length();
    private final static String SCHEME_HTTP = "http://";
    private final static int SCHEME_HTTP_LENGTH = SCHEME_HTTP.length();
    private final static String SCHEME_HTTPS = "https://";
    private final static int SCHEME_HTTPS_LENGTH = SCHEME_HTTPS.length();

    /**
     * Determines based on examining the current value for server address whether the address is pointing at a dev server. This
     * method determines this by comparing the scheme to the value <code>Format.SCHEME_DEV</code>. If equal, then this method will
     * return <code>true</code>; otherwise, will return <code>false</code>.
     * 
     * @return returns whether or not the server is pointing at a dev server.
     */
    public static boolean isDevServer(String serverAddress) {
        // Grab the server address
        if (serverAddress.startsWith(SCHEME_DEV)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the server address prepended with the appropriate scheme and with no trailing slash. If <code>serverAddress</code>
     * has a scheme of <code>Format.SCHEME_DEV</code>, then SSL will be disabled; otherwise, SSL will be enabled based on the
     * value of <code>enableSSL</code>.
     * 
     * @param enableSSL
     *            contains wehther or not SSL will be used, i.e., effecting the scheme in the return address.
     * @param serverAddress
     *            contains the server address.
     * @return returns the formatted server address.
     * 
     */
    public static String formatServerAddress(boolean enableSSL, String serverAddress) {

        // Grab the server address
        StringBuilder serverAdd = new StringBuilder();
        serverAdd.append(serverAddress);

        // Clean up
        if (SCHEME_HTTP.equalsIgnoreCase(serverAdd.substring(0, SCHEME_HTTP_LENGTH))) {
            serverAdd.delete(0, SCHEME_HTTP_LENGTH);
        } else if (SCHEME_HTTPS.equalsIgnoreCase(serverAdd.substring(0, SCHEME_HTTPS_LENGTH))) {
            serverAdd.delete(0, SCHEME_HTTPS_LENGTH);
        }

        char lastChar = serverAdd.charAt(serverAdd.length() - 1);
        if (lastChar == '/' || lastChar == '\\') {
            serverAdd.deleteCharAt(serverAdd.length() - 1);
        }

        if (SCHEME_DEV.equalsIgnoreCase(serverAdd.substring(0, SCHEME_DEV_LENGTH))) {
            enableSSL = false;
            serverAdd.delete(0, SCHEME_DEV_LENGTH);
        }

        if (enableSSL) {
            serverAdd.insert(0, SCHEME_HTTPS);
        } else {
            serverAdd.insert(0, SCHEME_HTTP);
        }

        return serverAdd.toString();
    }

    /**
     * Will generate a URI given a server address, a path and whether or not SSL should be used.
     * 
     * @param enableSSL
     *            contains whether or not SSL should be used.
     * @param serverAddress
     *            contains the server address.
     * @param path
     *            contains the path.
     * @return returns an instance of <code>URI</code> if <code>serverAddress</code> and <code>path</code> are valid; otherwise,
     *         <code>null</code> is returned.
     */
    public static URI formatServerURI(boolean enableSSL, String serverAddress, String path) {
        // Build our URI
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(formatServerAddress(enableSSL, serverAddress));
        strBldr.append(path);

        URI uri = null;

        try {
            uri = new URI(strBldr.toString());
        } catch (URISyntaxException e) {
            Log.w(Const.LOG_TAG, CLS_TAG + ".formatServerURI: failed to build URI for: " + path, e);
        }
        return uri;
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
     * Format a timestamp with null checking
     * 
     * @param formatter
     *            A {@link DateFormat} with the desired settings
     * @param cal
     *            The untested {@link Calendar} value to format
     * @return If successful, the timestamp in the proper format. If unsuccesful, a blank string.
     */
    public static String safeFormatCalendar(DateFormat formatter, Calendar cal) {
        String dateString = "";

        if (cal != null) {
            dateString = safeFormatDate(formatter, cal.getTime());
        }

        return dateString;
    }

    /**
     * Format a timestamp with null checking
     * 
     * @param formatter
     *            A {@link DateFormat} with the desired settings
     * @param date
     *            The untested {@link Date} value to format
     * @return If successful, the timestamp in the proper format. If unsuccesful, a blank string.
     */
    public static String safeFormatDate(DateFormat formatter, Date date) {
        String dateString = "";

        // fix for MOB-20041
        // MOB-20041 : needs to reopen. This code is not handling localization and UTC timezone very well
        // String localPattern = ((SimpleDateFormat) formatter).toLocalizedPattern();
        // formatter = new SimpleDateFormat(localPattern, Locale.getDefault());
        // formatter.setTimeZone(TimeZone.getTimeZone("UTC")); // MOB-19835 MOB-19844: Fixed timezone issue.

        if (date != null) {
            dateString = formatter.format(date);
        }

        return dateString;
    }

    /**
     * Retrieve the localized text for the given id and replace any tokens in the string. Tokens are integers delimited by double
     * percent signs (%%1%%). The integer is used to index into the values array to find the replacement value. The index is
     * 0-based.
     * 
     * @param context
     *            A {@link Context} object for finding the localized resources
     * @param id
     *            The resource ID for the string to retrieve
     * @param values
     *            A {@link String} array of the replacement values.
     * @return The localized string with placeholder tokens replaced with actual values
     */
    public static String localizeText(Context context, int id, Object... values) {
        String localString = context.getText(id).toString();
        return localizeText(context, localString, values);
    }

    /**
     * Retrieve the localized text for the given id and replace any tokens in the string. Tokens are integers delimited by double
     * percent signs (%%1%%). The integer is used to index into the values array to find the replacement value. The index is
     * 0-based.
     * 
     * @param context
     *            A {@link Context} object for finding the localized resources
     * @param localString
     *            A (@link String) with in-place tokens in need of replacement
     * @param values
     *            A {@link String} array of the replacement values.
     * @return The localized string with placeholder tokens replaced with actual values
     */
    public static String localizeText(Context context, String localString, Object... values) {

        if (values != null && values.length > 0) {
            final String delim = "%%";
            final int delimSize = delim.length();
            final String descSeparator = "!";

            // Builder to hold the final string
            StringBuilder sb = new StringBuilder();

            // Set up our initial positions
            int startPos = localString.indexOf(delim);
            int endPos = 0;

            // Run through the string
            while (startPos > -1) {

                // Append the part of the original string up to this token
                sb.append(localString.substring(endPos, startPos));

                // Step past the delimiter and look for the end delimiter
                startPos += delimSize;
                endPos = localString.indexOf(delim, startPos);

                // Pull out the token and turn it into an index
                int index;
                try {
                    String token = localString.substring(startPos, endPos);
                    int sepPos = token.indexOf(descSeparator);

                    // Split out the desc bit
                    token = token.substring(0, sepPos > 0 ? sepPos : token.length());

                    index = Integer.parseInt(token);

                } catch (NumberFormatException nfe) {
                    index = -1;
                }

                // Grab the replacement value
                if (index > -1 && index < values.length) {
                    Object o = values[index];
                    String s = "";
                    if (o != null) {
                        s = o.toString();
                    }

                    sb.append(s);
                }

                // Step past the end delimiter and get ready for the next one
                endPos += delimSize;
                startPos = localString.indexOf(delim, endPos);
            }

            // If we finish up with characters ahead then just glob them onto the end
            if (endPos <= localString.length()) {
                sb.append(localString.substring(endPos));
            }

            localString = sb.toString();

        }

        return localString;
    }

}
