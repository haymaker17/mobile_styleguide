package com.concur.mobile.platform.authentication;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

public class Session extends BaseParser {

    private static final String CLS_TAG = "Session";

    // Tags.
    private static final String TAG_ID = "ID";
    private static final String TAG_TIMEOUT = "TimeOut";

    // Tag codes.
    private static final int TAG_ID_CODE = 0;
    private static final int TAG_TIMEOUT_CODE = 1;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_ID, TAG_ID_CODE);
        tagMap.put(TAG_TIMEOUT, TAG_TIMEOUT_CODE);
    }

    /**
     * Contains the session id.
     */
    public String id;

    /**
     * Contains the session timeout in minutes.
     */
    public Integer timeout;

    /**
     * Contains the session expiration time.
     */
    public Long expirationTime;

    @Override
    public void handleText(String tag, String text) {

        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    id = text.trim();
                }
                break;
            }
            case TAG_TIMEOUT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    timeout = Parse.safeParseInteger(text.trim());
                    // Set the session expiration time.
                    if (timeout != null) {
                        expirationTime = System.currentTimeMillis() + (timeout * 60 * 1000);
                    }
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
