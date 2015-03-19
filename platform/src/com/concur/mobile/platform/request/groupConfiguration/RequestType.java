package com.concur.mobile.platform.request.groupConfiguration;

import com.google.gson.annotations.SerializedName;

/**
 * Created by OlivierB on 16/01/2015.
 */
public class RequestType {

    @SerializedName("Code")
    private String code;
    @SerializedName("Name")
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
