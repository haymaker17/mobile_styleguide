package com.concur.mobile.core.expense.charge.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.ReceiptCapture;
import com.concur.mobile.core.util.Const;

public class ReceiptCaptureListItem extends ExpenseListItem {

    private static final String CLS_TAG = ReceiptCaptureListItem.class.getSimpleName();

    /**
     * Constructs an instance of <code>ReceiptCaptureExpenseItem</code>.
     * 
     * @param expense
     *            the expense.
     * @param expenseButtonMap
     *            the expense button map.
     * @param checkedExpenses
     *            the checked expense list.
     * @param checkChangeListener
     *            the check change listener.
     * @param listItemViewType
     *            the list view item type.
     */
    public ReceiptCaptureListItem(Expense expense, HashMap<Expense, CompoundButton> expenseButtonMap,
            HashSet<Expense> checkedExpenses, OnCheckedChangeListener checkChangeListener, int listItemViewType) {
        super(expense, expenseButtonMap, checkedExpenses, checkChangeListener, listItemViewType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getCurrencyCode()
     */
    @Override
    protected String getCurrencyCode() {
        // MOB-15545 : if currency code is null treat it as a "USD"; to be consistent with other platforms
        String crnCode = null;
        ReceiptCapture receiptCaptures = expense.getReceiptCapture();
        if (receiptCaptures != null) {
            crnCode = receiptCaptures.crnCode;
            if (crnCode == null || crnCode.length() == 0) {
                crnCode = "USD";
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getCurrencyCode: receiptCaptures is null!");
        }
        return crnCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getExpenseName()
     */
    @Override
    protected String getExpenseName() {
        String expName = null;
        ReceiptCapture receiptCaptures = expense.getReceiptCapture();
        if (receiptCaptures != null) {
            expName = receiptCaptures.expName;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseName: receiptCaptures is null!");
        }
        return expName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getExpenseKey()
     */
    @Override
    protected String getExpenseKey() {
        String expKey = null;
        ReceiptCapture receiptCaptures = expense.getReceiptCapture();
        if (receiptCaptures != null) {
            expKey = receiptCaptures.expKey;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseKey: receiptCaptures is null!");
        }
        return expKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#isExpenseKeyEditable()
     */
    protected boolean isExpenseKeyEditable()
    {
    	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getTransactionAmount()
     */
    @Override
    protected Double getTransactionAmount() {
        // MOB-15545 : if transaction amount is null treat it as a 0.00 to be consistent with other platforms
        Double transAmt = null;
        ReceiptCapture receiptCaptures = expense.getReceiptCapture();
        if (receiptCaptures != null) {
            transAmt = receiptCaptures.transactionAmount;
            if (transAmt == null) {
                transAmt = 0.0;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTransactionAmount: receiptCaptures is null!");
        }
        return transAmt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getTransactionDate()
     */
    @Override
    public Calendar getTransactionDate() {
        Calendar transDate = null;
        ReceiptCapture receiptCaptures = expense.getReceiptCapture();
        if (receiptCaptures != null) {
            transDate = receiptCaptures.transactionDate;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTransactionDate: receiptCaptures is null!");
        }
        return transDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getVendorName()
     */
    @Override
    protected String getVendorName() {
        String vendorName = null;
        ReceiptCapture receiptCaptures = expense.getReceiptCapture();
        if (receiptCaptures != null) {
            vendorName = receiptCaptures.vendorName;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getVendorName: receiptCaptures is null!");
        }
        return vendorName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#showCard()
     */
    @Override
    protected boolean showCard() {
        if (expense.shouldShowCardIcon()) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#showLongPressMessage()
     */
    @Override
    protected boolean showLongPressMessage() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#showReceipt()
     */
    @Override
    public boolean showReceipt() {
        boolean showReceipt = false;
        ReceiptCapture receiptCaptures = expense.getReceiptCapture();
        if (receiptCaptures != null) {
            String id = receiptCaptures.receiptImageId;
            if (id != null && id.length() > 0) {
                showReceipt = true;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".showReceipt: receiptCaptures is null!");
        }
        return showReceipt;
    }

}
