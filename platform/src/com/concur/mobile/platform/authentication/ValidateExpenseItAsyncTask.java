/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.platform.authentication;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.service.ExpenseItAsyncRequestTask;
import com.concur.mobile.platform.util.Const;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class ValidateExpenseItAsyncTask  extends ExpenseItAsyncRequestTask {

    private static final String CLS_TAG = ValidateExpenseItAsyncTask.class.getSimpleName();

    static final String VALIDATION_URL = "v1/validation";

    ProvisionExpenseItResult provisionExpenseItResult;

    public static final String RESULT_IS_PROVISIONED = "RESULT_IS_PROVISIONED";

    public ValidateExpenseItAsyncTask(Context context, int requestId, BaseAsyncResultReceiver receiver) {
        super(context, requestId, receiver);
    }

    @Override
    protected String getServiceEndPoint() {
        Uri.Builder builder = Uri.parse(VALIDATION_URL).buildUpon();
        return builder.build().toString();
    }

    @Override
    protected String getPostBody() {
        return super.getPostBody();
    }

    @Override
    public int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            if (connection.getResponseCode() == HttpStatus.SC_OK) {
                // Build the parser with type deserializers.
                Gson gson = new GsonBuilder().create();
                provisionExpenseItResult = gson.fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"),
                    ProvisionExpenseItResult.class);
            }

        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception parsing data.", ioExc);
            result = BaseAsyncRequestTask.RESULT_ERROR;
        } catch (JsonSyntaxException jse) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: exception parsing JSON", jse);
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
        if (result == RESULT_OK && provisionExpenseItResult != null) {
            resultData.putBoolean(RESULT_IS_PROVISIONED, provisionExpenseItResult.getProvisioningStatus().isBasicAccess());
        }
        return result;
    }
}
