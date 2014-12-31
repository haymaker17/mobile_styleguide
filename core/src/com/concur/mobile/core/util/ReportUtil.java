package com.concur.mobile.core.util;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.IExpenseReportInfo.ReportType;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.report.data.ExpenseReport;

import android.content.Intent;

/**
 * Contains some static utility methods.
 */
public class ReportUtil {

    /**
     * Will return an instance of <code>ReportType</code> based on data contained within <code>intent</code>.
     * 
     * @param intent
     *            an intent containing data indicating report type information.
     * @return returns in instance of <code>ReportType</code> if found in <code>intent</code>; <code>null</code> otherwise.
     */
    public static ReportType getReportType(Intent intent) {
        ReportType reportType = null;

        int reportKeySource = intent.getIntExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, -1);
        switch (reportKeySource) {
        case Const.EXPENSE_REPORT_SOURCE_NEW: {
            reportType = ReportType.NEW;
            break;
        }
        case Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL: {
            reportType = ReportType.APPROVAL;
            break;
        }
        case Const.EXPENSE_REPORT_SOURCE_ACTIVE: {
            reportType = ReportType.ACTIVE;
            break;
        }
        }
        return reportType;
    }

    /**
     * Will get an instance of <code>IExpenseReportCache</code> based on a report type.
     * 
     * @param concurCore
     *            reference to the application.
     * @param reportType
     *            reference to the report type.
     * @return returns an instance of <code>IExpenseReportCache</code> appropriate for <code>reportType</code>. Returns
     *         <code>null</code> if <code>reportType</code> is of type <code>ReportType.NEW</code>.
     */
    public static IExpenseReportCache getReportCache(ConcurCore concurCore, ReportType reportType) {
        IExpenseReportCache expRepCache = null;
        switch (reportType) {
        case ACTIVE: {
            expRepCache = concurCore.getExpenseActiveCache();
            break;
        }
        case APPROVAL: {
            expRepCache = concurCore.getExpenseApprovalCache();
            break;
        }
        }
        return expRepCache;
    }

    /**
     * Will return whether or not the a report is editable based on a report type and the report itself.
     * 
     * @param reportType
     *            an instance of <code>ReportType</code>.
     * @param expRep
     *            an instance of <code>ExpenseReport</code>.
     * @return returns <code>true</code> if <code>expRep</code> is editable; <code>false</code> otherwise.
     */
    public static boolean isReportEditable(ReportType reportType, ExpenseReport expRep) {
        boolean reportEditable = false;
        reportEditable = ((reportType == ReportType.ACTIVE && (expRep.apsKey.equalsIgnoreCase("A_NOTF") || expRep.apsKey
                .equalsIgnoreCase("A_RESU"))) || (reportType == ReportType.NEW));

        return reportEditable;
    }

}
