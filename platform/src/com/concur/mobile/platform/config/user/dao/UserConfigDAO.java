package com.concur.mobile.platform.config.user.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

import com.concur.mobile.platform.config.provider.Config;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.CursorUtil;

/**
 * Provides a data access object of User Configuration Information.
 * 
 * @author yiwenw
 */
public class UserConfigDAO {

    private static final String CLS_TAG = UserConfigDAO.class.getSimpleName();

    /**
     * Contains the server-generated hash code for the user config data.
     */
    private String hash;

    /**
     * Contains the response id for this data.
     */
    private String responseId;

    /**
     * Contains the raw text of "allowed air classes of service".
     */
    private String allowedAirClassesOfServiceRaw;

    /**
     * Contains the list of "allowed air classes of service".
     */
    private List<String> allowedAirClassesOfService;

    /**
     * Contains a set of flags.
     */
    private String flags;

    /**
     * Contains the flag on whether to show gds name in search results
     */
    private Boolean showGDSNameInSearchResults;

    /**
     * Contains the currencies.
     */
    private List<CurrencyDAO> currencies;

    /**
     * Contains the reimbursement currencies.
     */
    private List<CurrencyDAO> reimbursementCurrencies;

    /**
     * Contains the car types.
     */
    private List<CarTypeDAO> allowedCarTypes;

    /**
     * Contains the expense policies.
     */
    private List<PolicyDAO> expensePolicies;

    /**
     * Contains the expense confirmations.
     */
    private List<ExpenseConfirmationDAO> expenseConfirmations;

    /**
     * Contains the attendee types.
     */
    private List<AttendeeTypeDAO> attendeeTypes;

    /**
     * Contains the attendee column definitions.
     */
    private List<AttendeeColumnDefinitionDAO> attendeeColumnDefinitions;

    /**
     * Contains the Yodlee payment types.
     */
    private List<YodleePaymentTypeDAO> yodleePaymentTypes;

    /**
     * Contains the travel points config. Use a list as a container for Observer
     */
    private List<TravelPointsConfigDAO> travelPointsConfig;

    /**
     * Contains User Config Content Observer.
     */
    private ContentObserver userConfigObserver;

    /**
     * Contains Attendee Type Content Observer.
     */
    private ContentObserver attendeeTypeObserver;

    /**
     * Contains Attendee Column Definition Content Observer.
     */
    private ContentObserver attendeeColumnDefinitionObserver;

    /**
     * Contains Car Type Content Observer.
     */
    private ContentObserver carTypeObserver;

    /**
     * Contains Currency Content Observer.
     */
    private ContentObserver currencyObserver;

    /**
     * Contains Expense Confirmation Content Observer.
     */
    private ContentObserver expenseConfirmationObserver;

    /**
     * Contains Policy Content Observer.
     */
    private ContentObserver policyObserver;

    /**
     * Contains Yodlee Payment Type Content Observer.
     */
    private ContentObserver yodleePaymentTypeObserver;

    /**
     * Contains Travel Points Config Content Observer.
     */
    private ContentObserver travelPointsConfigObserver;

    /**
     * Contains Content Resolver.
     */
    private ContentResolver resolver = null;
    /**
     * Contains reference of context.
     */
    private Context context;
    /**
     * Contains user id.
     */
    private String userId;

    /**
     * General Constructor.
     * 
     * @param context
     *            reference of context.
     * @param userId
     *            reference of userid.
     */
    public UserConfigDAO(Context context, String userId) {
        this.context = context;
        this.userId = userId;
        this.resolver = this.context.getContentResolver();

    }

    private void registerUserConfigObserver() {
        if (userConfigObserver == null) {
            userConfigObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    allowedAirClassesOfService = null;
                    allowedAirClassesOfServiceRaw = null;
                    flags = null;
                    showGDSNameInSearchResults = null;
                    hash = null;
                }
            };

            context.getContentResolver().registerContentObserver(Config.UserConfigColumns.CONTENT_URI, true,
                    userConfigObserver);
        }
    }

    private void unregisterUserConfigObserver() {
        if (userConfigObserver != null) {
            context.getContentResolver().unregisterContentObserver(userConfigObserver);
            userConfigObserver = null;
        }
    }

    private void registerAttendeeColumnDefinitionObserver() {
        if (this.attendeeColumnDefinitionObserver == null) {
            this.attendeeColumnDefinitionObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    attendeeColumnDefinitions = null;
                }
            };

            context.getContentResolver().registerContentObserver(Config.AttendeeColumnDefinitionColumns.CONTENT_URI,
                    true, attendeeColumnDefinitionObserver);
        }
    }

    private void unregisterAttendeeColumnDefinitionObserver() {
        if (this.attendeeColumnDefinitionObserver != null) {
            context.getContentResolver().unregisterContentObserver(this.attendeeColumnDefinitionObserver);
            this.attendeeColumnDefinitionObserver = null;
        }
    }

    private void registerAttendeeTypeObserver() {
        if (attendeeTypeObserver == null) {
            attendeeTypeObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    attendeeTypes = null;
                }
            };

            context.getContentResolver().registerContentObserver(Config.AttendeeTypeColumns.CONTENT_URI, true,
                    attendeeTypeObserver);
        }
    }

    private void unregisterAttendeeTypeObserver() {
        if (attendeeTypeObserver != null) {
            context.getContentResolver().unregisterContentObserver(attendeeTypeObserver);
            attendeeTypeObserver = null;
        }
    }

    private void registerCarTypeObserver() {
        if (carTypeObserver == null) {
            this.carTypeObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    allowedCarTypes = null;
                }
            };
            context.getContentResolver().registerContentObserver(Config.CarTypeColumns.CONTENT_URI, true,
                    carTypeObserver);

        }
    }

    private void unregisterCarTypeObserver() {
        if (carTypeObserver == null) {
            context.getContentResolver().unregisterContentObserver(carTypeObserver);
            carTypeObserver = null;
        }
    }

    private void registerCurrencyObserver() {
        if (currencyObserver == null) {
            currencyObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    currencies = null;
                    reimbursementCurrencies = null;
                }
            };

            context.getContentResolver().registerContentObserver(Config.CurrencyColumns.CONTENT_URI, true,
                    currencyObserver);
        }
    }

    private void unregisterCurrencyObserver() {
        if (currencyObserver != null) {
            context.getContentResolver().unregisterContentObserver(currencyObserver);
            currencyObserver = null;
        }
    }

    private void registerExpenseConfirmationObserver() {
        if (expenseConfirmationObserver == null) {
            this.expenseConfirmationObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    expenseConfirmations = null;
                }
            };

            context.getContentResolver().registerContentObserver(Config.ExpenseConfirmationColumns.CONTENT_URI, true,
                    expenseConfirmationObserver);
        }

    }

    private void unregisterExpenseConfirmationObserver() {
        if (expenseConfirmationObserver != null) {
            context.getContentResolver().unregisterContentObserver(expenseConfirmationObserver);
            expenseConfirmationObserver = null;

        }
    }

    private void registerPolicyObserver() {
        if (policyObserver == null) {
            this.policyObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    expensePolicies = null;
                }
            };

            context.getContentResolver()
                    .registerContentObserver(Config.PolicyColumns.CONTENT_URI, true, policyObserver);
        }

    }

    private void unregisterPolicyObserver() {
        if (policyObserver != null) {
            context.getContentResolver().unregisterContentObserver(policyObserver);
            policyObserver = null;
        }
    }

    private void registerYodleePaymentTypeObserver() {
        if (yodleePaymentTypeObserver == null) {
            this.yodleePaymentTypeObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    yodleePaymentTypes = null;
                }
            };

            context.getContentResolver().registerContentObserver(Config.YodleePaymentTypeColumns.CONTENT_URI, true,
                    yodleePaymentTypeObserver);

        }
    }

    private void unregisterYodleePaymentTypeObserver() {
        if (yodleePaymentTypeObserver == null) {
            context.getContentResolver().unregisterContentObserver(yodleePaymentTypeObserver);
            yodleePaymentTypeObserver = null;

        }
    }

    private void registerTravelPointsConfigObserver() {
        if (this.travelPointsConfigObserver == null) {
            this.travelPointsConfigObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    travelPointsConfig = null;
                }
            };

            context.getContentResolver().registerContentObserver(Config.TravelPointsConfigColumns.CONTENT_URI, true,
                    travelPointsConfigObserver);
        }
    }

    private void unregisterTravelPointsConfigObserver() {
        if (this.travelPointsConfigObserver != null) {
            context.getContentResolver().unregisterContentObserver(this.travelPointsConfigObserver);
            this.travelPointsConfigObserver = null;
        }
    }

    @Override
    public void finalize() throws Throwable {
        unregisterAttendeeColumnDefinitionObserver();
        unregisterAttendeeTypeObserver();
        unregisterCarTypeObserver();
        unregisterCurrencyObserver();
        unregisterExpenseConfirmationObserver();
        unregisterPolicyObserver();
        unregisterYodleePaymentTypeObserver();
        unregisterTravelPointsConfigObserver();
        unregisterUserConfigObserver();
        super.finalize();
    }

    /**
     * Gets the list of <code>AttendeeColumnDefinition</code> objects specific to the end user's config.
     * 
     * @return the list of <code>AttendeeColumnDefinition</code> objects specific to the end user's config.
     */
    public List<AttendeeColumnDefinitionDAO> getAttendeeColumnDefinitions() {
        if (attendeeColumnDefinitions == null) {
            Cursor cursor = null;
            try {
                attendeeColumnDefinitions = new ArrayList<AttendeeColumnDefinitionDAO>();
                String[] userColumns = { Config.AttendeeColumnDefinitionColumns.ID,
                        Config.AttendeeColumnDefinitionColumns.LABEL, Config.AttendeeColumnDefinitionColumns.DATA_TYPE,
                        Config.AttendeeColumnDefinitionColumns.ACCESS, Config.AttendeeColumnDefinitionColumns.CTRL_TYPE };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.AttendeeColumnDefinitionColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Config.AttendeeColumnDefinitionColumns.CONTENT_URI, userColumns, where,
                        whereArgs, Config.AttendeeColumnDefinitionColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String id = CursorUtil.getStringValue(cursor, Config.AttendeeColumnDefinitionColumns.ID);
                        String dataType = CursorUtil.getStringValue(cursor,
                                Config.AttendeeColumnDefinitionColumns.DATA_TYPE);
                        String label = CursorUtil.getStringValue(cursor, Config.AttendeeColumnDefinitionColumns.LABEL);
                        String access = CursorUtil
                                .getStringValue(cursor, Config.AttendeeColumnDefinitionColumns.ACCESS);
                        String ctrlType = CursorUtil.getStringValue(cursor,
                                Config.AttendeeColumnDefinitionColumns.CTRL_TYPE);
                        AttendeeColumnDefinitionDAO atnColDefDAO = new AttendeeColumnDefinitionDAO(id, label, dataType,
                                ctrlType, access);
                        attendeeColumnDefinitions.add(atnColDefDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerAttendeeColumnDefinitionObserver();
            }
        }
        return attendeeColumnDefinitions;
    }

    /**
     * Gets the list of <code>AttendeeType</code> objects specific to the end user's config.
     * 
     * @return the list of <code>AttendeeType</code> objects specific to the end user's config.
     */
    public List<AttendeeTypeDAO> getAttendeeTypes() {
        if (attendeeTypes == null) {
            Cursor cursor = null;
            try {
                attendeeTypes = new ArrayList<AttendeeTypeDAO>();
                String[] userColumns = { Config.AttendeeTypeColumns.ATN_TYPE_KEY,
                        Config.AttendeeTypeColumns.ATN_TYPE_CODE, Config.AttendeeTypeColumns.ATN_TYPE_NAME,
                        Config.AttendeeTypeColumns.FORM_KEY, Config.AttendeeTypeColumns.ALLOW_EDIT_ATN_COUNT,
                        Config.AttendeeTypeColumns.IS_EXTERNAL };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.AttendeeTypeColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Config.AttendeeTypeColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.AttendeeTypeColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String atnTypeKey = CursorUtil.getStringValue(cursor, Config.AttendeeTypeColumns.ATN_TYPE_KEY);
                        String atnTypeCode = CursorUtil
                                .getStringValue(cursor, Config.AttendeeTypeColumns.ATN_TYPE_CODE);
                        String atnTypeName = CursorUtil
                                .getStringValue(cursor, Config.AttendeeTypeColumns.ATN_TYPE_NAME);
                        String formKey = CursorUtil.getStringValue(cursor, Config.AttendeeTypeColumns.FORM_KEY);
                        Boolean allowEditAtnCount = CursorUtil.getBooleanValue(cursor,
                                Config.AttendeeTypeColumns.ALLOW_EDIT_ATN_COUNT);
                        Boolean isExternal = CursorUtil.getBooleanValue(cursor, Config.AttendeeTypeColumns.IS_EXTERNAL);
                        AttendeeTypeDAO attendeeTypeDAO = new AttendeeTypeDAO(atnTypeKey, atnTypeCode, atnTypeName,
                                formKey, allowEditAtnCount, isExternal);
                        attendeeTypes.add(attendeeTypeDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerAttendeeTypeObserver();

            }
        }
        return attendeeTypes;
    }

    /**
     * Gets the list of allowed <code>CarType</code> objects specific to the end user's config.
     * 
     * @return the list of allowed <code>CarType</code> objects specific to the end user's config.
     */
    public List<CarTypeDAO> getAllowedCarTypes() {
        if (allowedCarTypes == null) {
            Cursor cursor = null;
            try {
                allowedCarTypes = new ArrayList<CarTypeDAO>();
                String[] userColumns = { Config.CarTypeColumns.CODE, Config.CarTypeColumns.DESCRIPTION,
                        Config.CarTypeColumns.IS_DEFAULT };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.CarTypeColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Config.CarTypeColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.CarTypeColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String code = CursorUtil.getStringValue(cursor, Config.CarTypeColumns.CODE);
                        String description = CursorUtil.getStringValue(cursor, Config.CarTypeColumns.DESCRIPTION);
                        Boolean isDefault = CursorUtil.getBooleanValue(cursor, Config.CarTypeColumns.IS_DEFAULT);
                        CarTypeDAO attendeeTypeDAO = new CarTypeDAO(description, code, isDefault);
                        allowedCarTypes.add(attendeeTypeDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerCarTypeObserver();
            }
        }
        return allowedCarTypes;
    }

    /**
     * Gets the list of <code>Currency</code> objects specific to the end user's company.
     * 
     * @return the list of <code>Currency</code> objects specific to the end user's company.
     */
    public List<CurrencyDAO> getCurrencies() {
        if (currencies == null) {
            Cursor cursor = null;
            try {
                currencies = new ArrayList<CurrencyDAO>();
                String[] userColumns = { Config.CurrencyColumns.CRN_CODE, Config.CurrencyColumns.CRN_NAME,
                        Config.CurrencyColumns.DECIMAL_DIGITS };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.CurrencyColumns.USER_ID);
                strBldr.append(" = ? AND ");
                strBldr.append(Config.CurrencyColumns.IS_REIMBURSEMENT);
                strBldr.append(" = 0"); // Boolean is stored as bit integer 0/1
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Config.CurrencyColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.CurrencyColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String crnCode = CursorUtil.getStringValue(cursor, Config.CurrencyColumns.CRN_CODE);
                        String crnName = CursorUtil.getStringValue(cursor, Config.CurrencyColumns.CRN_NAME);
                        Integer digits = CursorUtil.getIntValue(cursor, Config.CurrencyColumns.DECIMAL_DIGITS);
                        CurrencyDAO currencyDAO = new CurrencyDAO(crnCode, crnName, digits);
                        currencies.add(currencyDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerCurrencyObserver();

            }
        }
        return currencies;
    }

    /**
     * Gets the list of reimbursement <code>Currency</code> objects specific to the end user.
     * 
     * @return the list of reimbursement <code>Currency</code> objects specific to the end user.
     */
    public List<CurrencyDAO> getReimbursementCurrencies() {
        if (this.reimbursementCurrencies == null) {
            Cursor cursor = null;

            try {
                reimbursementCurrencies = new ArrayList<CurrencyDAO>();
                String[] userColumns = { Config.CurrencyColumns.CRN_CODE, Config.CurrencyColumns.CRN_NAME,
                        Config.CurrencyColumns.DECIMAL_DIGITS };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.CurrencyColumns.USER_ID);
                strBldr.append(" = ? AND ");
                strBldr.append(Config.CurrencyColumns.IS_REIMBURSEMENT);
                strBldr.append(" = 1"); // Boolean is stored as bit integer 0/1
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Config.CurrencyColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.CurrencyColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String crnCode = CursorUtil.getStringValue(cursor, Config.CurrencyColumns.CRN_CODE);
                        String crnName = CursorUtil.getStringValue(cursor, Config.CurrencyColumns.CRN_NAME);
                        Integer digits = CursorUtil.getIntValue(cursor, Config.CurrencyColumns.DECIMAL_DIGITS);
                        CurrencyDAO currencyDAO = new CurrencyDAO(crnCode, crnName, digits);
                        reimbursementCurrencies.add(currencyDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerCurrencyObserver();
            }
        }
        return reimbursementCurrencies;
    }

    /**
     * Gets the list of <code>ExpenseConfirmation</code> objects specific to the end user's config.
     * 
     * @return the list of <code>ExpenseConfirmation</code> objects specific to the end user's config.
     */
    public List<ExpenseConfirmationDAO> getExpenseConfirmations() {
        if (expenseConfirmations == null) {
            Cursor cursor = null;
            try {
                expenseConfirmations = new ArrayList<ExpenseConfirmationDAO>();
                String[] userColumns = { Config.ExpenseConfirmationColumns.CONFIRMATION_KEY,
                        Config.ExpenseConfirmationColumns.TEXT, Config.ExpenseConfirmationColumns.TITLE };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.ExpenseConfirmationColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Config.ExpenseConfirmationColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.ExpenseConfirmationColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String key = CursorUtil.getStringValue(cursor,
                                Config.ExpenseConfirmationColumns.CONFIRMATION_KEY);
                        String text = CursorUtil.getStringValue(cursor, Config.ExpenseConfirmationColumns.TEXT);
                        String title = CursorUtil.getStringValue(cursor, Config.ExpenseConfirmationColumns.TITLE);
                        ExpenseConfirmationDAO attendeeTypeDAO = new ExpenseConfirmationDAO(key, text, title);
                        expenseConfirmations.add(attendeeTypeDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerExpenseConfirmationObserver();
            }
        }
        return expenseConfirmations;
    }

    /**
     * Gets the list of expense <code>Policy</code> objects specific to the end user's config.
     * 
     * @return the list of expense <code>Policy</code> objects specific to the end user's config.
     */
    public List<PolicyDAO> getExpensePolicies() {
        if (expensePolicies == null) {
            Cursor cursor = null;
            try {
                expensePolicies = new ArrayList<PolicyDAO>();
                String[] userColumns = { Config.PolicyColumns.POL_KEY, Config.PolicyColumns.SUPPORTS_IMAGING,
                        Config.PolicyColumns.APPROVAL_CONFIRMATION_KEY, Config.PolicyColumns.SUBMIT_CONFIRMATION_KEY };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.PolicyColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Config.PolicyColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.PolicyColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String key = CursorUtil.getStringValue(cursor, Config.PolicyColumns.POL_KEY);
                        Boolean supportsImaging = CursorUtil.getBooleanValue(cursor,
                                Config.PolicyColumns.SUPPORTS_IMAGING);
                        String approvalKey = CursorUtil.getStringValue(cursor,
                                Config.PolicyColumns.APPROVAL_CONFIRMATION_KEY);
                        String submitKey = CursorUtil.getStringValue(cursor,
                                Config.PolicyColumns.SUBMIT_CONFIRMATION_KEY);
                        PolicyDAO policyDAO = new PolicyDAO(key, supportsImaging, approvalKey, submitKey);
                        expensePolicies.add(policyDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerPolicyObserver();
            }
        }
        return expensePolicies;
    }

    /**
     * Gets the <code>TravelPointsConfig</code> objects specific to the end user's config.
     * 
     * @return the <code>TravelPointsConfig</code> objects specific to the end user's config.
     */
    public TravelPointsConfigDAO getTravelPointsConfig() {
        if (travelPointsConfig == null) {
            Cursor cursor = null;
            try {
                travelPointsConfig = new ArrayList<TravelPointsConfigDAO>();
                String[] userColumns = { Config.TravelPointsConfigColumns.AIR_TRAVEL_POINTS_ENABLED,
                        Config.TravelPointsConfigColumns.HOTEL_TRAVEL_POINTS_ENABLED };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.TravelPointsConfigColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Config.TravelPointsConfigColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.TravelPointsConfigColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        Boolean airTravelPointsEnabled = CursorUtil.getBooleanValue(cursor,
                                Config.TravelPointsConfigColumns.AIR_TRAVEL_POINTS_ENABLED);
                        Boolean hotelTravelPointsEnabled = CursorUtil.getBooleanValue(cursor,
                                Config.TravelPointsConfigColumns.HOTEL_TRAVEL_POINTS_ENABLED);
                        TravelPointsConfigDAO travelPointsConfigDAO = new TravelPointsConfigDAO(airTravelPointsEnabled,
                                hotelTravelPointsEnabled);
                        travelPointsConfig.add(travelPointsConfigDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerTravelPointsConfigObserver();
            }
        }
        return travelPointsConfig.size() > 0 ? travelPointsConfig.get(0) : null;
    }

    /**
     * Gets the list of <code>YodleePaymentType</code> objects specific to the end user's config.
     * 
     * @return the list of <code>YodleePaymentType</code> objects specific to the end user's config.
     */
    public List<YodleePaymentTypeDAO> getYodleePaymentTypes() {
        if (yodleePaymentTypes == null) {
            Cursor cursor = null;
            try {
                yodleePaymentTypes = new ArrayList<YodleePaymentTypeDAO>();
                String[] userColumns = { Config.YodleePaymentTypeColumns.KEY, Config.YodleePaymentTypeColumns.TEXT };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.YodleePaymentTypeColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Config.YodleePaymentTypeColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.YodleePaymentTypeColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String key = CursorUtil.getStringValue(cursor, Config.YodleePaymentTypeColumns.KEY);
                        String text = CursorUtil.getStringValue(cursor, Config.YodleePaymentTypeColumns.TEXT);
                        YodleePaymentTypeDAO dao = new YodleePaymentTypeDAO(key, text);
                        yodleePaymentTypes.add(dao);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerYodleePaymentTypeObserver();
            }
        }
        return yodleePaymentTypes;
    }

    /**
     * @return whether User Config table data has been loaded
     */
    private boolean isUserConfigLoaded() {
        return flags != null || this.allowedAirClassesOfServiceRaw != null || this.showGDSNameInSearchResults != null;
    }

    /**
     * Lazy loading of User Config table data
     */
    private void loadUserConfig() {
        Cursor cursor = null;
        try {
            String[] userColumns = { Config.UserConfigColumns.FLAGS,
                    Config.UserConfigColumns.ALLOWED_AIR_CLASSES_OF_SERVICE,
                    Config.UserConfigColumns.SHOW_GDS_NAME_IN_SEARCH_RESULTS };

            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Config.ReasonCodeColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Config.UserConfigColumns.CONTENT_URI, userColumns, where, whereArgs,
                    Config.UserConfigColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    flags = CursorUtil.getStringValue(cursor, Config.UserConfigColumns.FLAGS);
                    this.allowedAirClassesOfServiceRaw = CursorUtil.getStringValue(cursor,
                            Config.UserConfigColumns.ALLOWED_AIR_CLASSES_OF_SERVICE);
                    // allow string split statement to continue and indicates data has been loaded
                    if (this.allowedAirClassesOfServiceRaw == null)
                        this.allowedAirClassesOfServiceRaw = "";
                    this.allowedAirClassesOfService = Arrays.asList(this.allowedAirClassesOfServiceRaw.split(" "));
                    this.showGDSNameInSearchResults = CursorUtil.getBooleanValue(cursor,
                            Config.UserConfigColumns.SHOW_GDS_NAME_IN_SEARCH_RESULTS);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // initiate observer.
            registerUserConfigObserver();
        }

        Log.d(Const.LOG_TAG, CLS_TAG + ".loadUserConfig: fetch User Config from db");

    }

    /**
     * Gets flags
     * 
     * @return returns flags.
     */
    public String getFlags() {
        if (!isUserConfigLoaded()) {
            loadUserConfig();
        }
        return flags;
    }

    /**
     * Gets flags
     * 
     * @return returns flags.
     */
    public List<String> getAllowedAirClassesOfService() {
        if (!isUserConfigLoaded()) {
            loadUserConfig();
        }
        return allowedAirClassesOfService;
    }

    /**
     * Gets showGDSNameInSearchResults flag
     * 
     * @return returns showGDSNameInSearchResults flag.
     */
    public Boolean getshowGDSNameInSearchResults() {
        if (!isUserConfigLoaded()) {
            loadUserConfig();
        }
        return showGDSNameInSearchResults;
    }

    /**
     * Get User Id.
     * 
     * @return returns user id.
     */
    public String getUserId() {
        return userId;
    }

}
