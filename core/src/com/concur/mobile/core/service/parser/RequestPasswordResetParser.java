package com.concur.mobile.core.service.parser;

import com.concur.mobile.platform.service.parser.ActionResponseParser;

public class RequestPasswordResetParser extends ActionResponseParser {

    private String keyPartA;
    private String goodPasswordDescription;

    public String getKeyPartA() {
        return keyPartA;
    }

    public String getGoodPasswordDescripton() {
        return goodPasswordDescription;
    }

    @Override
    public void handleText(String tag, String text) {
        if (tag.equals("KeyPart")) {
            keyPartA = text;
        } else if (tag.equals("GoodPasswordDescription")) {
            goodPasswordDescription = text;
        } else {
            super.handleText(tag, text);
        }
    }

}
