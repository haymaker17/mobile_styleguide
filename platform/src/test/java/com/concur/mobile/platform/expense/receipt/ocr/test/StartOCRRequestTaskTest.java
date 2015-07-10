/**
 * 
 */
package com.concur.mobile.platform.expense.receipt.ocr.test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptListDAO;
import com.concur.mobile.platform.expense.receipt.ocr.StartOCR;
import com.concur.mobile.platform.expense.receipt.ocr.StartOCRRequestTask;
import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of unit testing the <code>StartOCRRequestTask</code> within the
 * platform code.
 * 
 * @author Chris N. Diaz
 */
public class StartOCRRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = StartOCRRequestTaskTest.class.getName();

    private static final boolean DEBUG = false;

    /**
     * Will perform the SUCCESS test throwing an exception if the test fails.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    public void doTestSuccess() throws Exception {
        doTest(true);
    }

    /**
     * Will perform the FAILURE test throwing an exception if the test fails.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    public void doTestFailure() throws Exception {
        doTest(false);
    }

    private void doTest(boolean testSuccess) throws Exception {
        Context context = PlatformTestApplication.getApplication();

        // Set the mock response if the mock server is being used.
        if (PlatformTestApplication.useMockServer()) {
            // Set the mock response for the test.
            Map<String, String> responseHeaders = new HashMap<String, String>();

            // Set the content-type.
            responseHeaders.put("Content-Type", "application/json");

            // Set the response on the server.
            if (testSuccess) {
                setMockResponse(mockServer, HttpStatus.SC_OK, "receipt/StartOCRSuccessResponse.json", responseHeaders);
            } else {
                setMockResponse(mockServer, HttpStatus.SC_OK, "receipt/StartOCRFailureResponse.json", responseHeaders);
            }
        } else {

        }

        // If we're testing for failure, then we use a non-existing
        // receipt image ID, which will then return an error response.
        String receiptImageId = "xxxMyFakeReceiptImageIdxxx";

        // If we're testing for success, we need to use a real receipt image ID.
        if (testSuccess) {
            // Grab the first ReceiptDAO object and write the receipt data to it.
            SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
            ReceiptListDAO recListDAO = new ReceiptListDAO(context, sessInfo.getUserId());
            List<ReceiptDAO> receipts = recListDAO.getReceipts();
            int receiptDAOCount = (receipts != null) ? receipts.size() : 0;

            Assert.assertTrue("receiptDAOCount must be > 0", (receiptDAOCount > 0));

            receiptImageId = receipts.get(0).getId();

            Assert.assertNotNull("Receipt Image ID is null!", receiptImageId);

        } else {
            expectedResultCode = BaseAsyncRequestTask.RESULT_ERROR;
        }

        // Initiate the StartOCR request.
        BaseAsyncResultReceiver startOcrReplyReceiver = new BaseAsyncResultReceiver(getHander());
        startOcrReplyReceiver.setListener(new AsyncReplyListenerImpl());
        StartOCRRequestTask reqTask = new StartOCRRequestTask(context, 1, startOcrReplyReceiver, receiptImageId);
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
            }
            case BaseAsyncRequestTask.RESULT_OK: {

                // Verify the result.

                // Grab the response.
                String response = getResponseString(reqTask);
                Assert.assertNotNull("request response is null", response);

                // Build the parser with type deserializers.
                Gson gson = new GsonBuilder().create();

                // prepare the object Type expected in MWS response 'data' element
                Type type = new TypeToken<MWSResponse<StartOCR>>() {}.getType();
                MWSResponse<StartOCR> mwsResp = gson.fromJson(response, type);
                StartOCR startOcr = null;

                if (mwsResp != null) {
                    if (mwsResp.getData() != null) {
                        startOcr = mwsResp.getData();
                        startOcr.errors = mwsResp.getErrors();
                    }
                } else {

                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".parseStream: MWSResponse was null!");
                    }
                }

                VerifyStartOCRResponse verifier = new VerifyStartOCRResponse();

                if (testSuccess) {
                    verifier.verifySuccess(context, startOcr);
                } else {
                    verifier.verifyFailure(context, startOcr);
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
