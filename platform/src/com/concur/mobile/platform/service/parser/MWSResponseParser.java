package com.concur.mobile.platform.service.parser;

import java.util.ArrayList;
import java.util.List;

import com.concur.mobile.base.service.parser.BaseParser;

/**
 * Parser for mobile web service common response format
 * 
 * @author RatanK
 * 
 */
public class MWSResponseParser extends BaseParser {

    /**
     * Contains the default tag for this parser.
     */
    public static final String TAG_MWS_RESPONSE = "MWSResponse";

    /**
     * Contains the default parent tag for responses to requests that are parented by MWSResponse objects.
     */
    public static final String TAG_RESPONSE = "Response";

    // value object to be passed across to the activity
    private MWSResponseStatus requestTaskStatus = new MWSResponseStatus();

    // below both are part of the MWSResponseStatus object
    private List<Error> errors;
    private Error error;

    @Override
    public void startTag(String tag) {
        if (tag.equals("Error")) {
            if (errors == null) {
                errors = new ArrayList<Error>();
            }
            error = new Error();
        }
    }

    @Override
    public void handleText(String tag, String text) {
        if (tag.equals("SystemMessage")) {
            error.setSystemMessage(text);
        } else if (tag.equals("UserMessage")) {
            error.setUserMessage(text);
        } else if (tag.equals("Code")) {
            error.setCode(text);
        } else if (tag.equals("IsSuccess")) {
            requestTaskStatus.setSuccess(text.equalsIgnoreCase("true"));
        } else if (tag.equals("Message")) {
            requestTaskStatus.setResponseMessage(text);
        }
    }

    @Override
    public void endTag(String tag) {
        if (tag.equals("Error")) {
            errors.add(error);
        } else if (tag.equals("Errors")) {
            requestTaskStatus.setErrors(errors);
        }
    }

    public MWSResponseStatus getRequestTaskStatus() {
        return requestTaskStatus;
    }

}
