package com.concur.mobile.platform.authentication;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing an <code>AccessToken</code> object.
 * 
 * @author andrewk
 */
public class AccessToken extends BaseParser {

    private static final String CLS_TAG = "AccessToken";

    private static final String TAG_KEY = "AccessTokenKey";

    public String key;

    @Override
    public void handleText(String tag, String text) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_KEY)) {
                if (!TextUtils.isEmpty(text)) {
                    key = text.trim();
                }
            } else {
                if (Const.DEBUG_PARSING) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
                }
            }
        }
    }

}
