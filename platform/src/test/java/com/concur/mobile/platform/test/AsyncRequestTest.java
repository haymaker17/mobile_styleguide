/**
 * 
 */
package com.concur.mobile.platform.test;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.platform.service.ExpenseItAsyncRequestTask;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.test.server.MockServer;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Provides an abstract class that can be extended to execute and wait for the response from an async request task.
 * 
 * @author andrewk
 */
public abstract class AsyncRequestTest {

    private static final String CLS_TAG = "AsyncRequestTask";

    private static final boolean DEBUG = Boolean.FALSE;

    /**
     * Contains a reference to the async request result.
     */
    protected AsyncRequestResult result;

    /**
     * Contains a reference to a <code>HandlerThread</code> used to run a looper in order to receive the result of an asynchronous
     * request.
     */
    protected HandlerThread handlerThread;

    /**
     * Contains a reference to a mock MWS server.
     */
    protected MockServer mockServer;

    /**
     * Contains the expected result code based on the design of the test. Defaults to <code>BaseAsyncRequestTask.RESULT_OK</code>.
     */
    protected int expectedResultCode = BaseAsyncRequestTask.RESULT_OK;

    public abstract void doTest() throws Exception;

    private final boolean useMockServer;

    public AsyncRequestTest(boolean useMockServer) {
        this.useMockServer =  useMockServer;
    }

    public boolean useMockServer() {
        return useMockServer;
    }

    /**
     * Get the stream bytes
     * @param is
     * @return
     * @throws IOException
     */
    private static byte[] getBytes(InputStream is) throws IOException {

        int len;
        int size = 1024;
        byte[] buf;

        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1)
                bos.write(buf, 0, len);
            buf = bos.toByteArray();
        }
        return buf;
    }

    /**
     * Sets the expected result code for the test. <br>
     * <br>
     * <b>NOTE:</b> Upon test completion, if <code>expectedResultCode</code> does not match the test result code; the test will
     * report a failure.
     * 
     * @param expectedResultCode
     *            contains one of <code>BaseAsyncRequestTask.RESULT_OK</code>, <code>BaseAsyncRequestTask.RESULT_CANCEL</code> or
     *            <code>BaseAsyncRequestTask.RESULT_ERROR</code>.
     * @see BaseAsyncRequestTask
     */
    public void setExpectedResultCode(int expectedResultCode) {

        try {
            Assert.assertTrue(
                    CLS_TAG + ".setExpectedResultCode: invalid value '" + expectedResultCode
                            + "' for expected result code.",
                    (expectedResultCode == BaseAsyncRequestTask.RESULT_CANCEL
                            || expectedResultCode == BaseAsyncRequestTask.RESULT_ERROR || expectedResultCode == BaseAsyncRequestTask.RESULT_OK));
        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setExpectedResultCode: " + afe.getMessage());
            throw afe;
        }

        this.expectedResultCode = expectedResultCode;
    }

    /**
     * Gets the expected result code for the test.
     * 
     * @return returns the expected result code for the test.
     */
    public int getExpectedResult() {
        return expectedResultCode;
    }

    /**
     * Gets the response from the server.
     * 
     * @return returns the response from the server.
     */
    public String getResponseString(PlatformAsyncRequestTask reqTask) {
        String response = null;

        InputStream ins = reqTask.getResponse();
        if (ins != null) {
            try {
                response = IOUtils.toString(reqTask.getResponse(), "UTF-8");
            } catch (UnsupportedEncodingException unsEncExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getResponseString: " + unsEncExc);
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getResponseString: " + ioExc);
            } catch (UnsupportedCharsetException unsCharSetExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getResponseString: " + unsCharSetExc);
            }
        }
        return response;
    }

    /**
     * Gets the response from the server.
     *
     * @return returns the response from the server.
     */
    public String getResponseString(ExpenseItAsyncRequestTask reqTask) {
        String response = null;

        InputStream ins = reqTask.getResponse();
        if (ins != null) {
            try {
                response = IOUtils.toString(reqTask.getResponse(), "UTF-8");
            } catch (UnsupportedEncodingException unsEncExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getResponseString: " + unsEncExc);
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getResponseString: " + ioExc);
            } catch (UnsupportedCharsetException unsCharSetExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getResponseString: " + unsCharSetExc);
            }
        }
        return response;
    }

    /**
     * Will verify the value of <code>expectedResultCode</code> with a value stored in <code>result.resultCode</code>. <br>
     */
    protected void verifyExpectedResultCode(String message) {
        Assert.assertNotNull(CLS_TAG + ".verifyExpectedResult: " + message + " - result is null.", result);
        Assert.assertEquals(CLS_TAG + ".verifyExpectedResult: " + message, expectedResultCode, result.resultCode);
    }

    public MockServer getMockServer() {
        return mockServer;
    }

    /**
     * Sets the instance of <code>MockServer</code> associated with this test.
     * 
     * @param mockServer
     *            contains a reference
     */
    public void setMockServer(MockServer mockServer) {
        this.mockServer = mockServer;
    }

    /**
     * Will set the mock response on <code>server</code> to the contents of <code>mockRespFile</code> located in
     * <code>assets</code>.
     * 
     * @param server
     *            contains the mock MWS server reference.
     * @param httpStatusCode
     *            contains the HTTP response status code.
     * @param mockRespFile
     *            contains the file path relative to the <code>assets</code> application path of the response data.
     */
    public void setMockResponse(MockServer server, int httpStatusCode, String mockRespFile) throws Exception {
        setMockResponse(server, httpStatusCode, mockRespFile, null);
    }

    /**
     * Will set the mock response on <code>server</code> to the contents of <code>mockRespFile</code> located in
     * <code>assets</code>.
     * 
     * @param server
     *            contains the mock MWS server reference.
     * @param httpStatusCode
     *            contains the HTTP response status code.
     * @param mockRespFile
     *            contains the file path relative to the <code>assets</code> application path of the response data.
     * @param headers
     *            contains the response headers.
     */
    public void setMockResponse(MockServer server, int httpStatusCode, String mockRespFile,
            Map<String, String> responseHeaders) throws Exception {

        // Set the mock response HTTP status code.
        server.setResponseHttpStatusCode(httpStatusCode);

        // Set the mock response data.
        Context context = PlatformTestApplication.getApplication();
        AssetManager assetMngr = context.getAssets();
        InputStream is = null;
        if (!TextUtils.isEmpty(mockRespFile)) {
            is = assetMngr.open(mockRespFile);
        }
        if (is != null) {
            try {
                InputStream inp = assetMngr.open(mockRespFile);
                byte[] text = getBytes(inp);
                if (responseHeaders != null) {
                    responseHeaders.put("Content-Length", Long.toString(text.length));
                } else {
                    responseHeaders = new HashMap<String, String>();
                    responseHeaders.put("Content-Length", Long.toString(text.length));
                }
            } catch (IOException ioExc) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".setMockResponse: I/O exception opening an asset file descriptor for '"
                        + mockRespFile + "'");
            }
        } else {
            if (responseHeaders == null) {
                responseHeaders = new HashMap<String, String>();
            }
            responseHeaders.put("Content-Length", "0");
            responseHeaders.put("Content-Type", "text/xml");
        }

        // Set the mock response content type.
        if (responseHeaders != null) {
            server.setResponseHeaders(responseHeaders);
        }

        // If the content-type does not start with "image", then set the response body as
        // a text string; otherwise, set it as a response stream.
        String contentType = null;
        if (responseHeaders != null) {
            contentType = responseHeaders.get("Content-Type");
        }
        if (TextUtils.isEmpty(contentType) || !contentType.startsWith("image")) {
            if (is != null) {
                // NOTE: The code below trims whitespace from each line due to the
                // kXML parser not gracefully handling whitespace within XML. Removing the call
                // to 'trim' will result in empty string values and force XML responses to be formatted
                // as a single-line.
                StringBuilder strBldr = new StringBuilder();
                BufferedReader bufRdr = null;
                try {
                    bufRdr = new BufferedReader(new InputStreamReader(is), (8 * 1024));
                    String line = null;
                    while ((line = bufRdr.readLine()) != null) {
                        strBldr.append(line.trim());
                    }
                    // Set the response body.
                    server.setResponseBody(strBldr.toString());
                } finally {
                    if (bufRdr != null) {
                        bufRdr.close();
                    }
                }
            } else {
                server.setResponseBody(null);
            }
        } else {
            // Set the response stream.
            server.setResponseStream(is);
        }
    }

    /**
     * Will retrieve the instance of <code>Handler</code> used to send the result.
     * 
     * @return returns an instance of <code>Handler</code> used to send the result.
     */
    protected Handler getHander() {

        // Construct the handler thread.
        handlerThread = new HandlerThread("AsyncRequestTask");
        handlerThread.start();

        // Construct the handler using the threads looper.
        return new Handler(handlerThread.getLooper());
    }

    /**
     * Will launch an instance of <code>BaseAsyncRequestTask</code>.
     * 
     * @param requestTask
     *            contains a reference to a <code>BaseAsyncRequestTask</code>.
     */
    protected void launchRequest(BaseAsyncRequestTask requestTask) {

        // Ensure the result is constructed prior to launching the request task.
        result = new AsyncRequestResult();

        // Launch the request.
        requestTask.execute();
    }

    /**
     * Will wait for the request to be completed prior to returning.
     * 
     * @throws InterruptedException
     *             throws interrupted exception if the wait was interrupted.
     */
    protected void waitForResult() throws InterruptedException {

        if (DEBUG) {
            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".waitForResult: waiting for login result.");
        }
        // Wait for the latch to be decremented.
        result.await();
        if (DEBUG) {
            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".waitForResult: got login result.");
        }

    }

    /**
     * An extension of <code>BaseAsyncResultReceiver</code> for handling the result of an asynchronous request.
     */
    public class AsyncReplyListenerImpl implements AsyncReplyListener {

        private static final String CLS_TAG = AsyncRequestTest.CLS_TAG + ".AsyncReplyListenerImpl";

        @Override
        public void onRequestSuccess(Bundle resultData) {
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: ");
            }
            if (result != null) {

                // Set the result information.
                result.resultCode = BaseAsyncRequestTask.RESULT_OK;
                result.resultData = resultData;

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: releasing login result.");
                }

                // Decrement the latch.
                result.countDown();

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: released login result.");
                }

                // Quit the handler thread.
                if (handlerThread != null) {
                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: quitting handler thread.");
                    }
                    handlerThread.quit();
                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: quit handler thread.");
                    }
                    handlerThread = null;
                } else {
                    ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: handlerThread is null!");
                }

            } else {
                ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: loginResult is null!");
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {

            if (DEBUG) {
                ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestFail: ");
            }
            if (result != null) {

                // Set the result information.
                result.resultCode = BaseAsyncRequestTask.RESULT_ERROR;
                result.resultData = resultData;

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestFail: releasing login result.");
                }

                // Decrement the latch.
                result.countDown();

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestFail: released login result.");
                }

                // Quit the handler thread.
                if (handlerThread != null) {
                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestFail: quitting handler thread.");
                    }
                    handlerThread.quit();
                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestFail: quit handler thread.");
                    }
                    handlerThread = null;
                } else {
                    ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".onRequestFail: handlerThread is null!");
                }
            } else {
                ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".onRequestFail: loginResult is null!");
            }
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            if (DEBUG) {
                ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: ");
            }
            if (result != null) {

                // Set the result information.
                result.resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
                result.resultData = resultData;

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: releasing login result.");
                }

                // Decrement the latch.
                result.countDown();

                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: released login result.");
                }

            } else {
                ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: loginResult is null!");
            }
            // Quit the handler thread.
            if (handlerThread != null) {
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: quitting handler thread.");
                }
                handlerThread.quit();
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: quit handler thread.");
                }
                handlerThread = null;
            } else {
                ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: handlerThread is null!");
            }
        }

        @Override
        public void cleanup() {
            if (DEBUG) {
                ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".cleanup: ");
            }
        }
    }

    /**
     * An extension of <code>CountDownLatch</code> used to store the result of an asynchronous request.
     * 
     * @author andrewk
     */
    protected class AsyncRequestResult extends CountDownLatch {

        @SuppressWarnings("unused")
        private static final long serialVersionUID = 1L;

        /**
         * Contains the result data from a login attempt.
         */
        public Bundle resultData;

        /**
         * Contains the result code from a login attempt.
         */
        public int resultCode;

        /**
         * Constructs an instance of <code>AsyncRequestResult</code>
         */
        public AsyncRequestResult() {
            super(1);
        }

    }
}
