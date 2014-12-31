package com.concur.mobile.platform.config.user.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "FormField")
public class AttendeeColumnDefinition {

    /**
     * Contains the id.
     */
    @Element(name = "Id")
    public String id;

    /**
     * Contains the label.
     */
    @Element(name = "Label")
    public String label;

    /**
     * Contains the data type.
     */
    @Element(name = "DataType")
    public String dataType;

    /**
     * Contains the control type.
     */
    @Element(name = "CtrlType")
    public String ctrlType;

    /**
     * Contains the access type.
     */
    @Element(name = "Access")
    public String accessType;
}
