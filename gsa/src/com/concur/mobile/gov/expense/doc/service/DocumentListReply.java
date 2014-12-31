/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.doc.data.GovDocument;

public class DocumentListReply extends ActionStatusServiceReply {

    public List<GovDocument> documentList;
    public Calendar lastRefreshTime;
    public String xmlReply;

    public void setLastRefreshTime(Calendar lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }

    public static DocumentListReply parseXml(String responseXml) {

        DocumentListReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            DocumentListReplySAXHandler handler = new DocumentListReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = (DocumentListReply) handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class DocumentListReplySAXHandler extends ActionStatusSAXHandler {

        // list of tags
        private static final String DOC_LIST = "DocumentList";
        private static final String DOCUMENT = "Document";

        // parsing character
        // private StringBuilder chars;

        // // flag to track our place in xml hierarchy.
        private boolean inDocList;
        private boolean inDoc;

        // data holders
        // private DocumentListReply reply;
        private GovDocument document;

        protected ActionStatusServiceReply createReply() {
            return new DocumentListReply();
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
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(DOC_LIST)) {
                    inDocList = true;
                    ((DocumentListReply) reply).documentList = new ArrayList<GovDocument>();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(DOCUMENT)) {
                    inDoc = true;
                    document = new GovDocument();
                    elementHandled = true;
                }
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (!elementHandled) {
                if (reply != null && reply instanceof DocumentListReply) {
                    if (inDoc) {
                        if (localName.equalsIgnoreCase(DOCUMENT)) {
                            ((DocumentListReply) reply).documentList.add(document);
                            inDoc = false;
                        } else {
                            document.handleElement(localName, chars.toString().trim());
                        }
                    } else if (inDocList) {
                        if (localName.equalsIgnoreCase(DOC_LIST)) {
                            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            ((DocumentListReply) reply).lastRefreshTime = now;
                        }

                    }
                }
            }
            chars.setLength(0);
        }

    }
}
