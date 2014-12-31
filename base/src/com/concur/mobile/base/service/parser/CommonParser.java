package com.concur.mobile.base.service.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * A common parsing class that will use the {@link XmlPullParser} to parse a stream. Secondary parsers are supported with a
 * registration mechanism to allow modular parsing.
 * 
 */
public class CommonParser implements Parser {

    protected HashMap<String, Parser> registeredParsers;
    protected Stack<Parser> activeParsers;
    protected XmlPullParser xpp;

    public CommonParser(XmlPullParser xpp) {
        this.xpp = xpp;
        registeredParsers = new HashMap<String, Parser>();
        activeParsers = new Stack<Parser>();
    }

    /**
     * Gets the current instance of <code>XmlPullParser</code> being used by this parser.
     * 
     * @return returns the current instance of <code>XmlPullParser</code> being used by this parser.
     */
    public final XmlPullParser getXmlPullParser() {
        return xpp;
    }

    /**
     * Registers a class that implements the Parser interface. Once registered, this parser will be responsible for parsing the
     * specified node and all of its children. Only secondary parsers can be registered. Attempts to register the common parser
     * will be ignored.
     * 
     * @param parser
     *            A {@link Parser} implementation that will handle specific nodes.
     * @param nodeName
     *            The name of the desired node to parse.
     */
    public final void registerParser(Parser parser, String nodeName) {
        if (parser != this) {
            // Only register non-base parsers
            registeredParsers.put(nodeName, parser);
        }
    }

    /**
     * Will unregister a class that implements the Parser interface.
     * 
     * @param parser
     *            A {@link Parser} implementation that will handle specific nodes.
     * @param nodeName
     *            The name of the desired node to parse.
     */
    public final void unregisterParser(Parser parser, String nodeName) {
        if (parser != this) {
            // Only unregister non-base parsers.
            registeredParsers.remove(nodeName);
        }
    }

    /**
     * Return the {@link Parser} that is registered to handle a specific node or the main {@link CommonParser}.
     * 
     * @param nodeName
     *            The name of the node being looked for.
     * @return A {@link Parser} implementation. If the requested node is registered, the registered secondary {@link Parser} will
     *         be returned. If there is no registration for the request node then the running {@link CommonParser} will be
     *         returned.
     */
    public final Parser findParser(String nodeName) {
        Parser parser = null;
        if (registeredParsers != null) {
            parser = (Parser) registeredParsers.get(nodeName);
        }

        if (parser == null) {
            parser = this;
        }

        return parser;
    }

    /**
     * Iterate the provided {@link XmlPullParser} and handle elements as found. If a secondary {@link Parser} is registered for a
     * node then it will be used to handle the node and all descendants.
     * 
     * @param xpp
     *            An {@link XmlPullParser} that has been initialized with the desired input
     * @throws XmlPullParserException
     * @throws IOException
     */
    public final void parse() throws XmlPullParserException, IOException {

        // A holder variable to track the name of the node being processed
        String tag = "";

        // The parser currently handling events. Starts with this
        // CommonParser.
        Parser currentParser = this;

        int eventType = xpp.getEventType();

        // Iterate the entire XML document
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {

            case XmlPullParser.START_TAG:

                tag = xpp.getName();

                if (currentParser == this) {
                    // Give other parsers a chance to jump in
                    currentParser = findParser(tag);
                } else {
                    Parser nextParser = findParser(tag);
                    if (nextParser != this && currentParser != nextParser) {
                        // This is a registered node and it is not us.
                        // Switch but save the current parser for
                        // resumption later.
                        activeParsers.push(currentParser);
                        currentParser = nextParser;
                    }
                }

                // Hand off the event to the appropriate parser
                currentParser.startTag(tag);
                break;

            case XmlPullParser.TEXT:

                String t = xpp.getText();

                // Hand off the event to the appropriate parser
                currentParser.handleText(tag, t);
                break;

            case XmlPullParser.END_TAG:

                tag = xpp.getName();

                // Hand off the event to the appropriate parser
                currentParser.endTag(tag);

                if (currentParser != this && findParser(tag) == currentParser) {
                    // Reset the parser if we have reached the end of
                    // the registered tag
                    if (!activeParsers.isEmpty()) {
                        // Grab the previous parser
                        currentParser = activeParsers.pop();
                    } else {
                        // Fall back to this CommonParser
                        currentParser = this;
                    }
                }
                break;
            }
            eventType = xpp.next();
        }
    }

    @Override
    public void startTag(String tag) {
    }

    @Override
    public void handleText(String tag, String text) {
    }

    @Override
    public void endTag(String tag) {
    }
}