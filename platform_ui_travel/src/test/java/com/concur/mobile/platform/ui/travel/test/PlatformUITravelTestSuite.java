package com.concur.mobile.platform.ui.travel.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

/**
 * Contains a suite of tests to run to exercise the ConcurPlatformUITravel project.
 *
 * @author ratank
 */
@RunWith(ConcurPlatformUITravelTestRunner.class) @Config(manifest = "src/test/AndroidManifest.xml", assetDir = "assets")
public class PlatformUITravelTestSuite {

    private static final String CLS_TAG = "PlatformUITravelTestSuite";

    private static final Boolean DEBUG = Boolean.FALSE;

    // Contains whether or not the mock server has been initialized.
    private static boolean mockServerInitialized = Boolean.FALSE;

    // Contains a reference to the mock MWS server.
    //private static MockMWSServer mwsServer;

    /**
     * Performs any test suite set-up.
     */
    @BeforeClass public static void setUp() throws Exception {

        // Initialize the shadow log to use stdout.
        ShadowLog.stream = System.out;

        if (DEBUG) {
            //ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".setUp: ");
            ShadowLog.d("", CLS_TAG + ".setUp: ");
        }

    }

    /**
     * Performs any test suite clean-up.
     */
    @AfterClass public static void cleanUp() throws Exception {

        if (DEBUG) {
            //ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".cleanUp: ");
            ShadowLog.d("", CLS_TAG + ".cleanUp: ");
        }

        // Shut-down the mock MWS server.
        //        if (mwsServer != null) {
        //            mwsServer.stop();
        //        }
    }

    @Test
    public void doViewUtil() throws Exception {

    }
}
