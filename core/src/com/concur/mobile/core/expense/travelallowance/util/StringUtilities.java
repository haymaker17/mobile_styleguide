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
}
