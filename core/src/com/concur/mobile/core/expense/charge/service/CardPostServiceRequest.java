package com.concur.mobile.core.expense.charge.service;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.http.client.methods.HttpRequestBase;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequest;

public class CardPostServiceRequest extends ServiceRequest {

    public String requestBody;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getRequestBase(com.concur.mobile.service.ConcurService)
     */
    @Override
    protected HttpRequestBase getRequestBase(ConcurService concurService) {
        // TODO Auto-generated method stub
        return null;
    }

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
