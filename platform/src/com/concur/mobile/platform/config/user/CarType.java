package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Models car type information.
 */
public class CarType extends BaseParser {

    private static final String CLS_TAG = "CarType";

    private static final String TAG_DESCRIPTION = "Description";
    private static final String TAG_CODE = "Code";
    private static final String TAG_IS_DEFAULT = "IsDefault";

    private static final int TAG_DESCRIPTION_CODE = 0;
    private static final int TAG_CODE_CODE = 1;
    private static final int TAG_IS_DEFAULT_CODE = 2;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_DESCRIPTION, TAG_DESCRIPTION_CODE);
        tagMap.put(TAG_IS_DEFAULT, TAG_IS_DEFAULT_CODE);
        tagMap.put(TAG_CODE, TAG_CODE_CODE);
    }

    /**
     * Contains the car-type description.
     */
    public String description;

    /**
     * Contains the car-type code.
     */
    public String code;

    /**
     * Contains whether this car-type is the default selection.
     */
    public Boolean isDefault;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_DESCRIPTION_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    description = text.trim();
                }
                break;
            }
            case TAG_CODE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    code = text.trim();
                }
                break;
            }
            case TAG_IS_DEFAULT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    isDefault = Parse.safeParseBoolean(text.trim());
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
