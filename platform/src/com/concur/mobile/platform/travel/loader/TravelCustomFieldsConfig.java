package com.concur.mobile.platform.travel.loader;

import android.util.Log;
import com.concur.mobile.platform.travel.loader.TravelCustomField.TravelCustomFieldSAXHandler;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

/**
 * Models travel custom fields configuration.
 * 
 * @author RatanK
 * 
 */
public class TravelCustomFieldsConfig implements Serializable {

    private static final String CLS_TAG = TravelCustomFieldsConfig.class.getSimpleName();

    /**
     * Contains whether or not the travel custom fields has dependencies, i.e., dynamic custom fields.
     */
    public Boolean hasDependencies = Boolean.FALSE;

    public boolean errorOccuredWhileRetrieving;

    /**
     * Contains the list of travel custom fields.
     */
    public List<TravelCustomField> formFields;

    public static TravelCustomFieldsConfig parseTravelCustomFieldsConfig(InputStream is) throws IOException {
        TravelCustomFieldsConfig config = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            TravelCustomFieldsSAXHandler handler = new TravelCustomFieldsSAXHandler();
            parser.parse(is, handler);
            config = handler.getConfig();
        } catch (ParserConfigurationException parsConfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseTravelCustomFieldsConfig: parser exception.", parsConfExc);
            throw new IOException(parsConfExc.getMessage());
        } catch (SAXException saxExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseTravelCustomFieldsConfig: sax parsing exception.", saxExc);
            throw new IOException(saxExc.getMessage());
        }
        return config;
    }

    /**
     * An extension of <code>DefaultHandler</code> to handle parsing travel custom fields information.
     */
    public static class TravelCustomFieldsSAXHandler extends DefaultHandler {

        private static final String FIELD_LIST = "FieldList";
        private static final String HAS_DEPENDENCY = "HasDependency";
        private static final String FIELDS = "Fields";

        /**
         * Reference to a parsed configuration.
         */
        protected TravelCustomFieldsConfig config;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Contained parsed field data.
         */
        protected StringBuilder chars = new StringBuilder();

        /**
         * Contains a reference to the form field parser.
         */
        private TravelCustomFieldSAXHandler formFieldHandler;

        /**
         * Gets the parsed travel custom field configuration.
         * 
         * @return returns the parsed travel custom field configuration.
         */
        public TravelCustomFieldsConfig getConfig() {
            return config;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

            if (formFieldHandler != null) {
                formFieldHandler.characters(ch, start, length);
            } else {
                super.characters(ch, start, length);
                chars.append(ch, start, length);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if (formFieldHandler != null) {
                formFieldHandler.startElement(uri, localName, qName, attributes);
            } else {
                elementHandled = false;
                super.startElement(uri, localName, qName, attributes);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(FIELD_LIST)) {
                        config = new TravelCustomFieldsConfig();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(FIELDS)) {
                        formFieldHandler = new TravelCustomField.TravelCustomFieldSAXHandler();
                        elementHandled = true;
                    }
                }
                if (elementHandled) {
                    chars.setLength(0);
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if (config != null) {
                if (formFieldHandler != null) {
                    if (localName.equalsIgnoreCase(FIELDS)) {
                        config.formFields = formFieldHandler.getFields();
                        formFieldHandler = null;
                    } else {
                        formFieldHandler.endElement(uri, localName, qName);
                    }
                    elementHandled = true;
                } else {
                    elementHandled = false;
                    super.endElement(uri, localName, qName);
                    if (!elementHandled) {
                        final String cleanChars = chars.toString().trim();
                        if (localName.equalsIgnoreCase(HAS_DEPENDENCY)) {
                            config.hasDependencies = Parse.safeParseBoolean(cleanChars);
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(FIELD_LIST)) {
                            // No-op.
                            elementHandled = true;
                        } else if (!elementHandled && this.getClass().equals(TravelCustomFieldsSAXHandler.class)) {
                            Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '" + localName
                                    + "' and value '" + chars.toString().trim() + "'.");
                        }
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: config is null!");
            }

            if (elementHandled) {
                // Clear out the stored element values.
                chars.setLength(0);
            }
        }

    }

}
