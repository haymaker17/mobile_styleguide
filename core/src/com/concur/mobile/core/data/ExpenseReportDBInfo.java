package com.concur.mobile.core.data;

import java.util.Calendar;

import com.concur.mobile.core.expense.report.data.ExpenseReport;

/**
 * An extension of <code>ExpenseReportInfo</code> for storing the XML report representation.
 */
public class ExpenseReportDBInfo extends ExpenseReportInfo implements IExpenseReportDBInfo {

    // Contains the report XML representation.
    protected String reportXml;

    /**
     * Constructs an instance of <code>ExpenseReportDBInfo</code>.
     * 
     * @param reportKey
     *            the report key.
     * @param report
     *            the report.
     * @param type
     *            the report type.
     * @param update
     *            the last client update time.
     * @param detail
     *            whether this is a detailed object.
     * @param reportXml
     *            the report XML representation.
     */
    public ExpenseReportDBInfo(String reportKey, ExpenseReport report, ReportType type, Calendar update,
            boolean detail, String reportXml) {
        super(reportKey, report, type, update, detail);
        this.reportXml = reportXml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportDBInfo#clearXML()
     */
    public void clearXML() {
        reportXml = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportDBInfo#getXML()
     */
    public String getXML() {
        return reportXml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportDBInfo#setReport(com.concur.mobile.data.expense.ExpenseReport)
     */
    public void setReport(ExpenseReport report) {
        this.report = report;
    }

}
