/**
 * 
 */
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

import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>PostServiceRequest</code> for the purposes of saving a report header.
 * 
 * @author AndrewK
 */
public class SaveReportRequest extends PostServiceRequest {

    private static final String CLS_TAG = SaveReportRequest.class.getSimpleName();

    /**
     * Contains the service end-point.
     */
    public static final String SERVICE_END_POINT = "/mobile/Expense/SaveReport";

    /**
     * The expense report detail object being saved.
     */
    public ExpenseReportDetail expRepDet;

    /**
     * Contains whether the "copy down to child forms" option should be sent on the request URI.
     */
    public boolean copyDownToChildForms;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    public String buildRequestBody() {
        if (requestBody == null) {
            if (expRepDet != null) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append("<Report xmlns=\"http://schemas.datacontract.org/2004/07/Snowbird\" ");
                strBldr.append("xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">");
                ExpenseReportDetail.ReportDetailSAXHandler.serializeToXML(strBldr, expRepDet);
                if (expRepDet.getFormFields() != null) {
                    ExpenseReportDetail.ReportDetailSAXHandler.serializeFormFieldsToXML(strBldr,
                            expRepDet.getFormFields());
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildRequestBody: list of form field views is null!");
                }
                strBldr.append("</Report>");
                requestBody = strBldr.toString();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildRequestBody: detailed expense report is null!");
            }
        }
        return requestBody;
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    public String getServiceEndpointURI() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(SERVICE_END_POINT);
        strBldr.append('/');
        strBldr.append(Const.MOBILE_EXPENSE_USER);
        if (copyDownToChildForms) {
            strBldr.append('/');
            strBldr.append(Const.COPY_DOWN_TO_CHILD_FORMS);
        }
        return strBldr.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        SaveReportReply reply = new SaveReportReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = SaveReportReply.parseXMLReply(responseXml);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;

    }

    @Override
    public ServiceReply process(ConcurService concurService) throws IOException {
        if (expRepDet != null) {
            ExpenseReportFormField name = expRepDet.getFormField("Name");
            if (name != null && "WaltCrash923322".equals(name.getValue())) {
                // To test the sending of crash logs, fail here for this special report name.
                throw new RuntimeException("Invalid report name");
            }
        }
        return super.process(concurService);
    }

}
