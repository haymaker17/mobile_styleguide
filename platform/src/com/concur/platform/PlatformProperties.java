/**
 * 
 */
package com.concur.platform;

import com.concur.mobile.platform.service.PlatformManager;

/**
 * Provides a place to store platform property values in order to communicate with mobile web services (MWS).
 */
public class PlatformProperties {

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

    /**
     * Contains a reference to the platform session manager.
     */
    private static PlatformManager sessionManager;

    /**
     * Sets the access token.
     * 
     * @param accessToken
     *            contains the access token.
     */
    public static void setAccessToken(String accessToken) {
        PlatformProperties.accessToken = accessToken;
    }

    /**
     * Gets the access token.
     * 
     * @return returns the access token.
     */
    public static String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the session id.
     * 
     * @param sessionId
     *            contains the session id.
     */
    public static void setSessionId(String sessionId) {
        PlatformProperties.sessionId = sessionId;
    }

    /**
     * Gets the session id.
     * 
     * @return returns the session id.
     */
    public static String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the user agent.
     * 
     * @param userAgent
     *            contains the user agent.
     */
    public static void setUserAgent(String userAgent) {
        PlatformProperties.userAgent = userAgent;
    }

    /**
     * Gets the user agent.
     * 
     * @return returns the user agent.
     */
    public static String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the server address.
     * 
     * @param serverAddress
     *            contains the server address.
     */
    public static void setServerAddress(String serverAddress) {
        PlatformProperties.serverAddress = serverAddress;
    }

    /**
     * Gets the server address.
     * 
     * @return contains the server address.
     */
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
    public static PlatformManager getPlatformSessionManager() {
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
    public static void setPlatformSessionManager(PlatformManager sessionManager) {
        PlatformProperties.sessionManager = sessionManager;
    }

}
