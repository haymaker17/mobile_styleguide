package com.concur.mobile.platform.expense.list.test;

import java.util.Calendar;

import org.simpleframework.xml.Element;

import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;

public class PersonalCardTransaction {

    /**
     * Contains the expense type.
     */
    ExpenseTypeEnum type = ExpenseTypeEnum.PERSONAL_CARD;

    /**
     * Contains the personal card transaction key.
     */
    @Element(name = "PctKey")
    String pctKey;

    /**
     * Contains the posted date.
     */
    @Element(name = "DatePosted")
    String datePostedStr;
    Calendar datePosted;

    /**
     * Contains the description.
     */
    @Element(name = "Description")
    String description;

    /**
     * Contains the amount.
     */
    @Element(name = "Amount")
    Double amount;

    /**
     * Contains the currency code.
     */
    // This value should be assigned from the PersonalCard.
    String crnCode;

    /**
     * Contains the status.
     */
    @Element(name = "Status")
    String status;

    /**
     * Contains the category.
     */
    @Element(name = "Category")
    String category;

    /**
     * Contains the expense type key.
     */
    @Element(name = "ExpKey")
    String expKey;

    /**
     * Contains the expense type name.
     */
    @Element(name = "ExpName")
    String expName;

    /**
     * Contains the report key.
     */
    String rptKey;

    /**
     * Contains the report name.
     */
    String rptName;

    /**
     * Contains the smart expense mobile entry key.
     */
    @Element(name = "SmartExpense", required = false)
    String smartExpenseMeKey;

    /**
     * Contains a reference to an associated mobile entry.
     */
    @Element(name = "MobileEntry", required = false)
    MobileEntry mobileEntry;

}
