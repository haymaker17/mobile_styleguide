package com.concur.mobile.platform.travel.recommendation;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for parsing recommendation factor data.
 */
public class Factor extends BaseParser {

    private static final String CLS_TAG = "Factor";

    private static final String TAG_SCORE = "Score";
    private static final String TAG_DISPLAY_VALUE = "DisplayValue";
    private static final String TAG_NAME = "Name";

    private static final int CODE_SCORE = 0;
    private static final int CODE_DISPLAY_VALUE = 1;
    private static final int CODE_NAME = 2;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_SCORE, CODE_SCORE);
        tagMap.put(TAG_DISPLAY_VALUE, CODE_DISPLAY_VALUE);
        tagMap.put(TAG_NAME, CODE_NAME);
    }

    public Double score;

    public String displayValue;

    public String name;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            if (text != null) {
                switch (tagCode) {
                case CODE_SCORE: {
                    if (!TextUtils.isEmpty(text)) {
                        score = Parse.safeParseDouble(text.trim());
                    }
                    break;
                }
                case CODE_DISPLAY_VALUE: {
                    if (!TextUtils.isEmpty(text)) {
                        displayValue = text.trim();
                    }
                    break;
                }
                case CODE_NAME: {
                    if (!TextUtils.isEmpty(text)) {
                        name = text.trim();
                    }
                    break;
                }
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }

}
