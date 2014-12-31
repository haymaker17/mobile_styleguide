/**
 * Copyright (c) 2014 Concur Technologies, Inc.
 */
package com.concur.mobile.platform.expense.receipt.ocr;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLEncoder;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.util.Const;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author Chris N. Diaz
 *
 */
public class StartOCRRequestTask extends PlatformAsyncRequestTask {

    /**
     * Class name used for logging.
     */
    public static final String CLS_TAG = StartOCRRequestTask.class.getName();

    /**
     * Key used to bundle the StartOCR result object.
     */
    public static final String START_OCR_RESULT_KEY = "START_OCR_RESULT";

    /**
     * MWS Endpoint to invoke. The first argument is the protected receipt image ID. The second argument should always be 'mobile'
     * for the mobile clients.
     */
    private static final String SERVICE_END_POINT = "/mobile/expense/v1.0/receipts/%s/StartOCR/mobile";

    /**
     * Object holding the result of calling this endpoint.
     */
    protected StartOCR startOcr;

    /**
     * The (protected) receipt image ID.
     */
    protected String receiptImageId;

    /**
     * Constructs an instance of <code>StartOCRRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains a result receiver.
     * @param receiptId
     *            contains the (protected) receipt image ID.
     */
    public StartOCRRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, String receiptImageId) {
        super(context, requestId, receiver);

        this.receiptImageId = receiptImageId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#configureConnection(java.net.HttpURLConnection)
     */
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#getServiceEndPoint()
     */
    @Override
    protected String getServiceEndPoint() {

        String rid;
        try {
            rid = URLEncoder.encode(receiptImageId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getServiceEndPoint: Couldn't encode receipt image ID: " + receiptImageId,
                    e);
            rid = URLEncoder.encode(receiptImageId);
        }

        return String.format(SERVICE_END_POINT, rid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.base.service.BaseAsyncRequestTask#parseStream(java.net.HttpURLConnection, java.io.InputStream)
     */
    @Override
    protected int parseStream(HttpURLConnection connection, InputStream is) {

        int result = BaseAsyncRequestTask.RESULT_OK;

        try {

            if (connection.getResponseCode() == HttpStatus.SC_OK) {

                // Build the parser with type deserializers.
                Gson gson = new GsonBuilder().create();

                // prepare the object Type expected in MWS response 'data' element
                Type type = new TypeToken<MWSResponse<StartOCR>>() {}.getType();
                MWSResponse<StartOCR> mwsResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                        "UTF-8"), type);

                if (mwsResp != null) {
                    if (mwsResp.getData() != null) {
                        startOcr = mwsResp.getData();
                    } else {
                        startOcr = new StartOCR();
                    }
                    startOcr.errors = mwsResp.getErrors();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWSResponse was null!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#onPostParse()
     */
    @Override
    protected int onPostParse() {
        int result = super.onPostParse();

        if (startOcr != null) {
            // Handle errors from the
            if (startOcr.errors != null && !startOcr.errors.isEmpty()) {
                result = BaseAsyncRequestTask.RESULT_ERROR;
            }

            resultData.putSerializable(START_OCR_RESULT_KEY, startOcr);
        }

        return result;
    }

}
