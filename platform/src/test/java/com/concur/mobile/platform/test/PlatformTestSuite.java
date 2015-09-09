/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.test;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.concur.mobile.platform.authentication.AccessToken;
import com.concur.mobile.platform.authentication.LoginResult;
import com.concur.mobile.platform.authentication.test.PPLoginRequestTaskTest;
import com.concur.mobile.platform.config.provider.ClearConfigDBHelper;
import com.concur.mobile.platform.config.provider.ConfigProvider;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.provider.ClearExpenseDBHelper;
import com.concur.mobile.platform.expense.provider.ExpenseProvider;
import com.concur.mobile.platform.provider.ClearSQLiteOpenHelper;
import com.concur.mobile.platform.provider.PlatformSQLiteOpenHelper;
import com.concur.mobile.platform.service.MWSPlatformManager;
import com.concur.mobile.platform.test.server.MockMWSServer;
import com.concur.mobile.platform.travel.provider.ClearTravelDBHelper;
import com.concur.mobile.platform.travel.provider.TravelProvider;
import com.concur.platform.PlatformProperties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowLog;

/**
 * Created by OlivierB on 20/08/2015.
 */
@RunWith(ConcurPlatformTestRunner.class)
@Config(application = PlatformTestApplication.class, sdk = 21)
public abstract class PlatformTestSuite {
    protected static final String CLS_TAG = "PlatformTestSuite";
    private static final Boolean DEBUG = Boolean.FALSE;
    // Contains a reference to the mock MWS server.
    protected static MockMWSServer mwsServer;
    // Contains whether or not the mock server has been initialized.
    private static boolean mockServerInitialized = Boolean.FALSE;

    /**
     * Performs any test suite set-up.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {

        // Initialize the shadow log to use stdout.
        ShadowLog.stream = System.out;

        if (DEBUG) {
            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".setUp: ");
        }

    }

    /**
     * Performs any test suite clean-up.
     */
    @AfterClass
    public static void cleanUpClass() throws Exception {

        if (DEBUG) {
            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".cleanUp: ");
        }

        // Shut-down the mock MWS server.
        if (mwsServer != null) {
            mwsServer.stop();
            mockServerInitialized = Boolean.FALSE;
        }
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
    protected static void initContentProviders() {

        // Initialize the config content provider.
        ConfigProvider configProvider = new ConfigProvider() {

            @Override
            public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
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

            @Override
            public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
                PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearTravelDBHelper(context));
                return helper;
            }
        };

        travelProvider.onCreate();
        ShadowContentResolver
                .registerProvider(com.concur.mobile.platform.travel.provider.Travel.AUTHORITY, travelProvider);

        // Initialize the expense content provider.
        ExpenseProvider expenseProvider = new ExpenseProvider() {

            @Override
            public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
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
    protected static void initMockServer() throws Exception {
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
    protected void initPlatformProperties() {

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

    /**
     * Performs a pin-password login test.
     *
     * @throws Exception throws an exception if the login request fails to properly parse the results.
     */
    public void doPinPasswordLogin() throws Exception {

        // Init and perform a PP login.
        PPLoginRequestTaskTest test = new PPLoginRequestTaskTest(PlatformTestApplication.useMockServer());

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

    protected <T extends AsyncRequestTest> void initTaskMockServer(T task) {
        if (PlatformTestApplication.useMockServer()) {
            // Set the mock server instance on the test.
            task.setMockServer(mwsServer);
        }
    }
}
