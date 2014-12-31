/**
 * 
 */
package com.concur.mobile.platform.expense.list.dao;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.concur.mobile.platform.expense.list.CorporateCardTransaction;
import com.concur.mobile.platform.expense.list.Expense;
import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;
import com.concur.mobile.platform.expense.list.MobileEntry;
import com.concur.mobile.platform.expense.list.PersonalCard;
import com.concur.mobile.platform.expense.list.PersonalCardTransaction;
import com.concur.mobile.platform.expense.list.ReceiptCapture;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;

/**
 * Provides a DAO object for accessing a list of expenses.
 * 
 * @author andrewk
 */
public class ExpenseListDAO {

    private static final String CLS_TAG = "ExpenseListDAO";

    private boolean DEBUG = true;

    /**
     * Contains the list of expenses.
     */
    private List<ExpenseDAO> expenses;

    /**
     * Contains the list of corporate card transactions.
     */
    private List<CorporateCardTransactionDAO> corporateCardTransactions;

    /**
     * Contains the content observer for changes to a corporate card transaction.
     */
    private ContentObserver corporateCardTransactionObserver;

    /**
     * Contains the list of personal card transactions.
     */
    private List<PersonalCardTransactionDAO> personalCardTransactions;

    /**
     * Contains the content observer for changes to a personal card transaction.
     */
    private ContentObserver personalCardTransactionObserver;

    /**
     * Contains the list of personal cards.
     */
    private List<PersonalCardDAO> personalCards;

    /**
     * Contains the content observer for changes to a personal card.
     */
    private ContentObserver personalCardObserver;

    /**
     * Contains the list of receipt captures.
     */
    private List<ReceiptCaptureDAO> receiptCaptures;

    /**
     * Contains the content observer for changes to a receipt capture.
     */
    private ContentObserver receiptCaptureObserver;

    /**
     * Contains the list of cash transactions.
     */
    private List<MobileEntryDAO> cashTransactions;

    /**
     * Contains the content observer for changes to a mobile entry.
     */
    private ContentObserver mobileEntryObserver;

    /**
     * Contains a reference to a <code>HandlerThread</code> used to run a looper in order to receive content observer callbacks.
     */
    protected HandlerThread handlerThread;

    /**
     * Contains a reference to a <code>Handler</code> used to receive content update notifications.
     */
    protected Handler contentObserverHandler;

    /**
     * Contains an application context.
     */
    private Context context;

    /**
     * Contains the user id.
     */
    private String userId;

    /**
     * Constructs an <code>ExpenseList</code> object for the purpose of retrieving a list of expenses.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     */
    public ExpenseListDAO(Context context, String userId) {
        this.context = context;
        this.userId = userId;
    }

    /**
     * Gets the list of expenses.
     * 
     * @return the list of expenses.
     */
    public List<ExpenseDAO> getExpenses() {
        if (expenses == null) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.CONTENT_URI,
                        Expense.fullColumnList, where, whereArgs,
                        com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        expenses = new ArrayList<ExpenseDAO>(cursor.getCount());
                        do {
                            Expense exp = new Expense(context, cursor);
                            expenses.add(exp);
                        } while (cursor.moveToNext());
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return expenses;
    }

    /**
     * Gets the list of corporate card transactions.
     * 
     * @return the list of corporate card transactions.
     */
    public List<CorporateCardTransactionDAO> getCorporateCardTransactions() {
        if (corporateCardTransactions == null) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.TYPE);
                strBldr.append(" IN ('");
                strBldr.append(ExpenseTypeEnum.CORPORATE_CARD.name());
                strBldr.append("','");
                strBldr.append(ExpenseTypeEnum.SMART_CORPORATE.name());
                strBldr.append("') AND ");
                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver
                        .query(com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.CONTENT_URI,
                                CorporateCardTransaction.fullColumnList,
                                where,
                                whereArgs,
                                com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        corporateCardTransactions = new ArrayList<CorporateCardTransactionDAO>(cursor.getCount());
                        do {
                            corporateCardTransactions.add(new CorporateCardTransaction(context, cursor));
                        } while (cursor.moveToNext());
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // Register the observer.
                registerCorporateCardTransactionObserver();
            }
        }
        return corporateCardTransactions;
    }

    public CorporateCardTransactionDAO getCorporateCardTransaction(Long expenseId) {
        CorporateCardTransactionDAO corpCardTrans = null;
        try {
            // Check for null.
            Assert.assertNotNull("expenseId is null", expenseId);

            // Check for existence of 'expenseId'.
            Uri corpCardTransUri = ContentUris.withAppendedId(
                    com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.CONTENT_URI,
                    expenseId);
            Assert.assertTrue("expenseId is invalid", ContentUtils.uriExists(context, corpCardTransUri));

            // Retrieve the corporate card transaction.
            corpCardTrans = new CorporateCardTransaction(context, corpCardTransUri);

        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getCorporateCardTransaction: " + afe.getMessage());
            throw new IllegalArgumentException(CLS_TAG + ".getCorporateCardTransaction: " + afe.getMessage());
        }
        return corpCardTrans;
    }

    /**
     * Will register a content observer for tracking corporate card transaction changes.
     */
    private void registerCorporateCardTransactionObserver() {
        if (corporateCardTransactionObserver == null) {
            corporateCardTransactionObserver = new ContentObserver(getHandler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    expenses = null;
                    corporateCardTransactions = null;
                }
            };

            context.getContentResolver().registerContentObserver(
                    com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.CONTENT_URI,
                    true, corporateCardTransactionObserver);
        }
    }

    /**
     * Will unregister a content observer for tracking corporate card transaction changes.
     */
    private void unregisterCorporateCardTransactionObserver() {
        if (corporateCardTransactionObserver != null) {
            context.getContentResolver().unregisterContentObserver(corporateCardTransactionObserver);
            corporateCardTransactionObserver = null;
        }
    }

    public List<PersonalCardTransactionDAO> getPersonalCardTransactions() {
        if (personalCardTransactions == null) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                StringBuilder strBldr = new StringBuilder();

                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.TYPE);
                strBldr.append(" IN ('");
                strBldr.append(ExpenseTypeEnum.PERSONAL_CARD.name());
                strBldr.append("','");
                strBldr.append(ExpenseTypeEnum.SMART_PERSONAL.name());
                strBldr.append("') AND ");
                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver
                        .query(com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.CONTENT_URI,
                                PersonalCardTransaction.fullColumnList,
                                where,
                                whereArgs,
                                com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        personalCardTransactions = new ArrayList<PersonalCardTransactionDAO>(cursor.getCount());
                        do {
                            personalCardTransactions.add(new PersonalCardTransaction(context, cursor));
                        } while (cursor.moveToNext());
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // Register the observer.
                registerPersonalCardTransactionObserver();
            }
        }
        return personalCardTransactions;
    }

    public PersonalCardTransactionDAO getPersonalCardTransaction(Long expenseId) {
        PersonalCardTransactionDAO persCardTrans = null;
        try {
            // Check for null.
            Assert.assertNotNull("expenseId is null", expenseId);

            // Check for existence of 'expenseId'.
            Uri persCardTransUri = ContentUris.withAppendedId(
                    com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.CONTENT_URI,
                    expenseId);
            Assert.assertTrue("expenseId is invalid", ContentUtils.uriExists(context, persCardTransUri));

            // Retrieve the personal card transaction.
            persCardTrans = new PersonalCardTransaction(context, persCardTransUri);

        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPersonalCardTransaction: " + afe.getMessage());
            throw new IllegalArgumentException(CLS_TAG + ".getPersonalCardTransaction: " + afe.getMessage());
        }
        return persCardTrans;
    }

    /**
     * Will register a content observer for tracking personal card transaction changes.
     */
    private void registerPersonalCardTransactionObserver() {
        if (personalCardTransactionObserver == null) {
            personalCardTransactionObserver = new ContentObserver(getHandler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    expenses = null;
                    personalCardTransactions = null;
                }
            };

            context.getContentResolver().registerContentObserver(
                    com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.CONTENT_URI,
                    true, personalCardTransactionObserver);
        }
    }

    /**
     * Will unregister a content observer for tracking personal card transaction changes.
     */
    private void unregisterPersonalCardTransactionObserver() {
        if (personalCardTransactionObserver != null) {
            context.getContentResolver().unregisterContentObserver(personalCardTransactionObserver);
            personalCardTransactionObserver = null;
        }
    }

    public List<PersonalCardDAO> getPersonalCards() {
        if (personalCards == null) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.PersonalCardColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(
                        com.concur.mobile.platform.expense.provider.Expense.PersonalCardColumns.CONTENT_URI,
                        PersonalCard.fullColumnList, where, whereArgs,
                        com.concur.mobile.platform.expense.provider.Expense.PersonalCardColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        personalCards = new ArrayList<PersonalCardDAO>(cursor.getCount());
                        do {
                            personalCards.add(new PersonalCard(context, cursor));
                        } while (cursor.moveToNext());
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // Register the observer.
                registerPersonalCardObserver();
            }
        }
        return personalCards;
    }

    public PersonalCardDAO getPersonalCard(Long expenseId) {
        PersonalCardDAO persCard = null;
        try {
            // Check for null.
            Assert.assertNotNull("expenseId is null", expenseId);

            // Check for existence of 'expenseId'.
            Uri persCardUri = ContentUris.withAppendedId(
                    com.concur.mobile.platform.expense.provider.Expense.PersonalCardColumns.CONTENT_URI, expenseId);
            Assert.assertTrue("expenseId is invalid", ContentUtils.uriExists(context, persCardUri));

            // Retrieve the personal card.
            persCard = new PersonalCard(context, persCardUri);

        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPersonalCard: " + afe.getMessage());
            throw new IllegalArgumentException(CLS_TAG + ".getPersonalCard: " + afe.getMessage());
        }
        return persCard;
    }

    /**
     * Will register a content observer for tracking personal card changes.
     */
    private void registerPersonalCardObserver() {
        if (personalCardObserver == null) {
            personalCardObserver = new ContentObserver(getHandler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    expenses = null;
                    personalCards = null;
                }
            };

            context.getContentResolver().registerContentObserver(
                    com.concur.mobile.platform.expense.provider.Expense.PersonalCardColumns.CONTENT_URI, true,
                    personalCardObserver);
        }
    }

    /**
     * Will unregister a content observer for tracking personal card changes.
     */
    private void unregisterPersonalCardObserver() {
        if (personalCardObserver != null) {
            context.getContentResolver().unregisterContentObserver(personalCardObserver);
            personalCardObserver = null;
        }
    }

    public List<ReceiptCaptureDAO> getReceiptCaptures() {
        if (receiptCaptures == null) {

            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.ReceiptCaptureColumns.TYPE);
                strBldr.append(" = '");
                strBldr.append(ExpenseTypeEnum.RECEIPT_CAPTURE.name());
                strBldr.append("' AND ");

                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.ReceiptCaptureColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(
                        com.concur.mobile.platform.expense.provider.Expense.ReceiptCaptureColumns.CONTENT_URI,
                        ReceiptCapture.fullColumnList, where, whereArgs,
                        com.concur.mobile.platform.expense.provider.Expense.ReceiptCaptureColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        receiptCaptures = new ArrayList<ReceiptCaptureDAO>(cursor.getCount());
                        do {
                            receiptCaptures.add(new ReceiptCapture(context, cursor));
                        } while (cursor.moveToNext());
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // Register the observer.
                registerReceiptCaptureObserver();
            }
        }
        return receiptCaptures;
    }

    public ReceiptCaptureDAO getReceiptCapture(Long expenseId) {
        ReceiptCaptureDAO recCap = null;
        try {
            // Check for null.
            Assert.assertNotNull("expenseId is null", expenseId);

            // Check for existence of 'expenseId'.
            Uri recCapUri = ContentUris.withAppendedId(
                    com.concur.mobile.platform.expense.provider.Expense.ReceiptCaptureColumns.CONTENT_URI, expenseId);
            Assert.assertTrue("expenseId is invalid", ContentUtils.uriExists(context, recCapUri));

            // Retrieve the receipt capture.
            recCap = new ReceiptCapture(context, recCapUri);

        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getReceiptCapture: " + afe.getMessage());
            throw new IllegalArgumentException(CLS_TAG + ".getReceiptCapture: " + afe.getMessage());
        }
        return recCap;
    }

    /**
     * Will register a content observer for tracking receipt capture changes.
     */
    private void registerReceiptCaptureObserver() {
        if (receiptCaptureObserver == null) {
            receiptCaptureObserver = new ContentObserver(getHandler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    expenses = null;
                    receiptCaptures = null;
                }
            };

            context.getContentResolver().registerContentObserver(
                    com.concur.mobile.platform.expense.provider.Expense.ReceiptCaptureColumns.CONTENT_URI, true,
                    receiptCaptureObserver);
        }
    }

    /**
     * Will unregister a content observer for tracking receipt capture changes.
     */
    private void unregisterReceiptCaptureObserver() {
        if (receiptCaptureObserver != null) {
            context.getContentResolver().unregisterContentObserver(receiptCaptureObserver);
            receiptCaptureObserver = null;
        }
    }

    public List<MobileEntryDAO> getCashTransactions() {
        if (cashTransactions == null) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.TYPE);
                strBldr.append(" = '");
                strBldr.append(ExpenseTypeEnum.CASH.name());
                strBldr.append("' AND ");
                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(
                        com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.CONTENT_URI,
                        MobileEntry.fullColumnList, where, whereArgs,
                        com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        cashTransactions = new ArrayList<MobileEntryDAO>(cursor.getCount());
                        do {
                            cashTransactions.add(new MobileEntry(cursor));
                        } while (cursor.moveToNext());
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // Register the observer.
                registerMobileEntryObserver();
            }
        }
        return cashTransactions;
    }

    public MobileEntryDAO getCashTransaction(Long expenseId) {

        MobileEntryDAO mobEnt = null;
        try {
            // Check for null.
            Assert.assertNotNull("expenseId is null", expenseId);

            // Check for existence of 'expenseId'.
            Uri mobEntUri = ContentUris.withAppendedId(
                    com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.CONTENT_URI, expenseId);
            Assert.assertTrue("expenseId is invalid", ContentUtils.uriExists(context, mobEntUri));

            // Retrieve the cash transaction.
            mobEnt = new MobileEntry(context, mobEntUri);

        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getCashTransaction: " + afe.getMessage());
            throw new IllegalArgumentException(CLS_TAG + ".getCashTransaction: " + afe.getMessage());
        }
        return mobEnt;
    }

    /**
     * Will register a content observer for tracking mobile entry changes.
     */
    private void registerMobileEntryObserver() {
        if (mobileEntryObserver == null) {
            mobileEntryObserver = new ContentObserver(getHandler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    expenses = null;
                    cashTransactions = null;
                }
            };

            context.getContentResolver().registerContentObserver(
                    com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.CONTENT_URI, true,
                    mobileEntryObserver);
        }
    }

    /**
     * Will unregister a content observer for tracking mobile entry changes.
     */
    private void unregisterMobileEntryObserver() {
        if (mobileEntryObserver != null) {
            context.getContentResolver().unregisterContentObserver(mobileEntryObserver);
            mobileEntryObserver = null;
        }
    }

    /**
     * Will retrieve the instance of <code>Handler</code> used to send the result.
     * 
     * @return returns an instance of <code>Handler</code> used to send the result.
     */
    protected Handler getHandler() {

        if (contentObserverHandler == null) {
            // Construct the handler thread.
            handlerThread = new HandlerThread("AsyncContentObserver");
            handlerThread.start();
            // Construct the handler using the handler thread looper.
            contentObserverHandler = new Handler(handlerThread.getLooper());
        }
        return contentObserverHandler;
    }

    @Override
    public void finalize() throws Throwable {
        unregisterCorporateCardTransactionObserver();
        unregisterPersonalCardTransactionObserver();
        unregisterPersonalCardObserver();
        unregisterMobileEntryObserver();
        unregisterReceiptCaptureObserver();
        // Quit the handler thread.
        if (handlerThread != null) {
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".finalize: quitting handler thread.");
            }
            handlerThread.quit();
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".finalize: quit handler thread.");
            }
            handlerThread = null;
        }
        super.finalize();
    }

    /**
     * Will create a new cash expense. <br>
     * <br>
     * Creates a new cash expense
     * 
     * @return returns the newly created cash expense.
     */
    public ExpenseDAO createCashExpense() {

        // Construct the mobile entry and call update.
        MobileEntry mobEntry = new MobileEntry();
        ExpenseDAO expense = new Expense(mobEntry);
        expense.update(context, userId);

        return expense;
    }
}
