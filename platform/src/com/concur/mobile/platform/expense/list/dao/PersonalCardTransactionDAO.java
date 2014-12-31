/**
 * 
 */
package com.concur.mobile.platform.expense.list.dao;

import java.util.Calendar;

import android.content.Context;
import android.net.Uri;

import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;
import com.concur.mobile.platform.expense.list.MobileEntry;

/**
 * An interface describing a <code>PersonalCardTransaction</code> Data Access Object (DAO).
 * 
 * @author andrewk
 */
public interface PersonalCardTransactionDAO {

    /**
     * Gets the receipt capture type.
     * 
     * @return the receipt capture type.
     */
    public ExpenseTypeEnum getType();

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
     *            contains the currency code.
     */
    public void setCrnCode(String crnCode);

    /**
     * Gets the personal card transaction key.
     * 
     * @return the personal card transaction key.
     */
    public String getPctKey();

    /**
     * Set the personal card transaction key.
     * 
     * @param pctKey
     *            contains the personal card transaction key.
     */
    public void setPctKey(String pctKey);

    /**
     * Gets the date posted.
     * 
     * @return the date posted.
     */
    public Calendar getDatePosted();

    /**
     * Sets the date posted.
     * 
     * @param datePosted
     *            contains the date posted.
     */
    public void setDatePosted(Calendar datePosted);

    /**
     * Gets the description.
     * 
     * @return the description.
     */
    public String getDescription();

    /**
     * Sets the description.
     * 
     * @param description
     *            contains the description.
     */
    public void setDescription(String description);

    /**
     * Gets the amount.
     * 
     * @return the amount.
     */
    public Double getAmount();

    /**
     * Sets the amount.
     * 
     * @param amount
     *            contains the amount.
     */
    public void setAmount(Double amount);

    /**
     * Gets the status.
     * 
     * @return the status.
     */
    public String getStatus();

    /**
     * Sets the status.
     * 
     * @param status
     *            contains the status.
     */
    public void setStatus(String status);

    /**
     * Gets the category.
     * 
     * @return the category.
     */
    public String getCategory();

    /**
     * Sets the category.
     * 
     * @param category
     *            contains the category.
     */
    public void setCategory(String category);

    /**
     * Gets the expense key.
     * 
     * @return the expense key.
     */
    public String getExpKey();

    /**
     * Sets the expense key.
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
     * Gets the report key.
     * 
     * @return the report key.
     */
    public String getRptKey();

    /**
     * Sets the report key.
     * 
     * @param rptKey
     *            contains the report key.
     */
    public void setRptKey(String rptKey);

    /**
     * Gets the report name.
     * 
     * @return the report name.
     */
    public String getRptName();

    /**
     * Sets the report name.
     * 
     * @param rptName
     *            contains the report name.
     */
    public void setRptName(String rptName);

    /**
     * Gets the smart expense mobile entry key.
     * 
     * @return the smart expense mobile entry key.
     */
    public String getSmartExpenseMeKey();

    /**
     * Sets the smart expense mobile entry key.
     * 
     * @param smartExpenseMeKey
     *            contains the smart expense mobile entry key.
     */
    public void setSmartExpenseMeKey(String smartExpenseMeKey);

    /**
     * Gets the mobile entry associated with this personal card transaction.
     * 
     * @return the mobile entry associated with this personal card transaction.
     */
    public MobileEntryDAO getMobileEntryDAO();

    /**
     * Sets the mobile entry associated with this personal card transaction.
     * 
     * @param mobileEntry
     *            contains the mobile entry associated with this personal card transaction.
     */
    public void setMobileEntry(MobileEntry mobileEntry);

    /**
     * Gets the instance of <code>MobileEntryDAO</code> that represents an expense matched with this personal card transaction.
     * 
     * @return the instance of <code>MobileEntryDAO</code> that represents an expense matched with this personal card transaction.
     */
    public MobileEntryDAO getSmartMatchedMobileEntryDAO();

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
     * Gets whether this transaction has been split.
     * 
     * @return whether this transaction has been split.
     */
    public boolean isSplit();

    /**
     * Sets whether this transaction has been split.
     * 
     * @param split
     *            whether this transaction has been split.
     */
    public void setSplit(boolean split);

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
