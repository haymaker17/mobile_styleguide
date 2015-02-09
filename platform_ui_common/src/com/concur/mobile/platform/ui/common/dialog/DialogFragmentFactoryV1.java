package com.concur.mobile.platform.ui.common.dialog;

import com.concur.mobile.platform.ui.common.R;
import com.concur.mobile.platform.ui.common.dialog.AlertDialogFragmentV1.OnClickListener;

/**
 * Provides a factory for producing various kinds of dialog fragments, i.e., alert dialogs, progress dialogs, etc. This class is a
 * copy of DialogFragmentFactory but has no dependency on support.v4 library
 */
public class DialogFragmentFactoryV1 {

    /**
     * Constructs a simple instance of <code>AlertDialogFragmentV1</code> for displaying a title and single okay button that will
     * dismiss upon okay button press.
     * 
     * @param title
     *            contains the dialog title.
     * @return returns an instance of <code>AlertDialogFragmentV1</code> with a title, message and single okay button.
     */
    public static AlertDialogFragmentV1 getAlertOkayInstance(String title) {
        if (title == null) {
            throw new IllegalArgumentException("title must not be null");
        }
        return getAlertOkayInstance(title, null);
    }

    /**
     * Constructs a simple instance of <code>AlertDialogFragmentV1</code> for displaying a title and single okay button that will
     * dismiss upon okay button press.
     * 
     * @param title
     *            contains the resource ID of the dialog title text.
     * @return returns an instance of <code>AlertDialogFragmentV1</code> with a title and single okay button.
     */
    public static AlertDialogFragmentV1 getAlertOkayInstance(int title) {
        return getAlertOkayInstance(title, null);
    }

    /**
     * Constructs a simple instance of <code>AlertDialogFragmentV1</code> for displaying a title, message and single okay button
     * that will dismiss upon okay button press.
     * 
     * @param title
     *            contains the dialog title.
     * @param message
     *            contains the dialog message.
     * @return returns an instance of <code>AlertDialogFragmentV1</code> with a title, message and single okay button.
     */
    public static AlertDialogFragmentV1 getAlertOkayInstance(String title, String message) {
        if (title == null) {
            throw new IllegalArgumentException("title must not be null");
        }
        AlertDialogFragmentV1 frag = new AlertDialogFragmentV1();
        frag.setTitle(title);
        frag.setMessage(message);
        frag.setPositiveButtonText(R.string.okay);
        return frag;
    }

    /**
     * Constructs a simple instance of <code>AlertDialogFragmentV1</code> for displaying a title, message and single okay button
     * that will dismiss upon okay button press and call the given <code>OnClickListener</code>.
     * 
     * @param title
     *            contains the dialog title.
     * @param message
     *            contains the dialog message.
     * @param buttonText
     *            the text to display in the single button.
     * @param okayListener
     *            <code>OnClickListener</code> to call when the OK button is pressed.
     * @return returns an instance of <code>AlertDialogFragmentV1</code> with a title, message and single okay button.
     */
    public static AlertDialogFragmentV1 getPositiveDialogFragment(String title, String message, String buttonText,
            OnClickListener okayListener) {
        if (title == null) {
            throw new IllegalArgumentException("title must not be null");
        }
        AlertDialogFragmentV1 frag = new AlertDialogFragmentV1();
        frag.setTitle(title);
        frag.setMessage(message);
        frag.setPositiveButtonText(buttonText);
        frag.setPositiveButtonListener(okayListener);
        return frag;
    }

    /**
     * Constructs a simple instance of <code>AlertDialogFragmentV1</code> for displaying a title, message and single okay button
     * that will dismiss upon okay button press.
     * 
     * @param title
     *            contains the dialog title.
     * @param message
     *            contains the resource ID of the dialog message text.
     * @return returns an instance of <code>AlertDialogFragmentV1</code> with a title, message and single okay button.
     */
    public static AlertDialogFragmentV1 getAlertOkayInstance(String title, int message) {
        if (title == null) {
            throw new IllegalArgumentException("title must not be null");
        }
        AlertDialogFragmentV1 frag = new AlertDialogFragmentV1();
        frag.setTitle(title);
        frag.setMessage(message);
        frag.setPositiveButtonText(R.string.okay);
        return frag;
    }

    /**
     * Constructs a simple instance of <code>AlertDialogFragmentV1</code> for displaying a title, message and single okay button
     * that will dismiss upon okay button press.
     * 
     * @param title
     *            contains the resource ID of the dialog title text.
     * @param message
     *            contains the dialog message.
     * @return returns an instance of <code>AlertDialogFragmentV1</code> with a title, message and single okay button.
     */
    public static AlertDialogFragmentV1 getAlertOkayInstance(int title, String message) {
        AlertDialogFragmentV1 frag = new AlertDialogFragmentV1();
        frag.setTitle(title);
        frag.setMessage(message);
        frag.setPositiveButtonText(R.string.okay);
        return frag;
    }

    /**
     * Constructs a simple instance of <code>AlertDialogFragmentV1</code> for displaying a title, message and single okay button
     * that will dismiss upon okay button press.
     * 
     * @param title
     *            contains the resource ID of the dialog title text.
     * @param message
     *            contains the resource ID of the dialog message text.
     * @return returns an instance of <code>AlertDialogFragmentV1</code> with a title, message and single okay button.
     */
    public static AlertDialogFragmentV1 getAlertOkayInstance(int title, int message) {
        AlertDialogFragmentV1 frag = new AlertDialogFragmentV1();
        frag.setTitle(title);
        frag.setMessage(message);
        frag.setPositiveButtonText(R.string.okay);
        return frag;
    }

    /**
     * Constructs a simple instance of <code>AlertDialogFragmentV1</code> for display a title, message, okay and cancel button.
     * 
     * @param title
     *            contains the dialog title.
     * @param message
     *            contains the dialog message.
     * @param okayListener
     *            contains the dialog okay button listener.
     * @param cancelListener
     *            contains the dialog cancel button listener.
     * @param cancelDialogListener
     *            contains the dialog cancel listener.
     * @return returns an instance of <code>AlertDialogFragmentV1</code> for display of a title, message, okay and cancel buttons.
     */
    public static AlertDialogFragmentV1 getAlertOkayCancelInstance(String title, String message,
            AlertDialogFragmentV1.OnClickListener okayListener, AlertDialogFragmentV1.OnClickListener cancelListener,
            AlertDialogFragmentV1.OnClickListener cancelDialogListener) {

        if (title == null) {
            throw new IllegalArgumentException("title must not be null");
        }

        AlertDialogFragmentV1 frag = new AlertDialogFragmentV1();
        frag.setTitle(title);
        frag.setMessage(message);
        frag.setPositiveButtonText(R.string.okay);
        frag.setPositiveButtonListener(okayListener);
        frag.setNegativeButtonText(R.string.cancel);
        frag.setNegativeButtonListener(cancelListener);
        frag.setCancelListener(cancelDialogListener);
        return frag;
    }

    /**
     * Constructs a simple instance of <code>AlertDialogFragmentV1</code> for display a title, okay and cancel button.
     * 
     * @param title
     *            contains the dialog title.
     * @param okayListener
     *            contains the dialog okay button listener.
     * @param cancelListener
     *            contains the dialog cancel button listener.
     * @param cancelDialogListener
     *            contains the dialog cancel listener.
     * @return returns an instance of <code>AlertDialogFragmentV1</code> for display of a title, message, okay and cancel buttons.
     */
    public static AlertDialogFragmentV1 getAlertOkayCancelInstance(String title,
            AlertDialogFragmentV1.OnClickListener okayListener, AlertDialogFragmentV1.OnClickListener cancelListener,
            AlertDialogFragmentV1.OnClickListener cancelDialogListener) {

        if (title == null) {
            throw new IllegalArgumentException("title must not be null");
        }

        AlertDialogFragmentV1 frag = new AlertDialogFragmentV1();
        frag.setTitle(title);
        frag.setPositiveButtonText(R.string.okay);
        frag.setPositiveButtonListener(okayListener);
        frag.setNegativeButtonText(R.string.cancel);
        frag.setNegativeButtonListener(cancelListener);
        frag.setCancelListener(cancelDialogListener);
        return frag;
    }

    /**
     * Constructs an <code>AlertDialogFragmentV1</code> containing a title, message, positive, neutral and negative buttons.
     * 
     * @param title
     *            contains the dialog title.
     * @param message
     *            contains the dialog message.
     * @param positiveButtonText
     *            contains the resource ID of the positive button text. A value of <code>-1</code> implies no positive button.
     * @param neutralButtonText
     *            contains the resource ID of the neutral button text. A value of <code>-1</code> implies no neutral button.
     * @param negativeButtonText
     *            contains the resource ID of the negative button text. A value of <code>-1</code> implies no negative button.
     * @param positiveListener
     *            contains the positive button dialog callback.
     * @param neutralListener
     *            contains the neutral button dialog callback.
     * @param negativeListener
     *            contains the negative button dialog callback.
     * @param cancelDialogListener
     *            contains the dialog cancel listener.
     * @return returns an instance of <code>AlertDialogFragmentV1</code> containing a title, message, positive, neutral and
     *         negative buttons.
     */
    public static AlertDialogFragmentV1 getAlertDialog(String title, String message, int positiveButtonText,
            int neutralButtonText, int negativeButtonText, AlertDialogFragmentV1.OnClickListener positiveListener,
            AlertDialogFragmentV1.OnClickListener neutralListener,
            AlertDialogFragmentV1.OnClickListener negativeListener,
            AlertDialogFragmentV1.OnClickListener cancelDialogListener) {

        if (title == null) {
            throw new IllegalArgumentException("title must not be null");
        }

        AlertDialogFragmentV1 frag = new AlertDialogFragmentV1();
        frag.setTitle(title);
        frag.setMessage(message);
        if (positiveButtonText != -1) {
            frag.setPositiveButtonText(positiveButtonText);
        }
        if (neutralButtonText != -1) {
            frag.setNeutralButtonText(neutralButtonText);
        }
        if (negativeButtonText != -1) {
            frag.setNegativeButtonText(negativeButtonText);
        }
        frag.setPositiveButtonListener(positiveListener);
        frag.setNeutralButtonListener(neutralListener);
        frag.setNegativeButtonListener(negativeListener);
        frag.setCancelListener(cancelDialogListener);

        return frag;
    }

    /**
     * Constructs a progress dialog fragment with a message and whether the dialog is cancelable and indeterminate.
     * 
     * @param message
     *            contains the dialog message.
     * @param cancelable
     *            contains whether the dialog is cancelable.
     * @param indeterminate
     *            contains whether the dialog is indeterminate.
     * @param cancelListener
     *            contains a reference to the cancel listener.
     * @return returns an instance of <code>ProgressDialogFragmentV1</code> configured with a message, whether the dialog is
     *         cancelable and indeterminate.
     */
    public static ProgressDialogFragmentV1 getProgressDialog(String message, boolean cancelable, boolean indeterminate,
            ProgressDialogFragmentV1.OnCancelListener cancelListener) {

        ProgressDialogFragmentV1 frag = new ProgressDialogFragmentV1();

        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }

        frag.setMessage(message);
        frag.setCancelable(cancelable);
        frag.setCancelListener(cancelListener);
        frag.setIndeterminate(indeterminate);
        return frag;
    }
}
