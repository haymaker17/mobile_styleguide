/**
 * 
 */
package com.concur.mobile.platform.authentication.system.config.test;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.config.system.SystemConfigRequestTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>SystemConfigRequestTaskTest</code> platform
 * request.
 * 
 * @author sunill
 */
public class SystemConfigRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "SystemConfigRequestTaskTest";

    private static final boolean DEBUG = false;

    /**
     * Contains the system config hash.
     */
    private String hash = null;

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
            setMockResponse(mockServer, HttpStatus.SC_OK, "config/SystemConfig.xml");
        }

        BaseAsyncResultReceiver sysConfigReceiver = new BaseAsyncResultReceiver(getHander());
        sysConfigReceiver.setListener(new AsyncReplyListenerImpl());
        SystemConfigRequestTask reqTask = new SystemConfigRequestTask(context, 1, sysConfigReceiver, hash);
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
            ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".doTest: interrupted while getting system config request.");
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

                // Use SimpleXML framework to deserialize the response.
                String response = getResponseString(reqTask);
                Assert.assertNotNull("request response is null", response);
                Serializer serializer = new Persister();
                SystemConfigResult sysConfigResult = serializer.read(SystemConfigResult.class, response, false);

                VerifySystemConfigResult verifyLoginResult = new VerifySystemConfigResult();
                verifyLoginResult.verify(context, sysConfigResult);

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }

        }

    }

}
