/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a model of an expense report.
 * 
 * @author AndrewK
 */
public class ExpenseReport implements Serializable {

    private static final long serialVersionUID = -2919012247717938016L;

    private static final String CLS_TAG = ExpenseReport.class.getSimpleName();

    public String apsKey;

    public String apvStatusName;

    public String caReturnsAmount;

    public String crnCode;

    public Boolean everSentBack;

    public Boolean hasException;

    public String lastComment;

    public String purpose;

    public Boolean receiptImageAvailable;

    public String reportDate;

    public Calendar reportDateCalendar;

    public String reportName;

    public String reportKey;

    public Double totalPostedAmount;

    public Double totalApprovedAmount;

    public Double totalApprovedAmountPending;

    public Double totalClaimedAmount;

    public Double totalDueCompany;

    public Double totalDueEmployee;

    public Double totalPersonalAmount;

    public Double totalRejectedAmount;

    public Double dueCompanyCard;

    public String employeeName;

    public String pdfUrl;

    public String realPdfUrl;

    public String receiptUrl;

    public String processInstanceKey;

    public String stepKey;

    public String currentSequence;

    public String payKey;

    public String payStatusName;

    public String polKey;

    public String submitDate;

    public Boolean readyToSubmit;

    public String tacKey;

    /**
     * Contains the severity level of any expense entries (and their itemizations) contained within this report.
     */
    public String severityLevel;

    /**
     * Contains the approver employee name.
     */
    public String aprvEmpName;

    /**
     * Contains a map from expense report entry key strings to instances of <code>ExpenseReportEntry</code>.
     */
    public Map<String, ExpenseReportEntry> reportExpenseEntryMap;

    /**
     * Contains a map from expense report entry key strings to instances of <code>ExpenseReportEntry</code>.
     */
    public static Map<String, ExpenseReportEntry> reportEntryMap;

    /**
     * Contains a list of <code>ExpenseReportEntry</code> objects.
     */
    public ArrayList<ExpenseReportEntry> expenseEntries;

    /**
     * Contains a list of workflow actions based on the current report status.
     */
    public List<WorkflowAction> workflowActions;

    /**
     * Finds an instance of <code>ExpenseReportEntry</code> based on a key look-up.
     * 
     * @param expEntRepKey
     *            the expense report entry key.
     * 
     * @return an instance of <code>ExpenseReportEntry</code> if found; <code>null</code> otherwise.
     */
    public ExpenseReportEntry findEntryByReportKey(String expEntRepKey) {
        ExpenseReportEntry expRepEnt = null;

        if (reportExpenseEntryMap != null) {
            expRepEnt = reportExpenseEntryMap.get(expEntRepKey);
        }

        return expRepEnt;
    }

    /**
     * Will replace an existing report entry with <code>expRepEnt</code> based on matching the report entry key. If not found, the
     * entry will be added. If the entry has itemizations, they will be iterated and replaced/added as well.
     * 
     * @param expRepEntDet
     *            the report entry.
     */
    public void replaceOrAddReportEntry(ExpenseReportEntryDetail expRepEntDet) {
        replaceOrAddReportEntry((ExpenseReportEntry) expRepEntDet);

        if (expRepEntDet.isItemized()) {
            ArrayList<ExpenseReportEntry> children = expRepEntDet.getItemizations();
            int size = children.size();
            for (int i = 0; i < size; i++) {
                replaceOrAddReportEntry(children.get(i));
            }
        }
    }

    /**
     * Will replace an existing report entry with <code>expRepEnt</code> based on matching the report entry key. If not found, the
     * entry will be added.
     * 
     * @param expRepEnt
     *            the report entry.
     */
    public void replaceOrAddReportEntry(ExpenseReportEntry expRepEnt) {

        boolean updatedOrAdded = false;

        if (expRepEnt != null) {

            if (expenseEntries == null) {
                // This must be the first entry being added (like MILEG to a new report)
                // Create our list
                expenseEntries = new ArrayList<ExpenseReportEntry>();
            }

            boolean isEntryItemization = (expRepEnt.parentReportEntryKey != null && expRepEnt.parentReportEntryKey
                    .length() > 0);

            // Determine the entry key we are searching for (parent or regular entry key)
            String entryOrParentKey = (isEntryItemization) ? expRepEnt.parentReportEntryKey : expRepEnt.reportEntryKey;

            ListIterator<ExpenseReportEntry> listIter = expenseEntries.listIterator();
            while (listIter.hasNext()) {

                ExpenseReportEntry expEnt = listIter.next();
                if (expEnt.reportEntryKey != null && entryOrParentKey != null
                        && expEnt.reportEntryKey.contentEquals(entryOrParentKey)) {

                    if (!isEntryItemization) {

                        // Update the expense entry list item position.
                        listIter.set(expRepEnt);
                        updatedOrAdded = true;

                    } else {

                        // Iterate over any itemizations of 'expEnt'.
                        if (expEnt instanceof ExpenseReportEntryDetail) {

                            ExpenseReportEntryDetail expRepEntDet = (ExpenseReportEntryDetail) expEnt;

                            if (expRepEntDet.getItemizations() != null) {
                                ListIterator<ExpenseReportEntry> itemIter = expRepEntDet.getItemizations()
                                        .listIterator();

                                while (itemIter.hasNext()) {
                                    ExpenseReportEntry expRepEntItem = itemIter.next();
                                    if (expRepEntItem.reportEntryKey != null && expRepEnt.reportEntryKey != null
                                            && expRepEntItem.reportEntryKey.contentEquals(expRepEnt.reportEntryKey)) {
                                        // Update the expense itemization list item position.
                                        itemIter.set(expRepEnt);

                                        updatedOrAdded = true;

                                        // Break out of the loop over itemizations.
                                        break;
                                    }
                                }

                                if (!updatedOrAdded) {
                                    // Not found, need to add it.
                                    expRepEntDet.getItemizations().add(expRepEnt);
                                    updatedOrAdded = true;
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG
                                            + ".replaceReportEntry: 'expEnt' is not a detailed report entry but has a parent report entry key!");
                        }
                    }

                    // Break out of the loop over entries.
                    break;
                }

            }

            if (!updatedOrAdded) {
                // Add it
                expenseEntries.add(expRepEnt);
            }

            // Update the entry map.
            if (reportExpenseEntryMap != null) {
                reportExpenseEntryMap.put(expRepEnt.reportEntryKey, expRepEnt);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".replaceReportEntry: reportExpenseEntryMap is null!");
            }

        }

    }

    /**
     * Whether or not this contains detailed report information.
     * 
     * @return whether or not this contains detailed report information.
     */
    public boolean isDetail() {
        return false;
    }

    /**
     * Gets whether this report has been re-submitted.
     * 
     * @return whether this report has been re-submitted.
     */
    public boolean everSentBack() {
        return ((everSentBack != null) ? everSentBack : Boolean.FALSE);
    }

    /**
     * Gets whether this report is ready to submit.
     * 
     * @return whether this report is ready to submit.
     */
    public boolean isReadyToSubmit() {
        return ((readyToSubmit != null) ? readyToSubmit : Boolean.FALSE);
    }

    /**
     * Gets whether or not this report has been submitted.
     * 
     * @return whether this report has been submitted.
     */
    public boolean isSubmitted() {
        return (!isUnsubmitted() && !isSentBack());
    }

    /**
     * Gets whether this report is unsubmitted.
     * 
     * @return whether this report is unsubmitted.
     */
    public boolean isUnsubmitted() {
        return apsKey.equalsIgnoreCase("A_NOTF");
    }

    /**
     * Gets whether this report is sent back.
     * 
     * @return whether this report has been sent back.
     */
    public boolean isSentBack() {
        return apsKey.equalsIgnoreCase("A_RESU");
    }

    /**
     * Gets whether this report has exceptions.
     * 
     * @return whether this report has exceptions.
     */
    public boolean hasException() {
        return ((hasException != null) ? hasException : Boolean.FALSE);
    }

    /**
     * Gets whether this report has receipt images available.
     * 
     * @return whether this report has receipt images available.
     */
    public boolean isReceiptImageAvailable() {
        return ((receiptImageAvailable != null) ? receiptImageAvailable : Boolean.FALSE);
    }

    // MOB-12750
    // This checks against a hard coded value, which is consistent with the rest of this class.
    /**
     * Checks if this report is of "Hold for Receipt Image" status.
     * 
     * @return whether this Report is of "Hold for Receipt Image" status.
     */
    public boolean isHoldForReceiptImageStatus() {
        return apsKey.equalsIgnoreCase("A_RHLD");
    }

    /**
     * Gets the report creation date as a string formatted to <code>FormatUtil.SHORT_DAY_MONTH_YEAR_DISPLAY</code> format.
     * 
     * @return report creation date formatted to <code>FormatUtil.SHORT_DAY_MONTH_YEAR_DISPLAY</code>
     */
    public String getFormattedReportDate() {
        return FormatUtil.SHORT_MONTH_DAY_YEAR_DISPLAY.format(reportDateCalendar.getTime());
    }

    /**
     * Gets the list of <code>ExpenseReportEntry</code> objects associated with this expense report.
     * 
     * @return the list of <code>ExpenseReportEntry</code> objects associated with this expense report.
     */
    public ArrayList<ExpenseReportEntry> getExpenseEntries() {
        return expenseEntries;
    }

    /**
     * Will parse the XML description of a report header and return an <code>ExpenseReport</code> object.
     * 
     * @param reportHeaderXml
     *            the report header xml.
     * @return an instance of <code>ExpenseReport</code> containing only report header information.
     */
    public static ExpenseReport parseReportHeaderSummary(String reportHeaderXml) {
        return parseReportHeaderSummary(new ByteArrayInputStream(reportHeaderXml.getBytes()));
    }

    /**
     * Will parse the XML description of a report header and return an <code>ExpenseReport</code> object.
     * 
     * @param stream
     *            a reference to an input stream providing report header XML content.
     * @return an instance of <code>ExpenseReport</code> containing only report header information.
     */
    public static ExpenseReport parseReportHeaderSummary(InputStream stream) {
        ExpenseReport header = null;

        ArrayList<ExpenseReport> reports = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ReportListSAXHandler handler = new ReportListSAXHandler();
            reportEntryMap = new HashMap<String, ExpenseReportEntry>();
            parser.parse(stream, handler);
            reports = handler.getReports();
            if (reports.size() == 1) {
                header = reports.get(0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return header;
    }

    /**
     * Will parse the XML description of a report header and return an <code>ExpenseReport</code> object.
     * 
     * @param reader
     *            a reference to a reader providing report header XML content.
     * @return an instance of <code>ExpenseReport</code> containing only report header information.
     */
    public static ExpenseReport parseReportHeaderSummary(Reader reader) {
        ExpenseReport header = null;

        ArrayList<ExpenseReport> reports = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ReportListSAXHandler handler = new ReportListSAXHandler();
            reportEntryMap = new HashMap<String, ExpenseReportEntry>();
            parser.parse(new InputSource(reader), handler);
            reports = handler.getReports();
            if (reports.size() == 1) {
                header = reports.get(0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return header;
    }

    /**
     * Will parse an XML description of a list of reports (either approval or active)
     * 
     * @param responseXml
     *            string containing the XML description.
     * 
     * @return returns a list of <code>ExpenseReport</code> objects.
     */
    public static ArrayList<ExpenseReport> parseReportsXml(String responseXml) {
        return parseReportsXml(new ByteArrayInputStream(responseXml.getBytes()));
    }

    /**
     * Will parse an XML description of a list of reports (either approval or active)
     * 
     * @param stream
     *            contains the input stream providing report list XML content.
     * 
     * @return returns a list of <code>ExpenseReport</code> objects.
     */
    public static ArrayList<ExpenseReport> parseReportsXml(InputStream stream) {

        ArrayList<ExpenseReport> reports = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ReportListSAXHandler handler = new ReportListSAXHandler();
            reportEntryMap = new HashMap<String, ExpenseReportEntry>();
            parser.parse(stream, handler);
            reports = handler.getReports();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return reports;
    }

    /**
     * Will parse an XML description of a list of reports (either approval or active)
     * 
     * @param reader
     *            contains the reader providing report list XML content.
     * 
     * @return returns a list of <code>ExpenseReport</code> objects.
     */
    public static ArrayList<ExpenseReport> parseReportsXml(Reader reader) {

        ArrayList<ExpenseReport> reports = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ReportListSAXHandler handler = new ReportListSAXHandler();
            reportEntryMap = new HashMap<String, ExpenseReportEntry>();
            parser.parse(new InputSource(reader), handler);
            reports = handler.getReports();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return reports;
    }

    /**
     * Helper class to handle parsing of expense report XML.
     */
    public static class ReportListSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = ExpenseReport.CLS_TAG + "." + ReportListSAXHandler.class.getSimpleName();

        // These are the outer elements for an approval list
        protected static final String REPORT_TO_APPROVE_LIST = "ReportToApproveList";
        protected static final String REPORT_TO_APPROVE = "ReportToApprove";

        // These are the outer elements for an active list
        protected static final String ARRAY_OF_REPORT = "ArrayOfReport";
        protected static final String REPORT = "Report";

        protected static final String APS_KEY = "ApsKey";
        protected static final String APV_STATUS_NAME = "ApvStatusName";
        protected static final String CA_RETURNS_AMOUNT = "CaReturnsAmount";
        protected static final String CRN_CODE = "CrnCode";
        protected static final String EVER_SENT_BACK = "EverSentBack";
        protected static final String HAS_EXCEPTION = "HasException";
        protected static final String LAST_COMMENT = "LastComment";
        protected static final String PURPOSE = "Purpose";
        protected static final String RECEIPT_IMAGE_AVAILABLE = "ReceiptImageAvailable";
        protected static final String REPORT_DATE = "ReportDate";
        protected static final String REPORT_NAME = "ReportName";
        protected static final String REPORT_KEY = "RptKey";
        protected static final String TOTAL_POSTED_AMOUNT = "TotalPostedAmount";
        protected static final String TOTAL_APPROVED_AMOUNT = "TotalApprovedAmount";
        protected static final String TOTAL_APPROVED_AMOUNT_PENDING = "TotalApprovedAmountPending";
        protected static final String TOTAL_CLAIMED_AMOUNT = "TotalClaimedAmount";
        protected static final String TOTAL_DUE_COMPANY = "TotalDueCompany";
        protected static final String TOTAL_DUE_EMPLOYEE = "TotalDueEmployee";
        protected static final String TOTAL_PERSONAL_AMOUNT = "TotalPersonalAmount";
        protected static final String TOTAL_REJECTED_AMOUNT = "TotalRejectedAmount";
        protected static final String DUE_COMPANY_CARD = "DueCompanyCard";
        protected static final String EMPLOYEE_NAME = "EmployeeName";
        protected static final String PDF_URL = "PdfUrl";
        protected static final String REAL_PDF_URL = "RealPdfUrl";
        protected static final String RECEIPT_URL = "ReceiptUrl";
        protected static final String PROCESS_INSTANCE_KEY = "ProcessInstanceKey";
        protected static final String STEP_KEY = "StepKey";
        protected static final String CURRENT_SEQUENCE = "CurrentSequence";
        protected static final String SEVERITY_LEVEL = "SeverityLevel";
        protected static final String PAY_KEY = "PayKey";
        protected static final String PAY_STATUS_NAME = "PayStatusName";
        protected static final String POL_KEY = "PolKey";
        protected static final String SUBMIT_DATE = "SubmitDate";
        protected static final String ENTRIES = "Entries";
        protected static final String APRV_EMP_NAME = "AprvEmpName";
        protected static final String WORKFLOW_ACTIONS = "WorkflowActions";
        protected static final String WORKFLOW_ACTION = "WorkflowAction";
        protected static final String ACTION_TEXT = "ActionText";
        protected static final String STAT_KEY = "StatKey";
        protected static final String READY_TO_SUBMIT = "PrepForSubmitEmpKey";
        protected static final String TAC_KEY = "TacKey";

        // Fields to help parsing
        protected StringBuilder chars = new StringBuilder();

        // Holders for our parsed data
        protected ArrayList<ExpenseReport> reports = new ArrayList<ExpenseReport>();
        protected ExpenseReport report;
        protected WorkflowAction workflowAction;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Contains whether or not report entries are currently being parsed.
         */
        private boolean parsingReportEntries;

        /**
         * Contains whether or not workflow actions are currently being parsed.
         */
        private boolean parsingWorkflowActions;

        /**
         * Contains a reference to a SAX handler for parsing expense report entries.
         */
        private ExpenseReportEntry.ExpenseReportEntrySAXHandler reportEntryHandler;

        /**
         * Retrieve our list of parsed reports
         * 
         * @return A List of {@link ExpenseReport} objects parsed from the XML
         */
        public ArrayList<ExpenseReport> getReports() {
            return reports;
        }

        /**
         * Will create an instance of <code>ExpenseReport</code> to be filled in by this parser.
         * 
         * @return an instance of <code>ExpenseReport</code> to be filled in by this parser.
         */
        protected ExpenseReport createReport() {
            return new ExpenseReport();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if (!parsingReportEntries) {
                chars.append(ch, start, length);
            } else {
                reportEntryHandler.characters(ch, start, length);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            elementHandled = false;

            super.startElement(uri, localName, qName, attributes);

            // Check for parsing report entry elements.
            if (!parsingReportEntries) {

                if (localName.equalsIgnoreCase(REPORT_TO_APPROVE_LIST) || localName.equalsIgnoreCase(ARRAY_OF_REPORT)) {

                    // Initialize the list.
                    if (reports == null) {
                        reports = new ArrayList<ExpenseReport>();
                    }
                    elementHandled = true;

                } else if (localName.equalsIgnoreCase(REPORT_TO_APPROVE) || localName.equalsIgnoreCase(REPORT)) {

                    report = createReport();
                    // Set up the new expense report entry key.
                    reportEntryMap = new HashMap<String, ExpenseReportEntry>();
                    elementHandled = true;

                } else if (localName.equalsIgnoreCase(ENTRIES)) {
                    parsingReportEntries = true;
                    reportEntryHandler = createEntriesHandler();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(WORKFLOW_ACTIONS)) {
                    parsingWorkflowActions = true;
                    report.workflowActions = new ArrayList<WorkflowAction>();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(WORKFLOW_ACTION)) {
                    workflowAction = new WorkflowAction();
                    elementHandled = true;
                }
            } else {
                // Pass to the delegate SAX handler.
                reportEntryHandler.startElement(uri, localName, qName, attributes);
                elementHandled = true;
            }
        }

        /**
         * Will construct a new instance of <code>ExpenseReportEntry.ExpenseReportEntrySAXHandler</code> to parse report entry
         * information.
         * 
         * @return a new instance of <code>ExpenseReportEntry.ExpenseReportEntrySAXHandler</code> to parse report entry
         *         information.
         */
        protected ExpenseReportEntry.ExpenseReportEntrySAXHandler createEntriesHandler() {
            return new ExpenseReportEntry.ExpenseReportEntrySAXHandler();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            elementHandled = false;

            super.endElement(uri, localName, qName);

            if (report != null) {

                if (!parsingReportEntries) {

                    final String cleanChars = chars.toString().trim();

                    if (localName.equalsIgnoreCase(APS_KEY)) {
                        report.apsKey = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(PAY_KEY)) {
                        report.payKey = cleanChars;
                        elementHandled = true;
                    } else if (localName.equals(TAC_KEY)) {
                        report.tacKey = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(PAY_STATUS_NAME)) {
                        report.payStatusName = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(POL_KEY)) {
                        report.polKey = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(SUBMIT_DATE)) {
                        report.submitDate = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(APV_STATUS_NAME)) {
                        report.apvStatusName = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(CA_RETURNS_AMOUNT)) {
                        report.caReturnsAmount = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(CRN_CODE)) {
                        report.crnCode = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(EVER_SENT_BACK)) {
                        report.everSentBack = Parse.safeParseBoolean(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(HAS_EXCEPTION)) {
                        report.hasException = Parse.safeParseBoolean(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(LAST_COMMENT)) {
                        report.lastComment = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(PURPOSE)) {
                        report.purpose = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(RECEIPT_IMAGE_AVAILABLE)) {
                        report.receiptImageAvailable = Parse.safeParseBoolean(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(REPORT_DATE)) {
                        report.reportDate = cleanChars;
                        report.reportDateCalendar = Parse.parseXMLTimestamp(report.reportDate);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(REPORT_NAME)) {
                        report.reportName = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(REPORT_KEY)) {
                        report.reportKey = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(TOTAL_POSTED_AMOUNT)) {
                        report.totalPostedAmount = Parse.safeParseDouble(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(TOTAL_APPROVED_AMOUNT)) {
                        report.totalApprovedAmount = Parse.safeParseDouble(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(TOTAL_APPROVED_AMOUNT_PENDING)) {
                        report.totalApprovedAmountPending = Parse.safeParseDouble(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(TOTAL_CLAIMED_AMOUNT)) {
                        report.totalClaimedAmount = Parse.safeParseDouble(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(TOTAL_DUE_COMPANY)) {
                        report.totalDueCompany = Parse.safeParseDouble(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(TOTAL_DUE_EMPLOYEE)) {
                        report.totalDueEmployee = Parse.safeParseDouble(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(TOTAL_PERSONAL_AMOUNT)) {
                        report.totalPersonalAmount = Parse.safeParseDouble(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(TOTAL_REJECTED_AMOUNT)) {
                        report.totalRejectedAmount = Parse.safeParseDouble(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(DUE_COMPANY_CARD)) {
                        report.dueCompanyCard = Parse.safeParseDouble(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(EMPLOYEE_NAME)) {
                        report.employeeName = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(PDF_URL)) {
                        report.pdfUrl = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(READY_TO_SUBMIT)) {
                        report.readyToSubmit = Parse.safeParseBoolean(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(REAL_PDF_URL)) {
                        report.realPdfUrl = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(PROCESS_INSTANCE_KEY)) {
                        report.processInstanceKey = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(RECEIPT_URL)) {
                        report.receiptUrl = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(STEP_KEY)) {
                        report.stepKey = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(CURRENT_SEQUENCE)) {
                        report.currentSequence = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(SEVERITY_LEVEL)) {
                        report.severityLevel = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(APRV_EMP_NAME)) {
                        report.aprvEmpName = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(REPORT_TO_APPROVE) || localName.equalsIgnoreCase(REPORT)) {
                        reports.add(report);
                        // Assign the map to the report.
                        report.reportExpenseEntryMap = ExpenseReport.reportEntryMap;
                        ExpenseReport.reportEntryMap = null;
                        report = null;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(REPORT_TO_APPROVE_LIST)
                            || localName.equalsIgnoreCase(ARRAY_OF_REPORT)) {
                        elementHandled = true;
                    } else if (parsingWorkflowActions) {
                        if (localName.equalsIgnoreCase(WORKFLOW_ACTION)) {
                            if (report.workflowActions != null) {
                                report.workflowActions.add(workflowAction);
                                workflowAction = null;
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: report.workflowActions is null!");
                            }
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(WORKFLOW_ACTIONS)) {
                            parsingWorkflowActions = false;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(ACTION_TEXT)) {
                            if (workflowAction != null) {
                                workflowAction.actionText = cleanChars;
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: workflowAction is null!");
                            }
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(STAT_KEY)) {
                            if (workflowAction != null) {
                                workflowAction.statKey = cleanChars;
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: workflowAction is null!");
                            }
                            elementHandled = true;
                        }
                    } else if (!elementHandled && this.getClass().equals(ReportListSAXHandler.class)) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '" + localName + "'.");
                        // Clear out any collected characters if this class is the last stop.
                        chars.setLength(0);
                    }
                } else {
                    if (reportEntryHandler != null) {
                        if (localName.equalsIgnoreCase(ENTRIES)) {
                            report.expenseEntries = reportEntryHandler.getReportEntries();
                            parsingReportEntries = false;
                            reportEntryHandler = null;
                        } else {
                            // Pass to delegate.
                            reportEntryHandler.endElement(uri, localName, qName);
                        }
                        elementHandled = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report entry handler!");
                        chars.setLength(0);
                    }
                }
            } else {
                if (localName.equalsIgnoreCase(REPORT_TO_APPROVE_LIST) || localName.equalsIgnoreCase(ARRAY_OF_REPORT)) {
                    // No-op.
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report - localName '" + localName + "' " + chars
                            + "'" + chars.toString() + "'.");
                }
                chars.setLength(0);
            }
            // Only clear out the string if this parser has handled it.
            if (elementHandled) {
                chars.setLength(0);
            }
        }
    }
}
