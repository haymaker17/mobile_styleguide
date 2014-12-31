/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.stamp.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.doc.stamp.data.StampApproveResponseRow;

public class StampTMDocumentResponse extends ActionStatusServiceReply {

    static final String CLS_TAG = StampTMDocumentResponse.class.getSimpleName();
    public StampApproveResponseRow response;
    public String xmlReply;

    public StampTMDocumentResponse() {
        response = new StampApproveResponseRow();
    }

    public static StampTMDocumentResponse parseXmlResponse(String responseXml) {
        StampTMDocumentResponse reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            StampTMDocumentResponseSAXHandler handler = new StampTMDocumentResponseSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = (StampTMDocumentResponse) handler.getReply();
            reply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class StampTMDocumentResponseSAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = StampTMDocumentResponse.CLS_TAG + "."
            + StampTMDocumentResponseSAXHandler.class.getSimpleName();
        // list of tags
        private static final String APPROVE_RESPONSE = "ApproveResponse";
        private static final String APPROVE_RESPONSE_ROW = "ApproveResponseRow";
        // pointers
        private boolean inAppResponse;
        private boolean inAppResponseRow;
        // data holder
        private StampApproveResponseRow stampAprRes;

        @Override
        protected ActionStatusServiceReply createReply() {
            return new StampTMDocumentResponse();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            super.startElement(uri, localName, qName, attributes);
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(APPROVE_RESPONSE)) {
                    reply = createReply();
                    inAppResponse = true;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(APPROVE_RESPONSE_ROW)) {
                    inAppResponseRow = true;
                    stampAprRes = new StampApproveResponseRow();
                    elementHandled = true;
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (!elementHandled) {
                if (reply != null) {
                    if (reply instanceof StampTMDocumentResponse) {
                        if (inAppResponse) {
                            if (inAppResponseRow) {
                                if (localName.equalsIgnoreCase(APPROVE_RESPONSE_ROW)) {
                                    inAppResponseRow = false;
                                    ((StampTMDocumentResponse) reply).response = stampAprRes;
                                } else {
                                    stampAprRes.handleElement(localName, chars.toString().trim());
                                }
                            }
                            // end of xml
                            if (localName.equalsIgnoreCase(APPROVE_RESPONSE)) {
                                inAppResponse = false;
                            }
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
                }
            }

            chars.setLength(0);

        }
    }
}
