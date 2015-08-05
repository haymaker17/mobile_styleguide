package com.concur.mobile.platform.ui.travel.test;

import android.app.Application;
//import com.concur.mobile.platform.test.Const;
//import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>Application</code> representing the platform ui travel test application.
 * 
 * @author ratank
 */
public class PlatformUITravelTestApplication extends Application {

    // Contains a reference to the application.
    private static PlatformUITravelTestApplication app;

    // Contains whether or not a mock MWS server instance should be used.
    private static final boolean useMockServer;

    static {
        useMockServer = true;//Parse.safeParseBoolean(System.getProperty(Const.USE_MOCK_SERVER, "true"));
    }

    @Override
    public void onCreate() {

        super.onCreate();

        PlatformUITravelTestApplication.app = this;

    }

    /**
     * Gets a reference to the application object.
     * 
     * @return returns a reference to the application object.
     */
    public static PlatformUITravelTestApplication getApplication() {
        return PlatformUITravelTestApplication.app;
    }

    /**
     * Gets whether or not a mock server should be used.
     * 
     * @return returns whether or not a mock server should be used.
     */
    public static boolean useMockServer() {
        return PlatformUITravelTestApplication.app.useMockServer;
    }

}
