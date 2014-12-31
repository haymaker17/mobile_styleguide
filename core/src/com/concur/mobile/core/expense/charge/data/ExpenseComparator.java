/**
 * 
 */
package com.concur.mobile.core.expense.charge.data;

import java.util.Calendar;
import java.util.Comparator;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.SortOrder;

/**
 * An implementation of <code>Comparator<Expense></code> for the purposes of ordering instances of <code>Expense</code> by
 * transaction date.
 * 
 * @author AndrewK
 */
public class ExpenseComparator implements Comparator<Expense> {

    public static final String CLS_TAG = ExpenseComparator.class.getSimpleName();

    private SortOrder sortOrder = SortOrder.ASCENDING;

    /**
     * Constructs an instance of <code>ExpenseComparator</code> with a default sort order based on ascending date.
     */
    public ExpenseComparator() {
        sortOrder = SortOrder.ASCENDING;
    }

    /**
     * Constructs an instance of <code>ExpenseComparator</code> using a specific sort order based on report date.
     * 
     * @param sortOrder
     *            the expense date sort order.
     */
    public ExpenseComparator(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Expense exp1, Expense exp2) {
        int retVal = 0;

        if (exp1 != exp2) {
            // Obtain the transaction date for 'exp1'.
            Calendar transDateExp1 = null;
            switch (exp1.getExpenseEntryType()) {
            case CASH:
                transDateExp1 = exp1.getCashTransaction().getTransactionDateCalendar();
                break;
            case PERSONAL_CARD:
                transDateExp1 = exp1.getPersonalCardTransaction().datePosted;
                break;
            case CORPORATE_CARD:
                transDateExp1 = exp1.getCorporateCardTransaction().getTransactionDate();
                break;
            case SMART_CORPORATE:
                transDateExp1 = exp1.getCorporateCardTransaction().getTransactionDate();
                break;
            case SMART_PERSONAL:
                transDateExp1 = exp1.getPersonalCardTransaction().datePosted;
                break;
            case RECEIPT_CAPTURE:
                transDateExp1 = exp1.getReceiptCapture().transactionDate;
                break;
            case E_RECEIPT:
                transDateExp1 = exp1.getEReceipt().getTransactionDate();
                break;
            case OCR_NOT_DONE:
                transDateExp1 = exp1.getOcrItem().getUploadDate();
                break;
            }
            // Obtain the transaction date for 'exp2'.
            Calendar transDateExp2 = null;
            switch (exp2.getExpenseEntryType()) {
            case CASH:
                transDateExp2 = exp2.getCashTransaction().getTransactionDateCalendar();
                break;
            case PERSONAL_CARD:
                transDateExp2 = exp2.getPersonalCardTransaction().datePosted;
                break;
            case CORPORATE_CARD:
                transDateExp2 = exp2.getCorporateCardTransaction().getTransactionDate();
                break;
            case SMART_CORPORATE:
                transDateExp2 = exp2.getCorporateCardTransaction().getTransactionDate();
                break;
            case SMART_PERSONAL:
                transDateExp2 = exp2.getPersonalCardTransaction().datePosted;
                break;
            case RECEIPT_CAPTURE:
                transDateExp2 = exp2.getReceiptCapture().transactionDate;
                break;
            case E_RECEIPT:
                transDateExp2 = exp2.getEReceipt().getTransactionDate();
                break;
            case OCR_NOT_DONE:
                transDateExp2 = exp2.getOcrItem().getUploadDate();
                break;
            }
            if (transDateExp1 != null) {
                if (transDateExp2 != null) {
                    switch (sortOrder) {
                    case ASCENDING: {
                        if (transDateExp1.before(transDateExp2)) {
                            retVal = -1;
                        } else if (transDateExp1.after(transDateExp2)) {
                            retVal = 1;
                        } else {
                            retVal = 0;
                        }
                        break;
                    }
                    case DESCENDING: {
                        if (transDateExp1.before(transDateExp2)) {
                            retVal = 1;
                        } else if (transDateExp1.after(transDateExp2)) {
                            retVal = -1;
                        } else {
                            retVal = 0;
                        }
                        break;
                    }
                    }
                } else {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".compare: expense 2 has no transaction date!");
                    retVal = -1;
                }
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG + ".compare: expense 1 has no transaction date!");
                retVal = -1;
            }
        } else {
            retVal = 0;
        }
        return retVal;
    }

}
