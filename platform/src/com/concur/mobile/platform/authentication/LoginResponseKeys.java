package com.concur.mobile.platform.authentication;

/**
 * An interface defining a set of keys containing some data specific to a login response.
 */
public interface LoginResponseKeys {

    /**
     * Contains a key to obtain a boolean value indicating whether the remote wipe flag has been set.
     */
    public static final String REMOTE_WIPE_KEY = "login.remote.wipe";

    /**
     * Contains a key to obtain a boolean value indicating whether the autologin is set.
     */
    public static final String DISABLE_AUTO_LOGIN = "login.disable.autlo.login";

}
