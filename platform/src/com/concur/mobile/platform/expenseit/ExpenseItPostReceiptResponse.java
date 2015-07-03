/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.platform.expenseit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ExpenseItPostReceiptResponse implements Serializable {

    @SerializedName("expenses")
    private ExpenseItPostReceipt[] expenses;


    public ExpenseItPostReceipt[] getExpenses() {
        return expenses;
    }

    public void setExpenses(ExpenseItPostReceipt[] expenses) {
        this.expenses = expenses;
    }
}
