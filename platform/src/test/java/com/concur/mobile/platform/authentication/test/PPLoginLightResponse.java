package com.concur.mobile.platform.authentication.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Provides a model of a PPLoginLight response.
 * 
 * @author andrewk
 */
@Root(name = "MWSResponse")
public class PPLoginLightResponse {

    /**
     * Contains the access token.
     */
    @Element(name = "Response")
    public LoginResult loginResult;

}
