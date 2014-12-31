/**
 * 
 */
package com.concur.mobile.core.expense.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.FileEntity;

import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.net.CountingFileEntity;
import com.concur.mobile.core.util.net.CountingFileEntity.UploadListener;

/**
 * An extension of <code>PostServiceRequest</code> for the purposes of saving a receipt.
 * 
 * @author AndrewK
 */
public class SaveReceiptRequest extends PostServiceRequest {

    private static final String CLS_TAG = SaveReceiptRequest.class.getSimpleName();

    public interface SaveReceiptUploadListener {

        /**
         * Provides a notification of the receipt upload percent progress.
         * 
         * @param percent
         *            the percentage upload progress.
         */
        public void onChange(int percent);

    }

    /**
     * An enumeration defining the type of save receipt call to make.
     */
    public enum SaveReceiptCall {
        // Saves through the MWS end-point.
        MWS,
        // Saves through the Concur Connect 'receipt' API call.
        CONNECT_POST_IMAGE,
        // Saves through the Concur Connect 'report' API call.
        CONNECT_POST_IMAGE_REPORT,
        // Saves through the Concur Connect 'expenseentry' API call.
        CONNECT_POST_IMAGE_REPORT_ENTRY
    }

    /**
     * Contains the type of save receipt end-point to call. Defaults to <code>SaveReceiptCall.MWS</code>.
     */
    public SaveReceiptCall receiptEndpoint = SaveReceiptCall.MWS;

    /**
     * Contains the service end-point.
     */
    public static final String SERVICE_END_POINT = "/mobile/Expense/SaveReceipt";

    /**
     * Contains the image origin.
     */
    public String imageOrigin;

    /**
     * Contains the absolute path of the receipt image file.
     */
    public String filePath;

    /**
     * Contains the mime-type of the receipt image.
     */
    public String contentType;

    /**
     * Contains whether to delete the receipt file after a save attempt.
     */
    public boolean deleteReceiptFile;

    /**
     * Contains a reference to an upload listener to receive progress updates.
     */
    public SaveReceiptUploadListener listener;

    /**
     * A flag to indicate whether this receipt is already attached to an expense. Needed for offline tracking.
     */
    public boolean standaloneReceipt;

    /**
     * Contains the report entry key if this receipt is being saved for association with a report entry.
     */
    public String rpeKey;

    /**
     * Contains the report key if this receipt is being saved for association with a report.
     */
    public String rptKey;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.ServiceRequest#addHeaders(org.apache.http.client.methods.HttpRequestBase)
     */
    @Override
    protected void addHeaders(HttpRequestBase request) {
        if (receiptEndpoint == SaveReceiptCall.MWS) {
            super.addHeaders(request);
        } else {
            request.addHeader(Const.HTTP_HEADER_USER_AGENT, Const.HTTP_HEADER_USER_AGENT_VALUE);
            if (accessToken != null) {
                request.addHeader(Const.HTTP_HEADER_AUTHORIZATION, "OAuth " + accessToken);
            }
        }
    }

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
                        if (receiptEndpoint == SaveReceiptCall.MWS) {
                            contentType = "image/x-png";
                        } else {
                            contentType = "image/png";
                        }
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
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    public String getServiceEndpointURI() {
        String retVal = null;
        switch (receiptEndpoint) {
        case MWS: {
            StringBuilder strBldr = new StringBuilder(SERVICE_END_POINT);
            strBldr.append('/');
            strBldr.append(imageOrigin);
            retVal = strBldr.toString();
            break;
        }
        case CONNECT_POST_IMAGE: {
            retVal = "/api/image/v1.0/receipt";
            break;
        }
        case CONNECT_POST_IMAGE_REPORT: {
            StringBuilder strBldr = new StringBuilder("/api/image/v1.0/report/");
            strBldr.append(URLEncoder.encode(rptKey));
            retVal = strBldr.toString();
            break;
        }
        case CONNECT_POST_IMAGE_REPORT_ENTRY: {
            StringBuilder strBldr = new StringBuilder("/api/image/v1.0/expenseentry/");
            strBldr.append(URLEncoder.encode(rpeKey));
            retVal = strBldr.toString();
            break;
        }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.ServiceRequest#getRequestName()
     */
    @Override
    protected String getRequestName() {
        String retVal = null;

        StringBuilder strBldr = new StringBuilder(getClass().getSimpleName());
        strBldr.append('(');
        strBldr.append(receiptEndpoint.name());
        strBldr.append(')');
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
        SaveReceiptReply reply = new SaveReceiptReply();

        switch (receiptEndpoint) {
        case MWS: {
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
                    reply = SaveReceiptReply.parseReply(xmlReply);
                } catch (Exception e) {
                    // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                    // Empty response will result in this exception, since a valid response should contain root element
                    IOException ioe = new IOException("Fail to parse xml response");
                    ioe.initCause(e);
                    throw ioe;
                }
                if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    Log.e(Const.LOG_TAG,
                            "SaveReceiptRequest: StatusCode: " + statusCode + ", StatusLine: "
                                    + response.getResponseMessage() + ", response: "
                                    + ((reply.mwsErrorMessage != null) ? reply.mwsErrorMessage : "null") + ".");
                }
            } else {
                // Log the error.
                logError(response, CLS_TAG + ".processResponse");
            }
            break;
        }
        case CONNECT_POST_IMAGE:
        case CONNECT_POST_IMAGE_REPORT:
        case CONNECT_POST_IMAGE_REPORT_ENTRY: {
            int statusCode = response.getResponseCode();
            if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK) {
                InputStream is = new BufferedInputStream(response.getInputStream());
                Encoding encoding = null;
                try {
                    String encodingHeader = response.getContentEncoding();
                    encoding = (encodingHeader != null) ? Encoding.valueOf(encodingHeader) : Xml.Encoding.UTF_8;
                } catch (Exception e) {
                    // Couldn't parse the encoding in the header, so just default to UTF-8;
                    encoding = Xml.Encoding.UTF_8;
                }
                reply = SaveReceiptReply.parseConnectReply(is, encoding);
            } else {
                // Log the error.
                logError(response, CLS_TAG + ".processResponse");
            }
            break;
        }
        }
        // Parse the response or log an error.
        // NOTE: If an entity does not have imaging configured, then the server will return a 500 when attempting
        // to save an image. Additionally, there is a specific error message that gets returned.
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
