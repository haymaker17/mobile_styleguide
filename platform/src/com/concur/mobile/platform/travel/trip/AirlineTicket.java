package com.concur.mobile.platform.travel.trip;

import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for parsing booking airline ticket information.
 */
public class AirlineTicket extends BaseParser {

    private static final String CLS_TAG = "AirlineTicket";

    private String startTag;

    public AirlineTicket(CommonParser parser, String startTag) {
        this.startTag = startTag;
    }

    @Override
    public void handleText(String tag, String text) {
        if (Const.DEBUG_PARSING) {
            Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
        }
    }

}
