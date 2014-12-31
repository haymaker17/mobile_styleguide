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

public class GetTaxFormRequest extends PostServiceRequest {

    public static final String CLS_TAG = GetTaxFormRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/expense/GetTaxFormsV4";

    // The report key.
    public String expKey;
    public String lnKey;
    public String date;
    public String reportEntryKey;
    public String ctryCode;
    public String ctrySubCode;

    @Override
    protected String getServiceEndpointURI() {
        StringBuilder sb = new StringBuilder();
        sb.append(SERVICE_END_POINT);
        if (reportEntryKey != null) {
            sb.append('/').append(reportEntryKey);
        }
        String serverURI = sb.toString();
        return serverURI;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        GetTaxFormReply reply = new GetTaxFormReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = GetTaxFormReply.parseXml(responseXml);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            reply.xmlReply = responseXml;
        } else {
            logError(response, GetTaxFormRequest.CLS_TAG + ".processResponse");
        }
        return reply;

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
            entity = new StringEntity(buildRequestBody(), Const.HTTP_BODY_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException unSupEncExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: unsupported encoding exception!", unSupEncExc);
            throw new ServiceRequestException(unSupEncExc.getMessage());
        }
        return entity;
    }

    @Override
    protected String buildRequestBody() {
        String requestBody = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<TaxCriteria>");
        if (ctryCode != null && ctryCode.length() > 0) {
            addElement(strBldr, "CtryCode", ctryCode);
        }
        if (ctrySubCode != null && ctrySubCode.length() > 0) {
            addElement(strBldr, "CtrySubCode", ctrySubCode);
        }
        if (expKey != null && expKey.length() > 0) {
            addElement(strBldr, "ExpKey", expKey);
        }
        if (lnKey != null && lnKey.length() > 0) {
            addElement(strBldr, "LnKey", lnKey);
        }
        if (date != null && date.length() > 0) {
            date = date.substring(0, date.length() - 9);
            addElement(strBldr, "TransactionDate", date);
        }
        strBldr.append("</TaxCriteria>");
        requestBody = strBldr.toString();
        return requestBody;
    }
}
