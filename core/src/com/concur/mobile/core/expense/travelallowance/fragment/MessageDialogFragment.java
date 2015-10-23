package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.Message;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

/**
 * Handles message dialog called by an activity. The dialog is parametrized via the arguments
 * bundle given to an object of this class.
 * Notifies the caller using callback interface {@link IFragmentCallback}, which should be
 * implemented by the caller. The fragment messages passed to the caller are given by the following
 * arguments:
 * {@link MessageDialogFragment#POSITIVE_BUTTON},
 * {@link MessageDialogFragment#NEUTRAL_BUTTON},
 * {@link MessageDialogFragment#NEGATIVE_BUTTON}.
 * Thereby this fragment passes a {@link Bundle} to the caller (extras), which was given
 * through the arguments bundle with key {@link BundleId#FRAGMENT_MESSAGE_EXTRAS} or null.
 *
 * Created by Michael Becherer on 29-Jul-15.
 */
public class MessageDialogFragment extends DialogFragment {

    private static final String CLASS_NAME = MessageDialogFragment.class.getName();
    private static final String CLASS_TAG = MessageDialogFragment.class.getSimpleName();

    private IFragmentCallback callback;

    /**
     * Used as key of an argument. Positive button is only shown, if this key was provided.
     * The associated value is supposed to be of type {@link String}. This value denotes
     * the fragment message sent to callback object when button was pressed by the user.
     */
    public static final String POSITIVE_BUTTON = CLASS_NAME + ".positive.button";

    /**
     * Used as key of an argument. Neutral button is only shown, if this key was provided.
     * The associated value is supposed to be of type {@link String}. This value denotes
     * the fragment message sent to callback object when button was pressed by the user.
     */
    public static final String NEUTRAL_BUTTON = CLASS_NAME + ".neutral.button";

    /**
     * Used as key of an argument. Negative button is only shown, if this key was provided.
     * The associated value is supposed to be of type {@link String}. This value denotes
     * the fragment message sent to callback object when button was pressed by the user.
     */
    public static final String NEGATIVE_BUTTON = CLASS_NAME + ".negative.button";

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link String}
     * and replaces the default text for the positive button.
     */
    public static final String POSITIVE_BUTTON_TEXT = CLASS_NAME + ".positive.button.text";

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link String}
     * and replaces the default text for the neutral button.
     */
    public static final String NEUTRAL_BUTTON_TEXT = CLASS_NAME + ".neutral.button.text";

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link String}
     * and replaces the default text for the negative button.
     */
    public static final String NEGATIVE_BUTTON_TEXT = CLASS_NAME + ".negative.button.text";

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link Message}.
     * The message text displayed is taken from there. Also the title is displayed in case
     * the message is of severity Error along with an error icon.
     */
    public static final String MESSAGE_OBJECT = CLASS_NAME + ".message.object";

    /**
     * Used as key of an argument. The dialog title is shown with the given text, if this
     * argument was provided and no {@link MessageDialogFragment#MESSAGE_OBJECT} was provided
     * through the arguments bundle.
     * The associated value is supposed to be of type {@link String} containing the title text.
     */
    public static final String MESSAGE_TITLE = CLASS_NAME + ".message.title";

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link String}.
     * The message text displayed is taken from there, if no argument
     * {@link MessageDialogFragment#MESSAGE_OBJECT} was provided through the arguments bundle.
     */
    public static final String MESSAGE_TEXT = CLASS_NAME + ".message.text";

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@code boolean}.
     * The dialog is not cancelable by touching an area outside the dialog window, if this argument
     * is provided with value {@code true}.
     */
    public static final String NOT_CANCELABLE = CLASS_NAME + ".not.cancelable";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle arguments = getArguments();
        //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.TADialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Default texts
        String positiveButtonText = getString(R.string.general_ok);
        String neutralButtonText = getString(R.string.cancel);
        String negativeButtonText = getString(R.string.general_no);

        if (arguments != null) {
            positiveButtonText = arguments.getString(POSITIVE_BUTTON_TEXT, positiveButtonText);
            neutralButtonText = arguments.getString(NEUTRAL_BUTTON_TEXT, neutralButtonText);
            negativeButtonText = arguments.getString(NEGATIVE_BUTTON_TEXT, negativeButtonText);
            //Get fragment message extras
            final Bundle extras = arguments.getBundle(BundleId.FRAGMENT_MESSAGE_EXTRAS);
            //Set buttons with button texts
            if (arguments.containsKey(POSITIVE_BUTTON)) {
                final String callBackMsg = arguments.getString(POSITIVE_BUTTON);
                builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null && !StringUtilities.isNullOrEmpty(callBackMsg)) {
                            callback.handleFragmentMessage(callBackMsg, extras);
                        }
                    }
                });
            }
            if (arguments.containsKey(NEUTRAL_BUTTON)) {
                final String callBackMsg = arguments.getString(NEUTRAL_BUTTON);
                builder.setNeutralButton(neutralButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null && !StringUtilities.isNullOrEmpty(callBackMsg)) {
                            callback.handleFragmentMessage(callBackMsg, extras);
                        }
                    }
                });
            }
            if (arguments.containsKey(NEGATIVE_BUTTON)) {
                final String callBackMsg = arguments.getString(NEGATIVE_BUTTON);
                builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null && !StringUtilities.isNullOrEmpty(callBackMsg)) {
                            callback.handleFragmentMessage(callBackMsg, extras);
                        }
                    }
                });
            }
            //Message text and title
            if (arguments.containsKey(MESSAGE_OBJECT)) {
                Message msg = (Message) arguments.getSerializable(MESSAGE_OBJECT);
                builder.setMessage(msg.getMessageText(getActivity().getApplicationContext()));
                if (msg.getSeverity() == Message.Severity.ERROR) {
                    builder.setIcon(R.drawable.icon_error_oval);
                    builder.setTitle(R.string.general_error);
                }
            } else {
                if (arguments.containsKey(MESSAGE_TEXT)) {
                    builder.setMessage(arguments.getString(MESSAGE_TEXT));
                }
                if (arguments.containsKey(MESSAGE_TITLE)) {
                    builder.setTitle(arguments.getString(MESSAGE_TITLE));
                }
            }
            //Cancelable or not
            if (arguments.containsKey(NOT_CANCELABLE)) {
                builder.setCancelable(false);
            } else {
                builder.setCancelable(true);
            }
        }
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (IFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IFragmentCallback") ;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
