package com.concur.mobile.core.dialog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ImageView;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.RolesUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.ui.common.util.ImageUtil;

/***
 * Dialog fragment to show a preview of the selected receipt
 */
public class ReceiptPreviewDialogFragment extends DialogFragment {

    public static final String DIALOG_FRAGMENT_ID = "ReceiptPreviewDialog";
    private static final String BUNDLE_KEY_RECEIPT_PATH = "key.receipt.path";
    private String mReceiptPath = null;
    private ReceiptPreviewDialogFragmentListener mReceiptPreviewDialogFragmentListener = null;

    private static final int REQUEST_TAKE_PICTURE = 100;

    public interface ReceiptPreviewDialogFragmentListener {

        public void onPreviewComplete(String ReceiptPath);

        public void onCameraFailure();
    }

    public ReceiptPreviewDialogFragment() {

    }

    public ReceiptPreviewDialogFragment(String RecieptPath) {
        super();

        mReceiptPath = RecieptPath;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putString(BUNDLE_KEY_RECEIPT_PATH, mReceiptPath);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = null;
        ImageView imgv = null;
        AlertDialog.Builder builder = null;

        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        builder = new AlertDialog.Builder(getActivity());

        if (savedInstanceState != null) {
            mReceiptPath = savedInstanceState.getString(BUNDLE_KEY_RECEIPT_PATH);
        }

        bitmap = BitmapFactory.decodeFile(mReceiptPath, options);
        imgv = new ImageView(getActivity());
        imgv.setImageBitmap(bitmap);
        builder.setView(imgv);
        builder.setPositiveButton("Use This Receipt", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mReceiptPreviewDialogFragmentListener != null) {
                    mReceiptPreviewDialogFragmentListener.onPreviewComplete(mReceiptPath);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNeutralButton("Capture New Image", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                captureReceipt();
            }
        });

        return builder.create();
    }

    /**
     * Captures a receipt image to be imported into the receipt store.
     * 
     * @return receiptCameraImageDataLocalFilePath
     */
    protected String captureReceipt() {
        String receiptCameraImageDataLocalFilePath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Create a place for the camera to write its output.
            String receiptFilePath = ViewUtil.createExternalMediaImageFilePath();
            File receiptFile = new File(receiptFilePath);
            Uri outputFileUri = Uri.fromFile(receiptFile);
            receiptCameraImageDataLocalFilePath = receiptFile.getAbsolutePath();
            Log.d(Const.LOG_TAG, ".captureReceipt: receipt image path -> '" + receiptCameraImageDataLocalFilePath
                    + "'.");
            // Launch the camera application.
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            try {
                startActivityForResult(intent, REQUEST_TAKE_PICTURE);
            } catch (Exception e) {
                // Device has no camera, see MOB-16872
                Log.d(Const.LOG_TAG, e.getMessage());
            }

        } else {
            receiptCameraImageDataLocalFilePath = null;
            // showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
        return receiptCameraImageDataLocalFilePath;
    }

    /***
     * Returns the Path of the Receipt that is being previewed
     * 
     * @return
     */
    public String getImagePath() {
        return mReceiptPath;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case REQUEST_TAKE_PICTURE: {
            if (resultCode == Activity.RESULT_OK) {
                if (copyCapturedImage()) {
                    // Flurry Notification
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_CAMERA);
                    params.put(Flurry.PARAM_NAME_RESULT, Flurry.PARAM_VALUE_OKAY);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_MOBILE_ENTRY, Flurry.EVENT_NAME_ACTION, params);
                    if (!RolesUtil.isTestDriveUser()) {
                        mReceiptPreviewDialogFragmentListener.onPreviewComplete(mReceiptPath);
                        this.dismiss();
                    }
                } else {
                    // Flurry Notification.
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_FAILURE,
                            Flurry.PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_MOBILE_ENTRY, Flurry.EVENT_NAME_FAILURE, params);

                    // DialogFragmentFactory.getAlertOkayInstance(
                    // getActivity().getText(R.string.dlg_expense_camera_image_import_failed_title).toString(),
                    // R.string.dlg_expense_camera_image_import_failed_message).show(getFragmentManager(), null);
                    mReceiptPreviewDialogFragmentListener.onCameraFailure();
                    this.dismiss();
                }
            } else {
                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_CAMERA);
                params.put(Flurry.PARAM_NAME_RESULT, Flurry.PARAM_VALUE_CANCEL);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_MOBILE_ENTRY, Flurry.EVENT_NAME_ACTION, params);
            }
            this.dismiss();
            break;
        }
        }

        if (mReceiptPreviewDialogFragmentListener != null) {
            mReceiptPreviewDialogFragmentListener.onPreviewComplete(mReceiptPath);
        }
    }

    public void setReceiptPreviewDialogFragmentListener(ReceiptPreviewDialogFragmentListener listener) {
        mReceiptPreviewDialogFragmentListener = listener;
    }

    /**
     * Will copy the image data captured by the camera.
     * 
     * @param data
     *            the intent object containing capture information.
     */
    private boolean copyCapturedImage() {
        boolean retVal = true;
        // Assign the path written by the camera application.
        retVal = ImageUtil.compressAndRotateImage(mReceiptPath);
        if (!retVal) {
            mReceiptPath = null;
        }
        return retVal;
    }

}
