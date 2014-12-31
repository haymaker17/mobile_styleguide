package com.concur.mobile.core.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.concur.mobile.core.activity.Preferences;

public class RolesUtil {

    /**
     * Whether the currently logged in user is an expense approver.
     * 
     * @return whether the currently logged in end-user is an expense approver.
     */
    public static boolean isExpenseApprover(Context context) {
        boolean retVal = false;
        if (Preferences.shouldAllowReportApprovals()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            retVal = prefs.getBoolean(Const.PREF_CAN_EXPENSE_APPROVE, false);
        }
        return retVal;
    }

    /**
     * Whether the currently logged in user is a Travel Request approver.
     * 
     * @return whether the currently logged in user is a Travel Request approver.
     */
    public static boolean isTRApprover(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_TR_APPROVE, false);
    }

    /**
     * Whether the currently logged in end-user is a traveler.
     * 
     * @return whether the currently logged in end-user is a traveler.
     */
    public static boolean isTraveler(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_TRAVEL, false);
    }

    /**
     * Whether the currently logged in end-user is an itinerary viewer.
     * 
     * @return whether the currently logged in end-user is an itinerary viewer.
     */
    public static boolean isItinViewer(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_IS_ITIN_VIEWER, false);
    }

    /**
     * Whether the currently logged in end-user is an expense user.
     * 
     * @return whether the currently logged in end-user is an expense user.
     */
    public static boolean isExpenser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_EXPENSE, false);
    }

    /**
     * Whether the currently logged in end-user can approve Invoices.
     * 
     * @return whether the currently logged in end-user can approve Invoices.
     */
    public static boolean isInvoiceApprover(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_IS_INVOICE_APPROVER, false);
    }

    /**
     * Whether the currently logged in user is a Travel approver.
     * 
     * @return whether the currently logged in user is a Travel approver.
     */
    public static boolean isTravelApprover(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_TRAVEL_APPROVE, false);
    }

    /**
     * Whether the currently logged in end-user can view and submit Invoices.
     * 
     * @return whether the currently logged in end-user can view and submit Invoices.
     */
    public static boolean isInvoiceUser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_IS_INVOICE_USER, false);
    }

    /**
     * Whether the currently logged in end-user is a Breeze user.
     * 
     * @return Whether the currently logged in end-user is a Breeze user.
     */
    public static boolean isBreezeUser(Context context) {
        boolean isBreezeUser = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String entityType = prefs.getString(Const.PREF_ENTITY_TYPE, null);
        if (entityType != null) {
            isBreezeUser = entityType.equalsIgnoreCase(Const.ENTITY_TYPE_BREEZE);
        }
        return isBreezeUser;
    }

    /**
     * Returns <code>true</code> if the currently logged in end-user is not a Breeze user and if their login SiteSetting allows
     * location check in.
     * 
     * @return <code>true</code> if the currently logged in end-user is not a Breeze user and if their login SiteSetting allows
     *         location check in.
     */
    public static boolean canCheckInLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isLocCheckInEnabled = prefs.getBoolean(Const.PREF_CAN_CHECK_IN_LOCATION, false);

        return !isBreezeUser(context) && isLocCheckInEnabled;
    }

    /**
     * Whether the currently logged in end-user can see open bookings.
     * 
     * 
     * @return whether the currently logged in end-user can see open bookings.
     */
    public static boolean showTripsForOpenBookingUser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hasOpenbookingRole = prefs.getBoolean(Const.PREF_SHOW_OPEN_BOOKING, false);

        return (hasOpenbookingRole);
    }

    /**
     * Whether logged in user is TestDriveUser
     * */
    public static boolean isTestDriveUser() {
        // Check whether or not this is a test drive user.
        boolean isTestDriveUser = Preferences.isTestDriveUser();
        return isTestDriveUser;
    }

    /**
     * Whether logged in user can see Price-to-Beat Generator
     * */
    public static boolean showPriceToBeatGenerator(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_SHOW_PRICE_TO_BEAT_MENU, false);
    }

    /**
     * Whether logged in user is Gov user
     * */
    public static boolean isGovUser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean returnVal = prefs.getBoolean(Const.PREF_CAN_GOV_USER, false);
        return returnVal;
    }
}
