/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * Models an expense report comment.
 * 
 * @author AndrewK
 */
public class ExpenseReportComment implements Serializable {

    private static final long serialVersionUID = -481034791120246184L;

    private static final String CLS_TAG = ExpenseReportComment.class.getSimpleName();

    private String comment;

    private String commentBy;

    private String commentKey;

    private String creationDate;

    private Calendar creationDateCalendar;

    private Boolean isLatest;

    private String reportEntryKey;

    private String reportKey;

    /**
     * Gets the comment text.
     * 
     * @return the comment text.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the author of the comment.
     * 
     * @return the author of the comment.
     */
    public String getCommentBy() {
        return commentBy;
    }

    /**
     * Gets the comment key.
     * 
     * @return the comment key.
     */
    public String getCommentKey() {
        return commentKey;
    }

    /**
     * Get the comment creation date.
     * 
     * @return the comment creation date.
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * Get the comment creation date calendar.
     * 
     * @return the comment creation date calendar.
     */
    public Calendar getCreationDateCalendar() {
        return creationDateCalendar;
    }

    /**
     * Get the formatted comment creation date.
     * 
     * @return the formatted comment creation date.
     */
    public String getFormattedCreationDate() {
        return FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(creationDateCalendar.getTime());
    }

    /**
     * Gets whether this is the latest comment.
     * 
     * @return whether this is the latest comment.
     */
    public boolean isLatest() {
        return ((isLatest != null) ? isLatest : Boolean.FALSE);
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
     * Gets the report key.
     * 
     * @return the report key.
     */
    public String getReportKey() {
        return reportKey;
    }

    /**
     * Provides an extension of <code>DefaultHandler</code> to support parsing expense report comment information.
     * 
     * @author AndrewK
     */
    static class ExpenseReportCommentSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = ExpenseReportComment.CLS_TAG + "."
                + ExpenseReportCommentSAXHandler.class.getSimpleName();

        private static final String REPORT_COMMENT = "ReportComment";

        private static final String COMMENT = "Comment";

        private static final String COMMENT_BY = "CommentBy";

        private static final String COMMENT_KEY = "CommentKey";

        private static final String CREATION_DATE = "CreationDate";

        private static final String IS_LATEST = "IsLatest";

        private static final String REPORT_ENTRY_KEY = "RpeKey";

        private static final String REPORT_KEY = "RptKey";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        /**
         * Contains a reference to a list of <code>ExpenseReportComment</code> objects that have been parsed.
         */
        private ArrayList<ExpenseReportComment> reportComments = new ArrayList<ExpenseReportComment>();

        /**
         * Contains a reference to the report comment currently being built.
         */
        private ExpenseReportComment reportComment;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Gets the list of <code>ExpenseReportComment</code> objects that have been parsed.
         * 
         * @return the list of parsed <code>ExpenseReportComment</code> objects.
         */
        ArrayList<ExpenseReportComment> getReportComments() {
            return reportComments;
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

            if (localName.equalsIgnoreCase(REPORT_COMMENT)) {
                reportComment = new ExpenseReportComment();
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

            if (reportComment != null) {
                if (localName.equalsIgnoreCase(COMMENT)) {
                    reportComment.comment = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(COMMENT_BY)) {
                    reportComment.commentBy = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(COMMENT_KEY)) {
                    reportComment.commentKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(CREATION_DATE)) {
                    reportComment.creationDate = chars.toString().trim();
                    reportComment.creationDateCalendar = Parse.parseXMLTimestamp(reportComment.creationDate);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(IS_LATEST)) {
                    reportComment.isLatest = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(REPORT_ENTRY_KEY)) {
                    reportComment.reportEntryKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(REPORT_KEY)) {
                    reportComment.reportKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(REPORT_COMMENT)) {
                    reportComments.add(reportComment);
                    elementHandled = true;
                } else if (!elementHandled && this.getClass().equals(ExpenseReportComment.class)) {
                    // Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '"
                    // + localName + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null current report comment!");
            }

            // Clear out the stored element values.
            chars.setLength(0);
        }

        /**
         * Will serialize to XML an expense report comment.
         * 
         * @param strBldr
         *            the string builder to contain the serialized XML.
         * @param comment
         *            the comment to serialize.
         */
        public static void serializeToXML(StringBuilder strBldr, ExpenseReportComment comment) {
            if (strBldr != null) {
                if (comment != null) {
                    strBldr.append('<');
                    strBldr.append(REPORT_COMMENT);
                    strBldr.append('>');
                    // Comment
                    ViewUtil.addXmlElement(strBldr, COMMENT, comment.comment);
                    // CommentBy
                    ViewUtil.addXmlElement(strBldr, COMMENT_BY, comment.commentBy);
                    // CommentKey
                    ViewUtil.addXmlElement(strBldr, COMMENT_KEY, comment.commentKey);
                    // CreationDate
                    ViewUtil.addXmlElement(strBldr, CREATION_DATE, comment.creationDate);
                    // IsLatest
                    ViewUtil.addXmlElementYN(strBldr, IS_LATEST, comment.isLatest);
                    // RpeKey
                    ViewUtil.addXmlElement(strBldr, REPORT_ENTRY_KEY, comment.reportEntryKey);
                    // RptKey
                    ViewUtil.addXmlElement(strBldr, REPORT_KEY, comment.reportKey);
                    strBldr.append("</");
                    strBldr.append(REPORT_COMMENT);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: comment is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: strBldr is null!");
            }
        }

    }

}
