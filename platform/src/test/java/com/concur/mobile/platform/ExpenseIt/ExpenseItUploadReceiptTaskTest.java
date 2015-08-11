/**
 *
 */
package com.concur.mobile.platform.ExpenseIt;

import android.content.Context;
import android.content.res.AssetManager;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.util.IOUtils;
import com.concur.mobile.platform.ExpenseIt.test.VerifyExpenseItUploadReceiptResult;
import com.concur.mobile.platform.expenseit.ExpenseItImage;
import com.concur.mobile.platform.expenseit.ExpenseItPostReceiptResponse;
import com.concur.mobile.platform.expenseit.PostExpenseItReceiptAsyncTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An extension of <code>AsyncRequestTest</code> for the purpose of testing the <code>PPLoginLightRequestTask</code> platform
 * request.
 */
public class ExpenseItUploadReceiptTaskTest extends AsyncRequestTest {

    public ExpenseItUploadReceiptTaskTest(boolean useMockServer) {
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

        // Initiate the login request.
        BaseAsyncResultReceiver uploadReceiptReceiver = new BaseAsyncResultReceiver(getHander());
        uploadReceiptReceiver.setListener(new AsyncReplyListenerImpl());

        PostExpenseItReceiptAsyncTask reqTask = new PostExpenseItReceiptAsyncTask(context,
            0, uploadReceiptReceiver, getImage(context));

        VerifyExpenseItUploadReceiptResult verifier = new VerifyExpenseItUploadReceiptResult();
        runTest("expenseIt/ExpenseItUploadImageResponse.json", reqTask, verifier);
    }

    protected ExpenseItImage getImage(Context context) throws IOException {

        String filePath = "receipt/IMG_20140731_142657.jpg";

        // Set the content-length.
        AssetManager assetMngr = context.getAssets();
        InputStream is = assetMngr.open(filePath);
        ByteArrayOutputStream outStr = IOUtils.reusableOutputStream(is);

        //Setup the image information
        ExpenseItImage image = new ExpenseItImage();
        String contentType = "image/png";

        //Compress the bitmap
        image.setData(outStr.toByteArray(), contentType);
        return image;
    }

    public Long getExpenseItId() {
        ExpenseItPostReceiptResponse receiptResponse = null;

        if (result.resultData != null &&
            result.resultData.containsKey(PostExpenseItReceiptAsyncTask.POST_EXPENSEIT_OCR_RESULT_KEY)) {
            receiptResponse = (ExpenseItPostReceiptResponse) result.resultData.get(PostExpenseItReceiptAsyncTask.POST_EXPENSEIT_OCR_RESULT_KEY);
        }

        if (receiptResponse != null && receiptResponse.getExpenses().length > 0) {
            return receiptResponse.getExpenses()[0].getId();
        } else {
            return null;
        }
    }
}
