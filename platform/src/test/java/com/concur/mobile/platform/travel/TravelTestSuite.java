package com.concur.mobile.platform.travel;

import com.concur.mobile.platform.test.ConcurPlatformTestRunner;
import com.concur.mobile.platform.test.PlatformTestSuite;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Created by OlivierB on 20/08/2015.
 */
@RunWith(ConcurPlatformTestRunner.class)
@Config(manifest = "src/test/AndroidManifest.xml", assetDir = "assets")
public class TravelTestSuite extends PlatformTestSuite {

    @Test
    public void doXyz() throws Exception {
    }
}
