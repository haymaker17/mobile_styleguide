package com.concur.mobile.platform.expense.list.test;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

public class PersonalCard {

    /**
     * Contains the personal card account key.
     */
    @Element(name = "PcaKey")
    String pcaKey;

    /**
     * Contains the card name.
     */
    @Element(name = "CardName")
    String cardName;

    /**
     * Contains the account number last four digits
     */
    @Element(name = "AccountNumberLastFour")
    String acctNumLastFour;

    /**
     * Contains the currency code.
     */
    @Element(name = "CrnCode")
    String crnCode;

    /**
     * Contains the list of personal card transactions.
     */
    @ElementList(name = "Transactions", required = false)
    List<PersonalCardTransaction> transactions;

}
