package com.concur.mobile.platform.ui.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;

import com.concur.mobile.platform.util.Format;

/**
 * Partial port of ImageUtil in core. Only APIs relevant to platform.ui.common is included here.
 * 
 */
public class ImageUtil {

    /**
     * An enumeration for supported document types.
     */
    public enum DocumentType {
        PNG, JPG, PDF, UNKNOWN
    };

    public static final String CLS_TAG = ImageUtil.class.getSimpleName();

    /**
     * Provides a recommendation for a sampling and quality settings for shrinking the size of a receipt image.
     */
    public static class SampleSizeCompressFormatQuality {

        /**
         * Contains the recommended sampling size.
         */
        public int sampleSize = Const.RECEIPT_SOURCE_BITMAP_SAMPLE_SIZE;

        /**
         * Contains the recommended compression quality.
         */
        public int compressQuality = Const.RECEIPT_COMPRESS_BITMAP_QUALITY;

        /**
         * Contains the recommended compression format.
         */
        public CompressFormat compressFormat = Const.RECEIPT_COMPRESS_BITMAP_FORMAT;

    };

    /**
     * Determines whether the external media is mounted.
     * 
     * @return returns <code>true</code> if external media is mounted; <code>false</code> otherwise.
     */
    public static boolean isExternalMediaMounted() {
        return (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
    }

    /**
     * Will copy the image data selected within the gallery.
     * 
     * @param data
     *            the intent object containing the selection information.
     */
    public static String compressAndRotateSelectedImage(Context ctx, Intent data, String receiptImageDataLocalFilePath) {

        String retVal = receiptImageDataLocalFilePath;

        // First, obtain the stream of the selected gallery image.
        InputStream inputStream = ImageUtil.getInputStream(ctx, data.getData());
        int angle = ImageUtil.getOrientaionAngle(ctx, data.getData());
        if (inputStream != null) {
            // Obtain the recommended sampling size, etc.
            ImageUtil.SampleSizeCompressFormatQuality recConf = ImageUtil
                    .getRecommendedSampleSizeCompressFormatQuality(inputStream);
            ImageUtil.closeInputStream(inputStream);
            inputStream = null;
            if (recConf != null) {
                // Copy from the input stream to an external file.
                receiptImageDataLocalFilePath = ImageUtil.createExternalMediaImageFilePath();
                inputStream = new BufferedInputStream(ImageUtil.getInputStream(ctx, data.getData()), (8 * 1024));
                if (!ImageUtil.copySampledBitmap(inputStream, receiptImageDataLocalFilePath, recConf.sampleSize,
                        recConf.compressFormat, recConf.compressQuality, angle)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".copySelectedImage: unable to copy sampled image from '"
                            + inputStream + "' to '" + receiptImageDataLocalFilePath + "'");
                    receiptImageDataLocalFilePath = null;
                    retVal = receiptImageDataLocalFilePath;
                } else {
                    retVal = receiptImageDataLocalFilePath;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".copySelectedImage: unable to obtain recommended samplesize, etc.!");
                receiptImageDataLocalFilePath = null;
                retVal = receiptImageDataLocalFilePath;
            }
        } else {
            retVal = receiptImageDataLocalFilePath;
        }
        return retVal;
    }

    public static boolean copySampledBitmap(InputStream inStream, String dstImageFile, int sampleSize,
            Bitmap.CompressFormat format, int quality, int orientation) {
        boolean retVal = true;
        if (inStream != null) {
            Bitmap sampledBitmap = ImageUtil.loadSampledBitmap(inStream, sampleSize);
            if (sampledBitmap != null) {
                if (orientation > 0) {
                    switch (orientation) {
                    case 90:
                        sampledBitmap = rotateImage(sampledBitmap, 90);
                        break;
                    case 180:
                        sampledBitmap = rotateImage(sampledBitmap, 180);
                        break;
                    case 270:
                        sampledBitmap = rotateImage(sampledBitmap, 270);
                        break;
                    }
                }
                if (sampledBitmap != null) {
                    if (!writeBitmapToFile(sampledBitmap, format, quality, dstImageFile)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: unable to write sampled bitmap to '"
                                + dstImageFile + "'.");
                        retVal = false;
                    }
                    // re-cycle the bitmap.
                    if (sampledBitmap != null) {
                        sampledBitmap.recycle();
                        sampledBitmap = null;
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: unable to load sampled bitmap.");
                retVal = false;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: inStream is null!");
            retVal = false;
        }
        return retVal;
    }

    public static boolean copySampledBitmap(InputStream inStream, String srcImageFile, String dstImageFile,
            int sampleSize, Bitmap.CompressFormat format, int quality, int orientation) {
        boolean retVal = true;
        if (inStream != null) {
            Bitmap sampledBitmap = ImageUtil.loadSampledBitmap(inStream, sampleSize);
            if (sampledBitmap != null) {
                if (orientation > 0) {
                    switch (orientation) {
                    case 90:
                        sampledBitmap = rotateImage(sampledBitmap, 90);
                        break;
                    case 180:
                        sampledBitmap = rotateImage(sampledBitmap, 180);
                        break;
                    case 270:
                        sampledBitmap = rotateImage(sampledBitmap, 270);
                        break;
                    }
                } else {
                    ExifInterface ei;
                    try {
                        ei = new ExifInterface(srcImageFile);
                        orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL);
                        switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            sampledBitmap = rotateImage(sampledBitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            sampledBitmap = rotateImage(sampledBitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            sampledBitmap = rotateImage(sampledBitmap, 270);
                            break;
                        }
                        Log.d(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap.orientation: " + orientation);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (sampledBitmap != null) {
                    if (!writeBitmapToFile(sampledBitmap, format, quality, dstImageFile)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: unable to write sampled bitmap to '"
                                + dstImageFile + "'.");
                        retVal = false;
                    }
                    // re-cycle the bitmap.
                    if (sampledBitmap != null) {
                        sampledBitmap.recycle();
                        sampledBitmap = null;
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: unable to load sampled bitmap.");
                retVal = false;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: inStream is null!");
            retVal = false;
        }
        return retVal;
    }

    public static Bitmap rotateImage(Bitmap sampledBitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(sampledBitmap, 0, 0, sampledBitmap.getWidth(), sampledBitmap.getHeight(), matrix,
                true);
    }

    /**
     * Will get an input stream for a Uri.
     * 
     * @param context
     *            contains a context.
     * @param uri
     *            contains a Uri.
     * @return returns an instance of <code>InputStream</code> upon success; <code>null</code> otherwise.
     */
    public static InputStream getInputStream(Context context, Uri uri) {
        InputStream inStream = null;
        if (uri != null) {
            try {
                ContentResolver cr = context.getContentResolver();
                inStream = cr.openInputStream(uri);
            } catch (Exception exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getInputStream: ", exc);
            }
        }
        return inStream;
    }

    /**
     * Will get an orientation angle of image captured by system camera
     * 
     * @param context
     *            contains a context.
     * @param uri
     *            contains a Uri.
     * @return returns an image orientation angel;
     */
    public static int getOrientaionAngle(Context context, Uri uri) {
        int retVal = 0;
        if (uri != null) {
            try {
                ContentResolver cr = context.getContentResolver();
                String[] projection = { MediaStore.Images.ImageColumns.ORIENTATION };
                Cursor cursor = null;
                try {
                    cursor = cr.query(uri, projection, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            int columnIndex = cursor.getColumnIndex(projection[0]);
                            if (columnIndex >= 0) {
                                String value = cursor.getString(columnIndex);
                                retVal = Math.abs(Integer.parseInt(value));
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".getOrientaionAngle: can't find columnIndex '"
                                        + MediaStore.Images.ImageColumns.ORIENTATION + "'.");
                            }
                        } else {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".getOrientaionAngle: moveToFirst is false.");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getOrientaionAngle: cursor is null.");
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } catch (Exception exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getOrientaionAngle: ", exc);
            }
        }
        return retVal;
    }

    /**
     * Gets the recommended sample size, compress format and quality settings for reducing the size of a receipt image.
     * 
     * @param inStream
     *            the image input stream.
     * @return an instance of <code>SampleSizeCompressFormatQuality</code>.
     */
    public static SampleSizeCompressFormatQuality getRecommendedSampleSizeCompressFormatQuality(InputStream inStream) {
        SampleSizeCompressFormatQuality retVal = null;
        if (inStream != null) {
            BitmapFactory.Options bmptOpts = loadBitmapBounds(inStream);
            if (bmptOpts != null) {
                retVal = getRecommendedSampleSizeCompressFormatQuality(bmptOpts);
            } else {
                Log.e(Const.LOG_TAG,
                        CLS_TAG
                                + ".getRecommendedSampleSizeCompressFormatQuality: unable to obtain bounds for bitmap input stream.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getRecommendedSampleSizeCompressFormatQuality: inStream is null!");
        }
        return retVal;
    }

    /**
     * Will load the bitmap bounds for an image based on reading an input stream.
     * 
     * @param in
     *            contains the input stream.
     * @return the instance of <code>BitmapFactory.Options</code> that contains the width/height of the image.
     */
    public static BitmapFactory.Options loadBitmapBounds(InputStream inStream) {
        BitmapFactory.Options retVal = null;

        if (inStream != null) {
            // Decode image size
            retVal = new BitmapFactory.Options();
            retVal.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inStream, null, retVal);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".loadBitmapBounds: in is null!");
        }
        return retVal;
    }

    /**
     * Gets the recommended sample size, compress format and quality settings for reducing the size of a receipt image.
     * 
     * @param bmptOpts
     *            contains the dimensions of the bitmap.
     * @return an instance of <code>SampleSizeCompressFormatQuality</code>.
     */
    public static SampleSizeCompressFormatQuality getRecommendedSampleSizeCompressFormatQuality(
            BitmapFactory.Options bmptOpts) {
        SampleSizeCompressFormatQuality retVal = null;
        if (bmptOpts != null) {
            retVal = new SampleSizeCompressFormatQuality();
            long numberPixels = bmptOpts.outWidth * bmptOpts.outHeight;
            Log.i(Const.LOG_TAG, CLS_TAG + ".getRecommended..: dim: " + bmptOpts.outWidth + "x" + bmptOpts.outHeight
                    + " " + Long.toString(numberPixels) + " pixels.");
            final long MEGA_BYTE = (1024 * 1024);
            if (numberPixels < (2 * MEGA_BYTE)) {
                Log.i(Const.LOG_TAG,
                        CLS_TAG + ".getRecommendedSampleSizeCompressFormatQuality: numberPixels("
                                + Long.toString(numberPixels) + ") is < 2 mebibytes using full-sized image.");
                retVal.sampleSize = 1;
                retVal.compressQuality = 100;
            } else if (numberPixels >= (2 * MEGA_BYTE) && numberPixels < (4 * MEGA_BYTE)) {
                Log.i(Const.LOG_TAG,
                        CLS_TAG + ".getRecommendedSampleSizeCompressFormatQuality: numberPixels("
                                + Long.toString(numberPixels)
                                + ") is >= 2 mebibytes and < 4 mebibytes using 1/2 image size.");
                retVal.sampleSize = 2;
            } else if (numberPixels >= (4 * MEGA_BYTE) && numberPixels < (8 * MEGA_BYTE)) {
                Log.i(Const.LOG_TAG,
                        CLS_TAG + ".getRecommendedSampleSizeCompressFormatQuality: numberPixels("
                                + Long.toString(numberPixels)
                                + ") is >= 4 mebibytes and < 8 mebibytes using 1/2 image size.");
                retVal.sampleSize = 2;
            } else if (numberPixels >= (8 * MEGA_BYTE) && numberPixels < (16 * MEGA_BYTE)) {
                Log.i(Const.LOG_TAG,
                        CLS_TAG + ".getRecommendedSampleSizeCompressformatQuality: numberPixels("
                                + Long.toString(numberPixels)
                                + ") is >= 8 mebibytes and < 16 mebibytes using 1/2 image size.");
                retVal.sampleSize = 2;
            } else {
                Log.i(Const.LOG_TAG,
                        CLS_TAG + ".getRecommendedSampleSizeCompressformatQuality: numberPixels("
                                + Long.toString(numberPixels) + ") is >= 16 using 1/4 image size.");

                retVal.sampleSize = 4;
            }
        }
        return retVal;
    }

    /**
     * Will close an input stream.
     * 
     * @param inStream
     *            contains the input stream to be closed.
     */
    public static void closeInputStream(InputStream inStream) {
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".closeInputStream: I/O exception closing input stream", ioExc);
            }
        }
    }

    /**
     * Will close an output stream.
     * 
     * @param outStream
     *            contains the output stream to be closed.
     */
    public static void closeOutputStream(OutputStream outStream) {
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".closeOutputStream: I/O exception closing outout stream", ioExc);
            }
        }
    }

    /**
     * Will create an absolute path to external media (SD card) that can be used to store an image file.
     * 
     * The file is named using a calendar instance and formatted using
     * <code>FormatUtil.LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND</code> format. The file ends with an extension of '.jpg'.
     * 
     * @return a string containing the abolute path to an image or <code>null</code> if external storage media is not present.
     */
    public static String createExternalMediaImageFilePath() {
        String retVal = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File receiptFile = new File(Environment.getExternalStorageDirectory(), createImageFileName());
            retVal = receiptFile.getAbsolutePath();
        }
        return retVal;
    }

    /**
     * Gets the number of megabytes available on the SD card as a floating point value.
     * 
     * <b>NOTE</b><br>
     * This method assumes that a client calling this has previously checked that external media has been mounted.
     * 
     * @return the amount of space available on the SD card in megabytes.
     */
    public static float getMegabytesAvailableOnSDCard() {
        float retVal = 0.0F;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        retVal = bytesAvailable / (1024.f * 1024.f);
        return retVal;
    }

    /**
     * Creates an image file name based on the current date.
     * 
     * @return returns an image file name based on the current date.
     */
    public static String createImageFileName() {
        String fileName = null;
        // Create a file name based on the current date.
        Calendar cal = Calendar.getInstance();
        fileName = Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND, cal) + ".jpg";

        return fileName;
    }

    /**
     * Will write out a bitmap to a file in a given compressed format.
     * 
     * @param bitmap
     *            the bitmap to be written.
     * @param format
     *            the image format.
     * @param quality
     *            the quality of the image.
     * @param filePath
     *            the file path in which to write the file.
     * @return returns <code>true</code> if <code>bitmap</code> was written to <code>filePath</code> in format <code>format</code>
     *         .
     */
    public static boolean writeBitmapToFile(Bitmap bitmap, Bitmap.CompressFormat format, int quality, String filePath) {
        boolean retVal = true;

        BufferedOutputStream bufOut = null;
        try {
            bufOut = new BufferedOutputStream(new FileOutputStream(filePath), (64 * 1024));
            if (!bitmap.compress(format, quality, bufOut)) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".writeBitmapToFile: unable to compress bitmap to JPEG.");
                retVal = false;
            }
        } catch (FileNotFoundException fnfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".writeBitmapToFile: unable to open file '" + filePath + "' for writing!",
                    fnfExc);
            retVal = false;
        } finally {
            if (bufOut != null) {
                try {
                    bufOut.close();
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".writeBitmapToFile: I/O exception closing output stream for file '"
                            + filePath + "'", ioExc);
                }
            }
        }
        return retVal;
    }

    /**
     * Will load the bitmap bounds for an image stored in a file and not the underlying image data.
     * 
     * @param filePath
     *            the path of the file.
     * @return the instance of <code>BitmapFactory.Options</code> that contains the width/height of the image.
     */
    public static BitmapFactory.Options loadBitmapBounds(String filePath) {
        BitmapFactory.Options retVal = null;

        if (filePath != null) {
            try {
                // Decode image size
                retVal = new BitmapFactory.Options();
                retVal.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(new File(filePath)), null, retVal);
            } catch (FileNotFoundException fnfExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".loadBitmapBounds: unable to locate image file '" + filePath + "'.");
                retVal = null;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".loadBitmapBounds: filePath is null!");
        }
        return retVal;
    }

    /**
     * Will decode an image into a <code>Bitmap</code> object such that no size is greater than <code>imageSize</code> pixels.
     * This method does not load the entire image into memory, but rather first obtains the width/height of the image, then
     * chooses a suitable sampling size based on <code>imageSize</code>.
     * 
     * @param filePath
     *            the absolute path to the image.
     * @param imageSize
     *            the maximum size in pixels of width/height to be loaded.
     * @return an instance of <code>Bitmap</code> if <code>filePath</code> can be decoded; otherwise, <code>null</code> is
     *         returned.
     */
    public static Bitmap loadScaledBitmap(String filePath, int imageSize) {
        Bitmap bitmap = null;
        try {
            // Decode image size
            BitmapFactory.Options bmpOpts = new BitmapFactory.Options();
            bmpOpts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(new File(filePath)), null, bmpOpts);

            Log.d(Const.LOG_TAG, CLS_TAG + ".loadScaledBitmap: bounds -> " + bmpOpts.outWidth + "x" + bmpOpts.outHeight);

            int scale = 1;
            if (bmpOpts.outHeight > imageSize || bmpOpts.outWidth > imageSize) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(imageSize / (double) Math.max(bmpOpts.outHeight, bmpOpts.outWidth))
                                / Math.log(0.5)));
            }
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(filePath)), null, o2);
        } catch (FileNotFoundException fnfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".loadScaledBitmap: unable to locate image file '" + filePath + "'.");
        }
        return bitmap;
    }

    /**
     * Will create a bitmap object representing a sampled image stored in a file.
     * 
     * @param filePath
     *            the image file path.
     * @param sampleSize
     *            the sample size.
     * @return an instance of <code>Bitmap</code> containing the sampled image.
     */
    public static Bitmap loadSampledBitmap(String filePath, int sampleSize) {
        Bitmap bitmap = null;
        try {
            // Decode with sampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sampleSize;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(filePath)), null, o2);
        } catch (FileNotFoundException fnfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".loadScaledBitmap: unable to locate image file '" + filePath + "'.");
        }
        return bitmap;
    }

    /**
     * Will create a bitmap object representing a sampled image from an input stream.
     * 
     * @param inStream
     *            the image file input stream.
     * @param sampleSize
     *            the sample size.
     * @return an instance of <code>Bitmap</code> containing the sampled image.
     */
    public static Bitmap loadSampledBitmap(InputStream inStream, int sampleSize) {
        Bitmap bitmap = null;
        // Decode with sampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = sampleSize;
        bitmap = BitmapFactory.decodeStream(inStream, null, o2);
        return bitmap;
    }

    /**
     * Creates a PDF file name based on the current date.
     * 
     * @return returns a PDF file name based on the current date.
     */
    public static String createPDFFileName() {
        String fileName = null;
        // Create a file name based on the current date.
        Calendar cal = Calendar.getInstance();
        fileName = Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND, cal) + ".pdf";
        return fileName;
    }

    /**
     * Will create an absolute path to external media (SD card) that can be used to store a pdf file.
     * 
     * The file is named using a calendar instance and formatted using
     * <code>FormatUtil.LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND</code> format. The file ends with an extension of '.pdf'.
     * 
     * @return a string containing the abolute path to an pdf or <code>null</code> if external storage media is not present.
     */
    public static String createExternalMediaPDFFilePath() {
        String retVal = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File pdfFile = new File(Environment.getExternalStorageDirectory(), createPDFFileName());
            retVal = pdfFile.getAbsolutePath();
        }
        return retVal;
    }

    /**
     * Will write all bytes from <code>inStream</code> to <code>outStream</code> transferring up to <code>bufSize</code> bytes at
     * a time.
     * 
     * @param inStream
     *            contains the input stream to read from.
     * @param outStream
     *            contains the output stream to write to.
     * @param bufSize
     *            contains the number of bytes read/written at one time.
     * @throws throws an IOException if an exception occurs while reading/writing.
     */
    public static void writeAllBytes(InputStream inStream, OutputStream outStream, int bufSize) throws IOException {
        if (inStream != null && outStream != null) {
            byte[] buffer = new byte[bufSize];
            int numBytesRead = 0;
            while ((numBytesRead = inStream.read(buffer, 0, bufSize)) != -1) {
                outStream.write(buffer, 0, numBytesRead);
            }
        }
    }

    /**
     * Will read the magic number from <code>file</code> and determine the document type. Supported values are defined in
     * <code>ImageUtil.DocumentType</code>.
     * 
     * @param file
     *            the file to examine.
     * 
     * @return an element of <code>DocumentType</code> representing the type of document; if unknown, then the value of
     *         <code>UNKNOWN</code> is returned.
     */
    public static DocumentType getDocumentType(File file) {

        DocumentType docType = DocumentType.UNKNOWN;

        byte[] magicNumber = new byte[4];
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            if ((fin.read(magicNumber, 0, magicNumber.length)) == 4) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Integer.toHexString((0x000000FF & magicNumber[0])));
                strBldr.append(',');
                strBldr.append(Integer.toHexString((0x000000FF & magicNumber[1])));
                strBldr.append(',');
                strBldr.append(Integer.toHexString((0x000000FF & magicNumber[2])));
                strBldr.append(',');
                strBldr.append(Integer.toHexString((0x000000FF & magicNumber[3])));
                Log.d(Const.LOG_TAG, CLS_TAG + ".getImageType: 1st 4 bytes: " + strBldr.toString());
                if ((0x000000FF & magicNumber[0]) == 0x89 && (0x000000FF & magicNumber[1]) == 0x50
                        && (0x000000FF & magicNumber[2]) == 0x4e && (0x000000FF & magicNumber[3]) == 0x47) {
                    docType = DocumentType.PNG;
                } else if ((0x000000FF & magicNumber[0]) == 0xff && (0x000000FF & magicNumber[1]) == 0xd8
                        && (0x000000FF & magicNumber[2]) == 0xff && (0x000000FF & magicNumber[3]) == 0xe0) {
                    // JFIF
                    docType = DocumentType.JPG;
                } else if ((0x000000FF & magicNumber[0]) == 0xff && (0x000000FF & magicNumber[1]) == 0xd8
                        && (0x000000FF & magicNumber[2]) == 0xff && (0x000000FF & magicNumber[3]) == 0xe1) {
                    // EXIF
                    docType = DocumentType.JPG;
                } else if ((0x000000FF & magicNumber[0]) == 0x25 && (0x000000FF & magicNumber[1]) == 0x50
                        && (0x000000FF & magicNumber[2]) == 0x44 && (0x000000FF & magicNumber[3]) == 0x46) {
                    // PDF
                    docType = DocumentType.PDF;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getImageType: corrupted image file '" + file.getAbsolutePath() + "'.");
            }
        } catch (FileNotFoundException fnfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getImageType: image file '" + file.getAbsolutePath() + "' not found.",
                    fnfExc);
        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getImageType: I/O error reading from '" + file.getAbsolutePath() + "'.",
                    ioExc);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                    fin = null;
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getImageType: I/O exception closing '" + file.getAbsolutePath()
                            + "'.", ioExc);
                }
            }
        }
        return docType;
    }

    public static InputStream convertFileTpInputStream(String filePath) {
        InputStream is = null;

        try {
            is = new FileInputStream(filePath);
            // is.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return is;
    }

    /**
     * This method will Sample the captured image into memory, then write out to file and it will rotate the image if required.
     * 
     * @param receiptImageDataLocalFilePath
     * @return
     */
    public static boolean compressAndRotateImage(String receiptImageDataLocalFilePath) {
        boolean retVal = true;
        if (receiptImageDataLocalFilePath == null || receiptImageDataLocalFilePath.length() == 0) {
            retVal = false;
        } else {
            // Sample the captured image into memory, then write out to file.
            ImageUtil.SampleSizeCompressFormatQuality recConf = ImageUtil
                    .getRecommendedSampleSizeCompressFormatQuality(convertFileTpInputStream(receiptImageDataLocalFilePath));
            if (recConf != null) {
                if (recConf.sampleSize > 1) {
                    if (!ImageUtil.copySampledBitmap(receiptImageDataLocalFilePath, receiptImageDataLocalFilePath,
                            recConf.sampleSize, recConf.compressFormat, recConf.compressQuality)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".compressAndRotateImage: unable to copy sampled image from '"
                                + receiptImageDataLocalFilePath + "' to '" + receiptImageDataLocalFilePath + "'");
                        retVal = false;
                    }
                } else {
                    // No-op, just use the captured image directly, i.e., no need to
                    // re-sample it.
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".compressAndRotateImage: unable to obtain recommended samplesize, etc.!");
                retVal = false;
            }
        }
        return retVal;
    }

    /**
     * Will sample the image stored in a file into a bitmap, then write it out to a destination file with a certain format and
     * quality.
     * 
     * @param srcImageFile
     *            the source image file.
     * @param dstImageFile
     *            the destination image file.
     * @param sampleSize
     *            the source image sample size.
     * @param format
     *            the destination image file format.
     * @param quality
     *            the destination image quality.
     * @return will return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public static boolean copySampledBitmap(String srcImageFile, String dstImageFile, int sampleSize,
            Bitmap.CompressFormat format, int quality) {

        boolean retVal = true;
        if (srcImageFile != null) {
            Bitmap sampledBitmap = ImageUtil.loadSampledBitmap(srcImageFile, sampleSize);
            if (sampledBitmap != null) {
                ExifInterface ei;
                try {
                    ei = new ExifInterface(srcImageFile);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
                    switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        sampledBitmap = rotateImage(sampledBitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        sampledBitmap = rotateImage(sampledBitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        sampledBitmap = rotateImage(sampledBitmap, 270);
                        break;
                    }
                    Log.d(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap.orientation: " + orientation);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (!writeBitmapToFile(sampledBitmap, format, quality, dstImageFile)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: unable to write sampled bitmap to '"
                            + dstImageFile + "'.");
                    retVal = false;
                }
                // re-cycle the bitmap.
                if (sampledBitmap != null) {
                    sampledBitmap.recycle();
                    sampledBitmap = null;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: unable to load sampled bitmap '" + srcImageFile
                        + "'.");
                retVal = false;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".copySampledBitmap: srcImageFile is null!");
            retVal = false;
        }
        return retVal;
    }
}
