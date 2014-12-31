package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a model of a currency.
 */
public class Currency extends BaseParser {

    private static final String CLS_TAG = "Currency";

    private static final String TAG_CRN_CODE = "CrnCode";
    private static final String TAG_CRN_NAME = "CrnName";
    private static final String TAG_DECIMAL_DIGITS = "DecimalDigits";

    private static final int TAG_CRN_CODE_CODE = 0;
    private static final int TAG_CRN_NAME_CODE = 1;
    private static final int TAG_DECIMAL_DIGITS_CODE = 2;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_CRN_CODE, TAG_CRN_CODE_CODE);
        tagMap.put(TAG_CRN_NAME, TAG_CRN_NAME_CODE);
        tagMap.put(TAG_DECIMAL_DIGITS, TAG_DECIMAL_DIGITS_CODE);
    }

    /**
     * Contains the currency code.
     */
    public String crnCode;

    /**
     * Contains the currency name.
     */
    public String crnName;

    /**
     * Contains the number of decimal digits. Default to 0, if server omits the value
     */
    public Integer decimalDigits = 0;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_CRN_CODE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    crnCode = text.trim();
                }
                break;
            }
            case TAG_CRN_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    crnName = text.trim();
                }
                break;
            }
            case TAG_DECIMAL_DIGITS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    decimalDigits = Parse.safeParseInteger(text.trim());
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
