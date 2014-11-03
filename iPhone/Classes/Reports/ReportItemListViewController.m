//
//  ReportItemListViewController.m
//  ConcurMobile
//
//  Created by yiwen on 5/19/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReportItemListViewController.h"
#import "ViewConstants.h"
#import "FormatUtils.h"
#import "ReportEntryItemViewController.h"
#import "ConcurMobileAppDelegate.h"

#import "ReportEntryFormData.h"
#import "DeleteReportEntryData.h"
#import "SummaryCellMLines.h"

#import "ExpenseTypesViewController.h"

#define kSectionItemsName @"Items"

@implementation ReportItemListViewController
@synthesize entry, itemTbHelper;

#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return ITEMIZATION_LIST;
}


#pragma mark - Data initialization
- (void)setSeedData:(NSDictionary*)pBag
{
    [self setSeedData:pBag[@"REPORT"] entry:pBag[@"ENTRY"] role:pBag[@"ROLE"]];
    
}

- (void)setSeedData:(ReportData*)report entry:(EntryData*)thisEntry role:(NSString*) curRole
{
    self.role = curRole;
    [self loadEntry:thisEntry withReport:report];
}


// Replace both loadEntry and updateEntry
- (void) loadEntry:(EntryData*) thisEntry withReport:(ReportData*) report
{
    if (report != nil)
        self.rpt = report;
    
    self.entry = thisEntry;
    self.itemTbHelper = [[ItemizedToolbarHelper alloc] init];
    [itemTbHelper setSeedData:self.entry];
    
	//[self initFields];
    
    [self recalculateSections];
    [self refreshView];
}

- (void) refreshView
{
    if (entry != nil && [self isViewLoaded])
	{
        if ([entry.itemKeys count] ==0) 
        {//show we gots no data view
            [self showNoDataView:self];
        }
        else
        {//refresh from the server, after an initial no show...
            [self hideNoDataView];
        }

		[tableList reloadData];
		[self setupToolbar];
        [self hideWaitView];        
	}
    else
        self.doReload = YES;
    
}


-(void) recalculateSections
{
    // Let's setup new sections data, before switch over
    NSMutableArray          *newSections = [[NSMutableArray alloc] init];
    NSMutableDictionary     *newSectionDataMap = [[NSMutableDictionary alloc] init]; // Non-field data map for sections
	if([entry.items count] > 0)
	{
		[newSections addObject:kSectionItemsName];
        newSectionDataMap[kSectionItemsName] = entry.itemKeys;
	}
    
    // ##TODO## to move it
    NSDictionary *dictionary = @{@"Count": [NSString stringWithFormat:@"%d", [entry.items count]]};
    [Flurry logEvent:@"Report Entry: Itemized Entry List" withParameters:dictionary];
	
    self.sections = newSections;
    self.sectionDataMap = newSectionDataMap;

}

+(void) showItemizationView:(MobileViewController*)parentVC withParameterBag: (NSMutableDictionary*)pBag
{
	ReportEntryItemViewController *vc;
	vc = [[ReportEntryItemViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
	
	[vc setSeedData:pBag];
	[parentVC.navigationController pushViewController:vc animated:YES];
}

//-(void) loadEntry
//{
//	[self recalculateSections];
//	if ([self isViewLoaded])
//	{
//		[self hideWaitView];
//        
//		[tableList reloadData];
//		if(![UIDevice isPad])
//			[self setupToolbar];
//	}
//	else {
//		self.doReload = YES;
//	}
//    
//}
//

-(void)setupToolbar
{
    [super setupToolbar];
    if ([self canEdit] && 
        [ExSystem connectedToNetwork])
    {
        [itemTbHelper setupToolbar:self];
		UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
		
        self.navigationItem.rightBarButtonItem = nil;
        [self.navigationItem setRightBarButtonItem:btnAdd animated:NO];
	}
}


-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
	
/* Uncomment this block of code, if we enable approval on itemization screens.
    if ([msg.idKey isEqualToString:APPROVE_REPORTS_DATA])
	{//this is the entry point for send back and also for approve
		//int x = 0;
		if(![UIDevice isPad])
			[self setupToolbar];
		
		if (msg.errBody != nil) 
		{
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:msg.errCode
								  message:msg.errBody
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
								  otherButtonTitles:nil];
            
			[alert show];
			[alert release];
		}
		else 
		{
			[[ExSystem sharedInstance].cacheData removeCache:APPROVE_REPORT_DETAIL_DATA UserID:[ExSystem sharedInstance].userName RecordKey:rpt.rptKey];
			NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"POP_TO_ROOT_VIEW", nil];
			[ConcurMobileAppDelegate switchToView:APPROVE_REPORTS viewFrom:APPROVE_ENTRIES ParameterBag:pBag];
			[pBag release];
		}
		
		[self hideWaitView];
	}
	else*/ if ([msg.idKey isEqualToString:REPORT_ENTRY_FORM_DATA])
	{
        if ([self isViewLoaded]) {
            [self hideLoadingView];
        }
        
        if (msg.errBody != nil) 
        {
            UIAlertView *alert = [[MobileAlertView alloc] 
                                  initWithTitle:[Localizer getLocalizedText:@"Unable to add itemization"]
                                  message:msg.errBody
                                  delegate:nil 
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
                                  otherButtonTitles:nil];
            
            [alert show];
        }
        else 
        {
            ReportEntryFormData* resp = (ReportEntryFormData*) msg.responder;
            resp.rpt.entry.parentRpeKey = self.entry.rpeKey;
            // Temp fix, b/c server passes back garbage
            resp.rpt.entry.rpeKey = nil;
            resp.rpt.entry.rptKey = self.rpt.rptKey;
            
            [resp.rpt.entry createDefaultAttendeeUsingExpenseTypeVersion:@"V3" policyKey:self.rpt.polKey forChild:YES];
            
            NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"SHORT_CIRCUIT", self.rpt, @"REPORT", self.entry, @"ENTRY", resp.rpt.entry, @"ITEM", nil];
            if (self.role != nil)
                pBag[@"ROLE"] = self.role;
            
            [ReportItemListViewController showItemizationView:self withParameterBag: pBag];
        }
	}
	else if ([msg.idKey isEqualToString:DELETE_REPORT_ENTRY_DATA])
	{
		NSString* errMsg = msg.errBody;
		DeleteReportEntryData* srd = (DeleteReportEntryData*) msg.responder;
		if (errMsg == nil && srd != nil)
		{
			if (srd.curStatus != nil && srd.curStatus.errMsg != nil)
			{
				errMsg = srd.curStatus.errMsg;
			}
		}
		
		if (errMsg != nil) 
		{
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:[Localizer getLocalizedText:@"Delete Entry Failed"]
								  message:errMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
		}
		else 
		{
			// update previous screens.
			int vcCount = [self.navigationController.viewControllers count];
			int stIx = [UIDevice isPad]?0:2;
			for (int ix = stIx ; ix < vcCount; ix++ )
			{
				MobileViewController *parentMVC = (MobileViewController *)(self.navigationController.viewControllers)[ix];
				if (parentMVC != nil && parentMVC != self)
				{
					[parentMVC respondToFoundData:msg];
				}
			}
			if ([UIDevice isPad])
			{
				
				MobileViewController* rptVc = (MobileViewController*)[ConcurMobileAppDelegate getBaseNavigationController].topViewController;
				[rptVc respondToFoundData:msg];
			}
            [self loadEntry:(srd.rpt.entries)[self.entry.rpeKey] withReport:srd.rpt];
            // TODO: verify if this is required.
//			([ConcurMobileAppDelegate findRootViewController].viewState)[ACTIVE_REPORTS] = @"FETCH";
		}
		
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
	}
	else if ([msg.idKey isEqualToString:ACTIVE_REPORT_DETAIL_DATA])
	{
		ReportDetailDataBase *rad = (ReportDetailDataBase *)msg.responder;
		EntryData* newEntry = (rad.rpt.entries)[self.entry.rpeKey];
		if (newEntry != nil)
		{
			[self loadEntry:newEntry withReport:rad.rpt];
		}
	}
	else if (msg.parameterBag != nil & (msg.parameterBag)[@"REPORT"] != nil & (msg.parameterBag)[@"ENTRY"] != nil)
	{
		[self loadEntry:(msg.parameterBag)[@"ENTRY"] 
             withReport:(msg.parameterBag)[@"REPORT"]];
	}
	
	if ([entry.itemKeys count] < 1 || entry.itemKeys == nil) 
	{//show we gots no data view
		[self showNoDataView:self];
	}
	else if (entry.itemKeys != nil & [entry.itemKeys count] > 0)
	{//refresh from the server, after an initial no show...
		[self hideNoDataView];
	}
    
	
}



#pragma mark -
#pragma mark ViewController Methods

- (void)viewDidAppear:(BOOL)animated 
{
	//[tableView setContentOffset:CGPointMake(0, 0) animated:NO]; mob-1170
	
	if ([entry.itemKeys count] < 1 || entry.itemKeys == nil) 
	{//show we gots no data view
		[self showNoDataView:self];
	}
	else if (entry.itemKeys != nil & [entry.itemKeys count] > 0)
	{//refresh from the server, after an initial no show...
		[self hideNoDataView];
	}
	
	[super viewDidAppear:animated];
}



// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{	
    [super viewDidLoad];
	
	if([UIDevice isPad])
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 360.0);
	}
    self.title = [Localizer getLocalizedText:@"EXPENSE_DETAILS_ITEMIZATIONS"];
    
}


#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [sections count];
}


-(void)makeItemCell:(SummaryCellMLines *)cell Item:(EntryData *)item
{
	if ([item.expKey isEqualToString:@"UNDEF"])
	{
		cell.lblName.textColor = [UIColor redColor];
	}
	else {
		cell.lblName.textColor = [UIColor blackColor];
	}
    
    NSString* nameStr = item.expName;
    NSString* amountStr = [FormatUtils formatMoney:item.transactionAmount crnCode:item.transactionCrnCode];
	// Entry date is local, not UTC
    NSString* dateStr = [DateTimeFormatter formatLocalDateMedium:item.transactionDate];
    NSString* vendorStr = [self getVendorString:item.vendorDescription WithLocation:item.locationName];
    NSString* line1Str = [NSString stringWithFormat:@"%@ - %@", dateStr, vendorStr];
	NSArray* iconNames = [self getIconNames:item];
    
    NSString* image1 = [iconNames count]>0?iconNames[0] : nil;
    NSString* image2 = [iconNames count]>1?iconNames[1] : nil;
    NSString* image3 = [iconNames count]>2?iconNames[2] : nil;
    
    BOOL hideExceptionIcon = ![item.hasExceptions isEqualToString:@"Y"];
    cell.ivException.hidden = hideExceptionIcon;
    
    [cell resetCellContent:nameStr withAmount:amountStr withLine1:line1Str withLine2: nil withImage1:image1 withImage2:image2 withImage3:image3];

	[cell setAccessoryType:UITableViewCellAccessoryNone];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
//	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];	
//	NSString *sectionName = [sections objectAtIndex:section];
	
//    if([sectionName isEqual:kSectionItemizedName])
//	{
//		ReportEntryItemizedCell *cell = (ReportEntryItemizedCell *)[tableView dequeueReusableCellWithIdentifier: @"ReportEntryItemizedCell"];
//		if (cell == nil)  
//		{
//			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ReportEntryItemizedCell" owner:self options:nil];
//			for (id oneObject in nib)
//				if ([oneObject isKindOfClass:[ReportEntryItemizedCell class]])
//					cell = (ReportEntryItemizedCell *)oneObject;
//		}
//		
//		cell.lblItemized.text = [Localizer getLocalizedText:@"Itemized:"];
//		cell.lblRemaining.text = [Localizer getLocalizedText:@"Remaining:"];
//		cell.lblItemizedAmt.text = self.itemizedAmount;
//		cell.lblRemainingAmt.text = self.remainingAmount;
//		
//		NSString* zeroAmt = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", 0.0] crnCode:entry.transactionCrnCode];
//		if ([zeroAmt isEqualToString:self.remainingAmount])
//		{
//			cell.lblRemaining.textColor = [UIColor blackColor];
//			cell.lblRemainingAmt.textColor = [UIColor blackColor];
//		}
//		else {
//			cell.lblRemaining.textColor = [UIColor redColor];
//			cell.lblRemainingAmt.textColor = [UIColor redColor];
//		}
//        
//		return cell;
//	}
//	else 
	{
		NSString *key = (entry.itemKeys)[row];
		EntryData *item = (entry.items)[key];
		
		SummaryCellMLines *cell = (SummaryCellMLines *)[tableView dequeueReusableCellWithIdentifier: @"SummaryCell2Lines"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SummaryCell2Lines" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[SummaryCellMLines class]])
					cell = (SummaryCellMLines *)oneObject;
		}
		[self makeItemCell:cell Item:item];
		return cell;
	}
}

#pragma mark -
#pragma mark NoData Delegate Methods 
-(void) actionOnNoData:(id)sender
{
    [self buttonAddPressed:sender];
}

#pragma mark -
#pragma mark Table Delegate Methods 
-(NSIndexPath *)tableView:(UITableView *)tableView 
 willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    return indexPath; 
}

-(void)buttonAddPressed:(id) sender
{
    NSDictionary *dictionary = @{@"Non Hotel": @"Type"};
    [Flurry logEvent:@"Report Entry: Itemize Entry" withParameters:dictionary];
    
    // Pop up expense types dialog and upon select an expense type, open the new itemization form
    [ExpenseTypesViewController showExpenseTypeEditor:self policy:self.rpt.polKey parentVC:self selectedExpKey:nil parentExpKey:self.entry.expKey withReport:self.rpt];

}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
	NSUInteger section = [newIndexPath section];
    NSUInteger row = [newIndexPath row];	
	NSString *sectionName = sections[section];
	
	if ([sectionName isEqualToString:kSectionItemsName])
	{
		NSString *key = (entry.itemKeys)[row];
		EntryData *itemEntry = (entry.items)[key];
		
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SHORT_CIRCUIT", nil];
		if (self.role != nil)
			pBag[@"ROLE"] = self.role;
		
		pBag[@"REPORT"] = rpt;
		pBag[@"ITEM"] = itemEntry;
		pBag[@"ENTRY"] = entry;
		pBag[@"RECORD_KEY"] = rpt.rptKey;
		pBag[@"ID_KEY"] = rpt.rptKey;
        
		[ReportItemListViewController showItemizationView:self withParameterBag: pBag];
        
	}
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
//	NSUInteger section = [indexPath section];
//    NSUInteger row = [indexPath row];	
//	NSString *sectionName = [sections objectAtIndex:section];
//    
//	else if ([sectionName isEqual:kSectionItemizedName])
//	{
//		return 44;
//	}
	return 55;
}

#pragma mark -
#pragma mark Delete Itemization Methods 
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
		NSUInteger section = [indexPath section];
		NSUInteger row = [indexPath row];
		NSString *sectionName = sections[section];
		if ([kSectionItemsName isEqual:sectionName]) 
		{
			[self showWaitView];
			NSArray * rpeKeys = @[(entry.itemKeys)[row]];
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: 
										 self.rpt.rptKey, @"RPT_KEY",
										 rpeKeys, @"RPE_KEYS",
										 [self getViewIDKey], @"TO_VIEW", @"YES", @"SKIP_CACHE", nil];
			[[ExSystem sharedInstance].msgControl createMsg:DELETE_REPORT_ENTRY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
		}
	}
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)aTableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
    // Detemine if it's in editing mode
	NSUInteger section = [indexPath section];
	NSString *sectionName = sections[section];
    
	if ([kSectionItemsName isEqual:sectionName] && [self canSubmit] ) 
	{
        return UITableViewCellEditingStyleDelete;
    }
    return UITableViewCellEditingStyleNone;
}

#pragma mark -
#pragma mark ExpenseTypeDelegate Methods 
- (void)cancelExpenseType
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)saveSelectedExpenseType:(ExpenseTypeData*) et
{
	[self showLoadingView];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 et.expKey, @"EXP_KEY",
								 rpt.rptKey, @"RPT_KEY", 
								 entry.rpeKey, @"PARENT_RPE_KEY",
								 nil];
	[[ExSystem sharedInstance].msgControl createMsg:REPORT_ENTRY_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	
	
	[self dismissViewControllerAnimated:YES completion:nil];
    
}


@end
