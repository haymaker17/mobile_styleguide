package com.concur.mobile.base.util;

import android.content.Context;

/**
 * This class provides some helpful general methods for formatting text.
 */
public class Format {

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
