/**
 * 
 */
package com.concur.mobile.core.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;

/**
 * An extension of <code>PostServiceRequest</code> for the purposes of querying the server for SSO information.
 * 
 * @author andy
 */
public class CorpSsoQueryRequest extends PostServiceRequest {

    private static final String CLS_TAG = CorpSsoQueryRequest.class.getSimpleName();

    private static final String SERVICE_END_POINT = "/mobile/MobileSession/CorpSsoQuery";

    /**
     * Contains the company code used to retrieve SSO login information.
     */
    public String companyCode;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#isSessionRequired()
     */
    @Override
    protected boolean isSessionRequired() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        String retVal = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<SsoQuery>");
        ViewUtil.addXmlElement(strBldr, "CompanyCode", companyCode);
        strBldr.append("</SsoQuery>");
        retVal = strBldr.toString();
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#getPostEntity(com.concur.mobile.service.ConcurService)
     */
    @Override
    protected HttpEntity getPostEntity(ConcurService concurService) throws ServiceRequestException {
        HttpEntity entity = null;
        try {
            final String buildRequestBody = buildRequestBody();
            entity = new StringEntity(buildRequestBody, Const.HTTP_BODY_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: unsupported encoding exception!", uee);
            throw new ServiceRequestException(uee.getMessage());
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
        CorpSsoQueryReply reply = new CorpSsoQueryReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = CorpSsoQueryReply.parseXMLReply(responseXml);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
        }
        return reply;
    }

}
