package com.concur.mobile.platform.request;

import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.concur.mobile.platform.test.PlatformTestSuite;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by OlivierB on 20/08/2015.
 */
public class RequestTestSuite extends PlatformTestSuite {

    // --- Fill those with your VM params for non-mocked runs
    private static final String TEST_SRV_URL = "dev://xxx.xxx.xxx.xxx";
    private static final String TEST_LOGIN_ID = "acsontos@outtask.com";
    private static final String TEST_LOGIN_PWD = "1111";
    // nb: add -Duse.mock.server="false" to VM options in your configuration to disable mocks
    private final RequestParser requestParser = new RequestParser();

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

        final GroupConfigurationTaskTest groupConfigurationTaskTest = new GroupConfigurationTaskTest(requestParser);
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
     * Performs save and submit tests.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doSaveAndSubmit() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        final SaveAndSubmitTaskTest saveAndSubmitTaskTest = new SaveAndSubmitTaskTest(requestParser);
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            saveAndSubmitTaskTest.setMockServer(mwsServer);
        }

        // Run the GroupConfigurationTaskTest test.
        saveAndSubmitTaskTest.doTest();
    }

    /**
     * Performs a recall test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doRecall() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        final RecallTaskTest recallTaskTest = new RecallTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            recallTaskTest.setMockServer(mwsServer);
        }

        // Run the GroupConfigurationTaskTest test.
        recallTaskTest.doTest();
    }


    /**
     * Performs a Request List retrieving test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doList() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        final RequestListTaskTest requestListTaskTest = new RequestListTaskTest(requestParser);
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            requestListTaskTest.setMockServer(mwsServer);
        }

        // Run the GroupConfigurationTaskTest test.
        requestListTaskTest.doTest();
    }


    /**
     * Performs a Request Detail retrieving test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doDetail() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        final RequestDetailTaskTest requestDetailTaskTest = new RequestDetailTaskTest(requestParser);
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            requestDetailTaskTest.setMockServer(mwsServer);
        }

        // Run the GroupConfigurationTaskTest test.
        requestDetailTaskTest.doTest();
    }


    /**
     * Performs a Request Location retrieving test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doLocation() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        final RequestLocationTaskTest requestLocationTaskTest = new RequestLocationTaskTest(requestParser);
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            requestLocationTaskTest.setMockServer(mwsServer);
        }

        // Run the GroupConfigurationTaskTest test.
        requestLocationTaskTest.doTest();
    }
}
