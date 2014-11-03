//
//  ReceiptStoreListView.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReceiptStoreListView.h"
#import "ReceiptStoreListData.h"
#import "ReceiptStoreListCell.h"
#import "DeleteReceipt.h"
#import "RequestController.h"
#import "ReceiptStoreDetailViewController.h"
#import "ReceiptImageMetaData.h"
#import "MobileActionSheet.h"
#import "MobileAlertView.h"
#import "UploadReceiptData.h"
#import "ExSystem.h"
#import "UploadQueue.h"
#import "UploadQueueViewController.h"
#import "ReceiptCache.h"
#import "ReceiptDownloader.h"
#import "ReceiptEditorVC.h"

#import "PostMsgInfo.h"
#import "UploadableReceipt.h"
#import "UploadQueueItemManager.h"

#define kTransitionDuration	0.75
#define kActionViewAddReceipt 110
#define RECEIPT_STORE_ALERT_ACTION 120
#define RECEIPT_STORE_URL_EXPIRATION_INTERVAL 31*60

@interface ReceiptStoreListView (Private)
-(void)setTableHeaderView;
-(void)prepareReceiptGroups;
-(void)showLastUpdatedDate:(NSString*)lastUpdated;
-(void)refreshIfStaleData;
-(ReceiptStoreReceipt*) getReceiptInfo:(NSIndexPath *)indexPath;
-(void) showPlaceHolderImageInCell:(ReceiptStoreListCell*)cell;
@end

@implementation ReceiptStoreListView
@synthesize tblDataSource,receiptsTableView,delegate,receiptIndexToDelete,flipView,uploadButton,disableEditActions,groupedReceiptsLookup,sectionsArray,lastUpdatedTimestamp,needsDataRefetch,canRefresh, isQuickMode, bannerAdjusted,tableViewY,flipViewY;

-(NSString *)getViewIDKey
{
	return RECEIPT_STORE_VIEWER;
}


-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:DELETE_RECEIPT])
	{
        canRefresh = FALSE;
		DeleteReceipt *deleteReceiptResponse = (DeleteReceipt*)msg.responder;
		if (msg.errBody == nil && [deleteReceiptResponse.status isEqualToString:@"SUCCESS"])
		{
			//Remove the entry from tblDataSource && from cache if cached
            if (groupedReceiptsLookup != nil) {
                self.groupedReceiptsLookup = nil;
            }
            
            if (sectionsArray != nil) {
                self.sectionsArray = nil;
            }
            
            // MOB-9407 recalculate receipt groups
			[tblDataSource removeObjectAtIndex:receiptIndexToDelete];   
            self.receiptIndexToDelete = -1;
            [self prepareReceiptGroups];
            [[ExReceiptManager sharedInstance] removeFromReceiptStoreCache:deleteReceiptResponse.receiptImageId];

            //MOB-11172 need to refresh receipt list in cache (therefore RespondTo parameter is nil)
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"N", @"FILTER_MOBILE_EXPENSE", nil];
            [[ExSystem sharedInstance].msgControl createMsg:RECEIPT_STORE_RECEIPTS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES	RespondTo:nil];
		}
		
		if ([self isViewLoaded])
		{
			[receiptsTableView setHidden:NO];
			
            [self hideNoDataView];
			[self hideWaitView];
            
			if ([tblDataSource count] == 0) {
                [self showNoDataView:self];
			}
			else {
				[receiptsTableView reloadData];	
			}
		}
	}
	else if ([msg.idKey isEqualToString:RECEIPT_STORE_RECEIPTS])
	{
		ReceiptStoreListData *receiptsData = (ReceiptStoreListData*)msg.responder;
		
		if ([self isViewLoaded]) 
		{
			[receiptsTableView setHidden:NO];
			[self hideNoDataView];
			[self hideLoadingView];
            
            self.lastUpdatedTimestamp = msg.dateOfData;
            NSString *lastUpdated = [CCDateUtilities formatDateToEEEMonthDayYearTimeFromNSDate:msg.dateOfData];
            lastUpdated = [NSString stringWithFormat:[Localizer getLocalizedText:@"Last updated"], lastUpdated];
            [self showLastUpdatedDate:lastUpdated];
            
            needsDataRefetch = NO;
            [self performSelector:@selector(refreshIfStaleData) withObject:nil afterDelay:RECEIPT_STORE_URL_EXPIRATION_INTERVAL];
		}
		
		if (msg.errBody == nil && [receiptsData.status isEqualToString:@"SUCCESS"])
		{
            [[ReceiptCache sharedInstance] purgeUnusedReceiptImagesFromCache:receiptsData.receiptObjects];

            if (groupedReceiptsLookup != nil) {
                self.groupedReceiptsLookup = nil;
            }
            
            if (sectionsArray != nil) {
                self.sectionsArray = nil;
            }
            
			[self orderReceiptsData:receiptsData.receiptObjects];
            // MOB-9407 recalculate receipt groups
            [self prepareReceiptGroups];
            
			if ([self isViewLoaded]) 
			{
				if ([receiptsData.receiptObjects count] == 0)
                {
                    [self showNoDataView:self];
                    
                    if (bannerAdjusted)
                    {
                        self.bannerAdjusted = NO;       //re-insert upload banner over the no data view
                        [self adjustViewForUploadBanner];
                    }
				}
				else
                {
					[receiptsTableView reloadData];	
				}
			}
		}
		else
		{
            if (![ExReceiptManager handleImageConfigError: msg.errBody])
            {
                UIAlertView *alert = [[MobileAlertView alloc] 
                                      initWithTitle:[NSString stringWithFormat:@"Error %@", msg.errCode]
                                      message:msg.errBody
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                      otherButtonTitles:nil];
                
                [alert show];
            }
            
			if ([self isViewLoaded]) 
			{
				if ([receiptsData.receiptObjects count] == 0)
				{
                    //NSString* msg = [Localizer getLocalizedText:@"ErrorReceiptsFetch"];
                    [self showNoDataView:self];
				}
			}
		}
        
	}
	else if([msg.idKey isEqualToString:UPLOAD_IMAGE_DATA])
	{
		UploadReceiptData *receipt = (UploadReceiptData*)msg.responder;
		canRefresh = FALSE;
        
		if ([self isViewLoaded]) 
		{
			[self hideWaitView];
		}
		
		if (! (msg.errBody == nil && [receipt.returnStatus isEqualToString:@"SUCCESS"]))
		{
            if (![ExReceiptManager handleImageConfigError: msg.errBody])
            {
                UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload failed"] 
                                                                    message:[Localizer getLocalizedText:@"ReceiptUploadFailMsg"] 
                                                                   delegate:self 
                                                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
                                                          otherButtonTitles:nil];
                [alert show];
            }
		}
		else
		{
			[self loadReceipts];
		}

	}
}

// Mob-5295
-(void)refreshIfStaleData
{
    if ([self isViewLoaded] && !canRefresh) 
    {
        if ([ExSystem connectedToNetwork])
        {
            if (lastUpdatedTimestamp == nil || needsDataRefetch)
            {
                [self loadReceipts];
                return;
            }
            
            NSDate *present = [NSDate date];
            NSTimeInterval elapsedTimeInterval = [present timeIntervalSinceDate:lastUpdatedTimestamp];
            
            if (elapsedTimeInterval >= RECEIPT_STORE_URL_EXPIRATION_INTERVAL) {
                [self loadReceipts];
                return;
            }
        }
        needsDataRefetch = NO;
        [self performSelector:@selector(refreshIfStaleData) withObject:nil afterDelay:RECEIPT_STORE_URL_EXPIRATION_INTERVAL];
    }
}

-(void)loadReceipts
{
    if ([self isViewLoaded]) 
    {
         [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Receipts"]];
    }
    
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"N", @"FILTER_MOBILE_EXPENSE", nil];
    
    if ([ExSystem connectedToNetwork])
        [[ExSystem sharedInstance].msgControl createMsg:RECEIPT_STORE_RECEIPTS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES	RespondTo:self];
    else
        [[ExSystem sharedInstance].msgControl createMsg:RECEIPT_STORE_RECEIPTS CacheOnly:@"YES" ParameterBag:pBag SkipCache:NO	RespondTo:self];
}


-(void)orderReceiptsData:(NSMutableArray*)data
{
	if (data != nil)
	{        
		NSSortDescriptor *dateDescriptor = [[NSSortDescriptor alloc] initWithKey:@"imageDate" ascending:NO];
		NSArray *sortDescriptors = @[dateDescriptor];
		NSArray *sortedArray = [data sortedArrayUsingDescriptors:sortDescriptors];
		if (sortedArray != nil) 
		{
			if (self.tblDataSource != nil)
			{
				self.tblDataSource = nil;
			} 
			self.tblDataSource = [[NSMutableArray alloc] initWithArray:sortedArray];
            
		}
	}
}


-(void)doneAction
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark -
#pragma mark Application changed notifications
- (void)applicationDidEnterBackground
{
    [super applicationDidEnterBackground];
    
    self.needsDataRefetch = YES;
}


#pragma mark -
#pragma mark Help View methods

- (IBAction)flipAction:(id)sender
{
	[UIView beginAnimations:nil context:NULL];
	[UIView setAnimationDuration:kTransitionDuration];
	
	[UIView setAnimationTransition:([self.receiptsTableView superview] ?
									UIViewAnimationTransitionFlipFromLeft : UIViewAnimationTransitionFlipFromRight)
						   forView:self.view cache:YES];
	if ([flipView superview])
	{
		[self.flipView removeFromSuperview];
		[self.view addSubview:receiptsTableView];
	}
	else
	{
		[self.receiptsTableView removeFromSuperview];
		[self.view addSubview:flipView];
	}
	
	[UIView commitAnimations];
}


#pragma mark -
#pragma mark Upload Receipt methods

-(void)prepareUploadView
{ 
    self.uploadButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(uploadAction:)];
	
    self.navigationItem.rightBarButtonItem = uploadButton;
}


- (IBAction)uploadAction:(id)sender
{	
	if (self.actionPopOver != nil && self.actionPopOver.visible)
        [self.actionPopOver dismissWithClickedButtonIndex:-2 animated:NO];
    
    self.actionPopOver = nil;
	
    if ([UIDevice isPad]) {
        if (self.pickerPopOver != nil && pickerPopOver.popoverVisible)
            [pickerPopOver dismissPopoverAnimated:YES];
        
        self.pickerPopOver = nil;
    }
    
    UIActionSheet* receiptActions = nil;
	if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
	{
		receiptActions = [[MobileActionSheet alloc] initWithTitle:nil
														 delegate:self 
												cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
										   destructiveButtonTitle:nil
												otherButtonTitles:[Localizer getLocalizedText:@"Camera"], 
						  [Localizer getLocalizedText:@"Photo Album"], nil];
	}
	else 
	{
		receiptActions = [[MobileActionSheet alloc] initWithTitle:nil
														 delegate:self 
												cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
										   destructiveButtonTitle:nil
												otherButtonTitles:[Localizer getLocalizedText:@"Photo Album"], nil];
	}
    
    if ([UIDevice isPad])
        self.actionPopOver = receiptActions;
	
    receiptActions.tag = kActionViewAddReceipt;
	
	if ([UIDevice isPad] && uploadButton != nil)
	{
		[receiptActions showFromBarButtonItem:uploadButton animated:YES];
	}
	else
	{
        receiptActions.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
        // MOB-8879 showFromToolbar make toolbar black coming fr home.  showInView:self.view causes last half to be blocked from click.  Show from tableList fixed both issues on iPhone 4.3
        [receiptActions showInView:self.receiptsTableView];
//        if(self.navigationController.toolbar != nil) // MOB-5873
//        {
//            [receiptActions showFromToolbar:self.navigationController.toolbar];   
//        }
//        else
//        {
//            [receiptActions showInView:self.view];
//        }
	}

}


-(void) buttonCameraPressed
{	
	if (![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
		return;
	
    NSDictionary *dict = @{@"Added Using": @"Camera",@"Added To": @"Receipt Store"};
    [Flurry logEvent:@"Receipts: Add" withParameters:dict];

	UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker];
	imgPicker.sourceType = UIImagePickerControllerSourceTypeCamera;
	[UnifiedImagePicker sharedInstance].delegate = self;
	
    if ([UIDevice isPad])
    {
        if(pickerPopOver != nil)
            [pickerPopOver dismissPopoverAnimated:YES];
        
		self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:imgPicker];  
		[pickerPopOver presentPopoverFromBarButtonItem:uploadButton permittedArrowDirections:UIPopoverArrowDirectionDown|UIPopoverArrowDirectionUp animated:YES];
    }
    else
    {
        [self presentViewController:imgPicker animated:YES completion:nil];			
    }
}


-(void)buttonAlbumPressed
{	
	if([UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypePhotoLibrary])
	{
        NSDictionary *dict = @{@"Added Using": @"Album", @"Added To": @"Receipt Store"};
        [Flurry logEvent:@"Receipts: Add" withParameters:dict];

		UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker]; 
		imgPicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
		[UnifiedImagePicker sharedInstance].delegate = self;
		
		if ([UIDevice isPad])
		{
			if(pickerPopOver != nil)
				[pickerPopOver dismissPopoverAnimated:YES];
			
		self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:imgPicker];  
		[pickerPopOver presentPopoverFromBarButtonItem:uploadButton permittedArrowDirections:UIPopoverArrowDirectionDown|UIPopoverArrowDirectionUp animated:YES];
		}
		else
		{
			[self presentViewController:imgPicker animated:YES completion:nil];			
		}
	}
}

#pragma mark - Queue stuff

-(void) showQueueAlert
{
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"Receipt Queued"]
                          message:[Localizer getLocalizedText:@"Your receipt has been queued"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                          otherButtonTitles:nil];
    [alert show];
    return;
}


#pragma mark - UnifiedImagePickerDelegate methods

-(void)unifiedImagePickerSelectedImage:(UIImage*)image
{	
	if ([UIDevice isPad])
	{
		if (self.pickerPopOver != nil)
		{
			[self.pickerPopOver dismissPopoverAnimated:YES];
			self.pickerPopOver = nil;
		}
        
        // MOB-8441 Do not dismiss the image picker modally - it is always presented within popover for iPad
	}
	else
		[[[UnifiedImagePicker sharedInstance] imagePicker] dismissViewControllerAnimated:YES completion:nil];
    
    
    if (![ExSystem connectedToNetwork])
    {
        [ReceiptEditorVC queueReceiptImage:image date:[NSDate date]];

        //show banner when add first receipt offline from receipt store
        int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
        if (itemNum > 0)
        {
            [self makeUploadView];
            [self adjustViewForUploadBanner];
        }
        [self showQueueAlert];
    }
    else
    {
        canRefresh = TRUE;
        [self showWaitViewWithProgress:YES withText:[Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"]];
        
        [[ExReceiptManager sharedInstance] configureReceiptManagerForDelegate:self andReportEntry:nil andReporData:nil andExpenseEntry:nil andRole:nil];
        [[ExReceiptManager sharedInstance] uploadReceipt:image];
    }
}


#pragma mark -
#pragma mark UIActionSheetDelegate method
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if	(actionSheet.tag == kActionViewAddReceipt)
	{
		BOOL hasCamera = [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera];
		int offset = hasCamera? 1 : 0;
		if (hasCamera && (buttonIndex ==(-1+offset)))
		{
			[self buttonCameraPressed];
		} 
		else if (buttonIndex == (0+ offset))
		{
			[self buttonAlbumPressed];
		} 
		else 
		{
		}
    }
    
    [super actionSheet:actionSheet clickedButtonAtIndex:buttonIndex];
}


#pragma mark -
#pragma mark View lifecycle
- (void)viewDidLoad 
{
    [super viewDidLoad];
    
    NSArray *viewList = [[self navigationController] viewControllers];
    int count = [viewList count];
    NSString *camefromvc = @"Select Receipt";
    if(count>1)
    {
        UINavigationController *navStack = viewList[count-2];
        // TODO: Is this Safe to comment out or find another way
//        if([navStack isKindOfClass:[RootViewController class]])
//        {
//           camefromvc = @"Home";
//        }
    }

    [Flurry logEvent:@"Receipt Store: Viewed" withParameters:@{@"Viewed From": camefromvc}];
    
    if (!disableEditActions) {
        [self prepareUploadView];
    }
    
    if([UIDevice isPad])
    {
        self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
        self.navigationItem.leftBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(doneAction)];
    }
    else
    {
        if (self.delegate != nil) 
        {
            UIBarButtonItem *doneBtn = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_DONE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(doneAction)];
            self.navigationItem.leftBarButtonItem = doneBtn;
        }	
    }
    
    [self.navigationController setToolbarHidden:NO];
    self.navigationItem.title = [Localizer getLocalizedText:@"Receipt Store"];
    [receiptsTableView setHidden:YES];
    [self setTableHeaderView];

    if ([ExSystem connectedToNetwork])
    {
        self.needsDataRefetch = YES;
        [self refreshIfStaleData]; // calls loadReceipts
    }
    else
    {
        self.needsDataRefetch = NO;
        [self loadReceipts]; // loads from cache when not online
        [self makeOfflineBar];
    }
    
    // remember cooradinates before inserts offline banner
    if (flipView != nil)
        self.flipViewY = flipView.frame.origin.y;
    
    if (receiptsTableView != nil)
        self.tableViewY = receiptsTableView.frame.origin.y;
    
    self.bannerAdjusted = NO;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    if (itemNum > 0)
    {
        [self makeUploadView];
        [self adjustViewForUploadBanner];
    }
    else
        [self adjustViewForNoUploadBanner];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
	[self.navigationController setToolbarHidden:NO];
}

-(void) showLastUpdatedDate:(NSString*)lastUpdated
{
    int refreshDateWidth = 280; 
	int refreshDateHeight = 20;
	int numberOfLines = 1;
    
    UIBarButtonItem *btnRefreshDate = nil;
    if (lastUpdated != nil)
    {
        UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, refreshDateWidth, refreshDateHeight)];
        UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, refreshDateWidth, refreshDateHeight)];
        lblText.numberOfLines = numberOfLines;
        lblText.lineBreakMode = NSLineBreakByWordWrapping;
        lblText.textAlignment = NSTextAlignmentLeft;
        lblText.text = lastUpdated;
        if( false == [ExSystem is7Plus] )
        {
            // iOS6 shows a black shadow behind white refresh text
            // iOS7 shows this as white text on white background so leave colors default
            [lblText setBackgroundColor:[UIColor clearColor]];
            [lblText setTextColor:[UIColor whiteColor]];
            [lblText setShadowColor:[UIColor blackColor]];
            [lblText setShadowOffset:CGSizeMake(0, -1)];
        }
        [lblText setFont:[UIFont boldSystemFontOfSize:12.0f]];
        [cv addSubview:lblText];
        
        btnRefreshDate = [[UIBarButtonItem alloc] initWithCustomView:cv];
    }
    
    UIBarButtonItem *refreshBtn = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRefresh target:self action:@selector(loadReceipts)];
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
	NSArray *toolbarItems = nil;
    if (btnRefreshDate != nil) 
    {
        toolbarItems =  @[btnRefreshDate,flexibleSpace,refreshBtn];
    }
    else
    {
        toolbarItems =  @[flexibleSpace,refreshBtn];
    }
    
    [self setToolbarItems:toolbarItems];

}

#pragma mark -
#pragma mark Table view data source
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    if (sectionsArray != nil && [sectionsArray count] > 0) {
        return sectionsArray[section];
    }
    else
    {
        return nil;
    }
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    if (sectionsArray != nil && [sectionsArray count] > 0) {
        return [sectionsArray count];
    }
    else {
        return 1;
    }
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    if (groupedReceiptsLookup != nil && sectionsArray != nil && [sectionsArray count] >0) {
        NSString *key = sectionsArray[section];
        NSMutableArray *rows = (NSMutableArray*)groupedReceiptsLookup[key];
        if (rows != nil && [rows count]>0) 
        {
            return [rows count];
        }
        else
            return 0;

    }
    return [tblDataSource count];
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
	return 80.0;
}

-(ReceiptStoreReceipt*) getReceiptInfo:(NSIndexPath *)indexPath
{
	ReceiptStoreReceipt *receiptInfo = nil;
	
    if (groupedReceiptsLookup != nil && sectionsArray != nil && [sectionsArray count] >0) 
    {
        NSString *key = sectionsArray[[indexPath section]];
        NSMutableArray *rows = (NSMutableArray*)groupedReceiptsLookup[key];
        receiptInfo = (ReceiptStoreReceipt*)rows[[indexPath row]];
    }
    else if ([tblDataSource count] >= [indexPath row]) 
	{
		receiptInfo = (ReceiptStoreReceipt*)tblDataSource[[indexPath row]];
	}
    return receiptInfo;
}
                                            
// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"ReceiptStoreListCell";
	
	ReceiptStoreReceipt *receiptInfo = [self getReceiptInfo:indexPath];
	ReceiptStoreListCell *cell = (ReceiptStoreListCell *)[tableView dequeueReusableCellWithIdentifier:CellIdentifier];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ReceiptStoreListCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ReceiptStoreListCell class]])
				cell = (ReceiptStoreListCell *)oneObject;
	}
    
    // The cell needs to know its receipt id, so that when a receipt image finishes loading, we can tell whether it needs to be displayed in the cell
    cell.receiptId = receiptInfo.receiptImageId;
	
	[cell.imageBackgroundView setIsRoundingDisabled:YES];

    NSString *dataType = nil;
    NSData *cachedImageData = [[ReceiptCache sharedInstance] getThumbNailReceiptForId:receiptInfo.receiptImageId dataType:&dataType];
    if (cachedImageData != nil)
    {
        // Show the image obtained from the cache
        UIImage *cachedThumbNailImage = [UIImage imageWithData:cachedImageData];
        [cell.activityView stopAnimating];
        [cell.activityView setHidden:YES];
        [cell.thumbImageView setImage:cachedThumbNailImage];
    }
    else if (receiptInfo.thumbUrl != nil)
    {
        // Show that we are downloading the image
        [cell.thumbImageView setImage:[UIImage imageNamed:@"receipt_store_loading"]];
        [cell.activityView startAnimating];
        [cell.activityView setHidden:NO];

        BOOL attemptingDownload = [ReceiptDownloader downloadReceiptForId:receiptInfo.receiptImageId dataType:receiptInfo.fileType thumbNail:YES url:receiptInfo.thumbUrl delegate:self];
        if (!attemptingDownload)
            [self showPlaceHolderImageInCell:cell];
    }
    else 
    {
        // Show a placeholder image
        [self showPlaceHolderImageInCell:cell];
    }
    // MOB-17378: the date missing when setting region to UK and 12 hrs
	NSString* imgDate = [CCDateUtilities formatDateToEEEMonthDayYearTime:receiptInfo.imageDate];
	[cell.imageDateLbl setText:imgDate];
	[cell.tagLbl setText:receiptInfo.fileName];	// change to commentTag
    return cell;
}

-(void)setTableHeaderView
{
    UIView * headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 81)];
    [headerView setBackgroundColor:[ExSystem getBaseBackgroundColor]];        
    UILabel *headerLbl = [[UILabel alloc] initWithFrame:CGRectMake(10, 5, 300, 70)];
    [headerLbl setBackgroundColor:[UIColor clearColor]];
    [headerLbl setFont:[UIFont fontWithName:@"Helvetica-neueu" size:14.0]];
    [headerLbl setLineBreakMode:NSLineBreakByWordWrapping];
    [headerLbl setShadowColor:[UIColor whiteColor]];
    [headerLbl setTextColor:[UIColor colorWithRed:(69.0/255.0) green:(69.0/255.0) blue:(69.0/255.0) alpha:1.0f]];
    [headerLbl setTextAlignment:NSTextAlignmentCenter];
    [headerLbl setNumberOfLines:4];

    [headerLbl setAutoresizingMask:UIViewAutoresizingFlexibleWidth];
    
    [headerLbl setText:[Localizer getLocalizedText:@"RECEIPT_STORE_HELP_TEXT"]];
    [headerView addSubview:headerLbl];
    self.receiptsTableView.tableHeaderView = headerView;
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
	ReceiptStoreReceipt *receiptInfo = [self getReceiptInfo:indexPath];
	if (receiptInfo.imageUrl != nil) 
	{
		// Navigation logic may go here. Create and push another view controller.
		ReceiptStoreDetailViewController *rsDetailViewController = [[ReceiptStoreDetailViewController alloc] initWithNibName:@"ReceiptStoreDetailViewController" bundle:nil];
        rsDetailViewController.title = receiptInfo.fileName;
		rsDetailViewController.receiptData = receiptInfo;
		rsDetailViewController.receiptDelegate = self.delegate;
		[self.navigationController pushViewController:rsDetailViewController animated:YES];
	}
}


- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
    // Detemine if it's in editing mode
    if (!disableEditActions) {
      	return UITableViewCellEditingStyleDelete;
    }
    else {
      	return UITableViewCellEditingStyleNone;
    }
}


// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if (editingStyle == UITableViewCellEditingStyleDelete) 
	{
        canRefresh = TRUE;
		[self showWaitViewWithText:[Localizer getLocalizedText:@"Deleting Receipt"]];
		
        if (groupedReceiptsLookup != nil && sectionsArray != nil && [sectionsArray count] >0) 
        {
            receiptIndexToDelete = [indexPath row];
            // Add up all previous section totals
            for (int ix = 0; ix < [indexPath section]; ix++)
            {
                NSString *key = sectionsArray[ix];
                NSMutableArray *rows = (NSMutableArray*)groupedReceiptsLookup[key];
                receiptIndexToDelete += [rows count];
            }
        }
        else
            receiptIndexToDelete = [indexPath row];
        
        ReceiptStoreReceipt *receiptInfo = [self getReceiptInfo:indexPath];
		
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: receiptInfo.receiptImageId, @"ReceiptImageId",
									 [self getViewIDKey], @"TO_VIEW", @"YES", @"SKIP_CACHE", nil];
		[[ExSystem sharedInstance].msgControl createMsg:DELETE_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}  
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    // Relinquish ownership any cached data, images, etc. that aren't in use.
}


- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
	self.receiptsTableView = nil;
}

#pragma mark - ReceiptDownloaderDelegate Methods

-(void) didDownloadReceiptId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url
{
    if (!self.isViewLoaded)
        return;
    
    // Check whether any of the visible cells care about this receipt
    NSArray *visibleCells = self.receiptsTableView.visibleCells;
    for (ReceiptStoreListCell *cell in visibleCells)
    {
        if ([cell.receiptId isEqualToString:receiptId] && thumbNail)
        {
            // Grab the receipt image from the cache.  We know it's there because we received this notification
            NSString *dataType = nil;
            NSData *imageData = [[ReceiptCache sharedInstance] getThumbNailReceiptForId:receiptId dataType:&dataType];
            UIImage *image = [UIImage imageWithData:imageData];
            
            // It should never be nil, but check it just to be safe
            if (image != nil)
            {
                [cell.activityView stopAnimating];
                [cell.activityView setHidden:YES];
                [cell.thumbImageView setImage:image];
            }
            
            break;
        }
    }
}

-(void) didFailToDownloadReceiptId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url
{
    if (!self.isViewLoaded)
        return;
    
    // Check whether any of the visible cells care about this receipt
    NSArray *visibleCells = self.receiptsTableView.visibleCells;
    for (ReceiptStoreListCell *cell in visibleCells)
    {
        if ([cell.receiptId isEqualToString:receiptId])
        {
            [self showPlaceHolderImageInCell:cell];
        }
    }
}

-(void) showPlaceHolderImageInCell:(ReceiptStoreListCell*)cell
{
    // This is what we show when we don't have the image and are not able to download it.
    [cell.activityView stopAnimating];
    [cell.activityView setHidden:YES];
    [cell.thumbImageView setImage:[UIImage imageNamed:@"receipt_store"]];
}

#pragma mark NoDataMasterViewDelegate method
-(void) actionOnNoData:(id)sender
{
    [self uploadAction:sender];
}

- (NSString *)instructionForNoDataView
{    
    return nil;
}

#pragma mark Receipt list grouping
-(void)prepareReceiptGroups
{
    NSString *key = nil;
    NSString *previousKey = nil;
    NSMutableArray *sortedArray =  [[NSMutableArray alloc] init];
    NSMutableDictionary *newGroupedReceiptsLookup = [[NSMutableDictionary alloc] init];
    NSMutableArray *newSectionsArray = [[NSMutableArray alloc] init];
    
    for (ReceiptStoreReceipt *receiptObject in tblDataSource) 
    {
        key = [CCDateUtilities formatDateToMonthYearFromDateStringWithTimeZone:receiptObject.imageDate];

        // MOB-9407 prevent crash resulted from date conversion error
        if (key == nil)
        {
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"ReceiptStoreListView::prepareReceiptGroups failed to format date %@", receiptObject.imageDate] Level:MC_LOG_DEBU];

            key = @""; 
        }
        
        if (![previousKey isEqualToString:key]) 
        {            
            [newSectionsArray addObject:key];            
            
            if ([sortedArray count] > 0) 
            {
                if (previousKey) {
                    newGroupedReceiptsLookup[previousKey] = sortedArray;
                }
                sortedArray = nil;
                
                sortedArray = [[NSMutableArray alloc] init];
            }
            
            previousKey = key;
        }
        
        [sortedArray addObject:receiptObject]; 
    }
    
    if ([sortedArray count] >0) 
    {
        if (previousKey) {
            newGroupedReceiptsLookup[previousKey] = sortedArray;
        }
    }
    
    sortedArray = nil;
    self.groupedReceiptsLookup = newGroupedReceiptsLookup;
    self.sectionsArray = newSectionsArray;
}

#pragma mark Refresh data on entering foreground
-(void)applicationEnteredForeground
{
    if ([self isViewLoaded]) 
    {
        [self refreshIfStaleData];
    }
}

#pragma mark -upload queue banner upload queue support
-(void) adjustViewForUploadBanner
{
    if (!bannerAdjusted)
    {
        self.bannerAdjusted = YES;
        self.uploadView.delegate = self;
        if (self.flipView != nil)
            self.flipView.frame = CGRectMake(0, flipViewY + self.uploadView.frame.size.height, flipView.frame.size.width, flipView.frame.size.height);
        if (self.receiptsTableView != nil)
            self.receiptsTableView.frame = CGRectMake(0, tableViewY + uploadView.frame.size.height, receiptsTableView.frame.size.width, receiptsTableView.frame.size.height - uploadView.frame.size.height);
        
        [self.view addSubview:uploadView];
        [self.view bringSubviewToFront:uploadView];
    }
}
-(void) adjustViewForNoUploadBanner
{
    if (bannerAdjusted)
    {
        [self.receiptsTableView setFrame:CGRectMake(0, 0, receiptsTableView.frame.size.width, receiptsTableView.frame.size.height + uploadView.frame.size.height)];
        self.flipView.frame = CGRectMake(0, 0, flipView.frame.size.width, flipView.frame.size.height);
        [self.uploadView removeFromSuperview];
        self.uploadView = nil;
        self.bannerAdjusted = NO;
    }
}

-(void) showUploadViewController
{
    UploadQueueViewController *vc = [[UploadQueueViewController alloc] initWithNibName:@"UploadQueueViewController" bundle:nil];
    vc.delegate = self;
    UIBarButtonItem *btnUpload = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Upload"] style:UIBarButtonItemStyleBordered target:vc action:@selector(startUpload)];
    vc.title = [Localizer getLocalizedText:@"Upload Queue"];
    vc.navigationItem.rightBarButtonItem = btnUpload;
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark - UploadQueueVCDelegate Methods

-(void) didDismissUploadQueueVC
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"N", @"FILTER_MOBILE_EXPENSE", nil];
	[[ExSystem sharedInstance].msgControl createMsg:RECEIPT_STORE_RECEIPTS CacheOnly:@"YES" ParameterBag:pBag SkipCache:NO RespondTo:self];
}

- (NSString*) titleForNoDataView
{
    return [Localizer getLocalizedText:@"NO_RECEIPTS_NEG"];
}

@end
