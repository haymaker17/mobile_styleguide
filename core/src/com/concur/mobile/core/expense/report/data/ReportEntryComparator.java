/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.util.Calendar;
import java.util.Comparator;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.SortOrder;

/**
 * An implementation of <code>Comparator<ExpenseReportEntry></code> for the purposes of ordering instances of
 * <code>ExpenseReportEntry</code> by transaction date.
 */
public class ReportEntryComparator implements Comparator<ExpenseReportEntry> {

    private static final String CLS_TAG = ReportEntryComparator.class.getSimpleName();

    private SortOrder sortOrder = SortOrder.ASCENDING;

    /**
     * Constructs an instance of <code>ReportEntryComparator</code> with a default sort order based on ascending date.
     */
    public ReportEntryComparator() {
        sortOrder = SortOrder.ASCENDING;
    }

    /**
     * Constructs an instance of <code>ReportEntryComparator</code> using a specific sort order based on report date.
     * 
     * @param sortOrder
     *            the expense date sort order.
     */
    public ReportEntryComparator(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(ExpenseReportEntry entry1, ExpenseReportEntry entry2) {
        int retVal = 0;

        if (entry1 != entry2) {
            // Obtain the transaction date for 'entry1'.
            Calendar entry1TransDate = entry1.transactionDateCalendar;
            // Obtain the transaction date for 'entry2'.
            Calendar entry2TransDate = entry2.transactionDateCalendar;
            if (entry1TransDate != null) {
                if (entry2TransDate != null) {
                    switch (sortOrder) {
                    case ASCENDING: {
                        if (entry1TransDate.before(entry2TransDate)) {
                            retVal = -1;
                        } else if (entry1TransDate.after(entry2TransDate)) {
                            retVal = 1;
                        } else {
                            retVal = 0;
                        }
                        break;
                    }
                    case DESCENDING: {
                        if (entry1TransDate.before(entry2TransDate)) {
                            retVal = 1;
                        } else if (entry1TransDate.after(entry2TransDate)) {
                            retVal = -1;
                        } else {
                            retVal = 0;
                        }
                        break;
                    }
                    }
                } else {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".compare: entry 2 has no transaction date!");
                    retVal = -1;
                }
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG + ".compare: entry 1 has no transaction date!");
                retVal = -1;
            }
        } else {
            retVal = 0;
        }
        return retVal;
    }

}
