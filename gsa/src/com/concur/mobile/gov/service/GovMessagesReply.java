/**
 * @author sunill
 */
package com.concur.mobile.gov.service;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentValues;
import android.database.Cursor;

import com.concur.mobile.core.data.MobileDatabaseHelper;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class GovMessagesReply extends ServiceReply {

    public String behaviorTitle, behaviorText;
    public String privacyTitle, privacyText, privacyTextShort;
    public String warningTitle, warningText, warningTextShort;

    public String xmlReply;

    public Calendar lastRefreshTime;

    public GovMessagesReply() {
        // TODO Auto-generated constructor stub
    }

    public GovMessagesReply(Cursor cursor) {
        this.behaviorTitle = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_BEHAVE_TITLE));
        this.behaviorText = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_BEHAVE_MSG));
        this.privacyTitle = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_PRIVACY_TITLE));
        this.privacyTextShort = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_PRIVACY_SHORT_MSG));
        this.privacyText = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_PRIVACY_MSG));
        this.warningTitle = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_WARNING_TITLE));
        this.warningTextShort = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_WARNING_SHORT_MSG));
        this.warningText = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_WARNING_MSG));
        String date = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_LAST_USED));
        this.lastRefreshTime = Parse.parseTimestamp(date, FormatUtil.XML_DF);
    }

    public void setLastRefreshTime(Calendar lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }

    public static GovMessagesReply parseXml(String responseXml) {

        GovMessagesReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            GovMessagesReplySAXHandler handler = new GovMessagesReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class GovMessagesReplySAXHandler extends DefaultHandler {

        // list of tags
        private static final String MSGS = "GovWarningMessages";

        // parsing character
        private StringBuilder chars;

        // flag to track our place in xml hierarchy.
        private boolean inMsgs;

        // data holders
        private GovMessagesReply reply;

        protected GovMessagesReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new GovMessagesReply();

        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        /**
         * Handle the opening of all elements. Create data objects as needed for use
         * in endElement().
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            super.startElement(uri, localName, qName, attributes);
            if (localName.equalsIgnoreCase(MSGS)) {
                inMsgs = true;
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();

            if (inMsgs) {
                if (localName.equalsIgnoreCase(MSGS)) {
                    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    reply.lastRefreshTime = now;
                    inMsgs = false;
                } else {
                    handleElement(localName, cleanChars);
                }
            }
            chars.setLength(0);
        }

        /* Handle Elements */
        private void handleElement(String localName, String cleanChars) {
            if (localName.equalsIgnoreCase("behaviorTitle")) {
                reply.behaviorTitle = cleanChars;
            } else if (localName.equalsIgnoreCase("behaviorText")) {
                reply.behaviorText = cleanChars;
            } else if (localName.equalsIgnoreCase("privacyTitle")) {
                reply.privacyTitle = cleanChars;
            } else if (localName.equalsIgnoreCase("privacyText")) {
                reply.privacyText = cleanChars;
            } else if (localName.equalsIgnoreCase("privacyTextShort")) {
                reply.privacyTextShort = cleanChars;
            } else if (localName.equalsIgnoreCase("warningTitle")) {
                reply.warningTitle = cleanChars;
            } else if (localName.equalsIgnoreCase("warningText")) {
                reply.warningText = cleanChars;
            } else if (localName.equalsIgnoreCase("warningTextShort")) {
                reply.warningTextShort = cleanChars;
            }
        }

    }

    /**
     * Create content values to store into database.
     * 
     * @param reply
     *            : reference of GovMessagesReply
     * @return : content value for database.
     */
    public ContentValues getContentVals(GovMessagesReply reply) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_BEHAVE_TITLE, reply.behaviorTitle);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_BEHAVE_MSG, reply.behaviorText);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_PRIVACY_TITLE, reply.privacyTitle);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_PRIVACY_SHORT_MSG, reply.privacyTextShort);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_PRIVACY_MSG, reply.privacyText);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_WARNING_TITLE, reply.warningTitle);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_WARNING_SHORT_MSG, reply.warningTextShort);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_WARNING_MSG, reply.warningText);
        if (reply.lastRefreshTime != null) {
            initialValues.put(MobileDatabaseHelper.COLUMN_LAST_USED, FormatUtil.XML_DF
                .format(reply.lastRefreshTime.getTime()));
        } else {
            initialValues.putNull(MobileDatabaseHelper.COLUMN_LAST_USED);
        }
        return initialValues;

    }
}
