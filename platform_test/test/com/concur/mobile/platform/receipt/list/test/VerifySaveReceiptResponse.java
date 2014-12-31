/**
 * 
 */
package com.concur.mobile.platform.receipt.list.test;

import org.junit.Assert;

import android.content.Context;
import android.net.Uri;

import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.list.Receipt;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptListDAO;

/**
 * Provides a verification of the save receipt reply parsed directly from the server response versus coming from the content
 * provider.
 * 
 * @author andrewk
 */
public class VerifySaveReceiptResponse {

    private static final String CLS_TAG = "VerifySaveReceiptResponse";

    /**
     * Will verify a parsed save receipt reply against information stored in the content provider.
     * 
     * @param context
     *            contains an application context.
     * @param receiptUri
     *            contains the receipt uri.
     * @param receipt
     *            contains the parsed receipt.
     * @throws Exception
     *             an <code>Exception</code> if the verification fails.
     */
    public void verify(Context context, Uri receiptUri, Receipt receipt) throws Exception {

        final String MTAG = CLS_TAG + ".verify";

        // Grab a reference to the receiptDAO object for 'receiptUri'.
        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        ReceiptListDAO recListDAO = new ReceiptListDAO(context, sessInfo.getUserId());
        ReceiptDAO recDAO = recListDAO.getReceipt(receiptUri);
        Assert.assertNotNull("receiptDAO is null!", recDAO);

        // Verify field information.

        // ETag.
        Assert.assertEquals(MTAG + ": eTAG", receipt.getETag(), recDAO.getETag());
        // Id
        Assert.assertEquals(MTAG + ": Id", receipt.getId(), recDAO.getId());
        // Uri
        Assert.assertEquals(MTAG + ": Uri", receipt.getUri(), recDAO.getUri());
        // Thumbnail Uri
        Assert.assertEquals(MTAG + ": Thumbnail Uri", receipt.getThumbnailUri(), recDAO.getThumbnailUri());
        // Content type
        Assert.assertEquals(MTAG + ": content type", receipt.getContentType(), recDAO.getContentType());

    }

}
