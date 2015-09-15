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
import java.net.HttpURLConnection;

public class UpdateExpenseItAccountInfoAsyncTask extends ExpenseItAsyncRequestTask {

    private static String CLS_TAG = UpdateExpenseItAccountInfoAsyncTask.class.getSimpleName();

    private static final String ACCOUNTS_URL = "/v1/accounts/me";

    public static final String UPDATE_EXPENSEIT_ACCOUNT_INFO = "UPDATE_EXPENSEIT_ACCOUNT_INFO";

    protected ExpenseItAccountInfo accountInfo;

    protected ExpenseItAccountInfoModel accountUpdateResponse;

    public UpdateExpenseItAccountInfoAsyncTask(Context context, int requestId, BaseAsyncResultReceiver receiver, ExpenseItAccountInfo accountInfo) {
        super(context, requestId, receiver);
        this.accountInfo = accountInfo;
    }

    @Override
    protected String getServiceEndPoint() {
        return ACCOUNTS_URL;
    }

    @Override
    protected String getPostBody() {
        Gson gson = new GsonBuilder().create();
        ExpenseItAccountInfoModel info = new ExpenseItAccountInfoModel();
        info.setAccountInfo(accountInfo);
        return gson.toJson(info);
    }

    @Override
    protected RequestMethod getRequestMethod() {
        return RequestMethod.PUT;
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
                    accountUpdateResponse = tmpResp;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWSResponse was null!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWSResponse Error response was null!");
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
        if (accountUpdateResponse != null) {
            resultData.putSerializable(UPDATE_EXPENSEIT_ACCOUNT_INFO, accountUpdateResponse);
        }

        return result;
    }
}
