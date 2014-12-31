package com.concur.mobile.core.service;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.service.parser.ResetPasswordParser;

/**
 * The base class of any ResetPasswordReset subclasses (Password, Mobile Password, etc).
 * 
 * @author westonw
 * 
 */
public abstract class BaseResetPassword extends CoreAsyncRequestTask {

    public final static String LOGIN_ID = "password.login_id";
    public final static String MIN_LENGTH = "password.min_length";
    public final static String REQUIRES_MIXED_CASE = "password.mixed_case";
    public final static String REQUIRES_NON_ALPHANUM = "password.non_alphanum";

    protected ResetPasswordParser resetPasswordParser; // Built in Parse()

    public BaseResetPassword(Context context, int id, BaseAsyncResultReceiver receiver) {
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

        resultData.putBoolean(IS_SUCCESS, resetPasswordParser.isSuccess());
        resultData.putString(ERROR_MESSAGE, resetPasswordParser.getErrorMessage());

        if (resetPasswordParser.isSuccess()) {
            // Return the correct login ID
            resultData.putString(LOGIN_ID, resetPasswordParser.getLoginId());
        } else {
            // Return the pin requirement fields
            Integer minLength = resetPasswordParser.getMinLength();
            Boolean reqMixedCase = resetPasswordParser.getRequiresMixedCase();
            Boolean reqNonAlphanum = resetPasswordParser.getRequiresNonAlphaNumeric();

            if (minLength != null) {
                resultData.putInt(MIN_LENGTH, minLength);
            }

            if (reqMixedCase != null) {
                resultData.putBoolean(REQUIRES_MIXED_CASE, reqMixedCase);
            }

            if (reqNonAlphanum != null) {
                resultData.putBoolean(REQUIRES_NON_ALPHANUM, reqNonAlphanum);
            }
        }

        return super.onPostParse();
    }

}
