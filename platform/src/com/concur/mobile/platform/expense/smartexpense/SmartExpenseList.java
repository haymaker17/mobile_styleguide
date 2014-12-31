package com.concur.mobile.platform.expense.smartexpense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.concur.mobile.platform.expense.list.dao.PersonalCardDAO;
import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.expense.smartexpense.dao.SmartExpenseDAO;
import com.google.gson.annotations.SerializedName;

/**
 * Models an expense list response.
 * 
 * @author yiwenw
 * 
 */
public class SmartExpenseList {

    @SerializedName("expenses")
    List<SmartExpense> expenses;

    @SerializedName("personalCards")
    List<SmartPersonalCard> personalCards;

    SmartExpenseList() {
        // No-args constructor.
    }

    /**
     * Gets the list of smart expenses.
     * 
     * @return the list of smart expenses.
     */
    public List<SmartExpenseDAO> getSmartExpenses() {
        List<SmartExpenseDAO> result = null;
        if (expenses != null) {
            result = new ArrayList<SmartExpenseDAO>(expenses.size());
            for (SmartExpense rec : expenses) {
                result.add(rec);
            }
        }
        return result;
    }

    /**
     * Gets the list of personal cards.
     * 
     * @return the list of personal cards.
     */
    public List<PersonalCardDAO> getPersonalCards() {
        List<PersonalCardDAO> result = null;
        if (personalCards != null) {
            result = new ArrayList<PersonalCardDAO>(personalCards.size());
            for (SmartPersonalCard smartPersCard : personalCards) {
                result.add(smartPersCard);
            }
        }
        return result;
    }

    /**
     * Will reconcile the list of passed in smart expenses with those smart expenses stored in the expense content provider. Smart
     * expenses in the expense content provider that have smart expense ID's and are not in the <code>items</code> list will be
     * punted.
     * 
     * @param context
     *            contains an application context.
     * @param userId
     *            contains the user id.
     * @param items
     *            contains the list of parsed expenses.
     */
    static void reconcile(Context context, String userId, List<SmartExpense> items) {

        // First, build a simple map to permit quick look-up based on smart expense ID key.
        Map<String, SmartExpense> seKeyMap = new HashMap<String, SmartExpense>((items != null) ? items.size() : 0);
        if (items != null) {
            for (SmartExpense smExp : items) {
                seKeyMap.put(smExp.getSmartExpenseId(), smExp);
            }
        }

        // Second, read in the stored smart expenses.
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.SmartExpenseColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Expense.SmartExpenseColumns.CONTENT_URI, SmartExpense.fullColumnList, where,
                    whereArgs, null);
            List<SmartExpenseDAO> expensesToBePunted = new ArrayList<SmartExpenseDAO>();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    // Populate the 'expensesToBePunted' list.
                    do {
                        SmartExpense smExp = new SmartExpense(cursor);
                        // If the smart expense id is non-null and is not found within our map, then
                        // add to the punt list.
                        if (!TextUtils.isEmpty(smExp.getSmartExpenseId())
                                && !seKeyMap.containsKey(smExp.getSmartExpenseId())) {
                            expensesToBePunted.add(smExp);
                        }
                    } while (cursor.moveToNext());

                    // Punt each smart expense.
                    for (SmartExpenseDAO seDAO : expensesToBePunted) {
                        seDAO.delete(context, userId);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }
}
