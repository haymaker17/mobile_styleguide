package com.concur.mobile.core.expense.charge.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.text.TextUtils;

import com.concur.mobile.core.expense.charge.data.MobileEntry.MobileEntrySAXHandler;
import com.concur.mobile.platform.expense.smartexpense.dao.SmartExpenseDAO;
import com.concur.mobile.platform.util.Parse;

public class ReceiptCapture implements Serializable {

    private static final long serialVersionUID = -2291335741104696001L;

    private static String CLS_TAG = ReceiptCapture.class.getSimpleName();

    /**
     * Contains the transaction currency code.
     */
    public String crnCode;

    /**
     * Contains the expense entry type key.
     */
    public String expKey;

    /**
     * Contains the expense name.
     */
    public String expName;

    /**
     * Contains the expense vendor name.
     */
    public String vendorName;

    /**
     * Contains the receipt capture expense key
     */
    public String rcKey;

    /**
     * Contains smart expense id which is same as rcKey
     */
    public String smartExpId;

    /**
     * Contains the expense transaction amount.
     */
    public Double transactionAmount;

    /**
     * Contains the expense transaction date.
     */
    public Calendar transactionDate;

    /**
     * Contains the receipt image id.
     */
    public String receiptImageId;

    /**
     * Contains the smart expense id
     */
    public String smartExpenseId;

    public ReceiptCapture() {
    }

    public ReceiptCapture(SmartExpenseDAO smartExpense) {
        crnCode = smartExpense.getCrnCode();
        expKey = smartExpense.getExpKey();
        expName = smartExpense.getExpenseName();
        vendorName = smartExpense.getMerchantName(); // VenLiName or MerchantName?
        rcKey = smartExpense.getRcKey();
        smartExpId = smartExpense.getSmartExpenseId();
        transactionAmount = smartExpense.getTransactionAmount();
        transactionDate = smartExpense.getTransactionDate();
        smartExpenseId = smartExpense.getSmartExpenseId();

        if (!TextUtils.isEmpty(smartExpense.getMobileReceiptImageId())) {
            receiptImageId = smartExpense.getMobileReceiptImageId();
        } else if (!TextUtils.isEmpty(smartExpense.getEReceiptImageId())) {
            receiptImageId = smartExpense.getEReceiptImageId();
        } else if (!TextUtils.isEmpty(smartExpense.getCctReceiptImageId())) {
            receiptImageId = smartExpense.getCctReceiptImageId();
        } else if (!TextUtils.isEmpty(smartExpense.getReceiptImageId())) {
            receiptImageId = smartExpense.getReceiptImageId();
        }
    }

    protected static class ReceiptCaptureSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = ReceiptCapture.CLS_TAG + "."
                + MobileEntrySAXHandler.class.getSimpleName();

        public static final String RECEIPT_CAPTURE = "ReceiptCapture";

        private static final String CRN_CODE = "CrnCode";

        private static final String EXP_KEY = "ExpKey";

        private static final String EXP_NAME = "ExpName";

        private static final String RECEIPT_IMAGE_ID = "ReceiptImageId";

        private static final String RC_KEY = "RcKey";

        private static final String TRANSACTION_AMOUNT = "TransactionAmount";

        private static final String TRANSACTION_DATE = "TransactionDate";

        private static final String SMRT_EXP_ID = "SmartExpenseId";

        private static final String VENDOR_NAME = "VendorName";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        /**
         * Contains a reference to a list of <code>ReceiptCaptures</code> objects that have been parsed.
         */
        private ArrayList<ReceiptCapture> listOfReceiptCaptures = new ArrayList<ReceiptCapture>();

        /**
         * Contains a reference to the report comment currently being built.
         */
        ReceiptCapture receiptCaptures;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * A reference to a context object used to construct a local file.
         */
        Context context;

        /**
         * Gets the list of <code>ReceiptCaptures</code> objects that have been parsed.
         * 
         * @return the list of parsed <code>ReceiptCaptures</code> objects.
         */
        ArrayList<ReceiptCapture> getReceiptCaptures() {
            return listOfReceiptCaptures;
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

            elementHandled = false;

            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(RECEIPT_CAPTURE)) {
                receiptCaptures = new ReceiptCapture();
                chars.setLength(0);
                elementHandled = true;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            elementHandled = false;

            super.endElement(uri, localName, qName);

            String cleanChars = chars.toString().trim();
            handleElement(localName, cleanChars);

            // Clear out the stored element values.
            chars.setLength(0);
        }

        void handleElement(String localName, String cleanChars) {

            if (receiptCaptures != null) {
                if (localName.equalsIgnoreCase(CRN_CODE)) {
                    receiptCaptures.crnCode = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(EXP_KEY)) {
                    receiptCaptures.expKey = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(EXP_NAME)) {
                    receiptCaptures.expName = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(RECEIPT_IMAGE_ID)) {
                    receiptCaptures.receiptImageId = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(RC_KEY)) {
                    receiptCaptures.rcKey = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(TRANSACTION_AMOUNT)) {
                    receiptCaptures.transactionAmount = Parse.safeParseDouble(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(TRANSACTION_DATE)) {
                    receiptCaptures.transactionDate = Parse.parseXMLTimestamp(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(SMRT_EXP_ID)) {
                    receiptCaptures.smartExpId = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(VENDOR_NAME)) {
                    receiptCaptures.vendorName = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(RECEIPT_CAPTURE)) {
                    listOfReceiptCaptures.add(receiptCaptures);
                    receiptCaptures = null;
                    elementHandled = true;
                } else {
                    // Do nothing.
                }
            }
        }

    }
}
