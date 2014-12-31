package com.concur.mobile.core.travel.hotel.service.parser;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * parser for Hotel segment cancel requests
 * 
 * @author TejoA
 * 
 */
public class HotelCancelAry extends BaseParser {

    private static final String CLS_TAG = "HotelCancelAry";

    private static final String TAG_CANCELLATION_NUMBER = "CancellationNumber";

    /**
     * Contains Hotel Cancellation number.
     */
    public String cancellationNumber;

    @Override
    public void handleText(String tag, String text) {
        if (tag != null && tag.equalsIgnoreCase(TAG_CANCELLATION_NUMBER)) {
            if (!TextUtils.isEmpty(text)) {
                cancellationNumber = text.trim();
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }
}
