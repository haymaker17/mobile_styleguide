package com.concur.mobile.platform.ui.expense.util;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.concur.mobile.platform.ui.common.util.PreferenceUtil;

public class ExpensePreferenceUtil {

    public static final String CLS_TAG = ExpensePreferenceUtil.class.getSimpleName();

    /**
     * Gets whether the transaction date for a credit card expense should be editable.
     * 
     * @param context
     *            references an application context.
     * @return returns <code>true</code> if credit card expense transactions are editable; <code>false</code> otherwise.
     */
    public static boolean isCardTransDateEditable(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_EDIT_CARD_TRANS_DATE, false);
    }

    /**
     * Will save the last selected location value.
     * 
     * @param concurCore
     *            the application.
     * @param ctx
     *            the context.
     * @param liKey
     *            contains the list item key.
     * @param liCode
     *            contains the list item code.
     * @param value
     *            contains the list item value.
     */
    public static void saveLocationSelection(Context ctx, String liKey, String liCode, String value) {
        String lastSavedLocation = String.format(Locale.US, "%s:%s:%s", liKey, liCode, value);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        long curTimeMillis = System.currentTimeMillis();
        // Save the last quick expense transaction and current time.
        PreferenceUtil.savePreference(prefs, Const.PREF_LAST_SAVED_LOCATION_SELECTION_TIME, curTimeMillis);
        PreferenceUtil.savePreference(ctx, Const.PREF_LAST_SAVED_LOCATION_SELECTION, lastSavedLocation);
    }

    /**
     * Saves the last used currency code into preferences.
     * 
     * @param concurCore
     *            the application.
     * @param ctx
     *            the context.
     * @param userCrnCode
     *            contains the currency code to save.
     */
    public static void saveLastUsedCrnCode(Context ctx, String userCrnCode) {
        PreferenceUtil.savePreference(ctx, Const.PREF_LAST_USED_CRN_CODE, userCrnCode);
    }

}
