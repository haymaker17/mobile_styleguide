/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.core.util.net;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.authentication.ValidateExpenseItAsyncTask;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.platform.ExpenseItProperties;

/**
 * Handles ExpenseIt provision Validation results. 2 outcome of the call:
 * 1- Everything is good and user is provisioned, or
 * 2- User is not provisioned. We disable login and set accessToken to null.
 */
public class ProvisionExpenseItListener implements BaseAsyncRequestTask.AsyncReplyListener {

    private final static String CLS_TAG = ProvisionExpenseItListener.class.getSimpleName();

    private final Context context;


    public ProvisionExpenseItListener(Context context) {
        this.context = context;
    }

    @Override
    public void onRequestSuccess(Bundle resultData) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".ValidateExpenseItAsyncTask.onRequestSuccess is called");
        if (resultData.getBoolean(ValidateExpenseItAsyncTask.RESULT_IS_PROVISIONED)) {
            final SessionInfo expenseItSessionInfo = ConfigUtil.getExpenseItSessionInfo(context);
            ExpenseItProperties.setAccessToken(expenseItSessionInfo.getAccessToken());
            Preferences.setUserLoggedOnToExpenseIt(true);
            // TODO: WESW - Track Login Success
        } else {
            ExpenseItProperties.setAccessToken(null);
            Preferences.setUserLoggedOnToExpenseIt(false);
            // TODO: WESW - Track Login Failure
        }
    }

    @Override
    public void onRequestFail(Bundle resultData) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".ValidateExpenseItAsyncTask.onRequestFail is called");
        ExpenseItProperties.setAccessToken(null);
        Preferences.setUserLoggedOnToExpenseIt(false);
        // TODO: WESW - Track Login Failure
    }

    @Override
    public void onRequestCancel(Bundle resultData) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".ValidateExpenseItAsyncTask.onRequestCancel is called");
    }

    @Override
    public void cleanup() {
    }
}

