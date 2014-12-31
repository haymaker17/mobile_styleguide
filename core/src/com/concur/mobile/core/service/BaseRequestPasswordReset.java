package com.concur.mobile.core.service;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.service.parser.RequestPasswordResetParser;

public abstract class BaseRequestPasswordReset extends CoreAsyncRequestTask {

    public static final String KEY_PART = "key_part";
    public static final String GOOD_PASSWORD_DESCRIPTION = "good_password_description";

    protected RequestPasswordResetParser resetPasswordRequestParser;

    public BaseRequestPasswordReset(Context context, int id, BaseAsyncResultReceiver receiver) {
        super(context, id, receiver);
    }

    @Override
    protected abstract String getServiceEndpoint();

    @Override
    protected abstract String getPostBody();

    @Override
    protected abstract int parse(CommonParser parser);

    @Override
    protected int onPostParse() {

        resultData.putBoolean(IS_SUCCESS, resetPasswordRequestParser.isSuccess());
        resultData.putString(ERROR_MESSAGE, resetPasswordRequestParser.getErrorMessage());

        if (resetPasswordRequestParser.isSuccess()) {
            // Return the key part as well
            resultData.putString(KEY_PART, resetPasswordRequestParser.getKeyPartA());
        }

        return super.onPostParse();
    }

}
