package com.concur.mobile.platform.authentication.test;

import org.simpleframework.xml.Element;

public class Session {

    /**
     * Contains the session id.
     */
    @Element(name = "ID")
    public String id;

    /**
     * Contains the session timeout in minutes.
     */
    @Element(name = "TimeOut")
    public Integer timeout;

    /**
     * Contains the session expiration time.
     */
    public Long expirationTime;

}
