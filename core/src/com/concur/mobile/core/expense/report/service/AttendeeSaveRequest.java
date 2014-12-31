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

import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.ControlType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.DataType;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;

/**
 * An extension of <code>PostServiceRequest</code> for posting an attendee save request.
 */
public class AttendeeSaveRequest extends PostServiceRequest {

    private static final String CLS_TAG = AttendeeSaveRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Expense/SaveAttendee";

    /**
     * Contains the attendee being saved.
     */
    public ExpenseReportAttendee attendee;

    // Whether to force the save, disregarding duplicates
    public boolean force;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {

        // If the fields for the attendee do not include AtnTypeKey then we must
        // synthesize
        // one to get the server to save properly
        ExpenseReportFormField ff = attendee.findFormFieldByFieldId(ExpenseReportAttendee.ATTENDEE_TYPE_KEY);
        if (ff == null) {
            ff = new ExpenseReportFormField(ExpenseReportAttendee.ATTENDEE_TYPE_KEY, attendee.atnTypeName,
                    attendee.atnTypeKey, AccessType.RW, ControlType.PICK_LIST, DataType.INTEGER, true);
            attendee.getFormFields().add(0, ff);
        }

        // MOB-15847 and CRMC-44098
        // In the 5 checks below, we are manually adding form fields for AtnTypeCode, AtnTypeName, ExternalId, FirstName and
        // LastName ids. This is because MWS checks for these form fields, and in certain customer cases, we're not getting these
        // form fields from MWS so we're forced to caress the data until MWS is fixed. iOS has handled this situation in the same
        // fashion since pre-2011, and while this will need to be fixed on MWS, it is blocking customers right now.

        // ExternalId
        ExpenseReportFormField externalIdFormField = attendee
                .findFormFieldByFieldId(ExpenseReportAttendee.EXTERNAL_FIELD_ID);
        if (externalIdFormField == null) {
            externalIdFormField = new ExpenseReportFormField(ExpenseReportAttendee.EXTERNAL_FIELD_ID, null,
                    attendee.externalId, AccessType.RW, ControlType.EDIT, DataType.VARCHAR, true);

            attendee.getFormFields().add(0, externalIdFormField);
        }

        // AtnTypeName
        ExpenseReportFormField atnTypeNameFormField = attendee
                .findFormFieldByFieldId(ExpenseReportAttendee.ATTENDEE_TYPE_NAME);
        if (atnTypeNameFormField == null) {
            atnTypeNameFormField = new ExpenseReportFormField(ExpenseReportAttendee.ATTENDEE_TYPE_NAME, null,
                    attendee.atnTypeName, AccessType.RW, ControlType.EDIT, DataType.VARCHAR, true);

            attendee.getFormFields().add(0, atnTypeNameFormField);
        }

        // AtnTypeCode
        ExpenseReportFormField atnTypeCodeFormField = attendee
                .findFormFieldByFieldId(ExpenseReportAttendee.ATTENDEE_TYPE_CODE);
        if (atnTypeCodeFormField == null) {
            atnTypeCodeFormField = new ExpenseReportFormField(ExpenseReportAttendee.ATTENDEE_TYPE_CODE, null,
                    attendee.atnTypeCode, AccessType.RW, ControlType.EDIT, DataType.VARCHAR, true);

            attendee.getFormFields().add(0, atnTypeCodeFormField);
        }

        // firstName
        ExpenseReportFormField firstNameFormField = attendee
                .findFormFieldByFieldId(ExpenseReportAttendee.FIRST_NAME_FIELD_ID);
        String atnFirstName = attendee.getFirstName();

        if (firstNameFormField == null && atnFirstName != null) {
            firstNameFormField = new ExpenseReportFormField(ExpenseReportAttendee.FIRST_NAME_FIELD_ID, null,
                    atnFirstName, AccessType.RW, ControlType.EDIT, DataType.VARCHAR, true);

            attendee.getFormFields().add(firstNameFormField);
        }

        // lastName
        ExpenseReportFormField lastNameFormField = attendee
                .findFormFieldByFieldId(ExpenseReportAttendee.LAST_NAME_FIELD_ID);
        String atnLastName = attendee.getLastName();

        if (lastNameFormField == null && atnLastName != null) {
            lastNameFormField = new ExpenseReportFormField(ExpenseReportAttendee.LAST_NAME_FIELD_ID, null, atnLastName,
                    AccessType.RW, ControlType.EDIT, DataType.VARCHAR, true);

            attendee.getFormFields().add(lastNameFormField);
        }

        String body = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<Attendee>");
        if (!force && attendee.atnKey != null) {
            // If we are forcing then always treat it as new: no atnKey
            ViewUtil.addXmlElement(strBldr, "AtnKey", attendee.atnKey);
        }
        // Save external Id.
        ViewUtil.addXmlElement(strBldr, "ExternalId", attendee.externalId);

        ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler.serializeFieldsToXML(strBldr, attendee, true);
        ViewUtil.addXmlElement(strBldr, "VersionNumber", attendee.versionNumber);
        strBldr.append("</Attendee>");
        body = strBldr.toString();
        return body;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#getPostEntity(com.concur .mobile.service.ConcurService)
     */
    @Override
    protected HttpEntity getPostEntity(ConcurService concurService) throws ServiceRequestException {
        HttpEntity entity = null;
        try {
            entity = new StringEntity(buildRequestBody(), Const.HTTP_BODY_CHARACTER_ENCODING);
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
        if (force) {
            StringBuilder sb = new StringBuilder(SERVICE_END_POINT);
            sb.append("/ignoreDuplicates");
            return sb.toString();
        } else {
            return SERVICE_END_POINT;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http .HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        AttendeeSaveReply reply = new AttendeeSaveReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = AttendeeSaveReply.parseXMLReply(responseXml);
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

}
