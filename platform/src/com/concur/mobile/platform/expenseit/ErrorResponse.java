/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.expenseit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ErrorResponse implements Serializable{

    public static final String DEFAULT_ERROR_MESSAGE = "Unknown error";
    public static final Integer ERROR_CODE_NO_ERROR = -1;

    @SerializedName("errorMessage")
    private String errorMessage = DEFAULT_ERROR_MESSAGE;

    @SerializedName("errorCode")
    private Integer errorCode = ERROR_CODE_NO_ERROR;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isError() {
        return (!errorCode.equals(ERROR_CODE_NO_ERROR));
    }
}
