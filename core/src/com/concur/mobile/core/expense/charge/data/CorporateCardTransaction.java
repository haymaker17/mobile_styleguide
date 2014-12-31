/**
 * 
 */
package com.concur.mobile.core.expense.charge.data;

import java.util.ArrayList;
import java.util.Calendar;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.expense.charge.data.MobileEntry.MobileEntrySAXHandler;
import com.concur.mobile.platform.expense.smartexpense.dao.SmartExpenseDAO;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a model of a corporate card transaction.
 * 
 * @author AndrewK
 */
public class CorporateCardTransaction {

    // private static final String CLS_TAG = CorporateCardTransaction.class.getSimpleName();

    private String cardTypeCode;

    private String cardTypeName;

    private String cctKey;

    private String description;

    private String doingBusinessAs;

    private String expenseKey;

    private String expenseName;

    private String merchantCity;

    private String merchantCountryCode;

    private String merchantName;

    private String merchantState;

    private String smartExpenseMeKey;

    private Double transactionAmount;

    private String transactionCrnCode;

    private Calendar transactionDate;

    public String smartExpenseId;

    /**
     * Contains a reference to a mobile entry associated with this personal card transaction.
     */
    private MobileEntry mobileEntry;

    protected CorporateCardTransaction() {
    }

    public CorporateCardTransaction(SmartExpenseDAO smartExpense) {

        mobileEntry = new MobileEntry(smartExpense);
        cardTypeCode = smartExpense.getCardTypeCode();
        cardTypeName = ""; // E-DAO: seems to come back empty all the time anyways.
        cctKey = smartExpense.getCctKey();
        description = smartExpense.getVendorDescription(); // E-DAO: Could be a few things here.
        doingBusinessAs = smartExpense.getDoingBusinessAs();
        expenseKey = smartExpense.getExpKey();
        expenseName = smartExpense.getExpenseName();
        merchantCity = smartExpense.getMerchantCity();
        merchantCountryCode = smartExpense.getMerchantCountryCode();
        merchantName = smartExpense.getMerchantName();
        merchantState = smartExpense.getMerchantState();
        smartExpenseMeKey = smartExpense.getMeKey(); // E-DAO: VERY POSSIBLY THE WRONG CALL!
        transactionAmount = smartExpense.getTransactionAmount();
        transactionCrnCode = smartExpense.getTransactionCurrencyCode();
        transactionDate = smartExpense.getTransactionDate();
        smartExpenseId = smartExpense.getSmartExpenseId();
    }

    /**
     * Gets the card type code.
     * 
     * @return the card type code.
     */
    public String getCardTypeCode() {
        return cardTypeCode;
    }

    /**
     * Gets the card type name.
     * 
     * @return the card type name.
     */
    public String getCardTypeName() {
        return cardTypeName;
    }

    /**
     * Gets the corporate card transaction key.
     * 
     * @return the corporate card transaction key.
     */
    public String getCctKey() {
        return cctKey;
    }

    /**
     * Gets the description.
     * 
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the "doing business as" name.
     * 
     * @return the "doing business as" name.
     */
    public String getDoingBusinessAs() {
        return doingBusinessAs;
    }

    /**
     * Gets the expense key.
     * 
     * @return the expense key.
     */
    public String getExpenseKey() {
        return expenseKey;
    }

    /**
     * Gets the expense name.
     * 
     * @return the expense name.
     */
    public String getExpenseName() {
        return expenseName;
    }

    /**
     * Gets the merchant city.
     * 
     * @return the merchant city.
     */
    public String getMerchantCity() {
        return merchantCity;
    }

    /**
     * Gets the merchant country code.
     * 
     * @return the merchant country code.
     */
    public String getMerchantCountryCode() {
        return merchantCountryCode;
    }

    /**
     * Gets the merchant name.
     * 
     * @return the merchant name.
     */
    public String getMerchantName() {
        return merchantName;
    }

    /**
     * Gets the merchant state.
     * 
     * @return the merchant state.
     */
    public String getMerchantState() {
        return merchantState;
    }

    /**
     * Gets the smart expense mobile entry key.
     * 
     * @return the smart expense mobile entry key if this corporate card has a smart expense hint; <code>null</code> otherwise.
     */
    public String getSmartExpenseMeKey() {
        return smartExpenseMeKey;
    }

    /**
     * Get the transaction amount.
     * 
     * @return the transaction amount.
     */
    public Double getTransactionAmount() {
        return transactionAmount;
    }

    /**
     * Gets the transaction date.
     * 
     * @return the transaction date.
     */
    public Calendar getTransactionDate() {
        return transactionDate;
    }

    /**
     * Gets the transaction currency code.
     * 
     * @return the transaction currency code.
     */
    public String getTransactionCrnCode() {
        return transactionCrnCode;
    }

    /**
     * Get the mobile entry associated with this card transaction.
     * 
     * @return the mobile entry associated with this card transaction.
     */
    public MobileEntry getMobileEntry() {
        return mobileEntry;
    }

    /**
     * Set the mobile entry associated with this card transaction.
     * 
     * @param mobileEntry
     *            the mobile entry associated with this card transaction.
     */
    public void setMobileEntry(MobileEntry mobileEntry) {
        this.mobileEntry = mobileEntry;
    }

    /**
     * Helper class to handle parsing of card XML.
     */
    protected static class CorporateCardSAXHandler extends DefaultHandler {

        // private static final String CLS_TAG = CorporateCardTransaction.CLS_TAG + "."
        // + CorporateCardSAXHandler.class.getSimpleName();

        private static final String CORPORATE_CARD_TRANSACTION = "CorporateCardTransaction";
        private static final String CARD_TYPE_CODE = "CardTypeCode";
        private static final String CARD_TYPE_NAME = "CardTypeName";
        private static final String CCT_KEY = "CctKey";
        private static final String DESCRIPTION = "Description";
        private static final String DOING_BUSINESS_AS = "DoingBusinessAs";
        private static final String EXPENSE_KEY = "ExpKey";
        private static final String EXPENSE_NAME = "ExpName";
        private static final String MERCHANT_CITY = "MerchantCity";
        private static final String MERCHANT_COUNTRY_CODE = "MerchantCtryCode";
        private static final String MERCHANT_NAME = "MerchantName";
        private static final String MERCHANT_STATE = "MerchantState";
        private static final String SMART_EXPENSE = "SmartExpense";
        private static final String TRANSACTION_AMOUNT = "TransactionAmount";
        private static final String TRANSACTION_CRN_CODE = "TransactionCrnCode";
        private static final String TRANSACTION_DATE = "TransactionDate";
        private static final String MOBILE_ENTRY = "MobileEntry";

        /**
         * Contains the list of corporate card transactions.
         */
        private ArrayList<CorporateCardTransaction> transactions = new ArrayList<CorporateCardTransaction>();

        /**
         * Contains a reference to the current transaction being parsed.
         */
        private CorporateCardTransaction transaction;

        /**
         * Contains a reference to a parser for mobile entries.
         */
        private MobileEntry.MobileEntrySAXHandler mobileEntryHandler;

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        public ArrayList<CorporateCardTransaction> getTransactions() {
            return transactions;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if (mobileEntryHandler != null) {
                mobileEntryHandler.characters(ch, start, length);
            } else {
                chars.append(ch, start, length);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (mobileEntryHandler != null) {
                mobileEntryHandler.startElement(uri, localName, qName, attributes);
            } else if (localName.equalsIgnoreCase(MOBILE_ENTRY)) {
                mobileEntryHandler = new MobileEntrySAXHandler();
                mobileEntryHandler.mobileEntry = new MobileEntry();
                mobileEntryHandler.mobileEntry.setEntryType(Expense.ExpenseEntryType.CORPORATE_CARD);
            } else if (localName.equalsIgnoreCase(CORPORATE_CARD_TRANSACTION)) {
                transaction = new CorporateCardTransaction();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (mobileEntryHandler != null) {
                if (localName.equalsIgnoreCase(MobileEntry.MobileEntrySAXHandler.MOBILE_ENTRY)) {
                    // End the mobile entry element.
                    transaction.mobileEntry = mobileEntryHandler.mobileEntry;
                    // Anything parsed in at this point is a NORMAL entry. It exists on the server.
                    transaction.mobileEntry.setStatus(MobileEntryStatus.NORMAL);
                    mobileEntryHandler = null;
                } else {
                    mobileEntryHandler.endElement(uri, localName, qName);
                }
            } else if (localName.equalsIgnoreCase(CORPORATE_CARD_TRANSACTION)) {
                // End the transaction element
                // Ensure that the 'cctKey' on an associated mobile entry is set.
                if (transaction.mobileEntry != null) {
                    transaction.mobileEntry.setCctKey(transaction.cctKey);
                }
                transactions.add(transaction);
                transaction = null;
            } else if (localName.equalsIgnoreCase(CARD_TYPE_CODE)) {
                transaction.cardTypeCode = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(CARD_TYPE_NAME)) {
                transaction.cardTypeName = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(CCT_KEY)) {
                transaction.cctKey = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(DESCRIPTION)) {
                transaction.description = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(DOING_BUSINESS_AS)) {
                transaction.doingBusinessAs = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(EXPENSE_KEY)) {
                transaction.expenseKey = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(EXPENSE_NAME)) {
                transaction.expenseName = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(MERCHANT_CITY)) {
                transaction.merchantCity = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(MERCHANT_COUNTRY_CODE)) {
                transaction.merchantCountryCode = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(MERCHANT_NAME)) {
                transaction.merchantName = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(MERCHANT_STATE)) {
                transaction.merchantState = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(SMART_EXPENSE)) {
                transaction.smartExpenseMeKey = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(TRANSACTION_AMOUNT)) {
                transaction.transactionAmount = Parse.safeParseDouble(chars.toString().trim());
            } else if (localName.equalsIgnoreCase(TRANSACTION_CRN_CODE)) {
                transaction.transactionCrnCode = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(TRANSACTION_DATE)) {
                transaction.transactionDate = Parse.parseXMLTimestamp(chars.toString().trim());
            } else {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML node '" + localName + "'.");
            }
            chars.setLength(0);
        }

    }

}
