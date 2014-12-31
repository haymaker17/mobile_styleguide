/**
 * 
 */
package com.concur.mobile.platform.expense.receipt.list;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptListDAO;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.Const;
import com.google.gson.Gson;

/**
 * An extension of <code>BaseAsyncRequestTask</code> for the purposes of saving a receipt.
 * 
 * @author andrewk
 */
public class SaveReceiptRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "SaveReceiptRequestTask";

    /**
     * Contains the key that should be used to look up the URI of a receipt that has been saved to the server.
     */
    public static final String RECEIPT_URI_KEY = "receipt.uri";

    // Contains the service end-point for the <code>expense/MobileReceipts</code> MWS call.
    private final String SERVICE_END_POINT = "/expense/MobileReceipts";

    /**
     * Provides an interface reporting upload progress.
     * 
     * @author andrewk
     */
    public static interface SaveReceiptUploadListener {

        /**
         * Provides a notification that the upload has started.
         * 
         * @param contentLength
         *            contains the content length, or <code>-1</code> if unknown.
         */
        public void onStart(long contentLength);

        /**
         * Provides a notification that <code>count</code> bytes of data have been uploaded.
         * 
         * @param count
         *            contains the count of bytes from the last write.
         */
        public void onUpload(int count);

        /**
         * Provides a notification that upload has completed.
         */
        public void onComplete();

    }

    /**
     * Contains a reference to an upload listener.
     */
    protected SaveReceiptUploadListener listener;

    /**
     * Contains the content as an input stream.
     */
    protected InputStream content;

    /**
     * Contains the content length.
     */
    protected Long contentLength = -1L;

    /**
     * Contains the content type.
     */
    protected String contentType;

    /**
     * Contains the receipt uri.
     */
    protected Uri receiptUri;

    /**
     * Contains whether or not the receipt data is being read from the receipt Uri.
     */
    protected boolean readingFromReceiptUri;

    /**
     * Contains a reference to the a receipt object stored locally.
     */
    protected ReceiptDAO receiptDAO;

    /**
     * Contains a reference to a parsed receipt object.
     */
    protected Receipt receipt;

    /**
     * Constructs an instance of <code>SaveReceiptRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains a result receiver.
     * @param receiptUri
     *            contains the Uri of the receipt being saved. If <code>null</code> then an entry in the
     *            <code>Expense.ReceiptColumns.TABLE_NAME</code> will be created.
     * @param content
     *            contains the content input stream.
     * @param contentLength
     *            contains the content-length, or <code>-1</code> if not known.
     * @param contentType
     *            contains the content-type or <code>null</code> if not known.
     * @param listener
     *            contains the upload listener.
     */
    public SaveReceiptRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, Uri receiptUri,
            SaveReceiptUploadListener listener) {

        super(context, requestId, receiver);

        if (receiptUri == null) {
            throw new IllegalArgumentException(CLS_TAG + ".<init>: receipt uri is null!");
        }
        this.receiptUri = receiptUri;
        this.content = getReceiptInputStream();
        if (content != null) {
            if (receiptDAO != null) {
                this.contentType = receiptDAO.getContentType();
            } else {
                throw new IllegalArgumentException(CLS_TAG + ".<init>: receipt uri has no content-type!");
            }
        } else {
            throw new IllegalArgumentException(CLS_TAG + ".<init>: receipt uri has no input stream!");
        }

        // HACK Alert - attempt to set the content length based on looking at the local file path!
        if (receiptDAO.getLocalPath() != null) {
            File rcptFile = new File(receiptDAO.getLocalPath());
            if (rcptFile.exists() && rcptFile.isFile()) {
                contentLength = rcptFile.length();
            }
        }

        // Set the flag indicating the data is being read from the receipt Uri.
        readingFromReceiptUri = true;

        this.listener = listener;
    }

    /**
     * Constructs an instance of <code>SaveReceiptRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains a result receiver.
     * @param receiptUri
     *            contains the Uri of the receipt being saved. If <code>null</code> then an entry in the
     *            <code>Expense.ReceiptColumns.TABLE_NAME</code> will be created.
     * @param content
     *            contains the content input stream.
     * @param contentLength
     *            contains the content-length, or <code>-1</code> if not known.
     * @param contentType
     *            contains the content-type or <code>null</code> if not known.
     * @param listener
     *            contains the upload listener.
     */
    public SaveReceiptRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, Uri receiptUri,
            InputStream content, long contentLength, String contentType, SaveReceiptUploadListener listener) {

        super(context, requestId, receiver);

        if (content == null) {
            throw new IllegalArgumentException(CLS_TAG + ".<init>: content is null!");
        }

        this.receiptUri = receiptUri;
        this.content = content;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.listener = listener;
    }

    /**
     * Constructs an instance of <code>SaveReceiptRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains a result receiver.
     * @param receiptUri
     *            contains the Uri of the receipt being saved. If <code>null</code> then an entry in the
     *            <code>Expense.ReceiptColumns.TABLE_NAME</code> will be created.
     * @param content
     *            contains the content data.
     * @param contentType
     *            contains the content-type or <code>null</code> if not known.
     * @param listener
     *            contains the upload listener.
     */
    public SaveReceiptRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, Uri receiptUri,
            byte[] content, String contentType, SaveReceiptUploadListener listener) {

        super(context, requestId, receiver);

        if (content == null) {
            throw new IllegalArgumentException(CLS_TAG + ".<init>: content is null!");
        }

        this.receiptUri = receiptUri;
        this.content = new ByteArrayInputStream(content);
        this.contentLength = new Long(content.length);
        this.contentType = contentType;
        this.listener = listener;
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {

        super.configureConnection(connection);

        try {
            connection.setRequestMethod(REQUEST_METHOD_POST);
        } catch (ProtocolException protExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureConnection: protocol exception setting request method to '"
                    + REQUEST_METHOD_POST + "'", protExc);
        }

        // Set the accept header to JSON.
        connection.setRequestProperty(HEADER_ACCEPT, CONTENT_TYPE_JSON);

        // Set the content-type.
        if (!TextUtils.isEmpty(contentType)) {
            connection.setRequestProperty(HEADER_CONTENT_TYPE, contentType);
        }

        // Set the content-length.
        if (contentLength != -1L) {
            connection.setRequestProperty(HEADER_CONTENT_LENGTH, Long.toString(contentLength));
        }
    }

    @Override
    protected int doPost(HttpURLConnection connection) {

        int res = RESULT_OK;

        // Only set up to write to the receipt Uri if the data is not
        // coming from there.
        OutputStream rcptOut = null;
        if (!readingFromReceiptUri) {
            rcptOut = getReceiptOutputStream();
            if (rcptOut == null) {
                res = RESULT_ERROR;
            }
        }

        // Set the stream mode based on whether the content length is known in
        // advance.
        if (contentLength > -1) {
            if (contentLength <= Integer.MAX_VALUE) {
                connection.setFixedLengthStreamingMode((new Long(contentLength).intValue()));
            } else {
                connection.setChunkedStreamingMode(0);
            }
        } else {
            connection.setChunkedStreamingMode(0);
        }

        // Check for cancel
        if (isCancelled()) {
            res = RESULT_CANCEL;
        }

        if (res == RESULT_OK) {

            // Set the "do output" flag.
            connection.setDoOutput(true);

            // Connect and send the post
            OutputStream out = null;
            try {
                out = new BufferedOutputStream(connection.getOutputStream());
                byte[] buf = new byte[(16 * 1024)];
                int bytesRead = -1;
                // Notify the listener.
                if (listener != null) {
                    try {
                        listener.onStart(contentLength);
                    } catch (Throwable t) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".doPost: listener.onStart -- ", t);
                    }
                }
                while ((bytesRead = content.read(buf)) != -1) {
                    // Write out to connection.
                    out.write(buf, 0, bytesRead);
                    // Write out to receipt output stream.
                    if (rcptOut != null) {
                        rcptOut.write(buf, 0, bytesRead);
                    }
                    // Notify the listener.
                    if (listener != null) {
                        try {
                            listener.onUpload(bytesRead);
                        } catch (Throwable t) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".doPost: listener.onWrite -- ", t);
                        }
                    }
                }
                // Flush the output stream.
                out.flush();
                if (rcptOut != null) {
                    rcptOut.flush();
                }
                // Notify the listener.
                if (listener != null) {
                    try {
                        listener.onComplete();
                    } catch (Throwable t) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".doPost: listener.onComplete -- ", t);
                    }
                }
            } catch (IOException e) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".doPost: // error opening and writing output stream // " + getURL(), e);
                res = RESULT_ERROR;
            } finally {
                if (rcptOut != null) {
                    try {
                        rcptOut.close();
                    } catch (IOException ioExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".doPost: I/O exception closing receipt output stream!", ioExc);
                    } finally {
                        rcptOut = null;
                    }
                }
            }
        }
        return res;
    }

    /**
     * Gets a reference to the <code>OutputStream</code> that can be used to write out receipt data.
     * 
     * @return a reference to the <code>OutputStream</code> that can be used to write out receipt data.
     */
    protected OutputStream getReceiptOutputStream() {
        OutputStream rcptOut = null;

        // Obtain a receipt list DAO object.
        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
        ReceiptListDAO receiptListDAO = new ReceiptListDAO(getContext(), sessInfo.getUserId());

        // Construct a local DAO object.
        if (receiptUri == null) {
            receiptDAO = receiptListDAO.createReceipt();
            if (receiptDAO.update()) {
                receiptUri = receiptDAO.getContentUri();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getReceiptOutputStream: unable to save new receipt object!");
            }
        }

        if (receiptUri != null) {
            // Obtain the receipt DAO object for 'receiptUri'.
            receiptDAO = receiptListDAO.getReceipt(receiptUri);
            if (receiptDAO != null) {
                ContentResolver resolver = getContext().getContentResolver();
                try {
                    rcptOut = new BufferedOutputStream(resolver.openOutputStream(receiptUri));
                } catch (FileNotFoundException fnfExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getReceiptOutputStream: unable to open output stream for uri '"
                            + receiptUri.toString() + "'.", fnfExc);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getReceiptOutputStream: unable to obtain receipt DAO object for uri '"
                        + receiptUri.toString() + "'.");
            }
        }
        return rcptOut;
    }

    /**
     * Gets a reference to the <code>InputStream</code> that can be used to read receipt data.
     * 
     * This method will also set the content type on this request task.
     * 
     * @return a reference to the <code>InputStream</code> that can be used to read receipt data.
     */
    protected InputStream getReceiptInputStream() {
        InputStream rcptIn = null;

        // Obtain a receipt list DAO object.
        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
        ReceiptListDAO receiptListDAO = new ReceiptListDAO(getContext(), sessInfo.getUserId());

        if (receiptUri != null) {
            // Obtain the receipt DAO object for 'receiptUri'.
            receiptDAO = receiptListDAO.getReceipt(receiptUri);
            if (receiptDAO != null) {
                ContentResolver resolver = getContext().getContentResolver();
                try {
                    rcptIn = new BufferedInputStream(resolver.openInputStream(receiptUri));
                } catch (FileNotFoundException fnfExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getReceiptInputStream: unable to open input stream for uri '"
                            + receiptUri.toString() + "'.", fnfExc);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getReceiptInputStream: unable to obtain receipt DAO object for uri '"
                        + receiptUri.toString() + "'.");
            }
        }
        return rcptIn;
    }

    @Override
    protected String getServiceEndPoint() {
        return SERVICE_END_POINT;
    }

    @Override
    protected int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {

            // Parse the receipt object.
            Gson gson = new Gson();
            receipt = gson.fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"), Receipt.class);

        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception parsing data.", ioExc);
            result = BaseAsyncRequestTask.RESULT_ERROR;
        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception closing input stream.", ioExc);
                }
            }
        }
        return result;
    }

    @Override
    public int onPostParse() {

        int result = super.onPostParse();

        if (receipt != null) {
            // Copy the parsed receipt information into the DAO object and update it.
            if (receiptDAO != null) {
                receiptDAO.setETag(receipt.getETag());
                receiptDAO.setId(receipt.getId());
                receiptDAO.setUri(receipt.getUri());
                receiptDAO.setThumbnailUri(receipt.getThumbnailUri());
                if (!receiptDAO.update()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: unable to save receipt DAO object!");
                    result = RESULT_ERROR;
                }

                // Set into the result bundle the Uri of the saved receipt object.
                resultData.putString(RECEIPT_URI_KEY, receiptDAO.getContentUri().toString());

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: no receipt DAO object was created!");
                result = RESULT_ERROR;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: no receipt object was parsed!");
            result = RESULT_ERROR;
        }

        return result;
    }

}
