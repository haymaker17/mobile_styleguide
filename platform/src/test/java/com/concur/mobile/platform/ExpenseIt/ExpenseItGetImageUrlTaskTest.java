/**
 *
 */
package com.concur.mobile.platform.ExpenseIt;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.ExpenseIt.test.VerifyExpenseItImageUrlResult;
import com.concur.mobile.platform.expenseit.ExpenseItGetImageUrlResponse;
import com.concur.mobile.platform.expenseit.ExpenseItParseCode;
import com.concur.mobile.platform.expenseit.ExpenseItPostReceipt;
import com.concur.mobile.platform.expenseit.ExpenseItPostReceiptResponse;
import com.concur.mobile.platform.expenseit.GetExpenseItImageUrlAsyncTask;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>PPLoginLightRequestTask</code> platform
 * request.
 *
 */
public class ExpenseItGetImageUrlTaskTest extends ExpenseItTest {

    private static final String CLS_TAG = ExpenseItGetImageUrlTaskTest.class.getSimpleName();

    /**
     * Will perform the test throwing an exception if the test fails.
     *
     * @throws Exception throws an exception if the test fails.
     */
    public void validateAll() throws Exception {

        List<Long> expenseIds = new ArrayList<>();

        Context context = PlatformTestApplication.getApplication();

        // Set the mock response if the mock server is being used.
        if (PlatformTestApplication.useMockServer()) {

            // Set the mock response for the test.
            Map<String, String> responseHeaders = new HashMap();

            // Set the content-type.
            responseHeaders.put("Content-Type", "application/json");
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "expenseIt/GetExpenseItImageUrlResponse.json", responseHeaders);
            expenseIds.add(new Long(1)); //anything really
        } else {
            ExpenseItGetReceiptTaskTest receiptTaskTest = new ExpenseItGetReceiptTaskTest();
            ExpenseItPostReceiptResponse receiptResponse = receiptTaskTest.getExpenseItReceipts();
            for (ExpenseItPostReceipt receipt : receiptResponse.getExpenses()) {
                if (receipt.getParsingStatusCode() != ExpenseItParseCode.EXPIRED.value() &&
                    receipt.getParsingStatusCode() != ExpenseItParseCode.FAILED_UPLOAD_ATTEMPTS.value() &&
                    receipt.getParsingStatusCode() != ExpenseItParseCode.PERMANENT_FAILURE.value()) {
                    expenseIds.add(receipt.getId());
                }
            }
        }

        for (Long expenseId: expenseIds) {
            ExpenseItGetImageUrlResponse urlResponse = getImageUrlAsyncTaskResponse(context, expenseId);
            verifyResponse(context,urlResponse);
        }
    }

    private void verifyResponse(Context context, ExpenseItGetImageUrlResponse imageUrlResponse) throws Exception {
        VerifyExpenseItImageUrlResult verifier = new VerifyExpenseItImageUrlResult();
        verifier.verify(context, imageUrlResponse);
        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
    }

    private ExpenseItGetImageUrlResponse getImageUrlAsyncTaskResponse(Context context, Long expenseId) {

        BaseAsyncResultReceiver getExpenseItImageUrlReceiver = new BaseAsyncResultReceiver(getHander());
        getExpenseItImageUrlReceiver.setListener(new AsyncReplyListenerImpl());
        //Make the call
        GetExpenseItImageUrlAsyncTask reqTask = new GetExpenseItImageUrlAsyncTask(context,
            0, getExpenseItImageUrlReceiver, expenseId);

        reqTask.setRetainResponse(true);

        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: launching the request.");

        // Launch the request.
        launchRequest(reqTask);

        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: launched the request.");

        try {

            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: waiting for result.");

            // Wait for the result.
            waitForResult();

            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: obtained result.");

        } catch (InterruptedException intExc) {
            ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".doTest: interrupted while acquiring login result.");
            result.resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
        }

        // Examine the result.
        if (result != null) {

            // Verify result code.
            verifyExpectedResultCode(CLS_TAG);

            switch (result.resultCode) {
                case BaseAsyncRequestTask.RESULT_CANCEL: {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result cancelled.");
                    break;
                }
                case BaseAsyncRequestTask.RESULT_ERROR: {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result error.");
                    break;
                }
                case BaseAsyncRequestTask.RESULT_OK: {
                    // Verify the result.

                    // Grab the response.
                    String response = getResponseString(reqTask);
                    Assert.assertNotNull("request response is null", response);

                    // Build the parser with type+ deserializers.
                    Gson gson = new GsonBuilder().create();

                    ExpenseItGetImageUrlResponse imageUrlResponse = gson.fromJson(response, ExpenseItGetImageUrlResponse.class);
                    return imageUrlResponse;
                }
            }

        }
        return null;
    }

    @Override
    public void doTest() throws Exception {
        validateAll();
        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
    }
}
