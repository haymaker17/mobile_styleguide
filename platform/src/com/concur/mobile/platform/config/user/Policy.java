package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a model of an expense policy
 */
public class Policy extends BaseParser {

    private static final String CLS_TAG = "Policy";

    private static final String TAG_POLICY_KEY = "PolKey";
    private static final String TAG_SUPPORTS_IMAGING = "SupportsImaging";
    private static final String TAG_APPROVAL_CONFIRMATION_KEY = "ApprovalConfirmationKey";
    private static final String TAG_SUBMIT_CONFIRMATION_KEY = "SubmitConfirmationKey";

    private static final int TAG_POLICY_KEY_CODE = 0;
    private static final int TAG_SUPPORTS_IMAGING_CODE = 1;
    private static final int TAG_APPROVAL_CONFIRMATION_KEY_CODE = 2;
    private static final int TAG_SUBMIT_CONFIRMATION_KEY_CODE = 3;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_POLICY_KEY, TAG_POLICY_KEY_CODE);
        tagMap.put(TAG_SUPPORTS_IMAGING, TAG_SUPPORTS_IMAGING_CODE);
        tagMap.put(TAG_APPROVAL_CONFIRMATION_KEY, TAG_APPROVAL_CONFIRMATION_KEY_CODE);
        tagMap.put(TAG_SUBMIT_CONFIRMATION_KEY, TAG_SUBMIT_CONFIRMATION_KEY_CODE);
    }

    /**
     * Contains the policy key.
     */
    public String key;

    /**
     * Contains whether or not the policy supports imaging.
     */
    public Boolean supportsImaging;

    /**
     * Contains the approval key.
     */
    public String approvalKey;

    /**
     * Contains the submit key.
     */
    public String submitKey;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_POLICY_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    key = text.trim();
                }
                break;
            }
            case TAG_SUPPORTS_IMAGING_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    supportsImaging = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_APPROVAL_CONFIRMATION_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    approvalKey = text.trim();
                }
                break;
            }
            case TAG_SUBMIT_CONFIRMATION_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    submitKey = text.trim();
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
