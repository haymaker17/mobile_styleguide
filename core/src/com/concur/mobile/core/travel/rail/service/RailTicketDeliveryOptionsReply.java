package com.concur.mobile.core.travel.rail.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.rail.data.RailTicketDeliveryOption;

public class RailTicketDeliveryOptionsReply extends ServiceReply {

    public static final String KEY_COUNT = "tdoCount";
    public static final String KEY_TDO = "tdo";

    public ArrayList<RailTicketDeliveryOption> deliveryOptions;

    public RailTicketDeliveryOptionsReply() {
        deliveryOptions = new ArrayList<RailTicketDeliveryOption>();
    }

    public static RailTicketDeliveryOptionsReply parseXMLReply(String responseXml) {

        RailTicketDeliveryOptionsReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            RailTicketDeliveryOptionsReplySAXHandler handler = new RailTicketDeliveryOptionsReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class RailTicketDeliveryOptionsReplySAXHandler extends DefaultHandler {

        private static final String OPTION = "TicketDeliveryOption";

        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.
        // A stack of element names would be cleaner and prettier but the booleans will be a lot faster.
        private boolean inOption;

        // Holders for our parsed data
        private RailTicketDeliveryOptionsReply reply;
        private RailTicketDeliveryOption tdo;

        protected RailTicketDeliveryOptionsReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new RailTicketDeliveryOptionsReply();
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

            if (localName.equalsIgnoreCase(OPTION)) {
                tdo = new RailTicketDeliveryOption();
                inOption = true;
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();

            if (inOption) {
                if (localName.equalsIgnoreCase(OPTION)) {
                    reply.deliveryOptions.add(tdo);
                    inOption = false;
                } else {
                    // hand off to object
                    tdo.handleElement(localName, cleanChars);
                }
            } else {
                // Top-level elements
                handleResponseElement(localName, cleanChars);
            }

            chars.setLength(0);
        }

        /**
         * Handle the response level elements
         * 
         * @param localName
         */
        private void handleResponseElement(String localName, String cleanChars) {

            if (localName.equalsIgnoreCase("Status")) {
                reply.mwsStatus = cleanChars;
            }
        }

    }
}
