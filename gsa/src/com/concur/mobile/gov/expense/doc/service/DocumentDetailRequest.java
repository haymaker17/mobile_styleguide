package com.concur.mobile.gov.expense.doc.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.content.ContentValues;
import android.util.Log;

import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.doc.data.DsDocDetailInfo;

public class DocumentDetailRequest extends PostServiceRequest {

    private static final String CLS_TAG = DocumentDetailRequest.class.getSimpleName();
    public static final String SERVICE_END_POINT = "/Mobile/GovTravelManager/GetTMDocDetail";
    public String docName;
    public String docType;
    public String travid;

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
        DocumentDetailReply reply = new DocumentDetailReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            reply = DocumentDetailReply.parseXMLReply(responseXml);
            // if (reply != null) {
            DsDocDetailInfo info = reply.detailInfo;
            if (info != null) {
                MobileDatabase mdb = concurService.getMobileDatabase();
                String userID = concurService.prefs.getString(Const.PREF_USER_ID, null);
                Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                info.userID = userID;
                info.lastUsed = now;
                info.docType = docType;
                ContentValues contentValues = DsDocDetailInfo.getContentVals(info);
                if (!(mdb
                    .insertGovDocument(userID, info.travelerId, info.documentName, info.docType, contentValues))) {
                    Log.e(Const.LOG_TAG, CLS_TAG + " .processResponse: DsDocDetailInfo insertion is failed");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + " .processResponse: DsDocDetailInfo is null");
            }
        }
        return reply;
    }

    @Override
    protected String buildRequestBody() {
        StringBuilder body = new StringBuilder();
        body.append("<TMDocRequest>");
        addElement(body, "docName", docName);
        addElement(body, "docType", docType);
        if (travid != null) {
            addElement(body, "travid", travid);
        }
        body.append("</TMDocRequest>");
        return body.toString();
    }
}
