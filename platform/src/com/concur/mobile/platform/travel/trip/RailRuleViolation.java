package com.concur.mobile.platform.travel.trip;

import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing rail rule violation information.
 */
public class RailRuleViolation extends BaseParser {

    private static final String CLS_TAG = "RailRuleViolation";

    private static final String TAG_RATE = "Rate";

    /**
     * Contains the rate.
     */
    public String rate;

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
     * Constructs an instance of <code>RailRuleViolation</code> for the purpose with a parser and start tag.
     * 
     * @param parser
     *            contains the common parser.
     * @param startTag
     *            contains the start tag.
     */
    public RailRuleViolation(CommonParser parser, String startTag) {

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
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_RATE)) {
                if (!TextUtils.isEmpty(text)) {
                    rate = text.trim();
                }
            } else {
                if (Const.DEBUG_PARSING) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
                }
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
