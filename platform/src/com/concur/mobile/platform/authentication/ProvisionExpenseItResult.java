/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.platform.authentication;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProvisionExpenseItResult implements Serializable {

    @SerializedName("provisioningStatus")
    private ProvisioningStatus provisioningStatus;

    public ProvisioningStatus getProvisioningStatus() {
        return provisioningStatus;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setProvisioningStatus(ProvisioningStatus provisioningStatus) {
        this.provisioningStatus = provisioningStatus;
    }

    public static class ProvisioningStatus {
        @SerializedName("basicAccess")
        boolean basicAccess;

        public boolean isBasicAccess() {
            return basicAccess;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setBasicAccess(boolean basicAccess) {
            this.basicAccess = basicAccess;
        }
    }

}
