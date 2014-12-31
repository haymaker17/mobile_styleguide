/**
 * Copyright (c) 2014 Concur Technologies, Inc.
 */
package com.concur.mobile.core.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptInfo;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptStoreCache;
import com.concur.mobile.platform.expense.receipt.list.ReceiptListUtil;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;

/**
 * 
 * TODO: CDIAZ - Once we move to the all-new Receipts Store UI which utilizes the new Receipts DA, then we should delete this
 * whole class
 * 
 * @author Chris N. Diaz
 *
 */
public class ReceiptDAOConverter {

    public final static String CLS_TAG = ReceiptDAOConverter.class.getName();

    /**
     * Private class to ensure no initialization.
     */
    private ReceiptDAOConverter() {
    }

    /**
     * 
     * @param userId
     *            the user's ID.
     * 
     * 
     * @return the number of <code>ReceiptInfo</code> objects migrated and saved to the <code>ReceiptStoreCache</code>.
     * 
     */
    public static int migrateReceiptListDAOToReceiptStoreCache(String userId) {

        ConcurCore concurCore = (ConcurCore) ConcurCore.getContext();

        // First, get the list of ReceiptDAOs saved in the new database.
        // Filter out OCR/ExpenseIt receipts.
        List<ReceiptDAO> receiptList = ReceiptListUtil.getReceiptList(concurCore, userId, true);

        // Convert new ReceiptList DAO to old list of Receipts and save to the cache.
        List<ReceiptInfo> receiptInfos = convertReceiptDAOToReceiptInfo(receiptList);

        // Update the in-memory cache.
        Calendar lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        ReceiptStoreCache receiptStoreCache = concurCore.getReceiptStoreCache();
        receiptStoreCache.setReceiptInfoList(receiptInfos, lastRetrievedTS);

        // TODO OCR: Do we need to delete anything for offline & in the old DB?

        return (receiptInfos != null) ? receiptInfos.size() : 0;
    }

    /**
     * Converts the list of <code>ReceiptDAO</code> objects to a list of <code>ReceiptInfo</code> objects.
     * 
     * @param receiptList
     *            the list of <code>ReceiptInfo</code> objects.
     * @return
     */
    public static List<ReceiptInfo> convertReceiptDAOToReceiptInfo(List<ReceiptDAO> receiptList) {

        List<ReceiptInfo> receiptInfos = new ArrayList<ReceiptInfo>();

        if (receiptList != null) {
            for (ReceiptDAO receipt : receiptList) {
                ReceiptInfo receiptInfo = new ReceiptInfo(receipt);
                receiptInfos.add(receiptInfo);
            }
        }

        return receiptInfos;
    }
}
