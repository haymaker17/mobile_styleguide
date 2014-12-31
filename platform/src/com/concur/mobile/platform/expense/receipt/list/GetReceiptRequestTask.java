/**
 * 
 */
package com.concur.mobile.platform.expense.receipt.list;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpStatus;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptListDAO;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of retrieving a receipt.
 * 
 * @author andrewk
 */
public class GetReceiptRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "GetReceiptRequestTask";

    // Contains the service end-point for the <code>expense/MobileReceipts</code> MWS call.
    private final String SERVICE_END_POINT = "/expense/MobileReceipts";

    /**
     * Provides an interface reporting download progress.
     * 
     * @author andrewk
     */
    public static interface GetReceiptDownloadListener {

        /**
         * Provides a notification that the upload has started.
         * 
         * @param contentLength
         *            contains the total size of the download. If not known, then <code>-1</code> will be provided.
         */
        public void onStart(long contentLength);

        /**
         * Provides a notification that <code>count</code> bytes of data have been written.
         * 
         * @param count
         *            contains the count of bytes from the last write.
         */
        public void onDownload(int count);

        /**
         * Provides a notification that upload has completed.
         */
        public void onComplete();

    }

    /**
     * Contains a reference to a download listener.
     */
    protected GetReceiptDownloadListener listener;

    /**
     * Contains the content type.
     */
    protected String contentType;

    /**
     * Contains the content length, or <code>-1</code> if not known.
     */
    protected long contentLength = -1L;

    /**
     * Contains the receipt Uri.
     */
    protected Uri receiptUri;

    /**
     * Contains the receipt DAO object.
     */
    protected ReceiptDAO receiptDAO;

    /**
     * Contains the receipt image ID.
     */
    protected String receiptImageId;

    /**
     * Contains the ETag of the receipt.
     */
    protected String eTag;

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
     *            contains the Uri of the receipt to be downloaded.
     * @param receiptImageId
     *            contains the receipt image ID of the receipt to be downloaded.
     * @param listener
     *            contains the download listener.
     */
    public GetReceiptRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, Uri receiptUri,
            String receiptImageId, GetReceiptDownloadListener listener) {

        super(context, requestId, receiver);

        // Check that either one has been provided.
        if (receiptUri == null && TextUtils.isEmpty(receiptImageId)) {
            throw new IllegalArgumentException(CLS_TAG + ".<init>: both receipt uri and receipt image id are null!");
        }

        // If 'receiptImageId' is null, then look it up if 'receiptImageUri' has been provided.
        if (TextUtils.isEmpty(receiptImageId)) {
            receiptImageId = ContentUtils.getColumnStringValue(context, receiptUri, Expense.ReceiptColumns.ID);
            if (TextUtils.isEmpty(receiptImageId)) {
                throw new IllegalArgumentException(CLS_TAG + ".<init>: receipt uri is missing the id.");
            }
        }

        // Attempt to get the content uri.
        if (receiptUri == null) {
            receiptUri = ContentUtils.getContentUri(context, Expense.ReceiptColumns.CONTENT_URI,
                    Expense.ReceiptColumns.ID, receiptImageId);
        }

        // Set the current ETag if a content uri could be found.
        if (receiptUri != null) {
            eTag = ContentUtils.getColumnStringValue(context, receiptUri, Expense.ReceiptColumns.ETAG);
        }

        this.receiptUri = receiptUri;
        this.receiptImageId = receiptImageId;
        this.listener = listener;
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {

        super.configureConnection(connection);

        // Set the ETag value if we have one.
        if (!TextUtils.isEmpty(eTag)) {
            connection.addRequestProperty(HEADER_IF_NONE_MATCH, eTag);
        }
    }

    @Override
    protected String getServiceEndPoint() {
        StringBuilder strBldr = new StringBuilder();
        try {
            strBldr.append(SERVICE_END_POINT);
            strBldr.append('/');
            strBldr.append(URLEncoder.encode(receiptImageId, "UTF-8"));
        } catch (UnsupportedEncodingException unsupEncExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getServiceEndPoint: ", unsupEncExc);
        }
        return strBldr.toString();
    }

    @Override
    public int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;

        OutputStream rcptOut = null;
        try {
            if (connection.getResponseCode() == HttpStatus.SC_OK) {

                // Obtain the ETag value.
                eTag = connection.getHeaderField(HEADER_ETAG);

                // Obtain the content type.
                contentType = connection.getHeaderField(HEADER_CONTENT_TYPE);

                // Obtain content length.
                String contentLengthStr = connection.getHeaderField(HEADER_CONTENT_LENGTH);
                if (!TextUtils.isEmpty(contentLengthStr)) {
                    try {
                        contentLength = Long.parseLong(contentLengthStr.trim());
                    } catch (NumberFormatException numFormExc) {
                        Log.w(Const.LOG_TAG,
                                CLS_TAG + ".parseStream: header content-length '" + contentLengthStr.trim()
                                        + "' can't be parsed!");
                    }
                }

                // Obtain an output stream in which to write the data.
                rcptOut = getReceiptOutputStream();
                if (rcptOut != null) {

                    InputStream in = null;
                    in = new BufferedInputStream(is);
                    byte[] buf = new byte[(16 * 1024)];
                    int bytesRead = -1;

                    // Notify the listener.
                    if (listener != null) {
                        try {
                            listener.onStart(contentLength);
                        } catch (Throwable t) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: listener.onStart -- ", t);
                        }
                    }
                    while ((bytesRead = in.read(buf)) != -1) {
                        // Write out to receipt output stream.
                        if (rcptOut != null) {
                            rcptOut.write(buf, 0, bytesRead);
                        }
                        // Notify the listener.
                        if (listener != null) {
                            try {
                                listener.onDownload(bytesRead);
                            } catch (Throwable t) {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: listener.onWrite -- ", t);
                            }
                        }
                    }
                    // Flush the output stream.
                    if (rcptOut != null) {
                        rcptOut.flush();
                    }
                    // Notify the listener.
                    if (listener != null) {
                        try {
                            listener.onComplete();
                        } catch (Throwable t) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: listener.onComplete -- ", t);
                        }
                    }

                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: unable to open an output stream to write data!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                }

            }

        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception reading receipt data!", ioExc);
            result = BaseAsyncRequestTask.RESULT_ERROR;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception closing input stream.", ioExc);
                } finally {
                    is = null;
                }
            }
            if (rcptOut != null) {
                try {
                    rcptOut.close();
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception closing output stream.", ioExc);
                } finally {
                    rcptOut = null;
                }
            }
        }
        return result;
    }

    @Override
    public int onPostParse() {

        int result = super.onPostParse();

        if (receiptDAO != null) {

            // Update values.
            receiptDAO.setETag(eTag);
            receiptDAO.setId(receiptImageId);
            receiptDAO.setUri(receiptUri.toString());
            receiptDAO.setContentType(contentType);
            if (!receiptDAO.update()) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: failed to update receipt DAO!");
            }
            // If 'receiptUri' is already set, then preserve it's value as it may refer to
            // the thumbnail.
            if (receiptUri == null) {
                receiptUri = receiptDAO.getContentUri();
            }
        }

        // Set into the result bundle the Uri of the saved receipt object.
        resultData.putString(SaveReceiptRequestTask.RECEIPT_URI_KEY, receiptUri.toString());

        return result;
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

}
