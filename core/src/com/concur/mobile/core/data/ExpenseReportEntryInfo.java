/**
 * 
 */
package com.concur.mobile.core.data;

import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;

/**
 * An implementation of <code>IExpenseReportEntryDBInfo</code> for the purposes of retrieving report entry information from the
 * database tier.
 */
class ExpenseReportEntryInfo implements IExpenseReportEntryInfo {

    // Contains the XML representation of the entry.
    protected String entryXml;

    // Contains whether this entry is a detailed entry.
    protected boolean detail;

    // Contains a parsed report entry object.
    protected ExpenseReportEntry entry;

    /**
     * Constructs an instance of <code>ExpenseReportEntryInfo</code>.
     * 
     * @param entry
     *            contains the parsed report entry.
     * @param entryXml
     *            the entry XML representation.
     * @param detail
     *            whether this is a detail object.
     */
    public ExpenseReportEntryInfo(ExpenseReportEntry entry, String entryXml, boolean detail) {
        this.entry = entry;
        this.entryXml = entryXml;
        this.detail = detail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportEntryDBInfo#clearXML()
     */
    public void clearXML() {
        entryXml = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportEntryDBInfo#getXML()
     */
    public String getXML() {
        return entryXml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportEntryInfo#getEntry()
     */
    public ExpenseReportEntry getEntry() {
        return entry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportEntryInfo#setEntry(com.concur.mobile.data.expense.ExpenseReportEntry)
     */
    public void setEntry(ExpenseReportEntry entry) {
        this.entry = entry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IExpenseReportEntryDBInfo#isDetail()
     */
    public boolean isDetail() {
        return detail;
    }

}
