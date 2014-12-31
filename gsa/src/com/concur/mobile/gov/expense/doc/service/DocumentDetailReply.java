/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.service;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.doc.data.AccountCode;
import com.concur.mobile.gov.expense.doc.data.Audit;
import com.concur.mobile.gov.expense.doc.data.DsDocDetailInfo;
import com.concur.mobile.gov.expense.doc.data.Exceptions;
import com.concur.mobile.gov.expense.doc.data.GovExpense;
import com.concur.mobile.gov.expense.doc.data.PerdiemTDY;
import com.concur.mobile.gov.expense.doc.data.ReasonCodes;

public class DocumentDetailReply extends ServiceReply {

    public Calendar lastRefreshTime;
    public DsDocDetailInfo detailInfo;

    public void setLastRefreshTime(Calendar lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }

    public static DocumentDetailReply parseXMLReply(String responseXml) {
        DocumentDetailReply reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            DocumentDetailReplySAXHandler handler = new DocumentDetailReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class DocumentDetailReplySAXHandler extends DefaultHandler {

        // list of tags
        private static final String DS_DOC_DETAIL = "dsDocDetail";
        private static final String DOC_DETAIL_INFO = "DocDetailInfo";
        private static final String PER_DIEM = "PerdiemTDY";
        private static final String TDY = "TDY";
        private static final String ACC_CODES = "AccountCodes";
        private static final String ACC_CODE = "AccountCode";
        private static final String AUDITS = "Audits";
        private static final String EXCEPTIONS = "Exceptions";
        private static final String EXCEPTION = "Exception";
        private static final String EXPENSES = "Expenses";
        private static final String EXPENSE = "Expense";
        private static final String REASON_CODES = "ReasonCodes";
        private static final String REASON_CODE = "ReasonCode";
        // parsing character
        private StringBuilder chars;
        // flag to track our place in xml hierarchy.
        private boolean inDsDocDetail, inDsDocDetailInfo, inPerDiem, inTDY, inAccCodes, inAccCode;
        private boolean inAudit, inExceptions, inException, inExpenses, inExpense;
        private boolean inReasonCodes, inReasonCode;
        // data holders
        private DocumentDetailReply reply;
        private DsDocDetailInfo dsDocDetailInfo;
        private PerdiemTDY tdy;
        private AccountCode accountCode;
        private Audit audit;
        private Exceptions exceptions;
        private ReasonCodes reasonCodes;
        private GovExpense expenses;

        protected DocumentDetailReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            chars = new StringBuilder();
            reply = new DocumentDetailReply();
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
            if (localName.equalsIgnoreCase(DS_DOC_DETAIL)) {
                inDsDocDetail = true;
            } else if (localName.equalsIgnoreCase(DOC_DETAIL_INFO)) {
                dsDocDetailInfo = new DsDocDetailInfo();
                inDsDocDetailInfo = true;
            } else if (localName.equalsIgnoreCase(PER_DIEM)) {
                inPerDiem = true;
            } else if (localName.equalsIgnoreCase(TDY)) {
                tdy = new PerdiemTDY();
                inTDY = true;
            } else if (localName.equalsIgnoreCase(ACC_CODES)) {
                inAccCodes = true;
            } else if (localName.equalsIgnoreCase(ACC_CODE)) {
                inAccCode = true;
                accountCode = new AccountCode();
            } else if (localName.equalsIgnoreCase(AUDITS)) {
                inAudit = true;
                audit = new Audit();
            } else if (localName.equalsIgnoreCase(EXCEPTIONS)) {
                inExceptions = true;
            } else if (localName.equalsIgnoreCase(EXCEPTION)) {
                inException = true;
                exceptions = new Exceptions();
            } else if (localName.equalsIgnoreCase(EXPENSES)) {
                inExpenses = true;
            } else if (localName.equalsIgnoreCase(EXPENSE)) {
                expenses = new GovExpense();
                inExpense = true;
            } else if (localName.equalsIgnoreCase(REASON_CODES)) {
                inReasonCodes = true;
            } else if (localName.equalsIgnoreCase(REASON_CODE)) {
                reasonCodes = new ReasonCodes();
                inReasonCode = true;
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            final String cleanChars = chars.toString().trim();
            if (inDsDocDetailInfo) {
                if (inPerDiem) {
                    handlePerDiemElements(localName, cleanChars);
                } else if (inAccCodes) {
                    handleAccCodeElements(localName, cleanChars);
                } else if (inAudit) {
                    if (localName.equalsIgnoreCase(AUDITS)) {
                        dsDocDetailInfo.audit = audit;
                        inAudit = false;
                    } else {
                        audit.handleElement(localName, cleanChars);
                    }
                } else if (inExceptions) {
                    handleExceptionsElements(localName, cleanChars);
                } else if (inExpenses) {
                    handleExpenseElements(localName, cleanChars);
                } else if (inReasonCodes) {
                    handleReasonCodeElements(localName, cleanChars);
                } else {
                    if (localName.equalsIgnoreCase(DOC_DETAIL_INFO)) {
                        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        dsDocDetailInfo.lastUsed = now;
                        inDsDocDetailInfo = false;
                    } else {
                        dsDocDetailInfo.handleElement(localName, cleanChars);
                    }
                }
            } else if (inDsDocDetail) {
                if (localName.equalsIgnoreCase(DS_DOC_DETAIL)) {
                    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    reply.lastRefreshTime = now;
                    reply.detailInfo = dsDocDetailInfo;
                    inDsDocDetail = false;
                }
            }
            chars.setLength(0);
        }

        private void handlePerDiemElements(String localName, String cleanChars) {
            if (localName.equalsIgnoreCase(PER_DIEM)) {
                inPerDiem = false;
            } else if (inTDY) {
                if (localName.equalsIgnoreCase(TDY)) {
                    dsDocDetailInfo.perdiemList.add(tdy);
                    inTDY = false;
                } else {
                    tdy.handleElement(localName, cleanChars);
                }
            }
        }

        private void handleAccCodeElements(String localName, String cleanChars) {
            if (localName.equalsIgnoreCase(ACC_CODES)) {
                inAccCodes = false;
            } else if (inAccCode) {
                if (localName.equalsIgnoreCase(ACC_CODE)) {
                    dsDocDetailInfo.accountCodeList.add(accountCode);
                    inAccCode = false;
                } else {
                    accountCode.handleElement(localName, cleanChars);
                }
            }
        }

        private void handleExceptionsElements(String localName, String cleanChars) {
            if (localName.equalsIgnoreCase(EXCEPTIONS)) {
                inExceptions = false;
            } else if (inException) {
                if (localName.equalsIgnoreCase(EXCEPTION)) {
                    dsDocDetailInfo.exceptionsList.add(exceptions);
                    inException = false;
                } else {
                    exceptions.handleElement(localName, cleanChars);
                }
            }
        }

        private void handleExpenseElements(String localName, String cleanChars) {
            if (localName.equalsIgnoreCase(EXPENSES)) {
                inExpenses = false;
            } else if (inExpense) {
                if (localName.equalsIgnoreCase(EXPENSE)) {
                    dsDocDetailInfo.expensesList.add(expenses);
                    inExpense = false;
                } else {
                    expenses.handleElement(localName, cleanChars);
                }
            }
        }

        private void handleReasonCodeElements(String localName, String cleanChars) {
            if (localName.equalsIgnoreCase(REASON_CODES)) {
                inReasonCodes = false;
            } else if (inReasonCode) {
                if (localName.equalsIgnoreCase(REASON_CODE)) {
                    dsDocDetailInfo.reasonCodeList.add(reasonCodes);
                    inReasonCode = false;
                } else {
                    reasonCodes.handleElement(localName, cleanChars);
                }
            }
        }
    }
}
