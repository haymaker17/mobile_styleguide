/**
 * 
 */
package com.concur.mobile.platform.expense.receipt.list.dao;

import java.util.Calendar;

import android.content.ContentResolver;
import android.net.Uri;

import com.concur.mobile.platform.expense.provider.Expense;

/**
 * Provides an interface describing receipt information.
 * 
 * @author andrewk
 */
public interface ReceiptDAO {

    /**
     * Gets the eTag value.
     * 
     * @return the eTag value.
     */
    public String getETag();

    /**
     * Sets the eTag value.
     * 
     * @param eTag
     *            contains the eTag value.
     */
    public void setETag(String eTag);

    /**
     * Gets the image id.
     * 
     * @return the image id.
     */
    public String getId();

    /**
     * Sets the image id.
     * 
     * @param id
     *            contains the image id.
     */
    public void setId(String id);

    /**
     * Gets the receipt image Uri. <br>
     * Value can be used to retrieve/update full-sized/thumbnail receipt data.<br>
     * The <code>THUMBNAIL_QUERY_PARAMETER</code> should be specified in order to read/write thumbnail receipt image data.
     * 
     * @see Expense.ReceiptColumns#THUMBNAIL_QUERY_PARAMETER
     * @see ContentResolver#openInputStream(Uri)
     * @see ContentResolver#openOutputStream(Uri)
     * 
     * @return the receipt image Uri.
     */
    public String getUri();

    /**
     * Sets the receipt image Uri.
     * 
     * @param uri
     *            contains the receipt image Uri.
     */
    public void setUri(String uri);

    /**
     * Gets the local path. <br>
     * <br>
     * Value can be used to directly access any locally stored receipt data that is contained in a file.
     * 
     * @return the local path.
     */
    public String getLocalPath();

    /**
     * Sets the local path.
     * 
     * @param localPath
     *            contains the local path.
     */
    public void setLocalPath(String localPath);

    /**
     * Gets the content type of the full-sized receipt data.
     * 
     * @return the content type of the full-sized receipt data.
     */
    public String getContentType();

    /**
     * Sets the content type of the full-sized receipt data.
     * 
     * @param contentType
     *            contains the content type.
     */
    public void setContentType(String contentType);

    /**
     * Gets the receipt data.<br>
     * <br>
     * Value contains the binary full-sized receipt image data.
     * 
     * @return the receipt data.
     */
    public byte[] getReceiptData();

    /**
     * Sets the receipt data.
     * 
     * @param receiptData
     *            contains the receipt data.
     */
    public void setReceiptData(byte[] receiptData);

    /**
     * Gets the thumbnail receipt image Uri. <br>
     * <br>
     * This value can be used to retrieve/update thumbnail receipt data.<br>
     * The <code>THUMBNAIL_QUERY_PARAMETER</code> should be a part of this URI if when using the expense content provider to
     * read/update receipt thumbnail receipt image data.
     * 
     * @see Expense.ReceiptColumns#THUMBNAIL_QUERY_PARAMETER
     * @see ContentResolver#openInputStream(Uri)
     * @see ContentResolver#openOutputStream(Uri)
     * 
     * @return the thumbnail receipt image Uri.
     */
    public String getThumbnailUri();

    /**
     * Sets the thumbnail receipt image Uri.
     * 
     * @param uri
     *            contains
     */
    public void setThumbnailUri(String uri);

    /**
     * Gets the thumbnail local path. <br>
     * <br>
     * This value can be used to directly access any locally stored thumbnail receipt data that is contained in a file.
     * 
     * @return the thumbnail local path.
     */
    public String getThumbnailLocalPath();

    /**
     * Sets the thumbnail local path.
     * 
     * @param localPath
     *            contains the thumbnail local path.
     */
    public void setThumbnailLocalPath(String localPath);

    /**
     * Gets the content type of the thumbnail receipt data.
     * 
     * @return the content type of the thumbnail receipt data.
     */
    public String getThumbnailContentType();

    /**
     * Sets the content type of the thumbnail receipt data.
     * 
     * @param contentType
     *            contains the content type.
     */
    public void setThumbnailContentType(String contentType);

    /**
     * Gets the thumbnail receipt data. <br>
     * <br>
     * This value contains the binary thumbnail-sized receipt image data.
     * 
     * @return the thumbnail receipt data.
     */
    public byte[] getThumbnailReceiptData();

    /**
     * Sets the thumbnail receipt data.
     * 
     * @param receiptData
     *            contains the thumbnail receipt data.
     */
    public void setThumbnailReceiptData(byte[] receiptData);

    /**
     * Gets the content Uri for this receipt.
     * 
     * @return the content Uri for this receipt.
     */
    public Uri getContentUri();

    /**
     * Gets whether this receipt is attached to an expense.
     * 
     * @return whether this receipt is attached to an expense.
     */
    public boolean isAttached();

    /**
     * Sets whether this receipt is attached to an expense.
     * 
     * @param attached
     */
    public void setAttached(boolean attached);

    /**
     * Gets the UTC millisecond timestamp reflecting the last time the full-sized receipt image was requested.
     * 
     * @return the UTC millisecond timestamp reflecting the last time the full-sized receipt image was requested.
     */
    public Long getLastAccessTime();

    /**
     * Gets the receipt image file name. <br>
     * <br>
     * This value contains the original file name of the receipt data as it was uploaded.
     * 
     * @return the receipt image file name.
     */
    public String getFileName();

    /**
     * Sets the receipt image file name.
     * 
     * @param fileName
     *            contains the receipt image file name.
     */
    public void setFileName(String fileName);

    /**
     * Gets the receipt image file type. <br>
     * <br>
     * This value contains the original file type, i.e., "JPEG", "JPG", "PDF" of the receipt data as it was uploaded.
     * 
     * @return the receipt image file type.
     */
    public String getFileType();

    /**
     * Sets the receipt image file type.
     * 
     * @param fileType
     *            contains the receipt image file type.
     */
    public void setFileType(String fileType);

    /**
     * Gets the receipt upload time.<br>
     * <br>
     * This value contains the original time of the receipt upload.
     * 
     * @return returns the receipt upload time.
     */
    public Calendar getReceiptUploadTime();

    /**
     * Sets the receipt upload time.
     * 
     * @param receiptUploadTime
     *            contains the receipt upload time.
     */
    public void setReceiptUploadTime(Calendar receiptUploadTime);

    /**
     * Gets the receipt system origin.
     * 
     * @return the receipt system origin.
     */
    public String getSystemOrigin();

    /**
     * Sets the receipt system origin.
     * 
     * @param systemOrigin
     *            contains the receipt system origin.
     */
    public void setSystemOrigin(String systemOrigin);

    /**
     * Gets the receipt image origin.
     * 
     * @return the receipt image origin.
     */
    public String getImageOrigin();

    /**
     * Sets the receipt image origin.
     * 
     * @param imageOrigin
     *            the receipt image origin.
     */
    public void setImageOrigin(String imageOrigin);

    /**
     * Gets the receipt full-sized image URL.<br>
     * <br>
     * This contains the URL of the receipt image data that can be retrieved from the server.<br>
     * <br>
     * <b>NOTE:</b>&nbsp;This URL is time sensitive and is only active for a period of 15 minutes from the time the receipt list
     * data was loaded.
     * 
     * @return the receipt full-sized image URL.
     */
    public String getImageUrl();

    /**
     * Sets the receipt full-sized image URL.
     * 
     * @param imageUrl
     *            contains the receipt full-sized image URL.
     */
    public void setImageUrl(String imageUrl);

    /**
     * Gets the receipt thumb-sized image URL. <br>
     * <br>
     * This contains the URL of the thumbnail receipt image data that can be retrieved from the server.<br>
     * <br>
     * <b>NOTE:</b>&nbsp;This URL is time sensitive and is only active for a period of 15 minutes from the time the receipt list
     * data was loaded.
     * 
     * @return the receipt thumb-sized image URL.
     */
    public String getThumbUrl();

    /**
     * Sets the receipt thumb-sized image URL.
     * 
     * @param thumbUrl
     *            contains the receipt thumb-sized image URL.
     */
    public void setThumbUrl(String thumbUrl);

    /**
     * Gets the receipt OCR image origin.
     * 
     * @return the receipt OCR image origin.
     */
    public String getOcrImageOrigin();

    /**
     * Sets the receipt OCR image origin.
     * 
     * @param ocrImageOrigin
     *            contains the receipt OCR image origin.
     */
    public void setOcrImageOrigin(String ocrImageOrigin);

    /**
     * Gets the receipt OCR status.
     * 
     * <br>
     * The following values are supported:<br>
     * <table border="1">
     * <tr>
     * <td><b>Value</b></td>
     * <td><b>Meaning</b></td>
     * </tr>
     * <tr>
     * <td>NOT_COMPANY_ENABLED</td>
     * <td>OCR is not enabled for the users company.</td>
     * </tr>
     * <tr>
     * <td>OCR_NOT_AVAILABLE</td>
     * <td>OCR is not available.</td>
     * </tr>
     * <tr>
     * <td>OCR_STAT_UNKNOWN</td>
     * <td>OCR has no knowledge of this receipt.</td>
     * </tr>
     * <tr>
     * <td>A_PEND</td>
     * <td>OCR is auto pending.</td>
     * </tr>
     * <tr>
     * <td>A_DONE</td>
     * <td>OCR auto has completed.</td>
     * </tr>
     * <tr>
     * <td>A_CNCL</td>
     * <td>OCR auto was cancelled.</td>
     * </tr>
     * <tr>
     * <td>A_FAIL</td>
     * <td>OCR auto has failed.</td>
     * </tr>
     * <tr>
     * <td>M_PEND</td>
     * <td>OCR is manual.</td>
     * </tr>
     * <tr>
     * <td>M_DONE</td>
     * <td>OCR manual has completed.</td>
     * </tr>
     * <tr>
     * <td>M_CNCL</td>
     * <td>OCR manual was cancelled.</td>
     * </tr>
     * </table>
     * 
     * @return the receipt OCR status.
     */
    public String getOcrStatus();

    /**
     * Sets the receipt OCR status.
     * 
     * @param ocrStatus
     *            contains the receipt OCR status.
     */
    public void setOcrStatus(String ocrStatus);

    /**
     * Gets the receipt OCR reject code.
     * 
     * <br>
     * The following values are supported:<br>
     * <table border="1">
     * <tr>
     * <td><b>Value</b></td>
     * <td><b>Meaning</b></td>
     * </tr>
     * <tr>
     * <td>PF</td>
     * <td>Processing has failed.</td>
     * </tr>
     * <tr>
     * <td>NR</td>
     * <td>Not a receipt.</td>
     * </tr>
     * <tr>
     * <td>UR</td>
     * <td>Unreadable receipt.</td>
     * </tr>
     * <tr>
     * <td>MR</td>
     * <td>Multiple receipts.</td>
     * </tr>
     * <tr>
     * <td>AC</td>
     * <td>OCR auto was cancelled.</td>
     * </tr>
     * <tr>
     * <td>MC</td>
     * <td>OCR manual was cancelled.</td>
     * </tr>
     * </table>
     * 
     * @return the receipt OCR reject code.
     */
    public String getOcrRejectCode();

    /**
     * Sets the receipt OCR reject code.
     * 
     * @param ocrRejectCode
     *            contains the receipt OCR reject code.
     */
    public void setOcrRejectCode(String ocrRejectCode);

    /**
     * Will update persistence with the current receipt values.
     */
    public boolean update();

    /**
     * Will delete the receipt.
     * 
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean delete();

}
