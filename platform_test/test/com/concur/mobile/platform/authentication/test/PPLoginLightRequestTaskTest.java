/**
 * 
 */
package com.concur.mobile.platform.authentication.test;

import java.util.Locale;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.PPLoginLightRequestTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>PPLoginLightRequestTask</code> platform
 * request.
 * 
 * @author andrewk
 */
public class PPLoginLightRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "PPLoginLightRequestTaskTest";

    private static final boolean DEBUG = false;

    /**
     * Contains the request login id.
     */
    private String ppLoginId;

    /**
     * Contains the request pin/password.
     */
    private String ppLoginPinPassword;

    /**
     * Sets the authentication credentials.
     * 
     * @param ppLoginId
     *            contains the login id.
     * @param ppLoginPinPassword
     *            contains the pin/password.
     */
    public void setCredentials(String ppLoginId, String ppLoginPinPassword) {
        this.ppLoginId = ppLoginId;
        this.ppLoginPinPassword = ppLoginPinPassword;
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
            setMockResponse(mockServer, HttpStatus.SC_OK, "authentication/PPLoginLightResponse.xml");
        }

        // Initiate the login request.
        BaseAsyncResultReceiver loginLightReplyReceiver = new BaseAsyncResultReceiver(getHander());
        loginLightReplyReceiver.setListener(new AsyncReplyListenerImpl());
        Locale locale = context.getResources().getConfiguration().locale;
        PPLoginLightRequestTask reqTask = new PPLoginLightRequestTask(context, loginLightReplyReceiver, 1, locale,
                ppLoginId, ppLoginPinPassword);

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

                // Use SimpleXML framework to deserialize the response.
                Serializer serializer = new Persister();
                PPLoginLightResponse loginLightResponse = serializer.read(PPLoginLightResponse.class, response, false);
                LoginResult loginResult = loginLightResponse.loginResult;

                // The PPLoginLightRequestTask will set to 'null' the session ID within the content
                // provider so we'll set it to 'null' within the parsed object in order to pass verification.
                loginResult.session.id = null;

                // Perform the verification ignoring session ID.
                VerifyLoginResult verifyLoginResult = new VerifyLoginResult();
                verifyLoginResult.verifySessionInfo(context, loginResult);
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }

        }

    }

}
