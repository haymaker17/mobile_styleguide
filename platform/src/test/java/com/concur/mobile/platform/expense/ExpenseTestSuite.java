/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.expense;

import com.concur.mobile.platform.expense.list.test.ExpenseListRequestTaskTest;
import com.concur.mobile.platform.expense.list.test.SaveMobileEntryRequestTaskTest;
import com.concur.mobile.platform.expense.smartexpense.list.test.SmartExpenseListRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.DeleteReceiptRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.GetReceiptRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.ReceiptListRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.SaveReceiptRequestTaskTest;
import com.concur.mobile.platform.test.PlatformAsyncRequestTestUtil;

import org.junit.Test;

/**
 * Created by OlivierB on 20/08/2015.
 */
public class ExpenseTestSuite extends PlatformAsyncRequestTestUtil {

    @Override
    protected boolean useMockServer() {
        return true;
    }

    /**
     * Performs an expense list test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doExpenseList() throws Exception {
        doTest(new ExpenseListRequestTaskTest(useMockServer()));
    }

    /**
     * Performs a smart expense list test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doSmartExpenseList() throws Exception {
        doTest(new SmartExpenseListRequestTaskTest(useMockServer()));
    }

    /**
     * Performs a receipt list test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doReceiptList() throws Exception {
        ReceiptListRequestTaskTest receiptListTest = new ReceiptListRequestTaskTest(useMockServer());
        doTest(receiptListTest);

        // Run the GetReceiptRequestTask test with a receipt image id.
        GetReceiptRequestTaskTest getReceiptTest = new GetReceiptRequestTaskTest(useMockServer());
        getReceiptTest.setReceiptIdSource(GetReceiptRequestTaskTest.ReceiptIdSource.SOURCE_ID);
        doTest(getReceiptTest);

        // Run the GetReceiptRequestTask test with a receipt Uri.
        getReceiptTest = new GetReceiptRequestTaskTest(useMockServer());
        getReceiptTest.setReceiptIdSource(GetReceiptRequestTaskTest.ReceiptIdSource.SOURCE_URI);
        doTest(getReceiptTest);

        // Run the SaveReceiptRequestTask with just a receipt Uri.
        SaveReceiptRequestTaskTest saveReceiptTest = new SaveReceiptRequestTaskTest(useMockServer());
        saveReceiptTest.setReceiptSource(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_URI);
        // The Roboelectric ContentResolver current throws an UnsupportedException upon attempting to read
        // from a content Uri input stream!
        // saveReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with an input stream.
        saveReceiptTest = new SaveReceiptRequestTaskTest(useMockServer());
        saveReceiptTest.setReceiptSource(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_INPUT_STREAM);
        doTest(saveReceiptTest);

        // Run the SaveReceiptRequestTask with a byte array.
        saveReceiptTest = new SaveReceiptRequestTaskTest(useMockServer());
        saveReceiptTest.setReceiptSource(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_BYTE_ARRAY);
        doTest(saveReceiptTest);

        // Run the DeleteReceiptRequestTask with receipt uri.
        DeleteReceiptRequestTaskTest deleteReceiptTest = new DeleteReceiptRequestTaskTest(useMockServer());
        deleteReceiptTest.setReceiptSource(DeleteReceiptRequestTaskTest.ReceiptSource.SOURCE_URI);
        doTest(deleteReceiptTest);

        // Run the DeleteReceiptRequestTask with a receipt image id.
        deleteReceiptTest = new DeleteReceiptRequestTaskTest(useMockServer());
        deleteReceiptTest.setReceiptSource(DeleteReceiptRequestTaskTest.ReceiptSource.SOURCE_RECEIPT_IMAGE_ID);
        doTest(deleteReceiptTest);
    }

    /**
     * Performs save mobile entry test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doSaveMobileEntry() throws Exception {
        doTest(new SaveMobileEntryRequestTaskTest(useMockServer()));
    }
}
