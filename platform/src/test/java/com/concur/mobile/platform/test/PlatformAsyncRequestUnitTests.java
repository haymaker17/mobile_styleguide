package com.concur.mobile.platform.test;

import android.text.TextUtils;

import com.concur.mobile.platform.authentication.system.config.test.SystemConfigRequestTaskTest;
import com.concur.mobile.platform.authentication.test.AutoLoginRequestTaskTest;
import com.concur.mobile.platform.authentication.test.PPLoginLightRequestTaskTest;
import com.concur.mobile.platform.config.user.test.UserConfigRequestTaskTest;
import com.concur.mobile.platform.emaillookup.test.EmailLookUpRequestTaskTest;
import com.concur.mobile.platform.expense.receipt.ocr.test.StartOCRRequestTaskTest;
import com.concur.mobile.platform.expense.receipt.ocr.test.StopOCRRequestTaskTest;
import com.concur.mobile.platform.expense.smartexpense.RemoveSmartExpenseRequestTaskTest;
import com.concur.mobile.platform.expense.smartexpense.SaveSmartExpenseRequestTaskTest;
import com.concur.mobile.platform.password.reset.test.RequestPasswordResetRequestTaskTest;
import com.concur.mobile.platform.password.reset.test.ResetUserPasswordRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.ReceiptListRequestTaskTest;
import com.concur.mobile.platform.test.server.MockMWSServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.shadows.ShadowLog;

import java.util.Locale;

/**
 * Contains a suite of tests to run to exercise the ConcurPlatform project.
 * 
 * @author andrewk
 */
public class PlatformAsyncRequestUnitTests extends PlatformAsyncRequestTestUtil {

    private static final String CLS_TAG = "PlatformTestSuite";

    /**
     * Performs any test suite set-up.
     */
    @Before
    public void setUp() throws Exception {

        // Initialize the shadow log to use stdout.
        ShadowLog.stream = System.out;

        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".setUp: ");
    }

    /**
     * Performs any test suite clean-up.
     */
    @After
    public void cleanUp() throws Exception {

        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".cleanUp: ");

        // Shut-down the mock MWS server.
        if (server != null) {
            server.stop();
        }
    }

    @Override
    protected boolean useMockServer() {
        return true;
    }

    /**
     * Performs a auto-login test. <br>
     * <br>
     * <b>NOTE:</b>&nbsp;&nbsp; This test will first perform a <code>PPLoginLightRequestTaskTest</code> test in order to obtain an
     * access token, then perform an <code>AutoLoginRequestTaskTest</code> with the access token to complete the login process
     * with fully-populated data.
     * 
     * @throws Exception
     *             throws an exception if the login request fails to properly parse the results.
     */
    @Test
    public void doAutoLogin() throws Exception {

        // Run the pin/password light test.
        doPinPasswordLoginLight();

        // Init the auto-login request and optionally set the mock server.
        AsyncRequestTest autoLoginTest = new AutoLoginRequestTaskTest(useMockServer());
        if (useMockServer()) {
            autoLoginTest.setMockServer(server);
        }

        // Run the AutoLogin test.
        autoLoginTest.doTest();
    }

    /**
     * Performs a pin-password "light" login test.
     * 
     * @throws Exception
     *             throws an exception if the login request fails to properly parse the results.
     */
    @Test
    public void doPinPasswordLoginLight() throws Exception {

        // Init and perform a PP light login.
        PPLoginLightRequestTaskTest test = new PPLoginLightRequestTaskTest(useMockServer());

        // Set login credentials.
        String ppLoginId;
        String ppLoginPinPassword;

        // If using the mock server, then
        if (useMockServer()) {

            // Using mock-server, doesn't matter what the credentials
            // actually are!
            ppLoginId = "ahuser40@utest.com";
            ppLoginPinPassword = "collective0";

            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            test.setMockServer(server);

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
     * @throws Exception
     *             throws an exception if the email look-up request fails to properly parse the results.
     */
    @Test
    public void doEmailLookUp() throws Exception {

        // Init and perform a PP login.
        EmailLookUpRequestTaskTest test = new EmailLookUpRequestTaskTest(useMockServer());

        // Set email lookup credentials.
        String email;
        Locale locale = PlatformTestApplication.getApplication().getResources().getConfiguration().locale;

        // If using the mock server, then
        if (useMockServer()) {

            // Using mock-server, doesn't matter what the credentials
            // actually are!
            email = "ahuser40@utest.com";

            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            test.setMockServer(server);

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
     * @throws Exception
     *             throws an exception if the password reset request fails to properly parse the results.
     */
    @Test
    public void doRequestPasswordReset() throws Exception {

        RequestPasswordResetRequestTaskTest test = new RequestPasswordResetRequestTaskTest(useMockServer());

        // Set email lookup credentials.
        String email;
        Locale locale = PlatformTestApplication.getApplication().getResources().getConfiguration().locale;

        // If using the mock server, then
        if (useMockServer()) {

            // Using mock-server, doesn't matter what the credentials
            // actually are!
            email = "ahuser40@utest.com";

            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            test.setMockServer(server);

        } else {
            // Using live server! Enforce specified credentials.
            email = System.getProperty(Const.RESET_PASSWORD_EMAIL, "").trim();
            if (TextUtils.isEmpty(email)) {
                throw new Exception(CLS_TAG + ".doRequestPasswordReset: using live server, no '"
                        + Const.RESET_PASSWORD_EMAIL + "' system property specified!");
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
     * @throws Exception
     *             throws an exception if the password reset request fails to properly parse the results.
     */
    @Test
    public void doResetUserPassword() throws Exception {

        ResetUserPasswordRequestTaskTest test = new ResetUserPasswordRequestTaskTest(useMockServer());

        // Set email lookup credentials.
        String email;
        String password;
        String keyPartA;
        String keyPartB;
        Locale locale = PlatformTestApplication.getApplication().getResources().getConfiguration().locale;

        // If using the mock server, then
        if (useMockServer()) {

            // Using mock-server, doesn't matter what the credentials
            // actually are!
            email = "ahuser40@utest.com";
            keyPartA = "f73f4208623a4f7492bf860aef44b9db";
            keyPartB = "f73f4208623a4f7492bf860aef44b9db";
            password = "foobar";

            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            test.setMockServer(server);

        } else {
            // Using live server! Enforce specified credentials.
            email = System.getProperty(Const.RESET_PASSWORD_EMAIL, "").trim();
            if (TextUtils.isEmpty(email)) {
                throw new Exception(CLS_TAG + ".doResetUserPassword: using live server, no '"
                        + Const.RESET_PASSWORD_EMAIL + "' system property specified!");
            }
            password = System.getProperty(Const.RESET_PASSWORD_PASSWORD, "").trim();
            if (TextUtils.isEmpty(password)) {
                throw new Exception(CLS_TAG + ".doResetUserPassword: using live server, no '"
                        + Const.RESET_PASSWORD_PASSWORD + "' system property specified!");
            }
            keyPartA = System.getProperty(Const.RESET_PASSWORD_KEY_PART_A, "").trim();
            if (TextUtils.isEmpty(email)) {
                throw new Exception(CLS_TAG + ".doResetUserPassword: using live server, no '"
                        + Const.RESET_PASSWORD_KEY_PART_A + "' system property specified!");
            }
            keyPartB = System.getProperty(Const.RESET_PASSWORD_KEY_PART_B, "").trim();
            if (TextUtils.isEmpty(email)) {
                throw new Exception(CLS_TAG + ".doResetUserPassword: using live server, no '"
                        + Const.RESET_PASSWORD_KEY_PART_B + "' system property specified!");
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
     * @throws Exception
     *             throws an exception if the system config fails to properly parse the results.
     */
    @Test
    public void doSystemConfig() throws Exception {
        doTest(new SystemConfigRequestTaskTest(useMockServer()));
    }

    /**
     * Performs a User Config test.
     * 
     * @throws Exception
     *             throws an exception if the user config fails to properly parse the results.
     */
    @Test
    public void doUserConfig() throws Exception {
        doTest(new UserConfigRequestTaskTest(useMockServer()));
    }

    /**
     * Performs a StartOCR test.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    @Test
    public void doStartOcr() throws Exception {
        // First, get the list of Receipts so we can grab one of them to StartOCR on.
        doTest(new ReceiptListRequestTaskTest(useMockServer()));
        // Run the StartOCRRequestTaskTest test on the receipt we just saved.
        doTest(new StartOCRRequestTaskTest(useMockServer()));
    }

    /**
     * Performs a StopOCR test.
     * 
     * NOTE: This only tests against the mock server. The reason being is testing against a real live server is difficult. First,
     * we need to call StartOCR and IMMEDIATELY call StopOCR. The timing needs to be perfect in order for this call to succeed,
     * otherwise, the we're trying to stop an OCR that has already succeeded.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    @Test
    public void doStopOcr() throws Exception {
        // First, get the list of Receipts so we can grab one of them to StartOCR on.
        doTest(new ReceiptListRequestTaskTest(useMockServer()));
        // Run the StartOCRRequestTaskTest test on the receipt we just saved.
        doTest(new StopOCRRequestTaskTest(useMockServer()));
    }

    @Test
    public void doSaveSmartExpense() throws Exception {
        doTest(new SaveSmartExpenseRequestTaskTest(useMockServer()));
    }

    @Test
    public void doRemoveSmartExpense() throws Exception {
        doTest(new RemoveSmartExpenseRequestTaskTest(useMockServer()));
    }
}
