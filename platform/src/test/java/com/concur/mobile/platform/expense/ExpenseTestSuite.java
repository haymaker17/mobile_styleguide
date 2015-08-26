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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Created by OlivierB on 20/08/2015.
 */
@RunWith(ConcurPlatformTestRunner.class)
@Config(manifest = "src/test/AndroidManifest.xml", assetDir = "assets")
public class ExpenseTestSuite extends PlatformTestSuite {

    private boolean loginDone = false;

    @Before
    public void configure() throws Exception {
        if (!loginDone) {
            // Init the login request
            doPinPasswordLogin();
            loginDone = true;
        }
        if (PlatformTestApplication.useMockServer()) {
            // Init mock server.
            initMockServer();
        }
    }

    /**
     * Performs an expense list test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doExpenseList() throws Exception {

        ExpenseListRequestTaskTest expenseListTest = new ExpenseListRequestTaskTest();
        initTaskMockServer(expenseListTest);

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

        SmartExpenseListRequestTaskTest smartExpenseListTest = new SmartExpenseListRequestTaskTest();
        initTaskMockServer(smartExpenseListTest);

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

        ReceiptListRequestTaskTest receiptListTest = new ReceiptListRequestTaskTest();
        boolean useMockServer = PlatformTestApplication.useMockServer();
        initTaskMockServer(receiptListTest);

        // Run the ReceiptListRequestTask test.
        receiptListTest.doTest();

        // Run the GetReceiptRequestTask test with a receipt image id.
        GetReceiptRequestTaskTest getReceiptTest = new GetReceiptRequestTaskTest(
                GetReceiptRequestTaskTest.ReceiptIdSource.SOURCE_ID);
        initTaskMockServer(getReceiptTest);
        getReceiptTest.doTest();

        // Run the GetReceiptRequestTask test with a receipt Uri.
        getReceiptTest = new GetReceiptRequestTaskTest(GetReceiptRequestTaskTest.ReceiptIdSource.SOURCE_URI);
        initTaskMockServer(getReceiptTest);
        getReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with just a receipt Uri.
        SaveReceiptRequestTaskTest saveReceiptTest = new SaveReceiptRequestTaskTest(
                SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_URI);
        initTaskMockServer(saveReceiptTest);
        // The Roboelectric ContentResolver current throws an UnsupportedException upon attempting to read
        // from a content Uri input stream!
        // saveReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with an input stream.
        saveReceiptTest = new SaveReceiptRequestTaskTest(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_INPUT_STREAM);
        initTaskMockServer(saveReceiptTest);
        saveReceiptTest.doTest();

        // Run the SaveReceiptRequestTask with a byte array.
        saveReceiptTest = new SaveReceiptRequestTaskTest(SaveReceiptRequestTaskTest.ReceiptSource.SOURCE_BYTE_ARRAY);
        initTaskMockServer(saveReceiptTest);
        saveReceiptTest.doTest();

        // Run the DeleteReceiptRequestTask with receipt uri.
        DeleteReceiptRequestTaskTest deleteReceiptTest = new DeleteReceiptRequestTaskTest(
                DeleteReceiptRequestTaskTest.ReceiptSource.SOURCE_URI);
        initTaskMockServer(deleteReceiptTest);
        deleteReceiptTest.doTest();

        // Run the DeleteReceiptRequestTask with a receipt image id.
        deleteReceiptTest = new DeleteReceiptRequestTaskTest(
                DeleteReceiptRequestTaskTest.ReceiptSource.SOURCE_RECEIPT_IMAGE_ID);
        initTaskMockServer(deleteReceiptTest);
        deleteReceiptTest.doTest();

    }

    /**
     * Performs save mobile entry test.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Test
    public void doSaveMobileEntry() throws Exception {

        SaveMobileEntryRequestTaskTest saveMETest = new SaveMobileEntryRequestTaskTest();
        initTaskMockServer(saveMETest);

        // Run the SaveMobileEntryRequestTask test.
        saveMETest.doTest();
    }
}
