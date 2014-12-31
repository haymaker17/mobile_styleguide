package com.concur.mobile.platform.travel.trip;

import android.text.TextUtils;

import com.concur.mobile.base.service.parser.BaseParser;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing a list of rules encoded as strings.
 * 
 * @author andrewk
 */
public class Rule extends BaseParser {

    private static final String TAG_RULE = "Rule";

    public String text;

    @Override
    public void handleText(String tag, String text) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_RULE)) {
                if (!TextUtils.isEmpty(text)) {
                    text = text.trim();
                }
            }
        }
    }

}
