package com.concur.mobile.platform.ocr;

import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.expense.receipt.list.SaveReceiptRequestTask;
import com.concur.mobile.platform.expense.receipt.list.SaveReceiptRequestTask.SaveReceiptUploadListener;

public class OCRHelper {

    private static String TAG = OCRHelper.class.getSimpleName() + ".TAG";

    private SaveReceiptRequestTask mSaveReceiptRequestTask;
    private final int RequestID = 0;
    private SaveReceiptUploadListener mSaveReceiptUploadListener = null;
    private BaseAsyncResultReceiver mBaseAsyncResultReceiver = null;
    private Context mContext = null;

    /***
     * Constructor for OCRHelper
     * 
     * @param ctx
     *            Current Application Context
     */
    public OCRHelper(Context ctx) {
        mContext = ctx;
        setupListeners();
    }

    /***
     * Initialize global variable to be used as event listeners
     */
    private void setupListeners() {
        mSaveReceiptUploadListener = new SaveReceiptUploadListener() {

            @Override
            public void onStart(long contentLength) {
                Toast.makeText(mContext, "Upload Started", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onUpload(int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onComplete() {
                Toast.makeText(mContext, "Upload Complete", Toast.LENGTH_LONG).show();

            }
        };

        mBaseAsyncResultReceiver = new BaseAsyncResultReceiver(new Handler() {});
    }

    /***
     * Mark local receipt file to be uploaded for OCR processing.
     * 
     * @param ReceiptToUpload
     *            Local file path of the receipt to be uploaded
     * @param ReceiptToUploadType
     *            Type of file to be uploaded as receipt.
     */
    public void UploadReceipt(String ReceiptToUpload, ReceiptType ReceiptToUploadType) {
        File file = null;
        FileInputStream fis = null;

        try {
            file = new File(ReceiptToUpload);
            fis = new FileInputStream(file);

            mSaveReceiptRequestTask = new SaveReceiptRequestTask(mContext, RequestID, mBaseAsyncResultReceiver, null,
                    fis, file.length(), ReceiptToUploadType.toString(), mSaveReceiptUploadListener);
            mSaveReceiptRequestTask.execute();

            fis.close();
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
    }
}
