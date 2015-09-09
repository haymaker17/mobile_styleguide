package com.concur.mobile.platform.test;

import android.app.Application;

import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>Application</code> representing the platform test application.
 *
 * @author andrewk
 */
public class PlatformTestApplication extends Application {

    // Contains a reference to the application.
    private static PlatformTestApplication app;

    // Contains whether or not a mock MWS server instance should be used.
    private static final boolean useMockServer;

    static {
        useMockServer = Parse.safeParseBoolean(System.getProperty(Const.USE_MOCK_SERVER, "true"));
    }

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

    /**
     * Gets whether or not a mock server should be used.
     *
     * @return returns whether or not a mock server should be used.
     */
    public static boolean useMockServer() {
        return PlatformTestApplication.app.useMockServer;
    }

}
