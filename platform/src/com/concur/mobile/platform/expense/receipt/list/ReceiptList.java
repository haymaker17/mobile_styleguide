package com.concur.mobile.platform.expense.receipt.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.service.parser.ActionStatus;
import com.google.gson.annotations.SerializedName;

/**
 * Models a receipt list response.
 * 
 * @author andrewk
 */
public class ReceiptList {

    /**
     * Contains the action status object associated with retrieving the receipt list.
     */
    @SerializedName("receiptListStatus")
    ActionStatus receiptListStatus;

    /**
     * Contains the list of receipts.
     */
    @SerializedName("ocrReceiptInfos")
    List<Receipt> receiptInfos;

    ReceiptList() {
        // No-args constructor.
    }

    /**
     * Gets the list of receipts.
     * 
     * @return the list of receipts.
     */
    public List<ReceiptDAO> getReceipts() {
        List<ReceiptDAO> receipts = null;
        if (receiptInfos != null) {
            receipts = new ArrayList<ReceiptDAO>(receiptInfos.size());
            for (Receipt rec : receiptInfos) {
                receipts.add(rec);
            }
        }
        return receipts;
    }

    /**
     * Will reconcile the list of passed in receipt objects with those items receipts stored in the expense content provider.
     * Receipts in the expense content provider that are "unattached" and not in the <code>receipts</code> list will be punted.
     * 
     * @param context
     *            contains an application context.
     * @param userId
     *            contains the user id.
     * @param receipts
     *            contains the list of parsed receipts.
     */
    static void reconcile(Context context, String userId, List<Receipt> receipts) {

        // First, build a simple map to permit quick look-up based on image ID key.
        Map<String, Receipt> recKeyMap = new HashMap<String, Receipt>((receipts != null) ? receipts.size() : 0);
        if (receipts != null) {
            for (Receipt rcpt : receipts) {
                recKeyMap.put(rcpt.getId(), rcpt);
            }
        }

        // Second, read in non-attached receipts.
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.ReceiptColumns.USER_ID);
            strBldr.append(" = ? AND ");
            strBldr.append(Expense.ReceiptColumns.IS_ATTACHED);
            strBldr.append(" = 0");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Expense.ReceiptColumns.CONTENT_URI, Receipt.fullColumnList, where, whereArgs,
                    Expense.ReceiptColumns.LAST_ACCESS_TIME + " DESC");
            List<ReceiptDAO> receiptsToBePunted = new ArrayList<ReceiptDAO>();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    // Populate the 'receiptsToBePunted' list.
                    do {
                        Receipt rcpt = new Receipt(context, cursor);
                        // If the receipt image ID is non-null and is not found within our map, then
                        // add to the punt list.
                        if (!TextUtils.isEmpty(rcpt.getId()) && !recKeyMap.containsKey(rcpt.getId())) {
                            receiptsToBePunted.add(rcpt);
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

}
