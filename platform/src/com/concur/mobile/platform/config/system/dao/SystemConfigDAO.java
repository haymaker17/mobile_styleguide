package com.concur.mobile.platform.config.system.dao;

import java.util.ArrayList;
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
 * Provides a data access object of System Configuration Information.
 * 
 * @author sunill
 */
public class SystemConfigDAO {

    private static final String CLS_TAG = SystemConfigDAO.class.getSimpleName();
    /**
     * Contains the list of air violation reasons.
     */
    private List<ReasonCodeDAO> airReasons;

    /**
     * Contains the list of hotel violation reasons.
     */
    private List<ReasonCodeDAO> hotelReasons;

    /**
     * Contains the list of car violation reasons.
     */
    private List<ReasonCodeDAO> carReasons;

    /**
     * Contains the list of expense types.
     */
    private List<ExpenseTypeDAO> expenseTypes;

    /**
     * Contains the server-generated hash code for the system config data.
     */
    private String hash;

    /**
     * Contains the response id for this data.
     */
    private String responseId;

    /**
     * Contains the list of company office locations.
     */
    private List<OfficeLocationDAO> officeLocations;

    /**
     * Contains whether the refundable checkbox is checked by default.
     */
    private Boolean refundableCheckboxDefault;

    /**
     * Contains the refundable message.
     */
    private String refundableMessage;

    /**
     * Contains whether the refundable checkbox should be displayed.
     */
    private Boolean refundableShowCheckbox;

    /**
     * Contains whether or not an explanation is required for a rule violation.
     */
    private Boolean ruleViolationExplanationRequired;
    /**
     * Contains System Config Content Observer.
     */
    private ContentObserver sysConfigObserver;

    /**
     * Contains Office Location Content Observer.
     */
    private ContentObserver officeLocationObserver;

    /**
     * Contains Expense Type Content Observer.
     */
    private ContentObserver expenseTypeObserver;

    /**
     * Contains Reason Code Content Observer.
     */
    private ContentObserver reasonCodeObserver;

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
    public SystemConfigDAO(Context context, String userId) {
        this.context = context;
        this.userId = userId;
        this.resolver = this.context.getContentResolver();
    }

    private void registerSystemConfigObserver() {
        if (sysConfigObserver == null) {
            sysConfigObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    refundableCheckboxDefault = null;
                    refundableMessage = null;
                    refundableShowCheckbox = null;
                    ruleViolationExplanationRequired = null;
                    hash = null;
                }
            };

            context.getContentResolver().registerContentObserver(Config.SystemConfigColumns.CONTENT_URI, true,
                    sysConfigObserver);
        }
    }

    private void unregisterSystemConfigObserver() {
        if (sysConfigObserver != null) {
            context.getContentResolver().unregisterContentObserver(sysConfigObserver);
            sysConfigObserver = null;
        }
    }

    private void registerOfficeLocationObserver() {
        if (officeLocationObserver == null) {
            officeLocationObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    officeLocations = null;
                }
            };

            context.getContentResolver().registerContentObserver(Config.AttendeeTypeColumns.CONTENT_URI, true,
                    officeLocationObserver);
        }
    }

    private void unregisterOfficeLocationObserver() {
        if (officeLocationObserver != null) {
            context.getContentResolver().unregisterContentObserver(officeLocationObserver);
            officeLocationObserver = null;
        }
    }

    private void registerReasonCodeObserver() {
        if (reasonCodeObserver == null) {
            reasonCodeObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    airReasons = null;
                    hotelReasons = null;
                    carReasons = null;
                }
            };

            context.getContentResolver().registerContentObserver(Config.AttendeeTypeColumns.CONTENT_URI, true,
                    reasonCodeObserver);
        }
    }

    private void unregisterReasonCodeObserver() {
        if (reasonCodeObserver != null) {
            context.getContentResolver().unregisterContentObserver(reasonCodeObserver);
            reasonCodeObserver = null;
        }
    }

    private void registerExpenseTypeObserver() {
        if (expenseTypeObserver == null) {
            expenseTypeObserver = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    expenseTypes = null;
                }
            };

            context.getContentResolver().registerContentObserver(Config.AttendeeTypeColumns.CONTENT_URI, true,
                    expenseTypeObserver);
        }
    }

    private void unregisterExpenseTypeObserver() {
        if (expenseTypeObserver != null) {
            context.getContentResolver().unregisterContentObserver(expenseTypeObserver);
            expenseTypeObserver = null;
        }
    }

    @Override
    public void finalize() throws Throwable {
        unregisterReasonCodeObserver();
        unregisterOfficeLocationObserver();
        unregisterExpenseTypeObserver();
        unregisterSystemConfigObserver();
        super.finalize();
    }

    /**
     * Gets the list of <code>CompanyLocation</code> objects specific to the end user's company.
     * 
     * @return the list of <code>CompanyLocation</code> objects specific to the end user's company.
     */
    public List<OfficeLocationDAO> getCompanyLocations() {
        if (officeLocations == null) {
            Cursor cursor = null;
            try {
                officeLocations = new ArrayList<OfficeLocationDAO>();
                String[] userColumns = { Config.OfficeLocationColumns.ADDRESS, Config.OfficeLocationColumns.CITY,
                        Config.OfficeLocationColumns.COUNTRY, Config.OfficeLocationColumns.LAT,
                        Config.OfficeLocationColumns.LON, Config.OfficeLocationColumns.STATE };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.OfficeLocationColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Config.OfficeLocationColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.OfficeLocationColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String address = CursorUtil.getStringValue(cursor, Config.OfficeLocationColumns.ADDRESS);
                        String city = CursorUtil.getStringValue(cursor, Config.OfficeLocationColumns.CITY);
                        String country = CursorUtil.getStringValue(cursor, Config.OfficeLocationColumns.COUNTRY);
                        String state = CursorUtil.getStringValue(cursor, Config.OfficeLocationColumns.STATE);
                        Double lat = CursorUtil.getDoubleValue(cursor, Config.OfficeLocationColumns.LAT);
                        Double lon = CursorUtil.getDoubleValue(cursor, Config.OfficeLocationColumns.LON);
                        OfficeLocationDAO officeLocationDAO = new OfficeLocationDAO(address, city, country, state, lat,
                                lon);
                        officeLocations.add(officeLocationDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerOfficeLocationObserver();
            }

        }
        return officeLocations;
    }

    /**
     * Gets the list of <code>ReasonCode</code> objects specific to the end user's car rental company information..
     * 
     * @return the list of <code>CarReason</code> objects specific to the end user's company.
     */
    public List<ReasonCodeDAO> getCarReasons() {
        if (carReasons == null) {
            Cursor cursor = null;
            try {
                carReasons = new ArrayList<ReasonCodeDAO>();
                String[] userColumns = { Config.ReasonCodeColumns.TYPE, Config.ReasonCodeColumns.DESCRIPTION,
                        Config.ReasonCodeColumns.ID, Config.ReasonCodeColumns.VIOLATION_TYPE };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.ReasonCodeColumns.USER_ID);
                strBldr.append(" = ? AND ");
                strBldr.append(Config.ReasonCodeColumns.TYPE);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId, Config.ReasonCodeColumns.TYPE_CAR };

                cursor = resolver.query(Config.ReasonCodeColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.ReasonCodeColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String type = CursorUtil.getStringValue(cursor, Config.ReasonCodeColumns.TYPE);
                        String desc = CursorUtil.getStringValue(cursor, Config.ReasonCodeColumns.DESCRIPTION);
                        String id = CursorUtil.getStringValue(cursor, Config.ReasonCodeColumns.ID);
                        String violationType = CursorUtil.getStringValue(cursor,
                                Config.ReasonCodeColumns.VIOLATION_TYPE);
                        ReasonCodeDAO reasonCodeDAO = new ReasonCodeDAO(type, desc, id, violationType);
                        carReasons.add(reasonCodeDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerReasonCodeObserver();
            }

        }
        return carReasons;
    }

    /**
     * Gets the list of <code>ReasonCode</code> objects specific to the end user's hotel rental company information.
     * 
     * @return the list of <code>ReasonCode</code> objects specific to the end user's company.
     */
    public List<ReasonCodeDAO> getHotelReasons() {
        if (hotelReasons == null) {
            Cursor cursor = null;
            try {
                hotelReasons = new ArrayList<ReasonCodeDAO>();
                String[] userColumns = { Config.ReasonCodeColumns.TYPE, Config.ReasonCodeColumns.DESCRIPTION,
                        Config.ReasonCodeColumns.ID, Config.ReasonCodeColumns.VIOLATION_TYPE };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.ReasonCodeColumns.USER_ID);
                strBldr.append(" = ? AND ");
                strBldr.append(Config.ReasonCodeColumns.TYPE);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId, Config.ReasonCodeColumns.TYPE_HOTEL };

                cursor = resolver.query(Config.ReasonCodeColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.ReasonCodeColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String type = CursorUtil.getStringValue(cursor, Config.ReasonCodeColumns.TYPE);
                        String desc = CursorUtil.getStringValue(cursor, Config.ReasonCodeColumns.DESCRIPTION);
                        String id = CursorUtil.getStringValue(cursor, Config.ReasonCodeColumns.ID);
                        String violationType = CursorUtil.getStringValue(cursor,
                                Config.ReasonCodeColumns.VIOLATION_TYPE);
                        ReasonCodeDAO reasonCodeDAO = new ReasonCodeDAO(type, desc, id, violationType);
                        hotelReasons.add(reasonCodeDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerReasonCodeObserver();

            }
        }
        return hotelReasons;
    }

    /**
     * Gets the list of <code>ReasonCode</code> objects specific to the end user's air booking company information.
     * 
     * @return the list of <code>ReasonCode</code> objects specific to the end user's company.
     */
    public List<ReasonCodeDAO> getAirReasons() {
        if (airReasons == null) {
            Cursor cursor = null;
            try {
                airReasons = new ArrayList<ReasonCodeDAO>();
                String[] userColumns = { Config.ReasonCodeColumns.TYPE, Config.ReasonCodeColumns.DESCRIPTION,
                        Config.ReasonCodeColumns.ID, Config.ReasonCodeColumns.VIOLATION_TYPE };
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.ReasonCodeColumns.USER_ID);
                strBldr.append(" = ? AND ");
                strBldr.append(Config.ReasonCodeColumns.TYPE);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId, Config.ReasonCodeColumns.TYPE_AIR };

                cursor = resolver.query(Config.ReasonCodeColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.ReasonCodeColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String type = CursorUtil.getStringValue(cursor, Config.ReasonCodeColumns.TYPE);
                        String desc = CursorUtil.getStringValue(cursor, Config.ReasonCodeColumns.DESCRIPTION);
                        String id = CursorUtil.getStringValue(cursor, Config.ReasonCodeColumns.ID);
                        String violationType = CursorUtil.getStringValue(cursor,
                                Config.ReasonCodeColumns.VIOLATION_TYPE);
                        ReasonCodeDAO reasonCodeDAO = new ReasonCodeDAO(type, desc, id, violationType);
                        airReasons.add(reasonCodeDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerReasonCodeObserver();
            }
        }
        return airReasons;
    }

    /**
     * Gets the list of <code>ExpenseType</code> objects specific to the end user's company information.
     * 
     * @return the list of <code>ExpenseType</code> objects specific to the end user's company.
     */
    public List<ExpenseTypeDAO> getExpenseTypes() {
        if (expenseTypes == null || expenseTypes.size() == 0) {
            Cursor cursor = null;
            try {
                expenseTypes = new ArrayList<ExpenseTypeDAO>();
                String[] userColumns = { Config.ExpenseTypeColumns.EXP_CODE, Config.ExpenseTypeColumns.EXP_KEY,
                        Config.ExpenseTypeColumns.EXP_NAME, Config.ExpenseTypeColumns.FORM_KEY,
                        Config.ExpenseTypeColumns.HAS_POST_AMT_CALC, Config.ExpenseTypeColumns.HAS_TAX_FORM,
                        Config.ExpenseTypeColumns.ITEMIZATION_UNALLOW_EXP_KEYS,
                        Config.ExpenseTypeColumns.ITEMIZATION_FORM_KEY, Config.ExpenseTypeColumns.ITEMIZATION_STYLE,
                        Config.ExpenseTypeColumns.ITEMIZATION_TYPE, Config.ExpenseTypeColumns.PARENT_EXP_KEY,
                        Config.ExpenseTypeColumns.PARENT_EXP_NAME, Config.ExpenseTypeColumns.SUPPORTS_ATTENDEES,
                        Config.ExpenseTypeColumns.VENDOR_LIST_KEY,
                        Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_AMOUNT,
                        Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_COUNT, Config.ExpenseTypeColumns.ALLOW_NO_SHOWS,
                        Config.ExpenseTypeColumns.DISPLAY_ADD_ATTENDEE_ON_FORM,
                        Config.ExpenseTypeColumns.DISPLAY_ATTENDEE_AMOUNTS,
                        Config.ExpenseTypeColumns.USER_AS_ATTENDEE_DEFAULT,
                        Config.ExpenseTypeColumns.UNALLOW_ATN_TYPE_KEYS };

                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.ExpenseTypeColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Config.ExpenseTypeColumns.CONTENT_URI, userColumns, where, whereArgs,
                        Config.ExpenseTypeColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String expCode = CursorUtil.getStringValue(cursor, Config.ExpenseTypeColumns.EXP_CODE);
                        String expKey = CursorUtil.getStringValue(cursor, Config.ExpenseTypeColumns.EXP_KEY);
                        String expName = CursorUtil.getStringValue(cursor, Config.ExpenseTypeColumns.EXP_NAME);
                        Integer formKey = CursorUtil.getIntValue(cursor, Config.ExpenseTypeColumns.FORM_KEY);
                        Boolean hasPostAmtCalc = CursorUtil.getBooleanValue(cursor,
                                Config.ExpenseTypeColumns.HAS_POST_AMT_CALC);
                        Boolean hasTaxForm = CursorUtil.getBooleanValue(cursor, Config.ExpenseTypeColumns.HAS_TAX_FORM);
                        String itemizationUnallowExpKeys = CursorUtil.getStringValue(cursor,
                                Config.ExpenseTypeColumns.ITEMIZATION_UNALLOW_EXP_KEYS);
                        Integer itemizeFormKey = CursorUtil.getIntValue(cursor,
                                Config.ExpenseTypeColumns.ITEMIZATION_FORM_KEY);
                        String itemizeStyle = CursorUtil.getStringValue(cursor,
                                Config.ExpenseTypeColumns.ITEMIZATION_STYLE);
                        String itemizeType = CursorUtil.getStringValue(cursor,
                                Config.ExpenseTypeColumns.ITEMIZATION_TYPE);
                        String parentExpKey = CursorUtil.getStringValue(cursor,
                                Config.ExpenseTypeColumns.PARENT_EXP_KEY);
                        String parentExpName = CursorUtil.getStringValue(cursor,
                                Config.ExpenseTypeColumns.PARENT_EXP_NAME);
                        Boolean supportsAttendees = CursorUtil.getBooleanValue(cursor,
                                Config.ExpenseTypeColumns.SUPPORTS_ATTENDEES);
                        Integer vendorListKey = CursorUtil.getIntValue(cursor,
                                Config.ExpenseTypeColumns.VENDOR_LIST_KEY);
                        Boolean allowEditAtnAmt = CursorUtil.getBooleanValue(cursor,
                                Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_AMOUNT);
                        Boolean allowEditAtnCount = CursorUtil.getBooleanValue(cursor,
                                Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_COUNT);
                        Boolean allowNoShows = CursorUtil.getBooleanValue(cursor,
                                Config.ExpenseTypeColumns.ALLOW_NO_SHOWS);
                        Boolean displayAddAtnOnForm = CursorUtil.getBooleanValue(cursor,
                                Config.ExpenseTypeColumns.DISPLAY_ADD_ATTENDEE_ON_FORM);
                        Boolean displayAtnAmounts = CursorUtil.getBooleanValue(cursor,
                                Config.ExpenseTypeColumns.DISPLAY_ATTENDEE_AMOUNTS);
                        Boolean userAsAtnDefault = CursorUtil.getBooleanValue(cursor,
                                Config.ExpenseTypeColumns.USER_AS_ATTENDEE_DEFAULT);
                        String unallowAtnTypeKeys = CursorUtil.getStringValue(cursor,
                                Config.ExpenseTypeColumns.UNALLOW_ATN_TYPE_KEYS);

                        ExpenseTypeDAO expenseTypeDAO = new ExpenseTypeDAO(expCode, expKey, expName, formKey,
                                hasPostAmtCalc, hasTaxForm, itemizationUnallowExpKeys, itemizeFormKey, itemizeStyle,
                                itemizeType, parentExpKey, parentExpName, supportsAttendees, vendorListKey,
                                allowEditAtnAmt, allowEditAtnCount, allowNoShows, displayAddAtnOnForm,
                                displayAtnAmounts, userAsAtnDefault, unallowAtnTypeKeys);
                        expenseTypes.add(expenseTypeDAO);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // initiate observer.
                registerExpenseTypeObserver();
            }
        }
        return expenseTypes;
    }

    /**
     * @return whether System Config table data has been loaded
     */
    private boolean isSystemConfigLoaded() {
        return hash != null || ruleViolationExplanationRequired != null || refundableCheckboxDefault != null
                || refundableShowCheckbox != null || refundableMessage != null;
    }

    /**
     * Lazy loading of System Config table data
     */
    private void loadSystemConfig() {
        Cursor cursor = null;
        try {
            String[] userColumns = { Config.SystemConfigColumns.HASH,
                    Config.SystemConfigColumns.RULE_VIOLATION_EXPLANATION_REQUIRED,
                    Config.SystemConfigColumns.REFUND_INFO_CHECKBOX_DEFAULT,
                    Config.SystemConfigColumns.REFUND_INFO_MESSAGE,
                    Config.SystemConfigColumns.REFUND_INFO_SHOW_CHECKBOX };
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Config.ReasonCodeColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Config.SystemConfigColumns.CONTENT_URI, userColumns, where, whereArgs,
                    Config.SystemConfigColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    hash = CursorUtil.getStringValue(cursor, Config.SystemConfigColumns.HASH);
                    ruleViolationExplanationRequired = CursorUtil.getBooleanValue(cursor,
                            Config.SystemConfigColumns.RULE_VIOLATION_EXPLANATION_REQUIRED);
                    refundableCheckboxDefault = CursorUtil.getBooleanValue(cursor,
                            Config.SystemConfigColumns.REFUND_INFO_CHECKBOX_DEFAULT);
                    refundableMessage = CursorUtil.getStringValue(cursor,
                            Config.SystemConfigColumns.REFUND_INFO_MESSAGE);
                    refundableShowCheckbox = CursorUtil.getBooleanValue(cursor,
                            Config.SystemConfigColumns.REFUND_INFO_SHOW_CHECKBOX);

                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // initiate observer.
            registerSystemConfigObserver();
        }

        Log.d(Const.LOG_TAG, CLS_TAG + ".loadSystemConfig: fetch System Config from db");

    }

    /**
     * Gets the server computed hash code representing the information contained in this system configuration object.
     * 
     * @return the server computed hash code representing the information contained in this system configuration object.
     */
    public String getHash() {
        if (!isSystemConfigLoaded()) {
            loadSystemConfig();
        }
        return hash;
    }

    /**
     * Gets whether or not a violation justification is required when booking a fare outside of company policy.
     * 
     * @return returns whether a violation justification is required when booking a fare outside of company policy.
     */
    public Boolean getRuleViolationExplanationRequired() {
        if (!isSystemConfigLoaded()) {
            loadSystemConfig();
        }
        return ruleViolationExplanationRequired;
    }

    /**
     * Gets whether or not a refundable checkbox is checked by default.
     * 
     * @return returns whether or not a refundable checkbox is checked by default.
     */
    public Boolean getRefundableCheckboxDefault() {
        if (!isSystemConfigLoaded()) {
            loadSystemConfig();
        }

        return refundableCheckboxDefault;
    }

    /**
     * Gets refundable message
     * 
     * @return returns refundable message.
     */
    public String getRefundableMessage() {
        if (!isSystemConfigLoaded()) {
            loadSystemConfig();
        }
        return refundableMessage;
    }

    /**
     * Gets whether or not the refundable checkbox should be displayed.
     * 
     * @return returns whether refundable checkbox should be displayed.
     */
    public Boolean getRefundableShowCheckbox() {
        if (!isSystemConfigLoaded()) {
            loadSystemConfig();
        }
        return refundableShowCheckbox;
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
