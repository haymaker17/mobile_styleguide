package com.concur.mobile.platform.travel.triplist;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purposes of parsing a list of messages.
 */
public class TripStateMessages extends BaseParser {

    private static final String CLS_TAG = "TripStateMessages";

    private static final String TAG_TRIP_STATE_MESSAGE = "TripStateMessage";

    public List<String> messages;

    @Override
    public void handleText(String tag, String text) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_TRIP_STATE_MESSAGE)) {
                if (!TextUtils.isEmpty(text)) {
                    if (messages == null) {
                        messages = new ArrayList<String>();
                    }
                    messages.add(text.trim());
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleText: unknown tag '" + tag + "'.");
            }
        }
    }

}
