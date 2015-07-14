/**
 * 
 */
package com.concur.mobile.platform.receipt.list.test;

import java.util.List;

import org.junit.Assert;

import android.content.Context;
import android.net.Uri;

import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptListDAO;
import com.concur.mobile.platform.util.ContentUtils;

/**
 * Provides a verification that the deleted receipt is no longer found in the content provider.
 * 
 * @author andrewk
 */
public class VerifyDeleteReceiptResponse {

    /**
     * Will verify a parsed save receipt reply against information stored in the content provider.
     * 
     * @param context
     *            contains an application context.
     * @param receiptUri
     *            contains the receipt uri that was deleted.
     * @param receiptCount
     *            contains the count of receipts prior to the deletion.
     * @throws Exception
     *             an <code>Exception</code> if the verification fails.
     */
    public void verify(Context context, Uri receiptUri, int receiptCount) throws Exception {

        // Grab a reference to the receiptDAO object for 'receiptUri'.
        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        ReceiptListDAO recListDAO = new ReceiptListDAO(context, sessInfo.getUserId());

        // First, compare count.
        List<ReceiptDAO> receipts = recListDAO.getReceipts();
        int recDaoCount = (receipts != null) ? receipts.size() : 0;
        Assert.assertTrue("receipt count in content provider is not one less than original count",
                (recDaoCount == (receiptCount - 1)));

        // Second, determine whether 'receiptUri' exists.
        Assert.assertTrue("receipt Uri still exists within the content provider!",
                !ContentUtils.uriExists(context, receiptUri));

    }

}
