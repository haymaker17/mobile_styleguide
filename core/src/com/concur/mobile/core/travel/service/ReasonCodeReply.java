/**
 * 
 */
package com.concur.mobile.core.travel.service;

import java.util.List;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.data.ReasonCode;

/**
 * An extension of <code>ServiceReply</code> that contains a parsed response to a GetReasonCodes request.
 */
public class ReasonCodeReply extends ServiceReply {

    /**
     * Contains XML reply.
     */
    public String xmlReply;

    /**
     * Contains the parsed reason codes.
     */
    public List<ReasonCode> reasonCodes;

}
