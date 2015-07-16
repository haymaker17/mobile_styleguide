package com.concur.mobile.platform.authentication.test;

import org.simpleframework.xml.Element;

public class UserContact {

    /**
     * Contains the company name.
     */
    @Element(name = "CompanyName")
    public String companyName;

    /**
     * Contains the email.
     */
    @Element(name = "Email")
    public String email;

    /**
     * Contains the first name.
     */
    @Element(name = "FirstName")
    public String firstName;

    /**
     * Contains the last name.
     */
    @Element(name = "LastName")
    public String lastName;

    /**
     * Contains the middle initial.
     */
    @Element(name = "Mi", required = false)
    public String middleInitial;

}
