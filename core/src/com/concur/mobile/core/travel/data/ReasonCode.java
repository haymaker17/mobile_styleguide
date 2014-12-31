/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.util.Const;

/**
 * @author AndrewK
 */
public class ReasonCode {

    private static final String CLS_TAG = ReasonCode.class.getSimpleName();

    // Contains the reason code description.
    public String description;

    // Contains the reason code id.
    public String id;

    // Contains the reason code violation type.
    public String violationType;

    /**
     * Will set the attribute <code>attrName</code> to the value <code>attrValue</code>.
     * 
     * @param localName
     *            the attribute name.
     * @param value
     *            the attribute value.
     * @return whether the attribute name was recognized.
     */
    public boolean handleElement(String localName, String value) {
        boolean attrSet = true;
        if (localName.equalsIgnoreCase(ReasonCodeSAXHandler.DESCRIPTION)) {
            description = value;
        } else if (localName.equalsIgnoreCase(ReasonCodeSAXHandler.ID)) {
            id = value;
        } else if (localName.equalsIgnoreCase(ReasonCodeSAXHandler.VIOLATION_TYPE)) {
            violationType = value;
        } else {
            attrSet = false;
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleElement: unrecognized XML tag '" + localName + "'.");
        }
        return attrSet;
    }

    public static List<ReasonCode> parseReasonCodes(String xmlReply) throws IOException {
        List<ReasonCode> reasonCodes = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ReasonCodeSAXHandler handler = new ReasonCodeSAXHandler();
            parser.parse(new ByteArrayInputStream(xmlReply.getBytes()), handler);
            reasonCodes = handler.getReasonCodes();
        } catch (ParserConfigurationException parsConfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseReasonCodes: parser exception.", parsConfExc);
            throw new IOException(parsConfExc.getMessage());
        } catch (SAXException saxExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseReasonCodes: sax parsing exception.", saxExc);
            throw new IOException(saxExc.getMessage());
        }
        return reasonCodes;
    }

    /**
     * An extension of <code>DefaultHandler</code> to handle parsing a list of reason codes.
     */
    public static class ReasonCodeSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = ReasonCode.CLS_TAG + "." + ReasonCodeSAXHandler.class.getSimpleName();

        static final String DESCRIPTION = "Description";
        static final String ID = "Id";
        static final String VIOLATION_TYPE = "ViolationType";
        static final String REASON_CODE_LIST = "ArrayOfReasonCode";
        static final String REASON_CODE = "ReasonCode";

        /**
         * Contains a list of the parsed reason codes.
         */
        protected List<ReasonCode> reasonCodes;

        /**
         * Contains the currently parsed reason code.
         */
        protected ReasonCode reasonCode;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Contained parsed field data.
         */
        protected StringBuilder chars = new StringBuilder();

        /**
         * Gets the parsed reason codes.
         * 
         * @return returns the parsed reason codes.
         */
        public List<ReasonCode> getReasonCodes() {
            return reasonCodes;
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
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            elementHandled = false;
            super.startElement(uri, localName, qName, attributes);
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(REASON_CODE_LIST)) {
                    reasonCodes = new ArrayList<ReasonCode>();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(REASON_CODE)) {
                    reasonCode = new ReasonCode();
                    elementHandled = true;
                }
            }
            if (elementHandled) {
                chars.setLength(0);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            elementHandled = false;
            super.endElement(uri, localName, qName);
            if (!elementHandled) {
                final String cleanChars = chars.toString().trim();
                if (localName.equalsIgnoreCase(REASON_CODE)) {
                    if (reasonCode != null) {
                        if (reasonCodes != null) {
                            reasonCodes.add(reasonCode);
                            reasonCode = null;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reasonCodes is null!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reasonCode is null!");
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(REASON_CODE_LIST)) {
                    // No-op.
                    elementHandled = true;
                } else if (reasonCode != null) {
                    reasonCode.handleElement(localName, cleanChars);
                    elementHandled = true;
                } else if (!elementHandled && this.getClass().equals(ReasonCodeSAXHandler.class)) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '" + localName
                            + "' and value '" + chars.toString().trim() + "'.");
                }
            }
            if (elementHandled) {
                // Clear out the stored element values.
                chars.setLength(0);
            }
        }

    }

}
