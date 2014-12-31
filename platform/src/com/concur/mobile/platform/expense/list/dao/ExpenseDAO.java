package com.concur.mobile.platform.expense.list.dao;

import java.util.Calendar;

import android.content.Context;

import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;

/**
 * An interface describing an <code>Expense</code> Data Access Object (DAO).
 * 
 * @author andrewk
 */
public interface ExpenseDAO {

    /**
     * Gets the expense type.
     * 
     * @return returns the expense type.
     */
    public ExpenseTypeEnum getType();

    /**
     * Gets the transaction amount.
     * 
     * @return the transaction amount.
     */
    public Double getTransactionAmount();

    /**
     * Gets the transaction currency code.
     * 
     * @return the transaction currency code.
     */
    public String getTransactionCurrencyCode();

    /**
     * Gets the transaction date.
     * 
     * @return the transaction date.
     */
    public Calendar getTransactionDate();

    /**
     * Gets the vendor name.
     * 
     * @return the vendor name.
     */
    public String getVendorName();

    /**
     * Gets the expense id.
     * 
     * @return the expense id.
     */
    public Long getExpenseId();

    /**
     * Gets the cash transaction.
     * 
     * @return the cash transaction.
     */
    public MobileEntryDAO getMobileEntryDAO();

    /**
     * Gets the receipt capture.
     * 
     * @return the receipt capture.
     */
    public ReceiptCaptureDAO getReceiptCaptureDAO();

    /**
     * Gets the personal card transaction.
     * 
     * @return the personal card transaction.
     */
    public PersonalCardTransactionDAO getPersonalCardTransactionDAO();

    /**
     * Gets the corporate card transaction.
     * 
     * @return the corporate card transaction.
     */
    public CorporateCardTransactionDAO getCorporateCardTransactionDAO();

    /**
     * Will split this smart personal/corporate expense into a regular personal/corporate transaction and a cash transaction.
     */
    public void split();

    /**
     * Will perform an update by calling the <code>update</code> method on the appropriate related expense.
     * 
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean update(Context context, String userId);

}
