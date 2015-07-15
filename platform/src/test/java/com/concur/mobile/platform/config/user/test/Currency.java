package com.concur.mobile.platform.config.user.test;

import org.simpleframework.xml.Element;

public class Currency {

    /**
     * Contains currency code.
     */
    @Element(name = "CrnCode")
    public String crnCode;

    /**
     * Contains the currency name.
     */
    @Element(name = "CrnName")
    public String crnName;

    /**
     * Contains the decimal digits.
     */
    @Element(name = "DecimalDigits", required = false)
    public int decimalDigits;

}
