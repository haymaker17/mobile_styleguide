/**
 * 
 */
package com.concur.mobile.core.data;

import java.util.Calendar;

import com.concur.mobile.core.expense.report.data.ExpenseReport;

/**
 * An interface describing report information.
 */
public interface IExpenseReportInfo {

    /**
     * An enumeration describing a report type.
     */
    public static enum ReportType {
        ACTIVE,			// An active report.
        APPROVAL,		// An approval report.
        NEW				// A new report.
    };

    /**
     * Gets the report key.
     * 
     * @return the report key.
     */
    public String getReportKey();

    /**
     * Gets the report.
     * 
     * @return the report.
     */
    public ExpenseReport getReport();

    /**
     * Gets the report type.
     * 
     * @return the report type.
     */
    public ReportType getReportType();

    /**
     * Gets the report update time.
     * 
     * @return the report update time.
     */
    public Calendar getUpdateTime();

    /**
     * Gets whether this is a detail object.
     * 
     * @return whether this is a detail object.
     */
    public boolean isDetail();

}
