/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Generic extension of <code>ServiceReply</code> for handling location check-ins.
 * 
 * @author Chris N. Diaz
 * 
 */
public class LocationCheckInReply extends ServiceReply {

    // //////////////////////////////////////////////////////////////////////
    // At the signpost ahead: XML parsing
    // //////////////////////////////////////////////////////////////////////

    public static LocationCheckInReply parseXMLReply(String responseXml) {

        LocationCheckInReply reply = new LocationCheckInReply();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            DefaultReplySAXHandler<LocationCheckInReply> handler = new DefaultReplySAXHandler<LocationCheckInReply>(
                    reply);
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }
}
