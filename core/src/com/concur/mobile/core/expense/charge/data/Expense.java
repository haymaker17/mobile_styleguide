/**
 * 
 */
package com.concur.mobile.core.expense.charge.data;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.smartexpense.SmartExpense;
import com.concur.mobile.platform.expenseit.ExpenseItReceipt;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * 
 * An expense representing either a card or cash expense.
 * 
 * @deprecated v9.16 - This has been replaced with the SmartExpense and ExpenseProvider.
 * 
 * @author AndrewK
 */
@Deprecated
public class Expense {

    private static final String CLS_TAG = Expense.class.getSimpleName();

    /**
     * An enumeration representing an expense entry type.
     * 
     * @author AndrewK
     */
    public enum ExpenseEntryType {
        PERSONAL_CARD, // Personal card transaction.
        CORPORATE_CARD, // Corporate card transaction.
        CASH, // Cash transaction.
        SMART_CORPORATE, // A smart expense type (corporate card transaction + cash).
        SMART_PERSONAL, // A smart expense type (personal card transaction + cash).
        E_RECEIPT,       // E-receipt
        RECEIPT_CAPTURE, // A receipt capture/expense it expenses.
        OCR_NOT_DONE,   // An OCR receipt that is either pending, failed, or canceled.
        EXPENSEIT_NOT_DONE,    // An ExpenseIt item that is currently in processing.
        UNKNOWN_EXPENSE // If we can't determine the type of expense based on keys and SmartExpenseId.

    };

    private SmartExpense smartExpense;

    // The expense entry type.
    private ExpenseEntryType type;

    // The cash transaction.
    private MobileEntry cashTransaction;

    // The personal card associated with this personal card transaction.
    private PersonalCard personalCard;

    // The personal card transaction.
    private PersonalCardTransaction personalCardTransaction;

    // The corporate card transaction.
    private CorporateCardTransaction corporateCardTransaction;

    // The receipt capture transaction.
    private ReceiptCapture receiptCapture;

    // The eReceipt transaction
    private EReceipt eReceipt;

    // The OCRItem for this expense
    private OCRItem ocrItem;

    // The ExpenseItReceipt for this expense.
    private ExpenseItReceipt expenseItReceipt;

    // Whether or not we show the card icon in the Expenses list
    private boolean shouldShowCardIcon;

    // Whether or not we show the receipt icon in the Expenses list
    private boolean shouldShowReceiptIcon;

    // Whether or not the Expense is smart matched in any way.
    private boolean isSmartMatched;

    /**
     * Created a new Expense Entry using the DAO representing an OCR receipt.
     * 
     * @param ocrReceipt
     */
    public Expense(ReceiptDAO ocrReceipt) {
        type = ExpenseEntryType.OCR_NOT_DONE;
        ocrItem = new OCRItem(ocrReceipt);
    }

    /**
     * Constructs a new instance of an <code>Expense</code> that represents an ExpenseIt processing
     * item.
     *
     * @param expItReceipt
     */
    public Expense(ExpenseItReceipt expItReceipt) {
        type = ExpenseEntryType.EXPENSEIT_NOT_DONE;
        this.expenseItReceipt = expItReceipt;
    }

    /**
     * Constructs a new instance of <code>Expense</code> representing a cash transaction.
     * 
     * @param cashTransaction
     *            the cash transaction.
     */
    public Expense(MobileEntry cashTransaction) {
        type = ExpenseEntryType.CASH;
        this.cashTransaction = cashTransaction;
    }

    /**
     * Constructs a new instance of <code>Expense</code> representing a personal card transaction.
     * 
     * @param personalCard
     *            the transaction personal card.
     * @param personalCardTransaction
     *            the transaction.
     */
    public Expense(PersonalCard personalCard, PersonalCardTransaction personalCardTransaction) {
        type = ExpenseEntryType.PERSONAL_CARD;
        this.personalCard = personalCard;
        this.personalCardTransaction = personalCardTransaction;
    }

    /**
     * Constructs an new instance of <code>Expense</code> representing a corporate card transaction.
     * 
     * @param corporateCardTransaction
     *            the corporate card transaction.
     */
    public Expense(CorporateCardTransaction corporateCardTransaction) {
        type = ExpenseEntryType.CORPORATE_CARD;
        this.corporateCardTransaction = corporateCardTransaction;
    }

    /**
     * Constructs an new instance of <code>Expense</code> representing a receiptCapture transactions
     * 
     * @param receiptCaptures
     *            the receipt capture transaction
     */
    public Expense(ReceiptCapture receiptCaptures) {
        type = ExpenseEntryType.RECEIPT_CAPTURE;
        this.receiptCapture = receiptCaptures;
    }

    public Expense(SmartExpense smartExpense) {
        // Get all keys, then determine what type of expense we're dealing with and build it out
        boolean hasEReceiptId = !(TextUtils.isEmpty(smartExpense.getEReceiptId()));
        boolean hasCctKey = !(TextUtils.isEmpty(smartExpense.getCctKey()));
        boolean hasPctKey = !(TextUtils.isEmpty(smartExpense.getPctKey()));
        boolean hasRcKey = !(TextUtils.isEmpty(smartExpense.getRcKey()));
        boolean hasMeKey = !(TextUtils.isEmpty(smartExpense.getMeKey()));

        this.smartExpense = smartExpense;

        // Card icons are shown if any card is present
        shouldShowCardIcon = (hasCctKey || hasPctKey);
        // Receipt icon is shown for sure if E-Receipt or OCR (if Mobile Entry, check in QuickExpense).
        shouldShowReceiptIcon = (hasEReceiptId || hasRcKey || !TextUtils.isEmpty(smartExpense.getCctReceiptImageId())
                || !TextUtils.isEmpty(smartExpense.getMobileReceiptImageId()) || !TextUtils.isEmpty(smartExpense
                .getReceiptImageId()));

        // E-Receipt is most restrictive in Read/Write fields, set highest priority
        if (hasEReceiptId) {
            type = ExpenseEntryType.E_RECEIPT;
            eReceipt = new EReceipt(smartExpense);
            // E-Receipt can match with any type of expense
            isSmartMatched = (hasCctKey || hasPctKey || hasRcKey || hasMeKey);

        } else if (hasRcKey) {
            // OCR Expenses are equally restrictive
            type = ExpenseEntryType.RECEIPT_CAPTURE;
            receiptCapture = new ReceiptCapture(smartExpense);
            // Can't be E-Receipt or Card down here, so this would only be smart matched with a Mobile Entry
            isSmartMatched = (hasMeKey || hasCctKey || hasPctKey);

        } else if (hasCctKey) {
            // Card charges (smart or not) are next most restrictive.
            corporateCardTransaction = new CorporateCardTransaction(smartExpense);
            if (hasMeKey) {
                type = ExpenseEntryType.SMART_CORPORATE;
                cashTransaction = new MobileEntry(smartExpense);
            } else {
                type = ExpenseEntryType.CORPORATE_CARD;
            }
            // Won't be E-Receipt or OCR at this point so match with only Mobile Entry or OCR
            isSmartMatched = (hasMeKey);

        } else if (hasPctKey) {
            personalCard = new PersonalCard(smartExpense);
            personalCardTransaction = new PersonalCardTransaction(smartExpense);
            if (hasMeKey) {
                type = ExpenseEntryType.SMART_PERSONAL;
                cashTransaction = new MobileEntry(smartExpense);
            } else {
                type = ExpenseEntryType.PERSONAL_CARD;
            }
            // Won't be E-Receipt or OCR at this point so match with only Mobile Entry or OCR
            isSmartMatched = (hasMeKey);

        } else if (hasMeKey) {
            // Pure Quick Expense - all RW fields, no smart match.
            type = ExpenseEntryType.CASH;
            cashTransaction = new MobileEntry(smartExpense);

        } else {
            // Last resort, just set it to UNKOWN
            type = ExpenseEntryType.UNKNOWN_EXPENSE;
            Log.w(Const.LOG_TAG, CLS_TAG + ".Expense() - could not determine the expense type!!!");
        }

    }

    /**
     * Constructs an new instance of <code>Expense</code> representing a e-receipt transactions
     * 
     * @param eReceipt
     *            the e-receipt transaction
     */
    public Expense(EReceipt eReceipt) {
        type = ExpenseEntryType.E_RECEIPT;
        this.eReceipt = eReceipt;
    }

    /**
     * Constructs an instance of <code>Expense</code> representing a "smart" expense, one for which the MWS has suggested may be
     * the same expense and consisting of a corporate card transaction and a cash expense.
     * 
     * @param corporateCardTransaction
     * @param cashTransaction
     */
    public Expense(CorporateCardTransaction corporateCardTransaction, MobileEntry cashTransaction) {
        type = ExpenseEntryType.SMART_CORPORATE;
        this.corporateCardTransaction = corporateCardTransaction;
        this.cashTransaction = cashTransaction;
    }

    /**
     * Constructs an instance of <code>Expense</code> representing a "smart" expense, one for which there is a personal card
     * charge and mobile entry that have been matched by the MWS.
     * 
     * @param personalCard
     *            the personal card.
     * @param personalCardTransaction
     *            the matched personal card transaction.
     * @param cashTransaction
     *            the matched cash transaction.
     */
    public Expense(PersonalCard personalCard, PersonalCardTransaction personalCardTransaction,
            MobileEntry cashTransaction) {
        type = ExpenseEntryType.SMART_PERSONAL;
        this.personalCard = personalCard;
        this.personalCardTransaction = personalCardTransaction;
        this.cashTransaction = cashTransaction;
    }

    /**
     * Gets the expense entry type.
     * 
     * @return the expense entry type.
     */
    public ExpenseEntryType getExpenseEntryType() {
        return type;
    }

    /**
     * Gets the personal card transaction associated with this expense entry.
     * 
     * @return the personal card transaction associated with this entry.
     */
    public PersonalCardTransaction getPersonalCardTransaction() {
        return personalCardTransaction;
    }

    /**
     * Gets the personal card associated with the personal card transaction.
     * 
     * @return the personal card associated with this personal card transaction.
     */
    public PersonalCard getPersonalCard() {
        return personalCard;
    }

    /**
     * Gets the corporate card transaction associated with this expense entry.
     * 
     * @return the corporate card transaction associated with this expense entry.
     */
    public CorporateCardTransaction getCorporateCardTransaction() {
        return corporateCardTransaction;
    }

    /**
     * Gets the Receipt Capture transaction associated with this expense entry.
     * 
     * @return the Receipt Capture transaction associated with this expense entry.
     */
    public ReceiptCapture getReceiptCapture() {
        return receiptCapture;
    }

    /**
     * Gets the E-Receipt transaction associated with this expense entry.
     * 
     * @return the E-Receipt transaction associated with this expense entry.
     */
    public EReceipt getEReceipt() {
        return eReceipt;
    }

    /**
     * @return the <code>OCRItem</code> if this is an OCR Expense Entry, or <code>null</code> if it isn't.
     * 
     */
    public OCRItem getOcrItem() {
        return ocrItem;
    }

    /**
     * @return the <code>ExpenseItReceipt</code> if this is an ExpenseIt Entry, or <code>null</code>
     * if it isn't.
     *
     */
    public ExpenseItReceipt getExpenseItReceipt() {
        return expenseItReceipt;
    }

    /**
     * Gets the cash transaction associated with this expense entry.
     * 
     * @return the cash transaction associated with this expense entry.
     */
    public MobileEntry getCashTransaction() {
        return cashTransaction;
    }

    public SmartExpense getSmartExpense() {
        return smartExpense;
    }

    /**
     * Will parse the XML representation of a list of expense entries containing, cash, personal and corporate card transactions.
     * 
     * @param expenseEntryXml
     *            the XML representation.
     * 
     * @return the parser handler containing the parsed entries.
     */
    public static ExpenseEntrySAXHandler parseExpenseEntryXml(String expenseEntryXml) throws IOException {
        ExpenseEntrySAXHandler handler = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            handler = new ExpenseEntrySAXHandler();
            parser.parse(new ByteArrayInputStream(expenseEntryXml.getBytes()), handler);
        } catch (ParserConfigurationException parsConfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseExpenseentryXML: parser exception.", parsConfExc);
            throw new IOException(parsConfExc.getMessage());
        } catch (SAXException saxExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseExpenseEntryXML: sax parsing exception.", saxExc);
            throw new IOException(saxExc.getMessage());
        }
        return handler;
    }

    /**
     * Provides an extension of <code>DefaultHandler</code> to support parsing expense entry information.
     * 
     * @author AndrewK
     */
    public static class ExpenseEntrySAXHandler extends DefaultHandler {

        // private static final String CLS_TAG = Expense.CLS_TAG + "." + ExpenseEntrySAXHandler.class.getSimpleName();

        private static final String ALL_EXPENSES = "AllExpenses";
        private static final String ENTRIES = "Entries";
        private static final String PERSONAL_CARDS = "PersonalCards";
        private static final String CORPORATE_CARD_TRANSACTIONS = "CorporateCardTransactions";
        private static final String RECEIPT_CAPTURES = "ReceiptCaptures";

        /**
         * Contains the list of parsed expenses.
         */
        private ArrayList<Expense> expenses;

        /**
         * Contains the list of parsed personal card objects.
         */
        private ArrayList<PersonalCard> personalCards;

        /**
         * Contains a reference to the cash entries handler.
         */
        private MobileEntry.MobileEntrySAXHandler entriesHandler;

        /**
         * Contains a reference to the personal card entries handler.
         */
        private PersonalCard.CardListSAXHandler cardsHandler;

        /**
         * Contains a reference to the corporate card transaction entries handler.
         */
        private CorporateCardTransaction.CorporateCardSAXHandler corpCardTransHandler;

        /**
         * Contains a reference to the expense it entries handler.
         */
        private ReceiptCapture.ReceiptCaptureSAXHandler receiptCaptureSAXHandler;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Contains the list of parsed expense it expenses.
         */
        private ArrayList<ReceiptCapture> listOfReceiptCaptures;

        /**
         * Gets the list of parsed expenses.
         * 
         * @return the list of parsed expenses.
         */
        public ArrayList<Expense> getExpenses() {
            return expenses;
        }

        /**
         * Gets the list of parsed personal card objects.
         * 
         * @return the list of personal card objects.
         */
        public ArrayList<PersonalCard> getPersonalCards() {
            return personalCards;
        }

        /**
         * Gets the list of parsed personal card objects.
         * 
         * @return the list of personal card objects.
         */
        public ArrayList<ReceiptCapture> getReceiptCaptureExpenses() {
            return listOfReceiptCaptures;
        }

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (entriesHandler != null) {
                entriesHandler.characters(ch, start, length);
            } else if (cardsHandler != null) {
                cardsHandler.characters(ch, start, length);
            } else if (corpCardTransHandler != null) {
                corpCardTransHandler.characters(ch, start, length);
            } else if (receiptCaptureSAXHandler != null) {
                receiptCaptureSAXHandler.characters(ch, start, length);
            } else {
                super.characters(ch, start, length);
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

            elementHandled = false;
            super.startElement(uri, localName, qName, attributes);
            if (entriesHandler != null) {
                entriesHandler.startElement(uri, localName, qName, attributes);
                elementHandled = true;
            } else if (cardsHandler != null) {
                cardsHandler.startElement(uri, localName, qName, attributes);
                elementHandled = true;
            } else if (corpCardTransHandler != null) {
                corpCardTransHandler.startElement(uri, localName, qName, attributes);
                elementHandled = true;
            } else if (receiptCaptureSAXHandler != null) {
                receiptCaptureSAXHandler.startElement(uri, localName, qName, attributes);
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(ALL_EXPENSES)) {
                expenses = new ArrayList<Expense>();
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(ENTRIES)) {
                chars.setLength(0);
                entriesHandler = new MobileEntry.MobileEntrySAXHandler();
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(PERSONAL_CARDS)) {
                chars.setLength(0);
                cardsHandler = new PersonalCard.CardListSAXHandler();
                cardsHandler.startDocument();
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(CORPORATE_CARD_TRANSACTIONS)) {
                chars.setLength(0);
                corpCardTransHandler = new CorporateCardTransaction.CorporateCardSAXHandler();
                corpCardTransHandler.startDocument();
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(RECEIPT_CAPTURES)) {
                chars.setLength(0);
                receiptCaptureSAXHandler = new ReceiptCapture.ReceiptCaptureSAXHandler();
                receiptCaptureSAXHandler.startDocument();
                elementHandled = true;
            } else if (!elementHandled) {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: unhandled XML tag '" + localName + "'.");
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
            if (entriesHandler != null) {
                if (localName.equalsIgnoreCase(ENTRIES)) {
                    // Add the list of cash entries to our expense list.
                    addCashEntriesToExpenses(entriesHandler.getMobileEntries());
                    entriesHandler = null;
                } else {
                    entriesHandler.endElement(uri, localName, qName);
                }
                elementHandled = true;
            }
            if (cardsHandler != null) {
                if (localName.equalsIgnoreCase(PERSONAL_CARDS)) {
                    personalCards = cardsHandler.getCards();
                    addPersonalCardEntriesToExpenses(personalCards);
                    cardsHandler = null;
                } else {
                    cardsHandler.endElement(uri, localName, qName);
                }
                elementHandled = true;
            }
            if (corpCardTransHandler != null) {
                if (localName.equalsIgnoreCase(CORPORATE_CARD_TRANSACTIONS)) {
                    addCorporateCardEntriesToExpenses(corpCardTransHandler.getTransactions());
                    corpCardTransHandler = null;
                } else {
                    corpCardTransHandler.endElement(uri, localName, qName);
                }
                elementHandled = true;
            }
            if (receiptCaptureSAXHandler != null) {
                if (localName.equalsIgnoreCase(RECEIPT_CAPTURES)) {
                    addReceiptCapturesToExpenses(receiptCaptureSAXHandler.getReceiptCaptures());
                    receiptCaptureSAXHandler = null;
                } else {
                    receiptCaptureSAXHandler.endElement(uri, localName, qName);
                }
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(ALL_EXPENSES)) {
                elementHandled = true;
            } else if (!elementHandled) {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML tag '" + localName + "'.");
                chars.setLength(0);
            }
        }

        /**
         * Will add a list of cash entries to the list of expenses.
         * 
         * @param mobileEntries
         *            the list of cash entries.
         */
        private void addCashEntriesToExpenses(ArrayList<MobileEntry> mobileEntries) {
            if (mobileEntries != null) {
                Iterator<MobileEntry> iterator = mobileEntries.iterator();
                while (iterator.hasNext()) {
                    MobileEntry cashEntry = iterator.next();
                    Expense exp = new Expense(cashEntry);
                    expenses.add(exp);
                }
            }
        }

        /**
         * Will add a list of personal card entries to the list of expenses.
         * 
         * @param personalCards
         *            the list of personal card entries.
         */
        private void addPersonalCardEntriesToExpenses(ArrayList<PersonalCard> personalCards) {
            if (personalCards != null) {
                Iterator<PersonalCard> iterator = personalCards.iterator();
                while (iterator.hasNext()) {
                    PersonalCard personalCard = iterator.next();
                    // Iterate over all the personal card transactions
                    // and add an Expense for each cards transactions.
                    if (personalCard.transactions != null) {
                        Iterator<PersonalCardTransaction> transIterator = personalCard.transactions.iterator();
                        while (transIterator.hasNext()) {
                            PersonalCardTransaction perCardTrans = transIterator.next();
                            Expense exp = new Expense(personalCard, perCardTrans);
                            expenses.add(exp);
                        }
                    }
                }
            }
        }

        /**
         * Will add a list of corporate card transaction entries to the list of expenses.
         * 
         * @param transactions
         */
        private void addCorporateCardEntriesToExpenses(ArrayList<CorporateCardTransaction> transactions) {
            if (transactions != null) {
                Iterator<CorporateCardTransaction> iterator = transactions.iterator();
                while (iterator.hasNext()) {
                    CorporateCardTransaction corpCardTrans = iterator.next();
                    Expense exp = new Expense(corpCardTrans);
                    expenses.add(exp);
                }
            }

        }

        /**
         * Will add a list of receipt capture entries to the list of expenses.
         *
         * @param transactions
         */
        private void addReceiptCapturesToExpenses(ArrayList<ReceiptCapture> transactions) {
            if (transactions != null) {
                Iterator<ReceiptCapture> iterator = transactions.iterator();
                while (iterator.hasNext()) {
                    ReceiptCapture receiptCaptures = iterator.next();
                    Expense exp = new Expense(receiptCaptures);
                    expenses.add(exp);
                }
            }

        }

    }

    /**
     * Checks whether or not a card icon should be shown in the Expenses list.
     * 
     * @return shouldShowCardIcon is true if the icon should be shown.
     */
    public boolean shouldShowCardIcon() {
        return shouldShowCardIcon;
    }

    /**
     * Checks whether or not a receipt icon should be shown in the Expenses list.
     * 
     * @return shouldShowReceiptIcon is true if the icon should be shown.
     */
    public boolean shouldShowReceiptIcon() {
        return shouldShowReceiptIcon;
    }

    /**
     * @return whether or not this is a smart matched expense.
     */
    public boolean isSmartMatched() {
        return isSmartMatched;
    }
}
