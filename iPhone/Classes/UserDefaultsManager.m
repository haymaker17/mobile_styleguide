//
//  UserDefaultsManager.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/30/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "UserDefaultsManager.h"

// TODO : Move all other userdefaults to one place.

@implementation UserDefaultsManager

NSString * const constSignInUserType = @"SignInUserType";
NSString * const constSignInUserEmail = @"SignInUserEmail";
NSString * const constSignInPasswordResetDeviceVerificationKey = @"SignInPasswordResetDeviceVerificationKey";
NSString * const constSignPasswordHelpText = @"SignPasswordHelpText";
NSString * const constSignInUserId = @"SignInUserId";
NSString * const constFusion14DemoMode = @"Fusion14DemoMode";

+(void)setSignInPasswordResetDeviceVerificationKey:(NSString *)signInPasswordResetDeviceVerificationKey
{
    DLog(@"Setting VerificationKey to: %@", signInPasswordResetDeviceVerificationKey);
    [[NSUserDefaults standardUserDefaults] setObject:signInPasswordResetDeviceVerificationKey forKey:constSignInPasswordResetDeviceVerificationKey];
    [[NSUserDefaults standardUserDefaults] synchronize];
    
}

//Getter method
+ (NSString*) getSignInPasswordResetDeviceVerificationKey
{
    
    return [[NSUserDefaults standardUserDefaults]  objectForKey:constSignInPasswordResetDeviceVerificationKey];
}


+(void)setSignInUserEmail:(NSString *)signInUserEmail
{
    [[NSUserDefaults standardUserDefaults] setObject:signInUserEmail forKey:constSignInUserEmail];
    [[NSUserDefaults standardUserDefaults] synchronize];
    
}


+(NSString *)getSignInUserEmail
{

    return [[NSUserDefaults standardUserDefaults]  objectForKey:constSignInUserEmail];

}

+(void)setSignInUserType:(SignInUserType)signInUserType
{
    [[NSUserDefaults standardUserDefaults] setInteger:signInUserType forKey:constSignInUserType];
    [[NSUserDefaults standardUserDefaults] synchronize];
}


+(SignInUserType)getSignInUserType
{
    NSInteger userType = [[NSUserDefaults standardUserDefaults]  integerForKey:constSignInUserType];
    return userType ;
}

+(void)setSignPasswordHelpText:(NSString *)signPasswordHelpText
{
    [[NSUserDefaults standardUserDefaults] setObject:signPasswordHelpText forKey:constSignPasswordHelpText];
    [[NSUserDefaults standardUserDefaults] synchronize];

}

+(NSString *)getSignPasswordHelpText
{
    return [[NSUserDefaults standardUserDefaults]  objectForKey:constSignPasswordHelpText];
}

+(void)setSignInUserId:(NSString *)signInUserId
{
    [[NSUserDefaults standardUserDefaults] setObject:signInUserId forKey:constSignInUserId];
    [[NSUserDefaults standardUserDefaults] synchronize];
    
}

+(NSString *)getSignInUserId
{
    return [[NSUserDefaults standardUserDefaults]  objectForKey:constSignInUserId];
}


#pragma mark - IPM Offers

+ (BOOL)isOfferAvailable {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    
    return [userDefaults boolForKey:@"ipm_offer_available"];
}

+ (void)setOfferAvailable:(BOOL)available {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    
    [userDefaults setBool:available forKey:@"ipm_offer_available"];
    
    [userDefaults synchronize];
}

@end
