package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * Provides a model of Yodlee payment type information.
 */
public class YodleePaymentType extends BaseParser {

    private static final String CLS_TAG = "YodleePaymentType";

    private static final String TAG_KEY = "Key";
    private static final String TAG_TEXT = "Text";

    private static final int TAG_KEY_CODE = 0;
    private static final int TAG_TEXT_CODE = 1;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_KEY, TAG_KEY_CODE);
        tagMap.put(TAG_TEXT, TAG_TEXT_CODE);
    }

    /**
     * Contains the payment type key.
     */
    public String key;

    /**
     * Contains the payment type text.
     */
    public String text;

    @Override
    public void handleText(String tag, String txt) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_KEY_CODE: {
                if (!TextUtils.isEmpty(txt)) {
                    key = txt.trim();
                }
                break;
            }
            case TAG_TEXT_CODE: {
                if (!TextUtils.isEmpty(txt)) {
                    text = txt.trim();
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
