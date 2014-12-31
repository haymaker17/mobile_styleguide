/**
 * 
 */
package com.concur.mobile.core.expense.charge.data;

import java.util.List;

import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;

/**
 * A map of a list of attendees onto an expense entry.
 */
public class AttendeesEntryMap {

    /**
     * Contains the list of attendees.
     */
    public List<ExpenseReportAttendee> attendees;

    /**
     * Contains the mobile entry key.
     */
    public String meKey;

}
