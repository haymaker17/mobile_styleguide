/**
 * 
 */
package com.concur.mobile.core.expense.charge.activity;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

/**
 * An abstract extension of <code>ListItem</code> for the purposes of providing an expense list item view.
 */
public abstract class ExpenseListItem extends ListItem {

    private static final String CLS_TAG = ExpenseListItem.class.getSimpleName();

    public Expense expense;

    protected HashMap<Expense, CompoundButton> expenseButtonMap;

    protected HashSet<Expense> checkedExpenses;

    protected OnCheckedChangeListener checkChangeListener;

    /**
     * Constructs an ExpenseListItem that does not support checkboxes
     * 
     * @param expense
     * @param listItemViewType
     */
    protected ExpenseListItem(Expense expense,  int listItemViewType) {
        this(expense, null, null, null, listItemViewType);
    }

    /**
     * Constructs an instance of <code>ExpenseListItem</code> associated with an expense.
     * 
     * @param expense
     *            the associated expense.
     * @param expenseButtonMap
     *            the expense checkbox button map.
     * @param checkedExpenses
     *            the list of checked expenses.
     * @param checkChangeListener
     *            the check change listener.
     * @param listItemViewType
     *            the list item view type.
     */
    protected ExpenseListItem(Expense expense, HashMap<Expense, CompoundButton> expenseButtonMap,
            HashSet<Expense> checkedExpenses, OnCheckedChangeListener checkChangeListener, int listItemViewType) {
        this.expense = expense;
        this.expenseButtonMap = expenseButtonMap;
        this.checkedExpenses = checkedExpenses;
        this.checkChangeListener = checkChangeListener;
        this.listItemViewType = listItemViewType;
    }

    @Override
    public Calendar getCalendar() {
        return getTransactionDate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ListItem#buildView(android.content.Context, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View expenseView = null;

        LayoutInflater inflater = LayoutInflater.from(context);
        if (Preferences.shouldUseNewOcrFeatures()) {
            expenseView = inflater.inflate(R.layout.expense_expenseit_row, null);
        } else {
            expenseView = inflater.inflate(R.layout.expense_list_row, null);
        }
        if (expenseView != null) {
            // Set expense type name.
            TextView txtView = (TextView) expenseView.findViewById(R.id.transaction_type);
            if (txtView != null) {
                // Check whether the expense type is undefind and if so set to color red.
                if ("UNDEF".equals(getExpenseKey()) && isExpenseKeyEditable()) {
                    txtView.setTextAppearance(context, R.style.RedCardExpenseTransactionText);
                }
                txtView.setText(getExpenseName());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate transaction type field!");
            }
            // Set expense date.
            txtView = (TextView) expenseView.findViewById(R.id.transaction_date);
            if (txtView != null) {
                Calendar transDate = getTransactionDate();
                if (transDate != null) {
                    txtView.setText(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(transDate.getTime()));
                } else {
                    txtView.setText("");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate transaction date field!");
            }
            // Set vendor name.
            txtView = (TextView) expenseView.findViewById(R.id.transaction_description);
            if (txtView != null) {
                txtView.setText(getVendorName());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate transaction description field!");
            }
            // Set the expense transaction amount and currency code character.
            txtView = (TextView) expenseView.findViewById(R.id.transaction_amount);
            Double transAmt = getTransactionAmount();
            if (txtView != null) {
                String formattedAmount = FormatUtil.formatAmount(getTransactionAmount(), context.getResources()
                        .getConfiguration().locale, getCurrencyCode(), true, true);
                if (formattedAmount != null) {
                    txtView.setText(formattedAmount);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to format transaction amount of '" + transAmt
                            + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate transaction amount field!");
            }
            // Show/hide receipt icon.
            ImageView imgView = (ImageView) expenseView.findViewById(R.id.expense_entry_receipt_icon);
            if (imgView != null) {
                if (!showReceipt()) {
                    imgView.setVisibility(View.GONE);
                } else {
                    imgView.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate receipt icon view!");
            }
            // Show/hide card icon.
            imgView = (ImageView) expenseView.findViewById(R.id.expense_entry_card_icon);
            if (imgView != null) {
                if (!showCard()) {
                    if (Preferences.shouldUseNewOcrFeatures()) {
                        imgView.setVisibility(View.INVISIBLE);
                    } else {
                        imgView.setVisibility(View.GONE);
                    }
                } else {
                    imgView.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate card icon view!");
            }
            // Show/hide long-press message.
            txtView = (TextView) expenseView.findViewById(R.id.long_press_msg_view);
            if (txtView != null) {
                if (showLongPressMessage()) {
                    txtView.setVisibility(View.VISIBLE);
                } else {
                    txtView.setVisibility(View.GONE);
                }
            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate long-press text view!");
            }

            // Add a listener to check for at least one selected item.
            final CheckBox ckBox = (CheckBox) expenseView.findViewById(R.id.expense_check);
            if (ckBox != null) {
                // Disable the checkbox if not wanted
                if (expenseButtonMap == null) {
                    ckBox.setVisibility(View.GONE);
                } else {
                    // Add to the button/expense mapping.
                    expenseButtonMap.put(expense, ckBox);
                    // Set the on check change listener.
                    ckBox.setChecked(checkedExpenses.contains(expense));
                    ckBox.setOnCheckedChangeListener(checkChangeListener);

                    // Build a runnable on the UI Thread using a TouchDelegate to expand hit area of CheckBox.
                    final View ckBoxParent = expenseView.findViewById(R.id.expense_check_parent);
                    // Post in parent so layout is populated before getHitRect() is called
                    ckBoxParent.post(new Runnable() {

                        public void run() {
                            Rect delegateArea = new Rect();

                            // Length in each direction to extend the hit box
                            int ckBoxHeight = ckBox.getHeight();

                            // In this case, ckBox is the delegate.
                            ckBox.getHitRect(delegateArea);
                            delegateArea.top -= ckBoxHeight;
                            delegateArea.bottom += ckBoxHeight;
                            delegateArea.left -= ckBoxHeight;
                            delegateArea.right += ckBoxHeight;
                            TouchDelegate expandedArea = new TouchDelegate(delegateArea, ckBox);

                            // Parent takes the delegate of the view we're delegating the area to
                            if (View.class.isInstance(ckBox.getParent())) {
                                ((View) ckBox.getParent()).setTouchDelegate(expandedArea);
                            }
                        };
                    });
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate checkbox!");
            }

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to inflate mobile entry row!");
        }
        return expenseView;
    }

    public Expense getExpense() {
        return expense;
    }

    /**
     * Gets the expense name.
     * 
     * @return the expense name.
     */
    protected abstract String getExpenseName();

    /**
     * Gets the expense key.
     * 
     * @return the expense key.
     */
    protected abstract String getExpenseKey();

    /**
     * Whether the expense key field is editable.
     * 
     * @return the expense key is editable.
     */
    protected abstract boolean isExpenseKeyEditable();

    /**
     * Gets the transaction date.
     * 
     * @return the transaction date.
     */
    protected abstract Calendar getTransactionDate();

    /**
     * Gets the vendor name.
     * 
     * @return the vendor name.
     */
    protected abstract String getVendorName();

    /**
     * @return
     */
    protected abstract Double getTransactionAmount();

    /**
     * Gets the transaction currency code.
     * 
     * @return the transaction currency code.
     */
    protected abstract String getCurrencyCode();

    /**
     * Whether a receipt icon should be displayed.
     * 
     * @return whether a receipt icon should be displayed.
     */
    protected abstract boolean showReceipt();

    /**
     * Whether a credit card icon should be displayed.
     * 
     * @return whether a credit card icon should be displayed.
     */
    protected abstract boolean showCard();

    /**
     * Whether the long press message should be displayed.
     * 
     * @return whether the long press message should be displayed.
     */
    protected abstract boolean showLongPressMessage();

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}
