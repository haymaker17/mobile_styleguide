/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ActionStatusServiceReply</code> to parse a response to the <code>GetReceiptImageUrlRequest</code>.
 * 
 * @author AndrewK
 */
public class GetReceiptImageUrlReply extends ActionStatusServiceReply {

    private static final String CLS_TAG = GetReceiptImageUrlReply.class.getSimpleName();

    /**
     * Contains the parsed receipt image URL.
     */
    public String receiptImageUrl;

    public static GetReceiptImageUrlReply parseReply(String responseXml) {

        GetReceiptImageUrlReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new GetReceiptImageUrlSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (GetReceiptImageUrlReply) handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    protected static class GetReceiptImageUrlSAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = GetReceiptImageUrlReply.CLS_TAG
                + GetReceiptImageUrlSAXHandler.class.getSimpleName();

        private static final String RECEIPT_IMAGE_URL = "ReceiptImageUrl";

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#createReply()
         */
        @Override
        protected ActionStatusServiceReply createReply() {
            return new GetReceiptImageUrlReply();
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
                    if (localName.equalsIgnoreCase(RECEIPT_IMAGE_URL)) {
                        ((GetReceiptImageUrlReply) reply).receiptImageUrl = chars.toString().trim();
                        elementHandled = true;
                    } else if (!elementHandled && this.getClass().equals(GetReceiptImageUrlSAXHandler.class)) {
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
