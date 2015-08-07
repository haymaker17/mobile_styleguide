/**
 * 
 */
package com.concur.mobile.platform.config.provider;

import android.util.Log;

import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

/**
 * Provides database schema support for the Config provider.
 * 
 * @author andrewk
 */
public class ConfigDBSchema {

    private static final String CLS_TAG = "ConfigDBSchema";

    // Contains the config database name.
    static final String DATABASE_NAME = "config.db";

    // Contains the current database version.
    // DB History
    // DATABASE_VERSION = 1 -- original version
    // DATABASE_VERSION = 2 -- removed Twitter information.
    // DATABASE_VERSION = 3 -- added 'LOGIN_ID', 'SERVER_URL', 'SIGN_IN_METHOD' and 'SSO_URL' columns to
    // 'SESSION' table.
    // DATABASE_VERSION = 4 -- added 'EMAIL' to 'SESSION' table.
    // DATABASE_VERSION = 5 -- added 'TRAVEL_POINTS_CONFIG' table.
    // DATABASE_VERSION = 6 -- added 'PERMISSIONS' table.
    // DATABASE_VERSION = 7 -- added IS_DISABLE_AUTO_LOGIN to 'User' table.
    static final int DATABASE_VERSION = 7;

    // Creates the session table.
    protected static final String SCHEMA_CREATE_SESSION_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.SessionColumns.TABLE_NAME + " (" + Config.SessionColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.SessionColumns.ACCESS_TOKEN_KEY + " TEXT, "
            + Config.SessionColumns.AUTHENTICATION_TYPE + " TEXT, " + Config.SessionColumns.SESSION_ID + " TEXT, "
            + Config.SessionColumns.SESSION_TIME_OUT + " INTEGER, " + Config.SessionColumns.SESSION_EXPIRATION_TIME
            + " INTEGER, " + Config.SessionColumns.LOGIN_ID + " TEXT, " + Config.SessionColumns.SERVER_URL + " TEXT, "
            + Config.SessionColumns.SIGN_IN_METHOD + " TEXT, " + Config.SessionColumns.SSO_URL + " TEXT, "
            + Config.SessionColumns.EMAIL + " TEXT, " + Config.SessionColumns.USER_ID + " TEXT" + ")";

    // Drop the session table.
    protected static final String DROP_SESSION_TABLE = "DROP TABLE IF EXISTS " + Config.SessionColumns.TABLE_NAME + ";";

    // Creates the user table.
    static final String SCHEMA_CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS " + Config.UserColumns.TABLE_NAME + " ("
            + Config.UserColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.UserColumns.ENTITY_TYPE
            + " TEXT, " + Config.UserColumns.EXPENSE_COUNTRY_CODE + " TEXT, "
            + Config.UserColumns.HAS_REQUIRED_CUSTOM_FIELDS + " INTEGER, " + Config.UserColumns.PIN_EXPIRATION_DATE
            + " TEXT, " + Config.UserColumns.PRODUCT_OFFERING + " TEXT, " + Config.UserColumns.PROFILE_STATUS
            + " INTEGER, " + Config.UserColumns.ROLES_MOBILE + " TEXT, " + Config.UserColumns.CONTACT_COMPANY_NAME
            + " TEXT, " + Config.UserColumns.CONTACT_EMAIL + " TEXT, " + Config.UserColumns.CONTACT_FIRST_NAME
            + " TEXT, " + Config.UserColumns.CONTACT_LAST_NAME + " TEXT, " + Config.UserColumns.CONTACT_MIDDLE_INITIAL
            + " TEXT, " + Config.UserColumns.USER_CURRENCY_CODE + " TEXT, " + Config.UserColumns.IS_DISABLE_AUTO_LOGIN + " INTEGER, "
            + Config.UserColumns.USER_ID + " TEXT"
            + ")";

    // Drop the user table.
    protected static final String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + Config.UserColumns.TABLE_NAME + ";";

    // Creates the site settings table.
    protected static final String SCHEMA_CREATE_SITE_SETTING_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.SiteSettingColumns.TABLE_NAME + " (" + Config.SiteSettingColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.SiteSettingColumns.NAME + " TEXT, "
            + Config.SiteSettingColumns.TYPE + " TEXT, " + Config.SiteSettingColumns.VALUE + " TEXT, "
            + Config.SiteSettingColumns.USER_ID + " TEXT" + ")";

    // Drop the site settings table.
    protected static final String DROP_SITE_SETTING_TABLE = "DROP TABLE IF EXISTS "
            + Config.SiteSettingColumns.TABLE_NAME + ";";

    // Creates the permissions table.
    protected static final String SCHEMA_CREATE_PERMISSIONS_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.PermissionsColumns.TABLE_NAME + " (" 
	    		+ Config.PermissionsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	            + Config.SiteSettingColumns.NAME + " TEXT, "
	            + Config.SiteSettingColumns.VALUE + " TEXT, "
	            + Config.PermissionsColumns.USER_ID + " TEXT" 
            + ")";

    // Drop the permissions table.
    protected static final String DROP_PERMISSIONS_TABLE = "DROP TABLE IF EXISTS "
            + Config.PermissionsColumns.TABLE_NAME + ";";

    // Creates the sytem config table.
    protected static final String SCHEMA_CREATE_SYSTEM_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.SystemConfigColumns.TABLE_NAME + " (" + Config.SystemConfigColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.SystemConfigColumns.HASH + " TEXT, "
            + Config.SystemConfigColumns.REFUND_INFO_CHECKBOX_DEFAULT + " INTEGER, "
            + Config.SystemConfigColumns.REFUND_INFO_MESSAGE + " TEXT, "
            + Config.SystemConfigColumns.REFUND_INFO_SHOW_CHECKBOX + " INTEGER, "
            + Config.SystemConfigColumns.RULE_VIOLATION_EXPLANATION_REQUIRED + " INTEGER, "
            + Config.SystemConfigColumns.USER_ID + " TEXT" + ")";

    // Drop the system config table.
    protected static final String DROP_SYSTEM_CONFIG_TABLE = "DROP TABLE IF EXISTS "
            + Config.SystemConfigColumns.TABLE_NAME + ";";

    // Creates the reason code table.
    protected static final String SCHEMA_CREATE_REASON_CODE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.ReasonCodeColumns.TABLE_NAME + " (" + Config.ReasonCodeColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.ReasonCodeColumns.TYPE + " TEXT, "
            + Config.ReasonCodeColumns.DESCRIPTION + " TEXT, " + Config.ReasonCodeColumns.ID + " INTEGER, "
            + Config.ReasonCodeColumns.VIOLATION_TYPE + " TEXT, " + Config.SystemConfigColumns.USER_ID + " TEXT" + ")";

    // Drop the reason code table.
    protected static final String DROP_REASON_CODE_TABLE = "DROP TABLE IF EXISTS "
            + Config.ReasonCodeColumns.TABLE_NAME + ";";

    // Creates the expense type table.
    protected static final String SCHEMA_CREATE_EXPENSE_TYPE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.ExpenseTypeColumns.TABLE_NAME + " (" + Config.ExpenseTypeColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.ExpenseTypeColumns.EXP_CODE + " TEXT, "
            + Config.ExpenseTypeColumns.EXP_KEY + " TEXT, " + Config.ExpenseTypeColumns.EXP_NAME + " TEXT, "
            + Config.ExpenseTypeColumns.FORM_KEY + " INTEGER, " + Config.ExpenseTypeColumns.HAS_POST_AMT_CALC
            + " INTEGER, " + Config.ExpenseTypeColumns.HAS_TAX_FORM + " INTEGER, "
            + Config.ExpenseTypeColumns.ITEMIZATION_UNALLOW_EXP_KEYS + " TEXT, "
            + Config.ExpenseTypeColumns.ITEMIZATION_FORM_KEY + " INTEGER, "
            + Config.ExpenseTypeColumns.ITEMIZATION_STYLE + " TEXT, " + Config.ExpenseTypeColumns.ITEMIZATION_TYPE
            + " TEXT, " + Config.ExpenseTypeColumns.PARENT_EXP_KEY + " TEXT, "
            + Config.ExpenseTypeColumns.PARENT_EXP_NAME + " TEXT, " + Config.ExpenseTypeColumns.SUPPORTS_ATTENDEES
            + " INTEGER, " + Config.ExpenseTypeColumns.VENDOR_LIST_KEY + " INTEGER, "
            + Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_AMOUNT + " INTEGER, "
            + Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_COUNT + " INTEGER, "
            + Config.ExpenseTypeColumns.ALLOW_NO_SHOWS + " INTEGER, "
            + Config.ExpenseTypeColumns.DISPLAY_ADD_ATTENDEE_ON_FORM + " INTEGER, "
            + Config.ExpenseTypeColumns.DISPLAY_ATTENDEE_AMOUNTS + " INTEGER, "
            + Config.ExpenseTypeColumns.USER_AS_ATTENDEE_DEFAULT + " INTEGER, "
            + Config.ExpenseTypeColumns.UNALLOW_ATN_TYPE_KEYS + " TEXT, " + Config.ExpenseTypeColumns.USER_ID + " TEXT"
            + ")";

    // Drop the expense type table.
    protected static final String DROP_EXPENSE_TYPE_TABLE = "DROP TABLE IF EXISTS "
            + Config.ExpenseTypeColumns.TABLE_NAME + ";";

    // Creates the office location table.
    protected static final String SCHEMA_CREATE_OFFICE_LOCATION_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.OfficeLocationColumns.TABLE_NAME + " (" + Config.OfficeLocationColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.OfficeLocationColumns.ADDRESS + " TEXT, "
            + Config.OfficeLocationColumns.CITY + " TEXT, " + Config.OfficeLocationColumns.COUNTRY + " TEXT, "
            + Config.OfficeLocationColumns.LAT + " REAL, " + Config.OfficeLocationColumns.LON + " REAL, "
            + Config.OfficeLocationColumns.STATE + " TEXT, " + Config.OfficeLocationColumns.USER_ID + " TEXT" + ")";

    // Drop the office location table.
    protected static final String DROP_OFFICE_LOCATION_TABLE = "DROP TABLE IF EXISTS "
            + Config.OfficeLocationColumns.TABLE_NAME + ";";

    // Creates the user config table.
    protected static final String SCHEMA_CREATE_USER_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.UserConfigColumns.TABLE_NAME + " (" + Config.UserConfigColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.UserConfigColumns.HASH + " TEXT, "
            + Config.UserConfigColumns.ALLOWED_AIR_CLASSES_OF_SERVICE + " TEXT, " + Config.UserConfigColumns.FLAGS
            + " TEXT, " + Config.UserConfigColumns.SHOW_GDS_NAME_IN_SEARCH_RESULTS + " INTEGER, "
            + Config.UserConfigColumns.USER_ID + " TEXT" + ")";

    // Drop the user config table.
    protected static final String DROP_USER_CONFIG_TABLE = "DROP TABLE IF EXISTS "
            + Config.UserConfigColumns.TABLE_NAME + ";";

    // Creates the user config car type table.
    protected static final String SCHEMA_CREATE_CAR_TYPE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.CarTypeColumns.TABLE_NAME + " (" + Config.CarTypeColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.CarTypeColumns.DESCRIPTION + " TEXT, "
            + Config.CarTypeColumns.CODE + " TEXT, " + Config.CarTypeColumns.IS_DEFAULT + " INTEGER, "
            + Config.CarTypeColumns.USER_ID + " TEXT" + ")";

    // Drop the user config car type table.
    protected static final String DROP_CAR_TYPE_TABLE = "DROP TABLE IF EXISTS " + Config.CarTypeColumns.TABLE_NAME
            + ";";

    // Creates the user config attendee column definition table.
    static final String SCHEMA_CREATE_ATTENDEE_COLUMN_DEFINITION_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.AttendeeColumnDefinitionColumns.TABLE_NAME + " (" + Config.AttendeeColumnDefinitionColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.AttendeeColumnDefinitionColumns.ID + " TEXT, "
            + Config.AttendeeColumnDefinitionColumns.LABEL + " TEXT, "
            + Config.AttendeeColumnDefinitionColumns.DATA_TYPE + " TEXT, "
            + Config.AttendeeColumnDefinitionColumns.CTRL_TYPE + " TEXT, "
            + Config.AttendeeColumnDefinitionColumns.ACCESS + " TEXT, "
            + Config.AttendeeColumnDefinitionColumns.USER_ID + " TEXT" + ")";

    // Drop the user config attendee column table.
    static final String DROP_ATTENDEE_COLUMN_DEFINITION_TABLE = "DROP TABLE IF EXISTS "
            + Config.AttendeeColumnDefinitionColumns.TABLE_NAME + ";";

    // Creates the user config attendee type table.
    protected static final String SCHEMA_CREATE_ATTENDEE_TYPE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.AttendeeTypeColumns.TABLE_NAME + " (" + Config.AttendeeTypeColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.AttendeeTypeColumns.ALLOW_EDIT_ATN_COUNT + " INTEGER, "
            + Config.AttendeeTypeColumns.ATN_TYPE_CODE + " TEXT, " + Config.AttendeeTypeColumns.ATN_TYPE_KEY
            + " TEXT, " + Config.AttendeeTypeColumns.ATN_TYPE_NAME + " TEXT, " + Config.AttendeeTypeColumns.FORM_KEY
            + " TEXT, " + Config.AttendeeTypeColumns.IS_EXTERNAL + " INTEGER, " + Config.AttendeeTypeColumns.USER_ID
            + " TEXT" + ")";

    // Drop the user config attendee type column table.
    protected static final String DROP_ATTENDEE_TYPE_TABLE = "DROP TABLE IF EXISTS "
            + Config.AttendeeTypeColumns.TABLE_NAME + ";";

    // Creates the user config currency table.
    protected static final String SCHEMA_CREATE_CURRENCY_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.CurrencyColumns.TABLE_NAME + " (" + Config.CurrencyColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.CurrencyColumns.CRN_CODE + " TEXT, "
            + Config.CurrencyColumns.CRN_NAME + " TEXT, " + Config.CurrencyColumns.DECIMAL_DIGITS
            + " INTEGER DEFAULT 0, " + Config.CurrencyColumns.IS_REIMBURSEMENT + " INTEGER, "
            + Config.CurrencyColumns.USER_ID + " TEXT" + ")";

    // Drop the user config currency table.
    protected static final String DROP_CURRENCY_TABLE = "DROP TABLE IF EXISTS " + Config.CurrencyColumns.TABLE_NAME
            + ";";

    // Creates the user config expense confirmation table.
    protected static final String SCHEMA_CREATE_EXPENSE_CONFIRMATION_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.ExpenseConfirmationColumns.TABLE_NAME + " (" + Config.ExpenseConfirmationColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.ExpenseConfirmationColumns.CONFIRMATION_KEY + " TEXT, "
            + Config.ExpenseConfirmationColumns.TEXT + " TEXT, " + Config.ExpenseConfirmationColumns.TITLE + " TEXT, "
            + Config.ExpenseConfirmationColumns.USER_ID + " TEXT" + ")";

    // Drop the user config expense confirmation table.
    protected static final String DROP_EXPENSE_CONFIRMATION_TABLE = "DROP TABLE IF EXISTS "
            + Config.ExpenseConfirmationColumns.TABLE_NAME + ";";

    // Creates the user config expense policy table.
    protected static final String SCHEMA_CREATE_POLICY_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.PolicyColumns.TABLE_NAME + " (" + Config.PolicyColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.PolicyColumns.POL_KEY + " TEXT, "
            + Config.PolicyColumns.SUPPORTS_IMAGING + " INTEGER, " + Config.PolicyColumns.APPROVAL_CONFIRMATION_KEY
            + " TEXT, " + Config.PolicyColumns.SUBMIT_CONFIRMATION_KEY + " TEXT, " + Config.PolicyColumns.USER_ID
            + " TEXT" + ")";

    // Drop the user config expense policy table.
    protected static final String DROP_POLICY_TABLE = "DROP TABLE IF EXISTS " + Config.PolicyColumns.TABLE_NAME + ";";

    // Creates the user config Yodlee payment type table.
    protected static final String SCHEMA_CREATE_YODLEE_PAYMENT_TYPE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.YodleePaymentTypeColumns.TABLE_NAME + " (" + Config.YodleePaymentTypeColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.YodleePaymentTypeColumns.KEY + " TEXT, "
            + Config.YodleePaymentTypeColumns.TEXT + " TEXT, " + Config.YodleePaymentTypeColumns.USER_ID + " TEXT"
            + ")";

    // Drop the user config Yodlee payment type table.
    protected static final String DROP_YODLEE_PAYMENT_TYPE_TABLE = "DROP TABLE IF EXISTS "
            + Config.YodleePaymentTypeColumns.TABLE_NAME + ";";

    // Creates the user travel points config table.
    static final String SCHEMA_CREATE_TRAVEL_POINTS_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.TravelPointsConfigColumns.TABLE_NAME + " (" + Config.TravelPointsConfigColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.TravelPointsConfigColumns.AIR_TRAVEL_POINTS_ENABLED
            + " INTEGER, " + Config.TravelPointsConfigColumns.HOTEL_TRAVEL_POINTS_ENABLED + " INTEGER, "
            + Config.TravelPointsConfigColumns.USER_ID + " TEXT" + ")";

    // Drop the user config credit card table.
    protected static final String DROP_TRAVEL_POINTS_CONFIG_TABLE = "DROP TABLE IF EXISTS "
            + Config.TravelPointsConfigColumns.TABLE_NAME + ";";

    // Creates the user config credit card table.
    protected static final String SCHEMA_CREATE_CREDIT_CARD_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.CreditCardColumns.TABLE_NAME + " (" + Config.CreditCardColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.CreditCardColumns.NAME + " TEXT, "
            + Config.CreditCardColumns.TYPE + " TEXT, " + Config.CreditCardColumns.MASKED_NUMBER + " TEXT, "
            + Config.CreditCardColumns.CC_ID + " TEXT, " + Config.CreditCardColumns.DEFAULT_FOR + " TEXT, "
            + Config.CreditCardColumns.ALLOW_FOR + " TEXT, " + Config.CreditCardColumns.LAST_FOUR + " TEXT, "
            + Config.CreditCardColumns.USER_ID + " TEXT" + ")";

    // Drop the user config credit card table.
    protected static final String DROP_CREDIT_CARD_TABLE = "DROP TABLE IF EXISTS "
            + Config.CreditCardColumns.TABLE_NAME + ";";

    // Creates the user config affinity program table.
    protected static final String SCHEMA_CREATE_AFFINITY_PROGRAM_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.AffinityProgramColumns.TABLE_NAME + " (" + Config.AffinityProgramColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.AffinityProgramColumns.ACCOUNT_NUMBER + " TEXT, "
            + Config.AffinityProgramColumns.DESCRIPTION + " TEXT, " + Config.AffinityProgramColumns.VENDOR + " TEXT, "
            + Config.AffinityProgramColumns.VENDOR_ABBREV + " TEXT, " + Config.AffinityProgramColumns.PROGRAM_NAME
            + " TEXT, " + Config.AffinityProgramColumns.PROGRAM_TYPE + " TEXT, "
            + Config.AffinityProgramColumns.PROGRAM_ID + " TEXT, " + Config.AffinityProgramColumns.IS_DEFAULT
            + " INTEGER, " + Config.AffinityProgramColumns.USER_ID + " TEXT" + ")";

    // Drop the user config affinity program table.
    protected static final String DROP_AFFINITY_PROGRAM_TABLE = "DROP TABLE IF EXISTS "
            + Config.AffinityProgramColumns.TABLE_NAME + ";";

    // Creates the client data table.
    protected static final String SCHEMA_CREATE_CLIENT_DATA_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Config.ClientDataColumns.TABLE_NAME + " (" + Config.ClientDataColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Config.ClientDataColumns.KEY + " TEXT, "
            + Config.ClientDataColumns.VALUE_TEXT + " TEXT, " + Config.ClientDataColumns.VALUE_BLOB + " BLOB, "
            + Config.AffinityProgramColumns.USER_ID + " TEXT" + ")";

    // Drop the client data table.
    protected static final String DROP_CLIENT_DATA_TABLE = "DROP TABLE IF EXISTS "
            + Config.AffinityProgramColumns.TABLE_NAME + ";";

    // Contains the config schema creation SQL. Must be in execution order
    protected static final String[] SCHEMA_CREATE_SQL = { SCHEMA_CREATE_SESSION_TABLE, //
            SCHEMA_CREATE_USER_TABLE, //
            SCHEMA_CREATE_SITE_SETTING_TABLE, //
            SCHEMA_CREATE_PERMISSIONS_TABLE, //
            SCHEMA_CREATE_SYSTEM_CONFIG_TABLE, //
            SCHEMA_CREATE_REASON_CODE_TABLE, //
            SCHEMA_CREATE_EXPENSE_TYPE_TABLE, //
            SCHEMA_CREATE_OFFICE_LOCATION_TABLE, //
            SCHEMA_CREATE_USER_CONFIG_TABLE, //
            SCHEMA_CREATE_CAR_TYPE_TABLE, //
            SCHEMA_CREATE_ATTENDEE_COLUMN_DEFINITION_TABLE, //
            SCHEMA_CREATE_ATTENDEE_TYPE_TABLE, //
            SCHEMA_CREATE_CURRENCY_TABLE, //
            SCHEMA_CREATE_EXPENSE_CONFIRMATION_TABLE, //
            SCHEMA_CREATE_POLICY_TABLE, //
            SCHEMA_CREATE_YODLEE_PAYMENT_TYPE_TABLE, //
            SCHEMA_CREATE_TRAVEL_POINTS_CONFIG_TABLE, //
            SCHEMA_CREATE_CREDIT_CARD_TABLE, //
            SCHEMA_CREATE_AFFINITY_PROGRAM_TABLE, //
            SCHEMA_CREATE_CLIENT_DATA_TABLE //
    };

    // Contains the config schema deletion SQL. Must be in execution order
    protected static final String[] SCHEMA_DELETE_SQL = { //
    DROP_SESSION_TABLE, //
            DROP_USER_TABLE, //
            DROP_SITE_SETTING_TABLE, //
            DROP_PERMISSIONS_TABLE, //
            DROP_SYSTEM_CONFIG_TABLE, //
            DROP_REASON_CODE_TABLE, //
            DROP_EXPENSE_TYPE_TABLE, //
            DROP_OFFICE_LOCATION_TABLE, //
            DROP_USER_CONFIG_TABLE, //
            DROP_CAR_TYPE_TABLE, //
            DROP_ATTENDEE_COLUMN_DEFINITION_TABLE, //
            DROP_ATTENDEE_TYPE_TABLE, //
            DROP_CURRENCY_TABLE, //
            DROP_EXPENSE_CONFIRMATION_TABLE, //
            DROP_POLICY_TABLE, //
            DROP_YODLEE_PAYMENT_TYPE_TABLE, //
            DROP_TRAVEL_POINTS_CONFIG_TABLE, //
            DROP_CREDIT_CARD_TABLE, //
            DROP_AFFINITY_PROGRAM_TABLE, //
            DROP_CLIENT_DATA_TABLE //
    };

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    static void onCreate(PlatformSQLiteDatabase db) {

        Log.v(Const.LOG_TAG, "Creating schema.");

        for (int i = 0; i < SCHEMA_CREATE_SQL.length; i++) {
            db.execSQL(SCHEMA_CREATE_SQL[i]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     * 
     * The pattern here is that the last numbered case statement should be the only one with a break; IE Case 1: code, Case 2:
     * code, Case 3: code, break;
     * 
     * The case used will be one less than the current db version because if we upgrade from version 3 to 4, the oldVersion will
     * be 3. The logic here is that if a user upgrades multiple versions (IE they have version 2 and upgrade to version 5), the
     * database upgrades will happen in the order that updates were written. That is, Case 2 will be executed, then 3, then 4. The
     * upgrades made in 4 will have the user up to date in version 5.
     */
    static void onUpgrade(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {

        Log.v(Const.LOG_TAG, "Upgrading database from " + oldVersion + " to " + newVersion);

        switch (oldVersion) {
        case 1: {
            SchemaUpgradeAction sua = new RemoveTwitterFromUserTableAction(db, oldVersion, newVersion);
            if (!sua.upgrade()) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute RemoveTwitterFromUserTableAction!");
            }
        }
        case 2: {
            SchemaUpgradeAction sua = new AddEmailLookUpToSessionAction(db, oldVersion, newVersion);
            if (!sua.upgrade()) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute AddEmailLookUpToSessionAction!");
            }
        }
        case 3: {
            SchemaUpgradeAction sua = new AddEmailToSessionAction(db, oldVersion, newVersion);
            if (!sua.upgrade()) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute AddEmailToSessionAction!");
            }
        }
        case 4: {
            SchemaUpgradeAction sua = new AddUserConfigV2SupportAction(db, oldVersion, newVersion);
            if (!sua.upgrade()) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute AddUserConfigV2SupportAction!");
            }
        }
        case 5: {
            final SchemaUpgradeAction sua = new AddPermissionsTableUpgradeAction(db, oldVersion, newVersion);
            if (!sua.upgrade()) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute AddPermissionsTableUpgradeAction!");
            }
            break;
        }
        case 6: {
            SchemaUpgradeAction sua = new AddUserDisableAutoLoginSupportAction(db, oldVersion, newVersion);
            if (!sua.upgrade()) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute AddUserDisableAutoLoginSupportAction!");
            }
        }
        default: {
            Log.v(Const.LOG_TAG, "DB version provided no upgrade path: " + newVersion);
            break;
        }
        }
    }

}
