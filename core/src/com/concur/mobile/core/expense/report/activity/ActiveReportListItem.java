/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ListItem;

/**
 * An extension of <code>ListItem</code> representing an active report.
 */
public class ActiveReportListItem extends ListItem {

    private static final String CLS_TAG = ActiveReportListItem.class.getSimpleName();

    public ExpenseReport report;

    /**
     * Constructs an instance of <code>ActiveReportListItem</code> with a report.
     * 
     * @param report
     *            the report.
     */
    public ActiveReportListItem(ExpenseReport report, int listItemViewType) {
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
            reportView = inflater.inflate(R.layout.active_report_row, null);
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
        ((TextView) reportView.findViewById(R.id.exp_app_row_report_name)).setText(report.reportName);
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(report.reportDateCalendar.getTime()));
        strBldr.append(" - ");
        strBldr.append(report.apvStatusName);
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

        // Check if report is ready to submit
        imgView = (ImageView) reportView.findViewById(R.id.exp_app_row_readytosubmit);
        if (!report.isReadyToSubmit()) {
            imgView.setVisibility(View.GONE);
        } else {
            imgView.setVisibility(View.VISIBLE);
        }

        // Set the approver name, if any.
        txtView = (TextView) reportView.findViewById(R.id.approver_name);
        if (txtView != null) {
            // Check for report submitted and defined approver name.
            if (report.isSubmitted()) {
                String apprName = report.aprvEmpName;
                if (apprName == null || apprName.length() == 0) {
                    // NOTE: As of 6/19/2012, approver name may not be in detailed report. So, attempt
                    // to obtain approver name from summary report.
                    // AVK.
                    if (report.isDetail()) {
                        ConcurCore concurCore = (ConcurCore) context.getApplicationContext();
                        IExpenseReportCache expRepActCache = concurCore.getExpenseActiveCache();
                        if (expRepActCache != null) {
                            ExpenseReport sumRep = expRepActCache.getReport(report.reportKey);
                            if (sumRep != null && sumRep.aprvEmpName != null && sumRep.aprvEmpName.length() > 0) {
                                apprName = sumRep.aprvEmpName;
                            }
                        }
                    }
                }
                if (apprName != null && apprName.length() > 0) {
                    txtView.setText(Format.localizeText(context, R.string.general_approver, apprName));
                    txtView.setVisibility(View.VISIBLE);
                } else {
                    txtView.setVisibility(View.GONE);
                }
            } else {
                txtView.setVisibility(View.GONE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate 'approver_name' field!");
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
