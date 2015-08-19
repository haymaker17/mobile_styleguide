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

public class GetExpenseItAccountInfoAsyncTask extends ExpenseItAsyncRequestTask {

    private static String CLS_TAG = GetExpenseItAccountInfoAsyncTask.class.getSimpleName();

    private static final String ACCOUNTS_URL = "/v1/accounts/me";

    private static final String PARAM_INCLUDE_EXPENSE_ITEMS = "should_include_expense_items";

    static final String PARAM_VALUE_FALSE = "0";

    public static final String GET_EXPENSEIT_ACCOUNT_INFO = "GET_EXPENSEIT_ACCOUNT_INFO";

    protected ExpenseItAccountInfoModel accountInfoResponse;

    protected final String userId;

    public GetExpenseItAccountInfoAsyncTask(Context context, int requestId, String userId, BaseAsyncResultReceiver receiver) {
        super(context, requestId, receiver);
        this.userId = userId;
    }

    @Override
    protected String getServiceEndPoint() {
        final Uri.Builder builder = Uri.parse(ACCOUNTS_URL).buildUpon();
        builder.appendQueryParameter(PARAM_INCLUDE_EXPENSE_ITEMS, PARAM_VALUE_FALSE);
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

                ExpenseItAccountInfoModel tmpResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                    "UTF-8"), ExpenseItAccountInfoModel.class);

                if (tmpResp != null) {
                    accountInfoResponse = tmpResp;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWSResponse was null!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                }
            } else {
                // prepare the object Type expected in MWS response 'data' element
                ErrorResponse errResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                    "UTF-8"), ErrorResponse.class);
                if (errResp != null) {
                    ExpenseItAccountInfoModel accountInfoResponse = new ExpenseItAccountInfoModel();
                    accountInfoResponse.setErrorCode(errResp.getErrorCode());
                    accountInfoResponse.setErrorMessage(errResp.getErrorMessage());
                    accountInfoResponse.setAccountInfo(null);
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

        if (accountInfoResponse != null) {
            resultData.putSerializable(GET_EXPENSEIT_ACCOUNT_INFO, accountInfoResponse);
        }

        return result;
    }
}
