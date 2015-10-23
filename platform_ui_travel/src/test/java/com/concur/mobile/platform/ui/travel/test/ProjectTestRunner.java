package com.concur.mobile.platform.ui.travel.test;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by tejoa on 05/10/2015.
 */
public class ProjectTestRunner extends RobolectricTestRunner {
    public ProjectTestRunner(Class<?> testClass) throws InitializationError {
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