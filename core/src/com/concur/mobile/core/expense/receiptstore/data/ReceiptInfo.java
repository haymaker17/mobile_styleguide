/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.util.Parse;

/**
 * A class holding receipt information for a receipt stored in the Receipt Store.
 * 
 * @author AndrewK
 */
public class ReceiptInfo {

    private static final String CLS_TAG = ReceiptInfo.class.getSimpleName();

    /**
     * The <code>ReceiptDAO</code> that backs this <code>ReceiptInfo</code>, or <code>null</code> if this object isn't backed by
     * one.
     */
    private ReceiptDAO receiptDAO = null;

    /**
     * Default constructor;
     */
    public ReceiptInfo() {
    }

    /**
     * Constructor to reference new <code>ReceiptDAO</code> from this old object.
     * 
     * @param receiptDAO
     */
    public ReceiptInfo(ReceiptDAO receiptDAO) {
        this.receiptDAO = receiptDAO;
    }

    // A utility constructor to help with massaging offline receipts into a format needed for the UI
    public ReceiptInfo(ReceiptShareItem rsi) {
        this.fileName = rsi.fileName;
        this.fileType = rsi.fileName.substring(rsi.fileName.lastIndexOf('.') + 1);
        this.imageOrigin = "mobile";

        Calendar imageCal = null;
        if (rsi.displayName != null) {
            // As a hack, we store the image selection timestamp in the display name
            imageCal = Parse.parseXMLTimestamp(rsi.displayName);
        }

        this.imageCalendar = this.updateTime = imageCal;
    }

    /**
     * Contains the receipt image ID.
     */
    private String receiptImageId;

    /**
     * Contains the file name (if any) provided when the receipt was uploaded.
     */
    private String fileName;

    /**
     * Contains the file type (if any) of the receipt image, i.e., PNG, JPG, etc.
     */
    private String fileType;

    /**
     * Contains the receipt store image upload date as a string.
     */
    private String imageDate;

    /**
     * Contains the receipt store image upload date as a <code>Calendar</code> object.
     */
    private Calendar imageCalendar;

    /**
     * Contains the receipt image origin, i.e., MOBILE_EXPENSE, MOBILE, etc.
     */
    private String imageOrigin;

    /**
     * Contains the receipt image source.
     */
    private String imageSource;

    /**
     * Contains the receipt image full-size URL.
     */
    private String imageUrl;

    /**
     * Contains the receipt image thumbnail URL (if any provided).
     */
    private String thumbUrl;

    /**
     * Contains a thumbnail image of the selected item.
     */
    transient private Bitmap thumbnail;

    /**
     * Contains the last update time for this receipt info.
     */
    private Calendar updateTime;

    /**
     * @param receiptDAO
     *            the receiptDAO to set
     */
    public void setReceiptDAO(ReceiptDAO receiptDAO) {
        this.receiptDAO = receiptDAO;
    }

    /**
     * @param receiptImageId
     *            the receiptImageId to set
     */
    public void setReceiptImageId(String receiptImageId) {
        this.receiptImageId = receiptImageId;

        if (receiptDAO != null) {
            receiptDAO.setId(receiptImageId);
        }
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;

        if (receiptDAO != null) {
            receiptDAO.setFileName(fileName);
        }
    }

    /**
     * @param fileType
     *            the fileType to set
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;

        if (receiptDAO != null) {
            receiptDAO.setFileType(fileType);
        }
    }

    /**
     * @param imageDate
     *            the imageDate to set
     */
    public void setImageDate(String imageDate) {
        this.imageDate = imageDate;
    }

    /**
     * @param imageCalendar
     *            the imageCalendar to set
     */
    public void setImageCalendar(Calendar imageCalendar) {
        this.imageCalendar = imageCalendar;
        if (receiptDAO != null) {
            receiptDAO.setReceiptUploadTime(imageCalendar);
        }
    }

    /**
     * @param imageOrigin
     *            the imageOrigin to set
     */
    public void setImageOrigin(String imageOrigin) {
        this.imageOrigin = imageOrigin;

        if (receiptDAO != null) {
            receiptDAO.setImageOrigin(imageOrigin);
        }
    }

    /**
     * @param imageSource
     *            the imageSource to set
     */
    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }

    /**
     * @param imageUrl
     *            the imageUrl to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;

        if (receiptDAO != null) {
            receiptDAO.setImageUrl(imageUrl);
        }
    }

    /**
     * @param thumbUrl
     *            the thumbUrl to set
     */
    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;

        if (receiptDAO != null) {
            receiptDAO.setThumbUrl(thumbUrl);
        }
    }

    /**
     * @param thumbnail
     *            the thumbnail to set
     */
    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    /**
     * @param updateTime
     *            the updateTime to set
     */
    public void setUpdateTime(Calendar updateTime) {
        this.updateTime = updateTime;

        if (receiptDAO != null) {
            receiptDAO.setReceiptUploadTime(updateTime);
        }
    }

    /**
     * Generates a thumbnail of the receipt image if the <code>fileName</code> is not null.
     */
    public void generateThumbnail() {
        if (getFileName() != null) {
            try {
                InputStream inStream = new BufferedInputStream(new FileInputStream(new File(getFileName())), (8 * 1024));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                thumbnail = BitmapFactory.decodeStream(inStream, null, options);
                ViewUtil.closeInputStream(inStream);
            } catch (FileNotFoundException fnfExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".loadImageBitmap: unable to load '" + getFileName() + "'.", fnfExc);
            }
        }
    }

    /**
     * @return the receiptDAO
     */
    public ReceiptDAO getReceiptDAO() {
        return receiptDAO;
    }

    /**
     * @return the receiptImageId
     */
    public String getReceiptImageId() {
        if (receiptDAO != null) {
            receiptImageId = receiptDAO.getId();
        }

        return receiptImageId;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        if (receiptDAO != null) {
            fileName = receiptDAO.getFileName();
        }

        return fileName;
    }

    /**
     * @return the fileType
     */
    public String getFileType() {
        if (receiptDAO != null) {
            fileType = receiptDAO.getFileType();
        }

        return fileType;
    }

    /**
     * @return the imageDate
     */
    public String getImageDate() {
        return imageDate;
    }

    /**
     * @return the imageCalendar
     */
    public Calendar getImageCalendar() {
        if (receiptDAO != null) {
            imageCalendar = receiptDAO.getReceiptUploadTime();
        }

        return imageCalendar;
    }

    /**
     * @return the imageOrigin
     */
    public String getImageOrigin() {
        if (receiptDAO != null) {
            imageOrigin = receiptDAO.getImageOrigin();
        }
        return imageOrigin;
    }

    /**
     * @return the imageSource
     */
    public String getImageSource() {
        return imageSource;
    }

    /**
     * @return the imageUrl
     */
    public String getImageUrl() {
        if (receiptDAO != null) {
            imageUrl = receiptDAO.getImageUrl();
        }
        return imageUrl;
    }

    /**
     * @return the thumbUrl
     */
    public String getThumbUrl() {
        if (receiptDAO != null) {
            thumbUrl = receiptDAO.getThumbUrl();
        }
        return thumbUrl;
    }

    /**
     * @return the thumbnail
     */
    public Bitmap getThumbnail() {
        return thumbnail;
    }

    /**
     * @return the updateTime
     */
    public Calendar getUpdateTime() {
        if (receiptDAO != null) {
            updateTime = receiptDAO.getReceiptUploadTime();
        }
        return updateTime;
    }

}
