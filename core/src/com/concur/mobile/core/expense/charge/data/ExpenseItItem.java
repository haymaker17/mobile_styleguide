package com.concur.mobile.core.expense.charge.data;

import com.concur.mobile.platform.expenseit.ExpenseItPostReceipt;

import java.util.Date;

/**
 * POJO used to represent an ExpenseIt item in the Expense List.
 *
 * @author Elliott Jacobsen-Watts
 */
public class ExpenseItItem {

    public static final String CLS_TAG = ExpenseItItem.class.getSimpleName();

    private ExpenseItPostReceipt receipt;

    public ExpenseItItem(ExpenseItPostReceipt exReceipt) {
        this.receipt = exReceipt;
    }

    /**
     * Returns the status code of the uploaded receipt.
     *
     * @return code.
     */
    public int getParsingStatusCode() {
        return receipt.getParsingStatusCode();
    }

    /**
     * Gets the upload date, when the ExpenseIt receipt was created.
     *
     * @return date.
     */
    public Date getUploadDate() {
        return receipt.getCreatedAt();
    }

    /**
     * Retrieves the ETA for receipt processing completion time.
     *
     * @return eta.
     */
    public int getEta() {
        return receipt.getEta();
    }
}
