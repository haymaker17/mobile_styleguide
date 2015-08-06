/**
 *
 */
package com.concur.mobile.platform.ExpenseIt;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.ExpenseItLoginResult;
import com.concur.mobile.platform.authentication.LoginExpenseItTask;
import com.concur.mobile.platform.ExpenseIt.test.VerifyExpenseItLoginResult;
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
 */
public class ExpenseItLoginRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = ExpenseItLoginRequestTaskTest.class.getSimpleName();

    private static final boolean DEBUG = false;

    /**
     * Contains the request login id.
     */
    private String loginId;

    /**
     * Contains the request pin/password.
     */
    private String loginPinPassword;


    public ExpenseItLoginRequestTaskTest(boolean useMockServer) {
        super(useMockServer);
    }

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

        // Set the mock response if the mock server is being used.
        if (useMockServer()) {

            // Set the mock response for the test.
            Map<String, String> responseHeaders = new HashMap<String, String>();

            // Set the content-type.
            responseHeaders.put("Content-Type", "application/json");
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "authentication/ExpenseitLoginResponse.json", responseHeaders);
        }

        // Initiate the login request.
        BaseAsyncResultReceiver loginExpenseItReplyReceiver = new BaseAsyncResultReceiver(getHander());
        loginExpenseItReplyReceiver.setListener(new AsyncReplyListenerImpl());
        LoginExpenseItTask reqTask = new LoginExpenseItTask(context, 0, loginExpenseItReplyReceiver, loginId, loginPinPassword);
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

                    // Build the parser with type deserializers.
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    ExpenseItLoginResult expenseItLoginResultResp = gson.fromJson(response, ExpenseItLoginResult.class);
                    VerifyExpenseItLoginResult verifier = new VerifyExpenseItLoginResult();
                    verifier.verify(context, expenseItLoginResultResp);

                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                    }
                    break;
                }
            }

        }

    }

}
