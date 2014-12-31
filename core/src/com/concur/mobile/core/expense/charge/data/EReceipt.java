package com.concur.mobile.core.expense.charge.data;

import java.io.Serializable;
import java.util.Calendar;

import android.text.TextUtils;

import com.concur.mobile.platform.expense.smartexpense.dao.SmartExpenseDAO;

/**
 * An implementation of <code>EReceipt</code>.
 * 
 * @author yiwenw
 */
public class EReceipt implements Serializable {

    private static final long serialVersionUID = 6888968291731597094L;

    private static String CLS_TAG = EReceipt.class.getSimpleName();

    /**
     * Contains the transaction currency code.
     */
    private String crnCode;

    /**
     * Contains the expense type key.
     */
    private String expKey;

    /**
     * Contains the expense type name.
     */
    private String expName;

    /**
     * Contains the expense location name.
     */
    private String locationName;

    /**
     * Contains the expense vendor description.
     */
    private String vendorDescription;

    /**
     * Contains the expense transaction amount.
     */
    private Double transactionAmount;

    /**
     * Contains the expense entry transaction date as a <code>Calendar</code> object.
     */
    private Calendar transactionDate;

    /**
     * Contains the e-receipt id.
     */
    private String eReceiptId;

    /**
     * Contains the e-receipt image id.
     */
    private String eReceiptImageId;

    /**
     * Containing key of linked MobileEntry with edited info on this e-receipt
     */
    private String smartExpenseMeKey;

    /**
     * Contains the smart expense id
     */
    public String smartExpenseId;

    /**
     * Contains the comment, from matched mobile entry
     */
    public String comment;

    protected EReceipt() {
    }

    public EReceipt(SmartExpenseDAO smartExpense) {
        crnCode = smartExpense.getCrnCode();
        expKey = smartExpense.getExpKey();
        if (TextUtils.isEmpty(expKey)) {
            expKey = smartExpense.getEReceiptType();
        }

        expName = smartExpense.getExpenseName();
        if (TextUtils.isEmpty(expName)) {
            expName = smartExpense.getEReceiptType();
        }

        comment = smartExpense.getComment();
        locationName = smartExpense.getLocName();
        vendorDescription = smartExpense.getVendorDescription();
        transactionAmount = smartExpense.getTransactionAmount();
        transactionDate = smartExpense.getTransactionDate();
        eReceiptId = smartExpense.getEReceiptId();
        eReceiptImageId = smartExpense.getEReceiptImageId();
        smartExpenseMeKey = smartExpense.getMeKey(); // E-DAO: Correct call here?
        smartExpenseId = smartExpense.getSmartExpenseId();
        
        if (!TextUtils.isEmpty(smartExpense.getMobileReceiptImageId())) {
            eReceiptImageId = smartExpense.getMobileReceiptImageId();
        } else if (!TextUtils.isEmpty(smartExpense.getEReceiptImageId())) {
            eReceiptImageId = smartExpense.getEReceiptImageId();
        } else if (!TextUtils.isEmpty(smartExpense.getCctReceiptImageId())) {
            eReceiptImageId = smartExpense.getCctReceiptImageId();
        } else if (!TextUtils.isEmpty(smartExpense.getReceiptImageId())) {
            eReceiptImageId = smartExpense.getReceiptImageId();
        }
    }

    /**
     * Gets transaction currency code
     * 
     * @return the transaction currency code
     */
    public String getCrnCode() {
        return crnCode;
    }

    /**
     * Gets expense type key
     * 
     * @return expense type key
     */
    public String getExpKey() {
        return expKey;
    }

    /**
     * Gets expense type name
     * 
     * @return expense type name
     */
    public String getExpName() {
        return expName;
    }

    /**
     * Gets location name
     * 
     * @return location name
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * Gets vendor description
     * 
     * @return vendor description
     */
    public String getVendorDescription() {
        return vendorDescription;
    }

    /**
     * Gets comment
     * 
     * @return comment
     */
    public String getComment() {
        return comment;
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
     * Get the transaction amount.
     * 
     * @return the transaction amount.
     */
    public Double getTransactionAmount() {
        return transactionAmount;
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
     * Gets the e-receipt id.
     * 
     * @return the e-receipt id
     */
    public String getEReceiptId() {
        return eReceiptId;
    }

    /**
     * Gets the e-receipt image id.
     * 
     * @return the e-receipt image id
     */
    public String getEReceiptImageId() {
        return eReceiptImageId;
    }

    // TODO - remove. Helper API to test EReceipt before DAO available
    public static EReceipt convertFromReceiptCapture(MobileEntry src) {
        EReceipt result = new EReceipt();
        result.crnCode = src.getCrnCode();
        result.eReceiptId = src.getMeKey();
        result.eReceiptImageId = src.getReceiptImageId();
        result.expKey = src.getExpKey();
        result.expName = src.getExpName();
        result.locationName = src.getLocationName();
        result.transactionAmount = src.getTransactionAmount();
        result.transactionDate = src.getTransactionDateCalendar();
        result.vendorDescription = src.getVendorName();
        return result;
    }
}
