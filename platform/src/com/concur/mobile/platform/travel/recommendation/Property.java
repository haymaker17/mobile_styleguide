/**
 * 
 */
package com.concur.mobile.platform.travel.recommendation;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for parsing property id information.
 */
public class Property extends BaseParser {

    private static final String CLS_TAG = "Property";

    private static final String TAG_TYPE = "Type";
    private static final String TAG_ID = "Id";

    private static final int CODE_TYPE = 0;
    private static final int CODE_ID = 1;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_TYPE, CODE_TYPE);
        tagMap.put(TAG_ID, CODE_ID);
    }

    /**
     * Contains the type.
     */
    public String type;

    /**
     * Contains the id.
     */
    public String id;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            if (text != null) {
                switch (tagCode) {
                case CODE_TYPE: {
                    if (!TextUtils.isEmpty(text)) {
                        text = text.trim();
                    }
                    type = text;
                    break;
                }
                case CODE_ID: {
                    if (!TextUtils.isEmpty(text)) {
                        text = text.trim();
                    }
                    id = text;
                    break;
                }
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }

}
