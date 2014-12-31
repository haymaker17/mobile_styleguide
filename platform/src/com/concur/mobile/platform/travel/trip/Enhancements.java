package com.concur.mobile.platform.travel.trip;

import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing trip enhancements.
 */
public class Enhancements extends BaseParser {

    private static final String CLS_TAG = "Enhancements";

    /**
     * Contains the list of days.
     */
    public List<Day> days;

    /**
     * Contains the list of offers.
     */
    public List<Offer> offers;

    /**
     * Contains the day list parser.
     */
    private ListParser<Day> dayListParser;

    /**
     * Contains the offer list parser.
     */
    private ListParser<Offer> offerListParser;

    /**
     * Contains the start tag.
     */
    private String startTag;

    /**
     * Constructs an instance of <code>Enhancements</code> for parsing a set of trip enhancements.
     * 
     * @param parser
     *            contains an instance to the common parser.
     * @param startTag
     *            contains the start tag.
     */
    public Enhancements(CommonParser parser, String startTag) {
        // Set the start tag.
        this.startTag = startTag;

        // Create and register the day list parser.
        String listTag = "Days";
        dayListParser = new ListParser<Day>(parser, listTag, "Day", Day.class);
        parser.registerParser(dayListParser, listTag);

        // Create and register the offer list parser.
        listTag = "Offers";
        offerListParser = new ListParser<Offer>(parser, listTag, "Offer", Offer.class);
        parser.registerParser(offerListParser, listTag);
    }

    @Override
    public void handleText(String tag, String text) {
        if (Const.DEBUG_PARSING) {
            Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
        }
    }

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {
                // Set the days.
                days = dayListParser.getList();
                // Set the offers.
                offers = offerListParser.getList();
            }
        }
    }
}
