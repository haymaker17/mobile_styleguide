/**
 * 
 */
package com.concur.mobile.core.expense.charge.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> for handling save mobile entry requests.
 * 
 * @author AndrewK
 */
public class SaveMobileEntryReply extends ServiceReply {

    private static final String CLS_TAG = SaveMobileEntryReply.class.getSimpleName();

    /**
     * Contains the mobile entry key.
     */
    public String mobileEntryKey;

    /**
     * Will parse the XML representation of a save mobile entry reply.
     * 
     * @param responseXml
     *            the XML representation of the reply.
     * 
     * @return an instance of <code>SaveMobileEntryReply</code> containing the service reply.
     */
    public static SaveMobileEntryReply parseXMLReply(String responseXml) {

        SaveMobileEntryReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            SaveMobileEntryReplySAXHandler handler = new SaveMobileEntryReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    /**
     * An extension of <code>DefaultHandler</code> to handle parsing a service reply.
     * 
     * @author AndrewK
     */
    protected static class SaveMobileEntryReplySAXHandler extends DefaultHandler {

        private static final String CLS_TAG = SaveMobileEntryReply.CLS_TAG + "."
                + SaveMobileEntryReplySAXHandler.class.getSimpleName();

        private static final String ACTION_STATUS = "ActionStatus";
        private static final String ERROR_MESSAGE = "ErrorMessage";
        private static final String STATUS = "Status";
        private static final String ME_KEY = "MeKey";

        // The reply being parsed.
        private SaveMobileEntryReply reply;

        private StringBuilder chars;

        SaveMobileEntryReply getReply() {
            return reply;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startDocument()
         */
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            chars = new StringBuilder();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                reply = new SaveMobileEntryReply();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (reply != null) {
                if (localName.equalsIgnoreCase(ERROR_MESSAGE)) {
                    reply.mwsErrorMessage = chars.toString().trim();
                } else if (localName.equalsIgnoreCase(STATUS)) {
                    reply.mwsStatus = chars.toString().trim();
                } else if (localName.equalsIgnoreCase(ME_KEY)) {
                    reply.mobileEntryKey = chars.toString().trim();
                } else if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                    // No-op.
                } else {
                    // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML node -- '" + localName + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply reference is null!");
            }
            chars.setLength(0);
        }
    }

}
