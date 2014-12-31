/**
 * 
 */
package com.concur.mobile.core.travel.air.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ActionStatusServiceReply.ActionStatusSAXHandler;

/**
 * An extension of <code>ActionStatusServiceReply</code> for handling the response to an <code>AirCancelRequest</code>.
 */
public class AirCancelReply extends ActionStatusServiceReply {

    // private static final String CLS_TAG = AirCancelReply.class.getSimpleName();

    public static AirCancelReply parseXMLReply(String responseXml) {

        AirCancelReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new AirCancelSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (AirCancelReply) handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    protected static class AirCancelSAXHandler extends ActionStatusSAXHandler {

        private static final String AIR_CANCEL_RESPONSE = "AirCancelResponse";

        @Override
        protected ActionStatusServiceReply createReply() {
            return new AirCancelReply();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(AIR_CANCEL_RESPONSE)) {
                    reply = createReply();
                    elementHandled = true;
                }
            }
        }

    }

}
