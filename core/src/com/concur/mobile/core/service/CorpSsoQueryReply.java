package com.concur.mobile.core.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>ActionStatusServiceReply</code> for handling the response to a CorpSsoQuery request.
 * 
 * @author andy
 */
public class CorpSsoQueryReply extends ActionStatusServiceReply {

    private static final String CLS_TAG = CorpSsoQueryReply.class.getSimpleName();

    /**
     * Contains whether or not sso is enabled for the company.
     */
    public Boolean ssoEnabled;

    /**
     * Contains the company sso URL, if <code>ssoEnabled</code> is <code>true</code>.
     */
    public String ssoUrl;

    public static CorpSsoQueryReply parseXMLReply(String responseXml) {

        CorpSsoQueryReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new CorpSsoQuerySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (CorpSsoQueryReply) handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    protected static class CorpSsoQuerySAXHandler extends ActionStatusSAXHandler {

        private static final String SSO_RESPONSE = "SsoResponse";
        private static final String SSO_ENABLED = "SsoEnabled";
        private static final String SSO_URL = "SsoUrl";

        @Override
        protected ActionStatusServiceReply createReply() {
            return new CorpSsoQueryReply();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(SSO_RESPONSE)) {
                    reply = createReply();
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
            super.endElement(uri, localName, qName);
            if (!elementHandled) {
                if (reply != null) {
                    if (localName.equalsIgnoreCase(SSO_ENABLED)) {
                        if (reply instanceof CorpSsoQueryReply) {
                            ((CorpSsoQueryReply) reply).ssoEnabled = Parse.safeParseBoolean(chars.toString().trim());
                            elementHandled = true;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is not instance of CorpSsoQueryReply!");
                        }
                    } else if (localName.equalsIgnoreCase(SSO_URL)) {
                        if (reply instanceof CorpSsoQueryReply) {
                            ((CorpSsoQueryReply) reply).ssoUrl = chars.toString().trim();
                            elementHandled = true;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is not instance of CorpSsoQueryReply!");
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
                }
                if (elementHandled) {
                    chars.setLength(0);
                }
            }
        }
    }
}
