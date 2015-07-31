package com.concur.mobile.core.expense.charge.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.ExpenseItItem;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.AnimationUtil;

import java.util.Calendar;

/**
 * An abstract extension of <code>ListItem</code> for the purposes of providing an expense list item view.
 */
public class ExpenseItListItem extends ExpenseListItem {

    public static final String CLS_TAG = ExpenseItListItem.class.getSimpleName();

    /**
     *  Did processing fail?
     *  TODO: Implement failure handling.
     */
    private boolean processingFailed;

    /**
     * the ExpenseItPostReceipt object.
     */
    private ExpenseItItem expenseItItem;

    /**
     * Is there a rubicon error?
     */
    private int rubiconErrorCode;

    /**
     * Default public constructor.
     */
    public ExpenseItListItem(Expense expense, int listItemViewType) {
        super(expense, listItemViewType);
        expenseItItem = expense.getExpenseItItem();
        rubiconErrorCode = expenseItItem.getErrorCode(); // necessary now?
        processingFailed = expenseItItem.isInErrorState();
    }

    /**
     * Display the upload date and time. This is going to be in the same format as the Receipt
     * Store.
     *
     * @return
     */
    @Override
    public Calendar getTransactionDate() {
        return expenseItItem.getUploadDate();
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {

        // instantiate the layout based on the expenseit list item
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View expenseView = layoutInflater.inflate(R.layout.expense_expenseit_row, null);

        if (expenseView != null) {
            // set the date
            TextView textView = (TextView) expenseView.findViewById(R.id.expenseit_upload_date);
            if (textView != null) {
                Calendar transDate = getTransactionDate();
                if (transDate != null) {
                    textView.setText(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(transDate.getTime()));
                } else {
                    textView.setText("");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate upload date field!");
            }

            // change the text based on the processingFailed
            textView = (TextView) expenseView.findViewById(R.id.expenseit_processing_status);
            if (textView != null) {
                textView.setText(getStatusText(context));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate processing parsingStatusCode field!");
            }

            // change the icon based on the parsingStatusCode
            ImageView imageView = (ImageView) expenseView.findViewById(R.id.expense_expenseit_icon);
            ImageView arrows = (ImageView) expenseView.findViewById(R.id.expense_expenseit_arrows);
            if (imageView != null && arrows != null) {
                // Check to make sure we're not in an error state, and if not, show processing.
                if (processingFailed) {
                    imageView.setImageResource(R.drawable.icon_error);
                    arrows.setVisibility(View.GONE);
                } else {
                    imageView.setImageResource(R.drawable.icon_processing);
                    arrows.setVisibility(View.VISIBLE);
                    arrows.setImageResource(R.drawable.icon_processing_arrow);
                    AnimationUtil.rotateAnimation(arrows);
                }
                // TODO: EJW - are there any other cases to handle (like exporting)?

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate the icon!");
            }

            // set the eta
            textView = (TextView) expenseView.findViewById(R.id.expenseit_details_processing_time);
            if (textView != null) {
                int etaInSeconds = expenseItItem.getEta();
                if (!processingFailed && etaInSeconds > 0) {
                    String time = FormatUtil.getEtaToString(context, etaInSeconds);
                    String eta = context.getResources().getString(R.string.expenseit_expense_processing_time,
                            time);
                    textView.setText(eta);
                } else {
                    // If no ETA or is failed, display no text.
                    textView.setText("");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate processing time (ETA) field!");
            }

            // Set checkbox to invisible for now (to maintain spacing)
            CheckBox checkBox = (CheckBox) expenseView.findViewById(R.id.expense_check);
            if (checkBox != null) {
                checkBox.setVisibility(View.INVISIBLE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate the checkbox!");
            }

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate the expenseView!");
        }

        return expenseView;
    }

    /**
     * Get the status text to put into the list.
     * @param context
     * @return
     */
    private String getStatusText(Context context) {
        String statusText;
        if (processingFailed) {
            statusText = context.getString(R.string.expenseit_expense_list_error);
        } else {
            statusText = context.getString(R.string.expenseit_expense_detail_submitted);
        }
        return statusText;
    }

    @Override
    protected String getVendorName() { return null; }

    @Override
    protected boolean showReceipt() { return false; }

    @Override
    protected String getExpenseName() { return null; }

    @Override
    protected String getExpenseKey() { return null; }

    @Override
    protected boolean isExpenseKeyEditable() { return false; }

    @Override
    protected Double getTransactionAmount() { return 0.0; }

    @Override
    protected String getCurrencyCode() { return null; }

    @Override
    protected boolean showCard() { return false; }

    @Override
    protected boolean showLongPressMessage() { return false; }
}
