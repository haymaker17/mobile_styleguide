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
import com.concur.mobile.gov.travel.data.PerDiemListRow;

public class PerDiemLocationListReply extends ServiceReply {

    public List<PerDiemListRow> perDiemListRows;

    public String xmlReply;

    public PerDiemListRow defaultPerDiemLocation;

    public Calendar lastRefreshTime;

    public PerDiemLocationListReply() {
    }

    public static PerDiemLocationListReply parseXml(String responseXml) {

        PerDiemLocationListReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            PerDiemListRowSAXHandler handler = new PerDiemListRowSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class PerDiemListRowSAXHandler extends DefaultHandler {

        // list of tags
        private static final String LIST = "GetLocationsResponse";
        private static final String ROW = "GetLocationsResponseRow";

        // parsing character
        private StringBuilder chars;

        // // flag to track our place in xml hierarchy.
        private boolean inList;
        private boolean inRow;

        // data holders
        private PerDiemLocationListReply reply;
        private PerDiemListRow listItem;

        protected PerDiemLocationListReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new PerDiemLocationListReply();

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
                reply.perDiemListRows = new ArrayList<PerDiemListRow>();
            } else if (localName.equalsIgnoreCase(ROW)) {
                inRow = true;
                listItem = new PerDiemListRow();
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
                    Boolean isdefault = listItem.isDefault;
                    if (isdefault != null && isdefault == Boolean.TRUE) {
                        reply.setDefaultPerDiemLocation(listItem);
                    }
                    reply.perDiemListRows.add(listItem);
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

    public PerDiemListRow getDefaultPerDiemLocation() {
        return defaultPerDiemLocation;
    }

    public void setDefaultPerDiemLocation(PerDiemListRow defaultPerDiemLocation) {
        this.defaultPerDiemLocation = defaultPerDiemLocation;
    }
}
