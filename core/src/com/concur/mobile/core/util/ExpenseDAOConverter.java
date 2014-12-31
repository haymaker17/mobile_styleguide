/**
 * Copyright (c) 2014 Concur Technologies, Inc.
 */
package com.concur.mobile.core.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.expense.charge.data.MobileEntryStatus;
import com.concur.mobile.core.expense.charge.data.PersonalCard;
import com.concur.mobile.core.expense.charge.data.PersonalCardTransaction;
import com.concur.mobile.core.expense.data.ExpenseListInfo;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.platform.expense.list.dao.PersonalCardDAO;
import com.concur.mobile.platform.expense.list.dao.PersonalCardTransactionDAO;
import com.concur.mobile.platform.expense.provider.ExpenseUtil;
import com.concur.mobile.platform.expense.smartexpense.SmartExpense;
import com.concur.mobile.platform.expense.smartexpense.dao.SmartExpenseDAO;

/**
 * TODO: CDIAZ - Once we move to the all-new Expense List UI which utilizes the new SmartExpenesDAOs, then we should delete this
 * whole class.
 * 
 * Util class for dealing with the new <code>SmartExpenesDAO</code> and old/existing <code>Expense</code> pojo.
 * 
 * @author Chris N. Diaz
 * 
 */
public final class ExpenseDAOConverter {

    public final static String CLS_TAG = ExpenseDAOConverter.class.getName();

    /**
     * Private class to ensure no initializtion.
     */
    private ExpenseDAOConverter() {

    }

    /**
     * Queries the ExpenseProvider in platform for a list of <code>SmartExpenseDAO</code> objects and then converts them to the
     * old (existing) Expense POJO. This method will then handle saving the Expenses to cache and updating any offline Expense
     * entries in the database.
     * 
     * @return the number of Expenses converted form a SmartExpenseDAO and then added to the ExpenseEntryCache or -1 if the user's
     *         session is null.
     */
    public static int migrateSmartExpenseDAOToExpenseEntryCache(String userId) {

        ConcurCore concurCore = (ConcurCore) ConcurCore.getContext();

        List<SmartExpense> smartExpenses = ExpenseUtil.getSmartExpenses(concurCore, userId);
        ExpenseListInfo expListInfo = convertSmartExpenseDAOToExpenseListInfo(smartExpenses, userId);
        IExpenseEntryCache expenseEntryCache = concurCore.getExpenseEntryCache();
        expenseEntryCache.setExpenseEntries(expListInfo);

        // Punt any non-offline expenses from the table
        // TODO This puts us in a middle ground of non-local expenses being persisted in the response table
        // TODO and local expenses persisted in the expense table.
        // TODO Rectify that and get everything into the expense table.
        ConcurService concurService = concurCore.getService();
        concurService.db.deleteNonLocalExpenseEntries(userId);

        return expListInfo.getExpenseList().size();
    }

    /**
     * Convenience method for querying the ExpenseProvider for <code>SmartExpenseDAO</code>s and then converting them to an
     * <code>ExpenseListInfo</code>
     * 
     * @param userId
     *            the user's ID.
     * @return an <code>ExpenseListInfo<code>
     */
    public static ExpenseListInfo convertSmartExpenseDAOToExpenseListInfo(List<SmartExpense> smartExpenses,
            String userId) {

        ConcurCore concurCore = (ConcurCore) ConcurCore.getContext();
        IExpenseEntryCache expEntCache = concurCore.getExpenseEntryCache();
        List<ExpenseType> expenseTypes = (ArrayList<ExpenseType>) expEntCache.getExpenseTypes();

        // Let the conversions begin!!!
        ArrayList<Expense> expenses = new ArrayList<Expense>();
        Map<String, SmartExpense> smartExpenseMap = new HashMap<String, SmartExpense>(); // Mapping used later for PCT.
        if (smartExpenses != null) {
            for (SmartExpense smartExpense : smartExpenses) {
                smartExpense.setExpenseName(findExpenseName(expenseTypes, smartExpense.getExpKey()));
                expenses.add(new Expense(smartExpense));

                if (!TextUtils.isEmpty(smartExpense.getPcaKey())) {
                    smartExpenseMap.put(smartExpense.getPcaKey(), smartExpense);
                }
            }
        }

        ConcurService concurService = concurCore.getService();
        Calendar lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // Add any offline expenses to the list before putting it in the cache
        ArrayList<MobileEntry> mes = concurService.db.loadMobileEntries(userId, MobileEntryStatus.NEW);
        if (mes != null) {
            for (MobileEntry me : mes) {
                // Add it to the reply list.
                expenses.add(new Expense(me));
            }
        }

        // Get the list of PersonalCard DAOs from GSEL then convert them to
        // the old PersonalCard object.
        List<PersonalCardDAO> personalCardDAOs = ExpenseUtil.getPersonalCards(concurCore, userId);
        ArrayList<PersonalCard> personalCards = convertPersonalCardToDAO(personalCardDAOs, smartExpenseMap);

        ExpenseListInfo expListInfo = new ExpenseListInfo(expenses, personalCards, lastRetrievedTS);

        return expListInfo;
    }

    /**
     * Convenience method for converting the new PersonalCardDAO from GSEL to the old PersonalCard object.
     * 
     * @param personalCardDAOs
     * @param smartExpenseMap
     * 
     * @return
     */
    private static ArrayList<PersonalCard> convertPersonalCardToDAO(List<PersonalCardDAO> personalCardDAOs,
            Map<String, SmartExpense> smartExpenseMap) {

        ArrayList<PersonalCard> personalCards = new ArrayList<PersonalCard>();
        if (personalCardDAOs != null) {
            for (PersonalCardDAO dao : personalCardDAOs) {

                // Create a new PersonalCard object based on the DAO.
                PersonalCard personalCard = new PersonalCard(dao);

                try {
                    // If this call fails, it means we have a SmartPersonalCard.
                    List<PersonalCardTransactionDAO> pctDAOs = dao.getPersonalCardTransactionDAOS();

                    // Now we need to go through and add the PersonalCardTransactions
                    // to each Personal Card if it matches.
                    addPersonalCardTransactions(personalCard, pctDAOs, smartExpenseMap);

                } catch (UnsupportedOperationException ex) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".convertPersonalCardToDAO(): ", ex);
                }

                // Add to the array of PCs.
                personalCards.add(personalCard);
            }
        }
        return personalCards;

    }

    /**
     * Convenience method for converting a PersonalCardTranscationDAO from the GSEL to the old PersonalCardTransaction object.
     * 
     * @param personalCard
     * @param personalCardTransactionsDAOs
     */
    private static void addPersonalCardTransactions(PersonalCard personalCard,
            List<PersonalCardTransactionDAO> personalCardTransactionsDAOs, Map<String, SmartExpense> smartExpenseMap) {

        if (personalCard != null && !TextUtils.isEmpty(personalCard.pcaKey) && personalCardTransactionsDAOs != null) {

            for (PersonalCardTransactionDAO pctDAO : personalCardTransactionsDAOs) {

                // If we found a PersonalCardTransaction DAO which is matched to the
                // given PersonalCard, then add the transaction to the card.
                if ((pctDAO.getSmartMatchedMobileEntryDAO() != null && personalCard.pcaKey.equals(pctDAO
                        .getSmartMatchedMobileEntryDAO().getPcaKey()))
                        || (pctDAO.getMobileEntryDAO() != null && personalCard.pcaKey.equals(pctDAO.getMobileEntryDAO()
                                .getPcaKey()))) {

                    // Convert the new DAO to the old PersonalCardTransaction object.
                    SmartExpenseDAO smartExpense = smartExpenseMap.get(personalCard.pcaKey);
                    PersonalCardTransaction pct = new PersonalCardTransaction(pctDAO, smartExpense);
                    personalCard.addTransaction(pct);

                }
            }
        }

    }

    /**
     * Convenience method to look up the Expense Type Name based on the Expense Key.
     * 
     * @param expenseTypes
     * @param expKey
     * @return
     */
    private static String findExpenseName(List<ExpenseType> expenseTypes, String expKey) {
        String expenseName = "Undefined"; // TODO E-DAO: Should this be localized.

        if (TextUtils.isEmpty(expKey)) {
            expKey = "UNDEF";
        }

        // MOB-21200 check whether there is expense types in cache to prevent crash
        if (expenseTypes != null && expenseTypes.size() > 0) {
            for (ExpenseType expenseType : expenseTypes) {
                if (expKey.equalsIgnoreCase(expenseType.key)) {
                    return expenseType.name;
                }
            }
        }

        return expenseName;
    }
}
