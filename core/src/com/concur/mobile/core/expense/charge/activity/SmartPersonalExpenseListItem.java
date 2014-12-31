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
import com.concur.mobile.core.expense.charge.data.PersonalCard;
import com.concur.mobile.core.expense.charge.data.PersonalCardTransaction;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ExpenseListItem</code> for rendering a smart personal expense.
 */
public class SmartPersonalExpenseListItem extends ExpenseListItem {

    private static final String CLS_TAG = SmartPersonalExpenseListItem.class.getSimpleName();

    /**
     * Constructs an instance of <code>SmartPersonalExpenseListItem</code>.
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
    public SmartPersonalExpenseListItem(Expense expense, HashMap<Expense, CompoundButton> expenseButtonMap,
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
        PersonalCard card = expense.getPersonalCard();
        if (card != null) {
            crnCode = card.crnCode;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getCurrencyCode: card is null!");
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
        PersonalCard card = expense.getPersonalCard();
        if (card != null) {
            MobileEntry mobileEntry = expense.getCashTransaction();
            if (mobileEntry != null) {
                expName = mobileEntry.getExpName();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseName: mobile entry is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseName: card is null!");
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
        PersonalCard card = expense.getPersonalCard();
        if (card != null) {
            MobileEntry mobileEntry = expense.getCashTransaction();
            if (mobileEntry != null) {
                expKey = mobileEntry.getExpKey();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseKey: mobile entry is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseKey: card is null!");
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
        PersonalCardTransaction txn = expense.getPersonalCardTransaction();
        if (txn != null) {
            transAmt = txn.amount;
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
        PersonalCardTransaction txn = expense.getPersonalCardTransaction();
        if (txn != null) {
            transDate = txn.datePosted;
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
        PersonalCardTransaction txn = expense.getPersonalCardTransaction();
        if (txn != null) {
            vendorName = txn.description;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTransactionDate: txn is null!");
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
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#showReceipt()
     */
    @Override
    public boolean showReceipt() {
        boolean showReceipt = false;
        PersonalCardTransaction txn = expense.getPersonalCardTransaction();
        if (txn != null) {
            MobileEntry mobileEntry = expense.getCashTransaction();
            if (mobileEntry != null) {
                showReceipt = mobileEntry.hasReceiptImage();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".showReceipt: mobile entry is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".showReceipt: txn is null!");
        }
        return showReceipt;
    }

}
