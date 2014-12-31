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
 * An extension of <code>ServiceReply</code> for handling responses to hide card charges.
 * 
 * @author AndrewK
 */
public class CardChargeHideReply extends ServiceReply {

    private static final String CLS_TAG = CardChargeHideReply.class.getSimpleName();

    // Contains a success/failure per each card transaction key in the response.
    ArrayList<KeyedServiceReply> ctKeys;

    /**
     * Will encode into <code>intent</code> the list of card charge key responses.
     * 
     * @param intent
     *            the intent object to receive the encoded responses.
     */
    public void encodeCardTransactionKeyResponses(Intent intent) {
        int statusCount = (ctKeys != null) ? ctKeys.size() : 0;
        intent.putExtra(Const.REPLY_STATUS_COUNT, Integer.toString(statusCount));
        StringBuilder strBldr = new StringBuilder();
        for (int statusInd = 0; statusInd < statusCount; ++statusInd) {
            KeyedServiceReply keyedSrvReply = ctKeys.get(statusInd);
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
     * Will decode the list of encoded card charge key responses from <code>intent</code>.
     * 
     * @param intent
     *            the intent containing zero or more encoded card charge key responses.
     * @return a list of <code>KeyedServiceReply</code> objects.
     */
    public static List<KeyedServiceReply> decodeCardTransactionKeyResponses(Intent intent) {
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
                Log.e(Const.LOG_TAG, CLS_TAG + ".decodeCardTransactionKeyResponses: invalid status count value of '"
                        + statusCountStr + "'.", numFormExc);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".decodeCardTransactionKeyResponses: null or empty status count value!");
        }
        return keyResponses;
    }

    /**
     * Parses an XML representation of an instance of <code>CardChargeReply</code> and populates <code>reply</code>.
     * 
     * @param reply
     * @param responseXml
     * @return
     */
    public static CardChargeHideReply parseXMLReply(CardChargeHideReply reply, String responseXml) {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            CardChargeHideReplySAXHandler handler = new CardChargeHideReplySAXHandler(reply);
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    /**
     * An extension of <code>DefaultHandler</code> used to parse the response of hiding either personal or corporate card charges,
     * but not both concurrently.
     * 
     * @author AndrewK
     */
    protected static class CardChargeHideReplySAXHandler extends DefaultHandler {

        private static final String CLS_TAG = CardChargeHideReply.CLS_TAG + "."
                + CardChargeHideReplySAXHandler.class.getSimpleName();

        private static final String ACTION_STATUS = "ActionStatus";
        private static final String ERROR_MESSAGE = "ErrorMessage";
        private static final String STATUS = "Status";
        private static final String CC_TRANSACTIONS = "CcTransactions";
        private static final String PC_TRANSACTIONS = "PcTransactions";
        private static final String PCT_KEY = "PctKey";
        private static final String CCT_Key = "CctKey";

        /**
         * Constructs an instance of <code>CardChargeHideReplySAXHandler</code> with a reply object to be filled in.
         * 
         * @param reply
         *            the reply object.
         */
        CardChargeHideReplySAXHandler(CardChargeHideReply reply) {
            super();
            this.reply = reply;
        }

        // The main reply
        private CardChargeHideReply reply;

        // The list of sub-replies being parsed
        private ArrayList<KeyedServiceReply> subReplies;

        // The sub-reply being parsed.
        private KeyedServiceReply keyReply;

        private StringBuilder chars;

        CardChargeHideReply getReply() {
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
                if (subReplies != null) {
                    // A sub-element status
                    keyReply = new KeyedServiceReply();
                }
            } else if (localName.equalsIgnoreCase(PC_TRANSACTIONS) || localName.equalsIgnoreCase(CC_TRANSACTIONS)) {
                subReplies = new ArrayList<KeyedServiceReply>();
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
                if (localName.equalsIgnoreCase(CC_TRANSACTIONS) || localName.equalsIgnoreCase(PC_TRANSACTIONS)) {
                    reply.ctKeys = subReplies;
                    subReplies = null;
                } else if (localName.equalsIgnoreCase(ERROR_MESSAGE)) {
                    String errorMsg = chars.toString().trim();
                    if (keyReply != null) {
                        keyReply.mwsErrorMessage = errorMsg;
                    } else {
                        reply.mwsErrorMessage = errorMsg;
                    }
                    reply.mwsErrorMessage = chars.toString().trim();
                } else if (localName.equalsIgnoreCase(STATUS)) {
                    String statusMsg = chars.toString().trim();
                    if (keyReply != null) {
                        // Sub-reply
                        keyReply.mwsStatus = statusMsg;
                    } else {
                        reply.mwsStatus = statusMsg;
                    }
                } else if (localName.equalsIgnoreCase(PCT_KEY) || localName.equalsIgnoreCase(CCT_Key)) {
                    keyReply.key = chars.toString().trim();
                } else if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                    if (keyReply != null) {
                        // Ending a sub-reply
                        subReplies.add(keyReply);
                        keyReply = null;
                    } else {
                        // Ending the big reply
                        // Nothing to do.
                    }
                } else {
                    // Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML node -- '" + localName + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply reference is null!");
            }

            chars.setLength(0);
        }
    }

}
