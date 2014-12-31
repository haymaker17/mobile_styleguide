/**
 * 
 */
package com.concur.mobile.core.expense.data;

import java.util.Calendar;
import java.util.List;

import com.concur.mobile.core.data.IExpenseReportInfo;
import com.concur.mobile.core.data.IExpenseReportInfo.ReportType;
import com.concur.mobile.core.expense.report.data.AttendeeSearchField;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.IExpenseReportListInfo;

/**
 * Provides an interface for interacting with an expense approval cache.
 * 
 * @author AndrewK
 */
public interface IExpenseReportCache {

    /**
     * Whether there is a detailed report request pending.
     * 
     * @return whether there is a detailed report request pending.
     */
    public boolean isDetailedReportRequestPending();

    /**
     * Sets whether there is a detailed report request pending.
     * 
     * @param requestPending
     *            whether there is a detailed report request pending.
     */
    public void setDetailedReportRequestPending(boolean requestPending);

    /**
     * Gets an instance of <code>ExpenseReport</code> based on the key <code>reportKey</code>.
     * 
     * @param reportKey
     *            the report key.
     * 
     * @return an instance of <code>ExpenseReport</code>.
     */
    public ExpenseReport getReport(String reportKey);

    /**
     * Gets the report type that this cache serves.
     * 
     * @return the report type.
     */
    public ReportType getReportType();

    /**
     * Gets an instance of <code>ExpenseReportEntry</code> associated with <code>expRep</code> with the expense report entry key
     * <code>expRepEntKey</code>.
     * 
     * @param expRep
     *            the expense report containing the entry.
     * @param expRepEntKey
     *            the entry key of the expense report entry.
     * 
     * @return an instance of <code>ExpenseReportEntry</code> within <code>expRep</code> with key <code>expRepEntKey</code>;
     *         <code>null</code> if not found.
     */
    public ExpenseReportEntry getReportEntry(ExpenseReport expRep, String expRepEntKey);

    /**
     * Whether the cache has a detailed report for the report identified by <code>reportKey</code>.
     * 
     * @param reportKey
     *            the report key.
     * 
     * @return will return <code>true</code> if the cache has the report; <code>false</code> otherwise.
     */
    public boolean hasReportDetail(String reportKey);

    /**
     * Will place <code>reportInfo</code> into the cache.
     * 
     * @param reportInfo
     *            the detail report info to place into the cache.
     */
    public void putReportDetail(IExpenseReportInfo reportInfo);

    /**
     * Will get the detailed report based on <code>reportKey</code>.
     * 
     * @param reportKey
     *            the detailed report based on <code>reportKey</code>; otherwise <code>null</code>.
     */
    public ExpenseReportDetail getReportDetail(String reportKey);

    /**
     * Gets the list of reports.
     * 
     * @return the list of reports to approve; <code>null</code> otherwise.
     */
    public List<ExpenseReport> getReportList();

    /**
     * Gets a list of reports in the cache substituting detailed reports for summary reports if a detailed report exists and is
     * more current then the summary report.
     * 
     * @return the list of <code>ExpenseReport</code> objects.
     */
    public List<ExpenseReport> getReportListDetail();

    /**
     * Sets the "list of reports" info in the cache.
     * 
     * @param reportListInfo
     *            the "list of reports" info object.
     */
    public void setReportList(IExpenseReportListInfo reportListInfo);

    /**
     * Will delete from the report detail cache (memory+persistent) detailed report information for reports not in the current
     * version of the "reports to approve" list.
     */
    public void clearDetailReportsNotInApproveList();

    /**
     * Will delete the detail report from the cache (in-memory and persistent) keyed by <code>reportKey</code>.
     * 
     * @param reportKey
     *            the report key of the detail report to purge.
     */
    public void deleteDetailReport(String reportKey);

    /**
     * Will delete the report from the cache (in-memory and persistent) keyed by <code>reportKey</code>.
     * 
     * @param reportKey
     *            the report key of the report to purge.
     */
    public void deleteReport(String reportKey);

    /**
     * Whether the report identified by <code>reportKey</code> has been submitted into the approval process for approval, i.e.,
     * not the act of approving a report.
     * 
     * <b>NOTE:</b> This call is different than the 'isSubmittedForApproval' in that this reflects the employee that is submitting
     * an expense report for approval rather than approving an expense report.
     * 
     * <b>NOTE:</b> The first call to this method will result in the list of reports submitted for approval to be retrieved from
     * persistence. Subsequent calls will use in-memory cached data.
     * 
     * @param reportKey
     *            the key of the report.
     * 
     * @return will return <code>true</code> if the report identified by <code>reportKey</code> has been submitted;
     *         <code>false</code> otherwise.
     */
    public boolean isSubmitted(String reportKey);

    /**
     * Will add the key of a report that has been submitted into the approval process.
     * 
     * @param reportKey
     *            the report key.
     */
    public void addSubmitted(String reportKey);

    /**
     * Will remove the key of a report that has been submitted into the approval process.
     * 
     * @param reportKey
     *            the report key.
     */
    public void removeSubmitted(String reportKey);

    /**
     * Whether the report identified by <code>reportKey</code> has been submitted for approval.
     * 
     * <b>NOTE:</b> The first call to this method will result in the list of reports submitted for approval to be retrieved from
     * persistence. Subsequent calls will use in-memory cached data.
     * 
     * @param reportKey
     *            the key of the report.
     * 
     * @return will return <code>true</code> if the report identified by <code>reportKey</code> has been submitted;
     *         <code>false</code> otherwise.
     */
    public boolean isSubmittedForApprove(String reportKey);

    /**
     * Will add the key of a report that has been submitted for approval.
     * 
     * @param reportKey
     *            the report key.
     */
    public void addSubmittedForApprove(String reportKey);

    /**
     * Will remove the key of a report that has been submitted for approval.
     * 
     * @param reportKey
     *            the report key.
     */
    public void removeSubmittedForApprove(String reportKey);

    /**
     * Whether the report identified by <code>reportKey</code> has been submitted for rejection.
     * 
     * <b>NOTE:</b> The first call to this method will result in the list of reports submitted for rejection to be retrieved from
     * persistence. Subsequent calls will use in-memory cached data.
     * 
     * @param reportKey
     *            the key of the report.
     * 
     * @return will return <code>true</code> if the report identified by <code>reportKey</code> has been submitted;
     *         <code>false</code> otherwise.
     */
    public boolean isSubmittedForReject(String reportKey);

    /**
     * Will add the key of a report that has been submitted for rejection.
     * 
     * @param reportKey
     *            the report key.
     */
    public void addSubmittedForReject(String reportKey);

    /**
     * Will remove the key of a report that has been submitted for approval.
     * 
     * @param reportKey
     *            the report key.
     */
    public void removeSubmittedForReject(String reportKey);

    /**
     * Gets the time at which the last "reports to approve" list was received.
     * 
     * @return the time at which the last "reports to approve" list was received.
     */
    public Calendar getLastReportListUpdateTime();

    /**
     * Gets whether or not this cache has a report list.
     * 
     * @return whether a report list has been set on the cache.
     */
    public boolean hasLastReportList();

    /**
     * Determines whether the last report list update is older than <code>expiration</code> milliseconds.
     * 
     * <br>
     * <b>NOTE:</b><br>
     * Clients should call <code>hasLastReportList</code> to determine whether the cache has a report list backed by persistence.
     * 
     * @param expiration
     *            the expiration time in milliseconds.
     * 
     * @return If the cache has a report list, then will return <code>true</code> if the last update time is older than
     *         <code>expiration</code> milliseconds; otherwise, <code>false</code> will be returned. If the cache has no report
     *         list, then <code>false</code> will be returned.
     */
    public boolean isLastReportListUpdateExpired(long expiration);

    /**
     * Gets the time at which the last detailed report was received for a particular report.
     * 
     * @param reportKey
     *            the detailed report key.
     * 
     * @return the time at which the last detailed report was received for a particulr report.
     */
    public Calendar getLastDetailReportUpdateTime(String reportKey);

    /**
     * Determines whether the last detail report update is older than <code>expiration</code> milliseconds.
     * 
     * <br>
     * <b>NOTE:</b><br>
     * Clients should call <code>hasReportDetail</code> to determine whether the cache has a report detail backed by persistence.
     * 
     * @param reportKey
     *            the report key.
     * @param expiration
     *            the expiration time in milliseconds.
     * 
     * @return If the cache has a report detail, then will return <code>true</code> if the last update time is older than
     *         <code>expiration</code> milliseconds; otherwise, <code>false</code> will be returned. If the cache has no report
     *         detail, then <code>false</code> will be returned.
     */
    public boolean isLastDetailReportUpdateExpired(String reportKey, long expiration);

    /**
     * Gets the list of <code>ExpenseReportEntry</code> objects associated with the report whose key is <code>reportKey</code>.
     * 
     * @param reportKey
     *            the report key.
     * @return returns a list of <code>ExpenseReportEntry</code> objects associated with the report whose key is
     *         <code>reportKey</code>.
     */
    public List<ExpenseReportEntry> getReportEntries(String reportKey);

    /**
     * Gets the list of <code>ExpenseReportEntry</code> objects associated with the report whose key is <code>reportKey</code> and
     * parent report entry key is <code>parentReportEntryKey</code>.
     * 
     * @param reportKey
     *            the report key.
     * @param parentReportEntryKey
     *            the parent report entry key.
     * @return returns a list of <code>ExpenseReportEntry</code> objects associated with the report whose key is
     *         <code>reportKey</code>.
     */
    public List<ExpenseReportEntry> getReportItemizationEntries(String reportKey, String parentReportEntryKey);

    /**
     * Sets the report list refetched flag from the server.
     */
    public void setShouldFetchReportList();

    /**
     * Contains whether an activity has altered a report in such a fashion that the report list should be retrieved again from the
     * server.
     * 
     * @return whether the report list should be refetched.
     */
    public boolean shouldRefetchReportList();

    /**
     * Clears the flag indicating that the report list should be refetched.
     */
    public void clearShouldRefetchReportList();

    /**
     * Sets whether the report list should be refreshed using local data.
     */
    public void setShouldRefreshReportList();

    /**
     * Gets whether the report list should be refreshed locally.
     * 
     * @return whether the report list should be refreshed using local data.
     */
    public boolean shouldRefreshReportList();

    /**
     * Clears the flag indicating the report list should refreshed using local data.
     */
    public void clearShouldRefreshReportList();

    /**
     * Will return the list of <code>ListItem</code> objects containing attendee type key values. If an expense type is provided,
     * filter the attendee list based on information in the expense type.<br>
     * 
     * @param expType
     *            an instance of <code>ExpenseType</code> used to filter <code>ListItem</code> objects in the
     *            <code>attendeeTypes</code> list.
     * @param attendeeTypes
     *            a list of <code>ListItem</code> objects containing attendee type information. This passed in list is the result
     *            of a list search with 'FieldId' set to 'AtnTypeKey' with 'FtCode' set to
     *            <code>Const.ATTENDEE_SEARCH_LIST_FT_CODE</code> to identify attendees for search versus an 'FtCode' of
     *            <code>null</code> identifying attendee types for attendee "add" (creation).
     * 
     * @return the list of <code>ListItem</code> objects containing attendee type keys.
     */
    public List<ListItem> getAttendeeTypes(ExpenseType expType, List<ListItem> attendeeTypes);

    /**
     * Sets the list of <code>ListItem</code> objects containing attendee type key values specific for performing an "add"
     * attendee operation, i.e., to create an attendee.
     * 
     * @param types
     *            the list of <code>ListItem</code> objects containing attendee type key values.
     */
    public void setAddAttendeeTypes(List<ListItem> types);

    /**
     * Gets the list of <code>ListItem</code> objects containing attendee type key values specific for performing an "add"
     * attendee operation, i.e., to create an attendee.
     * 
     * @return returns the list of <code>ListItem</code> objects containing attendee type key values.
     */
    public List<ListItem> getAddAttendeeTypes();

    /**
     * Sets the list of <code>ListItem</code> objects containing attendee type key values specific for performing attendee search.
     * 
     * @param types
     *            the list of <code>ListItem</code> objects containing attendee type key values.
     */
    public void setSearchAttendeeTypes(List<ListItem> types);

    /**
     * Gets the list of <code>ListItem</code> objects containing attendee type key values specific for performing attendee search.
     * 
     * @return returns the list of <code>ListItem</code> objects containing attendee type key values.
     */
    public List<ListItem> getSearchAttendeeTypes();

    /**
     * Sets the default attendee reflecting the current mobile user.
     * 
     * @param attendee
     *            the default attendee.
     */
    public void setDefaultAttendee(ExpenseReportAttendee attendee);

    /**
     * Gets the default attendee.
     * 
     * @return the default attendee.
     */
    public ExpenseReportAttendee getDefaultAttendee();

    /**
     * Sets the list of attendee search fields.
     * 
     * @param atnSrchFlds
     *            a list of <code>AttendeeSearchField</code> objects.
     */
    public void setAttendeeSearchFields(List<AttendeeSearchField> atnSrchFlds);

    /**
     * Gets the list of attendee search fields.
     * 
     * @return a list of <code>AttendeeSearchField</code> objects if downloaded from the server; otherwise <code>null</code> is
     *         returned.
     */
    public List<AttendeeSearchField> getAttendeeSearchFields();

}
