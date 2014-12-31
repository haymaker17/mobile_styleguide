package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

public class RuleViolationReason extends BaseParser {

    private static final String CLS_TAG = "RuleViolationReason";

    // Tags.
    private static final String TAG_REASON = "Reason";
    private static final String TAG_COMMENTS = "Comments";
    // Tag codes.
    private static final int TAG_REASON_CODE = 0;
    private static final int TAG_COMMENTS_CODE = 1;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_REASON, TAG_REASON_CODE);
        tagMap.put(TAG_COMMENTS, TAG_COMMENTS_CODE);
    }

    /**
     * violation reason code i.e. Spouse / Family travel
     */
    public String reasonCode;

    /**
     * traveler comments
     */
    public String bookerComments;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_REASON_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    reasonCode = text.trim();
                }
                break;
            }
            case TAG_COMMENTS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    bookerComments = text.trim();
                }
            }
                break;
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
            }
        }
    }
}
