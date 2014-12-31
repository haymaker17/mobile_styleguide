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

public class NotificationRegisterRequest extends PostServiceRequest {

    private static final String CLS_TAG = NotificationRegisterRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Notification/Register";

    public String token;
    public boolean isTesting;

    @Override
    protected String buildRequestBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("<DeviceInfo>");
        ViewUtil.addXmlElementYN(sb, "IsTest", isTesting);
        addElement(sb, "PhoneId", token);
        addElement(sb, "Platform", "Android Phone");
        sb.append("</DeviceInfo>");

        return sb.toString();
    }

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
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        ActionStatusServiceReply reply = new ActionStatusServiceReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = ActionStatusServiceReply.parseReply(responseXml);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            reply.mwsStatus = Const.REPLY_STATUS_FAIL;
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
