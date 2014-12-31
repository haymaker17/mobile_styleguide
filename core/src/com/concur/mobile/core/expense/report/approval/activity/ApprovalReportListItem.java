/**
 * 
 */
package com.concur.mobile.core.expense.report.approval.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ListItem;

/**
 * An extension of <code>ListItem</code> representing an approval report.
 */
public class ApprovalReportListItem extends ListItem {

    private static final String CLS_TAG = ApprovalReportListItem.class.getSimpleName();

    ExpenseReport report;

    /**
     * Constructs an instance of <code>ApprovalReportListItem</code> with a report.
     * 
     * @param report
     *            the report.
     */
    ApprovalReportListItem(ExpenseReport report, int listItemViewType) {
        this.report = report;
        this.listItemViewType = listItemViewType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ListItem#buildView(android.content.Context, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View reportView = null;

        if (convertView == null) {
            // Create the main row container and static elements
            LayoutInflater inflater = LayoutInflater.from(context);
            reportView = inflater.inflate(R.layout.expense_approval_row, null);
        } else {
            reportView = convertView;
        }

        // Format the report total
        String reportTotal = FormatUtil.formatAmount(report.totalClaimedAmount, context.getResources()
                .getConfiguration().locale, report.crnCode, true, true);

        // Fill in the view elements.
        // First four are straight text fields.
        TextView txtView = (TextView) reportView.findViewById(R.id.exp_app_row_employee_name);
        if (txtView != null) {
            txtView.setText(report.employeeName);
        }
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(report.reportDateCalendar.getTime()));
        strBldr.append(" - ");
        strBldr.append(report.reportName);
        ((TextView) reportView.findViewById(R.id.exp_app_row_report_date)).setText(strBldr.toString());
        ((TextView) reportView.findViewById(R.id.exp_app_row_report_amount)).setText(reportTotal);

        ImageView imgView = (ImageView) reportView.findViewById(R.id.exp_app_row_exception);
        if (imgView != null) {
            switch (ViewUtil.getExpenseReportExceptionSeverityLevel(report)) {
            case NONE:
                imgView.setVisibility(View.GONE);
                break;
            case WARN:
                imgView.setImageResource(R.drawable.icon_yellowex);
                imgView.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                imgView.setImageResource(R.drawable.icon_redex);
                imgView.setVisibility(View.VISIBLE);
                break;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate exception image view!");
        }
        // Check for existence of receipts.
        imgView = (ImageView) reportView.findViewById(R.id.exp_app_row_receipt);
        if (!report.isReceiptImageAvailable()) {
            imgView.setVisibility(View.GONE);
        } else {
            imgView.setVisibility(View.VISIBLE);
        }
        // Check for existence of comments.
        String hasComments = report.lastComment;
        imgView = (ImageView) reportView.findViewById(R.id.exp_app_row_comment);
        if (hasComments == null || hasComments.length() == 0) {
            imgView.setVisibility(View.GONE);
        } else {
            imgView.setVisibility(View.VISIBLE);
        }
        // If the view was converted, invalidate it and request a layout.
        if (convertView != null) {
            reportView.invalidate();
            reportView.requestLayout();
        }
        return reportView;
    }

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
