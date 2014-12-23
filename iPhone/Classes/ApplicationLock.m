//
//  ApplicationLock.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 11/10/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "ApplicationLock.h"
#import "ExpenseTypesManager.h"

#import "ReceiptStoreListView.h"
#import "SystemConfig.h"
#import "ExSystem.h"
#import "EntitySystem.h"
#import "OAuthUserManager.h"
#import "EntityOAuthUser.h"
#import "TripItCacheData.h"
#import "ValidateSessionData.h"
#import "TripItExpenserViewController.h"
#import "Flurry.h"
#import "UploadQueue.h"
#import "GovWarningMessagesData.h"
#import "Home9VC.h"
#import "ConcurMobileAppDelegate.h"
#import "Config.h"
#import "SalesforceUserManager.h"
#import "NotificationController.h"
#import "Config.h"
#import "KeychainManager.h"
#import "ReceiptManager2.h"

#import "RegisterPush.h"
#import "UserConfig.h"
#import "Localizer.h"
#import "CTENetworkSettings.h"

#import "AnalyticsTracker.h"
@interface ApplicationLock()
// when loading from a fresh app start and we have a url, we need to wait for the Homescreen to load up and display  the login screen. :/
@property BOOL isSessionExpiredOverrideOnce;
@property (nonatomic, readwrite, assign) BOOL shouldWaitForHomeScreenToLoad;
@property (nonatomic,copy) NSString *resetPinKeyPartB;
@property BOOL explicitSignOut;
@end

@implementation ApplicationLock

static ApplicationLock *sharedInstance;

#pragma mark - Properties

#pragma mark - Lifetime

+(ApplicationLock*)sharedInstance
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[ApplicationLock alloc] init];
    });
    return sharedInstance;
}



#pragma mark - Auto Login Methods

-(BOOL) shouldLoginWithConcurAccessToken
{
	NSString *token = [ExSystem sharedInstance].concurAccessToken;
	
	BOOL shouldLogin = (token != nil && [token length] > 0);
	
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::shouldLoginWithConcurAccessToken returning %@", shouldLogin ? @"true" : @"false"] Level:MC_LOG_INFO];
	
	return shouldLogin;
}

-(BOOL) canAttemptAutoLogin
{
    if ([Config isGov])
    {
        [[MCLogging getInstance] log:@"ApplicationLock::canAttemptAutoLogin: auto login not allowed for Gov" Level:MC_LOG_INFO];
        return NO;
    }
	else if (![[ExSystem sharedInstance].entitySettings.autoLogin isEqualToString:@"YES"])
	{
		[[MCLogging getInstance] log:@"ApplicationLock::canAttemptAutoLogin: auto login is turned off" Level:MC_LOG_INFO];
		return NO;
	}
    // MOB-9804, MOB-9293 Do not attempt auto-login while in offline mode.
    else if(![ExSystem connectedToNetwork])
	{
		[[MCLogging getInstance] log:@"ApplicationLock::canAttemptAutoLogin: offline precludes auto login" Level:MC_LOG_DEBU];
		return NO;
	}
	
    if ([self shouldLoginWithConcurAccessToken])
        return YES;
	else if([ExSystem sharedInstance].isCorpSSOUser)
        return YES;
    
    return NO;
}

-(void) attemptAutoLogin
{
    // Prevent reentrance.  Here is a scenario where reentrance would otherwise be possible.
    // 1. Turn auto-login on
    // 2. From the home screen, bring up the settings view and leave it open
    // 2. Background app
    // 3. Wait for session to expire
    // 4. Return app to foreground
    if (self.isAutoLoginInProgress)
        return;
    
	self.isAutoLoginInProgress = true;
	
	ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*)[[UIApplication sharedApplication] delegate];
	[delegate createAuthenticatingView];
    
	NSMutableDictionary *pBag = nil;
    
    if ([ExSystem sharedInstance].isCorpSSOUser)
    {
 		[[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::attemptAutoLogin for SSO user %@", [ExSystem sharedInstance].userName == nil? @"":[ExSystem sharedInstance].userName] Level:MC_LOG_INFO];

        pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"LOGIN_WITH_CONCUR_ACCESS_TOKEN", @"YES", @"SKIP_CACHE", nil];
    }
    else if ([self shouldLoginWithConcurAccessToken])
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::attemptAutoLogin with access token for %@", [ExSystem sharedInstance].userName == nil? @"":[ExSystem sharedInstance].userName] Level:MC_LOG_INFO];
		
		pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"LOGIN_WITH_CONCUR_ACCESS_TOKEN", @"YES", @"SKIP_CACHE", nil];
	}
	else
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::attemptAutoLogin with Pin for %@", [ExSystem sharedInstance].userName == nil? @"":[ExSystem sharedInstance].userName] Level:MC_LOG_INFO];

		pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[ExSystem sharedInstance].userName, @"USER_ID", [ExSystem sharedInstance].pin, @"PIN", @"YES", @"SKIP_CACHE", nil];
	}
	
	[[ExSystem sharedInstance].msgControl createMsg:AUTHENTICATION_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) didAttemptAutoLogin:(Msg*)msg
{
	self.isAutoLoginInProgress = false;
	
	ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
	[delegate destroyAuthenticatingView];
	
	if (msg.responseCode != 200 )
	{
		[[MCLogging getInstance] log:@"ApplicationLock::didAttemptAutoLogin: failed. Will show manual login view." Level:MC_LOG_INFO];
        
        if (msg.responseCode == 401) // Unauthorized
        {
            [[ExSystem sharedInstance] clearAccessToken];
        }
        
		[self showManualLoginViewAfterDelay];
		return;
	}
	
	Authenticate *auth = (Authenticate *)msg.responder;
	if ([auth.remoteWipe isEqualToString:@"Y"])
	{
		[[MCLogging getInstance] log:@"ApplicationLock::didAttemptAutoLogin: succeeded. Wiping application." Level:MC_LOG_INFO];
		[self wipeApplication];
		[self showManualLoginViewAfterDelay];
	}
	else
	{
		[[MCLogging getInstance] log:@"ApplicationLock::didAttemptAutoLogin: succeeded." Level:MC_LOG_INFO];
		[self onLoginSucceeded:msg];
	}
}


#pragma mark - Login Methods

-(BOOL) isLoggedIn
{
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::isLoggedIn returned %@", self.isUserLoggedIn ? @"true" : @"false"] Level:MC_LOG_INFO];

	return self.isUserLoggedIn;
}

-(void) loginAndAllowAutoLogin:(BOOL)allowAutoLogin
{
	if (self.isAutoLoginInProgress)
	{
		[[MCLogging getInstance] log:@"ApplicationLock::loginAndAllowAutoLogin: auto login is already in progress" Level:MC_LOG_INFO];
		return;
	}
	
	if (self.isManualLoginScheduled)
	{
		[[MCLogging getInstance] log:@"ApplicationLock::loginAndAllowAutoLogin: manual login is already scheduled" Level:MC_LOG_INFO];
		return;
	}
	
	if ([ConcurMobileAppDelegate isLoginViewShowing])
	{
		[[MCLogging getInstance] log:@"ApplicationLock::loginAndAllowAutoLogin: already showing login view" Level:MC_LOG_INFO];
		return;
	}
	
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::loginAndAllowAutoLogin: allowAutoLogin parameter is %@", allowAutoLogin ? @"true" : @"false"] Level:MC_LOG_INFO];

	// Dismiss alert views, action sheets, and bump
	[self dismissAlertViewsAndActionSheets];
    
	// Unwind the view controller stack to TestDrive(rootVC) if never logged in before
	if (![ConcurMobileAppDelegate isLoginViewShowing] && ![ExSystem sharedInstance].isTestDrive)
	{
		[[MCLogging getInstance] log:@"ApplicationLock::loginAndAllowAutoLogin: login view not showing. Unwinding to root." Level:MC_LOG_INFO];
        [ConcurMobileAppDelegate unwindToRootView];
	}
	
    if (allowAutoLogin && [self canAttemptAutoLogin])
    {
        // We will not logout unless the autoLogin attempt fails.  See didAttemptAutoLogin.
        [self attemptAutoLogin];
    }
    else
    {
        [self showManualLoginViewAfterDelay];
    }
}

#pragma mark - Manual Login Methods

-(void) showManualLoginViewAfterDelay
{
	if ([ConcurMobileAppDelegate isLoginViewShowing])
	{
		[[MCLogging getInstance] log:@"ApplicationLock::showManualLoginViewAfterDelay: login view is already showing" Level:MC_LOG_INFO];
		return;
	}

	if (self.isManualLoginScheduled)
	{
		[[MCLogging getInstance] log:@"ApplicationLock::showManualLoginViewAfterDelay: login view is already scheduled to be shown" Level:MC_LOG_INFO];
		return;
	}

	[[MCLogging getInstance] log:@"ApplicationLock::showManualLoginViewAfterDelay: performing selector after delay on showManualLoginView" Level:MC_LOG_INFO];

	self.isManualLoginScheduled = true;
	[self performSelector:@selector(showManualLoginView) withObject:nil afterDelay:0.05f];
}

-(void) showManualLoginView
{
	if ([ConcurMobileAppDelegate isLoginViewShowing])
	{
		[[MCLogging getInstance] log:@"ApplicationLock::showManualLoginView: login view is already showing" Level:MC_LOG_INFO];
	}
	else
	{
		[[MCLogging getInstance] log:@"ApplicationLock::showManualLoginView" Level:MC_LOG_INFO];
        self.shouldPopUpTouchID = self.explicitSignOut ? NO:YES;
        UIViewController *homevc = [ConcurMobileAppDelegate findHomeVC];
        
        
        if ([homevc respondsToSelector:@selector(showManualLoginView)])
        {
            [homevc performSelector:@selector(showManualLoginView) withObject:nil];
        }
	}
	self.isManualLoginScheduled = false;
}

#pragma mark - Login Handlers

-(void) onLoginSucceeded:(Msg*)msg
{
	//
	// Called whenever any type of login, automatic or manual, succeds
	//
	[[MCLogging getInstance] log:@"ApplicationLock::onLoginSucceeded" Level:MC_LOG_DEBU];
	// Received all user roles after updateSettings
	[[ExSystem sharedInstance] updateSettings:msg];
	NSString *sess = [ExSystem sharedInstance].sessionID;
	if (sess == nil || [sess length] == 0)
	{
		[[MCLogging getInstance] log:@"ApplicationLock::onLoginSucceeded: ERROR: NO SESSION!!!" Level:MC_LOG_DEBU];
		[self loginAndAllowAutoLogin:false];
		return;
	}
    
    //Extract safeHarbor information for Gov user
    Authenticate *auth = (Authenticate*) msg.responder;
    NSDictionary *authData = [[NSMutableDictionary alloc] initWithObjectsAndKeys:auth.needAgreement, @"NEED_SAFEHARBOR", nil];
    
    //TODO: Need this resolved before checking in - check if this is only for GOV
    if ([Config isGov]) {
        UIViewController *homevc = [ConcurMobileAppDelegate findHomeVC];
        
        if ([homevc respondsToSelector:@selector(savePrarmetersAfterLogin:)])
        {
            [homevc performSelector:@selector(savePrarmetersAfterLogin:) withObject:authData];
        }

    }
    
    self.isUserLoggedIn = true;
    
    // Save now in case abrupt app termination (in simulator) prevents saving during normal termination
    [[ExSystem sharedInstance] saveLastGoodRequest];  
    
    [self doPostLoginInitialization];
    
    // MOB-10003 Now that we have logged in and finished post-login app configuration, write the amount of time it took to Flurry.
    if ([msg.responder isKindOfClass:[Authenticate class]])
    {
        Authenticate *auth = (Authenticate*)msg.responder;
        NSString *authEndpoint = [auth.path lastPathComponent];
        
        // Units of NSInterval is seconds.  Multiplying by 1000 to get milliseconds.
        int msAuthentication = ([auth.dateResponseReceived timeIntervalSinceDate:auth.dateCreated] * 1000.0);
        int msParsing = ([auth.dateResponseParsed timeIntervalSinceDate:auth.dateResponseReceived] * 1000.0);
        int msConfig = ([[NSDate date] timeIntervalSinceDate:auth.dateResponseParsed] * 1000.0);

        if ([auth.userId lengthIgnoreWhitespace]) {
            [AnalyticsTracker updateCID:[NSString stringWithFormat:@"%@",auth.userId]];
        }
    }
    
    ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    [appDelegate initPush:nil];
    [appDelegate registerPush];
    [[NotificationController sharedInstance] processNotificationEvent:nil];
}

-(void) doPostLoginInitialization
{
    /*
        Post-login initialization code is any code that needs to run as soon as a user logins in OR as soon as it is determined that a newly launched app will use the existing session that was left over from the last time it ran.  An example is the code that configures the More button on the iPhone home screen.
     */

    // Notify Tab bar about succesful login.
    NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
    [defaultCenter postNotificationName:NotificationOnLoginSuccess object:self];
    
    UIViewController *homevc = [ConcurMobileAppDelegate findHomeVC];
    
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::doPostLoginInitialization. homevc===::%@  ", homevc] Level:MC_LOG_DEBU];
  
    if ([homevc respondsToSelector:@selector(doPostLoginInitialization)])
    {
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::doPostLoginInitialization. calling homevc::doPostLoginInitialization::%@  ", homevc] Level:MC_LOG_DEBU];
        [homevc performSelector:@selector(doPostLoginInitialization) withObject:nil];
    }
    // Load userConfig once on login rather than checking and loading at all the VCs that need it
    if ([ExSystem connectedToNetwork] && [UserConfig getSingleton] == nil) {
        [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_USER_CONFIG CacheOnly:@"NO" ParameterBag:nil SkipCache:YES RespondTo:self];
    }
    
    [self checkForTripItCacheKey];
}

-(void) onLoginViewAppeared
{
    [[NSNotificationCenter defaultCenter] postNotificationName:@"MESSAGE_LOGIN_VIEW_HAS_LOADED" object:self];
}

#pragma mark - Logout Method
-(void) sendLogoutMsg
{
    NSString *path = [NSString stringWithFormat:@"%@/mobile/MobileSession/Logout", [ExSystem sharedInstance].entitySettings.uri];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    RequestController *rc = [RequestController alloc];	
    Msg *msg = [[Msg alloc] initWithData:@"Logout" State:@"" Position:nil MessageData:nil URI:path MessageResponder:nil ParameterBag:pBag];
    
    [msg setHeader:[ExSystem sharedInstance].sessionID];
    [msg setContentType:@"application/xml"];
    [msg setMethod:@"POST"];
    msg.skipCache = YES;
    
    [rc initDirect:msg MVC:nil];				

}

-(void) logout
{
    if ([[ExSystem sharedInstance] isValidSessionID:[ExSystem sharedInstance].sessionID]) //MOB-9212
        [self sendLogoutMsg];
    
      
    if ([ExSystem sharedInstance].isCorpSSOUser) 
    {
        [ExSystem sharedInstance].isCorpSSOUser = NO;
        //[[ExSystem sharedInstance] clearCompanySSOLoginPageUrl];
        [[ExSystem sharedInstance] deleteCorpSSOSessionCookie];
        [ExSystem sharedInstance].userName = nil; // wipe out the user id
        [[ExSystem sharedInstance] clearUserId];
        [ExSystem sharedInstance].userInputOnLogin = nil;
        [[ExSystem sharedInstance] clearUserInputOnLogin];
    }
	else
	{
		[[MCLogging getInstance] log:@"ApplicationLock::logout" Level:MC_LOG_DEBU];
	}
	
	[[ExSystem sharedInstance] clearUserCredentialsAndSession];
    [[SalesForceUserManager sharedInstance] clearSalesForceCredentials];

    [[ExSystem sharedInstance] saveLastGoodRequest]; // Saves time of last good request which is nil (see call to clearUserCredentialsAndSession above)
	
	if([ExSystem connectedToNetwork])
		[[ExSystem sharedInstance] clearRoles];

	[[ExSystem sharedInstance] saveSystem];
	
	[SystemConfig setSingleton:nil];
	[UserConfig setSingleton:nil];
	
    [[TravelCustomFieldsManager sharedInstance] deleteAll];
    // MOB-13328
	// I don't think this is necessary since we don't use the plist receipt metadata anymore.  Remove this soon.
    [[ExSystem sharedInstance].receiptData clearCache];
    // remove receipts on logout
    ReceiptManager2 *receiptManager = [[ReceiptManager2 alloc] init];
    [receiptManager clearCachedReceipts];

	[[ExpenseTypesManager sharedInstance] clearCache];
    
    UIViewController *homevc = [ConcurMobileAppDelegate findHomeVC];
    //TODO: RVC - Temporary Hack to clear the home screen data implement the onLogout as required.
    [[HomeManager sharedInstance] clearAll];
    if([homevc respondsToSelector:@selector(onLogout)])
    {
        [homevc performSelector:@selector(onLogout) withObject:nil];
    }
}

#pragma mark - Event Handlers

-(void) onHomeScreenAppeared
{
	if (self.isApplicationBackgrounded)
	{
		[[MCLogging getInstance] log:@"ApplicationLock::onHomeScreenAppeared: app is backgrounded. Ignoring notification." Level:MC_LOG_DEBU];
		return;
	}
	
	if ([self isLoggedIn])
	{
		[[MCLogging getInstance] log:@"ApplicationLock::onHomeScreenAppeared: already logged in." Level:MC_LOG_DEBU];
	}
	else
	{
        [self loginAndAllowAutoLogin:true];
	}
}

-(void) onLogoutButtonPressed
{
	[[MCLogging getInstance] log:@"ApplicationLock::onLogoutButtonPressed" Level:MC_LOG_DEBU];
	self.isShowLoginView = [ConcurMobileAppDelegate isLoginViewShowing];
    self.explicitSignOut = YES;
	[self logout];
	[self loginAndAllowAutoLogin:false];
}

-(void) onServerRejectedRequest
{
	[[MCLogging getInstance] log:@"ApplicationLock::onServerRejectedRequest" Level:MC_LOG_DEBU];
	
	[self loginAndAllowAutoLogin:true];
}

-(void) onApplicationDidEnterBackground
{
	[[MCLogging getInstance] log:@"ApplicationLock::onApplicationDidEnterBackground" Level:MC_LOG_DEBU];
	
    if (self.isUserLoggedIn)
        [[UploadQueue sharedInstance] onApplicationDidEnterBackground];

    self.isApplicationBackgrounded = true;
	self.inactivityBeginDate = [NSDate date];
	[self dismissAlertViewsAndActionSheets];
}

-(void) onApplicationWillEnterForeground
{
	[[MCLogging getInstance] log:@"ApplicationLock::onApplicationWillEnterForeground" Level:MC_LOG_DEBU];
    self.isUserLoggedIn = false;
    self.isApplicationBackgrounded = false;
	[self checkConnectionAndSessionWhileLaunching:NO];
    self.shouldWaitForHomeScreenToLoad = NO;
}

-(void) onApplicationDidFinishLaunching
{
    [[MCLogging getInstance] log:@"ApplicationLock::onApplicationDidFinishLaunching" Level:MC_LOG_DEBU];
	self.isUserLoggedIn = false;
    
    //
    // compare current preferred language to last used preferred language
    //  if different, declare the session expired to pull new language strings from the server
    //
    NSString* preferredLanguage = [Localizer getPreferredLanguage];
    NSUserDefaults* standardUserDefaults = [NSUserDefaults standardUserDefaults];
    NSString* previousPreferredLanguage = (NSString*)[standardUserDefaults objectForKey:@"PreviousLanguage"];
    // on first run, previous preferred language will be nil, forcing a refresh.
    // this is intended to cover language upgrades for previous users
    if( ![preferredLanguage isEqualToString:previousPreferredLanguage] )
    {
        self.isSessionExpiredOverrideOnce = true;
    }
    
    [standardUserDefaults setObject:preferredLanguage forKey:@"PreviousLanguage"];
    [standardUserDefaults synchronize];
    
    // Set the ConcurLibrary language prefernce here.
    [[CTENetworkSettings sharedInstance] saveLocaleForServer:preferredLanguage];
    // For concurSDK - set the server path to the url specified in the settings.
    [[CTENetworkSettings sharedInstance] saveServerURL:[ExSystem sharedInstance].entitySettings.uri];
#ifdef DEBUG
    [[CTENetworkSettings sharedInstance] enableDebugMode];
#endif
    
    [self checkConnectionAndSessionWhileLaunching:YES];
    self.shouldWaitForHomeScreenToLoad = YES;
}

-(void) showOfflineAlert
{
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::showOfflineAlert"] Level:MC_LOG_DEBU];

    MobileAlertView *alert = [[MobileAlertView alloc] 
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"Offline limits features"]
                              delegate:nil 
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
    [alert show];
}

-(void) onHandledOpenUrl
{
	[[MCLogging getInstance] log:@"ApplicationLock::onHandledOpenUrl" Level:MC_LOG_DEBU];
    [self checkForTripItCacheKey];
}


#pragma mark - Checking Methods
-(void) checkConnectionAndSessionWhileLaunching:(BOOL)isAppLaunching
{
	[[MCLogging getInstance] log:@"ApplicationLock::checkConnectionAndSessionWhileLaunching" Level:MC_LOG_DEBU];
    
    // Note: the session will be checked after the connection is checked
    [self checkConnectionWhileLaunching:isAppLaunching];
}

-(BOOL) hasSession
{
	return [[ExSystem sharedInstance] isValidSessionID:[ExSystem sharedInstance].sessionID];
}

-(BOOL) isSessionExpired
{
    if( self.isSessionExpiredOverrideOnce )
    {
        self.isSessionExpiredOverrideOnce = false;
        return true;
    }
    
	BOOL isExpired = false;
    
    int minutesUntilSessionExpiration = [[ExSystem sharedInstance].msgControl minutesUntilSessionExpires];
    
    int requiredRemainingMinutesForSessionReuse = 5;
    
#ifdef UNWIND_TEST
    requiredRemainingMinutesForSessionReuse = 10000;
#endif
    
    if (minutesUntilSessionExpiration < requiredRemainingMinutesForSessionReuse)
    {
        isExpired = true;
    }

    return isExpired;
}

-(BOOL) canUseExpiredSession
{
    BOOL isOffline = ![ExSystem connectedToNetwork];
    BOOL isAutoLoginTurnedOn = [[ExSystem sharedInstance].entitySettings.autoLogin isEqualToString:@"YES"];

    if ([Config isGov])
        isAutoLoginTurnedOn = NO;
    return (isOffline && isAutoLoginTurnedOn);
}

-(BOOL) canUseExistingSession
{
    if (![self hasSession])
        return false;

    if ([self isSessionExpired])
    {
        if ([self canUseExpiredSession])
        {
            [[MCLogging getInstance] log:@"ApplicationLock::canUseExistingSession: session expired, working offline" Level:MC_LOG_DEBU];
            return true;
        }
        else
        {
            [[MCLogging getInstance] log:@"ApplicationLock::canUseExistingSession: not enough minutes left in session" Level:MC_LOG_DEBU];
            return false;
        }
    }
    
    return true;
}

-(void) checkConnectionWhileLaunching:(BOOL)isAppLaunching
{
	[[MCLogging getInstance] log:@"ApplicationLock::checkConnectionWhileLaunching" Level:MC_LOG_DEBU];
	
	// Check whether we're offline
    BOOL isOffLine = ![ExSystem connectedToNetwork];
	
	if (isOffLine)
        [self showOfflineAlert];
    
    [self checkSession:isAppLaunching];
}

-(void) checkSession:(BOOL)isAppLaunching
{
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::checkSession. isAppLaunching=%@", (isAppLaunching ? @"YES" : @"NO")] Level:MC_LOG_DEBU];
	
	if (![self canUseExistingSession])
	{
        if (![ExSystem sharedInstance].isCorpSSOUser) 
        {
           [[ExSystem sharedInstance] clearSession];
        }
        
        // TODO: Why Here always allow autoLogin
        // WHY do we always set autoLogin is true? 
		[self loginAndAllowAutoLogin:true];
	}
	else
	{
        [[MCLogging getInstance] log:@"ApplicationLock::checkSession. Reusing existing session." Level:MC_LOG_DEBU];

        self.isUserLoggedIn = true;

        /*
         The app is launching (as opposed to returning from the background and we have just determined that the existing session is still usable.  Make sure the post-login intialization code runs.  This is the code that does such things as setting up the More button on the iPhone home screen.
        */
        if (isAppLaunching)
            [self doPostLoginInitialization];
        
		[self notifyReceiptStore];
	}
    
}

#pragma mark - Wipe Method

-(void) wipeApplication
{
	[[MCLogging getInstance] log:@"ApplicationLock::wipeApplication" Level:MC_LOG_DEBU];
	[self logout];
	[FileManager cleanCache];
    // Clear keychain also
    KeychainManager *keychainManager = [[KeychainManager alloc] init];
    [keychainManager clearKeychain];

}

#pragma mark - Helper Methods
-(void) dismissAlertViewsAndActionSheets
{
	[[MCLogging getInstance] log:@"ApplicationLock::dismissAlertViewsAndActionSheets" Level:MC_LOG_DEBU];
    
	[MobileAlertView dismissAllMobileAlertViews];
	[MobileActionSheet dismissAllMobileActionSheets];
}

-(void) notifyReceiptStore
{
	ConcurMobileAppDelegate *delegate = [[UIApplication sharedApplication] delegate];
	NSObject *nestedVC = delegate.navController;
	NSObject *vc = nestedVC;
	
	if ([nestedVC isKindOfClass:[UINavigationController class]])
	{
		UINavigationController *nav = (UINavigationController*)nestedVC;
		
		vc = [nav.viewControllers lastObject];
	}
	
	// Mob-5295
	if ([vc isKindOfClass:[ReceiptStoreListView class]])
	{
		ReceiptStoreListView *receiptStoreListVC = (ReceiptStoreListView*)vc;
		if ([receiptStoreListVC respondsToSelector:@selector(applicationEnteredForeground)]) 
		{
			[receiptStoreListVC applicationEnteredForeground];
		}
	}
    else if ([vc isKindOfClass:[TripDetailsViewController class]])
    {
        TripDetailsViewController *tripDetailsVC = (TripDetailsViewController *)vc;
        if ([tripDetailsVC respondsToSelector:@selector(applicationEnteredForeground)]) {
            [tripDetailsVC applicationEnteredForeground];
        }
    }
}

#pragma mark - ExMsgRespondDelegate Methods

-(void) didProcessMessage:(Msg *)msg
{
	[[MCLogging getInstance] log:@"ApplicationLock::didProcessMessage" Level:MC_LOG_DEBU];

	if ([msg.idKey isEqualToString:AUTHENTICATION_DATA])
	{
        //Extract safeHarbor information for Gov user
        Authenticate *auth = (Authenticate*) msg.responder;
        NSDictionary *authData = [[NSMutableDictionary alloc] initWithObjectsAndKeys:auth.needAgreement, @"NEED_SAFEHARBOR", nil];
        if ([Config isGov])
        {
            UIViewController *homevc = [ConcurMobileAppDelegate findHomeVC];
            
            if ([homevc respondsToSelector:@selector(savePrarmetersAfterLogin:)])
            {
                [homevc performSelector:@selector(savePrarmetersAfterLogin:) withObject:authData];
            }
        }

  		[self didAttemptAutoLogin:msg];
	}
    else if ([msg.idKey isEqualToString:VALIDATE_SESSION])
	{  
        self.isAutoLoginInProgress = false;
        
        ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        [delegate destroyAuthenticatingView];
        
        if (msg.responseCode != 200 )
        {
            if (msg.responseCode == 401) // Unauthorized
            {
                [[ExSystem sharedInstance] clearAccessToken];
                [[ExSystem sharedInstance] deleteCorpSSOSessionCookie];
            }
            
            [self showManualLoginViewAfterDelay];
            return;
        }
        else
        {
            self.isUserLoggedIn = true;
            [self doPostLoginInitialization];
        }
    }
    else if ([msg.idKey isEqualToString:REGISTER_PUSH])
    {
        // We don't report anything to the user, but we'll monitor via Flurry (and GDS logs)
        RegisterPush *rp = (RegisterPush*)msg.responder;
        NSMutableDictionary *dictionary = [NSMutableDictionary dictionaryWithObjectsAndKeys:rp.actionStatus.status, @"Status", nil];
        if ([rp.actionStatus.status isEqualToString:@"FAILURE"])
        {
            [dictionary setValue:rp.actionStatus.errMsg forKey:@"Message"];
        }
        
    }

}

#pragma mark - TripIt

- (BOOL) handleOpenURL:(NSURL *)url
{
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::application:handleOpenURL %@", url] Level:MC_LOG_DEBU];
    if ([self handleIfValidLaunchUrl:url])
        return YES;
    
    NSString *cacheKey = [self tryGetTripItCacheKeyFromUrl:url];
    
    if (cacheKey != nil && [cacheKey length] > 0)
    {
        [[MCLogging getInstance] log:@"ApplicationLock:handleOpenURL: got TripIt cache key" Level:MC_LOG_INFO];
        self.tripItCacheKey = cacheKey;
        [self checkForTripItCacheKey];
        return YES;
    }
    
    return NO;
}

- (NSString*) tryGetTripItCacheKeyFromUrl:(NSURL*) url
{
    NSString *scheme = [url scheme];
    if (![@"concursmartexpense" isEqualToString:[scheme lowercaseString]])
    {
        return nil;
    }
    
    NSString *query = [url query];
    if (query == nil || [query length] == 0)
    {
        return nil;
    }
    
    NSString *cacheKeyValue = nil;
    
    NSArray  *paramList = [query componentsSeparatedByString:@"&"];
    for (NSString *param in paramList)
    {
        NSArray *components = [param componentsSeparatedByString:@"="];
        if (components != nil && [components count] == 2)
        {
            NSString *nonLowercaseKey = components[0];
            NSString *key = [nonLowercaseKey lowercaseString];
            NSString *val = components[1];
            
            if ([@"cachekey" isEqualToString:key])
            {
                cacheKeyValue = val;
                break;
            }
        }
    }
    
    return cacheKeyValue;
}

//concurmobile://notification?type=EXP_RPT_APPR
//concurmobile://notification?type=MOB_PIN_RSET
//concurmobile://notification?type=MOB_PWD_RSET
//concurmobile://notification?type=TRV_TRP_APPR
//concurmobile://notification?type=MOB_SSO_LGIN
//concurmobile://notification?type=MOB_SSO_LGIN_SAFARI

-(BOOL) handleIfValidLaunchUrl: (NSURL *)url
{
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::application:handleIfValidLaunchUrl: %@", url] Level:MC_LOG_DEBU];
    
    NSString *scheme = [url scheme];
    if (![@"concurmobile" isEqualToString:[scheme lowercaseString]])
    {
        return nil;
    }

    // handle the url query
    NSString *query = [url query];
    
    if (query == nil || [query length] == 0)
    {
        return nil;
    }
    NSMutableDictionary *queryParams = [self processLaunchQuery:query];

    // decide what to do based on the type
    NSString *typeValue = [queryParams objectForKey:@"type"];

    if (typeValue != nil) {
        NSDictionary *dictionary = @{@"Action": typeValue};
        [Flurry logEvent:@"Email Notification: Action" withParameters:dictionary];
    }

    // handle push notification launch url
    if ([PUSH_NOTIFICATION_TYPE_EXP_RPT_APPR isEqualToString:typeValue] || [PUSH_NOTIFICATION_TYPE_TRV_TRP_APPR isEqualToString:typeValue]) {
        [self handlePushNotificationLaunchWithType:typeValue];
        return YES;
    }

    // handle pin/mobile password / password reset launch url
    if ([MOB_PIN_RSET isEqualToString:typeValue] || [MOB_PWD_RSET isEqualToString:typeValue]) {
        [self handlePinResetLaunchUrlWithKey:[queryParams objectForKey:@"keypart"]];
        return YES;
    }

    // handle sso launch url
    if ([MOB_SSO_LGIN isEqualToString:typeValue]) {
        [self handleSSOLaunchURLWithCompanyCode:[queryParams objectForKey:@"companycode"]];
        return YES;
    }

    // handle sso login from safari
    if([MOB_SSO_LGIN_SAFARI isEqualToString:typeValue]){
        [self handleSSOLaunchWithToken:[queryParams objectForKey:@"token"]];
        return YES;
    }

    return NO;

}

/**
 Handles app launch with Push notification URL
 */
- (void)handlePushNotificationLaunchWithType:(NSString *)type
{
    NotificationEvent* event = [[NotificationEvent alloc] init];
    event.type = type;
    [[NotificationController sharedInstance] processNotificationEvent:event];
}

/**
 Handles app lauch with Pin Reset URL
 */
- (void)handlePinResetLaunchUrlWithKey:(NSString *)key
{
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"ApplicationLock::application:handlePinResetLaunchUrlWithKey: shouldWaitForHomeScreenToLoad %@::", self.shouldWaitForHomeScreenToLoad? @"YES": @"NO"] Level:MC_LOG_DEBU];

    self.resetPinKeyPartB = key;

    if (self.shouldWaitForHomeScreenToLoad) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(openPinCreateView) name:@"MESSAGE_LOGIN_VIEW_HAS_LOADED" object:nil];
    } else {
        [self openPinCreateView];
    }
}

/**
 Handles app launch with SSO company code URL
 */
- (void)handleSSOLaunchURLWithCompanyCode:(NSString *)companyCode
{
    KeychainManager *keychainManager = [[KeychainManager alloc] init];
    [keychainManager saveCompanyCode:companyCode];
    
    
    if (self.shouldWaitForHomeScreenToLoad) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(openSsoLoginView) name:@"MESSAGE_LOGIN_VIEW_HAS_LOADED" object:nil];
    } else {
        [self openSsoLoginView];
    }
}


/**
 Handles app lauch from safari pass us a token.
 */
- (void)handleSSOLaunchWithToken:(NSString *)token
{
    [[ExSystem sharedInstance] saveConcurSSOAccessToken:token];
    
    if (self.shouldWaitForHomeScreenToLoad) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(openSsofromSafari) name:@"MESSAGE_LOGIN_VIEW_HAS_LOADED" object:nil];
    } else {
        [self openSsofromSafari];
    }
    
}

/**
 Convert the query string into a Dictionary
 */
- (NSMutableDictionary *)processLaunchQuery:(NSString *)query
{
    NSMutableDictionary *params = [[NSMutableDictionary alloc] init];

    NSArray  *paramList = [query componentsSeparatedByString:@"&"];
    for (NSString *param in paramList)
    {
        NSArray *components = [param componentsSeparatedByString:@"="];
        if (components != nil && [components count] == 2)
        {
            // we want lower case keys
            NSString *key = [components[0] lowercaseString];
            NSString *val = components[1];
            if (key != nil && val != nil) {
                [params setObject:val forKey:key];
            }
        }
    }
    return params;
}

-(void)openPinCreateView
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"MESSAGE_LOGIN_VIEW_HAS_LOADED" object:nil];
    ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    [delegate switchToPinResetView];
}

-(void)openSsoLoginView
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"MESSAGE_LOGIN_VIEW_HAS_LOADED" object:nil];
    ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    [delegate switchToCompanySignInView];
}

-(void)openSsofromSafari
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"MESSAGE_LOGIN_VIEW_HAS_LOADED" object:nil];
    ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    [delegate switchToSafariSignInView];
}

-(void) checkForTripItCacheKey
{
    if (!self.isLoggedIn)
        [[MCLogging getInstance] log:@"ApplicationLock::checkForTripItCacheKey: not logged in" Level:MC_LOG_DEBU];
    else
    {
        if (self.tripItCacheKey == nil)
            [[MCLogging getInstance] log:@"ApplicationLock::checkForTripItCacheKey: no cache key" Level:MC_LOG_DEBU];
        else
        {
            [[MCLogging getInstance] log:@"ApplicationLock::checkForTripItCacheKey: cache key found" Level:MC_LOG_DEBU];
            
            BOOL isOffline = ![ExSystem connectedToNetwork];
            if (isOffline)
            {
                 MobileAlertView *alert = [[MobileAlertView alloc] 
                                          initWithTitle:[Localizer getLocalizedText:@"Cannot expense trip"]
                                          message:[Localizer getLocalizedText:@"You are offline. Please expense trip later."]
                                          delegate:nil 
                                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                          otherButtonTitles:nil];
                [alert show];
            }
            else
            {
                // Dismiss alert views, action sheets, and bump
                [self dismissAlertViewsAndActionSheets];
                
                // Unwind the view stack
                [ConcurMobileAppDelegate unwindToRootView];
                
                // We probably need a delay to allow the unwind to work in all cases (especially when there are modals of navs in modals, etc)
                TripItExpenserViewController* tripItVC = [[TripItExpenserViewController alloc] init];
                tripItVC.cacheKey = self.tripItCacheKey;
                
                [[ConcurMobileAppDelegate findHomeVC].navigationController pushViewController:tripItVC animated:NO];
                
            }
            
            self.tripItCacheKey = nil;
        }
    }
}

- (NSString*)getResetPinKeypart
{
    return self.resetPinKeyPartB;
}

#pragma mark - Push registration
// Moved from RVC controller.
-(void) registerPhoneForPush: (NSString*)phoneId isTest:(NSString*)isTest
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:

                                 phoneId, @"PHONE_ID"
                                 ,isTest, @"IS_TEST"
								 , nil];

	[[ExSystem sharedInstance].msgControl createMsg:REGISTER_PUSH CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


@end
