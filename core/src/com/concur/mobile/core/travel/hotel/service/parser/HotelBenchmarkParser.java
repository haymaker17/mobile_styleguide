package com.concur.mobile.core.travel.hotel.service.parser;

import java.util.ArrayList;

import com.concur.mobile.base.service.parser.Parser;
import com.concur.mobile.core.travel.data.HotelBenchmark;

/**
 * Parser for Hotel Price to Beat
 * 
 * @author RatanK
 * 
 */
public class HotelBenchmarkParser implements Parser {

    public ArrayList<HotelBenchmark> hotelBenchMarks;

    public HotelBenchmark benchmark;

    @Override
    public void startTag(String tag) {
        if (tag.equals("Benchmarks")) {
            hotelBenchMarks = new ArrayList<HotelBenchmark>();
        } else if (tag.equals("HotelBenchmark")) {
            benchmark = new HotelBenchmark();
        }
    }

    @Override
    public void handleText(String tag, String text) {
        benchmark.handleElement(tag, text);
    }

    @Override
    public void endTag(String tag) {
        if (tag.equals("HotelBenchmark")) {
            hotelBenchMarks.add(benchmark);
        }
    }
}
