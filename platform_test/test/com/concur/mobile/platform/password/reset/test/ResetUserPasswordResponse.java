package com.concur.mobile.platform.password.reset.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Provides a model of a <code>ResetUserPassword</code> request.
 * 
 * @author andrewk
 */
@Root(name = "ResetUserPasswordResult")
public class ResetUserPasswordResponse {

    @Element(name = "LoginId", required = false)
    public String loginId;

    @Element(name = "MinLength", required = false)
    public Integer minLength;

    @Element(name = "RequiresMixedCase", required = false)
    public Boolean requiresMixedCase;

    @Element(name = "ErrorMessage", required = false)
    public String errorMessage;

}
