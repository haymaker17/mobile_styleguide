/**
 * 
 */
package com.concur.mobile.core.travel.hotel.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> for handling the result of confirming a hotel room.
 * 
 * @author AndrewK
 */
public class HotelConfirmReply extends ServiceReply {

    // private static final String CLS_TAG = HotelConfirmReply.class.getSimpleName();

    public String itinLocator;

    /**
     * Parses a hotel confirm response body XML and returns a response object.
     * 
     * @param responseXml
     *            the response body XML.
     * @return the response object.
     */
    public static HotelConfirmReply parseXMLReply(String responseXml) {

        HotelConfirmReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            HotelConfirmReplySAXHandler handler = new HotelConfirmReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class HotelConfirmReplySAXHandler extends DefaultHandler {

        // private final String CLS_TAG = HotelConfirmReply.CLS_TAG + "." + HotelConfirmReplySAXHandler.class.getSimpleName();

        private static final String SELL_RESPONSE = "SellResponse";
        private static final String ITIN_LOCATOR = "ItinLocator";
        private static final String STATUS = "Status";
        private static final String ERROR_MESSAGE = "ErrorMessage";

        // The reply currently being built.
        private HotelConfirmReply reply;

        private StringBuilder chars;

        /**
         * Gets the built search reply.
         * 
         * @return the search reply.
         */
        protected HotelConfirmReply getReply() {
            return reply;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startDocument()
         */
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            reply = new HotelConfirmReply();
            chars = new StringBuilder();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName.equalsIgnoreCase(ERROR_MESSAGE)) {
                reply.mwsErrorMessage = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(ITIN_LOCATOR)) {
                reply.itinLocator = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(STATUS)) {
                reply.mwsStatus = chars.toString().trim();
                if (reply.mwsStatus.toLowerCase().startsWith("fail")) {
                    reply.mwsStatus = Const.REPLY_STATUS_FAILURE;
                }
            } else if (localName.equalsIgnoreCase(SELL_RESPONSE)) {
                // No-op.
            }
            chars.setLength(0);
        }
    }

}
