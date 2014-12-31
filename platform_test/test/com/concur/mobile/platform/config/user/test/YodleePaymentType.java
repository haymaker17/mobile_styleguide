package com.concur.mobile.platform.config.user.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "ListItem")
public class YodleePaymentType {

    @Element(name = "Key", required = false)
    public String key;

    @Element(name = "Text", required = false)
    public String text;

}
