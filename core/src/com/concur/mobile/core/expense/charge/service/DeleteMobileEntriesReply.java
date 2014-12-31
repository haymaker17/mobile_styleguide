/**
 * 
 */
package com.concur.mobile.core.expense.charge.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Intent;
import android.util.Log;

import com.concur.mobile.core.expense.service.KeyedServiceReply;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> for reading a delete mobile entries response from the server.
 * 
 * @author AndrewK
 */
public class DeleteMobileEntriesReply extends ServiceReply {

    private static final String CLS_TAG = DeleteMobileEntriesReply.class.getSimpleName();

    public ArrayList<KeyedServiceReply> meKeys;

    /**
     * Will encode into <code>intent</code> the list of mobile entry key responses.
     * 
     * @param intent
     *            the intent object to receive the encoded responses.
     */
    public void encodeMobileEntryKeyResponses(Intent intent) {
        int statusCount = (meKeys != null) ? meKeys.size() : 0;
        intent.putExtra(Const.REPLY_STATUS_COUNT, Integer.toString(statusCount));
        StringBuilder strBldr = new StringBuilder();
        for (int statusInd = 0; statusInd < statusCount; ++statusInd) {
            KeyedServiceReply keyedSrvReply = meKeys.get(statusInd);
            strBldr.append(Const.REPLY_STATUS_ENTRY_KEY);
            strBldr.append('.');
            strBldr.append(statusInd);
            intent.putExtra(strBldr.toString(), keyedSrvReply.key);
            strBldr.setLength(0);
            strBldr.append(Const.REPLY_STATUS);
            strBldr.append('.');
            strBldr.append(statusInd);
            intent.putExtra(strBldr.toString(), keyedSrvReply.mwsStatus);
            strBldr.setLength(0);
            if (!keyedSrvReply.mwsStatus.equalsIgnoreCase(Const.STATUS_SUCCESS)) {
                strBldr.append(Const.REPLY_ERROR_MESSAGE);
                strBldr.append('.');
                strBldr.append(statusInd);
                intent.putExtra(strBldr.toString(), keyedSrvReply.mwsErrorMessage);
                strBldr.setLength(0);
            }
        }
    }

    /**
     * Will decode the list of encoded mobile entry key responses from <code>intent</code>.
     * 
     * @param intent
     *            the intent containing zero or more encoded mobile entry key responses.
     * @return a list of <code>KeyedServiceReply</code> objects.
     */
    public static List<KeyedServiceReply> decodeMobileEntryKeyResponses(Intent intent) {
        List<KeyedServiceReply> keyResponses = null;
        String statusCountStr = intent.getStringExtra(Const.REPLY_STATUS_COUNT);
        if (statusCountStr != null && statusCountStr.length() > 0) {
            try {
                int statusCount = Integer.parseInt(statusCountStr);
                StringBuilder strBldr = new StringBuilder();
                for (int statusInd = 0; statusInd < statusCount; ++statusInd) {
                    KeyedServiceReply keySrvReply = new KeyedServiceReply();
                    strBldr.append(Const.REPLY_STATUS_ENTRY_KEY);
                    strBldr.append('.');
                    strBldr.append(statusInd);
                    String statusKey = intent.getStringExtra(strBldr.toString());
                    strBldr.setLength(0);
                    keySrvReply.key = statusKey;
                    strBldr.append(Const.REPLY_STATUS);
                    strBldr.append('.');
                    strBldr.append(statusInd);
                    keySrvReply.mwsStatus = intent.getStringExtra(strBldr.toString());
                    strBldr.setLength(0);
                    if (keySrvReply.mwsStatus != null && !keySrvReply.mwsStatus.equalsIgnoreCase(Const.STATUS_SUCCESS)) {
                        strBldr.append(Const.REPLY_ERROR_MESSAGE);
                        strBldr.append('.');
                        strBldr.append(statusInd);
                        keySrvReply.mwsErrorMessage = intent.getStringExtra(strBldr.toString());
                        strBldr.setLength(0);
                    }
                    if (keyResponses == null) {
                        keyResponses = new ArrayList<KeyedServiceReply>();
                    }
                    keyResponses.add(keySrvReply);
                }
            } catch (NumberFormatException numFormExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".decodeMobileEntryKeyResponses: invalid status count value of '"
                        + statusCountStr + "'.", numFormExc);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".decodeMobileEntryKeyResponses: null or empty status count value!");
        }
        return keyResponses;
    }

    /**
     * Will parse the XML representation of a delete mobile entries reply.
     * 
     * @param responseXml
     *            the XML representation of the reply.
     * 
     * @return an instance of <code>SaveMobileEntryReply</code> containing the service reply.
     */
    public static DeleteMobileEntriesReply parseXMLReply(String responseXml) {

        DeleteMobileEntriesReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            DeleteMobileEntriesReplySAXHandler handler = new DeleteMobileEntriesReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = new DeleteMobileEntriesReply();
            srvReply.meKeys = handler.getReplies();
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
    protected static class DeleteMobileEntriesReplySAXHandler extends DefaultHandler {

        private static final String CLS_TAG = DeleteMobileEntriesReply.CLS_TAG + "."
                + DeleteMobileEntriesReplySAXHandler.class.getSimpleName();

        private static final String ACTION_STATUS_ARRAY = "ArrayOfActionStatus";
        private static final String ACTION_STATUS = "ActionStatus";
        private static final String ERROR_MESSAGE = "ErrorMessage";
        private static final String STATUS = "Status";
        private static final String ME_KEY = "MeKey";

        // The reply being parsed.
        private KeyedServiceReply reply;

        // The list of replies.
        private ArrayList<KeyedServiceReply> replies;

        private StringBuilder chars;

        /**
         * Gets the list of built up replies.
         * 
         * @return the list of replies.
         */
        ArrayList<KeyedServiceReply> getReplies() {
            return replies;
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

            if (localName.equalsIgnoreCase(ACTION_STATUS_ARRAY)) {
                replies = new ArrayList<KeyedServiceReply>();
            } else if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                reply = new KeyedServiceReply();
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
                    reply.key = chars.toString().trim();
                } else if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                    replies.add(reply);
                    reply = null;
                } else if (localName.equalsIgnoreCase(ACTION_STATUS_ARRAY)) {
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
