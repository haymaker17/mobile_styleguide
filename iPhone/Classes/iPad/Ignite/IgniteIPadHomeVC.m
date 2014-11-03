//
//  IgniteIPadHomeVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteIPadHomeVC.h"
#import "IgniteItinDetailVC_iPad.h"
#import "ApplicationLock.h"
#import "EntitySalesForceUser.h"
#import "SalesForceUserManager.h"
#import "ExSystem.h"

// TODO's 
// 1. Block drawing of Settings/Logout button in super

@interface IgniteIPadHomeVC ()
-(void) loadSalesForceUserInfo;
-(void) loadSalesForceUserPhoto;
-(void) loadSalesforceTrips;
-(void) showWaitForSalesForceView;
-(void) hideWaitForSalesForceView;
-(BOOL) isShowingWaitForSalesForceView;
-(void) showTripVC;
-(void) showUserInfo;

- (void)setTripsTableHeight:(int) tripCount;

- (void)initTripsFromCache;
- (void)initFromCacheData;

- (BOOL)areTripsLoaded;
- (void)showHomeAnimation1;
- (void)showHomeAnimation2;
- (void)showHomeAnimation1Switch;
- (void)showHomeAnimation2Switch;
@end

@implementation IgniteIPadHomeVC
@synthesize btnShowTrip, btnTrips, tblTrips, dsTrips, selectedTrip, viewWaitForSalesForce, isLoadingSalesForceUserData;
@synthesize ivProfile, lblUserName, ivCity, ivCity2, ivTripBack;


- (void) hideButtons
{
    if(btnLogout != nil)
        [btnLogout removeFromSuperview];
    if(btnSettings != nil)
        [btnSettings removeFromSuperview];
    
    
}
- (void)adjustForPortrait
{
    [super adjustForPortrait];
    [self hideButtons];
}

- (void)adjustForLandscape
{
    [super adjustForLandscape];
    [self hideButtons];
}

- (void)setupNavBar
{
    // Settings button
	UIButton *btnSet = [UIButton buttonWithType:UIButtonTypeCustom]; 
    NSString *btnImage = @"ignite_icon_settings";
	[btnSet setBackgroundImage:[[UIImage imageNamed:btnImage]
								stretchableImageWithLeftCapWidth:0.0f 
								topCapHeight:0.0f]
					  forState:UIControlStateNormal];
    btnSet.frame = CGRectMake(0, 0, 24, 24);
	[btnSet addTarget:self action:@selector(buttonSettingsPressed:) forControlEvents:UIControlEventTouchUpInside];
	//create a UIBarButtonItem with the button as a custom view
	UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithCustomView:btnSet];
    self.navigationItem.rightBarButtonItem = customBarItem;
    
    UIBarButtonItem* btnBookTrip = [ExSystem makeColoredButton:@"IGNITE_DRKBLUE_PILL" W:103 H:25 Text:(NSString *)@"Book Trip" SelectorString:nil MobileVC:self];
    UIBarButtonItem* btnMyTrips = [ExSystem makeColoredButton:@"IGNITE_BLUE_PILL" W:103 H:25 Text:(NSString *)@"My Trips" SelectorString:nil MobileVC:self];
    [self.navigationItem setLeftBarButtonItems:[NSArray arrayWithObjects:btnMyTrips, btnBookTrip, nil]];
    
}
-(BOOL)neediPadWaitViews
{
    return FALSE;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Hide the Salesforce wait view
    [self hideWaitForSalesForceView];
    
    // Set title text color to white for all screens
    NSDictionary *navbarTitleTextAttributes = [NSDictionary dictionaryWithObjectsAndKeys:
                                               [UIColor whiteColor],UITextAttributeTextColor, nil];
//                                               [UIColor lightGrayColor], UITextAttributeTextShadowColor, 
//                                               [NSValue valueWithUIOffset:UIOffsetMake(-1, 0)], UITextAttributeTextShadowOffset, nil];
    [[UINavigationBar appearance] setTitleTextAttributes:navbarTitleTextAttributes];
    // Alter back button image
    [[UIBarButtonItem appearance] setBackButtonBackgroundImage:[ImageUtil getImageByName:@"button__blue_back"] forState:UIControlStateNormal barMetrics:UIBarMetricsDefault];
    
    // Do any additional setup after loading the view from its nib.
    self.title = @"My Trips";
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    
    if (self.dsTrips == nil) // Initial loading, do not clear out dsTrips if memory warning
    {
        // Hide the table rows before data is loaded
        [self setTripsTableHeight:0];

        // Set up Trips table 
        self.dsTrips = [[IgniteTripsDS alloc] init];
        [dsTrips setSeedData:[ad managedObjectContext] withTripFilter:@"CURRENT" withTable:self.tblTrips withDelegate:self];
//    if ([[ApplicationLock sharedInstance] isLoggedIn])
//        [dsTrips fetchedResults];
    }
    else 
    {
        [self.dsTrips resetTable:tblTrips];
        [self initTripsFromCache];
    }
    
    [self setupNavBar];
    
    // Adjust viewPadIntro to landscape and hide it from user
    self.viewPadIntro.frame = CGRectMake(0, 0, 1004, 768);
    [self.viewPadIntro removeFromSuperview];

}
- (BOOL) areTripsLoaded
{
    // Trips not loaded
    return self.dsTrips.fetchedResultsController != nil;
}
- (void)showIntroAnimation
{
    if (introAnimationShown)
        return;
    
    // Trips not loaded
    if (![self areTripsLoaded])
        return;
    
    // User not loaded
    if (![ self.lblUserName.text length])
        return;
    
    introAnimationShown = YES;
    self.ivCity.frame = CGRectMake(-150, -150, 1324, 1064);
    
    [UIView beginAnimations:@"Fade" context:nil];
    [UIView setAnimationDelegate:self];
    [UIView setAnimationDuration:10];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    self.ivCity.frame = CGRectMake(0, 0, 1024, 764);
    [UIView setAnimationDidStopSelector:@selector(stopDraggingAnimation)];
    
    [UIView commitAnimations];
}

- (void)showHomeAnimation1
{
    if (stopIntroAnimation)
        return;
    
    self.ivCity.frame = CGRectMake(-150, -150, 1324, 1064);
    
    [UIView beginAnimations:@"Fade1" context:nil];
    [UIView setAnimationDelay:0.5];
    [UIView setAnimationDelegate:self];
    [UIView setAnimationDuration:10];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    self.ivCity.frame = CGRectMake(0, 0, 1024, 764);
    [UIView setAnimationDidStopSelector:@selector(showHomeAnimation1Switch)];
    
    [UIView commitAnimations];
}

- (void)showHomeAnimation1Switch
{
//    self.ivCity2.frame = CGRectMake(-150, -150, 1324, 1064);
//    [UIView transitionWithView:self.view
//                      duration:8
//                       options:UIViewAnimationOptionTransitionCrossDissolve 
//                    animations:^{ [self.view insertSubview:self.ivCity belowSubview:self.ivCity2]; }
//                    completion:^(BOOL finished) {
//                        [self showHomeAnimation2];
//                    }];
    self.ivCity2.frame = CGRectMake(-150, -150, 1324, 1064);
    self.ivCity2.alpha = 0.0;
    [self.view insertSubview:self.ivCity2 belowSubview:self.ivCity];

    [UIView beginAnimations:@"FadeSwitch1" context:nil];
    [UIView setAnimationDelegate:self];
    [UIView setAnimationDuration:8];
    [UIView setAnimationCurve: UIViewAnimationCurveEaseInOut];
    self.ivCity2.alpha = 1.0;

    self.ivCity.alpha = 0.0;
    [UIView setAnimationDidStopSelector:@selector(showHomeAnimation2)];    
    [UIView commitAnimations];
    
}
- (void)showHomeAnimation2Switch
{
//    self.ivCity.frame = CGRectMake(-150, -150, 1324, 1064);
//    [UIView transitionWithView:self.view
//                      duration:8
//                       options:UIViewAnimationOptionTransitionCrossDissolve 
//                    animations:^{ [self.view insertSubview:self.ivCity2 belowSubview:self.ivCity]; }
//                    completion:^(BOOL finished) {
//                        [self showHomeAnimation1];
//                    }];

    self.ivCity.frame = CGRectMake(-150, -150, 1324, 1064);
    self.ivCity.alpha = 0.0;
    [self.view insertSubview:self.ivCity belowSubview:self.ivCity2];
    [UIView beginAnimations:@"FadeSwitch2" context:nil];
    [UIView setAnimationDelegate:self];
    [UIView setAnimationDuration:8];
    [UIView setAnimationCurve:UIViewAnimationOptionCurveEaseInOut];
    self.ivCity.alpha = 1.0;
    
    self.ivCity2.alpha = 0.0;
    [UIView setAnimationDidStopSelector:@selector(showHomeAnimation1)];
    
    [UIView commitAnimations];
    
}

- (void)showHomeAnimation2
{
    if (stopIntroAnimation)
        return;
    
    self.ivCity2.frame = CGRectMake(-150, -150, 1324, 1064);
    
    [UIView beginAnimations:@"Fade2" context:nil];
    [UIView setAnimationDelay:0.5];
    [UIView setAnimationDelegate:self];
    [UIView setAnimationDuration:10];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
//    self.ivCity.image = [UIImage imageNamed:@"bckgrd_SF2"];
    self.ivCity2.frame = CGRectMake(0, 0, 1024, 764);
    [UIView setAnimationDidStopSelector:@selector(showHomeAnimation2Switch)];
    
    [UIView commitAnimations];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    stopIntroAnimation = FALSE;
    self.ivCity.alpha = 1.0;
    [self.view insertSubview:self.ivCity2 belowSubview:self.ivCity];

    [self showHomeAnimation1];
}

- (void)viewWillDisappear:(BOOL)animated
{
    stopIntroAnimation = YES;
    [super viewWillDisappear:animated];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    if ([[ApplicationLock sharedInstance] isLoggedIn])
    {
        // Show loading view, if trips are not loaded
        if (![self areTripsLoaded])
        {
            if ([[ExSystem sharedInstance] shouldSendRequestsOverNetwork])
                [self showLoadingView];
        }
        else {
            [self.dsTrips fetchedResults];
        }
        [self showUserInfo];
        //[self showIntroAnimation];
    }
    else 
    {
        if ([[ExSystem sharedInstance] shouldSendRequestsOverNetwork])
            [self showLoadingView];
    }
}

- (void)setTripsTableHeight:(int) tripCount
{
    CGRect frame = self.tblTrips.frame;
    if (tripCount <=0) tripCount = 0;
    if (tripCount > 5) tripCount = 5;
    frame.size.height = tripCount * [dsTrips tableView:self.tblTrips heightForRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0]];
    self.tblTrips.frame = frame;
    self.ivTripBack.frame = frame;    
}
- (void)initTripsFromCache
{
    [dsTrips fetchedResults];
    
    NSArray* sections = [dsTrips.fetchedResultsController sections];
    id <NSFetchedResultsSectionInfo> sectionInfo = [sections count] > 0 ? [sections objectAtIndex:0] : nil;
    
    int tripCount = [sectionInfo numberOfObjects];
    [self setTripsTableHeight:tripCount];
//    CGRect frame = self.tblTrips.frame;
//    if (tripCount <=0) tripCount = 2;
//    if (tripCount > 5) tripCount = 5;
//    frame.size.height = tripCount * [dsTrips tableView:self.tblTrips heightForRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0]];
//    self.tblTrips.frame = frame;
//    self.ivTripBack.frame = frame;
    [self.tblTrips reloadData];    
    
    [self hideLoadingView];
    
//    [self showIntroAnimation];
}

- (void)initFromCacheData
{
    [self initTripsFromCache];
}

-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:TRIPS_DATA] && [self isViewLoaded])
	{
        [self initTripsFromCache];
    }
    else if ([msg.idKey isEqualToString:IGNITE_USER_INFO_DATA])
    {
        self.isLoadingSalesForceUserData = NO;

        if ([[SalesForceUserManager sharedInstance] getAccessToken] == nil || [[SalesForceUserManager sharedInstance] getInstanceUrl] == nil)
        {
            if (![[ExSystem sharedInstance] shouldErrorResponsesBeHandledSilently])
            {
                MobileAlertView *alert = [[MobileAlertView alloc] 
                                          initWithTitle:[Localizer getLocalizedText:@"Error"]
                                          message:@"iPadHome:There was an error accessing your Salesforce account. Please go to Concur web to grant access." // TODO: Localize
                                          delegate:nil
                                          cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                                          otherButtonTitles:nil];
                [alert show];
            }
        }
        else
        {
            if ([[ExSystem sharedInstance] shouldSendRequestsOverNetwork])
            {
                // Try to load the user's photo and salesforce trips early.
                [self loadSalesForceUserPhoto]; // Caches photo. It doesn't matter if the IgniteIPadHomeVC view is loaded.
                [self loadSalesforceTrips];
            }
        }

        if ([self isViewLoaded])
        {
            if ([self isShowingWaitForSalesForceView])
            {
                [self hideWaitForSalesForceView];
                [self showTripVC];
            }
            else 
            {
                [self showUserInfo];
//                [self showIntroAnimation];
            }
        }
    }
    else 
    {
        [super respondToFoundData:msg];
    }
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    if (interfaceOrientation == UIInterfaceOrientationLandscapeRight || interfaceOrientation == UIInterfaceOrientationLandscapeLeft)
        return YES;
	return NO;
}

#pragma mark - IGNITE actions
- (IBAction)buttonShowTripPressed:(id)sender
{
    if (!self.isLoadingSalesForceUserData)
        [self showTripVC];
    else
        [self showWaitForSalesForceView];
}

-(void) showTripVC
{
    IgniteItinDetailVC_iPad *itinVc = [[IgniteItinDetailVC_iPad alloc] initWithNibName:@"IgniteItinDetailVC_iPad" bundle:nil];
    itinVc.trip = self.selectedTrip;
    [self.navigationController pushViewController:itinVc animated:YES];
}

#pragma mark - Wait View methods
-(void) showWaitForSalesForceView
{
    self.viewWaitForSalesForce.hidden = NO;
    [self.view bringSubviewToFront:viewWaitForSalesForce];
}

-(void) hideWaitForSalesForceView
{
    self.viewWaitForSalesForce.hidden = YES;
    [self.view sendSubviewToBack:viewWaitForSalesForce];
}

-(BOOL) isShowingWaitForSalesForceView
{
    return (!self.viewWaitForSalesForce.hidden);
}

#pragma mark - IgniteTripsDelegate actions
- (void)tripSelected:(EntityTrip*) trip
{
    self.selectedTrip = trip;
    
    if (!self.isLoadingSalesForceUserData)
        [self showTripVC];
    else
        [self showWaitForSalesForceView];
}

#pragma mark - Overrides
-(void) doPostLoginInitialization
{
    [super doPostLoginInitialization]; // Let iPadHomeVC do its initialization first
    
    if ([[ExSystem sharedInstance] shouldSendRequestsOverNetwork])
    {
        [self loadSalesForceUserInfo];
    }
    else 
    {
        [self initFromCacheData];
    }
}

#pragma mark - Data fetching
-(void) loadSalesForceUserInfo
{
    self.isLoadingSalesForceUserData = YES;
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    [[ExSystem sharedInstance].msgControl createMsg:IGNITE_USER_INFO_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) loadSalesForceUserPhoto
{
    EntitySalesForceUser *user = [[SalesForceUserManager sharedInstance] fetchUser];
    if (user != nil && [user.smallPhotoUrl length])
    {
        NSString *imageCacheName = [NSString stringWithFormat:@"User_%@_Photo_Small", user.identifier];
        
        // Passing nil for IV (image view) and MVC.  The image will be cached.
        [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:user.smallPhotoUrl RespondToImage:nil IV:nil MVC:nil ImageCacheName:imageCacheName OAuth2AccessToken:[[SalesForceUserManager sharedInstance] getAccessToken]];
    }
}

-(void) loadSalesforceTrips
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    
    // RespondToFoundData will not be called for this message.
    [[ExSystem sharedInstance].msgControl createMsg:SALESFORCE_TRIP_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:nil]; // Just parse it, don't call back.
    
}

-(void) showUserInfo
{
    EntitySalesForceUser *user = [[SalesForceUserManager sharedInstance] fetchUser];
    if (user != nil)
    {
        if ( [user.identifier length] && [user.smallPhotoUrl length])
        {
            UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];

            NSString *imageCacheName = [NSString stringWithFormat:@"User_%@_Photo_Small", user.identifier];
            [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:user.smallPhotoUrl RespondToImage:img IV:self.ivProfile MVC:nil ImageCacheName:imageCacheName OAuth2AccessToken:[[SalesForceUserManager sharedInstance] getAccessToken]];
        }
        
        if ([user.name length])
        {
            self.lblUserName.text = user.name;
        }
    }
}
#pragma mark Demo helper API
+ (void)resetDemoData
{
    if (![[ExSystem sharedInstance] shouldSendRequestsOverNetwork])
    {
        // If in cache mode, remove recommendation/meetings and chatter posts
        NSArray* mtgSegs = [[TripManager sharedInstance] fetchSegmentsByType:SEG_TYPE_EVENT];
        NSArray* diningSegs = [[TripManager sharedInstance] fetchSegmentsByType:SEG_TYPE_DINING];
        NSMutableArray* allGeneratedSegs = [NSMutableArray arrayWithArray:mtgSegs];
        [allGeneratedSegs addObjectsFromArray:diningSegs];
        for (EntitySegment* seg in allGeneratedSegs )
        {
            [[TripManager sharedInstance] deleteObj:seg];
        }
    }
}

@end
