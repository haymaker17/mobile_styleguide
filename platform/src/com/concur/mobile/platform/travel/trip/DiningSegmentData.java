package com.concur.mobile.platform.travel.trip;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>SegmentData</code> for identifying dining specific segment information.
 */
public class DiningSegmentData extends SegmentData {

    private static final String CLS_TAG = "DiningSegmentData";

    private static final String TAG_RESERVATION_ID = "ReservationId";

    public String reservationId;

    public DiningSegmentData() {
        super(SegmentType.DINING);
    }

    @Override
    public boolean handleSegmentText(String tag, String text) {
        boolean retVal = false;
        retVal = super.handleSegmentText(tag, text);
        if (!retVal) {
            if (!TextUtils.isEmpty(tag)) {
                if (TAG_RESERVATION_ID.equalsIgnoreCase(tag)) {
                    if (!TextUtils.isEmpty(text)) {
                        reservationId = text.trim();
                    }
                    retVal = true;
                } else {
                    if (Const.DEBUG_PARSING) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".handleSegmentText: unexpected tag '" + tag + "'.'");
                    }
                }
            }
        }
        return retVal;
    }

}
