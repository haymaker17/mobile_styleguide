/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.util.Calendar;

/**
 * An interface describing information about a detailed report.
 * 
 * @author AndrewK
 */
public interface IExpenseReportDetailInfo {

    /**
     * Gets whether the report referenced by this info object is a "detail summary" report (detailed header but summary entry
     * objects).
     * 
     * @return whether this report is a "detail summary" report.
     */
    public boolean isReportDetailSummary();

    /**
     * Gets the report key for the info object.
     * 
     * @return the report key.
     */
    public String getReportKey();

    /**
     * Gets the instance of <code>ExpenseReportDetail</code> described by this info object.
     * 
     * @return the instance of <code>ExpenseReportDetail</code> described by this info object.
     */
    public ExpenseReportDetail getExpenseReportDetail();

    /**
     * Gets the last time the detailed report was received at the client.
     * 
     * @return the last time the detailed report was received at the client.
     */
    public Calendar getUpdateTime();

}
