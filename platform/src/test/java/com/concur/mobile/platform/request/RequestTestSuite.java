package com.concur.mobile.platform.request;

import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.concur.mobile.platform.test.PlatformTestSuite;

import org.junit.Before;
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
    private boolean loginDone = false;

    @BeforeClass
    public static void setUp() throws Exception {
        if (!PlatformTestApplication.useMockServer()) {
            System.setProperty(Const.SERVER_ADDRESS, TEST_SRV_URL);
            System.setProperty(Const.PPLOGIN_ID, TEST_LOGIN_ID);
            System.setProperty(Const.PPLOGIN_PIN_PASSWORD, TEST_LOGIN_PWD);
        }
    }

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


    /**
     * Performs a group configuration test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doGroupConfiguration() throws Exception {

        final GroupConfigurationTaskTest groupConfigurationTaskTest = new GroupConfigurationTaskTest(PlatformTestApplication.useMockServer());
        initTaskMockServer(groupConfigurationTaskTest);

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

        final SaveAndSubmitTaskTest saveAndSubmitTaskTest = new SaveAndSubmitTaskTest(PlatformTestApplication.useMockServer());
        initTaskMockServer(saveAndSubmitTaskTest);

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

        final RecallTaskTest recallTaskTest = new RecallTaskTest(PlatformTestApplication.useMockServer());
        initTaskMockServer(recallTaskTest);

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

        final RequestListTaskTest requestListTaskTest = new RequestListTaskTest(PlatformTestApplication.useMockServer());
        initTaskMockServer(requestListTaskTest);

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

        final RequestDetailTaskTest requestDetailTaskTest = new RequestDetailTaskTest(PlatformTestApplication.useMockServer());
        initTaskMockServer(requestDetailTaskTest);

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

        final RequestLocationTaskTest requestLocationTaskTest = new RequestLocationTaskTest(PlatformTestApplication.useMockServer());
        initTaskMockServer(requestLocationTaskTest);

        // Run the GroupConfigurationTaskTest test.
        requestLocationTaskTest.doTest();
    }
}
