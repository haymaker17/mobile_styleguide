/**
 * 
 */
package com.concur.mobile.core.expense.data;

/**
 * An enumeration describing an action taken on an expense receipt picture.
 * 
 * @author AndrewK
 */
public enum ReceiptPictureSaveAction {
    NO_ACTION,              // Receipt picture not changed.
    DOWNLOAD_PICTURE,       // Receipt picture was downloaded.
    TAKE_PICTURE,           // Receipt picture was taken with device.
    CHOOSE_PICTURE,         // Receipt picture was selected on device.
    CHOOSE_PICTURE_CLOUD,	// Receipt picture is selected from the cloud.
    APPEND,					// Receipt picture was appended to.
    CLEAR_PICTURE,          // Receipt picture was cleared.
    CANCEL,                 // Receipt menu action was canceled. Has no meaning outside of
    // of the 'ExpenseOutOfPocketEditView' class!
    VIEW,                    // Option to view receipt image.
    CHOOSE_PDF              // Receipt picture is selected from PDF's on device
}
