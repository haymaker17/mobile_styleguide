package com.concur.mobile.platform.travel.trip;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing an offer link.
 */
public class OfferLink extends BaseParser {

    private static final String CLS_TAG = "Link";

    private static final String TAG_SEGMENT = "Segment";

    /**
     * Contains a reference to the link segment.
     */
    public LinkSegment segment;

    /**
     * Contains a reference to the common parser.
     */
    private CommonParser parser;

    public OfferLink(CommonParser parser, String startTag) {
        this.parser = parser;
    }

    @Override
    public void startTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_SEGMENT)) {
                segment = new LinkSegment();
                segment.setAttributeValues(parser.getXmlPullParser());
            }
        }
    }

    @Override
    public void handleText(String tag, String text) {
        if (Const.DEBUG_PARSING) {
            Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
        }
    }

}
