/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.util.Const;

/**
 * Models an expense report entry itemization.
 * 
 * @author AndrewK
 */
public class ExpenseReportEntryItemization extends ExpenseReportEntryDetail {

    private static final long serialVersionUID = 1L;

    private final static String CLS_TAG = ExpenseReportEntryItemization.class.getSimpleName();

    /**
     * Provides an extension of <code>ExpenseReportEntryDetail.ExpenseReportEntryDetailSAXHandler</code> to support parsing
     * expense report entry itemization information.
     * 
     * @author AndrewK
     */
    public static class ExpenseReportEntryItemizationSAXHandler extends
            ExpenseReportEntryDetail.ExpenseReportEntryDetailSAXHandler {

        private static final String CLS_TAG = ExpenseReportEntryItemization.CLS_TAG + "."
                + ExpenseReportEntryItemizationSAXHandler.class.getSimpleName();

        private static final String ITEMIZATION_DETAIL = "ItemizationDetail";

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.data.expense.ExpenseReportEntry.ExpenseReportEntrySAXHandler#createReportEntry()
         */
        @Override
        protected ExpenseReportEntry createReportEntry() {
            return new ExpenseReportEntryItemization();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
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
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(ITEMIZATION_DETAIL)) {
                    reportEntry = createReportEntry();
                    elementHandled = true;
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if (reportEntry != null) {
                elementHandled = false;
                super.endElement(uri, localName, qName);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(ITEMIZATION_DETAIL)) {
                        reportEntries.add(reportEntry);
                        reportEntry = null;
                        elementHandled = true;
                    } else if (this.getClass().equals(ExpenseReportEntryItemizationSAXHandler.class)) {
                        // Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element '" + localName + "'.");
                        // Clear out any collected characters if this class should have handled it.
                        chars.setLength(0);
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report entry!");
                chars.setLength(0);
            }
            // Only clear if it was handled.
            if (elementHandled) {
                chars.setLength(0);
            }
        }

        public static void serializeItemizationAllToXML(StringBuilder strBldr, ExpenseReportEntryItemization itemization) {
            if (strBldr != null) {
                if (itemization != null) {
                    strBldr.append('<');
                    strBldr.append(ITEMIZATION_DETAIL);
                    strBldr.append('>');
                    // Serialize the top-level attributes.
                    serializeToXML(strBldr, itemization);
                    // Fields
                    if (itemization.getFormFields() != null) {
                        serializeFormFieldsToXML(strBldr, itemization.getFormFields());
                    }
                    // Attendees
                    if (itemization.getAttendees() != null) {
                        serializeAttendeesToXML(strBldr, itemization.getAttendees());
                    }
                    // Comments
                    if (itemization.getComments() != null) {
                        serializeCommentsToXML(strBldr, itemization.getComments());
                    }
                    // Exceptions
                    if (itemization.getExceptions() != null) {
                        serializeExceptionsToXML(strBldr, itemization.getExceptions());
                    }
                    // Itemizations
                    if (itemization.getItemizations() != null) {
                        serializeItemizationsToXML(strBldr, itemization.getItemizations());
                    }
                    strBldr.append("</");
                    strBldr.append(ITEMIZATION_DETAIL);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeItemizationAllToXML: itemization is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeItemizationAllToXML: strBldr is null!");
            }
        }

    }

}
