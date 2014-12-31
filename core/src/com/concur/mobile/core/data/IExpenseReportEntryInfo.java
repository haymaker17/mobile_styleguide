/**
 * 
 */
package com.concur.mobile.core.data;

import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;

/**
 * An interface used to retrieve report entry information from the database tier.
 */
public interface IExpenseReportEntryInfo {

    /**
     * Gets the entry XML representation.
     * 
     * @return the entry XML representation.
     */
    public String getXML();

    /**
     * Clears the entry XML representation.
     */
    public void clearXML();

    /**
     * Gets the parsed report entry.
     * 
     * @return the parsed report entry.
     */
    public ExpenseReportEntry getEntry();

    /**
     * Sets the associated instance of <code>ExpenseReportEntry</code>.
     * 
     * @param entry
     *            the associated instance of <code>ExpenseReportEntry</code>.
     */
    public void setEntry(ExpenseReportEntry entry);

    /**
     * Gets whether this entry object is a detailed object.
     * 
     * @return whether this entry object is a detailed object.
     */
    public boolean isDetail();

}
