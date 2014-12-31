/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * Models an expense report exception.
 * 
 * @author AndrewK
 */
public class ExpenseReportException implements Serializable {

    private static final long serialVersionUID = -7825819142867824178L;

    private static final String CLS_TAG = ExpenseReportException.class.getSimpleName();

    private String exception;

    private Boolean isCleared;

    private String reportEntryKey;

    private String severityLevel;

    /**
     * Gets the exception text.
     * 
     * @return the exception text.
     */
    public String getException() {
        return exception;
    }

    /**
     * Whether this exception has been cleared.
     * 
     * @return whether this exception has been cleared.
     */
    public boolean isCleared() {
        return ((isCleared != null) ? isCleared : Boolean.FALSE);
    }

    /**
     * Gets the report entry key.
     * 
     * @return the report entry key.
     */
    public String getReportEntryKey() {
        return reportEntryKey;
    }

    /**
     * Gets the severity level.
     * 
     * @return the severity level.
     */
    public String getSeverityLevel() {
        return severityLevel;
    }

    /**
     * Provides an extension of <code>DefaultHandler</code> to support parsing expense report exception information.
     * 
     * @author AndrewK
     */
    static class ExpenseReportExceptionSAXHandler extends DefaultHandler implements Serializable {

        private static final long serialVersionUID = -7595357222738522933L;

        private static final String CLS_TAG = ExpenseReportException.CLS_TAG + "."
                + ExpenseReportExceptionSAXHandler.class.getSimpleName();

        private static final String CES_EXCEPTION = "CESException";

        private static final String EXCEPTIONS_STR = "ExceptionsStr";

        private static final String IS_CLEARED = "IsCleared";

        private static final String REPORT_ENTRY_KEY = "RpeKey";

        private static final String SEVERITY_LEVEL = "SeverityLevel";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        /**
         * Contains a reference to a list of <code>ExpenseReportEntry</code> objects that have been parsed.
         */
        private ArrayList<ExpenseReportException> reportExceptions = new ArrayList<ExpenseReportException>();

        /**
         * Contains a reference to the report entry currently being built.
         */
        private ExpenseReportException reportException;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Gets the list of <code>ExpenseReportEntry</code> objects that have been parsed.
         * 
         * @return the list of parsed <code>ExpenseReportEntry</code> objects.
         */
        ArrayList<ExpenseReportException> getReportExceptions() {
            return reportExceptions;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
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

            if (localName.equalsIgnoreCase(CES_EXCEPTION)) {
                reportException = new ExpenseReportException();
                chars.setLength(0);
                elementHandled = true;
            }
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

            if (reportException != null) {
                if (localName.equalsIgnoreCase(EXCEPTIONS_STR)) {
                    reportException.exception = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(REPORT_ENTRY_KEY)) {
                    reportException.reportEntryKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(SEVERITY_LEVEL)) {
                    reportException.severityLevel = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(IS_CLEARED)) {
                    reportException.isCleared = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(CES_EXCEPTION)) {
                    reportExceptions.add(reportException);
                    elementHandled = true;
                } else if (!elementHandled && this.getClass().equals(ExpenseReportException.class)) {
                    // Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '"
                    // + localName + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null current report exception!");
            }

            // Clear out the stored element values.
            chars.setLength(0);
        }

        /**
         * Will serialize to XML an exception object.
         * 
         * @param strBldr
         *            the string builder to hold the serialized object.
         * @param exception
         *            the exception to serialize.
         */
        public static void serializeToXML(StringBuilder strBldr, ExpenseReportException exception) {
            if (strBldr != null) {
                if (exception != null) {
                    strBldr.append('<');
                    strBldr.append(CES_EXCEPTION);
                    strBldr.append('>');
                    // ExceptionsStr
                    ViewUtil.addXmlElement(strBldr, EXCEPTIONS_STR, exception.exception);
                    // IsCleared
                    ViewUtil.addXmlElementYN(strBldr, IS_CLEARED, exception.isCleared);
                    // RpeKey
                    ViewUtil.addXmlElement(strBldr, REPORT_ENTRY_KEY, exception.reportEntryKey);
                    // SeverityLevel
                    ViewUtil.addXmlElement(strBldr, SEVERITY_LEVEL, exception.severityLevel);
                    strBldr.append("</");
                    strBldr.append(CES_EXCEPTION);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: exception is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: strBldr is null!");
            }
        }

    }

}
