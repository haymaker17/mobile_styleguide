/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.platform.expenseit;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.service.ExpenseItAsyncRequestTask;
import com.concur.mobile.platform.util.Const;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.util.Arrays;

public class PostExpenseItReceiptAsyncTask extends ExpenseItAsyncRequestTask {

    private static String CLS_TAG = PostExpenseItReceiptAsyncTask.class.getSimpleName();

    private static final String EXPENSES_URL = "v1/expenses";

    public static final String POST_EXPENSEIT_OCR_RESULT_KEY = "POST_EXPENSEIT_OCR_RESULT_KEY";
    protected ExpenseItImage image;

    protected ExpenseItPostReceiptResponse receiptResponse;

    public PostExpenseItReceiptAsyncTask(Context context, int requestId, BaseAsyncResultReceiver receiver, ExpenseItImage image) {
        super(context, requestId, receiver);
        this.image = image;
    }

    @Override
    protected String getServiceEndPoint() {
        return EXPENSES_URL;
    }

    @Override
    protected String getPostBody() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(image);
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);
        connection.setRequestProperty(HTTP_HEADER_CONTENT_TYPE, "application/json");
    }

    @Override
    protected int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;

        try {
            // Build the parser with type deserializers.
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            if (connection.getResponseCode() >= HttpStatus.SC_OK &&
                connection.getResponseCode() < HttpStatus.SC_MULTIPLE_CHOICES) {

                ExpenseItPostReceiptResponse tmpResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                    "UTF-8"), ExpenseItPostReceiptResponse.class);

                if (tmpResp != null) {
                    receiptResponse = tmpResp;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWSResponse was null!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                }
            } else {
                // prepare the object Type expected in MWS response 'data' element
                ErrorResponse errResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                    "UTF-8"), ErrorResponse.class);
                if (errResp != null) {
                    ExpenseItPostReceipt receipt = new ExpenseItPostReceipt();
                    receipt.setExpenseError(errResp);
                    ExpenseItPostReceipt[] list = new ExpenseItPostReceipt[] {receipt};
                    receiptResponse.setExpenses((list));
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWSResponse Error response was null!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                }
            }
        } catch (Exception exc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception parsing data.", exc);
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
    protected int onPostParse() {
        int result = super.onPostParse();
        if (receiptResponse != null) {
            resultData.putSerializable(POST_EXPENSEIT_OCR_RESULT_KEY, receiptResponse);
        }

        return result;
    }
}
