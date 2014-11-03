//
//  ViewController.h
//  PDFViewController
//
//  Created by Sally Yan and Weston Winn on 4/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReceiptUploaderDelegate.h"
#import "ReceiptUploader.h"
#import "MobileTableViewController.h"
#import "MobileActionSheet.h"
#import "ReceiptEditorVC.h"
#import "QuickExpensesReceiptStoreVC.h"
#import "Receipt.h"
#import "ReceiptCache.h"
#import "ApplicationLock.h"

@interface PDFViewController : MobileViewController<ReceiptUploaderDelegate, UIActionSheetDelegate>
{
}

//@property (nonatomic, weak) id<ReceiptEditorDelegate>   delegate;

@property (strong,nonatomic) IBOutlet UIWebView *webView;
@property (nonatomic, strong) NSURL *url;
@property BOOL isAttachedReceipt;
@property BOOL isLoggedIn;
@property BOOL isUploaded;
- (void)showActionSheet;

@end
