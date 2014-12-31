package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.expense.report.service.TaxForm.TaxFormSAXHandler;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class GetTaxFormReply extends ServiceReply {

    public String xmlReply;

    public List<TaxForm> listOfTaxForm;

    public Calendar lastRefreshTime;

    public static GetTaxFormReply parseXml(String responseXml) {

        GetTaxFormReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            TaxFormsSAXHandler handler = new TaxFormsSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    public static class TaxFormsSAXHandler extends TaxFormSAXHandler {

        private static final String CLS_TAG = TaxFormsSAXHandler.class.getSimpleName();

        private static final String TAX_FORMS = "TaxForms";
        private static final String TAX_FORM = "TaxForm";

        /**
         * Contains whether or not tax forms are currently being parsed.
         */
        private boolean parsingTaxForms;

        private boolean inTaxForms;
        /**
         * Contains a reference to a SAX handler for parsing report entry itemizations.
         */
        private TaxForm.TaxFormSAXHandler taxFormHandler;

        private GetTaxFormReply reply;

        public TaxFormsSAXHandler() {
            reply = new GetTaxFormReply();
            reply.listOfTaxForm = new ArrayList<TaxForm>();
        }

        public GetTaxFormReply getReply() {
            return reply;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

            if (parsingTaxForms) {
                if (taxFormHandler != null) {
                    taxFormHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null entry comment handler!");
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

            if (parsingTaxForms) {
                if (taxFormHandler != null) {
                    taxFormHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null entry comment handler!");
                }
            } else {
                elementHandled = false;
                super.startElement(uri, localName, qName, attributes);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(TAX_FORMS)) {
                        inTaxForms = true;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(TAX_FORM)) {
                        parsingTaxForms = true;
                        taxFormHandler = new TaxForm.TaxFormSAXHandler();
                        taxFormHandler.createTaxForm();
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

            if (parsingTaxForms) {
                if (taxFormHandler != null) {
                    if (localName.equalsIgnoreCase(TAX_FORM)) {
                        taxForm = taxFormHandler.getTaxForm();
                        reply.listOfTaxForm.add(taxForm);
                        parsingTaxForms = false;
                        taxFormHandler = null;
                    } else {
                        taxFormHandler.endElement(uri, localName, qName);
                    }
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report entry comment handler!");
                }
            } else {
                elementHandled = false;
                super.endElement(uri, localName, qName);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(TAX_FORMS)) {
                        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        reply.lastRefreshTime = now;
                        elementHandled = true;
                        inTaxForms = false;
                    } else if (this.getClass().equals(TaxFormsSAXHandler.class)) {
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

        public static void serializeTaxFormItemToXML(StringBuilder strBldr, TaxForm taxForm) {
            strBldr.append('<');
            strBldr.append(TAX_FORM);
            strBldr.append('>');
            TaxForm.TaxFormSAXHandler.serializeTaxFormToXML(strBldr, taxForm);
            strBldr.append("</");
            strBldr.append(TAX_FORM);
            strBldr.append('>');
        }

    }
}
