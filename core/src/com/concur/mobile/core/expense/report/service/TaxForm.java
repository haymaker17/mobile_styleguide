package com.concur.mobile.core.expense.report.service;

import java.io.Serializable;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.FormFieldType;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;

public class TaxForm implements Serializable {

    private static final long serialVersionUID = -1169183810206196384L;

    public String taxAuthKey;
    public String taxFormKey;
    public List<ExpenseReportFormField> taxFormField;

    public boolean inFormFields = false;

    public static class TaxFormSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = TaxFormSAXHandler.class.getSimpleName();

        // list of tags
        private static final String TAK = "TaxAuthKey";
        private static final String TFK = "TaxFormKey";
        private static final String FIELDS = "Fields";

        protected TaxForm taxForm;
        /**
         * Contains whether or not report entry form fields are currently being parsed.
         */
        private boolean parsingReportEntryFields;

        /**
         * Contains a reference to a SAX handler for parsing report entry fields.
         */
        private ExpenseReportFormField.ExpenseReportFormFieldSAXHandler entryFieldHandler;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        // Fields to help parsing
        protected StringBuilder chars = new StringBuilder();

        public void createTaxForm() {
            taxForm = new TaxForm();
        }

        public TaxForm getTaxForm() {
            return taxForm;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (parsingReportEntryFields) {
                if (entryFieldHandler != null) {
                    entryFieldHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null entry field handler!");
                }
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

            if (parsingReportEntryFields) {
                if (entryFieldHandler != null) {
                    entryFieldHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null entry field handler!");
                }
            } else {
                elementHandled = false;
                super.startElement(uri, localName, qName, attributes);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(FIELDS)) {
                        parsingReportEntryFields = true;
                        entryFieldHandler = new ExpenseReportFormField.ExpenseReportFormFieldSAXHandler();
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
            if (parsingReportEntryFields) {
                if (entryFieldHandler != null) {
                    if (localName.equalsIgnoreCase(FIELDS)) {
                        taxForm.taxFormField = entryFieldHandler.getReportFormFields();
                        for (ExpenseReportFormField formField : taxForm.taxFormField) {
                            formField.setFormFieldType(FormFieldType.VAT);
                        }
                        parsingReportEntryFields = false;
                        entryFieldHandler = null;
                    } else {
                        entryFieldHandler.endElement(uri, localName, qName);
                    }
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report entry field handler!");
                }
            } else {
                elementHandled = false;
                super.endElement(uri, localName, qName);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(TAK)) {
                        taxForm.taxAuthKey = chars.toString().trim();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(TFK)) {
                        taxForm.taxFormKey = chars.toString().trim();
                        elementHandled = true;
                    } else if (this.getClass().equals(TaxFormSAXHandler.class)) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element '" + localName + "'.");
                        // Clear out any collected characters if this class is the last stop.
                        chars.setLength(0);
                    }
                }
            }

            // Only clear if it was handled.
            if (elementHandled) {
                chars.setLength(0);
            }
        }

        /**
         * Will serialize to XML the report entry level form fields
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

        public static void serializeTaxFormToXML(StringBuilder strBldr, TaxForm taxForm) {
            // from fields
            serializeFormFieldsToXML(strBldr, taxForm.taxFormField);
            // Tax Auth Key
            ViewUtil.addXmlElement(strBldr, TAK, taxForm.taxAuthKey);
            // Tax Form Key
            ViewUtil.addXmlElement(strBldr, TFK, taxForm.taxFormKey);
        }

    }

}
