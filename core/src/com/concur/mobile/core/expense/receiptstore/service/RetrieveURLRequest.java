/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>GetServiceRequest</code> to retrieve the contents of a URL.
 */
public class RetrieveURLRequest extends GetServiceRequest {

    private static final String CLS_TAG = RetrieveURLRequest.class.getSimpleName();

    // Contains the URL.
    public String urlStr;

    // Contains the file path.
    public File filePath;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getURI()
     */
    @Override
    protected String getURI() throws URISyntaxException {
        String uriStr = "";
        try {
            URL url = new URL(urlStr);
            uriStr = url.toURI().toString();
        } catch (MalformedURLException mlfUrlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getURI: malformed URL exception", mlfUrlExc);
        }
        return uriStr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
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
        ServiceReply reply = new ServiceReply();
        int statusCode = response.getResponseCode();
        if (statusCode == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());

            BufferedOutputStream bufOut = null;
            int BUF_SIZE = (256 * 1024);
            try {
                BufferedInputStream bufIn = new BufferedInputStream(is, BUF_SIZE);
                bufOut = new BufferedOutputStream(new FileOutputStream(filePath), BUF_SIZE);
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
                        Log.e(Const.LOG_TAG,
                                CLS_TAG + ".processResponse: I/O exception closing output stream for file '"
                                        + filePath.getAbsolutePath() + "'.", ioExc);
                        throw ioExc;
                    }
                }
            }
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String xmlReply = readStream(is, encoding);
            reply = ActionStatusServiceReply.parseReply(xmlReply);
            if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Log.e(Const.LOG_TAG,
                        "RetrieveURLRequest: StatusCode: " + statusCode + ", StatusLine: "
                                + response.getResponseMessage() + ", response: "
                                + ((reply.mwsErrorMessage != null) ? reply.mwsErrorMessage : "null") + ".");
            }
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }
}
