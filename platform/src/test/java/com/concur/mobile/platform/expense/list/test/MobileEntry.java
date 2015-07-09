package com.concur.mobile.platform.expense.list.test;

import java.util.Calendar;

import org.simpleframework.xml.Element;

import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;

public class MobileEntry {

    /**
     * Contains the transaction currency code.
     */
    @Element(name = "CrnCode", required = false)
    String crnCode;

    /**
     * Contains the expense entry type key.
     */
    @Element(name = "ExpKey", required = false)
    String expKey;

    /**
     * Contains the expense name.
     */
    @Element(name = "ExpName", required = false)
    String expName;

    /**
     * Contains the expense location name.
     */
    @Element(name = "LocationName", required = false)
    String locationName;

    /**
     * Contains the expense vendor name.
     */
    @Element(name = "VendorName", required = false)
    String vendorName;

    /**
     * Contains the expense entry type.
     */
    ExpenseTypeEnum type = ExpenseTypeEnum.CASH;

    /**
     * Contains the mobile expense entry key.
     */
    @Element(name = "MeKey")
    String meKey;

    /**
     * Contains the personal card account key containing a transaction associated with this mobile entry.
     */
    String pcaKey;

    /**
     * Contains the personal card transaction key for a transaction associated with this mobile entry.
     */
    String pctKey;

    /**
     * Contains the corporate card transaction key for a corporate card transaction associated with this mobile entry.
     */
    String cctKey;

    /**
     * Contains the receipt capture key for a receipt capture transaction associated with this mobile entry.
     */
    String rcKey;

    /**
     * Contains the expense transaction amount.
     */
    @Element(name = "TransactionAmount", required = false)
    Double transactionAmount;

    /**
     * Contains the expense transaction date.
     */
    @Element(name = "TransactionDate", required = false)
    String transactionDateStr;
    Calendar transactionDate;

    /**
     * Contains whether the expense entry has a receipt image.
     */
    @Element(name = "HasReceiptImage", required = false)
    String hasReceiptImageStr;
    boolean hasReceiptImage;

    /**
     * Contains the receipt image id.
     */
    @Element(name = "ReceiptImageId", required = false)
    String receiptImageId;

    /**
     * Contains the base-64 encoded receipt image data.
     */
    @Element(name = "ReceiptImage", required = false)
    String receiptImageData;

    /**
     * Contains the expense comment.
     */
    @Element(name = "Comment", required = false)
    String comment;

}
