package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * Provides a model of credit card information.
 */
public class CreditCard extends BaseParser {

    private static final String CLS_TAG = "CreditCard";

    private static final String TAG_NAME = "Name";
    private static final String TAG_TYPE = "Type";
    private static final String TAG_MASKED_NUMBER = "MaskedNumber";
    private static final String TAG_CC_ID = "CcId";
    private static final String TAG_DEFAULT_FOR = "DefaultFor";
    private static final String TAG_ALLOW_FOR = "AllowFor";
    private static final String TAG_CREDIT_CARD_ID = "CreditCardId";
    private static final String TAG_CREDIT_CARD_LAST_FOUR = "CreditCardLastFour";

    private static final int TAG_NAME_CODE = 0;
    private static final int TAG_TYPE_CODE = 1;
    private static final int TAG_MASKED_NUMBER_CODE = 2;
    private static final int TAG_CC_ID_CODE = 3;
    private static final int TAG_DEFAULT_FOR_CODE = 4;
    private static final int TAG_ALLOW_FOR_CODE = 5;
    private static final int TAG_CREDIT_CARD_ID_CODE = 6;
    private static final int TAG_CREDIT_CARD_LAST_FOUR_CODE = 7;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_NAME, TAG_NAME_CODE);
        tagMap.put(TAG_TYPE, TAG_TYPE_CODE);
        tagMap.put(TAG_MASKED_NUMBER, TAG_MASKED_NUMBER_CODE);
        tagMap.put(TAG_CC_ID, TAG_CC_ID_CODE);
        tagMap.put(TAG_DEFAULT_FOR, TAG_DEFAULT_FOR_CODE);
        tagMap.put(TAG_ALLOW_FOR, TAG_ALLOW_FOR_CODE);
        tagMap.put(TAG_CREDIT_CARD_ID, TAG_CREDIT_CARD_ID_CODE);
        tagMap.put(TAG_CREDIT_CARD_LAST_FOUR, TAG_CREDIT_CARD_LAST_FOUR_CODE);
    }

    /**
     * Contains the name.
     */
    public String name;

    /**
     * Contains the type.
     */
    public String type;

    /**
     * Contains the masked number.
     */
    public String maskedNumber;

    /**
     * Contains the credit card id.
     */
    public String ccId;

    /**
     * Contains the booking types for which this card is the default.
     */
    public String defaultFor;

    /**
     * Contains the booking types for which this card is permitted.
     */
    public String allowFor;

    /**
     * Contains the last four digits of the card.
     */
    public String lastFour;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    name = text.trim();
                }
                break;
            }
            case TAG_TYPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    type = text.trim();
                }
                break;
            }
            case TAG_MASKED_NUMBER_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    maskedNumber = text.trim();
                }
                break;
            }
            case TAG_CREDIT_CARD_ID_CODE:
            case TAG_CC_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    ccId = text.trim();
                }
                break;
            }
            case TAG_DEFAULT_FOR_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    defaultFor = text.trim();
                }
                break;
            }
            case TAG_ALLOW_FOR_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    allowFor = text.trim();
                }
                break;
            }
            case TAG_CREDIT_CARD_LAST_FOUR_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    lastFour = text.trim();
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
