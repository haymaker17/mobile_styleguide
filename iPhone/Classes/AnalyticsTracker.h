//
//  AnalyticsTracker.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 2/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "GAI.h"

@interface AnalyticsTracker : NSObject

+ (void)initAnalytics;
+ (void)updateCID:(NSString *)clientID;
#pragma mark - generic methods
+ (void)initializeScreenName:(NSString *)screenName;
+ (void)resetScreenName;
+ (void)dispatchAnalytics;

+ (void)logEventWithCategory:(NSString *)category eventAction:(NSString *)action eventLabel:(NSString *)label eventValue:(NSNumber *)value;

@end
