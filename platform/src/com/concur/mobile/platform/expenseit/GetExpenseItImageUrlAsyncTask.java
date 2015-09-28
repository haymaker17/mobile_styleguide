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

public class GetExpenseItImageUrlAsyncTask extends ExpenseItAsyncRequestTask {

    private static String CLS_TAG = GetExpenseItImageUrlAsyncTask.class.getSimpleName();

    private static final String EXPENSES_URL = "v1/expenses";

    private static final String SEGMENT_CURRENT_ID = "%d";

    private static final String SEGMENT_IMAGE = "image";

    public static final String GET_EXPENSEIT_IMAGE_URL_RESULT_KEY = "GET_EXPENSEIT_IMAGE_URL_RESULT_KEY";

    final private Long expenseId;

    protected ExpenseItGetImageUrlResponse imageUrlResponse;

    public GetExpenseItImageUrlAsyncTask(Context context, int requestId, BaseAsyncResultReceiver receiver, Long expenseId) {
        super(context, requestId, receiver);
        this.expenseId = expenseId;
    }

    @Override
    protected String getServiceEndPoint() {
        final Uri.Builder builder = Uri.parse(EXPENSES_URL).buildUpon();
        builder.appendPath(String.format(SEGMENT_CURRENT_ID, expenseId));
        builder.appendPath(SEGMENT_IMAGE);
        return builder.build().toString();
    }

    @Override
    protected String getPostBody() {
        return super.getPostBody();
    }

    @Override
    protected RequestMethod getRequestMethod() {
        return RequestMethod.GET;
    }

    @Override
    protected int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;

        try {
            // Build the parser with type deserializers.
            Gson gson = new GsonBuilder().create();
            if (connection.getResponseCode() >= HttpStatus.SC_OK &&
                connection.getResponseCode() < HttpStatus.SC_MULTIPLE_CHOICES) {

                ExpenseItGetImageUrlResponse tmpResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                    "UTF-8"), ExpenseItGetImageUrlResponse.class);

                if (tmpResp != null) {
                    imageUrlResponse = tmpResp;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWSResponse was null!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                }
            } else {
                // prepare the object Type expected in MWS response 'data' element
                ErrorResponse errResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                    "UTF-8"), ErrorResponse.class);

                if (errResp != null) {
                    ExpenseItGetImageUrlResponse imageUrlResponse = new ExpenseItGetImageUrlResponse();
                    imageUrlResponse.setErrorCode(errResp.getErrorCode());
                    imageUrlResponse.setErrorMessage(errResp.getErrorMessage());
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

        if (imageUrlResponse != null) {
            resultData.putSerializable(GET_EXPENSEIT_IMAGE_URL_RESULT_KEY, imageUrlResponse);
        }

        return result;
    }
}
