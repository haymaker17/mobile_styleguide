/**
 * 
 */
package com.concur.mobile.core.travel.air.activity;

import java.util.Locale;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.Benchmark;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;

/**
 * An extension of <code>ListItem</code> for displaying a <code>Benchmark</code> item for Air Price to Beat.
 * 
 * @author RatanK
 */
public class AirBenchmarkListItem extends ListItem {

    private static final String CLS_TAG = AirBenchmarkListItem.class.getSimpleName();

    private Benchmark benchmark;

    // As MWS do not send the Location as display name and at present mobile support only 1 Air Price to Beat,
    // so use the display name
    private String arriveIATADisplayName;

    /**
     * Constructs an instance of <code>AirBenchmarkListItem</code> given a benchmark.
     * 
     * @param benchmark
     *            contains the benchmark.
     */
    public AirBenchmarkListItem(Benchmark benchmark) {
        this.benchmark = benchmark;
    }

    // As MWS do not send the Location as display name and at present mobile support only 1 Air Price to Beat,
    // so use the display name
    public AirBenchmarkListItem(Benchmark benchmark, String arriveIATADisplayName) {
        this.benchmark = benchmark;
        this.arriveIATADisplayName = arriveIATADisplayName;
    }

    /**
     * Gets the <code>Benchmark</code> object backing this list item.
     * 
     * @return returns the <code>Benchmark</code> object backing this list item.
     */
    public Benchmark getBenchmark() {
        return benchmark;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {

        View benchmarkView = null;
        LayoutInflater inflater = null;

        if (convertView == null) {
            inflater = LayoutInflater.from(context);
            benchmarkView = inflater.inflate(R.layout.air_price_to_beat_row, null);
        } else {
            benchmarkView = convertView;
        }

        if (benchmark != null) {
            // price
            TextView priceTextView = (TextView) benchmarkView.findViewById(R.id.price);
            if (benchmark.getPrice() == null) {
                priceTextView.setText(R.string.general_price_to_beat_price_unavailable);
                priceTextView.setTextAppearance(context, R.style.ListCellSubHeaderText);
            } else {
                Locale loc = context.getResources().getConfiguration().locale;
                priceTextView.setText(FormatUtil.formatAmount(benchmark.getPrice(), loc, benchmark.getCrnCode(), true));
                priceTextView.setTextAppearance(context, R.style.ListCellHeaderText);
            }

            // arrival IATA location as display name
            TextView iataTextView = (TextView) benchmarkView.findViewById(R.id.iata);
            iataTextView.setText(arriveIATADisplayName);
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".buildView: benchmark is null");
        }

        return benchmarkView;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
