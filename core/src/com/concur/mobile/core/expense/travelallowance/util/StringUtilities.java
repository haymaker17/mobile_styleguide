package com.concur.mobile.core.expense.travelallowance.util;

/**
 * Created by Michael Becherer on 17-Jun-15.
 */
public final class StringUtilities {
    /**
     * The empty {@code String}, i.e. a value of "".
     */
    public static final String EMPTY_STRING = "";

    /**
     * Tests if the {@code String} given by <code>text</code> is either
     * <code>null</code> or an empty {@code String}.
     *
     * @param string
     *            the {@code String} to test
     * @return <code>true</code> if <code>text</code> is either
     *         <code>null</code> or empty; <code>false</code> otherwise
     */
    public static boolean isNullOrEmpty(final String string) {
        return string == null || string.length() == 0;
    }

    /**
     * Converts the given character representation into its corresponding boolean.
     * Supports input strings "Y" and "y" to be converted into boolean true. All others
     * are converted intto boolean false.
     * @param text The text to be converted
     * @return true, if the text is of "Y" or "y". Otherwise false.
     */
    public static boolean toBoolean (String text) {
        if (text != null && text.equalsIgnoreCase("Y")) {
            return true;
        }
        return false;
    }
}
