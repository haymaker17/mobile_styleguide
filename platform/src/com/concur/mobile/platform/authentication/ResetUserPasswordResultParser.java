/**
 * 
 */
package com.concur.mobile.platform.authentication;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser<code> for the purposes of parsing
 * a <code>ResetUserPasswordResult</code> object.
 * 
 * @author andrewk
 */
public class ResetUserPasswordResultParser extends BaseParser {

    private static final String CLS_TAG = "ResetUserPasswordResultParser";

    public static final String TAG_RESET_USER_PASSWORD_RESULT = "ResetUserPasswordResult";

    private static final String TAG_STATUS = "Status";
    private static final String TAG_ERROR_MESSAGE = "ErrorMessage";
    private static final String TAG_MIN_LENGTH = "MinLength";
    private static final String TAG_REQUIRES_MIXED_CASE = "RequiresMixedCase";
    private static final String TAG_LOGIN_ID = "LoginId";

    private static final int TAG_STATUS_CODE = 0;
    private static final int TAG_ERROR_MESSAGE_CODE = 1;
    private static final int TAG_MIN_LENGTH_CODE = 2;
    private static final int TAG_REQUIRES_MIXED_CASE_CODE = 3;
    private static final int TAG_LOGIN_ID_CODE = 4;

    private static final Map<String, Integer> tagMap;

    static {

        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_STATUS, TAG_STATUS_CODE);
        tagMap.put(TAG_ERROR_MESSAGE, TAG_ERROR_MESSAGE_CODE);
        tagMap.put(TAG_MIN_LENGTH, TAG_MIN_LENGTH_CODE);
        tagMap.put(TAG_REQUIRES_MIXED_CASE, TAG_REQUIRES_MIXED_CASE_CODE);
        tagMap.put(TAG_LOGIN_ID, TAG_LOGIN_ID_CODE);
    }

    /**
     * Contains the status message.
     */
    public Boolean status;

    /**
     * Contains the error message.
     */
    public String errorMessage;

    /**
     * Contains the minimum length.
     */
    public Integer minLength;

    /**
     * Contains whether mixed case is required.
     */
    public Boolean requiresMixedCase;

    /**
     * Contains the login id.
     */
    public String loginId;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_STATUS_CODE: {
                if (text != null) {
                    status = (text.trim().equalsIgnoreCase("success") ? Boolean.TRUE : Boolean.FALSE);
                }
                break;
            }
            case TAG_ERROR_MESSAGE_CODE: {
                if (text != null) {
                    errorMessage = text.trim();
                }
                break;
            }
            case TAG_MIN_LENGTH_CODE: {
                if (text != null) {
                    minLength = Parse.safeParseInteger(text.trim());
                }
                break;
            }
            case TAG_REQUIRES_MIXED_CASE_CODE: {
                if (text != null) {
                    requiresMixedCase = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_LOGIN_ID_CODE: {
                if (text != null) {
                    loginId = text.trim();
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
