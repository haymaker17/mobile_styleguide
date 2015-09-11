/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.platform.expenseit;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("UnusedDeclaration")
public class ExpenseItAccountInfoModel extends ErrorResponse {

    @SerializedName("account")
    private ExpenseItAccountInfo accountInfo;

    public ExpenseItAccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(ExpenseItAccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

}
