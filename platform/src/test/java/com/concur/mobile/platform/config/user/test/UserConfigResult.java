package com.concur.mobile.platform.config.user.test;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Provides a model of user config response information. <br>
 * Set strict to false to ignore <AttendeeColumnDefs> subtree. <br>
 * Alternatively, you can do so in code <br>
 * Root root = serializer.read(Root.class, source, false);
 */
@Root(name = "UserConfigResult", strict = false)
public class UserConfigResult {

    /**
     * Contains the allowed car types.
     */
    @ElementList(name = "AllowedCarTypes", required = false)
    public List<CarType> allowedCarTypes;

    /**
     * Contains the expense currencies.
     */
    @ElementList(name = "Currencies", required = false)
    public List<Currency> currencies;

    /**
     * Contains the expense reimbursement currencies.
     */
    @ElementList(name = "ReimbursementCurrencies", required = false)
    public List<Currency> reimbursementCurrencies;

    /**
     * Contains the expense attendee types.
     */
    @ElementList(name = "AttendeeTypes", required = false)
    public List<AttendeeType> attendeeTypes;

    /**
     * Contains the allowed expense confirmations.
     */
    @ElementList(name = "ExpenseConfirmations", required = false)
    public List<ExpenseConfirmation> expenseConfirmations;

    /**
     * Contains the allowed expense policies.
     */
    @ElementList(name = "ExpensePolicies", required = false)
    public List<ExpensePolicy> expensePolicies;

    /**
     * Contains the Yodlee payment types.
     */
    @ElementList(name = "YodleePaymentTypes", required = false)
    public List<YodleePaymentType> yodleePaymentTypes;

    /**
     * Contains the TravelPointsConfig.
     */
    @Element(name = "TravelPointsConfig", required = false)
    public TravelPointsConfig travelPointsConfig;

    /**
     * Contains the AttendeeColumnDefinition.
     */
    @ElementList(name = "AttendeeColumnDefs", required = false)
    public List<AttendeeColumnDefinition> attendeeColumnDefinitions;
    /**
     * Contains the flag on whether to show GDS name in search results.
     */
    @Element(name = "ShowGDSNameInSearchResults", required = false)
    public Boolean showGDSNameInSearchResults;

    /**
     * Contains the set of allowed air classes of service.
     */
    @Element(name = "AllowedAirClassesOfService", required = false)
    public String allowedAirClassesOfService;
    public List<String> allowedAirClassServiceList;

    /**
     * Contains the set of allowed air classes of service.
     */
    @Element(name = "Flags", required = false)
    public String flags;
}
