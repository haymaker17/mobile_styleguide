/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.platform;

import com.concur.mobile.platform.service.ExpenseItManager;

/**
 * Provides a place to store platform property values in order to communicate with mobile web services (MWS).
 */
public class ExpenseItProperties {

    /**
     * Contains the platform property access token.
     */
    private static String accessToken;

    /**
     * Contains the platform property session ID
     */
    private static String sessionId;

    /**
     * Contains the platform property user agent.
     */
    private static String userAgent;

    /**
     * Contains the platform property server address.
     */
    private static String serverAddress;


    public static String getAppId() {
        return appId;
    }

    public static void setAppId(String appId) {
        ExpenseItProperties.appId = appId;
    }

    public static String getConsumerKey() {
        return consumerKey;
    }

    private static String appId;

    /**
     * Contains a reference to the platform session manager.
     */
    private static ExpenseItManager sessionManager;

    private static String consumerKey;

    public static void setAccessToken(String accessToken) {
        ExpenseItProperties.accessToken = accessToken;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setSessionId(String sessionId) {
        ExpenseItProperties.sessionId = sessionId;
    }

    public static void setUserAgent(String userAgent) {
        ExpenseItProperties.userAgent = userAgent;
    }

    public static void setConsumerKey(String consumerKey) {
        ExpenseItProperties.consumerKey = consumerKey;
    }

    public static String getUserAgent() {
        return userAgent;
    }

    public static String getSessionId() {
        return sessionId;
    }

    public static void setServerAddress(String serverAddress) {
        ExpenseItProperties.serverAddress = serverAddress;
    }

    public static String getServerAddress() {
        return serverAddress;
    }

    /**
     * Gets the instance of <code>PlatformSessionManager</code> that is used to manage session information appropriate for the
     * platform.
     * 
     * @return returns an instance of <code>PlatformSessionManager</code> that is used to manage session information appropriate
     *         for the platform.
     */
    public static ExpenseItManager getExpenseItSessionManager() {
        return sessionManager;
    }

    /**
     * Sets the instance of <code>PlatformSessionManager</code> that is used to manage session information appropriate for the
     * platform.
     * 
     * @param sessionManager
     *            contains an instance of <code>PlatformSessionManager</code> that is used to manage session information
     *            appropriate for the platform.
     */
    public static void setExpenseItSessionManager(ExpenseItManager sessionManager) {
        ExpenseItProperties.sessionManager = sessionManager;
    }

}
