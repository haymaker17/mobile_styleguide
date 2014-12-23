//
//  UploadQueueViewController.m
//  ConcurMobile
//
//  Created by Shifan Wu on 10/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "UploadQueueViewController.h"
#import "DataConstants.h"
#import "UploadQueue.h"
#import "QEFormVC.h"
#import "ReceiptStoreDetailViewController.h"
#import "QuickExpensesReceiptStoreVC.h"

@interface UploadQueueViewController ()

@end

@implementation UploadQueueViewController

@synthesize ds, tableList;
@synthesize delegate = _delegate;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

-(NSString *)getViewIDKey
{
	return UPLOAD_QUEUE_VIEW_CONTROLLER;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    ds = [[UploadQueueDS alloc]init];
    [ds setSeedData:self.tableList withDelegate:self];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    NSIndexPath *indexPath = [self.tableList indexPathForSelectedRow];
    if(indexPath != nil)
        [self.tableList deselectRowAtIndexPath:indexPath animated:NO];

    [self configureToolbar];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if (![self.navigationController.toolbar isHidden])
    {
        // check if offline
        if(![ExSystem connectedToNetwork])
            [self makeOfflineBar];
    }
    
    if ([self isViewLoaded])
        [self.tableList reloadData];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    [self.delegate didDismissUploadQueueVC];
}

- (void)viewDidUnload
{
    [self setTableList:nil];
    [super viewDidUnload];
}



-(void)startUpload
{
    if (![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Currently Offline"]
                              message:[Localizer getLocalizedText:@"You must be online before you can upload your items"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                              otherButtonTitles:nil];
        [alert show];
        
//        NSDictionary *dict = [[NSDictionary alloc] initWithObjectsAndKeys:@"", @"Time", @"horrible view", @"Uploaded Count", @"", @"Uploaded Image Count", nil];
//        [Flurry logEvent:@"Offline: Upload" withParameters:dict];
    }
    else
    {
        if ([[UploadQueue sharedInstance] visibleQueuedItemCount] > 0)
        {
            [[UploadQueue sharedInstance] startUpload];
            [UploadQueue sharedInstance].uploadQueueVCDelegatedelegate = self;
        }
    }
}

-(void) closeMe:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Toolbar Methods
-(void) configureToolbar
{
    UIBarButtonItem *btnUpload = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Upload"] style:UIBarButtonItemStyleBordered target:self action:@selector(startUpload)];
    self.title = [Localizer getLocalizedText:@"Upload Queue"];
    self.navigationItem.rightBarButtonItem = btnUpload;
}

#pragma mark - UploadQueueDSDelegate Methods
-(void) didSelectMobileEntry:(EntityMobileEntry*)mobileEntry
{
    QEFormVC *formVC = [[QEFormVC alloc] initWithEntryOrNil:mobileEntry];
    [self.navigationController pushViewController:formVC animated:YES];
}

-(void) didSelectReceiptWithId:(NSString*)receiptId isPdfReceipt:(BOOL)isPdfReceipt
{
    ReceiptStoreDetailViewController *receiptDetailVC = [ReceiptStoreDetailViewController receiptStoreDetailViewControllerForLocalReceiptId:receiptId];
	// MOB-21462: for display pdf receipt in upload queue
    receiptDetailVC.isPDF = isPdfReceipt;
    [self.navigationController pushViewController:receiptDetailVC animated:YES];
}

#pragma mark - UploadQueueDelegate methods
-(void) showNoDataView
{
    [self showNoDataView:self];
    self.navigationItem.rightBarButtonItem = nil;
}

#pragma mark NoDataViewDelegate method
- (BOOL)adjustNoDataView:(NoDataMasterView*) negView
{
    // Return whether to hide toolbar
    return YES;
}

-(void) actionOnNoData:(id)sender
{
}

-(BOOL)canShowActionOnNoData
{
    return YES;
}

- (NSString*) buttonTitleForNoDataView
{
    return @"";
}

- (NSString*) titleForNoDataView
{
    return ([Localizer getLocalizedText:@"No items to upload"]);
}

-(BOOL) canShowOfflineTitleForNoDataView
{
    return NO;
}

- (NSString *)instructionForNoDataView
{
    return @"";
}

#pragma mark - UploadQueueVCDelegate method
-(void) didDismissUploadQueueVC
{
    [self navigateView:YES];
}

-(void)navigateView:(BOOL)refresh
{
    QuickExpensesReceiptStoreVC *nextController = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    [nextController setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:YES allowListEdit:YES];
    
    [nextController reloadExpensesWithLoadingView:NO];
}


@end
