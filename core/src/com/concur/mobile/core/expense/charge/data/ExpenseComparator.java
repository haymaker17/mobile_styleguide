/**
 * 
 */
package com.concur.mobile.core.expense.charge.data;


import java.util.Calendar;
import java.util.Comparator;

/**
 * An implementation of <code>Comparator<Expense></code> for the purposes of ordering instances of <code>Expense</code> by
 * transaction date.
 * 
 * @author AndrewK
 */
public class ExpenseComparator implements Comparator<Expense> {

    public static final String CLS_TAG = ExpenseComparator.class.getSimpleName();

    private String sortOrder;

    /**
     * Constructs an instance of <code>ExpenseComparator</code> with a default sort order based on ascending date.
     */
    public ExpenseComparator() {
        sortOrder = com.concur.mobile.platform.expense.provider.Expense.SmartExpenseColumns.DATE_NEWEST_SORT_ORDER;
    }

    /**
     * Constructs an instance of <code>ExpenseComparator</code> using a specific sort order based on report date.
     * 
     * @param sortOrder
     *            the expense date sort order. One of sort order fields in <code>ExpenseContract.SmartExpenseColumns</code>.
     */
    public ExpenseComparator(String sortOrder) {
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
            Double amountExp1 = null;
            String expenseType1 = null;
            String vendor1 = null;
            switch (exp1.getExpenseEntryType()) {
                case CASH:
                    transDateExp1 = exp1.getCashTransaction().getTransactionDateCalendar();
                    amountExp1 = exp1.getCashTransaction().getTransactionAmount();
                    expenseType1 = exp1.getCashTransaction().getExpName();
                    vendor1 = exp1.getCashTransaction().getVendorName();
                    break;
                case PERSONAL_CARD:
                case SMART_PERSONAL:
                    transDateExp1 = exp1.getPersonalCardTransaction().datePosted;
                    amountExp1 = exp1.getPersonalCardTransaction().amount;
                    expenseType1 = exp1.getPersonalCardTransaction().expName;
                    vendor1 = exp1.getPersonalCardTransaction().description;
                    break;
                case CORPORATE_CARD:
                case SMART_CORPORATE:
                    transDateExp1 = exp1.getCorporateCardTransaction().getTransactionDate();
                    amountExp1 = exp1.getCorporateCardTransaction().getTransactionAmount();
                    expenseType1 = exp1.getCorporateCardTransaction().getExpenseName();
                    vendor1 = exp1.getCorporateCardTransaction().getMerchantName();
                    break;
                case RECEIPT_CAPTURE:
                    transDateExp1 = exp1.getReceiptCapture().transactionDate;
                    amountExp1 = exp1.getReceiptCapture().transactionAmount;
                    expenseType1 = exp1.getReceiptCapture().expName;
                    vendor1 = exp1.getReceiptCapture().vendorName;
                    break;
                case E_RECEIPT:
                    transDateExp1 = exp1.getEReceipt().getTransactionDate();
                    amountExp1 = exp1.getEReceipt().getTransactionAmount();
                    expenseType1 = exp1.getEReceipt().getExpName();
                    vendor1 = exp1.getEReceipt().getVendorDescription();
                    break;
                case OCR_NOT_DONE:
                    transDateExp1 = exp1.getOcrItem().getUploadDate();
                    break;
                case EXPENSEIT_NOT_DONE:
                    transDateExp1 = exp1.getExpenseItReceipt().getCreatedAt();
                    break;
                case UNKNOWN_EXPENSE:
                    if (exp1.getSmartExpense().getTransactionDate() != null) {
                        transDateExp1 = exp1.getSmartExpense().getTransactionDate();
                    }
                    break;
            }
            // Obtain the transaction date for 'exp2'.
            Calendar transDateExp2 = null;
            Double amountExp2 = null;
            String expenseType2 = null;
            String vendor2 = null;
            switch (exp2.getExpenseEntryType()) {
                case CASH:
                    transDateExp2 = exp2.getCashTransaction().getTransactionDateCalendar();
                    amountExp2 = exp2.getCashTransaction().getTransactionAmount();
                    expenseType2 = exp2.getCashTransaction().getExpName();
                    vendor2 = exp2.getCashTransaction().getVendorName();
                    break;
                case PERSONAL_CARD:
                case SMART_PERSONAL:
                    transDateExp2 = exp2.getPersonalCardTransaction().datePosted;
                    amountExp2 = exp2.getPersonalCardTransaction().amount;
                    expenseType2 = exp2.getPersonalCardTransaction().expName;
                    vendor2 = exp2.getPersonalCardTransaction().description;
                    break;
                case CORPORATE_CARD:
                case SMART_CORPORATE:
                    transDateExp2 = exp2.getCorporateCardTransaction().getTransactionDate();
                    amountExp2 = exp2.getCorporateCardTransaction().getTransactionAmount();
                    expenseType2 = exp2.getCorporateCardTransaction().getExpenseName();
                    vendor2 = exp2.getCorporateCardTransaction().getMerchantName();
                    break;
                case RECEIPT_CAPTURE:
                    transDateExp2 = exp2.getReceiptCapture().transactionDate;
                    amountExp2 = exp2.getReceiptCapture().transactionAmount;
                    expenseType2 = exp2.getReceiptCapture().expName;
                    vendor2 = exp2.getReceiptCapture().vendorName;
                    break;
                case E_RECEIPT:
                    transDateExp2 = exp2.getEReceipt().getTransactionDate();
                    amountExp2 = exp2.getEReceipt().getTransactionAmount();
                    expenseType2 = exp2.getEReceipt().getExpName();
                    vendor2 = exp2.getEReceipt().getVendorDescription();
                    break;
                case OCR_NOT_DONE:
                    transDateExp2 = exp2.getOcrItem().getUploadDate();
                    break;
                case EXPENSEIT_NOT_DONE:
                    transDateExp2 = exp2.getExpenseItReceipt().getCreatedAt();
                    break;
                case UNKNOWN_EXPENSE:
                    if (exp2.getSmartExpense().getTransactionDate() != null) {
                        transDateExp2 = exp2.getSmartExpense().getTransactionDate();
                    }
                    break;
            }


            // Do the actual comparison based on the sort type!
            switch (sortOrder) {
                case com.concur.mobile.platform.expense.provider.Expense.SmartExpenseColumns.DATE_NEWEST_SORT_ORDER: {

                    if(transDateExp1 == null) {
                        retVal = -1;
                    } else if(transDateExp2 == null) {
                        retVal = -1;
                    } else if (transDateExp1.before(transDateExp2)) {
                        retVal = 1;
                    } else if (transDateExp1.after(transDateExp2)) {
                        retVal = -1;
                    } else {
                        retVal = 0;
                    }
                    break;
                }

                case com.concur.mobile.platform.expense.provider.Expense.SmartExpenseColumns.DATE_OLDEST_SORT_ORDER: {

                    if(transDateExp1 == null) {
                        retVal = -1;
                    } else if(transDateExp2 == null) {
                        retVal = -1;
                    } else if (transDateExp2.before(transDateExp1)) {
                        retVal = 1;
                    } else if (transDateExp2.after(transDateExp1)) {
                        retVal = -1;
                    } else {
                        retVal = 0;
                    }
                    break;
                }

                case com.concur.mobile.platform.expense.provider.Expense.SmartExpenseColumns.AMOUNT_LOWEST_SORT_ORDER: {

                    if(amountExp1 == null) {
                        retVal = -1;
                    } else if(amountExp2 == null) {
                        retVal = -1;
                    } else if (amountExp2 > amountExp1) {
                        retVal = -1;
                    } else if (amountExp2 < amountExp1) {
                        retVal = 1;
                    } else {
                        retVal = 0;
                    }
                    break;
                }

                case com.concur.mobile.platform.expense.provider.Expense.SmartExpenseColumns.AMOUNT_HIGHEST_SORT_ORDER: {

                    if(amountExp1 == null) {
                        retVal = -1;
                    } else if(amountExp2 == null) {
                        retVal = -1;
                    } else if (amountExp1 > amountExp2) {
                        retVal = -1;
                    } else if (amountExp1 < amountExp2) {
                        retVal = 1;
                    } else {
                        retVal = 0;
                    }
                    break;
                }

                case com.concur.mobile.platform.expense.provider.Expense.SmartExpenseColumns.EXPENSE_TYPE_SORT_ORDER: {

                    if(expenseType1 == null) {
                        retVal = -1;
                    } else if(expenseType2 == null) {
                        retVal = -1;
                    } else {
                        retVal = (expenseType1.compareTo(expenseType2));
                    }

                    break;
                }

                case com.concur.mobile.platform.expense.provider.Expense.SmartExpenseColumns.VENDOR_SORT_ORDER: {
                    if(vendor1 == null) {
                        retVal = -1;
                    } else if(vendor2 == null) {
                        retVal = -1;
                    } else {
                        retVal = (vendor1.compareTo(vendor2));
                    }

                    break;
                }

                default: {
                    retVal = 0;
                    break;
                }
            }

        }

        return retVal;
    }

}
