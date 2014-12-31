package com.concur.mobile.gov.expense.doc.stamp.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;

public class StampTMDocumentRequest extends PostServiceRequest {

    private static final String CLS_TAG = StampTMDocumentRequest.class.getSimpleName();
    public static final String SERVICE_END_POINT = "/Mobile/GovTravelManager/StampTMDocument";
    public String docName;
    public String docType;
    public String travid;
    public String stampName;
    public String reasonCode;
    public String returnTo;
    public String signkey;

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

    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService)
        throws IOException
    {
        StampTMDocumentResponse reply = new StampTMDocumentResponse();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            reply = StampTMDocumentResponse.parseXmlResponse(responseXml);
        }
        return reply;
    }

    @Override
    protected String buildRequestBody() {
        StringBuilder body = new StringBuilder();
        body.append("<StampTMDocumentRequest>");
        addElement(body, "docName", docName);
        addElement(body, "docType", docType);
        addElement(body, "reasonCode", reasonCode);
        addElement(body, "returnTo", returnTo);
        addElement(body, "sigkey", signkey);
        addElement(body, "stampName", stampName);
        addElement(body, "travid", travid);
        body.append("</StampTMDocumentRequest>");
        return body.toString();
    }
}
