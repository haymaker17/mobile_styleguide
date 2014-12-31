package com.concur.mobile.platform.expense.list.test;

import java.util.Calendar;

import org.simpleframework.xml.Element;

import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;

public class CorporateCardTransaction {

    /**
     * Contains the expense type.
     */
    ExpenseTypeEnum type = ExpenseTypeEnum.CORPORATE_CARD;

    /**
     * Contains the card type code.
     */
    @Element(name = "CardTypeCode", required = false)
    String cardTypeCode;

    /**
     * Contains the card type name.
     */
    @Element(name = "CardTypeName", required = false)
    String cardTypeName;

    /**
     * Contains the corporate card transaction key.
     */
    @Element(name = "CctKey")
    String cctKey;

    /**
     * Contains the corporate card transaction type.
     */
    @Element(name = "CctType")
    String cctType;

    /**
     * Contains the description.
     */
    @Element(name = "Description", required = false)
    String description;

    /**
     * Contains whether the corporate card transaction has rich data.
     */
    @Element(name = "HasRichData")
    String hasRichDataStr;
    Boolean hasRichData;

    /**
     * Contains the "doing business as".
     */
    @Element(name = "DoingBusinessAs", required = false)
    String doingBusinessAs;

    /**
     * Contains the expense type key.
     */
    @Element(name = "ExpKey")
    String expenseKey;

    /**
     * Contains the expense type name.
     */
    @Element(name = "ExpName")
    String expenseName;

    /**
     * Contains the merchant city.
     */
    @Element(name = "MerchantCity", required = false)
    String merchantCity;

    /**
     * Contains the merchant country code.
     */
    @Element(name = "MerchantCtryCode", required = false)
    String merchantCountryCode;

    /**
     * Contains the merchant name.
     */
    @Element(name = "MerchantName", required = false)
    String merchantName;

    /**
     * Contains the merchant state.
     */
    @Element(name = "MerchantState", required = false)
    String merchantState;

    /**
     * Contains the matched smart expense mobile entry key.
     */
    @Element(name = "SmartExpense", required = false)
    String smartExpenseMeKey;

    /**
     * Contains the transaction amount.
     */
    @Element(name = "TransactionAmount")
    Double transactionAmount;

    /**
     * Contains the transaction currency code.
     */
    @Element(name = "TransactionCrnCode")
    String transactionCrnCode;

    /**
     * Contains the transaction date.
     */
    @Element(name = "TransactionDate")
    String transactionDateStr;
    Calendar transactionDate;

    /**
     * Contains a related mobile entry.
     */
    @Element(name = "MobileEntry", required = false)
    MobileEntry mobileEntry;

}
