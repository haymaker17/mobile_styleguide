package com.concur.mobile.core.expense.charge.data;

import android.graphics.Bitmap;

import com.concur.mobile.platform.expenseit.ExpenseItReceipt;

import java.io.Serializable;
import java.util.Calendar;

/**
 * POJO used to represent an ExpenseIt item in the Expense List.
 *
 * @author Elliott Jacobsen-Watts
 */
public class ExpenseItItem implements Serializable {

    public static final String CLS_TAG = ExpenseItItem.class.getSimpleName();

    private static final long serialVersionUID = -8493533688325626158L;

    private ExpenseItReceipt receipt;

    public ExpenseItItem(ExpenseItReceipt expItReceipt) {
        this.receipt = expItReceipt;
    }

    public ExpenseItReceipt getReceipt() {
        return receipt;
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

    /**
     * Gets the error code from the error in v1 ExpenseIt response.
     *
     * @return error code.
     */
    public int getErrorCode() {
        return receipt.getErrorCode();
    }

    /**
     * Gets the error message from the error in v1 ExpenseIt response.
     *
     * @return error message.
     */
    public String getErrorMessage() {
        return receipt.getErrorMessage();
    }

    public Bitmap getImageData() {
        return receipt.getImageData();
    }

    public long getReceiptId(){
        return receipt.getId();
    }
}
