/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.ExpenseReportFormFieldSAXHandler;
import com.concur.mobile.core.util.Const;

/**
 * A class that associates an attendee type key with a list of fields used to search for attendees of that type.
 * 
 * @author andy
 */
public class AttendeeSearchField {

    private static final String CLS_TAG = AttendeeSearchField.class.getSimpleName();

    public String atnTypeKey;

    public List<ExpenseReportFormField> searchFields;

    /**
     * An extension of <code>DefaultHandler</code> for the purposes of parsing an association of an attendee type key to a list of
     * fields for attendee search.
     */
    public static class AttendeeSearchFieldSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = AttendeeSearchField.CLS_TAG + "."
                + AttendeeSearchFieldSAXHandler.class.getSimpleName();

        private static final String ATTENDEE_SEARCH_FIELD_LIST = "ArrayOfAttendeeSearchFields";
        private static final String ATTENDEE_SEARCH_FIELD = "AttendeeSearchFields";
        private static final String ATTENDEE_TYPE_KEY = "AtnTypeKey";
        private static final String FIELDS = "Fields";

        /**
         * Contains a reference to the form field parser.
         */
        private ExpenseReportFormFieldSAXHandler formFieldHandler;

        // Fields to help parsing
        protected StringBuilder chars = new StringBuilder();

        /**
         * Contains a reference to an instance of <code>AttendeeSearchField</code> currently being parsed.
         */
        AttendeeSearchField atnSrchFld;

        /**
         * Contains a list of parsed instances of <code>AttendeeSearchField.</code>.
         */
        List<AttendeeSearchField> atnSrchFldList;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Gets the list of parsed instances of <code>AttendeeSearchField</code>.
         * 
         * @return the list of parsed instances of <code>AttendeeSearchField</code>.
         */
        public List<AttendeeSearchField> getAttendeeSearchFields() {
            return atnSrchFldList;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

            if (formFieldHandler != null) {
                formFieldHandler.characters(ch, start, length);
            } else {
                super.characters(ch, start, length);
                chars.append(ch, start, length);
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

            if (formFieldHandler != null) {
                formFieldHandler.startElement(uri, localName, qName, attributes);
                elementHandled = true;
            } else {
                elementHandled = false;
                super.startElement(uri, localName, qName, attributes);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(ATTENDEE_SEARCH_FIELD_LIST)) {
                        atnSrchFldList = new ArrayList<AttendeeSearchField>();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(ATTENDEE_SEARCH_FIELD)) {
                        atnSrchFld = new AttendeeSearchField();
                        chars.setLength(0);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(FIELDS)) {
                        formFieldHandler = new ExpenseReportFormField.ExpenseReportFormFieldSAXHandler();
                        elementHandled = true;
                    }
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

            if (formFieldHandler != null) {
                if (localName.equalsIgnoreCase(FIELDS)) {
                    if (atnSrchFld != null) {
                        atnSrchFld.searchFields = formFieldHandler.getReportFormFields();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: formFieldHandler is null!");
                    }
                    formFieldHandler = null;
                } else {
                    formFieldHandler.endElement(uri, localName, qName);
                }
                elementHandled = true;
            } else {
                elementHandled = false;
                super.endElement(uri, localName, qName);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(ATTENDEE_TYPE_KEY)) {
                        if (atnSrchFld != null) {
                            atnSrchFld.atnTypeKey = chars.toString().trim();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: atnSrchFld is null!");
                        }
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(ATTENDEE_SEARCH_FIELD)) {
                        if (atnSrchFld != null) {
                            if (atnSrchFldList == null) {
                                atnSrchFldList = new ArrayList<AttendeeSearchField>();
                            }
                            atnSrchFldList.add(atnSrchFld);
                            atnSrchFld = null;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: atnSrchFld is null!");
                        }
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(ATTENDEE_SEARCH_FIELD_LIST)) {
                        elementHandled = true;
                    } else if (this.getClass().equals(AttendeeSearchFieldSAXHandler.class)) {
                        elementHandled = true;
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled tag '" + localName + "'.");
                    }
                }
            }

            if (elementHandled) {
                // Clear out the stored element values.
                chars.setLength(0);
            }
        }

    }

}
