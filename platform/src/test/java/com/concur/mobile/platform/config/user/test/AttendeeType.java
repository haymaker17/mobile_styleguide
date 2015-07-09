package com.concur.mobile.platform.config.user.test;

import org.simpleframework.xml.Element;

public class AttendeeType {

    /**
     * Contains whether attendee count editing is enabled.
     */
    @Element(name = "AllowEditAtnCount")
    public String allowEditAtnCount;

    /**
     * Contains the attendee type key.
     */
    @Element(name = "AtnTypeKey")
    public String atnTypeKey;

    /**
     * Contains the attendee type code.
     */
    @Element(name = "AtnTypeCode")
    public String atnTypeCode;

    /**
     * Contains the attendee type name.
     */
    @Element(name = "AtnTypeName")
    public String atnTypeName;

    /**
     * Contains the attendee form key.
     */
    @Element(name = "FormKey")
    public String formKey;

    /**
     * Contains whether this attendee represents an external type.
     */
    @Element(name = "IsExternal")
    public String isExternal;

}
