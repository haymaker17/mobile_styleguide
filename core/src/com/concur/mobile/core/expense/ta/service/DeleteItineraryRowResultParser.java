package com.concur.mobile.core.expense.ta.service;

import com.concur.mobile.platform.service.parser.ActionResponseParser;

public class DeleteItineraryRowResultParser extends ActionResponseParser {

    public String status;
    public String statusText;

    @Override
    public void handleText(String tag, String text) {
        if ("Status".equals(tag)) {
            this.status = text;
        } else if ("StatusText".equals(tag)) {
            this.statusText = text;
        }
    }
}
