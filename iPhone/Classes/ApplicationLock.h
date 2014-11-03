//
//  ApplicationLock.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 11/10/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExMsgRespondDelegate.h"
#import "Flurry.h"

@interface ApplicationLock : NSObject <ExMsgRespondDelegate>{
}

@property (nonatomic, assign) bool		isAutoLoginInProgress;
@property (nonatomic, assign) bool		isManualLoginScheduled;
@property (nonatomic, assign) bool		isApplicationBackgrounded;
@property (nonatomic, strong) NSString  *tripItCacheKey;


// It is assumed that when the application launches or is returned to the foreground, the user is NOT logged in.
// Then we either establish that they are logged in (by having a session that is not expired) or actually log them
// in either automatically or manually.  In all those cases, doPostLoginInitialization should be called and we will
// know that they are logged in.
@property (nonatomic, assign) bool      isUserLoggedIn;

// This will be used to measure the duration of inactivity.
@property (nonatomic, strong) NSDate	*inactivityBeginDate;
@property  BOOL      isShowLoginView;

+(ApplicationLock*)sharedInstance;

// Auto Login
-(bool) canAttemptAutoLogin;
-(bool) canAttemptAutoLoginWithConcurAccessToken;
-(void) attemptAutoLogin;

// Login methods
-(bool) isLoggedIn;
-(void) loginAndAllowAutoLogin:(bool)allowAutoLogin;

// Manual login methods
-(void) showManualLoginViewAfterDelay;
-(void) showManualLoginView;

// Logout method
-(void) logout;

// Event Handlers
-(void) onHomeScreenAppeared;
-(void) onLogoutButtonPressed;
-(void) onServerRejectedRequest;
-(void) onApplicationDidEnterBackground;
-(void) onApplicationWillEnterForeground;
-(void) onApplicationDidFinishLaunching;

// Other Event Handlers
-(void) onLoginSucceeded:(Msg*)msg;  // Called by LoginViewController::respondToFoundData and other places
-(void) onLoginViewAppeared;
-(void) doPostLoginInitialization;

// Checking Methods
-(void) checkConnectionAndSessionWhileLaunching:(BOOL)isAppLaunching;
-(void) checkConnectionWhileLaunching:(BOOL)isAppLaunching;
-(void) checkSession:(BOOL)isAppLaunching;

// Session Methods
-(bool) hasSession;
-(bool) isSessionExpired;
-(bool) canUseExpiredSession;
-(bool) canUseExistingSession;

// Wipe Method
-(void) wipeApplication;

// Helper Methods
-(void) dismissAlertViewsAndActionSheets;
-(void) notifyReceiptStore;

// Alerts
-(void) showOfflineAlert;

// TripIt
-(BOOL) handleOpenURL:(NSURL *)url;
-(NSString*) tryGetTripItCacheKeyFromUrl:(NSURL*) url;
-(void) checkForTripItCacheKey;

// PIN Reset
- (NSString*)getResetPinKeypart;

// RVC method
-(void) registerPhoneForPush: (NSString*)phoneId isTest:(NSString*)xId;

@end
