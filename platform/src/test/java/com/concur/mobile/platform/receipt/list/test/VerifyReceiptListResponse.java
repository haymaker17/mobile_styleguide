/**
 * 
 */
package com.concur.mobile.platform.receipt.list.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import android.content.Context;
import android.text.TextUtils;

import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.list.ReceiptList;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptListDAO;

/**
 * Provides a verification of the receipt list parsed directly from the server response versus coming from the content provider.
 * 
 * @author andrewk
 */
public class VerifyReceiptListResponse {

    private static final String CLS_TAG = "VerifyReceiptListResponse";

    /**
     * Will verify parsed receipt list information against receipt list information stored in the content provider.
     * 
     * @param context
     *            contains an application context.
     * @param recListResp
     *            contains the parsed receipt list.
     * @throws Exception
     *             an <code>Exception</code> if the verification fails.
     */
    public void verify(Context context, ReceiptList recListResp) throws Exception {

        final String MTAG = CLS_TAG + ".verify";

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        Assert.assertNotNull(MTAG + ": session info is null", sessInfo);

        ReceiptListDAO recListDAO = new ReceiptListDAO(context, sessInfo.getUserId());

        // Verify count of receipts.
        List<ReceiptDAO> recDAOS = recListDAO.getReceipts();
        Long recDaosCount = (recDAOS != null) ? recDAOS.size() : 0L;
        List<ReceiptDAO> parsedReceipts = (recListResp != null) ? recListResp.getReceipts() : null;
        Long recCount = (parsedReceipts != null) ? parsedReceipts.size() : 0L;
        Assert.assertEquals(MTAG + ": receipt count", recCount, recDaosCount);

        // Verify parsed receipt information against content provider information.
        if (recDAOS != null) {
            // First build look-up map.
            Map<String, ReceiptDAO> recDAOMap = new HashMap<String, ReceiptDAO>(recDAOS.size());
            for (ReceiptDAO recDAO : recDAOS) {
                if (!TextUtils.isEmpty(recDAO.getId())) {
                    recDAOMap.put(recDAO.getId(), recDAO);
                }
            }
            for (ReceiptDAO parsedRec : parsedReceipts) {

                // Verify the parsed receipt Id can be found in the map built from the content provider.
                ReceiptDAO recDAO = recDAOMap.get(parsedRec.getId());
                Assert.assertNotNull(MTAG + ": unable to locate parsed receipt in DAO map", recDAO);

                // Verify field information.

                // ETag.
                Assert.assertEquals(MTAG + ": eTAG", parsedRec.getETag(), recDAO.getETag());
                // Id
                Assert.assertEquals(MTAG + ": Id", parsedRec.getId(), recDAO.getId());
                // Uri
                Assert.assertEquals(MTAG + ": Uri", parsedRec.getUri(), recDAO.getUri());
                // Thumbnail Uri
                Assert.assertEquals(MTAG + ": Thumbnail Uri", parsedRec.getThumbnailUri(), recDAO.getThumbnailUri());
                // File name
                Assert.assertEquals(MTAG + ": Filename", parsedRec.getFileName(), recDAO.getFileName());
                // File type
                Assert.assertEquals(MTAG + ": Filetype", parsedRec.getFileType(), recDAO.getFileType());
                // Receipt Upload Time
                Assert.assertEquals(MTAG + ": Receipt upload time", parsedRec.getReceiptUploadTime(),
                        recDAO.getReceiptUploadTime());
                // System Origin
                Assert.assertEquals(MTAG + ": System Origin", parsedRec.getSystemOrigin(), recDAO.getSystemOrigin());
                // Image Origin
                Assert.assertEquals(MTAG + ": Image Origin", parsedRec.getImageOrigin(), recDAO.getImageOrigin());
                // Image Url
                Assert.assertEquals(MTAG + ": Image Url", parsedRec.getImageUrl(), recDAO.getImageUrl());
                // Thumb Url
                Assert.assertEquals(MTAG + ": Image Url", parsedRec.getThumbUrl(), recDAO.getThumbUrl());
                // Ocr Image Origin
                Assert.assertEquals(MTAG + ": Ocr Image Url", parsedRec.getOcrImageOrigin(), recDAO.getOcrImageOrigin());
                // Ocr Status
                Assert.assertEquals(MTAG + ": Ocr Status", parsedRec.getOcrStatus(), recDAO.getOcrStatus());
                // Ocr Reject Code
                Assert.assertEquals(MTAG + ": Ocr Reject Code", parsedRec.getOcrRejectCode(), recDAO.getOcrRejectCode());
            }
        }

    }
}
