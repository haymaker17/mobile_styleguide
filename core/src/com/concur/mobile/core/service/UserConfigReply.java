package com.concur.mobile.core.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.expense.data.CurrencyType;
import com.concur.mobile.core.expense.data.ExpensePolicy;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.report.data.AttendeeType;
import com.concur.mobile.core.expense.report.data.ExpenseConfirmation;
import com.concur.mobile.core.travel.car.data.CarType;
import com.concur.mobile.core.travel.data.TravelPointsConfig;
import com.concur.mobile.platform.util.Parse;

public class UserConfigReply extends ServiceReply {

    // private static final String CLS_TAG = UserConfigReply.class.getSimpleName();

    public UserConfig config;
    public String xmlReply;

    public UserConfigReply() {
        config = new UserConfig();
    }

    public static UserConfigReply parseXMLReply(String responseXml) {

        UserConfigReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            UserConfigReplySAXHandler handler = new UserConfigReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class UserConfigReplySAXHandler extends DefaultHandler {

        // private static final String CLS_TAG = UserConfigReply.CLS_TAG + "."
        // + UserConfigReplySAXHandler.class.getSimpleName();

        private static final String ALLOWED_AIR_CLASS_SERVICE = "AllowedAirClassesOfService";
        private static final String ALLOWED_CAR_TYPES = "AllowedCarTypes";
        private static final String CAR_TYPE = "CarType";
        private static final String INFO = "Info";
        private static final String CURRENCIES = "Currencies";
        private static final String REIMBURSEMENT_CURRENCIES = "ReimbursmentCurrencies";
        private static final String RESPONSE = "Response";

        private static final String ATTENDEE_TYPES = "AttendeeTypes";
        private static final String ATTENDEE_TYPE = "AttendeeType";

        private static final String EXPENSE_CONFIRMATIONS = "ExpenseConfirmations";
        private static final String EXPENSE_CONFIRMATION = "ExpenseConfirmation";

        private static final String EXPENSE_POLICIES = "ExpensePolicies";
        private static final String EXPENSE_POLICY = "Policy";

        private static final String YODLEE_PAYMENT_TYPES = "YodleePaymentTypes";
        private static final String LIST_ITEM = "ListItem";
        private static final String TRAVEL_POINTS_CONFIG = "TravelPointsConfig";
        // MOB-15911 - flag to show GDSNames in travel search results - only for DEV & QA
        private static final String SHOW_GDS_NAME = "showGDSNameInSearchResults";
        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.

        private boolean inAllowedAirClass;
        private boolean inAllowedCars;
        private boolean inInfo;
        private boolean inAttendeeTypes;
        private boolean inExpenseConfirmations;
        private boolean inExpensePolicies;
        private boolean inYodleePaymentTypes;
        private boolean inTravelPointsConfig;

        // Holders for our parsed data
        private UserConfigReply reply;
        private CarType carType;
        private CurrencyType.CurrencyTypeSAXHandler currencyHandler;
        private CurrencyType.CurrencyTypeSAXHandler reimbursementCurrencyHandler;
        private AttendeeType attendeeType;
        private ExpenseConfirmation expenseConfirmation;
        private ExpensePolicy expensePolicy;
        private ListItem listItem;
        private TravelPointsConfig tpConfig;

        protected UserConfigReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            chars = new StringBuilder();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if (currencyHandler != null) {
                currencyHandler.characters(ch, start, length);
            } else if (reimbursementCurrencyHandler != null) {
                reimbursementCurrencyHandler.characters(ch, start, length);
            } else {
                chars.append(ch, start, length);
            }
        }

        /**
         * Handle the opening of all elements. Create data objects as needed for use in endElement().
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(RESPONSE)) {
                reply = new UserConfigReply();
            } else if (localName.equalsIgnoreCase(ALLOWED_AIR_CLASS_SERVICE)) {
                inAllowedAirClass = true;
                reply.config.allowedAirClassService = new ArrayList<String>();
            } else if (localName.equalsIgnoreCase(ALLOWED_CAR_TYPES)) {
                reply.config.allowedCarTypes = new ArrayList<CarType>();
                inAllowedCars = true;
            } else if (localName.equalsIgnoreCase(CAR_TYPE)) {
                carType = new CarType();
            } else if (localName.equalsIgnoreCase(INFO)) {
                inInfo = true;
            } else if (localName.equalsIgnoreCase(CURRENCIES)) {
                currencyHandler = new CurrencyType.CurrencyTypeSAXHandler();
            } else if (currencyHandler != null) {
                currencyHandler.startElement(uri, localName, qName, attributes);
            } else if (localName.equalsIgnoreCase(REIMBURSEMENT_CURRENCIES)) {
                reimbursementCurrencyHandler = new CurrencyType.CurrencyTypeSAXHandler();
            } else if (reimbursementCurrencyHandler != null) {
                reimbursementCurrencyHandler.startElement(uri, localName, qName, attributes);
            } else if (localName.equalsIgnoreCase(ATTENDEE_TYPES)) {
                reply.config.attendeeTypes = new ArrayList<AttendeeType>();
                inAttendeeTypes = true;
            } else if (localName.equalsIgnoreCase(ATTENDEE_TYPE)) {
                attendeeType = new AttendeeType();
            } else if (localName.equalsIgnoreCase(EXPENSE_CONFIRMATIONS)) {
                reply.config.expenseConfirmations = new ArrayList<ExpenseConfirmation>();
                inExpenseConfirmations = true;
            } else if (localName.equalsIgnoreCase(EXPENSE_CONFIRMATION)) {
                expenseConfirmation = new ExpenseConfirmation();
            } else if (localName.equalsIgnoreCase(EXPENSE_POLICIES)) {
                reply.config.expensePolicies = new ArrayList<ExpensePolicy>();
                inExpensePolicies = true;
            } else if (localName.equalsIgnoreCase(EXPENSE_POLICY)) {
                expensePolicy = new ExpensePolicy();
            } else if (localName.equalsIgnoreCase(YODLEE_PAYMENT_TYPES)) {
                reply.config.yodleePaymentTypes = new ArrayList<ListItem>();
                inYodleePaymentTypes = true;
            } else if (inYodleePaymentTypes && localName.equalsIgnoreCase(LIST_ITEM)) {
                listItem = new ListItem();
            } else if (localName.equalsIgnoreCase(TRAVEL_POINTS_CONFIG)) {
                inTravelPointsConfig = true;
                tpConfig = new TravelPointsConfig();
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();
            if (inAllowedAirClass) {
                if (localName.equalsIgnoreCase(ALLOWED_AIR_CLASS_SERVICE)) {
                    inAllowedAirClass = false;
                    reply.config.populateAirClass(cleanChars);
                }
            } else if (inAllowedCars) {
                if (localName.equalsIgnoreCase(CAR_TYPE)) {
                    // Sanity
                    if (carType != null) {
                        reply.config.allowedCarTypes.add(carType);
                        carType = null;
                    }
                } else if (localName.equalsIgnoreCase(ALLOWED_CAR_TYPES)) {
                    // Leaving car types
                    inAllowedCars = false;
                    reply.config.populateCarTypes();
                } else {
                    // Sanity
                    if (carType != null) {
                        // hand off to object
                        carType.handleElement(localName, cleanChars);
                    }
                }
            } else if (inYodleePaymentTypes) {
                if (localName.equalsIgnoreCase(LIST_ITEM)) {
                    // Sanity
                    if (listItem != null) {
                        reply.config.yodleePaymentTypes.add(listItem);
                        listItem = null;
                    }
                } else if (localName.equalsIgnoreCase(YODLEE_PAYMENT_TYPES)) {
                    inYodleePaymentTypes = false;
                } else {
                    // Sanity check.
                    if (listItem != null) {
                        // Hand off to object.
                        listItem.handleElement(localName, cleanChars);
                    }
                }
            } else if (inAttendeeTypes) {
                if (localName.equalsIgnoreCase(ATTENDEE_TYPE)) {
                    // Sanity
                    if (attendeeType != null) {
                        reply.config.attendeeTypes.add(attendeeType);
                        attendeeType = null;
                    }
                } else if (localName.equalsIgnoreCase(ATTENDEE_TYPES)) {
                    // Leaving attendee types
                    inAttendeeTypes = false;
                } else {
                    // Sanity
                    if (attendeeType != null) {
                        // hand off to object
                        attendeeType.handleElement(localName, cleanChars);
                    }
                }
            } else if (inExpenseConfirmations) {
                if (localName.equalsIgnoreCase(EXPENSE_CONFIRMATION)) {
                    // Sanity
                    if (expenseConfirmation != null) {
                        reply.config.expenseConfirmations.add(expenseConfirmation);
                        expenseConfirmation = null;
                    }
                } else if (localName.equalsIgnoreCase(EXPENSE_CONFIRMATIONS)) {
                    // Leaving expense confirmations.
                    inExpenseConfirmations = false;
                } else {
                    // Sanity
                    if (expenseConfirmation != null) {
                        // hand off to object
                        expenseConfirmation.handleElement(localName, cleanChars);
                    }
                }
            } else if (inExpensePolicies) {
                if (localName.equalsIgnoreCase(EXPENSE_POLICY)) {
                    // Sanity
                    if (expensePolicy != null) {
                        reply.config.expensePolicies.add(expensePolicy);
                        expensePolicy = null;
                    }
                } else if (localName.equalsIgnoreCase(EXPENSE_POLICIES)) {
                    // Leaving expense policies.
                    inExpensePolicies = false;
                } else {
                    // Sanity
                    if (expensePolicy != null) {
                        // hand off to object
                        expensePolicy.handleElement(localName, cleanChars);
                    }
                }
            } else if (inInfo) {
                if (localName.equalsIgnoreCase(INFO)) {
                    // Leaving info
                    inInfo = false;
                } else {
                    // hand off to object
                    reply.config.handleElement(localName, cleanChars);
                }
            } else if (currencyHandler != null) {
                if (localName.equalsIgnoreCase(CURRENCIES)) {
                    ArrayList<CurrencyType> currencies = currencyHandler.getCurrencyTypes();
                    reply.config.currencies = reply.getCurrencyItems(currencies);
                    currencyHandler = null;
                } else {
                    currencyHandler.endElement(uri, localName, qName);
                }
            } else if (reimbursementCurrencyHandler != null) {
                if (localName.equalsIgnoreCase(REIMBURSEMENT_CURRENCIES)) {
                    ArrayList<CurrencyType> currencies = reimbursementCurrencyHandler.getCurrencyTypes();
                    reply.config.reimbursementCurrencies = reply.getCurrencyItems(currencies);
                    reimbursementCurrencyHandler = null;
                } else {
                    reimbursementCurrencyHandler.endElement(uri, localName, qName);
                }
            } else if (inTravelPointsConfig) {
                if (localName.equalsIgnoreCase(TRAVEL_POINTS_CONFIG)) {
                    inTravelPointsConfig = false;
                    reply.config.travelPointsConfig = tpConfig;
                } else {
                    tpConfig.handleElement(localName, cleanChars);
                }
            } else if (localName.equalsIgnoreCase(SHOW_GDS_NAME)) {
                reply.config.showGDSNameInSearchResults = Parse.safeParseBoolean(cleanChars);
            } else if (localName.equalsIgnoreCase(RESPONSE)) {
                // No-op.
            } else {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML tag '" + localName + "' with value '" +
                // chars.toString() + "'.");
            }

            chars.setLength(0);
        }
    }

    public ArrayList<ListItem> getCurrencyItems(ArrayList<CurrencyType> currencies) {
        ArrayList<ListItem> result = new ArrayList<ListItem>(currencies.size());
        for (CurrencyType currencyType : currencies) {
            ListItem item = new ListItem(currencyType);
            result.add(item);
        }
        return result;
    }

}
