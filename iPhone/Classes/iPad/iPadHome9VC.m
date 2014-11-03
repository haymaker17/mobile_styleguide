//
//  iPadHome9VC.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/20/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "iPadHome9VC.h"
#import "iPadHome9Cell.h"
#import "iPadHome9TravelCell.h"
#import "HomeManager.h"
#import "ApplicationLock.h"

#import "TripsViewController.h"
#import "TripDetailsViewController.h"
#import "ItinDetailsViewController.h"
#import "SummaryData.h"
#import "LoginViewController.h"
#import "HotelViewController.h"
#import "TrainBookVC.h"
#import "CarViewController.h"
#import "TripItAuthVC.h"
#import "ExpenseTypesManager.h"
#import "ReceiptStoreUploadHelper.h"
#import "MessageCenterManager.h"
#import "MessageCenterViewController.h"

#import "UploadQueue.h"
#import "UploadQueueViewController.h"
#import "QuickExpensesReceiptStoreVC.h"

#import "WhatsNewView.h"
#import "OverlayView.h"
#import "MoreMenuViewController.h"
#import "MoreMenuData.h"
#import "CarMileageDataLoader.h"
#import "SelectReportViewController.h"
#import "TravelBookingAlertController.h"

// Salesforce
#import "SalesForceUserManager.h"

#import "Config.h"
#import "HelpOverlayFactory.h"
#import "TransparentViewUnderMoreMenu.h"

// Collection View
#import "HomeCollectionView.h"

#import "CarRatesData.h"
#import "ConcurTestDrive.h"
#import "NSStringAdditions.h"
#import "ViewConstants.h"
#import "NSString+Additions.h"
#import "MobileTourViewController.h"
#import "SignInUserTypeViewController.h"
#import "SignInResetPasswordViewController.h"

#import "AnalyticsTracker.h"

// Book Travel Action sheet button IDs
#define BOOKINGS_BTN_AIR @"Book Air"
#define BOOKINGS_BTN_HOTEL @"Book Hotel"
#define BOOKINGS_BTN_CAR @"Book Car"
#define BOOKINGS_BTN_RAIL @"Book Rail"


#define kSECTION_TRAVEL @"TRIPS_BUTTON"
#define kSECTION_APPROVALS @"EXPENSE_APPROVALS"
#define kSECTION_EXPENSES @"kSECTION_EXPENSE_QUICK"
#define kSECTION_EXPENSE_REPORTS @"EXPENSE_REPORTS"
#define kSECTION_CAR_MILEAGE @"kSECTION_CAR_MILEAGE"


// New positions
#define kSECTION_TRIPS_POS 0
#define kSECTION_EXPENSES_POS 1
#define kSECTION_EXPENSES_REPORTS_POS 2
#define kSECTION_APPROVALS_POS 3

// RVC Definitions
#define kSECTION_EXPENSE_CARDS @"EXPENSE_CARDS"
#define kSECTION_EXPENSE_QUICK @"kSECTION_EXPENSE_QUICK"
#define kSECTION_EXPENSE_POS 2
#define kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON @"TRAVEL_REQUEST_BUTTON"
#define kSECTION_EXPENSE @"EXPENSE"
#define kSECTION_EXPENSE_APPROVALS @"EXPENSE_APPROVALS"
#define kSECTION_TRIPS_POS 0
#define kSECTION_TRIPS @"TRIPS"


@interface iPadHome9VC ()

// This class handles the UI, it does NOT handle data or network calls
@property (nonatomic, readwrite, strong) IBOutlet HomeCollectionView *homeCollectionView;
// need to close the CollectionView refresh control after a refresh
@property (nonatomic, readwrite, strong) UIRefreshControl *collectionViewRefreshControl;

// Makes it easier to switch from a TableViewController to a ViewController
@property (nonatomic, readwrite, getter=isRefreshing) BOOL refreshing;
@property (nonatomic)  BOOL                             showWhatsNew, isTravelOnly;
@property BOOL                                          requireHomeScreenRefresh;
@property (nonatomic, strong) SummaryData               *summaryData;
@property (nonatomic, strong) EntityTrip                *currentTrip;
@property (nonatomic, strong) WhatsNewView              *whatsNewView;
@property (nonatomic, strong) OverlayView               *overlayView;
@property (nonatomic, strong) CarMileageDataLoader      *carMileageDataLoader;
@property (strong, nonatomic) CarRatesData              *carRatesData;
@property (strong, nonatomic) TransparentViewUnderMoreMenu *transparentView;
@property (strong, nonatomic) UINavigationController    *moreMenuCtrlNav;
@property BOOL                                          isCameraPress;
@property BOOL                                          accountExpired;

- (void) setupNavBar;

@end

@implementation iPadHome9VC

@synthesize lblOffline, offlineHeaderView;
@synthesize currentTrip, rootVC, requireHomeScreenRefresh ;

const int BOOKINGS_ACTION_TAG = 101;
static int const constAccountExpiredAlert = 102;

BOOL isDataReady = NO;
int servercalls = 0;
UIView *offlineBackground;
UILabel *offlineText;


// The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self)
    {
        requireHomeScreenRefresh = YES;
    }
    return self;
}

#pragma mark -
#pragma mark SalesForce code

// calls old salesforce login information endpoint.  We still call this for the user name and portrait url.
// eventually this call can be eliminated when the login response contains that information.
- (void) requestSalesForceUserData
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    [[ExSystem sharedInstance].msgControl createMsg:IGNITE_USER_INFO_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return @"iPAD_HOME_PAGE90";
}


#pragma mark - ApplicationLock Notifications
-(void) doPostLoginInitialization
{
	// Always get server data if user logs in, so logout and login with another user doesnt have stale data
    [self fetchHomePageDataAndSkipCache:YES];
    requireHomeScreenRefresh = NO;

    if ([Config isSalesforceChatterEnabled]) {
        [self requestSalesForceUserData];
    }

    // update based on role information
    [self updateCollectionViewLayout];
    [HelpOverlayFactory addiPadHomeOverlayToView:self.navigationController.view];

    // if user logged in through testdrive or some other Storyboard then dismiss the login storyboard.
    if(self.signInViewNavigationController != nil)
    {
        [self.signInViewNavigationController dismissViewControllerAnimated:NO completion:nil];
    }
    
    [AnalyticsTracker initializeScreenName:@"Home"];
}


-(BOOL)isTravelOnly
{
 	// check if the user has Travel only role
    return [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] && ![self hasAnyExpenseRole];
}

-(BOOL)hasAnyExpenseRole
{
    return  [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER] ||
            [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER] ||
            [[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER] ||
            [[ExSystem sharedInstance] hasRole:MOBILE_INVOICE_PAYMENT_USER] ||  // Only for approvals? Review Later.
            [[ExSystem sharedInstance] hasRole:ROLE_INVOICE_APPROVER] ||        // Only for approvals? Review Later.
            [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER] || [[ExSystem sharedInstance]hasRole:ROLE_MOBILE_INVOICE_PURCH_APRVR];   // Only for approvals? Review Later.
}

-(BOOL)hasAnyApprovalsRole
{
    return  [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER] ||
            [[ExSystem sharedInstance] hasRole:MOBILE_INVOICE_PAYMENT_USER] ||
            [[ExSystem sharedInstance] hasRole:ROLE_INVOICE_APPROVER] ||
            [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER] ||
            [[ExSystem sharedInstance] hasRole:ROLE_TRIP_APPROVER] || [[ExSystem sharedInstance]hasRole:ROLE_MOBILE_INVOICE_PURCH_APRVR];
}

#pragma mark -
#pragma mark Navigation Controller Methods

- (void)viewDidLoad
{
    [super viewDidLoad];
    [ExSystem sharedInstance].sys.topViewName = self.getViewIDKey;
    
    self.rootVC = [[RootViewController alloc] init];

    // TODO : Clear home data only if version is 9.0 or lower
    if([[ExSystem sharedInstance].sys.showWhatsNew boolValue])
    {
        // First time use of 9.0 clean older version data
        [self clearHomeData];
    }

    
    // this checks ios7 within the method no need to surround it with if ios7 check.
    [ExSystem setStatusBarBlack];

    if([[ExSystem sharedInstance]isBreeze])
    {
        NSDictionary *dictionary = @{@"Type": @"Breeze"};
        [Flurry logEvent:@"User: Type" withParameters:dictionary];
    }
    else if([ExSystem sharedInstance].isBronxUser)
    {
        NSDictionary *dictionary = @{@"Type": @"Bronx"};
        [Flurry logEvent:@"User: Type" withParameters:dictionary];
    }
    else if ([ExSystem sharedInstance].sys.productLine == PROD_GOVERNMENT){
        NSDictionary *dictionary = @{@"Type": @"Gov"};
        [Flurry logEvent:@"User: Type" withParameters:dictionary];
    }
    else if (![[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER]){
        NSDictionary *dictionary = @{@"Type": @" Expense Only"};
        [Flurry logEvent:@"User: Type" withParameters:dictionary];
    }
    else if (![[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER]){
        NSDictionary *dictionary = @{@"Type": @" Travel Only"};
        [Flurry logEvent:@"User: Type" withParameters:dictionary];
    }
    else{
        NSDictionary *dictionary = @{@"Type": @"CTE"};
        [Flurry logEvent:@"User: Type" withParameters:dictionary];
    }

    [self setupCollectionView];
    [self updateCollectionViewLayout];
    
     //MOB-17238 : Test Drive Expiration
    self.accountExpired = NO;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleAccountExpiration) name:NotificationOnAccountExpired object:nil];

}

/**
 Setup collection view
 */
- (void)setupCollectionView
{
    self.homeCollectionView.delegate = self;
    [self rotateHomeCollectionViewToInterfaceOrientation:self.interfaceOrientation];
}

/**
 Update the collection view based on roles
 */
- (void)updateCollectionViewLayout
{
    // Overall Home layout
    // Uses ExSystem to figure out what layout to use
    if ([[ExSystem sharedInstance] isTravelAndApprovalOnlyUser])
    {
        [self.homeCollectionView switchLayoutToTravelAndApproval];
    }
    else if ([[ExSystem sharedInstance] isTravelAndExpenseOnlyUser])
    {
        [self.homeCollectionView switchLayoutToExpenseAndTravelOnly];
    }
    else if ([[ExSystem sharedInstance] isExpenseAndApprovalOnlyUser]  && ![[ExSystem sharedInstance] hasRole:ROLE_OPEN_BOOKING_USER])
    {
         [self.homeCollectionView switchLayoutToExpenseAndApprovalOnly];
    }
    else if ([[ExSystem sharedInstance] isTravelOnly])
    {
        [self.homeCollectionView switchLayoutToTravelOnly];
    }
    else if ([[ExSystem sharedInstance] isExpenseOnlyUser])
    {
        [self.homeCollectionView switchLayoutToExpenseOnly];
    }
    else if ([[ExSystem sharedInstance] isApprovalOnlyUser])
    {
        [self.homeCollectionView switchLayoutToApprovalOnly];
    }
    else
    {
        [self.homeCollectionView switchLayoutToDefault];
    }

    // Disable features based on sitesettings
    if (![[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals]) {
        // cannot disable the approval cell since it includes travel and invoice approvals
        // TODO : Disable approvals if user is Expenese report approver only and does not have any invoice or travel request approver role. 
        //[self.homeCollectionView disableApprovals];
    }

    if (![[ExSystem sharedInstance] siteSettingAllowsExpenseReports]) {
        [self.homeCollectionView disableExpenseReports];
    }
// MOB-17062 - Open booking users do not have book trips option.
    if (![[ExSystem sharedInstance] siteSettingAllowsTravelBooking] ||  ([[ExSystem sharedInstance] hasRole:ROLE_OPEN_BOOKING_USER] && ![[ExSystem sharedInstance] hasTravelBooking])) {
        // Do we need to disable trips cell too?  If I remember correctly, this is for booking only.
        [self.homeCollectionView disableFlightBooking];
        [self.homeCollectionView disableHotelBooking];
        [self.homeCollectionView disableCarBooking];
        [self.homeCollectionView disableRailBooking];
    }
}

/**
 The layout of the HomeCollectionView could change quite a bit depending on orientation.
 */
- (void)rotateHomeCollectionViewToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    if (UIInterfaceOrientationIsPortrait(toInterfaceOrientation)) {
        [self.homeCollectionView switchToPortrait];
    } else {
        [self.homeCollectionView switchToLandscape];
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    // Handle background to foreground etc states
    if ([[ApplicationLock sharedInstance] isLoggedIn])
	{
        if (requireHomeScreenRefresh)
		{
			//means refresh the home screen 
            [self fetchHomePageDataAndSkipCache:YES];
            requireHomeScreenRefresh = NO;          
        }
        [self checkOffline];
        
        [self pullWhatsNewAndTipsOverlayToFront];

        if(isDataReady)
        {
            [self hideWaitView];
        }

        [self updateBadgeCounts];
        // for displaying mobile tour - only for the new user first time log in
        NSUserDefaults *userDefault = [NSUserDefaults standardUserDefaults];
        if ([[userDefault objectForKey:@"NotFirstTimeLogin"] isEqualToValue:@(NO)] ){
            NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
            [defaultCenter postNotificationName:NotificationOnFirstTimeLogin object:self];
        }

        //MOB-17238 : Test Drive Expiration Dialog
        if (self.accountExpired) {
            // Show an alert message that account is expired and dont let the user do anything else here.
            MobileAlertView *alertView = [[MobileAlertView alloc]
                                          initWithTitle:@"Test Drive expired"
                                          message:@"Your Test Drive account has expired.If you would like to learn more about Concur, please contact us at 1-888-883-8411"
                                          delegate:self
                                          cancelButtonTitle:@"Close"
                                          otherButtonTitles:nil];
            [alertView show];
            alertView.tag = constAccountExpiredAlert;
        }

    }
    else
    {
        [[ApplicationLock sharedInstance] onHomeScreenAppeared];
    }
}

- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
    [self setupNavBar];
    // update topview so isUserLogged in check fails. If Signin Storyboard is presented then the topview is updated again
    ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    appDelegate.topView = HOME_PAGE;

}

- (void)setToolbarBackground
{
    if (![ExSystem is7Plus]) {
        // Set the background image of toolbar here so the user doesnt see the blue transition from other screen
        UIImage *toolbarimage = [UIImage imageNamed:@"bar_bottom"];
        [self.navigationController.toolbar setBackgroundImage:toolbarimage forToolbarPosition:UIToolbarPositionAny barMetrics:UIBarMetricsDefault];
    } else {
        // make background transparent... because the background image doesn't work anymore. :/
        [self.navigationController.toolbar setBackgroundImage:[UIImage new]
                      forToolbarPosition:UIBarPositionAny
                              barMetrics:UIBarMetricsDefault];
        [self.navigationController.toolbar setShadowImage:[UIImage new]
                  forToolbarPosition:UIToolbarPositionAny];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    // Reset Toolbar background for other view
    if (!self.isCameraPress)
    {
        [self.navigationController.toolbar setBackgroundImage:nil forToolbarPosition:UIToolbarPositionAny barMetrics:UIBarMetricsDefault];
    }
    // Reset the offline bar
    if(![ExSystem connectedToNetwork])
    {
    	// Need a better way to check if there was offline bar.
        
        [offlineBackground removeFromSuperview];
    }
}

- (void)viewDidDisappear:(BOOL)animated{
    [super viewDidDisappear:animated];
    
    // let's make sure that the menu is out of the way... application lock mess with home and causes crashes
    [self dismissMenuView];
    [self.transparentView removeFromSuperview];
    [AnalyticsTracker resetScreenName];
}

- (void)pullWhatsNewAndTipsOverlayToFront
{
    // pull this to if this is already built
    if (self.overlayView != nil) {
        [self.navigationController.view bringSubviewToFront:self.overlayView];
    }
    if (self.whatsNewView != nil) {
        [self.navigationController.view bringSubviewToFront:self.whatsNewView];
    }
}

- (void)makeWhatsNewAndTipsOverlay
{
    self.showWhatsNew = [[ExSystem sharedInstance].sys.showWhatsNew boolValue];

    // handle what's new and help overlay
    if (self.showWhatsNew)
    {
        // add help overlay
        self.overlayView = [[OverlayView alloc] initForIPad];
        [self.navigationController.view addSubview:self.overlayView];
        
        /*
        // does not scale for ipad, need ipad version
        self.whatsNewView = [self.rootVC getWhatsNewViewForIPad];
        [self.navigationController.view addSubview:self.whatsNewView];
         */
    }
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
    // probably not necessary -Ernest 11/26/13
    //[self.view setNeedsDisplay];
    
    if ([self.pickerPopOver isPopoverVisible] && [self.pickerPopOver.contentViewController isKindOfClass:[UIImagePickerController class]])
    {
        [self.pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
    }

    [self rotateHomeCollectionViewToInterfaceOrientation:toInterfaceOrientation];
    [[ConcurTestDrive sharedInstance].controller willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}


-(void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    [super didRotateFromInterfaceOrientation:fromInterfaceOrientation];
    [[ConcurTestDrive sharedInstance].controller didRotateFromInterfaceOrientation:fromInterfaceOrientation];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

// Public method so detailsviewcontroller can call this when it cancels a trip
-(void) tripsButtonPressed
{
    if(pickerPopOver != nil)
    {
        [pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
	
    // Check offline and update header
    [self checkOffline];
    
    // If role is disabled then display alert
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_TRAVEL];
    if(entity==nil)
    {
        [self ShowRoleDisabledAlert];
        return;
        
    }
    NSDictionary *dictionary = @{@"Action": @"View Trips"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    if(pickerPopOver != nil)
    {
        [pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
    
    TripsViewController *tripsListVC = [[TripsViewController alloc] initWithNibName:@"TripsView" bundle:nil];
    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:tripsListVC];
    localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    
    [localNavigationController setToolbarHidden:NO];
    [self presentViewController:localNavigationController animated:YES completion:nil];
    [tripsListVC loadTrips];

}


// Show Air booking
-(void)btnBookFlightsPressed:(id)sender
{
    [self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"Book Air"};
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
    
	if(![self checkBookAir])
        return;
    
	if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
	
	AirBookingCriteriaVC *nextController = [[AirBookingCriteriaVC alloc] initWithNibName:@"AirBookingCriteriaVC" bundle:nil];
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	[self presentViewController:localNavigationController animated:YES completion:NULL];

}

// Show Car booking
-(void)btnBookCarPressed:(id)sender
{
    
    [self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"Book Car"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	if(![ExSystem connectedToNetwork])
	{
        [self ShowOfflineAlert];
		return;
	}
    
    
	if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
	

	CarViewController *nextController = [[CarViewController alloc] initWithNibName:@"CarViewController" bundle:nil];
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    [self presentViewController:localNavigationController animated:YES completion:NULL];

}

// Show Hotelbooking
-(void)btnBookHotelPressed:(id)sender
{
    [self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"Book Car"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	if(![ExSystem connectedToNetwork])
	{
        [self ShowOfflineAlert];
		return;
	}
    
    if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
    
    HotelViewController *nextController = [[HotelViewController alloc] initWithNibName:@"HotelViewController" bundle:nil];
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    [self presentViewController:localNavigationController animated:YES completion:NULL];
}

-(void)btnBookRailPressed:(id)sender
{
    [self checkOffline];
    NSDictionary *dictionary = @{@"Action": @"Book Rail"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];

	if(![ExSystem connectedToNetwork])
	{
        [self ShowOfflineAlert];
		return;
	}

    if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}

    TrainBookVC *nextController = [[TrainBookVC alloc] initWithNibName:@"TrainBookVC" bundle:nil];

	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];

	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;

    [self presentViewController:localNavigationController animated:YES completion:NULL];
}


//        kSECTION_EXPENSE_CARDS
// Show expenses
-(void)btnExpensesPressed:(id)sender
{
    // This is single user only
    if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
    // Check offline and update header
    [self checkOffline];
    
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_CARDS];
    if(entity==nil)
    {
        [self ShowRoleDisabledAlert];
        return;
        
    }

    NSDictionary *dictionary = @{@"Action": @"View Card Charges"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    QuickExpensesReceiptStoreVC *nextController = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    [nextController setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:YES allowListEdit:YES];
    
   
 	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
    [localNavigationController setToolbarHidden:NO];
	[self presentViewController:localNavigationController animated:YES completion:nil];

}

// ShowReports
-(void)btnReportsPressed:(id)sender
{
    
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
	
    // Check offline and update header
    [self checkOffline];
    
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_REPORTS];
    if(entity==nil)
    {
        [self ShowRoleDisabledAlert];
        return;
        
    }

    NSDictionary *dictionary = @{@"Action": @"View Reports"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    // drill to the report view from the report list view
	
	ActiveReportListViewController *reportsListVC = [[ActiveReportListViewController alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
	reportsListVC.enablefilterUnsubmittedActiveReports = NO;
	//reportsListVC.iPadHome = self;
	//reportsListVC.isPad = YES;
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:reportsListVC];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	[self presentViewController:localNavigationController animated:YES completion:nil];
	
	[reportsListVC loadReports];
}

// show approvals
-(void)btnApprovalsPressed:(id)sender
{
    [self SwitchToApprovalsView];
}

// Clear cached home screen items
-(void) clearHomeData
{
    
    if(currentTrip != nil)
    {
        self.currentTrip = nil;
    }
    [[HomeManager sharedInstance] clearAll];
}


-(void) didProcessMessage:(Msg *)msg
{
    [self respondToFoundData:msg];
}

-(void)respondToFoundData:(Msg *)msg
{
	// Start wait view if there are any pending servercalls 
    if(servercalls > 0  && !self.isRefreshing)
    {
        isDataReady = NO;
    }
    
    if ([msg.idKey isEqualToString:TRIPS_DATA])
	{
		TripsData* tripsData = (TripsData *)msg.responder;
		[self refreshUIWithTripsData:tripsData];
        servercalls--;
	}
    else if ([msg.idKey isEqualToString:SUMMARY_DATA])
	{
		if ([ExSystem sharedInstance].sessionID == nil)
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
		
        // Check role in SummaryData instead
		self.summaryData = (SummaryData *)msg.responder;
		[self refreshUIWithSummaryData:self.summaryData];
        servercalls--;
        
	}
    else if([msg.idKey isEqualToString:CAR_RATES_DATA])
    {
		self.carRatesData = (CarRatesData*) msg.responder;
        // save this for the report detail screen.  There's a lot of places where code asks root for car rate data.
        // This will make sure it's set correctly.
        [ConcurMobileAppDelegate findRootViewController].carRatesData = self.carRatesData;
        
        // MOB-16587 - This is a hack to notify tabbar that the car mileage is availabe
        // Notify Tab bar about car rates.
        if([self.carRatesData hasAnyPersonalsWithRates:[ExSystem sharedInstance].sys.crnCode])
            [[NSNotificationCenter defaultCenter] postNotificationName:NotificationHasCarRatesData object:self];

	}
    else if ([msg.idKey isEqualToString:IGNITE_USER_INFO_DATA]) {
        [self verifySalesForceUserData];
    }
    
	// close wait view if there are no more servercalls
    if(servercalls==0)
    {
        isDataReady = YES;
        [self hideWaitView];

        // need to close the collection view refresh thing, this is awkward.
        // this is how we handled it in the old UI and it works well enough where I dont want to refactor it.
        if ([self isRefreshing]) {
            self.refreshing = NO;
            if (self.collectionViewRefreshControl) {
                [self.collectionViewRefreshControl endRefreshing];
            }
        }
    }
}

// we could ignore the response since we're not doing anything with it.
-(void) verifySalesForceUserData
{
    if ([[SalesForceUserManager sharedInstance] getAccessToken] == nil || [[SalesForceUserManager sharedInstance] getInstanceUrl] == nil)
    {
        NSLog(@"No salesforce login info!");
        /*
        MobileAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:[Localizer getLocalizedText:@"Error"]
                                      message:@"iPadHome:There was an error accessing your Salesforce account. Please go to Concur web to grant access." // TODO: Localize
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                                      otherButtonTitles:nil];
        [alert show];
         */
    } else {
        NSLog(@"YAY!");
    }
}


# pragma mark -
# pragma mark implementation

- (void)fetchHomePageData
{
    [self fetchHomePageDataAndSkipCache:NO];
}

- (void)fetchHomePageDataAndSkipCache:(BOOL)shouldSkipCache
{
    
    isDataReady = NO;
    servercalls = 0 ;

	// Make server calls only if connected to network 	     
   	if([ExSystem connectedToNetwork])
	{
        if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
        {
            // MOB-13132 : Initialze carmileage data call carrates first so we can show car mileage button sooner
            [self fetchCarRatesAndSkipCache:YES];
        }

        [self getSummaryData:shouldSkipCache];
        [self getTripsData:shouldSkipCache];
        
        if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
		{
            // for offline QE
            // Load the expense types
            ExpenseTypesManager* etMgr = [ExpenseTypesManager sharedInstance];
            [etMgr loadExpenseTypes:nil msgControl:[ExSystem sharedInstance].msgControl];
            
            // Load currencies
            FormFieldData *currencyField = [[FormFieldData alloc] init];
            currencyField.iD = @"TransactionCurrencyName";
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: currencyField, @"FIELD", @"Y", @"MRU", nil];
            [[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
            
		}
        
    }
}


-(void)refreshUIWithSummaryData:(SummaryData*)sd
{
	NSString *reportsToApproveCount = (sd.dict)[@"ReportsToApproveCount"];
    if (![reportsToApproveCount length])
        reportsToApproveCount = @"0";
	NSString *unsubmittedReportsCount = (sd.dict)[@"UnsubmittedReportsCount"];
    if (![unsubmittedReportsCount length])
        unsubmittedReportsCount = @"0";
	NSString *corpCardTransactionCount = (sd.dict)[@"CorporateCardTransactionCount"];
    if (![corpCardTransactionCount length])
        corpCardTransactionCount = @"0";

    NSString *travelRequestsToApprove = (sd.dict)[@"TravelRequestApprovalCount"];
    NSString *invoicesToApprove = (sd.dict)[@"InvoicesToApproveCount"];
    NSString *invoicesToSubmit = (sd.dict)[@"InvoicesToSubmitCount"];
    NSString *tripsToApproveCount = (sd.dict)[@"TripsToApproveCount"];
    NSString *purchaseRequestCount = (sd.dict)[purchaseRequestsToApproveCount];
    
    if (![travelRequestsToApprove length])
        travelRequestsToApprove = @"0";
    
    int iPos = 1;
    EntityHome *entity = nil;
    
    int totalApprovals =  [travelRequestsToApprove intValue] + [invoicesToApprove intValue] + [invoicesToSubmit intValue] + [tripsToApproveCount intValue] + [purchaseRequestCount intValue];
    
    // MOB-13478 - If site settings doesnt allow expense approvals remove approval reports count from the total.
    // Approval counts dont match if user has invoice or request approvals and sitesettings disabled since the approval screen hides request approvals if sitesetting is disabled.
    // To avoid confusion add reportsto approve count to totalapprovals only if sitesettings allows expense report approvals
    if([[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals])
    {
        totalApprovals += [reportsToApproveCount intValue];
    }
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_QUICK];
        entity.name = [Localizer getLocalizedText:@"Quick Expense"];
        entity.subLine  = [Localizer getLocalizedText:@"Capture expense and receipt"];
        entity.key = kSECTION_EXPENSE_QUICK;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = @kSECTION_EXPENSE_POS;
        entity.imageName = @"icon_quickexpense";
        entity.rowPosition = @(iPos);
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON];
        entity.name = [Localizer getLocalizedText:@"Authorizations"];
        entity.subLine  = [Localizer getLocalizedText:@"View and update authorizations"];
        entity.imageName = @"icon_trip_approvals";
        entity.key = kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = @kSECTION_EXPENSE_POS;
        entity.rowPosition = @(iPos);
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_REPORTS];
        entity.name = [Localizer getLocalizedText:@"Vouchers"];
        entity.subLine  = [Localizer getLocalizedText:@"View, create and update vouchers"];
        entity.key = kSECTION_EXPENSE_REPORTS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = @kSECTION_EXPENSE_POS;
        entity.imageName = @"icon_report";
        entity.rowPosition = @(iPos);
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
    }
    else if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
    {
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_REPORTS];
        entity.name = [Localizer getLocalizedText:@"EXPENSE_REPORTS"];
        entity.itemCount = @([unsubmittedReportsCount intValue]);
        
        NSString *sub = nil;
        int unSubReports = [unsubmittedReportsCount intValue];
        if(unSubReports == 0)
        {
            sub = [Localizer getLocalizedText:@"EXPENSE_REPORTS_NEG_TEXT"];
        }
        else
        {
            sub = [NSString stringWithFormat:[Localizer getLocalizedText:@"REPORTS_TO_SUBMIT"], unSubReports];
        }
        
        entity.subLine = sub;
        entity.key = kSECTION_EXPENSE_REPORTS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = @kSECTION_EXPENSES_REPORTS_POS;
        entity.imageName = @"icon_expensereport_ipad";
        entity.rowPosition = @(iPos);
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_CARDS];
            
        entity.name = [Localizer getLocalizedText:@"Expenses"];
        if([corpCardTransactionCount intValue] == 0)
        {
            sub = [Localizer getLocalizedText:@"EXPENSES_NEG_TEXT"];
        }
        else
        {
            sub = [NSString stringWithFormat:@"%@ %@", corpCardTransactionCount, [Localizer getLocalizedText:@"corporate card transactions"]];
        }
        entity.subLine  = sub;
        entity.itemCount = @([corpCardTransactionCount intValue]);
        entity.key = kSECTION_EXPENSE_CARDS;
        entity.sectionValue = kSECTION_EXPENSE;
            
        entity.sectionPosition = @kSECTION_EXPENSES_POS;
        entity.imageName = @"icon_expenses_ipad";
        entity.rowPosition = @(iPos);
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
    }
    else // Delete the rows if user has no role
    {
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_CARDS];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_REPORTS];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];

    }
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_APPROVALS];
        entity.name = [Localizer getLocalizedText:@"Stamp Documents"];
        entity.subLine  = [Localizer getLocalizedText:@"Approve authorizations and vouchers"];
        entity.key = kSECTION_EXPENSE_APPROVALS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = @kSECTION_EXPENSE_POS;
        entity.imageName = @"icon_approval";
        entity.rowPosition = @(iPos);
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_CARDS];
        entity.name = [Localizer getLocalizedText:@"Expenses"];
        entity.subLine  = [Localizer getLocalizedText:@"View unapplied expenses"];
        entity.key = kSECTION_EXPENSE_CARDS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = @kSECTION_EXPENSE_POS;
        entity.imageName = @"icon_card";
        entity.rowPosition = @(iPos);
        [[HomeManager sharedInstance] saveIt:entity];
        iPos ++;
    }
    // Approvals - show approvals if user has any of these roles
    else  if([self hasAnyApprovalsRole])
    {
       // Modules are handled while building table
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_APPROVALS];
        entity.name = [Localizer getLocalizedText:@"Approvals"];
        NSString *sub = nil;
        if(totalApprovals == 0)
        {
            sub = [Localizer getLocalizedText:@"APPROVALS_NEG_TEXT"];
        }
        else
        {
            sub  =   [NSString stringWithFormat:[Localizer getLocalizedText:@"ITEMS_TO_APPROVE"], totalApprovals];
        }
        
        entity.subLine  = sub;
        entity.itemCount = @(totalApprovals);
        entity.key = kSECTION_EXPENSE_APPROVALS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = @kSECTION_EXPENSE_POS;
        entity.imageName = @"icon_approvals_ipad";
        entity.rowPosition = @(iPos);
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
    }
    else 
    {
        //kill off approvals if the user should not have it
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
        if(entity != nil)
        	[[HomeManager sharedInstance] deleteObj:entity];
    }

    // update the new UI counts
    [self updateBadgeCounts];
}

// Get Trips data
- (void) refreshUIWithTripsData: (TripsData *) tripsData
{
	//TripData* currentTrip = nil;
    int upcoming = 0;
    int active = 0;
    
    NSArray *aTrips = [[TripManager sharedInstance] fetchAll];
    
    // MOB-7192 Strip out timezone info in current local time, so that we can compare it with travel time in GMT(w/o timezone info).
    NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
    
    // MOB-5945 Whenever new TripsData comes, we need to reset active/upcoming trips as well as currentTrip
	if ([aTrips count] > 0) /*&& self.currentTrip == nil*/
	{
		for (int i = 0; i < [aTrips count]; i++)
		{
			EntityTrip* trip = (EntityTrip*)aTrips[i];
            // MOB-5945 - Make comparison consistent with the logic in TripsViewController
			if (([trip.tripEndDateLocal compare:now] == NSOrderedDescending) && ([trip.tripStartDateLocal compare:now] == NSOrderedAscending))
            {
                //is the end date of the looped to trip greater than today and is the startdate before now?
                active ++;
                if (self.currentTrip == nil)
                {
                    self.currentTrip = trip;
                }
                else
                {
                    if ([trip.tripStartDateLocal compare:currentTrip.tripStartDateLocal] == NSOrderedAscending)
                    {
                        self.currentTrip = trip;
                    }
                }
            }
		}
	}
    else
        self.currentTrip = nil;  // No trips
    
    
    if ([aTrips count] > 0)
	{
		for (int i = 0; i < [aTrips count]; i++)
		{
			EntityTrip* trip = (EntityTrip*)aTrips[i];
			//NSDate* startDate = [DateTimeFormatter getLocalDate:trip.tripStartDateLocal];
			if ([trip.tripStartDateLocal compare:now] == NSOrderedAscending) //is the start date of the looped to trip greater than today?
				continue; //yup yup
			
            upcoming++;
		}
	}

    //Current Trip Row
	if (currentTrip != nil)
	{
        
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRAVEL];
        entity.name = currentTrip.tripName;
        entity.subLine  = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateForTravelByDate:currentTrip.tripStartDateLocal], [DateTimeFormatter formatDateForTravelByDate:currentTrip.tripEndDateLocal]];
        entity.keyValue = currentTrip.tripKey;
        entity.imageName = @"icon_current_trip";
        entity.sectionValue = kSECTION_TRIPS;
        entity.sectionPosition = @kSECTION_TRIPS_POS;
        entity.rowPosition = @0;
        entity.key = kSECTION_TRAVEL;
        [[HomeManager sharedInstance] saveIt:entity];
	}
    else if ([[HomeManager sharedInstance] fetchHome:kSECTION_TRAVEL] != nil)
    {
        //kill off find travel if the user should not have it
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_TRAVEL];
        [[HomeManager sharedInstance] deleteObj:entity];
    }
    
    //Trips Row
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRAVEL];
    entity.name = [Localizer getLocalizedText:@"Trips"];
    
    NSString *sub = nil;
    if (active==0 && upcoming == 0)
    {
        sub = [[ExSystem sharedInstance] hasTravelBooking] ? [Localizer getLocalizedText:@"TRAVEL_NEG_TEXT"] : [@"You have no upcoming trips" localize];
    }
    else
    {
        sub = [NSString stringWithFormat:[Localizer getLocalizedText:@"int active int upcoming"], active, upcoming];
    }
    // new UI checks the itemCount.  need to set it
    entity.itemCount = [[NSNumber alloc] initWithInt:upcoming];

    entity.subLine = sub;
    entity.key = kSECTION_TRAVEL;
    entity.sectionValue = kSECTION_TRIPS;
    entity.sectionPosition = @kSECTION_TRIPS_POS;
    entity.imageName = @"icon_travel_ipad";
    entity.rowPosition = @1;
    [[HomeManager sharedInstance] saveIt:entity];
    
    int rowPosition = 1;
    
    if (self.isTravelOnly)
    {
        // Add booking sections
        //book Hotel
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:BOOKINGS_BTN_HOTEL];
        entity.name =[Localizer getLocalizedText:@"Hotels"];
        entity.subLine = [Localizer getLocalizedText:@"Hotels_subline"] ;
        entity.key = BOOKINGS_BTN_HOTEL;
        entity.sectionValue = kSECTION_TRIPS;
        entity.imageName = @"icon_hotel";
        entity.rowPosition = @(rowPosition++);
        [[HomeManager sharedInstance] saveIt:entity];
        
        
        //book flight always shows up, site settings/profile status is checked when user clicks on flight booking
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:BOOKINGS_BTN_AIR];
        entity.name = [Localizer getLocalizedText:@"Flights"];
        entity.subLine = [Localizer getLocalizedText:@"Flights_subline"];
        entity.key = BOOKINGS_BTN_AIR;
        entity.sectionValue = kSECTION_TRIPS;
        entity.imageName = @"icon_air";
        entity.rowPosition = @(rowPosition++);
        [[HomeManager sharedInstance] saveIt:entity];
        
        //book car
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:BOOKINGS_BTN_CAR];
        entity.name = [Localizer getLocalizedText:@"Cars"];
        entity.subLine = [Localizer getLocalizedText:@"Cars_subline"];
        entity.key = BOOKINGS_BTN_CAR;
        entity.sectionValue = kSECTION_TRIPS;
        entity.imageName = @"icon_car";
        entity.rowPosition = @(rowPosition++);
        [[HomeManager sharedInstance] saveIt:entity];
        
    }
    else     // Delete any stale rows
    {
        //kill off book travel rows if the user should not have it
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:BOOKINGS_BTN_HOTEL];
        if (entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:BOOKINGS_BTN_AIR];
        if (entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:BOOKINGS_BTN_CAR];
        if (entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];        
        
    }

    // update the new ui counts
    [self updateBadgeCounts];
}


// get the car rate data
- (void)fetchCarRatesAndSkipCache:(BOOL)shouldSkipCache
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	[[ExSystem sharedInstance].msgControl createMsg:CAR_RATES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache: shouldSkipCache RespondTo:self];
}

// Place a server call to get summary data
// Move these to a common homeutilities object
-(void)getSummaryData:(BOOL)shouldSkipCache
{
    if([self hasAnyExpenseRole])
    {
        
        NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
        
        // MOB-13374 mark REPORT_APPROVAL_LIST_DATA as expired so if the approver tap on it it can find new data
        //MOB-15536 - Call only if user has the Expense Approver role
        if( [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER])
            [[ExSystem sharedInstance].cacheData markAsNeedingRefresh:REPORT_APPROVAL_LIST_DATA UserID:[ExSystem sharedInstance].userName RecordKey:@"0"];
        
        [[ExSystem sharedInstance].msgControl createMsg:SUMMARY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:shouldSkipCache RespondTo:self];
        
        // Load the expenses
        NSMutableDictionary* pBag3 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
        [[ExSystem sharedInstance].msgControl createMsg:ME_LIST_DATA() CacheOnly:@"NO" ParameterBag:pBag3 SkipCache:shouldSkipCache RespondTo:self];
        
        if(shouldSkipCache) {
            servercalls++;
         }
            
    }
    else // if user is travel only then coredata might have other info left out
    {
        // Delete QE/Expenses/Reports rows from home.
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_QUICK];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_CARDS];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_REPORTS];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        //kill off approvals if the user should not have it
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
    }
    
}

//Place a server call to get Trips data
// Move these to a common homeutilities object
-(void)getTripsData:(BOOL)shouldSkipCache
{
    if (([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] || [[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER] || [[ExSystem sharedInstance] hasRole:ROLE_OPEN_BOOKING_USER]))
    {
        NSMutableDictionary* pBag2 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag2 SkipCache:shouldSkipCache RespondTo:self];
        
         if(shouldSkipCache)
         {
             servercalls++;
         }
    }
    else
    {
        //remove travel data if the user does not have role
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_TRAVEL];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
    }
    
}


# pragma mark -
# pragma mark Refresh and utility methods

// Return whether the task is completed
// Implement pulldown refresh
-(BOOL) refreshView:(UIRefreshControl*) refresh
{
    self.refreshing = YES;
    NSString *title = [Localizer getLocalizedText:@"Pull to Refresh"];
    refresh.attributedTitle = [[NSAttributedString alloc] initWithString:title];
    self.collectionViewRefreshControl = refresh;
    
    // MOB-16947 'Pull to Refresh' string is overlapping the spinner dialogue.
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.collectionViewRefreshControl beginRefreshing];
        [self.collectionViewRefreshControl endRefreshing];
    });
    
    NSDictionary *dictionary = @{@"Action": @"Refresh Data"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    [self fetchHomePageDataAndSkipCache:YES];

    // end refreshing view later
    return NO;
}


// Get everything all over again
-(void) forceReload
{
    requireHomeScreenRefresh = YES;
    isDataReady = NO;
    // Force request new data from server
    [self fetchHomePageDataAndSkipCache:YES];
    //[self performSelector:@selector(refreshTableViews) withObject:nil afterDelay:0.1f];
}


// Post a server call to refresh Trips data
// Temp fix only.Server call is actually not needed here. We can update coredata when a new trip is added.
-(void) refreshTripsData
{
    if ([ExSystem connectedToNetwork])
    {
        [self getTripsData:YES];
        //[self performSelector:@selector(refreshTableViews) withObject:nil afterDelay:0.01f];
    }
    
    
}

// Post a server call to refresh summary data
// Temp fix only. Server call is actually not needed here. we can simply update core data when summary is udpated
-(void) refreshSummaryData
{
    if ([ExSystem connectedToNetwork])
    {
        [self getSummaryData:YES];
        //[self performSelector:@selector(refreshTableViews) withObject:nil afterDelay:0.01f];
    }
}

/**
 Updates Badge counts with info from Core Data.  The core data values are populated by old code that probably should be refactored.
 */
- (void)updateBadgeCounts
{
    [self updateExpenseReportsBadgeCount];
    [self updateExpensesBadgeCount];
    [self updateApprovalsBadgeCount];
    [self updateTripsBadgeCount];
}

/**
 Updates the Expense Report badge count.
 */
- (void)updateExpenseReportsBadgeCount
{
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_REPORTS];
    NSNumber *count = entity.itemCount;
    if (!count) {
        count = [[NSNumber alloc] initWithInt:0];
    }
    [self.homeCollectionView setExpenseReportsCount:count];
}

/**
 Updates the Expenses badge count.
 */
- (void)updateExpensesBadgeCount
{
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_CARDS];
    NSNumber *count = entity.itemCount;
    if (!count) {
        count = [[NSNumber alloc] initWithInt:0];
    }
    [self.homeCollectionView setExpensesCount:count];
}

/**
 Updates the Approvals badge count.
 */
- (void)updateApprovalsBadgeCount
{
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
    NSNumber *count = entity.itemCount;
    if (!count) {
        count = [[NSNumber alloc] initWithInt:0];
    }
    [self.homeCollectionView setApprovalCount:count];
}

/**
 Updates the Trips badge count.
 */
- (void)updateTripsBadgeCount
{
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_TRAVEL];
    NSNumber *count = entity.itemCount;
    if (!count) {
        count = [[NSNumber alloc] initWithInt:0];
    }
    [self.homeCollectionView setTripsCount:count];
}

#pragma mark - Handle Login view
/**
 Displays login view or test drive if the user opened the app for first time
 */
-(void) showManualLoginView
{
    if ([ConcurMobileAppDelegate isLoginViewShowing])
        return;
    
    if ([Config isNewSignInFlowEnabled]) {
        SignInUserTypeViewController *lvc = [[UIStoryboard storyboardWithName:@"SignIn_iPad" bundle:nil] instantiateInitialViewController];
        self.signInViewNavigationController = [[UINavigationController alloc] initWithRootViewController:lvc];
    }
    else
    {
        // MOB-16161- Load new login UI , new login UI is in a storyboard.
        LoginViewController* lvc = [[UIStoryboard storyboardWithName:@"Login_iPad" bundle:nil] instantiateInitialViewController];
        self.signInViewNavigationController = [[UINavigationController alloc] initWithRootViewController:lvc];
        lvc.loginDelegate = self;
    }
    
	[self presentViewController:self.signInViewNavigationController animated:YES completion:nil];
	ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    delegate.topView = LOGIN;


    // if User logged in ever ie has saved user id then dont show testdrive storyboard.
    // Just display the login screen if user ever logged in or if the user is SSO user. SSO users should not see the signin/Test drive start up screen
    // MOB-16425: The ExSystem.isCorpSSOUser is set to "NO" when SSO user logs out so check if SSO url exists to check if user is SSO.
    if( ([ExSystem sharedInstance].entitySettings.saveUserName != nil &&
         [[ExSystem sharedInstance].entitySettings.saveUserName isEqualToString:@"YES"] &&
         [[ExSystem sharedInstance].userName lengthIgnoreWhitespace] > 0) || [[[ExSystem sharedInstance] loadCompanySSOLoginPageUrl] lengthIgnoreWhitespace])
    {
        [[MCLogging getInstance] log:@"iPadHome9vc::showManualLoginView: Showing Login storyboard." Level:MC_LOG_INFO];
        return ;
    }
    
    // TODO : this would need to be refactored and move to application did finish lauching so that we can consolidate one time show of stuffs
    [[NSUserDefaults standardUserDefaults] setObject:@(NO) forKey:@"NotFirstTimeLogin"];

    [[ConcurTestDrive sharedInstance] showTestDriveAnimated:YES];
}

/**
 Segue to password reset screen
 */
-(void) showPasswordRestScreen
{

    [[MCLogging getInstance] log:[NSString stringWithFormat:@"iPadHome9VC::showPasswordRestScreen"] Level:MC_LOG_DEBU];
    [[ConcurTestDrive sharedInstance] removeAnimated:NO];
    // If user is already seeing the login storyboard then use the storboard to segue to the reset screen.
    SignInUserTypeViewController *initialViewController = nil;
    if (self.signInViewNavigationController == nil) {
        initialViewController = [[UIStoryboard storyboardWithName:[@"SignIn" storyboardName] bundle:nil] instantiateViewControllerWithIdentifier:@"ResetPasswordScreen"];
        self.signInViewNavigationController = [[UINavigationController alloc] initWithRootViewController:initialViewController];
        [initialViewController performSegueWithIdentifier:@"ShowPasswordResetScreen" sender:self];
        [self presentViewController:self.signInViewNavigationController animated:YES completion:nil];
        
    }
    else
    {
        // segue to reset screen only if its not showing. check whats visible on stack
        if (![[self.signInViewNavigationController visibleViewController] isMemberOfClass:[SignInResetPasswordViewController class]] ) {
            initialViewController = [self.signInViewNavigationController.viewControllers objectAtIndex: 0];
            [initialViewController performSegueWithIdentifier:@"ShowPasswordResetScreen" sender:self];
        }
    }
    
}

/**
 Segue to SignInScreen screen
 */
-(void) showSignInScreen
{
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Home9VC::showSignInScreen"] Level:MC_LOG_DEBU];
    [[ConcurTestDrive sharedInstance] removeAnimated:NO];
    // If user is already seeing the login storyboard then use the storboard to segue to the reset screen.
    SignInUserTypeViewController *initialViewController = nil;
    if (self.signInViewNavigationController == nil) {
        initialViewController = [[UIStoryboard storyboardWithName:[@"SignIn" storyboardName] bundle:nil] instantiateInitialViewController];
        self.signInViewNavigationController = [[UINavigationController alloc] initWithRootViewController:initialViewController];
        [self presentViewController:self.signInViewNavigationController animated:YES completion:nil];
    }
    else
    {
        // check whats visible on stack , if Signin screen is not showing then pop to sign in screen
        if (![[self.signInViewNavigationController visibleViewController] isMemberOfClass:[SignInUserTypeViewController class]] ) {
            [self.signInViewNavigationController popToRootViewControllerAnimated:YES];
        }
    }
    
}


-(void)showTestDriveStoryBoard
{
    [[ConcurTestDrive sharedInstance] popTestDriveAnimated:YES];
}

-(void)removeTestDriveStoryBoard
{
    [[MCLogging getInstance] log:@"iPadHome9vc::removeTestDriveStoryBoard: Closing TestDrive storyboard." Level:MC_LOG_INFO];
    
    UIWindow *currentWindow = [[[UIApplication sharedApplication] delegate] window];
    
    [currentWindow makeKeyAndVisible];
    currentWindow.alpha = 0;
    [UIView animateWithDuration:0.3 animations:^{
        currentWindow.alpha = 1;
    }];
    [[ConcurTestDrive sharedInstance] removeAnimated:YES];
}

#pragma  mark - login delegate
/**
This method actally removes the login view and not the home view itself
*/

- (void)dismissYourself:(UIViewController*)vc;
{
    [vc dismissViewControllerAnimated:YES completion:nil];
    [self removeTestDriveStoryBoard];
}

- (void)setupNavBar
{
    
    UIImageView *img = nil;
    
    if ([ExSystem is7Plus])
        img = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"logo-header"]];
    else
        img = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"concur_logo_titlebar"]];
    self.title = [Localizer getLocalizedText:@"Home"];
    
    
    self.navigationItem.titleView = img;
    // Check offline and update header
     [self checkOffline];
    
    // Wire up Moremenu and messageCenter actions
    SEL moreMenuSelector = @selector(showMoreMenu:);
    SEL messageCenterSelector = @selector(showMessageCenter:);
    // Add More menu and Message center buttons.

    if ([ExSystem is7Plus])
    {
        self.navigationItem.leftBarButtonItem = [self getNavBarButtonWithImage:@"icon_menu_iOS7" withSelector:moreMenuSelector];
        
        [self setMessageCenterIcon];
    }
    else
    {
        self.navigationItem.leftBarButtonItem = [self getNavBarButtonWithImage:@"icon_menu_ipad" withSelector:moreMenuSelector];
        self.navigationItem.rightBarButtonItem = [self getNavBarButtonWithImage:@"icon_messagecenter_iOS6" withSelector:messageCenterSelector];
    }
    
}

-  (void)setMessageCenterIcon {
    MessageCenterManager *messageCenterManager = [MessageCenterManager sharedInstance];
    
    if ([messageCenterManager numMessagesForType:MessageTypeUnread] > 0) {
        self.navigationItem.rightBarButtonItem = [self getNavBarButtonWithImage:@"icon_messagecenter_badged" withSelector:@selector(showMessageCenter:)];
    } else {
        self.navigationItem.rightBarButtonItem = [self getNavBarButtonWithImage:@"icon_messagecenter_iOS7" withSelector:@selector(showMessageCenter:)];
    }
}

// Nav bar buttons
-(UIBarButtonItem *)getNavBarButtonWithImage:(NSString *)imgName withSelector:(SEL)selectorName
{
    UIButton* mbtn =[UIButton buttonWithType:UIButtonTypeCustom];
    UIImage* mImage = [UIImage imageNamed:imgName];
    [mbtn addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
    [mbtn setImage:mImage forState:UIControlStateNormal];
    mbtn.frame = CGRectMake(0, 0, mImage.size.width, mImage.size.height);
    UIBarButtonItem *menuButton = [[UIBarButtonItem alloc]initWithCustomView:mbtn];
    
    return menuButton;
    
}

// Create custom button for toolbar
-(UIBarButtonItem *)getCustomBarButton:(NSString *)imgName withSelector:(SEL)selectorName isEnabled:(BOOL)isEnabled
{
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];;
    UIBarButtonItem *barButton = nil;

    UIImage *buttonImage = [UIImage imageNamed:imgName];
    [button setImage:buttonImage forState:UIControlStateNormal];
    
    button.bounds = CGRectMake(0,0,buttonImage.size.width, buttonImage.size.height);

    [button addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
    button.contentVerticalAlignment = UIControlContentVerticalAlignmentTop;
   
    barButton = [[UIBarButtonItem alloc] initWithCustomView:button];
    
    barButton.enabled = isEnabled;
    return barButton;
}


//MOB-13130: Create custom button for toolbar
-(UIBarButtonItem *)getCustomBarButton:(NSString *)imgName withTitle:(NSString *)title withSelector:(SEL)selectorName isEnabled:(BOOL)isEnabled
{
    if (![ExSystem is7Plus])
    {
        UIImage *toolbarimage = [UIImage imageNamed: @"bar_bottom"];
        UIImage *imgBtnBackground = [UIImage imageNamed:@"buttons_gray_blank"] ;
        
        UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
        [button setImage:[UIImage imageNamed:imgName] forState:UIControlStateNormal];
        [button setTitle:title forState:UIControlStateNormal];
        [button.titleLabel setFont:[UIFont fontWithName:@"HelveticaNeue-Bold" size:17.0f]];
        [button setTitleColor:[UIColor colorWithRed:22/255.f green:66/255.f blue:113/255.f alpha:1.f] forState:UIControlStateNormal];
        [button setBackgroundImage:imgBtnBackground forState:UIControlStateNormal];
        [button sizeToFit];
        [button addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
        
        UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, button.bounds.size.width,toolbarimage.size.height)]; // This is to properly align the button in the larger toolbar, else you can create custom UIBarButton directly from UIButton
        [view addSubview:button];
        UIBarButtonItem *barButton = [[UIBarButtonItem alloc] initWithCustomView:view];
        barButton.enabled = isEnabled;
        return barButton;
    }
    else
    {
        UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
        [button setImage:[UIImage imageNamed:imgName] forState:UIControlStateNormal];
        [button setTitle:title forState:UIControlStateNormal];
        [button setTitleColor:[UIColor colorWithRed:22/255.f green:66/255.f blue:113/255.f alpha:1.f] forState:UIControlStateNormal];
        [button sizeToFit];
        [button addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
        UIBarButtonItem *barBtn = [[UIBarButtonItem alloc] initWithCustomView:button];
        barBtn.enabled = isEnabled;
        return barBtn;
    }
    
}

-(void) bookingsActionPressed:(id)sender
{
    if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Bookings offline"]
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return;
	}
    
    if ([ExSystem is8Plus] && [UIDevice isPad]) {
        TravelBookingAlertController *bookingAlert = [[TravelBookingAlertController alloc] initWithNavigationController:self.navigationController];
        CGRect rect = CGRectMake(95, self.navigationController.toolbar.frame.origin.y-44, 1, 1);
        [bookingAlert showInRect:rect withViewController:self withSender:sender];
    }
    else {
        MobileActionSheet *action = [[MobileActionSheet alloc] initWithTitle:nil
                                                                    delegate:self
                                                           cancelButtonTitle:nil
                                                      destructiveButtonTitle:nil
                                                           otherButtonTitles: nil];
        NSMutableArray* btnIds = [[NSMutableArray alloc] init];
        
        [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Air"]];
        [btnIds addObject:BOOKINGS_BTN_AIR];
        [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Hotel"]];
        [btnIds addObject:BOOKINGS_BTN_HOTEL];
        [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Car"]];
        [btnIds addObject:BOOKINGS_BTN_CAR];
        
        if([[ExSystem sharedInstance] canBookRail])
        {
            [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Rail"]];
            [btnIds addObject:BOOKINGS_BTN_RAIL];
        }
        
        [action addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
        action.cancelButtonIndex = [btnIds count];
        
        action.btnIds = btnIds;
        
        action.tag = BOOKINGS_ACTION_TAG;

        // Hard code location for action sheet
        // This is only known way to make trip booking sheet shows at the right location on iPad
        // TODO: Handle rotation while action sheet is showing
        if (self.interfaceOrientation == UIInterfaceOrientationPortrait)
        {
            [action showFromRect:CGRectMake(95, 965, 1, 1) inView:[UIApplication sharedApplication].keyWindow animated:NO];     // portrait upside down
        }
        else if (self.interfaceOrientation == UIInterfaceOrientationPortraitUpsideDown)
        {
            [action showFromRect:CGRectMake(670, 65, 1, 1) inView:[UIApplication sharedApplication].keyWindow animated:NO];     // portrait upside down
        }
        else if(self.interfaceOrientation == UIInterfaceOrientationLandscapeLeft)
        {
            [action showFromRect:CGRectMake(700, 895, 1, 1) inView:[UIApplication sharedApplication].keyWindow animated:NO];     // portrait upside down
        }
        else if (self.interfaceOrientation == UIInterfaceOrientationLandscapeRight)
        {
            [action showFromRect:CGRectMake(70, 128, 1, 1) inView:[UIApplication sharedApplication].keyWindow animated:NO];     // portrait upside down
        }
    }
}

// show new quick expense form
-(void)btnQuickExpensePressed:(id)sender
{
    NSDictionary *dictionary = @{@"Action": @"Quick Expense"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    // Check offline and update header
    [self checkOffline];

    QEFormVC *vc = [[QEFormVC alloc] initWithEntryOrNil:nil withCloseButton:YES];
    
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:vc];
    navi.modalPresentationStyle = UIModalPresentationFormSheet;
    
    [self presentViewController:navi animated:YES completion:NULL];
    
    NSDictionary *dict = @{@"Came From": @"Home"};
    [Flurry logEvent:@"Mobile Entry: Create2" withParameters:dict];
}

// Show car mileage ui - This could be a common utility method
-(void)btnCarMileagePressed:(id)sender
{
    NSDictionary *dictionary = @{@"Action": @"Car mileage"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    NSDictionary *dictionary1 = @{@"Add from": @"Home"};
    [Flurry logEvent:@"Car Mileage: Add from" withParameters:dictionary1];

    // Check offline and update header
    [self checkOffline];
    
    // If reports are disabled then user cannot do the car mileage also 
    if(![[ExSystem sharedInstance] siteSettingAllowsExpenseReports])    // Check site settings
    {
        [self ShowModuleDisabledAlert];
        return;
    }

    if(![ExSystem connectedToNetwork])
    {
        [self ShowOfflineAlert];
        return;
    }


  	//takes you to the select report view
	SelectReportViewController * pVC = [[SelectReportViewController alloc] initWithNibName:@"SelectReportViewController" bundle:nil];
	pVC.meKeys = nil;
	pVC.pctKeys = nil;
	pVC.cctKeys = nil;
	pVC.meAtnMap = nil;
	pVC.isCarMileage = YES;
	
	if([UIDevice isPad])
	{
		UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:pVC];
		localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
		[localNavigationController setToolbarHidden:NO];

        [self presentViewController:localNavigationController animated:YES completion:NULL];

	}
}


- (void)cameraPressed:(id) sender
{
    BOOL isDeviceHasCamera = [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera];
    if (!isDeviceHasCamera)
        [self showPhotoAlbum];
    else{
        if (rsuHelper == nil)
        {
            rsuHelper = [[ReceiptStoreUploadHelper alloc] init];
            rsuHelper.openReceiptListWhenFinished = true;
            rsuHelper.vc = self;
        }
        self.isCameraPress = TRUE;
        [rsuHelper startCamera:nil];
    }
}

-(void)showPhotoAlbum
{
    CCImagePickerViewController *picker = [[CCImagePickerViewController alloc] init];
    picker.albumSelected = YES;
    
    if (rsuHelper == nil)
    {
        rsuHelper = [[ReceiptStoreUploadHelper alloc] init];
        rsuHelper.openReceiptListWhenFinished = true;
        rsuHelper.vc = self;
    }
    
    [picker setCancel:^(CCImagePickerViewController *picker) {
        if (self.pickerPopOver !=nil)
            [self.pickerPopOver dismissPopoverAnimated:YES];
        else
            [self dismissViewControllerAnimated:YES completion:nil];
    }];
    
    [picker setRetake:^(CCImagePickerViewController *picker, NSDictionary *info) {
        [pickerPopOver dismissPopoverAnimated:YES];
        [self cameraPressed:nil];
    }];
    
    [picker setDone:^(CCImagePickerViewController *picker, NSDictionary *info) {
        UIImage* smallerImage = [picker restrictImageSize:[info objectForKey:UIImagePickerControllerOriginalImage]];
        [rsuHelper didTakePicture: smallerImage];
        [self.pickerPopOver dismissPopoverAnimated:YES];
        
    }];
    
    [picker setExpense:^(CCImagePickerViewController *picker, NSDictionary *info) {
        rsuHelper.picker = picker;
        UIImage* smallerImage = [picker restrictImageSize:[info objectForKey:UIImagePickerControllerOriginalImage]];
        [rsuHelper showQuickExpense:smallerImage];
    }];
    
    picker.picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    picker.picker.delegate = self;
    self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:picker];
    CGRect rect = CGRectMake(self.navigationController.toolbar.frame.size.width/2, self.navigationController.toolbar.frame.origin.y-44, 1, 1);
    [pickerPopOver presentPopoverFromRect:rect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES];
}

// Place holder for message center action
-(void)showMessageCenter:(id)sender
{
    // Check offline and update header
    [self checkOffline];
    
    if(![ExSystem connectedToNetwork])
    {
        [self ShowOfflineAlert];
        return;
    }

}

-(void)dismissMenuView
{
    if (self.moreMenuCtrlNav != nil)
    {
        [self.moreMenuCtrlNav willMoveToParentViewController:nil];
        [[self.moreMenuCtrlNav view] removeFromSuperview];
        [self.moreMenuCtrlNav removeFromParentViewController];
    }
}

#pragma mark - Upload and offline

-(void) showUploadViewController
{
    UploadQueueViewController *vc = [[UploadQueueViewController alloc] initWithNibName:@"UploadQueueViewController" bundle:nil];
    UIBarButtonItem *btnUpload = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Upload"] style:UIBarButtonItemStyleBordered target:vc action:@selector(startUpload)];
    vc.title = [Localizer getLocalizedText:@"Upload Queue"];
    UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:vc action:@selector(closeMe:)];

    vc.navigationItem.rightBarButtonItem = btnUpload;
    vc.navigationItem.leftBarButtonItem = btnClose;

    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:vc];

    [navi setModalPresentationStyle:UIModalPresentationFormSheet];
    [self presentViewController:navi animated:YES completion:NULL];
}

-(void) checkOffline
{
	// MOB-16557
    // Don't do the offline bar for now. PM is working on a new design.
    // Also we already have a popup to notify the user.
//    if(![ExSystem connectedToNetwork])
//    {
//        [self makeOfflineHeader];
//        [self.navigationController.view addSubview:offlineBackground];
//    }
//    else
//    {
//        [offlineBackground removeFromSuperview];
//        offlineBackground = nil;
//    }
}

// Create an offline bar - TODO : text should be white color
-(UIView *)makeOfflineHeader
{
    if (offlineBackground == nil || offlineText == nil)
    {
        if ([ExSystem isLandscape])
        {
            offlineBackground = [[UIView alloc] initWithFrame:CGRectMake(0, 62, 1024, 18)];
            offlineText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 1024, 18)];
        }
        else
        {
            offlineBackground = [[UIView alloc] initWithFrame:CGRectMake(0, 62, 768, 18)];
            offlineText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 768, 18)];
        }
        
        offlineBackground.autoresizingMask = (UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin);
        offlineText.autoresizingMask = (UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin);
    }
    
    NSString *offlinemsg = [Localizer getLocalizedText:@"Offline"];
    
    [offlineBackground setBackgroundColor:[UIColor redColor]];
    offlineText.text = offlinemsg;
    
    offlineText.textColor = [UIColor whiteColor];
    offlineText.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:17.0f];
    offlineText.textAlignment =  NSTextAlignmentCenter;
    offlineText.backgroundColor = [UIColor clearColor];
    CGPoint point = CGPointMake(CGRectGetMidX([self.navigationController.navigationBar bounds]), CGRectGetMidY([offlineBackground bounds]));
    [offlineText setCenter:point];
    [offlineBackground addSubview:offlineText];
    [offlineBackground bringSubviewToFront:offlineText];
    
    return offlineBackground;
}

#pragma mark - alerts


-(BOOL) checkBookAir
{
    NSString* msg = nil;
    if (!([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_AIR_BOOKING_ENABLED]))
    {
        msg = [Localizer getLocalizedText:@"AIR_BOOKING_DISABLED_MSG"];
    }
    else
    {
        NSString* profileStatus = [[ExSystem sharedInstance] getUserSetting:@"ProfileStatus" withDefault:@"0"];
        // MOB-10390 Allow users with profileStatus 1 (missing middlename, gender) to go ahead and search air.
        if (![profileStatus isEqualToString:@"0"] && ![profileStatus isEqualToString:@"1"])
        {
            if ([profileStatus isEqualToString:@"20"])
                profileStatus = @"2";
            NSString* msgKey = [NSString stringWithFormat:@"AIR_BOOKING_PROFILE_%@_MSG", profileStatus];
            msg = [NSString stringWithFormat:@"%@\n\n%@", [Localizer getLocalizedText:msgKey], [@"AIR_BOOKING_PROFILE_PROLOG_MSG" localize]];
        }
        else
            return TRUE;
    }
    
    MobileAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:nil
                              message:msg
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                              otherButtonTitles:nil];
    [alert show];
    
    return FALSE;
}


// Checks and displays alert if offline
-(void)ShowOfflineAlert
{
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"Operation Not Supported Offline"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                              otherButtonTitles:nil];
        [alert show];
}

-(void)ShowRoleDisabledAlert
{
    // Travel cell is disabled so show disabled alert.
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"ROLE_DISABLED_ALERT_TITLE"]
                          message:[Localizer getLocalizedText:@"ROLE_DISABLED_TEXT"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                          otherButtonTitles:nil];
    [alert show];
    
}

-(void)ShowModuleDisabledAlert
{
    // Travel cell is disabled so show disabled alert.
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"MODULE_DISABLED_ALERT_TITLE"]
                          message:[Localizer getLocalizedText:@"MODULE_DISABLED_ALERT_TEXT"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                          otherButtonTitles:nil];
    [alert show];
    
}


-(void)SwitchToApprovalsView
{
    // Approvals
    NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"View Report Approval", @"Action", nil];
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
	
    // Check offline and update header
    [self checkOffline];
    
    if ([self hasAnyApprovalsRole] || [[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
        ReportApprovalListViewController *approvalsListVC = [[ReportApprovalListViewController alloc] initWithSummaryData:self.summaryData];
        
        UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:approvalsListVC];
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        [localNavigationController setToolbarHidden:NO];

        [self presentViewController:localNavigationController animated:YES completion:nil];
        // Calling loadApprovals *after* the popover is presented, because the view controller will only receive the message response if it's visible.
        //[approvalsListVC loadApprovals]; // This call is now made in ViewDidLoad in ReportApprovalListViewController
        [self.navigationController.view bringSubviewToFront:approvalsListVC.view];
    }
    else
    {
        [self ShowRoleDisabledAlert];
    }
}

#pragma mark -
#pragma mark UIActionSheetDelegate
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (actionSheet.tag == BOOKINGS_ACTION_TAG)
	{
        if (buttonIndex != actionSheet.cancelButtonIndex)
        {
            MobileActionSheet* mas = (MobileActionSheet*) actionSheet;
            NSString* btnId = [mas getButtonId:buttonIndex];
            [self.navigationController setToolbarHidden:YES animated:YES];
            
            if ([BOOKINGS_BTN_HOTEL isEqualToString:btnId])
            {
                [HotelViewController showHotelVC:self.navigationController withTAFields:nil];
            }
            else if ([BOOKINGS_BTN_CAR isEqualToString:btnId])
            {
                [CarViewController showCarVC:self.navigationController withTAFields:nil];
            }
            else if ([BOOKINGS_BTN_RAIL isEqualToString:btnId])
            {
                [TrainBookVC showTrainVC:self.navigationController withTAFields:nil];
            }
            else if ([BOOKINGS_BTN_AIR isEqualToString:btnId])
            {
                if ([self checkBookAir])
                {
                    [AirBookingCriteriaVC showAirVC:self.navigationController withTAFields:nil];
                }
            }
        }
	}
}

// MOB-17238 : Test Drive Expiration
-(void)handleAccountExpiration
{
    self.accountExpired = YES;
}


- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:NotificationOnAccountExpired object:nil];
    
}

/**
 Handle alert message buttons
 */
- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == constAccountExpiredAlert)
    {
        if (buttonIndex == alertView.cancelButtonIndex)
        {
            // Call logout
            //MOB-17238 : Test Drive Expiration
            [[ApplicationLock sharedInstance] onLogoutButtonPressed];
            self.accountExpired = NO;
        }
        
    }
    
}

@end
