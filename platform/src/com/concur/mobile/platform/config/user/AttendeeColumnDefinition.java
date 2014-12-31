package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * Provides a model of an attendee column definition.
 */
public class AttendeeColumnDefinition extends BaseParser {

    private static final String CLS_TAG = "AttendeeColumnDefinition";

    // Text Text.
    private static final String TAG_ID = "Id";
    private static final String TAG_LABEL = "Label";
    private static final String TAG_DATA_TYPE = "DataType";
    private static final String TAG_CTRL_TYPE = "CtrlType";
    private static final String TAG_ACCESS = "Access";

    // Tag codes.
    private static final int TAG_ID_CODE = 0;
    private static final int TAG_LABEL_CODE = 1;
    private static final int TAG_DATA_TYPE_CODE = 2;
    private static final int TAG_CTRL_TYPE_CODE = 3;
    private static final int TAG_ACCESS_CODE = 4;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_ID, TAG_ID_CODE);
        tagMap.put(TAG_LABEL, TAG_LABEL_CODE);
        tagMap.put(TAG_DATA_TYPE, TAG_DATA_TYPE_CODE);
        tagMap.put(TAG_CTRL_TYPE, TAG_CTRL_TYPE_CODE);
        tagMap.put(TAG_ACCESS, TAG_ACCESS_CODE);
    }

    /**
     * Contains the id.
     */
    public String id;

    /**
     * Contains the label.
     */
    public String label;

    /**
     * Contains the data type.
     */
    public String dataType;

    /**
     * Contains the control type.
     */
    public String controlType;

    /**
     * Contains the access type.
     */
    public String accessType;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    id = text.trim();
                }
                break;
            }
            case TAG_LABEL_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    label = text.trim();
                }
                break;
            }
            case TAG_DATA_TYPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    dataType = text.trim();
                }
                break;
            }
            case TAG_CTRL_TYPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    controlType = text.trim();
                }
                break;
            }
            case TAG_ACCESS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    accessType = text.trim();
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
