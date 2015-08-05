/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.platform.expenseit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;

public class ExpenseItPostReceiptResponse implements Serializable {

    @SerializedName("expenses")
    private ExpenseItPostReceipt[] expenses;


    public ExpenseItPostReceipt[] getExpenses() {
        return expenses;
    }

    public void setExpenses(ExpenseItPostReceipt[] expenses) {
        this.expenses = expenses;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseItPostReceiptResponse)) return false;

        ExpenseItPostReceiptResponse that = (ExpenseItPostReceiptResponse) o;

        return Arrays.equals(getExpenses(), that.getExpenses());

    }

    @Override
    public int hashCode() {
        return getExpenses() != null ? Arrays.hashCode(getExpenses()) : 0;
    }
}
