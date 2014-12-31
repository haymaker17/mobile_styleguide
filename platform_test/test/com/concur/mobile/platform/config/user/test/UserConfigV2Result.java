package com.concur.mobile.platform.config.user.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Provides a model of user config V2 response information. <br>
 */
@Root(name = "MWSResponse")
public class UserConfigV2Result {

    @Element(name = "Response")
    public UserConfigResult userConfig;
}
