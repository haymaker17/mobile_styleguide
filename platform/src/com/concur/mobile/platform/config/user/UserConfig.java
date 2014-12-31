package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ItemParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a model of a user configuration object.
 */
public class UserConfig extends BaseParser {

    private static final String CLS_TAG = "UserConfig";

    static final String TAG_USER_CONFIG = "UserConfig";

    private static final String TAG_HASH = "Hash";
    private static final String TAG_RESPONSE_ID = "ResponseId";
    private static final String TAG_ALLOWED_AIR_CLASSES_OF_SERVICE = "AllowedAirClassesOfService";
    private static final String TAG_FLAGS = "Flags";
    private static final String TAG_SHOW_GDS_NAME_IN_SEARCH_RESULTS = "ShowGDSNameInSearchResults";

    private static final int TAG_HASH_CODE = 0;
    private static final int TAG_RESPONSE_ID_CODE = 1;
    private static final int TAG_ALLOWED_AIR_CLASSES_OF_SERVICE_CODE = 2;
    private static final int TAG_FLAGS_CODE = 3;
    private static final int TAG_SHOW_GDS_NAME_IN_SEARCH_RESULTS_CODE = 4;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_HASH, TAG_HASH_CODE);
        tagMap.put(TAG_RESPONSE_ID, TAG_RESPONSE_ID_CODE);
        tagMap.put(TAG_ALLOWED_AIR_CLASSES_OF_SERVICE, TAG_ALLOWED_AIR_CLASSES_OF_SERVICE_CODE);
        tagMap.put(TAG_FLAGS, TAG_FLAGS_CODE);
        tagMap.put(TAG_SHOW_GDS_NAME_IN_SEARCH_RESULTS, TAG_SHOW_GDS_NAME_IN_SEARCH_RESULTS_CODE);
    }

    /**
     * Contains the server-generated hash code for the user config data.
     */
    public String hash;

    /**
     * Contains the response id for this data.
     */
    public String responseId;

    /**
     * Contains the "allowed air classes of service".
     */
    public String allowedAirClassesOfService;

    /**
     * Contains a set of flags.
     */
    public String flags;

    /**
     * Contains the flag on whether to show gds name in search results
     */
    public Boolean showGDSNameInSearchResults;

    /**
     * Contains the currencies.
     */
    public List<Currency> currencies;

    /**
     * Contains the reimbursement currencies.
     */
    public List<Currency> reimbursementCurrencies;

    /**
     * Contains the car types.
     */
    public List<CarType> allowedCarTypes;

    /**
     * Contains the expense policies.
     */
    public List<Policy> expensePolicies;

    /**
     * Contains the expense confirmations.
     */
    public List<ExpenseConfirmation> expenseConfirmations;

    /**
     * Contains the attendee types.
     */
    public List<AttendeeType> attendeeTypes;

    /**
     * Contains the attendee column definitions.
     */
    public List<AttendeeColumnDefinition> attendeeColumnDefinitions;

    /**
     * Contains the Yodlee payment types.
     */
    public List<YodleePaymentType> yodleePaymentTypes;

    /**
     * Contains the credit cards.
     */
    public List<CreditCard> creditCards;

    /**
     * Contains the affinity programs.
     */
    public List<AffinityProgram> affinityPrograms;

    /**
     * Contains the travel points config
     */
    public TravelPointsConfig travelPointsConfig;

    /**
     * Contains the parser that parses a list of <code>Currency</code> objects.
     */
    protected ListParser<Currency> currencyListParser;

    /**
     * Contains the parser that parses a list of <code>Currency</code> objects that represent reimbursement currencies.
     */
    protected ListParser<Currency> reimbursementCurrencyListParser;

    /**
     * Contains the parser that parses a list of <code>CarType</code> objects.
     */
    protected ListParser<CarType> carTypeListParser;

    /**
     * Contains the parser that parses a list of <code>Policy</code> objects.
     */
    protected ListParser<Policy> policyListParser;

    /**
     * Contains the parser that parses a list of <code>ExpenseConfirmation</code> objects.
     */
    protected ListParser<ExpenseConfirmation> expenseConfirmationListParser;

    /**
     * Contains the parser that parses a list of <code>AttendeeType</code> objects.
     */
    protected ListParser<AttendeeType> attendeeTypeListParser;

    /**
     * Contains the parser that parses a list of <code>AttendeeColumnDefinition</code> objects.
     */
    protected ListParser<AttendeeColumnDefinition> attendeeColumnDefinitionListParser;

    /**
     * Contains the parser that parses a list of <code>YodleePaymentType</code> objects.
     */
    protected ListParser<YodleePaymentType> yodleePaymentTypeListParser;

    /**
     * Contains the parser that parses a list of <code>CreditCard</code> objects.
     */
    protected ListParser<CreditCard> creditCardListParser;

    /**
     * Contains the parser parsing a list of <code>AffinityProgram</code> objects.
     */
    protected ListParser<AffinityProgram> affinityListParser;

    /**
     * Contains the parser parsing one or none of <code>TravelPointsConfig</code> object.
     */
    protected ItemParser<TravelPointsConfig> travelPointsConfigParser;

    /**
     * Contains the start tag used to register this parser.
     */
    private String startTag;

    /**
     * Constructs an instance of <code>UserConfigParser</code> for parsing a UserConfig object.
     * 
     * @param parser
     *            contains a reference to a <code>CommonParser</code> object.
     * @param startTag
     *            contains the start tag used to register this parser.
     */
    UserConfig(CommonParser parser, String startTag) {

        // Set the start tag and register this parser.
        this.startTag = startTag;
        parser.registerParser(this, startTag);

        // Register the currencies parser.
        currencyListParser = new ListParser<Currency>("Currencies", "Currency", Currency.class);
        parser.registerParser(currencyListParser, "Currencies");

        // Register the reimbursement currencies parser.
        reimbursementCurrencyListParser = new ListParser<Currency>("ReimbursmentCurrencies", "Currency", Currency.class);
        parser.registerParser(reimbursementCurrencyListParser, "ReimbursmentCurrencies");

        // Register the allowed car types parser.
        carTypeListParser = new ListParser<CarType>("AllowedCarTypes", "CarType", CarType.class);
        parser.registerParser(carTypeListParser, "AllowedCarTypes");

        // Register the expense policies parser.
        policyListParser = new ListParser<Policy>("ExpensePolicies", "Policy", Policy.class);
        parser.registerParser(policyListParser, "ExpensePolicies");

        // Register the expense confirmations parser.
        expenseConfirmationListParser = new ListParser<ExpenseConfirmation>("ExpenseConfirmations",
                "ExpenseConfirmation", ExpenseConfirmation.class);
        parser.registerParser(expenseConfirmationListParser, "ExpenseConfirmations");

        // Register the attendee types parser.
        attendeeTypeListParser = new ListParser<AttendeeType>("AttendeeTypes", "AttendeeType", AttendeeType.class);
        parser.registerParser(attendeeTypeListParser, "AttendeeTypes");

        // Register the attendee column definitions parser.
        attendeeColumnDefinitionListParser = new ListParser<AttendeeColumnDefinition>("AttendeeColumnDefs",
                "FormField", AttendeeColumnDefinition.class);
        parser.registerParser(attendeeColumnDefinitionListParser, "AttendeeColumnDefs");

        // Register the Yodlee payment types parser.
        yodleePaymentTypeListParser = new ListParser<YodleePaymentType>("YodleePaymentTypes", "ListItem",
                YodleePaymentType.class);
        parser.registerParser(yodleePaymentTypeListParser, "YodleePaymentTypes");

        // Register the credit card parser.
        creditCardListParser = new ListParser<CreditCard>("Cards", "CreditCard", CreditCard.class);
        parser.registerParser(creditCardListParser, "Cards");

        // Register the affinity program parser.
        affinityListParser = new ListParser<AffinityProgram>("AffinityPrograms", "AffinityProgram",
                AffinityProgram.class);
        parser.registerParser(affinityListParser, "AffinityPrograms");

        travelPointsConfigParser = new ItemParser<TravelPointsConfig>("TravelPointsConfig", TravelPointsConfig.class);
        parser.registerParser(travelPointsConfigParser, "TravelPointsConfig");
    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
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
            case TAG_FLAGS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    flags = text.trim();
                }
                break;
            }
            case TAG_ALLOWED_AIR_CLASSES_OF_SERVICE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    allowedAirClassesOfService = text.trim();
                }
                break;
            }
            case TAG_SHOW_GDS_NAME_IN_SEARCH_RESULTS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    showGDSNameInSearchResults = Parse.safeParseBoolean(text.trim());
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
                // Assemble data from the sub-parsers.
                currencies = currencyListParser.getList();
                reimbursementCurrencies = reimbursementCurrencyListParser.getList();
                allowedCarTypes = carTypeListParser.getList();
                expensePolicies = policyListParser.getList();
                expenseConfirmations = expenseConfirmationListParser.getList();
                attendeeTypes = attendeeTypeListParser.getList();
                attendeeColumnDefinitions = attendeeColumnDefinitionListParser.getList();
                yodleePaymentTypes = yodleePaymentTypeListParser.getList();
                creditCards = creditCardListParser.getList();
                affinityPrograms = affinityListParser.getList();
                travelPointsConfig = travelPointsConfigParser.getItem();
            }
        }
    }

}
