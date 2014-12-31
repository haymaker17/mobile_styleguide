package com.concur.mobile.platform.authentication;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

public class SiteSetting extends BaseParser {

    private static final String CLS_TAG = "SiteSetting";

    // Tags.
    private static final String TAG_NAME = "Name";
    private static final String TAG_TYPE = "Type";
    private static final String TAG_VALUE = "Value";

    // Tag codes.
    private static final int TAG_NAME_CODE = 0;
    private static final int TAG_TYPE_CODE = 1;
    private static final int TAG_VALUE_CODE = 2;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_NAME, TAG_NAME_CODE);
        tagMap.put(TAG_TYPE, TAG_TYPE_CODE);
        tagMap.put(TAG_VALUE, TAG_VALUE_CODE);
    }

    /**
     * Contains the name.
     */
    public String name;

    /**
     * Contains the type.
     */
    public String type;

    /**
     * Contains the value.
     */
    public String value;

    @Override
    public void handleText(String tag, String text) {

        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    name = text.trim();
                }
                break;
            }
            case TAG_TYPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    type = text.trim();
                }
                break;
            }
            case TAG_VALUE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    value = text.trim();
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
