/**
 * 
 */
package com.concur.mobile.core.expense.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Calendar;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>GetServiceRequest</code> used to request a mobile expense entry receipt.
 * 
 * @author AndrewK
 */
public class DownloadMobileEntryReceiptRequest extends GetServiceRequest {

    private static final String CLS_TAG = DownloadMobileEntryReceiptRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Expense/GetMobileEntryReceipt";

    /**
     * Contains the local key of the mobile entry whose receipt image will be downloaded.
     */
    public String localKey;

    /**
     * Contains a reference to the mobile entry in to which receipt download information is stored.
     */
    public MobileEntry mobileEntry;

    /**
     * Contains the mobile entry key.
     */
    public String meKey;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(SERVICE_END_POINT);
        strBldr.append('/');
        strBldr.append(meKey);
        strBldr.append("/png/Large");
        return strBldr.toString();
    }

    /**
     * Will construct the service URI to download a mobile entry receipt.
     * 
     * @param meKey
     *            the mobile entry key.
     * @return A URI used to retrieve a mobile entry receipt image.
     */
    public static String getServiceEndPointURI(String meKey) {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(SERVICE_END_POINT);
        strBldr.append('/');
        strBldr.append(meKey);
        strBldr.append("/png/Large");
        return strBldr.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        DownloadMobileEntryReceiptReply reply = new DownloadMobileEntryReceiptReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());

            File receiptImageDir = concurService.getDir(Const.RECEIPT_DIRECTORY, Context.MODE_PRIVATE);
            Calendar cal = Calendar.getInstance();
            String fileName = Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND, cal)
                    + ".png";
            File receiptFile = new File(receiptImageDir, fileName);
            reply.filePath = receiptFile.getAbsolutePath();
            BufferedOutputStream bufOut = null;
            int BUF_SIZE = (256 * 1024);
            try {
                BufferedInputStream bufIn = new BufferedInputStream(is, BUF_SIZE);
                bufOut = new BufferedOutputStream(new FileOutputStream(reply.filePath), BUF_SIZE);
                byte[] data = new byte[BUF_SIZE];
                int bytesRead;
                while ((bytesRead = bufIn.read(data, 0, data.length)) != -1) {
                    bufOut.write(data, 0, bytesRead);
                }
                bufOut.flush();
                bufOut.close();
                bufOut = null;
            } finally {
                if (bufOut != null) {
                    try {
                        bufOut.close();
                        bufOut = null;
                    } catch (IOException ioExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".processResponse: I/O exception closing output stream for file '" + fileName + "'.",
                                ioExc);
                        throw ioExc;
                    }
                }
            }
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
