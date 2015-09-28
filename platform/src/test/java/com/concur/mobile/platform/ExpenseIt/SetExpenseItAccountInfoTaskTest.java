/**
 *
 */
package com.concur.mobile.platform.ExpenseIt;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.expenseit.ExpenseItAccountInfo;
import com.concur.mobile.platform.expenseit.ExpenseItAccountInfoModel;
import com.concur.mobile.platform.expenseit.UpdateExpenseItAccountInfoAsyncTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;
import com.concur.mobile.platform.test.VerifyResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>PPLoginLightRequestTask</code> platform
 * request.
 *
 */
public class SetExpenseItAccountInfoTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = SetExpenseItAccountInfoTaskTest.class.getSimpleName();

    public SetExpenseItAccountInfoTaskTest(boolean useMockServer) {
        super(useMockServer);
    }

    @Override
    public void doTest() throws Exception {
        testSetAutoCTE(true);
        testSetAutoCTE(false);
    }
    /**
     * Will perform the test throwing an exception if the test fails.
     *
     * @throws Exception throws an exception if the test fails.
     */
    public void testSetAutoCTE(final boolean setToOn) throws Exception {

        Context context = PlatformTestApplication.getApplication();

        String content = getFileContents(context, setToOn ? "expenseIt/UserAccountInfo.json" : "expenseIt/UserAccountInfoWithAutoCTEOn.json");

        ExpenseItAccountInfoModel accountInfo = serializeAccountFromJson(content);
        accountInfo.getAccountInfo().setAutoCTE(setToOn);

        BaseAsyncResultReceiver getAccountReceiver = new BaseAsyncResultReceiver(getHander());
        getAccountReceiver.setListener(new AsyncReplyListenerImpl());

        //Make the call
        UpdateExpenseItAccountInfoAsyncTask reqTask = new UpdateExpenseItAccountInfoAsyncTask(context,
            0, getAccountReceiver, accountInfo.getAccountInfo());

        runTest(setToOn ? "expenseIt/UserAccountInfoWithAutoCTEOn.json" : "expenseIt/UserAccountInfo.json", reqTask, new VerifyResponse<ExpenseItAccountInfoModel>() {
            @Override
            public ExpenseItAccountInfoModel serializeResponse(String result) {
                return serializeAccountFromJson(result);
            }

            @Override
            public void verify(Context context, ExpenseItAccountInfoModel accountInfoModel) throws Exception {
                testAccountInfo(accountInfoModel, setToOn);
                testAccountInfo(getResultAccountInfoModel(), setToOn);
            }
        });
    }

    protected static ExpenseItAccountInfoModel serializeAccountFromJson(String result) {
        // Build the parser with type deserializers.
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(result, ExpenseItAccountInfoModel.class);
    }

    protected static String getFileContents(Context context, String mockRespFile) throws IOException {
        InputStream is = null;
        StringBuilder strBldr = new StringBuilder();
        AssetManager assetMngr = context.getAssets();
        if (!TextUtils.isEmpty(mockRespFile)) {
            is = assetMngr.open(mockRespFile);
        }
        if (is != null) {

            BufferedReader bufRdr = null;
            try {
                bufRdr = new BufferedReader(new InputStreamReader(is), (8 * 1024));
                String line = null;
                while ((line = bufRdr.readLine()) != null) {
                    strBldr.append(line.trim());
                }
            } catch (Exception e){
                Assert.assertFalse("Cannot open Content file", true);
            }
            finally {
                if (bufRdr != null) {
                    bufRdr.close();
                }
            }
        }
        return strBldr.toString();
    }

    public ExpenseItAccountInfoModel getResultAccountInfoModel() {
        ExpenseItAccountInfoModel accountInfoModel = null;

        if (result.resultData != null &&
            result.resultData.containsKey(UpdateExpenseItAccountInfoAsyncTask.UPDATE_EXPENSEIT_ACCOUNT_INFO)) {
            accountInfoModel = (ExpenseItAccountInfoModel) result.resultData.getSerializable(UpdateExpenseItAccountInfoAsyncTask.UPDATE_EXPENSEIT_ACCOUNT_INFO);
        }

        return accountInfoModel;
    }

    private void testAccountInfo(ExpenseItAccountInfoModel accountInfoModel, boolean setToOn) {
        Assert.assertFalse("AccountInfo Response contains errors!", accountInfoModel != null && accountInfoModel.isError());
        ExpenseItAccountInfo info = accountInfoModel.getAccountInfo();

        Assert.assertNotNull("AccountInfo is not valid and is null", info);
        Assert.assertTrue("AccountInfo ExpenseId is not valid", info.getId() != 0L);
        Assert.assertTrue("AccountInfo CreatedAt is not valid", !TextUtils.isEmpty(info.getCreatedAt()));

        Assert.assertTrue("Account Failed to set CTE autoExport properly",info.isAutoCTE() == setToOn);
    }
}
