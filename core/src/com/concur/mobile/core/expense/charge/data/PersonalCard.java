package com.concur.mobile.core.expense.charge.data;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.platform.expense.list.dao.PersonalCardDAO;
import com.concur.mobile.platform.expense.smartexpense.dao.SmartExpenseDAO;

public class PersonalCard {

    public String pcaKey;
    public String cardName;
    public String acctNumLastFour;
    public String crnCode;

    public ArrayList<PersonalCardTransaction> transactions;

    public PersonalCard() {
    }

    public PersonalCard(PersonalCardDAO personalCardDAO) {
        pcaKey = personalCardDAO.getPCAKey();
        cardName = personalCardDAO.getCardName();
        acctNumLastFour = personalCardDAO.getAcctNumLastFour();
        crnCode = personalCardDAO.getCrnCode();
    }

    public PersonalCard(SmartExpenseDAO smartExpense) {
        pcaKey = smartExpense.getPcaKey();
        // E-DAO: cardName needed here.
        acctNumLastFour = smartExpense.getCardLastSegment();
        crnCode = smartExpense.getCrnCode();
    }

    public int getTransactionCount() {
        int count = 0;
        if (transactions != null) {
            count = transactions.size();
        }
        return count;
    }

    public void addTransaction(PersonalCardTransaction t) {
        if (transactions == null) {
            transactions = new ArrayList<PersonalCardTransaction>();
        }

        transactions.add(t);

    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////
    //
    // BELOW HERE BE SAX DRAGONS
    //
    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////

    public static ArrayList<PersonalCard> parseCardXml(String responseXml) {

        ArrayList<PersonalCard> cards = null;

        if (responseXml != null && responseXml.length() > 0) {

            cards = new ArrayList<PersonalCard>();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                SAXParser parser = factory.newSAXParser();
                CardListSAXHandler handler = new CardListSAXHandler();
                parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
                cards = handler.getCards();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return cards;
    }

    /**
     * Handle the card level elements
     * 
     * @param localName
     */
    protected void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("AccountNumberLastFour")) {
            acctNumLastFour = cleanChars;
        } else if (localName.equalsIgnoreCase("CardName")) {
            cardName = cleanChars;
        } else if (localName.equalsIgnoreCase("CrnCode")) {
            crnCode = cleanChars;
        } else if (localName.equalsIgnoreCase("PcaKey")) {
            pcaKey = cleanChars;
        }
    }

    /**
     * Helper class to handle parsing of card XML.
     */
    protected static class CardListSAXHandler extends DefaultHandler {

        private static final String CARD = "PersonalCard";
        private static final String TRANSACTION = "PersonalCardTransaction";

        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.
        // A stack of element names would be cleaner and prettier but the booleans will be a lot faster.
        private boolean inCard;
        private boolean inTransaction;
        private boolean inMobileEntry;

        // Holders for our parsed data
        private ArrayList<PersonalCard> cards;
        private PersonalCard card;
        private PersonalCardTransaction txn;
        private MobileEntry.MobileEntrySAXHandler mobileEntryHandler;

        /**
         * Retrieve our list of parsed cards
         * 
         * @return A List of {@link PersonalCard} objects parsed from the XML
         */
        protected ArrayList<PersonalCard> getCards() {
            return cards;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            cards = new ArrayList<PersonalCard>();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            chars.append(ch, start, length);
        }

        /**
         * Handle the opening of all elements. Create data objects as needed for use in endElement().
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(CARD)) {
                card = new PersonalCard();
                inCard = true;
            } else if (inCard) {
                if (inTransaction) {
                    // Have to be in a transaction in order to get to a mobile entry.
                    if (localName.equals(MobileEntry.MobileEntrySAXHandler.MOBILE_ENTRY)) {
                        mobileEntryHandler = new MobileEntry.MobileEntrySAXHandler();
                        mobileEntryHandler.mobileEntry = new MobileEntry();
                        mobileEntryHandler.mobileEntry.setEntryType(Expense.ExpenseEntryType.PERSONAL_CARD);
                        inMobileEntry = true;
                    }
                } else if (localName.equalsIgnoreCase(TRANSACTION)) {
                    txn = new PersonalCardTransaction();
                    inTransaction = true;
                }
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (card != null) { // paranoia

                final String cleanChars = chars.toString().trim();

                if (inCard) {
                    if (inTransaction) {
                        if (inMobileEntry) {
                            if (localName.equalsIgnoreCase(MobileEntry.MobileEntrySAXHandler.MOBILE_ENTRY)) {
                                // End the mobile entry element.
                                txn.mobileEntry = mobileEntryHandler.mobileEntry;
                                // Anything parsed in at this point is a NORMAL entry. It exists on the server.
                                txn.mobileEntry.setStatus(MobileEntryStatus.NORMAL);
                                mobileEntryHandler = null;
                                inMobileEntry = false;
                            } else {
                                mobileEntryHandler.handleElement(localName, cleanChars);
                            }
                        } else if (localName.equalsIgnoreCase(TRANSACTION)) {
                            // End the transaction element
                            // Ensure that the 'pcaKey' and 'pctKey' on an associated mobile entry
                            // are set.
                            if (txn.mobileEntry != null) {
                                txn.mobileEntry.setPctKey(txn.pctKey);
                                txn.mobileEntry.setPcaKey(card.pcaKey);
                            }
                            inTransaction = false;
                            card.addTransaction(txn);
                        } else {
                            txn.handleElement(localName, cleanChars);
                        }
                    } else if (localName.equalsIgnoreCase(CARD)) {
                        // End the card element
                        inCard = false;
                        cards.add(card);
                    } else {
                        card.handleElement(localName, cleanChars);
                    }
                }
                chars.setLength(0);
            }
        }

    }

}
