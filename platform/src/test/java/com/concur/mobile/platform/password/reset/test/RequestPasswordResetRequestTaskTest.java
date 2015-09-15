/**
 * 
 */
package com.concur.mobile.platform.password.reset.test;

import java.util.Locale;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.text.TextUtils;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.RequestPasswordResetRequestTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;

/**
 * An extension of <code>AsyncRequestTask</code> for the purpose of testing the platform request to perform a password reset.
 * 
 * @author andrewk
 */
public class RequestPasswordResetRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "RequestPasswordResetRequestTaskTest";

    private static final boolean DEBUG = false;

    protected String email;

    protected Locale locale;

    public RequestPasswordResetRequestTaskTest(boolean useMockServer) {
        super(useMockServer);
    }

    /**
     * Sets the password reset credentials.
     * 
     * @param email
     *            contains the email.
     * @param locale
     *            contains the locale.
     */
    public void setCredentials(String email, Locale locale) {
        this.email = email;
        this.locale = locale;
    }

    /**
     * Will perform the test throwing an exception if the test fails.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    @Override
    public void doTest() throws Exception {

        Context context = PlatformTestApplication.getApplication();

        // Set the mock response if the mock server is being used.
        if (useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "authentication/RequestPasswordResetResponse.xml");
        }

        // Initiate the request.
        BaseAsyncResultReceiver resetPasswordReplyReceiver = new BaseAsyncResultReceiver(getHander());
        resetPasswordReplyReceiver.setListener(new AsyncReplyListenerImpl());
        RequestPasswordResetRequestTask reqTask = new RequestPasswordResetRequestTask(context, 1,
                resetPasswordReplyReceiver, locale, email);
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
            ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".doTest: interrupted while acquiring email look-up result.");
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
                RequestPasswordResetResponse resetPasswordResponse = serializer.read(
                        RequestPasswordResetResponse.class, response, false);

                // Perform the verification.
                VerifyRequestPasswordResetResponse verifyResetPasswordResponse = new VerifyRequestPasswordResetResponse();
                verifyResetPasswordResponse.verify(result.resultData, resetPasswordResponse);

                String keyPartA = result.resultData.getString(RequestPasswordResetRequestTask.EXTRA_KEY_PART_A_KEY);
                if (!TextUtils.isEmpty(keyPartA)) {
                    System.out.println(CLS_TAG + ": KEY_PART_A = '" + keyPartA + "'.");
                }
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }

        }
    }

}
