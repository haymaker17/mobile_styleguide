//
//  ViewController.m
//  PDFViewController
//
//  Created by Sally Yan and Weston Winn 4/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "PDFViewController.h"

#define kBTN_UPLOAD 0
#define kBTN_UPLOAD_TO_RECEIPSTORE 1

@interface PDFViewController ()
{
    ConcurMobileAppDelegate *appDelegate;
    QuickExpensesReceiptStoreVC *qeReceiptStoreVC;
}

@property (nonatomic, strong) NSMutableArray *actionSheetItems;
@property (nonatomic, strong) ReceiptUploader *uploader;
@property (nonatomic, strong) ReceiptEditorVC *receiptEditorVC;
@property BOOL isAttachToExpense;
@property BOOL isQuickExpenseOpened;
@property BOOL isReceiptEditorOpened;
@property BOOL isReportDetailVCOpened;

- (void)handleDocumentOpenURL:(NSURL *)urlResouce;
- (IBAction)uploadButtonClicked:(id)sender;

@end

@implementation PDFViewController


- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = [Localizer getLocalizedText:@"PDF Receipt"];
    UIBarButtonItem *btnUpload = [[UIBarButtonItem alloc] initWithTitle:@"Upload" style:UIBarButtonItemStylePlain target:self action:@selector(uploadButtonClicked:)];
    self.navigationItem.rightBarButtonItem = btnUpload;
    [self handleDocumentOpenURL:self.url];
}

- (void) viewWillAppear:(BOOL)animated
{
    if (self.isUploaded)
        self.navigationItem.rightBarButtonItem = nil;
}

- (void) viewDidAppear:(BOOL)animated
{
    self.isLoggedIn = [[ApplicationLock sharedInstance] isLoggedIn];
    if( self.isLoggedIn && !self.isUploaded)
        [self showActionSheet];
}


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - ActionSheet Methods
- (void)showActionSheet
{
    MobileActionSheet *actionSheet = [[MobileActionSheet alloc] initWithTitle:@"Would you like to upload this receipt now?"
                                                                     delegate:self
                                                            cancelButtonTitle:nil
                                                       destructiveButtonTitle:nil
                                                            otherButtonTitles:nil, nil];
    if (!self.isAttachedReceipt)
        self.actionSheetItems = [@[@"Upload", @"Cancel"] mutableCopy];
    else
    {
        appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        if (appDelegate != nil && appDelegate.qeFormVC.receipt.receiptId != nil)
            self.actionSheetItems = [@[@"Upload & Replace", @"Upload to Receipt Store", @"Cancel"] mutableCopy];
        else
            self.actionSheetItems = [@[@"Attach & Upload", @"Upload to Receipt Store", @"Cancel"] mutableCopy];
    }
    
    for (NSString *unlocalizedItemName in self.actionSheetItems)
        [actionSheet addButtonWithTitle:unlocalizedItemName];
    
    actionSheet.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
    [actionSheet showInView:self.webView];
}


#pragma mark UIActionSheet Delegate methods
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (!self.isAttachedReceipt)    // only upload the receipt to receipt store
    {
        if (buttonIndex == kBTN_UPLOAD)
            [self uploadButtonClicked:self];
    }
    else                        // the quick expense is opened and the pdf is probably attatched to this expense item
    {
        if (buttonIndex == kBTN_UPLOAD)
        {
            self.isAttachToExpense = YES;
            [self attachAndUploadClicked];
        }
        else if (buttonIndex == kBTN_UPLOAD_TO_RECEIPSTORE)
        {
            self.isAttachToExpense = NO;
            [self uploadButtonClicked:self];
        }
    }
}


#pragma mark - Local Methods

#pragma mark Handle Document URL
- (void)handleDocumentOpenURL:(NSURL *)urlResouce
{
    NSURLRequest *requestObj = [NSURLRequest requestWithURL:urlResouce];
    [self.webView setUserInteractionEnabled:YES];
    [self.webView loadRequest:requestObj];
}


#pragma mark Button Methods
- (IBAction)uploadButtonClicked:(id)sender
{
    NSDictionary *dict = @{@"Added Using": @"Open PDF", @"Added To": @"Receipt Store"};
    [Flurry logEvent:@"Receipts: Add" withParameters:dict];
    
    NSData *data = [NSData dataWithContentsOfURL:self.url];
    
    if (![ExSystem connectedToNetwork]) {
        [self queuePdfReceipt:data];
    }
    else {
        [self showWaitViewWithProgress:YES withText:[Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"]];
        self.uploader = [[ReceiptUploader alloc] init];
        self.uploader.delegate = self;
        [self.uploader setSeedDataPdf:self withPdf:data];
        [self.uploader startUploadPdf];
    }
}


-(void) attachAndUploadClicked
{
    NSDictionary *dict = @{@"Added Using": @"Open PDF", @"Added To": @"Expense or Report"};
    [Flurry logEvent:@"Receipts: Add" withParameters:dict];
    
    [self showWaitViewWithProgress:YES withText:[Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"]];
    NSData *data = [NSData dataWithContentsOfURL:self.url];
    
    self.uploader = [[ReceiptUploader alloc] init];
    self.uploader.delegate = self;
    appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    
    // only the QuickExpense is opened
    if (appDelegate != nil && appDelegate.qeFormVC != nil && appDelegate.receiptVC == nil)
    {
        self.isQuickExpenseOpened = YES;
        BOOL isOnline = [ExSystem connectedToNetwork];
        BOOL isNewExpense = [appDelegate.qeFormVC isNewQuickExpense];
        BOOL isExpenseQueued = [appDelegate.qeFormVC isQueuedQuickExpense];
        
        BOOL allowReceiptEdits = (isOnline || isNewExpense || isExpenseQueued);
        
        BOOL willQueueExpense = (isExpenseQueued || (!isOnline && isNewExpense));
        
        [appDelegate.qeFormVC showReceiptViewerAndAllowEdits:allowReceiptEdits excludeReceiptStoreOption:willQueueExpense];
        
        if ([self.navigationController.topViewController isKindOfClass:[ReceiptEditorVC class]])
            self.receiptEditorVC = (ReceiptEditorVC*) self.navigationController.topViewController;
    }
 
    else if (appDelegate.receiptVC != nil)     // in receipt viewer
    {
        self.isReceiptEditorOpened = YES;
        self.receiptEditorVC = appDelegate.receiptVC;
    }
    
    //    if (self.receiptEditorVC.receipt.receiptId != nil && self.receiptEditorVC.receipt.receiptImg != nil)
    self.receiptEditorVC.receipt.receiptImg = nil;
    self.receiptEditorVC.receipt.pdfData= data;
    self.receiptEditorVC.receipt.dataType = @"pdf";
    [self.uploader setSeedDataPdf:self withPdf:self.receiptEditorVC.receipt.pdfData];
    [self.uploader startUploadPdf];
}


#pragma mark - ReceiptUploaderDelegate Methods
-(void) receiptUploadSucceeded:(NSString*) receiptPdfId
{
    if ([self isViewLoaded])
        [ self hideWaitView];
    if (!self.isAttachedReceipt || (self.isAttachedReceipt && !self.isAttachToExpense) )
    {
        NSArray *viewControllers = self.navigationController.viewControllers;
        if (viewControllers != nil && [viewControllers count] > 1)
        {
            UIViewController *vc = viewControllers[([viewControllers count] - 2)];
            if ([vc isKindOfClass:[QuickExpensesReceiptStoreVC class]])
            {
                qeReceiptStoreVC = (QuickExpensesReceiptStoreVC*)vc;
                if ( [qeReceiptStoreVC respondsToSelector:@selector(refreshView:)])
                {
                    UIRefreshControl *refresh = nil;
                    [qeReceiptStoreVC performSelector:@selector(refreshView:) withObject:refresh afterDelay:0.5f];
                }
            }
        }
        
        [self.navigationController popViewControllerAnimated:YES];
    }
    else
    {
        NSString *receiptId = [self.receiptEditorVC.receipt fileCacheKey];
        if ([receiptId length])
            [[ReceiptCache sharedInstance] deleteReceiptsMatchingId:[self.receiptEditorVC.receipt fileCacheKey]];
        
        self.receiptEditorVC.receipt.receiptId = receiptPdfId;
        [self.receiptEditorVC.delegate receiptUpdated:self.receiptEditorVC.receipt useV2Endpoint:true];
        
        if (self.isQuickExpenseOpened && appDelegate.qeFormVC != nil)
            [self.navigationController popToViewController:appDelegate.qeFormVC animated:YES];
        else if (self.isReceiptEditorOpened && appDelegate.receiptVC != nil)
            [self.navigationController popToViewController:appDelegate.receiptVC animated:YES];

    }
    
    self.isUploaded = YES;
}


-(void) failedToPrepareImageData // memory issue
{
    if (self.isViewLoaded)
        [self hideWaitView];
    
    NSDictionary *dict = @{@"Failure": @"Failed to capture or reduce resolution for receipt PDF data"};
    [Flurry logEvent:@"Receipts: Failure" withParameters:dict];
    
    // MOB-9416 handle image conversion failure b/c memory shortage
    NSString *errMsg = [Localizer getLocalizedText:@"Free up memory and retry receipt upload"];
    
    UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload failed"]
                                                        message:errMsg
                                                       delegate:nil
                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                              otherButtonTitles:nil];
    [alert show];
}


-(void) failedToUploadImage:(NSString*) errorStatus // e.g. Imaging not configured
{
    if (self.isViewLoaded)
        [self hideWaitView];
    
    if (![self handleImageConfigError:errorStatus])
    {
        NSString *errMsg = errorStatus != nil? errorStatus :
        [Localizer getLocalizedText:@"ReceiptUploadFailMsg"];
        
        UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload failed"]
                                                            message:errMsg
                                                           delegate:nil
                                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                                  otherButtonTitles:nil];
        [alert show];
        
        // If offline is supported, then queue the receipt that failed to upload
        [[MCLogging getInstance] log:@"Receipt upload failed and queue the receipt for upload later" Level:MC_LOG_DEBU];
        // MOB-21462: need to save the pdf data to local
        [self queuePdfReceipt:self.uploader.pdfData];
    }
    else
        // Receipt Configuration error.
        [[MCLogging getInstance] log:@"Receipt upload failed because of receipt configuration error" Level:MC_LOG_DEBU];
}


#pragma - receipt error handling
-(BOOL) handleImageConfigError:(NSString*) errCode
{
    if ([@"Imaging Configuration Not Available." isEqualToString: errCode])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Cannot access receipt"]
                              message:[Localizer getLocalizedText:@"ERROR_BAD_CONFIG_MSG"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
        [alert show];
        return TRUE;
    }
    return FALSE;
}


#pragma mark - Queue Receipt Methods
-(void) queuePdfReceipt:(NSData*)pdfData
{
	// MOB-21462: queue Pdf receipt 
    [ReceiptEditorVC queuePdfReceipt:pdfData date:[NSDate date]];
    [self receiptQueued];
}


-(void) receiptQueued
{
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"Receipt Queued"]
                          message:[Localizer getLocalizedText:@"Your receipt has been queued"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                          otherButtonTitles:nil];
    [alert show];
}

@end
