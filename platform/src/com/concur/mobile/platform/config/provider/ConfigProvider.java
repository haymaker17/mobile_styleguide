/**
 * 
 */
package com.concur.mobile.platform.config.provider;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.UriMatcher;
import android.util.SparseArray;

import com.concur.mobile.platform.provider.EncryptedSQLiteOpenHelper;
import com.concur.mobile.platform.provider.PlatformContentProvider;
import com.concur.mobile.platform.provider.PlatformSQLiteOpenHelper;
import com.concur.mobile.platform.provider.UriMatcherInfo;

/**
 * An extension of <code>PlatformContentProvider</code> providing configuration content.
 * 
 * @author andrewk
 */
public class ConfigProvider extends PlatformContentProvider {

    // Contains a static reference to the last instantiated <code>ConfigProvider</code>.
    private static ConfigProvider provider;

    // Login response.
    private static final int SESSIONS = 1;
    private static final int SESSION_ID = 2;
    private static final int USERS = 3;
    private static final int USER_ID = 4;
    private static final int SITE_SETTINGS = 5;
    private static final int SITE_SETTING_ID = 6;

    // System config.
    private static final int SYSTEM_CONFIGS = 7;
    private static final int SYSTEM_CONFIG_ID = 8;
    private static final int REASON_CODES = 9;
    private static final int REASON_CODE_ID = 10;
    private static final int EXPENSE_TYPES = 11;
    private static final int EXPENSE_TYPE_ID = 12;
    private static final int OFFICE_LOCATIONS = 13;
    private static final int OFFICE_LOCATION_ID = 14;

    // User config.
    private static final int USER_CONFIGS = 15;
    private static final int USER_CONFIG_ID = 16;

    private static final int CAR_TYPES = 17;
    private static final int CAR_TYPE_ID = 18;

    private static final int ATTENDEE_COLUMN_DEFINITIONS = 19;
    private static final int ATTENDEE_COLUMN_DEFINITION_ID = 20;

    private static final int ATTENDEE_TYPES = 21;
    private static final int ATTENDEE_TYPE_ID = 22;

    private static final int CURRENCY_TYPES = 23;
    private static final int CURRENCY_TYPE_ID = 24;

    private static final int EXPENSE_CONFIRMATIONS = 25;
    private static final int EXPENSE_CONFIRMATION_ID = 26;

    private static final int POLICIES = 27;
    private static final int POLICY_ID = 28;

    private static final int YODLEE_PAYMENT_TYPES = 29;
    private static final int YODLEE_PAYMENT_TYPE_ID = 30;

    private static final int CREDIT_CARDS = 31;
    private static final int CREDIT_CARD_ID = 32;

    private static final int AFFINITY_PROGRAMS = 33;
    private static final int AFFINITY_PROGRAM_ID = 34;

    private static final int CLIENT_DATA = 35;
    private static final int CLIENT_DATA_ID = 36;

    private static final int TRAVEL_POINTS_CONFIG = 37;
    private static final int TRAVEL_POINTS_CONFIG_ID = 38;

    private static final int PERMISSIONS = 39;
    private static final int PERMISSIONS_ID = 40;

    // Contains the session projection map.
    private static Map<String, String> sessionProjectionMap;

    // Contains the user projection map.
    private static Map<String, String> userProjectionMap;

    // Contains the siteSetting projection map.
    private static Map<String, String> siteSettingProjectionMap;

    // Contains the system config projection map.
    private static Map<String, String> systemConfigProjectionMap;

    // Contains the reason code projection map.
    private static Map<String, String> reasonCodeProjectionMap;

    // Contains the expense type projection map.
    private static Map<String, String> expenseTypeProjectionMap;

    // Contains the office location projection map.
    private static Map<String, String> officeLocationProjectionMap;

    // Contains the user config projection map.
    private static Map<String, String> userConfigProjectionMap;

    // Contains the car type projection map.
    private static Map<String, String> carTypeProjectionMap;

    // Contains the attendee column definition projection map.
    private static Map<String, String> attendeeColumnDefinitionProjectionMap;

    // Contains the attendee type projection map.
    private static Map<String, String> attendeeTypeProjectionMap;

    // Contains the currency projection map.
    private static Map<String, String> currencyProjectionMap;

    // Contains the expense confirmation projection map.
    private static Map<String, String> expenseConfirmationProjectionMap;

    // Contains the expense policy projection map.
    private static Map<String, String> policyProjectionMap;

    // Contains the Yodlee payment type projection map.
    private static Map<String, String> yodleePaymentTypeProjectionMap;

    // Contains the travel points config projection map.
    private static Map<String, String> travelPointsConfigProjectionMap;

    // Contains the credit card projection map.
    private static Map<String, String> creditCardProjectionMap;

    // Contains the affinity program projection map.
    private static Map<String, String> affinityProgramProjectionMap;

    // Contains the client data projection map.
    private static Map<String, String> clientDataProjectionMap;

    // Contains the permissions projection map.
    private static Map<String, String> permissionsProjectionMap;

    @Override
    public boolean onCreate() {
        boolean retVal = super.onCreate();

        // Set the static reference.
        provider = this;

        return retVal;
    }

    /**
     * Gets the current instance of <code>ConfigProvider</code>.
     * 
     * @return returns the current instance of <code>ConfigProvider</code>
     */
    public static ConfigProvider getConfigProvider() {
        return provider;
    }

    @Override
    protected UriMatcher initUriMatcher() {

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Config.AUTHORITY, "sessions", SESSIONS);
        matcher.addURI(Config.AUTHORITY, "sessions/#", SESSION_ID);
        matcher.addURI(Config.AUTHORITY, "users", USERS);
        matcher.addURI(Config.AUTHORITY, "users/#", USER_ID);
        matcher.addURI(Config.AUTHORITY, "site_settings", SITE_SETTINGS);
        matcher.addURI(Config.AUTHORITY, "site_settings/#", SITE_SETTING_ID);
        matcher.addURI(Config.AUTHORITY, "system_configs", SYSTEM_CONFIGS);
        matcher.addURI(Config.AUTHORITY, "system_configs/#", SYSTEM_CONFIG_ID);
        matcher.addURI(Config.AUTHORITY, "reason_codes", REASON_CODES);
        matcher.addURI(Config.AUTHORITY, "reason_codes/#", REASON_CODE_ID);
        matcher.addURI(Config.AUTHORITY, "expense_types", EXPENSE_TYPES);
        matcher.addURI(Config.AUTHORITY, "expense_types/#", EXPENSE_TYPE_ID);
        matcher.addURI(Config.AUTHORITY, "office_locations", OFFICE_LOCATIONS);
        matcher.addURI(Config.AUTHORITY, "office_locations/#", OFFICE_LOCATION_ID);
        matcher.addURI(Config.AUTHORITY, "user_configs", USER_CONFIGS);
        matcher.addURI(Config.AUTHORITY, "user_configs/#", USER_CONFIG_ID);
        matcher.addURI(Config.AUTHORITY, "car_types", CAR_TYPES);
        matcher.addURI(Config.AUTHORITY, "car_types/#", CAR_TYPE_ID);
        matcher.addURI(Config.AUTHORITY, "attendee_column_definitions", ATTENDEE_COLUMN_DEFINITIONS);
        matcher.addURI(Config.AUTHORITY, "attendee_column_definitions/#", ATTENDEE_COLUMN_DEFINITION_ID);
        matcher.addURI(Config.AUTHORITY, "attendee_types", ATTENDEE_TYPES);
        matcher.addURI(Config.AUTHORITY, "attendee_types/#", ATTENDEE_TYPE_ID);
        matcher.addURI(Config.AUTHORITY, "currencies", CURRENCY_TYPES);
        matcher.addURI(Config.AUTHORITY, "currencies/#", CURRENCY_TYPE_ID);
        matcher.addURI(Config.AUTHORITY, "expense_confirmations", EXPENSE_CONFIRMATIONS);
        matcher.addURI(Config.AUTHORITY, "expense_confirmations/#", EXPENSE_CONFIRMATION_ID);
        matcher.addURI(Config.AUTHORITY, "policies", POLICIES);
        matcher.addURI(Config.AUTHORITY, "policies/#", POLICY_ID);
        matcher.addURI(Config.AUTHORITY, "yodlee_payment_types", YODLEE_PAYMENT_TYPES);
        matcher.addURI(Config.AUTHORITY, "yodlee_payment_types/#", YODLEE_PAYMENT_TYPE_ID);
        matcher.addURI(Config.AUTHORITY, "travel_points_config", TRAVEL_POINTS_CONFIG);
        matcher.addURI(Config.AUTHORITY, "travel_points_config/#", TRAVEL_POINTS_CONFIG_ID);
        matcher.addURI(Config.AUTHORITY, "credit_cards", CREDIT_CARDS);
        matcher.addURI(Config.AUTHORITY, "credit_cards/#", CREDIT_CARD_ID);
        matcher.addURI(Config.AUTHORITY, "affinity_programs", AFFINITY_PROGRAMS);
        matcher.addURI(Config.AUTHORITY, "affinity_programs/#", AFFINITY_PROGRAM_ID);
        matcher.addURI(Config.AUTHORITY, "client_data", CLIENT_DATA);
        matcher.addURI(Config.AUTHORITY, "client_data/#", CLIENT_DATA_ID);
        matcher.addURI(Config.AUTHORITY, "permissions", PERMISSIONS);
        matcher.addURI(Config.AUTHORITY, "permissions/#", PERMISSIONS_ID);

        return matcher;
    }

    @Override
    protected void initProjectionMaps() {
        /*
         * Create and initialize the session project map.
         */
        sessionProjectionMap = new HashMap<String, String>();
        sessionProjectionMap.put(Config.SessionColumns._ID, Config.SessionColumns._ID);
        sessionProjectionMap.put(Config.SessionColumns._COUNT, Config.SessionColumns._COUNT);
        sessionProjectionMap.put(Config.SessionColumns.ACCESS_TOKEN_KEY, Config.SessionColumns.ACCESS_TOKEN_KEY);
        sessionProjectionMap.put(Config.SessionColumns.AUTHENTICATION_TYPE, Config.SessionColumns.AUTHENTICATION_TYPE);
        sessionProjectionMap.put(Config.SessionColumns.SESSION_ID, Config.SessionColumns.SESSION_ID);
        sessionProjectionMap.put(Config.SessionColumns.SESSION_TIME_OUT, Config.SessionColumns.SESSION_TIME_OUT);
        sessionProjectionMap.put(Config.SessionColumns.SESSION_EXPIRATION_TIME,
                Config.SessionColumns.SESSION_EXPIRATION_TIME);
        sessionProjectionMap.put(Config.SessionColumns.LOGIN_ID, Config.SessionColumns.LOGIN_ID);
        sessionProjectionMap.put(Config.SessionColumns.SERVER_URL, Config.SessionColumns.SERVER_URL);
        sessionProjectionMap.put(Config.SessionColumns.SIGN_IN_METHOD, Config.SessionColumns.SIGN_IN_METHOD);
        sessionProjectionMap.put(Config.SessionColumns.SSO_URL, Config.SessionColumns.SSO_URL);
        sessionProjectionMap.put(Config.SessionColumns.EMAIL, Config.SessionColumns.EMAIL);
        sessionProjectionMap.put(Config.SessionColumns.USER_ID, Config.SessionColumns.USER_ID);

        /*
         * Create and initialize the user project map.
         */
        userProjectionMap = new HashMap<String, String>();
        userProjectionMap.put(Config.UserColumns._ID, Config.UserColumns._ID);
        userProjectionMap.put(Config.UserColumns._COUNT, Config.UserColumns._COUNT);
        userProjectionMap.put(Config.UserColumns.ENTITY_TYPE, Config.UserColumns.ENTITY_TYPE);
        userProjectionMap.put(Config.UserColumns.EXPENSE_COUNTRY_CODE, Config.UserColumns.EXPENSE_COUNTRY_CODE);
        userProjectionMap.put(Config.UserColumns.HAS_REQUIRED_CUSTOM_FIELDS,
                Config.UserColumns.HAS_REQUIRED_CUSTOM_FIELDS);
        userProjectionMap.put(Config.UserColumns.PIN_EXPIRATION_DATE, Config.UserColumns.PIN_EXPIRATION_DATE);
        userProjectionMap.put(Config.UserColumns.PRODUCT_OFFERING, Config.UserColumns.PRODUCT_OFFERING);
        userProjectionMap.put(Config.UserColumns.PROFILE_STATUS, Config.UserColumns.PROFILE_STATUS);
        userProjectionMap.put(Config.UserColumns.ROLES_MOBILE, Config.UserColumns.ROLES_MOBILE);
        userProjectionMap.put(Config.UserColumns.CONTACT_COMPANY_NAME, Config.UserColumns.CONTACT_COMPANY_NAME);
        userProjectionMap.put(Config.UserColumns.CONTACT_EMAIL, Config.UserColumns.CONTACT_EMAIL);
        userProjectionMap.put(Config.UserColumns.CONTACT_FIRST_NAME, Config.UserColumns.CONTACT_FIRST_NAME);
        userProjectionMap.put(Config.UserColumns.CONTACT_LAST_NAME, Config.UserColumns.CONTACT_LAST_NAME);
        userProjectionMap.put(Config.UserColumns.CONTACT_MIDDLE_INITIAL, Config.UserColumns.CONTACT_MIDDLE_INITIAL);
        userProjectionMap.put(Config.UserColumns.USER_CURRENCY_CODE, Config.UserColumns.USER_CURRENCY_CODE);
        userProjectionMap.put(Config.UserColumns.USER_ID, Config.UserColumns.USER_ID);

        /*
         * Create and initialize the site setting project map.
         */
        siteSettingProjectionMap = new HashMap<String, String>();
        siteSettingProjectionMap.put(Config.SiteSettingColumns._ID, Config.SiteSettingColumns._ID);
        siteSettingProjectionMap.put(Config.SiteSettingColumns._COUNT, Config.SiteSettingColumns._COUNT);
        siteSettingProjectionMap.put(Config.SiteSettingColumns.NAME, Config.SiteSettingColumns.NAME);
        siteSettingProjectionMap.put(Config.SiteSettingColumns.TYPE, Config.SiteSettingColumns.TYPE);
        siteSettingProjectionMap.put(Config.SiteSettingColumns.VALUE, Config.SiteSettingColumns.VALUE);
        siteSettingProjectionMap.put(Config.SiteSettingColumns.USER_ID, Config.SiteSettingColumns.USER_ID);

        /*
         * Create and initialize the system config project map.
         */
        systemConfigProjectionMap = new HashMap<String, String>();
        systemConfigProjectionMap.put(Config.SystemConfigColumns._ID, Config.SystemConfigColumns._ID);
        systemConfigProjectionMap.put(Config.SystemConfigColumns._COUNT, Config.SystemConfigColumns._COUNT);
        systemConfigProjectionMap.put(Config.SystemConfigColumns.HASH, Config.SystemConfigColumns.HASH);
        systemConfigProjectionMap.put(Config.SystemConfigColumns.REFUND_INFO_CHECKBOX_DEFAULT,
                Config.SystemConfigColumns.REFUND_INFO_CHECKBOX_DEFAULT);
        systemConfigProjectionMap.put(Config.SystemConfigColumns.REFUND_INFO_MESSAGE,
                Config.SystemConfigColumns.REFUND_INFO_MESSAGE);
        systemConfigProjectionMap.put(Config.SystemConfigColumns.REFUND_INFO_SHOW_CHECKBOX,
                Config.SystemConfigColumns.REFUND_INFO_SHOW_CHECKBOX);
        systemConfigProjectionMap.put(Config.SystemConfigColumns.RULE_VIOLATION_EXPLANATION_REQUIRED,
                Config.SystemConfigColumns.RULE_VIOLATION_EXPLANATION_REQUIRED);
        systemConfigProjectionMap.put(Config.SystemConfigColumns.USER_ID, Config.SystemConfigColumns.USER_ID);

        /*
         * Create and initialize the reason code project map.
         */
        reasonCodeProjectionMap = new HashMap<String, String>();
        reasonCodeProjectionMap.put(Config.ReasonCodeColumns._ID, Config.ReasonCodeColumns._ID);
        reasonCodeProjectionMap.put(Config.ReasonCodeColumns._COUNT, Config.ReasonCodeColumns._COUNT);
        reasonCodeProjectionMap.put(Config.ReasonCodeColumns.TYPE, Config.ReasonCodeColumns.TYPE);
        reasonCodeProjectionMap.put(Config.ReasonCodeColumns.DESCRIPTION, Config.ReasonCodeColumns.DESCRIPTION);
        reasonCodeProjectionMap.put(Config.ReasonCodeColumns.ID, Config.ReasonCodeColumns.ID);
        reasonCodeProjectionMap.put(Config.ReasonCodeColumns.VIOLATION_TYPE, Config.ReasonCodeColumns.VIOLATION_TYPE);
        reasonCodeProjectionMap.put(Config.ReasonCodeColumns.USER_ID, Config.ReasonCodeColumns.USER_ID);

        /*
         * Create and initialize the expense type project map.
         */
        expenseTypeProjectionMap = new HashMap<String, String>();
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns._ID, Config.ExpenseTypeColumns._ID);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns._COUNT, Config.ExpenseTypeColumns._COUNT);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.EXP_CODE, Config.ExpenseTypeColumns.EXP_CODE);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.EXP_KEY, Config.ExpenseTypeColumns.EXP_KEY);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.EXP_NAME, Config.ExpenseTypeColumns.EXP_NAME);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.FORM_KEY, Config.ExpenseTypeColumns.FORM_KEY);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.HAS_POST_AMT_CALC,
                Config.ExpenseTypeColumns.HAS_POST_AMT_CALC);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.HAS_TAX_FORM, Config.ExpenseTypeColumns.HAS_TAX_FORM);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.ITEMIZATION_UNALLOW_EXP_KEYS,
                Config.ExpenseTypeColumns.ITEMIZATION_UNALLOW_EXP_KEYS);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.ITEMIZATION_FORM_KEY,
                Config.ExpenseTypeColumns.ITEMIZATION_FORM_KEY);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.ITEMIZATION_STYLE,
                Config.ExpenseTypeColumns.ITEMIZATION_STYLE);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.ITEMIZATION_TYPE,
                Config.ExpenseTypeColumns.ITEMIZATION_TYPE);
        expenseTypeProjectionMap
                .put(Config.ExpenseTypeColumns.PARENT_EXP_KEY, Config.ExpenseTypeColumns.PARENT_EXP_KEY);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.PARENT_EXP_NAME,
                Config.ExpenseTypeColumns.PARENT_EXP_NAME);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.SUPPORTS_ATTENDEES,
                Config.ExpenseTypeColumns.SUPPORTS_ATTENDEES);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.VENDOR_LIST_KEY,
                Config.ExpenseTypeColumns.VENDOR_LIST_KEY);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_AMOUNT,
                Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_AMOUNT);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_COUNT,
                Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_COUNT);
        expenseTypeProjectionMap
                .put(Config.ExpenseTypeColumns.ALLOW_NO_SHOWS, Config.ExpenseTypeColumns.ALLOW_NO_SHOWS);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.DISPLAY_ADD_ATTENDEE_ON_FORM,
                Config.ExpenseTypeColumns.DISPLAY_ADD_ATTENDEE_ON_FORM);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.DISPLAY_ATTENDEE_AMOUNTS,
                Config.ExpenseTypeColumns.DISPLAY_ATTENDEE_AMOUNTS);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.USER_AS_ATTENDEE_DEFAULT,
                Config.ExpenseTypeColumns.USER_AS_ATTENDEE_DEFAULT);
        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.UNALLOW_ATN_TYPE_KEYS,
                Config.ExpenseTypeColumns.UNALLOW_ATN_TYPE_KEYS);

        expenseTypeProjectionMap.put(Config.ExpenseTypeColumns.USER_ID, Config.ExpenseTypeColumns.USER_ID);

        /*
         * Create and initialize the office location project map.
         */
        officeLocationProjectionMap = new HashMap<String, String>();
        officeLocationProjectionMap.put(Config.OfficeLocationColumns._ID, Config.OfficeLocationColumns._ID);
        officeLocationProjectionMap.put(Config.OfficeLocationColumns._COUNT, Config.OfficeLocationColumns._COUNT);
        officeLocationProjectionMap.put(Config.OfficeLocationColumns.ADDRESS, Config.OfficeLocationColumns.ADDRESS);
        officeLocationProjectionMap.put(Config.OfficeLocationColumns.CITY, Config.OfficeLocationColumns.CITY);
        officeLocationProjectionMap.put(Config.OfficeLocationColumns.COUNTRY, Config.OfficeLocationColumns.COUNTRY);
        officeLocationProjectionMap.put(Config.OfficeLocationColumns.LAT, Config.OfficeLocationColumns.LAT);
        officeLocationProjectionMap.put(Config.OfficeLocationColumns.LON, Config.OfficeLocationColumns.LON);
        officeLocationProjectionMap.put(Config.OfficeLocationColumns.STATE, Config.OfficeLocationColumns.STATE);
        officeLocationProjectionMap.put(Config.OfficeLocationColumns.USER_ID, Config.OfficeLocationColumns.USER_ID);

        /*
         * Create and initialize the user config projection map.
         */
        userConfigProjectionMap = new HashMap<String, String>();
        userConfigProjectionMap.put(Config.UserConfigColumns._ID, Config.UserConfigColumns._ID);
        userConfigProjectionMap.put(Config.UserConfigColumns.USER_ID, Config.UserConfigColumns.USER_ID);
        userConfigProjectionMap.put(Config.UserConfigColumns.HASH, Config.UserConfigColumns.HASH);
        userConfigProjectionMap.put(Config.UserConfigColumns.ALLOWED_AIR_CLASSES_OF_SERVICE,
                Config.UserConfigColumns.ALLOWED_AIR_CLASSES_OF_SERVICE);
        userConfigProjectionMap.put(Config.UserConfigColumns.FLAGS, Config.UserConfigColumns.FLAGS);
        userConfigProjectionMap.put(Config.UserConfigColumns.SHOW_GDS_NAME_IN_SEARCH_RESULTS,
                Config.UserConfigColumns.SHOW_GDS_NAME_IN_SEARCH_RESULTS);

        /*
         * Create and initialize the car type projection map.
         */
        carTypeProjectionMap = new HashMap<String, String>();
        carTypeProjectionMap.put(Config.CarTypeColumns._ID, Config.CarTypeColumns._ID);
        carTypeProjectionMap.put(Config.CarTypeColumns.USER_ID, Config.CarTypeColumns.USER_ID);
        carTypeProjectionMap.put(Config.CarTypeColumns.DESCRIPTION, Config.CarTypeColumns.DESCRIPTION);
        carTypeProjectionMap.put(Config.CarTypeColumns.CODE, Config.CarTypeColumns.CODE);
        carTypeProjectionMap.put(Config.CarTypeColumns.IS_DEFAULT, Config.CarTypeColumns.IS_DEFAULT);

        /*
         * Create and initialize the attendee column definition projection map.
         */
        attendeeColumnDefinitionProjectionMap = new HashMap<String, String>();
        attendeeColumnDefinitionProjectionMap.put(Config.AttendeeColumnDefinitionColumns._ID,
                Config.AttendeeColumnDefinitionColumns._ID);
        attendeeColumnDefinitionProjectionMap.put(Config.AttendeeColumnDefinitionColumns.USER_ID,
                Config.AttendeeColumnDefinitionColumns.USER_ID);
        attendeeColumnDefinitionProjectionMap.put(Config.AttendeeColumnDefinitionColumns.ID,
                Config.AttendeeColumnDefinitionColumns.ID);
        attendeeColumnDefinitionProjectionMap.put(Config.AttendeeColumnDefinitionColumns.LABEL,
                Config.AttendeeColumnDefinitionColumns.LABEL);
        attendeeColumnDefinitionProjectionMap.put(Config.AttendeeColumnDefinitionColumns.DATA_TYPE,
                Config.AttendeeColumnDefinitionColumns.DATA_TYPE);
        attendeeColumnDefinitionProjectionMap.put(Config.AttendeeColumnDefinitionColumns.CTRL_TYPE,
                Config.AttendeeColumnDefinitionColumns.CTRL_TYPE);
        attendeeColumnDefinitionProjectionMap.put(Config.AttendeeColumnDefinitionColumns.ACCESS,
                Config.AttendeeColumnDefinitionColumns.ACCESS);

        /*
         * Create and initialize the attendee type projection map.
         */
        attendeeTypeProjectionMap = new HashMap<String, String>();
        attendeeTypeProjectionMap.put(Config.AttendeeTypeColumns._ID, Config.AttendeeTypeColumns._ID);
        attendeeTypeProjectionMap.put(Config.AttendeeTypeColumns.USER_ID, Config.AttendeeTypeColumns.USER_ID);
        attendeeTypeProjectionMap.put(Config.AttendeeTypeColumns.ALLOW_EDIT_ATN_COUNT,
                Config.AttendeeTypeColumns.ALLOW_EDIT_ATN_COUNT);
        attendeeTypeProjectionMap.put(Config.AttendeeTypeColumns.ATN_TYPE_CODE,
                Config.AttendeeTypeColumns.ATN_TYPE_CODE);
        attendeeTypeProjectionMap.put(Config.AttendeeTypeColumns.ATN_TYPE_KEY, Config.AttendeeTypeColumns.ATN_TYPE_KEY);
        attendeeTypeProjectionMap.put(Config.AttendeeTypeColumns.ATN_TYPE_NAME,
                Config.AttendeeTypeColumns.ATN_TYPE_NAME);
        attendeeTypeProjectionMap.put(Config.AttendeeTypeColumns.FORM_KEY, Config.AttendeeTypeColumns.FORM_KEY);
        attendeeTypeProjectionMap.put(Config.AttendeeTypeColumns.IS_EXTERNAL, Config.AttendeeTypeColumns.IS_EXTERNAL);

        /*
         * Create and initialize the currency projection map.
         */
        currencyProjectionMap = new HashMap<String, String>();
        currencyProjectionMap.put(Config.CurrencyColumns._ID, Config.CurrencyColumns._ID);
        currencyProjectionMap.put(Config.CurrencyColumns.USER_ID, Config.CurrencyColumns.USER_ID);
        currencyProjectionMap.put(Config.CurrencyColumns.CRN_CODE, Config.CurrencyColumns.CRN_CODE);
        currencyProjectionMap.put(Config.CurrencyColumns.CRN_NAME, Config.CurrencyColumns.CRN_NAME);
        currencyProjectionMap.put(Config.CurrencyColumns.DECIMAL_DIGITS, Config.CurrencyColumns.DECIMAL_DIGITS);
        currencyProjectionMap.put(Config.CurrencyColumns.IS_REIMBURSEMENT, Config.CurrencyColumns.IS_REIMBURSEMENT);

        /*
         * Create and initialize the expense confirmation projection map.
         */
        expenseConfirmationProjectionMap = new HashMap<String, String>();
        expenseConfirmationProjectionMap.put(Config.ExpenseConfirmationColumns._ID,
                Config.ExpenseConfirmationColumns._ID);
        expenseConfirmationProjectionMap.put(Config.ExpenseConfirmationColumns.USER_ID,
                Config.ExpenseConfirmationColumns.USER_ID);
        expenseConfirmationProjectionMap.put(Config.ExpenseConfirmationColumns.CONFIRMATION_KEY,
                Config.ExpenseConfirmationColumns.CONFIRMATION_KEY);
        expenseConfirmationProjectionMap.put(Config.ExpenseConfirmationColumns.TEXT,
                Config.ExpenseConfirmationColumns.TEXT);
        expenseConfirmationProjectionMap.put(Config.ExpenseConfirmationColumns.TITLE,
                Config.ExpenseConfirmationColumns.TITLE);

        /*
         * Create and initialize the policy projection map.
         */
        policyProjectionMap = new HashMap<String, String>();
        policyProjectionMap.put(Config.PolicyColumns._ID, Config.PolicyColumns._ID);
        policyProjectionMap.put(Config.PolicyColumns.USER_ID, Config.PolicyColumns.USER_ID);
        policyProjectionMap.put(Config.PolicyColumns.POL_KEY, Config.PolicyColumns.POL_KEY);
        policyProjectionMap.put(Config.PolicyColumns.SUPPORTS_IMAGING, Config.PolicyColumns.SUPPORTS_IMAGING);
        policyProjectionMap.put(Config.PolicyColumns.APPROVAL_CONFIRMATION_KEY,
                Config.PolicyColumns.APPROVAL_CONFIRMATION_KEY);
        policyProjectionMap.put(Config.PolicyColumns.SUBMIT_CONFIRMATION_KEY,
                Config.PolicyColumns.SUBMIT_CONFIRMATION_KEY);

        /*
         * Create and initialize the Yodlee payment type projection map.
         */
        yodleePaymentTypeProjectionMap = new HashMap<String, String>();
        yodleePaymentTypeProjectionMap.put(Config.YodleePaymentTypeColumns._ID, Config.YodleePaymentTypeColumns._ID);
        yodleePaymentTypeProjectionMap.put(Config.YodleePaymentTypeColumns.USER_ID,
                Config.YodleePaymentTypeColumns.USER_ID);
        yodleePaymentTypeProjectionMap.put(Config.YodleePaymentTypeColumns.KEY, Config.YodleePaymentTypeColumns.KEY);
        yodleePaymentTypeProjectionMap.put(Config.YodleePaymentTypeColumns.TEXT, Config.YodleePaymentTypeColumns.TEXT);

        /*
         * Create and initialize the travel points config projection map.
         */
        travelPointsConfigProjectionMap = new HashMap<String, String>();
        travelPointsConfigProjectionMap.put(Config.TravelPointsConfigColumns._ID, Config.TravelPointsConfigColumns._ID);
        travelPointsConfigProjectionMap.put(Config.TravelPointsConfigColumns.USER_ID,
                Config.TravelPointsConfigColumns.USER_ID);
        travelPointsConfigProjectionMap.put(Config.TravelPointsConfigColumns.AIR_TRAVEL_POINTS_ENABLED,
                Config.TravelPointsConfigColumns.AIR_TRAVEL_POINTS_ENABLED);
        travelPointsConfigProjectionMap.put(Config.TravelPointsConfigColumns.HOTEL_TRAVEL_POINTS_ENABLED,
                Config.TravelPointsConfigColumns.HOTEL_TRAVEL_POINTS_ENABLED);

        /*
         * Create and initialize the credit card projection map.
         */
        creditCardProjectionMap = new HashMap<String, String>();
        creditCardProjectionMap.put(Config.CreditCardColumns._ID, Config.CreditCardColumns._ID);
        creditCardProjectionMap.put(Config.CreditCardColumns.USER_ID, Config.CreditCardColumns.USER_ID);
        creditCardProjectionMap.put(Config.CreditCardColumns.NAME, Config.CreditCardColumns.NAME);
        creditCardProjectionMap.put(Config.CreditCardColumns.TYPE, Config.CreditCardColumns.TYPE);
        creditCardProjectionMap.put(Config.CreditCardColumns.MASKED_NUMBER, Config.CreditCardColumns.MASKED_NUMBER);
        creditCardProjectionMap.put(Config.CreditCardColumns.CC_ID, Config.CreditCardColumns.CC_ID);
        creditCardProjectionMap.put(Config.CreditCardColumns.DEFAULT_FOR, Config.CreditCardColumns.DEFAULT_FOR);
        creditCardProjectionMap.put(Config.CreditCardColumns.ALLOW_FOR, Config.CreditCardColumns.ALLOW_FOR);
        creditCardProjectionMap.put(Config.CreditCardColumns.LAST_FOUR, Config.CreditCardColumns.LAST_FOUR);
        creditCardProjectionMap.put(Config.CreditCardColumns.IS_DEFAULT, Config.CreditCardColumns.IS_DEFAULT);

        /*
         * Create and initialize the affinity program projection map.
         */
        affinityProgramProjectionMap = new HashMap<String, String>();
        affinityProgramProjectionMap.put(Config.AffinityProgramColumns._ID, Config.AffinityProgramColumns._ID);
        affinityProgramProjectionMap.put(Config.AffinityProgramColumns.USER_ID, Config.AffinityProgramColumns.USER_ID);
        affinityProgramProjectionMap.put(Config.AffinityProgramColumns.ACCOUNT_NUMBER,
                Config.AffinityProgramColumns.ACCOUNT_NUMBER);
        affinityProgramProjectionMap.put(Config.AffinityProgramColumns.DESCRIPTION,
                Config.AffinityProgramColumns.DESCRIPTION);
        affinityProgramProjectionMap.put(Config.AffinityProgramColumns.VENDOR, Config.AffinityProgramColumns.VENDOR);
        affinityProgramProjectionMap.put(Config.AffinityProgramColumns.VENDOR_ABBREV,
                Config.AffinityProgramColumns.VENDOR_ABBREV);
        affinityProgramProjectionMap.put(Config.AffinityProgramColumns.PROGRAM_NAME,
                Config.AffinityProgramColumns.PROGRAM_NAME);
        affinityProgramProjectionMap.put(Config.AffinityProgramColumns.PROGRAM_TYPE,
                Config.AffinityProgramColumns.PROGRAM_TYPE);
        affinityProgramProjectionMap.put(Config.AffinityProgramColumns.PROGRAM_ID,
                Config.AffinityProgramColumns.PROGRAM_ID);
        affinityProgramProjectionMap.put(Config.AffinityProgramColumns.IS_DEFAULT,
                Config.AffinityProgramColumns.IS_DEFAULT);

        /*
         * Create and initialize the client data projection map.
         */
        clientDataProjectionMap = new HashMap<String, String>();
        clientDataProjectionMap.put(Config.ClientDataColumns._ID, Config.ClientDataColumns._ID);
        clientDataProjectionMap.put(Config.ClientDataColumns.USER_ID, Config.ClientDataColumns.USER_ID);
        clientDataProjectionMap.put(Config.ClientDataColumns.KEY, Config.ClientDataColumns.KEY);
        clientDataProjectionMap.put(Config.ClientDataColumns.VALUE_TEXT, Config.ClientDataColumns.VALUE_TEXT);
        clientDataProjectionMap.put(Config.ClientDataColumns.VALUE_BLOB, Config.ClientDataColumns.VALUE_BLOB);

        /*
         * Create and initialize the site permissions map.
         */
        permissionsProjectionMap = new HashMap<String, String>();
        permissionsProjectionMap.put(Config.PermissionsColumns._ID, Config.PermissionsColumns._ID);
        permissionsProjectionMap.put(Config.PermissionsColumns._COUNT, Config.PermissionsColumns._COUNT);
        permissionsProjectionMap.put(Config.PermissionsColumns.NAME, Config.PermissionsColumns.NAME);
        permissionsProjectionMap.put(Config.PermissionsColumns.VALUE, Config.PermissionsColumns.VALUE);
        permissionsProjectionMap.put(Config.PermissionsColumns.USER_ID, Config.PermissionsColumns.USER_ID);
    }

    @Override
    protected SparseArray<UriMatcherInfo> initCodeUriMatcherInfoMap() {

        SparseArray<UriMatcherInfo> map = new SparseArray<UriMatcherInfo>();

        // Init the SESSIONS info.
        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.SessionColumns.CONTENT_TYPE;
        info.tableName = Config.SessionColumns.TABLE_NAME;
        info.nullColumnName = Config.SessionColumns.USER_ID;
        info.contentIdUriBase = Config.SessionColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.SessionColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = sessionProjectionMap;
        map.put(SESSIONS, info);

        // Init the SESSION_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.SessionColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.SessionColumns.TABLE_NAME;
        info.nullColumnName = Config.SessionColumns.USER_ID;
        info.contentIdUriBase = Config.SessionColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.SessionColumns._ID;
        info.projectionMap = sessionProjectionMap;
        info.defaultSortOrder = Config.SessionColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.SessionColumns.SESSION_ID_PATH_POSITION;
        map.put(SESSION_ID, info);

        // Init the USERS info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.UserColumns.CONTENT_TYPE;
        info.tableName = Config.UserColumns.TABLE_NAME;
        info.nullColumnName = Config.UserColumns.USER_ID;
        info.contentIdUriBase = Config.UserColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.UserColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = userProjectionMap;
        map.put(USERS, info);

        // Init the USER_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.UserColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.UserColumns.TABLE_NAME;
        info.nullColumnName = Config.UserColumns.USER_ID;
        info.contentIdUriBase = Config.UserColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.UserColumns._ID;
        info.projectionMap = userProjectionMap;
        info.defaultSortOrder = Config.UserColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.UserColumns.USER_ID_PATH_POSITION;
        map.put(USER_ID, info);

        // Init the SITE_SETTING info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.SiteSettingColumns.CONTENT_TYPE;
        info.tableName = Config.SiteSettingColumns.TABLE_NAME;
        info.nullColumnName = Config.SiteSettingColumns.USER_ID;
        info.contentIdUriBase = Config.SiteSettingColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.SiteSettingColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = siteSettingProjectionMap;
        map.put(SITE_SETTINGS, info);

        // Init the SITE_SETTING_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.SiteSettingColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.SiteSettingColumns.TABLE_NAME;
        info.nullColumnName = Config.SiteSettingColumns.USER_ID;
        info.contentIdUriBase = Config.SiteSettingColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.SiteSettingColumns._ID;
        info.projectionMap = siteSettingProjectionMap;
        info.defaultSortOrder = Config.SiteSettingColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.SiteSettingColumns.SITE_SETTING_ID_PATH_POSITION;
        map.put(SITE_SETTING_ID, info);

        // Init the SYSTEM_CONFIGS info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.SystemConfigColumns.CONTENT_TYPE;
        info.tableName = Config.SystemConfigColumns.TABLE_NAME;
        info.nullColumnName = Config.SystemConfigColumns.USER_ID;
        info.contentIdUriBase = Config.SystemConfigColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.SystemConfigColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = systemConfigProjectionMap;
        map.put(SYSTEM_CONFIGS, info);

        // Init the SYSTEM_CONFIG_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.SystemConfigColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.SystemConfigColumns.TABLE_NAME;
        info.nullColumnName = Config.SystemConfigColumns.USER_ID;
        info.contentIdUriBase = Config.SystemConfigColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.SystemConfigColumns._ID;
        info.projectionMap = systemConfigProjectionMap;
        info.defaultSortOrder = Config.SystemConfigColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.SystemConfigColumns.SYSTEM_CONFIG_ID_PATH_POSITION;
        map.put(SYSTEM_CONFIG_ID, info);

        // Init the REASON_CODES info.
        ReasonCodeBulkInserter reasonCodeBulkInserter = new ReasonCodeBulkInserter();
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.ReasonCodeColumns.CONTENT_TYPE;
        info.tableName = Config.ReasonCodeColumns.TABLE_NAME;
        info.nullColumnName = Config.ReasonCodeColumns.USER_ID;
        info.contentIdUriBase = Config.ReasonCodeColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.ReasonCodeColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = reasonCodeProjectionMap;
        info.bulkInserter = reasonCodeBulkInserter;
        map.put(REASON_CODES, info);

        // Init the REASON_CODE_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.ReasonCodeColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.ReasonCodeColumns.TABLE_NAME;
        info.nullColumnName = Config.ReasonCodeColumns.USER_ID;
        info.contentIdUriBase = Config.ReasonCodeColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.ReasonCodeColumns._ID;
        info.projectionMap = reasonCodeProjectionMap;
        info.defaultSortOrder = Config.ReasonCodeColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.ReasonCodeColumns.REASON_CODE_ID_PATH_POSITION;
        info.bulkInserter = reasonCodeBulkInserter;
        map.put(REASON_CODE_ID, info);

        // Init the EXPENSE_TYPES info.
        ExpenseTypeBulkInserter expTypeBulkInsert = new ExpenseTypeBulkInserter();
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.ExpenseTypeColumns.CONTENT_TYPE;
        info.tableName = Config.ExpenseTypeColumns.TABLE_NAME;
        info.nullColumnName = Config.ExpenseTypeColumns.USER_ID;
        info.contentIdUriBase = Config.ExpenseTypeColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.ExpenseTypeColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = expenseTypeProjectionMap;
        info.bulkInserter = expTypeBulkInsert;
        map.put(EXPENSE_TYPES, info);

        // Init the EXPENSE_TYPE_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.ExpenseTypeColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.ExpenseTypeColumns.TABLE_NAME;
        info.nullColumnName = Config.ExpenseTypeColumns.USER_ID;
        info.contentIdUriBase = Config.ExpenseTypeColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.ExpenseTypeColumns._ID;
        info.projectionMap = expenseTypeProjectionMap;
        info.defaultSortOrder = Config.ExpenseTypeColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.ExpenseTypeColumns.EXPENSE_TYPE_ID_PATH_POSITION;
        info.bulkInserter = expTypeBulkInsert;
        map.put(EXPENSE_TYPE_ID, info);

        // Init the OFFICE_LOCATIONS info.
        OfficeLocationBulkInserter officeLocationBulkInserter = new OfficeLocationBulkInserter();
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.OfficeLocationColumns.CONTENT_TYPE;
        info.tableName = Config.OfficeLocationColumns.TABLE_NAME;
        info.nullColumnName = Config.OfficeLocationColumns.USER_ID;
        info.contentIdUriBase = Config.OfficeLocationColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.OfficeLocationColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = officeLocationProjectionMap;
        info.bulkInserter = officeLocationBulkInserter;
        map.put(OFFICE_LOCATIONS, info);

        // Init the OFFICE_LOCATION_ID info.

        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.OfficeLocationColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.OfficeLocationColumns.TABLE_NAME;
        info.nullColumnName = Config.OfficeLocationColumns.USER_ID;
        info.contentIdUriBase = Config.OfficeLocationColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.OfficeLocationColumns._ID;
        info.projectionMap = officeLocationProjectionMap;
        info.defaultSortOrder = Config.OfficeLocationColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.OfficeLocationColumns.OFFICE_LOCATION_ID_PATH_POSITION;
        info.bulkInserter = officeLocationBulkInserter;
        map.put(OFFICE_LOCATION_ID, info);

        // Init the USER_CONFIGS info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.UserConfigColumns.CONTENT_TYPE;
        info.tableName = Config.UserConfigColumns.TABLE_NAME;
        info.nullColumnName = Config.UserConfigColumns.USER_ID;
        info.contentIdUriBase = Config.UserConfigColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.UserConfigColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = userConfigProjectionMap;
        map.put(USER_CONFIGS, info);

        // Init the USER_CONFIG_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.UserConfigColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.UserConfigColumns.TABLE_NAME;
        info.nullColumnName = Config.UserConfigColumns.USER_ID;
        info.contentIdUriBase = Config.UserConfigColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.UserConfigColumns._ID;
        info.projectionMap = userConfigProjectionMap;
        info.defaultSortOrder = Config.UserConfigColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.UserConfigColumns.USER_CONFIG_ID_PATH_POSITION;
        map.put(USER_CONFIG_ID, info);

        // Init the CAR_TYPES info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.CarTypeColumns.CONTENT_TYPE;
        info.tableName = Config.CarTypeColumns.TABLE_NAME;
        info.nullColumnName = Config.CarTypeColumns.USER_ID;
        info.contentIdUriBase = Config.CarTypeColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.CarTypeColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = carTypeProjectionMap;
        map.put(CAR_TYPES, info);

        // Init the CAR_TYPE_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.CarTypeColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.CarTypeColumns.TABLE_NAME;
        info.nullColumnName = Config.CarTypeColumns.USER_ID;
        info.contentIdUriBase = Config.CarTypeColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.CarTypeColumns._ID;
        info.projectionMap = carTypeProjectionMap;
        info.defaultSortOrder = Config.CarTypeColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.CarTypeColumns.CAR_TYPE_ID_PATH_POSITION;
        map.put(CAR_TYPE_ID, info);

        // Init the ATTENDEE_COLUMN_DEFINITIONS info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.AttendeeColumnDefinitionColumns.CONTENT_TYPE;
        info.tableName = Config.AttendeeColumnDefinitionColumns.TABLE_NAME;
        info.nullColumnName = Config.AttendeeColumnDefinitionColumns.USER_ID;
        info.contentIdUriBase = Config.AttendeeColumnDefinitionColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.AttendeeColumnDefinitionColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = attendeeColumnDefinitionProjectionMap;
        map.put(ATTENDEE_COLUMN_DEFINITIONS, info);

        // Init the ATTENDEE_COLUMN_DEFINITION_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.AttendeeColumnDefinitionColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.AttendeeColumnDefinitionColumns.TABLE_NAME;
        info.nullColumnName = Config.AttendeeColumnDefinitionColumns.USER_ID;
        info.contentIdUriBase = Config.AttendeeColumnDefinitionColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.AttendeeColumnDefinitionColumns._ID;
        info.projectionMap = attendeeColumnDefinitionProjectionMap;
        info.defaultSortOrder = Config.AttendeeColumnDefinitionColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.AttendeeColumnDefinitionColumns.ATTENDEE_COLUMN_DEFINITION_ID_PATH_POSITION;
        map.put(ATTENDEE_COLUMN_DEFINITION_ID, info);

        // Init the ATTENDEE_TYPES info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.AttendeeTypeColumns.CONTENT_TYPE;
        info.tableName = Config.AttendeeTypeColumns.TABLE_NAME;
        info.nullColumnName = Config.AttendeeTypeColumns.USER_ID;
        info.contentIdUriBase = Config.AttendeeTypeColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.AttendeeTypeColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = attendeeTypeProjectionMap;
        map.put(ATTENDEE_TYPES, info);

        // Init the ATTENDEE_TYPE_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.AttendeeTypeColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.AttendeeTypeColumns.TABLE_NAME;
        info.nullColumnName = Config.AttendeeTypeColumns.USER_ID;
        info.contentIdUriBase = Config.AttendeeTypeColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.AttendeeTypeColumns._ID;
        info.projectionMap = attendeeTypeProjectionMap;
        info.defaultSortOrder = Config.AttendeeTypeColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.AttendeeTypeColumns.ATTENDEE_TYPE_ID_PATH_POSITION;
        map.put(ATTENDEE_TYPE_ID, info);

        // Init the CURRENCY_TYPES info.
        CurrencyBulkInserter currencyBulkInserter = new CurrencyBulkInserter();
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.CurrencyColumns.CONTENT_TYPE;
        info.tableName = Config.CurrencyColumns.TABLE_NAME;
        info.nullColumnName = Config.CurrencyColumns.USER_ID;
        info.contentIdUriBase = Config.CurrencyColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.CurrencyColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = currencyProjectionMap;
        info.bulkInserter = currencyBulkInserter;
        map.put(CURRENCY_TYPES, info);

        // Init the CURRENCY_TYPE_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.CurrencyColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.CurrencyColumns.TABLE_NAME;
        info.nullColumnName = Config.CurrencyColumns.USER_ID;
        info.contentIdUriBase = Config.CurrencyColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.CurrencyColumns._ID;
        info.projectionMap = currencyProjectionMap;
        info.defaultSortOrder = Config.CurrencyColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.CurrencyColumns.CURRENCY_ID_PATH_POSITION;
        info.bulkInserter = currencyBulkInserter;
        map.put(CURRENCY_TYPE_ID, info);

        // Init the EXPENSE_CONFIRMATIONS info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.ExpenseConfirmationColumns.CONTENT_TYPE;
        info.tableName = Config.ExpenseConfirmationColumns.TABLE_NAME;
        info.nullColumnName = Config.ExpenseConfirmationColumns.USER_ID;
        info.contentIdUriBase = Config.ExpenseConfirmationColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.ExpenseConfirmationColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = expenseConfirmationProjectionMap;
        map.put(EXPENSE_CONFIRMATIONS, info);

        // Init the EXPENSE_CONFIRMATION_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.ExpenseConfirmationColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.ExpenseConfirmationColumns.TABLE_NAME;
        info.nullColumnName = Config.ExpenseConfirmationColumns.USER_ID;
        info.contentIdUriBase = Config.ExpenseConfirmationColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.ExpenseConfirmationColumns._ID;
        info.projectionMap = expenseConfirmationProjectionMap;
        info.defaultSortOrder = Config.ExpenseConfirmationColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.ExpenseConfirmationColumns.EXPENSE_CONFIRMATION_ID_PATH_POSITION;
        map.put(EXPENSE_CONFIRMATION_ID, info);

        // Init the POLICIES info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.PolicyColumns.CONTENT_TYPE;
        info.tableName = Config.PolicyColumns.TABLE_NAME;
        info.nullColumnName = Config.PolicyColumns.USER_ID;
        info.contentIdUriBase = Config.PolicyColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.PolicyColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = policyProjectionMap;
        map.put(POLICIES, info);

        // Init the POLICY_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.PolicyColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.PolicyColumns.TABLE_NAME;
        info.nullColumnName = Config.PolicyColumns.USER_ID;
        info.contentIdUriBase = Config.PolicyColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.PolicyColumns._ID;
        info.projectionMap = policyProjectionMap;
        info.defaultSortOrder = Config.PolicyColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.PolicyColumns.POLICY_ID_PATH_POSITION;
        map.put(POLICY_ID, info);

        // Init the YODLEE_PAYMENT_TYPES info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.YodleePaymentTypeColumns.CONTENT_TYPE;
        info.tableName = Config.YodleePaymentTypeColumns.TABLE_NAME;
        info.nullColumnName = Config.YodleePaymentTypeColumns.USER_ID;
        info.contentIdUriBase = Config.YodleePaymentTypeColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.YodleePaymentTypeColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = yodleePaymentTypeProjectionMap;
        map.put(YODLEE_PAYMENT_TYPES, info);

        // Init the YODLEE_PAYMENT_TYPE_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.YodleePaymentTypeColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.YodleePaymentTypeColumns.TABLE_NAME;
        info.nullColumnName = Config.YodleePaymentTypeColumns.USER_ID;
        info.contentIdUriBase = Config.YodleePaymentTypeColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.YodleePaymentTypeColumns._ID;
        info.projectionMap = yodleePaymentTypeProjectionMap;
        info.defaultSortOrder = Config.YodleePaymentTypeColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.YodleePaymentTypeColumns.YODLEE_PAYMENT_TYPE_ID_PATH_POSITION;
        map.put(YODLEE_PAYMENT_TYPE_ID, info);

        // Init the TRAVEL_POINTS_CONFIG info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.TravelPointsConfigColumns.CONTENT_TYPE;
        info.tableName = Config.TravelPointsConfigColumns.TABLE_NAME;
        info.nullColumnName = Config.TravelPointsConfigColumns.USER_ID;
        info.contentIdUriBase = Config.TravelPointsConfigColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.TravelPointsConfigColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = travelPointsConfigProjectionMap;
        map.put(TRAVEL_POINTS_CONFIG, info);

        // Init the TRAVEL_POINTS_CONFIG_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.TravelPointsConfigColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.TravelPointsConfigColumns.TABLE_NAME;
        info.nullColumnName = Config.TravelPointsConfigColumns.USER_ID;
        info.contentIdUriBase = Config.TravelPointsConfigColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.TravelPointsConfigColumns._ID;
        info.projectionMap = travelPointsConfigProjectionMap;
        info.defaultSortOrder = Config.TravelPointsConfigColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.TravelPointsConfigColumns.TRAVEL_POINTS_CONFIG_ID_PATH_POSITION;
        map.put(TRAVEL_POINTS_CONFIG_ID, info);

        // Init the CREDIT_CARDS info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.CreditCardColumns.CONTENT_TYPE;
        info.tableName = Config.CreditCardColumns.TABLE_NAME;
        info.nullColumnName = Config.CreditCardColumns.USER_ID;
        info.contentIdUriBase = Config.CreditCardColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.CreditCardColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = creditCardProjectionMap;
        map.put(CREDIT_CARDS, info);

        // Init the CREDIT_CARD_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.CreditCardColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.CreditCardColumns.TABLE_NAME;
        info.nullColumnName = Config.CreditCardColumns.USER_ID;
        info.contentIdUriBase = Config.CreditCardColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.CreditCardColumns._ID;
        info.projectionMap = creditCardProjectionMap;
        info.defaultSortOrder = Config.CreditCardColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.CreditCardColumns.CREDIT_CARD_ID_PATH_POSITION;
        map.put(CREDIT_CARD_ID, info);

        // Init the AFFINITY_PROGRAMS info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.AffinityProgramColumns.CONTENT_TYPE;
        info.tableName = Config.AffinityProgramColumns.TABLE_NAME;
        info.nullColumnName = Config.AffinityProgramColumns.USER_ID;
        info.contentIdUriBase = Config.AffinityProgramColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.AffinityProgramColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = affinityProgramProjectionMap;
        map.put(AFFINITY_PROGRAMS, info);

        // Init the AFFINITY_PROGRAM_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.AffinityProgramColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.AffinityProgramColumns.TABLE_NAME;
        info.nullColumnName = Config.AffinityProgramColumns.USER_ID;
        info.contentIdUriBase = Config.AffinityProgramColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.AffinityProgramColumns._ID;
        info.projectionMap = affinityProgramProjectionMap;
        info.defaultSortOrder = Config.AffinityProgramColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.AffinityProgramColumns.AFFINITY_PROGRAM_ID_PATH_POSITION;
        map.put(AFFINITY_PROGRAM_ID, info);

        // Init the CLIENT_DATA info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.ClientDataColumns.CONTENT_TYPE;
        info.tableName = Config.ClientDataColumns.TABLE_NAME;
        info.nullColumnName = Config.ClientDataColumns.USER_ID;
        info.contentIdUriBase = Config.ClientDataColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.ClientDataColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = clientDataProjectionMap;
        map.put(CLIENT_DATA, info);

        // Init the CLIENT_DATA_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.ClientDataColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.ClientDataColumns.TABLE_NAME;
        info.nullColumnName = Config.ClientDataColumns.USER_ID;
        info.contentIdUriBase = Config.ClientDataColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.ClientDataColumns._ID;
        info.projectionMap = clientDataProjectionMap;
        info.defaultSortOrder = Config.ClientDataColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.ClientDataColumns.CLIENT_DATA_ID_PATH_POSITION;
        map.put(CLIENT_DATA_ID, info);

        // Init the PERMISSIONS info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Config.PermissionsColumns.CONTENT_TYPE;
        info.tableName = Config.PermissionsColumns.TABLE_NAME;
        info.nullColumnName = Config.PermissionsColumns.USER_ID;
        info.contentIdUriBase = Config.PermissionsColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Config.PermissionsColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = permissionsProjectionMap;
        map.put(PERMISSIONS, info);

        // Init the PERMISSIONS_ID info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Config.PermissionsColumns.CONTENT_ITEM_TYPE;
        info.tableName = Config.PermissionsColumns.TABLE_NAME;
        info.nullColumnName = Config.PermissionsColumns.USER_ID;
        info.contentIdUriBase = Config.PermissionsColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Config.PermissionsColumns._ID;
        info.projectionMap = permissionsProjectionMap;
        info.defaultSortOrder = Config.PermissionsColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Config.PermissionsColumns.PERMISSIONS_ID_PATH_POSITION;
        map.put(PERMISSIONS_ID, info);

        return map;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformContentProvider#initPlatformSQLiteOpenHelper()
     */
    @Override
    public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
        // This implementation will use an encrypted database.
        PlatformSQLiteOpenHelper helper = new EncryptedSQLiteOpenHelper(new EncryptedConfigDBHelper(context));
        return helper;
    }

    @Override
    protected String getDatabaseName() {
        return ConfigDBSchema.DATABASE_NAME;
    }

}
