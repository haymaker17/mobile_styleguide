/**
 * 
 */
package com.concur.mobile.platform.expense.smartexpense;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.concur.mobile.platform.test.VerifyResponse;
import com.concur.mobile.platform.util.Parse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of unit testing the <code>GetSmartExpenseListRequestTask</code>
 * within the platform code.
 */
public class RemoveSmartExpenseRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = RemoveSmartExpenseRequestTaskTest.class.getSimpleName();

    public RemoveSmartExpenseRequestTaskTest(boolean useMockServer) {
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

        BaseAsyncResultReceiver deleteAccountReceiver = new BaseAsyncResultReceiver(getHander());
        deleteAccountReceiver.setListener(new AsyncReplyListenerImpl());

        // UserId Information used for SmartExpense
        SessionInfo sessionInfo = ConfigUtil.getSessionInfo(context);
        String userId = sessionInfo.getUserId();

        ArrayList<SmartExpense> itemsToRemove = new ArrayList<>();

        // Use SimpleXML framework to deserialize the response.
        SmartExpense smartExpense = new SmartExpense(context, userId);
        smartExpense.setSmartExpenseId("gWkD5tMuL5BgoEmLYAEND8dXGbm1y1XZV30eV");
        smartExpense.setCrnCode("USD");
        smartExpense.setExpKey("GASXX");
        smartExpense.setTransactionAmount(Parse.safeParseDouble("53.8"));
        Calendar transDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        transDate.clear();
        transDate.set(2015, 8, 21, 0, 0, 0);
        smartExpense.setTransactionDate(transDate);
        smartExpense.setVendorDescription("DANS PUMP AND GO");

        itemsToRemove.add(smartExpense);

        //Make the call
        RemoveSmartExpensesRequestTask reqTask = new RemoveSmartExpensesRequestTask(context,
            0, deleteAccountReceiver, itemsToRemove);

        runTest("expense/SaveSmartExpenseResponse.json", reqTask, new VerifyResponse<SmartExpenseAction>() {
            @Override
            public SmartExpenseAction serializeResponse(String result) {
                // Build the parser with type deserializers.
                Gson gson = new GsonBuilder().create();
                return gson.fromJson(result, SmartExpenseAction.class);
            }

            @Override
            public void verify(Context context, SmartExpenseAction accountInfoModel) throws Exception {
                Assert.assertNotNull(accountInfoModel);
                Assert.assertNotNull(result);
                Assert.assertNotNull("We should have a ResultCode status Success", BaseAsyncRequestTask.RESULT_OK == result.resultCode);
            }
        });
    }
}
