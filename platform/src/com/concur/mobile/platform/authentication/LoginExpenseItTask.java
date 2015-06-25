/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.platform.authentication;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.ExpenseItAsyncRequestTask;
import com.concur.mobile.platform.util.Const;
import com.concur.platform.ExpenseItProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

public class LoginExpenseItTask extends ExpenseItAsyncRequestTask {

    private static final String CLS_TAG = LoginExpenseItTask.class.getSimpleName();

    // Contains the service end-point for the <code>AutoLoginV3</code> MWS call.
    private static final String SERVICE_END_POINT = "/v1/access_token";

    private static final String PARAM_IS_CONCUR_CREDENTIALS = "is_concur_credentials";

    private static final String PARAM_VALUE_TRUE = "1";

    private final String userName;

    private final String password;

    protected ExpenseItLoginResult loginResult;

    public LoginExpenseItTask(Context context, int requestId, BaseAsyncResultReceiver receiver, String userName, String password) {
        super(context, requestId, receiver);
        this.userName = userName;
        this.password = password;
    }

    private static String getBasicAuthorization(String username, String password) {
        String authString = String.format("%1$s:%2$s", username, password);
        byte data[] = null;
        try {
            data = authString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + "ApiClient.getBasicAuthorization: ", e);
        }

        String auth = null;
        if (data != null) {
            String encoded = Base64.encodeToString(data, Base64.DEFAULT);
            auth = String.format("Basic %s", encoded.trim());
        }
        return auth;
    }

    @Override
    protected String getServiceEndPoint() {
        Uri.Builder builder = Uri.parse(SERVICE_END_POINT).buildUpon();
        builder.appendQueryParameter(PARAM_IS_CONCUR_CREDENTIALS, PARAM_VALUE_TRUE);
        return builder.build().toString();
    }

    @Override
    protected String getBasicAuthorization() {
        return getBasicAuthorization(userName, password);
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
                loginResult = gson.fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"),
                    ExpenseItLoginResult.class);
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
        if (result == RESULT_OK && loginResult != null) {

            // Update the config content provider.
            ConfigUtil.updateExpenseItLoginInfo(getContext(), loginResult);

            // Update ExpenseIt properties.
            if (loginResult.getToken() != null) {
                ExpenseItProperties.setAccessToken(loginResult.getToken());
            } else {
                ExpenseItProperties.setAccessToken(null);
            }
        }
        return result;
    }

}
