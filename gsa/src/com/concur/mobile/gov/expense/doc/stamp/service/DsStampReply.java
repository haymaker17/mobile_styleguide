/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.stamp.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.doc.stamp.data.MttDocument;
import com.concur.mobile.gov.expense.doc.stamp.data.MttReturnTo;
import com.concur.mobile.gov.expense.doc.stamp.data.MttStamps;

public class DsStampReply extends ServiceReply {

    public MttDocument document;
    public List<MttStamps> stamps;
    public List<MttReturnTo> returnTos;

    public DsStampReply() {
        document = new MttDocument();
        stamps = new ArrayList<MttStamps>();
        returnTos = new ArrayList<MttReturnTo>();
    }

    public static DsStampReply parseXMLReply(String responseXml) {
        DsStampReply reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            TMDocAvailableStampsReplySAXHandler handler = new TMDocAvailableStampsReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class TMDocAvailableStampsReplySAXHandler extends DefaultHandler {

        // list of tags
        private static final String DS_STAMP = "dsStamps";
        private static final String MTT_DOCUMENT = "Mtt-document";
        private static final String MTT_STAMPS = "Mtt-stamps";
        private static final String MTT_RETURNTO = "Mtt-returnto";
        // parsing character
        private StringBuilder chars;
        // flag to track our place in xml hierarchy.
        private boolean inDsStamps, inMttDoc, inMttStamp, inMttReturnTo;
        // data holders
        private DsStampReply reply;
        private MttDocument mttDocument;
        private MttStamps mttStamps;
        private MttReturnTo mttReturnTo;

        protected DsStampReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            chars = new StringBuilder();
            reply = new DsStampReply();
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
            if (localName.equalsIgnoreCase(DS_STAMP)) {
                inDsStamps = true;
            } else if (localName.equalsIgnoreCase(MTT_DOCUMENT)) {
                mttDocument = new MttDocument();
                inMttDoc = true;
            } else if (localName.equalsIgnoreCase(MTT_STAMPS)) {
                mttStamps = new MttStamps();
                inMttStamp = true;
            } else if (localName.equalsIgnoreCase(MTT_RETURNTO)) {
                inMttReturnTo = true;
                mttReturnTo = new MttReturnTo();
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            final String cleanChars = chars.toString().trim();
            if (inDsStamps) {
                if (inMttDoc) {
                    if (localName.equalsIgnoreCase(MTT_DOCUMENT)) {
                        inMttDoc = false;
                        reply.document = mttDocument;
                    } else {
                        mttDocument.handleElement(localName, cleanChars);
                    }
                } else if (inMttStamp) {
                    if (localName.equalsIgnoreCase(MTT_STAMPS)) {
                        inMttStamp = false;
                        reply.stamps.add(mttStamps);
                    } else {
                        mttStamps.handleElement(localName, cleanChars);
                    }
                } else if (inMttReturnTo) {
                    if (localName.equalsIgnoreCase(MTT_RETURNTO)) {
                        inMttReturnTo = false;
                        reply.returnTos.add(mttReturnTo);
                    } else {
                        mttReturnTo.handleElement(localName, cleanChars);
                    }
                }
                // end of xml
                if (localName.equalsIgnoreCase(DS_STAMP)) {
                    inDsStamps = false;
                }
            }
            chars.setLength(0);
        }
    }
}
