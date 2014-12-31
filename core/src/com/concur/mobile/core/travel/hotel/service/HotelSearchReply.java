/**
 * 
 */
package com.concur.mobile.core.travel.hotel.service;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.data.HotelBenchmark;
import com.concur.mobile.core.travel.data.TravelPointsBank;
import com.concur.mobile.core.travel.hotel.data.HotelChoice;
import com.concur.mobile.core.travel.hotel.data.HotelChoice.HotelChoiceSAXHandler;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>ServiceReply</code> for containing the results of a hotel search.
 * 
 * @author AndrewK
 */
public class HotelSearchReply extends ServiceReply implements Serializable {

    private static final long serialVersionUID = 21436786187895595L;

    private static final String CLS_TAG = HotelSearchReply.class.getSimpleName();

    public ArrayList<HotelChoice> hotelChoices;

    // Contains the start index of this result set into the entire list of cached results on the server.
    public Integer startIndex;

    // Contains the total count of cached results on the server.
    public Integer totalCount;

    // Contains the number of results returned in this reply.
    public Integer length;

    // hotel list has a recommendation - to be used to identify the default sorting of hotel search list
    public boolean hasRecommendation;

    public String pollingId;

    public boolean isFinal;

    // will be set by GetHotels Async Task
    public ArrayList<HotelBenchmark> hotelBenchmarks;
    public TravelPointsBank travelPointsBank;

    /**
     * Parses a hotel search response body XML and returns a response object.
     * 
     * @param responseXml
     *            the response body XML.
     * @return the response object.
     */
    public static HotelSearchReply parseXMLReply(String responseXml) {

        HotelSearchReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            HotelSearchReplySAXHandler handler = new HotelSearchReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    /**
     * Will parse from <code>reader</code> a hotel search response object.
     * 
     * @param reader
     *            the reader providing the content.
     * @return an instance of <code>HotelSearchReply</code>.
     */
    public static HotelSearchReply parseXmlReply(Reader reader) {
        HotelSearchReply reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            HotelSearchReplySAXHandler handler = new HotelSearchReplySAXHandler();
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

    protected static class HotelSearchReplySAXHandler extends DefaultHandler {

        private final String CLS_TAG = HotelSearchReply.CLS_TAG + "."
                + HotelSearchReplySAXHandler.class.getSimpleName();

        private static final String CHOICES = "Choices";
        private static final String HOTEL_CHOICE = "HotelChoice";
        private static final String HOTEL_CHOICE_PAGE = "HotelChoicePage";
        private static final String LENGTH = "Length";
        private static final String START_INDEX = "StartIndex";
        private static final String TOTAL_COUNT = "TotalCount";
        private static final String POLLING_ID = "PollingId";
        private static final String FINAL = "IsFinal";

        // The reply currently being built.
        private HotelSearchReply reply;

        // The hotel choice parser.
        private HotelChoiceSAXHandler hotelChoiceHandler;

        private StringBuilder chars;

        /**
         * Gets the built search reply.
         * 
         * @return the search reply.
         */
        protected HotelSearchReply getReply() {
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

            reply = new HotelSearchReply();
            chars = new StringBuilder();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if (hotelChoiceHandler != null) {
                hotelChoiceHandler.characters(ch, start, length);
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
            if (hotelChoiceHandler != null) {
                hotelChoiceHandler.startElement(uri, localName, qName, attributes);
            } else {
                if (localName.equalsIgnoreCase(CHOICES)) {
                    reply.hotelChoices = new ArrayList<HotelChoice>();
                } else if (localName.equalsIgnoreCase(HOTEL_CHOICE)) {
                    hotelChoiceHandler = new HotelChoiceSAXHandler();
                }
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName.equalsIgnoreCase(HOTEL_CHOICE)) {
                if (hotelChoiceHandler != null) {
                    HotelChoice hotelChoice = hotelChoiceHandler.getHotelChoice();
                    reply.hotelChoices.add(hotelChoice);
                    // if at least one hotel had recommendation then we do not care about others.
                    if (!reply.hasRecommendation) {
                        reply.hasRecommendation = hotelChoiceHandler.hasRecommendation;
                    }
                    hotelChoiceHandler = null;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: hotel choice handler is null!");
                }
            } else if (localName.equalsIgnoreCase(START_INDEX)) {
                String cleanChars = chars.toString().trim();
                reply.startIndex = Parse.safeParseInteger(cleanChars);
                if (reply.startIndex == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: invalid start index value '" + cleanChars + "'.");
                }
            } else if (localName.equalsIgnoreCase(TOTAL_COUNT)) {
                String cleanChars = chars.toString().trim();
                reply.totalCount = Parse.safeParseInteger(cleanChars);
                if (reply.totalCount == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: invalid total count value '" + cleanChars + "'.");
                }
            } else if (localName.equalsIgnoreCase(LENGTH)) {
                String cleanChars = chars.toString().trim();
                reply.length = Parse.safeParseInteger(cleanChars);
                if (reply.length == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: invalid length value '" + cleanChars + "'.");
                }
            } else if (localName.equalsIgnoreCase(POLLING_ID)) {
                reply.pollingId = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(FINAL)) {
                String cleanChars = chars.toString().trim();
                reply.isFinal = Parse.safeParseBoolean(cleanChars);
            } else if (localName.equalsIgnoreCase(CHOICES)) {
                // No-op.
            } else if (localName.equalsIgnoreCase(HOTEL_CHOICE_PAGE)) {
                // No-op.
            } else {
                if (hotelChoiceHandler != null) {
                    hotelChoiceHandler.endElement(uri, localName, qName);
                } else if (localName.equalsIgnoreCase("Status")) {
                    reply.mwsStatus = chars.toString().trim();
                } else if (localName.equalsIgnoreCase("ErrorMessage")) {
                    reply.mwsErrorMessage = chars.toString().trim();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML tag '" + localName + "' with value '"
                            + chars.toString() + "'.");
                }
            }
            chars.setLength(0);
        }
    }

}
