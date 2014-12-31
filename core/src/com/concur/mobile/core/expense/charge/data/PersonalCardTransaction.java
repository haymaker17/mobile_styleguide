package com.concur.mobile.core.expense.charge.data;

import java.util.Calendar;

import com.concur.mobile.platform.expense.list.dao.PersonalCardTransactionDAO;
import com.concur.mobile.platform.expense.smartexpense.dao.SmartExpenseDAO;
import com.concur.mobile.platform.util.Parse;

public class PersonalCardTransaction {

    public String pctKey;
    public Calendar datePosted;
    public String description;
    public Double amount;
    public String status;
    public String category;
    public String expKey;
    public String expName;
    public String rptKey;
    public String rptName;
    public String smartExpenseMeKey;
    public String smartExpenseId;

    /**
     * Contains a reference to a mobile entry associated with this personal card transaction.
     */
    public MobileEntry mobileEntry;

    protected PersonalCardTransaction() {
    }

    public PersonalCardTransaction(PersonalCardTransactionDAO personalCardTransactionDAO, SmartExpenseDAO smartExpense) {

        if (smartExpense != null) {
            mobileEntry = new MobileEntry(smartExpense);
            smartExpenseId = smartExpense.getSmartExpenseId();
        }

        pctKey = personalCardTransactionDAO.getPctKey();
        datePosted = personalCardTransactionDAO.getDatePosted();
        description = personalCardTransactionDAO.getDescription();
        amount = personalCardTransactionDAO.getAmount();
        status = personalCardTransactionDAO.getStatus();
        category = personalCardTransactionDAO.getCategory();
        expKey = personalCardTransactionDAO.getExpKey();
        expName = personalCardTransactionDAO.getExpName();
        smartExpenseMeKey = personalCardTransactionDAO.getSmartExpenseMeKey();
    }

    public PersonalCardTransaction(SmartExpenseDAO smartExpense) {

        mobileEntry = new MobileEntry(smartExpense);
        pctKey = smartExpense.getPctKey();
        datePosted = smartExpense.getTransactionDate();
        description = smartExpense.getVendorDescription();
        amount = smartExpense.getTransactionAmount();
        status = ""; // E-DAO: Missing endpoint.
        category = smartExpense.getCardCategoryName();
        expKey = smartExpense.getExpKey();
        expName = smartExpense.getExpenseName();
        smartExpenseMeKey = smartExpense.getMeKey(); // E-DAO: VERY POSSIBLY THE WRONG CALL!
        smartExpenseId = smartExpense.getSmartExpenseId();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////
    //
    // BELOW HERE BE SAX DRAGONS
    //
    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle the card level elements
     * 
     * @param localName
     */
    protected void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("Amount")) {
            amount = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("Category")) {
            category = cleanChars;
        } else if (localName.equalsIgnoreCase("DatePosted")) {
            datePosted = Parse.parseXMLTimestamp(cleanChars);
        } else if (localName.equalsIgnoreCase("Description")) {
            description = cleanChars;
        } else if (localName.equalsIgnoreCase("ExpKey")) {
            expKey = cleanChars;
        } else if (localName.equalsIgnoreCase("ExpName")) {
            expName = cleanChars;
        } else if (localName.equalsIgnoreCase("PctKey")) {
            pctKey = cleanChars;
        } else if (localName.equalsIgnoreCase("Status")) {
            status = cleanChars;
        } else if (localName.equalsIgnoreCase("SmartExpense")) {
            smartExpenseMeKey = cleanChars;
        } else if (localName.equalsIgnoreCase(MobileEntry.MobileEntrySAXHandler.MOBILE_ENTRY)) {

        }
    }
}
