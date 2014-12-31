package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing hotel rule violation information.
 */
public class HotelRuleViolation extends BaseParser {

    private static final String CLS_TAG = "HotelRuleViolation";

    private static final String TAG_RATE = "Rate";
    private static final String TAG_NAME = "Name";
    private static final String TAG_ADDRESS = "Address";
    private static final String TAG_DESCRIPTION = "Description";

    // Tag codes.
    private static final int CODE_RATE = 0;
    private static final int CODE_NAME = 1;
    private static final int CODE_ADDRESS = 2;
    private static final int CODE_DESCRIPTION = 3;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_RATE, CODE_RATE);
        tagMap.put(TAG_NAME, CODE_NAME);
        tagMap.put(TAG_ADDRESS, CODE_ADDRESS);
        tagMap.put(TAG_DESCRIPTION, CODE_DESCRIPTION);
    }

    /**
     * Contains the rate.
     */
    public String rate;

    /**
     * Contains the name.
     */
    public String name;

    /**
     * Contains the address.
     */
    public String address;

    /**
     * Contains the description.
     */
    public String description;

    /**
     * Contains the list of rules.
     */
    public List<Rule> rules;

    /**
     * Contains the list of violation reasons.
     */
    public List<RuleViolationReason> violationReasons;

    /**
     * Contains the rule list parser.
     */
    private ListParser<Rule> ruleListParser;

    /**
     * Contains the rule violation list parser.
     */
    private ListParser<RuleViolationReason> violationReasonListParser;

    /**
     * Contains the start tag.
     */
    private String startTag;

    /**
     * Constructs an instance of <code>HotelRuleViolation</code> with a common parser and a start tag.
     * 
     * @param parser
     *            contains the common parser.
     * @param startTag
     *            contains the start tag.
     */
    public HotelRuleViolation(CommonParser parser, String startTag) {

        this.startTag = startTag;

        // Create and register the rule violation parser.
        String itemTag = "ViolationReasons";
        violationReasonListParser = new ListParser<RuleViolationReason>(parser, null, itemTag,
                RuleViolationReason.class);
        parser.registerParser(violationReasonListParser, itemTag);

        // Create and register the rule parser.
        itemTag = "Rule";
        ruleListParser = new ListParser<Rule>(null, itemTag, Rule.class);
        parser.registerParser(ruleListParser, itemTag);
    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            if (!TextUtils.isEmpty(text)) {
                switch (tagCode) {
                case CODE_RATE: {
                    rate = text.trim();
                    break;
                }
                case CODE_NAME: {
                    name = text.trim();
                    break;
                }
                case CODE_ADDRESS: {
                    address = text.trim();
                    break;
                }
                case CODE_DESCRIPTION: {
                    description = text.trim();
                    break;
                }
                default: {
                    if (Const.DEBUG_PARSING) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: missing case statement for tag '" + tag + "'.");
                    }
                    break;
                }
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {
                // Set the rule violation reasons.
                violationReasons = violationReasonListParser.getList();
                // Set the rule list.
                rules = ruleListParser.getList();
            }
        }
    }

}
