package com.concur.mobile.core.dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.RolesUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.widget.FileSearchActivity;
import com.concur.mobile.platform.ui.common.util.ImageUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class ReceiptChoiceDialogFragment extends ChoiceGridDialogFragment {

    private static final String CLS_TAG = ReceiptChoiceDialogFragment.class.getSimpleName();

    public static final String DIALOG_FRAGMENT_ID = "ReceiptChoiceDialog";

    public static final int ITEM_SIZE = 2;
    private static final int REQUEST_TAKE_PICTURE = 100;
    private static final int REQUEST_CHOOSE_IMAGE = 102;
    private static final int REQUEST_CHOOSE_PDF = 103;

    public static final long CAMERA_ITEM_ID = 1L;
    public static final long GALLERY_ITEM_ID = 2L;
    public static final long PDF_ITEM_ID = 3L;

    public ReceiptChoiceListener listener;

    private String receiptCameraImageDataLocalFilePath = null;
    private String receiptImageDataLocalFilePath;

    public interface ReceiptChoiceListener {

        public void onCameraSuccess(String filePath);

        public void onCameraFailure(String filePath);

        public void onGallerySuccess(String filePath);

        public void onGalleryFailure(String filePath);

        public void onStorageMountFailure(String filePath);
    }

    public ReceiptChoiceDialogFragment() {
        super();
        ChoiceItem[] items = new ChoiceItem[ITEM_SIZE];
        items[0] = new ChoiceItem(R.drawable.expense_icon_as_capture, R.string.camera, CAMERA_ITEM_ID);
        items[1] = new ChoiceItem(R.drawable.expense_icon_as_gallery, R.string.gallery, GALLERY_ITEM_ID);
        // items[2] = new ChoiceItem(R.drawable.expense_icon_add_receipt, R.string.pdf, PDF_ITEM_ID);
        setItems(items);
        setTitle(R.string.dlg_title_ocr_options);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (CAMERA_ITEM_ID == id) {
            captureReceipt();
        } else if (GALLERY_ITEM_ID == id) {
            chooseReceiptPicture();
        } else if (PDF_ITEM_ID == id) {
            choosePDF();
        }
    }

    /**
     * Captures a receipt image to be imported into the receipt store.
     * 
     * @return receiptCameraImageDataLocalFilePath
     */
    protected String captureReceipt() {
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
            }

        } else {
            receiptCameraImageDataLocalFilePath = null;
            // showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
        return receiptCameraImageDataLocalFilePath;
    }

    private void choosePDF() {
        Intent intent = null;
        intent = new Intent(getActivity(), FileSearchActivity.class);
        startActivityForResult(intent, REQUEST_CHOOSE_PDF);
    }

    /**
     * Will choose a picture from the devices media gallery.
     */
    private void chooseReceiptPicture() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
        } else {
            listener.onStorageMountFailure(receiptImageDataLocalFilePath);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // TODO try catch. reference : emaillookupfragment.onattach();
        listener = (ReceiptChoiceListener) activity;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case REQUEST_CHOOSE_IMAGE: {
            if (resultCode == Activity.RESULT_OK) {
                if (!copySelectedImage(data)) {
                    // Flurry Notification.
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_FAILURE,
                            Flurry.PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPT_CHOICE, Flurry.EVENT_NAME_FAILURE, params);
                    listener.onGalleryFailure(receiptImageDataLocalFilePath);
                    this.dismiss();
                } else {
                    // Flurry Notification
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_ALBUM);
                    params.put(Flurry.PARAM_NAME_RESULT, Flurry.PARAM_VALUE_OKAY);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPT_CHOICE, Flurry.EVENT_NAME_ACTION, params);
                    if (!RolesUtil.isTestDriveUser()) {
                        listener.onGallerySuccess(receiptImageDataLocalFilePath);
                        this.dismiss();
                    }
                }
            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + "onActivityResult(ChoosePicture): unhandled result code '" + resultCode
                        + "'.");
            }
            break;
        }

        case REQUEST_TAKE_PICTURE: {
            if (resultCode == Activity.RESULT_OK) {
                if (copyCapturedImage()) {
                    // Flurry Notification
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_CAMERA);
                    params.put(Flurry.PARAM_NAME_RESULT, Flurry.PARAM_VALUE_OKAY);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPT_CHOICE, Flurry.EVENT_NAME_ACTION, params);
                    if (!RolesUtil.isTestDriveUser()) {
                        listener.onCameraSuccess(receiptImageDataLocalFilePath);
                        this.dismiss();
                    }
                } else {
                    // Flurry Notification.
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_FAILURE,
                            Flurry.PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPT_CHOICE, Flurry.EVENT_NAME_FAILURE, params);

                    // DialogFragmentFactory.getAlertOkayInstance(
                    // getActivity().getText(R.string.dlg_expense_camera_image_import_failed_title).toString(),
                    // R.string.dlg_expense_camera_image_import_failed_message).show(getFragmentManager(), null);
                    listener.onCameraFailure(receiptImageDataLocalFilePath);
                    this.dismiss();
                }
            } else {
                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_CAMERA);
                params.put(Flurry.PARAM_NAME_RESULT, Flurry.PARAM_VALUE_CANCEL);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPT_CHOICE, Flurry.EVENT_NAME_ACTION, params);
            }
            this.dismiss();
            break;
        }
        case REQUEST_CHOOSE_PDF: {
            String selectedPDF = "unknown";

            if (resultCode == Activity.RESULT_OK) {
                if (data.hasExtra(Const.EXTRA_FILEPATH)) {
                    selectedPDF = data.getStringExtra(Const.EXTRA_FILEPATH);
                }
                Toast.makeText(getActivity(), "PDF Selected - " + selectedPDF, Toast.LENGTH_LONG).show();
            }

            this.dismiss();
        }
        default:
            break;
        }
    }

    /**
     * Will copy the image data selected within the gallery.
     * 
     * @param data
     *            the intent object containing the selection information.
     */
    private boolean copySelectedImage(Intent data) {
        receiptImageDataLocalFilePath = ImageUtil.compressAndRotateSelectedImage(getActivity(), data,
                receiptImageDataLocalFilePath);
        return receiptImageDataLocalFilePath != null ? true : false;
    }

    /**
     * Will copy the image data captured by the camera.
     * 
     */
    private boolean copyCapturedImage() {
        boolean retVal = true;
        // Assign the path written by the camera application.
        receiptImageDataLocalFilePath = receiptCameraImageDataLocalFilePath;
        retVal = ImageUtil.compressAndRotateImage(receiptImageDataLocalFilePath);
        if (!retVal) {
            receiptImageDataLocalFilePath = null;
        }
        return retVal;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (receiptCameraImageDataLocalFilePath != null) {
            outState.putString(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH, receiptCameraImageDataLocalFilePath);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH)) {
            receiptCameraImageDataLocalFilePath = savedInstanceState.getString(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH);
        }
    }
}
