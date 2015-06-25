package com.concur.mobile.platform.config.provider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.authentication.ExpenseItLoginResult;
import com.concur.mobile.platform.authentication.LoginResult;
import com.concur.mobile.platform.authentication.Permissions;
import com.concur.mobile.platform.authentication.Permissions.PermissionName;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.authentication.SiteSetting;
import com.concur.mobile.platform.authentication.SiteSettingInfo;
import com.concur.mobile.platform.authentication.UserInfo;
import com.concur.mobile.platform.config.provider.Config.PermissionsColumns;
import com.concur.mobile.platform.config.system.ExpenseType;
import com.concur.mobile.platform.config.system.OfficeLocation;
import com.concur.mobile.platform.config.system.ReasonCode;
import com.concur.mobile.platform.config.system.SystemConfig;
import com.concur.mobile.platform.config.user.AffinityProgram;
import com.concur.mobile.platform.config.user.AttendeeColumnDefinition;
import com.concur.mobile.platform.config.user.AttendeeType;
import com.concur.mobile.platform.config.user.CarType;
import com.concur.mobile.platform.config.user.CreditCard;
import com.concur.mobile.platform.config.user.Currency;
import com.concur.mobile.platform.config.user.ExpenseConfirmation;
import com.concur.mobile.platform.config.user.Policy;
import com.concur.mobile.platform.config.user.TravelPointsConfig;
import com.concur.mobile.platform.config.user.UserConfig;
import com.concur.mobile.platform.config.user.YodleePaymentType;
import com.concur.mobile.platform.provider.PlatformContentProvider;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

/**
 * Contains a set of utility methods for updating the Config provider.
 */
public class ConfigUtil {

    private static final String CLS_TAG = "ConfigUtil";

    private static final Boolean DEBUG = Boolean.TRUE;

    private static final Boolean TRACK_INSERTION_TIME = Boolean.TRUE;

    /**
     * FIXME:
     * Dummy Session Id to identify ExpenseIt session id.
     * ExpenseIt Doesn't support session so we use this column to track expenseit authorization
     * If that changes, we need to add a column to differentiate between expenseIt and Concur
     * But the plan at this moment is for expenseit to have a single logon
     */
    private static final String EXPENSE_IT_SESSION_ID="AAAAAAAAA-1111-1111-1111-111111111111";

    /**
     * Will update the session expiration time for the session with id <code>sessionId</code>.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param sessionId
     *            contains a reference to the session id.
     * @param sessionExpirationTime
     *            contains the session expiration time.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     * @throws IllegalArgumentException
     *             if <code>sessionId</code> is null or empty.
     */
    public static boolean updateSessionExpirationTime(Context context, String sessionId, Long sessionExpirationTime) {
        boolean updated = true;

        if (TextUtils.isEmpty(sessionId)) {
            throw new IllegalArgumentException(CLS_TAG + ".updateSessionExpirationTime: sessionId is null or empty!");
        }

        // Update the session expiration time for the session id.
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(Config.SessionColumns.SESSION_EXPIRATION_TIME, sessionExpirationTime);
        String where = Config.SessionColumns.SESSION_ID + " = ?";
        String[] selectionArgs = { sessionId };
        int rowsUpdated = resolver.update(Config.SessionColumns.CONTENT_URI, values, where, selectionArgs);

        if (rowsUpdated == 1) {
            updated = true;
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".updateSessionExpirationTime: updated session expiration time!");
            }
        } else {
            updated = false;
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateSessionExpirationTime: failed to update session expiration time!");
        }
        return updated;
    }

    /**
     * Will retrieve the current session information stored in the Config provider.
     * 
     * @param context
     *            contains the application context.
     * @return returns an instance of <code>SessionInfo</code> containing the latest session information. If no session exists,
     *         then <code>null</code> will be returned.
     */
    public static SessionInfo getSessionInfo(Context context) {

        SessionInfoImpl info = null;

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] sessionColumns = { Config.SessionColumns.ACCESS_TOKEN_KEY,
                    Config.SessionColumns.AUTHENTICATION_TYPE, Config.SessionColumns.SESSION_ID,
                    Config.SessionColumns.SESSION_TIME_OUT, Config.SessionColumns.SESSION_EXPIRATION_TIME,
                    Config.SessionColumns.LOGIN_ID, Config.SessionColumns.SERVER_URL,
                    Config.SessionColumns.SIGN_IN_METHOD, Config.SessionColumns.SSO_URL, Config.SessionColumns.EMAIL,
                    Config.SessionColumns.USER_ID };

            // select non-expenseIt session
            String whereClause = Config.SessionColumns.SESSION_ID + " != ?";
            String[] whereArgs = { EXPENSE_IT_SESSION_ID };
            cursor = resolver.query(Config.SessionColumns.CONTENT_URI, sessionColumns, whereClause, whereArgs,
                    Config.SessionColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    info = new SessionInfoImpl();

                    // Set the access token.
                    info.accessToken = CursorUtil.getStringValue(cursor, Config.SessionColumns.ACCESS_TOKEN_KEY);

                    // Set the authentication type.
                    info.authenticationType = CursorUtil.getStringValue(cursor,
                            Config.SessionColumns.AUTHENTICATION_TYPE);

                    // Set the session id.
                    info.sessionId = CursorUtil.getStringValue(cursor, Config.SessionColumns.SESSION_ID);

                    // Set the session timeout.
                    info.sessionTimeout = CursorUtil.getIntValue(cursor, Config.SessionColumns.SESSION_TIME_OUT);

                    // Set the session expiration time.
                    info.sessionExpirationTime = CursorUtil.getLongValue(cursor,
                            Config.SessionColumns.SESSION_EXPIRATION_TIME);

                    // Set the login id.
                    info.loginId = CursorUtil.getStringValue(cursor, Config.SessionColumns.LOGIN_ID);

                    // Set the server url.
                    info.serverUrl = CursorUtil.getStringValue(cursor, Config.SessionColumns.SERVER_URL);

                    // Set the sign-in method.
                    info.signInMethod = CursorUtil.getStringValue(cursor, Config.SessionColumns.SIGN_IN_METHOD);

                    // Set the SSO url.
                    info.ssoUrl = CursorUtil.getStringValue(cursor, Config.SessionColumns.SSO_URL);

                    // Set the email.
                    info.email = CursorUtil.getStringValue(cursor, Config.SessionColumns.EMAIL);

                    // Set the user id.
                    info.userId = CursorUtil.getStringValue(cursor, Config.SessionColumns.USER_ID);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return info;
    }

    /**
     * Will update the current session information stored in the Config provider.
     * 
     * @param context
     *            contains the application context.
     * @param sessInfo
     *            contains the session information.
     * @return returns <code>true</code> upon success; otherwise, returns <code>false</code>
     * @throws IllegalArgumentException
     *             if <code>sessInfo</code> is null or <code>sessInfo.getSessionId</code> returns null or empty.
     */
    public static boolean updateSessionInfo(Context context, SessionInfo sessInfo) {
        boolean updated = true;

        if (sessInfo == null) {
            throw new IllegalArgumentException(CLS_TAG + ".updateSessionInfo: sessInfo is null!");
        }
        if (TextUtils.isEmpty(sessInfo.getSessionId())) {
            throw new IllegalArgumentException(CLS_TAG + ".updateSessionInfo: sessInfo.getSessionId is null or empty!");
        }

        // Update the session expiration time for the session id.
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();

        // Access token.
        ContentUtils.putValue(values, Config.SessionColumns.ACCESS_TOKEN_KEY, sessInfo.getAccessToken());

        // Authentication type.
        ContentUtils.putValue(values, Config.SessionColumns.AUTHENTICATION_TYPE, sessInfo.getAuthenticationType());

        // Session id.
        ContentUtils.putValue(values, Config.SessionColumns.SESSION_ID, sessInfo.getSessionId());

        // Session timeout.
        ContentUtils.putValue(values, Config.SessionColumns.SESSION_TIME_OUT, sessInfo.getSessionTimeout());

        // Session expiration time.
        ContentUtils.putValue(values, Config.SessionColumns.SESSION_EXPIRATION_TIME,
                sessInfo.getSessionExpirationTime());

        // Login id.
        ContentUtils.putValue(values, Config.SessionColumns.LOGIN_ID, sessInfo.getLoginId());

        // Server Url.
        ContentUtils.putValue(values, Config.SessionColumns.SERVER_URL, sessInfo.getServerUrl());

        // Sign-in method.
        ContentUtils.putValue(values, Config.SessionColumns.SIGN_IN_METHOD, sessInfo.getSignInMethod());

        // SSO Url.
        ContentUtils.putValue(values, Config.SessionColumns.SSO_URL, sessInfo.getSSOUrl());

        // Email.
        ContentUtils.putValue(values, Config.SessionColumns.EMAIL, sessInfo.getEmail());

        // User id.
        ContentUtils.putValue(values, Config.SessionColumns.USER_ID, sessInfo.getUserId());

        String where = Config.SessionColumns.SESSION_ID + " = ?";
        String[] selectionArgs = { sessInfo.getSessionId() };
        int rowsUpdated = resolver.update(Config.SessionColumns.CONTENT_URI, values, where, selectionArgs);

        if (rowsUpdated == 1) {
            updated = true;
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".updateSessionInfo: updated session information!");
            }
        } else {
            updated = false;
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateSessionInfo: failed to update session information!");
        }
        return updated;
    }

    /**
     * Will retrieve the current user information stored in the Config provider for a specific user id.
     * 
     * @param context
     *            contains the application context.
     * @param userId
     *            contains the user id.
     * @return returns an instance of <code>UserInfo</code> containing the latest user information. If no user exists, then
     *         <code>null</code> will be returned.
     * @throws IllegalArgumentException
     *             if <code>userId</code> is null or empty.
     */
    public static UserInfo getUserInfo(Context context, String userId) {

        UserInfoImpl info = null;

        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException(CLS_TAG + ".getUserInfo: userId is null or empty!");
        }

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] userColumns = { Config.UserColumns.ENTITY_TYPE, Config.UserColumns.EXPENSE_COUNTRY_CODE,
                    Config.UserColumns.HAS_REQUIRED_CUSTOM_FIELDS, Config.UserColumns.PIN_EXPIRATION_DATE,
                    Config.UserColumns.PRODUCT_OFFERING, Config.UserColumns.PROFILE_STATUS,
                    Config.UserColumns.ROLES_MOBILE, Config.UserColumns.CONTACT_COMPANY_NAME,
                    Config.UserColumns.CONTACT_EMAIL, Config.UserColumns.CONTACT_FIRST_NAME,
                    Config.UserColumns.CONTACT_LAST_NAME, Config.UserColumns.CONTACT_MIDDLE_INITIAL,
                    Config.UserColumns.USER_CURRENCY_CODE, Config.UserColumns.USER_ID };
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Config.UserColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Config.UserColumns.CONTENT_URI, userColumns, where, whereArgs,
                    Config.UserColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    info = new UserInfoImpl();

                    // Set the entity type.
                    info.entityType = CursorUtil.getStringValue(cursor, Config.UserColumns.ENTITY_TYPE);

                    // Set the expense country code.
                    info.expenseCountryCode = CursorUtil
                            .getStringValue(cursor, Config.UserColumns.EXPENSE_COUNTRY_CODE);

                    // Set has required custom fields.
                    info.hasRequiredCustomFields = CursorUtil.getBooleanValue(cursor,
                            Config.UserColumns.HAS_REQUIRED_CUSTOM_FIELDS);

                    // Set pin expiration date.
                    String pinExpirationDateStr = CursorUtil.getStringValue(cursor,
                            Config.UserColumns.PIN_EXPIRATION_DATE);
                    if (!TextUtils.isEmpty(pinExpirationDateStr)) {
                        info.pinExpirationDate = Parse.parseXMLTimestamp(pinExpirationDateStr);
                    }

                    // Set the product offering.
                    info.productOffering = CursorUtil.getStringValue(cursor, Config.UserColumns.PRODUCT_OFFERING);

                    // Set the profile status.
                    info.profileStatus = CursorUtil.getIntValue(cursor, Config.UserColumns.PROFILE_STATUS);

                    // Set the roles mobile.
                    info.rolesMobile = CursorUtil.getStringValue(cursor, Config.UserColumns.ROLES_MOBILE);

                    // Set the contact company name.
                    info.contactCompanyName = CursorUtil
                            .getStringValue(cursor, Config.UserColumns.CONTACT_COMPANY_NAME);

                    // Set the contact email.
                    info.contactEmail = CursorUtil.getStringValue(cursor, Config.UserColumns.CONTACT_EMAIL);

                    // Set the contact first name.
                    info.contactFirstName = CursorUtil.getStringValue(cursor, Config.UserColumns.CONTACT_FIRST_NAME);

                    // Set the contact last name.
                    info.contactLastName = CursorUtil.getStringValue(cursor, Config.UserColumns.CONTACT_LAST_NAME);

                    // Set the contact middle initial.
                    info.contactMiddleInitial = CursorUtil.getStringValue(cursor,
                            Config.UserColumns.CONTACT_MIDDLE_INITIAL);

                    // Set the user currency code.
                    info.userCurrencyCode = CursorUtil.getStringValue(cursor, Config.UserColumns.USER_CURRENCY_CODE);

                    // Set the user id.
                    info.userId = CursorUtil.getStringValue(cursor, Config.UserColumns.USER_ID);

                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return info;
    }

    /**
     * Will retrieve the current site setting information stored in the Config provider for a specific user id.
     * 
     * @param context
     *            contains the application context.
     * @param userId
     *            contains the user id.
     * @return returns a list of <code>SiteSettingInfo</code> containing the latest site setting information. If no user exists,
     *         then <code>null</code> will be returned.
     * @throws IllegalArgumentException
     *             if <code>userId</code> is null or empty.
     */
    public static List<SiteSettingInfo> getSiteSettingInfo(Context context, String userId) {

        List<SiteSettingInfo> infos = null;

        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException(CLS_TAG + ".getSiteSettingInfo: userId is null or empty!");
        }

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] siteSettingColumns = { Config.SiteSettingColumns.NAME, Config.SiteSettingColumns.TYPE,
                    Config.SiteSettingColumns.VALUE, Config.SiteSettingColumns.USER_ID };
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Config.SiteSettingColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Config.SiteSettingColumns.CONTENT_URI, siteSettingColumns, where, whereArgs,
                    Config.SiteSettingColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    infos = new ArrayList<SiteSettingInfo>(cursor.getCount());

                    do {

                        SiteSettingInfoImpl info = new SiteSettingInfoImpl();

                        // Set the name.
                        info.name = CursorUtil.getStringValue(cursor, Config.SiteSettingColumns.NAME);

                        // Set the type.
                        info.type = CursorUtil.getStringValue(cursor, Config.SiteSettingColumns.TYPE);

                        // Set the value.
                        info.value = CursorUtil.getStringValue(cursor, Config.SiteSettingColumns.VALUE);

                        // Set the user id.
                        info.userId = CursorUtil.getStringValue(cursor, Config.SiteSettingColumns.USER_ID);

                        infos.add(info);

                    } while (cursor.moveToNext());

                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return infos;
    }

    /**
     * Will retrieve the current permissions information stored in the Config provider for a specific user id.
     * 
     * @param context
     *            contains the application context.
     * @param userId
     *            contains the user id.
     * @return returns the latest permissions information. If no user exists, then <code>null</code> will be returned.
     * @throws IllegalArgumentException
     *             if <code>userId</code> is null or empty.
     */
    public static Permissions getPermissionsInfo(Context context, String userId) {
        final Permissions infos = new Permissions();

        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException(CLS_TAG + ".getPermissionsInfo: userId is null or empty!");
        }

        final ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            final String[] permissionsColumns = { Config.PermissionsColumns.NAME, Config.PermissionsColumns.VALUE };
            final StringBuilder strBldr = new StringBuilder();
            strBldr.append(Config.PermissionsColumns.USER_ID);
            strBldr.append(" = ?");
            final String where = strBldr.toString();
            final String[] whereArgs = { userId };

            cursor = resolver.query(Config.PermissionsColumns.CONTENT_URI, permissionsColumns, where, whereArgs,
                    Config.PermissionsColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    setPermission(infos, cursor);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return infos;
    }

    /**
     * Will retrieve the value of <code>Config.SystemConfig.HASH</code> column for the user id value of <code>userId</code>.
     * 
     * @param context
     *            contains an application context.
     * @param userId
     *            contains the user id.
     * @return Returns the server-generated hash code for the system configuration information; otherwise, <code>null</code> is
     *         returned.
     * @throws IllegalArgumentException
     *             if <code>userId</code> is null or empty.
     */
    public static String getSystemConfigHash(Context context, String userId) {
        String hash = null;

        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException(CLS_TAG + ".getSystemConfigHash: userId is null or empty!");
        }

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] systemConfigColumns = { Config.SystemConfigColumns.HASH };
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Config.SystemConfigColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };
            cursor = resolver.query(Config.SystemConfigColumns.CONTENT_URI, systemConfigColumns, where, whereArgs,
                    Config.SystemConfigColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    // Set the hash value.
                    hash = CursorUtil.getStringValue(cursor, Config.SystemConfigColumns.HASH);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return hash;
    }

    /**
     * Will retrieve the value of <code>Config.UserConfig.HASH</code> column for the user id value of <code>userId</code>.
     * 
     * @param context
     *            contains an application context.
     * @param userId
     *            contains the user id.
     * @return Returns the server-generated hash code for the user configuration information; otherwise, <code>null</code> is
     *         returned.
     * @throws IllegalArgumentException
     *             if <code>userId</code> is null or empty.
     */
    public static String getUserConfigHash(Context context, String userId) {
        String hash = null;

        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException(CLS_TAG + ".getUserConfigHash: userId is null or empty!");
        }

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] userConfigColumns = { Config.UserConfigColumns.HASH };
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Config.UserConfigColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };
            cursor = resolver.query(Config.UserConfigColumns.CONTENT_URI, userConfigColumns, where, whereArgs,
                    Config.UserConfigColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    // Set the hash value.
                    hash = CursorUtil.getStringValue(cursor, Config.UserConfigColumns.HASH);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return hash;
    }

    /**
     * Will remove the information stored in the Config content provider that is supplied upon login.
     * 
     * @param context
     *            contains a reference to the application context.
     */
    public static void removeLoginInfo(Context context) {

        ContentResolver resolver = context.getContentResolver();

        // Punt the session information.
        int rowsAffected = deleteSessionConfigInfo(resolver);

        // Punt the user information.
        rowsAffected = resolver.delete(Config.UserColumns.CONTENT_URI, null, null);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".removeLoginInfo: deleted " + Integer.toString(rowsAffected)
                    + " user rows.");
        }

        // Punt the site-setting information.
        rowsAffected = resolver.delete(Config.SiteSettingColumns.CONTENT_URI, null, null);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".removeLoginInfo: deleted " + Integer.toString(rowsAffected)
                    + " site setting rows.");
        }

        // Punt the permissions information.
        rowsAffected = resolver.delete(Config.PermissionsColumns.CONTENT_URI, null, null);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".removeLoginInfo: deleted " + Integer.toString(rowsAffected)
                    + " permissions rows.");
        }
    }

    /**
     * Will perform a remote wipe on the data in the config provider.
     * 
     * @param context
     *            contains the application context.
     */
    public static void remoteWipe(Context context) {

        ContentResolver resolver = context.getContentResolver();

        // Punt all session information.
        int rowsAffected = deleteSessionConfigInfo(resolver);

        // Punt the user information.
        rowsAffected = resolver.delete(Config.UserColumns.CONTENT_URI, null, null);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".remoteWipe: deleted " + Integer.toString(rowsAffected) + " user rows.");
        }

        // Punt the site-setting information.
        rowsAffected = resolver.delete(Config.SiteSettingColumns.CONTENT_URI, null, null);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".remoteWipe: deleted " + Integer.toString(rowsAffected)
                    + " site setting rows.");
        }

        // Punt the permissions information.
        rowsAffected = resolver.delete(Config.PermissionsColumns.CONTENT_URI, null, null);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".remoteWipe: deleted " + Integer.toString(rowsAffected)
                    + " permissions rows.");
        }
    }

    /**
     * Update the Config session table to include the new Token for ExpenseIt and other session info
     * @param context
     * @param expenseItLoginResult
     */
    public static void updateExpenseItLoginInfo(Context context, ExpenseItLoginResult expenseItLoginResult) {
        if (expenseItLoginResult == null) {
            throw new IllegalArgumentException(CLS_TAG + ".updateLoginInfo: loginResponse is null!");
        }

        ContentResolver resolver = context.getContentResolver();

        // Update session information.
        updateExpenseItSessionInfo(resolver, expenseItLoginResult, EXPENSE_IT_SESSION_ID);
    }

    /**
     * Will update the session, user and site-setting information in the config provider.
     * 
     * @param context
     *            contains the application context.
     * @param loginResponse
     *            contains the login response object.
     * @throws IllegalArgumentException
     *             if <code>loginResponse</code> is null.
     */
    public static void updateLoginInfo(Context context, LoginResult loginResponse) {

        if (loginResponse == null) {
            throw new IllegalArgumentException(CLS_TAG + ".updateLoginInfo: loginResponse is null!");
        }

        ContentResolver resolver = context.getContentResolver();

        // Update session information.
        updateSessionInfo(resolver, loginResponse, true);

        // Punt the user information.
        int rowsAffected = resolver.delete(Config.UserColumns.CONTENT_URI, null, null);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateLoginInfo: deleted " + Integer.toString(rowsAffected)
                    + " user rows.");
        }
        // Insert new user information.
        insertUserInfo(resolver, loginResponse);

        // Punt the site-setting information.
        rowsAffected = resolver.delete(Config.SiteSettingColumns.CONTENT_URI, null, null);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateLoginInfo: deleted " + Integer.toString(rowsAffected)
                    + " site setting rows.");
        }
        // Insert new site-setting information.
        insertSiteSettingInfo(resolver, loginResponse);

        // Punt the permissions information.
        rowsAffected = resolver.delete(Config.PermissionsColumns.CONTENT_URI, null, null);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateLoginInfo: deleted " + Integer.toString(rowsAffected)
                    + " permissions rows.");
        }

        // Insert new permissions information
        insertPermissionsInfo(resolver, loginResponse);
    }

    /**
     * Will insert site-setting info table with information from login response.
     * 
     * @param resolver
     *            contains the content resolver.
     * @param loginResponse
     *            contains the login response.
     */
    private static void insertSiteSettingInfo(ContentResolver resolver, LoginResult loginResponse) {

        ContentValues values = new ContentValues();
        if (loginResponse.siteSettings != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            for (SiteSetting siteSetting : loginResponse.siteSettings) {

                // Set name.
                ContentUtils.putValue(values, Config.SiteSettingColumns.NAME, siteSetting.name);

                // Set type.
                ContentUtils.putValue(values, Config.SiteSettingColumns.TYPE, siteSetting.type);

                // Set value.
                ContentUtils.putValue(values, Config.SiteSettingColumns.VALUE, siteSetting.value);

                // Set user id.
                ContentUtils.putValue(values, Config.SiteSettingColumns.USER_ID, loginResponse.userId);

                Uri siteSettingUri = resolver.insert(Config.SiteSettingColumns.CONTENT_URI, values);
                values.clear();

                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".insertSiteSettingInfo: new site setting uri '"
                            + ((siteSettingUri != null) ? siteSettingUri.toString() : "null"));
                }
            }

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertSiteSettingInfo: took " + (endTimeMillis - startTimeMillis)
                        + " ms to insert " + Integer.toString(loginResponse.siteSettings.size()) + " items.");
            }
        }
    }

    /**
     * Will insert permissions info table with information from login response.
     * 
     * @param resolver
     *            contains the content resolver.
     * @param loginResponse
     *            contains the login response.
     */
    private static void insertPermissionsInfo(ContentResolver resolver, LoginResult loginResponse) {
        final ContentValues values = new ContentValues();
        if (loginResponse.permissions != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            // Set IsRequestUser.
            ContentUtils.putValue(values, Config.PermissionsColumns.NAME,
                    Permissions.PermissionName.HAS_TRAVEL_REQUEST.name());
            ContentUtils.putValue(values, Config.PermissionsColumns.VALUE,
                    loginResponse.permissions.getAreasPermissions().hasTravelRequest);
            ContentUtils.putValue(values, Config.PermissionsColumns.USER_ID, loginResponse.userId);

            Uri permissionsUri = resolver.insert(Config.PermissionsColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertPermissionsInfo: new permissions uri '"
                        + ((permissionsUri != null) ? permissionsUri.toString() : "null") + "'");
            }

            // Set IsRequestUser.
            ContentUtils.putValue(values, Config.PermissionsColumns.NAME, Permissions.PermissionName.TR_USER.name());
            ContentUtils.putValue(values, Config.PermissionsColumns.VALUE,
                    loginResponse.permissions.getTravelRequestPermissions().isRequestUser);

            permissionsUri = resolver.insert(Config.PermissionsColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertPermissionsInfo: new permissions uri '"
                        + ((permissionsUri != null) ? permissionsUri.toString() : "null") + "'");
            }

            // Set IsRequestApprover.
            ContentUtils
                    .putValue(values, Config.PermissionsColumns.NAME, Permissions.PermissionName.TR_APPROVER.name());
            ContentUtils.putValue(values, Config.PermissionsColumns.VALUE,
                    loginResponse.permissions.getTravelRequestPermissions().isRequestApprover);

            permissionsUri = resolver.insert(Config.PermissionsColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertPermissionsInfo: new permissions uri '"
                        + ((permissionsUri != null) ? permissionsUri.toString() : "null") + "'");
            }

            values.clear();

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertPermissionsInfo: took " + (endTimeMillis - startTimeMillis)
                        + " ms to insert permissions items.");
            }
        }
    }

    /**
     * Will insert user info table with information from login response.
     * 
     * @param resolver
     *            contains the content resolver.
     * @param loginResponse
     *            contains the login response.
     */
    private static void insertUserInfo(ContentResolver resolver, LoginResult loginResponse) {

        ContentValues values = new ContentValues();

        // Entity type.
        ContentUtils.putValue(values, Config.UserColumns.ENTITY_TYPE, loginResponse.entityType);

        // Expense country code.
        ContentUtils.putValue(values, Config.UserColumns.EXPENSE_COUNTRY_CODE, loginResponse.expenseCountryCode);

        // Has required custom fields.
        ContentUtils.putValue(values, Config.UserColumns.HAS_REQUIRED_CUSTOM_FIELDS,
                loginResponse.hasRequiredCustomFields);

        // Pin expiration date.
        ContentUtils.putValue(values, Config.UserColumns.PIN_EXPIRATION_DATE,
                Format.safeFormatCalendar(Parse.XML_DF, loginResponse.pinExpirationDate));

        // Product offering.
        ContentUtils.putValue(values, Config.UserColumns.PRODUCT_OFFERING, loginResponse.productOffering);

        // Profile status.
        ContentUtils.putValue(values, Config.UserColumns.PROFILE_STATUS, loginResponse.profileStatus);

        // Roles mobile.
        ContentUtils.putValue(values, Config.UserColumns.ROLES_MOBILE, loginResponse.rolesMobile);

        // Contact company name.
        ContentUtils.putValue(values, Config.UserColumns.CONTACT_COMPANY_NAME,
                ((loginResponse.userContact != null) ? loginResponse.userContact.companyName : null));

        // Contact email.
        ContentUtils.putValue(values, Config.UserColumns.CONTACT_EMAIL,
                ((loginResponse.userContact != null) ? loginResponse.userContact.email : null));

        // Contact first name.
        ContentUtils.putValue(values, Config.UserColumns.CONTACT_FIRST_NAME,
                ((loginResponse.userContact != null) ? loginResponse.userContact.firstName : null));

        // Contact last name.
        ContentUtils.putValue(values, Config.UserColumns.CONTACT_LAST_NAME,
                ((loginResponse.userContact != null) ? loginResponse.userContact.lastName : null));

        // Contact middle initial.
        ContentUtils.putValue(values, Config.UserColumns.CONTACT_MIDDLE_INITIAL,
                ((loginResponse.userContact != null) ? loginResponse.userContact.middleInitial : null));

        // User currency code.
        ContentUtils.putValue(values, Config.UserColumns.USER_CURRENCY_CODE, loginResponse.userCurrencyCode);

        // User id.
        ContentUtils.putValue(values, Config.UserColumns.USER_ID, loginResponse.userId);

        Uri userUri = resolver.insert(Config.UserColumns.CONTENT_URI, values);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".insertUserInfo: new user uri '"
                    + ((userUri != null) ? userUri.toString() : "null"));
        }

    }

    /**
     * Gets expenseIt session information such as OAuth token
     * @param context
     * @return
     */
    public static SessionInfo getExpenseItSessionInfo(Context context) {
        SessionInfoImpl info = null;

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] sessionColumns = {
                Config.SessionColumns.ACCESS_TOKEN_KEY
            };

            // select non-expenseIt session
            String whereClause = Config.SessionColumns.SESSION_ID + " = ?";
            String[] whereArgs = {EXPENSE_IT_SESSION_ID};
            cursor = resolver.query(Config.SessionColumns.CONTENT_URI, sessionColumns, whereClause, whereArgs,
                Config.SessionColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    info = new SessionInfoImpl();

                    // Set the access token.
                    info.accessToken = CursorUtil.getStringValue(cursor, Config.SessionColumns.ACCESS_TOKEN_KEY);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return info;
    }

    /**
     * Adds Oauth token to ExpenseIt sessionID.
     * @param resolver
     * @param expenseItLoginResult
     * @param sessionId
     */
    public static void updateExpenseItSessionInfo(ContentResolver resolver, ExpenseItLoginResult expenseItLoginResult, String sessionId) {
        if (expenseItLoginResult == null) {
            throw new IllegalArgumentException(CLS_TAG + ".updateSessionInfo: expenseItLoginResult is null!");
        }

        String whereClause = Config.SessionColumns.SESSION_ID + " = ?";
        String[] whereArgs = { sessionId };

        // Punt all session information.
        int rowsAffected = resolver.delete(Config.SessionColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateExpenseItSessionInfo: deleted " + Integer.toString(rowsAffected)
                + " session rows.");
        }

        // Set up the content values object.
        ContentValues values = new ContentValues();

        // Access token key.
        ContentUtils.putValue(values, Config.SessionColumns.ACCESS_TOKEN_KEY, expenseItLoginResult.getToken());

        // Session id.
        ContentUtils.putValue(values, Config.SessionColumns.SESSION_ID, sessionId);

        Uri sessionUri = resolver.insert(Config.SessionColumns.CONTENT_URI, values);
        if (DEBUG) {
            Log.d(Const.LOG_TAG,
                CLS_TAG + ".updateExpenseItSessionInfo: new session uri '"
                    + ((sessionUri != null) ? sessionUri.toString() : "null"));
        }
    }


    /**
     * Deletes rows in session table
     * FIXME
     * At this moment we don't have single login to expenseIt services. ExpenseIt also doesn't support Autologin
     * We don't expire on oauth to expenseIt so therefore we keep track of the oauth.
     * Once single login to expenseIt is implemented, we need to revisit this piece to wipe all sessions.
     * @param resolver
     * @return
     */
    private static int deleteSessionConfigInfo(ContentResolver resolver) {

        String whereClause = Config.SessionColumns.SESSION_ID + " != ?";
        String[] whereArgs = { EXPENSE_IT_SESSION_ID };

        // Punt all concur session information.
        int rowsAffected = resolver.delete(Config.SessionColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateSessionInfo: deleted " + Integer.toString(rowsAffected)
                + " session rows.");
        }
        return rowsAffected;
    }

    /**
     * Will update session info table with information from login response.
     * 
     * @param resolver
     *            contains the content resolver.
     * @param loginResponse
     *            contains the login response.
     * @throws IllegalArgumentException
     *             if <code>loginResponse</code> is null.
     */
    public static void updateSessionInfo(ContentResolver resolver, LoginResult loginResponse, boolean setSessionId) {

        if (loginResponse == null) {
            throw new IllegalArgumentException(CLS_TAG + ".updateSessionInfo: loginResponse is null!");
        }

        // Punt all session information.
        int rowsAffected = deleteSessionConfigInfo(resolver);

        // Set up the content values object.
        ContentValues values = new ContentValues();

        // Access token key.
        ContentUtils.putValue(values, Config.SessionColumns.ACCESS_TOKEN_KEY,
                ((loginResponse.accessToken != null) ? loginResponse.accessToken.key : null));

        // Authentication type.
        ContentUtils.putValue(values, Config.SessionColumns.AUTHENTICATION_TYPE, loginResponse.authenticationType);

        // Session id.
        if (setSessionId) {
            ContentUtils.putValue(values, Config.SessionColumns.SESSION_ID,
                    ((loginResponse.session != null) ? loginResponse.session.id : null));
        } else {
            values.putNull(Config.SessionColumns.SESSION_ID);
        }

        // Session timeout.
        ContentUtils.putValue(values, Config.SessionColumns.SESSION_TIME_OUT,
                ((loginResponse.session != null) ? loginResponse.session.timeout : null));

        // Session expiration time.
        ContentUtils.putValue(values, Config.SessionColumns.SESSION_EXPIRATION_TIME,
                ((loginResponse.session != null) ? loginResponse.session.expirationTime : null));

        // Clear out login id.
        ContentUtils.putValue(values, Config.SessionColumns.LOGIN_ID, (String) null);

        // Clear out server url.
        ContentUtils.putValue(values, Config.SessionColumns.SERVER_URL, (String) null);

        // Clear out sign-in method.
        ContentUtils.putValue(values, Config.SessionColumns.SIGN_IN_METHOD, (String) null);

        // Clear out SSO url.
        ContentUtils.putValue(values, Config.SessionColumns.SSO_URL, (String) null);

        // Clear out email.
        ContentUtils.putValue(values, Config.SessionColumns.EMAIL, (String) null);

        // User id.
        ContentUtils.putValue(values, Config.SessionColumns.USER_ID, loginResponse.userId);

        Uri sessionUri = resolver.insert(Config.SessionColumns.CONTENT_URI, values);
        if (DEBUG) {
            Log.d(Const.LOG_TAG,
                    CLS_TAG + ".updateSessionInfo: new session uri '"
                            + ((sessionUri != null) ? sessionUri.toString() : "null"));
        }

    }

    /**
     * Will update system configuration information.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param systemConfig
     *            contains the system configuration object.
     * @param userId
     *            contains the user id.
     * @throws IllegalArgumentException
     *             if <code>userId</code> is null or empty.
     */
    public static void updateSystemConfigInfo(Context context, SystemConfig systemConfig, String userId) {

        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException(CLS_TAG + ".updateSystemConfigInfo: userId is null or empty!");
        }

        ContentResolver resolver = context.getContentResolver();

        StringBuilder strBldr = new StringBuilder();
        strBldr.append(Config.SystemConfigColumns.USER_ID);
        strBldr.append(" = ?");
        String whereClause = strBldr.toString();
        String[] whereArgs = { userId };

        // Punt all system configuration information.
        int rowsAffected = resolver.delete(Config.SystemConfigColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateSystemConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " system config rows.");
        }
        // Insert new system configuration information.
        insertSystemConfigInfo(resolver, systemConfig, userId);

        // Punt the user information.
        strBldr.setLength(0);
        strBldr.append(Config.ReasonCodeColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();

        rowsAffected = resolver.delete(Config.ReasonCodeColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateSystemConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " reason code rows.");
        }
        // Insert new reason code information.
        insertReasonCodeInfo(resolver, systemConfig, userId);

        // Punt the expense type information.
        strBldr.setLength(0);
        strBldr.append(Config.ExpenseTypeColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();
        rowsAffected = resolver.delete(Config.ExpenseTypeColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateSystemConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " expense type rows.");
        }
        // Insert new expense type information.
        insertExpenseTypeInfo(resolver, systemConfig, userId);

        // Punt the office location information.
        strBldr.setLength(0);
        strBldr.append(Config.ExpenseTypeColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();
        rowsAffected = resolver.delete(Config.OfficeLocationColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateSystemConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " office location rows.");
        }
        // Insert new expense type information.
        insertOfficeLocationInfo(resolver, systemConfig, userId);
    }

    /**
     * Will insert into the office location table information from <code>systemConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param systemConfig
     *            contains the system configuration.
     * @param userId
     *            contains the user id.
     */
    private static void insertOfficeLocationInfo(ContentResolver resolver, SystemConfig systemConfig, String userId) {

        if (systemConfig.officeLocations != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            ContentValues[] valuesInfo = new ContentValues[systemConfig.officeLocations.size()];
            int valInd = 0;

            for (OfficeLocation officeLocation : systemConfig.officeLocations) {

                valuesInfo[valInd] = new ContentValues();

                // Set the address.
                ContentUtils.putValue(valuesInfo[valInd], Config.OfficeLocationColumns.ADDRESS, officeLocation.address);

                // Set the city.
                ContentUtils.putValue(valuesInfo[valInd], Config.OfficeLocationColumns.CITY, officeLocation.city);

                // Set the country.
                ContentUtils.putValue(valuesInfo[valInd], Config.OfficeLocationColumns.COUNTRY, officeLocation.country);

                // Set the latitude.
                ContentUtils.putValue(valuesInfo[valInd], Config.OfficeLocationColumns.LAT, officeLocation.lat);

                // Set the longitude.
                ContentUtils.putValue(valuesInfo[valInd], Config.OfficeLocationColumns.LON, officeLocation.lon);

                // Set the state.
                ContentUtils.putValue(valuesInfo[valInd], Config.OfficeLocationColumns.STATE, officeLocation.state);

                // Set the user id.
                ContentUtils.putValue(valuesInfo[valInd], Config.OfficeLocationColumns.USER_ID, userId);

                ++valInd;

            }

            int numInserted = resolver.bulkInsert(Config.OfficeLocationColumns.CONTENT_URI, valuesInfo);

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertOfficeLocationInfo: took " + (endTimeMillis - startTimeMillis)
                        + " ms to insert " + Integer.toString(numInserted) + " items.");
            }

        }
    }

    /**
     * Will insert into the expense type table information from <code>systemConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param systemConfig
     *            contains the system configuration.
     * @param userId
     *            contains the user id.
     */
    private static void insertExpenseTypeInfo(ContentResolver resolver, SystemConfig systemConfig, String userId) {

        if (systemConfig.expenseTypes != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            ContentValues[] valuesInfo = new ContentValues[systemConfig.expenseTypes.size()];
            int valInd = 0;

            for (ExpenseType expenseType : systemConfig.expenseTypes) {

                valuesInfo[valInd] = new ContentValues();

                // Set expense code.
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.EXP_CODE, expenseType.expCode);

                // Set expense key.
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.EXP_KEY, expenseType.expKey);

                // Set expense name.
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.EXP_NAME, expenseType.expName);

                // Set form key.
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.FORM_KEY, expenseType.formKey);

                // Set "has post amt calc".
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.HAS_POST_AMT_CALC,
                        expenseType.hasPostAmtCalc);

                // Set "has tax form".
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.HAS_TAX_FORM,
                        expenseType.hasTaxForm);

                // Set "itemization unallow expense keys".
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.ITEMIZATION_UNALLOW_EXP_KEYS,
                        expenseType.itemizationUnallowExpKeys);

                // Set "itemization form key"
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.ITEMIZATION_FORM_KEY,
                        expenseType.itemizeFormKey);

                // Set "itemization style"
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.ITEMIZATION_STYLE,
                        expenseType.itemizeStyle);

                // Set "itemization type"
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.ITEMIZATION_TYPE,
                        expenseType.itemizeType);

                // Set "parent expense key"
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.PARENT_EXP_KEY,
                        expenseType.parentExpKey);

                // Set "parent expense name"
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.PARENT_EXP_NAME,
                        expenseType.parentExpName);

                // Set "supports attendees"
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.SUPPORTS_ATTENDEES,
                        expenseType.supportsAttendees);

                // Set "vendor list key".
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.VENDOR_LIST_KEY,
                        expenseType.vendorListKey);

                // Set "allow edit attendee amount".
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_AMOUNT,
                        expenseType.allowEditAtnAmt);

                // Set "allow edit attendee count".
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_COUNT,
                        expenseType.allowEditAtnCount);

                // Set "allow no shows".
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.ALLOW_NO_SHOWS,
                        expenseType.allowNoShows);

                // Set "display add attendee on form".
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.DISPLAY_ADD_ATTENDEE_ON_FORM,
                        expenseType.displayAddAtnOnForm);

                // Set "display attendee amounts".
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.DISPLAY_ATTENDEE_AMOUNTS,
                        expenseType.displayAtnAmounts);

                // Set "user as default attendee".
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.USER_AS_ATTENDEE_DEFAULT,
                        expenseType.userAsAtnDefault);

                // Set "unallowed attendee type keys".
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.UNALLOW_ATN_TYPE_KEYS,
                        expenseType.unallowAtnTypeKeys);

                // Set the user id.
                ContentUtils.putValue(valuesInfo[valInd], Config.ExpenseTypeColumns.USER_ID, userId);

                ++valInd;
            }

            int numInserted = resolver.bulkInsert(Config.ExpenseTypeColumns.CONTENT_URI, valuesInfo);

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertExpenseTypeInfo: took " + (endTimeMillis - startTimeMillis)
                        + " ms to insert " + Integer.toString(numInserted) + " items.");
            }

        }
    }

    /**
     * Will insert into the reason code table information from <code>systemConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param systemConfig
     *            contains the system configuration.
     * @param userId
     *            contains the user id.
     */
    private static void insertReasonCodeInfo(ContentResolver resolver, SystemConfig systemConfig, String userId) {

        List<ReasonCode> allReasons = new ArrayList<ReasonCode>();

        // Add air reasons.
        if (systemConfig.airReasons != null) {
            allReasons.addAll(systemConfig.airReasons);
        }
        // Add hotel reasons.
        if (systemConfig.hotelReasons != null) {
            allReasons.addAll(systemConfig.hotelReasons);
        }
        if (systemConfig.carReasons != null) {
            allReasons.addAll(systemConfig.carReasons);
        }

        long startTimeMillis = 0L;
        if (TRACK_INSERTION_TIME) {
            startTimeMillis = System.currentTimeMillis();
        }

        ContentValues[] valuesInfo = new ContentValues[allReasons.size()];
        int valInd = 0;

        // Insert all reasons.
        for (ReasonCode reasonCode : allReasons) {

            valuesInfo[valInd] = new ContentValues();

            // Set the type.
            ContentUtils.putValue(valuesInfo[valInd], Config.ReasonCodeColumns.TYPE, reasonCode.type);

            // Set the description.
            ContentUtils.putValue(valuesInfo[valInd], Config.ReasonCodeColumns.DESCRIPTION, reasonCode.description);

            // Set the id.
            ContentUtils.putValue(valuesInfo[valInd], Config.ReasonCodeColumns.ID, reasonCode.id);

            // Set the violation type.
            ContentUtils
                    .putValue(valuesInfo[valInd], Config.ReasonCodeColumns.VIOLATION_TYPE, reasonCode.violationType);

            // Set the user id.
            ContentUtils.putValue(valuesInfo[valInd], Config.ReasonCodeColumns.USER_ID, userId);

            ++valInd;

        }

        int numInserted = resolver.bulkInsert(Config.ReasonCodeColumns.CONTENT_URI, valuesInfo);

        if (TRACK_INSERTION_TIME) {
            long endTimeMillis = System.currentTimeMillis();
            Log.d(Const.LOG_TAG, CLS_TAG + ".insertReasonCodeInfo: took " + (endTimeMillis - startTimeMillis)
                    + " ms to insert " + Integer.toString(numInserted) + " items.");
        }

    }

    /**
     * Will insert into the system configuration table information from <code>systemConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param systemConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertSystemConfigInfo(ContentResolver resolver, SystemConfig systemConfig, String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        // Set hash value.
        ContentUtils.putValue(values, Config.SystemConfigColumns.HASH, systemConfig.hash);

        // Set refundable checkbox default
        ContentUtils.putValue(values, Config.SystemConfigColumns.REFUND_INFO_CHECKBOX_DEFAULT,
                systemConfig.refundableCheckboxDefault);

        // Set refundable message
        ContentUtils.putValue(values, Config.SystemConfigColumns.REFUND_INFO_MESSAGE, systemConfig.refundableMessage);

        // Set refundable show checkbox.
        ContentUtils.putValue(values, Config.SystemConfigColumns.REFUND_INFO_SHOW_CHECKBOX,
                systemConfig.refundableShowCheckbox);

        // Set rule violation explanation required.
        ContentUtils.putValue(values, Config.SystemConfigColumns.RULE_VIOLATION_EXPLANATION_REQUIRED,
                systemConfig.ruleViolationExplanationRequired);

        // Set the user id.
        ContentUtils.putValue(values, Config.SystemConfigColumns.USER_ID, userId);

        Uri sysConfigUri = resolver.insert(Config.SystemConfigColumns.CONTENT_URI, values);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".insertSystemConfigInfo: new system config uri '"
                    + ((sysConfigUri != null) ? sysConfigUri.toString() : "null"));
        }
    }

    /**
     * Will update the config content provider with user configuration information.
     * 
     * @param context
     *            contains an application context.
     * @param userConfig
     *            contains a user configuration object.
     * @param userId
     *            contains the user id for which the information will be updated.
     * @throws IllegalArgumentException
     *             if <code>userId</code> is null or empty.
     */
    public static void updateUserConfigInfo(Context context, UserConfig userConfig, String userId) {

        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException(CLS_TAG + ".updateUserConfigInfo: userId is null or empty!");
        }

        ContentResolver resolver = context.getContentResolver();

        // Update user config table.
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(Config.UserConfigColumns.USER_ID);
        strBldr.append(" = ?");
        String whereClause = strBldr.toString();
        String[] whereArgs = { userId };

        // Punt all system configuration information.
        int rowsAffected = resolver.delete(Config.UserConfigColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateUserConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " user config rows.");
        }
        // Insert new user configuration information.
        insertUserConfigInfo(resolver, userConfig, userId);

        // Update car type table.
        strBldr.setLength(0);
        strBldr.append(Config.CarTypeColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();

        // Punt all car type configuration information.
        rowsAffected = resolver.delete(Config.CarTypeColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateUserConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " car type rows.");
        }
        // Insert new car type information.
        insertCarTypeInfo(resolver, userConfig, userId);

        // Update attendee column definition table.
        strBldr.setLength(0);
        strBldr.append(Config.AttendeeColumnDefinitionColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();

        // Punt all attendee column definition configuration information.
        rowsAffected = resolver.delete(Config.AttendeeColumnDefinitionColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateUserConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " attendee column definition rows.");
        }
        // Insert new attendee column definition information.
        insertAttendeeColumnDefinitionInfo(resolver, userConfig, userId);

        // Update attendee type table.
        strBldr.setLength(0);
        strBldr.append(Config.AttendeeTypeColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();

        // Punt all attendee type configuration information.
        rowsAffected = resolver.delete(Config.AttendeeTypeColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateUserConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " attendee type rows.");
        }
        // Insert new attendee type information.
        insertAttendeeTypeInfo(resolver, userConfig, userId);

        // Update currency table.
        strBldr.setLength(0);
        strBldr.append(Config.CurrencyColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();

        // Punt all currency configuration information.
        rowsAffected = resolver.delete(Config.CurrencyColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateUserConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " currency rows.");
        }
        // Insert new currency information.
        insertCurrencyInfo(resolver, userConfig, userId);

        // Update expense confirmation table.
        strBldr.setLength(0);
        strBldr.append(Config.ExpenseConfirmationColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();

        // Punt all expense confirmation configuration information.
        rowsAffected = resolver.delete(Config.ExpenseConfirmationColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateUserConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " expense confirmation rows.");
        }
        // Insert new expense confirmation information.
        insertExpenseConfirmationInfo(resolver, userConfig, userId);

        // Update policy table.
        strBldr.setLength(0);
        strBldr.append(Config.PolicyColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();

        // Punt all expense confirmation configuration information.
        rowsAffected = resolver.delete(Config.PolicyColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateUserConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " policy rows.");
        }
        // Insert new policy information.
        insertPolicyInfo(resolver, userConfig, userId);

        // Update Yodlee payment type table.
        strBldr.setLength(0);
        strBldr.append(Config.YodleePaymentTypeColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();

        // Punt all Yodlee payment type configuration information.
        rowsAffected = resolver.delete(Config.YodleePaymentTypeColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateUserConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " Yodlee payment type rows.");
        }
        // Insert new Yodlee payment type information.
        insertYodleePaymentTypeInfo(resolver, userConfig, userId);

        // Update credit card table.
        strBldr.setLength(0);
        strBldr.append(Config.CreditCardColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();

        // Punt all credit card configuration information.
        rowsAffected = resolver.delete(Config.CreditCardColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateUserConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " credit card rows.");
        }
        // Insert new credit card information.
        insertCreditCardInfo(resolver, userConfig, userId);

        // Update affinity program table.
        strBldr.setLength(0);
        strBldr.append(Config.AffinityProgramColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();

        // Punt all affinity program configuration information.
        rowsAffected = resolver.delete(Config.AffinityProgramColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateUserConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " affinity program rows.");
        }
        // Insert new affinity program information.
        insertAffinityProgramInfo(resolver, userConfig, userId);

        // Update travel points config table.
        strBldr.setLength(0);
        strBldr.append(Config.TravelPointsConfigColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();

        // Punt all affinity program configuration information.
        rowsAffected = resolver.delete(Config.TravelPointsConfigColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateUserConfigInfo: deleted " + Integer.toString(rowsAffected)
                    + " travel points config rows.");
        }
        // Insert new affinity program information.
        insertTravelPointsConfigInfo(resolver, userConfig, userId);
    }

    /**
     * Will update client data table with information from login response.
     * 
     * @param context
     *            contains an application context.
     * @param loginResponse
     *            contains the login response.
     * @throws IllegalArgumentException
     *             if <code>loginResponse</code> is null.
     */
    public static void updateAnalyticsIdInClientData(Context context, LoginResult loginResult) {
        ClientData clientData = new ClientData(context);
        clientData.userId = loginResult.userId;
        clientData.key = LoginResult.TAG_ANALYTICS_ID;
        clientData.text = loginResult.analyticsId;
        clientData.blob = null;
        clientData.update();

    }

    /**
     * Will insert into the user configuration table information from <code>userConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param userConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertUserConfigInfo(ContentResolver resolver, UserConfig userConfig, String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        // Set hash value.
        ContentUtils.putValue(values, Config.UserConfigColumns.HASH, userConfig.hash);

        // Set allowed air classes of service
        ContentUtils.putValue(values, Config.UserConfigColumns.ALLOWED_AIR_CLASSES_OF_SERVICE,
                userConfig.allowedAirClassesOfService);

        // Set flags.
        ContentUtils.putValue(values, Config.UserConfigColumns.FLAGS, userConfig.flags);

        // Set showGDSNameInSearchResults.
        ContentUtils.putValue(values, Config.UserConfigColumns.SHOW_GDS_NAME_IN_SEARCH_RESULTS,
                userConfig.showGDSNameInSearchResults);

        // Set the user id.
        ContentUtils.putValue(values, Config.UserConfigColumns.USER_ID, userId);

        Uri userConfigUri = resolver.insert(Config.UserConfigColumns.CONTENT_URI, values);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".insertUserConfigInfo: new user config uri '"
                    + ((userConfigUri != null) ? userConfigUri.toString() : "null"));
        }
    }

    /**
     * Will insert into the car type configuration table information from <code>userConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param userConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertCarTypeInfo(ContentResolver resolver, UserConfig userConfig, String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (userConfig.allowedCarTypes != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            for (CarType carType : userConfig.allowedCarTypes) {

                // Set description.
                ContentUtils.putValue(values, Config.CarTypeColumns.DESCRIPTION, carType.description);

                // Set code.
                ContentUtils.putValue(values, Config.CarTypeColumns.CODE, carType.code);

                // Set "is default".
                ContentUtils.putValue(values, Config.CarTypeColumns.IS_DEFAULT, carType.isDefault);

                // Set the user id.
                ContentUtils.putValue(values, Config.CarTypeColumns.USER_ID, userId);

                Uri carTypeUri = resolver.insert(Config.CarTypeColumns.CONTENT_URI, values);
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".insertCarTypeInfo: new car type uri '"
                            + ((carTypeUri != null) ? carTypeUri.toString() : "null"));
                }
                values.clear();
            }

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertCarTypeInfo: took " + (endTimeMillis - startTimeMillis)
                        + " ms to insert " + Integer.toString(userConfig.allowedCarTypes.size()) + " items.");
            }

        }

    }

    /**
     * Will insert into the attendee column definition table information from <code>userConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param userConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertAttendeeColumnDefinitionInfo(ContentResolver resolver, UserConfig userConfig,
            String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (userConfig.attendeeColumnDefinitions != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            for (AttendeeColumnDefinition atdColDef : userConfig.attendeeColumnDefinitions) {

                // Set id.
                ContentUtils.putValue(values, Config.AttendeeColumnDefinitionColumns.ID, atdColDef.id);

                // Set label.
                ContentUtils.putValue(values, Config.AttendeeColumnDefinitionColumns.LABEL, atdColDef.label);

                // Set data type.
                ContentUtils.putValue(values, Config.AttendeeColumnDefinitionColumns.DATA_TYPE, atdColDef.dataType);

                // Set control type.
                ContentUtils.putValue(values, Config.AttendeeColumnDefinitionColumns.CTRL_TYPE, atdColDef.controlType);

                // Set access.
                ContentUtils.putValue(values, Config.AttendeeColumnDefinitionColumns.ACCESS, atdColDef.accessType);

                // Set the user id.
                ContentUtils.putValue(values, Config.AttendeeColumnDefinitionColumns.USER_ID, userId);

                Uri atdColDefUri = resolver.insert(Config.AttendeeColumnDefinitionColumns.CONTENT_URI, values);
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG
                            + ".insertAttendeeColumnDefinitionInfo: new attendee column definition uri '"
                            + ((atdColDefUri != null) ? atdColDefUri.toString() : "null"));
                }
                values.clear();
            }

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".insertAttendeeColumnDefinitionInfo: took " + (endTimeMillis - startTimeMillis)
                                + " ms to insert " + Integer.toString(userConfig.attendeeColumnDefinitions.size())
                                + " items.");
            }

        }
    }

    /**
     * Will insert into the attendee type table information from <code>userConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param userConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertAttendeeTypeInfo(ContentResolver resolver, UserConfig userConfig, String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (userConfig.attendeeTypes != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            for (AttendeeType atdType : userConfig.attendeeTypes) {

                // Set allow edit attendee count
                ContentUtils.putValue(values, Config.AttendeeTypeColumns.ALLOW_EDIT_ATN_COUNT,
                        atdType.allowEditAtnCount);

                // Set attendee type code.
                ContentUtils.putValue(values, Config.AttendeeTypeColumns.ATN_TYPE_CODE, atdType.atnTypeCode);

                // Set attendee type key.
                ContentUtils.putValue(values, Config.AttendeeTypeColumns.ATN_TYPE_KEY, atdType.atnTypeKey);

                // Set attendee type name.
                ContentUtils.putValue(values, Config.AttendeeTypeColumns.ATN_TYPE_NAME, atdType.atnTypeName);

                // Set form key.
                ContentUtils.putValue(values, Config.AttendeeTypeColumns.FORM_KEY, atdType.formKey);

                // Set is external.
                ContentUtils.putValue(values, Config.AttendeeTypeColumns.IS_EXTERNAL, atdType.isExternal);

                // Set the user id.
                ContentUtils.putValue(values, Config.AttendeeTypeColumns.USER_ID, userId);

                Uri atdTypeUri = resolver.insert(Config.AttendeeTypeColumns.CONTENT_URI, values);
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".insertAttendeeTypeInfo: new attendee type uri '"
                            + ((atdTypeUri != null) ? atdTypeUri.toString() : "null"));
                }
                values.clear();
            }

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertAttendeeTypeInfo: took " + (endTimeMillis - startTimeMillis)
                        + " ms to insert " + Integer.toString(userConfig.attendeeTypes.size()) + " items.");
            }

        }

    }

    /**
     * Will insert into the currency table information from <code>userConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param userConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertCurrencyInfo(ContentResolver resolver, UserConfig userConfig, String userId) {

        // First, do non-reimbursable currencies.
        if (userConfig.currencies != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            ContentValues[] valuesInfo = new ContentValues[userConfig.currencies.size()];
            int valInd = 0;

            for (Currency curr : userConfig.currencies) {

                valuesInfo[valInd] = new ContentValues();

                // Set crn code.
                ContentUtils.putValue(valuesInfo[valInd], Config.CurrencyColumns.CRN_CODE, curr.crnCode);

                // Set crn name.
                ContentUtils.putValue(valuesInfo[valInd], Config.CurrencyColumns.CRN_NAME, curr.crnName);

                // Set decimal digits.
                ContentUtils.putValue(valuesInfo[valInd], Config.CurrencyColumns.DECIMAL_DIGITS, curr.decimalDigits);

                // Set the "is reimbursable" to 'false'.
                ContentUtils.putValue(valuesInfo[valInd], Config.CurrencyColumns.IS_REIMBURSEMENT, false);

                // Set the user id.
                ContentUtils.putValue(valuesInfo[valInd], Config.CurrencyColumns.USER_ID, userId);

                ++valInd;
            }

            int numInserted = resolver.bulkInsert(Config.CurrencyColumns.CONTENT_URI, valuesInfo);

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertCurrencyInfo: took " + (endTimeMillis - startTimeMillis)
                        + " ms to insert " + Integer.toString(numInserted) + " items.");
            }

        }

        // Second, do reimbursable currencies.
        if (userConfig.reimbursementCurrencies != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            ContentValues[] valuesInfo = new ContentValues[userConfig.reimbursementCurrencies.size()];
            int valInd = 0;

            for (Currency curr : userConfig.reimbursementCurrencies) {

                valuesInfo[valInd] = new ContentValues();

                // Set crn code.
                ContentUtils.putValue(valuesInfo[valInd], Config.CurrencyColumns.CRN_CODE, curr.crnCode);

                // Set crn name.
                ContentUtils.putValue(valuesInfo[valInd], Config.CurrencyColumns.CRN_NAME, curr.crnName);

                // Set decimal digits.
                ContentUtils.putValue(valuesInfo[valInd], Config.CurrencyColumns.DECIMAL_DIGITS, curr.decimalDigits);

                // Set the "is reimbursable" to 'true'.
                ContentUtils.putValue(valuesInfo[valInd], Config.CurrencyColumns.IS_REIMBURSEMENT, true);

                // Set the user id.
                ContentUtils.putValue(valuesInfo[valInd], Config.CurrencyColumns.USER_ID, userId);

                ++valInd;
            }

            int numInserted = resolver.bulkInsert(Config.CurrencyColumns.CONTENT_URI, valuesInfo);

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertReimbursementCurrencyInfo: took "
                        + (endTimeMillis - startTimeMillis) + " ms to insert " + Integer.toString(numInserted)
                        + " items.");
            }

        }
    }

    /**
     * Will insert into the expense confirmation table information from <code>userConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param userConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertExpenseConfirmationInfo(ContentResolver resolver, UserConfig userConfig, String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (userConfig.expenseConfirmations != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            for (ExpenseConfirmation expConf : userConfig.expenseConfirmations) {

                // Set confirmation key.
                ContentUtils.putValue(values, Config.ExpenseConfirmationColumns.CONFIRMATION_KEY, expConf.key);

                // Set text.
                ContentUtils.putValue(values, Config.ExpenseConfirmationColumns.TEXT, expConf.text);

                // Set title.
                ContentUtils.putValue(values, Config.ExpenseConfirmationColumns.TITLE, expConf.title);

                // Set the user id.
                ContentUtils.putValue(values, Config.ExpenseConfirmationColumns.USER_ID, userId);

                Uri expConfUri = resolver.insert(Config.ExpenseConfirmationColumns.CONTENT_URI, values);
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".insertExpenseConfirmationInfo: new expense confirmation uri '"
                            + ((expConfUri != null) ? expConfUri.toString() : "null"));
                }
                values.clear();
            }

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".insertExpenseConfirmationInfo: took " + (endTimeMillis - startTimeMillis)
                                + " ms to insert " + Integer.toString(userConfig.expenseConfirmations.size())
                                + " items.");
            }

        }

    }

    /**
     * Will insert into the policy table information from <code>userConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param userConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertPolicyInfo(ContentResolver resolver, UserConfig userConfig, String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (userConfig.expensePolicies != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            for (Policy pol : userConfig.expensePolicies) {

                // Set pol key.
                ContentUtils.putValue(values, Config.PolicyColumns.POL_KEY, pol.key);

                // Set supports imaging.
                ContentUtils.putValue(values, Config.PolicyColumns.SUPPORTS_IMAGING, pol.supportsImaging);

                // Set approval confirmation key.
                ContentUtils.putValue(values, Config.PolicyColumns.APPROVAL_CONFIRMATION_KEY, pol.approvalKey);

                // Set submit confirmation key.
                ContentUtils.putValue(values, Config.PolicyColumns.SUBMIT_CONFIRMATION_KEY, pol.submitKey);

                // Set the user id.
                ContentUtils.putValue(values, Config.PolicyColumns.USER_ID, userId);

                Uri polUri = resolver.insert(Config.PolicyColumns.CONTENT_URI, values);
                if (DEBUG) {
                    Log.d(Const.LOG_TAG,
                            CLS_TAG + ".insertPolicyInfo: new policy uri '"
                                    + ((polUri != null) ? polUri.toString() : "null"));
                }
                values.clear();
            }

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertPolicyInfo: took " + (endTimeMillis - startTimeMillis)
                        + " ms to insert " + Integer.toString(userConfig.expensePolicies.size()) + " items.");
            }

        }

    }

    /**
     * Will insert into the Yodlee payment type table information from <code>userConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param userConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertYodleePaymentTypeInfo(ContentResolver resolver, UserConfig userConfig, String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (userConfig.yodleePaymentTypes != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            for (YodleePaymentType yodPmtType : userConfig.yodleePaymentTypes) {

                // Set key.
                ContentUtils.putValue(values, Config.YodleePaymentTypeColumns.KEY, yodPmtType.key);

                // Set text.
                ContentUtils.putValue(values, Config.YodleePaymentTypeColumns.TEXT, yodPmtType.text);

                // Set the user id.
                ContentUtils.putValue(values, Config.YodleePaymentTypeColumns.USER_ID, userId);

                Uri ydlPmtTypeUri = resolver.insert(Config.YodleePaymentTypeColumns.CONTENT_URI, values);
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".insertYodleePaymentTypeInfo: new Yodlee payment type uri '"
                            + ((ydlPmtTypeUri != null) ? ydlPmtTypeUri.toString() : "null"));
                }
                values.clear();
            }

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".insertYodleePaymentTypeInfo: took " + (endTimeMillis - startTimeMillis)
                                + " ms to insert " + Integer.toString(userConfig.yodleePaymentTypes.size()) + " items.");
            }

        }

    }

    /**
     * Will insert into the credit card type table information from <code>userConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param userConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertCreditCardInfo(ContentResolver resolver, UserConfig userConfig, String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (userConfig.creditCards != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            for (CreditCard creditCard : userConfig.creditCards) {

                // Set name.
                ContentUtils.putValue(values, Config.CreditCardColumns.NAME, creditCard.name);

                // Set type.
                ContentUtils.putValue(values, Config.CreditCardColumns.TYPE, creditCard.type);

                // Set masked number.
                ContentUtils.putValue(values, Config.CreditCardColumns.MASKED_NUMBER, creditCard.maskedNumber);

                // Set cc id.
                ContentUtils.putValue(values, Config.CreditCardColumns.CC_ID, creditCard.ccId);

                // Set default for.
                ContentUtils.putValue(values, Config.CreditCardColumns.DEFAULT_FOR, creditCard.defaultFor);

                // Set allow for.
                ContentUtils.putValue(values, Config.CreditCardColumns.ALLOW_FOR, creditCard.allowFor);

                // Set last four digits.
                ContentUtils.putValue(values, Config.CreditCardColumns.LAST_FOUR, creditCard.lastFour);

                // Set the user id.
                ContentUtils.putValue(values, Config.CreditCardColumns.USER_ID, userId);

                Uri creditCardUri = resolver.insert(Config.CreditCardColumns.CONTENT_URI, values);
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".insertCreditCardInfo: new credit card uri '"
                            + ((creditCardUri != null) ? creditCardUri.toString() : "null"));
                }
                values.clear();
            }

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertCreditCardInfo: took " + (endTimeMillis - startTimeMillis)
                        + " ms to insert " + Integer.toString(userConfig.creditCards.size()) + " items.");
            }
        }
    }

    /**
     * Will insert into the affinity program table information from <code>userConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param userConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertAffinityProgramInfo(ContentResolver resolver, UserConfig userConfig, String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (userConfig.affinityPrograms != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            for (AffinityProgram affProg : userConfig.affinityPrograms) {

                // Set description.
                ContentUtils.putValue(values, Config.AffinityProgramColumns.DESCRIPTION, affProg.description);

                // Set set vendor.
                ContentUtils.putValue(values, Config.AffinityProgramColumns.VENDOR, affProg.vendor);

                // Set vendor abbreviation.
                ContentUtils.putValue(values, Config.AffinityProgramColumns.VENDOR_ABBREV, affProg.vendorAbbrev);

                // Set program name.
                ContentUtils.putValue(values, Config.AffinityProgramColumns.PROGRAM_NAME, affProg.programName);

                // Set program type.
                ContentUtils.putValue(values, Config.AffinityProgramColumns.PROGRAM_TYPE, affProg.programType);

                // Set program id.
                ContentUtils.putValue(values, Config.AffinityProgramColumns.PROGRAM_ID, affProg.programId);

                // Set is default.
                ContentUtils.putValue(values, Config.AffinityProgramColumns.IS_DEFAULT, affProg.isDefault);

                // Set the user id.
                ContentUtils.putValue(values, Config.AffinityProgramColumns.USER_ID, userId);

                Uri affProgUri = resolver.insert(Config.AffinityProgramColumns.CONTENT_URI, values);
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".insertAffinityProgramInfo: new affinity program uri '"
                            + ((affProgUri != null) ? affProgUri.toString() : "null"));
                }
                values.clear();
            }

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertAffinityProgramInfo: took " + (endTimeMillis - startTimeMillis)
                        + " ms to insert " + Integer.toString(userConfig.affinityPrograms.size()) + " items.");
            }

        }

    }

    /**
     * Will insert into the travel points config table information from <code>userConfig</code>.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param userConfig
     *            contains the system configuration information.
     * @param userId
     *            contains the user id.
     */
    private static void insertTravelPointsConfigInfo(ContentResolver resolver, UserConfig userConfig, String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (userConfig.travelPointsConfig != null) {

            long startTimeMillis = 0L;
            if (TRACK_INSERTION_TIME) {
                startTimeMillis = System.currentTimeMillis();
            }

            TravelPointsConfig travelPointsConfig = userConfig.travelPointsConfig;

            // Set air travel points enabled.
            ContentUtils.putValue(values, Config.TravelPointsConfigColumns.AIR_TRAVEL_POINTS_ENABLED,
                    travelPointsConfig.airTravelPointsEnabled);

            // Set hotel travel points enabled.
            ContentUtils.putValue(values, Config.TravelPointsConfigColumns.HOTEL_TRAVEL_POINTS_ENABLED,
                    travelPointsConfig.hotelTravelPointsEnabled);

            // Set the user id.
            ContentUtils.putValue(values, Config.TravelPointsConfigColumns.USER_ID, userId);

            Uri travPtConfigUri = resolver.insert(Config.TravelPointsConfigColumns.CONTENT_URI, values);
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertTravelPointsConfigInfo: new travel points config uri '"
                        + ((travPtConfigUri != null) ? travPtConfigUri.toString() : "null"));
            }
            values.clear();

            if (TRACK_INSERTION_TIME) {
                long endTimeMillis = System.currentTimeMillis();
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertTravelPointsConfigInfo: took "
                        + (endTimeMillis - startTimeMillis) + " ms to insert one item.");
            }

        }
    }

    /**
     * Set the corresponding permission on Permissions object => Uses Permissions.PermissionName enum to map ws response to field
     * 
     * @param permissions
     * @param cursor
     */
    private static void setPermission(Permissions permissions, Cursor cursor) {
        switch (PermissionName.valueOf(CursorUtil.getStringValue(cursor, Config.PermissionsColumns.NAME))) {
        case HAS_TRAVEL_REQUEST:
            permissions.getAreasPermissions().hasTravelRequest = CursorUtil.getBooleanValue(cursor,
                    PermissionsColumns.VALUE);
            break;
        case TR_USER:
            permissions.getTravelRequestPermissions().isRequestUser = CursorUtil.getBooleanValue(cursor,
                    PermissionsColumns.VALUE);
            break;
        case TR_APPROVER:
            permissions.getTravelRequestPermissions().isRequestApprover = CursorUtil.getBooleanValue(cursor,
                    PermissionsColumns.VALUE);
            break;
        }
    }

    /**
     * Will set the passphrase used to access content from the <code>Config</code> content provider.
     * 
     * @param context
     *            contains an application context.
     * @param passphrase
     *            contains the passphrase.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     * @throws IllegalArgumentException
     *             if <code>passphrase</code> is null or empty
     */
    @SuppressLint("NewApi")
    public static boolean setPassphrase(Context context, String passphrase) {

        if (TextUtils.isEmpty(passphrase)) {
            throw new IllegalArgumentException(CLS_TAG + ".setPassphrase: passphrase is null or empty!");
        }

        boolean retVal = true;
        Bundle result = null;
        // Check for HoneyComb or later to execute 'call' on the ContentResolver object.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ContentResolver resolver = context.getContentResolver();
            result = resolver.call(Config.AUTHORITY_URI, PlatformContentProvider.PROVIDER_METHOD_SET_PASSPHRASE,
                    passphrase, null);
        } else {
            // First, attempt to retrieve an instance of ConfigProvider if the content resolver
            // has created one, if not, then force the content resolver to create it with a bogus query.
            ConfigProvider configProvider = ConfigProvider.getConfigProvider();
            if (configProvider == null) {
                ContentResolver resolver = context.getContentResolver();
                // Force the content resolver to create the ConfigProvider. The query will fail immediately since
                // it doesn't refer to any actual table within the Config provider.
                try {
                    resolver.query(Config.AUTHORITY_URI, null, null, null, null);
                } catch (Exception exc) {
                    // No-op...
                    Log.i(Const.LOG_TAG, CLS_TAG + ".setPassphrase: forced creation of provider -- ignore this error: "
                            + exc.getMessage());
                }
                configProvider = ConfigProvider.getConfigProvider();
            }
            if (configProvider != null) {
                result = configProvider.call(PlatformContentProvider.PROVIDER_METHOD_SET_PASSPHRASE, passphrase, null);
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG
                        + ".setPassphrase: unable to force creation of the Config content provider!");
            }
        }
        if (result != null) {
            retVal = result.getBoolean(PlatformContentProvider.PROVIDER_METHOD_RESULT_KEY, false);
        } else {
            retVal = false;
        }
        return retVal;
    }

    /**
     * Will reset the passphrase used to access content from the <code>Config</code> content provider.
     * 
     * @param context
     *            contains an application context.
     * @param currentPassphrase
     *            contains the current passphrase.
     * @param newPassphrase
     *            contains the new passphrase.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     * @throws IllegalArgumentException
     *             if <code>currentPassphrase</code> or <code>newPassphrase</code> is null or empty.
     */
    @SuppressLint("NewApi")
    public static boolean resetPassphrase(Context context, String currentPassphrase, String newPassphrase) {

        if (TextUtils.isEmpty(currentPassphrase)) {
            throw new IllegalArgumentException(CLS_TAG + ".resetPassphrase: currentPassphrase is null or empty!");
        }
        if (TextUtils.isEmpty(newPassphrase)) {
            throw new IllegalArgumentException(CLS_TAG + ".resetPassphrase: newPassphrase is null or empty!");
        }

        boolean retVal = true;
        Bundle result = null;
        Bundle extras = new Bundle();
        extras.putString(PlatformContentProvider.PROVIDER_METHOD_PASSPHRASE_KEY, newPassphrase);
        // Check for HoneyComb or later to execute 'call' on the ContentResolver object.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ContentResolver resolver = context.getContentResolver();
            result = resolver.call(Config.AUTHORITY_URI, PlatformContentProvider.PROVIDER_METHOD_RESET_PASSPHRASE,
                    currentPassphrase, extras);
        } else {
            // First, attempt to retrieve an instance of ConfigProvider if the content resolver
            // has created one, if not, then force the content resolver to create it with a bogus query.
            ConfigProvider configProvider = ConfigProvider.getConfigProvider();
            if (configProvider == null) {
                ContentResolver resolver = context.getContentResolver();
                // Force the content resolver to create the ConfigProvider. The query will fail immediately since
                // it doesn't refer to any actual table within the Config provider.
                try {
                    resolver.query(Config.AUTHORITY_URI, null, null, null, null);
                } catch (Exception exc) {
                    // No-op...
                    Log.i(Const.LOG_TAG, CLS_TAG
                            + ".resetPassphrase: forced creation of provider -- ignore this error: " + exc.getMessage());
                }
                configProvider = ConfigProvider.getConfigProvider();
            }
            if (configProvider != null) {
                result = configProvider.call(PlatformContentProvider.PROVIDER_METHOD_RESET_PASSPHRASE,
                        currentPassphrase, extras);
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG
                        + ".resetPassphrase: unable to force creation of the Config content provider!");
            }
        }
        if (result != null) {
            retVal = result.getBoolean(PlatformContentProvider.PROVIDER_METHOD_RESULT_KEY, false);
        } else {
            retVal = false;
        }
        return retVal;
    }

    /**
     * Will clear all content contained in the <code>Config</code> content provider.
     * 
     * @param context
     *            contains an application context.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    @SuppressLint("NewApi")
    public static boolean clearContent(Context context) {
        boolean retVal = true;
        Bundle result = null;
        // Check for HoneyComb or later to execute 'call' on the ContentResolver object.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ContentResolver resolver = context.getContentResolver();
            result = resolver.call(Config.AUTHORITY_URI, PlatformContentProvider.PROVIDER_METHOD_CLEAR_CONTENT, null,
                    null);
        } else {
            // First, attempt to retrieve an instance of ConfigProvider if the content resolver
            // has created one, if not, then force the content resolver to create it with a bogus query.
            ConfigProvider configProvider = ConfigProvider.getConfigProvider();
            if (configProvider == null) {
                ContentResolver resolver = context.getContentResolver();
                // Force the content resolver to create the ConfigProvider. The query will fail immediately since
                // it doesn't refer to any actual table within the Config provider.
                try {
                    resolver.query(Config.AUTHORITY_URI, null, null, null, null);
                } catch (Exception exc) {
                    // No-op...
                    Log.i(Const.LOG_TAG, CLS_TAG + ".clearContent: forced creation of provider -- ignore this error: "
                            + exc.getMessage());
                }
                configProvider = ConfigProvider.getConfigProvider();
            }
            if (configProvider != null) {
                result = configProvider.call(PlatformContentProvider.PROVIDER_METHOD_CLEAR_CONTENT, null, null);
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG
                        + ".clearContent: unable to force creation of the Config content provider!");
            }
        }
        if (result != null) {
            retVal = result.getBoolean(PlatformContentProvider.PROVIDER_METHOD_RESULT_KEY, false);
        } else {
            retVal = false;
        }
        return retVal;
    }

    /**
     * Provides an implementation of <code>SessionInfo</code>.
     */
    static class SessionInfoImpl implements SessionInfo {

        String accessToken;

        String authenticationType;

        String sessionId;

        Integer sessionTimeout;

        Long sessionExpirationTime;

        String loginId;

        String serverUrl;

        String signInMethod;

        String ssoUrl;

        String email;

        String userId;

        @Override
        public String getAccessToken() {
            return accessToken;
        }

        @Override
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        public String getAuthenticationType() {
            return authenticationType;
        }

        @Override
        public void setAuthenticationType(String authenticationType) {
            this.authenticationType = authenticationType;
        }

        @Override
        public String getSessionId() {
            return sessionId;
        }

        @Override
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        @Override
        public Integer getSessionTimeout() {
            return sessionTimeout;
        }

        @Override
        public void setSessionTimeout(Integer sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public Long getSessionExpirationTime() {
            return sessionExpirationTime;
        }

        @Override
        public void setSessionExpirationTime(Long sessionExpirationTime) {
            this.sessionExpirationTime = sessionExpirationTime;
        }

        @Override
        public String getLoginId() {
            return loginId;
        }

        @Override
        public void setLoginId(String loginId) {
            this.loginId = loginId;
        }

        @Override
        public String getServerUrl() {
            return serverUrl;
        }

        @Override
        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        @Override
        public String getSignInMethod() {
            return signInMethod;
        }

        @Override
        public void setSignInMethod(String signInMethod) {
            this.signInMethod = signInMethod;
        }

        @Override
        public String getSSOUrl() {
            return ssoUrl;
        }

        @Override
        public void setSSOUrl(String ssoUrl) {
            this.ssoUrl = ssoUrl;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String getUserId() {
            return userId;
        }

        @Override
        public void setUserId(String userId) {
            this.userId = userId;
        }

    }

    /**
     * Provides an implementation of <code>UserInfo</code>.
     */
    static class UserInfoImpl implements UserInfo {

        String entityType;

        String expenseCountryCode;

        Boolean hasRequiredCustomFields;

        Calendar pinExpirationDate;

        String productOffering;

        Integer profileStatus;

        String rolesMobile;

        String contactCompanyName;

        String contactEmail;

        String contactFirstName;

        String contactLastName;

        String contactMiddleInitial;

        String userCurrencyCode;

        String userId;

        @Override
        public String getEntityType() {
            return entityType;
        }

        @Override
        public String getExpenseCountryCode() {
            return expenseCountryCode;
        }

        @Override
        public Boolean hasRequiredCustomFields() {
            return hasRequiredCustomFields;
        }

        @Override
        public Calendar getPinExpirationDate() {
            return pinExpirationDate;
        }

        @Override
        public String getProductOffering() {
            return productOffering;
        }

        @Override
        public Integer getProfileStatus() {
            return profileStatus;
        }

        @Override
        public String getRolesMobile() {
            return rolesMobile;
        }

        @Override
        public String getContactCompanyName() {
            return contactCompanyName;
        }

        @Override
        public String getContactEmail() {
            return contactEmail;
        }

        @Override
        public String getContactFirstName() {
            return contactFirstName;
        }

        @Override
        public String getContactLastName() {
            return contactLastName;
        }

        @Override
        public String getContactMiddleInitial() {
            return contactMiddleInitial;
        }

        @Override
        public String getUserCurrencyCode() {
            return userCurrencyCode;
        }

        @Override
        public String getUserId() {
            return userId;
        }

    }

    /**
     * Provides an implementation of <code>SiteSettingInfo</code>.
     * 
     * @author andrewk
     */
    static class SiteSettingInfoImpl implements SiteSettingInfo {

        String name;

        String type;

        String value;

        String userId;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String getUserId() {
            return userId;
        }

    }

}
