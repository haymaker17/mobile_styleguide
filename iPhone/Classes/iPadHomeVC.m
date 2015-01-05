//
//  iPadHomeVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 9/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "iPadHomeVC.h"
#import "RootCellPad.h"

#import "ConcurMobileAppDelegate.h"
#import "TripsViewController.h"
#import "SettingsViewController.h"
#import "SelectReportViewController.h"
#import "HomePageCell.h"
#import "FormatUtils.h"
#import "PersonalCardData.h"
#import "OutOfPocketData.h"
#import "SummaryData.h"
#import	"iPadHomeCell.h"
#import "SystemConfig.h"
#import "UserConfig.h"
#import "HotelImagesData.h"
#import "ImageViewerVC.h"
#import "iPadImageViewerVC.h"
#import "FindMe.h"
#import "MapViewController.h"

#import "TripItAuthVC.h"
#import "TripsViewController.h"

#import "SegmentStuff.h"
#import "SegmentRow.h"

#import "TripAirSegmentCell.h"
#import "NavShellVC.h"

#import "ExceptionLogSender.h"
#import "ActiveReportListViewController.h"
#import "ReportDetailViewController_iPad.h"

#import "TrainBookVC.h"
#import "ButtonSegment.h"
#import "CarViewController.h"
#import "HotelViewController.h"
#import "ExSystem.h"


#import "DateTimeConverter.h"
#import "ReportEntryFormData.h"
#import "ExpenseTypesManager.h"
#import "ReceiptStoreListView.h"
#import "MobileActionSheet.h"
#import "MobileAlertView.h"

#import	"UploadReceiptData.h"
#import "ExReceiptManager.h"

#import "ReportDetailViewController.h"

#import "AppsUtil.h"
#import "ApplicationLock.h"
#import "SearchYodleeCardsVC.h"
#import "TripItLinkVC.h"

#import "KeyboardNavigationController.h"
#import "YodleeCardAgreementVC.h"

#import "TravelRequestViewController.h"

#import "ReceiptCacheManager.h"
#import "ReceiptEditorVC.h"

#import "PostMsgInfo.h"
#import "UploadableReceipt.h"
#import "UploadQueueItemManager.h"
#import "UploadQueue.h"
#import "UploadQueueViewController.h"
#import "GovExpenseEditViewController.h"
#import "GovUnappliedExpensesVC.h"
#import "GovSelectTANumVC.h"
#import "GovLoginNoticeVC.h"
#import "GovDocDetailVC_iPad.h"
#import "GovDocumentListVC.h"
#import "GovDocumentManager.h"
#import "QuickExpensesReceiptStoreVC.h"

@interface iPadHomeVC (Private)
-(void)suAdjustForPortrait;
-(void)suAdjustForLandscape;
- (IBAction)buttonTravelRequestPressed:(id)sender;

-(IBAction) showGovAuthorizations:(id)sender;
-(IBAction) showGovVouchers:(id)sender;
-(IBAction) showGovStampDocuments:(id)sender;
-(IBAction) showGovUnappliedExpenses:(id)sender;

-(void) checkRoles;
-(void) configureViews;
-(void) updatedDisplayOfQueuedItemCount;
@end

static int adsIndex = 0;
#define kActionViewAddReceipt 111
#define RECEIPT_STORE_ALERT_ACTION 112
#define PRIVACY_ACT_ALERT_ACTION 113

#define GSA_HOME_BUTTON_WIDTH 88.0

@implementation iPadHomeVC
//Single user
@synthesize btnReportsTable;
@synthesize btnTripItTable;
@synthesize bkQuickActionPanel;
@synthesize bkMenuPanel;
@synthesize bkContentPanel;
@synthesize loadingContentView;
@synthesize tripsVC;
@synthesize reportsTableView;
@synthesize tripItTripsTableView, tripItWebView;
@synthesize dashboardTableView;
@synthesize btnCreateReport;
@synthesize btnAddMileage;
@synthesize menuPanel;
@synthesize contentTableContainer, viewPadIntro;
//Concur
@synthesize     addExpenseClicked;
@synthesize		lblNewTitle, lblOffline;
@synthesize		scrollerNew;
@synthesize		btnNewOK;
@synthesize		ivNewBackground;
@synthesize		viewNew;
@synthesize		showWhatsNew;
@synthesize     carRatesData;
@synthesize		aList, aKeys;
@synthesize		dictList, oopeListVC,oopeFilterListVC, scrollerButtons, tripsListVC,receiptStoreVC, homeViews, rootVC, rootPopoverButtonItem;
@synthesize		ivBarLeft, ivBarRight, ivBarMiddle, scrollerExpense, lblTripName, lblTripData, lblExpense, lblTripWait, lblTripDate, lblExpenseWait;
@synthesize		viewTrip, viewExpense, viewTripDetails, viewTripWait, viewExpenseWait;
@synthesize		lblCurrentTrip, scrollerTripDetails;
@synthesize		ivTripInfo, segmentActive, ivBackground, ivTwitBackground;
@synthesize		currentTrip, tripBits, keys, dictSegRows, dictSkins;
@synthesize		tripsData, reportsListVC, approvalsListVC;
@synthesize		isExpenseOnly, isTravelOnly, isItinViewerOnly;
@synthesize		skinExpense, imageExpenseWait;
@synthesize		btnLogout, btnSettings, btnUpload, btnAddExpense,enableActiveTrips;
@synthesize		canvas,headerImgView;
@synthesize		headerPanel,mainPanel,contentPanel,h2,h1_2,h1_3,h1_4;
@synthesize		currentTripSummaryView;
@synthesize		addFunctionsView;
@synthesize		expSummaryView;
@synthesize		adsView;
@synthesize		ivTripIcon,ivTripWait;
@synthesize		lblTripSummary;
@synthesize		tripCategoryIconsView;
@synthesize		addFuncPrev,addFuncNext,expSummaryPrev,expSummaryNext,showAdBtn;
@synthesize		addFuncScroller,expSummaryScroller;
@synthesize		lblTripDated;
@synthesize		separatorView;
@synthesize		adsArray,dataOOP,touchedTripView;
@synthesize		ivActionButtonsLeftArrow,ivActionButtonsRightArrow,ivExpSummaryButtonsLeftArrow,ivExpSummaryButtonsRightArrow;
@synthesize		summaryData, requireHomeScreenRefresh;
@synthesize		ivTravelUserPromoBanner,btnUploadReceipt, hideReceiptStore;

@synthesize lblBumpHelpTitle, lblBumpHelpText1, lblBumpHelpText2, btnBumpShare;
@synthesize btnBumpCancel, ivBumpBackground, viewBumpHelp;

@synthesize wizDlgDismissed, wizPBag;

@synthesize lblNoData, btnNoData, viewNoData;

@synthesize postLoginAttribute;

#define kSectionCards 0
#define kSectionQE 1
#define kSectionApprovals 3
#define kSectionReport 2
#define kButtonW 57
#define kButtonH 59

int sectionCorpTrip = 0;
int sectionCorpQE = 100;
int sectionCorpReport = 1;

-(void)refreshPanelViews
{
    [self refreshHeader];
        
    canvas.marginTop = 56;
    canvas.marginLeft = 14;
    headerPanel.marginLeft = 10.0;
    headerPanel.marginRight = 10.0;
        
    mainPanel.autoFit = YES;
    mainPanel.stackVertically = NO;
    contentPanel.percentWidth = 100;
    contentPanel.marginLeft = 20;
    h2.marginRight = 2;
        
    h1_2.percentWidth = 100;
    h1_2.paddingLeft = 18;
        
    h1_3.percentWidth = 100;
    h1_4.percentWidth = 100;
    h2.percentHeight = 100;
        
    currentTripSummaryView.stackVertically = NO;
    currentTripSummaryView.autoFit = NO;
        
    tripCategoryIconsView.stackVertically = NO;
    if ([ExSystem isLandscape])
    {
        currentTripSummaryView.paddingLeft = 70.0;
        currentTripSummaryView.paddingRight = 70.0;
    }
    else
    {
        currentTripSummaryView.paddingLeft = 25.0;
        currentTripSummaryView.paddingRight = 25.0;
    }
    currentTripSummaryView.paddingTop = 16;
    currentTripSummaryView.paddingBottom = 16;
    currentTripSummaryView.marginBottom = 7;
    
    addFunctionsView.stackVertically = NO;
        
    addFunctionsView.paddingTop = 30;
    addFunctionsView.paddingBottom = 30;
    addFunctionsView.paddingLeft = 12;
    addFunctionsView.paddingRight = 12;
    addFunctionsView.marginBottom = 7;
        
    separatorView.marginBottom = 7;
        
    self.addFuncScroller.scrollEnabled = YES;
    self.addFuncScroller.alwaysBounceVertical = NO;
        
    expSummaryView.stackVertically = NO;
    expSummaryView.paddingTop = 16;
    expSummaryView.paddingBottom = 16;
    expSummaryView.paddingLeft = 12;
    expSummaryView.paddingRight = 12;
    expSummaryView.marginBottom = 7;
    self.expSummaryScroller.scrollEnabled = YES;
    self.expSummaryScroller.alwaysBounceVertical = NO;
}

- (void)createQuickActionLabel:(NSString *)btnTitle forButton:(UIButton*)btn
{
    UILabel *lblTitle = [[UILabel alloc] initWithFrame:CGRectMake(20, 25, 120, 40)];
    [lblTitle setFont:[UIFont fontWithName:@"Helvetica Neue Bold" size:16]];
    [lblTitle setTextColor:[UIColor colorWithRed:0.16f green:0.16f blue:0.16f alpha:1]];
    [lblTitle setBackgroundColor:[UIColor clearColor]];
    [lblTitle setTextAlignment:NSTextAlignmentCenter];
    [lblTitle setText:btnTitle];
    
    [btn addSubview:lblTitle];
}

-(void) refetchData
{
}

-(void) refreshHomeScreen
{
	[self refreshSkin];
	[self fetchHomePageData];
	self.requireHomeScreenRefresh = false;
}

- (void)setSegmentButtonBackgrounds {
    if ([ExSystem isLandscape])
    {
        [btnReportsTable setBackgroundImage:[UIImage imageNamed:@"tab_leftblue_ipad.png"] forState:UIControlStateSelected];
        [btnReportsTable setBackgroundImage:[UIImage imageNamed:@"tab_leftblue_ipad.png"] forState:UIControlStateHighlighted];
        [btnReportsTable setBackgroundImage:[UIImage imageNamed:@"tab_leftblue_ipad.png"] forState:UIControlStateDisabled];
        [btnReportsTable setBackgroundImage:[UIImage imageNamed:@"tab_leftgray_ipad.png"] forState:UIControlStateNormal];
        
        [btnTripItTable setBackgroundImage:[UIImage imageNamed:@"tab_rightblue_ipad.png"] forState:UIControlStateSelected];
        [btnTripItTable setBackgroundImage:[UIImage imageNamed:@"tab_rightblue_ipad.png"] forState:UIControlStateHighlighted];
        [btnTripItTable setBackgroundImage:[UIImage imageNamed:@"tab_rightblue_ipad.png"] forState:UIControlStateDisabled];
        [btnTripItTable setBackgroundImage:[UIImage imageNamed:@"tab_rightgray_ipad.png"] forState:UIControlStateNormal];
    }
    else
    {
        [btnReportsTable setBackgroundImage:[UIImage imageNamed:@"tab_leftblueportrait_ipad.png"] forState:UIControlStateSelected];
        [btnReportsTable setBackgroundImage:[UIImage imageNamed:@"tab_leftblueportrait_ipad.png"] forState:UIControlStateHighlighted];
        [btnReportsTable setBackgroundImage:[UIImage imageNamed:@"tab_leftblueportrait_ipad.png"] forState:UIControlStateDisabled];
        [btnReportsTable setBackgroundImage:[UIImage imageNamed:@"tab_leftgrayportrait_ipad.png"] forState:UIControlStateNormal];
        
        [btnTripItTable setBackgroundImage:[UIImage imageNamed:@"tab_rightblueportrait_ipad.png"] forState:UIControlStateSelected];
        [btnTripItTable setBackgroundImage:[UIImage imageNamed:@"tab_rightblueportrait_ipad.png"] forState:UIControlStateHighlighted];
        [btnTripItTable setBackgroundImage:[UIImage imageNamed:@"tab_rightblueportrait_ipad.png"] forState:UIControlStateDisabled];
        [btnTripItTable setBackgroundImage:[UIImage imageNamed:@"tab_rightgrayportrait_ipad.png"] forState:UIControlStateNormal];
    }
}

-(void)animate1:(id)sender
{
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:0.5];
    [UIView setAnimationDelegate:self];
    viewPadIntro.alpha = 0.0;
    [UIView setAnimationDidStopSelector:@selector(animate2:)];
    [UIView commitAnimations];
}

-(void)animate2:(id)sender
{
    self.viewPadIntro.hidden = YES;
    [viewPadIntro removeFromSuperview];
    
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    checkedMileage = FALSE;
    
    self.viewPadIntro = [[NSBundle mainBundle] loadNibNamed:@"ViewPadIntro" owner:self options:nil][0];

    if([ExSystem isLandscape])
    {
        viewPadIntro.iv2.image = nil;
        viewPadIntro.iv.image = [UIImage imageNamed:@"Default-Landscape~ipad"];
    }
    else {
        viewPadIntro.iv2.image = nil;
        viewPadIntro.iv.image = [UIImage imageNamed:@"Default-Portrait~ipad"];
    }

    
    
	self.aKeys = nil;
    
    self.aKeys = [[NSMutableArray alloc] initWithObjects:@"Trips", @"Expenses", @"Reports", @"Approvals", @"Information", nil];
	self.dictSkins = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	[self loadSkins];
	
    self.postLoginAttribute = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil]; //Used to figure out if 'safeHarbor' message needed to display. Contain necessary value after login

	[super viewDidLoad];
    
    [viewNew setHidden:YES];
    [viewBumpHelp setHidden:YES];
    
    self.homeViews = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
    hasAir = NO;
    hasRail = NO;
        
    [self adjustHomeButtons:self];
        
    if(btnLogout != nil)
        [btnLogout removeFromSuperview];
        
    self.btnLogout = [ExSystem makeColoredButtonRegular:@"DARK_BLUE" W:80.0 H:30.0 Text:[Localizer getLocalizedText:@"Logout"] SelectorString:@"buttonLogoutPressed:" MobileVC:self];
    btnLogout.frame = CGRectMake(888, 72, 80.0, 30);
    [self.view addSubview:btnLogout];
        
    if(btnSettings != nil)
        [btnSettings removeFromSuperview];
    self.btnSettings = [ExSystem makeColoredButtonRegular:@"DARK_BLUE" W:80.0 H:30.0 Text:[Localizer getLocalizedText:@"Settings"] SelectorString:@"buttonSettingsPressed:" MobileVC:self];
    btnSettings.frame = CGRectMake(800, 72, 80.0, 30);
    [self.view addSubview:btnSettings];
    [self updateXIBLabels];
        

        
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(queueCountChangedNotification:) name:@"queue item count changed" object:nil];
        
    [self refreshHeader];
    [self refreshPanelViews];
    [self showViewsByUserTypes];
    
	[self performSelector:@selector(switchAd:) withObject:nil afterDelay:1.0f];
	
    if ([ExSystem isLandscape])
    {
        [self adjustForLandscape];
    }
    else
    {
        [self adjustForPortrait];
    }
	
	self.showWhatsNew = [[ExSystem sharedInstance].sys.showWhatsNew boolValue];
    
	if(showWhatsNew)
	{
        self.viewNew = [[NSBundle mainBundle] loadNibNamed:@"WhatsNewView" owner:self options:nil][0];
        
        viewNew.scroller.contentSize = CGSizeMake(280 * 2, 288);
        [self.view addSubview:viewNew];
        if(self.viewNew != nil)
            [self.view bringSubviewToFront:viewNew];
        
        
		if([ExSystem isLandscape])
			[self adjustWhatsNewLandscape];
		else
			[self adjustWhatsNewPortrait];
		
	
        self.lblNewTitle.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"What's New in Version"], [ExSystem sharedInstance].sys.currentVersion];
	}
    [self.view addSubview:viewPadIntro];
    if(self.viewPadIntro != nil)
        [self.view bringSubviewToFront:viewPadIntro];
    
    [self performSelector:@selector(animate1:) withObject:nil afterDelay:0.1];
}


- (void)viewDidAppear:(BOOL)animated
{
	[self checkOffline];
	[self.navigationController.navigationBar setHidden:YES];
	[super viewDidAppear:animated];
	
	if (![[ApplicationLock sharedInstance] isLoggedIn])
	{
		[[ApplicationLock sharedInstance] onHomeScreenAppeared];
	}
	else
	{
		if (requireHomeScreenRefresh)
		{
			[self refreshHomeScreen];
		}
	}
}

-(void) updatedDisplayOfQueuedItemCount
{
    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    if (itemNum > 0)
    {
        [self adjustViewForUploadButton:itemNum];
    }
    else
        [self adjustViewForNoUploadButton];
}

-(void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
}

-(void) forceReload
{
    requireHomeScreenRefresh = true;
}

- (void)viewWillAppear:(BOOL)animated
{
	[self checkOffline];
    self.hideReceiptStore = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"HIDE_RECEIPT_STORE" withType:@"CTE_EXPENSE_ADMIN"]];
	
    [self configureViews];
    
    viewNew.frame = self.view.frame;
    viewNew.bounds = self.view.bounds;
}

-(void) configureViews
{
    if (![[ApplicationLock sharedInstance] isLoggedIn])
        return;
    
    if (!self.isViewLoaded)
        return;
    
    if ([[ApplicationLock sharedInstance] isLoggedIn]) //if(toolCount >= 1)
        [self adjustHomeButtons:self];
    
    [self refreshHeader];
    
    if (!isExpenseOnly)
    {
        self.currentTripSummaryView.hidden = NO;
        [self refreshTripData];
    }
    else
    {
        self.currentTripSummaryView.hidden = YES;
    }
    
    if (isTravelOnly && [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER]){
        self.addFunctionsView.hidden = NO;
        self.separatorView.hidden = YES;
        self.expSummaryView.hidden = NO;
    }
    else if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER] || (isTravelOnly && ![[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER]))
    {
        // Treat GOV user like a travel only user for now.
        self.addFunctionsView.hidden = NO;
        self.separatorView.hidden = NO;
        self.expSummaryView.hidden = YES;
    }
    else if(isItinViewerOnly)
    {
        self.addFunctionsView.hidden = YES;
        self.separatorView.hidden = NO;
        self.expSummaryView.hidden = YES;
    }
    else
    {
        self.addFunctionsView.hidden = NO;
        self.separatorView.hidden = YES;
        ivTravelUserPromoBanner.image = nil;
        self.expSummaryView.hidden = NO;
        
        [self refreshOOPData];
        [self refreshSummaryData];
    }
    
    if([ExSystem isLandscape])
        [self adjustForLandscape];
    else
        [self adjustForPortrait];
    
    [self updatedDisplayOfQueuedItemCount];
}

#pragma mark - some other stuff that does not belong with vc load stuff
-(void) updateXIBLabels
{
	lblExpenseWait.text = [Localizer getLocalizedText:@"Fetching Expense Data"];
	lblExpense.text = [Localizer getLocalizedText:@"Expense"];
	lblTripWait.text = [Localizer getLocalizedText:@"Fetching Trip"];
}


-(void) refreshSkin
{
	NSMutableDictionary *skin = dictSkins[@"default"];
	
	if(skin == nil)
		return;
	
    skin[@"header"] = @"concur_header_landscape";
    skin[@"header_portrait"] = @"concur_header_portrait";
    [self refreshHeader];
    [self.lblOffline setHidden:YES];
}

-(void) refreshSkinForOffline
{
    NSMutableDictionary *skin = dictSkins[@"default"];
	
	if(skin == nil)
		return;
    
    skin[@"header"] = @"offline_header_landscape";
    skin[@"header_portrait"] = @"offline_header_portrait";
    [self.lblOffline setText:[Localizer getLocalizedText:@"Offline"]];
    [self refreshHeader];
    [self.lblOffline setHidden:NO];

}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}


- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.lblOffline = nil;
	self.btnLogout = nil;
	self.btnSettings = nil;
    self.btnUpload = nil;
	self.btnAddExpense = nil;
    
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"queue item count changed" object:nil];
    [self killHomeData];
	
	self.lblNewTitle = nil;
	self.scrollerNew = nil;
	self.btnNewOK = nil;
	self.ivNewBackground = nil;
	self.viewNew = nil;
}




#pragma mark -
#pragma mark Add Expense
- (IBAction)buttonAddPressed:(id)sender
{
    self.addExpenseClicked = TRUE;
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER]) {
        [self buttonGovQuickPressed:nil];
    } else {
        [self buttonQuickPressed:nil];
    }
}


-(void) buttonQuickPressed:(id)sender
{
    NSDictionary *dictionary = @{@"Action": @"Quick Expense"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    QEFormVC *fromVC = [[QEFormVC alloc] initWithEntryOrNil:nil withCloseButton:YES];
    // MOB-8440 - allow dismiss keyboard from formsheet
    UINavigationController *navi = [[KeyboardNavigationController alloc] initWithRootViewController:fromVC];
    navi.modalPresentationStyle = UIModalPresentationFormSheet;
    navi.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
    navi.toolbar.tintColor = [UIColor navBarTintColor_iPad];
    [self.navigationController presentViewController:navi animated:YES completion:nil];
}

-(IBAction) buttonGovQuickPressed:(id)sender
{
    NSDictionary *dictionary = @{@"Action": @"Quick Expense"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    if ([Config isGov])
    {
        GovExpenseEditViewController* vc = [[GovExpenseEditViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
        
        UINavigationController *navi = [[KeyboardNavigationController alloc] initWithRootViewController:vc];
        navi.modalPresentationStyle = UIModalPresentationFormSheet;
        [self.navigationController presentViewController:navi animated:YES completion:nil];
    }
}

#pragma mark -
#pragma mark popover methods

-(void) refreshReportList
{
    // After new report, Refresh report list on SU ipad home
}

-(void) goToReportDetailScreen:(ReportData*)newRpt
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: newRpt, @"REPORT", newRpt.rptKey, @"ID_KEY",
                                 newRpt, @"REPORT_DETAIL",
                                 newRpt.rptKey, @"RECORD_KEY",
                                 ROLE_EXPENSE_TRAVELER, @"ROLE",
                                 @"YES", @"REPORT_CREATE_WIZARD",
                                 @"YES", @"SHORT_CIRCUIT"
                                 ,nil];
    
    [self switchToDetail:@"Report" ParameterBag:pBag];
    
}

-(void) reportCreated:(ReportData*) newRpt
{
    // Redirect to report detail as REPORT_CREATE_WIZARD
    // MOB-8671 Adopt code from ActiveReportListVC.reportCreated
    [self refreshReportList];
    [self.navigationController dismissViewControllerAnimated:YES completion:nil];
    [self goToReportDetailScreen:newRpt];
}

-(IBAction) buttonCreateReportPressed:(id)sender
{
    NSDictionary *dictionary = @{@"Action": @"Create Report"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
	ReportSummaryViewController *vc = [[ReportSummaryViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
    vc.title = [Localizer getLocalizedText:@"Create Report"];
	vc.role = ROLE_EXPENSE_TRAVELER;
    vc.delegate = self;
	// Retrieve report form data
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 vc.role, @"ROLE_CODE",
								 nil];
    
	UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:vc];
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	[localNavigationController setToolbarHidden:NO];
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];
    
    [[ExSystem sharedInstance].msgControl createMsg:REPORT_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:vc];
    
	
}

-(IBAction) buttonTravelBookingAppsMenuPressed:(id)sender
{
	[self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"Book Trip"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	MobileViewController *vc = (MobileViewController *)[self.navigationController topViewController];
	
	if(vc.pickerPopOver != nil)
	{
		[vc.pickerPopOver dismissPopoverAnimated:YES];
		vc.pickerPopOver = nil;
	}
	
    AppsMenuVC *appsMenuVC = [[AppsMenuVC alloc] initWithNibName:@"AppsMenuVC" bundle:nil];
	appsMenuVC.iPadHome = self;
	appsMenuVC.displayAppsOfType = BOOKING_APPS;
    
	vc.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:appsMenuVC];
    
    
	CGRect myRect = CGRectMake(0, 0, 0, 0);
	ButtonSegment *btn = (ButtonSegment *)sender;
	if([vc isKindOfClass:[iPadHomeVC class]])
	{
		myRect = [self.view convertRect:btn.parentView.frame fromView:scrollerButtons];
	}
	else if([vc isKindOfClass:[ReportDetailViewController_iPad class]])
	{
		ReportDetailViewController_iPad *dvc = (ReportDetailViewController_iPad *)vc;
		myRect = [dvc.view convertRect:btn.parentView.frame fromView:dvc.scrollerButtons];
	}
	else if([vc isKindOfClass:[DetailViewController class]])
	{
		DetailViewController *dvc = (DetailViewController *)vc;
		myRect = [dvc.view convertRect:btn.parentView.frame fromView:dvc.scrollerButtons];
	}
	
    [vc.pickerPopOver presentPopoverFromRect:myRect inView:vc.view permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES];
}

-(IBAction) buttonAppsMenuPressed:(id)sender
{
	[self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"Apps Menu"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	MobileViewController *vc = (MobileViewController *)[self.navigationController topViewController];
	
	if(vc.pickerPopOver != nil)
	{
		[vc.pickerPopOver dismissPopoverAnimated:YES];
		vc.pickerPopOver = nil;
	}
	
	AppsMenuVC *appsMenuVC = [[AppsMenuVC alloc] initWithNibName:@"AppsMenuVC" bundle:nil];
	appsMenuVC.iPadHome = self;
    appsMenuVC.displayAppsOfType = TRAVEL_APPS;
	
	vc.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:appsMenuVC];
    
	CGRect myRect = CGRectMake(0, 0, 0, 0);
	ButtonSegment *btn = (ButtonSegment *)sender;
	if([vc isKindOfClass:[iPadHomeVC class]])
	{
		myRect = [self.view convertRect:btn.parentView.frame fromView:scrollerButtons];
	}
	else if([vc isKindOfClass:[ReportDetailViewController_iPad class]])
	{
		ReportDetailViewController_iPad *dvc = (ReportDetailViewController_iPad *)vc;
		myRect = [dvc.view convertRect:btn.parentView.frame fromView:dvc.scrollerButtons];
	}
	else if([vc isKindOfClass:[DetailViewController class]])
	{
		DetailViewController *dvc = (DetailViewController *)vc;
		myRect = [dvc.view convertRect:btn.parentView.frame fromView:dvc.scrollerButtons];
	}
	
    [vc.pickerPopOver presentPopoverFromRect:myRect inView:vc.view permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES];
}

-(IBAction) buttonRailPressed:(id)sender
{
	[self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"Book Rail"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
	if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
    
    if ([Config isGov])
    {
        // Add a dummy root vc for the modal dialog, so that we can switch out the GovSelectTANumVC to land on booking criteria page
        FormViewControllerBase *nextController = [[FormViewControllerBase alloc] initWithNibName:@"EditFormView" bundle:nil];
        
        UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:nextController];
        
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        
        [localNavigationController setToolbarHidden:NO];
        localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        
        [self presentViewController:localNavigationController animated:NO completion:nil];
        [GovSelectTANumVC showSelectTANum:nextController withCompletion:@"Book Rail" withFields:nil withDelegate:nil asRoot:YES];
    }
    
    else
    {
        TrainBookVC *nextController = [[TrainBookVC alloc] initWithNibName:@"TrainBookVC" bundle:nil];
        
        UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:nextController];
        
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        
        [localNavigationController setToolbarHidden:NO];
        localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        
        [self presentViewController:localNavigationController animated:YES completion:nil];
    }
}

-(IBAction) buttonCarPressed:(id)sender
{
	[self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"Book Car"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Bookings offline"]
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return;
	}
    
    
	if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
	
    if ([Config isGov])
    {
        // Add a dummy root vc for the modal dialog, so that we can switch out the GovSelectTANumVC to land on booking criteria page
        FormViewControllerBase *nextController = [[FormViewControllerBase alloc] initWithNibName:@"EditFormView" bundle:nil];
        
        UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:nextController];
        
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        
        [localNavigationController setToolbarHidden:NO];
        localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        
        [self presentViewController:localNavigationController animated:NO completion:nil];
        [GovSelectTANumVC showSelectTANum:nextController withCompletion:@"Book Car" withFields:nil withDelegate:nil asRoot:YES];
    }
    
    else{
        CarViewController *nextController = [[CarViewController alloc] initWithNibName:@"CarViewController" bundle:nil];
        
        UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:nextController];
        
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        
        [localNavigationController setToolbarHidden:NO];
        localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        
        [self presentViewController:localNavigationController animated:YES completion:nil];
    }
}

-(IBAction) buttonHotelPressed:(id)sender
{
	[self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"Book Hotel"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
	if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Bookings offline"]
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return;
	}
    
	if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
	
    if ([Config isGov])
    {
        // Add a dummy root vc for the modal dialog, so that we can switch out the GovSelectTANumVC to land on booking criteria page
        FormViewControllerBase *nextController = [[FormViewControllerBase alloc] initWithNibName:@"EditFormView" bundle:nil];
        
        UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:nextController];
        
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        
        [localNavigationController setToolbarHidden:NO];
        localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        
        [self presentViewController:localNavigationController animated:NO completion:nil];
        [GovSelectTANumVC showSelectTANum:nextController withCompletion:@"Book Hotel" withFields:nil withDelegate:nil asRoot:YES];
    }
    else{
        HotelViewController *nextController = [[HotelViewController alloc] initWithNibName:@"HotelViewController" bundle:nil];
        
        UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:nextController];
        
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        
        [localNavigationController setToolbarHidden:NO];
        //A---  get rid of this
        localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        
        //B--- put this in applicationdidfinishlaunch withOptions
        [[UINavigationBar appearance] setTintColor:[UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1]];
        
        [self presentViewController:localNavigationController animated:YES completion:nil];
    }
}

-(IBAction) buttonAirPressed:(id)sender
{
    [self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"Book Air"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    return;
    
	
	
	if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Bookings offline"]
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return;
	}
    
	
	if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
	
    if ([Config isGov])
    {
        // Add a dummy root vc for the modal dialog, so that we can switch out the GovSelectTANumVC to land on booking criteria page
        FormViewControllerBase *nextController = [[FormViewControllerBase alloc] initWithNibName:@"EditFormView" bundle:nil];
        
        UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:nextController];
        
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        
        [localNavigationController setToolbarHidden:NO];
        localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        
        [self presentViewController:localNavigationController animated:NO completion:nil];
        [GovSelectTANumVC showSelectTANum:nextController withCompletion:@"Book Air" withFields:nil withDelegate:nil asRoot:YES];
    }
    else{
        AirBookingCriteriaVC *nextController = [[AirBookingCriteriaVC alloc] initWithNibName:@"AirBookingCriteriaVC" bundle:nil];
        
        UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:nextController];
        
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        
        [localNavigationController setToolbarHidden:NO];
        localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
        
        [self presentViewController:localNavigationController animated:YES completion:nil];
    }
}


- (IBAction)buttonExpensesPressed:(id)sender
{
	[self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"View Expenses"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}

    if ([Config isGov])
    {
        oopeListVC = [[OutOfPocketListViewController alloc] initWithNibName:@"OutOfPocketListViewController" bundle:nil];
        oopeListVC.padHomeVC = self;
        [oopeListVC loadExpenses];
    }
    else
        [(QuickExpensesReceiptStoreVC*)oopeListVC setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:YES allowListEdit:YES];
    
    // MOB-8440 Allow dismiss of keyboard from formsheet to prevent orphaned keyboard events when coming to foreground
	UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:oopeListVC];
    //	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:oopeListVC];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
    [localNavigationController setToolbarHidden:NO];
	//R 162 , G 160, B 160
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];
	
}


- (IBAction)buttonTripsPressed:(id)sender
{
	[self performSelector:@selector(tripsPressed:) withObject:sender afterDelay:0.01f];
}


//-(IBAction)buttonCardsPressed:(id) sender
//{
//    // This is single user only
//    if(pickerPopOver != nil)
//	{
//		[pickerPopOver dismissPopoverAnimated:YES];
//        pickerPopOver = nil;
//	}
//    
//    // MOB-8982 If there is never a card, pop up agreement view
//    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_CARDS];
//    MobileViewController* vc = nil;
//    if (entity != nil)
//    {
//        vc = [[SearchYodleeCardsVC alloc] initWithNibName:@"SearchYodleeCardsVC" bundle:nil];
//    }
//    else
//    {
//        vc = [[YodleeCardAgreementVC alloc] initWithNibName:@"YodleeWebView" bundle:nil];
//    }
//    
//	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:vc];
//	
//	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
//	
//    [localNavigationController setToolbarHidden:NO];
//	//R 162 , G 160, B 160
//	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
//	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
//	
//	[self presentViewController:localNavigationController animated:YES completion:nil];
//}

-(void) tripsPressed:(id)sender
{
	[self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"View Trips"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
	
	if([tripsData.trips count] == 1)
	{
		NSString *key = (tripsData.keys)[0];
        EntityTrip *trip = [[TripManager sharedInstance] fetchByTripKey:key];
        
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:trip, @"TRIP", nil];
		[self switchToDetail:@"Trip" ParameterBag:pBag];
		return;
	}
	
	tripsListVC = [[TripsViewController alloc] initWithNibName:@"TripsView" bundle:nil];
	tripsListVC.iPadHome = self;
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:tripsListVC];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	//R 162 , G 160, B 160
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];
	[tripsListVC loadTrips];
}

- (IBAction)buttonApprovalsPressed:(id)sender
{
	[self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"View Report Approval"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
	
	
	self.approvalsListVC = [[ReportApprovalListViewController alloc] initWithNibName:@"ReportApprovalListViewController" bundle:nil];
	approvalsListVC.iPadHome = self;
	approvalsListVC.isPad = YES;
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:approvalsListVC];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	//R 162 , G 160, B 160
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];
	// Calling loadApprovals *after* the popover is presented, because the view controller will only receive the message response if it's visible.
	[approvalsListVC loadApprovals];
}


- (IBAction)buttonReportsPressed:(id)sender
{
	[self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"View Reports"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
	
	
	self.reportsListVC = [[ActiveReportListViewController alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
	self.reportsListVC.enablefilterUnsubmittedActiveReports = NO;
	reportsListVC.iPadHome = self;
	reportsListVC.isPad = YES;
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:reportsListVC];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	//R 162 , G 160, B 160
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];
	// Calling loadReports *after* the popover is presented, because the view controller will only receive the message response if it's visible.
	[reportsListVC loadReports];
}

- (IBAction)buttonReportsFromScrollerPressed:(id)sender
{
	[self checkOffline];
	
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
	
	
	self.reportsListVC = [[ActiveReportListViewController alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
	self.reportsListVC.enablefilterUnsubmittedActiveReports = NO;
	reportsListVC.iPadHome = self;
	reportsListVC.isPad = YES;
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:reportsListVC];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	//R 162 , G 160, B 160
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];
	
	[reportsListVC loadReports];
}

- (IBAction)buttonApprovalsFromScrollerPressed:(id)sender
{
	[self checkOffline];
	
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
	
	
	self.approvalsListVC = [[ReportApprovalListViewController alloc] initWithNibName:@"ReportApprovalListViewController" bundle:nil];
	approvalsListVC.iPadHome = self;
	approvalsListVC.isPad = YES;
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:approvalsListVC];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	//R 162 , G 160, B 160
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];
	// Calling load *after* the popover is presented, because the view controller will only receive the message response if it's visible.
	[approvalsListVC loadApprovals];
}


-(void) removeSubViews
{
	NSMutableArray *homeKeys = [[NSMutableArray alloc] initWithObjects:nil];
	
	for(NSString *key in homeViews)
	{
		MobileViewController *vc = homeViews[key];
		if(vc.pickerPopOver != nil)
			[vc.pickerPopOver dismissPopoverAnimated:NO];
		//[vc.view removeFromSuperview];
		[homeKeys addObject:key];
	}
	
	for(NSString *key in homeKeys)
		[homeViews removeObjectForKey:key];
	
	
	//[self.navigationController popToRootViewControllerAnimated:YES];
	[self.navigationController popViewControllerAnimated:NO];
}

//MOB-7247
-(IBAction)buttonLocationCheckPressed:(id)sender
{
    if (![ExSystem connectedToNetwork])
    {
 		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Location Check Offline"]
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return;
        
    }
    
    SafetyCheckInVC* vc = [[SafetyCheckInVC alloc] initWithNibName:@"EditFormView" bundle:nil];
    [vc setSeedData:nil];
    
    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:vc];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    
	[localNavigationController setToolbarHidden:NO];
	//R 162 , G 160, B 160
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
    
    [self presentViewController:localNavigationController animated:YES completion:NULL];
}

#pragma mark -
#pragma mark Receipt Attach methods
- (IBAction)buttonReceiptsPressed:(id)sender
{
	[self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"Receipt Store"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
	
	
	self.receiptStoreVC = [[ReceiptStoreListView alloc] initWithNibName:@"ReceiptStoreListView" bundle:nil];
	receiptStoreVC.title = [Localizer getLocalizedText:@"Receipts"];
    
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:receiptStoreVC];
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	[localNavigationController setToolbarHidden:NO];
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];
}


-(IBAction)btnUploadReceiptPressed:(id)sender
{
	self.btnUploadReceipt = (UIButton*)sender;
	
	UIActionSheet *receiptActions = nil;
	
	if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
	{
		receiptActions = [[MobileActionSheet alloc] initWithTitle:nil
														 delegate:self
												cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
										   destructiveButtonTitle:nil
												otherButtonTitles:[Localizer getLocalizedText:@"Camera"],
						  [Localizer getLocalizedText:@"Photo Album"], nil];
	}
	else {
		receiptActions = [[MobileActionSheet alloc] initWithTitle:nil
														 delegate:self
												cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
										   destructiveButtonTitle:nil
												otherButtonTitles:[Localizer getLocalizedText:@"Photo Album"], nil];
	}
	
	receiptActions.tag = kActionViewAddReceipt;
	CGRect cellRect = CGRectMake(addFuncScroller.frame.origin.x+btnUploadReceipt.frame.origin.x, btnUploadReceipt.frame.origin.y, btnUploadReceipt.frame.size.width, btnUploadReceipt.frame.size.height);
	[receiptActions showFromRect:cellRect inView:self.addFuncScroller animated:YES];
}


-(void) buttonCameraPressed
{
	if (![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
		return;
	
	UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker];
	imgPicker.sourceType = UIImagePickerControllerSourceTypeCamera;
	[UnifiedImagePicker sharedInstance].delegate = self;
	imgPicker.allowsEditing = NO;
	
	if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:imgPicker];
	
	CGRect cellRect = [btnUploadReceipt frame];
    
    @try
    {
        [pickerPopOver presentPopoverFromRect:cellRect inView:self.addFuncScroller permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES];
    }
    @catch (NSException *exception)
    {
        NSLog(@"Error presenting Image Picker: %@",[exception reason]);
        [Flurry logError:@"Receipts: Failure" message:@"Error presenting Image Picker" exception:exception];
    }
}


-(void)buttonAlbumPressed
{
	UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker];
	imgPicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
	imgPicker.allowsEditing = YES;
	imgPicker.wantsFullScreenLayout = YES;
	[UnifiedImagePicker sharedInstance].delegate = self;
	
	if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:imgPicker];
	
	CGRect cellRect = [btnUploadReceipt frame];
    
    @try
    {
        [pickerPopOver presentPopoverFromRect:cellRect inView:self.addFuncScroller permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES];
    }
    @catch (NSException *exception)
    {
        NSLog(@"Error presenting Image Picker: %@",[exception reason]);
        [Flurry logError:@"Receipts: Failure" message:@"Error presenting Image Picker" exception:exception];
    }
}

#pragma mark - Offline Receipt upload queue support
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

-(void) queueCountChangedNotification:(NSNotification *) notification
{
    if ([[notification name] isEqualToString:@"queue item count changed"]) {
        int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
        if (itemNum > 0)
        {
            [self adjustViewForUploadButton:itemNum];
        }
        else
            [self adjustViewForNoUploadButton];
    }
}

-(void) adjustViewForUploadButton: (int) numOfItem
{
    NSString *btnText = [NSString stringWithFormat:@" %d %@", numOfItem, [Localizer getLocalizedText:@"items to upload"]];
    CGSize fontSize = [btnText sizeWithFont:[UIFont fontWithName:@"Helvetica-Bold" size: 15]];
    float capW = 7.0f;
    
    CGRect buttonFrame = CGRectMake(35, 72, fontSize.width+40, 30);
    [self.btnUpload setFrame:buttonFrame];
    [self.btnUpload setBackgroundImage:[[UIImage imageNamed:@"ipad_drk_blue_button"] stretchableImageWithLeftCapWidth:capW topCapHeight:0.0f] forState:UIControlStateNormal];
    
    [self.btnUpload setTitle:btnText forState:UIControlStateNormal];
    
    [self.view addSubview:btnUpload];
    
    [self.btnUpload setHidden:NO];
}
-(void) adjustViewForNoUploadButton
{
    [self.btnUpload setHidden:YES];
    [self.btnUpload removeFromSuperview];
}

- (IBAction)btnUploadPressed:(id)sender
{
    UploadQueueViewController *vc = [[UploadQueueViewController alloc] initWithNibName:@"UploadQueueViewController" bundle:nil];
    UIBarButtonItem *VcUploadBtn = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Upload"] style:UIBarButtonItemStyleBordered target:vc action:@selector(startUpload)];
    UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:vc action:@selector(closeMe:)];
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:vc];
    
    vc.title = [Localizer getLocalizedText:@"Upload Queue"];
    vc.delegate = self;
    vc.navigationItem.leftBarButtonItem = btnClose;
    vc.navigationItem.rightBarButtonItem = VcUploadBtn;
    
    [navi setModalPresentationStyle:UIModalPresentationFormSheet];
    navi.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
    [self presentViewController:navi animated:YES completion:NULL];
}

#pragma mark - UploadQueueVCDelegate method
-(void) didDismissUploadQueueVC
{
    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    if (itemNum > 0)
    {
        [self adjustViewForUploadButton:itemNum];
    }
    else
        [self adjustViewForNoUploadButton];
}

#pragma mark -
#pragma negative view for segment control
-(void) popLinkToTripIt
{
    NSDictionary *dictionary = @{@"Action": @"Link to TripIt"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    TripItAuthVC *vc =  [[TripItAuthVC alloc] initWithNibName:@"TripItAuthVC" bundle:nil];
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:vc];
    navi.modalPresentationStyle = UIModalPresentationFormSheet;
    UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Cancel"] style:UIBarButtonItemStyleBordered target:vc action:@selector(closeMe:)];
    vc.navigationItem.leftBarButtonItem = btnClose;
    [self presentViewController:navi animated:YES completion:nil];
}

-(IBAction) actionOnSUNoData:(id)sender
{
    [self popLinkToTripIt];
    
}


-(void) hideSUNoDataView
{
    [self.viewNoData setHidden:YES];
}


-(void) showSUNoDataView
{
    NSString *imgName = @"blue_pill_button";
    [btnNoData setBackgroundImage:[UIImage imageNamed:imgName] forState:UIControlStateNormal];
    
    //    CGRect frame = self.btnNoData.frame;
    //    self.btnNoData = [ExSystem makeColoredButtonRegular:@"GREEN_BIG" W:280 H:40 Text:[Localizer getLocalizedText:@"Link with TripIt"] SelectorString:@"actionOnNoData:" MobileVC:self];
    //    self.btnNoData.frame = frame;
    self.btnNoData.hidden = YES;
    if ([self.btnTripItTable isSelected])
    {
        if ([ExSystem sharedInstance].isTripItLinked)
            lblNoData.text = [@"No TripIt Trips" localize];
        else {
            self.btnNoData.hidden = NO;
            lblNoData.text = @"Link your TripIt account to SmartExpense account using the button below";
            [btnNoData setTitle:[@"Link to TripIt" localize] forState:UIControlStateNormal];
        }
    }
    else {
        lblNoData.text = [@"No Reports" localize];
    }
    
    if ([ExSystem isLandscape])
    {
        CGRect f = self.viewNoData.frame;
        self.viewNoData.frame = CGRectMake(f.origin.x, 64, f.size.width, f.size.height);
    }
    else {
        CGRect f = self.viewNoData.frame;
        self.viewNoData.frame = CGRectMake(f.origin.x, 72, f.size.width, f.size.height);
    }
    [self.viewNoData setHidden:NO];
    [self.contentTableContainer bringSubviewToFront:viewNoData];
    
    if(self.viewPadIntro != nil)
        [self.view bringSubviewToFront:viewPadIntro];
    
}

-(void) initSUReportsView
{
    if (![self.btnTripItTable isSelected])
    {
        if (self.reportsListVC.aKeys == nil || [self.reportsListVC.aKeys count] ==0)
        {
            [self showSUNoDataView];
        }
        else {
            [self hideSUNoDataView];
        }
    }
}

#pragma mark -
#pragma segment control action
- (IBAction)segmentSwitch:(id)sender {
    NSInteger selectedBtn = [(UIButton*)sender tag];
    
    switch (selectedBtn)
    {
        case 9901: //Reports
        {
            [btnTripItTable setSelected:NO];
            [btnReportsTable setSelected:YES];
            [self hideNoDataView];
            
        }
            break;
        case 9902: //tripit
        {
            [self hideNoDataView];
            [btnTripItTable setSelected:YES];
            [btnReportsTable setSelected:NO];
        }
            break;
        default:
            break;
    }
}

-(void) checkStateOfTrips
{
    if(self.tripItTripsTableView.hidden)
        return;
    
    if(![ExSystem sharedInstance].isTripItLinked)
    {
        [self showSUNoDataView];
    }
    else
    {
        if ([self.tripsVC hasTrips])
        {
            [self hideSUNoDataView];
        }
        else {
            [self showSUNoDataView];
        }
    }
}

#pragma mark -
#pragma mark Alert Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
	if (alertView.tag == RECEIPT_STORE_ALERT_ACTION)
	{
		if (buttonIndex == 1)
		{
			[self buttonReceiptsPressed:nil];
		}
	}
    else if (alertView.tag == PRIVACY_ACT_ALERT_ACTION)
    {
        if ([Config isGov])
        {
            // Display rules of behavior for gov Safe harbor required user
            if ([[postLoginAttribute objectForKey:@"NEED_SAFEHARBOR"] isEqualToString:@"true"])
            {
                GovLoginNoticeVC *noticeVC = [[GovLoginNoticeVC alloc] initWithNibName:@"LoginHelpTopicVC" bundle:nil];
                noticeVC.title = [Localizer getLocalizedText:@"Rules of Behavior"];
                UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:noticeVC];
                localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
                [self presentViewController:localNavigationController animated:YES completion:nil];
            }
        }
    }
}

#pragma mark -
#pragma mark ActionSheet delegate methods
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
	if(actionSheet.tag == kActionViewAddReceipt)
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
	}
}


#pragma mark -
#pragma mark UnifiedImagePickerDelegate methods
-(void)unifiedImagePickerSelectedImage:(UIImage*)image
{
    
    if ([UIDevice isPad])
	{
		if (self.pickerPopOver != nil)
		{
            @try
            {
                [self.pickerPopOver dismissPopoverAnimated:YES];
            }
            @catch (NSException *exception)
            {
                NSLog(@"Error dismissing the image picker: %@", [exception reason]);
                [Flurry logError:@"Receipts: Failure" message:@"Error dismissing the image picker" exception:exception];
            }
            
			self.pickerPopOver = nil;
		}
	}
    if (![ExSystem connectedToNetwork])
    {
        [ReceiptEditorVC queueReceiptImage:image date:[NSDate date]];
        //show banner when add first receipt offline from receipt store
        int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
        if (itemNum > 0)
        {
            [self adjustViewForUploadButton:itemNum];
            [self showQueueAlert];
        }
        else
        {
            [self adjustViewForNoUploadButton];
        }
    }
    else
    {
        UIActivityIndicatorView *activityView = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(btnUploadReceipt.frame.size.width/2 - 15,btnUploadReceipt.frame.size.height/2 - 15, 30, 30)];
        [activityView setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleGray];
        [btnUploadReceipt addSubview:activityView];
        [activityView startAnimating];
        [btnUploadReceipt setEnabled:NO];
        
        NSDictionary *dictionary = @{@"Action": @"Add Quick Receipt"};
        [Flurry logEvent:@"Home: Action" withParameters:dictionary];
        
        [self showWaitViewWithText:[Localizer getLocalizedText:@"Uploading receipt"]];
        [[ExReceiptManager sharedInstance] configureReceiptManagerForDelegate:self andReportEntry:nil andReporData:nil andExpenseEntry:nil andRole:nil];
        [[ExReceiptManager sharedInstance] uploadReceipt:image];
    }
}


#pragma mark -
#pragma mark Detail View Switching
-(void)switchToDetail:(NSString *)menuItem ParameterBag:(NSMutableDictionary *)pBag
{
	[self checkOffline];
	
	if([menuItem isEqualToString:@"Expenses"])
	{
	}
	else if ([menuItem isEqualToString:@"Trip"])
	{
		
		[self removeSubViews];
		
		DetailViewController *newDetailViewController = [[DetailViewController alloc] initWithNibName:@"BaseDetailVC_iPad" bundle:nil];
		newDetailViewController.iPadHome = self;
		[newDetailViewController.ivLogo setHidden:YES];
		EntityTrip *trip = pBag[@"TRIP"];
		newDetailViewController.tripsData = tripsData;
        
		[homeViews removeObjectForKey:menuItem];
		homeViews[menuItem] = newDetailViewController;
		
		// Dismiss the popover if it's present.
		if (pickerPopOver != nil)
        {
			[pickerPopOver dismissPopoverAnimated:YES];
            pickerPopOver = nil;
        }
		//////
		BOOL isNext = NO;
		NSString *direction = pBag[@"DIRECTION"];
		if([direction isEqualToString:@"NEXT"])
			isNext = YES;
		
		if(direction != nil)
		{
			
            //			[UIView beginAnimations:@"View Flip" context:nil];
            //			[UIView setAnimationDuration:0.80];
            //			[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
			
			if(isNext)
				[self.navigationController pushViewController:newDetailViewController animated:YES];
			else
			{
				[self.navigationController pushViewController:newDetailViewController animated:NO];
				
				DetailViewController *fakeVC = [[DetailViewController alloc] initWithNibName:@"BaseDetailVC_iPad" bundle:nil];
				fakeVC.iPadHome = self;
				[fakeVC.ivLogo setHidden:YES];
				//TripData *trip = [pBag objectForKey:@"TRIP"];
				fakeVC.tripsData = tripsData;
				//WARNING WORK fakeVC.view;
				//[fakeVC displayTrip:trip TripKey:trip.tripKey];
				[self.navigationController pushViewController:fakeVC animated:NO];
				[self.navigationController popViewControllerAnimated:YES];
			}
		}
		else
		{
			[self.navigationController pushViewController:newDetailViewController animated:YES];
		}
		[newDetailViewController displayTrip:trip TripKey:trip.tripKey];
		return;
	}
    else if ([menuItem isEqualToString:@"Document"])
    {
        if ([Config isGov])
        {
            [self removeSubViews];
            
            GovDocDetailVC_iPad *newDetailViewController = [[GovDocDetailVC_iPad alloc] initWithNibName:@"GovDocDetailVC_iPad" bundle:nil];
            newDetailViewController.iPadHome = self;
            
            [homeViews removeObjectForKey:menuItem];
            [homeViews setObject:newDetailViewController forKey:menuItem];
            
            // Dismiss the popover if it's present.
            if (pickerPopOver != nil)
            {
                [pickerPopOver dismissPopoverAnimated:YES];
                pickerPopOver = nil;
            }
            [newDetailViewController setSeedData:pBag];
            
            [self.navigationController pushViewController:newDetailViewController animated:YES];
            
            return;
        }
    }
	else if ([menuItem isEqualToString:@"Report"])
	{
		[self removeSubViews];
		
		ReportDetailViewController_iPad *newDetailViewController = [[ReportDetailViewController_iPad alloc] initWithNibName:@"ReportDetailViewController_iPad" bundle:nil];
		newDetailViewController.role = ROLE_EXPENSE_TRAVELER;
		newDetailViewController.iPadHome = self;
		newDetailViewController.isReport = YES;
		
		[homeViews removeObjectForKey:menuItem];
		homeViews[menuItem] = newDetailViewController;
		
		// Dismiss the popover if it's present.
		if (pickerPopOver != nil)
        {
			[pickerPopOver dismissPopoverAnimated:YES];
            pickerPopOver = nil;
        }
		
        [self.navigationController pushViewController:newDetailViewController animated:YES];
		
        NSString *comingFrom = pBag[@"COMING_FROM"];
        if (comingFrom == nil || ![comingFrom isEqualToString:@"TRIPIT"])
            pBag[@"COMING_FROM"] = @"REPORT";
        
        [newDetailViewController loadReport:pBag];
        
        return;
        
	}
	else if ([menuItem isEqualToString:@"Approval"])
	{
		pBag[@"COMING_FROM"] = @"APPROVAL";
		[self removeSubViews];
		
		ReportDetailViewController_iPad *newDetailViewController = [[ReportDetailViewController_iPad alloc] initWithNibName:@"ReportDetailViewController_iPad" bundle:nil];
		newDetailViewController.iPadHome = self;
		newDetailViewController.role = ROLE_EXPENSE_MANAGER;
        
		[homeViews removeObjectForKey:menuItem];
		homeViews[menuItem] = newDetailViewController;
		
		// Dismiss the popover if it's present.
		if (pickerPopOver != nil)
        {
			[pickerPopOver dismissPopoverAnimated:YES];
            pickerPopOver = nil;
        }
        
        [self.navigationController pushViewController:newDetailViewController animated:YES];
		
        [newDetailViewController loadReport:pBag];
		return;
	}
}


#pragma mark -
#pragma mark Managing the popover

- (void)showRootPopoverButtonItem:(UIBarButtonItem *)barButtonItem {
    // Add the popover button to the left navigation item.
    //[navigationBar.topItem setLeftBarButtonItem:barButtonItem animated:NO];
}


- (void)invalidateRootPopoverButtonItem:(UIBarButtonItem *)barButtonItem {
    // Remove the popover button.
    //[navigationBar.topItem setLeftBarButtonItem:nil animated:NO];
}


#pragma mark -
#pragma mark Content Views support

-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
	NSUInteger numTaps = [[touches anyObject] tapCount];
	UITouch * touch = [touches anyObject];
    
	CGPoint pt = [touch locationInView:self.currentTripSummaryView];
	if ((numTaps == 1) && CGRectContainsPoint([currentTripSummaryView frame], pt))
	{
		if (self.currentTrip != nil)
		{
			//MOB-10675
            EntityTrip* activeTrip = [[TripManager sharedInstance] fetchByTripKey:currentTrip.tripKey];
            if (activeTrip != nil) {
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:activeTrip, @"TRIP", nil];
                [self switchToDetail:@"Trip" ParameterBag:pBag];
            }
		}
	}
}

-(NSDictionary*)getCurrentAdContext
{
	NSString *imgName = nil;
	//SEL selector = nil;
    __autoreleasing NSMutableDictionary *adsLookup = [[NSMutableDictionary alloc] init];
    NSMutableArray *selectorArray = [[NSMutableArray alloc] init];
    //MOB-11909 - Added check for TripIt ad role
    if ([[ExSystem sharedInstance] hasRole:ROLE_TRIPITAD_USER])
    {
        //MOB-11145
        [selectorArray addObject:@"buttonTravelTextPressed:"];
    }
    
    NSMutableArray *images = [[NSMutableArray alloc] init];
	if ([ExSystem isLandscape])
	{
        
        // MOB-11145 - 
        if ([[ExSystem sharedInstance] hasRole:ROLE_TRIPITAD_USER])
        {
            [images addObject:@"traveltext_ad_ipad_landscape"];
        }
	}
	else
	{
        if ([[ExSystem sharedInstance] hasRole:ROLE_TRIPITAD_USER])
        {
            [images addObject:@"traveltext_ad_ipad_portrait"];
        }
	}
	
    if (selectorArray != nil && [selectorArray count] > adsIndex)
    {
        adsView.hidden = NO;
        imgName = images[(NSInteger)adsIndex];
        id selector = selectorArray[(NSInteger)adsIndex];
        adsLookup[@"selector"] = selector;
        adsLookup[@"image"] = imgName;
    }
    else
    {
        adsView.hidden = YES;
    }
	
	return (NSDictionary*)adsLookup;
}

- (IBAction)buttonTravelRequestPressed:(id)sender
{
    NSDictionary *dictionary = @{@"Action": @"Travel Request"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
	TravelRequestViewController *nextController = [[TravelRequestViewController alloc] initWithNibName:@"TRWebViewController" bundle:nil];
	
	if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
	
	UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:nextController];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];
}

- (IBAction)buttonTripItPressed:(id)sender
{
    NSDictionary *dictionary = @{@"Action": @"TripIt Launch"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    [AppsUtil launchTripItApp];
}

//MOB-11145
- (IBAction)buttonTravelTextPressed:(id)sender;
{
    [AppsUtil launchTravelTextApp];
}
-(void)switchAd:(NSTimer*)theTimer
{
	[self checkOffline];
	
	SEL selector;
	NSString * img = nil;
	
	NSDictionary *lookup = [self getCurrentAdContext];
	img = lookup[@"image"];
	selector = NSSelectorFromString(lookup[@"selector"]);
	
	if(img != nil && selector != nil)
	{
		[showAdBtn setImage:[UIImage imageNamed:img] forState:UIControlStateNormal];
		if(adsIndex == 0) // MOB-11145
            [showAdBtn removeTarget:self action: @selector(buttonTravelTextPressed:) forControlEvents:UIControlEventTouchUpInside];
		[showAdBtn addTarget:self action:selector forControlEvents:UIControlEventTouchUpInside];
	}
}

- (void)getOrderedSegments:(TripData *)trip
{
	NSMutableDictionary *segDictByDate = [[NSMutableDictionary alloc] init];
	NSMutableArray *holdKeys = [[NSMutableArray alloc] init];
	
	for(NSString *bookingId in trip.bookings)
	{
		BookingData *bd = (trip.bookings)[bookingId];
		for(NSString *x in bd.segments)
		{
			SegmentData *seg = (bd.segments)[x];
			
            if(seg != nil)
            {
                if (seg.startDateLocal == nil)
                {
                    seg.startDateLocal = @"1900-01-01 01:01";
                }
                
                NSString *formedDate = [DateTimeFormatter formatDateForTravel:seg.startDateLocal];
                if (segDictByDate[formedDate] == nil)
                {
                    NSMutableArray *ma = [[NSMutableArray alloc] initWithObjects:seg, nil];
                    segDictByDate[formedDate] = ma;
                    [holdKeys addObject:seg.startDateLocal];
                }
                else
                {
                    NSMutableArray *ma = segDictByDate[formedDate];
                    [ma addObject:seg];
                }
            }
		}
	}
	
	//now sort inside each day
	for(NSString *segDate in segDictByDate)
	{
		NSMutableArray *ma = segDictByDate[segDate];
		NSSortDescriptor *descriptor = [[NSSortDescriptor alloc] initWithKey:@"relStartLocation.dateLocal" ascending:YES];
		[ma sortUsingDescriptors:[NSMutableArray arrayWithObjects:descriptor,nil]];
	}
	
	//shove the date back into the header slot
	for(NSString *segDate in segDictByDate)
	{
		NSMutableArray *ma = segDictByDate[segDate];
		[ma insertObject:segDate atIndex:0];
	}
	
	NSArray *sortedKeys = [holdKeys sortedArrayUsingSelector:@selector(caseInsensitiveCompare:)];
	
	holdKeys = [[NSMutableArray alloc] init];
	
	for (int x = 0; x < [sortedKeys count]; x++)
	{
		NSString *sortedDate = sortedKeys[x];
		[holdKeys addObject:[DateTimeFormatter formatDateForTravel:sortedDate]];
	}
	
	self.tripBits = segDictByDate;
	self.keys = holdKeys;
	
	SegmentStuff *segStuff = [[SegmentStuff alloc] init];
	//Let's dump out all of the segments, with their rows, into an array that is then used to populate the table for an itin...
	
	dictSegRows = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	for(NSString *key in keys)
	{
		NSMutableArray *segs = tripBits[key];
		
		NSMutableArray *segmentDayRows = [[NSMutableArray alloc] initWithObjects:nil];
		int iPos = 0;
		for(EntitySegment *seg in segs)
		{
			if(iPos > 0)
			{
				NSMutableArray *segmentRows = nil;
				
				if([seg.type isEqualToString:@"AIR"])
					segmentRows = [segStuff fillAirSections:seg];
				else if([seg.type isEqualToString:@"HOTEL"])
					segmentRows = [segStuff fillHotelSections:seg];
				else if([seg.type isEqualToString:@"CAR"])
					segmentRows = [segStuff fillCarSections:seg];
				else if([seg.type isEqualToString:@"RIDE"])
					segmentRows = [segStuff fillRideSections:seg];
				else if([seg.type isEqualToString:@"DINING"])
					segmentRows = [segStuff fillDiningSections:seg];
				else if([seg.type isEqualToString:@"RAIL"])
					segmentRows = [segStuff fillRailSections:seg];
                
				
				for(SegmentRow *segRow in segmentRows)
					[segmentDayRows addObject:segRow];
			}
			iPos++;
		}
		dictSegRows[key] = segmentDayRows;
		
	}
	
}


-(void)prepareCurrentTripSummaryView:(id)activeTripDetails
{
	currentTripSummaryView.hidden = NO;
	lblTripSummary.text = @"";
	lblTripDated.text = @"";
	viewTripWait.hidden = YES;
	[viewTripWait removeFromSuperview];
	
	ivTripIcon.image = [UIImage imageNamed:@"trips_icon"];
	if (activeTripDetails != nil)
	{
		TripData *activeTrip = (TripData*)activeTripDetails;
		[self getOrderedSegments:activeTrip];
		NSString *startFormatted = [DateTimeFormatter formatDateEEEMMMdd:activeTrip.tripStartDateLocal];
		NSString *endFormatted = [DateTimeFormatter formatDateEEEMMMdd:activeTrip.tripEndDateLocal];
		
		lblTripSummary.text = [NSString stringWithFormat:@"%@", activeTrip.tripName];
		lblTripDated.text =  [NSString stringWithFormat:@"%@ - %@", startFormatted, endFormatted];
		[lblTripDated setFont:[UIFont fontWithName:@"Helvetica" size:12.0]];
        
	}
	else
	{
		//[tripCategoryIconsView removeAllSubviews];
		lblTripSummary.text = @"";
		// To keep the text centered, using the following label instead
		[lblTripDated setFont:[UIFont fontWithName:@"Helvetica" size:14.0]];
		lblTripDated.text = [Localizer getLocalizedText:@"No Active Trips"];
	}
}

-(void) addTripCategoryImage:(NSString*)category
{
	UILayoutView *lv = [[UILayoutView alloc] initWithFrame:CGRectMake(0, 0, 26, 26)];
	lv.paddingTop = 3.5;
	lv.paddingLeft = 3.5;
	UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 19, 19)];
	
	if ([category isEqualToString:@"AIR"])
	{
		iv.image = [UIImage imageNamed:@"trips_flight_sm.png"];
	}
	else if ([category isEqualToString:@"CAR"])
	{
		iv.image = [UIImage imageNamed:@"trip_car_sm.png"];
	}
	else if ([category isEqualToString:@"HOTEL"])
	{
		iv.image = [UIImage imageNamed:@"trip_hotel_sm.png"];
	}
	else if ([category isEqualToString:@"DINING"])
	{
		iv.image = [UIImage imageNamed:@"trip_dine_sm.png"];
	}
	else if ([category isEqualToString:@"RIDE"])
	{
		iv.image = [UIImage imageNamed:@"trip_taxi_sm.png"];
	}
	else if ([category isEqualToString:@"PARKING"])
	{
		iv.image = [UIImage imageNamed:@"trip_parking_sm.png"];
	}
	else if ([category isEqualToString:@"RAIL"])
	{
		iv.image = [UIImage imageNamed:@"trip_rail_sm.png"];
	}
	
	[lv addSubview:iv];
	[tripCategoryIconsView addSubview:lv];
	
}

-(IBAction)buttonCorpCardFromScrollerPressed:(id)sender
{
	[self checkOffline];
	
	NSDictionary *cardsData = nil;
    //	NSArray *cardKeys = nil;
    //	NSArray *allCardKeys = nil;
	//NSString *specialMessage = nil;
	
	if ([dataOOP count] > 1)
	{
		cardsData = dataOOP[1];
        //		allCardKeys = [cardsData allKeys];
        //		cardKeys = [dataOOP objectAtIndex:0];
	}
	
	if (cardsData[FILTER_CORP_CARDS] != nil)
	{
		oopeFilterListVC = [[OutOfPocketListViewController alloc] initWithNibName:@"OutOfPocketListViewController" bundle:nil];
		oopeFilterListVC.padHomeVC = self;
		[oopeFilterListVC loadExpenses];
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW",
									 FILTER_CORP_CARDS, @"FILTER", nil];
		
		[[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:oopeFilterListVC];
        
		//UIButton *btn = (UIButton *)sender;
        //		float y = 660.0;
        //		if(![ExSystem isLandscape])
        //		{
        //			float screenH = 1004.0;
        //			//y = screenH - 90.0;
        //		}
		//CGRect myRect = [self.view convertRect:btn.frame fromView:expSummaryScroller];
		UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:oopeFilterListVC];
		
		localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
		
		[localNavigationController setToolbarHidden:NO];
		//R 162 , G 160, B 160
		localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
		localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
		
		[self presentViewController:localNavigationController animated:YES completion:nil];
		
	}
}

-(IBAction)buttonPersonalCardFromScrollerPressed:(id)sender
{
	[self checkOffline];
	
	CardButton *btn = (CardButton*)sender;
	NSString *aKey = btn.cardKey;
	
	NSDictionary *cardsData = nil;
    //	NSArray *cardKeys = nil;
    //	NSArray *allCardKeys = nil;
	id rptRowData = nil;
	
	if (aKey == nil)
	{
		return;
	}
	
	if ([dataOOP count] > 1)
	{
		cardsData = dataOOP[1];
        //		allCardKeys = [cardsData allKeys];
        //		cardKeys = [dataOOP objectAtIndex:0];
		rptRowData = cardsData[aKey];
	}
	
	if (rptRowData != nil && [rptRowData isKindOfClass:[PersonalCardData class]])
	{
		//PersonalCardData* pcData = (PersonalCardData*) rptRowData;
		
		oopeFilterListVC = [[OutOfPocketListViewController alloc] initWithNibName:@"OutOfPocketListViewController" bundle:nil];
		oopeFilterListVC.padHomeVC = self;
		[oopeFilterListVC loadExpenses];
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW",
									 aKey, @"FILTER", nil];
        
		[[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:oopeFilterListVC];
		
		//UIButton *btn = (UIButton *)sender;
		//float y = 660.0;
		if(![ExSystem isLandscape])
		{
			//float screenH = 1004.0;
			//y = screenH - 90.0;
		}
        
		//CGRect myRect = [self.view convertRect:btn.frame fromView:expSummaryScroller];
		
		UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:oopeFilterListVC];
		localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
		
		[localNavigationController setToolbarHidden:NO];
		//R 162 , G 160, B 160
		localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
		localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
		
		[self presentViewController:localNavigationController animated:YES completion:nil];
		
		return;
	}
	
}

-(IBAction)launchConcurBreezeURL:(id)sender
{
	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"http://%@", @"www.concurbreeze.com"]]];
}

-(void)processCardsInfo
{
	NSDictionary *cardsData = nil;
	NSArray *cardKeys = nil;
	//NSArray *allCardKeys = nil;
	NSString *specialMessage = nil;
	SEL selector = nil;
	//NSString *corpCardTransactionCount = [summaryData.dict objectForKey:@"CorporateCardTransactionCount"];
	
	int cardCount; // = [corpCardTransactionCount intValue];
	//NSString *cards = [NSString stringWithFormat:@"%d unused card charges", cardCount];
	
	[expSummaryScroller removeAllSubviews];
	
	// process card here before rendering images
	if ([dataOOP count] > 1)
	{
		cardsData = dataOOP[1];
		cardKeys = dataOOP[0];
	}
	
	for(NSString *aKey in cardKeys)
	{
		if([aKey isEqualToString:@"NOCARDS"])
		{
			if ([[ExSystem sharedInstance]isBreeze])
			{
				[addFuncScroller removeAllSubviews];
				selector = @selector(buttonAddPressed:);
				[self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_expense_ipad" andTitle:[Localizer getLocalizedText:@"Quick Expense"] andSelector:selector andPCAKey:nil];
				selector = @selector(showPersonalCarMileage:);
                selector = @selector(launchConcurBreezeURL:);
				[self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_setup_card" andTitle:[Localizer getLocalizedText:@"Add Card"] andSelector:selector andPCAKey:nil];
			}
			else
			{
				specialMessage = cardsData[@"NOCARDS"];
				selector = nil;
				
				[self renderDynamicButtonsForView:@"EXPENSE_SUMMARY" withImage:@"summery_card_charges" andTitle:specialMessage andSelector:selector andPCAKey:nil];
			}
            
		}
		else if([aKey isEqualToString:@"CORP_CARDS"])
		{
			selector = @selector(buttonCorpCardFromScrollerPressed:);
			specialMessage = cardsData[@"CORP_CARDS"];
			cardCount = [specialMessage intValue];
			//specialMessage = [specialMessage stringByAppendingString:@" unused corporate card charges"];
			
			if(cardCount == 0)
				specialMessage = [Localizer getLocalizedText:@"CORP_CARDS_0"];
			else if (cardCount == 1)
				specialMessage = [Localizer getLocalizedText:@"CORP_CARDS_1"];
			else
				specialMessage = [NSString stringWithFormat:[Localizer getLocalizedText:@"CORP_CARDS_2"], cardCount];
            
			[self renderDynamicButtonsForView:@"EXPENSE_SUMMARY" withImage:@"summery_card_charges" andTitle:specialMessage andSelector:selector andPCAKey:nil];
		}
		else
		{
			selector = @selector(buttonPersonalCardFromScrollerPressed:);
			id cData = cardsData[aKey];//@"UnsubmittedReportsMessage"];
			
			if ([cData isKindOfClass:[PersonalCardData class]])
			{
				PersonalCardData* pcData = (PersonalCardData*) cData;
				if (pcData != nil)
				{
//					specialMessage = [[ConcurMobileAppDelegate findRootViewController] getPersonalCardCountMessage:pcData];
				}
				
				[self renderDynamicButtonsForView:@"EXPENSE_SUMMARY" withImage:@"summery_card_charges" andTitle:specialMessage andSelector:selector  andPCAKey:aKey];
			}
		}
	}
}


-(void)prepareExpSummaryView
{//pjk:  this code is constructed quite oddly.  The if else if block should work wiht thif block below it
	
	NSString *reportsToApproveCount = (summaryData.dict)[@"ReportsToApproveCount"];
	NSString *unsubmittedReportsCount = (summaryData.dict)[@"UnsubmittedReportsCount"];
	NSString *tripsToApprove = (summaryData.dict)[@"TravelRequestApprovalCount"];
    
	int reportCount = [unsubmittedReportsCount intValue];
	int approvalCount = [reportsToApproveCount intValue];
	
	//NSString *rcBase = [Localizer getLocalizedText:@"UnsubmittedReportsRow_0"];
	//NSString *reports = [NSString stringWithFormat:rcBase, reportCount];
	//NSString *approvals = [NSString stringWithFormat:@"%d approval reports to approve", approvalCount];
	
    //MOB-9729 fix for wrong text disply with wrong icon.
    if ([[ExSystem sharedInstance] siteSettingAllowsExpenseReports]) //([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
    {
        NSString *btnText = @"";
        if(reportCount == 1)
			btnText = [Localizer getLocalizedText:@"UnsubmittedReportsRow_1"];
		else if(reportCount > 1)
			btnText = [NSString stringWithFormat:[Localizer getLocalizedText:@"UnsubmittedReportsRow_2"], reportCount];
		else
			btnText = [Localizer getLocalizedText:@"UnsubmittedReportsRow_0"];
        
        SEL selector = @selector(buttonReportsFromScrollerPressed:);
		[self renderDynamicButtonsForView:@"EXPENSE_SUMMARY" withImage:@"summery_reports" andTitle:btnText andSelector:selector andPCAKey:nil];
    }
    
	if ([[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals] && [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER])
	{
		NSString *btnText = @"";
		if(approvalCount == 1)
			btnText = [Localizer getLocalizedText:@"ReportsToApproveRow_1"];
		else if(approvalCount > 1)
			btnText = [NSString stringWithFormat:[Localizer getLocalizedText:@"ReportsToApproveRow_2"], approvalCount];
		else
			btnText = [Localizer getLocalizedText:@"ReportsToApproveRow_0"];
		
		SEL selector = @selector(buttonApprovalsFromScrollerPressed:);
		[self renderDynamicButtonsForView:@"EXPENSE_SUMMARY" withImage:@"summery_approval" andTitle:btnText andSelector:selector andPCAKey:nil];
	}
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER])
    {
        NSString *btnText = @"";
        int travelRequestCount = ![tripsToApprove length]? 0: [tripsToApprove intValue];
		if( travelRequestCount == 1)
			btnText = [Localizer getLocalizedText:@"TravelRequestsToApproveRow_1"];
		else if(travelRequestCount > 1)
			btnText = [NSString stringWithFormat:[Localizer getLocalizedText:@"TravelRequestsToApproveRow_2"], travelRequestCount];
		else
			btnText = [Localizer getLocalizedText:@"TravelRequestsToApproveRow_0"];
		
		SEL selector = @selector(buttonTravelRequestPressed:);
		[self renderDynamicButtonsForView:@"EXPENSE_SUMMARY" withImage:@"summery_travel_request" andTitle:btnText andSelector:selector andPCAKey:nil];
    }
}


- (CGRect) adjustScrollerView:(UIScrollView*)scroller leftArrow:(UIImageView*)ivLeftArrow rightArrow:(UIImageView*)ivRightArrow buttonWidth: (const int) buttonWidth buttonHeight:(const int) buttonHeight offset: (int) offset padding: (int) padding
{
	int count;
	CGRect frame;
	count = (int)[[scroller subviews] count];
	frame = CGRectMake(padding + (offset+buttonWidth)*count, 0, buttonWidth, buttonHeight);
    
	if ((count + 1) > 3) //we are inserting the 4th and beyond, so we need to see if there are 3 already in there.
	{
		scroller.scrollEnabled = YES;
		[scroller setContentSize:CGSizeMake(padding + (offset+buttonWidth)*(count+1) - offset + padding, scroller.frame.size.height)];
		[scroller setPagingEnabled:YES];
        
		if (ivLeftArrow != nil)
		{
			[ivLeftArrow setImage:[UIImage imageNamed:@"scroll_arrow_left"]];
		}
		if (ivRightArrow != nil)
		{
			[ivRightArrow setImage:[UIImage imageNamed:@"scroll_arrow_right"]];
		}
	}
	else
	{
		if (ivLeftArrow != nil) {
			ivLeftArrow.image = nil;
		}
		if (ivRightArrow != nil) {
			ivRightArrow.image = nil;
		}
		scroller.scrollEnabled = NO;
		[scroller setContentSize:CGSizeMake(frame.origin.x + frame.size.width, scroller.frame.size.height)];
	}
    return frame;
}


-(void)renderDynamicButtonsForView:(NSString*)parent withImage:(NSString*)img andTitle:(NSString*)name andSelector:(SEL)selector andPCAKey:(NSString*)personalCardKey
{
	CGRect frame = CGRectMake(0, 0, 0, 0);
	CGRect lblFrame = CGRectMake(0, 0, 0, 0);
	int offset = 0;
	const int buttonWidth = 144.0;
	int padding  = 0.0;
	
	if([ExSystem isLandscape])
	{
		offset = 76.0;
		padding  = 52.0;
	}
	else
	{
		offset = 4.0;
		padding  = 6.0;
	}
	
	//todo: The adjustScrollerView method is really just intended to set the frame for the button.  We should probably rename the current method and make it only get the frame.
	// after doing this, then adjust the scroller contentsize at the end of this method.
	if ([parent isEqualToString:@"ACTION_BUTTONS"])
	{
		frame = [self adjustScrollerView:addFuncScroller
							   leftArrow:ivActionButtonsLeftArrow
							  rightArrow:ivActionButtonsRightArrow
							 buttonWidth:buttonWidth
							buttonHeight:60
								  offset:offset
								 padding:padding];
		if([img isEqualToString:@"button_add_mileage.png"])
			lblFrame = CGRectMake(55,0, 85, 60);
		else
			lblFrame = CGRectMake(60,10, 85, 40);
	}
	else if ([parent isEqualToString:@"EXPENSE_SUMMARY"])
	{
		frame = [self adjustScrollerView:expSummaryScroller
							   leftArrow:ivExpSummaryButtonsLeftArrow
							  rightArrow:ivExpSummaryButtonsRightArrow
							 buttonWidth:buttonWidth
							buttonHeight:150
								  offset:offset
								 padding:padding];
        
		lblFrame = CGRectMake(5,100, 128, 50);
	}
	
	UIButton * btn = nil;
	CardButton *cBtn = nil;
	
	if (personalCardKey != nil)
	{
		cBtn = [[CardButton alloc] initWithFrame:frame];
		cBtn.cardKey = personalCardKey;
		[cBtn setImage:[UIImage imageNamed:img] forState:UIControlStateNormal];
		[cBtn addTarget:self action:selector forControlEvents:UIControlEventTouchUpInside];
	}
	else
	{
		btn = [[UIButton alloc] initWithFrame:frame];
		[btn setImage:[UIImage imageNamed:img] forState:UIControlStateNormal];
		[btn addTarget:self action:selector forControlEvents:UIControlEventTouchUpInside];
	}
	
	if ([parent isEqualToString:@"EXPENSE_SUMMARY"])
	{
		NSString *cardCountDetail = name;
		NSArray *split = [name componentsSeparatedByString:@"from"];
		if ([split count] > 1)
		{
			cardCountDetail = split[0];
			UILabel *cardName = nil;
			
			if (cBtn != nil && personalCardKey != nil)
			{
				cardName =  [[UILabel alloc] initWithFrame:CGRectMake(2, 0, cBtn.frame.size.width-4, 35)];
			}
			else
			{
				cardName =  [[UILabel alloc] initWithFrame:CGRectMake(2, 0, btn.frame.size.width-4, 35)];
			}
            
			[cardName setNumberOfLines:2];
			[cardName setLineBreakMode:NSLineBreakByTruncatingTail];
			[cardName setFont:[UIFont fontWithName:@"Helvetica" size:13.0]];
			[cardName setBackgroundColor:[UIColor clearColor]];
			[cardName setTextColor:[UIColor blackColor]];
			[cardName setHighlighted:YES];
			[cardName setText:split[1]];
			[cardName setTextAlignment:NSTextAlignmentCenter];
			
			if (personalCardKey != nil)
			{
				[cBtn addSubview:cardName];
			}
			else
			{
				[btn addSubview:cardName];
			}
            
		}
        
		UILabel *lbl = [[UILabel alloc] initWithFrame:lblFrame];
		[lbl setNumberOfLines:3];
		[lbl setLineBreakMode:NSLineBreakByWordWrapping];
		[lbl setFont:[UIFont fontWithName:@"Helvetica" size:12.0]];
		[lbl setBackgroundColor:[UIColor clearColor]];
		[lbl setTextColor:[UIColor blackColor]];
		[lbl setHighlighted:YES];
		[lbl setText:cardCountDetail];
		[lbl setTextAlignment:NSTextAlignmentCenter];
		
		if (personalCardKey != nil)
		{
			[cBtn addSubview:lbl];
		}
		else
		{
			[btn addSubview:lbl];
		}
        
	}
	
	if ([parent isEqualToString:@"ACTION_BUTTONS"])
	{
		UILabel *lbl = [[UILabel alloc] initWithFrame:lblFrame];
		
		[lbl setNumberOfLines:3];
		[lbl setLineBreakMode:NSLineBreakByWordWrapping];
		[lbl setFont:[UIFont fontWithName:@"Helvetica" size:12.0]];
		[lbl setBackgroundColor:[UIColor clearColor]];
		[lbl setTextColor:[UIColor blackColor]];
		[lbl setHighlighted:YES];
		
		name = [name stringByReplacingOccurrencesOfString:@" " withString:@"\n"];
		[lbl setText:name];
		[lbl setTextAlignment:NSTextAlignmentCenter];
		if([img isEqualToString:@"button_add_mileage.png"])
		{
			[lbl setLineBreakMode:NSLineBreakByTruncatingMiddle];
			//[lbl setTextAlignment:NSTextAlignmentRight];
		}
		[btn addSubview:lbl];
        
		[addFuncScroller addSubview:btn];
	}
	else if ([parent isEqualToString:@"EXPENSE_SUMMARY"])
	{
		if (personalCardKey != nil)
		{
			[expSummaryScroller addSubview:cBtn];
		}
		else
		{
			[expSummaryScroller addSubview:btn];
		}
	}
	
	
}


-(void)renderActionButtons
{
	[addFuncScroller removeAllSubviews];
	SEL selector;
    bool enableCarMileage = ![@"N" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"PersonalCarMileageOnHome" withType:@"Mobile"]];
    enableCarMileage &= [[ExSystem sharedInstance] siteSettingAllowsExpenseReports];
    //MOB-7247
    bool enableLocateAndAlert = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"LocateAndAlert" withType:@"OTMODULE"]];
    if (enableLocateAndAlert)
    {
        // MOB-7502 check LNA_User role
        enableLocateAndAlert = [[ExSystem sharedInstance] hasRole:ROLE_LNA_USER];
    }
    
    // add for gov user
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
		selector = @selector(buttonAddPressed:);
		[self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_expense_ipad" andTitle:[Localizer getLocalizedText:@"Quick Expense"] andSelector:selector andPCAKey:nil];
        
        if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER])
        {
            selector = @selector(buttonAirPressed:);
            [self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_air" andTitle:[Localizer getLocalizedText:@"Book Air"] andSelector:selector andPCAKey:nil];
        }
        
        if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER])
        {
            selector = @selector(buttonHotelPressed:);
            [self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_hotel" andTitle:[Localizer getLocalizedText:@"Book Hotel"] andSelector:selector andPCAKey:nil];
            selector = @selector(buttonCarPressed:);
            [self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_car" andTitle:[Localizer getLocalizedText:@"Book Car"] andSelector:selector andPCAKey:nil];
        }
    }
	else if	(isExpenseOnly)
	{
		selector = @selector(buttonAddPressed:);
		[self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_expense_ipad" andTitle:[Localizer getLocalizedText:@"Quick Expense"] andSelector:selector andPCAKey:nil];
		
		selector = @selector(showPersonalCarMileage:);
//		if(enableCarMileage && [carRatesData hasAnyPersonalsWithRates:[ExSystem sharedInstance].sys.crnCode])
//			[self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_mileage" andTitle:[Localizer getLocalizedText:@"Personal Car Mileage"] andSelector:selector andPCAKey:nil];
        
        if (!hideReceiptStore)
        {
            selector = @selector(btnUploadReceiptPressed:);
            [self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_receipt_store" andTitle:[Localizer getLocalizedText:@"Upload Receipt"] andSelector:selector andPCAKey:nil];
        }
	}
	else if(isTravelOnly && !isItinViewerOnly && [[ExSystem sharedInstance] siteSettingAllowsTravelBooking])
	{
        selector = @selector(buttonAirPressed:);
        [self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_air" andTitle:[Localizer getLocalizedText:@"Book Air"] andSelector:selector andPCAKey:nil];
		selector = @selector(buttonHotelPressed:);
		[self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_hotel" andTitle:[Localizer getLocalizedText:@"Book Hotel"] andSelector:selector andPCAKey:nil];
		selector = @selector(buttonCarPressed:);
		[self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_car" andTitle:[Localizer getLocalizedText:@"Book Car"] andSelector:selector andPCAKey:nil];
	}
	else if(!isItinViewerOnly)
	{
        if ([[ExSystem sharedInstance] siteSettingAllowsTravelBooking])
        {
            selector = @selector(buttonAirPressed:);
            [self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_air" andTitle:[Localizer getLocalizedText:@"Book Air"] andSelector:selector andPCAKey:nil];
        }
        
		selector = @selector(buttonAddPressed:);
		[self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_expense_ipad" andTitle:[Localizer getLocalizedText:@"Quick Expense"] andSelector:selector andPCAKey:nil];
        
        if (!hideReceiptStore)
        {
            selector = @selector(btnUploadReceiptPressed:);
            [self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_receipt_store" andTitle:[Localizer getLocalizedText:@"Upload Receipt"] andSelector:selector andPCAKey:nil];
		}
        
		selector = @selector(showPersonalCarMileage:);
//		if(enableCarMileage && [carRatesData hasAnyPersonalsWithRates:[ExSystem sharedInstance].sys.crnCode])
//			[self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_mileage" andTitle:[Localizer getLocalizedText:@"Personal Car Mileage"] andSelector:selector andPCAKey:nil];
        
        if ([[ExSystem sharedInstance] siteSettingAllowsTravelBooking])
        {
            selector = @selector(buttonHotelPressed:);
            [self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_hotel" andTitle:[Localizer getLocalizedText:@"Book Hotel"] andSelector:selector andPCAKey:nil];
            selector = @selector(buttonCarPressed:);
            [self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_add_car" andTitle:[Localizer getLocalizedText:@"Book Car"] andSelector:selector andPCAKey:nil];
        }
	}
 	//MOB-7247
    if (enableLocateAndAlert)
    {
        selector = @selector(buttonLocationCheckPressed:);
		[self renderDynamicButtonsForView:@"ACTION_BUTTONS" withImage:@"button_location_checkin" andTitle:[Localizer getLocalizedText:@"Safety Check In"] andSelector:selector andPCAKey:nil];
    }
    
}

-(void)showViewsByUserTypes
{
    if (isTravelOnly && [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER])
    {
        self.addFunctionsView.hidden = NO;
        self.separatorView.hidden = YES;
        self.expSummaryView.hidden = NO;
    }
	else if (isTravelOnly)
	{
		addFunctionsView.hidden = NO;
		expSummaryView.hidden = YES;
		currentTripSummaryView.hidden = NO;
        
	}
	else if(isItinViewerOnly)
	{
		addFunctionsView.hidden = YES;
		expSummaryView.hidden = YES;
		currentTripSummaryView.hidden = NO;
	}
	else
	{
		addFunctionsView.hidden = NO;
		expSummaryView.hidden = NO;
		if (isExpenseOnly)
		{
			currentTripSummaryView.hidden = YES;
		}
		else
		{
			currentTripSummaryView.hidden = NO;
		}
	}
	
	if (!currentTripSummaryView.hidden)
	{
		viewTripWait.hidden = NO;
		[currentTripSummaryView addSubview:viewTripWait];
	}
}

-(void)refreshHeader
{
	NSMutableDictionary *skins = dictSkins[@"default"];
	NSString *imgName = nil;
    
	if ([ExSystem isLandscape])
	{
        
        headerImgView.frame = CGRectMake(10, 11, 1007, 50);
        
        imgName = skins[@"header"];
	}
	else
	{
        headerImgView.frame = CGRectMake(9, 11, 750, 48);
        
        imgName = skins[@"header_portrait"];
	}
	
	headerImgView.image = [UIImage imageNamed:imgName];
}



#pragma mark -
#pragma mark Rotation support
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {

    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

- (NSUInteger)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskAll;
}

// Tell the system It should autorotate
- (BOOL) shouldAutorotate {
    return YES;
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation duration:(NSTimeInterval)duration
{
    if([ExSystem isLandscape] && viewPadIntro != nil)
    {
        viewPadIntro.iv2.image = nil;
        viewPadIntro.iv.image = [UIImage imageNamed:@"Default-Landscape~ipad"];
    }
    else if (viewPadIntro != nil) {
        //            viewPadIntro.iv2.frame = CGRectMake(151, 324, 467, 84);
        viewPadIntro.iv2.image = nil;
        viewPadIntro.iv.image = [UIImage imageNamed:@"Default-Portrait~ipad"];
    }
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{    
    if ([ExSystem isLandscape])
    {
        [self adjustForLandscape];
    }
    else
    {
        [self adjustForPortrait];
    }
        
    if([homeViews count] > 0)
    {
        for(NSString *key in homeViews)
        {
            MobileViewController *mvc = homeViews[key];
            [mvc willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
        }
    }
        
    if((toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight)  && showWhatsNew) {
            [self adjustWhatsNewLandscape];
    }
    else {
        [self adjustWhatsNewPortrait];
    }
}

-(void) checkOffline
{
    if(![ExSystem connectedToNetwork])
	{
        [self refreshSkinForOffline];
	}
    else
        [self refreshSkin];
}


-(void)suAdjustForPortrait
{
    NSMutableDictionary *skin = dictSkins[@"default"];
    ivBackground.image = [UIImage imageNamed:skin[@"ivBackground_port"]];
    ivBackground.frame = CGRectMake(0, 0, 768, 1004);
    
    [self refreshHeader];
    
    [bkQuickActionPanel setImage:[UIImage imageNamed:skin[@"QuickActionPanelBackground_port"]]];
    [bkMenuPanel setImage:[UIImage imageNamed:skin[@"MenuPanelBackground_port"]]];
    [bkContentPanel setImage:[UIImage imageNamed:skin[@"ContentPanelBackground_port"]]];
    
    mainPanel.frame = CGRectMake(12, mainPanel.frame.origin.y, 731, mainPanel.frame.size.height);
    addFuncScroller.frame = CGRectMake(11, bkQuickActionPanel.frame.origin.y, 732, bkQuickActionPanel.frame.size.height);
    contentPanel.frame = CGRectMake(298, contentPanel.frame.origin.y, 435, 602);
    btnUploadReceipt.frame = CGRectMake(13, 12, 154, 62);
    btnAddExpense.frame = CGRectMake(197, 12, 154, 62);
    btnAddMileage.frame = CGRectMake(381, 12, 154, 62);
    btnCreateReport.frame = CGRectMake(565, 12, 154, 62);
    contentTableContainer.frame = CGRectMake(3, 69, 429, 505);
    
    if(btnLogout != nil)
        [btnLogout removeFromSuperview];
    self.btnLogout = [ExSystem makeColoredButtonRegular:@"DARK_BLUE" W:80.0 H:30.0 Text:[Localizer getLocalizedText:@"Logout"] SelectorString:@"buttonLogoutPressed:" MobileVC:self];
    [btnLogout setFrame:CGRectMake(645, 7, 83, 32)];
    [headerPanel addSubview:btnLogout];
    
    NSDictionary *lookup = [self getCurrentAdContext];
    [showAdBtn setImage:[UIImage imageNamed:lookup[@"image"]] forState:UIControlStateNormal];
    
    if(self.viewNew != nil)
    {
        //        self.viewNew.frame = CGRectMake(0, 0, 768, 1024);
        //        [self.view bringSubviewToFront:viewNew];
    }
}

-(void)suAdjustForLandscape
{
    NSMutableDictionary *skin = dictSkins[@"default"];
    ivBackground.image = [UIImage imageNamed:skin[@"ivBackground"]];
    ivBackground.frame = CGRectMake(0, 0, 1024, 748);
    
    [self refreshHeader];
    
    [bkQuickActionPanel setImage:[UIImage imageNamed:skin[@"QuickActionPanelBackground"]]];
    [bkMenuPanel setImage:[UIImage imageNamed:skin[@"MenuPanelBackground"]]];
    [bkContentPanel setImage:[UIImage imageNamed:skin[@"ContentPanelBackground"]]];
    mainPanel.frame = CGRectMake(51, mainPanel.frame.origin.y, 905, mainPanel.frame.size.height);
    addFuncScroller.frame = bkQuickActionPanel.frame;
    contentPanel.frame = CGRectMake(311, contentPanel.frame.origin.y, 595, 404);
    btnUploadReceipt.frame = CGRectMake(58, 12, 154, 62);
    btnAddExpense.frame = CGRectMake(270, 12, 154, 62);
    btnAddMileage.frame = CGRectMake(482, 12, 154, 62);
    btnCreateReport.frame = CGRectMake(694, 12, 154, 62);
    contentTableContainer.frame = CGRectMake(3, 62, 589, 327);
    
    if(btnLogout != nil)
        [btnLogout removeFromSuperview];
    self.btnLogout = [ExSystem makeColoredButtonRegular:@"DARK_BLUE" W:80.0 H:30.0 Text:[Localizer getLocalizedText:@"Logout"] SelectorString:@"buttonLogoutPressed:" MobileVC:self];
    [btnLogout setFrame:CGRectMake(902, 7, 83, 32)];
    [headerPanel addSubview:btnLogout];
    
    NSDictionary *lookup = [self getCurrentAdContext];
    [showAdBtn setImage:[UIImage imageNamed:lookup[@"image"]] forState:UIControlStateNormal];
    
    if(self.viewNew != nil)
    {
        //        self.viewNew.frame = CGRectMake(0, 0, 1024, 768);
        //        [self.view bringSubviewToFront:viewNew];
    }
}

-(void) adjustForPortrait
{
	float w = 768.0;
	float h = 1004.0;
	
	[self checkOffline];
	
    NSMutableDictionary *skin = dictSkins[@"default"];
    ivBackground.image = [UIImage imageNamed:skin[@"ivBackground_port"]];
    ivBackground.frame = CGRectMake(0, 0, w, h);
        
    [self refreshHeader];
    //The center of the offline text in portrait
    [lblOffline setCenter:CGPointMake(384.000000, 50.500000)];
    canvas.frame = CGRectMake(11, 56, 750, 761);
    contentPanel.frame = CGRectMake(contentPanel.frame.origin.x, contentPanel.frame.origin.y, contentPanel.frame.size.width, 700);
    h2.marginLeft = 8;
    currentTripSummaryView.paddingLeft = 25.0;
    currentTripSummaryView.paddingRight = 25.0;
    [currentTripSummaryView setBackgroundImg:@"trips_panel_portrait"];
    [ivTripWait setImage:[UIImage imageNamed:@"trips_panel_portrait_loading"]];
        
    [addFunctionsView setBackgroundImg:@"action_buttons_panel_portrait"];
    [expSummaryView setBackgroundImg:@"expense_panel_portrait"];
    h2.frame = CGRectMake(0, 0, h2.frame.size.width, 702);
        
    NSDictionary *lookup = [self getCurrentAdContext];
    [showAdBtn setImage:[UIImage imageNamed:lookup[@"image"]] forState:UIControlStateNormal];
    [self renderActionButtons];
        
    if (!(isTravelOnly || isItinViewerOnly) && ![[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
        [self processCardsInfo];
        [self prepareExpSummaryView];
    }
    else
    {
        if (![[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
        {
            [ivTravelUserPromoBanner setImage:[UIImage imageNamed:@"travel_marketing_ad_portrait"]];
                [separatorView setBackgroundImg:@"expense_panel_portrait"];
        }
    }
        
    [canvas relayout];
        
    if(btnLogout != nil) {
            [btnLogout removeFromSuperview];
    }
    self.btnLogout = [ExSystem makeColoredButtonRegular:@"DARK_BLUE" W:120.0 H:32.0 Text:[Localizer getLocalizedText:@"Logout"] SelectorString:@"buttonLogoutPressed:" MobileVC:self];
    btnLogout.frame = CGRectMake(w - 140, 70, 120, 30);
    [self.view addSubview:btnLogout];
        
    if(btnSettings != nil){
        [btnSettings removeFromSuperview];
    }
    self.btnSettings = [ExSystem makeColoredButtonRegular:@"DARK_BLUE" W:120.0 H:32.0 Text:[Localizer getLocalizedText:@"Settings"] SelectorString:@"buttonSettingsPressed:" MobileVC:self];
    btnSettings.frame = CGRectMake(w - 270, 70, 120, 30);
    [self.view addSubview:btnSettings];
        
    [self adjustHomeButtons:self];
    
	[self.navigationController.navigationBar setHidden:YES];
}

-(void) adjustForLandscape
{
	[self checkOffline];
    
    float w = 1024.0;
    float h = 768.0;
        
    NSMutableDictionary *skin = dictSkins[@"default"];
    [self refreshHeader];
    //The center of the offline text in portrait
    [lblOffline setCenter:CGPointMake(511.500000, 50.500000)];
    canvas.frame = CGRectMake(13, 61, 1004, 549);
    contentPanel.frame = CGRectMake(contentPanel.frame.origin.x, contentPanel.frame.origin.y, contentPanel.frame.size.width, 494);
    h2.marginLeft = 25;
    currentTripSummaryView.paddingLeft = 70.0;
    currentTripSummaryView.paddingRight = 70.0;
        
    [ivTripWait setImage:[UIImage imageNamed:@"trips_panel_loading"]];
    [currentTripSummaryView setBackgroundImg:@"trips_panel"];
    [addFunctionsView setBackgroundImg:@"action_buttons_panel"];
    [expSummaryView setBackgroundImg:@"expense_panel"];

    NSDictionary *lookup = [self getCurrentAdContext];
    [showAdBtn setImage:[UIImage imageNamed:lookup[@"image"]] forState:UIControlStateNormal];
        
    [self renderActionButtons];
        
    if (!(isTravelOnly || isItinViewerOnly) && ![[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
        [self processCardsInfo];
        [self prepareExpSummaryView];
    }
    else
    {
        if (![[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
        {
            [ivTravelUserPromoBanner setImage:[UIImage imageNamed:@"travel_marketing_ad"]];
            [separatorView setBackgroundImg:@"expense_panel"];
        }
    }
        
    [canvas relayout];
    
    ivBackground.image = [UIImage imageNamed:skin[@"ivBackground"]];
    ivBackground.frame = CGRectMake(0, 0, w, h);
    
    float btnW = 100.0;
    if(btnLogout != nil)
        [btnLogout removeFromSuperview];
    self.btnLogout = [ExSystem makeColoredButtonRegular:@"DARK_BLUE" W:btnW H:32.0 Text:[Localizer getLocalizedText:@"Logout"] SelectorString:@"buttonLogoutPressed:" MobileVC:self];
    btnLogout.frame = CGRectMake(w - (btnW + 50)+30, 70, btnW, 30);
    [self.view addSubview:btnLogout];
    
    if(btnSettings != nil)
        [btnSettings removeFromSuperview];
    self.btnSettings = [ExSystem makeColoredButtonRegular:@"DARK_BLUE" W:btnW H:32.0 Text:[Localizer getLocalizedText:@"Settings"] SelectorString:@"buttonSettingsPressed:" MobileVC:self];
    btnSettings.frame = CGRectMake(w - (btnW + 160)+30, 70, btnW, 30);
    [self.view addSubview:btnSettings];
    
    [self adjustHomeButtons:self];
    
	[self.navigationController.navigationBar setHidden:YES];
}


#pragma mark -
#pragma mark Skins
-(void) loadSkins
{
	//default
	NSMutableDictionary *skin = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    
    skin[@"ivBackground"] = @"background_landscape";
    skin[@"ivBackground_port"] = @"background_portrait";
    skin[@"ivBarLeft"] = @"nav_bar_left";
    skin[@"ivBarMiddle"] = @"nav_bar_middle";
    skin[@"ivBarRight"] = @"nav_bar_right";
    skin[@"ivHotelBackground"] = @"trip_infomation_panel";
    skin[@"ivHotelBackground_port"] = @"portrait_location_panel";
    skin[@"ivLocationIcon"] = @"iPad_Btn_Trips";
    skin[@"ivLocation"] = @"pic_frame";
    skin[@"ivExpenseBackground"] = @"expense_book";
    skin[@"ivTripInfo"] = @"trip_ticket";
    skin[@"header"] = @"concur_header_landscape";
    skin[@"header_portrait"] = @"concur_header_portrait";
    
    //home buttons
    skin[@"HomeBtnApprovals"] = @"hoo_nav_approval";
    skin[@"HomeBtnCar"] = @"nav_car";
    skin[@"HomeBtnDining"] = @"hoo_nav_dine";
    skin[@"HomeBtnExpense"] = @"nav_expense";
    skin[@"HomeBtnHome"] = @"hoo_nav_home";
    skin[@"HomeBtnAttendee"] = @"hoo_nav_attendee";
    skin[@"HomeBtnHotel"] = @"nav_hotel";
    skin[@"HomeBtnReports"] = @"nav_reports";
    skin[@"HomeBtnReceipts"] = @"nav_receipts";
    skin[@"HomeBtnTaxi"] = @"nav_taxi";
    skin[@"HomeBtnTrips"] = @"hoo_nav_trips";
    skin[@"HomeBtnTravelRequests"] = @"nav_travel_request_approval";
    skin[@"HomeTextColor"] = [UIColor whiteColor];
    
	dictSkins[@"default"] = skin;
}


-(void) updateBackgroundsForExpenseOnlyUsers
{
	imageExpenseWait.image = nil;
    
    if ([ExSystem isLandscape]) {
        [self setExpenseOnlyBackgroundLandscape];
    }
    else {
        [self setExpenseOnlyBackgroundPortrait];
    }
}


-(void)setExpenseOnlyBackgroundLandscape
{
	NSMutableDictionary *skins = dictSkins[@"default"];
	skins[@"ivExpenseBackground"] = @"expenses_only_panel";
	[imageExpenseWait setImage:[UIImage imageNamed:@"expenses_only_panel_loading.png"]];
	
	skinExpense.image = nil;
	NSString *imgPath = [NSString stringWithFormat:@"%@.png",skins[@"ivExpenseBackground"]];
	[skinExpense setImage:[UIImage imageNamed:imgPath]];
}

-(void)setExpenseOnlyBackgroundPortrait
{
	NSMutableDictionary *skins = dictSkins[@"default"];
	skins[@"ivExpenseBackground"] = @"expenses_only_portrait_panel";
	[imageExpenseWait setImage:[UIImage imageNamed:@"expenses_only_portrait_panel_loading.png"]];
	
	skinExpense.image = nil;
	NSString *imgPath = [NSString stringWithFormat:@"%@.png",skins[@"ivExpenseBackground"]];
	[skinExpense setImage:[UIImage imageNamed:imgPath]];
}

-(void) resetSkins
{
    imageExpenseWait.image = nil;
    skinExpense.image = nil;
    
    NSMutableDictionary *skins = dictSkins[@"default"];
    skins[@"ivExpenseBackground"] = @"expense_book";
    
    skinExpense.image = nil;
    NSString *imgPath = [NSString stringWithFormat:@"%@.png",skins[@"ivExpenseBackground"]];
    [skinExpense setImage:[UIImage imageNamed:imgPath]];

}

#pragma mark -
#pragma mark Logout and Settings Button Methods
-(void) killHomeData
{
	for(UIView *v in scrollerExpense.subviews)
		[v removeFromSuperview];
	
	[keys removeAllObjects];
	[dictSegRows removeAllObjects];
    
	if(dataOOP != nil)
	{
		self.dataOOP = nil;
	}
	
	[self processCardsInfo];
	[self prepareExpSummaryView];
}

-(void)cleanUpViews
{
	[expSummaryScroller removeAllSubviews];
    [addFuncScroller removeAllSubviews];
}

// Based on buttonLogoutPressed
- (void)onLogout
{
	[aKeys removeAllObjects];
	[self killHomeData];
	
	[self cleanUpViews];
//	[[ConcurMobileAppDelegate findRootViewController].sectionKeys removeAllObjects];
//	[[ConcurMobileAppDelegate findRootViewController].sectionData removeAllObjects];
//	[[ConcurMobileAppDelegate findRootViewController].sections removeAllObjects];
}

- (IBAction)buttonLogoutPressed:(id)sender
{
	[[ApplicationLock sharedInstance] onLogoutButtonPressed];
}

-(void) doPostLoginInitialization
{
    [self checkRoles];
    [self configureViews]; // Requires that checkRoles has already been called
	[self fetchHomePageData];
	
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
        if (self.viewNew != nil && showWhatsNew)
        {
            [self.viewNew closeMe:self];
        }
        
//        MobileAlertView *alert = [rootVC getPrivacyActView:self];
//        alert.tag = PRIVACY_ACT_ALERT_ACTION;
//        [alert show];
    }
}

-(void) savePrarmetersAfterLogin:(NSDictionary *) pBag
{
    if (pBag != nil)
    {
        [self.postLoginAttribute addEntriesFromDictionary:pBag];
    }
}

//-(void) showManualLoginView
//{
//    if ([RootViewController isLoginViewShowing])
//        return;
//}

- (IBAction)buttonSettingsPressed:(id)sender
{
	[self checkOffline];
	SettingsViewController *svc = [[SettingsViewController alloc] init];
    svc.padHomeVC = self;
    
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:svc];
	
    navi.modalPresentationStyle = UIModalPresentationFormSheet;
    [self presentViewController:navi animated:YES completion:nil];
    
}


#pragma mark -
#pragma mark Home Page Data

//-(void) refreshTripData
//{
//        [self checkOffline];
//        
//        NSMutableDictionary* pBag2 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[[ConcurMobileAppDelegate findRootViewController] getViewIDKey], @"TO_VIEW", nil];
//        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag2 SkipCache:YES RespondTo:self];
//}



//-(void) refreshSummaryData
//{
//	[self checkOffline];
//	
//	NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[[ConcurMobileAppDelegate findRootViewController] getViewIDKey], @"TO_VIEW", nil];
//	[[ExSystem sharedInstance].msgControl createMsg:SUMMARY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
//}


//-(void) refreshOOPData
//{
//	// MOB-9593 Fetch all expenses for expense user only
//	if (!(isTravelOnly || isItinViewerOnly) && [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
//	{
//        [self checkOffline];
//		NSMutableDictionary* pBag3 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[[ConcurMobileAppDelegate findRootViewController] getViewIDKey], @"TO_VIEW", nil];
//		[[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag3 SkipCache:NO RespondTo:self];
//	}
//}

- (void)fetchHomePageData
{
	[self checkOffline];
	if([[ApplicationLock sharedInstance] isLoggedIn])
	{
        if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
			[self getTheRates];
        
//		NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[[ConcurMobileAppDelegate findRootViewController] getViewIDKey], @"TO_VIEW", nil];
//		[[ExSystem sharedInstance].msgControl createMsg:SUMMARY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
//		
//		if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] || [[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER])
//		{
//			NSMutableDictionary* pBag2 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[[ConcurMobileAppDelegate findRootViewController] getViewIDKey], @"TO_VIEW", nil];
//			[[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag2 SkipCache:NO RespondTo:self];
//		}
//		
//		if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
//		{
//            // Load the expense types
//            ExpenseTypesManager* etMgr = [ExpenseTypesManager sharedInstance];
//            [etMgr loadExpenseTypes:nil msgControl:[ExSystem sharedInstance].msgControl];
//            
//            // Load the expenses
//			NSMutableDictionary* pBag3 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[[ConcurMobileAppDelegate findRootViewController] getViewIDKey], @"TO_VIEW", nil];
//			[[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag3 SkipCache:NO RespondTo:self];
//            
//            // Load currencies
//            FormFieldData *currencyField = [[FormFieldData alloc] init];
//            currencyField.iD = @"TransactionCurrencyName";
//            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: currencyField, @"FIELD", @"Y", @"MRU", nil];
//            [[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
//		}
	}
}

-(void) checkRoles
{
    if ([[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] && ![[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
    {
        isTravelOnly = YES;
        isExpenseOnly = NO;
        isItinViewerOnly = NO;
    }
    else if (![[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] && [[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER] && ![[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
    {
        isTravelOnly = NO;
        isExpenseOnly = NO;
        isItinViewerOnly = YES;
    }
    else if (!([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER]|| [[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER]) && ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER]))
    {
        isTravelOnly = NO;
        isExpenseOnly = YES;
        isItinViewerOnly = NO;
    }
    else
    {
        isItinViewerOnly = NO;
        isTravelOnly = NO;
        isExpenseOnly = NO;
    }
}

-(void)respondToFoundData:(Msg *)msg
{
	[self checkOffline];
	
	//NSString* kRptRowKey = @"UnsubmittedReportsRow";
	//NSString* kAprRowKey = @"ReportsToApproveRow";
    
	if([msg.idKey isEqualToString:CAR_RATES_DATA])
	{
//		self.carRatesData = (CarRatesData*) msg.responder;
//		[ConcurMobileAppDelegate findRootViewController].carRatesData = carRatesData;
        //MOB-8826: Car mileage not shown at first login.
        
        if (checkedMileage == FALSE) {
            [self renderActionButtons];
            checkedMileage = true;
        }
        
	}
	else if ([msg.idKey isEqualToString:REPORT_ENTRY_FORM_DATA])
	{
		if (msg.errBody != nil)
		{
			UIAlertView *alert = [[MobileAlertView alloc]
								  initWithTitle:[Localizer getLocalizedText:@"Unable to add select Car Mileage"]
								  message:msg.errBody
								  delegate:nil
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			
			[alert show];
		}
		else
		{
			[self presentPersonalCarMileageForm:msg];
		}
	}
	else if ([msg.idKey isEqualToString:HOTEL_IMAGES] && [self isViewLoaded])
	{
		//HotelImagesData *hids = (HotelImagesData *)msg.responder;
	}
	else if ([msg.idKey isEqualToString:WEATHER] && [self isViewLoaded])
	{
	}
//	else if ([msg.idKey isEqualToString:OOPES_DATA] && [self isViewLoaded])
//	{
//        OutOfPocketData *oopData = (OutOfPocketData *)msg.responder;
//        self.dataOOP = [[ConcurMobileAppDelegate findRootViewController] getCardRowDetails:oopData];// corpCardTCount:self.summaryData.];
//            
//        //[rootVC preparePersonalCardRows:oopData]; already called in above API
//        [self processCardsInfo];
//        [self prepareExpSummaryView];
//	}
	else if ([msg.idKey isEqualToString:SUMMARY_DATA] && [self isViewLoaded])
	{
		if ([ExSystem sharedInstance].sessionID == nil && [ExSystem connectedToNetwork]) //MOB-3404: Need to check for offline here.  Still need to process the cached data...
		{
			// Fix for MOB-1599:
			//
			// The session id is nil, but we're here because a response
			// was just received from the server for the user who logged out.
			// Do not remove any objects from the sectionData, sectionKeys, and
			// sections arrays.  Those arrays were cleaned up in initSections
			// which was called by buttonLogoutPressed when the user pressed
			// the logout button.  If we mess with them now, things could go
			// wrong for the next user who logs in.
			//
			return;
		}
        //		if (![[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER] && ![[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
        //			return;
		
		SummaryData *sd = (SummaryData *)msg.responder;
		self.summaryData = sd;
        
        if (![[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
        {
            [self processCardsInfo];
            [self prepareExpSummaryView];
        }
	}
	else if ([msg.idKey isEqualToString:TRIPS_DATA] && [self isViewLoaded])
	{
        
        //        self.tripsData = (TripsData *)msg.responder;
        //        [self refreshUIWithTripsData:self.tripsData];
        
        //MOB-10675 current active trip is not loaded on the home screen
        self.tripsData = (TripsData *)msg.responder;
        NSArray *aTrips = [[TripManager sharedInstance] fetchAll];
        
        for (int i = 0; i < [aTrips count]; i++) {
            EntityTrip* trip = (EntityTrip*)aTrips[i];
            [tripsData.keys addObject:trip.tripKey];
            
            TripData* tempTrip = [[TripData alloc] init];
            [tempTrip setFromEntityTrip:trip];
            
            (tripsData.trips)[trip.tripKey] = tempTrip;
        }
        
		TripData* activeTrip = nil;
        
		if ([tripsData.keys count] >0)
		{
			NSDate* now = [NSDate date];
			for (NSString* key in tripsData.keys)
			{
				TripData* trip = (tripsData.trips)[key];
				NSDate* endDate = [DateTimeFormatter getLocalDate:trip.tripEndDateLocal];
                NSDate* startDate = [DateTimeFormatter getLocalDate:trip.tripStartDateLocal];
				if ([endDate compare:now] == NSOrderedAscending || [startDate compare:now] == NSOrderedDescending)
					continue;
                
				if (activeTrip == nil)
				{
					activeTrip = trip;
				}
				else
				{
					NSDate* currentDate = [DateTimeFormatter getLocalDate:activeTrip.tripStartDateLocal];
					NSDate* date = [DateTimeFormatter getLocalDate:trip.tripStartDateLocal];
					if ([date compare:currentDate] == NSOrderedAscending)
					{
						activeTrip = trip;
					}
				}
			}
		}
		if (activeTrip != nil)
		{
			self.currentTrip = activeTrip;
			for (UIView *v in [tripCategoryIconsView subviews])
			{
				[v removeFromSuperview];
			}
		}
		
		[self prepareCurrentTripSummaryView:activeTrip];
	}
	else if([msg.idKey isEqualToString:UPLOAD_IMAGE_DATA])
	{
		UploadReceiptData *receipt = (UploadReceiptData*)msg.responder;
        
		if ([self isViewLoaded])
		{
			UIActivityIndicatorView *activityView = nil;
			for (UIView *v in [btnUploadReceipt subviews])
			{
				if ([v isKindOfClass:[UIActivityIndicatorView class]])
				{
					activityView = (UIActivityIndicatorView*)v;
					break;
				}
			}
			
			[activityView stopAnimating];
			[activityView removeFromSuperview];
			[btnUploadReceipt setEnabled:YES];
		}
		
        //Mob-5262
        ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*)[[UIApplication sharedApplication] delegate];
		MobileViewController* vc = (MobileViewController*)delegate.padHomeVC.navigationController.topViewController;
        
        // don't show the pop-over if some modal is showing on top
        if ([vc isKindOfClass:[iPadHomeVC class]] && vc.presentedViewController == nil)
        {
            if (msg.errBody == nil && [receipt.returnStatus isEqualToString:@"SUCCESS"])
            {
                UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload complete"]
                                                                    message:[Localizer getLocalizedText:@"ToReceiptStoreMsg"]
                                                                   delegate:self
                                                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                                                          otherButtonTitles:[Localizer getLocalizedText:@"LABEL_OK_BTN"],nil];
                alert.tag = RECEIPT_STORE_ALERT_ACTION;
                [alert show];
            }
            else
            {
                // MOB-10154
                if ([@"Imaging Configuration Not Available." isEqualToString: msg.errBody])
                {
                    UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Cannot access receipt"]
                                                                        message:[Localizer getLocalizedText:@"ERROR_BAD_CONFIG_MSG"]
                                                                       delegate:self
                                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                                              otherButtonTitles:nil];
                    [alert show];
                    
                }
                else {
                    
                    UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload failed"]
                                                                        message:[Localizer getLocalizedText:@"ReceiptUploadFailMsg"]
                                                                       delegate:self
                                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                                              otherButtonTitles:nil];
                    [alert show];
                }
            }
            
        }
	}
}

#pragma mark -
#pragma mark Home Buttons
-(void) adjustHomeButtons:(UIViewController *) vc
{
	[self checkOffline];
	
	float y = 653.0;
	float h = 95.0;
	float endW = 20.0;
	float viewW = 80.0;
	float viewH = 80.0;
	float w = 1024.0;
	float screenH = 768.0;
	
	if(![ExSystem isLandscape])
	{
		w = 768.0;
		screenH = 1004.0;
		y = screenH - h;
	}
	
	int btnPos = 0;
	
	if(aBtns != nil && [aBtns count] > 0)
	{
		[aBtns removeAllObjects];
	}
	
	if([self isKindOfClass:[iPadHomeVC class]])
		[self makeHomeButtons:YES ViewToAddTo:vc];
	else
		[self makeHomeButtons:NO ViewToAddTo:vc];
	
	NSUInteger numButtons = [aBtns count]; //need to initially ask about roles...
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
        viewW = GSA_HOME_BUTTON_WIDTH;
    
	float middleW = numButtons * viewW;
	float x = (w - middleW) / 2;
	middleX = x;
	
	if (![ExSystem isLandscape]) {
		if(middleW > 600)
		{
			middleW = 600;
			x = (w - middleW) / 2;
			middleX = x;
		}
	}
	
	if([vc isKindOfClass:[iPadHomeVC class]])
	{
		ivBarMiddle.frame = CGRectMake(x, y, middleW, h);
		ivBarLeft.frame = CGRectMake(x - endW, y, endW, h);
		ivBarRight.frame = CGRectMake(x + middleW, y, endW, h);
		
		for(UIView *v in scrollerButtons.subviews)
			[v removeFromSuperview];
		
		scrollerButtons.frame = CGRectMake(x, y, middleW, h);
		if(![ExSystem isLandscape] && middleW == 600)
			scrollerButtons.contentSize = CGSizeMake(numButtons * viewW, h);
		else
			scrollerButtons.contentSize = CGSizeMake(middleW, h);
		
		for(UIView *iPadBtn in aBtns)
		{
			iPadBtn.frame = CGRectMake((btnPos * viewW), 10, viewW, viewH);
			[scrollerButtons addSubview:iPadBtn];
			btnPos++;
		}
		[self.view bringSubviewToFront:scrollerButtons];
        if(self.viewPadIntro != nil)
            [self.view bringSubviewToFront:viewPadIntro];
	}
	else if([vc isKindOfClass:[ReportDetailViewController_iPad class]])
	{
		ReportDetailViewController_iPad *dvc = (ReportDetailViewController_iPad *)vc;
		dvc.ivBarMiddle.frame = CGRectMake(x, y, middleW, h);
		dvc.ivBarLeft.frame = CGRectMake(x - endW, y, endW, h);
		dvc.ivBarRight.frame = CGRectMake(x + middleW, y, endW, h);
		
		for(UIView *v in dvc.scrollerButtons.subviews)
			[v removeFromSuperview];
		
		dvc.scrollerButtons.frame = CGRectMake(x, y, middleW, h);
		if(![ExSystem isLandscape] && middleW == 600)
			dvc.scrollerButtons.contentSize = CGSizeMake(numButtons * viewW, h);
		else
			dvc.scrollerButtons.contentSize = CGSizeMake(middleW, h);
		
		for(UIView *iPadBtn in aBtns)
		{
			iPadBtn.frame = CGRectMake((btnPos * viewW), 10, viewW, viewH);
			[dvc.scrollerButtons addSubview:iPadBtn];
			btnPos++;
		}
		[dvc.view bringSubviewToFront:dvc.scrollerButtons];
        if(self.viewPadIntro != nil)
            [self.view bringSubviewToFront:viewPadIntro];
	}
	else if([vc isKindOfClass:[DetailViewController class]])
	{
		DetailViewController *dvc = (DetailViewController *)vc;
		dvc.ivBarMiddle.frame = CGRectMake(x, y, middleW, h);
		dvc.ivBarLeft.frame = CGRectMake(x - endW, y, endW, h);
		dvc.ivBarRight.frame = CGRectMake(x + middleW, y, endW, h);
		//[dvc.view bringSubviewToFront: dvc.ivBarMiddle];
		
		for(UIView *v in dvc.scrollerButtons.subviews)
			[v removeFromSuperview];
		
		dvc.scrollerButtons.frame = CGRectMake(x, y, middleW, h);
		if(![ExSystem isLandscape] && middleW == 600)
			dvc.scrollerButtons.contentSize = CGSizeMake(numButtons * viewW, h);
		else
			dvc.scrollerButtons.contentSize = CGSizeMake(middleW, h);
		
		for(UIView *iPadBtn in aBtns)
		{
			iPadBtn.frame = CGRectMake((btnPos * viewW), 10, viewW, viewH);
			[dvc.scrollerButtons addSubview:iPadBtn];
			btnPos++;
		}
		[dvc.view bringSubviewToFront:dvc.scrollerButtons];
        if(self.viewPadIntro != nil)
            [self.view bringSubviewToFront:viewPadIntro];
	}
}


-(void)popHome:(id)sender
{
	[self checkOffline];
	
	[self.navigationController popToRootViewControllerAnimated:YES];
}


-(void) makeHomeButtons:(BOOL) isHome ViewToAddTo:(UIViewController *) vc
{
	int btnPos = 0;
	if(aBtns == nil)
		aBtns = [[NSMutableArray alloc] initWithObjects:nil];
	
	
	NSMutableDictionary *skin = dictSkins[@"default"];
	
	UIView *btnView = [self makeHomeButton:[Localizer getLocalizedText:@"Home"] ImageName:@"HomeBtnHome" SelectorName:@"popHome:" ButtonPos:btnPos ViewToAddTo:vc];
	[aBtns addObject:btnView];
	btnPos++;
	
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
        UIView *btnView = [self makeHomeButton:[Localizer getLocalizedText:@"Stamp Docs"] ImageName:@"HomeBtnApprovals" SelectorName:@"showGovStampDocuments:" ButtonPos:btnPos ViewToAddTo:vc];
		[aBtns addObject:btnView];
		btnPos++;
        
        btnView = [self makeHomeButton:[Localizer getLocalizedText:@"Auths"] ImageName:@"HomeBtnTravelRequests" SelectorName:@"showGovAuthorizations:" ButtonPos:btnPos ViewToAddTo:vc];
		[aBtns addObject:btnView];
		btnPos++;
        
        btnView = [self makeHomeButton:[Localizer getLocalizedText:@"Vouchers"] ImageName:@"HomeBtnReports" SelectorName:@"showGovVouchers:" ButtonPos:btnPos ViewToAddTo:vc];
		[aBtns addObject:btnView];
		btnPos++;
        
        btnView = [self makeHomeButton:[Localizer getLocalizedText:@"Expenses"] ImageName:@"HomeBtnExpense" SelectorName:@"showGovUnappliedExpenses:" ButtonPos:btnPos ViewToAddTo:vc];
		[aBtns addObject:btnView];
		btnPos++;
        
    }
    
	if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER]|| [[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER])
	{
		UIView *btnView = [self makeHomeButton:[Localizer getLocalizedText:@"Trips"] ImageName:@"HomeBtnTrips" SelectorName:@"buttonTripsPressed:" ButtonPos:btnPos ViewToAddTo:vc];
		[aBtns addObject:btnView];
		btnPos++;
	}
	
    if ([[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER])
    {
        UIView *btnView = [self makeHomeButton:[Localizer getLocalizedText:@"Travel Requests"] ImageName:@"HomeBtnTravelRequests" SelectorName:@"buttonTravelRequestPressed:" ButtonPos:btnPos ViewToAddTo:vc];
		[aBtns addObject:btnView];
		btnPos++;
    }
    
	if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
	{
		UIView *btnView = [self makeHomeButton:[Localizer getLocalizedText:@"Expenses"] ImageName:@"HomeBtnExpense" SelectorName:@"buttonExpensesPressed:" ButtonPos:btnPos ViewToAddTo:vc];
		[aBtns addObject:btnView];
		btnPos++;
	}
	else
	{
		if(btnAddExpense != nil)
			[btnAddExpense removeFromSuperview];
	}
    
	if (![[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
        if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER] && !hideReceiptStore)
        {
            UIView *btnView = [self makeHomeButton:[Localizer getLocalizedText:@"Receipts"] ImageName:@"HomeBtnReceipts" SelectorName:@"buttonReceiptsPressed:" ButtonPos:btnPos ViewToAddTo:vc];
            [aBtns addObject:btnView];
            btnPos++;
        }
        
        if ([[ExSystem sharedInstance] siteSettingAllowsExpenseReports])
        {
            UIView *btnView = [self makeHomeButton:[Localizer getLocalizedText:@"Reports"] ImageName:@"HomeBtnReports" SelectorName:@"buttonReportsPressed:" ButtonPos:btnPos ViewToAddTo:vc];
            [aBtns addObject:btnView];
            btnPos++;
        }
        
        if ([[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals] && [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER])
        {
            UIView *btnView = [self makeHomeButton:[Localizer getLocalizedText:@"Approvals"] ImageName:@"HomeBtnApprovals" SelectorName:@"buttonApprovalsPressed:" ButtonPos:btnPos ViewToAddTo:vc];
            [aBtns addObject:btnView];
            btnPos++;
        }
    }
      
    if (([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] siteSettingAllowsTravelBooking]) && ![vc isKindOfClass:[DetailViewController class]])
	{
        UIView *travelBookingAppsMenu = [self makeHomeButton:[Localizer getLocalizedText:@"Travel"] ImageName:@"nav_travel" SelectorName:@"buttonTravelBookingAppsMenuPressed:" ButtonPos:btnPos ViewToAddTo:vc];
		[aBtns addObject:travelBookingAppsMenu];
        btnPos++;
    }
    
    // MOB-11528 [iOS] New URL and landing page for HTML5
    if(![[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] &&
       ([[ExSystem sharedInstance] hasRole:ROLE_INVOICE_APPROVER]  || [[ExSystem sharedInstance] hasRole:MOBILE_INVOICE_PAYMENT_USER]))
	{
		UIView *btnAppsMenu = [self makeHomeButton:[Localizer getLocalizedText:@"Invoices"] ImageName:@"nav_invoice" SelectorName:@"buttonInvoicePressed:" ButtonPos:btnPos ViewToAddTo:vc];
		[aBtns addObject:btnAppsMenu];
	}
    
    
    
	if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
	{
		UIView *btnAppsMenu = [self makeHomeButton:[Localizer getLocalizedText:@"Apps"] ImageName:@"nav_apps" SelectorName:@"buttonAppsMenuPressed:" ButtonPos:btnPos ViewToAddTo:vc];
		[aBtns addObject:btnAppsMenu];
		//btnPos++;
	}
}


-(UIView *) makeHomeButton:(NSString *)btnTitle ImageName:(NSString *)imageName SelectorName:(NSString *)selectorName ButtonPos:(int)btnPos ViewToAddTo:(UIViewController *) vc
{
	float viewW = 80.0;
	float viewH = 80.0;
	float btnW = 57.0;
	float btnH = 50.0;
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
        viewW = GSA_HOME_BUTTON_WIDTH;
    
	__autoreleasing UIView *btnView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, viewW, viewH)];
	
	ButtonSegment *btn = [[ButtonSegment alloc] initWithFrame:CGRectMake((viewW - btnW) / 2, 0, btnW, btnH)];
	UIImage *image = [UIImage imageNamed:imageName];
	[btn setBackgroundImage:image forState:UIControlStateNormal];
	if(selectorName != nil)
		[btn addTarget:self action:NSSelectorFromString(selectorName) forControlEvents:UIControlEventTouchUpInside];
	btn.tag = btnPos;
	btn.parentView = btnView;
	[btnView addSubview:btn];
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, btnH + 1, viewW, 35)];
	[lbl setNumberOfLines:2];
	[lbl setTextAlignment:NSTextAlignmentCenter];
	[lbl setFont:[UIFont boldSystemFontOfSize:14]];
    [lbl setMinimumScaleFactor:4.0/14.0];
	[lbl setText:btnTitle];
	[lbl setTextColor:[UIColor whiteColor]];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setLineBreakMode:NSLineBreakByWordWrapping];
	[btnView addSubview:lbl];
	
	return btnView;
}


#pragma mark -
#pragma mark Expense Panel Stuff
-(void)makeExpenseView:(NSString *) btnTitle SelectorName:(NSString *)selectorName ImageName:(NSString *)imageName Row:(int)row
{
	float h = 45.0;
	float w = 360.0;
	
	UIView *btnView = [[UIView alloc] initWithFrame:CGRectMake(0, (row * h), w, h)];
	UIButton *btn = [[UIButton alloc] initWithFrame:CGRectMake(0, (row * h), w, h)];
	if(selectorName != nil)
		[btn addTarget:self action:NSSelectorFromString(selectorName) forControlEvents:UIControlEventTouchUpInside];
    
	//[btn setImage:[UIImage imageNamed:imageName] forState:UIControlStateNormal];
	//[btn setTitle:btnTitle forState:UIControlStateNormal];
	
	UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(1, 6, 38.0, 38.0)];
	iv.image = [UIImage imageNamed:imageName];
	[btnView addSubview:iv];
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(45.0, 0, w - 5, h)];
	lbl.text = btnTitle;
	[lbl setTextAlignment:NSTextAlignmentLeft];
	[lbl setBackgroundColor:[UIColor clearColor]];
	
	[btnView addSubview:lbl];
	//[btnView addSubview:btn];
	//[btn release];
	
	scrollerExpense.frame = CGRectMake(5, scrollerExpense.frame.origin.y, scrollerExpense.frame.size.width, scrollerExpense.frame.size.height);
	
	[scrollerExpense addSubview:btnView];
    
	[scrollerExpense addSubview:btn];
	//return [btnView autorelease];
}


-(void) loadExpenseSection:(int)cardCount ExpenseCount:(int)expenseCount ReportCount:(int)reportCount ApprovalCount:(int)approvalCount
{
	for(UIView *v in scrollerExpense.subviews)
		[v removeFromSuperview];
	
	scrollerExpense.contentSize = CGSizeMake(360.0, 200.0);
	//[scrollerExpense.subviews removeObserver: fromObjectsAtIndexes:<#(NSIndexSet *)indexes#> forKeyPath:<#(NSString *)keyPath#>
	
	NSString *cards = @"";// [NSString stringWithFormat:@"You have %d unused charges", cardCount];
	NSString *reports = @""; // [NSString stringWithFormat:@"You have %d %@", reportCount, [Localizer getLocalizedText:@"unsubmitted Reports"]];
	NSString *approvals = nil;// [NSString stringWithFormat:@"You have %d approval reports to approve", approvalCount];
	int iPos = 0;
	
	if(cardCount > 0)
	{
		if(cardCount == 1)
			cards = [Localizer getLocalizedText:@"CORP_CARDS_1"];
		else
			cards = [NSString stringWithFormat:[Localizer getLocalizedText:@"CORP_CARDS_2"], cardCount];
		[self makeExpenseView:cards SelectorName:nil ImageName:@"blue_expense_card.png" Row:iPos]; //expense_card_charge.png
		iPos++;
	}
	
	if(reportCount > 0)
	{
		if(reportCount == 1)
			reports = [Localizer getLocalizedText:@"UnsubmittedReportsRow_1"];
		else
			reports = [NSString stringWithFormat:[Localizer getLocalizedText:@"UnsubmittedReportsRow_2"], reportCount];
		[self makeExpenseView:reports SelectorName:@"buttonReportsFromScrollerPressed:" ImageName:@"blue_reports.png" Row:iPos];//expense_report.png
		iPos++;
	}
	
	if(approvalCount > 0)
	{
		if(approvalCount == 1)
			approvals = [Localizer getLocalizedText:@"ReportsToApproveRow_1"];
		else
			approvals = [NSString stringWithFormat:[Localizer getLocalizedText:@"ReportsToApproveRow_2"], approvalCount];
		[self makeExpenseView:approvals SelectorName:@"buttonApprovalsFromScrollerPressed:" ImageName:@"blue_reports_approval.png" Row:iPos];//expense_report_approval.png
        //		iPos++;
	}
	
}

-(IBAction) showCurrentTrip:(id)sender
{
	if(currentTrip != nil)
	{
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:currentTrip, @"TRIP", nil];
		[self switchToDetail:@"Trip" ParameterBag:pBag];
	}
}

#pragma mark -
#pragma mark Button Press Methods
- (void)buttonDiningPressed:(id)sender
{
    [AppsUtil launchOpenTableApp];
}

-(void)buttonAirportsPressed:(id)sender
{
	[self performSelector:@selector(switchToAirports:) withObject:nil afterDelay:0.05f];
}

- (void)switchToAirports:(id)sender
{
    [AppsUtil launchGateGuruAppWithUrl:nil];
}

- (void)buttonTaxiPressed:(id)sender
{
    [AppsUtil launchTaxiMagicApp];
}

- (void)buttonMetroPressed:(id)sender
{
    [AppsUtil launchMetroApp];
}


#pragma mark -
#pragma mark Car Mileage
-(IBAction)showPersonalCarMileage:(id)sender
{
    
//	if(carRatesData == nil || ![carRatesData hasAnyPersonalsWithRates:[ExSystem sharedInstance].sys.crnCode])
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
//		return;
//    }
	//let's go and get car rates...
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	[[ExSystem sharedInstance].msgControl createMsg:CAR_RATES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
	
    self.addExpenseClicked = NO;
    
	[self goToSelectReport];
	
}


-(void) getTheRates
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	[[ExSystem sharedInstance].msgControl createMsg:CAR_RATES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
}

-(void) fetchPersonalCarMileageFormFields:(id)sender rptKey:(NSString*) rptKey rpt:(ReportData*) rpt
{
	//set up the formdata
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 @"MILEG", @"EXP_KEY",
								 rpt.rptKey, @"RPT_KEY", //, nil ]; //MILEG = Default Personal Car Mileage  CARMI = Company Car Mileage
								 //parentRpeKey, @"PARENT_RPE_KEY",
								 rpt, @"rpt",
								 nil];
    if (addExpenseClicked)
        pBag[@"EXP_KEY"] = @"UNDEF";
    
	[[ExSystem sharedInstance].msgControl createMsg:REPORT_ENTRY_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	
	
}


-(void) presentPersonalCarMileageForm:(Msg *)msg
{
	//	NSString *rptKey = @"n1uivIbsjMlElJo4CJqYUOrulrA";
	//	NSString *parentRpeKey = @"nfo7FnsxOG$pLN7lLhvej$p35IoGw";
	
	ReportEntryFormData* resp = (ReportEntryFormData*) msg.responder;
	ReportData *rpt = (msg.parameterBag)[@"rpt"];
	
	resp.rpt.entry.parentRpeKey = @"";
	// Temp fix, b/c server passes back garbage
	resp.rpt.entry.rpeKey = nil;
	resp.rpt.entry.rptKey =rpt.rptKey;
	resp.rpt.entry.transactionCrnCode = rpt.crnCode;
    
    if (addExpenseClicked)
    {
        resp.rpt.entry.expKey = nil;
        resp.rpt.entry.expName = [Localizer getLocalizedText:@"Undefined"];
        FormFieldData* expFld = (resp.rpt.entry.fields)[@"ExpKey"];
        if (expFld != nil)
        {
            expFld.liKey = nil;
            expFld.fieldValue = nil;
        }
    }
	
	NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"SHORT_CIRCUIT", resp.rpt.entry, @"ENTRY", rpt, @"REPORT", @"MOBILE_EXPENSE_TRAVELER", @"ROLE", [ConcurMobileAppDelegate findRootViewController], @"ROOT_VC", nil];
    
    pBag[@"FROM_HOME"] = @"YES";
    
	self.wizPBag = pBag;
    [self nextStepForWizard];
}


- (void) goToSelectReport
{
    if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Operation Not Supported Offline"]
							  delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                              otherButtonTitles:nil];
		[alert show];
		return;
	}
    
    self.wizPBag = nil;
    self.wizDlgDismissed = NO;
    
	//takes you to the select report view
	SelectReportViewController * pVC = [[SelectReportViewController alloc] initWithNibName:@"SelectReportViewController" bundle:nil];
	pVC.meKeys = nil;
	pVC.pctKeys = nil;
	pVC.cctKeys = nil;
	pVC.meAtnMap = nil;
	pVC.isCarMileage = YES;
	pVC.parentMVC = self;
	pVC.padHomeVC = self;
	
	if([UIDevice isPad])
	{
		UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:pVC];
		localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
		[localNavigationController setToolbarHidden:NO];
        
		localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
		localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
		
		[self presentViewController:localNavigationController animated:YES completion:nil];
	}
	else
		[self.navigationController pushViewController:pVC animated:YES];
	
}

#pragma mark Car Mileage dialog transition
// API for transition in add car mileage dialogs
-(void)nextStepForWizard
{
	if (self.wizDlgDismissed && self.wizPBag != nil)
	{
        if (addExpenseClicked)
            (self.wizPBag)[@"TITLE"] = [Localizer getLocalizedText:@"Add Expense"];
        else
            (self.wizPBag)[@"TITLE"] = [Localizer getLocalizedText:@"Add Car Mileage"];
        
		[ReportDetailViewController showEntryView:self withParameterBag: self.wizPBag carMileageFlag:!addExpenseClicked];
	}
}

-(void)wizardDlgDismissed
{
	self.wizDlgDismissed = YES;
	[self nextStepForWizard];
}


-(IBAction) closeNew:(id)sender
{
    [ExSystem sharedInstance].sys.showWhatsNew = NO;
	[[ExSystem sharedInstance] saveSystem];
	CGContextRef context = UIGraphicsGetCurrentContext();
	[UIView beginAnimations:nil context:context];
	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
	[UIView setAnimationDidStopSelector:@selector(hideNew:)];
	[UIView setAnimationDuration:0.33];
	[viewNew setAlpha:0.0f];
	[UIView commitAnimations];
	//[viewNew setHidden:YES];
}

-(void) adjustWhatsNewLandscape
{
    //    viewNew.frame = CGRectMake(0, 0, 1024, 768);
	btnNewOK.frame = CGRectMake(421, 492, 187, 31);
	lblNewTitle.frame = CGRectMake(374, 183, 280, 29);
}


-(void)adjustWhatsNewPortrait
{
    //    viewNew.frame = CGRectMake(0, 0, 768, 1024);
	lblNewTitle.frame = CGRectMake((768 - 280) / 2, 294, 280, 29);
	btnNewOK.frame = CGRectMake((768 - 187) / 2, 600, 187, 31);
}

#pragma mark -
#pragma mark Button Press Methods
-(void)buttonInvoicePressed:(id)sender
{
    [self checkOffline];
	
	if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Bookings offline"]
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return;
	}
    
    // MOB-11528 [iOS] New URL and landing page for HTML5
    NSURL *url = [[ExSystem sharedInstance] urlForWebExtension:@"invoice-home-page"];
    [[UIApplication sharedApplication] openURL:url];
	
    /*
     if(pickerPopOver != nil)
     {
     [pickerPopOver dismissPopoverAnimated:YES];
     pickerPopOver = nil;
     }
     
     PaymentRequestListViewController* nextController = [[PaymentRequestListViewController alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
     nextController.iPadHome = self;
     UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
     
     NSString *msgName = APPROVE_INVOICES_DATA;
     NSString *cacheOnly = @"NO";
     
     NSMutableDictionary *paramBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"MOON", @"TO_VIEW", nil];
     
     [paramBag setObject:cacheOnly forKey:@"CACHE_ONLY"];
     
     [paramBag setObject:msgName forKey:@"MSG_NAME"];
     
     BOOL skipCache = NO;
     
     [[ExSystem sharedInstance].msgControl createMsg:msgName CacheOnly:cacheOnly ParameterBag:paramBag SkipCache:skipCache RespondTo:nextController];
     
     localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
     
     [localNavigationController setToolbarHidden:NO];
     localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
     localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
     
     [self presentViewController:localNavigationController animated:YES completion:nil];
     //    [self switchToView:APPROVE_INVOICES viewFrom:HOME_PAGE ParameterBag:nil];
     */
}

#pragma mark SUHomeActionDelegate
-(IBAction) buttonApproveReportsPressed:(id)sender
{
    [self buttonApprovalsFromScrollerPressed:nil];
}

-(void) finishHomeUpdate
{
    [self hideWaitView];// TODO - use custom wait view?
}


#pragma GSA Actions

-(void) showGovDocumentListView:(NSString*) filter
{
    GovDocumentListVC *vc = [[GovDocumentListVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    vc.filter = filter;
    //    [self.navigationController pushViewController:vc animated:YES];
    
    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:vc];
    localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    [localNavigationController setToolbarHidden:NO];
    
    localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
    localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
    
    NSString* msgId = [filter isEqualToString:GOV_DOC_TYPE_STAMP]? GOV_DOCUMENTS_TO_STAMP: GOV_DOCUMENTS;
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    [[ExSystem sharedInstance].msgControl createMsg:msgId CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:vc];
    
    [self presentViewController:localNavigationController animated:YES completion:nil];
}

-(IBAction) showGovUnappliedExpenses:(id)sender
{
    [GovUnappliedExpensesVC showUnappliedExpenses:self];
}
@end












