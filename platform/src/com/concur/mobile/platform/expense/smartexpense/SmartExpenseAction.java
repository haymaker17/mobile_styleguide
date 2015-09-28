package com.concur.mobile.platform.expense.smartexpense;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/*
 * Copyright (c) 2015 Concur Technologies, Inc.
*/
public class SmartExpenseAction implements Serializable {
    /**
     * Contains the action status object associated with retrieving the receipt list.
     */
    @SerializedName("status")
    public String status;

     /**
     * Contains the (protected) MobileEntry Key.
     */
    @SerializedName("meKey")
    public String meKey;

    /**
     * Contains SmartExpenseId for the current saved Action
     */
    public String smartExpenseId;

    /**
     * Contains error message when call failed
     */
    @SerializedName("errorMessage")
    public String errorMessage;

    public String getMeKey() {
        return meKey;
    }

    public void setSmartExpenseId(String smartExpenseId) {
        this.smartExpenseId = smartExpenseId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSmartExpenseId() {
        return smartExpenseId;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public SmartExpenseAction() {
    }

}
