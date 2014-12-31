/**
 * 
 */
package com.concur.mobile.platform.expense.list.dao;

import java.util.List;

import android.content.Context;
import android.net.Uri;

/**
 * An interface describing a <code>PersonalCard</code> Data Access Object (DAO).
 * 
 * @author andrewk
 */
public interface PersonalCardDAO {

    /**
     * Gets the card account key.
     * 
     * @return the card account key.
     */
    public String getPCAKey();

    /**
     * Sets the card account key.
     * 
     * @param pcaKey
     *            contains the card account key.
     */
    public void setPCAKey(String pcaKey);

    /**
     * Gets the card name.
     * 
     * @return the card name.
     */
    public String getCardName();

    /**
     * Set the card name.
     * 
     * @param cardName
     *            contains the card name.
     */
    public void setCardName(String cardName);

    /**
     * Gets the card account last four numbers.
     * 
     * @return the card account last four numbers.
     */
    public String getAcctNumLastFour();

    /**
     * Sets the card account last four numbers.
     * 
     * @param acctNumLastFour
     *            contains the card account last four numbers.
     */
    public void setAcctNumLastFour(String acctNumLastFour);

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
     * Gets the list of personal card transactions.
     * 
     * @return the list of personal card transactions.
     */
    public List<PersonalCardTransactionDAO> getPersonalCardTransactionDAOS();

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
