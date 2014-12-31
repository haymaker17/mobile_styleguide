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

import com.concur.mobile.core.expense.charge.data.CorporateCardTransaction;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ListItem</code> to show corporate card transactions.
 */
public class CorporateCardExpenseListItem extends ExpenseListItem {

    private static final String CLS_TAG = CorporateCardExpenseListItem.class.getSimpleName();

    /**
     * Constructs an instance of <code>CorporateCardExpenseListItem</code>.
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
    public CorporateCardExpenseListItem(Expense expense, HashMap<Expense, CompoundButton> expenseButtonMap,
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
        CorporateCardTransaction txn = expense.getCorporateCardTransaction();
        if (txn != null) {
            crnCode = txn.getTransactionCrnCode();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getCurrencyCode: txn is null!");
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
        CorporateCardTransaction txn = expense.getCorporateCardTransaction();
        if (txn != null) {
            if (txn.getMobileEntry() == null) {
                expName = txn.getExpenseName();
            } else {
                expName = txn.getMobileEntry().getExpName();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseName: txn is null!");
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
        CorporateCardTransaction txn = expense.getCorporateCardTransaction();
        if (txn != null) {
            if (txn.getMobileEntry() == null) {
                expKey = txn.getExpenseKey();
            } else {
                expKey = txn.getMobileEntry().getExpKey();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseKey: txn is null!");
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
        CorporateCardTransaction txn = expense.getCorporateCardTransaction();
        if (txn != null) {
            transAmt = txn.getTransactionAmount();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTransactionAmount: txn is null!");
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
        CorporateCardTransaction txn = expense.getCorporateCardTransaction();
        if (txn != null) {
            transDate = txn.getTransactionDate();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTransactionDate: txn is null!");
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
        CorporateCardTransaction txn = expense.getCorporateCardTransaction();
        if (txn != null) {
            // First try the "doing business as" name.
            if (txn.getDoingBusinessAs() != null) {
                vendorName = txn.getDoingBusinessAs().trim();
            }
            // Second, go with the merchant name.
            if (vendorName == null || vendorName.length() == 0) {
                vendorName = txn.getMerchantName();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getVendorName: txn is null!");
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
        return true;
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
        CorporateCardTransaction txn = expense.getCorporateCardTransaction();
        if (txn != null) {
            if (txn.getMobileEntry() != null) {
                showReceipt = txn.getMobileEntry().hasReceiptImage();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".showReceipt: txn is null!");
        }
        return showReceipt;
    }

}
