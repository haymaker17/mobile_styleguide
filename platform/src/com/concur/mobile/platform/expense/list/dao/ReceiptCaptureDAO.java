/**
 * 
 */
package com.concur.mobile.platform.expense.list.dao;

import java.util.Calendar;

import android.content.Context;
import android.net.Uri;

import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;

/**
 * An interface describing a <code>ReceiptCapture</code> Data Access Object (DAO).
 * 
 * @author andrewk
 */
public interface ReceiptCaptureDAO {

    /**
     * Gets the receipt capture type.
     * 
     * @return the receipt capture type.
     */
    public ExpenseTypeEnum getType();

    /**
     * Gets the transaction currency code.
     * 
     * @return the currency code.
     */
    public String getCurrencyCode();

    /**
     * Sets the transaction currency code.
     * 
     * @param crnCode
     *            contains the transaction currency code.
     */
    public void setCurrencyCode(String crnCode);

    /**
     * Gets the expense entry type key.
     * 
     * @return the expense key.
     */
    public String getExpKey();

    /**
     * Sets the expense entry type key.
     * 
     * @param expKey
     *            contains the expense key.
     */
    public void setExpKey(String expKey);

    /**
     * Gets the expense name.
     * 
     * @return the expense name.
     */
    public String getExpName();

    /**
     * Sets the expense name.
     * 
     * @param expName
     *            contains the expense name.
     */
    public void setExpName(String expName);

    /**
     * Gets the vendor name.
     * 
     * @return the vendor name.
     */
    public String getVendorName();

    /**
     * Sets the vendor name.
     * 
     * @param vendorName
     *            contains the vendor name.
     */
    public void setVendorName(String vendorName);

    /**
     * Gets the receipt capture expense key.
     * 
     * @return the receipt capture expense key.
     */
    public String getRCKey();

    /**
     * Sets the receipt capture expense key.
     * 
     * @param rcKey
     *            contains the receipt capture expense key.
     */
    public void setRCKey(String rcKey);

    /**
     * Gets the smart expense id.
     * 
     * @return the smart expense id.
     */
    public String getSmartExpenseId();

    /**
     * Sets the smart expense id.
     * 
     * @param smartExpenseId
     *            contains the smart expense id.
     */
    public void setSmartExpenseId(String smartExpenseId);

    /**
     * Gets the transaction amount.
     * 
     * @return the transaction amount.
     */
    public Double getTransactionAmount();

    /**
     * Sets the transaction amount.
     * 
     * @param transactionAmount
     *            contains the transaction amount.
     */
    public void setTransactionAmount(Double transactionAmount);

    /**
     * Gets the transaction date.
     * 
     * @return the transaction date.
     */
    public Calendar getTransactionDate();

    /**
     * Sets the transaction date.
     * 
     * @param transactionDate
     *            the transaction date.
     */
    public void setTransactionDate(Calendar transactionDate);

    /**
     * Gets the receipt image id.
     * 
     * @return the receipt image id.
     */
    public String getReceiptImageId();

    /**
     * Sets the receipt image id.
     * 
     * @param receiptImageId
     *            contains the receipt image id.
     */
    public void setReceiptImageId(String receiptImageId);

    /**
     * Gets the tag.
     * 
     * @return the tag.
     */
    public String getTag();

    /**
     * Sets the tag.
     * 
     * @param tag
     *            contains the tag.
     */
    public void setTag(String tag);

    /**
     * Gets the content uri associated with this DAO object.
     * 
     * @param context
     *            contains an application context.
     * 
     * @return the content Uri associated with this DAO object.
     */
    public Uri getContentURI(Context context);

    /**
     * Sets the content uri associated with this DAO object.
     * 
     * @param contentUri
     *            contains the content Uri associated with this DAO object.
     */
    public void setContentURI(Uri contentUri);

    /**
     * Will perform an update based on current values.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean update(Context context, String userId);

}
