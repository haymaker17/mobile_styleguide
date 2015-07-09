/**
 * 
 */
package com.concur.mobile.platform.password.reset.test;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.ResetUserPasswordRequestTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;

/**
 * An extension of <code>AsyncRequestTask</code> to test the platform <code>ResetUserPasswordRequestTask</code> class.
 * 
 * @author andrewk
 */
public class ResetUserPasswordRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "ResetUserPasswordRequestTaskTest";

    private static final boolean DEBUG = false;

    /**
     * Contains the email.
     */
    protected String email;

    /**
     * Contains the "key part A".
     */
    protected String keyPartA;

    /**
     * Contains the "key part B".
     */
    protected String keyPartB;

    /**
     * Contains the password.
     */
    protected String password;

    /**
     * Sets the password reset credentials.
     * 
     * @param email
     *            contains the email.
     * @param locale
     *            contains the locale.
     */
    public void setCredentials(String email, String keyPartA, String keyPartB, String password) {
        this.email = email;
        this.keyPartA = keyPartA;
        this.keyPartB = keyPartB;
        this.password = password;
    }

    /**
     * Will perform the test throwing an exception if the test fails.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    public void doTest() throws Exception {

        Context context = PlatformTestApplication.getApplication();

        // Set the mock response if the mock server is being used.
        if (PlatformTestApplication.useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "authentication/ResetUserPasswordResponse.xml");
        }

        // Initiate the request.
        BaseAsyncResultReceiver resetPasswordReplyReceiver = new BaseAsyncResultReceiver(getHander());
        resetPasswordReplyReceiver.setListener(new AsyncReplyListenerImpl());
        ResetUserPasswordRequestTask reqTask = new ResetUserPasswordRequestTask(context, 1, resetPasswordReplyReceiver,
                email, keyPartA, keyPartB, password);

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
            ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".doTest: interrupted while acquiring result.");
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

                // Use SimpleXML framework to deserialize the response.
                Serializer serializer = new Persister();
                ResetUserPasswordResponse resetUserPasswordResponse = serializer.read(ResetUserPasswordResponse.class,
                        response, false);

                // Perform the verification.
                VerifyResetUserPasswordResponse verifyResetUserPasswordResponse = new VerifyResetUserPasswordResponse();
                verifyResetUserPasswordResponse.verify(result.resultData, resetUserPasswordResponse);

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }

        }
    }

}
