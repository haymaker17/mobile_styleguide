package com.concur.mobile.core.service.parser;

import com.concur.mobile.platform.service.parser.ActionResponseParser;
import com.concur.mobile.platform.util.Parse;

public class ResetPasswordParser extends ActionResponseParser { // Done.

    private String loginId;
    private Integer minLength;
    private Boolean requiresMixedCase;
    private Boolean requiresNonAlphaNumeric;

    public String getLoginId() {
        return loginId;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public Boolean getRequiresMixedCase() {
        return requiresMixedCase;
    }

    public Boolean getRequiresNonAlphaNumeric() {
        return requiresNonAlphaNumeric;
    }

    @Override
    public void handleText(String tag, String text) {
        if (tag.equals("LoginId")) {
            loginId = text;
        } else if (tag.equals("MinLength")) {
            minLength = Parse.safeParseInteger(text);
        } else if (tag.equals("RequiresMixedCase")) {
            requiresMixedCase = Parse.safeParseBoolean(text);
        } else if (tag.equals("RequiresNonAlphanum")) {
            requiresNonAlphaNumeric = Parse.safeParseBoolean(text);
        } else {
            super.handleText(tag, text);
        }
    }

}
