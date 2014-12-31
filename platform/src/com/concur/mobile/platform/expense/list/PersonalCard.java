package com.concur.mobile.platform.expense.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.expense.list.dao.PersonalCardDAO;
import com.concur.mobile.platform.expense.list.dao.PersonalCardTransactionDAO;
import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;
import com.google.gson.annotations.SerializedName;

/**
 * An extension of <code>BaseParser</code> for parsing a personal card.
 * 
 * @author andrewk
 */
public class PersonalCard extends BaseParser implements PersonalCardDAO {

    private static final String CLS_TAG = "PersonalCard";

    public static final String TAG_PERSONAL_CARD = "PersonalCard";

    public static String[] fullColumnList = { Expense.PersonalCardColumns._ID, Expense.PersonalCardColumns.PCA_KEY,
            Expense.PersonalCardColumns.CARD_NAME, Expense.PersonalCardColumns.ACCT_NUM_LAST_FOUR,
            Expense.PersonalCardColumns.CRN_CODE, Expense.PersonalCardColumns.TAG };

    // tags.
    private static final String TAG_PCA_KEY = "PcaKey";
    private static final String TAG_CARD_NAME = "CardName";
    private static final String TAG_CRN_CODE = "CrnCode";
    private static final String TAG_ACCOUNT_NUMBER_LAST_FOUR = "AccountNumberLastFour";

    // tag codes.
    private static final int TAG_PCA_KEY_CODE = 0;
    private static final int TAG_CARD_NAME_CODE = 1;
    private static final int TAG_CRN_CODE_CODE = 2;
    private static final int TAG_ACCOUNT_NUMBER_LAST_FOUR_CODE = 3;

    // Contains the map from tags to codes.
    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_PCA_KEY, TAG_PCA_KEY_CODE);
        tagMap.put(TAG_CARD_NAME, TAG_CARD_NAME_CODE);
        tagMap.put(TAG_CRN_CODE, TAG_CRN_CODE_CODE);
        tagMap.put(TAG_ACCOUNT_NUMBER_LAST_FOUR, TAG_ACCOUNT_NUMBER_LAST_FOUR_CODE);
    }

    transient public List<PersonalCardTransaction> transactions;

    /**
     * Contains the personal card account key.
     */
    String pcaKey;

    /**
     * Contains the card name.
     */
    String cardName;

    /**
     * Contains the account number last four digits
     */
    @SerializedName("accountNumberLastFour")
    String acctNumLastFour;

    /**
     * Contains the currency code.
     */
    String crnCode;

    /**
     * Contains a tag.
     */
    transient String tag;

    /**
     * Contains the content Uri.
     */
    transient Uri contentUri;

    /**
     * Contains a reference to an application context.
     */
    transient Context context;

    // Contains the list parser for a personal card transaction.
    private transient ListParser<PersonalCardTransaction> personalCardTransactionListParser;

    private transient String startTag;

    private transient CommonParser parser;

    private transient String personalCardTransactionListTag = "Transactions";

    public PersonalCard() {
    }

    /**
     * Constructs an instance of <code>PersonalCard</code> given a context and an Uri.
     * 
     * @param context
     *            contains an application context.
     * @param contentUri
     *            contains the content uri.
     */
    public PersonalCard(Context context, Uri contentUri) {
        this.context = context;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(contentUri, fullColumnList, null, null,
                    Expense.PersonalCardColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    init(cursor);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Constructs an instance of <code>PersonalCard</code> based on reading values from a <code>Cursor</code> object.
     * 
     * @param context
     *            TODO
     * @param cursor
     *            contains the cursor.
     */
    public PersonalCard(Context context, Cursor cursor) {
        this.context = context;
        init(cursor);
    }

    /**
     * Constructs an instance of <code>PersonalCard</code> for use with parsing.
     * 
     * @param parser
     *            contains a reference to a common parser.
     * @param startTag
     *            contains the start tag for this parser.
     */
    public PersonalCard(CommonParser parser, String startTag) {

        // Set the parser.
        this.parser = parser;

        // Set the start tag.
        this.startTag = startTag;

        // Register the list of personal card transactions parser.
        personalCardTransactionListParser = new ListParser<PersonalCardTransaction>(parser,
                personalCardTransactionListTag, PersonalCardTransaction.TAG_PERSONAL_CARD_TRANSACTION,
                PersonalCardTransaction.class);
        parser.registerParser(personalCardTransactionListParser, personalCardTransactionListTag);
    }

    /**
     * Will initialize this <code>PersonalCard</code> object given information in <code>cursor</code>.
     * 
     * @param cursor
     *            contains the cursor.
     */
    private void init(Cursor cursor) {

        pcaKey = CursorUtil.getStringValue(cursor, Expense.PersonalCardColumns.PCA_KEY);
        cardName = CursorUtil.getStringValue(cursor, Expense.PersonalCardColumns.CARD_NAME);
        acctNumLastFour = CursorUtil.getStringValue(cursor, Expense.PersonalCardColumns.ACCT_NUM_LAST_FOUR);
        crnCode = CursorUtil.getStringValue(cursor, Expense.PersonalCardColumns.CRN_CODE);
        tag = CursorUtil.getStringValue(cursor, Expense.PersonalCardColumns.TAG);

        // Set the content id.
        Long contentId = CursorUtil.getLongValue(cursor, Expense.PersonalCardColumns._ID);
        if (contentId != null) {
            contentUri = ContentUris.withAppendedId(Expense.PersonalCardColumns.CONTENT_URI, contentId);
        }
    }

    @Override
    public void handleText(String tag, String text) {

        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            if (text != null) {
                switch (tagCode) {
                case TAG_ACCOUNT_NUMBER_LAST_FOUR_CODE: {
                    acctNumLastFour = text.trim();
                    break;
                }
                case TAG_CARD_NAME_CODE: {
                    cardName = text.trim();
                    break;
                }
                case TAG_CRN_CODE_CODE: {
                    crnCode = text.trim();
                    break;
                }
                case TAG_PCA_KEY_CODE: {
                    pcaKey = text.trim();
                    break;
                }
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
            }
        }

    }

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {

                // Set the list of transactions and unregister the parser.
                transactions = personalCardTransactionListParser.getList();
                parser.unregisterParser(personalCardTransactionListParser, personalCardTransactionListTag);

                // Post process the personal card transactions.
                if (transactions != null) {
                    for (PersonalCardTransaction pct : transactions) {
                        // Set the currency code on the transaction.
                        pct.crnCode = crnCode;
                        // Set the pct, pca and type on any associated mobile entry.
                        if (pct.getMobileEntryDAO() != null) {
                            // Set the personal card transaction key.
                            pct.getMobileEntryDAO().setPctKey(pct.getPctKey());
                            // Set the personal card account key.
                            pct.getMobileEntryDAO().setPcaKey(pcaKey);
                            // Set the type on the mobile entry.
                            pct.getMobileEntryDAO().setEntryType(ExpenseTypeEnum.PERSONAL_CARD);
                        }
                    }
                }
            }
        }
    }

    // Start DAO methods.

    @Override
    public String getPCAKey() {
        return pcaKey;
    }

    @Override
    public void setPCAKey(String pcaKey) {
        this.pcaKey = pcaKey;
    }

    @Override
    public String getCardName() {
        return cardName;
    }

    @Override
    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    @Override
    public String getAcctNumLastFour() {
        return acctNumLastFour;
    }

    @Override
    public void setAcctNumLastFour(String acctNumLastFour) {
        this.acctNumLastFour = acctNumLastFour;
    }

    @Override
    public String getCrnCode() {
        return crnCode;
    }

    @Override
    public void setCrnCode(String crnCode) {
        this.crnCode = crnCode;
    }

    List<PersonalCardTransaction> getPersonalCardTransactions() {
        if (transactions == null) {
            ContentResolver resolver = context.getContentResolver();
            getContentURI(context);
            Long contentId = ContentUris.parseId(contentUri);
            Cursor cursor = null;
            try {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.PERSONAL_CARD_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { Long.toString(contentId) };

                cursor = resolver
                        .query(com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.CONTENT_URI,
                                PersonalCardTransaction.fullColumnList,
                                where,
                                whereArgs,
                                com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        transactions = new ArrayList<PersonalCardTransaction>(cursor.getCount());
                        do {
                            PersonalCardTransaction persCardTrans = new PersonalCardTransaction(context, cursor);
                            transactions.add(persCardTrans);
                        } while (cursor.moveToNext());
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return transactions;
    }

    @Override
    public List<PersonalCardTransactionDAO> getPersonalCardTransactionDAOS() {
        List<PersonalCardTransactionDAO> trans = null;
        List<PersonalCardTransaction> persCardTransList = getPersonalCardTransactions();
        if (persCardTransList != null) {
            trans = new ArrayList<PersonalCardTransactionDAO>(persCardTransList.size());
            for (PersonalCardTransaction pct : persCardTransList) {
                trans.add(pct);
            }
        }
        return trans;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean update(Context context, String userId) {
        boolean retVal = true;

        ContentResolver resolver = context.getContentResolver();

        // Build the content values object for insert/update.
        ContentValues values = new ContentValues();
        ContentUtils.putValue(values, Expense.PersonalCardColumns.PCA_KEY, pcaKey);
        ContentUtils.putValue(values, Expense.PersonalCardColumns.CARD_NAME, cardName);
        ContentUtils.putValue(values, Expense.PersonalCardColumns.ACCT_NUM_LAST_FOUR, acctNumLastFour);
        ContentUtils.putValue(values, Expense.PersonalCardColumns.CRN_CODE, crnCode);
        ContentUtils.putValue(values, Expense.PersonalCardColumns.TAG, tag);
        ContentUtils.putValue(values, Expense.PersonalCardColumns.USER_ID, userId);

        // Grab the content URI if any.
        Uri persCardUri = getContentURI(context);

        if (persCardUri != null) {
            // Perform an update.
            int rowsUpdated = resolver.update(persCardUri, values, null, null);
            if (rowsUpdated == 0) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".update: 0 rows updated for Uri '" + persCardUri.toString() + "'.");
                // Perform an insertion.
                contentUri = resolver.insert(Expense.PersonalCardColumns.CONTENT_URI, values);
                retVal = (contentUri != null);
            } else {
                retVal = true;
                if (rowsUpdated > 1) {
                    Log.w(Const.LOG_TAG,
                            CLS_TAG + ".update: more than 1 row updated for Uri '" + persCardUri.toString() + "'.");
                }
            }
            retVal = (rowsUpdated == 1);
        } else {
            // Perform an insertion.
            contentUri = resolver.insert(Expense.PersonalCardColumns.CONTENT_URI, values);
            retVal = (contentUri != null);
        }

        // Obtain the content id for the pers card.
        try {
            Long contentId = ContentUris.parseId(contentUri);
            if (contentId != -1L) {
                if (transactions != null) {
                    for (PersonalCardTransaction persCardTrans : transactions) {
                        persCardTrans.personalCardId = contentId;
                        if (!persCardTrans.update(context, userId)) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".update: unable to update personal card transaction '"
                                    + persCardTrans.pctKey + "'.");
                        }
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".update: path is empty: '" + persCardUri.toString() + "'.");
            }
        } catch (UnsupportedOperationException unsupOpExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".update: Uri is non-hierarchical: '" + persCardUri.toString() + "'.",
                    unsupOpExc);
        } catch (NumberFormatException numFormExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".update: last-path element is not a number: '" + persCardUri.toString()
                    + "'.", numFormExc);
        }
        return retVal;
    }

    @Override
    public Uri getContentURI(Context context) {
        if (contentUri == null) {
            if (!TextUtils.isEmpty(pcaKey)) {
                contentUri = ContentUtils.getContentUri(context, Expense.PersonalCardColumns.CONTENT_URI,
                        Expense.PersonalCardColumns.PCA_KEY, pcaKey);
            }
        }
        return contentUri;
    }

    @Override
    public void setContentURI(Uri contentUri) {
        this.contentUri = contentUri;
    }

    // End DAO methods.

    /**
     * Will reconcile a list of <code>PersonalCard</code> objects with entries in the expense database punting any entries in the
     * database with non-null keys that are not contained within <code>persCards</code>.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     * @param persCards
     *            contains a list of <code>PersonalCard</code> objects.
     */
    public static void reconcile(Context context, String userId, List<PersonalCard> persCards) {

        // First, build a simple map to permit quick look-up based on PCA key.
        Map<String, PersonalCard> pcKeyMap = new HashMap<String, PersonalCard>((persCards != null) ? persCards.size()
                : 0);
        if (persCards != null) {
            for (PersonalCard persCard : persCards) {
                pcKeyMap.put(persCard.pcaKey, persCard);
            }
        }
        // Second, read in content id and PCA_KEY.
        List<Long> idsToBePunted = new ArrayList<Long>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] pcaColumns = { Expense.PersonalCardColumns._ID, Expense.PersonalCardColumns.PCA_KEY };
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.PersonalCardColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Expense.PersonalCardColumns.CONTENT_URI, pcaColumns, where, whereArgs,
                    Expense.PersonalCardColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Long contentId = CursorUtil.getLongValue(cursor, Expense.PersonalCardColumns._ID);
                        String pcaKey = CursorUtil.getStringValue(cursor, Expense.PersonalCardColumns.PCA_KEY);
                        if (!TextUtils.isEmpty(pcaKey) && !pcKeyMap.containsKey(pcaKey)) {
                            idsToBePunted.add(contentId);
                        }
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Third, punt cards.
        for (Long contId : idsToBePunted) {
            Uri contUri = ContentUris.withAppendedId(Expense.PersonalCardColumns.CONTENT_URI, contId);
            int rowsAffected = resolver.delete(contUri, null, null);
            if (rowsAffected != 1) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".reconcile: 0 rows affected for deletion of '" + contUri.toString()
                        + "'.");
            }
        }

    }

}
