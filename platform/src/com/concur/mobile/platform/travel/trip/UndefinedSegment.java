package com.concur.mobile.platform.travel.trip;

import android.util.Log;

import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>SegmentData</code> for handling a undefined segment information.
 */
public class UndefinedSegment extends SegmentData {

    private static final String CLS_TAG = "UndefinedSegmentData";

    public UndefinedSegment() {
        super(SegmentType.UNDEFINED);
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
