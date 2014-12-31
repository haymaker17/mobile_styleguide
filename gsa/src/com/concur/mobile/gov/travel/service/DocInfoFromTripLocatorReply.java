/**
 * @author sunill
 */
package com.concur.mobile.gov.travel.service;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.doc.data.GovDocument;

public class DocInfoFromTripLocatorReply extends ServiceReply {

    public GovDocument document;
    public Calendar lastRefreshTime;
    public String xmlReply;

    public void setLastRefreshTime(Calendar lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }

    public static DocInfoFromTripLocatorReply parseXml(String responseXml) {

        DocInfoFromTripLocatorReply reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            DocumentListReplySAXHandler handler = new DocumentListReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class DocumentListReplySAXHandler extends DefaultHandler {

        // list of tags
        private static final String DOCUMENT = "Document";
        private static final String LOCATOR = "TMTripLocatorResponse";

        // parsing character
        private StringBuilder chars;

        // // flag to track our place in xml hierarchy.
        private boolean inDoc;
        private boolean inLoc;

        // data holders
        private DocInfoFromTripLocatorReply reply;
        private GovDocument document;

        protected DocInfoFromTripLocatorReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            chars = new StringBuilder();
            reply = new DocInfoFromTripLocatorReply();

        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        /**
         * Handle the opening of all elements. Create data objects as needed for use
         * in endElement().
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            super.startElement(uri, localName, qName, attributes);
            if (localName.equalsIgnoreCase(LOCATOR)) {
                inLoc = true;
            } else if (localName.equalsIgnoreCase(DOCUMENT)) {
                inDoc = true;
                document = new GovDocument();
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();

            if (inDoc) {
                if (localName.equalsIgnoreCase(DOCUMENT)) {
                    reply.document = document;
                    inDoc = false;
                } else {
                    document.handleElement(localName, cleanChars);
                }
            } else if (inLoc) {
                if (localName.equalsIgnoreCase(LOCATOR)) {
                    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    reply.lastRefreshTime = now;
                    inLoc = false;
                }
            }
            chars.setLength(0);
        }

    }
}
