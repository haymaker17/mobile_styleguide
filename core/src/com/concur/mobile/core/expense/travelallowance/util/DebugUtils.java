package com.concur.mobile.core.expense.travelallowance.util;

import android.util.Log;

/**
 * Created by Michael Becherer on 31-Jul-15.
 */
public final class DebugUtils {
    /**
     * Log Tag used by Travel Allowance
     */
    public static String LOG_TAG_TA = "TA";

    /**
     * Convenience method to build the log message according to the pattern
     * {@code <className>}.{@code<methodName>}: {@code <info>}
     * @param className The name of the class
     * @param methodName The name of the method
     * @param info The info to be logged
     * @return The resulting text to be logged
     */
    public static String buildLogText(String className, String methodName, String info) {
        return (className + "." + methodName + ": " + info);
    }

}
