package com.concur.mobile.base.service.parser;


/**
 * A basic parsing interface used by any class that wants to handle parse events from the {@link CommonParser} implementation.
 */
public interface Parser {

    /**
     * This method is invoked by the CommonParser when a new tag is encountered in the stream being parsed. For XML this
     * corresponds to the opening element of a node.
     * 
     * @param tag
     *            The name of the new node
     */
    public void startTag(String tag);

    /**
     * This method is invoked by the CommonParser when a text section has been completely read from a stream.
     * 
     * @param tag
     *            The name of the parent node of the text
     * @param text
     *            The text read from the stream
     */
    public void handleText(String tag, String text);

    /**
     * This method is invoked by the CommonParser when an end tag is encountered in the stream being parsed.
     * 
     * @param tag
     */
    public void endTag(String tag);
}