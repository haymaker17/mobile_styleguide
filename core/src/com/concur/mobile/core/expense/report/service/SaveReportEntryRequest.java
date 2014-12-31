/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail.ExpenseReportEntryDetailSAXHandler;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;

/**
 * An extension of <code>PostServiceRequest</code> for saving information about a report entry.
 * 
 * @author AndrewK
 */
public class SaveReportEntryRequest extends PostServiceRequest {

    private static final String CLS_TAG = SaveReportEntryRequest.class.getSimpleName();

    /**
     * Contains the service end-point.
     */
    public static final String SERVICE_END_POINT = "/Mobile/Expense/SaveReportEntryV4";

    /**
     * A reference to the expense report entry being saved.
     */
    public ExpenseReportEntryDetail expRepEntDet;

    /**
     * Contains whether the "copy down to child forms" option should be sent on the request URI.
     */
    public boolean copyDownToChildForms;

    // MOB-8452
    String selectedPOLKey, selectedEXPKey;

    public String getPolKey() {
        return selectedPOLKey;
    }

    public void setPolKey(String polKey) {
        this.selectedPOLKey = polKey;
    }

    public String getExpKey() {
        return selectedEXPKey;
    }

    public void setExpKey(String expKey) {
        this.selectedEXPKey = expKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    public String buildRequestBody() {
        if (requestBody == null) {
            if (expRepEntDet != null) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append("<ReportEntry xmlns=\"http://schemas.datacontract.org/2004/07/Snowbird\" ");
                strBldr.append("xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">");
                // Serialize the direct attributes.
                ExpenseReportEntryDetailSAXHandler.serializeToXML(strBldr, expRepEntDet);
                // Serialize any form fields.
                List<ExpenseReportFormField> frmFields = expRepEntDet.getFormFields();
                List<TaxForm> taxForms = expRepEntDet.getTaxForm();
                if (frmFields != null && frmFields.size() > 0) {
                    ExpenseReportEntryDetailSAXHandler.serializeFormFieldsToXML(strBldr, frmFields);
                }

                if (taxForms != null && taxForms.size() > 0) {
                    strBldr.append("<TaxForms>");
                    for (TaxForm taxForm : taxForms) {
                        if (taxForm != null && taxForm.taxFormField != null && taxForm.taxFormField.size() > 0) {
                            strBldr.append("<TaxForm>");
                            List<ExpenseReportFormField> taxFrmFld = taxForm.taxFormField;
                            ExpenseReportEntryDetailSAXHandler.serializeFormFieldsToXML(strBldr, taxFrmFld);
                            ViewUtil.addXmlElement(strBldr, "TaxAuthKey", taxForm.taxAuthKey);
                            ViewUtil.addXmlElement(strBldr, "TaxFormKey", taxForm.taxFormKey);
                            strBldr.append("</TaxForm>");
                        }
                    }
                    strBldr.append("</TaxForms>");
                }

                // Serialize any attendees.
                if (expRepEntDet.getAttendees() != null) {
                    strBldr.append("<Attendees>");
                    for (ExpenseReportAttendee attendee : expRepEntDet.getAttendees()) {
                        strBldr.append("<Attendee xmlns=''>");
                        ViewUtil.addXmlElement(strBldr, "Amount", attendee.amount);
                        ViewUtil.addXmlElement(strBldr, "AtnKey", attendee.atnKey);
                        ViewUtil.addXmlElement(strBldr, "ExternalId", attendee.externalId);
                        ViewUtil.addXmlElement(strBldr, "InstanceCount", attendee.instanceCount);
                        ViewUtil.addXmlElementYN(strBldr, "IsAmountEdited", attendee.isAmountEdited);
                        ViewUtil.addXmlElement(strBldr, "VersionNumber", attendee.versionNumber);
                        strBldr.append("</Attendee>");
                    }
                    strBldr.append("</Attendees>");
                }
                ViewUtil.addXmlElement(strBldr, "NoShowCount", expRepEntDet.noShowCount);

                strBldr.append("</ReportEntry>");
                requestBody = strBldr.toString();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildRequestBody: expRepEntDet is null!");
            }
        }
        return requestBody;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#getPostEntity(com.concur.mobile .service.ConcurService)
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
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http .HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        SaveReportEntryReply reply = new SaveReportEntryReply();

        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String xmlReply = readStream(is, encoding);
            try {
                reply = SaveReportEntryReply.parseReply(xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
