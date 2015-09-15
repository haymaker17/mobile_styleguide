/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.util.net;

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.authentication.SiteSettingInfo;

/**
 * Utility class used for getting/setting the user's <code>SiteSetting</code> values. These are the values from the Login
 * response.
 * 
 * @author Chris N. Diaz
 * 
 */
public final class SiteSettings {

    public enum SiteSetting {
        // Add more <SiteSetting> here:
        IS_DATE_EDITABLE("IS_DATE_EDITABLE"),
        ALLOW_TRANS_DELETE("ALLOW_TRANS_DELETE"),
        PERSONAL_CAR_MILEAGE_ON_HOME("PersonalCarMileageOnHome"),
        HIDE_RECEIPT_STORE("HIDE_RECEIPT_STORE"),
        LOCATE_AND_ALERT("LocateAndAlert"),
        SHOW_NONREFUNDABLE_MESSAGE("ShowNonrefundableMessage"),
        VIEW_PICKLIST_CODES("MobileViewPicklistCodes"),
        ALLOW_APPROVALS("AllowApprovals"),
        ALLOW_REPORTS("AllowReports"),
        ALLOW_TRAVEL_BOOKING("AllowTravelBooking"),
        ENABLE_VOICE_BOOKING("VoiceBookingEnabled"),
        ENABLE_HOTEL_SEARCH_STREAM("StreamHotelSearchResults"),
        HAS_FIXED_TA("HasFixedTA"),
        HAS_TRAVEL_ALLOWANCE_FIXED("HasTravelAllowanceFixed"),
        ENABLE_CONDITIONAL_FIELD_EVALUATION("ENABLE_DYNAMIC_FIELD_EVALUATION"),
        ENABLE_SPDY("EnableSpdy"),
        SHOW_JARVIS_HOTEL_UI("ShowJarvisHotelUIOnAndroid"),
        ENABLE_EXPENSE_IT_EXPERIENCE("EnableExpenseIt");

        // ****** enum fields and methods ****** //

        private final String name;
        public String type;
        public String value;

        SiteSetting(String name) {
            this.name = name;
        }

        /**
         * Clears out all the SiteSetting enum values. This should be called after every login in case the values or user/account
         * changed.
         */
        public static void clear() {
            for (SiteSetting setting : values()) {
                setting.type = null;
                setting.value = null;
            }
        }

        /**
         * Sets the SiteSetting enum with the given <code>name</code> the specified <type>type</code> and <code>value</code>.
         * 
         * @param name
         *            the SiteSetting <code>Name</code>
         * @param type
         *            the SiteSetting <code>Type</code>
         * @param value
         *            the SiteSetting <code>Value</code>
         */
        public static void init(String name, String type, String value) {
            for (SiteSetting setting : values()) {
                if (setting.name.equals(name)) {
                    setting.type = type;
                    setting.value = value;
                    break;
                }
            }
        }
    }

    private static final String CLS_TAG = SiteSettings.class.getSimpleName();

    /**
     * Single instance of this util class.
     */
    private final static SiteSettings instance = new SiteSettings();

    /**
     * Private constructor to ensure single instance.
     */
    private SiteSettings() {
        // empty
    }

    /**
     * Returns single instance of this util class.
     * 
     * @return single instance of this util class.
     */
    public static SiteSettings getInstance() {
        return instance;
    }

    /**
     * Initializes the user's <code>SiteSettings</code>. This should only be called from the Login reseponse.
     * 
     * @param siteSettings
     */
    public static void init(Node siteSettings) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".init");

        // First clear the old settings.
        SiteSetting.clear();

        if (siteSettings != null) {
            NodeList list = siteSettings.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                NodeList siteSetting = list.item(i).getChildNodes();
                if (siteSetting.getLength() == 3) {
                    String name = siteSetting.item(0).getChildNodes().item(0).getNodeValue();
                    String type = siteSetting.item(1).getChildNodes().item(0).getNodeValue();
                    String value = siteSetting.item(2).getChildNodes().item(0).getNodeValue();

                    Log.d(Const.LOG_TAG, CLS_TAG + ".init: setting SiteSetting values: " + name + ", " + type + ", "
                            + value);

                    SiteSetting.init(name, type, value);
                }
            }
        }
    }

    /**
     * Initializes the user's <code>SiteSettingInfo</code>. This should only be called from the <code>LoginPassword</code> after
     * <code>AutoLoginRequestTask</code> success.
     * 
     * @param siteSettings
     */
    public static void initWithSiteSetting(List<SiteSettingInfo> siteSettings) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".init");

        // First clear the old settings.
        SiteSetting.clear();

        if (siteSettings != null) {
            for (int i = 0; i < siteSettings.size(); i++) {
                SiteSettingInfo siteSetting = siteSettings.get(i);
                if (siteSetting != null) {
                    String name = siteSetting.getName();
                    String type = siteSetting.getType();
                    String value = siteSetting.getValue();

                    Log.d(Const.LOG_TAG, CLS_TAG + ".init: setting SiteSetting values: " + name + ", " + type + ", "
                            + value);

                    SiteSetting.init(name, type, value);
                }
            }
        }
    }

    /**
     * Helper: return true if the given SiteSetting's type and value match
     */
    private static boolean checkSiteSetting(SiteSetting ss, String type, String value) {
        boolean retVal = false;
        retVal = (ss.type != null && ss.type.equalsIgnoreCase(type) && ss.value != null && ss.value
                .equalsIgnoreCase("y"));
        return retVal;
    }

    /**
     * Returns <code>true</code> if this user's account has "Locate and Alert" feature enabled, otherwise <code>false</code> is
     * return.
     * 
     * @return <code>true</code> if this user's account has "Locate and Alert" feature enabled, otherwise <code>false</code> is
     *         return.
     */
    public boolean isLocationCheckInEnabled() {
        return checkSiteSetting(SiteSetting.LOCATE_AND_ALERT, "otmodule", "y");
    }

    /**
     * Returns <code>true</code> if this user's account has "Report Approvals" feature enabled, otherwise <code>false</code> is
     * return.
     * 
     * @return <code>true</code> if this user's account has "Report Approvals" feature enabled, otherwise <code>false</code> is
     *         return.
     */
    public boolean isAllowApprovalsEnabled() {
        return checkSiteSetting(SiteSetting.ALLOW_APPROVALS, "otmodule", "y");
    }

    /**
     * Returns <code>true</code> if this user's account has "Reports" feature enabled, otherwise <code>false</code> is return.
     * 
     * @return <code>true</code> if this user's account has "Reports" feature enabled, otherwise <code>false</code> is return.
     */
    public boolean isAllowReportsEnabled() {
        return checkSiteSetting(SiteSetting.ALLOW_REPORTS, "otmodule", "y");
    }

    /**
     * Returns <code>true</code> if this user's account has "Travel Booking" feature enabled, otherwise <code>false</code> is
     * return.
     * 
     * @return <code>true</code> if this user's account has "Travel Booking" feature enabled, otherwise <code>false</code> is
     *         return.
     */
    public boolean isAllowTravelBookingEnabled() {
        return checkSiteSetting(SiteSetting.ALLOW_TRAVEL_BOOKING, "otmodule", "y");
    }

    /**
     * Gets whether editing of card transactions is enabled.
     * 
     * @return whether editing of card transactions is enabled.
     */
    public boolean isCardTransDateEditEnabled() {
        return checkSiteSetting(SiteSetting.IS_DATE_EDITABLE, "card", "y");
    }

    /**
     * Gets whether card transaction delete is enabled.
     * 
     * @return whether card transaction delete is enabled.
     */
    public boolean isCardTransDeleteEnabled() {
        // boolean retVal = false;
        //
        // retVal = (SiteSetting.ALLOW_TRANS_DELETE.type != null
        // && SiteSetting.ALLOW_TRANS_DELETE.type.equalsIgnoreCase("card")
        // && SiteSetting.ALLOW_TRANS_DELETE.value != null && SiteSetting.ALLOW_TRANS_DELETE.value
        // .equalsIgnoreCase("y"));
        //
        // return retVal;

        // MOB-13675
        // Currently mobile platforms do not support deletion of any card transactions.
        return false;
    }

    /**
     * Gets whether showing personal card mileage on home screen is enabled.
     * 
     * @return whether showing personal card mileage on home screen is enabled.
     */
    public boolean isPersonalCardMileageOnHomeEnabled() {
        return checkSiteSetting(SiteSetting.PERSONAL_CAR_MILEAGE_ON_HOME, "mobile", "y");
    }

    /**
     * Gets whether the receipt store should be hidden from the end-user.
     * 
     * @return whether the receipt store should be hidden from the end-user.
     */
    public boolean isReceiptStoreHidden() {
        return checkSiteSetting(SiteSetting.HIDE_RECEIPT_STORE, "mobile", "y");
    }

    /**
     * Gets whether the user has a Fixed TA Configuration
     * 
     * @return
     */
    public boolean hasFixedTa() {
        return checkSiteSetting(SiteSetting.HAS_FIXED_TA, "mobile", "y");
    }

    /**
     * Gets whether the user has a Fixed TA Configuration
     *
     * @return
     */
    public boolean hasTravelAllowanceFixed() {
        return checkSiteSetting(SiteSetting.HAS_TRAVEL_ALLOWANCE_FIXED, "mobile", "y");
    }

    /**
     * Gets whether or not a non-refundable message warning should be displayed when making a non-refundable booking.
     * 
     * @return returns whether or not a non-refundable warning message should be displayed when making a booking.
     */
    public boolean isShowNonRefundableMessageEnabled() {
        return checkSiteSetting(SiteSetting.SHOW_NONREFUNDABLE_MESSAGE, "mobile", "y");
    }

    /**
     * Gets whether the search lists should show code+label or just label
     */
    public boolean shouldShowListCodes() {
        return checkSiteSetting(SiteSetting.VIEW_PICKLIST_CODES, "otmodule", "y");
    }

    /**
     * Gets whether or not to allow the user to search/book travel via Voice.
     * 
     * @return <code>true</code> to allow the user to search/book travel by Voice.
     */
    public boolean isVoiceBookingEnabled() {
        return checkSiteSetting(SiteSetting.ENABLE_VOICE_BOOKING, "mobile", "y");
    }

    /**
     * Gets whether conditional field feature is enabled.
     * 
     * @return <code>true</code> to allow make conditional fields calls
     */
    public boolean isConditionalFieldEvaluationEnabled() {
        return checkSiteSetting(SiteSetting.ENABLE_CONDITIONAL_FIELD_EVALUATION, "ui_modification", "y");
    }

    /**
     * Gets whether Spdy flag is enabled.
     * 
     * @return <code>true</code> to allow make conditional fields calls
     */
    public boolean isSpdyEnabled() {
        return checkSiteSetting(SiteSetting.ENABLE_SPDY, "otmodule", "y");
    }

    /**
     * Gets whether ShowHotelJarvisUIOnAndroid flag is enabled.
     *
     * @return <code>true</code> to allow make conditional fields calls
     */
    public boolean shouldShowHotelJarvisUI() {
        return checkSiteSetting(SiteSetting.SHOW_JARVIS_HOTEL_UI, "otmodule", "y");
    }

    /**
     * Gets ExpenseIt experience flag.
     *
     * @return <code>true</code> to allow make conditional fields calls
     */
    public boolean isExpenseItExperienceEnabled() {
        return checkSiteSetting(SiteSetting.ENABLE_EXPENSE_IT_EXPERIENCE, "otmodule", "y");
    }
}
