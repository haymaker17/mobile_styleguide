package com.concur.mobile.platform.travel.trip;

import android.util.Log;

import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>SegmentData</code> for identifying event specific segment information.
 */
public class EventSegmentData extends SegmentData {

    private static final String CLS_TAG = "EventSegmentData";

    public EventSegmentData() {
        super(SegmentType.EVENT);
    }

    @Override
    public boolean handleSegmentText(String tag, String text) {
        boolean retVal = false;
        retVal = super.handleSegmentText(tag, text);
        if (!retVal) {
            if (Const.DEBUG_PARSING) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".handleSegmentText: unexpected tag '" + tag + "'.");
            }
        }
        return retVal;
    }

}
