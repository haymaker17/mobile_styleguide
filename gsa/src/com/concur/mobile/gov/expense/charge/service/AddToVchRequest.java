package com.concur.mobile.gov.expense.charge.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.charge.data.MobileExpense;

public class AddToVchRequest extends PostServiceRequest {

    private static final String CLS_TAG = AddToVchRequest.class.getSimpleName();
    public static final String SERVICE_END_POINT = "/Mobile/GovTravelManager/AttachExpenseToDocument";
    public String docType;
    public String vchNum;
    public HashSet<MobileExpense> mobileExpList;

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
        AddToVchReply reply = new AddToVchReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            reply = AddToVchReply.parseXml(responseXml);
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        }
        return reply;
    }

    @Override
    protected String buildRequestBody() {
        StringBuilder body = new StringBuilder();
        body.append("<AttachExpenseToDocumentRequest>");
        addElement(body, "docType", docType);
        body.append("<expenseIds  xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">");
        Iterator<MobileExpense> ckItemIter = mobileExpList.iterator();
        while (ckItemIter.hasNext()) {
            MobileExpense exp = ckItemIter.next();
            body.append("<a:string>");
            body.append(exp.getCcexpid());
            body.append("</a:string>");
        }
        body.append("</expenseIds>");
        addElement(body, "vchnum", vchNum);
        body.append("</AttachExpenseToDocumentRequest>");
        return body.toString();
    }
}
