package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing a content link object.
 */
public class ContentLink extends BaseParser {

    private static final String CLS_TAG = "ContentLink";

    private static final String TAG_TITLE = "Title";
    private static final String TAG_ACTION_URL = "ActionURL";

    private static final int TAG_TITLE_CODE = 0;
    private static final int TAG_ACTION_URL_CODE = 1;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_TITLE, TAG_TITLE_CODE);
        tagMap.put(TAG_ACTION_URL, TAG_ACTION_URL_CODE);
    }

    /**
     * Contains the title.
     */
    public String title;

    /**
     * Contains the action URL.
     */
    public String actionUrl;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_TITLE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    title = text.trim();
                }
                break;
            }
            case TAG_ACTION_URL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    actionUrl = text.trim();
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
