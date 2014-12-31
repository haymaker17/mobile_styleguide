package com.concur.mobile.platform.service.parser;

import com.concur.mobile.base.service.parser.BaseParser;

/**
 * Parser for existing mobile web service response format i.e. ActionStatus
 * 
 * @author RatanK
 * 
 */
public class ActionResponseParser extends BaseParser {

    public static final String TAG_ACTION_STATUS = "ActionStatus";

    /**
     * Contains a key used to retrieve a boolean value indicating the outcome of an action response.
     */
    public static final String ACTION_RESULT_KEY = "action.result";

    /**
     * Contains a key used to retrieve an error message returned if <code>ACTION_RESULT_KEY</code> is <code>false</code>.
     */
    public static final String ACTION_ERROR_MESSAGE_KEY = "action.error.message";

    private boolean isSuccess;
    private String errorMessage;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void handleText(String tag, String text) {
        if (tag.equals("ErrorMessage")) {
            errorMessage = text;
        } else if (tag.equals("Status")) {
            isSuccess = (text.equalsIgnoreCase("SUCCESS"));
        }
    }

}
