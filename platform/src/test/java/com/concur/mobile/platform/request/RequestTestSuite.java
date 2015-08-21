package com.concur.mobile.platform.request;

import com.concur.mobile.platform.test.ConcurPlatformTestRunner;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.concur.mobile.platform.test.PlatformTestSuite;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Created by OlivierB on 20/08/2015.
 */
@RunWith(ConcurPlatformTestRunner.class)
@Config(manifest = "src/test/AndroidManifest.xml", assetDir = "assets")
public class RequestTestSuite extends PlatformTestSuite {

    // --- Fill those with your VM params for non-mocked runs
    private static final String TEST_SRV_URL = "dev://172.27.64.163";
    private static final String TEST_LOGIN_ID = "acsontos@outtask.com";
    private static final String TEST_LOGIN_PWD = "1111";
    // nb: add -Duse.mock.server="false" to VM options in your configuration to disable mocks

    @BeforeClass
    public static void setUp() throws Exception {
        PlatformTestSuite.setUp();

        if (!PlatformTestApplication.useMockServer()) {
            System.setProperty(Const.SERVER_ADDRESS, TEST_SRV_URL);
            System.setProperty(Const.PPLOGIN_ID, TEST_LOGIN_ID);
            System.setProperty(Const.PPLOGIN_PIN_PASSWORD, TEST_LOGIN_PWD);
        }
    }


    /**
     * Performs a group configuration test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doGroupConfiguration() throws Exception {

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
     * Performs a group configuration test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doSaveAndSubmit() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        SaveAndSubmitTaskTest saveAndSubmitTaskTest = new SaveAndSubmitTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            saveAndSubmitTaskTest.setMockServer(mwsServer);
        }

        // Run the GroupConfigurationTaskTest test.
        saveAndSubmitTaskTest.doTest();
    }
}
