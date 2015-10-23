/**
 * This class is inspired from Platfomr project ConcurPlatformTestRunner
 */
package com.concur.mobile.platform.ui.travel.test;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.bytecode.ClassInfo;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * An extension of <code>RobolectricTestRunner</code> for the purpose of providing further customizations.
 *
 * @author ratank
 */
public class ConcurPlatformUITravelTestRunner extends RobolectricTestRunner {

    public ConcurPlatformUITravelTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    public static void startFragment(Fragment fragment) {
        FragmentManager fragmentManager = new Activity().getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment, null);
        fragmentTransaction.commit();
    }

    public static String getResourceString(int resourceId) {
        return PlatformUITravelTestApplication.getApplication().getApplicationContext().getString(resourceId);
    }

    public static Drawable getResourceDrawable(int resourceId) {
        return PlatformUITravelTestApplication.getApplication().getApplicationContext().getResources().getDrawable(resourceId);
    }

    public static void assertViewIsVisible(View view) {
        assertNotNull(view);
        assertEquals(view.getVisibility(), View.VISIBLE);
    }

    public static void assertViewIsHidden(View view) {
        assertNotNull(view);
        assertEquals(view.getVisibility(), View.GONE);
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

//
//    @Override
//    public void beforeTest(Method method) {
//        super.beforeTest(method);
//        // setup the environment expected by all tests
//        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
//    }
//
//    @Override
//    public void afterTest(Method method) {
//        super.afterTest(method);
//        // cleanup anything that might have been modified by a test
//        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_UNMOUNTED);
//    }

}
