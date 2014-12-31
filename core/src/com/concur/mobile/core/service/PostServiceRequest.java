package com.concur.mobile.core.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;

/**
 * An extension of <code>ServiceRequest</code> for posting.
 * 
 * @author AndrewK
 */
public abstract class PostServiceRequest extends ServiceRequest {

    /**
     * Contains the post request body.
     */
    protected String requestBody;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getRequestBase(com.concur.mobile.service.ConcurService)
     */
    @Override
    protected HttpRequestBase getRequestBase(ConcurService concurService) throws ServiceRequestException {
        HttpPost request = null;
        HttpEntity entity = getPostEntity(concurService);
        request = new HttpPost();
        request.setEntity(entity);
        request.addHeader(Const.HTTP_HEADER_CONTENT_TYPE, getContentType());
        return request;
    }

    /**
     * Gets the instance of <code>HttpEntity</code> that comprises the body of the request.
     * 
     * @param concurService
     *            a reference to the concur service.
     * 
     * @return the instance of <code>HttpEntity</code> comprising body of the request.
     */
    protected abstract HttpEntity getPostEntity(ConcurService concurService) throws ServiceRequestException;

    /**
     * Gets the content type of the body of the post request.
     * 
     * @return the content type [Default: text/xml].
     */
    protected String getContentType() {
        return "text/xml";
    }

    protected static void addElement(StringBuilder body, String elementName, String elementValue) {
        FormatUtil.addXMLElement(body, elementName, elementValue);
    }

    // Simplifies areas in which elements with double values are added.
    protected static void addElement(StringBuilder body, String elementName, Double elementValue) {
        String elementValueToString = (elementValue == null ? null : Double.toString(elementValue));
        FormatUtil.addXMLElement(body, elementName, elementValueToString);
    }

    protected abstract String buildRequestBody();

}
