//
//  ReportDetailViewController.m
//  ConcurMobile
//
//  Created by yiwen on 4/25/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "FeedbackManager.h"
#import "ReportDetailViewController.h"
#import "MobileActionSheet.h"
#import "LabelConstants.h"
#import "ViewConstants.h"
#import "DataConstants.h"
#import "ReceiptStoreListView.h"
#import "ReportEntryFormData.h"
#import "DeleteReportEntryData.h"
#import "ExpenseTypesManager.h"
#import "ReportEntryViewController.h"
#import "ActiveReportListViewController.h"
#import "ConcurMobileAppDelegate.h"
#import "ExReceiptManager.h"
#import "ReceiptStoreReceipt.h"
#import "ReceiptCacheManager.h"
#import "KeyboardNavigationController.h"
#import "ReceiptEditorVC.h"
#import "QuickExpensesReceiptStoreVC.h"
#import "SaveReportReceipt.h"
#import "SaveReportReceipt2.h"
#import "HelpOverlayFactory.h"
#import "ItineraryStopViewController.h"
#import "AnalyticsTracker.h"
#import "ItineraryTableViewController.h"
#import "Itinerary.h"
#import "ApproverTAViewController.h"
#import "ItineraryInitialViewController.h"
#import "ItineraryImportViewController.h"
#import "UIActionSheet+Blocks.h"
#import "ItineraryAllowanceAdjustmentViewController.h"

@interface ReportDetailViewController ()
//Receipt actions
- (void)showReceiptViewer;
@property BOOL isDeleteCarMileageExpense;
@end

@implementation ReportDetailViewController

#define kSectionReport 0
#define kSectionEntries 1

#define kSectionReportName @"Report"
#define kSectionEntriesName @"Entries"

#define kActionSheetReceipt     100101
#define kActionSheetAddExpense  100102
#define kActionSheetTravelAllowance  100103


- (NSString*)getViewIDKey
{
    return REPORT_DETAIL;    
}

- (void)setSeedDataWithIDOnly:(NSString*)rptKey role:(NSString*) curRole
{
	// This is for tripit, which only gives us a report key, and not a report object when TripIt trip is expensed.
    self.role = curRole;
    isLoading = YES;
    // Fetch report detail with ID only
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: rptKey, @"ID_KEY", rptKey, @"RECORD_KEY", ACTIVE_ENTRIES, @"TO_VIEW", nil];
    pBag[@"ROLE_CODE"] = self.role;
    pBag[@"V3"] = @"YES";
    if([ExSystem connectedToNetwork])
    {
        [[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    else
    {
        [[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
    }
}

- (void)setSeedData:(NSDictionary*)pBag
{
    if ([pBag[@"SOURCE_SECTION"]isEqualToString:@"REPORT_APPROVAL_SECTION"])
    {
        self.isReportApproval = YES;
    }
    
    if (pBag[@"REPORT"] == nil)
    {
        [self setSeedDataWithIDOnly:pBag[@"ID_KEY"] role:pBag[@"ROLE"]];
    }
    else
        [self setSeedData:pBag[@"REPORT"] role:pBag[@"ROLE"]];
}

- (void)setSeedData:(ReportData*)report role:(NSString*) curRole
{
    self.role = curRole;
    [self loadReport:report];
    
    if (![report isDetail])
    {
        isLoading = YES;
    }
    [self fetchReportDetail];
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self refreshView];

    // Add Test drive help overlay
    if (!self.isApproving) {
        [HelpOverlayFactory addiPhoneReportDetailOverlayToView:self.navigationController.view];
    } else {
        [HelpOverlayFactory addiPhoneApprovalDetailOverlayToView:self.navigationController.view];
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [[FeedbackManager sharedInstance] setShowRatingOnNextView:YES];
}

-(void) resortEntries
{
    NSMutableArray   *unsortedEntries = [[NSMutableArray alloc] init];
    for (NSString* key in rpt.keys)
    {
        [unsortedEntries addObject:(rpt.entries)[key]];
    }
    NSSortDescriptor *dateDescriptor = [[NSSortDescriptor alloc] initWithKey:@"transactionDate" ascending:NO];
    NSArray *sortDescriptors = @[dateDescriptor];
    
    NSArray *sortedEntries = [unsortedEntries sortedArrayUsingDescriptors:sortDescriptors];

    NSMutableArray* sortedKeys = [[NSMutableArray alloc] init];
    for (EntryData* e in sortedEntries)
    {
        [sortedKeys addObject:e.rpeKey];
    }
    rpt.keys = sortedKeys;
}

#pragma mark - Loading/Reloading
-(void) recalculateSections
{
    [self resortEntries];  // MOB-6594, newest on top
    
    // Let's setup new sections data, before switch over
    NSMutableArray          *newSections = [[NSMutableArray alloc] init];
    NSMutableDictionary     *newSectionDataMap = [[NSMutableDictionary alloc] init]; // Non-field data map for sections

    if (rpt != nil)
    {
        [newSections addObject:kSectionReportName];
        NSMutableArray      *rptItems = [[NSMutableArray alloc] init];
        [rptItems addObject:@"APPROVE_REPORT_SUMMARY"];
        
        // MOB-14894: only display receipt row if the report item has receipt or it is allowed to update the receipt
        if ( self.rpt.receiptImageId != nil || [self hasReceipt] || [self canUpdateReceipt]) {
            [rptItems addObject:@"REPORT_DETAIL_RECEIPTS"];
        }

        // Add Itinerary link
        BOOL hasFixedTA = [[ExSystem sharedInstance] siteSettingHasFixedTA];
        if (hasFixedTA)
        {
            if([self isApproving])
            {
                if([self hasTravelAllowanceEntries:self.rpt])
                {
                    [rptItems addObject:@"REPORT_TA_ITINERARY"];
                }
            }
            else
            {
                [rptItems addObject:@"REPORT_TA_ITINERARY"];
            }
        }
        
        newSectionDataMap[kSectionReportName] = rptItems;
        
        if ([rpt hasEntry])
        {
            [newSections addObject:kSectionEntriesName];
            newSectionDataMap[kSectionEntriesName] = rpt.keys;
        }
    }

    self.sections = newSections;
    self.sectionDataMap = newSectionDataMap;
}


-(void) loadReport:(ReportData*) report
{
    if (self.rpt == nil)
        self.rpt = report;
    else
    {
        // Merge with existing report data and refresh the view, if loaded
        if ([report isDetail])
            [rpt copyDetail:report];
        else
            [rpt copyHeaderDetail:report];
    }
 
    if (self == self.navigationController.topViewController)
    {   // MOB-7170 - Do not reconfig receipt view, if this is not the top view
        [[ExReceiptManager sharedInstance] configureReceiptManagerForDelegate:self 
                                                           andReportEntry:nil 
                                                             andReporData:rpt 
                                                          andExpenseEntry:nil 
                                                                  andRole:self.role];
    }
    [self recalculateSections];
    [self refreshView];
}


- (void) refreshView
{
    if (rpt != nil && [self isViewLoaded])
    {
        [self drawHeaderRpt:rpt HeadLabel:lblName AmountLabel:lblAmount LabelLine1:lblLine1 LabelLine2:lblLine2 Image1:img1 Image2:img2 Image3:img3];
		// MOB-17301 - Show report summary when user taps the header view 
        [self.rptHeaderTapGesture addTarget:self action:@selector(rptHeaderTapped:)];
        [self setupToolbar];
        
        // If no header detail
        if (!(self.rpt.fields != nil && [self.rpt.fields count]>0) && [ExSystem connectedToNetwork])
        {
        //MOB-17083 - commented as loading or wait view is not required as base class is already showing a waitview. 
            // Check if loadingview is showing
//            if (![self isLoadingViewShowing] && ![self isWaitViewShowing]) {
//                 [self showWaitViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
//            }
           
        }
        else if (![rpt hasEntry]) 
        {//show we gots no data view

            [tableList setSectionFooterHeight:0];
            
            int vertSize = [ExSystem is5]? 280 : 200;
            
            UIView  *footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, vertSize)];
            [self showNoDataView:self asSubviewOfView:footerView];
            [tableList setTableFooterView:footerView];
        }
        else
        {//refresh from the server, after an initial no show...
            [self hideNoDataView];
            [self hideLoadingView];
        }
        
        [tableList reloadData];
    }
}


-(void) fetchReportDetail
{
    if(rpt != nil && [rpt isDetail])
        return;
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
                                 ACTIVE_ENTRIES, @"TO_VIEW", nil];
    
    // Problem when there is an old rpt detail in cache.  Need to clear rpt dtl cache first. not much saving.
    pBag[@"ROLE_CODE"] = self.role;
    pBag[@"V3"] = @"YES";
    if([ExSystem connectedToNetwork])
    {
        [[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    else
    {
        [[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
    }
}


-(void)setupToolbar
{
    [super setupToolbar];

	if([ExSystem connectedToNetwork])
	{
		UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
		flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
		
		NSMutableArray *toolbarItems = [NSMutableArray arrayWithCapacity:5];
		
		SEL leftSelector = @selector(actionReject:);
        
		SEL rightSelector = (self.rpt.workflowActions == nil || [self.rpt.workflowActions count]==0)?
            @selector(actionApprove:) : @selector(actionApproveActions:); // MOB-9753
		
		// Mob-2517,2518 Localization of Send Back & Approve
		NSString* leftBtnLabel = [Localizer getLocalizedText:@"APPROVE_SENDBACK_BUTTON_TITLE"];
		NSString* rightBtnLabel = [Localizer getLocalizedText:@"APPROVE_APPROVE_BUTTON_TITLE"];
		
		if (![self isApproving])
		{
            leftSelector = nil;
            
			if ([self canSubmit] && ![UIDevice isPad])
			{
				rightSelector = @selector(actionSubmit:);
                rightBtnLabel = [Localizer getLocalizedText:@"LABEL_SUBMIT_BTN"];

			}
            else
			{
				rightSelector = nil;
			}
		}
		
		if (leftSelector != nil)
		{
			UIBarButtonItem *btnLeft = [[UIBarButtonItem alloc] initWithTitle:leftBtnLabel style:UIBarButtonItemStyleBordered target:self action:leftSelector];
			[toolbarItems addObject:btnLeft];
		}
		
		[toolbarItems addObject:flexibleSpace];
		
		if (rightSelector != nil)
		{
			UIBarButtonItem *btnRight = [[UIBarButtonItem alloc] initWithTitle:rightBtnLabel style:UIBarButtonItemStyleBordered target:self action:rightSelector];
			[toolbarItems addObject:btnRight];
		}

		[self.navigationController.toolbar setHidden:NO];
		[self setToolbarItems:toolbarItems animated:YES];
        
        if ([self canEdit])
        {
            UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
            
            self.navigationItem.rightBarButtonItem = nil;
            [self.navigationItem setRightBarButtonItem:btnAdd animated:NO];
        }

	}
    
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
    [super alertView:alertView clickedButtonAtIndex:buttonIndex];
}

-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
	if ([msg.idKey isEqualToString:SUBMIT_REPORT_DATA]||[msg.idKey isEqualToString:APPROVE_REPORTS_DATA])
	{
		return;
	}
	else if ([msg.idKey isEqualToString:REPORT_ENTRY_FORM_DATA])
	{
        if ([self isViewLoaded]) {
            [self hideLoadingView];
        }
        
        // MOB-5920 redisplay no data view, which is hid displaying loading view in MVC.
        if (![self.rpt hasEntry])
            [self refreshView];
        
        if (msg.errBody != nil) 
        {
            UIAlertView *alert = [[MobileAlertView alloc] 
                                  initWithTitle:[Localizer getLocalizedText:@"Unable to add expense"]
                                  message:msg.errBody
                                  delegate:nil 
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
                                  otherButtonTitles:nil];
            
            [alert show];
        }
        else 
        {
            ReportEntryFormData* resp = (ReportEntryFormData*) msg.responder;
            resp.rpt.entry.rpeKey = nil;
            resp.rpt.entry.rptKey = self.rpt.rptKey;
            
            // need to update amount field based on expense and policy information
            [resp checkIfServerHandlesAmountsForExpenseKey:resp.expKey withPolicy:self.rpt.polKey];
            
            [resp.rpt.entry createDefaultAttendeeUsingExpenseTypeVersion:@"V3" policyKey:self.rpt.polKey forChild:NO];
            
            NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"SHORT_CIRCUIT", self.rpt, @"REPORT", resp.rpt.entry, @"ENTRY", nil];
            if (self.role != nil)
                pBag[@"ROLE"] = self.role;
            
            pBag[@"TITLE"] = [Localizer getLocalizedText:@"Add Expense"];

            [ReportDetailViewController showEntryView:self withParameterBag: pBag carMileageFlag:false];
        }
	}
	else if ([msg.idKey isEqualToString:DELETE_REPORT_ENTRY_DATA])
	{
        // Do not hide wait view, if this msg is propagated from sub views
        if ([self isViewLoaded] && msg.responder.respondToMvc == self) {
            [self hideWaitView];
        }
        
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
		else {
            // MOB-21980: if deleting a car mileage expense, need to update car rates data
            if (self.isDeleteCarMileageExpense){
                [self updateCarRatesData];
            }

            [self refreshWithUpdatedReport:srd.rpt];
		}
	}
	else if ([msg.idKey isEqualToString:REPORT_HEADER_DETAIL_DATA])
	{
		// Get report detail cache
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
									 ACTIVE_ENTRIES, @"TO_VIEW", nil];
		[[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"YES" ParameterBag:pBag SkipCache:NO RespondTo:self];
	}
	else if ([msg.idKey isEqualToString:ACTIVE_REPORT_DETAIL_DATA]|| [msg.idKey isEqualToString:APPROVE_REPORT_DETAIL_DATA])
	{
        isLoading = NO;

		//NSLog(@"CAME_FROM_CACHE = %@", [msg.parameterBag objectForKey:@"CAME_FROM_CACHE"]);
		ReportDetailDataBase *rad = (ReportDetailDataBase *)msg.responder;
		ReportData*	theRpt = nil;
		
		if ([rad.keys count] > 0) 
		{
			NSString *rptKey = (rad.keys)[0];
			theRpt = (rad.rpts)[rptKey];
		}
		else {
			theRpt = rad.rpt;
		}
        
		if (theRpt != nil)
		{
            self.rpt = nil;  // Force refresh of entries
            [self loadReport:theRpt]; // ##TODO## Check receipt Url
            //			[self refreshWithDetail:theRpt];
            if ([msg.idKey isEqualToString:ACTIVE_REPORT_DETAIL_DATA])
                [self refreshParent:msg]; 
            
		}
        
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }

	}
    else if ([msg.idKey isEqualToString:SAVE_REPORT_RECEIPT2])
    {
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
        
		NSString* errMsg = msg.errBody;
		SaveReportReceipt2* srd = (SaveReportReceipt2*) msg.responder;
		if (errMsg == nil && srd != nil)
		{
			if (srd.reportStatus != nil && srd.reportStatus.errMsg != nil)
			{
				errMsg = srd.reportStatus.errMsg;
			}
		}
		
		if (errMsg != nil)
		{
			UIAlertView *alert = [[MobileAlertView alloc]
								  initWithTitle:[Localizer getLocalizedText:@"SAVE_REPORT_RECEIPT_FAILURE"]
								  message:nil
								  delegate:nil
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
            // Clear the receiptImageId, since we no longer need it
            self.rpt.receiptImageId = nil;
		}
		else
            [self savedReportReceipt];
    }
    else if ([msg.idKey isEqualToString:SAVE_REPORT_RECEIPT])
    {
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
        
		NSString* errMsg = msg.errBody;
		SaveReportReceipt* srd = (SaveReportReceipt*) msg.responder;
		if (errMsg == nil && srd != nil)
		{
			if (srd.reportStatus != nil && srd.reportStatus.errMsg != nil)
			{
				errMsg = srd.reportStatus.errMsg;
			}
		}
		
		if (errMsg != nil)
		{
			UIAlertView *alert = [[MobileAlertView alloc]
								  initWithTitle:[Localizer getLocalizedText:@"SAVE_REPORT_RECEIPT_FAILURE"]
								  message:nil
								  delegate:nil
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
            // Clear the receiptImageId, since we no longer need it
            self.rpt.receiptImageId = nil;
		}
		else
            [self savedReportReceipt];
        
    }
	else if (![msg.idKey isEqualToString:SAVE_REPORT_RECEIPT2] && ![msg.idKey isEqualToString:@"PRE_FETCH"] &&
             msg.parameterBag != nil && 
             ((msg.parameterBag)[@"REPORT"] != nil ||(msg.parameterBag)[@"REPORT_DETAIL"] != nil))
	{
        if ((msg.parameterBag)[@"REPORT"] != nil && (msg.parameterBag)[@"REPORT_DETAIL"] == nil)
		{
			// SHORT_CIRCUIT (from ActiveReports) comes here
			[self loadReport:(msg.parameterBag)[@"REPORT"]];
		}
		else {
			// AddToReport 
			[self loadReport:(msg.parameterBag)[@"REPORT_DETAIL"]];
		}
        
		// MOB-4765 - preload expense types to see which expense can itemize
		ExpenseTypesManager* etMgr = [ExpenseTypesManager sharedInstance];
		[etMgr loadExpenseTypes:self.rpt.polKey msgControl:[ExSystem sharedInstance].msgControl];
        
		BOOL isWizard = [@"YES" isEqualToString:(msg.parameterBag)[@"REPORT_CREATE_WIZARD"]];
		
		if ([self isViewLoaded])
		{
			if (isWizard)
			{
				[self buttonAddPressed:nil];
			}
		}
		
        if ([role isEqualToString:ROLE_EXPENSE_TRAVELER])
            [self refreshParent:msg];
	}
	else if ([msg.idKey isEqualToString:@"PRE_FETCH"])
	{
        [self fetchReportDetail];
	}
    
    else if([msg.idKey isEqualToString:CAR_RATES_DATA])
    {
        NSString* errMsg = msg.errBody;
        if (errMsg == nil){
            [ConcurMobileAppDelegate findRootViewController].carRatesData = (CarRatesData*) msg.responder;
        }
    }
}

// get the car rate data
- (void)updateCarRatesData
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    [[ExSystem sharedInstance].msgControl createMsg:CAR_RATES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache: YES RespondTo:self];
}

-(void)rptHeaderTapped:(id) sender
{
	if (self.rpt == nil)
		return;
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc]
								 initWithObjectsAndKeys:
								 //@"YES", @"SKIP_PARSE",
								 self.role, @"ROLE",
								 //@"YES", @"SHORT_CIRCUIT",
								 nil];
	if (rpt.employeeName != nil)
		pBag[@"EmployeeName"] = rpt.employeeName;
	pBag[@"ReportName"] = rpt.reportName;
	pBag[@"TotalPostedAmount"] = rpt.totalPostedAmount;
	pBag[@"CrnCode"] = rpt.crnCode;
	pBag[@"REPORT"] = rpt;
	pBag[@"RECORD_KEY"] = rpt.rptKey;
	pBag[@"REPORT_OG"] = rpt;
	
    [ConcurMobileAppDelegate switchToView:APPROVE_REPORT_SUMMARY viewFrom:[self getViewIDKey] ParameterBag:pBag];

    NSLog(@" Report header tapped");
}
#pragma mark -
#pragma mark Table View Data Source Methods
- (UITableViewCell *)tableView:(UITableView *)tblView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	if (section == kSectionReport) 
	{
        UITableViewCell* cell = nil;
        NSArray* sectionData = [self getSectionData:section];
		NSString *val = sectionData[row];
		
		if ([val isEqualToString:@"REPORT_DETAIL_RECEIPTS"]) 
		{
            NSString *command = nil;
            if (self.rpt.receiptImageId != nil || [self hasReceipt])
                command = [Localizer getLocalizedText:@"APPROVE_ENTRIES_ACTION_SECTION_VIEW_RECEIPTS_ROW"];
            else if ([self canUpdateReceipt])
                command = [Localizer getLocalizedText:@"Add Receipt"];
            
            cell = [self makeDrillCell:tblView withText:command withImage:@"icon_receipt_button" enabled:YES];
		}
		else if ([val isEqualToString:@"APPROVE_REPORT_SUMMARY"])
		{
			NSString* command = [Localizer getLocalizedText:val]; 
			cell = [self makeDrillCell:tblView withText:command withImage:@"icon_summary_button" enabled:YES];
		}
        else if ([val isEqualToString:@"REPORT_TA_ITINERARY"])
		{
			NSString* command = [Localizer getLocalizedText:val];
			cell = [self makeDrillCell:tblView withText:command withImage:@"icon_travel_allowance" enabled:YES];

            // Do a split for the add item

		}
		
		return cell;
	}
	else 
	{
		SummaryCellMLines *cell = (SummaryCellMLines *)[tblView dequeueReusableCellWithIdentifier: @"SummaryCell3Lines"];
		if (cell == nil) 
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SummaryCell3Lines" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[SummaryCellMLines class]])
					cell = (SummaryCellMLines *)oneObject;
		}
		
		NSString *key = (self.rpt.keys)[row];
		EntryData *entry = (self.rpt.entries)[key];
		[self makeEntryCell:cell Entry:entry];
		
		return cell;
	}
}


#pragma mark -
#pragma mark Table Delegate Methods 
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
	NSUInteger section = [newIndexPath section];
    NSUInteger row = [newIndexPath row];	
	
	if (self.rpt == nil)
		return;
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] 
								 initWithObjectsAndKeys:
								 //@"YES", @"SKIP_PARSE", 
								 self.role, @"ROLE", 
								 //@"YES", @"SHORT_CIRCUIT", 
								 nil];
	if (rpt.employeeName != nil)
		pBag[@"EmployeeName"] = rpt.employeeName;
	pBag[@"ReportName"] = rpt.reportName;
	pBag[@"TotalPostedAmount"] = rpt.totalPostedAmount;
	pBag[@"CrnCode"] = rpt.crnCode;
	pBag[@"REPORT"] = rpt;
	pBag[@"RECORD_KEY"] = rpt.rptKey;
	pBag[@"REPORT_OG"] = rpt;
	
	if(section == kSectionReport)
	{
        NSArray* sectionData = [self getSectionData:kSectionReport];
		NSString *rowName =  sectionData[row];
		if([rowName isEqualToString:@"REPORT_DETAIL_RECEIPTS"])
		{
            [self showReceiptViewer];
		}
		else if([rowName isEqualToString:@"APPROVE_REPORT_SUMMARY"]) // Mob-2514
		{
			[ConcurMobileAppDelegate switchToView:APPROVE_REPORT_SUMMARY viewFrom:[self getViewIDKey] ParameterBag:pBag];
		}
        else if([rowName isEqualToString:@"REPORT_TA_ITINERARY"]) // Add Itinerary handler
        {

            if([self isApproving])
            {
                UIStoryboard* storyboard = [UIStoryboard storyboardWithName:[@"ItineraryApproverStoryboard" storyboardName] bundle:nil];
                ApproverTAViewController *o = (ApproverTAViewController *)[storyboard instantiateViewControllerWithIdentifier:@"ApproverTabbedView"];
//                UIViewController *o = (UIViewController *)[storyboard instantiateViewControllerWithIdentifier:@"TAApproverSegmented"];
                o.paramBag = pBag;
                o.role = self.role;
                NSLog(@"~~~Show the tab bar");
                [self.navigationController.toolbar setHidden:YES];
                [self.navigationController pushViewController:o animated:YES];
            }
            else {
                UIStoryboard* storyboard = [UIStoryboard storyboardWithName:[@"ItineraryStoryboard" storyboardName] bundle:nil];
                if ([self hasTravelAllowanceEntries:self.rpt]) {
                    [tableView deselectRowAtIndexPath:newIndexPath animated:YES];
                    [self showTravelAllowanceActionSheet:pBag storyboard:storyboard];
                }
                else
                {
                    ItineraryInitialViewController *o = (ItineraryInitialViewController *) [storyboard instantiateViewControllerWithIdentifier:@"ItineraryInitialViewController"];
                    o.paramBag = pBag;
                    [self.navigationController pushViewController:o animated:YES];
                }
            }
            return;
        }
	}
	else if(section == kSectionEntries)
	{
		NSString *key = (rpt.keys)[row];
		EntryData *entry = (rpt.entries)[key];
		
		pBag[@"ENTRY"] = entry;
		pBag[@"ID_KEY"] = rpt.rptKey;
        if (self.isReportApproval) {
            pBag[@"SOURCE_SECTION"] = @"REPORT_APPROVAL_SECTION";
        }
        [ConcurMobileAppDelegate switchToView:APPROVE_EXPENSE_DETAILS viewFrom:[self getViewIDKey] ParameterBag:pBag];
	}
	
    [self.tableList deselectRowAtIndexPath:newIndexPath animated:NO];
}

-(BOOL) isCompanyCarMileageExpType:(NSString*)expType
{
    ExpenseTypeData* expTypeData = [[ExpenseTypesManager sharedInstance]
                                    expenseTypeForVersion:@"V3" policyKey:self.rpt.polKey
                                    expenseKey:expType forChild:NO];

    if (expTypeData == nil && [expType isEqualToString:@"CARMI"])
        return TRUE;
    return [expTypeData isCompanyCarMileage];
}

- (void)showTravelAllowanceActionSheet:(NSMutableDictionary *)pBag storyboard:(UIStoryboard *)storyboard
{
// There are ta entries, so show the action sheet
    if ([ExSystem is8Plus]){
        [self showTravelAllowanceAlertController:pBag storyboard:storyboard];
    }
    else
    {
        UIActionSheet *sheet = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:nil destructiveButtonTitle:nil otherButtonTitles:nil];
        sheet.tag = kActionSheetTravelAllowance;

        [sheet addButtonWithTitle:[Localizer getLocalizedText:@"Itineraries"]];
        [sheet addButtonWithTitle:[Localizer getLocalizedText:@"Adjustments"]];

        NSInteger cancelIndex = [sheet addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
        sheet.cancelButtonIndex = cancelIndex;

        void (^tapped)(UIActionSheet *, NSInteger) = ^(UIActionSheet *actionSheet, NSInteger buttonIndex)
        {
            if (buttonIndex == sheet.cancelButtonIndex)
            {
                // Just a placeholder
            }
            else
                if (buttonIndex == 0)
                {
                    [self showItineraryInitial:pBag storyboard:storyboard];
                }
                else
                {
                    [self showItineraryAllowance:storyboard];
                }
        };
        sheet.tapBlock = tapped;

        [sheet showFromToolbar:self.navigationController.toolbar];
    }
}

- (void)showItineraryAllowance:(UIStoryboard *)storyboard
{
    ItineraryAllowanceAdjustmentViewController *o = (ItineraryAllowanceAdjustmentViewController *) [storyboard instantiateViewControllerWithIdentifier:@"ItineraryAllowanceAdjustment"];
    o.rptKey = rpt.rptKey;
    o.crnCode = rpt.crnCode;
    o.role = self.role;
    BOOL canEdit = [self canEdit];
    o.hideGenerateExpenseButton = !canEdit;
    [self.navigationController pushViewController:o animated:YES];
}

- (void)showItineraryInitial:(NSMutableDictionary *)pBag storyboard:(UIStoryboard *)storyboard
{
    ItineraryInitialViewController *o = (ItineraryInitialViewController *) [storyboard instantiateViewControllerWithIdentifier:@"ItineraryInitialViewController"];
    o.paramBag = pBag;
    [self.navigationController pushViewController:o animated:YES];
}

- (void)showTravelAllowanceAlertController:(NSMutableDictionary *)pBag storyboard:(UIStoryboard *)storyboard
{
    UIAlertController *sheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];

    void (^itineraryAction)(UIAlertAction *) = ^(UIAlertAction *action){
        NSLog(@"action = %@", action.title);
        [self showItineraryInitial:pBag storyboard:storyboard];
    };
    UIAlertAction *itinAction = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Itineraries"] style:UIAlertActionStyleDefault handler:itineraryAction];
    [sheet addAction:itinAction];

    void (^adjustmentAction)(UIAlertAction *) = ^(UIAlertAction *action){
        NSLog(@"action = %@", action.title);
        [self showItineraryAllowance:storyboard];
    };
    UIAlertAction *adjustAction = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Adjustments"] style:UIAlertActionStyleDefault handler:adjustmentAction];
    [sheet addAction:adjustAction];

    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Cancel"] style:UIAlertActionStyleCancel handler:nil];
    [sheet addAction:cancelAction];

    [self presentViewController:sheet animated:YES completion:nil];

}


- (NSInteger)tableView:(UITableView *)tblView 
sectionForSectionIndexTitle:(NSString *)title 
               atIndex:(NSInteger)index
{
    NSString *key = (rpt.keys)[index];
    if (key == UITableViewIndexSearch)
    {
        [tblView setContentOffset:CGPointZero animated:NO];
        return NSNotFound;
    }
    else return index;
    
}


- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
	if (section == kSectionReport) 
		return 44;
	else 
		return 70;
}

#pragma mark -
#pragma mark Delete Report Entry methods
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if (![self canSubmit])
        return;
    
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
		NSUInteger section = [indexPath section];
		NSUInteger row = [indexPath row];
		if (section > kSectionReport) 
		{
			[self showWaitView];

            // Why it is a array???
			NSArray * rpeKeys = @[(rpt.keys)[row]];
            
            // MOB-21980: need to check if the deleted item is a car mileage expense. if so, need to update car rates data
            EntryData *entry = (self.rpt.entries)[rpeKeys[0]];
            self.isDeleteCarMileageExpense = [self isCompanyCarMileageExpType:entry.expKey];
            
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
	if (section > kSectionReport && [self canSubmit] ) 
	{
        return UITableViewCellEditingStyleDelete;
    }
    return UITableViewCellEditingStyleNone;
}

#pragma mark -
#pragma mark Attach/Update Report level Receipt methods
-(BOOL) hasReceipt
{
	return (rpt == nil || rpt.receiptImageAvailable == nil)? FALSE : [rpt.receiptImageAvailable isEqualToString:@"Y"];
}

- (void)showReceiptViewer
{
    //
    // set up data model
    //
    ReceiptEditorDataModel* receiptEditorDataModel = [[ReceiptEditorDataModel alloc] initWithReportData:self.rpt];
    
    ReceiptEditorVC *receiptView = [[ReceiptEditorVC alloc] initWithNibName:@"ReceiptEditorVC" bundle:nil];
    receiptView.title = [Localizer getLocalizedText:@"Receipt"];
    receiptView.delegate = self;
    receiptView.canDelete = NO;
    receiptView.canUpdate = NO;
    // MOB-13593 - Force reload the receipts if its from report header. 
    receiptView.ignoreCache = YES;
    receiptView.canAppend = [self canUpdateReceipt];
    receiptView.canUseReceiptStore = [self canUpdateReceipt];
    receiptView.receiptEditorDataModel = receiptEditorDataModel;
        
    // Do not cache report receipt image, since entry view can upload/update images.
    Receipt *receipt = [[Receipt alloc] init];
    receipt.cacheId = [NSString stringWithFormat:@"RPT_%@", self.rpt.rptKey];
    receipt.url = self.rpt.realPdfUrl;
    receipt.useHttpPost = TRUE;
    if ([self hasReceipt])
    {
        // Make sure receipt::hasReceipt returns TRUE
        receipt.dataType = PDF;
    }
    
    [receiptView setSeedData:receipt];

    receiptView.supportsOffline = NO;   // ##TODO##, to test.  Cannot push the receipt to upload queue

    [self.navigationController pushViewController:receiptView animated:YES];
}

#pragma mark ReceiptEditorDelegate
-(void) receiptUpdated:(Receipt*) rcpt useV2Endpoint:(BOOL)useV2Endpoint
{
    // AJC - MOB-13542 - this is terrible and i apologize. the alternative is that it just doesn't work
    if (rcpt != nil )//&& rcpt.receiptId != nil)    // the second part of this if && was included prior to MOB-134542 / apologize
    // AJC - MOB-13542 - this is terrible and i apologize. the alternative is that it just doesn't work
    {
        self.rpt.receiptImageId = rcpt.receiptId;
        
        NSString* dataType = rcpt.dataType;

        NSData* fileData = nil;
        NSString* mimeType = @"";
        
        if([dataType caseInsensitiveCompare:JPG] == NSOrderedSame)
        {
            // pngs and jpgs are both called just "jpg"
            // other areas of the code treat images generically and it works with the server
            fileData = UIImageJPEGRepresentation(rcpt.receiptImg, 0.9f);
            mimeType = MIME_TYPE_JPG;
        }
        else if ([dataType caseInsensitiveCompare:PDF] == NSOrderedSame)
        {
            fileData = rcpt.pdfData;
            mimeType = MIME_TYPE_PDF;
        }
        else
        {
            NSLog(@"!!! This case should never be hit !!!");
            
            // if dataType is not set - as it previously was not in all places - treating the incoming data like an image
            // which it would be, because that was the only pathway previously existing
            fileData = UIImageJPEGRepresentation(rcpt.receiptImg, 0.9f);
            mimeType = MIME_TYPE_JPG;
        }
        
        if( useV2Endpoint )
        {
            NSDictionary *dict = @{@"Added To": @"Report"};
            [Flurry logEvent:@"Receipts: Add" withParameters:dict];
            
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.rpt, @"REPORT", [self getViewIDKey], @"TO_VIEW", mimeType, @"MIME_TYPE", fileData, @"FILE_DATA", nil];
            [[ExSystem sharedInstance].msgControl createMsg:SAVE_REPORT_RECEIPT2 CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        }
        else // use V1 endpoint
        {
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.rpt, @"REPORT", [self getViewIDKey], @"TO_VIEW", nil];
            [[ExSystem sharedInstance].msgControl createMsg:SAVE_REPORT_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        }
    }
}

-(void) receiptDeleted:(Receipt*) receipt{}
-(void) receiptQueued:(Receipt*) receipt{}  // For offline?

- (void)receiptDisplayed:(Receipt *)rcpt
{
    // MOB-6132
    // MOB-10146 check for PDF as well as image files as entry receipt
    if ([self isApproving] && [rcpt hasReceipt]) {
        // Audit approver has viewed image.
        [self sendViewReceiptAuditMsg];
    }
}


-(void) sendViewReceiptAuditMsg
{
    NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/MarkReportReceiptsAsViewed/%@", [ExSystem sharedInstance].entitySettings.uri, self.rpt.rptKey];
    
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	RequestController *rc = [RequestController alloc];
	Msg *msg = [[Msg alloc] initWithData:@"MarkReportReceiptsAsViewed" State:@"" Position:nil MessageData:nil URI:path MessageResponder:nil ParameterBag:pBag];
	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	msg.skipCache = YES;
	
	[rc initDirect:msg MVC:self];
}


#pragma mark ReceiptDetailViewDelegate method
-(void)savedReportReceipt
{
    [self fetchReportDetail];
}

#pragma mark -
#pragma mark Report/Entry edit methods
-(void) buttonAddExpensePressed
{
	// Pop up expense types dialog and upon select an expense type, open the new itemization form
	[ExpenseTypesViewController showExpenseTypeEditor:self policy:self.rpt.polKey   
		parentVC:self selectedExpKey:nil parentExpKey:nil withReport:self.rpt];	
}

-(void) buttonImportExpensesPressed
{
    // MOB-12690 
    QuickExpensesReceiptStoreVC* vc = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    [vc setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:NO allowListEdit:NO];
    vc.reportToWhichToAddExpenses = self.rpt;
    vc.requireRefresh = YES;

	[self.navigationController pushViewController:vc animated:YES];
}

+(void) showEntryView:(MobileViewController*)parentVC withParameterBag: (NSMutableDictionary*)pBag carMileageFlag:(BOOL)isCarMileage
{
	// MOB-17086 close button is only for iPad
	ReportEntryViewController* vc = [[ReportEntryViewController alloc] initWithCloseButton:[UIDevice isPad]];

    if ([parentVC isKindOfClass:[ReportViewControllerBase class]])
    {
        vc.role = ((ReportViewControllerBase*)parentVC).role;
    }
    else
    {
        vc.role = pBag[@"ROLE"];
    }
    
    NSString* title = pBag[@"TITLE"];
    if (title != nil)
        vc.title = title;
    
	vc.isCarMileage = isCarMileage;
	
    NSString* fromHome = pBag[@"FROM_HOME"];
    if (fromHome != nil && [fromHome isEqualToString:@"YES"])
        vc.isFromHome = YES;
    
	Msg *msg = [[Msg alloc] init];
	msg.parameterBag = pBag;
	
	if([UIDevice isPad])
	{
        // MOB-8533 allow dismiss keyboard using modal form sheet
		UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:vc];
		localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
		[localNavigationController setToolbarHidden:NO];
		localNavigationController.toolbar.tintColor = [UIColor darkBlueConcur_iOS6];
		localNavigationController.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];
		
		[parentVC presentViewController:localNavigationController animated:YES completion:nil];
	}
	else
		[parentVC.navigationController pushViewController:vc animated:YES];
	[vc respondToFoundData:msg];
}

#pragma mark REFRESH UPDATE
-(void) refreshParent:(Msg *) msg
{
	if ((msg.parameterBag)[@"REFRESH_PARENT"] != nil)
	{
		if(![UIDevice isPad])
		{
			NSUInteger vcCount = [self.navigationController.viewControllers count];
			MobileViewController* parentMVC = vcCount >= 3?(self.navigationController.viewControllers)[vcCount - 3] : nil;
			if (([[parentMVC getViewIDKey] isEqualToString:ACTIVE_REPORTS])
				&& [parentMVC isKindOfClass:ActiveReportListViewController.class])
			{
				ActiveReportListViewController *lvc = (ActiveReportListViewController*) parentMVC;
				[lvc updateReport:rpt];
			}
		}
		else {
			ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
			[ad.navController.navigationBar setHidden:NO];
			ReportDetailViewController_iPad *davc = (ad.navController.viewControllers)[[ad.navController.viewControllers count] - 1];
			if([davc isKindOfClass:[ReportDetailViewController_iPad class]])
			{
				NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.rpt, @"REPORT", self.rpt.rptKey, @"ID_KEY", self.rpt.rptKey, @"RECORD_KEY", @"YES", @"SHORT_CIRCUIT", nil]; //, @"YES", @"SKIP_PARSE"
				//[rootViewController switchToView:APPROVE_ENTRIES viewFrom:APPROVE_REPORTS ParameterBag:pBag];
				[davc loadReport:pBag];
			}
		}
        
	}
	
}

#pragma mark -
#pragma mark UIActionSheetDelegate Methods
- (void)setSmartExpenseMatching:(BOOL)enableSmartExpenseMatching
{
    [ExSystem sharedInstance].entitySettings.smartExpenseEnabledOnReports = [NSNumber numberWithBool:enableSmartExpenseMatching];
    [[ExSystem sharedInstance] saveSettings];

    NSDictionary *dict = @{@"From": @"Report Screen"};
    if (enableSmartExpenseMatching) {
        [Flurry logEvent:@"SmartExpense: Report Match On" withParameters:dict];
    } else {
        [Flurry logEvent:@"SmartExpense: Report Match Off" withParameters:dict];
    }
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (actionSheet.tag == kActionSheetAddExpense)
	{
		if (buttonIndex == 0) // Add expense	
		{
			[self buttonAddExpensePressed];
		} 
		else if (buttonIndex == 1) // Import expenses
		{
            [self setSmartExpenseMatching:NO];

			[self buttonImportExpensesPressed];
		} else if (buttonIndex == 2) // Import and Match
        {
            [self setSmartExpenseMatching:YES];

            [self buttonImportExpensesPressed];
        }
	}
    if (actionSheet.tag == kActionSheetTravelAllowance)
    {
        // Placeholder, the button click is handled by a block
    }
    else
    {
        [super actionSheet:actionSheet clickedButtonAtIndex:buttonIndex];
    }
}

-(IBAction)buttonAddPressed:(id) sender
{
	UIActionSheet * addExpenseAction = [[MobileActionSheet alloc] initWithTitle:nil
                                                                       delegate:self 
                                                              cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                                                         destructiveButtonTitle:nil
                                                              otherButtonTitles:[Localizer getLocalizedText:@"Add New Expense"],
                                        [Localizer getLocalizedText:@"Import Expenses"],
                                        [Localizer getLocalizedText:@"Import and Match"], nil];
	
	addExpenseAction.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
	addExpenseAction.tag = kActionSheetAddExpense;

    [addExpenseAction showInView:self.tableList];
    
    if (![rpt hasEntry] && [sender isKindOfClass:[UIBarButtonItem class]])
    {
        NSString *eventLabel = [NSString stringWithFormat:@"From: %@", @"Plus"];
        [AnalyticsTracker logEventWithCategory:@"Report" eventAction:@"Add Expense" eventLabel:eventLabel eventValue:nil];
    }
}


#pragma mark NoDataMasterDelegate methods
-(void) actionOnNoData:(id)sender
{
    [self buttonAddPressed:sender];
    if (![rpt hasEntry])
    {
        NSString *eventLabel = [NSString stringWithFormat:@"From: %@", @"Button"];
        [AnalyticsTracker logEventWithCategory:@"Report" eventAction:@"Add Expense" eventLabel:eventLabel eventValue:nil];
    }
}

#pragma mark -
#pragma mark ExpenseTypeDelegate Methods 
- (void)cancelExpenseType
{
  	[self dismissViewControllerAnimated:YES completion:nil];
}

- (void)saveSelectedExpenseType:(ExpenseTypeData*) expenseType
{
	[self showLoadingView];
	
    if([expenseType isCompanyCarMileage] || [expenseType isPersonalCarMileage])
    {
        NSDictionary *dictionary = @{@"Add from": @"Report Add Expense"};
        [Flurry logEvent:@"Car Mileage: Add from" withParameters:dictionary];
    }

	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 expenseType.expKey, @"EXP_KEY",
								 rpt.rptKey, @"RPT_KEY", 
								 nil];
	[[ExSystem sharedInstance].msgControl createMsg:REPORT_ENTRY_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	
	[self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark -
#pragma mark ExpenseTypeDelegate Methods 

+(void)showReportDetail:(MobileViewController*)pvc withReport:(ReportData*)report withRole:(NSString*) curRole
{
	ReportDetailViewController *c = [[ReportDetailViewController alloc] initWithNibName:@"ReportHeaderView" bundle:nil];
    
    [c setSeedData:report role:curRole];

	if([UIDevice isPad])
	{
		c.modalPresentationStyle = UIModalPresentationFormSheet;
	}
	[pvc.navigationController pushViewController:c animated:YES];
}

-(IBAction)unwindFromAllowanceAdjustment:(UIStoryboardSegue *)segue
{
    //This exists to make the unwind segue from the allowance adjustment work
    //TODO There needs to be a block to refresh the entries
}

@end
