package com.concur.mobile.core.expense.report.service;

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
import com.concur.mobile.core.util.FormatUtil;

public class GetConditionalFieldActionRequest extends PostServiceRequest {

    private static final String CLS_TAG = GetConditionalFieldActionRequest.class.getSimpleName();

    public String formFieldKey;

    public String formFieldValue;

    /**
     * Contains the service end-point.
     */
    public static final String SERVICE_END_POINT = "/mobile/expense/GetFormFieldDynamicAction";

    @Override
    protected String getServiceEndpointURI() {
        StringBuilder sb = new StringBuilder();
        sb.append(SERVICE_END_POINT);
        sb.append('/');
        sb.append(Const.MOBILE_EXPENSE_USER);
        return sb.toString();
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        GetConditionalFieldActionReply reply = new GetConditionalFieldActionReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            reply = GetConditionalFieldActionReply.parseReply(responseXml);

        } else {
            logError(response, GetConditionalFieldActionReply.CLS_TAG + ".processResponse");
        }
        return reply;
    }

    @Override
    protected HttpEntity getPostEntity(ConcurService concurService) throws ServiceRequestException {
        HttpEntity entity;
        try {
            entity = new StringEntity(buildRequestBody(), Const.HTTP_BODY_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException ue) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: unsupported encoding exception!", ue);
            throw new ServiceRequestException(ue.getMessage());
        }
        return entity;
    }

    @Override
    protected String buildRequestBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("<DynamicFieldCriteria>");
        if (formFieldKey != null && formFieldKey.length() > 0) {
            addElement(sb, "FormFieldKey", formFieldKey);
        }
        if (formFieldValue != null && formFieldValue.length() > 0) {
            addElement(sb, "FormFieldValue", FormatUtil.escapeForXML(formFieldValue));
        }
        sb.append("</DynamicFieldCriteria>");
        return sb.toString();
    }
}
