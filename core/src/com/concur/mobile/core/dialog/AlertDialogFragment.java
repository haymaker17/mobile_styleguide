package com.concur.mobile.core.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

/**
 * A standard extension of {@link DialogFragment} to display alert dialogs with up to 3 buttons.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.dialog.AlertDialogFragment} instead.
 */
public class AlertDialogFragment extends DialogFragment {

    // Title resource text/resource ID argument keys.
    protected static final String TITLE_TEXT = "title.text";
    protected static final String TITLE_RESOURCE_ID = "title.id";

    // Message text/resource ID argument keys.
    protected static final String MESSAGE_RESOURCE_ID = "msg.id";
    protected static final String MESSAGE_TEXT = "msg.text";

    // Positive button text/resource ID argument keys.
    protected static final String POSITIVE_BUTTON_TEXT = "positive.button.text";
    protected static final String POSITIVE_BUTTON_RESOURCE_ID = "positive.button.resource.id";

    // Neutral button text/resource ID argument keys.
    protected static final String NEUTRAL_BUTTON_TEXT = "neutral.button.text";
    protected static final String NEUTRAL_BUTTON_RESOURCE_ID = "neutral.button.resource.id";

    // Negative button text/resource ID argument keys.
    protected static final String NEGATIVE_BUTTON_TEXT = "negative.button.text";
    protected static final String NEGATIVE_BUTTON_RESOURCE_ID = "negative.button.resource.id";

    public interface OnClickListener extends ProgressDialogFragment.OnCancelListener {

        /**
         * Provides a notification of a dialog button being clicked.
         * 
         * @param activity
         *            contains a reference to the current <code>FragmentActivity</code> associated with the dialog fragment.
         *            <b>NOTE:</b> this value may change due to device orientation. Clients should use this instance to invoke
         *            methods in response to a dialog button being pressed.
         * @param dialog
         *            contains a reference to the <code>DialogInterface</code> object.
         * @param which
         *            contains which button was pressed, see <code>DialogInterface.BUTTON_POSITIVE</code>,
         *            <code>DialogInterface.BUTTON_NEUTRAL</code> or <code>DialogInterface.BUTTON_NEGATIVE</code>.
         */
        public void onClick(FragmentActivity activity, DialogInterface dialog, int which);
    }

    // Contains the positive button listener.
    protected DialogInterface.OnClickListener mPositiveListener;
    // Contains the neutral button listener.
    protected DialogInterface.OnClickListener mNeutralListener;
    // Contains the negative button listener.
    protected DialogInterface.OnClickListener mNegativeListener;
    // Contains the cancel listener.
    protected AlertDialogFragment.OnClickListener mCancelListener;

    public AlertDialogFragment() {
        setRetainInstance(true);
    }

    /**
     * Sets the text of the dialog title.
     * 
     * @param title
     *            contains the text of the dialog title.
     */
    public void setTitle(String title) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putString(TITLE_TEXT, title);
        setArguments(args);
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
     * Sets the text of the dialog message.
     * 
     * @param message
     *            contains the text of the dialog message.
     */
    public void setMessage(String message) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putString(MESSAGE_TEXT, message);
        setArguments(args);
    }

    /**
     * Sets the text resource ID of the dialog message.
     * 
     * @param title
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
     * Sets the positive button text.
     * 
     * @param text
     *            contains the positive button text.
     */
    public void setPositiveButtonText(String text) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putString(POSITIVE_BUTTON_TEXT, text);
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
     * Sets the positive button listener for the dialog.
     * 
     * @param listener
     *            contains a reference to an instance of <code>AlertDialogFragment.OnClickListener</code> to handle the positive
     *            button click.
     */
    public void setPositiveButtonListener(final AlertDialogFragment.OnClickListener listener) {
        this.mPositiveListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onClick(getActivity(), dialog, which);
                }
            }
        };
    }

    /**
     * Sets the neutral button text.
     * 
     * @param text
     *            contains the neutral button text.
     */
    public void setNeutralButtonText(String text) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putString(NEUTRAL_BUTTON_TEXT, text);
        setArguments(args);
    }

    /**
     * Sets the resource ID of the neutral button text.
     * 
     * @param text
     *            contains the resource ID of the neutral button text.
     */
    public void setNeutralButtonText(int text) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putInt(NEUTRAL_BUTTON_RESOURCE_ID, text);
        setArguments(args);
    }

    /**
     * Sets the neutral button listener for the dialog.
     * 
     * @param listener
     *            contains a reference to an instance of <code>AlertDialogFragment.OnClickListener</code> to handle the neutral
     *            button click.
     */
    public void setNeutralButtonListener(final AlertDialogFragment.OnClickListener listener) {
        this.mNeutralListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onClick(getActivity(), dialog, which);
                }
            }
        };
    }

    /**
     * Sets the negative button text.
     * 
     * @param text
     *            contains the negative button text.
     */
    public void setNegativeButtonText(String text) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putString(NEGATIVE_BUTTON_TEXT, text);
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

    /**
     * Sets the negative button listener for the dialog.
     * 
     * @param listener
     *            contains a reference to an instance of <code>AlertDialogFragment.OnClickListener</code> to handle the negative
     *            button click.
     */
    public void setNegativeButtonListener(final AlertDialogFragment.OnClickListener listener) {
        this.mNegativeListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onClick(getActivity(), dialog, which);
                }
            }
        };
    }

    /**
     * Sets the cancel listener for the dialog.
     * 
     * @param listener
     *            contains a reference to an instance of <code>AlertDialogFragment.OnClickListener</code> to handle the dialog
     *            cancel event.
     */
    public void setCancelListener(AlertDialogFragment.OnClickListener listener) {
        mCancelListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(getActivity());
        // Construct or reference the current arguments.
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        // Configure title, message and buttons.
        configureTitle(dlgBldr, args);
        configureMessage(dlgBldr, args);
        configureButtons(dlgBldr, args);

        return dlgBldr.create();
    }

    /**
     * Will configure a title within the dialog builder.
     * 
     * @param dlgBldr
     *            contains an instance of <code>AlertDialog.Builder</code> used to configure the title.
     * 
     * @param args
     *            contains an instance of <code>Bundle</code> containing the dialog arguments.
     */
    protected void configureTitle(AlertDialog.Builder dlgBldr, Bundle args) {
        // Set the title using the resource ID or the text
        if (args.containsKey(TITLE_RESOURCE_ID)) {
            dlgBldr.setTitle(args.getInt(TITLE_RESOURCE_ID));
        } else if (args.containsKey(TITLE_TEXT)) {
            dlgBldr.setTitle(args.getString(TITLE_TEXT));
        }
    }

    /**
     * Will configure a message within the dialog builder.
     * 
     * @param dlgBldr
     *            contains an instance of <code>AlertDialog.Builder</code> used to configure the message.
     * 
     * @param args
     *            contains an instance of <code>Bundle</code> containing the dialog arguments.
     */
    protected void configureMessage(AlertDialog.Builder dlgBldr, Bundle args) {
        // Set the message using the resource ID or the text.
        if (args.containsKey(MESSAGE_RESOURCE_ID)) {
            dlgBldr.setMessage(args.getInt(MESSAGE_RESOURCE_ID));
        } else if (args.containsKey(MESSAGE_TEXT)) {
            dlgBldr.setMessage(args.getString(MESSAGE_TEXT));
        }
    }

    /**
     * Will configure any dialog buttons within the dialog builder.
     * 
     * @param dlgBldr
     *            contains an instance of <code>AlertDialog.Builder</code> used to configure buttons.
     * @param args
     *            contains an instance of <code>Bundle</code> containing the dialog arguments.
     */
    protected void configureButtons(AlertDialog.Builder dlgBldr, Bundle args) {

        CharSequence buttonText = null;
        if (args.containsKey(POSITIVE_BUTTON_RESOURCE_ID)) {
            buttonText = getActivity().getText(args.getInt(POSITIVE_BUTTON_RESOURCE_ID));
        } else if (args.containsKey(POSITIVE_BUTTON_TEXT)) {
            buttonText = args.getString(POSITIVE_BUTTON_TEXT);
        }
        if (buttonText != null) {
            dlgBldr.setPositiveButton(buttonText, mPositiveListener);
        }

        buttonText = null;
        if (args.containsKey(NEUTRAL_BUTTON_RESOURCE_ID)) {
            buttonText = getActivity().getText(args.getInt(NEUTRAL_BUTTON_RESOURCE_ID));
        } else if (args.containsKey(NEUTRAL_BUTTON_TEXT)) {
            buttonText = args.getString(NEUTRAL_BUTTON_TEXT);
        }
        if (buttonText != null) {
            dlgBldr.setNeutralButton(buttonText, mNeutralListener);
        }

        buttonText = null;
        if (args.containsKey(NEGATIVE_BUTTON_RESOURCE_ID)) {
            buttonText = getActivity().getText(args.getInt(NEGATIVE_BUTTON_RESOURCE_ID));
        } else if (args.containsKey(NEGATIVE_BUTTON_TEXT)) {
            buttonText = args.getString(NEGATIVE_BUTTON_TEXT);
        }
        if (buttonText != null) {
            dlgBldr.setNegativeButton(buttonText, mNegativeListener);
        }
    }

    @Override
    public void onDestroyView() {
        // NOTE: This code below will permit the dialog to be reshown after a device orientation change
        // if 'onRetainInstance' is set to 'true'.
        if (getDialog() != null && getRetainInstance()) {
            // getDialog().setOnDismissListener(null);
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        if (mCancelListener != null) {
            mCancelListener.onCancel(getActivity(), dialog);
        }
    }

}
