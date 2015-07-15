package com.concur.mobile.platform.emaillookup.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * Provides a model of an email lookup response.
 * 
 * @author andrewk
 */
@Root(name = "MWSResponse")
public class EmailLookUpResponse {

    @Path("Response")
    @Element(name = "LoginId")
    public String loginId;

    @Path("Response")
    @Element(name = "ServerUrl", required = false)
    public String serverUrl;

    @Path("Response")
    @Element(name = "SignInMethod")
    public String signInMethod;

    @Path("Response")
    @Element(name = "SsoUrl", required = false)
    public String ssoUrl;

    @Path("Response")
    @Element(name = "Email")
    public String email;

}
