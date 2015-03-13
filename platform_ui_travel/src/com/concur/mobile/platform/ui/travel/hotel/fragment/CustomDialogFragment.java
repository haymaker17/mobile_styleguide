package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.concur.mobile.platform.ui.travel.R;

/**
 * Custom layout Dialog fragment
 * 
 * @author tejoa
 * 
 */
public class CustomDialogFragment extends DialogFragment {

    public int titleResourceId;
    public int msgResourceId;
    public int okButtonId;
    public int cancelButtonId;
    private CustomDialogFragmentCallbackListener callBackListener;

    public CustomDialogFragment() {
    }

    /**
     * 
     * @param titleResourceId
     * @param msgResourceId
     */
    public CustomDialogFragment(int titleResourceId, int msgResourceId, int okButtonId, int cancelButtonId) {
        this.titleResourceId = titleResourceId;
        this.msgResourceId = msgResourceId;
        this.okButtonId = okButtonId;
        this.cancelButtonId = cancelButtonId;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(getActivity());

        final AlertDialog dialog = dlgBldr.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface ad) {

                dialog.setContentView(R.layout.custom_dialog);
                dialog.setTitle(titleResourceId);
                TextView msg = (TextView) dialog.findViewById(R.id.msg);
                msg.setText(msgResourceId);
                if (okButtonId > 0) {
                    Button b = (Button) dialog.findViewById(R.id.dialogButtonOk);
                    b.setText(okButtonId);
                    b.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            callBackListener.onCustomAction();

                        }
                    });
                }
                if (cancelButtonId > 0) {
                    Button c = (Button) dialog.findViewById(R.id.dialogButtonCancel);
                    c.setText(cancelButtonId);

                    c.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });

                }
            };

        });

        // set the title
        dlgBldr.setTitle(titleResourceId);
        dlgBldr.setMessage(msgResourceId);

        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (CustomDialogFragmentCallbackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CustomDialogFragmentCallbackListener");
        }

    }

    // call back interface to be implemented by the activities
    public interface CustomDialogFragmentCallbackListener {

        public void onCustomAction();
    }
}
