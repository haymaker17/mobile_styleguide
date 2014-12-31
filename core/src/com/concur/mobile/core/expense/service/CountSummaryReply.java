/**
 * 
 */
package com.concur.mobile.core.expense.service;

import com.concur.mobile.core.expense.data.CountSummary;
import com.concur.mobile.core.service.ServiceReply;

/**
 * A service reply object encapsulating the county summary response.
 * 
 * @author AndrewK
 */
public class CountSummaryReply extends ServiceReply {

    public CountSummary countSummary;

    public String xmlReply;

}
