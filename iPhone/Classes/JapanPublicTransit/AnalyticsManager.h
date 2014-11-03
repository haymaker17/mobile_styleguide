//
//  AnalyticsManager.h
//  ConcurMobile
//
//  Created by Richard Puckett on 11/6/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXStack.h"

@interface AnalyticsManager : NSObject

// Stack to record actual path; map for fast (simple) lookup.
//
@property(strong, nonatomic) CXStack *impressionStack;
@property(strong, nonatomic) NSMutableDictionary *impressionMap;

+ (AnalyticsManager *)sharedInstance;

- (void)clearImpressionPath;

- (void)logCategory:(NSString *)category
           withName:(NSString *)name;

- (void)logCategory:(NSString *)category
           withName:(NSString *)name
            andType:(NSString *)type;

- (void)logCategory:(NSString *)category
           withName:(NSString *)name
      fromAncestors:(NSArray *)names;

- (void)logCategory:(NSString *)category
           withName:(NSString *)name
               from:(NSString *)name;

- (void)popImpression:(NSString *)name;

- (void)pushImpression:(NSString *)name;

@end
