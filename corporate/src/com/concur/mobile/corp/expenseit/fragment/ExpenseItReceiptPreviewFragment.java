package com.concur.mobile.corp.expenseit.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apptentive.android.sdk.Log;
import com.concur.breeze.R;
import com.concur.mobile.base.util.Const;

public class ExpenseItReceiptPreviewFragment extends Fragment {

    public static final String EXPENSEIT_RECEIPT_PREVIEW_FRAGMENT_TAG = "EXPENSEIT_RECEIPT_PREVIEW_FRAGMENT";

    public static final String EXPENSEIT_IMAGE_FILE_PATH_KEY = "EXPENSEIT_IMAGE_FILE_PATH_KEY";

    public static final String EXPENSEIT_PREVIEW_RETAKE_RESULT_KEY =  "EXPENSEIT_PREVIEW_RETAKE_RESULT";

    public static final String EXPENSEIT_PREVIEW_IMAGE_SOURCE_KEY = "EXPENSEIT_PREVIEW_IMAGE_SOURCE";

    public static final String EXPENSEIT_PREVIEW_SOURCE_GALLERY_KEY = "EXPENSEIT_PREVIEW_SOURCE_GALLERY";

    public static final String EXPENSEIT_PREVIEW_SOURCE_CAMERA_KEY = "EXPENSEIT_PREVIEW_SOURCE_CAMERA";

    public static final int USE_IMAGE_REQUEST_CODE = 626;

    private View fragmentView;
    private String imageSource;
    private Button retakeButton;
    private Button useButton;
    private ProgressBar receiptImageProgressBar;
    private TextView receiptImageUnavailable;
    private ImageView receiptImageView;
    private Bitmap receiptImage;

    private ExpenseItPreviewCallbacks mExpenseItPreviewCallbacks;

    public interface ExpenseItPreviewCallbacks {
        void onReceiptPreviewResult(boolean retake);
    }

    public static final ExpenseItReceiptPreviewFragment newInstance(String fileName, String imageSource) {
        ExpenseItReceiptPreviewFragment dialog = new ExpenseItReceiptPreviewFragment();
        Bundle args = new Bundle();
        args.putString(ExpenseItReceiptPreviewFragment.EXPENSEIT_PREVIEW_IMAGE_SOURCE_KEY, imageSource);
        args.putString(ExpenseItReceiptPreviewFragment.EXPENSEIT_IMAGE_FILE_PATH_KEY, fileName);
        dialog.setArguments(args);

        return dialog;
    }

    private void setImageToDisplay() {
        receiptImageView = (ImageView) fragmentView.findViewById(R.id.camera_preview);
        receiptImageUnavailable = (TextView) fragmentView.findViewById(R.id.txtvReceiptImageUnavailable);

        if (receiptImageView != null) {
            final String receiptImageFilePath = getArguments().getString(ExpenseItReceiptPreviewFragment.EXPENSEIT_IMAGE_FILE_PATH_KEY);
            if (receiptImageFilePath != null) {
                receiptImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        receiptImage = BitmapFactory.decodeFile(receiptImageFilePath);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                receiptImageProgressBar.setVisibility(View.GONE);
                                if (receiptImage != null) {
                                    receiptImageView.setImageBitmap(receiptImage);
                                } else {
                                    receiptImageUnavailable.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setImageToDisplay();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mExpenseItPreviewCallbacks = (ExpenseItPreviewCallbacks) activity;
        } catch (ClassCastException e ) {
            Log.e(Const.LOG_TAG, "activity must implement ExpenseItPreviewCallbacks callback!", e);
            throw e;
        }
    }

    private void initializeActionButtons() {
        if (getArguments().containsKey(ExpenseItReceiptPreviewFragment.EXPENSEIT_PREVIEW_IMAGE_SOURCE_KEY)) {
            imageSource = getArguments().getString(ExpenseItReceiptPreviewFragment.EXPENSEIT_PREVIEW_IMAGE_SOURCE_KEY);
            if (imageSource != null && imageSource.equals(EXPENSEIT_PREVIEW_SOURCE_GALLERY_KEY)) {
                retakeButton = (Button) fragmentView.findViewById(R.id.retake_btn);
                retakeButton.setText(R.string.cancel_button);
                retakeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().finish();
                    }
                });
            } else {
                retakeButton = (Button) fragmentView.findViewById(R.id.retake_btn);
                retakeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mExpenseItPreviewCallbacks.onReceiptPreviewResult(true);
                    }
                });
            }
        }

        useButton = (Button) fragmentView.findViewById(R.id.accept_btn);
        useButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpenseItPreviewCallbacks.onReceiptPreviewResult(false);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fragmentView = inflater.inflate(R.layout.fragment_expense_it_receipt_preview, container, false);
        receiptImageProgressBar = (ProgressBar) fragmentView.findViewById(R.id.expenseit_receipt_image_loading_progress);
        receiptImageProgressBar.setVisibility(View.VISIBLE);
        initializeActionButtons();
        return fragmentView;
    }
}
