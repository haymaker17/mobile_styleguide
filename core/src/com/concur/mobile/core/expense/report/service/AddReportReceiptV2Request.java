/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.FileEntity;

import android.util.Log;

import com.concur.mobile.core.expense.service.SaveReceiptRequest.SaveReceiptUploadListener;
import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.net.CountingFileEntity;
import com.concur.mobile.core.util.net.CountingFileEntity.UploadListener;

/**
 * An extension of <code>PostServiceRequest</code> supporting using version 2 of the AddReportReceipt end-point.
 */
public class AddReportReceiptV2Request extends PostServiceRequest {

    private static final String CLS_TAG = AddReportReceiptV2Request.class.getSimpleName();

    /**
     * Contains the absolute path of the receipt image file.
     */
    public String filePath;

    /**
     * Contains the report key of the report to which the receipt is being added.
     */
    public String rptKey;

    /**
     * Contains whether to delete the receipt file after a save attempt.
     */
    public boolean deleteReceiptFile;

    /**
     * Contains a reference to an upload listener to receive progress updates.
     */
    public SaveReceiptUploadListener listener;

    /**
     * Contains the mime-type of the receipt image.
     */
    public String contentType;

    /**
     * Contains the service end-point.
     */
    public static final String SERVICE_END_POINT = "/mobile/Expense/AddReportReceiptV2";

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.PostServiceRequest#getPostEntity(com.concur.mobile.core.service.ConcurService)
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
                    case PDF:
                        contentType = "application/pdf";
                        break;
                    case UNKNOWN:
                        Log.d(Const.LOG_TAG, CLS_TAG + ".getPostEntity: non jpg/png receipt image type.");
                        throw new ServiceRequestException("Receipt image file of non jpg/png type!");
                    }
                    if (listener != null) {
                        entity = new CountingFileEntity(receiptFile, contentType);
                        final SaveReceiptUploadListener uploadListener = listener;
                        ((CountingFileEntity) entity).setUploadListener(new UploadListener() {

                            @Override
                            public void onChange(int percent) {
                                uploadListener.onChange(percent);
                            }
                        });
                    } else {
                        entity = new FileEntity(receiptFile, contentType);
                    }
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
     * @see com.concur.mobile.core.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(SERVICE_END_POINT);
        strBldr.append('/');
        strBldr.append(rptKey);
        return strBldr.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.core.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        ActionStatusServiceReply reply = new ActionStatusServiceReply();

        // Parse the response or log an error.
        int statusCode = response.getResponseCode();
        if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }

            String xmlReply = readStream(is, encoding);
            try {
                reply = ActionStatusServiceReply.parseReply(xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Log.e(Const.LOG_TAG, "AddReportReceiptV2Request: StatusCode: " + statusCode + ", StatusLine: "
                        + response.getResponseMessage() + ", response: "
                        + ((reply.mwsErrorMessage != null) ? reply.mwsErrorMessage : "null") + ".");
            }
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
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

}
