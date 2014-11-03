//
//  ReceiptStoreUploadHelper.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 3/14/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReceiptCameraOverlayVC.h"
#import "ReceiptUploader.h"
#import "Receipt.h"
#import "MobileTableViewController.h"
#import "ReceiptCameraOverlayView.h"
#import "CCImagePickerViewController.h"

// This is a lighter version of 
@interface ReceiptStoreUploadHelper : NSObject< ReceiptUploaderDelegate, ReceiptCameraOverlayVCDelegate> {    
}

@property (nonatomic, strong) ReceiptCameraOverlayVC      *cameraOverlayVC;
@property (nonatomic, strong) ReceiptUploader               *uploader;
@property (nonatomic, strong) MobileTableViewController     *vc;
@property (nonatomic, strong) CCImagePickerViewController  *picker;

@property (nonatomic, assign) BOOL openReceiptListWhenFinished;
@property (nonatomic, assign) BOOL isFromReceiptStore;

- (void)startCamera:(UIBarButtonItem*)button;
- (void)showQuickExpense: (UIImage*)image;
@end
