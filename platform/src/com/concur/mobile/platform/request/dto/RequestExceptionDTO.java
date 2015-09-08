package com.concur.mobile.platform.request.dto;

import com.google.gson.annotations.SerializedName;

/**
 * @author olivierb
 */
public class RequestExceptionDTO {

    public enum ExceptionLevel {
        NONE,
        NON_BLOCKING,
        BLOCKING;
    }

    @SerializedName("Level")
    ExceptionLevel level;
    String title;
    @SerializedName("Message")
    String message;

    public RequestExceptionDTO(ExceptionLevel level, String title, String message) {
        this.setLevel(level);
        this.setTitle(title);
        this.setMessage(message);
    }

    public ExceptionLevel getLevel() {
        return level;
    }

    public void setLevel(ExceptionLevel level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
