/**
 * 
 */
package com.concur.mobile.core.data;

import java.util.Calendar;

import com.concur.mobile.core.expense.report.data.ExpenseReport;

/**
 * An abstract implementation of <code>IExpenseReportInfo</code>.
 * 
 * @author andy
 */
public abstract class ExpenseReportInfo implements IExpenseReportInfo {

    // Contains the report key.
    protected String reportKey;

    // Reference to the report.
    protected ExpenseReport report;

    // Contains the report type.
    protected ReportType type;

    // Contains the report update time.
    protected Calendar update;

    // Contains whether this is a detailed report.
    protected boolean detail;

    /**
     * Constructs an instance of <code>ExpenseReportInfo</code>.
     * 
     * @param reportKey
     *            the report key.
     * @param report
     *            the report.
     * @param type
     *            the report type.
     * @param update
     *            the last update time.
     * @param detail
     *            whether this is a detail object.
     */
    protected ExpenseReportInfo(String reportKey, ExpenseReport report, ReportType type, Calendar update, boolean detail) {
        this.reportKey = reportKey;
        this.report = report;
        this.type = type;
        this.update = update;
        this.detail = detail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportInfo#getReportKey()
     */
    public String getReportKey() {
        return reportKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportInfo#getExpenseReport()
     */
    public ExpenseReport getReport() {
        return report;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportInfo#getReportType()
     */
    public ReportType getReportType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportInfo#getUpdateTime()
     */
    public Calendar getUpdateTime() {
        return update;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportInfo#isDetail()
     */
    public boolean isDetail() {
        return detail;
    }

}
