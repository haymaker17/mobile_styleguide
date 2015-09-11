/**
 *
 */
package com.concur.mobile.platform.test;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.ClassInfo;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

import java.util.HashMap;
import java.util.Map;

/**
 * An extension of <code>RobolectricTestRunner</code> for the purpose of providing further customizations.
 *
 * @author andrewk
 */
public class ConcurPlatformTestRunner extends RobolectricTestRunner {

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        String manifestPath = "../platform/AndroidManifest.xml";
        String resDir = "../platform/res";
        String assetsDir = "../platform/src/test/assets";

        return new AndroidManifest(Fs.fileFromPath(manifestPath), Fs.fileFromPath(resDir), Fs.fileFromPath(assetsDir));
    }

    public ConcurPlatformTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

//    @Override
//    public MySet createSetup() {
//        return new MySetup();
//    }

    private class MySetup {

        // This is the only way i found how to allow instrumentation of some classes.
        // Without this shadows are not instrumented
        public boolean shouldInstrument(ClassInfo info) {

            boolean instrument =  /*super.shouldInstrument(info)*/
                info.getName().equals("com.concur.mobile.platform.config.provider.ConfigProvider")
                    || info.getName().equals("com.concur.mobile.platform.travel.provider.TravelProvider")
                    || info.getName().equals("com.concur.mobile.base.service.BaseAsyncRequestTask")
                    || info.getName().equals("com.concur.mobile.platform.expense.provider.ExpenseProvider");

            return instrument;
        }

        public Map<String, String> classNameTranslations() {

            // Map<String, String> classNameMap = super.classNameTranslations();
            Map<String, String> classNameMap = new HashMap<>();
            return classNameMap;
        }

    }
}