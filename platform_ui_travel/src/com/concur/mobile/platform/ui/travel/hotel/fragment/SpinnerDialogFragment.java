package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.ui.travel.R;

/**
 * Dialog fragment to show a text and an image at right
 * 
 * @author RatanK
 * 
 */
public class SpinnerDialogFragment extends DialogFragment {

    private static final String CLS_TAG = SpinnerDialogFragment.class.getSimpleName();

    public SpinnerItem[] spinnerItems;
    public String curSpinnerItemId;
    public int titleResourceId = R.string.general_select_one_of_the_below;
    public int imageResourceId = R.drawable.btn_check_on;
    private SpinnerDialogFragmentCallbackListener callBackListener;
    private int selectedSpinnerItemPosition = -1;

    public SpinnerDialogFragment() {
    }

    /**
     * Create an instance of SpinnerDialogFragment with the given details.
     * 
     * @param titleResourceId
     *            - if <=0, defaults to R.string.general_select_one_of_the_below
     * @param imageResourceId
     *            - if <=0, defaults to R.drawable.btn_check_on
     * @param spinnerItems
     */
    public SpinnerDialogFragment(int titleResourceId, int imageResourceId, SpinnerItem[] spinnerItems) {
        if (titleResourceId <= 0) {
            this.titleResourceId = R.string.general_select_one_of_the_below;
        } else {
            this.titleResourceId = titleResourceId;
        }
        this.spinnerItems = spinnerItems;
        if (imageResourceId <= 0) {
            this.imageResourceId = R.drawable.btn_check_on;
        } else {
            this.imageResourceId = imageResourceId;
        }
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
                    android.R.layout.simple_spinner_item, spinnerItems) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    View view = inflater.inflate(R.layout.text_with_image_to_right, null);
                    ((TextView) view.findViewById(R.id.hotel_violaiton_reason)).setText(spinnerItems[position].name);
                    if (curSpinnerItemId != null && curSpinnerItemId == spinnerItems[position].id) {
                        ((ImageView) view.findViewById(R.id.hotel_violaiton_reason_selected_icon))
                                .setImageResource(imageResourceId);
                        selectedSpinnerItemPosition = position;
                    }

                    return view;
                }
            };
            listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            dlgBldr.setSingleChoiceItems(listAdapter, selectedSpinnerItemPosition,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            callBackListener.onSpinnerItemSelected(spinnerItems[which], thisFragmentTagName);
                            dialog.dismiss();
                        }
                    });
        }

        return dlgBldr.create();
    }

    @Override
    public void onAttach(Activity activity) {
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
         * 
         * @param selectedSpinnerItem
         *            - selected item
         * @param fragmentTagName
         *            - fragment tag name if available
         */
        public void onSpinnerItemSelected(SpinnerItem selectedSpinnerItem, String fragmentTagName);
    }

}
