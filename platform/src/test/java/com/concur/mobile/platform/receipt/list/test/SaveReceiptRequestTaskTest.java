package com.concur.mobile.platform.receipt.list.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.util.IOUtils;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.list.Receipt;
import com.concur.mobile.platform.expense.receipt.list.SaveReceiptRequestTask;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptListDAO;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.google.gson.Gson;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>SaveReceiptRequestTask</code> platform
 * request.
 * 
 * @author andrewk
 */
public class SaveReceiptRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "SaveReceiptRequestTaskTest";

    private static final boolean DEBUG = false;

    public static enum ReceiptSource {
        SOURCE_URI, SOURCE_INPUT_STREAM, SOURCE_BYTE_ARRAY
    };

    private ReceiptSource source;

    /**
     * Will construct an instance of <code>SaveReceiptRequestTaskTest</code> with a test type.
     * 
     * @param source
     *            contains the type of test to run.
     */
    public SaveReceiptRequestTaskTest(ReceiptSource source) {
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
            Map<String, String> responseHeaders = new HashMap<String, String>();
            // Set the content-type.
            responseHeaders.put("Content-Type", "application/json");
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "receipt/SaveReceiptResponse.json", responseHeaders);
        }

        // Initiate the save receipt request.
        BaseAsyncResultReceiver saveReceiptReplyReceiver = new BaseAsyncResultReceiver(getHander());
        saveReceiptReplyReceiver.setListener(new AsyncReplyListenerImpl());
        SaveReceiptRequestTask reqTask = null;
        SaveReceiptUploadListener uploadListener = new SaveReceiptUploadListener();

        // Set up the input stream for the image.
        AssetManager assetMngr = context.getAssets();
        InputStream is = assetMngr.open("receipt/IMG_20140731_142657.jpg");

        switch (source) {
        case SOURCE_BYTE_ARRAY: {
            ByteArrayOutputStream outStr = IOUtils.reusableOutputStream(is);
            reqTask = new SaveReceiptRequestTask(context, 1, saveReceiptReplyReceiver, null, outStr.toByteArray(),
                    "image/jpeg", uploadListener);
            reqTask.setRetainResponse(true);
            break;
        }
        case SOURCE_INPUT_STREAM: {
            ByteArrayOutputStream outStr = IOUtils.reusableOutputStream(is);
            byte[] outStrBytes = outStr.toByteArray();
            long contentLength = outStrBytes.length;
            reqTask = new SaveReceiptRequestTask(context, 1, saveReceiptReplyReceiver, null, new ByteArrayInputStream(
                    outStrBytes), contentLength, "image/jpeg", uploadListener);
            reqTask.setRetainResponse(true);
            break;
        }
        case SOURCE_URI: {
            // Create a new ReceiptDAO object and write the receipt data to it.
            SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
            ReceiptListDAO recListDAO = new ReceiptListDAO(context, sessInfo.getUserId());
            ReceiptDAO recDAO = recListDAO.createReceipt();
            recDAO.setContentType("image/jpeg");
            Assert.assertTrue("unable to update receipt!", recDAO.update());
            Assert.assertTrue("unable to write receipt data to receipt!", writeReceiptData(context, recDAO, is));
            reqTask = new SaveReceiptRequestTask(context, 1, saveReceiptReplyReceiver, recDAO.getContentUri(),
                    uploadListener);
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

                // Grab the response.
                String response = getResponseString(reqTask);
                Assert.assertNotNull("request response is null", response);

                // Use GSON framework to deserialize the response.
                // Parse the receipt object.
                Gson gson = new Gson();
                Receipt receipt = gson.fromJson(response, Receipt.class);

                String recUriStr = result.resultData.getString(SaveReceiptRequestTask.RECEIPT_URI_KEY);
                Assert.assertNotNull("receipt URI is null!", recUriStr);
                Uri receiptUri = Uri.parse(recUriStr);

                VerifySaveReceiptResponse verify = new VerifySaveReceiptResponse();
                verify.verify(context, receiptUri, receipt);

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }

        }

    }

    /**
     * Will write the receipt data in <code>is</code> to <code>recDAO</code>.
     * 
     * @param recDAO
     *            contains the receipt DAO object.
     * @param is
     *            contains the input stream.
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    private boolean writeReceiptData(Context context, ReceiptDAO recDAO, InputStream is) {
        boolean retVal = true;

        Uri contentUri = recDAO.getContentUri();
        if (contentUri != null) {
            OutputStream rcptOut = null;
            try {
                ContentResolver resolver = context.getContentResolver();
                rcptOut = resolver.openOutputStream(contentUri);
                InputStream in = new BufferedInputStream(is);
                byte[] buf = new byte[(16 * 1024)];
                int bytesRead = -1;
                while ((bytesRead = in.read(buf)) != -1) {
                    // Write out to receipt output stream.
                    if (rcptOut != null) {
                        rcptOut.write(buf, 0, bytesRead);
                    }
                }
                // Flush the output stream.
                if (rcptOut != null) {
                    rcptOut.flush();
                }
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".writeReceiptData: I/O exception writing to the receipt uri '"
                        + contentUri.toString() + "'", ioExc);
                retVal = false;
            } finally {
                if (rcptOut != null) {
                    try {
                        rcptOut.close();
                    } catch (IOException ioExc) {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG + ".writeReceiptData: I/O exception closing receipt output stream '"
                                        + contentUri.toString() + "'", ioExc);
                    } finally {
                        rcptOut = null;
                    }
                }
            }
        } else {
            retVal = false;
        }
        return retVal;
    }

    /**
     * An implementation of <code>GetReceiptRequestTask.GetReceiptDownloadListener</code> to report receipt download progress.
     * 
     * @author andrewk
     */
    class SaveReceiptUploadListener implements SaveReceiptRequestTask.SaveReceiptUploadListener {

        private static final String CLS_TAG = SaveReceiptRequestTaskTest.CLS_TAG + "." + "SaveReceiptUploadListener";

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
        public void onUpload(int count) {
            currentLength += count;
            Log.d(Const.LOG_TAG, CLS_TAG + ".onUpload: " + Long.toString(currentLength) + "/" + contentLengthStr);
        }

        @Override
        public void onComplete() {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onComplete: " + Long.toString(currentLength) + "/" + contentLengthStr);
        }

    }

}
