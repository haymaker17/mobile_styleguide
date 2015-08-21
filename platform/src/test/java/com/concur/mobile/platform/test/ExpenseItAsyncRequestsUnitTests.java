/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.test;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Pair;

import com.concur.mobile.core.expenseIt.ExpenseItServerUtil;
import com.concur.mobile.platform.ExpenseIt.DeleteExpenseItReceiptTaskTest;
import com.concur.mobile.platform.ExpenseIt.ExpenseItGetImageUrlTaskTest;
import com.concur.mobile.platform.ExpenseIt.ExpenseItGetReceiptTaskTest;
import com.concur.mobile.platform.ExpenseIt.ExpenseItLoginRequestTaskTest;
import com.concur.mobile.platform.ExpenseIt.ExpenseItUploadReceiptTaskTest;
import com.concur.mobile.platform.ExpenseIt.GetExpenseItAccountInfoTaskTest;
import com.concur.mobile.platform.ExpenseIt.SetExpenseItAccountInfoTaskTest;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.test.server.MockExpenseItServer;
import com.concur.platform.ExpenseItProperties;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.shadows.ShadowLog;

public class ExpenseItAsyncRequestsUnitTests extends PlatformAsyncRequestTestUtil {

    private static final String CLS_TAG = ExpenseItAsyncRequestsUnitTests.class.getSimpleName();

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

    protected void doExpenseItTest(AsyncRequestTest test) throws Exception {

        if (test == null) {
            throw new IllegalArgumentException("Your test should be valid");
        }

        // If using the mock server, then
        if (useMockServer()) {

            // Init mock server.
            initMockServer(new MockExpenseItServer());

            // Set the mock server instance on the test.
            test.setMockServer(server);

            // Init content providers.
            initContentProviders();

            initExpenseItProperties();

        } else {
            //Test against real server, do login first
            doExpenseItServerLogin();
        }

        // Run the test.
        test.doTest();
    }

    /**
     * Note that this is a special test which needs to handle both mockServer and regular server.
     * this can be called from any other test.
     * @throws Exception
     */
    @Test
    public void doExpenseItServerLogin() throws Exception {

        // Init and perform a PP login.
        ExpenseItLoginRequestTaskTest test = new ExpenseItLoginRequestTaskTest(useMockServer());

        // Set login credentials.
        String loginId;
        String loginPinPassword;

        // If using the mock server, then
        if (useMockServer()) {
            // Using mock-server, doesn't matter what the credentials
            // actually are!
            loginId = "ahuser40@utest.com";
            loginPinPassword = "collective0";

            // Init mock server.
            initMockServer(new MockExpenseItServer());

            // Set the mock server instance on the test.
            test.setMockServer(server);
        } else {
            // Using live server! Enforce specified credentials.
            loginId = System.getProperty(Const.PPLOGIN_ID, "").trim();
            if (TextUtils.isEmpty(loginId)) {
                throw new Exception(CLS_TAG + ".doPinPassword: using live server, no '" + Const.PPLOGIN_ID
                    + "' system property specified!");
            }
            loginPinPassword = System.getProperty(Const.PPLOGIN_PIN_PASSWORD, "").trim();
            if (TextUtils.isEmpty(loginPinPassword)) {
                throw new Exception(CLS_TAG + ".doPinPassword: using live server, no '" + Const.PPLOGIN_PIN_PASSWORD
                    + "' system property specified!");
            }
        }
        // Set the credentials.
        test.setCredentials(loginId, loginPinPassword);

        // Init content provider authority
        initContentProvidersAuthority();

        // Init content providers.
        initContentProviders();

        initExpenseItProperties();
        // Run the test.
        test.doTest();
    }

    /**
     * Initialize the ExpenseIt properties used to authenticate ExpenseIt services
     */
    private void initExpenseItProperties() {

        Application app = PlatformTestApplication.getApplication();

        // Set the server name.
        if (useMockServer()) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("dev://");
            strBldr.append(server.getAddress());
            strBldr.append(":");
            strBldr.append(server.getPort());
            ExpenseItProperties.setServerAddress(strBldr.toString());
            ExpenseItProperties.setConsumerKey("ExpenseItConsumerKey");
            ExpenseItProperties.setAppId("ExpenseItMockAppId");
        } else {
            String serverAddr = System.getProperty(Const.SERVER_ADDRESS, Const.DEFAULT_SERVER_ADDRESS);
            Pair<String, String> expenseItServerAddress = ExpenseItServerUtil.getMatchingConcurExpenseItServer(serverAddr);
            ExpenseItProperties.setServerAddress(expenseItServerAddress.first);
            ExpenseItProperties.setConsumerKey(expenseItServerAddress.second);
            ExpenseItProperties.setAppId(ExpenseItServerUtil.getAppId(PlatformTestApplication.getApplication()));

            //Invalidate any Expenseit login information
            SessionInfo expenseItSessionInfo = ConfigUtil.getExpenseItSessionInfo(PlatformTestApplication.getApplication());
            if (expenseItSessionInfo != null && !TextUtils.isEmpty(expenseItSessionInfo.getAccessToken())) {
                //Clear ExpenseIt Login Info
                ExpenseItProperties.setAccessToken(null);
                // Update the config content provider.
                ConfigUtil.removeExpenseItLoginInfo(PlatformTestApplication.getApplication());
            }
        }

        // set user agent
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
        ExpenseItProperties.setUserAgent(userAgent);
    }

    @Test
    public void doGetExpenseListFromExpenseIt() throws Exception {
        ExpenseItGetReceiptTaskTest test = new ExpenseItGetReceiptTaskTest(useMockServer());
        doExpenseItTest(test);
    }

    @Test
    public void doExpenseItGetImageUrlsTest() throws Exception {
        ExpenseItGetImageUrlTaskTest test = new ExpenseItGetImageUrlTaskTest(useMockServer());
        doExpenseItTest(test);
    }

    @Test
    public void doUploadImageToExpenseIt() throws Exception {
        ExpenseItUploadReceiptTaskTest test = new ExpenseItUploadReceiptTaskTest(useMockServer());
        doExpenseItTest(test);
    }

    @Test
    public void doDeleteExpenseItReceipt() throws Exception {
        ExpenseItUploadReceiptTaskTest testUpload = new ExpenseItUploadReceiptTaskTest(useMockServer());
        doExpenseItTest(testUpload);
        Long id = testUpload.getExpenseItId();

        Assert.assertNotNull("ExpenseIt Id should not be null", id);
        if (id == null) {
            return;
        }

        DeleteExpenseItReceiptTaskTest testDelete = new DeleteExpenseItReceiptTaskTest(useMockServer());
        testDelete.setExpenseId(id);
        doExpenseItTest(testDelete);
    }

    @Test
    public void doGetAccountInfo() throws Exception {
        GetExpenseItAccountInfoTaskTest test = new GetExpenseItAccountInfoTaskTest(useMockServer());
        doExpenseItTest(test);
    }

    @Test
    public void doSetAccountInfo() throws Exception {
        SetExpenseItAccountInfoTaskTest test = new SetExpenseItAccountInfoTaskTest(useMockServer());
        doExpenseItTest(test);
    }
}
