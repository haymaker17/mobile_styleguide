/**
 * 
 */
package com.concur.mobile.platform.expense.smartexpense.list.test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.expense.smartexpense.SmartExpenseList;
import com.concur.mobile.platform.expense.smartexpense.SmartExpenseListRequestTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.concur.mobile.platform.util.BooleanDeserializer;
import com.concur.mobile.platform.util.CalendarDeserializer;
import com.concur.mobile.platform.util.DoubleDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of unit testing the <code>GetSmartExpenseListRequestTask</code>
 * within the platform code.
 * 
 * @author andrewk
 */
public class SmartExpenseListRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "GetSmartExpenseListRequestTaskTest";

    private static final boolean DEBUG = false;

    public SmartExpenseListRequestTaskTest(boolean useMockServer) {
        super(useMockServer);
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
            Map<String, String> responseHeaders = new HashMap<String, String>();

            // Set the content-type.
            responseHeaders.put("Content-Type", "application/json");
            // Set the ETag value.
            // TODO - responseHeaders.put("ETag", "Kfgi459hJTdke4dsED==");

            // Set the response on the server.
            setMockResponse(mockServer, HttpStatus.SC_OK, "expense/SmartExpenseListResponse.json", responseHeaders);
        }

        // Initiate the receipt list request.
        BaseAsyncResultReceiver receiptListReplyReceiver = new BaseAsyncResultReceiver(getHander());
        receiptListReplyReceiver.setListener(new AsyncReplyListenerImpl());
        SmartExpenseListRequestTask reqTask = new SmartExpenseListRequestTask(context, 1, receiptListReplyReceiver);
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

                // Use GSON framework to deserialize the response.

                // Build the parser with type deserializers.
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Calendar.class, new CalendarDeserializer());
                builder.registerTypeAdapter(Boolean.class, new BooleanDeserializer());
                builder.registerTypeAdapter(Double.class, new DoubleDeserializer());
                Gson gson = builder.create();

                SmartExpenseList smartExpListResp = gson.fromJson(response, SmartExpenseList.class);
                VerifySmartExpenseListResponse verifier = new VerifySmartExpenseListResponse();
                verifier.verify(context, smartExpListResp);

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }

        }

    }
}
