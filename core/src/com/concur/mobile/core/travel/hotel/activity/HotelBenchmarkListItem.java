/**
 * 
 */
package com.concur.mobile.core.travel.hotel.activity;

import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.HotelBenchmark;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;

/**
 * An extension of <code>ListItem</code> for displaying a <code>HotelBenchmark</code> item.
 */
public class HotelBenchmarkListItem extends ListItem {

    private static final String CLS_TAG = HotelBenchmarkListItem.class.getSimpleName();

    private HotelBenchmark benchmark;

    /**
     * Constructs an instance of <code>HotelBenchmarkListItem</code> given a benchmark.
     * 
     * @param benchmark
     *            contains the benchmark.
     */
    public HotelBenchmarkListItem(HotelBenchmark benchmark) {
        this.benchmark = benchmark;
    }

    /**
     * Gets the <code>HotelBenchmark</code> object backing this list item.
     * 
     * @return returns the <code>HotelBenchmark</code> object backing this list item.
     */
    public HotelBenchmark getBenchmark() {
        return benchmark;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {

        View benchmarkView = null;
        LayoutInflater inflater = null;

        if (convertView == null) {
            inflater = LayoutInflater.from(context);
            benchmarkView = inflater.inflate(R.layout.hotel_price_to_beat_row, null);
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

            // location
            TextView locTextView = (TextView) benchmarkView.findViewById(R.id.location);
            StringBuilder locTextBldr = new StringBuilder();
            if (benchmark.getLocationName() != null) {
                locTextBldr.append(benchmark.getLocationName());
            }
            if (benchmark.getSubDivCode() != null) {
                locTextBldr.append(", ");
                locTextBldr.append(benchmark.getSubDivCode());
            }
            locTextView.setText(locTextBldr);
        }

        return benchmarkView;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
