package com.concur.mobile.platform.password.reset.test;

import org.junit.Assert;

import android.os.Bundle;

import com.concur.mobile.platform.authentication.RequestPasswordResetRequestTask;

/**
 * Provides a class to perform verification of a reset password response.
 * 
 * @author andrewk
 */
public class VerifyRequestPasswordResetResponse {

    private static final String CLS_TAG = "VerifyRequestPasswordResetResponse";

    /**
     * Will verify that reset password response information contained in <code>resultData</code> matches data stored in
     * <code>response</code>.
     * 
     * @param resultData
     *            contains the reset password response bundle.
     * @param response
     *            contains parsed reset password response information parsed by the test.
     * @throws Exception
     *             an exception if reset password response information stored in <code>resultData</code> does not match data
     *             stored in <code>response</code>.
     */
    public void verify(Bundle resultData, RequestPasswordResetResponse response) throws Exception {

        final String MTAG = CLS_TAG + ".verify";

        // Verify key part.
        String keyPartA = resultData.getString(RequestPasswordResetRequestTask.EXTRA_KEY_PART_A_KEY);
        Assert.assertEquals(MTAG + ": (result data) key part A", response.keyPartA, keyPartA);

        // Verify good password description.
        String goodPasswordDescription = resultData
                .getString(RequestPasswordResetRequestTask.EXTRA_GOOD_PASSWORD_DESCRIPTION_KEY);
        Assert.assertEquals(MTAG + ": (result data) good password description", response.goodPasswordDescription,
                goodPasswordDescription);

    }

}
