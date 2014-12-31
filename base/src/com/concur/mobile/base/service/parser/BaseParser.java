package com.concur.mobile.base.service.parser;

/**
 * Provides a simple no-op adapter that implements <code>Parser</code> for sub-classes to customize.
 */
public class BaseParser implements Parser {

    @Override
    public void startTag(String tag) {
        // No-op.
    }

    @Override
    public void handleText(String tag, String text) {
        // No-op.
    }

    @Override
    public void endTag(String tag) {
        // No-op.
    }

}
