package com.concur.mobile.platform.ui.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceUtil {

    private static final String CLS_TAG = FormUtil.class.getSimpleName();

    private PreferenceUtil() {
    }

    /**
     * Save a preference value into the default {@link SharedPreferences} used by the application.
     * 
     * @param ctx
     *            The {@link Context}. Typically a calling {@link Activity}
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link String} value to save
     */
    public static void savePreference(Context ctx, String name, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        savePreference(prefs, name, value);
    }

    /**
     * Save a preference value into the default {@link SharedPreferences} used by the application.
     * 
     * @param ctx
     *            The {@link Context}. Typically a calling {@link Activity}
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link Boolean} value to save
     */
    public static void savePreference(Context ctx, String name, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        savePreference(prefs, name, value);
    }

    /**
     * Save a preference value into an existing {@link SharedPreferences}
     * <p>
     * replaces com.concur.mobile.core.ConcurCore#savePreference(SharedPreferences prefs, String name, String value)
     * </p>
     * 
     * @param prefs
     *            A {@link SharedPreferences} to hold the preference.
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link String} value to save
     */
    public static void savePreference(SharedPreferences prefs, String name, String value) {
        Editor e = prefs.edit();
        e.putString(name, value);
        e.commit();
    }

    /**
     * Save a preference value into an existing {@link SharedPreferences}
     * 
     * @param prefs
     *            A {@link SharedPreferences} to hold the preference.
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link Long} value to save
     */
    public static void savePreference(SharedPreferences prefs, String name, Long value) {
        Editor e = prefs.edit();
        e.putLong(name, value);
        e.commit();
    }

    /**
     * Save a preference value into an existing {@link SharedPreferences}
     * 
     * @param prefs
     *            A {@link SharedPreferences} to hold the preference.
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link Integer} value to save
     */
    public static void savePreference(SharedPreferences prefs, String name, Integer value) {
        Editor e = prefs.edit();
        e.putInt(name, value);
        e.commit();
    }

    /**
     * Save a preference value into an existing {@link SharedPreferences}
     * 
     * @param prefs
     *            A {@link SharedPreferences} to hold the preference.
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link Boolean} value to save
     */
    public static void savePreference(SharedPreferences prefs, String name, boolean value) {
        Editor e = prefs.edit();
        e.putBoolean(name, value);
        e.commit();
    }

}
