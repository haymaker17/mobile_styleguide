package com.concur.mobile.platform.config.user.test;

import java.util.Arrays;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.text.TextUtils;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.config.user.UserConfigRequestTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;

/**
 * Unit Test for the <code>com.concur.mobile.platform.config.user.UserConfigRequestTask</code>.
 * 
 * @author Yiwen Wu
 */
public class UserConfigRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "UserConfigRequestTaskTest";

    private static final boolean DEBUG = false;

    /**
     * Contains the user config hash.
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
            setMockResponse(mockServer, HttpStatus.SC_OK, "config/UserConfigV2.xml");
        }

        // Initiate the login request.
        BaseAsyncResultReceiver userConfigReplyReceiver = new BaseAsyncResultReceiver(getHander());
        userConfigReplyReceiver.setListener(new AsyncReplyListenerImpl());
        UserConfigRequestTask reqTask = new UserConfigRequestTask(context, 1, userConfigReplyReceiver, hash);

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
                UserConfigV2Result userConfigV2Result = serializer.read(UserConfigV2Result.class, response, false);

                // Convert any pin expiration date string into a Calendar object in the same was
                // the parsing code does in ConcurPlatform.
                if (!TextUtils.isEmpty(userConfigV2Result.userConfig.allowedAirClassesOfService)) {
                    userConfigV2Result.userConfig.allowedAirClassServiceList = Arrays
                            .asList(userConfigV2Result.userConfig.allowedAirClassesOfService.split(" "));
                }

                // Perform the verification.
                VerifyUserConfigResult verifyUserConfigResult = new VerifyUserConfigResult();
                verifyUserConfigResult.verify(context, userConfigV2Result.userConfig);
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }

        }

    }
}
