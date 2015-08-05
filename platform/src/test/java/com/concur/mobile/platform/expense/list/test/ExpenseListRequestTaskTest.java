/**
 * 
 */
package com.concur.mobile.platform.expense.list.test;

import java.util.Locale;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.text.TextUtils;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.expense.list.ExpenseListRequestTask;
import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>AsyncRequestTest</code> for the purposes of testing the <code>ExpenseListRequestTask</code> request.
 * 
 * @author andrewk
 */
public class ExpenseListRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "ExpenseListRequestTaskTest";

    private static final boolean DEBUG = false;

    public ExpenseListRequestTaskTest(boolean useMockServer) {
        super(useMockServer);
    }

    /**
     * Will perform the test throwing an exception if the test fails.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    @Override
    public void doTest() throws Exception {

        Context context = PlatformTestApplication.getApplication();

        // Set the mock response if the mock server is being used.
        if (useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "expense/ExpenseListResponse.xml");
        }

        // Initiate the expense list request.
        BaseAsyncResultReceiver expenseListReplyReceiver = new BaseAsyncResultReceiver(getHander());
        expenseListReplyReceiver.setListener(new AsyncReplyListenerImpl());
        Locale locale = context.getResources().getConfiguration().locale;
        ExpenseListRequestTask reqTask = new ExpenseListRequestTask(context, 1, expenseListReplyReceiver);
        reqTask.setRetainResponse(true);

        if (DEBUG) {
            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: launching the request.");
        }

        // Launch the request.
        launchRequest(reqTask);

        if (DEBUG) {
            ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: launched the request.");
        }

        try {
            if (DEBUG) {
                ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: waiting for result.");
            }
            // Wait for the result.
            waitForResult();
            if (DEBUG) {
                ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: obtained result.");
            }
        } catch (InterruptedException intExc) {
            ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".doTest: interrupted while acquiring login result.");
            result.resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
        }

        // Examine the result.
        if (result != null) {

            // Verify result code.
            verifyExpectedResultCode(CLS_TAG);

            switch (result.resultCode) {
            case BaseAsyncRequestTask.RESULT_CANCEL: {
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result cancelled.");
                }
                break;
            }
            case BaseAsyncRequestTask.RESULT_ERROR: {
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result error.");
                }
                break;
            }
            case BaseAsyncRequestTask.RESULT_OK: {

                // Verify the result.

                // Grab the response.
                String response = getResponseString(reqTask);
                Assert.assertNotNull("request response is null", response);

                // Use SimpleXML framework to deserialize the response.
                Serializer serializer = new Persister();
                ExpenseListResponse expenseListResponse = serializer.read(ExpenseListResponse.class, response, false);
                if (expenseListResponse != null) {
                    // Convert any corporate card transaction dates/boolean values.
                    if (expenseListResponse.corporateCardTransactions != null) {
                        for (CorporateCardTransaction cctTrans : expenseListResponse.corporateCardTransactions) {
                            // Parse transaction date.
                            if (!TextUtils.isEmpty(cctTrans.transactionDateStr)) {
                                cctTrans.transactionDate = Parse.parseXMLTimestamp(cctTrans.transactionDateStr);
                            }
                            // Parse HasRichData.
                            if (!TextUtils.isEmpty(cctTrans.hasRichDataStr)) {
                                cctTrans.hasRichData = Parse.safeParseBoolean(cctTrans.hasRichDataStr);
                            }
                            // If there is a related mobile entry, then set the 'type' attribute on the related
                            // mobile entry to 'CORPORATE' and the 'cctKey'.
                            if (cctTrans.mobileEntry != null) {
                                cctTrans.mobileEntry.type = ExpenseTypeEnum.CORPORATE_CARD;
                                cctTrans.mobileEntry.cctKey = cctTrans.cctKey;
                                // Parse transaction date.
                                if (!TextUtils.isEmpty(cctTrans.mobileEntry.transactionDateStr)) {
                                    cctTrans.mobileEntry.transactionDate = Parse
                                            .parseXMLTimestamp(cctTrans.mobileEntry.transactionDateStr);
                                }
                                // Parse "has receipt image".
                                if (!TextUtils.isEmpty(cctTrans.mobileEntry.hasReceiptImageStr)) {
                                    cctTrans.mobileEntry.hasReceiptImage = Parse
                                            .safeParseBoolean(cctTrans.mobileEntry.hasReceiptImageStr);
                                }
                            }
                            // If the SmartExpenseMeKey is set, then set the type to 'SMART_CORPORATE'.
                            if (!TextUtils.isEmpty(cctTrans.smartExpenseMeKey)) {
                                cctTrans.type = ExpenseTypeEnum.SMART_CORPORATE;
                            }
                        }
                    }
                    // Convert any personal card transactions dates/boolean values.
                    if (expenseListResponse.personalCards != null) {
                        for (PersonalCard persCard : expenseListResponse.personalCards) {
                            if (persCard.transactions != null) {
                                for (PersonalCardTransaction pctTrans : persCard.transactions) {
                                    // Set the crn code on 'pctTrans' from 'persCard'.
                                    pctTrans.crnCode = persCard.crnCode;
                                    // Parse date posted.
                                    if (!TextUtils.isEmpty(pctTrans.datePostedStr)) {
                                        pctTrans.datePosted = Parse.parseXMLTimestamp(pctTrans.datePostedStr);
                                    }
                                    // If there is a related mobile entry, then set the 'type' attribute on the related
                                    // mobile entry to 'PERSONAL'.
                                    if (pctTrans.mobileEntry != null) {
                                        pctTrans.mobileEntry.type = ExpenseTypeEnum.PERSONAL_CARD;
                                        pctTrans.mobileEntry.pcaKey = persCard.pcaKey;
                                        pctTrans.mobileEntry.pctKey = pctTrans.pctKey;
                                        // Parse transaction date.
                                        if (!TextUtils.isEmpty(pctTrans.mobileEntry.transactionDateStr)) {
                                            pctTrans.mobileEntry.transactionDate = Parse
                                                    .parseXMLTimestamp(pctTrans.mobileEntry.transactionDateStr);
                                        }
                                        // Parse "has receipt image".
                                        if (!TextUtils.isEmpty(pctTrans.mobileEntry.hasReceiptImageStr)) {
                                            pctTrans.mobileEntry.hasReceiptImage = Parse
                                                    .safeParseBoolean(pctTrans.mobileEntry.hasReceiptImageStr);
                                        }
                                    }
                                    // If the SmartExpenseMeKey is set, then set the type to 'SMART_PERSONAL'
                                    // and the 'pcaKey' and 'pctKey'.
                                    if (!TextUtils.isEmpty(pctTrans.smartExpenseMeKey)) {
                                        pctTrans.type = ExpenseTypeEnum.SMART_PERSONAL;
                                    }
                                }
                            }
                        }
                    }
                    // Convert any mobile entry transaction dates/boolean values.
                    if (expenseListResponse.mobileEntries != null) {
                        for (MobileEntry mobEnt : expenseListResponse.mobileEntries) {
                            // Parse transaction date.
                            if (!TextUtils.isEmpty(mobEnt.transactionDateStr)) {
                                mobEnt.transactionDate = Parse.parseXMLTimestamp(mobEnt.transactionDateStr);
                            }
                            // Parse "has receipt image".
                            if (!TextUtils.isEmpty(mobEnt.hasReceiptImageStr)) {
                                mobEnt.hasReceiptImage = Parse.safeParseBoolean(mobEnt.hasReceiptImageStr);
                            }
                        }
                    }
                    // Convert any receipt capture dates/boolean values.
                    if (expenseListResponse.receiptCaptures != null) {
                        for (ReceiptCapture recCap : expenseListResponse.receiptCaptures) {
                            // Parse transaction date.
                            if (!TextUtils.isEmpty(recCap.transactionDateStr)) {
                                recCap.transactionDate = Parse.parseXMLTimestamp(recCap.transactionDateStr);
                            }
                        }
                    }
                }

                // Perform the verification.
                VerifyExpenseListResponse verifyExpenseListResponse = new VerifyExpenseListResponse();
                verifyExpenseListResponse.verify(context, expenseListResponse);
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                }
                break;
            }
            }

        }

    }

}
