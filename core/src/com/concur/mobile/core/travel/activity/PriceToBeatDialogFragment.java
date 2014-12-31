package com.concur.mobile.core.travel.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.air.activity.AirPriceToBeatSearch;
import com.concur.mobile.core.travel.hotel.activity.HotelPriceToBeatSearch;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;

/**
 * Fragment to display the Price to Beat menu
 * 
 * @author RatanK
 * 
 */
public class PriceToBeatDialogFragment extends DialogFragment {

    private static final String CLS_TAG = PriceToBeatDialogFragment.class.getSimpleName();

    public enum PriceToBeatAction {
        AIR_PRICE_TO_BEAT, HOTEL_PRICE_TO_BEAT
    }

    public PriceToBeatDialogFragment() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getText(R.string.home_action_title));

        PriceToBeatOptionListAdapter priceToBeatActionAdapter = new PriceToBeatOptionListAdapter(this);
        priceToBeatActionAdapter.options.add(PriceToBeatAction.AIR_PRICE_TO_BEAT);
        priceToBeatActionAdapter.options.add(PriceToBeatAction.HOTEL_PRICE_TO_BEAT);

        builder.setSingleChoiceItems(priceToBeatActionAdapter, -1, new PriceToBeatDialogListener(
                priceToBeatActionAdapter));

        // Flurry Notification.
        EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT,
                Flurry.EVENT_NAME_VIEWED_PRICE_TO_BEAT_MENU);

        return builder.create();

    }

    class PriceToBeatOptionListAdapter extends BaseAdapter {

        /**
         * Contains a list of available options.
         */
        ArrayList<PriceToBeatAction> options = new ArrayList<PriceToBeatAction>();
        PriceToBeatDialogFragment dialogFragment;

        public PriceToBeatOptionListAdapter(PriceToBeatDialogFragment dialogFragment) {
            super();
            this.dialogFragment = dialogFragment;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getCount()
         */
        public int getCount() {
            return options.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        public Object getItem(int position) {
            return options.get(position);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            view = inflater.inflate(R.layout.context_menu_icon_option, null);

            int textResId = 0;
            final PriceToBeatAction priceToBeatAction = options.get(position);
            switch (priceToBeatAction) {
            case AIR_PRICE_TO_BEAT:
                textResId = R.string.home_action_air_price_to_beat;
                break;
            case HOTEL_PRICE_TO_BEAT:
                textResId = R.string.home_action_hotel_price_to_beat;
                break;
            default:
                break;
            }

            // Set the text.
            if (textResId != 0) {
                TextView txtView = (TextView) view.findViewById(R.id.text);
                if (txtView != null) {
                    txtView.setPadding(10, 8, 0, 8);
                    txtView.setText(getActivity().getText(textResId));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: can't locate text view!");
                }
            }

            return view;

        }
    }

    class PriceToBeatDialogListener implements DialogInterface.OnClickListener {

        PriceToBeatOptionListAdapter adapter;

        public PriceToBeatDialogListener(PriceToBeatOptionListAdapter adapter) {
            super();
            this.adapter = adapter;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
         */
        public void onClick(DialogInterface dialog, int which) {
            Intent intent;
            PriceToBeatAction priceToBeatAction = (PriceToBeatAction) adapter.getItem(which);

            switch (priceToBeatAction) {
            case AIR_PRICE_TO_BEAT:
                intent = new Intent(getActivity(), AirPriceToBeatSearch.class);
                startActivity(intent);
                break;
            case HOTEL_PRICE_TO_BEAT:
                intent = new Intent(getActivity(), HotelPriceToBeatSearch.class);
                startActivity(intent);
                break;
            }

            dialog.dismiss();

        }
    }
}
