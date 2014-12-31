/**
 * 
 */
package com.concur.mobile.platform.expense.receipt.list.dao;

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

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.expense.receipt.list.Receipt;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;

/**
 * Provides a receipt list data access object.
 * 
 * @author andrewk
 */
public class ReceiptListDAO {

    private static final String CLS_TAG = "ReceiptListDAO";

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
     * Contains the list of receipts.
     */
    protected List<ReceiptDAO> receipts;

    /**
     * Contains the content observer for changes to receipts.
     */
    private ContentObserver receiptObserver;

    /**
     * Contains a reference to a <code>HandlerThread</code> used to run a looper in order to receive content observer callbacks.
     */
    protected HandlerThread handlerThread;

    /**
     * Contains a reference to a <code>Handler</code> used to receive content update notifications.
     */
    protected Handler contentObserverHandler;

    /**
     * Constructs an instance of <code>ReceiptListDAO</code> given a context and user id.
     * 
     * @param context
     *            contains the application context.
     * @param userId
     *            contains the user id.
     */
    public ReceiptListDAO(Context context, String userId) {
        this.context = context;
        this.userId = userId;
    }

    /**
     * Gets the list of receipts.
     * 
     * @return the list of receipts.
     */
    public List<ReceiptDAO> getReceipts() {
        if (receipts == null) {

            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Expense.ReceiptColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { userId };

                cursor = resolver.query(Expense.ReceiptColumns.CONTENT_URI, Receipt.fullColumnList, where, whereArgs,
                        Expense.ReceiptColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        receipts = new ArrayList<ReceiptDAO>(cursor.getCount());
                        do {
                            receipts.add(new Receipt(context, cursor));
                        } while (cursor.moveToNext());
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                // Register the observer.
                registerReceiptObserver();
            }
        }
        return receipts;
    }

    /**
     * Will retrieve a <code>ReceiptDAO</code> object based on a <code>uri</code>.
     * 
     * @param uri
     *            contains the receipt uri.
     * @return return a <code>ReceiptDAO</code> object based on a <code>uri</code>.
     */
    public ReceiptDAO getReceipt(Uri uri) {
        ReceiptDAO receipt = null;

        try {
            // Check for null.
            Assert.assertNotNull("uri is null", uri);

            // Check for existence of 'uri'.
            Assert.assertTrue("uri is invalid", ContentUtils.uriExists(context, uri));

            // Retrieve the cash transaction.
            receipt = new Receipt(context, uri);

        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getReceipt: " + afe.getMessage());
            throw new IllegalArgumentException(CLS_TAG + ".getReceipt: " + afe.getMessage());
        }
        return receipt;
    }

    /**
     * Will construct a <code>ReceiptDAO</code> object that can be used to populate receipt data.
     * 
     * @return returns a <code>ReceiptDAO</code> object that can be used to populate receipt data.
     */
    public ReceiptDAO createReceipt() {

        ReceiptDAO receipt = new Receipt(context, userId);

        return receipt;
    }

    /**
     * Will trim the receipt cache based on size. The top "attached" <code>maxSize</code> receipts based on "last access time"
     * descending order will be retained.
     * 
     * @param maxSize
     *            contains the maximum number of "unattached" receipts to be retained.
     */
    public void trimBySize(int maxSize) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.ReceiptColumns.USER_ID);
            strBldr.append(" = ? AND ");
            strBldr.append(Expense.ReceiptColumns.IS_ATTACHED);
            strBldr.append(" = 1");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Expense.ReceiptColumns.CONTENT_URI, Receipt.fullColumnList, where, whereArgs,
                    Expense.ReceiptColumns.LAST_ACCESS_TIME + " DESC");
            long rowCount = 0L;
            List<ReceiptDAO> receiptsToBePunted = new ArrayList<ReceiptDAO>();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    // Populate the 'receiptsToBePunted' list.
                    do {
                        ++rowCount;
                        if (rowCount > maxSize) {
                            receiptsToBePunted.add(new Receipt(context, cursor));
                        }
                    } while (cursor.moveToNext());
                    // Punt each receipt.
                    for (ReceiptDAO recDAO : receiptsToBePunted) {
                        recDAO.delete();
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Will trim the receipt cache based on time. Receipts that are "attached" and younger than <code>time</code> will be
     * retained.
     * 
     * @param time
     *            contains the time that which receipts must be younger than to be retained.
     */
    public void trimByTime(long time) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.ReceiptColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Expense.ReceiptColumns.CONTENT_URI, Receipt.fullColumnList, where, whereArgs,
                    Expense.ReceiptColumns.LAST_ACCESS_TIME + " DESC");
            List<ReceiptDAO> receiptsToBePunted = new ArrayList<ReceiptDAO>();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    // Populate the 'receiptsToBePunted' list based on last access time.
                    do {
                        ReceiptDAO recDAO = new Receipt(context, cursor);
                        if (recDAO.getLastAccessTime() != null && recDAO.getLastAccessTime() < time) {
                            receiptsToBePunted.add(recDAO);
                        }
                    } while (cursor.moveToNext());
                    // Punt each receipt.
                    for (ReceiptDAO recDAO : receiptsToBePunted) {
                        recDAO.delete();
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Will register a content observer for tracking receipt changes.
     */
    private void registerReceiptObserver() {
        if (receiptObserver == null) {
            receiptObserver = new ContentObserver(getHandler()) {

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    receipts = null;
                }
            };

            context.getContentResolver().registerContentObserver(Expense.ReceiptColumns.CONTENT_URI, true,
                    receiptObserver);
        }
    }

    /**
     * Will unregister a content observer for tracking receipt capture changes.
     */
    private void unregisterReceiptCaptureObserver() {
        if (receiptObserver != null) {
            context.getContentResolver().unregisterContentObserver(receiptObserver);
            receiptObserver = null;
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

}
