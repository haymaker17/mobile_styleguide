/**
 *
 */
package com.concur.mobile.platform.ExpenseIt;

import android.content.Context;
import android.text.TextUtils;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expenseit.ExpenseItAccountInfo;
import com.concur.mobile.platform.expenseit.ExpenseItAccountInfoModel;
import com.concur.mobile.platform.expenseit.GetExpenseItAccountInfoAsyncTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.concur.mobile.platform.test.VerifyResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>PPLoginLightRequestTask</code> platform
 * request.
 *
 */
public class GetExpenseItAccountInfoTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = GetExpenseItAccountInfoTaskTest.class.getSimpleName();

    public GetExpenseItAccountInfoTaskTest(boolean useMockServer) {
        super(useMockServer);
    }

    /**
     * Will perform the test throwing an exception if the test fails.
     *
     * @throws Exception throws an exception if the test fails.
     */
    @Override
    public void doTest() throws Exception {

        Context context = PlatformTestApplication.getApplication();

        BaseAsyncResultReceiver getAccountReceiver = new BaseAsyncResultReceiver(getHander());
        getAccountReceiver.setListener(new AsyncReplyListenerImpl());

        SessionInfo sessionInfo = ConfigUtil.getSessionInfo(context);
        String userId = sessionInfo.getUserId();

        //Make the call
        GetExpenseItAccountInfoAsyncTask reqTask = new GetExpenseItAccountInfoAsyncTask(context,
            0, userId, getAccountReceiver);

        runTest("expenseIt/UserAccountInfo.json", reqTask, new VerifyResponse<ExpenseItAccountInfoModel>() {
            @Override
            public ExpenseItAccountInfoModel serializeResponse(String result) {
                // Build the parser with type deserializers.
                Gson gson = new GsonBuilder().create();
                return gson.fromJson(result, ExpenseItAccountInfoModel.class);
            }

            @Override
            public void verify(Context context, ExpenseItAccountInfoModel accountInfoModel) throws Exception {
                testAccountInfo(accountInfoModel);
                testAccountInfo(getResultAccountInfoModel());
            }
        });
    }

    public ExpenseItAccountInfoModel getResultAccountInfoModel() {
        ExpenseItAccountInfoModel accountInfoModel = null;

        if (result.resultData != null &&
            result.resultData.containsKey(GetExpenseItAccountInfoAsyncTask.GET_EXPENSEIT_ACCOUNT_INFO)) {
            accountInfoModel = (ExpenseItAccountInfoModel) result.resultData.getSerializable(GetExpenseItAccountInfoAsyncTask.GET_EXPENSEIT_ACCOUNT_INFO);
        }

        return accountInfoModel;
    }

    private void testAccountInfo(ExpenseItAccountInfoModel accountInfoModel) {
        Assert.assertFalse("AccountInfo Response contains errors!", accountInfoModel != null && accountInfoModel.isError());
        ExpenseItAccountInfo info = accountInfoModel.getAccountInfo();

        Assert.assertNotNull("AccountInfo is not valid and is null", info);
        Assert.assertTrue("AccountInfo ExpenseId is not valid", info.getId() != 0L);
        Assert.assertTrue("AccountInfo CreatedAt is not valid", !TextUtils.isEmpty(info.getCreatedAt()));
    }
}
