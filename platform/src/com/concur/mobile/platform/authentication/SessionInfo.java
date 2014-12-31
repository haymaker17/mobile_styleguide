package com.concur.mobile.platform.authentication;

/**
 * Provides an interface for obtaining session information.
 */
public interface SessionInfo {

    /**
     * Contains the session expiration window from <code>now</code> in milliseconds. This value can be added to the current time
     * in milliseconds and then compared with the value of <code>SessionInfo.getSessionTimeout</code> to determine whether the
     * session is within <code>sessionExpirationWindowFromNowMillis</code> of expiring.
     */
    public long sessionExpirationWindowFromNowMillis = (5L * 60L * 1000L);

    /**
     * Gets the access token.
     * 
     * @return returns the access token.
     */
    public String getAccessToken();

    /**
     * Sets the access token.
     * 
     * @param accessToken
     *            contains the access token.
     */
    public void setAccessToken(String accessToken);

    /**
     * Gets the authentication type, i.e, pin/password.
     * 
     * @return returns the authentication type.
     */
    public String getAuthenticationType();

    /**
     * Sets the authentication type, i.e., pin/password.
     * 
     * @param authenticationType
     *            contains the authentication type.
     */
    public void setAuthenticationType(String authenticationType);

    /**
     * Gets the session id.
     * 
     * @return returns the session id.
     */
    public String getSessionId();

    /**
     * Sets the session id.
     * 
     * @param sessionId
     *            contains the session id.
     */
    public void setSessionId(String sessionId);

    /**
     * Gets the session timeout in minutes.
     * 
     * @return returns the session timeout in minutes.
     */
    public Integer getSessionTimeout();

    /**
     * Sets the session timeout in minutes.
     * 
     * @param sessionTimeout
     *            contains the session timeout in minutes.
     */
    public void setSessionTimeout(Integer sessionTimeout);

    /**
     * Gets the session expiration time.
     * 
     * @return returns the session expiration time.
     */
    public Long getSessionExpirationTime();

    /**
     * Sets the session expiration time.
     * 
     * @param sessionExpirationTime
     *            contains the session expiration time.
     */
    public void setSessionExpirationTime(Long sessionExpirationTime);

    /**
     * Gets the login ID associated with this session.
     * 
     * @return returns the login ID associated with this session.
     */
    public String getLoginId();

    /**
     * Sets the login ID associated with this session.
     * 
     * @param loginId
     *            contains the login id.
     */
    public void setLoginId(String loginId);

    /**
     * Gets the server URL associated with this session.
     * 
     * @return returns the server URL associated with this session.
     */
    public String getServerUrl();

    /**
     * Sets the server URL associated with this session.
     * 
     * @param serverUrl
     *            contains the server URL associated with this session.
     */
    public void setServerUrl(String serverUrl);

    /**
     * Gets the sign-in method associated with this session.
     * 
     * @return returns the sign-in method associated with this session.
     */
    public String getSignInMethod();

    /**
     * Sets the sign-in method.
     * 
     * @param signInMethod
     *            contains the sign-in method.
     */
    public void setSignInMethod(String signInMethod);

    /**
     * Gets the SSO URL associated with this session.
     * 
     * @return returns the SSO URL associated with this session.
     */
    public String getSSOUrl();

    /**
     * Sets the SSO URL associated with this session.
     * 
     * @param ssoUrl
     *            contains the SSO URL associated with this session.
     */
    public void setSSOUrl(String ssoUrl);

    /**
     * Gets the EMAIL associated with this session.
     * 
     * @return returns the EMAIL associated with this session.
     */
    public String getEmail();

    /**
     * Sets the EMAIL associated with this session.
     * 
     * @param email
     *            contains the EMAIL associated with this session.
     */
    public void setEmail(String email);

    /**
     * Gets the user id.
     * 
     * @return returns the user id.
     */
    public String getUserId();

    /**
     * Sets the user id.
     * 
     * @param userId
     *            contains the user id.
     */
    public void setUserId(String userId);

}
