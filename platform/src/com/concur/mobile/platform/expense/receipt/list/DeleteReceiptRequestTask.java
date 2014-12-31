/**
 * 
 */
package com.concur.mobile.platform.expense.receipt.list;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLEncoder;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

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
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of deleting a receipt.
 * 
 * @author andrewk
 */
public class DeleteReceiptRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "DeleteReceiptRequestTask";

    // Contains the service end-point for the <code>expense/MobileReceipts</code> MWS call.
    private final String SERVICE_END_POINT = "/expense/MobileReceipts";

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
     * Constructs an instance of <code>DeleteReceiptRequestTask</code>.
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
     */
    public DeleteReceiptRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, Uri receiptUri,
            String receiptImageId) {

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

        // Obtain a receipt list DAO object.
        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
        ReceiptListDAO receiptListDAO = new ReceiptListDAO(getContext(), sessInfo.getUserId());

        if (this.receiptUri != null) {
            // Obtain the receipt DAO object for 'receiptUri'.
            receiptDAO = receiptListDAO.getReceipt(this.receiptUri);
            if (receiptDAO == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".<init>: unable to obtain receipt DAO object for uri '"
                        + this.receiptUri.toString() + "'.");
            }
        }

    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {

        super.configureConnection(connection);

        try {
            connection.setRequestMethod(REQUEST_METHOD_DELETE);
        } catch (ProtocolException protExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureConnection: protocol exception setting request method to '"
                    + REQUEST_METHOD_DELETE + "'", protExc);
        }

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
    public int onPostParse() {

        int result = super.onPostParse();

        if (receiptDAO != null) {

            // Delete the receipt DAO object.
            if (!receiptDAO.delete()) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: unable to delete receipt at uri '"
                        + receiptDAO.getContentUri().toString() + "'");
            }

            // If 'receiptUri' is already set, then preserve it's value.
            if (receiptUri == null) {
                receiptUri = receiptDAO.getContentUri();
            }
        }

        // Set into the result bundle the Uri of the saved receipt object.
        resultData.putString(SaveReceiptRequestTask.RECEIPT_URI_KEY, receiptUri.toString());

        return result;
    }

    /**
     * Gets a reference to
     * 
     * @return a reference to the <code>OutputStream</code> that can be used to write out receipt data.
     */
    protected ReceiptDAO getReceiptDAO() {
        ReceiptDAO rcptDao = null;

        return rcptDao;
    }

}
