/**
 * Copyright (c) 2014 Concur Technologies, Inc.
 */
package com.concur.mobile.platform.authentication.test;

import java.util.Locale;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.PPLoginRequestTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.concur.mobile.platform.util.Parse;

/**
 * Unit Test for the <code>com.concur.mobile.platform.authentication.PPLoginRequestTask</code>.
 * 
 * @author Andy Kispert
 */
public class PPLoginRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "PPLoginRequestTaskTest";

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
            setMockResponse(mockServer, HttpStatus.SC_OK, "authentication/PPLoginResponse.xml");
        }

        // Initiate the login request.
        BaseAsyncResultReceiver loginReplyReceiver = new BaseAsyncResultReceiver(getHander());
        loginReplyReceiver.setListener(new AsyncReplyListenerImpl());
        Locale locale = context.getResources().getConfiguration().locale;
        PPLoginRequestTask reqTask = new PPLoginRequestTask(context, loginReplyReceiver, 1, locale, ppLoginId,
                ppLoginPinPassword);
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

                // Use SimpleXML framework to deserialize the response.
                Serializer serializer = new Persister();
                LoginResult loginResult = serializer.read(LoginResult.class, response, false);

                // Convert any pin expiration date string into a Calendar object in the same was
                // the parsing code does in ConcurPlatform.
                if (!TextUtils.isEmpty(loginResult.pinExpirationDateStr)) {
                    loginResult.pinExpirationDate = Parse.parseXMLTimestamp(loginResult.pinExpirationDateStr.trim());
                }

                // Perform the verification.
                VerifyLoginResult verifyLoginResult = new VerifyLoginResult();
                verifyLoginResult.verify(context, loginResult);
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }

        }

    }

}
