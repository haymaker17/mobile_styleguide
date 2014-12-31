/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.service;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Default SAXHandler that extends <code>DefaultHandler</code>. This can be used if all we care about is retrieving the reply's
 * HTTP status code and error message (if any). <br>
 * <br>
 * Sub-classes can override the methods (notably <code>handleResponseElement()</code> if they want to handle the reply in a
 * different way.
 * 
 * @author Chris N. Diaz
 * 
 */
/* package scope */class DefaultReplySAXHandler<T extends ServiceReply> extends DefaultHandler {

    // Fields to help parsing
    private StringBuilder chars;

    // Holders for our parsed data
    private T reply;

    /**
     * Default constructor.
     * 
     * @param reply
     */
    public DefaultReplySAXHandler(T reply) {
        this.chars = new StringBuilder();
        this.reply = reply;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);

        chars.append(ch, start, length);
    }

    /**
     * Handle the opening of all elements. Create data objects as needed for use in endElement().
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

    }

    /**
     * Handle the closing of all elements.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        final String cleanChars = chars.toString().trim();

        // Top-level elements
        handleResponseElement(localName, cleanChars);

        chars.setLength(0);
    }

    /**
     * Handle the response level elements
     * 
     * @param localName
     */
    protected void handleResponseElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("Status")) {
            reply.mwsStatus = cleanChars;
        } else if (localName.equalsIgnoreCase("ErrorMessage")) {
            reply.mwsErrorMessage = cleanChars;
        }
    }

}