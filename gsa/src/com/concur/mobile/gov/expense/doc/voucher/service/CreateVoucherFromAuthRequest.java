package com.concur.mobile.gov.expense.doc.voucher.service;

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
import com.concur.mobile.gov.expense.doc.service.DocumentListReply;

public class CreateVoucherFromAuthRequest extends PostServiceRequest {

    private static final String CLS_TAG = CreateVoucherFromAuthRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/GovTravelManager/CreateVoucherFromAuth";

    public String travelerId;
    public String authName;
    public String authType;

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

    @Override
    protected String buildRequestBody() {
        if (requestBody == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("<TMDocRequest>");
            addElement(sb, "docName", authName);
            addElement(sb, "docType", authType);
            addElement(sb, "travid", travelerId);
            sb.append("</TMDocRequest>");
            requestBody = sb.toString();
        }

        return requestBody;
    }

    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        DocumentListReply reply = new DocumentListReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            reply = DocumentListReply.parseXml(responseXml);
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
