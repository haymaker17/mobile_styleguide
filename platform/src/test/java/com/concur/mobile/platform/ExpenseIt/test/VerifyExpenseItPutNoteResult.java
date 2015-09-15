package com.concur.mobile.platform.ExpenseIt.test;

import android.content.Context;

import com.concur.mobile.platform.expenseit.ExpenseItPostReceipt;
import com.concur.mobile.platform.expenseit.PutExpenseItNoteResponse;
import com.concur.mobile.platform.test.VerifyResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;

/**
 * @author Elliott Jacobsen-Watts
 */
public class VerifyExpenseItPutNoteResult implements VerifyResponse<PutExpenseItNoteResponse> {

    private final String MTAG = CLS_TAG + ".verify";

    private static final String CLS_TAG = VerifyExpenseItPutNoteResult.class.getSimpleName();

    @Override
    public PutExpenseItNoteResponse serializeResponse(String result) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        PutExpenseItNoteResponse response = gson.fromJson(result, PutExpenseItNoteResponse.class);
        return response;
    }

    @Override
    public void verify(Context context, PutExpenseItNoteResponse response) throws Exception {
        if (response == null) {
            throw new Exception(CLS_TAG + "." + MTAG + ": response info is null");
        }
        Assert.assertNotNull(MTAG + ": response returned null.", response.getExpenses());

        if (response.getExpenses() == null) {
            return;
        }
        Assert.assertEquals(MTAG + ": expense count is not correct.", response.getExpenses().length, 1);

        if (response.getExpenses().length != 1) {
            return;
        }

        ExpenseItPostReceipt receipt = response.getExpenses()[0];

        Assert.assertNotNull(MTAG + ": Created At", receipt.getCreatedAt());

        Assert.assertEquals(MTAG + ": Note", receipt.getNote(), "ConcurMobile");

        Assert.assertEquals(MTAG + ": Id not Null", receipt.getId(), Long.valueOf(19093406));

        Assert.assertNotNull(MTAG + ": Processing engine not Null", receipt.getProcessingEngine());
    }
}
