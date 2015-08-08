package com.concur.mobile.platform.authentication.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "SiteSetting")
public class SiteSetting {

    /**
     * Contains the name.
     */
    @Element(name = "Name")
    public String name;

    /**
     * Contains the type.
     */
    @Element(name = "Type")
    public String type;

    /**
     * Contains the value.
     */
    @Element(name = "Value")
    public String value;

}
