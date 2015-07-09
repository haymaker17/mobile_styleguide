/**
 * 
 */
package com.concur.mobile.platform.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.bytecode.ClassInfo;
/**
 * An extension of <code>RobolectricTestRunner</code> for the purpose of providing further customizations.
 * 
 * @author andrewk
 */
public class ConcurPlatformTestRunner extends RobolectricTestRunner {

    public ConcurPlatformTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

//    @Override
//    public MySet createSetup() {
//        return new MySetup();
//    }
}

class MySetup {

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
