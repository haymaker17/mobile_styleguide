/**
 * 
 */
package com.concur.mobile.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportException;
import com.concur.mobile.core.expense.report.service.SaveReportReply;
import com.concur.mobile.core.travel.data.RuleEnforcementLevel;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.travel.hotel.data.HotelChoice;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

/**
 * Contains a collection of utility methods to build particular views based on data type.
 * 
 * @author AndrewK
 */
public class ViewUtil {

    // Severity Level warn string.
    private static final String SEVERITY_LEVEL_WARN_STR = "warn";

    // Severity Level warning string.
    private static final String SEVERITY_LEVEL_WARNING_STR = "warning";

    // Severity Level error string.
    private static final String SEVERITY_LEVEL_ERROR_STR = "error";

    /**
     * An enumeration that defines an exception severity level.
     */
    public enum ExceptionSeverityLevel {
        NONE, WARN, ERROR
    };

    /**
     * An enumeration for supported document types.
     */
    public enum DocumentType {
        PNG, JPG, PDF, UNKNOWN
    };

    /**
     * Provides a recommendation for a sampling and quality settings for shrinking the size of a receipt image.
     */
    public static class SampleSizeCompressFormatQuality {

        /**
         * Contains the recommended sampling size.
         */
        public int sampleSize = Const.RECEIPT_SOURCE_BITMAP_SAMPLE_SIZE;

        /**
         * Contains the recommended compression quality.
         */
        public int compressQuality = Const.RECEIPT_COMPRESS_BITMAP_QUALITY;

        /**
         * Contains the recommended compression format.
         */
        public CompressFormat compressFormat = Const.RECEIPT_COMPRESS_BITMAP_FORMAT;

    };

    /**
     * Models a location selection value.
     */
    public static class LocationSelection {

        public String liKey;

        public String liCode;

        public String value;

    }

    public static final String CLS_TAG = ViewUtil.class.getSimpleName();

    /**
     * Utility method which will clear the web view cache, both in memory + disk files.
     * 
     * @param ctx
     *            an application context.
     */
    public static void clearWebViewCache(Context ctx) {
        // Clear the cache directory.
        clearCacheFolder(ctx.getCacheDir());
        // Clear databases.
        File databaseFilePath = ctx.getDatabasePath("webview.db");
        if (databaseFilePath != null && databaseFilePath.exists()) {
            try {
                if (!databaseFilePath.delete()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".clearWebViewCache: unable to delete database file '"
                            + databaseFilePath.getName() + ".");
                }
            } catch (Exception exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".clearWebViewCache: exception deleting database file.", exc);
            }
        }
        databaseFilePath = ctx.getDatabasePath("webviewCache.db");
        if (databaseFilePath != null && databaseFilePath.exists()) {
            try {
                if (!databaseFilePath.delete()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".clearWebViewCache: unable to delete database file '"
                            + databaseFilePath.getName() + "'.");
                }
            } catch (Exception exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".clearWebViewCache: exception deleting database file.", exc);
            }
        }
    }

    /**
     * Will clear all cookies stored by the application as a result of using a <code>WebView</code>.
     * 
     * @param ctx
     *            contains a reference to an application context.
     */
    public static void clearWebViewCookies(Context ctx) {
        CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(ctx);
        CookieManager cookieMngr = CookieManager.getInstance();
        cookieMngr.setAcceptCookie(true);

        // Perform a sync of the cookies and then punt them.
        cookieSyncMngr.sync();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException intExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".clearWebViewCookies: interrupted in a sleep!", intExc);
        }
        cookieMngr.removeAllCookie();
    }

    /**
     * Will clear the cache folder.
     * 
     * @param dir
     *            the directory containing the cache.
     */
    private static void clearCacheFolder(final File dir) {
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {

                    // first delete subdirectories recursively
                    if (child.isDirectory()) {
                        clearCacheFolder(child);
                    }

                    // then delete the files and subdirectories in this dir
                    child.delete();
                }
            } catch (Exception e) {
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".clearCacheFolder: "
                                + String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
    }

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
     * Gets whether the currently logged in user has permission to book air.
     * 
     * @param context
     *            an application context.
     * @return returns <code>true</code> if the user can book air; <code>false</code> otherwise.
     */
    public static boolean isAirUser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_AIR, false);
    }

    /**
     * Whether the currently logged in end-user is a rail user.
     * 
     * @param context
     *            an application context.
     * 
     * @return whether the currently logged in end-user is a rail user.
     * 
     */
    public static boolean isRailUser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_RAIL, false);
    }

    /**
     * Gets whether the currently logged in user is a Flex Fare user.
     * 
     * @param context
     *            an application context.
     * @return returns <code>true</code> if the user is a flex fare user; <code>false</code> otherwise.
     */
    public static boolean isFlexFareUser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_FLEX_FARE, false);
    }

    /**
     * Gets whether or not personal car mileage should be displayed on the home screen.
     * 
     * @param context
     *            an application context.
     * @return returns <code>true</code> if the preferences exists and is set to true; <code>false</code> otherwise.
     */
    public static boolean isShowMileageExpenseOnHomeScreenEnabled(Context context) {
        boolean retVal = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        retVal = prefs.getBoolean(Const.PREF_CAN_SHOW_PERSONAL_CAR_MILEAGE_HOME, false);
        return retVal;
    }

    /**
     * Gets whether or not a non-refundable warning message should be displayed when booking a non-refundable fare.
     * 
     * @param context
     *            an application context.
     * @return returns <code>true</code> if a non-refundable warning message preferences exists and is set to true;
     *         <code>false</code> otherwise.
     */
    public static boolean isShowNonRefundableMessageEnabled(Context context) {
        boolean retVal = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        retVal = prefs.getBoolean(Const.PREF_SHOW_NONREFUNDABLE_MESSAGE, false);
        return retVal;
    }

    /**
     * Gets whether the current user is a travel user.
     * 
     * @param context
     *            the application context.
     * @return returns <code>true</code> if the current user is a travel user; <code>false</code> otherwise.
     */
    public static boolean isTravelUser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_TRAVEL, false);
    }

    /**
     * Whether the currently logged in end-user can approve Purchase Requests.
     * 
     * @return whether the currently logged in end-user can approve Purchase Requests.
     */
    public static boolean isPurchaseRequestApprover(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_IS_PURCHASE_REQUEST_APPROVER, false);
    }

    /**
     * Gets whether the current user is a government user.
     * 
     * @param context
     *            the application context.
     * @return returns <code>true</code> if the current user is a government user; <code>false</code> otherwise.
     */
    public static boolean isGovUser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_GOV_USER, false);
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
     * Whether the currently logged in end-user is a Corporate user.
     * 
     * @return Whether the currently logged in end-user is a Corporate user.
     */
    public static boolean isCorporateUser(Context context) {
        boolean isCorporateUser = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String entityType = prefs.getString(Const.PREF_ENTITY_TYPE, null);
        if (entityType != null) {
            isCorporateUser = entityType.equalsIgnoreCase(Const.ENTITY_TYPE_CORPORATE);
        }
        return isCorporateUser;
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

    public static boolean isExpenseApprover(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_EXPENSE_APPROVE, false);
    }

    /**
     * Gets whether the currently logged in users travel profile is complete.
     * 
     * @return returns <code>true</code>if the user's travel profile is complete; <code>false</code> otherwise.
     */
    public static boolean isTravelProfileComplete(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int travelProfile = prefs.getInt(Const.PREF_TRAVEL_PROFILE_STATUS,
                Const.TRAVEL_PROFILE_ALL_REQUIRED_DATA_PLUS_TSA);
        return (travelProfile == Const.TRAVEL_PROFILE_ALL_REQUIRED_DATA_PLUS_TSA);
    }

    /**
     * Gets whether the travel profile is complete except for missing TSA data.
     * 
     * @param context
     *            the application context.
     * @return returns whether the travel profile is complete except for missing TSA data.
     */
    public static boolean isTravelProfileCompleteMissingTSA(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int travelProfile = prefs.getInt(Const.PREF_TRAVEL_PROFILE_STATUS,
                Const.TRAVEL_PROFILE_ALL_REQUIRED_DATA_PLUS_TSA);
        return (travelProfile == Const.TRAVEL_PROFILE_ALL_REQUIRED_DATA_MISSING_TSA);
    }

    /**
     * Gets whether the current user is a travel request user.
     * 
     * @param context
     *            the application context.
     * @return returns <code>true</code> if the current user is a travel request user; <code>false</code> otherwise.
     */
    public static boolean isTravelRequestUser(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_HAS_TR, false) && prefs.getBoolean(Const.PREF_CAN_TR, false);
    }

    /**
     * Whether the currently logged in users company has custom required fields.
     * 
     * @return returns <code>true</code> if the users company has required custom fields; <code>false</code> otherwise.
     */
    public static boolean hasCustomRequiredFields(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_REQUIRED_CUSTOM_FIELDS, false);
    }

    /**
     * Will determine whether the Receipt Store UI should be hidden from the end-user.
     * 
     * @param context
     *            an application context.
     * @return whether the Receipt Store should be hidden from the end-user. Defaults to <code>false</code> if the preference
     *         doesn't exist.
     */
    public static boolean isReceiptStoreHidden(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_RECEIPT_STORE_HIDDEN, false);
    }

    public static boolean hasFixedTA(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_HAS_FIXED_TA, false);
    }

    public static boolean hasTravelAllowanceFixed(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_HAS_TRAVEL_ALLOWANCE_FIXED, false);
    }

    public static boolean isConditionalFieldEvaluationEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_ALLOW_CONDITIONAL_FIELD_EVALUATION, false);
    }

    /**
     * Gets whether the current user is a LNA user.
     * 
     * @param context
     *            the application context.
     * @return returns <code>true</code> if the current user is a LNA user; <code>false</code> otherwise.
     */
    public static boolean isLNAUser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Const.PREF_CAN_LNA_USER, false);
    }

    /**
     * Will determine the mapping from an integer-based enforcement level to an instance of <code>RuleEnforcementLevel</code>.
     * 
     * @param level
     *            the enforcement level from a violation.
     * @return an instance of <code>RuleEnforcementLevel</code>.
     */
    public static RuleEnforcementLevel getRuleEnforcementLevel(Integer level) {
        RuleEnforcementLevel ruleLevel = RuleEnforcementLevel.NONE;
        if (level != null) {
            if (level < 10 || level == 100) {
                ruleLevel = RuleEnforcementLevel.NONE;
            } else if (level == 10 || level == 20) {
                ruleLevel = RuleEnforcementLevel.WARNING;
            } else if (level == 25 || level == 30) {
                ruleLevel = RuleEnforcementLevel.ERROR;
            } else if (level == 40) {
                ruleLevel = RuleEnforcementLevel.INACTIVE;
            } else if (level == 50) {
                ruleLevel = RuleEnforcementLevel.HIDE;
            }
        }
        return ruleLevel;
    }

    /**
     * Gets the text appearance style resource id to be applied to a form field label based on the maximum enforcement level
     * within a list of <code>Violation</code> objects.
     * 
     * @param violations
     *            the list of violations.
     * @return an text style attribute.
     */
    public static int getFormFieldLabelStyle(List<Violation> violations) {
        int retVal = R.style.FormFieldLabelGreenFareText;
        int enforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(violations);
        switch (ViewUtil.getRuleEnforcementLevel(enforcementLevel)) {
        case ERROR: {
            retVal = R.style.FormFieldLabelRedFareText;
            break;
        }
        case WARNING: {
            retVal = R.style.FormFieldLabelYellowFareText;
            break;
        }
        case NONE: {
            retVal = R.style.FormFieldLabelGreenFareText;
            break;
        }
        case INACTIVE: {
            retVal = R.style.FormFieldLabelGrayFareText;
            break;
        }
        }
        return retVal;
    }

    /**
     * Gets the text appearance style resource id to be applied to a form field value based on the maximum enforcement level
     * within a list of <code>Violation</code> objects.
     * 
     * @param violations
     *            the list of violations.
     * @return an text style attribute.
     */
    public static int getFormFieldValueStyle(List<Violation> violations) {
        int retVal = R.style.FormFieldValueGreenFareText;
        int enforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(violations);
        switch (ViewUtil.getRuleEnforcementLevel(enforcementLevel)) {
        case ERROR: {
            retVal = R.style.FormFieldValueRedFareText;
            break;
        }
        case WARNING: {
            retVal = R.style.FormFieldValueYellowFareText;
            break;
        }
        case NONE: {
            retVal = R.style.FormFieldValueGreenFareText;
            break;
        }
        case INACTIVE: {
            retVal = R.style.FormFieldValueGrayFareText;
            break;
        }
        }
        return retVal;
    }

    /**
     * Will determine whether the list of violations represent helpful messages coded as violations.
     * 
     * @param violations
     *            the list of violations.
     * @return returns <code>true</code> if the messages contained in <code>violations</code> represent helpful messages;
     *         <code>false</code> otherwise.
     */
    public static boolean isEnforcementLevelForHelpfulMessages(List<Violation> violations) {
        boolean retVal = false;
        if (violations != null) {
            int maxEnforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(violations);
            retVal = (maxEnforcementLevel < 10 || maxEnforcementLevel == 100);
        }
        return retVal;
    }

    /**
     * Will determine, based on looking at the maximum enforcement level, whether a booking should be shown, but not permitted to
     * book.
     * 
     * @param violations
     *            the list of violations.
     * @return returns <code>true</code> if the messages indicate a booking should be displayed, but blocked from booking;
     *         <code>false</code> otherwise.
     */
    public static boolean showButNoBooking(List<Violation> violations) {
        boolean retVal = false;
        if (violations != null) {
            int maxEnforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(violations);
            RuleEnforcementLevel ruleEnfLevel = ViewUtil.getRuleEnforcementLevel(maxEnforcementLevel);
            retVal = (ruleEnfLevel == RuleEnforcementLevel.INACTIVE);
        }
        return retVal;
    }

    /**
     * Will return the maximum enforcment level over a list of violations.
     * 
     * @return the maximum enforcement level for any associated violations.
     */
    public static int getMaxRuleEnforcementLevel(List<Violation> violations) {
        int maxEnforcementLevel = 0;
        if (violations != null) {
            for (Violation violation : violations) {
                maxEnforcementLevel = Math.max(maxEnforcementLevel, violation.enforcementLevel);
            }
        }
        return maxEnforcementLevel;
    }

    /**
     * Will get a reference to a directory on the external storage medium where applicaton specific files can be written that are
     * not considered "media".
     * 
     * @param context
     *            the application context.
     * @return a reference to a <code>File</code> representing the external storage app files directory; otherwise
     *         <code>null</code> is returned.
     */
    public static File getExternalFilesDir(Context context) {
        File extFilesDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File extStoreDir = Environment.getExternalStorageDirectory();
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(extStoreDir.getAbsolutePath());
            strBldr.append(File.separatorChar);
            strBldr.append("Android");
            strBldr.append(File.separatorChar);
            strBldr.append("data");
            strBldr.append(File.separatorChar);
            strBldr.append(context.getPackageName());
            strBldr.append(File.separatorChar);
            strBldr.append("files");
            extFilesDir = new File(strBldr.toString());
            if (!extFilesDir.mkdirs()) {
                if (!extFilesDir.exists()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getExternalFilesDir: unable to create external files directory '"
                            + extFilesDir.getAbsolutePath() + "'.");
                    extFilesDir = null;
                }
            }
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".getExternalFilesDir: media not mounted!");
        }
        return extFilesDir;
    }

    /**
     * Will map a profile check code onto a text resource id describing the profile check.
     * 
     * @param profileCheck
     *            the profile check integer-based value.
     * @return upon success, returns the text resource id; otherwise, <code>-1</code> is returned.
     */
    public static int getTextResourceIdForProfileCheck(int profileCheck) {
        int retVal = -1;
        switch (profileCheck) {
        case 1: {
            retVal = R.string.profile_check_1;
            break;
        }
        case 2: {
            retVal = R.string.profile_check_2;
            break;
        }
        case 20: {
            retVal = R.string.profile_check_20;
            break;
        }
        case 21: {
            retVal = R.string.profile_check_21;
            break;
        }
        case 22: {
            retVal = R.string.profile_check_22;
            break;
        }
        case 24: {
            retVal = R.string.profile_check_24;
            break;
        }
        case 25: {
            retVal = R.string.profile_check_25;
            break;
        }
        case 26: {
            retVal = R.string.profile_check_26;
            break;
        }
        case 27: {
            retVal = R.string.profile_check_27;
            break;
        }
        case 28: {
            retVal = R.string.profile_check_28;
            break;
        }
        case 29: {
            retVal = R.string.profile_check_29;
            break;
        }
        case 30: {
            retVal = R.string.profile_check_30;
            break;
        }
        case 31: {
            retVal = R.string.profile_check_31;
            break;
        }
        case 32: {
            retVal = R.string.profile_check_32;
            break;
        }
        case 33: {
            retVal = R.string.profile_check_33;
            break;
        }
        default: {
            if (profileCheck > 0) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".getTextResourceIdForProfileCheck: unknown profile check value '"
                        + Integer.toString(profileCheck) + "' - using default profile incomplete message.");
                retVal = R.string.profile_check_2;
            }
            break;
        }
        }
        return retVal;
    }

    /**
     * Sets the checked state of a <code>CheckedTextView</code> object.
     * 
     * @param container
     *            the view containing the checked text view.
     * @param checkedTextViewResId
     *            the resource id of the checked text view.
     * @param value
     *            the boolean checked state.
     */
    public static void setCheckedTextViewState(View container, int checkedTextViewResId, boolean value) {
        if (container != null) {
            CheckedTextView chkTxtView = (CheckedTextView) container.findViewById(R.id.field_name);
            if (chkTxtView != null) {
                chkTxtView.setChecked(value);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".setCheckedTextViewText: unable to locate 'field_name' CheckedTextView in view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setCheckedTextViewText: view is null!");
        }
    }

    /**
     * Gets the checked state of a <code>CheckedTextView</code> contained in <code>view</code> with resource id
     * <code>checkedTextViewResId</code>.
     * 
     * @param container
     *            the checked text view container.
     * @param checkedTextViewResId
     *            the checked text view resource ID.
     * @return returns <code>true</code> if the text view is checked; <code>false</code> otherwise.
     */
    public static boolean getCheckedTextViewState(View container, int checkedTextViewResId) {
        boolean retVal = false;
        if (container != null) {
            CheckedTextView chkTxtView = (CheckedTextView) container.findViewById(R.id.field_name);
            if (chkTxtView != null) {
                retVal = chkTxtView.isChecked();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".setCheckedTextViewText: unable to locate 'field_name' CheckedTextView in view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setCheckedTextViewText: view is null!");
        }
        return retVal;
    }

    /**
     * Will add a separator view to a view group.
     * 
     * @param context
     *            the context used to inflate the separator view.
     * @param root
     *            the parent of the inflated view.
     */
    public static void addSeparatorView(Context context, ViewGroup root) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.group_separator, root);
    }

    /**
     * Will add a separator view to a view group.
     * 
     * @param context
     *            the context used to inflate the separator view.
     * @param root
     *            the parent of the inflated view.
     * @param index
     *            the index at which to add the separator.
     */
    public static void addSeparatorView(Context context, ViewGroup root, int index) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.group_separator, null);
        root.addView(view, index);
    }

    /**
     * Will construct a view object appropriate to displaying an expense report as a list entry in a list of active reports.
     * 
     * @param context
     *            the application context.
     * @param convertView
     *            contains an existing view object to convert.
     * @param expRep
     *            the expense report.
     * 
     * @return a view of the expense report appropriate for display in a list of active reports.
     */
    public static View buildActiveReportListEntryView(Context context, View convertView, ExpenseReport expRep) {

        View view = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.expense_active_reports_row, null);
        } else {
            view = convertView;
        }
        if (view != null) {
            // Set the report name.
            TextView txtView = (TextView) view.findViewById(R.id.exp_rep_ent_row_report_name);
            if (txtView != null) {
                String reportName = "";
                if (expRep.reportName != null) {
                    reportName = expRep.reportName;
                }
                txtView.setText(reportName);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildActiveReportListEntryView: can't locate report name text view!");
            }
            // Set the report amount.
            txtView = (TextView) view.findViewById(R.id.exp_rep_ent_row_amount);
            if (txtView != null) {
                String amount = "";
                if (expRep.totalClaimedAmount != null) {
                    String formattedAmount = FormatUtil.formatAmount(expRep.totalClaimedAmount, context.getResources()
                            .getConfiguration().locale, expRep.crnCode, true);
                    if (formattedAmount != null) {
                        amount = formattedAmount;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".buildActiveReportListEntryView: null formatted amount!");
                    }
                }
                txtView.setText(amount);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildActiveReportListEntryView: can't locate report amount text view!");
            }
            // Set the report date.
            txtView = (TextView) view.findViewById(R.id.exp_rep_ent_row_expense_date);
            if (txtView != null) {
                String reportDate = "";
                Calendar reportDateCal = expRep.reportDateCalendar;
                if (reportDateCal != null) {
                    String formattedRepDate = Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY,
                            reportDateCal);
                    if (formattedRepDate != null) {
                        reportDate = formattedRepDate;
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildActiveReportListEntryView: null report date calendar!");
                }
                txtView.setText(reportDate);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildActiveReportListEntryView: can't locate report date text view!");
            }
            // If we're converting an existing view, force a relayout.
            if (convertView != null) {
                view.invalidate();
                view.requestLayout();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".buildActiveReportsListEntryView: unable to inflate layout 'expense_active_reports_row'");
        }
        return view;
    }

    /**
     * Will set the visibility of the network activity progress indicator.
     * 
     * @param activity
     *            the activity containing the progress indicator.
     * @param visibility
     *            the visiblity, should be one of <code>View.VISIBILE</code>, <code>View.INVISIBLE</code>, or
     *            <code>View.GONE</code>.
     */
    public static void setNetworkActivityIndicatorVisibility(Activity activity, int visibility, String text) {
        // Set the progress bar.
        ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.network_indicator);
        if (progressBar != null) {
            if (visibility == View.VISIBLE) {
                Animation fadeInAnim = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
                fadeInAnim.setDuration(350L);
                fadeInAnim.setFillAfter(false);
                progressBar.setVisibility(visibility);
                progressBar.startAnimation(fadeInAnim);
            } else {
                progressBar.setVisibility(visibility);
            }
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG
                    + ".setNetworkActivityIndicatorVisibility: network activity progress bar not found!");
        }
        // Set any status text.
        TextView networkActText = (TextView) activity.findViewById(R.id.network_indicator_text);
        if (networkActText != null) {
            if (visibility == View.VISIBLE) {
                if (text != null && text.length() > 0) {
                    networkActText.setText(text);
                    Animation fadeInAnim = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
                    fadeInAnim.setDuration(350L);
                    fadeInAnim.setFillAfter(false);
                    networkActText.setVisibility(visibility);
                    networkActText.startAnimation(fadeInAnim);
                } else {
                    // If there's no text to be displayed, then just make the view invisible.
                    networkActText.setVisibility(View.INVISIBLE);
                }
            } else {
                networkActText.setVisibility(visibility);
            }
        } else {
            if (text != null) {
                // Only log this if someone expected to set the text value
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".setNetworkActivityIndicatorVisibility: network activity text view not found!");
            }
        }
    }

    /**
     * Gets the severity level of the list of exceptions based on looking at the highest level (ERROR) and returning
     * <code>ERROR</code> value; otherwise either a <code>WARN</code> will be returned; or <code>NONE</code> if no uncleared
     * exceptions are found.
     * 
     * @param exceptions
     *            the list of passed in exceptions.
     * 
     * @return a value in the enum <code>ExceptionSeverityLevel</code>.
     */
    public static ExceptionSeverityLevel getExceptionListExceptionSeverityLevel(
            Iterator<ExpenseReportException> exceptions) {

        ExceptionSeverityLevel retVal = ExceptionSeverityLevel.NONE;
        boolean foundError = false;
        boolean foundWarning = false;
        while (exceptions.hasNext()) {
            ExpenseReportException expRepExc = exceptions.next();
            if (!expRepExc.isCleared()) {
                switch (getExceptionSeverityLevel(expRepExc.getSeverityLevel())) {
                case NONE:
                    // No-op.
                    break;
                case WARN:
                    foundWarning = true;
                    break;
                case ERROR:
                    foundError = true;
                    break;
                }
                // Short-circuit as soon as we have have found an error.
                if (foundError) {
                    break;
                }
            }
        }
        // Determine if we found an error, if so, then use the error icon; otherwise if we found a warning, use
        // the warning icon.
        if (foundError) {
            // At least one non-cleared error.
            retVal = ExceptionSeverityLevel.ERROR;
        } else if (foundWarning) {
            // At least one non-cleared warning.
            retVal = ExceptionSeverityLevel.WARN;
        }
        return retVal;
    }

    /**
     * Will iterate through all expense entries (and their itemizations) to determine if any of them have an undefined expense
     * type. <br>
     * <b>NOTE: currently, this comparison for "undefined" expense type is based on expense name, which can be localized and is
     * clearly not a good thing! This will need to change prior to shipping!
     * 
     * @param expRepDet
     *            the expense report detail object being examined for undefined expense types.
     * 
     * @return <code>true</code> if an undefined expense type was found; <code>false</code> otherwise.
     */
    public static boolean expenseReportHasUndefinedExpenseTypes(ExpenseReportDetail expRepDet) {
        boolean foundUndefinedExpenseType = false;

        Iterator<ExpenseReportEntry> entIter = expRepDet.getExpenseEntries().iterator();
        while (entIter.hasNext() && !foundUndefinedExpenseType) {
            ExpenseReportEntry expRepEnt = (ExpenseReportEntry) entIter.next();
            if (!expRepEnt.expenseName.equalsIgnoreCase("UNDEFINED")) {
                if (expRepEnt instanceof ExpenseReportEntryDetail) {
                    ExpenseReportEntryDetail expRepEntDet = (ExpenseReportEntryDetail) expRepEnt;
                    ArrayList<ExpenseReportEntry> itemizations = expRepEntDet.getItemizations();
                    if (itemizations != null) {
                        Iterator<ExpenseReportEntry> itemIter = itemizations.iterator();
                        while (itemIter.hasNext() && !foundUndefinedExpenseType) {
                            ExpenseReportEntry expRepItemEnt = itemIter.next();
                            if (expRepItemEnt.expenseName.equalsIgnoreCase("UNDEFINED")) {
                                foundUndefinedExpenseType = true;
                            }
                        }
                    }
                }
            } else {
                foundUndefinedExpenseType = true;
            }
        }
        return foundUndefinedExpenseType;
    }

    /**
     * Will convert a string containing an exception severity level and convert it into an instance of
     * <code>ViewUtil.ExceptionSeverityLevel</code>.
     * 
     * @param severityLevel
     *            the severity level to convert.
     * @return returns an instance of <code>ViewUtil.ExceptionSeverityLevel</code>. If an unknown severity level is passed in,
     *         then <code>ViewUtil.ExceptionSeverityLevel.NONE</code> is returned.
     */
    private static ExceptionSeverityLevel getExceptionSeverityLevel(String severityLevel) {
        ExceptionSeverityLevel retVal = ExceptionSeverityLevel.NONE;
        if (severityLevel != null) {
            if (severityLevel.equalsIgnoreCase(SEVERITY_LEVEL_ERROR_STR)) {
                retVal = ExceptionSeverityLevel.ERROR;
            } else if (severityLevel.equalsIgnoreCase(SEVERITY_LEVEL_WARN_STR)
                    || severityLevel.equalsIgnoreCase(SEVERITY_LEVEL_WARNING_STR)) {
                retVal = ExceptionSeverityLevel.WARN;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getExceptionSeverityLevel: unknown severityLevel value of '"
                        + severityLevel + "'.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExceptionSeverityLevel: severityLevel is null!");
        }
        return retVal;
    }

    /**
     * Will examine an expense report for exceptions and return the highest exception severity level.
     * 
     * @param expRep
     *            the expense report to examine.
     * 
     * @return the severity level.
     */
    public static ExceptionSeverityLevel getExpenseReportExceptionSeverityLevel(ExpenseReport expRep) {
        ExceptionSeverityLevel retVal = ExceptionSeverityLevel.NONE;
        if (expRep.hasException()) {
            if (expRep instanceof ExpenseReportDetail) {
                // Detail report objects have exception lists.
                ExpenseReportDetail expRepDet = (ExpenseReportDetail) expRep;
                if (expRepDet.hasException()) {
                    ArrayList<ExpenseReportException> expRepExcs = expRepDet.getExceptions();
                    if (expRepExcs != null) {
                        switch (getExceptionListExceptionSeverityLevel(expRepExcs.iterator())) {
                        case NONE:
                            // No-op.
                            break;
                        case WARN:
                            retVal = ExceptionSeverityLevel.WARN;
                            break;
                        case ERROR:
                            retVal = ExceptionSeverityLevel.ERROR;
                            break;
                        }
                    }
                }
            } else {
                // Summary report objects don't have exceptions so we'll rely upon
                // looking at the 'severityLevel' property.
                if (expRep.severityLevel != null) {
                    retVal = getExceptionSeverityLevel(expRep.severityLevel);
                }
            }
            // Find an error yet?
            if (retVal != ExceptionSeverityLevel.ERROR) {
                // No error yet, look at the expense entries themselves.
                // Iterate over the entries.
                if (expRep.getExpenseEntries() != null) {
                    Iterator<ExpenseReportEntry> entIter = expRep.getExpenseEntries().iterator();
                    while (entIter.hasNext() && (retVal != ExceptionSeverityLevel.ERROR)) {
                        ExpenseReportEntry expRepEnt = entIter.next();
                        switch (getExpenseEntryExceptionSeverityLevel(expRepEnt)) {
                        case NONE:
                            // No-op.
                            break;
                        case WARN:
                            retVal = ExceptionSeverityLevel.WARN;
                            break;
                        case ERROR:
                            retVal = ExceptionSeverityLevel.ERROR;
                            break;
                        }
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Will examine an instance of <code>ExpenseReportEntry</code> and determine its severity level by examining the
     * <code>hasExceptions</code>, <code>severityLevel</code> and any exceptions contained within <code>expRepEnt</code> or it's
     * itemized entries.
     * 
     * @param expRepEnt
     *            the expense report entry.
     * @return an instance of <code>ExceptionSeverityLevel</code>.
     */
    public static ExceptionSeverityLevel getExpenseEntryExceptionSeverityLevel(ExpenseReportEntry expRepEnt) {
        ExceptionSeverityLevel retVal = ExceptionSeverityLevel.NONE;
        if (expRepEnt.hasExceptions()) {
            if (expRepEnt instanceof ExpenseReportEntryDetail) {
                // Examine entry-level exceptions.
                ExpenseReportEntryDetail expRepEntDet = (ExpenseReportEntryDetail) expRepEnt;
                ArrayList<ExpenseReportException> expRepEntExcs = expRepEntDet.getExceptions();
                if (expRepEntExcs != null) {
                    switch (getExceptionListExceptionSeverityLevel(expRepEntExcs.iterator())) {
                    case NONE:
                        // No-op.
                        break;
                    case WARN:
                        retVal = ExceptionSeverityLevel.WARN;
                        break;
                    case ERROR:
                        retVal = ExceptionSeverityLevel.ERROR;
                        break;
                    }
                }
                // If no entry-level exceptions of level error, then check itemizations.
                if (retVal != ExceptionSeverityLevel.ERROR) {
                    if (expRepEntDet.getItemizations() != null) {
                        Iterator<ExpenseReportEntry> itemizations = expRepEntDet.getItemizations().iterator();
                        while (itemizations.hasNext() && (retVal != ExceptionSeverityLevel.ERROR)) {
                            ExpenseReportEntry expRepEntItem = itemizations.next();
                            switch (getExpenseEntryExceptionSeverityLevel(expRepEntItem)) {
                            case NONE:
                                // No-op.
                                break;
                            case WARN:
                                retVal = ExceptionSeverityLevel.WARN;
                                break;
                            case ERROR:
                                retVal = ExceptionSeverityLevel.ERROR;
                                break;
                            }
                        }
                    }
                }
            } else {
                // Summary expense report entry objects do not have exceptions, so rely
                // upon 'severityLevel' property.
                if (expRepEnt.severityLevel != null) {
                    retVal = getExceptionSeverityLevel(expRepEnt.severityLevel);
                }
            }
        }
        return retVal;
    }

    /**
     * Will generate a new unique default report name consisting of 'Mobile Expense Report <Date>' + [#N].
     * 
     * @param activity
     *            the activity
     * 
     * @return the unique default report name.
     */
    public static String getUniqueDefaultNewReportName(Activity activity) {
        String reportName;
        StringBuilder strBldr = new StringBuilder(activity.getText(R.string.auto_report_name)).append(" ");
        strBldr.append(FormatUtil.REPORT_NAME_DATE_LOCAL.format(new Date()));
        reportName = strBldr.toString();
        ConcurCore concurMobile = (ConcurCore) activity.getApplication();
        IExpenseReportCache actRepCache = concurMobile.getExpenseActiveCache();
        List<ExpenseReport> actReports = actRepCache.getReportList();
        if (actReports != null && actReports.size() > 0) {
            boolean reportNameUnique;
            String reportNameCompare = reportName;
            int reportNameTry = 0;
            do {
                reportNameUnique = true;
                Iterator<ExpenseReport> expRepIter = actReports.iterator();
                while (expRepIter.hasNext()) {
                    ExpenseReport expRep = expRepIter.next();
                    if (reportNameCompare.equalsIgnoreCase(expRep.reportName)) {
                        reportNameUnique = false;
                        strBldr.setLength(0);
                        strBldr.append(reportName);
                        ++reportNameTry;
                        strBldr.append(" #");
                        strBldr.append(reportNameTry);
                        reportNameCompare = strBldr.toString();
                        break;
                    }
                }
            } while (!reportNameUnique);
            reportName = reportNameCompare;
        }
        return reportName;

    }

    /**
     * Will retrieve an <code>Intent</code> that can be used to launch an activity in package <code>packageName</code> with an
     * intent filter of action <code>Intent.ACTION_MAIN</code> and category <code>Intent.CATEGORY_LAUNCHER</code>.
     * 
     * @param packageName
     *            contains the package name.
     * @return returns an <code>Intent</code> that can be used to launch the activity.
     */
    public static Intent getPackageLaunchIntent(Context ctx, String packageName) {
        Intent intent = null;
        PackageManager pm = ctx.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);

        // Get a list of all the launchers
        List<ResolveInfo> launchables = pm.queryIntentActivities(main, 0);
        for (int i = 0; i < launchables.size(); i++) {
            ResolveInfo launchable = launchables.get(i);
            ActivityInfo act = launchable.activityInfo;
            if (act.applicationInfo.packageName.equals(packageName)) {
                intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(new ComponentName(act.applicationInfo.packageName, act.name));
            }

        }
        return intent;
    }

    /**
     * Determines whether an activity is available that can handle a mapping intent, i.e., one with a 'geo:' scheme.
     * 
     * @return whether an activity is available that can handle a mapping intent, i.e., one with a 'geo:' scheme.
     */
    public static boolean isMappingAvailable(Context context) {
        boolean mappingAvailable = false;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Seattle%2CWA+98188"));
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        mappingAvailable = isActivityAvailable(context, intent);
        return mappingAvailable;
    }

    /**
     * Will determine whether or not there are any activities available that can handle the given intent.
     * 
     * @param context
     *            the application context.
     * @param intent
     *            the intent.
     * @return returns <code>true</code> if there's an activity available; <code>false</code> otherwise.
     */
    public static boolean isActivityAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (list != null && list.size() > 0);
    }

    /**
     * Will determine whether this rail choice is "in policy" or not. This is determined by examining the enforcement level in any
     * violations and if at least one is found to be a "warning" or "error", then this method will return true.
     * 
     * @param violations
     *            contains the list of violations.
     * @return returns whether or not this rail choice is "in policy".
     */
    public static boolean isInPolicy(List<Violation> violations) {
        boolean retVal = true;
        if (violations != null) {
            for (Violation violation : violations) {
                RuleEnforcementLevel enfLevel = ViewUtil.getRuleEnforcementLevel(violation.enforcementLevel);
                if (enfLevel == RuleEnforcementLevel.WARNING || enfLevel == RuleEnforcementLevel.ERROR) {
                    retVal = false;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Will determine whether the last saved date is expired.
     * 
     * @param ctx
     *            the context object to obtain a shared preferences object.
     * @param expiration
     *            the expiration period in milliseconds.
     * @return returns <code>false</code> if the last date/location information is
     */
    public static boolean isLastDateExpired(Context ctx, long expiration) {
        boolean retVal = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.contains(Const.PREF_LAST_SAVED_DATE_TIME)) {
            long lastSavedDateMillis = prefs.getLong(Const.PREF_LAST_SAVED_DATE_TIME, 0L);
            if (lastSavedDateMillis != 0L) {
                long currentTimeMillis = System.currentTimeMillis();
                retVal = ((currentTimeMillis - lastSavedDateMillis) >= expiration);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".isLastDateExpired: last saved date time is 0!");
            }
        } else {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Will get the last stored transaction date if not expired.
     * 
     * @param ctx
     *            an application context.
     * @return an instance of <code>Calendar</code> containing the last transaction date if exists and non-expired; otherwise
     *         returns <code>null</code>.
     */
    public static Calendar getLastTransDate(Context ctx) {
        Calendar lastTransDate = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.contains(Const.PREF_LAST_SAVED_DATE)) {
            if (prefs.contains(Const.PREF_LAST_SAVED_DATE_TIME)) {
                if (!ViewUtil.isLastDateExpired(ctx, Const.LAST_TRANS_DATE_LOCATION_EXPIRATION_MILLISECONDS)) {
                    String lastTransDateStr = prefs.getString(Const.PREF_LAST_SAVED_DATE, "");
                    lastTransDate = Parse.parseXMLTimestamp(lastTransDateStr);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getLastLocationName: missing saved location time!");
            }
        }
        return lastTransDate;
    }

    /**
     * Will save the last selected location value.
     * 
     * @deprecated - use
     *             {@link com.concur.platform.ui.expense.util.ExpensePreferenceUtil#saveLocationSelection(Context ctx, String liKey, String liCode, String value)}
     *             instead.
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
    public static void saveLocationSelection(ConcurCore concurCore, Context ctx, String liKey, String liCode,
            String value) {
        String lastSavedLocation = String.format(Locale.US, "%s:%s:%s", liKey, liCode, value);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        long curTimeMillis = System.currentTimeMillis();
        // Save the last quick expense transaction and current time.
        concurCore.savePreference(prefs, Const.PREF_LAST_SAVED_LOCATION_SELECTION_TIME, curTimeMillis);
        concurCore.savePreference(ctx, Const.PREF_LAST_SAVED_LOCATION_SELECTION, lastSavedLocation);
    }

    /**
     * Saves the last used currency code into preferences.
     * 
     * @deprecated - use
     *             {@link com.concur.platform.ui.expense.util.ExpensePreferenceUtil#saveLastUsedCrnCode(Context ctx, String userCrnCode)}
     *             instead.
     * 
     * @param concurCore
     *            the application.
     * @param ctx
     *            the context.
     * @param userCrnCode
     *            contains the currency code to save.
     */
    public static void saveLastUsedCrnCode(ConcurCore concurCore, Context ctx, String userCrnCode) {
        concurCore.savePreference(ctx, Const.PREF_LAST_USED_CRN_CODE, userCrnCode);
    }

    /**
     * Gets the last saved location selection information if the selection isn't older than
     * <code>Const.LAST_TRANS_DATE_LOCATION_EXPIRATION_MILLISECONDS</code> old.
     * 
     * @param ctx
     *            contains a context.
     * @return an instance of <code>LocationSelection</code> if the data has been stored and not expired.
     */
    public static LocationSelection getLocationSelection(Context ctx) {
        LocationSelection retVal = null;
        if (!ViewUtil.isLastLocationSelectionExpired(ctx, Const.LAST_TRANS_DATE_LOCATION_EXPIRATION_MILLISECONDS)) {
            String lastLocSel = ViewUtil.getLastLocationSelection(ctx);
            if (lastLocSel != null && lastLocSel.length() > 0) {
                retVal = new LocationSelection();
                String[] lastSelLocVals = lastLocSel.split(":");
                if (lastSelLocVals[0] != null && !lastSelLocVals[0].equalsIgnoreCase("null")) {
                    retVal.liKey = lastSelLocVals[0];
                }
                if (lastSelLocVals[1] != null && !lastSelLocVals[1].equalsIgnoreCase("null")) {
                    retVal.liCode = lastSelLocVals[1];
                }
                if (lastSelLocVals[2] != null && !lastSelLocVals[2].equalsIgnoreCase("null")) {
                    retVal.value = lastSelLocVals[2];
                }
            }
        }
        return retVal;
    }

    /**
     * Gets the last used currency code that was saved into Preferences.
     * 
     * @param ctx
     *            the context.
     * @return the String of the 4217 currency code abbreviation stored in preferences
     */
    public static String getLastUsedCrnCode(Context ctx) {
        String userCrnCode = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.contains(Const.PREF_LAST_USED_CRN_CODE)) {
            userCrnCode = prefs.getString(Const.PREF_LAST_USED_CRN_CODE, "");
        }
        return userCrnCode;
    }

    /**
     * Will determine whether the last saved location is expired.
     * 
     * @param ctx
     *            the context object to obtain a shared preferences object.
     * @param expiration
     *            the expiration in milliseconds.
     * @return returns <code>false</code> if the last location is not expired; otherwise returns <code>true</code> if it has or if
     *         no last location exists.
     */
    public static boolean isLastLocationExpired(Context ctx, long expiration) {
        boolean retVal = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.contains(Const.PREF_LAST_SAVED_LOCATION_TIME)) {
            long lastSavedDateMillis = prefs.getLong(Const.PREF_LAST_SAVED_LOCATION_TIME, 0L);
            if (lastSavedDateMillis != 0L) {
                long currentTimeMillis = System.currentTimeMillis();
                retVal = ((currentTimeMillis - lastSavedDateMillis) >= expiration);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".isLastLocationExpired: last saved date time is 0!");
            }
        } else {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Will retrieve the last stored location name.
     * 
     * @return returns the last stored location name if exists and is not expired; otherwise, returns <code>null</code>.
     */
    public static String getLastLocationName(Context ctx) {
        String lastLocName = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.contains(Const.PREF_LAST_SAVED_LOCATION)) {
            if (prefs.contains(Const.PREF_LAST_SAVED_LOCATION_TIME)) {
                if (!ViewUtil.isLastLocationExpired(ctx, Const.LAST_TRANS_DATE_LOCATION_EXPIRATION_MILLISECONDS)) {
                    lastLocName = prefs.getString(Const.PREF_LAST_SAVED_LOCATION, "");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getLastLocationName: missing saved location time!");
            }
        }
        return lastLocName;
    }

    /**
     * Will determine whether the last saved location selection is expired.
     * 
     * @param ctx
     *            the context object to obtain a shared preferences object.
     * @param expiration
     *            the expiration in milliseconds.
     * @return returns <code>false</code> if the last location selection is not expired; otherwise returns <code>true</code> if it
     *         has or if no last location selection exists.
     */
    public static boolean isLastLocationSelectionExpired(Context ctx, long expiration) {
        boolean retVal = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.contains(Const.PREF_LAST_SAVED_LOCATION_SELECTION_TIME)) {
            long lastSavedDateMillis = prefs.getLong(Const.PREF_LAST_SAVED_LOCATION_SELECTION_TIME, 0L);
            if (lastSavedDateMillis != 0L) {
                long currentTimeMillis = System.currentTimeMillis();
                retVal = ((currentTimeMillis - lastSavedDateMillis) >= expiration);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".isLastLocationSelectionExpired: last saved date time is 0!");
            }
        } else {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Will get the last stored location selection if it exists and is not expired.
     * 
     * @param ctx
     *            an application context.
     * @return returns the last location selection if it exists and is non-expired; otherwise, returns <code>null</code>.
     */
    public static String getLastLocationSelection(Context ctx) {
        String locationSelection = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.contains(Const.PREF_LAST_SAVED_LOCATION_SELECTION)) {
            if (prefs.contains(Const.PREF_LAST_SAVED_LOCATION_SELECTION_TIME)) {
                if (!ViewUtil.isLastLocationSelectionExpired(ctx,
                        Const.LAST_TRANS_DATE_LOCATION_EXPIRATION_MILLISECONDS)) {
                    locationSelection = prefs.getString(Const.PREF_LAST_SAVED_LOCATION_SELECTION, "");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getLastLocationSelection: missing saved location selection time!");
            }
        }
        return locationSelection;
    }

    /**
     * Will determine whether the timestamp <code>dataUpdateTime</code> is older than <code>expiration</code> milliseconds.
     * 
     * @param updateTime
     *            the data update time stamp.
     * @param expiration
     *            the expiration period in milliseconds.
     * @return whether <code>dataUpdateTime</code> is older than <code>expiration</code> milliseconds.
     */
    public static boolean isDataExpired(Calendar updateTime, long expiration) {
        boolean dataExpired = false;
        if (updateTime != null) {
            long curTimeMillis = System.currentTimeMillis();
            try {
                long updateTimeMillis = updateTime.getTimeInMillis();
                dataExpired = ((curTimeMillis - updateTimeMillis) > expiration);
            } catch (IllegalArgumentException ilaArgExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".isDataExpired: unable to get millisecond time from 'updateTime'!",
                        ilaArgExc);
                // Err to the side of caution.
                dataExpired = true;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".isDataExpired: updateTime is null!");
            // Err to the side of caution.
            dataExpired = true;
        }
        return dataExpired;
    }

    /**
     * Will copy the contents from <code>srcFile</code> to </code>dstFile</code>.
     * 
     * @param srcFile
     *            the source file.
     * @param dstFile
     *            the destination file.
     * @param bufSize
     *            the size of data to move at one time.
     */
    public static boolean copyFile(File srcFile, File dstFile, int bufSize) {
        boolean retVal = false;
        BufferedInputStream bufIn = null;
        BufferedOutputStream bufOut = null;
        if (bufSize == 0) {
            bufSize = (64 * 1024);
        }
        try {
            bufIn = new BufferedInputStream(new FileInputStream(srcFile), bufSize);
            bufOut = new BufferedOutputStream(new FileOutputStream(dstFile), bufSize);
            byte[] data = new byte[bufSize];
            int bytesRead = 0;
            while ((bytesRead = bufIn.read(data, 0, data.length)) != -1) {
                bufOut.write(data, 0, bytesRead);
            }
            retVal = true;
        } catch (FileNotFoundException fnfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".copyFile: ", fnfExc);
        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".copyFile: I/O exception", ioExc);
        } finally {
            if (bufIn != null) {
                try {
                    bufIn.close();
                    bufIn = null;
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".copyFile: I/O exception closing input stream ", ioExc);
                }
            }
            if (bufOut != null) {
                try {
                    bufOut.close();
                    bufOut = null;
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".copyFile: I/O exceptino closing output stream.", ioExc);
                }
            }
        }
        return retVal;
    }

    /**
     * Will get an input stream for a Uri.
     * 
     * @param context
     *            contains a context.
     * @param uri
     *            contains a Uri.
     * @return returns an instance of <code>InputStream</code> upon success; <code>null</code> otherwise.
     */
    public static InputStream getInputStream(Context context, Uri uri) {
        InputStream inStream = null;
        if (uri != null) {
            try {
                ContentResolver cr = context.getContentResolver();
                inStream = cr.openInputStream(uri);
            } catch (Exception exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getInputStream: ", exc);
            }
        }
        return inStream;
    }

    /**
     * Will get an orientation angle of image captured by system camera
     * 
     * @param context
     *            contains a context.
     * @param uri
     *            contains a Uri.
     * @return returns an image orientation angel;
     */
    public static int getOrientaionAngle(Context context, Uri uri) {
        int retVal = 0;
        if (uri != null) {
            try {
                ContentResolver cr = context.getContentResolver();
                String[] projection = { MediaStore.Images.ImageColumns.ORIENTATION };
                Cursor cursor = null;
                try {
                    cursor = cr.query(uri, projection, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            int columnIndex = cursor.getColumnIndex(projection[0]);
                            if (columnIndex >= 0) {
                                String value = cursor.getString(columnIndex);
                                retVal = Math.abs(Integer.parseInt(value));
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".getOrientaionAngle: can't find columnIndex '"
                                        + MediaStore.Images.ImageColumns.ORIENTATION + "'.");
                            }
                        } else {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".getOrientaionAngle: moveToFirst is false.");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getOrientaionAngle: cursor is null.");
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } catch (Exception exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getOrientaionAngle: ", exc);
            }
        }
        return retVal;
    }

    /**
     * Will close an input stream.
     * 
     * @param inStream
     *            contains the input stream to be closed.
     */
    public static void closeInputStream(InputStream inStream) {
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".closeInputStream: I/O exception closing input stream", ioExc);
            }
        }
    }

    /**
     * Will close an output stream.
     * 
     * @param outStream
     *            contains the output stream to be closed.
     */
    public static void closeOutputStream(OutputStream outStream) {
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".closeOutputStream: I/O exception closing outout stream", ioExc);
            }
        }
    }

    /**
     * Will delete a file given an absolute path.
     * 
     * @param filePath
     *            contains the absolute file path of the file to be deleted.
     */
    public static void deleteFile(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                if (!file.delete()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".deleteFile: failed to delete file '" + filePath + "'.");
                }
            }
        }
    }

    /**
     * Gets the recommended sample size, compress format and quality settings for reducing the size of a receipt image.
     * 
     * @param bmptOpts
     *            contains the dimensions of the bitmap.
     * @return an instance of <code>SampleSizeCompressFormatQuality</code>.
     */
    public static SampleSizeCompressFormatQuality getRecommendedSampleSizeCompressFormatQuality(
            BitmapFactory.Options bmptOpts) {
        SampleSizeCompressFormatQuality retVal = null;
        if (bmptOpts != null) {
            retVal = new SampleSizeCompressFormatQuality();
            long numberPixels = bmptOpts.outWidth * bmptOpts.outHeight;
            Log.i(Const.LOG_TAG, CLS_TAG + ".getRecommended..: dim: " + bmptOpts.outWidth + "x" + bmptOpts.outHeight
                    + " " + Long.toString(numberPixels) + " pixels.");
            final long MEGA_BYTE = (1024 * 1024);
            if (numberPixels < (2 * MEGA_BYTE)) {
                Log.i(Const.LOG_TAG,
                        CLS_TAG + ".getRecommendedSampleSizeCompressFormatQuality: numberPixels("
                                + Long.toString(numberPixels) + ") is < 2 mebibytes using full-sized image.");
                retVal.sampleSize = 1;
                retVal.compressQuality = 100;
            } else if (numberPixels >= (2 * MEGA_BYTE) && numberPixels < (4 * MEGA_BYTE)) {
                Log.i(Const.LOG_TAG,
                        CLS_TAG + ".getRecommendedSampleSizeCompressFormatQuality: numberPixels("
                                + Long.toString(numberPixels)
                                + ") is >= 2 mebibytes and < 4 mebibytes using 1/2 image size.");
                retVal.sampleSize = 2;
            } else if (numberPixels >= (4 * MEGA_BYTE) && numberPixels < (8 * MEGA_BYTE)) {
                Log.i(Const.LOG_TAG,
                        CLS_TAG + ".getRecommendedSampleSizeCompressFormatQuality: numberPixels("
                                + Long.toString(numberPixels)
                                + ") is >= 4 mebibytes and < 8 mebibytes using 1/2 image size.");
                retVal.sampleSize = 2;
            } else if (numberPixels >= (8 * MEGA_BYTE) && numberPixels < (16 * MEGA_BYTE)) {
                Log.i(Const.LOG_TAG,
                        CLS_TAG + ".getRecommendedSampleSizeCompressformatQuality: numberPixels("
                                + Long.toString(numberPixels)
                                + ") is >= 8 mebibytes and < 16 mebibytes using 1/2 image size.");
                retVal.sampleSize = 2;
            } else {
                Log.i(Const.LOG_TAG,
                        CLS_TAG + ".getRecommendedSampleSizeCompressformatQuality: numberPixels("
                                + Long.toString(numberPixels) + ") is >= 16 using 1/4 image size.");

                retVal.sampleSize = 4;
            }
        }
        return retVal;
    }

    /**
     * Gets the recommended sample size, compress format and quality settings for reducing the size of a receipt image.
     * 
     * @param filePath
     *            the image file path.
     * @return an instance of <code>SampleSizeCompressFormatQuality</code>.
     */
    public static SampleSizeCompressFormatQuality getRecommendedSampleSizeCompressFormatQuality(String filePath) {
        SampleSizeCompressFormatQuality retVal = null;
        if (filePath != null) {
            BitmapFactory.Options bmptOpts = loadBitmapBounds(filePath);
            if (bmptOpts != null) {
                retVal = getRecommendedSampleSizeCompressFormatQuality(bmptOpts);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".getRecommendedSampleSizeCompressFormatQuality: unable to obtain bounds for bitmap file '"
                        + filePath + "'.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getRecommendedSampleSizeCompressFormatQuality: filePath is null!");
        }
        return retVal;
    }

    /**
     * Gets the recommended sample size, compress format and quality settings for reducing the size of a receipt image.
     * 
     * @param inStream
     *            the image input stream.
     * @return an instance of <code>SampleSizeCompressFormatQuality</code>.
     */
    public static SampleSizeCompressFormatQuality getRecommendedSampleSizeCompressFormatQuality(InputStream inStream) {
        SampleSizeCompressFormatQuality retVal = null;
        if (inStream != null) {
            BitmapFactory.Options bmptOpts = loadBitmapBounds(inStream);
            if (bmptOpts != null) {
                retVal = getRecommendedSampleSizeCompressFormatQuality(bmptOpts);
            } else {
                Log.e(Const.LOG_TAG,
                        CLS_TAG
                                + ".getRecommendedSampleSizeCompressFormatQuality: unable to obtain bounds for bitmap input stream.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getRecommendedSampleSizeCompressFormatQuality: inStream is null!");
        }
        return retVal;
    }

    /**
     * Will load the bitmap bounds for an image stored in a file and not the underlying image data.
     * 
     * @param filePath
     *            the path of the file.
     * @return the instance of <code>BitmapFactory.Options</code> that contains the width/height of the image.
     */
    public static BitmapFactory.Options loadBitmapBounds(String filePath) {
        BitmapFactory.Options retVal = null;

        if (filePath != null) {
            try {
                // Decode image size
                retVal = new BitmapFactory.Options();
                retVal.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(new File(filePath)), null, retVal);
            } catch (FileNotFoundException fnfExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".loadBitmapBounds: unable to locate image file '" + filePath + "'.");
                retVal = null;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".loadBitmapBounds: filePath is null!");
        }
        return retVal;
    }

    /**
     * Will load the bitmap bounds for an image based on reading an input stream.
     * 
     * @param in
     *            contains the input stream.
     * @return the instance of <code>BitmapFactory.Options</code> that contains the width/height of the image.
     */
    public static BitmapFactory.Options loadBitmapBounds(InputStream inStream) {
        BitmapFactory.Options retVal = null;

        if (inStream != null) {
            // Decode image size
            retVal = new BitmapFactory.Options();
            retVal.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inStream, null, retVal);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".loadBitmapBounds: in is null!");
        }
        return retVal;
    }

    /**
     * Will decode an image into a <code>Bitmap</code> object such that no size is greater than <code>imageSize</code> pixels.
     * This method does not load the entire image into memory, but rather first obtains the width/height of the image, then
     * chooses a suitable sampling size based on <code>imageSize</code>.
     * 
     * @param filePath
     *            the absolute path to the image.
     * @param imageSize
     *            the maximum size in pixels of width/height to be loaded.
     * @return an instance of <code>Bitmap</code> if <code>filePath</code> can be decoded; otherwise, <code>null</code> is
     *         returned.
     */
    public static Bitmap loadScaledBitmap(String filePath, int imageSize) {
        Bitmap bitmap = null;
        try {
            // Decode image size
            BitmapFactory.Options bmpOpts = new BitmapFactory.Options();
            bmpOpts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(new File(filePath)), null, bmpOpts);

            Log.d(Const.LOG_TAG, CLS_TAG + ".loadScaledBitmap: bounds -> " + bmpOpts.outWidth + "x" + bmpOpts.outHeight);

            int scale = 1;
            if (bmpOpts.outHeight > imageSize || bmpOpts.outWidth > imageSize) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(imageSize / (double) Math.max(bmpOpts.outHeight, bmpOpts.outWidth))
                                / Math.log(0.5)));
            }
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(filePath)), null, o2);
        } catch (FileNotFoundException fnfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".loadScaledBitmap: unable to locate image file '" + filePath + "'.");
        }
        return bitmap;
    }

    /**
     * Will create a bitmap object representing a sampled image stored in a file.
     * 
     * @param filePath
     *            the image file path.
     * @param sampleSize
     *            the sample size.
     * @return an instance of <code>Bitmap</code> containing the sampled image.
     */
    public static Bitmap loadSampledBitmap(String filePath, int sampleSize) {
        Bitmap bitmap = null;
        try {
            // Decode with sampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sampleSize;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(filePath)), null, o2);
        } catch (FileNotFoundException fnfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".loadScaledBitmap: unable to locate image file '" + filePath + "'.");
        }
        return bitmap;
    }

    /**
     * Will create a bitmap object representing a sampled image from an input stream.
     * 
     * @param inStream
     *            the image file input stream.
     * @param sampleSize
     *            the sample size.
     * @return an instance of <code>Bitmap</code> containing the sampled image.
     */
    public static Bitmap loadSampledBitmap(InputStream inStream, int sampleSize) {
        Bitmap bitmap = null;
        // Decode with sampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = sampleSize;
        bitmap = BitmapFactory.decodeStream(inStream, null, o2);
        return bitmap;
    }

    /**
     * Will create an absolute path to external media (SD card) that can be used to store an image file.
     * 
     * The file is named using a calendar instance and formatted using
     * <code>FormatUtil.LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND</code> format. The file ends with an extension of '.jpg'.
     * 
     * @return a string containing the abolute path to an image or <code>null</code> if external storage media is not present.
     */
    public static String createExternalMediaImageFilePath() {
        String retVal = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File receiptFile = new File(Environment.getExternalStorageDirectory(), createImageFileName());
            retVal = receiptFile.getAbsolutePath();
        }
        return retVal;
    }

    /**
     * Creates an image file name based on the current date.
     * 
     * @return returns an image file name based on the current date.
     */
    public static String createImageFileName() {
        String fileName = null;
        // Create a file name based on the current date.
        Calendar cal = Calendar.getInstance();
        fileName = Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND, cal) + ".jpg";

        return fileName;
    }

    /**
     * Creates a PDF file name based on the current date.
     * 
     * @return returns a PDF file name based on the current date.
     */
    public static String createPDFFileName() {
        String fileName = null;
        // Create a file name based on the current date.
        Calendar cal = Calendar.getInstance();
        fileName = Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND, cal) + ".pdf";
        return fileName;
    }

    /**
     * Will create an absolute path to external media (SD card) that can be used to store a pdf file.
     * 
     * The file is named using a calendar instance and formatted using
     * <code>FormatUtil.LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND</code> format. The file ends with an extension of '.pdf'.
     * 
     * @return a string containing the abolute path to an pdf or <code>null</code> if external storage media is not present.
     */
    public static String createExternalMediaPDFFilePath() {
        String retVal = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File pdfFile = new File(Environment.getExternalStorageDirectory(), createPDFFileName());
            retVal = pdfFile.getAbsolutePath();
        }
        return retVal;
    }

    /**
     * Determines whether the external media is mounted.
     * 
     * @return returns <code>true</code> if external media is mounted; <code>false</code> otherwise.
     */
    public static boolean isExternalMediaMounted() {
        return (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
    }

    /**
     * Will write all bytes from <code>inStream</code> to <code>outStream</code> transferring up to <code>bufSize</code> bytes at
     * a time.
     * 
     * @param inStream
     *            contains the input stream to read from.
     * @param outStream
     *            contains the output stream to write to.
     * @param bufSize
     *            contains the number of bytes read/written at one time.
     * @throws throws an IOException if an exception occurs while reading/writing.
     */
    public static void writeAllBytes(InputStream inStream, OutputStream outStream, int bufSize) throws IOException {
        if (inStream != null && outStream != null) {
            byte[] buffer = new byte[bufSize];
            int numBytesRead = 0;
            while ((numBytesRead = inStream.read(buffer, 0, bufSize)) != -1) {
                outStream.write(buffer, 0, numBytesRead);
            }
        }
    }

    /**
     * Will write out a bitmap to a file in a given compressed format.
     * 
     * @param bitmap
     *            the bitmap to be written.
     * @param format
     *            the image format.
     * @param quality
     *            the quality of the image.
     * @param filePath
     *            the file path in which to write the file.
     * @return returns <code>true</code> if <code>bitmap</code> was written to <code>filePath</code> in format <code>format</code>
     *         .
     */
    public static boolean writeBitmapToFile(Bitmap bitmap, Bitmap.CompressFormat format, int quality, String filePath) {
        boolean retVal = true;

        BufferedOutputStream bufOut = null;
        try {
            bufOut = new BufferedOutputStream(new FileOutputStream(filePath), (64 * 1024));
            if (!bitmap.compress(format, quality, bufOut)) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".writeBitmapToFile: unable to compress bitmap to JPEG.");
                retVal = false;
            }
        } catch (FileNotFoundException fnfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".writeBitmapToFile: unable to open file '" + filePath + "' for writing!",
                    fnfExc);
            retVal = false;
        } finally {
            if (bufOut != null) {
                try {
                    bufOut.close();
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".writeBitmapToFile: I/O exception closing output stream for file '"
                            + filePath + "'", ioExc);
                }
            }
        }
        return retVal;
    }

    /**
     * Gets the number of megabytes available on the SD card as a floating point value.
     * 
     * <b>NOTE</b><br>
     * This method assumes that a client calling this has previously checked that external media has been mounted.
     * 
     * @return the amount of space available on the SD card in megabytes.
     */
    public static float getMegabytesAvailableOnSDCard() {
        float retVal = 0.0F;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        retVal = bytesAvailable / (1024.f * 1024.f);
        return retVal;
    }

    /**
     * Will sample the image stored in a file into a bitmap, then write it out to a destination file with a certain format and
     * quality.
     * 
     * @param srcImageFile
     *            the source image file.
     * @param dstImageFile
     *            the destination image file.
     * @param sampleSize
     *            the source image sample size.
     * @param format
     *            the destination image file format.
     * @param quality
     *            the destination image quality.
     * @return will return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public static boolean copySampledBitmap(String srcImageFile, String dstImageFile, int sampleSize,
            Bitmap.CompressFormat format, int quality) {

        boolean retVal = true;
        if (srcImageFile != null) {
            Bitmap sampledBitmap = ViewUtil.loadSampledBitmap(srcImageFile, sampleSize);
            if (sampledBitmap != null) {
                ExifInterface ei;
                try {
                    ei = new ExifInterface(srcImageFile);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
                    switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        sampledBitmap = rotateImage(sampledBitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        sampledBitmap = rotateImage(sampledBitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        sampledBitmap = rotateImage(sampledBitmap, 270);
                        break;
                    }
                    Log.d(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap.orientation: " + orientation);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (!writeBitmapToFile(sampledBitmap, format, quality, dstImageFile)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: unable to write sampled bitmap to '"
                            + dstImageFile + "'.");
                    retVal = false;
                }
                // re-cycle the bitmap.
                if (sampledBitmap != null) {
                    sampledBitmap.recycle();
                    sampledBitmap = null;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: unable to load sampled bitmap '" + srcImageFile
                        + "'.");
                retVal = false;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: srcImageFile is null!");
            retVal = false;
        }
        return retVal;
    }

    public static Bitmap rotateImage(Bitmap sampledBitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(sampledBitmap, 0, 0, sampledBitmap.getWidth(), sampledBitmap.getHeight(), matrix,
                true);
    }

    /**
     * Will sample the image from an input stream into a bitmap, then write it out to a destination file with a certain format and
     * quality.
     * 
     * @param inStream
     *            the source image input stream.
     * @param dstImageFile
     *            the destination image file.
     * @param sampleSize
     *            the source image sample size.
     * @param format
     *            the destination image file format.
     * @param quality
     *            the destination image quality.
     * @return will return <code>true</code> upon success; <code>false</code> otherwise.
     */
    // public static boolean copySampledBitmap(InputStream inStream, String dstImageFile, int sampleSize,
    // Bitmap.CompressFormat format, int quality) {
    // //call helper method with angle 0..
    // return copySampledBitmap(inStream, dstImageFile, sampleSize, format, quality, 0);
    // }

    public static boolean copySampledBitmap(InputStream inStream, String dstImageFile, int sampleSize,
            Bitmap.CompressFormat format, int quality, int orientation) {
        boolean retVal = true;
        if (inStream != null) {
            Bitmap sampledBitmap = ViewUtil.loadSampledBitmap(inStream, sampleSize);
            if (sampledBitmap != null) {
                if (orientation > 0) {
                    switch (orientation) {
                    case 90:
                        sampledBitmap = rotateImage(sampledBitmap, 90);
                        break;
                    case 180:
                        sampledBitmap = rotateImage(sampledBitmap, 180);
                        break;
                    case 270:
                        sampledBitmap = rotateImage(sampledBitmap, 270);
                        break;
                    }
                }
                if (sampledBitmap != null) {
                    if (!writeBitmapToFile(sampledBitmap, format, quality, dstImageFile)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: unable to write sampled bitmap to '"
                                + dstImageFile + "'.");
                        retVal = false;
                    }
                    // re-cycle the bitmap.
                    if (sampledBitmap != null) {
                        sampledBitmap.recycle();
                        sampledBitmap = null;
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: unable to load sampled bitmap.");
                retVal = false;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: inStream is null!");
            retVal = false;
        }
        return retVal;
    }

    /**
     * Will retrieving the absolute path of a selected media store image file based on examining intent data.
     * 
     * @param activity
     *            the activity context.
     * @param data
     *            the intent data.
     * @return the absolute file path of the choosen image; otherwise <code>null</code>.
     */
    public static String getSelectedImageFilePath(Activity activity, Intent data) {
        String retVal = null;

        Uri selectedImage = data.getData();
        retVal = getSelectedImageFilePath(activity, selectedImage);
        return retVal;
    }

    /**
     * Will retrieving the absolute path of a selected media store image file based on examining the Uri.
     * 
     * @param context
     *            the activity context.
     * @param uri
     *            contains an Uri pointing to an image with the media store.
     * @return the absolute file path of the choosen image; otherwise <code>null</code>.
     */
    public static String getSelectedImageFilePath(Context context, Uri uri) {
        String retVal = null;
        if (uri != null) {
            String[] filePathColumn = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    retVal = cursor.getString(columnIndex);
                    cursor.close();
                    cursor = null;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
        }
        return retVal;
    }

    /**
     * Will read the magic number from <code>file</code> and determine the document type. Supported values are defined in
     * <code>ViewUtil.DocumentType</code>.
     * 
     * @param file
     *            the file to examine.
     * 
     * @return an element of <code>DocumentType</code> representing the type of document; if unknown, then the value of
     *         <code>UNKNOWN</code> is returned.
     */
    public static DocumentType getDocumentType(File file) {

        DocumentType docType = DocumentType.UNKNOWN;

        byte[] magicNumber = new byte[4];
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            if ((fin.read(magicNumber, 0, magicNumber.length)) == 4) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Integer.toHexString((0x000000FF & magicNumber[0])));
                strBldr.append(',');
                strBldr.append(Integer.toHexString((0x000000FF & magicNumber[1])));
                strBldr.append(',');
                strBldr.append(Integer.toHexString((0x000000FF & magicNumber[2])));
                strBldr.append(',');
                strBldr.append(Integer.toHexString((0x000000FF & magicNumber[3])));
                Log.d(Const.LOG_TAG, CLS_TAG + ".getImageType: 1st 4 bytes: " + strBldr.toString());
                if ((0x000000FF & magicNumber[0]) == 0x89 && (0x000000FF & magicNumber[1]) == 0x50
                        && (0x000000FF & magicNumber[2]) == 0x4e && (0x000000FF & magicNumber[3]) == 0x47) {
                    docType = DocumentType.PNG;
                } else if ((0x000000FF & magicNumber[0]) == 0xff && (0x000000FF & magicNumber[1]) == 0xd8
                        && (0x000000FF & magicNumber[2]) == 0xff && (0x000000FF & magicNumber[3]) == 0xe0) {
                    // JFIF
                    docType = DocumentType.JPG;
                } else if ((0x000000FF & magicNumber[0]) == 0xff && (0x000000FF & magicNumber[1]) == 0xd8
                        && (0x000000FF & magicNumber[2]) == 0xff && (0x000000FF & magicNumber[3]) == 0xe1) {
                    // EXIF
                    docType = DocumentType.JPG;
                } else if ((0x000000FF & magicNumber[0]) == 0x25 && (0x000000FF & magicNumber[1]) == 0x50
                        && (0x000000FF & magicNumber[2]) == 0x44 && (0x000000FF & magicNumber[3]) == 0x46) {
                    // PDF
                    docType = DocumentType.PDF;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getImageType: corrupted image file '" + file.getAbsolutePath() + "'.");
            }
        } catch (FileNotFoundException fnfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getImageType: image file '" + file.getAbsolutePath() + "' not found.",
                    fnfExc);
        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getImageType: I/O error reading from '" + file.getAbsolutePath() + "'.",
                    ioExc);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                    fin = null;
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getImageType: I/O exception closing '" + file.getAbsolutePath()
                            + "'.", ioExc);
                }
            }
        }
        return docType;
    }

    /**
     * Sets the visibility for a child view by resource id.
     * 
     * @param parent
     *            the parent containing the child view.
     * @param childId
     *            the resource id of the child view.
     * @param visibility
     *            the visiblity.
     */
    public static void setVisibility(View parent, int childId, int visibility) {
        if (parent != null) {
            View v = parent.findViewById(childId);
            if (v != null) {
                v.setVisibility(visibility);
            }
        }
    }

    /**
     * Will set the text to be displayed in a <code>TextView</code> component within a <code>View</code>.
     * 
     * @param activity
     *            the activity containing the view.
     * @param field_view_res_id
     *            the resource id of the view containing text view.
     * @param field_name_res_id
     *            the resource id of the text view.
     * @param text
     *            the text view string value.
     * @param singleLine
     *            whether the text displayed should be constrained to a single line.
     */
    public static void setTextViewText(Activity activity, int field_view_res_id, int field_name_res_id, String text,
            boolean singleLine) {

        setTextViewText(activity.getWindow().getDecorView(), field_view_res_id, field_name_res_id, text, singleLine);
    }

    /**
     * Will set the text to be displayed in a <code>TextView</code> component within a <code>View</code>.
     * 
     * @param view
     *            the parent view containing the TextView
     * @param field_view_res_id
     *            the resource id of the view containing text view.
     * @param field_name_res_id
     *            the resource id of the text view.
     * @param text
     *            the text view string value.
     * @param singleLine
     *            whether the text displayed should be constrained to a single line.
     */
    public static void setTextViewText(View parent, int field_view_res_id, int field_name_res_id, String text,
            boolean singleLine) {

        // Grab a handle to the containing view.
        View view = parent.findViewById(field_view_res_id);
        if (view != null) {
            // Grab a handle to the text view.
            TextView txtView = (TextView) view.findViewById(field_name_res_id);
            if (txtView != null) {
                if (text != null) {
                    txtView.setText(text);
                    txtView.setSingleLine(singleLine);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: null text!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: can't find text view by resource id.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: can't find view by resource id.");
        }
    }

    /**
     * Will set view to be enabled.
     * 
     * @param activity
     *            the activity containing the view.
     * @param field_view_res_id
     *            the resource id of the view containing text view.
     * @param field_name_res_id
     *            the resource id of the text view.
     * @param enabled
     *            whether the field is enabled.
     */
    public static void setViewEnabled(Activity activity, int field_view_res_id, int field_name_res_id, boolean enabled) {
        // Grab a handle to the containing view.
        View view = activity.findViewById(field_view_res_id);
        if (view != null) {
            // Grab a handle to the text view.
            View subView = view.findViewById(field_name_res_id);
            if (subView != null) {
                subView.setEnabled(enabled);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: can't find text view by resource id.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: can't find view by resource id.");
        }
    }

    /**
     * Will get the text displayed in <code>TextView</code> component within a <code>View</code>.
     * 
     * @param activity
     *            the activity containing the view.
     * @param field_view_res_id
     *            the resource id of the view containing text view.
     * @param field_name_res_id
     *            the resource id of the text view.
     */
    public static String getTextViewText(Activity activity, int field_view_res_id, int field_name_res_id) {
        String txt = null;
        // Grab a handle to the containing view.
        View view = activity.findViewById(field_view_res_id);
        if (view != null) {
            // Grab a handle to the text view.
            TextView txtView = (TextView) view.findViewById(field_name_res_id);
            if (txtView != null) {
                txt = txtView.getText().toString();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getTextViewText: can't find text view by resource id.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTextViewText: can't find view by resource id.");
        }
        return txt;
    }

    /**
     * Will locate a sub-view contained within another view.
     * 
     * @param activity
     *            the activity containing the view.
     * @param view_res_id
     *            the parent view.
     * @param subview_res_id
     *            the sub-view or child view.
     * @return the sub-view upon success; otherwise <code>null</code> is returned.
     */
    public static View findSubView(Activity activity, int view_res_id, int subview_res_id) {
        View subView = null;
        View view = activity.findViewById(view_res_id);
        if (view != null) {
            subView = view.findViewById(subview_res_id);
            if (subView == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".findView: can't find sub-view resource id.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".findView: can't find view by resource id.");
        }
        return subView;
    }

    /**
     * Helper to quickly set the text on a text view and turn it into a clickable link
     * 
     * @param viewId
     *            The ID of a TextView
     * @param text
     *            The text to set into the view
     * @param linkifyMask
     *            An integer mask indicating the type of text to link. See {@link Linkify}.
     */
    public static TextView setText(View view, int viewId, String text, int linkifyMask) {

        TextView tv = (TextView) view.findViewById(viewId);

        if (tv != null && text != null) {
            Spannable linkText = new SpannableString(text);
            boolean linksFound = Linkify.addLinks(linkText, linkifyMask);
            tv.setText(linkText);
            if (linksFound) {
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                tv.setFocusable(false);
                tv.setFocusableInTouchMode(false);
            }
        }
        return tv;
    }

    /**
     * Will examine the available address information in <code>hotel</code> and return a string containing possible multiple
     * lines.
     * 
     * @param hotel
     *            the hotel containing address information.
     * @return a string containing one or more address lines.
     */
    public static String getHotelAddress(HotelChoice hotel) {
        String hotelAddress = null;
        StringBuilder strBldr = new StringBuilder();
        if (hotel.addr1 != null && hotel.addr1.length() > 0) {
            strBldr.append(hotel.addr1);
        }
        if (hotel.addr2 != null && hotel.addr2.length() > 0) {
            if (strBldr.length() > 0) {
                strBldr.append('\n');
            }
            strBldr.append(hotel.addr2);
        }
        boolean citySet = false;
        if (hotel.city != null && hotel.city.length() > 0) {
            if (strBldr.length() > 0) {
                strBldr.append('\n');
            }
            strBldr.append(hotel.city);
            citySet = true;
        }
        boolean stateSet = false;
        if (hotel.stateAbbrev != null && hotel.stateAbbrev.length() > 0) {
            if (citySet) {
                strBldr.append(',');
            } else if (strBldr.length() > 0) {
                strBldr.append('\n');
            }
            strBldr.append(hotel.stateAbbrev);
            stateSet = true;
        }
        boolean zipSet = false;
        if (hotel.zipCode != null && hotel.zipCode.length() > 0) {
            if (stateSet || citySet) {
                strBldr.append(' ');
            } else if (strBldr.length() > 0) {
                strBldr.append('\n');
            }
            strBldr.append(hotel.zipCode);
            zipSet = true;
        }
        if (hotel.countryCode != null && hotel.countryCode.length() > 0) {
            if (stateSet || citySet || zipSet) {
                strBldr.append(' ');
            } else if (strBldr.length() > 0) {
                strBldr.append('\n');
            }
            strBldr.append(hotel.countryCode);
        }
        if (strBldr.length() > 0) {
            hotelAddress = strBldr.toString();
        }
        return hotelAddress;
    }

    /**
     * Will return a string containing the "<city>, <state><space><country code>" for a hotel.
     * 
     * @param hotel
     *            contains the hotel choice.
     * @return returns a string containing the "<city>, <state><space><country code>" for a hotel.
     */
    public static String getHotelCityStateCountry(HotelChoice hotel) {
        String hotelAddress = null;
        StringBuilder strBldr = new StringBuilder();
        if (hotel.city != null && hotel.city.length() > 0) {
            strBldr.append(hotel.city);
        }
        if (hotel.stateAbbrev != null && hotel.stateAbbrev.length() > 0) {
            if (strBldr.length() > 0) {
                strBldr.append(", ");
            }
            strBldr.append(hotel.stateAbbrev);
        }
        if (hotel.countryCode != null && hotel.countryCode.length() > 0) {
            if (strBldr.length() > 0) {
                strBldr.append(' ');
            }
            strBldr.append(hotel.countryCode);
        }
        hotelAddress = strBldr.toString();
        return hotelAddress;
    }

    /**
     * Adds an XML element with its value to a string builder object.
     * 
     * If <code>elementValue</code> is <code>null</code>, then <code>elementName</code> will not be added.
     * 
     * <b>NOTE:</b> The <code>elementValue</code> parameter will be passed through the <code>FormatUtil.escapeForXML</code> method
     * call prior to placement into the generated XML.
     * 
     * @param strBldr
     *            the string builder.
     * @param elementName
     *            the element name.
     * @param elementValue
     *            the element value.
     */
    public static void addXmlElement(StringBuilder strBldr, String elementName, String elementValue) {
        if (elementValue != null) {
            strBldr.append('<');
            strBldr.append(elementName);
            strBldr.append('>');
            strBldr.append(FormatUtil.escapeForXML(elementValue));
            strBldr.append("</");
            strBldr.append(elementName);
            strBldr.append('>');
        }
    }

    /**
     * Adds an integer XML element with its value to a string builder object.
     * 
     * If <code>elementValue</code> is <code>null</code>, then <code>elementName</code> will not be added.
     * 
     * @param strBldr
     *            the string builder.
     * @param elementName
     *            the element name.
     * @param elementValue
     *            the integer element value.
     */
    public static void addXmlElement(StringBuilder strBldr, String elementName, Integer elementValue) {
        if (elementValue != null) {
            strBldr.append('<');
            strBldr.append(elementName);
            strBldr.append('>');
            strBldr.append(Integer.toString(elementValue));
            strBldr.append("</");
            strBldr.append(elementName);
            strBldr.append('>');
        }
    }

    /**
     * Adds a boolean XML element with its value to a string builder object providing either 'true' or 'false'.
     * 
     * If <code>elementValue</code> is <code>null</code>, then <code>elementName</code> will not be added.
     * 
     * @param strBldr
     *            the string builder.
     * @param elementName
     *            the element name.
     * @param elementValue
     *            the element value, if <code>true</code>, then value will be written as <code>true</code>; otherwise will be
     *            written as <code>false</code>.
     */
    public static void addXmlElementTF(StringBuilder strBldr, String elementName, Boolean elementValue) {
        if (elementValue != null) {
            strBldr.append('<');
            strBldr.append(elementName);
            strBldr.append('>');
            strBldr.append((elementValue ? "true" : "false"));
            strBldr.append("</");
            strBldr.append(elementName);
            strBldr.append('>');
        }
    }

    /**
     * Adds a boolean XML element with its value to a string builder object providing either 'Y' or 'N'
     * 
     * If <code>elementValue</code> is <code>null</code>, then <code>elementName</code> will not be added.
     * 
     * @param strBldr
     *            the string builder.
     * @param elementName
     *            the element name.
     * @param elementValue
     *            the element value, if <code>true</code>, then value will be written as <code>Y</code>; otherwise will be written
     *            as <code>N</code>.
     */
    public static void addXmlElementYN(StringBuilder strBldr, String elementName, Boolean elementValue) {
        if (elementValue != null) {
            strBldr.append('<');
            strBldr.append(elementName);
            strBldr.append('>');
            strBldr.append((elementValue ? "Y" : "N"));
            strBldr.append("</");
            strBldr.append(elementName);
            strBldr.append('>');
        }
    }

    /**
     * Adds an XML element with its value to a string builder object.
     * 
     * If <code>elementValue</code> is <code>null</code>, then <code>elementName</code> will not be added.
     * 
     * <b>NOTE:</b> The <code>elementValue</code> parameter will be passed through the <code>FormatUtil.escapeForXML</code> method
     * call prior to placement into the generated XML.
     * 
     * @param strBldr
     *            the string builder.
     * @param elementName
     *            the element name.
     * @param elementValue
     *            the element value.
     */
    public static void addXmlElement(StringBuilder strBldr, String elementName, Double doubleValue) {
        if (doubleValue != null) {
            String stringValue = doubleValue.toString();
            addXmlElement(strBldr, elementName, stringValue);
        }
    }

    /**
     * Extracts the report detail object which is embedded within the response.
     * 
     * @param responseXML
     *            the full response XML, i.e., includes ActionStatus, etc.
     * @return the portion of the XML response containing just the report detail object.
     */
    public static String extractReportDetailXMLString(String responseXML) {
        String retVal = null;
        if (responseXML != null) {
            int startRepIndex = responseXML.indexOf("<Report");
            if (startRepIndex == -1) {
                startRepIndex = responseXML.indexOf("<ReportDetail");
            }
            if (startRepIndex != -1) {
                int stopRepIndex = responseXML.indexOf("</Report>");
                int stopRepTagLen = 0;
                if (stopRepIndex == -1) {
                    stopRepIndex = responseXML.indexOf("</ReportDetail>");
                    if (stopRepIndex != -1) {
                        stopRepTagLen = "</ReportDetail>".length();
                    }
                } else {
                    stopRepTagLen = "</Report>".length();
                }
                if (stopRepIndex != -1) {
                    retVal = responseXML.substring(startRepIndex, stopRepIndex + stopRepTagLen);
                } else {
                    Log.e(Const.LOG_TAG, SaveReportReply.CLS_TAG
                            + ".extractReportDetailXMLString: unable to locate end of report tag!");
                }
            } else {
                Log.e(Const.LOG_TAG, SaveReportReply.CLS_TAG
                        + ".extractReportDetailXMLString: unable to locate start of Report tag!");
            }
        } else {
            Log.e(Const.LOG_TAG, SaveReportReply.CLS_TAG + ".extractReportDetailXMLString: responseXML is null!");
        }
        return retVal;
    }

    /**
     * Will retrieve a string value for the current row of <code>cursor</code> stored in the column <code>columnName</code>.
     * 
     * @param cursor
     *            the database cursor.
     * @param columnName
     *            the column name.
     * @return the string value stored in <code>columName</code> of the current row of <code>cursor</code>.
     */
    public static String getCursorStringValue(Cursor cursor, String columnName) {
        final String MTAG = CLS_TAG + ".getCursorStringValue: ";
        String retVal = null;
        try {
            Assert.assertNotNull(MTAG + "cursor is null!", cursor);
            Assert.assertNotNull(MTAG + "columnName is null!", cursor);
            int colInd = cursor.getColumnIndex(columnName);
            Assert.assertTrue(MTAG + "column '" + columnName + "' not found in cursor row!", (colInd != -1));
            if (!cursor.isNull(colInd)) {
                retVal = cursor.getString(colInd);
            }
        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, afe.getMessage(), afe);
        }
        return retVal;
    }

    /**
     * Determines whether a detail summary report (detailed header + summary entries) should be fetched versus a full detailed
     * report (detail header + detail entries).
     * 
     * @param report
     *            the expense report.
     * @return returns <code>true</code> if a summary detail report should be fetched; <code>false</code> otherwise.
     */
    public static boolean shouldFetchDetailSummaryReport(ExpenseReport report) {
        // boolean fetchDetailSummary = false;
        // fetchDetailSummary = (report.expenseEntries != null &&
        // report.expenseEntries.size() > Const.REPORT_DETAIL_SUMMARY_ENTRY_COUNT_THRESHOLD);
        // return fetchDetailSummary;
        // Per Prashanth's request, always fetch detail summary reports.
        return true;
    }

    /**
     * Gets an intent that can be used to launch the Taxi Magic application.
     * 
     * @param context
     *            contains an application context.
     * @return returns an instance of <code>Intent</code> that can be used to launch the Taxi Magic application. Otherwise,
     *         <code>null</code> is returned.
     */
    public static Intent getTaxiMagicIntent(Context context) {
        Intent tm = null;
        PackageManager pm = context.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> launchables = pm.queryIntentActivities(main, 0);

        for (int i = 0; i < launchables.size(); i++) {
            ResolveInfo launchable = launchables.get(i);
            ActivityInfo act = launchable.activityInfo;
            if (act.applicationInfo.packageName.equals("com.ridecharge.android.taximagic")) {
                tm = new Intent(Intent.ACTION_MAIN);
                tm.addCategory(Intent.CATEGORY_LAUNCHER);
                tm.setComponent(new ComponentName(act.applicationInfo.packageName, act.name));
            }
        }
        return tm;
    }

    /**
     * Gets the Violation with the enforcement level matched to the passed in MaxEnforcementLevel
     */
    public static Violation getMaxRuleEnforcementViolation(List<Violation> violations, Integer maxEnforcementLevel) {
        Violation maxEnforcementViolation = null;
        if (violations != null) {
            for (Violation violation : violations) {
                if (maxEnforcementLevel == violation.enforcementLevel) {
                    maxEnforcementViolation = violation;
                    break;
                }
            }
        }
        return maxEnforcementViolation;
    }

    /**
     * Gets the 'show but no booking' maxenforcement violation from the passed in maxenforcementlevel
     */
    public static Violation getShowButNoBookingViolation(List<Violation> violations, Integer maxEnforcementLevel) {
        Violation maxEnforcementViolation = getMaxRuleEnforcementViolation(violations, maxEnforcementLevel);
        if (maxEnforcementViolation != null
                && (getRuleEnforcementLevel(maxEnforcementViolation.enforcementLevel) == RuleEnforcementLevel.INACTIVE)) {
            return maxEnforcementViolation;
        }
        return null;
    }

    /**
     * This method will Sample the captured image into memory, then write out to file and it will rotate the image if required.
     * 
     * @param receiptImageDataLocalFilePath
     * @return
     */
    public static boolean compressAndRotateImage(String receiptImageDataLocalFilePath) {
        boolean retVal = true;
        // Sample the captured image into memory, then write out to file.
        ViewUtil.SampleSizeCompressFormatQuality recConf = ViewUtil
                .getRecommendedSampleSizeCompressFormatQuality(receiptImageDataLocalFilePath);
        if (recConf != null) {
            if (recConf.sampleSize > 1) {
                if (!ViewUtil.copySampledBitmap(receiptImageDataLocalFilePath, receiptImageDataLocalFilePath,
                        recConf.sampleSize, recConf.compressFormat, recConf.compressQuality)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".compressAndRotateImage: unable to copy sampled image from '"
                            + receiptImageDataLocalFilePath + "' to '" + receiptImageDataLocalFilePath + "'");
                    retVal = false;
                }
            } else {
                // No-op, just use the captured image directly, i.e., no need to
                // re-sample it.
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".compressAndRotateImage: unable to obtain recommended samplesize, etc.!");
            retVal = false;
        }
        return retVal;
    }

    public static String getUserCountryCode(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // get country code and conver it to upper case.
        String cntryCode = manager.getSimCountryIso();

        if (cntryCode == null || cntryCode.equals("")) {
            // get locale
            cntryCode = getResourcesConfigurationLocale(context).getCountry();
        }
        return cntryCode.toUpperCase();
    }

    public static Locale getResourcesConfigurationLocale(Context context) {

        Locale locale = context.getResources().getConfiguration().locale;
        String cntryCode = locale.getCountry();

        // MOB-17048 Rare case when the device doesn't properly set the country code.
        if (cntryCode == null || cntryCode.length() == 0) {
            // It seems some Samsung GS2 in Poland aren't setting the Locale
            // correctly. That is, only the language is set as both language_countrycode,
            // which is in correct ISO 3166 format. So try to parse the language
            // to get the country code.
            String lang = locale.getLanguage();
            if (lang != null && lang.contains("_")) {
                String[] split = lang.split("_"); // e.g. en_us
                if (split.length > 1) {
                    locale = new Locale(split[0], split[1]);
                }
            }
        }

        return locale;
    }

    /**
     * Gets whether the currently logged in user company name.
     * 
     * @param context
     *            an application context.
     * 
     */
    public static String getUserCompanyName(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(Const.PREF_USER_COMPANY_NAME, null);
    }

    /**
     * MOB-15911 - Show GDSName in travel search results - only for DEV & QA
     * 
     * @param context
     * @param textView
     * @param gdsName
     */
    public static void showGDSName(Context context, TextView textView, String gdsName) {
        Activity activity = (Activity) context;
        UserConfig uc = ((ConcurCore) activity.getApplication()).getUserConfig();
        if (uc.showGDSNameInSearchResults && gdsName != null) {
            StringBuilder sb = new StringBuilder();
            String name = sb.append("(").append(gdsName).append(")").toString();
            textView.setText(name);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }

    }

    /**
     * Will copy the image data selected within the gallery.
     * 
     * @param data
     *            the intent object containing the selection information.
     */
    public static String compressAndRotateSelectedImage(Context ctx, Intent data, String receiptImageDataLocalFilePath) {

        String retVal = receiptImageDataLocalFilePath;

        // First, obtain the stream of the selected gallery image.
        InputStream inputStream = ViewUtil.getInputStream(ctx, data.getData());
        int angle = ViewUtil.getOrientaionAngle(ctx, data.getData());
        if (inputStream != null) {
            // Obtain the recommended sampling size, etc.
            ViewUtil.SampleSizeCompressFormatQuality recConf = ViewUtil
                    .getRecommendedSampleSizeCompressFormatQuality(inputStream);
            ViewUtil.closeInputStream(inputStream);
            inputStream = null;
            if (recConf != null) {
                // Copy from the input stream to an external file.
                receiptImageDataLocalFilePath = ViewUtil.createExternalMediaImageFilePath();
                inputStream = new BufferedInputStream(ViewUtil.getInputStream(ctx, data.getData()), (8 * 1024));
                if (!ViewUtil.copySampledBitmap(inputStream, receiptImageDataLocalFilePath, recConf.sampleSize,
                        recConf.compressFormat, recConf.compressQuality, angle)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".copySelectedImage: unable to copy sampled image from '"
                            + inputStream + "' to '" + receiptImageDataLocalFilePath + "'");
                    receiptImageDataLocalFilePath = null;
                    retVal = receiptImageDataLocalFilePath;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".copySelectedImage: unable to obtain recommended samplesize, etc.!");
                receiptImageDataLocalFilePath = null;
                retVal = receiptImageDataLocalFilePath;
            }
        } else {
            retVal = receiptImageDataLocalFilePath;
        }
        return retVal;
    }

    /**
     * Will create DFP ads on UI
     * 
     * @param activity
     * @param unitID
     * @param field_view_res_id
     * @param IpmParams
     *            ipm extras
     */

//    public static PublisherAdView showDFPAds(Activity activity, IpmMsg ipmMsg, int field_view_res_id) {
//        /** The view to show the ad. */
//        PublisherAdView adView = new PublisherAdView(activity);
//
//        if (ipmMsg != null && ipmMsg.adUnitId != null) {
//
//            adView.setAdUnitId(ipmMsg.adUnitId);
//            // adView.setAdUnitId("/19197427/Dev/DevMobileTextAd");
//            adView.setAdListener(new LoggingAdListener());
//            LinearLayout layout = (LinearLayout) activity.findViewById(field_view_res_id);
//            // Add the adView to it.
//            if (layout != null) {
//                layout.addView(adView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
//                        LayoutParams.WRAP_CONTENT));
//                PublisherAdRequest pr = null;
//                if (ipmMsg.params != null) {
//
//                    IpmParams extras = ipmMsg.params;
//                    Bundle bundle = new Bundle();
//                    bundle.putString("CteProduct", extras.toString(extras.cteProduct));
//                    bundle.putString("Lang", extras.getLang());
//                    PackageInfo pinfo = null;
//                    try {
//                        pinfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
//                    } catch (NameNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                    if (pinfo != null) {
//                        String versionName = pinfo.versionName;
//                        bundle.putString("AppVersion", versionName);
//                    }
//
//                    pr = new PublisherAdRequest.Builder().addNetworkExtras(new AdMobExtras(bundle)).build();
//                } else {
//                    pr = new PublisherAdRequest.Builder().build();
//
//                }
//
//                adView.setAdSizes(AdSize.SMART_BANNER);
//                // adView.setAdSizes(new AdSize(50, 40), AdSize.BANNER);
//                // Initiate an request to load the AdView with an ad.
//                adView.loadAd(pr);
//
//            }
//        }
//        return adView;
//    }
}
