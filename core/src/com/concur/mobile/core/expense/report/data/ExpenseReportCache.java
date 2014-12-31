/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.IExpenseReportInfo;
import com.concur.mobile.core.data.IExpenseReportInfo.ReportType;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;

/**
 * An implementation of <code>IExpenseReportCache</code> for retrieving expense report information.
 * 
 * @author AndrewK
 */
public class ExpenseReportCache implements IExpenseReportCache {

    private static String CLS_TAG = ExpenseReportCache.class.getSimpleName();

    // Contains the cache report type.
    private ReportType type;

    /**
     * Contains the expense report list information.
     */
    private IExpenseReportListInfo expenseReportsInfo;

    /**
     * Contains a map from a report key to the report detail info objects.
     */
    private HashMap<String, IExpenseReportInfo> reportDetailInfoMap = new HashMap<String, IExpenseReportInfo>();

    /**
     * Contains a list of report keys representing reports that have been submitted into the report approval process by a
     * submittee requesting reimbursement. This list contains those submitted reports for which a server response is pending. In
     * general, items in this list should have a short life.
     */
    private ArrayList<String> reportsSubmitted;

    /**
     * Contains a list of the reports that have been submitted for approval. This list contains those submitted reports for which
     * a server response is pending. In general, items in this list should have a short life.
     */
    private ArrayList<String> reportsSubmittedApprove;

    /**
     * Contains a list of the reports that have been submitted for rejection. This list contains those submitted reports for which
     * a server response is pending. In general, items in this list should have a short life.
     */
    private ArrayList<String> reportsSubmittedReject;

    /**
     * Contains a reference to the Concur mobile application object.
     */
    private ConcurCore concurMobile;

    /**
     * Contains whether or not there is a pending request to retrieve detailed report data.
     */
    private boolean detailedReportRequestPending;

    /**
     * Contains whether the the report list should be refetched by an activity requiring up to date report information.
     */
    private boolean refetchReportList;

    /**
     * Contains whether the cache has changed such that any report lists should be regenerated.
     */
    private boolean refreshReportList;

    /**
     * Contains the list of <code>ListItem</code> objects containing attendee type keys used in an "add" attendee, i.e., create an
     * attendee.
     */
    private List<ListItem> addAttendeeTypes;

    /**
     * Contains the list of <code>ListItem</code> objects containing attendee type keys used in an "add" attendee, i.e., create an
     * attendee.
     */
    private List<ListItem> searchAttendeeTypes;

    /**
     * Contains the default attendee representing the current mobile user.
     */
    private ExpenseReportAttendee defaultAttendee;

    /**
     * Contains the list of attendee search fields.
     */
    private List<AttendeeSearchField> atnSrchFields;

    /**
     * Constructs an instance of <code>ExpenseReportCache</code> passing in a reference to the concur mobile application instance.
     * 
     * @param type
     *            the cache report type.
     * @param concurMobile
     *            the concur mobile application instance.
     */
    public ExpenseReportCache(ReportType type, ConcurCore concurMobile) {
        this.type = type;
        this.concurMobile = concurMobile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#isDetailedReportRequestPending()
     */
    public synchronized boolean isDetailedReportRequestPending() {
        return detailedReportRequestPending;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#setDetailedReportRequestPending(boolean)
     */
    public synchronized void setDetailedReportRequestPending(boolean requestPending) {
        detailedReportRequestPending = requestPending;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#getReportType()
     */
    public ReportType getReportType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#getReport(java.lang.String)
     */
    public ExpenseReport getReport(String reportKey) {

        ExpenseReport expRep = null;

        if (expenseReportsInfo == null) {
            loadReportListFromPersistence();
        }
        if (expenseReportsInfo != null) {
            Iterator<ExpenseReport> reportsIterator = expenseReportsInfo.getReports().iterator();
            while (reportsIterator.hasNext()) {
                expRep = reportsIterator.next();
                if (expRep.reportKey.equalsIgnoreCase(reportKey)) {
                    break;
                }
                expRep = null;
            }
        }
        return expRep;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#getReportEntry(com.concur.mobile.data.expense.ExpenseReport,
     * java.lang.String)
     */
    public ExpenseReportEntry getReportEntry(ExpenseReport expRep, String expRepEntKey) {

        ExpenseReportEntry expRepEnt = null;

        if (expRep != null) {
            if (expRepEntKey != null) {
                expRepEnt = expRep.findEntryByReportKey(expRepEntKey);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getReportEntry: null expense entry key!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getReportEntry: null expense report!");
        }

        return expRepEnt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#getReportDetail(java.lang.String)
     */
    public ExpenseReportDetail getReportDetail(String reportKey) {

        ExpenseReportDetail report = null;

        // Try to load from the persistent cache.
        if (!reportDetailInfoMap.containsKey(reportKey)) {
            IExpenseReportInfo reportInfo = concurMobile.getService().getReportDetail(reportKey, type);
            if (reportInfo != null) {
                reportDetailInfoMap.put(reportKey, reportInfo);
                report = (ExpenseReportDetail) reportInfo.getReport();
            }
        } else {
            report = (ExpenseReportDetail) reportDetailInfoMap.get(reportKey).getReport();
        }
        return report;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#getReportList()
     */
    public List<ExpenseReport> getReportList() {

        List<ExpenseReport> reportsToApprove = null;
        if (expenseReportsInfo == null) {
            loadReportListFromPersistence();
        }
        if (expenseReportsInfo != null) {
            reportsToApprove = expenseReportsInfo.getReports();
        }
        return reportsToApprove;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#getReportListDetail()
     */
    public List<ExpenseReport> getReportListDetail() {
        List<ExpenseReport> reportList = getReportList();
        if (reportList != null) {
            // First, use current active report list as a guide to pulling detailed report objects
            // and comparing their update times versus the active report list update time.
            ListIterator<ExpenseReport> listIter = reportList.listIterator();
            Calendar reportListUpdateTime = getLastReportListUpdateTime();
            List<String> repIds = new ArrayList<String>(reportList.size());
            while (listIter.hasNext()) {
                ExpenseReport summaryReport = listIter.next();
                repIds.add(summaryReport.reportKey);
                if (hasReportDetail(summaryReport.reportKey)) {
                    Calendar reportDetailUpdateTime = getLastDetailReportUpdateTime(summaryReport.reportKey);
                    if (reportDetailUpdateTime.after(reportListUpdateTime)) {
                        // Report detail is newer than summary object, use the report detail object.
                        listIter.set(getReportDetail(summaryReport.reportKey));
                    }
                }
            }
            // Populating an active report list?
            if (type == ReportType.ACTIVE) {
                // Second, in some cases, an active detailed report can exist that is not in an updated
                // active report list, in that case, compare it's update time versus that of the active report
                // list update time and add.
                ConcurService concurService = concurMobile.getService();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                        .getApplicationContext());
                String userId = prefs.getString(Const.PREF_USER_ID, null);
                List<IExpenseReportInfo> repDetInfos = concurService.getReportDetailInfos(userId, type);
                if (repDetInfos != null) {
                    for (IExpenseReportInfo reportInfo : repDetInfos) {
                        // Check whether the active report list doesn't already contain the report key from the detail
                        // object.
                        if (!repIds.contains(reportInfo.getReportKey())) {
                            // Check whether the report detail update time is newer than report list update time.
                            if (reportInfo.getUpdateTime().after(reportListUpdateTime)) {
                                reportList.add(getReportDetail(reportInfo.getReportKey()));
                            }
                        }
                    }
                }
            }
        }
        return reportList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#hasReportDetail(java.lang.String)
     */
    public boolean hasReportDetail(String reportKey) {

        boolean result = reportDetailInfoMap.containsKey(reportKey);
        if (!result) {
            IExpenseReportInfo reportInfo = concurMobile.getService().getReportDetail(reportKey, type);
            if (reportInfo != null) {
                reportDetailInfoMap.put(reportKey, reportInfo);
                result = true;
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.data.expense.IExpenseApprovalCache#putReportDetail(com.concur.mobile.data.expense.IExpenseReportDetailInfo
     * )
     */
    public void putReportDetail(IExpenseReportInfo reportInfo) {
        reportDetailInfoMap.put(reportInfo.getReport().reportKey, reportInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.data.expense.IExpenseApprovalCache#setReportList(com.concur.mobile.data.expense.IExpenseReportListInfo)
     */
    public void setReportList(IExpenseReportListInfo reportListInfo) {
        expenseReportsInfo = reportListInfo;
        // Get the list of report keys in the "reports to approve" list.
        ArrayList<String> approveReportKeyList = new ArrayList<String>();
        if (expenseReportsInfo != null) {
            Iterator<ExpenseReport> iter = expenseReportsInfo.getReports().iterator();
            while (iter.hasNext()) {
                ExpenseReport expRep = iter.next();
                if (expRep != null) {
                    approveReportKeyList.add(expRep.reportKey);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".setReportList: null report entry in list!");
                }
            }
        }
        // Make a call to the service to punt persisted reports.
        concurMobile.getService().deleteReportNotInReportKeyList(approveReportKeyList, type, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#clearDetailReportsNotInApproveList()
     */
    public void clearDetailReportsNotInApproveList() {

        // Get the list of report keys in the "reports to approve" list.
        ArrayList<String> approveReportKeyList = new ArrayList<String>();
        if (expenseReportsInfo != null) {
            Iterator<ExpenseReport> iter = expenseReportsInfo.getReports().iterator();
            while (iter.hasNext()) {
                ExpenseReport expRep = iter.next();
                if (expRep != null) {
                    approveReportKeyList.add(expRep.reportKey);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".clearDetailReportsNotInApproveList: null report entry in list!");
                }
            }
        }

        // Iterate over the entries in 'reportDetailMap' punting those whose keys are not
        // in 'approveReportKeyList'.
        if (reportDetailInfoMap != null) {
            Iterator<String> keys = reportDetailInfoMap.keySet().iterator();
            while (keys.hasNext()) {
                String reportDetailKey = keys.next();
                if (!approveReportKeyList.contains(reportDetailKey)) {
                    // Punt from the underlying keyset and subsequent map.
                    keys.remove();
                }
            }
        }

        // Make a call to the service to punt persisted detailed reports.
        concurMobile.getService().deleteReportNotInReportKeyList(approveReportKeyList, type, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#deleteDetailReport(java.lang.String)
     */
    public void deleteDetailReport(String reportKey) {

        // First, purge it from the in-memory map.
        if (reportDetailInfoMap != null && reportDetailInfoMap.containsKey(reportKey)) {
            // Punt from the detail map.
            reportDetailInfoMap.remove(reportKey);
        }

        // Second, purge it from the persistent store.
        concurMobile.getService().deleteDetailReport(reportKey, type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#deleteReport(java.lang.String)
     */
    public void deleteReport(String reportKey) {
        ExpenseReport rpt = getReport(reportKey);
        // First, purge it from the in-memory list.
        if (rpt != null) {
            expenseReportsInfo.removeReport(rpt.reportKey);
        }
        // Second, purge it from the persistent store.
        concurMobile.getService().deleteReport(reportKey, type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#addSubmitted(java.lang.String)
     */
    public void addSubmitted(String reportKey) {
        if (reportsSubmitted != null) {
            if (!reportsSubmitted.contains(reportKey)) {
                reportsSubmitted.add(reportKey);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + "addSubmitted: report with key '" + reportKey + "' already in list!");
            }
        } else {
            // No-op. Only the 'isSubmitted' will initialize/load from the DB.
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#addSubmittedForApprove(java.lang.String)
     */
    public void addSubmittedForApprove(String reportKey) {
        if (reportsSubmittedApprove != null) {
            if (!reportsSubmittedApprove.contains(reportKey)) {
                reportsSubmittedApprove.add(reportKey);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + "addSubmittedForApproval: report with key '" + reportKey
                        + "' already in list!");
            }
        } else {
            // No-op. Only the 'isSubmittedFor<Approve|Reject>' will initialize/load from the
            // DB.
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#addSubmittedForReject(java.lang.String)
     */
    public void addSubmittedForReject(String reportKey) {
        if (reportsSubmittedReject != null) {
            if (!reportsSubmittedReject.contains(reportKey)) {
                reportsSubmittedReject.add(reportKey);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + "addSubmittedForReject: report with key '" + reportKey
                        + "' already in list!");
            }
        } else {
            // No-op. Only the 'isSubmittedFor<Approve|Reject>' will initialize/load from the
            // DB.
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#isSubmitted(java.lang.String)
     */
    public boolean isSubmitted(String reportKey) {
        boolean result = false;

        // Load from the service if needbe.
        if (reportsSubmitted == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            reportsSubmitted = concurMobile.getService().getReportsSubmitted(prefs.getString(Const.PREF_USER_ID, null));
        }
        result = reportsSubmitted.contains(reportKey);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#isSubmittedForApprove(java.lang.String)
     */
    public boolean isSubmittedForApprove(String reportKey) {
        boolean result = false;

        // Load from the service if needbe.
        if (reportsSubmittedApprove == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            reportsSubmittedApprove = concurMobile.getService().getReportsSubmittedApprove(
                    prefs.getString(Const.PREF_USER_ID, null));
        }
        result = reportsSubmittedApprove.contains(reportKey);

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#isSubmittedForReject(java.lang.String)
     */
    public boolean isSubmittedForReject(String reportKey) {
        boolean result = false;

        // Load from the service if needbe.
        if (reportsSubmittedReject == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            reportsSubmittedReject = concurMobile.getService().getReportsSubmittedReject(
                    prefs.getString(Const.PREF_USER_ID, null));
        }
        result = reportsSubmittedReject.contains(reportKey);

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#removeSubmitted(java.lang.String)
     */
    public void removeSubmitted(String reportKey) {
        if (reportsSubmitted != null) {
            if (reportsSubmitted.contains(reportKey)) {
                reportsSubmitted.remove(reportKey);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + "removeSubmitted: report with key '" + reportKey + "' not in list!");
            }
        } else {
            // No-op. Only the 'isSubmitted' will initialize/load from the DB.
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#removeSubmittedForApprove(java.lang.String)
     */
    public void removeSubmittedForApprove(String reportKey) {
        if (reportsSubmittedApprove != null) {
            if (reportsSubmittedApprove.contains(reportKey)) {
                reportsSubmittedApprove.remove(reportKey);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + "removeSubmittedForApproval: report with key '" + reportKey
                        + "' not in list!");
            }
        } else {
            // No-op. Only the 'isSubmittedFor<Approve|Reject>' will initialize/load from the
            // DB.
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#removeSubmittedForReject(java.lang.String)
     */
    public void removeSubmittedForReject(String reportKey) {
        if (reportsSubmittedReject != null) {
            if (reportsSubmittedReject.contains(reportKey)) {
                reportsSubmittedReject.remove(reportKey);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + "removeSubmittedForReject: report with key '" + reportKey
                        + "' not in list!");
            }
        } else {
            // No-op. Only the 'isSubmittedFor<Approve|Reject>' will initialize/load from the
            // DB.
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#getReportEntries(java.lang.String)
     */
    public List<ExpenseReportEntry> getReportEntries(String reportKey) {
        IExpenseReportInfo rptInfo = concurMobile.getService().getReportDetail(reportKey, type);
        if (rptInfo != null) {
            ExpenseReport expRep = rptInfo.getReport();
            if (expRep != null) {
                return expRep.getExpenseEntries();
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#getReportItemizationEntries(java.lang.String, java.lang.String)
     */
    public List<ExpenseReportEntry> getReportItemizationEntries(String reportKey, String parentReportEntryKey) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#getLastDetailReportUpdateTime(java.lang.String)
     */
    public Calendar getLastDetailReportUpdateTime(String reportKey) {

        Calendar updateTime = null;
        if (reportDetailInfoMap.containsKey(reportKey)) {
            IExpenseReportInfo reportInfo = reportDetailInfoMap.get(reportKey);
            updateTime = reportInfo.getUpdateTime();
        }
        return updateTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#isLastDetailReportUpdateExpired(java.lang.String, long)
     */
    public boolean isLastDetailReportUpdateExpired(String reportKey, long expiration) {
        boolean retVal = false;
        if (hasReportDetail(reportKey)) {
            Calendar updateTime = getLastDetailReportUpdateTime(reportKey);
            Calendar updateTimeList = getLastReportListUpdateTime();
            if (updateTime != null && updateTimeList != null) {
                long curTimeMillis = System.currentTimeMillis();
                try {
                    long updateTimeMillis = updateTime.getTimeInMillis();
                    long updateTimeMillisList = updateTimeList.getTimeInMillis();
                    if (updateTimeMillisList > updateTimeMillis) {
                        retVal = true;
                    } else {
                        retVal = ((curTimeMillis - updateTimeMillis) > expiration);
                    }
                } catch (IllegalArgumentException ilaArgExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".isLastDetailReportUpdateExpired: unable to get millisecond time from 'updateTime'!",
                            ilaArgExc);
                    // Err to the side of caution.
                    retVal = true;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".isLastDetailReportUpdateExpired: report detail info has null 'updateTime'!");
                // Err to the side of caution.
                retVal = true;
            }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseApprovalCache#getLastReportListUpdateTime()
     */
    public Calendar getLastReportListUpdateTime() {
        Calendar updateTime = null;
        if (expenseReportsInfo != null) {
            updateTime = expenseReportsInfo.getUpdateTime();
        }
        return updateTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#hasLastReportList()
     */
    public boolean hasLastReportList() {
        if (expenseReportsInfo == null) {
            loadReportListFromPersistence();
        }
        return (expenseReportsInfo != null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#isLastReportListUpdateExpired(long)
     */
    public boolean isLastReportListUpdateExpired(long expiration) {
        boolean retVal = false;
        if (hasLastReportList()) {
            Calendar updateTime = expenseReportsInfo.getUpdateTime();
            if (updateTime != null) {
                long curTimeMillis = System.currentTimeMillis();
                try {
                    long updateTimeMillis = updateTime.getTimeInMillis();
                    retVal = ((curTimeMillis - updateTimeMillis) > expiration);
                } catch (IllegalArgumentException ilaArgExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".isLastReportListUpdateExpired: unable to get millisecond time from 'updateTime'!",
                            ilaArgExc);
                    // Err to the side of caution.
                    retVal = true;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".isLastReportListUpdateExpired: expense report info has null 'updateTime'!");
                // Err to the side of caution.
                retVal = true;
            }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#clearShouldRefetchReportList()
     */
    public void clearShouldRefetchReportList() {
        refetchReportList = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#shouldRefetchReportList()
     */
    public boolean shouldRefetchReportList() {
        return refetchReportList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#setShouldFetchReportList()
     */
    public void setShouldFetchReportList() {
        refetchReportList = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#clearShouldRefreshReportList()
     */
    public void clearShouldRefreshReportList() {
        refreshReportList = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#setShouldRefreshReportList()
     */
    public void setShouldRefreshReportList() {
        refreshReportList = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#shouldRefreshReportList()
     */
    public boolean shouldRefreshReportList() {
        return refreshReportList;
    }

    public List<ListItem> getAttendeeTypes(ExpenseType expType, List<ListItem> attendeeTypes) {
        List<ListItem> atnTypes = attendeeTypes;

        if (attendeeTypes != null && expType != null && expType.unallowedAttendeeTypeKeys != null) {
            atnTypes = new ArrayList<ListItem>(attendeeTypes.size());
            for (ListItem atnType : attendeeTypes) {
                int l = expType.unallowedAttendeeTypeKeys.length;
                boolean keep = true;
                for (int i = 0; i < l; i++) {
                    if (expType.unallowedAttendeeTypeKeys[i].equals(atnType.key)) {
                        keep = false;
                        break;
                    }
                }
                if (keep) {
                    atnTypes.add(atnType);
                }
            }
        }

        return atnTypes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#setAttendeeTypes(java.util.List)
     */
    public void setAddAttendeeTypes(List<ListItem> types) {
        addAttendeeTypes = types;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.data.IExpenseReportCache#getAddAttendeeTypes()
     */
    public List<ListItem> getAddAttendeeTypes() {
        return addAttendeeTypes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.data.IExpenseReportCache#setSearchAttendeeTypes(java.util.List)
     */
    @Override
    public void setSearchAttendeeTypes(List<ListItem> types) {
        searchAttendeeTypes = types;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.data.IExpenseReportCache#getSearchAttendeeTypes()
     */
    public List<ListItem> getSearchAttendeeTypes() {
        return searchAttendeeTypes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseReportCache#getDefaultAttendee()
     */
    public ExpenseReportAttendee getDefaultAttendee() {
        return defaultAttendee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.data.expense.IExpenseReportCache#setDefaultAttendee(com.concur.mobile.data.expense.ExpenseReportAttendee)
     */
    public void setDefaultAttendee(ExpenseReportAttendee attendee) {
        defaultAttendee = attendee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.data.IExpenseReportCache#getAttendeeSearchFields()
     */
    @Override
    public List<AttendeeSearchField> getAttendeeSearchFields() {
        return atnSrchFields;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.data.IExpenseReportCache#setAttendeeSearchFields(java.util.List)
     */
    @Override
    public void setAttendeeSearchFields(List<AttendeeSearchField> atnSrchFlds) {
        this.atnSrchFields = atnSrchFlds;
    }

    /**
     * Will attempt to load the report list from persistence.
     */
    private void loadReportListFromPersistence() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile.getApplicationContext());
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        ConcurService concurService = concurMobile.getService();
        if (concurService != null) {
            expenseReportsInfo = concurService.getReports(userId, type, false);
        }
    }
}
