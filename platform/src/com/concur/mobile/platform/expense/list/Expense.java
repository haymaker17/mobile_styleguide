package com.concur.mobile.platform.expense.list;

import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.expense.list.dao.CorporateCardTransactionDAO;
import com.concur.mobile.platform.expense.list.dao.ExpenseDAO;
import com.concur.mobile.platform.expense.list.dao.MobileEntryDAO;
import com.concur.mobile.platform.expense.list.dao.PersonalCardTransactionDAO;
import com.concur.mobile.platform.expense.list.dao.ReceiptCaptureDAO;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;

/**
 * An expense representing either a card or cash expense.
 * 
 * @author AndrewK
 */
public class Expense implements ExpenseDAO {

    private static final String CLS_TAG = Expense.class.getSimpleName();

    /**
     * Contains whether expense list related functioning should generate debug statements.
     */
    static final boolean DEBUG = true;

    /**
     * Contains the full list of columns to select when loading an expense from the expense content provider.
     */
    public static final String[] fullColumnList = {
            com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.TYPE,
            com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.TRANSACTION_AMOUNT,
            com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.TRANSACTION_CRN_CODE,
            com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.TRANSACTION_DATE,
            com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.VENDOR_NAME,
            com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.EXP_ID,
            com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.USER_ID };

    /**
     * Contains the expense type.
     */
    private ExpenseTypeEnum type;

    /**
     * Contains the transaction amount.
     */
    private Double transactionAmount;

    /**
     * Contains the transaction currency code.
     */
    private String transactionCrnCode;

    /**
     * Contains the transaction date.
     */
    private Calendar transactionDate;

    /**
     * Contains the vendor name.
     */
    private String vendorName;

    /**
     * Contains the expense id.
     */
    private Long expenseId;

    /**
     * Contains the user id.
     */
    private String userId;

    /**
     * Contains the cash transaction.
     */
    private MobileEntry cashTransaction;

    /**
     * Contains the personal card transaction.
     */
    private PersonalCardTransaction personalCardTransaction;

    /**
     * Contains the corporate card transaction.
     */
    private CorporateCardTransaction corporateCardTransaction;

    /**
     * Contains the receipt capture.
     */
    private ReceiptCapture receiptCapture;

    /**
     * Contains a reference to the application context.
     */
    private Context context;

    public Expense(Context context, Cursor cursor) {
        this.context = context;

        // Type name.
        String typeName = CursorUtil.getStringValue(cursor,
                com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.TYPE);
        if (!TextUtils.isEmpty(typeName)) {
            type = ExpenseTypeEnum.valueOf(typeName);
        }
        // Transaction amount.
        transactionAmount = CursorUtil.getDoubleValue(cursor,
                com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.TRANSACTION_AMOUNT);
        // Transaction currency code.
        transactionCrnCode = CursorUtil.getStringValue(cursor,
                com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.TRANSACTION_CRN_CODE);
        // Transaction date.
        Long transDateMillis = CursorUtil.getLongValue(cursor,
                com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.TRANSACTION_DATE);
        if (transDateMillis != null) {
            transactionDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            transactionDate.setTimeInMillis(transDateMillis);
            transactionDate.set(Calendar.MILLISECOND, 0);
        }
        // Vendor name.
        vendorName = CursorUtil.getStringValue(cursor,
                com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.VENDOR_NAME);
        // Expense id.
        expenseId = CursorUtil.getLongValue(cursor,
                com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.EXP_ID);
        // User Id.
        userId = CursorUtil.getStringValue(cursor,
                com.concur.mobile.platform.expense.provider.Expense.ExpenseColumns.USER_ID);
    }

    /**
     * Constructs a new instance of <code>Expense</code> representing a cash transaction.
     * 
     * @param cashTransaction
     *            the cash transaction.
     */
    public Expense(MobileEntry cashTransaction) {
        type = ExpenseTypeEnum.CASH;
        this.cashTransaction = cashTransaction;
    }

    // Start ExpenseDAO methods.

    @Override
    public ExpenseTypeEnum getType() {
        return type;
    }

    @Override
    public Double getTransactionAmount() {
        return transactionAmount;
    }

    @Override
    public String getTransactionCurrencyCode() {
        return transactionCrnCode;
    }

    @Override
    public Calendar getTransactionDate() {
        return transactionDate;
    }

    @Override
    public String getVendorName() {
        return vendorName;
    }

    @Override
    public Long getExpenseId() {
        return expenseId;
    }

    @Override
    public void split() {
        try {
            Assert.assertNotNull("expense type is null", getType());
            Assert.assertTrue("expense type is not smart corporate/personal",
                    ((getType() == ExpenseTypeEnum.SMART_CORPORATE) || (getType() == ExpenseTypeEnum.SMART_PERSONAL)));

            if (getType() == ExpenseTypeEnum.SMART_CORPORATE) {
                // Convert the expense into a non-smart corporate card transaction.
                CorporateCardTransaction corpCardTrans = getCorporateCardTransaction();
                Assert.assertNotNull("corporate card part of smart corporate transaction is invalid", corpCardTrans);
                corpCardTrans.split = true;
                corpCardTrans.type = ExpenseTypeEnum.CORPORATE_CARD;
                corpCardTrans.update(context, userId, false);
            } else {
                // Convert the expense into a non-smart personal card transaction.
                PersonalCardTransaction persCardTrans = getPersonalCardTransaction();
                Assert.assertNotNull("personal card part of smart personal transaction is invalid", persCardTrans);
                persCardTrans.split = true;
                persCardTrans.type = ExpenseTypeEnum.PERSONAL_CARD;
                persCardTrans.update(context, userId, false);
            }
        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".split: " + afe.getMessage());
            throw new IllegalArgumentException(afe.getMessage());
        }
    }

    /**
     * Gets the cash transaction associated with this expense.
     * 
     * @return the cash transaction associated with this expense.
     */
    MobileEntry getCashTransaction() {

        if (cashTransaction == null) {
            try {
                Assert.assertNotNull("expense type is null", getType());
                Assert.assertNotNull("expense id is null", getExpenseId());
                Assert.assertTrue("expense type is not cash", (getType() == ExpenseTypeEnum.CASH));

                Uri mobEntUri = ContentUris.withAppendedId(
                        com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.CONTENT_URI,
                        getExpenseId());
                Assert.assertTrue("expenseId is invalid", ContentUtils.uriExists(context, mobEntUri));

                cashTransaction = new MobileEntry(context, mobEntUri);
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getCashTransaction: " + afe.getMessage());
            }
        }
        return cashTransaction;
    }

    @Override
    public MobileEntryDAO getMobileEntryDAO() {
        return getCashTransaction();
    }

    /**
     * Gets the receipt capture associated with this expense.
     * 
     * @return the receipt capture associated with this expense.
     */
    ReceiptCapture getReceiptCapture() {

        if (receiptCapture == null) {

            try {
                Assert.assertNotNull("expense type is null", getType());
                Assert.assertNotNull("expense id is null", getExpenseId());
                Assert.assertTrue("expense type is not receipt capture", (getType() == ExpenseTypeEnum.RECEIPT_CAPTURE));

                Uri recCapUri = ContentUris.withAppendedId(
                        com.concur.mobile.platform.expense.provider.Expense.ReceiptCaptureColumns.CONTENT_URI,
                        getExpenseId());
                Assert.assertTrue("expenseId is invalid", ContentUtils.uriExists(context, recCapUri));
                receiptCapture = new ReceiptCapture(context, recCapUri);
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getReceiptCapture: " + afe.getMessage());
            }
        }
        return receiptCapture;
    }

    @Override
    public ReceiptCaptureDAO getReceiptCaptureDAO() {
        return getReceiptCapture();
    }

    /**
     * Gets the personal card transaction associated with this expense.
     * 
     * @return the personal card transaction associated with this expense.
     */
    PersonalCardTransaction getPersonalCardTransaction() {
        if (personalCardTransaction == null) {
            try {
                Assert.assertNotNull("expense type is null", getType());
                Assert.assertNotNull("expense id is null", getExpenseId());
                Assert.assertTrue("expense type is not personal card",
                        (getType() == ExpenseTypeEnum.PERSONAL_CARD || getType() == ExpenseTypeEnum.SMART_PERSONAL));

                Uri persCardTransUri = ContentUris.withAppendedId(
                        com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.CONTENT_URI,
                        getExpenseId());
                Assert.assertTrue("expenseId is invalid", ContentUtils.uriExists(context, persCardTransUri));
                personalCardTransaction = new PersonalCardTransaction(context, persCardTransUri);
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getPersonalCardTransaction: " + afe.getMessage());
            }
        }
        return personalCardTransaction;
    }

    @Override
    public PersonalCardTransactionDAO getPersonalCardTransactionDAO() {
        return getPersonalCardTransaction();
    }

    /**
     * Gets the corporate card transaction associated with this expense.
     * 
     * @return the corporate card transaction associated with this expense.
     */
    CorporateCardTransaction getCorporateCardTransaction() {
        if (corporateCardTransaction == null) {

            try {
                Assert.assertNotNull("expense type is null", getType());
                Assert.assertNotNull("expense id is null", getExpenseId());
                Assert.assertTrue("expense type is not corporate card",
                        (getType() == ExpenseTypeEnum.CORPORATE_CARD || getType() == ExpenseTypeEnum.SMART_CORPORATE));

                Uri corpCardTransUri = ContentUris
                        .withAppendedId(
                                com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.CONTENT_URI,
                                getExpenseId());
                Assert.assertTrue("expenseId is invalid", ContentUtils.uriExists(context, corpCardTransUri));
                corporateCardTransaction = new CorporateCardTransaction(context, corpCardTransUri);
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getCorporateCardTransaction: " + afe.getMessage());
            }
        }
        return corporateCardTransaction;
    }

    @Override
    public CorporateCardTransactionDAO getCorporateCardTransactionDAO() {
        return getCorporateCardTransaction();
    }

    @Override
    public boolean update(Context context, String userId) {

        boolean retVal = true;

        // Call update on any related items.
        switch (type) {
        case CASH: {
            if (cashTransaction != null) {
                if (!cashTransaction.update(context, userId)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".update: failed to update cash transaction.");
                    retVal = false;
                }
            }
            break;
        }
        case CORPORATE_CARD: {
            if (corporateCardTransaction != null) {
                if (!corporateCardTransaction.update(context, userId)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".update: failed to update corporate card transaction.");
                    retVal = false;
                }
            }
            break;
        }
        case PERSONAL_CARD: {
            if (personalCardTransaction != null) {
                if (!personalCardTransaction.update(context, userId)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".update: failed to update personal card transaction.");
                    retVal = false;
                }
            }
            break;
        }
        case SMART_CORPORATE: {
            if (corporateCardTransaction != null) {
                if (!corporateCardTransaction.update(context, userId)) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".update: failed to update corporate card part of smart-corporate transaction.");
                    retVal = false;
                }
            }
            if (cashTransaction != null) {
                if (!cashTransaction.update(context, userId)) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".update: failed to update cash part of smart-corporate transaction.");
                    retVal = false;
                }
            }
            break;
        }
        case SMART_PERSONAL: {
            if (personalCardTransaction != null) {
                if (!personalCardTransaction.update(context, userId)) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".update: failed to update personal card part of smart-personal transaction.");
                    retVal = false;
                }
            }
            if (cashTransaction != null) {
                if (!cashTransaction.update(context, userId)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".update: failed to update cash part of smart-personal transaction.");
                    retVal = false;
                }
            }
            break;
        }
        case RECEIPT_CAPTURE: {
            if (receiptCapture != null) {
                if (!receiptCapture.update(context, userId)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".update: failed to update receipt capture.");
                    retVal = false;
                }
            }
            break;
        }
        }

        return retVal;
    }

    // End ExpenseDAO methods.

}
