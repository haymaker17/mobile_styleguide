package com.concur.mobile.gov.travel.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.travel.data.TANumberListRow;

public class AuthNumsReply extends ServiceReply {

    public List<TANumberListRow> taNumberList;

    public String xmlReply;

    public Calendar lastRefreshTime;

    public AuthNumsReply() {
        // TODO Auto-generated constructor stub
    }

    public static AuthNumsReply parseXml(String responseXml) {

        AuthNumsReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            TANumberSAXParserHandler handler = new TANumberSAXParserHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class TANumberSAXParserHandler extends DefaultHandler {

        // list of tags
        private static final String LIST = "TANumberList";
        private static final String ROW = "TANumberListRow";

        // parsing character
        private StringBuilder chars;

        // // flag to track our place in xml hierarchy.
        private boolean inList;
        private boolean inRow;

        // data holders
        private AuthNumsReply reply;
        private TANumberListRow listItem;

        protected AuthNumsReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new AuthNumsReply();

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
            if (localName.equalsIgnoreCase(LIST)) {
                inList = true;
                reply.taNumberList = new ArrayList<TANumberListRow>();
            } else if (localName.equalsIgnoreCase(ROW)) {
                inRow = true;
                listItem = new TANumberListRow();
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();

            if (inRow) {
                if (localName.equalsIgnoreCase(ROW)) {
                    reply.taNumberList.add(listItem);
                    inRow = false;
                } else {
                    listItem.handleElement(localName, cleanChars);
                }
            } else if (inList) {
                if (localName.equalsIgnoreCase(LIST)) {
                    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    reply.lastRefreshTime = now;
                }

            }
            chars.setLength(0);
        }

    }
}
