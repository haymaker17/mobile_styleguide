/**
 *
 */
package com.concur.mobile.platform.ExpenseIt.test;

import android.content.Context;

import com.concur.mobile.platform.expenseit.ExpenseItPostReceipt;
import com.concur.mobile.platform.expenseit.ExpenseItPostReceiptResponse;
import com.concur.mobile.platform.test.VerifyResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;

/**
 * Provides a class to verify a <code>LoginResult</code> object against data stored in the content provider.
 *
 * @author andrewk
 */
public class VerifyExpenseItUploadReceiptResult implements VerifyResponse<ExpenseItPostReceiptResponse> {

    final String MTAG = CLS_TAG + ".verify";

    private static final String CLS_TAG = VerifyExpenseItUploadReceiptResult.class.getSimpleName();

    /**
     * Build the parser with type deserializers.
     * @param result
     * @return
     */
    @Override
    public ExpenseItPostReceiptResponse serializeResponse(String result) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        ExpenseItPostReceiptResponse resp = gson.fromJson(result, ExpenseItPostReceiptResponse.class);
        return resp;
    }

    /**
     * Will verify login response information stored in the config content provider against information stored in
     * <code>loginResult</code>.
     *
     * @param context         contains a reference to the application context.
     * @param receiptResponse contains a reference to a login response.
     * @throws Exception throws an exception if the stored login result data does not match <code>loginResult</code>.
     */
    @Override
    public void verify(Context context, ExpenseItPostReceiptResponse receiptResponse) throws Exception {

        //Verify LoginResult is not null
        if (receiptResponse == null) {
            throw new Exception(CLS_TAG + "." + MTAG + ": LoginResult info is null!");
        }

        Assert.assertNotNull(MTAG + ": Response return null", receiptResponse.getExpenses());

        if (receiptResponse.getExpenses() == null) {
            return;
        }
        Assert.assertEquals(MTAG + ": Expense count is not correct", receiptResponse.getExpenses().length, 1);

        if (receiptResponse.getExpenses().length != 1) {
            return;
        }

        ExpenseItPostReceipt receipt = receiptResponse.getExpenses()[0];

        Assert.assertEquals(MTAG + ": Receipt Image Count", receipt.getTotalImageCount(), 1);

        Assert.assertEquals(MTAG + ": Receipt Image Uploaded count", receipt.getTotalImagesUploaded(), 1);

        Assert.assertNotNull(MTAG + ": Created At", receipt.getCreatedAt());

        Assert.assertNotNull(MTAG + ": Id not Null", receipt.getId());

        Assert.assertNotNull(MTAG + ": Processing engine not Null", receipt.getProcessingEngine());

    }
}
