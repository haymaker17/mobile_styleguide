/**
 * 
 */
package com.concur.mobile.platform.emaillookup.test;

import java.util.Locale;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;

/**
 * Provides an extension of <code>AsyncRequestTask</code> for the purpose of testing the <code>EmailLookUpRequestTask</code>
 * request.
 * 
 * @see EmailLookUpRequestTask
 * 
 * @author andrewk
 */
public class EmailLookUpRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "EmailLookUpRequestTaskTest";

    private static final boolean DEBUG = false;

    private String email;

    private Locale locale;

    private EmailLookUpRequestTask reqTask;

    /**
     * Sets the look-up credentials.
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
    public void doTest() throws Exception {

        Context context = PlatformTestApplication.getApplication();

        // Set the mock response if the mock server is being used.
        if (PlatformTestApplication.useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "authentication/EmailLookUpResponse.xml");
        }

        // Initiate the login request.
        BaseAsyncResultReceiver emailLookUpReplyReceiver = new BaseAsyncResultReceiver(getHander());
        emailLookUpReplyReceiver.setListener(new AsyncReplyListenerImpl());
        reqTask = new EmailLookUpRequestTask(context, 1, emailLookUpReplyReceiver, locale, email);

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
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }
        }
    }

    /**
     * Will verify that the email look-up response information has been parsed correctly and can be stored successfully in the
     * current session object.
     * 
     * @throws Exception
     *             throws an exception if verification fails.
     */
    public void verifyEmailLookUpResponse() throws Exception {

        // At the moment, only verification of OK results are being performed.
        if (result != null && result.resultCode == BaseAsyncRequestTask.RESULT_OK) {

            // Grab the response.
            String response = getResponseString(reqTask);
            Assert.assertNotNull("request response is null", response);

            // Use SimpleXML framework to deserialize the response.
            Serializer serializer = new Persister();
            EmailLookUpResponse emailLookUpResponse = serializer.read(EmailLookUpResponse.class, response, false);

            // Perform the verification.
            VerifyEmailLookUpResponse verifyEmailLookUpResponse = new VerifyEmailLookUpResponse();
            verifyEmailLookUpResponse.verify(result.resultData, emailLookUpResponse);
        }
    }

}
