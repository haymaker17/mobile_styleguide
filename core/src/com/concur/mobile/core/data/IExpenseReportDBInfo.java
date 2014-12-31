/**
 * 
 */
package com.concur.mobile.core.data;

import com.concur.mobile.core.expense.report.data.ExpenseReport;

/**
 * An extension of <code>IExpenseReportInfo</code> containing parsed XML information.
 */
public interface IExpenseReportDBInfo extends IExpenseReportInfo {

    /**
     * Gets the report XML representation.
     * 
     * @return the report XML representation.
     */
    String getXML();

    /**
     * Clears the report XML representation.
     */
    void clearXML();

    /**
     * Sets the report object.
     * 
     * @param report
     *            the report object.
     */
    void setReport(ExpenseReport report);

}
