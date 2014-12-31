package com.concur.mobile.platform.authentication;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing the response to a <code>RequestPasswordReset</code> MWS
 * call.
 * 
 * @author andrewk
 */
public class RequestPasswordResetResultParser extends BaseParser {

    private static final String CLS_TAG = "RequestPasswordResetResultParser";

    public static final String TAG_REQUEST_PASSWORD_RESET_RESULT = "RequestPasswordResetResult";

    private static final String TAG_STATUS = "Status";
    private static final String TAG_KEY_PART = "KeyPart";
    private static final String TAG_GOOD_PASSWORD_DESCRIPTION = "GoodPasswordDescription";

    private static final int TAG_STATUS_CODE = 0;
    private static final int TAG_KEY_PART_CODE = 1;
    private static final int TAG_GOOD_PASSWORD_DESCRIPTION_CODE = 2;

    private static final Map<String, Integer> tagMap;

    static {

        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_STATUS, TAG_STATUS_CODE);
        tagMap.put(TAG_KEY_PART, TAG_KEY_PART_CODE);
        tagMap.put(TAG_GOOD_PASSWORD_DESCRIPTION, TAG_GOOD_PASSWORD_DESCRIPTION_CODE);
    }

    /**
     * Contains the status message.
     */
    public Boolean status;

    /**
     * Contains the key part.
     */
    public String keyPartA;

    /**
     * Contains the good password description.
     */
    public String goodPasswordDescription;

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
            case TAG_KEY_PART_CODE: {
                if (text != null) {
                    keyPartA = text.trim();
                }
                break;
            }
            case TAG_GOOD_PASSWORD_DESCRIPTION_CODE: {
                if (text != null) {
                    goodPasswordDescription = text.trim();
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
