/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.platform.expenseit;

import android.content.Context;
import android.net.Uri;
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
import java.net.HttpURLConnection;

public class DeleteExpenseItReceiptAsyncTask extends ExpenseItAsyncRequestTask {

    private static String CLS_TAG = DeleteExpenseItReceiptAsyncTask.class.getSimpleName();

    private static final String EXPENSES_URL = "v1/expenses";

    private static final String SEGMENT_CURRENT_ID = "%d";

    public static final String DELETE_EXPENSEIT_RECEIPT_ASYNC_TASK = "DELETE_EXPENSEIT_RECEIPT_ASYNC_TASK";

    final private Long expenseId;

    protected ErrorResponse deleteReceiptResponse;

    public DeleteExpenseItReceiptAsyncTask(Context context, int requestId, BaseAsyncResultReceiver receiver, Long expenseId) {
        super(context, requestId, receiver);
        this.expenseId = expenseId;
    }

    @Override
    protected String getServiceEndPoint() {
        final Uri.Builder builder = Uri.parse(EXPENSES_URL).buildUpon();
        builder.appendPath(String.format(SEGMENT_CURRENT_ID, expenseId));
        return builder.build().toString();
    }

    @Override
    protected String getPostBody() {
        return super.getPostBody();
    }

    @Override
    protected RequestMethod getRequestMethod() {
        return RequestMethod.DELETE;
    }

    @Override
    protected int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;

        try {
            // Build the parser with type deserializers.
            Gson gson = new GsonBuilder().create();
            if (connection.getResponseCode() >= HttpStatus.SC_OK &&
                connection.getResponseCode() < HttpStatus.SC_MULTIPLE_CHOICES) {

                ErrorResponse tmpResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                    "UTF-8"), ErrorResponse.class);
                if (tmpResp == null) {
                    //Success when ErrorResponse doesn't exist
                    deleteReceiptResponse = new ErrorResponse();
                } else {
                    deleteReceiptResponse = tmpResp;
                    if (deleteReceiptResponse.isError()) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: Error while Deleting the image!");
                        result = BaseAsyncRequestTask.RESULT_ERROR;
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: Error responseCode is not within the correct range !");
                result = BaseAsyncRequestTask.RESULT_ERROR;
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

        if (deleteReceiptResponse != null) {
            resultData.putSerializable(DELETE_EXPENSEIT_RECEIPT_ASYNC_TASK, deleteReceiptResponse);
        }

        return result;
    }
}
