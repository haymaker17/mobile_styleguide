/**
 * 
 */
package com.concur.mobile.core.expense.charge.service;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.FileEntity;

import android.util.Log;

import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.expense.data.ReceiptPictureSaveAction;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;

/**
 * An extension of <code>PostServiceRequest</code> for the purpose of uploading a receipt image associated with a mobile entry.
 * 
 * @author AndrewK
 */
public class UploadMobileEntryReceiptRequest extends PostServiceRequest {

    private static final String CLS_TAG = UploadMobileEntryReceiptRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Expense/SaveMobileEntryReceipt";

    /**
     * Contains the mobile entry key associated with this request.
     */
    String mobileEntryKey;

    /**
     * Contains a reference to the mobile entry being saved.
     */
    MobileEntry mobileEntry;

    /**
     * Contains a reference to an immediate prior request object used to save a mobile entry.
     */
    SaveMobileEntryRequest saveRequest;

    /**
     * Contains the local key of the mobile entry.
     */
    String localKey;

    /**
     * Contains the absolute path of the receipt image file.
     */
    String filePath;

    /**
     * Contains the mime-type of the receipt image.
     */
    String contentType;

    /**
     * Contains whether the receipt image file should be deleted after the attempted save.
     */
    boolean deleteReceiptFile;

    /**
     * Contains how the receipt image was generated, i.e., either selected from the device gallery or captured with the camera.
     */
    ReceiptPictureSaveAction receiptSaveAction;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#getPostEntity(com.concur.mobile.service.ConcurService)
     */
    @Override
    protected HttpEntity getPostEntity(ConcurService concurService) throws ServiceRequestException {
        HttpEntity entity = null;
        if (filePath != null) {
            File receiptFile = new File(filePath);
            try {
                if (receiptFile.exists()) {
                    switch (ViewUtil.getDocumentType(receiptFile)) {
                    case PNG:
                        contentType = "image/x-png";
                        break;
                    case JPG:
                        contentType = "image/jpeg";
                        break;
                    case UNKNOWN:
                        Log.d(Const.LOG_TAG, CLS_TAG + ".getPostEntity: non jpg/png receipt image type.");
                        throw new ServiceRequestException("Receipt image file of non jpg/png type!");
                    }
                    entity = new FileEntity(receiptFile, contentType);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: receipt image file '" + filePath
                            + "' does not exist!");
                    throw new ServiceRequestException("Receipt image file '" + filePath + "' does not exist!");
                }
            } catch (SecurityException secExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: can't access receipt file '" + filePath + ".", secExc);
                throw new ServiceRequestException("Receipt image file '" + filePath + "' is not accessible.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: receipt image file is null!");
        }
        return entity;
    }

    @Override
    protected HttpRequestBase getRequestBase(ConcurService concurService) throws ServiceRequestException {
        HttpPost post = (HttpPost) super.getRequestBase(concurService);
        HttpEntity entity = post.getEntity();
        if (entity != null) {
            long len = entity.getContentLength();
            if (len > 0) {
                post.addHeader(Const.HTTP_HEADER_EXPECTED_LENGTH, Long.toString(len));
            }
        }
        return post;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#getContentType()
     */
    @Override
    protected String getContentType() {
        return contentType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        String retVal = null;
        StringBuilder strBldr = new StringBuilder(SERVICE_END_POINT);
        strBldr.append('/');
        if (saveRequest != null && saveRequest.mobileEntry != null && saveRequest.mobileEntry.getMeKey() != null) {
            strBldr.append(saveRequest.mobileEntry.getMeKey());
        } else {
            strBldr.append(mobileEntryKey);
        }
        retVal = strBldr.toString();
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        ServiceReply srvReply = new ServiceReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            srvReply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return srvReply;
    }

    @Override
    protected String buildRequestBody() {
        // TODO Auto-generated method stub
        return null;
    }

}
