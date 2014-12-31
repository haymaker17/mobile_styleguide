/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportDisbursement.DisbursementType;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * Models expense report detail information.
 * 
 * @author AndrewK
 */
public class ExpenseReportDetail extends ExpenseReport implements Serializable {

    private static final long serialVersionUID = -1635837246131094970L;

    private static final String CLS_TAG = ExpenseReportDetail.class.getSimpleName();

    /**
     * Contains a list of <code>ExpenseReportComment</code> objects representing report-level comments.
     */
    protected ArrayList<ExpenseReportComment> comments;

    /**
     * Contains a list of <code>ExpenseReportExceptions</code> objects representing report-level exceptions.
     */
    protected ArrayList<ExpenseReportException> exceptions;

    /**
     * Contains a list of <code>ExpenseReportFormField</code> objects representing report-level custom fields.
     */
    protected List<ExpenseReportFormField> formFields;

    protected ArrayList<ExpenseReportDisbursement> companyDisbursements;
    protected ArrayList<ExpenseReportDisbursement> employeeDisbursements;

    /**
     * Contains whether a receipt image is required.
     */
    protected Boolean imageRequired;

    /**
     * Contains the total due to the company card.
     */
    public Double totalDueCompanyCard;

    /**
     * Contains the total owed by the employee.
     */
    public Double totalOwedByEmployee;

    /**
     * Contains the total paid by the company.
     */
    public Double totalPaidByCompany;

    /**
     * Contains the form editing key.
     */
    public String formKey;

    /**
     * Will update the header information from <code>expRepDet</code>.
     * 
     * @param expRepDet
     *            the expense report detail object providing new header values.
     */
    public void updateHeader(ExpenseReportDetail expRepDet) {
        apsKey = expRepDet.apsKey;
        apvStatusName = expRepDet.apvStatusName;
        caReturnsAmount = expRepDet.caReturnsAmount;
        crnCode = expRepDet.crnCode;
        everSentBack = expRepDet.everSentBack;
        hasException = expRepDet.hasException;
        lastComment = expRepDet.lastComment;
        purpose = expRepDet.purpose;
        receiptImageAvailable = expRepDet.receiptImageAvailable;
        reportDate = expRepDet.reportDate;
        reportDateCalendar = expRepDet.reportDateCalendar;
        reportName = expRepDet.reportName;
        reportKey = expRepDet.reportKey;
        totalPostedAmount = expRepDet.totalPostedAmount;
        totalApprovedAmount = expRepDet.totalApprovedAmount;
        totalApprovedAmountPending = expRepDet.totalApprovedAmountPending;
        totalClaimedAmount = expRepDet.totalClaimedAmount;
        totalDueCompany = expRepDet.totalDueCompany;
        totalDueEmployee = expRepDet.totalDueEmployee;
        totalPersonalAmount = expRepDet.totalPersonalAmount;
        totalRejectedAmount = expRepDet.totalRejectedAmount;
        dueCompanyCard = expRepDet.dueCompanyCard;
        employeeName = expRepDet.employeeName;
        pdfUrl = expRepDet.pdfUrl;
        receiptUrl = expRepDet.receiptUrl;
        processInstanceKey = expRepDet.processInstanceKey;
        stepKey = expRepDet.stepKey;
        currentSequence = expRepDet.currentSequence;
        payKey = expRepDet.payKey;
        payStatusName = expRepDet.payStatusName;
        polKey = expRepDet.polKey;
        submitDate = expRepDet.submitDate;
        severityLevel = expRepDet.severityLevel;
        comments = expRepDet.comments;
        exceptions = expRepDet.exceptions;
        formFields = expRepDet.formFields;
        companyDisbursements = expRepDet.companyDisbursements;
        employeeDisbursements = expRepDet.employeeDisbursements;
        imageRequired = expRepDet.imageRequired;
        totalDueCompanyCard = expRepDet.totalDueCompanyCard;
        totalOwedByEmployee = expRepDet.totalOwedByEmployee;
        totalPaidByCompany = expRepDet.totalPaidByCompany;
        formKey = expRepDet.formKey;
        tacKey = expRepDet.tacKey;
    }

    /**
     * Gets whether a receipt image is required for this expense report.
     * 
     * @return whether a receipt image is required for this expense report.
     */
    public boolean isImageRequired() {
        return ((imageRequired != null) ? imageRequired : Boolean.FALSE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.ExpenseReport#isDetail()
     */
    @Override
    public boolean isDetail() {
        return true;
    }

    /**
     * Gets the employee name.
     * 
     * @return the employee name.
     */
    public String getEmployeeName() {
        String retVal = employeeName;
        if (retVal == null || retVal.length() == 0) {
            // Try to pull it from a form field data.
            employeeName = getFormFieldData(IExpenseReportFormField.EMPLOYEE_NAME_FIELD_ID);
        }
        return employeeName;
    }

    /**
     * Gets the CTE assigned report Id.
     * 
     * @return the CTE assigned report Id.
     */
    public String getCTEReportId() {
        return getFormFieldData(IExpenseReportFormField.CTE_ASSIGNED_REPORT_FIELD_ID);
    }

    /**
     * Gets the report expense policy.
     * 
     * @return the report expense policy.
     */
    public String getExpensePolicy() {
        return getFormFieldData(IExpenseReportFormField.EXPENSE_POLICY_FIELD_ID);
    }

    /**
     * Gets the list of report-level comments.
     * 
     * @return the list of report-level comments.
     */
    public ArrayList<ExpenseReportComment> getComments() {
        return comments;
    }

    /**
     * Gets the list of report-level exceptions.
     * 
     * @return the list of report-level exceptions.
     */
    public ArrayList<ExpenseReportException> getExceptions() {
        return exceptions;
    }

    /**
     * Gets the list of report-level form fields.
     * 
     * @return the list of report-level form fields.
     */
    public List<ExpenseReportFormField> getFormFields() {
        return formFields;
    }

    public ArrayList<ExpenseReportDisbursement> getCompanyDisbursements() {
        return companyDisbursements;
    }

    public ArrayList<ExpenseReportDisbursement> getEmployeeDisbursements() {
        return employeeDisbursements;
    }

    /**
     * Will examine the list of report-level form fields and return the form field matching on <code>id</code>.
     * 
     * @param id
     *            the field id of the form field.
     * @return an instance of <code>ExpenseReportFormField</code> matching on <code>id</code>; otherwise <code>null</code> is
     *         returned.
     */
    public ExpenseReportFormField getFormField(String id) {
        ExpenseReportFormField retVal = null;
        if (formFields != null && id != null) {
            for (ExpenseReportFormField frmFld : formFields) {
                if (frmFld.getId() != null && frmFld.getId().equalsIgnoreCase(id)) {
                    retVal = frmFld;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Will parse a report header detail object.
     * 
     * @param headerXML
     *            the report header detail XML.
     * @return an instance of <code>ExpenseReportDetail</code>.
     */
    public static ExpenseReportDetail parseReportHeaderDetail(String headerXML) {
        ExpenseReportDetail report = null;
        report = parseReportDetailXml(headerXML, false);
        return report;
    }

    /**
     * Will parse an XML description of report detail information.
     * 
     * @param responseXml
     *            string containing the XML description.
     * 
     * @return returns an instance of <code>ExpenseReportDetail</code>
     */
    public static ExpenseReportDetail parseReportDetailXml(String responseXml) {
        return parseReportDetailXml(responseXml, false);
    }

    /**
     * Will parse an XML description of report detail information.
     * 
     * @param responseXml
     *            string containing the XML description.
     * @param detailSummary
     *            whether this is a detail summary report, i.e., one that has a detail report header but summary entries.
     * 
     * @return returns an instance of <code>ExpenseReportDetail</code>
     */
    public static ExpenseReportDetail parseReportDetailXml(String responseXml, boolean detailSummary) {
        return parseReportDetailXml(new ByteArrayInputStream(responseXml.getBytes()), detailSummary);
    }

    /**
     * Will parse an XML description of report detail information.
     * 
     * @param reader
     *            reference to an input stream reader providing the XML.
     * @param detailSummary
     *            whether this is a detail summary report, i.e., one that has a detail report header but summary entries.
     * 
     * @return returns an instance of <code>ExpenseReportDetail</code>
     */
    public static ExpenseReportDetail parseReportDetailXml(InputStream stream, boolean detailSummary) {
        ExpenseReportDetail reportDetail = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ReportDetailSAXHandler handler = new ReportDetailSAXHandler();
            handler.parsingDetailSummary = detailSummary;
            parser.parse(stream, handler);
            reportDetail = handler.getReport();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reportDetail;
    }

    /**
     * Will parse an XML description of report detail information.
     * 
     * @param reader
     *            reference to an input stream reader providing the XML.
     * @param detailSummary
     *            whether this is a detail summary report, i.e., one that has a detail report header but summary entries.
     * 
     * @return returns an instance of <code>ExpenseReportDetail</code>
     */
    public static ExpenseReportDetail parseReportDetailXml(Reader reader, boolean detailSummary) {
        ExpenseReportDetail reportDetail = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ReportDetailSAXHandler handler = new ReportDetailSAXHandler();
            handler.parsingDetailSummary = detailSummary;
            parser.parse(new InputSource(reader), handler);
            reportDetail = handler.getReport();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reportDetail;
    }

    /**
     * Will construct a new instance of <code>ExpenseReportEntry.ExpenseReportEntrySAXHandler</code> to parse report entry
     * information.
     * 
     * @return a new instance of <code>ExpenseReportEntry.ExpenseReportEntrySAXHandler</code> to parse report entry information.
     */
    protected ExpenseReportEntry.ExpenseReportEntrySAXHandler createEntriesHandler() {
        return new ExpenseReportEntryDetail.ExpenseReportEntryDetailSAXHandler();
    }

    /**
     * Gets a form field value based on <code>fieldId</code>.
     * 
     * @param fieldId
     *            the form field ID.
     * 
     * @return the value associated with <code>fieldId</code>
     */
    private String getFormFieldData(String fieldId) {
        String fldVal = null;
        if (formFields != null) {
            Iterator<ExpenseReportFormField> fldIter = formFields.iterator();
            while (fldIter.hasNext()) {
                ExpenseReportFormField frmFld = fldIter.next();
                if (frmFld.getId().equalsIgnoreCase(fieldId)) {
                    fldVal = frmFld.getValue();
                    break;
                }
            }
        }
        return fldVal;
    }

    /**
     * Helper class to handle parsing of expense report detail XML.
     */
    public static class ReportDetailSAXHandler extends ReportListSAXHandler {

        private static final String CLS_TAG = ExpenseReportDetail.CLS_TAG + "."
                + ReportDetailSAXHandler.class.getSimpleName();

        protected static final String REPORT_DETAIL = "ReportDetail";
        protected static final String COMMENTS = "Comments";
        protected static final String EXCEPTIONS = "Exceptions";
        protected static final String FIELDS = "Fields";
        protected static final String IMAGE_REQUIRED = "ImageRequired";
        protected static final String TOTAL_DUE_COMPANY_CARD = "TotalDueCompanyCard";
        protected static final String TOTAL_OWED_BY_EMPLOYEE = "TotalOwedByEmployee";
        protected static final String TOTAL_PAID_BY_COMPANY = "TotalPaidByCompany";
        protected static final String FORM_KEY = "FormKey";
        protected static final String COMPANY_DISBURSEMENTS = "CompanyDisbursements";
        protected static final String EMPLOYEE_DISBURSEMENTS = "EmployeeDisbursements";

        // Contains whether a detail summary report is being parsed, i.e., one that has
        // a detailed report header but summary entries.
        boolean parsingDetailSummary;

        /**
         * Contains whether or not report comments are currently being parsed.
         */
        private boolean parsingReportComments;

        /**
         * Contains whether or not report exceptions are currently being parsed.
         */
        private boolean parsingReportExceptions;

        /**
         * Contains whether or not report form fields are currently being parsed.
         */
        private boolean parsingReportFields;

        private boolean parsingDisbursements;

        /**
         * Contains a reference to a SAX handler for parsing report comments.
         */
        private ExpenseReportComment.ExpenseReportCommentSAXHandler reportCommentHandler;

        /**
         * Contains a reference to a SAX handler for parsing report exceptions.
         */
        private ExpenseReportException.ExpenseReportExceptionSAXHandler reportExceptionHandler;

        /**
         * Contains a reference to a SAX handler for parsing report fields.
         */
        private ExpenseReportFormField.ExpenseReportFormFieldSAXHandler reportFieldHandler;

        private ExpenseReportDisbursement.ExpenseReportDisbursementsSAXHandler reportDisbursementHandler;

        /**
         * Gets the parsed instance of <code>ExpenseReportDetail</code>.
         * 
         * @return the parsed instance of <code>ExpenseReportDetail</code>.
         */
        public ExpenseReportDetail getReport() {
            ExpenseReportDetail retVal = null;
            // First check that a report starting with the tag '<ReportDetail>'
            // was parsed..in that case, 'report' should not be null.
            if (report != null && report instanceof ExpenseReportDetail) {
                retVal = (ExpenseReportDetail) report;
            }
            // Second, if a report detail object was returned from the server, but started
            // with a '<Report>' tag, then 'report' will be 'null', but the list 'reports' will
            // contain the one parsed report, this is due to the parent parser expecting a list of report
            // approvals.
            if (retVal == null && reports != null && reports.size() == 1) {
                if (reports.get(0) instanceof ExpenseReportDetail) {
                    retVal = (ExpenseReportDetail) reports.get(0);
                }
            }
            return retVal;
        }

        /**
         * Will initialize this expense detail report parser by creating a default expense report and initializing any internal
         * structures required for parsing.
         */
        public void initForParsing() {
            this.report = createReport();
            reportEntryMap = new HashMap<String, ExpenseReportEntry>();
        }

        /**
         * Will perform any clean up related to completion of parsing.
         */
        public void finishForParsing() {
            report.reportExpenseEntryMap = ExpenseReport.reportEntryMap;
            ExpenseReport.reportEntryMap = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.data.expense.ExpenseReport.ReportListSAXHandler#createReport()
         */
        @Override
        public ExpenseReport createReport() {
            return new ExpenseReportDetail();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

            if (parsingReportComments) {
                if (reportCommentHandler != null) {
                    reportCommentHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null report comment handler!");
                }
            } else if (parsingReportExceptions) {
                if (reportExceptionHandler != null) {
                    reportExceptionHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null report exception handler!");
                }
            } else if (parsingReportFields) {
                if (reportFieldHandler != null) {
                    reportFieldHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null report field handler!");
                }
            } else if (parsingDisbursements) {
                if (reportDisbursementHandler != null) {
                    reportDisbursementHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null report disbursement handler!");
                }
            } else {
                super.characters(ch, start, length);
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

            if (parsingReportComments) {
                if (reportCommentHandler != null) {
                    reportCommentHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null report comment handler!");
                }
            } else if (parsingReportExceptions) {
                if (reportExceptionHandler != null) {
                    reportExceptionHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null report exception handler!");
                }
            } else if (parsingReportFields) {
                if (reportFieldHandler != null) {
                    reportFieldHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null report field handler!");
                }
            } else if (parsingDisbursements) {
                if (reportDisbursementHandler != null) {
                    reportDisbursementHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null report disbursement handler!");
                }
            } else {
                elementHandled = false;
                super.startElement(uri, localName, qName, attributes);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(REPORT_DETAIL)) {
                        report = createReport();
                        // Set up the new expense report entry key.
                        reportEntryMap = new HashMap<String, ExpenseReportEntry>();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(COMMENTS)) {
                        parsingReportComments = true;
                        reportCommentHandler = new ExpenseReportComment.ExpenseReportCommentSAXHandler();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(EXCEPTIONS)) {
                        parsingReportExceptions = true;
                        reportExceptionHandler = new ExpenseReportException.ExpenseReportExceptionSAXHandler();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(FIELDS)) {
                        parsingReportFields = true;
                        reportFieldHandler = new ExpenseReportFormField.ExpenseReportFormFieldSAXHandler();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(COMPANY_DISBURSEMENTS)) {
                        parsingDisbursements = true;
                        reportDisbursementHandler = new ExpenseReportDisbursement.ExpenseReportDisbursementsSAXHandler(
                                DisbursementType.COMPANY);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(EMPLOYEE_DISBURSEMENTS)) {
                        parsingDisbursements = true;
                        reportDisbursementHandler = new ExpenseReportDisbursement.ExpenseReportDisbursementsSAXHandler(
                                DisbursementType.EMPLOYEE);
                        elementHandled = true;
                    }
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.data.expense.ExpenseReport.ReportToApproveListSAXHandler#createEntriesHandler()
         */
        protected ExpenseReportEntry.ExpenseReportEntrySAXHandler createEntriesHandler() {
            if (parsingDetailSummary) {
                return super.createEntriesHandler();
            } else {
                return new ExpenseReportEntryDetail.ExpenseReportEntryDetailSAXHandler();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if (report != null) {
                if (parsingReportComments) {
                    if (reportCommentHandler != null) {
                        if (localName.equalsIgnoreCase(COMMENTS)) {
                            ((ExpenseReportDetail) report).comments = reportCommentHandler.getReportComments();
                            parsingReportComments = false;
                            reportCommentHandler = null;
                        } else {
                            reportCommentHandler.endElement(uri, localName, qName);
                        }
                        elementHandled = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report comment handler!");
                        chars.setLength(0);
                    }
                } else if (parsingReportExceptions) {
                    if (reportExceptionHandler != null) {
                        if (localName.equalsIgnoreCase(EXCEPTIONS)) {
                            ((ExpenseReportDetail) report).exceptions = reportExceptionHandler.getReportExceptions();
                            parsingReportExceptions = false;
                            reportExceptionHandler = null;
                        } else {
                            reportExceptionHandler.endElement(uri, localName, qName);
                        }
                        elementHandled = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report exception handler!");
                        chars.setLength(0);
                    }
                } else if (parsingReportFields) {
                    if (reportFieldHandler != null) {
                        if (localName.equalsIgnoreCase(FIELDS)) {
                            ((ExpenseReportDetail) report).formFields = reportFieldHandler.getReportFormFields();
                            parsingReportFields = false;
                            reportFieldHandler = null;
                        } else {
                            reportFieldHandler.endElement(uri, localName, qName);
                        }
                        elementHandled = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report field handler!");
                        chars.setLength(0);
                    }
                } else if (parsingDisbursements) {
                    if (reportDisbursementHandler != null) {
                        if (localName.equalsIgnoreCase(COMPANY_DISBURSEMENTS)) {
                            ((ExpenseReportDetail) report).companyDisbursements = reportDisbursementHandler
                                    .getReportDisbursements();
                            parsingDisbursements = false;
                            reportDisbursementHandler = null;
                        } else if (localName.equalsIgnoreCase(EMPLOYEE_DISBURSEMENTS)) {
                            ((ExpenseReportDetail) report).employeeDisbursements = reportDisbursementHandler
                                    .getReportDisbursements();
                            parsingDisbursements = false;
                            reportDisbursementHandler = null;
                        } else {
                            reportDisbursementHandler.endElement(uri, localName, qName);
                        }
                        elementHandled = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report disbursement handler!");
                        chars.setLength(0);
                    }
                } else {
                    elementHandled = false;
                    super.endElement(uri, localName, qName);
                    if (!elementHandled) {
                        final String cleanChars = chars.toString().trim();
                        if (localName.equalsIgnoreCase(IMAGE_REQUIRED)) {
                            ((ExpenseReportDetail) report).imageRequired = Parse.safeParseBoolean(cleanChars);
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(TOTAL_DUE_COMPANY_CARD)) {
                            ((ExpenseReportDetail) report).totalDueCompanyCard = Parse.safeParseDouble(cleanChars);
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(TOTAL_OWED_BY_EMPLOYEE)) {
                            ((ExpenseReportDetail) report).totalOwedByEmployee = Parse.safeParseDouble(cleanChars);
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(TOTAL_PAID_BY_COMPANY)) {
                            ((ExpenseReportDetail) report).totalPaidByCompany = Parse.safeParseDouble(cleanChars);
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(FORM_KEY)) {
                            ((ExpenseReportDetail) report).formKey = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(REPORT_DETAIL)) {
                            // Assign the map to the report.
                            report.reportExpenseEntryMap = ExpenseReport.reportEntryMap;
                            ExpenseReport.reportEntryMap = null;
                            elementHandled = true;
                        } else if (this.getClass().equals(ReportDetailSAXHandler.class)) {
                            Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element '" + localName + "'.");
                            // Clear out any collected characters if this class is the last stop.
                            chars.setLength(0);
                        }
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report detail!");
                chars.setLength(0);
            }
            // Only clear out the characters if this item has been handled.
            if (elementHandled) {
                chars.setLength(0);
            }
        }

        /**
         * Will serialize a report header, i.e., everything except the expense entries.
         * 
         * @param strBldr
         *            the string builder to contain the serialized XML.
         * @param report
         *            the expense report to serialize.
         */
        public static void serializeReportHeader(StringBuilder strBldr, ExpenseReport report) {
            if (strBldr != null) {
                if (report != null) {
                    ExpenseReportDetail reportDetail = null;
                    if (report instanceof ExpenseReportDetail) {
                        reportDetail = (ExpenseReportDetail) report;
                    }
                    strBldr.append('<');
                    if (reportDetail != null) {
                        strBldr.append(REPORT_DETAIL);
                    } else {
                        strBldr.append(REPORT);
                    }
                    strBldr.append('>');
                    // Serialize all top-level attributes.
                    serializeToXML(strBldr, report);
                    // Serialize related objects.
                    if (reportDetail != null) {
                        // Serialize all fields.
                        if (reportDetail.getFormFields() != null) {
                            serializeFormFieldsToXML(strBldr, reportDetail.getFormFields());
                        }
                        // Serialize all comments.
                        if (reportDetail.getComments() != null) {
                            serializeCommentsToXML(strBldr, reportDetail.getComments());
                        }
                        // Serialize all exceptions.
                        if (reportDetail.getExceptions() != null) {
                            serializeExceptionsToXML(strBldr, reportDetail.getExceptions());
                        }
                    }
                    strBldr.append("</");
                    if (reportDetail != null) {
                        strBldr.append(REPORT_DETAIL);
                    } else {
                        strBldr.append(REPORT);
                    }
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeReportHeader: expRep is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeReportHeader: strBldr is null!");
            }
        }

        /**
         * Will serialize to XML the report level form fields
         * 
         * @param strBldr
         *            the <code>StringBuilder</code> instance.
         * @param frmFlds
         *            the list of form fields.
         */
        public static void serializeFormFieldsToXML(StringBuilder strBldr, List<ExpenseReportFormField> frmFlds) {
            if (strBldr != null) {
                if (frmFlds != null) {
                    strBldr.append('<');
                    strBldr.append(FIELDS);
                    strBldr.append(" xmlns:f='http://schemas.datacontract.org/2004/07/Snowbird'");
                    strBldr.append('>');
                    for (ExpenseReportFormField frmFld : frmFlds) {
                        ExpenseReportFormField.ExpenseReportFormFieldSAXHandler.serializeToXML(strBldr, frmFld);
                    }
                    strBldr.append("</");
                    strBldr.append(FIELDS);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeFormFieldsToXML: frmFlds is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeFormFieldsToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize an instance of <code>ExpenseReportDetail</code> to XML.
         * 
         * @param strBldr
         *            the string builder to contain the serialization.
         * @param expRepDet
         *            the expense report detail object to be serialized.
         */
        public static void serializeAllToXML(StringBuilder strBldr, ExpenseReportDetail expRepDet) {
            if (strBldr != null) {
                if (expRepDet != null) {
                    strBldr.append('<');
                    strBldr.append(REPORT_DETAIL);
                    strBldr.append('>');
                    // Serialize all top-level attributes.
                    serializeToXML(strBldr, expRepDet);
                    // Serialize all fields.
                    if (expRepDet.getFormFields() != null) {
                        serializeFormFieldsToXML(strBldr, expRepDet.getFormFields());
                    }
                    // Serialize all comments.
                    if (expRepDet.getComments() != null) {
                        serializeCommentsToXML(strBldr, expRepDet.getComments());
                    }
                    // Serialize all entries.
                    if (expRepDet.getExpenseEntries() != null) {
                        serializeExpenseEntriesToXML(strBldr, expRepDet.getExpenseEntries());
                    }
                    // Serialize all exceptions.
                    if (expRepDet.getExceptions() != null) {
                        serializeExceptionsToXML(strBldr, expRepDet.getExceptions());
                    }
                    strBldr.append("</");
                    strBldr.append(REPORT_DETAIL);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeAllToXML: expRepDet is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeAllToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize a list of expenese report comments to XML.
         * 
         * @param strBldr
         *            the string builder holding the serialized XML.
         * @param comments
         *            the list of comments.
         */
        public static void serializeCommentsToXML(StringBuilder strBldr, List<ExpenseReportComment> comments) {
            if (strBldr != null) {
                if (comments != null) {
                    strBldr.append('<');
                    strBldr.append(COMMENTS);
                    strBldr.append('>');
                    for (ExpenseReportComment comment : comments) {
                        ExpenseReportComment.ExpenseReportCommentSAXHandler.serializeToXML(strBldr, comment);
                    }
                    strBldr.append("</");
                    strBldr.append(COMMENTS);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeCommentsToXML: comments is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeCommentsToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize a list of expenese report exceptions to XML.
         * 
         * @param strBldr
         *            the string builder holding the serialized XML.
         * @param comments
         *            the list of exceptions.
         */
        public static void serializeExceptionsToXML(StringBuilder strBldr, List<ExpenseReportException> exceptions) {
            if (strBldr != null) {
                if (exceptions != null) {
                    strBldr.append('<');
                    strBldr.append(EXCEPTIONS);
                    strBldr.append('>');
                    for (ExpenseReportException exception : exceptions) {
                        ExpenseReportException.ExpenseReportExceptionSAXHandler.serializeToXML(strBldr, exception);
                    }
                    strBldr.append("</");
                    strBldr.append(EXCEPTIONS);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeExceptionsToXML: comments is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeExceptionsToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize a list of expense report entries to XML.
         * 
         * @param strBldr
         *            the string builder holding the serialized XML.
         * @param comments
         *            the list of entries.
         */
        public static void serializeExpenseEntriesToXML(StringBuilder strBldr, List<ExpenseReportEntry> entries) {
            if (strBldr != null) {
                if (entries != null) {
                    strBldr.append('<');
                    strBldr.append(ENTRIES);
                    strBldr.append('>');
                    for (ExpenseReportEntry entry : entries) {
                        ExpenseReportEntryDetail.ExpenseReportEntryDetailSAXHandler.serializeAllToXML(strBldr,
                                (ExpenseReportEntryDetail) entry);
                    }
                    strBldr.append("</");
                    strBldr.append(ENTRIES);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeExpenseEntriesToXML: comments is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeExpenseEntriesToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize to XML the attributes of a report detail object.
         * 
         * @param strBldr
         *            the string builder to append XML serialized data.
         * @param expRepDet
         *            the expense report detail object.
         */
        public static void serializeToXML(StringBuilder strBldr, ExpenseReport report) {
            if (strBldr != null) {
                if (report != null) {
                    ExpenseReportDetail reportDetail = null;
                    if (report instanceof ExpenseReportDetail) {
                        reportDetail = (ExpenseReportDetail) report;
                    }
                    // ApsKey.
                    ViewUtil.addXmlElement(strBldr, APS_KEY, report.apsKey);
                    // ApvStatusName.
                    ViewUtil.addXmlElement(strBldr, APV_STATUS_NAME, report.apvStatusName);
                    // AprEmpName.
                    ViewUtil.addXmlElement(strBldr, APRV_EMP_NAME, report.aprvEmpName);
                    // CaReturnsAmount.
                    ViewUtil.addXmlElement(strBldr, CA_RETURNS_AMOUNT, report.caReturnsAmount);
                    // CrnCode
                    ViewUtil.addXmlElement(strBldr, CRN_CODE, report.crnCode);
                    // CurrentSequence
                    ViewUtil.addXmlElement(strBldr, CURRENT_SEQUENCE, report.currentSequence);
                    // DueCompanyCard
                    ViewUtil.addXmlElement(strBldr, DUE_COMPANY_CARD, report.dueCompanyCard);
                    // EmployeeName
                    ViewUtil.addXmlElement(strBldr, EMPLOYEE_NAME, report.employeeName);
                    // EverSentBack
                    ViewUtil.addXmlElementYN(strBldr, EVER_SENT_BACK, report.everSentBack);
                    if (reportDetail != null) {
                        // FormKey
                        ViewUtil.addXmlElement(strBldr, FORM_KEY, reportDetail.formKey);
                    }
                    // HasException
                    ViewUtil.addXmlElementYN(strBldr, HAS_EXCEPTION, report.hasException);
                    if (reportDetail != null) {
                        // ImageRequired
                        ViewUtil.addXmlElementYN(strBldr, IMAGE_REQUIRED, reportDetail.imageRequired);
                    }
                    // LastComment
                    ViewUtil.addXmlElement(strBldr, LAST_COMMENT, report.lastComment);
                    // PayKey
                    ViewUtil.addXmlElement(strBldr, PAY_KEY, report.payKey);
                    // PayStatusName
                    ViewUtil.addXmlElement(strBldr, PAY_STATUS_NAME, report.payStatusName);
                    // PdfUrl
                    ViewUtil.addXmlElement(strBldr, PDF_URL, report.pdfUrl);
                    // PolKey
                    ViewUtil.addXmlElement(strBldr, POL_KEY, report.polKey);
                    // ProcessInstanceKey
                    ViewUtil.addXmlElement(strBldr, PROCESS_INSTANCE_KEY, report.processInstanceKey);
                    // Purpose
                    ViewUtil.addXmlElement(strBldr, PURPOSE, report.purpose);
                    // ReceiptImageAvailable
                    ViewUtil.addXmlElementYN(strBldr, RECEIPT_IMAGE_AVAILABLE, report.receiptImageAvailable);
                    // ReceiptUrl
                    ViewUtil.addXmlElement(strBldr, RECEIPT_URL, report.receiptUrl);
                    // ReportDate
                    ViewUtil.addXmlElement(strBldr, REPORT_DATE, report.reportDate);
                    // ReportName
                    ViewUtil.addXmlElement(strBldr, REPORT_NAME, report.reportName);
                    // RptKey
                    ViewUtil.addXmlElement(strBldr, REPORT_KEY, report.reportKey);
                    // SeverityLevel
                    ViewUtil.addXmlElement(strBldr, SEVERITY_LEVEL, report.severityLevel);
                    // StepKey
                    ViewUtil.addXmlElement(strBldr, STEP_KEY, report.stepKey);
                    // SubmitDate
                    ViewUtil.addXmlElement(strBldr, SUBMIT_DATE, report.submitDate);
                    // TotalPostedAmount
                    ViewUtil.addXmlElement(strBldr, TOTAL_POSTED_AMOUNT, report.totalPostedAmount);
                    // TotalApprovedAmount
                    ViewUtil.addXmlElement(strBldr, TOTAL_APPROVED_AMOUNT, report.totalApprovedAmount);
                    // TotalApprovedAmountPending
                    ViewUtil.addXmlElement(strBldr, TOTAL_APPROVED_AMOUNT_PENDING, report.totalApprovedAmountPending);
                    // TotalClaimedAmount
                    ViewUtil.addXmlElement(strBldr, TOTAL_CLAIMED_AMOUNT, report.totalClaimedAmount);
                    // TotalDueCompany
                    ViewUtil.addXmlElement(strBldr, TOTAL_DUE_COMPANY, report.totalDueCompany);
                    if (reportDetail != null) {
                        // TotalDueCompanyCard
                        ViewUtil.addXmlElement(strBldr, TOTAL_DUE_COMPANY_CARD, reportDetail.totalDueCompanyCard);
                    }
                    // TotalDueEmployee
                    ViewUtil.addXmlElement(strBldr, TOTAL_DUE_EMPLOYEE, report.totalDueEmployee);
                    if (reportDetail != null) {
                        // TotalOwedByEmployee
                        ViewUtil.addXmlElement(strBldr, TOTAL_OWED_BY_EMPLOYEE, reportDetail.totalOwedByEmployee);
                    }
                    if (reportDetail != null) {
                        // TotalPaidByCompany
                        ViewUtil.addXmlElement(strBldr, TOTAL_PAID_BY_COMPANY, reportDetail.totalPaidByCompany);
                    }
                    // TotalPersonalAmount
                    ViewUtil.addXmlElement(strBldr, TOTAL_PERSONAL_AMOUNT, report.totalPersonalAmount);
                    // TotalRejectedAmount
                    ViewUtil.addXmlElement(strBldr, TOTAL_REJECTED_AMOUNT, report.totalRejectedAmount);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: expRep is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: strBldr is null!");
            }
        }
    }

}
