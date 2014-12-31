package com.concur.mobile.platform.expense.list.test;

import java.util.Calendar;

import org.simpleframework.xml.Element;

import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;

public class ReceiptCapture {

    /**
     * Contains the expense type.
     */
    ExpenseTypeEnum type = ExpenseTypeEnum.RECEIPT_CAPTURE;

    /**
     * Contains the transaction currency code.
     */
    @Element(name = "CrnCode")
    String crnCode;

    /**
     * Contains the expense entry type key.
     */
    @Element(name = "ExpKey")
    String expKey;

    /**
     * Contains the expense name.
     */
    @Element(name = "ExpName")
    String expName;

    /**
     * Contains the expense vendor name.
     */
    @Element(name = "VendorName", required = false)
    String vendorName;

    /**
     * Contains the receipt capture expense key
     */
    @Element(name = "RcKey")
    String rcKey;

    /**
     * Contains smart expense id which is same as rcKey
     */
    @Element(name = "SmartExpenseId")
    String smartExpId;

    /**
     * Contains the expense transaction amount.
     */
    @Element(name = "TransactionAmount", required = false)
    Double transactionAmount;

    /**
     * Contains the expense transaction date.
     */
    @Element(name = "TransactionDate")
    String transactionDateStr;
    Calendar transactionDate;

    /**
     * Contains the receipt image id.
     */
    @Element(name = "ReceiptImageId")
    String receiptImageId;

}
