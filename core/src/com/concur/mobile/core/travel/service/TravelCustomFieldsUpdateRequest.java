/**
 * 
 */
package com.concur.mobile.core.travel.service;

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

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.TravelCustomFieldsConfig;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>PostServiceRequest</code> for posting a set of travel custom fields in order to retrieve an updated set
 * of fields.
 */
public class TravelCustomFieldsUpdateRequest extends PostServiceRequest {

    private static final String CLS_TAG = TravelCustomFieldsUpdateRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Config/UpdateTravelCustomFields";

    /**
     * Contains the list of <code>TravelCustomField</code> objects containing the values to be sent for update.
     */
    public List<TravelCustomField> fields;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        StringBuilder strBldr = new StringBuilder();
        if (fields != null) {
            TravelCustomField.serializeToXMLForWire(strBldr, fields, false);
        }
        return strBldr.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.PostServiceRequest#getPostEntity(com.concur.mobile.core.service.ConcurService)
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
     * @see com.concur.mobile.core.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.core.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        TravelCustomFieldsUpdateReply reply = new TravelCustomFieldsUpdateReply();

        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);
            try {
                reply.config = TravelCustomFieldsConfig.parseTravelCustomFieldsConfig(reply.xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }
}
