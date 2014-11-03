//
//  ReceiptEditorVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MobileViewController.h"
#import "Receipt.h"
#import "ReceiptEditorDelegate.h"
#import "ReceiptDownloaderDelegate.h"
#import "ReceiptUploaderDelegate.h"
#import "ReceiptUploader.h"
#import "UnifiedImagePicker.h"
#import "ReceiptCameraOverlayVC.h"
#import "EntityUploadQueueItem.h"
#import "ReceiptEditorDataModel.h"
#import "ReceiptStoreUploadHelper.h"

@interface ReceiptEditorVC : MobileViewController <UIScrollViewDelegate,
        UIActionSheetDelegate,
        UnifiedImagePickerDelegate,
        ReceiptCameraOverlayVCDelegate,
        UIAlertViewDelegate,
        ReceiptDownloaderDelegate,
        ReceiptUploaderDelegate,
        UIPopoverPresentationControllerDelegate> {
}

@property BOOL            mustQueue;
@property BOOL            canDelete;   // Detach receipt from entry/report
@property BOOL            canUseReceiptStore;
@property BOOL            ignoreCache;
@property BOOL            canUpdate;   // Replace existing or Add new
@property BOOL            canAppend;   // Display "Add Another" button.  Append to existing receipt image id; if no image id, then receiptUpdated is called on delegate.
@property BOOL            supportsOffline;

@property BOOL appendSelected;  // Whether the user selected append, instead of update existing or add new
@property BOOL showActionSheet;  // MIB - show action sheet for first time


@property (nonatomic, strong) IBOutlet UIImageView      *receiptImgView;
@property (nonatomic, strong) IBOutlet UIScrollView     *scrollView;
@property (nonatomic, strong) IBOutlet UIToolbar        *tbar;

@property (nonatomic, strong) UIWebView                 *pdfWebView;
@property (nonatomic, strong) UIActionSheet             *receiptActionSheet;
@property (nonatomic, strong) ReceiptUploader           *uploader;
@property (nonatomic, strong) ReceiptCameraOverlayVC    *cameraOverlayVC;
@property (nonatomic, strong) Receipt                   *receipt;

@property (nonatomic, strong) ReceiptEditorDataModel*   receiptEditorDataModel;
@property (nonatomic, weak) id<ReceiptEditorDelegate>   delegate;

-(void)setSeedData:(Receipt*) rcpt;
-(void) queueReceipt:(UIImage*)receiptImage;

+(NSData *) receiptImageDataFromReceiptImage:(UIImage*)receiptImage;
+(EntityUploadQueueItem *) queueReceiptImage:(UIImage*)receiptImage date:(NSDate*)creationDate;

@end
