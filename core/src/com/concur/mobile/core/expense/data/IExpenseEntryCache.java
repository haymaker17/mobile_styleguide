/**
 * 
 */
package com.concur.mobile.core.expense.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.expense.charge.data.PersonalCard;
import com.concur.mobile.core.service.ConcurService;

/**
 * An interface for providing a cache of expense entry related information.
 * 
 * @author AndrewK
 */
public interface IExpenseEntryCache {

    /**
     * Gets the list of available expense types.
     * 
     * @return the list of available expense types.
     */
    public ArrayList<ExpenseType> getExpenseTypes();

    /**
     * Will put a list of expense types based on a key.
     * 
     * @param key
     *            the expense types list key.
     * @param types
     *            the list of <code>ExpenseType</code> objects.
     * @param concurService
     */
    public void putExpenseTypes(String key, List<ExpenseType> types, ConcurService concurService);

    /**
     * Gets a list of expense types based on a key.
     * 
     * @param key
     *            the expense types list key.
     * @return a list of <code>ExpenseTypes</code> objects; otherwise, <code>null</code> is returned.
     */
    public List<ExpenseType> getExpenseTypes(String key);

    /**
     * Gets the list of expense entries.
     * 
     * @return the list of expense entries.
     */
    public ArrayList<Expense> getExpenseEntries();

    /**
     * Will refresh the set of expense entries maintained within the expense entry cache. This method will only refresh from local
     * persistence.
     */
    public void refreshExpenseEntries();

    /**
     * Will split a smart expense within the cache.
     * 
     * @param smartExpense
     *            the smart expense to be split.
     */
    public void splitSmartExpense(Expense expense);

    /**
     * Gets the list of personal cards.
     * 
     * @return the list of personal cards.
     */
    public ArrayList<PersonalCard> getPersonalCards();

    /**
     * Sets the expense entry list information.
     * 
     * @param expListInfo
     *            the expense entry list information.
     */
    public void setExpenseEntries(ExpenseListInfo expListInfo);

    /**
     * Gets the time at which the last expense entry list was received.
     * 
     * @return the time at which the last expense entry list was received.
     */
    public Calendar getLastExpenseEntriesUpdateTime();

    /**
     * Gets whether or not this cache has an expense entry list.
     * 
     * @return whether an expense entry list has been set on the cache.
     */
    public boolean hasExpenseList();

    /**
     * Determines whether the last expense list update is older than <code>expiration</code> milliseconds.
     * 
     * <br>
     * <b>NOTE:</b><br>
     * Clients should call <code>hasExpenseList</code> to determine whether the cache has an expense list backed by persistence.
     * 
     * @param expiration
     *            the expiration time in milliseconds.
     * 
     * @return If the cache has an expense list, then will return <code>true</code> if the last update time is older than
     *         <code>expiration</code> milliseconds; otherwise, <code>false</code> will be returned. If the cache has no expense
     *         list, then <code>false</code> will be returned.
     */
    public boolean isLastExpenseListUpdateExpired(long expiration);

    /**
     * Will locate a cash expense entry matching on mobile entry key <code>meKey</code>.
     * 
     * @param meKey
     *            the mobile entry key for the cash transaction.
     * @return a cash expense entry matching on mobile entry key <code>meKey</code>.
     */
    public Expense findCashExpenseEntry(String meKey);

    /**
     * Will locate a personal card expense entry matching on card account key <code>pcaKey</code> and <code>pctKey</code>.
     * 
     * @param pcaKey
     *            the personal card account key.
     * @param pctKey
     *            the personal card transaction key.
     * @return a personal card expense entry matching on card account key <code>pcaKey</code> and <code>pctKey</code>.
     */
    public Expense findPersonalCardExpenseEntry(String pcaKey, String pctKey);

    /**
     * Will locate a corporate card expense entry matching on corporate card transaction key.
     * 
     * @param cctKey
     *            the corporate card transaction key.
     * 
     * @return a corporate card expense entry matching on corporate card transaction key <code>cctKey</code>; otherwise
     *         <code>null</code> is returned.
     */
    public Expense findCorporateCardExpenseEntry(String cctKey);

    /**
     * Will locate a smart expense entry matching on corporate card transaction key.
     * 
     * @param cctKey
     *            the corporate card transaction key.
     * @return a smart expense entry matching on corporate card transaction key <code>cctKey</code>; otherwise <code>null</code>
     *         is returned.
     */
    public Expense findSmartCorpExpenseEntry(String cctKey);

    /**
     * Will locate a smart expense entry matching on personal card transaction key.
     * 
     * @param pctKey
     *            the personal card transaction key.
     * @return a smart expense entry matching on personal card transaction key <code>cctKey</code>; otherwise <code>null</code> is
     *         returned.
     */
    public Expense findSmartPersExpenseEntry(String pctKey);

    /**
     * Will locate a receipt capture expense entry matching on receipt capture key.
     * 
     * @param rcKey
     *            the receipt capture key.
     * @return a receipt capture expense entry matching on receipt capture key <code>rcKey</code>; otherwise <code>null</code> is
     *         returned.
     */
    public Expense findReceiptCaptureExpenseEntry(String rcKey);

    /**
     * Will locate a receipt capture expense entry matching on receipt capture key.
     * 
     * @param eReceiptId
     *            the receipt capture key.
     * @return an e-receipt expense entry matching on e-receipt id <code>eReceiptId</code>; otherwise <code>null</code> is
     *         returned.
     */
    public Expense findEReceiptExpenseEntry(String eReceiptId);

    /**
     * Will add an expense entry.
     * 
     * @param expense
     *            the expense entry.
     */
    public void addExpenseEntry(Expense expense);

    /**
     * Will remove an expense entry from the cache.
     * 
     * @param expense
     *            the expense to remove.
     */
    public void removeExpenseEntry(Expense expense);

    /**
     * Gets the list of currency types.
     * 
     * @return the list of currency types.
     */
    public ArrayList<ListItem> getCurrencyTypes();

    /**
     * Gets the list of mobile expense entries not associated with any report.
     * 
     * @return the list of mobile expense entries.
     */
    public ArrayList<MobileEntry> getMobileEntries();

    /**
     * Sets the list of mobile expense entries not associated with any report.
     * 
     * @param mobileEntries
     *            the list of mobile entries.
     */
    public void setMobileEntries(ArrayList<MobileEntry> mobileEntries);

    /**
     * Will attempt to find an instance of <code>MobileEntry</code> based on the mobile entry key.
     * 
     * <b>NOTE:</b> Implementors of this method must ensure that an attempt is made to load the set of mobile entries from the DB
     * prior to performing the search.
     * 
     * @param key
     *            the mobile entry key.
     * 
     * @return an instance of <code>MobileEntry</code> based on the mobile entry key; otherwise, <code>null</code> is returned.
     */
    public MobileEntry findMobileEntryByMeKey(String key);

    /**
     * Will attempt to find an instance of <code>MobileEntry</code> based on a local key.
     * 
     * <b>NOTE:</b> Implementors of this method must ensure that an attempt is made to load the set of mobile entries from the DB
     * prior to performing the search.
     * 
     * 
     * @param key
     *            the local key.
     * 
     * @return an instance of <code>MobileEntry</code> based on a local key; otherwise, <code>null</code> is returned.
     */
    public MobileEntry findMobileEntryByLocalKey(String key);

    /**
     * Adds a mobile entry to the in-memory cache.
     * 
     * @param mobileEntry
     *            the mobile entry to be added.
     */
    public void addMobileEntry(MobileEntry mobileEntry);

    /**
     * Removes a mobile entry from the in-memory cache.
     * 
     * @param mobileEntry
     *            the mobile entry to be removed.
     */
    public void removeMobileEntry(MobileEntry mobileEntry);

    /**
     * Sets the expense list refetched flag from the server.
     */
    public void setShouldFetchExpenseList();

    /**
     * Contains whether an activity has altered a report in such a fashion that the expense list should be retrieved again from
     * the server.
     * 
     * @return whether the expense list should be refetched.
     */
    public boolean shouldRefetchExpenseList();

    /**
     * Clears the flag indicating that the expense list should be refetched.
     */
    public void clearShouldRefetchExpenseList();

    /**
     * Filter out selected expense type using expense key.
     * 
     * @param expTypes
     *            : List of expense type
     * @param selectedExpKey
     *            : selected expense report key.
     * @return : respective expense type.
     */
    public ExpenseType getFilteredExpenseType(List<ExpenseType> expTypes, String selectedExpKey);

    /***
     * Sort expense list in to get most recently used expense type.
     * 
     * @param expTypes
     *            : list of expense type to sort.
     * @return sorted list of expense type.
     * */
    public List<ExpenseType> sortExpenseList(List<ExpenseType> expTypes);

    /**
     * Gets a list of expense types based on a key from Database and update it.
     * 
     * @param key
     *            the expense types policy key.
     * @param concurservice
     *            concur service.
     * @return a list of <code>ExpenseTypes</code> objects; otherwise, <code>null</code> is returned.
     */
    public List<ExpenseType> updateExpenseTypesCacheForDB(ConcurService concurService, String polKey);

    /**
     * Gets a list of expense types based on a key from Database Map.
     * 
     * @param key
     *            the expense types policy key.
     * 
     * @return a list of <code>ExpenseTypes</code> objects; otherwise, <code>null</code> is returned.
     */

    public List<ExpenseType> getExpenseTypeFromDB(String key);

    /***
     * Put expense type in database map.
     * */
    public void putExpenseTypeInDatabaseMap(ConcurService concurService, String polKey);

    /**
     * Generate cache for MRU list items
     * 
     * @param concurService
     *            : reference of concirService
     * @param userId
     *            : user id
     * @param fieldId
     *            : field id
     */
    public void putListItemInCacheForMRU(ConcurService concurService, String userId, String fieldId);

    /**
     * sort list items
     * 
     * @param litems
     *            : list of listItems
     * @return : sorted MRU list
     */
    public List<ListItem> sortListItemMruList(List<ListItem> litems);

    /**
     * get MRU list from DB
     * 
     * @param key
     *            : key for mru cache map
     * @param fieldId
     *            : field id
     * @return : list items from db
     */
    public List<ListItem> getListItemFromDB(String key, String fieldId);

    /**
     * Update MRU list cache after insertion.
     * 
     * @param concurService
     *            : reference of concur service
     * @param userId
     *            : user id
     * @param fieldId
     *            : field id
     * @return : updated list.
     */
    public List<ListItem> updateListItemCache(ConcurService concurService, String userId, String fieldId);

}
