/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.util.Calendar;

/**
 * An implementation of <code>IExpenseReportDetailInfo</code>.
 * 
 * @author AndrewK
 */
public class ExpenseReportDetailInfo implements IExpenseReportDetailInfo {

    // The report detail summary flag (report with header detail + summary entries).
    protected boolean detailSummary;

    // The report key.
    protected String reportKey;

    // The expense report detail.
    protected ExpenseReportDetail expenseReportDetail;

    // The last update time.
    protected Calendar updateTime;

    /**
     * Constructs an instance of <code>ExpenseReportDetailInfo</code> with the report detail and the last update time.
     * 
     * @param expenseReportDetail
     *            the expense report detail.
     * @param updateTime
     *            the last update time.
     * @param reportKey
     *            the report key.
     * @param detailSummary
     *            the report detail summary flag.
     */
    public ExpenseReportDetailInfo(ExpenseReportDetail expenseReportDetail, Calendar updateTime, String reportKey,
            boolean detailSummary) {
        this.detailSummary = detailSummary;
        this.reportKey = reportKey;
        this.expenseReportDetail = expenseReportDetail;
        this.updateTime = updateTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportDetailInfo#isReportDetailSummary()
     */
    public boolean isReportDetailSummary() {
        return detailSummary;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportDetailInfo#getReportKey()
     */
    public String getReportKey() {
        return reportKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportDetailInfo#getExpenseReportDetail()
     */
    public ExpenseReportDetail getExpenseReportDetail() {
        return expenseReportDetail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportDetailInfo#getUpdateTime()
     */
    public Calendar getUpdateTime() {
        return updateTime;
    }

}
