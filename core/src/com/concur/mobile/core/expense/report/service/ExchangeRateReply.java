/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ActionStatusServiceReply.ActionStatusSAXHandler;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ActionStatusServiceReply</code> for handling the response to an ExchangeRateRequest.
 * 
 * @author andy
 */
public class ExchangeRateReply extends ActionStatusServiceReply {

    private static final String CLS_TAG = ExchangeRateReply.class.getSimpleName();

    // Contains the parsed exchange rate.
    public String exchangeRate;

    public static ExchangeRateReply parseReply(String responseXml) {

        ExchangeRateReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new ExchangeRateSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (ExchangeRateReply) handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    protected static class ExchangeRateSAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = ExchangeRateReply.CLS_TAG + ExchangeRateSAXHandler.class.getSimpleName();

        private static final String EXCHANGE_RATE = "ExchangeRate";

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#createReply()
         */
        @Override
        protected ActionStatusServiceReply createReply() {
            return new ExchangeRateReply();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#endElement(java.lang.String,
         * java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            elementHandled = false;
            super.endElement(uri, localName, qName);
            if (reply != null) {
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(EXCHANGE_RATE)) {
                        ((ExchangeRateReply) reply).exchangeRate = chars.toString().trim();
                        elementHandled = true;
                    } else if (!elementHandled && this.getClass().equals(ExchangeRateSAXHandler.class)) {
                        // Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '"
                        // + localName + "'.");
                        // Set the collected chars length to '0' since no sub-class of this is performing
                        // parsing and we don't recognize the tag.
                        chars.setLength(0);
                    }
                    // If this class did handle the tag, then clear the characters.
                    if (elementHandled) {
                        chars.setLength(0);
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
            }
        }
    }

}
