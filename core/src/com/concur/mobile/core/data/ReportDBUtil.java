/**
 * 
 */
package com.concur.mobile.core.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.util.Log;

import com.concur.mobile.core.data.IExpenseReportInfo.ReportType;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.util.Const;

/**
 * Provides utility methods to assemble and persist both header and entry information for reports to/from the database.
 * 
 * @author andy
 */
public class ReportDBUtil {

    private static final String CLS_TAG = ReportDBUtil.class.getSimpleName();

    /**
     * Will update persistence with the contents of the report header from <code>report</code>.
     * 
     * @param db
     *            a reference to the database.
     * @param type
     *            the report type.
     * @param report
     *            the report.
     * @param userId
     *            the user id associated with the report header data.
     * @param updateTime
     *            the update time.
     * @param headerDBKey
     *            the local database primary key for the report header. May be <code>-1</code> if report is being inserted.
     */
    public static IExpenseReportInfo updateReportHeader(MobileDatabase db, ReportType type, ExpenseReport report,
            String userId, Calendar updateTime) {
        synchronized (ReportDBUtil.class) {

            final String MTAG = CLS_TAG + ".updateReportHeader: ";
            IExpenseReportDBInfo reportInfo = null;
            // Perform an assertion check.
            try {
                Assert.assertNotNull(MTAG + "db is null!", db);
                Assert.assertNotNull(MTAG + "report type is null!", type);
                Assert.assertNotNull(MTAG + "user id is null!", userId);
                Assert.assertNotNull(MTAG + "report is null!", report);
                Assert.assertNotNull(MTAG + "update is null!", updateTime);

                // Update the header.
                StringBuilder strBldr = new StringBuilder();
                ExpenseReportDetail.ReportDetailSAXHandler.serializeReportHeader(strBldr, report);
                // Update the report.
                reportInfo = db.updateReportHeader(strBldr.toString(), report.reportKey, type, userId,
                        report.isDetail(), updateTime);
                // Check for an updated header.
                Assert.assertNotNull(MTAG + "could not update report header!", reportInfo);
                reportInfo.setReport(report);
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                reportInfo = null;
            }
            return reportInfo;
        }
    }

    /**
     * Will load the set of reports for a specific report type.
     * 
     * @param db
     *            a reference to the database.
     * @param type
     *            the type of the report.
     * @param userId
     *            the user id associated with the report data.
     * @param detail
     *            whether detailed reports should be loaded.
     * @return a list of <code>IExpenseReportInfo</code> objects containing the report information.
     */
    public static List<IExpenseReportInfo> loadReports(MobileDatabase db, ReportType type, String userId, boolean detail) {
        synchronized (ReportDBUtil.class) {

            final String MTAG = CLS_TAG + ".loadSummaryReports: ";
            List<IExpenseReportInfo> reportInfos = null;

            // Perform an assertion check.
            try {
                Assert.assertNotNull(MTAG + "db is null!", db);
                Assert.assertNotNull(MTAG + "report type is null!", type);
                Assert.assertNotNull(MTAG + "user id is null!", userId);

                // Load the summary reports.
                List<IExpenseReportDBInfo> headers = db.loadReportHeaders(type, userId, detail, true);
                if (headers != null) {
                    reportInfos = new ArrayList<IExpenseReportInfo>(headers.size());
                    for (IExpenseReportDBInfo headerInfo : headers) {
                        // Parse the header into an ExpenseReport object.
                        if (headerInfo.getXML() != null) {
                            ExpenseReport expRep = null;
                            if (detail) {
                                expRep = ExpenseReportDetail.parseReportHeaderDetail(headerInfo.getXML());
                            } else {
                                expRep = ExpenseReport.parseReportHeaderSummary(headerInfo.getXML());
                            }
                            if (expRep != null) {
                                // Set the expenseEntriesMap from the report back on to the static ExpenseReport reference.
                                // This will ensure that an parsing of expense entries below get added onto the map associated
                                // with the report.
                                ExpenseReport.reportEntryMap = expRep.reportExpenseEntryMap;
                                // Add the expense report.
                                reportInfos.add(headerInfo);
                                // Set the report on the info object.
                                headerInfo.setReport(expRep);
                                // Grab the report header key for 'headerInfo'.
                                long reportHeaderDBKey = db.lookUpReportHeaderID(headerInfo.getReportKey(),
                                        headerInfo.getReportType(), headerInfo.isDetail(), userId);
                                try {
                                    // Check report header key.
                                    Assert.assertTrue(MTAG + "lookup failed for report header id!",
                                            (reportHeaderDBKey != -1L));
                                    // Retrieve the entries.
                                    List<IExpenseReportEntryInfo> entries = db.loadReportEntries(reportHeaderDBKey);
                                    if (entries != null) {
                                        for (IExpenseReportEntryInfo entryInfo : entries) {
                                            if (entryInfo.getXML() != null) {
                                                ExpenseReportEntry expRepEnt = null;
                                                if (entryInfo.isDetail()) {
                                                    expRepEnt = ExpenseReportEntryDetail
                                                            .parseReportEntryDetailXml(entryInfo.getXML());
                                                } else {
                                                    expRepEnt = ExpenseReportEntry.parseReportEntry(entryInfo.getXML());
                                                }
                                                if (expRepEnt != null) {
                                                    if (expRep.expenseEntries == null) {
                                                        expRep.expenseEntries = new ArrayList<ExpenseReportEntry>(
                                                                entries.size());
                                                    }
                                                    // Add the entry to the report.
                                                    expRep.expenseEntries.add(expRepEnt);
                                                } else {
                                                    Log.e(Const.LOG_TAG, MTAG + "entry XML could not be parsed!");
                                                }
                                            } else {
                                                Log.e(Const.LOG_TAG, MTAG + "entry XML is null!");
                                            }
                                        }
                                    }
                                } catch (AssertionFailedError afe) {
                                    Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                                }
                                // Clear the map.
                                ExpenseReport.reportEntryMap = null;
                            } else {
                                Log.e(Const.LOG_TAG, MTAG + "header XML could not be parsed!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, MTAG + "header XML is null!");
                        }
                    }
                }
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, afe.getMessage(), afe);
            }
            return reportInfos;
        }
    }

    /**
     * Will update a report.
     * 
     * @param db
     *            a reference to the database.
     * @param report
     *            a reference to the report to be updated.
     * @param type
     *            the report type.
     * @param detail
     *            whether this is a detailed report.
     * @param userId
     *            the user is associated with the report.
     * @param updateTime
     *            the update time.
     */
    public static IExpenseReportDBInfo updateReport(MobileDatabase db, ExpenseReport report, ReportType type,
            boolean detail, String userId, Calendar updateTime) {
        synchronized (ReportDBUtil.class) {

            final String MTAG = CLS_TAG + ".updateReport: ";
            IExpenseReportDBInfo reportInfo = null;

            // Perform an assertion check.
            try {
                Assert.assertNotNull(MTAG + "db is null!", db);
                Assert.assertNotNull(MTAG + "report type is null!", type);
                Assert.assertNotNull(MTAG + "user id is null!", userId);
                Assert.assertNotNull(MTAG + "report is null!", report);
                Assert.assertNotNull(MTAG + "update is null!", updateTime);

                // Update the report.
                // Serialize the report header.
                StringBuilder strBldr = new StringBuilder();
                ExpenseReportDetail.ReportDetailSAXHandler.serializeReportHeader(strBldr, report);
                // Update the report.
                reportInfo = db.updateReportHeader(strBldr.toString(), report.reportKey, type, userId, detail,
                        updateTime);
                // Check reportInfo.
                Assert.assertNotNull(MTAG + "unable to update report header!", reportInfo);
                reportInfo.setReport(report);
                long reportHeaderDBKey = db.lookUpReportHeaderID(reportInfo.getReportKey(), reportInfo.getReportType(),
                        reportInfo.isDetail(), userId);
                try {
                    // Check report header key.
                    Assert.assertTrue(MTAG + "lookup failed for report header id!", (reportHeaderDBKey != -1L));
                    // Remove all entries which are associated with report.
                    db.deleteReportEntries(reportHeaderDBKey);
                    // Iterate over the entries, serialize each one and update the database.
                    if (report.expenseEntries != null) {
                        for (ExpenseReportEntry entry : report.expenseEntries) {
                            // Serialize the entry.
                            strBldr.setLength(0);
                            ExpenseReportEntryDetail.ExpenseReportEntryDetailSAXHandler.serializeAllToXML(strBldr,
                                    entry);
                            // Update the report entry.
                            db.updateReportEntry(reportHeaderDBKey, userId, report.reportKey, entry.reportEntryKey,
                                    entry.isDetail(), strBldr.toString());
                        }
                    }
                } catch (AssertionFailedError afe) {
                    Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                }
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, afe.getMessage(), afe);
            }
            return reportInfo;
        }
    }

    /**
     * Will delete all reports whose keys are not contained in the <code>excludeReportKeys</code> list.
     * 
     * @param db
     *            a reference to the database.
     * @param type
     *            the report type.
     * @param userId
     *            the user id.
     * @param detail
     *            whether the reports should be detail reports.
     * @param excludeReportKeys
     *            the list of report keys to exclude.
     */
    public static void deleteReports(MobileDatabase db, ReportType type, String userId, boolean detail,
            List<String> excludeReportKeys) {
        synchronized (ReportDBUtil.class) {

            List<IExpenseReportInfo> reportInfos = loadReportInfos(db, type, userId, detail);
            if (reportInfos != null) {
                for (IExpenseReportInfo reportInfo : reportInfos) {
                    if (excludeReportKeys == null || !excludeReportKeys.contains(reportInfo.getReportKey())) {
                        deleteReport(db, type, reportInfo.getReportKey(), userId, reportInfo.isDetail());
                    }
                }
            }
        }
    }

    /**
     * Will delete a report.
     * 
     * @param db
     *            a reference to the database.
     * @param type
     *            the type of report.
     * @param reportKey
     *            the report key.
     * @param userId
     *            user id associated with the report data.
     * @param detail
     *            whether to load a detail report.
     */
    public static void deleteReport(MobileDatabase db, ReportType type, String reportKey, String userId, boolean detail) {
        synchronized (ReportDBUtil.class) {

            final String MTAG = CLS_TAG + ".deleteReport: ";
            // Perform an assertion check.
            try {
                Assert.assertNotNull(MTAG + "db is null!", db);
                Assert.assertNotNull(MTAG + "report type is null!", type);
                Assert.assertNotNull(MTAG + "reportKey is null!", reportKey);
                Assert.assertNotNull(MTAG + "user id is null!", userId);
                // Look up the report header database key.
                long reportHeaderDBKey = db.lookUpReportHeaderID(reportKey, type, detail, userId);
                if (reportHeaderDBKey != -1L) {
                    // First, punt the report header.
                    db.deleteReportHeader(reportHeaderDBKey);
                    // Second, punt the entries associated with the header.
                    db.deleteReportEntries(reportHeaderDBKey);
                }
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, afe.getMessage(), afe);
            }
        }
    }

    /**
     * Will load a report entry.
     * 
     * <br>
     * <b>NOTE:</b><br>
     * If <code>detail</code> is <code>true</code>, but only a non-detailed report entry is persisted, then <code>null</code> will
     * be returned. Otherwise, if <code>detail</code> is <code>false</code>, then a report entry info object will be returned
     * containing a non-detailed report entry.
     * 
     * @param db
     *            a reference to the database.
     * @param type
     *            the type of report.
     * @param reportKey
     *            the report key.
     * @param reportEntryKey
     *            the report entry key.
     * @param userId
     *            user id associated with the report data.
     * @param detail
     *            whether to load a detail report entry.
     * @param reportEntryMap
     *            contains a map that will be filled in with mappings from report entry keys to instances of
     *            <code>ExpenseReportEntry</code>.
     * @return an instance of <code>IExpenseReportEntryInfo</code> containing the report entry information.
     */
    public static IExpenseReportEntryInfo loadReportEntry(MobileDatabase db, ReportType type, String reportKey,
            String reportEntryKey, String userId, boolean detail, Map<String, ExpenseReportEntry> reportEntryMap) {
        synchronized (ReportDBUtil.class) {

            final String MTAG = CLS_TAG + ".loadReportEntry: ";

            IExpenseReportEntryInfo reportEntryInfo = null;

            // Perform an assertion check.
            try {
                Assert.assertNotNull(MTAG + "db is null!", db);
                Assert.assertNotNull(MTAG + "report type is null!", type);
                Assert.assertNotNull(MTAG + "reportKey is null!", reportKey);
                Assert.assertNotNull(MTAG + "reportEntryKey is null!", reportEntryKey);
                Assert.assertNotNull(MTAG + "user id is null!", userId);

                // Set the passed in map static ExpenseReport reference.
                // This will ensure that an parsing of expense entries below get added onto the map associated
                // with the report.
                if (reportEntryMap != null) {
                    ExpenseReport.reportEntryMap = reportEntryMap;
                } else {
                    ExpenseReport.reportEntryMap = new HashMap<String, ExpenseReportEntry>();
                }

                // Look up the report header database key.
                long reportHeaderDBKey = db.lookUpReportHeaderID(reportKey, type, detail, userId);
                // Retrieve the entry.
                try {
                    // Check the report header id.
                    Assert.assertTrue(MTAG + "lookup failed for report header id!", (reportHeaderDBKey != -1L));
                    reportEntryInfo = db.loadReportEntry(reportHeaderDBKey, reportKey, reportEntryKey, userId);
                    if (reportEntryInfo != null) {
                        if (reportEntryInfo.getXML() != null) {
                            ExpenseReportEntry expRepEnt = null;
                            if (reportEntryInfo.isDetail()) {
                                if (detail) {
                                    // Persistence contains a detailed version and the requested version is detail, so just
                                    // use the persisted version.
                                    expRepEnt = ExpenseReportEntryDetail.parseReportEntryDetailXml(reportEntryInfo
                                            .getXML());
                                } else {
                                    // Persistence version is detail, but a non-detailed version has been requested. However,
                                    // the detailed parser has to be used in case itemizations exist. So, the strategy here
                                    // is to use the detailed parser, then convert over to a non-detailed version.

                                    // First, set the 'ExpenseReportEntry.reportEntryMap' value to 'null'. This will prevent the
                                    // parser from inserting into the entry map for the report any parsed itemization entries.
                                    ExpenseReport.reportEntryMap = null;
                                    // Second, parse the detailed object.
                                    expRepEnt = ExpenseReportEntryDetail.parseReportEntryDetailXml(reportEntryInfo
                                            .getXML());
                                    // Third, construct an instance of 'ExpenseReportEntry' from the detailed version.
                                    ExpenseReportEntry nonDetailedExpRepEnt = new ExpenseReportEntry(
                                            (ExpenseReportEntryDetail) expRepEnt);
                                    expRepEnt = nonDetailedExpRepEnt;
                                }
                            } else {
                                // If the report entry persisted is non-detail and 'detail' is 'false', then
                                // just use the non-detail version.
                                if (!detail) {
                                    expRepEnt = ExpenseReportEntry.parseReportEntry(reportEntryInfo.getXML());
                                } else {
                                    // A detailed version is requested, but persistence contains a non-detailed version,
                                    // fail and return a null reportEntryInfo object.
                                    reportEntryInfo.clearXML();
                                    reportEntryInfo = null;
                                }
                            }
                            if (expRepEnt != null) {
                                reportEntryInfo.setEntry(expRepEnt);
                            } else {
                                Log.e(Const.LOG_TAG, MTAG + "entry XML could not be parsed!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, MTAG + "entry XML is null!");
                        }
                    }
                } catch (AssertionFailedError afe) {
                    Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                    reportEntryInfo = null;
                }
                // Reset the entry map.
                ExpenseReport.reportEntryMap = null;
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                reportEntryInfo = null;
            }
            return reportEntryInfo;
        }
    }

    /**
     * Will load a report.
     * 
     * @param db
     *            a reference to the database.
     * @param type
     *            the type of report.
     * @param reportKey
     *            the report key.
     * @param userId
     *            user id associated with the report data.
     * @param detail
     *            whether to load a detail report.
     * @return an instance of <code>IExpenseReportInfo</code> containing the report information.
     */
    public static IExpenseReportInfo loadReport(MobileDatabase db, ReportType type, String reportKey, String userId,
            boolean detail) {
        synchronized (ReportDBUtil.class) {

            final String MTAG = CLS_TAG + ".loadReport: ";

            IExpenseReportDBInfo reportInfo = null;

            // Perform an assertion check.
            try {
                Assert.assertNotNull(MTAG + "db is null!", db);
                Assert.assertNotNull(MTAG + "report type is null!", type);
                Assert.assertNotNull(MTAG + "reportKey is null!", reportKey);
                Assert.assertNotNull(MTAG + "user id is null!", userId);

                // First, load and de-serialize the header.
                reportInfo = db.loadReportHeader(type, userId, reportKey, detail);
                if (reportInfo != null) {
                    // Check XML.
                    Assert.assertNotNull(MTAG + "report header XML is null!", reportInfo.getXML());
                    // Second, load and de-serialize the entries.
                    ExpenseReport report = null;
                    if (detail) {
                        report = ExpenseReportDetail.parseReportHeaderDetail(reportInfo.getXML());
                    } else {
                        report = ExpenseReport.parseReportHeaderSummary(reportInfo.getXML());
                    }
                    // Check parsed report.
                    Assert.assertNotNull(MTAG + "header XML could not be parsed!", report);
                    // Set the expenseEntriesMap from the report back on to the static ExpenseReport reference.
                    // This will ensure that an parsing of expense entries below get added onto the map associated
                    // with the report.
                    ExpenseReport.reportEntryMap = report.reportExpenseEntryMap;

                    // Set the report on the info object.
                    reportInfo.setReport(report);
                    // Look up the report header database key.
                    long reportHeaderDBKey = db.lookUpReportHeaderID(reportInfo.getReportKey(),
                            reportInfo.getReportType(), reportInfo.isDetail(), userId);
                    // Retrieve the entries.
                    try {
                        // Check the report header id.
                        Assert.assertTrue(MTAG + "lookup failed for report header id!", (reportHeaderDBKey != -1L));
                        List<IExpenseReportEntryInfo> entries = db.loadReportEntries(reportHeaderDBKey);
                        if (entries != null) {
                            for (IExpenseReportEntryInfo entryInfo : entries) {
                                if (entryInfo.getXML() != null) {
                                    ExpenseReportEntry expRepEnt = null;
                                    if (entryInfo.isDetail()) {
                                        expRepEnt = ExpenseReportEntryDetail.parseReportEntryDetailXml(entryInfo
                                                .getXML());
                                    } else {
                                        expRepEnt = ExpenseReportEntry.parseReportEntry(entryInfo.getXML());
                                    }
                                    if (expRepEnt != null) {
                                        if (report.expenseEntries == null) {
                                            report.expenseEntries = new ArrayList<ExpenseReportEntry>(entries.size());
                                        }
                                        // Add the entry to the report.
                                        report.expenseEntries.add(expRepEnt);
                                    } else {
                                        Log.e(Const.LOG_TAG, MTAG + "entry XML could not be parsed!");
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG, MTAG + "entry XML is null!");
                                }
                            }
                        }
                    } catch (AssertionFailedError afe) {
                        Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                        reportInfo = null;
                    }
                    // Reset the entry map.
                    ExpenseReport.reportEntryMap = null;
                }
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                reportInfo = null;
            }
            return reportInfo;
        }
    }

    /**
     * Will load the list of detail report infos <b>only</b> which will not contain built report domain objects.
     * 
     * @param db
     *            a reference to the database.
     * @param type
     *            the type of report.
     * @param userId
     *            the user id associated with the report data.
     * @return a list of <code>IExpenseReportInfo</code> objects containing the report information.
     */
    public static List<IExpenseReportInfo> loadReportInfos(MobileDatabase db, ReportType type, String userId,
            boolean isDetail) {
        synchronized (ReportDBUtil.class) {

            final String MTAG = CLS_TAG + ".loadSummaryReports: ";
            List<IExpenseReportInfo> reportInfos = null;

            // Perform an assertion check.
            try {
                Assert.assertNotNull(MTAG + "db is null!", db);
                Assert.assertNotNull(MTAG + "report type is null!", type);
                Assert.assertNotNull(MTAG + "user id is null!", userId);

                // Load the report info objects.
                List<IExpenseReportDBInfo> reportDBInfos = db.loadReportHeaders(type, userId, isDetail, false);
                if (reportDBInfos != null) {
                    reportInfos = new ArrayList<IExpenseReportInfo>(reportDBInfos.size());
                    reportInfos.addAll(reportDBInfos);
                }
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, afe.getMessage(), afe);
            }
            return reportInfos;
        }
    }

    /**
     * Will update a report entry.
     * 
     * @param db
     *            a reference to the database.
     * @param report
     *            the expense report.
     * @param type
     *            the report type.
     * @param entry
     *            the entry.
     * @param userId
     *            the user id
     * @param updateTime
     *            the update time.
     * @param isDetail
     *            whether the report entry is part of a detailed report.
     * @param headerDBKey
     *            the report header db key.
     */
    public static IExpenseReportInfo updateReportEntry(MobileDatabase db, ExpenseReport report, ReportType type,
            ExpenseReportEntry entry, String userId, Calendar updateTime, boolean isDetail) {
        synchronized (ReportDBUtil.class) {

            final String MTAG = CLS_TAG + ".updateReportEntry: ";
            IExpenseReportDBInfo reportInfo = null;
            // Perform an assertion check.
            try {
                Assert.assertNotNull(MTAG + "db is null!", db);
                Assert.assertNotNull(MTAG + "report type is null!", type);
                Assert.assertNotNull(MTAG + "user id is null!", userId);
                Assert.assertNotNull(MTAG + "entry is null!", entry);
                Assert.assertNotNull(MTAG + "updateTime is null!", updateTime);

                reportInfo = db.updateReportHeader(entry.rptKey, type, userId, isDetail, updateTime);
                // Check the report info.
                Assert.assertNotNull(MTAG + "unable to update report header!", reportInfo);
                reportInfo.setReport(report);

                String entryXmlRep = null;
                if (entry instanceof ExpenseReportEntryDetail) {
                    ExpenseReportEntryDetail entryDetail = (ExpenseReportEntryDetail) entry;
                    entryXmlRep = entryDetail.xmlRep;
                    if (entryXmlRep != null) {
                        entryDetail.xmlRep = null;
                    }
                }
                if (entryXmlRep == null) {
                    // Serialize the report entry.
                    StringBuilder strBldr = new StringBuilder();
                    ExpenseReportEntryDetail.ExpenseReportEntryDetailSAXHandler.serializeAllToXML(strBldr, entry);
                    // Check whether anything was written to the builder.
                    Assert.assertTrue(MTAG + "unable to serialize report entry!", (strBldr.length() > 0));
                    entryXmlRep = strBldr.toString();
                }
                // Get the report header database key.
                long reportHeaderDBKey = db.lookUpReportHeaderID(reportInfo.getReportKey(), reportInfo.getReportType(),
                        reportInfo.isDetail(), userId);
                try {
                    // Check the report header id.
                    Assert.assertTrue(MTAG + "lookup failed for report header id!", (reportHeaderDBKey != -1L));
                    // Punt any non-detailed version of the report entry.
                    db.deleteReportEntry(reportHeaderDBKey, userId, report.reportKey, entry.reportEntryKey, false);
                    // Update the report entry.
                    db.updateReportEntry(reportHeaderDBKey, userId, entry.rptKey, entry.reportEntryKey,
                            entry.isDetail(), entryXmlRep);
                } catch (AssertionFailedError afe) {
                    Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                }
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                reportInfo = null;
            }
            return reportInfo;
        }
    }

}
