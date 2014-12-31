/**
 * 
 */
package com.concur.mobile.core.expense.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.data.SystemConfig;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.expense.charge.data.CorporateCardTransaction;
import com.concur.mobile.core.expense.charge.data.EReceipt;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.Expense.ExpenseEntryType;
import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.expense.charge.data.PersonalCard;
import com.concur.mobile.core.expense.charge.data.PersonalCardTransaction;
import com.concur.mobile.core.expense.charge.data.ReceiptCapture;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.ComparatorUtil;
import com.concur.mobile.core.util.ComparatorUtil.Operation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.CurrencyListItemComparatorUtil;
import com.concur.mobile.platform.ui.common.util.PreferenceUtil;

/**
 * An implementation of <code>IExpenseCache</code> for providng expense cache services.
 * 
 * @author AndrewK
 */
public class ExpenseEntryCache implements IExpenseEntryCache {

    private static final String CLS_TAG = ExpenseEntryCache.class.getSimpleName();

    /**
     * Contains how many exp type MRU we need to show to user.
     * */
    private static final int MIN_MRU_EXP_TYPE = 10;

    /**
     * Contains how many list Item MRU we need to show to user.
     * */
    private static final int MIN_MRU_LIST_ITEM = 10;

    /**
     * Contains a reference to the expense list information.
     */
    private ExpenseListInfo expListInfo;

    /**
     * Contains a reference to the Concur mobile application object.
     */
    private ConcurCore concurMobile;

    /**
     * Contains a reference to a list of mobile entries not associated with an expense report.
     */
    private ArrayList<MobileEntry> mobileEntries;

    private Map<String, List<ExpenseType>> expenseTypeMap;

    private Map<String, List<ExpenseType>> expenseTypeMapDB;

    private Map<String, List<ListItem>> listItemMapDB;

    /**
     * Constructs an instance of <code>ExpenseEntryCache</code>.
     * 
     * @param concurMobile
     *            the application.
     */
    public ExpenseEntryCache(ConcurCore concurMobile) {
        this.concurMobile = concurMobile;
        this.expenseTypeMap = new HashMap<String, List<ExpenseType>>();
        this.expenseTypeMapDB = new HashMap<String, List<ExpenseType>>();
        this.listItemMapDB = new HashMap<String, List<ListItem>>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#getExpenseTypes()
     */
    public ArrayList<ExpenseType> getExpenseTypes() {
        ArrayList<ExpenseType> expenseTypes = null;
        SystemConfig sysConfig = concurMobile.getSystemConfig();
        if (sysConfig != null) {
            expenseTypes = sysConfig.getExpenseTypes();
        }
        return expenseTypes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#getExpenseTypes(java.lang.String)
     */
    public List<ExpenseType> getExpenseTypes(String key) {
        List<ExpenseType> retVal = null;
        if (expenseTypeMap != null) {
            retVal = expenseTypeMap.get(key);
        }
        return retVal;
    }

    public List<ExpenseType> getExpenseTypeFromDB(String key) {
        List<ExpenseType> expList = null;
        if (expenseTypeMapDB != null) {
            List<ExpenseType> mruList = expenseTypeMapDB.get(key);
            if (mruList != null) {
                expList = new ArrayList<ExpenseType>();
                expList.addAll(0, mruList);
            }
        }
        return expList;
    }

    public List<ExpenseType> updateExpenseTypesCacheForDB(ConcurService concurService, String polKey) {
        List<ExpenseType> retVal = null;
        if (expenseTypeMapDB != null) {
            if (expenseTypeMapDB.containsKey(polKey)) {
                expenseTypeMapDB.remove(polKey);
            }
            putExpenseTypeInDatabaseMap(concurService, polKey);
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#putExpenseTypes(java.lang.String, java.util.List)
     */
    public void putExpenseTypes(String polKey, List<ExpenseType> types, ConcurService concurService) {
        if (expenseTypeMap != null) {
            expenseTypeMap.put(polKey, types);
            putExpenseTypeInDatabaseMap(concurService, polKey);
        }
    }

    /**
     * Create Database Map and put data in it.
     * */
    public void putExpenseTypeInDatabaseMap(ConcurService concurService, String polKey) {
        List<ExpenseType> listFromDB = loadExpenseTypeListFromDB(concurService, polKey);
        if (listFromDB != null) {
            final int dbSize = listFromDB.size();
            if (dbSize > 0) {
                ArrayList<ExpenseType> mruList = new ArrayList<ExpenseType>();
                listFromDB = sortExpenseList(listFromDB);
                int countOfLastElement = listFromDB.get(dbSize - 1).getuseCount();
                if (countOfLastElement != 0) {
                    for (int i = dbSize - 1; ((i >= dbSize - MIN_MRU_EXP_TYPE) && (i >= 0)); i--) {
                        if (listFromDB.get(i) != null && listFromDB.get(i).getuseCount() > 0) {
                            mruList.add(listFromDB.get(i));
                        }
                    }
                    if (mruList.size() > 0 && expenseTypeMapDB != null) {
                        if (expenseTypeMapDB.containsKey(polKey)) {
                            expenseTypeMapDB.remove(polKey);
                        }
                        expenseTypeMapDB.put(polKey, mruList);
                    }
                } else {
                    Log.d(Const.LOG_TAG, CLS_TAG
                            + ".ExpenseTypeSpinnerAdapter.setExpenseTypes: no expense type has been used recently.");
                }
            }
        }
    }

    /**
     * load expense type list from database and put it into map.
     * 
     * @param concurService
     *            : concur service
     * @param polKey
     *            : policy key
     * @return : listofExpenseTypeFromDB list from databases.
     */
    private List<ExpenseType> loadExpenseTypeListFromDB(ConcurService concurService, String polKey) {
        MobileDatabase mdb = concurService.getMobileDatabase();
        List<ExpenseType> listofExpenseTypeFromDB = null;
        if (mdb != null) {
            if (concurService.prefs != null) {
                String userId = concurService.prefs.getString(Const.PREF_USER_ID, null);
                if (userId != null) {
                    listofExpenseTypeFromDB = mdb.loadExpenseTypeFromDB(userId, polKey);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".ExpenseEntryCache.loadExpenseListFromDB: userid is null");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".ExpenseEntryCache.loadExpenseListFromDB: concurService.prefs is null");
            }
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".ExpenseEntryCache.loadExpenseListFromDB: database is null.");
        }
        return listofExpenseTypeFromDB;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#getExpenseEntries()
     */
    public ArrayList<Expense> getExpenseEntries() {
        ArrayList<Expense> expenses = null;
        // If the local list is 'null', then retrieve it from the service.
        if (expListInfo == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            ConcurService concurService = concurMobile.getService();
            if (concurService != null) {
                expListInfo = concurService.getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG + ".getExpenseEntries: service is unavailable!");
            }
        }
        if (expListInfo != null) {
            expenses = expListInfo.getExpenseList();
        }
        return expenses;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#refreshExpenseEntries()
     */
    public void refreshExpenseEntries() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile.getApplicationContext());
        expListInfo = concurMobile.getService().getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#splitSmartExpense(com.concur.mobile.data.expense.Expense)
     */
    public void splitSmartExpense(Expense expense) {
        if (expense.getExpenseEntryType() == ExpenseEntryType.SMART_CORPORATE
                || expense.getExpenseEntryType() == ExpenseEntryType.SMART_PERSONAL) {
            // Load the list, but should be in-memory already.
            if (expListInfo == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                        .getApplicationContext());
                expListInfo = concurMobile.getService().getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
            }
            if (expListInfo != null) {
                ArrayList<Expense> expenses = expListInfo.getExpenseList();
                if (expenses != null) {
                    if (!expenses.remove(expense)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".splitSmartExpense: expense to be split not in expenses list!");
                    }
                    switch (expense.getExpenseEntryType()) {
                    case SMART_CORPORATE: {
                        Expense cashExpense = new Expense(expense.getCashTransaction());
                        Expense corpCardExpense = new Expense(expense.getCorporateCardTransaction());
                        expenses.add(cashExpense);
                        expenses.add(corpCardExpense);
                        break;
                    }
                    case SMART_PERSONAL: {
                        Expense cashExpense = new Expense(expense.getCashTransaction());
                        Expense persCardExpense = new Expense(expense.getPersonalCard(),
                                expense.getPersonalCardTransaction());
                        expenses.add(cashExpense);
                        expenses.add(persCardExpense);
                        break;
                    }
                    default:
                        break;
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".splitSmartExpense: empty parsed expense list!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".splitSmartExpense: null list of expenses in cache!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".splitSmartExpense: invalid expense type passed in of type: '"
                    + expense.getExpenseEntryType().name() + "'.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#findCashExpenseEntry(java.lang.String)
     */
    public Expense findCashExpenseEntry(String meKey) {
        Expense exp = null;
        if (meKey != null) {
            // If the local list is 'null', then retrieve it from the service.
            if (expListInfo == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                        .getApplicationContext());
                expListInfo = concurMobile.getService().getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
            }
            if (expListInfo != null) {
                ArrayList<Expense> expenses = expListInfo.getExpenseList();
                if (expenses != null) {
                    Iterator<Expense> expIter = expenses.iterator();
                    while (expIter.hasNext()) {
                        Expense expense = expIter.next();
                        if (expense.getExpenseEntryType().equals(Expense.ExpenseEntryType.CASH)) {
                            MobileEntry mobileEntry = expense.getCashTransaction();
                            if (mobileEntry != null) {
                                if (mobileEntry.getMeKey() != null) {
                                    if (meKey.equalsIgnoreCase(mobileEntry.getMeKey())) {
                                        exp = expense;
                                        break;
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".findCashExpenseEntry: mobile entry has null mobile entry key!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".findCashExpenseEntry: mobile entry associated with expense is null!");
                            }
                        }
                    }
                }
            }
        }
        return exp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#findPersonalCardExpenseEntry(java.lang.String, java.lang.String)
     */
    public Expense findPersonalCardExpenseEntry(String pcaKey, String pctKey) {
        Expense exp = null;
        if (pcaKey != null && pctKey != null) {
            // If the local list is 'null', then retrieve it from the service.
            if (expListInfo == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                        .getApplicationContext());
                expListInfo = concurMobile.getService().getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
            }
            if (expListInfo != null) {
                ArrayList<Expense> expenses = expListInfo.getExpenseList();
                if (expenses != null) {
                    Iterator<Expense> expIter = expenses.iterator();
                    while (expIter.hasNext()) {
                        Expense expense = expIter.next();
                        if (expense.getExpenseEntryType().equals(Expense.ExpenseEntryType.PERSONAL_CARD)) {
                            PersonalCard persCard = expense.getPersonalCard();
                            if (persCard != null) {
                                if (persCard.pcaKey != null) {
                                    if (persCard.pcaKey.equalsIgnoreCase(pcaKey)) {
                                        PersonalCardTransaction pctTrans = expense.getPersonalCardTransaction();
                                        if (pctTrans != null) {
                                            if (pctTrans.pctKey != null) {
                                                if (pctTrans.pctKey.equalsIgnoreCase(pctKey)) {
                                                    exp = expense;
                                                    break;
                                                }
                                            } else {
                                                Log.e(Const.LOG_TAG,
                                                        CLS_TAG
                                                                + ".findPersonalCardExpenseEntry: personal card expense has null card transaction key!");
                                            }
                                        } else {
                                            Log.e(Const.LOG_TAG,
                                                    CLS_TAG
                                                            + ".findPersonalCardExpenseEntry: personal card expense has null card transaction!");
                                        }
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".findPersonalCardExpenseEntry: personal card expense has null card account key!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".findPersonalCardExpenseEntry: personal card expense entry has null card!");
                            }
                        }
                    }
                }
            }
        }
        return exp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#findCorporateCardExpenseEntry(java.lang.String)
     */
    public Expense findCorporateCardExpenseEntry(String cctKey) {
        Expense exp = null;
        if (cctKey != null) {
            // If the local list is 'null', then retrieve it from the service.
            if (expListInfo == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                        .getApplicationContext());
                expListInfo = concurMobile.getService().getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
            }
            if (expListInfo != null) {
                ArrayList<Expense> expenses = expListInfo.getExpenseList();
                if (expenses != null) {
                    Iterator<Expense> expIter = expenses.iterator();
                    while (expIter.hasNext()) {
                        Expense expense = expIter.next();
                        if (expense.getExpenseEntryType().equals(Expense.ExpenseEntryType.CORPORATE_CARD)) {
                            CorporateCardTransaction corpCardTrans = expense.getCorporateCardTransaction();
                            if (corpCardTrans != null) {
                                if (corpCardTrans.getCctKey() != null) {
                                    if (corpCardTrans.getCctKey().equalsIgnoreCase(cctKey)) {
                                        exp = expense;
                                        break;
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".findCorporateCardExpenseEntry: corporate card expense has null transaction key!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".findCorporateCardExpenseEntry: corporate card expense entry has null transaction!");
                            }
                        }
                    }
                }
            }
        }
        return exp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#findSmartCorpExpenseEntry(java.lang.String)
     */
    public Expense findSmartCorpExpenseEntry(String cctKey) {
        Expense exp = null;
        if (cctKey != null) {
            // If the local list is 'null', then retrieve it from the service.
            if (expListInfo == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                        .getApplicationContext());
                expListInfo = concurMobile.getService().getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
            }
            if (expListInfo != null) {
                ArrayList<Expense> expenses = expListInfo.getExpenseList();
                if (expenses != null) {
                    Iterator<Expense> expIter = expenses.iterator();
                    while (expIter.hasNext()) {
                        Expense expense = expIter.next();
                        if (expense.getExpenseEntryType().equals(Expense.ExpenseEntryType.SMART_CORPORATE)) {
                            CorporateCardTransaction corpCardTrans = expense.getCorporateCardTransaction();
                            if (corpCardTrans != null) {
                                if (corpCardTrans.getCctKey() != null) {
                                    if (corpCardTrans.getCctKey().equalsIgnoreCase(cctKey)) {
                                        exp = expense;
                                        break;
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".findSmartCorpExpenseEntry: corporate card expense has null transaction key!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".findSmartCorpExpenseEntry: corporate card expense entry has null transaction!");
                            }
                        }
                    }
                }
            }
        }
        return exp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#findReceiptCaptureExpenseEntry(java.lang.String)
     */
    public Expense findReceiptCaptureExpenseEntry(String rcKey) {
        Expense exp = null;
        if (rcKey != null) {
            // If the local list is 'null', then retrieve it from the service.
            if (expListInfo == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                        .getApplicationContext());
                expListInfo = concurMobile.getService().getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
            }
            if (expListInfo != null) {
                ArrayList<Expense> expenses = expListInfo.getExpenseList();
                if (expenses != null) {
                    Iterator<Expense> expIter = expenses.iterator();
                    while (expIter.hasNext()) {
                        Expense expense = expIter.next();
                        if (expense.getExpenseEntryType().equals(Expense.ExpenseEntryType.RECEIPT_CAPTURE)) {
                            ReceiptCapture receiptCaptures = expense.getReceiptCapture();
                            if (receiptCaptures != null) {
                                if (receiptCaptures.rcKey != null) {
                                    if (receiptCaptures.rcKey.equalsIgnoreCase(rcKey)) {
                                        exp = expense;
                                        break;
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".findReceiptCaptureExpenseEntry: receipt capture expense has null transaction key!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".findReceiptCaptureExpenseEntry: receipt capture expense entry has null transaction!");
                            }
                        }
                    }
                }
            }
        }
        return exp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#findEReceiptExpenseEntry(java.lang.String)
     */
    public Expense findEReceiptExpenseEntry(String eReceiptId) {
        Expense exp = null;
        // TODO - part of the DAO to Core objects conversion

        // This is for the new EReceipts (which is created from the SmartExpenseDAO).
        if (eReceiptId != null) {
            // If the local list is 'null', then retrieve it from the service.
            if (expListInfo == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                        .getApplicationContext());
                expListInfo = concurMobile.getService().getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
            }
            if (expListInfo != null) {
                ArrayList<Expense> expenses = expListInfo.getExpenseList();
                if (expenses != null) {
                    Iterator<Expense> expIter = expenses.iterator();
                    while (expIter.hasNext()) {
                        Expense expense = expIter.next();
                        if (expense.getExpenseEntryType().equals(ExpenseEntryType.E_RECEIPT)) {
                            EReceipt eReceipt = expense.getEReceipt();
                            if (eReceipt != null) {
                                if (eReceipt.getEReceiptId() != null) {
                                    if (eReceipt.getEReceiptId().equals(eReceiptId)) {
                                        exp = expense;
                                        break;
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".findReceiptCaptureExpenseEntry: eReceipt expense has null transaction key!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".findReceiptCaptureExpenseEntry: eReceipt expense entry has null transaction!");
                            }
                        }
                    }
                }

            }
        }

        return exp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#findSmartPersExpenseEntry(java.lang.String)
     */
    public Expense findSmartPersExpenseEntry(String pctKey) {
        Expense exp = null;
        if (pctKey != null) {
            // If the local list is 'null', then retrieve it from the service.
            if (expListInfo == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                        .getApplicationContext());
                expListInfo = concurMobile.getService().getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
            }
            if (expListInfo != null) {
                ArrayList<Expense> expenses = expListInfo.getExpenseList();
                if (expenses != null) {
                    Iterator<Expense> expIter = expenses.iterator();
                    while (expIter.hasNext()) {
                        Expense expense = expIter.next();
                        if (expense.getExpenseEntryType().equals(Expense.ExpenseEntryType.SMART_PERSONAL)) {
                            PersonalCardTransaction persCardTrans = expense.getPersonalCardTransaction();
                            if (persCardTrans != null) {
                                if (persCardTrans.pctKey != null) {
                                    if (persCardTrans.pctKey.equalsIgnoreCase(pctKey)) {
                                        exp = expense;
                                        break;
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".findSmartPersExpenseEntry: personal card expense has null transaction key!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".findSmartPersExpenseEntry: personal card expense entry has null transaction!");
                            }
                        }
                    }
                }
            }
        }
        return exp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#addExpenseEntry(com.concur.mobile.data.expense.Expense)
     */
    public void addExpenseEntry(Expense expense) {
        // If the local list is 'null', then retrieve it from the service.
        if (expListInfo == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            expListInfo = concurMobile.getService().getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
        }
        if (expListInfo != null) {
            expListInfo.getExpenseList().add(expense);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#removeExpenseEntry(com.concur.mobile.data.expense.Expense)
     */
    public void removeExpenseEntry(Expense expense) {
        if (expListInfo != null) {
            if (expListInfo.getExpenseList().contains(expense)) {
                expListInfo.getExpenseList().remove(expense);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#setExpenseEntries(java.util.ArrayList)
     */
    public void setExpenseEntries(ExpenseListInfo expListInfo) {
        this.expListInfo = expListInfo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#getLastExpenseEntriesUpdateTime()
     */
    public Calendar getLastExpenseEntriesUpdateTime() {
        Calendar updateTime = null;
        if (expListInfo != null) {
            updateTime = expListInfo.getUpdateTime();
        }
        return updateTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#hasExpenseList()
     */
    public boolean hasExpenseList() {
        // If the local list is 'null', then retrieve it from the service.
        if (expListInfo == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            ConcurService concurService = concurMobile.getService();
            if (concurService != null) {
                expListInfo = concurService.getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG + ".hasExpenseList: service is unavailable!");
            }
        }
        return (expListInfo != null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#isLastExpenseListUpdateExpired(long)
     */
    public boolean isLastExpenseListUpdateExpired(long expiration) {
        boolean retVal = false;
        if (hasExpenseList()) {
            Calendar updateTime = expListInfo.getUpdateTime();
            if (updateTime != null) {
                long curTimeMillis = System.currentTimeMillis();
                try {
                    long updateTimeMillis = updateTime.getTimeInMillis();
                    retVal = ((curTimeMillis - updateTimeMillis) > expiration);
                } catch (IllegalArgumentException ilaArgExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".isLastExpenseListUpdateExpired: unable to get millisecond time from 'updateTime'!",
                            ilaArgExc);
                    // Err to the side of caution.
                    retVal = true;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".isLastExpenseListUpdateExpired: expense report info has null 'updateTime'!");
                // Err to the side of caution.
                retVal = true;
            }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#getCurrencyTypes()
     */
    public ArrayList<ListItem> getCurrencyTypes() {
        ArrayList<ListItem> currencyTypes = null;
        UserConfig userConfig = concurMobile.getUserConfig();
        if (userConfig != null) {
            currencyTypes = userConfig.currencies;
        }
        return currencyTypes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#getMobileEntries()
     */
    public ArrayList<MobileEntry> getMobileEntries() {

        if (mobileEntries == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            mobileEntries = concurMobile.getService().getMobileEntries(prefs.getString(Const.PREF_USER_ID, null));
        }
        return mobileEntries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#setMobileEntries(java.util.ArrayList)
     */
    public void setMobileEntries(ArrayList<MobileEntry> mobileEntries) {
        this.mobileEntries = mobileEntries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#findMobileEntryByLocalKey(java.lang.String)
     */
    public MobileEntry findMobileEntryByLocalKey(String key) {
        MobileEntry result = null;

        if (mobileEntries == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            mobileEntries = concurMobile.getService().getMobileEntries(prefs.getString(Const.PREF_USER_ID, null));
        }
        if (mobileEntries != null) {
            Iterator<MobileEntry> iterator = mobileEntries.iterator();
            while (iterator.hasNext()) {
                MobileEntry mobileEntry = iterator.next();
                if (mobileEntry.getLocalKey() != null && mobileEntry.getLocalKey().equalsIgnoreCase(key)) {
                    result = mobileEntry;
                    break;
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#findMobileEntryByMeKey(java.lang.String)
     */
    public MobileEntry findMobileEntryByMeKey(String key) {
        MobileEntry result = null;

        if (mobileEntries == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            mobileEntries = concurMobile.getService().getMobileEntries(prefs.getString(Const.PREF_USER_ID, null));
        }
        if (mobileEntries != null) {
            Iterator<MobileEntry> iterator = mobileEntries.iterator();
            while (iterator.hasNext()) {
                MobileEntry mobileEntry = iterator.next();
                if (mobileEntry.getMeKey() != null && mobileEntry.getMeKey().equalsIgnoreCase(key)) {
                    result = mobileEntry;
                    break;
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#addMobileEntry(com.concur.mobile.data.expense.MobileEntry)
     */
    public void addMobileEntry(MobileEntry mobileEntry) {

        // First attempt to load from the db if empty.
        if (mobileEntries == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            mobileEntries = concurMobile.getService().getMobileEntries(prefs.getString(Const.PREF_USER_ID, null));
        }
        if (mobileEntries == null) {
            mobileEntries = new ArrayList<MobileEntry>();
        }
        mobileEntries.add(mobileEntry);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#removeMobileEntry(com.concur.mobile.data.expense.MobileEntry)
     */
    public void removeMobileEntry(MobileEntry mobileEntry) {

        // First attempt to load from the db if empty.
        if (mobileEntries == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            mobileEntries = concurMobile.getService().getMobileEntries(prefs.getString(Const.PREF_USER_ID, null));
        }
        if (mobileEntries == null) {
            mobileEntries = new ArrayList<MobileEntry>();
        }
        mobileEntries.remove(mobileEntry);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#getPersonalCards()
     */
    public ArrayList<PersonalCard> getPersonalCards() {
        ArrayList<PersonalCard> cards = null;
        // If the local list is 'null', then retrieve it from the service.
        if (expListInfo == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            expListInfo = concurMobile.getService().getAllExpenses(prefs.getString(Const.PREF_USER_ID, null));
        }
        if (expListInfo != null) {
            cards = expListInfo.getPersonalCards();
        }
        return cards;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#clearShouldRefetchExpenseList()
     */
    public void clearShouldRefetchExpenseList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile.getApplicationContext());
        String name = prefs.getString(Const.PREF_USER_ID, "") + Const.PREF_USER_SHOULD_REFRESH_EXPENSE_LIST;
        PreferenceUtil.savePreference(concurMobile.getApplicationContext(), name, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#setShouldFetchExpenseList()
     */
    public void setShouldFetchExpenseList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile.getApplicationContext());
        String name = prefs.getString(Const.PREF_USER_ID, "") + Const.PREF_USER_SHOULD_REFRESH_EXPENSE_LIST;
        PreferenceUtil.savePreference(concurMobile.getApplicationContext(), name, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseEntryCache#shouldRefetchExpenseList()
     */
    public boolean shouldRefetchExpenseList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile.getApplicationContext());
        String name = prefs.getString(Const.PREF_USER_ID, "") + Const.PREF_USER_SHOULD_REFRESH_EXPENSE_LIST;
        return prefs.getBoolean(name, true);
    }

    @Override
    public ExpenseType getFilteredExpenseType(List<ExpenseType> expTypes, String selectedExpKey) {
        ExpenseType selectedObj = null;
        if (expTypes != null) {
            for (int j = 0; j < expTypes.size(); j++) {
                String key = expTypes.get(j).key;
                if (key != null) {
                    if (key.equals(selectedExpKey)) {
                        selectedObj = expTypes.get(j);
                    }
                }
            }
        }
        return selectedObj;
    }

    @Override
    public List<ExpenseType> sortExpenseList(List<ExpenseType> expTypes) {
        try {
            // first sort by date
            Collections.sort(expTypes, new ComparatorUtil(Operation.DATESORT));
            // then sort by count
            // Collections.sort(expTypes, new ComparatorUtil(Operation.INTEGERSORT));
        } catch (NullPointerException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".ExpenseEntryCache.sorExpenseList NullPointerException");
        }
        return expTypes;
    }

    @Override
    public List<ListItem> updateListItemCache(ConcurService concurService, String userId, String fieldId) {
        List<ListItem> retVal = null;
        if (listItemMapDB != null) {
            String key = getKey(userId, fieldId);
            if (listItemMapDB.containsKey(key)) {
                listItemMapDB.remove(key);
            }
            putListItemInCacheForMRU(concurService, userId, fieldId);
        }
        return retVal;
    }

    @Override
    public void putListItemInCacheForMRU(ConcurService concurService, String userId, String fieldId) {
        List<ListItem> listFromDB = loadListItemFromDB(concurService, fieldId);
        if (listFromDB != null) {
            final int dbSize = listFromDB.size();
            if (dbSize > 0) {
                ArrayList<ListItem> mruList = new ArrayList<ListItem>();
                listFromDB = sortListItemMruList(listFromDB);
                int countOfLastElement = listFromDB.get(dbSize - 1).getLastUseCount();
                if (countOfLastElement != 0) {
                    for (int i = dbSize - 1; ((i >= dbSize - MIN_MRU_LIST_ITEM) && (i >= 0)); i--) {
                        if (listFromDB.get(i) != null && listFromDB.get(i).getLastUseCount() > 0) {
                            mruList.add(listFromDB.get(i));
                        }
                    }
                    if (mruList.size() > 0 && listItemMapDB != null) {
                        String key = getKey(userId, fieldId);
                        if (listItemMapDB.containsKey(key)) {
                            listItemMapDB.remove(key);
                        }
                        listItemMapDB.put(key, mruList);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".ExpenseTypeSpinnerAdapter.setExpenseTypes: no currency has been used recently.");
                }
            }
        }

    }

    /**
     * Get Composite Key for Database map
     * 
     * @param userId
     * @param fieldId
     * */
    private String getKey(String userId, String fieldId) {
        StringBuilder strBldr = new StringBuilder("");
        strBldr.append(userId);
        strBldr.append(ListItem.DEFAULT_SEPARATOR);
        strBldr.append(fieldId);
        return strBldr.toString();
    }

    /**
     * Load List Item from DB
     * 
     * @param concurService
     * @param fieldId
     * @return : list from DB
     */
    private List<ListItem> loadListItemFromDB(ConcurService concurService, String fieldId) {
        MobileDatabase mdb = concurService.getMobileDatabase();
        List<ListItem> listFromDB = null;
        if (mdb != null) {
            if (concurService.prefs != null) {
                String userId = concurService.prefs.getString(Const.PREF_USER_ID, null);
                if (userId != null && fieldId != null) {
                    listFromDB = mdb.loadListItemFromDB(userId, fieldId);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".ExpenseEntryCache.loadCrnListItemFromDB: userid is null");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".ExpenseEntryCache.loadCrnListItemFromDB: concurService.prefs is null");
            }
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".ExpenseEntryCache.loadCrnListItemFromDB: database is null.");
        }
        return listFromDB;
    }

    @Override
    public List<ListItem> getListItemFromDB(String userId, String fieldId) {
        List<ListItem> list = null;
        if (listItemMapDB != null) {
            String key = getKey(userId, fieldId);
            List<ListItem> mruList = listItemMapDB.get(key);
            if (mruList != null) {
                list = new ArrayList<ListItem>();
                list.addAll(0, mruList);
            }
        }
        return list;
    }

    @Override
    public List<ListItem> sortListItemMruList(List<ListItem> crnTypList) {
        try {
            // first sort by date
            Collections.sort(crnTypList, new CurrencyListItemComparatorUtil(
                    CurrencyListItemComparatorUtil.Operation.DATESORT));
        } catch (NullPointerException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".ExpenseEntryCache.sortCrnListItemMruList NullPointerException");
        }
        return crnTypList;
    }

}
