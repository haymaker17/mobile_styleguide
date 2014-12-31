/**
 * 
 */
package com.concur.mobile.core.expense.charge.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>PostServiceRequest</code> to handle deleting mobile entries.
 * 
 * @author AndrewK
 */
public class DeleteMobileEntriesRequest extends PostServiceRequest {

    private static final String CLS_TAG = DeleteMobileEntriesRequest.class.getSimpleName();

    private static final String SERVICE_END_POINT = "/mobile/Expense/DeleteMobileEntries";

    public ArrayList<MobileEntry> mobileEntries;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#getPostEntity(com.concur.mobile.service.ConcurService)
     */
    @Override
    protected HttpEntity getPostEntity(ConcurService concurService) throws ServiceRequestException {
        HttpEntity entity = null;
        try {
            entity = new StringEntity(buildRequestBody(), Const.HTTP_BODY_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException unSupEncExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: unsupported encoding exception!", unSupEncExc);
            throw new ServiceRequestException(unSupEncExc.getMessage());
        }
        return entity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        DeleteMobileEntriesReply reply = new DeleteMobileEntriesReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = DeleteMobileEntriesReply.parseXMLReply(responseXml);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

    @Override
    public String buildRequestBody() {
        String requestBody = null;

        if (mobileEntries != null) {
            Iterator<MobileEntry> iterator = mobileEntries.iterator();
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("<a:ArrayOfstring xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">");
            while (iterator.hasNext()) {
                MobileEntry mobileEntry = iterator.next();
                strBldr.append("<a:string>");
                strBldr.append(mobileEntry.getMeKey());
                strBldr.append("</a:string>");
            }
            strBldr.append("</a:ArrayOfstring>");
            requestBody = strBldr.toString();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildDeleteMobileEntriesPostBodyXML: mobile entries is null!");
        }
        return requestBody;
    }

}
