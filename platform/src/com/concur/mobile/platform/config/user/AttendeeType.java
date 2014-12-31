package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a model of an attendee type.
 */
public class AttendeeType extends BaseParser {

    private static final String CLS_TAG = "AttendeeType";

    private static final String TAG_ALLOW_EDIT_ATTENDEE_COUNT = "AllowEditAtnCount";
    private static final String TAG_ATTENDEE_TYPE_CODE = "AtnTypeCode";
    private static final String TAG_ATTENDEE_TYPE_KEY = "AtnTypeKey";
    private static final String TAG_ATTENDEE_TYPE_NAME = "AtnTypeName";
    private static final String TAG_FORM_KEY = "FormKey";
    private static final String TAG_IS_EXTERNAL = "IsExternal";

    private static final int TAG_ALLOW_EDIT_ATTENDEE_COUNT_CODE = 0;
    private static final int TAG_ATTENDEE_TYPE_CODE_CODE = 1;
    private static final int TAG_ATTENDEE_TYPE_KEY_CODE = 2;
    private static final int TAG_ATTENDEE_TYPE_NAME_CODE = 3;
    private static final int TAG_FORM_KEY_CODE = 4;
    private static final int TAG_IS_EXTERNAL_CODE = 5;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_ALLOW_EDIT_ATTENDEE_COUNT, TAG_ALLOW_EDIT_ATTENDEE_COUNT_CODE);
        tagMap.put(TAG_ATTENDEE_TYPE_CODE, TAG_ATTENDEE_TYPE_CODE_CODE);
        tagMap.put(TAG_ATTENDEE_TYPE_KEY, TAG_ATTENDEE_TYPE_KEY_CODE);
        tagMap.put(TAG_ATTENDEE_TYPE_NAME, TAG_ATTENDEE_TYPE_NAME_CODE);
        tagMap.put(TAG_FORM_KEY, TAG_FORM_KEY_CODE);
        tagMap.put(TAG_IS_EXTERNAL, TAG_IS_EXTERNAL_CODE);
    }

    /**
     * Contains whether the attendee count is editable.
     */
    public Boolean allowEditAtnCount;

    /**
     * Contains the attendee type code.
     */
    public String atnTypeCode;

    /**
     * Contains the attendee type key.
     */
    public String atnTypeKey;

    /**
     * Contains the attendee type name.
     */
    public String atnTypeName;

    /**
     * Contains the attendee type form key.
     */
    public String formKey;

    /**
     * Contains whether the attendee type is external.
     */
    public Boolean isExternal;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_ALLOW_EDIT_ATTENDEE_COUNT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    allowEditAtnCount = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_ATTENDEE_TYPE_CODE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    atnTypeCode = text.trim();
                }
                break;
            }
            case TAG_ATTENDEE_TYPE_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    atnTypeKey = text.trim();
                }
                break;
            }
            case TAG_ATTENDEE_TYPE_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    atnTypeName = text.trim();
                }
                break;
            }
            case TAG_FORM_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    formKey = text.trim();
                }
                break;
            }
            case TAG_IS_EXTERNAL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    isExternal = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }

}
