package com.concur.mobile.core.travel.car.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ActionStatusServiceReply.ActionStatusSAXHandler;

public class CancelCarReply extends ActionStatusServiceReply {

    public static CancelCarReply parseReply(String responseXml) {

        CancelCarReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new CancelCarSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (CancelCarReply) handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    protected static class CancelCarSAXHandler extends ActionStatusSAXHandler {

        private static final String CAR_CANCEL = "CarCancelResponse";

        @Override
        protected ActionStatusServiceReply createReply() {
            return new CancelCarReply();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(CAR_CANCEL)) {
                    reply = createReply();
                    elementHandled = true;
                }
            }
        }

    }
}
