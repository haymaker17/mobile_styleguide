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
 * An implementation of <code>Comparator<ExpenseReport></code> for the purposes of ordering instances of
 * <code>ExpenseReport</code> by report date.
 * 
 * @author AndrewK
 */
public class ReportComparator implements Comparator<ExpenseReport> {

    private static final String CLS_TAG = ReportComparator.class.getSimpleName();

    private SortOrder sortOrder = SortOrder.ASCENDING;

    /**
     * Constructs an instance of <code>ReportComparator</code> with a default sort order based on ascending date.
     */
    public ReportComparator() {
        sortOrder = SortOrder.ASCENDING;
    }

    /**
     * Constructs an instance of <code>ReportComparator</code> using a specific sort order based on report date.
     * 
     * @param sortOrder
     *            the report date sort order.
     */
    public ReportComparator(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(ExpenseReport rep1, ExpenseReport rep2) {
        int retVal = 0;

        if (rep1 != rep2) {
            // Obtain the report date for 'rep1'.
            Calendar rep1Date = rep1.reportDateCalendar;
            // Obtain the report date for 'rep2'.
            Calendar rep2Date = rep2.reportDateCalendar;
            if (rep1Date != null) {
                if (rep2Date != null) {
                    switch (sortOrder) {
                    case ASCENDING: {
                        if (rep1Date.before(rep2Date)) {
                            retVal = -1;
                        } else if (rep1Date.after(rep2Date)) {
                            retVal = 1;
                        } else {
                            retVal = 0;
                        }
                        break;
                    }
                    case DESCENDING: {
                        if (rep1Date.before(rep2Date)) {
                            retVal = 1;
                        } else if (rep1Date.after(rep2Date)) {
                            retVal = -1;
                        } else {
                            retVal = 0;
                        }
                        break;
                    }
                    }
                } else {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".compare: report 2 has no date!");
                    retVal = -1;
                }
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG + ".compare: report 1 has no date!");
                retVal = -1;
            }
        } else {
            retVal = 0;
        }
        return retVal;
    }

}
