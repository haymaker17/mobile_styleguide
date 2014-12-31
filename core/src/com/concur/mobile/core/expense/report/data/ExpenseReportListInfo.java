/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.concur.mobile.core.data.IExpenseReportInfo;

/**
 * An implementation of <code>IExpenseReportListInfo</code>.
 * 
 * @author AndrewK
 */
public class ExpenseReportListInfo implements IExpenseReportListInfo {

    // The list of reports.
    protected List<IExpenseReportInfo> reportInfos;

    // The last update time.
    protected Calendar updateTime;

    /**
     * Constructs an instance of <code>ExpenseReportList</code> with a list of reports and the last update time.
     * 
     * @param reports
     *            the list of reports.
     * @param updateTime
     *            the last update time.
     */
    public ExpenseReportListInfo(List<IExpenseReportInfo> reportInfos, Calendar updateTime) {
        if (reportInfos != null) {
            this.reportInfos = reportInfos;
        } else {
            this.reportInfos = new ArrayList<IExpenseReportInfo>();
        }
        this.updateTime = updateTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportListInfo#getReports()
     */
    public List<ExpenseReport> getReports() {
        ArrayList<ExpenseReport> reports = new ArrayList<ExpenseReport>(reportInfos.size());
        for (IExpenseReportInfo reportInfo : reportInfos) {
            reports.add(reportInfo.getReport());
        }
        return reports;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportListInfo#getUpdateTime()
     */
    public Calendar getUpdateTime() {
        return updateTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportListInfo#removeReport(java.lang.String)
     */
    public void removeReport(String reportKey) {
        for (IExpenseReportInfo reportInfo : reportInfos) {
            if (reportInfo.getReport() != null && reportInfo.getReport().reportKey.equalsIgnoreCase(reportKey)) {
                reportInfos.remove(reportInfo);
                break;
            }
        }
    }
}
