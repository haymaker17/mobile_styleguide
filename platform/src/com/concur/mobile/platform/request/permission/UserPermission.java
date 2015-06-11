package com.concur.mobile.platform.request.permission;

import com.google.gson.annotations.SerializedName;

/**
 * Created by OlivierB on 21/05/2015.
 */
public class UserPermission {
    @SerializedName("Action")
    private String action;
    @SerializedName("Method")
    private String method;

    public String getAction() {
        return action;
    }

    public String getMethod() {
        return method;
    }
}
