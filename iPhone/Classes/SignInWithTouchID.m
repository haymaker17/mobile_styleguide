//
//  SignInWithTouchID.m
//  ConcurMobile
//
//  Created by Shifan Wu on 8/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SignInWithTouchID.h"
#import "CTELogin.h"
#import "CTENetworkSettings.h"
#import "KeychainManager.h"
#import "AnalyticsTracker.h"
#import "SignInPasswordViewController.h"
#import "WaitViewController.h"
#import "UserDefaultsManager.h"

@import LocalAuthentication;

@interface SignInWithTouchID ()
@property (nonatomic, strong) KeychainManager *kcManager;
@end

@implementation SignInWithTouchID

- (instancetype)init
{
    self = [super init];
    
    if (self) {
        _kcManager = [[KeychainManager alloc] init];
    }
    return  self;
}

- (NSString *)loadACLuserID
{
    return [_kcManager loadACLuserID];
}

- (void)saveACLuserID:(NSString*)ACLuserID
{
    [_kcManager saveACLuserID:ACLuserID];
}

- (void)clearACLuserID
{
    [_kcManager clearACLuserID];
}

- (NSString *)loadACLpassword
{
   return [_kcManager loadACLpassword];
}

- (void)saveACLpassword:(NSString *)ACLpassword
{
    [_kcManager saveACLpassword:ACLpassword];
}

- (void)clearACLpassword
{
    [_kcManager clearACLpassword];
}

+ (BOOL)canEvaluatePolicy
{
    LAContext *context = [[LAContext alloc] init];
    __block  NSString *msg;
    NSError *error;
    BOOL success;
    
    // test if we can evaluate the policy, this test will tell us if Touch ID is available and enrolled
    success = [context canEvaluatePolicy: LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error];
    if (success) {
        msg =[NSString stringWithFormat:NSLocalizedString(@"TOUCH_ID_IS_AVAILABLE", nil)];
        DLog(@"%@" ,msg);
    } else {
        msg =[NSString stringWithFormat:NSLocalizedString(@"TOUCH_ID_IS_NOT_AVAILABLE", nil)];
        DLog(@"%@" ,msg);
    }
    
    return success;
}

- (void)signInWithUserName:(NSString *)userName Passwrod:(NSString *)password
{
    // Check if you are offline
    if (![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"Please wait until you are online to login"]
                              delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
        [alert show];
        
        NSString *eventLabel = [NSString stringWithFormat:@"Type: %@", @"Offline"];
        [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Failed Attempt" eventLabel:eventLabel eventValue:nil];
        return;
    }
    
    // Validate password
    if(![password lengthIgnoreWhitespace]) {
        // Show error message for
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Incorrect Password"]
                                                             message:[Localizer getLocalizedText:@"Try again to re-enter your password or select I Forgot to receive signin in instructions"]
                                                            delegate:self
                                                   cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"]
                                                   otherButtonTitles:[Localizer getLocalizedText:@"I Forgot"] ,nil];
        [av show];
        return;
    }
    // After validations are done make the networking call
    [WaitViewController showWithText:@"" animated:YES];
    SignInPasswordViewController *vc = [[SignInPasswordViewController alloc] init];
    vc.userId = userName;
    vc.secret = password;
    vc.signInUserType = [UserDefaultsManager getSignInUserType];
    [CTELogin loginConcurMobileWithUsername:userName Password:password success:^(NSString *loginXML) {
        //MOB-19311 : Dont have to set the useragent string anymore. The mws will pick it up after the session.
        [[CTENetworkSettings sharedInstance] setUserAgentString:nil];
        ALog(@"::User %@ Login successful::" , userName);
        [vc loginAndShowHome:loginXML];
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        
    } failure:^(CTEError *error) {
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        [vc handleSignInError:error];
    }];
}

@end
