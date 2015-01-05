//
//  UploadQueueDS.m
//  ConcurMobile
//
//  Created by Shifan Wu on 11/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "UploadQueueDS.h"
#import "UploadQueue.h"

#import "MobileEntryManager.h"
#import "QEEntryDefaultLayoutCell.h"
#import "ReceiptStoreListCell.h"
//#import "CCardTransaction.h"
//#import "PCardTransaction.h"
#import "DateTimeFormatter.h"
#import "FormatUtils.h"
#import "UploadableReceipt.h"
#import "UploadQueueItemManager.h"
#import "ReceiptCache.h"

@implementation UploadQueueDS

@synthesize tableList, managedObjectContext, fetchedResultsController;
@synthesize delegate = _delegate;

-(id)init
{
    self = [super init];
    if (self)
    {
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate *) [UIApplication sharedApplication].delegate;
        self.managedObjectContext = ad.managedObjectContext;
    }
    return self;
}

-(void) setSeedData:(UITableView *)tbl withDelegate:(id<UploadQueueDSDelegate>)del
{
    self.delegate = del;
    self.tableList = tbl;
    self.tableList.delegate = self;
    self.tableList.dataSource = self;
    [self fetchedResults];
}

#pragma mark - NSFetchedResultsController
- (void)fetchedResults
{
    if (fetchedResultsController == nil)
    {
        NSFetchRequest *request = [[UploadQueue sharedInstance]makeFetchRequestForVisibleQueuedItemsInContext:managedObjectContext];
        
        self.fetchedResultsController = [[NSFetchedResultsController alloc]
                                         initWithFetchRequest:request
                                         managedObjectContext:self.managedObjectContext
                                         sectionNameKeyPath:nil
                                         cacheName:nil];
        fetchedResultsController.delegate = self;
    }
    
    NSError *error;
  	if (![fetchedResultsController performFetch:&error])
    {
		// Update to handle the error appropriately.
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"UploadQueueDS: fetchedResults %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
    }
}

#pragma mark - NSFetchedResultsControllerDelegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableList beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo
           atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
{
    switch(type)
    {
        case NSFetchedResultsChangeInsert:
            [self.tableList insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [self.tableList deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type newIndexPath:(NSIndexPath *)newIndexPath
{
    UITableView *tableView = self.tableList;
    
    switch(type)
    {
        case NSFetchedResultsChangeInsert:
            [tableView insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeUpdate:
            [tableView reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
            break;
            
        case NSFetchedResultsChangeMove:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [tableView insertRowsAtIndexPaths:@[newIndexPath]withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableList endUpdates];
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [[fetchedResultsController sections] count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    id <NSFetchedResultsSectionInfo> sectionInfo = [fetchedResultsController sections][section];
    
    if ([sectionInfo numberOfObjects] == 0)
        [self.delegate showNoDataView];
    else
        [self.delegate hideNoDataView]; // A modified item will be dequeued and requeued, causing the queue to be temporarily empty if there are no other items besides the modified one.  This line hides the negative view when the modified item gets requeued.
    
    return [sectionInfo numberOfObjects];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return [self tableView:tableView configureCellForRowAtIndexPath:indexPath];
}

- (UITableViewCell *)tableView:(UITableView *)tableView configureCellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    EntityUploadQueueItem *item  = [fetchedResultsController objectAtIndexPath:indexPath];
    
    if ([item.entityTypeName isEqualToString:@"EntityMobileEntry"])
    {
        EntityMobileEntry *mobileEntry = [[MobileEntryManager sharedInstance] fetchByLocalId:(NSString*)item.entityInstanceId];
        return [self tableView:tableView configureCellForMobileEntry:mobileEntry];
    }
    else
    {
        NSDate *receiptDate = item.creationDate;
        UIImage *receiptImage = nil;
        NSData *receiptData = nil;
        // MOB-21462: need to consider the case when the receipt data type is pdf
        BOOL isPdfReceipt = [item.entityTypeName isEqualToString:@"PdfReceipt"];
        if (isPdfReceipt) {
            receiptData = [UploadableReceipt receiptDataForLocalReceiptImageId:item.entityInstanceId];
        } else {
            receiptImage = [UploadableReceipt imageForLocalReceiptImageId:item.entityInstanceId];
        }
        return [self tableView:tableView configureCellForReceiptImage:receiptImage date:receiptDate pdfData:receiptData];
    }
}

- (UITableViewCell*)tableView:(UITableView *)tableView configureCellForMobileEntry:(EntityMobileEntry *)mobileEntry
{
	static NSString *cellIdentity = @"QEEntryDefaultLayoutCell";
    QEEntryDefaultLayoutCell *cell = (QEEntryDefaultLayoutCell *)[tableView dequeueReusableCellWithIdentifier: cellIdentity];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:cellIdentity owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[QEEntryDefaultLayoutCell class]])
                cell = (QEEntryDefaultLayoutCell *)oneObject;
    }
    
    
    cell.ivIcon2.hidden = YES;
    cell.ivIcon1.hidden = YES;
    
	if ([MobileEntryManager isCardTransaction:mobileEntry])
	{
		if ([MobileEntryManager isCorporateCardTransaction:mobileEntry])
		{
			//cell.type = CCT_TYPE;
			cell.lblSub2.text = mobileEntry.cardTypeName;
		}
		else if ([MobileEntryManager isPersonalCardTransaction:mobileEntry])
		{
			//cell.type = PCT_TYPE;
			cell.lblSub2.text = mobileEntry.cardName;
		}
        cell.ivIcon2.hidden = NO;
        cell.ivIcon2.image = [UIImage imageNamed: @"icon_card_19"];
	}
    
	cell.lblHeading.text = mobileEntry.expName;
	cell.lblSub1.text = [DateTimeFormatter formatDateMediumByDate:mobileEntry.transactionDate];
	
	cell.lblAmount.text = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", [mobileEntry.transactionAmount floatValue] ] crnCode:mobileEntry.crnCode];
	if (mobileEntry.vendorName != nil)
	{
		if(mobileEntry.locationName != nil)
			cell.lblSub2.text = [NSString stringWithFormat:@"%@ - %@", mobileEntry.vendorName, mobileEntry.locationName];
		else
			cell.lblSub2.text = mobileEntry.vendorName;
	}
	else
	{
		cell.lblSub2.text = mobileEntry.locationName;
	}
	
    //NSLog(@"[entry hasReceipt] %@", [entry hasReceipt]);
    //MOB-13315: if localreceiptimagid is present then the entry is yet to be updated
    if([mobileEntry.hasReceipt isEqualToString:@"Y"] || mobileEntry.localReceiptImageId !=nil)
    {
        /* MOB-5783
         OK, I'm using the icon_receipt_button icon, not the icon_receipt_19 image.*/
        if([MobileEntryManager isCardTransaction:mobileEntry])
        {
            cell.ivIcon1.hidden = NO;
            cell.ivIcon1.image = [UIImage imageNamed: @"icon_receipt_button"];
        }
        else
        {
            cell.ivIcon2.hidden = NO;
            cell.ivIcon2.image = [UIImage imageNamed: @"icon_receipt_button"];
        }
    }

    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    return cell;
}

-(UITableViewCell*)tableView:(UITableView *)tableView configureCellForReceiptImage:(UIImage *)receiptImage date:(NSDate *)receiptDate pdfData:(NSData *)pdfData
{
    static NSString *CellIdentifier = @"ReceiptStoreListCell";
	
	ReceiptStoreListCell *cell = (ReceiptStoreListCell *)[tableView dequeueReusableCellWithIdentifier:CellIdentifier];
	if (cell == nil)
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ReceiptStoreListCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ReceiptStoreListCell class]])
				cell = (ReceiptStoreListCell *)oneObject;
	}
	
	[cell.imageBackgroundView setIsRoundingDisabled:YES];
    
    [cell.activityView stopAnimating];
    [cell.activityView setHidden:YES];
    // MOB-21462: if it is pdf, need to display on webview
    if ([pdfData length]) {
        [cell.imageView setHidden:YES];
        [cell.pdfWebView setHidden:NO]; // it is pdf receipt, need webview
        [cell.pdfWebView loadData:pdfData MIMEType:@"application/pdf" textEncodingName:@"UTF-8" baseURL:nil];
        
    } else {
        [cell.pdfWebView setHidden:YES];    // it is regular jpg receipt, no need webview
        [cell.imageView setHidden:NO];
        [cell.thumbImageView setImage:receiptImage];//[UIImage imageNamed:@"receipt_store"]];
    }
    
    NSString* imgDate = [DateTimeFormatter formatDateEEEMMMddyyyyByDate:receiptDate];
	[cell.imageDateLbl setText:imgDate];
	[cell.tagLbl setText:[Localizer getLocalizedText:@"Receipt"]];
    
    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    return cell;
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    // No editing mode, only support support to delete
    return UITableViewCellEditingStyleDelete;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(editingStyle == UITableViewCellEditingStyleDelete)
    {
        EntityUploadQueueItem *item = [fetchedResultsController objectAtIndexPath:indexPath];
        [UploadQueue dequeue:item];
        
        NSString *isOffline = @"No";
        if([ExSystem connectedToNetwork])
            isOffline = @"Yes";

        NSDictionary *dict = @{@"Was Offline": isOffline, @"Queue Count": [NSString stringWithFormat: @"%lu", (unsigned long)[fetchedResultsController.fetchedObjects count]]};
        [Flurry logEvent:@"Offline: Delete" withParameters:dict];
        
        if ([[UploadQueue sharedInstance] visibleQueuedItemCount] == 0) {
            [self.delegate showNoDataView];
        }
    }
}

#pragma mark - Table view delegate
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 74;      //Height for one row in OutOfPocketListVC
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    EntityUploadQueueItem *item = [fetchedResultsController objectAtIndexPath:indexPath];
    if ([item.entityTypeName isEqualToString:@"EntityMobileEntry"])
    {
        NSDictionary *dict = @{@"Type": @"Queued Quick Expense"};
        [Flurry logEvent:@"Offline: Viewed" withParameters:dict];
        
        EntityMobileEntry *mobileEntry = [[MobileEntryManager sharedInstance] fetchByLocalId:item.entityInstanceId];
        [self.delegate didSelectMobileEntry:mobileEntry];
    }
    else if ([item.entityTypeName isEqualToString:@"Receipt"] || [item.entityTypeName isEqualToString:@"PdfReceipt"])
    {
        NSDictionary *dict = @{@"Type": @"Queued Receipt"};
        [Flurry logEvent:@"Offline: Viewed" withParameters:dict];
        
        // MOB-21462: need to know if the receipt data type for displaying on the upload queue
        [self.delegate didSelectReceiptWithId:item.entityInstanceId isPdfReceipt:[item.entityTypeName isEqualToString:@"PdfReceipt"]];
    }
}
@end
