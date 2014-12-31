/**
 * 
 */
package com.concur.mobile.platform.expense.list.dao;

import java.util.Calendar;

import android.content.Context;
import android.net.Uri;

import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;

/**
 * An interface describing a <code>MobileEntry</code> Data Access Object (DAO).
 * 
 * @author andrewk
 */
public interface MobileEntryDAO {

    /**
     * Gets the currency code.
     * 
     * @return the currency code.
     */
    public String getCrnCode();

    /**
     * Sets the currency code.
     * 
     * @param crnCode
     *            the currency code.
     */
    public void setCrnCode(String crnCode);

    /**
     * Gets the expense entry type key.
     * 
     * @return the expense entry type key.
     */
    public String getExpKey();

    /**
     * Sets the expense type key.
     * 
     * @param expKey
     *            contains the expense type key.
     */
    public void setExpKey(String expKey);

    /**
     * Gets the expense type name.
     * 
     * @return the expense type name.
     */
    public String getExpName();

    /**
     * Sets the expense type name.
     * 
     * @param expName
     *            contains the expense type name.
     */
    public void setExpName(String expName);

    /**
     * Gets the location name.
     * 
     * @return the location name.
     */
    public String getLocationName();

    /**
     * Sets the location name.
     * 
     * @param locationName
     *            contains the location name.
     */
    public void setLocationName(String locationName);

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
     * Gets the entry type.
     * 
     * @return the entry type.
     */
    public ExpenseTypeEnum getEntryType();

    /**
     * Sets the entry type.
     * 
     * @param entryType
     *            contains the entry type.
     */
    public void setEntryType(ExpenseTypeEnum entryType);

    /**
     * Gets the mobile entry key.
     * 
     * @return the mobile entry key.
     */
    public String getMeKey();

    /**
     * Sets the mobile entry key.
     * 
     * @param meKey
     *            contains the mobile entry key.
     */
    public void setMeKey(String meKey);

    /**
     * Gets the personal card account key containing a transaction associated with this mobile entry.
     * 
     * @return the personal card account key.
     */
    public String getPcaKey();

    /**
     * Sets the personal card account key containing a transaction associated with this mobile entry.
     * 
     * @param pcaKey
     *            contains the personal card account key.
     */
    public void setPcaKey(String pcaKey);

    /**
     * Gets the personal card transaction key for a transaction associated with this mobile entry.
     * 
     * @return the personal card transaction key.
     */
    public String getPctKey();

    /**
     * Sets the personal card transaction key for a transaction associated with this mobile entry.
     * 
     * @param pctKey
     *            contains the personal card transaction key.
     */
    public void setPctKey(String pctKey);

    /**
     * Gets the corporate card transaction key for a corporate card transaction associated with this mobile entry.
     * 
     * @return the corporate card transaction key.
     */
    public String getCctKey();

    /**
     * Sets the corporate card transaction key for a corporate card transaction associated with this mobile entry.
     * 
     * @param cctKey
     *            contains the corporate card transaction key.
     */
    public void setCctKey(String cctKey);

    /**
     * Gets the receipt capture key for a receipt capture transaction associated with this mobile entry.
     * 
     * @return the receipt capture key.
     */
    public String getRcKey();

    /**
     * Sets the receipt capture key for a receipt capture transaction associated with this mobile entry.
     * 
     * @param rcKey
     *            contains the receipt capture key.
     */
    public void setRcKey(String rcKey);

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
     *            contains the transaction date.
     */
    public void setTransactionDate(Calendar transactionDate);

    /**
     * Gets whether this mobile entry has a receipt image.
     * 
     * @return whether this mobile entry has a receipt image.
     */
    public boolean hasReceiptImage();

    /**
     * Sets whether this mobile entry has a receipt image.
     * 
     * @param hasReceiptImage
     *            contains whether this mobile entry has a receipt image.
     */
    public void setHasReceiptImage(boolean hasReceiptImage);

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
     * Gets the content URI of receipt image stored in the <code>Expense.ReceiptColumns.TABLE_NAME</code> that is associated with
     * this mobile entry.
     * 
     * @return the content URI of receipt image stored in the <code>Expense.ReceiptColumns.TABLE_NAME</code> that is associated
     *         with this mobile entry.
     */
    public Uri getReceiptContentUri();

    /**
     * Gets the content URI of receipt image stored in the <code>Expense.ReceiptColumns.TABLE_NAME</code> that is associated with
     * this mobile entry.
     * 
     * @param receiptContentUri
     *            contains the content URI of receipt image stored in the <code>Expense.ReceiptColumns.TABLE_NAME</code> that is
     *            associated with this mobile entry.
     */
    public void setReceiptContentUri(Uri receiptContentUri);

    /**
     * Gets the base-64 encoded receipt image data.
     * 
     * @return the base-64 encoded receipt image data.
     */
    public String getReceiptImageData();

    /**
     * Sets the base-64 encoded receipt image data.
     * 
     * @param receiptImageData
     *            contains the base-64 encoded receipt image data.
     */
    public void setReceiptImageData(String receiptImageData);

    /**
     * Gets the receipt image data local file path.
     * 
     * @return contains the receipt image data local file path.
     */
    public String getReceiptImageDataLocalFilePath();

    /**
     * Sets the receipt image data local file path.
     * 
     * @param receiptImageDataLocalFilePath
     *            contains the receipt image data local file path.
     */
    public void setReceiptImageDataLocalFilePath(String receiptImageDataLocalFilePath);

    /**
     * Gets the comment.
     * 
     * @return the comment.
     */
    public String getComment();

    /**
     * Sets the comment.
     * 
     * @param comment
     *            contains the comment.
     */
    public void setComment(String comment);

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
