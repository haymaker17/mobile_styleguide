package com.concur.mobile.core.expense.charge.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.concur.mobile.core.expense.charge.data.EReceipt;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.util.Const;

/**
 * NOTE: This was copied over from <code>ReceiptCaptureListItem</code>, but modified to work with EReceipts.
 * 
 * @author Chris N. Diaz
 * 
 */
public class EReceiptListItem extends ExpenseListItem {

    private static final String CLS_TAG = EReceiptListItem.class.getSimpleName();

    /**
     * Constructs an instance of <code>EREceipotListItem</code>.
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
    public EReceiptListItem(Expense expense, HashMap<Expense, CompoundButton> expenseButtonMap,
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
        EReceipt eReceipt = expense.getEReceipt();
        if (eReceipt != null) {
            crnCode = eReceipt.getCrnCode();
            if (crnCode == null || crnCode.length() == 0) {
                crnCode = "USD";
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getCurrencyCode: eReceipt is null!");
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
        EReceipt eReceipt = expense.getEReceipt();
        if (eReceipt != null) {
            expName = eReceipt.getExpName();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseName: eReceipt is null!");
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
        EReceipt eReceipt = expense.getEReceipt();
        if (eReceipt != null) {
            expKey = eReceipt.getExpKey();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseKey: eReceipt is null!");
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
        EReceipt eReceipt = expense.getEReceipt();
        if (eReceipt != null) {
            transAmt = eReceipt.getTransactionAmount();
            if (transAmt == null) {
                transAmt = 0.0;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTransactionAmount: eReceipt is null!");
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
        EReceipt eReceipt = expense.getEReceipt();
        if (eReceipt != null) {
            transDate = eReceipt.getTransactionDate();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTransactionDate: eReceipt is null!");
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
        EReceipt eReceipt = expense.getEReceipt();
        if (eReceipt != null) {
            vendorName = eReceipt.getVendorDescription();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getVendorName: eReceipt is null!");
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
        EReceipt eReceipt = expense.getEReceipt();
        if (eReceipt != null) {
            String id = eReceipt.getEReceiptId();
            if (id != null && id.length() > 0) {
                showReceipt = true;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".showReceipt: eReceipt is null!");
        }
        return showReceipt;
    }

}
