/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.test;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.concur.mobile.platform.authentication.AccessToken;
import com.concur.mobile.platform.authentication.LoginResult;
import com.concur.mobile.platform.config.provider.ClearConfigDBHelper;
import com.concur.mobile.platform.config.provider.ConfigProvider;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.provider.ClearExpenseDBHelper;
import com.concur.mobile.platform.expense.provider.ExpenseProvider;
import com.concur.mobile.platform.provider.ClearSQLiteOpenHelper;
import com.concur.mobile.platform.provider.PlatformSQLiteOpenHelper;
import com.concur.mobile.platform.service.MWSPlatformManager;
import com.concur.mobile.platform.test.server.MockServer;
import com.concur.mobile.platform.travel.provider.ClearTravelDBHelper;
import com.concur.mobile.platform.travel.provider.TravelProvider;
import com.concur.mobile.platform.util.Format;
import com.concur.platform.PlatformProperties;

import org.robolectric.shadows.ShadowContentResolver;

public abstract class PlatformTestUtil {

    // Contains whether or not the mock server has been initialized.
    private boolean mockServerInitialized = Boolean.FALSE;

    // Contains a reference to the mock MWS server.
    protected MockServer server;

    protected abstract boolean useMockServer();

    /**
     * Will initialize the config and travel content providers.
     */
    protected void initContentProviders() {

        // Initialize the config content provider.
        ConfigProvider configProvider = new ConfigProvider() {
            @Override
            public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
                PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearConfigDBHelper(context));
                return helper;
            }
        };

        configProvider.onCreate();
        ShadowContentResolver.registerProvider(com.concur.mobile.platform.config.provider.Config.AUTHORITY,
            configProvider);

        initConfigLoginInfo();

        // Initialize the travel content provider.
        TravelProvider travelProvider = new TravelProvider() {
            @Override
            public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
                PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearTravelDBHelper(context));
                return helper;
            }
        };

        travelProvider.onCreate();
        ShadowContentResolver.registerProvider(com.concur.mobile.platform.travel.provider.Travel.AUTHORITY,
            travelProvider);

        // Initialize the expense content provider.
        ExpenseProvider expenseProvider = new ExpenseProvider() {
            @Override
            public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
                PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearExpenseDBHelper(context));
                return helper;
            }
        };

        expenseProvider.onCreate();
        ShadowContentResolver.registerProvider(com.concur.mobile.platform.expense.provider.Expense.AUTHORITY,
            expenseProvider);


    }

    /**
     * Initializes a test user in config.Db session table.
     */
    private void initConfigLoginInfo() {
        //Init Session info for user
        Context context = PlatformTestApplication.getApplication();
        LoginResult loginResult = new LoginResult();
        loginResult.userId = "allroles@ccrdemo.com";
        loginResult.serverUrl = "www.concursolutions.com";
        loginResult.userId = "gWnB$s4gQVrnBamNOI$sNB0Eoq54BXExKsAHw";
        loginResult.accessToken = new AccessToken();
        loginResult.accessToken.key = "AOai6mA9MM26vxtFb9DOx+exX/4=";
        ConfigUtil.updateLoginInfo(context, loginResult);
    }

    /**
     * Will initalize the mock server.
     */
    protected void initMockServer(MockServer mockServer) throws Exception {
        // Short-circuit of the platform has already been inited.
        if (mockServerInitialized) {
            return;
        } else {
            mockServerInitialized = true;
        }

        // Initialize the mock MWS server.
        server = mockServer;
        server.start();
    }


    /**
     * Will initialize the platform properties.
     */
    protected void initPlatformProperties() {

        Application app = PlatformTestApplication.getApplication();

        // Set up platform properties.

        // Set the server name.
        if (useMockServer()) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("dev://");
            strBldr.append(server.getAddress());
            strBldr.append(":");
            strBldr.append(server.getPort());
            PlatformProperties.setServerAddress(strBldr.toString());
        } else {
            String serverAddr = System.getProperty(Const.SERVER_ADDRESS, Const.DEFAULT_SERVER_ADDRESS);
            serverAddr = Format.formatServerAddress(!Format.isDevServer(serverAddr), serverAddr);
            PlatformProperties.setServerAddress(serverAddr);
        }

        // Initialize the user-agent http header information.
        StringBuilder ua = new StringBuilder("ConcurPlatformTest/");
        String versionName;
        try {
            versionName = app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "0.0.0";
        }
        ua.append(versionName);
        ua.append(" (Android, ").append(Build.MODEL).append(", ").append(Build.VERSION.RELEASE).append(")");
        String userAgent = ua.toString();
        PlatformProperties.setUserAgent(userAgent);

        MWSPlatformManager mwsPlatMngr = new MWSPlatformManager();

        // Set the platform session manager.
        PlatformProperties.setPlatformSessionManager(mwsPlatMngr);

        // Set the auto-login setting.
        mwsPlatMngr.setAutoLoginEnabled(false);

        // Initialize any session/token information.
        PlatformProperties.setAccessToken(null);
        PlatformProperties.setSessionId(null);
    }

}
