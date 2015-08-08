/**
 * 
 */
package com.concur.mobile.platform.password.reset.test;

import java.util.ArrayList;

import org.junit.Assert;

import android.os.Bundle;

import com.concur.mobile.platform.authentication.ResetUserPasswordRequestTask;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;

/**
 * Provides verification of the platform handling a <code>ResetUserPassword</code> request.
 * 
 * @author andrewk
 */
public class VerifyResetUserPasswordResponse {

    private static final String CLS_TAG = "VerifyResetUserPasswordResponse";

    /**
     * Will verify that reset password response information contained in <code>resultData</code> matches data stored in
     * <code>response</code>.
     * 
     * @param resultData
     *            contains the reset password response bundle.
     * @param response
     *            contains parsed reset password response information parsed by the test.
     * @throws Exception
     *             an exception if reset password response information stored in <code>resultData</code> does not match data
     *             stored in <code>response</code>.
     */
    public void verify(Bundle resultData, ResetUserPasswordResponse response) throws Exception {

        final String MTAG = CLS_TAG + ".verify";

        // Verify Login id.
        String loginId = resultData.getString(ResetUserPasswordRequestTask.EXTRA_LOGIN_ID_KEY);
        Assert.assertEquals(MTAG + ": (result data) login id", response.loginId, loginId);

        // Verify min length.
        Integer minLength = (resultData.containsKey(ResetUserPasswordRequestTask.EXTRA_MINIMUM_PIN_LENGTH_KEY) ? resultData
                .getInt(ResetUserPasswordRequestTask.EXTRA_MINIMUM_PIN_LENGTH_KEY) : null);
        Assert.assertEquals(MTAG + ": (result data) min pin length", response.minLength, minLength);

        // Verify requires mixed case.
        Boolean requiresMixedCase = (resultData.containsKey(ResetUserPasswordRequestTask.EXTRA_REQUIRES_MIXED_CASE_KEY) ? resultData
                .getBoolean(ResetUserPasswordRequestTask.EXTRA_REQUIRES_MIXED_CASE_KEY) : null);
        Assert.assertEquals(MTAG + ": (result data) requires mixed case", response.requiresMixedCase, requiresMixedCase);

        // Verify error message.
        ArrayList<com.concur.mobile.platform.service.parser.Error> errors = (ArrayList<com.concur.mobile.platform.service.parser.Error>) resultData
                .getSerializable(PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY);

        String errorMessage = null;
        if (errors != null && errors.size() > 0) {
            com.concur.mobile.platform.service.parser.Error error = errors.get(0);
            errorMessage = error.getSystemMessage();
        }
        Assert.assertEquals(MTAG + ": (result data) error message", response.errorMessage, errorMessage);
    }

}
