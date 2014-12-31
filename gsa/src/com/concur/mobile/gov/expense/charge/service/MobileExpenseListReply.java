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
import com.concur.mobile.gov.expense.charge.data.MobileExpense;

public class MobileExpenseListReply extends ServiceReply {

    public List<MobileExpense> mobExpList;
    public Calendar lastRefreshTime;
    public String xmlReply;

    public void setLastRefreshTime(Calendar lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }

    public static MobileExpenseListReply parseXmlResponse(String responseXml) {
        MobileExpenseListReply reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ExpenseListReplySAXHandler handler = new ExpenseListReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class ExpenseListReplySAXHandler extends DefaultHandler {

        // list of tags
        private static final String MOB_EXPS = "MobileExps";
        private static final String MOB_EXP = "MobileExp";
        // parsing character
        private StringBuilder chars;
        // // flag to track our place in xml hierarchy.
        private boolean inExps;
        private boolean inExp;
        // data holders
        private MobileExpenseListReply reply;
        private MobileExpense expense;

        protected MobileExpenseListReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            chars = new StringBuilder();
            reply = new MobileExpenseListReply();
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
            if (localName.equalsIgnoreCase(MOB_EXPS)) {
                inExps = true;
                reply.mobExpList = new ArrayList<MobileExpense>();
            } else if (localName.equalsIgnoreCase(MOB_EXP)) {
                inExp = true;
                expense = new MobileExpense();
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            final String cleanChars = chars.toString().trim();
            if (inExp) {
                if (localName.equalsIgnoreCase(MOB_EXP)) {
                    reply.mobExpList.add(expense);
                    inExp = false;
                } else {
                    expense.handleElement(localName, cleanChars);
                }
            } else if (inExps) {
                if (localName.equalsIgnoreCase(MOB_EXPS)) {
                    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    reply.lastRefreshTime = now;
                }
            }
            chars.setLength(0);
        }
    }
}
