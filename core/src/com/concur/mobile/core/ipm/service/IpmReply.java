package com.concur.mobile.core.ipm.service;

import java.io.Serializable;
import java.util.ArrayList;

import com.concur.mobile.core.ipm.data.IpmMsg;

/**
 * 
 * An extension of <code>ServiceReply</code> for handling the response to a IPMRequest request.
 * 
 * @author tejoa
 * 
 */
public class IpmReply implements Serializable {

    /**
     * auto generated
     */
    private static final long serialVersionUID = 1L;

    public ArrayList<IpmMsg> ipmMsgs;

    // /**
    // * parse ipm response
    // *
    // * @param responseXml
    // * @return
    // */
    // public static IpmReply parseXMLReply(String responseXml) {
    //
    // IpmReply reply = null;
    //
    // SAXParserFactory factory = SAXParserFactory.newInstance();
    // try {
    // SAXParser parser = factory.newSAXParser();
    // IpmMsgSAXHandler handler = new IpmMsgSAXHandler();
    // parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
    // reply = new IpmReply();
    // reply.ipmMsgs = handler.getIpmMsgs();
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // return reply;
    //
    // }
}
