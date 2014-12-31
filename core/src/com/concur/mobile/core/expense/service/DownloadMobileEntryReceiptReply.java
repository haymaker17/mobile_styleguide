/**
 * 
 */
package com.concur.mobile.core.expense.service;

import com.concur.mobile.core.service.ServiceReply;

/**
 * An extension of <code>ServiceReply</code> for storing the result of a <code>DownloadMobileEntryReceiptRequest</code>.
 * 
 * @author AndrewK
 */
public class DownloadMobileEntryReceiptReply extends ServiceReply {

    /**
     * Contains the absolute path of the file containing the receipt image data. If this value is <code>null</code> with an HTTP
     * status code of OK and an MWS status code of 'success', then it means that there is no image data!
     */
    public String filePath;

}
