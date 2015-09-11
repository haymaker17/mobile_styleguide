/**
 * 
 */
package com.concur.mobile.platform.receipt.list.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.robolectric.shadows.ShadowLog;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.util.IOUtils;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.list.GetReceiptRequestTask;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptListDAO;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>GetReceiptRequestTask</code>.
 * 
 * @author andrewk
 */
public class GetReceiptRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "GetReceiptRequestTaskTest";

    private static final boolean DEBUG = false;

    public enum ReceiptIdSource {
        SOURCE_URI, SOURCE_ID
    }

    private ReceiptIdSource idSource;

    public GetReceiptRequestTaskTest(boolean useMockServer) {
        super(useMockServer);
    }

    /**
     * Will construct an instance of <code>GetReceiptRequestTaskTest</code> with a test type.
     * 
     * @param idSource
     *            contains the source of the receipt id.
     */
    public void setReceiptIdSource(ReceiptIdSource idSource) {
        this.idSource = idSource;
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
            responseHeaders.put("Content-Type", "image/jpeg");
            // Set the ETag value.
            responseHeaders.put("ETag", "Kfgi459hJTdke4dsED==");

            // Set the content-length.
            AssetManager assetMngr = context.getAssets();
            InputStream is = assetMngr.open("receipt/IMG_20140731_142657.jpg");
            ByteArrayOutputStream outStr = IOUtils.reusableOutputStream(is);
            byte[] outStrBytes = outStr.toByteArray();
            long contentLength = outStrBytes.length;
            responseHeaders.put("Content-Length", Long.toString(contentLength));

            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "receipt/IMG_20140731_142657.jpg", responseHeaders);
        }

        // Initiate the get receipt request.
        BaseAsyncResultReceiver receiptReplyReceiver = new BaseAsyncResultReceiver(getHander());
        receiptReplyReceiver.setListener(new AsyncReplyListenerImpl());

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        ReceiptListDAO recListDAO = new ReceiptListDAO(context, sessInfo.getUserId());
        ReceiptDAO recDAO = recListDAO.getReceipts().get(0);

        GetReceiptRequestTask reqTask = null;
        GetReceiptDownloadListener downloadListener = new GetReceiptDownloadListener();
        switch (idSource) {
        case SOURCE_ID: {
            reqTask = new GetReceiptRequestTask(context, 1, receiptReplyReceiver, null, recDAO.getId(),
                    downloadListener);
            reqTask.setRetainResponse(true);
            break;
        }
        case SOURCE_URI: {
            reqTask = new GetReceiptRequestTask(context, 1, receiptReplyReceiver, null, recDAO.getContentUri()
                    .toString(), downloadListener);
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

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }

        }

    }

    /**
     * An implementation of <code>GetReceiptRequestTask.GetReceiptDownloadListener</code> to report receipt download progress.
     * 
     * @author andrewk
     */
    class GetReceiptDownloadListener implements GetReceiptRequestTask.GetReceiptDownloadListener {

        private static final String CLS_TAG = GetReceiptRequestTaskTest.CLS_TAG + "." + "GetReceiptDownloadListener";

        private long contentLength;

        private String contentLengthStr;

        private long currentLength;

        @Override
        public void onStart(long contentLength) {
            this.contentLength = contentLength;
            this.contentLengthStr = Long.toString(this.contentLength);
            this.currentLength = 0;
            Log.d(Const.LOG_TAG, CLS_TAG + ".onStart: " + Long.toString(currentLength) + "/" + contentLengthStr);
        }

        @Override
        public void onDownload(int count) {
            currentLength += count;
            Log.d(Const.LOG_TAG, CLS_TAG + ".onDownload: " + Long.toString(currentLength) + "/" + contentLengthStr);
        }

        @Override
        public void onComplete() {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onComplete: " + Long.toString(currentLength) + "/" + contentLengthStr);
        }

    }

}
