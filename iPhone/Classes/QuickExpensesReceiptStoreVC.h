//
//  QuickExpensesReceiptStoreVC.h
//  ConcurMobile
//
//  Created by charlottef on 3/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MobileTableViewController.h"
#import "ReceiptDownloaderDelegate.h"
#import "ReceiptStoreDetailViewController.h" // Defines ReceiptDownloaderDelegate
#import "ReceiptUploaderDelegate.h"

@interface QuickExpensesReceiptStoreVC : MobileTableViewController <NSFetchedResultsControllerDelegate, UIActionSheetDelegate, ReceiptDownloaderDelegate, ReceiptUploaderDelegate, UploadBannerDelegate, UnifiedImagePickerDelegate>
{
}

@property (strong, nonatomic) ReportData    *reportToWhichToAddExpenses;
@property (nonatomic, assign) BOOL          requireRefresh;

@property (nonatomic, strong) NSString      *currentAuthRefNo; // Current AuthRefNo from push nofication

-(void) setSeedDataAndShowReceiptsInitially:(BOOL)showReceiptsInitially allowSegmentSwitch:(BOOL)allowSegmentSwitch allowListEdit:(BOOL)allowListEdit;
-(void) segmentSelected:(id)sender;
-(BOOL) refreshView:(UIRefreshControl*) refresh;
-(void) reloadExpensesWithLoadingView:(BOOL)showloadingView;

@end
