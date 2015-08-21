package com.concur.mobile.platform.expense;

import com.concur.mobile.platform.expense.list.test.ExpenseListRequestTaskTest;
import com.concur.mobile.platform.expense.list.test.SaveMobileEntryRequestTaskTest;
import com.concur.mobile.platform.expense.smartexpense.list.test.SmartExpenseListRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.DeleteReceiptRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.GetReceiptRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.ReceiptListRequestTaskTest;
import com.concur.mobile.platform.receipt.list.test.SaveReceiptRequestTaskTest;
import com.concur.mobile.platform.test.ConcurPlatformTestRunner;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.concur.mobile.platform.test.PlatformTestSuite;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Created by OlivierB on 20/08/2015.
 */
@RunWith(ConcurPlatformTestRunner.class)
@Config(manifest = "src/test/AndroidManifest.xml", assetDir = "assets")
public class ExpenseTestSuite extends PlatformTestSuite {

    /**
     * Performs an expense list test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doExpenseList() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        ExpenseListRequestTaskTest expenseListTest = new ExpenseListRequestTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            expenseListTest.setMockServer(mwsServer);
        }

        // Run the ExpenseListRequestTask test.
        expenseListTest.doTest();
    }

    /**
     * Performs a smart expense list test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doSmartExpenseList() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        SmartExpenseListRequestTaskTest smartExpenseListTest = new SmartExpenseListRequestTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            smartExpenseListTest.setMockServer(mwsServer);
        }

        // Run the ExpenseListRequestTask test.
        smartExpenseListTest.doTest();
    }

    /**
     * Performs a receipt list test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doReceiptList() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        ReceiptListRequestTaskTest receiptListTest = new ReceiptListRequestTaskTest();
        boolean useMockServer = PlatformTestApplication.useMockServer();
        if (useMockServer) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            receiptListTest.setMockServer(mwsServer);
        }

        // Run the ReceiptListRequestTask test.
        receiptListTest.doTest();

        // Run the GetReceiptRequestTask test with a receipt image id.
        GetReceiptRequestTaskTest getReceiptTest = new GetReceiptRequestTaskTest(
                GetReceiptRequestTaskTest.ReceiptIdSource.SOURCE_ID);
        if (useMockServer) {
            getReceiptTest.setMockServer(mwsServer);
        }
        getReceiptTest.doTest();

        // Run the GetReceiptRequestTask test with a receipt Uri.
        getReceiptTest = new GetReceiptRequestTaskTest(GetReceiptRequestTaskTest.ReceiptIdSource.SOURCE_URI);
        if (useMockServer) {
            getReceiptTest.setMockServer(mwsServer);
        }
        getReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with just a receipt Uri.
        SaveReceiptRequestTaskTest saveReceiptTest = new SaveReceiptRequestTaskTest(
                SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_URI);
        if (useMockServer) {
            saveReceiptTest.setMockServer(mwsServer);
        }
        // The Roboelectric ContentResolver current throws an UnsupportedException upon attempting to read
        // from a content Uri input stream!
        // saveReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with an input stream.
        saveReceiptTest = new SaveReceiptRequestTaskTest(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_INPUT_STREAM);
        if (useMockServer) {
            saveReceiptTest.setMockServer(mwsServer);
        }
        saveReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with a byte array.
        saveReceiptTest = new SaveReceiptRequestTaskTest(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_BYTE_ARRAY);
        if (useMockServer) {
            saveReceiptTest.setMockServer(mwsServer);
        }
        saveReceiptTest.doTest();

        // Run the DeleteReceiptRequestTask with receipt uri.
        DeleteReceiptRequestTaskTest deleteReceiptTest = new DeleteReceiptRequestTaskTest(
                DeleteReceiptRequestTaskTest.ReceiptSource.SOURCE_URI);
        if (useMockServer) {
            deleteReceiptTest.setMockServer(mwsServer);
        }
        deleteReceiptTest.doTest();

        // Run the DeleteReceiptRequestTask with a receipt image id.
        deleteReceiptTest = new DeleteReceiptRequestTaskTest(
                DeleteReceiptRequestTaskTest.ReceiptSource.SOURCE_RECEIPT_IMAGE_ID);
        if (useMockServer) {
            deleteReceiptTest.setMockServer(mwsServer);
        }
        deleteReceiptTest.doTest();

    }

    /**
     * Performs save mobile entry test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doSaveMobileEntry() throws Exception {

        // Init the login request
        doPinPasswordLogin();

        SaveMobileEntryRequestTaskTest saveMETest = new SaveMobileEntryRequestTaskTest();
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();

            // Set the mock server instance on the test.
            saveMETest.setMockServer(mwsServer);
        }

        // Run the SaveMobileEntryRequestTask test.
        saveMETest.doTest();
    }
}
