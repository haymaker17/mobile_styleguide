package com.concur.mobile.platform.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by OlivierB on 29/01/2015.
 */
public class ConnectExceptionMessage {

    @SerializedName("Code")
    private String code;
    @SerializedName("Level")
    private Integer level;
    @SerializedName("Message")
    private String message;

    public String getCode() {
        return code;
    }

    public Integer getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }
}

