/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.platform.authentication;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ExpenseItLoginResult implements Serializable {

    @SerializedName("accessToken")
    private AccessToken accessToken;

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public String getToken() {
        String result = null;
        if (accessToken != null) {
            result = accessToken.getToken();
        }
        return result;
    }

    public static class AccessToken {
        @SerializedName("token")
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

}
