/**
 * 
 */
package com.concur.mobile.platform.expense.receipt.list;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ClientData;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.ActionStatus;
import com.concur.mobile.platform.service.parser.Error;
import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.util.CalendarDeserializer;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of retrieving a receipt list.
 * 
 * @author andrewk
 */
public class ReceiptListRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "ReceiptListRequestTask";

    // Contains the service end-point for the <code><name here></code> MWS call.
    private final String SERVICE_END_POINT = "/mobile/expense/v1.0/receipts";

    /**
     * Contains the client data key that stores the ETag value for the receipt list.
     * 
     * @see {@link ClientData}
     */
    public static final String RECEIPT_LIST_ETAG_CLIENT_DATA_KEY = "_RECEIPT_LIST_ETAG";

    /**
     * Contains the key that can be used to retrieve an <code>ActionStatus</code> object related to retrieving the receipt list.
     * 
     * @see ActionStatus
     */
    public static final String RECEIPT_LIST_ACTION_STATUS_KEY = "RECEIPT_LIST_ACTION_STATUS";

    /**
     * Contains the key that can be used to retrieve a list of error objects.
     * 
     * @see List
     */
    public static final String RECEIPT_LIST_ERROR_KEY = "RECEIPT_LIST_ERROR";

    /**
     * Contains the parsed MWS response.
     */
    protected MWSResponse<ReceiptList> mwsResp;

    /**
     * Contains the client data that stores the receipt list ETag value.
     */
    protected ClientData eTagClientData;

    /**
     * Constructs an instance of <code>ExpenseListRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains a result receiver.
     */
    public ReceiptListRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver) {
        super(context, requestId, receiver);
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {

        super.configureConnection(connection);

        // Set the accept header to JSON.
        connection.setRequestProperty(HEADER_ACCEPT, CONTENT_TYPE_JSON);

        // Set the ETag.
        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
        String userId = sessInfo.getUserId();
        eTagClientData = new ClientData(getContext());
        eTagClientData.key = RECEIPT_LIST_ETAG_CLIENT_DATA_KEY;
        eTagClientData.userId = userId;
        if (eTagClientData.load()) {
            // Set the etag header, if needbe.
            if (!TextUtils.isEmpty(eTagClientData.text)) {
                connection.setRequestProperty(HEADER_IF_NONE_MATCH, eTagClientData.text);
            }
        }
    }

    @Override
    protected String getServiceEndPoint() {
        return SERVICE_END_POINT;
    }

    @Override
    public int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {

            if (connection.getResponseCode() == HttpStatus.SC_OK) {

                // Build the parser with type deserializers.
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Calendar.class, new CalendarDeserializer(Parse.XML_DF_NO_T));
                Gson gson = builder.create();

                // prepare the object Type expected in MWS response 'data' element
                Type type = new TypeToken<MWSResponse<ReceiptList>>() {}.getType();
                mwsResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"), type);

                // Obtain the ETag value.
                String eTagField = connection.getHeaderField(HEADER_ETAG);
                if (!TextUtils.isEmpty(eTagField)) {
                    eTagClientData.text = eTagField;
                    if (!eTagClientData.update()) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: unable to save ETag client data!");
                    }
                }

            }

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

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
        if (sessInfo == null) {
            return result;
        }

        String userId = sessInfo.getUserId();
        if (userId == null) {
            return result;
        }

        ReceiptList receiptList = mwsResp.getData();

        if (receiptList != null) {

            // Add to the result data the action status result of retrieving the result
            // list.
            if (receiptList.receiptListStatus != null) {
                resultData.putSerializable(RECEIPT_LIST_ACTION_STATUS_KEY, receiptList.receiptListStatus);
            }

            // Add to the result data any list of returned errors.
            if (mwsResp.getErrors() != null) {
                ArrayList<Error> ocrErrorList = new ArrayList<Error>(mwsResp.getErrors().size());
                ocrErrorList.addAll(mwsResp.getErrors());
                resultData.putSerializable(RECEIPT_LIST_ERROR_KEY, ocrErrorList);
            }

            // Reconcile the current persisted receipt list with the
            // newly parsed list.
            ReceiptList.reconcile(getContext(), userId, receiptList.receiptInfos);

            if (receiptList.receiptInfos != null) {
                for (Receipt rcpt : receiptList.receiptInfos) {

                    // Set the user id and context.
                    rcpt.userId = userId;
                    rcpt.context = getContext();

                    // Update the receipt.
                    if (!rcpt.update()) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: unable to save receipt!");
                    }

                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: receipt list was not parsed.");
            result = BaseAsyncRequestTask.RESULT_ERROR;
        }
        return result;
    }
}
