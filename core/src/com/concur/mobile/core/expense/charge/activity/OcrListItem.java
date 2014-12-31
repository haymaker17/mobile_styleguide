package com.concur.mobile.core.expense.charge.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.OCRItem;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.ocr.OcrStatusEnum;

/**
 * NOTE: This was copied over from <code>ReceiptCaptureListItem</code>, but modified to work with EReceipts.
 * 
 * @author Chris N. Diaz
 * 
 */
public class OcrListItem extends ExpenseListItem {

    public static final String CLS_TAG = OcrListItem.class.getSimpleName();

    /**
     * Reference to the OCRItem.
     */
    private OCRItem ocrItem;

    /**
     * True if this OCR process has failed.
     */
    private boolean ocrFailed;

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
    public OcrListItem(Expense expense, int listItemViewType) {
        super(expense, listItemViewType);

        ocrItem = expense.getOcrItem();

        // If OCR failed, then allow the row to be clickable so that
        // users manually create an ExpenseEntry from the receipt.
        OcrStatusEnum ocrStatus = ocrItem.getOcrStatus();
        ocrFailed = OcrStatusEnum.isFailed(ocrStatus);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getCurrencyCode()
     */
    @Override
    protected String getCurrencyCode() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getExpenseName()
     */
    @Override
    protected String getExpenseName() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getExpenseKey()
     */
    @Override
    protected String getExpenseKey() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#isExpenseKeyEditable()
     */
    protected boolean isExpenseKeyEditable() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getTransactionAmount()
     */
    @Override
    protected Double getTransactionAmount() {
        return 0.0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getTransactionDate()
     */
    @Override
    public Calendar getTransactionDate() {
        return ocrItem.getUploadDate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ExpenseListItem#getVendorName()
     */
    @Override
    protected String getVendorName() {
        return null;
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
        return true;
    }

    /*
     * 
     */
    @Override
    public boolean isEnabled() {
        return ocrFailed;
    }

    /**
     * 
     * @return the resource ID for a human-readable string of the OCR status used to display in the row.
     */
    private int getDisplayOcrStatus() {

        OcrStatusEnum ocrStatus = ocrItem.getOcrStatus();

        if (ocrStatus != null) {
            if (OcrStatusEnum.isProcessing(ocrStatus)) {
                return R.string.ocr_status_procesing;
            } else if (OcrStatusEnum.isFailed(ocrStatus)) {
                return R.string.ocr_status_procesing_error;
            } else if (OcrStatusEnum.isCancelled(ocrStatus)) {
                return R.string.ocr_status_procesing_cancelled;
            }
        }

        Log.e(Const.LOG_TAG, CLS_TAG + ".getDisplayOcrStatus - Unknown OCR Status: '" + ocrStatus + "'");

        return R.string.ocr_status_procesing;
    }

    /*
     * 
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View expenseView = inflater.inflate(R.layout.expense_list_ocr_row, null);

        if (expenseView != null) {
            // Set expense type name.
            TextView txtView = (TextView) expenseView.findViewById(R.id.ocr_status);
            if (txtView != null) {

                if (ocrFailed) {
                    txtView.setTextAppearance(context, R.style.RedCardExpenseTransactionText);
                }
                txtView.setText(getDisplayOcrStatus());

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate transaction type field!");
            }

            // Set expense date.
            txtView = (TextView) expenseView.findViewById(R.id.receipt_upload_date);
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

            // Set expense time.
            txtView = (TextView) expenseView.findViewById(R.id.receipt_upload_time);
            if (txtView != null) {
                Calendar transDate = getTransactionDate();
                if (transDate != null) {
                    // Using local device time instead of UTC.
                    DateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    txtView.setText(format.format(transDate.getTime()));
                } else {
                    txtView.setText("");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate transaction date field!");
            }

            // Hide the checkbox.
            final CheckBox ckBox = (CheckBox) expenseView.findViewById(R.id.expense_check);
            if (ckBox != null) {
                ckBox.setVisibility(View.INVISIBLE);
            }

            // OCR: Handle row clicking.

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate checkbox!");
        }

        return expenseView;
    }

}
