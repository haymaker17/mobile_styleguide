package com.concur.mobile.platform.expense.receipt.ocr.test;

import org.junit.Assert;

import android.content.Context;

import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.ocr.StopOCR;

/**
 * Provides a class to verify a <code>StopOCRReqeuestResponse</code> object against data stored in the content provider.
 * 
 * @author Chris N. Diaz
 */
public class VerifyStopOCRResponse {

    private static final String CLS_TAG = VerifyStopOCRResponse.class.getName();

    /**
     * Will verify the <code>StopOCR</code> object with success values.
     * 
     * @param context
     *            contains an application context.
     * @param stopOcr
     *            contains the StopOCR response object.
     */
    public void verifySuccess(Context context, StopOCR stopOcr) throws Exception {

        final String MTAG = CLS_TAG + ".verifySuccess";

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        Assert.assertNotNull(MTAG + ": session info is null", sessInfo);

        // Make sure OCR object isn't null!
        Assert.assertNotNull(MTAG + ": StopOCR object is null!", stopOcr);

        // ActionSatatus
        Assert.assertNotNull(MTAG + ": actionStatus", stopOcr.actionStatus);

        Assert.assertEquals(MTAG + ": actionStatus", stopOcr.actionStatus.status, "SUCCESS");

        // ReceiptImage Id
        Assert.assertNotNull(MTAG + ": receiptImageId", stopOcr.receiptImageId);

        // Image Origin
        Assert.assertEquals(MTAG + ": imageOrigin", stopOcr.imageOrigin, "MOB");

        // OCR Status
        Assert.assertEquals(MTAG + ": ocrStatus", stopOcr.ocrStatus, "A_CNCL");

        // Reject code
        Assert.assertEquals(MTAG + ": rejectCode", stopOcr.rejectCode, "AC");

    }

    /**
     * Will verify the <code>StopOCR</code> object with failure values along with errors.
     * 
     * @param context
     *            contains an application context.
     * @param stopOcr
     *            contains the StopOCR response object.
     */
    public void verifyFailure(Context context, StopOCR stopOcr) throws Exception {

        final String MTAG = CLS_TAG + ".verifyFailure";

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        Assert.assertNotNull(MTAG + ": session info is null", sessInfo);

        // Make sure OCR object isn't null!
        Assert.assertNotNull(MTAG + ": StopOCR object is null!", stopOcr);

        // ActionSatatus
        Assert.assertNotNull(MTAG + ": actionStatus", stopOcr.actionStatus);
        Assert.assertEquals(MTAG + ": actionStatus", stopOcr.actionStatus.status, "FAILURE");

        // Receipt Image ID
        Assert.assertNotNull(MTAG + ": receiptImageId object is null!", stopOcr.receiptImageId);

        // Other values SHOULD be null.
        Assert.assertNull(MTAG + ": imageOrigin", stopOcr.imageOrigin);
        Assert.assertNull(MTAG + ": ocrStatus", stopOcr.ocrStatus);

        // Test for Errors.
        if (stopOcr.errors != null) {
            for (com.concur.mobile.platform.service.parser.Error error : stopOcr.errors) {
                // Code
                Assert.assertNotNull(MTAG + ": code", error.getCode());

                // System Message
                Assert.assertNotNull(MTAG + ": systemMessage", error.getSystemMessage());
            }
        }

    }

}
