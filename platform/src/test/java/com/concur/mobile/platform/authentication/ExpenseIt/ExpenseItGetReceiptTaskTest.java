/**
 *
 */
package com.concur.mobile.platform.authentication.ExpenseIt;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.authentication.test.VerifyExpenseItGetReceiptsResult;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expenseit.ExpenseItPostReceiptResponse;
import com.concur.mobile.platform.expenseit.GetExpenseItExpenseListAsyncTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;

import java.util.HashMap;
import java.util.Map;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>PPLoginLightRequestTask</code> platform
 * request.
 *
 * @author andrewk
 */
public class ExpenseItGetReceiptTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = ExpenseItGetReceiptTaskTest.class.getSimpleName();

    private static final boolean DEBUG = false;

    /**
     * Contains the request login id.
     */
    private String loginId;

    /**
     * Contains the request pin/password.
     */
    private String loginPinPassword;

    /**
     * Sets the authentication credentials.
     *
     * @param loginId          contains the login id.
     * @param loginPinPassword contains the pin/password.
     */
    public void setCredentials(String loginId, String loginPinPassword) {
        this.loginId = loginId;
        this.loginPinPassword = loginPinPassword;
    }

    /**
     * Will perform the test throwing an exception if the test fails.
     *
     * @throws Exception throws an exception if the test fails.
     */
    public void doTest() throws Exception {

        Context context = PlatformTestApplication.getApplication();

        // Verify User Information.
        SessionInfo sessionInfo = ConfigUtil.getSessionInfo(context);
        String userId = sessionInfo.getUserId();

        // Set the mock response if the mock server is being used.
        if (PlatformTestApplication.useMockServer()) {

            // Set the mock response for the test.
            Map<String, String> responseHeaders = new HashMap();

            // Set the content-type.
            responseHeaders.put("Content-Type", "application/json");
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "receipt/GetExpenseItExpenseListResponse.json", responseHeaders);
        }

        // Initiate the login request.
        BaseAsyncResultReceiver getExpenseItReceiptReceiver = new BaseAsyncResultReceiver(getHander());
        getExpenseItReceiptReceiver.setListener(new AsyncReplyListenerImpl());
        //Make the call
        GetExpenseItExpenseListAsyncTask reqTask = new GetExpenseItExpenseListAsyncTask(context,
            0, userId, getExpenseItReceiptReceiver);

        reqTask.setRetainResponse(true);

        if (DEBUG) {
            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: launching the request.");
        }

        // Launch the request.
        launchRequest(reqTask);

        if (DEBUG) {
            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: launched the request.");
        }

        try {
            if (DEBUG) {
                ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: waiting for result.");
            }
            // Wait for the result.
            waitForResult();
            if (DEBUG) {
                ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: obtained result.");
            }
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
                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result cancelled.");
                    }
                    break;
                }
                case BaseAsyncRequestTask.RESULT_ERROR: {
                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result error.");
                    }
                    break;
                }
                case BaseAsyncRequestTask.RESULT_OK: {
                    // Verify the result.

                    // Grab the response.
                    String response = getResponseString(reqTask);
                    Assert.assertNotNull("request response is null", response);

                    // Build the parser with type+ deserializers.
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

                    ExpenseItPostReceiptResponse receiptResponse = gson.fromJson(response, ExpenseItPostReceiptResponse.class);
                    VerifyExpenseItGetReceiptsResult verifier = new VerifyExpenseItGetReceiptsResult();
                    verifier.verify(context, userId, receiptResponse);

                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                    }
                    break;
                }
            }

        }

    }

}
