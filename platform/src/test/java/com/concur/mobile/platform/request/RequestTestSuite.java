package com.concur.mobile.platform.request;

import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformAsyncRequestTestUtil;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by OlivierB on 20/08/2015.
 */
public class RequestTestSuite extends PlatformAsyncRequestTestUtil {

    // --- Fill those with your VM params for non-mocked runs
    private static final String TEST_SRV_URL = "dev://xxx.xxx.xxx.xxx";
    private static final String TEST_LOGIN_ID = "acsontos@outtask.com";
    private static final String TEST_LOGIN_PWD = "1111";

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty(Const.SERVER_ADDRESS, TEST_SRV_URL);
        System.setProperty(Const.PPLOGIN_ID, TEST_LOGIN_ID);
        System.setProperty(Const.PPLOGIN_PIN_PASSWORD, TEST_LOGIN_PWD);
    }

    /**
     * Performs a group configuration test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doGroupConfiguration() throws Exception {
        doTest(new GroupConfigurationTaskTest(useMockServer()));
    }

    /**
     * Performs save and submit tests.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doSaveAndSubmit() throws Exception {
        doTest(new SaveAndSubmitTaskTest(useMockServer()));
    }

    /**
     * Performs a recall test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doRecall() throws Exception {
        doTest(new RecallTaskTest(useMockServer()));
    }

    /**
     * Performs a Request List retrieving test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doList() throws Exception {
        doTest(new RequestListTaskTest(useMockServer()));
    }

    /**
     * Performs a Request Detail retrieving test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doDetail() throws Exception {
        doTest(new RequestDetailTaskTest(useMockServer()));
    }

    /**
     * Performs a Request Location retrieving test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doLocation() throws Exception {
        doTest(new RequestLocationTaskTest(useMockServer()));
    }

    @Override
    protected boolean useMockServer() {
        return true;
    }
}
