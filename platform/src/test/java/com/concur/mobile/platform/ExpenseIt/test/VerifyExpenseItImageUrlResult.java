/**
 * 
 */
package com.concur.mobile.platform.ExpenseIt.test;

import android.content.Context;

import com.concur.mobile.platform.expenseit.ErrorResponse;
import com.concur.mobile.platform.expenseit.ExpenseItGetImageUrlResponse;

import org.junit.Assert;

/**
 * Provides a class to verify a <code>ExpenseItGetImageUrlResponse</code> for valid contents.
 */
public class VerifyExpenseItImageUrlResult {

    private final String MTAG = CLS_TAG + ".verify";

    private static final String CLS_TAG = VerifyExpenseItImageUrlResult.class.getSimpleName();

     /**
     * Will verify imageUrl response information stored in the config content provider against information stored in
     * <code>loginResult</code>.
     */
    public void verify(Context context, ExpenseItGetImageUrlResponse imageUrlResponse) throws Exception {

        //Verify LoginResult is not null
        if (imageUrlResponse == null) {
            throw new Exception(CLS_TAG + "." + MTAG + ": ImageUrl response is null!");
        }

        if (imageUrlResponse.getImages() == null) {
            throw new Exception(CLS_TAG + "." + MTAG + ": Images is null!");
        }

        Assert.assertEquals(MTAG + ": entries should match", imageUrlResponse.getImages().size(), 1);

        for (ExpenseItGetImageUrlResponse.ExpenseImageUrl imageInfo : imageUrlResponse.getImages()) {

            Assert.assertTrue(MTAG + ": Image id is not valid", imageInfo.getExpenseId() > 0);

            Assert.assertNotNull(MTAG + ": Image Url is not valid", imageInfo.getImageDataUrl());

            Assert.assertTrue(MTAG + ": No Errors", imageInfo.getErrorCode() == -1);

            Assert.assertTrue(MTAG + ": Default Error Message is Invalid", imageInfo.getErrorMessage() == ErrorResponse.DEFAULT_NO_ERROR_MESSAGE);
        }
    }
}
