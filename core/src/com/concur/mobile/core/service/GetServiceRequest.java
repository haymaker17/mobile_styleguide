/**
 * 
 */
package com.concur.mobile.core.service;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * An extension of <code>ServiceRequest</code> for handling HTTP GET requests.
 * 
 * @author AndrewK
 */
public abstract class GetServiceRequest extends ServiceRequest {

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getRequestBase(com.concur.mobile.service.ConcurService)
     */
    @Override
    protected HttpRequestBase getRequestBase(ConcurService concurService) throws ServiceRequestException {
        return new HttpGet();
    }
}
