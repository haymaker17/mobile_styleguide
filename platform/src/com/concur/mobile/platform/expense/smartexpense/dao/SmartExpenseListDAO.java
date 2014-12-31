/**
 * 
 */
package com.concur.mobile.platform.expense.smartexpense.dao;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.concur.mobile.platform.expense.list.PersonalCard;
import com.concur.mobile.platform.expense.list.dao.PersonalCardDAO;
import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.expense.smartexpense.SmartExpense;
import com.concur.mobile.platform.expense.smartexpense.SmartPersonalCard;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;

/**
 * Provides a smart expense list data access object.
 * 
 * @author sunill
 */
public class SmartExpenseListDAO {

    private static final String CLS_TAG = "SmartExpenseListDAO";

    private boolean DEBUG = true;

    /**
     * Contains an application context.
     */
    protected Context context;

    /**
     * Contains the user id.
     */
    protected String userId;

    /**
     * Contains the list of smart expenses.
     */
    protected List<SmartExpenseDAO> smartExpenses;

    /**
     * Contains the list of personal cards.
     */
    protected List<PersonalCardDAO> personalCards;

    /**
     * Contains the content observer for changes to smart expense.
     */
    private ContentObserver smartExpenseObserver;

    /**
     * Contains the content observer for changes to personal cards.
     */
    private ContentObserver personalCardObserver;

    /**
     * Contains a reference to a <code>HandlerThread</code> used to run a looper in order to receive content observer callbacks.
     */
    protected HandlerThread handlerThread;

    /**
     * Contains a reference to a <code>Handler</code> used to receive content update notifications.
     */
    protected Handler contentObserverHandler;

    /**
     * Constructs an instance of <code>SmartExpenseListDAO</code> given a context and user id.
     * 
     * @param context
     *            contains the application context.
     * @param userId
     *            contains the user id.
     */
    public SmartExpenseListDAO(Context context, String userId) {
        this.context = context;
        this.userId = userId;
    }

    /**
     * Will retrieve a list of <code>SmartExpenseDAO</code>.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     * @return return a list of <code>SmartExpenseDAO</code> objects.
     */

    public List<SmartExpenseDAO> getSmartExpenses(Context context, String userId) {
        if (smartExpenses == null) {

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
                        smartExpenses = new ArrayList<SmartExpenseDAO>(cursor.getCount());
                        do {
                            smartExpenses.add(new SmartExpense(cursor));
                        } while (cursor.moveToNext());
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // Register the observer.
                registerSmartExpenseObserver();
            }
        }
        return smartExpenses;
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

    public List<PersonalCardDAO> getPersonalCards(Context context, String userId) {
        if (personalCards == null) {

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
                // Register the observer.
                registerPersonalCardObserver();
            }
        }
        return personalCards;
    }

    /**
     * Will retrieve a <code>SmartExpenseDAO</code> object based on a <code>uri</code>.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param uri
     *            contains the smart expense uri.
     * @return return a <code>SmartExpenseDAO</code> object based on a <code>uri</code>.
     */
    public SmartExpenseDAO getSmartExpense(Context context, Uri uri) {
        SmartExpenseDAO smartExpense = null;

        try {
            // Check for null.
            Assert.assertNotNull("uri is null", uri);

            // Check for existence of 'uri'.
            Assert.assertTrue("uri is invalid", ContentUtils.uriExists(context, uri));

            // Retrieve the cash transaction.
            smartExpense = new SmartExpense(context, uri);

        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getSmartExpense: " + afe.getMessage());
            throw new IllegalArgumentException(CLS_TAG + ".getSmartExpense: " + afe.getMessage());
        }
        return smartExpense;
    }

    /**
     * Will retrieve a <code>PersonalCardDAO</code> object based on a <code>uri</code>.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param uri
     *            contains the personal card uri.
     * @return return a <code>PersonalCardDAO</code> object based on a <code>uri</code>.
     */
    public PersonalCardDAO getPersonalCard(Context context, Uri uri) {
        PersonalCardDAO personalCard = null;

        try {
            // Check for null.
            Assert.assertNotNull("uri is null", uri);

            // Check for existence of 'uri'.
            Assert.assertTrue("uri is invalid", ContentUtils.uriExists(context, uri));

            // Retrieve the cash transaction.
            personalCard = new SmartPersonalCard(context, uri);

        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPersonalCard: " + afe.getMessage());
            throw new IllegalArgumentException(CLS_TAG + ".getPersonalCard: " + afe.getMessage());
        }
        return personalCard;
    }

    /**
     * Will construct a <code>SmartExpenseDAO</code> object that can be used to populate smart expense data.
     * 
     * @return returns a <code>SmartExpenseDAO</code> object that can be used to populate smart expense data.
     */
    public SmartExpenseDAO createSmartExpense() {

        SmartExpenseDAO smartExpense = new SmartExpense(context, userId);

        return smartExpense;
    }

    /**
     * Will construct a <code>PersonalCardDAO</code> object that can be used to populate personal card data.
     * 
     * @return returns a <code>PersonalCardDAO</code> object that can be used to populate personal card data.
     */
    public PersonalCardDAO createPersonalCard() {

        PersonalCardDAO personalCard = new SmartPersonalCard();

        return personalCard;
    }

    /**
     * Will register a content observer for tracking smart expense changes.
     */
    private void registerSmartExpenseObserver() {
        if (smartExpenseObserver == null) {
            smartExpenseObserver = new ContentObserver(getHandler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    smartExpenses = null;
                }
            };

            context.getContentResolver().registerContentObserver(Expense.SmartExpenseColumns.CONTENT_URI, true,
                    smartExpenseObserver);
        }
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
                    personalCards = null;
                }
            };

            context.getContentResolver().registerContentObserver(Expense.PersonalCardColumns.CONTENT_URI, true,
                    personalCardObserver);
        }
    }

    /**
     * Will unregister a content observer for tracking smart expense changes.
     */
    private void unregisterSmartExpenseObserver() {
        if (smartExpenseObserver != null) {
            context.getContentResolver().unregisterContentObserver(smartExpenseObserver);
            smartExpenseObserver = null;
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

    /**
     * Will retrieve the instance of <code>Handler</code> used to send the result.
     * 
     * @return returns an instance of <code>Handler</code> used to send the result.
     */
    protected Handler getHandler() {

        if (contentObserverHandler == null) {
            // Construct the handler thread.
            handlerThread = new HandlerThread("SmartExpenseAsyncContentObserver");
            handlerThread.start();
            // Construct the handler using the handler thread looper.
            contentObserverHandler = new Handler(handlerThread.getLooper());
        }
        return contentObserverHandler;
    }

    @Override
    public void finalize() throws Throwable {
        unregisterSmartExpenseObserver();
        unregisterPersonalCardObserver();
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

}
