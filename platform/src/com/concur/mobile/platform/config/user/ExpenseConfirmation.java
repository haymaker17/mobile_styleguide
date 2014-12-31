package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * Provides a model of an expense confirmation.
 */
public class ExpenseConfirmation extends BaseParser {

    private static final String CLS_TAG = "ExpenseConfirmation";

    private static final String TAG_CONFIRMATION_KEY = "ConfirmationKey";
    private static final String TAG_TEXT = "Text";
    private static final String TAG_TITLE = "Title";

    private static final int TAG_CONFIRMATION_KEY_CODE = 0;
    private static final int TAG_TEXT_CODE = 1;
    private static final int TAG_TITLE_CODE = 2;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_CONFIRMATION_KEY, TAG_CONFIRMATION_KEY_CODE);
        tagMap.put(TAG_TEXT, TAG_TEXT_CODE);
        tagMap.put(TAG_TITLE, TAG_TITLE_CODE);
    }

    /**
     * Contains the confirmation key.
     */
    public String key;

    /**
     * Contains the confirmation text.
     */
    public String text;

    /**
     * Contains the confirmation title.
     */
    public String title;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_CONFIRMATION_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    key = text.trim();
                }
                break;
            }
            case TAG_TEXT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    text = text.trim();
                }
                break;
            }
            case TAG_TITLE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    title = text.trim();
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
