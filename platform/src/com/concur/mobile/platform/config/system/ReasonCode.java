package com.concur.mobile.platform.config.system;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing a <code>ReasonCode</code> object.
 */
public class ReasonCode extends BaseParser {

    private static final String CLS_TAG = "ReasonCode";

    // tags.
    private static final String TAG_DESCRIPTION = "Description";
    private static final String TAG_ID = "Id";
    private static final String TAG_VIOLATION_TYPE = "ViolationType";

    // tag codes.
    private static final int TAG_DESCRIPTION_CODE = 0;
    private static final int TAG_ID_CODE = 1;
    private static final int TAG_VIOLATION_TYPE_CODE = 2;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_DESCRIPTION, TAG_DESCRIPTION_CODE);
        tagMap.put(TAG_ID, TAG_ID_CODE);
        tagMap.put(TAG_VIOLATION_TYPE, TAG_VIOLATION_TYPE_CODE);
    }

    /**
     * Contains the reason code type, see <code>Config.ReasonCodeColumns.TYPE_AIR, Config.ReasonCodeColumns.TYPE_HOTEL and
     * Config.ReasonCodeColumns.TYPE_AIR</code>.
     */
    public String type;

    /**
     * Contains the description.
     */
    public String description;

    /**
     * Contains the id.
     */
    public Integer id;

    /**
     * Contains the violation type.
     */
    public String violationType;

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
            case TAG_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    id = Parse.safeParseInteger(text.trim());
                    if (id == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unable to parse reason code id.");
                    }
                }
                break;
            }
            case TAG_VIOLATION_TYPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    violationType = text.trim();
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
