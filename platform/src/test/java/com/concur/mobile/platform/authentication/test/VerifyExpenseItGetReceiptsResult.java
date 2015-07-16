/**
 * 
 */
package com.concur.mobile.platform.authentication.test;

import android.content.Context;

import com.concur.mobile.platform.expenseit.ExpenseItPostReceipt;
import com.concur.mobile.platform.expenseit.ExpenseItPostReceiptResponse;
import com.concur.mobile.platform.expenseit.ExpenseItReceipt;
import com.concur.mobile.platform.expenseit.dao.ExpenseItReceiptDAO;

import org.junit.Assert;

import java.util.HashMap;

/**
 * Provides a class to verify a <code>LoginResult</code> object against data stored in the content provider.
 * 
 * @author andrewk
 */
public class VerifyExpenseItGetReceiptsResult {

    private final String MTAG = CLS_TAG + ".verify";

    private static final String CLS_TAG = VerifyExpenseItGetReceiptsResult.class.getSimpleName();

    private HashMap<Long, ExpenseItPostReceipt> getExpenseItReceipts(Context context, String userId) {

        HashMap<Long, ExpenseItPostReceipt> receiptsMap =  new HashMap<>();

        ExpenseItReceiptDAO receipts = new ExpenseItReceipt(context, userId);
        for (ExpenseItPostReceipt receipt : receipts.getReceipts()) {
            Assert.assertNotNull(MTAG + "Id should never be null", receipt.getId());
            receiptsMap.put(receipt.getId(), receipt);
        }

        return receiptsMap;
    }

     /**
     * Will verify login response information stored in the config content provider against information stored in
     * <code>loginResult</code>.
     * 
     * @param context
     *            contains a reference to the application context.
     * @param receiptResponse
     *            contains a reference to a login response.
     * @throws Exception
     *             throws an exception if the stored login result data does not match <code>loginResult</code>.
     */
    public void verify(Context context, String userId, ExpenseItPostReceiptResponse receiptResponse) throws Exception {

        HashMap<Long, ExpenseItPostReceipt> receipts = getExpenseItReceipts(context, userId);

        //Verify LoginResult is not null
        if (receiptResponse == null) {
            throw new Exception(CLS_TAG + "." + MTAG + ": LoginResult info is null!");
        }

        Assert.assertEquals(MTAG + ": entries should match", receiptResponse.getExpenses().length, receipts.size());

        Assert.assertNotNull(MTAG + ": Response return null", receiptResponse.getExpenses());

        if (receiptResponse.getExpenses() == null) {
            return;
        }
        Assert.assertTrue(MTAG + ": Expense count is not correct", receiptResponse.getExpenses().length >= 1);

        if (receiptResponse.getExpenses().length != 1) {
            return;
        }

        for (ExpenseItPostReceipt responseReceipt : receiptResponse.getExpenses()) {

            ExpenseItPostReceipt receipt = responseReceipt;

            ExpenseItPostReceipt receiptInDb = receipts.get(responseReceipt.getId());

            Assert.assertEquals(MTAG + ": getTotalImageCount should be equal ", receipt.getTotalImageCount(), receiptInDb.getTotalImageCount());
            Assert.assertEquals(MTAG + ": Receipt Image Count", receipt.getTotalImageCount(), 1);

            Assert.assertEquals(MTAG + ": getTotalImagesUploaded should be equal ", receipt.getTotalImagesUploaded(), receiptInDb.getTotalImagesUploaded());
            Assert.assertEquals(MTAG + ": Receipt Image Uploaded count", receipt.getTotalImagesUploaded(), 1);

            Assert.assertEquals(MTAG + ": getCreatedAt should be equal ", receipt.getCreatedAt(), receiptInDb.getCreatedAt());
            Assert.assertNotNull(MTAG + ": Created At", receipt.getCreatedAt());

            Assert.assertEquals(MTAG + ": getId should be equal ", receipt.getId(), receiptInDb.getId());
            Assert.assertNotNull(MTAG + ": Id not Null", receipt.getId());

            Assert.assertEquals(MTAG + ": getId should be equal ", receipt.getProcessingEngine(), receiptInDb.getProcessingEngine());
            Assert.assertNotNull(MTAG + ": Processing engine not Null", receipt.getProcessingEngine());
        }
    }
}
