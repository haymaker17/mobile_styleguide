package com.concur.mobile.platform.expenseit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Elliott Jacobsen-Watts
 */
public class PutExpenseItNoteResponse implements Serializable {

    private static final long serialVersionUID = -8283442092654590236L;

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
        if (this == o) {
            return true;
        }
        if (!(o instanceof PutExpenseItNoteResponse)) {
            return false;
        }

        PutExpenseItNoteResponse that = (PutExpenseItNoteResponse) o;

        return Arrays.equals(getExpenses(), that.getExpenses());
    }

    @Override
    public int hashCode() {
        return getExpenses() != null ? Arrays.hashCode(getExpenses()) : 0;
    }
}
