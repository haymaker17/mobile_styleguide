package com.concur.mobile.platform.test;

import android.text.TextUtils;

import com.concur.mobile.platform.authentication.system.config.test.SystemConfigRequestTaskTest;
import com.concur.mobile.platform.authentication.test.AutoLoginRequestTaskTest;
import com.concur.mobile.platform.authentication.test.PPLoginLightRequestTaskTest;
import com.concur.mobile.platform.config.user.test.UserConfigRequestTaskTest;
import com.concur.mobile.platform.emaillookup.test.EmailLookUpRequestTaskTest;
import com.concur.mobile.platform.expense.receipt.ocr.test.StartOCRRequestTaskTest;
import com.concur.mobile.platform.expense.receipt.ocr.test.StopOCRRequestTaskTest;
import com.concur.mobile.platform.password.reset.test.RequestPasswordResetRequestTaskTest;
import com.concur.mobile.platform.password.reset.test.ResetUserPasswordRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.ReceiptListRequestTaskTest;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

/**
 * Contains a suite of tests to run to exercise the ConcurPlatform project.
 *
 * @author andrewk
 */
public class CommonTestSuite extends PlatformTestSuite {

    private boolean loginDone = false;

    @Before
    public void configure() throws Exception {
        if (!loginDone) {
            // Init the login request
            doPinPasswordLogin();
            loginDone = true;
        }
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();
        }
    }

    @Test
    public void doPinPasswordLogin() throws Exception {
        super.doPinPasswordLogin();
    }

    /**
     * Performs a auto-login test. <br>
     * <br>
     * <b>NOTE:</b>&nbsp;&nbsp; This test will first perform a <code>PPLoginLightRequestTaskTest</code> test in order to obtain an
     * access token, then perform an <code>AutoLoginRequestTaskTest</code> with the access token to complete the login process
     * with fully-populated data.
     *
     * @throws Exception throws an exception if the login request fails to properly parse the results.
     */
    @Test
    public void doAutoLogin() throws Exception {

        // Run the pin/password light test.
        doPinPasswordLoginLight();

        // Init the auto-login request and optionally set the mock server.
        AutoLoginRequestTaskTest autoLoginTest = new AutoLoginRequestTaskTest();
        initTaskMockServer(autoLoginTest);

        // Run the AutoLogin test.
        autoLoginTest.doTest();
    }

    /**
     * Performs a pin-password "light" login test.
     *
     * @throws Exception throws an exception if the login request fails to properly parse the results.
     */
    @Test
    public void doPinPasswordLoginLight() throws Exception {

        // Init and perform a PP light login.
        PPLoginLightRequestTaskTest test = new PPLoginLightRequestTaskTest();

        // Set login credentials.
        String ppLoginId;
        String ppLoginPinPassword;

        // If using the mock server, then
        if (PlatformTestApplication.useMockServer()) {

            // Using mock-server, doesn't matter what the credentials
            // actually are!
            ppLoginId = "ahuser40@utest.com";
            ppLoginPinPassword = "collective0";

            // Set the mock server instance on the test.
            test.setMockServer(mwsServer);

        } else {
            // Using live server! Enforce specified credentials.
            ppLoginId = System.getProperty(Const.PPLOGIN_ID, "").trim();
            if (TextUtils.isEmpty(ppLoginId)) {
                throw new Exception(CLS_TAG + ".doPinPassword: using live server, no '" + Const.PPLOGIN_ID
                        + "' system property specified!");
            }
            ppLoginPinPassword = System.getProperty(Const.PPLOGIN_PIN_PASSWORD, "").trim();
            if (TextUtils.isEmpty(ppLoginPinPassword)) {
                throw new Exception(CLS_TAG + ".doPinPassword: using live server, no '" + Const.PPLOGIN_PIN_PASSWORD
                        + "' system property specified!");
            }
        }
        // Set the credentials.
        test.setCredentials(ppLoginId, ppLoginPinPassword);

        // Init content providers.
        initContentProviders();

        // Init platform props.
        initPlatformProperties();

        // Run the test.
        test.doTest();
    }

    /**
     * Performs a email look-up test.
     *
     * @throws Exception throws an exception if the email look-up request fails to properly parse the results.
     */
    @Test
    public void doEmailLookUp() throws Exception {

        // Init and perform a PP login.
        EmailLookUpRequestTaskTest test = new EmailLookUpRequestTaskTest();

        // Set email lookup credentials.
        String email;
        Locale locale = PlatformTestApplication.getApplication().getResources().getConfiguration().locale;

        // If using the mock server, then
        if (PlatformTestApplication.useMockServer()) {

            // Using mock-server, doesn't matter what the credentials
            // actually are!
            email = "ahuser40@utest.com";

            // Set the mock server instance on the test.
            test.setMockServer(mwsServer);

        } else {
            // Using live server! Enforce specified credentials.
            email = System.getProperty(Const.EMAIL_LOOKUP_EMAIL, "").trim();
            if (TextUtils.isEmpty(email)) {
                throw new Exception(CLS_TAG + ".doEmailLookUp: using live server, no '" + Const.EMAIL_LOOKUP_EMAIL
                        + "' system property specified!");
            }
        }
        // Set the credentials.
        test.setCredentials(email, locale);

        // Init content providers.
        initContentProviders();

        // Init platform props.
        initPlatformProperties();

        // Run the test.
        test.doTest();

        // Perform an auto-login request, this will establish a session.
        doAutoLogin();

        // Verify that email look-up information can be successfully updated and retrieved.
        test.verifyEmailLookUpResponse();
    }

    /**
     * Performs a request password reset test.
     *
     * @throws Exception throws an exception if the password reset request fails to properly parse the results.
     */
    @Test
    public void doRequestPasswordReset() throws Exception {

        RequestPasswordResetRequestTaskTest test = new RequestPasswordResetRequestTaskTest();

        // Set email lookup credentials.
        String email;
        Locale locale = PlatformTestApplication.getApplication().getResources().getConfiguration().locale;

        // If using the mock server, then
        if (PlatformTestApplication.useMockServer()) {

            // Using mock-server, doesn't matter what the credentials
            // actually are!
            email = "ahuser40@utest.com";

            // Set the mock server instance on the test.
            test.setMockServer(mwsServer);

        } else {
            // Using live server! Enforce specified credentials.
            email = System.getProperty(Const.RESET_PASSWORD_EMAIL, "").trim();
            if (TextUtils.isEmpty(email)) {
                throw new Exception(
                        CLS_TAG + ".doRequestPasswordReset: using live server, no '" + Const.RESET_PASSWORD_EMAIL
                                + "' system property specified!");
            }
        }
        // Set the credentials.
        test.setCredentials(email, locale);

        // Init content providers.
        initContentProviders();

        // Init platform props.
        initPlatformProperties();

        // Run the test.
        test.doTest();
    }

    /**
     * Performs a reset user password reset test.
     *
     * @throws Exception throws an exception if the password reset request fails to properly parse the results.
     */
    @Test
    public void doResetUserPassword() throws Exception {

        ResetUserPasswordRequestTaskTest test = new ResetUserPasswordRequestTaskTest();

        // Set email lookup credentials.
        String email;
        String password;
        String keyPartA;
        String keyPartB;

        // If using the mock server, then
        if (PlatformTestApplication.useMockServer()) {

            // Using mock-server, doesn't matter what the credentials
            // actually are!
            email = "ahuser40@utest.com";
            keyPartA = "f73f4208623a4f7492bf860aef44b9db";
            keyPartB = "f73f4208623a4f7492bf860aef44b9db";
            password = "foobar";

            // Set the mock server instance on the test.
            test.setMockServer(mwsServer);

        } else {
            // Using live server! Enforce specified credentials.
            email = System.getProperty(Const.RESET_PASSWORD_EMAIL, "").trim();
            if (TextUtils.isEmpty(email)) {
                throw new Exception(
                        CLS_TAG + ".doResetUserPassword: using live server, no '" + Const.RESET_PASSWORD_EMAIL
                                + "' system property specified!");
            }
            password = System.getProperty(Const.RESET_PASSWORD_PASSWORD, "").trim();
            if (TextUtils.isEmpty(password)) {
                throw new Exception(
                        CLS_TAG + ".doResetUserPassword: using live server, no '" + Const.RESET_PASSWORD_PASSWORD
                                + "' system property specified!");
            }
            keyPartA = System.getProperty(Const.RESET_PASSWORD_KEY_PART_A, "").trim();
            if (TextUtils.isEmpty(email)) {
                throw new Exception(
                        CLS_TAG + ".doResetUserPassword: using live server, no '" + Const.RESET_PASSWORD_KEY_PART_A
                                + "' system property specified!");
            }
            keyPartB = System.getProperty(Const.RESET_PASSWORD_KEY_PART_B, "").trim();
            if (TextUtils.isEmpty(email)) {
                throw new Exception(
                        CLS_TAG + ".doResetUserPassword: using live server, no '" + Const.RESET_PASSWORD_KEY_PART_B
                                + "' system property specified!");
            }
        }
        // Set the credentials.
        test.setCredentials(email, keyPartA, keyPartB, password);

        // Init content providers.
        initContentProviders();

        // Init platform props.
        initPlatformProperties();

        // Run the test.
        test.doTest();

    }

    /**
     * Performs a System Config test.
     *
     * @throws Exception throws an exception if the system config fails to properly parse the results.
     */
    @Test
    public void doSystemConfig() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        SystemConfigRequestTaskTest sysConfigTask = new SystemConfigRequestTaskTest();
        initTaskMockServer(sysConfigTask);

        // Run the SystemConfigTest test.
        sysConfigTask.doTest();
    }

    /**
     * Performs a User Config test.
     *
     * @throws Exception throws an exception if the user config fails to properly parse the results.
     */
    @Test
    public void doUserConfig() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        UserConfigRequestTaskTest userConfigTask = new UserConfigRequestTaskTest();
        initTaskMockServer(userConfigTask);

        // Run the SystemConfigTest test.
        userConfigTask.doTest();
    }

    /**
     * Performs a StartOCR test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doStartOcr() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        StartOCRRequestTaskTest startOcrTest = new StartOCRRequestTaskTest();
        initTaskMockServer(startOcrTest);

        // First, get the list of Receipts so we can grab one of them to StartOCR on.
        ReceiptListRequestTaskTest receiptListTest = new ReceiptListRequestTaskTest();
        initTaskMockServer(receiptListTest);

        // Run the ReceiptListRequestTask test.
        receiptListTest.doTest();

        // Run the StartOCRRequestTaskTest test on the receipt we just saved.
        startOcrTest.doTestSuccess();
        startOcrTest.doTestFailure();
    }

    /**
     * Performs a StopOCR test.
     * <p/>
     * NOTE: This only tests against the mock server. The reason being is testing against a real live server is difficult. First,
     * we need to call StartOCR and IMMEDIATELY call StopOCR. The timing needs to be perfect in order for this call to succeed,
     * otherwise, the we're trying to stop an OCR that has already succeeded.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doStopOcr() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        StopOCRRequestTaskTest stopOcrTask = new StopOCRRequestTaskTest();
        initTaskMockServer(stopOcrTask);

        // First, get the list of Receipts so we can grab one of them to StartOCR on.
        ReceiptListRequestTaskTest receiptListTest = new ReceiptListRequestTaskTest();
        initTaskMockServer(receiptListTest);

        // Run the ReceiptListRequestTask test.
        receiptListTest.doTest();

        // Run the StartOCRRequestTaskTest test on the receipt we just saved.
        stopOcrTask.doTestSuccess();
        stopOcrTask.doTestFailure();
    }
}
