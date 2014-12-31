/**
 * 
 */
package com.concur.mobile.core.travel.hotel.service;

import java.io.ByteArrayInputStream;
import java.io.Reader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.hotel.data.HotelChoiceDetail;
import com.concur.mobile.core.travel.hotel.data.HotelChoiceDetail.HotelChoiceDetailSAXHandler;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> for parsing hotel detail information.
 * 
 * @author AndrewK
 */
public class HotelDetailReply extends ServiceReply {

    private static final String CLS_TAG = HotelDetailReply.class.getSimpleName();

    public HotelChoiceDetail hotelChoiceDetail;

    /**
     * Parses a hotel detail response body XML and returns a response object.
     * 
     * @param responseXml
     *            the response body XML.
     * @return the response object.
     */
    public static HotelDetailReply parseXMLReply(String responseXml) {

        HotelDetailReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            HotelDetailReplySAXHandler handler = new HotelDetailReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    /**
     * Will parse from <code>reader</code> a hotel detail response object.
     * 
     * @param reader
     *            the reader providing the content.
     * @return an instance of <code>HotelDetailReply</code>.
     */
    public static HotelDetailReply parseXmlReply(Reader reader) {
        HotelDetailReply reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            HotelDetailReplySAXHandler handler = new HotelDetailReplySAXHandler();
            parser.parse(new InputSource(reader), handler);
            reply = handler.getReply();
            // If no status has been parsed, default to success.
            if (reply.mwsStatus == null || reply.mwsStatus.length() == 0) {
                reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
                Log.w(Const.LOG_TAG, CLS_TAG + ".parseXmlReply: defaulting status to success!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    /**
     * An extension of <code>DefaultHandler</code> to parse the response to a request to obtain detailed hotel information.
     * 
     * @author AndrewK
     */
    protected static class HotelDetailReplySAXHandler extends DefaultHandler {

        private final String CLS_TAG = HotelDetailReply.CLS_TAG + "."
                + HotelDetailReplySAXHandler.class.getSimpleName();

        private static final String HOTEL_DETAILS = "HotelDetails";

        // The reply currently being built.
        private HotelDetailReply reply;

        // The hotel choice parser.
        private HotelChoiceDetailSAXHandler hotelChoiceDetailHandler;

        private StringBuilder chars;

        /**
         * Gets the built search reply.
         * 
         * @return the search reply.
         */
        protected HotelDetailReply getReply() {
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

            reply = new HotelDetailReply();
            chars = new StringBuilder();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if (hotelChoiceDetailHandler != null) {
                hotelChoiceDetailHandler.characters(ch, start, length);
            } else {
                chars.append(ch, start, length);
            }
        }

        /**
         * Handle the opening of all elements. Create data objects as needed for use in endElement().
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (hotelChoiceDetailHandler != null) {
                hotelChoiceDetailHandler.startElement(uri, localName, qName, attributes);
            } else if (localName.equalsIgnoreCase(HOTEL_DETAILS)) {
                hotelChoiceDetailHandler = new HotelChoiceDetailSAXHandler();
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName.equalsIgnoreCase(HOTEL_DETAILS)) {
                if (hotelChoiceDetailHandler != null) {
                    reply.hotelChoiceDetail = hotelChoiceDetailHandler.getHotelChoiceDetail();
                    hotelChoiceDetailHandler = null;
                    if (reply.hotelChoiceDetail == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: hotel choice detail parser returned null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: hotel choice detail handler is null!");
                }
            } else if (hotelChoiceDetailHandler != null) {
                hotelChoiceDetailHandler.endElement(uri, localName, qName);
            } else if (localName.equalsIgnoreCase("Status")) {
                reply.mwsStatus = chars.toString().trim();
            } else if (this.getClass().equals(HotelDetailReplySAXHandler.class)) {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML tag '" + localName + "' with value '" +
                // chars.toString() + "'.");
            }
            if (this.getClass().equals(HotelDetailReplySAXHandler.class)) {
                chars.setLength(0);
            }
        }
    }

}
