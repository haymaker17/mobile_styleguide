/**
 * 
 */
package com.concur.mobile.core.service;

/**
 * Base class modeling a service reply.
 * 
 * @author AndrewK
 */
public class ServiceReply {

    /**
     * Contains the status embedded in the MWS service reply.
     */
    public String mwsStatus;

    /**
     * Contains the error message embedded in the MWS service reply.
     */
    public String mwsErrorMessage;

    /**
     * Contains the HTTP header status code.
     */
    public int httpStatusCode;

    /**
     * Contains the HTTP status text.
     */
    public String httpStatusText;

}
