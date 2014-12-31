package com.concur.mobile.platform.authentication;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

public class UserContact extends BaseParser {

    private static final String CLS_TAG = "UserContact";

    // Tags.
    private static final String TAG_COMPANY_NAME = "CompanyName";
    private static final String TAG_EMAIL = "Email";
    private static final String TAG_FIRST_NAME = "FirstName";
    private static final String TAG_LAST_NAME = "LastName";
    private static final String TAG_MIDDLE_INITIAL = "Mi";

    private static final int TAG_COMPANY_NAME_CODE = 0;
    private static final int TAG_EMAIL_CODE = 1;
    private static final int TAG_FIRST_NAME_CODE = 2;
    private static final int TAG_LAST_NAME_CODE = 3;
    private static final int TAG_MIDDLE_INITIAL_CODE = 4;

    // Tag codes.
    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_COMPANY_NAME, TAG_COMPANY_NAME_CODE);
        tagMap.put(TAG_EMAIL, TAG_EMAIL_CODE);
        tagMap.put(TAG_FIRST_NAME, TAG_FIRST_NAME_CODE);
        tagMap.put(TAG_LAST_NAME, TAG_LAST_NAME_CODE);
        tagMap.put(TAG_MIDDLE_INITIAL, TAG_MIDDLE_INITIAL_CODE);
    }

    /**
     * Contains the company name.
     */
    public String companyName;

    /**
     * Contains the email.
     */
    public String email;

    /**
     * Contains the first name.
     */
    public String firstName;

    /**
     * Contains the last name.
     */
    public String lastName;

    /**
     * Contains the middle initial.
     */
    public String middleInitial;

    @Override
    public void handleText(String tag, String text) {

        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_COMPANY_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    companyName = text.trim();
                }
                break;
            }
            case TAG_EMAIL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    email = text.trim();
                }
                break;
            }
            case TAG_FIRST_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    firstName = text.trim();
                }
                break;
            }
            case TAG_LAST_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    lastName = text.trim();
                }
                break;
            }
            case TAG_MIDDLE_INITIAL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    middleInitial = text.trim();
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
