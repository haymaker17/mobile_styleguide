/**
 * This class is inspired from Platfomr project ConcurPlatformTestRunner
 */
package com.concur.mobile.platform.ui.travel.test;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.bytecode.ClassInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * An extension of <code>RobolectricTestRunner</code> for the purpose of providing further customizations.
 * 
 * @author ratank
 */
public class ConcurPlatformUITravelTestRunner extends RobolectricTestRunner {

    public ConcurPlatformUITravelTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }
}

class MySetup {

    public boolean shouldInstrument(ClassInfo info) {

        boolean instrument =  /*super.shouldInstrument(info)*/
                 info.getName().equals("com.concur.mobile.platform.config.provider.ConfigProvider")
                || info.getName().equals("com.concur.mobile.platform.travel.provider.TravelProvider")
                || info.getName().equals("com.concur.mobile.base.service.BaseAsyncRequestTask");

        return instrument;
    }

    public Map<String, String> classNameTranslations() {
        Map<String, String> classNameMap = new HashMap<>();
        return classNameMap;
    }

}
