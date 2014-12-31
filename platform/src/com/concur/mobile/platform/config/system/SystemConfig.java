package com.concur.mobile.platform.config.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.config.provider.Config;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing a <code>SystemConfig</code> object.
 */
public class SystemConfig extends BaseParser {

    private static final String CLS_TAG = "SystemConfig";

    public static final String TAG_SYSTEM_CONFIG = "SystemConfig";

    // tags.
    private static final String TAG_CHECKBOX_DEFAULT = "CheckboxDefault";
    private static final String TAG_MESSAGE = "Message";
    private static final String TAG_SHOW_CHECKBOX = "ShowCheckbox";
    private static final String TAG_RULE_VIOLATION_EXPLANATION_REQUIRED = "RuleViolationExplanationRequired";
    private static final String TAG_HASH = "Hash";
    private static final String TAG_RESPONSE_ID = "ResponseId";

    // tag codes.
    private static final int TAG_CHECKBOX_DEFAULT_CODE = 0;
    private static final int TAG_MESSAGE_CODE = 1;
    private static final int TAG_SHOW_CHECKBOX_CODE = 2;
    private static final int TAG_RULE_VIOLATION_EXPLANATION_REQUIRED_CODE = 3;
    private static final int TAG_HASH_CODE = 4;
    private static final int TAG_RESPONSE_ID_CODE = 5;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_CHECKBOX_DEFAULT, TAG_CHECKBOX_DEFAULT_CODE);
        tagMap.put(TAG_MESSAGE, TAG_MESSAGE_CODE);
        tagMap.put(TAG_SHOW_CHECKBOX, TAG_SHOW_CHECKBOX_CODE);
        tagMap.put(TAG_RULE_VIOLATION_EXPLANATION_REQUIRED, TAG_RULE_VIOLATION_EXPLANATION_REQUIRED_CODE);
        tagMap.put(TAG_HASH, TAG_HASH_CODE);
        tagMap.put(TAG_RESPONSE_ID, TAG_RESPONSE_ID_CODE);
    }

    /**
     * Contains the list of air violation reasons.
     */
    public List<ReasonCode> airReasons;

    /**
     * Contains the list of hotel violation reasons.
     */
    public List<ReasonCode> hotelReasons;

    /**
     * Contains the list of car violation reasons.
     */
    public List<ReasonCode> carReasons;

    /**
     * Contains the list of expense types.
     */
    public List<ExpenseType> expenseTypes;

    /**
     * Contains the server-generated hash code for the system config data.
     */
    public String hash;

    /**
     * Contains the response id for this data.
     */
    public String responseId;

    /**
     * Contains the list of company office locations.
     */
    public List<OfficeLocation> officeLocations;

    /**
     * Contains whether the refundable checkbox is checked by default.
     */
    public Boolean refundableCheckboxDefault;

    /**
     * Contains the refundable message.
     */
    public String refundableMessage;

    /**
     * Contains whether the refundable checkbox should be displayed.
     */
    public Boolean refundableShowCheckbox;

    /**
     * Contains whether or not an explanation is required for a rule violation.
     */
    public Boolean ruleViolationExplanationRequired;

    /**
     * Contains the parser parsing a list of air <code>ReasonCode</code> objects.
     */
    private ListParser<ReasonCode> airReasonListParser;

    /**
     * Contains the parser parsing a list of hotel <code>ReasonCode</code> objects.
     */
    private ListParser<ReasonCode> hotelReasonListParser;

    /**
     * Contains the parser parsing a list of car <code>ReasonCode</code> objects.
     */
    private ListParser<ReasonCode> carReasonListParser;

    /**
     * Contains the parser parsing a list of <code>ExpenseType</code> objects.
     */
    private ListParser<ExpenseType> expenseTypeListParser;

    /**
     * Contains the parser parsing a list of <code>OfficeLocation</code> objects.
     */
    private ListParser<OfficeLocation> officeLocationListParser;

    /**
     * Contains the start tag used to register this parser.
     */
    private String startTag;

    /**
     * Constructs an instance of <code>SystemConfig</code> with a parser to be used to parse system configuration attributes.
     * 
     * @param parser
     *            contains a reference to a <code>CommonParser</code> object.
     * @param startTag
     *            contains the start tag used to register this parser.
     */
    public SystemConfig(CommonParser parser, String startTag) {

        // Set the start tag and register this parser.
        this.startTag = startTag;
        parser.registerParser(this, startTag);

        // Initialize the air reasons list parser.
        String listTag = "AirReasons";
        airReasonListParser = new ListParser<ReasonCode>(listTag, "ReasonCode", ReasonCode.class);
        parser.registerParser(airReasonListParser, listTag);

        // Initialize the car reasons list parser.
        listTag = "CarReasons";
        carReasonListParser = new ListParser<ReasonCode>(listTag, "ReasonCode", ReasonCode.class);
        parser.registerParser(carReasonListParser, listTag);

        // Initialize the hotel reasons list parser.
        listTag = "HotelReasons";
        hotelReasonListParser = new ListParser<ReasonCode>(listTag, "ReasonCode", ReasonCode.class);
        parser.registerParser(hotelReasonListParser, listTag);

        // Initialize the expense type list parser.
        listTag = "ExpenseTypes";
        expenseTypeListParser = new ListParser<ExpenseType>(listTag, "ExpenseType", ExpenseType.class);
        parser.registerParser(expenseTypeListParser, listTag);

        listTag = "Offices";
        officeLocationListParser = new ListParser<OfficeLocation>(listTag, "OfficeChoice", OfficeLocation.class);
        parser.registerParser(officeLocationListParser, listTag);
    }

    @Override
    public void handleText(String tag, String text) {

        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_CHECKBOX_DEFAULT_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    refundableCheckboxDefault = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_MESSAGE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    refundableMessage = text.trim();
                }
                break;
            }
            case TAG_SHOW_CHECKBOX_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    refundableShowCheckbox = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_RULE_VIOLATION_EXPLANATION_REQUIRED_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    ruleViolationExplanationRequired = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_HASH_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    hash = text.trim();
                }
                break;
            }
            case TAG_RESPONSE_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    responseId = text.trim();
                }
                break;
            }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
            }
        }
    }

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {

                // Set air reason codes.
                airReasons = airReasonListParser.getList();
                if (airReasons != null) {
                    for (ReasonCode rc : airReasons) {
                        rc.type = Config.ReasonCodeColumns.TYPE_AIR;
                    }
                }

                // Set car reason codes.
                carReasons = carReasonListParser.getList();
                if (carReasons != null) {
                    for (ReasonCode rc : carReasons) {
                        rc.type = Config.ReasonCodeColumns.TYPE_CAR;
                    }
                }

                // Set hotel reason codes.
                hotelReasons = hotelReasonListParser.getList();
                if (hotelReasons != null) {
                    for (ReasonCode rc : hotelReasons) {
                        rc.type = Config.ReasonCodeColumns.TYPE_HOTEL;
                    }
                }

                // Set expense types.
                expenseTypes = expenseTypeListParser.getList();

                // Set office locations.
                officeLocations = officeLocationListParser.getList();
            }
        }
    }

}
