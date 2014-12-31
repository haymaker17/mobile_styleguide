package com.concur.mobile.platform.travel.trip;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for parsing a map display overlay object.
 */
public class Overlay extends BaseParser {

    private static final String CLS_TAG = "Overlay";

    private static final String TAG_OVERLAY = "Overlay";

    /**
     * Contains the name.
     */
    public String name;

    @Override
    public void handleText(String tag, String text) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_OVERLAY)) {
                if (!TextUtils.isEmpty(text)) {
                    name = text.trim();
                }
            } else {
                if (Const.DEBUG_PARSING) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
                }
            }
        }
    }

}
