/**
 * 
 */
package com.concur.mobile.platform.config.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This class provide a contract between the config content provider and calling application code.
 * 
 * @author andrewk
 */
public final class Config {

    /**
     * The authority for the config provider.
     */
    public static final String AUTHORITY = "com.concur.mobile.platform.config";

    /**
     * A content:// style uri to the authority for the config provider
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    //
    // Start User Information
    //

    /**
     * Models session information.
     */
    public static final class SessionColumns implements BaseColumns {

        // Prevent instantiation.
        private SessionColumns() {
        }

        /**
         * Contains the session table name.
         */
        public static final String TABLE_NAME = "SESSION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Sessions URI
         */
        private static final String PATH_SESSIONS = "/sessions";

        /**
         * Path part for the Session ID URI
         */
        private static final String PATH_SESSION_ID = "/sessions/";

        /**
         * 0-relative position of a session ID segment in the path part of a session ID URI
         */
        public static final int SESSION_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SESSIONS);

        /**
         * The content URI base for a single note. Callers must append a numeric note id to this Uri to retrieve a note
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_SESSION_ID);

        /**
         * The content URI match pattern for a single note, specified by its ID. Use this to match incoming URIs or to construct
         * an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_SESSION_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of sessions.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.sessions";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single session.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.session";

        // Column definitions

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        /**
         * Contains the session access token column name.
         */
        public static final String ACCESS_TOKEN_KEY = "ACCESS_TOKEN_KEY";

        /**
         * Contains the authentication type column name.
         */
        public static final String AUTHENTICATION_TYPE = "AUTHENTICATION_TYPE";

        /**
         * Contains the session id column name.
         */
        public static final String SESSION_ID = "SESSION_ID";

        /**
         * Contains the session expiration time.
         */
        public static final String SESSION_EXPIRATION_TIME = "SESSION_EXPIRATION_TIME";

        /**
         * Contains the session timeout column name.
         */
        public static final String SESSION_TIME_OUT = "SESSION_TIME_OUT";

        /**
         * Contains the login ID (TEXT) column name.
         */
        public static final String LOGIN_ID = "LOGIN_ID";

        /**
         * Contains the server URL (TEXT) column name.
         */
        public static final String SERVER_URL = "SERVER_URL";

        /**
         * Contains the sign-in method (TEXT) column name.
         */
        public static final String SIGN_IN_METHOD = "SIGN_IN_METHOD";

        /**
         * Contains the SSO URL (TEXT) column name.
         */
        public static final String SSO_URL = "SSO_URL";

        /**
         * Contains the Email (TEXT) column name.
         */
        public static final String EMAIL = "EMAIL";

        /**
         * Contains the column name of the user id associated with this session information.
         */
        public static final String USER_ID = UserColumns.USER_ID;
    }

    /**
     * Contains the user information column names.
     */
    public static final class UserColumns implements BaseColumns {

        // Prevent instantiation.
        private UserColumns() {
        }

        /**
         * Contains the user table name.
         */
        public static final String TABLE_NAME = "USER";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Users URI
         */
        private static final String PATH_USERS = "/users";

        /**
         * Path part for the User ID URI
         */
        private static final String PATH_USER_ID = "/users/";

        /**
         * 0-relative position of a session ID segment in the path part of a session ID URI
         */
        public static final int USER_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_USERS);

        /**
         * The content URI base for a single user. Callers must append a numeric user id to this Uri to retrieve a user
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_USER_ID);

        /**
         * The content URI match pattern for a single user, specified by its ID. Use this to match incoming URIs or to construct
         * an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_USER_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of sessions.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.users";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single session.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.user";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        /**
         * Contains the user entity type column name.
         */
        public static final String ENTITY_TYPE = "ENTITY_TYPE";

        /**
         * Contains the user expense country code column name.
         */
        public static final String EXPENSE_COUNTRY_CODE = "EXPENSE_COUNTRY_CODE";

        /**
         * Contains the has required custom fields column name.
         */
        public static final String HAS_REQUIRED_CUSTOM_FIELDS = "HAS_REQUIRED_CUSTOM_FIELDS";

        /**
         * Contains the pin expiration date column name.
         */
        public static final String PIN_EXPIRATION_DATE = "PIN_EXPIRATION_DATE";

        /**
         * Contains the product offering column name.
         */
        public static final String PRODUCT_OFFERING = "PRODUCT_OFFERING";

        /**
         * Contains the profile status column name.
         */
        public static final String PROFILE_STATUS = "PROFILE_STATUS";

        /**
         * Contains the mobile roles column name.
         */
        public static final String ROLES_MOBILE = "ROLES_MOBILE";

        /**
         * Contains the contact company name column name.
         */
        public static final String CONTACT_COMPANY_NAME = "CONTACT_COMPANY_NAME";

        /**
         * Contains the contact email column name.
         */
        public static final String CONTACT_EMAIL = "CONTACT_EMAIL";

        /**
         * Contains the contact first name column name.
         */
        public static final String CONTACT_FIRST_NAME = "CONTACT_FIRST_NAME";

        /**
         * Contains the contact last name column name.
         */
        public static final String CONTACT_LAST_NAME = "CONTACT_LAST_NAME";

        /**
         * Contains the contact middle initial column name.
         */
        public static final String CONTACT_MIDDLE_INITIAL = "CONTACT_MIDDLE_INITIAL";

        /**
         * Contains the user currency code column name.
         */
        public static final String USER_CURRENCY_CODE = "USER_CURRENCY_CODE";

        /**
         * Contains the disable auto login column name.
         */
        public static final String IS_DISABLE_AUTO_LOGIN = "IS_DISABLE_AUTO_LOGIN";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = "USER_ID";
    }

    /**
     * Contains the site setting columns names.
     */
    public static final class SiteSettingColumns implements BaseColumns {

        // Prevent instantiation.
        private SiteSettingColumns() {
        }

        /**
         * Contains the site setting table name.
         */
        public static final String TABLE_NAME = "SITE_SETTING";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the site settings URI
         */
        private static final String PATH_SITE_SETTINGS = "/site_settings";

        /**
         * Path part for the site setting ID URI
         */
        private static final String PATH_SITE_SETTING_ID = "/site_settings/";

        /**
         * 0-relative position of a site setting ID segment in the path part of a site setting ID URI
         */
        public static final int SITE_SETTING_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SITE_SETTINGS);

        /**
         * The content URI base for a single site setting. Callers must append a numeric user id to this Uri to retrieve a site
         * setting
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_SITE_SETTING_ID);

        /**
         * The content URI match pattern for a single site setting, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_SITE_SETTING_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of sessions.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.site_settings";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single session.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.site_setting";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        /**
         * Contains the setting name column name.
         */
        public static final String NAME = "NAME";

        /**
         * Contains the type column name.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the value column name.
         */
        public static final String VALUE = "VALUE";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }
    
    /**
     * Contains the permissions columns names.
     */
    public static final class PermissionsColumns implements BaseColumns {

        // Prevent instantiation.
        private PermissionsColumns() {
        }

        /**
         * Contains the permissions table name.
         */
        public static final String TABLE_NAME = "PERMISSIONS";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the permissionss URI
         */
        private static final String PATH_PERMISSIONS = "/permissions";

        /**
         * Path part for the permissions ID URI
         */
        private static final String PATH_PERMISSIONS_ID = "/permissions/";

        /**
         * 0-relative position of a permissions ID segment in the path part of a permissions ID URI
         */
        public static final int PERMISSIONS_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PERMISSIONS);

        /**
         * The content URI base for a single permissions. Callers must append a numeric user id to this Uri to retrieve a site
         * setting
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_PERMISSIONS_ID);

        /**
         * The content URI match pattern for a single permissions, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_PERMISSIONS_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of sessions.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.permissions";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single session.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.permission";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        /**
         * Contains the permission column name.
         */
        public static final String NAME = "NAME";

        /**
         * Contains the value column name.
         */
        public static final String VALUE = "VALUE";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    //
    // End User Information
    //

    //
    // Start System Config Information
    //
    /**
     * Contains the system configuration table information.
     */
    public static class SystemConfigColumns implements BaseColumns {

        // Prevent instantiation.
        private SystemConfigColumns() {
        }

        /**
         * Contains the system config table name.
         */
        public static final String TABLE_NAME = "SYSTEM_CONFIG";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the system configs URI
         */
        private static final String PATH_SYSTEM_CONFIGS = "/system_configs";

        /**
         * Path part for the system config ID URI
         */
        private static final String PATH_SYSTEM_CONFIG_ID = "/system_configs/";

        /**
         * 0-relative position of a sysem config ID segment in the path part of a system config ID URI
         */
        public static final int SYSTEM_CONFIG_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SYSTEM_CONFIGS);

        /**
         * The content URI base for a single system config. Callers must append a numeric user id to this Uri to retrieve a system
         * config.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_SYSTEM_CONFIG_ID);

        /**
         * The content URI match pattern for a single system config, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_SYSTEM_CONFIG_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of system configs.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.system_configs";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single system config.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.system_config";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the column name storing server-created hash code value (TEXT) for the system configuration information.
         */
        public static final String HASH = "HASH";

        /**
         * Contains the column name storing the default check value (BOOLEAN) for the default refundable checkbox value.
         */
        public static final String REFUND_INFO_CHECKBOX_DEFAULT = "REFUND_INFO_CHECKBOX_DEFAULT";

        /**
         * Contains the column name storing the refundable message value (TEXT).
         */
        public static final String REFUND_INFO_MESSAGE = "REFUND_INFO_MESSAGE";

        /**
         * Contains the column name storing whether to show the refundable checkbox (BOOLEAN).
         */
        public static final String REFUND_INFO_SHOW_CHECKBOX = "REFUND_INFO_SHOW_CHECKBOX";

        /**
         * Contains the column name storing whether an explanation is required when booking with a rule violation (BOOLEAN).
         */
        public static final String RULE_VIOLATION_EXPLANATION_REQUIRED = "RULE_VIOLATION_EXPLANATION_REQUIRED";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the system configuration reason code table information.
     */
    public static class ReasonCodeColumns implements BaseColumns {

        // Prevent instantiation.
        private ReasonCodeColumns() {
        }

        /**
         * Contains the reason code table name.
         */
        public static final String TABLE_NAME = "REASON_CODE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the reason codes URI
         */
        private static final String PATH_REASON_CODES = "/reason_codes";

        /**
         * Path part for the reason code ID URI
         */
        private static final String PATH_REASON_CODE_ID = "/reason_codes/";

        /**
         * 0-relative position of a reason code ID segment in the path part of a reason code ID URI
         */
        public static final int REASON_CODE_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_REASON_CODES);

        /**
         * The content URI base for a single reason code. Callers must append a numeric user id to this Uri to retrieve a reason
         * code.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_REASON_CODE_ID);

        /**
         * The content URI match pattern for a single reason code, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_REASON_CODE_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of reason codes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.reason_codes";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single reason code.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.reason_code";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the "reason code type" air value.
         */
        public static final String TYPE_AIR = "AIR";

        /**
         * Contains the "reason code type" hotel value.
         */
        public static final String TYPE_HOTEL = "HOTEL";

        /**
         * Contains the "reason code type" car value.
         */
        public static final String TYPE_CAR = "CAR";

        /**
         * Contains the type (TEXT) column name.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the description (TEXT) column name.
         */
        public static final String DESCRIPTION = "DESCRIPTION";

        /**
         * Contains the ID (TEXT) column name.
         */
        public static final String ID = "ID";

        /**
         * Contains the "violation type" (TEXT) column name.
         */
        public static final String VIOLATION_TYPE = "VIOLATION_TYPE";

        /**
         * Contains the user id (TEXT) column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the system configuration expense type table information.
     */
    public static class ExpenseTypeColumns implements BaseColumns {

        // Prevent instantiation.
        private ExpenseTypeColumns() {
        }

        /**
         * Contains the expense type table name.
         */
        public static final String TABLE_NAME = "EXPENSE_TYPE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the expense types URI
         */
        private static final String PATH_EXPENSE_TYPES = "/expense_types";

        /**
         * Path part for the expense type ID URI
         */
        private static final String PATH_EXPENSE_TYPE_ID = "/expense_types/";

        /**
         * 0-relative position of a expense type ID segment in the path part of a expense type ID URI
         */
        public static final int EXPENSE_TYPE_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSE_TYPES);

        /**
         * The content URI base for a expense type. Callers must append a numeric user id to this Uri to retrieve an expense type.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSE_TYPE_ID);

        /**
         * The content URI match pattern for a single expense type, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSE_TYPE_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of expense types.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.expense_types";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single expense type.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.expense_type";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the expense code (TEXT) column name.
         */
        public static final String EXP_CODE = "EXP_CODE";

        /**
         * Contains the expense key (TEXT) column name.
         */
        public static final String EXP_KEY = "EXP_KEY";

        /**
         * Contains the expense name (TEXT) column name.
         */
        public static final String EXP_NAME = "EXP_NAME";

        /**
         * Contains the form key (INTEGER) column name.
         */
        public static final String FORM_KEY = "FORM_KEY";

        /**
         * Contains the "has post amount calculation" (BOOLEAN) column name.
         */
        public static final String HAS_POST_AMT_CALC = "HAS_POST_AMT_CALC";

        /**
         * Contains the "has tax form" (BOOLEAN) column name.
         */
        public static final String HAS_TAX_FORM = "HAS_TAX_FORM";

        /**
         * Contains the "itemization unallowed expense keys" (TEXT) column name.
         */
        public static final String ITEMIZATION_UNALLOW_EXP_KEYS = "ITEMIZATION_UNALLOW_EXP_KEYS";

        /**
         * Contains the "itemization form key" (INTEGER) column name.
         */
        public static final String ITEMIZATION_FORM_KEY = "ITEMIZATION_FORM_KEY";

        /**
         * Contains the "itemization style" (TEXT) column name.
         */
        public static final String ITEMIZATION_STYLE = "ITEMIZATION_STYLE";

        /**
         * Contains the "itemization type" (TEXT) column name.
         */
        public static final String ITEMIZATION_TYPE = "ITEMIZATION_TYPE";

        /**
         * Contains the "parent expense key" (TEXT) column name.
         */
        public static final String PARENT_EXP_KEY = "PARENT_EXP_KEY";

        /**
         * Contains the "parent expense name" (TEXT) column name.
         */
        public static final String PARENT_EXP_NAME = "PARENT_EXP_NAME";

        /**
         * Contains the "supports attendees" (BOOLEAN) column name.
         */
        public static final String SUPPORTS_ATTENDEES = "SUPPORTS_ATTENDEES";

        /**
         * Contains the "vendor list key" (INTEGER) column name.
         */
        public static final String VENDOR_LIST_KEY = "VENDOR_LIST_KEY";

        /**
         * Contains the "allow edit attendee amount" (BOOLEAN) column name.
         */
        public static final String ALLOW_EDIT_ATTENDEE_AMOUNT = "ALLOW_EDIT_ATTENDEE_AMOUNT";

        /**
         * Contains the "allow edit attendee count" (BOOLEAN) column name.
         */
        public static final String ALLOW_EDIT_ATTENDEE_COUNT = "ALLOW_EDIT_ATTENDEE_COUNT";

        /**
         * Contains the "allow no shows" (BOOLEAN) column name.
         */
        public static final String ALLOW_NO_SHOWS = "ALLOW_NO_SHOWS";

        /**
         * Contains the "display add attendee on form" (BOOLEAN) column name.
         */
        public static final String DISPLAY_ADD_ATTENDEE_ON_FORM = "DISPLAY_ADD_ATTENDEE_ON_FORM";

        /**
         * Contains the "display attendee amounts" (BOOLEAN) column name.
         */
        public static final String DISPLAY_ATTENDEE_AMOUNTS = "DISPLAY_ATTENDEE_AMOUNTS";

        /**
         * Contains the "user as attended default" (BOOLEAN) column name.
         */
        public static final String USER_AS_ATTENDEE_DEFAULT = "USER_AS_ATTENDEE_DEFAULT";

        /**
         * Contains the "unallow attendee type keys" (TEXT) column name.
         */
        public static final String UNALLOW_ATN_TYPE_KEYS = "UNALLOW_ATN_TYPE_KEYS";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the system configuration office location table information.
     */
    public static class OfficeLocationColumns implements BaseColumns {

        // Prevent instantiation.
        private OfficeLocationColumns() {
        }

        /**
         * Contains the office location table name.
         */
        public static final String TABLE_NAME = "OFFICE_LOCATION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the office locations URI
         */
        private static final String PATH_OFFICE_LOCATIONS = "/office_locations";

        /**
         * Path part for the office location ID URI
         */
        private static final String PATH_OFFICE_LOCATION_ID = "/office_locations/";

        /**
         * 0-relative position of a office location ID segment in the path part of a office location ID URI
         */
        public static final int OFFICE_LOCATION_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_OFFICE_LOCATIONS);

        /**
         * The content URI base for an office location. Callers must append a numeric user id to this Uri to retrieve an office
         * location.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_OFFICE_LOCATION_ID);

        /**
         * The content URI match pattern for a single office location, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_OFFICE_LOCATION_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of office locations.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.office_locations";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single office location.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.office_location";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the address (TEXT) column name.
         */
        public static final String ADDRESS = "ADDRESS";

        /**
         * Contains the city (TEXT) column name.
         */
        public static final String CITY = "CITY";

        /**
         * Contains the country (TEXT) column name.
         */
        public static final String COUNTRY = "COUNTRY";

        /**
         * Contains the latitude (DOUBLE) column name.
         */
        public static final String LAT = "LAT";

        /**
         * Contains the longitude (DOUBLE) column name.
         */
        public static final String LON = "LON";

        /**
         * Contains the state (TEXT) column name.
         */
        public static final String STATE = "STATE";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    //
    // End System Config Information
    //

    //
    // Start User Config Information
    //

    /**
     * Contains the user configuration table information.
     */
    public static class UserConfigColumns implements BaseColumns {

        // Prevent instantiation.
        private UserConfigColumns() {
        }

        /**
         * Contains the user config table name.
         */
        public static final String TABLE_NAME = "USER_CONFIG";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the user configs URI
         */
        private static final String PATH_USER_CONFIGS = "/user_configs";

        /**
         * Path part for the user config ID URI
         */
        private static final String PATH_USER_CONFIG_ID = "/user_configs/";

        /**
         * 0-relative position of a user config ID segment in the path part of a user config ID URI
         */
        public static final int USER_CONFIG_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_USER_CONFIGS);

        /**
         * The content URI base for a user config. Callers must append a numeric user id to this Uri to retrieve a user config.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_USER_CONFIG_ID);

        /**
         * The content URI match pattern for a single user config, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_USER_CONFIG_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of user configs.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.user_configs";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single user config.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.user_config";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the column name storing server-created hash code value (TEXT) for the system configuration information.
         */
        public static final String HASH = "HASH";

        /**
         * Contains the "allowed air classes of service" (TEXT) column name.
         */
        public static final String ALLOWED_AIR_CLASSES_OF_SERVICE = "ALLOWED_AIR_CLASSES_OF_SERVICE";

        /**
         * Contains the constant value for "class of service" for 'Economy' which can be used to check the values in
         * <code>ALLOWED_AIR_CLASSES_OF_SERVICE</code>.
         */
        public static final String COS_ECONOMY = "Economy";
        /**
         * Contains the constant value for "class of service" for 'First' which can be used to check the values in
         * <code>ALLOWED_AIR_CLASSES_OF_SERVICE</code>.
         */
        public static final String COS_FIRST = "First";
        /**
         * Contains the constant value for "class of service" for 'Business' which can be used to check the values in
         * <code>ALLOWED_AIR_CLASSES_OF_SERVICE</code>.
         */
        public static final String COS_BUSINESS = "Business";
        /**
         * Contains the constant value for "class of service" for 'PremiumEconomy' which can be used to check the values in
         * <code>ALLOWED_AIR_CLASSES_OF_SERVICE</code>.
         */
        public static final String COS_PREMIUM_ECONOMY = "PremiumEconomy";
        /**
         * Contains the constant value for "class of service" for 'OneClassUpgrade' which can be used to check the values in
         * <code>ALLOWED_AIR_CLASSES_OF_SERVICE</code>.
         */
        public static final String COS_ONE_CLASS_UPGRADE = "OneClassUpgrade";

        /**
         * Contains the flags (TEXT) column name.
         */
        public static final String FLAGS = "FLAGS";

        /**
         * Contains the showGDSNameInSearchResults (TEXT) column name.
         */
        public static final String SHOW_GDS_NAME_IN_SEARCH_RESULTS = "SHOW_GDS_NAME_IN_SEARCH_RESULTS";

        /**
         * Contains the user id (TEXT) column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the user configuration car type table information.
     */
    public static class CarTypeColumns implements BaseColumns {

        // Prevent instantiation.
        private CarTypeColumns() {
        }

        /**
         * Contains the car type table name.
         */
        public static final String TABLE_NAME = "CAR_TYPE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the car types URI
         */
        private static final String PATH_CAR_TYPES = "/car_types";

        /**
         * Path part for the car type ID URI
         */
        private static final String PATH_CAR_TYPE_ID = "/car_types/";

        /**
         * 0-relative position of a car type ID segment in the path part of a car type ID URI
         */
        public static final int CAR_TYPE_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CAR_TYPES);

        /**
         * The content URI base for a car type. Callers must append a numeric user id to this Uri to retrieve a car type.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_CAR_TYPE_ID);

        /**
         * The content URI match pattern for a single car type, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_CAR_TYPE_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of car types.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.car_types";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single car type.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.car_type";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the description (TEXT) column name.
         */
        public static final String DESCRIPTION = "DESCRIPTION";

        /**
         * Contains the code (TEXT) column name.
         */
        public static final String CODE = "CODE";

        /**
         * Contains the "is default" (BOOLEAN) column name.
         */
        public static final String IS_DEFAULT = "IS_DEFAULT";

        /**
         * Contains the user id (TEXT) column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the user configuration attendee column definition table information.
     */
    public static class AttendeeColumnDefinitionColumns implements BaseColumns {

        // Prevent instantiation.
        private AttendeeColumnDefinitionColumns() {
        }

        /**
         * Contains the car type table name.
         */
        public static final String TABLE_NAME = "ATTENDEE_COLUMN_DEFINITION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the attendee column definitions URI
         */
        private static final String PATH_ATTENDEE_COLUMN_DEFINITIONS = "/attendee_column_definitions";

        /**
         * Path part for the attendee column definition ID URI
         */
        private static final String PATH_ATTENDEE_COLUMN_DEFINITION_ID = "/attendee_column_definitions/";

        /**
         * 0-relative position of a attendee column definition ID segment in the path part of a attendee column definition ID URI
         */
        public static final int ATTENDEE_COLUMN_DEFINITION_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_ATTENDEE_COLUMN_DEFINITIONS);

        /**
         * The content URI base for an attendee column definition. Callers must append a numeric user id to this Uri to retrieve
         * an attendee column definition.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri
                .parse(SCHEME + AUTHORITY + PATH_ATTENDEE_COLUMN_DEFINITION_ID);

        /**
         * The content URI match pattern for a single attendee column definition, specified by its ID. Use this to match incoming
         * URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY
                + PATH_ATTENDEE_COLUMN_DEFINITION_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of attendee column definitions.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.attendee_column_definitions";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single attendee column definition.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.attendee_column_definition";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the id (TEXT) column name.
         */
        public static final String ID = "ID";

        /**
         * Contains the label (TEXT) column name.
         */
        public static final String LABEL = "LABEL";

        /**
         * Contains the "data type" (TEXT) column name.
         */
        public static final String DATA_TYPE = "DATA_TYPE";

        /**
         * Contains the constant value for "variable length char" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_VARCHAR = "VARCHAR";
        /**
         * Contains the constant value for "money" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_MONEY = "MONEY";
        /**
         * Contains the constant value for "numeric" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_NUMERIC = "NUMERIC";
        /**
         * Contains the constant value for "char" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_CHAR = "CHAR";
        /**
         * Contains the constant value for "integer" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_INTEGER = "INTEGER";
        /**
         * Contains the constant value for "timestamp" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_TIMESTAMP = "TIMESTAMP";
        /**
         * Contains the constant value for "expense type" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_EXPENSE_TYPE = "EXPTYPE";
        /**
         * Contains the constant value for "boolean" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_BOOLEAN = "BOOLEANCHAR";
        /**
         * Contains the constant value for "connected list" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_CONNECTED_LIST = "MLIST";
        /**
         * Contains the constant value for "list" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_LIST = "LIST";
        /**
         * Contains the constant value for "currency" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_CURRENCY = "CURRENCY";
        /**
         * Contains the constant value for "location" that can be used to check the value of <code>DATA_TYPE</code>.
         */
        public static final String DATA_LOCATION = "LOCATION";

        /**
         * Contains the "control type" (TEXT) column name.
         */
        public static final String CTRL_TYPE = "CTRL_TYPE";

        /**
         * Contains the constant value for a "hidden" control type that can be used to check the value of <code>CTRL_TYPE</code>.
         */
        public static final String CTRL_HIDDEN = "hidden";
        /**
         * Contains the constant value for a "edit" control type that can be used to check the value of <code>CTRL_TYPE</code>.
         */
        public static final String CTRL_EDIT = "edit";
        /**
         * Contains the constant value for a "checkbox" control type that can be used to check the value of <code>CTRL_TYPE</code>
         * .
         */
        public static final String CTRL_CHECKBOX = "checkbox";
        /**
         * Contains the constant value for a "pick list" control type that can be used to check the value of
         * <code>CTRL_TYPE</code>.
         */
        public static final String CTRL_PICK_LIST = "picklist";
        /**
         * Contains the constant value for a "list edit" control type that can be used to check the value of
         * <code>CTRL_TYPE</code>.
         */
        public static final String CTRL_LIST_EDIT = "list_edit";
        /**
         * Contains the constant value for a "date edit" control type that can be used to check the value of
         * <code>CTRL_TYPE</code>.
         */
        public static final String CTRL_DATE_EDIT = "date_edit";
        /**
         * Contains the constant value for a "static text" control type that can be used to check the value of
         * <code>CTRL_TYPE</code>.
         */
        public static final String CTRL_STATIC = "static";
        /**
         * Contains the constant value for a "text area" control type that can be used to check the value of
         * <code>CTRL_TYPE</code>.
         */
        public static final String CTRL_TEXT_AREA = "textarea";

        /**
         * Contains the access (TEXT) column name.
         */
        public static final String ACCESS = "ACCESS";

        /**
         * Contains the constant value for "read/write" that can be used to check the value of <code>ACCESS</code>.
         */
        public static final String ACCESS_READ_WRITE = "RW";
        /**
         * Contains the constant value for "read only" that can be used to check the value of <code>ACCESS</code>.
         */
        public static final String ACCESS_READ_ONLY = "RO";
        /**
         * Contains the constant value for "hidden" that can be used to check the value of <code>ACCESS</code>.
         */
        public static final String ACCESS_HIDDEN = "HD";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the user configuration attendee type table information.
     */
    public static class AttendeeTypeColumns implements BaseColumns {

        // Prevent instantiation.
        private AttendeeTypeColumns() {
        }

        /**
         * Contains the attendee type table name.
         */
        public static final String TABLE_NAME = "ATTENDEE_TYPE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the attendee types URI
         */
        private static final String PATH_ATTENDEE_TYPES = "/attendee_types";

        /**
         * Path part for the attendee type ID URI
         */
        private static final String PATH_ATTENDEE_TYPE_ID = "/attendee_types/";

        /**
         * 0-relative position of an attendee type ID segment in the path part of an attendee type ID URI
         */
        public static final int ATTENDEE_TYPE_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_ATTENDEE_TYPES);

        /**
         * The content URI base for an attendee type. Callers must append a numeric user id to this Uri to retrieve an attendee
         * type.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_ATTENDEE_TYPE_ID);

        /**
         * The content URI match pattern for a single attendee type, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_ATTENDEE_TYPE_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of attendee types.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.attendee_types";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single attendee type.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.attendee_type";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the "allow edit attendee count" (BOOLEAN) column name.
         */
        public static final String ALLOW_EDIT_ATN_COUNT = "ALLOW_EDIT_ATN_COUNT";

        /**
         * Contains the "attendee type code" (TEXT) column name.
         */
        public static final String ATN_TYPE_CODE = "ATN_TYPE_CODE";

        /**
         * Contains the "attendee type key" (INTEGER) column name.
         */
        public static final String ATN_TYPE_KEY = "ATN_TYPE_KEY";

        /**
         * Contains the "attendee type name" (TEXT) column name.
         */
        public static final String ATN_TYPE_NAME = "ATN_TYPE_NAME";

        /**
         * Contains the "form key" (INTEGER) column name.
         */
        public static final String FORM_KEY = "FORM_KEY";

        /**
         * Contains the "is external" (BOOLEAN) column name.
         */
        public static final String IS_EXTERNAL = "IS_EXTERNAL";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the user configuration currency table information.
     */
    public static class CurrencyColumns implements BaseColumns {

        // Prevent instantiation.
        private CurrencyColumns() {
        }

        /**
         * Contains the car type table name.
         */
        public static final String TABLE_NAME = "CURRENCY";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the currencies URI
         */
        private static final String PATH_CURRENCIES = "/currencies";

        /**
         * Path part for the currency ID URI
         */
        private static final String PATH_CURRENCY_ID = "/currencies/";

        /**
         * 0-relative position of a currency ID segment in the path part of a currency ID URI
         */
        public static final int CURRENCY_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CURRENCIES);

        /**
         * The content URI base for a currency. Callers must append a numeric user id to this Uri to retrieve a currency.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_CURRENCY_ID);

        /**
         * The content URI match pattern for a single currency, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_CURRENCY_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of currencies.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.currencies";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single currency.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.currency";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the "currency code" (TEXT) column name.
         */
        public static final String CRN_CODE = "CRN_CODE";

        /**
         * Contains the "currency name" (TEXT) column name.
         */
        public static final String CRN_NAME = "CRN_NAME";

        /**
         * Contains the "decimal digits" (INTEGER) column name.
         */
        public static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";

        /**
         * Contains the "is reimbursement" (BOOLEAN) column name.
         */
        public static final String IS_REIMBURSEMENT = "IS_REIMBURSEMENT";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the user configuration expense confirmation table information.
     */
    public static class ExpenseConfirmationColumns implements BaseColumns {

        // Prevent instantiation.
        private ExpenseConfirmationColumns() {
        }

        /**
         * Contains the expense confirmation table name.
         */
        public static final String TABLE_NAME = "EXPENSE_CONFIRMATION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the expense confirmations URI
         */
        private static final String PATH_EXPENSE_CONFIRMATIONS = "/expense_confirmations";

        /**
         * Path part for the expense confirmation ID URI
         */
        private static final String PATH_EXPENSE_CONFIRMATION_ID = "/expense_confirmations/";

        /**
         * 0-relative position of a expense confirmation ID segment in the path part of a expense confirmation ID URI
         */
        public static final int EXPENSE_CONFIRMATION_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSE_CONFIRMATIONS);

        /**
         * The content URI base for an expense confirmation. Callers must append a numeric user id to this Uri to retrieve an
         * expense confirmation.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSE_CONFIRMATION_ID);

        /**
         * The content URI match pattern for a single expense confirmation, specified by its ID. Use this to match incoming URIs
         * or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSE_CONFIRMATION_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of expense confirmations.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.expense_confirmations";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single expense confirmation.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.expense_confirmation";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the "confirmation key" (TEXT) column name.
         */
        public static final String CONFIRMATION_KEY = "CONFIRMATION_KEY";

        /**
         * Contains the text (TEXT) column name.
         */
        public static final String TEXT = "CONFIRMATION_TEXT";

        /**
         * Contains the title (TEXT) column name.
         */
        public static final String TITLE = "TITLE";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the user configuration policy table information.
     */
    public static class PolicyColumns implements BaseColumns {

        // Prevent instantiation.
        private PolicyColumns() {
        }

        /**
         * Contains the policy table name.
         */
        public static final String TABLE_NAME = "POLICY";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the policies URI
         */
        private static final String PATH_POLICIES = "/policies";

        /**
         * Path part for the policy ID URI
         */
        private static final String PATH_POLICY_ID = "/policies/";

        /**
         * 0-relative position of a policy ID segment in the path part of a policy ID URI
         */
        public static final int POLICY_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_POLICIES);

        /**
         * The content URI base for a policy. Callers must append a numeric user id to this Uri to retrieve a policy.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_POLICY_ID);

        /**
         * The content URI match pattern for a single policy, specified by its ID. Use this to match incoming URIs or to construct
         * an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_POLICY_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of policies.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.policies";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single policy.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.policy";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the "policy key" (TEXT) column name.
         */
        public static final String POL_KEY = "POL_KEY";

        /**
         * Contains the "supports imaging" (BOOLEAN) column name.
         */
        public static final String SUPPORTS_IMAGING = "SUPPORTS_IMAGING";

        /**
         * Contains the "approval confirmation key" (TEXT) column name.
         */
        public static final String APPROVAL_CONFIRMATION_KEY = "APPROVAL_CONFIRMATION_KEY";

        /**
         * Contains the "submit confirmation key" (TEXT) colum name.
         */
        public static final String SUBMIT_CONFIRMATION_KEY = "SUBMIT_CONFIRMATION_KEY";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the user configuration Yodlee payment type table information.
     */
    public static class YodleePaymentTypeColumns implements BaseColumns {

        // Prevent instantiation.
        private YodleePaymentTypeColumns() {
        }

        /**
         * Contains the Yodlee payment type table name.
         */
        public static final String TABLE_NAME = "YODLEE_PAYMENT_TYPE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Yodlee payment types URI
         */
        private static final String PATH_YODLEE_PAYMENT_TYPES = "/yodlee_payment_types";

        /**
         * Path part for the Yodlee payment type ID URI
         */
        private static final String PATH_YODLEE_PAYMENT_TYPE_ID = "/yodlee_payment_types/";

        /**
         * 0-relative position of a Yodlee payment type ID segment in the path part of a Yodlee payment type ID URI
         */
        public static final int YODLEE_PAYMENT_TYPE_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_YODLEE_PAYMENT_TYPES);

        /**
         * The content URI base for a Yodlee payment type. Callers must append a numeric user id to this Uri to retrieve a Yodlee
         * payment type.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_YODLEE_PAYMENT_TYPE_ID);

        /**
         * The content URI match pattern for a single Yodlee payment type, specified by its ID. Use this to match incoming URIs or
         * to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_YODLEE_PAYMENT_TYPE_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Yodlee payment types.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.yodlee_payment_types";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Yodlee payment type.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.yodlee_payment_type";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the key (TEXT) column name.
         */
        public static final String KEY = "KEY";

        /**
         * Contains the text (TEXT) column name.
         */
        public static final String TEXT = "POLICY_TEXT";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the user configuration credit card table information.
     */
    public static class CreditCardColumns implements BaseColumns {

        // Prevent instantiation.
        private CreditCardColumns() {
        }

        /**
         * Contains the credit card table name.
         */
        public static final String TABLE_NAME = "CREDIT_CARD";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the credit cards URI
         */
        private static final String PATH_CREDIT_CARDS = "/credit_cards";

        /**
         * Path part for the credit card ID URI
         */
        private static final String PATH_CREDIT_CARD_ID = "/credit_cards/";

        /**
         * 0-relative position of a credit card ID segment in the path part of a credit card ID URI
         */
        public static final int CREDIT_CARD_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CREDIT_CARDS);

        /**
         * The content URI base for a credit card. Callers must append a numeric user id to this Uri to retrieve a credit card.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_CREDIT_CARD_ID);

        /**
         * The content URI match pattern for a single credit card, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_CREDIT_CARD_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of credit cards.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.credit_cards";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single credit card.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.credit_card";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the name (TEXT) column name.
         */
        public static final String NAME = "NAME";

        /**
         * Contains the type (TEXT) column name.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the "masked number" (TEXT) column name.
         */
        public static final String MASKED_NUMBER = "MASKED_NUMBER";

        /**
         * Contains the "credit card id" (TEXT) column name.
         */
        public static final String CC_ID = "CC_ID";

        /**
         * Contains the "for car" value that can be used with the <code>DEFAULT_FOR</code> and <code>ALLOW_FOR</code> column
         * values.
         */
        public static final String FOR_CAR = "Car";
        /**
         * Contains the "for hotel" value that can be used with the <code>DEFAULT_FOR</code> and <code>ALLOW_FOR</code> column
         * values.
         */
        public static final String FOR_HOTEL = "Hotel";

        /**
         * Contains the "for air" value that can be used with the <code>DEFAULT_FOR</code> and <code>ALLOW_FOR</code> column
         * values.
         */
        public static final String FOR_AIR = "Air";

        /**
         * Contains the "for rail" value that can be used with the <code>DEFAULT_FOR</code> and <code>ALLOW_FOR</code> column
         * values.
         */
        public static final String FOR_RAIL = "Rail";

        /**
         * Contains the "for taxi" value that can be used with the <code>DEFAULT_FOR</code> and <code>ALLOW_FOR</code> column
         * values.
         */
        public static final String FOR_TAXI = "Taxi";

        /**
         * Contains the "for limo" value that can be used with the <code>DEFAULT_FOR</code> and <code>ALLOW_FOR</code> column
         * values.
         */
        public static final String FOR_LIMO = "Limo";

        /**
         * Contains the "default for" (TEXT) column name.
         */
        public static final String DEFAULT_FOR = "DEFAULT_FOR";

        /**
         * Contains the "allow for" (TEXT) column name.
         */
        public static final String ALLOW_FOR = "ALLOW_FOR";

        /**
         * Contains the "last four" (TEXT) column name.
         */
        public static final String LAST_FOUR = "LAST_FOUR";

        /**
         * Contains the "is default" (BOOLEAN) column name.
         */
        public static final String IS_DEFAULT = "IS_DEFAULT";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Contains the user configuration affinity program table information.
     */
    public static class AffinityProgramColumns implements BaseColumns {

        // Prevent instantiation.
        private AffinityProgramColumns() {
        }

        /**
         * Contains the affinity program table name.
         */
        public static final String TABLE_NAME = "AFFINITY_PROGRAM";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the affinity programs URI
         */
        private static final String PATH_AFFINITY_PROGRAMS = "/affinity_programs";

        /**
         * Path part for the affinity program ID URI
         */
        private static final String PATH_AFFINITY_PROGRAM_ID = "/affinity_programs/";

        /**
         * 0-relative position of a affinity program ID segment in the path part of an affinity program ID URI
         */
        public static final int AFFINITY_PROGRAM_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_AFFINITY_PROGRAMS);

        /**
         * The content URI base for an affinity program. Callers must append a numeric user id to this Uri to retrieve an affinity
         * program.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_AFFINITY_PROGRAM_ID);

        /**
         * The content URI match pattern for a single affinity program, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri
                .parse(SCHEME + AUTHORITY + PATH_AFFINITY_PROGRAM_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of affinity programs.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.affinity_programs";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single affinity program.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.affinity_program";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the "account number" (TEXT) column name.
         */
        public static final String ACCOUNT_NUMBER = "ACCOUNT_NUMBER";

        /**
         * Contains the description (TEXT) column name.
         */
        public static final String DESCRIPTION = "DESCRIPTION";

        /**
         * Contains the vendor (TEXT) column name.
         */
        public static final String VENDOR = "VENDOR";

        /**
         * Contains the "vendor abbreviation" (TEXT) column name.
         */
        public static final String VENDOR_ABBREV = "VENDOR_ABBREV";

        /**
         * Contains the "program name" (TEXT) column name.
         */
        public static final String PROGRAM_NAME = "PROGRAM_NAME";

        /**
         * Contains the constant identify the "air program type". This value can be used to check the value stored in the column
         * <code>PROGRAM_TYPE</code>.
         */
        public static final String TYPE_AIR = "A";
        /**
         * Contains the constant identify the "hotel program type". This value can be used to check the value stored in the column
         * <code>PROGRAM_TYPE</code>.
         */
        public static final String TYPE_HOTEL = "H";
        /**
         * Contains the constant identify the "car program type". This value can be used to check the value stored in the column
         * <code>PROGRAM_TYPE</code>.
         */
        public static final String TYPE_CAR = "C";

        /**
         * Contains the "program type" (TEXT) column name.
         */
        public static final String PROGRAM_TYPE = "PROGRAM_TYPE";

        /**
         * Contains the "program id" (TEXT) column name.
         */
        public static final String PROGRAM_ID = "PROGRAM_ID";

        /**
         * Contains the "is default" (BOOLEAN) column name.
         */
        public static final String IS_DEFAULT = "IS_DEFAULT";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    //
    // End User Config Information
    //

    //
    // Start Client Data Support
    //

    /**
     * Contains client data table information.
     */
    public static class ClientDataColumns implements BaseColumns {

        // Prevent instantiation.
        private ClientDataColumns() {
        }

        /**
         * Contains the client data table name.
         */
        public static final String TABLE_NAME = "CLIENT_DATA";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the client data URI
         */
        private static final String PATH_CLIENT_DATA = "/client_data";

        /**
         * Path part for the client data ID URI
         */
        private static final String PATH_CLIENT_DATA_ID = "/client_data/";

        /**
         * 0-relative position of a client data ID segment in the path part of a client data ID URI
         */
        public static final int CLIENT_DATA_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CLIENT_DATA);

        /**
         * The content URI base for client data. Callers must append a numeric user id to this Uri to retrieve a specific piece of
         * client data.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_CLIENT_DATA_ID);

        /**
         * The content URI match pattern for a single client data, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_CLIENT_DATA_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of client data.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.client_data";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single client data.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.client_data";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the client data "key" (TEXT) column name.
         */
        public static final String KEY = "KEY";

        /**
         * Contains the client data "value text" (TEXT) column name.
         */
        public static final String VALUE_TEXT = "VALUE_TEXT";

        /**
         * Contains the client data "value blob" (BLOB) column name.
         */
        public static final String VALUE_BLOB = "VALUE_BLOB";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    //
    // End Client Data Support
    //

    //
    // Start Travel Points Config Support
    //

    /**
     * Contains travel points config table information.
     */
    public static class TravelPointsConfigColumns implements BaseColumns {

        // Prevent instantiation.
        private TravelPointsConfigColumns() {
        }

        /**
         * Contains the travel points config table name.
         */
        public static final String TABLE_NAME = "TRAVEL_POINTS_CONFIG";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the travel points config URI
         */
        private static final String PATH_TRAVEL_POINTS_CONFIG = "/travel_points_config";

        /**
         * Path part for the travel points config ID URI
         */
        private static final String PATH_TRAVEL_POINTS_CONFIG_ID = "/travel_points_config/";

        /**
         * 0-relative position of a travel points config ID segment in the path part of a travel points config ID URI
         */
        public static final int TRAVEL_POINTS_CONFIG_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TRAVEL_POINTS_CONFIG);

        /**
         * The content URI base for travel points config. Callers must append a numeric user id to this Uri to retrieve a specific
         * piece of client data.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_TRAVEL_POINTS_CONFIG_ID);

        /**
         * The content URI match pattern for a single travel points config, specified by its ID. Use this to match incoming URIs
         * or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_TRAVEL_POINTS_CONFIG_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of travel points config.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.travel_points_config";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single travel points config.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.travel_points_config";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Table column names.

        /**
         * Contains the travel points config "AirTravelPointsEnabled" (AIR_TRAVEL_POINTS_ENABLED) column name.
         */
        public static final String AIR_TRAVEL_POINTS_ENABLED = "AIR_TRAVEL_POINTS_ENABLED";

        /**
         * Contains the travel points config "HotelTravelPointsEnabled" (HOTEL_TRAVEL_POINTS_ENABLED) column name.
         */
        public static final String HOTEL_TRAVEL_POINTS_ENABLED = "HOTEL_TRAVEL_POINTS_ENABLED";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    //
    // End Travel Points Config Support
    //

}
