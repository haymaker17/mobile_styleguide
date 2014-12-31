/**
 * 
 */
package com.concur.mobile.core.expense.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ActionStatusServiceReply</code> for the purposes of parsing a reply from a
 * <code>SaveReceiptRequest</code>.
 * 
 * @author AndrewK
 */
public class SaveReceiptReply extends ActionStatusServiceReply {

    private static final String CLS_TAG = SaveReceiptReply.class.getSimpleName();

    private static final String CONNECT_NAME_SPACE = "http://www.concursolutions.com/api/image/2011/02";

    public static final String OFFLINE_RECEIPT_ID = "offline";

    // Contains the parsed receipt image ID if the reply indicates a success.
    public String receiptImageId;

    public static SaveReceiptReply parseReply(String responseXml) {

        SaveReceiptReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new SaveReceiptSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (SaveReceiptReply) handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    /**
     * Will parse a response from the Concur Connect Imaging service.
     * 
     * @param inputStream
     *            contains the response input stream.
     * @param encoding
     *            contains the response encoding.
     * @return returns an instance of <code>SaveReceiptReply</code> containing the receipt image ID.
     */
    public static SaveReceiptReply parseConnectReply(InputStream inputStream, Encoding encoding) {
        final SaveReceiptReply srvReply = new SaveReceiptReply();
        if (inputStream != null && encoding != null) {

            // Set up the Root and Id elements.
            RootElement root = new RootElement(CONNECT_NAME_SPACE, "Image");
            Element idInfo = root.getChild(CONNECT_NAME_SPACE, "Id");
            idInfo.setEndTextElementListener(new EndTextElementListener() {

                public void end(String body) {
                    srvReply.receiptImageId = body.trim();
                }
            });
            try {
                Xml.parse(inputStream, encoding, root.getContentHandler());
                srvReply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
            } catch (Exception e) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".respnseXml - error parsing XML.", e);
                throw new RuntimeException(e);
            }
        }
        return srvReply;
    }

    protected static class SaveReceiptSAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = SaveReceiptReply.CLS_TAG + SaveReceiptSAXHandler.class.getSimpleName();

        private static final String RECEIPT_IMAGE_ID = "ReceiptImageId";

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#createReply()
         */
        @Override
        protected ActionStatusServiceReply createReply() {
            return new SaveReceiptReply();
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
                    if (localName.equalsIgnoreCase(RECEIPT_IMAGE_ID)) {
                        ((SaveReceiptReply) reply).receiptImageId = chars.toString().trim();
                        elementHandled = true;
                    } else if (!elementHandled && this.getClass().equals(SaveReceiptSAXHandler.class)) {
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
