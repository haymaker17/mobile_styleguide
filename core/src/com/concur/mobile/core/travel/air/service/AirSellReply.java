/**
 * 
 */
package com.concur.mobile.core.travel.air.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ActionStatusServiceReply.ActionStatusSAXHandler;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ActionStatusServiceReply</code> for handling a AirSellRequest response.
 */
public class AirSellReply extends ActionStatusServiceReply {

    private static final String CLS_TAG = AirSellReply.class.getSimpleName();

    public String itinLocator;

    public static AirSellReply parseXMLReply(String responseXml) {

        AirSellReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new AirSellSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (AirSellReply) handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    protected static class AirSellSAXHandler extends ActionStatusSAXHandler {

        private static final String AIR_SELL_RESPONSE = "AirSellResponse";
        private static final String ITIN_LOCATOR = "ItinLocator";

        @Override
        protected ActionStatusServiceReply createReply() {
            return new AirSellReply();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(AIR_SELL_RESPONSE)) {
                    reply = createReply();
                    elementHandled = true;
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#endElement(java.lang.String,
         * java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (!elementHandled) {
                if (reply != null) {
                    if (localName.equalsIgnoreCase(ITIN_LOCATOR)) {
                        if (reply instanceof AirSellReply) {
                            ((AirSellReply) reply).itinLocator = chars.toString().trim();
                            elementHandled = true;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is not instance of AirSellReply!");
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
                }
            }
            chars.setLength(0);// need to be cleaned up otherwise the 'record locator' data is being prefixed to the 'status'
                               // element data
        }

    }

}
