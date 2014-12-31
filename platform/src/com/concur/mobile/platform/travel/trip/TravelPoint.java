package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for parsing travel points information.
 */
public class TravelPoint extends BaseParser {

    public static final String CLS_TAG = "TravelPoint";

    private static final String TAG_BENCHMARK = "Benchmark";
    private static final String TAG_BENCHMARK_CURRENCY = "BenchmarkCurrency";
    private static final String TAG_POINTS_POSTED = "PointsPosted";
    private static final String TAG_POINTS_PENDING = "PointsPending";
    private static final String TAG_TOTAL_POINTS = "TotalPoints";

    private static final int CODE_BENCHMARK = 0;
    private static final int CODE_BENCHMARK_CURRENCY = 1;
    private static final int CODE_POINTS_POSTED = 2;
    private static final int CODE_POINTS_PENDING = 3;
    private static final int CODE_TOTAL_POINTS = 4;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_BENCHMARK, CODE_BENCHMARK);
        tagMap.put(TAG_BENCHMARK_CURRENCY, CODE_BENCHMARK_CURRENCY);
        tagMap.put(TAG_POINTS_POSTED, CODE_POINTS_POSTED);
        tagMap.put(TAG_POINTS_PENDING, CODE_POINTS_PENDING);
        tagMap.put(TAG_TOTAL_POINTS, CODE_TOTAL_POINTS);
    }

    public String benchmark;
    public String benchmarkCurrency;
    public String pointsPosted;
    public String pointsPending;
    public String totalPoints;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            if (!TextUtils.isEmpty(text)) {
                switch (tagCode) {
                case CODE_BENCHMARK: {
                    benchmark = text.trim();
                    break;
                }
                case CODE_BENCHMARK_CURRENCY: {
                    benchmarkCurrency = text.trim();
                    break;
                }
                case CODE_POINTS_POSTED: {
                    pointsPosted = text.trim();
                    break;
                }
                case CODE_POINTS_PENDING: {
                    pointsPending = text.trim();
                    break;
                }
                case CODE_TOTAL_POINTS: {
                    totalPoints = text.trim();
                    break;
                }
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }

}
