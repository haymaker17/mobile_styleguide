/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.charge.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.charge.data.ActionStatus;

public class AddToVchReply extends ServiceReply {

    public List<ActionStatus> documentList;
    public String status;
    public Calendar lastRefreshTime;
    public String xmlReply;

    // not required right now, but in future it may required when we add this data into database.
    public void setLastRefreshTime(Calendar lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }

    public static AddToVchReply parseXml(String responseXml) {

        AddToVchReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new ActionStatusSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class ActionStatusSAXHandler extends DefaultHandler {

        // list of tags
        private static final String ACTION_STATUS = "ActionStatus";
        private static final String STATUS = "Status";
        private static final String EXP = "expenses";

        // parsing character
        private StringBuilder chars;

        // // flag to track our place in xml hierarchy.
        private boolean inOuterActionStatus, innerActionStatus;
        private boolean inOuterStatus;
        private boolean inExp;

        // data holders
        private AddToVchReply reply;
        private ActionStatus status;

        protected AddToVchReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new AddToVchReply();

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

            if (localName.equalsIgnoreCase(EXP)) {
                inExp = true;
            } else if (inExp && localName.equalsIgnoreCase(ACTION_STATUS)) {
                innerActionStatus = true;
                status = new ActionStatus();
            } else if (!inExp && !innerActionStatus && localName.equalsIgnoreCase(ACTION_STATUS)) {
                inOuterActionStatus = true;
                reply.documentList = new ArrayList<ActionStatus>();
            } else if (!inExp && !innerActionStatus && localName.equalsIgnoreCase(STATUS)) {
                inOuterStatus = true;
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();

            if (inOuterActionStatus) {
                if (inOuterStatus) {
                    if (localName.equalsIgnoreCase(STATUS)) {
                        reply.status = cleanChars;
                        inOuterStatus = false;
                    }
                } else if (inExp) {
                    if (localName.equalsIgnoreCase(EXP)) {
                        inExp = false;
                    } else {
                        if (innerActionStatus && localName.equalsIgnoreCase(ACTION_STATUS)) {
                            reply.documentList.add(status);
                            innerActionStatus = false;
                        } else {
                            status.handleElement(localName, cleanChars);
                        }
                    }
                }
                if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    reply.lastRefreshTime = now;
                }
            }
            chars.setLength(0);
        }

    }
}
