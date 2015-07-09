package com.concur.mobile.platform.emaillookup.test;

import org.junit.Assert;

import android.content.Context;
import android.os.Bundle;

import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.test.PlatformTestApplication;

/**
 * Provides a verification of a parsed email look-up response against data returned in a request bundle.
 * 
 * @author andrewk
 */
public class VerifyEmailLookUpResponse {

    private static final String CLS_TAG = "VerifyEmailLookUpResponse";

    /**
     * Will verify that email look-up response information contained in <code>resultData</code> matches data stored in
     * <code>response</code>.
     * 
     * @param resultData
     *            contains the email look-up response bundle.
     * @param response
     *            contains parsed email look-up response information parsed by the test.
     * @throws Exception
     *             an exception if email look-up response information stored in <code>resultData</code> matches data stored in
     *             <code>response</code>.
     */
    public void verify(Bundle resultData, EmailLookUpResponse response) throws Exception {

        final String MTAG = CLS_TAG + ".verify";

        // First, obtain the current session information.
        Context context = PlatformTestApplication.getApplication();
        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);

        // Second, update the session information with information from 'resultData'.
        sessInfo.setLoginId(resultData.getString(EmailLookUpRequestTask.EXTRA_LOGIN_ID_KEY));
        sessInfo.setServerUrl(resultData.getString(EmailLookUpRequestTask.EXTRA_SERVER_URL_KEY));
        sessInfo.setSignInMethod(resultData.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY));
        sessInfo.setSSOUrl(resultData.getString(EmailLookUpRequestTask.EXTRA_SSO_URL_KEY));
        sessInfo.setEmail(resultData.getString(EmailLookUpRequestTask.EXTRA_EMAIL_KEY));
        boolean updatedSessionInfo = ConfigUtil.updateSessionInfo(context, sessInfo);
        Assert.assertTrue(MTAG + ": session info update", updatedSessionInfo);

        // Third, obtain session info again for verification below.
        sessInfo = ConfigUtil.getSessionInfo(context);

        // Verify login id.
        String loginId = resultData.getString(EmailLookUpRequestTask.EXTRA_LOGIN_ID_KEY);
        Assert.assertEquals(MTAG + ": (result data) login id", response.loginId, loginId);
        Assert.assertEquals(MTAG + ": (session info) login id", response.loginId, sessInfo.getLoginId());

        // Verify server url.
        String serverUrl = resultData.getString(EmailLookUpRequestTask.EXTRA_SERVER_URL_KEY);
        Assert.assertEquals(MTAG + ": (result data) server url", response.serverUrl, serverUrl);
        Assert.assertEquals(MTAG + ": (session info) server url", response.serverUrl, sessInfo.getServerUrl());

        // Verify sign-in method.
        String signInMethod = resultData.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
        Assert.assertEquals(MTAG + ": (result data) sign-in method", response.signInMethod, signInMethod);
        Assert.assertEquals(MTAG + ": (session info) sign-in method", response.signInMethod, sessInfo.getSignInMethod());

        // Verify sso url method.
        String ssoUrl = resultData.getString(EmailLookUpRequestTask.EXTRA_SSO_URL_KEY);
        Assert.assertEquals(MTAG + ": (result data) sso url", response.ssoUrl, ssoUrl);
        Assert.assertEquals(MTAG + ": (session info) sso url", response.ssoUrl, sessInfo.getSSOUrl());

        // Verify email method.
        String email = resultData.getString(EmailLookUpRequestTask.EXTRA_EMAIL_KEY);
        Assert.assertEquals(MTAG + ": (result data) email", response.email, email);
        Assert.assertEquals(MTAG + ": (session info) email", response.email, sessInfo.getEmail());
    }

}
