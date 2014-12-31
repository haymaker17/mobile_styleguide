package com.concur.mobile.platform.service.parser;

import java.util.List;

/**
 * Place holder class for the response from the server
 * 
 * @author RatanK
 * 
 */
public class MWSResponseStatus {

    private boolean success;
    private String responseMessage;
    private List<Error> errors;

    public MWSResponseStatus() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

}