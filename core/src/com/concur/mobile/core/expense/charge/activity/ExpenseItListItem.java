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
import com.concur.mobile.platform.expenseit.ExpenseItPostReceipt;
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
        rubiconErrorCode = expenseItItem.getErrorCode();
        processingFailed = isInErrorState(expenseItItem.getParsingStatusCode());
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
            textView = (TextView) expenseView.findViewById(R.id.expenseit_processing_time);
            if (textView != null) {
                int etaInSeconds = expenseItItem.getEta();
                if (!processingFailed && etaInSeconds > 0) {
                    String time = getEtaToString(etaInSeconds);
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

    /**
     * Returns true if the object is in error state.
     *
     * @param status
     * @return
     */
    private boolean isInErrorState(int status) {
        return ((status == ExpenseItParseCode.MULTIPLE_RECEIPTS.value()) ||
                (status == ExpenseItParseCode.UNREADABLE.value()) ||
                (status == ExpenseItParseCode.EXPIRED.value()) ||
                (status == ExpenseItParseCode.NO_IMAGE_FOUND.value()) ||
                (status == ExpenseItParseCode.NOT_RECEIPT.value()) ||
                (status == ExpenseItParseCode.OTHER.value()) ||
                (status == ExpenseItParseCode.INTERVENTION_NEEDED.value()) ||
                (status == ExpenseItParseCode.PERMANENT_FAILURE.value()) ||
                (status == ExpenseItParseCode.DEFAULT.value()) ||
                (rubiconErrorCode == ExpenseItPostReceipt.RUBICON_ERROR));
    }

    /**
     * Returns true if the object is processing.
     *
     * @param status
     * @return
     */
    private boolean isProcessing(int status) {
        return ((status == ExpenseItParseCode.UNPARSED.value()) ||
                (status == ExpenseItParseCode.UPLOADED.value()) ||
                (status == ExpenseItParseCode.UPLOADED_BUT_NOT_QUEUED.value()) ||
                (status == ExpenseItParseCode.FAILED_UPLOAD_ATTEMPTS.value()) ||
                (status == ExpenseItParseCode.ANALYZING_REMOTELY_PENDING.value()) ||
                (status == ExpenseItParseCode.UPLOADING_IN_PROGRESS.value()) ||
                (status == ExpenseItParseCode.QUEUED_FOR_UPLOAD.value()) ||
                (status == ExpenseItParseCode.QUEUED_FOR_EXPORT.value()) ||
                (status == ExpenseItParseCode.QUEUED_FOR_DELETE.value()) ||
                (status == ExpenseItParseCode.QUEUED_FOR_MODIFY.value()) ||
                (status == ExpenseItParseCode.QUEUED_FOR_CREATION.value()) ||
                (status == ExpenseItParseCode.QUEUED_FOR_EXPORT_ON_SERVER.value()) ||
                (status == ExpenseItParseCode.DEFAULT.value()));
    }

    /**
     * Returns true if the object is in an export state - do we need this as a transition condition?
     *
     * @param status
     * @return
     */
    private boolean isInExportState(int status) {
        return ((status == ExpenseItParseCode.PARSED.value()) ||
                (status == ExpenseItParseCode.SUCCESS_HIDDEN.value()) ||
                (status == ExpenseItParseCode.SUCCESS_VISIBLE.value()) || // XXX: Not sure about this one.
                (status == ExpenseItParseCode.EXPORTED.value()));   // XXX: Not sure about this one.
    }

    private String getEtaToString(int etaInSeconds) {
        int etaTotalMinutes = etaInSeconds / 60;
        int etaRemainingSeconds = etaInSeconds % 60;
        String time;
        // TODO: EJW - all this needs to be localized in strings.xml.
        // Determine time. The first and last cases are safety precautions,
        // in case they are not handled above.
        if ((etaTotalMinutes + etaRemainingSeconds) <= 0) {
            time = "N/A";
        } else if (etaTotalMinutes > 0 && etaRemainingSeconds <= 0) {
            time = etaTotalMinutes + " min";
        } else if (etaTotalMinutes > 0 && etaRemainingSeconds > 0) {
            time = etaTotalMinutes + " min, " + etaRemainingSeconds + " sec";
        } else if (etaTotalMinutes <= 0 && etaRemainingSeconds > 0) {
            time = etaRemainingSeconds + " sec";
        } else {
            time = "N/A";
        }

        return time;
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
