package com.concur.mobile.platform.expense.list.test;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "AllExpenses", strict = false)
public class ExpenseListResponse {

    /**
     * Contains the corporate card transaction charges.
     */
    @ElementList(name = "CorporateCardTransactions", required = false)
    public List<CorporateCardTransaction> corporateCardTransactions;

    /**
     * Contains the mobile entries.
     */
    @ElementList(name = "Entries", required = false)
    public List<MobileEntry> mobileEntries;

    /**
     * Contains the personal cards.
     */
    @ElementList(name = "PersonalCards", required = false)
    public List<PersonalCard> personalCards;

    /**
     * Contains the receipt captures.
     */
    @ElementList(name = "ReceiptCaptures", required = false)
    public List<ReceiptCapture> receiptCaptures;

}
