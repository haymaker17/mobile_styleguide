package com.concur.mobile.platform.config.user.test;

import org.simpleframework.xml.Element;

public class ExpenseConfirmation {

    /**
     * Contains the key.
     */
    @Element(name = "ConfirmationKey")
    public String confirmationKey;

    /**
     * Contains the text.
     */
    @Element(name = "Text")
    public String text;

    /**
     * Contains the title.
     */
    @Element(name = "Title")
    public String title;

}
