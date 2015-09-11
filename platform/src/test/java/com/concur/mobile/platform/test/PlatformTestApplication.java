package com.concur.mobile.platform.test;

import android.app.Application;

/**
 * An extension of <code>Application</code> representing the platform test application.
 *
 * @author andrewk
 */
public class PlatformTestApplication extends Application {

    // Contains a reference to the application.
    private static PlatformTestApplication app;

    @Override
    public void onCreate() {

        super.onCreate();

        PlatformTestApplication.app = this;
        // Using live server! Enforce specified credentials.
        System.setProperty(Const.PPLOGIN_ID, "ahuser40@utest.com");
        System.setProperty(Const.PPLOGIN_PIN_PASSWORD, "collective0");
        System.setProperty(Const.RESET_PASSWORD_EMAIL, "ahuser40@utest.com");
    }

    /**
     * Gets a reference to the application object.
     *
     * @return returns a reference to the application object.
     */
    public static PlatformTestApplication getApplication() {
        return PlatformTestApplication.app;
    }
}
