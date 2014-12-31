/**
 * 
 */
package com.concur.mobile.core.service;

import java.io.IOException;

/**
 * An extension of <code>IOException</code> that indicates the service request could not be formed. I.e., something bad happened
 * in the formation of the request.
 * 
 * @author AndrewK
 */
public class ServiceRequestException extends IOException {

    /**
     * Contains default serial version ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an instance of <code>ServiceRequestException</code> with no detail message.
     */
    public ServiceRequestException() {
    }

    /**
     * Constructs an instance of <code>ServiceRequestException</code> with a detail message.
     * 
     * @param detailMessage
     *            the detail message.
     */
    public ServiceRequestException(String detailMessage) {
        super(detailMessage);
    }

}
