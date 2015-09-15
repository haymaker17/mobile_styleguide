/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.test;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.concur.mobile.platform.authentication.test.PPLoginRequestTaskTest;
import com.concur.mobile.platform.service.MWSPlatformManager;
import com.concur.mobile.platform.test.server.MockMWSServer;
import com.concur.mobile.platform.test.server.MockServer;
import com.concur.mobile.platform.util.Format;
import com.concur.platform.PlatformProperties;

import org.junit.After;
import org.junit.Test;
import org.robolectric.shadows.ShadowLog;

public abstract class PlatformAsyncRequestTestUtil extends PlatformTestSuite {

    // Contains whether or not the mock server has been initialized.
    private boolean mockServerInitialized = Boolean.FALSE;

    // Contains a reference to the mock MWS server.
    protected MockServer server;

    protected abstract boolean useMockServer();

    @After
    public void cleanUp() throws Exception {

        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".cleanUp: ");

        // Shut-down the mock MWS server.
        if (server != null) {
            server.stop();
        }
    }

    protected void doTest(AsyncRequestTest test) throws Exception {

        if (test == null) {
            throw new IllegalArgumentException("Your test should be valid");
        }

        // Init the login request
        doPinPasswordLogin();

        if (useMockServer()) {
            // Init mock server.
            initMockServer(new MockMWSServer());

            // Set the mock server instance on the test.
            test.setMockServer(server);
        }

        // Run the SystemConfigTest test.
        test.doTest();
    }

    /**
     * Will initialize the mock server.
     */
    protected void initMockServer(MockServer mockServer) throws Exception {
        // Short-circuit of the platform has already been inited.
        if (mockServerInitialized) {
            return;
        } else {
            mockServerInitialized = true;
        }

        // Initialize the mock MWS server.
        server = mockServer;
        server.start();
    }

    /**
     * Will initialize the config and travel and expense content provider authority.
     */
    protected static void initContentProvidersAuthority() {
        //set up authority for content providers
        PlatformProperties.setConfigProviderAuthority("com.concur.platform.provider.config");
        PlatformProperties.setTravelProviderAuthority("com.concur.platform.provider.travel");
        PlatformProperties.setExpenseProviderAuthority("com.concur.platform.provider.expense");
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
