package com.concur.mobile.core.expense.travelallowance.util;

import android.util.Log;

/**
 * Created by Michael Becherer on 31-Jul-15.
 */
public final class DebugUtils {
    public static String LOG_TAG_TA = "TA";

    public static void LogDTA(String className, String methodName, String text) {
        Log.d(LOG_TAG_TA, className + "." + methodName + ": " + text);
    }

    public static void LogETA(String className, String methodName, String text) {
        Log.e(LOG_TAG_TA, className + "." + methodName + ": " + text);
    }
}
