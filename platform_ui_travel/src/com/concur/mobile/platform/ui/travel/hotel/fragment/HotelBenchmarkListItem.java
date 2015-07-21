package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.concur.mobile.platform.travel.search.hotel.HotelBenchmark;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.ui.common.view.ListItem;
import com.concur.mobile.platform.ui.travel.R;

import java.util.Locale;

/**
 * An extension of <code>ListItem</code> for displaying a <code>HotelBenchmark</code> item.
 * <p/>
 * Created by RatanK.
 */
public class HotelBenchmarkListItem extends ListItem {

    private HotelBenchmark benchmark;

    /**
     * Constructs an instance of <code>HotelBenchmarkListItem</code> given a benchmark.
     *
     * @param benchmark contains the benchmark.
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
    public boolean isEnabled() {
        return false;
    }

    @Override public View buildView(Context context, View convertView, ViewGroup parent) {
        View benchmarkView = null;
        LayoutInflater inflater = null;

        if (convertView == null) {
            inflater = LayoutInflater.from(context);
            benchmarkView = inflater.inflate(R.layout.hotel_benchmark_price_to_beat_row, null);
        } else {
            benchmarkView = convertView;
        }

        if (benchmark != null) {
            // price
            TextView priceTextView = (TextView) benchmarkView.findViewById(R.id.price);
            if (benchmark.price == null) {
                priceTextView.setText(R.string.price_to_beat_price_unavailable);
                priceTextView.setTextAppearance(context, R.style.ListCellUnavailableText);
            } else {
                Locale loc = context.getResources().getConfiguration().locale;
                priceTextView.setText(FormatUtil.formatAmount(benchmark.price, loc, benchmark.crnCode, true));
                priceTextView.setTextAppearance(context, R.style.ListCellPriceText);
            }

            // location
            TextView locTextView = (TextView) benchmarkView.findViewById(R.id.location);
            StringBuilder locTextBldr = new StringBuilder();
            if (benchmark.locationName != null) {
                locTextBldr.append(benchmark.locationName);
            }
            if (benchmark.subDivCode != null) {
                locTextBldr.append(", ");
                locTextBldr.append(benchmark.subDivCode);
            }
            locTextView.setText(locTextBldr);
        }

        return benchmarkView;
    }
}
