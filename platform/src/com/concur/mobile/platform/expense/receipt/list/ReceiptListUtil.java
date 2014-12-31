/**
 * Copyright (c) 2014 Concur Technologies, Inc.
 */
package com.concur.mobile.platform.expense.receipt.list;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.ocr.OcrStatusEnum;

/**
 * @author Chris N. Diaz
 * 
 */
public final class ReceiptListUtil {

    /**
     * Private constructor to ensure Singleton.
     */
    private ReceiptListUtil() {
    }

    /**
     * Returns the list of <code>ReceiptDAO</code> objects persisted in the database, otherwise an empty list is returned if
     * nothing is currently saved in the database.
     * 
     * @param context
     *            the application context.
     * @param userId
     *            the user's ID
     * @param filterOcrReceipts
     *            if <code>true</code> don't return OCR/ExpenseIt receipts.
     * @return the list of <code>ReceiptDAO</code> objects or an empty list if nothing is currently stored in the database.
     * 
     */
    public static List<ReceiptDAO> getReceiptList(Context context, String userId, boolean filterOcrReceipts) {

        List<ReceiptDAO> receiptList = new ArrayList<ReceiptDAO>();

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
                    do {
                        Receipt receipt = new Receipt(context, cursor);
                        // If we need to filter OCR/ExpenseIt receipts, only include those
                        // whose ocrStatus is "OCR_NOT_AVAILABLE", "OCR_STAT_UNKNOWN" or "NOT_COMPANY_ENABLED"
                        // OCR: Get with Niru about status "OCR_STAT_UNKNOWN"
                        if (filterOcrReceipts) {

                            String ocrStatus = receipt.getOcrStatus();
                            if (TextUtils.isEmpty(ocrStatus)
                                    || OcrStatusEnum.OCR_NOT_AVAILABLE.toString().equalsIgnoreCase(ocrStatus)
                                    || OcrStatusEnum.OCR_STAT_UNKNOWN.toString().equalsIgnoreCase(ocrStatus)
                                    || OcrStatusEnum.OCR_NOT_COMPANY_ENABLED.toString().equalsIgnoreCase(ocrStatus)
                                    || OcrStatusEnum.A_DONE.toString().equalsIgnoreCase(ocrStatus)
                                    || OcrStatusEnum.M_DONE.toString().equalsIgnoreCase(ocrStatus)) {

                                receiptList.add(receipt);
                            }
                        }
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return receiptList;
    }

    /**
     * Returns a list of OCR'd <code>ReceiptDAO</code> - that is, a receipt whose status is NOT "OCR_NOT_AVAILABLE",
     * "OCR_STAT_UNKNOWN", and "NOT_COMPANY_ENABLED".
     * 
     * @param context
     *            the application context.
     * @param userId
     *            the user's ID
     * @param includeDoneStatus
     *            if <code>false</code>, filter out "A_DONE" and "M_DONE" OCR status.
     * @return
     */
    public static List<ReceiptDAO> getOcrReceiptList(Context context, String userId, boolean includeDoneStatus) {

        List<ReceiptDAO> receiptList = new ArrayList<ReceiptDAO>();

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
                    do {
                        // OCR: Get with Niru about status "OCR_STAT_UNKNOWN"
                        Receipt receipt = new Receipt(context, cursor);
                        String ocrStatus = receipt.getOcrStatus();
                        if (!OcrStatusEnum.OCR_NOT_AVAILABLE.toString().equalsIgnoreCase(ocrStatus)
                                && !OcrStatusEnum.OCR_STAT_UNKNOWN.toString().equalsIgnoreCase(ocrStatus)
                                && !OcrStatusEnum.OCR_NOT_COMPANY_ENABLED.toString().equalsIgnoreCase(ocrStatus)) {

                            if (!includeDoneStatus
                                    && (OcrStatusEnum.A_DONE.toString().equalsIgnoreCase(ocrStatus) || OcrStatusEnum.M_DONE
                                            .toString().equalsIgnoreCase(ocrStatus))) {
                                // Skip DONE statuses.
                                continue;
                            } else {
                                receiptList.add(receipt);
                            }
                        }
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return receiptList;
    }
}
