package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.ui.travel.R;

/**
 * Dialog fragment to show a text and an image at right
 *
 * @author RatanK
 */
public class SpinnerDialogFragment extends DialogFragment {

    private static final String CLS_TAG = SpinnerDialogFragment.class.getSimpleName();

    public SpinnerItem[] spinnerItems;
    public String curSpinnerItemId;
    public int titleResourceId = R.string.general_select_one_of_the_below;
    private SpinnerDialogFragmentCallbackListener callBackListener;
    private int selectedSpinnerItemPosition = -1;

    public SpinnerDialogFragment() {
    }

    /**
     * Create an instance of SpinnerDialogFragment with the given details.
     *
     * @param titleResourceId - if <=0, defaults to R.string.general_select_one_of_the_below
     * @param spinnerItems
     */
    public SpinnerDialogFragment(int titleResourceId, SpinnerItem[] spinnerItems) {
        if (titleResourceId <= 0) {
            this.titleResourceId = R.string.general_select_one_of_the_below;
        } else {
            this.titleResourceId = titleResourceId;
        }
        this.spinnerItems = spinnerItems;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String thisFragmentTagName = getTag();

        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(getActivity());

        // set the title
        dlgBldr.setTitle(titleResourceId);

        // set the spinner items
        if (spinnerItems != null) {
            ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(getActivity(),
                    android.R.layout.simple_list_item_single_choice, spinnerItems) {

                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    View view = inflater.inflate(R.layout.text_with_image_to_right, null);
                    RadioButton radioButton = (RadioButton) view.findViewById(R.id.hotel_violations_radio_button);
                    radioButton.setText(spinnerItems[position].name);
                    if (curSpinnerItemId != null && curSpinnerItemId == spinnerItems[position].id) {
                        selectedSpinnerItemPosition = position;
                        radioButton.setChecked(true);
                    } else {
                        radioButton.setChecked(false);
                    }
                    radioButton.setOnClickListener(new View.OnClickListener() {

                        @Override public void onClick(View v) {
                            callBackListener.onSpinnerItemSelected(spinnerItems[position], thisFragmentTagName);
                            getDialog().dismiss();
                        }
                    });
                    return view;
                }
            };
            listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dlgBldr.setAdapter(listAdapter, null);
        }
        return dlgBldr.create();
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (SpinnerDialogFragmentCallbackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SpinnerDialogFragmentCallbackListener");
        }

    }

    // call back interface to be implemented by the activities
    public interface SpinnerDialogFragmentCallbackListener {

        /**
         * @param selectedSpinnerItem - selected item
         * @param fragmentTagName     - fragment tag name if available
         */
        public void onSpinnerItemSelected(SpinnerItem selectedSpinnerItem, String fragmentTagName);
    }

}
