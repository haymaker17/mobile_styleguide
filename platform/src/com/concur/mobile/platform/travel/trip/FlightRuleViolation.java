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
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing flight rule violation information.
 */
public class FlightRuleViolation extends BaseParser {

    private static final String CLS_TAG = "FlightRuleViolation";

    // Tags.
    private static final String TAG_REFUNDABLE = "Refundable";
    private static final String TAG_COST = "Cost";

    // Tag codes.
    private static final int TAG_REFUNDABLE_CODE = 0;
    private static final int TAG_COST_CODE = 1;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_REFUNDABLE, TAG_REFUNDABLE_CODE);
        tagMap.put(TAG_COST, TAG_COST_CODE);
    }

    /**
     * Contains the refundable value.
     */
    public boolean refundable;

    /**
     * Contains the cost.
     */
    public String cost;

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
     * Constructs an instance of <code>FlightRuleViolation</code> with a common parser and a start tag.
     * 
     * @param parser
     *            contains the common parser.
     * @param startTag
     *            contains the start tag.
     */
    public FlightRuleViolation(CommonParser parser, String startTag) {

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
            switch (tagCode) {
            case TAG_REFUNDABLE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    refundable = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_COST_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    cost = text.trim();
                }
                break;
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
