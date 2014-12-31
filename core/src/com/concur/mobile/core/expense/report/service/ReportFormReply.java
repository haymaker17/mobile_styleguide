/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.expense.report.service;

import com.concur.mobile.core.service.ServiceReply;

/**
 * Generic extension of <code>ServiceReply</code> for getting the list of form fields for a report.
 * 
 * @author Chris N. Diaz
 * 
 */
public class ReportFormReply extends ServiceReply {

    /**
     * Contains the XML representation of the response body.
     */
    public String xmlReply;
}
