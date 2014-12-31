package com.concur.mobile.core.travel.air.service.parser;

import java.util.ArrayList;

import com.concur.mobile.base.service.parser.Parser;
import com.concur.mobile.core.travel.data.Benchmark;

/**
 * Parser for Air Price to Beat
 * 
 * @author RatanK
 * 
 */
public class AirBenchmarkParser implements Parser {

    public ArrayList<Benchmark> airBenchMarks;

    public Benchmark benchmark;

    @Override
    public void startTag(String tag) {
        if (tag.equals("AirBenchmarks")) {
            // Currently we do not support hubs so we always get one Benchmark, hence capacity is set to 1
            airBenchMarks = new ArrayList<Benchmark>(1);
        } else if (tag.equals("AirBenchmark")) {
            benchmark = new Benchmark();
        }
    }

    @Override
    public void handleText(String tag, String text) {
        // we are interested in only the two elements for now.
        // later when we support hubs then we will handle other elements
        if (tag.equalsIgnoreCase("Currency")) {
            benchmark.handleElement(tag, text);
        } else if (tag.equalsIgnoreCase("Price")) {
            benchmark.handleElement(tag, text);
        } else if (tag.equalsIgnoreCase("Destination")) {
            benchmark.handleElement(tag, text);
        } else if (tag.equalsIgnoreCase("RoundTrip")) {
            benchmark.handleElement(tag, text);
        }
    }

    @Override
    public void endTag(String tag) {
        if (tag.equals("AirBenchmark")) {
            airBenchMarks.add(benchmark);
        }
    }
}
