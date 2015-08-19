package com.concur.mobile.platform.test;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.concur.mobile.platform.authentication.system.config.test.SystemConfigRequestTaskTest;
import com.concur.mobile.platform.authentication.test.AutoLoginRequestTaskTest;
import com.concur.mobile.platform.authentication.test.PPLoginLightRequestTaskTest;
import com.concur.mobile.platform.authentication.test.PPLoginRequestTaskTest;
import com.concur.mobile.platform.config.user.test.UserConfigRequestTaskTest;
import com.concur.mobile.platform.emaillookup.test.EmailLookUpRequestTaskTest;
import com.concur.mobile.platform.expense.list.test.ExpenseListRequestTaskTest;
import com.concur.mobile.platform.expense.list.test.SaveMobileEntryRequestTaskTest;
import com.concur.mobile.platform.expense.receipt.ocr.test.StartOCRRequestTaskTest;
import com.concur.mobile.platform.expense.receipt.ocr.test.StopOCRRequestTaskTest;
import com.concur.mobile.platform.expense.smartexpense.list.test.SmartExpenseListRequestTaskTest;
import com.concur.mobile.platform.password.reset.test.RequestPasswordResetRequestTaskTest;
import com.concur.mobile.platform.password.reset.test.ResetUserPasswordRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.DeleteReceiptRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.GetReceiptRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.ReceiptListRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.SaveReceiptRequestTaskTest;
import com.concur.mobile.platform.service.MWSPlatformManager;
import com.concur.mobile.platform.test.server.MockMWSServer;
import com.concur.mobile.platform.util.Format;
import com.concur.platform.PlatformProperties;

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
     * Performs a pin-password login test.
     * 
     * @throws Exception
     *             throws an exception if the login request fails to properly parse the results.
     */
    @Test
    public void doPinPasswordLogin() throws Exception {

        // Init and perform a PP login.
        PPLoginRequestTaskTest test = new PPLoginRequestTaskTest(useMockServer());

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

        // Init content provider authority
        initContentProvidersAuthority();

        // Init content providers.
        initContentProviders();

        // Init platform props.
        initPlatformProperties();

        // Run the test.
        test.doTest();
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

        // Init the login request
        doPinPasswordLogin();

        AsyncRequestTest sysConfigTask = new SystemConfigRequestTaskTest(useMockServer());
        if (useMockServer()) {
            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            sysConfigTask.setMockServer(server);
        }

        // Run the SystemConfigTest test.
        sysConfigTask.doTest();
    }

    /**
     * Performs a User Config test.
     * 
     * @throws Exception
     *             throws an exception if the user config fails to properly parse the results.
     */
    @Test
    public void doUserConfig() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        AsyncRequestTest userConfigTask = new UserConfigRequestTaskTest(useMockServer());
        if (useMockServer()) {
            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            userConfigTask.setMockServer(server);
        }

        // Run the SystemConfigTest test.
        userConfigTask.doTest();
    }

    /**
     * Performs an expense list test.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    @Test
    public void doExpenseList() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        AsyncRequestTest expenseListTest = new ExpenseListRequestTaskTest(useMockServer());
        if (useMockServer()) {
            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            expenseListTest.setMockServer(server);
        }

        // Run the ExpenseListRequestTask test.
        expenseListTest.doTest();
    }

    /**
     * Performs a smart expense list test.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    @Test
    public void doSmartExpenseList() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        AsyncRequestTest smartExpenseListTest = new SmartExpenseListRequestTaskTest(useMockServer());
        if (useMockServer()) {
            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            smartExpenseListTest.setMockServer(server);
        }

        // Run the ExpenseListRequestTask test.
        smartExpenseListTest.doTest();
    }

    /**
     * Performs a StartOCR test.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    @Test
    public void doStartOcr() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        AsyncRequestTest startOcrTest = new StartOCRRequestTaskTest(useMockServer());
        if (useMockServer()) {
            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            startOcrTest.setMockServer(server);
        }

        // First, get the list of Receipts so we can grab one of them to StartOCR on.
        AsyncRequestTest receiptListTest = new ReceiptListRequestTaskTest(useMockServer());
        if (useMockServer()) {
            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            receiptListTest.setMockServer(server);
        }

        // Run the ReceiptListRequestTask test.
        receiptListTest.doTest();

        // Run the StartOCRRequestTaskTest test on the receipt we just saved.
        startOcrTest.doTest();
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

        // Init the login request
        doPinPasswordLogin();

        AsyncRequestTest stopOcrTask = new StopOCRRequestTaskTest(useMockServer());
        // Init mock server.
        initMockServer(new MockMWSServer());
        // Set the mock server instance on the test.
        stopOcrTask.setMockServer(server);

        // First, get the list of Receipts so we can grab one of them to StartOCR on.
        AsyncRequestTest receiptListTest = new ReceiptListRequestTaskTest(useMockServer());
        // Init mock server.
        initMockServer(new MockMWSServer());
        // Set the mock server instance on the test.
        receiptListTest.setMockServer(server);

        // Run the ReceiptListRequestTask test.
        receiptListTest.doTest();

        // Run the StartOCRRequestTaskTest test on the receipt we just saved.
        stopOcrTask.doTest();
    }

    /**
     * Performs a receipt list test.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    @Test
    public void doReceiptList() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        AsyncRequestTest receiptListTest = new ReceiptListRequestTaskTest(useMockServer());
        boolean useMockServer = useMockServer();
        if (useMockServer) {
            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            receiptListTest.setMockServer(server);
        }

        // Run the ReceiptListRequestTask test.
        receiptListTest.doTest();

        // Run the GetReceiptRequestTask test with a receipt image id.
        GetReceiptRequestTaskTest getReceiptTest = new GetReceiptRequestTaskTest(useMockServer());
        getReceiptTest.setReceiptIdSource(GetReceiptRequestTaskTest.ReceiptIdSource.SOURCE_ID);
        if (useMockServer) {
            getReceiptTest.setMockServer(server);
        }
        getReceiptTest.doTest();

        // Run the GetReceiptRequestTask test with a receipt Uri.
        getReceiptTest = new GetReceiptRequestTaskTest(useMockServer());
        getReceiptTest.setReceiptIdSource(GetReceiptRequestTaskTest.ReceiptIdSource.SOURCE_URI);
        if (useMockServer) {
            getReceiptTest.setMockServer(server);
        }
        getReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with just a receipt Uri.
        SaveReceiptRequestTaskTest saveReceiptTest = new SaveReceiptRequestTaskTest(useMockServer());
        saveReceiptTest.setReceiptSource(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_URI);
        if (useMockServer) {
            saveReceiptTest.setMockServer(server);
        }
        // The Roboelectric ContentResolver current throws an UnsupportedException upon attempting to read
        // from a content Uri input stream!
        // saveReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with an input stream.
        saveReceiptTest = new SaveReceiptRequestTaskTest(useMockServer());
        saveReceiptTest.setReceiptSource(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_INPUT_STREAM);
        if (useMockServer) {
            saveReceiptTest.setMockServer(server);
        }
        saveReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with a byte array.
        saveReceiptTest = new SaveReceiptRequestTaskTest(useMockServer());
        saveReceiptTest.setReceiptSource(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_BYTE_ARRAY);
        if (useMockServer) {
            saveReceiptTest.setMockServer(server);
        }
        saveReceiptTest.doTest();

        // Run the DeleteReceiptRequestTask with receipt uri.
        DeleteReceiptRequestTaskTest deleteReceiptTest = new DeleteReceiptRequestTaskTest(useMockServer());
        deleteReceiptTest.setReceiptSource(DeleteReceiptRequestTaskTest.ReceiptSource.SOURCE_URI);
        if (useMockServer) {
            deleteReceiptTest.setMockServer(server);
        }
        deleteReceiptTest.doTest();

        // Run the DeleteReceiptRequestTask with a receipt image id.
        deleteReceiptTest = new DeleteReceiptRequestTaskTest(useMockServer());
        deleteReceiptTest.setReceiptSource(DeleteReceiptRequestTaskTest.ReceiptSource.SOURCE_RECEIPT_IMAGE_ID);
        if (useMockServer) {
            deleteReceiptTest.setMockServer(server);
        }
        deleteReceiptTest.doTest();

    }

    /**
     * Performs save mobile entry test.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    @Test
    public void doSaveMobileEntry() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        AsyncRequestTest saveMETest = new SaveMobileEntryRequestTaskTest(useMockServer());
        if (useMockServer()) {
            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            saveMETest.setMockServer(server);
        }

        // Run the SaveMobileEntryRequestTask test.
        saveMETest.doTest();
    }

    /**
     * Will initialize the platform properties.
     */
    protected void initPlatformProperties() {

        Application app = PlatformTestApplication.getApplication();

        // Set up platform properties.

        // Set the server name.
        if (useMockServer()) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("dev://");
            strBldr.append(server.getAddress());
            strBldr.append(":");
            strBldr.append(server.getPort());
            PlatformProperties.setServerAddress(strBldr.toString());
        } else {
            String serverAddr = System.getProperty(Const.SERVER_ADDRESS, Const.DEFAULT_SERVER_ADDRESS);
            serverAddr = Format.formatServerAddress(!Format.isDevServer(serverAddr), serverAddr);
            PlatformProperties.setServerAddress(serverAddr);
        }

        // Initialize the user-agent http header information.
        StringBuilder ua = new StringBuilder("ConcurPlatformTest/");
        String versionName;
        try {
            versionName = app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "0.0.0";
        }
        ua.append(versionName);
        ua.append(" (Android, ").append(Build.MODEL).append(", ").append(Build.VERSION.RELEASE).append(")");
        String userAgent = ua.toString();
        PlatformProperties.setUserAgent(userAgent);

        MWSPlatformManager mwsPlatMngr = new MWSPlatformManager();

        // Set the platform session manager.
        PlatformProperties.setPlatformSessionManager(mwsPlatMngr);

        // Set the auto-login setting.
        mwsPlatMngr.setAutoLoginEnabled(false);

        // Initialize any session/token information.
        PlatformProperties.setAccessToken(null);
        PlatformProperties.setSessionId(null);
    }

}
