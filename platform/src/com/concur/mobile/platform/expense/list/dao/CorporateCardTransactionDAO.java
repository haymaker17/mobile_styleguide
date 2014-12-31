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
 * An interface describing a <code>CorporateCardTransaction</code> Data Access Object (DAO).
 * 
 * @author andrewk
 */
public interface CorporateCardTransactionDAO {

    /**
     * Gets the receipt capture type.
     * 
     * @return the receipt capture type.
     */
    public ExpenseTypeEnum getType();

    /**
     * Gets the card type code.
     * 
     * @return the card type code.
     */
    public String getCardTypeCode();

    /**
     * Sets the card type code.
     * 
     * @param cardTypeCode
     *            contains the card type code.
     */
    public void setCardTypeCode(String cardTypeCode);

    /**
     * Gets the card type name.
     * 
     * @return the card type name.
     */
    public String getCardTypeName();

    /**
     * Sets the card type name.
     * 
     * @param cardTypeName
     *            contains the card type name.
     */
    public void setCardTypeName(String cardTypeName);

    /**
     * Gets the corporate card transaction key.
     * 
     * @return the corporate card transaction key.
     */
    public String getCctKey();

    /**
     * Sets the corporate card transaction key.
     * 
     * @param cctKey
     *            contains the corporate card transaction key.
     */
    public void setCctKey(String cctKey);

    /**
     * Gets the corporate card transaction type.
     * 
     * @return the corporate card transaction type.
     */
    public String getCctType();

    /**
     * Sets the corporate card transaction type.
     * 
     * @param cctType
     *            contains the corporate card transaction type.
     */
    public void setCctType(String cctType);

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
     * Gets whether the corporate card transaction has rich data.
     * 
     * @return whether the corporate card transaction has rich data.
     */
    public Boolean getHasRichData();

    /**
     * Sets whether the corporate card transaction has rich data.
     * 
     * @param hasRichData
     *            contains whether the corporate card transaction has rich data.
     */
    public void setHasRichData(Boolean hasRichData);

    /**
     * Gets the "doing business as" value.
     * 
     * @return the "doing business as" value.
     */
    public String getDoingBusinessAs();

    /**
     * Sets the "doing business as" value.
     * 
     * @param doingBusinessAs
     *            contains the "doing business as" value.
     */
    public void setDoingBusinessAs(String doingBusinessAs);

    /**
     * Gets the expense key.
     * 
     * @return the expense key.
     */
    public String getExpenseKey();

    /**
     * Sets the expense key.
     * 
     * @param expenseKey
     *            contains the expense key.
     */
    public void setExpenseKey(String expenseKey);

    /**
     * Gets the expense name.
     * 
     * @return the expense name.
     */
    public String getExpenseName();

    /**
     * Sets the expense name.
     * 
     * @param expenseName
     *            contains the expense name.
     */
    public void setExpenseName(String expenseName);

    /**
     * Gets the merchant city.
     * 
     * @return the merchant city.
     */
    public String getMerchantCity();

    /**
     * Sets the merchant city.
     * 
     * @param merchantCity
     *            contains the merchant city.
     */
    public void setMerchantCity(String merchantCity);

    /**
     * Gets the merchant country code.
     * 
     * @return the merchant country code.
     */
    public String getMerchantCountryCode();

    /**
     * Sets the merchant country code.
     * 
     * @param merchantCountryCode
     *            contains the merchant country code.
     */
    public void setMerchantCountryCode(String merchantCountryCode);

    /**
     * Gets the merchant name.
     * 
     * @return the merchant name.
     */
    public String getMerchantName();

    /**
     * Sets the merchant name.
     * 
     * @param merchantName
     *            contains the merchant name.
     */
    public void setMerchantName(String merchantName);

    /**
     * Gets the merchant state.
     * 
     * @return the merchant state.
     */
    public String getMerchantState();

    /**
     * Sets the merchant state.
     * 
     * @param merchantState
     *            contains the merchant state.
     */
    public void setMerchantState(String merchantState);

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
     * Gets the transaction currency code.
     * 
     * @return the transaction currency code.
     */
    public String getTransactionCrnCode();

    /**
     * Sets the transaction currency code.
     * 
     * @param transactionCrnCode
     *            contains the transaction currency code.
     */
    public void setTransactionCrnCode(String transactionCrnCode);

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
     * Gets the mobile entry associated with this corporate card transaction.
     * 
     * @return the mobile entry associated with this corporate card transaction.
     */
    public MobileEntryDAO getMobileEntryDAO();

    /**
     * Sets the mobile entry associated with this corporate card transaction.
     * 
     * @param mobileEntry
     *            contains the mobile entry associated with this corporate card transaction.
     */
    public void setMobileEntry(MobileEntry mobileEntry);

    /**
     * Gets the instance of <code>MobileEntryDAO</code> that represents an expense matched with this corporate card transaction.
     * 
     * @return the instance of <code>MobileEntryDAO</code> that represents an expense matched with this corporate card
     *         transaction.
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
     *            contains whether this transaction has been split.
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
