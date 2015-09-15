/**
 *
 */
package com.concur.mobile.platform.ExpenseIt;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.expenseit.DeleteExpenseItReceiptAsyncTask;
import com.concur.mobile.platform.expenseit.ErrorResponse;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.concur.mobile.platform.test.VerifyResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>PPLoginLightRequestTask</code> platform
 * request.
 *
 */
public class DeleteExpenseItReceiptTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = DeleteExpenseItReceiptTaskTest.class.getSimpleName();

    private Long expenseId;

    public DeleteExpenseItReceiptTaskTest(boolean useMockServer) {
        super(useMockServer);
    }

    public void setExpenseId(long id) {
        this.expenseId = id;
    }

    /**
     * Will perform the test throwing an exception if the test fails.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Override
    public void doTest() throws Exception {

        Context context = PlatformTestApplication.getApplication();

        // Initiate the login request.
        BaseAsyncResultReceiver deleteReceiptReceiver = new BaseAsyncResultReceiver(getHander());
        deleteReceiptReceiver.setListener(new AsyncReplyListenerImpl());

        //Make the call
        DeleteExpenseItReceiptAsyncTask reqTask = new DeleteExpenseItReceiptAsyncTask(context,
            0, deleteReceiptReceiver, expenseId);

        runTest("expenseIt/ErrorResponseSuccess.json", reqTask, new VerifyResponse<ErrorResponse>() {
            @Override
            public ErrorResponse serializeResponse(String result) {
                // Build the parser with type deserializers.
                Gson gson = new GsonBuilder().create();
                return gson.fromJson(result, ErrorResponse.class);
            }

            @Override
            public void verify(Context context, ErrorResponse response) throws Exception {
                //Verify response
                Assert.assertFalse("DeleteImage Response contains errors!", response != null && response.isError());

                ErrorResponse errorResponse1 = (ErrorResponse) result.resultData.getSerializable(DeleteExpenseItReceiptAsyncTask.DELETE_EXPENSEIT_RECEIPT_ASYNC_TASK);

                //Verify server response that is serialized
                Assert.assertFalse("Serialized DeleteImage Response contains errors!", errorResponse1 != null && errorResponse1.isError());
            }
        });
    }
}
