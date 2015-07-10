/**
 * 
 */
package com.concur.mobile.platform.receipt.list.test;

import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;

import android.content.Context;
import android.net.Uri;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.list.DeleteReceiptRequestTask;
import com.concur.mobile.platform.expense.receipt.list.SaveReceiptRequestTask;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptListDAO;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>DeleteReceiptRequestTask</code> platform
 * request.
 * 
 * @author andrewk
 */
public class DeleteReceiptRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "DeleteReceiptRequestTaskTest";

    private static final boolean DEBUG = false;

    public static enum ReceiptSource {
        SOURCE_URI, SOURCE_RECEIPT_IMAGE_ID
    };

    private ReceiptSource source;

    /**
     * Will construct an instance of <code>DeleteReceiptRequestTaskTest</code> with a test type.
     * 
     * @param source
     *            contains the type of test to run.
     */
    public DeleteReceiptRequestTaskTest(ReceiptSource source) {
        this.source = source;
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
            setMockResponse(mockServer, HttpStatus.SC_OK, null);
        }

        // Initiate the save receipt request.
        BaseAsyncResultReceiver deleteReceiptReplyReceiver = new BaseAsyncResultReceiver(getHander());
        deleteReceiptReplyReceiver.setListener(new AsyncReplyListenerImpl());
        DeleteReceiptRequestTask reqTask = null;

        // Grab the first ReceiptDAO object and write the receipt data to it.
        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        ReceiptListDAO recListDAO = new ReceiptListDAO(context, sessInfo.getUserId());
        List<ReceiptDAO> receipts = recListDAO.getReceipts();
        int receiptDAOCount = (receipts != null) ? receipts.size() : 0;

        Assert.assertTrue("receiptDAOCount must be > 0", (receiptDAOCount > 0));

        switch (source) {
        case SOURCE_URI: {
            ReceiptDAO recDAO = receipts.get(0);
            reqTask = new DeleteReceiptRequestTask(context, 1, deleteReceiptReplyReceiver, recDAO.getContentUri(), null);
            reqTask.setRetainResponse(true);
            break;
        }
        case SOURCE_RECEIPT_IMAGE_ID: {
            ReceiptDAO recDAO = receipts.get(0);
            reqTask = new DeleteReceiptRequestTask(context, 1, deleteReceiptReplyReceiver, null, recDAO.getId());
            reqTask.setRetainResponse(true);
            break;
        }
        }

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

                String recUriStr = result.resultData.getString(SaveReceiptRequestTask.RECEIPT_URI_KEY);
                Assert.assertNotNull("receipt URI is null!", recUriStr);
                Uri receiptUri = Uri.parse(recUriStr);

                VerifyDeleteReceiptResponse verify = new VerifyDeleteReceiptResponse();
                verify.verify(context, receiptUri, receiptDAOCount);

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }

        }

    }

}
