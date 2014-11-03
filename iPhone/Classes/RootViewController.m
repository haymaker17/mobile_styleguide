////
////  RootViewController.m
////  ConcurMobile
////
////  Created by Paul Kramer on 10/27/09.
////  Copyright 2009 Concur. All rights reserved.
////
//
//#import <AddressBook/AddressBook.h>
//#import <AddressBookUI/AddressBookUI.h>
//#import "ExUnitTestsVC.h"
//#import "ReportEntryFormData.h"
//#import "SelectReportViewController.h"
//#import "FindMe.h"
//#import "CardsGetPersonalAndTransactions.h"
//#import "PCardTransaction.h"
//#import "FormatUtils.h"
//#import "SummaryData.h"
//#import "CurrencyData.h"
//#import "OOPEntry.h"
//#import "OutOfPocketData.h"
//#import "SystemConfig.h"
//#import "UserConfig.h"
//#import "ExSystem.h"
//
//#import "LoginViewController.h"
//#import "ExpenseListViewController.h"
//#import "EntryListViewController.h"
//#import "TripsViewController.h"
//#import "TripDetailsViewController.h"
//#import "ItinDetailsViewController.h"
//#import "ConcurMobileAppDelegate.h"
//#import "MoreVC.h"
//
//#import "MapViewController.h"
//#import "WebViewController.h"
//#import "MobileViewController.h"
//
//#import "ApproveEntriesViewController.h"
//#import "ApproveExpenseDetailViewController.h"
//#import "ApproveReportSummaryViewController.h"
//#import "ApproveReportViewItemizations.h"
//#import "ApproveReportViewAttendees.h"
//#import "OutOfPocketListViewController.h"
//#import "SettingsViewController.h"
//#import "ReportApprovalListViewController.h"
//#import "ActiveReportListViewController.h"
//#import "RequestController.h"
//
//#import "HotelViewController.h"
//#import "DistanceViewController.h"
//#import "HotelLocationViewController.h"
//#import "HotelTextEditorViewController.h"
//#import "HotelOptionsViewController.h"
//#import "HotelSearchResultsViewController.h"
//#import "RoomListViewController.h"
//#import "HotelDetailsViewController.h"
//#import "HotelDetailedMapViewController.h"
////#import "HotelBookingViewController.h"
//#import "HotelCreditCardViewController.h"
//
//#import "CarViewController.h"
//#import "CarListViewController.h"
//#import "CarMapViewController.h"
//
//#import "TrainBookVC.h"
//#import "SmartExpenseManager.h"
//#import "LabelConstants.h"
//#import "UncaughtExceptionHandler.h"
//#import "ExceptionLogSender.h"
//
//#import "ValidateSessionData.h"
//#import "AttendeeEntryEditViewController.h"
//#import "TextAreaEditVC.h"
//#import "MobileAlertView.h"
//#import "DateTimeConverter.h"
//#import "ReceiptStoreListView.h"
//#import "ExpenseTypesManager.h"
//#import "ReceiptDetailViewController.h"
//
//#import	"UploadReceiptData.h"
//#import "MobileActionSheet.h"
//#import "FileManager.h"
//
//#import "ReportDetailViewController.h"
//#import "ReportEntryViewController.h"
//#import "ReportSummaryViewController.h"
//#import "QEFormVC.h"
//#import "SafetyCheckInVC.h"
//#import "SearchYodleeCardsVC.h"
//#import "YodleeCardAgreementVC.h"
//
//#import "AirBookingCriteriaVC.h"
//#import "AppsUtil.h"
//#import "ApplicationLock.h"
//
//#import "TripItLinkVC.h"
//#import "TripItAuthVC.h" 
//
//#import "TravelRequestViewController.h"
//
//#import "UploadQueue.h"
//#import "UploadQueueViewController.h"
//
//#import "RegisterPush.h"
//#import "InvoiceCountData.h"
//
//#import "GovExpenseEditViewController.h"
//#import "GovUnappliedExpensesVC.h"
//#import "GovLoginNoticeVC.h"
//#import "GovSelectTANumVC.h"
//
//#import "GovDocumentManager.h"
//#import "GovDocumentListVC.h"
//
//
//
//// Action sheet button IDs
//#define BOOKINGS_BTN_AIR @"Book Air"
//#define BOOKINGS_BTN_HOTEL @"Book Hotel"
//#define BOOKINGS_BTN_CAR @"Book Car"
//#define BOOKINGS_BTN_RAIL @"Book Rail"
//
//// Home action sheet button IDs
//#define HOME_BTN_REFRESH_DATA @"Refresh Data"
//#define HOME_BTN_RECEIPTS @"Receipts"
//#define HOME_BTN_CAR_MILEAGE @"Car Mileage"
//#define HOME_BTN_LOCATION @"Location"
//#define HOME_BTN_INVOICES @"Invoices"
//
//const int APPS_ACTION_SHEET_TAG = 100;
//const int BOOKINGS_ACTION_SHEET_TAG = 101;
//
//@interface RootViewController(Private)
////-(void) makeTravelRequestRow:(NSString*) numberOfTRToApprove;
//-(void) showTravelRequests;
//-(void) showGovAuthorizations;
//-(void) showGovVouchers;
//-(void) showGovStampDocuments;
//-(void) showGovExpenses;
//@end
//
@implementation RootViewController
//
//@synthesize addExpenseClicked, isFetchingEntryForm;
//@synthesize showWhatsNew;
//
//@synthesize carRatesData, cameFromTripIt;
//@synthesize topView;
//@synthesize findMe, tableList;
//@synthesize aTripRows, aFindTravelRows, aExpenseRows, dictRowData;
//@synthesize navBar;
//@synthesize viewState, isFindingMe;
//@synthesize	lblNewTitle, ivTripItAd, btnTripItAd;
//@synthesize	scrollerNew;
//@synthesize	btnNewOK;
//@synthesize	ivNewBackground;
//@synthesize	viewNew;
//@synthesize lblBumpHelpTitle, lblBumpHelpText1, lblBumpHelpText2, btnBumpShare;
//@synthesize btnBumpCancel, ivBumpBackground, viewBumpHelp, rowData;
//@synthesize hasDrawn, whatsNewView;
//
//@synthesize sectionData;
//@synthesize sections, sectionKeys, inLowMemory;
//@synthesize adBannerView = _adBannerView;
//@synthesize adBannerViewIsVisible = _adBannerViewIsVisible;
//@synthesize contentView = _contentView;
//
//@synthesize meImageStatusDict;
//@synthesize enablefilterUnsubmittedActiveReports;
//@synthesize unassociatedReceiptObjects, aAppRows, homeData;
//@synthesize fetchedResultsController=__fetchedResultsController;
//@synthesize managedObjectContext=__managedObjectContext;
//@synthesize viewFooter, lblFooter, currentTrip, viewSUHeader;
//
//@synthesize  isLoggingOutAndReauthenticating;
//
//@synthesize invoiceCount;
//
//@synthesize bannerAdjusted;

//@synthesize postLoginAttribute;
//@synthesize allMessages;
//
//
//int kSectionCorpTrip = 0;
//int kSectionCorpQE = 1;
//int kSectionCorpReport = 2;
//
//const NSString* kRptRowKey = @"UnsubmittedReportsRow";
//const NSString* kAprRowKey = @"ReportsToApproveRow";
//
//-(void)setWasLandscape:(BOOL)val
//{
//	_wasLandscape = val;
//}
//
//-(BOOL)wasLandscape
//{
//	return _wasLandscape;
//}
//
//#pragma mark -
//#pragma mark Navigation Controller Methods
//- (void)navigationController:(UINavigationController *)navigationController willShowViewController:(UIViewController *)viewController animated:(BOOL)animated
//{
//    [viewController viewWillAppear:animated];
//}
//
//
//- (void)navigationController:(UINavigationController *)navigationController willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
//{
//	if ([[ExSystem sharedInstance] isBreeze])
//		[self fixupAdView:toInterfaceOrientation];
//}
//
//
//- (void)navigationController:(UINavigationController *)navigationController
//	   didShowViewController:(UIViewController *)viewController animated:(BOOL)animated
//{
//	MobileViewController *mvc = (MobileViewController *)viewController;
//	NSString *viewKey = @"";
//	
//	if(![viewController isKindOfClass:[RootViewController class]])
//		viewKey = [mvc getViewIDKey];
//    
//	if ([viewController isKindOfClass:[RootViewController class]])
//	{
//		[ExSystem sharedInstance].sys.topViewName = HOME_PAGE;
//        self.title = @"Concur";
//		
//		[self.navigationController popToRootViewControllerAnimated:NO];
//	}
//	else if(viewKey != nil)
//	{
//		if ([viewKey isEqualToString:ENTRIES])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:ENTRIES];
//		}
//		else if ([viewKey isEqualToString:TRIPS])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:TRIPS];
//		}
//		else if ([viewKey isEqualToString:TRIP_DETAILS])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:TRIP_DETAILS];
//		}
//		else if ([viewKey isEqualToString:ITIN_DETAILS])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:ITIN_DETAILS];
//		}
//		else if ([viewKey isEqualToString:LOGIN])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:LOGIN];
//		}
//		else if ([viewKey isEqualToString:APPROVE_REPORTS])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:APPROVE_REPORTS];
//		}
//        //		else if ([viewKey isEqualToString:APPROVE_ENTRIES])
//        //		{
//        //			[[ExSystem sharedInstance].sys setTopViewName:APPROVE_ENTRIES];
//        //
//        //				MobileViewController *mvc = (MobileViewController *)viewController;
//        //				Msg *msg = [[Msg alloc] initWithData:@"PRE_FETCH" State:@"" Position:nil MessageData:nil URI:@"" MessageResponder:nil ParameterBag:nil];
//        //				[mvc respondToFoundData:msg];
//        //				[msg release];
//        //		}
//		else if ([viewKey isEqualToString:ITEMIZATION_LIST])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:ITEMIZATION_LIST];
//		}
//		else if ([viewKey isEqualToString:APPROVE_EXPENSE_DETAILS])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:APPROVE_EXPENSE_DETAILS];
//		}
//		else if ([viewKey isEqualToString:EXPENSE_RECEIPT_MANAGER])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:EXPENSE_RECEIPT_MANAGER];
//		}
//		else if ([viewKey isEqualToString:APPROVE_REPORT_SUMMARY])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:APPROVE_REPORT_SUMMARY];
//		}
//		else if ([viewKey isEqualToString:APPROVE_VIEW_EXCEPTIONS])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:APPROVE_VIEW_EXCEPTIONS];
//		}
//		else if ([viewKey isEqualToString:APPROVE_VIEW_COMMENTS])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:APPROVE_VIEW_COMMENTS];
//		}
//		else if ([viewKey isEqualToString:APPROVE_VIEW_ITEMIZATIONS])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:APPROVE_VIEW_ITEMIZATIONS];
//		}
//		else if ([viewKey isEqualToString:OUT_OF_POCKET_LIST])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:OUT_OF_POCKET_LIST];
//            
//			if(viewState[OUT_OF_POCKET_LIST] != nil && [viewState[OUT_OF_POCKET_LIST] isEqualToString:@"FETCH"])
//			{
//				MobileViewController *mvc = (MobileViewController *)viewController;
//				Msg *msg = [[Msg alloc] initWithData:@"FORCE_FETCH" State:@"" Position:nil MessageData:nil URI:@"" MessageResponder:nil ParameterBag:nil];
//				[mvc respondToFoundData:msg];
//				[viewState removeObjectForKey:OUT_OF_POCKET_LIST];
//			}
//		}
//		else if ([viewKey isEqualToString:OUT_OF_POCKET_FORM])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:OUT_OF_POCKET_FORM];
//		}
//		else if ([viewKey isEqualToString:EXPENSE_TYPES_LIST])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:EXPENSE_TYPES_LIST];
//		}
//        else if ([viewKey isEqualToString:RECEIPT_DETAIL_VIEW])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:RECEIPT_DETAIL_VIEW];
//		}
//		else if ([viewKey isEqualToString:ACTIVE_REPORTS])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:ACTIVE_REPORTS];
//			
//			if(viewState[ACTIVE_REPORTS] != nil && [viewState[ACTIVE_REPORTS] isEqualToString:@"FETCH"])
//			{
//				// MOB-1868
//				NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
//											 ACTIVE_REPORTS, @"TO_VIEW", nil];
//				[[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORTS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES];
//				[viewState removeObjectForKey:ACTIVE_REPORTS];
//			}
//		}
//		else if ([viewKey isEqualToString:ACTIVE_ENTRIES])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:ACTIVE_ENTRIES];
//			
//            //			ReportDetailViewController *mvc = (ReportDetailViewController *)viewController;
//            //			Msg *msg = [[Msg alloc] initWithData:@"PRE_FETCH" State:@"" Position:nil MessageData:nil URI:@"" MessageResponder:nil ParameterBag:nil];
//            //			[mvc respondToFoundData:msg];
//            //			[msg release];
//		}
//		else if ([viewKey isEqualToString:APPROVE_INVOICES])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:APPROVE_INVOICES];
//		}
//		else if ([viewKey isEqualToString:INVOICE_LINEITEMS])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:INVOICE_LINEITEMS];
//			
//			MobileViewController *mvc = (MobileViewController *)viewController;
//			Msg *msg = [[Msg alloc] initWithData:@"PRE_FETCH" State:@"" Position:nil MessageData:nil URI:@"" MessageResponder:nil ParameterBag:nil];
//			[mvc respondToFoundData:msg];
//		}
//		else if([viewKey isEqualToString:RECEIPT_STORE_VIEWER])
//		{
//			[[ExSystem sharedInstance].sys setTopViewName:RECEIPT_STORE_VIEWER];
//		}
//	}
//}
//
//
//#pragma mark -
//#pragma mark ViewController Methods
//// The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
//- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
//    //    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
//    //        // Custom initialization
//    //    }
//    return self;
//}
//
//
//- (void)displayTraveTextAd
//{
//    //MOB-10737 iPhone 5 support
//    // MOB-8133
//    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
//    // MOB-11145
//    if([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
//    {
//        CGFloat h = 420.0;        //adjustable height based device screen
//        self.ivTripItAd.hidden = YES;
//        self.btnTripItAd.hidden = YES;
//        
//        if([ExSystem is5])
//            h += 88.0;
//        self.tableList.frame = CGRectMake(0, 0, 320, h);
//    }
//    else
//    {
//        CGFloat h = 368.0;        //adjustable height based device screen
//        //MOB-11909 - Added check for TripIt ad role
//        if([[ExSystem sharedInstance] hasRole:ROLE_TRIPITAD_USER])
//        {
//            self.ivTripItAd.hidden = NO;
//            self.btnTripItAd.hidden = NO;
//        }
//        else
//        {
//            self.ivTripItAd.hidden = YES;
//            self.btnTripItAd.hidden = YES;
//        }
//        if([ExSystem is5])
//            h += 88.0;
//        
//        if (itemNum > 0)
//            h -= 37;
//        self.tableList.frame = CGRectMake(0, 0, 320, h);
//    }
//}
//
//-(void)viewWillAppear:(BOOL)animated
//{
//	[self.navigationController setToolbarHidden:YES animated:NO];
//	
//	if ([[ExSystem sharedInstance]isBreeze])
//		[self fixupAdView:[UIDevice currentDevice].orientation];
//	[super viewWillAppear:animated];
//	
//    self.navigationItem.title = @"Concur";
//    
//    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
//    if (itemNum > 0)
//    {
//        [self makeUploadView];
//        [self adjustViewForUploadBanner];
//    }
//    else
//        [self adjustViewForNoUploadBanner];
//    
//    
//}
//
//- (void)viewDidAppear:(BOOL)animated
//{
//	[super viewDidAppear:animated];
//    
//    self.title = @"Concur";
//	
//	[[ApplicationLock sharedInstance] onHomeScreenAppeared];
//	
//	if ([ExSystem connectedToNetwork] && findMe == nil && [ExSystem sharedInstance].sessionID != nil && isFindingMe == NO)
//	{
//		isFindingMe = YES;
//		[self performSelector:@selector(doFindMe) withObject:nil afterDelay:0.5f];
//	}
//    
//    if ([[ApplicationLock sharedInstance] isLoggedIn])
//	{
//		if (requireHomeScreenRefresh)
//		{
//            [self fetchHomePageData];
//            requireHomeScreenRefresh = false;
//		}
//	}
//    
//    if(showWhatsNew)
//        [self disableSettingsAndLogoutButtons];
//    else
//        [self enableSettingsAndLogoutButtons];
//    
//    [self displayTraveTextAd];
//}
//
//
//// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
//- (void)viewDidLoadImpl
//{
//	if(!inLowMemory)
//	{
//		/////BEGIN INLINE EXPANSION OF doBaseInit
//		[ExSystem sharedInstance].msgControl.rootVC = self;
//		
//        self.postLoginAttribute = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil]; //determine if 'safeHarbor' message needs to display. Contain parsed value from loginResult xml message
//		viewState = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil]; //used to figure out if a view is in save mode or not, or if data has changed... so we don't go off and fetch stuff all the time blindly
//		//self.settings = [SettingsData getInstance];
//		self.showWhatsNew = [[ExSystem sharedInstance].sys.showWhatsNew boolValue];
//        
//		if(![UIDevice isPad] && showWhatsNew)
//		{
//            // this sets a class variable... :/
//            [self getWhatsNewView];
//            if(self.whatsNewView != nil)
//            {
//                [self.view addSubview:whatsNewView];
//                [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didCloseWhatsNew:) name:@"DidCloseWhatsNew" object:nil];
//                [self.view bringSubviewToFront:whatsNewView];
//            }
//		}
//		
//		[[ExSystem sharedInstance] initURLMaps];
//		
//		/////END INLINE EXPANSION OF doBaseInit
//        
//		[self switchToView:HOME_PAGE viewFrom:nil ParameterBag:nil];
//	}
//	
//    if ([Config isGov])
//    {
//        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//        if (![BaseManager hasEntriesForEntityName:@"EntityWarningMessages" withContext:[ad managedObjectContext]])
//            [[ExSystem sharedInstance].msgControl createMsg:GOV_WARNING_MSG CacheOnly:@"NO" ParameterBag:nil SkipCache:NO RespondTo:self];
//    }
//    
//    self.title = @"Concur";
//    
//	// If a flag indicates that an uncaught exception occured the last time the app ran
//	if (DetectedUncaughtException())
//	{
//        // Log failed cache id
//        [[ExSystem sharedInstance].cacheData clearLastCorruptedCache];
//        
//		// Clear the flag
//		SetDetectedUncaughtException(NO);
//        
//		// Ask the user if they want to send the crash log
//		[ExceptionLogSender offerToSendExceptionLogFromViewController:self];
//	}
//}
//
//// creates what's new view and returns a reference to it.  This is so the new 9.0 UI can use the old what's new view.
//- (WhatsNewView *)getWhatsNewView
//{
//    self.whatsNewView = [[NSBundle mainBundle] loadNibNamed:@"WhatsNewView" owner:self options:nil][0];
//    
//    //MOB-10749 iPhone 5 what's new support
//    CGFloat h = 480;
//    if ([ExSystem is5])
//    {
//        h += 88.0;
//    }
//    
//    whatsNewView.frame = CGRectMake(0, 0, 320, h - 64);
//    
//    whatsNewView.scroller.contentSize = CGSizeMake(280 * 2, 288);
//    [self makeWhatsNew];
//    return self.whatsNewView;
//}
//
//// creates what's new view and returns a reference to it.  This is so the new 9.0 UI can use the old what's new view.
//- (WhatsNewView *)getWhatsNewViewForIPad
//{
//    self.whatsNewView = [[NSBundle mainBundle] loadNibNamed:@"WhatsNewView" owner:self options:nil][0];
//    
//    CGFloat w = 768;
//    CGFloat h = 1024;
//    if ([ExSystem isLandscape]) {
//        w = 1024;
//        h = 768;
//    }
//    
//    whatsNewView.frame = CGRectMake(0, 0, w, h);
//    
//    whatsNewView.scroller.contentSize = CGSizeMake(280 * 2, 288);
//    //[self.view addSubview:whatsNewView];
//    [self makeWhatsNew];
//    
//    return self.whatsNewView;
//}
//
//
//- (void)viewDidLoad
//{
//    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//    self.managedObjectContext = [ad managedObjectContext];
//    [self refetchData];
//
//	showWhatsNew = [[ExSystem sharedInstance].sys.showWhatsNew boolValue];
//	[viewNew setHidden:YES];
//	[viewBumpHelp setHidden:YES];
//	[super viewDidLoad];
//    
//    [viewSUHeader setHidden:YES];
//    [viewFooter setHidden:YES]; // MOB-10032 Need to setHidden after super viewDidLoad.
//    
//	if(!inLowMemory)
//	{
//		[self performSelector:@selector(viewDidLoadImpl) withObject:nil afterDelay:0.01f];
//	}
//	else {
//		[self viewDidLoadImpl];
//	}
//    
//    UIImageView* img = nil;
//
//    if ([ExSystem is7Plus])
//        img = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"concur_logo_iOS7"]];
//    else
//        img = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"concur_logo_titlebar"]];
//    self.title = @"Concur";
//    
//    self.navigationItem.titleView = img;
//
//    self.navigationController.navigationBar.alpha = 0.9f;
//    
//    if (![ExSystem is7Plus]) {
//        self.navigationController.toolbar.tintColor = [UIColor darkBlueConcur_iOS6];
//        self.navigationController.toolbar.alpha = 0.9f;
//    }
//    
//    if([[ExSystem sharedInstance]isBreeze])
//    {
//        NSDictionary *dictionary = @{@"Type": @"Breeze"};
//        [Flurry logEvent:@"User: Type" withParameters:dictionary];
//    }
//    else if([ExSystem sharedInstance].isBronxUser)
//    {
//        NSDictionary *dictionary = @{@"Type": @"Bronx"};
//        [Flurry logEvent:@"User: Type" withParameters:dictionary];
//    }
//    else if ([ExSystem sharedInstance].sys.productLine == PROD_GOVERNMENT){
//        NSDictionary *dictionary = @{@"Type": @"Gov"};
//        [Flurry logEvent:@"User: Type" withParameters:dictionary];
//    }
//    else if (![[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER]){
//        NSDictionary *dictionary = @{@"Type": @" Expense Only"};
//        [Flurry logEvent:@"User: Type" withParameters:dictionary];
//    }
//    else if (![[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER]){
//        NSDictionary *dictionary = @{@"Type": @" Travel Only"};
//        [Flurry logEvent:@"User: Type" withParameters:dictionary];
//    }
//    else{
//        NSDictionary *dictionary = @{@"Type": @"CTE"};
//        [Flurry logEvent:@"User: Type" withParameters:dictionary];
//    }
//    // first time add upload queue banner to root view
//    // Only adjust banner once, in case view will appear called multiple times.
//    self.bannerAdjusted = NO;
//}
//
//- (void)viewDidUnload {
//	// Release any retained subviews of the main view.
//	// e.g. self.myOutlet = nil;
//	
//	self.lblNewTitle = nil;
//	self.scrollerNew = nil;
//	self.btnNewOK = nil;
//	self.ivNewBackground = nil;
//	self.viewNew = nil;
//    self.lblFooter = nil;
//    self.viewFooter = nil;
//    
//    self.ivTripItAd = nil;
//    self.btnTripItAd = nil;
//}
//
//- (void)didReceiveMemoryWarning {
//    [super didReceiveMemoryWarning];
//	self.inLowMemory = YES;
//	// Release any cached data, images, etc that aren't in use.
//}
//
//#pragma mark - Rotation Orientation Stuff
//- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
//{
//	if((toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight)  && showWhatsNew)
//		[self adjustWhatsNewLandscape];
//	else
//		[self adjustWhatsNewPortrait];
//    
//	[tableList reloadData];
//	if ([[ExSystem sharedInstance]isBreeze])
//		[self fixupAdView:toInterfaceOrientation];
//    
//}
//
//#pragma mark -
//#pragma mark Whats New Stuff
//-(void) hideNew:(id)sender
//{
//	[viewNew setHidden:YES];
//}
//
//
//-(IBAction) closeNew:(id)sender
//{
//	self.showWhatsNew = NO;
//	[ExSystem sharedInstance].sys.showWhatsNew = NO;
//    [[ExSystem sharedInstance] saveSystem];
//	CGContextRef context = UIGraphicsGetCurrentContext();
//	[UIView beginAnimations:nil context:context];
//	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
//	[UIView setAnimationDidStopSelector:@selector(hideNew:)];
//	[UIView setAnimationDuration:0.33];
//	[viewNew setAlpha:0.0f];
//	[UIView commitAnimations];
//	
//    // Good place for starting version based data migration, e.g. receipt migration
//}
//
//
//-(void) adjustWhatsNewLandscape
//{
//	viewNew.frame = CGRectMake(0, 0, 480, 320);
//	NSString *imgName = @"iPhonelandscape_whats_new_dialog";
//	ivNewBackground.image = [UIImage imageNamed:imgName]; //iPhonelandscape_whats_new_dialog
//	ivNewBackground.frame = CGRectMake((480 - 303) / 2, 2, 303, 266);
//	
//	btnNewOK.frame = CGRectMake((480 - 187) / 2, 230, 187, 31);
//	lblNewTitle.frame = CGRectMake((480 - 280) / 2, 15, 280, 29);
//	scrollerNew.frame = CGRectMake((480 - 280) / 2, 50, 280, 175);
//	[self makeWhatsNew];
//	
//}
//
//
//-(void)adjustWhatsNewPortrait
//{
//	viewNew.frame = CGRectMake(0, 0, 320, 480);
//	NSString *imgName = @"iPhonelandscape_whats_new_dialog";
//	ivNewBackground.image = [UIImage imageNamed:imgName]; //iPhonelandscape_whats_new_dialog
//	ivNewBackground.frame = CGRectMake(8, 50, 303, 324);
//	
//	btnNewOK.frame = CGRectMake(66, 331, 187, 31);
//	lblNewTitle.frame = CGRectMake(20, 64, 280, 29);
//	scrollerNew.frame = CGRectMake(30, 105, 260, 218);
//	
//	[self makeWhatsNew];
//}
//
//
//
//#pragma mark -
//#pragma mark View Switching Methods
//-(MobileViewController *)getController:(NSString *)to
//{//this is the factory to build out the views
//	//Even though the controller that we are getting is the MVC, we actually want to create the nib with the desired form class,
//	//this is the class that gets inserted into the dictionary.
//	//add in the one time run code defines the view here, not in switchviews, which does things like like the addInfobutton or specifying the message to create and run
//    
//	///MobileViewController *nextController = [self.views objectForKey:to];
//	__autoreleasing MobileViewController *nextController = [RootViewController getMobileViewControllerByViewIdKey:(NSString*)to];
//    
//	
//	if (nextController == nil)
//	{
//		if ([to isEqualToString:HOME_PAGE])
//		{//Home Page View
//			//nextController = [[HomePageViewController alloc] initWithNibName:@"HomePageView" bundle:nil];
//		}
//		else if ([to isEqualToString:LOGIN])
//		{//Login virew
//			nextController = [[LoginViewController alloc] initWithNibName:@"LoginView" bundle:nil];
//		}
//		else if ([to isEqualToString:TRIPS])
//		{//Trips List view
//			nextController = [[TripsViewController alloc] initWithNibName:@"TripsView" bundle:nil];
//		}
//		else if ([to isEqualToString:TRIP_DETAILS])
//		{//Trip Details List
//			nextController = [[TripDetailsViewController alloc] initWithNibName:@"TripDetailsView" bundle:nil];
//		}
//		else if ([to isEqualToString:ITIN_DETAILS_AIR])
//		{//Segment Details Air
//			nextController = [[ItinDetailsViewController alloc] initWithNibName:@"ItinDetailsViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:ITIN_DETAILS])
//		{//Itin Segment Details View
//			nextController = [[ItinDetailsViewController alloc] initWithNibName:@"ItinDetailsViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:MAP])
//		{//go to maps from itin detail
//			nextController = [[MapViewController alloc] initWithNibName:@"MapView" bundle:nil];
//		}
//		else if ([to isEqualToString:WEBVIEW])
//		{//go to webview from itin detail
//			nextController = [[WebViewController alloc] initWithNibName:@"WebView" bundle:nil];
//		}
//		else if ([to isEqualToString:SETTINGS_VIEW])
//		{//Settings view
//			nextController = [[SettingsViewController alloc] init];
//		}
//		else if ([to isEqualToString:APPROVE_REPORTS])
//		{
//			// move to report list view
//			nextController = [[ReportApprovalListViewController alloc] initWithNibName:@"ReportApprovalListViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:APPROVE_EXPENSE_DETAILS])
//		{
//			// drill to the expense detail view from the report view
//			nextController = [[ReportEntryViewController alloc] initWithNibName:@"EntryHeaderView" bundle:nil];
//		}
//		else if ([to isEqualToString:APPROVE_REPORT_SUMMARY])
//		{
//			// drill to report summary view from the report view
//            nextController = [[ReportSummaryViewController alloc] initWithNibName:@"ReportHeaderView" bundle:nil];
//		}
//		else if ([to isEqualToString:RECEIPT_STORE_VIEWER])
//		{
//			nextController = [[ReceiptStoreListView alloc] initWithNibName:@"ReceiptStoreListView" bundle:nil];
//		}
//		else if ([to isEqualToString:OUT_OF_POCKET_LIST])
//		{
//			nextController = [[OutOfPocketListViewController alloc] initWithNibName:@"OutOfPocketListViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:EXPENSE_TYPES_LIST])
//		{
//			nextController = [[ExpenseTypesViewController alloc] initWithNibName:@"ExpenseTypesViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:ACTIVE_REPORTS])
//		{
//			// drill to the report view from the report list view
//			nextController = (MobileViewController *)[[ActiveReportListViewController alloc] initWithNibName:@"MobileTableViewController" bundle:nil]; //initWithNibName:@"ReportApprovalListViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:ACTIVE_ENTRIES] || [to isEqualToString:APPROVE_ENTRIES])
//		{
//			// drill to the report view from the report list view
//			nextController = [[ReportDetailViewController alloc] initWithNibName:@"ReportHeaderView" bundle:nil];
//		}
//        else if ([to isEqualToString:RECEIPT_DETAIL_VIEW])
//		{
//			// drill to the report view from the report list view
//			nextController = [[ReceiptDetailViewController alloc] initWithNibName:@"ReceiptDetailViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:DISTANCE])
//		{
//			nextController = [[DistanceViewController alloc] initWithNibName:@"DistanceViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:HOTEL_LOCATION])
//		{
//			nextController = [[HotelLocationViewController alloc] initWithNibName:@"HotelLocationViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:HOTEL_TEXT_EDITOR])
//		{
//			nextController = [[HotelTextEditorViewController alloc] initWithNibName:@"HotelTextEditorViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:HOTEL_OPTIONS])
//		{
//			nextController = [[HotelOptionsViewController alloc] initWithNibName:@"HotelOptionsViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:HOTEL_SEARCH_RESULTS])
//		{
//			nextController = [[HotelSearchResultsViewController alloc] initWithNibName:@"HotelSearchResultsViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:HOTEL_ROOM_LIST])
//		{
//			nextController = [[RoomListViewController alloc] initWithNibName:@"RoomListViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:HOTEL_DETAILS])
//		{
//			nextController = [[HotelDetailsViewController alloc] initWithNibName:@"HotelDetailsViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:HOTEL_DETAILED_MAP])
//		{
//			nextController = [[HotelDetailedMapViewController alloc] initWithNibName:@"HotelDetailedMapViewController" bundle:nil];
//		}
//        //		else if ([to isEqualToString:HOTEL_BOOKING])
//        //		{
//        //			nextController = [[HotelBookingViewController alloc] initWithNibName:@"HotelBookingViewController" bundle:nil];
//        //		}
//		else if ([to isEqualToString:HOTEL_CREDIT_CARD])
//		{
//			nextController = [[HotelCreditCardViewController alloc] initWithNibName:@"HotelCreditCardViewController" bundle:nil];
//		}
////		else if ([to isEqualToString:CAR])
////		{
////			nextController = [[CarViewController alloc] initWithNibName:@"CarViewController" bundle:nil];
////		}
//		else if ([to isEqualToString:CAR_LIST])
//		{
//			nextController = [[CarListViewController alloc] initWithNibName:@"CarListViewController" bundle:nil];
//		}
//        //		else if ([to isEqualToString:CAR_DETAILS])
//        //		{
//        //			nextController = [[CarDetailsViewController alloc] initWithNibName:@"CarDetailsViewController" bundle:nil];
//        //		}
//		else if ([to isEqualToString:CAR_MAP])
//		{
//			nextController = [[CarMapViewController alloc] initWithNibName:@"CarMapViewController" bundle:nil];
//		}
//		else if ([to isEqualToString:TRAIN_BOOK])
//		{
//			nextController = [[TrainBookVC alloc] initWithNibName:@"TrainBookVC" bundle:nil];
//		}
//		else if ([to isEqualToString:TEXT_FIELD_EDIT])
//		{
//			nextController = [[TextAreaEditVC alloc] initWithNibName:@"TextAreaEditView" bundle:nil];
//		}
//	}
//	
//	if (nextController != nil)
//	{
//        if(![to isEqualToString:@"HOME_PAGE"])
//            nextController.title = [Localizer getViewTitle:to]; //causing a exec bad access when running on device... might be me messing around with Breeze...
//		
//		///[self.views setObject:nextController forKey:to];
//		// MOB-10761 use __autoreleaseing qualifier for this one, need to test for memory leak
//        //		if (didAlloc)
//        //			[nextController autorelease];
//		
//		return nextController; ///[views objectForKey:to];
//	}
//	else {
//		// TODO - fix this
//		return nil;
//	}
//    
//	return nil;
//}
//
//
//-(void)pushOnToViewStack:(MobileViewController *)toView FromView:(MobileViewController *)fromView
//{//this method actually does the popping and pushing of things on to the stack
//	
//	if([[toView getViewDisplayType] isEqualToString:VIEW_DISPLAY_TYPE_NAVI])
//	{
//        //		if([[fromView getViewIDKey] isEqualToString:HOME_PAGE])
//        //		{
//        ////			ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//        ////			[delegate.navController.navigationBar setHidden:NO];
//        //			[self.navigationController pushViewController:toView animated:YES];
//        //		}
//        //		else
//        //		{
//        ////			ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//        ////			[delegate.navController.navigationBar setHidden:NO];
//        //        [self.navigationController pushViewController:toView animated:YES];
//        //		}
//        //Use navcontroller from delegate for new Home to work.
//        ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//        [delegate.navController pushViewController:toView animated:YES];
//		
//	}
//	else if([[toView getViewDisplayType] isEqualToString:VIEW_DISPLAY_TYPE_REGULAR])
//	{
//		//get individual animation information from the view and then invoke it here against the main UIView
//		if (fromView != nil)
//		{
//			[fromView.view removeFromSuperview];
//			fromView = nil;
//		}
//        
//        if(![[toView getViewIDKey] isEqualToString:HOME_PAGE])
//            [self.view insertSubview:toView.view atIndex:0];
//	}
//    else if([[toView getViewDisplayType] isEqualToString:@"VIEW_DISPLAY_TYPE_MODAL_NAVI"])
//	{
//        UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:toView];
//		[self presentViewController:navi animated:YES completion:nil];
//	}
//	else if([[toView getViewDisplayType] isEqualToString:VIEW_DISPLAY_TYPE_MODAL])
//	{
//		[self presentViewController:toView animated:YES completion:nil];
//	}
//	
//	[[ExSystem sharedInstance].sys setTopViewName:[toView getViewIDKey]];
//}
//
//-(void) showManualLoginView
//{
//	if ([RootViewController isLoginViewShowing])
//		return;
//    
//	LoginViewController *lvc = [[LoginViewController alloc] initWithNibName:@"LoginView" bundle:nil];
//	UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:lvc];
//	navi.modalPresentationStyle = UIModalPresentationFormSheet;
//	[self presentViewController:navi animated:YES completion:nil];
//	self.topView = LOGIN;
//}
//
////main view switch up routine
//// False, failed to switch and True, successfully switched.
//-(BOOL)switchToView:(NSString *)to viewFrom:(NSString *)from ParameterBag:(NSMutableDictionary *)paramBag
//{
//	NSString *msgName = nil;
//	NSString *cacheOnly = @"NO";
//	
//	MobileViewController *currController = nil;
//	MobileViewController *nextController = nil;
//    
//	ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//    UINavigationController *navcontroller = [ConcurMobileAppDelegate getBaseNavigationController] ;
//    
//	// If the view is already on top (as a result of double-click), let's return false
//	UIViewController*	topVC = delegate.navController.topViewController;
//	if (topVC != nil &&
//		[topVC isKindOfClass:MobileViewController.class] &&
//		[to isEqualToString:[((MobileViewController*)topVC) getViewIDKey]] &&
//		![to isEqualToString:HOME_PAGE] &&
//		[ExSystem sharedInstance].sessionID != nil &&
//		[[ExSystem sharedInstance].sessionID length] > 1)
//		return FALSE;
//	
//	if([to isEqualToString:HOME_PAGE] || [to isEqualToString:SETTINGS_VIEW])
//	{
//		[navcontroller setToolbarHidden:YES animated:NO];
//	}
//	else {
//		[navcontroller setToolbarHidden:NO animated:NO];
//	}
//	
//	if([to isEqualToString:HOME_PAGE] && [from isEqualToString:AUTHENTICATION_VIEW])
//	{
//		currController = [self getController:from];
//
//		[currController.view removeFromSuperview];
//		///[views removeObjectForKey:AUTHENTICATION_VIEW];
//		[self viewDidAppear:NO];
//		return TRUE;
//	}
//	
//	nextController = [self getController:to];
//	currController = [self getController:from];
//	self.topView = to;
//	[ExSystem sharedInstance].sys.topViewName = to;
//	
//	// Mob-2866 This causes logout button to be disabled immediately after login
//	//self.navigationItem.rightBarButtonItem = nil; //remove the right button
//	
//	//if(![to isEqualToString:HOME_PAGE])
//    [delegate.navController.navigationBar setHidden:NO];
//	//else
//    //[delegate.navController.navigationBar setHidden:YES];
//    // Not sure when this is in use.  Just comment out for now.
//    //    if ([UIDevice isPad] && [ExSystem sharedInstance].isSingleUser) {
//    //        [delegate.navController.navigationBar setHidden:YES];
//    //    }
//    
//	if ([to isEqualToString:SETTINGS_VIEW])
//	{
//		nextController.cameFrom = from;
//        
//	}
//    
//	if ([from isEqualToString:SPLASH])
//	{
//        
//	}
//	else if(from == nil)
//	{
//		from = [ExSystem sharedInstance].sys.topViewName;
//	}
//	
//	if ([to isEqualToString:TRIP_DETAILS] || [to isEqualToString:ITIN_DETAILS_AIR])
//	{//drill to the Trip Details view from the Trips List view
//		msgName = TRIPS_DATA;
//		cacheOnly = @"YES";
//        
//		paramBag[@"SKIP_PARSE"] = @"YES";
//	}
//	else if ([to isEqualToString:TRIPS])
//	{//go from Trip Details view to the Trip List view
//		msgName = TRIPS_DATA;
//	}
//	else if ([to isEqualToString:ITIN_DETAILS])
//	{//drill to the itin details view from the Trip Detail view
//	}
//	else if ([to isEqualToString:MAP])
//	{//go to maps from itin detail
//		msgName = @"LOCATION";
//	}
//	else if ([to isEqualToString:APPROVE_REPORTS])
//	{
//		msgName = REPORT_APPROVAL_LIST_DATA;
//	}
//	else if ([to isEqualToString:APPROVE_VIEW_EXCEPTIONS])
//	{
//		// go from report view to the view exceptions view
//        msgName = APPROVE_REPORT_DETAIL_DATA;
//		cacheOnly = @"YES";
//	}
//	else if ([to isEqualToString:APPROVE_VIEW_COMMENTS])
//	{
//		// go from report view to the view comments view
//        msgName = APPROVE_REPORT_DETAIL_DATA;
//		cacheOnly = @"YES";
//	}
//	else if ([to isEqualToString:ITEMIZATION_LIST] ||
//			 [to isEqualToString:APPROVE_VIEW_ITEMIZATIONS] || [to isEqualToString:APPROVE_VIEW_ATTENDEES])
//	{
//		NSString* role = (NSString*)paramBag[@"ROLE"];
//		if (paramBag == nil || role == nil || ![role isEqualToString:ROLE_EXPENSE_TRAVELER])
//		{
//			msgName = APPROVE_REPORT_DETAIL_DATA;
//		}
//		else
//		{
//			msgName = ACTIVE_REPORT_DETAIL_DATA;
//		}
//		cacheOnly = @"YES";
//	}
//    //	else if ([to isEqualToString:RECEIPT_MANAGER])
//    //	{
//    //		if(paramBag != nil)
//    //		{
//    //			nextController.parameterBag = paramBag;
//    //		}
//    //	}
//	else if ([to isEqualToString:OUT_OF_POCKET_LIST])
//	{
//        msgName = OOPES_DATA;
//	}
//	else if ([to isEqualToString:OUT_OF_POCKET_FORM])
//	{
//        msgName = OOPES_DATA;
//		//cacheOnly = @"YES";
//		paramBag[@"SKIP_CACHE"] = @"YES";
//		paramBag[@"NO_STATUS"] = @"YES";
//		
//		paramBag[@"SKIP_PARSE"] = @"YES";
//	}
//	else if ([to isEqualToString:AUTHENTICATION_VIEW])
//	{
//        msgName = AUTHENTICATION_DATA;
//	}
//	else if ([to isEqualToString:ACTIVE_REPORTS])
//	{
//		msgName = ACTIVE_REPORTS_DATA;
//	}
//    else if ([to isEqualToString:APPROVE_EXPENSE_DETAILS]
//             || [to isEqualToString:APPROVE_REPORT_SUMMARY]
//             || [to isEqualToString:APPROVE_ENTRIES]
//             || [to isEqualToString:ACTIVE_ENTRIES]) // From AddToReport or ActiveReports
//    {
//        [(ReportViewControllerBase*)nextController setSeedData:paramBag];
//        //		msgName = ACTIVE_REPORT_DETAIL_DATA;
//        //		cacheOnly = @"NO";
//	}
//	else if ([to isEqualToString:HOTEL_ROOM_LIST])
//	{
//		[(RoomListViewController*)nextController initData:paramBag];
//		msgName = FIND_HOTEL_ROOMS;
//		cacheOnly = @"NO";
//	}
//	else if ([to isEqualToString:HOTEL_BOOKING])
//	{
//		[(RoomListViewController*)nextController initData:paramBag];
//	}
//	else if ([to isEqualToString:HOTEL_OPTIONS])
//	{
//		HotelOptionsViewController *hotelOptionsViewController = (HotelOptionsViewController*)nextController;
//		hotelOptionsViewController.optionTitle = (NSString*)paramBag[@"TITLE"];
//	}
//	else if ([to isEqualToString:HOTEL_TEXT_EDITOR])
//	{
//		HotelTextEditorViewController *hotelTextEditorViewController = (HotelTextEditorViewController*)nextController;
//		hotelTextEditorViewController.customTitle = (NSString*)paramBag[@"TITLE"];
//	}
//	else if ([to isEqualToString:APPROVE_INVOICES])
//	{
//        msgName = APPROVE_INVOICES_DATA;
//		cacheOnly = @"NO";
//	}
//	else if ([to isEqualToString:INVOICE_LINEITEMS])
//	{
//        msgName = INVOICE_DETAIL_DATA;
//		cacheOnly = @"NO";
//	}
//	else if ([to isEqualToString:TEXT_FIELD_EDIT])
//	{
//		TextAreaEditVC *editVC = (TextAreaEditVC*)nextController;
//		editVC.title = (NSString*)paramBag[@"TITLE"];
//	}
//	
//	BOOL pushView = YES;
//	if(paramBag != nil && (paramBag[@"DONTPUSHVIEW"] != nil))
//		pushView = NO;
//    
//	BOOL popView = NO;
//	if(paramBag != nil && (paramBag[@"POPTOVIEW"] != nil))
//		popView = YES;
//	
//	BOOL popUntilView = NO;
//	if (paramBag != nil && (paramBag[@"POPUNTILVIEW"] != nil))
//		popUntilView = YES;
//	
//	BOOL skipCache = NO;
//	if(paramBag != nil && (paramBag[@"SKIP_CACHE"] != nil))
//		skipCache = YES;
//    
//	// Pop to root, before push and pop
//	if(paramBag != nil && (paramBag[@"POP_TO_ROOT_VIEW"] != nil))
//	{
//		[navcontroller popToRootViewControllerAnimated:NO];
//	}
//	
//	if(pushView && nextController != nil)
//	{
//		[[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::switchToView: pushOnToViewStack to: %@, from: %@", to, from] Level:MC_LOG_DEBU];
//        //MOB-10136 support add car/ add hotel popup after booking air.
//        if([to isEqualToString:TRIP_DETAILS] && [from isEqualToString:AIR_SELL])
//            [nextController setCameFrom:from];
//		[self pushOnToViewStack:nextController FromView:currController]; //this does all the pushing and popping
//	}
//	else if(popView)
//	{
//		[[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::switchToView: popViewControllerAnimated: %@", from] Level:MC_LOG_DEBU];
//		[navcontroller popViewControllerAnimated:YES];
//	}
//	else if (popUntilView)
//	{
//		[[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::switchToView: popToViewController: %@", to] Level:MC_LOG_DEBU];
//		[navcontroller popToViewController:nextController animated:YES];
//	}
//    
//	if (msgName != nil)
//	{//if a view specifies the message that it wants to run/create by name, then do it.  This is the start of fetching data for a view
//		if (paramBag == nil)
//		{
//			paramBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:to, @"TO_VIEW", nil];
//		}
//		else
//		{
//			paramBag[@"TO_VIEW"] = to;
//		}
//		
//		if (paramBag[@"SHORT_CIRCUIT"] == nil)
//		{
//			if(skipCache)
//				paramBag[@"SKIP_CACHE"] = @"YES";
//            
//			paramBag[@"CACHE_ONLY"] = cacheOnly;
//			
//			paramBag[@"MSG_NAME"] = msgName;
//			[self performSelector:@selector(fetchData:) withObject:paramBag afterDelay:0.1f];
//			//[msgControl createMsg:msgName CacheOnly:cacheOnly ParameterBag:parameterBag SkipCache:skipCache];
//		}
//	}
//    
//	if ([from isEqualToString:AUTHENTICATION_VIEW])
//	{
//		[currController.view removeFromSuperview];
//	}
//	else if ([[currController getViewDisplayType] isEqualToString:VIEW_DISPLAY_TYPE_REGULAR])//([from isEqualToString:HOME_PAGE] || [from isEqualToString:LOGIN] || [from isEqualToString:SETTINGS_VIEW])
//	{
//	}
//	else if ([[nextController getViewDisplayType] isEqualToString:VIEW_DISPLAY_TYPE_REGULAR])//([to isEqualToString:HOME_PAGE] || [to isEqualToString:LOGIN] || [to isEqualToString:SETTINGS_VIEW] || [to isEqualToString:AUTHENTICATION_VIEW] )
//	{
//	}
//	else
//	{
//	}
//    
//	
//	if (paramBag[@"SHORT_CIRCUIT"] != nil)
//	{
//		Msg *msg = [[Msg alloc] init];
//		msg.parameterBag = paramBag;
//		msg.idKey = @"SHORT_CIRCUIT";
//		[nextController respondToFoundData:msg];
//	}
//	
//	return TRUE;
//}
//
//-(void)fetchData:(NSMutableDictionary *)pBag
//{
//	NSString *msgName = pBag[@"MSG_NAME"];
//	
//	BOOL skipCache = NO;
//	if(pBag != nil && (pBag[@"SKIP_CACHE"] != nil))
//		skipCache = YES;
//    
//	NSString *cacheOnly = pBag[@"CACHE_ONLY"] ;
//	[[ExSystem sharedInstance].msgControl createMsg:msgName CacheOnly:cacheOnly ParameterBag:pBag SkipCache:skipCache];
//}
//
//
////detects what button was pressed and does an action abased on that
//-(IBAction)switchViewsByTag:(id)sender
//{
//	UIControl *btn = (UIControl*)sender;
//	
//	if (btn.tag == 100002)
//	{//sign in button and the home button
//		[self switchToView:SETTINGS_VIEW viewFrom:LOGIN ParameterBag:nil];
//	}
//	else if (btn.tag == 102000 || btn.tag == 101999)
//	{//btnBack from Info screen (102000) or the Logout button (101999), show the login screen
//		if (btn.tag == 102000)
//		{
//			[self switchToView:LOGIN viewFrom:INFO ParameterBag:nil];
//		}
//		else if (btn.tag == 101999)
//		{
//			[[ExSystem sharedInstance] clearSession]; //@"";
//			[self switchToView:LOGIN viewFrom:HOME_PAGE ParameterBag:nil];
//		}
//	}
//}
//
//-(IBAction)switchViews:(id)sender ParameterBag:(NSMutableDictionary *)paramBag
//{
//	UIControl *btn = (UIControl*)sender;
//    
//	if (btn.tag == 100002)
//	{//sign in button and the home button
//		[self switchToView:SETTINGS_VIEW viewFrom:LOGIN ParameterBag:nil];
//	}
//	else if (btn.tag == 100001 || btn.tag == 100 || btn.tag == 101)
//	{//sign in button and the home button
//		[self switchToView:HOME_PAGE viewFrom:LOGIN ParameterBag:nil];
//	}
//	else if (btn.tag == 600001)
//	{//drill to the Trip Details view from the Trips List view
//		[self switchToView:TRIP_DETAILS viewFrom:TRIPS ParameterBag:paramBag];
//	}
//	else if (btn.tag == 600002)
//	{//go from Trip Details view to the Trip List view
//		[self switchToView:TRIPS viewFrom:TRIP_DETAILS ParameterBag:nil];
//	}
//	else if (btn.tag == 600003)
//	{//drill to the itin details view from the Trip Detail view
//		[self switchToView:ITIN_DETAILS viewFrom:TRIP_DETAILS ParameterBag:nil];
//	}
//	else if (btn.tag == 600004)
//	{//go from itin details details view to the segments Trip Detail view
//		[self switchToView:TRIP_DETAILS viewFrom:ITIN_DETAILS ParameterBag:nil];
//	}
//	else if (btn.tag == 700001)
//	{//go to maps from itin detail
//		[self switchToView:MAP viewFrom:ITIN_DETAILS ParameterBag:nil];
//	}
//	else if (btn.tag == 700002)
//	{//go to webview from itin details
//		[self switchToView:WEBVIEW viewFrom:ITIN_DETAILS ParameterBag:nil];
//	}
//	else if (btn.tag == 101032)
//	{
//		[self switchToView:APPROVE_REPORTS viewFrom:HOME_PAGE  ParameterBag:nil];
//	}
//	else if (btn.tag == 101031 || btn.tag == 101002 || btn.tag == 101010)
//	{//expense, trip, weather button from homepage view
//		if (btn.tag == 101031)
//		{//go to expense list
//			[self switchToView:ENTRIES viewFrom:HOME_PAGE ParameterBag:nil];
//		}
//		else if (btn.tag == 101002)
//		{//go to trips
//			[self switchToView:TRIPS viewFrom:HOME_PAGE ParameterBag:nil];
//		}
//		else if (btn.tag == 101010)
//		{//go to southwest
//			if ([self.findMe.doneLoading isEqualToString:@"YES"])
//			{
//				//NSString *urlString = @"http://maps.google.com/maps?daddr=37.74324,-121.43432&saddr=37.5,-121.4";
//				//NSString *urlString = [NSString stringWithFormat:@"http://maps.google.com/maps?daddr=%@,%@&saddr=37.5,-121.4", self.findMe.latitude, self.findMe.longitude];
//				NSString *address2 = @"18400+NE+Union+Hill+Road,+Redmond,+WA,+980523,+USA";
//				NSString *escaped_address =  [address2 stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
//				NSString *urlString = [NSString stringWithFormat:@"http://maps.google.com/maps?daddr=%@&saddr=%@,%@", escaped_address, self.findMe.latitude, self.findMe.longitude];
//				//NSString *requestString = [NSString stringWithFormat:@"http://maps.google.com/maps/geo?q=%@&output=xml&oe=utf8&key=%@&sensor=false&gl=it", escaped_address, MAPS_API_KEY];
//                
//				[[UIApplication sharedApplication] openURL: [NSURL URLWithString: urlString]];
//			}
//		}
//	}
//	else if (btn.tag == 102000 || btn.tag == 101999)
//	{//btnBack from Info screen (102000) or the Logout button (101999), show the login screen
//		if (btn.tag == 102000)
//		{
//			[self switchToView:LOGIN viewFrom:INFO ParameterBag:nil];
//		}
//		else if (btn.tag == 101999)
//		{
//			[self switchToView:LOGIN viewFrom:HOME_PAGE ParameterBag:nil];
//		}
//	}
//	else if (btn.tag == 100003)
//	{//btnInfo from login screen, show the registration screen
//		[self switchToView:PWD_LOGIN viewFrom:LOGIN ParameterBag:nil];
//	}
//}
//
//
//+(NSArray*)getAllViewControllers
//{
//	ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*)[[UIApplication sharedApplication] delegate];
//	if (delegate != nil)
//	{
//		UINavigationController* navController = delegate.navController;
//		if (navController != nil)
//		{
//			NSArray* allViewControllers = [self getViewControllersInNavController:navController];
//			return allViewControllers;
//		}
//	}
//	
//	return nil;
//}
//
//
//+(NSArray*)getViewControllersInNavController:(UINavigationController*)navController
//{
//	__autoreleasing NSMutableArray* allControllers = [[NSMutableArray alloc] initWithObjects:nil];
//    
//	if (navController != nil)
//	{
//		NSArray* navStack = navController.viewControllers;
//		if (navStack != nil)
//		{
//			// Get all the controllers on the navigation stack
//			[allControllers addObjectsFromArray:navStack];
//			
//			// For each controler on the navigation stack...
//			for (int i = 0; i < [navStack count]; i++)
//			{
//				UIViewController* navViewController = navStack[i];
//                
//				// ... get it's modal view controller, and it's modal view controller's modal view controller, and so on and so on
//				UIViewController* modalViewController = navViewController.presentedViewController;
//				while (modalViewController != nil)
//				{
//					[allControllers addObject:modalViewController];
//					modalViewController = modalViewController.presentedViewController;
//				}
//			}
//		}
//	}
//	
//	if ([UIDevice isPad])
//	{
//		// Get the content controller of each popup controller belonging to a MobileViewController
//		int numNavAndModalViewControllers = [allControllers count];
//		for (int j = 0; j < numNavAndModalViewControllers; j++)
//		{
//			UIViewController *navOrModalViewController = allControllers[j];
//			if ([navOrModalViewController isKindOfClass:[MobileViewController class]])
//			{
//				NSArray* popovers = [(MobileViewController*)navOrModalViewController getPopovers];
//				for (UIPopoverController *popover in popovers)
//				{
//					if (popover != nil && popover.popoverVisible && popover.contentViewController != nil)
//					{
//						[allControllers addObject:popover.contentViewController];
//					}
//				}
//			}
//		}
//	}
//    
//	// Now that we've gathered up all the controllers in the nav controller, as well as the modal controls, and popover content controllers,
//	// walk through each of them determining which are also nav controllers.  Recursively add the controllers belonging to each nav controller.
//	//
//	// Note: with the addition of the ExpenseLocationsViewController, the iPhone now has this structure as well the iPad,
//	// so nav controllers will have to be recursed for iPhone as well as iPad.
//	//
//	NSMutableArray* extraControllers = [[NSMutableArray alloc] initWithObjects:nil];
//	int numControllers = [allControllers count];
//	for (int k = 0; k < numControllers; k++)
//	{
//		UIViewController *controller = allControllers[k];
//		if ([controller isKindOfClass:[UINavigationController class]])
//		{
//			[extraControllers addObjectsFromArray:[self getViewControllersInNavController:(UINavigationController*)controller]];
//		}
//	}
//	[allControllers addObjectsFromArray:extraControllers];
//	
//	return allControllers;
//}
//
//+(MobileViewController*)getMobileViewControllerByViewIdKey:(NSString*)key
//{
//	if (key == nil)
//		return nil;
//	
//	NSArray* viewControllers = [RootViewController getAllViewControllers];
//	int numViewControllers = [viewControllers count];
//	
//	for (int j = numViewControllers - 1; j >= 0; j--)
//	{
//		UIViewController* viewController = viewControllers[j];
//		
//		if ([viewController respondsToSelector:@selector(getViewIDKey)])
//		{
//			NSString* viewIdKey = [((MobileViewController*)viewController) getViewIDKey];
//			if (viewIdKey != nil && [viewIdKey isEqualToString:key])
//				return (MobileViewController*)viewController;
//		}
//	}
//	
//	return nil;
//}
//
//+(BOOL) isLoginViewShowing
//{
//    MobileViewController *loginViewController = [RootViewController getMobileViewControllerByViewIdKey:@"LOGIN"];
//	return (loginViewController != nil);
//}
//
//+(BOOL) hasMobileViewController:(MobileViewController*)viewController
//{
//	////////////////////////////////////////////////////////////////////////////////////////////////////////
//	//
//	// January 25, 2011
//	// This method is called to determine whether a message should be passed to a MobileViewController's
//	// respondToFoundData method for processing.  Since it is believed that
//	//   1) MobileViewControllers are deallocated as soon as they are removed from the view stack, and
//	//   2) MobileViewController::dealloc cancels all its message requests
//	// it should no longer be necessary to check whether a MobileViewController is on the view stack
//	// before sending it its messages.  Therefore this method will always return YES.  After there has
//	// been adequate time for testing, this method and calls to it should be removed altogether.
//	//
//	return YES;
//	//
//	////////////////////////////////////////////////////////////////////////////////////////////////////////
//    
//	NSArray* viewControllers = [RootViewController getAllViewControllers];
//	int numViewControllers = [viewControllers count];
//	
//	for (int j = numViewControllers - 1; j >= 0; j--)
//	{
//		if (viewController == viewControllers[j])
//			return YES;
//	}
//	
//	[[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::hasMobileViewController: specified view controller not found.  View controllers: %@", [RootViewController getAllViewControllers]] Level:MC_LOG_DEBU];
//	return NO;
//}
//
//-(void) refreshTopViewData:(Msg *)msg
//{//this is where the rvc asks the top view if it wants to use the currently retrieved data
//	
//	if (msg.responder != nil && msg.responder.cancellationReceived)
//	{
//		[[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::refreshTopViewData: dropping cancelled message %@.", msg.idKey] Level:MC_LOG_DEBU];
//		return;
//	}
//	
//	MobileViewController *mvc = [RootViewController getMobileViewControllerByViewIdKey:[ExSystem sharedInstance].sys.topViewName];
//	if (mvc != nil)
//	{
//		[[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::refreshTopViewData: invoking %@::respondToFoundData:%@", [mvc class], msg.idKey] Level:MC_LOG_DEBU];
//		[mvc respondToFoundData:msg];
//	}
//	else
//	{
//		[[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::refreshTopViewData: message not handled: %@.  %@, which is the top view according to topViewName, was not found.  View controllers: %@", msg.idKey, ([ExSystem sharedInstance].sys.topViewName != nil ? [ExSystem sharedInstance].sys.topViewName : @"nil"), [RootViewController getAllViewControllers]] Level:MC_LOG_DEBU];
//	}
//    
//}
//
//#pragma mark -
//#pragma mark UIAlertViewDelegate Methods
//-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
//{
//    // Display rules of behavior for gov Safe harbor required user
//    if ([Config isGov])
//    {
//        if (buttonIndex == 0 && [[postLoginAttribute objectForKey:@"NEED_SAFEHARBOR"] isEqualToString:@"true"])
//        {
//            GovLoginNoticeVC *noticeVC = [[GovLoginNoticeVC alloc] initWithNibName:@"LoginHelpTopicVC" bundle:nil];
//            noticeVC.title = [Localizer getLocalizedText:@"Rules of Behavior"];
//            UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:noticeVC];
//            localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
//            [self presentViewController:localNavigationController animated:YES completion:nil];
//        }
//    }
//}
//
//#pragma mark -
//#pragma mark MobileViewController Methods
//-(NSString *)getViewIDKey
//{
//	return HOME_PAGE;
//}
//
//-(int)processCards:(CardsGetPersonalAndTransactions *)pCards
//{
//	int totalTransactions = 0;
//	
//	for(NSString *key in pCards.cards)
//	{
//		float runningTotal = 0;
//		PersonalCardData *card = (pCards.cards)[key];
//		for(NSString *tranKey in card.trans)
//		{
//			PCardTransaction *tran = (card.trans)[tranKey];
//			runningTotal = runningTotal + tran.tranAmount;
//			totalTransactions++;
//		}
//		card.runningTotal = runningTotal;
//	}
//	return totalTransactions;
//}
//
//
//-(void) fillCardSummary:(NSMutableDictionary*) cards withKeys:(NSMutableArray*) cardKeys withData:(OutOfPocketData*)oopData
//{
//	if (oopData.pCards != nil && [oopData.pCards count] > 0)
//	{
//		NSArray* allCards = [oopData.pCards allValues];
//		for (int ix = 0; ix < [allCards count]; ix++)
//		{
//			PersonalCardData * card = (PersonalCardData*) allCards[ix];
//			NSString* pcaKey = card.pcaKey;
//			cards[pcaKey] = card;
//			[cardKeys addObject:pcaKey];
//		}
//	}
//	int cctCount = 0;
//	
//	NSArray* allValues = [oopData.oopes allValues];
//	for (int ix = 0; ix < [oopData.oopes count]; ix++)
//	{
//		OOPEntry* entry = (OOPEntry*) allValues[ix];
//		if ([entry isPersonalCardTransaction])
//		{
//			NSString* pcaKey = ((PCardTransaction*) entry).pcaKey;
//			PersonalCardData * card = cards[pcaKey];
//			if (card == nil && pcaKey != nil)
//			{
//				card = [[PersonalCardData alloc] init];
//				card.pcaKey = pcaKey;
//				card.cardName = ((PCardTransaction*) entry).cardName;
//				card.crnCode = entry.crnCode;
//				card.runningTotal = entry.tranAmount;
//				card.transCount = 1;
//				[cardKeys addObject:pcaKey];
//				cards[pcaKey] = card;
//			}
//			else if (card != nil)
//			{
//				card.runningTotal += entry.tranAmount;
//				card.transCount ++;
//			}
//		}
//		else if ([entry isCorporateCardTransaction]){
//			cctCount++;
//		}
//	}
//	if (cctCount > 0)
//	{
//		// Add corporate card message to cards section
//		cards[FILTER_CORP_CARDS] = [NSString stringWithFormat:@"%d", cctCount];
//		[cardKeys addObject:FILTER_CORP_CARDS];
//	}
//	
//}
//
//-(NSArray*)getCardRowDetails:(OutOfPocketData*)oopData
//{
//	if ((sectionKeys == nil && sectionData == nil) ||([sectionKeys count] ==0 && [sectionData count] ==0))
//	{
//		kSectionCorpReport = 0;
//		self.sectionKeys = [[NSMutableArray alloc] init];
//		self.sectionData = [[NSMutableArray alloc] init];
//		
//		NSMutableArray *rpt = [[NSMutableArray alloc] init];
//		NSMutableDictionary *rptData = [[NSMutableDictionary alloc] init];
//		[sectionKeys addObject:rpt];
//		[sectionData addObject:rptData];
//	}
//	[self preparePersonalCardRows:oopData];
//	__autoreleasing NSArray *expArray = nil;
//	if([sectionKeys count] > 0 && [sectionData count] >0)
//	{
//		expArray = @[sectionKeys[kSectionCorpReport]
//                    ,sectionData[kSectionCorpReport]];
//	}
//	return expArray;
//}
//
//-(void) preparePersonalCardRows: (OutOfPocketData*)oopData
//{
//	NSMutableArray *cardKeys = [[NSMutableArray alloc] initWithObjects: nil];
//	NSMutableDictionary* cards = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
//	NSMutableArray* expenseKeys = nil; //[sectionKeys objectAtIndex:kSectionCorpReport];
//	NSMutableDictionary* expenseData = nil; // [sectionData objectAtIndex:kSectionCorpReport];
//    
//	if ([UIDevice isPad])
//	{
//		int goodIndex = 0;
//		for(int i = 0; i < [sections count]; i++)
//		{
//			if([sections[i] isEqualToString:[Localizer getLocalizedText:@"Expense Updates"]])
//			{
//				goodIndex = i;
//				break;
//			}
//		}
//		expenseKeys = sectionKeys[goodIndex];
//		expenseData = sectionData[goodIndex];
//		kSectionCorpReport = goodIndex;
//	}
//	else
//	{
//		expenseKeys = sectionKeys[kSectionCorpReport];
//		expenseData = sectionData[kSectionCorpReport];
//	}
//    
//	
//	[self fillCardSummary:cards withKeys:cardKeys withData:oopData];
//	
//	int ix = 0;
//	for (NSString* key in cardKeys)
//	{
//		NSString* msg = expenseData[key];
//		if (msg == nil)
//		{
//			[expenseKeys insertObject:key atIndex:ix];
//			ix++;
//		}
//		expenseData[key] = cards[key];
//	}
//    
//	if ([[ExSystem sharedInstance]isBreeze])
//	{
//		if (cardKeys == nil || [cardKeys count]==0){
//            //nothing to be done
//		}
//		else
//		{
//            EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_BREEZE_CARD];
//            if(entity != nil)
//                [[HomeManager sharedInstance] deleteObj:entity];
//		}
//	}
//    else
//    {
//        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_BREEZE_CARD];
//        if(entity != nil)
//            [[HomeManager sharedInstance] deleteObj:entity];
//    }
//    
//	
//}
//
//-(void) prepareBreezeCardsSection: (OutOfPocketData*)oopData
//{
//	// Calcualte for personal card totals
//	NSMutableArray *cardKeys = [[NSMutableArray alloc] initWithObjects: nil];
//	NSMutableDictionary* cards = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
//	[sectionData removeObjectAtIndex:0];
//	[sectionData insertObject:cards atIndex:0];
//	[sectionKeys removeObjectAtIndex:0];
//	[sectionKeys insertObject:cardKeys atIndex:0 ];
//	[self fillCardSummary:cards withKeys:cardKeys withData:oopData];
//	
//	//MOB-1906
//	// TODO - check for Breeze?
//	if ([cards count]==0)
//	{
//        //		NSString *cardMessage = [Localizer getLocalizedText:@"Import your credit card"];
//        //		NSMutableArray *c = [[NSMutableArray alloc] initWithObjects:@"NOCARDS", nil];
//        //		NSMutableDictionary *cData = [[NSMutableDictionary alloc] initWithObjectsAndKeys:cardMessage, @"NOCARDS", nil];
//		
//        //		[sectionData removeObjectAtIndex:kSectionCards];
//        //		[sectionKeys removeObjectAtIndex:kSectionCards];
//        //		[sectionData insertObject:cData atIndex:kSectionCards];
//        //		[sectionKeys insertObject:c atIndex:kSectionCards];
//		
//	}
//	
//	
//}
//
//
//
//-(void) updateExpenseUpdatesRow:(NSString*) strCount withKey:(const NSString*) key withDict:(NSMutableDictionary*) rptData
//                       withKeys:(NSMutableArray*) rpt inFrontOfKey:(const NSString*) nextKey
//{
//	NSString* msg = rptData[key];
//	if (msg == nil)
//	{
//		NSUInteger ix = nextKey == nil ? NSNotFound : [rpt indexOfObject:nextKey];
//		if (ix == NSNotFound)
//			[rpt addObject:key];
//		else
//			[rpt insertObject:key atIndex:ix];
//	}
//	rptData[key] = strCount== nil?@"0":strCount;
//}
//
//
//-(void) updateExpenseUpdatesCorpCardRow:(NSString*) corpCardTransactionCount withDict:(NSMutableDictionary*) rptData
//							   withKeys:(NSMutableArray*) rpt
//{
//	if (corpCardTransactionCount == nil || [corpCardTransactionCount intValue] ==0)
//	{
//		// Remove corp card transaction count
//		[rpt removeObject:FILTER_CORP_CARDS];
//		[rptData removeObjectForKey:FILTER_CORP_CARDS];
//	}
//	else {
//		[self updateExpenseUpdatesRow:corpCardTransactionCount withKey:FILTER_CORP_CARDS
//							 withDict:rptData withKeys:rpt inFrontOfKey:kRptRowKey];
//	}
//	
//}
//
//
//-(void) didProcessMessage:(Msg *)msg
//{
//    [self respondToFoundData:msg]; // TODO: handle case where msg.didConnectionFail is YES
//}
//
//-(void)respondToFoundData:(Msg *)msg
//{   //respond to data that might be coming from the cache
//	
//	if([msg.idKey isEqualToString:CAR_RATES_DATA])
//	{
//		self.carRatesData = (CarRatesData*) msg.responder;
//	}
//	else if ([msg.idKey isEqualToString:REPORT_ENTRY_FORM_DATA])
//	{
//        isFetchingEntryForm = FALSE;
//        [self hideWaitView];
//		if (msg.errBody != nil)
//		{
//			UIAlertView *alert = [[MobileAlertView alloc]
//								  initWithTitle:[Localizer getLocalizedText:@"ADD_CAR_MILEAGE_HOME_NOT_SUPPORTED"]
//								  message:msg.errBody
//								  delegate:nil
//								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
//								  otherButtonTitles:nil];
//			
//			[alert show];
//		}
//		else
//		{
//            if (msg.responseCode == 200)
//                [self presentPersonalCarMileageForm:msg];
//		}
//        //        [self hideWaitView];  // If hide wait view after popping mileage form, the home view cannot be clicked afterwards
//	}
//	else if ([msg.idKey isEqualToString:CURRENCY_DATA])
//	{
//		CurrencyData *currencyData = (CurrencyData *)msg.responder;
//		[ExSystem sharedInstance].currencies = currencyData.dataDict;
//		[[ExSystem sharedInstance] saveSettings];
//	}
//	else if ([msg.idKey isEqualToString:OOPES_DATA])
//	{
//        // MOB-7484 Need to refresh expenses row after add yodlee card
//        //		if (![UIDevice isPad] && delegate.navController.topViewController != self)
//        //			return;
//		
//		OutOfPocketData *oopData = (OutOfPocketData *)msg.responder;
//		[self refreshUIWithOOPData:oopData];
//	}
//	else if ([msg.idKey isEqualToString:SUMMARY_DATA])
//	{
//		if ([ExSystem sharedInstance].sessionID == nil)
//		{
//			// Fix for MOB-1599:
//			//
//			// The session id is nil, but we're here because a response
//			// was just received from the server for the user who logged out.
//			// Do not remove any objects from the sectionData, sectionKeys, and
//			// sections arrays.  Those arrays were cleaned up in initSections
//			// which was called by buttonLogoutPressed when the user pressed
//			// the logout button.  If we mess with them now, things could go
//			// wrong for the next user who logs in.
//			//
//			return;
//		}
//		
//		//[self prepareSections];
//		
//        //		if (false&&![[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER] && ![[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
//        //			return;
//        // Check role in SummaryData instead
//        
//        // RootViewController.m Should not modify Home UI via Coredata(EntityHome).
//        // All the logic is in the Home9VC.m/GovHome9VC.m
//        // Comment out to avoid un-wanted UI changes on Home
////		SummaryData *sd = (SummaryData *)msg.responder;
////		[self refreshUIWithSummaryData:sd];
//	}
//	else if ([msg.idKey isEqualToString:TRIPS_DATA])
//	{
//        // RootViewController.m Should not modify Home UI.
//        // All the logic is in the Home9VC.m
//        // Comment out to avoid un-wanted UI changes on Home
////		TripsData* tripsData = (TripsData *)msg.responder;
////		[self refreshUIWithTripsData:tripsData];
//	}
//    else if ([msg.idKey isEqualToString:REGISTER_PUSH])
//    {
//        // We don't report anything to the user, but we'll monitor via Flurry (and GDS logs)
//        RegisterPush *rp = (RegisterPush*)msg.responder;
//        NSMutableDictionary *dictionary = [NSMutableDictionary dictionaryWithObjectsAndKeys:rp.actionStatus.status, @"Status", nil];
//        if ([rp.actionStatus.status isEqualToString:@"FAILURE"])
//        {
//            [dictionary setValue:rp.actionStatus.errMsg forKey:@"Message"];
//        }
//        
//    }
//    
//    else if ([msg.idKey isEqualToString:INVOICE_COUNT])
//    {
//        InvoiceCountData *cd = (InvoiceCountData*)msg.responder;
//        self.invoiceCount = cd.count;
//        // MOB-11948- put back Invoice cell on home screen
//        // Update invoice cell
////        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_INVOICE_INVOICE];
////        if(entity != nil)
////        {
////            entity.subLine  = [NSString stringWithFormat:@"%@ %@",
////                                            [NSString isStringNullEmptyOrAllSpaces:self.invoiceCount]? @"0":self.invoiceCount ,
////                                            [Localizer getLocalizedText:@"INVOICES_TO_REVIEW"]];
////            [[HomeManager sharedInstance] saveIt:entity];
////        }
//    }
//    else if ([msg.idKey isEqualToString:GOV_WARNING_MSG])
//    {
//        GovWarningMessagesData *messages = (GovWarningMessagesData*) msg.responder;
//        if (messages != nil)
//        {
//            NSManagedObjectContext *context = [ExSystem sharedInstance].context;
//            NSArray *allMessage = [BaseManager fetchAll:@"EntityWarningMessages" withContext:context];
//            if ([allMessage count] > 0)
//            {
//                self.allMessages = (EntityWarningMessages*) [allMessage objectAtIndex:0];
//            }
//        }
//    }
//    if (self.showWhatsNew)
//        [self disableSettingsAndLogoutButtons];
//}
//
//#pragma mark -
//#pragma mark UI Refresh methods
//
//- (void) prepareSections
//{
//}
//
//- (void) refreshUIWithOOPData: (OutOfPocketData *) oopData
//{
//	//int mobileEntryCount = ([oopData.oopes count] - [[SmartExpenseManager getInstance] getSmartExpenseCount]);// + [oopData.pcts count];
//    
//	[self preparePersonalCardRows:oopData];
//}
//
//
//-(void)adjustReportSections
//{
//	NSMutableArray *rpt = sectionKeys[kSectionCorpReport];
//	NSMutableDictionary *rptData = sectionData[kSectionCorpReport];
//	
//	if ([rpt[0] isEqualToString:@"QuickE"] || [rpt[0] isEqualToString:@"SPINNER"])
//	{
//		[rpt removeAllObjects];
//		[rptData removeAllObjects];
//	}
//	else
//	{
//		if ([rpt count]>1)
//		{
//			// Remove the spinner row
//			for (int ix = 0; ix < [rpt count]; ix++)
//			{
//				if ([rpt[ix] isEqualToString:@"QuickE"] || [rpt[ix] isEqualToString:@"SPINNER"])
//				{
//					[rpt removeObjectAtIndex:ix];
//					break;
//				}
//			}
//		}
//	}
//}
//
//
//-(void)refreshUIWithSummaryData:(SummaryData*)sd
//{
//	NSString *reportsToApproveCount = (sd.dict)[@"ReportsToApproveCount"];
//    if (![reportsToApproveCount length])
//        reportsToApproveCount = @"0";
//	NSString *unsubmittedReportsCount = (sd.dict)[@"UnsubmittedReportsCount"];
//    if (![unsubmittedReportsCount length])
//        unsubmittedReportsCount = @"0";
//	NSString *corpCardTransactionCount = (sd.dict)[@"CorporateCardTransactionCount"];
//    if (![corpCardTransactionCount length])
//        corpCardTransactionCount = @"0";
//    
//    int iPos = 0;
//    EntityHome *entity = nil;
//    
//    
//    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
//    {
//        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_QUICK];
//        entity.name = [Localizer getLocalizedText:@"Quick Expense"];
//        entity.subLine  = [Localizer getLocalizedText:@"Capture expense and receipt"];
//        entity.key = kSECTION_EXPENSE_QUICK;
//        entity.sectionValue = kSECTION_EXPENSE;
//        entity.sectionPosition = @kSECTION_EXPENSE_POS;
//        entity.imageName = @"icon_quickexpense";
//        entity.rowPosition = @(iPos);
//        [[HomeManager sharedInstance] saveIt:entity];
//        iPos++;
//        
//        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON];
//        entity.name = [Localizer getLocalizedText:@"Authorizations"];
//        entity.subLine  = [Localizer getLocalizedText:@"View and update authorizations"];
//        entity.imageName = @"icon_trip_approvals";
//        entity.key = kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON;
//        entity.sectionValue = kSECTION_EXPENSE;
//        entity.sectionPosition = @kSECTION_EXPENSE_POS;
//        entity.rowPosition = @(iPos);
//        [[HomeManager sharedInstance] saveIt:entity];
//        iPos++;
//        
//        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_REPORTS];
//        entity.name = [Localizer getLocalizedText:@"Vouchers"];
//        entity.subLine  = [Localizer getLocalizedText:@"View, create and update vouchers"];
//        entity.key = kSECTION_EXPENSE_REPORTS;
//        entity.sectionValue = kSECTION_EXPENSE;
//        entity.sectionPosition = @kSECTION_EXPENSE_POS;
//        entity.imageName = @"icon_report";
//        entity.rowPosition = @(iPos);
//        [[HomeManager sharedInstance] saveIt:entity];
//        iPos++;
//    }
//    else if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
//    {
//        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_QUICK];
//        entity.name = [Localizer getLocalizedText:@"Quick Expense"];
//        entity.subLine  = [Localizer getLocalizedText:@"Capture expense and receipt"];
//        entity.key = kSECTION_EXPENSE_QUICK;
//        entity.sectionValue = kSECTION_EXPENSE;
//        entity.sectionPosition = @kSECTION_EXPENSE_POS;
//        entity.imageName = @"icon_quickexpense";
//        entity.rowPosition = @(iPos);
//        [[HomeManager sharedInstance] saveIt:entity];
//        iPos++;
//
//        
//        if ([[ExSystem sharedInstance] siteSettingAllowsExpenseReports])
//        {
//            entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_REPORTS];
//            entity.name = [Localizer getLocalizedText:@"Reports"];
//            entity.itemCount = @([unsubmittedReportsCount intValue]);
//            entity.subLine  = [NSString stringWithFormat:@"%@ %@", unsubmittedReportsCount, [Localizer getLocalizedText:@"unsubmitted Reports"]];
//            entity.key = kSECTION_EXPENSE_REPORTS;
//            entity.sectionValue = kSECTION_EXPENSE;
//            entity.sectionPosition = @kSECTION_EXPENSE_POS;
//            entity.imageName = @"icon_report";
//            entity.rowPosition = @(iPos);
//            [[HomeManager sharedInstance] saveIt:entity];
//            iPos++;
//        }
//        else
//        {
//            entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_REPORTS];
//            if(entity != nil)
//                [[HomeManager sharedInstance] deleteObj:entity];
//            
//        }
//        
//        
//        
//        
//        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_CARDS];
//            
//        entity.name = [Localizer getLocalizedText:@"Expenses"];
//        entity.subLine  = [NSString stringWithFormat:@"%@ %@", corpCardTransactionCount, [Localizer getLocalizedText:@"corporate card transactions"]];
//        entity.itemCount = @([corpCardTransactionCount intValue]);
//        entity.key = kSECTION_EXPENSE_CARDS;
//        entity.sectionValue = kSECTION_EXPENSE;
//            
//        entity.sectionPosition = @kSECTION_EXPENSE_POS;
//        entity.imageName = @"icon_card";
//        entity.rowPosition = @(iPos);
//        [[HomeManager sharedInstance] saveIt:entity];
//        iPos++;
//    }
//    else
//    {
//        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_QUICK];
//        if(entity != nil)
//            [[HomeManager sharedInstance] deleteObj:entity];
//    }
//    
//    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
//    {
//        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_APPROVALS];
//        entity.name = [Localizer getLocalizedText:@"Stamp Documents"];
//        entity.subLine  = [Localizer getLocalizedText:@"Approve authorizations and vouchers"];
//        entity.key = kSECTION_EXPENSE_APPROVALS;
//        entity.sectionValue = kSECTION_EXPENSE;
//        entity.sectionPosition = @kSECTION_EXPENSE_POS;
//        entity.imageName = @"icon_approval";
//        entity.rowPosition = @(iPos);
//        [[HomeManager sharedInstance] saveIt:entity];
//        iPos++;
//        
//        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_CARDS];
//        entity.name = [Localizer getLocalizedText:@"Expenses"];
//        entity.subLine  = [Localizer getLocalizedText:@"View unapplied expenses"];
//        entity.key = kSECTION_EXPENSE_CARDS;
//        entity.sectionValue = kSECTION_EXPENSE;
//        entity.sectionPosition = @kSECTION_EXPENSE_POS;
//        entity.imageName = @"icon_card";
//        entity.rowPosition = @(iPos);
//        [[HomeManager sharedInstance] saveIt:entity];
//        iPos ++;
//    }
//    else if ([[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals]) //([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER])
//    {
//        if([reportsToApproveCount intValue] > 0)
//        {
//            EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_APPROVALS];
//            entity.name = [Localizer getLocalizedText:@"Approvals"];
//            entity.subLine  = [NSString stringWithFormat:@"%@ %@", reportsToApproveCount, [Localizer getLocalizedText:@"Reports to approve"]];
//            entity.itemCount = @([reportsToApproveCount intValue]);
//            entity.key = kSECTION_EXPENSE_APPROVALS;
//            entity.sectionValue = kSECTION_EXPENSE;
//            entity.sectionPosition = @kSECTION_EXPENSE_POS;
//            entity.imageName = @"icon_approval";
//            entity.rowPosition = @(iPos);
//            [[HomeManager sharedInstance] saveIt:entity];
//            iPos++;
//        }
//        else if([[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS] != nil)
//        {
//            entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
//            [[HomeManager sharedInstance] deleteObj:entity];
//        }
//    }
//    else if([[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS] != nil)
//    {
//        //kill off approvals if the user should not have it
//        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
//        [[HomeManager sharedInstance] deleteObj:entity];
//    }
//}
//
//- (void) removeOptionToFindTravel
//{
//    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_TRIPS_FIND_TRAVEL];
//    if(entity != nil)
//        [[HomeManager sharedInstance] deleteObj:entity];
//}
//
//- (void) refreshUIWithTripsData: (TripsData *) tripsData
//{
//	//TripData* currentTrip = nil;
//    int upcoming = 0;
//    int active = 0;
//    
//    NSArray *aTrips = [[TripManager sharedInstance] fetchAll];
//    
//    // MOB-7192 Strip out timezone info in current local time, so that we can compare it with travel time in GMT(w/o timezone info).
//    NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
//    
//    // MOB-5945 Whenever new TripsData comes, we need to reset active/upcoming trips as well as currentTrip
//	if ([aTrips count] > 0) /*&& self.currentTrip == nil*/
//	{
//		for (int i = 0; i < [aTrips count]; i++)
//		{
//			EntityTrip* trip = (EntityTrip*)aTrips[i];
//            // MOB-5945 - Make comparison consistent with the logic in TripsViewController
//			if (([trip.tripEndDateLocal compare:now] == NSOrderedDescending) && ([trip.tripStartDateLocal compare:now] == NSOrderedAscending))
//            {//is the end date of the looped to trip greater than today and is the startdate before now?
//                active ++; /*MOB-5945
//                            I moved the active counter to be inside of the current trip logic.  This will push the active trip to be > 0 if you actually have a trip that fits the criteria.*/
//                /*MOB-5856
//                 Changed the logic up so that start date is also looked at.  It also will no longer automatically drop into adding an active trip as long as the end date is in the future.*/
//                if (self.currentTrip == nil)
//                {
//                    self.currentTrip = trip;
//                }
//                else
//                {
//
//                    if ([trip.tripStartDateLocal compare:currentTrip.tripStartDateLocal] == NSOrderedAscending)
//                    {
//                        self.currentTrip = trip;
//                    }
//                }
//            }
//		}
//	}
//    else
//        self.currentTrip = nil;  // No trips
//    
//    //    if(currentTrip != nil)
//    //    {
//    //        NSDate* now = [NSDate date];
//    //        NSDate* startDate = [DateTimeFormatter getLocalDate:self.currentTrip.tripStartDateLocal];
//    //        if ([startDate compare:now] == NSOrderedAscending) //is the end date of the looped to trip greater than today?
//    //            active = 1;
//    //    }
//    
//    if ([aTrips count] > 0)
//	{
//		for (int i = 0; i < [aTrips count]; i++)
//		{
//			EntityTrip* trip = (EntityTrip*)aTrips[i];
//			//NSDate* startDate = [DateTimeFormatter getLocalDate:trip.tripStartDateLocal];
//			if ([trip.tripStartDateLocal compare:now] == NSOrderedAscending) //is the start date of the looped to trip greater than today?
//				continue; //yup yup
//			
//            upcoming++;
//		}
//	}
//	
//    //Current Trip Row
//	if (currentTrip != nil)
//	{
//		NSMutableArray *tripKeys = [[NSMutableArray alloc] initWithObjects:@"CurrentTrip", nil];
//		NSMutableDictionary *tripData = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
//										 currentTrip, @"CurrentTrip", nil];
//		sectionData[kSectionCorpTrip] = tripData;
//		sectionKeys[kSectionCorpTrip] = tripKeys;
//        
//        rowData[@"CURRENT_TRIP"] = currentTrip;
//        
//        HomeField *f = dictRowData[kSECTION_TRIPS_CURRENT_BUTTON];
//        if(f != nil)
//        {
//            f.heading = currentTrip.tripName;
//            f.subHeading = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateForTravelByDate:currentTrip.tripStartDateLocal], [DateTimeFormatter formatDateForTravelByDate:currentTrip.tripEndDateLocal]];
//        }
//        
//        
//        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRIPS_CURRENT_BUTTON];
//        entity.name = currentTrip.tripName;
//        entity.subLine  = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateForTravelByDate:currentTrip.tripStartDateLocal], [DateTimeFormatter formatDateForTravelByDate:currentTrip.tripEndDateLocal]];
//        entity.keyValue = currentTrip.tripKey;
//        entity.imageName = @"icon_current_trip";
//        entity.sectionValue = kSECTION_TRIPS;
//        entity.sectionPosition = @kSECTION_TRIPS_POS;
//        entity.rowPosition = @0;
//        entity.key = kSECTION_TRIPS_CURRENT_BUTTON;
//        [[HomeManager sharedInstance] saveIt:entity];
//	}
//    else if ([[HomeManager sharedInstance] fetchHome:kSECTION_TRIPS_CURRENT_BUTTON] != nil)
//    {
//        //kill off find travel if the user should not have it
//        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_TRIPS_CURRENT_BUTTON];
//        [[HomeManager sharedInstance] deleteObj:entity];
//    }
//    
//    //Trips Row
//    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRIPS_TRIPS_BUTTON];
//    entity.name = [Localizer getLocalizedText:@"Trips"];
//    NSString *sub = [NSString stringWithFormat:[Localizer getLocalizedText:@"int active int upcoming"], active, upcoming];
//    entity.subLine = sub;
//    entity.key = kSECTION_TRIPS_TRIPS_BUTTON;
//    entity.sectionValue = kSECTION_TRIPS;
//    entity.sectionPosition = @kSECTION_TRIPS_POS;
//    entity.imageName = @"icon_trips";
//    entity.rowPosition = @1;
//    [[HomeManager sharedInstance] saveIt:entity];
//    
//    //Book Something Row
//
//    if([[ExSystem sharedInstance] siteSettingAllowsTravelBooking])
//    {
//        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRIPS_FIND_TRAVEL];
//        entity.name = [Localizer getLocalizedText:@"Book Travel"];
//        entity.subLine = [Localizer getLocalizedText:@"Book a Hotel Car and more"];
//        entity.imageName = @"icon_find";
//        entity.sectionValue = kSECTION_TRIPS;
//        entity.sectionPosition = @kSECTION_TRIPS_POS;
//        entity.rowPosition = @5;
//        entity.key = kSECTION_TRIPS_FIND_TRAVEL;
//        [[HomeManager sharedInstance] saveIt:entity];
//    }
//    else if ([[HomeManager sharedInstance] fetchHome:kSECTION_TRIPS_FIND_TRAVEL] != nil )
//    {
//        //kill off find travel if the user should not have it
//        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_TRIPS_FIND_TRAVEL];
//        [[HomeManager sharedInstance] deleteObj:entity];
//    }
//}
//
//
//#pragma mark -
//#pragma mark FindMe Initialization
//-(void)doFindMe
//{
//    //	self.findMe = [[FindMe alloc] init];
//    //    [findMe release];
//    //	isFindingMe = NO;
//}
//
//#pragma mark -
//#pragma mark Table View Data Source Methods
//- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
//{
//    return [[self.fetchedResultsController sections] count];
//}
//
//
//- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
//{
//    
//    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
//    return [sectionInfo numberOfObjects];
//}
//
//
//- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    
//	NSString *currSection;// = [sections objectAtIndex:section];
//    
//    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
//    EntityHome *entity = (EntityHome *)managedObject;
//    currSection = entity.sectionValue;
//    
//    if([currSection isEqualToString:kSECTION_APP])
//    {
//        
//        HomePageCell *cell = (HomePageCell *)[tableView dequeueReusableCellWithIdentifier: @"HomePageAppCell"];
//        if (cell == nil)
//        {
//            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HomePageAppCell" owner:self options:nil];
//            for (id oneObject in nib)
//                if ([oneObject isKindOfClass:[HomePageCell class]])
//                    cell = (HomePageCell *)oneObject;
//        }
//        
//        //        HomeField *homeField = [dictRowData objectForKey:rowKey];
//        //        if(homeField == nil)
//        //            return cell;
//        //
//        //        cell.lblHeading.text = homeField.heading;
//        [self configureCell:cell atIndexPath:indexPath];
//        
//        return cell;
//    }
//    
//	HomePageCell *cell = (HomePageCell *)[tableView dequeueReusableCellWithIdentifier: @"HomePageCell"];
//	if (cell == nil)
//	{
//		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HomePageCell" owner:self options:nil];
//		for (id oneObject in nib)
//			if ([oneObject isKindOfClass:[HomePageCell class]])
//				cell = (HomePageCell *)oneObject;
//	}
//
//    [self configureCell:cell atIndexPath:indexPath];
//    
//	return cell;
//}
//
//#pragma mark - Cell Config
//- (void)configureCell:(HomePageCell *)cell atIndexPath:(NSIndexPath *)indexPath
//{
//    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
//    EntityHome *entity = (EntityHome *)managedObject;
//    
//    cell.lblHeading.text = entity.name;
//    cell.lblSubheading.text = entity.subLine;
//    if(entity.imageName != nil)
//        cell.iv.image = [UIImage imageNamed:entity.imageName];
//}
//
//
//#pragma mark -
//#pragma mark Table Delegate Methods
//-(UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
//{
//    if([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
//        return nil;
//
//    
//    
//    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
//    if([sectionInfo.name intValue] == kSECTION_EXPENSE_POS)
//    {
//        EntityHome *entity = (EntityHome*)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
//        if(entity != nil && [entity.itemCount intValue] <= 0)
//        {
//            [viewFooter setHidden:NO];
//            lblFooter.text = [@"There are no reports to approve" localize];
//            return viewFooter;
//        }
//        else if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER] && entity == nil)
//        {
//            [viewFooter setHidden:NO];
//            lblFooter.text = [@"There are no reports to approve" localize];
//            return viewFooter;
//        }
//    }
//    
//    return nil;
//}
//
//- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
//{
//    
//    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
//
//    if([sectionInfo.name intValue] == kSECTION_APP_POS)
//        return [Localizer getLocalizedText:@"Apps"];
//    else if([sectionInfo.name intValue] == kSECTION_TRIPS_POS)
//        return [Localizer getLocalizedText:@"Travel"];
//    else if([sectionInfo.name intValue] == kSECTION_EXPENSE_POS)
//        return [Localizer getLocalizedText:@"Expense"];
//   // MOB-11948- put back Invoice cell on home screen
//    else if([sectionInfo.name intValue] == kSECTION_INVOICE_POS)
//        return [Localizer getLocalizedText:@"Invoice Approval"];
//    else
//        return @"";
//	
//}
//
//-(UIView *) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
//{
//    return nil;
//}
//
//-(CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
//{
//    return 24;
//}
//
//
//-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
//{
//	NSUInteger section = [newIndexPath section];
//    NSUInteger row = [newIndexPath row];
//    
//    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:newIndexPath];
//    EntityHome *entity = (EntityHome *)managedObject;
//	
//	[tableList deselectRowAtIndexPath:newIndexPath animated:YES];
//    
//	NSString *currSection = sections[section];
//	NSString *rowKey =nil; //[sectionData objectAtIndex:section];
//    if([entity.sectionValue isEqualToString:kSECTION_TRIPS])
//    {
//        if([entity.key isEqualToString:kSECTION_TRIPS_TRIPS_BUTTON])
//        {
//            NSDictionary *dictionary = @{@"Action": @"View Trips"};
//            [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//            
//            [self switchToView:TRIPS viewFrom:HOME_PAGE ParameterBag:nil];
//            //        else if([entity.key isEqualToString:kSECTION_TRIPS_CURRENT_BUTTON] && [ExSystem sharedInstance].isSingleUser)
//            //        {
//            //            [self buttonTripItPressed:nil];
//            //        }
//        }
//        else if([entity.key isEqualToString:kSECTION_TRIPS_CURRENT_BUTTON])
//        {
//            NSDictionary *dictionary = @{@"Action": @"View Current Trip"};
//            [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//            
//            
//            if (self.currentTrip != nil)
//            {
//                EntityTrip* trip = self.currentTrip;
//                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:trip, @"TRIP", trip.tripKey, @"TRIP_KEY", @"YES", @"SKIP_PARSE", nil];
//                [self switchToView:TRIP_DETAILS viewFrom:HOME_PAGE ParameterBag:pBag];
//            }
//        }
//        else if([entity.key isEqualToString:kSECTION_TRIPS_FIND_TRAVEL])
//        {
//            NSDictionary *dictionary = @{@"Action": @"Book Trip"};
//            [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//            
//            
//            [self bookingsActionPressed:nil];
//        }
//        else if([entity.key isEqualToString:kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON])
//        {
//            NSDictionary *dictionary = @{@"Action": @"Travel Request"};
//            [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//            
//            [self showTravelRequests];
//        }
//    }
//    else if([entity.sectionValue isEqualToString:kSECTION_EXPENSE])
//    {
//        if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
//        {
//            if([entity.key isEqualToString:kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON])
//            {
//                // GSA Authorizations
//                NSDictionary *dictionary = @{@"Action": @"GSA Authorizations"};
//                [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//                
//                [self showGovAuthorizations];
//            }
//            else if ([entity.key isEqualToString:kSECTION_EXPENSE_REPORTS])
//            {
//                // GSA Vouchers
//                NSDictionary *dictionary = @{@"Action": @"GSA Vouchers"};
//                [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//                
//                [self showGovVouchers];
//            }
//            else if ([entity.key isEqualToString:kSECTION_EXPENSE_APPROVALS])
//            {
//                // GSA Vouchers
//                NSDictionary *dictionary = @{@"Action": @"GSA Stamp Documents"};
//                [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//                
//                [self showGovStampDocuments];
//            }
//            else if([entity.key isEqualToString:kSECTION_EXPENSE_QUICK]) {
//                NSDictionary *dictionary = @{@"Action": @"GSA Quick Expense"};
//                [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//                
//                [self buttonGovQuickPressed:nil];
//            }
//            else if([entity.key isEqualToString:kSECTION_EXPENSE_CARDS]) {
//                NSDictionary *dictionary = @{@"Action": @"GSA Expenses"};
//                [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//                
//                [self showGovExpenses];
//            }
//            
//        }
//        else if([entity.key isEqualToString:kSECTION_EXPENSE_REPORTS])
//        {
//            NSDictionary *dictionary = @{@"Action": @"View Reports"};
//            [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//            
//            [self buttonUnsubmmitedReportsPressed:self];
//        }
//        else if([entity.key isEqualToString:kSECTION_EXPENSE_CARDS]) {
//            NSDictionary *dictionary = @{@"Action": @"View Card Charges"};
//            [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//            
//            [self switchToExpenses:self];
//        }
//        else if([entity.key isEqualToString:kSECTION_EXPENSE_APPROVALS]) {
//            
//            NSDictionary *dictionary = @{@"Action": @"View Report Approval"};
//            [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//            
//            [self buttonApproveReportsPressed:self];
//        }
//        else if([entity.key isEqualToString:kSECTION_EXPENSE_QUICK]) {
//            NSDictionary *dictionary = @{@"Action": @"Quick Expense"};
//            [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//            
//            [self buttonQuickPressed:nil];
//        }
//        else if([entity.key isEqualToString:kSECTION_EXPENSE_BREEZE_CARD])
//        {
//            NSDictionary *dictionary = @{@"Action": @"View Breeze Cards"};
//            [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//            
//            [self buttonCardsPressed:nil];
//        }
//    }
//    // MOB-11948- put back Invoice cell on home screen
//      else if([entity.sectionValue isEqualToString:kSECTION_INVOICE])
//        {
//            if([entity.key isEqualToString:kSECTION_INVOICE_INVOICE])
//            {
//                NSDictionary *dictionary = @{@"Action": @"View Invoices"};
//                [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//    
//                [self buttonInvoicePressed:nil];
//            }
//        }
//    else if([currSection isEqualToString:kSECTION_BOOKINGS])
//    {
//        //Need to go to the new booking screen
//    }
//    else if([currSection isEqualToString:kSECTION_APP])
//    {
//        rowKey = aAppRows[row];
//        if([rowKey isEqualToString:kSECTION_BOOKINGS_GATE_GURU])
//            [self buttonAirportsPressed:self];
//        else if([rowKey isEqualToString:kSECTION_BOOKINGS_METRO])
//            [self buttonMetroPressed:self];
//        else if([rowKey isEqualToString:kSECTION_BOOKINGS_TAXI_MAGIC])
//            [self buttonTaxiPressed:self];
//    }
//}
//
//
//- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    return 54;
//}
//
//- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
//{
//    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
//    
//    if([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
//        return 0.0;
//    
//    else if([sectionInfo.name intValue] == kSECTION_EXPENSE_POS)
//    {
//        
//        if (![[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals])
//        {
//            [viewFooter setHidden:YES]; // In case it was triggered to show before data refresh
//            return 0.0;
//        }
//        
//        
//        EntityHome *entity = (EntityHome*)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
//        if(entity != nil && [entity.itemCount intValue] <= 0)
//        {
//            return 30;
//        }
//        else if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER] && entity == nil)
//        {
//            return 30;
//        }
//        else
//        {
//            [viewFooter setHidden:YES]; // In case it was triggered to show before data refresh
//            return 0.0;
//        }
//    }
//    else
//        return 0.0;
//}
//
//#pragma mark -
//#pragma mark messages
//-(void) showFilteredCorpCards:(id)sender
//{
//	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW",
//								 FILTER_CORP_CARDS, @"FILTER", nil];
//	
//	[self switchToView:OUT_OF_POCKET_LIST	viewFrom:HOME_PAGE  ParameterBag:pBag];
//}
//
//- (NSString*) getPersonalCardCountMessage:(PersonalCardData*) pcData
//{
//	NSString* cardName = pcData.cardName;
//	
//	NSString* msgId = [NSString stringWithFormat:@"PersonalCardCount_%d", pcData.transCount>1? 2 : pcData.transCount];
//    
//	NSString* rptMsg = pcData.transCount>1?
//	[NSString stringWithFormat:[Localizer getLocalizedText:msgId], pcData.transCount, cardName]:
//	[NSString stringWithFormat:[Localizer getLocalizedText:msgId], cardName];
//	
//	return rptMsg;
//}
//
//-(NSString*) getCardTransactionCountMessage:(int) count
//{
// 	NSString* rptMsg = [Localizer getLocalizedText:@"There are no cct"];
//    
//    if (count>1)
//        rptMsg = [NSString stringWithFormat:[Localizer getLocalizedText:@"There are cct"], @(count)];
//    else if (count == 1)
//        rptMsg = [NSString stringWithFormat:[Localizer getLocalizedText:@"There is cct"], @(count)];
//	
//	return rptMsg;
//}
//
//-(NSString*) getExpensesCountMessage:(int) count
//{
// 	NSString* rptMsg = [Localizer getLocalizedText:@"Total no expenses"];
//    
//    if (count>1)
//        rptMsg = [NSString stringWithFormat:[Localizer getLocalizedText:@"Total num expenses"], @(count)];
//    else if (count == 1)
//        rptMsg = [NSString stringWithFormat:[Localizer getLocalizedText:@"Total num expense"], @(count)];
//	
//	return rptMsg;
//}
//
//// This for SU only
//-(NSString*) getCardChargesCountMessage:(int) count
//{
// 	NSString* rptMsg = [Localizer getLocalizedText:@"Total no card charges"];
//    
//    if (count>1)
//        rptMsg = [NSString stringWithFormat:[Localizer getLocalizedText:@"Total num card charges"], @(count)];
//    else if (count == 1)
//        rptMsg = [NSString stringWithFormat:[Localizer getLocalizedText:@"Total num card charge"], @(count)];
//	
//	return rptMsg;
//}
//
//- (NSString*) getRptRowCountMessage:(NSString*)strCount msgKey:(NSString*)key
//{
//	if (strCount == nil)
//	{
//		strCount = @"0";
//	}
//	int count = [strCount intValue];
//	
//	NSString* msgId = [NSString stringWithFormat:@"%@_%@", key, (count>1)?@"2" : strCount];
//	NSString* rptMsg = [NSString stringWithFormat:[Localizer getLocalizedText:msgId], [strCount intValue]];
//	return rptMsg;
//}
//
//#pragma mark -
//#pragma mark HomePage Methods
//
//
//
//- (void)drawOfflineView
//{
//	[self showOfflineView:self];
//}
//
//- (void)fetchHomePageData
//{
//    [self fetchHomePageDataAndSkipCache:NO];
//}
//
//- (void)fetchHomePageDataAndSkipCache:(BOOL)shouldSkipCache
//{
//    
//    
//	if ([self isViewLoaded])
//		[tableList setHidden:NO];
//    
//	[self setToolbarHome];
//    
//    if([[self.fetchedResultsController sections] count] <= 0)
//        [self showWaitView];
//    

//	
//	if([[ExSystem sharedInstance] isValidSessionID:[ExSystem sharedInstance].sessionID] || [@"OFFLINE" isEqualToString:[ExSystem sharedInstance].sessionID])
//	{
//		if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
//			[self fetchCarRatesAndSkipCache:shouldSkipCache];
//        
//		NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
//		[[ExSystem sharedInstance].msgControl createMsg:SUMMARY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:shouldSkipCache RespondTo:self];
//		
//		if (([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] || [[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER]))
//		{
//			NSMutableDictionary* pBag2 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
//			[[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag2 SkipCache:YES RespondTo:self];
//		}
//        else
//        {
//            // MOB-7823.  Need to remove option to find travel that was added to core data before the user's travel permission was revoked by the administrator.
//            [self removeOptionToFindTravel];
//        }
//        
//		if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
//		{
//            // Load the expense types
//            ExpenseTypesManager* etMgr = [ExpenseTypesManager sharedInstance];
//            [etMgr loadExpenseTypes:nil msgControl:[ExSystem sharedInstance].msgControl];
//            
//            // Load the expenses
//			NSMutableDictionary* pBag3 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
//			[[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag3 SkipCache:shouldSkipCache RespondTo:self];
//            
//            // Load currencies
//            FormFieldData *currencyField = [[FormFieldData alloc] init];
//            currencyField.iD = @"TransactionCurrencyName";
//            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: currencyField, @"FIELD", @"Y", @"MRU", nil];
//            [[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
//		}
//        
//        if ([[ExSystem sharedInstance] hasRole:MOBILE_INVOICE_PAYMENT_USER] || [[ExSystem sharedInstance] hasRole:ROLE_INVOICE_APPROVER])
//        {
//            [self fetchInvoiceCount];
//        }
//        
//        // Clean up SingleUser rows
//        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:@"INVITE"];
//        
//        if (entity != nil)
//            [[HomeManager sharedInstance] deleteObj:entity];
//        
//	}
//}
//
//-(void) fetchInvoiceCount
//{
//    [[ExSystem sharedInstance].msgControl createMsg:INVOICE_COUNT CacheOnly:@"NO" ParameterBag:nil SkipCache:YES RespondTo:self];
//    
//}
//
//-(void) forceRefetch
//{
//    //self.fetchedResultsController = nil;
//    NSError *error;
//	if (![[self fetchedResultsController] performFetch:&error]) {
//		// Update to handle the error appropriately.
//		
//        if ([Config isDevBuild]) {
//            exit(-1);  // Fail
//        } else {
//            // be more graceful when dying abort();
//            [[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::forceRefetch: fetchedResultsController %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
//        }
//	}
//}
//
//-(void)initSections
//{
//    
//}
//
//-(void)setToolbarHome
//{
//    
//    UIBarButtonItem *btnAction = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(buttonActionPressed:)];
//        
//    UIBarButtonItem *btnSettings = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"icon_settings"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonSettingsPressed:)];
//    self.navigationItem.rightBarButtonItem = nil; //remove the right button
//    self.navigationItem.rightBarButtonItem = btnAction;
//        
//    self.navigationItem.leftBarButtonItem = nil; //remove the right button
//    self.navigationItem.leftBarButtonItem = btnSettings;
//}
//
//
//
//#pragma mark -
//#pragma mark Button Press Methods
//-(void)buttonInvoicePressed:(id)sender
//{
//    // MOB-11528 [iOS] New URL and landing page for HTML5
//    NSURL *url = [[ExSystem sharedInstance] urlForWebExtension:@"invoice-home-page"];
//    [[UIApplication sharedApplication] openURL:url];
//    
//    //[self switchToView:APPROVE_INVOICES viewFrom:HOME_PAGE ParameterBag:nil];
//}
//
//
//-(IBAction) buttonQuickPressed:(id)sender
//{
//    QEFormVC *fromVC = [[QEFormVC alloc] initWithEntryOrNil:nil];
//    [self.navigationController pushViewController:fromVC animated:YES];
//}
//
//-(IBAction) buttonGovQuickPressed:(id)sender
//{
//    if ([Config isGov])
//    {
//        GovExpenseEditViewController* vc = [[GovExpenseEditViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
//        
//        [self.navigationController pushViewController:vc animated:YES];
//    }
//}
//
//
//-(IBAction) buttonAddReportPressed:(id)sender
//{
//    ReportSummaryViewController *vc = [[ReportSummaryViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
//	vc.role = ROLE_EXPENSE_TRAVELER;
//	
//	[self.navigationController pushViewController:vc animated:YES];
//    
//    // Retrieve report form data
//	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
//								 vc.role, @"ROLE_CODE",
//								 nil];
//	[[ExSystem sharedInstance].msgControl createMsg:REPORT_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:vc];
//    
//	
//}
//
//-(void)buttonCardsPressed:(id)sender
//{
//    NSDictionary *dictionary = @{@"Action": @"Add Card from Home"};
//    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//    
//    [self launchConcurBreezeURL:sender];
//}
//
//-(IBAction)launchConcurBreezeURL:(id)sender
//{
//	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"http://%@", @"www.concurbreeze.com"]]];
//}
//
//- (void)buttonDiningPressed:(id)sender
//{
//    [AppsUtil launchOpenTableApp];
//}
//
//-(void)buttonAirportsPressed:(id)sender
//{
//	[self performSelector:@selector(switchToAirports:) withObject:nil afterDelay:0.05f];
//}
//
//
//- (void)switchToAirports:(id)sender
//{
//    [AppsUtil launchGateGuruAppWithUrl:nil];
//}
//
//
//- (void)buttonAmtrakPressed:(id)sender
//{
//    
//	[self performSelector:@selector(switchToAmtrak:) withObject:nil afterDelay:0.05f];
//}
//
//
//- (void)switchToAmtrak:(id)sender
//{
//	[self switchToView:TRAIN_BOOK viewFrom:HOME_PAGE ParameterBag:nil];
//}
//
//
//- (void)buttonCarPressed:(id)sender
//{
//    
//	[self performSelector:@selector(switchToCar:) withObject:nil afterDelay:0.05f];
//}
//
//
//- (void)buttonHotelPressed:(id)sender
//{
//    
//    //	[RootViewController turnOffNetworkFlag];
//	[self performSelector:@selector(switchToHotel:) withObject:nil afterDelay:0.05f];
//}
//
//
//- (void)switchToCar:(id)sender
//{
//    [CarViewController showCarVC:self.navigationController withTAFields:nil];
//
////	[self switchToView:CAR viewFrom:HOME_PAGE ParameterBag:nil];
//}
//
//
//- (void)switchToHotel:(id)sender
//{
//    [HotelViewController showHotelVC:self.navigationController withTAFields:nil];
//
////	[self switchToView:HOTEL viewFrom:HOME_PAGE ParameterBag:nil];
//}
//
//
//- (void)buttonTaxiPressed:(id)sender
//{
//    [AppsUtil launchTaxiMagicApp];
//}
//
//- (void)buttonMetroPressed:(id)sender
//{
//    [AppsUtil launchMetroApp];
//}
//
//- (IBAction)buttonTripItPressed:(id)sender
//{
//    [AppsUtil launchTripItApp];
//}
////MOB-11145
//- (IBAction)buttonTravelTextPressed:(id)sender
//{
//    [AppsUtil launchTravelTextApp];
//}
//
//- (IBAction)buttonSettingsPressed:(id)sender
//{
//    SettingsViewController *vc = [[SettingsViewController alloc] init];
//    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:vc];
//    [self presentViewController:localNavigationController animated:YES completion:nil];
//}
//
//
//- (IBAction)buttonSwitchViewPressed:(id)sender
//{
//	
//	[self switchViews:sender ParameterBag:nil];
//}
//
//
//- (void)buttonTripsPressed:(id)sender
//{
//    
//	[self switchToView:TRIPS viewFrom:HOME_PAGE ParameterBag:nil]; // switchViews:sender ParameterBag:nil];
//}
//
//-(void)onLogout
//{
//	[self disableSettingsAndLogoutButtons];
//	hasDrawn = NO;
//    [self clearHomeData];
//	[self setToolbarItems:nil];
//}
//
//- (IBAction)buttonLogoutPressed:(id)sender
//{
//	[[ApplicationLock sharedInstance] onLogoutButtonPressed];
//}
//
//-(void)disableSettingsAndLogoutButtons
//{
//	if (self.navigationItem == nil)
//		return;
//	
//    //	MOB-7497 Logout button is no longer on the right side of the nav bar
//    //
//    //	UIBarButtonItem *btnLogout = self.navigationItem.rightBarButtonItem;
//    //	if (btnLogout != nil)
//    //	{
//    //		btnLogout.action = nil;
//    //	}
//	
//	UIBarButtonItem *btnSettings = self.navigationItem.leftBarButtonItem;
//	if (btnSettings != nil)
//	{
//		btnSettings.action = nil;
//	}
//    
//	UIBarButtonItem *btnAction = self.navigationItem.rightBarButtonItem;
//	if (btnAction != nil)
//	{
//		btnAction.action = nil;
//	}
//}
//
//
//-(void)enableSettingsAndLogoutButtons
//{
//	if (self.navigationItem == nil)
//		return;
//    
//    //	MOB-7497 Logout button is no longer on the right side of the nav bar
//    //
//    //	UIBarButtonItem *btnLogout = self.navigationItem.rightBarButtonItem;
//    //	if (btnLogout != nil)
//    //	{
//    //		btnLogout.action = @selector(buttonLogoutPressed:);
//    //	}
//	
//	UIBarButtonItem *btnSettings = self.navigationItem.leftBarButtonItem;
//	if (btnSettings != nil)
//	{
//		btnSettings.action = @selector(buttonSettingsPressed:);
//	}
//    
//    UIBarButtonItem *btnAction = self.navigationItem.rightBarButtonItem;
//	if (btnAction != nil)
//	{
//		btnAction.action = @selector(buttonActionPressed:);
//	}
//    
//}
//
//
//-(void)buttonApproveReportsPressed:(id)sender
//{
//    
//	[self performSelector:@selector(switchToApprovals:) withObject:nil afterDelay:0.05f];
//}
//
//
//-(void)switchToApprovals:(id)sender
//{
//	[self switchToView:APPROVE_REPORTS viewFrom:HOME_PAGE  ParameterBag:nil];
//}
//
//
//-(void)buttonApproveInvoicesPressed:(id)sender
//{
//    
//	[self performSelector:@selector(switchToInvoiceApprovals:) withObject:nil afterDelay:0.05f];
//}
//
//
//-(void)switchToInvoiceApprovals:(id)sender
//{
//	[self switchToView:APPROVE_INVOICES viewFrom:HOME_PAGE  ParameterBag:nil];
//}
//
//
//-(void)buttonOutOfPocketPressed:(id)sender
//{
//    
//	[self performSelector:@selector(switchToExpenses:) withObject:nil afterDelay:0.05f];
//}
//
//
//-(void)switchToExpenses:(id)sender
//{
//	[self switchToView:OUT_OF_POCKET_LIST viewFrom:HOME_PAGE ParameterBag:nil];
//}
//
//
//-(void)buttonUnsubmmitedReportsPressed:(id)sender
//{
//    
//	enablefilterUnsubmittedActiveReports = NO;
//	[self performSelector:@selector(switchToReportsList:) withObject:nil afterDelay:0.05f];
//}
//
//
//-(void)buttonReportsPressed:(id)sender
//{
//    
//	enablefilterUnsubmittedActiveReports = NO;
//    
//    // MOB-11402
//    // [self performSelector:@selector(switchToReportsList:) withObject:nil afterDelay:0.05f];
//    
//    [self switchToReportsList:sender];
//}
//
//
//-(void)switchToReportsList:(id)sender
//{
//	[self switchToView:ACTIVE_REPORTS viewFrom:HOME_PAGE ParameterBag:nil];
//}
//
//#pragma mark -
//#pragma mark Quick Expense Link
//
//-(void) createUI:(MobileViewController *)nextController
//{
//	ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//	[delegate.navController pushViewController:nextController animated:YES];
//}
//
//
//#pragma mark -
//#pragma mark iAd methods
//- (void)createAdBannerView {
//    Class classAdBannerView = NSClassFromString(@"ADBannerView");
//    if (classAdBannerView != nil) {
//        self.adBannerView = [[classAdBannerView alloc]
//                             initWithFrame:CGRectZero];
//        // MOB-10667 XCode 4.5 changes
//        //        [_adBannerView setRequiredContentSizeIdentifiers:[NSSet setWithObjects:
//        //														  ADBannerContentSizeIdentifier320x50,
//        //														  ADBannerContentSizeIdentifier480x32, nil]];
//        //        if (UIInterfaceOrientationIsLandscape([UIDevice currentDevice].orientation)) {
//        //            [_adBannerView setCurrentContentSizeIdentifier:
//        //			 ADBannerContentSizeIdentifier480x32];
//        //        } else {
//        //            [_adBannerView setCurrentContentSizeIdentifier:
//        //			 ADBannerContentSizeIdentifier320x50];
//        //        }
//        //
//        [_adBannerView setFrame:CGRectOffset([_adBannerView frame], 0, self.view.frame.size.height)];
//        [_adBannerView setDelegate:self];
//		
//		[_adBannerView setAutoresizingMask:UIViewAutoresizingFlexibleTopMargin];
//        
//        [self.view addSubview:_adBannerView];
//		[self.view bringSubviewToFront:_adBannerView];
//    }
//}
//
//
//- (void)fixupAdView:(UIInterfaceOrientation)toInterfaceOrientation
//{
//    if (_adBannerView != nil) {
//        // MOB-10667 XCode 4.5 changes
//        //        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
//        //            [_adBannerView setCurrentContentSizeIdentifier:
//        //			 ADBannerContentSizeIdentifier480x32];
//        //        } else {
//        //            [_adBannerView setCurrentContentSizeIdentifier:
//        //			 ADBannerContentSizeIdentifier320x50];
//        //        }
//        [UIView beginAnimations:@"fixupViews" context:nil];
//        if (_adBannerViewIsVisible) {
//            CGRect adBannerViewFrame = [_adBannerView frame];
//            CGRect contentViewFrame = _contentView.frame;
//			int bH = [self getBannerHeight:toInterfaceOrientation];
//            adBannerViewFrame.origin.x = 0;
//            adBannerViewFrame.origin.y = self.view.frame.size.height - bH;
//            [_adBannerView setFrame:adBannerViewFrame];
//            contentViewFrame.origin.y = 0;
//            contentViewFrame.size.height = self.view.frame.size.height - bH;
//            _contentView.frame = contentViewFrame;
//        } else {
//            CGRect adBannerViewFrame = [_adBannerView frame];
//            CGRect contentViewFrame = _contentView.frame;
//            
//            adBannerViewFrame.origin.x = 0;
//            adBannerViewFrame.origin.y = self.view.frame.size.height;
//            [_adBannerView setFrame:adBannerViewFrame];
//            contentViewFrame.origin.y = 0;
//            contentViewFrame.size.height = self.view.frame.size.height;
//            _contentView.frame = contentViewFrame;
//        }
//        [UIView commitAnimations];
//    }
//}
//
//
//- (int)getBannerHeight:(UIDeviceOrientation)orientation
//{
//    if (UIInterfaceOrientationIsLandscape(orientation)) {
//        return 32;
//    } else {
//        return 50;
//    }
//}
//
//
//- (int)getBannerHeight
//{
//    return [self getBannerHeight:[UIDevice currentDevice].orientation];
//}
//
//
//#pragma mark ADBannerViewDelegate
//- (void)bannerViewDidLoadAd:(ADBannerView *)banner {
//    if (!_adBannerViewIsVisible) {
//        _adBannerViewIsVisible = YES;
//        [self fixupAdView:[UIDevice currentDevice].orientation];
//    }
//}
//
//
//- (void)bannerView:(ADBannerView *)banner didFailToReceiveAdWithError:(NSError *)error
//{
//    if (_adBannerViewIsVisible)
//    {
//        _adBannerViewIsVisible = NO;
//        [self fixupAdView:[UIDevice currentDevice].orientation];
//    }
//}
//
//
//#pragma mark -
//#pragma mark Action Methods
//-(void)buttonActionPressed:(id)sender
//{
//	bool enableLocateAndAlert = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"LocateAndAlert" withType:@"OTMODULE"]];
//    if (enableLocateAndAlert)
//    {
//        // MOB-7502 check LNA_User role
//        enableLocateAndAlert = [[ExSystem sharedInstance] hasRole:ROLE_LNA_USER];
//    }
//	bool hideReceiptStore = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"HIDE_RECEIPT_STORE" withType:@"CTE_EXPENSE_ADMIN"]];
//    
//    MobileActionSheet *actionSheet = [[MobileActionSheet alloc] initWithTitle:nil
//                                                                     delegate:self
//                                                            cancelButtonTitle:nil
//                                                       destructiveButtonTitle:nil
//                                                            otherButtonTitles:nil];
//    
//    NSMutableArray* btnIds = [[NSMutableArray alloc] init];
//    
//    
//    [actionSheet addButtonWithTitle:[Localizer getLocalizedText:@"Refresh Data"]];
//    [btnIds addObject:HOME_BTN_REFRESH_DATA];
//    
//    
//    if([[ExSystem sharedInstance] hasRole:ROLE_INVOICE_APPROVER]  || [[ExSystem sharedInstance] hasRole:MOBILE_INVOICE_PAYMENT_USER])
//    {
//        NSString *label = [Localizer getLocalizedText:@"Invoices"];
//        if (self.invoiceCount != nil)
//            label = [label stringByAppendingFormat:@" (%@)", self.invoiceCount];
//        [actionSheet addButtonWithTitle:label];
//        [btnIds addObject:HOME_BTN_INVOICES];
//    }
//    
//	if([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
//	{
//        
//        
//        if (!hideReceiptStore) // MOB-7625
//        {
//            [actionSheet addButtonWithTitle:[Localizer getLocalizedText:@"Receipts"]];
//            [btnIds addObject:HOME_BTN_RECEIPTS];
//        }
//        bool enableCarMileage = ![@"N" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"PersonalCarMileageOnHome" withType:@"Mobile"]];
//        enableCarMileage &= [carRatesData hasAnyPersonalsWithRates:[ExSystem sharedInstance].sys.crnCode];
//        enableCarMileage &= [[ExSystem sharedInstance] siteSettingAllowsExpenseReports];
//        
//        if (enableCarMileage)
//        {
//            [actionSheet addButtonWithTitle:[Localizer getLocalizedText:@"Car Mileage"]];
//            [btnIds addObject:HOME_BTN_CAR_MILEAGE];
//        }
//        
//        if (![[ExSystem sharedInstance] isBreeze] && enableLocateAndAlert)
//        {
//            [actionSheet addButtonWithTitle:[Localizer getLocalizedText:@"Safety Check In"]];
//            [btnIds addObject:HOME_BTN_LOCATION];
//        }
//	}
//    else if (enableLocateAndAlert)
//    {
//        [actionSheet addButtonWithTitle:[Localizer getLocalizedText:@"Safety Check In"]];
//        [btnIds addObject:HOME_BTN_LOCATION];
//    }
//    
//    // If there aren't any actions, then return instead of creating an action sheet.
//    if ([btnIds count] == 0)
//        return;
//    
//    // Add the cancel button to the action sheet.
//    actionSheet.cancelButtonIndex = [actionSheet addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
//    
//    actionSheet.btnIds = btnIds;
//    
//	actionSheet.tag = APPS_ACTION_SHEET_TAG;
//	
//    [actionSheet showFromRect:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height) inView:self.view animated:YES];
//}
//
//-(void) showTravelRequests
//{
//    TravelRequestViewController *trVC = [[TravelRequestViewController alloc] initWithNibName:@"TRWebViewController" bundle:nil];
//    UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:trVC];
//	navController.modalPresentationStyle = UIModalPresentationFormSheet;
//    
//    [self presentViewController:navController animated:YES completion:nil];
//    
//}
//
//-(void)bookingsActionPressed:(id)sender
//{
//    if(![ExSystem connectedToNetwork])
//	{
//		UIAlertView *alert = [[MobileAlertView alloc]
//							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
//							  message:[Localizer getLocalizedText:@"Bookings offline"]
//							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
//		[alert show];
//		return;
//	}
//    
//	MobileActionSheet *action = [[MobileActionSheet alloc] initWithTitle:nil
//                                                                delegate:self
//                                                       cancelButtonTitle:nil
//                                                  destructiveButtonTitle:nil
//                                                       otherButtonTitles: nil];
//    NSMutableArray* btnIds = [[NSMutableArray alloc] init];
//    
//    [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Air"]];
//    [btnIds addObject:BOOKINGS_BTN_AIR];
//    [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Hotel"]];
//    [btnIds addObject:BOOKINGS_BTN_HOTEL];
//    [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Car"]];
//    [btnIds addObject:BOOKINGS_BTN_CAR];
//    
//    if([[ExSystem sharedInstance] canBookRail])
//    {
//        [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Rail"]];
//        [btnIds addObject:BOOKINGS_BTN_RAIL];
//    }
//    
//    [action addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
//    action.cancelButtonIndex = [btnIds count];
//    
//    action.btnIds = btnIds;
//    
//	action.tag = BOOKINGS_ACTION_SHEET_TAG;
//	
//    //	if([UIDevice isPad])
//    //		[action showFromBarButtonItem:sender animated:YES];
//    //	else
//	{
//		//action.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
//		[action showFromRect:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height) inView:self.view animated:YES];
//	}
//}
//
//#pragma GSA Actions
//
//-(void) showGovDocumentListView:(NSString*) filter
//{
//    GovDocumentListVC *vc = [[GovDocumentListVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
//    vc.filter = filter;
//    // MOB-12319 get same document list content on iPhone and iPad.
//    NSString* msgId = [filter isEqualToString:GOV_DOC_TYPE_STAMP]? GOV_DOCUMENTS_TO_STAMP: GOV_DOCUMENTS;
//    
//    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
//    [[ExSystem sharedInstance].msgControl createMsg:msgId CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:vc];
//    
//    [self.navigationController pushViewController:vc animated:YES];
//}
//
//-(void) showGovAuthorizations
//{
//    [self showGovDocumentListView:GOV_DOC_TYPE_AUTH];
//}
//
//-(void) showGovVouchers
//{
//    [self showGovDocumentListView:GOV_DOC_TYPE_VOUCHER];
//}
//
//-(void) showGovStampDocuments
//{
//    [self showGovDocumentListView:GOV_DOC_TYPE_STAMP];
//}
//
//-(void) showGovExpenses
//{
//    [GovUnappliedExpensesVC showUnappliedExpenses:self];
//}
//
//#pragma mark -
//#pragma mark Scroller stuff
//- (void)scrollViewDidEndDecelerating:(UIScrollView *)sv
//{
//}
//
//
//#pragma mark -
//#pragma mark Car Mileage
//-(IBAction) showPersonalCarMileage:(id)sender
//{
//    [self openPersonalCarMileage:self];
//}
//
//-(void) openPersonalCarMileage:(UIViewController *)parentView
//{
//    NSDictionary *dictionary = @{@"Action": @"Personal Car Mileage"};
//    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//    
//    
//    if(carRatesData == nil || ![carRatesData hasAnyPersonalsWithRates:[ExSystem sharedInstance].sys.crnCode])
//    {
//        // MOB-5171 - Pop up dialog to warn user on Car Mileage
//        if (carRatesData == nil)
//        {
//            UIAlertView *alert = [[MobileAlertView alloc]
//                                  initWithTitle:nil
//                                  message:[Localizer getLocalizedText:@"WAIT_FOR_CAR_RATES_DATA"]
//                                  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] otherButtonTitles:nil];
//            [alert show];
//            
//        }
//        else
//        {
//            UIAlertView *alert = [[MobileAlertView alloc]
//                                  initWithTitle:nil
//                                  message:[Localizer getLocalizedText:@"ADD_CAR_MILEAGE_HOME_NOT_SUPPORTED"]
//                                  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] otherButtonTitles:nil];
//            [alert show];
//        }
//        return;
//    }
//    
//	//let's go and get car rates...
//	[self fetchCarRatesAndSkipCache:NO];
//    
//    self.addExpenseClicked = NO;
//	
//	[self goToSelectReport:parentView];
//    
//}
//
//
//-(void) fetchCarRatesAndSkipCache:(BOOL)shouldSkipCache
//{
//	//let's go and get car rates...
//	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
//	[[ExSystem sharedInstance].msgControl createMsg:CAR_RATES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:shouldSkipCache RespondTo:self];
//}
//
//
//-(void) fetchPersonalCarMileageFormFields:(id)sender rptKey:(NSString*) rptKey rpt:(ReportData*) rpt
//{
//    // pop up wait view
//    [self showWaitView];
//    self.isFetchingEntryForm = TRUE;
//	//set up the formdata
//	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
//								 @"MILEG", @"EXP_KEY",
//								 rpt.rptKey, @"RPT_KEY", //, nil ]; //MILEG = Default Personal Car Mileage  CARMI = Company Car Mileage
//								 //parentRpeKey, @"PARENT_RPE_KEY",
//								 rpt, @"rpt",
//								 nil];
//    if (addExpenseClicked)
//        pBag[@"EXP_KEY"] = @"UNDEF";
//    
//	[[ExSystem sharedInstance].msgControl createMsg:REPORT_ENTRY_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
//	
//	
//}
//
//
//-(void) presentPersonalCarMileageForm:(Msg *)msg
//{
//	// MOB-5133 Reuse showEntryView to make sure DistanceToDate is fetched for mileage.
//	ReportEntryFormData* resp = (ReportEntryFormData*) msg.responder;
//	ReportData *rpt = (msg.parameterBag)[@"rpt"];
//	
//	resp.rpt.entry.parentRpeKey = @"";
//	// Temp fix, b/c server passes back garbage
//	resp.rpt.entry.rpeKey = nil;
//	resp.rpt.entry.rptKey =rpt.rptKey;
//	resp.rpt.entry.transactionCrnCode = rpt.crnCode;
//    
//    if (addExpenseClicked)
//    {
//        resp.rpt.entry.expKey = nil;
//        resp.rpt.entry.expName = [Localizer getLocalizedText:@"Undefined"];
//        FormFieldData* expFld = (resp.rpt.entry.fields)[@"ExpKey"];
//        if (expFld != nil)
//        {
//            expFld.liKey = nil;
//            expFld.fieldValue = nil;
//        }
//    }
//    
//	if(self.carRatesData == nil)
//	{
//		NSMutableDictionary *pBag2 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
//		[[ExSystem sharedInstance].msgControl createMsg:CAR_RATES_DATA CacheOnly:@"NO" ParameterBag:pBag2 SkipCache:NO RespondTo:self];
//	}
//	
//	
//    NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"SHORT_CIRCUIT", resp.rpt.entry, @"ENTRY", rpt, @"REPORT", nil];
//    pBag[@"ROLE"] = ROLE_EXPENSE_TRAVELER;
//    if (addExpenseClicked)
//        pBag[@"TITLE"] = [Localizer getLocalizedText:@"Add Expense"];
//    else
//        pBag[@"TITLE"] = [Localizer getLocalizedText:@"Add Car Mileage"];
//    pBag[@"FROM_HOME"] = @"YES";
//    
//    [ReportDetailViewController showEntryView:self withParameterBag:pBag carMileageFlag:!addExpenseClicked];
//    
//}
//
//- (void) goToSelectReport
//{
//    [self goToSelectReport:self];
//}
//
//- (void) goToSelectReport:(UIViewController *)parentView
//{
//	//takes you to the select report view
//	SelectReportViewController * pVC = [[SelectReportViewController alloc] initWithNibName:@"SelectReportViewController" bundle:nil];
//	pVC.meKeys = nil;
//	pVC.pctKeys = nil;
//	pVC.cctKeys = nil;
//	pVC.meAtnMap = nil;
//	pVC.isCarMileage = YES;
//	pVC.parentMVC = self;
//	
//    [parentView.navigationController pushViewController:pVC animated:YES];
//}
//
//
//-(NSMutableArray *) makeWhatsNewText
//{
//	__autoreleasing NSMutableArray *a = [[NSMutableArray alloc] initWithObjects:nil];
//        // 8.12
//    [a addObject:@"Support for password based login"];
//    [a addObject:@"Automatically defaulting the expense currency based upon the country location entered"];
//    [a addObject:@"View alternate flight schedules from an existing flight"];
//    [a addObject:@"Allow Invoice submission"];
//    
//    // 8.11
//    //        [a addObject:@"Capture a receipt or create a Quick Expense in \"airplane\" mode"];
//    //        [a addObject:@"Upload queued receipts and Quick Expenses created in \"airplane\" mode when network connection is restored"];
//    //        [a addObject:@"Show Most Recently Used (MRU) for expense types/currencies"];
//    //        [a addObject:@"Support for iPad Mini"];
//    
//    // 8.10
//    //        [a addObject:@"Improved user experience when booking Air with Hotel/Car"];
//    //        [a addObject:@"Displaying Travel Agency contact information"];
//    //        [a addObject:@"Support for location check-in on iPad"];
//    
//    // 8.9
//    //        [a addObject:@"iOS 6 and iPhone 5 are now supported."];
//    //        [a addObject:@"Support for entering Company Car mileage"];
//    //        [a addObject:@"Support for restricting the Class of Service on Air bookings"];
//    
//    //        [a addObject:@"Added message about removing attendees"];
//    //        [a addObject:@"Support for custom trip fields at the start and end of booking process"];
//    //        [a addObject:@"Added prompt to book hotel/car after air booking"];
//    //        [a addObject:@"Bug fixes"];
//    
//    //        [a addObject:@"Support for Traditional and Simplified Chinese"];
//    //        [a addObject:@"Allow user to look up attendees from an external source if the client has been configured for the connector"];
//    //        [a addObject:@"Disable air booking if the client is using Travel Requests"];
//    //        [a addObject:@"Improved error message when a user does not have receipt imaging"];
//    //        [a addObject:@"Allow Approver to send the Expense Report to Accounting Review instead of next approver"];
//    //        [a addObject:@"Display a message if the web site is down for maintenance"];
//    //        [a addObject:@"Alert end user if receipt did not upload successfully"];
//    //        [a addObject:@"Added icon to show a receipt is required for report entry and entry detail"];
//    //        [a addObject:@"Displaying a custom \"No Shows\" attendee label"];
//    
//    //        [a addObject:@"Travel booking for clients who use required static custom trip fields"];
//    //        [a addObject:@"Ghost/corporate card support for travel booking"];
//    //        [a addObject:@"Support for using Attendee groups"];
//    //        [a addObject:@"Ability to suppress the Hotel itemization wizard"];
//    //        [a addObject:@"Attendee Search and Editing Enhancements"];
//    //        [a addObject:@"Improved Car Violations Display"];
//    //        [a addObject:@"Support for Mobile SSO (Single Sign-On)"];
//    //        [a addObject:@"Bug and Crash Fixes"];
//
//	return a;
//}
//
//#pragma mark - Switch to Receipts
//-(IBAction)showReceiptViewer:(id)sender
//{
//    NSDictionary *dictionary = @{@"Action": @"Receipt Store"};
//    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//        
//    [self switchToView:RECEIPT_STORE_VIEWER viewFrom:HOME_PAGE ParameterBag:nil];
//}
//
//#pragma mark -
//#pragma mark UIActionSheetDelegate method
//-(IBAction) showSafetyCheckIn:(id)sender
//{
//    [self openSafetyCheckIn:self];
//}
//
//- (void)openSafetyCheckIn:(UIViewController *)parentVC
//{
//    if (![ExSystem connectedToNetwork])
//    {
// 		UIAlertView *alert = [[MobileAlertView alloc]
//							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
//							  message:[Localizer getLocalizedText:@"Location Check Offline"]
//							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
//		[alert show];
//		return;
//        
//    }
//    
//    NSDictionary *dictionary = @{@"Action": @"Safety Checkin"};
//    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//    
//    SafetyCheckInVC* vc = [[SafetyCheckInVC alloc] initWithNibName:@"EditFormView" bundle:nil];
//    [vc setSeedData:nil];
//    [parentVC.navigationController pushViewController:vc animated:YES];
//}
//
//-(BOOL) checkBookAir
//{
//    NSString* msg = nil;
//    if (!([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_AIR_BOOKING_ENABLED]))
//    {
//        msg = [Localizer getLocalizedText:@"AIR_BOOKING_DISABLED_MSG"];
//    }
//    //    else if ([[[ExSystem sharedInstance] getUserSetting:@"HasRequiredCustomFields" withDefault:@"false"] isEqualToString:@"true"])
//    //    {
//    //        msg = [Localizer getLocalizedText:@"AIR_BOOKING_REQ_CUSTOM_FIELDS_MSG"];
//    //    }
//    else
//    {
//        NSString* profileStatus = [[ExSystem sharedInstance] getUserSetting:@"ProfileStatus" withDefault:@"0"];
//        // MOB-10390 Allow users with profileStatus 1 (missing middlename, gender) to go ahead and search air.
//        if (![profileStatus isEqualToString:@"0"] && ![profileStatus isEqualToString:@"1"])
//        {
//            if ([profileStatus isEqualToString:@"20"])
//                profileStatus = @"2";
//            NSString* msgKey = [NSString stringWithFormat:@"AIR_BOOKING_PROFILE_%@_MSG", profileStatus];
//            msg = [NSString stringWithFormat:@"%@\n\n%@", [Localizer getLocalizedText:msgKey], [@"AIR_BOOKING_PROFILE_PROLOG_MSG" localize]];
//        }
//        else
//            return TRUE;
//    }
//    
//    MobileAlertView *alert = [[MobileAlertView alloc]
//                              initWithTitle:nil
//                              message:msg
//                              delegate:nil
//                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
//                              otherButtonTitles:nil];
//    [alert show];
//    
//    return FALSE;
//}
//
//- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
//{
//	if (actionSheet.tag == APPS_ACTION_SHEET_TAG)
//	{
//        if (buttonIndex != actionSheet.cancelButtonIndex)
//        {
//            MobileActionSheet* mas = (MobileActionSheet*) actionSheet;
//            NSString* btnId = [mas getButtonId:buttonIndex];
//            
//            if ([HOME_BTN_REFRESH_DATA isEqualToString:btnId])
//            {
//                if (![ExSystem connectedToNetwork])
//                {
//                    UIAlertView *alert = [[MobileAlertView alloc]
//                                          initWithTitle:[Localizer getLocalizedText:@"Offline"]
//                                          message:[Localizer getLocalizedText:@"Refresh Data Offline"]
//                                          delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
//                    [alert show];
//                    return;
//                    
//                }
//
//                // MOB-11971
//                NSDictionary *dictionary = @{@"Action": @"Refresh Data"};
//                [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//
//                [self fetchHomePageDataAndSkipCache:YES];
//            }
//            // MOB-11528 [iOS] New URL and landing page for HTML5
//            else if ([HOME_BTN_INVOICES isEqualToString:btnId])
//            {
//                
//                NSURL *url = [[ExSystem sharedInstance] urlForWebExtension:@"invoice-home-page"];
//                [[UIApplication sharedApplication] openURL:url];
//                
//            }
//            else if ([HOME_BTN_RECEIPTS isEqualToString:btnId])
//            {
//                NSDictionary *dictionary = @{@"Action": @"Receipt Store"};
//                [Flurry logEvent:@"Home: Action" withParameters:dictionary];
//                
//                [self switchToView:RECEIPT_STORE_VIEWER viewFrom:HOME_PAGE ParameterBag:nil];
//            }
//            else if ([HOME_BTN_CAR_MILEAGE isEqualToString:btnId])
//            {
//                [self showPersonalCarMileage:self];
//            }
//            else if ([HOME_BTN_LOCATION isEqualToString:btnId])
//            {
//                [self showSafetyCheckIn:self];
//            }
//        }
//	}
//    else if (actionSheet.tag == BOOKINGS_ACTION_SHEET_TAG)
//	{
//        if (buttonIndex != actionSheet.cancelButtonIndex)
//        {
//            MobileActionSheet* mas = (MobileActionSheet*) actionSheet;
//            NSString* btnId = [mas getButtonId:buttonIndex];
//            
//            if ([BOOKINGS_BTN_HOTEL isEqualToString:btnId])
//            {
//                if ([Config isGov])
//                    [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_HOTEL withFields:nil withDelegate:nil asRoot:NO];
//                else
//                    [HotelViewController showHotelVC:self.navigationController withTAFields:nil];
//            }
//            else if ([BOOKINGS_BTN_CAR isEqualToString:btnId])
//            {
//                if ([Config isGov])
//                    [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_CAR withFields:nil withDelegate:nil asRoot:NO];
//                else
//                    [CarViewController showCarVC:self.navigationController withTAFields:nil];
//            }
//            else if ([BOOKINGS_BTN_RAIL isEqualToString:btnId])
//            {
//                if ([Config isGov])
//                    [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_RAIL withFields:nil withDelegate:nil asRoot:NO];
//                else
//                    [TrainBookVC showTrainVC:self.navigationController withTAFields:nil];
//            }
//            else if ([BOOKINGS_BTN_AIR isEqualToString:btnId])
//            {
//                if ([self checkBookAir])
//                {
//                    if ([Config isGov])
//                        [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_AIR withFields:nil withDelegate:nil asRoot:NO];
//                    else
//                        [AirBookingCriteriaVC showAirVC:self.navigationController withTAFields:nil];
//                }
//            }
//        }
//    }
//    
//}
//
//-(MobileAlertView*) getPrivacyActView:(UIViewController* )del
//{
//    NSManagedObjectContext *context = [ExSystem sharedInstance].context;
//    NSArray *allMessage = [BaseManager fetchAll:@"EntityWarningMessages" withContext:context];
//    if ([allMessage count] > 0)
//    {
//        self.allMessages = (EntityWarningMessages*) allMessage[0];
//    }
//    
//    __autoreleasing MobileAlertView *alert = [[MobileAlertView alloc]
//                                              initWithTitle:allMessages.privacyTitle
//                                              message:allMessages.privacyText
//                                              delegate:del
//                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
//                                              otherButtonTitles:nil];
//    return alert;
//}
//
//
//#pragma mark -
//#pragma mark Whats New
//-(void) makeWhatsNew
//{
//	
//	for(UIView *v in scrollerNew.subviews)
//	{
//		if([v isKindOfClass:[UILabel class]])
//			[v removeFromSuperview];
//	}
//	
//	float w = whatsNewView.scrollNew.frame.size.width;
//	float y = 0;
//	float margin = 25.0;
//	
//	NSMutableArray *a = [self makeWhatsNewText];
//	int ix = 0;
//    
//    UILabel *lblHead = [[UILabel alloc] initWithFrame:CGRectMake(margin-5, y, w - margin, 20)];
//    [lblHead setText:[NSString stringWithFormat:[Localizer getLocalizedText:@"What's New in Version"], [ExSystem sharedInstance].sys.currentVersion]];
//    
//    [lblHead setFont:[UIFont fontWithName:@"HelveticaNeue-Bold" size:18]];
//    [lblHead setTextColor:[UIColor whiteColor]];
//    [lblHead setBackgroundColor:[UIColor clearColor]];
//    [whatsNewView.scrollNew addSubview:lblHead];
//    y = y + 20 + 25;
//    
//	for(NSString *txt in a)
//	{
//		CGSize txtSize = [txt sizeWithFont:[UIFont systemFontOfSize:13.0f] constrainedToSize:CGSizeMake(w - margin, CGFLOAT_MAX) lineBreakMode:NSLineBreakByWordWrapping];
//		float h = txtSize.height;
//        
//        float x = margin-5;
//        
//		UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(x, y, w - margin, h)];
//		[lbl setText:txt];
//		
//		[lbl setFont:[UIFont systemFontOfSize:13]];
//		[lbl setTextColor:[UIColor whiteColor]];
//		[lbl setBackgroundColor:[UIColor clearColor]];
//		[lbl setNumberOfLines:4];
//		[lbl setLineBreakMode:NSLineBreakByWordWrapping];
//        
//		[whatsNewView.scrollNew addSubview:lbl];
//		
//		lbl = [[UILabel alloc] initWithFrame:CGRectMake(5, y, 20, 18)];
//        if (ix >0)
//        {
//            [lbl setText:@"\u2022"];
//            
//            [lbl setFont:[UIFont systemFontOfSize:13]];
//            [lbl setTextColor:[UIColor whiteColor]];
//            [lbl setBackgroundColor:[UIColor clearColor]];
//            [lbl setNumberOfLines:1];
//            [whatsNewView.scrollNew addSubview:lbl];
//        }
//		ix++;
//		y = y + h + 20;
//	}
//    
//    whatsNewView.lblFooter.text = [Localizer getLocalizedText: @"Book Travel on the go with Concur"];
//    whatsNewView.lblHeading.text = [Localizer getLocalizedText: @"What's New"];
//    whatsNewView.lblSwipe.text = [Localizer getLocalizedText: @"Swipe for more"];
//	
//    lblNewTitle.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"What's New in Version"], [ExSystem sharedInstance].sys.currentVersion];
//	whatsNewView.scrollNew.contentSize = CGSizeMake(w, y);
//	[whatsNewView.scrollNew showsVerticalScrollIndicator];
//	[whatsNewView.scrollNew flashScrollIndicators];
//}
//
//// Commented out receipt migration code during ARC conversion, no need any more
////#pragma mark Receipt Migration for version 7.3
//
//
//#pragma mark - ApplicationLock Notifications
//-(void) doPostLoginInitialization
//{
//	//TODO: Is a delay really needed?
//	[self performSelector:@selector(fetchHomePageData) withObject:nil afterDelay:0.005f];
//    
//    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
//    {
//        if (self.whatsNewView != nil && showWhatsNew)
//            [self.whatsNewView closeMe:self];
//        
//        MobileAlertView *alert = [self getPrivacyActView:self];
//        [alert show];
//    }
//}
//
//-(void) savePrarmetersAfterLogin:(NSDictionary *) pBag;
//{
//    if (pBag != nil)
//    {
//        [self.postLoginAttribute addEntriesFromDictionary:pBag];
//    }
//}
//
//#pragma mark -
//#pragma mark Unwind Methods
//-(void) unwindToRootView
//{
//    NSMutableArray *viewControllersToUnwind = [[NSMutableArray alloc] init];
//    [self addViewControllersToUnwindToArray:viewControllersToUnwind];
//	
//	for (int i = [viewControllersToUnwind count] - 1; i >= 0; i--)
//	{
//		UIViewController *vc = viewControllersToUnwind[i];
//		[[MCLogging getInstance] log:[NSString stringWithFormat:@"Examining %@", [vc class]] Level:MC_LOG_DEBU];
//        
//        if ([vc isKindOfClass:[MobileViewController class]])
//        {
//            MobileViewController *mvc = (MobileViewController*)vc;
//            if ([mvc isWaitViewShowing])
//                [mvc hideWaitView];
//        }
//		
//		// If this view controller is a nav, then pop to its root
//		if([vc isKindOfClass:[UINavigationController class]])
//		{
//			// Pop to the root of the nav controller
//			UINavigationController *nav = (UINavigationController*)vc;
//			[[MCLogging getInstance] log:[NSString stringWithFormat:@"    Popping to the root of the nav which is showing %@", [[nav.viewControllers lastObject] class]] Level:MC_LOG_DEBU];
//			[nav popToRootViewControllerAnimated:NO];
//		}
//        
//		// If this view controller is a modal, then dismiss it
//		if ([vc isKindOfClass:[UIViewController class]])
//        {
//            // Prior to iOS 5, the presenter of the modal view controller could be gotten with parentViewController,
//            // beginning with iOS 5, the presenter of the modal view controller must be gotten with presentingViewController
//            UIViewController *pvc = nil;
//            if ([vc respondsToSelector:@selector(presentingViewController)])
//                pvc = vc.presentingViewController;
//            else
//                pvc = vc.parentViewController;
//            
//            if (pvc.presentedViewController == vc)
//            {
//                [[MCLogging getInstance] log:[NSString stringWithFormat:@"    Dismissing modal %@", [vc class]] Level:MC_LOG_DEBU];
//                [vc dismissViewControllerAnimated:NO completion:nil];
//            }
//        }
//        
//		// If this view controller is a popup, then dismiss it
//		if ([vc isKindOfClass:[UIPopoverController class]])
//		{
//			[[MCLogging getInstance] log:[NSString stringWithFormat:@"    Dismissing popover %@", [vc class]] Level:MC_LOG_DEBU];
//			UIPopoverController *popover = (UIPopoverController*)vc;
//			[popover dismissPopoverAnimated:NO];
//		}
//	}
//	
//}
//
//-(void) addViewControllersToUnwindToArray:(NSMutableArray*)viewControllersToUnwind
//{
//	ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//	NSObject *nestedVC = delegate.navController;
//	[[MCLogging getInstance] log:[NSString stringWithFormat:@"Starting with delegate.navController"] Level:MC_LOG_DEBU];
//	
//	while (nestedVC != nil)
//	{
//		// Add this view controller to the unwind stack
//		if (![viewControllersToUnwind containsObject:nestedVC])
//		{
//			[viewControllersToUnwind addObject:nestedVC];
//			
//			if ([nestedVC isKindOfClass:[UINavigationController class]])
//			{
//				[[MCLogging getInstance] log:[NSString stringWithFormat:@"    Queued view controller: the nav controller which is showing %@", [[((UINavigationController*)nestedVC).viewControllers lastObject] class]] Level:MC_LOG_DEBU];
//			}
//			else
//			{
//				[[MCLogging getInstance] log:[NSString stringWithFormat:@"    Queued view controller: %@", [nestedVC class]] Level:MC_LOG_DEBU];
//			}
//            
//		}
//		
//		// If it *is* a nav controller, then process the top-most view controller
//		if ([nestedVC isKindOfClass:[UINavigationController class]])
//		{
//			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Next up: top-most view controller on nav stack"] Level:MC_LOG_DEBU];
//			nestedVC = [((UINavigationController*)nestedVC).viewControllers lastObject];
//		}
//		// Else if it is the parent of a modal, then process the modal view controller
//		else if ([nestedVC isKindOfClass:[UIViewController class]] &&
//				 ((UIViewController*)nestedVC).presentedViewController != nil)
//		{
//			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Next up: modal"] Level:MC_LOG_DEBU];
//			nestedVC = ((UIViewController*)nestedVC).presentedViewController;
//		}
//		// Else if it is the parent of a popover, then process the popover
//		else if ([nestedVC isKindOfClass:[MobileViewController class]] &&
//				 ((MobileViewController*)nestedVC).pickerPopOver != nil)
//		{
//			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Next up: popover"] Level:MC_LOG_DEBU];
//			nestedVC = ((MobileViewController*)nestedVC).pickerPopOver;
//		}
//		// Else if it's a popover, then process popover's content
//		else if ([nestedVC isKindOfClass:[UIPopoverController class]]
//				 && ((UIPopoverController*)nestedVC).contentViewController != nil)
//		{
//			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Next up: popover's content view controller"] Level:MC_LOG_DEBU];
//			nestedVC = ((UIPopoverController*)nestedVC).contentViewController;
//		}
//		// Else we're done digging
//		else
//		{
//			break;
//		}
//	}
//}
//
//
//#pragma mark -
//#pragma mark Test
//-(void) buttonTestPressed:(id)sender
//{
//    ExUnitTestsVC *vc = [[ExUnitTestsVC alloc]  initWithNibName:@"ExUnitTestsVC" bundle:nil];
//    [self presentViewController:vc animated:YES completion:nil];
//    
//}
//
//
//#pragma mark - Fetched results controller
//- (NSFetchedResultsController *)fetchedResultsController
//{
//    if (__fetchedResultsController != nil) {
//        return __fetchedResultsController;
//    }
//    
//    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
//    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityHome" inManagedObjectContext:self.managedObjectContext];
//    [fetchRequest setEntity:entity];
//    
//    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"sectionPosition" ascending:YES];
//    NSSortDescriptor *sort2 = [[NSSortDescriptor alloc] initWithKey:@"rowPosition" ascending:YES];
//    [fetchRequest setSortDescriptors:@[sort, sort2]];
//    
//    //[fetchRequest setFetchBatchSize:20];
//    
//    NSFetchedResultsController *theFetchedResultsController =
//    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
//                                        managedObjectContext:self.managedObjectContext sectionNameKeyPath:@"sectionPosition"
//                                                   cacheName:@"Root"];
//    self.fetchedResultsController = theFetchedResultsController;
//    __fetchedResultsController.delegate = self;
//    
//    
//    return __fetchedResultsController;
//    
//}
//
//
//#pragma mark - Fetched results controller delegate
//- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
//{
//    [self hideWaitView];
//    [self.tableList beginUpdates];
//}
//
//- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo
//           atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
//{
//    switch(type)
//    {
//        case NSFetchedResultsChangeInsert:
//            [self.tableList insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
//            break;
//            
//        case NSFetchedResultsChangeDelete:
//            [self.tableList deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
//            break;
//    }
//}
//
//- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject
//       atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type
//      newIndexPath:(NSIndexPath *)newIndexPath
//{
//    UITableView *tableView = self.tableList;
//    
//    switch(type)
//    {
//            
//        case NSFetchedResultsChangeInsert:
//            [tableView insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
//            break;
//            
//        case NSFetchedResultsChangeDelete:
//            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
//            break;
//            
//        case NSFetchedResultsChangeUpdate:
//            [self configureCell:(HomePageCell*)[self.tableList cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
//            break;
//            
//        case NSFetchedResultsChangeMove:
//            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
//            [tableView insertRowsAtIndexPaths:@[newIndexPath]withRowAnimation:UITableViewRowAnimationFade];
//            break;
//    }
//}
//
//- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
//{
//    [self.tableList endUpdates];
//}
//
///*
// // Implementing the above methods to update the table view in response to individual changes may have performance implications if a large number of changes are made simultaneously. If this proves to be an issue, you can instead just implement controllerDidChangeContent: which notifies the delegate that all section and object changes have been processed.
// 
// - (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
// {
// // In the simplest, most efficient, case, reload the table view.
// [self.tableView reloadData];
// }
// */
//
//-(void) clearHomeData
//{
//    
//    //[currentTrip release];
//    //self.currentTrip = nil;
//    
//    if(currentTrip != nil)
//    {
//        self.currentTrip = nil;
//        //        [currentTrip release];
//        //        self.currentTrip = nil;
//    }
//    [[HomeManager sharedInstance] clearAll];
//    //self.fetchedResultsController = nil;
//}
//
//
//#pragma mark - reset and then fetch the managed results
//-(void) refetchData
//{
//    self.fetchedResultsController = nil;
//    NSError *error;
//	if (![[self fetchedResultsController] performFetch:&error]) {
//		// Update to handle the error appropriately.
//        if ([Config isDevBuild]) {
//            exit(-1);  // Fail
//        } else {
//            // be more graceful when dying abort();
//            [[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::viewDidLoad: fetchedResultsController %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
//        }
//	}
//}
//
//-(void) forceReload
//{
//    requireHomeScreenRefresh = true;
//}
//
//-(void) refreshExpenseSummary
//{
//    NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
//    [[ExSystem sharedInstance].msgControl createMsg:SUMMARY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
//}
//
//#pragma mark - Notification method
//-(void) didCloseWhatsNew:(NSNotification *)notification
//{
//    self.showWhatsNew = [[ExSystem sharedInstance].sys.showWhatsNew boolValue];
//    [self enableSettingsAndLogoutButtons];
//}
//#pragma mark - Upload Queue Banner View adjustment
//-(void) adjustViewForUploadBanner
//{
//    if (!bannerAdjusted)
//    {
//        self.bannerAdjusted = YES;
//        self.uploadView.delegate = self;
//        self.contentView.frame =CGRectMake(0, uploadView.frame.size.height, self.contentView.frame.size.width, self.contentView.frame.size.height - uploadView.frame.size.height);
//        [self.view addSubview:uploadView];
//        [self.view bringSubviewToFront:uploadView];
//    }
//}
//
//-(void) adjustViewForNoUploadBanner
//{
//    if (self.uploadView != nil && bannerAdjusted == YES)
//    {
//        [self.uploadView removeFromSuperview];
//        self.uploadView = nil;
//        if(![[ExSystem sharedInstance] hasRole:ROLE_TRIPITAD_USER])
//            [self.contentView setFrame:CGRectMake(0, 0, self.contentView.frame.size.width, self.contentView.frame.size.height + uploadView.frame.size.height)];
//        else    // add 37 to tablelist height to offset change in displayTripItAd function
//            [self.contentView setFrame:CGRectMake(0, 0, self.contentView.frame.size.width, self.contentView.frame.size.height + uploadView.frame.size.height+37)];
//        
//        self.bannerAdjusted = NO;
//    }
//}
//
//-(void) showUploadViewController
//{
//    UploadQueueViewController *vc = [[UploadQueueViewController alloc] initWithNibName:@"UploadQueueViewController" bundle:nil];
//    UIBarButtonItem *btnUpload = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Upload"] style:UIBarButtonItemStyleBordered target:vc action:@selector(startUpload)];
//    vc.title = [Localizer getLocalizedText:@"Upload Queue"];
//    vc.navigationItem.rightBarButtonItem = btnUpload;
//    [self.navigationController pushViewController:vc animated:YES];
//}
@end
