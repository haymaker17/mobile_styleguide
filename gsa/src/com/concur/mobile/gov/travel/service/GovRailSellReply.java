package com.concur.mobile.gov.travel.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.travel.rail.service.RailSellReply;
import com.concur.mobile.platform.util.Parse;

public class GovRailSellReply extends RailSellReply {

    // Contains the itinerary locator.
    public Long itinLocator;
    // Gov authorization
    public String authorizationNumber;

    // //////////////////////////////////////////////////////////////////////
    // At the signpost ahead: XML parsing
    // //////////////////////////////////////////////////////////////////////

    public static GovRailSellReply parseXMLReply(String responseXml) {

        GovRailSellReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            RailSellReplySAXHandler handler = new RailSellReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class RailSellReplySAXHandler extends DefaultHandler {

        // Fields to help parsing
        private StringBuilder chars;

        // Holders for our parsed data
        private GovRailSellReply reply;

        protected GovRailSellReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new GovRailSellReply();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            chars.append(ch, start, length);
        }

        /**
         * Handle the opening of all elements. Create data objects as needed for use in endElement().
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();

            // Top-level elements
            handleResponseElement(localName, cleanChars);

            chars.setLength(0);
        }

        /**
         * Handle the response level elements
         * 
         * @param localName
         */
        private void handleResponseElement(String localName, String cleanChars) {

            if (localName.equalsIgnoreCase("ItinLocator")) {
                reply.itinLocator = Parse.safeParseLong(cleanChars);
            } else if (localName.equalsIgnoreCase("Status")) {
                reply.mwsStatus = cleanChars;
            } else if (localName.equalsIgnoreCase("ErrorMessage")) {
                reply.mwsErrorMessage = cleanChars;
            } else if (localName.equalsIgnoreCase("AuthorizationNumber")) {
                reply.authorizationNumber = cleanChars;
            }
        }

    }
}