package com.concur.mobile.platform.expense.list.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "ActionStatus", strict = false)
public class SaveMobileEntryResponse {

    @Element(name = "MeKey", required = false)
    public String meKey;

    @Element(name = "Status")
    public String status;
}
