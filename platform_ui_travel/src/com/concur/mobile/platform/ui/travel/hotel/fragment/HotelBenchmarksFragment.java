package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;
import com.concur.mobile.platform.ui.travel.R;

/**
 * Fragment to display the Hotel Price to Beat ranges
 * <p/>
 * Created by RatanK
 */
public class HotelBenchmarksFragment extends DialogFragment {

    public ListItemAdapter<HotelBenchmarkListItem> listItemAdapter;
    public String priceToBeatRangeText;

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View mainView = getActivity().getLayoutInflater().inflate(R.layout.hotel_price_to_beat_details_fragment, null);

        // set the price to beat range
        ((TextView) mainView.findViewById(R.id.priceToBeatText))
                .setText(getString(R.string.price_to_beat_label).toUpperCase() + " : " + priceToBeatRangeText);

        // set the list item adapter
        ListView benchmarksList = (ListView) mainView.findViewById(R.id.price_to_beat_list_view);
        benchmarksList.setAdapter(listItemAdapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mainView);

        return builder.create();
    }

}
