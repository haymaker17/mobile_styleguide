/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.util.Calendar;
import java.util.List;

/**
 * An interface describing expense report list information.
 * 
 * @author AndrewK
 */
public interface IExpenseReportListInfo {

    /**
     * Gets the report list.
     * 
     * This method should always return a non-null list.
     * 
     * @return the list of reports; otherwise, an empty array list
     */
    public List<ExpenseReport> getReports();

    /**
     * Gets the report list last update time.
     * 
     * @return the
     */
    public Calendar getUpdateTime();

    /**
     * Will remove a report from the internally maintained list based on a report key.
     * 
     * @param reportKey
     *            the key of the report to remove.
     */
    public void removeReport(String reportKey);

}
