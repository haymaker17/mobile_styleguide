/**
 * 
 */
package com.concur.mobile.core.expense.charge.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ListItem</code> to represent an expense within a list.
 */
public class CashExpenseListItem extends ExpenseListItem {

    private static final String CLS_TAG = CashExpenseListItem.class.getSimpleName();

    /**
     * Constructs an instance of <code>CashExpenseListItem</code>.
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
    public CashExpenseListItem(Expense expense, HashMap<Expense, CompoundButton> expenseButtonMap,
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
        String crnCode = null;
        MobileEntry mobileEntry = expense.getCashTransaction();
        if (mobileEntry != null) {
            crnCode = mobileEntry.getCrnCode();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getCurrencyCode: mobileEntry is null!");
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
        MobileEntry mobileEntry = expense.getCashTransaction();
        if (mobileEntry != null) {
            expName = mobileEntry.getExpName();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseName: mobileEntry is null!");
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
        MobileEntry mobileEntry = expense.getCashTransaction();
        if (mobileEntry != null) {
            expKey = mobileEntry.getExpKey();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseKey: mobileEntry is null!");
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
    	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getTransactionAmount()
     */
    @Override
    protected Double getTransactionAmount() {
        Double transAmt = null;
        MobileEntry mobileEntry = expense.getCashTransaction();
        if (mobileEntry != null) {
            transAmt = mobileEntry.getTransactionAmount();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTransactionAmount: mobileEntry is null!");
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
        MobileEntry mobileEntry = expense.getCashTransaction();
        if (mobileEntry != null) {
            transDate = mobileEntry.getTransactionDateCalendar();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTransactionDate: mobileEntry is null!");
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
        MobileEntry mobileEntry = expense.getCashTransaction();
        if (mobileEntry != null) {
            vendorName = mobileEntry.getVendorName();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getVendorName: mobileEntry is null!");
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
        return false;
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
        MobileEntry mobileEntry = expense.getCashTransaction();
        if (mobileEntry != null) {
            showReceipt = mobileEntry.hasReceiptImage() || mobileEntry.hasReceiptImageDataLocal()
                    || (mobileEntry.getReceiptImageId() != null);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".showReceipt: mobileEntry is null!");
        }
        return showReceipt;
    }

}
