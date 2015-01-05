//
//  ConcurMobileAppDelegate.m
//  ConcurMobile
//
//  Created by Paul Kramer on 10/27/09.
//  Copyright __MyCompanyName__ 2009. All rights reserved.
//

#import "ATConnect.h"
#import "BeaconManager.h"
#import "CTENetworking.h"

#import "AnalyticsManager.h"
#import "GoGoCloud.h"
#import "GoGoNotificationHandler.h"
#import "GoGoOffer.h"
#import "ConcurMobileAppDelegate.h"
#import "CXWebSocket.h"
#import "ExSystem.h" 

#import "PostQueue.h"
#import "LoginViewController.h"
#import "FileManager.h"
#import "Localizer.h"
#import "MobileAlertView.h"
#import "MobileActionSheet.h"
#import "ExSystem.h"
#import "Flurry.h"
#import "ApplicationLock.h"
#import "RoundedRectView.h"
#import "MoreVC.h"
#import "PDFVC.h"
#import "OAuthUserManager.h"
#import "SafetyCheckInVC.h"
#import "PDFViewController.h"
#import "NotificationController.h"
#import "LoginOptionsViewController.h"
#import "SafariLoginViewController.h"

// For RVC change
#import "HotelTextEditorViewController.h"
#import "HotelOptionsViewController.h"
#import "RoomListViewController.h"
#import "TextAreaEditVC.h"
#import "SettingsViewController.h"
#import "ReportEntryViewController.h"
#import "DistanceViewController.h"
#import "HotelLocationViewController.h"
#import "HotelSearchResultsViewController.h"
#import "HotelDetailsViewController.h"
#import "TrainBookVC.h"
#import "HotelDetailedMapViewController.h"
#import "HotelCreditCardViewController.h"
#import "CarListViewController.h"
#import "CarMapViewController.h"
#import "UIColor+CCPalette.h"
#import "UIColor+ConcurColor.h"
#import "NSStringAdditions.h"
#import "ViewConstants.h"

#import "AnalyticsTracker.h"
#import "UserDefaultsManager.h"

#import "FusionMockServer.h"
#import "MockServer.h"

#import "Config.h"

@interface ConcurMobileAppDelegate ()
@property BOOL pushInitialized;
@end

//MOB-17398: Enabled for codecoverage builds only.
#if CODE_COVERAGE
    extern void __gcov_flush();
#endif


@implementation ConcurMobileAppDelegate

@synthesize navController;
@synthesize window, detailViewController, coverView, authView;
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
@synthesize splitViewController;
#endif

@synthesize managedObjectContext=__managedObjectContext;

@synthesize managedObjectModel=__managedObjectModel;

@synthesize persistentStoreCoordinator=__persistentStoreCoordinator;

@synthesize receiptVC, qeFormVC, isUploadPdfReceipt;

@synthesize pushInitialized;

#pragma mark - Navigation Utility Methods
+(UINavigationController*) getBaseNavigationController
{
    ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*)[UIApplication sharedApplication].delegate;
    return appDelegate.navController;
}

+(RootViewController*) findRootViewController
{
	RootViewController *rvc = nil;
	
	ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
	if (appDelegate != nil && appDelegate.navController != nil)
	{
		NSArray *viewControllers = appDelegate.navController.viewControllers;
		if (viewControllers != nil && [viewControllers count] > 0)
		{
            UIViewController *vc = viewControllers[0];
            if(([vc isKindOfClass:[HomeLoaderVC class]]) )
            {
                if ([vc respondsToSelector:@selector(getRootviewController)])
                {
                    HomeLoaderVC *homeLoaderVC = (HomeLoaderVC *)vc;
                    rvc = (RootViewController *)[homeLoaderVC getRootviewController];
                }
            }
		}
	}
	
	return rvc;
}

- (NSUInteger)application:(UIApplication *)application supportedInterfaceOrientationsForWindow:(UIWindow *)window
{
    if ([UIDevice isPad])
    {
        return UIInterfaceOrientationMaskAll;
    }
    else
        return UIInterfaceOrientationMaskPortrait | UIInterfaceOrientationMaskPortraitUpsideDown;
}

+(iPadHomeVC*) findiPadHomeVC
{
    iPadHomeVC* ipadVC = nil;
	
	ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
	if (appDelegate != nil && appDelegate.navController != nil)
	{
		NSArray *viewControllers = appDelegate.navController.viewControllers;
		if (viewControllers != nil && [viewControllers count] > 0)
		{
			UIViewController *vc = viewControllers[0];
			if ([vc isKindOfClass:[iPadHomeVC class]])
			{
				ipadVC = (iPadHomeVC*)vc;
			}
		}
	}
	
	return ipadVC;
}


// Generic method to find the Corresponding homevc of the application
+(UIViewController*) findHomeVC
{
    UIViewController *newHome = nil;
	
	ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
	
	if (appDelegate != nil && appDelegate.navController != nil)
	{
		NSArray *viewControllers = appDelegate.navController.viewControllers;
		if (viewControllers != nil && [viewControllers count] > 0)
		{
		// The application loads the Homeloader container object after applicationdidfinishlaunch as the rootviewocntroller.
		// Homeloader contains references to ipad and iphone home, gethomeVC returns the corresponding reference to the home objects
		// We need references to home objects to display modalviews / call methods which are in the home object or refresh home contents.
            if(([viewControllers[0] isKindOfClass:[HomeLoaderVC class]]) ) 
            {
				HomeLoaderVC *homeLoaderVC = (HomeLoaderVC *)viewControllers[0];
                if ([homeLoaderVC respondsToSelector:@selector(getHomeVC)])
                {
                    newHome = [homeLoaderVC getHomeVC];
                }
            }
            else		// Gov does not use HomeLoader class
            {
                newHome = viewControllers[0];
            }
		}
	}

	return newHome;
}


-(UINavigationController*) getNavigationController
{
	return navController;
}


#pragma mark - Authenticating View
-(void)createAuthenticatingView
{
	[[MCLogging getInstance] log:@"ConcurMobileAppDelegate::createAuthenticatingView" Level:MC_LOG_DEBU];

	// Find the view that will parent the authenticating view
	UIView *parentView = self.window;
	
	float pw = parentView.bounds.size.width;
	float ph = parentView.bounds.size.height;

	float w = 200;
	float h = 100;

	// Create the connection view
    self.authView = [[RotatingRoundedRectView alloc] initCenteredWithParentView:parentView withHeight:h withWidth:w];
	
	// Make a "Authenticating..." label
	UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, (h / 2) - 45, w, 37)];
	[label setText:[Localizer getLocalizedText:@"Authenticating"]];
	[label setBackgroundColor:[UIColor clearColor]];
	[label setTextAlignment:NSTextAlignmentCenter];
	[label setFont:[UIFont boldSystemFontOfSize:18.0f]];
	[label setTextColor:[UIColor whiteColor]];
	[label setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
	[label setShadowOffset:CGSizeMake(0.0f, -1.0f)];
	label.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleBottomMargin;
	[authView addSubview:label];
	
	// Create an activity indicator
	const CGFloat activityDiameter = 37.0;
	UIActivityIndicatorView *activity = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake((w / 2) - (activityDiameter / 2), label.frame.origin.y + label.frame.size.height + 4, activityDiameter, activityDiameter)];
	[activity setHidesWhenStopped:YES];
	[activity setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleWhiteLarge];
	activity.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleBottomMargin;
	[activity startAnimating];
	[authView addSubview:activity];
	
    // Create a view that will cover everything behind the authentication view
    self.coverView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, pw, ph)]; // Incs ref count by 2
     // Decs ref count by 1
    [coverView setBackgroundColor:[UIColor whiteColor]];
    [coverView setAlpha:0.3];
    [parentView addSubview:coverView];
    [coverView setHidden:NO];

	// Add the connection view to its parent and show it
	[parentView addSubview:authView];
	[authView setHidden:NO];
}

-(void)destroyAuthenticatingView
{
	[[MCLogging getInstance] log:@"ConcurMobileAppDelegate::destroyAuthenticatingView" Level:MC_LOG_DEBU];
	[authView removeFromSuperview];
	self.authView = nil;
    [coverView removeFromSuperview];
    self.coverView = nil;
}

#pragma mark - Application Methods
- (void)applicationDidFinishLaunching:(UIApplication *)application
{
    ATConnect *connection = [ATConnect sharedConnection];
    connection.apiKey = @"2a4d9d876b481b64c9481db061e4028f716ea74ac57cab35e8a48a2cee9ba7a2";
    
    // Initialize GoogleAnalytics and Flurry
    [AnalyticsTracker initAnalytics];

    [FileManager cleanOldLogs];
    
	// Set the name on the main thread
	[[NSThread mainThread] setName:@"main"];

	if ([UIDevice isPad])
	{
		[[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
        if ([Config isGov])
        {
            HomeLoaderVC *newiPadHomeVC = [[UIStoryboard storyboardWithName:@"iPadHome" bundle:nil] instantiateViewControllerWithIdentifier:@"GoviPadHomeLoaderVC"];
            navController = [[UINavigationController alloc] initWithRootViewController:newiPadHomeVC];
            [window setRootViewController:navController];
        }
        else
        {
            HomeLoaderVC *newiPadHomeVC = [[UIStoryboard storyboardWithName:@"iPadHome" bundle:nil] instantiateInitialViewController];
            navController = [[UINavigationController alloc] initWithRootViewController:newiPadHomeVC];
            [window setRootViewController:navController];
            // MOB-21435 : hack to ensure that viewdidload is called and homeloader is initialized other wise findhomeVC will be nil.
            // This need to be removed when we re-write the login flow
            newiPadHomeVC.view;
        }
        // Needed for auto rotation to work on iOS6
        [window setRootViewController:navController];
	}
	else
    {
        if ([Config isGov])
        {
            HomeLoaderVC *newHomeVC = [[UIStoryboard storyboardWithName:@"Home" bundle:nil] instantiateViewControllerWithIdentifier:@"GovHomeLoaderVC"];
            navController = [[UINavigationController alloc] initWithRootViewController:newHomeVC];
            [window setRootViewController:navController];
        }
        else
        {
            HomeLoaderVC *newHomeVC = [[UIStoryboard storyboardWithName:@"Home" bundle:nil] instantiateInitialViewController];
            navController = [[UINavigationController alloc] initWithRootViewController:newHomeVC];
            [window setRootViewController:navController];
            // MOB-21435 : hack to ensure that viewdidload is called and homeloader is initialized other wise findhomeVC will be nil.
            newHomeVC.view;
        }
	}
	
    [window makeKeyAndVisible];

    UIViewController *homevc = [ConcurMobileAppDelegate findHomeVC];
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurMobileAppDelegate::applicationDidFinishLaunching:: homevc===::%@, navcontroller:%@ ", homevc,navController] Level:MC_LOG_DEBU];

	[[ApplicationLock sharedInstance] onApplicationDidFinishLaunching];
}       

- (void)applicationWillTerminate:(UIApplication *)application {
	[[ExSystem sharedInstance] saveLastGoodRequest];
    [GlobalLocationManager stopTrackingSignificantLocationUpdates];

    // flush any analytics log events here
    [AnalyticsTracker dispatchAnalytics];
}


#pragma mark - Open URL stuff
// Pre 4.2 support
- (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url 
{
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurMobileAppDelegate::application:handleOpenURL %@", url] Level:MC_LOG_DEBU];

    if ([[ApplicationLock sharedInstance] handleOpenURL:url])
    {
        return YES;
    }
    return NO;
}

// For 4.2+ support
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
        
    //Nicely close test drive incase its shown
    [[ConcurTestDrive sharedInstance] removeAnimated:NO];

    if (![self isMOBPinResetUrl:url])
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurMobileAppDelegate::application:openURL %@", url] Level:MC_LOG_DEBU];
    
    if (url != nil && [url isFileURL])
    {
        if ([[url pathExtension] caseInsensitiveCompare:@"pdf"] == NSOrderedSame)
        {
            PDFViewController *pdfViewController = [[PDFViewController alloc] initWithNibName:@"PDFViewController_iPhone" bundle:nil];
            pdfViewController.isUploaded = NO;
            pdfViewController.url = url;
            
            qeFormVC = nil;
            receiptVC = nil;
            self.isUploadPdfReceipt = YES;
            pdfViewController.isAttachedReceipt = NO;
            
            NSString *flurryStr = nil;
            
            if (![UIDevice isPad])
            {
                if ([self.navController.topViewController isKindOfClass:[QEFormVC class]])
                {
                    flurryStr = @"Open PDF from Quick Expense list";
                    
                    pdfViewController.isAttachedReceipt = YES;
                    self.qeFormVC = (QEFormVC*)self.navController.topViewController;
                }
                else if ([self.navController.topViewController isKindOfClass:[ReceiptEditorVC class]])
                {
                    self.receiptVC = (ReceiptEditorVC*)self.navController.topViewController;
                    NSArray *viewControllers = self.navController.viewControllers;
                    if (viewControllers != nil && [viewControllers count] > 1)
                    {
                        UIViewController *vc = viewControllers[([viewControllers count] - 2)];
                        if ([vc isKindOfClass:[QEFormVC class]])
                        {
                            flurryStr = @"Open PDF from Quick Expense Receipt Viewer";
                            
                            self.qeFormVC = (QEFormVC*)vc;
                            pdfViewController.isAttachedReceipt = YES;
                         }
                        else
                        {
                            if ([vc isKindOfClass:[ReportDetailViewController class]])
                                flurryStr = @"Open PDF from Report Receipt Viewer";
                            [self pushReceiptStoreVCiPhone];
                        }
                    }
                }
                else
                {
                    if ([self.navController.topViewController isKindOfClass:[ReportDetailViewController class]] || [self.navController.topViewController isKindOfClass:[ActiveReportListViewController class]])
                        flurryStr = @"Open PDF from Report";
                    
                    [self pushReceiptStoreVCiPhone];
                }
                [self.navController pushViewController:pdfViewController animated:YES];
            }
            else    // for iPad
            {
                UIViewController *topVC = nil;
                UINavigationController *localNavigationController = nil;
                NSArray *viewControllers = self.navController.presentedViewController.childViewControllers;
                
                if (viewControllers != nil && [viewControllers count] >= 1)
                {
                    //MOB-21633 - Enable 64 bit support on iOS
                    topVC = viewControllers[[viewControllers count] - 1];
                }
                // if the quick expense view is the top view
                if ([topVC isKindOfClass:[QEFormVC class]])
                {
                    flurryStr = @"Open PDF from Quick Expense list";
                    pdfViewController.isAttachedReceipt = YES;
                    
                    self.qeFormVC = (QEFormVC*)topVC;
                    [self.qeFormVC.navigationController pushViewController:pdfViewController animated:YES];
                }
                // the receipt viewer is the top view
                else if ([topVC isKindOfClass:[ReceiptEditorVC class]])
                {
                    self.receiptVC = (ReceiptEditorVC*)topVC;
                    if (viewControllers != nil && [viewControllers count] > 1)
                    {
                        UIViewController *subVC = viewControllers[([viewControllers count] - 2)];
                        if ([subVC isKindOfClass:[QEFormVC class]])
                        {
                            flurryStr = @"Open PDF from Quick Expense Receipt Viewer";
                            self.qeFormVC = (QEFormVC*)subVC;
                            pdfViewController.isAttachedReceipt = YES;
                            [self.receiptVC.navigationController pushViewController:pdfViewController animated:YES];
                        }
                    }
                    else
                    {
                        flurryStr = @"Open PDF from Report Receipt Viewer";
                      
                        [ConcurMobileAppDelegate unwindToRootView];
                        localNavigationController = [self getLocalNavigationController];
                        [self.navController presentViewController:localNavigationController animated:YES completion:nil];
                        [localNavigationController pushViewController:pdfViewController animated:YES];
                    }
//                    [self.receiptVC.navigationController pushViewController:pdfViewController animated:YES];
                }
                else
                {
                    if ([topVC isKindOfClass:[ReportDetailViewController class]] || [topVC isKindOfClass:[ActiveReportListViewController class]])
                        flurryStr = @"Open PDF from Report";
                    
                    if ([[ApplicationLock sharedInstance] isLoggedIn])
                    {

                        [ConcurMobileAppDelegate unwindToRootView];
                    }
                    localNavigationController = [self getLocalNavigationController];
                    [self.navController presentViewController:localNavigationController animated:YES completion:nil];

                    [localNavigationController pushViewController:pdfViewController animated:YES];
                }
            }
            
            if (flurryStr != nil)
            {
                NSDictionary *dict = @{@"Open PDF": flurryStr};
                [Flurry logEvent:@"Receipts: Open PDF" withParameters:dict];
            }
            
            [self.window makeKeyAndVisible];
            return  YES;
        }
    }
    
    if ([[ApplicationLock sharedInstance] handleOpenURL:url])
    {
        return YES;
    }

    return NO;
}

-(void) pushReceiptStoreVCiPhone
{

    [ConcurMobileAppDelegate unwindToRootView];
    QuickExpensesReceiptStoreVC *qeReceiptStoreVC = [QuickExpensesReceiptStoreVC alloc];
    [qeReceiptStoreVC setSeedDataAndShowReceiptsInitially:YES allowSegmentSwitch:YES allowListEdit:YES];
    [self.navController pushViewController:qeReceiptStoreVC animated:NO];
}

-(UINavigationController*) getLocalNavigationController
{
    QuickExpensesReceiptStoreVC *qeReceiptStoreVC = [QuickExpensesReceiptStoreVC alloc];
    [qeReceiptStoreVC setSeedDataAndShowReceiptsInitially:YES allowSegmentSwitch:YES allowListEdit:YES];
    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:qeReceiptStoreVC];
    
    localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    localNavigationController.toolbar.tintColor = [UIColor darkBlueConcur_iOS6];
    localNavigationController.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];
    [localNavigationController setToolbarHidden:YES];
    return localNavigationController;
}

- (BOOL) isMOBPinResetUrl:(NSURL*)url
{
    NSString *query = [url query];
    if (query == nil || [query length] == 0)
    {
        return nil;
    }
    
    NSArray  *paramList = [query componentsSeparatedByString:@"&"];
    for (NSString *param in paramList)
    {
        NSArray *components = [param componentsSeparatedByString:@"="];
        if (components != nil && [components count] == 2)
        {
            NSString *nonLowercaseKey = components[0];
            NSString *key = [nonLowercaseKey lowercaseString];
            NSString *val = components[1];
        
            if ([@"type" isEqualToString:key] && [@"MOB_PIN_RSET" isEqualToString:val])
                return true;
            
        }
    }
    return FALSE;
}


#pragma mark Push Notifications

- (id)init
{
    self = [super init];
    if (self) {
        self.pushInitialized = false;
    }
    return self;
}

- (void) initPush:(NSDictionary *)launchOptions
{

    NSString *loginId = [ExSystem sharedInstance].userName;

    if (self.pushInitialized == false)
    {
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"register push for user %@", loginId] Level:MC_LOG_INFO];
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:
             (UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)];

        self.pushInitialized = true;
    }
}

static NSString *deviceTokenStr = nil;

// Devices with enterprise build are for test devices for push notification
#ifdef ENTERPRISE
static NSString *isTest = @"Y";
#else
static NSString *isTest = @"N";
#endif

- (void)registerPush {
    if (!self.pushInitialized) // MOB-13909 restrict push notification to mobile demo user only
        return;
    
    if (![deviceTokenStr length] || [ExSystem sharedInstance].sessionID == nil)
        return;
    
    [[ApplicationLock sharedInstance] registerPhoneForPush: deviceTokenStr isTest:isTest];
    
    [self handleIpm];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *) error {
    NSDictionary *dict = [NSDictionary dictionaryWithObjectsAndKeys:@"Error",
                          [NSString stringWithFormat:@"%@", error] , nil];
    
    [Flurry logEvent:@"Push:Registration failed" withParameters:dict];
    
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Failed To Register For Remote Notifications With Error: %@", error] Level:MC_LOG_INFO];
    
    //[[CXWebSocket sharedClient] connectWithDeviceToken:@"FAILED"];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {

    [[MCLogging getInstance] log:[NSString
                                  stringWithFormat:@"Succeeded registering for push notifications. Device token: %@", deviceToken]
                           Level:MC_LOG_INFO];
    //MOB-21633 - Enable 64 bit support on iOS - length will not run over int value.
    deviceTokenStr = [NSString hexStringFromData:deviceToken length:(int)[deviceToken length]];

    //if (!(![deviceTokenStr length] || [ExSystem sharedInstance].sessionID == nil))
    
    if ([deviceTokenStr length] && [ExSystem sharedInstance].sessionID != nil) {
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"application::didRegisterForRemoteNotificationsWithDeviceToken deviceToken:%@", deviceTokenStr] Level:MC_LOG_INFO];
        [self registerPush];
    }
    
    //[[CXWebSocket sharedClient] connectWithDeviceToken:deviceTokenStr];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    //MOB-10737 iPhone 5 support
    window.frame = window.screen.bounds;
    
    [[MCLogging getInstance] log:@"ConcurMobileAppDelegate::didFinishLaunchingWithOptions" Level:MC_LOG_DEBU];
    
    [self applicationDidFinishLaunching:application];
    
    // MOB-16595: change navigation bar and toolbar items' color to blue
    if ([ExSystem is7Plus]) {
        [[UINavigationBar appearance] setTintColor:[UIColor concurBlueColor]];
        [[UIBarButtonItem appearance] setTintColor:[UIColor concurBlueColor]];
    } else {
        //TODO: this will replace all the other call we do on ViewDidLoad
        [[UINavigationBar appearance] setTintColor:[UIColor darkBlueConcur_iOS6]];
        [[UIToolbar appearance] setTintColor:[UIColor darkBlueConcur_iOS6]];
    }
    
    // Init Push, if user is logged in, or he clicked on a push notifcation
    if ([[ApplicationLock sharedInstance] isLoggedIn] || [launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey]!=nil)
	{
        [self initPush:launchOptions];
    }

    NSLog(@"Beacons: monitoring");
    
    if ([ExSystem is7Plus]) {
        [BeaconManager.sharedInstance startMonitoring];
    }
    
	return YES;
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
{
    NSLog(@"didReceiveRemoteNotification: %@", userInfo);

    [[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurMobileAppDelegate::didReceiveRemoteNotification %@", userInfo] Level:MC_LOG_DEBU];

    if (!self.pushInitialized)
        [self initPush:nil];

    NotificationEvent* event = [[NotificationEvent alloc] init];
    event.type = userInfo[@"type"];
    event.data = userInfo;

    if (event.type != nil)
    {
        NSDictionary *dictionary = @{@"Action": event.type};
        [Flurry logEvent:@"Push Notification: Action" withParameters:dictionary];
    }

    [[NotificationController sharedInstance] processNotificationEvent:event];
}

-(void) switchToPinResetView
{
    if ([[ApplicationLock sharedInstance] isLoggedIn])
    {
        // do nothing
    }
    else    // not logged in
    {
        if ([Config isNewSignInFlowEnabled] ) {	 // TODO: 9.11 This if check is temporary. 
            // if user is logged in then Get the handle to SignInStoryboard and performSeque to the password reset screen
            // Get the reference to SignIn storyboard
            
            // if storyboard is nil then instantiate the storyboard.
            // segue to password reset screen
            UIViewController *view = [ConcurMobileAppDelegate findHomeVC];
            if (view && [view respondsToSelector:@selector(showPasswordRestScreen)]) {
                [view performSelector:@selector(showPasswordRestScreen)];
            }
        }
        else
        {
            MobileViewController* topVC = [self findTopView];
            if (![topVC isKindOfClass:[LoginCreatePinVC class]])
            {
                LoginCreatePinVC* createPinVC = [[LoginCreatePinVC alloc] initWithNibName:@"LoginCreatePinVC" bundle:nil];
                [[topVC navigationController] pushViewController:createPinVC animated:YES];
            }
        }
        
    }
}

-(void) switchToCompanySignInView
{
    if ([[ApplicationLock sharedInstance] isLoggedIn])
    {
        // do nothing
    }
    else
    {
        // MOB-18214 - With new sign in flow, we donot use company code anymore. user has to enter email to get his ssourl.
        if ([Config isNewSignInFlowEnabled]) {
            // if storyboard is nil then instantiate the storyboard.
            // segue to password reset screen
            UIViewController *view = [ConcurMobileAppDelegate findHomeVC];
            if (view && [view respondsToSelector:@selector(showPasswordRestScreen)]) {
                [view performSelector:@selector(showSignInScreen)];
            }
            
        }
        else
        {
            MobileViewController* topVC = [self findTopView];
            if (![topVC isKindOfClass:[LoginOptionsViewController class]])
            {
                LoginOptionsViewController* loginOptionVC = [[LoginOptionsViewController alloc] initWithNibName:@"LoginOptionsViewController" bundle:nil];
                [[topVC navigationController] pushViewController:loginOptionVC animated:YES];
            }
        }
    }
}

-(void) switchToSafariSignInView{
    
    UIViewController* view = [ConcurMobileAppDelegate findHomeVC];

        /*
         LoginWebViewController *lWVC = [[LoginWebViewController alloc] init];
         [lWVC setLoginUrl:ssoUrl];
         lWVC.loginDelegate = loginDelegate;
         [self.navigationController pushViewController:lWVC animated:YES];
         */
        if (view && [view respondsToSelector:@selector(showSafariSignInScreen)]) {
            [view performSelector:@selector(showSafariSignInScreen)];
        }
    
    

}

- (BOOL) handlePreAuthCharge:(NSDictionary*) data
{
    // We are logged in

    [ConcurMobileAppDelegate unwindToRootView];
    ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    UINavigationController *nav = delegate.navController;
    
    if ([UIDevice isPad])
    {
        if ([Config isCorpHome])
        {
            QuickExpensesReceiptStoreVC *nextController = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
            nextController.requireRefresh = YES;
            nextController.currentAuthRefNo = [data objectForKey:@"AuthTrxId"];
            [nextController setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:YES allowListEdit:YES];
            
            UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
            localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
            [localNavigationController setToolbarHidden:NO];
            iPadHome9VC * homeVc = (iPadHome9VC*) [ConcurMobileAppDelegate findHomeVC];
//        [homeVc ResetBarColors:localNavigationController];
            [homeVc presentViewController:localNavigationController animated:YES completion:nil];
        }
    }
    else    // is iPhone
    {
        QuickExpensesReceiptStoreVC* qeVc = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
        qeVc.requireRefresh = YES;
        qeVc.currentAuthRefNo = [data objectForKey:@"AuthTrxId"];
        [qeVc setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:YES allowListEdit:YES];
        [nav pushViewController:qeVc animated:YES];
    }
    
    return TRUE;
}

//- (void) orientationChanged:(NSNotification *)notification
//{
//	UIDeviceOrientation orientation = [[UIDevice currentDevice] orientation];
//	[[Director sharedDirector] setDeviceOrientation (ccDeviceOrientation)orientation];
//}

-(MobileViewController *) findTopView
{
    NSMutableArray* allViews = [[NSMutableArray alloc] init];

    [ConcurMobileAppDelegate addViewControllersToUnwindToArray:allViews];
    MobileViewController *topVc = nil;
    if ([allViews count] > 0)
    {
        topVc = allViews[([allViews count] -1)];
    }
    return topVc;
}

#pragma mark - Background Active stuff
- (void)applicationDidBecomeActive:(UIApplication *)application
{
    [[MCLogging getInstance] log:@"ConcurMobileAppDelegate::applicationDidBecomeActive" Level:MC_LOG_DEBU];
    
    if ([[UIApplication sharedApplication] applicationIconBadgeNumber] > 0) {
        DLog(@"Has badge. Setting offer available");
        [UserDefaultsManager setOfferAvailable:YES];
        
        if (deviceTokenStr) {
            DLog(@"Has device token. Handling IPM");
            [self handleIpm];
        }
    }
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
	[[MCLogging getInstance] log:@"ConcurMobileAppDelegate::applicationDidEnterBackground" Level:MC_LOG_DEBU];
	[[ExSystem sharedInstance] saveLastGoodRequest];
    [[ApplicationLock sharedInstance] onApplicationDidEnterBackground];
	[self sendApplicationDidEnterBackgroundNotifications];
    [GlobalLocationManager stopTrackingSignificantLocationUpdates];
    [ApplicationLock sharedInstance].shouldPopUpTouchID = YES;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    isFBCallback = NO;
	[[MCLogging getInstance] log:@"ConcurMobileAppDelegate::applicationWillEnterForeground" Level:MC_LOG_DEBU];
	[[ApplicationLock sharedInstance] onApplicationWillEnterForeground];
    
    if (([self.navController.topViewController isKindOfClass:[TripDetailsViewController class]] ||
                                                    [self.navController.topViewController isKindOfClass:[DetailViewController class]] ||
                                                    [self.navController.topViewController isKindOfClass:[TripsViewController class]]) && (![ExSystem sharedInstance].isGovernment))
    {
        [GlobalLocationManager startTrackingSignificantLocationUpdates];
    }
    
}


#pragma mark -
#pragma mark Security delegate methods
- (void)applicationProtectedDataDidBecomeAvailable:(UIApplication *)application
{
}

- (void)applicationProtectedDataWillBecomeUnavailable:(UIApplication *)application
{
}

- (void)saveContext
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil)
    {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
        {
            /*
             Replace this implementation with code to handle the error appropriately.
             
             abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. If it is not possible to recover from the error, display an alert panel that instructs the user to quit the application by pressing the Home button.
             */
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
            // If this happens during migration from FIRST Gov release to SECOND gov release,
            // Feel free to delete the coredata model for gov release ONLY and use new model for the new release.
            // We are not saving anything offline for first gov release yet.
            // For more information please see Jira: MOB-18969, commit message.
        }
    }
}

#pragma mark - Core Data stack

/**
 Returns the managed object context for the application.
 If the context doesn't already exist, it is created and bound to the persistent store coordinator for the application.
 */
- (NSManagedObjectContext *)managedObjectContext
{
    if (__managedObjectContext != nil)
    {
        return __managedObjectContext;
    }
    
    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator];
    if (coordinator != nil)
    {
        __managedObjectContext = [[NSManagedObjectContext alloc] init];
        [__managedObjectContext setPersistentStoreCoordinator:coordinator];
    }
    return __managedObjectContext;
}

/**
 Returns the managed object model for the application.
 If the model doesn't already exist, it is created from the application's model.
 */
- (NSManagedObjectModel *)managedObjectModel
{
    if (__managedObjectModel != nil)
    {
        return __managedObjectModel;
    }
    NSURL *modelURL = [[NSBundle mainBundle] URLForResource:@"ExModel" withExtension:@"momd"];
    __managedObjectModel = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];    
    return __managedObjectModel;
}

/**
 Returns the persistent store coordinator for the application.
 If the coordinator doesn't already exist, it is created and the application's store added to it.
 */
- (NSPersistentStoreCoordinator *)persistentStoreCoordinator
{
    if (__persistentStoreCoordinator != nil)
    {
        return __persistentStoreCoordinator;
    }
    
    NSURL *storeURL = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"ExTest.sqlite"];
    
    NSError *error = nil;
    __persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];

    NSDictionary *options = @{NSMigratePersistentStoresAutomaticallyOption:@YES, NSInferMappingModelAutomaticallyOption:@YES, NSSQLitePragmasOption:@{@"journal_mode":@"DELETE"}};

    if (![__persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:options error:&error])
    {
        /*
         Replace this implementation with code to handle the error appropriately.
         
         abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. If it is not possible to recover from the error, display an alert panel that instructs the user to quit the application by pressing the Home button.
         
         Typical reasons for an error here include:
         * The persistent store is not accessible;
         * The schema for the persistent store is incompatible with current managed object model.
         Check the error message to determine what the actual problem was.
         
         
         If the persistent store is not accessible, there is typically something wrong with the file path. Often, a file URL is pointing into the application's resources directory instead of a writeable directory.
         
         If you encounter schema incompatibility errors during development, you can reduce their frequency by:
         * Simply deleting the existing store:
         [[NSFileManager defaultManager] removeItemAtURL:storeURL error:nil]
         
         * Performing automatic lightweight migration by passing the following dictionary as the options parameter: 
         [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithBool:YES], NSMigratePersistentStoresAutomaticallyOption, [NSNumber numberWithBool:YES], NSInferMappingModelAutomaticallyOption, nil];
         
         Lightweight migration will only work for a limited set of schema changes; consult "Core Data Model Versioning and Data Migration Programming Guide" for details.
         
         */
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
        abort();
    }    
    
    return __persistentStoreCoordinator;
}


#pragma mark - Application's Documents directory

/**
 Returns the URL to the application's Documents directory.
 */
- (NSURL *)applicationDocumentsDirectory
{
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
}

#pragma mark - Notifications

- (void)application:(UIApplication *)application didChangeStatusBarOrientation:(UIInterfaceOrientation)oldStatusBarOrientation
{
	if (authView != nil)
	{
		[authView onUIInterfaceOrientationChanged];
	}
    if ([UIDevice isPad]) {
        
    }
}

-(void) sendApplicationDidEnterBackgroundNotifications
{
	[[MCLogging getInstance] log:@"ApplicationLock::sendApplicationDidEnterBackgroundNotifications" Level:MC_LOG_DEBU];
    
    RootViewController *rvc = [ConcurMobileAppDelegate findRootViewController];
    if (rvc != nil)
    {
        NSMutableArray *viewControllersToUnwind = [[NSMutableArray alloc] init];
        [ConcurMobileAppDelegate addViewControllersToUnwindToArray:viewControllersToUnwind];
//        MOB-21633 - Enable 64 bit support on iOS
        int count  = (int)[viewControllersToUnwind count];
        for (int i =  count - 1; i >= 0; i--)
        {
            UIViewController *vc = viewControllersToUnwind[i];
            if ([vc isKindOfClass:[MobileViewController class]])
            {
                MobileViewController* mvc = (MobileViewController*)vc;
                [mvc applicationDidEnterBackground];
            }
        }
		
    }
}

-(void) processNotification:(NSNotification*)notification
{
    //NSLog(@"Received notification %@ on thread: %p with name %@.  Main thread is %p", notification.name, [NSThread currentThread], [NSThread currentThread].name, [NSThread mainThread]);
    if (notification.name != nil && [notification.name isEqualToString:@"NSManagingContextDidSaveChangesNotification"]){
        // when merge changes, should perform on main thread and make sure it completes before it release the lock. but there is not enough time to do testing
        // and also worry about deadlock, so use the old call instead. but we might use the commented out one
//        [self.managedObjectContext performSelectorOnMainThread:@selector(mergeChangesFromContextDidSaveNotification:)
//                                      withObject:notification
//                                   waitUntilDone:YES];
        [self.managedObjectContext mergeChangesFromContextDidSaveNotification:notification];
    }
}


+(BOOL)isUniversalTourScreenShown
{
    NSUserDefaults* standardUserDefaults = [NSUserDefaults standardUserDefaults];
    NSString* isTourScreenShown = (NSString*)[standardUserDefaults objectForKey:@"isUniversalTourScreenShown"];
    return [isTourScreenShown boolValue] ;
    
}

//MOB-17398: Enabled for codecoverage builds only.
// Due to bug in iOS 7 the codecoverage data is not automatically flush. so have to explicitly call flush in the main app.
// CodeCoverage configuration is used for running tests. (slows down running tests)

#if CODE_COVERAGE
-(void)flushCodeCoverageData
{
   __gcov_flush();
}
#endif


#pragma mark -
#pragma RVC methods
/**
 */



+(NSArray*)getAllViewControllers
{
	ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*)[[UIApplication sharedApplication] delegate];
	if (delegate != nil)
	{
		UINavigationController* navController = delegate.navController;
		if (navController != nil)
		{
			NSArray* allViewControllers = [ConcurMobileAppDelegate getViewControllersInNavController:navController];
			return allViewControllers;
		}
	}
	
	return nil;
}


+(NSArray*)getViewControllersInNavController:(UINavigationController*)navController
{
	__autoreleasing NSMutableArray* allControllers = [[NSMutableArray alloc] initWithObjects:nil];
    
	if (navController != nil)
	{
		NSArray* navStack = navController.viewControllers;
		if (navStack != nil)
		{
			// Get all the controllers on the navigation stack
			[allControllers addObjectsFromArray:navStack];
			
			// For each controler on the navigation stack...
			for (int i = 0; i < [navStack count]; i++)
			{
				UIViewController* navViewController = navStack[i];
                
				// ... get it's modal view controller, and it's modal view controller's modal view controller, and so on and so on
				UIViewController* modalViewController = navViewController.modalViewController;
				while (modalViewController != nil)
				{
					[allControllers addObject:modalViewController];
					modalViewController = modalViewController.modalViewController;
				}
			}
		}
	}
	
	if ([UIDevice isPad])
	{
		// Get the content controller of each popup controller belonging to a MobileViewController
        // MOB-21633 : Enable 64 bit support on iOS
		int numNavAndModalViewControllers = (int)[allControllers count];
		for (int j = 0; j < numNavAndModalViewControllers; j++)
		{
			UIViewController *navOrModalViewController = allControllers[j];
			if ([navOrModalViewController isKindOfClass:[MobileViewController class]])
			{
				NSArray* popovers = [(MobileViewController*)navOrModalViewController getPopovers];
				for (UIPopoverController *popover in popovers)
				{
					if (popover != nil && popover.popoverVisible && popover.contentViewController != nil)
					{
						[allControllers addObject:popover.contentViewController];
					}
				}
			}
		}
	}
    
	// Now that we've gathered up all the controllers in the nav controller, as well as the modal controls, and popover content controllers,
	// walk through each of them determining which are also nav controllers.  Recursively add the controllers belonging to each nav controller.
	//
	// Note: with the addition of the ExpenseLocationsViewController, the iPhone now has this structure as well the iPad,
	// so nav controllers will have to be recursed for iPhone as well as iPad.
	//
	NSMutableArray* extraControllers = [[NSMutableArray alloc] initWithObjects:nil];
    // MOB-21633: Enable 64 bit support on iOS
	for (NSUInteger k = 0; k < [allControllers count]; k++)
	{
		UIViewController *controller = allControllers[k];
		if ([controller isKindOfClass:[UINavigationController class]])
		{
			[extraControllers addObjectsFromArray:[ConcurMobileAppDelegate getViewControllersInNavController:(UINavigationController*)controller]];
		}
	}
	[allControllers addObjectsFromArray:extraControllers];
	
	return allControllers;
}

+(MobileViewController*)getMobileViewControllerByViewIdKey:(NSString*)key
{
	if (key == nil)
		return nil;
	
	NSArray* viewControllers = [ConcurMobileAppDelegate getAllViewControllers];
	int numViewControllers = (int)[viewControllers count];

    // MOB-21633: Enable 64 bit support on iOS
    if ([viewControllers count] == 0) {
        return nil;
    }
    
	for (int j = numViewControllers - 1; j >= 0; j--)
	{
		UIViewController* viewController = viewControllers[j];
		
		if ([viewController respondsToSelector:@selector(getViewIDKey)])
		{
			NSString* viewIdKey = [((MobileViewController*)viewController) getViewIDKey];
			if (viewIdKey != nil && [viewIdKey isEqualToString:key])
				return (MobileViewController*)viewController;
		}
	}
	
	return nil;
}

+(BOOL) isLoginViewShowing
{
    MobileViewController *loginViewController = [self getMobileViewControllerByViewIdKey:@"LOGIN"];
    ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    // TODO: Temporary hack to make the new login appear.
    // Ideal fix is to check if user is logged in from library.

    if (loginViewController != nil)
        return YES;
    else if ([Config isNewSignInFlowEnabled])
        return [appDelegate.topView isEqualToString:LOGIN];
    else
        return NO;
}

+(BOOL) hasMobileViewController:(MobileViewController*)viewController
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// January 25, 2011
	// This method is called to determine whether a message should be passed to a MobileViewController's
	// respondToFoundData method for processing.  Since it is believed that
	//   1) MobileViewControllers are deallocated as soon as they are removed from the view stack, and
	//   2) MobileViewController::dealloc cancels all its message requests
	// it should no longer be necessary to check whether a MobileViewController is on the view stack
	// before sending it its messages.  Therefore this method will always return YES.  After there has
	// been adequate time for testing, this method and calls to it should be removed altogether.
	//
	return YES;
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////
    
	NSArray* viewControllers = [self getAllViewControllers];
	int numViewControllers = [viewControllers count];
	
	for (int j = numViewControllers - 1; j >= 0; j--)
	{
		if (viewController == viewControllers[j])
			return YES;
	}
	
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurMobileAppDelegate::hasMobileViewController: specified view controller not found.  View controllers: %@", [self getAllViewControllers]] Level:MC_LOG_DEBU];
	return NO;
}

#pragma mark -
#pragma mark Unwind Methods
+(void) unwindToRootView
{
    NSMutableArray *viewControllersToUnwind = [[NSMutableArray alloc] init];
    [ConcurMobileAppDelegate addViewControllersToUnwindToArray:viewControllersToUnwind];
	
    int numViewControllers = (int)[viewControllersToUnwind count];
    
	for (int i = numViewControllers - 1; i >= 0; i--)
	{
		UIViewController *vc = viewControllersToUnwind[i];
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"Examining %@", [vc class]] Level:MC_LOG_DEBU];
        
        if ([vc isKindOfClass:[MobileViewController class]])
        {
            MobileViewController *mvc = (MobileViewController*)vc;
            if ([mvc isWaitViewShowing])
                [mvc hideWaitView];
        }
		
		// If this view controller is a nav, then pop to its root
		if([vc isKindOfClass:[UINavigationController class]])
		{
			// Pop to the root of the nav controller
			UINavigationController *nav = (UINavigationController*)vc;
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Popping to the root of the nav which is showing %@", [[nav.viewControllers lastObject] class]] Level:MC_LOG_DEBU];
			[nav popToRootViewControllerAnimated:NO];
		}
        
		// If this view controller is a modal, then dismiss it
		if ([vc isKindOfClass:[UIViewController class]])
        {
            // Prior to iOS 5, the presenter of the modal view controller could be gotten with parentViewController,
            // beginning with iOS 5, the presenter of the modal view controller must be gotten with presentingViewController
            UIViewController *pvc = nil;
            if ([vc respondsToSelector:@selector(presentingViewController)])
                pvc = vc.presentingViewController;
            else
                pvc = vc.parentViewController;
            
            if (pvc.modalViewController == vc)
            {
                [[MCLogging getInstance] log:[NSString stringWithFormat:@"Dismissing modal %@", [vc class]] Level:MC_LOG_DEBU];
                [vc dismissModalViewControllerAnimated:NO];
            }
        }
        
		// If this view controller is a popup, then dismiss it
		if ([vc isKindOfClass:[UIPopoverController class]])
		{
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Dismissing popover %@", [vc class]] Level:MC_LOG_DEBU];
			UIPopoverController *popover = (UIPopoverController*)vc;
			[popover dismissPopoverAnimated:NO];
		}
	}
	
}

+(void) addViewControllersToUnwindToArray:(NSMutableArray*)viewControllersToUnwind
{
	ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
	NSObject *nestedVC = delegate.navController;
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"Starting with delegate.navController"] Level:MC_LOG_DEBU];
	
	while (nestedVC != nil)
	{
		// Add this view controller to the unwind stack
		if (![viewControllersToUnwind containsObject:nestedVC])
		{
			[viewControllersToUnwind addObject:nestedVC];
			
			if ([nestedVC isKindOfClass:[UINavigationController class]])
			{
				[[MCLogging getInstance] log:[NSString stringWithFormat:@"    Queued view controller: the nav controller which is showing %@", [[((UINavigationController*)nestedVC).viewControllers lastObject] class]] Level:MC_LOG_DEBU];
			}
			else
			{
				[[MCLogging getInstance] log:[NSString stringWithFormat:@"    Queued view controller: %@", [nestedVC class]] Level:MC_LOG_DEBU];
			}
            
		}
		
		// If it *is* a nav controller, then process the top-most view controller
		if ([nestedVC isKindOfClass:[UINavigationController class]])
		{
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Next up: top-most view controller on nav stack"] Level:MC_LOG_DEBU];
			nestedVC = [((UINavigationController*)nestedVC).viewControllers lastObject];
		}
		// Else if it is the parent of a modal, then process the modal view controller
		else if ([nestedVC isKindOfClass:[UIViewController class]] && ((UIViewController*)nestedVC).modalViewController != nil)
		{
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Next up: modal"] Level:MC_LOG_DEBU];
			nestedVC = ((UIViewController*)nestedVC).modalViewController;
		}
		// Else if it is the parent of a popover, then process the popover
		else if ([nestedVC isKindOfClass:[MobileViewController class]] &&
				 ((MobileViewController*)nestedVC).pickerPopOver != nil)
		{
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Next up: popover"] Level:MC_LOG_DEBU];
			nestedVC = ((MobileViewController*)nestedVC).pickerPopOver;
		}
		// Else if it's a popover, then process popover's content
		else if ([nestedVC isKindOfClass:[UIPopoverController class]]
				 && ((UIPopoverController*)nestedVC).contentViewController != nil)
		{
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Next up: popover's content view controller"] Level:MC_LOG_DEBU];
			nestedVC = ((UIPopoverController*)nestedVC).contentViewController;
		}
		// Else we're done digging
		else
		{
			break;
		}
	}
}

#pragma mark View Switching Methods
+(BOOL)switchToView:(NSString *)to viewFrom:(NSString *)from ParameterBag:(NSMutableDictionary *)paramBag
{
	NSString *msgName = nil;
	NSString *cacheOnly = @"NO";
	
	MobileViewController *currController = nil;
	MobileViewController *nextController = nil;
    
	ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    UINavigationController *navcontroller = [ConcurMobileAppDelegate getBaseNavigationController] ;
    
	// If the view is already on top (as a result of double-click), let's return false
	UIViewController* topVC = delegate.navController.topViewController;
	if (topVC != nil &&
		[topVC isKindOfClass:MobileViewController.class] &&
		[to isEqualToString:[((MobileViewController*)topVC) getViewIDKey]] &&
		![to isEqualToString:HOME_PAGE] &&
		[ExSystem sharedInstance].sessionID != nil &&
		[[ExSystem sharedInstance].sessionID length] > 1)
		return FALSE;
	
	if([to isEqualToString:HOME_PAGE] || [to isEqualToString:SETTINGS_VIEW])
	{
		[navcontroller setToolbarHidden:YES animated:NO];
	}
	else {
		[navcontroller setToolbarHidden:NO animated:NO];
	}
	
	if([to isEqualToString:HOME_PAGE] && [from isEqualToString:AUTHENTICATION_VIEW])
	{
		currController = [self getController:from];
		// Stop the animation
        //		if (currController != nil && [currController isKindOfClass:[AuthenticationViewController class]])
        //			((AuthenticationViewController*)currController).doAnimate = NO;
        
		[currController.view removeFromSuperview];
		///[views removeObjectForKey:AUTHENTICATION_VIEW];
		//[self viewDidAppear:NO];
		return TRUE;
	}
	
	nextController = [self getController:to];
	currController = [self getController:from];
	//self.topView = to;
    
	[ExSystem sharedInstance].sys.topViewName = to;
	
	// Mob-2866 This causes logout button to be disabled immediately after login
	//self.navigationItem.rightBarButtonItem = nil; //remove the right button
	
	//if(![to isEqualToString:HOME_PAGE])
    [delegate.navController.navigationBar setHidden:NO];
	//else
    //[delegate.navController.navigationBar setHidden:YES];
    // Not sure when this is in use.  Just comment out for now.
    //    if ([UIDevice isPad] && [ExSystem sharedInstance].isSingleUser) {
    //        [delegate.navController.navigationBar setHidden:YES];
    //    }
    
	if ([to isEqualToString:SETTINGS_VIEW])
	{
		nextController.cameFrom = from;
        
	}
    
	if ([from isEqualToString:SPLASH])
	{
        
	}
	else if(from == nil)
	{
		from = [ExSystem sharedInstance].sys.topViewName;
	}
	
	if ([to isEqualToString:TRIP_DETAILS] || [to isEqualToString:ITIN_DETAILS_AIR])
	{//drill to the Trip Details view from the Trips List view
		msgName = TRIPS_DATA;
		cacheOnly = @"YES";
        
		paramBag[@"SKIP_PARSE"] = @"YES";
	}
	else if ([to isEqualToString:TRIPS])
	{//go from Trip Details view to the Trip List view
		msgName = TRIPS_DATA;
	}
	else if ([to isEqualToString:ITIN_DETAILS])
	{//drill to the itin details view from the Trip Detail view
	}
	else if ([to isEqualToString:MAP])
	{//go to maps from itin detail
		msgName = @"LOCATION";
	}
	else if ([to isEqualToString:APPROVE_REPORTS])
	{
		msgName = REPORT_APPROVAL_LIST_DATA;
	}
	else if ([to isEqualToString:APPROVE_VIEW_EXCEPTIONS])
	{
		// go from report view to the view exceptions view
        msgName = APPROVE_REPORT_DETAIL_DATA;
		cacheOnly = @"YES";
	}
	else if ([to isEqualToString:APPROVE_VIEW_COMMENTS])
	{
		// go from report view to the view comments view
        msgName = APPROVE_REPORT_DETAIL_DATA;
		cacheOnly = @"YES";
	}
	else if ([to isEqualToString:ITEMIZATION_LIST] ||
			 [to isEqualToString:APPROVE_VIEW_ITEMIZATIONS] || [to isEqualToString:APPROVE_VIEW_ATTENDEES])
	{
		NSString* role = (NSString*)paramBag[@"ROLE"];
		if (paramBag == nil || role == nil || ![role isEqualToString:ROLE_EXPENSE_TRAVELER])
		{
			msgName = APPROVE_REPORT_DETAIL_DATA;
		}
		else
		{
			msgName = ACTIVE_REPORT_DETAIL_DATA;
		}
		cacheOnly = @"YES";
	}
    //	else if ([to isEqualToString:RECEIPT_MANAGER])
    //	{
    //		if(paramBag != nil)
    //		{
    //			nextController.parameterBag = paramBag;
    //		}
    //	}
	else if ([to isEqualToString:OUT_OF_POCKET_LIST])
	{
        msgName = OOPES_DATA;
	}
	else if ([to isEqualToString:OUT_OF_POCKET_FORM])
	{
        msgName = OOPES_DATA;
		//cacheOnly = @"YES";
		paramBag[@"SKIP_CACHE"] = @"YES";
		paramBag[@"NO_STATUS"] = @"YES";
		
		paramBag[@"SKIP_PARSE"] = @"YES";
	}
	else if ([to isEqualToString:AUTHENTICATION_VIEW])
	{
        msgName = AUTHENTICATION_DATA;
	}
	else if ([to isEqualToString:ACTIVE_REPORTS])
	{
		msgName = ACTIVE_REPORTS_DATA;
	}
    else if ([to isEqualToString:APPROVE_EXPENSE_DETAILS]
             || [to isEqualToString:APPROVE_REPORT_SUMMARY]
             || [to isEqualToString:APPROVE_ENTRIES]
             || [to isEqualToString:ACTIVE_ENTRIES]) // From AddToReport or ActiveReports
    {
        [(ReportViewControllerBase*)nextController setSeedData:paramBag];
        //		msgName = ACTIVE_REPORT_DETAIL_DATA;
        //		cacheOnly = @"NO";
	}
    
	else if ([to isEqualToString:HOTEL_ROOM_LIST])
	{
		[(RoomListViewController*)nextController initData:paramBag];
		msgName = FIND_HOTEL_ROOMS;
		cacheOnly = @"NO";
	}
	else if ([to isEqualToString:HOTEL_BOOKING])
	{
		[(RoomListViewController*)nextController initData:paramBag];
	}
	else if ([to isEqualToString:HOTEL_OPTIONS])
	{
		HotelOptionsViewController *hotelOptionsViewController = (HotelOptionsViewController*)nextController;
		hotelOptionsViewController.optionTitle = (NSString*)paramBag[@"TITLE"];
	}
	else if ([to isEqualToString:HOTEL_TEXT_EDITOR])
	{
		HotelTextEditorViewController *hotelTextEditorViewController = (HotelTextEditorViewController*)nextController;
		hotelTextEditorViewController.customTitle = (NSString*)paramBag[@"TITLE"];
	}
	else if ([to isEqualToString:APPROVE_INVOICES])
	{
        msgName = APPROVE_INVOICES_DATA;
		cacheOnly = @"NO";
	}
	else if ([to isEqualToString:INVOICE_LINEITEMS])
	{
        msgName = INVOICE_DETAIL_DATA;
		cacheOnly = @"NO";
	}
	else if ([to isEqualToString:TEXT_FIELD_EDIT])
	{
		TextAreaEditVC *editVC = (TextAreaEditVC*)nextController;
		editVC.title = (NSString*)paramBag[@"TITLE"];
	}
	
	BOOL pushView = YES;
	if(paramBag != nil && (paramBag[@"DONTPUSHVIEW"] != nil))
		pushView = NO;
    
	BOOL popView = NO;
	if(paramBag != nil && (paramBag[@"POPTOVIEW"] != nil))
		popView = YES;
	
	BOOL popUntilView = NO;
	if (paramBag != nil && (paramBag[@"POPUNTILVIEW"] != nil))
		popUntilView = YES;
	
	BOOL skipCache = NO;
	if(paramBag != nil && (paramBag[@"SKIP_CACHE"] != nil))
		skipCache = YES;
    
	// Pop to root, before push and pop
	if(paramBag != nil && (paramBag[@"POP_TO_ROOT_VIEW"] != nil))
	{
		[navcontroller popToRootViewControllerAnimated:NO];
	}
	
	if(pushView && nextController != nil)
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurMobileAppDelegate::switchToView: pushOnToViewStack to: %@, from: %@", to, from] Level:MC_LOG_DEBU];
        //MOB-10136 support add car/ add hotel popup after booking air.
        if([to isEqualToString:TRIP_DETAILS] && [from isEqualToString:AIR_SELL])
            [nextController setCameFrom:from];
		[ConcurMobileAppDelegate pushOnToViewStack:nextController FromView:currController]; //this does all the pushing and popping
	}
	else if(popView)
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurMobileAppDelegate::switchToView: popViewControllerAnimated: %@", from] Level:MC_LOG_DEBU];
		[navcontroller popViewControllerAnimated:YES];
	}
	else if (popUntilView)
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurMobileAppDelegate::switchToView: popToViewController: %@", to] Level:MC_LOG_DEBU];
		[navcontroller popToViewController:nextController animated:YES];
	}
    
	if (msgName != nil)
	{//if a view specifies the message that it wants to run/create by name, then do it.  This is the start of fetching data for a view
		if (paramBag == nil)
		{
			paramBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:to, @"TO_VIEW", nil];
		}
		else
		{
			paramBag[@"TO_VIEW"] = to;
		}
		
		if (paramBag[@"SHORT_CIRCUIT"] == nil)
		{
			if(skipCache)
				paramBag[@"SKIP_CACHE"] = @"YES";
            
			paramBag[@"CACHE_ONLY"] = cacheOnly;
			
			paramBag[@"MSG_NAME"] = msgName;
            
			[self performSelector:@selector(fetchData:) withObject:paramBag afterDelay:0.1f];
			//[msgControl createMsg:msgName CacheOnly:cacheOnly ParameterBag:parameterBag SkipCache:skipCache];
		}
	}
    
	if ([from isEqualToString:AUTHENTICATION_VIEW])
	{
		[currController.view removeFromSuperview];
	}
	else if ([[currController getViewDisplayType] isEqualToString:VIEW_DISPLAY_TYPE_REGULAR])//([from isEqualToString:HOME_PAGE] || [from isEqualToString:LOGIN] || [from isEqualToString:SETTINGS_VIEW])
	{
	}
	else if ([[nextController getViewDisplayType] isEqualToString:VIEW_DISPLAY_TYPE_REGULAR])//([to isEqualToString:HOME_PAGE] || [to isEqualToString:LOGIN] || [to isEqualToString:SETTINGS_VIEW] || [to isEqualToString:AUTHENTICATION_VIEW] )
	{
	}
	else
	{
	}
    
	
	if (paramBag[@"SHORT_CIRCUIT"] != nil)
	{
		Msg *msg = [[Msg alloc] init];
		msg.parameterBag = paramBag;
		msg.idKey = @"SHORT_CIRCUIT";
		[nextController respondToFoundData:msg];
	}
	
	return TRUE;
}

+(void)fetchData:(NSMutableDictionary *)pBag
{
	NSString *msgName = pBag[@"MSG_NAME"];
	
	BOOL skipCache = NO;
	if(pBag != nil && (pBag[@"SKIP_CACHE"] != nil))
		skipCache = YES;
    
	NSString *cacheOnly = pBag[@"CACHE_ONLY"] ;
	[[ExSystem sharedInstance].msgControl createMsg:msgName CacheOnly:cacheOnly ParameterBag:pBag SkipCache:skipCache];
}


+(MobileViewController *)getController:(NSString *)to
{//this is the factory to build out the views
	//Even though the controller that we are getting is the MVC, we actually want to create the nib with the desired form class,
	//this is the class that gets inserted into the dictionary.
	//add in the one time run code defines the view here, not in switchviews, which does things like like the addInfobutton or specifying the message to create and run
    
	///MobileViewController *nextController = [self.views objectForKey:to];
	__autoreleasing MobileViewController *nextController = [ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:(NSString*)to];
    
	//self.switchDerView = to;
	BOOL didAlloc = NO;
	
	if (nextController == nil)
	{
		didAlloc = YES;
		
		if ([to isEqualToString:HOME_PAGE])
		{//Home Page View
			//nextController = [[HomePageViewController alloc] initWithNibName:@"HomePageView" bundle:nil];
		}
		else if ([to isEqualToString:LOGIN])
		{//Login virew
			nextController = [[LoginViewController alloc] initWithNibName:@"LoginView" bundle:nil];
		}
		else if ([to isEqualToString:TRIPS])
		{//Trips List view
			nextController = [[TripsViewController alloc] initWithNibName:@"TripsView" bundle:nil];
		}
		else if ([to isEqualToString:TRIP_DETAILS])
		{//Trip Details List
			nextController = [[TripDetailsViewController alloc] initWithNibName:@"TripDetailsView" bundle:nil];
		}
		else if ([to isEqualToString:ITIN_DETAILS_AIR])
		{//Segment Details Air
			nextController = [[ItinDetailsViewController alloc] initWithNibName:@"ItinDetailsViewController" bundle:nil];
		}
		else if ([to isEqualToString:ITIN_DETAILS])
		{//Itin Segment Details View
			nextController = [[ItinDetailsViewController alloc] initWithNibName:@"ItinDetailsViewController" bundle:nil];
		}
		else if ([to isEqualToString:MAP])
		{//go to maps from itin detail
			nextController = [[MapViewController alloc] initWithNibName:@"MapView" bundle:nil];
		}
		else if ([to isEqualToString:WEBVIEW])
		{//go to webview from itin detail
			nextController = [[WebViewController alloc] initWithNibName:@"WebView" bundle:nil];
		}
		else if ([to isEqualToString:SETTINGS_VIEW])
		{//Settings view
			nextController = [[SettingsViewController alloc] init];
		}
		else if ([to isEqualToString:APPROVE_REPORTS])
		{
			// move to report list view
			nextController = [[ReportApprovalListViewController alloc] initWithNibName:@"ReportApprovalListViewController" bundle:nil];
		}
		else if ([to isEqualToString:APPROVE_EXPENSE_DETAILS])
		{
			// drill to the expense detail view from the report view
			nextController = [[ReportEntryViewController alloc] initWithNibName:@"EntryHeaderView" bundle:nil];
		}
		else if ([to isEqualToString:APPROVE_REPORT_SUMMARY])
		{
			// drill to report summary view from the report view
            nextController = [[ReportSummaryViewController alloc] initWithNibName:@"ReportHeaderView" bundle:nil];
		}
		else if ([to isEqualToString:APPROVE_VIEW_EXCEPTIONS])
		{
			// drill to report summary view from the report view
			//nextController = [[ApproveReportViewExceptionsViewController alloc] initWithNibName:@"ApproveReportViewExceptionsViewController" bundle:nil];
		}
		else if ([to isEqualToString:APPROVE_VIEW_COMMENTS])
		{
			// drill to report summary view from the report view
			//nextController = [[ApproveReportViewCommentsViewController alloc] initWithNibName:@"ApproveReportViewCommentsViewController" bundle:nil];
		}
		else if ([to isEqualToString:RECEIPT_STORE_VIEWER])
		{
			nextController = [[ReceiptStoreListView alloc] initWithNibName:@"ReceiptStoreListView" bundle:nil];
		}
		else if ([to isEqualToString:OUT_OF_POCKET_LIST])
		{
			nextController = [[OutOfPocketListViewController alloc] initWithNibName:@"OutOfPocketListViewController" bundle:nil];
		}
		else if ([to isEqualToString:EXPENSE_TYPES_LIST])
		{
			nextController = [[ExpenseTypesViewController alloc] initWithNibName:@"ExpenseTypesViewController" bundle:nil];
		}
		else if ([to isEqualToString:ACTIVE_REPORTS])
		{
			// drill to the report view from the report list view
			nextController = (MobileViewController *)[[ActiveReportListViewController alloc] initWithNibName:@"MobileTableViewController" bundle:nil]; //initWithNibName:@"ReportApprovalListViewController" bundle:nil];
		}
		else if ([to isEqualToString:ACTIVE_ENTRIES] || [to isEqualToString:APPROVE_ENTRIES])
		{
			// drill to the report view from the report list view
			nextController = [[ReportDetailViewController alloc] initWithNibName:@"ReportHeaderView" bundle:nil];
		}
        else if ([to isEqualToString:RECEIPT_DETAIL_VIEW])
		{
			// drill to the report view from the report list view
			nextController = [[ReceiptDetailViewController alloc] initWithNibName:@"ReceiptDetailViewController" bundle:nil];
		}
        //		else if ([to isEqualToString:HOTEL])
        //		{
        //			nextController = [[HotelViewController alloc] initWithNibName:@"HotelViewController" bundle:nil];
        //		}
		else if ([to isEqualToString:DISTANCE])
		{
			nextController = [[DistanceViewController alloc] initWithNibName:@"DistanceViewController" bundle:nil];
		}
		else if ([to isEqualToString:HOTEL_LOCATION])
		{
			nextController = [[HotelLocationViewController alloc] initWithNibName:@"HotelLocationViewController" bundle:nil];
		}
		else if ([to isEqualToString:HOTEL_TEXT_EDITOR])
		{
			nextController = [[HotelTextEditorViewController alloc] initWithNibName:@"HotelTextEditorViewController" bundle:nil];
		}
		else if ([to isEqualToString:HOTEL_OPTIONS])
		{
			nextController = [[HotelOptionsViewController alloc] initWithNibName:@"HotelOptionsViewController" bundle:nil];
		}
		else if ([to isEqualToString:HOTEL_SEARCH_RESULTS])
		{
			nextController = [[HotelSearchResultsViewController alloc] initWithNibName:@"HotelSearchResultsViewController" bundle:nil];
		}
		else if ([to isEqualToString:HOTEL_ROOM_LIST])
		{
			nextController = [[RoomListViewController alloc] initWithNibName:@"RoomListViewController" bundle:nil];
		}
		else if ([to isEqualToString:HOTEL_DETAILS])
		{
			nextController = [[HotelDetailsViewController alloc] initWithNibName:@"HotelDetailsViewController" bundle:nil];
		}
		else if ([to isEqualToString:HOTEL_DETAILED_MAP])
		{
			nextController = [[HotelDetailedMapViewController alloc] initWithNibName:@"HotelDetailedMapViewController" bundle:nil];
		}
        //		else if ([to isEqualToString:HOTEL_BOOKING])
        //		{
        //			nextController = [[HotelBookingViewController alloc] initWithNibName:@"HotelBookingViewController" bundle:nil];
        //		}
		else if ([to isEqualToString:HOTEL_CREDIT_CARD])
		{
			nextController = [[HotelCreditCardViewController alloc] initWithNibName:@"HotelCreditCardViewController" bundle:nil];
		}
        //		else if ([to isEqualToString:CAR])
        //		{
        //			nextController = [[CarViewController alloc] initWithNibName:@"CarViewController" bundle:nil];
        //		}
		else if ([to isEqualToString:CAR_LIST])
		{
			nextController = [[CarListViewController alloc] initWithNibName:@"CarListViewController" bundle:nil];
		}
        //		else if ([to isEqualToString:CAR_DETAILS])
        //		{
        //			nextController = [[CarDetailsViewController alloc] initWithNibName:@"CarDetailsViewController" bundle:nil];
        //		}
		else if ([to isEqualToString:CAR_MAP])
		{
			nextController = [[CarMapViewController alloc] initWithNibName:@"CarMapViewController" bundle:nil];
		}
		else if ([to isEqualToString:TRAIN_BOOK])
		{
			nextController = [[TrainBookVC alloc] initWithNibName:@"TrainBookVC" bundle:nil];
		}
		else if ([to isEqualToString:TEXT_FIELD_EDIT])
		{
			nextController = [[TextAreaEditVC alloc] initWithNibName:@"TextAreaEditView" bundle:nil];
		}
	}
	
	if (nextController != nil)
	{
        if(![to isEqualToString:@"HOME_PAGE"])
            nextController.title = [Localizer getViewTitle:to]; //causing a exec bad access when running on device... might be me messing around with Breeze...
		
		///[self.views setObject:nextController forKey:to];
		// MOB-10761 use __autoreleaseing qualifier for this one, need to test for memory leak
        //		if (didAlloc)
        //			[nextController autorelease];
		
		return nextController; ///[views objectForKey:to];
	}
	else {
		// TODO - fix this
		return nil;
	}
    
	return nil;
}


+(void)pushOnToViewStack:(MobileViewController *)toView FromView:(MobileViewController *)fromView
{//this method actually does the popping and pushing of things on to the stack
	
	if([[toView getViewDisplayType] isEqualToString:VIEW_DISPLAY_TYPE_NAVI])
	{
        ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        [delegate.navController pushViewController:toView animated:YES];
		
	}
	else if([[toView getViewDisplayType] isEqualToString:VIEW_DISPLAY_TYPE_REGULAR])
	{
		//get individual animation information from the view and then invoke it here against the main UIView
		if (fromView != nil)
		{
			[fromView.view removeFromSuperview];
			fromView = nil;
		}
        
        // TODO: Fix this later       if(![[toView getViewIDKey] isEqualToString:HOME_PAGE])
        //            [self.view insertSubview:toView.view atIndex:0];
	}
    else if([[toView getViewDisplayType] isEqualToString:@"VIEW_DISPLAY_TYPE_MODAL_NAVI"])
	{
        UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:toView];
        navi.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];
		// TODO : Fix this later[self presentViewController:navi animated:YES completion:nil];
	}
	else if([[toView getViewDisplayType] isEqualToString:VIEW_DISPLAY_TYPE_MODAL])
	{
        // TODO : Fix this later - use homevc and presen it modally.  [self presentViewController:toView animated:YES completion:nil];
	}
	
	[[ExSystem sharedInstance].sys setTopViewName:[toView getViewIDKey]];
}

+(void) refreshTopViewData:(Msg *)msg
{//this is where the rvc asks the top view if it wants to use the currently retrieved data
	
	if (msg.responder != nil && msg.responder.cancellationReceived)
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurMobileAppDelegate::refreshTopViewData: dropping cancelled message %@.", msg.idKey] Level:MC_LOG_DEBU];
		return;
	}
	
	MobileViewController *mvc = [ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:[ExSystem sharedInstance].sys.topViewName];
	if (mvc != nil)
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurMobileAppDelegate::refreshTopViewData: invoking %@::respondToFoundData:%@", [mvc class], msg.idKey] Level:MC_LOG_DEBU];
		[mvc respondToFoundData:msg];
	}
	else
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurMobileAppDelegate::refreshTopViewData: message not handled: %@.  %@, which is the top view according to topViewName, was not found.  View controllers: %@", msg.idKey, ([ExSystem sharedInstance].sys.topViewName != nil ? [ExSystem sharedInstance].sys.topViewName : @"nil"), [ConcurMobileAppDelegate getAllViewControllers]] Level:MC_LOG_DEBU];
	}
    
}


//detects what button was pressed and does an action abased on that
+(void)switchViewsByTag:(id)sender
{
	UIControl *btn = (UIControl*)sender;
	
	if (btn.tag == 100002)
	{//sign in button and the home button
		[ConcurMobileAppDelegate switchToView:SETTINGS_VIEW viewFrom:LOGIN ParameterBag:nil];
	}
	else if (btn.tag == 102000 || btn.tag == 101999)
	{//btnBack from Info screen (102000) or the Logout button (101999), show the login screen
		if (btn.tag == 102000)
		{
			[ConcurMobileAppDelegate switchToView:LOGIN viewFrom:INFO ParameterBag:nil];
		}
		else if (btn.tag == 101999)
		{
			[[ExSystem sharedInstance] clearSession]; //@"";
			[ConcurMobileAppDelegate switchToView:LOGIN viewFrom:HOME_PAGE ParameterBag:nil];
		}
	}
}

+(void)switchViews:(id)sender ParameterBag:(NSMutableDictionary *)paramBag
{
	UIControl *btn = (UIControl*)sender;
    
	if (btn.tag == 100002)
	{//sign in button and the home button
		[ConcurMobileAppDelegate switchToView:SETTINGS_VIEW viewFrom:LOGIN ParameterBag:nil];
	}
	else if (btn.tag == 100001 || btn.tag == 100 || btn.tag == 101)
	{//sign in button and the home button
		[ConcurMobileAppDelegate switchToView:HOME_PAGE viewFrom:LOGIN ParameterBag:nil];
	}
	else if (btn.tag == 600001)
	{//drill to the Trip Details view from the Trips List view
		[ConcurMobileAppDelegate switchToView:TRIP_DETAILS viewFrom:TRIPS ParameterBag:paramBag];
	}
	else if (btn.tag == 600002)
	{//go from Trip Details view to the Trip List view
		[ConcurMobileAppDelegate switchToView:TRIPS viewFrom:TRIP_DETAILS ParameterBag:nil];
	}
	else if (btn.tag == 600003)
	{//drill to the itin details view from the Trip Detail view
		[ConcurMobileAppDelegate switchToView:ITIN_DETAILS viewFrom:TRIP_DETAILS ParameterBag:nil];
	}
	else if (btn.tag == 600004)
	{//go from itin details details view to the segments Trip Detail view
		[ConcurMobileAppDelegate switchToView:TRIP_DETAILS viewFrom:ITIN_DETAILS ParameterBag:nil];
	}
	else if (btn.tag == 700001)
	{//go to maps from itin detail
		[ConcurMobileAppDelegate switchToView:MAP viewFrom:ITIN_DETAILS ParameterBag:nil];
	}
	else if (btn.tag == 700002)
	{//go to webview from itin details
		[ConcurMobileAppDelegate switchToView:WEBVIEW viewFrom:ITIN_DETAILS ParameterBag:nil];
	}
	else if (btn.tag == 101032)
	{
		[ConcurMobileAppDelegate switchToView:APPROVE_REPORTS viewFrom:HOME_PAGE  ParameterBag:nil];
	}
	else if (btn.tag == 101031 || btn.tag == 101002 || btn.tag == 101010)
	{//expense, trip, weather button from homepage view
		if (btn.tag == 101031)
		{//go to expense list
			[ConcurMobileAppDelegate switchToView:ENTRIES viewFrom:HOME_PAGE ParameterBag:nil];
		}
		else if (btn.tag == 101002)
		{//go to trips
			[ConcurMobileAppDelegate switchToView:TRIPS viewFrom:HOME_PAGE ParameterBag:nil];
		}
		else if (btn.tag == 101010)
		{//go to southwest
            // TODO : Fix this later
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
		}
	}
	else if (btn.tag == 102000 || btn.tag == 101999)
	{//btnBack from Info screen (102000) or the Logout button (101999), show the login screen
		if (btn.tag == 102000)
		{
			[ConcurMobileAppDelegate switchToView:LOGIN viewFrom:INFO ParameterBag:nil];
		}
		else if (btn.tag == 101999)
		{
			[ConcurMobileAppDelegate switchToView:LOGIN viewFrom:HOME_PAGE ParameterBag:nil];
		}
	}
	else if (btn.tag == 100003)
	{//btnInfo from login screen, show the registration screen
		[ConcurMobileAppDelegate switchToView:PWD_LOGIN viewFrom:LOGIN ParameterBag:nil];
	}
}

-(void) refreshTopViewData:(Msg *)msg
{//this is where the rvc asks the top view if it wants to use the currently retrieved data
    
	if (msg.responder != nil && msg.responder.cancellationReceived)
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::refreshTopViewData: dropping cancelled message %@.", msg.idKey] Level:MC_LOG_DEBU];
		return;
	}
    
	MobileViewController *mvc = [ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:[ExSystem sharedInstance].sys.topViewName];
	if (mvc != nil)
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::refreshTopViewData: invoking %@::respondToFoundData:%@", [mvc class], msg.idKey] Level:MC_LOG_DEBU];
		[mvc respondToFoundData:msg];
	}
	else
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::refreshTopViewData: message not handled: %@.  %@, which is the top view according to topViewName, was not found.  View controllers: %@", msg.idKey, ([ExSystem sharedInstance].sys.topViewName != nil ? [ExSystem sharedInstance].sys.topViewName : @"nil"), [ConcurMobileAppDelegate getAllViewControllers]] Level:MC_LOG_DEBU];
	}
    
}

#pragma mark - IPM

- (void)handleIpm {
    NSLog(@"handleIpm");
    
    NSString *user = [ExSystem sharedInstance].userName;
    
    NSLog(@"handleIpm user = %@ token = %@", user, deviceTokenStr);
    
    if ([UserDefaultsManager isOfferAvailable]) {
        [GoGoCloud getOfferForUser:user
                   withDeviceToken:deviceTokenStr
                      successBlock:^(GoGoOffer *offer) {
                          NSLog(@"handleIpm: Success. Invoking handler.");
                          GoGoNotificationHandler *handler = [[GoGoNotificationHandler alloc] init];
                          
                          [handler processOffer:offer
                            forApplicationState:[[UIApplication sharedApplication] applicationState]];
                      } failureBlock:^(NSError *error) {
                          NSLog(@"GoGoOffer Error: %@", error);
                      }
         ];
    }
}

@end
