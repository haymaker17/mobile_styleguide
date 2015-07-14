package com.concur.mobile.platform.config.user.test;

import org.simpleframework.xml.Element;

public class CarType {

    /**
     * Contains code.
     */
    @Element(name = "Code", required = false)
    public String code;

    /**
     * Contains description.
     */
    @Element(name = "Description")
    public String description;

    /**
     * Contains isDefault.
     */
    @Element(name = "IsDefault", required = false)
    public String isDefault;

}
