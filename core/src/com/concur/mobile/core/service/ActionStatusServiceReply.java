/**
 * 
 */
package com.concur.mobile.core.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> that utilizes the 'ActionStatus' object.
 * 
 * @author AndrewK
 */
public class ActionStatusServiceReply extends ServiceReply {

    private static final String CLS_TAG = ActionStatusServiceReply.class.getSimpleName();

    /**
     * Contains the original body of the reply.
     */
    protected String xmlReply;

    public static ActionStatusServiceReply parseReply(String responseXml) {

        ActionStatusServiceReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new ActionStatusSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = handler.getReply();
            srvReply.xmlReply = responseXml;
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
    public static class ActionStatusSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = ActionStatusServiceReply.CLS_TAG + "."
                + ActionStatusSAXHandler.class.getSimpleName();

        private static final String ACTION_STATUS = "ActionStatus";
        private static final String ERROR_MESSAGE = "ErrorMessage";
        private static final String STATUS = "Status";
        private static final String ERROR = "Error";
        private static final String MESSAGE = "Message";

        // The reply being parsed.
        protected ActionStatusServiceReply reply;

        protected StringBuilder chars;

        /**
         * Contains whether or not this parser handled an event parsing notification.
         */
        protected boolean elementHandled;

        /**
         * Gets the reply that has been parsed.
         * 
         * @return the parsed instance of <code>ActionStatusServiceReply</code>.
         */
        public ActionStatusServiceReply getReply() {
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
            reply = createReply();
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
            elementHandled = false;
            if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(ERROR)) {
                // Non-200 responses, i.e., a 500 come back with a body of the form
                // <ERROR><MESSAGE>blah, blah</MESSAGE></ERROR> so a check is here for 'ERROR' in order
                // to set the error status prior to the end of parsing the MESSAGE in 'endElement'.
                reply.mwsStatus = Const.REPLY_STATUS_FAILURE;
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

            elementHandled = false;
            if (reply != null) {
                if (localName.equalsIgnoreCase(STATUS)) {
                    reply.mwsStatus = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ERROR_MESSAGE)) {
                    reply.mwsErrorMessage = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                    // No-op.
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ERROR)) {
                    reply.mwsStatus = Const.REPLY_STATUS_FAILURE;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(MESSAGE)) {
                    if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_FAILURE)
                            || reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_FAIL)) {
                        reply.mwsErrorMessage = chars.toString().trim();
                        elementHandled = true;
                    }
                } else if (!elementHandled && this.getClass().equals(ActionStatusSAXHandler.class)) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '" + localName + "'.");
                }
                if (elementHandled) {
                    chars.setLength(0);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
            }
        }

        /**
         * Constructs a new instance of <code>ActionStatusServiceReply</code> appropriate for this SAX handler.
         * 
         * @return an instance of <code>ActionStatusServiceReply</code> appropriate for this SAX handler.
         */
        protected ActionStatusServiceReply createReply() {
            return new ActionStatusServiceReply();
        }
    }

}
