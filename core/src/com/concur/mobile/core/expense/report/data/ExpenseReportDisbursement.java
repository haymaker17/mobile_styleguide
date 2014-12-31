package com.concur.mobile.core.expense.report.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

public class ExpenseReportDisbursement implements Serializable {

    private static final long serialVersionUID = 321325271380949352L;

    public enum DisbursementType {
        COMPANY, EMPLOYEE
    };

    public DisbursementType type;
    public String label;
    public Double amount;

    static class ExpenseReportDisbursementsSAXHandler extends DefaultHandler implements Serializable {

        private static final long serialVersionUID = -1721430467316286362L;

        private static final String FORM_FIELD = "FormField";
        private static final String LABEL = "Label";
        private static final String VALUE = "Value";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        private DisbursementType type;

        private ArrayList<ExpenseReportDisbursement> disbursements = new ArrayList<ExpenseReportDisbursement>();

        private ExpenseReportDisbursement disbursement;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        public ExpenseReportDisbursementsSAXHandler(DisbursementType type) {
            this.type = type;
        }

        ArrayList<ExpenseReportDisbursement> getReportDisbursements() {
            return disbursements;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            elementHandled = false;

            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(FORM_FIELD)) {
                disbursement = new ExpenseReportDisbursement();
                disbursement.type = type;
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

            if (disbursement != null) {
                if (localName.equalsIgnoreCase(LABEL)) {
                    disbursement.label = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(VALUE)) {
                    disbursement.amount = Parse.safeParseDouble(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(FORM_FIELD)) {
                    disbursements.add(disbursement);
                    elementHandled = true;
                }
            } else {
                Log.e(Const.LOG_TAG, this.getClass().getSimpleName() + ".endElement: null current disbursement!");
            }

            // Clear out the stored element values.
            chars.setLength(0);
        }

    }
}
