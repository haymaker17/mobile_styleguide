package com.concur.mobile.platform.authentication.system.config.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "ReasonCode")
public class ReasonCode {

    /**
     * Contains the type.
     */
    @Element(name = "Type", required = false)
    public String type;

    /**
     * Contains the violationType.
     */
    @Element(name = "ViolationType")
    public String violationType;

    /**
     * Contains the description.
     */
    @Element(name = "Description")
    public String description;

    /**
     * Contains the id.
     */
    @Element(name = "Id")
    public Integer id;

}
