package com.concur.mobile.platform.service.parser;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * value object for the Error element in the MWSResponse XML from the server
 * 
 * @author RatanK
 * 
 */
public class Error implements Serializable {

    private static final long serialVersionUID = 5203633921550782684L;

    @SerializedName("code")
    private String code;

    @SerializedName("systemMessage")
    private String systemMessage;

    @SerializedName("userMessage")
    private String userMessage;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

}
