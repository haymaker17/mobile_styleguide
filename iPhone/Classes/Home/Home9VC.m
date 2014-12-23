//
//  Home9VC.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 2/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXClient.h"
#import "HotelRecommendationsRequestFactory.h"
#import "RXMLElement.h"

#import <QuartzCore/QuartzCore.h>
#import "ApplicationLock.h"
#import "Home9VC.h"
#import "TripsViewController.h"
#import "LoginViewController.h"
#import "HotelViewController.h"
#import "TrainBookVC.h"
#import "CarViewController.h"
#import "TripItAuthVC.h"

#import "ReceiptStoreUploadHelper.h"

#import "UploadQueue.h"
#import "UploadQueueViewController.h"
#import "QuickExpensesReceiptStoreVC.h"

#import "MessageCenterViewController.h"

#import "WhatsNewView.h"
#import "OverlayView.h"
#import "MoreMenuViewController.h"
#import "MoreMenuData.h"
#import "TourVC.h"
#import "CarMileageDataLoader.h"
#import "SelectReportViewController.h"

#import "Config.h"
#import "NotificationController.h"
#import "HomeDataProvider.h"

#import "HelpOverlayFactory.h"

// decouple the layout from the data
#import "HomeTableViewCellDescription.h"
#import "HomeTableViewCellDescriptionFactory.h"
// RVC
#import "CarRatesData.h"
#import "SignInUserTypeViewController.h"
#import "SignInResetPasswordViewController.h"
#import "SafariLoginViewController.h"
#import "HotelSearchTableViewController.h"

#import "ActiveRequestListViewController.h"

#import "AnalyticsTracker.h"

@interface Home9VC ()

// decouple the layout from the data.
@property (nonatomic, readwrite, strong) NSMutableArray *cellDescriptions;
@property (nonatomic, strong) EntityTrip                    *currentTrip;
@property (nonatomic, strong) WhatsNewView *whatsNewView;
@property (nonatomic, strong) OverlayView *overlayView;
@property (nonatomic, strong) HomeDataProvider *dataprovider;
@property BOOL showWhatsNew;
@property BOOL requireHomeScreenRefresh, requireTripsDataRefresh, requireSummaryDataRefresh;
@property BOOL accountExpired;

- (BOOL) checkBookAir;

@end

@implementation Home9VC

@synthesize currentTrip, rootVC, requireHomeScreenRefresh,requireTripsDataRefresh, requireSummaryDataRefresh;

const int BOOKINGS_ACTIONSHEET_TAG = 101;
static int const constAccountExpiredAlert = 102;


#pragma mark -
#pragma mark Init Methods

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

// The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];    
    if (self)
    {
        requireHomeScreenRefresh = requireTripsDataRefresh = requireSummaryDataRefresh = YES;
    }
    return self;
}

/**
 Update the table view based on roles
 */
- (void)updateTableViewLayout
{
    // Overall Home layout
    // Uses ExSystem to figure out what layout to use
    if ([[ExSystem sharedInstance] isTravelAndApprovalOnlyUser])
    {
        self.cellDescriptions = [HomeTableViewCellDescriptionFactory descriptionsForTravelandApprovalWithRail:[[ExSystem sharedInstance] canBookRail]];
    }
    else if ([[ExSystem sharedInstance] isTravelAndExpenseOnlyUser])
    {
        self.cellDescriptions = [HomeTableViewCellDescriptionFactory descriptionsForExpenseAndTravelOnly];
    }
    else if ([[ExSystem sharedInstance] isExpenseAndApprovalOnlyUser]  && ![[ExSystem sharedInstance] hasRole:ROLE_OPEN_BOOKING_USER])
    {
        self.cellDescriptions = [HomeTableViewCellDescriptionFactory descriptionsForExpenseAndApprovalOnly];
    }
    else if ([[ExSystem sharedInstance] isTravelOnly])
    {
        self.cellDescriptions = [HomeTableViewCellDescriptionFactory descriptionsForTravelOnlyWithRail:[[ExSystem sharedInstance] canBookRail]];
    }
    else if ([[ExSystem sharedInstance] isExpenseOnlyUser])
    {
        self.cellDescriptions = [HomeTableViewCellDescriptionFactory descriptionsForExpenseOnly];
    }
    else if ([[ExSystem sharedInstance] isApprovalOnlyUser])
    {
        self.cellDescriptions = [HomeTableViewCellDescriptionFactory descriptionsForApprovalOnly];
    }
    else
    {
        self.cellDescriptions = [HomeTableViewCellDescriptionFactory descriptionsForDefault];
    }

    // Disable features based on sitesettings
    if (![[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals]) {
        // cannot disable the approval cell since it includes travel and invoice approvals
        //[self disableCellWithType:ApprovalsHomeCell];
    }

    if (![[ExSystem sharedInstance] siteSettingAllowsExpenseReports]) {
        [self disableCellWithType:ExpenseReportsHomeCell];
    }
    
    if (![[ExSystem sharedInstance] hasTravelRequest] ||
        ![[ExSystem sharedInstance] isRequestUser]) {
        
        [self disableCellWithType:TravelRequestHomeCell];
    }
    
// MOB-17062 - Open booking users do not have book trips option. 
    if (![[ExSystem sharedInstance] siteSettingAllowsTravelBooking] ||  ([[ExSystem sharedInstance] hasRole:ROLE_OPEN_BOOKING_USER] && ![[ExSystem sharedInstance] hasTravelBooking]) ) {
        // Do we need to disable trips cell too?  If I remember correctly, this is for booking only.
        [self disableCellWithType:FlightBookingHomeCell];
        [self disableCellWithType:HotelBookingHomeCell];
        [self disableCellWithType:CarBookingHomeCell];
        [self disableCellWithType:RailBookingHomeCell];
    }
}

/**
 Disables cell with a type
 */
- (void)disableCellWithType:(HomeTableViewCellType)cellType
{
    for (HomeTableViewCellDescription *description in self.cellDescriptions) {
        if (description.cellType == cellType) {
            description.disabled = YES;
        }
    }
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return @"HOME_PAGE90";
}


#pragma mark - ApplicationLock Notifications
-(void) doPostLoginInitialization
{
    [self makeWhatsNewAndTipsOverlay];
    // When user logs in always get data from server
    [self fetchHomePageDataAndSkipCache:YES];
    requireHomeScreenRefresh = requireTripsDataRefresh = requireSummaryDataRefresh = NO;

    [self updateTableViewLayout];
    [HelpOverlayFactory addiPhoneHomeOverlayToView:self.navigationController.view];
    if ([Config isSprintDemoBuild])
    {
        [HelpOverlayFactory addiPhoneHomeReleaseNoteOverlayToView:self.navigationController.view];
    }
    
    // if user logged in through testdrive or some other Storyboard then dismiss the login storyboard.
    if(self.signInViewNavigationController != nil)
    {
        [self.signInViewNavigationController dismissViewControllerAnimated:NO completion:nil];
    }
    [AnalyticsTracker initializeScreenName:@"Home"];
}

#pragma mark - Handle Login view
/**
 Displays login view or test drive if the user opened the app for first time
 */

-(void) showManualLoginView
{
	if ([ConcurMobileAppDelegate isLoginViewShowing])
		return;

    
	// MOB-16161- Load new login UI , new login UI is in a storyboard.
    if ([Config isNewSignInFlowEnabled]) {
        SignInUserTypeViewController *lvc = [[UIStoryboard storyboardWithName:[@"SignIn" storyboardName] bundle:nil] instantiateInitialViewController];
        self.signInViewNavigationController = [[UINavigationController alloc] initWithRootViewController:lvc];
    }
    else
    {
        LoginViewController* lvc = [[UIStoryboard storyboardWithName:@"Login" bundle:nil] instantiateInitialViewController];
        self.signInViewNavigationController = [[UINavigationController alloc] initWithRootViewController:lvc];
    }

	[self presentViewController:self.signInViewNavigationController animated:YES completion:nil];
    
	ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    appDelegate.topView = LOGIN;
    
    // if User logged in ever ie has saved user id then dont show testdrive storyboard.
    // Just display the login screen if user ever logged in or if the user is SSO user. SSO users should not see the signin/Test drive start up screen
    // MOB-16425: The ExSystem.isCorpSSOUser is set to "NO" when SSO user logs out so check if SSO url exists to check if user is SSO.
    if( ([ExSystem sharedInstance].entitySettings.saveUserName != nil &&
         [[ExSystem sharedInstance].entitySettings.saveUserName isEqualToString:@"YES"] &&
         [[ExSystem sharedInstance].userName  lengthIgnoreWhitespace] > 0) || [[[ExSystem sharedInstance] loadCompanySSOLoginPageUrl] lengthIgnoreWhitespace])
        {
        [[MCLogging getInstance] log:@"Home9vc::showManualLoginView: Showing Login storyboard." Level:MC_LOG_INFO];
        return ;
    }
    
    // TODO : this would need to be refactored and move to application did finish lauching so that we can consolidate one time show of stuffs
    // MOB-18156 Save UserName == OFF && AutoLogin == OFF, but used app before(logged in before)
    // DO NOT SHOW test drive in this case
    if (![ExSystem sharedInstance].isTestDrive) {
        [[NSUserDefaults standardUserDefaults] setObject:@(NO) forKey:@"NotFirstTimeLogin"];
        return;
    }

    [[ConcurTestDrive sharedInstance] showTestDriveAnimated:YES];
    
}

/**
 Segue to password reset screen
 */
-(void) showPasswordRestScreen
{
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Home9VC::showPasswordRestScreen"] Level:MC_LOG_DEBU];
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

-(void) showSafariSignInScreen
{
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Home9VC::showSafariLoginScreen"] Level:MC_LOG_DEBU];
    [[ConcurTestDrive sharedInstance] removeAnimated:NO];
    
    //TODO: Clean up the logic
    
    SafariLoginViewController *vc = [[SafariLoginViewController alloc] init];
    [self presentViewController:vc animated:YES completion:nil];
//    if (![[self.signInViewNavigationController visibleViewController] isMemberOfClass:[SafariLoginViewController class]] ) {
//        [self.signInViewNavigationController popToRootViewControllerAnimated:YES];
//    }
    
    
}

-(void)showTestDriveStoryBoard
{
    [[ConcurTestDrive sharedInstance] popTestDriveAnimated:YES];
}

-(void)removeTestDriveStoryBoard
{
    [[MCLogging getInstance] log:@"Home9vc::removeTestDriveStoryBoard: Closing TestDrive storyboard." Level:MC_LOG_INFO];
    
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


#pragma mark -
#pragma mark Navigation Controller Methods

- (void)viewDidLoad
{
    [super viewDidLoad];
    
//    CXRequest *request = [HotelRecommendationsRequestFactory recommendationsForCheckInDate:[NSDate date] andLatitude:47.676394 andLongitude:-122.095231 andRadius:10 andUnit:@"M"];
//    
//    [[CXClient sharedClient] performRequest:request success:^(NSString *result) {
//        RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
//        NSLog(@"XML = %@", rootXML);
//    } failure:^(NSError *error) {
//        NSLog(@"Error = %@", error);
//    }];
    
    [ExSystem sharedInstance].sys.topViewName = self.getViewIDKey;

    self.dataprovider = [[HomeDataProvider alloc] init];
    self.dataprovider.delegate = self;

    self.rootVC = [[RootViewController alloc] init];
	[ExSystem sharedInstance].msgControl.rootVC = rootVC;
    
    if(![UIDevice isPad] && [[ExSystem sharedInstance].sys.showWhatsNew boolValue])
    {
        // First time use of 9.0 clean older version data
        [self clearHomeData];
    }
    
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
    
    [self setEdgeGestureRecognizer:UIRectEdgeLeft];

	// this could be nicer, we're just loading up the default home layout.
	// really this should be blank until we're logged in.
    [self updateTableViewLayout];

    // MOB-17238 : Test Drive Expiration
    self.accountExpired = NO;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleAccountExpiration) name:NotificationOnAccountExpired object:nil];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    // Handle background to foreground etc states

    if ([[ApplicationLock sharedInstance] isLoggedIn])
	{
		if (requireHomeScreenRefresh)
		{
            //refresh home screen with fresh datas
            [self fetchHomePageDataAndSkipCache:YES];
            requireHomeScreenRefresh = NO;
           
        }
        // Refresh the table - incase there is upload queue update.
        [self.tableView reloadData];
        [self pullWhatsNewAndTipsOverlayToFront];
        
        // used for launching approvals view from email/push notification when user is not logged in
        // after log in, approvals view will be shown
        // This API will do nothing, if no notification event needs to be proceeded
        [[NotificationController sharedInstance] processNotificationEvent:nil];

         // MOB-17238 : Test Drive Expiration dialogue
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

    [self updateBadgeCounts];
    
    // the code below is only for displaying mobile tours. It would move to homeloader eventually
    // check if it's the first time
    NSUserDefaults *userDefault = [NSUserDefaults standardUserDefaults];
    if ( ![ExSystem sharedInstance].isTestDrive && [[userDefault objectForKey:@"NotFirstTimeLogin"] isEqualToValue:@(NO)] ){
        NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
        [defaultCenter postNotificationName:NotificationOnFirstTimeLogin object:self];
    }
    
}

- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
    
    [self.navigationController setNavigationBarHidden:NO animated:NO];
    // Delete QE row
    // temp code only. Quick expense might appear if user has both 8.x and 9.0 versions. so delete when view loads
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_QUICK];
    if(entity != nil)
        [[HomeManager sharedInstance] deleteObj:entity];
    
    // update topview so isUserLogged in check fails. If Signin Storyboard is presented then the topview is updated again
    ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    appDelegate.topView = HOME_PAGE;

}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    
    [AnalyticsTracker resetScreenName];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)pullWhatsNewAndTipsOverlayToFront
{
    // pull this to if this is already built
    if (self.overlayView != nil) {
        [self.navigationController.view bringSubviewToFront:self.overlayView];
    }
    //if (self.whatsNewView != nil) {
    //    [self.navigationController.view bringSubviewToFront:self.whatsNewView];
    //}
}

// this is created on login, if necessary.  either regular or autologin
- (void)makeWhatsNewAndTipsOverlay
{
    // pull this to if this is already built
    if (self.overlayView != nil) {
        [self.navigationController.view bringSubviewToFront:self.overlayView];
    }
    
    // handle what's new and help overlay
    self.showWhatsNew = [[ExSystem sharedInstance].sys.showWhatsNew boolValue];
    if(![UIDevice isPad] && self.showWhatsNew)
    {
        // add help overlay
        self.overlayView = [[OverlayView alloc] init];
        [self.navigationController.view addSubview:self.overlayView];
    }
    
    [self pullWhatsNewAndTipsOverlayToFront];
}

-(void) setEdgeGestureRecognizer: (UIRectEdge*)direction
{
    if ([UIScreenEdgePanGestureRecognizer class]) {
        UIScreenEdgePanGestureRecognizer *leftEdgePan = [[UIScreenEdgePanGestureRecognizer alloc] initWithTarget:self action:@selector(showMoreMenu:)];
        leftEdgePan.edges = direction;
        [self.view setUserInteractionEnabled:YES];
        [self.view addGestureRecognizer:leftEdgePan];
    } else {
        // we are sorry but ios 6 does not have UIScreenEdgePanGestureRecognizer
    }

}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.cellDescriptions count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    HomeCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"HomeCell" forIndexPath:indexPath];

    // get description of the cell and configure appropriately
    if ([self.cellDescriptions count] > indexPath.row) {
        HomeTableViewCellDescription *description = [self.cellDescriptions objectAtIndex:indexPath.row];
        [self configureCell:cell withDescription:description]; // MOB-16685 Configure cell always and then disable it, if necessary
        if (description.disabled)
        {
            [self disableCell:cell];
        }
    }
    return cell;
}

- (void)configureCell:(HomeCell *)cell withDescription:(HomeTableViewCellDescription *)description
{
    cell.lblSubTitle.hidden = NO;
    cell.lblTitle.hidden = NO;
    cell.ivIcon.hidden = NO;
    cell.lblDivider.hidden = NO;
    cell.lblTitle.enabled = YES;
    cell.lblSubTitle.enabled = YES;
    cell.ivIcon.alpha = 1.0;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.userInteractionEnabled = YES;

    cell.backgroundView = [[UIView alloc] initWithFrame:cell.bounds];
    cell.lblTitle.text = description.label;
    cell.lblSubTitle.text = description.sublabel;
    
    // set up the position of the titles
    CGFloat cellHt = cell.frame.size.height;
    CGFloat titleHt = cell.lblTitle.frame.size.height;
    CGFloat subTitleFrameHt = cell.lblSubTitle.frame.size.height;
    cell.coTitleTop.constant = (cellHt - titleHt - subTitleFrameHt) / 2;
    cell.coSubTitleTop.constant = cell.coTitleTop.constant + titleHt;

    // set image
    cell.ivIcon.image = [UIImage imageNamed:description.icon];

    // MOB-16341 just so we can reuse the ipad icons on iphone.
    cell.ivIcon.contentMode = UIViewContentModeScaleAspectFit;

    [cell.badge updateBadgeCount:description.count];
}

-(void) disableCell:(HomeCell*)cell
{
    cell.lblSubTitle.enabled = NO;
    cell.lblTitle.enabled = NO;
    cell.ivIcon.alpha = 0.5;
    cell.accessoryType = UITableViewCellAccessoryNone;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    if (itemNum > 0)
        return 40.0;
    else
        return 0.0;
}

-(UIView*) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    if (itemNum > 0)
    {
        [self makeUploadView];
        self.uploadView.delegate = self;
        return self.uploadView;
    }
    else
        return nil;
}


#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    HomeTableViewCellDescription *description = [self.cellDescriptions objectAtIndex:indexPath.row];

    if (description.disabled) {
        [self ShowModuleDisabledAlert];
    }
    else if(description.cellType == TripsHomeCell)
    {
        NSDictionary *dictionary = @{@"Action": @"View Trips"};
        [Flurry logEvent:@"Home: Action" withParameters:dictionary];
        [AnalyticsTracker logEventWithCategory:@"Main Menu" eventAction:@"Trip" eventLabel:nil eventValue:nil];
        
        TripsViewController *nextController = [[TripsViewController alloc] initWithNibName:@"TripsView" bundle:nil];
        [self.navigationController pushViewController:nextController animated:YES];
        
    }
    else if(description.cellType == ExpensesHomeCell) // Expenses
    {
        NSDictionary *dictionary = @{@"Action": @"View Card Charges"};
        [Flurry logEvent:@"Home: Action" withParameters:dictionary];
        
        QuickExpensesReceiptStoreVC *nextController = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
        [(QuickExpensesReceiptStoreVC*)nextController setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:YES allowListEdit:YES];
        
        [self.navigationController pushViewController:nextController animated:YES];
    }
    else if (description.cellType == ApprovalsHomeCell)
    {
        [self SwitchToApprovalsView];
    }
    else if (description.cellType == ExpenseReportsHomeCell)
    {
        // Expense reports
        if(![[ExSystem sharedInstance] siteSettingAllowsExpenseReports])    // Check site settings
        {
            [self ShowModuleDisabledAlert];
            return;
        }
        
        NSDictionary *dictionary = @{@"Action": @"View Reports"};
        [Flurry logEvent:@"Home: Action" withParameters:dictionary];
        
        // drill to the report view from the report list view
        ActiveReportListViewController *nextController = [[ActiveReportListViewController alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
        [self.navigationController pushViewController:nextController animated:YES];
        
    }
    else if (description.cellType == HotelBookingHomeCell)
    {
        if ([Config isNewHotelBooking])
            [HotelSearchTableViewController showHotelsNearMe:self.navigationController];
        else
            [HotelViewController showHotelVC:self.navigationController withTAFields:nil];
    }
    else if (description.cellType == FlightBookingHomeCell)
    {
        if ([self checkBookAir])
        {
            [AirBookingCriteriaVC showAirVC:self.navigationController withTAFields:nil];
        }
    }
    else if (description.cellType == CarBookingHomeCell)
    {
        [CarViewController showCarVC:self.navigationController withTAFields:nil];
    }
    else if (description.cellType == RailBookingHomeCell)
    {
        [TrainBookVC showTrainVC:self.navigationController withTAFields:nil];
    }
    else if (description.cellType == TravelRequestHomeCell)
    {
        
        //redirect to Travel Request List
        UIStoryboard *sb = [UIStoryboard storyboardWithName:@"TravelRequest" bundle:nil];
        ActiveRequestListViewController *nextController = [sb instantiateViewControllerWithIdentifier:@"ActiveTravelRequestList"];
		
		[nextController setCallerViewName:@"Home"];
        [self.navigationController pushViewController:nextController animated:NO];
        
		[Flurry logEvent:@"Home: Open" withParameters:@{@"View": @"List Requests"}];
    }
}


-(void)fetchData:(NSMutableDictionary *)pBag
{
	NSString *msgName = pBag[@"MSG_NAME"];
	
	BOOL skipCache = NO;
	if(pBag != nil && (pBag[@"SKIP_CACHE"] != nil))
		skipCache = YES;
    
	NSString *cacheOnly = pBag[@"CACHE_ONLY"] ;
	[[ExSystem sharedInstance].msgControl createMsg:msgName CacheOnly:cacheOnly ParameterBag:pBag SkipCache:skipCache];
}

-(void) clearHomeData
{
    
    if(currentTrip != nil)
    {
        self.currentTrip = nil;
    }
    [[HomeManager sharedInstance] clearAll];
}

# pragma mark -
# pragma mark Pulldown Refresh

// Return whether the task is completed
-(BOOL) refreshView:(UIRefreshControl*) refresh
{
    // Implement pulldown refresh
    NSDictionary *dictionary = @{@"Action": @"Refresh Data"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    [self fetchHomePageDataAndSkipCache:YES];
    return NO;
}



# pragma mark - implement HomeDataProviderDelegate

/**
 * This method is called back when HomeDataProvider completes all refresh.
 */
-(void) refreshComplete
{
    [self hideWaitView];
    [self hideLoadingView];
    if(self.isRefreshing)
        [self doneRefreshing];
    
}

# pragma mark - implementation
//-(void) didProcessMessage:(Msg *)msg
//{
//    [self respondToFoundData:msg]; // TODO: handle case where msg.didConnectionFail is YES
//}

-(void)respondToFoundData:(Msg *)msg
{
    
 if([msg.idKey isEqualToString:CAR_RATES_DATA])
    {
		self.carRatesData = (CarRatesData*) msg.responder;
        // save this for the report detail screen.  There's a lot of places where code asks root for car rate data.
        // This will make sure it's set correctly.
        [ConcurMobileAppDelegate findRootViewController].carRatesData = self.carRatesData;
        // MOB-16587 - This is a hack to notify tabbar that the car mileage is availabe
        // Notify Tab bar about car rates.
        // TODO : More menu also should get the notification 
        if ([self.carRatesData hasAnyPersonalsWithRates:[ExSystem sharedInstance].sys.crnCode])
        {
            [[NSNotificationCenter defaultCenter] postNotificationName:NotificationHasCarRatesData object:self];
        }

	}
}

- (void)fetchHomePageData
{
    [self fetchHomePageDataAndSkipCache:NO];
}

- (void)fetchHomePageDataAndSkipCache:(BOOL)shouldSkipCache
{
	
	// Make server calls only if connected to network
   	if([ExSystem connectedToNetwork])
	{
        // Show wait view if the refresh is not yet complete
        //if([[self.fetchedResultsController sections] count] <= 0)
        //    [self showWaitView];

        if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
        {
            // MOB-13132 : Initialze carmileage data
              [[MCLogging getInstance] log:[NSString stringWithFormat:@"Home9VC::fetchHomePageDataAndSkipCache. calling fetchCarRatesAndSkipCache:%@  ", (shouldSkipCache ? @"YES" : @"NO")] Level:MC_LOG_DEBU];
            [self fetchCarRatesAndSkipCache:shouldSkipCache];
        }
        
        [self.dataprovider setupHomeData:shouldSkipCache];
        [self.dataprovider preFetchExpenseData:shouldSkipCache];
    }
}

// get the car rate data
- (void)fetchCarRatesAndSkipCache:(BOOL)shouldSkipCache
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	[[ExSystem sharedInstance].msgControl createMsg:CAR_RATES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache: shouldSkipCache RespondTo:self];
}

#pragma mark -
#pragma mark Properties

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

#pragma mark -
#pragma mark Toolbar/Nav bar action delegates

-(void)showMessageCenter:(id)sender
{
    MessageCenterViewController *nextController = [[MessageCenterViewController alloc] init];
    
    [self presentViewController:nextController withDirection:@"fromRight"];
}

- (void) presentViewController:(UIViewController *)viewController withDirection: (NSString *) direction {
    
    [CATransaction begin];
    
    CATransition *transition = [CATransition animation];
    transition.type = kCATransitionMoveIn;
    transition.subtype = direction;
    transition.duration = 0.25f;
    transition.fillMode = kCAFillModeForwards;
    transition.removedOnCompletion = YES;
    
    [[UIApplication sharedApplication].keyWindow.layer addAnimation:transition forKey:@"transition"];
    [[UIApplication sharedApplication] beginIgnoringInteractionEvents];
    [CATransaction setCompletionBlock: ^ {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(transition.duration * NSEC_PER_SEC)), dispatch_get_main_queue(), ^ {
            [[UIApplication sharedApplication] endIgnoringInteractionEvents];
        });
    }];
    
    [self presentViewController:viewController animated:NO completion:NULL];
    
    [CATransaction commit];
}

-(void)showMoreMenu:(id)sender
{
    if ([sender isKindOfClass:[UIGestureRecognizer class]]) {
        if ([(UIGestureRecognizer*)sender state] != UIGestureRecognizerStateEnded) {
            // UIGestureRecognizer call at least 3 times: 1 stateStarted statedChanged stateEnded
            // we only care about state ended
            return;
        }
    }
    
    MoreMenuViewController *moreMenuVC = [[MoreMenuViewController alloc] init];
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:moreMenuVC];
    [self presentViewController:navi withDirection:@"fromLeft"];
}

-(void)bookingsActionPressed:(id)sender
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
    
	action.tag = BOOKINGS_ACTIONSHEET_TAG;
	
    [action showInView:[UIApplication sharedApplication].keyWindow];
    
    [AnalyticsTracker logEventWithCategory:@"Bottom Bar" eventAction:@"Book" eventLabel:nil eventValue:nil];
}


-(void) buttonQuickExpensePressed:(id)sender
{
    if([Config isNewEditingEnabled])
    {
        UITableViewController *ctrl = [[UIStoryboard storyboardWithName:@"FormVCBaseInline" bundle:nil] instantiateViewControllerWithIdentifier:@"FormVCBaseInline"];
        [self.navigationController pushViewController:ctrl animated:NO];
        
        /*
        UINavigationController *navi = [[UIStoryboard storyboardWithName:@"FormVCBaseInline" bundle:nil] instantiateInitialViewController];
        [self.navigationController pushViewController:navi.viewControllers[0] animated:NO];
        */
//        CreateExpenseVC *expenseVC = [[CreateExpenseVC alloc] initWithEntryOrNil:nil];
//        [expenseVC loadView];
//        expenseVC = [navi viewControllers][0];
//        [self.navigationController pushViewController:expenseVC animated:NO];
//        CreateExpenseVC *expenseVC = [[UIStoryboard storyboardWithName:@"FormVCBaseInline" bundle:nil] instantiateInitialViewController];
////        expenseVC = [expenseVC initWithEntryOrNil:nil];
//        [self.navigationController pushViewController:expenseVC animated:NO];
    }
    else
    {
        QEFormVC *fromVC = [[QEFormVC alloc] initWithEntryOrNil:nil];
        [self.navigationController pushViewController:fromVC animated:YES];
    }
    NSDictionary *dict = @{@"Came From": @"Home"};
    [Flurry logEvent:@"Mobile Entry: Create2" withParameters:dict];
}

- (void)cameraPressed:(id) sender
{
     //TestExpenseReportsViewController *vc = [[TestExpenseReportsViewController alloc] init];
     //[self.navigationController pushViewController:vc animated:YES];

    if (rsuHelper == nil)
    {
        rsuHelper = [[ReceiptStoreUploadHelper alloc] init];
        rsuHelper.openReceiptListWhenFinished = true;
        rsuHelper.vc = self;
    }
    [rsuHelper startCamera:nil];
}

- (void)btnCarMileagePressed:(id) sender
{
    // TODO : Add flurry events here
    // if reports are disabled then users cannot do car mileage anyway
    if(![[ExSystem sharedInstance] siteSettingAllowsExpenseReports])    // Check site settings
    {
        [self ShowModuleDisabledAlert];
        return;
    }
    
    // if offline show an alert
    if(![ExSystem connectedToNetwork])
    {
        [self ShowOfflineAlert];
        return;
    }
    NSDictionary *dictionary = @{@"Add from": @"Home"};
    [Flurry logEvent:@"Car Mileage: Add from" withParameters:dictionary];
    
    // Everything is good
    //takes you to the select report view
    SelectReportViewController * pVC = [[SelectReportViewController alloc] initWithNibName:@"SelectReportViewController" bundle:nil];
    pVC.meKeys = nil;
    pVC.pctKeys = nil;
    pVC.cctKeys = nil;
    pVC.meAtnMap = nil;
    pVC.isCarMileage = YES;
    //AJC -- is this code needed? delete after 2013-09-20 if not needed by then
    //pVC.parentMVC = self;
    
    [self.navigationController pushViewController:pVC animated:YES];
    
}

// Action Sheet delegates
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (actionSheet.tag == BOOKINGS_ACTIONSHEET_TAG)
	{
        MobileActionSheet* mas = (MobileActionSheet*) actionSheet;
        NSString* btnId = [mas getButtonId:buttonIndex];
        [self.navigationController setToolbarHidden:YES animated:YES];
        
        if ([BOOKINGS_BTN_HOTEL isEqualToString:btnId])
        {
            if ([Config isNewHotelBooking]) {
                [AnalyticsTracker logEventWithCategory:@"Action Sheet" eventAction:@"Book Hotel" eventLabel:nil eventValue:nil];
                [HotelSearchTableViewController showHotelsNearMe:self.navigationController];
            }
            else
                [HotelViewController showHotelVC:self.navigationController withTAFields:nil];

        }
        else if ([BOOKINGS_BTN_CAR isEqualToString:btnId])
        {
            [AnalyticsTracker logEventWithCategory:@"Action Sheet" eventAction:@"Book Car" eventLabel:nil eventValue:nil];
            [CarViewController showCarVC:self.navigationController withTAFields:nil];
        }
        else if ([BOOKINGS_BTN_RAIL isEqualToString:btnId])
        {
            [AnalyticsTracker logEventWithCategory:@"Action Sheet" eventAction:@"Book Rail" eventLabel:nil eventValue:nil];
            [TrainBookVC showTrainVC:self.navigationController withTAFields:nil];
        }
        else if ([BOOKINGS_BTN_AIR isEqualToString:btnId])
        {
            if ([self checkBookAir])
            {
                [AnalyticsTracker logEventWithCategory:@"Action Sheet" eventAction:@"Book Air" eventLabel:nil eventValue:nil];
                [AirBookingCriteriaVC showAirVC:self.navigationController withTAFields:nil];

            }
        }
        else{                                   // Press Cancel Button
            [AnalyticsTracker logEventWithCategory:@"Action Sheet" eventAction:@"Cancel" eventLabel:nil eventValue:nil];
        }
        
	}
}


#pragma mark - Upload Queue Banner View adjustment
-(void) showUploadViewController
{
    UploadQueueViewController *vc = [[UploadQueueViewController alloc] initWithNibName:@"UploadQueueViewController" bundle:nil];
    UIBarButtonItem *btnUpload = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Upload"] style:UIBarButtonItemStyleBordered target:vc action:@selector(startUpload)];
    vc.title = [Localizer getLocalizedText:@"Upload Queue"];
    vc.navigationItem.rightBarButtonItem = btnUpload;
    [self.navigationController pushViewController:vc animated:YES];
}

-(void) forceReload
{
    //requireHomeScreenRefresh = requireTripsDataRefresh = requireSummaryDataRefresh = YES;
    [self.dataprovider setupHomeData:YES];
}

// Implement proper update method later
// For now home reloads everything
-(void) refreshSummaryData
{
    //requireHomeScreenRefresh = requireTripsDataRefresh = requireSummaryDataRefresh = YES;
    //ideally the Data provider
    [self.dataprovider getSummaryData:YES];
}

// Implement proper update method later
// For now home reloads everything
-(void) refreshTripsData
{
    requireHomeScreenRefresh = requireTripsDataRefresh = requireSummaryDataRefresh = YES;
//    [self.dataprovider getTripsData:YES]; //Commented this as refresh should happen only when viewAppears 
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
    // Check site settings
    bool hasOtherApprovalRole = [[ExSystem sharedInstance] hasRole:MOBILE_INVOICE_PAYMENT_USER]
                                || [[ExSystem sharedInstance] hasRole:ROLE_INVOICE_APPROVER]
                                || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER]
                                || [[ExSystem sharedInstance] hasRole:ROLE_TRIP_APPROVER] || [[ExSystem sharedInstance]hasRole:ROLE_MOBILE_INVOICE_PURCH_APRVR];
    bool siteSettingsAllowExpenseApprovals = [[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals];
    // must check for both approval role and site settings. only if neither are present, approvals is disabled 
    if( !hasOtherApprovalRole && !siteSettingsAllowExpenseApprovals && [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER] )
    {
        [self ShowRoleDisabledAlert];
        return;
    }
    
//    if( ![[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals] )
//    {
//        [self ShowModuleDisabledAlert];
//        return;
//    }
    
    // drill to the report view from the report list view
    ReportApprovalListViewController *nextController = [[ReportApprovalListViewController alloc] initWithSummaryData:self.dataprovider.summaryData];
    [self.navigationController pushViewController:nextController animated:YES];
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

/**
 Sets expense count
 */
- (void)setExpensesCount:(NSNumber *)count
{
    [self setBadgeCount:count forCellType:ExpensesHomeCell];
}

/**
 Sets expense report count
 */
- (void)setExpenseReportsCount:(NSNumber *)count
{
    [self setBadgeCount:count forCellType:ExpenseReportsHomeCell];
}

/**
 Sets approval count
 */
- (void)setApprovalCount:(NSNumber *)count
{
    [self setBadgeCount:count forCellType:ApprovalsHomeCell];
}

/**
 Sets trips count
 */
- (void)setTripsCount:(NSNumber *)count
{
    [self setBadgeCount:count forCellType:TripsHomeCell];
}

/**
 Sets the badge count for a given cell type
 */
- (void)setBadgeCount:(NSNumber *)count forCellType:(HomeTableViewCellType)cellType
{
    for (HomeTableViewCellDescription *description in self.cellDescriptions) {
        if (description.cellType == cellType) {
            description.count = count;
        }
    }
    [self.tableView reloadData];
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
    [self setExpenseReportsCount:count];
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
    [self setExpensesCount:count];
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
    [self setApprovalCount:count];
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
    [self setTripsCount:count];
}


-(void)onLogout
{
    [self clearHomeData];
}

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
            // MOB-17238 : Test Drive Expiration
            // Call logout
            [[ApplicationLock sharedInstance] onLogoutButtonPressed];
            self.accountExpired = NO;
        }
        
    }
    
}

@end