/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.stamp.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.doc.stamp.data.ReasonCodeReqdResponse;

public class StampRequirementInfoReply extends ServiceReply {

    public ReasonCodeReqdResponse reqdResponse;
    public String xmlReply;

    public static StampRequirementInfoReply parseXmlResponse(String responseXml) {
        StampRequirementInfoReply reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            StampRequirementInfoReplySAXHandler handler = new StampRequirementInfoReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class StampRequirementInfoReplySAXHandler extends DefaultHandler {

        // list of tags
        private static final String GET_REASON_RESPONSE = "getReasonCodeReqdResponse";
        private static final String GET_REASON_RESPONSE_ROW = "getReasonCodeReqdResponseRow";
        // parsing character
        private StringBuilder chars;
        // // flag to track our place in xml hierarchy.
        private boolean inReasonResponse;
        private boolean inResponseRow;
        // data holders
        private StampRequirementInfoReply reply;
        private ReasonCodeReqdResponse response;

        protected StampRequirementInfoReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            chars = new StringBuilder();
            reply = new StampRequirementInfoReply();
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
            if (localName.equalsIgnoreCase(GET_REASON_RESPONSE)) {
                inReasonResponse = true;
            } else if (localName.equalsIgnoreCase(GET_REASON_RESPONSE_ROW)) {
                inResponseRow = true;
                response = new ReasonCodeReqdResponse();
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            final String cleanChars = chars.toString().trim();
            if (inReasonResponse) {
                if (inResponseRow) {
                    if (localName.equalsIgnoreCase(GET_REASON_RESPONSE_ROW)) {
                        inResponseRow = false;
                    } else {
                        response.handleElement(localName, cleanChars);
                    }
                }
                // end of xml
                if (localName.equalsIgnoreCase(GET_REASON_RESPONSE)) {
                    inReasonResponse = false;
                    reply.reqdResponse = response;
                }
            }
            chars.setLength(0);
        }
    }
}
