//
//  AppsUtil.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/4/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityOffer.h"

@interface AppsUtil : NSObject
+(void)launchTaxiMagicApp;
+(void)launchMetroApp;
+(void)launchUberApp;
+(void)launchTripItApp;
+(void)launchExpenseItApp;
+(void)launchGateGuruAppWithUrl:(NSString *)urlString;
+(void)launchOpenTableApp;
+(void)launchMapsWithOffer:(EntityOffer*)offer;
+(void)launchSUApp;
+(void)launchCorpApp;
+(void)launchTravelTextApp;
+(void)launchChatterApp;

+(void)showTestDrivePrivacyLink;
+(void)showTestDriveTermsofUse;
@end
