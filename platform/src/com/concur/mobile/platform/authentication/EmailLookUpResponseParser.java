/**
 * 
 */
package com.concur.mobile.platform.authentication;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * Provides an extension of <code>BaseParser</code> for the purpose of parsing a response to an email lookup request.
 * 
 * @author andrewk
 */
public class EmailLookUpResponseParser extends BaseParser {

    private static final String CLS_TAG = "EmailLookUpResponseParser";

    // Tags.
    private static final String TAG_SERVER_URL = "ServerUrl";
    private static final String TAG_SIGN_IN_METHOD = "SignInMethod";
    private static final String TAG_SSO_URL = "SsoUrl";

    // Tag codes.
    private static final int TAG_SERVER_URL_CODE = 1;
    private static final int TAG_SIGN_IN_METHOD_CODE = 2;
    private static final int TAG_SSO_URL_CODE = 3;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        // tagMap.put(TAG_LOGIN_ID, TAG_LOGIN_ID_CODE);
        tagMap.put(TAG_SERVER_URL, TAG_SERVER_URL_CODE);
        tagMap.put(TAG_SIGN_IN_METHOD, TAG_SIGN_IN_METHOD_CODE);
        tagMap.put(TAG_SSO_URL, TAG_SSO_URL_CODE);
        // tagMap.put(TAG_EMAIL, TAG_EMAIL_CODE);
    }

    public String loginId;

    public String serverUrl;

    public String signInMethod;

    public String ssoUrl;

    public String email;

    @Override
    public void handleText(String tag, String text) {

        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_LOGIN_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    loginId = text.trim();
                }
                break;
            }
            case TAG_SERVER_URL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    serverUrl = text.trim();
                }
                break;
            }
            case TAG_SIGN_IN_METHOD_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    signInMethod = text.trim();
                }
                break;
            }
            case TAG_SSO_URL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    ssoUrl = text.trim();
                }
                break;
            }
            case TAG_EMAIL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    email = text.trim();
                }
            }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
            }
        }
    }

}
