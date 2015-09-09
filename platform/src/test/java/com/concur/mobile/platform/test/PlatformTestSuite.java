/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.test;

import android.content.Context;

import com.concur.mobile.platform.authentication.AccessToken;
import com.concur.mobile.platform.authentication.LoginResult;
import com.concur.mobile.platform.config.provider.ClearConfigDBHelper;
import com.concur.mobile.platform.config.provider.ConfigProvider;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.provider.ClearExpenseDBHelper;
import com.concur.mobile.platform.expense.provider.ExpenseProvider;
import com.concur.mobile.platform.provider.ClearSQLiteOpenHelper;
import com.concur.mobile.platform.provider.PlatformSQLiteOpenHelper;
import com.concur.mobile.platform.travel.provider.ClearTravelDBHelper;
import com.concur.mobile.platform.travel.provider.TravelProvider;

import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

/**
 * Created by OlivierB on 20/08/2015.
 */
@RunWith(ConcurPlatformTestRunner.class)
@Config(application = PlatformTestApplication.class, sdk = 21)
public abstract class PlatformTestSuite {
    protected static final String CLS_TAG = "PlatformTestSuite";

    /**
     * Will initialize the config and travel content providers.
     */
    protected static void initContentProviders() {

        // Initialize the config content provider.
        ConfigProvider configProvider = new ConfigProvider() {

            @Override
            public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
                PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearConfigDBHelper(context));
                return helper;
            }
        };
        configProvider.onCreate();
        ShadowContentResolver
                .registerProvider(com.concur.mobile.platform.config.provider.Config.AUTHORITY, configProvider);

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
        ShadowContentResolver
                .registerProvider(com.concur.mobile.platform.travel.provider.Travel.AUTHORITY, travelProvider);

        // Initialize the expense content provider.
        ExpenseProvider expenseProvider = new ExpenseProvider() {

            @Override
            public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
                PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearExpenseDBHelper(context));
                return helper;
            }
        };

        expenseProvider.onCreate();
        ShadowContentResolver
                .registerProvider(com.concur.mobile.platform.expense.provider.Expense.AUTHORITY, expenseProvider);

    }

    /**
     * Initializes a test user in config.Db session table.
     */
    private static void initConfigLoginInfo() {
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
}
