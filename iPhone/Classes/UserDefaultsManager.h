//
//  UserDefaultsManager.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/30/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RotatingBaseVC.h"

@interface UserDefaultsManager : NSObject

+(void)setSignInPasswordResetDeviceVerificationKey:(NSString *)signInPasswordResetDeviceVerificationKey;
+ (NSString*) getSignInPasswordResetDeviceVerificationKey;

+(void)setSignInUserEmail:(NSString *)signInUserEmail;
+(NSString *)getSignInUserEmail;

+(void)setSignInUserType:(SignInUserType)signInUserEmail;
+(SignInUserType)getSignInUserType;

+(void)setSignPasswordHelpText:(NSString *)signPasswordHelpText;
+(NSString *)getSignPasswordHelpText;

+(void)setSignInUserId:(NSString *)signInUserId;
+(NSString *)getSignInUserId;

+ (BOOL)isOfferAvailable;
+ (void)setOfferAvailable:(BOOL)available;

@end
