package com.concur.mobile.platform.password.reset.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Provides a model of a reset password response.
 * 
 * @author andrewk
 */
@Root(name = "RequestPasswordResetResult")
public class RequestPasswordResetResponse {

    @Element(name = "KeyPart")
    public String keyPartA;

    @Element(name = "GoodPasswordDescription", required = false)
    public String goodPasswordDescription;

}
