package com.concur.mobile.core.expense.charge.data;

import com.concur.mobile.platform.expenseit.dao.ExpenseItReceiptDAO;

import java.util.Calendar;

/**
 * POJO used to represent an ExpenseIt item in the Expense List.
 *
 * @author Elliott Jacobsen-Watts
 */
public class ExpenseItItem {

    public static final String CLS_TAG = ExpenseItItem.class.getSimpleName();

    private ExpenseItReceiptDAO receipt;

    public ExpenseItItem(ExpenseItReceiptDAO expItReceiptDAO) {
        this.receipt = expItReceiptDAO;
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
    public Calendar getUploadDate() {
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
