/**
 * Alternative flight schedule reply will parse and handle in this class
 * 
 * @author sunill
 * */
package com.concur.mobile.core.travel.rail.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

public class CancelRailReply extends ActionStatusServiceReply {

    private static final String CLS_TAG = CancelRailReply.class.getSimpleName();

    public boolean isTripCancel;

    public static CancelRailReply parseReply(String responseXml) {

        CancelRailReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new CancelRailReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (CancelRailReply) handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    protected static class CancelRailReplySAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = CancelRailReply.CLS_TAG + "."
                + CancelRailReplySAXHandler.class.getSimpleName();

        private static final String RAIL_CANCEL = "TravelCancelResponse";
        private static final String TRIP_CANCEL = "EntireTripCancelled";

        @Override
        protected ActionStatusServiceReply createReply() {
            return new CancelRailReply();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(RAIL_CANCEL)) {
                    reply = createReply();
                    elementHandled = true;
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (!elementHandled) {
                if (reply != null) {
                    if (localName.equalsIgnoreCase(TRIP_CANCEL)) {
                        ((CancelRailReply) reply).isTripCancel = Parse.safeParseBoolean(chars.toString().trim());
                        elementHandled = true;
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
                }
            }
            chars.setLength(0);
        }
    }
}
