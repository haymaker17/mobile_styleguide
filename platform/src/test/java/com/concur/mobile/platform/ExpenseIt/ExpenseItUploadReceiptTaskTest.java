/**
 *
 */
package com.concur.mobile.platform.ExpenseIt;

import android.content.Context;
import android.content.res.AssetManager;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.util.IOUtils;
import com.concur.mobile.platform.ExpenseIt.test.VerifyExpenseItUploadReceiptResult;
import com.concur.mobile.platform.expenseit.ExpenseItImage;
import com.concur.mobile.platform.expenseit.ExpenseItPostReceiptResponse;
import com.concur.mobile.platform.expenseit.PostExpenseItReceiptAsyncTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>PPLoginLightRequestTask</code> platform
 * request.
 *
 * @author andrewk
 */
public class ExpenseItUploadReceiptTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = ExpenseItUploadReceiptTaskTest.class.getSimpleName();

    private static final boolean DEBUG = false;

    public ExpenseItUploadReceiptTaskTest(boolean useMockServer) {
        super(useMockServer);
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
            setMockResponse(mockServer, HttpStatus.SC_OK, "receipt/ExpenseItUploadImageResponse.json", responseHeaders);
        }

        // Initiate the login request.
        BaseAsyncResultReceiver uploadReceiptReceiver = new BaseAsyncResultReceiver(getHander());
        uploadReceiptReceiver.setListener(new AsyncReplyListenerImpl());
        //Make the call
        PostExpenseItReceiptAsyncTask reqTask = new PostExpenseItReceiptAsyncTask(context,
            0, uploadReceiptReceiver, getImage(context));

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
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

                    ExpenseItPostReceiptResponse receiptResponse = gson.fromJson(response, ExpenseItPostReceiptResponse.class);
                    VerifyExpenseItUploadReceiptResult verifier = new VerifyExpenseItUploadReceiptResult();
                    verifier.verify(context, receiptResponse);

                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                    }
                    break;
                }
            }

        }

    }

    protected ExpenseItImage getImage(Context context) throws IOException {

        String filePath = "receipt/IMG_20140731_142657.jpg";

        // Set the content-length.
        AssetManager assetMngr = context.getAssets();
        InputStream is = assetMngr.open(filePath);
        ByteArrayOutputStream outStr = IOUtils.reusableOutputStream(is);

        //Setup the image information
        ExpenseItImage image = new ExpenseItImage();
        String contentType = contentType = "image/png";

        //Compress the bitmap
        image.setData(outStr.toByteArray(), contentType);
        return image;
    }

}
