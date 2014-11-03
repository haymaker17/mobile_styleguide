    //
//  ReportDetailViewController_iPad.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FeedbackManager.h"
#import "ReportDetailViewController_iPad.h"
#import "ExSystem.h" 

#import "SettingsViewController.h"
#import "ViewConstants.h"
#import "FormatUtils.h"
#import "ImageUtil.h"
#import "PCardTransaction.h"
#import "DetailApprovalCell.h"
#import "ReportApprovalDetailData.h"
#import "RequestController.h"
#import "LabelConstants.h"
#import "SubmitReportData.h"
#import "PadExpenseEntryCell.h"
#import "ReportItemListViewController.h"
#import "MobileAlertView.h"
#import "ReportDetailViewController.h"
#import "ReportEntryFormData.h"
#import "ReportSummaryViewController.h"
#import "UploadReceiptData.h"
#import "DeleteReportEntryData.h"
#import "ReportDetailViewController.h"
#import "ExpenseTypesManager.h"
#import "MobileActionSheet.h"
#import "ReportRejectionViewController.h"
#import "SubmitNeedReceiptsViewController.h"
#import "CommentListVC.h"
#import "PDFVC.h"
#import "KeyboardNavigationController.h"
#import "ReportPaneHeader_iPad.h"
#import "ReportPaneFooter_iPad.h"
#import "ReportDetailCell_iPad.h"
#import "QuickExpensesReceiptStoreVC.h"
#import "ReceiptEditorVC.h"
#import "Config.h"
#import "ApproveReportsData.h"
#import "HelpOverlayFactory.h"
#import "UserConfig.h"
#import "ItineraryAllowanceAdjustmentViewController.h"
#import "ItineraryInitialViewController.h"
#import "ApproverTAViewController.h"

#define MAX_REPORT_BUTTONS 6

#define BUTTON_ID_REPORT_SUMMARY  @"BUTTON_ID_REPORT_SUMMARY"
#define BUTTON_ID_REPORT_RECEIPTS  @"BUTTON_ID_REPORT_RECEIPTS"
#define BUTTON_ID_REPORT_ATTACH_RECEIPT  @"BUTTON_ID_REPORT_ATTACH_RECEIPT"
#define BUTTON_ID_REPORT_COMMENTS  @"BUTTON_ID_REPORT_COMMENTS"
#define BUTTON_ID_REPORT_EXCEPTIONS  @"BUTTON_ID_REPORT_EXCEPTIONS"
#define BUTTON_ID_ADD_EXPENSE  @"BUTTON_ID_ADD_EXPENSE"
#define BUTTON_ID_TRAVEL_ALLOWANCE  @"BUTTON_ID_TRAVEL_ALLOWANCE"

#define GAP_BETWEEN_TABLE_ROWS 7.0

#define kReportSection 0
#define kEntrySection 1

#define		kBtnApproveTag		101777
#define		kBtnSendBackTag		101778

#define		kAlertViewApprove	101777
#define		kAlertViewSubmit	101780
#define		kAlertViewRateApp	101781

#define		kActionViewAddReceipt 101701
#define		kActionViewAddExpense 101702
#define		kAlertViewRateAppApproval	101782
#define     kActionSheetTravelAllowance  101705

@interface ReportButtonDescriptor : NSObject
@property (copy, nonatomic) NSString *buttonId;
@property (copy, nonatomic) NSString *title;
+ (ReportButtonDescriptor*) buttonDescriptorWithId:(NSString*)btnId title:(NSString*)btnTitle;
@end
@implementation ReportButtonDescriptor
@synthesize buttonId, title;

+ (ReportButtonDescriptor*) buttonDescriptorWithId:(NSString*)btnId title:(NSString*)btnTitle
{
    ReportButtonDescriptor* descriptor = [[ReportButtonDescriptor alloc] init];
    descriptor.buttonId = btnId;
    descriptor.title = btnTitle;
    return descriptor;
}

@end

@interface ReportDetailViewController_iPad ()
{
    NSArray *_buttonDescriptors;
}

@property (nonatomic, strong) UIActivityIndicatorView *activity;

// Loading view
@property (strong, nonatomic) IBOutlet UIView       *loadingView;
@property (strong, nonatomic) IBOutlet UILabel      *loadingLabel;

// Left and right panes
@property (strong, nonatomic) IBOutlet UIView       *leftPaneView;
@property (strong, nonatomic) IBOutlet UIView       *leftPaneButtonContainerView;

// Buttons
@property (strong, nonatomic) IBOutlet UIButton     *button0;
@property (strong, nonatomic) IBOutlet UIButton     *button1;
@property (strong, nonatomic) IBOutlet UIButton     *button2;
@property (strong, nonatomic) IBOutlet UIButton     *button3;
@property (strong, nonatomic) IBOutlet UIButton     *button4;
@property (strong, nonatomic) IBOutlet UIButton     *button5;

// Button Labels
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton0;
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton1;
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton2;
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton3;
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton4;
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton5;

- (IBAction) buttonPressed:(id)sender;

-(void) actionCloseEntryReceipt:(id) sender;

@property (assign, nonatomic) CGRect attachReceiptRect;
@property BOOL selectedSendBack;
@end

@implementation ReportDetailViewController_iPad

@synthesize leftPaneHeaderView = _leftPaneHeaderView;
@synthesize leftPaneFooterView = _leftPaneFooterView;

@synthesize popoverController, detailItem, detailDescriptionLabel,btnAttachReceipt,receiptImage;
@synthesize viewType;
@synthesize	aKeys;
@synthesize	aSections, aSectionHeaders;
@synthesize	selectedRows;
@synthesize	oopeDict, rptDetail;
@synthesize oopePopOver, iPadHome;
@synthesize hasComment, isReport, holdView, holdText, holdActivity;
@synthesize ivBackground, ivBlotter, ivBarLeft, ivBarRight, ivBarMiddle, scroller, btnSubmit, entryDetailsDict, btnApprove, btnReject;
@synthesize tripsListVC, reportsListVC;
@synthesize	btnSummary,btnReceipts, btnComments,lblReportName,lblReportDescription,lblReportDate,lblReportStatus, btnAddEntry, lblOffline;
@synthesize detailApprovalListVC, lblTotalAmount, lblTotalRequested, ivLoading, scrollerButtons, lblDivider, lblExpensesTitle, ivRptReadyforSubmit ;
@synthesize		reportKeys;
@synthesize		reportDictionary, previousRpt, nextRpt;
@synthesize		lblTableBack, ivTableTop, ivTableBottom, btnNext, btnPrevious, btnExceptions, headerImg;
@synthesize etDlgDismissed, pBagAddExpense, btnExport, adsView,btnHome, hideReceiptStore;

#pragma mark - Seed methods
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
    
    self.attachReceiptRect = CGRectMake(0, 0, 1, 1); //default
}

#pragma mark -
#pragma mark Rotation support
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

-(void) checkOffline
{
    [iPadHome checkOffline];
    if ([ExSystem connectedToNetwork]) {
        [self.lblOffline setHidden:YES];
    } else {
        [self.lblOffline setHidden:NO];
    }
}

#pragma mark -
#pragma mark View lifecycle
-(void) initSections
{
	if(aSections == nil) //iOS8.5 let's remove this check
		self.aSections = [[NSMutableArray alloc] init];
	
	[aSections removeAllObjects]; //iOS8.5 let's remove this 
	
	self.entryDetailsDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];

}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setToolbarHidden:NO];
    self.hideReceiptStore = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"HIDE_RECEIPT_STORE" withType:@"CTE_EXPENSE_ADMIN"]];
}

- (void)viewDidAppear:(BOOL)animated
{
    // Add Test drive help overlay
    if (!self.isApproving) {
        [HelpOverlayFactory addiPadReportDetailOverlayToView:self.navigationController.view];
    } else {
        [HelpOverlayFactory addiPadApprovalDetailOverlayToView:self.navigationController.view];
    }

    [super viewDidAppear:animated];
}

- (void)viewDidUnload 
{
    self.popoverController = nil;
	// In case of memory warning, the main view is already unloaded, and we don't want to use these references to call removeFromSuperView.
	self.btnSubmit = nil;
	self.btnApprove = nil;
	self.btnReject = nil;
	self.btnComments = nil;
	self.btnSummary = nil;
	self.btnReceipts = nil;
	self.receiptImage = nil; //MOB-3956: we should clean this one out for low mem situations
    self.btnExport = nil;
    self.lblOffline =nil;
    [super viewDidUnload];
}


#pragma mark -
#pragma mark Memory management
- (void)didReceiveMemoryWarning
{
     // MOB-8446 Do not dismiss modal view, since a modal view transition might be in progress; 
     // remove the '[[ExReceiptManager sharedInstance] reset]', which will cause delegate to be nil and may cause a crash if the ExReceiptManager wants to respond.
     // [self actionCloseEntryReceipt:nil];
     // [[ExReceiptManager sharedInstance] reset];
     
     //[holdView removeFromSuperview];  // ViewDidUnload will do this
     
     // Release any cached data, images, etc that aren't in use.
     // Releases the view if it doesn't have a superview.
     [super didReceiveMemoryWarning];
}




#pragma mark -
#pragma mark System Buttons
-(void)makeSystemButtons
{
}

- (IBAction)buttonSettingsPressed:(id)sender
{
	SettingsViewController *svc = [[SettingsViewController alloc] init];
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
	svc.modalPresentationStyle = UIModalPresentationFormSheet;
#endif
	[self presentViewController:svc animated:YES completion:nil];
	
}


-(NSString *)getViewIDKey
{
	return TRIP_DETAILS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void) fetchReportDetail
{
    if(rpt != nil && [rpt isDetail])
        return;
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
                                 ACTIVE_ENTRIES, @"TO_VIEW", nil];
    pBag[@"ROLE_CODE"] = self.role;
    [[ExSystem sharedInstance].msgControl createMsg:REPORT_HEADER_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

    [_dictExpensesToFetch removeAllObjects];
}

-(void) loadReport:(NSMutableDictionary *)pBag
{
    [self showWaitViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
    
	ReportData *rptData = pBag[@"REPORT"];

	NSString *comingFrom = pBag[@"COMING_FROM"];
    
    if ([comingFrom isEqualToString:@"TRIPIT"])
    {
        [self setSeedDataWithIDOnly:pBag[@"ID_KEY"] role:pBag[@"ROLE"]];
    }
	else if([comingFrom isEqualToString:@"REPORT"])
	{
		//NSLog(@"wizard here %@", [pBag objectForKey:@"REPORT_CREATE_WIZARD"]);
		NSMutableDictionary *pBagNew = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rptData, @"REPORT", rptData.rptKey, @"ID_KEY", rptData.rptKey, @"RECORD_KEY",
										ACTIVE_ENTRIES, @"TO_VIEW", pBag[@"REPORT_KEYS"], @"REPORT_KEYS", pBag[@"REPORT_DICT"], @"REPORT_DICT",
										nil];
		
		if(pBag[@"REPORT_CREATE_WIZARD"] != nil)
			pBagNew[@"REPORT_CREATE_WIZARD"] = pBag[@"REPORT_CREATE_WIZARD"];
		
        pBagNew[@"V3"] = @"YES";
		NSString *skippy = pBag[@"SKIPCACHE"];
        
        BOOL skipCache = (skippy != nil || [ExSystem connectedToNetwork]);
        [[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBagNew SkipCache:skipCache RespondTo:self];
	}
	else 
	{
		if(rptDetail != nil)
        {
            [self hideWaitView];
			return;
        }

		NSMutableDictionary *pBaggie = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rptData, @"REPORT", rptData.rptKey, @"ID_KEY", rptData.rptKey, @"RECORD_KEY",
									 APPROVE_ENTRIES, @"TO_VIEW", nil];
        BOOL skipCache = [ExSystem connectedToNetwork];
        [[ExSystem sharedInstance].msgControl createMsg:APPROVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBaggie SkipCache:skipCache RespondTo:self];
	}
}


#pragma mark -
#pragma mark MobileViewController Methods
-(void) addReportHeaderData:(int)sectionPos
{
	[aSections removeAllObjects];
	
	if(rptDetail.exceptions != nil && [rptDetail.exceptions count] > 0)
	{
		//make exceptions
	}
	
	[self makeEntries];
}

-(void) resortEntries
{
    NSMutableArray   *unsortedEntries = [[NSMutableArray alloc] init];
    for (NSString* key in rptDetail.keys)
    {
        [unsortedEntries addObject:(rptDetail.entries)[key]];
    }
    NSSortDescriptor *dateDescriptor = [[NSSortDescriptor alloc] initWithKey:@"transactionDate" ascending:NO];
    NSArray *sortDescriptors = @[dateDescriptor];
    
    NSArray *sortedEntries = [unsortedEntries sortedArrayUsingDescriptors:sortDescriptors];
    
    NSMutableArray* sortedKeys = [[NSMutableArray alloc] init];
    for (EntryData* e in sortedEntries)
    {
        [sortedKeys addObject:e.rpeKey];
    }
    rptDetail.keys = sortedKeys;
}

-(void)makeEntries
{
    [self resortEntries];
    
	[aSections removeAllObjects];
	for(NSString *key in rptDetail.keys)
	{
		EntryData *e = (rptDetail.entries)[key];
		[aSections addObject:e];
		[self makeEntryDetails:e];
	}
}

-(void)respondToFoundData:(Msg *)msg
{
    // MOB-16008 after report submission the home screen count fails to update.
    // We forgot to call the super respondToFoundData which handles refresh of home...
    [super respondToFoundData:msg];
    
    if ([msg.idKey isEqualToString:RECALL_REPORT_DATA])
    {
        [super respondToFoundData:msg];
    }
	else if ([msg.idKey isEqualToString:DELETE_REPORT_ENTRY_DATA])
	{
        if ([self isViewLoaded])
            [self hideWaitView];
        
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
			self.rpt =  srd.rpt;
			self.rptDetail = self.rpt;
			//self.entries = rpt.entries;
			self.aKeys = rpt.keys;
			
			//[self drawHeaderRpt:rpt HeadLabel:headLabelName AmountLabel:headLabelAmount LabelLine1:headLabel1 LabelLine2:headLabel2 HeadBackground:headBack];
			[self makeEntries];
			[tableList reloadData];
			
//			ReportData *rptData = [pBag objectForKey:@"REPORT"];
//			
//			NSString *comingFrom = [pBag objectForKey:@"COMING_FROM"];
            if ((msg.parameterBag)[@"REPORT_KEYS"]!= nil && (msg.parameterBag)[@"REPORT_DICT"] != nil)
            {
                self.reportKeys = (msg.parameterBag)[@"REPORT_KEYS"];
                self.reportDictionary = (msg.parameterBag)[@"REPORT_DICT"];
            }
			NSMutableDictionary *pBagNew = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", @"REPORT", @"COMING_FROM"
											, @"Y", @"SKIPCACHE"
											, self.reportKeys, @"REPORT_KEYS"
											,self.reportDictionary, @"REPORT_DICT", nil];
			[self loadReport:pBagNew];
			
//			if([comingFrom isEqualToString:@"REPORT"])
//			{
//				NSMutableDictionary *pBagNew = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rptData, @"REPORT", rptData.rptKey, @"ID_KEY", rptData.rptKey, @"RECORD_KEY",
//												ACTIVE_ENTRIES, @"TO_VIEW", [pBag objectForKey:@"REPORT_KEYS"], @"REPORT_KEYS", [pBag objectForKey:@"REPORT_DICT"], @"REPORT_DICT", nil];
//				
			
			// MOB-1868
			//[rootViewController.viewState setObject:@"FETCH" forKey:ACTIVE_REPORTS];
		}
	}
	else if ([msg.idKey isEqualToString:SAVE_REPORT_RECEIPT2])
	{
		if ([self isViewLoaded])
		{
			[self hideWaitView];
			
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
										 ACTIVE_ENTRIES, @"TO_VIEW", nil];
			[self loadReport:pBag];
		}
	}
	else if ([msg.idKey isEqualToString:APPROVE_REPORTS_DATA])
	{
        //this is the entry point for send back and also for approve
		if ([self isViewLoaded])
		{
            [self hideWaitView];
            // MOB-13494
            NSString* errMsg = msg.errBody;
            if (errMsg == nil && msg.responder != nil)
            {
                ApproveReportsData* srd = (ApproveReportsData*) msg.responder;
                if (srd.reportStatus != nil && srd.reportStatus.errMsg != nil)
                {
                    errMsg = srd.reportStatus.errMsg;
                }
            }

			if (errMsg != nil) 
			{
				UIAlertView *alert = [[MobileAlertView alloc] 
									  initWithTitle:msg.errCode
									  message:errMsg
									  delegate:nil 
									  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
									  otherButtonTitles:nil];
				[alert show];
                
			}
			else 
			{
				[[ExSystem sharedInstance].cacheData removeCache:APPROVE_REPORT_DETAIL_DATA UserID:[ExSystem sharedInstance].userName RecordKey:rpt.rptKey];
	//			parameterBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"POP_TO_ROOT_VIEW", nil];
	//			[ConcurMobileAppDelegate switchToView:APPROVE_REPORTS viewFrom:APPROVE_ENTRIES ParameterBag:parameterBag];
	//			[rpt release];
                
                NSString *holdTitle = nil;
				if(![self isApproving] || self.selectedSendBack)
					holdTitle = [NSString stringWithFormat:@"%@ has been sent back to the employee", rpt.reportName];
				else 
					holdTitle = [NSString stringWithFormat:@"%@ has been approved", rpt.reportName];
                
                [self showWaitViewWithText:holdTitle];
				
                //[AppRating offerChoiceToRateApp:self alertTag:kAlertViewRateApp];
                // DISABLE feedback manager for Gov
                if (![Config isGov])
                {
                    [[FeedbackManager sharedInstance] requestRatingFromViewController:self withBlock:^{
                        [self afterChoiceToRateApp];
                    }];
                }
                // Use new homevc and post refresh call if you can
                // Any new home should have refreshsummaryData so it can be called to refresh expenses
                UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
                if ([homeVC respondsToSelector:@selector(refreshSummaryData)])
                {
                    [homeVC performSelector:@selector(refreshSummaryData) withObject:nil];
                }

                // MOB-17661 Loading view shows on home forever
                // Send back approval report->Shows the message as "report is approved" and loading forever.
                [self hideWaitView];
				[self.navigationController popToRootViewControllerAnimated:YES];
                
                // MOB-14243 after approving or sending back a report, it should navigate back to the report list instead of the home screen
                [(iPadHome9VC*)homeVC SwitchToApprovalsView];
			}
		}
	}
	else if ([msg.idKey isEqualToString:SUBMIT_REPORT_DATA]) 
	{//this is the entry point for submit
		inAction = FALSE;
		
		if ([self isViewLoaded])
		{
            [self hideWaitView];
            
            SubmitReportData* srd = (SubmitReportData*) msg.responder;
			NSString* errMsg = msg.errBody;
			if (errMsg == nil && msg.responder != nil)
			{
				if (srd.reportStatus != nil && srd.reportStatus.errMsg != nil)
				{
					if ([srd.reportStatus.status isEqualToString:@"not_an_approver"])
					{
						errMsg = [Localizer getLocalizedText:@"ERROR_ACCOUNT_NOT_CONFIGURED"];
					}
					else if ([srd.reportStatus.status isEqualToString:@"error.submit.missing_reqd_fields"])
					{
						errMsg = [Localizer getLocalizedText:@"ERROR_REQUIRED_FIELDS_MISSING"];
					}
					else
					{
						errMsg = srd.reportStatus.errMsg;
					}
				}
			}
			
            if (srd != nil && 
                ([srd.reportStatus.status isEqualToString:@"review_approval_flow_approver"] ||
                 [srd.reportStatus.status isEqualToString:@"no_approver"]))
            {
                [self searchApprovers:srd];
            }
			else if (errMsg != nil) 
			{
				//[self setupToolbar];
				
				UIAlertView *alert = [[MobileAlertView alloc] 
									  initWithTitle:[Localizer getLocalizedText:@"ERROR_SUBMIT_FAILED"]
									  message:errMsg
									  delegate:nil 
									  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
									  otherButtonTitles:nil];
				[alert show];
			}
			else 
            {
                [self showWaitViewWithText:[NSString stringWithFormat:[Localizer getLocalizedText:@"%@ has been submitted"], rpt.reportName]];
                
                //[AppRating offerChoiceToRateApp:self alertTag:kAlertViewRateApp];
                // DISABLE feedback manager for Gov
                if (![Config isGov])
                {
                    [[FeedbackManager sharedInstance] requestRatingFromViewController:self withBlock:^{
                        [self afterChoiceToRateApp];
                    }];
                }
                
                if ([UIDevice isPad]) // MOB-6256
                {
                    iPadHomeVC* padVC = [ConcurMobileAppDelegate findiPadHomeVC];
                    [padVC forceReload];
                }
                [self refreshReportList]; // MOB-6374

			}
			// To refresh to get submit exception
			// Uncomment b/c MOB-4329
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
										 [self getViewIDKey], @"TO_VIEW", nil];
			
			[[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
		}
	}
	else if([msg.idKey isEqualToString:APPROVE_REPORT_DETAIL_DATA] || [msg.idKey isEqualToString:ACTIVE_REPORT_DETAIL_DATA])
	{
		if ([self isViewLoaded])
			[self hideWaitView];
            
        ReportDetailDataBase *rad = (ReportDetailDataBase *)msg.responder;
        if ([rad.keys count] > 0) 
        {
            NSString *rptKey = (rad.keys)[0];

            self.rptDetail = (rad.rpts)[rptKey];
            if ((msg.parameterBag)[@"REPORT"] != nil)
                self.rpt = (msg.parameterBag)[@"REPORT"];
            
            rpt.apsKey = rptDetail.apsKey;
        }
        else 
        {
            if ((msg.parameterBag)[@"REPORT"] != nil)
            {
                self.rpt = (msg.parameterBag)[@"REPORT"];
            }
            if ([msg.idKey isEqualToString:ACTIVE_REPORT_DETAIL_DATA] && rad.rpt != nil)
            {
                self.rptDetail = rad.rpt;
            }
        }
        
        if ((msg.parameterBag)[@"REPORT_KEYS"]!= nil && (msg.parameterBag)[@"REPORT_DICT"] != nil)
        {
            self.reportKeys = (msg.parameterBag)[@"REPORT_KEYS"];
            self.reportDictionary = (msg.parameterBag)[@"REPORT_DICT"];
        }
        
        //[self setTBUp:rptDetail.employeeName ReportAmount:[FormatUtils formatMoney:rptDetail.totalPostedAmount crnCode:rptDetail.crnCode] ReportName:rptDetail.reportName
           //HasReceipt:hasReceipt HasComment:hasComment];
        
        [self addReportHeaderData: 0]; // Creates the entries array.  Does not require view to be loaded.
        
        //todo: we need to unload the popover if it is showing. [self.popoverController dismissPopoverAnimated:YES]
        //[tableList reloadData];
        //[self setUpPreviousNextReports];
        
        //NSLog(@"wiz = %@", [msg.parameterBag objectForKey:@"REPORT_CREATE_WIZARD"]);
        BOOL isWizard = [@"YES" isEqualToString:(msg.parameterBag)[@"REPORT_CREATE_WIZARD"]];
        if (isWizard)
        {
            //[self performSelector:@selector(buttonAddEntryTapped:) withObject:nil afterDelay:0.5f]; // buttonAddEntryTapped:nil];
            [self openAddExpenseActionSheet];
        }
        
        // MOB-4765 - preload expense types to see which expense can itemize
        ExpenseTypesManager* etMgr = [ExpenseTypesManager sharedInstance];
        [etMgr loadExpenseTypes:self.rpt.polKey msgControl:[ExSystem sharedInstance].msgControl];			
	}
	else if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] || (msg.parameterBag)[@"REPORT_DETAIL"] != nil)
	{
        if ([self isViewLoaded])
            [self hideWaitView];
        
		ReportData* oldRpt = self.rpt;
		self.rptDetail = (msg.parameterBag)[@"REPORT_DETAIL"];
		self.rptDetail.receiptUrl = oldRpt.receiptUrl;
		self.rpt = self.rptDetail;
		[self setTBUp:rptDetail.employeeName ReportAmount:[FormatUtils formatMoney:rptDetail.totalPostedAmount crnCode:rptDetail.crnCode] ReportName:rptDetail.reportName
		   HasReceipt:[self hasReceipt] HasComment:hasComment];
		
		[self addReportHeaderData: 0];
		
        if ([self isViewLoaded])
        {
            [self setUpPreviousNextReports];
        }
	}
	else if ([msg.idKey isEqualToString:REPORT_ENTRY_FORM_DATA])
	{
        if ([self isViewLoaded])
            [self hideLoadingView];
        
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

            [resp.rpt.entry createDefaultAttendeeUsingExpenseTypeVersion:@"V3" policyKey:self.rpt.polKey forChild:NO];
            
            self.pBagAddExpense = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"SHORT_CIRCUIT", self.rpt, @"REPORT", resp.rpt.entry, @"ENTRY", 
                                    nil];
            
            if (self.role != nil)
                pBagAddExpense[@"ROLE"] = self.role;
            
            [self nextStepForAddExpense];
        }
	} 
    else if ([msg.idKey isEqualToString:REPORT_ENTRY_DETAIL_DATA])
    {
		ReportDetailDataBase *rad = (ReportDetailDataBase *)msg.responder;
		EntryData* newEntry = rad.rpt.entry;
		if (newEntry != nil)
        {
            for (int ix = 0; ix < [aSections count]; ix++)
            {
                EntryData *entry = aSections[ix];
                if ([entry.rpeKey isEqualToString:newEntry.rpeKey])
                {
                    (self.rpt.entries)[newEntry.rpeKey] = newEntry;
                    (self.rptDetail.entries)[newEntry.rpeKey] = newEntry;
                    aSections[ix] = newEntry;

                    [_dictExpensesToFetch removeObjectForKey:newEntry.rpeKey];

                    //NSLog(@"####Reload row %d", ix);
                    if ([_dictExpensesToFetch count] ==0)
                    {
                        //NSLog(@"####Reload table");
                        [tableList reloadData];
                    }
                    break;
                }
            }
        }
    }
    else if ([msg.idKey isEqualToString:@"UPLOAD_IMAGE_DATA"])
    {
        // MOB-10130 need to handle failure here
        if ([self isViewLoaded])
        {
            [self hideWaitView];
        }
        UploadReceiptData *receiptData = (UploadReceiptData*)msg.responder;
        // MOB-9414 check for error status 
        if (msg.errBody != nil || msg.responseCode >= 400 || ![receiptData.returnStatus isEqualToString:@"SUCCESS"]) 
        {
            // MOB-9653 handle imaging not configured error
            if (![ExReceiptManager handleImageConfigError:msg.errBody])
            {
                NSString *errMsg = msg.errBody != nil? msg.errBody : 
                [Localizer getLocalizedText:@"ReceiptUploadFailMsg"];
                
                UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload failed"] 
                                                                    message:errMsg 
                                                                   delegate:nil 
                                                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
                                                          otherButtonTitles:
                                      nil];
                [alert show];
            }
        }
    }
    [self updateViews];
}

-(void)nextStepForAddExpense
{
	if (self.etDlgDismissed && self.pBagAddExpense != nil)
	{
		[ReportDetailViewController showEntryView:self withParameterBag: self.pBagAddExpense carMileageFlag:NO];
	}
}
-(void)expenseTypeDlgDismissed
{
	self.etDlgDismissed = YES;
	[self nextStepForAddExpense];
}

-(void)showOfflineView:(MobileViewController*)callingViewController
{
    // Look at the call stack when this method is called.  Note that iPadHomeVC's switchToDetail method is on it.  That method called the framework to push this VC onto the call stack and has not yet returned.  Therefore, we cannot yet call the framework to pop this VC from the call stack.
    [self performSelector:@selector(showOfflineViewImpl:) withObject:self afterDelay:0.5f];
}

-(void)showOfflineViewImpl:(MobileViewController*)callingViewController
{
    MobileAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"View Report Offline"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
    [alert show];

    [iPadHome popHome:self];
}

-(void) respondToConnectionFailure:(Msg* )msg
{
    if ([self isWaitViewShowing] && ([msg.idKey isEqualToString:ACTIVE_REPORT_DETAIL_DATA] || [msg.idKey isEqualToString:APPROVE_REPORT_DETAIL_DATA]))
    {
        [self hideWaitView];
        [self showOfflineView:self];
    }
    else
    {
        [super respondToConnectionFailure:msg];
    }
}
#pragma mark - UITableViewDataSource Methods

//
// In order to get a gap to appear betwen rows, there will be one section per expense and each section will have only 1 row.  The gap is actually the section header with the same background color as the view behind the table.
//
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [aSections count];
    
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	EntryData *e = aSections[section];
    return (e == nil ? 0 : 1);
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    int section = [indexPath section];
	EntryData *entry = aSections[section];

    BOOL hasComments = [entry.hasComments isEqualToString:@"Y"];
    BOOL hasAttendees = [entry.hasAttendees isEqualToString:@"Y"];
    BOOL needsCellExtension =  hasComments || hasAttendees ;
    
    // Attendee data is not in the entryData so make a call here if there are attendees
    if (![entry isDetail] && hasAttendees)
    {
        [self fetchEntryDetail:entry];
        _dictExpensesToFetch[entry.rpeKey] = entry.rpeKey;
    }

    
    ReportDetailCell_iPad *cell = (ReportDetailCell_iPad*)[tableView dequeueReusableCellWithIdentifier: @"ReportDetailCell_iPad"];   
    if (cell == nil)
        cell = (ReportDetailCell_iPad*)[[NSBundle mainBundle] loadNibNamed:@"ReportDetailCell_iPad" owner:self options:nil][0];
    
    NSString *description = @"";
    if (entry.vendorDescription != nil && entry.locationName != nil)
    {
        description = [NSString stringWithFormat:@"%@, %@", entry.vendorDescription, entry.locationName];
    }
    else if (entry.vendorDescription != nil)
        description = entry.vendorDescription;
    else if (entry.locationName != nil)
        description = entry.locationName;
    
    cell.nameLabel.text = entry.expName;
    cell.dateLabel.text = [CCDateUtilities formatDateToMMMddYYYFromString:entry.transactionDate];
    cell.descriptionLabel.text = description;
    
    NSString *currencyFontName = @"HelveticaNeue-Bold";
    float currencyFontSize = 17.0;
    
    NSString *amountValue = [FormatUtils formatMoney:entry.transactionAmount crnCode:entry.transactionCrnCode];
    NSString *amountLabelText = [NSString stringWithFormat:@"%@\n", [Localizer getLocalizedText:@"Amount"]];
    NSObject *amountAttributedString = [self attributedStringWithAmountForLabel:cell.amountLabel labelText:amountLabelText valueText:amountValue fontName:currencyFontName fontSize:currencyFontSize];

    if ([ExSystem is6Plus])
        cell.amountLabel.attributedText = (NSAttributedString*)amountAttributedString;
    else
        cell.amountLabel.text = (NSString*)amountAttributedString;
    
    // MOB-12924 expense entry 'amount' and 'requested amount' in different currency
    // rqeuested amount currency should be the same with the report 'total requested amount'
    NSString *requestedValue = [FormatUtils formatMoney:entry.approvedAmount crnCode:rptDetail.crnCode];
    NSString *requestedLabelText = [NSString stringWithFormat:@"%@\n", [Localizer getLocalizedText:@"Requested"]];
    NSObject *requestedAttributedString = [self attributedStringWithAmountForLabel:cell.requestedLabel labelText:requestedLabelText valueText:requestedValue fontName:currencyFontName fontSize:currencyFontSize];
    
    if ([ExSystem is6Plus])
        cell.requestedLabel.attributedText = (NSAttributedString*)requestedAttributedString;
    else
        cell.requestedLabel.text = (NSString*)requestedAttributedString;
    
    float newHeight = [self tableView:tableView heightForRowAtIndexPath:indexPath];
    cell.frame = CGRectMake(0, 0, cell.frame.size.width, newHeight);
    
    cell.separatorLabel.hidden = !needsCellExtension;
    cell.extraLabel0.hidden = YES;
    cell.extraLabel1.hidden = YES;
    
    if (needsCellExtension)
    {
        NSString *textFontName = @"Helvetica Neue";
        float textFontSize = 13.0;
        NSString *commentText = nil;
        
        // Add appended comments text
        if(hasComments)
        {
            commentText = [self fetchSubValue:entry SubType:@"COMMENTS"];

            if (commentText != nil)
            {
                UILabel *commentsLabel = cell.extraLabel0;
                commentsLabel.hidden = NO;
                
                NSString *lbl = [NSString stringWithFormat:@"%@: ", [Localizer getLocalizedText:@"Comments"]];
                
                NSObject *commentAttributedString = [self attributedStringWithAmountForLabel:commentsLabel labelText:lbl valueText:commentText fontName:textFontName fontSize:textFontSize];
                
                if ([ExSystem is6Plus])
                    commentsLabel.attributedText = (NSAttributedString*)commentAttributedString;
                else
                    commentsLabel.text = (NSString*)commentAttributedString;
            }
        }
        // Add appended attendees text under comments if present 
		if (hasAttendees)
		{
            UILabel *attendeesLabel = (commentText == nil ? cell.extraLabel0 : cell.extraLabel1);
            attendeesLabel.hidden = NO;
            
			NSString *val = [self fetchSubValue:entry SubType:@"ATTENDEES"];
            NSString *lbl = [NSString stringWithFormat:@"%@: ", [Localizer getLocalizedText:@"Attendees"]];
			
            NSObject *attendeesAttributedString = [self attributedStringWithAmountForLabel:attendeesLabel labelText:lbl valueText:val fontName:textFontName fontSize:textFontSize];
 
            if ([ExSystem is6Plus])
                attendeesLabel.attributedText = (NSAttributedString*)attendeesAttributedString;
            else
                attendeesLabel.text = (NSString*)attendeesAttributedString;
        }
    }
    
    cell.iv0.image = nil;
    cell.iv1.image = nil;
	cell.iv2.image = nil;
	
	int iImagePos = 0;
	
	if((entry.hasExceptions != nil && [entry.hasExceptions isEqualToString:@"Y"]) || [entry.exceptions count] > 0)
	{
		BOOL showAlert = NO;
		for(ExceptionData *ed in entry.exceptions)
		{
			if([ed.severityLevel isEqualToString:@"ERROR"])
			{
				showAlert = YES;
				break;
			}
		}
		
		entry.hasExceptions = @"Y";
		
		if (showAlert)
			[self setImageByPos:iImagePos TableCell:cell ImageName:@"icon_redex"];
		else
			[self setImageByPos:iImagePos TableCell:cell ImageName:@"icon_yellowex"];
        
		iImagePos++;
	}
	
    cell.nameLabel.textColor = [UIColor blackColor];
    cell.amountLabel.textColor = [UIColor blackColor];
    // MOB-13616 AMEX preauth transaction
    if([ReportViewControllerBase isCardAuthorizationTransaction:entry])
    {
		[self setImageByPos:iImagePos TableCell:cell ImageName:@"icon_card_gray"];
        cell.nameLabel.textColor = [UIColor grayColor];
        cell.amountLabel.textColor = [UIColor grayColor];
		iImagePos++;
    }
	else if((entry.isCreditCardCharge != nil && [entry.isCreditCardCharge isEqualToString:@"Y"]) ||
       (entry.isPersonalCardCharge != nil && [entry.isPersonalCardCharge isEqualToString:@"Y"]))
	{
		[self setImageByPos:iImagePos TableCell:cell ImageName:@"icon_card_blue"];
		iImagePos++;
	}
	
    BOOL gotReceipt = (entry.hasMobileReceipt != nil && [entry.hasMobileReceipt isEqualToString:@"Y"]);
	if (gotReceipt)
	{
		[self setImageByPos:iImagePos TableCell:cell ImageName:@"icon_receipt"];
        [cell.receiptButton addTarget:self action:@selector(receiptButtonPressed:) forControlEvents:UIControlEventTouchUpInside];
	}
    else if ((entry.receiptRequired != nil && [entry.receiptRequired isEqualToString:@"Y"]) ||
             (entry.imageRequired != nil && [entry.imageRequired isEqualToString:@"Y"]))
        [self setImageByPos:iImagePos TableCell:cell ImageName:@"icon_receiptrequired_entry"]; // MOB-8756
    
    cell.receiptButton.hidden = !gotReceipt;
    cell.viewReceiptLabel.hidden = !gotReceipt;
    
    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    return cell;
}

#pragma mark - UITableViewDelegate Methods
- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return GAP_BETWEEN_TABLE_ROWS;
}

-(UIView*)tableView:(UITableView*)tableView viewForHeaderInSection:(NSInteger)section
{
    UIView* tableHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, tableView.frame.size.width, GAP_BETWEEN_TABLE_ROWS)];
    tableHeaderView.backgroundColor = tableView.superview.backgroundColor;
    return tableHeaderView;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    BOOL needsCellExtension = [self shouldShowCellExtension:indexPath];
    return (needsCellExtension ? 162 : 112);
}

- (BOOL) shouldShowCellExtension:(NSIndexPath*)indexPath
{
    int row = indexPath.section;
    NSString *key = (rptDetail.keys)[row];
    EntryData *entry = (rptDetail.entries)[key];

    return [entry.hasComments isEqualToString:@"Y"] || [entry.hasAttendees isEqualToString:@"Y"];
}

-(NSString *) fetchSubValue:(EntryData *)e SubType:(NSString *)subType
{
	NSString *rVal = @"";
	
	if([subType isEqualToString:@"COMMENTS"])
	{
		if([e.comments count] > 0)
		{
			NSString *key = (e.commentKeys)[0];
			CommentData *c = (e.comments)[key];
			rVal = [NSString stringWithFormat:@"%@", c.comment];
		}
	}
	else if([subType isEqualToString:@"ATTENDEES"])
	{
		if([e.attendees count] > 0)
		{
			NSMutableString *s = [[NSMutableString alloc] initWithString:@""];
			for(NSString *key in e.attendees)
			{
				AttendeeData *a = (e.attendees)[key];
				if([s length] > 0)
					[s appendString:@", "];
				
				[s appendString:[NSString stringWithFormat:@"%@", [a getFullName]]];

			}
			rVal = [NSString stringWithFormat:@"%@", s];
		}
	}
	else if([subType isEqualToString:@"EXCEPTIONS"])
	{
		if([e.exceptions count] > 0)
		{
			ExceptionData *a = (e.exceptions)[0];
			
			NSMutableString *s = [[NSMutableString alloc] initWithString:@""];
			[s appendString:[NSString stringWithFormat:@"%@", a.exceptionsStr]];
			 rVal = [NSString stringWithFormat:@"%@", s];
			 
//			for(NSString *key in e.exceptions)
//			{
//				ExceptionData *a = [e.exceptions objectForKey:key];
////				if([s length] > 0)
////					[s appendString:@", "];
////				
//				[s appendString:[NSString stringWithFormat:@"%@", a.exceptionsStr]];
//				break;
//			}
//			rVal = [NSString stringWithFormat:@"%@", s];
//			[s release];
		}
	}
	else if([subType isEqualToString:@"ITEMIZATIONS"])
	{
//		if([e.comments count] > 0)
//		{
//			NSString *key = [e.commentKeys objectAtIndex:0];
//			CommentData *c = [e.comments objectForKey:key];
//			rVal = [NSString stringWithFormat:@"%@...", c.comment];
//		}
	}
		
	return rVal;
}

- (void) fetchEntryDetail:(EntryData*) entry
{
    // If entry does not have form fields, then fetch entry detail 
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 self.rpt.rptKey, @"RPT_KEY",
								 entry.rpeKey, @"RPE_KEY",
								 self.role, @"ROLE_CODE",
								 [self getViewIDKey], @"TO_VIEW", nil];
	[[ExSystem sharedInstance].msgControl createMsg:REPORT_ENTRY_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

//MOB-9310 long comment overlapping.
-(ReportData*) getReportData
{
    return self.rptDetail;
}

#pragma mark - Receipt Button
-(void) receiptButtonPressed:(id)sender
{
    UIButton *button = (UIButton*)sender;
    CGRect buttonFrameInTableView = [button convertRect:button.bounds toView:self.rightTableView];
    NSIndexPath *indexPath = [self.rightTableView indexPathForRowAtPoint:buttonFrameInTableView.origin];

    NSString *key = (rptDetail.keys)[indexPath.section];
    EntryData *entry = (rptDetail.entries)[key];
    
    [self buttonReceiptTapped:entry IndexPath:indexPath];
}

#pragma mark Label Adjustment
-(void) adjustLabel:(UILabel *) lblHeading LabelValue:(UILabel*) lblVal HeadingText:(NSString *) headText ValueText:(NSString *) valText ValueColor:(UIColor *) color
{
	[lblHeading setHidden:NO];
	[lblVal setHidden:NO];
	
	[lblVal setNumberOfLines:1];
	[lblHeading setNumberOfLines:1];
	
	NSString *val = [NSString stringWithFormat:@"%@ ", headText];
	lblHeading.text = val;// [NSString stringWithFormat:@"%@: %@", segRow2.rowLabel, segRow2.rowValue];
	
	CGSize lblSize = [val sizeWithFont:lblHeading.font];
	float x = lblHeading.frame.origin.x + lblSize.width + 5;
	
	float w = (lblHeading.frame.size.width - lblSize.width);
	lblVal.frame = CGRectMake(x, lblVal.frame.origin.y, w, lblVal.frame.size.height);
	lblVal.text = valText;
	
	if(color == nil)
		lblVal.textColor = [UIColor blackColor];
	else 
		lblVal.textColor = color;
}

#pragma mark -
#pragma mark Table View Delegate Methods
//- (NSString *)tableView:(UITableView *)tableView 
//titleForHeaderInSection:(NSInteger)section
//{
////    NSDate *key = [aSectionHeaders objectAtIndex:section];
////    return key;
//	if(section == 0)
//		return @"Expenses";
//	else 
//		return @"";
//}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    [self.rightTableView deselectRowAtIndexPath:newIndexPath animated:NO];
    
	NSUInteger section = [newIndexPath section];
    NSUInteger row = [newIndexPath row];
	
	if(row == 0)
	{
		EntryData *e = aSections[section];
		[self buttonEntryDetailsTapped:e IndexPath:newIndexPath];
	}
	else {

		EntryData *entry = aSections[section];
		NSMutableArray *a = entryDetailsDict[entry.rpeKey];
		if(a != nil)
		{
			NSString *rowTitle = a[(row -1)];
			if([rowTitle isEqualToString:@"ATTENDEES"])
				[self buttonAttendeesTapped:self IndexPath:newIndexPath];
			else if([rowTitle isEqualToString:@"ITEMIZATIONS"])
				[self buttonItemizationsTapped:self IndexPath:newIndexPath];
			else if([rowTitle isEqualToString:@"COMMENTS"])
				[self buttonCommentsTapped:self IndexPath:newIndexPath];
			else if([rowTitle isEqualToString:@"EXCEPTIONS"])
				[self buttonExceptionsTapped:self IndexPath:newIndexPath];
			else if([rowTitle isEqualToString:@"RECEIPT"])
				[self buttonReceiptTapped:entry IndexPath:newIndexPath];
		}
	}

}

- (NSIndexPath *)tableView:(UITableView *)tableView willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView reloadData];
    return indexPath;
}

// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
		NSUInteger section = [indexPath section];
		NSUInteger row = [indexPath row];
		if (row == 0) 
		{
            [self showWaitViewWithText:[Localizer getLocalizedText:@"Removing entry"]];
            
			EntryData *entry = aSections[section];
			//NSMutableArray *a = [entryDetailsDict objectForKey:entry.rpeKey];
			
			NSArray * rpeKeys = @[entry.rpeKey];
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
	//NSUInteger section = [indexPath section];
	NSUInteger row = [indexPath row];
	if (row == 0 && [self canSubmit] ) 
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
	[self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
	
    if([et isCompanyCarMileage] || [et isPersonalCarMileage])
    {
        NSDictionary *dictionary = @{@"Add from": @"Report Add Expense"};
        [Flurry logEvent:@"Car Mileage: Add from" withParameters:dictionary];

    }
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 et.expKey, @"EXP_KEY",
								 rpt.rptKey, @"RPT_KEY", 
								 //								 entry.rpeKey, @"PARENT_RPE_KEY",
								 nil];
	[[ExSystem sharedInstance].msgControl createMsg:REPORT_ENTRY_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    
   	[self dismissViewControllerAnimated:YES completion:nil];
}



#pragma mark -
#pragma mark PopOver
- (void)cancelSelected
{
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
	[self.oopePopOver dismissPopoverAnimated:YES];
#endif
}


#pragma mark -
#pragma mark Selection Toolbar Management
#define kButtonWidth 100
-(void)makeDeleteButton:(int)count
{

	
}

#pragma mark -
#pragma mark Toolbar Setup
-(void)setTBUp:(NSString *)empName ReportAmount:(NSString *)rptAmount ReportName:(NSString *)reportName HasReceipt:(BOOL)fHasReceipt HasComment:(BOOL)fHasComment
{

}

#pragma mark -
#pragma mark Entry Cell Creation
-(void)makeEntryCell:(PadExpenseEntryCell *)cell Entry:(EntryData *)entry
{	
	if (entry.expKey != nil && [entry.expKey isEqualToString:@"UNDEF"])
	{
		cell.lblExpenseType.textColor = [UIColor redColor];
	}
	else {
		cell.lblExpenseType.textColor = [UIColor blackColor];
	}

	cell.lblExpenseType.text = entry.expName;
	cell.lblAmount.text = [FormatUtils formatMoney:entry.transactionAmount crnCode:entry.transactionCrnCode];
	cell.lblRequest.text = [FormatUtils formatMoney:entry.approvedAmount crnCode:self.rpt.crnCode];
	cell.lblLine1.text = [CCDateUtilities formatDateToMMMddYYYFromString:entry.transactionDate];

	[cell setAccessoryType:UITableViewCellAccessoryNone];
	
	cell.lblLine2.text = [self getVendorString:entry.vendorDescription WithLocation:entry.locationName];
	
	cell.iv1.image = nil;
	cell.iv2.image = nil;
	cell.iv3.image = nil;
	cell.iv4.image = nil;
	cell.iv5.image = nil;
	
	int iImagePos = 0;
	//int extraCount = 0;
	
	if((entry.hasExceptions != nil && [entry.hasExceptions isEqualToString:@"Y"]) || [entry.exceptions count] > 0)
	{
		BOOL showAlert = NO;
		for(ExceptionData *ed in entry.exceptions)
		{
			if([ed.severityLevel isEqualToString:@"ERROR"])
			{
				showAlert = YES;
				break;
			}
		}
		
		entry.hasExceptions = @"Y";
		
		if (showAlert) 
			[self setImageByPos:iImagePos TableCell:(ReportDetailCell_iPad *)cell ImageName:@"icon_redex"];
		else 
			[self setImageByPos:iImagePos TableCell:(ReportDetailCell_iPad *)cell ImageName:@"icon_yellowex"];

		iImagePos++;
	}
	

	// Personal card and corporate card is mutually exclusive
	if((entry.isCreditCardCharge != nil && [entry.isCreditCardCharge isEqualToString:@"Y"]) ||
       (entry.isPersonalCardCharge != nil && [entry.isPersonalCardCharge isEqualToString:@"Y"]))
	{
		[self setImageByPos:iImagePos TableCell:(ReportDetailCell_iPad *)cell ImageName:@"icon_card"];
		iImagePos++;
	}
	
	if(entry.hasMobileReceipt != nil && [entry.hasMobileReceipt isEqualToString:@"Y"])
	{
		[self setImageByPos:iImagePos TableCell:(ReportDetailCell_iPad *)cell ImageName:@"icon_receipt"];
		
		//iImagePos++;
	}
    else if ((entry.receiptRequired != nil && [entry.receiptRequired isEqualToString:@"Y"]) ||
             (entry.imageRequired != nil && [entry.imageRequired isEqualToString:@"Y"]))
        [self setImageByPos:iImagePos TableCell:(ReportDetailCell_iPad *)cell ImageName:@"icon_receiptrequired_entry"]; // MOB-8756
	
}

-(void)makeEntryDetails:(EntryData *)entry
{	
	NSMutableArray *a = [[NSMutableArray alloc] initWithObjects:nil];
	
	//exception, comments, credit card, itemization, attendees
	
	if(entry.hasMobileReceipt != nil && [entry.hasMobileReceipt isEqualToString:@"Y"])
	{
		[a addObject:@"RECEIPT"];
	}
	
	entryDetailsDict[entry.rpeKey] = a;
	
}

- (NSString*) getVendorString:(NSString*) vendor WithLocation:(NSString*) locationName
{
	vendor = [vendor stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
	locationName = [locationName stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
	
	//vendor = [vendor stringByReplacingOccurrencesOfString:@"\n" withString:@"" options:0 range:NSMakeRange(0, 1)];
	//NSLog(@"pre%@post lend=%d", vendor, [vendor length]);
	if ((vendor == nil||[vendor length]==0) && (locationName == nil || [locationName length]==0))
	{
		return @"";
	} else if (vendor == nil||[vendor length]==0)
	{
		return locationName;
	} else if (locationName == nil|| [locationName length]==0)
	{
		return vendor;
	} else {
		return [NSString stringWithFormat:@"%@, %@", vendor, locationName];
	}
}

-(void) setImageByPos:(int)imagePos TableCell:(ReportDetailCell_iPad *)cell ImageName:(NSString *)imgName
{
	UIImage *img = [UIImage imageNamed:imgName];
	switch (imagePos)
	{
		case 0:
			cell.iv0.image = nil;
			cell.iv0.image = img;
			break;
		case 1:
			cell.iv1.image = nil;
			cell.iv1.image = img;
			break;
		case 2:
			cell.iv2.image = nil;
			cell.iv2.image = img;
			break;
		default:
			break;
	}
}


#pragma mark -
#pragma mark PopOver Methods
- (void)buttonEntryDetailsTapped:(EntryData *)e IndexPath:(NSIndexPath *)newIndexPath
{
	
	[self loadEditableEntry:e IndexPath:newIndexPath];
	return;
}

-(void) loadEditableEntry:(EntryData *)e IndexPath:(NSIndexPath *)newIndexPath
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] 
								 initWithObjectsAndKeys:
								 //@"YES", @"SKIP_PARSE", 
                                 //MOB-11958
								 ![self.role lengthIgnoreWhitespace]? ROLE_EXPENSE_TRAVELER : self.role, @"ROLE",
								 @"YES", @"SHORT_CIRCUIT",
								 nil];
	if (rptDetail.employeeName != nil)
		pBag[@"EmployeeName"] = rptDetail.employeeName;
	pBag[@"ReportName"] = rptDetail.reportName;
	pBag[@"TotalPostedAmount"] = rptDetail.totalPostedAmount;
	pBag[@"CrnCode"] = rptDetail.crnCode;
	pBag[@"REPORT"] = rptDetail;
	pBag[@"RECORD_KEY"] = rptDetail.rptKey;
	pBag[@"REPORT_OG"] = rptDetail;
	

	EntryData *entry = e; //[rptDetail.entries objectForKey:key];
	for(NSString *eKey in rptDetail.entries)
	{
		//EntryData *e = [rptDetail.entries objectForKey:eKey];
		//NSLog(@"e.rpeKey = %@", e.rpeKey);
	}
	
	pBag[@"ENTRY"] = entry;
	pBag[@"ID_KEY"] = rptDetail.rptKey;

    [ReportDetailViewController showEntryView:self withParameterBag: pBag carMileageFlag:false];
}

-(void)actionCloseEntryReceipt:(id) sender
{
    if ([sender isKindOfClass:[ReceiptDetailViewController class]])
    {
        [self dismissViewControllerAnimated:YES completion:nil];
    }
    else
        [self dismissViewControllerAnimated:YES completion:nil];
}

// Display mobile receipt
- (void)buttonReceiptTapped:(EntryData*)entry IndexPath:(NSIndexPath *)newIndexPath
{
	ReceiptEditorVC *receiptView = [[ReceiptEditorVC alloc] initWithNibName:@"ReceiptEditorVC" bundle:nil];
    receiptView.title = [Localizer getLocalizedText:@"Receipt"];
    receiptView.delegate = self;
    receiptView.canDelete = NO;
    receiptView.canUpdate = NO;
    receiptView.canUseReceiptStore = [self canUpdateReceipt];
    receiptView.canAppend = [self canUpdateReceipt];
    receiptView.ignoreCache = YES;
    
    Receipt *receipt = [[Receipt alloc] init];
    receipt.receiptId = entry.receiptImageId;
    [receiptView setSeedData:receipt];

    [self.navigationController pushViewController:receiptView animated:YES];
}

-(void) startAddEntry
{
	etDlgDismissed = NO;
	self.pBagAddExpense = nil;
	// Pop up expense types dialog and upon select an expense type, open the new itemization form
   	[ExpenseTypesViewController showExpenseTypeEditor:self policy:self.rpt.polKey parentVC:self selectedExpKey:nil parentExpKey:nil withReport:self.rpt];
}

-(void) importExpenses
{
    QuickExpensesReceiptStoreVC *quickExpensesVC = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    [quickExpensesVC setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:NO allowListEdit:NO];
    quickExpensesVC.reportToWhichToAddExpenses = self.rptDetail;
    quickExpensesVC.requireRefresh = YES;
    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:quickExpensesVC];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;

    [localNavigationController setToolbarHidden:NO];
    
	[self presentViewController:localNavigationController animated:YES completion:nil];
    
}

- (void)buttonTravelAllowanceTappedInRect:(CGRect)rect
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc]
            initWithObjectsAndKeys:
                    //@"YES", @"SKIP_PARSE",
                    //MOB-11958
                    ![self.role lengthIgnoreWhitespace]? ROLE_EXPENSE_TRAVELER : self.role, @"ROLE",
                    @"YES", @"SHORT_CIRCUIT",
                    nil];
    if (rptDetail.employeeName != nil)
        pBag[@"EmployeeName"] = rptDetail.employeeName;
    pBag[@"ReportName"] = rptDetail.reportName;
    pBag[@"TotalPostedAmount"] = rptDetail.totalPostedAmount;
    pBag[@"CrnCode"] = rptDetail.crnCode;
    pBag[@"REPORT"] = rptDetail;
    pBag[@"RECORD_KEY"] = rptDetail.rptKey;
    pBag[@"REPORT_OG"] = rptDetail;

    NSLog(@"buttonTravelAllowanceTappedInRect - rect = ", rect);
    if([self isApproving])
    {
        UIStoryboard* storyboard = [UIStoryboard storyboardWithName:[@"ItineraryApproverStoryboard" storyboardName] bundle:nil];
        ApproverTAViewController *o = (ApproverTAViewController *)[storyboard instantiateViewControllerWithIdentifier:@"ApproverTabbedView"];
        o.paramBag = pBag;
        o.role = self.role;

        o.hasCloseButton = YES;

        UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:o]; //TODO is this necessary?

        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        [localNavigationController setToolbarHidden:NO];
        localNavigationController.toolbar.tintColor = [UIColor darkBlueConcur_iOS6];
        localNavigationController.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];

        [self presentViewController:localNavigationController animated:YES completion:nil];
    }
    else
    {
        UIStoryboard* storyboard = [UIStoryboard storyboardWithName:[@"ItineraryStoryboard" storyboardName] bundle:nil];
        NSLog(@"storyboard.description = %@", storyboard.description);
        if ([self hasTravelAllowanceEntries:self.rpt]) {
            // There are ta entries, so show the action sheet
            MobileActionSheet *sheet = [[MobileActionSheet alloc]     initWithTitle:nil delegate:self cancelButtonTitle:nil destructiveButtonTitle:nil otherButtonTitles:nil];

            sheet.tag = kActionSheetTravelAllowance;

            [sheet addButtonWithTitle:[Localizer getLocalizedText:@"Itineraries"]];
            [sheet addButtonWithTitle:[Localizer getLocalizedText:@"Adjustments"]];

            NSInteger cancelIndex = [sheet addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
            sheet.cancelButtonIndex = cancelIndex;


            [sheet showFromRect:rect inView:self.view animated:YES];
        }
        else
        {
            ItineraryInitialViewController *o = (ItineraryInitialViewController *) [storyboard instantiateViewControllerWithIdentifier:@"ItineraryInitialViewController"];
            o.paramBag = pBag;
            o.hasCloseButton = YES;

            UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:o]; //TODO is this necessary?
//            UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:o];

            localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
            [localNavigationController setToolbarHidden:NO];
            localNavigationController.toolbar.tintColor = [UIColor darkBlueConcur_iOS6];
            localNavigationController.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];

            [self presentViewController:localNavigationController animated:YES completion:nil];
        }
    }
}


-(void) buttonAddEntryTapped:(id)sender
{
    [self buttonAddEntryTappedInRect:CGRectMake(0, 0, 1, 1)];
}

-(void) buttonAddEntryTappedInRect:(CGRect)rect
{
    if (![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"Expenses cannot be added offline"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                              otherButtonTitles:nil];
        [alert show];
        return;
    }
    
    // MOB-21029 - Unable to add new expense because UIActionSheet is not working
    if ([ExSystem is8Plus]) {
        [self showAddExpenseAlertController:rect];
    }
    else {
        // There's a bug in iOS7 where the actionsheet is missing line breaks if the cancelButtonTitle is nil
        // Bring up the action sheet
        UIActionSheet* addExpenseActions = [[MobileActionSheet alloc] initWithTitle:nil
                                                     delegate:self 
                                            cancelButtonTitle:@""
                                       destructiveButtonTitle:nil
                                                                  otherButtonTitles:[Localizer getLocalizedText:@"Add New Expense"], [Localizer getLocalizedText:@"Import Expenses"],
                                            [Localizer getLocalizedText:@"Import and Match"], nil];
        addExpenseActions.tag = kActionViewAddExpense;
        [addExpenseActions showFromRect:rect inView:self.view animated:YES];
    }
}

// UIAlertController is used for iOS8+ only
-(void)showAddExpenseAlertController:(CGRect)rectangle
{
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    UIAlertAction *addNewExpense = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Add New Expense"]
                                                            style:UIAlertActionStyleDefault
                                                          handler:^(UIAlertAction *action){
                                                              DLog(@"Add New Expense clicked");
                                                              [self startAddEntry];
                                                          }];
    [alertController addAction:addNewExpense];
    
    UIAlertAction *importExpenses = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Import Expenses"]
                                                             style:UIAlertActionStyleDefault
                                                           handler:^(UIAlertAction *action){
                                                               DLog(@"Import Expenses clicked");
                                                               [self setSmartExpenseMatching:NO];
                                                               [self importExpenses];
                                                           }];
    [alertController addAction:importExpenses];
    
    UIAlertAction *importAndMatch = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Import and Match"]
                                                             style:UIAlertActionStyleDefault
                                                           handler:^(UIAlertAction *action){
                                                               DLog(@"Import and Match clicked");
                                                               [self setSmartExpenseMatching:YES];
                                                               [self importExpenses];
                                                           }];
    [alertController addAction:importAndMatch];
    
    [alertController setModalPresentationStyle:UIModalPresentationPopover];
    [alertController.popoverPresentationController setSourceView:[self view]];
    [alertController.popoverPresentationController setSourceRect:rectangle];
    [alertController.popoverPresentationController setPermittedArrowDirections:UIPopoverArrowDirectionLeft];
    [self presentViewController:alertController animated:YES completion:nil];
}

- (void)openAddExpenseActionSheet
{
    
/*
 MOB-16997: there's no separation line between the last two rows on action sheet. did the search on line and people said it is probably a ios 7 bug.
 The solution is adding @"" for cancel button
 http://stackoverflow.com/questions/18790868/uiactionsheet-is-not-showing-separator-on-the-last-item-on-ios-7-gm
 */
    UIActionSheet* addExpenseActions = [[MobileActionSheet alloc] initWithTitle:nil
                                                                       delegate:self
                                                              cancelButtonTitle:@""
                                                         destructiveButtonTitle:nil
                                                              otherButtonTitles:[Localizer getLocalizedText:@"Add New Expense"], [Localizer getLocalizedText:@"Import Expenses"],
                                        [Localizer getLocalizedText:@"Import and Match"], nil];
	
	addExpenseActions.tag = kActionViewAddExpense;
	[addExpenseActions showInView:self.view];
}

#pragma mark ReceiptEditorDelegate
-(void) receiptUpdated:(Receipt*) rcpt useV2Endpoint:(BOOL)useV2Endpoint
{
    if (rcpt != nil && rcpt.receiptId != nil)
    {
        //        // MOB-12200 creating new receipt object and only use receiptID.
        //        // This fix "rcpt", "self.receipt" share same memory address. Cause new rcpt.receiptId getting override with old one.
        //        NSString *newReceiptId = rcpt.receiptId;
        //        if (![rcpt.receiptId isEqualToString:self.doc.receiptId] && self.doc.receiptId != nil)
        //        {
        //            // Reset - need to refresh the document level receipt.
        //            self.receipt.receiptId = self.doc.receiptId;
        //            self.receipt.pdfData = nil;
        //            self.receipt.receiptImg = nil;
        //        }
        //        // send out receipt attaching message
        //        [self showWaitView];
        //
        //        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
        //                                     newReceiptId, @"RECEIPT_ID",
        //                                     self.doc.docName, @"DOC_NAME",
        //                                     self.doc.docType, @"DOC_TYPE",
        //                                     nil];
        //        [[ExSystem sharedInstance].msgControl createMsg:GOV_ATTACH_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        
        //        networkCallInProgress = YES;
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


- (void)receiptDisplayed:(Receipt *)rcpt
{
    // MOB-6132
    // MOB-10146 check for PDF as well as image files as entry receipt
    if ([self isApproving] && [rcpt hasReceipt]) {
        // Audit approver has viewed image.
        [self sendViewReceiptAuditMsg];
    }
}

-(BOOL) hasReceipt
{
	return (rpt == nil || rpt.receiptImageAvailable == nil)? FALSE : [rpt.receiptImageAvailable isEqualToString:@"Y"];
}

- (void)showReceiptViewer
{
    
    ReceiptEditorVC *receiptView = [[ReceiptEditorVC alloc] initWithNibName:@"ReceiptEditorVC" bundle:nil];
    receiptView.title = [Localizer getLocalizedText:@"Receipt"];
    receiptView.delegate = self;
    receiptView.canDelete = NO;
    receiptView.canUpdate = NO;
    // MOB-13593 - Force reload the receipts if its from report header. 
    receiptView.ignoreCache = YES;
    receiptView.canAppend = [self canUpdateReceipt];
    receiptView.canUseReceiptStore = [self canUpdateReceipt];
    
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
    
//	if([UIDevice isPad])
        // MOB-8533 allow dismiss keyboard using modal form sheet
    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:receiptView];
    localNavigationController.modalPresentationStyle = UIModalPresentationFullScreen;
    [localNavigationController setToolbarHidden:NO];

    [self presentViewController:localNavigationController animated:YES completion:nil];
}

- (void)buttonReceiptsTapped:(id)sender
{
    [self showReceiptViewer];
}


#pragma mark -
#pragma mark ReportRejectionDelegate Methods
- (void)rejectedWithComment:(NSString*)comment
{
	// Block the action bar while sending back.
    [self hideToolBarActions];

	holdText.text = [NSString stringWithFormat:@"Sending %@ back to employee", rpt.reportName];
    self.selectedSendBack = YES;
	//NSLog(@"rptKey = %@, procKey = %@, currentSeq = %@", rpt.rptKey, rpt.processInstanceKey, rpt.currentSequence);
	
	//NSMutableArray *toolbarItems = [NSArray arrayWithObjects: nil];
	//[self setToolbarItems:toolbarItems animated:YES];
	
	//test bits
	//		Msg *msg = [[Msg alloc] init];
	//		msg.idKey = APPROVE_REPORTS_DATA;
	//		[self respondToFoundData:msg];
	//		[msg release];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
								 APPROVE_ENTRIES, @"TO_VIEW", @"YES", @"SKIP_CACHE",comment, @"SendBackComment", nil];
	[[ExSystem sharedInstance].msgControl createMsg:APPROVE_REPORTS_DATA_REJECT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

/**
 Grays out the toolbar buttons during send back or approve

 MOB-16799 prevent user from doing a double send back or approve.
 */
- (void)hideToolBarActions
{
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    NSArray *toolbarItems = nil;

    UIBarButtonItem *btnApproveReport = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"APPROVE_REPORT"] style:UIBarButtonItemStyleBordered target:self action:nil];
    UIBarButtonItem *btnRejectReport = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"APPROVE_SENDBACK_BUTTON_TITLE"] style:UIBarButtonItemStyleBordered target:self action:nil];

    btnApproveReport.tintColor = [UIColor grayColor];
    btnRejectReport.tintColor = [UIColor grayColor];

    toolbarItems = @[flexibleSpace, btnRejectReport, btnApproveReport];

    [self setToolbarItems:toolbarItems];
}

- (void)rejectionCancelled
{
    [self hideWaitView];
}

#pragma mark -
#pragma mark Approval SendBack Sumit Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if(alertView.tag == kAlertViewApprove & buttonIndex == 1)
	{
        // Block the action bar while approving.
        [self hideToolBarActions];

		[self showWaitViewWithText:[NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Approving Report"], rpt.reportName] ];
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
									 APPROVE_ENTRIES, @"TO_VIEW", @"YES", @"SKIP_CACHE", nil];
        if (self.approvalStatKey != nil)
            pBag[@"STAT_KEY"] = self.approvalStatKey; // MOB-9927 custom approval status

		[[ExSystem sharedInstance].msgControl createMsg:APPROVE_REPORTS_DATA_APPROVE CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
	else if(alertView.tag == kAlertViewApprove)
	{
		[self hideWaitView];
	}
	else if (alertView.tag == kAlertViewSubmit)
	{
		if (buttonIndex == 0){
			[self hideWaitView];
		}
		if (buttonIndex == 1){
			
			[self showWaitView];
			
			inAction = TRUE;
			//[self setupToolbar];
			
//			Msg *msg = [[Msg alloc] init];
//			msg.idKey = SUBMIT_REPORT_DATA;
//			[self respondToFoundData:msg];
//			[msg release];
			
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
										 ACTIVE_ENTRIES, @"TO_VIEW",  
										 self.rpt.rptKey, @"ID_KEY", 
										 nil];
			
			[[ExSystem sharedInstance].msgControl createMsg:SUBMIT_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
			
		}
		
	}
}


-(IBAction) actionApprove:(id)sender
{
	if(![self onlineCheck])
		return;

	NSString* nsQuestion = [Localizer getLocalizedText:@"APPROVE_QUESTION_AREYOUSURE"];

    NSString* alertTitle = nil;
    NSString* alertMessage = nil;
    // MOB-7870, 8370 (MWS) check and use custom confirmation msg.
    ExpenseConfirmation *approvalConf = [[UserConfig getSingleton] approvalConfirmationForPolicy:self.rpt.polKey];
    if (approvalConf != nil && (approvalConf.title != nil && approvalConf.text != nil))
    {
        if ([approvalConf.title length])
            alertTitle = approvalConf.title;
        if ([approvalConf.text length])
            alertMessage = approvalConf.text;
    }
    else
    {
        alertTitle = [Localizer getLocalizedText:@"APPROVE_PLEASE_CONFIRM"];
        alertMessage = nsQuestion;
    }
    

	UIAlertView* alert = [[MobileAlertView alloc] initWithTitle:alertTitle
													message:alertMessage delegate:self cancelButtonTitle:
						  [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
										  otherButtonTitles:[Localizer getLocalizedText:@"APPROVE_OK"], 
						  nil];
	
	alert.tag = kAlertViewApprove;
	[alert show];
}


-(IBAction) actionReject:(id)sender
{
	if(![self onlineCheck])
		return;

    [self showWaitViewWithText:@""];
	
	ReportRejectionViewController *rejectionVC = [[ReportRejectionViewController alloc] initWithNibName:@"ReportRejectionViewController" bundle:nil];
	rejectionVC.reportRejectionDelegate = self;
	rejectionVC.modalPresentationStyle = UIModalPresentationFormSheet;
	[self presentViewController:rejectionVC animated:YES completion:nil];
}

-(IBAction) actionSubmit:(id)sender
{
	if(![self onlineCheck])
		return;

	// Test imageRequired to make sure we are using a reportDetail object; otherwise, we must wait for the report detail to come back
	// from the server
	if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle: [Localizer getLocalizedText:@"Cannot Submit Report"]
							  message: [Localizer getLocalizedText:@"OFFLINE_MSG"]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
		return;		
	}
	
	if (self.rptDetail == nil || self.rptDetail.imageRequired == nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle: [Localizer getLocalizedText:@"Cannot Submit Report"]
							  message:[Localizer getLocalizedText:@"MSG_WAIT_FETCHING_INFO"]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
							  otherButtonTitles:nil];
		[alert show];
		return;	
	}
	
	if([ExSystem connectedToNetwork] && self.rptDetail != nil && self.rptDetail.imageRequired != nil)
	{
		// Check for no_entries, undefined expense type, & red flag exceptions before submit
		NSString *cannotSubmitMsg = nil;
        NSString *cannotSubmitTitle = [Localizer getLocalizedText:@"Cannot Submit Report"];
		
        if (rptDetail.entries == nil || [rptDetail.entries count] ==0)
		{
			cannotSubmitMsg = [Localizer getLocalizedText:@"SUBMIT_ERROR_NO_ENTRY_MSG"];
		}
		else
		{
			NSMutableArray* rpeKeys = rptDetail.keys;
			NSMutableDictionary* entries = rptDetail.entries;
			for (int ix = 0; cannotSubmitMsg == nil && ix < [rpeKeys count]; ix++)
			{
				EntryData* entry = (EntryData*)entries[rpeKeys[ix]];
				if (entry != nil && ((entry.expKey != nil && [entry.expKey isEqualToString:@"UNDEF"]) ||
									 (entry.expKey == nil && [entry.expName isEqualToString:@"Undefined"])))
				{
					cannotSubmitMsg = [Localizer getLocalizedText:@"SUBMIT_ERROR_UNDEF_MSG"];
				}
                else if ([ReportViewControllerBase isCardAuthorizationTransaction:entry])
                {
                    // MOB-13616 Disable report submit if contains card authorization
                    cannotSubmitTitle = [Localizer getLocalizedText:@"Report Not Ready"];
                    cannotSubmitMsg = [Localizer getLocalizedText:@"SUBMIT_ERROR_CCT_AUTH"];
                    
                }
			}
		}
		
		if (cannotSubmitMsg != nil)
		{
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle: cannotSubmitTitle
								  message: cannotSubmitMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
								  otherButtonTitles:nil];
			[alert show];
			return;
		}
		
		NSString* receiptRequired = self.rptDetail.imageRequired;
		NSMutableArray *entriesNeedReceipt = [[NSMutableArray alloc] init];
		NSMutableArray *entriesNeedPaperRecipt = [@[] mutableCopy];
        
		if (receiptRequired!= nil && [receiptRequired isEqualToString:@"Y"])
		{
			// We need to figure out entries needs receipts
			NSArray * keys = self.rptDetail.keys;
			NSDictionary * entries = self.rptDetail.entries;
			for (int ix = 0; ix < [keys count]; ix++)
			{
				NSString * key = (NSString*)keys[ix];
				EntryData*entry = (EntryData *) entries[key];
				
				// Logic adapted from SubmitWizard.js
				if ([@"Y" isEqualToString:entry.imageRequired] || [@"Y" isEqualToString: entry.receiptRequired])
				{
					if (entry.ereceiptId == nil &&  (entry.hasMobileReceipt == nil || [@"N" isEqualToString:entry.hasMobileReceipt]) && entry.receiptImageId == nil)
					{
						[entriesNeedReceipt addObject:entry];
					}
                    
                    // MOB-13715 need paper receipt flag
                    // Logic adapted from SubmitWizard.js " updateText: function(rpt, confirmAgreement, wizardView, numExpNeedRcpt) "
                    if ([entry.receiptRequired isEqualToString:@"Y"])
                    {
                        [entriesNeedPaperRecipt addObject:entry];
                    }
				}
			}
		}
		
		NSString *alertTitle = [Localizer getLocalizedText:@"CONFIRM_REPORT_SUBMISSION"];
		NSString *alertMessage = [Localizer getLocalizedText:@"MSG_RECEIPT_NOT_REQUIRED"];
		NSString *cancelButtonText = [[NSString alloc] initWithString:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
		// Mob-2516 Localization of Submit label text
		NSString *submitButtonText = [Localizer getLocalizedText:@"LABEL_SUBMIT_BTN"];;
		
		
		if ([entriesNeedReceipt count] >0)
		{
			// show modal dialog with entries need receipts
			SubmitNeedReceiptsViewController * dlg = [[SubmitNeedReceiptsViewController alloc] initWithNibName:@"SubmitNeedReceiptsView" bundle:nil];
			dlg.delegate = self;
			dlg.entryList = entriesNeedReceipt;
            dlg.howToProvideMsgType = @"entriesNeedReceipt";
            dlg.rpt = self.rpt;
			dlg.modalPresentationStyle = UIModalPresentationFormSheet;
			[self presentViewController:dlg animated:YES completion:nil];
		}
        else if ([entriesNeedPaperRecipt count] > 0)
        {
            // show modal dialog with entries need paper receipts
			SubmitNeedReceiptsViewController * dlg = [[SubmitNeedReceiptsViewController alloc] initWithNibName:@"SubmitNeedReceiptsView" bundle:nil];
			dlg.delegate = self;
			dlg.entryList = entriesNeedPaperRecipt;
            dlg.howToProvideMsgType = @"entriesNeedPaperRecipt";
            dlg.rpt = self.rpt;
			dlg.modalPresentationStyle = UIModalPresentationFormSheet;
			[self presentViewController:dlg animated:YES completion:nil];
        }
        else 
        {
			if (receiptRequired!= nil && [receiptRequired isEqualToString:@"Y"])
				alertMessage = [Localizer getLocalizedText:@"MSG_ADDITIONAL_RECEIPT_NOT_REQUIRED"];
			
            // MOB-7870, 8370 (MWS) check and use custom confirmation msg.
            ExpenseConfirmation *submitConf = [[UserConfig getSingleton] submitConfirmationForPolicy:self.rpt.polKey];
            if (submitConf != nil && (submitConf.title != nil && submitConf.text != nil))
            {
                if ([submitConf.title length])
                    alertTitle = submitConf.title;
                if ([submitConf.text length])
                    alertMessage = submitConf.text;
            }
            
			UIAlertView *alert = [[MobileAlertView alloc] initWithTitle: alertTitle 
															message: alertMessage
														   delegate: self 
												  cancelButtonTitle: cancelButtonText 
												  otherButtonTitles: submitButtonText, nil];
			alert.tag = kAlertViewSubmit;
			
			[alert show];
		}
	}
}


// Go directly to home page
-(IBAction) actionGoHome:(id)sender
{
    NSMutableDictionary *parameterBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"POP_TO_ROOT_VIEW", @"YES", @"DONTPUSHVIEW", nil];
    [ConcurMobileAppDelegate switchToView:HOME_PAGE viewFrom:[self getViewIDKey] ParameterBag:parameterBag];
}

- (UILabel*) makeLabel: (CGRect) rect alert:(UIAlertView*) alert
{
	UILabel *label = [[UILabel alloc] initWithFrame:rect];
	label.font = [UIFont systemFontOfSize:12];
	label.textColor = [UIColor whiteColor];
	label.backgroundColor = [UIColor clearColor];
	label.shadowColor = [UIColor blackColor];
	label.shadowOffset = CGSizeMake(0,-1);
	label.lineBreakMode = NSLineBreakByWordWrapping;
	label.numberOfLines = 999;
	label.textAlignment = NSTextAlignmentLeft;
	[alert addSubview:label];
	return label;
}


#pragma mark Reports PopOvers
- (void)buttonReportCommentsInRect:(CGRect)rect
{

	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
	}
	
	CommentListVC *vc = [[CommentListVC alloc] initWithNibName:@"EditFormView" bundle:nil];
    
	[vc setSeedData:self.rptDetail.comments field:nil delegate:nil];
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:vc];               
	
    [self.pickerPopOver presentPopoverFromRect:rect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
}

- (void)buttonReportExceptionsPressedInRect:(CGRect)rect
{
	detailApprovalListVC = [[DetailApproverList alloc] initWithNibName:@"DetailApproverList" bundle:nil];
	detailApprovalListVC.listType = 3; //exceptions
	detailApprovalListVC.report = rptDetail;
	//detailApprovalListVC.delegate = self;
	if([rptDetail.exceptions count] < 4)
	{
		NSMutableArray *aRows = [[NSMutableArray alloc] init];
		for(int i = 0; i < [rptDetail.exceptions count]; i++)
		{
			ExceptionData *ex = (rptDetail.exceptions)[i];
			KeyValue *kv = [[KeyValue alloc] init];
			kv.label = ex.severityLevel;
			kv.val = ex.exceptionsStr;
			[aRows addObject:kv];
		}
		
		float totalH = 0.0;
		
		for(int i = 0; i < [aRows count]; i++)
		{
			CGFloat w = 311.0;
			KeyValue *kv = aRows[i];
			NSString *val = kv.val;
			CGFloat height =  [FormatUtils getTextFieldHeight:w Text:val FontSize:17.0f];
			totalH = totalH + height;
		}
		detailApprovalListVC.adjustedHeight = totalH + 30; // (44.0 * [entry.exceptions count]);
	}
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:detailApprovalListVC];               
	[detailApprovalListVC makeExceptionDetailsReport:rptDetail];
	

    [self.pickerPopOver presentPopoverFromRect:rect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
	
}

#pragma mark -
#pragma mark popover methods

-(void) dismissPopovers
{
	if (popoverController != nil) {
        [popoverController dismissPopoverAnimated:YES];
        self.popoverController = nil;
    } 
	if (pickerPopOver != nil) {
        [pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
    } 
}

- (IBAction)buttonReportsPressed:(id)sender
{
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
	}
	
    // Check offline and update header
    [self checkOffline];
	
	self.reportsListVC = [[ActiveReportListViewController alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    self.reportsListVC.fromMVC = self;
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:self.reportsListVC];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	[self presentViewController:localNavigationController animated:YES completion:nil];
	
	[self.reportsListVC loadReports];


}

- (IBAction)buttonTripsPressed:(id)sender
{
	if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
    }
	tripsListVC = [[TripsViewController alloc] initWithNibName:@"TripsView" bundle:nil];
	tripsListVC.iPadHome = iPadHome;
	tripsListVC.fromMVC = self;
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:tripsListVC];               
	
    //[self.pickerPopOver presentPopoverFromBarButtonItem:sender permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES]; 
	UIButton *btn = (UIButton *)sender;
	float viewW = 80.0;
	int btnPos = btn.tag;
	float x = middleX + (btnPos * viewW);
	
	float y = 660.0;
	if(![ExSystem isLandscape])
	{
		float screenH = 1004.0;
		y = screenH - 90.0;
	}
	
	CGRect rect = CGRectMake(x, y, viewW, 1);
	
    [self.pickerPopOver presentPopoverFromRect:rect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES]; 

	// Calling loadTrips *after* the popover is presented, because the view controller will only receive the message response if it's visible.
	[tripsListVC loadTrips];
}


#pragma mark -
#pragma mark PopOver Methods
- (void)buttonAttendeesTapped:(id)sender IndexPath:(NSIndexPath *)newIndexPath
{
	int section = [newIndexPath section];

	EntryData *entry = aSections[section];
	
	detailApprovalListVC = [[DetailApproverList alloc] initWithNibName:@"DetailApproverList" bundle:nil];
	detailApprovalListVC.listType = 1; //attendees
	detailApprovalListVC.entry = entry;

	NSMutableArray *aRows = (NSMutableArray*)[detailApprovalListVC makeAttendeeDetails:entry];
	if([aRows count] > 0)
	{
		CGFloat runningHeight = 0.0;
		
		for(KeyValue *kv in aRows)
		{
			CGFloat w = 311.0;
			
			NSString *val = kv.val;
			CGFloat height =  [FormatUtils getTextFieldHeight:w Text:val FontSize:13.0f];
			
			w = 152.0;
			val = kv.label;
			CGFloat heightLabel =  [FormatUtils getTextFieldHeight:w Text:val FontSize:13.0f];
			
			if(heightLabel > height)
				height = heightLabel;
			
			runningHeight = runningHeight + height;			
		}
		detailApprovalListVC.adjustedHeight = runningHeight;
	}
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:detailApprovalListVC];   
	
	[detailApprovalListVC makeAttendeeDetails:entry];

	CGRect cellRect = [tableList rectForRowAtIndexPath:newIndexPath];
	CGRect myRect = [self.view convertRect:cellRect fromView:tableList];
	
    [self.pickerPopOver presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES]; 

}


- (void)buttonCommentsTapped:(id)sender IndexPath:(NSIndexPath *)newIndexPath
{
	int section = [newIndexPath section];
	//    int row = [newIndexPath row];
	EntryData *entry = aSections[section];
	//	NSMutableArray *a = [entryDetailsDict objectForKey:entry.rpeKey];
	
	detailApprovalListVC = [[DetailApproverList alloc] initWithNibName:@"DetailApproverList" bundle:nil];
	detailApprovalListVC.listType = 2; //comments
	detailApprovalListVC.entry = entry;
	if([entry.comments count] < 4)
	{
		NSMutableArray *aRows = [[NSMutableArray alloc] init];
		for(int i = 0; i < [entry.commentKeys count]; i++)
		{
			NSString *key = (entry.commentKeys)[i];
			CommentData *c = (entry.comments)[key];
			NSString *dt = [CCDateUtilities formatDateToMMMddYYYFromString:c.creationDate];
			NSString *commentHeader = [NSString stringWithFormat:@"%@ - %@", dt, c.commentBy];
			
			KeyValue *kv = [[KeyValue alloc] init];
			kv.label = commentHeader;
			kv.val = c.comment;
			[aRows addObject:kv];
		}
		
		float totalH = 0.0;
		
		for(int i = 0; i < [aRows count]; i++)
		{
			CGFloat w = 311.0;
			KeyValue *kv = aRows[i];
			NSString *val = kv.val;
			CGFloat heightText = [FormatUtils getTextFieldHeight:w Text:val FontSize:17.0f];
			w = 152.0;
			CGFloat heightLabel =  [FormatUtils getTextFieldHeight:w Text:kv.label FontSize:17.0f];
			if(heightLabel > heightText)
				heightText = heightLabel;
			totalH = totalH + heightText;
		}
		detailApprovalListVC.adjustedHeight = totalH + 30; // (44.0 * [entry.exceptions count]);
	}
	//detailApprovalListVC.delegate = self;
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:detailApprovalListVC];               
	[detailApprovalListVC makeCommentDetails:entry];
	
	CGRect cellRect = [tableList rectForRowAtIndexPath:newIndexPath];
	CGRect myRect = [self.view convertRect:cellRect fromView:tableList];
	
    [self.pickerPopOver presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES]; 

}

- (void)buttonExceptionsTapped:(id)sender IndexPath:(NSIndexPath *)newIndexPath
{
	int section = [newIndexPath section];
	//    int row = [newIndexPath row];
	EntryData *entry = aSections[section];
	//	NSMutableArray *a = [entryDetailsDict objectForKey:entry.rpeKey];
	
	detailApprovalListVC = [[DetailApproverList alloc] initWithNibName:@"DetailApproverList" bundle:nil];
	detailApprovalListVC.listType = 3; //exceptions
	detailApprovalListVC.entry = entry;
	//detailApprovalListVC.delegate = self;
	if([entry.exceptions count] < 4)
	{
		NSMutableArray *aRows = [[NSMutableArray alloc] init];
		for(int i = 0; i < [entry.exceptions count]; i++)
		{
			ExceptionData *ex = (entry.exceptions)[i];
			KeyValue *kv = [[KeyValue alloc] init];
			kv.label = ex.severityLevel;
			kv.val = ex.exceptionsStr;
			[aRows addObject:kv];
		}
		
		float totalH = 0.0;
		
		for(int i = 0; i < [aRows count]; i++)
		{
			CGFloat w = 311.0;
			KeyValue *kv = aRows[i];
			NSString *val = kv.val;
			CGFloat height =  [FormatUtils getTextFieldHeight:w Text:val FontSize:17.0f];
			totalH = totalH + height;
		}
		detailApprovalListVC.adjustedHeight = totalH + 30; // (44.0 * [entry.exceptions count]);
	}
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:detailApprovalListVC];               
	[detailApprovalListVC makeExceptionDetails:entry];
	
	CGRect cellRect = [tableList rectForRowAtIndexPath:newIndexPath];
	CGRect myRect = [self.view convertRect:cellRect fromView:tableList];
	
    [self.pickerPopOver presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES]; 

}

- (void)buttonItemizationsTapped:(id)sender IndexPath:(NSIndexPath *)newIndexPath
{
	int section = [newIndexPath section];
	//    int row = [newIndexPath row];
	EntryData *entry = aSections[section];
	//	NSMutableArray *a = [entryDetailsDict objectForKey:entry.rpeKey];
	
	ReportItemListViewController *vc;
	vc = [[ReportItemListViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", entry, @"ENTRY", nil];
    [vc setSeedData:pBag];
	
	UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:vc];
    
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:navController];               
	
	CGRect cellRect = [tableList rectForRowAtIndexPath:newIndexPath];
	CGRect myRect = [self.view convertRect:cellRect fromView:tableList];
	
    [self.pickerPopOver presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES]; 

}


- (void)buttonSummaryTapped:(id)sender
{
	[self loadEditableReport];
	
}


-(void) loadEditableReport
{	
	if(rptDetail == nil)
		return; //we can't do anything
	
	rptDetail.employeeName = rpt.employeeName;
	rptDetail.processInstanceKey = rpt.processInstanceKey;
	rptDetail.stepKey = rpt.stepKey;
	rptDetail.currentSequence = rpt.currentSequence;
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] 
								 initWithObjectsAndKeys:
								 //@"YES", @"SKIP_PARSE", 
								 ROLE_EXPENSE_TRAVELER, @"ROLE", 
								 @"YES", @"SHORT_CIRCUIT", 
								 nil];
	if (rptDetail.employeeName != nil)
		pBag[@"EmployeeName"] = rptDetail.employeeName;
	pBag[@"ReportName"] = rptDetail.reportName;
	pBag[@"TotalPostedAmount"] = rptDetail.totalPostedAmount;
	pBag[@"CrnCode"] = rptDetail.crnCode;
	pBag[@"REPORT"] = rptDetail;
	pBag[@"RECORD_KEY"] = rptDetail.rptKey;
	pBag[@"REPORT_OG"] = rptDetail;
	

	pBag[@"ID_KEY"] = rptDetail.rptKey;

    ReportSummaryViewController* vc = [[ReportSummaryViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
    [vc setSeedData:pBag];        
    

	UINavigationController *navController = [[KeyboardNavigationController alloc] initWithRootViewController:vc];
	
	navController.modalPresentationStyle = UIModalPresentationFormSheet;
	[navController setToolbarHidden:NO];

	[self presentViewController:navController animated:YES completion:nil];
	
}


-(void) setUpPreviousNextReports
{
	//mob-3256
	[btnPrevious setHidden:YES];
	[btnNext setHidden:YES];
	
	if(reportKeys == nil || reportDictionary == nil || [reportKeys count] < 2)
		return;
	
	int currTripPos = -1;
	int iPos = 0;
	
	for (NSString *key in reportKeys)
	{
		if([key isEqualToString:rptDetail.rptKey])
		{
			currTripPos = iPos;
			break;
		}
		iPos++;
	}
	
	if (currTripPos != -1) {
		//we have a position!
		if (currTripPos > 0) {
			//we have a previous trip
			self.previousRpt = reportDictionary[reportKeys[(currTripPos - 1)]];
//			UISwipeGestureRecognizer *Recognizer = [[[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(Perform_Swiped_left:)] autorelease];
//			Recognizer.direction = UISwipeGestureRecognizerDirectionLeft;
//			[self.view addGestureRecognizer:Recognizer];
			
			[btnPrevious setHidden:NO];

			//lblPreviousTrip.text = @"<< Previous Trip"; // [NSString stringWithFormat:@"<< %@", previousTrip.tripName];
		}
		
		if (currTripPos < ([reportKeys count] - 1)) {
			//we have a next trip
			self.nextRpt = reportDictionary[reportKeys[(currTripPos + 1)]];
//			UISwipeGestureRecognizer *Recognizer = [[[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(Perform_Swiped_right:)] autorelease];
//			Recognizer.direction = UISwipeGestureRecognizerDirectionRight;
//			[self.view addGestureRecognizer:Recognizer];
			
			[btnNext setHidden:NO];
			//lblNextTrip.text = @"Next Trip >>"; // [NSString stringWithFormat:@"%@ >>", nextTrip.tripName];
		}
		
		
	}
	
}

- (void) Perform_Swiped_left:(UISwipeGestureRecognizer*)sender
{
	//NSLog(@"Swiped Left");
	if(previousRpt != nil)
	{		
		//[pBag objectForKey:@"REPORT_KEYS"], @"REPORT_KEYS", [pBag objectForKey:@"REPORT_DICT"]
		NSMutableDictionary *pBagNew = [[NSMutableDictionary alloc] initWithObjectsAndKeys:previousRpt, @"REPORT", previousRpt.rptKey, @"ID_KEY", previousRpt.rptKey, @"RECORD_KEY",
										ACTIVE_ENTRIES, @"TO_VIEW", reportKeys, @"REPORT_KEYS", reportDictionary, @"REPORT_DICT", @"PREVIOUS", @"DIRECTION", nil];
		if(!isReport)
			[iPadHome switchToDetail:@"Approval" ParameterBag:pBagNew];
		else 
			[iPadHome switchToDetail:@"Report" ParameterBag:pBagNew];
	}
}


- (void) Perform_Swiped_right:(UISwipeGestureRecognizer*)sender
{
	//NSLog(@"Swiped Right");
	if(nextRpt != nil)
	{
		NSMutableDictionary *pBagNew = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nextRpt, @"REPORT", nextRpt.rptKey, @"ID_KEY", nextRpt.rptKey, @"RECORD_KEY",
										ACTIVE_ENTRIES, @"TO_VIEW", reportKeys, @"REPORT_KEYS", reportDictionary, @"REPORT_DICT", @"NEXT", @"DIRECTION", nil];
		
		if(!isReport)
			[iPadHome switchToDetail:@"Approval" ParameterBag:pBagNew];
		else 
			[iPadHome switchToDetail:@"Report" ParameterBag:pBagNew];
	}
}

-(NSMutableArray*) getPopovers
{
	NSMutableArray* popovers = [super getPopovers];
	
	if (oopePopOver != nil)
		[popovers addObject: oopePopOver];
	
	return popovers;
}

-(BOOL)onlineCheck
{
	if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Actions offline"] 
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return NO;
	}
	else 
		return YES;
}


-(void) buttonCameraPressed
{
    UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker]; 
	imgPicker.sourceType = UIImagePickerControllerSourceTypeCamera;
//	imgPicker.allowsEditing = NO; // MOB-6488
	imgPicker.wantsFullScreenLayout = YES;
	[UnifiedImagePicker sharedInstance].delegate = self;
	
	if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
    }
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:imgPicker];
	
	[pickerPopOver presentPopoverFromRect:btnAttachReceipt.frame inView:self.view permittedArrowDirections:UIPopoverArrowDirectionLeft animated:YES];
}


-(void)buttonAlbumPressed
{	
	UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker]; 
	imgPicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
	imgPicker.allowsEditing = YES; // Needed to see full image
	imgPicker.wantsFullScreenLayout = YES;
	[UnifiedImagePicker sharedInstance].delegate = self;
	
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:imgPicker];
	
	[pickerPopOver presentPopoverFromRect:self.attachReceiptRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionLeft animated:YES];
}


-(void)buttonReceiptStorePressed
{
    [self checkOffline];
	
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
	}
    
    ReceiptStoreListView *receiptStoreVC = [[ReceiptStoreListView alloc] initWithNibName:@"ReceiptStoreListView" bundle:nil];
    receiptStoreVC.delegate = self;
    receiptStoreVC.disableEditActions = YES;
    
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:receiptStoreVC];
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	[localNavigationController setToolbarHidden:NO];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];
}

-(void) showApproveActions:(UIActionSheet*)actionSheet
{
    if (![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"Reports cannot be approved offline"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                              otherButtonTitles:nil];
        [alert show];
        return;
    }

    CGRect cellRect = [self.btnApprove frame];
	[actionSheet showFromRect:cellRect inView:self.view animated:YES];
}


- (void)cancelApprovalList
{
    
}

#pragma mark -
#pragma mark Managing the popover

- (void)showRootPopoverButtonItem:(UIBarButtonItem *)barButtonItem {

}


- (void)invalidateRootPopoverButtonItem:(UIBarButtonItem *)barButtonItem {

}


#pragma mark UnifiedImagePickerDelegate methods
- (void)unifiedImagePickerSelectedImage:(UIImage*)image
{
    if (self.pickerPopOver != nil)
    {
        [self.pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
    }
	
    // MOB-8441 Do not dismiss the image picker modally - it is always presented within popover for iPad
    
	[self showWaitViewWithProgress:YES withText:[Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"]];
    [[ExReceiptManager sharedInstance] configureReceiptManagerForDelegate:self andReportEntry:nil andReporData:self.rpt andExpenseEntry:nil andRole:self.role];

    holdText.text = [Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"];
    [[ExReceiptManager sharedInstance] uploadReceipt:image];
}

#pragma mark Receipt store delegate
-(void)didSelectImageFromReceiptStore:(ReceiptStoreReceipt*)receiptData
{
    self.rpt.receiptImageId = receiptData.receiptImageId;
    
    [self.navigationController dismissViewControllerAnimated:YES completion:nil];
    
    [[ExReceiptManager sharedInstance] configureReceiptManagerForDelegate:self andReportEntry:nil andReporData:self.rpt andExpenseEntry:nil andRole:self.role];
    
    [self showWaitViewWithText:[Localizer getLocalizedText:@"Loading Data"]];

    [[ExReceiptManager sharedInstance] addReceiptToReport];
}


-(void)setupToolbar
{}

-(void)refreshView
{
    [self addReportHeaderData: 0];
    [self updateViews];
}


#pragma mark ReceiptDetailViewDelegate method
-(void)savedReportReceipt
{
}

- (void)viewWillLayoutSubviews
{
    // This method is called whenever view layout normally happens, e.g. the view is loaded, the device is rotated, etc.
    
    float viewWidth = UIInterfaceOrientationIsLandscape(self.interfaceOrientation) ? 1024.0 : 768.0;
    float viewHeight = UIInterfaceOrientationIsLandscape(self.interfaceOrientation) ? 660.0 : 916.0;
    
    // Layout the loading view
    self.loadingView.frame = CGRectMake(0, 0, viewWidth, viewHeight);
    
    // Layout the left pane
    float newPaneWidth = UIInterfaceOrientationIsLandscape(self.interfaceOrientation) ? 300.0 : 246.0;
    CGRect newPaneFrame = CGRectMake(0, 0, newPaneWidth, viewHeight);
    self.leftPaneView.frame = newPaneFrame;
    
    // Layout the right table
    const float widthOfBorderAroundTable = 12.0;
    float newTableLeft = newPaneWidth + widthOfBorderAroundTable; // Account for left border
    float newTableTop = widthOfBorderAroundTable; // Account for top border
    float newTableWidth = viewWidth - newPaneWidth - widthOfBorderAroundTable - widthOfBorderAroundTable; // Account for left and right borders
    float newTableHeight = viewHeight - newTableTop;
    CGRect newTableFrame = CGRectMake(newTableLeft, newTableTop, newTableWidth, newTableHeight);
    self.rightTableView.frame = newTableFrame;
    
    // Layout the pane header view
    if (self.leftPaneHeaderView != nil)
    {
        for (UIView *subview in self.leftPaneView.subviews)
        {
            if (subview == self.leftPaneHeaderView)
            {
                CGRect newHeaderFrame = CGRectMake(0, 0, self.leftPaneView.frame.size.width, self.leftPaneHeaderView.frame.size.height);
                self.leftPaneHeaderView.frame = newHeaderFrame;
            }
        }
    }
    
    // Layout the pane footer view
    if (self.leftPaneFooterView != nil)
    {
        for (UIView *subview in self.leftPaneView.subviews)
        {
            if (subview == self.leftPaneFooterView)
            {
                CGRect newFooterFrame = CGRectMake(0, self.leftPaneView.frame.size.height - self.leftPaneFooterView.frame.size.height, newPaneWidth, self.leftPaneFooterView.frame.size.height);
                self.leftPaneFooterView.frame = newFooterFrame;
            }
        }
    }
    
    // Layout the pane button container view
    CGRect newButtonContainerFrame = CGRectMake(0, self.leftPaneHeaderView.frame.size.height, newPaneWidth, viewHeight - self.leftPaneHeaderView.frame.size.height);
    self.leftPaneButtonContainerView.frame = newButtonContainerFrame;
    
    // Layout everything else
    [super viewWillLayoutSubviews];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.navigationItem.hidesBackButton = NO;
    
    //self.loadingLabel.text = [Localizer getLocalizedText:@"Loading Data"];
    self.loadingView.hidden = YES;
    
    if (self.sections == nil) // MOB-8438 do not reset data when come back from OutOfMemory; init data before tbl refresh
    {
        _dictExpensesToFetch = [[NSMutableDictionary alloc] init];
        [self initSections];
    }
    
    // Just add the header view. Leave the layout to viewWillLayoutSubviews
    _leftPaneHeaderView = [self loadHeaderView];
    [self.leftPaneView addSubview:_leftPaneHeaderView];
    
    // Just add the footer view. Leave the layout to viewWillLayoutSubviews
    _leftPaneFooterView = [self loadFooterView];
    [self.leftPaneView addSubview:_leftPaneFooterView];
    
    // Tags will be used to identify buttons.  See buttonPressed method.
    self.button0.tag = 0;
    self.button1.tag = 1;
    self.button2.tag = 2;
    self.button3.tag = 3;
    self.button4.tag = 4;
    self.button5.tag = 5;
    
    self.rightTableView.dataSource = self;
    self.rightTableView.delegate = self;
    
    [self updateViews];
}

#pragma mark - Bar Methods
-(void) configureBars
{
    if ([Config isCorpHome])
    {
        self.navigationController.navigationBar.hidden = NO;
        self.navigationController.toolbarHidden = NO;
        
        // Nav bar
        self.navigationItem.title = rptDetail.reportName;
        
        UIBarButtonItem *btnReportsList = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Reports List"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonReportsPressed:)];
        self.navigationItem.rightBarButtonItem = btnReportsList;
        
        // Tool bar
        UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
        NSArray *toolbarItems = nil;
        
        if (![self isApproving]) {
            if ([self canSubmit]) {
                UIBarButtonItem *btnSubmitReport = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Submit Report"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionSubmit:)];
                
                toolbarItems = @[flexibleSpace, flexibleSpace, btnSubmitReport];
            } else  {
                toolbarItems = @[];
            }
        } else {
            UIBarButtonItem *btnApproveReport = nil;
            if (self.rpt.workflowActions == nil || [self.rpt.workflowActions count]==0)
                btnApproveReport = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"APPROVE_REPORT"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionApprove:)];
            else
                btnApproveReport = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"APPROVE_REPORT"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionApproveActions:)];
            
            UIBarButtonItem *btnRejectReport = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"APPROVE_SENDBACK_BUTTON_TITLE"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionReject:)];
            
            toolbarItems = @[btnRejectReport, flexibleSpace, btnApproveReport];
        }
        
        [self setToolbarItems:toolbarItems];
    }
}

- (void) closeMe:(id)sender
{
    [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark - Button Methods (in BaseDetailVC_iPad.m)
- (IBAction) buttonPressed:(id)sender
{
    UIButton *button = (UIButton*)sender;
    int buttonIndex = button.tag;
    
    if (buttonIndex < _buttonDescriptors.count)
    {
        ReportButtonDescriptor *descriptor = _buttonDescriptors[buttonIndex];
        CGRect buttonRect = [self.view convertRect:button.frame fromView:self.leftPaneButtonContainerView];
        [self didPressButtonAtIndex:buttonIndex withId:descriptor.buttonId inRect:buttonRect];
    }
}

- (UIButton*) buttonAtIndex:(int)index
{
    if (index == 0)
        return self.button0;
    else if (index == 1)
        return self.button1;
    else if (index == 2)
        return self.button2;
    else if (index == 3)
        return self.button3;
    else if (index == 4)
        return self.button4;
    else if (index == 5)
        return self.button5;
    else
        return nil;
}

- (UILabel*) labelOnButtonAtIndex:(int)index
{
    if (index == 0)
        return self.labelOnButton0;
    else if (index == 1)
        return self.labelOnButton1;
    else if (index == 2)
        return self.labelOnButton2;
    else if (index == 3)
        return self.labelOnButton3;
    else if (index == 4)
        return self.labelOnButton4;
    else if (index == 5)
        return self.labelOnButton5;
    else
        return nil;
}

- (void) configureButtons
{
    if (!self.isViewLoaded || _buttonDescriptors == nil)
        return;
    
    for (int buttonIndex = 0; buttonIndex < _buttonDescriptors.count; buttonIndex++)
    {
        ReportButtonDescriptor *descriptor = _buttonDescriptors[buttonIndex];
        
        UIButton *button = [self buttonAtIndex:buttonIndex];
        button.hidden = NO;
        
        UILabel *labelForButton = [self labelOnButtonAtIndex:buttonIndex];
        labelForButton.text = descriptor.title;
    }
    
    for (int unusedButtonIndex = _buttonDescriptors.count; unusedButtonIndex < MAX_REPORT_BUTTONS; unusedButtonIndex++)
    {
        UIButton *unusedButton = [self buttonAtIndex:unusedButtonIndex];
        unusedButton.hidden = YES;
    }
}

- (void) setButtonDescriptors:(NSArray*)descriptors
{
    // This method can be called any time and the UI will be updated to show the new buttons
    
    // This method makes its own immutable copy of the array, so if you need to change a button, then (1) arrange the buttons in your own array, and (2) pass your array to this method which will copy it.
    
    _buttonDescriptors = [NSArray arrayWithArray:descriptors]; // Make an immutable copy of the array.
    [self configureButtons];
}

- (UIView*) loadHeaderView
{
    UIView *headerView = (UIView*)[[NSBundle mainBundle] loadNibNamed:@"ReportPaneHeader_iPad" owner:self options:nil][0];
    return headerView;
}

- (UIView*) loadFooterView
{
    UIView *headerView = (UIView*)[[NSBundle mainBundle] loadNibNamed:@"ReportPaneFooter_iPad" owner:self options:nil][0];
    return headerView;
}

- (void) didPressButtonAtIndex:(int)buttonIndex withId:(NSString*)buttonId inRect:(CGRect)rect
{
    if ([buttonId isEqualToString:BUTTON_ID_REPORT_SUMMARY])
        [self buttonReportSummaryPressed];
    else if ([buttonId isEqualToString:BUTTON_ID_REPORT_ATTACH_RECEIPT])
        [self showAttachReceiptOptionsFromRect:rect];
    else if ([buttonId isEqualToString:BUTTON_ID_REPORT_RECEIPTS])
        [self buttonReceiptsPressed];
    else if ([buttonId isEqualToString:BUTTON_ID_REPORT_COMMENTS])
        [self buttonCommentsPressedInRect:rect];
    else if ([buttonId isEqualToString:BUTTON_ID_REPORT_EXCEPTIONS])
        [self buttonExceptionsPressedInRect:rect];
    else if ([buttonId isEqualToString:BUTTON_ID_ADD_EXPENSE])
        [self buttonAddExpensePressedInRect:rect];
    else if ([buttonId isEqualToString:BUTTON_ID_TRAVEL_ALLOWANCE])
        [self buttonTravelAllowancePressedInRect:rect];
}

#pragma mark - Button Methods
- (void) makeButtonLabels
{
    NSMutableArray *descriptors = [NSMutableArray array];
    [descriptors addObject:[ReportButtonDescriptor buttonDescriptorWithId:BUTTON_ID_REPORT_SUMMARY title:[Localizer getLocalizedText:@"Report Summary"]]];
    if (self.rpt.receiptImageId != nil || [self hasReceipt])
        [descriptors addObject:[ReportButtonDescriptor buttonDescriptorWithId:BUTTON_ID_REPORT_RECEIPTS title:[Localizer getLocalizedText:@"APPROVE_ENTRIES_ACTION_SECTION_VIEW_RECEIPTS_ROW"]]];
    else if ([self canEdit])
        [descriptors addObject:[ReportButtonDescriptor buttonDescriptorWithId:BUTTON_ID_REPORT_RECEIPTS title:[Localizer getLocalizedText:@"Add Receipt"]]];
//    [descriptors addObject:[ReportButtonDescriptor buttonDescriptorWithId:BUTTON_ID_REPORT_ATTACH_RECEIPT title:[Localizer getLocalizedText:@"Attach Receipt"]]];

	if (rptDetail.comments != nil && [rptDetail.comments count] > 0)
        [descriptors addObject:[ReportButtonDescriptor buttonDescriptorWithId:BUTTON_ID_REPORT_COMMENTS title:[Localizer getLocalizedText:@"Report Comments"]]];
    
    if (rptDetail.exceptions != nil && rptDetail.exceptions.count > 0)
        [descriptors addObject:[ReportButtonDescriptor buttonDescriptorWithId:BUTTON_ID_REPORT_EXCEPTIONS title:[Localizer getLocalizedText:@"Report Exceptions"]]];
    
    if (isReport && [self canEdit]) // Show add expense only if Report is editable.
        [descriptors addObject:[ReportButtonDescriptor buttonDescriptorWithId:BUTTON_ID_ADD_EXPENSE title:[Localizer getLocalizedText:@"Add Expense"]]];

    // Add Itinerary link
    BOOL hasFixedTA = [[ExSystem sharedInstance] siteSettingHasFixedTA];
    if (hasFixedTA)
    {
        [descriptors addObject:[ReportButtonDescriptor buttonDescriptorWithId:BUTTON_ID_TRAVEL_ALLOWANCE title:[Localizer getLocalizedText:@"REPORT_TA_ITINERARY"]]];
    }
    
    [self setButtonDescriptors:descriptors];
}

-(void) buttonReportSummaryPressed
{
	[self buttonSummaryTapped:nil];
}
-(void) buttonReceiptsPressed
{
    [self buttonReceiptsTapped:nil];
}

-(void) buttonCommentsPressedInRect:(CGRect)rect
{
    [self buttonReportCommentsInRect:rect];
}

-(void) buttonExceptionsPressedInRect:(CGRect)rect
{
	[self buttonReportExceptionsPressedInRect:rect];
}

-(void) buttonAddExpensePressedInRect:(CGRect)rect
{
    [self buttonAddEntryTappedInRect:rect];
}

-(void) buttonTravelAllowancePressedInRect:(CGRect)rect
{
    [self buttonTravelAllowanceTappedInRect:rect];
}



#pragma mark - Header Methods
- (void) updateHeaderView
{
    ReportPaneHeader_iPad* headerView = (ReportPaneHeader_iPad*)self.leftPaneHeaderView;
    headerView.reportName.text = rptDetail.reportName;
    headerView.reportDate.text = [CCDateUtilities formatDateToMMMddYYYFromString:rptDetail.reportDate];
    headerView.reportPurpose.text = rptDetail.purpose;
    headerView.reportStatus.text = rptDetail.apvStatusName;
	// MOB-17301 - Show report summary when user taps the header view   
    [headerView.rptHeaderTapGesture addTarget:self action:@selector(buttonSummaryTapped:)];
}

#pragma mark - Footer Methods
-(void) updateFooterView
{
    NSString *fontName = @"HelveticaNeue-Bold";
    float fontSize = 16.0;
    
    ReportPaneFooter_iPad *footerView = (ReportPaneFooter_iPad*)self.leftPaneFooterView;
    
    if (rptDetail.totalPostedAmount != nil && rptDetail.totalPostedAmount.length > 0)
    {
        NSString *totalAmountValue = [FormatUtils formatMoney:rptDetail.totalPostedAmount crnCode:rptDetail.crnCode];
        NSObject *totalAmountAttributedString = [self attributedStringWithAmountForLabel:footerView.totalAmountLabel labelText:[NSString stringWithFormat:@"%@ ", [Localizer getLocalizedText:@"Total Amount"]] valueText:totalAmountValue fontName:fontName fontSize:fontSize];
        
        if ([ExSystem is6Plus])
            footerView.totalAmountLabel.attributedText = (NSAttributedString*)totalAmountAttributedString;
        else
            footerView.totalAmountLabel.text = (NSString*)totalAmountAttributedString;
        
        footerView.totalAmountLabel.hidden = NO;
    }
    else
    {
        footerView.totalAmountLabel.hidden = YES;
    }
    
    if (rptDetail.totalClaimedAmount != nil && rptDetail.totalClaimedAmount.length > 0)
    {
        NSString *requestedAmountValue = [FormatUtils formatMoney:rptDetail.totalClaimedAmount crnCode:rptDetail.crnCode];
        NSObject *requestedAmountAttributedString = [self attributedStringWithAmountForLabel:footerView.totalRequestedLabel labelText:[NSString stringWithFormat:@"%@ ", [Localizer getLocalizedText:@"Total Requested" ]] valueText:requestedAmountValue fontName:fontName fontSize:fontSize];

        if ([ExSystem is6Plus])
            footerView.totalRequestedLabel.attributedText = (NSAttributedString*)requestedAmountAttributedString;
        else
            footerView.totalRequestedLabel.text = (NSString*)requestedAmountAttributedString;

        
        footerView.totalRequestedLabel.hidden = NO;
    }
    else
    {
        footerView.totalRequestedLabel.hidden = YES;
    }
}

- (NSObject*)attributedStringWithAmountForLabel:(UILabel*)label labelText:(NSString*)labelText valueText:(NSString*)valueText fontName:(NSString*)fontName fontSize:(float)fontSize
{
    if (![ExSystem is6Plus])
    {
        return [NSString stringWithFormat:@"%@%@", labelText, valueText];
    }
    else
    {
        if (label.attributedText == nil)
            return nil;
        
        // Get the attributes from the existing text inside the UILabel
        NSDictionary *labelAttributes = [label.attributedText attributesAtIndex:0 effectiveRange:nil];
        
        // Create a new label with the same attributes
        NSMutableAttributedString *labelString = [[NSMutableAttributedString alloc] initWithString:labelText attributes:labelAttributes];
        
        if (valueText != nil && valueText.length > 0)
        {
            // Create a new dictionary of attributes that is identical to the label attributes except for the font
            NSMutableDictionary *valueAttributes = [NSMutableDictionary dictionaryWithDictionary:labelAttributes];
            valueAttributes[NSFontAttributeName] = [UIFont fontWithName:fontName size:fontSize];
            
            // Create an attributed string for the value using the new attributes
            NSAttributedString *value = [[NSAttributedString alloc] initWithString:valueText attributes:valueAttributes];
            
            // Put the label and value together
            [labelString appendAttributedString:value];
        }
        
        return labelString;
    }
}

- (void) updateViews
{
    if (!self.isViewLoaded)
        return;
    
    [self configureBars];
    [self updateHeaderView];
    [self updateFooterView];
    [self makeButtonLabels];
    [self.rightTableView reloadData];
    [self.view setNeedsLayout];
}

-(void) showAttachReceiptOptionsFromRect:(CGRect)rect
{
    self.attachReceiptRect = rect;
    
    if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
    
	UIActionSheet *receiptActions = nil;
    NSString *receiptStoreBtnTitle = nil;
    
    if (!hideReceiptStore) {
        receiptStoreBtnTitle = [Localizer getLocalizedText:@"Receipt Store"];
    }
    
	if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
	{
		receiptActions = [[MobileActionSheet alloc] initWithTitle:nil
														 delegate:self
												cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
										   destructiveButtonTitle:nil
												otherButtonTitles:[Localizer getLocalizedText:@"Camera"],
						  [Localizer getLocalizedText:@"Photo Album"],receiptStoreBtnTitle, nil];
	}
	else {
		receiptActions = [[MobileActionSheet alloc] initWithTitle:nil
														 delegate:self
												cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
										   destructiveButtonTitle:nil
												otherButtonTitles:[Localizer getLocalizedText:@"Photo Album"],receiptStoreBtnTitle, nil];
	}
	
	receiptActions.tag = kActionViewAddReceipt;
	[receiptActions showFromRect:rect inView:self.view animated:YES];
}

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
	if (actionSheet.tag == kActionViewAddExpense)
	{
		if (buttonIndex ==0)
		{
			[self startAddEntry]; // Add expense
		}
		else if (buttonIndex == 1) // Import
		{
            [self setSmartExpenseMatching:NO];

			[self importExpenses];
		} else if (buttonIndex == 2) // Import and Match
        {
            [self setSmartExpenseMatching:YES];
            
            [self importExpenses];
        }
		
		return;
	}
    else if (actionSheet.tag == kActionViewAddReceipt)
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
        else if (buttonIndex == (1 + offset) && !hideReceiptStore)
        {
            [self buttonReceiptStorePressed];
        }
        return;
    }
    else if (actionSheet.tag == kActionSheetTravelAllowance)
    {
        [self handleTravelAllowanceActionSheet:buttonIndex];
    }
    else {
        [super actionSheet:actionSheet clickedButtonAtIndex:buttonIndex];
    }
}

    - (void)handleTravelAllowanceActionSheet:(NSInteger)buttonIndex
    {
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc]
                initWithObjectsAndKeys:
                        //@"YES", @"SKIP_PARSE",
                        //MOB-11958
                        ![self.role lengthIgnoreWhitespace] ? ROLE_EXPENSE_TRAVELER : self.role, @"ROLE",
                        @"YES", @"SHORT_CIRCUIT",
                        nil];
        if (rptDetail.employeeName != nil)
                pBag[@"EmployeeName"] = rptDetail.employeeName;
        pBag[@"ReportName"] = rptDetail.reportName;
        pBag[@"TotalPostedAmount"] = rptDetail.totalPostedAmount;
        pBag[@"CrnCode"] = rptDetail.crnCode;
        pBag[@"REPORT"] = rptDetail;
        pBag[@"RECORD_KEY"] = rptDetail.rptKey;
        pBag[@"REPORT_OG"] = rptDetail;

        UIStoryboard* storyboard = [UIStoryboard storyboardWithName:[@"ItineraryStoryboard" storyboardName] bundle:nil];

        if(buttonIndex == 0){
                //Itineraries
                ItineraryInitialViewController *o = (ItineraryInitialViewController *) [storyboard instantiateViewControllerWithIdentifier:@"ItineraryInitialViewController"];
                o.paramBag = pBag;
                o.hasCloseButton = YES;

                UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:o]; //TODO is this necessary?
    //            UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:o];

                localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
                [localNavigationController setToolbarHidden:NO];
                localNavigationController.toolbar.tintColor = [UIColor darkBlueConcur_iOS6];
                localNavigationController.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];

                [self presentViewController:localNavigationController animated:YES completion:nil];
            }
            else if (buttonIndex == 1){
                // Adjustments
                ItineraryAllowanceAdjustmentViewController *o = (ItineraryAllowanceAdjustmentViewController *) [storyboard instantiateViewControllerWithIdentifier:@"ItineraryAllowanceAdjustment"];
                o.rptKey = rpt.rptKey;
                o.crnCode = rpt.crnCode;
                o.role = self.role;
                BOOL canEdit = [self canEdit];
                o.hideGenerateExpenseButton = !canEdit;

                o.hasCloseButton = YES;

                UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:o]; //TODO is this necessary?
    //            UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:o];

                localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
                [localNavigationController setToolbarHidden:NO];
                localNavigationController.toolbar.tintColor = [UIColor darkBlueConcur_iOS6];
                localNavigationController.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];

                [self presentViewController:localNavigationController animated:YES completion:nil];

            }
            else{
                //Cancel

            }
    }

    @end







