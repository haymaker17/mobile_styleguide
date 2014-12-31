package com.concur.mobile.platform.travel.trip;

import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purposes of parsing a set of itinerary rule violations.
 */
public class RuleViolations extends BaseParser {

    public static final String CLS_TAG = "RuleViolations";

    /**
     * Contains a list of parsed itinerary rule violations.
     */
    public List<RuleViolation> itinRuleViolations;

    /**
     * Contains a list of parsed car rule violations.
     */
    public List<CarRuleViolation> carRuleViolations;

    /**
     * Contains a list of hotel rule violations.
     */
    public List<HotelRuleViolation> hotelRuleViolations;

    /**
     * Contians a list of parsed flight rule violations.
     */
    public List<FlightRuleViolation> flightRuleViolations;

    /**
     * Contains a list of parsed rail rule violations.
     */
    public List<RailRuleViolation> railRuleViolations;

    /**
     * Contains the itinerary rule violation list parser.
     */
    private ListParser<RuleViolation> itinRuleViolationListParser;

    /**
     * Contains the car rule violation list parser.
     */
    private ListParser<CarRuleViolation> carRuleViolationListParser;

    /**
     * Contains the hotel rule violation list parser.
     */
    private ListParser<HotelRuleViolation> hotelRuleViolationListParser;

    /**
     * Contains the flight rule violation list parser.
     */
    private ListParser<FlightRuleViolation> flightRuleViolationListParser;

    /**
     * Contains the rail rule violation list parser.
     */
    private ListParser<RailRuleViolation> railRuleViolationListParser;

    /**
     * Contains the start tag for the rule violations.
     */
    private String startTag;

    /**
     * Constructs an instance of <code>RuleViolations</code> for the purpose of parsing a set of itinerary rule violations.
     * 
     * @param parser
     *            contains a reference to the common parser.
     * @param startTag
     *            contains the start tag.
     */
    public RuleViolations(CommonParser parser, String startTag) {

        this.startTag = startTag;

        // Create and initialize the itin rule violations parser.
        String itemTag = "Itinerary";
        itinRuleViolationListParser = new ListParser<RuleViolation>(parser, null, itemTag, RuleViolation.class);
        parser.registerParser(itinRuleViolationListParser, itemTag);

        // Create and initialize the car rule violations parser.
        itemTag = "Car";
        carRuleViolationListParser = new ListParser<CarRuleViolation>(parser, null, itemTag, CarRuleViolation.class);
        parser.registerParser(carRuleViolationListParser, itemTag);

        // Create and initialize the hotel rule violations parser.
        itemTag = "Hotel";
        hotelRuleViolationListParser = new ListParser<HotelRuleViolation>(parser, null, itemTag,
                HotelRuleViolation.class);
        parser.registerParser(hotelRuleViolationListParser, itemTag);

        // Create and initialize the flight rule violations parser.
        itemTag = "Air";
        flightRuleViolationListParser = new ListParser<FlightRuleViolation>(parser, null, itemTag,
                FlightRuleViolation.class);
        parser.registerParser(flightRuleViolationListParser, itemTag);

        // Create and initialize the rail rule violations parser.
        itemTag = "Rail";
        railRuleViolationListParser = new ListParser<RailRuleViolation>(parser, null, itemTag, RailRuleViolation.class);
        parser.registerParser(railRuleViolationListParser, itemTag);
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
                // Set the itinerary rule violations.
                itinRuleViolations = itinRuleViolationListParser.getList();
                // Set the car rule violations.
                carRuleViolations = carRuleViolationListParser.getList();
                // Set the hotel rule violations.
                hotelRuleViolations = hotelRuleViolationListParser.getList();
                // Set the flight rule violations.
                flightRuleViolations = flightRuleViolationListParser.getList();
                // Set the rail rule violations.
                railRuleViolations = railRuleViolationListParser.getList();
            }
        }
    }
}
