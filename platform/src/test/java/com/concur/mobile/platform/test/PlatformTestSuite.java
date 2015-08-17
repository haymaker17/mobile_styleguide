package com.concur.mobile.platform.test;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.text.TextUtils;
import com.concur.mobile.platform.authentication.AccessToken;
import com.concur.mobile.platform.authentication.LoginResult;
import com.concur.mobile.platform.authentication.system.config.test.SystemConfigRequestTaskTest;
import com.concur.mobile.platform.authentication.test.AutoLoginRequestTaskTest;
import com.concur.mobile.platform.authentication.test.PPLoginLightRequestTaskTest;
import com.concur.mobile.platform.authentication.test.PPLoginRequestTaskTest;
import com.concur.mobile.platform.config.provider.ClearConfigDBHelper;
import com.concur.mobile.platform.config.provider.ConfigProvider;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.config.user.test.UserConfigRequestTaskTest;
import com.concur.mobile.platform.emaillookup.test.EmailLookUpRequestTaskTest;
import com.concur.mobile.platform.expense.list.test.ExpenseListRequestTaskTest;
import com.concur.mobile.platform.expense.list.test.SaveMobileEntryRequestTaskTest;
import com.concur.mobile.platform.expense.provider.ClearExpenseDBHelper;
import com.concur.mobile.platform.expense.provider.ExpenseProvider;
import com.concur.mobile.platform.expense.receipt.ocr.test.StartOCRRequestTaskTest;
import com.concur.mobile.platform.expense.receipt.ocr.test.StopOCRRequestTaskTest;
import com.concur.mobile.platform.expense.smartexpense.list.test.SmartExpenseListRequestTaskTest;
import com.concur.mobile.platform.password.reset.test.RequestPasswordResetRequestTaskTest;
import com.concur.mobile.platform.password.reset.test.ResetUserPasswordRequestTaskTest;
import com.concur.mobile.platform.provider.ClearSQLiteOpenHelper;
import com.concur.mobile.platform.provider.PlatformSQLiteOpenHelper;
import com.concur.mobile.platform.receipt.list.test.DeleteReceiptRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.GetReceiptRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.ReceiptListRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.SaveReceiptRequestTaskTest;
import com.concur.mobile.platform.request.GroupConfigurationTaskTest;
import com.concur.mobile.platform.service.MWSPlatformManager;
import com.concur.mobile.platform.test.server.MockMWSServer;
import com.concur.mobile.platform.travel.provider.ClearTravelDBHelper;
import com.concur.mobile.platform.travel.provider.TravelProvider;
import com.concur.platform.PlatformProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowLog;

import java.util.Locale;

/**
 * Contains a suite of tests to run to exercise the ConcurPlatform project.
 *
 * @author andrewk
 */
@RunWith(ConcurPlatformTestRunner.class) @Config(manifest = "src/test/AndroidManifest.xml", assetDir = "assets")
public class PlatformTestSuite {

    private static final String CLS_TAG = "PlatformTestSuite";

    private static final Boolean DEBUG = Boolean.FALSE;

    // Contains whether or not the mock server has been initialized.
    private static boolean mockServerInitialized = Boolean.FALSE;

    // Contains a reference to the mock MWS server.
    private static MockMWSServer mwsServer;

    /**
     * Performs any test suite set-up.
     */
    @BeforeClass public static void setUp() throws Exception {

        // Initialize the shadow log to use stdout.
        ShadowLog.stream = System.out;

        if (DEBUG) {
            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".setUp: ");
        }

    }

    /**
     * Performs any test suite clean-up.
     */
    @AfterClass public static void cleanUp() throws Exception {

        if (DEBUG) {
            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".cleanUp: ");
        }

        // Shut-down the mock MWS server.
        if (mwsServer != null) {
            mwsServer.stop();
        }
    }

    /**
     * Performs a pin-password login test.
     *
     * @throws Exception throws an exception if the login request fails to properly parse the results.
     */
    @Test public void doPinPasswordLogin() throws Exception {

        // Init and perform a PP login.
        PPLoginRequestTaskTest test = new PPLoginRequestTaskTest();

        // Set login credentials.
        String ppLoginId;
        String ppLoginPinPassword;

        // If using the mock server, then
        if (PlatformTestApplication.useMockServer()) {

            // Using mock-server, doesn't matter what the credentials
            // actually are!
            ppLoginId = "ahuser40@utest.com";
            ppLoginPinPassword = "collective0";

            // Init mock server.
            initMockServer();

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
     * @throws Exception throws an exception if the login request fails to properly parse the results.
     */
    @Test public void doAutoLogin() throws Exception {

        // Run the pin/password light test.
        doPinPasswordLoginLight();

        // Init the auto-login request and optionally set the mock server.
        AutoLoginRequestTaskTest autoLoginTest = new AutoLoginRequestTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            autoLoginTest.setMockServer(mwsServer);
        }

        // Run the AutoLogin test.
        autoLoginTest.doTest();
    }

    /**
     * Performs a pin-password "light" login test.
     *
     * @throws Exception throws an exception if the login request fails to properly parse the results.
     */
    @Test public void doPinPasswordLoginLight() throws Exception {

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

            // Init mock server.
            initMockServer();

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
    @Test public void doEmailLookUp() throws Exception {

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

            // Init mock server.
            initMockServer();

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
    @Test public void doRequestPasswordReset() throws Exception {

        RequestPasswordResetRequestTaskTest test = new RequestPasswordResetRequestTaskTest();

        // Set email lookup credentials.
        String email;
        Locale locale = PlatformTestApplication.getApplication().getResources().getConfiguration().locale;

        // If using the mock server, then
        if (PlatformTestApplication.useMockServer()) {

            // Using mock-server, doesn't matter what the credentials
            // actually are!
            email = "ahuser40@utest.com";

            // Init mock server.
            initMockServer();

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
    @Test public void doResetUserPassword() throws Exception {

        ResetUserPasswordRequestTaskTest test = new ResetUserPasswordRequestTaskTest();

        // Set email lookup credentials.
        String email;
        String password;
        String keyPartA;
        String keyPartB;
        Locale locale = PlatformTestApplication.getApplication().getResources().getConfiguration().locale;

        // If using the mock server, then
        if (PlatformTestApplication.useMockServer()) {

            // Using mock-server, doesn't matter what the credentials
            // actually are!
            email = "ahuser40@utest.com";
            keyPartA = "f73f4208623a4f7492bf860aef44b9db";
            keyPartB = "f73f4208623a4f7492bf860aef44b9db";
            password = "foobar";

            // Init mock server.
            initMockServer();

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
    @Test public void doSystemConfig() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        SystemConfigRequestTaskTest sysConfigTask = new SystemConfigRequestTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            sysConfigTask.setMockServer(mwsServer);
        }

        // Run the SystemConfigTest test.
        sysConfigTask.doTest();
    }

    /**
     * Performs a User Config test.
     *
     * @throws Exception throws an exception if the user config fails to properly parse the results.
     */
    @Test public void doUserConfig() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        UserConfigRequestTaskTest userConfigTask = new UserConfigRequestTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            userConfigTask.setMockServer(mwsServer);
        }

        // Run the SystemConfigTest test.
        userConfigTask.doTest();
    }

    /**
     * Performs an expense list test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test public void doExpenseList() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        ExpenseListRequestTaskTest expenseListTest = new ExpenseListRequestTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            expenseListTest.setMockServer(mwsServer);
        }

        // Run the ExpenseListRequestTask test.
        expenseListTest.doTest();
    }

    /**
     * Performs a smart expense list test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test public void doSmartExpenseList() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        SmartExpenseListRequestTaskTest smartExpenseListTest = new SmartExpenseListRequestTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            smartExpenseListTest.setMockServer(mwsServer);
        }

        // Run the ExpenseListRequestTask test.
        smartExpenseListTest.doTest();
    }

    /**
     * Performs a group configuration test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test public void doGroupConfiguration() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        GroupConfigurationTaskTest groupConfigurationTaskTest = new GroupConfigurationTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            groupConfigurationTaskTest.setMockServer(mwsServer);
        }

        // Run the GroupConfigurationTaskTest test.
        groupConfigurationTaskTest.doTest();
    }

    /**
     * Performs a StartOCR test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test public void doStartOcr() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        StartOCRRequestTaskTest startOcrTest = new StartOCRRequestTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            startOcrTest.setMockServer(mwsServer);
        }

        // First, get the list of Receipts so we can grab one of them to StartOCR on.
        ReceiptListRequestTaskTest receiptListTest = new ReceiptListRequestTaskTest();
        boolean useMockServer = PlatformTestApplication.useMockServer();
        if (useMockServer) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            receiptListTest.setMockServer(mwsServer);
        }

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
    @Test public void doStopOcr() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        StopOCRRequestTaskTest stopOcrTask = new StopOCRRequestTaskTest();
        // Init mock server.
        initMockServer();
        // Set the mock server instance on the test.
        stopOcrTask.setMockServer(mwsServer);

        // First, get the list of Receipts so we can grab one of them to StartOCR on.
        ReceiptListRequestTaskTest receiptListTest = new ReceiptListRequestTaskTest();
        // Init mock server.
        initMockServer();
        // Set the mock server instance on the test.
        receiptListTest.setMockServer(mwsServer);

        // Run the ReceiptListRequestTask test.
        receiptListTest.doTest();

        // Run the StartOCRRequestTaskTest test on the receipt we just saved.
        stopOcrTask.doTestSuccess();
        stopOcrTask.doTestFailure();
    }

    /**
     * Performs a receipt list test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test public void doReceiptList() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        ReceiptListRequestTaskTest receiptListTest = new ReceiptListRequestTaskTest();
        boolean useMockServer = PlatformTestApplication.useMockServer();
        if (useMockServer) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            receiptListTest.setMockServer(mwsServer);
        }

        // Run the ReceiptListRequestTask test.
        receiptListTest.doTest();

        // Run the GetReceiptRequestTask test with a receipt image id.
        GetReceiptRequestTaskTest getReceiptTest = new GetReceiptRequestTaskTest(
                GetReceiptRequestTaskTest.ReceiptIdSource.SOURCE_ID);
        if (useMockServer) {
            getReceiptTest.setMockServer(mwsServer);
        }
        getReceiptTest.doTest();

        // Run the GetReceiptRequestTask test with a receipt Uri.
        getReceiptTest = new GetReceiptRequestTaskTest(GetReceiptRequestTaskTest.ReceiptIdSource.SOURCE_URI);
        if (useMockServer) {
            getReceiptTest.setMockServer(mwsServer);
        }
        getReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with just a receipt Uri.
        SaveReceiptRequestTaskTest saveReceiptTest = new SaveReceiptRequestTaskTest(
                SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_URI);
        if (useMockServer) {
            saveReceiptTest.setMockServer(mwsServer);
        }
        // The Roboelectric ContentResolver current throws an UnsupportedException upon attempting to read
        // from a content Uri input stream!
        // saveReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with an input stream.
        saveReceiptTest = new SaveReceiptRequestTaskTest(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_INPUT_STREAM);
        if (useMockServer) {
            saveReceiptTest.setMockServer(mwsServer);
        }
        saveReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with a byte array.
        saveReceiptTest = new SaveReceiptRequestTaskTest(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_BYTE_ARRAY);
        if (useMockServer) {
            saveReceiptTest.setMockServer(mwsServer);
        }
        saveReceiptTest.doTest();

        // Run the DeleteReceiptRequestTask with receipt uri.
        DeleteReceiptRequestTaskTest deleteReceiptTest = new DeleteReceiptRequestTaskTest(
                DeleteReceiptRequestTaskTest.ReceiptSource.SOURCE_URI);
        if (useMockServer) {
            deleteReceiptTest.setMockServer(mwsServer);
        }
        deleteReceiptTest.doTest();

        // Run the DeleteReceiptRequestTask with a receipt image id.
        deleteReceiptTest = new DeleteReceiptRequestTaskTest(
                DeleteReceiptRequestTaskTest.ReceiptSource.SOURCE_RECEIPT_IMAGE_ID);
        if (useMockServer) {
            deleteReceiptTest.setMockServer(mwsServer);
        }
        deleteReceiptTest.doTest();

    }

    /**
     * Performs save mobile entry test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test public void doSaveMobileEntry() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        SaveMobileEntryRequestTaskTest saveMETest = new SaveMobileEntryRequestTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            saveMETest.setMockServer(mwsServer);
        }

        // Run the SaveMobileEntryRequestTask test.
        saveMETest.doTest();
    }

    /**
     * Will initialize the config and travel and expense content provider authority.
     */
    private static void initContentProvidersAuthority() {
        //set up authority for content providers
        PlatformProperties.setConfigProviderAuthority("com.concur.platform.provider.config");
        PlatformProperties.setTravelProviderAuthority("com.concur.platform.provider.travel");
        PlatformProperties.setExpenseProviderAuthority("com.concur.platform.provider.expense");
    }
    /**
     * Will initialize the config and travel content providers.
     */
    private static void initContentProviders() {

        // Initialize the config content provider.
        ConfigProvider configProvider = new ConfigProvider() {

            @Override public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
                PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearConfigDBHelper(context));
                return helper;
            }
        };
        configProvider.onCreate();
        ShadowContentResolver
                .registerProvider(com.concur.mobile.platform.config.provider.Config.AUTHORITY, configProvider);

        initConfigLoginInfo();

        // Initialize the travel content provider.
        TravelProvider travelProvider = new TravelProvider() {

            @Override public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
                PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearTravelDBHelper(context));
                return helper;
            }
        };

        travelProvider.onCreate();
        ShadowContentResolver
                .registerProvider(com.concur.mobile.platform.travel.provider.Travel.AUTHORITY, travelProvider);

        // Initialize the expense content provider.
        ExpenseProvider expenseProvider = new ExpenseProvider() {

            @Override public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
                PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearExpenseDBHelper(context));
                return helper;
            }
        };

        expenseProvider.onCreate();
        ShadowContentResolver
                .registerProvider(com.concur.mobile.platform.expense.provider.Expense.AUTHORITY, expenseProvider);

    }

    /**
     * Initializes a test user in config.Db session table.
     */
    private static void initConfigLoginInfo() {
        //Init Session info for user
        Context context = PlatformTestApplication.getApplication();
        LoginResult loginResult = new LoginResult();
        loginResult.userId = "allroles@ccrdemo.com";
        loginResult.serverUrl = "www.concursolutions.com";
        loginResult.userId = "gWnB$s4gQVrnBamNOI$sNB0Eoq54BXExKsAHw";
        loginResult.accessToken = new AccessToken();
        loginResult.accessToken.key = "AOai6mA9MM26vxtFb9DOx+exX/4=";
        ConfigUtil.updateLoginInfo(context, loginResult);
    }

    /**
     * Will initalize the mock server.
     */
    private static void initMockServer() throws Exception {
        // Short-circuit of the platform has already been inited.
        if (mockServerInitialized) {
            return;
        } else {
            mockServerInitialized = true;
        }

        // Initialize the mock MWS server.
        mwsServer = new MockMWSServer();
        mwsServer.start();
    }

    /**
     * Will initialize the platform properties.
     */
    private static void initPlatformProperties() {

        Application app = PlatformTestApplication.getApplication();

        // Set up platform properties.

        // Set the server name.
        if (PlatformTestApplication.useMockServer()) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("dev://");
            strBldr.append(MockMWSServer.ADDRESS);
            strBldr.append(":");
            strBldr.append(MockMWSServer.PORT);
            PlatformProperties.setServerAddress(strBldr.toString());
        } else {
            String serverAddr = System.getProperty(Const.SERVER_ADDRESS, Const.DEFAULT_SERVER_ADDRESS);
            //serverAddr = Format.formatServerAddress(!Format.isDevServer(serverAddr), serverAddr);
            /* You can't format server address here, because PlatformAsyncRequestTask will force it to https
             * if it's not starting with dev://, which will end up in a certificate error
             */

            PlatformProperties.setServerAddress(serverAddr);
        }

        // Initialize the user-agent http header information.
        StringBuilder ua = new StringBuilder("ConcurPlatformTest/");
        String versionName;
        try {
            versionName = app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
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
