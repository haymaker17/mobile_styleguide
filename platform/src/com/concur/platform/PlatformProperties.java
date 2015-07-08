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
     * Contains a reference of expense content provider authority.
     */
    private static String expenseProviderAuthority;

    /**
     * Contains a reference of travel content provider authority.
     */
    private static String travelProviderAuthority;

    /**
     * Contains a reference of config content provider authority.
     */
    private static String configProviderAuthority;

    /**
     * Contains a reference of account type for sync adapter.
     */
    private static String accountTypeForSyncAdapter;

    /**
     * Sets the access token.
     *
     * @param accessToken contains the access token.
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
     * @param sessionId contains the session id.
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
     * @param userAgent contains the user agent.
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
     * @param serverAddress contains the server address.
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
     * for the platform.
     */
    public static PlatformManager getPlatformSessionManager() {
        return sessionManager;
    }

    /**
     * Sets the instance of <code>PlatformSessionManager</code> that is used to manage session information appropriate for the
     * platform.
     *
     * @param sessionManager contains an instance of <code>PlatformSessionManager</code> that is used to manage session information
     *                       appropriate for the platform.
     */
    public static void setPlatformSessionManager(PlatformManager sessionManager) {
        PlatformProperties.sessionManager = sessionManager;
    }


    /**
     * Get the Expense Provider Authority used in <code>Expense</code>.
     *
     * @return contains the unique authority name for expense provider
     */
    public static String getExpenseProviderAuthority() {
        return expenseProviderAuthority;
    }

    /**
     * Sets the authority name for expense provider used in <code>Expense</code>.
     *
     * @param expenseProviderAuthority contains the unique expense provider authority name.
     */
    public static void setExpenseProviderAuthority(String expenseProviderAuthority) {
        PlatformProperties.expenseProviderAuthority = expenseProviderAuthority;
    }

    /**
     * Get the Travel Provider Authority used in <code>Travel</code>.
     *
     * @return travelProviderAuthority : contains the unique authority name for travel provider
     */
    public static String getTravelProviderAuthority() {
        return travelProviderAuthority;
    }

    /**
     * Sets the authority name for travel provider used in <code>Travel</code>.
     *
     * @param travelProviderAuthority contains the unique travel provider authority name.
     */
    public static void setTravelProviderAuthority(String travelProviderAuthority) {
        PlatformProperties.travelProviderAuthority = travelProviderAuthority;
    }

    /**
     * Get the Config Provider Authority used in <code>Config</code>.
     *
     * @return contains the unique authority name for config provider
     */
    public static String getConfigProviderAuthority() {
        return configProviderAuthority;
    }

    /**
     * Sets the authority name for config provider used in <code>Config</code>.
     *
     * @param configProviderAuthority contains the unique config provider authority name.
     */
    public static void setConfigProviderAuthority(String configProviderAuthority) {
        PlatformProperties.configProviderAuthority = configProviderAuthority;
    }

    /**
     * Get the Account Type used in <code>ExpenseSyncUtils</code>.
     *
     * @return contains the unique account name sync adapter
     */
    public static String getAccountTypeForSyncAdapter() {
        return accountTypeForSyncAdapter;
    }

    /**
     * Sets the account type for expense provider used in <code>ExpenseSyncUtils</code>.
     *
     * @param accountTypeForSyncAdapter contains the unique account type name.
     */
    public static void setAccountTypeForSyncAdapter(String accountTypeForSyncAdapter) {
        PlatformProperties.accountTypeForSyncAdapter = accountTypeForSyncAdapter;
    }
}
