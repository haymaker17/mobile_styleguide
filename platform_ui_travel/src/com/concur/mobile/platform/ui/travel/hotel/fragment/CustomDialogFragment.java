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
 */
public class CustomDialogFragment extends DialogFragment {

    // Title resource text/resource ID argument keys.
  //  protected static final String TITLE_TEXT = "title.text";
    protected static final String TITLE_RESOURCE_ID = "title.id";

    // Message text/resource ID argument keys.
    protected static final String MESSAGE_RESOURCE_ID = "msg.id";
 //   protected static final String MESSAGE_TEXT = "msg.text";

    // Positive button text/resource ID argument keys.
   // protected static final String POSITIVE_BUTTON_TEXT = "positive.button.text";
    protected static final String POSITIVE_BUTTON_RESOURCE_ID = "positive.button.resource.id";

    // Negative button text/resource ID argument keys.
    //protected static final String NEGATIVE_BUTTON_TEXT = "negative.button.text";
   protected static final String NEGATIVE_BUTTON_RESOURCE_ID = "negative.button.resource.id";

    public int titleResourceId;
    public int msgResourceId;
    public int okButtonId;
    public int cancelButtonId;
    private CustomDialogFragmentCallbackListener callBackListener;
    public AlertDialog dialog;
    public  Bundle args;

    public CustomDialogFragment() {
        setRetainInstance(true);
    }



    /**
     * Sets the text resource ID of the dialog title.
     *
     * @param title
     *            contains the resource ID of the dialog title.
     */
    public void setTitle(int title) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putInt(TITLE_RESOURCE_ID, title);
        setArguments(args);
    }



    /**
     * Sets the text resource ID of the dialog message.
     *
     * @param message
     *            contains the resource ID of the dialog message.
     */
    public void setMessage(int message) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putInt(MESSAGE_RESOURCE_ID, message);
        setArguments(args);
    }


    /**
     * Sets the resource ID of the positive button text.
     *
     * @param text
     *            contains the resource ID of the positive button text.
     */
    public void setPositiveButtonText(int text) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putInt(POSITIVE_BUTTON_RESOURCE_ID, text);
        setArguments(args);
    }

    /**
     * Sets the resource ID of the negative button text.
     *
     * @param text
     *            contains the resource ID of the negative button text.
     */
    public void setNegativeButtonText(int text) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putInt(NEGATIVE_BUTTON_RESOURCE_ID, text);
        setArguments(args);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(getActivity());
        // Construct or reference the current arguments.
         args = getArguments();
        if (args == null) {
            args = new Bundle();
        }

        dialog = dlgBldr.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface ad) {

                dialog.setContentView(R.layout.custom_dialog);
                //   dialog.setTitle(titleResourceId);
                TextView title = (TextView) dialog.findViewById(R.id.title);
              //  title.setText(titleResourceId);
                if (args.containsKey(TITLE_RESOURCE_ID)) {
                    title.setText(args.getInt(TITLE_RESOURCE_ID));
                }
                TextView msg = (TextView) dialog.findViewById(R.id.msg);
                //msg.setText(msgResourceId);
                if (args.containsKey(MESSAGE_RESOURCE_ID)) {
                    msg.setText(args.getInt(MESSAGE_RESOURCE_ID));
                }

                if (args.containsKey(POSITIVE_BUTTON_RESOURCE_ID)) {

                    Button b = (Button) dialog.findViewById(R.id.dialogButtonOk);
                    b.setText(args.getInt(POSITIVE_BUTTON_RESOURCE_ID));
                    b.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            if (callBackListener != null) {
                                callBackListener.onCustomAction();
                            }

                        }
                    });
                }
                Button c = (Button) dialog.findViewById(R.id.dialogButtonCancel);
                if (args.containsKey(NEGATIVE_BUTTON_RESOURCE_ID)) {
                    c.setText(args.getInt(NEGATIVE_BUTTON_RESOURCE_ID));
                    c.setVisibility(View.VISIBLE);

                    c.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });

                } else {
                    c.setVisibility(View.GONE);
                }
            };


        });
         return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {

        // NOTE: This code below will permit the dialog to be reshown after a device orientation change
        // if 'onRetainInstance' is set to 'true'.
        if (dialog != null && getRetainInstance()) {
            // getDialog().setOnDismissListener(null);
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (CustomDialogFragmentCallbackListener) activity;
        } catch (ClassCastException e) {
            // throw new ClassCastException(activity.toString() + " must implement CustomDialogFragmentCallbackListener");
        }

    }

//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        super.onSaveInstanceState(savedInstanceState);
//        savedInstanceState.putInt("titleResourceId", titleResourceId);
//        savedInstanceState.putInt("msgResourceId", msgResourceId);
//        savedInstanceState.putInt("okButtonId", okButtonId);
//        savedInstanceState.putInt("cancelButtonId", cancelButtonId);
//
//    }
//
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        titleResourceId = savedInstanceState.getInt("titleResourceId");
//        msgResourceId = savedInstanceState.getInt("msgResourceId");
//        okButtonId = savedInstanceState.getInt("okButtonId");
//        cancelButtonId = savedInstanceState.getInt("cancelButtonId");
//    }

    // call back interface to be implemented by the activities
    public interface CustomDialogFragmentCallbackListener {

        public void onCustomAction();
    }
}
