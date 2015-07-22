package com.concur.mobile.core.expense.fragment;

import com.concur.mobile.platform.expenseit.ExpenseItParseCode;
import com.concur.mobile.platform.expenseit.ExpenseItPostReceipt;
import com.concur.mobile.platform.expenseit.ExpenseItReceipt;

import java.util.GregorianCalendar;
import java.util.HashSet;

/**
 * @author Elliott Jacobsen-Watts
 */
public class ExpenseItReceiptUiStatusTest {

    private HashSet<ExpenseItReceipt> mExpenseItReceipts = new HashSet<>();

    public ExpenseItReceiptUiStatusTest() {
        buildReceipts();
    }

    public HashSet<ExpenseItReceipt> getExpenseItReceipts() {
        return mExpenseItReceipts;
    }

    private void buildReceipts() {
        ExpenseItReceipt receipt;
        for (int i = 0; i < 11; ++i) {
            receipt = new ExpenseItReceipt();
            receipt.setEta(120 + (i * 10));
            switch(i) {
                case 0:
                    // error, despite valid parsing code.
                    receipt.setCreatedAt(new GregorianCalendar(2016,0,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.PARSED.value());
                    receipt.setErrorCode(ExpenseItPostReceipt.RUBICON_ERROR);
                    break;
                case 1:
                    // error.
                    receipt.setCreatedAt(new GregorianCalendar(2016,1,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.EXPIRED.value());
                    break;
                case 2:
                    // error.
                    receipt.setCreatedAt(new GregorianCalendar(2016,2,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.OTHER.value());
                    break;
                case 3:
                    // error.
                    receipt.setCreatedAt(new GregorianCalendar(2016,3,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.NO_IMAGE_FOUND.value());
                    break;
                case 4:
                    // processing.
                    receipt.setCreatedAt(new GregorianCalendar(2015,0,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.UNPARSED.value());
                    break;
                case 5:
                    // processing.
                    receipt.setCreatedAt(new GregorianCalendar(2015,1,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.UPLOADING_IN_PROGRESS.value());
                    break;
                case 6:
                    // processing.
                    receipt.setCreatedAt(new GregorianCalendar(2015,2,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.UPLOADED_BUT_NOT_QUEUED.value());
                    break;
                case 7:
                    // processing.
                    receipt.setCreatedAt(new GregorianCalendar(2015,3,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.PARSED.value());
                    break;
                case 8:
                    // processing.
                    receipt.setCreatedAt(new GregorianCalendar(2015,4,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.SUCCESS_HIDDEN.value());
                    break;
                case 9:
                    // processing.
                    receipt.setCreatedAt(new GregorianCalendar(2015,5,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.SUCCESS_VISIBLE.value());
                    break;
                case 10:
                    // processing.
                    receipt.setCreatedAt(new GregorianCalendar(2015,6,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.EXPORTED.value());
                    break;
                default:
                    receipt.setCreatedAt(new GregorianCalendar(2015,7,1));
                    receipt.setParsingStatusCode(ExpenseItParseCode.OTHER.value());
                    break;

            }
            mExpenseItReceipts.add(receipt);
            // 7 processing.
            // 4 error. no ETA.
        }
    }
}
