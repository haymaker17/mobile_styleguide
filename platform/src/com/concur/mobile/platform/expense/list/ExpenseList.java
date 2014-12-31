/**
 * 
 */
package com.concur.mobile.platform.expense.list;

import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for parsing a reply to an all expenses request.
 * 
 * @author andrewk
 */
public class ExpenseList extends BaseParser {

    private static final String CLS_TAG = "ExpenseListParser";

    /**
     * Contains the start tag.
     */
    public static final String TAG_ALL_EXPENSES = "AllExpenses";

    /**
     * Contains the list of parsed expenses.
     */
    public List<Expense> expenses;

    /**
     * Contains the parsed list of corporate card transactions.
     */
    public List<CorporateCardTransaction> corporateCardTransactions;

    /**
     * Contains the parsed list of mobile entries.
     */
    public List<MobileEntry> entries;

    /**
     * Contains the parsed list of personal cards.
     */
    public List<PersonalCard> personalCards;

    /**
     * Contains the parsed list of receipt captures.
     */
    public List<ReceiptCapture> receiptCaptures;

    // Contains the list parser for corporate card transaction objects.
    private ListParser<CorporateCardTransaction> corpCardListParser;

    // Contains the list parser for mobile entry objects.
    private ListParser<MobileEntry> entriesListParser;

    // Contains the list parser for personal card objects.
    private ListParser<PersonalCard> personalCardListParser;

    // Contains the list parser for receipt capture objects.
    private ListParser<ReceiptCapture> receiptCaptureListParser;

    /**
     * Contains the start tag used to register this parser.
     */
    private String startTag;

    /**
     * Contains a reference to the common parser.
     */
    private CommonParser parser;

    private String corporateCardTransactionListTag = "CorporateCardTransactions";

    private String entriesListTag = "Entries";

    private String personalCardsListTag = "PersonalCards";

    private String receiptCaptureListTag = "ReceiptCaptures";

    /**
     * Constructs an instance of <code>ExpenseList</code>.
     * 
     * @param cp
     *            contains a reference to a common parser.
     * @param startTag
     *            contains the start tag.
     */
    public ExpenseList(CommonParser parser, String startTag) {

        // Set the start tag.
        this.startTag = startTag;

        // Set the parser reference.
        this.parser = parser;

        // Register the corporate card transaction list parser.
        corpCardListParser = new ListParser<CorporateCardTransaction>(parser, corporateCardTransactionListTag,
                CorporateCardTransaction.TAG_CORPORATE_CARD_TRANSACTION, CorporateCardTransaction.class);
        parser.registerParser(corpCardListParser, corporateCardTransactionListTag);

        // Register the mobile entry list parser.
        entriesListParser = new ListParser<MobileEntry>(entriesListTag, MobileEntry.TAG_MOBILE_ENTRY, MobileEntry.class);
        parser.registerParser(entriesListParser, entriesListTag);

        // Register the personal card list parser.
        personalCardListParser = new ListParser<PersonalCard>(parser, personalCardsListTag,
                PersonalCard.TAG_PERSONAL_CARD, PersonalCard.class);
        parser.registerParser(personalCardListParser, personalCardsListTag);

        // Register the receipt capture list parser.
        receiptCaptureListParser = new ListParser<ReceiptCapture>(receiptCaptureListTag,
                ReceiptCapture.TAG_RECEIPT_CAPTURE, ReceiptCapture.class);
        parser.registerParser(receiptCaptureListParser, receiptCaptureListTag);
    }

    @Override
    public void handleText(String tag, String text) {
        if (Const.DEBUG_PARSING) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
        }
    }

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {

                // Set the corporate card transaction list and unregister the parser.
                corporateCardTransactions = corpCardListParser.getList();
                parser.unregisterParser(corpCardListParser, corporateCardTransactionListTag);

                // Set the entries list and unregister the parser.
                entries = entriesListParser.getList();
                parser.unregisterParser(entriesListParser, entriesListTag);

                // Set the personal cards list and unregister the parser.
                personalCards = personalCardListParser.getList();
                parser.unregisterParser(personalCardListParser, personalCardsListTag);

                // Set the receipt capture list and unregister the parser.
                receiptCaptures = receiptCaptureListParser.getList();
                parser.unregisterParser(receiptCaptureListParser, receiptCaptureListTag);
            }
        }
    }

}
