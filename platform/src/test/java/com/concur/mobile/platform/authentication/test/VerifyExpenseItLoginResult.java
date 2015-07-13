/**
 * 
 */
package com.concur.mobile.platform.authentication.test;

import android.content.Context;

import com.concur.mobile.platform.authentication.ExpenseItLoginResult;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;

import org.junit.Assert;

/**
 * Provides a class to verify a <code>LoginResult</code> object against data stored in the content provider.
 * 
 * @author andrewk
 */
public class VerifyExpenseItLoginResult {

    private static final String CLS_TAG = VerifyExpenseItLoginResult.class.getSimpleName();

     /**
     * Will verify login response information stored in the config content provider against information stored in
     * <code>loginResult</code>.
     * 
     * @param context
     *            contains a reference to the application context.
     * @param loginResult
     *            contains a reference to a login response.
     * @throws Exception
     *             throws an exception if the stored login result data does not match <code>loginResult</code>.
     */
    public void verify(Context context, ExpenseItLoginResult loginResult) throws Exception {

        final String MTAG = CLS_TAG + ".verify";

        //Verify LoginResult is not null
        if (loginResult == null) {
            throw new Exception(CLS_TAG + "." + MTAG + ": LoginResult info is null!");
        }

        // Verify User Information.
        SessionInfo sessionInfo = ConfigUtil.getExpenseItSessionInfo(context);
        if (sessionInfo == null) {
            throw new Exception(CLS_TAG + "." + MTAG + ": user info is null!");
        }

        Assert.assertEquals(MTAG + ": Access token", sessionInfo.getAccessToken(), loginResult.getToken());

    }
}
