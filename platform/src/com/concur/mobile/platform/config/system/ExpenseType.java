package com.concur.mobile.platform.config.system;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing an <code>ExpenseType</code> object.
 */
public class ExpenseType extends BaseParser {

    private static final String CLS_TAG = "ExpenseType";

    // tags.
    private static final String TAG_EXP_CODE = "ExpCode";
    private static final String TAG_EXP_KEY = "ExpKey";
    private static final String TAG_EXP_NAME = "ExpName";
    private static final String TAG_FORM_KEY = "FormKey";
    private static final String TAG_HAS_POST_AMT_CALC = "HasPostAmtCalc";
    private static final String TAG_HAS_TAX_FORM = "HasTaxForm";
    private static final String TAG_ITEMIZATION_UNALLOW_EXP_KEYS = "ItemizationUnallowExpKeys";
    private static final String TAG_ITEMIZATION_FORM_KEY = "ItemizeFormKey";
    private static final String TAG_ITEMIZE_STYLE = "ItemizeStyle";
    private static final String TAG_ITEMIZE_TYPE = "ItemizeType";
    private static final String TAG_PARENT_EXP_KEY = "ParentExpKey";
    private static final String TAG_PARENT_EXP_NAME = "ParentExpName";
    private static final String TAG_SUPPORTS_ATTENDEES = "SupportsAttendees";
    private static final String TAG_VENDOR_LIST_KEY = "VendorListKey";

    private static final String TAG_ALLOW_EDIT_ATN_AMT = "AllowEditAtnAmt";
    private static final String TAG_ALLOW_EDIT_ATN_COUNT = "AllowEditAtnCount";
    private static final String TAG_ALLOW_NO_SHOWS = "AllowNoShows";
    private static final String TAG_DISPLAY_ADD_ATN_ON_FORM = "DisplayAddAtnOnForm";
    private static final String TAG_DISPLAY_ATN_AMOUNTS = "DisplayAtnAmounts";
    private static final String TAG_USER_AS_ATN_DEFAULT = "UserAsAtnDefault";
    private static final String TAG_UNALLOW_ATN_TYPE_KEYS = "UnallowAtnTypeKeys";

    // tag codes.
    private static final int TAG_EXP_CODE_CODE = 0;
    private static final int TAG_EXP_KEY_CODE = 1;
    private static final int TAG_EXP_NAME_CODE = 2;
    private static final int TAG_FORM_KEY_CODE = 3;
    private static final int TAG_HAS_POST_AMT_CALC_CODE = 4;
    private static final int TAG_HAS_TAX_FORM_CODE = 5;
    private static final int TAG_ITEMIZATION_UNALLOW_EXP_KEYS_CODE = 6;
    private static final int TAG_ITEMIZATION_FORM_KEY_CODE = 7;
    private static final int TAG_ITEMIZE_STYLE_CODE = 8;
    private static final int TAG_ITEMIZE_TYPE_CODE = 9;
    private static final int TAG_PARENT_EXP_KEY_CODE = 10;
    private static final int TAG_PARENT_EXP_NAME_CODE = 11;
    private static final int TAG_SUPPORTS_ATTENDEES_CODE = 12;
    private static final int TAG_VENDOR_LIST_KEY_CODE = 13;
    private static final int TAG_ALLOW_EDIT_ATN_AMT_CODE = 14;
    private static final int TAG_ALLOW_EDIT_ATN_COUNT_CODE = 15;
    private static final int TAG_ALLOW_NO_SHOWS_CODE = 16;
    private static final int TAG_DISPLAY_ADD_ATN_ON_FORM_CODE = 17;
    private static final int TAG_DISPLAY_ATN_AMOUNTS_CODE = 18;
    private static final int TAG_USER_AS_ATN_DEFAULT_CODE = 19;
    private static final int TAG_UNALLOW_ATN_TYPE_KEYS_CODE = 20;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_EXP_CODE, TAG_EXP_CODE_CODE);
        tagMap.put(TAG_EXP_KEY, TAG_EXP_KEY_CODE);
        tagMap.put(TAG_EXP_NAME, TAG_EXP_NAME_CODE);
        tagMap.put(TAG_FORM_KEY, TAG_FORM_KEY_CODE);
        tagMap.put(TAG_HAS_POST_AMT_CALC, TAG_HAS_POST_AMT_CALC_CODE);
        tagMap.put(TAG_HAS_TAX_FORM, TAG_HAS_TAX_FORM_CODE);
        tagMap.put(TAG_ITEMIZATION_UNALLOW_EXP_KEYS, TAG_ITEMIZATION_UNALLOW_EXP_KEYS_CODE);
        tagMap.put(TAG_ITEMIZATION_FORM_KEY, TAG_ITEMIZATION_FORM_KEY_CODE);
        tagMap.put(TAG_ITEMIZE_STYLE, TAG_ITEMIZE_STYLE_CODE);
        tagMap.put(TAG_ITEMIZE_TYPE, TAG_ITEMIZE_TYPE_CODE);
        tagMap.put(TAG_PARENT_EXP_KEY, TAG_PARENT_EXP_KEY_CODE);
        tagMap.put(TAG_PARENT_EXP_NAME, TAG_PARENT_EXP_NAME_CODE);
        tagMap.put(TAG_SUPPORTS_ATTENDEES, TAG_SUPPORTS_ATTENDEES_CODE);
        tagMap.put(TAG_VENDOR_LIST_KEY, TAG_VENDOR_LIST_KEY_CODE);
        tagMap.put(TAG_ALLOW_EDIT_ATN_AMT, TAG_ALLOW_EDIT_ATN_AMT_CODE);
        tagMap.put(TAG_ALLOW_EDIT_ATN_COUNT, TAG_ALLOW_EDIT_ATN_COUNT_CODE);
        tagMap.put(TAG_ALLOW_NO_SHOWS, TAG_ALLOW_NO_SHOWS_CODE);
        tagMap.put(TAG_DISPLAY_ADD_ATN_ON_FORM, TAG_DISPLAY_ADD_ATN_ON_FORM_CODE);
        tagMap.put(TAG_DISPLAY_ATN_AMOUNTS, TAG_DISPLAY_ATN_AMOUNTS_CODE);
        tagMap.put(TAG_USER_AS_ATN_DEFAULT, TAG_USER_AS_ATN_DEFAULT_CODE);
        tagMap.put(TAG_UNALLOW_ATN_TYPE_KEYS, TAG_UNALLOW_ATN_TYPE_KEYS_CODE);
    }

    /**
     * Contains the expense code.
     */
    public String expCode;

    /**
     * Contains the expense key.
     */
    public String expKey;

    /**
     * Contains the expense name.
     */
    public String expName;

    /**
     * Contains the form key.
     */
    public Integer formKey;

    /**
     * Contains whether or not there is a post amount calculation.
     */
    public Boolean hasPostAmtCalc;

    /**
     * Contains whether or not there is a tax form.
     */
    public Boolean hasTaxForm;

    /**
     * Contains the list of expense keys that are not permitted itemizations of this expense type.
     */
    public String itemizationUnallowExpKeys;

    /**
     * Contains the itemize form key.
     */
    public Integer itemizeFormKey;

    /**
     * Contains the itemize style.
     */
    public String itemizeStyle;

    /**
     * Contains the itemize type.
     */
    public String itemizeType;

    /**
     * Contains the parent expense key.
     */
    public String parentExpKey;

    /**
     * Contains the parent expense name.
     */
    public String parentExpName;

    /**
     * Contains whether this expense type supports attendees.
     */
    public Boolean supportsAttendees;

    /**
     * Contains the vendor list key.
     */
    public Integer vendorListKey;

    /**
     * Contains the "allow edit attendee amount" value.
     */
    public Boolean allowEditAtnAmt;

    /**
     * Contains the "allow edit attendee count" value.
     */
    public Boolean allowEditAtnCount;

    /**
     * Contains the "allow no shows" value.
     */
    public Boolean allowNoShows;

    /**
     * Contains the "display add attendee on form" value.
     */
    public Boolean displayAddAtnOnForm;

    /**
     * Contains the "display attendee amounts" value.
     */
    public Boolean displayAtnAmounts;

    /**
     * Contains the "user as attendee default" value.
     */
    public Boolean userAsAtnDefault;

    /**
     * Contains the "unallowed attendee type keys" value.
     */
    public String unallowAtnTypeKeys;

    @Override
    public void handleText(String tag, String text) {

        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_EXP_CODE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    expCode = text.trim();
                }
                break;
            }
            case TAG_EXP_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    expKey = text.trim();
                }
                break;
            }
            case TAG_EXP_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    expName = text.trim();
                }
                break;
            }
            case TAG_FORM_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    formKey = Parse.safeParseInteger(text.trim());
                    if (formKey == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse expense type form key.");
                    }
                }
                break;
            }
            case TAG_HAS_POST_AMT_CALC_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    hasPostAmtCalc = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_HAS_TAX_FORM_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    hasTaxForm = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_ITEMIZATION_UNALLOW_EXP_KEYS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    itemizationUnallowExpKeys = text.trim();
                }
                break;
            }
            case TAG_ITEMIZATION_FORM_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    itemizeFormKey = Parse.safeParseInteger(text.trim());
                    if (itemizeFormKey == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse expense type itemize form key.");
                    }
                }
                break;
            }
            case TAG_ITEMIZE_STYLE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    itemizeStyle = text.trim();
                }
                break;
            }
            case TAG_ITEMIZE_TYPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    itemizeType = text.trim();
                }
                break;
            }
            case TAG_PARENT_EXP_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    parentExpKey = text.trim();
                }
                break;
            }
            case TAG_PARENT_EXP_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    parentExpName = text.trim();
                }
                break;
            }
            case TAG_SUPPORTS_ATTENDEES_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    supportsAttendees = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_VENDOR_LIST_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    vendorListKey = Parse.safeParseInteger(text.trim());
                    if (vendorListKey == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse expense type vendor list key.");
                    }
                }
                break;
            }
            case TAG_ALLOW_EDIT_ATN_AMT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    allowEditAtnAmt = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_ALLOW_EDIT_ATN_COUNT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    allowEditAtnCount = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_ALLOW_NO_SHOWS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    allowNoShows = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_DISPLAY_ADD_ATN_ON_FORM_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    displayAddAtnOnForm = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_DISPLAY_ATN_AMOUNTS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    displayAtnAmounts = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_USER_AS_ATN_DEFAULT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    userAsAtnDefault = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_UNALLOW_ATN_TYPE_KEYS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    unallowAtnTypeKeys = text.trim();
                }
                break;
            }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
            }
        }
    }

}
