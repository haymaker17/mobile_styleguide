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
import com.concur.mobile.platform.expenseit.ExpenseItParseCode;
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
     * Default public constructor.
     */
    public ExpenseItListItem(Expense expense, int listItemViewType) {
        super(expense, listItemViewType);
        expenseItItem = expense.getExpenseItItem();
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

            int status = getExpenseItStatusCode();
            // change the text based on the status
            TextView textView = (TextView) expenseView.findViewById(R.id.expenseit_processing_status);
            if (textView != null) {
                // Call a separate function that converts the status code into text.
                // We need the context to get the string resources.
                textView.setText(getStatusText(context, status));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate processing status field!");
            }

            // change the icon based on the status
            ImageView imageView = (ImageView) expenseView.findViewById(R.id.expense_expenseit_icon);
            ImageView arrows = (ImageView) expenseView.findViewById(R.id.expense_expenseit_arrows);
            if (imageView != null && arrows != null) {
                if (isProcessing(status)) {
                    imageView.setImageResource(R.drawable.icon_processing);
                    arrows.setImageResource(R.drawable.icon_processing_arrow);
                    AnimationUtil.rotateAnimation(arrows);
                } else {
                    // TODO: EJ-W: Deal with the different cases.
                    // For now, just keeps the default icon (which is the processing icon),
                    // as defined in the layout file.
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate the icon!");
            }

            // set the date
            textView = (TextView) expenseView.findViewById(R.id.expenseit_upload_date);
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

            // set the eta
            textView = (TextView) expenseView.findViewById(R.id.expenseit_processing_time);
            if (textView != null) {
                int etaMinutes = expenseItItem.getEta();
                if (etaMinutes > 0) {
                    String eta = context.getResources().getString(R.string.expenseit_expense_processing_time, etaMinutes);
                    textView.setText(eta);
                } else {
                    // If no ETA, display no text.
                    textView.setText("");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate processing time (ETA) field!");
            }

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
     * Returns the <code>ExpenseItParseCode</code>; See enum by the same name.
     *
     * @return
     */
    public int getExpenseItStatusCode() {
        return expenseItItem.getParsingStatusCode();
    }

    private String getStatusText(Context context, int status) {
        String statusText = "";
        if (isProcessing(status)) {
            statusText = context.getString(R.string.expenseit_expense_detail_submitted);
        } else {
            // TODO: EJW: Elaborate more on the different status codes.
        }

        return statusText;
    }

    private boolean isProcessing(int status) {
        if (status == ExpenseItParseCode.UPLOADED.value()
                || status == ExpenseItParseCode.UPLOADED_BUT_NOT_QUEUED.value()
                || status == ExpenseItParseCode.FAILED_UPLOAD_ATTEMPTS.value()
                || status == ExpenseItParseCode.ANALYZING_REMOTELY_PENDING.value()
                || status == ExpenseItParseCode.UNPARSED.value()
                || status == ExpenseItParseCode.DEFAULT.value()) {
            return true;
        }
        return false;
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
