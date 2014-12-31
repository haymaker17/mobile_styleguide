/**
 * 
 */
package com.concur.mobile.core.expense.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.data.ListItemField;
import com.concur.mobile.core.expense.data.SearchListResponse;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>ServiceReply</code> for parsing the response to a <code>SearchListRequest</code>.
 * 
 * @author AndrewK
 */
public class SearchListReply extends ServiceReply {

    private static final String CLS_TAG = SearchListReply.class.getSimpleName();

    /**
     * Contains the search list response.
     */
    public SearchListResponse response;

    public static SearchListReply parseXMLReply(String responseXml) {

        SearchListReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            SearchListReplySAXHandler handler = new SearchListReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class SearchListReplySAXHandler extends DefaultHandler {

        private static final String CLS_TAG = SearchListReply.CLS_TAG + "."
                + SearchListReplySAXHandler.class.getSimpleName();

        private static final String LIST = "List";
        private static final String DESCRIPTOR = "Descriptor";
        private static final String LIST_ITEMS = "ListItems";
        private static final String LIST_ITEM = "ListItem";
        private static final String FIELDS = "Fields";
        private static final String FIELD = "Field";
        private static final String ID = "Id";
        private static final String VALUE = "Value";
        private static final String FIELD_ID = "FieldId";
        private static final String FT_CODE = "FtCode";
        private static final String CODE = "Code";
        private static final String KEY = "Key";
        private static final String TEXT = "Text";
        private static final String REPORT_KEY = "RptKey";
        private static final String QUERY = "Query";
        private static final String EXTERNAL = "External";

        /**
         * Gets a reference to the parsed reply.
         * 
         * @return the parsed reply.
         */
        SearchListReply getReply() {
            return reply;
        }

        // Contains the parsed reply.
        private SearchListReply reply;

        private StringBuilder chars;

        private boolean parsingDescriptor;

        /**
         * Contains the list item currently being parsed.
         */
        private ListItem listItem;

        /**
         * Contains the list item field currently being parsed.
         */
        private ListItemField listItemField;

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startDocument()
         */
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            chars = new StringBuilder();
            reply = new SearchListReply();
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
            super.startElement(uri, localName, qName, attributes);
            if (localName.equalsIgnoreCase(LIST)) {
                if (reply != null) {
                    reply.response = new SearchListResponse();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: reply is null!");
                }
            } else if (localName.equalsIgnoreCase(DESCRIPTOR)) {
                if (reply != null) {
                    if (reply.response != null) {
                        parsingDescriptor = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: reply.response is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: reply is null!");
                }
            } else if (localName.equalsIgnoreCase(LIST_ITEMS)) {
                if (reply != null) {
                    if (reply.response != null) {
                        reply.response.listItems = new ArrayList<ListItem>();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: reply.response is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: reply is null!");
                }
            } else if (localName.equalsIgnoreCase(LIST_ITEM)) {
                listItem = new ListItem();
            } else if (localName.equalsIgnoreCase(FIELDS)) {
                if (listItem != null) {
                    listItem.fields = new ArrayList<ListItemField>();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: listItem is null!");
                }
            } else if (localName.equalsIgnoreCase(FIELD)) {
                listItemField = new ListItemField();
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
            if (localName.equalsIgnoreCase(DESCRIPTOR)) {
                parsingDescriptor = false;
            } else if (localName.equalsIgnoreCase(FIELD)) {
                if (listItem != null) {
                    if (listItem.fields != null) {
                        if (listItemField != null) {
                            listItem.fields.add(listItemField);
                        }
                        listItemField = null;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: listItem.fields is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: listItem is null!");
                }
            } else if (localName.equalsIgnoreCase(REPORT_KEY)) {
                if (reply != null) {
                    if (reply.response != null) {
                        reply.response.rptKey = chars.toString().trim();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply.response is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
                }
            } else if (localName.equalsIgnoreCase(LIST_ITEM)) {
                if (reply != null) {
                    if (reply.response != null) {
                        if (reply.response.listItems != null) {
                            if (listItem != null) {
                                reply.response.listItems.add(listItem);
                                listItem = null;
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: listItem is null!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply.response.listItems is null!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply.response is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
                }
            } else if (localName.equalsIgnoreCase(FIELD_ID)) {
                if (reply != null) {
                    if (parsingDescriptor) {
                        if (reply.response != null) {
                            reply.response.fieldId = chars.toString().trim();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply.response is null!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: parsingDescriptor is false!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
                }
            } else if (localName.equalsIgnoreCase(FT_CODE)) {
                if (reply != null) {
                    if (parsingDescriptor) {
                        if (reply.response != null) {
                            reply.response.ftCode = chars.toString().trim();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply.response is null!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: parsingDescriptor is false!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
                }
            } else if (localName.equalsIgnoreCase(ID)) {
                if (listItemField != null) {
                    listItemField.id = chars.toString().trim();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: listItemField is null!");
                }
            } else if (localName.equalsIgnoreCase(EXTERNAL)) {
                if (listItem != null) {
                    listItem.external = Parse.safeParseBoolean(chars.toString().trim());
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: listItem is null!");
                }
            } else if (localName.equalsIgnoreCase(VALUE)) {
                if (listItemField != null) {
                    listItemField.value = chars.toString().trim();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: listItemField is null!");
                }
            } else if (localName.equalsIgnoreCase(CODE)) {
                if (listItem != null) {
                    listItem.code = chars.toString().trim();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: listItem is null!");
                }
            } else if (localName.equalsIgnoreCase(KEY)) {
                if (listItem != null) {
                    listItem.key = chars.toString().trim();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: listItem is null!");
                }
            } else if (localName.equalsIgnoreCase(TEXT)) {
                if (listItem != null) {
                    listItem.text = chars.toString().trim();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: listItem is null!");
                }
            } else if (localName.equalsIgnoreCase(QUERY)) {
                if (reply != null) {
                    if (parsingDescriptor) {
                        if (reply.response != null) {
                            reply.response.query = chars.toString().trim();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply.response is null!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: parsingDescriptor is false!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
                }
            } else if (localName.equalsIgnoreCase(LIST_ITEMS)) {
                // No-op.
            } else if (localName.equalsIgnoreCase(LIST)) {
                // No-op.
            } else if (localName.equalsIgnoreCase(FIELDS)) {
                // No-op.
            } else {
                // Log.d(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML element with name '" + localName + "' and value '" +
                // chars.toString().trim() + "'.");
            }

            // Clear out any collected characters.
            chars.setLength(0);
        }

    }

}
