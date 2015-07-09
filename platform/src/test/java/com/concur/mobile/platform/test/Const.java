package com.concur.mobile.platform.test;

public class Const {

    /**
     * Contains the log tag for the test suite code.
     */
    public static final String LOG_TAG = com.concur.mobile.platform.util.Const.LOG_TAG + ".TEST";

    /**
     * Contains the system property used to determine whether the tests should run against an internal mock server (should be a
     * boolean value).
     */
    public static final String USE_MOCK_SERVER = "use.mock.server";

    /**
     * Contains the system property used to hold the server address when running tests against a live server.
     */
    public static final String SERVER_ADDRESS = "server.address";

    /**
     * Contains the default server address if 'use.mock.server=false' (or not set) and 'server.address' server property is not
     * set.
     */
    public static final String DEFAULT_SERVER_ADDRESS = "www.concursolutions.com";

    // PPLoginRequestTaskTest

    /**
     * Contains the system property used to hold the login ID for a <code>PPLogin</code> request.
     */
    public static final String PPLOGIN_ID = "pplogin.id";

    /**
     * Contains the system property used to hold the login pin/password for a <code>PPLogin</code> request.
     */
    public static final String PPLOGIN_PIN_PASSWORD = "pplogin.pin.password";

    // EmailLookUpRequestTaskTest

    /**
     * Contains the system property used to hold the email address for an <code>EmailLookUp</code> request.
     */
    public static final String EMAIL_LOOKUP_EMAIL = "email.lookup.email";

    /**
     * Contains the system property used to hold the email address for a <code>RequestPasswordReset</code> request.
     */
    public static final String RESET_PASSWORD_EMAIL = "reset.password.email";

    /**
     * Contains the system property used to hold the "part A" of the key for a <code>ResetUserPassword</code> request.
     */
    public static final String RESET_PASSWORD_KEY_PART_A = "reset.password.key.part.a";

    /**
     * Contains the system property used to hold the "part B" of the key for a <code>ResetUserPassword</code> request.
     */
    public static final String RESET_PASSWORD_KEY_PART_B = "reset.password.key.part.b";

    /**
     * Contains the system property used to hold the password for a <code>ResetUserPassword</code> request.
     */
    public static final String RESET_PASSWORD_PASSWORD = "reset.password.password";

}
