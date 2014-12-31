package com.concur.mobile.platform.ui.common.util;

import java.util.Arrays;
import java.util.List;

import android.graphics.Bitmap.CompressFormat;

public class Const {

    // Instantiation is a crime.
    private Const() {
    }

    // -------------------------------------------------
    // General
    // -------------------------------------------------
    public static final String LOG_TAG = "CNQR.PLATFORM.UI.COMMON";

    // Broadcast action/extra related to on-going network activity.
    public static final String ACTION_NETWORK_ACTIVITY_START = "com.concur.mobile.action.network.activity.start";
    public static final String ACTION_NETWORK_ACTIVITY_STOP = "com.concur.mobile.action.network.activity.stop";
    public static final String ACTION_NETWORK_ACTIVITY_TYPE = "com.concur.mobile.action.network.activity.type";
    public static final String ACTION_NETWORK_ACTIVITY_TEXT = "com.concur.mobile.action.network.activity.text";

    // -------------------------------------------------
    // Supported Login methods
    // -------------------------------------------------
    public static final String LOGIN_METHOD_SSO = "SSO";
    public static final String LOGIN_METHOD_MOBILE_PASSWORD = "MobilePassword";
    public static final String LOGIN_METHOD_PASSWORD = "Password";
    public static final String EXTRA_LOGOUT = "logout";

    /**
     * MOB-11669 A 'flagged' phone is one that has a keyboard layout that doesn't always show the proper symbols for currency
     * input. <br>
     */
    public static final List<String> FLAGGED_LOCALE_KEYBOARD_LAYOUT = Arrays.asList(new String[] { "SAMSUNG" });

    public static final String EXTRA_COMBO_BOX_ACTION = "combo_box_action";
    public static final String EXTRA_COMBO_BOX_INLINE_TEXT = "combo_box_inline_text";
    public static final int COMBO_BOX_INLINE_TEXT = 0;
    public static final int COMBO_BOX_LIST_SELECTION = 1;

    // -------------------------------------------------
    // Extra Intent Values Affecting Expense List Search
    // -------------------------------------------------
    public static final String EXTRA_LIST_SEARCH_IS_MRU = "list.search.is.mru";
    public static final String EXTRA_LIST_SEARCH_FIELD_ID = "list.search.field.id";
    public static final String EXTRA_LIST_SEARCH_FT_CODE = "list.search.ft.code";
    public static final String EXTRA_LIST_SEARCH_LIST_KEY = "list.search.list.key";
    public static final String EXTRA_LIST_SEARCH_PARENT_LI_KEY = "list.search.parent.li.key";
    public static final String EXTRA_LIST_SEARCH_REPORT_KEY = "list.search.report.key";
    public static final String EXTRA_LIST_SELECTED_LIST_ITEM_KEY = "list.search.selected.list.item.key";
    public static final String EXTRA_LIST_SELECTED_LIST_ITEM_CODE = "list.search.selected.list.item.code";
    public static final String EXTRA_LIST_SELECTED_LIST_ITEM_TEXT = "list.search.selected.list.item.text";
    public static final String EXTRA_LIST_SELECTED_LIST_ITEM_CRN_CODE = "list.search.selected.list.item.crn.code";
    public static final String EXTRA_LIST_SELECTED_LIST_ITEM_CRN_KEY = "list.search.selected.list.item.crn.key";
    public static final String EXTRA_LIST_SEARCH_TITLE = "list.search.title";
    public static final String EXTRA_LIST_SEARCH_EXCLUDE_KEYS = "list.search.exclude.keys";
    public static final String EXTRA_LIST_SHOW_CODES = "list.search.show.codes";
    public static final String EXTRA_LIST_SEARCH_STATIC_LIST = "list.search.static.list";

    public static final String EXTRA_SEARCH_SELECTED_ITEM = "search.selected.item";

    // Contains the expiration in milliseconds for using the last saved
    // transaction date and location.
    public static final long LAST_TRANS_DATE_LOCATION_EXPIRATION_MILLISECONDS = (8 * 60 * 60 * 1000L);

    // ===============================
    // Sticky broadcast intent action indicating application gained/lost connectivity.
    // ===============================
    public static final String ACTION_DATA_CONNECTIVITY_AVAILABLE = "com.concur.mobile.action.data.available";
    public static final String ACTION_DATA_CONNECTIVITY_UNAVAILABLE = "com.concur.mobile.action.data.unavailable";

    // ===============================
    // Global Preference Keys
    // ===============================

    // Contains the last saved date.
    public static final String PREF_LAST_SAVED_DATE = "pref_last_date";
    // Contains the timestamp of the last saved date.
    public static final String PREF_LAST_SAVED_DATE_TIME = "pref_last_date_time";
    // Contains the last saved location selection (report entry).
    public static final String PREF_LAST_SAVED_LOCATION_SELECTION = "pref_last_location_selection";
    // Contains the timestamp of the last saved location selection.
    public static final String PREF_LAST_SAVED_LOCATION_SELECTION_TIME = "pref_last_location_selection_time";
    // Contains the last saved location (quick expense).
    public static final String PREF_LAST_SAVED_LOCATION = "pref_last_location";
    // Contains the timestamp of the last saved location (quick expense).
    public static final String PREF_LAST_SAVED_LOCATION_TIME = "pref_last_location_time";
    /**
     * Key to indicate the user's last used CRN code.
     */
    public static final String PREF_LAST_USED_CRN_CODE = "pref_last_used_crn_code";
    /**
     * Contains whether or not the Receipt Store UI should be hidden from the end-user.
     */
    public static final String PREF_RECEIPT_STORE_HIDDEN = "pref_receipt_store_hidden";

    // ===============================
    // Receipt Image Generation constants.
    // ===============================

    // Contains the image source pixel sample size used to reduce the size of a
    // receipt.
    public static final int RECEIPT_SOURCE_BITMAP_SAMPLE_SIZE = 2;
    // Contains the receipt output compression quality (value from 1 to 100).
    public static final int RECEIPT_COMPRESS_BITMAP_QUALITY = 90;
    // Contains the receipt output compression format.
    public static final CompressFormat RECEIPT_COMPRESS_BITMAP_FORMAT = CompressFormat.JPEG;

}
