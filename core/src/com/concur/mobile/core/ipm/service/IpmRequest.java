package com.concur.mobile.core.ipm.service;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.ipm.data.IpmMsg;
import com.concur.mobile.core.ipm.parser.IpmMsgParser;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

/**
 * An extension of <code>GetServiceRequest</code> for the purposes of querying the server for ipm dfp ads.
 * 
 * @author tejoa
 * 
 */
public class IpmRequest extends CoreAsyncRequestTask {

    public static final String SERVICE_END_POINT = "/mobile/ipm/getmsg";

    public String target;
    protected IpmMsgParser ipmMsgParser;

    public IpmRequest(Context context, int id, BaseAsyncResultReceiver receiver, String target) {
        super(context, id, receiver);

        this.target = target;
    }

    @Override
    protected String getServiceEndpoint() {
        StringBuilder strBldr = new StringBuilder(SERVICE_END_POINT);
        strBldr.append("/?target=");
        strBldr.append(target);
        return strBldr.toString();

    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);
        connection.setReadTimeout(60000);
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;
        ipmMsgParser = new IpmMsgParser(parser, IpmMsgParser.TAG_ARRAY_OF_IPM_MSGS);

        // register the parsers of interest
        parser.registerParser(ipmMsgParser, "IpmResponse");
        try {
            parser.parse();
        } catch (XmlPullParserException e) {
            result = RESULT_ERROR;
            e.printStackTrace();
        } catch (IOException e) {
            result = RESULT_ERROR;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected int onPostParse() {
        int resultcode = RESULT_OK;

        IpmReply reply = new IpmReply();
        reply.ipmMsgs = (ArrayList<IpmMsg>) ipmMsgParser.ipmMsgs;
        ConcurCore core = (ConcurCore) ConcurCore.getContext();

        // search end point invoked
        if (core.getIpmMsgResults() == null) {
            core.setIpmMsgResults(reply);
        } else {
            // invoked by the sub class parsers, so don't update the app object
            resultData.putSerializable("IpmReply", (Serializable) reply);
        }

        return resultcode;
    }

}
