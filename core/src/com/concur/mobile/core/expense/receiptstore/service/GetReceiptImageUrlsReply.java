/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.expense.receiptstore.data.ReceiptInfo;
import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>ActionStatusServiceReply</code> for handling the response to a <code>GetReceiptImageUrlsRequest</code>.
 * 
 * @author AndrewK
 */
public class GetReceiptImageUrlsReply extends ActionStatusServiceReply {

    private static final String CLS_TAG = GetReceiptImageUrlsReply.class.getSimpleName();

    /**
     * Contains the list of parsed receipts.
     */
    public List<ReceiptInfo> receiptInfos = null;

    /**
     * Parses the XML that contains the reply to a <code>GetReceiptImageUrlsRequest</code>.
     * 
     * @param responseXml
     *            the XML to be parsed.
     * @return an instance of <code>GetReceiptImageUrlReply</code> containing the parsed reply.
     */
    public static GetReceiptImageUrlsReply parseReply(String responseXml) {

        GetReceiptImageUrlsReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new GetReceiptImageUrlsSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (GetReceiptImageUrlsReply) handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    /**
     * An extension of <code>ActionStatusSAXHandler</code> to parse the response to a <code>GetReceiptImageUrlsReply</code>.
     * 
     * @author AndrewK
     */
    protected static class GetReceiptImageUrlsSAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = GetReceiptImageUrlsReply.CLS_TAG
                + GetReceiptImageUrlsSAXHandler.class.getSimpleName();

        private static final String RECEIPT_INFOS = "ReceiptInfos";
        private static final String RECEIPT_INFO = "ReceiptInfo";
        private static final String FILE_NAME = "FileName";
        private static final String FILE_TYPE = "FileType";
        private static final String IMAGE_DATE = "ImageDate";
        private static final String IMAGE_ORIGIN = "ImageOrigin";
        private static final String IMAGE_SOURCE = "ImageSource";
        private static final String IMAGE_URL = "ImageUrl";
        private static final String RECEIPT_IMAGE_ID = "ReceiptImageId";
        private static final String THUMB_URL = "ThumbUrl";

        // The ReceiptInfo object currently being constructed.
        private ReceiptInfo receiptInfo = null;

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#createReply()
         */
        @Override
        protected ActionStatusServiceReply createReply() {
            return new GetReceiptImageUrlsReply();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#startElement(java.lang.String,
         * java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            elementHandled = false;
            super.startElement(uri, localName, qName, attributes);
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(RECEIPT_INFOS)) {
                    ((GetReceiptImageUrlsReply) getReply()).receiptInfos = new ArrayList<ReceiptInfo>();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(RECEIPT_INFO)) {
                    receiptInfo = new ReceiptInfo();
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
            elementHandled = false;
            super.endElement(uri, localName, qName);
            if (reply != null) {
                GetReceiptImageUrlsReply urlsReply = (GetReceiptImageUrlsReply) reply;
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(FILE_NAME)) {
                        receiptInfo.setFileName(chars.toString().trim());
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(FILE_TYPE)) {
                        receiptInfo.setFileType(chars.toString().trim());
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(IMAGE_DATE)) {
                        receiptInfo.setImageDate(chars.toString().trim());
                        receiptInfo.setImageCalendar(Parse.parseTimestamp(receiptInfo.getImageDate(),
                                FormatUtil.RECEIPT_STORE_LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND));
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(IMAGE_ORIGIN)) {
                        receiptInfo.setImageOrigin(chars.toString().trim());
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(IMAGE_SOURCE)) {
                        receiptInfo.setImageSource(chars.toString().trim());
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(IMAGE_URL)) {
                        receiptInfo.setImageUrl(chars.toString().trim());
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(RECEIPT_IMAGE_ID)) {
                        receiptInfo.setReceiptImageId(chars.toString().trim());
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(THUMB_URL)) {
                        receiptInfo.setThumbUrl(chars.toString().trim());
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(RECEIPT_INFO)) {
                        if (receiptInfo != null) {
                            if (urlsReply.receiptInfos == null) {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: found null receipt infos list!");
                                urlsReply.receiptInfos = new ArrayList<ReceiptInfo>();
                            }
                            urlsReply.receiptInfos.add(receiptInfo);
                            receiptInfo = null;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null current receipt info!");
                        }
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(RECEIPT_INFOS)) {
                        // No-op.
                        elementHandled = true;
                    } else if (!elementHandled && this.getClass().equals(GetReceiptImageUrlsSAXHandler.class)) {
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
