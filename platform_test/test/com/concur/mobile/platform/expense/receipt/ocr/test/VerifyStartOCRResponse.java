package com.concur.mobile.platform.expense.receipt.ocr.test;

import org.junit.Assert;

import android.content.Context;

import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.ocr.StartOCR;

/**
 * Provides a class to verify a <code>StartOCRReqeuestResponse</code> object against data stored in the content provider.
 * 
 * @author Chris N. Diaz
 */
public class VerifyStartOCRResponse {

    private static final String CLS_TAG = VerifyStartOCRResponse.class.getName();

    /**
     * Will verify the <code>StartOCR</code> object with success values.
     * 
     * @param context
     *            contains an application context.
     * @param startOcr
     *            contains the StartOCR response object.
     */
    public void verifySuccess(Context context, StartOCR startOcr) throws Exception {

        final String MTAG = CLS_TAG + ".verifySuccess";

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        Assert.assertNotNull(MTAG + ": session info is null", sessInfo);

        // Make sure OCR object isn't null!
        Assert.assertNotNull(MTAG + ": StartOCR object is null!", startOcr);

        // ActionSatatus
        Assert.assertNotNull(MTAG + ": actionStatus", startOcr.actionStatus);

        Assert.assertEquals(MTAG + ": actionStatus", startOcr.actionStatus.status, "SUCCESS");

        // ReceiptImage Id
        Assert.assertNotNull(MTAG + ": receiptImageId", startOcr.receiptImageId);

        // Image Origin
        Assert.assertEquals(MTAG + ": imageOrigin", startOcr.imageOrigin, "MOB");

        // OCR Status
        Assert.assertEquals(MTAG + ": ocrStatus", startOcr.ocrStatus, "A_PEND");
    }

    /**
     * Will verify the <code>StartOCR</code> object with failure values along with errors.
     * 
     * @param context
     *            contains an application context.
     * @param startOcr
     *            contains the StartOCR response object.
     */
    public void verifyFailure(Context context, StartOCR startOcr) throws Exception {

        final String MTAG = CLS_TAG + ".verifyFailure";

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        Assert.assertNotNull(MTAG + ": session info is null", sessInfo);

        // Make sure OCR object isn't null!
        Assert.assertNotNull(MTAG + ": StartOCR object is null!", startOcr);

        // ActionSatatus
        Assert.assertNotNull(MTAG + ": actionStatus", startOcr.actionStatus);
        Assert.assertEquals(MTAG + ": actionStatus", startOcr.actionStatus.status, "FAILURE");

        // Receipt Image ID
        Assert.assertNotNull(MTAG + ": receiptImageId object is null!", startOcr.receiptImageId);

        // Other values SHOULD be null.
        Assert.assertNull(MTAG + ": imageOrigin", startOcr.imageOrigin);
        Assert.assertNull(MTAG + ": ocrStatus", startOcr.ocrStatus);

        // Test for Errors.
        if (startOcr.errors != null) {
            for (com.concur.mobile.platform.service.parser.Error error : startOcr.errors) {
                // Code
                Assert.assertNotNull(MTAG + ": code", error.getCode());

                // System Message
                Assert.assertNotNull(MTAG + ": systemMessage", error.getSystemMessage());
            }
        }

    }

}
