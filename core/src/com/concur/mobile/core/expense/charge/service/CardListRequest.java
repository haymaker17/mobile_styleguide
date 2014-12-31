/**
 * 
 */
package com.concur.mobile.core.expense.charge.service;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;

/**
 * An extension of <code>GetServiceRequest</code> for retrieving and processing card transaction information.
 * 
 * @author AndrewK
 */
public class CardListRequest extends GetServiceRequest {

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
