/**
 * 
 */
package com.concur.mobile.platform.expense.provider;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.expense.list.PersonalCard;
import com.concur.mobile.platform.expense.list.dao.PersonalCardDAO;
import com.concur.mobile.platform.expense.smartexpense.SmartExpense;
import com.concur.mobile.platform.expense.smartexpense.SmartPersonalCard;
import com.concur.mobile.platform.expenseit.ExpenseItReceipt;
import com.concur.mobile.platform.provider.PlatformContentProvider;
import com.concur.mobile.platform.util.Const;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a set of utility methods for accessing the Expense content provider.
 * 
 * @author andrewk
 */
public class ExpenseUtil {

    private static final String CLS_TAG = "ExpenseUtil";

    /**
     * Convenience method for querying the DB via ContentProvider to retrieve the list of Smart Expenses.
     * 
     * If no Smart Expenses are found, an empty <code>List</code> is returned.
     * 
     * @param context
     *            a <code>Context</code> used get a <code>ContentResolver</code>.
     * @param userId
     *            the user's ID used for retrieving the data.
     * 
     * @return a <code>List</code> of <code>SmartExpenseDAO</code>s, or an empty list.
     */
    public static List<SmartExpense> getSmartExpenses(Context context, String userId) {

        List<SmartExpense> smartExpenses = new ArrayList<SmartExpense>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.SmartExpenseColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Expense.SmartExpenseColumns.CONTENT_URI, SmartExpense.fullColumnList, where,
                    whereArgs, Expense.SmartExpenseColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        smartExpenses.add(new SmartExpense(cursor));
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return smartExpenses;
    }

    public static ExpenseItReceipt getExpenseIt(Context context, String userId, long expenseItId) {

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.SmartExpenseColumns.USER_ID);
            strBldr.append(" = ? AND ");
            strBldr.append(Expense.ExpenseItReceiptColumns.ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId, String.valueOf(expenseItId) };

            cursor = resolver.query(Expense.ExpenseItReceiptColumns.CONTENT_URI, null, where,
                    whereArgs, Expense.ExpenseItReceiptColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    ExpenseItReceipt expenseItReceipt = new ExpenseItReceipt(context, cursor);
                    return expenseItReceipt;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Will retrieve a list of <code>PersonalCardDAO</code> objects.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     * @return return a list of <code>PersonalCardDAO</code> objects.
     */
    public static List<PersonalCardDAO> getPersonalCards(Context context, String userId) {

        List<PersonalCardDAO> personalCards = new ArrayList<PersonalCardDAO>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.PersonalCardColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Expense.PersonalCardColumns.CONTENT_URI, PersonalCard.fullColumnList, where,
                    whereArgs, Expense.PersonalCardColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    personalCards = new ArrayList<PersonalCardDAO>(cursor.getCount());
                    do {
                        personalCards.add(new SmartPersonalCard(context, cursor));
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return personalCards;
    }

    /**
     * Convenience method for querying the DB via ContentProvider to delete the list of Smart Expenses.
     * 
     * If no Smart Expenses are found, an false is returned.
     * 
     * @param context
     *            a <code>Context</code> used get a <code>ContentResolver</code>.
     * @param userId
     *            the user's ID used for retrieving the data.
     * 
     * @return true if we successfully clean the database.
     */
    public static boolean clearSmartExpenses(Context context, String userId) {

        boolean retVal = false;

        ContentResolver resolver = context.getContentResolver();
        int numberOfRecords;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(Expense.SmartExpenseColumns.USER_ID);
        strBldr.append(" = ?");
        String where = strBldr.toString();
        String[] whereArgs = { userId };
        numberOfRecords = resolver.delete(Expense.SmartExpenseColumns.CONTENT_URI, where, whereArgs);
        if (numberOfRecords >= 0) {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Will set the passphrase used to access content from the <code>Expense</code> content provider.
     * 
     * @param context
     *            contains an application context.
     * @param passphrase
     *            contains the passphrase.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     * @throws IllegalArgumentException
     *             if <code>passphrase</code> is null or empty
     */
    @SuppressLint("NewApi")
    public static boolean setPassphrase(Context context, String passphrase) {

        if (TextUtils.isEmpty(passphrase)) {
            throw new IllegalArgumentException(CLS_TAG + ".setPassphrase: passphrase is null or empty!");
        }

        boolean retVal = true;
        Bundle result = null;
        // Check for HoneyComb or later to execute 'call' on the ContentResolver object.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ContentResolver resolver = context.getContentResolver();
            result = resolver.call(Expense.AUTHORITY_URI, PlatformContentProvider.PROVIDER_METHOD_SET_PASSPHRASE,
                    passphrase, null);
        } else {
            // First, attempt to retrieve an instance of ExpenseProvider if the content resolver
            // has created one, if not, then force the content resolver to create it with a bogus query.
            ExpenseProvider expenseProvider = ExpenseProvider.getExpenseProvider();
            if (expenseProvider == null) {
                ContentResolver resolver = context.getContentResolver();
                // Force the content resolver to create the ExpenseProvider. The query will fail immediately since
                // it doesn't refer to any actual table within the Expense provider.
                try {
                    resolver.query(Expense.AUTHORITY_URI, null, null, null, null);
                } catch (Exception exc) {
                    // No-op...
                    Log.i(Const.LOG_TAG, CLS_TAG + ".setPassphrase: forced creation of provider -- ignore this error: "
                            + exc.getMessage());
                }
                expenseProvider = ExpenseProvider.getExpenseProvider();
            }
            if (expenseProvider != null) {
                result = expenseProvider.call(PlatformContentProvider.PROVIDER_METHOD_SET_PASSPHRASE, passphrase, null);
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG
                        + ".setPassphrase: unable to force creation of the Expense content provider!");
            }
        }
        if (result != null) {
            retVal = result.getBoolean(PlatformContentProvider.PROVIDER_METHOD_RESULT_KEY, false);
        } else {
            retVal = false;
        }
        return retVal;
    }

    /**
     * Will reset the passphrase used to access content from the <code>Expense</code> content provider.
     * 
     * @param context
     *            contains an application context.
     * @param currentPassphrase
     *            contains the current passphrase.
     * @param newPassphrase
     *            contains the new passphrase.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     * @throws IllegalArgumentException
     *             if <code>currentPassphrase</code> or <code>newPassphrase</code> is null or empty.
     */
    @SuppressLint("NewApi")
    public static boolean resetPassphrase(Context context, String currentPassphrase, String newPassphrase) {

        if (TextUtils.isEmpty(currentPassphrase)) {
            throw new IllegalArgumentException(CLS_TAG + ".resetPassphrase: currentPassphrase is null or empty!");
        }
        if (TextUtils.isEmpty(newPassphrase)) {
            throw new IllegalArgumentException(CLS_TAG + ".resetPassphrase: newPassphrase is null or empty!");
        }

        boolean retVal = true;
        Bundle result = null;
        Bundle extras = new Bundle();
        extras.putString(PlatformContentProvider.PROVIDER_METHOD_PASSPHRASE_KEY, newPassphrase);
        // Check for HoneyComb or later to execute 'call' on the ContentResolver object.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ContentResolver resolver = context.getContentResolver();
            result = resolver.call(Expense.AUTHORITY_URI, PlatformContentProvider.PROVIDER_METHOD_RESET_PASSPHRASE,
                    currentPassphrase, extras);
        } else {
            // First, attempt to retrieve an instance of ExpenseProvider if the content resolver
            // has created one, if not, then force the content resolver to create it with a bogus query.
            ExpenseProvider expenseProvider = ExpenseProvider.getExpenseProvider();
            if (expenseProvider == null) {
                ContentResolver resolver = context.getContentResolver();
                // Force the content resolver to create the ExpenseProvider. The query will fail immediately since
                // it doesn't refer to any actual table within the Expense provider.
                try {
                    resolver.query(Expense.AUTHORITY_URI, null, null, null, null);
                } catch (Exception exc) {
                    // No-op...
                    Log.i(Const.LOG_TAG, CLS_TAG
                            + ".resetPassphrase: forced creation of provider -- ignore this error: " + exc.getMessage());
                }
                expenseProvider = ExpenseProvider.getExpenseProvider();
            }
            if (expenseProvider != null) {
                result = expenseProvider.call(PlatformContentProvider.PROVIDER_METHOD_RESET_PASSPHRASE,
                        currentPassphrase, extras);
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG
                        + ".resetPassphrase: unable to force creation of the Expense content provider!");
            }
        }
        if (result != null) {
            retVal = result.getBoolean(PlatformContentProvider.PROVIDER_METHOD_RESULT_KEY, false);
        } else {
            retVal = false;
        }
        return retVal;
    }

    /**
     * Will clear all content contained in the <code>Expense</code> content provider.
     * 
     * @param context
     *            contains an application context.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    @SuppressLint("NewApi")
    public static boolean clearContent(Context context) {
        boolean retVal = true;
        Bundle result = null;
        // Check for HoneyComb or later to execute 'call' on the ContentResolver object.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ContentResolver resolver = context.getContentResolver();
            result = resolver.call(Expense.AUTHORITY_URI, PlatformContentProvider.PROVIDER_METHOD_CLEAR_CONTENT, null,
                    null);
        } else {
            // First, attempt to retrieve an instance of ExpenseProvider if the content resolver
            // has created one, if not, then force the content resolver to create it with a bogus query.
            ExpenseProvider expenseProvider = ExpenseProvider.getExpenseProvider();
            if (expenseProvider == null) {
                ContentResolver resolver = context.getContentResolver();
                // Force the content resolver to create the ExpenseProvider. The query will fail immediately since
                // it doesn't refer to any actual table within the Expense provider.
                try {
                    resolver.query(Expense.AUTHORITY_URI, null, null, null, null);
                } catch (Exception exc) {
                    // No-op...
                    Log.i(Const.LOG_TAG, CLS_TAG + ".clearContent: forced creation of provider -- ignore this error: "
                            + exc.getMessage());
                }
                expenseProvider = ExpenseProvider.getExpenseProvider();
            }
            if (expenseProvider != null) {
                result = expenseProvider.call(PlatformContentProvider.PROVIDER_METHOD_CLEAR_CONTENT, null, null);
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG
                        + ".clearContent: unable to force creation of the Expense content provider!");
            }
        }
        if (result != null) {
            retVal = result.getBoolean(PlatformContentProvider.PROVIDER_METHOD_RESULT_KEY, false);
        } else {
            retVal = false;
        }
        return retVal;
    }

}
